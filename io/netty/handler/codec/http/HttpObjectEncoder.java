/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.channel.FileRegion;
/*   8:    */ import io.netty.handler.codec.MessageToMessageEncoder;
/*   9:    */ import io.netty.util.CharsetUtil;
/*  10:    */ import io.netty.util.internal.StringUtil;
/*  11:    */ import java.util.List;
/*  12:    */ 
/*  13:    */ public abstract class HttpObjectEncoder<H extends HttpMessage>
/*  14:    */   extends MessageToMessageEncoder<Object>
/*  15:    */ {
/*  16: 44 */   private static final byte[] CRLF = { 13, 10 };
/*  17: 45 */   private static final byte[] ZERO_CRLF = { 48, 13, 10 };
/*  18: 46 */   private static final byte[] ZERO_CRLF_CRLF = { 48, 13, 10, 13, 10 };
/*  19: 47 */   private static final ByteBuf CRLF_BUF = Unpooled.unreleasableBuffer(Unpooled.directBuffer(CRLF.length).writeBytes(CRLF));
/*  20: 48 */   private static final ByteBuf ZERO_CRLF_CRLF_BUF = Unpooled.unreleasableBuffer(Unpooled.directBuffer(ZERO_CRLF_CRLF.length).writeBytes(ZERO_CRLF_CRLF));
/*  21:    */   private static final int ST_INIT = 0;
/*  22:    */   private static final int ST_CONTENT_NON_CHUNK = 1;
/*  23:    */   private static final int ST_CONTENT_CHUNK = 2;
/*  24: 55 */   private int state = 0;
/*  25:    */   
/*  26:    */   protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out)
/*  27:    */     throws Exception
/*  28:    */   {
/*  29: 60 */     ByteBuf buf = null;
/*  30: 61 */     if ((msg instanceof HttpMessage))
/*  31:    */     {
/*  32: 62 */       if (this.state != 0) {
/*  33: 63 */         throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
/*  34:    */       }
/*  35: 67 */       H m = (HttpMessage)msg;
/*  36:    */       
/*  37: 69 */       buf = ctx.alloc().buffer();
/*  38:    */       
/*  39: 71 */       encodeInitialLine(buf, m);
/*  40: 72 */       HttpHeaders.encode(m.headers(), buf);
/*  41: 73 */       buf.writeBytes(CRLF);
/*  42: 74 */       this.state = (HttpHeaders.isTransferEncodingChunked(m) ? 2 : 1);
/*  43:    */     }
/*  44: 76 */     if (((msg instanceof HttpContent)) || ((msg instanceof ByteBuf)) || ((msg instanceof FileRegion)))
/*  45:    */     {
/*  46: 77 */       if (this.state == 0) {
/*  47: 78 */         throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
/*  48:    */       }
/*  49: 81 */       long contentLength = contentLength(msg);
/*  50: 82 */       if (this.state == 1)
/*  51:    */       {
/*  52: 83 */         if (contentLength > 0L)
/*  53:    */         {
/*  54: 84 */           if ((buf != null) && (buf.writableBytes() >= contentLength) && ((msg instanceof HttpContent)))
/*  55:    */           {
/*  56: 86 */             buf.writeBytes(((HttpContent)msg).content());
/*  57: 87 */             out.add(buf);
/*  58:    */           }
/*  59:    */           else
/*  60:    */           {
/*  61: 89 */             if (buf != null) {
/*  62: 90 */               out.add(buf);
/*  63:    */             }
/*  64: 92 */             out.add(encodeAndRetain(msg));
/*  65:    */           }
/*  66:    */         }
/*  67: 95 */         else if (buf != null) {
/*  68: 96 */           out.add(buf);
/*  69:    */         } else {
/*  70:100 */           out.add(Unpooled.EMPTY_BUFFER);
/*  71:    */         }
/*  72:104 */         if ((msg instanceof LastHttpContent)) {
/*  73:105 */           this.state = 0;
/*  74:    */         }
/*  75:    */       }
/*  76:107 */       else if (this.state == 2)
/*  77:    */       {
/*  78:108 */         if (buf != null) {
/*  79:109 */           out.add(buf);
/*  80:    */         }
/*  81:111 */         encodeChunkedContent(ctx, msg, contentLength, out);
/*  82:    */       }
/*  83:    */       else
/*  84:    */       {
/*  85:113 */         throw new Error();
/*  86:    */       }
/*  87:    */     }
/*  88:116 */     else if (buf != null)
/*  89:    */     {
/*  90:117 */       out.add(buf);
/*  91:    */     }
/*  92:    */   }
/*  93:    */   
/*  94:    */   private void encodeChunkedContent(ChannelHandlerContext ctx, Object msg, long contentLength, List<Object> out)
/*  95:    */   {
/*  96:123 */     if (contentLength > 0L)
/*  97:    */     {
/*  98:124 */       byte[] length = Long.toHexString(contentLength).getBytes(CharsetUtil.US_ASCII);
/*  99:125 */       ByteBuf buf = ctx.alloc().buffer(length.length + 2);
/* 100:126 */       buf.writeBytes(length);
/* 101:127 */       buf.writeBytes(CRLF);
/* 102:128 */       out.add(buf);
/* 103:129 */       out.add(encodeAndRetain(msg));
/* 104:130 */       out.add(CRLF_BUF.duplicate());
/* 105:    */     }
/* 106:133 */     if ((msg instanceof LastHttpContent))
/* 107:    */     {
/* 108:134 */       HttpHeaders headers = ((LastHttpContent)msg).trailingHeaders();
/* 109:135 */       if (headers.isEmpty())
/* 110:    */       {
/* 111:136 */         out.add(ZERO_CRLF_CRLF_BUF.duplicate());
/* 112:    */       }
/* 113:    */       else
/* 114:    */       {
/* 115:138 */         ByteBuf buf = ctx.alloc().buffer();
/* 116:139 */         buf.writeBytes(ZERO_CRLF);
/* 117:140 */         HttpHeaders.encode(headers, buf);
/* 118:141 */         buf.writeBytes(CRLF);
/* 119:142 */         out.add(buf);
/* 120:    */       }
/* 121:145 */       this.state = 0;
/* 122:    */     }
/* 123:147 */     else if (contentLength == 0L)
/* 124:    */     {
/* 125:150 */       out.add(Unpooled.EMPTY_BUFFER);
/* 126:    */     }
/* 127:    */   }
/* 128:    */   
/* 129:    */   public boolean acceptOutboundMessage(Object msg)
/* 130:    */     throws Exception
/* 131:    */   {
/* 132:157 */     return ((msg instanceof HttpObject)) || ((msg instanceof ByteBuf)) || ((msg instanceof FileRegion));
/* 133:    */   }
/* 134:    */   
/* 135:    */   private static Object encodeAndRetain(Object msg)
/* 136:    */   {
/* 137:161 */     if ((msg instanceof ByteBuf)) {
/* 138:162 */       return ((ByteBuf)msg).retain();
/* 139:    */     }
/* 140:164 */     if ((msg instanceof HttpContent)) {
/* 141:165 */       return ((HttpContent)msg).content().retain();
/* 142:    */     }
/* 143:167 */     if ((msg instanceof FileRegion)) {
/* 144:168 */       return ((FileRegion)msg).retain();
/* 145:    */     }
/* 146:170 */     throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
/* 147:    */   }
/* 148:    */   
/* 149:    */   private static long contentLength(Object msg)
/* 150:    */   {
/* 151:174 */     if ((msg instanceof HttpContent)) {
/* 152:175 */       return ((HttpContent)msg).content().readableBytes();
/* 153:    */     }
/* 154:177 */     if ((msg instanceof ByteBuf)) {
/* 155:178 */       return ((ByteBuf)msg).readableBytes();
/* 156:    */     }
/* 157:180 */     if ((msg instanceof FileRegion)) {
/* 158:181 */       return ((FileRegion)msg).count();
/* 159:    */     }
/* 160:183 */     throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
/* 161:    */   }
/* 162:    */   
/* 163:    */   @Deprecated
/* 164:    */   protected static void encodeAscii(String s, ByteBuf buf)
/* 165:    */   {
/* 166:188 */     HttpHeaders.encodeAscii0(s, buf);
/* 167:    */   }
/* 168:    */   
/* 169:    */   protected abstract void encodeInitialLine(ByteBuf paramByteBuf, H paramH)
/* 170:    */     throws Exception;
/* 171:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpObjectEncoder
 * JD-Core Version:    0.7.0.1
 */