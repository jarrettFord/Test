/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.Unpooled;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.ChannelFuture;
/*   6:    */ import io.netty.channel.ChannelFutureListener;
/*   7:    */ import io.netty.channel.ChannelHandler;
/*   8:    */ import io.netty.channel.ChannelHandlerContext;
/*   9:    */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*  10:    */ import io.netty.channel.ChannelPipeline;
/*  11:    */ import io.netty.handler.codec.http.DefaultFullHttpResponse;
/*  12:    */ import io.netty.handler.codec.http.FullHttpRequest;
/*  13:    */ import io.netty.handler.codec.http.FullHttpResponse;
/*  14:    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*  15:    */ import io.netty.handler.codec.http.HttpVersion;
/*  16:    */ import io.netty.util.Attribute;
/*  17:    */ import io.netty.util.AttributeKey;
/*  18:    */ import java.util.List;
/*  19:    */ 
/*  20:    */ public class WebSocketServerProtocolHandler
/*  21:    */   extends WebSocketProtocolHandler
/*  22:    */ {
/*  23:    */   public static enum ServerHandshakeStateEvent
/*  24:    */   {
/*  25: 60 */     HANDSHAKE_COMPLETE;
/*  26:    */     
/*  27:    */     private ServerHandshakeStateEvent() {}
/*  28:    */   }
/*  29:    */   
/*  30: 63 */   private static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER_ATTR_KEY = AttributeKey.valueOf(WebSocketServerHandshaker.class.getName() + ".HANDSHAKER");
/*  31:    */   private final String websocketPath;
/*  32:    */   private final String subprotocols;
/*  33:    */   private final boolean allowExtensions;
/*  34:    */   private final int maxFramePayloadLength;
/*  35:    */   
/*  36:    */   public WebSocketServerProtocolHandler(String websocketPath)
/*  37:    */   {
/*  38: 72 */     this(websocketPath, null, false);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public WebSocketServerProtocolHandler(String websocketPath, String subprotocols)
/*  42:    */   {
/*  43: 76 */     this(websocketPath, subprotocols, false);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public WebSocketServerProtocolHandler(String websocketPath, String subprotocols, boolean allowExtensions)
/*  47:    */   {
/*  48: 80 */     this(websocketPath, subprotocols, allowExtensions, 65536);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public WebSocketServerProtocolHandler(String websocketPath, String subprotocols, boolean allowExtensions, int maxFrameSize)
/*  52:    */   {
/*  53: 85 */     this.websocketPath = websocketPath;
/*  54: 86 */     this.subprotocols = subprotocols;
/*  55: 87 */     this.allowExtensions = allowExtensions;
/*  56: 88 */     this.maxFramePayloadLength = maxFrameSize;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  60:    */   {
/*  61: 93 */     ChannelPipeline cp = ctx.pipeline();
/*  62: 94 */     if (cp.get(WebSocketServerProtocolHandshakeHandler.class) == null) {
/*  63: 96 */       ctx.pipeline().addBefore(ctx.name(), WebSocketServerProtocolHandshakeHandler.class.getName(), new WebSocketServerProtocolHandshakeHandler(this.websocketPath, this.subprotocols, this.allowExtensions, this.maxFramePayloadLength));
/*  64:    */     }
/*  65:    */   }
/*  66:    */   
/*  67:    */   protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out)
/*  68:    */     throws Exception
/*  69:    */   {
/*  70:104 */     if ((frame instanceof CloseWebSocketFrame))
/*  71:    */     {
/*  72:105 */       WebSocketServerHandshaker handshaker = getHandshaker(ctx);
/*  73:106 */       if (handshaker != null)
/*  74:    */       {
/*  75:107 */         frame.retain();
/*  76:108 */         handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame);
/*  77:    */       }
/*  78:    */       else
/*  79:    */       {
/*  80:110 */         ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
/*  81:    */       }
/*  82:112 */       return;
/*  83:    */     }
/*  84:114 */     super.decode(ctx, frame, out);
/*  85:    */   }
/*  86:    */   
/*  87:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/*  88:    */     throws Exception
/*  89:    */   {
/*  90:119 */     if ((cause instanceof WebSocketHandshakeException))
/*  91:    */     {
/*  92:120 */       FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(cause.getMessage().getBytes()));
/*  93:    */       
/*  94:122 */       ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
/*  95:    */     }
/*  96:    */     else
/*  97:    */     {
/*  98:124 */       ctx.close();
/*  99:    */     }
/* 100:    */   }
/* 101:    */   
/* 102:    */   static WebSocketServerHandshaker getHandshaker(ChannelHandlerContext ctx)
/* 103:    */   {
/* 104:129 */     return (WebSocketServerHandshaker)ctx.attr(HANDSHAKER_ATTR_KEY).get();
/* 105:    */   }
/* 106:    */   
/* 107:    */   static void setHandshaker(ChannelHandlerContext ctx, WebSocketServerHandshaker handshaker)
/* 108:    */   {
/* 109:133 */     ctx.attr(HANDSHAKER_ATTR_KEY).set(handshaker);
/* 110:    */   }
/* 111:    */   
/* 112:    */   static ChannelHandler forbiddenHttpRequestResponder()
/* 113:    */   {
/* 114:137 */     new ChannelInboundHandlerAdapter()
/* 115:    */     {
/* 116:    */       public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 117:    */         throws Exception
/* 118:    */       {
/* 119:140 */         if ((msg instanceof FullHttpRequest))
/* 120:    */         {
/* 121:141 */           ((FullHttpRequest)msg).release();
/* 122:142 */           FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
/* 123:    */           
/* 124:144 */           ctx.channel().writeAndFlush(response);
/* 125:    */         }
/* 126:    */         else
/* 127:    */         {
/* 128:146 */           ctx.fireChannelRead(msg);
/* 129:    */         }
/* 130:    */       }
/* 131:    */     };
/* 132:    */   }
/* 133:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
 * JD-Core Version:    0.7.0.1
 */