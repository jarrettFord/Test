/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.ResourceLeak;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.InputStream;
/*   6:    */ import java.io.OutputStream;
/*   7:    */ import java.nio.ByteBuffer;
/*   8:    */ import java.nio.ByteOrder;
/*   9:    */ import java.nio.channels.GatheringByteChannel;
/*  10:    */ import java.nio.channels.ScatteringByteChannel;
/*  11:    */ import java.nio.charset.Charset;
/*  12:    */ 
/*  13:    */ final class AdvancedLeakAwareByteBuf
/*  14:    */   extends WrappedByteBuf
/*  15:    */ {
/*  16:    */   private final ResourceLeak leak;
/*  17:    */   
/*  18:    */   AdvancedLeakAwareByteBuf(ByteBuf buf, ResourceLeak leak)
/*  19:    */   {
/*  20: 35 */     super(buf);
/*  21: 36 */     this.leak = leak;
/*  22:    */   }
/*  23:    */   
/*  24:    */   public boolean release()
/*  25:    */   {
/*  26: 41 */     boolean deallocated = super.release();
/*  27: 42 */     if (deallocated) {
/*  28: 43 */       this.leak.close();
/*  29:    */     } else {
/*  30: 45 */       this.leak.record();
/*  31:    */     }
/*  32: 47 */     return deallocated;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public boolean release(int decrement)
/*  36:    */   {
/*  37: 52 */     boolean deallocated = super.release(decrement);
/*  38: 53 */     if (deallocated) {
/*  39: 54 */       this.leak.close();
/*  40:    */     } else {
/*  41: 56 */       this.leak.record();
/*  42:    */     }
/*  43: 58 */     return deallocated;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public ByteBuf order(ByteOrder endianness)
/*  47:    */   {
/*  48: 63 */     this.leak.record();
/*  49: 64 */     if (order() == endianness) {
/*  50: 65 */       return this;
/*  51:    */     }
/*  52: 67 */     return new AdvancedLeakAwareByteBuf(super.order(endianness), this.leak);
/*  53:    */   }
/*  54:    */   
/*  55:    */   public ByteBuf slice()
/*  56:    */   {
/*  57: 73 */     this.leak.record();
/*  58: 74 */     return new AdvancedLeakAwareByteBuf(super.slice(), this.leak);
/*  59:    */   }
/*  60:    */   
/*  61:    */   public ByteBuf slice(int index, int length)
/*  62:    */   {
/*  63: 79 */     this.leak.record();
/*  64: 80 */     return new AdvancedLeakAwareByteBuf(super.slice(index, length), this.leak);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public ByteBuf duplicate()
/*  68:    */   {
/*  69: 85 */     this.leak.record();
/*  70: 86 */     return new AdvancedLeakAwareByteBuf(super.duplicate(), this.leak);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public ByteBuf readSlice(int length)
/*  74:    */   {
/*  75: 91 */     this.leak.record();
/*  76: 92 */     return new AdvancedLeakAwareByteBuf(super.readSlice(length), this.leak);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public ByteBuf discardReadBytes()
/*  80:    */   {
/*  81: 97 */     this.leak.record();
/*  82: 98 */     return super.discardReadBytes();
/*  83:    */   }
/*  84:    */   
/*  85:    */   public ByteBuf discardSomeReadBytes()
/*  86:    */   {
/*  87:103 */     this.leak.record();
/*  88:104 */     return super.discardSomeReadBytes();
/*  89:    */   }
/*  90:    */   
/*  91:    */   public ByteBuf ensureWritable(int minWritableBytes)
/*  92:    */   {
/*  93:109 */     this.leak.record();
/*  94:110 */     return super.ensureWritable(minWritableBytes);
/*  95:    */   }
/*  96:    */   
/*  97:    */   public int ensureWritable(int minWritableBytes, boolean force)
/*  98:    */   {
/*  99:115 */     this.leak.record();
/* 100:116 */     return super.ensureWritable(minWritableBytes, force);
/* 101:    */   }
/* 102:    */   
/* 103:    */   public boolean getBoolean(int index)
/* 104:    */   {
/* 105:121 */     this.leak.record();
/* 106:122 */     return super.getBoolean(index);
/* 107:    */   }
/* 108:    */   
/* 109:    */   public byte getByte(int index)
/* 110:    */   {
/* 111:127 */     this.leak.record();
/* 112:128 */     return super.getByte(index);
/* 113:    */   }
/* 114:    */   
/* 115:    */   public short getUnsignedByte(int index)
/* 116:    */   {
/* 117:133 */     this.leak.record();
/* 118:134 */     return super.getUnsignedByte(index);
/* 119:    */   }
/* 120:    */   
/* 121:    */   public short getShort(int index)
/* 122:    */   {
/* 123:139 */     this.leak.record();
/* 124:140 */     return super.getShort(index);
/* 125:    */   }
/* 126:    */   
/* 127:    */   public int getUnsignedShort(int index)
/* 128:    */   {
/* 129:145 */     this.leak.record();
/* 130:146 */     return super.getUnsignedShort(index);
/* 131:    */   }
/* 132:    */   
/* 133:    */   public int getMedium(int index)
/* 134:    */   {
/* 135:151 */     this.leak.record();
/* 136:152 */     return super.getMedium(index);
/* 137:    */   }
/* 138:    */   
/* 139:    */   public int getUnsignedMedium(int index)
/* 140:    */   {
/* 141:157 */     this.leak.record();
/* 142:158 */     return super.getUnsignedMedium(index);
/* 143:    */   }
/* 144:    */   
/* 145:    */   public int getInt(int index)
/* 146:    */   {
/* 147:163 */     this.leak.record();
/* 148:164 */     return super.getInt(index);
/* 149:    */   }
/* 150:    */   
/* 151:    */   public long getUnsignedInt(int index)
/* 152:    */   {
/* 153:169 */     this.leak.record();
/* 154:170 */     return super.getUnsignedInt(index);
/* 155:    */   }
/* 156:    */   
/* 157:    */   public long getLong(int index)
/* 158:    */   {
/* 159:175 */     this.leak.record();
/* 160:176 */     return super.getLong(index);
/* 161:    */   }
/* 162:    */   
/* 163:    */   public char getChar(int index)
/* 164:    */   {
/* 165:181 */     this.leak.record();
/* 166:182 */     return super.getChar(index);
/* 167:    */   }
/* 168:    */   
/* 169:    */   public float getFloat(int index)
/* 170:    */   {
/* 171:187 */     this.leak.record();
/* 172:188 */     return super.getFloat(index);
/* 173:    */   }
/* 174:    */   
/* 175:    */   public double getDouble(int index)
/* 176:    */   {
/* 177:193 */     this.leak.record();
/* 178:194 */     return super.getDouble(index);
/* 179:    */   }
/* 180:    */   
/* 181:    */   public ByteBuf getBytes(int index, ByteBuf dst)
/* 182:    */   {
/* 183:199 */     this.leak.record();
/* 184:200 */     return super.getBytes(index, dst);
/* 185:    */   }
/* 186:    */   
/* 187:    */   public ByteBuf getBytes(int index, ByteBuf dst, int length)
/* 188:    */   {
/* 189:205 */     this.leak.record();
/* 190:206 */     return super.getBytes(index, dst, length);
/* 191:    */   }
/* 192:    */   
/* 193:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 194:    */   {
/* 195:211 */     this.leak.record();
/* 196:212 */     return super.getBytes(index, dst, dstIndex, length);
/* 197:    */   }
/* 198:    */   
/* 199:    */   public ByteBuf getBytes(int index, byte[] dst)
/* 200:    */   {
/* 201:217 */     this.leak.record();
/* 202:218 */     return super.getBytes(index, dst);
/* 203:    */   }
/* 204:    */   
/* 205:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 206:    */   {
/* 207:223 */     this.leak.record();
/* 208:224 */     return super.getBytes(index, dst, dstIndex, length);
/* 209:    */   }
/* 210:    */   
/* 211:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 212:    */   {
/* 213:229 */     this.leak.record();
/* 214:230 */     return super.getBytes(index, dst);
/* 215:    */   }
/* 216:    */   
/* 217:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 218:    */     throws IOException
/* 219:    */   {
/* 220:235 */     this.leak.record();
/* 221:236 */     return super.getBytes(index, out, length);
/* 222:    */   }
/* 223:    */   
/* 224:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 225:    */     throws IOException
/* 226:    */   {
/* 227:241 */     this.leak.record();
/* 228:242 */     return super.getBytes(index, out, length);
/* 229:    */   }
/* 230:    */   
/* 231:    */   public ByteBuf setBoolean(int index, boolean value)
/* 232:    */   {
/* 233:247 */     this.leak.record();
/* 234:248 */     return super.setBoolean(index, value);
/* 235:    */   }
/* 236:    */   
/* 237:    */   public ByteBuf setByte(int index, int value)
/* 238:    */   {
/* 239:253 */     this.leak.record();
/* 240:254 */     return super.setByte(index, value);
/* 241:    */   }
/* 242:    */   
/* 243:    */   public ByteBuf setShort(int index, int value)
/* 244:    */   {
/* 245:259 */     this.leak.record();
/* 246:260 */     return super.setShort(index, value);
/* 247:    */   }
/* 248:    */   
/* 249:    */   public ByteBuf setMedium(int index, int value)
/* 250:    */   {
/* 251:265 */     this.leak.record();
/* 252:266 */     return super.setMedium(index, value);
/* 253:    */   }
/* 254:    */   
/* 255:    */   public ByteBuf setInt(int index, int value)
/* 256:    */   {
/* 257:271 */     this.leak.record();
/* 258:272 */     return super.setInt(index, value);
/* 259:    */   }
/* 260:    */   
/* 261:    */   public ByteBuf setLong(int index, long value)
/* 262:    */   {
/* 263:277 */     this.leak.record();
/* 264:278 */     return super.setLong(index, value);
/* 265:    */   }
/* 266:    */   
/* 267:    */   public ByteBuf setChar(int index, int value)
/* 268:    */   {
/* 269:283 */     this.leak.record();
/* 270:284 */     return super.setChar(index, value);
/* 271:    */   }
/* 272:    */   
/* 273:    */   public ByteBuf setFloat(int index, float value)
/* 274:    */   {
/* 275:289 */     this.leak.record();
/* 276:290 */     return super.setFloat(index, value);
/* 277:    */   }
/* 278:    */   
/* 279:    */   public ByteBuf setDouble(int index, double value)
/* 280:    */   {
/* 281:295 */     this.leak.record();
/* 282:296 */     return super.setDouble(index, value);
/* 283:    */   }
/* 284:    */   
/* 285:    */   public ByteBuf setBytes(int index, ByteBuf src)
/* 286:    */   {
/* 287:301 */     this.leak.record();
/* 288:302 */     return super.setBytes(index, src);
/* 289:    */   }
/* 290:    */   
/* 291:    */   public ByteBuf setBytes(int index, ByteBuf src, int length)
/* 292:    */   {
/* 293:307 */     this.leak.record();
/* 294:308 */     return super.setBytes(index, src, length);
/* 295:    */   }
/* 296:    */   
/* 297:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 298:    */   {
/* 299:313 */     this.leak.record();
/* 300:314 */     return super.setBytes(index, src, srcIndex, length);
/* 301:    */   }
/* 302:    */   
/* 303:    */   public ByteBuf setBytes(int index, byte[] src)
/* 304:    */   {
/* 305:319 */     this.leak.record();
/* 306:320 */     return super.setBytes(index, src);
/* 307:    */   }
/* 308:    */   
/* 309:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 310:    */   {
/* 311:325 */     this.leak.record();
/* 312:326 */     return super.setBytes(index, src, srcIndex, length);
/* 313:    */   }
/* 314:    */   
/* 315:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 316:    */   {
/* 317:331 */     this.leak.record();
/* 318:332 */     return super.setBytes(index, src);
/* 319:    */   }
/* 320:    */   
/* 321:    */   public int setBytes(int index, InputStream in, int length)
/* 322:    */     throws IOException
/* 323:    */   {
/* 324:337 */     this.leak.record();
/* 325:338 */     return super.setBytes(index, in, length);
/* 326:    */   }
/* 327:    */   
/* 328:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 329:    */     throws IOException
/* 330:    */   {
/* 331:343 */     this.leak.record();
/* 332:344 */     return super.setBytes(index, in, length);
/* 333:    */   }
/* 334:    */   
/* 335:    */   public ByteBuf setZero(int index, int length)
/* 336:    */   {
/* 337:349 */     this.leak.record();
/* 338:350 */     return super.setZero(index, length);
/* 339:    */   }
/* 340:    */   
/* 341:    */   public boolean readBoolean()
/* 342:    */   {
/* 343:355 */     this.leak.record();
/* 344:356 */     return super.readBoolean();
/* 345:    */   }
/* 346:    */   
/* 347:    */   public byte readByte()
/* 348:    */   {
/* 349:361 */     this.leak.record();
/* 350:362 */     return super.readByte();
/* 351:    */   }
/* 352:    */   
/* 353:    */   public short readUnsignedByte()
/* 354:    */   {
/* 355:367 */     this.leak.record();
/* 356:368 */     return super.readUnsignedByte();
/* 357:    */   }
/* 358:    */   
/* 359:    */   public short readShort()
/* 360:    */   {
/* 361:373 */     this.leak.record();
/* 362:374 */     return super.readShort();
/* 363:    */   }
/* 364:    */   
/* 365:    */   public int readUnsignedShort()
/* 366:    */   {
/* 367:379 */     this.leak.record();
/* 368:380 */     return super.readUnsignedShort();
/* 369:    */   }
/* 370:    */   
/* 371:    */   public int readMedium()
/* 372:    */   {
/* 373:385 */     this.leak.record();
/* 374:386 */     return super.readMedium();
/* 375:    */   }
/* 376:    */   
/* 377:    */   public int readUnsignedMedium()
/* 378:    */   {
/* 379:391 */     this.leak.record();
/* 380:392 */     return super.readUnsignedMedium();
/* 381:    */   }
/* 382:    */   
/* 383:    */   public int readInt()
/* 384:    */   {
/* 385:397 */     this.leak.record();
/* 386:398 */     return super.readInt();
/* 387:    */   }
/* 388:    */   
/* 389:    */   public long readUnsignedInt()
/* 390:    */   {
/* 391:403 */     this.leak.record();
/* 392:404 */     return super.readUnsignedInt();
/* 393:    */   }
/* 394:    */   
/* 395:    */   public long readLong()
/* 396:    */   {
/* 397:409 */     this.leak.record();
/* 398:410 */     return super.readLong();
/* 399:    */   }
/* 400:    */   
/* 401:    */   public char readChar()
/* 402:    */   {
/* 403:415 */     this.leak.record();
/* 404:416 */     return super.readChar();
/* 405:    */   }
/* 406:    */   
/* 407:    */   public float readFloat()
/* 408:    */   {
/* 409:421 */     this.leak.record();
/* 410:422 */     return super.readFloat();
/* 411:    */   }
/* 412:    */   
/* 413:    */   public double readDouble()
/* 414:    */   {
/* 415:427 */     this.leak.record();
/* 416:428 */     return super.readDouble();
/* 417:    */   }
/* 418:    */   
/* 419:    */   public ByteBuf readBytes(int length)
/* 420:    */   {
/* 421:433 */     this.leak.record();
/* 422:434 */     return super.readBytes(length);
/* 423:    */   }
/* 424:    */   
/* 425:    */   public ByteBuf readBytes(ByteBuf dst)
/* 426:    */   {
/* 427:439 */     this.leak.record();
/* 428:440 */     return super.readBytes(dst);
/* 429:    */   }
/* 430:    */   
/* 431:    */   public ByteBuf readBytes(ByteBuf dst, int length)
/* 432:    */   {
/* 433:445 */     this.leak.record();
/* 434:446 */     return super.readBytes(dst, length);
/* 435:    */   }
/* 436:    */   
/* 437:    */   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/* 438:    */   {
/* 439:451 */     this.leak.record();
/* 440:452 */     return super.readBytes(dst, dstIndex, length);
/* 441:    */   }
/* 442:    */   
/* 443:    */   public ByteBuf readBytes(byte[] dst)
/* 444:    */   {
/* 445:457 */     this.leak.record();
/* 446:458 */     return super.readBytes(dst);
/* 447:    */   }
/* 448:    */   
/* 449:    */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/* 450:    */   {
/* 451:463 */     this.leak.record();
/* 452:464 */     return super.readBytes(dst, dstIndex, length);
/* 453:    */   }
/* 454:    */   
/* 455:    */   public ByteBuf readBytes(ByteBuffer dst)
/* 456:    */   {
/* 457:469 */     this.leak.record();
/* 458:470 */     return super.readBytes(dst);
/* 459:    */   }
/* 460:    */   
/* 461:    */   public ByteBuf readBytes(OutputStream out, int length)
/* 462:    */     throws IOException
/* 463:    */   {
/* 464:475 */     this.leak.record();
/* 465:476 */     return super.readBytes(out, length);
/* 466:    */   }
/* 467:    */   
/* 468:    */   public int readBytes(GatheringByteChannel out, int length)
/* 469:    */     throws IOException
/* 470:    */   {
/* 471:481 */     this.leak.record();
/* 472:482 */     return super.readBytes(out, length);
/* 473:    */   }
/* 474:    */   
/* 475:    */   public ByteBuf skipBytes(int length)
/* 476:    */   {
/* 477:487 */     this.leak.record();
/* 478:488 */     return super.skipBytes(length);
/* 479:    */   }
/* 480:    */   
/* 481:    */   public ByteBuf writeBoolean(boolean value)
/* 482:    */   {
/* 483:493 */     this.leak.record();
/* 484:494 */     return super.writeBoolean(value);
/* 485:    */   }
/* 486:    */   
/* 487:    */   public ByteBuf writeByte(int value)
/* 488:    */   {
/* 489:499 */     this.leak.record();
/* 490:500 */     return super.writeByte(value);
/* 491:    */   }
/* 492:    */   
/* 493:    */   public ByteBuf writeShort(int value)
/* 494:    */   {
/* 495:505 */     this.leak.record();
/* 496:506 */     return super.writeShort(value);
/* 497:    */   }
/* 498:    */   
/* 499:    */   public ByteBuf writeMedium(int value)
/* 500:    */   {
/* 501:511 */     this.leak.record();
/* 502:512 */     return super.writeMedium(value);
/* 503:    */   }
/* 504:    */   
/* 505:    */   public ByteBuf writeInt(int value)
/* 506:    */   {
/* 507:517 */     this.leak.record();
/* 508:518 */     return super.writeInt(value);
/* 509:    */   }
/* 510:    */   
/* 511:    */   public ByteBuf writeLong(long value)
/* 512:    */   {
/* 513:523 */     this.leak.record();
/* 514:524 */     return super.writeLong(value);
/* 515:    */   }
/* 516:    */   
/* 517:    */   public ByteBuf writeChar(int value)
/* 518:    */   {
/* 519:529 */     this.leak.record();
/* 520:530 */     return super.writeChar(value);
/* 521:    */   }
/* 522:    */   
/* 523:    */   public ByteBuf writeFloat(float value)
/* 524:    */   {
/* 525:535 */     this.leak.record();
/* 526:536 */     return super.writeFloat(value);
/* 527:    */   }
/* 528:    */   
/* 529:    */   public ByteBuf writeDouble(double value)
/* 530:    */   {
/* 531:541 */     this.leak.record();
/* 532:542 */     return super.writeDouble(value);
/* 533:    */   }
/* 534:    */   
/* 535:    */   public ByteBuf writeBytes(ByteBuf src)
/* 536:    */   {
/* 537:547 */     this.leak.record();
/* 538:548 */     return super.writeBytes(src);
/* 539:    */   }
/* 540:    */   
/* 541:    */   public ByteBuf writeBytes(ByteBuf src, int length)
/* 542:    */   {
/* 543:553 */     this.leak.record();
/* 544:554 */     return super.writeBytes(src, length);
/* 545:    */   }
/* 546:    */   
/* 547:    */   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/* 548:    */   {
/* 549:559 */     this.leak.record();
/* 550:560 */     return super.writeBytes(src, srcIndex, length);
/* 551:    */   }
/* 552:    */   
/* 553:    */   public ByteBuf writeBytes(byte[] src)
/* 554:    */   {
/* 555:565 */     this.leak.record();
/* 556:566 */     return super.writeBytes(src);
/* 557:    */   }
/* 558:    */   
/* 559:    */   public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
/* 560:    */   {
/* 561:571 */     this.leak.record();
/* 562:572 */     return super.writeBytes(src, srcIndex, length);
/* 563:    */   }
/* 564:    */   
/* 565:    */   public ByteBuf writeBytes(ByteBuffer src)
/* 566:    */   {
/* 567:577 */     this.leak.record();
/* 568:578 */     return super.writeBytes(src);
/* 569:    */   }
/* 570:    */   
/* 571:    */   public int writeBytes(InputStream in, int length)
/* 572:    */     throws IOException
/* 573:    */   {
/* 574:583 */     this.leak.record();
/* 575:584 */     return super.writeBytes(in, length);
/* 576:    */   }
/* 577:    */   
/* 578:    */   public int writeBytes(ScatteringByteChannel in, int length)
/* 579:    */     throws IOException
/* 580:    */   {
/* 581:589 */     this.leak.record();
/* 582:590 */     return super.writeBytes(in, length);
/* 583:    */   }
/* 584:    */   
/* 585:    */   public ByteBuf writeZero(int length)
/* 586:    */   {
/* 587:595 */     this.leak.record();
/* 588:596 */     return super.writeZero(length);
/* 589:    */   }
/* 590:    */   
/* 591:    */   public int indexOf(int fromIndex, int toIndex, byte value)
/* 592:    */   {
/* 593:601 */     this.leak.record();
/* 594:602 */     return super.indexOf(fromIndex, toIndex, value);
/* 595:    */   }
/* 596:    */   
/* 597:    */   public int bytesBefore(byte value)
/* 598:    */   {
/* 599:607 */     this.leak.record();
/* 600:608 */     return super.bytesBefore(value);
/* 601:    */   }
/* 602:    */   
/* 603:    */   public int bytesBefore(int length, byte value)
/* 604:    */   {
/* 605:613 */     this.leak.record();
/* 606:614 */     return super.bytesBefore(length, value);
/* 607:    */   }
/* 608:    */   
/* 609:    */   public int bytesBefore(int index, int length, byte value)
/* 610:    */   {
/* 611:619 */     this.leak.record();
/* 612:620 */     return super.bytesBefore(index, length, value);
/* 613:    */   }
/* 614:    */   
/* 615:    */   public int forEachByte(ByteBufProcessor processor)
/* 616:    */   {
/* 617:625 */     this.leak.record();
/* 618:626 */     return super.forEachByte(processor);
/* 619:    */   }
/* 620:    */   
/* 621:    */   public int forEachByte(int index, int length, ByteBufProcessor processor)
/* 622:    */   {
/* 623:631 */     this.leak.record();
/* 624:632 */     return super.forEachByte(index, length, processor);
/* 625:    */   }
/* 626:    */   
/* 627:    */   public int forEachByteDesc(ByteBufProcessor processor)
/* 628:    */   {
/* 629:637 */     this.leak.record();
/* 630:638 */     return super.forEachByteDesc(processor);
/* 631:    */   }
/* 632:    */   
/* 633:    */   public int forEachByteDesc(int index, int length, ByteBufProcessor processor)
/* 634:    */   {
/* 635:643 */     this.leak.record();
/* 636:644 */     return super.forEachByteDesc(index, length, processor);
/* 637:    */   }
/* 638:    */   
/* 639:    */   public ByteBuf copy()
/* 640:    */   {
/* 641:649 */     this.leak.record();
/* 642:650 */     return super.copy();
/* 643:    */   }
/* 644:    */   
/* 645:    */   public ByteBuf copy(int index, int length)
/* 646:    */   {
/* 647:655 */     this.leak.record();
/* 648:656 */     return super.copy(index, length);
/* 649:    */   }
/* 650:    */   
/* 651:    */   public int nioBufferCount()
/* 652:    */   {
/* 653:661 */     this.leak.record();
/* 654:662 */     return super.nioBufferCount();
/* 655:    */   }
/* 656:    */   
/* 657:    */   public ByteBuffer nioBuffer()
/* 658:    */   {
/* 659:667 */     this.leak.record();
/* 660:668 */     return super.nioBuffer();
/* 661:    */   }
/* 662:    */   
/* 663:    */   public ByteBuffer nioBuffer(int index, int length)
/* 664:    */   {
/* 665:673 */     this.leak.record();
/* 666:674 */     return super.nioBuffer(index, length);
/* 667:    */   }
/* 668:    */   
/* 669:    */   public ByteBuffer[] nioBuffers()
/* 670:    */   {
/* 671:679 */     this.leak.record();
/* 672:680 */     return super.nioBuffers();
/* 673:    */   }
/* 674:    */   
/* 675:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 676:    */   {
/* 677:685 */     this.leak.record();
/* 678:686 */     return super.nioBuffers(index, length);
/* 679:    */   }
/* 680:    */   
/* 681:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 682:    */   {
/* 683:691 */     this.leak.record();
/* 684:692 */     return super.internalNioBuffer(index, length);
/* 685:    */   }
/* 686:    */   
/* 687:    */   public String toString(Charset charset)
/* 688:    */   {
/* 689:697 */     this.leak.record();
/* 690:698 */     return super.toString(charset);
/* 691:    */   }
/* 692:    */   
/* 693:    */   public String toString(int index, int length, Charset charset)
/* 694:    */   {
/* 695:703 */     this.leak.record();
/* 696:704 */     return super.toString(index, length, charset);
/* 697:    */   }
/* 698:    */   
/* 699:    */   public ByteBuf retain()
/* 700:    */   {
/* 701:709 */     this.leak.record();
/* 702:710 */     return super.retain();
/* 703:    */   }
/* 704:    */   
/* 705:    */   public ByteBuf retain(int increment)
/* 706:    */   {
/* 707:715 */     this.leak.record();
/* 708:716 */     return super.retain(increment);
/* 709:    */   }
/* 710:    */   
/* 711:    */   public ByteBuf capacity(int newCapacity)
/* 712:    */   {
/* 713:721 */     this.leak.record();
/* 714:722 */     return super.capacity(newCapacity);
/* 715:    */   }
/* 716:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.AdvancedLeakAwareByteBuf
 * JD-Core Version:    0.7.0.1
 */