/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import java.util.Collections;
/*   4:    */ import java.util.Iterator;
/*   5:    */ import java.util.List;
/*   6:    */ import java.util.NoSuchElementException;
/*   7:    */ import java.util.concurrent.AbstractExecutorService;
/*   8:    */ import java.util.concurrent.Callable;
/*   9:    */ import java.util.concurrent.RunnableFuture;
/*  10:    */ import java.util.concurrent.TimeUnit;
/*  11:    */ 
/*  12:    */ public abstract class AbstractEventExecutor
/*  13:    */   extends AbstractExecutorService
/*  14:    */   implements EventExecutor
/*  15:    */ {
/*  16:    */   public EventExecutor next()
/*  17:    */   {
/*  18: 34 */     return this;
/*  19:    */   }
/*  20:    */   
/*  21:    */   public boolean inEventLoop()
/*  22:    */   {
/*  23: 39 */     return inEventLoop(Thread.currentThread());
/*  24:    */   }
/*  25:    */   
/*  26:    */   public Iterator<EventExecutor> iterator()
/*  27:    */   {
/*  28: 44 */     return new EventExecutorIterator(null);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public Future<?> shutdownGracefully()
/*  32:    */   {
/*  33: 49 */     return shutdownGracefully(2L, 15L, TimeUnit.SECONDS);
/*  34:    */   }
/*  35:    */   
/*  36:    */   @Deprecated
/*  37:    */   public abstract void shutdown();
/*  38:    */   
/*  39:    */   @Deprecated
/*  40:    */   public List<Runnable> shutdownNow()
/*  41:    */   {
/*  42: 65 */     shutdown();
/*  43: 66 */     return Collections.emptyList();
/*  44:    */   }
/*  45:    */   
/*  46:    */   public <V> Promise<V> newPromise()
/*  47:    */   {
/*  48: 71 */     return new DefaultPromise(this);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public <V> ProgressivePromise<V> newProgressivePromise()
/*  52:    */   {
/*  53: 76 */     return new DefaultProgressivePromise(this);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public <V> Future<V> newSucceededFuture(V result)
/*  57:    */   {
/*  58: 81 */     return new SucceededFuture(this, result);
/*  59:    */   }
/*  60:    */   
/*  61:    */   public <V> Future<V> newFailedFuture(Throwable cause)
/*  62:    */   {
/*  63: 86 */     return new FailedFuture(this, cause);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public Future<?> submit(Runnable task)
/*  67:    */   {
/*  68: 91 */     return (Future)super.submit(task);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public <T> Future<T> submit(Runnable task, T result)
/*  72:    */   {
/*  73: 96 */     return (Future)super.submit(task, result);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public <T> Future<T> submit(Callable<T> task)
/*  77:    */   {
/*  78:101 */     return (Future)super.submit(task);
/*  79:    */   }
/*  80:    */   
/*  81:    */   protected final <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value)
/*  82:    */   {
/*  83:106 */     return new PromiseTask(this, runnable, value);
/*  84:    */   }
/*  85:    */   
/*  86:    */   protected final <T> RunnableFuture<T> newTaskFor(Callable<T> callable)
/*  87:    */   {
/*  88:111 */     return new PromiseTask(this, callable);
/*  89:    */   }
/*  90:    */   
/*  91:    */   public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
/*  92:    */   {
/*  93:117 */     throw new UnsupportedOperationException();
/*  94:    */   }
/*  95:    */   
/*  96:    */   public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
/*  97:    */   {
/*  98:122 */     throw new UnsupportedOperationException();
/*  99:    */   }
/* 100:    */   
/* 101:    */   public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
/* 102:    */   {
/* 103:127 */     throw new UnsupportedOperationException();
/* 104:    */   }
/* 105:    */   
/* 106:    */   public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
/* 107:    */   {
/* 108:132 */     throw new UnsupportedOperationException();
/* 109:    */   }
/* 110:    */   
/* 111:    */   private final class EventExecutorIterator
/* 112:    */     implements Iterator<EventExecutor>
/* 113:    */   {
/* 114:    */     private boolean nextCalled;
/* 115:    */     
/* 116:    */     private EventExecutorIterator() {}
/* 117:    */     
/* 118:    */     public boolean hasNext()
/* 119:    */     {
/* 120:140 */       return !this.nextCalled;
/* 121:    */     }
/* 122:    */     
/* 123:    */     public EventExecutor next()
/* 124:    */     {
/* 125:145 */       if (!hasNext()) {
/* 126:146 */         throw new NoSuchElementException();
/* 127:    */       }
/* 128:148 */       this.nextCalled = true;
/* 129:149 */       return AbstractEventExecutor.this;
/* 130:    */     }
/* 131:    */     
/* 132:    */     public void remove()
/* 133:    */     {
/* 134:154 */       throw new UnsupportedOperationException("read-only");
/* 135:    */     }
/* 136:    */   }
/* 137:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.AbstractEventExecutor
 * JD-Core Version:    0.7.0.1
 */