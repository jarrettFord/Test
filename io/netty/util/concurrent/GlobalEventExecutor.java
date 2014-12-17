/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.logging.InternalLogger;
/*   4:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   5:    */ import java.util.Iterator;
/*   6:    */ import java.util.PriorityQueue;
/*   7:    */ import java.util.Queue;
/*   8:    */ import java.util.concurrent.BlockingQueue;
/*   9:    */ import java.util.concurrent.Callable;
/*  10:    */ import java.util.concurrent.Executors;
/*  11:    */ import java.util.concurrent.LinkedBlockingQueue;
/*  12:    */ import java.util.concurrent.ThreadFactory;
/*  13:    */ import java.util.concurrent.TimeUnit;
/*  14:    */ import java.util.concurrent.atomic.AtomicBoolean;
/*  15:    */ 
/*  16:    */ public final class GlobalEventExecutor
/*  17:    */   extends AbstractEventExecutor
/*  18:    */ {
/*  19: 40 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(GlobalEventExecutor.class);
/*  20: 42 */   private static final long SCHEDULE_PURGE_INTERVAL = TimeUnit.SECONDS.toNanos(1L);
/*  21: 44 */   public static final GlobalEventExecutor INSTANCE = new GlobalEventExecutor();
/*  22: 46 */   final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue();
/*  23: 47 */   final Queue<ScheduledFutureTask<?>> delayedTaskQueue = new PriorityQueue();
/*  24: 48 */   final ScheduledFutureTask<Void> purgeTask = new ScheduledFutureTask(this, this.delayedTaskQueue, Executors.callable(new PurgeTask(null), null), ScheduledFutureTask.deadlineNanos(SCHEDULE_PURGE_INTERVAL), -SCHEDULE_PURGE_INTERVAL);
/*  25: 52 */   private final ThreadFactory threadFactory = new DefaultThreadFactory(getClass());
/*  26: 53 */   private final TaskRunner taskRunner = new TaskRunner();
/*  27: 54 */   private final AtomicBoolean started = new AtomicBoolean();
/*  28:    */   volatile Thread thread;
/*  29: 57 */   private final Future<?> terminationFuture = new FailedFuture(this, new UnsupportedOperationException());
/*  30:    */   
/*  31:    */   private GlobalEventExecutor()
/*  32:    */   {
/*  33: 60 */     this.delayedTaskQueue.add(this.purgeTask);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public EventExecutorGroup parent()
/*  37:    */   {
/*  38: 65 */     return null;
/*  39:    */   }
/*  40:    */   
/*  41:    */   Runnable takeTask()
/*  42:    */   {
/*  43: 74 */     BlockingQueue<Runnable> taskQueue = this.taskQueue;
/*  44:    */     for (;;)
/*  45:    */     {
/*  46: 76 */       ScheduledFutureTask<?> delayedTask = (ScheduledFutureTask)this.delayedTaskQueue.peek();
/*  47: 77 */       if (delayedTask == null)
/*  48:    */       {
/*  49: 78 */         Runnable task = null;
/*  50:    */         try
/*  51:    */         {
/*  52: 80 */           task = (Runnable)taskQueue.take();
/*  53:    */         }
/*  54:    */         catch (InterruptedException e) {}
/*  55: 84 */         return task;
/*  56:    */       }
/*  57: 86 */       long delayNanos = delayedTask.delayNanos();
/*  58:    */       Runnable task;
/*  59: 88 */       if (delayNanos > 0L) {
/*  60:    */         try
/*  61:    */         {
/*  62: 90 */           task = (Runnable)taskQueue.poll(delayNanos, TimeUnit.NANOSECONDS);
/*  63:    */         }
/*  64:    */         catch (InterruptedException e)
/*  65:    */         {
/*  66: 92 */           return null;
/*  67:    */         }
/*  68:    */       } else {
/*  69: 95 */         task = (Runnable)taskQueue.poll();
/*  70:    */       }
/*  71: 98 */       if (task == null)
/*  72:    */       {
/*  73: 99 */         fetchFromDelayedQueue();
/*  74:100 */         task = (Runnable)taskQueue.poll();
/*  75:    */       }
/*  76:103 */       if (task != null) {
/*  77:104 */         return task;
/*  78:    */       }
/*  79:    */     }
/*  80:    */   }
/*  81:    */   
/*  82:    */   private void fetchFromDelayedQueue()
/*  83:    */   {
/*  84:111 */     long nanoTime = 0L;
/*  85:    */     for (;;)
/*  86:    */     {
/*  87:113 */       ScheduledFutureTask<?> delayedTask = (ScheduledFutureTask)this.delayedTaskQueue.peek();
/*  88:114 */       if (delayedTask == null) {
/*  89:    */         break;
/*  90:    */       }
/*  91:118 */       if (nanoTime == 0L) {
/*  92:119 */         nanoTime = ScheduledFutureTask.nanoTime();
/*  93:    */       }
/*  94:122 */       if (delayedTask.deadlineNanos() > nanoTime) {
/*  95:    */         break;
/*  96:    */       }
/*  97:123 */       this.delayedTaskQueue.remove();
/*  98:124 */       this.taskQueue.add(delayedTask);
/*  99:    */     }
/* 100:    */   }
/* 101:    */   
/* 102:    */   public int pendingTasks()
/* 103:    */   {
/* 104:138 */     return this.taskQueue.size();
/* 105:    */   }
/* 106:    */   
/* 107:    */   private void addTask(Runnable task)
/* 108:    */   {
/* 109:146 */     if (task == null) {
/* 110:147 */       throw new NullPointerException("task");
/* 111:    */     }
/* 112:149 */     this.taskQueue.add(task);
/* 113:    */   }
/* 114:    */   
/* 115:    */   public boolean inEventLoop(Thread thread)
/* 116:    */   {
/* 117:154 */     return thread == this.thread;
/* 118:    */   }
/* 119:    */   
/* 120:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/* 121:    */   {
/* 122:159 */     return terminationFuture();
/* 123:    */   }
/* 124:    */   
/* 125:    */   public Future<?> terminationFuture()
/* 126:    */   {
/* 127:164 */     return this.terminationFuture;
/* 128:    */   }
/* 129:    */   
/* 130:    */   @Deprecated
/* 131:    */   public void shutdown()
/* 132:    */   {
/* 133:170 */     throw new UnsupportedOperationException();
/* 134:    */   }
/* 135:    */   
/* 136:    */   public boolean isShuttingDown()
/* 137:    */   {
/* 138:175 */     return false;
/* 139:    */   }
/* 140:    */   
/* 141:    */   public boolean isShutdown()
/* 142:    */   {
/* 143:180 */     return false;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public boolean isTerminated()
/* 147:    */   {
/* 148:185 */     return false;
/* 149:    */   }
/* 150:    */   
/* 151:    */   public boolean awaitTermination(long timeout, TimeUnit unit)
/* 152:    */   {
/* 153:190 */     return false;
/* 154:    */   }
/* 155:    */   
/* 156:    */   public boolean awaitInactivity(long timeout, TimeUnit unit)
/* 157:    */     throws InterruptedException
/* 158:    */   {
/* 159:202 */     if (unit == null) {
/* 160:203 */       throw new NullPointerException("unit");
/* 161:    */     }
/* 162:206 */     Thread thread = this.thread;
/* 163:207 */     if (thread != null) {
/* 164:208 */       thread.join(unit.toMillis(timeout));
/* 165:    */     }
/* 166:210 */     return !thread.isAlive();
/* 167:    */   }
/* 168:    */   
/* 169:    */   public void execute(Runnable task)
/* 170:    */   {
/* 171:215 */     if (task == null) {
/* 172:216 */       throw new NullPointerException("task");
/* 173:    */     }
/* 174:219 */     addTask(task);
/* 175:220 */     if (!inEventLoop()) {
/* 176:221 */       startThread();
/* 177:    */     }
/* 178:    */   }
/* 179:    */   
/* 180:    */   public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
/* 181:    */   {
/* 182:229 */     if (command == null) {
/* 183:230 */       throw new NullPointerException("command");
/* 184:    */     }
/* 185:232 */     if (unit == null) {
/* 186:233 */       throw new NullPointerException("unit");
/* 187:    */     }
/* 188:235 */     if (delay < 0L) {
/* 189:236 */       throw new IllegalArgumentException(String.format("delay: %d (expected: >= 0)", new Object[] { Long.valueOf(delay) }));
/* 190:    */     }
/* 191:239 */     return schedule(new ScheduledFutureTask(this, this.delayedTaskQueue, command, null, ScheduledFutureTask.deadlineNanos(unit.toNanos(delay))));
/* 192:    */   }
/* 193:    */   
/* 194:    */   public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
/* 195:    */   {
/* 196:245 */     if (callable == null) {
/* 197:246 */       throw new NullPointerException("callable");
/* 198:    */     }
/* 199:248 */     if (unit == null) {
/* 200:249 */       throw new NullPointerException("unit");
/* 201:    */     }
/* 202:251 */     if (delay < 0L) {
/* 203:252 */       throw new IllegalArgumentException(String.format("delay: %d (expected: >= 0)", new Object[] { Long.valueOf(delay) }));
/* 204:    */     }
/* 205:255 */     return schedule(new ScheduledFutureTask(this, this.delayedTaskQueue, callable, ScheduledFutureTask.deadlineNanos(unit.toNanos(delay))));
/* 206:    */   }
/* 207:    */   
/* 208:    */   public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
/* 209:    */   {
/* 210:261 */     if (command == null) {
/* 211:262 */       throw new NullPointerException("command");
/* 212:    */     }
/* 213:264 */     if (unit == null) {
/* 214:265 */       throw new NullPointerException("unit");
/* 215:    */     }
/* 216:267 */     if (initialDelay < 0L) {
/* 217:268 */       throw new IllegalArgumentException(String.format("initialDelay: %d (expected: >= 0)", new Object[] { Long.valueOf(initialDelay) }));
/* 218:    */     }
/* 219:271 */     if (period <= 0L) {
/* 220:272 */       throw new IllegalArgumentException(String.format("period: %d (expected: > 0)", new Object[] { Long.valueOf(period) }));
/* 221:    */     }
/* 222:276 */     return schedule(new ScheduledFutureTask(this, this.delayedTaskQueue, Executors.callable(command, null), ScheduledFutureTask.deadlineNanos(unit.toNanos(initialDelay)), unit.toNanos(period)));
/* 223:    */   }
/* 224:    */   
/* 225:    */   public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
/* 226:    */   {
/* 227:283 */     if (command == null) {
/* 228:284 */       throw new NullPointerException("command");
/* 229:    */     }
/* 230:286 */     if (unit == null) {
/* 231:287 */       throw new NullPointerException("unit");
/* 232:    */     }
/* 233:289 */     if (initialDelay < 0L) {
/* 234:290 */       throw new IllegalArgumentException(String.format("initialDelay: %d (expected: >= 0)", new Object[] { Long.valueOf(initialDelay) }));
/* 235:    */     }
/* 236:293 */     if (delay <= 0L) {
/* 237:294 */       throw new IllegalArgumentException(String.format("delay: %d (expected: > 0)", new Object[] { Long.valueOf(delay) }));
/* 238:    */     }
/* 239:298 */     return schedule(new ScheduledFutureTask(this, this.delayedTaskQueue, Executors.callable(command, null), ScheduledFutureTask.deadlineNanos(unit.toNanos(initialDelay)), -unit.toNanos(delay)));
/* 240:    */   }
/* 241:    */   
/* 242:    */   private <V> ScheduledFuture<V> schedule(final ScheduledFutureTask<V> task)
/* 243:    */   {
/* 244:304 */     if (task == null) {
/* 245:305 */       throw new NullPointerException("task");
/* 246:    */     }
/* 247:308 */     if (inEventLoop()) {
/* 248:309 */       this.delayedTaskQueue.add(task);
/* 249:    */     } else {
/* 250:311 */       execute(new Runnable()
/* 251:    */       {
/* 252:    */         public void run()
/* 253:    */         {
/* 254:314 */           GlobalEventExecutor.this.delayedTaskQueue.add(task);
/* 255:    */         }
/* 256:    */       });
/* 257:    */     }
/* 258:319 */     return task;
/* 259:    */   }
/* 260:    */   
/* 261:    */   private void startThread()
/* 262:    */   {
/* 263:323 */     if (this.started.compareAndSet(false, true))
/* 264:    */     {
/* 265:324 */       Thread t = this.threadFactory.newThread(this.taskRunner);
/* 266:325 */       t.start();
/* 267:326 */       this.thread = t;
/* 268:    */     }
/* 269:    */   }
/* 270:    */   
/* 271:    */   final class TaskRunner
/* 272:    */     implements Runnable
/* 273:    */   {
/* 274:    */     TaskRunner() {}
/* 275:    */     
/* 276:    */     public void run()
/* 277:    */     {
/* 278:    */       for (;;)
/* 279:    */       {
/* 280:334 */         Runnable task = GlobalEventExecutor.this.takeTask();
/* 281:335 */         if (task != null)
/* 282:    */         {
/* 283:    */           try
/* 284:    */           {
/* 285:337 */             task.run();
/* 286:    */           }
/* 287:    */           catch (Throwable t)
/* 288:    */           {
/* 289:339 */             GlobalEventExecutor.logger.warn("Unexpected exception from the global event executor: ", t);
/* 290:    */           }
/* 291:342 */           if (task != GlobalEventExecutor.this.purgeTask) {}
/* 292:    */         }
/* 293:348 */         else if ((GlobalEventExecutor.this.taskQueue.isEmpty()) && (GlobalEventExecutor.this.delayedTaskQueue.size() == 1))
/* 294:    */         {
/* 295:352 */           boolean stopped = GlobalEventExecutor.this.started.compareAndSet(true, false);
/* 296:353 */           assert (stopped);
/* 297:356 */           if ((GlobalEventExecutor.this.taskQueue.isEmpty()) && (GlobalEventExecutor.this.delayedTaskQueue.size() == 1)) {
/* 298:    */             break;
/* 299:    */           }
/* 300:365 */           if (!GlobalEventExecutor.this.started.compareAndSet(false, true)) {
/* 301:    */             break;
/* 302:    */           }
/* 303:    */         }
/* 304:    */       }
/* 305:    */     }
/* 306:    */   }
/* 307:    */   
/* 308:    */   private final class PurgeTask
/* 309:    */     implements Runnable
/* 310:    */   {
/* 311:    */     private PurgeTask() {}
/* 312:    */     
/* 313:    */     public void run()
/* 314:    */     {
/* 315:382 */       Iterator<ScheduledFutureTask<?>> i = GlobalEventExecutor.this.delayedTaskQueue.iterator();
/* 316:383 */       while (i.hasNext())
/* 317:    */       {
/* 318:384 */         ScheduledFutureTask<?> task = (ScheduledFutureTask)i.next();
/* 319:385 */         if (task.isCancelled()) {
/* 320:386 */           i.remove();
/* 321:    */         }
/* 322:    */       }
/* 323:    */     }
/* 324:    */   }
/* 325:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.GlobalEventExecutor
 * JD-Core Version:    0.7.0.1
 */