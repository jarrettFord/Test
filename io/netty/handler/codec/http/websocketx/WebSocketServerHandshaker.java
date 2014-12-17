/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelFuture;
/*   5:    */ import io.netty.channel.ChannelFutureListener;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.channel.ChannelPipeline;
/*   8:    */ import io.netty.channel.ChannelPromise;
/*   9:    */ import io.netty.handler.codec.http.FullHttpRequest;
/*  10:    */ import io.netty.handler.codec.http.FullHttpResponse;
/*  11:    */ import io.netty.handler.codec.http.HttpContentCompressor;
/*  12:    */ import io.netty.handler.codec.http.HttpHeaders;
/*  13:    */ import io.netty.handler.codec.http.HttpObjectAggregator;
/*  14:    */ import io.netty.handler.codec.http.HttpRequestDecoder;
/*  15:    */ import io.netty.handler.codec.http.HttpResponseEncoder;
/*  16:    */ import io.netty.handler.codec.http.HttpServerCodec;
/*  17:    */ import io.netty.util.internal.EmptyArrays;
/*  18:    */ import io.netty.util.internal.StringUtil;
/*  19:    */ import io.netty.util.internal.logging.InternalLogger;
/*  20:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  21:    */ import java.util.Collections;
/*  22:    */ import java.util.LinkedHashSet;
/*  23:    */ import java.util.Set;
/*  24:    */ 
/*  25:    */ public abstract class WebSocketServerHandshaker
/*  26:    */ {
/*  27: 45 */   protected static final InternalLogger logger = InternalLoggerFactory.getInstance(WebSocketServerHandshaker.class);
/*  28:    */   private final String uri;
/*  29:    */   private final String[] subprotocols;
/*  30:    */   private final WebSocketVersion version;
/*  31:    */   private final int maxFramePayloadLength;
/*  32:    */   private String selectedSubprotocol;
/*  33:    */   public static final String SUB_PROTOCOL_WILDCARD = "*";
/*  34:    */   
/*  35:    */   protected WebSocketServerHandshaker(WebSocketVersion version, String uri, String subprotocols, int maxFramePayloadLength)
/*  36:    */   {
/*  37: 78 */     this.version = version;
/*  38: 79 */     this.uri = uri;
/*  39: 80 */     if (subprotocols != null)
/*  40:    */     {
/*  41: 81 */       String[] subprotocolArray = StringUtil.split(subprotocols, ',');
/*  42: 82 */       for (int i = 0; i < subprotocolArray.length; i++) {
/*  43: 83 */         subprotocolArray[i] = subprotocolArray[i].trim();
/*  44:    */       }
/*  45: 85 */       this.subprotocols = subprotocolArray;
/*  46:    */     }
/*  47:    */     else
/*  48:    */     {
/*  49: 87 */       this.subprotocols = EmptyArrays.EMPTY_STRINGS;
/*  50:    */     }
/*  51: 89 */     this.maxFramePayloadLength = maxFramePayloadLength;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public String uri()
/*  55:    */   {
/*  56: 96 */     return this.uri;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public Set<String> subprotocols()
/*  60:    */   {
/*  61:103 */     Set<String> ret = new LinkedHashSet();
/*  62:104 */     Collections.addAll(ret, this.subprotocols);
/*  63:105 */     return ret;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public WebSocketVersion version()
/*  67:    */   {
/*  68:112 */     return this.version;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public int maxFramePayloadLength()
/*  72:    */   {
/*  73:121 */     return this.maxFramePayloadLength;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public ChannelFuture handshake(Channel channel, FullHttpRequest req)
/*  77:    */   {
/*  78:136 */     return handshake(channel, req, null, channel.newPromise());
/*  79:    */   }
/*  80:    */   
/*  81:    */   public final ChannelFuture handshake(Channel channel, FullHttpRequest req, HttpHeaders responseHeaders, final ChannelPromise promise)
/*  82:    */   {
/*  83:158 */     if (logger.isDebugEnabled()) {
/*  84:159 */       logger.debug("{} WebSocket version {} server handshake", channel, version());
/*  85:    */     }
/*  86:161 */     FullHttpResponse response = newHandshakeResponse(req, responseHeaders);
/*  87:162 */     ChannelPipeline p = channel.pipeline();
/*  88:163 */     if (p.get(HttpObjectAggregator.class) != null) {
/*  89:164 */       p.remove(HttpObjectAggregator.class);
/*  90:    */     }
/*  91:166 */     if (p.get(HttpContentCompressor.class) != null) {
/*  92:167 */       p.remove(HttpContentCompressor.class);
/*  93:    */     }
/*  94:169 */     ChannelHandlerContext ctx = p.context(HttpRequestDecoder.class);
/*  95:    */     String encoderName;
/*  96:    */     final String encoderName;
/*  97:171 */     if (ctx == null)
/*  98:    */     {
/*  99:173 */       ctx = p.context(HttpServerCodec.class);
/* 100:174 */       if (ctx == null)
/* 101:    */       {
/* 102:175 */         promise.setFailure(new IllegalStateException("No HttpDecoder and no HttpServerCodec in the pipeline"));
/* 103:    */         
/* 104:177 */         return promise;
/* 105:    */       }
/* 106:179 */       p.addBefore(ctx.name(), "wsdecoder", newWebsocketDecoder());
/* 107:180 */       p.addBefore(ctx.name(), "wsencoder", newWebSocketEncoder());
/* 108:181 */       encoderName = ctx.name();
/* 109:    */     }
/* 110:    */     else
/* 111:    */     {
/* 112:183 */       p.replace(ctx.name(), "wsdecoder", newWebsocketDecoder());
/* 113:    */       
/* 114:185 */       encoderName = p.context(HttpResponseEncoder.class).name();
/* 115:186 */       p.addBefore(encoderName, "wsencoder", newWebSocketEncoder());
/* 116:    */     }
/* 117:188 */     channel.writeAndFlush(response).addListener(new ChannelFutureListener()
/* 118:    */     {
/* 119:    */       public void operationComplete(ChannelFuture future)
/* 120:    */         throws Exception
/* 121:    */       {
/* 122:191 */         if (future.isSuccess())
/* 123:    */         {
/* 124:192 */           ChannelPipeline p = future.channel().pipeline();
/* 125:193 */           p.remove(encoderName);
/* 126:194 */           promise.setSuccess();
/* 127:    */         }
/* 128:    */         else
/* 129:    */         {
/* 130:196 */           promise.setFailure(future.cause());
/* 131:    */         }
/* 132:    */       }
/* 133:199 */     });
/* 134:200 */     return promise;
/* 135:    */   }
/* 136:    */   
/* 137:    */   protected abstract FullHttpResponse newHandshakeResponse(FullHttpRequest paramFullHttpRequest, HttpHeaders paramHttpHeaders);
/* 138:    */   
/* 139:    */   public ChannelFuture close(Channel channel, CloseWebSocketFrame frame)
/* 140:    */   {
/* 141:217 */     if (channel == null) {
/* 142:218 */       throw new NullPointerException("channel");
/* 143:    */     }
/* 144:220 */     return close(channel, frame, channel.newPromise());
/* 145:    */   }
/* 146:    */   
/* 147:    */   public ChannelFuture close(Channel channel, CloseWebSocketFrame frame, ChannelPromise promise)
/* 148:    */   {
/* 149:234 */     if (channel == null) {
/* 150:235 */       throw new NullPointerException("channel");
/* 151:    */     }
/* 152:237 */     return channel.writeAndFlush(frame, promise).addListener(ChannelFutureListener.CLOSE);
/* 153:    */   }
/* 154:    */   
/* 155:    */   protected String selectSubprotocol(String requestedSubprotocols)
/* 156:    */   {
/* 157:248 */     if ((requestedSubprotocols == null) || (this.subprotocols.length == 0)) {
/* 158:249 */       return null;
/* 159:    */     }
/* 160:252 */     String[] requestedSubprotocolArray = StringUtil.split(requestedSubprotocols, ',');
/* 161:253 */     for (String p : requestedSubprotocolArray)
/* 162:    */     {
/* 163:254 */       String requestedSubprotocol = p.trim();
/* 164:256 */       for (String supportedSubprotocol : this.subprotocols) {
/* 165:257 */         if (("*".equals(supportedSubprotocol)) || (requestedSubprotocol.equals(supportedSubprotocol)))
/* 166:    */         {
/* 167:259 */           this.selectedSubprotocol = requestedSubprotocol;
/* 168:260 */           return requestedSubprotocol;
/* 169:    */         }
/* 170:    */       }
/* 171:    */     }
/* 172:266 */     return null;
/* 173:    */   }
/* 174:    */   
/* 175:    */   public String selectedSubprotocol()
/* 176:    */   {
/* 177:276 */     return this.selectedSubprotocol;
/* 178:    */   }
/* 179:    */   
/* 180:    */   protected abstract WebSocketFrameDecoder newWebsocketDecoder();
/* 181:    */   
/* 182:    */   protected abstract WebSocketFrameEncoder newWebSocketEncoder();
/* 183:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker
 * JD-Core Version:    0.7.0.1
 */