/*   1:    */ package io.netty.handler.codec.http.cors;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelDuplexHandler;
/*   4:    */ import io.netty.channel.ChannelFuture;
/*   5:    */ import io.netty.channel.ChannelFutureListener;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.channel.ChannelPromise;
/*   8:    */ import io.netty.handler.codec.http.DefaultFullHttpResponse;
/*   9:    */ import io.netty.handler.codec.http.HttpHeaders;
/*  10:    */ import io.netty.handler.codec.http.HttpMethod;
/*  11:    */ import io.netty.handler.codec.http.HttpRequest;
/*  12:    */ import io.netty.handler.codec.http.HttpResponse;
/*  13:    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*  14:    */ import io.netty.util.internal.logging.InternalLogger;
/*  15:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  16:    */ import java.util.Set;
/*  17:    */ 
/*  18:    */ public class CorsHandler
/*  19:    */   extends ChannelDuplexHandler
/*  20:    */ {
/*  21: 41 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(CorsHandler.class);
/*  22:    */   private final CorsConfig config;
/*  23:    */   private HttpRequest request;
/*  24:    */   
/*  25:    */   public CorsHandler(CorsConfig config)
/*  26:    */   {
/*  27: 47 */     this.config = config;
/*  28:    */   }
/*  29:    */   
/*  30:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  31:    */     throws Exception
/*  32:    */   {
/*  33: 52 */     if ((this.config.isCorsSupportEnabled()) && ((msg instanceof HttpRequest)))
/*  34:    */     {
/*  35: 53 */       this.request = ((HttpRequest)msg);
/*  36: 54 */       if (isPreflightRequest(this.request))
/*  37:    */       {
/*  38: 55 */         handlePreflight(ctx, this.request);
/*  39: 56 */         return;
/*  40:    */       }
/*  41: 58 */       if ((this.config.isShortCurcuit()) && (!validateOrigin()))
/*  42:    */       {
/*  43: 59 */         forbidden(ctx, this.request);
/*  44: 60 */         return;
/*  45:    */       }
/*  46:    */     }
/*  47: 63 */     ctx.fireChannelRead(msg);
/*  48:    */   }
/*  49:    */   
/*  50:    */   private void handlePreflight(ChannelHandlerContext ctx, HttpRequest request)
/*  51:    */   {
/*  52: 67 */     HttpResponse response = new DefaultFullHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
/*  53: 68 */     if (setOrigin(response))
/*  54:    */     {
/*  55: 69 */       setAllowMethods(response);
/*  56: 70 */       setAllowHeaders(response);
/*  57: 71 */       setAllowCredentials(response);
/*  58: 72 */       setMaxAge(response);
/*  59: 73 */       setPreflightHeaders(response);
/*  60:    */     }
/*  61: 75 */     ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
/*  62:    */   }
/*  63:    */   
/*  64:    */   private void setPreflightHeaders(HttpResponse response)
/*  65:    */   {
/*  66: 85 */     response.headers().add(this.config.preflightResponseHeaders());
/*  67:    */   }
/*  68:    */   
/*  69:    */   private boolean setOrigin(HttpResponse response)
/*  70:    */   {
/*  71: 89 */     String origin = this.request.headers().get("Origin");
/*  72: 90 */     if (origin != null)
/*  73:    */     {
/*  74: 91 */       if (("null".equals(origin)) && (this.config.isNullOriginAllowed()))
/*  75:    */       {
/*  76: 92 */         setAnyOrigin(response);
/*  77: 93 */         return true;
/*  78:    */       }
/*  79: 95 */       if (this.config.isAnyOriginSupported())
/*  80:    */       {
/*  81: 96 */         if (this.config.isCredentialsAllowed())
/*  82:    */         {
/*  83: 97 */           echoRequestOrigin(response);
/*  84: 98 */           setVaryHeader(response);
/*  85:    */         }
/*  86:    */         else
/*  87:    */         {
/*  88:100 */           setAnyOrigin(response);
/*  89:    */         }
/*  90:102 */         return true;
/*  91:    */       }
/*  92:104 */       if (this.config.origins().contains(origin))
/*  93:    */       {
/*  94:105 */         setOrigin(response, origin);
/*  95:106 */         setVaryHeader(response);
/*  96:107 */         return true;
/*  97:    */       }
/*  98:109 */       logger.debug("Request origin [" + origin + "] was not among the configured origins " + this.config.origins());
/*  99:    */     }
/* 100:111 */     return false;
/* 101:    */   }
/* 102:    */   
/* 103:    */   private boolean validateOrigin()
/* 104:    */   {
/* 105:115 */     if (this.config.isAnyOriginSupported()) {
/* 106:116 */       return true;
/* 107:    */     }
/* 108:119 */     String origin = this.request.headers().get("Origin");
/* 109:120 */     if (origin == null) {
/* 110:122 */       return true;
/* 111:    */     }
/* 112:125 */     if (("null".equals(origin)) && (this.config.isNullOriginAllowed())) {
/* 113:126 */       return true;
/* 114:    */     }
/* 115:129 */     return this.config.origins().contains(origin);
/* 116:    */   }
/* 117:    */   
/* 118:    */   private void echoRequestOrigin(HttpResponse response)
/* 119:    */   {
/* 120:133 */     setOrigin(response, this.request.headers().get("Origin"));
/* 121:    */   }
/* 122:    */   
/* 123:    */   private static void setVaryHeader(HttpResponse response)
/* 124:    */   {
/* 125:137 */     response.headers().set("Vary", "Origin");
/* 126:    */   }
/* 127:    */   
/* 128:    */   private static void setAnyOrigin(HttpResponse response)
/* 129:    */   {
/* 130:141 */     setOrigin(response, "*");
/* 131:    */   }
/* 132:    */   
/* 133:    */   private static void setOrigin(HttpResponse response, String origin)
/* 134:    */   {
/* 135:145 */     response.headers().set("Access-Control-Allow-Origin", origin);
/* 136:    */   }
/* 137:    */   
/* 138:    */   private void setAllowCredentials(HttpResponse response)
/* 139:    */   {
/* 140:149 */     if (this.config.isCredentialsAllowed()) {
/* 141:150 */       response.headers().set("Access-Control-Allow-Credentials", "true");
/* 142:    */     }
/* 143:    */   }
/* 144:    */   
/* 145:    */   private static boolean isPreflightRequest(HttpRequest request)
/* 146:    */   {
/* 147:155 */     HttpHeaders headers = request.headers();
/* 148:156 */     return (request.getMethod().equals(HttpMethod.OPTIONS)) && (headers.contains("Origin")) && (headers.contains("Access-Control-Request-Method"));
/* 149:    */   }
/* 150:    */   
/* 151:    */   private void setExposeHeaders(HttpResponse response)
/* 152:    */   {
/* 153:162 */     if (!this.config.exposedHeaders().isEmpty()) {
/* 154:163 */       response.headers().set("Access-Control-Expose-Headers", this.config.exposedHeaders());
/* 155:    */     }
/* 156:    */   }
/* 157:    */   
/* 158:    */   private void setAllowMethods(HttpResponse response)
/* 159:    */   {
/* 160:168 */     response.headers().set("Access-Control-Allow-Methods", this.config.allowedRequestMethods());
/* 161:    */   }
/* 162:    */   
/* 163:    */   private void setAllowHeaders(HttpResponse response)
/* 164:    */   {
/* 165:172 */     response.headers().set("Access-Control-Allow-Headers", this.config.allowedRequestHeaders());
/* 166:    */   }
/* 167:    */   
/* 168:    */   private void setMaxAge(HttpResponse response)
/* 169:    */   {
/* 170:176 */     response.headers().set("Access-Control-Max-Age", Long.valueOf(this.config.maxAge()));
/* 171:    */   }
/* 172:    */   
/* 173:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 174:    */     throws Exception
/* 175:    */   {
/* 176:182 */     if ((this.config.isCorsSupportEnabled()) && ((msg instanceof HttpResponse)))
/* 177:    */     {
/* 178:183 */       HttpResponse response = (HttpResponse)msg;
/* 179:184 */       if (setOrigin(response))
/* 180:    */       {
/* 181:185 */         setAllowCredentials(response);
/* 182:186 */         setAllowHeaders(response);
/* 183:187 */         setExposeHeaders(response);
/* 184:    */       }
/* 185:    */     }
/* 186:190 */     ctx.writeAndFlush(msg, promise);
/* 187:    */   }
/* 188:    */   
/* 189:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 190:    */     throws Exception
/* 191:    */   {
/* 192:195 */     logger.error("Caught error in CorsHandler", cause);
/* 193:196 */     ctx.fireExceptionCaught(cause);
/* 194:    */   }
/* 195:    */   
/* 196:    */   private static void forbidden(ChannelHandlerContext ctx, HttpRequest request)
/* 197:    */   {
/* 198:200 */     ctx.writeAndFlush(new DefaultFullHttpResponse(request.getProtocolVersion(), HttpResponseStatus.FORBIDDEN)).addListener(ChannelFutureListener.CLOSE);
/* 199:    */   }
/* 200:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.cors.CorsHandler
 * JD-Core Version:    0.7.0.1
 */