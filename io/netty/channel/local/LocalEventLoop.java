/*  1:   */ package io.netty.channel.local;
/*  2:   */ 
/*  3:   */ import io.netty.channel.SingleThreadEventLoop;
/*  4:   */ import java.util.concurrent.ThreadFactory;
/*  5:   */ 
/*  6:   */ final class LocalEventLoop
/*  7:   */   extends SingleThreadEventLoop
/*  8:   */ {
/*  9:   */   LocalEventLoop(LocalEventLoopGroup parent, ThreadFactory threadFactory)
/* 10:   */   {
/* 11:25 */     super(parent, threadFactory, true);
/* 12:   */   }
/* 13:   */   
/* 14:   */   protected void run()
/* 15:   */   {
/* 16:   */     for (;;)
/* 17:   */     {
/* 18:31 */       Runnable task = takeTask();
/* 19:32 */       if (task != null)
/* 20:   */       {
/* 21:33 */         task.run();
/* 22:34 */         updateLastExecutionTime();
/* 23:   */       }
/* 24:37 */       if (confirmShutdown()) {
/* 25:   */         break;
/* 26:   */       }
/* 27:   */     }
/* 28:   */   }
/* 29:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.local.LocalEventLoop
 * JD-Core Version:    0.7.0.1
 */