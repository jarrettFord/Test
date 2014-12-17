/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelFuture;
/*   5:    */ import io.netty.channel.ChannelPromise;
/*   6:    */ import io.netty.handler.codec.http.DefaultHttpResponse;
/*   7:    */ import io.netty.handler.codec.http.HttpHeaders;
/*   8:    */ import io.netty.handler.codec.http.HttpRequest;
/*   9:    */ import io.netty.handler.codec.http.HttpResponse;
/*  10:    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*  11:    */ import io.netty.handler.codec.http.HttpVersion;
/*  12:    */ 
/*  13:    */ public class WebSocketServerHandshakerFactory
/*  14:    */ {
/*  15:    */   private final String webSocketURL;
/*  16:    */   private final String subprotocols;
/*  17:    */   private final boolean allowExtensions;
/*  18:    */   private final int maxFramePayloadLength;
/*  19:    */   
/*  20:    */   public WebSocketServerHandshakerFactory(String webSocketURL, String subprotocols, boolean allowExtensions)
/*  21:    */   {
/*  22: 55 */     this(webSocketURL, subprotocols, allowExtensions, 65536);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public WebSocketServerHandshakerFactory(String webSocketURL, String subprotocols, boolean allowExtensions, int maxFramePayloadLength)
/*  26:    */   {
/*  27: 75 */     this.webSocketURL = webSocketURL;
/*  28: 76 */     this.subprotocols = subprotocols;
/*  29: 77 */     this.allowExtensions = allowExtensions;
/*  30: 78 */     this.maxFramePayloadLength = maxFramePayloadLength;
/*  31:    */   }
/*  32:    */   
/*  33:    */   public WebSocketServerHandshaker newHandshaker(HttpRequest req)
/*  34:    */   {
/*  35: 89 */     String version = req.headers().get("Sec-WebSocket-Version");
/*  36: 90 */     if (version != null)
/*  37:    */     {
/*  38: 91 */       if (version.equals(WebSocketVersion.V13.toHttpHeaderValue())) {
/*  39: 93 */         return new WebSocketServerHandshaker13(this.webSocketURL, this.subprotocols, this.allowExtensions, this.maxFramePayloadLength);
/*  40:    */       }
/*  41: 95 */       if (version.equals(WebSocketVersion.V08.toHttpHeaderValue())) {
/*  42: 97 */         return new WebSocketServerHandshaker08(this.webSocketURL, this.subprotocols, this.allowExtensions, this.maxFramePayloadLength);
/*  43:    */       }
/*  44: 99 */       if (version.equals(WebSocketVersion.V07.toHttpHeaderValue())) {
/*  45:101 */         return new WebSocketServerHandshaker07(this.webSocketURL, this.subprotocols, this.allowExtensions, this.maxFramePayloadLength);
/*  46:    */       }
/*  47:104 */       return null;
/*  48:    */     }
/*  49:108 */     return new WebSocketServerHandshaker00(this.webSocketURL, this.subprotocols, this.maxFramePayloadLength);
/*  50:    */   }
/*  51:    */   
/*  52:    */   @Deprecated
/*  53:    */   public static void sendUnsupportedWebSocketVersionResponse(Channel channel)
/*  54:    */   {
/*  55:117 */     sendUnsupportedVersionResponse(channel);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public static ChannelFuture sendUnsupportedVersionResponse(Channel channel)
/*  59:    */   {
/*  60:124 */     return sendUnsupportedVersionResponse(channel, channel.newPromise());
/*  61:    */   }
/*  62:    */   
/*  63:    */   public static ChannelFuture sendUnsupportedVersionResponse(Channel channel, ChannelPromise promise)
/*  64:    */   {
/*  65:131 */     HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UPGRADE_REQUIRED);
/*  66:    */     
/*  67:    */ 
/*  68:134 */     res.headers().set("Sec-WebSocket-Version", WebSocketVersion.V13.toHttpHeaderValue());
/*  69:135 */     return channel.write(res, promise);
/*  70:    */   }
/*  71:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory
 * JD-Core Version:    0.7.0.1
 */