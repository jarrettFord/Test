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
/*  12:    */ public class WebSocketServerHandshaker13
/*  13:    */   extends WebSocketServerHandshaker
/*  14:    */ {
/*  15:    */   public static final String WEBSOCKET_13_ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
/*  16:    */   private final boolean allowExtensions;
/*  17:    */   
/*  18:    */   public WebSocketServerHandshaker13(String webSocketURL, String subprotocols, boolean allowExtensions, int maxFramePayloadLength)
/*  19:    */   {
/*  20: 57 */     super(WebSocketVersion.V13, webSocketURL, subprotocols, maxFramePayloadLength);
/*  21: 58 */     this.allowExtensions = allowExtensions;
/*  22:    */   }
/*  23:    */   
/*  24:    */   protected FullHttpResponse newHandshakeResponse(FullHttpRequest req, HttpHeaders headers)
/*  25:    */   {
/*  26: 97 */     FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SWITCHING_PROTOCOLS);
/*  27: 98 */     if (headers != null) {
/*  28: 99 */       res.headers().add(headers);
/*  29:    */     }
/*  30:102 */     String key = req.headers().get("Sec-WebSocket-Key");
/*  31:103 */     if (key == null) {
/*  32:104 */       throw new WebSocketHandshakeException("not a WebSocket request: missing key");
/*  33:    */     }
/*  34:106 */     String acceptSeed = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
/*  35:107 */     byte[] sha1 = WebSocketUtil.sha1(acceptSeed.getBytes(CharsetUtil.US_ASCII));
/*  36:108 */     String accept = WebSocketUtil.base64(sha1);
/*  37:110 */     if (logger.isDebugEnabled()) {
/*  38:111 */       logger.debug("WebSocket version 13 server handshake key: {}, response: {}", key, accept);
/*  39:    */     }
/*  40:114 */     res.headers().add("Upgrade", "WebSocket".toLowerCase());
/*  41:115 */     res.headers().add("Connection", "Upgrade");
/*  42:116 */     res.headers().add("Sec-WebSocket-Accept", accept);
/*  43:117 */     String subprotocols = req.headers().get("Sec-WebSocket-Protocol");
/*  44:118 */     if (subprotocols != null)
/*  45:    */     {
/*  46:119 */       String selectedSubprotocol = selectSubprotocol(subprotocols);
/*  47:120 */       if (selectedSubprotocol == null)
/*  48:    */       {
/*  49:121 */         if (logger.isDebugEnabled()) {
/*  50:122 */           logger.debug("Requested subprotocol(s) not supported: {}", subprotocols);
/*  51:    */         }
/*  52:    */       }
/*  53:    */       else {
/*  54:125 */         res.headers().add("Sec-WebSocket-Protocol", selectedSubprotocol);
/*  55:    */       }
/*  56:    */     }
/*  57:128 */     return res;
/*  58:    */   }
/*  59:    */   
/*  60:    */   protected WebSocketFrameDecoder newWebsocketDecoder()
/*  61:    */   {
/*  62:133 */     return new WebSocket13FrameDecoder(true, this.allowExtensions, maxFramePayloadLength());
/*  63:    */   }
/*  64:    */   
/*  65:    */   protected WebSocketFrameEncoder newWebSocketEncoder()
/*  66:    */   {
/*  67:138 */     return new WebSocket13FrameEncoder(false);
/*  68:    */   }
/*  69:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker13
 * JD-Core Version:    0.7.0.1
 */