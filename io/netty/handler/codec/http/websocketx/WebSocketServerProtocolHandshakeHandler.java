/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelFuture;
/*   5:    */ import io.netty.channel.ChannelFutureListener;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*   8:    */ import io.netty.channel.ChannelPipeline;
/*   9:    */ import io.netty.handler.codec.http.DefaultFullHttpResponse;
/*  10:    */ import io.netty.handler.codec.http.FullHttpRequest;
/*  11:    */ import io.netty.handler.codec.http.HttpHeaders;
/*  12:    */ import io.netty.handler.codec.http.HttpMethod;
/*  13:    */ import io.netty.handler.codec.http.HttpRequest;
/*  14:    */ import io.netty.handler.codec.http.HttpResponse;
/*  15:    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*  16:    */ import io.netty.handler.codec.http.HttpVersion;
/*  17:    */ import io.netty.handler.ssl.SslHandler;
/*  18:    */ 
/*  19:    */ class WebSocketServerProtocolHandshakeHandler
/*  20:    */   extends ChannelInboundHandlerAdapter
/*  21:    */ {
/*  22:    */   private final String websocketPath;
/*  23:    */   private final String subprotocols;
/*  24:    */   private final boolean allowExtensions;
/*  25:    */   private final int maxFramePayloadSize;
/*  26:    */   
/*  27:    */   WebSocketServerProtocolHandshakeHandler(String websocketPath, String subprotocols, boolean allowExtensions, int maxFrameSize)
/*  28:    */   {
/*  29: 48 */     this.websocketPath = websocketPath;
/*  30: 49 */     this.subprotocols = subprotocols;
/*  31: 50 */     this.allowExtensions = allowExtensions;
/*  32: 51 */     this.maxFramePayloadSize = maxFrameSize;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public void channelRead(final ChannelHandlerContext ctx, Object msg)
/*  36:    */     throws Exception
/*  37:    */   {
/*  38: 56 */     FullHttpRequest req = (FullHttpRequest)msg;
/*  39:    */     try
/*  40:    */     {
/*  41: 58 */       if (req.getMethod() != HttpMethod.GET)
/*  42:    */       {
/*  43: 59 */         sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
/*  44:    */       }
/*  45:    */       else
/*  46:    */       {
/*  47: 63 */         WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(ctx.pipeline(), req, this.websocketPath), this.subprotocols, this.allowExtensions, this.maxFramePayloadSize);
/*  48:    */         
/*  49:    */ 
/*  50: 66 */         WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
/*  51: 67 */         if (handshaker == null)
/*  52:    */         {
/*  53: 68 */           WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
/*  54:    */         }
/*  55:    */         else
/*  56:    */         {
/*  57: 70 */           ChannelFuture handshakeFuture = handshaker.handshake(ctx.channel(), req);
/*  58: 71 */           handshakeFuture.addListener(new ChannelFutureListener()
/*  59:    */           {
/*  60:    */             public void operationComplete(ChannelFuture future)
/*  61:    */               throws Exception
/*  62:    */             {
/*  63: 74 */               if (!future.isSuccess()) {
/*  64: 75 */                 ctx.fireExceptionCaught(future.cause());
/*  65:    */               } else {
/*  66: 77 */                 ctx.fireUserEventTriggered(WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE);
/*  67:    */               }
/*  68:    */             }
/*  69: 81 */           });
/*  70: 82 */           WebSocketServerProtocolHandler.setHandshaker(ctx, handshaker);
/*  71: 83 */           ctx.pipeline().replace(this, "WS403Responder", WebSocketServerProtocolHandler.forbiddenHttpRequestResponder());
/*  72:    */         }
/*  73:    */       }
/*  74:    */     }
/*  75:    */     finally
/*  76:    */     {
/*  77: 87 */       req.release();
/*  78:    */     }
/*  79:    */   }
/*  80:    */   
/*  81:    */   private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res)
/*  82:    */   {
/*  83: 92 */     ChannelFuture f = ctx.channel().writeAndFlush(res);
/*  84: 93 */     if ((!HttpHeaders.isKeepAlive(req)) || (res.getStatus().code() != 200)) {
/*  85: 94 */       f.addListener(ChannelFutureListener.CLOSE);
/*  86:    */     }
/*  87:    */   }
/*  88:    */   
/*  89:    */   private static String getWebSocketLocation(ChannelPipeline cp, HttpRequest req, String path)
/*  90:    */   {
/*  91: 99 */     String protocol = "ws";
/*  92:100 */     if (cp.get(SslHandler.class) != null) {
/*  93:102 */       protocol = "wss";
/*  94:    */     }
/*  95:104 */     return protocol + "://" + req.headers().get("Host") + path;
/*  96:    */   }
/*  97:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandshakeHandler
 * JD-Core Version:    0.7.0.1
 */