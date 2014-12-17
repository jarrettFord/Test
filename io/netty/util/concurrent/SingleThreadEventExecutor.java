/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import io.netty.util.internal.logging.InternalLogger;
/*   5:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   6:    */ import java.util.ArrayList;
/*   7:    */ import java.util.Iterator;
/*   8:    */ import java.util.LinkedHashSet;
/*   9:    */ import java.util.List;
/*  10:    */ import java.util.PriorityQueue;
/*  11:    */ import java.util.Queue;
/*  12:    */ import java.util.Set;
/*  13:    */ import java.util.concurrent.BlockingQueue;
/*  14:    */ import java.util.concurrent.Callable;
/*  15:    */ import java.util.concurrent.Executors;
/*  16:    */ import java.util.concurrent.LinkedBlockingQueue;
/*  17:    */ import java.util.concurrent.RejectedExecutionException;
/*  18:    */ import java.util.concurrent.Semaphore;
/*  19:    */ import java.util.concurrent.ThreadFactory;
/*  20:    */ import java.util.concurrent.TimeUnit;
/*  21:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  22:    */ 
/*  23:    */ public abstract class SingleThreadEventExecutor
/*  24:    */   extends AbstractEventExecutor
/*  25:    */ {
/*  26:    */   private static final InternalLogger logger;
/*  27:    */   private static final int ST_NOT_STARTED = 1;
/*  28:    */   private static final int ST_STARTED = 2;
/*  29:    */   private static final int ST_SHUTTING_DOWN = 3;
/*  30:    */   private static final int ST_SHUTDOWN = 4;
/*  31:    */   private static final int ST_TERMINATED = 5;
/*  32:    */   private static final Runnable WAKEUP_TASK;
/*  33:    */   private static final AtomicIntegerFieldUpdater<SingleThreadEventExecutor> STATE_UPDATER;
/*  34:    */   private final EventExecutorGroup parent;
/*  35:    */   private final Queue<Runnable> taskQueue;
/*  36:    */   
/*  37:    */   static
/*  38:    */   {
/*  39: 45 */     logger = InternalLoggerFactory.getInstance(SingleThreadEventExecutor.class);
/*  40:    */     
/*  41:    */ 
/*  42:    */ 
/*  43:    */ 
/*  44:    */ 
/*  45:    */ 
/*  46:    */ 
/*  47:    */ 
/*  48: 54 */     WAKEUP_TASK = new Runnable()
/*  49:    */     {
/*  50:    */       public void run() {}
/*  51: 63 */     };
/*  52: 64 */     AtomicIntegerFieldUpdater<SingleThreadEventExecutor> updater = PlatformDependent.newAtomicIntegerFieldUpdater(SingleThreadEventExecutor.class, "state");
/*  53: 66 */     if (updater == null) {
/*  54: 67 */       updater = AtomicIntegerFieldUpdater.newUpdater(SingleThreadEventExecutor.class, "state");
/*  55:    */     }
/*  56: 69 */     STATE_UPDATER = updater;
/*  57:    */   }
/*  58:    */   
/*  59: 74 */   final Queue<ScheduledFutureTask<?>> delayedTaskQueue = new PriorityQueue();
/*  60:    */   private final Thread thread;
/*  61: 77 */   private final Semaphore threadLock = new Semaphore(0);
/*  62: 78 */   private final Set<Runnable> shutdownHooks = new LinkedHashSet();
/*  63:    */   private final boolean addTaskWakesUp;
/*  64:    */   private long lastExecutionTime;
/*  65: 83 */   private volatile int state = 1;
/*  66:    */   private volatile long gracefulShutdownQuietPeriod;
/*  67:    */   private volatile long gracefulShutdownTimeout;
/*  68:    */   private long gracefulShutdownStartTime;
/*  69: 90 */   private final Promise<?> terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
/*  70:    */   
/*  71:    */   protected SingleThreadEventExecutor(EventExecutorGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp)
/*  72:    */   {
/*  73:103 */     if (threadFactory == null) {
/*  74:104 */       throw new NullPointerException("threadFactory");
/*  75:    */     }
/*  76:107 */     this.parent = parent;
/*  77:108 */     this.addTaskWakesUp = addTaskWakesUp;
/*  78:    */     
/*  79:110 */     this.thread = threadFactory.newThread(new Runnable()
/*  80:    */     {
/*  81:    */       public void run()
/*  82:    */       {
/*  83:113 */         boolean success = false;
/*  84:114 */         SingleThreadEventExecutor.this.updateLastExecutionTime();
/*  85:    */         try
/*  86:    */         {
/*  87:116 */           SingleThreadEventExecutor.this.run();
/*  88:117 */           success = true;
/*  89:    */           for (;;)
/*  90:    */           {
/*  91:122 */             int oldState = SingleThreadEventExecutor.STATE_UPDATER.get(SingleThreadEventExecutor.this);
/*  92:123 */             if ((oldState >= 3) || (SingleThreadEventExecutor.STATE_UPDATER.compareAndSet(SingleThreadEventExecutor.this, oldState, 3))) {
/*  93:    */               break;
/*  94:    */             }
/*  95:    */           }
/*  96:129 */           if ((success) && (SingleThreadEventExecutor.this.gracefulShutdownStartTime == 0L)) {
/*  97:130 */             SingleThreadEventExecutor.logger.error("Buggy " + EventExecutor.class.getSimpleName() + " implementation; " + SingleThreadEventExecutor.class.getSimpleName() + ".confirmShutdown() must be called " + "before run() implementation terminates.");
/*  98:    */           }
/*  99:    */           try
/* 100:    */           {
/* 101:    */             for (;;)
/* 102:    */             {
/* 103:139 */               if (SingleThreadEventExecutor.this.confirmShutdown()) {
/* 104:    */                 break;
/* 105:    */               }
/* 106:    */             }
/* 107:    */           }
/* 108:    */           finally
/* 109:    */           {
/* 110:    */             try
/* 111:    */             {
/* 112:145 */               SingleThreadEventExecutor.this.cleanup();
/* 113:    */             }
/* 114:    */             finally
/* 115:    */             {
/* 116:147 */               SingleThreadEventExecutor.STATE_UPDATER.set(SingleThreadEventExecutor.this, 5);
/* 117:148 */               SingleThreadEventExecutor.this.threadLock.release();
/* 118:149 */               if (!SingleThreadEventExecutor.this.taskQueue.isEmpty()) {
/* 119:150 */                 SingleThreadEventExecutor.logger.warn("An event executor terminated with non-empty task queue (" + SingleThreadEventExecutor.this.taskQueue.size() + ')');
/* 120:    */               }
/* 121:155 */               SingleThreadEventExecutor.this.terminationFuture.setSuccess(null);
/* 122:    */             }
/* 123:    */           }
/* 124:    */         }
/* 125:    */         catch (Throwable t)
/* 126:    */         {
/* 127:119 */           SingleThreadEventExecutor.logger.warn("Unexpected exception from an event executor: ", t);
/* 128:    */           for (;;)
/* 129:    */           {
/* 130:122 */             int oldState = SingleThreadEventExecutor.STATE_UPDATER.get(SingleThreadEventExecutor.this);
/* 131:123 */             if ((oldState >= 3) || (SingleThreadEventExecutor.STATE_UPDATER.compareAndSet(SingleThreadEventExecutor.this, oldState, 3))) {
/* 132:    */               break;
/* 133:    */             }
/* 134:    */           }
/* 135:129 */           if ((success) && (SingleThreadEventExecutor.this.gracefulShutdownStartTime == 0L)) {
/* 136:130 */             SingleThreadEventExecutor.logger.error("Buggy " + EventExecutor.class.getSimpleName() + " implementation; " + SingleThreadEventExecutor.class.getSimpleName() + ".confirmShutdown() must be called " + "before run() implementation terminates.");
/* 137:    */           }
/* 138:    */           try
/* 139:    */           {
/* 140:    */             for (;;)
/* 141:    */             {
/* 142:139 */               if (SingleThreadEventExecutor.this.confirmShutdown()) {
/* 143:    */                 break;
/* 144:    */               }
/* 145:    */             }
/* 146:    */           }
/* 147:    */           finally
/* 148:    */           {
/* 149:    */             try
/* 150:    */             {
/* 151:145 */               SingleThreadEventExecutor.this.cleanup();
/* 152:    */             }
/* 153:    */             finally
/* 154:    */             {
/* 155:147 */               SingleThreadEventExecutor.STATE_UPDATER.set(SingleThreadEventExecutor.this, 5);
/* 156:148 */               SingleThreadEventExecutor.this.threadLock.release();
/* 157:149 */               if (!SingleThreadEventExecutor.this.taskQueue.isEmpty()) {
/* 158:150 */                 SingleThreadEventExecutor.logger.warn("An event executor terminated with non-empty task queue (" + SingleThreadEventExecutor.this.taskQueue.size() + ')');
/* 159:    */               }
/* 160:155 */               SingleThreadEventExecutor.this.terminationFuture.setSuccess(null);
/* 161:    */             }
/* 162:    */           }
/* 163:    */         }
/* 164:    */         finally
/* 165:    */         {
/* 166:    */           for (;;)
/* 167:    */           {
/* 168:122 */             int oldState = SingleThreadEventExecutor.STATE_UPDATER.get(SingleThreadEventExecutor.this);
/* 169:123 */             if ((oldState >= 3) || (SingleThreadEventExecutor.STATE_UPDATER.compareAndSet(SingleThreadEventExecutor.this, oldState, 3))) {
/* 170:    */               break;
/* 171:    */             }
/* 172:    */           }
/* 173:129 */           if ((success) && (SingleThreadEventExecutor.this.gracefulShutdownStartTime == 0L)) {
/* 174:130 */             SingleThreadEventExecutor.logger.error("Buggy " + EventExecutor.class.getSimpleName() + " implementation; " + SingleThreadEventExecutor.class.getSimpleName() + ".confirmShutdown() must be called " + "before run() implementation terminates.");
/* 175:    */           }
/* 176:    */           try
/* 177:    */           {
/* 178:    */             for (;;)
/* 179:    */             {
/* 180:139 */               if (SingleThreadEventExecutor.this.confirmShutdown()) {
/* 181:    */                 break;
/* 182:    */               }
/* 183:    */             }
/* 184:    */           }
/* 185:    */           finally
/* 186:    */           {
/* 187:    */             try
/* 188:    */             {
/* 189:145 */               SingleThreadEventExecutor.this.cleanup();
/* 190:    */             }
/* 191:    */             finally
/* 192:    */             {
/* 193:147 */               SingleThreadEventExecutor.STATE_UPDATER.set(SingleThreadEventExecutor.this, 5);
/* 194:148 */               SingleThreadEventExecutor.this.threadLock.release();
/* 195:149 */               if (!SingleThreadEventExecutor.this.taskQueue.isEmpty()) {
/* 196:150 */                 SingleThreadEventExecutor.logger.warn("An event executor terminated with non-empty task queue (" + SingleThreadEventExecutor.this.taskQueue.size() + ')');
/* 197:    */               }
/* 198:155 */               SingleThreadEventExecutor.this.terminationFuture.setSuccess(null);
/* 199:    */             }
/* 200:    */           }
/* 201:    */         }
/* 202:    */       }
/* 203:161 */     });
/* 204:162 */     this.taskQueue = newTaskQueue();
/* 205:    */   }
/* 206:    */   
/* 207:    */   protected Queue<Runnable> newTaskQueue()
/* 208:    */   {
/* 209:172 */     return new LinkedBlockingQueue();
/* 210:    */   }
/* 211:    */   
/* 212:    */   public EventExecutorGroup parent()
/* 213:    */   {
/* 214:177 */     return this.parent;
/* 215:    */   }
/* 216:    */   
/* 217:    */   protected void interruptThread()
/* 218:    */   {
/* 219:184 */     this.thread.interrupt();
/* 220:    */   }
/* 221:    */   
/* 222:    */   protected Runnable pollTask()
/* 223:    */   {
/* 224:191 */     assert (inEventLoop());
/* 225:    */     Runnable task;
/* 226:    */     do
/* 227:    */     {
/* 228:193 */       task = (Runnable)this.taskQueue.poll();
/* 229:194 */     } while (task == WAKEUP_TASK);
/* 230:197 */     return task;
/* 231:    */   }
/* 232:    */   
/* 233:    */   protected Runnable takeTask()
/* 234:    */   {
/* 235:211 */     assert (inEventLoop());
/* 236:212 */     if (!(this.taskQueue instanceof BlockingQueue)) {
/* 237:213 */       throw new UnsupportedOperationException();
/* 238:    */     }
/* 239:216 */     BlockingQueue<Runnable> taskQueue = (BlockingQueue)this.taskQueue;
/* 240:    */     for (;;)
/* 241:    */     {
/* 242:218 */       ScheduledFutureTask<?> delayedTask = (ScheduledFutureTask)this.delayedTaskQueue.peek();
/* 243:219 */       if (delayedTask == null)
/* 244:    */       {
/* 245:220 */         Runnable task = null;
/* 246:    */         try
/* 247:    */         {
/* 248:222 */           task = (Runnable)taskQueue.take();
/* 249:223 */           if (task == WAKEUP_TASK) {
/* 250:224 */             task = null;
/* 251:    */           }
/* 252:    */         }
/* 253:    */         catch (InterruptedException e) {}
/* 254:229 */         return task;
/* 255:    */       }
/* 256:231 */       long delayNanos = delayedTask.delayNanos();
/* 257:232 */       Runnable task = null;
/* 258:233 */       if (delayNanos > 0L) {
/* 259:    */         try
/* 260:    */         {
/* 261:235 */           task = (Runnable)taskQueue.poll(delayNanos, TimeUnit.NANOSECONDS);
/* 262:    */         }
/* 263:    */         catch (InterruptedException e)
/* 264:    */         {
/* 265:237 */           return null;
/* 266:    */         }
/* 267:    */       }
/* 268:240 */       if (task == null)
/* 269:    */       {
/* 270:245 */         fetchFromDelayedQueue();
/* 271:246 */         task = (Runnable)taskQueue.poll();
/* 272:    */       }
/* 273:249 */       if (task != null) {
/* 274:250 */         return task;
/* 275:    */       }
/* 276:    */     }
/* 277:    */   }
/* 278:    */   
/* 279:    */   private void fetchFromDelayedQueue()
/* 280:    */   {
/* 281:257 */     long nanoTime = 0L;
/* 282:    */     for (;;)
/* 283:    */     {
/* 284:259 */       ScheduledFutureTask<?> delayedTask = (ScheduledFutureTask)this.delayedTaskQueue.peek();
/* 285:260 */       if (delayedTask == null) {
/* 286:    */         break;
/* 287:    */       }
/* 288:264 */       if (nanoTime == 0L) {
/* 289:265 */         nanoTime = ScheduledFutureTask.nanoTime();
/* 290:    */       }
/* 291:268 */       if (delayedTask.deadlineNanos() > nanoTime) {
/* 292:    */         break;
/* 293:    */       }
/* 294:269 */       this.delayedTaskQueue.remove();
/* 295:270 */       this.taskQueue.add(delayedTask);
/* 296:    */     }
/* 297:    */   }
/* 298:    */   
/* 299:    */   protected Runnable peekTask()
/* 300:    */   {
/* 301:281 */     assert (inEventLoop());
/* 302:282 */     return (Runnable)this.taskQueue.peek();
/* 303:    */   }
/* 304:    */   
/* 305:    */   protected boolean hasTasks()
/* 306:    */   {
/* 307:289 */     assert (inEventLoop());
/* 308:290 */     return !this.taskQueue.isEmpty();
/* 309:    */   }
/* 310:    */   
/* 311:    */   public final int pendingTasks()
/* 312:    */   {
/* 313:300 */     return this.taskQueue.size();
/* 314:    */   }
/* 315:    */   
/* 316:    */   protected void addTask(Runnable task)
/* 317:    */   {
/* 318:308 */     if (task == null) {
/* 319:309 */       throw new NullPointerException("task");
/* 320:    */     }
/* 321:311 */     if (isShutdown()) {
/* 322:312 */       reject();
/* 323:    */     }
/* 324:314 */     this.taskQueue.add(task);
/* 325:    */   }
/* 326:    */   
/* 327:    */   protected boolean removeTask(Runnable task)
/* 328:    */   {
/* 329:321 */     if (task == null) {
/* 330:322 */       throw new NullPointerException("task");
/* 331:    */     }
/* 332:324 */     return this.taskQueue.remove(task);
/* 333:    */   }
/* 334:    */   
/* 335:    */   protected boolean runAllTasks()
/* 336:    */   {
/* 337:333 */     fetchFromDelayedQueue();
/* 338:334 */     Runnable task = pollTask();
/* 339:335 */     if (task == null) {
/* 340:336 */       return false;
/* 341:    */     }
/* 342:    */     do
/* 343:    */     {
/* 344:    */       try
/* 345:    */       {
/* 346:341 */         task.run();
/* 347:    */       }
/* 348:    */       catch (Throwable t)
/* 349:    */       {
/* 350:343 */         logger.warn("A task raised an exception.", t);
/* 351:    */       }
/* 352:346 */       task = pollTask();
/* 353:347 */     } while (task != null);
/* 354:348 */     this.lastExecutionTime = ScheduledFutureTask.nanoTime();
/* 355:349 */     return true;
/* 356:    */   }
/* 357:    */   
/* 358:    */   protected boolean runAllTasks(long timeoutNanos)
/* 359:    */   {
/* 360:359 */     fetchFromDelayedQueue();
/* 361:360 */     Runnable task = pollTask();
/* 362:361 */     if (task == null) {
/* 363:362 */       return false;
/* 364:    */     }
/* 365:365 */     long deadline = ScheduledFutureTask.nanoTime() + timeoutNanos;
/* 366:366 */     long runTasks = 0L;
/* 367:    */     do
/* 368:    */     {
/* 369:    */       try
/* 370:    */       {
/* 371:370 */         task.run();
/* 372:    */       }
/* 373:    */       catch (Throwable t)
/* 374:    */       {
/* 375:372 */         logger.warn("A task raised an exception.", t);
/* 376:    */       }
/* 377:375 */       runTasks += 1L;
/* 378:379 */       if ((runTasks & 0x3F) == 0L)
/* 379:    */       {
/* 380:380 */         long lastExecutionTime = ScheduledFutureTask.nanoTime();
/* 381:381 */         if (lastExecutionTime >= deadline) {
/* 382:    */           break;
/* 383:    */         }
/* 384:    */       }
/* 385:386 */       task = pollTask();
/* 386:387 */     } while (task != null);
/* 387:388 */     long lastExecutionTime = ScheduledFutureTask.nanoTime();
/* 388:    */     
/* 389:    */ 
/* 390:    */ 
/* 391:    */ 
/* 392:393 */     this.lastExecutionTime = lastExecutionTime;
/* 393:394 */     return true;
/* 394:    */   }
/* 395:    */   
/* 396:    */   protected long delayNanos(long currentTimeNanos)
/* 397:    */   {
/* 398:401 */     ScheduledFutureTask<?> delayedTask = (ScheduledFutureTask)this.delayedTaskQueue.peek();
/* 399:402 */     if (delayedTask == null) {
/* 400:403 */       return SCHEDULE_PURGE_INTERVAL;
/* 401:    */     }
/* 402:406 */     return delayedTask.delayNanos(currentTimeNanos);
/* 403:    */   }
/* 404:    */   
/* 405:    */   protected void updateLastExecutionTime()
/* 406:    */   {
/* 407:417 */     this.lastExecutionTime = ScheduledFutureTask.nanoTime();
/* 408:    */   }
/* 409:    */   
/* 410:    */   protected void wakeup(boolean inEventLoop)
/* 411:    */   {
/* 412:433 */     if ((!inEventLoop) || (STATE_UPDATER.get(this) == 3)) {
/* 413:434 */       this.taskQueue.add(WAKEUP_TASK);
/* 414:    */     }
/* 415:    */   }
/* 416:    */   
/* 417:    */   public boolean inEventLoop(Thread thread)
/* 418:    */   {
/* 419:440 */     return thread == this.thread;
/* 420:    */   }
/* 421:    */   
/* 422:    */   public void addShutdownHook(final Runnable task)
/* 423:    */   {
/* 424:447 */     if (inEventLoop()) {
/* 425:448 */       this.shutdownHooks.add(task);
/* 426:    */     } else {
/* 427:450 */       execute(new Runnable()
/* 428:    */       {
/* 429:    */         public void run()
/* 430:    */         {
/* 431:453 */           SingleThreadEventExecutor.this.shutdownHooks.add(task);
/* 432:    */         }
/* 433:    */       });
/* 434:    */     }
/* 435:    */   }
/* 436:    */   
/* 437:    */   public void removeShutdownHook(final Runnable task)
/* 438:    */   {
/* 439:463 */     if (inEventLoop()) {
/* 440:464 */       this.shutdownHooks.remove(task);
/* 441:    */     } else {
/* 442:466 */       execute(new Runnable()
/* 443:    */       {
/* 444:    */         public void run()
/* 445:    */         {
/* 446:469 */           SingleThreadEventExecutor.this.shutdownHooks.remove(task);
/* 447:    */         }
/* 448:    */       });
/* 449:    */     }
/* 450:    */   }
/* 451:    */   
/* 452:    */   private boolean runShutdownHooks()
/* 453:    */   {
/* 454:476 */     boolean ran = false;
/* 455:478 */     while (!this.shutdownHooks.isEmpty())
/* 456:    */     {
/* 457:479 */       List<Runnable> copy = new ArrayList(this.shutdownHooks);
/* 458:480 */       this.shutdownHooks.clear();
/* 459:481 */       for (Runnable task : copy) {
/* 460:    */         try
/* 461:    */         {
/* 462:483 */           task.run();
/* 463:    */         }
/* 464:    */         catch (Throwable t)
/* 465:    */         {
/* 466:485 */           logger.warn("Shutdown hook raised an exception.", t);
/* 467:    */         }
/* 468:    */         finally
/* 469:    */         {
/* 470:487 */           ran = true;
/* 471:    */         }
/* 472:    */       }
/* 473:    */     }
/* 474:492 */     if (ran) {
/* 475:493 */       this.lastExecutionTime = ScheduledFutureTask.nanoTime();
/* 476:    */     }
/* 477:496 */     return ran;
/* 478:    */   }
/* 479:    */   
/* 480:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/* 481:    */   {
/* 482:501 */     if (quietPeriod < 0L) {
/* 483:502 */       throw new IllegalArgumentException("quietPeriod: " + quietPeriod + " (expected >= 0)");
/* 484:    */     }
/* 485:504 */     if (timeout < quietPeriod) {
/* 486:505 */       throw new IllegalArgumentException("timeout: " + timeout + " (expected >= quietPeriod (" + quietPeriod + "))");
/* 487:    */     }
/* 488:508 */     if (unit == null) {
/* 489:509 */       throw new NullPointerException("unit");
/* 490:    */     }
/* 491:512 */     if (isShuttingDown()) {
/* 492:513 */       return terminationFuture();
/* 493:    */     }
/* 494:516 */     boolean inEventLoop = inEventLoop();
/* 495:    */     boolean wakeup;
/* 496:    */     int oldState;
/* 497:    */     for (;;)
/* 498:    */     {
/* 499:520 */       if (isShuttingDown()) {
/* 500:521 */         return terminationFuture();
/* 501:    */       }
/* 502:524 */       wakeup = true;
/* 503:525 */       oldState = STATE_UPDATER.get(this);
/* 504:    */       int newState;
/* 505:    */       int newState;
/* 506:526 */       if (inEventLoop) {
/* 507:527 */         newState = 3;
/* 508:    */       } else {
/* 509:529 */         switch (oldState)
/* 510:    */         {
/* 511:    */         case 1: 
/* 512:    */         case 2: 
/* 513:532 */           newState = 3;
/* 514:533 */           break;
/* 515:    */         default: 
/* 516:535 */           newState = oldState;
/* 517:536 */           wakeup = false;
/* 518:    */         }
/* 519:    */       }
/* 520:539 */       if (STATE_UPDATER.compareAndSet(this, oldState, newState)) {
/* 521:    */         break;
/* 522:    */       }
/* 523:    */     }
/* 524:543 */     this.gracefulShutdownQuietPeriod = unit.toNanos(quietPeriod);
/* 525:544 */     this.gracefulShutdownTimeout = unit.toNanos(timeout);
/* 526:546 */     if (oldState == 1) {
/* 527:547 */       this.thread.start();
/* 528:    */     }
/* 529:550 */     if (wakeup) {
/* 530:551 */       wakeup(inEventLoop);
/* 531:    */     }
/* 532:554 */     return terminationFuture();
/* 533:    */   }
/* 534:    */   
/* 535:    */   public Future<?> terminationFuture()
/* 536:    */   {
/* 537:559 */     return this.terminationFuture;
/* 538:    */   }
/* 539:    */   
/* 540:    */   @Deprecated
/* 541:    */   public void shutdown()
/* 542:    */   {
/* 543:565 */     if (isShutdown()) {
/* 544:566 */       return;
/* 545:    */     }
/* 546:569 */     boolean inEventLoop = inEventLoop();
/* 547:    */     boolean wakeup;
/* 548:    */     int oldState;
/* 549:    */     for (;;)
/* 550:    */     {
/* 551:573 */       if (isShuttingDown()) {
/* 552:574 */         return;
/* 553:    */       }
/* 554:577 */       wakeup = true;
/* 555:578 */       oldState = STATE_UPDATER.get(this);
/* 556:    */       int newState;
/* 557:    */       int newState;
/* 558:579 */       if (inEventLoop) {
/* 559:580 */         newState = 4;
/* 560:    */       } else {
/* 561:582 */         switch (oldState)
/* 562:    */         {
/* 563:    */         case 1: 
/* 564:    */         case 2: 
/* 565:    */         case 3: 
/* 566:586 */           newState = 4;
/* 567:587 */           break;
/* 568:    */         default: 
/* 569:589 */           newState = oldState;
/* 570:590 */           wakeup = false;
/* 571:    */         }
/* 572:    */       }
/* 573:593 */       if (STATE_UPDATER.compareAndSet(this, oldState, newState)) {
/* 574:    */         break;
/* 575:    */       }
/* 576:    */     }
/* 577:598 */     if (oldState == 1) {
/* 578:599 */       this.thread.start();
/* 579:    */     }
/* 580:602 */     if (wakeup) {
/* 581:603 */       wakeup(inEventLoop);
/* 582:    */     }
/* 583:    */   }
/* 584:    */   
/* 585:    */   public boolean isShuttingDown()
/* 586:    */   {
/* 587:609 */     return STATE_UPDATER.get(this) >= 3;
/* 588:    */   }
/* 589:    */   
/* 590:    */   public boolean isShutdown()
/* 591:    */   {
/* 592:614 */     return STATE_UPDATER.get(this) >= 4;
/* 593:    */   }
/* 594:    */   
/* 595:    */   public boolean isTerminated()
/* 596:    */   {
/* 597:619 */     return STATE_UPDATER.get(this) == 5;
/* 598:    */   }
/* 599:    */   
/* 600:    */   protected boolean confirmShutdown()
/* 601:    */   {
/* 602:626 */     if (!isShuttingDown()) {
/* 603:627 */       return false;
/* 604:    */     }
/* 605:630 */     if (!inEventLoop()) {
/* 606:631 */       throw new IllegalStateException("must be invoked from an event loop");
/* 607:    */     }
/* 608:634 */     cancelDelayedTasks();
/* 609:636 */     if (this.gracefulShutdownStartTime == 0L) {
/* 610:637 */       this.gracefulShutdownStartTime = ScheduledFutureTask.nanoTime();
/* 611:    */     }
/* 612:640 */     if ((runAllTasks()) || (runShutdownHooks()))
/* 613:    */     {
/* 614:641 */       if (isShutdown()) {
/* 615:643 */         return true;
/* 616:    */       }
/* 617:647 */       wakeup(true);
/* 618:648 */       return false;
/* 619:    */     }
/* 620:651 */     long nanoTime = ScheduledFutureTask.nanoTime();
/* 621:653 */     if ((isShutdown()) || (nanoTime - this.gracefulShutdownStartTime > this.gracefulShutdownTimeout)) {
/* 622:654 */       return true;
/* 623:    */     }
/* 624:657 */     if (nanoTime - this.lastExecutionTime <= this.gracefulShutdownQuietPeriod)
/* 625:    */     {
/* 626:660 */       wakeup(true);
/* 627:    */       try
/* 628:    */       {
/* 629:662 */         Thread.sleep(100L);
/* 630:    */       }
/* 631:    */       catch (InterruptedException e) {}
/* 632:667 */       return false;
/* 633:    */     }
/* 634:672 */     return true;
/* 635:    */   }
/* 636:    */   
/* 637:    */   private void cancelDelayedTasks()
/* 638:    */   {
/* 639:676 */     if (this.delayedTaskQueue.isEmpty()) {
/* 640:677 */       return;
/* 641:    */     }
/* 642:680 */     ScheduledFutureTask<?>[] delayedTasks = (ScheduledFutureTask[])this.delayedTaskQueue.toArray(new ScheduledFutureTask[this.delayedTaskQueue.size()]);
/* 643:683 */     for (ScheduledFutureTask<?> task : delayedTasks) {
/* 644:684 */       task.cancel(false);
/* 645:    */     }
/* 646:687 */     this.delayedTaskQueue.clear();
/* 647:    */   }
/* 648:    */   
/* 649:    */   public boolean awaitTermination(long timeout, TimeUnit unit)
/* 650:    */     throws InterruptedException
/* 651:    */   {
/* 652:692 */     if (unit == null) {
/* 653:693 */       throw new NullPointerException("unit");
/* 654:    */     }
/* 655:696 */     if (inEventLoop()) {
/* 656:697 */       throw new IllegalStateException("cannot await termination of the current thread");
/* 657:    */     }
/* 658:700 */     if (this.threadLock.tryAcquire(timeout, unit)) {
/* 659:701 */       this.threadLock.release();
/* 660:    */     }
/* 661:704 */     return isTerminated();
/* 662:    */   }
/* 663:    */   
/* 664:    */   public void execute(Runnable task)
/* 665:    */   {
/* 666:709 */     if (task == null) {
/* 667:710 */       throw new NullPointerException("task");
/* 668:    */     }
/* 669:713 */     boolean inEventLoop = inEventLoop();
/* 670:714 */     if (inEventLoop)
/* 671:    */     {
/* 672:715 */       addTask(task);
/* 673:    */     }
/* 674:    */     else
/* 675:    */     {
/* 676:717 */       startThread();
/* 677:718 */       addTask(task);
/* 678:719 */       if ((isShutdown()) && (removeTask(task))) {
/* 679:720 */         reject();
/* 680:    */       }
/* 681:    */     }
/* 682:724 */     if ((!this.addTaskWakesUp) && (wakesUpForTask(task))) {
/* 683:725 */       wakeup(inEventLoop);
/* 684:    */     }
/* 685:    */   }
/* 686:    */   
/* 687:    */   protected boolean wakesUpForTask(Runnable task)
/* 688:    */   {
/* 689:731 */     return true;
/* 690:    */   }
/* 691:    */   
/* 692:    */   protected static void reject()
/* 693:    */   {
/* 694:735 */     throw new RejectedExecutionException("event executor terminated");
/* 695:    */   }
/* 696:    */   
/* 697:740 */   private static final long SCHEDULE_PURGE_INTERVAL = TimeUnit.SECONDS.toNanos(1L);
/* 698:    */   
/* 699:    */   public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
/* 700:    */   {
/* 701:744 */     if (command == null) {
/* 702:745 */       throw new NullPointerException("command");
/* 703:    */     }
/* 704:747 */     if (unit == null) {
/* 705:748 */       throw new NullPointerException("unit");
/* 706:    */     }
/* 707:750 */     if (delay < 0L) {
/* 708:751 */       throw new IllegalArgumentException(String.format("delay: %d (expected: >= 0)", new Object[] { Long.valueOf(delay) }));
/* 709:    */     }
/* 710:754 */     return schedule(new ScheduledFutureTask(this, this.delayedTaskQueue, command, null, ScheduledFutureTask.deadlineNanos(unit.toNanos(delay))));
/* 711:    */   }
/* 712:    */   
/* 713:    */   public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
/* 714:    */   {
/* 715:760 */     if (callable == null) {
/* 716:761 */       throw new NullPointerException("callable");
/* 717:    */     }
/* 718:763 */     if (unit == null) {
/* 719:764 */       throw new NullPointerException("unit");
/* 720:    */     }
/* 721:766 */     if (delay < 0L) {
/* 722:767 */       throw new IllegalArgumentException(String.format("delay: %d (expected: >= 0)", new Object[] { Long.valueOf(delay) }));
/* 723:    */     }
/* 724:770 */     return schedule(new ScheduledFutureTask(this, this.delayedTaskQueue, callable, ScheduledFutureTask.deadlineNanos(unit.toNanos(delay))));
/* 725:    */   }
/* 726:    */   
/* 727:    */   public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
/* 728:    */   {
/* 729:776 */     if (command == null) {
/* 730:777 */       throw new NullPointerException("command");
/* 731:    */     }
/* 732:779 */     if (unit == null) {
/* 733:780 */       throw new NullPointerException("unit");
/* 734:    */     }
/* 735:782 */     if (initialDelay < 0L) {
/* 736:783 */       throw new IllegalArgumentException(String.format("initialDelay: %d (expected: >= 0)", new Object[] { Long.valueOf(initialDelay) }));
/* 737:    */     }
/* 738:786 */     if (period <= 0L) {
/* 739:787 */       throw new IllegalArgumentException(String.format("period: %d (expected: > 0)", new Object[] { Long.valueOf(period) }));
/* 740:    */     }
/* 741:791 */     return schedule(new ScheduledFutureTask(this, this.delayedTaskQueue, Executors.callable(command, null), ScheduledFutureTask.deadlineNanos(unit.toNanos(initialDelay)), unit.toNanos(period)));
/* 742:    */   }
/* 743:    */   
/* 744:    */   public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
/* 745:    */   {
/* 746:798 */     if (command == null) {
/* 747:799 */       throw new NullPointerException("command");
/* 748:    */     }
/* 749:801 */     if (unit == null) {
/* 750:802 */       throw new NullPointerException("unit");
/* 751:    */     }
/* 752:804 */     if (initialDelay < 0L) {
/* 753:805 */       throw new IllegalArgumentException(String.format("initialDelay: %d (expected: >= 0)", new Object[] { Long.valueOf(initialDelay) }));
/* 754:    */     }
/* 755:808 */     if (delay <= 0L) {
/* 756:809 */       throw new IllegalArgumentException(String.format("delay: %d (expected: > 0)", new Object[] { Long.valueOf(delay) }));
/* 757:    */     }
/* 758:813 */     return schedule(new ScheduledFutureTask(this, this.delayedTaskQueue, Executors.callable(command, null), ScheduledFutureTask.deadlineNanos(unit.toNanos(initialDelay)), -unit.toNanos(delay)));
/* 759:    */   }
/* 760:    */   
/* 761:    */   private <V> ScheduledFuture<V> schedule(final ScheduledFutureTask<V> task)
/* 762:    */   {
/* 763:819 */     if (task == null) {
/* 764:820 */       throw new NullPointerException("task");
/* 765:    */     }
/* 766:823 */     if (inEventLoop()) {
/* 767:824 */       this.delayedTaskQueue.add(task);
/* 768:    */     } else {
/* 769:826 */       execute(new Runnable()
/* 770:    */       {
/* 771:    */         public void run()
/* 772:    */         {
/* 773:829 */           SingleThreadEventExecutor.this.delayedTaskQueue.add(task);
/* 774:    */         }
/* 775:    */       });
/* 776:    */     }
/* 777:834 */     return task;
/* 778:    */   }
/* 779:    */   
/* 780:    */   private void startThread()
/* 781:    */   {
/* 782:838 */     if ((STATE_UPDATER.get(this) == 1) && 
/* 783:839 */       (STATE_UPDATER.compareAndSet(this, 1, 2)))
/* 784:    */     {
/* 785:840 */       this.delayedTaskQueue.add(new ScheduledFutureTask(this, this.delayedTaskQueue, Executors.callable(new PurgeTask(null), null), ScheduledFutureTask.deadlineNanos(SCHEDULE_PURGE_INTERVAL), -SCHEDULE_PURGE_INTERVAL));
/* 786:    */       
/* 787:    */ 
/* 788:843 */       this.thread.start();
/* 789:    */     }
/* 790:    */   }
/* 791:    */   
/* 792:    */   protected abstract void run();
/* 793:    */   
/* 794:    */   protected void cleanup() {}
/* 795:    */   
/* 796:    */   private final class PurgeTask
/* 797:    */     implements Runnable
/* 798:    */   {
/* 799:    */     private PurgeTask() {}
/* 800:    */     
/* 801:    */     public void run()
/* 802:    */     {
/* 803:851 */       Iterator<ScheduledFutureTask<?>> i = SingleThreadEventExecutor.this.delayedTaskQueue.iterator();
/* 804:852 */       while (i.hasNext())
/* 805:    */       {
/* 806:853 */         ScheduledFutureTask<?> task = (ScheduledFutureTask)i.next();
/* 807:854 */         if (task.isCancelled()) {
/* 808:855 */           i.remove();
/* 809:    */         }
/* 810:    */       }
/* 811:    */     }
/* 812:    */   }
/* 813:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.SingleThreadEventExecutor
 * JD-Core Version:    0.7.0.1
 */