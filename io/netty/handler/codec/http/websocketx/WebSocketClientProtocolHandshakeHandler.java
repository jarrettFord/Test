/*  1:   */ package io.netty.handler.codec.http.websocketx;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelFuture;
/*  4:   */ import io.netty.channel.ChannelFutureListener;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*  7:   */ import io.netty.channel.ChannelPipeline;
/*  8:   */ import io.netty.handler.codec.http.FullHttpResponse;
/*  9:   */ 
/* 10:   */ class WebSocketClientProtocolHandshakeHandler
/* 11:   */   extends ChannelInboundHandlerAdapter
/* 12:   */ {
/* 13:   */   private final WebSocketClientHandshaker handshaker;
/* 14:   */   
/* 15:   */   WebSocketClientProtocolHandshakeHandler(WebSocketClientHandshaker handshaker)
/* 16:   */   {
/* 17:28 */     this.handshaker = handshaker;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public void channelActive(final ChannelHandlerContext ctx)
/* 21:   */     throws Exception
/* 22:   */   {
/* 23:33 */     super.channelActive(ctx);
/* 24:34 */     this.handshaker.handshake(ctx.channel()).addListener(new ChannelFutureListener()
/* 25:   */     {
/* 26:   */       public void operationComplete(ChannelFuture future)
/* 27:   */         throws Exception
/* 28:   */       {
/* 29:37 */         if (!future.isSuccess()) {
/* 30:38 */           ctx.fireExceptionCaught(future.cause());
/* 31:   */         } else {
/* 32:40 */           ctx.fireUserEventTriggered(WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_ISSUED);
/* 33:   */         }
/* 34:   */       }
/* 35:   */     });
/* 36:   */   }
/* 37:   */   
/* 38:   */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 39:   */     throws Exception
/* 40:   */   {
/* 41:49 */     if (!(msg instanceof FullHttpResponse))
/* 42:   */     {
/* 43:50 */       ctx.fireChannelRead(msg);
/* 44:51 */       return;
/* 45:   */     }
/* 46:54 */     if (!this.handshaker.isHandshakeComplete())
/* 47:   */     {
/* 48:55 */       this.handshaker.finishHandshake(ctx.channel(), (FullHttpResponse)msg);
/* 49:56 */       ctx.fireUserEventTriggered(WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE);
/* 50:   */       
/* 51:58 */       ctx.pipeline().remove(this);
/* 52:59 */       return;
/* 53:   */     }
/* 54:61 */     throw new IllegalStateException("WebSocketClientHandshaker should have been non finished yet");
/* 55:   */   }
/* 56:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandshakeHandler
 * JD-Core Version:    0.7.0.1
 */