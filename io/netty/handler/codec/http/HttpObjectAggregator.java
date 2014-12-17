/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.CompositeByteBuf;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelFutureListener;
/*   8:    */ import io.netty.channel.ChannelHandlerContext;
/*   9:    */ import io.netty.handler.codec.DecoderResult;
/*  10:    */ import io.netty.handler.codec.MessageToMessageDecoder;
/*  11:    */ import io.netty.handler.codec.TooLongFrameException;
/*  12:    */ import java.util.List;
/*  13:    */ 
/*  14:    */ public class HttpObjectAggregator
/*  15:    */   extends MessageToMessageDecoder<HttpObject>
/*  16:    */ {
/*  17:    */   public static final int DEFAULT_MAX_COMPOSITEBUFFER_COMPONENTS = 1024;
/*  18: 54 */   private static final FullHttpResponse CONTINUE = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER);
/*  19:    */   private final int maxContentLength;
/*  20:    */   private FullHttpMessage currentMessage;
/*  21:    */   private boolean tooLongFrameFound;
/*  22: 61 */   private int maxCumulationBufferComponents = 1024;
/*  23:    */   private ChannelHandlerContext ctx;
/*  24:    */   
/*  25:    */   public HttpObjectAggregator(int maxContentLength)
/*  26:    */   {
/*  27: 73 */     if (maxContentLength <= 0) {
/*  28: 74 */       throw new IllegalArgumentException("maxContentLength must be a positive integer: " + maxContentLength);
/*  29:    */     }
/*  30: 78 */     this.maxContentLength = maxContentLength;
/*  31:    */   }
/*  32:    */   
/*  33:    */   public final int getMaxCumulationBufferComponents()
/*  34:    */   {
/*  35: 88 */     return this.maxCumulationBufferComponents;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public final void setMaxCumulationBufferComponents(int maxCumulationBufferComponents)
/*  39:    */   {
/*  40: 99 */     if (maxCumulationBufferComponents < 2) {
/*  41:100 */       throw new IllegalArgumentException("maxCumulationBufferComponents: " + maxCumulationBufferComponents + " (expected: >= 2)");
/*  42:    */     }
/*  43:105 */     if (this.ctx == null) {
/*  44:106 */       this.maxCumulationBufferComponents = maxCumulationBufferComponents;
/*  45:    */     } else {
/*  46:108 */       throw new IllegalStateException("decoder properties cannot be changed once the decoder is added to a pipeline.");
/*  47:    */     }
/*  48:    */   }
/*  49:    */   
/*  50:    */   protected void decode(final ChannelHandlerContext ctx, HttpObject msg, List<Object> out)
/*  51:    */     throws Exception
/*  52:    */   {
/*  53:115 */     FullHttpMessage currentMessage = this.currentMessage;
/*  54:117 */     if ((msg instanceof HttpMessage))
/*  55:    */     {
/*  56:118 */       this.tooLongFrameFound = false;
/*  57:119 */       assert (currentMessage == null);
/*  58:    */       
/*  59:121 */       HttpMessage m = (HttpMessage)msg;
/*  60:128 */       if (HttpHeaders.is100ContinueExpected(m)) {
/*  61:129 */         ctx.writeAndFlush(CONTINUE).addListener(new ChannelFutureListener()
/*  62:    */         {
/*  63:    */           public void operationComplete(ChannelFuture future)
/*  64:    */             throws Exception
/*  65:    */           {
/*  66:132 */             if (!future.isSuccess()) {
/*  67:133 */               ctx.fireExceptionCaught(future.cause());
/*  68:    */             }
/*  69:    */           }
/*  70:    */         });
/*  71:    */       }
/*  72:139 */       if (!m.getDecoderResult().isSuccess())
/*  73:    */       {
/*  74:140 */         HttpHeaders.removeTransferEncodingChunked(m);
/*  75:141 */         out.add(toFullMessage(m));
/*  76:142 */         this.currentMessage = null;
/*  77:143 */         return;
/*  78:    */       }
/*  79:145 */       if ((msg instanceof HttpRequest))
/*  80:    */       {
/*  81:146 */         HttpRequest header = (HttpRequest)msg;
/*  82:147 */         this.currentMessage = (currentMessage = new DefaultFullHttpRequest(header.getProtocolVersion(), header.getMethod(), header.getUri(), Unpooled.compositeBuffer(this.maxCumulationBufferComponents)));
/*  83:    */       }
/*  84:149 */       else if ((msg instanceof HttpResponse))
/*  85:    */       {
/*  86:150 */         HttpResponse header = (HttpResponse)msg;
/*  87:151 */         this.currentMessage = (currentMessage = new DefaultFullHttpResponse(header.getProtocolVersion(), header.getStatus(), Unpooled.compositeBuffer(this.maxCumulationBufferComponents)));
/*  88:    */       }
/*  89:    */       else
/*  90:    */       {
/*  91:155 */         throw new Error();
/*  92:    */       }
/*  93:158 */       currentMessage.headers().set(m.headers());
/*  94:    */       
/*  95:    */ 
/*  96:161 */       HttpHeaders.removeTransferEncodingChunked(currentMessage);
/*  97:    */     }
/*  98:162 */     else if ((msg instanceof HttpContent))
/*  99:    */     {
/* 100:163 */       if (this.tooLongFrameFound)
/* 101:    */       {
/* 102:164 */         if ((msg instanceof LastHttpContent)) {
/* 103:165 */           this.currentMessage = null;
/* 104:    */         }
/* 105:168 */         return;
/* 106:    */       }
/* 107:170 */       assert (currentMessage != null);
/* 108:    */       
/* 109:    */ 
/* 110:173 */       HttpContent chunk = (HttpContent)msg;
/* 111:174 */       CompositeByteBuf content = (CompositeByteBuf)currentMessage.content();
/* 112:176 */       if (content.readableBytes() > this.maxContentLength - chunk.content().readableBytes())
/* 113:    */       {
/* 114:177 */         this.tooLongFrameFound = true;
/* 115:    */         
/* 116:    */ 
/* 117:180 */         currentMessage.release();
/* 118:181 */         this.currentMessage = null;
/* 119:    */         
/* 120:183 */         throw new TooLongFrameException("HTTP content length exceeded " + this.maxContentLength + " bytes.");
/* 121:    */       }
/* 122:189 */       if (chunk.content().isReadable())
/* 123:    */       {
/* 124:190 */         chunk.retain();
/* 125:191 */         content.addComponent(chunk.content());
/* 126:192 */         content.writerIndex(content.writerIndex() + chunk.content().readableBytes());
/* 127:    */       }
/* 128:    */       boolean last;
/* 129:    */       boolean last;
/* 130:196 */       if (!chunk.getDecoderResult().isSuccess())
/* 131:    */       {
/* 132:197 */         currentMessage.setDecoderResult(DecoderResult.failure(chunk.getDecoderResult().cause()));
/* 133:    */         
/* 134:199 */         last = true;
/* 135:    */       }
/* 136:    */       else
/* 137:    */       {
/* 138:201 */         last = chunk instanceof LastHttpContent;
/* 139:    */       }
/* 140:204 */       if (last)
/* 141:    */       {
/* 142:205 */         this.currentMessage = null;
/* 143:208 */         if ((chunk instanceof LastHttpContent))
/* 144:    */         {
/* 145:209 */           LastHttpContent trailer = (LastHttpContent)chunk;
/* 146:210 */           currentMessage.headers().add(trailer.trailingHeaders());
/* 147:    */         }
/* 148:214 */         currentMessage.headers().set("Content-Length", String.valueOf(content.readableBytes()));
/* 149:    */         
/* 150:    */ 
/* 151:    */ 
/* 152:    */ 
/* 153:219 */         out.add(currentMessage);
/* 154:    */       }
/* 155:    */     }
/* 156:    */     else
/* 157:    */     {
/* 158:222 */       throw new Error();
/* 159:    */     }
/* 160:    */   }
/* 161:    */   
/* 162:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 163:    */     throws Exception
/* 164:    */   {
/* 165:228 */     super.channelInactive(ctx);
/* 166:231 */     if (this.currentMessage != null)
/* 167:    */     {
/* 168:232 */       this.currentMessage.release();
/* 169:233 */       this.currentMessage = null;
/* 170:    */     }
/* 171:    */   }
/* 172:    */   
/* 173:    */   public void handlerAdded(ChannelHandlerContext ctx)
/* 174:    */     throws Exception
/* 175:    */   {
/* 176:239 */     this.ctx = ctx;
/* 177:    */   }
/* 178:    */   
/* 179:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 180:    */     throws Exception
/* 181:    */   {
/* 182:244 */     super.handlerRemoved(ctx);
/* 183:247 */     if (this.currentMessage != null)
/* 184:    */     {
/* 185:248 */       this.currentMessage.release();
/* 186:249 */       this.currentMessage = null;
/* 187:    */     }
/* 188:    */   }
/* 189:    */   
/* 190:    */   private static FullHttpMessage toFullMessage(HttpMessage msg)
/* 191:    */   {
/* 192:254 */     if ((msg instanceof FullHttpMessage)) {
/* 193:255 */       return ((FullHttpMessage)msg).retain();
/* 194:    */     }
/* 195:259 */     if ((msg instanceof HttpRequest))
/* 196:    */     {
/* 197:260 */       HttpRequest req = (HttpRequest)msg;
/* 198:261 */       FullHttpMessage fullMsg = new DefaultFullHttpRequest(req.getProtocolVersion(), req.getMethod(), req.getUri(), Unpooled.EMPTY_BUFFER, false);
/* 199:    */       
/* 200:263 */       fullMsg.setDecoderResult(req.getDecoderResult());
/* 201:    */     }
/* 202:264 */     else if ((msg instanceof HttpResponse))
/* 203:    */     {
/* 204:265 */       HttpResponse res = (HttpResponse)msg;
/* 205:266 */       FullHttpMessage fullMsg = new DefaultFullHttpResponse(res.getProtocolVersion(), res.getStatus(), Unpooled.EMPTY_BUFFER, false);
/* 206:    */       
/* 207:268 */       fullMsg.setDecoderResult(res.getDecoderResult());
/* 208:    */     }
/* 209:    */     else
/* 210:    */     {
/* 211:270 */       throw new IllegalStateException();
/* 212:    */     }
/* 213:    */     FullHttpMessage fullMsg;
/* 214:273 */     return fullMsg;
/* 215:    */   }
/* 216:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpObjectAggregator
 * JD-Core Version:    0.7.0.1
 */