/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import java.util.concurrent.TimeUnit;
/*   4:    */ 
/*   5:    */ public final class ImmediateEventExecutor
/*   6:    */   extends AbstractEventExecutor
/*   7:    */ {
/*   8: 24 */   public static final ImmediateEventExecutor INSTANCE = new ImmediateEventExecutor();
/*   9: 26 */   private final Future<?> terminationFuture = new FailedFuture(GlobalEventExecutor.INSTANCE, new UnsupportedOperationException());
/*  10:    */   
/*  11:    */   public EventExecutorGroup parent()
/*  12:    */   {
/*  13: 35 */     return null;
/*  14:    */   }
/*  15:    */   
/*  16:    */   public boolean inEventLoop()
/*  17:    */   {
/*  18: 40 */     return true;
/*  19:    */   }
/*  20:    */   
/*  21:    */   public boolean inEventLoop(Thread thread)
/*  22:    */   {
/*  23: 45 */     return true;
/*  24:    */   }
/*  25:    */   
/*  26:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/*  27:    */   {
/*  28: 50 */     return terminationFuture();
/*  29:    */   }
/*  30:    */   
/*  31:    */   public Future<?> terminationFuture()
/*  32:    */   {
/*  33: 55 */     return this.terminationFuture;
/*  34:    */   }
/*  35:    */   
/*  36:    */   @Deprecated
/*  37:    */   public void shutdown() {}
/*  38:    */   
/*  39:    */   public boolean isShuttingDown()
/*  40:    */   {
/*  41: 64 */     return false;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public boolean isShutdown()
/*  45:    */   {
/*  46: 69 */     return false;
/*  47:    */   }
/*  48:    */   
/*  49:    */   public boolean isTerminated()
/*  50:    */   {
/*  51: 74 */     return false;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public boolean awaitTermination(long timeout, TimeUnit unit)
/*  55:    */   {
/*  56: 79 */     return false;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public void execute(Runnable command)
/*  60:    */   {
/*  61: 84 */     if (command == null) {
/*  62: 85 */       throw new NullPointerException("command");
/*  63:    */     }
/*  64: 87 */     command.run();
/*  65:    */   }
/*  66:    */   
/*  67:    */   public <V> Promise<V> newPromise()
/*  68:    */   {
/*  69: 92 */     return new ImmediatePromise(this);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public <V> ProgressivePromise<V> newProgressivePromise()
/*  73:    */   {
/*  74: 97 */     return new ImmediateProgressivePromise(this);
/*  75:    */   }
/*  76:    */   
/*  77:    */   static class ImmediatePromise<V>
/*  78:    */     extends DefaultPromise<V>
/*  79:    */   {
/*  80:    */     ImmediatePromise(EventExecutor executor)
/*  81:    */     {
/*  82:102 */       super();
/*  83:    */     }
/*  84:    */     
/*  85:    */     protected void checkDeadLock() {}
/*  86:    */   }
/*  87:    */   
/*  88:    */   static class ImmediateProgressivePromise<V>
/*  89:    */     extends DefaultProgressivePromise<V>
/*  90:    */   {
/*  91:    */     ImmediateProgressivePromise(EventExecutor executor)
/*  92:    */     {
/*  93:113 */       super();
/*  94:    */     }
/*  95:    */     
/*  96:    */     protected void checkDeadLock() {}
/*  97:    */   }
/*  98:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.ImmediateEventExecutor
 * JD-Core Version:    0.7.0.1
 */