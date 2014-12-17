/*  1:   */ package io.netty.handler.codec.http.websocketx;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.Channel;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.MessageToMessageDecoder;
/*  7:   */ import java.util.List;
/*  8:   */ 
/*  9:   */ abstract class WebSocketProtocolHandler
/* 10:   */   extends MessageToMessageDecoder<WebSocketFrame>
/* 11:   */ {
/* 12:   */   protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out)
/* 13:   */     throws Exception
/* 14:   */   {
/* 15:27 */     if ((frame instanceof PingWebSocketFrame))
/* 16:   */     {
/* 17:28 */       frame.content().retain();
/* 18:29 */       ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content()));
/* 19:30 */       return;
/* 20:   */     }
/* 21:32 */     if ((frame instanceof PongWebSocketFrame)) {
/* 22:34 */       return;
/* 23:   */     }
/* 24:37 */     out.add(frame.retain());
/* 25:   */   }
/* 26:   */   
/* 27:   */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 28:   */     throws Exception
/* 29:   */   {
/* 30:42 */     ctx.close();
/* 31:   */   }
/* 32:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketProtocolHandler
 * JD-Core Version:    0.7.0.1
 */