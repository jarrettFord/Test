/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.handler.codec.http.DefaultFullHttpResponse;
/*   4:    */ import io.netty.handler.codec.http.FullHttpRequest;
/*   5:    */ import io.netty.handler.codec.http.FullHttpResponse;
/*   6:    */ import io.netty.handler.codec.http.HttpHeaders;
/*   7:    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*   8:    */ import io.netty.handler.codec.http.HttpVersion;
/*   9:    */ import io.netty.util.CharsetUtil;
/*  10:    */ import io.netty.util.internal.logging.InternalLogger;
/*  11:    */ 
/*  12:    */ public class WebSocketServerHandshaker07
/*  13:    */   extends WebSocketServerHandshaker
/*  14:    */ {
/*  15:    */   public static final String WEBSOCKET_07_ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
/*  16:    */   private final boolean allowExtensions;
/*  17:    */   
/*  18:    */   public WebSocketServerHandshaker07(String webSocketURL, String subprotocols, boolean allowExtensions, int maxFramePayloadLength)
/*  19:    */   {
/*  20: 58 */     super(WebSocketVersion.V07, webSocketURL, subprotocols, maxFramePayloadLength);
/*  21: 59 */     this.allowExtensions = allowExtensions;
/*  22:    */   }
/*  23:    */   
/*  24:    */   protected FullHttpResponse newHandshakeResponse(FullHttpRequest req, HttpHeaders headers)
/*  25:    */   {
/*  26: 98 */     FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SWITCHING_PROTOCOLS);
/*  27:101 */     if (headers != null) {
/*  28:102 */       res.headers().add(headers);
/*  29:    */     }
/*  30:105 */     String key = req.headers().get("Sec-WebSocket-Key");
/*  31:106 */     if (key == null) {
/*  32:107 */       throw new WebSocketHandshakeException("not a WebSocket request: missing key");
/*  33:    */     }
/*  34:109 */     String acceptSeed = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
/*  35:110 */     byte[] sha1 = WebSocketUtil.sha1(acceptSeed.getBytes(CharsetUtil.US_ASCII));
/*  36:111 */     String accept = WebSocketUtil.base64(sha1);
/*  37:113 */     if (logger.isDebugEnabled()) {
/*  38:114 */       logger.debug("WebSocket version 07 server handshake key: {}, response: {}.", key, accept);
/*  39:    */     }
/*  40:117 */     res.headers().add("Upgrade", "WebSocket".toLowerCase());
/*  41:118 */     res.headers().add("Connection", "Upgrade");
/*  42:119 */     res.headers().add("Sec-WebSocket-Accept", accept);
/*  43:120 */     String subprotocols = req.headers().get("Sec-WebSocket-Protocol");
/*  44:121 */     if (subprotocols != null)
/*  45:    */     {
/*  46:122 */       String selectedSubprotocol = selectSubprotocol(subprotocols);
/*  47:123 */       if (selectedSubprotocol == null)
/*  48:    */       {
/*  49:124 */         if (logger.isDebugEnabled()) {
/*  50:125 */           logger.debug("Requested subprotocol(s) not supported: {}", subprotocols);
/*  51:    */         }
/*  52:    */       }
/*  53:    */       else {
/*  54:128 */         res.headers().add("Sec-WebSocket-Protocol", selectedSubprotocol);
/*  55:    */       }
/*  56:    */     }
/*  57:131 */     return res;
/*  58:    */   }
/*  59:    */   
/*  60:    */   protected WebSocketFrameDecoder newWebsocketDecoder()
/*  61:    */   {
/*  62:136 */     return new WebSocket07FrameDecoder(true, this.allowExtensions, maxFramePayloadLength());
/*  63:    */   }
/*  64:    */   
/*  65:    */   protected WebSocketFrameEncoder newWebSocketEncoder()
/*  66:    */   {
/*  67:141 */     return new WebSocket07FrameEncoder(false);
/*  68:    */   }
/*  69:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker07
 * JD-Core Version:    0.7.0.1
 */