/*  1:   */ package io.netty.channel.nio;
/*  2:   */ 
/*  3:   */ import io.netty.channel.MultithreadEventLoopGroup;
/*  4:   */ import io.netty.util.concurrent.EventExecutor;
/*  5:   */ import java.nio.channels.spi.SelectorProvider;
/*  6:   */ import java.util.concurrent.ThreadFactory;
/*  7:   */ 
/*  8:   */ public class NioEventLoopGroup
/*  9:   */   extends MultithreadEventLoopGroup
/* 10:   */ {
/* 11:   */   public NioEventLoopGroup()
/* 12:   */   {
/* 13:36 */     this(0);
/* 14:   */   }
/* 15:   */   
/* 16:   */   public NioEventLoopGroup(int nThreads)
/* 17:   */   {
/* 18:44 */     this(nThreads, null);
/* 19:   */   }
/* 20:   */   
/* 21:   */   public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory)
/* 22:   */   {
/* 23:52 */     this(nThreads, threadFactory, SelectorProvider.provider());
/* 24:   */   }
/* 25:   */   
/* 26:   */   public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory, SelectorProvider selectorProvider)
/* 27:   */   {
/* 28:61 */     super(nThreads, threadFactory, new Object[] { selectorProvider });
/* 29:   */   }
/* 30:   */   
/* 31:   */   public void setIoRatio(int ioRatio)
/* 32:   */   {
/* 33:69 */     for (EventExecutor e : children()) {
/* 34:70 */       ((NioEventLoop)e).setIoRatio(ioRatio);
/* 35:   */     }
/* 36:   */   }
/* 37:   */   
/* 38:   */   public void rebuildSelectors()
/* 39:   */   {
/* 40:79 */     for (EventExecutor e : children()) {
/* 41:80 */       ((NioEventLoop)e).rebuildSelector();
/* 42:   */     }
/* 43:   */   }
/* 44:   */   
/* 45:   */   protected EventExecutor newChild(ThreadFactory threadFactory, Object... args)
/* 46:   */     throws Exception
/* 47:   */   {
/* 48:87 */     return new NioEventLoop(this, threadFactory, (SelectorProvider)args[0]);
/* 49:   */   }
/* 50:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.nio.NioEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */