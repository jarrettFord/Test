/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.util.DefaultAttributeMap;
/*   5:    */ import io.netty.util.ReferenceCountUtil;
/*   6:    */ import io.netty.util.internal.EmptyArrays;
/*   7:    */ import io.netty.util.internal.OneTimeTask;
/*   8:    */ import io.netty.util.internal.PlatformDependent;
/*   9:    */ import io.netty.util.internal.ThreadLocalRandom;
/*  10:    */ import io.netty.util.internal.logging.InternalLogger;
/*  11:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  12:    */ import java.io.EOFException;
/*  13:    */ import java.io.IOException;
/*  14:    */ import java.net.InetAddress;
/*  15:    */ import java.net.InetSocketAddress;
/*  16:    */ import java.net.SocketAddress;
/*  17:    */ import java.nio.channels.ClosedChannelException;
/*  18:    */ import java.nio.channels.NotYetConnectedException;
/*  19:    */ import java.util.concurrent.RejectedExecutionException;
/*  20:    */ 
/*  21:    */ public abstract class AbstractChannel
/*  22:    */   extends DefaultAttributeMap
/*  23:    */   implements Channel
/*  24:    */ {
/*  25: 41 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractChannel.class);
/*  26: 43 */   static final ClosedChannelException CLOSED_CHANNEL_EXCEPTION = new ClosedChannelException();
/*  27: 44 */   static final NotYetConnectedException NOT_YET_CONNECTED_EXCEPTION = new NotYetConnectedException();
/*  28:    */   private MessageSizeEstimator.Handle estimatorHandle;
/*  29:    */   private final Channel parent;
/*  30:    */   
/*  31:    */   static
/*  32:    */   {
/*  33: 47 */     CLOSED_CHANNEL_EXCEPTION.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
/*  34: 48 */     NOT_YET_CONNECTED_EXCEPTION.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
/*  35:    */   }
/*  36:    */   
/*  37: 54 */   private final long hashCode = ThreadLocalRandom.current().nextLong();
/*  38:    */   private final Channel.Unsafe unsafe;
/*  39:    */   private final DefaultChannelPipeline pipeline;
/*  40: 57 */   private final ChannelFuture succeededFuture = new SucceededChannelFuture(this, null);
/*  41: 58 */   private final VoidChannelPromise voidPromise = new VoidChannelPromise(this, true);
/*  42: 59 */   private final VoidChannelPromise unsafeVoidPromise = new VoidChannelPromise(this, false);
/*  43: 60 */   private final CloseFuture closeFuture = new CloseFuture(this);
/*  44:    */   private volatile SocketAddress localAddress;
/*  45:    */   private volatile SocketAddress remoteAddress;
/*  46:    */   private volatile EventLoop eventLoop;
/*  47:    */   private volatile boolean registered;
/*  48:    */   private boolean strValActive;
/*  49:    */   private String strVal;
/*  50:    */   
/*  51:    */   protected AbstractChannel(Channel parent)
/*  52:    */   {
/*  53: 78 */     this.parent = parent;
/*  54: 79 */     this.unsafe = newUnsafe();
/*  55: 80 */     this.pipeline = new DefaultChannelPipeline(this);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public boolean isWritable()
/*  59:    */   {
/*  60: 85 */     ChannelOutboundBuffer buf = this.unsafe.outboundBuffer();
/*  61: 86 */     return (buf != null) && (buf.getWritable());
/*  62:    */   }
/*  63:    */   
/*  64:    */   public Channel parent()
/*  65:    */   {
/*  66: 91 */     return this.parent;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public ChannelPipeline pipeline()
/*  70:    */   {
/*  71: 96 */     return this.pipeline;
/*  72:    */   }
/*  73:    */   
/*  74:    */   public ByteBufAllocator alloc()
/*  75:    */   {
/*  76:101 */     return config().getAllocator();
/*  77:    */   }
/*  78:    */   
/*  79:    */   public EventLoop eventLoop()
/*  80:    */   {
/*  81:106 */     EventLoop eventLoop = this.eventLoop;
/*  82:107 */     if (eventLoop == null) {
/*  83:108 */       throw new IllegalStateException("channel not registered to an event loop");
/*  84:    */     }
/*  85:110 */     return eventLoop;
/*  86:    */   }
/*  87:    */   
/*  88:    */   public SocketAddress localAddress()
/*  89:    */   {
/*  90:115 */     SocketAddress localAddress = this.localAddress;
/*  91:116 */     if (localAddress == null) {
/*  92:    */       try
/*  93:    */       {
/*  94:118 */         this.localAddress = (localAddress = unsafe().localAddress());
/*  95:    */       }
/*  96:    */       catch (Throwable t)
/*  97:    */       {
/*  98:121 */         return null;
/*  99:    */       }
/* 100:    */     }
/* 101:124 */     return localAddress;
/* 102:    */   }
/* 103:    */   
/* 104:    */   protected void invalidateLocalAddress()
/* 105:    */   {
/* 106:128 */     this.localAddress = null;
/* 107:    */   }
/* 108:    */   
/* 109:    */   public SocketAddress remoteAddress()
/* 110:    */   {
/* 111:133 */     SocketAddress remoteAddress = this.remoteAddress;
/* 112:134 */     if (remoteAddress == null) {
/* 113:    */       try
/* 114:    */       {
/* 115:136 */         this.remoteAddress = (remoteAddress = unsafe().remoteAddress());
/* 116:    */       }
/* 117:    */       catch (Throwable t)
/* 118:    */       {
/* 119:139 */         return null;
/* 120:    */       }
/* 121:    */     }
/* 122:142 */     return remoteAddress;
/* 123:    */   }
/* 124:    */   
/* 125:    */   protected void invalidateRemoteAddress()
/* 126:    */   {
/* 127:149 */     this.remoteAddress = null;
/* 128:    */   }
/* 129:    */   
/* 130:    */   public boolean isRegistered()
/* 131:    */   {
/* 132:154 */     return this.registered;
/* 133:    */   }
/* 134:    */   
/* 135:    */   public ChannelFuture bind(SocketAddress localAddress)
/* 136:    */   {
/* 137:159 */     return this.pipeline.bind(localAddress);
/* 138:    */   }
/* 139:    */   
/* 140:    */   public ChannelFuture connect(SocketAddress remoteAddress)
/* 141:    */   {
/* 142:164 */     return this.pipeline.connect(remoteAddress);
/* 143:    */   }
/* 144:    */   
/* 145:    */   public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 146:    */   {
/* 147:169 */     return this.pipeline.connect(remoteAddress, localAddress);
/* 148:    */   }
/* 149:    */   
/* 150:    */   public ChannelFuture disconnect()
/* 151:    */   {
/* 152:174 */     return this.pipeline.disconnect();
/* 153:    */   }
/* 154:    */   
/* 155:    */   public ChannelFuture close()
/* 156:    */   {
/* 157:179 */     return this.pipeline.close();
/* 158:    */   }
/* 159:    */   
/* 160:    */   public ChannelFuture deregister()
/* 161:    */   {
/* 162:184 */     return this.pipeline.deregister();
/* 163:    */   }
/* 164:    */   
/* 165:    */   public Channel flush()
/* 166:    */   {
/* 167:189 */     this.pipeline.flush();
/* 168:190 */     return this;
/* 169:    */   }
/* 170:    */   
/* 171:    */   public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise)
/* 172:    */   {
/* 173:195 */     return this.pipeline.bind(localAddress, promise);
/* 174:    */   }
/* 175:    */   
/* 176:    */   public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise)
/* 177:    */   {
/* 178:200 */     return this.pipeline.connect(remoteAddress, promise);
/* 179:    */   }
/* 180:    */   
/* 181:    */   public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 182:    */   {
/* 183:205 */     return this.pipeline.connect(remoteAddress, localAddress, promise);
/* 184:    */   }
/* 185:    */   
/* 186:    */   public ChannelFuture disconnect(ChannelPromise promise)
/* 187:    */   {
/* 188:210 */     return this.pipeline.disconnect(promise);
/* 189:    */   }
/* 190:    */   
/* 191:    */   public ChannelFuture close(ChannelPromise promise)
/* 192:    */   {
/* 193:215 */     return this.pipeline.close(promise);
/* 194:    */   }
/* 195:    */   
/* 196:    */   public ChannelFuture deregister(ChannelPromise promise)
/* 197:    */   {
/* 198:220 */     return this.pipeline.deregister(promise);
/* 199:    */   }
/* 200:    */   
/* 201:    */   public Channel read()
/* 202:    */   {
/* 203:225 */     this.pipeline.read();
/* 204:226 */     return this;
/* 205:    */   }
/* 206:    */   
/* 207:    */   public ChannelFuture write(Object msg)
/* 208:    */   {
/* 209:231 */     return this.pipeline.write(msg);
/* 210:    */   }
/* 211:    */   
/* 212:    */   public ChannelFuture write(Object msg, ChannelPromise promise)
/* 213:    */   {
/* 214:236 */     return this.pipeline.write(msg, promise);
/* 215:    */   }
/* 216:    */   
/* 217:    */   public ChannelFuture writeAndFlush(Object msg)
/* 218:    */   {
/* 219:241 */     return this.pipeline.writeAndFlush(msg);
/* 220:    */   }
/* 221:    */   
/* 222:    */   public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise)
/* 223:    */   {
/* 224:246 */     return this.pipeline.writeAndFlush(msg, promise);
/* 225:    */   }
/* 226:    */   
/* 227:    */   public ChannelPromise newPromise()
/* 228:    */   {
/* 229:251 */     return new DefaultChannelPromise(this);
/* 230:    */   }
/* 231:    */   
/* 232:    */   public ChannelProgressivePromise newProgressivePromise()
/* 233:    */   {
/* 234:256 */     return new DefaultChannelProgressivePromise(this);
/* 235:    */   }
/* 236:    */   
/* 237:    */   public ChannelFuture newSucceededFuture()
/* 238:    */   {
/* 239:261 */     return this.succeededFuture;
/* 240:    */   }
/* 241:    */   
/* 242:    */   public ChannelFuture newFailedFuture(Throwable cause)
/* 243:    */   {
/* 244:266 */     return new FailedChannelFuture(this, null, cause);
/* 245:    */   }
/* 246:    */   
/* 247:    */   public ChannelFuture closeFuture()
/* 248:    */   {
/* 249:271 */     return this.closeFuture;
/* 250:    */   }
/* 251:    */   
/* 252:    */   public Channel.Unsafe unsafe()
/* 253:    */   {
/* 254:276 */     return this.unsafe;
/* 255:    */   }
/* 256:    */   
/* 257:    */   public final int hashCode()
/* 258:    */   {
/* 259:289 */     return (int)this.hashCode;
/* 260:    */   }
/* 261:    */   
/* 262:    */   public final boolean equals(Object o)
/* 263:    */   {
/* 264:298 */     return this == o;
/* 265:    */   }
/* 266:    */   
/* 267:    */   public final int compareTo(Channel o)
/* 268:    */   {
/* 269:303 */     if (this == o) {
/* 270:304 */       return 0;
/* 271:    */     }
/* 272:307 */     long ret = this.hashCode - o.hashCode();
/* 273:308 */     if (ret > 0L) {
/* 274:309 */       return 1;
/* 275:    */     }
/* 276:311 */     if (ret < 0L) {
/* 277:312 */       return -1;
/* 278:    */     }
/* 279:315 */     ret = System.identityHashCode(this) - System.identityHashCode(o);
/* 280:316 */     if (ret != 0L) {
/* 281:317 */       return (int)ret;
/* 282:    */     }
/* 283:321 */     throw new Error();
/* 284:    */   }
/* 285:    */   
/* 286:    */   public String toString()
/* 287:    */   {
/* 288:332 */     boolean active = isActive();
/* 289:333 */     if ((this.strValActive == active) && (this.strVal != null)) {
/* 290:334 */       return this.strVal;
/* 291:    */     }
/* 292:337 */     SocketAddress remoteAddr = remoteAddress();
/* 293:338 */     SocketAddress localAddr = localAddress();
/* 294:339 */     if (remoteAddr != null)
/* 295:    */     {
/* 296:    */       SocketAddress dstAddr;
/* 297:    */       SocketAddress srcAddr;
/* 298:    */       SocketAddress dstAddr;
/* 299:342 */       if (this.parent == null)
/* 300:    */       {
/* 301:343 */         SocketAddress srcAddr = localAddr;
/* 302:344 */         dstAddr = remoteAddr;
/* 303:    */       }
/* 304:    */       else
/* 305:    */       {
/* 306:346 */         srcAddr = remoteAddr;
/* 307:347 */         dstAddr = localAddr;
/* 308:    */       }
/* 309:349 */       this.strVal = String.format("[id: 0x%08x, %s %s %s]", new Object[] { Integer.valueOf((int)this.hashCode), srcAddr, active ? "=>" : ":>", dstAddr });
/* 310:    */     }
/* 311:350 */     else if (localAddr != null)
/* 312:    */     {
/* 313:351 */       this.strVal = String.format("[id: 0x%08x, %s]", new Object[] { Integer.valueOf((int)this.hashCode), localAddr });
/* 314:    */     }
/* 315:    */     else
/* 316:    */     {
/* 317:353 */       this.strVal = String.format("[id: 0x%08x]", new Object[] { Integer.valueOf((int)this.hashCode) });
/* 318:    */     }
/* 319:356 */     this.strValActive = active;
/* 320:357 */     return this.strVal;
/* 321:    */   }
/* 322:    */   
/* 323:    */   public final ChannelPromise voidPromise()
/* 324:    */   {
/* 325:362 */     return this.voidPromise;
/* 326:    */   }
/* 327:    */   
/* 328:    */   final MessageSizeEstimator.Handle estimatorHandle()
/* 329:    */   {
/* 330:366 */     if (this.estimatorHandle == null) {
/* 331:367 */       this.estimatorHandle = config().getMessageSizeEstimator().newHandle();
/* 332:    */     }
/* 333:369 */     return this.estimatorHandle;
/* 334:    */   }
/* 335:    */   
/* 336:    */   protected abstract class AbstractUnsafe
/* 337:    */     implements Channel.Unsafe
/* 338:    */   {
/* 339:377 */     private ChannelOutboundBuffer outboundBuffer = ChannelOutboundBuffer.newInstance(AbstractChannel.this);
/* 340:    */     private boolean inFlush0;
/* 341:    */     
/* 342:    */     protected AbstractUnsafe() {}
/* 343:    */     
/* 344:    */     public final ChannelOutboundBuffer outboundBuffer()
/* 345:    */     {
/* 346:382 */       return this.outboundBuffer;
/* 347:    */     }
/* 348:    */     
/* 349:    */     public final SocketAddress localAddress()
/* 350:    */     {
/* 351:387 */       return AbstractChannel.this.localAddress0();
/* 352:    */     }
/* 353:    */     
/* 354:    */     public final SocketAddress remoteAddress()
/* 355:    */     {
/* 356:392 */       return AbstractChannel.this.remoteAddress0();
/* 357:    */     }
/* 358:    */     
/* 359:    */     public final void register(EventLoop eventLoop, final ChannelPromise promise)
/* 360:    */     {
/* 361:397 */       if (eventLoop == null) {
/* 362:398 */         throw new NullPointerException("eventLoop");
/* 363:    */       }
/* 364:400 */       if (AbstractChannel.this.isRegistered())
/* 365:    */       {
/* 366:401 */         promise.setFailure(new IllegalStateException("registered to an event loop already"));
/* 367:402 */         return;
/* 368:    */       }
/* 369:404 */       if (!AbstractChannel.this.isCompatible(eventLoop))
/* 370:    */       {
/* 371:405 */         promise.setFailure(new IllegalStateException("incompatible event loop type: " + eventLoop.getClass().getName()));
/* 372:    */         
/* 373:407 */         return;
/* 374:    */       }
/* 375:410 */       AbstractChannel.this.eventLoop = eventLoop;
/* 376:412 */       if (eventLoop.inEventLoop()) {
/* 377:413 */         register0(promise);
/* 378:    */       } else {
/* 379:    */         try
/* 380:    */         {
/* 381:416 */           eventLoop.execute(new OneTimeTask()
/* 382:    */           {
/* 383:    */             public void run()
/* 384:    */             {
/* 385:419 */               AbstractChannel.AbstractUnsafe.this.register0(promise);
/* 386:    */             }
/* 387:    */           });
/* 388:    */         }
/* 389:    */         catch (Throwable t)
/* 390:    */         {
/* 391:423 */           AbstractChannel.logger.warn("Force-closing a channel whose registration task was not accepted by an event loop: {}", AbstractChannel.this, t);
/* 392:    */           
/* 393:    */ 
/* 394:426 */           closeForcibly();
/* 395:427 */           AbstractChannel.this.closeFuture.setClosed();
/* 396:428 */           safeSetFailure(promise, t);
/* 397:    */         }
/* 398:    */       }
/* 399:    */     }
/* 400:    */     
/* 401:    */     private void register0(ChannelPromise promise)
/* 402:    */     {
/* 403:    */       try
/* 404:    */       {
/* 405:437 */         if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/* 406:438 */           return;
/* 407:    */         }
/* 408:440 */         AbstractChannel.this.doRegister();
/* 409:441 */         AbstractChannel.this.registered = true;
/* 410:442 */         safeSetSuccess(promise);
/* 411:443 */         AbstractChannel.this.pipeline.fireChannelRegistered();
/* 412:444 */         if (AbstractChannel.this.isActive()) {
/* 413:445 */           AbstractChannel.this.pipeline.fireChannelActive();
/* 414:    */         }
/* 415:    */       }
/* 416:    */       catch (Throwable t)
/* 417:    */       {
/* 418:449 */         closeForcibly();
/* 419:450 */         AbstractChannel.this.closeFuture.setClosed();
/* 420:451 */         safeSetFailure(promise, t);
/* 421:    */       }
/* 422:    */     }
/* 423:    */     
/* 424:    */     public final void bind(SocketAddress localAddress, ChannelPromise promise)
/* 425:    */     {
/* 426:457 */       if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/* 427:458 */         return;
/* 428:    */       }
/* 429:462 */       if ((!PlatformDependent.isWindows()) && (!PlatformDependent.isRoot()) && (Boolean.TRUE.equals(AbstractChannel.this.config().getOption(ChannelOption.SO_BROADCAST))) && ((localAddress instanceof InetSocketAddress)) && (!((InetSocketAddress)localAddress).getAddress().isAnyLocalAddress())) {
/* 430:468 */         AbstractChannel.logger.warn("A non-root user can't receive a broadcast packet if the socket is not bound to a wildcard address; binding to a non-wildcard address (" + localAddress + ") anyway as requested.");
/* 431:    */       }
/* 432:474 */       boolean wasActive = AbstractChannel.this.isActive();
/* 433:    */       try
/* 434:    */       {
/* 435:476 */         AbstractChannel.this.doBind(localAddress);
/* 436:    */       }
/* 437:    */       catch (Throwable t)
/* 438:    */       {
/* 439:478 */         safeSetFailure(promise, t);
/* 440:479 */         closeIfClosed();
/* 441:480 */         return;
/* 442:    */       }
/* 443:483 */       if ((!wasActive) && (AbstractChannel.this.isActive())) {
/* 444:484 */         invokeLater(new OneTimeTask()
/* 445:    */         {
/* 446:    */           public void run()
/* 447:    */           {
/* 448:487 */             AbstractChannel.this.pipeline.fireChannelActive();
/* 449:    */           }
/* 450:    */         });
/* 451:    */       }
/* 452:492 */       safeSetSuccess(promise);
/* 453:    */     }
/* 454:    */     
/* 455:    */     public final void disconnect(ChannelPromise promise)
/* 456:    */     {
/* 457:497 */       if (!promise.setUncancellable()) {
/* 458:498 */         return;
/* 459:    */       }
/* 460:501 */       boolean wasActive = AbstractChannel.this.isActive();
/* 461:    */       try
/* 462:    */       {
/* 463:503 */         AbstractChannel.this.doDisconnect();
/* 464:    */       }
/* 465:    */       catch (Throwable t)
/* 466:    */       {
/* 467:505 */         safeSetFailure(promise, t);
/* 468:506 */         closeIfClosed();
/* 469:507 */         return;
/* 470:    */       }
/* 471:510 */       if ((wasActive) && (!AbstractChannel.this.isActive())) {
/* 472:511 */         invokeLater(new OneTimeTask()
/* 473:    */         {
/* 474:    */           public void run()
/* 475:    */           {
/* 476:514 */             AbstractChannel.this.pipeline.fireChannelInactive();
/* 477:    */           }
/* 478:    */         });
/* 479:    */       }
/* 480:519 */       safeSetSuccess(promise);
/* 481:520 */       closeIfClosed();
/* 482:    */     }
/* 483:    */     
/* 484:    */     public final void close(final ChannelPromise promise)
/* 485:    */     {
/* 486:525 */       if (!promise.setUncancellable()) {
/* 487:526 */         return;
/* 488:    */       }
/* 489:529 */       if (this.inFlush0)
/* 490:    */       {
/* 491:530 */         invokeLater(new OneTimeTask()
/* 492:    */         {
/* 493:    */           public void run()
/* 494:    */           {
/* 495:533 */             AbstractChannel.AbstractUnsafe.this.close(promise);
/* 496:    */           }
/* 497:535 */         });
/* 498:536 */         return;
/* 499:    */       }
/* 500:539 */       if (AbstractChannel.this.closeFuture.isDone())
/* 501:    */       {
/* 502:541 */         safeSetSuccess(promise);
/* 503:542 */         return;
/* 504:    */       }
/* 505:545 */       boolean wasActive = AbstractChannel.this.isActive();
/* 506:546 */       ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
/* 507:547 */       this.outboundBuffer = null;
/* 508:    */       try
/* 509:    */       {
/* 510:550 */         AbstractChannel.this.doClose();
/* 511:551 */         AbstractChannel.this.closeFuture.setClosed();
/* 512:552 */         safeSetSuccess(promise);
/* 513:    */       }
/* 514:    */       catch (Throwable t)
/* 515:    */       {
/* 516:554 */         AbstractChannel.this.closeFuture.setClosed();
/* 517:555 */         safeSetFailure(promise, t);
/* 518:    */       }
/* 519:    */       try
/* 520:    */       {
/* 521:560 */         outboundBuffer.failFlushed(AbstractChannel.CLOSED_CHANNEL_EXCEPTION);
/* 522:561 */         outboundBuffer.close(AbstractChannel.CLOSED_CHANNEL_EXCEPTION);
/* 523:    */       }
/* 524:    */       finally
/* 525:    */       {
/* 526:564 */         if ((wasActive) && (!AbstractChannel.this.isActive())) {
/* 527:565 */           invokeLater(new OneTimeTask()
/* 528:    */           {
/* 529:    */             public void run()
/* 530:    */             {
/* 531:568 */               AbstractChannel.this.pipeline.fireChannelInactive();
/* 532:    */             }
/* 533:    */           });
/* 534:    */         }
/* 535:573 */         deregister(voidPromise());
/* 536:    */       }
/* 537:    */     }
/* 538:    */     
/* 539:    */     public final void closeForcibly()
/* 540:    */     {
/* 541:    */       try
/* 542:    */       {
/* 543:580 */         AbstractChannel.this.doClose();
/* 544:    */       }
/* 545:    */       catch (Exception e)
/* 546:    */       {
/* 547:582 */         AbstractChannel.logger.warn("Failed to close a channel.", e);
/* 548:    */       }
/* 549:    */     }
/* 550:    */     
/* 551:    */     public final void deregister(ChannelPromise promise)
/* 552:    */     {
/* 553:588 */       if (!promise.setUncancellable()) {
/* 554:589 */         return;
/* 555:    */       }
/* 556:592 */       if (!AbstractChannel.this.registered)
/* 557:    */       {
/* 558:593 */         safeSetSuccess(promise);
/* 559:594 */         return;
/* 560:    */       }
/* 561:    */       try
/* 562:    */       {
/* 563:598 */         AbstractChannel.this.doDeregister();
/* 564:    */       }
/* 565:    */       catch (Throwable t)
/* 566:    */       {
/* 567:600 */         AbstractChannel.logger.warn("Unexpected exception occurred while deregistering a channel.", t);
/* 568:    */       }
/* 569:    */       finally
/* 570:    */       {
/* 571:602 */         if (AbstractChannel.this.registered)
/* 572:    */         {
/* 573:603 */           AbstractChannel.this.registered = false;
/* 574:604 */           invokeLater(new OneTimeTask()
/* 575:    */           {
/* 576:    */             public void run()
/* 577:    */             {
/* 578:607 */               AbstractChannel.this.pipeline.fireChannelUnregistered();
/* 579:    */             }
/* 580:609 */           });
/* 581:610 */           safeSetSuccess(promise);
/* 582:    */         }
/* 583:    */         else
/* 584:    */         {
/* 585:615 */           safeSetSuccess(promise);
/* 586:    */         }
/* 587:    */       }
/* 588:    */     }
/* 589:    */     
/* 590:    */     public void beginRead()
/* 591:    */     {
/* 592:622 */       if (!AbstractChannel.this.isActive()) {
/* 593:623 */         return;
/* 594:    */       }
/* 595:    */       try
/* 596:    */       {
/* 597:627 */         AbstractChannel.this.doBeginRead();
/* 598:    */       }
/* 599:    */       catch (Exception e)
/* 600:    */       {
/* 601:629 */         invokeLater(new OneTimeTask()
/* 602:    */         {
/* 603:    */           public void run()
/* 604:    */           {
/* 605:632 */             AbstractChannel.this.pipeline.fireExceptionCaught(e);
/* 606:    */           }
/* 607:634 */         });
/* 608:635 */         close(voidPromise());
/* 609:    */       }
/* 610:    */     }
/* 611:    */     
/* 612:    */     public void write(Object msg, ChannelPromise promise)
/* 613:    */     {
/* 614:641 */       ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
/* 615:642 */       if (outboundBuffer == null)
/* 616:    */       {
/* 617:647 */         safeSetFailure(promise, AbstractChannel.CLOSED_CHANNEL_EXCEPTION);
/* 618:    */         
/* 619:649 */         ReferenceCountUtil.release(msg);
/* 620:650 */         return;
/* 621:    */       }
/* 622:652 */       outboundBuffer.addMessage(msg, promise);
/* 623:    */     }
/* 624:    */     
/* 625:    */     public void flush()
/* 626:    */     {
/* 627:657 */       ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
/* 628:658 */       if (outboundBuffer == null) {
/* 629:659 */         return;
/* 630:    */       }
/* 631:662 */       outboundBuffer.addFlush();
/* 632:663 */       flush0();
/* 633:    */     }
/* 634:    */     
/* 635:    */     protected void flush0()
/* 636:    */     {
/* 637:667 */       if (this.inFlush0) {
/* 638:669 */         return;
/* 639:    */       }
/* 640:672 */       ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
/* 641:673 */       if ((outboundBuffer == null) || (outboundBuffer.isEmpty())) {
/* 642:674 */         return;
/* 643:    */       }
/* 644:677 */       this.inFlush0 = true;
/* 645:680 */       if (!AbstractChannel.this.isActive())
/* 646:    */       {
/* 647:    */         try
/* 648:    */         {
/* 649:682 */           if (AbstractChannel.this.isOpen()) {
/* 650:683 */             outboundBuffer.failFlushed(AbstractChannel.NOT_YET_CONNECTED_EXCEPTION);
/* 651:    */           } else {
/* 652:685 */             outboundBuffer.failFlushed(AbstractChannel.CLOSED_CHANNEL_EXCEPTION);
/* 653:    */           }
/* 654:    */         }
/* 655:    */         finally
/* 656:    */         {
/* 657:688 */           this.inFlush0 = false;
/* 658:    */         }
/* 659:690 */         return;
/* 660:    */       }
/* 661:    */       try
/* 662:    */       {
/* 663:694 */         AbstractChannel.this.doWrite(outboundBuffer);
/* 664:    */       }
/* 665:    */       catch (Throwable t)
/* 666:    */       {
/* 667:696 */         outboundBuffer.failFlushed(t);
/* 668:697 */         if (((t instanceof IOException)) && (AbstractChannel.this.config().isAutoClose())) {
/* 669:698 */           close(voidPromise());
/* 670:    */         }
/* 671:    */       }
/* 672:    */       finally
/* 673:    */       {
/* 674:701 */         this.inFlush0 = false;
/* 675:    */       }
/* 676:    */     }
/* 677:    */     
/* 678:    */     public ChannelPromise voidPromise()
/* 679:    */     {
/* 680:707 */       return AbstractChannel.this.unsafeVoidPromise;
/* 681:    */     }
/* 682:    */     
/* 683:    */     protected final boolean ensureOpen(ChannelPromise promise)
/* 684:    */     {
/* 685:711 */       if (AbstractChannel.this.isOpen()) {
/* 686:712 */         return true;
/* 687:    */       }
/* 688:715 */       safeSetFailure(promise, AbstractChannel.CLOSED_CHANNEL_EXCEPTION);
/* 689:716 */       return false;
/* 690:    */     }
/* 691:    */     
/* 692:    */     protected final void safeSetSuccess(ChannelPromise promise)
/* 693:    */     {
/* 694:723 */       if ((!(promise instanceof VoidChannelPromise)) && (!promise.trySuccess())) {
/* 695:724 */         AbstractChannel.logger.warn("Failed to mark a promise as success because it is done already: {}", promise);
/* 696:    */       }
/* 697:    */     }
/* 698:    */     
/* 699:    */     protected final void safeSetFailure(ChannelPromise promise, Throwable cause)
/* 700:    */     {
/* 701:732 */       if ((!(promise instanceof VoidChannelPromise)) && (!promise.tryFailure(cause))) {
/* 702:733 */         AbstractChannel.logger.warn("Failed to mark a promise as failure because it's done already: {}", promise, cause);
/* 703:    */       }
/* 704:    */     }
/* 705:    */     
/* 706:    */     protected final void closeIfClosed()
/* 707:    */     {
/* 708:738 */       if (AbstractChannel.this.isOpen()) {
/* 709:739 */         return;
/* 710:    */       }
/* 711:741 */       close(voidPromise());
/* 712:    */     }
/* 713:    */     
/* 714:    */     private void invokeLater(Runnable task)
/* 715:    */     {
/* 716:    */       try
/* 717:    */       {
/* 718:757 */         AbstractChannel.this.eventLoop().execute(task);
/* 719:    */       }
/* 720:    */       catch (RejectedExecutionException e)
/* 721:    */       {
/* 722:759 */         AbstractChannel.logger.warn("Can't invoke task later as EventLoop rejected it", e);
/* 723:    */       }
/* 724:    */     }
/* 725:    */   }
/* 726:    */   
/* 727:    */   protected static void checkEOF(FileRegion region)
/* 728:    */     throws IOException
/* 729:    */   {
/* 730:823 */     if (region.transfered() < region.count()) {
/* 731:824 */       throw new EOFException("Expected to be able to write " + region.count() + " bytes, but only wrote " + region.transfered());
/* 732:    */     }
/* 733:    */   }
/* 734:    */   
/* 735:    */   protected abstract AbstractUnsafe newUnsafe();
/* 736:    */   
/* 737:    */   protected abstract boolean isCompatible(EventLoop paramEventLoop);
/* 738:    */   
/* 739:    */   protected abstract SocketAddress localAddress0();
/* 740:    */   
/* 741:    */   protected abstract SocketAddress remoteAddress0();
/* 742:    */   
/* 743:    */   protected void doRegister()
/* 744:    */     throws Exception
/* 745:    */   {}
/* 746:    */   
/* 747:    */   protected abstract void doBind(SocketAddress paramSocketAddress)
/* 748:    */     throws Exception;
/* 749:    */   
/* 750:    */   protected abstract void doDisconnect()
/* 751:    */     throws Exception;
/* 752:    */   
/* 753:    */   protected abstract void doClose()
/* 754:    */     throws Exception;
/* 755:    */   
/* 756:    */   protected void doDeregister()
/* 757:    */     throws Exception
/* 758:    */   {}
/* 759:    */   
/* 760:    */   protected abstract void doBeginRead()
/* 761:    */     throws Exception;
/* 762:    */   
/* 763:    */   protected abstract void doWrite(ChannelOutboundBuffer paramChannelOutboundBuffer)
/* 764:    */     throws Exception;
/* 765:    */   
/* 766:    */   static final class CloseFuture
/* 767:    */     extends DefaultChannelPromise
/* 768:    */   {
/* 769:    */     CloseFuture(AbstractChannel ch)
/* 770:    */     {
/* 771:833 */       super();
/* 772:    */     }
/* 773:    */     
/* 774:    */     public ChannelPromise setSuccess()
/* 775:    */     {
/* 776:838 */       throw new IllegalStateException();
/* 777:    */     }
/* 778:    */     
/* 779:    */     public ChannelPromise setFailure(Throwable cause)
/* 780:    */     {
/* 781:843 */       throw new IllegalStateException();
/* 782:    */     }
/* 783:    */     
/* 784:    */     public boolean trySuccess()
/* 785:    */     {
/* 786:848 */       throw new IllegalStateException();
/* 787:    */     }
/* 788:    */     
/* 789:    */     public boolean tryFailure(Throwable cause)
/* 790:    */     {
/* 791:853 */       throw new IllegalStateException();
/* 792:    */     }
/* 793:    */     
/* 794:    */     boolean setClosed()
/* 795:    */     {
/* 796:857 */       return super.trySuccess();
/* 797:    */     }
/* 798:    */   }
/* 799:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.AbstractChannel
 * JD-Core Version:    0.7.0.1
 */