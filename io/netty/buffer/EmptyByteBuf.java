/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.EmptyArrays;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import io.netty.util.internal.StringUtil;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.io.OutputStream;
/*   8:    */ import java.nio.ByteBuffer;
/*   9:    */ import java.nio.ByteOrder;
/*  10:    */ import java.nio.ReadOnlyBufferException;
/*  11:    */ import java.nio.channels.GatheringByteChannel;
/*  12:    */ import java.nio.channels.ScatteringByteChannel;
/*  13:    */ import java.nio.charset.Charset;
/*  14:    */ 
/*  15:    */ public final class EmptyByteBuf
/*  16:    */   extends ByteBuf
/*  17:    */ {
/*  18: 37 */   private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocateDirect(0);
/*  19:    */   private static final long EMPTY_BYTE_BUFFER_ADDRESS;
/*  20:    */   private final ByteBufAllocator alloc;
/*  21:    */   private final ByteOrder order;
/*  22:    */   private final String str;
/*  23:    */   private EmptyByteBuf swapped;
/*  24:    */   
/*  25:    */   static
/*  26:    */   {
/*  27: 41 */     long emptyByteBufferAddress = 0L;
/*  28:    */     try
/*  29:    */     {
/*  30: 43 */       if (PlatformDependent.hasUnsafe()) {
/*  31: 44 */         emptyByteBufferAddress = PlatformDependent.directBufferAddress(EMPTY_BYTE_BUFFER);
/*  32:    */       }
/*  33:    */     }
/*  34:    */     catch (Throwable t) {}
/*  35: 49 */     EMPTY_BYTE_BUFFER_ADDRESS = emptyByteBufferAddress;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public EmptyByteBuf(ByteBufAllocator alloc)
/*  39:    */   {
/*  40: 58 */     this(alloc, ByteOrder.BIG_ENDIAN);
/*  41:    */   }
/*  42:    */   
/*  43:    */   private EmptyByteBuf(ByteBufAllocator alloc, ByteOrder order)
/*  44:    */   {
/*  45: 62 */     if (alloc == null) {
/*  46: 63 */       throw new NullPointerException("alloc");
/*  47:    */     }
/*  48: 66 */     this.alloc = alloc;
/*  49: 67 */     this.order = order;
/*  50: 68 */     this.str = (StringUtil.simpleClassName(this) + (order == ByteOrder.BIG_ENDIAN ? "BE" : "LE"));
/*  51:    */   }
/*  52:    */   
/*  53:    */   public int capacity()
/*  54:    */   {
/*  55: 73 */     return 0;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public ByteBuf capacity(int newCapacity)
/*  59:    */   {
/*  60: 78 */     throw new ReadOnlyBufferException();
/*  61:    */   }
/*  62:    */   
/*  63:    */   public ByteBufAllocator alloc()
/*  64:    */   {
/*  65: 83 */     return this.alloc;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public ByteOrder order()
/*  69:    */   {
/*  70: 88 */     return this.order;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public ByteBuf unwrap()
/*  74:    */   {
/*  75: 93 */     return null;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public boolean isDirect()
/*  79:    */   {
/*  80: 98 */     return true;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public int maxCapacity()
/*  84:    */   {
/*  85:103 */     return 0;
/*  86:    */   }
/*  87:    */   
/*  88:    */   public ByteBuf order(ByteOrder endianness)
/*  89:    */   {
/*  90:108 */     if (endianness == null) {
/*  91:109 */       throw new NullPointerException("endianness");
/*  92:    */     }
/*  93:111 */     if (endianness == order()) {
/*  94:112 */       return this;
/*  95:    */     }
/*  96:115 */     EmptyByteBuf swapped = this.swapped;
/*  97:116 */     if (swapped != null) {
/*  98:117 */       return swapped;
/*  99:    */     }
/* 100:120 */     this.swapped = (swapped = new EmptyByteBuf(alloc(), endianness));
/* 101:121 */     return swapped;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public int readerIndex()
/* 105:    */   {
/* 106:126 */     return 0;
/* 107:    */   }
/* 108:    */   
/* 109:    */   public ByteBuf readerIndex(int readerIndex)
/* 110:    */   {
/* 111:131 */     return checkIndex(readerIndex);
/* 112:    */   }
/* 113:    */   
/* 114:    */   public int writerIndex()
/* 115:    */   {
/* 116:136 */     return 0;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public ByteBuf writerIndex(int writerIndex)
/* 120:    */   {
/* 121:141 */     return checkIndex(writerIndex);
/* 122:    */   }
/* 123:    */   
/* 124:    */   public ByteBuf setIndex(int readerIndex, int writerIndex)
/* 125:    */   {
/* 126:146 */     checkIndex(readerIndex);
/* 127:147 */     checkIndex(writerIndex);
/* 128:148 */     return this;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public int readableBytes()
/* 132:    */   {
/* 133:153 */     return 0;
/* 134:    */   }
/* 135:    */   
/* 136:    */   public int writableBytes()
/* 137:    */   {
/* 138:158 */     return 0;
/* 139:    */   }
/* 140:    */   
/* 141:    */   public int maxWritableBytes()
/* 142:    */   {
/* 143:163 */     return 0;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public boolean isReadable()
/* 147:    */   {
/* 148:168 */     return false;
/* 149:    */   }
/* 150:    */   
/* 151:    */   public boolean isWritable()
/* 152:    */   {
/* 153:173 */     return false;
/* 154:    */   }
/* 155:    */   
/* 156:    */   public ByteBuf clear()
/* 157:    */   {
/* 158:178 */     return this;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public ByteBuf markReaderIndex()
/* 162:    */   {
/* 163:183 */     return this;
/* 164:    */   }
/* 165:    */   
/* 166:    */   public ByteBuf resetReaderIndex()
/* 167:    */   {
/* 168:188 */     return this;
/* 169:    */   }
/* 170:    */   
/* 171:    */   public ByteBuf markWriterIndex()
/* 172:    */   {
/* 173:193 */     return this;
/* 174:    */   }
/* 175:    */   
/* 176:    */   public ByteBuf resetWriterIndex()
/* 177:    */   {
/* 178:198 */     return this;
/* 179:    */   }
/* 180:    */   
/* 181:    */   public ByteBuf discardReadBytes()
/* 182:    */   {
/* 183:203 */     return this;
/* 184:    */   }
/* 185:    */   
/* 186:    */   public ByteBuf discardSomeReadBytes()
/* 187:    */   {
/* 188:208 */     return this;
/* 189:    */   }
/* 190:    */   
/* 191:    */   public ByteBuf ensureWritable(int minWritableBytes)
/* 192:    */   {
/* 193:213 */     if (minWritableBytes < 0) {
/* 194:214 */       throw new IllegalArgumentException("minWritableBytes: " + minWritableBytes + " (expected: >= 0)");
/* 195:    */     }
/* 196:216 */     if (minWritableBytes != 0) {
/* 197:217 */       throw new IndexOutOfBoundsException();
/* 198:    */     }
/* 199:219 */     return this;
/* 200:    */   }
/* 201:    */   
/* 202:    */   public int ensureWritable(int minWritableBytes, boolean force)
/* 203:    */   {
/* 204:224 */     if (minWritableBytes < 0) {
/* 205:225 */       throw new IllegalArgumentException("minWritableBytes: " + minWritableBytes + " (expected: >= 0)");
/* 206:    */     }
/* 207:228 */     if (minWritableBytes == 0) {
/* 208:229 */       return 0;
/* 209:    */     }
/* 210:232 */     return 1;
/* 211:    */   }
/* 212:    */   
/* 213:    */   public boolean getBoolean(int index)
/* 214:    */   {
/* 215:237 */     throw new IndexOutOfBoundsException();
/* 216:    */   }
/* 217:    */   
/* 218:    */   public byte getByte(int index)
/* 219:    */   {
/* 220:242 */     throw new IndexOutOfBoundsException();
/* 221:    */   }
/* 222:    */   
/* 223:    */   public short getUnsignedByte(int index)
/* 224:    */   {
/* 225:247 */     throw new IndexOutOfBoundsException();
/* 226:    */   }
/* 227:    */   
/* 228:    */   public short getShort(int index)
/* 229:    */   {
/* 230:252 */     throw new IndexOutOfBoundsException();
/* 231:    */   }
/* 232:    */   
/* 233:    */   public int getUnsignedShort(int index)
/* 234:    */   {
/* 235:257 */     throw new IndexOutOfBoundsException();
/* 236:    */   }
/* 237:    */   
/* 238:    */   public int getMedium(int index)
/* 239:    */   {
/* 240:262 */     throw new IndexOutOfBoundsException();
/* 241:    */   }
/* 242:    */   
/* 243:    */   public int getUnsignedMedium(int index)
/* 244:    */   {
/* 245:267 */     throw new IndexOutOfBoundsException();
/* 246:    */   }
/* 247:    */   
/* 248:    */   public int getInt(int index)
/* 249:    */   {
/* 250:272 */     throw new IndexOutOfBoundsException();
/* 251:    */   }
/* 252:    */   
/* 253:    */   public long getUnsignedInt(int index)
/* 254:    */   {
/* 255:277 */     throw new IndexOutOfBoundsException();
/* 256:    */   }
/* 257:    */   
/* 258:    */   public long getLong(int index)
/* 259:    */   {
/* 260:282 */     throw new IndexOutOfBoundsException();
/* 261:    */   }
/* 262:    */   
/* 263:    */   public char getChar(int index)
/* 264:    */   {
/* 265:287 */     throw new IndexOutOfBoundsException();
/* 266:    */   }
/* 267:    */   
/* 268:    */   public float getFloat(int index)
/* 269:    */   {
/* 270:292 */     throw new IndexOutOfBoundsException();
/* 271:    */   }
/* 272:    */   
/* 273:    */   public double getDouble(int index)
/* 274:    */   {
/* 275:297 */     throw new IndexOutOfBoundsException();
/* 276:    */   }
/* 277:    */   
/* 278:    */   public ByteBuf getBytes(int index, ByteBuf dst)
/* 279:    */   {
/* 280:302 */     return checkIndex(index, dst.writableBytes());
/* 281:    */   }
/* 282:    */   
/* 283:    */   public ByteBuf getBytes(int index, ByteBuf dst, int length)
/* 284:    */   {
/* 285:307 */     return checkIndex(index, length);
/* 286:    */   }
/* 287:    */   
/* 288:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 289:    */   {
/* 290:312 */     return checkIndex(index, length);
/* 291:    */   }
/* 292:    */   
/* 293:    */   public ByteBuf getBytes(int index, byte[] dst)
/* 294:    */   {
/* 295:317 */     return checkIndex(index, dst.length);
/* 296:    */   }
/* 297:    */   
/* 298:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 299:    */   {
/* 300:322 */     return checkIndex(index, length);
/* 301:    */   }
/* 302:    */   
/* 303:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 304:    */   {
/* 305:327 */     return checkIndex(index, dst.remaining());
/* 306:    */   }
/* 307:    */   
/* 308:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 309:    */   {
/* 310:332 */     return checkIndex(index, length);
/* 311:    */   }
/* 312:    */   
/* 313:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 314:    */   {
/* 315:337 */     checkIndex(index, length);
/* 316:338 */     return 0;
/* 317:    */   }
/* 318:    */   
/* 319:    */   public ByteBuf setBoolean(int index, boolean value)
/* 320:    */   {
/* 321:343 */     throw new IndexOutOfBoundsException();
/* 322:    */   }
/* 323:    */   
/* 324:    */   public ByteBuf setByte(int index, int value)
/* 325:    */   {
/* 326:348 */     throw new IndexOutOfBoundsException();
/* 327:    */   }
/* 328:    */   
/* 329:    */   public ByteBuf setShort(int index, int value)
/* 330:    */   {
/* 331:353 */     throw new IndexOutOfBoundsException();
/* 332:    */   }
/* 333:    */   
/* 334:    */   public ByteBuf setMedium(int index, int value)
/* 335:    */   {
/* 336:358 */     throw new IndexOutOfBoundsException();
/* 337:    */   }
/* 338:    */   
/* 339:    */   public ByteBuf setInt(int index, int value)
/* 340:    */   {
/* 341:363 */     throw new IndexOutOfBoundsException();
/* 342:    */   }
/* 343:    */   
/* 344:    */   public ByteBuf setLong(int index, long value)
/* 345:    */   {
/* 346:368 */     throw new IndexOutOfBoundsException();
/* 347:    */   }
/* 348:    */   
/* 349:    */   public ByteBuf setChar(int index, int value)
/* 350:    */   {
/* 351:373 */     throw new IndexOutOfBoundsException();
/* 352:    */   }
/* 353:    */   
/* 354:    */   public ByteBuf setFloat(int index, float value)
/* 355:    */   {
/* 356:378 */     throw new IndexOutOfBoundsException();
/* 357:    */   }
/* 358:    */   
/* 359:    */   public ByteBuf setDouble(int index, double value)
/* 360:    */   {
/* 361:383 */     throw new IndexOutOfBoundsException();
/* 362:    */   }
/* 363:    */   
/* 364:    */   public ByteBuf setBytes(int index, ByteBuf src)
/* 365:    */   {
/* 366:388 */     throw new IndexOutOfBoundsException();
/* 367:    */   }
/* 368:    */   
/* 369:    */   public ByteBuf setBytes(int index, ByteBuf src, int length)
/* 370:    */   {
/* 371:393 */     return checkIndex(index, length);
/* 372:    */   }
/* 373:    */   
/* 374:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 375:    */   {
/* 376:398 */     return checkIndex(index, length);
/* 377:    */   }
/* 378:    */   
/* 379:    */   public ByteBuf setBytes(int index, byte[] src)
/* 380:    */   {
/* 381:403 */     return checkIndex(index, src.length);
/* 382:    */   }
/* 383:    */   
/* 384:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 385:    */   {
/* 386:408 */     return checkIndex(index, length);
/* 387:    */   }
/* 388:    */   
/* 389:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 390:    */   {
/* 391:413 */     return checkIndex(index, src.remaining());
/* 392:    */   }
/* 393:    */   
/* 394:    */   public int setBytes(int index, InputStream in, int length)
/* 395:    */   {
/* 396:418 */     checkIndex(index, length);
/* 397:419 */     return 0;
/* 398:    */   }
/* 399:    */   
/* 400:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 401:    */   {
/* 402:424 */     checkIndex(index, length);
/* 403:425 */     return 0;
/* 404:    */   }
/* 405:    */   
/* 406:    */   public ByteBuf setZero(int index, int length)
/* 407:    */   {
/* 408:430 */     return checkIndex(index, length);
/* 409:    */   }
/* 410:    */   
/* 411:    */   public boolean readBoolean()
/* 412:    */   {
/* 413:435 */     throw new IndexOutOfBoundsException();
/* 414:    */   }
/* 415:    */   
/* 416:    */   public byte readByte()
/* 417:    */   {
/* 418:440 */     throw new IndexOutOfBoundsException();
/* 419:    */   }
/* 420:    */   
/* 421:    */   public short readUnsignedByte()
/* 422:    */   {
/* 423:445 */     throw new IndexOutOfBoundsException();
/* 424:    */   }
/* 425:    */   
/* 426:    */   public short readShort()
/* 427:    */   {
/* 428:450 */     throw new IndexOutOfBoundsException();
/* 429:    */   }
/* 430:    */   
/* 431:    */   public int readUnsignedShort()
/* 432:    */   {
/* 433:455 */     throw new IndexOutOfBoundsException();
/* 434:    */   }
/* 435:    */   
/* 436:    */   public int readMedium()
/* 437:    */   {
/* 438:460 */     throw new IndexOutOfBoundsException();
/* 439:    */   }
/* 440:    */   
/* 441:    */   public int readUnsignedMedium()
/* 442:    */   {
/* 443:465 */     throw new IndexOutOfBoundsException();
/* 444:    */   }
/* 445:    */   
/* 446:    */   public int readInt()
/* 447:    */   {
/* 448:470 */     throw new IndexOutOfBoundsException();
/* 449:    */   }
/* 450:    */   
/* 451:    */   public long readUnsignedInt()
/* 452:    */   {
/* 453:475 */     throw new IndexOutOfBoundsException();
/* 454:    */   }
/* 455:    */   
/* 456:    */   public long readLong()
/* 457:    */   {
/* 458:480 */     throw new IndexOutOfBoundsException();
/* 459:    */   }
/* 460:    */   
/* 461:    */   public char readChar()
/* 462:    */   {
/* 463:485 */     throw new IndexOutOfBoundsException();
/* 464:    */   }
/* 465:    */   
/* 466:    */   public float readFloat()
/* 467:    */   {
/* 468:490 */     throw new IndexOutOfBoundsException();
/* 469:    */   }
/* 470:    */   
/* 471:    */   public double readDouble()
/* 472:    */   {
/* 473:495 */     throw new IndexOutOfBoundsException();
/* 474:    */   }
/* 475:    */   
/* 476:    */   public ByteBuf readBytes(int length)
/* 477:    */   {
/* 478:500 */     return checkLength(length);
/* 479:    */   }
/* 480:    */   
/* 481:    */   public ByteBuf readSlice(int length)
/* 482:    */   {
/* 483:505 */     return checkLength(length);
/* 484:    */   }
/* 485:    */   
/* 486:    */   public ByteBuf readBytes(ByteBuf dst)
/* 487:    */   {
/* 488:510 */     return checkLength(dst.writableBytes());
/* 489:    */   }
/* 490:    */   
/* 491:    */   public ByteBuf readBytes(ByteBuf dst, int length)
/* 492:    */   {
/* 493:515 */     return checkLength(length);
/* 494:    */   }
/* 495:    */   
/* 496:    */   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/* 497:    */   {
/* 498:520 */     return checkLength(length);
/* 499:    */   }
/* 500:    */   
/* 501:    */   public ByteBuf readBytes(byte[] dst)
/* 502:    */   {
/* 503:525 */     return checkLength(dst.length);
/* 504:    */   }
/* 505:    */   
/* 506:    */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/* 507:    */   {
/* 508:530 */     return checkLength(length);
/* 509:    */   }
/* 510:    */   
/* 511:    */   public ByteBuf readBytes(ByteBuffer dst)
/* 512:    */   {
/* 513:535 */     return checkLength(dst.remaining());
/* 514:    */   }
/* 515:    */   
/* 516:    */   public ByteBuf readBytes(OutputStream out, int length)
/* 517:    */   {
/* 518:540 */     return checkLength(length);
/* 519:    */   }
/* 520:    */   
/* 521:    */   public int readBytes(GatheringByteChannel out, int length)
/* 522:    */   {
/* 523:545 */     checkLength(length);
/* 524:546 */     return 0;
/* 525:    */   }
/* 526:    */   
/* 527:    */   public ByteBuf skipBytes(int length)
/* 528:    */   {
/* 529:551 */     return checkLength(length);
/* 530:    */   }
/* 531:    */   
/* 532:    */   public ByteBuf writeBoolean(boolean value)
/* 533:    */   {
/* 534:556 */     throw new IndexOutOfBoundsException();
/* 535:    */   }
/* 536:    */   
/* 537:    */   public ByteBuf writeByte(int value)
/* 538:    */   {
/* 539:561 */     throw new IndexOutOfBoundsException();
/* 540:    */   }
/* 541:    */   
/* 542:    */   public ByteBuf writeShort(int value)
/* 543:    */   {
/* 544:566 */     throw new IndexOutOfBoundsException();
/* 545:    */   }
/* 546:    */   
/* 547:    */   public ByteBuf writeMedium(int value)
/* 548:    */   {
/* 549:571 */     throw new IndexOutOfBoundsException();
/* 550:    */   }
/* 551:    */   
/* 552:    */   public ByteBuf writeInt(int value)
/* 553:    */   {
/* 554:576 */     throw new IndexOutOfBoundsException();
/* 555:    */   }
/* 556:    */   
/* 557:    */   public ByteBuf writeLong(long value)
/* 558:    */   {
/* 559:581 */     throw new IndexOutOfBoundsException();
/* 560:    */   }
/* 561:    */   
/* 562:    */   public ByteBuf writeChar(int value)
/* 563:    */   {
/* 564:586 */     throw new IndexOutOfBoundsException();
/* 565:    */   }
/* 566:    */   
/* 567:    */   public ByteBuf writeFloat(float value)
/* 568:    */   {
/* 569:591 */     throw new IndexOutOfBoundsException();
/* 570:    */   }
/* 571:    */   
/* 572:    */   public ByteBuf writeDouble(double value)
/* 573:    */   {
/* 574:596 */     throw new IndexOutOfBoundsException();
/* 575:    */   }
/* 576:    */   
/* 577:    */   public ByteBuf writeBytes(ByteBuf src)
/* 578:    */   {
/* 579:601 */     throw new IndexOutOfBoundsException();
/* 580:    */   }
/* 581:    */   
/* 582:    */   public ByteBuf writeBytes(ByteBuf src, int length)
/* 583:    */   {
/* 584:606 */     return checkLength(length);
/* 585:    */   }
/* 586:    */   
/* 587:    */   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/* 588:    */   {
/* 589:611 */     return checkLength(length);
/* 590:    */   }
/* 591:    */   
/* 592:    */   public ByteBuf writeBytes(byte[] src)
/* 593:    */   {
/* 594:616 */     return checkLength(src.length);
/* 595:    */   }
/* 596:    */   
/* 597:    */   public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
/* 598:    */   {
/* 599:621 */     return checkLength(length);
/* 600:    */   }
/* 601:    */   
/* 602:    */   public ByteBuf writeBytes(ByteBuffer src)
/* 603:    */   {
/* 604:626 */     return checkLength(src.remaining());
/* 605:    */   }
/* 606:    */   
/* 607:    */   public int writeBytes(InputStream in, int length)
/* 608:    */   {
/* 609:631 */     checkLength(length);
/* 610:632 */     return 0;
/* 611:    */   }
/* 612:    */   
/* 613:    */   public int writeBytes(ScatteringByteChannel in, int length)
/* 614:    */   {
/* 615:637 */     checkLength(length);
/* 616:638 */     return 0;
/* 617:    */   }
/* 618:    */   
/* 619:    */   public ByteBuf writeZero(int length)
/* 620:    */   {
/* 621:643 */     return checkLength(length);
/* 622:    */   }
/* 623:    */   
/* 624:    */   public int indexOf(int fromIndex, int toIndex, byte value)
/* 625:    */   {
/* 626:648 */     checkIndex(fromIndex);
/* 627:649 */     checkIndex(toIndex);
/* 628:650 */     return -1;
/* 629:    */   }
/* 630:    */   
/* 631:    */   public int bytesBefore(byte value)
/* 632:    */   {
/* 633:655 */     return -1;
/* 634:    */   }
/* 635:    */   
/* 636:    */   public int bytesBefore(int length, byte value)
/* 637:    */   {
/* 638:660 */     checkLength(length);
/* 639:661 */     return -1;
/* 640:    */   }
/* 641:    */   
/* 642:    */   public int bytesBefore(int index, int length, byte value)
/* 643:    */   {
/* 644:666 */     checkIndex(index, length);
/* 645:667 */     return -1;
/* 646:    */   }
/* 647:    */   
/* 648:    */   public int forEachByte(ByteBufProcessor processor)
/* 649:    */   {
/* 650:672 */     return -1;
/* 651:    */   }
/* 652:    */   
/* 653:    */   public int forEachByte(int index, int length, ByteBufProcessor processor)
/* 654:    */   {
/* 655:677 */     checkIndex(index, length);
/* 656:678 */     return -1;
/* 657:    */   }
/* 658:    */   
/* 659:    */   public int forEachByteDesc(ByteBufProcessor processor)
/* 660:    */   {
/* 661:683 */     return -1;
/* 662:    */   }
/* 663:    */   
/* 664:    */   public int forEachByteDesc(int index, int length, ByteBufProcessor processor)
/* 665:    */   {
/* 666:688 */     checkIndex(index, length);
/* 667:689 */     return -1;
/* 668:    */   }
/* 669:    */   
/* 670:    */   public ByteBuf copy()
/* 671:    */   {
/* 672:694 */     return this;
/* 673:    */   }
/* 674:    */   
/* 675:    */   public ByteBuf copy(int index, int length)
/* 676:    */   {
/* 677:699 */     return checkIndex(index, length);
/* 678:    */   }
/* 679:    */   
/* 680:    */   public ByteBuf slice()
/* 681:    */   {
/* 682:704 */     return this;
/* 683:    */   }
/* 684:    */   
/* 685:    */   public ByteBuf slice(int index, int length)
/* 686:    */   {
/* 687:709 */     return checkIndex(index, length);
/* 688:    */   }
/* 689:    */   
/* 690:    */   public ByteBuf duplicate()
/* 691:    */   {
/* 692:714 */     return this;
/* 693:    */   }
/* 694:    */   
/* 695:    */   public int nioBufferCount()
/* 696:    */   {
/* 697:719 */     return 1;
/* 698:    */   }
/* 699:    */   
/* 700:    */   public ByteBuffer nioBuffer()
/* 701:    */   {
/* 702:724 */     return EMPTY_BYTE_BUFFER;
/* 703:    */   }
/* 704:    */   
/* 705:    */   public ByteBuffer nioBuffer(int index, int length)
/* 706:    */   {
/* 707:729 */     checkIndex(index, length);
/* 708:730 */     return nioBuffer();
/* 709:    */   }
/* 710:    */   
/* 711:    */   public ByteBuffer[] nioBuffers()
/* 712:    */   {
/* 713:735 */     return new ByteBuffer[] { EMPTY_BYTE_BUFFER };
/* 714:    */   }
/* 715:    */   
/* 716:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 717:    */   {
/* 718:740 */     checkIndex(index, length);
/* 719:741 */     return nioBuffers();
/* 720:    */   }
/* 721:    */   
/* 722:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 723:    */   {
/* 724:746 */     return EMPTY_BYTE_BUFFER;
/* 725:    */   }
/* 726:    */   
/* 727:    */   public boolean hasArray()
/* 728:    */   {
/* 729:751 */     return true;
/* 730:    */   }
/* 731:    */   
/* 732:    */   public byte[] array()
/* 733:    */   {
/* 734:756 */     return EmptyArrays.EMPTY_BYTES;
/* 735:    */   }
/* 736:    */   
/* 737:    */   public int arrayOffset()
/* 738:    */   {
/* 739:761 */     return 0;
/* 740:    */   }
/* 741:    */   
/* 742:    */   public boolean hasMemoryAddress()
/* 743:    */   {
/* 744:766 */     return EMPTY_BYTE_BUFFER_ADDRESS != 0L;
/* 745:    */   }
/* 746:    */   
/* 747:    */   public long memoryAddress()
/* 748:    */   {
/* 749:771 */     if (hasMemoryAddress()) {
/* 750:772 */       return EMPTY_BYTE_BUFFER_ADDRESS;
/* 751:    */     }
/* 752:774 */     throw new UnsupportedOperationException();
/* 753:    */   }
/* 754:    */   
/* 755:    */   public String toString(Charset charset)
/* 756:    */   {
/* 757:780 */     return "";
/* 758:    */   }
/* 759:    */   
/* 760:    */   public String toString(int index, int length, Charset charset)
/* 761:    */   {
/* 762:785 */     checkIndex(index, length);
/* 763:786 */     return toString(charset);
/* 764:    */   }
/* 765:    */   
/* 766:    */   public int hashCode()
/* 767:    */   {
/* 768:791 */     return 0;
/* 769:    */   }
/* 770:    */   
/* 771:    */   public boolean equals(Object obj)
/* 772:    */   {
/* 773:796 */     return ((obj instanceof ByteBuf)) && (!((ByteBuf)obj).isReadable());
/* 774:    */   }
/* 775:    */   
/* 776:    */   public int compareTo(ByteBuf buffer)
/* 777:    */   {
/* 778:801 */     return buffer.isReadable() ? -1 : 0;
/* 779:    */   }
/* 780:    */   
/* 781:    */   public String toString()
/* 782:    */   {
/* 783:806 */     return this.str;
/* 784:    */   }
/* 785:    */   
/* 786:    */   public boolean isReadable(int size)
/* 787:    */   {
/* 788:811 */     return false;
/* 789:    */   }
/* 790:    */   
/* 791:    */   public boolean isWritable(int size)
/* 792:    */   {
/* 793:816 */     return false;
/* 794:    */   }
/* 795:    */   
/* 796:    */   public int refCnt()
/* 797:    */   {
/* 798:821 */     return 1;
/* 799:    */   }
/* 800:    */   
/* 801:    */   public ByteBuf retain()
/* 802:    */   {
/* 803:826 */     return this;
/* 804:    */   }
/* 805:    */   
/* 806:    */   public ByteBuf retain(int increment)
/* 807:    */   {
/* 808:831 */     return this;
/* 809:    */   }
/* 810:    */   
/* 811:    */   public boolean release()
/* 812:    */   {
/* 813:836 */     return false;
/* 814:    */   }
/* 815:    */   
/* 816:    */   public boolean release(int decrement)
/* 817:    */   {
/* 818:841 */     return false;
/* 819:    */   }
/* 820:    */   
/* 821:    */   private ByteBuf checkIndex(int index)
/* 822:    */   {
/* 823:845 */     if (index != 0) {
/* 824:846 */       throw new IndexOutOfBoundsException();
/* 825:    */     }
/* 826:848 */     return this;
/* 827:    */   }
/* 828:    */   
/* 829:    */   private ByteBuf checkIndex(int index, int length)
/* 830:    */   {
/* 831:852 */     if (length < 0) {
/* 832:853 */       throw new IllegalArgumentException("length: " + length);
/* 833:    */     }
/* 834:855 */     if ((index != 0) || (length != 0)) {
/* 835:856 */       throw new IndexOutOfBoundsException();
/* 836:    */     }
/* 837:858 */     return this;
/* 838:    */   }
/* 839:    */   
/* 840:    */   private ByteBuf checkLength(int length)
/* 841:    */   {
/* 842:862 */     if (length < 0) {
/* 843:863 */       throw new IllegalArgumentException("length: " + length + " (expected: >= 0)");
/* 844:    */     }
/* 845:865 */     if (length != 0) {
/* 846:866 */       throw new IndexOutOfBoundsException();
/* 847:    */     }
/* 848:868 */     return this;
/* 849:    */   }
/* 850:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.EmptyByteBuf
 * JD-Core Version:    0.7.0.1
 */