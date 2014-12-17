/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.SingleThreadEventExecutor;
/*  4:   */ import java.util.concurrent.ThreadFactory;
/*  5:   */ 
/*  6:   */ public abstract class SingleThreadEventLoop
/*  7:   */   extends SingleThreadEventExecutor
/*  8:   */   implements EventLoop
/*  9:   */ {
/* 10:   */   protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp)
/* 11:   */   {
/* 12:33 */     super(parent, threadFactory, addTaskWakesUp);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public EventLoopGroup parent()
/* 16:   */   {
/* 17:38 */     return (EventLoopGroup)super.parent();
/* 18:   */   }
/* 19:   */   
/* 20:   */   public EventLoop next()
/* 21:   */   {
/* 22:43 */     return (EventLoop)super.next();
/* 23:   */   }
/* 24:   */   
/* 25:   */   public ChannelFuture register(Channel channel)
/* 26:   */   {
/* 27:48 */     return register(channel, new DefaultChannelPromise(channel, this));
/* 28:   */   }
/* 29:   */   
/* 30:   */   public ChannelFuture register(Channel channel, ChannelPromise promise)
/* 31:   */   {
/* 32:53 */     if (channel == null) {
/* 33:54 */       throw new NullPointerException("channel");
/* 34:   */     }
/* 35:56 */     if (promise == null) {
/* 36:57 */       throw new NullPointerException("promise");
/* 37:   */     }
/* 38:60 */     channel.unsafe().register(this, promise);
/* 39:61 */     return promise;
/* 40:   */   }
/* 41:   */   
/* 42:   */   protected boolean wakesUpForTask(Runnable task)
/* 43:   */   {
/* 44:66 */     return !(task instanceof NonWakeupRunnable);
/* 45:   */   }
/* 46:   */   
/* 47:   */   static abstract interface NonWakeupRunnable
/* 48:   */     extends Runnable
/* 49:   */   {}
/* 50:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.SingleThreadEventLoop
 * JD-Core Version:    0.7.0.1
 */