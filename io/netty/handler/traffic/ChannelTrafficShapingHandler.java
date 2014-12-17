/*  1:   */ package io.netty.handler.traffic;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelHandlerContext;
/*  4:   */ 
/*  5:   */ public class ChannelTrafficShapingHandler
/*  6:   */   extends AbstractTrafficShapingHandler
/*  7:   */ {
/*  8:   */   public ChannelTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval)
/*  9:   */   {
/* 10:60 */     super(writeLimit, readLimit, checkInterval);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public ChannelTrafficShapingHandler(long writeLimit, long readLimit)
/* 14:   */   {
/* 15:73 */     super(writeLimit, readLimit);
/* 16:   */   }
/* 17:   */   
/* 18:   */   public ChannelTrafficShapingHandler(long checkInterval)
/* 19:   */   {
/* 20:84 */     super(checkInterval);
/* 21:   */   }
/* 22:   */   
/* 23:   */   public void handlerAdded(ChannelHandlerContext ctx)
/* 24:   */     throws Exception
/* 25:   */   {
/* 26:89 */     TrafficCounter trafficCounter = new TrafficCounter(this, ctx.executor(), "ChannelTC" + ctx.channel().hashCode(), this.checkInterval);
/* 27:   */     
/* 28:91 */     setTrafficCounter(trafficCounter);
/* 29:92 */     trafficCounter.start();
/* 30:   */   }
/* 31:   */   
/* 32:   */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 33:   */     throws Exception
/* 34:   */   {
/* 35:97 */     if (this.trafficCounter != null) {
/* 36:98 */       this.trafficCounter.stop();
/* 37:   */     }
/* 38:   */   }
/* 39:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.traffic.ChannelTrafficShapingHandler
 * JD-Core Version:    0.7.0.1
 */