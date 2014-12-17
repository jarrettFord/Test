/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.DefaultThreadFactory;
/*   4:    */ import io.netty.util.internal.MpscLinkedQueueNode;
/*   5:    */ import io.netty.util.internal.PlatformDependent;
/*   6:    */ import io.netty.util.internal.logging.InternalLogger;
/*   7:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   8:    */ import java.util.ArrayList;
/*   9:    */ import java.util.List;
/*  10:    */ import java.util.Queue;
/*  11:    */ import java.util.concurrent.ThreadFactory;
/*  12:    */ import java.util.concurrent.TimeUnit;
/*  13:    */ import java.util.concurrent.atomic.AtomicBoolean;
/*  14:    */ 
/*  15:    */ public final class ThreadDeathWatcher
/*  16:    */ {
/*  17: 42 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ThreadDeathWatcher.class);
/*  18: 43 */   private static final ThreadFactory threadFactory = new DefaultThreadFactory(ThreadDeathWatcher.class, true, 1);
/*  19: 46 */   private static final Queue<Entry> pendingEntries = PlatformDependent.newMpscQueue();
/*  20: 47 */   private static final Watcher watcher = new Watcher(null);
/*  21: 48 */   private static final AtomicBoolean started = new AtomicBoolean();
/*  22:    */   private static volatile Thread watcherThread;
/*  23:    */   
/*  24:    */   public static void watch(Thread thread, Runnable task)
/*  25:    */   {
/*  26: 60 */     if (thread == null) {
/*  27: 61 */       throw new NullPointerException("thread");
/*  28:    */     }
/*  29: 63 */     if (task == null) {
/*  30: 64 */       throw new NullPointerException("task");
/*  31:    */     }
/*  32: 66 */     if (!thread.isAlive()) {
/*  33: 67 */       throw new IllegalArgumentException("thread must be alive.");
/*  34:    */     }
/*  35: 70 */     schedule(thread, task, true);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public static void unwatch(Thread thread, Runnable task)
/*  39:    */   {
/*  40: 77 */     if (thread == null) {
/*  41: 78 */       throw new NullPointerException("thread");
/*  42:    */     }
/*  43: 80 */     if (task == null) {
/*  44: 81 */       throw new NullPointerException("task");
/*  45:    */     }
/*  46: 84 */     schedule(thread, task, false);
/*  47:    */   }
/*  48:    */   
/*  49:    */   private static void schedule(Thread thread, Runnable task, boolean isWatch)
/*  50:    */   {
/*  51: 88 */     pendingEntries.add(new Entry(thread, task, isWatch));
/*  52: 90 */     if (started.compareAndSet(false, true))
/*  53:    */     {
/*  54: 91 */       Thread watcherThread = threadFactory.newThread(watcher);
/*  55: 92 */       watcherThread.start();
/*  56: 93 */       watcherThread = watcherThread;
/*  57:    */     }
/*  58:    */   }
/*  59:    */   
/*  60:    */   public static boolean awaitInactivity(long timeout, TimeUnit unit)
/*  61:    */     throws InterruptedException
/*  62:    */   {
/*  63:107 */     if (unit == null) {
/*  64:108 */       throw new NullPointerException("unit");
/*  65:    */     }
/*  66:111 */     Thread watcherThread = watcherThread;
/*  67:112 */     if (watcherThread != null)
/*  68:    */     {
/*  69:113 */       watcherThread.join(unit.toMillis(timeout));
/*  70:114 */       return !watcherThread.isAlive();
/*  71:    */     }
/*  72:116 */     return true;
/*  73:    */   }
/*  74:    */   
/*  75:    */   private static final class Watcher
/*  76:    */     implements Runnable
/*  77:    */   {
/*  78:124 */     private final List<ThreadDeathWatcher.Entry> watchees = new ArrayList();
/*  79:    */     
/*  80:    */     public void run()
/*  81:    */     {
/*  82:    */       for (;;)
/*  83:    */       {
/*  84:129 */         fetchWatchees();
/*  85:130 */         notifyWatchees();
/*  86:    */         
/*  87:    */ 
/*  88:133 */         fetchWatchees();
/*  89:134 */         notifyWatchees();
/*  90:    */         try
/*  91:    */         {
/*  92:137 */           Thread.sleep(1000L);
/*  93:    */         }
/*  94:    */         catch (InterruptedException ignore) {}
/*  95:142 */         if ((this.watchees.isEmpty()) && (ThreadDeathWatcher.pendingEntries.isEmpty()))
/*  96:    */         {
/*  97:147 */           boolean stopped = ThreadDeathWatcher.started.compareAndSet(true, false);
/*  98:148 */           assert (stopped);
/*  99:151 */           if (ThreadDeathWatcher.pendingEntries.isEmpty()) {
/* 100:    */             break;
/* 101:    */           }
/* 102:160 */           if (!ThreadDeathWatcher.started.compareAndSet(false, true)) {
/* 103:    */             break;
/* 104:    */           }
/* 105:    */         }
/* 106:    */       }
/* 107:    */     }
/* 108:    */     
/* 109:    */     private void fetchWatchees()
/* 110:    */     {
/* 111:    */       for (;;)
/* 112:    */       {
/* 113:175 */         ThreadDeathWatcher.Entry e = (ThreadDeathWatcher.Entry)ThreadDeathWatcher.pendingEntries.poll();
/* 114:176 */         if (e == null) {
/* 115:    */           break;
/* 116:    */         }
/* 117:180 */         if (e.isWatch) {
/* 118:181 */           this.watchees.add(e);
/* 119:    */         } else {
/* 120:183 */           this.watchees.remove(e);
/* 121:    */         }
/* 122:    */       }
/* 123:    */     }
/* 124:    */     
/* 125:    */     private void notifyWatchees()
/* 126:    */     {
/* 127:189 */       List<ThreadDeathWatcher.Entry> watchees = this.watchees;
/* 128:190 */       for (int i = 0; i < watchees.size();)
/* 129:    */       {
/* 130:191 */         ThreadDeathWatcher.Entry e = (ThreadDeathWatcher.Entry)watchees.get(i);
/* 131:192 */         if (!e.thread.isAlive())
/* 132:    */         {
/* 133:193 */           watchees.remove(i);
/* 134:    */           try
/* 135:    */           {
/* 136:195 */             e.task.run();
/* 137:    */           }
/* 138:    */           catch (Throwable t)
/* 139:    */           {
/* 140:197 */             ThreadDeathWatcher.logger.warn("Thread death watcher task raised an exception:", t);
/* 141:    */           }
/* 142:    */         }
/* 143:    */         else
/* 144:    */         {
/* 145:200 */           i++;
/* 146:    */         }
/* 147:    */       }
/* 148:    */     }
/* 149:    */   }
/* 150:    */   
/* 151:    */   private static final class Entry
/* 152:    */     extends MpscLinkedQueueNode<Entry>
/* 153:    */   {
/* 154:    */     final Thread thread;
/* 155:    */     final Runnable task;
/* 156:    */     final boolean isWatch;
/* 157:    */     
/* 158:    */     Entry(Thread thread, Runnable task, boolean isWatch)
/* 159:    */     {
/* 160:212 */       this.thread = thread;
/* 161:213 */       this.task = task;
/* 162:214 */       this.isWatch = isWatch;
/* 163:    */     }
/* 164:    */     
/* 165:    */     public Entry value()
/* 166:    */     {
/* 167:219 */       return this;
/* 168:    */     }
/* 169:    */     
/* 170:    */     public int hashCode()
/* 171:    */     {
/* 172:224 */       return this.thread.hashCode() ^ this.task.hashCode();
/* 173:    */     }
/* 174:    */     
/* 175:    */     public boolean equals(Object obj)
/* 176:    */     {
/* 177:229 */       if (obj == this) {
/* 178:230 */         return true;
/* 179:    */       }
/* 180:233 */       if (!(obj instanceof Entry)) {
/* 181:234 */         return false;
/* 182:    */       }
/* 183:237 */       Entry that = (Entry)obj;
/* 184:238 */       return (this.thread == that.thread) && (this.task == that.task);
/* 185:    */     }
/* 186:    */   }
/* 187:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.ThreadDeathWatcher
 * JD-Core Version:    0.7.0.1
 */