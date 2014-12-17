/*   1:    */ package io.netty.util.internal.chmv8;
/*   2:    */ 
/*   3:    */ public class ForkJoinWorkerThread
/*   4:    */   extends Thread
/*   5:    */ {
/*   6:    */   final ForkJoinPool pool;
/*   7:    */   final ForkJoinPool.WorkQueue workQueue;
/*   8:    */   
/*   9:    */   protected ForkJoinWorkerThread(ForkJoinPool pool)
/*  10:    */   {
/*  11: 66 */     super("aForkJoinWorkerThread");
/*  12: 67 */     this.pool = pool;
/*  13: 68 */     this.workQueue = pool.registerWorker(this);
/*  14:    */   }
/*  15:    */   
/*  16:    */   public ForkJoinPool getPool()
/*  17:    */   {
/*  18: 77 */     return this.pool;
/*  19:    */   }
/*  20:    */   
/*  21:    */   public int getPoolIndex()
/*  22:    */   {
/*  23: 91 */     return this.workQueue.poolIndex >>> 1;
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected void onStart() {}
/*  27:    */   
/*  28:    */   protected void onTermination(Throwable exception) {}
/*  29:    */   
/*  30:    */   public void run()
/*  31:    */   {
/*  32:123 */     Throwable exception = null;
/*  33:    */     try
/*  34:    */     {
/*  35:125 */       onStart();
/*  36:126 */       this.pool.runWorker(this.workQueue);
/*  37:    */     }
/*  38:    */     catch (Throwable ex)
/*  39:    */     {
/*  40:128 */       exception = ex;
/*  41:    */     }
/*  42:    */     finally
/*  43:    */     {
/*  44:    */       try
/*  45:    */       {
/*  46:131 */         onTermination(exception);
/*  47:    */       }
/*  48:    */       catch (Throwable ex)
/*  49:    */       {
/*  50:133 */         if (exception == null) {
/*  51:134 */           exception = ex;
/*  52:    */         }
/*  53:    */       }
/*  54:    */       finally
/*  55:    */       {
/*  56:136 */         this.pool.deregisterWorker(this, exception);
/*  57:    */       }
/*  58:    */     }
/*  59:    */   }
/*  60:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.chmv8.ForkJoinWorkerThread
 * JD-Core Version:    0.7.0.1
 */