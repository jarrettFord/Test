/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.channel.embedded.EmbeddedChannel;
/*   6:    */ import io.netty.handler.codec.MessageToMessageDecoder;
/*   7:    */ import io.netty.util.ReferenceCountUtil;
/*   8:    */ import java.util.List;
/*   9:    */ 
/*  10:    */ public abstract class HttpContentDecoder
/*  11:    */   extends MessageToMessageDecoder<HttpObject>
/*  12:    */ {
/*  13:    */   private EmbeddedChannel decoder;
/*  14:    */   private HttpMessage message;
/*  15:    */   private boolean decodeStarted;
/*  16:    */   private boolean continueResponse;
/*  17:    */   
/*  18:    */   protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out)
/*  19:    */     throws Exception
/*  20:    */   {
/*  21: 54 */     if (((msg instanceof HttpResponse)) && (((HttpResponse)msg).getStatus().code() == 100))
/*  22:    */     {
/*  23: 56 */       if (!(msg instanceof LastHttpContent)) {
/*  24: 57 */         this.continueResponse = true;
/*  25:    */       }
/*  26: 60 */       out.add(ReferenceCountUtil.retain(msg));
/*  27: 61 */       return;
/*  28:    */     }
/*  29: 64 */     if (this.continueResponse)
/*  30:    */     {
/*  31: 65 */       if ((msg instanceof LastHttpContent)) {
/*  32: 66 */         this.continueResponse = false;
/*  33:    */       }
/*  34: 69 */       out.add(ReferenceCountUtil.retain(msg));
/*  35: 70 */       return;
/*  36:    */     }
/*  37: 73 */     if ((msg instanceof HttpMessage))
/*  38:    */     {
/*  39: 74 */       assert (this.message == null);
/*  40: 75 */       this.message = ((HttpMessage)msg);
/*  41: 76 */       this.decodeStarted = false;
/*  42: 77 */       cleanup();
/*  43:    */     }
/*  44: 80 */     if ((msg instanceof HttpContent))
/*  45:    */     {
/*  46: 81 */       HttpContent c = (HttpContent)msg;
/*  47: 83 */       if (!this.decodeStarted)
/*  48:    */       {
/*  49: 84 */         this.decodeStarted = true;
/*  50: 85 */         HttpMessage message = this.message;
/*  51: 86 */         HttpHeaders headers = message.headers();
/*  52: 87 */         this.message = null;
/*  53:    */         
/*  54:    */ 
/*  55: 90 */         String contentEncoding = headers.get("Content-Encoding");
/*  56: 91 */         if (contentEncoding != null) {
/*  57: 92 */           contentEncoding = contentEncoding.trim();
/*  58:    */         } else {
/*  59: 94 */           contentEncoding = "identity";
/*  60:    */         }
/*  61: 97 */         if ((this.decoder = newContentDecoder(contentEncoding)) != null)
/*  62:    */         {
/*  63:100 */           String targetContentEncoding = getTargetContentEncoding(contentEncoding);
/*  64:101 */           if ("identity".equals(targetContentEncoding)) {
/*  65:104 */             headers.remove("Content-Encoding");
/*  66:    */           } else {
/*  67:106 */             headers.set("Content-Encoding", targetContentEncoding);
/*  68:    */           }
/*  69:109 */           out.add(message);
/*  70:110 */           decodeContent(c, out);
/*  71:113 */           if (headers.contains("Content-Length"))
/*  72:    */           {
/*  73:114 */             int contentLength = 0;
/*  74:115 */             int size = out.size();
/*  75:116 */             for (int i = 0; i < size; i++)
/*  76:    */             {
/*  77:117 */               Object o = out.get(i);
/*  78:118 */               if ((o instanceof HttpContent)) {
/*  79:119 */                 contentLength += ((HttpContent)o).content().readableBytes();
/*  80:    */               }
/*  81:    */             }
/*  82:122 */             headers.set("Content-Length", Integer.toString(contentLength));
/*  83:    */           }
/*  84:126 */           return;
/*  85:    */         }
/*  86:129 */         if ((c instanceof LastHttpContent)) {
/*  87:130 */           this.decodeStarted = false;
/*  88:    */         }
/*  89:132 */         out.add(message);
/*  90:133 */         out.add(c.retain());
/*  91:134 */         return;
/*  92:    */       }
/*  93:137 */       if (this.decoder != null)
/*  94:    */       {
/*  95:138 */         decodeContent(c, out);
/*  96:    */       }
/*  97:    */       else
/*  98:    */       {
/*  99:140 */         if ((c instanceof LastHttpContent)) {
/* 100:141 */           this.decodeStarted = false;
/* 101:    */         }
/* 102:143 */         out.add(c.retain());
/* 103:    */       }
/* 104:    */     }
/* 105:    */   }
/* 106:    */   
/* 107:    */   private void decodeContent(HttpContent c, List<Object> out)
/* 108:    */   {
/* 109:149 */     ByteBuf content = c.content();
/* 110:    */     
/* 111:151 */     decode(content, out);
/* 112:153 */     if ((c instanceof LastHttpContent))
/* 113:    */     {
/* 114:154 */       finishDecode(out);
/* 115:    */       
/* 116:156 */       LastHttpContent last = (LastHttpContent)c;
/* 117:    */       
/* 118:    */ 
/* 119:159 */       HttpHeaders headers = last.trailingHeaders();
/* 120:160 */       if (headers.isEmpty()) {
/* 121:161 */         out.add(LastHttpContent.EMPTY_LAST_CONTENT);
/* 122:    */       } else {
/* 123:163 */         out.add(new ComposedLastHttpContent(headers));
/* 124:    */       }
/* 125:    */     }
/* 126:    */   }
/* 127:    */   
/* 128:    */   protected abstract EmbeddedChannel newContentDecoder(String paramString)
/* 129:    */     throws Exception;
/* 130:    */   
/* 131:    */   protected String getTargetContentEncoding(String contentEncoding)
/* 132:    */     throws Exception
/* 133:    */   {
/* 134:189 */     return "identity";
/* 135:    */   }
/* 136:    */   
/* 137:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 138:    */     throws Exception
/* 139:    */   {
/* 140:194 */     cleanup();
/* 141:195 */     super.handlerRemoved(ctx);
/* 142:    */   }
/* 143:    */   
/* 144:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 145:    */     throws Exception
/* 146:    */   {
/* 147:200 */     cleanup();
/* 148:201 */     super.channelInactive(ctx);
/* 149:    */   }
/* 150:    */   
/* 151:    */   private void cleanup()
/* 152:    */   {
/* 153:205 */     if (this.decoder != null)
/* 154:    */     {
/* 155:207 */       if (this.decoder.finish()) {
/* 156:    */         for (;;)
/* 157:    */         {
/* 158:209 */           ByteBuf buf = (ByteBuf)this.decoder.readOutbound();
/* 159:210 */           if (buf == null) {
/* 160:    */             break;
/* 161:    */           }
/* 162:214 */           buf.release();
/* 163:    */         }
/* 164:    */       }
/* 165:217 */       this.decoder = null;
/* 166:    */     }
/* 167:    */   }
/* 168:    */   
/* 169:    */   private void decode(ByteBuf in, List<Object> out)
/* 170:    */   {
/* 171:223 */     this.decoder.writeInbound(new Object[] { in.retain() });
/* 172:224 */     fetchDecoderOutput(out);
/* 173:    */   }
/* 174:    */   
/* 175:    */   private void finishDecode(List<Object> out)
/* 176:    */   {
/* 177:228 */     if (this.decoder.finish()) {
/* 178:229 */       fetchDecoderOutput(out);
/* 179:    */     }
/* 180:231 */     this.decodeStarted = false;
/* 181:232 */     this.decoder = null;
/* 182:    */   }
/* 183:    */   
/* 184:    */   private void fetchDecoderOutput(List<Object> out)
/* 185:    */   {
/* 186:    */     for (;;)
/* 187:    */     {
/* 188:237 */       ByteBuf buf = (ByteBuf)this.decoder.readInbound();
/* 189:238 */       if (buf == null) {
/* 190:    */         break;
/* 191:    */       }
/* 192:241 */       if (!buf.isReadable()) {
/* 193:242 */         buf.release();
/* 194:    */       } else {
/* 195:245 */         out.add(new DefaultHttpContent(buf));
/* 196:    */       }
/* 197:    */     }
/* 198:    */   }
/* 199:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpContentDecoder
 * JD-Core Version:    0.7.0.1
 */