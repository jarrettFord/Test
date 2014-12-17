/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufUtil;
/*   5:    */ 
/*   6:    */ public class Snappy
/*   7:    */ {
/*   8:    */   private static final int MAX_HT_SIZE = 16384;
/*   9:    */   private static final int MIN_COMPRESSIBLE_BYTES = 15;
/*  10:    */   private static final int PREAMBLE_NOT_FULL = -1;
/*  11:    */   private static final int NOT_ENOUGH_INPUT = -1;
/*  12:    */   private static final int LITERAL = 0;
/*  13:    */   private static final int COPY_1_BYTE_OFFSET = 1;
/*  14:    */   private static final int COPY_2_BYTE_OFFSET = 2;
/*  15:    */   private static final int COPY_4_BYTE_OFFSET = 3;
/*  16:    */   private State state;
/*  17:    */   private byte tag;
/*  18:    */   private int written;
/*  19:    */   
/*  20:    */   public Snappy()
/*  21:    */   {
/*  22: 42 */     this.state = State.READY;
/*  23:    */   }
/*  24:    */   
/*  25:    */   private static enum State
/*  26:    */   {
/*  27: 47 */     READY,  READING_PREAMBLE,  READING_TAG,  READING_LITERAL,  READING_COPY;
/*  28:    */     
/*  29:    */     private State() {}
/*  30:    */   }
/*  31:    */   
/*  32:    */   public void reset()
/*  33:    */   {
/*  34: 55 */     this.state = State.READY;
/*  35: 56 */     this.tag = 0;
/*  36: 57 */     this.written = 0;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void encode(ByteBuf in, ByteBuf out, int length)
/*  40:    */   {
/*  41: 62 */     for (int i = 0;; i++)
/*  42:    */     {
/*  43: 63 */       int b = length >>> i * 7;
/*  44: 64 */       if ((b & 0xFFFFFF80) != 0)
/*  45:    */       {
/*  46: 65 */         out.writeByte(b & 0x7F | 0x80);
/*  47:    */       }
/*  48:    */       else
/*  49:    */       {
/*  50: 67 */         out.writeByte(b);
/*  51: 68 */         break;
/*  52:    */       }
/*  53:    */     }
/*  54: 72 */     int inIndex = in.readerIndex();
/*  55: 73 */     int baseIndex = in.readerIndex();
/*  56: 74 */     int maxIndex = length;
/*  57:    */     
/*  58: 76 */     short[] table = getHashTable(maxIndex);
/*  59: 77 */     int shift = 32 - (int)Math.floor(Math.log(table.length) / Math.log(2.0D));
/*  60:    */     
/*  61: 79 */     int nextEmit = inIndex;
/*  62: 81 */     if (maxIndex - inIndex >= 15)
/*  63:    */     {
/*  64: 82 */       int nextHash = hash(in, ++inIndex, shift);
/*  65:    */       for (;;)
/*  66:    */       {
/*  67: 84 */         int skip = 32;
/*  68:    */         
/*  69:    */ 
/*  70: 87 */         int nextIndex = inIndex;
/*  71:    */         int candidate;
/*  72:    */         do
/*  73:    */         {
/*  74: 89 */           inIndex = nextIndex;
/*  75: 90 */           int hash = nextHash;
/*  76: 91 */           int bytesBetweenHashLookups = skip++ >> 5;
/*  77: 92 */           nextIndex = inIndex + bytesBetweenHashLookups;
/*  78: 95 */           if (nextIndex > maxIndex - 4) {
/*  79:    */             break;
/*  80:    */           }
/*  81: 99 */           nextHash = hash(in, nextIndex, shift);
/*  82:    */           
/*  83:101 */           candidate = baseIndex + table[hash];
/*  84:    */           
/*  85:103 */           table[hash] = ((short)(inIndex - baseIndex));
/*  86:105 */         } while (in.getInt(inIndex) != in.getInt(candidate));
/*  87:107 */         encodeLiteral(in, out, inIndex - nextEmit);
/*  88:    */         int insertTail;
/*  89:    */         do
/*  90:    */         {
/*  91:111 */           int base = inIndex;
/*  92:112 */           int matched = 4 + findMatchingLength(in, candidate + 4, inIndex + 4, maxIndex);
/*  93:113 */           inIndex += matched;
/*  94:114 */           int offset = base - candidate;
/*  95:115 */           encodeCopy(out, offset, matched);
/*  96:116 */           in.readerIndex(in.readerIndex() + matched);
/*  97:117 */           insertTail = inIndex - 1;
/*  98:118 */           nextEmit = inIndex;
/*  99:119 */           if (inIndex >= maxIndex - 4) {
/* 100:    */             break;
/* 101:    */           }
/* 102:123 */           int prevHash = hash(in, insertTail, shift);
/* 103:124 */           table[prevHash] = ((short)(inIndex - baseIndex - 1));
/* 104:125 */           int currentHash = hash(in, insertTail + 1, shift);
/* 105:126 */           candidate = baseIndex + table[currentHash];
/* 106:127 */           table[currentHash] = ((short)(inIndex - baseIndex));
/* 107:129 */         } while (in.getInt(insertTail + 1) == in.getInt(candidate));
/* 108:131 */         nextHash = hash(in, insertTail + 2, shift);
/* 109:132 */         inIndex++;
/* 110:    */       }
/* 111:    */     }
/* 112:137 */     if (nextEmit < maxIndex) {
/* 113:138 */       encodeLiteral(in, out, maxIndex - nextEmit);
/* 114:    */     }
/* 115:    */   }
/* 116:    */   
/* 117:    */   private static int hash(ByteBuf in, int index, int shift)
/* 118:    */   {
/* 119:153 */     return in.getInt(index) + 506832829 >>> shift;
/* 120:    */   }
/* 121:    */   
/* 122:    */   private static short[] getHashTable(int inputSize)
/* 123:    */   {
/* 124:163 */     int htSize = 256;
/* 125:164 */     while ((htSize < 16384) && (htSize < inputSize)) {
/* 126:165 */       htSize <<= 1;
/* 127:    */     }
/* 128:    */     short[] table;
/* 129:    */     short[] table;
/* 130:169 */     if (htSize <= 256) {
/* 131:170 */       table = new short[256];
/* 132:    */     } else {
/* 133:172 */       table = new short[16384];
/* 134:    */     }
/* 135:175 */     return table;
/* 136:    */   }
/* 137:    */   
/* 138:    */   private static int findMatchingLength(ByteBuf in, int minIndex, int inIndex, int maxIndex)
/* 139:    */   {
/* 140:190 */     int matched = 0;
/* 141:192 */     while ((inIndex <= maxIndex - 4) && (in.getInt(inIndex) == in.getInt(minIndex + matched)))
/* 142:    */     {
/* 143:194 */       inIndex += 4;
/* 144:195 */       matched += 4;
/* 145:    */     }
/* 146:198 */     while ((inIndex < maxIndex) && (in.getByte(minIndex + matched) == in.getByte(inIndex)))
/* 147:    */     {
/* 148:199 */       inIndex++;
/* 149:200 */       matched++;
/* 150:    */     }
/* 151:203 */     return matched;
/* 152:    */   }
/* 153:    */   
/* 154:    */   private static int bitsToEncode(int value)
/* 155:    */   {
/* 156:215 */     int highestOneBit = Integer.highestOneBit(value);
/* 157:216 */     int bitLength = 0;
/* 158:217 */     while (highestOneBit >>= 1 != 0) {
/* 159:218 */       bitLength++;
/* 160:    */     }
/* 161:221 */     return bitLength;
/* 162:    */   }
/* 163:    */   
/* 164:    */   private static void encodeLiteral(ByteBuf in, ByteBuf out, int length)
/* 165:    */   {
/* 166:234 */     if (length < 61)
/* 167:    */     {
/* 168:235 */       out.writeByte(length - 1 << 2);
/* 169:    */     }
/* 170:    */     else
/* 171:    */     {
/* 172:237 */       int bitLength = bitsToEncode(length - 1);
/* 173:238 */       int bytesToEncode = 1 + bitLength / 8;
/* 174:239 */       out.writeByte(59 + bytesToEncode << 2);
/* 175:240 */       for (int i = 0; i < bytesToEncode; i++) {
/* 176:241 */         out.writeByte(length - 1 >> i * 8 & 0xFF);
/* 177:    */       }
/* 178:    */     }
/* 179:245 */     out.writeBytes(in, length);
/* 180:    */   }
/* 181:    */   
/* 182:    */   private static void encodeCopyWithOffset(ByteBuf out, int offset, int length)
/* 183:    */   {
/* 184:249 */     if ((length < 12) && (offset < 2048))
/* 185:    */     {
/* 186:250 */       out.writeByte(0x1 | length - 4 << 2 | offset >> 8 << 5);
/* 187:251 */       out.writeByte(offset & 0xFF);
/* 188:    */     }
/* 189:    */     else
/* 190:    */     {
/* 191:253 */       out.writeByte(0x2 | length - 1 << 2);
/* 192:254 */       out.writeByte(offset & 0xFF);
/* 193:255 */       out.writeByte(offset >> 8 & 0xFF);
/* 194:    */     }
/* 195:    */   }
/* 196:    */   
/* 197:    */   private static void encodeCopy(ByteBuf out, int offset, int length)
/* 198:    */   {
/* 199:267 */     while (length >= 68)
/* 200:    */     {
/* 201:268 */       encodeCopyWithOffset(out, offset, 64);
/* 202:269 */       length -= 64;
/* 203:    */     }
/* 204:272 */     if (length > 64)
/* 205:    */     {
/* 206:273 */       encodeCopyWithOffset(out, offset, 60);
/* 207:274 */       length -= 60;
/* 208:    */     }
/* 209:277 */     encodeCopyWithOffset(out, offset, length);
/* 210:    */   }
/* 211:    */   
/* 212:    */   public void decode(ByteBuf in, ByteBuf out)
/* 213:    */   {
/* 214:281 */     while (in.isReadable()) {
/* 215:282 */       switch (1.$SwitchMap$io$netty$handler$codec$compression$Snappy$State[this.state.ordinal()])
/* 216:    */       {
/* 217:    */       case 1: 
/* 218:284 */         this.state = State.READING_PREAMBLE;
/* 219:    */       case 2: 
/* 220:286 */         int uncompressedLength = readPreamble(in);
/* 221:287 */         if (uncompressedLength == -1) {
/* 222:289 */           return;
/* 223:    */         }
/* 224:291 */         if (uncompressedLength == 0)
/* 225:    */         {
/* 226:293 */           this.state = State.READY;
/* 227:294 */           return;
/* 228:    */         }
/* 229:296 */         out.ensureWritable(uncompressedLength);
/* 230:297 */         this.state = State.READING_TAG;
/* 231:    */       case 3: 
/* 232:299 */         if (!in.isReadable()) {
/* 233:300 */           return;
/* 234:    */         }
/* 235:302 */         this.tag = in.readByte();
/* 236:303 */         switch (this.tag & 0x3)
/* 237:    */         {
/* 238:    */         case 0: 
/* 239:305 */           this.state = State.READING_LITERAL;
/* 240:306 */           break;
/* 241:    */         case 1: 
/* 242:    */         case 2: 
/* 243:    */         case 3: 
/* 244:310 */           this.state = State.READING_COPY;
/* 245:    */         }
/* 246:313 */         break;
/* 247:    */       case 4: 
/* 248:315 */         int literalWritten = decodeLiteral(this.tag, in, out);
/* 249:316 */         if (literalWritten != -1)
/* 250:    */         {
/* 251:317 */           this.state = State.READING_TAG;
/* 252:318 */           this.written += literalWritten;
/* 253:    */         }
/* 254:    */         else
/* 255:    */         {
/* 256:    */           return;
/* 257:    */         }
/* 258:    */         break;
/* 259:    */       case 5: 
/* 260:    */         int decodeWritten;
/* 261:326 */         switch (this.tag & 0x3)
/* 262:    */         {
/* 263:    */         case 1: 
/* 264:328 */           decodeWritten = decodeCopyWith1ByteOffset(this.tag, in, out, this.written);
/* 265:329 */           if (decodeWritten != -1)
/* 266:    */           {
/* 267:330 */             this.state = State.READING_TAG;
/* 268:331 */             this.written += decodeWritten;
/* 269:    */           }
/* 270:    */           else
/* 271:    */           {
/* 272:    */             return;
/* 273:    */           }
/* 274:    */           break;
/* 275:    */         case 2: 
/* 276:338 */           decodeWritten = decodeCopyWith2ByteOffset(this.tag, in, out, this.written);
/* 277:339 */           if (decodeWritten != -1)
/* 278:    */           {
/* 279:340 */             this.state = State.READING_TAG;
/* 280:341 */             this.written += decodeWritten;
/* 281:    */           }
/* 282:    */           else
/* 283:    */           {
/* 284:    */             return;
/* 285:    */           }
/* 286:    */           break;
/* 287:    */         case 3: 
/* 288:348 */           decodeWritten = decodeCopyWith4ByteOffset(this.tag, in, out, this.written);
/* 289:349 */           if (decodeWritten != -1)
/* 290:    */           {
/* 291:350 */             this.state = State.READING_TAG;
/* 292:351 */             this.written += decodeWritten;
/* 293:    */           }
/* 294:    */           else
/* 295:    */           {
/* 296:    */             return;
/* 297:    */           }
/* 298:    */           break;
/* 299:    */         }
/* 300:    */         break;
/* 301:    */       }
/* 302:    */     }
/* 303:    */   }
/* 304:    */   
/* 305:    */   private static int readPreamble(ByteBuf in)
/* 306:    */   {
/* 307:372 */     int length = 0;
/* 308:373 */     int byteIndex = 0;
/* 309:374 */     while (in.isReadable())
/* 310:    */     {
/* 311:375 */       int current = in.readUnsignedByte();
/* 312:376 */       length |= (current & 0x7F) << byteIndex++ * 7;
/* 313:377 */       if ((current & 0x80) == 0) {
/* 314:378 */         return length;
/* 315:    */       }
/* 316:381 */       if (byteIndex >= 4) {
/* 317:382 */         throw new DecompressionException("Preamble is greater than 4 bytes");
/* 318:    */       }
/* 319:    */     }
/* 320:386 */     return 0;
/* 321:    */   }
/* 322:    */   
/* 323:    */   private static int decodeLiteral(byte tag, ByteBuf in, ByteBuf out)
/* 324:    */   {
/* 325:401 */     in.markReaderIndex();
/* 326:    */     int length;
/* 327:403 */     switch (tag >> 2 & 0x3F)
/* 328:    */     {
/* 329:    */     case 60: 
/* 330:405 */       if (!in.isReadable()) {
/* 331:406 */         return -1;
/* 332:    */       }
/* 333:408 */       length = in.readUnsignedByte();
/* 334:409 */       break;
/* 335:    */     case 61: 
/* 336:411 */       if (in.readableBytes() < 2) {
/* 337:412 */         return -1;
/* 338:    */       }
/* 339:414 */       length = ByteBufUtil.swapShort(in.readShort());
/* 340:415 */       break;
/* 341:    */     case 62: 
/* 342:417 */       if (in.readableBytes() < 3) {
/* 343:418 */         return -1;
/* 344:    */       }
/* 345:420 */       length = ByteBufUtil.swapMedium(in.readUnsignedMedium());
/* 346:421 */       break;
/* 347:    */     case 64: 
/* 348:423 */       if (in.readableBytes() < 4) {
/* 349:424 */         return -1;
/* 350:    */       }
/* 351:426 */       length = ByteBufUtil.swapInt(in.readInt());
/* 352:427 */       break;
/* 353:    */     case 63: 
/* 354:    */     default: 
/* 355:429 */       length = tag >> 2 & 0x3F;
/* 356:    */     }
/* 357:431 */     length++;
/* 358:433 */     if (in.readableBytes() < length)
/* 359:    */     {
/* 360:434 */       in.resetReaderIndex();
/* 361:435 */       return -1;
/* 362:    */     }
/* 363:438 */     out.writeBytes(in, length);
/* 364:439 */     return length;
/* 365:    */   }
/* 366:    */   
/* 367:    */   private static int decodeCopyWith1ByteOffset(byte tag, ByteBuf in, ByteBuf out, int writtenSoFar)
/* 368:    */   {
/* 369:456 */     if (!in.isReadable()) {
/* 370:457 */       return -1;
/* 371:    */     }
/* 372:460 */     int initialIndex = out.writerIndex();
/* 373:461 */     int length = 4 + ((tag & 0x1C) >> 2);
/* 374:462 */     int offset = (tag & 0xE0) << 8 >> 5 | in.readUnsignedByte();
/* 375:    */     
/* 376:464 */     validateOffset(offset, writtenSoFar);
/* 377:    */     
/* 378:466 */     out.markReaderIndex();
/* 379:467 */     if (offset < length)
/* 380:    */     {
/* 381:468 */       for (int copies = length / offset; copies > 0; copies--)
/* 382:    */       {
/* 383:470 */         out.readerIndex(initialIndex - offset);
/* 384:471 */         out.readBytes(out, offset);
/* 385:    */       }
/* 386:473 */       if (length % offset != 0)
/* 387:    */       {
/* 388:474 */         out.readerIndex(initialIndex - offset);
/* 389:475 */         out.readBytes(out, length % offset);
/* 390:    */       }
/* 391:    */     }
/* 392:    */     else
/* 393:    */     {
/* 394:478 */       out.readerIndex(initialIndex - offset);
/* 395:479 */       out.readBytes(out, length);
/* 396:    */     }
/* 397:481 */     out.resetReaderIndex();
/* 398:    */     
/* 399:483 */     return length;
/* 400:    */   }
/* 401:    */   
/* 402:    */   private static int decodeCopyWith2ByteOffset(byte tag, ByteBuf in, ByteBuf out, int writtenSoFar)
/* 403:    */   {
/* 404:500 */     if (in.readableBytes() < 2) {
/* 405:501 */       return -1;
/* 406:    */     }
/* 407:504 */     int initialIndex = out.writerIndex();
/* 408:505 */     int length = 1 + (tag >> 2 & 0x3F);
/* 409:506 */     int offset = ByteBufUtil.swapShort(in.readShort());
/* 410:    */     
/* 411:508 */     validateOffset(offset, writtenSoFar);
/* 412:    */     
/* 413:510 */     out.markReaderIndex();
/* 414:511 */     if (offset < length)
/* 415:    */     {
/* 416:512 */       for (int copies = length / offset; copies > 0; copies--)
/* 417:    */       {
/* 418:514 */         out.readerIndex(initialIndex - offset);
/* 419:515 */         out.readBytes(out, offset);
/* 420:    */       }
/* 421:517 */       if (length % offset != 0)
/* 422:    */       {
/* 423:518 */         out.readerIndex(initialIndex - offset);
/* 424:519 */         out.readBytes(out, length % offset);
/* 425:    */       }
/* 426:    */     }
/* 427:    */     else
/* 428:    */     {
/* 429:522 */       out.readerIndex(initialIndex - offset);
/* 430:523 */       out.readBytes(out, length);
/* 431:    */     }
/* 432:525 */     out.resetReaderIndex();
/* 433:    */     
/* 434:527 */     return length;
/* 435:    */   }
/* 436:    */   
/* 437:    */   private static int decodeCopyWith4ByteOffset(byte tag, ByteBuf in, ByteBuf out, int writtenSoFar)
/* 438:    */   {
/* 439:544 */     if (in.readableBytes() < 4) {
/* 440:545 */       return -1;
/* 441:    */     }
/* 442:548 */     int initialIndex = out.writerIndex();
/* 443:549 */     int length = 1 + (tag >> 2 & 0x3F);
/* 444:550 */     int offset = ByteBufUtil.swapInt(in.readInt());
/* 445:    */     
/* 446:552 */     validateOffset(offset, writtenSoFar);
/* 447:    */     
/* 448:554 */     out.markReaderIndex();
/* 449:555 */     if (offset < length)
/* 450:    */     {
/* 451:556 */       for (int copies = length / offset; copies > 0; copies--)
/* 452:    */       {
/* 453:558 */         out.readerIndex(initialIndex - offset);
/* 454:559 */         out.readBytes(out, offset);
/* 455:    */       }
/* 456:561 */       if (length % offset != 0)
/* 457:    */       {
/* 458:562 */         out.readerIndex(initialIndex - offset);
/* 459:563 */         out.readBytes(out, length % offset);
/* 460:    */       }
/* 461:    */     }
/* 462:    */     else
/* 463:    */     {
/* 464:566 */       out.readerIndex(initialIndex - offset);
/* 465:567 */       out.readBytes(out, length);
/* 466:    */     }
/* 467:569 */     out.resetReaderIndex();
/* 468:    */     
/* 469:571 */     return length;
/* 470:    */   }
/* 471:    */   
/* 472:    */   private static void validateOffset(int offset, int chunkSizeSoFar)
/* 473:    */   {
/* 474:584 */     if (offset > 32767) {
/* 475:585 */       throw new DecompressionException("Offset exceeds maximum permissible value");
/* 476:    */     }
/* 477:588 */     if (offset <= 0) {
/* 478:589 */       throw new DecompressionException("Offset is less than minimum permissible value");
/* 479:    */     }
/* 480:592 */     if (offset > chunkSizeSoFar) {
/* 481:593 */       throw new DecompressionException("Offset exceeds size of chunk");
/* 482:    */     }
/* 483:    */   }
/* 484:    */   
/* 485:    */   public static int calculateChecksum(ByteBuf data)
/* 486:    */   {
/* 487:604 */     return calculateChecksum(data, data.readerIndex(), data.readableBytes());
/* 488:    */   }
/* 489:    */   
/* 490:    */   public static int calculateChecksum(ByteBuf data, int offset, int length)
/* 491:    */   {
/* 492:614 */     Crc32c crc32 = new Crc32c();
/* 493:    */     try
/* 494:    */     {
/* 495:    */       byte[] array;
/* 496:616 */       if (data.hasArray())
/* 497:    */       {
/* 498:617 */         crc32.update(data.array(), data.arrayOffset() + offset, length);
/* 499:    */       }
/* 500:    */       else
/* 501:    */       {
/* 502:619 */         array = new byte[length];
/* 503:620 */         data.getBytes(offset, array);
/* 504:621 */         crc32.update(array, 0, length);
/* 505:    */       }
/* 506:624 */       return maskChecksum((int)crc32.getValue());
/* 507:    */     }
/* 508:    */     finally
/* 509:    */     {
/* 510:626 */       crc32.reset();
/* 511:    */     }
/* 512:    */   }
/* 513:    */   
/* 514:    */   static void validateChecksum(int expectedChecksum, ByteBuf data)
/* 515:    */   {
/* 516:640 */     validateChecksum(expectedChecksum, data, data.readerIndex(), data.readableBytes());
/* 517:    */   }
/* 518:    */   
/* 519:    */   static void validateChecksum(int expectedChecksum, ByteBuf data, int offset, int length)
/* 520:    */   {
/* 521:653 */     int actualChecksum = calculateChecksum(data, offset, length);
/* 522:654 */     if (actualChecksum != expectedChecksum) {
/* 523:655 */       throw new DecompressionException("mismatching checksum: " + Integer.toHexString(actualChecksum) + " (expected: " + Integer.toHexString(expectedChecksum) + ')');
/* 524:    */     }
/* 525:    */   }
/* 526:    */   
/* 527:    */   static int maskChecksum(int checksum)
/* 528:    */   {
/* 529:673 */     return (checksum >> 15 | checksum << 17) + -1568478504;
/* 530:    */   }
/* 531:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.compression.Snappy
 * JD-Core Version:    0.7.0.1
 */