/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelPromise;
/*   8:    */ import io.netty.handler.codec.http.DefaultFullHttpResponse;
/*   9:    */ import io.netty.handler.codec.http.FullHttpRequest;
/*  10:    */ import io.netty.handler.codec.http.FullHttpResponse;
/*  11:    */ import io.netty.handler.codec.http.HttpHeaders;
/*  12:    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*  13:    */ import io.netty.handler.codec.http.HttpVersion;
/*  14:    */ import io.netty.util.internal.logging.InternalLogger;
/*  15:    */ import java.util.regex.Matcher;
/*  16:    */ import java.util.regex.Pattern;
/*  17:    */ 
/*  18:    */ public class WebSocketServerHandshaker00
/*  19:    */   extends WebSocketServerHandshaker
/*  20:    */ {
/*  21: 49 */   private static final Pattern BEGINNING_DIGIT = Pattern.compile("[^0-9]");
/*  22: 50 */   private static final Pattern BEGINNING_SPACE = Pattern.compile("[^ ]");
/*  23:    */   
/*  24:    */   public WebSocketServerHandshaker00(String webSocketURL, String subprotocols, int maxFramePayloadLength)
/*  25:    */   {
/*  26: 65 */     super(WebSocketVersion.V00, webSocketURL, subprotocols, maxFramePayloadLength);
/*  27:    */   }
/*  28:    */   
/*  29:    */   protected FullHttpResponse newHandshakeResponse(FullHttpRequest req, HttpHeaders headers)
/*  30:    */   {
/*  31:112 */     if ((!"Upgrade".equalsIgnoreCase(req.headers().get("Connection"))) || (!"WebSocket".equalsIgnoreCase(req.headers().get("Upgrade")))) {
/*  32:114 */       throw new WebSocketHandshakeException("not a WebSocket handshake request: missing upgrade");
/*  33:    */     }
/*  34:118 */     boolean isHixie76 = (req.headers().contains("Sec-WebSocket-Key1")) && (req.headers().contains("Sec-WebSocket-Key2"));
/*  35:    */     
/*  36:    */ 
/*  37:121 */     FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(101, isHixie76 ? "WebSocket Protocol Handshake" : "Web Socket Protocol Handshake"));
/*  38:123 */     if (headers != null) {
/*  39:124 */       res.headers().add(headers);
/*  40:    */     }
/*  41:127 */     res.headers().add("Upgrade", "WebSocket");
/*  42:128 */     res.headers().add("Connection", "Upgrade");
/*  43:131 */     if (isHixie76)
/*  44:    */     {
/*  45:133 */       res.headers().add("Sec-WebSocket-Origin", req.headers().get("Origin"));
/*  46:134 */       res.headers().add("Sec-WebSocket-Location", uri());
/*  47:135 */       String subprotocols = req.headers().get("Sec-WebSocket-Protocol");
/*  48:136 */       if (subprotocols != null)
/*  49:    */       {
/*  50:137 */         String selectedSubprotocol = selectSubprotocol(subprotocols);
/*  51:138 */         if (selectedSubprotocol == null)
/*  52:    */         {
/*  53:139 */           if (logger.isDebugEnabled()) {
/*  54:140 */             logger.debug("Requested subprotocol(s) not supported: {}", subprotocols);
/*  55:    */           }
/*  56:    */         }
/*  57:    */         else {
/*  58:143 */           res.headers().add("Sec-WebSocket-Protocol", selectedSubprotocol);
/*  59:    */         }
/*  60:    */       }
/*  61:148 */       String key1 = req.headers().get("Sec-WebSocket-Key1");
/*  62:149 */       String key2 = req.headers().get("Sec-WebSocket-Key2");
/*  63:150 */       int a = (int)(Long.parseLong(BEGINNING_DIGIT.matcher(key1).replaceAll("")) / BEGINNING_SPACE.matcher(key1).replaceAll("").length());
/*  64:    */       
/*  65:152 */       int b = (int)(Long.parseLong(BEGINNING_DIGIT.matcher(key2).replaceAll("")) / BEGINNING_SPACE.matcher(key2).replaceAll("").length());
/*  66:    */       
/*  67:154 */       long c = req.content().readLong();
/*  68:155 */       ByteBuf input = Unpooled.buffer(16);
/*  69:156 */       input.writeInt(a);
/*  70:157 */       input.writeInt(b);
/*  71:158 */       input.writeLong(c);
/*  72:159 */       res.content().writeBytes(WebSocketUtil.md5(input.array()));
/*  73:    */     }
/*  74:    */     else
/*  75:    */     {
/*  76:162 */       res.headers().add("WebSocket-Origin", req.headers().get("Origin"));
/*  77:163 */       res.headers().add("WebSocket-Location", uri());
/*  78:164 */       String protocol = req.headers().get("WebSocket-Protocol");
/*  79:165 */       if (protocol != null) {
/*  80:166 */         res.headers().add("WebSocket-Protocol", selectSubprotocol(protocol));
/*  81:    */       }
/*  82:    */     }
/*  83:169 */     return res;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public ChannelFuture close(Channel channel, CloseWebSocketFrame frame, ChannelPromise promise)
/*  87:    */   {
/*  88:182 */     return channel.writeAndFlush(frame, promise);
/*  89:    */   }
/*  90:    */   
/*  91:    */   protected WebSocketFrameDecoder newWebsocketDecoder()
/*  92:    */   {
/*  93:187 */     return new WebSocket00FrameDecoder(maxFramePayloadLength());
/*  94:    */   }
/*  95:    */   
/*  96:    */   protected WebSocketFrameEncoder newWebSocketEncoder()
/*  97:    */   {
/*  98:192 */     return new WebSocket00FrameEncoder();
/*  99:    */   }
/* 100:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker00
 * JD-Core Version:    0.7.0.1
 */