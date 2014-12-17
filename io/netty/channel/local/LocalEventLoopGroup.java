/*  1:   */ package io.netty.channel.local;
/*  2:   */ 
/*  3:   */ import io.netty.channel.MultithreadEventLoopGroup;
/*  4:   */ import io.netty.util.concurrent.EventExecutor;
/*  5:   */ import java.util.concurrent.ThreadFactory;
/*  6:   */ 
/*  7:   */ public class LocalEventLoopGroup
/*  8:   */   extends MultithreadEventLoopGroup
/*  9:   */ {
/* 10:   */   public LocalEventLoopGroup()
/* 11:   */   {
/* 12:32 */     this(0);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public LocalEventLoopGroup(int nThreads)
/* 16:   */   {
/* 17:41 */     this(nThreads, null);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public LocalEventLoopGroup(int nThreads, ThreadFactory threadFactory)
/* 21:   */   {
/* 22:51 */     super(nThreads, threadFactory, new Object[0]);
/* 23:   */   }
/* 24:   */   
/* 25:   */   protected EventExecutor newChild(ThreadFactory threadFactory, Object... args)
/* 26:   */     throws Exception
/* 27:   */   {
/* 28:57 */     return new LocalEventLoop(this, threadFactory);
/* 29:   */   }
/* 30:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.local.LocalEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */