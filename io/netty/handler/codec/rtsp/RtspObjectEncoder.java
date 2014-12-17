/*  1:   */ package io.netty.handler.codec.rtsp;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  4:   */ import io.netty.handler.codec.http.FullHttpMessage;
/*  5:   */ import io.netty.handler.codec.http.HttpMessage;
/*  6:   */ import io.netty.handler.codec.http.HttpObjectEncoder;
/*  7:   */ 
/*  8:   */ @ChannelHandler.Sharable
/*  9:   */ public abstract class RtspObjectEncoder<H extends HttpMessage>
/* 10:   */   extends HttpObjectEncoder<H>
/* 11:   */ {
/* 12:   */   public boolean acceptOutboundMessage(Object msg)
/* 13:   */     throws Exception
/* 14:   */   {
/* 15:39 */     return msg instanceof FullHttpMessage;
/* 16:   */   }
/* 17:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.rtsp.RtspObjectEncoder
 * JD-Core Version:    0.7.0.1
 */