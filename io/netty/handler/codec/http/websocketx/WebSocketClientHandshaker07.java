/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.handler.codec.http.DefaultFullHttpRequest;
/*   4:    */ import io.netty.handler.codec.http.FullHttpRequest;
/*   5:    */ import io.netty.handler.codec.http.FullHttpResponse;
/*   6:    */ import io.netty.handler.codec.http.HttpHeaders;
/*   7:    */ import io.netty.handler.codec.http.HttpMethod;
/*   8:    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*   9:    */ import io.netty.handler.codec.http.HttpVersion;
/*  10:    */ import io.netty.util.CharsetUtil;
/*  11:    */ import io.netty.util.internal.logging.InternalLogger;
/*  12:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  13:    */ import java.net.URI;
/*  14:    */ 
/*  15:    */ public class WebSocketClientHandshaker07
/*  16:    */   extends WebSocketClientHandshaker
/*  17:    */ {
/*  18: 42 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(WebSocketClientHandshaker07.class);
/*  19:    */   public static final String MAGIC_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
/*  20:    */   private String expectedChallengeResponseString;
/*  21:    */   private final boolean allowExtensions;
/*  22:    */   
/*  23:    */   public WebSocketClientHandshaker07(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength)
/*  24:    */   {
/*  25: 69 */     super(webSocketURL, version, subprotocol, customHeaders, maxFramePayloadLength);
/*  26: 70 */     this.allowExtensions = allowExtensions;
/*  27:    */   }
/*  28:    */   
/*  29:    */   protected FullHttpRequest newHandshakeRequest()
/*  30:    */   {
/*  31: 94 */     URI wsURL = uri();
/*  32: 95 */     String path = wsURL.getPath();
/*  33: 96 */     if ((wsURL.getQuery() != null) && (!wsURL.getQuery().isEmpty())) {
/*  34: 97 */       path = wsURL.getPath() + '?' + wsURL.getQuery();
/*  35:    */     }
/*  36:100 */     if ((path == null) || (path.isEmpty())) {
/*  37:101 */       path = "/";
/*  38:    */     }
/*  39:105 */     byte[] nonce = WebSocketUtil.randomBytes(16);
/*  40:106 */     String key = WebSocketUtil.base64(nonce);
/*  41:    */     
/*  42:108 */     String acceptSeed = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
/*  43:109 */     byte[] sha1 = WebSocketUtil.sha1(acceptSeed.getBytes(CharsetUtil.US_ASCII));
/*  44:110 */     this.expectedChallengeResponseString = WebSocketUtil.base64(sha1);
/*  45:112 */     if (logger.isDebugEnabled()) {
/*  46:113 */       logger.debug("WebSocket version 07 client handshake key: {}, expected response: {}", key, this.expectedChallengeResponseString);
/*  47:    */     }
/*  48:119 */     FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
/*  49:120 */     HttpHeaders headers = request.headers();
/*  50:    */     
/*  51:122 */     headers.add("Upgrade", "WebSocket".toLowerCase()).add("Connection", "Upgrade").add("Sec-WebSocket-Key", key).add("Host", wsURL.getHost());
/*  52:    */     
/*  53:    */ 
/*  54:    */ 
/*  55:    */ 
/*  56:127 */     int wsPort = wsURL.getPort();
/*  57:128 */     String originValue = "http://" + wsURL.getHost();
/*  58:129 */     if ((wsPort != 80) && (wsPort != 443)) {
/*  59:132 */       originValue = originValue + ':' + wsPort;
/*  60:    */     }
/*  61:134 */     headers.add("Sec-WebSocket-Origin", originValue);
/*  62:    */     
/*  63:136 */     String expectedSubprotocol = expectedSubprotocol();
/*  64:137 */     if ((expectedSubprotocol != null) && (!expectedSubprotocol.isEmpty())) {
/*  65:138 */       headers.add("Sec-WebSocket-Protocol", expectedSubprotocol);
/*  66:    */     }
/*  67:141 */     headers.add("Sec-WebSocket-Version", "7");
/*  68:143 */     if (this.customHeaders != null) {
/*  69:144 */       headers.add(this.customHeaders);
/*  70:    */     }
/*  71:146 */     return request;
/*  72:    */   }
/*  73:    */   
/*  74:    */   protected void verify(FullHttpResponse response)
/*  75:    */   {
/*  76:168 */     HttpResponseStatus status = HttpResponseStatus.SWITCHING_PROTOCOLS;
/*  77:169 */     HttpHeaders headers = response.headers();
/*  78:171 */     if (!response.getStatus().equals(status)) {
/*  79:172 */       throw new WebSocketHandshakeException("Invalid handshake response getStatus: " + response.getStatus());
/*  80:    */     }
/*  81:175 */     String upgrade = headers.get("Upgrade");
/*  82:176 */     if (!"WebSocket".equalsIgnoreCase(upgrade)) {
/*  83:177 */       throw new WebSocketHandshakeException("Invalid handshake response upgrade: " + upgrade);
/*  84:    */     }
/*  85:180 */     String connection = headers.get("Connection");
/*  86:181 */     if (!"Upgrade".equalsIgnoreCase(connection)) {
/*  87:182 */       throw new WebSocketHandshakeException("Invalid handshake response connection: " + connection);
/*  88:    */     }
/*  89:185 */     String accept = headers.get("Sec-WebSocket-Accept");
/*  90:186 */     if ((accept == null) || (!accept.equals(this.expectedChallengeResponseString))) {
/*  91:187 */       throw new WebSocketHandshakeException(String.format("Invalid challenge. Actual: %s. Expected: %s", new Object[] { accept, this.expectedChallengeResponseString }));
/*  92:    */     }
/*  93:    */   }
/*  94:    */   
/*  95:    */   protected WebSocketFrameDecoder newWebsocketDecoder()
/*  96:    */   {
/*  97:194 */     return new WebSocket07FrameDecoder(false, this.allowExtensions, maxFramePayloadLength());
/*  98:    */   }
/*  99:    */   
/* 100:    */   protected WebSocketFrameEncoder newWebSocketEncoder()
/* 101:    */   {
/* 102:199 */     return new WebSocket07FrameEncoder(true);
/* 103:    */   }
/* 104:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker07
 * JD-Core Version:    0.7.0.1
 */