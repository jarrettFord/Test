/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.logging.InternalLogger;
/*  4:   */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  5:   */ 
/*  6:   */ @ChannelHandler.Sharable
/*  7:   */ public abstract class ChannelInitializer<C extends Channel>
/*  8:   */   extends ChannelInboundHandlerAdapter
/*  9:   */ {
/* 10:52 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChannelInitializer.class);
/* 11:   */   
/* 12:   */   protected abstract void initChannel(C paramC)
/* 13:   */     throws Exception;
/* 14:   */   
/* 15:   */   public final void channelRegistered(ChannelHandlerContext ctx)
/* 16:   */     throws Exception
/* 17:   */   {
/* 18:66 */     ChannelPipeline pipeline = ctx.pipeline();
/* 19:67 */     boolean success = false;
/* 20:   */     try
/* 21:   */     {
/* 22:69 */       initChannel(ctx.channel());
/* 23:70 */       pipeline.remove(this);
/* 24:71 */       ctx.fireChannelRegistered();
/* 25:72 */       success = true;
/* 26:   */     }
/* 27:   */     catch (Throwable t)
/* 28:   */     {
/* 29:74 */       logger.warn("Failed to initialize a channel. Closing: " + ctx.channel(), t);
/* 30:   */     }
/* 31:   */     finally
/* 32:   */     {
/* 33:76 */       if (pipeline.context(this) != null) {
/* 34:77 */         pipeline.remove(this);
/* 35:   */       }
/* 36:79 */       if (!success) {
/* 37:80 */         ctx.close();
/* 38:   */       }
/* 39:   */     }
/* 40:   */   }
/* 41:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.ChannelInitializer
 * JD-Core Version:    0.7.0.1
 */