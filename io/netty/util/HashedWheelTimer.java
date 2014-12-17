/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.MpscLinkedQueueNode;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import io.netty.util.internal.StringUtil;
/*   6:    */ import io.netty.util.internal.logging.InternalLogger;
/*   7:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   8:    */ import java.util.Collections;
/*   9:    */ import java.util.HashSet;
/*  10:    */ import java.util.Queue;
/*  11:    */ import java.util.Set;
/*  12:    */ import java.util.concurrent.CountDownLatch;
/*  13:    */ import java.util.concurrent.Executors;
/*  14:    */ import java.util.concurrent.ThreadFactory;
/*  15:    */ import java.util.concurrent.TimeUnit;
/*  16:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  17:    */ 
/*  18:    */ public class HashedWheelTimer
/*  19:    */   implements Timer
/*  20:    */ {
/*  21: 77 */   static final InternalLogger logger = InternalLoggerFactory.getInstance(HashedWheelTimer.class);
/*  22: 80 */   private static final ResourceLeakDetector<HashedWheelTimer> leakDetector = new ResourceLeakDetector(HashedWheelTimer.class, 1, Runtime.getRuntime().availableProcessors() * 4);
/*  23:    */   private static final AtomicIntegerFieldUpdater<HashedWheelTimer> WORKER_STATE_UPDATER;
/*  24:    */   private final ResourceLeak leak;
/*  25:    */   
/*  26:    */   static
/*  27:    */   {
/*  28: 86 */     AtomicIntegerFieldUpdater<HashedWheelTimer> workerStateUpdater = PlatformDependent.newAtomicIntegerFieldUpdater(HashedWheelTimer.class, "workerState");
/*  29: 88 */     if (workerStateUpdater == null) {
/*  30: 89 */       workerStateUpdater = AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimer.class, "workerState");
/*  31:    */     }
/*  32: 91 */     WORKER_STATE_UPDATER = workerStateUpdater;
/*  33:    */   }
/*  34:    */   
/*  35: 95 */   private final Worker worker = new Worker(null);
/*  36:    */   private final Thread workerThread;
/*  37:    */   public static final int WORKER_STATE_INIT = 0;
/*  38:    */   public static final int WORKER_STATE_STARTED = 1;
/*  39:    */   public static final int WORKER_STATE_SHUTDOWN = 2;
/*  40:101 */   private volatile int workerState = 0;
/*  41:    */   private final long tickDuration;
/*  42:    */   private final HashedWheelBucket[] wheel;
/*  43:    */   private final int mask;
/*  44:107 */   private final CountDownLatch startTimeInitialized = new CountDownLatch(1);
/*  45:108 */   private final Queue<HashedWheelTimeout> timeouts = PlatformDependent.newMpscQueue();
/*  46:    */   private volatile long startTime;
/*  47:    */   
/*  48:    */   public HashedWheelTimer()
/*  49:    */   {
/*  50:118 */     this(Executors.defaultThreadFactory());
/*  51:    */   }
/*  52:    */   
/*  53:    */   public HashedWheelTimer(long tickDuration, TimeUnit unit)
/*  54:    */   {
/*  55:132 */     this(Executors.defaultThreadFactory(), tickDuration, unit);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public HashedWheelTimer(long tickDuration, TimeUnit unit, int ticksPerWheel)
/*  59:    */   {
/*  60:146 */     this(Executors.defaultThreadFactory(), tickDuration, unit, ticksPerWheel);
/*  61:    */   }
/*  62:    */   
/*  63:    */   public HashedWheelTimer(ThreadFactory threadFactory)
/*  64:    */   {
/*  65:159 */     this(threadFactory, 100L, TimeUnit.MILLISECONDS);
/*  66:    */   }
/*  67:    */   
/*  68:    */   public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit)
/*  69:    */   {
/*  70:175 */     this(threadFactory, tickDuration, unit, 512);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel)
/*  74:    */   {
/*  75:194 */     if (threadFactory == null) {
/*  76:195 */       throw new NullPointerException("threadFactory");
/*  77:    */     }
/*  78:197 */     if (unit == null) {
/*  79:198 */       throw new NullPointerException("unit");
/*  80:    */     }
/*  81:200 */     if (tickDuration <= 0L) {
/*  82:201 */       throw new IllegalArgumentException("tickDuration must be greater than 0: " + tickDuration);
/*  83:    */     }
/*  84:203 */     if (ticksPerWheel <= 0) {
/*  85:204 */       throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
/*  86:    */     }
/*  87:208 */     this.wheel = createWheel(ticksPerWheel);
/*  88:209 */     this.mask = (this.wheel.length - 1);
/*  89:    */     
/*  90:    */ 
/*  91:212 */     this.tickDuration = unit.toNanos(tickDuration);
/*  92:215 */     if (this.tickDuration >= 9223372036854775807L / this.wheel.length) {
/*  93:216 */       throw new IllegalArgumentException(String.format("tickDuration: %d (expected: 0 < tickDuration in nanos < %d", new Object[] { Long.valueOf(tickDuration), Long.valueOf(9223372036854775807L / this.wheel.length) }));
/*  94:    */     }
/*  95:220 */     this.workerThread = threadFactory.newThread(this.worker);
/*  96:    */     
/*  97:222 */     this.leak = leakDetector.open(this);
/*  98:    */   }
/*  99:    */   
/* 100:    */   private static HashedWheelBucket[] createWheel(int ticksPerWheel)
/* 101:    */   {
/* 102:227 */     if (ticksPerWheel <= 0) {
/* 103:228 */       throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
/* 104:    */     }
/* 105:231 */     if (ticksPerWheel > 1073741824) {
/* 106:232 */       throw new IllegalArgumentException("ticksPerWheel may not be greater than 2^30: " + ticksPerWheel);
/* 107:    */     }
/* 108:236 */     ticksPerWheel = normalizeTicksPerWheel(ticksPerWheel);
/* 109:237 */     HashedWheelBucket[] wheel = new HashedWheelBucket[ticksPerWheel];
/* 110:238 */     for (int i = 0; i < wheel.length; i++) {
/* 111:239 */       wheel[i] = new HashedWheelBucket(null);
/* 112:    */     }
/* 113:241 */     return wheel;
/* 114:    */   }
/* 115:    */   
/* 116:    */   private static int normalizeTicksPerWheel(int ticksPerWheel)
/* 117:    */   {
/* 118:245 */     int normalizedTicksPerWheel = 1;
/* 119:246 */     while (normalizedTicksPerWheel < ticksPerWheel) {
/* 120:247 */       normalizedTicksPerWheel <<= 1;
/* 121:    */     }
/* 122:249 */     return normalizedTicksPerWheel;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public void start()
/* 126:    */   {
/* 127:260 */     switch (WORKER_STATE_UPDATER.get(this))
/* 128:    */     {
/* 129:    */     case 0: 
/* 130:262 */       if (WORKER_STATE_UPDATER.compareAndSet(this, 0, 1)) {
/* 131:263 */         this.workerThread.start();
/* 132:    */       }
/* 133:    */       break;
/* 134:    */     case 1: 
/* 135:    */       break;
/* 136:    */     case 2: 
/* 137:269 */       throw new IllegalStateException("cannot be started once stopped");
/* 138:    */     default: 
/* 139:271 */       throw new Error("Invalid WorkerState");
/* 140:    */     }
/* 141:275 */     while (this.startTime == 0L) {
/* 142:    */       try
/* 143:    */       {
/* 144:277 */         this.startTimeInitialized.await();
/* 145:    */       }
/* 146:    */       catch (InterruptedException ignore) {}
/* 147:    */     }
/* 148:    */   }
/* 149:    */   
/* 150:    */   public Set<Timeout> stop()
/* 151:    */   {
/* 152:286 */     if (Thread.currentThread() == this.workerThread) {
/* 153:287 */       throw new IllegalStateException(HashedWheelTimer.class.getSimpleName() + ".stop() cannot be called from " + TimerTask.class.getSimpleName());
/* 154:    */     }
/* 155:293 */     if (!WORKER_STATE_UPDATER.compareAndSet(this, 1, 2))
/* 156:    */     {
/* 157:295 */       WORKER_STATE_UPDATER.set(this, 2);
/* 158:297 */       if (this.leak != null) {
/* 159:298 */         this.leak.close();
/* 160:    */       }
/* 161:301 */       return Collections.emptySet();
/* 162:    */     }
/* 163:304 */     boolean interrupted = false;
/* 164:305 */     while (this.workerThread.isAlive())
/* 165:    */     {
/* 166:306 */       this.workerThread.interrupt();
/* 167:    */       try
/* 168:    */       {
/* 169:308 */         this.workerThread.join(100L);
/* 170:    */       }
/* 171:    */       catch (InterruptedException e)
/* 172:    */       {
/* 173:310 */         interrupted = true;
/* 174:    */       }
/* 175:    */     }
/* 176:314 */     if (interrupted) {
/* 177:315 */       Thread.currentThread().interrupt();
/* 178:    */     }
/* 179:318 */     if (this.leak != null) {
/* 180:319 */       this.leak.close();
/* 181:    */     }
/* 182:321 */     return this.worker.unprocessedTimeouts();
/* 183:    */   }
/* 184:    */   
/* 185:    */   public Timeout newTimeout(TimerTask task, long delay, TimeUnit unit)
/* 186:    */   {
/* 187:326 */     if (task == null) {
/* 188:327 */       throw new NullPointerException("task");
/* 189:    */     }
/* 190:329 */     if (unit == null) {
/* 191:330 */       throw new NullPointerException("unit");
/* 192:    */     }
/* 193:332 */     start();
/* 194:    */     
/* 195:    */ 
/* 196:    */ 
/* 197:336 */     long deadline = System.nanoTime() + unit.toNanos(delay) - this.startTime;
/* 198:337 */     HashedWheelTimeout timeout = new HashedWheelTimeout(this, task, deadline);
/* 199:338 */     this.timeouts.add(timeout);
/* 200:339 */     return timeout;
/* 201:    */   }
/* 202:    */   
/* 203:    */   private final class Worker
/* 204:    */     implements Runnable
/* 205:    */   {
/* 206:343 */     private final Set<Timeout> unprocessedTimeouts = new HashSet();
/* 207:    */     private long tick;
/* 208:    */     
/* 209:    */     private Worker() {}
/* 210:    */     
/* 211:    */     public void run()
/* 212:    */     {
/* 213:350 */       HashedWheelTimer.this.startTime = System.nanoTime();
/* 214:351 */       if (HashedWheelTimer.this.startTime == 0L) {
/* 215:353 */         HashedWheelTimer.this.startTime = 1L;
/* 216:    */       }
/* 217:357 */       HashedWheelTimer.this.startTimeInitialized.countDown();
/* 218:    */       do
/* 219:    */       {
/* 220:360 */         long deadline = waitForNextTick();
/* 221:361 */         if (deadline > 0L)
/* 222:    */         {
/* 223:362 */           transferTimeoutsToBuckets();
/* 224:363 */           HashedWheelTimer.HashedWheelBucket bucket = HashedWheelTimer.this.wheel[((int)(this.tick & HashedWheelTimer.this.mask))];
/* 225:    */           
/* 226:365 */           bucket.expireTimeouts(deadline);
/* 227:366 */           this.tick += 1L;
/* 228:    */         }
/* 229:368 */       } while (HashedWheelTimer.WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == 1);
/* 230:371 */       for (HashedWheelTimer.HashedWheelBucket bucket : HashedWheelTimer.this.wheel) {
/* 231:372 */         bucket.clearTimeouts(this.unprocessedTimeouts);
/* 232:    */       }
/* 233:    */       for (;;)
/* 234:    */       {
/* 235:375 */         HashedWheelTimer.HashedWheelTimeout timeout = (HashedWheelTimer.HashedWheelTimeout)HashedWheelTimer.this.timeouts.poll();
/* 236:376 */         if (timeout == null) {
/* 237:    */           break;
/* 238:    */         }
/* 239:379 */         this.unprocessedTimeouts.add(timeout);
/* 240:    */       }
/* 241:    */     }
/* 242:    */     
/* 243:    */     private void transferTimeoutsToBuckets()
/* 244:    */     {
/* 245:386 */       for (int i = 0; i < 100000; i++)
/* 246:    */       {
/* 247:387 */         HashedWheelTimer.HashedWheelTimeout timeout = (HashedWheelTimer.HashedWheelTimeout)HashedWheelTimer.this.timeouts.poll();
/* 248:388 */         if (timeout == null) {
/* 249:    */           break;
/* 250:    */         }
/* 251:392 */         if ((timeout.state() == 2) || (!timeout.compareAndSetState(0, 1)))
/* 252:    */         {
/* 253:396 */           timeout.remove();
/* 254:    */         }
/* 255:    */         else
/* 256:    */         {
/* 257:399 */           long calculated = timeout.deadline / HashedWheelTimer.this.tickDuration;
/* 258:400 */           long remainingRounds = (calculated - this.tick) / HashedWheelTimer.this.wheel.length;
/* 259:401 */           timeout.remainingRounds = remainingRounds;
/* 260:    */           
/* 261:403 */           long ticks = Math.max(calculated, this.tick);
/* 262:404 */           int stopIndex = (int)(ticks & HashedWheelTimer.this.mask);
/* 263:    */           
/* 264:406 */           HashedWheelTimer.HashedWheelBucket bucket = HashedWheelTimer.this.wheel[stopIndex];
/* 265:407 */           bucket.addTimeout(timeout);
/* 266:    */         }
/* 267:    */       }
/* 268:    */     }
/* 269:    */     
/* 270:    */     private long waitForNextTick()
/* 271:    */     {
/* 272:417 */       long deadline = HashedWheelTimer.this.tickDuration * (this.tick + 1L);
/* 273:    */       for (;;)
/* 274:    */       {
/* 275:420 */         long currentTime = System.nanoTime() - HashedWheelTimer.this.startTime;
/* 276:421 */         long sleepTimeMs = (deadline - currentTime + 999999L) / 1000000L;
/* 277:423 */         if (sleepTimeMs <= 0L)
/* 278:    */         {
/* 279:424 */           if (currentTime == -9223372036854775808L) {
/* 280:425 */             return -9223372036854775807L;
/* 281:    */           }
/* 282:427 */           return currentTime;
/* 283:    */         }
/* 284:436 */         if (PlatformDependent.isWindows()) {
/* 285:437 */           sleepTimeMs = sleepTimeMs / 10L * 10L;
/* 286:    */         }
/* 287:    */         try
/* 288:    */         {
/* 289:441 */           Thread.sleep(sleepTimeMs);
/* 290:    */         }
/* 291:    */         catch (InterruptedException e)
/* 292:    */         {
/* 293:443 */           if (HashedWheelTimer.WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == 2) {
/* 294:444 */             return -9223372036854775808L;
/* 295:    */           }
/* 296:    */         }
/* 297:    */       }
/* 298:    */     }
/* 299:    */     
/* 300:    */     public Set<Timeout> unprocessedTimeouts()
/* 301:    */     {
/* 302:451 */       return Collections.unmodifiableSet(this.unprocessedTimeouts);
/* 303:    */     }
/* 304:    */   }
/* 305:    */   
/* 306:    */   private static final class HashedWheelTimeout
/* 307:    */     extends MpscLinkedQueueNode<Timeout>
/* 308:    */     implements Timeout
/* 309:    */   {
/* 310:    */     private static final int ST_INIT = 0;
/* 311:    */     private static final int ST_IN_BUCKET = 1;
/* 312:    */     private static final int ST_CANCELLED = 2;
/* 313:    */     private static final int ST_EXPIRED = 3;
/* 314:    */     private static final AtomicIntegerFieldUpdater<HashedWheelTimeout> STATE_UPDATER;
/* 315:    */     private final HashedWheelTimer timer;
/* 316:    */     private final TimerTask task;
/* 317:    */     private final long deadline;
/* 318:    */     
/* 319:    */     static
/* 320:    */     {
/* 321:465 */       AtomicIntegerFieldUpdater<HashedWheelTimeout> updater = PlatformDependent.newAtomicIntegerFieldUpdater(HashedWheelTimeout.class, "state");
/* 322:467 */       if (updater == null) {
/* 323:468 */         updater = AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimeout.class, "state");
/* 324:    */       }
/* 325:470 */       STATE_UPDATER = updater;
/* 326:    */     }
/* 327:    */     
/* 328:477 */     private volatile int state = 0;
/* 329:    */     long remainingRounds;
/* 330:    */     HashedWheelTimeout next;
/* 331:    */     HashedWheelTimeout prev;
/* 332:    */     HashedWheelTimer.HashedWheelBucket bucket;
/* 333:    */     
/* 334:    */     HashedWheelTimeout(HashedWheelTimer timer, TimerTask task, long deadline)
/* 335:    */     {
/* 336:493 */       this.timer = timer;
/* 337:494 */       this.task = task;
/* 338:495 */       this.deadline = deadline;
/* 339:    */     }
/* 340:    */     
/* 341:    */     public Timer timer()
/* 342:    */     {
/* 343:500 */       return this.timer;
/* 344:    */     }
/* 345:    */     
/* 346:    */     public TimerTask task()
/* 347:    */     {
/* 348:505 */       return this.task;
/* 349:    */     }
/* 350:    */     
/* 351:    */     public boolean cancel()
/* 352:    */     {
/* 353:510 */       int state = state();
/* 354:511 */       if (state >= 2) {
/* 355:513 */         return false;
/* 356:    */       }
/* 357:515 */       if ((state != 1) && (compareAndSetState(0, 2))) {
/* 358:519 */         return true;
/* 359:    */       }
/* 360:522 */       if (!compareAndSetState(1, 2)) {
/* 361:523 */         return false;
/* 362:    */       }
/* 363:528 */       this.timer.timeouts.add(this);
/* 364:529 */       return true;
/* 365:    */     }
/* 366:    */     
/* 367:    */     public void remove()
/* 368:    */     {
/* 369:533 */       if (this.bucket != null) {
/* 370:534 */         this.bucket.remove(this);
/* 371:    */       }
/* 372:    */     }
/* 373:    */     
/* 374:    */     public boolean compareAndSetState(int expected, int state)
/* 375:    */     {
/* 376:539 */       return STATE_UPDATER.compareAndSet(this, expected, state);
/* 377:    */     }
/* 378:    */     
/* 379:    */     public int state()
/* 380:    */     {
/* 381:543 */       return this.state;
/* 382:    */     }
/* 383:    */     
/* 384:    */     public boolean isCancelled()
/* 385:    */     {
/* 386:548 */       return state() == 2;
/* 387:    */     }
/* 388:    */     
/* 389:    */     public boolean isExpired()
/* 390:    */     {
/* 391:553 */       return state() > 1;
/* 392:    */     }
/* 393:    */     
/* 394:    */     public HashedWheelTimeout value()
/* 395:    */     {
/* 396:558 */       return this;
/* 397:    */     }
/* 398:    */     
/* 399:    */     public void expire()
/* 400:    */     {
/* 401:562 */       if (!compareAndSetState(1, 3))
/* 402:    */       {
/* 403:563 */         assert (state() != 0);
/* 404:564 */         return;
/* 405:    */       }
/* 406:    */       try
/* 407:    */       {
/* 408:568 */         this.task.run(this);
/* 409:    */       }
/* 410:    */       catch (Throwable t)
/* 411:    */       {
/* 412:570 */         if (HashedWheelTimer.logger.isWarnEnabled()) {
/* 413:571 */           HashedWheelTimer.logger.warn("An exception was thrown by " + TimerTask.class.getSimpleName() + '.', t);
/* 414:    */         }
/* 415:    */       }
/* 416:    */     }
/* 417:    */     
/* 418:    */     public String toString()
/* 419:    */     {
/* 420:578 */       long currentTime = System.nanoTime();
/* 421:579 */       long remaining = this.deadline - currentTime + this.timer.startTime;
/* 422:    */       
/* 423:581 */       StringBuilder buf = new StringBuilder(192);
/* 424:582 */       buf.append(StringUtil.simpleClassName(this));
/* 425:583 */       buf.append('(');
/* 426:    */       
/* 427:585 */       buf.append("deadline: ");
/* 428:586 */       if (remaining > 0L)
/* 429:    */       {
/* 430:587 */         buf.append(remaining);
/* 431:588 */         buf.append(" ns later");
/* 432:    */       }
/* 433:589 */       else if (remaining < 0L)
/* 434:    */       {
/* 435:590 */         buf.append(-remaining);
/* 436:591 */         buf.append(" ns ago");
/* 437:    */       }
/* 438:    */       else
/* 439:    */       {
/* 440:593 */         buf.append("now");
/* 441:    */       }
/* 442:596 */       if (isCancelled()) {
/* 443:597 */         buf.append(", cancelled");
/* 444:    */       }
/* 445:600 */       buf.append(", task: ");
/* 446:601 */       buf.append(task());
/* 447:    */       
/* 448:603 */       return ')';
/* 449:    */     }
/* 450:    */   }
/* 451:    */   
/* 452:    */   private static final class HashedWheelBucket
/* 453:    */   {
/* 454:    */     private HashedWheelTimer.HashedWheelTimeout head;
/* 455:    */     private HashedWheelTimer.HashedWheelTimeout tail;
/* 456:    */     
/* 457:    */     public void addTimeout(HashedWheelTimer.HashedWheelTimeout timeout)
/* 458:    */     {
/* 459:622 */       assert (timeout.bucket == null);
/* 460:623 */       timeout.bucket = this;
/* 461:624 */       if (this.head == null)
/* 462:    */       {
/* 463:625 */         this.head = (this.tail = timeout);
/* 464:    */       }
/* 465:    */       else
/* 466:    */       {
/* 467:627 */         this.tail.next = timeout;
/* 468:628 */         timeout.prev = this.tail;
/* 469:629 */         this.tail = timeout;
/* 470:    */       }
/* 471:    */     }
/* 472:    */     
/* 473:    */     public void expireTimeouts(long deadline)
/* 474:    */     {
/* 475:637 */       HashedWheelTimer.HashedWheelTimeout timeout = this.head;
/* 476:640 */       while (timeout != null)
/* 477:    */       {
/* 478:641 */         boolean remove = false;
/* 479:642 */         if (timeout.remainingRounds <= 0L)
/* 480:    */         {
/* 481:643 */           if (HashedWheelTimer.HashedWheelTimeout.access$800(timeout) <= deadline) {
/* 482:644 */             timeout.expire();
/* 483:    */           } else {
/* 484:647 */             throw new IllegalStateException(String.format("timeout.deadline (%d) > deadline (%d)", new Object[] { Long.valueOf(HashedWheelTimer.HashedWheelTimeout.access$800(timeout)), Long.valueOf(deadline) }));
/* 485:    */           }
/* 486:650 */           remove = true;
/* 487:    */         }
/* 488:651 */         else if (timeout.isCancelled())
/* 489:    */         {
/* 490:652 */           remove = true;
/* 491:    */         }
/* 492:    */         else
/* 493:    */         {
/* 494:654 */           timeout.remainingRounds -= 1L;
/* 495:    */         }
/* 496:657 */         HashedWheelTimer.HashedWheelTimeout next = timeout.next;
/* 497:658 */         if (remove) {
/* 498:659 */           remove(timeout);
/* 499:    */         }
/* 500:661 */         timeout = next;
/* 501:    */       }
/* 502:    */     }
/* 503:    */     
/* 504:    */     public void remove(HashedWheelTimer.HashedWheelTimeout timeout)
/* 505:    */     {
/* 506:666 */       HashedWheelTimer.HashedWheelTimeout next = timeout.next;
/* 507:668 */       if (timeout.prev != null) {
/* 508:669 */         timeout.prev.next = next;
/* 509:    */       }
/* 510:671 */       if (timeout.next != null) {
/* 511:672 */         timeout.next.prev = timeout.prev;
/* 512:    */       }
/* 513:675 */       if (timeout == this.head)
/* 514:    */       {
/* 515:677 */         if (timeout == this.tail)
/* 516:    */         {
/* 517:678 */           this.tail = null;
/* 518:679 */           this.head = null;
/* 519:    */         }
/* 520:    */         else
/* 521:    */         {
/* 522:681 */           this.head = next;
/* 523:    */         }
/* 524:    */       }
/* 525:683 */       else if (timeout == this.tail) {
/* 526:685 */         this.tail = timeout.prev;
/* 527:    */       }
/* 528:688 */       timeout.prev = null;
/* 529:689 */       timeout.next = null;
/* 530:690 */       timeout.bucket = null;
/* 531:    */     }
/* 532:    */     
/* 533:    */     public void clearTimeouts(Set<Timeout> set)
/* 534:    */     {
/* 535:    */       for (;;)
/* 536:    */       {
/* 537:698 */         HashedWheelTimer.HashedWheelTimeout timeout = pollTimeout();
/* 538:699 */         if (timeout == null) {
/* 539:700 */           return;
/* 540:    */         }
/* 541:702 */         if ((!timeout.isExpired()) && (!timeout.isCancelled())) {
/* 542:705 */           set.add(timeout);
/* 543:    */         }
/* 544:    */       }
/* 545:    */     }
/* 546:    */     
/* 547:    */     private HashedWheelTimer.HashedWheelTimeout pollTimeout()
/* 548:    */     {
/* 549:710 */       HashedWheelTimer.HashedWheelTimeout head = this.head;
/* 550:711 */       if (head == null) {
/* 551:712 */         return null;
/* 552:    */       }
/* 553:714 */       HashedWheelTimer.HashedWheelTimeout next = head.next;
/* 554:715 */       if (next == null)
/* 555:    */       {
/* 556:716 */         this.tail = (this.head = null);
/* 557:    */       }
/* 558:    */       else
/* 559:    */       {
/* 560:718 */         this.head = next;
/* 561:719 */         next.prev = null;
/* 562:    */       }
/* 563:723 */       head.next = null;
/* 564:724 */       head.prev = null;
/* 565:725 */       head.bucket = null;
/* 566:726 */       return head;
/* 567:    */     }
/* 568:    */   }
/* 569:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.HashedWheelTimer
 * JD-Core Version:    0.7.0.1
 */