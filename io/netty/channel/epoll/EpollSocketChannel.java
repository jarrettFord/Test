/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.ChannelConfig;
/*   7:    */ import io.netty.channel.ChannelFuture;
/*   8:    */ import io.netty.channel.ChannelFutureListener;
/*   9:    */ import io.netty.channel.ChannelOption;
/*  10:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  11:    */ import io.netty.channel.ChannelPipeline;
/*  12:    */ import io.netty.channel.ChannelPromise;
/*  13:    */ import io.netty.channel.ConnectTimeoutException;
/*  14:    */ import io.netty.channel.DefaultFileRegion;
/*  15:    */ import io.netty.channel.EventLoop;
/*  16:    */ import io.netty.channel.RecvByteBufAllocator;
/*  17:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  18:    */ import io.netty.channel.socket.ChannelInputShutdownEvent;
/*  19:    */ import io.netty.channel.socket.ServerSocketChannel;
/*  20:    */ import io.netty.channel.socket.SocketChannel;
/*  21:    */ import io.netty.util.internal.StringUtil;
/*  22:    */ import java.io.IOException;
/*  23:    */ import java.net.ConnectException;
/*  24:    */ import java.net.InetSocketAddress;
/*  25:    */ import java.net.SocketAddress;
/*  26:    */ import java.nio.ByteBuffer;
/*  27:    */ import java.util.concurrent.ScheduledFuture;
/*  28:    */ import java.util.concurrent.TimeUnit;
/*  29:    */ 
/*  30:    */ public final class EpollSocketChannel
/*  31:    */   extends AbstractEpollChannel
/*  32:    */   implements SocketChannel
/*  33:    */ {
/*  34:    */   private final EpollSocketChannelConfig config;
/*  35:    */   private ChannelPromise connectPromise;
/*  36:    */   private ScheduledFuture<?> connectTimeoutFuture;
/*  37:    */   private SocketAddress requestedRemoteAddress;
/*  38:    */   private volatile InetSocketAddress local;
/*  39:    */   private volatile InetSocketAddress remote;
/*  40:    */   private volatile boolean inputShutdown;
/*  41:    */   private volatile boolean outputShutdown;
/*  42:    */   
/*  43:    */   EpollSocketChannel(Channel parent, int fd)
/*  44:    */     throws IOException
/*  45:    */   {
/*  46: 67 */     super(parent, fd, 1, true);
/*  47: 68 */     this.config = new EpollSocketChannelConfig(this);
/*  48:    */     
/*  49:    */ 
/*  50: 71 */     this.remote = Native.remoteAddress(fd);
/*  51: 72 */     this.local = Native.localAddress(fd);
/*  52:    */   }
/*  53:    */   
/*  54:    */   public EpollSocketChannel()
/*  55:    */   {
/*  56: 76 */     super(Native.socketStreamFd(), 1);
/*  57: 77 */     this.config = new EpollSocketChannelConfig(this);
/*  58:    */   }
/*  59:    */   
/*  60:    */   protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe()
/*  61:    */   {
/*  62: 82 */     return new EpollSocketUnsafe();
/*  63:    */   }
/*  64:    */   
/*  65:    */   protected SocketAddress localAddress0()
/*  66:    */   {
/*  67: 87 */     return this.local;
/*  68:    */   }
/*  69:    */   
/*  70:    */   protected SocketAddress remoteAddress0()
/*  71:    */   {
/*  72: 92 */     return this.remote;
/*  73:    */   }
/*  74:    */   
/*  75:    */   protected void doBind(SocketAddress local)
/*  76:    */     throws Exception
/*  77:    */   {
/*  78: 97 */     InetSocketAddress localAddress = (InetSocketAddress)local;
/*  79: 98 */     Native.bind(this.fd, localAddress.getAddress(), localAddress.getPort());
/*  80: 99 */     this.local = Native.localAddress(this.fd);
/*  81:    */   }
/*  82:    */   
/*  83:    */   private int doWriteBytes(ByteBuf buf, int readable)
/*  84:    */     throws Exception
/*  85:    */   {
/*  86:108 */     int readerIndex = buf.readerIndex();
/*  87:    */     int localFlushedAmount;
/*  88:    */     int localFlushedAmount;
/*  89:110 */     if (buf.nioBufferCount() == 1)
/*  90:    */     {
/*  91:111 */       ByteBuffer nioBuf = buf.internalNioBuffer(readerIndex, readable);
/*  92:112 */       localFlushedAmount = Native.write(this.fd, nioBuf, nioBuf.position(), nioBuf.limit());
/*  93:    */     }
/*  94:    */     else
/*  95:    */     {
/*  96:115 */       ByteBuffer[] nioBufs = buf.nioBuffers();
/*  97:116 */       localFlushedAmount = (int)Native.writev(this.fd, nioBufs, 0, nioBufs.length);
/*  98:    */     }
/*  99:118 */     if (localFlushedAmount > 0) {
/* 100:119 */       buf.readerIndex(readerIndex + localFlushedAmount);
/* 101:    */     }
/* 102:121 */     return localFlushedAmount;
/* 103:    */   }
/* 104:    */   
/* 105:    */   private void writeBytesMultiple(ChannelOutboundBuffer in, int msgCount, ByteBuffer[] nioBuffers)
/* 106:    */     throws IOException
/* 107:    */   {
/* 108:127 */     int nioBufferCnt = in.nioBufferCount();
/* 109:128 */     long expectedWrittenBytes = in.nioBufferSize();
/* 110:    */     
/* 111:130 */     long localWrittenBytes = Native.writev(this.fd, nioBuffers, 0, nioBufferCnt);
/* 112:132 */     if (localWrittenBytes < expectedWrittenBytes)
/* 113:    */     {
/* 114:133 */       setEpollOut();
/* 115:137 */       for (int i = msgCount; i > 0; i--)
/* 116:    */       {
/* 117:138 */         ByteBuf buf = (ByteBuf)in.current();
/* 118:139 */         int readerIndex = buf.readerIndex();
/* 119:140 */         int readableBytes = buf.writerIndex() - readerIndex;
/* 120:142 */         if (readableBytes < localWrittenBytes)
/* 121:    */         {
/* 122:143 */           in.remove();
/* 123:144 */           localWrittenBytes -= readableBytes;
/* 124:    */         }
/* 125:    */         else
/* 126:    */         {
/* 127:145 */           if (readableBytes > localWrittenBytes)
/* 128:    */           {
/* 129:147 */             buf.readerIndex(readerIndex + (int)localWrittenBytes);
/* 130:148 */             in.progress(localWrittenBytes);
/* 131:149 */             break;
/* 132:    */           }
/* 133:151 */           in.remove();
/* 134:152 */           break;
/* 135:    */         }
/* 136:    */       }
/* 137:    */     }
/* 138:    */     else
/* 139:    */     {
/* 140:157 */       for (int i = msgCount; i > 0; i--) {
/* 141:158 */         in.remove();
/* 142:    */       }
/* 143:    */     }
/* 144:    */   }
/* 145:    */   
/* 146:    */   private long doWriteFileRegion(DefaultFileRegion region, long count)
/* 147:    */     throws Exception
/* 148:    */   {
/* 149:170 */     return Native.sendfile(this.fd, region, region.transfered(), count);
/* 150:    */   }
/* 151:    */   
/* 152:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 153:    */     throws Exception
/* 154:    */   {
/* 155:    */     for (;;)
/* 156:    */     {
/* 157:176 */       int msgCount = in.size();
/* 158:178 */       if (msgCount == 0)
/* 159:    */       {
/* 160:180 */         clearEpollOut();
/* 161:181 */         break;
/* 162:    */       }
/* 163:187 */       if (msgCount > 1)
/* 164:    */       {
/* 165:189 */         ByteBuffer[] nioBuffers = in.nioBuffers();
/* 166:190 */         if (nioBuffers != null)
/* 167:    */         {
/* 168:191 */           writeBytesMultiple(in, msgCount, nioBuffers);
/* 169:    */           
/* 170:    */ 
/* 171:    */ 
/* 172:    */ 
/* 173:196 */           continue;
/* 174:    */         }
/* 175:    */       }
/* 176:201 */       Object msg = in.current();
/* 177:202 */       if ((msg instanceof ByteBuf))
/* 178:    */       {
/* 179:203 */         ByteBuf buf = (ByteBuf)msg;
/* 180:204 */         int readableBytes = buf.readableBytes();
/* 181:205 */         if (readableBytes == 0)
/* 182:    */         {
/* 183:206 */           in.remove();
/* 184:    */         }
/* 185:    */         else
/* 186:    */         {
/* 187:210 */           int expected = buf.readableBytes();
/* 188:211 */           int localFlushedAmount = doWriteBytes(buf, expected);
/* 189:212 */           in.progress(localFlushedAmount);
/* 190:213 */           if (localFlushedAmount < expected)
/* 191:    */           {
/* 192:214 */             setEpollOut();
/* 193:215 */             break;
/* 194:    */           }
/* 195:217 */           if (!buf.isReadable()) {
/* 196:218 */             in.remove();
/* 197:    */           }
/* 198:    */         }
/* 199:    */       }
/* 200:221 */       else if ((msg instanceof DefaultFileRegion))
/* 201:    */       {
/* 202:222 */         DefaultFileRegion region = (DefaultFileRegion)msg;
/* 203:    */         
/* 204:224 */         long expected = region.count() - region.position();
/* 205:225 */         long localFlushedAmount = doWriteFileRegion(region, expected);
/* 206:226 */         in.progress(localFlushedAmount);
/* 207:228 */         if (localFlushedAmount < expected)
/* 208:    */         {
/* 209:229 */           setEpollOut();
/* 210:230 */           break;
/* 211:    */         }
/* 212:233 */         if (region.transfered() >= region.count()) {
/* 213:234 */           in.remove();
/* 214:    */         }
/* 215:    */       }
/* 216:    */       else
/* 217:    */       {
/* 218:237 */         throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg));
/* 219:    */       }
/* 220:    */     }
/* 221:    */   }
/* 222:    */   
/* 223:    */   public EpollSocketChannelConfig config()
/* 224:    */   {
/* 225:244 */     return this.config;
/* 226:    */   }
/* 227:    */   
/* 228:    */   public boolean isInputShutdown()
/* 229:    */   {
/* 230:249 */     return this.inputShutdown;
/* 231:    */   }
/* 232:    */   
/* 233:    */   public boolean isOutputShutdown()
/* 234:    */   {
/* 235:254 */     return (this.outputShutdown) || (!isActive());
/* 236:    */   }
/* 237:    */   
/* 238:    */   public ChannelFuture shutdownOutput()
/* 239:    */   {
/* 240:259 */     return shutdownOutput(newPromise());
/* 241:    */   }
/* 242:    */   
/* 243:    */   public ChannelFuture shutdownOutput(final ChannelPromise promise)
/* 244:    */   {
/* 245:264 */     EventLoop loop = eventLoop();
/* 246:265 */     if (loop.inEventLoop()) {
/* 247:    */       try
/* 248:    */       {
/* 249:267 */         Native.shutdown(this.fd, false, true);
/* 250:268 */         this.outputShutdown = true;
/* 251:269 */         promise.setSuccess();
/* 252:    */       }
/* 253:    */       catch (Throwable t)
/* 254:    */       {
/* 255:271 */         promise.setFailure(t);
/* 256:    */       }
/* 257:    */     } else {
/* 258:274 */       loop.execute(new Runnable()
/* 259:    */       {
/* 260:    */         public void run()
/* 261:    */         {
/* 262:277 */           EpollSocketChannel.this.shutdownOutput(promise);
/* 263:    */         }
/* 264:    */       });
/* 265:    */     }
/* 266:281 */     return promise;
/* 267:    */   }
/* 268:    */   
/* 269:    */   public ServerSocketChannel parent()
/* 270:    */   {
/* 271:286 */     return (ServerSocketChannel)super.parent();
/* 272:    */   }
/* 273:    */   
/* 274:    */   final class EpollSocketUnsafe
/* 275:    */     extends AbstractEpollChannel.AbstractEpollUnsafe
/* 276:    */   {
/* 277:    */     private RecvByteBufAllocator.Handle allocHandle;
/* 278:    */     
/* 279:    */     EpollSocketUnsafe()
/* 280:    */     {
/* 281:289 */       super();
/* 282:    */     }
/* 283:    */     
/* 284:    */     public void write(Object msg, ChannelPromise promise)
/* 285:    */     {
/* 286:294 */       if ((msg instanceof ByteBuf))
/* 287:    */       {
/* 288:295 */         ByteBuf buf = (ByteBuf)msg;
/* 289:296 */         if (!buf.isDirect())
/* 290:    */         {
/* 291:299 */           int readable = buf.readableBytes();
/* 292:300 */           ByteBuf dst = EpollSocketChannel.this.alloc().directBuffer(readable);
/* 293:301 */           dst.writeBytes(buf, buf.readerIndex(), readable);
/* 294:    */           
/* 295:303 */           buf.release();
/* 296:304 */           msg = dst;
/* 297:    */         }
/* 298:    */       }
/* 299:307 */       super.write(msg, promise);
/* 300:    */     }
/* 301:    */     
/* 302:    */     private void closeOnRead(ChannelPipeline pipeline)
/* 303:    */     {
/* 304:311 */       EpollSocketChannel.this.inputShutdown = true;
/* 305:312 */       if (EpollSocketChannel.this.isOpen()) {
/* 306:313 */         if (Boolean.TRUE.equals(EpollSocketChannel.this.config().getOption(ChannelOption.ALLOW_HALF_CLOSURE)))
/* 307:    */         {
/* 308:314 */           clearEpollIn0();
/* 309:315 */           pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
/* 310:    */         }
/* 311:    */         else
/* 312:    */         {
/* 313:317 */           close(voidPromise());
/* 314:    */         }
/* 315:    */       }
/* 316:    */     }
/* 317:    */     
/* 318:    */     private boolean handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause, boolean close)
/* 319:    */     {
/* 320:323 */       if (byteBuf != null) {
/* 321:324 */         if (byteBuf.isReadable())
/* 322:    */         {
/* 323:325 */           this.readPending = false;
/* 324:326 */           pipeline.fireChannelRead(byteBuf);
/* 325:    */         }
/* 326:    */         else
/* 327:    */         {
/* 328:328 */           byteBuf.release();
/* 329:    */         }
/* 330:    */       }
/* 331:331 */       pipeline.fireChannelReadComplete();
/* 332:332 */       pipeline.fireExceptionCaught(cause);
/* 333:333 */       if ((close) || ((cause instanceof IOException)))
/* 334:    */       {
/* 335:334 */         closeOnRead(pipeline);
/* 336:335 */         return true;
/* 337:    */       }
/* 338:337 */       return false;
/* 339:    */     }
/* 340:    */     
/* 341:    */     public void connect(final SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 342:    */     {
/* 343:343 */       if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/* 344:344 */         return;
/* 345:    */       }
/* 346:    */       try
/* 347:    */       {
/* 348:348 */         if (EpollSocketChannel.this.connectPromise != null) {
/* 349:349 */           throw new IllegalStateException("connection attempt already made");
/* 350:    */         }
/* 351:352 */         boolean wasActive = EpollSocketChannel.this.isActive();
/* 352:353 */         if (doConnect((InetSocketAddress)remoteAddress, (InetSocketAddress)localAddress))
/* 353:    */         {
/* 354:354 */           fulfillConnectPromise(promise, wasActive);
/* 355:    */         }
/* 356:    */         else
/* 357:    */         {
/* 358:356 */           EpollSocketChannel.this.connectPromise = promise;
/* 359:357 */           EpollSocketChannel.this.requestedRemoteAddress = remoteAddress;
/* 360:    */           
/* 361:    */ 
/* 362:360 */           int connectTimeoutMillis = EpollSocketChannel.this.config().getConnectTimeoutMillis();
/* 363:361 */           if (connectTimeoutMillis > 0) {
/* 364:362 */             EpollSocketChannel.this.connectTimeoutFuture = EpollSocketChannel.this.eventLoop().schedule(new Runnable()
/* 365:    */             {
/* 366:    */               public void run()
/* 367:    */               {
/* 368:365 */                 ChannelPromise connectPromise = EpollSocketChannel.this.connectPromise;
/* 369:366 */                 ConnectTimeoutException cause = new ConnectTimeoutException("connection timed out: " + remoteAddress);
/* 370:368 */                 if ((connectPromise != null) && (connectPromise.tryFailure(cause))) {
/* 371:369 */                   EpollSocketChannel.EpollSocketUnsafe.this.close(EpollSocketChannel.EpollSocketUnsafe.this.voidPromise());
/* 372:    */                 }
/* 373:    */               }
/* 374:369 */             }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
/* 375:    */           }
/* 376:375 */           promise.addListener(new ChannelFutureListener()
/* 377:    */           {
/* 378:    */             public void operationComplete(ChannelFuture future)
/* 379:    */               throws Exception
/* 380:    */             {
/* 381:378 */               if (future.isCancelled())
/* 382:    */               {
/* 383:379 */                 if (EpollSocketChannel.this.connectTimeoutFuture != null) {
/* 384:380 */                   EpollSocketChannel.this.connectTimeoutFuture.cancel(false);
/* 385:    */                 }
/* 386:382 */                 EpollSocketChannel.this.connectPromise = null;
/* 387:383 */                 EpollSocketChannel.EpollSocketUnsafe.this.close(EpollSocketChannel.EpollSocketUnsafe.this.voidPromise());
/* 388:    */               }
/* 389:    */             }
/* 390:    */           });
/* 391:    */         }
/* 392:    */       }
/* 393:    */       catch (Throwable t)
/* 394:    */       {
/* 395:389 */         if ((t instanceof ConnectException))
/* 396:    */         {
/* 397:390 */           Throwable newT = new ConnectException(t.getMessage() + ": " + remoteAddress);
/* 398:391 */           newT.setStackTrace(t.getStackTrace());
/* 399:392 */           t = newT;
/* 400:    */         }
/* 401:394 */         closeIfClosed();
/* 402:395 */         promise.tryFailure(t);
/* 403:    */       }
/* 404:    */     }
/* 405:    */     
/* 406:    */     private void fulfillConnectPromise(ChannelPromise promise, boolean wasActive)
/* 407:    */     {
/* 408:400 */       if (promise == null) {
/* 409:402 */         return;
/* 410:    */       }
/* 411:404 */       EpollSocketChannel.this.active = true;
/* 412:    */       
/* 413:    */ 
/* 414:407 */       boolean promiseSet = promise.trySuccess();
/* 415:411 */       if ((!wasActive) && (EpollSocketChannel.this.isActive())) {
/* 416:412 */         EpollSocketChannel.this.pipeline().fireChannelActive();
/* 417:    */       }
/* 418:416 */       if (!promiseSet) {
/* 419:417 */         close(voidPromise());
/* 420:    */       }
/* 421:    */     }
/* 422:    */     
/* 423:    */     private void fulfillConnectPromise(ChannelPromise promise, Throwable cause)
/* 424:    */     {
/* 425:422 */       if (promise == null) {}
/* 426:427 */       promise.tryFailure(cause);
/* 427:428 */       closeIfClosed();
/* 428:    */     }
/* 429:    */     
/* 430:    */     private void finishConnect()
/* 431:    */     {
/* 432:435 */       assert (EpollSocketChannel.this.eventLoop().inEventLoop());
/* 433:    */       
/* 434:437 */       boolean connectStillInProgress = false;
/* 435:    */       try
/* 436:    */       {
/* 437:439 */         boolean wasActive = EpollSocketChannel.this.isActive();
/* 438:440 */         if (!doFinishConnect()) {
/* 439:441 */           connectStillInProgress = true;
/* 440:    */         } else {
/* 441:444 */           fulfillConnectPromise(EpollSocketChannel.this.connectPromise, wasActive);
/* 442:    */         }
/* 443:    */       }
/* 444:    */       catch (Throwable t)
/* 445:    */       {
/* 446:446 */         if ((t instanceof ConnectException))
/* 447:    */         {
/* 448:447 */           Throwable newT = new ConnectException(t.getMessage() + ": " + EpollSocketChannel.this.requestedRemoteAddress);
/* 449:448 */           newT.setStackTrace(t.getStackTrace());
/* 450:449 */           t = newT;
/* 451:    */         }
/* 452:452 */         fulfillConnectPromise(EpollSocketChannel.this.connectPromise, t);
/* 453:    */       }
/* 454:    */       finally
/* 455:    */       {
/* 456:454 */         if (!connectStillInProgress)
/* 457:    */         {
/* 458:457 */           if (EpollSocketChannel.this.connectTimeoutFuture != null) {
/* 459:458 */             EpollSocketChannel.this.connectTimeoutFuture.cancel(false);
/* 460:    */           }
/* 461:460 */           EpollSocketChannel.this.connectPromise = null;
/* 462:    */         }
/* 463:    */       }
/* 464:    */     }
/* 465:    */     
/* 466:    */     void epollOutReady()
/* 467:    */     {
/* 468:467 */       if (EpollSocketChannel.this.connectPromise != null) {
/* 469:469 */         finishConnect();
/* 470:    */       } else {
/* 471:471 */         super.epollOutReady();
/* 472:    */       }
/* 473:    */     }
/* 474:    */     
/* 475:    */     private boolean doConnect(InetSocketAddress remoteAddress, InetSocketAddress localAddress)
/* 476:    */       throws Exception
/* 477:    */     {
/* 478:479 */       if (localAddress != null)
/* 479:    */       {
/* 480:480 */         AbstractEpollChannel.checkResolvable(localAddress);
/* 481:481 */         Native.bind(EpollSocketChannel.this.fd, localAddress.getAddress(), localAddress.getPort());
/* 482:    */       }
/* 483:484 */       boolean success = false;
/* 484:    */       try
/* 485:    */       {
/* 486:486 */         AbstractEpollChannel.checkResolvable(remoteAddress);
/* 487:487 */         boolean connected = Native.connect(EpollSocketChannel.this.fd, remoteAddress.getAddress(), remoteAddress.getPort());
/* 488:    */         
/* 489:489 */         EpollSocketChannel.this.remote = remoteAddress;
/* 490:490 */         EpollSocketChannel.this.local = Native.localAddress(EpollSocketChannel.this.fd);
/* 491:491 */         if (!connected) {
/* 492:492 */           EpollSocketChannel.this.setEpollOut();
/* 493:    */         }
/* 494:494 */         success = true;
/* 495:495 */         return connected;
/* 496:    */       }
/* 497:    */       finally
/* 498:    */       {
/* 499:497 */         if (!success) {
/* 500:498 */           EpollSocketChannel.this.doClose();
/* 501:    */         }
/* 502:    */       }
/* 503:    */     }
/* 504:    */     
/* 505:    */     private boolean doFinishConnect()
/* 506:    */       throws Exception
/* 507:    */     {
/* 508:507 */       if (Native.finishConnect(EpollSocketChannel.this.fd))
/* 509:    */       {
/* 510:508 */         EpollSocketChannel.this.clearEpollOut();
/* 511:509 */         return true;
/* 512:    */       }
/* 513:511 */       EpollSocketChannel.this.setEpollOut();
/* 514:512 */       return false;
/* 515:    */     }
/* 516:    */     
/* 517:    */     private int doReadBytes(ByteBuf byteBuf)
/* 518:    */       throws Exception
/* 519:    */     {
/* 520:520 */       int writerIndex = byteBuf.writerIndex();
/* 521:    */       int localReadAmount;
/* 522:    */       int localReadAmount;
/* 523:522 */       if (byteBuf.hasMemoryAddress())
/* 524:    */       {
/* 525:523 */         localReadAmount = Native.readAddress(EpollSocketChannel.this.fd, byteBuf.memoryAddress(), writerIndex, byteBuf.capacity());
/* 526:    */       }
/* 527:    */       else
/* 528:    */       {
/* 529:525 */         ByteBuffer buf = byteBuf.internalNioBuffer(writerIndex, byteBuf.writableBytes());
/* 530:526 */         localReadAmount = Native.read(EpollSocketChannel.this.fd, buf, buf.position(), buf.limit());
/* 531:    */       }
/* 532:528 */       if (localReadAmount > 0) {
/* 533:529 */         byteBuf.writerIndex(writerIndex + localReadAmount);
/* 534:    */       }
/* 535:531 */       return localReadAmount;
/* 536:    */     }
/* 537:    */     
/* 538:    */     void epollRdHupReady()
/* 539:    */     {
/* 540:536 */       if (EpollSocketChannel.this.isActive()) {
/* 541:537 */         epollInReady();
/* 542:    */       } else {
/* 543:539 */         closeOnRead(EpollSocketChannel.this.pipeline());
/* 544:    */       }
/* 545:    */     }
/* 546:    */     
/* 547:    */     void epollInReady()
/* 548:    */     {
/* 549:545 */       ChannelConfig config = EpollSocketChannel.this.config();
/* 550:546 */       ChannelPipeline pipeline = EpollSocketChannel.this.pipeline();
/* 551:547 */       ByteBufAllocator allocator = config.getAllocator();
/* 552:548 */       RecvByteBufAllocator.Handle allocHandle = this.allocHandle;
/* 553:549 */       if (allocHandle == null) {
/* 554:550 */         this.allocHandle = (allocHandle = config.getRecvByteBufAllocator().newHandle());
/* 555:    */       }
/* 556:553 */       ByteBuf byteBuf = null;
/* 557:554 */       boolean close = false;
/* 558:    */       try
/* 559:    */       {
/* 560:556 */         int totalReadAmount = 0;
/* 561:    */         for (;;)
/* 562:    */         {
/* 563:560 */           byteBuf = allocHandle.allocate(allocator);
/* 564:561 */           int writable = byteBuf.writableBytes();
/* 565:562 */           int localReadAmount = doReadBytes(byteBuf);
/* 566:563 */           if (localReadAmount <= 0)
/* 567:    */           {
/* 568:565 */             byteBuf.release();
/* 569:566 */             close = localReadAmount < 0;
/* 570:    */           }
/* 571:    */           else
/* 572:    */           {
/* 573:569 */             this.readPending = false;
/* 574:570 */             pipeline.fireChannelRead(byteBuf);
/* 575:571 */             byteBuf = null;
/* 576:573 */             if (totalReadAmount >= 2147483647 - localReadAmount)
/* 577:    */             {
/* 578:574 */               allocHandle.record(totalReadAmount);
/* 579:    */               
/* 580:    */ 
/* 581:577 */               totalReadAmount = localReadAmount;
/* 582:    */             }
/* 583:    */             else
/* 584:    */             {
/* 585:579 */               totalReadAmount += localReadAmount;
/* 586:    */             }
/* 587:582 */             if (localReadAmount < writable) {
/* 588:    */               break;
/* 589:    */             }
/* 590:    */           }
/* 591:    */         }
/* 592:588 */         pipeline.fireChannelReadComplete();
/* 593:589 */         allocHandle.record(totalReadAmount);
/* 594:591 */         if (close)
/* 595:    */         {
/* 596:592 */           closeOnRead(pipeline);
/* 597:593 */           close = false;
/* 598:    */         }
/* 599:    */       }
/* 600:    */       catch (Throwable t)
/* 601:    */       {
/* 602:596 */         boolean closed = handleReadException(pipeline, byteBuf, t, close);
/* 603:597 */         if (!closed) {
/* 604:600 */           EpollSocketChannel.this.eventLoop().execute(new Runnable()
/* 605:    */           {
/* 606:    */             public void run()
/* 607:    */             {
/* 608:603 */               EpollSocketChannel.EpollSocketUnsafe.this.epollInReady();
/* 609:    */             }
/* 610:    */           });
/* 611:    */         }
/* 612:    */       }
/* 613:    */       finally
/* 614:    */       {
/* 615:614 */         if ((!config.isAutoRead()) && (!this.readPending)) {
/* 616:615 */           clearEpollIn0();
/* 617:    */         }
/* 618:    */       }
/* 619:    */     }
/* 620:    */   }
/* 621:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.epoll.EpollSocketChannel
 * JD-Core Version:    0.7.0.1
 */