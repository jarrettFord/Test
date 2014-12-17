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
/*  11:    */ import io.netty.handler.codec.http.HttpClientCodec;
/*  12:    */ import io.netty.handler.codec.http.HttpContentDecompressor;
/*  13:    */ import io.netty.handler.codec.http.HttpHeaders;
/*  14:    */ import io.netty.handler.codec.http.HttpRequestEncoder;
/*  15:    */ import io.netty.handler.codec.http.HttpResponseDecoder;
/*  16:    */ import java.net.URI;
/*  17:    */ 
/*  18:    */ public abstract class WebSocketClientHandshaker
/*  19:    */ {
/*  20:    */   private final URI uri;
/*  21:    */   private final WebSocketVersion version;
/*  22:    */   private volatile boolean handshakeComplete;
/*  23:    */   private final String expectedSubprotocol;
/*  24:    */   private volatile String actualSubprotocol;
/*  25:    */   protected final HttpHeaders customHeaders;
/*  26:    */   private final int maxFramePayloadLength;
/*  27:    */   
/*  28:    */   protected WebSocketClientHandshaker(URI uri, WebSocketVersion version, String subprotocol, HttpHeaders customHeaders, int maxFramePayloadLength)
/*  29:    */   {
/*  30: 70 */     this.uri = uri;
/*  31: 71 */     this.version = version;
/*  32: 72 */     this.expectedSubprotocol = subprotocol;
/*  33: 73 */     this.customHeaders = customHeaders;
/*  34: 74 */     this.maxFramePayloadLength = maxFramePayloadLength;
/*  35:    */   }
/*  36:    */   
/*  37:    */   public URI uri()
/*  38:    */   {
/*  39: 81 */     return this.uri;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public WebSocketVersion version()
/*  43:    */   {
/*  44: 88 */     return this.version;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public int maxFramePayloadLength()
/*  48:    */   {
/*  49: 95 */     return this.maxFramePayloadLength;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public boolean isHandshakeComplete()
/*  53:    */   {
/*  54:102 */     return this.handshakeComplete;
/*  55:    */   }
/*  56:    */   
/*  57:    */   private void setHandshakeComplete()
/*  58:    */   {
/*  59:106 */     this.handshakeComplete = true;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public String expectedSubprotocol()
/*  63:    */   {
/*  64:113 */     return this.expectedSubprotocol;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public String actualSubprotocol()
/*  68:    */   {
/*  69:121 */     return this.actualSubprotocol;
/*  70:    */   }
/*  71:    */   
/*  72:    */   private void setActualSubprotocol(String actualSubprotocol)
/*  73:    */   {
/*  74:125 */     this.actualSubprotocol = actualSubprotocol;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public ChannelFuture handshake(Channel channel)
/*  78:    */   {
/*  79:135 */     if (channel == null) {
/*  80:136 */       throw new NullPointerException("channel");
/*  81:    */     }
/*  82:138 */     return handshake(channel, channel.newPromise());
/*  83:    */   }
/*  84:    */   
/*  85:    */   public final ChannelFuture handshake(Channel channel, final ChannelPromise promise)
/*  86:    */   {
/*  87:150 */     FullHttpRequest request = newHandshakeRequest();
/*  88:    */     
/*  89:152 */     HttpResponseDecoder decoder = (HttpResponseDecoder)channel.pipeline().get(HttpResponseDecoder.class);
/*  90:153 */     if (decoder == null)
/*  91:    */     {
/*  92:154 */       HttpClientCodec codec = (HttpClientCodec)channel.pipeline().get(HttpClientCodec.class);
/*  93:155 */       if (codec == null)
/*  94:    */       {
/*  95:156 */         promise.setFailure(new IllegalStateException("ChannelPipeline does not contain a HttpResponseDecoder or HttpClientCodec"));
/*  96:    */         
/*  97:158 */         return promise;
/*  98:    */       }
/*  99:    */     }
/* 100:162 */     channel.writeAndFlush(request).addListener(new ChannelFutureListener()
/* 101:    */     {
/* 102:    */       public void operationComplete(ChannelFuture future)
/* 103:    */       {
/* 104:165 */         if (future.isSuccess())
/* 105:    */         {
/* 106:166 */           ChannelPipeline p = future.channel().pipeline();
/* 107:167 */           ChannelHandlerContext ctx = p.context(HttpRequestEncoder.class);
/* 108:168 */           if (ctx == null) {
/* 109:169 */             ctx = p.context(HttpClientCodec.class);
/* 110:    */           }
/* 111:171 */           if (ctx == null)
/* 112:    */           {
/* 113:172 */             promise.setFailure(new IllegalStateException("ChannelPipeline does not contain a HttpRequestEncoder or HttpClientCodec"));
/* 114:    */             
/* 115:174 */             return;
/* 116:    */           }
/* 117:176 */           p.addAfter(ctx.name(), "ws-encoder", WebSocketClientHandshaker.this.newWebSocketEncoder());
/* 118:    */           
/* 119:178 */           promise.setSuccess();
/* 120:    */         }
/* 121:    */         else
/* 122:    */         {
/* 123:180 */           promise.setFailure(future.cause());
/* 124:    */         }
/* 125:    */       }
/* 126:183 */     });
/* 127:184 */     return promise;
/* 128:    */   }
/* 129:    */   
/* 130:    */   protected abstract FullHttpRequest newHandshakeRequest();
/* 131:    */   
/* 132:    */   public final void finishHandshake(Channel channel, FullHttpResponse response)
/* 133:    */   {
/* 134:201 */     verify(response);
/* 135:202 */     setActualSubprotocol(response.headers().get("Sec-WebSocket-Protocol"));
/* 136:203 */     setHandshakeComplete();
/* 137:    */     
/* 138:205 */     ChannelPipeline p = channel.pipeline();
/* 139:    */     
/* 140:207 */     HttpContentDecompressor decompressor = (HttpContentDecompressor)p.get(HttpContentDecompressor.class);
/* 141:208 */     if (decompressor != null) {
/* 142:209 */       p.remove(decompressor);
/* 143:    */     }
/* 144:212 */     ChannelHandlerContext ctx = p.context(HttpResponseDecoder.class);
/* 145:213 */     if (ctx == null)
/* 146:    */     {
/* 147:214 */       ctx = p.context(HttpClientCodec.class);
/* 148:215 */       if (ctx == null) {
/* 149:216 */         throw new IllegalStateException("ChannelPipeline does not contain a HttpRequestEncoder or HttpClientCodec");
/* 150:    */       }
/* 151:219 */       p.replace(ctx.name(), "ws-decoder", newWebsocketDecoder());
/* 152:    */     }
/* 153:    */     else
/* 154:    */     {
/* 155:221 */       if (p.get(HttpRequestEncoder.class) != null) {
/* 156:222 */         p.remove(HttpRequestEncoder.class);
/* 157:    */       }
/* 158:224 */       p.replace(ctx.name(), "ws-decoder", newWebsocketDecoder());
/* 159:    */     }
/* 160:    */   }
/* 161:    */   
/* 162:    */   protected abstract void verify(FullHttpResponse paramFullHttpResponse);
/* 163:    */   
/* 164:    */   protected abstract WebSocketFrameDecoder newWebsocketDecoder();
/* 165:    */   
/* 166:    */   protected abstract WebSocketFrameEncoder newWebSocketEncoder();
/* 167:    */   
/* 168:    */   public ChannelFuture close(Channel channel, CloseWebSocketFrame frame)
/* 169:    */   {
/* 170:253 */     if (channel == null) {
/* 171:254 */       throw new NullPointerException("channel");
/* 172:    */     }
/* 173:256 */     return close(channel, frame, channel.newPromise());
/* 174:    */   }
/* 175:    */   
/* 176:    */   public ChannelFuture close(Channel channel, CloseWebSocketFrame frame, ChannelPromise promise)
/* 177:    */   {
/* 178:270 */     if (channel == null) {
/* 179:271 */       throw new NullPointerException("channel");
/* 180:    */     }
/* 181:273 */     return channel.writeAndFlush(frame, promise);
/* 182:    */   }
/* 183:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker
 * JD-Core Version:    0.7.0.1
 */