/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import java.util.Collections;
/*   4:    */ import java.util.Iterator;
/*   5:    */ import java.util.LinkedHashMap;
/*   6:    */ import java.util.Set;
/*   7:    */ import java.util.concurrent.ThreadFactory;
/*   8:    */ import java.util.concurrent.TimeUnit;
/*   9:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  10:    */ 
/*  11:    */ public abstract class MultithreadEventExecutorGroup
/*  12:    */   extends AbstractEventExecutorGroup
/*  13:    */ {
/*  14:    */   private final EventExecutor[] children;
/*  15: 33 */   private final AtomicInteger childIndex = new AtomicInteger();
/*  16: 34 */   private final AtomicInteger terminatedChildren = new AtomicInteger();
/*  17: 35 */   private final Promise<?> terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
/*  18:    */   private final EventExecutorChooser chooser;
/*  19:    */   
/*  20:    */   protected MultithreadEventExecutorGroup(int nThreads, ThreadFactory threadFactory, Object... args)
/*  21:    */   {
/*  22: 46 */     if (nThreads <= 0) {
/*  23: 47 */       throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", new Object[] { Integer.valueOf(nThreads) }));
/*  24:    */     }
/*  25: 50 */     if (threadFactory == null) {
/*  26: 51 */       threadFactory = newDefaultThreadFactory();
/*  27:    */     }
/*  28: 54 */     this.children = new SingleThreadEventExecutor[nThreads];
/*  29: 55 */     if (isPowerOfTwo(this.children.length)) {
/*  30: 56 */       this.chooser = new PowerOfTwoEventExecutorChooser(null);
/*  31:    */     } else {
/*  32: 58 */       this.chooser = new GenericEventExecutorChooser(null);
/*  33:    */     }
/*  34: 61 */     for (int i = 0; i < nThreads; i++)
/*  35:    */     {
/*  36: 62 */       boolean success = false;
/*  37:    */       try
/*  38:    */       {
/*  39: 64 */         this.children[i] = newChild(threadFactory, args);
/*  40: 65 */         success = true;
/*  41:    */       }
/*  42:    */       catch (Exception e)
/*  43:    */       {
/*  44:    */         int j;
/*  45:    */         int j;
/*  46:    */         EventExecutor e;
/*  47: 68 */         throw new IllegalStateException("failed to create a child event loop", e);
/*  48:    */       }
/*  49:    */       finally
/*  50:    */       {
/*  51: 70 */         if (!success)
/*  52:    */         {
/*  53: 71 */           for (int j = 0; j < i; j++) {
/*  54: 72 */             this.children[j].shutdownGracefully();
/*  55:    */           }
/*  56: 75 */           for (int j = 0; j < i; j++)
/*  57:    */           {
/*  58: 76 */             EventExecutor e = this.children[j];
/*  59:    */             try
/*  60:    */             {
/*  61: 78 */               while (!e.isTerminated()) {
/*  62: 79 */                 e.awaitTermination(2147483647L, TimeUnit.SECONDS);
/*  63:    */               }
/*  64:    */             }
/*  65:    */             catch (InterruptedException interrupted)
/*  66:    */             {
/*  67: 82 */               Thread.currentThread().interrupt();
/*  68: 83 */               break;
/*  69:    */             }
/*  70:    */           }
/*  71:    */         }
/*  72:    */       }
/*  73:    */     }
/*  74: 90 */     FutureListener<Object> terminationListener = new FutureListener()
/*  75:    */     {
/*  76:    */       public void operationComplete(Future<Object> future)
/*  77:    */         throws Exception
/*  78:    */       {
/*  79: 93 */         if (MultithreadEventExecutorGroup.this.terminatedChildren.incrementAndGet() == MultithreadEventExecutorGroup.this.children.length) {
/*  80: 94 */           MultithreadEventExecutorGroup.this.terminationFuture.setSuccess(null);
/*  81:    */         }
/*  82:    */       }
/*  83:    */     };
/*  84: 99 */     for (EventExecutor e : this.children) {
/*  85:100 */       e.terminationFuture().addListener(terminationListener);
/*  86:    */     }
/*  87:    */   }
/*  88:    */   
/*  89:    */   protected ThreadFactory newDefaultThreadFactory()
/*  90:    */   {
/*  91:105 */     return new DefaultThreadFactory(getClass());
/*  92:    */   }
/*  93:    */   
/*  94:    */   public EventExecutor next()
/*  95:    */   {
/*  96:110 */     return this.chooser.next();
/*  97:    */   }
/*  98:    */   
/*  99:    */   public Iterator<EventExecutor> iterator()
/* 100:    */   {
/* 101:115 */     return children().iterator();
/* 102:    */   }
/* 103:    */   
/* 104:    */   public final int executorCount()
/* 105:    */   {
/* 106:123 */     return this.children.length;
/* 107:    */   }
/* 108:    */   
/* 109:    */   protected Set<EventExecutor> children()
/* 110:    */   {
/* 111:130 */     Set<EventExecutor> children = Collections.newSetFromMap(new LinkedHashMap());
/* 112:131 */     Collections.addAll(children, this.children);
/* 113:132 */     return children;
/* 114:    */   }
/* 115:    */   
/* 116:    */   protected abstract EventExecutor newChild(ThreadFactory paramThreadFactory, Object... paramVarArgs)
/* 117:    */     throws Exception;
/* 118:    */   
/* 119:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/* 120:    */   {
/* 121:145 */     for (EventExecutor l : this.children) {
/* 122:146 */       l.shutdownGracefully(quietPeriod, timeout, unit);
/* 123:    */     }
/* 124:148 */     return terminationFuture();
/* 125:    */   }
/* 126:    */   
/* 127:    */   public Future<?> terminationFuture()
/* 128:    */   {
/* 129:153 */     return this.terminationFuture;
/* 130:    */   }
/* 131:    */   
/* 132:    */   @Deprecated
/* 133:    */   public void shutdown()
/* 134:    */   {
/* 135:159 */     for (EventExecutor l : this.children) {
/* 136:160 */       l.shutdown();
/* 137:    */     }
/* 138:    */   }
/* 139:    */   
/* 140:    */   public boolean isShuttingDown()
/* 141:    */   {
/* 142:166 */     for (EventExecutor l : this.children) {
/* 143:167 */       if (!l.isShuttingDown()) {
/* 144:168 */         return false;
/* 145:    */       }
/* 146:    */     }
/* 147:171 */     return true;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public boolean isShutdown()
/* 151:    */   {
/* 152:176 */     for (EventExecutor l : this.children) {
/* 153:177 */       if (!l.isShutdown()) {
/* 154:178 */         return false;
/* 155:    */       }
/* 156:    */     }
/* 157:181 */     return true;
/* 158:    */   }
/* 159:    */   
/* 160:    */   public boolean isTerminated()
/* 161:    */   {
/* 162:186 */     for (EventExecutor l : this.children) {
/* 163:187 */       if (!l.isTerminated()) {
/* 164:188 */         return false;
/* 165:    */       }
/* 166:    */     }
/* 167:191 */     return true;
/* 168:    */   }
/* 169:    */   
/* 170:    */   public boolean awaitTermination(long timeout, TimeUnit unit)
/* 171:    */     throws InterruptedException
/* 172:    */   {
/* 173:197 */     long deadline = System.nanoTime() + unit.toNanos(timeout);
/* 174:198 */     for (EventExecutor l : this.children) {
/* 175:    */       for (;;)
/* 176:    */       {
/* 177:200 */         long timeLeft = deadline - System.nanoTime();
/* 178:201 */         if (timeLeft <= 0L) {
/* 179:    */           break label84;
/* 180:    */         }
/* 181:204 */         if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS)) {
/* 182:    */           break;
/* 183:    */         }
/* 184:    */       }
/* 185:    */     }
/* 186:    */     label84:
/* 187:209 */     return isTerminated();
/* 188:    */   }
/* 189:    */   
/* 190:    */   private static boolean isPowerOfTwo(int val)
/* 191:    */   {
/* 192:213 */     return (val & -val) == val;
/* 193:    */   }
/* 194:    */   
/* 195:    */   private static abstract interface EventExecutorChooser
/* 196:    */   {
/* 197:    */     public abstract EventExecutor next();
/* 198:    */   }
/* 199:    */   
/* 200:    */   private final class PowerOfTwoEventExecutorChooser
/* 201:    */     implements MultithreadEventExecutorGroup.EventExecutorChooser
/* 202:    */   {
/* 203:    */     private PowerOfTwoEventExecutorChooser() {}
/* 204:    */     
/* 205:    */     public EventExecutor next()
/* 206:    */     {
/* 207:223 */       return MultithreadEventExecutorGroup.this.children[(MultithreadEventExecutorGroup.this.childIndex.getAndIncrement() & MultithreadEventExecutorGroup.this.children.length - 1)];
/* 208:    */     }
/* 209:    */   }
/* 210:    */   
/* 211:    */   private final class GenericEventExecutorChooser
/* 212:    */     implements MultithreadEventExecutorGroup.EventExecutorChooser
/* 213:    */   {
/* 214:    */     private GenericEventExecutorChooser() {}
/* 215:    */     
/* 216:    */     public EventExecutor next()
/* 217:    */     {
/* 218:230 */       return MultithreadEventExecutorGroup.this.children[java.lang.Math.abs(MultithreadEventExecutorGroup.this.childIndex.getAndIncrement() % MultithreadEventExecutorGroup.this.children.length)];
/* 219:    */     }
/* 220:    */   }
/* 221:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.MultithreadEventExecutorGroup
 * JD-Core Version:    0.7.0.1
 */