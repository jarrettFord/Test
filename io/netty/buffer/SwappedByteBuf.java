/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.io.InputStream;
/*   5:    */ import java.io.OutputStream;
/*   6:    */ import java.nio.ByteBuffer;
/*   7:    */ import java.nio.ByteOrder;
/*   8:    */ import java.nio.channels.GatheringByteChannel;
/*   9:    */ import java.nio.channels.ScatteringByteChannel;
/*  10:    */ import java.nio.charset.Charset;
/*  11:    */ 
/*  12:    */ public class SwappedByteBuf
/*  13:    */   extends ByteBuf
/*  14:    */ {
/*  15:    */   private final ByteBuf buf;
/*  16:    */   private final ByteOrder order;
/*  17:    */   
/*  18:    */   public SwappedByteBuf(ByteBuf buf)
/*  19:    */   {
/*  20: 36 */     if (buf == null) {
/*  21: 37 */       throw new NullPointerException("buf");
/*  22:    */     }
/*  23: 39 */     this.buf = buf;
/*  24: 40 */     if (buf.order() == ByteOrder.BIG_ENDIAN) {
/*  25: 41 */       this.order = ByteOrder.LITTLE_ENDIAN;
/*  26:    */     } else {
/*  27: 43 */       this.order = ByteOrder.BIG_ENDIAN;
/*  28:    */     }
/*  29:    */   }
/*  30:    */   
/*  31:    */   public ByteOrder order()
/*  32:    */   {
/*  33: 49 */     return this.order;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public ByteBuf order(ByteOrder endianness)
/*  37:    */   {
/*  38: 54 */     if (endianness == null) {
/*  39: 55 */       throw new NullPointerException("endianness");
/*  40:    */     }
/*  41: 57 */     if (endianness == this.order) {
/*  42: 58 */       return this;
/*  43:    */     }
/*  44: 60 */     return this.buf;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public ByteBuf unwrap()
/*  48:    */   {
/*  49: 65 */     return this.buf.unwrap();
/*  50:    */   }
/*  51:    */   
/*  52:    */   public ByteBufAllocator alloc()
/*  53:    */   {
/*  54: 70 */     return this.buf.alloc();
/*  55:    */   }
/*  56:    */   
/*  57:    */   public int capacity()
/*  58:    */   {
/*  59: 75 */     return this.buf.capacity();
/*  60:    */   }
/*  61:    */   
/*  62:    */   public ByteBuf capacity(int newCapacity)
/*  63:    */   {
/*  64: 80 */     this.buf.capacity(newCapacity);
/*  65: 81 */     return this;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public int maxCapacity()
/*  69:    */   {
/*  70: 86 */     return this.buf.maxCapacity();
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean isDirect()
/*  74:    */   {
/*  75: 91 */     return this.buf.isDirect();
/*  76:    */   }
/*  77:    */   
/*  78:    */   public int readerIndex()
/*  79:    */   {
/*  80: 96 */     return this.buf.readerIndex();
/*  81:    */   }
/*  82:    */   
/*  83:    */   public ByteBuf readerIndex(int readerIndex)
/*  84:    */   {
/*  85:101 */     this.buf.readerIndex(readerIndex);
/*  86:102 */     return this;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public int writerIndex()
/*  90:    */   {
/*  91:107 */     return this.buf.writerIndex();
/*  92:    */   }
/*  93:    */   
/*  94:    */   public ByteBuf writerIndex(int writerIndex)
/*  95:    */   {
/*  96:112 */     this.buf.writerIndex(writerIndex);
/*  97:113 */     return this;
/*  98:    */   }
/*  99:    */   
/* 100:    */   public ByteBuf setIndex(int readerIndex, int writerIndex)
/* 101:    */   {
/* 102:118 */     this.buf.setIndex(readerIndex, writerIndex);
/* 103:119 */     return this;
/* 104:    */   }
/* 105:    */   
/* 106:    */   public int readableBytes()
/* 107:    */   {
/* 108:124 */     return this.buf.readableBytes();
/* 109:    */   }
/* 110:    */   
/* 111:    */   public int writableBytes()
/* 112:    */   {
/* 113:129 */     return this.buf.writableBytes();
/* 114:    */   }
/* 115:    */   
/* 116:    */   public int maxWritableBytes()
/* 117:    */   {
/* 118:134 */     return this.buf.maxWritableBytes();
/* 119:    */   }
/* 120:    */   
/* 121:    */   public boolean isReadable()
/* 122:    */   {
/* 123:139 */     return this.buf.isReadable();
/* 124:    */   }
/* 125:    */   
/* 126:    */   public boolean isReadable(int size)
/* 127:    */   {
/* 128:144 */     return this.buf.isReadable(size);
/* 129:    */   }
/* 130:    */   
/* 131:    */   public boolean isWritable()
/* 132:    */   {
/* 133:149 */     return this.buf.isWritable();
/* 134:    */   }
/* 135:    */   
/* 136:    */   public boolean isWritable(int size)
/* 137:    */   {
/* 138:154 */     return this.buf.isWritable(size);
/* 139:    */   }
/* 140:    */   
/* 141:    */   public ByteBuf clear()
/* 142:    */   {
/* 143:159 */     this.buf.clear();
/* 144:160 */     return this;
/* 145:    */   }
/* 146:    */   
/* 147:    */   public ByteBuf markReaderIndex()
/* 148:    */   {
/* 149:165 */     this.buf.markReaderIndex();
/* 150:166 */     return this;
/* 151:    */   }
/* 152:    */   
/* 153:    */   public ByteBuf resetReaderIndex()
/* 154:    */   {
/* 155:171 */     this.buf.resetReaderIndex();
/* 156:172 */     return this;
/* 157:    */   }
/* 158:    */   
/* 159:    */   public ByteBuf markWriterIndex()
/* 160:    */   {
/* 161:177 */     this.buf.markWriterIndex();
/* 162:178 */     return this;
/* 163:    */   }
/* 164:    */   
/* 165:    */   public ByteBuf resetWriterIndex()
/* 166:    */   {
/* 167:183 */     this.buf.resetWriterIndex();
/* 168:184 */     return this;
/* 169:    */   }
/* 170:    */   
/* 171:    */   public ByteBuf discardReadBytes()
/* 172:    */   {
/* 173:189 */     this.buf.discardReadBytes();
/* 174:190 */     return this;
/* 175:    */   }
/* 176:    */   
/* 177:    */   public ByteBuf discardSomeReadBytes()
/* 178:    */   {
/* 179:195 */     this.buf.discardSomeReadBytes();
/* 180:196 */     return this;
/* 181:    */   }
/* 182:    */   
/* 183:    */   public ByteBuf ensureWritable(int writableBytes)
/* 184:    */   {
/* 185:201 */     this.buf.ensureWritable(writableBytes);
/* 186:202 */     return this;
/* 187:    */   }
/* 188:    */   
/* 189:    */   public int ensureWritable(int minWritableBytes, boolean force)
/* 190:    */   {
/* 191:207 */     return this.buf.ensureWritable(minWritableBytes, force);
/* 192:    */   }
/* 193:    */   
/* 194:    */   public boolean getBoolean(int index)
/* 195:    */   {
/* 196:212 */     return this.buf.getBoolean(index);
/* 197:    */   }
/* 198:    */   
/* 199:    */   public byte getByte(int index)
/* 200:    */   {
/* 201:217 */     return this.buf.getByte(index);
/* 202:    */   }
/* 203:    */   
/* 204:    */   public short getUnsignedByte(int index)
/* 205:    */   {
/* 206:222 */     return this.buf.getUnsignedByte(index);
/* 207:    */   }
/* 208:    */   
/* 209:    */   public short getShort(int index)
/* 210:    */   {
/* 211:227 */     return ByteBufUtil.swapShort(this.buf.getShort(index));
/* 212:    */   }
/* 213:    */   
/* 214:    */   public int getUnsignedShort(int index)
/* 215:    */   {
/* 216:232 */     return getShort(index) & 0xFFFF;
/* 217:    */   }
/* 218:    */   
/* 219:    */   public int getMedium(int index)
/* 220:    */   {
/* 221:237 */     return ByteBufUtil.swapMedium(this.buf.getMedium(index));
/* 222:    */   }
/* 223:    */   
/* 224:    */   public int getUnsignedMedium(int index)
/* 225:    */   {
/* 226:242 */     return getMedium(index) & 0xFFFFFF;
/* 227:    */   }
/* 228:    */   
/* 229:    */   public int getInt(int index)
/* 230:    */   {
/* 231:247 */     return ByteBufUtil.swapInt(this.buf.getInt(index));
/* 232:    */   }
/* 233:    */   
/* 234:    */   public long getUnsignedInt(int index)
/* 235:    */   {
/* 236:252 */     return getInt(index) & 0xFFFFFFFF;
/* 237:    */   }
/* 238:    */   
/* 239:    */   public long getLong(int index)
/* 240:    */   {
/* 241:257 */     return ByteBufUtil.swapLong(this.buf.getLong(index));
/* 242:    */   }
/* 243:    */   
/* 244:    */   public char getChar(int index)
/* 245:    */   {
/* 246:262 */     return (char)getShort(index);
/* 247:    */   }
/* 248:    */   
/* 249:    */   public float getFloat(int index)
/* 250:    */   {
/* 251:267 */     return Float.intBitsToFloat(getInt(index));
/* 252:    */   }
/* 253:    */   
/* 254:    */   public double getDouble(int index)
/* 255:    */   {
/* 256:272 */     return Double.longBitsToDouble(getLong(index));
/* 257:    */   }
/* 258:    */   
/* 259:    */   public ByteBuf getBytes(int index, ByteBuf dst)
/* 260:    */   {
/* 261:277 */     this.buf.getBytes(index, dst);
/* 262:278 */     return this;
/* 263:    */   }
/* 264:    */   
/* 265:    */   public ByteBuf getBytes(int index, ByteBuf dst, int length)
/* 266:    */   {
/* 267:283 */     this.buf.getBytes(index, dst, length);
/* 268:284 */     return this;
/* 269:    */   }
/* 270:    */   
/* 271:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 272:    */   {
/* 273:289 */     this.buf.getBytes(index, dst, dstIndex, length);
/* 274:290 */     return this;
/* 275:    */   }
/* 276:    */   
/* 277:    */   public ByteBuf getBytes(int index, byte[] dst)
/* 278:    */   {
/* 279:295 */     this.buf.getBytes(index, dst);
/* 280:296 */     return this;
/* 281:    */   }
/* 282:    */   
/* 283:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 284:    */   {
/* 285:301 */     this.buf.getBytes(index, dst, dstIndex, length);
/* 286:302 */     return this;
/* 287:    */   }
/* 288:    */   
/* 289:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 290:    */   {
/* 291:307 */     this.buf.getBytes(index, dst);
/* 292:308 */     return this;
/* 293:    */   }
/* 294:    */   
/* 295:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 296:    */     throws IOException
/* 297:    */   {
/* 298:313 */     this.buf.getBytes(index, out, length);
/* 299:314 */     return this;
/* 300:    */   }
/* 301:    */   
/* 302:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 303:    */     throws IOException
/* 304:    */   {
/* 305:319 */     return this.buf.getBytes(index, out, length);
/* 306:    */   }
/* 307:    */   
/* 308:    */   public ByteBuf setBoolean(int index, boolean value)
/* 309:    */   {
/* 310:324 */     this.buf.setBoolean(index, value);
/* 311:325 */     return this;
/* 312:    */   }
/* 313:    */   
/* 314:    */   public ByteBuf setByte(int index, int value)
/* 315:    */   {
/* 316:330 */     this.buf.setByte(index, value);
/* 317:331 */     return this;
/* 318:    */   }
/* 319:    */   
/* 320:    */   public ByteBuf setShort(int index, int value)
/* 321:    */   {
/* 322:336 */     this.buf.setShort(index, ByteBufUtil.swapShort((short)value));
/* 323:337 */     return this;
/* 324:    */   }
/* 325:    */   
/* 326:    */   public ByteBuf setMedium(int index, int value)
/* 327:    */   {
/* 328:342 */     this.buf.setMedium(index, ByteBufUtil.swapMedium(value));
/* 329:343 */     return this;
/* 330:    */   }
/* 331:    */   
/* 332:    */   public ByteBuf setInt(int index, int value)
/* 333:    */   {
/* 334:348 */     this.buf.setInt(index, ByteBufUtil.swapInt(value));
/* 335:349 */     return this;
/* 336:    */   }
/* 337:    */   
/* 338:    */   public ByteBuf setLong(int index, long value)
/* 339:    */   {
/* 340:354 */     this.buf.setLong(index, ByteBufUtil.swapLong(value));
/* 341:355 */     return this;
/* 342:    */   }
/* 343:    */   
/* 344:    */   public ByteBuf setChar(int index, int value)
/* 345:    */   {
/* 346:360 */     setShort(index, value);
/* 347:361 */     return this;
/* 348:    */   }
/* 349:    */   
/* 350:    */   public ByteBuf setFloat(int index, float value)
/* 351:    */   {
/* 352:366 */     setInt(index, Float.floatToRawIntBits(value));
/* 353:367 */     return this;
/* 354:    */   }
/* 355:    */   
/* 356:    */   public ByteBuf setDouble(int index, double value)
/* 357:    */   {
/* 358:372 */     setLong(index, Double.doubleToRawLongBits(value));
/* 359:373 */     return this;
/* 360:    */   }
/* 361:    */   
/* 362:    */   public ByteBuf setBytes(int index, ByteBuf src)
/* 363:    */   {
/* 364:378 */     this.buf.setBytes(index, src);
/* 365:379 */     return this;
/* 366:    */   }
/* 367:    */   
/* 368:    */   public ByteBuf setBytes(int index, ByteBuf src, int length)
/* 369:    */   {
/* 370:384 */     this.buf.setBytes(index, src, length);
/* 371:385 */     return this;
/* 372:    */   }
/* 373:    */   
/* 374:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 375:    */   {
/* 376:390 */     this.buf.setBytes(index, src, srcIndex, length);
/* 377:391 */     return this;
/* 378:    */   }
/* 379:    */   
/* 380:    */   public ByteBuf setBytes(int index, byte[] src)
/* 381:    */   {
/* 382:396 */     this.buf.setBytes(index, src);
/* 383:397 */     return this;
/* 384:    */   }
/* 385:    */   
/* 386:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 387:    */   {
/* 388:402 */     this.buf.setBytes(index, src, srcIndex, length);
/* 389:403 */     return this;
/* 390:    */   }
/* 391:    */   
/* 392:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 393:    */   {
/* 394:408 */     this.buf.setBytes(index, src);
/* 395:409 */     return this;
/* 396:    */   }
/* 397:    */   
/* 398:    */   public int setBytes(int index, InputStream in, int length)
/* 399:    */     throws IOException
/* 400:    */   {
/* 401:414 */     return this.buf.setBytes(index, in, length);
/* 402:    */   }
/* 403:    */   
/* 404:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 405:    */     throws IOException
/* 406:    */   {
/* 407:419 */     return this.buf.setBytes(index, in, length);
/* 408:    */   }
/* 409:    */   
/* 410:    */   public ByteBuf setZero(int index, int length)
/* 411:    */   {
/* 412:424 */     this.buf.setZero(index, length);
/* 413:425 */     return this;
/* 414:    */   }
/* 415:    */   
/* 416:    */   public boolean readBoolean()
/* 417:    */   {
/* 418:430 */     return this.buf.readBoolean();
/* 419:    */   }
/* 420:    */   
/* 421:    */   public byte readByte()
/* 422:    */   {
/* 423:435 */     return this.buf.readByte();
/* 424:    */   }
/* 425:    */   
/* 426:    */   public short readUnsignedByte()
/* 427:    */   {
/* 428:440 */     return this.buf.readUnsignedByte();
/* 429:    */   }
/* 430:    */   
/* 431:    */   public short readShort()
/* 432:    */   {
/* 433:445 */     return ByteBufUtil.swapShort(this.buf.readShort());
/* 434:    */   }
/* 435:    */   
/* 436:    */   public int readUnsignedShort()
/* 437:    */   {
/* 438:450 */     return readShort() & 0xFFFF;
/* 439:    */   }
/* 440:    */   
/* 441:    */   public int readMedium()
/* 442:    */   {
/* 443:455 */     return ByteBufUtil.swapMedium(this.buf.readMedium());
/* 444:    */   }
/* 445:    */   
/* 446:    */   public int readUnsignedMedium()
/* 447:    */   {
/* 448:460 */     return readMedium() & 0xFFFFFF;
/* 449:    */   }
/* 450:    */   
/* 451:    */   public int readInt()
/* 452:    */   {
/* 453:465 */     return ByteBufUtil.swapInt(this.buf.readInt());
/* 454:    */   }
/* 455:    */   
/* 456:    */   public long readUnsignedInt()
/* 457:    */   {
/* 458:470 */     return readInt() & 0xFFFFFFFF;
/* 459:    */   }
/* 460:    */   
/* 461:    */   public long readLong()
/* 462:    */   {
/* 463:475 */     return ByteBufUtil.swapLong(this.buf.readLong());
/* 464:    */   }
/* 465:    */   
/* 466:    */   public char readChar()
/* 467:    */   {
/* 468:480 */     return (char)readShort();
/* 469:    */   }
/* 470:    */   
/* 471:    */   public float readFloat()
/* 472:    */   {
/* 473:485 */     return Float.intBitsToFloat(readInt());
/* 474:    */   }
/* 475:    */   
/* 476:    */   public double readDouble()
/* 477:    */   {
/* 478:490 */     return Double.longBitsToDouble(readLong());
/* 479:    */   }
/* 480:    */   
/* 481:    */   public ByteBuf readBytes(int length)
/* 482:    */   {
/* 483:495 */     return this.buf.readBytes(length).order(order());
/* 484:    */   }
/* 485:    */   
/* 486:    */   public ByteBuf readSlice(int length)
/* 487:    */   {
/* 488:500 */     return this.buf.readSlice(length).order(this.order);
/* 489:    */   }
/* 490:    */   
/* 491:    */   public ByteBuf readBytes(ByteBuf dst)
/* 492:    */   {
/* 493:505 */     this.buf.readBytes(dst);
/* 494:506 */     return this;
/* 495:    */   }
/* 496:    */   
/* 497:    */   public ByteBuf readBytes(ByteBuf dst, int length)
/* 498:    */   {
/* 499:511 */     this.buf.readBytes(dst, length);
/* 500:512 */     return this;
/* 501:    */   }
/* 502:    */   
/* 503:    */   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/* 504:    */   {
/* 505:517 */     this.buf.readBytes(dst, dstIndex, length);
/* 506:518 */     return this;
/* 507:    */   }
/* 508:    */   
/* 509:    */   public ByteBuf readBytes(byte[] dst)
/* 510:    */   {
/* 511:523 */     this.buf.readBytes(dst);
/* 512:524 */     return this;
/* 513:    */   }
/* 514:    */   
/* 515:    */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/* 516:    */   {
/* 517:529 */     this.buf.readBytes(dst, dstIndex, length);
/* 518:530 */     return this;
/* 519:    */   }
/* 520:    */   
/* 521:    */   public ByteBuf readBytes(ByteBuffer dst)
/* 522:    */   {
/* 523:535 */     this.buf.readBytes(dst);
/* 524:536 */     return this;
/* 525:    */   }
/* 526:    */   
/* 527:    */   public ByteBuf readBytes(OutputStream out, int length)
/* 528:    */     throws IOException
/* 529:    */   {
/* 530:541 */     this.buf.readBytes(out, length);
/* 531:542 */     return this;
/* 532:    */   }
/* 533:    */   
/* 534:    */   public int readBytes(GatheringByteChannel out, int length)
/* 535:    */     throws IOException
/* 536:    */   {
/* 537:547 */     return this.buf.readBytes(out, length);
/* 538:    */   }
/* 539:    */   
/* 540:    */   public ByteBuf skipBytes(int length)
/* 541:    */   {
/* 542:552 */     this.buf.skipBytes(length);
/* 543:553 */     return this;
/* 544:    */   }
/* 545:    */   
/* 546:    */   public ByteBuf writeBoolean(boolean value)
/* 547:    */   {
/* 548:558 */     this.buf.writeBoolean(value);
/* 549:559 */     return this;
/* 550:    */   }
/* 551:    */   
/* 552:    */   public ByteBuf writeByte(int value)
/* 553:    */   {
/* 554:564 */     this.buf.writeByte(value);
/* 555:565 */     return this;
/* 556:    */   }
/* 557:    */   
/* 558:    */   public ByteBuf writeShort(int value)
/* 559:    */   {
/* 560:570 */     this.buf.writeShort(ByteBufUtil.swapShort((short)value));
/* 561:571 */     return this;
/* 562:    */   }
/* 563:    */   
/* 564:    */   public ByteBuf writeMedium(int value)
/* 565:    */   {
/* 566:576 */     this.buf.writeMedium(ByteBufUtil.swapMedium(value));
/* 567:577 */     return this;
/* 568:    */   }
/* 569:    */   
/* 570:    */   public ByteBuf writeInt(int value)
/* 571:    */   {
/* 572:582 */     this.buf.writeInt(ByteBufUtil.swapInt(value));
/* 573:583 */     return this;
/* 574:    */   }
/* 575:    */   
/* 576:    */   public ByteBuf writeLong(long value)
/* 577:    */   {
/* 578:588 */     this.buf.writeLong(ByteBufUtil.swapLong(value));
/* 579:589 */     return this;
/* 580:    */   }
/* 581:    */   
/* 582:    */   public ByteBuf writeChar(int value)
/* 583:    */   {
/* 584:594 */     writeShort(value);
/* 585:595 */     return this;
/* 586:    */   }
/* 587:    */   
/* 588:    */   public ByteBuf writeFloat(float value)
/* 589:    */   {
/* 590:600 */     writeInt(Float.floatToRawIntBits(value));
/* 591:601 */     return this;
/* 592:    */   }
/* 593:    */   
/* 594:    */   public ByteBuf writeDouble(double value)
/* 595:    */   {
/* 596:606 */     writeLong(Double.doubleToRawLongBits(value));
/* 597:607 */     return this;
/* 598:    */   }
/* 599:    */   
/* 600:    */   public ByteBuf writeBytes(ByteBuf src)
/* 601:    */   {
/* 602:612 */     this.buf.writeBytes(src);
/* 603:613 */     return this;
/* 604:    */   }
/* 605:    */   
/* 606:    */   public ByteBuf writeBytes(ByteBuf src, int length)
/* 607:    */   {
/* 608:618 */     this.buf.writeBytes(src, length);
/* 609:619 */     return this;
/* 610:    */   }
/* 611:    */   
/* 612:    */   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/* 613:    */   {
/* 614:624 */     this.buf.writeBytes(src, srcIndex, length);
/* 615:625 */     return this;
/* 616:    */   }
/* 617:    */   
/* 618:    */   public ByteBuf writeBytes(byte[] src)
/* 619:    */   {
/* 620:630 */     this.buf.writeBytes(src);
/* 621:631 */     return this;
/* 622:    */   }
/* 623:    */   
/* 624:    */   public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
/* 625:    */   {
/* 626:636 */     this.buf.writeBytes(src, srcIndex, length);
/* 627:637 */     return this;
/* 628:    */   }
/* 629:    */   
/* 630:    */   public ByteBuf writeBytes(ByteBuffer src)
/* 631:    */   {
/* 632:642 */     this.buf.writeBytes(src);
/* 633:643 */     return this;
/* 634:    */   }
/* 635:    */   
/* 636:    */   public int writeBytes(InputStream in, int length)
/* 637:    */     throws IOException
/* 638:    */   {
/* 639:648 */     return this.buf.writeBytes(in, length);
/* 640:    */   }
/* 641:    */   
/* 642:    */   public int writeBytes(ScatteringByteChannel in, int length)
/* 643:    */     throws IOException
/* 644:    */   {
/* 645:653 */     return this.buf.writeBytes(in, length);
/* 646:    */   }
/* 647:    */   
/* 648:    */   public ByteBuf writeZero(int length)
/* 649:    */   {
/* 650:658 */     this.buf.writeZero(length);
/* 651:659 */     return this;
/* 652:    */   }
/* 653:    */   
/* 654:    */   public int indexOf(int fromIndex, int toIndex, byte value)
/* 655:    */   {
/* 656:664 */     return this.buf.indexOf(fromIndex, toIndex, value);
/* 657:    */   }
/* 658:    */   
/* 659:    */   public int bytesBefore(byte value)
/* 660:    */   {
/* 661:669 */     return this.buf.bytesBefore(value);
/* 662:    */   }
/* 663:    */   
/* 664:    */   public int bytesBefore(int length, byte value)
/* 665:    */   {
/* 666:674 */     return this.buf.bytesBefore(length, value);
/* 667:    */   }
/* 668:    */   
/* 669:    */   public int bytesBefore(int index, int length, byte value)
/* 670:    */   {
/* 671:679 */     return this.buf.bytesBefore(index, length, value);
/* 672:    */   }
/* 673:    */   
/* 674:    */   public int forEachByte(ByteBufProcessor processor)
/* 675:    */   {
/* 676:684 */     return this.buf.forEachByte(processor);
/* 677:    */   }
/* 678:    */   
/* 679:    */   public int forEachByte(int index, int length, ByteBufProcessor processor)
/* 680:    */   {
/* 681:689 */     return this.buf.forEachByte(index, length, processor);
/* 682:    */   }
/* 683:    */   
/* 684:    */   public int forEachByteDesc(ByteBufProcessor processor)
/* 685:    */   {
/* 686:694 */     return this.buf.forEachByteDesc(processor);
/* 687:    */   }
/* 688:    */   
/* 689:    */   public int forEachByteDesc(int index, int length, ByteBufProcessor processor)
/* 690:    */   {
/* 691:699 */     return this.buf.forEachByteDesc(index, length, processor);
/* 692:    */   }
/* 693:    */   
/* 694:    */   public ByteBuf copy()
/* 695:    */   {
/* 696:704 */     return this.buf.copy().order(this.order);
/* 697:    */   }
/* 698:    */   
/* 699:    */   public ByteBuf copy(int index, int length)
/* 700:    */   {
/* 701:709 */     return this.buf.copy(index, length).order(this.order);
/* 702:    */   }
/* 703:    */   
/* 704:    */   public ByteBuf slice()
/* 705:    */   {
/* 706:714 */     return this.buf.slice().order(this.order);
/* 707:    */   }
/* 708:    */   
/* 709:    */   public ByteBuf slice(int index, int length)
/* 710:    */   {
/* 711:719 */     return this.buf.slice(index, length).order(this.order);
/* 712:    */   }
/* 713:    */   
/* 714:    */   public ByteBuf duplicate()
/* 715:    */   {
/* 716:724 */     return this.buf.duplicate().order(this.order);
/* 717:    */   }
/* 718:    */   
/* 719:    */   public int nioBufferCount()
/* 720:    */   {
/* 721:729 */     return this.buf.nioBufferCount();
/* 722:    */   }
/* 723:    */   
/* 724:    */   public ByteBuffer nioBuffer()
/* 725:    */   {
/* 726:734 */     return this.buf.nioBuffer().order(this.order);
/* 727:    */   }
/* 728:    */   
/* 729:    */   public ByteBuffer nioBuffer(int index, int length)
/* 730:    */   {
/* 731:739 */     return this.buf.nioBuffer(index, length).order(this.order);
/* 732:    */   }
/* 733:    */   
/* 734:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 735:    */   {
/* 736:744 */     return nioBuffer(index, length);
/* 737:    */   }
/* 738:    */   
/* 739:    */   public ByteBuffer[] nioBuffers()
/* 740:    */   {
/* 741:749 */     ByteBuffer[] nioBuffers = this.buf.nioBuffers();
/* 742:750 */     for (int i = 0; i < nioBuffers.length; i++) {
/* 743:751 */       nioBuffers[i] = nioBuffers[i].order(this.order);
/* 744:    */     }
/* 745:753 */     return nioBuffers;
/* 746:    */   }
/* 747:    */   
/* 748:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 749:    */   {
/* 750:758 */     ByteBuffer[] nioBuffers = this.buf.nioBuffers(index, length);
/* 751:759 */     for (int i = 0; i < nioBuffers.length; i++) {
/* 752:760 */       nioBuffers[i] = nioBuffers[i].order(this.order);
/* 753:    */     }
/* 754:762 */     return nioBuffers;
/* 755:    */   }
/* 756:    */   
/* 757:    */   public boolean hasArray()
/* 758:    */   {
/* 759:767 */     return this.buf.hasArray();
/* 760:    */   }
/* 761:    */   
/* 762:    */   public byte[] array()
/* 763:    */   {
/* 764:772 */     return this.buf.array();
/* 765:    */   }
/* 766:    */   
/* 767:    */   public int arrayOffset()
/* 768:    */   {
/* 769:777 */     return this.buf.arrayOffset();
/* 770:    */   }
/* 771:    */   
/* 772:    */   public boolean hasMemoryAddress()
/* 773:    */   {
/* 774:782 */     return this.buf.hasMemoryAddress();
/* 775:    */   }
/* 776:    */   
/* 777:    */   public long memoryAddress()
/* 778:    */   {
/* 779:787 */     return this.buf.memoryAddress();
/* 780:    */   }
/* 781:    */   
/* 782:    */   public String toString(Charset charset)
/* 783:    */   {
/* 784:792 */     return this.buf.toString(charset);
/* 785:    */   }
/* 786:    */   
/* 787:    */   public String toString(int index, int length, Charset charset)
/* 788:    */   {
/* 789:797 */     return this.buf.toString(index, length, charset);
/* 790:    */   }
/* 791:    */   
/* 792:    */   public int refCnt()
/* 793:    */   {
/* 794:802 */     return this.buf.refCnt();
/* 795:    */   }
/* 796:    */   
/* 797:    */   public ByteBuf retain()
/* 798:    */   {
/* 799:807 */     this.buf.retain();
/* 800:808 */     return this;
/* 801:    */   }
/* 802:    */   
/* 803:    */   public ByteBuf retain(int increment)
/* 804:    */   {
/* 805:813 */     this.buf.retain(increment);
/* 806:814 */     return this;
/* 807:    */   }
/* 808:    */   
/* 809:    */   public boolean release()
/* 810:    */   {
/* 811:819 */     return this.buf.release();
/* 812:    */   }
/* 813:    */   
/* 814:    */   public boolean release(int decrement)
/* 815:    */   {
/* 816:824 */     return this.buf.release(decrement);
/* 817:    */   }
/* 818:    */   
/* 819:    */   public int hashCode()
/* 820:    */   {
/* 821:829 */     return this.buf.hashCode();
/* 822:    */   }
/* 823:    */   
/* 824:    */   public boolean equals(Object obj)
/* 825:    */   {
/* 826:834 */     if (this == obj) {
/* 827:835 */       return true;
/* 828:    */     }
/* 829:837 */     if ((obj instanceof ByteBuf)) {
/* 830:838 */       return ByteBufUtil.equals(this, (ByteBuf)obj);
/* 831:    */     }
/* 832:840 */     return false;
/* 833:    */   }
/* 834:    */   
/* 835:    */   public int compareTo(ByteBuf buffer)
/* 836:    */   {
/* 837:845 */     return ByteBufUtil.compare(this, buffer);
/* 838:    */   }
/* 839:    */   
/* 840:    */   public String toString()
/* 841:    */   {
/* 842:850 */     return "Swapped(" + this.buf.toString() + ')';
/* 843:    */   }
/* 844:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.SwappedByteBuf
 * JD-Core Version:    0.7.0.1
 */