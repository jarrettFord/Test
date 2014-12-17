/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.Signal;
/*   4:    */ import io.netty.util.internal.EmptyArrays;
/*   5:    */ import io.netty.util.internal.InternalThreadLocalMap;
/*   6:    */ import io.netty.util.internal.PlatformDependent;
/*   7:    */ import io.netty.util.internal.StringUtil;
/*   8:    */ import io.netty.util.internal.logging.InternalLogger;
/*   9:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  10:    */ import java.util.ArrayDeque;
/*  11:    */ import java.util.concurrent.CancellationException;
/*  12:    */ import java.util.concurrent.TimeUnit;
/*  13:    */ 
/*  14:    */ public class DefaultPromise<V>
/*  15:    */   extends AbstractFuture<V>
/*  16:    */   implements Promise<V>
/*  17:    */ {
/*  18: 34 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultPromise.class);
/*  19: 35 */   private static final InternalLogger rejectedExecutionLogger = InternalLoggerFactory.getInstance(DefaultPromise.class.getName() + ".rejectedExecution");
/*  20:    */   private static final int MAX_LISTENER_STACK_DEPTH = 8;
/*  21: 39 */   private static final Signal SUCCESS = Signal.valueOf(DefaultPromise.class.getName() + ".SUCCESS");
/*  22: 40 */   private static final Signal UNCANCELLABLE = Signal.valueOf(DefaultPromise.class.getName() + ".UNCANCELLABLE");
/*  23: 41 */   private static final CauseHolder CANCELLATION_CAUSE_HOLDER = new CauseHolder(new CancellationException(), null);
/*  24:    */   private final EventExecutor executor;
/*  25:    */   private volatile Object result;
/*  26:    */   private Object listeners;
/*  27:    */   private DefaultPromise<V>.LateListeners lateListeners;
/*  28:    */   private short waiters;
/*  29:    */   
/*  30:    */   static
/*  31:    */   {
/*  32: 44 */     CANCELLATION_CAUSE_HOLDER.cause.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public DefaultPromise(EventExecutor executor)
/*  36:    */   {
/*  37: 75 */     if (executor == null) {
/*  38: 76 */       throw new NullPointerException("executor");
/*  39:    */     }
/*  40: 78 */     this.executor = executor;
/*  41:    */   }
/*  42:    */   
/*  43:    */   protected DefaultPromise()
/*  44:    */   {
/*  45: 83 */     this.executor = null;
/*  46:    */   }
/*  47:    */   
/*  48:    */   protected EventExecutor executor()
/*  49:    */   {
/*  50: 87 */     return this.executor;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public boolean isCancelled()
/*  54:    */   {
/*  55: 92 */     return isCancelled0(this.result);
/*  56:    */   }
/*  57:    */   
/*  58:    */   private static boolean isCancelled0(Object result)
/*  59:    */   {
/*  60: 96 */     return ((result instanceof CauseHolder)) && ((((CauseHolder)result).cause instanceof CancellationException));
/*  61:    */   }
/*  62:    */   
/*  63:    */   public boolean isCancellable()
/*  64:    */   {
/*  65:101 */     return this.result == null;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public boolean isDone()
/*  69:    */   {
/*  70:106 */     return isDone0(this.result);
/*  71:    */   }
/*  72:    */   
/*  73:    */   private static boolean isDone0(Object result)
/*  74:    */   {
/*  75:110 */     return (result != null) && (result != UNCANCELLABLE);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public boolean isSuccess()
/*  79:    */   {
/*  80:115 */     Object result = this.result;
/*  81:116 */     if ((result == null) || (result == UNCANCELLABLE)) {
/*  82:117 */       return false;
/*  83:    */     }
/*  84:119 */     return !(result instanceof CauseHolder);
/*  85:    */   }
/*  86:    */   
/*  87:    */   public Throwable cause()
/*  88:    */   {
/*  89:124 */     Object result = this.result;
/*  90:125 */     if ((result instanceof CauseHolder)) {
/*  91:126 */       return ((CauseHolder)result).cause;
/*  92:    */     }
/*  93:128 */     return null;
/*  94:    */   }
/*  95:    */   
/*  96:    */   public Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener)
/*  97:    */   {
/*  98:133 */     if (listener == null) {
/*  99:134 */       throw new NullPointerException("listener");
/* 100:    */     }
/* 101:137 */     if (isDone())
/* 102:    */     {
/* 103:138 */       notifyLateListener(listener);
/* 104:139 */       return this;
/* 105:    */     }
/* 106:142 */     synchronized (this)
/* 107:    */     {
/* 108:143 */       if (!isDone())
/* 109:    */       {
/* 110:144 */         if (this.listeners == null)
/* 111:    */         {
/* 112:145 */           this.listeners = listener;
/* 113:    */         }
/* 114:147 */         else if ((this.listeners instanceof DefaultFutureListeners))
/* 115:    */         {
/* 116:148 */           ((DefaultFutureListeners)this.listeners).add(listener);
/* 117:    */         }
/* 118:    */         else
/* 119:    */         {
/* 120:151 */           GenericFutureListener<? extends Future<V>> firstListener = (GenericFutureListener)this.listeners;
/* 121:    */           
/* 122:153 */           this.listeners = new DefaultFutureListeners(firstListener, listener);
/* 123:    */         }
/* 124:156 */         return this;
/* 125:    */       }
/* 126:    */     }
/* 127:160 */     notifyLateListener(listener);
/* 128:161 */     return this;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public Promise<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listeners)
/* 132:    */   {
/* 133:166 */     if (listeners == null) {
/* 134:167 */       throw new NullPointerException("listeners");
/* 135:    */     }
/* 136:170 */     for (GenericFutureListener<? extends Future<? super V>> l : listeners)
/* 137:    */     {
/* 138:171 */       if (l == null) {
/* 139:    */         break;
/* 140:    */       }
/* 141:174 */       addListener(l);
/* 142:    */     }
/* 143:176 */     return this;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public Promise<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener)
/* 147:    */   {
/* 148:181 */     if (listener == null) {
/* 149:182 */       throw new NullPointerException("listener");
/* 150:    */     }
/* 151:185 */     if (isDone()) {
/* 152:186 */       return this;
/* 153:    */     }
/* 154:189 */     synchronized (this)
/* 155:    */     {
/* 156:190 */       if (!isDone()) {
/* 157:191 */         if ((this.listeners instanceof DefaultFutureListeners)) {
/* 158:192 */           ((DefaultFutureListeners)this.listeners).remove(listener);
/* 159:193 */         } else if (this.listeners == listener) {
/* 160:194 */           this.listeners = null;
/* 161:    */         }
/* 162:    */       }
/* 163:    */     }
/* 164:199 */     return this;
/* 165:    */   }
/* 166:    */   
/* 167:    */   public Promise<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listeners)
/* 168:    */   {
/* 169:204 */     if (listeners == null) {
/* 170:205 */       throw new NullPointerException("listeners");
/* 171:    */     }
/* 172:208 */     for (GenericFutureListener<? extends Future<? super V>> l : listeners)
/* 173:    */     {
/* 174:209 */       if (l == null) {
/* 175:    */         break;
/* 176:    */       }
/* 177:212 */       removeListener(l);
/* 178:    */     }
/* 179:214 */     return this;
/* 180:    */   }
/* 181:    */   
/* 182:    */   public Promise<V> sync()
/* 183:    */     throws InterruptedException
/* 184:    */   {
/* 185:219 */     await();
/* 186:220 */     rethrowIfFailed();
/* 187:221 */     return this;
/* 188:    */   }
/* 189:    */   
/* 190:    */   public Promise<V> syncUninterruptibly()
/* 191:    */   {
/* 192:226 */     awaitUninterruptibly();
/* 193:227 */     rethrowIfFailed();
/* 194:228 */     return this;
/* 195:    */   }
/* 196:    */   
/* 197:    */   private void rethrowIfFailed()
/* 198:    */   {
/* 199:232 */     Throwable cause = cause();
/* 200:233 */     if (cause == null) {
/* 201:234 */       return;
/* 202:    */     }
/* 203:237 */     PlatformDependent.throwException(cause);
/* 204:    */   }
/* 205:    */   
/* 206:    */   public Promise<V> await()
/* 207:    */     throws InterruptedException
/* 208:    */   {
/* 209:242 */     if (isDone()) {
/* 210:243 */       return this;
/* 211:    */     }
/* 212:246 */     if (Thread.interrupted()) {
/* 213:247 */       throw new InterruptedException(toString());
/* 214:    */     }
/* 215:250 */     synchronized (this)
/* 216:    */     {
/* 217:251 */       while (!isDone())
/* 218:    */       {
/* 219:252 */         checkDeadLock();
/* 220:253 */         incWaiters();
/* 221:    */         try
/* 222:    */         {
/* 223:255 */           wait();
/* 224:    */         }
/* 225:    */         finally
/* 226:    */         {
/* 227:257 */           decWaiters();
/* 228:    */         }
/* 229:    */       }
/* 230:    */     }
/* 231:261 */     return this;
/* 232:    */   }
/* 233:    */   
/* 234:    */   public boolean await(long timeout, TimeUnit unit)
/* 235:    */     throws InterruptedException
/* 236:    */   {
/* 237:267 */     return await0(unit.toNanos(timeout), true);
/* 238:    */   }
/* 239:    */   
/* 240:    */   public boolean await(long timeoutMillis)
/* 241:    */     throws InterruptedException
/* 242:    */   {
/* 243:272 */     return await0(TimeUnit.MILLISECONDS.toNanos(timeoutMillis), true);
/* 244:    */   }
/* 245:    */   
/* 246:    */   public Promise<V> awaitUninterruptibly()
/* 247:    */   {
/* 248:277 */     if (isDone()) {
/* 249:278 */       return this;
/* 250:    */     }
/* 251:281 */     boolean interrupted = false;
/* 252:282 */     synchronized (this)
/* 253:    */     {
/* 254:283 */       while (!isDone())
/* 255:    */       {
/* 256:284 */         checkDeadLock();
/* 257:285 */         incWaiters();
/* 258:    */         try
/* 259:    */         {
/* 260:287 */           wait();
/* 261:    */         }
/* 262:    */         catch (InterruptedException e)
/* 263:    */         {
/* 264:290 */           interrupted = true;
/* 265:    */         }
/* 266:    */         finally
/* 267:    */         {
/* 268:292 */           decWaiters();
/* 269:    */         }
/* 270:    */       }
/* 271:    */     }
/* 272:297 */     if (interrupted) {
/* 273:298 */       Thread.currentThread().interrupt();
/* 274:    */     }
/* 275:301 */     return this;
/* 276:    */   }
/* 277:    */   
/* 278:    */   public boolean awaitUninterruptibly(long timeout, TimeUnit unit)
/* 279:    */   {
/* 280:    */     try
/* 281:    */     {
/* 282:307 */       return await0(unit.toNanos(timeout), false);
/* 283:    */     }
/* 284:    */     catch (InterruptedException e)
/* 285:    */     {
/* 286:310 */       throw new InternalError();
/* 287:    */     }
/* 288:    */   }
/* 289:    */   
/* 290:    */   public boolean awaitUninterruptibly(long timeoutMillis)
/* 291:    */   {
/* 292:    */     try
/* 293:    */     {
/* 294:317 */       return await0(TimeUnit.MILLISECONDS.toNanos(timeoutMillis), false);
/* 295:    */     }
/* 296:    */     catch (InterruptedException e)
/* 297:    */     {
/* 298:320 */       throw new InternalError();
/* 299:    */     }
/* 300:    */   }
/* 301:    */   
/* 302:    */   protected void checkDeadLock()
/* 303:    */   {
/* 304:389 */     EventExecutor e = executor();
/* 305:390 */     if ((e != null) && (e.inEventLoop())) {
/* 306:391 */       throw new BlockingOperationException(toString());
/* 307:    */     }
/* 308:    */   }
/* 309:    */   
/* 310:    */   public Promise<V> setSuccess(V result)
/* 311:    */   {
/* 312:397 */     if (setSuccess0(result))
/* 313:    */     {
/* 314:398 */       notifyListeners();
/* 315:399 */       return this;
/* 316:    */     }
/* 317:401 */     throw new IllegalStateException("complete already: " + this);
/* 318:    */   }
/* 319:    */   
/* 320:    */   public boolean trySuccess(V result)
/* 321:    */   {
/* 322:406 */     if (setSuccess0(result))
/* 323:    */     {
/* 324:407 */       notifyListeners();
/* 325:408 */       return true;
/* 326:    */     }
/* 327:410 */     return false;
/* 328:    */   }
/* 329:    */   
/* 330:    */   public Promise<V> setFailure(Throwable cause)
/* 331:    */   {
/* 332:415 */     if (setFailure0(cause))
/* 333:    */     {
/* 334:416 */       notifyListeners();
/* 335:417 */       return this;
/* 336:    */     }
/* 337:419 */     throw new IllegalStateException("complete already: " + this, cause);
/* 338:    */   }
/* 339:    */   
/* 340:    */   public boolean tryFailure(Throwable cause)
/* 341:    */   {
/* 342:424 */     if (setFailure0(cause))
/* 343:    */     {
/* 344:425 */       notifyListeners();
/* 345:426 */       return true;
/* 346:    */     }
/* 347:428 */     return false;
/* 348:    */   }
/* 349:    */   
/* 350:    */   public boolean cancel(boolean mayInterruptIfRunning)
/* 351:    */   {
/* 352:433 */     Object result = this.result;
/* 353:434 */     if ((isDone0(result)) || (result == UNCANCELLABLE)) {
/* 354:435 */       return false;
/* 355:    */     }
/* 356:438 */     synchronized (this)
/* 357:    */     {
/* 358:440 */       result = this.result;
/* 359:441 */       if ((isDone0(result)) || (result == UNCANCELLABLE)) {
/* 360:442 */         return false;
/* 361:    */       }
/* 362:445 */       this.result = CANCELLATION_CAUSE_HOLDER;
/* 363:446 */       if (hasWaiters()) {
/* 364:447 */         notifyAll();
/* 365:    */       }
/* 366:    */     }
/* 367:451 */     notifyListeners();
/* 368:452 */     return true;
/* 369:    */   }
/* 370:    */   
/* 371:    */   public boolean setUncancellable()
/* 372:    */   {
/* 373:457 */     Object result = this.result;
/* 374:458 */     if (isDone0(result)) {
/* 375:459 */       return !isCancelled0(result);
/* 376:    */     }
/* 377:462 */     synchronized (this)
/* 378:    */     {
/* 379:464 */       result = this.result;
/* 380:465 */       if (isDone0(result)) {
/* 381:466 */         return !isCancelled0(result);
/* 382:    */       }
/* 383:469 */       this.result = UNCANCELLABLE;
/* 384:    */     }
/* 385:471 */     return true;
/* 386:    */   }
/* 387:    */   
/* 388:    */   private boolean setFailure0(Throwable cause)
/* 389:    */   {
/* 390:475 */     if (isDone()) {
/* 391:476 */       return false;
/* 392:    */     }
/* 393:479 */     synchronized (this)
/* 394:    */     {
/* 395:481 */       if (isDone()) {
/* 396:482 */         return false;
/* 397:    */       }
/* 398:485 */       this.result = new CauseHolder(cause, null);
/* 399:486 */       if (hasWaiters()) {
/* 400:487 */         notifyAll();
/* 401:    */       }
/* 402:    */     }
/* 403:490 */     return true;
/* 404:    */   }
/* 405:    */   
/* 406:    */   private boolean setSuccess0(V result)
/* 407:    */   {
/* 408:494 */     if (isDone()) {
/* 409:495 */       return false;
/* 410:    */     }
/* 411:498 */     synchronized (this)
/* 412:    */     {
/* 413:500 */       if (isDone()) {
/* 414:501 */         return false;
/* 415:    */       }
/* 416:503 */       if (result == null) {
/* 417:504 */         this.result = SUCCESS;
/* 418:    */       } else {
/* 419:506 */         this.result = result;
/* 420:    */       }
/* 421:508 */       if (hasWaiters()) {
/* 422:509 */         notifyAll();
/* 423:    */       }
/* 424:    */     }
/* 425:512 */     return true;
/* 426:    */   }
/* 427:    */   
/* 428:    */   public V getNow()
/* 429:    */   {
/* 430:518 */     Object result = this.result;
/* 431:519 */     if (((result instanceof CauseHolder)) || (result == SUCCESS)) {
/* 432:520 */       return null;
/* 433:    */     }
/* 434:522 */     return result;
/* 435:    */   }
/* 436:    */   
/* 437:    */   private boolean hasWaiters()
/* 438:    */   {
/* 439:526 */     return this.waiters > 0;
/* 440:    */   }
/* 441:    */   
/* 442:    */   private void incWaiters()
/* 443:    */   {
/* 444:530 */     if (this.waiters == 32767) {
/* 445:531 */       throw new IllegalStateException("too many waiters: " + this);
/* 446:    */     }
/* 447:533 */     this.waiters = ((short)(this.waiters + 1));
/* 448:    */   }
/* 449:    */   
/* 450:    */   private void decWaiters()
/* 451:    */   {
/* 452:537 */     this.waiters = ((short)(this.waiters - 1));
/* 453:    */   }
/* 454:    */   
/* 455:    */   private void notifyListeners()
/* 456:    */   {
/* 457:547 */     Object listeners = this.listeners;
/* 458:548 */     if (listeners == null) {
/* 459:549 */       return;
/* 460:    */     }
/* 461:552 */     EventExecutor executor = executor();
/* 462:553 */     if (executor.inEventLoop())
/* 463:    */     {
/* 464:554 */       InternalThreadLocalMap threadLocals = InternalThreadLocalMap.get();
/* 465:555 */       int stackDepth = threadLocals.futureListenerStackDepth();
/* 466:556 */       if (stackDepth < 8)
/* 467:    */       {
/* 468:557 */         threadLocals.setFutureListenerStackDepth(stackDepth + 1);
/* 469:    */         try
/* 470:    */         {
/* 471:559 */           if ((listeners instanceof DefaultFutureListeners))
/* 472:    */           {
/* 473:560 */             notifyListeners0(this, (DefaultFutureListeners)listeners);
/* 474:    */           }
/* 475:    */           else
/* 476:    */           {
/* 477:563 */             GenericFutureListener<? extends Future<V>> l = (GenericFutureListener)listeners;
/* 478:    */             
/* 479:565 */             notifyListener0(this, l);
/* 480:    */           }
/* 481:    */         }
/* 482:    */         finally
/* 483:    */         {
/* 484:568 */           this.listeners = null;
/* 485:569 */           threadLocals.setFutureListenerStackDepth(stackDepth);
/* 486:    */         }
/* 487:571 */         return;
/* 488:    */       }
/* 489:    */     }
/* 490:575 */     if ((listeners instanceof DefaultFutureListeners))
/* 491:    */     {
/* 492:576 */       final DefaultFutureListeners dfl = (DefaultFutureListeners)listeners;
/* 493:577 */       execute(executor, new Runnable()
/* 494:    */       {
/* 495:    */         public void run()
/* 496:    */         {
/* 497:580 */           DefaultPromise.notifyListeners0(DefaultPromise.this, dfl);
/* 498:581 */           DefaultPromise.this.listeners = null;
/* 499:    */         }
/* 500:    */       });
/* 501:    */     }
/* 502:    */     else
/* 503:    */     {
/* 504:586 */       final GenericFutureListener<? extends Future<V>> l = (GenericFutureListener)listeners;
/* 505:    */       
/* 506:588 */       execute(executor, new Runnable()
/* 507:    */       {
/* 508:    */         public void run()
/* 509:    */         {
/* 510:591 */           DefaultPromise.notifyListener0(DefaultPromise.this, l);
/* 511:592 */           DefaultPromise.this.listeners = null;
/* 512:    */         }
/* 513:    */       });
/* 514:    */     }
/* 515:    */   }
/* 516:    */   
/* 517:    */   private static void notifyListeners0(Future<?> future, DefaultFutureListeners listeners)
/* 518:    */   {
/* 519:599 */     GenericFutureListener<?>[] a = listeners.listeners();
/* 520:600 */     int size = listeners.size();
/* 521:601 */     for (int i = 0; i < size; i++) {
/* 522:602 */       notifyListener0(future, a[i]);
/* 523:    */     }
/* 524:    */   }
/* 525:    */   
/* 526:    */   private void notifyLateListener(GenericFutureListener<?> l)
/* 527:    */   {
/* 528:612 */     EventExecutor executor = executor();
/* 529:613 */     if (executor.inEventLoop()) {
/* 530:614 */       if ((this.listeners == null) && (this.lateListeners == null))
/* 531:    */       {
/* 532:615 */         InternalThreadLocalMap threadLocals = InternalThreadLocalMap.get();
/* 533:616 */         int stackDepth = threadLocals.futureListenerStackDepth();
/* 534:617 */         if (stackDepth < 8)
/* 535:    */         {
/* 536:618 */           threadLocals.setFutureListenerStackDepth(stackDepth + 1);
/* 537:    */           try
/* 538:    */           {
/* 539:620 */             notifyListener0(this, l);
/* 540:    */           }
/* 541:    */           finally
/* 542:    */           {
/* 543:622 */             threadLocals.setFutureListenerStackDepth(stackDepth);
/* 544:    */           }
/* 545:624 */           return;
/* 546:    */         }
/* 547:    */       }
/* 548:    */       else
/* 549:    */       {
/* 550:627 */         DefaultPromise<V>.LateListeners lateListeners = this.lateListeners;
/* 551:628 */         if (lateListeners == null) {
/* 552:629 */           this.lateListeners = (lateListeners = new LateListeners());
/* 553:    */         }
/* 554:631 */         lateListeners.add(l);
/* 555:632 */         execute(executor, lateListeners);
/* 556:633 */         return;
/* 557:    */       }
/* 558:    */     }
/* 559:640 */     execute(executor, new LateListenerNotifier(l));
/* 560:    */   }
/* 561:    */   
/* 562:    */   protected static void notifyListener(EventExecutor eventExecutor, Future<?> future, final GenericFutureListener<?> l)
/* 563:    */   {
/* 564:646 */     if (eventExecutor.inEventLoop())
/* 565:    */     {
/* 566:647 */       InternalThreadLocalMap threadLocals = InternalThreadLocalMap.get();
/* 567:648 */       int stackDepth = threadLocals.futureListenerStackDepth();
/* 568:649 */       if (stackDepth < 8)
/* 569:    */       {
/* 570:650 */         threadLocals.setFutureListenerStackDepth(stackDepth + 1);
/* 571:    */         try
/* 572:    */         {
/* 573:652 */           notifyListener0(future, l);
/* 574:    */         }
/* 575:    */         finally
/* 576:    */         {
/* 577:654 */           threadLocals.setFutureListenerStackDepth(stackDepth);
/* 578:    */         }
/* 579:656 */         return;
/* 580:    */       }
/* 581:    */     }
/* 582:660 */     execute(eventExecutor, new Runnable()
/* 583:    */     {
/* 584:    */       public void run()
/* 585:    */       {
/* 586:663 */         DefaultPromise.notifyListener0(this.val$future, l);
/* 587:    */       }
/* 588:    */     });
/* 589:    */   }
/* 590:    */   
/* 591:    */   private static void execute(EventExecutor executor, Runnable task)
/* 592:    */   {
/* 593:    */     try
/* 594:    */     {
/* 595:670 */       executor.execute(task);
/* 596:    */     }
/* 597:    */     catch (Throwable t)
/* 598:    */     {
/* 599:672 */       rejectedExecutionLogger.error("Failed to submit a listener notification task. Event loop shut down?", t);
/* 600:    */     }
/* 601:    */   }
/* 602:    */   
/* 603:    */   static void notifyListener0(Future future, GenericFutureListener l)
/* 604:    */   {
/* 605:    */     try
/* 606:    */     {
/* 607:679 */       l.operationComplete(future);
/* 608:    */     }
/* 609:    */     catch (Throwable t)
/* 610:    */     {
/* 611:681 */       if (logger.isWarnEnabled()) {
/* 612:682 */         logger.warn("An exception was thrown by " + l.getClass().getName() + ".operationComplete()", t);
/* 613:    */       }
/* 614:    */     }
/* 615:    */   }
/* 616:    */   
/* 617:    */   private synchronized Object progressiveListeners()
/* 618:    */   {
/* 619:692 */     Object listeners = this.listeners;
/* 620:693 */     if (listeners == null) {
/* 621:695 */       return null;
/* 622:    */     }
/* 623:698 */     if ((listeners instanceof DefaultFutureListeners))
/* 624:    */     {
/* 625:700 */       DefaultFutureListeners dfl = (DefaultFutureListeners)listeners;
/* 626:701 */       int progressiveSize = dfl.progressiveSize();
/* 627:702 */       switch (progressiveSize)
/* 628:    */       {
/* 629:    */       case 0: 
/* 630:704 */         return null;
/* 631:    */       case 1: 
/* 632:706 */         for (GenericFutureListener<?> l : dfl.listeners()) {
/* 633:707 */           if ((l instanceof GenericProgressiveFutureListener)) {
/* 634:708 */             return l;
/* 635:    */           }
/* 636:    */         }
/* 637:711 */         return null;
/* 638:    */       }
/* 639:714 */       GenericFutureListener<?>[] array = dfl.listeners();
/* 640:715 */       GenericProgressiveFutureListener<?>[] copy = new GenericProgressiveFutureListener[progressiveSize];
/* 641:716 */       int i = 0;
/* 642:716 */       for (int j = 0; j < progressiveSize; i++)
/* 643:    */       {
/* 644:717 */         GenericFutureListener<?> l = array[i];
/* 645:718 */         if ((l instanceof GenericProgressiveFutureListener)) {
/* 646:719 */           copy[(j++)] = ((GenericProgressiveFutureListener)l);
/* 647:    */         }
/* 648:    */       }
/* 649:723 */       return copy;
/* 650:    */     }
/* 651:724 */     if ((listeners instanceof GenericProgressiveFutureListener)) {
/* 652:725 */       return listeners;
/* 653:    */     }
/* 654:728 */     return null;
/* 655:    */   }
/* 656:    */   
/* 657:    */   void notifyProgressiveListeners(final long progress, long total)
/* 658:    */   {
/* 659:734 */     Object listeners = progressiveListeners();
/* 660:735 */     if (listeners == null) {
/* 661:736 */       return;
/* 662:    */     }
/* 663:739 */     final ProgressiveFuture<V> self = (ProgressiveFuture)this;
/* 664:    */     
/* 665:741 */     EventExecutor executor = executor();
/* 666:742 */     if (executor.inEventLoop())
/* 667:    */     {
/* 668:743 */       if ((listeners instanceof GenericProgressiveFutureListener[])) {
/* 669:744 */         notifyProgressiveListeners0(self, (GenericProgressiveFutureListener[])listeners, progress, total);
/* 670:    */       } else {
/* 671:747 */         notifyProgressiveListener0(self, (GenericProgressiveFutureListener)listeners, progress, total);
/* 672:    */       }
/* 673:    */     }
/* 674:751 */     else if ((listeners instanceof GenericProgressiveFutureListener[]))
/* 675:    */     {
/* 676:752 */       final GenericProgressiveFutureListener<?>[] array = (GenericProgressiveFutureListener[])listeners;
/* 677:    */       
/* 678:754 */       execute(executor, new Runnable()
/* 679:    */       {
/* 680:    */         public void run()
/* 681:    */         {
/* 682:757 */           DefaultPromise.notifyProgressiveListeners0(self, array, progress, this.val$total);
/* 683:    */         }
/* 684:    */       });
/* 685:    */     }
/* 686:    */     else
/* 687:    */     {
/* 688:761 */       final GenericProgressiveFutureListener<ProgressiveFuture<V>> l = (GenericProgressiveFutureListener)listeners;
/* 689:    */       
/* 690:763 */       execute(executor, new Runnable()
/* 691:    */       {
/* 692:    */         public void run()
/* 693:    */         {
/* 694:766 */           DefaultPromise.notifyProgressiveListener0(self, l, progress, this.val$total);
/* 695:    */         }
/* 696:    */       });
/* 697:    */     }
/* 698:    */   }
/* 699:    */   
/* 700:    */   private static void notifyProgressiveListeners0(ProgressiveFuture<?> future, GenericProgressiveFutureListener<?>[] listeners, long progress, long total)
/* 701:    */   {
/* 702:775 */     for (GenericProgressiveFutureListener<?> l : listeners)
/* 703:    */     {
/* 704:776 */       if (l == null) {
/* 705:    */         break;
/* 706:    */       }
/* 707:779 */       notifyProgressiveListener0(future, l, progress, total);
/* 708:    */     }
/* 709:    */   }
/* 710:    */   
/* 711:    */   private static void notifyProgressiveListener0(ProgressiveFuture future, GenericProgressiveFutureListener l, long progress, long total)
/* 712:    */   {
/* 713:    */     try
/* 714:    */     {
/* 715:787 */       l.operationProgressed(future, progress, total);
/* 716:    */     }
/* 717:    */     catch (Throwable t)
/* 718:    */     {
/* 719:789 */       if (logger.isWarnEnabled()) {
/* 720:790 */         logger.warn("An exception was thrown by " + l.getClass().getName() + ".operationProgressed()", t);
/* 721:    */       }
/* 722:    */     }
/* 723:    */   }
/* 724:    */   
/* 725:    */   private static final class CauseHolder
/* 726:    */   {
/* 727:    */     final Throwable cause;
/* 728:    */     
/* 729:    */     private CauseHolder(Throwable cause)
/* 730:    */     {
/* 731:798 */       this.cause = cause;
/* 732:    */     }
/* 733:    */   }
/* 734:    */   
/* 735:    */   public String toString()
/* 736:    */   {
/* 737:804 */     return toStringBuilder().toString();
/* 738:    */   }
/* 739:    */   
/* 740:    */   protected StringBuilder toStringBuilder()
/* 741:    */   {
/* 742:808 */     StringBuilder buf = new StringBuilder(64);
/* 743:809 */     buf.append(StringUtil.simpleClassName(this));
/* 744:810 */     buf.append('@');
/* 745:811 */     buf.append(Integer.toHexString(hashCode()));
/* 746:    */     
/* 747:813 */     Object result = this.result;
/* 748:814 */     if (result == SUCCESS)
/* 749:    */     {
/* 750:815 */       buf.append("(success)");
/* 751:    */     }
/* 752:816 */     else if (result == UNCANCELLABLE)
/* 753:    */     {
/* 754:817 */       buf.append("(uncancellable)");
/* 755:    */     }
/* 756:818 */     else if ((result instanceof CauseHolder))
/* 757:    */     {
/* 758:819 */       buf.append("(failure(");
/* 759:820 */       buf.append(((CauseHolder)result).cause);
/* 760:821 */       buf.append(')');
/* 761:    */     }
/* 762:    */     else
/* 763:    */     {
/* 764:823 */       buf.append("(incomplete)");
/* 765:    */     }
/* 766:825 */     return buf;
/* 767:    */   }
/* 768:    */   
/* 769:    */   /* Error */
/* 770:    */   private boolean await0(long timeoutNanos, boolean interruptable)
/* 771:    */     throws InterruptedException
/* 772:    */   {
/* 773:    */     // Byte code:
/* 774:    */     //   0: aload_0
/* 775:    */     //   1: invokevirtual 20	io/netty/util/concurrent/DefaultPromise:isDone	()Z
/* 776:    */     //   4: ifeq +5 -> 9
/* 777:    */     //   7: iconst_1
/* 778:    */     //   8: ireturn
/* 779:    */     //   9: lload_1
/* 780:    */     //   10: lconst_0
/* 781:    */     //   11: lcmp
/* 782:    */     //   12: ifgt +8 -> 20
/* 783:    */     //   15: aload_0
/* 784:    */     //   16: invokevirtual 20	io/netty/util/concurrent/DefaultPromise:isDone	()Z
/* 785:    */     //   19: ireturn
/* 786:    */     //   20: iload_3
/* 787:    */     //   21: ifeq +21 -> 42
/* 788:    */     //   24: invokestatic 35	java/lang/Thread:interrupted	()Z
/* 789:    */     //   27: ifeq +15 -> 42
/* 790:    */     //   30: new 36	java/lang/InterruptedException
/* 791:    */     //   33: dup
/* 792:    */     //   34: aload_0
/* 793:    */     //   35: invokevirtual 37	io/netty/util/concurrent/DefaultPromise:toString	()Ljava/lang/String;
/* 794:    */     //   38: invokespecial 38	java/lang/InterruptedException:<init>	(Ljava/lang/String;)V
/* 795:    */     //   41: athrow
/* 796:    */     //   42: invokestatic 50	java/lang/System:nanoTime	()J
/* 797:    */     //   45: lstore 4
/* 798:    */     //   47: lload_1
/* 799:    */     //   48: lstore 6
/* 800:    */     //   50: iconst_0
/* 801:    */     //   51: istore 8
/* 802:    */     //   53: aload_0
/* 803:    */     //   54: dup
/* 804:    */     //   55: astore 9
/* 805:    */     //   57: monitorenter
/* 806:    */     //   58: aload_0
/* 807:    */     //   59: invokevirtual 20	io/netty/util/concurrent/DefaultPromise:isDone	()Z
/* 808:    */     //   62: ifeq +23 -> 85
/* 809:    */     //   65: iconst_1
/* 810:    */     //   66: istore 10
/* 811:    */     //   68: aload 9
/* 812:    */     //   70: monitorexit
/* 813:    */     //   71: iload 8
/* 814:    */     //   73: ifeq +9 -> 82
/* 815:    */     //   76: invokestatic 46	java/lang/Thread:currentThread	()Ljava/lang/Thread;
/* 816:    */     //   79: invokevirtual 47	java/lang/Thread:interrupt	()V
/* 817:    */     //   82: iload 10
/* 818:    */     //   84: ireturn
/* 819:    */     //   85: lload 6
/* 820:    */     //   87: lconst_0
/* 821:    */     //   88: lcmp
/* 822:    */     //   89: ifgt +26 -> 115
/* 823:    */     //   92: aload_0
/* 824:    */     //   93: invokevirtual 20	io/netty/util/concurrent/DefaultPromise:isDone	()Z
/* 825:    */     //   96: istore 10
/* 826:    */     //   98: aload 9
/* 827:    */     //   100: monitorexit
/* 828:    */     //   101: iload 8
/* 829:    */     //   103: ifeq +9 -> 112
/* 830:    */     //   106: invokestatic 46	java/lang/Thread:currentThread	()Ljava/lang/Thread;
/* 831:    */     //   109: invokevirtual 47	java/lang/Thread:interrupt	()V
/* 832:    */     //   112: iload 10
/* 833:    */     //   114: ireturn
/* 834:    */     //   115: aload_0
/* 835:    */     //   116: invokevirtual 39	io/netty/util/concurrent/DefaultPromise:checkDeadLock	()V
/* 836:    */     //   119: aload_0
/* 837:    */     //   120: invokespecial 40	io/netty/util/concurrent/DefaultPromise:incWaiters	()V
/* 838:    */     //   123: aload_0
/* 839:    */     //   124: lload 6
/* 840:    */     //   126: ldc2_w 51
/* 841:    */     //   129: ldiv
/* 842:    */     //   130: lload 6
/* 843:    */     //   132: ldc2_w 51
/* 844:    */     //   135: lrem
/* 845:    */     //   136: l2i
/* 846:    */     //   137: invokevirtual 53	java/lang/Object:wait	(JI)V
/* 847:    */     //   140: goto +15 -> 155
/* 848:    */     //   143: astore 10
/* 849:    */     //   145: iload_3
/* 850:    */     //   146: ifeq +6 -> 152
/* 851:    */     //   149: aload 10
/* 852:    */     //   151: athrow
/* 853:    */     //   152: iconst_1
/* 854:    */     //   153: istore 8
/* 855:    */     //   155: aload_0
/* 856:    */     //   156: invokevirtual 20	io/netty/util/concurrent/DefaultPromise:isDone	()Z
/* 857:    */     //   159: ifeq +27 -> 186
/* 858:    */     //   162: iconst_1
/* 859:    */     //   163: istore 10
/* 860:    */     //   165: aload_0
/* 861:    */     //   166: invokespecial 42	io/netty/util/concurrent/DefaultPromise:decWaiters	()V
/* 862:    */     //   169: aload 9
/* 863:    */     //   171: monitorexit
/* 864:    */     //   172: iload 8
/* 865:    */     //   174: ifeq +9 -> 183
/* 866:    */     //   177: invokestatic 46	java/lang/Thread:currentThread	()Ljava/lang/Thread;
/* 867:    */     //   180: invokevirtual 47	java/lang/Thread:interrupt	()V
/* 868:    */     //   183: iload 10
/* 869:    */     //   185: ireturn
/* 870:    */     //   186: lload_1
/* 871:    */     //   187: invokestatic 50	java/lang/System:nanoTime	()J
/* 872:    */     //   190: lload 4
/* 873:    */     //   192: lsub
/* 874:    */     //   193: lsub
/* 875:    */     //   194: lstore 6
/* 876:    */     //   196: lload 6
/* 877:    */     //   198: lconst_0
/* 878:    */     //   199: lcmp
/* 879:    */     //   200: ifgt -77 -> 123
/* 880:    */     //   203: aload_0
/* 881:    */     //   204: invokevirtual 20	io/netty/util/concurrent/DefaultPromise:isDone	()Z
/* 882:    */     //   207: istore 10
/* 883:    */     //   209: aload_0
/* 884:    */     //   210: invokespecial 42	io/netty/util/concurrent/DefaultPromise:decWaiters	()V
/* 885:    */     //   213: aload 9
/* 886:    */     //   215: monitorexit
/* 887:    */     //   216: iload 8
/* 888:    */     //   218: ifeq +9 -> 227
/* 889:    */     //   221: invokestatic 46	java/lang/Thread:currentThread	()Ljava/lang/Thread;
/* 890:    */     //   224: invokevirtual 47	java/lang/Thread:interrupt	()V
/* 891:    */     //   227: iload 10
/* 892:    */     //   229: ireturn
/* 893:    */     //   230: astore 11
/* 894:    */     //   232: aload_0
/* 895:    */     //   233: invokespecial 42	io/netty/util/concurrent/DefaultPromise:decWaiters	()V
/* 896:    */     //   236: aload 11
/* 897:    */     //   238: athrow
/* 898:    */     //   239: astore 12
/* 899:    */     //   241: aload 9
/* 900:    */     //   243: monitorexit
/* 901:    */     //   244: aload 12
/* 902:    */     //   246: athrow
/* 903:    */     //   247: astore 13
/* 904:    */     //   249: iload 8
/* 905:    */     //   251: ifeq +9 -> 260
/* 906:    */     //   254: invokestatic 46	java/lang/Thread:currentThread	()Ljava/lang/Thread;
/* 907:    */     //   257: invokevirtual 47	java/lang/Thread:interrupt	()V
/* 908:    */     //   260: aload 13
/* 909:    */     //   262: athrow
/* 910:    */     // Line number table:
/* 911:    */     //   Java source line #325	-> byte code offset #0
/* 912:    */     //   Java source line #326	-> byte code offset #7
/* 913:    */     //   Java source line #329	-> byte code offset #9
/* 914:    */     //   Java source line #330	-> byte code offset #15
/* 915:    */     //   Java source line #333	-> byte code offset #20
/* 916:    */     //   Java source line #334	-> byte code offset #30
/* 917:    */     //   Java source line #337	-> byte code offset #42
/* 918:    */     //   Java source line #338	-> byte code offset #47
/* 919:    */     //   Java source line #339	-> byte code offset #50
/* 920:    */     //   Java source line #342	-> byte code offset #53
/* 921:    */     //   Java source line #343	-> byte code offset #58
/* 922:    */     //   Java source line #344	-> byte code offset #65
/* 923:    */     //   Java source line #379	-> byte code offset #71
/* 924:    */     //   Java source line #380	-> byte code offset #76
/* 925:    */     //   Java source line #347	-> byte code offset #85
/* 926:    */     //   Java source line #348	-> byte code offset #92
/* 927:    */     //   Java source line #379	-> byte code offset #101
/* 928:    */     //   Java source line #380	-> byte code offset #106
/* 929:    */     //   Java source line #351	-> byte code offset #115
/* 930:    */     //   Java source line #352	-> byte code offset #119
/* 931:    */     //   Java source line #356	-> byte code offset #123
/* 932:    */     //   Java source line #363	-> byte code offset #140
/* 933:    */     //   Java source line #357	-> byte code offset #143
/* 934:    */     //   Java source line #358	-> byte code offset #145
/* 935:    */     //   Java source line #359	-> byte code offset #149
/* 936:    */     //   Java source line #361	-> byte code offset #152
/* 937:    */     //   Java source line #365	-> byte code offset #155
/* 938:    */     //   Java source line #366	-> byte code offset #162
/* 939:    */     //   Java source line #375	-> byte code offset #165
/* 940:    */     //   Java source line #379	-> byte code offset #172
/* 941:    */     //   Java source line #380	-> byte code offset #177
/* 942:    */     //   Java source line #368	-> byte code offset #186
/* 943:    */     //   Java source line #369	-> byte code offset #196
/* 944:    */     //   Java source line #370	-> byte code offset #203
/* 945:    */     //   Java source line #375	-> byte code offset #209
/* 946:    */     //   Java source line #379	-> byte code offset #216
/* 947:    */     //   Java source line #380	-> byte code offset #221
/* 948:    */     //   Java source line #375	-> byte code offset #230
/* 949:    */     //   Java source line #377	-> byte code offset #239
/* 950:    */     //   Java source line #379	-> byte code offset #247
/* 951:    */     //   Java source line #380	-> byte code offset #254
/* 952:    */     // Local variable table:
/* 953:    */     //   start	length	slot	name	signature
/* 954:    */     //   0	263	0	this	DefaultPromise<V>
/* 955:    */     //   0	263	1	timeoutNanos	long
/* 956:    */     //   0	263	3	interruptable	boolean
/* 957:    */     //   45	146	4	startTime	long
/* 958:    */     //   48	149	6	waitTime	long
/* 959:    */     //   51	199	8	interrupted	boolean
/* 960:    */     //   66	47	10	bool1	boolean
/* 961:    */     //   143	85	10	e	InterruptedException
/* 962:    */     //   163	65	10	bool2	boolean
/* 963:    */     //   230	7	11	localObject1	Object
/* 964:    */     //   239	6	12	localObject2	Object
/* 965:    */     //   247	14	13	localObject3	Object
/* 966:    */     // Exception table:
/* 967:    */     //   from	to	target	type
/* 968:    */     //   123	140	143	java/lang/InterruptedException
/* 969:    */     //   123	165	230	finally
/* 970:    */     //   186	209	230	finally
/* 971:    */     //   230	232	230	finally
/* 972:    */     //   58	71	239	finally
/* 973:    */     //   85	101	239	finally
/* 974:    */     //   115	172	239	finally
/* 975:    */     //   186	216	239	finally
/* 976:    */     //   230	244	239	finally
/* 977:    */     //   53	71	247	finally
/* 978:    */     //   85	101	247	finally
/* 979:    */     //   115	172	247	finally
/* 980:    */     //   186	216	247	finally
/* 981:    */     //   230	249	247	finally
/* 982:    */   }
/* 983:    */   
/* 984:    */   private final class LateListeners
/* 985:    */     extends ArrayDeque<GenericFutureListener<?>>
/* 986:    */     implements Runnable
/* 987:    */   {
/* 988:    */     private static final long serialVersionUID = -687137418080392244L;
/* 989:    */     
/* 990:    */     LateListeners()
/* 991:    */     {
/* 992:833 */       super();
/* 993:    */     }
/* 994:    */     
/* 995:    */     public void run()
/* 996:    */     {
/* 997:838 */       if (DefaultPromise.this.listeners == null) {
/* 998:    */         for (;;)
/* 999:    */         {
/* :00:840 */           GenericFutureListener<?> l = (GenericFutureListener)poll();
/* :01:841 */           if (l == null) {
/* :02:    */             break;
/* :03:    */           }
/* :04:844 */           DefaultPromise.notifyListener0(DefaultPromise.this, l);
/* :05:    */         }
/* :06:    */       }
/* :07:849 */       DefaultPromise.execute(DefaultPromise.this.executor(), this);
/* :08:    */     }
/* :09:    */   }
/* :10:    */   
/* :11:    */   private final class LateListenerNotifier
/* :12:    */     implements Runnable
/* :13:    */   {
/* :14:    */     private GenericFutureListener<?> l;
/* :15:    */     
/* :16:    */     LateListenerNotifier()
/* :17:    */     {
/* :18:858 */       this.l = l;
/* :19:    */     }
/* :20:    */     
/* :21:    */     public void run()
/* :22:    */     {
/* :23:863 */       DefaultPromise<V>.LateListeners lateListeners = DefaultPromise.this.lateListeners;
/* :24:864 */       if (this.l != null)
/* :25:    */       {
/* :26:865 */         if (lateListeners == null) {
/* :27:866 */           DefaultPromise.this.lateListeners = (lateListeners = new DefaultPromise.LateListeners(DefaultPromise.this));
/* :28:    */         }
/* :29:868 */         lateListeners.add(this.l);
/* :30:869 */         this.l = null;
/* :31:    */       }
/* :32:872 */       lateListeners.run();
/* :33:    */     }
/* :34:    */   }
/* :35:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.DefaultPromise
 * JD-Core Version:    0.7.0.1
 */