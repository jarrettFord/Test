/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.ByteBufHolder;
/*   6:    */ import io.netty.buffer.Unpooled;
/*   7:    */ import io.netty.buffer.UnpooledByteBufAllocator;
/*   8:    */ import io.netty.buffer.UnpooledDirectByteBuf;
/*   9:    */ import io.netty.util.Recycler;
/*  10:    */ import io.netty.util.Recycler.Handle;
/*  11:    */ import io.netty.util.ReferenceCountUtil;
/*  12:    */ import io.netty.util.internal.PlatformDependent;
/*  13:    */ import io.netty.util.internal.SystemPropertyUtil;
/*  14:    */ import io.netty.util.internal.logging.InternalLogger;
/*  15:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  16:    */ import java.nio.ByteBuffer;
/*  17:    */ import java.nio.channels.ClosedChannelException;
/*  18:    */ import java.util.Arrays;
/*  19:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  20:    */ import java.util.concurrent.atomic.AtomicLongFieldUpdater;
/*  21:    */ 
/*  22:    */ public final class ChannelOutboundBuffer
/*  23:    */ {
/*  24: 49 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChannelOutboundBuffer.class);
/*  25:    */   private static final int INITIAL_CAPACITY = 32;
/*  26: 56 */   private static final int threadLocalDirectBufferSize = SystemPropertyUtil.getInt("io.netty.threadLocalDirectBufferSize", 65536);
/*  27:    */   private static final Recycler<ChannelOutboundBuffer> RECYCLER;
/*  28:    */   private final Recycler.Handle handle;
/*  29:    */   private AbstractChannel channel;
/*  30:    */   private Entry[] buffer;
/*  31:    */   private int flushed;
/*  32:    */   private int unflushed;
/*  33:    */   private int tail;
/*  34:    */   private ByteBuffer[] nioBuffers;
/*  35:    */   private int nioBufferCount;
/*  36:    */   private long nioBufferSize;
/*  37:    */   private boolean inFail;
/*  38:    */   private static final AtomicLongFieldUpdater<ChannelOutboundBuffer> TOTAL_PENDING_SIZE_UPDATER;
/*  39:    */   private volatile long totalPendingSize;
/*  40:    */   private static final AtomicIntegerFieldUpdater<ChannelOutboundBuffer> WRITABLE_UPDATER;
/*  41:    */   
/*  42:    */   static ChannelOutboundBuffer newInstance(AbstractChannel channel)
/*  43:    */   {
/*  44: 68 */     ChannelOutboundBuffer buffer = (ChannelOutboundBuffer)RECYCLER.get();
/*  45: 69 */     buffer.channel = channel;
/*  46: 70 */     buffer.totalPendingSize = 0L;
/*  47: 71 */     buffer.writable = 1;
/*  48: 72 */     return buffer;
/*  49:    */   }
/*  50:    */   
/*  51:    */   static
/*  52:    */   {
/*  53: 57 */     logger.debug("-Dio.netty.threadLocalDirectBufferSize: {}", Integer.valueOf(threadLocalDirectBufferSize));
/*  54:    */     
/*  55:    */ 
/*  56: 60 */     RECYCLER = new Recycler()
/*  57:    */     {
/*  58:    */       protected ChannelOutboundBuffer newObject(Recycler.Handle handle)
/*  59:    */       {
/*  60: 63 */         return new ChannelOutboundBuffer(handle, null);
/*  61:    */       }
/*  62: 99 */     };
/*  63:100 */     AtomicIntegerFieldUpdater<ChannelOutboundBuffer> writableUpdater = PlatformDependent.newAtomicIntegerFieldUpdater(ChannelOutboundBuffer.class, "writable");
/*  64:102 */     if (writableUpdater == null) {
/*  65:103 */       writableUpdater = AtomicIntegerFieldUpdater.newUpdater(ChannelOutboundBuffer.class, "writable");
/*  66:    */     }
/*  67:105 */     WRITABLE_UPDATER = writableUpdater;
/*  68:    */     
/*  69:107 */     AtomicLongFieldUpdater<ChannelOutboundBuffer> pendingSizeUpdater = PlatformDependent.newAtomicLongFieldUpdater(ChannelOutboundBuffer.class, "totalPendingSize");
/*  70:109 */     if (pendingSizeUpdater == null) {
/*  71:110 */       pendingSizeUpdater = AtomicLongFieldUpdater.newUpdater(ChannelOutboundBuffer.class, "totalPendingSize");
/*  72:    */     }
/*  73:112 */     TOTAL_PENDING_SIZE_UPDATER = pendingSizeUpdater;
/*  74:    */   }
/*  75:    */   
/*  76:115 */   private volatile int writable = 1;
/*  77:    */   
/*  78:    */   private ChannelOutboundBuffer(Recycler.Handle handle)
/*  79:    */   {
/*  80:118 */     this.handle = handle;
/*  81:    */     
/*  82:120 */     this.buffer = new Entry[32];
/*  83:121 */     for (int i = 0; i < this.buffer.length; i++) {
/*  84:122 */       this.buffer[i] = new Entry(null);
/*  85:    */     }
/*  86:125 */     this.nioBuffers = new ByteBuffer[32];
/*  87:    */   }
/*  88:    */   
/*  89:    */   void addMessage(Object msg, ChannelPromise promise)
/*  90:    */   {
/*  91:129 */     int size = this.channel.estimatorHandle().size(msg);
/*  92:130 */     if (size < 0) {
/*  93:131 */       size = 0;
/*  94:    */     }
/*  95:134 */     Entry e = this.buffer[(this.tail++)];
/*  96:135 */     e.msg = msg;
/*  97:136 */     e.pendingSize = size;
/*  98:137 */     e.promise = promise;
/*  99:138 */     e.total = total(msg);
/* 100:    */     
/* 101:140 */     this.tail &= this.buffer.length - 1;
/* 102:142 */     if (this.tail == this.flushed) {
/* 103:143 */       addCapacity();
/* 104:    */     }
/* 105:148 */     incrementPendingOutboundBytes(size);
/* 106:    */   }
/* 107:    */   
/* 108:    */   private void addCapacity()
/* 109:    */   {
/* 110:152 */     int p = this.flushed;
/* 111:153 */     int n = this.buffer.length;
/* 112:154 */     int r = n - p;
/* 113:155 */     int s = size();
/* 114:    */     
/* 115:157 */     int newCapacity = n << 1;
/* 116:158 */     if (newCapacity < 0) {
/* 117:159 */       throw new IllegalStateException();
/* 118:    */     }
/* 119:162 */     Entry[] e = new Entry[newCapacity];
/* 120:163 */     System.arraycopy(this.buffer, p, e, 0, r);
/* 121:164 */     System.arraycopy(this.buffer, 0, e, r, p);
/* 122:165 */     for (int i = n; i < e.length; i++) {
/* 123:166 */       e[i] = new Entry(null);
/* 124:    */     }
/* 125:169 */     this.buffer = e;
/* 126:170 */     this.flushed = 0;
/* 127:171 */     this.unflushed = s;
/* 128:172 */     this.tail = n;
/* 129:    */   }
/* 130:    */   
/* 131:    */   void addFlush()
/* 132:    */   {
/* 133:180 */     if (this.unflushed != this.tail)
/* 134:    */     {
/* 135:181 */       this.unflushed = this.tail;
/* 136:    */       
/* 137:183 */       int mask = this.buffer.length - 1;
/* 138:184 */       int i = this.flushed;
/* 139:185 */       while ((i != this.unflushed) && (this.buffer[i].msg != null))
/* 140:    */       {
/* 141:186 */         Entry entry = this.buffer[i];
/* 142:187 */         if (!entry.promise.setUncancellable())
/* 143:    */         {
/* 144:189 */           int pending = entry.cancel();
/* 145:190 */           decrementPendingOutboundBytes(pending);
/* 146:    */         }
/* 147:192 */         i = i + 1 & mask;
/* 148:    */       }
/* 149:    */     }
/* 150:    */   }
/* 151:    */   
/* 152:    */   void incrementPendingOutboundBytes(int size)
/* 153:    */   {
/* 154:204 */     Channel channel = this.channel;
/* 155:205 */     if ((size == 0) || (channel == null)) {
/* 156:206 */       return;
/* 157:    */     }
/* 158:209 */     long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, size);
/* 159:210 */     if ((newWriteBufferSize > channel.config().getWriteBufferHighWaterMark()) && 
/* 160:211 */       (WRITABLE_UPDATER.compareAndSet(this, 1, 0))) {
/* 161:212 */       channel.pipeline().fireChannelWritabilityChanged();
/* 162:    */     }
/* 163:    */   }
/* 164:    */   
/* 165:    */   void decrementPendingOutboundBytes(int size)
/* 166:    */   {
/* 167:224 */     Channel channel = this.channel;
/* 168:225 */     if ((size == 0) || (channel == null)) {
/* 169:226 */       return;
/* 170:    */     }
/* 171:229 */     long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, -size);
/* 172:230 */     if (((newWriteBufferSize == 0L) || (newWriteBufferSize < channel.config().getWriteBufferLowWaterMark())) && 
/* 173:231 */       (WRITABLE_UPDATER.compareAndSet(this, 0, 1))) {
/* 174:232 */       channel.pipeline().fireChannelWritabilityChanged();
/* 175:    */     }
/* 176:    */   }
/* 177:    */   
/* 178:    */   private static long total(Object msg)
/* 179:    */   {
/* 180:238 */     if ((msg instanceof ByteBuf)) {
/* 181:239 */       return ((ByteBuf)msg).readableBytes();
/* 182:    */     }
/* 183:241 */     if ((msg instanceof FileRegion)) {
/* 184:242 */       return ((FileRegion)msg).count();
/* 185:    */     }
/* 186:244 */     if ((msg instanceof ByteBufHolder)) {
/* 187:245 */       return ((ByteBufHolder)msg).content().readableBytes();
/* 188:    */     }
/* 189:247 */     return -1L;
/* 190:    */   }
/* 191:    */   
/* 192:    */   public Object current()
/* 193:    */   {
/* 194:251 */     return current(true);
/* 195:    */   }
/* 196:    */   
/* 197:    */   public Object current(boolean preferDirect)
/* 198:    */   {
/* 199:255 */     if (isEmpty()) {
/* 200:256 */       return null;
/* 201:    */     }
/* 202:259 */     Entry entry = this.buffer[this.flushed];
/* 203:260 */     Object msg = entry.msg;
/* 204:261 */     if ((threadLocalDirectBufferSize <= 0) || (!preferDirect)) {
/* 205:262 */       return msg;
/* 206:    */     }
/* 207:264 */     if ((msg instanceof ByteBuf))
/* 208:    */     {
/* 209:265 */       ByteBuf buf = (ByteBuf)msg;
/* 210:266 */       if (buf.isDirect()) {
/* 211:267 */         return buf;
/* 212:    */       }
/* 213:269 */       int readableBytes = buf.readableBytes();
/* 214:270 */       if (readableBytes == 0) {
/* 215:271 */         return buf;
/* 216:    */       }
/* 217:277 */       ByteBufAllocator alloc = this.channel.alloc();
/* 218:    */       ByteBuf directBuf;
/* 219:    */       ByteBuf directBuf;
/* 220:279 */       if (alloc.isDirectBufferPooled()) {
/* 221:280 */         directBuf = alloc.directBuffer(readableBytes);
/* 222:    */       } else {
/* 223:282 */         directBuf = ThreadLocalPooledByteBuf.newInstance();
/* 224:    */       }
/* 225:284 */       directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
/* 226:285 */       current(directBuf);
/* 227:286 */       return directBuf;
/* 228:    */     }
/* 229:289 */     return msg;
/* 230:    */   }
/* 231:    */   
/* 232:    */   public void current(Object msg)
/* 233:    */   {
/* 234:298 */     Entry entry = this.buffer[this.flushed];
/* 235:299 */     safeRelease(entry.msg);
/* 236:300 */     entry.msg = msg;
/* 237:    */   }
/* 238:    */   
/* 239:    */   public void progress(long amount)
/* 240:    */   {
/* 241:304 */     Entry e = this.buffer[this.flushed];
/* 242:305 */     ChannelPromise p = e.promise;
/* 243:306 */     if ((p instanceof ChannelProgressivePromise))
/* 244:    */     {
/* 245:307 */       long progress = e.progress + amount;
/* 246:308 */       e.progress = progress;
/* 247:309 */       ((ChannelProgressivePromise)p).tryProgress(progress, e.total);
/* 248:    */     }
/* 249:    */   }
/* 250:    */   
/* 251:    */   public boolean remove()
/* 252:    */   {
/* 253:314 */     if (isEmpty()) {
/* 254:315 */       return false;
/* 255:    */     }
/* 256:318 */     Entry e = this.buffer[this.flushed];
/* 257:319 */     Object msg = e.msg;
/* 258:320 */     if (msg == null) {
/* 259:321 */       return false;
/* 260:    */     }
/* 261:324 */     ChannelPromise promise = e.promise;
/* 262:325 */     int size = e.pendingSize;
/* 263:    */     
/* 264:327 */     e.clear();
/* 265:    */     
/* 266:329 */     this.flushed = (this.flushed + 1 & this.buffer.length - 1);
/* 267:331 */     if (!e.cancelled)
/* 268:    */     {
/* 269:333 */       safeRelease(msg);
/* 270:334 */       safeSuccess(promise);
/* 271:335 */       decrementPendingOutboundBytes(size);
/* 272:    */     }
/* 273:338 */     return true;
/* 274:    */   }
/* 275:    */   
/* 276:    */   public boolean remove(Throwable cause)
/* 277:    */   {
/* 278:342 */     if (isEmpty()) {
/* 279:343 */       return false;
/* 280:    */     }
/* 281:346 */     Entry e = this.buffer[this.flushed];
/* 282:347 */     Object msg = e.msg;
/* 283:348 */     if (msg == null) {
/* 284:349 */       return false;
/* 285:    */     }
/* 286:352 */     ChannelPromise promise = e.promise;
/* 287:353 */     int size = e.pendingSize;
/* 288:    */     
/* 289:355 */     e.clear();
/* 290:    */     
/* 291:357 */     this.flushed = (this.flushed + 1 & this.buffer.length - 1);
/* 292:359 */     if (!e.cancelled)
/* 293:    */     {
/* 294:361 */       safeRelease(msg);
/* 295:    */       
/* 296:363 */       safeFail(promise, cause);
/* 297:364 */       decrementPendingOutboundBytes(size);
/* 298:    */     }
/* 299:367 */     return true;
/* 300:    */   }
/* 301:    */   
/* 302:    */   public ByteBuffer[] nioBuffers()
/* 303:    */   {
/* 304:382 */     long nioBufferSize = 0L;
/* 305:383 */     int nioBufferCount = 0;
/* 306:384 */     int mask = this.buffer.length - 1;
/* 307:385 */     ByteBufAllocator alloc = this.channel.alloc();
/* 308:386 */     ByteBuffer[] nioBuffers = this.nioBuffers;
/* 309:    */     
/* 310:388 */     int i = this.flushed;
/* 311:    */     Object m;
/* 312:389 */     while ((i != this.unflushed) && ((m = this.buffer[i].msg) != null))
/* 313:    */     {
/* 314:390 */       if (!(m instanceof ByteBuf))
/* 315:    */       {
/* 316:391 */         this.nioBufferCount = 0;
/* 317:392 */         this.nioBufferSize = 0L;
/* 318:393 */         return null;
/* 319:    */       }
/* 320:396 */       Entry entry = this.buffer[i];
/* 321:398 */       if (!entry.cancelled)
/* 322:    */       {
/* 323:399 */         ByteBuf buf = (ByteBuf)m;
/* 324:400 */         int readerIndex = buf.readerIndex();
/* 325:401 */         int readableBytes = buf.writerIndex() - readerIndex;
/* 326:403 */         if (readableBytes > 0)
/* 327:    */         {
/* 328:404 */           nioBufferSize += readableBytes;
/* 329:405 */           int count = entry.count;
/* 330:406 */           if (count == -1) {
/* 331:408 */             entry.count = (count = buf.nioBufferCount());
/* 332:    */           }
/* 333:410 */           int neededSpace = nioBufferCount + count;
/* 334:411 */           if (neededSpace > nioBuffers.length) {
/* 335:412 */             this.nioBuffers = (nioBuffers = expandNioBufferArray(nioBuffers, neededSpace, nioBufferCount));
/* 336:    */           }
/* 337:415 */           if ((buf.isDirect()) || (threadLocalDirectBufferSize <= 0))
/* 338:    */           {
/* 339:416 */             if (count == 1)
/* 340:    */             {
/* 341:417 */               ByteBuffer nioBuf = entry.buf;
/* 342:418 */               if (nioBuf == null) {
/* 343:421 */                 entry.buf = (nioBuf = buf.internalNioBuffer(readerIndex, readableBytes));
/* 344:    */               }
/* 345:423 */               nioBuffers[(nioBufferCount++)] = nioBuf;
/* 346:    */             }
/* 347:    */             else
/* 348:    */             {
/* 349:425 */               ByteBuffer[] nioBufs = entry.buffers;
/* 350:426 */               if (nioBufs == null) {
/* 351:429 */                 entry.buffers = (nioBufs = buf.nioBuffers());
/* 352:    */               }
/* 353:431 */               nioBufferCount = fillBufferArray(nioBufs, nioBuffers, nioBufferCount);
/* 354:    */             }
/* 355:    */           }
/* 356:    */           else {
/* 357:434 */             nioBufferCount = fillBufferArrayNonDirect(entry, buf, readerIndex, readableBytes, alloc, nioBuffers, nioBufferCount);
/* 358:    */           }
/* 359:    */         }
/* 360:    */       }
/* 361:440 */       i = i + 1 & mask;
/* 362:    */     }
/* 363:442 */     this.nioBufferCount = nioBufferCount;
/* 364:443 */     this.nioBufferSize = nioBufferSize;
/* 365:    */     
/* 366:445 */     return nioBuffers;
/* 367:    */   }
/* 368:    */   
/* 369:    */   private static int fillBufferArray(ByteBuffer[] nioBufs, ByteBuffer[] nioBuffers, int nioBufferCount)
/* 370:    */   {
/* 371:449 */     for (ByteBuffer nioBuf : nioBufs)
/* 372:    */     {
/* 373:450 */       if (nioBuf == null) {
/* 374:    */         break;
/* 375:    */       }
/* 376:453 */       nioBuffers[(nioBufferCount++)] = nioBuf;
/* 377:    */     }
/* 378:455 */     return nioBufferCount;
/* 379:    */   }
/* 380:    */   
/* 381:    */   private static int fillBufferArrayNonDirect(Entry entry, ByteBuf buf, int readerIndex, int readableBytes, ByteBufAllocator alloc, ByteBuffer[] nioBuffers, int nioBufferCount)
/* 382:    */   {
/* 383:    */     ByteBuf directBuf;
/* 384:    */     ByteBuf directBuf;
/* 385:461 */     if (alloc.isDirectBufferPooled()) {
/* 386:462 */       directBuf = alloc.directBuffer(readableBytes);
/* 387:    */     } else {
/* 388:464 */       directBuf = ThreadLocalPooledByteBuf.newInstance();
/* 389:    */     }
/* 390:466 */     directBuf.writeBytes(buf, readerIndex, readableBytes);
/* 391:467 */     buf.release();
/* 392:468 */     entry.msg = directBuf;
/* 393:    */     
/* 394:470 */     ByteBuffer nioBuf = entry.buf = directBuf.internalNioBuffer(0, readableBytes);
/* 395:471 */     entry.count = 1;
/* 396:472 */     nioBuffers[(nioBufferCount++)] = nioBuf;
/* 397:473 */     return nioBufferCount;
/* 398:    */   }
/* 399:    */   
/* 400:    */   private static ByteBuffer[] expandNioBufferArray(ByteBuffer[] array, int neededSpace, int size)
/* 401:    */   {
/* 402:477 */     int newCapacity = array.length;
/* 403:    */     do
/* 404:    */     {
/* 405:481 */       newCapacity <<= 1;
/* 406:483 */       if (newCapacity < 0) {
/* 407:484 */         throw new IllegalStateException();
/* 408:    */       }
/* 409:487 */     } while (neededSpace > newCapacity);
/* 410:489 */     ByteBuffer[] newArray = new ByteBuffer[newCapacity];
/* 411:490 */     System.arraycopy(array, 0, newArray, 0, size);
/* 412:    */     
/* 413:492 */     return newArray;
/* 414:    */   }
/* 415:    */   
/* 416:    */   public int nioBufferCount()
/* 417:    */   {
/* 418:496 */     return this.nioBufferCount;
/* 419:    */   }
/* 420:    */   
/* 421:    */   public long nioBufferSize()
/* 422:    */   {
/* 423:500 */     return this.nioBufferSize;
/* 424:    */   }
/* 425:    */   
/* 426:    */   boolean getWritable()
/* 427:    */   {
/* 428:504 */     return this.writable != 0;
/* 429:    */   }
/* 430:    */   
/* 431:    */   public int size()
/* 432:    */   {
/* 433:508 */     return this.unflushed - this.flushed & this.buffer.length - 1;
/* 434:    */   }
/* 435:    */   
/* 436:    */   public boolean isEmpty()
/* 437:    */   {
/* 438:512 */     return this.unflushed == this.flushed;
/* 439:    */   }
/* 440:    */   
/* 441:    */   void failFlushed(Throwable cause)
/* 442:    */   {
/* 443:521 */     if (this.inFail) {
/* 444:522 */       return;
/* 445:    */     }
/* 446:    */     try
/* 447:    */     {
/* 448:526 */       this.inFail = true;
/* 449:    */       for (;;)
/* 450:    */       {
/* 451:528 */         if (!remove(cause)) {
/* 452:    */           break;
/* 453:    */         }
/* 454:    */       }
/* 455:    */     }
/* 456:    */     finally
/* 457:    */     {
/* 458:533 */       this.inFail = false;
/* 459:    */     }
/* 460:    */   }
/* 461:    */   
/* 462:    */   void close(final ClosedChannelException cause)
/* 463:    */   {
/* 464:538 */     if (this.inFail)
/* 465:    */     {
/* 466:539 */       this.channel.eventLoop().execute(new Runnable()
/* 467:    */       {
/* 468:    */         public void run()
/* 469:    */         {
/* 470:542 */           ChannelOutboundBuffer.this.close(cause);
/* 471:    */         }
/* 472:544 */       });
/* 473:545 */       return;
/* 474:    */     }
/* 475:548 */     this.inFail = true;
/* 476:550 */     if (this.channel.isOpen()) {
/* 477:551 */       throw new IllegalStateException("close() must be invoked after the channel is closed.");
/* 478:    */     }
/* 479:554 */     if (!isEmpty()) {
/* 480:555 */       throw new IllegalStateException("close() must be invoked after all flushed writes are handled.");
/* 481:    */     }
/* 482:559 */     int unflushedCount = this.tail - this.unflushed & this.buffer.length - 1;
/* 483:    */     try
/* 484:    */     {
/* 485:561 */       for (int i = 0; i < unflushedCount; i++)
/* 486:    */       {
/* 487:562 */         Entry e = this.buffer[(this.unflushed + i & this.buffer.length - 1)];
/* 488:    */         
/* 489:    */ 
/* 490:565 */         int size = e.pendingSize;
/* 491:566 */         TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, -size);
/* 492:    */         
/* 493:568 */         e.pendingSize = 0;
/* 494:569 */         if (!e.cancelled)
/* 495:    */         {
/* 496:570 */           safeRelease(e.msg);
/* 497:571 */           safeFail(e.promise, cause);
/* 498:    */         }
/* 499:573 */         e.msg = null;
/* 500:574 */         e.promise = null;
/* 501:    */       }
/* 502:    */     }
/* 503:    */     finally
/* 504:    */     {
/* 505:577 */       this.tail = this.unflushed;
/* 506:578 */       this.inFail = false;
/* 507:    */     }
/* 508:581 */     recycle();
/* 509:    */   }
/* 510:    */   
/* 511:    */   private static void safeRelease(Object message)
/* 512:    */   {
/* 513:    */     try
/* 514:    */     {
/* 515:586 */       ReferenceCountUtil.release(message);
/* 516:    */     }
/* 517:    */     catch (Throwable t)
/* 518:    */     {
/* 519:588 */       logger.warn("Failed to release a message.", t);
/* 520:    */     }
/* 521:    */   }
/* 522:    */   
/* 523:    */   private static void safeSuccess(ChannelPromise promise)
/* 524:    */   {
/* 525:593 */     if ((!(promise instanceof VoidChannelPromise)) && (!promise.trySuccess())) {
/* 526:594 */       logger.warn("Failed to mark a promise as success because it is done already: {}", promise);
/* 527:    */     }
/* 528:    */   }
/* 529:    */   
/* 530:    */   private static void safeFail(ChannelPromise promise, Throwable cause)
/* 531:    */   {
/* 532:599 */     if ((!(promise instanceof VoidChannelPromise)) && (!promise.tryFailure(cause))) {
/* 533:600 */       logger.warn("Failed to mark a promise as failure because it's done already: {}", promise, cause);
/* 534:    */     }
/* 535:    */   }
/* 536:    */   
/* 537:    */   public void recycle()
/* 538:    */   {
/* 539:605 */     if (this.buffer.length > 32)
/* 540:    */     {
/* 541:606 */       Entry[] e = new Entry[32];
/* 542:607 */       System.arraycopy(this.buffer, 0, e, 0, 32);
/* 543:608 */       this.buffer = e;
/* 544:    */     }
/* 545:611 */     if (this.nioBuffers.length > 32) {
/* 546:612 */       this.nioBuffers = new ByteBuffer[32];
/* 547:    */     } else {
/* 548:616 */       Arrays.fill(this.nioBuffers, null);
/* 549:    */     }
/* 550:621 */     this.flushed = 0;
/* 551:622 */     this.unflushed = 0;
/* 552:623 */     this.tail = 0;
/* 553:    */     
/* 554:    */ 
/* 555:626 */     this.channel = null;
/* 556:    */     
/* 557:628 */     RECYCLER.recycle(this, this.handle);
/* 558:    */   }
/* 559:    */   
/* 560:    */   public long totalPendingWriteBytes()
/* 561:    */   {
/* 562:632 */     return this.totalPendingSize;
/* 563:    */   }
/* 564:    */   
/* 565:    */   private static final class Entry
/* 566:    */   {
/* 567:    */     Object msg;
/* 568:    */     ByteBuffer[] buffers;
/* 569:    */     ByteBuffer buf;
/* 570:    */     ChannelPromise promise;
/* 571:    */     long progress;
/* 572:    */     long total;
/* 573:    */     int pendingSize;
/* 574:643 */     int count = -1;
/* 575:    */     boolean cancelled;
/* 576:    */     
/* 577:    */     public int cancel()
/* 578:    */     {
/* 579:647 */       if (!this.cancelled)
/* 580:    */       {
/* 581:648 */         this.cancelled = true;
/* 582:649 */         int pSize = this.pendingSize;
/* 583:    */         
/* 584:    */ 
/* 585:652 */         ChannelOutboundBuffer.safeRelease(this.msg);
/* 586:653 */         this.msg = Unpooled.EMPTY_BUFFER;
/* 587:    */         
/* 588:655 */         this.pendingSize = 0;
/* 589:656 */         this.total = 0L;
/* 590:657 */         this.progress = 0L;
/* 591:658 */         this.buffers = null;
/* 592:659 */         this.buf = null;
/* 593:660 */         return pSize;
/* 594:    */       }
/* 595:662 */       return 0;
/* 596:    */     }
/* 597:    */     
/* 598:    */     public void clear()
/* 599:    */     {
/* 600:666 */       this.buffers = null;
/* 601:667 */       this.buf = null;
/* 602:668 */       this.msg = null;
/* 603:669 */       this.promise = null;
/* 604:670 */       this.progress = 0L;
/* 605:671 */       this.total = 0L;
/* 606:672 */       this.pendingSize = 0;
/* 607:673 */       this.count = -1;
/* 608:674 */       this.cancelled = false;
/* 609:    */     }
/* 610:    */   }
/* 611:    */   
/* 612:    */   static final class ThreadLocalPooledByteBuf
/* 613:    */     extends UnpooledDirectByteBuf
/* 614:    */   {
/* 615:    */     private final Recycler.Handle handle;
/* 616:681 */     private static final Recycler<ThreadLocalPooledByteBuf> RECYCLER = new Recycler()
/* 617:    */     {
/* 618:    */       protected ChannelOutboundBuffer.ThreadLocalPooledByteBuf newObject(Recycler.Handle handle)
/* 619:    */       {
/* 620:684 */         return new ChannelOutboundBuffer.ThreadLocalPooledByteBuf(handle, null);
/* 621:    */       }
/* 622:    */     };
/* 623:    */     
/* 624:    */     private ThreadLocalPooledByteBuf(Recycler.Handle handle)
/* 625:    */     {
/* 626:689 */       super(256, 2147483647);
/* 627:690 */       this.handle = handle;
/* 628:    */     }
/* 629:    */     
/* 630:    */     static ThreadLocalPooledByteBuf newInstance()
/* 631:    */     {
/* 632:694 */       ThreadLocalPooledByteBuf buf = (ThreadLocalPooledByteBuf)RECYCLER.get();
/* 633:695 */       buf.setRefCnt(1);
/* 634:696 */       return buf;
/* 635:    */     }
/* 636:    */     
/* 637:    */     protected void deallocate()
/* 638:    */     {
/* 639:701 */       if (capacity() > ChannelOutboundBuffer.threadLocalDirectBufferSize)
/* 640:    */       {
/* 641:702 */         super.deallocate();
/* 642:    */       }
/* 643:    */       else
/* 644:    */       {
/* 645:704 */         clear();
/* 646:705 */         RECYCLER.recycle(this, this.handle);
/* 647:    */       }
/* 648:    */     }
/* 649:    */   }
/* 650:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.ChannelOutboundBuffer
 * JD-Core Version:    0.7.0.1
 */