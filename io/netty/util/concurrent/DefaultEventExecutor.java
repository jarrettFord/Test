/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.ThreadFactory;
/*  4:   */ 
/*  5:   */ final class DefaultEventExecutor
/*  6:   */   extends SingleThreadEventExecutor
/*  7:   */ {
/*  8:   */   DefaultEventExecutor(DefaultEventExecutorGroup parent, ThreadFactory threadFactory)
/*  9:   */   {
/* 10:28 */     super(parent, threadFactory, true);
/* 11:   */   }
/* 12:   */   
/* 13:   */   protected void run()
/* 14:   */   {
/* 15:   */     for (;;)
/* 16:   */     {
/* 17:34 */       Runnable task = takeTask();
/* 18:35 */       if (task != null)
/* 19:   */       {
/* 20:36 */         task.run();
/* 21:37 */         updateLastExecutionTime();
/* 22:   */       }
/* 23:40 */       if (confirmShutdown()) {
/* 24:   */         break;
/* 25:   */       }
/* 26:   */     }
/* 27:   */   }
/* 28:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.DefaultEventExecutor
 * JD-Core Version:    0.7.0.1
 */