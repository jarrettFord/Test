/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.channel.CombinedChannelDuplexHandler;
/*   6:    */ import io.netty.handler.codec.PrematureChannelClosureException;
/*   7:    */ import java.util.ArrayDeque;
/*   8:    */ import java.util.List;
/*   9:    */ import java.util.Queue;
/*  10:    */ import java.util.concurrent.atomic.AtomicLong;
/*  11:    */ 
/*  12:    */ public final class HttpClientCodec
/*  13:    */   extends CombinedChannelDuplexHandler<HttpResponseDecoder, HttpRequestEncoder>
/*  14:    */ {
/*  15: 47 */   private final Queue<HttpMethod> queue = new ArrayDeque();
/*  16:    */   private boolean done;
/*  17: 52 */   private final AtomicLong requestResponseCounter = new AtomicLong();
/*  18:    */   private final boolean failOnMissingResponse;
/*  19:    */   
/*  20:    */   public HttpClientCodec()
/*  21:    */   {
/*  22: 61 */     this(4096, 8192, 8192, false);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public void setSingleDecode(boolean singleDecode)
/*  26:    */   {
/*  27: 65 */     ((HttpResponseDecoder)inboundHandler()).setSingleDecode(singleDecode);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public boolean isSingleDecode()
/*  31:    */   {
/*  32: 69 */     return ((HttpResponseDecoder)inboundHandler()).isSingleDecode();
/*  33:    */   }
/*  34:    */   
/*  35:    */   public HttpClientCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize)
/*  36:    */   {
/*  37: 76 */     this(maxInitialLineLength, maxHeaderSize, maxChunkSize, false);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public HttpClientCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse)
/*  41:    */   {
/*  42: 84 */     this(maxInitialLineLength, maxHeaderSize, maxChunkSize, failOnMissingResponse, true);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public HttpClientCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse, boolean validateHeaders)
/*  46:    */   {
/*  47: 93 */     init(new Decoder(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders), new Encoder(null));
/*  48: 94 */     this.failOnMissingResponse = failOnMissingResponse;
/*  49:    */   }
/*  50:    */   
/*  51:    */   private final class Encoder
/*  52:    */     extends HttpRequestEncoder
/*  53:    */   {
/*  54:    */     private Encoder() {}
/*  55:    */     
/*  56:    */     protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out)
/*  57:    */       throws Exception
/*  58:    */     {
/*  59:102 */       if (((msg instanceof HttpRequest)) && (!HttpClientCodec.this.done)) {
/*  60:103 */         HttpClientCodec.this.queue.offer(((HttpRequest)msg).getMethod());
/*  61:    */       }
/*  62:106 */       super.encode(ctx, msg, out);
/*  63:108 */       if (HttpClientCodec.this.failOnMissingResponse) {
/*  64:110 */         if ((msg instanceof LastHttpContent)) {
/*  65:112 */           HttpClientCodec.this.requestResponseCounter.incrementAndGet();
/*  66:    */         }
/*  67:    */       }
/*  68:    */     }
/*  69:    */   }
/*  70:    */   
/*  71:    */   private final class Decoder
/*  72:    */     extends HttpResponseDecoder
/*  73:    */   {
/*  74:    */     Decoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders)
/*  75:    */     {
/*  76:120 */       super(maxHeaderSize, maxChunkSize, validateHeaders);
/*  77:    */     }
/*  78:    */     
/*  79:    */     protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out)
/*  80:    */       throws Exception
/*  81:    */     {
/*  82:126 */       if (HttpClientCodec.this.done)
/*  83:    */       {
/*  84:127 */         int readable = actualReadableBytes();
/*  85:128 */         if (readable == 0) {
/*  86:131 */           return;
/*  87:    */         }
/*  88:133 */         out.add(buffer.readBytes(readable));
/*  89:    */       }
/*  90:    */       else
/*  91:    */       {
/*  92:135 */         int oldSize = out.size();
/*  93:136 */         super.decode(ctx, buffer, out);
/*  94:137 */         if (HttpClientCodec.this.failOnMissingResponse)
/*  95:    */         {
/*  96:138 */           int size = out.size();
/*  97:139 */           for (int i = oldSize; i < size; i++) {
/*  98:140 */             decrement(out.get(i));
/*  99:    */           }
/* 100:    */         }
/* 101:    */       }
/* 102:    */     }
/* 103:    */     
/* 104:    */     private void decrement(Object msg)
/* 105:    */     {
/* 106:147 */       if (msg == null) {
/* 107:148 */         return;
/* 108:    */       }
/* 109:152 */       if ((msg instanceof LastHttpContent)) {
/* 110:153 */         HttpClientCodec.this.requestResponseCounter.decrementAndGet();
/* 111:    */       }
/* 112:    */     }
/* 113:    */     
/* 114:    */     protected boolean isContentAlwaysEmpty(HttpMessage msg)
/* 115:    */     {
/* 116:159 */       int statusCode = ((HttpResponse)msg).getStatus().code();
/* 117:160 */       if (statusCode == 100) {
/* 118:162 */         return true;
/* 119:    */       }
/* 120:167 */       HttpMethod method = (HttpMethod)HttpClientCodec.this.queue.poll();
/* 121:    */       
/* 122:169 */       char firstChar = method.name().charAt(0);
/* 123:170 */       switch (firstChar)
/* 124:    */       {
/* 125:    */       case 'H': 
/* 126:176 */         if (HttpMethod.HEAD.equals(method)) {
/* 127:177 */           return true;
/* 128:    */         }
/* 129:    */         break;
/* 130:    */       case 'C': 
/* 131:195 */         if ((statusCode == 200) && 
/* 132:196 */           (HttpMethod.CONNECT.equals(method)))
/* 133:    */         {
/* 134:198 */           HttpClientCodec.this.done = true;
/* 135:199 */           HttpClientCodec.this.queue.clear();
/* 136:200 */           return true;
/* 137:    */         }
/* 138:    */         break;
/* 139:    */       }
/* 140:206 */       return super.isContentAlwaysEmpty(msg);
/* 141:    */     }
/* 142:    */     
/* 143:    */     public void channelInactive(ChannelHandlerContext ctx)
/* 144:    */       throws Exception
/* 145:    */     {
/* 146:212 */       super.channelInactive(ctx);
/* 147:214 */       if (HttpClientCodec.this.failOnMissingResponse)
/* 148:    */       {
/* 149:215 */         long missingResponses = HttpClientCodec.this.requestResponseCounter.get();
/* 150:216 */         if (missingResponses > 0L) {
/* 151:217 */           ctx.fireExceptionCaught(new PrematureChannelClosureException("channel gone inactive with " + missingResponses + " missing response(s)"));
/* 152:    */         }
/* 153:    */       }
/* 154:    */     }
/* 155:    */   }
/* 156:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpClientCodec
 * JD-Core Version:    0.7.0.1
 */