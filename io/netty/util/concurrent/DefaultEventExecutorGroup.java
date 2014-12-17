/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.ThreadFactory;
/*  4:   */ 
/*  5:   */ public class DefaultEventExecutorGroup
/*  6:   */   extends MultithreadEventExecutorGroup
/*  7:   */ {
/*  8:   */   public DefaultEventExecutorGroup(int nThreads)
/*  9:   */   {
/* 10:30 */     this(nThreads, null);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public DefaultEventExecutorGroup(int nThreads, ThreadFactory threadFactory)
/* 14:   */   {
/* 15:40 */     super(nThreads, threadFactory, new Object[0]);
/* 16:   */   }
/* 17:   */   
/* 18:   */   protected EventExecutor newChild(ThreadFactory threadFactory, Object... args)
/* 19:   */     throws Exception
/* 20:   */   {
/* 21:46 */     return new DefaultEventExecutor(this, threadFactory);
/* 22:   */   }
/* 23:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.DefaultEventExecutorGroup
 * JD-Core Version:    0.7.0.1
 */