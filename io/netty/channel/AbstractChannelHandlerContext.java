/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.util.DefaultAttributeMap;
/*   5:    */ import io.netty.util.Recycler;
/*   6:    */ import io.netty.util.Recycler.Handle;
/*   7:    */ import io.netty.util.ReferenceCountUtil;
/*   8:    */ import io.netty.util.concurrent.EventExecutor;
/*   9:    */ import io.netty.util.concurrent.EventExecutorGroup;
/*  10:    */ import io.netty.util.internal.OneTimeTask;
/*  11:    */ import io.netty.util.internal.RecyclableMpscLinkedQueueNode;
/*  12:    */ import io.netty.util.internal.StringUtil;
/*  13:    */ import io.netty.util.internal.logging.InternalLogger;
/*  14:    */ import java.net.SocketAddress;
/*  15:    */ import java.util.Map;
/*  16:    */ 
/*  17:    */ abstract class AbstractChannelHandlerContext
/*  18:    */   extends DefaultAttributeMap
/*  19:    */   implements ChannelHandlerContext
/*  20:    */ {
/*  21:    */   volatile AbstractChannelHandlerContext next;
/*  22:    */   volatile AbstractChannelHandlerContext prev;
/*  23:    */   private final boolean inbound;
/*  24:    */   private final boolean outbound;
/*  25:    */   private final AbstractChannel channel;
/*  26:    */   private final DefaultChannelPipeline pipeline;
/*  27:    */   private final String name;
/*  28:    */   private boolean removed;
/*  29:    */   final EventExecutor executor;
/*  30:    */   private ChannelFuture succeededFuture;
/*  31:    */   private volatile Runnable invokeChannelReadCompleteTask;
/*  32:    */   private volatile Runnable invokeReadTask;
/*  33:    */   private volatile Runnable invokeChannelWritableStateChangedTask;
/*  34:    */   private volatile Runnable invokeFlushTask;
/*  35:    */   
/*  36:    */   AbstractChannelHandlerContext(DefaultChannelPipeline pipeline, EventExecutorGroup group, String name, boolean inbound, boolean outbound)
/*  37:    */   {
/*  38: 60 */     if (name == null) {
/*  39: 61 */       throw new NullPointerException("name");
/*  40:    */     }
/*  41: 64 */     this.channel = pipeline.channel;
/*  42: 65 */     this.pipeline = pipeline;
/*  43: 66 */     this.name = name;
/*  44: 68 */     if (group != null)
/*  45:    */     {
/*  46: 71 */       EventExecutor childExecutor = (EventExecutor)pipeline.childExecutors.get(group);
/*  47: 72 */       if (childExecutor == null)
/*  48:    */       {
/*  49: 73 */         childExecutor = group.next();
/*  50: 74 */         pipeline.childExecutors.put(group, childExecutor);
/*  51:    */       }
/*  52: 76 */       this.executor = childExecutor;
/*  53:    */     }
/*  54:    */     else
/*  55:    */     {
/*  56: 78 */       this.executor = null;
/*  57:    */     }
/*  58: 81 */     this.inbound = inbound;
/*  59: 82 */     this.outbound = outbound;
/*  60:    */   }
/*  61:    */   
/*  62:    */   void teardown()
/*  63:    */   {
/*  64: 87 */     EventExecutor executor = executor();
/*  65: 88 */     if (executor.inEventLoop()) {
/*  66: 89 */       teardown0();
/*  67:    */     } else {
/*  68: 91 */       executor.execute(new Runnable()
/*  69:    */       {
/*  70:    */         public void run()
/*  71:    */         {
/*  72: 94 */           AbstractChannelHandlerContext.this.teardown0();
/*  73:    */         }
/*  74:    */       });
/*  75:    */     }
/*  76:    */   }
/*  77:    */   
/*  78:    */   private void teardown0()
/*  79:    */   {
/*  80:101 */     AbstractChannelHandlerContext prev = this.prev;
/*  81:102 */     if (prev != null)
/*  82:    */     {
/*  83:103 */       synchronized (this.pipeline)
/*  84:    */       {
/*  85:104 */         this.pipeline.remove0(this);
/*  86:    */       }
/*  87:106 */       prev.teardown();
/*  88:    */     }
/*  89:    */   }
/*  90:    */   
/*  91:    */   public Channel channel()
/*  92:    */   {
/*  93:112 */     return this.channel;
/*  94:    */   }
/*  95:    */   
/*  96:    */   public ChannelPipeline pipeline()
/*  97:    */   {
/*  98:117 */     return this.pipeline;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public ByteBufAllocator alloc()
/* 102:    */   {
/* 103:122 */     return channel().config().getAllocator();
/* 104:    */   }
/* 105:    */   
/* 106:    */   public EventExecutor executor()
/* 107:    */   {
/* 108:127 */     if (this.executor == null) {
/* 109:128 */       return channel().eventLoop();
/* 110:    */     }
/* 111:130 */     return this.executor;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public String name()
/* 115:    */   {
/* 116:136 */     return this.name;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public ChannelHandlerContext fireChannelRegistered()
/* 120:    */   {
/* 121:141 */     final AbstractChannelHandlerContext next = findContextInbound();
/* 122:142 */     EventExecutor executor = next.executor();
/* 123:143 */     if (executor.inEventLoop()) {
/* 124:144 */       next.invokeChannelRegistered();
/* 125:    */     } else {
/* 126:146 */       executor.execute(new OneTimeTask()
/* 127:    */       {
/* 128:    */         public void run()
/* 129:    */         {
/* 130:149 */           next.invokeChannelRegistered();
/* 131:    */         }
/* 132:    */       });
/* 133:    */     }
/* 134:153 */     return this;
/* 135:    */   }
/* 136:    */   
/* 137:    */   private void invokeChannelRegistered()
/* 138:    */   {
/* 139:    */     try
/* 140:    */     {
/* 141:158 */       ((ChannelInboundHandler)handler()).channelRegistered(this);
/* 142:    */     }
/* 143:    */     catch (Throwable t)
/* 144:    */     {
/* 145:160 */       notifyHandlerException(t);
/* 146:    */     }
/* 147:    */   }
/* 148:    */   
/* 149:    */   public ChannelHandlerContext fireChannelUnregistered()
/* 150:    */   {
/* 151:166 */     final AbstractChannelHandlerContext next = findContextInbound();
/* 152:167 */     EventExecutor executor = next.executor();
/* 153:168 */     if (executor.inEventLoop()) {
/* 154:169 */       next.invokeChannelUnregistered();
/* 155:    */     } else {
/* 156:171 */       executor.execute(new OneTimeTask()
/* 157:    */       {
/* 158:    */         public void run()
/* 159:    */         {
/* 160:174 */           next.invokeChannelUnregistered();
/* 161:    */         }
/* 162:    */       });
/* 163:    */     }
/* 164:178 */     return this;
/* 165:    */   }
/* 166:    */   
/* 167:    */   private void invokeChannelUnregistered()
/* 168:    */   {
/* 169:    */     try
/* 170:    */     {
/* 171:183 */       ((ChannelInboundHandler)handler()).channelUnregistered(this);
/* 172:    */     }
/* 173:    */     catch (Throwable t)
/* 174:    */     {
/* 175:185 */       notifyHandlerException(t);
/* 176:    */     }
/* 177:    */   }
/* 178:    */   
/* 179:    */   public ChannelHandlerContext fireChannelActive()
/* 180:    */   {
/* 181:191 */     final AbstractChannelHandlerContext next = findContextInbound();
/* 182:192 */     EventExecutor executor = next.executor();
/* 183:193 */     if (executor.inEventLoop()) {
/* 184:194 */       next.invokeChannelActive();
/* 185:    */     } else {
/* 186:196 */       executor.execute(new OneTimeTask()
/* 187:    */       {
/* 188:    */         public void run()
/* 189:    */         {
/* 190:199 */           next.invokeChannelActive();
/* 191:    */         }
/* 192:    */       });
/* 193:    */     }
/* 194:203 */     return this;
/* 195:    */   }
/* 196:    */   
/* 197:    */   private void invokeChannelActive()
/* 198:    */   {
/* 199:    */     try
/* 200:    */     {
/* 201:208 */       ((ChannelInboundHandler)handler()).channelActive(this);
/* 202:    */     }
/* 203:    */     catch (Throwable t)
/* 204:    */     {
/* 205:210 */       notifyHandlerException(t);
/* 206:    */     }
/* 207:    */   }
/* 208:    */   
/* 209:    */   public ChannelHandlerContext fireChannelInactive()
/* 210:    */   {
/* 211:216 */     final AbstractChannelHandlerContext next = findContextInbound();
/* 212:217 */     EventExecutor executor = next.executor();
/* 213:218 */     if (executor.inEventLoop()) {
/* 214:219 */       next.invokeChannelInactive();
/* 215:    */     } else {
/* 216:221 */       executor.execute(new OneTimeTask()
/* 217:    */       {
/* 218:    */         public void run()
/* 219:    */         {
/* 220:224 */           next.invokeChannelInactive();
/* 221:    */         }
/* 222:    */       });
/* 223:    */     }
/* 224:228 */     return this;
/* 225:    */   }
/* 226:    */   
/* 227:    */   private void invokeChannelInactive()
/* 228:    */   {
/* 229:    */     try
/* 230:    */     {
/* 231:233 */       ((ChannelInboundHandler)handler()).channelInactive(this);
/* 232:    */     }
/* 233:    */     catch (Throwable t)
/* 234:    */     {
/* 235:235 */       notifyHandlerException(t);
/* 236:    */     }
/* 237:    */   }
/* 238:    */   
/* 239:    */   public ChannelHandlerContext fireExceptionCaught(final Throwable cause)
/* 240:    */   {
/* 241:241 */     if (cause == null) {
/* 242:242 */       throw new NullPointerException("cause");
/* 243:    */     }
/* 244:245 */     final AbstractChannelHandlerContext next = this.next;
/* 245:    */     
/* 246:247 */     EventExecutor executor = next.executor();
/* 247:248 */     if (executor.inEventLoop()) {
/* 248:249 */       next.invokeExceptionCaught(cause);
/* 249:    */     } else {
/* 250:    */       try
/* 251:    */       {
/* 252:252 */         executor.execute(new OneTimeTask()
/* 253:    */         {
/* 254:    */           public void run()
/* 255:    */           {
/* 256:255 */             next.invokeExceptionCaught(cause);
/* 257:    */           }
/* 258:    */         });
/* 259:    */       }
/* 260:    */       catch (Throwable t)
/* 261:    */       {
/* 262:259 */         if (DefaultChannelPipeline.logger.isWarnEnabled())
/* 263:    */         {
/* 264:260 */           DefaultChannelPipeline.logger.warn("Failed to submit an exceptionCaught() event.", t);
/* 265:261 */           DefaultChannelPipeline.logger.warn("The exceptionCaught() event that was failed to submit was:", cause);
/* 266:    */         }
/* 267:    */       }
/* 268:    */     }
/* 269:266 */     return this;
/* 270:    */   }
/* 271:    */   
/* 272:    */   private void invokeExceptionCaught(Throwable cause)
/* 273:    */   {
/* 274:    */     try
/* 275:    */     {
/* 276:271 */       handler().exceptionCaught(this, cause);
/* 277:    */     }
/* 278:    */     catch (Throwable t)
/* 279:    */     {
/* 280:273 */       if (DefaultChannelPipeline.logger.isWarnEnabled()) {
/* 281:274 */         DefaultChannelPipeline.logger.warn("An exception was thrown by a user handler's exceptionCaught() method while handling the following exception:", cause);
/* 282:    */       }
/* 283:    */     }
/* 284:    */   }
/* 285:    */   
/* 286:    */   public ChannelHandlerContext fireUserEventTriggered(final Object event)
/* 287:    */   {
/* 288:283 */     if (event == null) {
/* 289:284 */       throw new NullPointerException("event");
/* 290:    */     }
/* 291:287 */     final AbstractChannelHandlerContext next = findContextInbound();
/* 292:288 */     EventExecutor executor = next.executor();
/* 293:289 */     if (executor.inEventLoop()) {
/* 294:290 */       next.invokeUserEventTriggered(event);
/* 295:    */     } else {
/* 296:292 */       executor.execute(new OneTimeTask()
/* 297:    */       {
/* 298:    */         public void run()
/* 299:    */         {
/* 300:295 */           next.invokeUserEventTriggered(event);
/* 301:    */         }
/* 302:    */       });
/* 303:    */     }
/* 304:299 */     return this;
/* 305:    */   }
/* 306:    */   
/* 307:    */   private void invokeUserEventTriggered(Object event)
/* 308:    */   {
/* 309:    */     try
/* 310:    */     {
/* 311:304 */       ((ChannelInboundHandler)handler()).userEventTriggered(this, event);
/* 312:    */     }
/* 313:    */     catch (Throwable t)
/* 314:    */     {
/* 315:306 */       notifyHandlerException(t);
/* 316:    */     }
/* 317:    */   }
/* 318:    */   
/* 319:    */   public ChannelHandlerContext fireChannelRead(final Object msg)
/* 320:    */   {
/* 321:312 */     if (msg == null) {
/* 322:313 */       throw new NullPointerException("msg");
/* 323:    */     }
/* 324:316 */     final AbstractChannelHandlerContext next = findContextInbound();
/* 325:317 */     EventExecutor executor = next.executor();
/* 326:318 */     if (executor.inEventLoop()) {
/* 327:319 */       next.invokeChannelRead(msg);
/* 328:    */     } else {
/* 329:321 */       executor.execute(new OneTimeTask()
/* 330:    */       {
/* 331:    */         public void run()
/* 332:    */         {
/* 333:324 */           next.invokeChannelRead(msg);
/* 334:    */         }
/* 335:    */       });
/* 336:    */     }
/* 337:328 */     return this;
/* 338:    */   }
/* 339:    */   
/* 340:    */   private void invokeChannelRead(Object msg)
/* 341:    */   {
/* 342:    */     try
/* 343:    */     {
/* 344:333 */       ((ChannelInboundHandler)handler()).channelRead(this, msg);
/* 345:    */     }
/* 346:    */     catch (Throwable t)
/* 347:    */     {
/* 348:335 */       notifyHandlerException(t);
/* 349:    */     }
/* 350:    */   }
/* 351:    */   
/* 352:    */   public ChannelHandlerContext fireChannelReadComplete()
/* 353:    */   {
/* 354:341 */     final AbstractChannelHandlerContext next = findContextInbound();
/* 355:342 */     EventExecutor executor = next.executor();
/* 356:343 */     if (executor.inEventLoop())
/* 357:    */     {
/* 358:344 */       next.invokeChannelReadComplete();
/* 359:    */     }
/* 360:    */     else
/* 361:    */     {
/* 362:346 */       Runnable task = next.invokeChannelReadCompleteTask;
/* 363:347 */       if (task == null) {
/* 364:348 */         next.invokeChannelReadCompleteTask = (task = new Runnable()
/* 365:    */         {
/* 366:    */           public void run()
/* 367:    */           {
/* 368:351 */             next.invokeChannelReadComplete();
/* 369:    */           }
/* 370:    */         });
/* 371:    */       }
/* 372:355 */       executor.execute(task);
/* 373:    */     }
/* 374:357 */     return this;
/* 375:    */   }
/* 376:    */   
/* 377:    */   private void invokeChannelReadComplete()
/* 378:    */   {
/* 379:    */     try
/* 380:    */     {
/* 381:362 */       ((ChannelInboundHandler)handler()).channelReadComplete(this);
/* 382:    */     }
/* 383:    */     catch (Throwable t)
/* 384:    */     {
/* 385:364 */       notifyHandlerException(t);
/* 386:    */     }
/* 387:    */   }
/* 388:    */   
/* 389:    */   public ChannelHandlerContext fireChannelWritabilityChanged()
/* 390:    */   {
/* 391:370 */     final AbstractChannelHandlerContext next = findContextInbound();
/* 392:371 */     EventExecutor executor = next.executor();
/* 393:372 */     if (executor.inEventLoop())
/* 394:    */     {
/* 395:373 */       next.invokeChannelWritabilityChanged();
/* 396:    */     }
/* 397:    */     else
/* 398:    */     {
/* 399:375 */       Runnable task = next.invokeChannelWritableStateChangedTask;
/* 400:376 */       if (task == null) {
/* 401:377 */         next.invokeChannelWritableStateChangedTask = (task = new Runnable()
/* 402:    */         {
/* 403:    */           public void run()
/* 404:    */           {
/* 405:380 */             next.invokeChannelWritabilityChanged();
/* 406:    */           }
/* 407:    */         });
/* 408:    */       }
/* 409:384 */       executor.execute(task);
/* 410:    */     }
/* 411:386 */     return this;
/* 412:    */   }
/* 413:    */   
/* 414:    */   private void invokeChannelWritabilityChanged()
/* 415:    */   {
/* 416:    */     try
/* 417:    */     {
/* 418:391 */       ((ChannelInboundHandler)handler()).channelWritabilityChanged(this);
/* 419:    */     }
/* 420:    */     catch (Throwable t)
/* 421:    */     {
/* 422:393 */       notifyHandlerException(t);
/* 423:    */     }
/* 424:    */   }
/* 425:    */   
/* 426:    */   public ChannelFuture bind(SocketAddress localAddress)
/* 427:    */   {
/* 428:399 */     return bind(localAddress, newPromise());
/* 429:    */   }
/* 430:    */   
/* 431:    */   public ChannelFuture connect(SocketAddress remoteAddress)
/* 432:    */   {
/* 433:404 */     return connect(remoteAddress, newPromise());
/* 434:    */   }
/* 435:    */   
/* 436:    */   public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 437:    */   {
/* 438:409 */     return connect(remoteAddress, localAddress, newPromise());
/* 439:    */   }
/* 440:    */   
/* 441:    */   public ChannelFuture disconnect()
/* 442:    */   {
/* 443:414 */     return disconnect(newPromise());
/* 444:    */   }
/* 445:    */   
/* 446:    */   public ChannelFuture close()
/* 447:    */   {
/* 448:419 */     return close(newPromise());
/* 449:    */   }
/* 450:    */   
/* 451:    */   public ChannelFuture deregister()
/* 452:    */   {
/* 453:424 */     return deregister(newPromise());
/* 454:    */   }
/* 455:    */   
/* 456:    */   public ChannelFuture bind(final SocketAddress localAddress, final ChannelPromise promise)
/* 457:    */   {
/* 458:429 */     if (localAddress == null) {
/* 459:430 */       throw new NullPointerException("localAddress");
/* 460:    */     }
/* 461:432 */     if (!validatePromise(promise, false)) {
/* 462:434 */       return promise;
/* 463:    */     }
/* 464:437 */     final AbstractChannelHandlerContext next = findContextOutbound();
/* 465:438 */     EventExecutor executor = next.executor();
/* 466:439 */     if (executor.inEventLoop()) {
/* 467:440 */       next.invokeBind(localAddress, promise);
/* 468:    */     } else {
/* 469:442 */       safeExecute(executor, new OneTimeTask()
/* 470:    */       {
/* 471:    */         public void run()
/* 472:    */         {
/* 473:445 */           next.invokeBind(localAddress, promise);
/* 474:    */         }
/* 475:445 */       }, promise, null);
/* 476:    */     }
/* 477:450 */     return promise;
/* 478:    */   }
/* 479:    */   
/* 480:    */   private void invokeBind(SocketAddress localAddress, ChannelPromise promise)
/* 481:    */   {
/* 482:    */     try
/* 483:    */     {
/* 484:455 */       ((ChannelOutboundHandler)handler()).bind(this, localAddress, promise);
/* 485:    */     }
/* 486:    */     catch (Throwable t)
/* 487:    */     {
/* 488:457 */       notifyOutboundHandlerException(t, promise);
/* 489:    */     }
/* 490:    */   }
/* 491:    */   
/* 492:    */   public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise)
/* 493:    */   {
/* 494:463 */     return connect(remoteAddress, null, promise);
/* 495:    */   }
/* 496:    */   
/* 497:    */   public ChannelFuture connect(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise)
/* 498:    */   {
/* 499:470 */     if (remoteAddress == null) {
/* 500:471 */       throw new NullPointerException("remoteAddress");
/* 501:    */     }
/* 502:473 */     if (!validatePromise(promise, false)) {
/* 503:475 */       return promise;
/* 504:    */     }
/* 505:478 */     final AbstractChannelHandlerContext next = findContextOutbound();
/* 506:479 */     EventExecutor executor = next.executor();
/* 507:480 */     if (executor.inEventLoop()) {
/* 508:481 */       next.invokeConnect(remoteAddress, localAddress, promise);
/* 509:    */     } else {
/* 510:483 */       safeExecute(executor, new OneTimeTask()
/* 511:    */       {
/* 512:    */         public void run()
/* 513:    */         {
/* 514:486 */           next.invokeConnect(remoteAddress, localAddress, promise);
/* 515:    */         }
/* 516:486 */       }, promise, null);
/* 517:    */     }
/* 518:491 */     return promise;
/* 519:    */   }
/* 520:    */   
/* 521:    */   private void invokeConnect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 522:    */   {
/* 523:    */     try
/* 524:    */     {
/* 525:496 */       ((ChannelOutboundHandler)handler()).connect(this, remoteAddress, localAddress, promise);
/* 526:    */     }
/* 527:    */     catch (Throwable t)
/* 528:    */     {
/* 529:498 */       notifyOutboundHandlerException(t, promise);
/* 530:    */     }
/* 531:    */   }
/* 532:    */   
/* 533:    */   public ChannelFuture disconnect(final ChannelPromise promise)
/* 534:    */   {
/* 535:504 */     if (!validatePromise(promise, false)) {
/* 536:506 */       return promise;
/* 537:    */     }
/* 538:509 */     final AbstractChannelHandlerContext next = findContextOutbound();
/* 539:510 */     EventExecutor executor = next.executor();
/* 540:511 */     if (executor.inEventLoop())
/* 541:    */     {
/* 542:514 */       if (!channel().metadata().hasDisconnect()) {
/* 543:515 */         next.invokeClose(promise);
/* 544:    */       } else {
/* 545:517 */         next.invokeDisconnect(promise);
/* 546:    */       }
/* 547:    */     }
/* 548:    */     else {
/* 549:520 */       safeExecute(executor, new OneTimeTask()
/* 550:    */       {
/* 551:    */         public void run()
/* 552:    */         {
/* 553:523 */           if (!AbstractChannelHandlerContext.this.channel().metadata().hasDisconnect()) {
/* 554:524 */             next.invokeClose(promise);
/* 555:    */           } else {
/* 556:526 */             next.invokeDisconnect(promise);
/* 557:    */           }
/* 558:    */         }
/* 559:526 */       }, promise, null);
/* 560:    */     }
/* 561:532 */     return promise;
/* 562:    */   }
/* 563:    */   
/* 564:    */   private void invokeDisconnect(ChannelPromise promise)
/* 565:    */   {
/* 566:    */     try
/* 567:    */     {
/* 568:537 */       ((ChannelOutboundHandler)handler()).disconnect(this, promise);
/* 569:    */     }
/* 570:    */     catch (Throwable t)
/* 571:    */     {
/* 572:539 */       notifyOutboundHandlerException(t, promise);
/* 573:    */     }
/* 574:    */   }
/* 575:    */   
/* 576:    */   public ChannelFuture close(final ChannelPromise promise)
/* 577:    */   {
/* 578:545 */     if (!validatePromise(promise, false)) {
/* 579:547 */       return promise;
/* 580:    */     }
/* 581:550 */     final AbstractChannelHandlerContext next = findContextOutbound();
/* 582:551 */     EventExecutor executor = next.executor();
/* 583:552 */     if (executor.inEventLoop()) {
/* 584:553 */       next.invokeClose(promise);
/* 585:    */     } else {
/* 586:555 */       safeExecute(executor, new OneTimeTask()
/* 587:    */       {
/* 588:    */         public void run()
/* 589:    */         {
/* 590:558 */           next.invokeClose(promise);
/* 591:    */         }
/* 592:558 */       }, promise, null);
/* 593:    */     }
/* 594:563 */     return promise;
/* 595:    */   }
/* 596:    */   
/* 597:    */   private void invokeClose(ChannelPromise promise)
/* 598:    */   {
/* 599:    */     try
/* 600:    */     {
/* 601:568 */       ((ChannelOutboundHandler)handler()).close(this, promise);
/* 602:    */     }
/* 603:    */     catch (Throwable t)
/* 604:    */     {
/* 605:570 */       notifyOutboundHandlerException(t, promise);
/* 606:    */     }
/* 607:    */   }
/* 608:    */   
/* 609:    */   public ChannelFuture deregister(final ChannelPromise promise)
/* 610:    */   {
/* 611:576 */     if (!validatePromise(promise, false)) {
/* 612:578 */       return promise;
/* 613:    */     }
/* 614:581 */     final AbstractChannelHandlerContext next = findContextOutbound();
/* 615:582 */     EventExecutor executor = next.executor();
/* 616:583 */     if (executor.inEventLoop()) {
/* 617:584 */       next.invokeDeregister(promise);
/* 618:    */     } else {
/* 619:586 */       safeExecute(executor, new OneTimeTask()
/* 620:    */       {
/* 621:    */         public void run()
/* 622:    */         {
/* 623:589 */           next.invokeDeregister(promise);
/* 624:    */         }
/* 625:589 */       }, promise, null);
/* 626:    */     }
/* 627:594 */     return promise;
/* 628:    */   }
/* 629:    */   
/* 630:    */   private void invokeDeregister(ChannelPromise promise)
/* 631:    */   {
/* 632:    */     try
/* 633:    */     {
/* 634:599 */       ((ChannelOutboundHandler)handler()).deregister(this, promise);
/* 635:    */     }
/* 636:    */     catch (Throwable t)
/* 637:    */     {
/* 638:601 */       notifyOutboundHandlerException(t, promise);
/* 639:    */     }
/* 640:    */   }
/* 641:    */   
/* 642:    */   public ChannelHandlerContext read()
/* 643:    */   {
/* 644:607 */     final AbstractChannelHandlerContext next = findContextOutbound();
/* 645:608 */     EventExecutor executor = next.executor();
/* 646:609 */     if (executor.inEventLoop())
/* 647:    */     {
/* 648:610 */       next.invokeRead();
/* 649:    */     }
/* 650:    */     else
/* 651:    */     {
/* 652:612 */       Runnable task = next.invokeReadTask;
/* 653:613 */       if (task == null) {
/* 654:614 */         next.invokeReadTask = (task = new Runnable()
/* 655:    */         {
/* 656:    */           public void run()
/* 657:    */           {
/* 658:617 */             next.invokeRead();
/* 659:    */           }
/* 660:    */         });
/* 661:    */       }
/* 662:621 */       executor.execute(task);
/* 663:    */     }
/* 664:624 */     return this;
/* 665:    */   }
/* 666:    */   
/* 667:    */   private void invokeRead()
/* 668:    */   {
/* 669:    */     try
/* 670:    */     {
/* 671:629 */       ((ChannelOutboundHandler)handler()).read(this);
/* 672:    */     }
/* 673:    */     catch (Throwable t)
/* 674:    */     {
/* 675:631 */       notifyHandlerException(t);
/* 676:    */     }
/* 677:    */   }
/* 678:    */   
/* 679:    */   public ChannelFuture write(Object msg)
/* 680:    */   {
/* 681:637 */     return write(msg, newPromise());
/* 682:    */   }
/* 683:    */   
/* 684:    */   public ChannelFuture write(Object msg, ChannelPromise promise)
/* 685:    */   {
/* 686:642 */     if (msg == null) {
/* 687:643 */       throw new NullPointerException("msg");
/* 688:    */     }
/* 689:646 */     if (!validatePromise(promise, true))
/* 690:    */     {
/* 691:647 */       ReferenceCountUtil.release(msg);
/* 692:    */       
/* 693:649 */       return promise;
/* 694:    */     }
/* 695:651 */     write(msg, false, promise);
/* 696:    */     
/* 697:653 */     return promise;
/* 698:    */   }
/* 699:    */   
/* 700:    */   private void invokeWrite(Object msg, ChannelPromise promise)
/* 701:    */   {
/* 702:    */     try
/* 703:    */     {
/* 704:658 */       ((ChannelOutboundHandler)handler()).write(this, msg, promise);
/* 705:    */     }
/* 706:    */     catch (Throwable t)
/* 707:    */     {
/* 708:660 */       notifyOutboundHandlerException(t, promise);
/* 709:    */     }
/* 710:    */   }
/* 711:    */   
/* 712:    */   public ChannelHandlerContext flush()
/* 713:    */   {
/* 714:666 */     final AbstractChannelHandlerContext next = findContextOutbound();
/* 715:667 */     EventExecutor executor = next.executor();
/* 716:668 */     if (executor.inEventLoop())
/* 717:    */     {
/* 718:669 */       next.invokeFlush();
/* 719:    */     }
/* 720:    */     else
/* 721:    */     {
/* 722:671 */       Runnable task = next.invokeFlushTask;
/* 723:672 */       if (task == null) {
/* 724:673 */         next.invokeFlushTask = (task = new Runnable()
/* 725:    */         {
/* 726:    */           public void run()
/* 727:    */           {
/* 728:676 */             next.invokeFlush();
/* 729:    */           }
/* 730:    */         });
/* 731:    */       }
/* 732:680 */       safeExecute(executor, task, this.channel.voidPromise(), null);
/* 733:    */     }
/* 734:683 */     return this;
/* 735:    */   }
/* 736:    */   
/* 737:    */   private void invokeFlush()
/* 738:    */   {
/* 739:    */     try
/* 740:    */     {
/* 741:688 */       ((ChannelOutboundHandler)handler()).flush(this);
/* 742:    */     }
/* 743:    */     catch (Throwable t)
/* 744:    */     {
/* 745:690 */       notifyHandlerException(t);
/* 746:    */     }
/* 747:    */   }
/* 748:    */   
/* 749:    */   public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise)
/* 750:    */   {
/* 751:696 */     if (msg == null) {
/* 752:697 */       throw new NullPointerException("msg");
/* 753:    */     }
/* 754:700 */     if (!validatePromise(promise, true))
/* 755:    */     {
/* 756:701 */       ReferenceCountUtil.release(msg);
/* 757:    */       
/* 758:703 */       return promise;
/* 759:    */     }
/* 760:706 */     write(msg, true, promise);
/* 761:    */     
/* 762:708 */     return promise;
/* 763:    */   }
/* 764:    */   
/* 765:    */   private void write(Object msg, boolean flush, ChannelPromise promise)
/* 766:    */   {
/* 767:713 */     AbstractChannelHandlerContext next = findContextOutbound();
/* 768:714 */     EventExecutor executor = next.executor();
/* 769:715 */     if (executor.inEventLoop())
/* 770:    */     {
/* 771:716 */       next.invokeWrite(msg, promise);
/* 772:717 */       if (flush) {
/* 773:718 */         next.invokeFlush();
/* 774:    */       }
/* 775:    */     }
/* 776:    */     else
/* 777:    */     {
/* 778:721 */       int size = this.channel.estimatorHandle().size(msg);
/* 779:722 */       if (size > 0)
/* 780:    */       {
/* 781:723 */         ChannelOutboundBuffer buffer = this.channel.unsafe().outboundBuffer();
/* 782:725 */         if (buffer != null) {
/* 783:726 */           buffer.incrementPendingOutboundBytes(size);
/* 784:    */         }
/* 785:    */       }
/* 786:    */       Runnable task;
/* 787:    */       Runnable task;
/* 788:730 */       if (flush) {
/* 789:731 */         task = WriteAndFlushTask.newInstance(next, msg, size, promise);
/* 790:    */       } else {
/* 791:733 */         task = WriteTask.newInstance(next, msg, size, promise);
/* 792:    */       }
/* 793:735 */       safeExecute(executor, task, promise, msg);
/* 794:    */     }
/* 795:    */   }
/* 796:    */   
/* 797:    */   public ChannelFuture writeAndFlush(Object msg)
/* 798:    */   {
/* 799:741 */     return writeAndFlush(msg, newPromise());
/* 800:    */   }
/* 801:    */   
/* 802:    */   private static void notifyOutboundHandlerException(Throwable cause, ChannelPromise promise)
/* 803:    */   {
/* 804:747 */     if ((promise instanceof VoidChannelPromise)) {
/* 805:748 */       return;
/* 806:    */     }
/* 807:751 */     if ((!promise.tryFailure(cause)) && 
/* 808:752 */       (DefaultChannelPipeline.logger.isWarnEnabled())) {
/* 809:753 */       DefaultChannelPipeline.logger.warn("Failed to fail the promise because it's done already: {}", promise, cause);
/* 810:    */     }
/* 811:    */   }
/* 812:    */   
/* 813:    */   private void notifyHandlerException(Throwable cause)
/* 814:    */   {
/* 815:759 */     if (inExceptionCaught(cause))
/* 816:    */     {
/* 817:760 */       if (DefaultChannelPipeline.logger.isWarnEnabled()) {
/* 818:761 */         DefaultChannelPipeline.logger.warn("An exception was thrown by a user handler while handling an exceptionCaught event", cause);
/* 819:    */       }
/* 820:765 */       return;
/* 821:    */     }
/* 822:768 */     invokeExceptionCaught(cause);
/* 823:    */   }
/* 824:    */   
/* 825:    */   private static boolean inExceptionCaught(Throwable cause)
/* 826:    */   {
/* 827:    */     do
/* 828:    */     {
/* 829:773 */       StackTraceElement[] trace = cause.getStackTrace();
/* 830:774 */       if (trace != null) {
/* 831:775 */         for (StackTraceElement t : trace)
/* 832:    */         {
/* 833:776 */           if (t == null) {
/* 834:    */             break;
/* 835:    */           }
/* 836:779 */           if ("exceptionCaught".equals(t.getMethodName())) {
/* 837:780 */             return true;
/* 838:    */           }
/* 839:    */         }
/* 840:    */       }
/* 841:785 */       cause = cause.getCause();
/* 842:786 */     } while (cause != null);
/* 843:788 */     return false;
/* 844:    */   }
/* 845:    */   
/* 846:    */   public ChannelPromise newPromise()
/* 847:    */   {
/* 848:793 */     return new DefaultChannelPromise(channel(), executor());
/* 849:    */   }
/* 850:    */   
/* 851:    */   public ChannelProgressivePromise newProgressivePromise()
/* 852:    */   {
/* 853:798 */     return new DefaultChannelProgressivePromise(channel(), executor());
/* 854:    */   }
/* 855:    */   
/* 856:    */   public ChannelFuture newSucceededFuture()
/* 857:    */   {
/* 858:803 */     ChannelFuture succeededFuture = this.succeededFuture;
/* 859:804 */     if (succeededFuture == null) {
/* 860:805 */       this.succeededFuture = (succeededFuture = new SucceededChannelFuture(channel(), executor()));
/* 861:    */     }
/* 862:807 */     return succeededFuture;
/* 863:    */   }
/* 864:    */   
/* 865:    */   public ChannelFuture newFailedFuture(Throwable cause)
/* 866:    */   {
/* 867:812 */     return new FailedChannelFuture(channel(), executor(), cause);
/* 868:    */   }
/* 869:    */   
/* 870:    */   private boolean validatePromise(ChannelPromise promise, boolean allowVoidPromise)
/* 871:    */   {
/* 872:816 */     if (promise == null) {
/* 873:817 */       throw new NullPointerException("promise");
/* 874:    */     }
/* 875:820 */     if (promise.isDone())
/* 876:    */     {
/* 877:825 */       if (promise.isCancelled()) {
/* 878:826 */         return false;
/* 879:    */       }
/* 880:828 */       throw new IllegalArgumentException("promise already done: " + promise);
/* 881:    */     }
/* 882:831 */     if (promise.channel() != channel()) {
/* 883:832 */       throw new IllegalArgumentException(String.format("promise.channel does not match: %s (expected: %s)", new Object[] { promise.channel(), channel() }));
/* 884:    */     }
/* 885:836 */     if (promise.getClass() == DefaultChannelPromise.class) {
/* 886:837 */       return true;
/* 887:    */     }
/* 888:840 */     if ((!allowVoidPromise) && ((promise instanceof VoidChannelPromise))) {
/* 889:841 */       throw new IllegalArgumentException(StringUtil.simpleClassName(VoidChannelPromise.class) + " not allowed for this operation");
/* 890:    */     }
/* 891:845 */     if ((promise instanceof AbstractChannel.CloseFuture)) {
/* 892:846 */       throw new IllegalArgumentException(StringUtil.simpleClassName(AbstractChannel.CloseFuture.class) + " not allowed in a pipeline");
/* 893:    */     }
/* 894:849 */     return true;
/* 895:    */   }
/* 896:    */   
/* 897:    */   private AbstractChannelHandlerContext findContextInbound()
/* 898:    */   {
/* 899:853 */     AbstractChannelHandlerContext ctx = this;
/* 900:    */     do
/* 901:    */     {
/* 902:855 */       ctx = ctx.next;
/* 903:856 */     } while (!ctx.inbound);
/* 904:857 */     return ctx;
/* 905:    */   }
/* 906:    */   
/* 907:    */   private AbstractChannelHandlerContext findContextOutbound()
/* 908:    */   {
/* 909:861 */     AbstractChannelHandlerContext ctx = this;
/* 910:    */     do
/* 911:    */     {
/* 912:863 */       ctx = ctx.prev;
/* 913:864 */     } while (!ctx.outbound);
/* 914:865 */     return ctx;
/* 915:    */   }
/* 916:    */   
/* 917:    */   public ChannelPromise voidPromise()
/* 918:    */   {
/* 919:870 */     return this.channel.voidPromise();
/* 920:    */   }
/* 921:    */   
/* 922:    */   void setRemoved()
/* 923:    */   {
/* 924:874 */     this.removed = true;
/* 925:    */   }
/* 926:    */   
/* 927:    */   public boolean isRemoved()
/* 928:    */   {
/* 929:879 */     return this.removed;
/* 930:    */   }
/* 931:    */   
/* 932:    */   private static void safeExecute(EventExecutor executor, Runnable runnable, ChannelPromise promise, Object msg)
/* 933:    */   {
/* 934:    */     try
/* 935:    */     {
/* 936:884 */       executor.execute(runnable);
/* 937:    */     }
/* 938:    */     catch (Throwable cause)
/* 939:    */     {
/* 940:    */       try
/* 941:    */       {
/* 942:887 */         promise.setFailure(cause);
/* 943:    */       }
/* 944:    */       finally
/* 945:    */       {
/* 946:889 */         if (msg != null) {
/* 947:890 */           ReferenceCountUtil.release(msg);
/* 948:    */         }
/* 949:    */       }
/* 950:    */     }
/* 951:    */   }
/* 952:    */   
/* 953:    */   static abstract class AbstractWriteTask
/* 954:    */     extends RecyclableMpscLinkedQueueNode<Runnable>
/* 955:    */     implements Runnable
/* 956:    */   {
/* 957:    */     private AbstractChannelHandlerContext ctx;
/* 958:    */     private Object msg;
/* 959:    */     private ChannelPromise promise;
/* 960:    */     private int size;
/* 961:    */     
/* 962:    */     private AbstractWriteTask(Recycler.Handle handle)
/* 963:    */     {
/* 964:903 */       super();
/* 965:    */     }
/* 966:    */     
/* 967:    */     protected static void init(AbstractWriteTask task, AbstractChannelHandlerContext ctx, Object msg, int size, ChannelPromise promise)
/* 968:    */     {
/* 969:908 */       task.ctx = ctx;
/* 970:909 */       task.msg = msg;
/* 971:910 */       task.promise = promise;
/* 972:911 */       task.size = size;
/* 973:    */     }
/* 974:    */     
/* 975:    */     public final void run()
/* 976:    */     {
/* 977:    */       try
/* 978:    */       {
/* 979:917 */         if (this.size > 0)
/* 980:    */         {
/* 981:918 */           ChannelOutboundBuffer buffer = this.ctx.channel.unsafe().outboundBuffer();
/* 982:920 */           if (buffer != null) {
/* 983:921 */             buffer.decrementPendingOutboundBytes(this.size);
/* 984:    */           }
/* 985:    */         }
/* 986:924 */         write(this.ctx, this.msg, this.promise);
/* 987:    */       }
/* 988:    */       finally
/* 989:    */       {
/* 990:927 */         this.ctx = null;
/* 991:928 */         this.msg = null;
/* 992:929 */         this.promise = null;
/* 993:    */       }
/* 994:    */     }
/* 995:    */     
/* 996:    */     public Runnable value()
/* 997:    */     {
/* 998:935 */       return this;
/* 999:    */     }
/* :00:    */     
/* :01:    */     protected void write(AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* :02:    */     {
/* :03:939 */       ctx.invokeWrite(msg, promise);
/* :04:    */     }
/* :05:    */   }
/* :06:    */   
/* :07:    */   static final class WriteTask
/* :08:    */     extends AbstractChannelHandlerContext.AbstractWriteTask
/* :09:    */     implements SingleThreadEventLoop.NonWakeupRunnable
/* :10:    */   {
/* :11:945 */     private static final Recycler<WriteTask> RECYCLER = new Recycler()
/* :12:    */     {
/* :13:    */       protected AbstractChannelHandlerContext.WriteTask newObject(Recycler.Handle handle)
/* :14:    */       {
/* :15:948 */         return new AbstractChannelHandlerContext.WriteTask(handle, null);
/* :16:    */       }
/* :17:    */     };
/* :18:    */     
/* :19:    */     private static WriteTask newInstance(AbstractChannelHandlerContext ctx, Object msg, int size, ChannelPromise promise)
/* :20:    */     {
/* :21:954 */       WriteTask task = (WriteTask)RECYCLER.get();
/* :22:955 */       init(task, ctx, msg, size, promise);
/* :23:956 */       return task;
/* :24:    */     }
/* :25:    */     
/* :26:    */     private WriteTask(Recycler.Handle handle)
/* :27:    */     {
/* :28:960 */       super(null);
/* :29:    */     }
/* :30:    */     
/* :31:    */     protected void recycle(Recycler.Handle handle)
/* :32:    */     {
/* :33:965 */       RECYCLER.recycle(this, handle);
/* :34:    */     }
/* :35:    */   }
/* :36:    */   
/* :37:    */   static final class WriteAndFlushTask
/* :38:    */     extends AbstractChannelHandlerContext.AbstractWriteTask
/* :39:    */   {
/* :40:971 */     private static final Recycler<WriteAndFlushTask> RECYCLER = new Recycler()
/* :41:    */     {
/* :42:    */       protected AbstractChannelHandlerContext.WriteAndFlushTask newObject(Recycler.Handle handle)
/* :43:    */       {
/* :44:974 */         return new AbstractChannelHandlerContext.WriteAndFlushTask(handle, null);
/* :45:    */       }
/* :46:    */     };
/* :47:    */     
/* :48:    */     private static WriteAndFlushTask newInstance(AbstractChannelHandlerContext ctx, Object msg, int size, ChannelPromise promise)
/* :49:    */     {
/* :50:980 */       WriteAndFlushTask task = (WriteAndFlushTask)RECYCLER.get();
/* :51:981 */       init(task, ctx, msg, size, promise);
/* :52:982 */       return task;
/* :53:    */     }
/* :54:    */     
/* :55:    */     private WriteAndFlushTask(Recycler.Handle handle)
/* :56:    */     {
/* :57:986 */       super(null);
/* :58:    */     }
/* :59:    */     
/* :60:    */     public void write(AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* :61:    */     {
/* :62:991 */       super.write(ctx, msg, promise);
/* :63:992 */       ctx.invokeFlush();
/* :64:    */     }
/* :65:    */     
/* :66:    */     protected void recycle(Recycler.Handle handle)
/* :67:    */     {
/* :68:997 */       RECYCLER.recycle(this, handle);
/* :69:    */     }
/* :70:    */   }
/* :71:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.AbstractChannelHandlerContext
 * JD-Core Version:    0.7.0.1
 */