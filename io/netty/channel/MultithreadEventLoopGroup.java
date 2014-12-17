/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.DefaultThreadFactory;
/*  4:   */ import io.netty.util.concurrent.MultithreadEventExecutorGroup;
/*  5:   */ import io.netty.util.internal.SystemPropertyUtil;
/*  6:   */ import io.netty.util.internal.logging.InternalLogger;
/*  7:   */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  8:   */ import java.util.concurrent.ThreadFactory;
/*  9:   */ 
/* 10:   */ public abstract class MultithreadEventLoopGroup
/* 11:   */   extends MultithreadEventExecutorGroup
/* 12:   */   implements EventLoopGroup
/* 13:   */ {
/* 14:32 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(MultithreadEventLoopGroup.class);
/* 15:37 */   private static final int DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));
/* 16:   */   
/* 17:   */   static
/* 18:   */   {
/* 19:40 */     if (logger.isDebugEnabled()) {
/* 20:41 */       logger.debug("-Dio.netty.eventLoopThreads: {}", Integer.valueOf(DEFAULT_EVENT_LOOP_THREADS));
/* 21:   */     }
/* 22:   */   }
/* 23:   */   
/* 24:   */   protected MultithreadEventLoopGroup(int nThreads, ThreadFactory threadFactory, Object... args)
/* 25:   */   {
/* 26:49 */     super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, threadFactory, args);
/* 27:   */   }
/* 28:   */   
/* 29:   */   protected ThreadFactory newDefaultThreadFactory()
/* 30:   */   {
/* 31:54 */     return new DefaultThreadFactory(getClass(), 10);
/* 32:   */   }
/* 33:   */   
/* 34:   */   public EventLoop next()
/* 35:   */   {
/* 36:59 */     return (EventLoop)super.next();
/* 37:   */   }
/* 38:   */   
/* 39:   */   public ChannelFuture register(Channel channel)
/* 40:   */   {
/* 41:64 */     return next().register(channel);
/* 42:   */   }
/* 43:   */   
/* 44:   */   public ChannelFuture register(Channel channel, ChannelPromise promise)
/* 45:   */   {
/* 46:69 */     return next().register(channel, promise);
/* 47:   */   }
/* 48:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.MultithreadEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */