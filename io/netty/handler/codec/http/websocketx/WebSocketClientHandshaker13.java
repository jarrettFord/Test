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
/*  15:    */ public class WebSocketClientHandshaker13
/*  16:    */   extends WebSocketClientHandshaker
/*  17:    */ {
/*  18: 42 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(WebSocketClientHandshaker13.class);
/*  19:    */   public static final String MAGIC_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
/*  20:    */   private String expectedChallengeResponseString;
/*  21:    */   private final boolean allowExtensions;
/*  22:    */   
/*  23:    */   public WebSocketClientHandshaker13(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength)
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
/*  46:113 */       logger.debug("WebSocket version 13 client handshake key: {}, expected response: {}", key, this.expectedChallengeResponseString);
/*  47:    */     }
/*  48:119 */     int wsPort = wsURL.getPort();
/*  49:122 */     if (wsPort == -1) {
/*  50:123 */       if ("wss".equals(wsURL.getScheme())) {
/*  51:124 */         wsPort = 443;
/*  52:    */       } else {
/*  53:126 */         wsPort = 80;
/*  54:    */       }
/*  55:    */     }
/*  56:130 */     FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
/*  57:131 */     HttpHeaders headers = request.headers();
/*  58:    */     
/*  59:133 */     headers.add("Upgrade", "WebSocket".toLowerCase()).add("Connection", "Upgrade").add("Sec-WebSocket-Key", key).add("Host", wsURL.getHost() + ':' + wsPort);
/*  60:    */     
/*  61:    */ 
/*  62:    */ 
/*  63:    */ 
/*  64:138 */     String originValue = "http://" + wsURL.getHost();
/*  65:139 */     if ((wsPort != 80) && (wsPort != 443)) {
/*  66:142 */       originValue = originValue + ':' + wsPort;
/*  67:    */     }
/*  68:144 */     headers.add("Sec-WebSocket-Origin", originValue);
/*  69:    */     
/*  70:146 */     String expectedSubprotocol = expectedSubprotocol();
/*  71:147 */     if ((expectedSubprotocol != null) && (!expectedSubprotocol.isEmpty())) {
/*  72:148 */       headers.add("Sec-WebSocket-Protocol", expectedSubprotocol);
/*  73:    */     }
/*  74:151 */     headers.add("Sec-WebSocket-Version", "13");
/*  75:153 */     if (this.customHeaders != null) {
/*  76:154 */       headers.add(this.customHeaders);
/*  77:    */     }
/*  78:156 */     return request;
/*  79:    */   }
/*  80:    */   
/*  81:    */   protected void verify(FullHttpResponse response)
/*  82:    */   {
/*  83:178 */     HttpResponseStatus status = HttpResponseStatus.SWITCHING_PROTOCOLS;
/*  84:179 */     HttpHeaders headers = response.headers();
/*  85:181 */     if (!response.getStatus().equals(status)) {
/*  86:182 */       throw new WebSocketHandshakeException("Invalid handshake response getStatus: " + response.getStatus());
/*  87:    */     }
/*  88:185 */     String upgrade = headers.get("Upgrade");
/*  89:186 */     if (!"WebSocket".equalsIgnoreCase(upgrade)) {
/*  90:187 */       throw new WebSocketHandshakeException("Invalid handshake response upgrade: " + upgrade);
/*  91:    */     }
/*  92:190 */     String connection = headers.get("Connection");
/*  93:191 */     if (!"Upgrade".equalsIgnoreCase(connection)) {
/*  94:192 */       throw new WebSocketHandshakeException("Invalid handshake response connection: " + connection);
/*  95:    */     }
/*  96:195 */     String accept = headers.get("Sec-WebSocket-Accept");
/*  97:196 */     if ((accept == null) || (!accept.equals(this.expectedChallengeResponseString))) {
/*  98:197 */       throw new WebSocketHandshakeException(String.format("Invalid challenge. Actual: %s. Expected: %s", new Object[] { accept, this.expectedChallengeResponseString }));
/*  99:    */     }
/* 100:    */   }
/* 101:    */   
/* 102:    */   protected WebSocketFrameDecoder newWebsocketDecoder()
/* 103:    */   {
/* 104:204 */     return new WebSocket13FrameDecoder(false, this.allowExtensions, maxFramePayloadLength());
/* 105:    */   }
/* 106:    */   
/* 107:    */   protected WebSocketFrameEncoder newWebSocketEncoder()
/* 108:    */   {
/* 109:209 */     return new WebSocket13FrameEncoder(true);
/* 110:    */   }
/* 111:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker13
 * JD-Core Version:    0.7.0.1
 */