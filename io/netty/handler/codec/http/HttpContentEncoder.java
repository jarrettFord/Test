/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufHolder;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.channel.embedded.EmbeddedChannel;
/*   7:    */ import io.netty.handler.codec.MessageToMessageCodec;
/*   8:    */ import io.netty.util.ReferenceCountUtil;
/*   9:    */ import java.util.ArrayDeque;
/*  10:    */ import java.util.List;
/*  11:    */ import java.util.Queue;
/*  12:    */ 
/*  13:    */ public abstract class HttpContentEncoder
/*  14:    */   extends MessageToMessageCodec<HttpRequest, HttpObject>
/*  15:    */ {
/*  16:    */   private final Queue<String> acceptEncodingQueue;
/*  17:    */   private String acceptEncoding;
/*  18:    */   private EmbeddedChannel encoder;
/*  19:    */   private State state;
/*  20:    */   
/*  21:    */   private static enum State
/*  22:    */   {
/*  23: 56 */     PASS_THROUGH,  AWAIT_HEADERS,  AWAIT_CONTENT;
/*  24:    */     
/*  25:    */     private State() {}
/*  26:    */   }
/*  27:    */   
/*  28:    */   public HttpContentEncoder()
/*  29:    */   {
/*  30: 61 */     this.acceptEncodingQueue = new ArrayDeque();
/*  31:    */     
/*  32:    */ 
/*  33: 64 */     this.state = State.AWAIT_HEADERS;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public boolean acceptOutboundMessage(Object msg)
/*  37:    */     throws Exception
/*  38:    */   {
/*  39: 68 */     return ((msg instanceof HttpContent)) || ((msg instanceof HttpResponse));
/*  40:    */   }
/*  41:    */   
/*  42:    */   protected void decode(ChannelHandlerContext ctx, HttpRequest msg, List<Object> out)
/*  43:    */     throws Exception
/*  44:    */   {
/*  45: 74 */     String acceptedEncoding = msg.headers().get("Accept-Encoding");
/*  46: 75 */     if (acceptedEncoding == null) {
/*  47: 76 */       acceptedEncoding = "identity";
/*  48:    */     }
/*  49: 78 */     this.acceptEncodingQueue.add(acceptedEncoding);
/*  50: 79 */     out.add(ReferenceCountUtil.retain(msg));
/*  51:    */   }
/*  52:    */   
/*  53:    */   protected void encode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out)
/*  54:    */     throws Exception
/*  55:    */   {
/*  56: 84 */     boolean isFull = ((msg instanceof HttpResponse)) && ((msg instanceof LastHttpContent));
/*  57: 85 */     switch (1.$SwitchMap$io$netty$handler$codec$http$HttpContentEncoder$State[this.state.ordinal()])
/*  58:    */     {
/*  59:    */     case 1: 
/*  60: 87 */       ensureHeaders(msg);
/*  61: 88 */       assert (this.encoder == null);
/*  62:    */       
/*  63: 90 */       HttpResponse res = (HttpResponse)msg;
/*  64: 92 */       if (res.getStatus().code() == 100)
/*  65:    */       {
/*  66: 93 */         if (isFull)
/*  67:    */         {
/*  68: 94 */           out.add(ReferenceCountUtil.retain(res));
/*  69:    */         }
/*  70:    */         else
/*  71:    */         {
/*  72: 96 */           out.add(res);
/*  73:    */           
/*  74: 98 */           this.state = State.PASS_THROUGH;
/*  75:    */         }
/*  76:    */       }
/*  77:    */       else
/*  78:    */       {
/*  79:104 */         this.acceptEncoding = ((String)this.acceptEncodingQueue.poll());
/*  80:105 */         if (this.acceptEncoding == null) {
/*  81:106 */           throw new IllegalStateException("cannot send more responses than requests");
/*  82:    */         }
/*  83:109 */         if (isFull) {
/*  84:111 */           if (!((ByteBufHolder)res).content().isReadable())
/*  85:    */           {
/*  86:112 */             out.add(ReferenceCountUtil.retain(res));
/*  87:113 */             return;
/*  88:    */           }
/*  89:    */         }
/*  90:118 */         Result result = beginEncode(res, this.acceptEncoding);
/*  91:121 */         if (result == null)
/*  92:    */         {
/*  93:122 */           if (isFull)
/*  94:    */           {
/*  95:123 */             out.add(ReferenceCountUtil.retain(res));
/*  96:    */           }
/*  97:    */           else
/*  98:    */           {
/*  99:125 */             out.add(res);
/* 100:    */             
/* 101:127 */             this.state = State.PASS_THROUGH;
/* 102:    */           }
/* 103:    */         }
/* 104:    */         else
/* 105:    */         {
/* 106:132 */           this.encoder = result.contentEncoder();
/* 107:    */           
/* 108:    */ 
/* 109:    */ 
/* 110:136 */           res.headers().set("Content-Encoding", result.targetContentEncoding());
/* 111:    */           
/* 112:    */ 
/* 113:139 */           res.headers().remove("Content-Length");
/* 114:140 */           res.headers().set("Transfer-Encoding", "chunked");
/* 115:143 */           if (isFull)
/* 116:    */           {
/* 117:145 */             HttpResponse newRes = new DefaultHttpResponse(res.getProtocolVersion(), res.getStatus());
/* 118:146 */             newRes.headers().set(res.headers());
/* 119:147 */             out.add(newRes);
/* 120:    */           }
/* 121:    */           else
/* 122:    */           {
/* 123:150 */             out.add(res);
/* 124:151 */             this.state = State.AWAIT_CONTENT;
/* 125:152 */             if (!(msg instanceof HttpContent)) {
/* 126:    */               return;
/* 127:    */             }
/* 128:    */           }
/* 129:    */         }
/* 130:    */       }
/* 131:    */       break;
/* 132:    */     case 2: 
/* 133:161 */       ensureContent(msg);
/* 134:162 */       if (encodeContent((HttpContent)msg, out)) {
/* 135:163 */         this.state = State.AWAIT_HEADERS;
/* 136:    */       }
/* 137:    */       break;
/* 138:    */     case 3: 
/* 139:168 */       ensureContent(msg);
/* 140:169 */       out.add(ReferenceCountUtil.retain(msg));
/* 141:171 */       if ((msg instanceof LastHttpContent)) {
/* 142:172 */         this.state = State.AWAIT_HEADERS;
/* 143:    */       }
/* 144:    */       break;
/* 145:    */     }
/* 146:    */   }
/* 147:    */   
/* 148:    */   private static void ensureHeaders(HttpObject msg)
/* 149:    */   {
/* 150:180 */     if (!(msg instanceof HttpResponse)) {
/* 151:181 */       throw new IllegalStateException("unexpected message type: " + msg.getClass().getName() + " (expected: " + HttpResponse.class.getSimpleName() + ')');
/* 152:    */     }
/* 153:    */   }
/* 154:    */   
/* 155:    */   private static void ensureContent(HttpObject msg)
/* 156:    */   {
/* 157:188 */     if (!(msg instanceof HttpContent)) {
/* 158:189 */       throw new IllegalStateException("unexpected message type: " + msg.getClass().getName() + " (expected: " + HttpContent.class.getSimpleName() + ')');
/* 159:    */     }
/* 160:    */   }
/* 161:    */   
/* 162:    */   private boolean encodeContent(HttpContent c, List<Object> out)
/* 163:    */   {
/* 164:196 */     ByteBuf content = c.content();
/* 165:    */     
/* 166:198 */     encode(content, out);
/* 167:200 */     if ((c instanceof LastHttpContent))
/* 168:    */     {
/* 169:201 */       finishEncode(out);
/* 170:202 */       LastHttpContent last = (LastHttpContent)c;
/* 171:    */       
/* 172:    */ 
/* 173:    */ 
/* 174:206 */       HttpHeaders headers = last.trailingHeaders();
/* 175:207 */       if (headers.isEmpty()) {
/* 176:208 */         out.add(LastHttpContent.EMPTY_LAST_CONTENT);
/* 177:    */       } else {
/* 178:210 */         out.add(new ComposedLastHttpContent(headers));
/* 179:    */       }
/* 180:212 */       return true;
/* 181:    */     }
/* 182:214 */     return false;
/* 183:    */   }
/* 184:    */   
/* 185:    */   protected abstract Result beginEncode(HttpResponse paramHttpResponse, String paramString)
/* 186:    */     throws Exception;
/* 187:    */   
/* 188:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 189:    */     throws Exception
/* 190:    */   {
/* 191:235 */     cleanup();
/* 192:236 */     super.handlerRemoved(ctx);
/* 193:    */   }
/* 194:    */   
/* 195:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 196:    */     throws Exception
/* 197:    */   {
/* 198:241 */     cleanup();
/* 199:242 */     super.channelInactive(ctx);
/* 200:    */   }
/* 201:    */   
/* 202:    */   private void cleanup()
/* 203:    */   {
/* 204:246 */     if (this.encoder != null)
/* 205:    */     {
/* 206:248 */       if (this.encoder.finish()) {
/* 207:    */         for (;;)
/* 208:    */         {
/* 209:250 */           ByteBuf buf = (ByteBuf)this.encoder.readOutbound();
/* 210:251 */           if (buf == null) {
/* 211:    */             break;
/* 212:    */           }
/* 213:256 */           buf.release();
/* 214:    */         }
/* 215:    */       }
/* 216:259 */       this.encoder = null;
/* 217:    */     }
/* 218:    */   }
/* 219:    */   
/* 220:    */   private void encode(ByteBuf in, List<Object> out)
/* 221:    */   {
/* 222:265 */     this.encoder.writeOutbound(new Object[] { in.retain() });
/* 223:266 */     fetchEncoderOutput(out);
/* 224:    */   }
/* 225:    */   
/* 226:    */   private void finishEncode(List<Object> out)
/* 227:    */   {
/* 228:270 */     if (this.encoder.finish()) {
/* 229:271 */       fetchEncoderOutput(out);
/* 230:    */     }
/* 231:273 */     this.encoder = null;
/* 232:    */   }
/* 233:    */   
/* 234:    */   private void fetchEncoderOutput(List<Object> out)
/* 235:    */   {
/* 236:    */     for (;;)
/* 237:    */     {
/* 238:278 */       ByteBuf buf = (ByteBuf)this.encoder.readOutbound();
/* 239:279 */       if (buf == null) {
/* 240:    */         break;
/* 241:    */       }
/* 242:282 */       if (!buf.isReadable()) {
/* 243:283 */         buf.release();
/* 244:    */       } else {
/* 245:286 */         out.add(new DefaultHttpContent(buf));
/* 246:    */       }
/* 247:    */     }
/* 248:    */   }
/* 249:    */   
/* 250:    */   public static final class Result
/* 251:    */   {
/* 252:    */     private final String targetContentEncoding;
/* 253:    */     private final EmbeddedChannel contentEncoder;
/* 254:    */     
/* 255:    */     public Result(String targetContentEncoding, EmbeddedChannel contentEncoder)
/* 256:    */     {
/* 257:295 */       if (targetContentEncoding == null) {
/* 258:296 */         throw new NullPointerException("targetContentEncoding");
/* 259:    */       }
/* 260:298 */       if (contentEncoder == null) {
/* 261:299 */         throw new NullPointerException("contentEncoder");
/* 262:    */       }
/* 263:302 */       this.targetContentEncoding = targetContentEncoding;
/* 264:303 */       this.contentEncoder = contentEncoder;
/* 265:    */     }
/* 266:    */     
/* 267:    */     public String targetContentEncoding()
/* 268:    */     {
/* 269:307 */       return this.targetContentEncoding;
/* 270:    */     }
/* 271:    */     
/* 272:    */     public EmbeddedChannel contentEncoder()
/* 273:    */     {
/* 274:311 */       return this.contentEncoder;
/* 275:    */     }
/* 276:    */   }
/* 277:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpContentEncoder
 * JD-Core Version:    0.7.0.1
 */