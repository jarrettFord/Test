/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import java.util.Queue;
/*   4:    */ import java.util.concurrent.Callable;
/*   5:    */ import java.util.concurrent.Delayed;
/*   6:    */ import java.util.concurrent.TimeUnit;
/*   7:    */ import java.util.concurrent.atomic.AtomicLong;
/*   8:    */ 
/*   9:    */ final class ScheduledFutureTask<V>
/*  10:    */   extends PromiseTask<V>
/*  11:    */   implements ScheduledFuture<V>
/*  12:    */ {
/*  13: 27 */   private static final AtomicLong nextTaskId = new AtomicLong();
/*  14: 28 */   private static final long START_TIME = System.nanoTime();
/*  15:    */   
/*  16:    */   static long nanoTime()
/*  17:    */   {
/*  18: 31 */     return System.nanoTime() - START_TIME;
/*  19:    */   }
/*  20:    */   
/*  21:    */   static long deadlineNanos(long delay)
/*  22:    */   {
/*  23: 35 */     return nanoTime() + delay;
/*  24:    */   }
/*  25:    */   
/*  26: 38 */   private final long id = nextTaskId.getAndIncrement();
/*  27:    */   private final Queue<ScheduledFutureTask<?>> delayedTaskQueue;
/*  28:    */   private long deadlineNanos;
/*  29:    */   private final long periodNanos;
/*  30:    */   
/*  31:    */   ScheduledFutureTask(EventExecutor executor, Queue<ScheduledFutureTask<?>> delayedTaskQueue, Runnable runnable, V result, long nanoTime)
/*  32:    */   {
/*  33: 48 */     this(executor, delayedTaskQueue, toCallable(runnable, result), nanoTime);
/*  34:    */   }
/*  35:    */   
/*  36:    */   ScheduledFutureTask(EventExecutor executor, Queue<ScheduledFutureTask<?>> delayedTaskQueue, Callable<V> callable, long nanoTime, long period)
/*  37:    */   {
/*  38: 55 */     super(executor, callable);
/*  39: 56 */     if (period == 0L) {
/*  40: 57 */       throw new IllegalArgumentException("period: 0 (expected: != 0)");
/*  41:    */     }
/*  42: 59 */     this.delayedTaskQueue = delayedTaskQueue;
/*  43: 60 */     this.deadlineNanos = nanoTime;
/*  44: 61 */     this.periodNanos = period;
/*  45:    */   }
/*  46:    */   
/*  47:    */   ScheduledFutureTask(EventExecutor executor, Queue<ScheduledFutureTask<?>> delayedTaskQueue, Callable<V> callable, long nanoTime)
/*  48:    */   {
/*  49: 68 */     super(executor, callable);
/*  50: 69 */     this.delayedTaskQueue = delayedTaskQueue;
/*  51: 70 */     this.deadlineNanos = nanoTime;
/*  52: 71 */     this.periodNanos = 0L;
/*  53:    */   }
/*  54:    */   
/*  55:    */   protected EventExecutor executor()
/*  56:    */   {
/*  57: 76 */     return super.executor();
/*  58:    */   }
/*  59:    */   
/*  60:    */   public long deadlineNanos()
/*  61:    */   {
/*  62: 80 */     return this.deadlineNanos;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public long delayNanos()
/*  66:    */   {
/*  67: 84 */     return Math.max(0L, deadlineNanos() - nanoTime());
/*  68:    */   }
/*  69:    */   
/*  70:    */   public long delayNanos(long currentTimeNanos)
/*  71:    */   {
/*  72: 88 */     return Math.max(0L, deadlineNanos() - (currentTimeNanos - START_TIME));
/*  73:    */   }
/*  74:    */   
/*  75:    */   public long getDelay(TimeUnit unit)
/*  76:    */   {
/*  77: 93 */     return unit.convert(delayNanos(), TimeUnit.NANOSECONDS);
/*  78:    */   }
/*  79:    */   
/*  80:    */   public int compareTo(Delayed o)
/*  81:    */   {
/*  82: 98 */     if (this == o) {
/*  83: 99 */       return 0;
/*  84:    */     }
/*  85:102 */     ScheduledFutureTask<?> that = (ScheduledFutureTask)o;
/*  86:103 */     long d = deadlineNanos() - that.deadlineNanos();
/*  87:104 */     if (d < 0L) {
/*  88:105 */       return -1;
/*  89:    */     }
/*  90:106 */     if (d > 0L) {
/*  91:107 */       return 1;
/*  92:    */     }
/*  93:108 */     if (this.id < that.id) {
/*  94:109 */       return -1;
/*  95:    */     }
/*  96:110 */     if (this.id == that.id) {
/*  97:111 */       throw new Error();
/*  98:    */     }
/*  99:113 */     return 1;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public void run()
/* 103:    */   {
/* 104:119 */     assert (executor().inEventLoop());
/* 105:    */     try
/* 106:    */     {
/* 107:121 */       if (this.periodNanos == 0L)
/* 108:    */       {
/* 109:122 */         if (setUncancellableInternal())
/* 110:    */         {
/* 111:123 */           V result = this.task.call();
/* 112:124 */           setSuccessInternal(result);
/* 113:    */         }
/* 114:    */       }
/* 115:128 */       else if (!isCancelled())
/* 116:    */       {
/* 117:129 */         this.task.call();
/* 118:130 */         if (!executor().isShutdown())
/* 119:    */         {
/* 120:131 */           long p = this.periodNanos;
/* 121:132 */           if (p > 0L) {
/* 122:133 */             this.deadlineNanos += p;
/* 123:    */           } else {
/* 124:135 */             this.deadlineNanos = (nanoTime() - p);
/* 125:    */           }
/* 126:137 */           if (!isCancelled()) {
/* 127:138 */             this.delayedTaskQueue.add(this);
/* 128:    */           }
/* 129:    */         }
/* 130:    */       }
/* 131:    */     }
/* 132:    */     catch (Throwable cause)
/* 133:    */     {
/* 134:144 */       setFailureInternal(cause);
/* 135:    */     }
/* 136:    */   }
/* 137:    */   
/* 138:    */   protected StringBuilder toStringBuilder()
/* 139:    */   {
/* 140:150 */     StringBuilder buf = super.toStringBuilder();
/* 141:151 */     buf.setCharAt(buf.length() - 1, ',');
/* 142:152 */     buf.append(" id: ");
/* 143:153 */     buf.append(this.id);
/* 144:154 */     buf.append(", deadline: ");
/* 145:155 */     buf.append(this.deadlineNanos);
/* 146:156 */     buf.append(", period: ");
/* 147:157 */     buf.append(this.periodNanos);
/* 148:158 */     buf.append(')');
/* 149:159 */     return buf;
/* 150:    */   }
/* 151:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.ScheduledFutureTask
 * JD-Core Version:    0.7.0.1
 */