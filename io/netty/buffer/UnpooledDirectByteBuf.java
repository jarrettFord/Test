/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.InputStream;
/*   6:    */ import java.io.OutputStream;
/*   7:    */ import java.nio.Buffer;
/*   8:    */ import java.nio.ByteBuffer;
/*   9:    */ import java.nio.ByteOrder;
/*  10:    */ import java.nio.channels.ClosedChannelException;
/*  11:    */ import java.nio.channels.GatheringByteChannel;
/*  12:    */ import java.nio.channels.ScatteringByteChannel;
/*  13:    */ 
/*  14:    */ public class UnpooledDirectByteBuf
/*  15:    */   extends AbstractReferenceCountedByteBuf
/*  16:    */ {
/*  17:    */   private final ByteBufAllocator alloc;
/*  18:    */   private ByteBuffer buffer;
/*  19:    */   private ByteBuffer tmpNioBuf;
/*  20:    */   private int capacity;
/*  21:    */   private boolean doNotFree;
/*  22:    */   
/*  23:    */   protected UnpooledDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/*  24:    */   {
/*  25: 50 */     super(maxCapacity);
/*  26: 51 */     if (alloc == null) {
/*  27: 52 */       throw new NullPointerException("alloc");
/*  28:    */     }
/*  29: 54 */     if (initialCapacity < 0) {
/*  30: 55 */       throw new IllegalArgumentException("initialCapacity: " + initialCapacity);
/*  31:    */     }
/*  32: 57 */     if (maxCapacity < 0) {
/*  33: 58 */       throw new IllegalArgumentException("maxCapacity: " + maxCapacity);
/*  34:    */     }
/*  35: 60 */     if (initialCapacity > maxCapacity) {
/*  36: 61 */       throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", new Object[] { Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) }));
/*  37:    */     }
/*  38: 65 */     this.alloc = alloc;
/*  39: 66 */     setByteBuffer(ByteBuffer.allocateDirect(initialCapacity));
/*  40:    */   }
/*  41:    */   
/*  42:    */   protected UnpooledDirectByteBuf(ByteBufAllocator alloc, ByteBuffer initialBuffer, int maxCapacity)
/*  43:    */   {
/*  44: 75 */     super(maxCapacity);
/*  45: 76 */     if (alloc == null) {
/*  46: 77 */       throw new NullPointerException("alloc");
/*  47:    */     }
/*  48: 79 */     if (initialBuffer == null) {
/*  49: 80 */       throw new NullPointerException("initialBuffer");
/*  50:    */     }
/*  51: 82 */     if (!initialBuffer.isDirect()) {
/*  52: 83 */       throw new IllegalArgumentException("initialBuffer is not a direct buffer.");
/*  53:    */     }
/*  54: 85 */     if (initialBuffer.isReadOnly()) {
/*  55: 86 */       throw new IllegalArgumentException("initialBuffer is a read-only buffer.");
/*  56:    */     }
/*  57: 89 */     int initialCapacity = initialBuffer.remaining();
/*  58: 90 */     if (initialCapacity > maxCapacity) {
/*  59: 91 */       throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", new Object[] { Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) }));
/*  60:    */     }
/*  61: 95 */     this.alloc = alloc;
/*  62: 96 */     this.doNotFree = true;
/*  63: 97 */     setByteBuffer(initialBuffer.slice().order(ByteOrder.BIG_ENDIAN));
/*  64: 98 */     writerIndex(initialCapacity);
/*  65:    */   }
/*  66:    */   
/*  67:    */   protected ByteBuffer allocateDirect(int initialCapacity)
/*  68:    */   {
/*  69:105 */     return ByteBuffer.allocateDirect(initialCapacity);
/*  70:    */   }
/*  71:    */   
/*  72:    */   protected void freeDirect(ByteBuffer buffer)
/*  73:    */   {
/*  74:112 */     PlatformDependent.freeDirectBuffer(buffer);
/*  75:    */   }
/*  76:    */   
/*  77:    */   private void setByteBuffer(ByteBuffer buffer)
/*  78:    */   {
/*  79:116 */     ByteBuffer oldBuffer = this.buffer;
/*  80:117 */     if (oldBuffer != null) {
/*  81:118 */       if (this.doNotFree) {
/*  82:119 */         this.doNotFree = false;
/*  83:    */       } else {
/*  84:121 */         freeDirect(oldBuffer);
/*  85:    */       }
/*  86:    */     }
/*  87:125 */     this.buffer = buffer;
/*  88:126 */     this.tmpNioBuf = null;
/*  89:127 */     this.capacity = buffer.remaining();
/*  90:    */   }
/*  91:    */   
/*  92:    */   public boolean isDirect()
/*  93:    */   {
/*  94:132 */     return true;
/*  95:    */   }
/*  96:    */   
/*  97:    */   public int capacity()
/*  98:    */   {
/*  99:137 */     return this.capacity;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public ByteBuf capacity(int newCapacity)
/* 103:    */   {
/* 104:142 */     ensureAccessible();
/* 105:143 */     if ((newCapacity < 0) || (newCapacity > maxCapacity())) {
/* 106:144 */       throw new IllegalArgumentException("newCapacity: " + newCapacity);
/* 107:    */     }
/* 108:147 */     int readerIndex = readerIndex();
/* 109:148 */     int writerIndex = writerIndex();
/* 110:    */     
/* 111:150 */     int oldCapacity = this.capacity;
/* 112:151 */     if (newCapacity > oldCapacity)
/* 113:    */     {
/* 114:152 */       ByteBuffer oldBuffer = this.buffer;
/* 115:153 */       ByteBuffer newBuffer = allocateDirect(newCapacity);
/* 116:154 */       oldBuffer.position(0).limit(oldBuffer.capacity());
/* 117:155 */       newBuffer.position(0).limit(oldBuffer.capacity());
/* 118:156 */       newBuffer.put(oldBuffer);
/* 119:157 */       newBuffer.clear();
/* 120:158 */       setByteBuffer(newBuffer);
/* 121:    */     }
/* 122:159 */     else if (newCapacity < oldCapacity)
/* 123:    */     {
/* 124:160 */       ByteBuffer oldBuffer = this.buffer;
/* 125:161 */       ByteBuffer newBuffer = allocateDirect(newCapacity);
/* 126:162 */       if (readerIndex < newCapacity)
/* 127:    */       {
/* 128:163 */         if (writerIndex > newCapacity) {
/* 129:164 */           writerIndex(writerIndex = newCapacity);
/* 130:    */         }
/* 131:166 */         oldBuffer.position(readerIndex).limit(writerIndex);
/* 132:167 */         newBuffer.position(readerIndex).limit(writerIndex);
/* 133:168 */         newBuffer.put(oldBuffer);
/* 134:169 */         newBuffer.clear();
/* 135:    */       }
/* 136:    */       else
/* 137:    */       {
/* 138:171 */         setIndex(newCapacity, newCapacity);
/* 139:    */       }
/* 140:173 */       setByteBuffer(newBuffer);
/* 141:    */     }
/* 142:175 */     return this;
/* 143:    */   }
/* 144:    */   
/* 145:    */   public ByteBufAllocator alloc()
/* 146:    */   {
/* 147:180 */     return this.alloc;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public ByteOrder order()
/* 151:    */   {
/* 152:185 */     return ByteOrder.BIG_ENDIAN;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public boolean hasArray()
/* 156:    */   {
/* 157:190 */     return false;
/* 158:    */   }
/* 159:    */   
/* 160:    */   public byte[] array()
/* 161:    */   {
/* 162:195 */     throw new UnsupportedOperationException("direct buffer");
/* 163:    */   }
/* 164:    */   
/* 165:    */   public int arrayOffset()
/* 166:    */   {
/* 167:200 */     throw new UnsupportedOperationException("direct buffer");
/* 168:    */   }
/* 169:    */   
/* 170:    */   public boolean hasMemoryAddress()
/* 171:    */   {
/* 172:205 */     return false;
/* 173:    */   }
/* 174:    */   
/* 175:    */   public long memoryAddress()
/* 176:    */   {
/* 177:210 */     throw new UnsupportedOperationException();
/* 178:    */   }
/* 179:    */   
/* 180:    */   public byte getByte(int index)
/* 181:    */   {
/* 182:215 */     ensureAccessible();
/* 183:216 */     return _getByte(index);
/* 184:    */   }
/* 185:    */   
/* 186:    */   protected byte _getByte(int index)
/* 187:    */   {
/* 188:221 */     return this.buffer.get(index);
/* 189:    */   }
/* 190:    */   
/* 191:    */   public short getShort(int index)
/* 192:    */   {
/* 193:226 */     ensureAccessible();
/* 194:227 */     return _getShort(index);
/* 195:    */   }
/* 196:    */   
/* 197:    */   protected short _getShort(int index)
/* 198:    */   {
/* 199:232 */     return this.buffer.getShort(index);
/* 200:    */   }
/* 201:    */   
/* 202:    */   public int getUnsignedMedium(int index)
/* 203:    */   {
/* 204:237 */     ensureAccessible();
/* 205:238 */     return _getUnsignedMedium(index);
/* 206:    */   }
/* 207:    */   
/* 208:    */   protected int _getUnsignedMedium(int index)
/* 209:    */   {
/* 210:243 */     return (getByte(index) & 0xFF) << 16 | (getByte(index + 1) & 0xFF) << 8 | getByte(index + 2) & 0xFF;
/* 211:    */   }
/* 212:    */   
/* 213:    */   public int getInt(int index)
/* 214:    */   {
/* 215:248 */     ensureAccessible();
/* 216:249 */     return _getInt(index);
/* 217:    */   }
/* 218:    */   
/* 219:    */   protected int _getInt(int index)
/* 220:    */   {
/* 221:254 */     return this.buffer.getInt(index);
/* 222:    */   }
/* 223:    */   
/* 224:    */   public long getLong(int index)
/* 225:    */   {
/* 226:259 */     ensureAccessible();
/* 227:260 */     return _getLong(index);
/* 228:    */   }
/* 229:    */   
/* 230:    */   protected long _getLong(int index)
/* 231:    */   {
/* 232:265 */     return this.buffer.getLong(index);
/* 233:    */   }
/* 234:    */   
/* 235:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 236:    */   {
/* 237:270 */     checkDstIndex(index, length, dstIndex, dst.capacity());
/* 238:271 */     if (dst.hasArray()) {
/* 239:272 */       getBytes(index, dst.array(), dst.arrayOffset() + dstIndex, length);
/* 240:273 */     } else if (dst.nioBufferCount() > 0) {
/* 241:274 */       for (ByteBuffer bb : dst.nioBuffers(dstIndex, length))
/* 242:    */       {
/* 243:275 */         int bbLen = bb.remaining();
/* 244:276 */         getBytes(index, bb);
/* 245:277 */         index += bbLen;
/* 246:    */       }
/* 247:    */     } else {
/* 248:280 */       dst.setBytes(dstIndex, this, index, length);
/* 249:    */     }
/* 250:282 */     return this;
/* 251:    */   }
/* 252:    */   
/* 253:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 254:    */   {
/* 255:287 */     getBytes(index, dst, dstIndex, length, false);
/* 256:288 */     return this;
/* 257:    */   }
/* 258:    */   
/* 259:    */   private void getBytes(int index, byte[] dst, int dstIndex, int length, boolean internal)
/* 260:    */   {
/* 261:292 */     checkDstIndex(index, length, dstIndex, dst.length);
/* 262:294 */     if ((dstIndex < 0) || (dstIndex > dst.length - length)) {
/* 263:295 */       throw new IndexOutOfBoundsException(String.format("dstIndex: %d, length: %d (expected: range(0, %d))", new Object[] { Integer.valueOf(dstIndex), Integer.valueOf(length), Integer.valueOf(dst.length) }));
/* 264:    */     }
/* 265:    */     ByteBuffer tmpBuf;
/* 266:    */     ByteBuffer tmpBuf;
/* 267:300 */     if (internal) {
/* 268:301 */       tmpBuf = internalNioBuffer();
/* 269:    */     } else {
/* 270:303 */       tmpBuf = this.buffer.duplicate();
/* 271:    */     }
/* 272:305 */     tmpBuf.clear().position(index).limit(index + length);
/* 273:306 */     tmpBuf.get(dst, dstIndex, length);
/* 274:    */   }
/* 275:    */   
/* 276:    */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/* 277:    */   {
/* 278:311 */     checkReadableBytes(length);
/* 279:312 */     getBytes(this.readerIndex, dst, dstIndex, length, true);
/* 280:313 */     this.readerIndex += length;
/* 281:314 */     return this;
/* 282:    */   }
/* 283:    */   
/* 284:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 285:    */   {
/* 286:319 */     getBytes(index, dst, false);
/* 287:320 */     return this;
/* 288:    */   }
/* 289:    */   
/* 290:    */   private void getBytes(int index, ByteBuffer dst, boolean internal)
/* 291:    */   {
/* 292:324 */     checkIndex(index);
/* 293:325 */     if (dst == null) {
/* 294:326 */       throw new NullPointerException("dst");
/* 295:    */     }
/* 296:329 */     int bytesToCopy = Math.min(capacity() - index, dst.remaining());
/* 297:    */     ByteBuffer tmpBuf;
/* 298:    */     ByteBuffer tmpBuf;
/* 299:331 */     if (internal) {
/* 300:332 */       tmpBuf = internalNioBuffer();
/* 301:    */     } else {
/* 302:334 */       tmpBuf = this.buffer.duplicate();
/* 303:    */     }
/* 304:336 */     tmpBuf.clear().position(index).limit(index + bytesToCopy);
/* 305:337 */     dst.put(tmpBuf);
/* 306:    */   }
/* 307:    */   
/* 308:    */   public ByteBuf readBytes(ByteBuffer dst)
/* 309:    */   {
/* 310:342 */     int length = dst.remaining();
/* 311:343 */     checkReadableBytes(length);
/* 312:344 */     getBytes(this.readerIndex, dst, true);
/* 313:345 */     this.readerIndex += length;
/* 314:346 */     return this;
/* 315:    */   }
/* 316:    */   
/* 317:    */   public ByteBuf setByte(int index, int value)
/* 318:    */   {
/* 319:351 */     ensureAccessible();
/* 320:352 */     _setByte(index, value);
/* 321:353 */     return this;
/* 322:    */   }
/* 323:    */   
/* 324:    */   protected void _setByte(int index, int value)
/* 325:    */   {
/* 326:358 */     this.buffer.put(index, (byte)value);
/* 327:    */   }
/* 328:    */   
/* 329:    */   public ByteBuf setShort(int index, int value)
/* 330:    */   {
/* 331:363 */     ensureAccessible();
/* 332:364 */     _setShort(index, value);
/* 333:365 */     return this;
/* 334:    */   }
/* 335:    */   
/* 336:    */   protected void _setShort(int index, int value)
/* 337:    */   {
/* 338:370 */     this.buffer.putShort(index, (short)value);
/* 339:    */   }
/* 340:    */   
/* 341:    */   public ByteBuf setMedium(int index, int value)
/* 342:    */   {
/* 343:375 */     ensureAccessible();
/* 344:376 */     _setMedium(index, value);
/* 345:377 */     return this;
/* 346:    */   }
/* 347:    */   
/* 348:    */   protected void _setMedium(int index, int value)
/* 349:    */   {
/* 350:382 */     setByte(index, (byte)(value >>> 16));
/* 351:383 */     setByte(index + 1, (byte)(value >>> 8));
/* 352:384 */     setByte(index + 2, (byte)value);
/* 353:    */   }
/* 354:    */   
/* 355:    */   public ByteBuf setInt(int index, int value)
/* 356:    */   {
/* 357:389 */     ensureAccessible();
/* 358:390 */     _setInt(index, value);
/* 359:391 */     return this;
/* 360:    */   }
/* 361:    */   
/* 362:    */   protected void _setInt(int index, int value)
/* 363:    */   {
/* 364:396 */     this.buffer.putInt(index, value);
/* 365:    */   }
/* 366:    */   
/* 367:    */   public ByteBuf setLong(int index, long value)
/* 368:    */   {
/* 369:401 */     ensureAccessible();
/* 370:402 */     _setLong(index, value);
/* 371:403 */     return this;
/* 372:    */   }
/* 373:    */   
/* 374:    */   protected void _setLong(int index, long value)
/* 375:    */   {
/* 376:408 */     this.buffer.putLong(index, value);
/* 377:    */   }
/* 378:    */   
/* 379:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 380:    */   {
/* 381:413 */     checkSrcIndex(index, length, srcIndex, src.capacity());
/* 382:414 */     if (src.nioBufferCount() > 0) {
/* 383:415 */       for (ByteBuffer bb : src.nioBuffers(srcIndex, length))
/* 384:    */       {
/* 385:416 */         int bbLen = bb.remaining();
/* 386:417 */         setBytes(index, bb);
/* 387:418 */         index += bbLen;
/* 388:    */       }
/* 389:    */     } else {
/* 390:421 */       src.getBytes(srcIndex, this, index, length);
/* 391:    */     }
/* 392:423 */     return this;
/* 393:    */   }
/* 394:    */   
/* 395:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 396:    */   {
/* 397:428 */     checkSrcIndex(index, length, srcIndex, src.length);
/* 398:429 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 399:430 */     tmpBuf.clear().position(index).limit(index + length);
/* 400:431 */     tmpBuf.put(src, srcIndex, length);
/* 401:432 */     return this;
/* 402:    */   }
/* 403:    */   
/* 404:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 405:    */   {
/* 406:437 */     ensureAccessible();
/* 407:438 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 408:439 */     if (src == tmpBuf) {
/* 409:440 */       src = src.duplicate();
/* 410:    */     }
/* 411:443 */     tmpBuf.clear().position(index).limit(index + src.remaining());
/* 412:444 */     tmpBuf.put(src);
/* 413:445 */     return this;
/* 414:    */   }
/* 415:    */   
/* 416:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 417:    */     throws IOException
/* 418:    */   {
/* 419:450 */     getBytes(index, out, length, false);
/* 420:451 */     return this;
/* 421:    */   }
/* 422:    */   
/* 423:    */   private void getBytes(int index, OutputStream out, int length, boolean internal)
/* 424:    */     throws IOException
/* 425:    */   {
/* 426:455 */     ensureAccessible();
/* 427:456 */     if (length == 0) {
/* 428:457 */       return;
/* 429:    */     }
/* 430:460 */     if (this.buffer.hasArray())
/* 431:    */     {
/* 432:461 */       out.write(this.buffer.array(), index + this.buffer.arrayOffset(), length);
/* 433:    */     }
/* 434:    */     else
/* 435:    */     {
/* 436:463 */       byte[] tmp = new byte[length];
/* 437:    */       ByteBuffer tmpBuf;
/* 438:    */       ByteBuffer tmpBuf;
/* 439:465 */       if (internal) {
/* 440:466 */         tmpBuf = internalNioBuffer();
/* 441:    */       } else {
/* 442:468 */         tmpBuf = this.buffer.duplicate();
/* 443:    */       }
/* 444:470 */       tmpBuf.clear().position(index);
/* 445:471 */       tmpBuf.get(tmp);
/* 446:472 */       out.write(tmp);
/* 447:    */     }
/* 448:    */   }
/* 449:    */   
/* 450:    */   public ByteBuf readBytes(OutputStream out, int length)
/* 451:    */     throws IOException
/* 452:    */   {
/* 453:478 */     checkReadableBytes(length);
/* 454:479 */     getBytes(this.readerIndex, out, length, true);
/* 455:480 */     this.readerIndex += length;
/* 456:481 */     return this;
/* 457:    */   }
/* 458:    */   
/* 459:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 460:    */     throws IOException
/* 461:    */   {
/* 462:486 */     return getBytes(index, out, length, false);
/* 463:    */   }
/* 464:    */   
/* 465:    */   private int getBytes(int index, GatheringByteChannel out, int length, boolean internal)
/* 466:    */     throws IOException
/* 467:    */   {
/* 468:490 */     ensureAccessible();
/* 469:491 */     if (length == 0) {
/* 470:492 */       return 0;
/* 471:    */     }
/* 472:    */     ByteBuffer tmpBuf;
/* 473:    */     ByteBuffer tmpBuf;
/* 474:496 */     if (internal) {
/* 475:497 */       tmpBuf = internalNioBuffer();
/* 476:    */     } else {
/* 477:499 */       tmpBuf = this.buffer.duplicate();
/* 478:    */     }
/* 479:501 */     tmpBuf.clear().position(index).limit(index + length);
/* 480:502 */     return out.write(tmpBuf);
/* 481:    */   }
/* 482:    */   
/* 483:    */   public int readBytes(GatheringByteChannel out, int length)
/* 484:    */     throws IOException
/* 485:    */   {
/* 486:507 */     checkReadableBytes(length);
/* 487:508 */     int readBytes = getBytes(this.readerIndex, out, length, true);
/* 488:509 */     this.readerIndex += readBytes;
/* 489:510 */     return readBytes;
/* 490:    */   }
/* 491:    */   
/* 492:    */   public int setBytes(int index, InputStream in, int length)
/* 493:    */     throws IOException
/* 494:    */   {
/* 495:515 */     ensureAccessible();
/* 496:516 */     if (this.buffer.hasArray()) {
/* 497:517 */       return in.read(this.buffer.array(), this.buffer.arrayOffset() + index, length);
/* 498:    */     }
/* 499:519 */     byte[] tmp = new byte[length];
/* 500:520 */     int readBytes = in.read(tmp);
/* 501:521 */     if (readBytes <= 0) {
/* 502:522 */       return readBytes;
/* 503:    */     }
/* 504:524 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 505:525 */     tmpBuf.clear().position(index);
/* 506:526 */     tmpBuf.put(tmp, 0, readBytes);
/* 507:527 */     return readBytes;
/* 508:    */   }
/* 509:    */   
/* 510:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 511:    */     throws IOException
/* 512:    */   {
/* 513:533 */     ensureAccessible();
/* 514:534 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 515:535 */     tmpBuf.clear().position(index).limit(index + length);
/* 516:    */     try
/* 517:    */     {
/* 518:537 */       return in.read(this.tmpNioBuf);
/* 519:    */     }
/* 520:    */     catch (ClosedChannelException e) {}
/* 521:539 */     return -1;
/* 522:    */   }
/* 523:    */   
/* 524:    */   public int nioBufferCount()
/* 525:    */   {
/* 526:545 */     return 1;
/* 527:    */   }
/* 528:    */   
/* 529:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 530:    */   {
/* 531:550 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 532:    */   }
/* 533:    */   
/* 534:    */   public ByteBuf copy(int index, int length)
/* 535:    */   {
/* 536:555 */     ensureAccessible();
/* 537:    */     ByteBuffer src;
/* 538:    */     try
/* 539:    */     {
/* 540:558 */       src = (ByteBuffer)this.buffer.duplicate().clear().position(index).limit(index + length);
/* 541:    */     }
/* 542:    */     catch (IllegalArgumentException e)
/* 543:    */     {
/* 544:560 */       throw new IndexOutOfBoundsException("Too many bytes to read - Need " + (index + length));
/* 545:    */     }
/* 546:563 */     return alloc().directBuffer(length, maxCapacity()).writeBytes(src);
/* 547:    */   }
/* 548:    */   
/* 549:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 550:    */   {
/* 551:568 */     checkIndex(index, length);
/* 552:569 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 553:    */   }
/* 554:    */   
/* 555:    */   private ByteBuffer internalNioBuffer()
/* 556:    */   {
/* 557:573 */     ByteBuffer tmpNioBuf = this.tmpNioBuf;
/* 558:574 */     if (tmpNioBuf == null) {
/* 559:575 */       this.tmpNioBuf = (tmpNioBuf = this.buffer.duplicate());
/* 560:    */     }
/* 561:577 */     return tmpNioBuf;
/* 562:    */   }
/* 563:    */   
/* 564:    */   public ByteBuffer nioBuffer(int index, int length)
/* 565:    */   {
/* 566:582 */     checkIndex(index, length);
/* 567:583 */     return ((ByteBuffer)this.buffer.duplicate().position(index).limit(index + length)).slice();
/* 568:    */   }
/* 569:    */   
/* 570:    */   protected void deallocate()
/* 571:    */   {
/* 572:588 */     ByteBuffer buffer = this.buffer;
/* 573:589 */     if (buffer == null) {
/* 574:590 */       return;
/* 575:    */     }
/* 576:593 */     this.buffer = null;
/* 577:595 */     if (!this.doNotFree) {
/* 578:596 */       freeDirect(buffer);
/* 579:    */     }
/* 580:    */   }
/* 581:    */   
/* 582:    */   public ByteBuf unwrap()
/* 583:    */   {
/* 584:602 */     return null;
/* 585:    */   }
/* 586:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.UnpooledDirectByteBuf
 * JD-Core Version:    0.7.0.1
 */