/*  1:   */ package io.netty.channel.epoll;
/*  2:   */ 
/*  3:   */ import io.netty.channel.MultithreadEventLoopGroup;
/*  4:   */ import io.netty.util.concurrent.EventExecutor;
/*  5:   */ import java.util.concurrent.ThreadFactory;
/*  6:   */ 
/*  7:   */ public final class EpollEventLoopGroup
/*  8:   */   extends MultithreadEventLoopGroup
/*  9:   */ {
/* 10:   */   public EpollEventLoopGroup()
/* 11:   */   {
/* 12:34 */     this(0);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public EpollEventLoopGroup(int nThreads)
/* 16:   */   {
/* 17:41 */     this(nThreads, null);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory)
/* 21:   */   {
/* 22:48 */     this(nThreads, threadFactory, 128);
/* 23:   */   }
/* 24:   */   
/* 25:   */   public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory, int maxEventsAtOnce)
/* 26:   */   {
/* 27:56 */     super(nThreads, threadFactory, new Object[] { Integer.valueOf(maxEventsAtOnce) });
/* 28:   */   }
/* 29:   */   
/* 30:   */   public void setIoRatio(int ioRatio)
/* 31:   */   {
/* 32:64 */     for (EventExecutor e : children()) {
/* 33:65 */       ((EpollEventLoop)e).setIoRatio(ioRatio);
/* 34:   */     }
/* 35:   */   }
/* 36:   */   
/* 37:   */   protected EventExecutor newChild(ThreadFactory threadFactory, Object... args)
/* 38:   */     throws Exception
/* 39:   */   {
/* 40:71 */     return new EpollEventLoop(this, threadFactory, ((Integer)args[0]).intValue());
/* 41:   */   }
/* 42:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.epoll.EpollEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */