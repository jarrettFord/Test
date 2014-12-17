/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.InputStream;
/*   6:    */ import java.io.OutputStream;
/*   7:    */ import java.nio.ByteBuffer;
/*   8:    */ import java.nio.ByteOrder;
/*   9:    */ import java.nio.channels.GatheringByteChannel;
/*  10:    */ import java.nio.channels.ScatteringByteChannel;
/*  11:    */ import java.nio.charset.Charset;
/*  12:    */ 
/*  13:    */ class WrappedByteBuf
/*  14:    */   extends ByteBuf
/*  15:    */ {
/*  16:    */   protected final ByteBuf buf;
/*  17:    */   
/*  18:    */   protected WrappedByteBuf(ByteBuf buf)
/*  19:    */   {
/*  20: 35 */     if (buf == null) {
/*  21: 36 */       throw new NullPointerException("buf");
/*  22:    */     }
/*  23: 38 */     this.buf = buf;
/*  24:    */   }
/*  25:    */   
/*  26:    */   public boolean hasMemoryAddress()
/*  27:    */   {
/*  28: 43 */     return this.buf.hasMemoryAddress();
/*  29:    */   }
/*  30:    */   
/*  31:    */   public long memoryAddress()
/*  32:    */   {
/*  33: 48 */     return this.buf.memoryAddress();
/*  34:    */   }
/*  35:    */   
/*  36:    */   public int capacity()
/*  37:    */   {
/*  38: 53 */     return this.buf.capacity();
/*  39:    */   }
/*  40:    */   
/*  41:    */   public ByteBuf capacity(int newCapacity)
/*  42:    */   {
/*  43: 58 */     this.buf.capacity(newCapacity);
/*  44: 59 */     return this;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public int maxCapacity()
/*  48:    */   {
/*  49: 64 */     return this.buf.maxCapacity();
/*  50:    */   }
/*  51:    */   
/*  52:    */   public ByteBufAllocator alloc()
/*  53:    */   {
/*  54: 69 */     return this.buf.alloc();
/*  55:    */   }
/*  56:    */   
/*  57:    */   public ByteOrder order()
/*  58:    */   {
/*  59: 74 */     return this.buf.order();
/*  60:    */   }
/*  61:    */   
/*  62:    */   public ByteBuf order(ByteOrder endianness)
/*  63:    */   {
/*  64: 79 */     return this.buf.order(endianness);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public ByteBuf unwrap()
/*  68:    */   {
/*  69: 84 */     return this.buf;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public boolean isDirect()
/*  73:    */   {
/*  74: 89 */     return this.buf.isDirect();
/*  75:    */   }
/*  76:    */   
/*  77:    */   public int readerIndex()
/*  78:    */   {
/*  79: 94 */     return this.buf.readerIndex();
/*  80:    */   }
/*  81:    */   
/*  82:    */   public ByteBuf readerIndex(int readerIndex)
/*  83:    */   {
/*  84: 99 */     this.buf.readerIndex(readerIndex);
/*  85:100 */     return this;
/*  86:    */   }
/*  87:    */   
/*  88:    */   public int writerIndex()
/*  89:    */   {
/*  90:105 */     return this.buf.writerIndex();
/*  91:    */   }
/*  92:    */   
/*  93:    */   public ByteBuf writerIndex(int writerIndex)
/*  94:    */   {
/*  95:110 */     this.buf.writerIndex(writerIndex);
/*  96:111 */     return this;
/*  97:    */   }
/*  98:    */   
/*  99:    */   public ByteBuf setIndex(int readerIndex, int writerIndex)
/* 100:    */   {
/* 101:116 */     this.buf.setIndex(readerIndex, writerIndex);
/* 102:117 */     return this;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public int readableBytes()
/* 106:    */   {
/* 107:122 */     return this.buf.readableBytes();
/* 108:    */   }
/* 109:    */   
/* 110:    */   public int writableBytes()
/* 111:    */   {
/* 112:127 */     return this.buf.writableBytes();
/* 113:    */   }
/* 114:    */   
/* 115:    */   public int maxWritableBytes()
/* 116:    */   {
/* 117:132 */     return this.buf.maxWritableBytes();
/* 118:    */   }
/* 119:    */   
/* 120:    */   public boolean isReadable()
/* 121:    */   {
/* 122:137 */     return this.buf.isReadable();
/* 123:    */   }
/* 124:    */   
/* 125:    */   public boolean isWritable()
/* 126:    */   {
/* 127:142 */     return this.buf.isWritable();
/* 128:    */   }
/* 129:    */   
/* 130:    */   public ByteBuf clear()
/* 131:    */   {
/* 132:147 */     this.buf.clear();
/* 133:148 */     return this;
/* 134:    */   }
/* 135:    */   
/* 136:    */   public ByteBuf markReaderIndex()
/* 137:    */   {
/* 138:153 */     this.buf.markReaderIndex();
/* 139:154 */     return this;
/* 140:    */   }
/* 141:    */   
/* 142:    */   public ByteBuf resetReaderIndex()
/* 143:    */   {
/* 144:159 */     this.buf.resetReaderIndex();
/* 145:160 */     return this;
/* 146:    */   }
/* 147:    */   
/* 148:    */   public ByteBuf markWriterIndex()
/* 149:    */   {
/* 150:165 */     this.buf.markWriterIndex();
/* 151:166 */     return this;
/* 152:    */   }
/* 153:    */   
/* 154:    */   public ByteBuf resetWriterIndex()
/* 155:    */   {
/* 156:171 */     this.buf.resetWriterIndex();
/* 157:172 */     return this;
/* 158:    */   }
/* 159:    */   
/* 160:    */   public ByteBuf discardReadBytes()
/* 161:    */   {
/* 162:177 */     this.buf.discardReadBytes();
/* 163:178 */     return this;
/* 164:    */   }
/* 165:    */   
/* 166:    */   public ByteBuf discardSomeReadBytes()
/* 167:    */   {
/* 168:183 */     this.buf.discardSomeReadBytes();
/* 169:184 */     return this;
/* 170:    */   }
/* 171:    */   
/* 172:    */   public ByteBuf ensureWritable(int minWritableBytes)
/* 173:    */   {
/* 174:189 */     this.buf.ensureWritable(minWritableBytes);
/* 175:190 */     return this;
/* 176:    */   }
/* 177:    */   
/* 178:    */   public int ensureWritable(int minWritableBytes, boolean force)
/* 179:    */   {
/* 180:195 */     return this.buf.ensureWritable(minWritableBytes, force);
/* 181:    */   }
/* 182:    */   
/* 183:    */   public boolean getBoolean(int index)
/* 184:    */   {
/* 185:200 */     return this.buf.getBoolean(index);
/* 186:    */   }
/* 187:    */   
/* 188:    */   public byte getByte(int index)
/* 189:    */   {
/* 190:205 */     return this.buf.getByte(index);
/* 191:    */   }
/* 192:    */   
/* 193:    */   public short getUnsignedByte(int index)
/* 194:    */   {
/* 195:210 */     return this.buf.getUnsignedByte(index);
/* 196:    */   }
/* 197:    */   
/* 198:    */   public short getShort(int index)
/* 199:    */   {
/* 200:215 */     return this.buf.getShort(index);
/* 201:    */   }
/* 202:    */   
/* 203:    */   public int getUnsignedShort(int index)
/* 204:    */   {
/* 205:220 */     return this.buf.getUnsignedShort(index);
/* 206:    */   }
/* 207:    */   
/* 208:    */   public int getMedium(int index)
/* 209:    */   {
/* 210:225 */     return this.buf.getMedium(index);
/* 211:    */   }
/* 212:    */   
/* 213:    */   public int getUnsignedMedium(int index)
/* 214:    */   {
/* 215:230 */     return this.buf.getUnsignedMedium(index);
/* 216:    */   }
/* 217:    */   
/* 218:    */   public int getInt(int index)
/* 219:    */   {
/* 220:235 */     return this.buf.getInt(index);
/* 221:    */   }
/* 222:    */   
/* 223:    */   public long getUnsignedInt(int index)
/* 224:    */   {
/* 225:240 */     return this.buf.getUnsignedInt(index);
/* 226:    */   }
/* 227:    */   
/* 228:    */   public long getLong(int index)
/* 229:    */   {
/* 230:245 */     return this.buf.getLong(index);
/* 231:    */   }
/* 232:    */   
/* 233:    */   public char getChar(int index)
/* 234:    */   {
/* 235:250 */     return this.buf.getChar(index);
/* 236:    */   }
/* 237:    */   
/* 238:    */   public float getFloat(int index)
/* 239:    */   {
/* 240:255 */     return this.buf.getFloat(index);
/* 241:    */   }
/* 242:    */   
/* 243:    */   public double getDouble(int index)
/* 244:    */   {
/* 245:260 */     return this.buf.getDouble(index);
/* 246:    */   }
/* 247:    */   
/* 248:    */   public ByteBuf getBytes(int index, ByteBuf dst)
/* 249:    */   {
/* 250:265 */     this.buf.getBytes(index, dst);
/* 251:266 */     return this;
/* 252:    */   }
/* 253:    */   
/* 254:    */   public ByteBuf getBytes(int index, ByteBuf dst, int length)
/* 255:    */   {
/* 256:271 */     this.buf.getBytes(index, dst, length);
/* 257:272 */     return this;
/* 258:    */   }
/* 259:    */   
/* 260:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 261:    */   {
/* 262:277 */     this.buf.getBytes(index, dst, dstIndex, length);
/* 263:278 */     return this;
/* 264:    */   }
/* 265:    */   
/* 266:    */   public ByteBuf getBytes(int index, byte[] dst)
/* 267:    */   {
/* 268:283 */     this.buf.getBytes(index, dst);
/* 269:284 */     return this;
/* 270:    */   }
/* 271:    */   
/* 272:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 273:    */   {
/* 274:289 */     this.buf.getBytes(index, dst, dstIndex, length);
/* 275:290 */     return this;
/* 276:    */   }
/* 277:    */   
/* 278:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 279:    */   {
/* 280:295 */     this.buf.getBytes(index, dst);
/* 281:296 */     return this;
/* 282:    */   }
/* 283:    */   
/* 284:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 285:    */     throws IOException
/* 286:    */   {
/* 287:301 */     this.buf.getBytes(index, out, length);
/* 288:302 */     return this;
/* 289:    */   }
/* 290:    */   
/* 291:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 292:    */     throws IOException
/* 293:    */   {
/* 294:307 */     return this.buf.getBytes(index, out, length);
/* 295:    */   }
/* 296:    */   
/* 297:    */   public ByteBuf setBoolean(int index, boolean value)
/* 298:    */   {
/* 299:312 */     this.buf.setBoolean(index, value);
/* 300:313 */     return this;
/* 301:    */   }
/* 302:    */   
/* 303:    */   public ByteBuf setByte(int index, int value)
/* 304:    */   {
/* 305:318 */     this.buf.setByte(index, value);
/* 306:319 */     return this;
/* 307:    */   }
/* 308:    */   
/* 309:    */   public ByteBuf setShort(int index, int value)
/* 310:    */   {
/* 311:324 */     this.buf.setShort(index, value);
/* 312:325 */     return this;
/* 313:    */   }
/* 314:    */   
/* 315:    */   public ByteBuf setMedium(int index, int value)
/* 316:    */   {
/* 317:330 */     this.buf.setMedium(index, value);
/* 318:331 */     return this;
/* 319:    */   }
/* 320:    */   
/* 321:    */   public ByteBuf setInt(int index, int value)
/* 322:    */   {
/* 323:336 */     this.buf.setInt(index, value);
/* 324:337 */     return this;
/* 325:    */   }
/* 326:    */   
/* 327:    */   public ByteBuf setLong(int index, long value)
/* 328:    */   {
/* 329:342 */     this.buf.setLong(index, value);
/* 330:343 */     return this;
/* 331:    */   }
/* 332:    */   
/* 333:    */   public ByteBuf setChar(int index, int value)
/* 334:    */   {
/* 335:348 */     this.buf.setChar(index, value);
/* 336:349 */     return this;
/* 337:    */   }
/* 338:    */   
/* 339:    */   public ByteBuf setFloat(int index, float value)
/* 340:    */   {
/* 341:354 */     this.buf.setFloat(index, value);
/* 342:355 */     return this;
/* 343:    */   }
/* 344:    */   
/* 345:    */   public ByteBuf setDouble(int index, double value)
/* 346:    */   {
/* 347:360 */     this.buf.setDouble(index, value);
/* 348:361 */     return this;
/* 349:    */   }
/* 350:    */   
/* 351:    */   public ByteBuf setBytes(int index, ByteBuf src)
/* 352:    */   {
/* 353:366 */     this.buf.setBytes(index, src);
/* 354:367 */     return this;
/* 355:    */   }
/* 356:    */   
/* 357:    */   public ByteBuf setBytes(int index, ByteBuf src, int length)
/* 358:    */   {
/* 359:372 */     this.buf.setBytes(index, src, length);
/* 360:373 */     return this;
/* 361:    */   }
/* 362:    */   
/* 363:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 364:    */   {
/* 365:378 */     this.buf.setBytes(index, src, srcIndex, length);
/* 366:379 */     return this;
/* 367:    */   }
/* 368:    */   
/* 369:    */   public ByteBuf setBytes(int index, byte[] src)
/* 370:    */   {
/* 371:384 */     this.buf.setBytes(index, src);
/* 372:385 */     return this;
/* 373:    */   }
/* 374:    */   
/* 375:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 376:    */   {
/* 377:390 */     this.buf.setBytes(index, src, srcIndex, length);
/* 378:391 */     return this;
/* 379:    */   }
/* 380:    */   
/* 381:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 382:    */   {
/* 383:396 */     this.buf.setBytes(index, src);
/* 384:397 */     return this;
/* 385:    */   }
/* 386:    */   
/* 387:    */   public int setBytes(int index, InputStream in, int length)
/* 388:    */     throws IOException
/* 389:    */   {
/* 390:402 */     return this.buf.setBytes(index, in, length);
/* 391:    */   }
/* 392:    */   
/* 393:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 394:    */     throws IOException
/* 395:    */   {
/* 396:407 */     return this.buf.setBytes(index, in, length);
/* 397:    */   }
/* 398:    */   
/* 399:    */   public ByteBuf setZero(int index, int length)
/* 400:    */   {
/* 401:412 */     this.buf.setZero(index, length);
/* 402:413 */     return this;
/* 403:    */   }
/* 404:    */   
/* 405:    */   public boolean readBoolean()
/* 406:    */   {
/* 407:418 */     return this.buf.readBoolean();
/* 408:    */   }
/* 409:    */   
/* 410:    */   public byte readByte()
/* 411:    */   {
/* 412:423 */     return this.buf.readByte();
/* 413:    */   }
/* 414:    */   
/* 415:    */   public short readUnsignedByte()
/* 416:    */   {
/* 417:428 */     return this.buf.readUnsignedByte();
/* 418:    */   }
/* 419:    */   
/* 420:    */   public short readShort()
/* 421:    */   {
/* 422:433 */     return this.buf.readShort();
/* 423:    */   }
/* 424:    */   
/* 425:    */   public int readUnsignedShort()
/* 426:    */   {
/* 427:438 */     return this.buf.readUnsignedShort();
/* 428:    */   }
/* 429:    */   
/* 430:    */   public int readMedium()
/* 431:    */   {
/* 432:443 */     return this.buf.readMedium();
/* 433:    */   }
/* 434:    */   
/* 435:    */   public int readUnsignedMedium()
/* 436:    */   {
/* 437:448 */     return this.buf.readUnsignedMedium();
/* 438:    */   }
/* 439:    */   
/* 440:    */   public int readInt()
/* 441:    */   {
/* 442:453 */     return this.buf.readInt();
/* 443:    */   }
/* 444:    */   
/* 445:    */   public long readUnsignedInt()
/* 446:    */   {
/* 447:458 */     return this.buf.readUnsignedInt();
/* 448:    */   }
/* 449:    */   
/* 450:    */   public long readLong()
/* 451:    */   {
/* 452:463 */     return this.buf.readLong();
/* 453:    */   }
/* 454:    */   
/* 455:    */   public char readChar()
/* 456:    */   {
/* 457:468 */     return this.buf.readChar();
/* 458:    */   }
/* 459:    */   
/* 460:    */   public float readFloat()
/* 461:    */   {
/* 462:473 */     return this.buf.readFloat();
/* 463:    */   }
/* 464:    */   
/* 465:    */   public double readDouble()
/* 466:    */   {
/* 467:478 */     return this.buf.readDouble();
/* 468:    */   }
/* 469:    */   
/* 470:    */   public ByteBuf readBytes(int length)
/* 471:    */   {
/* 472:483 */     return this.buf.readBytes(length);
/* 473:    */   }
/* 474:    */   
/* 475:    */   public ByteBuf readSlice(int length)
/* 476:    */   {
/* 477:488 */     return this.buf.readSlice(length);
/* 478:    */   }
/* 479:    */   
/* 480:    */   public ByteBuf readBytes(ByteBuf dst)
/* 481:    */   {
/* 482:493 */     this.buf.readBytes(dst);
/* 483:494 */     return this;
/* 484:    */   }
/* 485:    */   
/* 486:    */   public ByteBuf readBytes(ByteBuf dst, int length)
/* 487:    */   {
/* 488:499 */     this.buf.readBytes(dst, length);
/* 489:500 */     return this;
/* 490:    */   }
/* 491:    */   
/* 492:    */   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/* 493:    */   {
/* 494:505 */     this.buf.readBytes(dst, dstIndex, length);
/* 495:506 */     return this;
/* 496:    */   }
/* 497:    */   
/* 498:    */   public ByteBuf readBytes(byte[] dst)
/* 499:    */   {
/* 500:511 */     this.buf.readBytes(dst);
/* 501:512 */     return this;
/* 502:    */   }
/* 503:    */   
/* 504:    */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/* 505:    */   {
/* 506:517 */     this.buf.readBytes(dst, dstIndex, length);
/* 507:518 */     return this;
/* 508:    */   }
/* 509:    */   
/* 510:    */   public ByteBuf readBytes(ByteBuffer dst)
/* 511:    */   {
/* 512:523 */     this.buf.readBytes(dst);
/* 513:524 */     return this;
/* 514:    */   }
/* 515:    */   
/* 516:    */   public ByteBuf readBytes(OutputStream out, int length)
/* 517:    */     throws IOException
/* 518:    */   {
/* 519:529 */     this.buf.readBytes(out, length);
/* 520:530 */     return this;
/* 521:    */   }
/* 522:    */   
/* 523:    */   public int readBytes(GatheringByteChannel out, int length)
/* 524:    */     throws IOException
/* 525:    */   {
/* 526:535 */     return this.buf.readBytes(out, length);
/* 527:    */   }
/* 528:    */   
/* 529:    */   public ByteBuf skipBytes(int length)
/* 530:    */   {
/* 531:540 */     this.buf.skipBytes(length);
/* 532:541 */     return this;
/* 533:    */   }
/* 534:    */   
/* 535:    */   public ByteBuf writeBoolean(boolean value)
/* 536:    */   {
/* 537:546 */     this.buf.writeBoolean(value);
/* 538:547 */     return this;
/* 539:    */   }
/* 540:    */   
/* 541:    */   public ByteBuf writeByte(int value)
/* 542:    */   {
/* 543:552 */     this.buf.writeByte(value);
/* 544:553 */     return this;
/* 545:    */   }
/* 546:    */   
/* 547:    */   public ByteBuf writeShort(int value)
/* 548:    */   {
/* 549:558 */     this.buf.writeShort(value);
/* 550:559 */     return this;
/* 551:    */   }
/* 552:    */   
/* 553:    */   public ByteBuf writeMedium(int value)
/* 554:    */   {
/* 555:564 */     this.buf.writeMedium(value);
/* 556:565 */     return this;
/* 557:    */   }
/* 558:    */   
/* 559:    */   public ByteBuf writeInt(int value)
/* 560:    */   {
/* 561:570 */     this.buf.writeInt(value);
/* 562:571 */     return this;
/* 563:    */   }
/* 564:    */   
/* 565:    */   public ByteBuf writeLong(long value)
/* 566:    */   {
/* 567:576 */     this.buf.writeLong(value);
/* 568:577 */     return this;
/* 569:    */   }
/* 570:    */   
/* 571:    */   public ByteBuf writeChar(int value)
/* 572:    */   {
/* 573:582 */     this.buf.writeChar(value);
/* 574:583 */     return this;
/* 575:    */   }
/* 576:    */   
/* 577:    */   public ByteBuf writeFloat(float value)
/* 578:    */   {
/* 579:588 */     this.buf.writeFloat(value);
/* 580:589 */     return this;
/* 581:    */   }
/* 582:    */   
/* 583:    */   public ByteBuf writeDouble(double value)
/* 584:    */   {
/* 585:594 */     this.buf.writeDouble(value);
/* 586:595 */     return this;
/* 587:    */   }
/* 588:    */   
/* 589:    */   public ByteBuf writeBytes(ByteBuf src)
/* 590:    */   {
/* 591:600 */     this.buf.writeBytes(src);
/* 592:601 */     return this;
/* 593:    */   }
/* 594:    */   
/* 595:    */   public ByteBuf writeBytes(ByteBuf src, int length)
/* 596:    */   {
/* 597:606 */     this.buf.writeBytes(src, length);
/* 598:607 */     return this;
/* 599:    */   }
/* 600:    */   
/* 601:    */   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/* 602:    */   {
/* 603:612 */     this.buf.writeBytes(src, srcIndex, length);
/* 604:613 */     return this;
/* 605:    */   }
/* 606:    */   
/* 607:    */   public ByteBuf writeBytes(byte[] src)
/* 608:    */   {
/* 609:618 */     this.buf.writeBytes(src);
/* 610:619 */     return this;
/* 611:    */   }
/* 612:    */   
/* 613:    */   public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
/* 614:    */   {
/* 615:624 */     this.buf.writeBytes(src, srcIndex, length);
/* 616:625 */     return this;
/* 617:    */   }
/* 618:    */   
/* 619:    */   public ByteBuf writeBytes(ByteBuffer src)
/* 620:    */   {
/* 621:630 */     this.buf.writeBytes(src);
/* 622:631 */     return this;
/* 623:    */   }
/* 624:    */   
/* 625:    */   public int writeBytes(InputStream in, int length)
/* 626:    */     throws IOException
/* 627:    */   {
/* 628:636 */     return this.buf.writeBytes(in, length);
/* 629:    */   }
/* 630:    */   
/* 631:    */   public int writeBytes(ScatteringByteChannel in, int length)
/* 632:    */     throws IOException
/* 633:    */   {
/* 634:641 */     return this.buf.writeBytes(in, length);
/* 635:    */   }
/* 636:    */   
/* 637:    */   public ByteBuf writeZero(int length)
/* 638:    */   {
/* 639:646 */     this.buf.writeZero(length);
/* 640:647 */     return this;
/* 641:    */   }
/* 642:    */   
/* 643:    */   public int indexOf(int fromIndex, int toIndex, byte value)
/* 644:    */   {
/* 645:652 */     return this.buf.indexOf(fromIndex, toIndex, value);
/* 646:    */   }
/* 647:    */   
/* 648:    */   public int bytesBefore(byte value)
/* 649:    */   {
/* 650:657 */     return this.buf.bytesBefore(value);
/* 651:    */   }
/* 652:    */   
/* 653:    */   public int bytesBefore(int length, byte value)
/* 654:    */   {
/* 655:662 */     return this.buf.bytesBefore(length, value);
/* 656:    */   }
/* 657:    */   
/* 658:    */   public int bytesBefore(int index, int length, byte value)
/* 659:    */   {
/* 660:667 */     return this.buf.bytesBefore(index, length, value);
/* 661:    */   }
/* 662:    */   
/* 663:    */   public int forEachByte(ByteBufProcessor processor)
/* 664:    */   {
/* 665:672 */     return this.buf.forEachByte(processor);
/* 666:    */   }
/* 667:    */   
/* 668:    */   public int forEachByte(int index, int length, ByteBufProcessor processor)
/* 669:    */   {
/* 670:677 */     return this.buf.forEachByte(index, length, processor);
/* 671:    */   }
/* 672:    */   
/* 673:    */   public int forEachByteDesc(ByteBufProcessor processor)
/* 674:    */   {
/* 675:682 */     return this.buf.forEachByteDesc(processor);
/* 676:    */   }
/* 677:    */   
/* 678:    */   public int forEachByteDesc(int index, int length, ByteBufProcessor processor)
/* 679:    */   {
/* 680:687 */     return this.buf.forEachByteDesc(index, length, processor);
/* 681:    */   }
/* 682:    */   
/* 683:    */   public ByteBuf copy()
/* 684:    */   {
/* 685:692 */     return this.buf.copy();
/* 686:    */   }
/* 687:    */   
/* 688:    */   public ByteBuf copy(int index, int length)
/* 689:    */   {
/* 690:697 */     return this.buf.copy(index, length);
/* 691:    */   }
/* 692:    */   
/* 693:    */   public ByteBuf slice()
/* 694:    */   {
/* 695:702 */     return this.buf.slice();
/* 696:    */   }
/* 697:    */   
/* 698:    */   public ByteBuf slice(int index, int length)
/* 699:    */   {
/* 700:707 */     return this.buf.slice(index, length);
/* 701:    */   }
/* 702:    */   
/* 703:    */   public ByteBuf duplicate()
/* 704:    */   {
/* 705:712 */     return this.buf.duplicate();
/* 706:    */   }
/* 707:    */   
/* 708:    */   public int nioBufferCount()
/* 709:    */   {
/* 710:717 */     return this.buf.nioBufferCount();
/* 711:    */   }
/* 712:    */   
/* 713:    */   public ByteBuffer nioBuffer()
/* 714:    */   {
/* 715:722 */     return this.buf.nioBuffer();
/* 716:    */   }
/* 717:    */   
/* 718:    */   public ByteBuffer nioBuffer(int index, int length)
/* 719:    */   {
/* 720:727 */     return this.buf.nioBuffer(index, length);
/* 721:    */   }
/* 722:    */   
/* 723:    */   public ByteBuffer[] nioBuffers()
/* 724:    */   {
/* 725:732 */     return this.buf.nioBuffers();
/* 726:    */   }
/* 727:    */   
/* 728:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 729:    */   {
/* 730:737 */     return this.buf.nioBuffers(index, length);
/* 731:    */   }
/* 732:    */   
/* 733:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 734:    */   {
/* 735:742 */     return this.buf.internalNioBuffer(index, length);
/* 736:    */   }
/* 737:    */   
/* 738:    */   public boolean hasArray()
/* 739:    */   {
/* 740:747 */     return this.buf.hasArray();
/* 741:    */   }
/* 742:    */   
/* 743:    */   public byte[] array()
/* 744:    */   {
/* 745:752 */     return this.buf.array();
/* 746:    */   }
/* 747:    */   
/* 748:    */   public int arrayOffset()
/* 749:    */   {
/* 750:757 */     return this.buf.arrayOffset();
/* 751:    */   }
/* 752:    */   
/* 753:    */   public String toString(Charset charset)
/* 754:    */   {
/* 755:762 */     return this.buf.toString(charset);
/* 756:    */   }
/* 757:    */   
/* 758:    */   public String toString(int index, int length, Charset charset)
/* 759:    */   {
/* 760:767 */     return this.buf.toString(index, length, charset);
/* 761:    */   }
/* 762:    */   
/* 763:    */   public int hashCode()
/* 764:    */   {
/* 765:772 */     return this.buf.hashCode();
/* 766:    */   }
/* 767:    */   
/* 768:    */   public boolean equals(Object obj)
/* 769:    */   {
/* 770:777 */     return this.buf.equals(obj);
/* 771:    */   }
/* 772:    */   
/* 773:    */   public int compareTo(ByteBuf buffer)
/* 774:    */   {
/* 775:782 */     return this.buf.compareTo(buffer);
/* 776:    */   }
/* 777:    */   
/* 778:    */   public String toString()
/* 779:    */   {
/* 780:787 */     return StringUtil.simpleClassName(this) + '(' + this.buf.toString() + ')';
/* 781:    */   }
/* 782:    */   
/* 783:    */   public ByteBuf retain(int increment)
/* 784:    */   {
/* 785:792 */     this.buf.retain(increment);
/* 786:793 */     return this;
/* 787:    */   }
/* 788:    */   
/* 789:    */   public ByteBuf retain()
/* 790:    */   {
/* 791:798 */     this.buf.retain();
/* 792:799 */     return this;
/* 793:    */   }
/* 794:    */   
/* 795:    */   public boolean isReadable(int size)
/* 796:    */   {
/* 797:804 */     return this.buf.isReadable(size);
/* 798:    */   }
/* 799:    */   
/* 800:    */   public boolean isWritable(int size)
/* 801:    */   {
/* 802:809 */     return this.buf.isWritable(size);
/* 803:    */   }
/* 804:    */   
/* 805:    */   public int refCnt()
/* 806:    */   {
/* 807:814 */     return this.buf.refCnt();
/* 808:    */   }
/* 809:    */   
/* 810:    */   public boolean release()
/* 811:    */   {
/* 812:819 */     return this.buf.release();
/* 813:    */   }
/* 814:    */   
/* 815:    */   public boolean release(int decrement)
/* 816:    */   {
/* 817:824 */     return this.buf.release(decrement);
/* 818:    */   }
/* 819:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.WrappedByteBuf
 * JD-Core Version:    0.7.0.1
 */