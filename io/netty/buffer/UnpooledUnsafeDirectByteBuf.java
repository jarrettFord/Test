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
/*  14:    */ public class UnpooledUnsafeDirectByteBuf
/*  15:    */   extends AbstractReferenceCountedByteBuf
/*  16:    */ {
/*  17: 36 */   private static final boolean NATIVE_ORDER = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
/*  18:    */   private final ByteBufAllocator alloc;
/*  19:    */   private long memoryAddress;
/*  20:    */   private ByteBuffer buffer;
/*  21:    */   private ByteBuffer tmpNioBuf;
/*  22:    */   private int capacity;
/*  23:    */   private boolean doNotFree;
/*  24:    */   
/*  25:    */   protected UnpooledUnsafeDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/*  26:    */   {
/*  27: 53 */     super(maxCapacity);
/*  28: 54 */     if (alloc == null) {
/*  29: 55 */       throw new NullPointerException("alloc");
/*  30:    */     }
/*  31: 57 */     if (initialCapacity < 0) {
/*  32: 58 */       throw new IllegalArgumentException("initialCapacity: " + initialCapacity);
/*  33:    */     }
/*  34: 60 */     if (maxCapacity < 0) {
/*  35: 61 */       throw new IllegalArgumentException("maxCapacity: " + maxCapacity);
/*  36:    */     }
/*  37: 63 */     if (initialCapacity > maxCapacity) {
/*  38: 64 */       throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", new Object[] { Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) }));
/*  39:    */     }
/*  40: 68 */     this.alloc = alloc;
/*  41: 69 */     setByteBuffer(allocateDirect(initialCapacity));
/*  42:    */   }
/*  43:    */   
/*  44:    */   protected UnpooledUnsafeDirectByteBuf(ByteBufAllocator alloc, ByteBuffer initialBuffer, int maxCapacity)
/*  45:    */   {
/*  46: 78 */     super(maxCapacity);
/*  47: 79 */     if (alloc == null) {
/*  48: 80 */       throw new NullPointerException("alloc");
/*  49:    */     }
/*  50: 82 */     if (initialBuffer == null) {
/*  51: 83 */       throw new NullPointerException("initialBuffer");
/*  52:    */     }
/*  53: 85 */     if (!initialBuffer.isDirect()) {
/*  54: 86 */       throw new IllegalArgumentException("initialBuffer is not a direct buffer.");
/*  55:    */     }
/*  56: 88 */     if (initialBuffer.isReadOnly()) {
/*  57: 89 */       throw new IllegalArgumentException("initialBuffer is a read-only buffer.");
/*  58:    */     }
/*  59: 92 */     int initialCapacity = initialBuffer.remaining();
/*  60: 93 */     if (initialCapacity > maxCapacity) {
/*  61: 94 */       throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", new Object[] { Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) }));
/*  62:    */     }
/*  63: 98 */     this.alloc = alloc;
/*  64: 99 */     this.doNotFree = true;
/*  65:100 */     setByteBuffer(initialBuffer.slice().order(ByteOrder.BIG_ENDIAN));
/*  66:101 */     writerIndex(initialCapacity);
/*  67:    */   }
/*  68:    */   
/*  69:    */   protected ByteBuffer allocateDirect(int initialCapacity)
/*  70:    */   {
/*  71:108 */     return ByteBuffer.allocateDirect(initialCapacity);
/*  72:    */   }
/*  73:    */   
/*  74:    */   protected void freeDirect(ByteBuffer buffer)
/*  75:    */   {
/*  76:115 */     PlatformDependent.freeDirectBuffer(buffer);
/*  77:    */   }
/*  78:    */   
/*  79:    */   private void setByteBuffer(ByteBuffer buffer)
/*  80:    */   {
/*  81:119 */     ByteBuffer oldBuffer = this.buffer;
/*  82:120 */     if (oldBuffer != null) {
/*  83:121 */       if (this.doNotFree) {
/*  84:122 */         this.doNotFree = false;
/*  85:    */       } else {
/*  86:124 */         freeDirect(oldBuffer);
/*  87:    */       }
/*  88:    */     }
/*  89:128 */     this.buffer = buffer;
/*  90:129 */     this.memoryAddress = PlatformDependent.directBufferAddress(buffer);
/*  91:130 */     this.tmpNioBuf = null;
/*  92:131 */     this.capacity = buffer.remaining();
/*  93:    */   }
/*  94:    */   
/*  95:    */   public boolean isDirect()
/*  96:    */   {
/*  97:136 */     return true;
/*  98:    */   }
/*  99:    */   
/* 100:    */   public int capacity()
/* 101:    */   {
/* 102:141 */     return this.capacity;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public ByteBuf capacity(int newCapacity)
/* 106:    */   {
/* 107:146 */     ensureAccessible();
/* 108:147 */     if ((newCapacity < 0) || (newCapacity > maxCapacity())) {
/* 109:148 */       throw new IllegalArgumentException("newCapacity: " + newCapacity);
/* 110:    */     }
/* 111:151 */     int readerIndex = readerIndex();
/* 112:152 */     int writerIndex = writerIndex();
/* 113:    */     
/* 114:154 */     int oldCapacity = this.capacity;
/* 115:155 */     if (newCapacity > oldCapacity)
/* 116:    */     {
/* 117:156 */       ByteBuffer oldBuffer = this.buffer;
/* 118:157 */       ByteBuffer newBuffer = allocateDirect(newCapacity);
/* 119:158 */       oldBuffer.position(0).limit(oldBuffer.capacity());
/* 120:159 */       newBuffer.position(0).limit(oldBuffer.capacity());
/* 121:160 */       newBuffer.put(oldBuffer);
/* 122:161 */       newBuffer.clear();
/* 123:162 */       setByteBuffer(newBuffer);
/* 124:    */     }
/* 125:163 */     else if (newCapacity < oldCapacity)
/* 126:    */     {
/* 127:164 */       ByteBuffer oldBuffer = this.buffer;
/* 128:165 */       ByteBuffer newBuffer = allocateDirect(newCapacity);
/* 129:166 */       if (readerIndex < newCapacity)
/* 130:    */       {
/* 131:167 */         if (writerIndex > newCapacity) {
/* 132:168 */           writerIndex(writerIndex = newCapacity);
/* 133:    */         }
/* 134:170 */         oldBuffer.position(readerIndex).limit(writerIndex);
/* 135:171 */         newBuffer.position(readerIndex).limit(writerIndex);
/* 136:172 */         newBuffer.put(oldBuffer);
/* 137:173 */         newBuffer.clear();
/* 138:    */       }
/* 139:    */       else
/* 140:    */       {
/* 141:175 */         setIndex(newCapacity, newCapacity);
/* 142:    */       }
/* 143:177 */       setByteBuffer(newBuffer);
/* 144:    */     }
/* 145:179 */     return this;
/* 146:    */   }
/* 147:    */   
/* 148:    */   public ByteBufAllocator alloc()
/* 149:    */   {
/* 150:184 */     return this.alloc;
/* 151:    */   }
/* 152:    */   
/* 153:    */   public ByteOrder order()
/* 154:    */   {
/* 155:189 */     return ByteOrder.BIG_ENDIAN;
/* 156:    */   }
/* 157:    */   
/* 158:    */   public boolean hasArray()
/* 159:    */   {
/* 160:194 */     return false;
/* 161:    */   }
/* 162:    */   
/* 163:    */   public byte[] array()
/* 164:    */   {
/* 165:199 */     throw new UnsupportedOperationException("direct buffer");
/* 166:    */   }
/* 167:    */   
/* 168:    */   public int arrayOffset()
/* 169:    */   {
/* 170:204 */     throw new UnsupportedOperationException("direct buffer");
/* 171:    */   }
/* 172:    */   
/* 173:    */   public boolean hasMemoryAddress()
/* 174:    */   {
/* 175:209 */     return true;
/* 176:    */   }
/* 177:    */   
/* 178:    */   public long memoryAddress()
/* 179:    */   {
/* 180:214 */     return this.memoryAddress;
/* 181:    */   }
/* 182:    */   
/* 183:    */   protected byte _getByte(int index)
/* 184:    */   {
/* 185:219 */     return PlatformDependent.getByte(addr(index));
/* 186:    */   }
/* 187:    */   
/* 188:    */   protected short _getShort(int index)
/* 189:    */   {
/* 190:224 */     short v = PlatformDependent.getShort(addr(index));
/* 191:225 */     return NATIVE_ORDER ? v : Short.reverseBytes(v);
/* 192:    */   }
/* 193:    */   
/* 194:    */   protected int _getUnsignedMedium(int index)
/* 195:    */   {
/* 196:230 */     long addr = addr(index);
/* 197:231 */     return (PlatformDependent.getByte(addr) & 0xFF) << 16 | (PlatformDependent.getByte(addr + 1L) & 0xFF) << 8 | PlatformDependent.getByte(addr + 2L) & 0xFF;
/* 198:    */   }
/* 199:    */   
/* 200:    */   protected int _getInt(int index)
/* 201:    */   {
/* 202:238 */     int v = PlatformDependent.getInt(addr(index));
/* 203:239 */     return NATIVE_ORDER ? v : Integer.reverseBytes(v);
/* 204:    */   }
/* 205:    */   
/* 206:    */   protected long _getLong(int index)
/* 207:    */   {
/* 208:244 */     long v = PlatformDependent.getLong(addr(index));
/* 209:245 */     return NATIVE_ORDER ? v : Long.reverseBytes(v);
/* 210:    */   }
/* 211:    */   
/* 212:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 213:    */   {
/* 214:250 */     checkIndex(index, length);
/* 215:251 */     if (dst == null) {
/* 216:252 */       throw new NullPointerException("dst");
/* 217:    */     }
/* 218:254 */     if ((dstIndex < 0) || (dstIndex > dst.capacity() - length)) {
/* 219:255 */       throw new IndexOutOfBoundsException("dstIndex: " + dstIndex);
/* 220:    */     }
/* 221:258 */     if (dst.hasMemoryAddress()) {
/* 222:259 */       PlatformDependent.copyMemory(addr(index), dst.memoryAddress() + dstIndex, length);
/* 223:260 */     } else if (dst.hasArray()) {
/* 224:261 */       PlatformDependent.copyMemory(addr(index), dst.array(), dst.arrayOffset() + dstIndex, length);
/* 225:    */     } else {
/* 226:263 */       dst.setBytes(dstIndex, this, index, length);
/* 227:    */     }
/* 228:265 */     return this;
/* 229:    */   }
/* 230:    */   
/* 231:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 232:    */   {
/* 233:270 */     checkIndex(index, length);
/* 234:271 */     if (dst == null) {
/* 235:272 */       throw new NullPointerException("dst");
/* 236:    */     }
/* 237:274 */     if ((dstIndex < 0) || (dstIndex > dst.length - length)) {
/* 238:275 */       throw new IndexOutOfBoundsException(String.format("dstIndex: %d, length: %d (expected: range(0, %d))", new Object[] { Integer.valueOf(dstIndex), Integer.valueOf(length), Integer.valueOf(dst.length) }));
/* 239:    */     }
/* 240:279 */     if (length != 0) {
/* 241:280 */       PlatformDependent.copyMemory(addr(index), dst, dstIndex, length);
/* 242:    */     }
/* 243:282 */     return this;
/* 244:    */   }
/* 245:    */   
/* 246:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 247:    */   {
/* 248:287 */     getBytes(index, dst, false);
/* 249:288 */     return this;
/* 250:    */   }
/* 251:    */   
/* 252:    */   private void getBytes(int index, ByteBuffer dst, boolean internal)
/* 253:    */   {
/* 254:292 */     checkIndex(index);
/* 255:293 */     if (dst == null) {
/* 256:294 */       throw new NullPointerException("dst");
/* 257:    */     }
/* 258:297 */     int bytesToCopy = Math.min(capacity() - index, dst.remaining());
/* 259:    */     ByteBuffer tmpBuf;
/* 260:    */     ByteBuffer tmpBuf;
/* 261:299 */     if (internal) {
/* 262:300 */       tmpBuf = internalNioBuffer();
/* 263:    */     } else {
/* 264:302 */       tmpBuf = this.buffer.duplicate();
/* 265:    */     }
/* 266:304 */     tmpBuf.clear().position(index).limit(index + bytesToCopy);
/* 267:305 */     dst.put(tmpBuf);
/* 268:    */   }
/* 269:    */   
/* 270:    */   public ByteBuf readBytes(ByteBuffer dst)
/* 271:    */   {
/* 272:310 */     int length = dst.remaining();
/* 273:311 */     checkReadableBytes(length);
/* 274:312 */     getBytes(this.readerIndex, dst, true);
/* 275:313 */     this.readerIndex += length;
/* 276:314 */     return this;
/* 277:    */   }
/* 278:    */   
/* 279:    */   protected void _setByte(int index, int value)
/* 280:    */   {
/* 281:319 */     PlatformDependent.putByte(addr(index), (byte)value);
/* 282:    */   }
/* 283:    */   
/* 284:    */   protected void _setShort(int index, int value)
/* 285:    */   {
/* 286:324 */     PlatformDependent.putShort(addr(index), NATIVE_ORDER ? (short)value : Short.reverseBytes((short)value));
/* 287:    */   }
/* 288:    */   
/* 289:    */   protected void _setMedium(int index, int value)
/* 290:    */   {
/* 291:329 */     long addr = addr(index);
/* 292:330 */     PlatformDependent.putByte(addr, (byte)(value >>> 16));
/* 293:331 */     PlatformDependent.putByte(addr + 1L, (byte)(value >>> 8));
/* 294:332 */     PlatformDependent.putByte(addr + 2L, (byte)value);
/* 295:    */   }
/* 296:    */   
/* 297:    */   protected void _setInt(int index, int value)
/* 298:    */   {
/* 299:337 */     PlatformDependent.putInt(addr(index), NATIVE_ORDER ? value : Integer.reverseBytes(value));
/* 300:    */   }
/* 301:    */   
/* 302:    */   protected void _setLong(int index, long value)
/* 303:    */   {
/* 304:342 */     PlatformDependent.putLong(addr(index), NATIVE_ORDER ? value : Long.reverseBytes(value));
/* 305:    */   }
/* 306:    */   
/* 307:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 308:    */   {
/* 309:347 */     checkIndex(index, length);
/* 310:348 */     if (src == null) {
/* 311:349 */       throw new NullPointerException("src");
/* 312:    */     }
/* 313:351 */     if ((srcIndex < 0) || (srcIndex > src.capacity() - length)) {
/* 314:352 */       throw new IndexOutOfBoundsException("srcIndex: " + srcIndex);
/* 315:    */     }
/* 316:355 */     if (length != 0) {
/* 317:356 */       if (src.hasMemoryAddress()) {
/* 318:357 */         PlatformDependent.copyMemory(src.memoryAddress() + srcIndex, addr(index), length);
/* 319:358 */       } else if (src.hasArray()) {
/* 320:359 */         PlatformDependent.copyMemory(src.array(), src.arrayOffset() + srcIndex, addr(index), length);
/* 321:    */       } else {
/* 322:361 */         src.getBytes(srcIndex, this, index, length);
/* 323:    */       }
/* 324:    */     }
/* 325:364 */     return this;
/* 326:    */   }
/* 327:    */   
/* 328:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 329:    */   {
/* 330:369 */     checkIndex(index, length);
/* 331:370 */     if (length != 0) {
/* 332:371 */       PlatformDependent.copyMemory(src, srcIndex, addr(index), length);
/* 333:    */     }
/* 334:373 */     return this;
/* 335:    */   }
/* 336:    */   
/* 337:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 338:    */   {
/* 339:378 */     ensureAccessible();
/* 340:379 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 341:380 */     if (src == tmpBuf) {
/* 342:381 */       src = src.duplicate();
/* 343:    */     }
/* 344:384 */     tmpBuf.clear().position(index).limit(index + src.remaining());
/* 345:385 */     tmpBuf.put(src);
/* 346:386 */     return this;
/* 347:    */   }
/* 348:    */   
/* 349:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 350:    */     throws IOException
/* 351:    */   {
/* 352:391 */     ensureAccessible();
/* 353:392 */     if (length != 0)
/* 354:    */     {
/* 355:393 */       byte[] tmp = new byte[length];
/* 356:394 */       PlatformDependent.copyMemory(addr(index), tmp, 0, length);
/* 357:395 */       out.write(tmp);
/* 358:    */     }
/* 359:397 */     return this;
/* 360:    */   }
/* 361:    */   
/* 362:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 363:    */     throws IOException
/* 364:    */   {
/* 365:402 */     return getBytes(index, out, length, false);
/* 366:    */   }
/* 367:    */   
/* 368:    */   private int getBytes(int index, GatheringByteChannel out, int length, boolean internal)
/* 369:    */     throws IOException
/* 370:    */   {
/* 371:406 */     ensureAccessible();
/* 372:407 */     if (length == 0) {
/* 373:408 */       return 0;
/* 374:    */     }
/* 375:    */     ByteBuffer tmpBuf;
/* 376:    */     ByteBuffer tmpBuf;
/* 377:412 */     if (internal) {
/* 378:413 */       tmpBuf = internalNioBuffer();
/* 379:    */     } else {
/* 380:415 */       tmpBuf = this.buffer.duplicate();
/* 381:    */     }
/* 382:417 */     tmpBuf.clear().position(index).limit(index + length);
/* 383:418 */     return out.write(tmpBuf);
/* 384:    */   }
/* 385:    */   
/* 386:    */   public int readBytes(GatheringByteChannel out, int length)
/* 387:    */     throws IOException
/* 388:    */   {
/* 389:423 */     checkReadableBytes(length);
/* 390:424 */     int readBytes = getBytes(this.readerIndex, out, length, true);
/* 391:425 */     this.readerIndex += readBytes;
/* 392:426 */     return readBytes;
/* 393:    */   }
/* 394:    */   
/* 395:    */   public int setBytes(int index, InputStream in, int length)
/* 396:    */     throws IOException
/* 397:    */   {
/* 398:431 */     checkIndex(index, length);
/* 399:432 */     byte[] tmp = new byte[length];
/* 400:433 */     int readBytes = in.read(tmp);
/* 401:434 */     if (readBytes > 0) {
/* 402:435 */       PlatformDependent.copyMemory(tmp, 0, addr(index), readBytes);
/* 403:    */     }
/* 404:437 */     return readBytes;
/* 405:    */   }
/* 406:    */   
/* 407:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 408:    */     throws IOException
/* 409:    */   {
/* 410:442 */     ensureAccessible();
/* 411:443 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 412:444 */     tmpBuf.clear().position(index).limit(index + length);
/* 413:    */     try
/* 414:    */     {
/* 415:446 */       return in.read(tmpBuf);
/* 416:    */     }
/* 417:    */     catch (ClosedChannelException e) {}
/* 418:448 */     return -1;
/* 419:    */   }
/* 420:    */   
/* 421:    */   public int nioBufferCount()
/* 422:    */   {
/* 423:454 */     return 1;
/* 424:    */   }
/* 425:    */   
/* 426:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 427:    */   {
/* 428:459 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 429:    */   }
/* 430:    */   
/* 431:    */   public ByteBuf copy(int index, int length)
/* 432:    */   {
/* 433:464 */     checkIndex(index, length);
/* 434:465 */     ByteBuf copy = alloc().directBuffer(length, maxCapacity());
/* 435:466 */     if (length != 0) {
/* 436:467 */       if (copy.hasMemoryAddress())
/* 437:    */       {
/* 438:468 */         PlatformDependent.copyMemory(addr(index), copy.memoryAddress(), length);
/* 439:469 */         copy.setIndex(0, length);
/* 440:    */       }
/* 441:    */       else
/* 442:    */       {
/* 443:471 */         copy.writeBytes(this, index, length);
/* 444:    */       }
/* 445:    */     }
/* 446:474 */     return copy;
/* 447:    */   }
/* 448:    */   
/* 449:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 450:    */   {
/* 451:479 */     checkIndex(index, length);
/* 452:480 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 453:    */   }
/* 454:    */   
/* 455:    */   private ByteBuffer internalNioBuffer()
/* 456:    */   {
/* 457:484 */     ByteBuffer tmpNioBuf = this.tmpNioBuf;
/* 458:485 */     if (tmpNioBuf == null) {
/* 459:486 */       this.tmpNioBuf = (tmpNioBuf = this.buffer.duplicate());
/* 460:    */     }
/* 461:488 */     return tmpNioBuf;
/* 462:    */   }
/* 463:    */   
/* 464:    */   public ByteBuffer nioBuffer(int index, int length)
/* 465:    */   {
/* 466:493 */     checkIndex(index, length);
/* 467:494 */     return ((ByteBuffer)this.buffer.duplicate().position(index).limit(index + length)).slice();
/* 468:    */   }
/* 469:    */   
/* 470:    */   protected void deallocate()
/* 471:    */   {
/* 472:499 */     ByteBuffer buffer = this.buffer;
/* 473:500 */     if (buffer == null) {
/* 474:501 */       return;
/* 475:    */     }
/* 476:504 */     this.buffer = null;
/* 477:506 */     if (!this.doNotFree) {
/* 478:507 */       freeDirect(buffer);
/* 479:    */     }
/* 480:    */   }
/* 481:    */   
/* 482:    */   public ByteBuf unwrap()
/* 483:    */   {
/* 484:513 */     return null;
/* 485:    */   }
/* 486:    */   
/* 487:    */   long addr(int index)
/* 488:    */   {
/* 489:517 */     return this.memoryAddress + index;
/* 490:    */   }
/* 491:    */   
/* 492:    */   protected SwappedByteBuf newSwappedByteBuf()
/* 493:    */   {
/* 494:522 */     return new UnsafeDirectSwappedByteBuf(this);
/* 495:    */   }
/* 496:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.UnpooledUnsafeDirectByteBuf
 * JD-Core Version:    0.7.0.1
 */