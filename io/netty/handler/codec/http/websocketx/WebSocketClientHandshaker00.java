/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.handler.codec.http.DefaultFullHttpRequest;
/*   6:    */ import io.netty.handler.codec.http.FullHttpRequest;
/*   7:    */ import io.netty.handler.codec.http.FullHttpResponse;
/*   8:    */ import io.netty.handler.codec.http.HttpHeaders;
/*   9:    */ import io.netty.handler.codec.http.HttpMethod;
/*  10:    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*  11:    */ import io.netty.handler.codec.http.HttpVersion;
/*  12:    */ import java.net.URI;
/*  13:    */ import java.nio.ByteBuffer;
/*  14:    */ 
/*  15:    */ public class WebSocketClientHandshaker00
/*  16:    */   extends WebSocketClientHandshaker
/*  17:    */ {
/*  18:    */   private ByteBuf expectedChallengeResponseBytes;
/*  19:    */   
/*  20:    */   public WebSocketClientHandshaker00(URI webSocketURL, WebSocketVersion version, String subprotocol, HttpHeaders customHeaders, int maxFramePayloadLength)
/*  21:    */   {
/*  22: 64 */     super(webSocketURL, version, subprotocol, customHeaders, maxFramePayloadLength);
/*  23:    */   }
/*  24:    */   
/*  25:    */   protected FullHttpRequest newHandshakeRequest()
/*  26:    */   {
/*  27: 88 */     int spaces1 = WebSocketUtil.randomNumber(1, 12);
/*  28: 89 */     int spaces2 = WebSocketUtil.randomNumber(1, 12);
/*  29:    */     
/*  30: 91 */     int max1 = 2147483647 / spaces1;
/*  31: 92 */     int max2 = 2147483647 / spaces2;
/*  32:    */     
/*  33: 94 */     int number1 = WebSocketUtil.randomNumber(0, max1);
/*  34: 95 */     int number2 = WebSocketUtil.randomNumber(0, max2);
/*  35:    */     
/*  36: 97 */     int product1 = number1 * spaces1;
/*  37: 98 */     int product2 = number2 * spaces2;
/*  38:    */     
/*  39:100 */     String key1 = Integer.toString(product1);
/*  40:101 */     String key2 = Integer.toString(product2);
/*  41:    */     
/*  42:103 */     key1 = insertRandomCharacters(key1);
/*  43:104 */     key2 = insertRandomCharacters(key2);
/*  44:    */     
/*  45:106 */     key1 = insertSpaces(key1, spaces1);
/*  46:107 */     key2 = insertSpaces(key2, spaces2);
/*  47:    */     
/*  48:109 */     byte[] key3 = WebSocketUtil.randomBytes(8);
/*  49:    */     
/*  50:111 */     ByteBuffer buffer = ByteBuffer.allocate(4);
/*  51:112 */     buffer.putInt(number1);
/*  52:113 */     byte[] number1Array = buffer.array();
/*  53:114 */     buffer = ByteBuffer.allocate(4);
/*  54:115 */     buffer.putInt(number2);
/*  55:116 */     byte[] number2Array = buffer.array();
/*  56:    */     
/*  57:118 */     byte[] challenge = new byte[16];
/*  58:119 */     System.arraycopy(number1Array, 0, challenge, 0, 4);
/*  59:120 */     System.arraycopy(number2Array, 0, challenge, 4, 4);
/*  60:121 */     System.arraycopy(key3, 0, challenge, 8, 8);
/*  61:122 */     this.expectedChallengeResponseBytes = Unpooled.wrappedBuffer(WebSocketUtil.md5(challenge));
/*  62:    */     
/*  63:    */ 
/*  64:125 */     URI wsURL = uri();
/*  65:126 */     String path = wsURL.getPath();
/*  66:127 */     if ((wsURL.getQuery() != null) && (!wsURL.getQuery().isEmpty())) {
/*  67:128 */       path = wsURL.getPath() + '?' + wsURL.getQuery();
/*  68:    */     }
/*  69:131 */     if ((path == null) || (path.isEmpty())) {
/*  70:132 */       path = "/";
/*  71:    */     }
/*  72:136 */     FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
/*  73:137 */     HttpHeaders headers = request.headers();
/*  74:138 */     headers.add("Upgrade", "WebSocket").add("Connection", "Upgrade").add("Host", wsURL.getHost());
/*  75:    */     
/*  76:    */ 
/*  77:    */ 
/*  78:142 */     int wsPort = wsURL.getPort();
/*  79:143 */     String originValue = "http://" + wsURL.getHost();
/*  80:144 */     if ((wsPort != 80) && (wsPort != 443)) {
/*  81:147 */       originValue = originValue + ':' + wsPort;
/*  82:    */     }
/*  83:150 */     headers.add("Origin", originValue).add("Sec-WebSocket-Key1", key1).add("Sec-WebSocket-Key2", key2);
/*  84:    */     
/*  85:    */ 
/*  86:    */ 
/*  87:154 */     String expectedSubprotocol = expectedSubprotocol();
/*  88:155 */     if ((expectedSubprotocol != null) && (!expectedSubprotocol.isEmpty())) {
/*  89:156 */       headers.add("Sec-WebSocket-Protocol", expectedSubprotocol);
/*  90:    */     }
/*  91:159 */     if (this.customHeaders != null) {
/*  92:160 */       headers.add(this.customHeaders);
/*  93:    */     }
/*  94:165 */     headers.set("Content-Length", Integer.valueOf(key3.length));
/*  95:166 */     request.content().writeBytes(key3);
/*  96:167 */     return request;
/*  97:    */   }
/*  98:    */   
/*  99:    */   protected void verify(FullHttpResponse response)
/* 100:    */   {
/* 101:192 */     HttpResponseStatus status = new HttpResponseStatus(101, "WebSocket Protocol Handshake");
/* 102:194 */     if (!response.getStatus().equals(status)) {
/* 103:195 */       throw new WebSocketHandshakeException("Invalid handshake response getStatus: " + response.getStatus());
/* 104:    */     }
/* 105:198 */     HttpHeaders headers = response.headers();
/* 106:    */     
/* 107:200 */     String upgrade = headers.get("Upgrade");
/* 108:201 */     if (!"WebSocket".equalsIgnoreCase(upgrade)) {
/* 109:202 */       throw new WebSocketHandshakeException("Invalid handshake response upgrade: " + upgrade);
/* 110:    */     }
/* 111:206 */     String connection = headers.get("Connection");
/* 112:207 */     if (!"Upgrade".equalsIgnoreCase(connection)) {
/* 113:208 */       throw new WebSocketHandshakeException("Invalid handshake response connection: " + connection);
/* 114:    */     }
/* 115:212 */     ByteBuf challenge = response.content();
/* 116:213 */     if (!challenge.equals(this.expectedChallengeResponseBytes)) {
/* 117:214 */       throw new WebSocketHandshakeException("Invalid challenge");
/* 118:    */     }
/* 119:    */   }
/* 120:    */   
/* 121:    */   private static String insertRandomCharacters(String key)
/* 122:    */   {
/* 123:219 */     int count = WebSocketUtil.randomNumber(1, 12);
/* 124:    */     
/* 125:221 */     char[] randomChars = new char[count];
/* 126:222 */     int randCount = 0;
/* 127:223 */     while (randCount < count)
/* 128:    */     {
/* 129:224 */       int rand = (int)(Math.random() * 126.0D + 33.0D);
/* 130:225 */       if (((33 < rand) && (rand < 47)) || ((58 < rand) && (rand < 126)))
/* 131:    */       {
/* 132:226 */         randomChars[randCount] = ((char)rand);
/* 133:227 */         randCount++;
/* 134:    */       }
/* 135:    */     }
/* 136:231 */     for (int i = 0; i < count; i++)
/* 137:    */     {
/* 138:232 */       int split = WebSocketUtil.randomNumber(0, key.length());
/* 139:233 */       String part1 = key.substring(0, split);
/* 140:234 */       String part2 = key.substring(split);
/* 141:235 */       key = part1 + randomChars[i] + part2;
/* 142:    */     }
/* 143:238 */     return key;
/* 144:    */   }
/* 145:    */   
/* 146:    */   private static String insertSpaces(String key, int spaces)
/* 147:    */   {
/* 148:242 */     for (int i = 0; i < spaces; i++)
/* 149:    */     {
/* 150:243 */       int split = WebSocketUtil.randomNumber(1, key.length() - 1);
/* 151:244 */       String part1 = key.substring(0, split);
/* 152:245 */       String part2 = key.substring(split);
/* 153:246 */       key = part1 + ' ' + part2;
/* 154:    */     }
/* 155:249 */     return key;
/* 156:    */   }
/* 157:    */   
/* 158:    */   protected WebSocketFrameDecoder newWebsocketDecoder()
/* 159:    */   {
/* 160:254 */     return new WebSocket00FrameDecoder(maxFramePayloadLength());
/* 161:    */   }
/* 162:    */   
/* 163:    */   protected WebSocketFrameEncoder newWebSocketEncoder()
/* 164:    */   {
/* 165:259 */     return new WebSocket00FrameEncoder();
/* 166:    */   }
/* 167:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker00
 * JD-Core Version:    0.7.0.1
 */