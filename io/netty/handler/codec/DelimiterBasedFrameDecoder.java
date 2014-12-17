/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import java.util.List;
/*   6:    */ 
/*   7:    */ public class DelimiterBasedFrameDecoder
/*   8:    */   extends ByteToMessageDecoder
/*   9:    */ {
/*  10:    */   private final ByteBuf[] delimiters;
/*  11:    */   private final int maxFrameLength;
/*  12:    */   private final boolean stripDelimiter;
/*  13:    */   private final boolean failFast;
/*  14:    */   private boolean discardingTooLongFrame;
/*  15:    */   private int tooLongFrameLength;
/*  16:    */   private final LineBasedFrameDecoder lineBasedDecoder;
/*  17:    */   
/*  18:    */   public DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf delimiter)
/*  19:    */   {
/*  20: 78 */     this(maxFrameLength, true, delimiter);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf delimiter)
/*  24:    */   {
/*  25: 93 */     this(maxFrameLength, stripDelimiter, true, delimiter);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, boolean failFast, ByteBuf delimiter)
/*  29:    */   {
/*  30:116 */     this(maxFrameLength, stripDelimiter, failFast, new ByteBuf[] { delimiter.slice(delimiter.readerIndex(), delimiter.readableBytes()) });
/*  31:    */   }
/*  32:    */   
/*  33:    */   public DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf... delimiters)
/*  34:    */   {
/*  35:129 */     this(maxFrameLength, true, delimiters);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf... delimiters)
/*  39:    */   {
/*  40:144 */     this(maxFrameLength, stripDelimiter, true, delimiters);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, boolean failFast, ByteBuf... delimiters)
/*  44:    */   {
/*  45:166 */     validateMaxFrameLength(maxFrameLength);
/*  46:167 */     if (delimiters == null) {
/*  47:168 */       throw new NullPointerException("delimiters");
/*  48:    */     }
/*  49:170 */     if (delimiters.length == 0) {
/*  50:171 */       throw new IllegalArgumentException("empty delimiters");
/*  51:    */     }
/*  52:174 */     if ((isLineBased(delimiters)) && (!isSubclass()))
/*  53:    */     {
/*  54:175 */       this.lineBasedDecoder = new LineBasedFrameDecoder(maxFrameLength, stripDelimiter, failFast);
/*  55:176 */       this.delimiters = null;
/*  56:    */     }
/*  57:    */     else
/*  58:    */     {
/*  59:178 */       this.delimiters = new ByteBuf[delimiters.length];
/*  60:179 */       for (int i = 0; i < delimiters.length; i++)
/*  61:    */       {
/*  62:180 */         ByteBuf d = delimiters[i];
/*  63:181 */         validateDelimiter(d);
/*  64:182 */         this.delimiters[i] = d.slice(d.readerIndex(), d.readableBytes());
/*  65:    */       }
/*  66:184 */       this.lineBasedDecoder = null;
/*  67:    */     }
/*  68:186 */     this.maxFrameLength = maxFrameLength;
/*  69:187 */     this.stripDelimiter = stripDelimiter;
/*  70:188 */     this.failFast = failFast;
/*  71:    */   }
/*  72:    */   
/*  73:    */   private static boolean isLineBased(ByteBuf[] delimiters)
/*  74:    */   {
/*  75:193 */     if (delimiters.length != 2) {
/*  76:194 */       return false;
/*  77:    */     }
/*  78:196 */     ByteBuf a = delimiters[0];
/*  79:197 */     ByteBuf b = delimiters[1];
/*  80:198 */     if (a.capacity() < b.capacity())
/*  81:    */     {
/*  82:199 */       a = delimiters[1];
/*  83:200 */       b = delimiters[0];
/*  84:    */     }
/*  85:202 */     return (a.capacity() == 2) && (b.capacity() == 1) && (a.getByte(0) == 13) && (a.getByte(1) == 10) && (b.getByte(0) == 10);
/*  86:    */   }
/*  87:    */   
/*  88:    */   private boolean isSubclass()
/*  89:    */   {
/*  90:211 */     return getClass() != DelimiterBasedFrameDecoder.class;
/*  91:    */   }
/*  92:    */   
/*  93:    */   protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  94:    */     throws Exception
/*  95:    */   {
/*  96:216 */     Object decoded = decode(ctx, in);
/*  97:217 */     if (decoded != null) {
/*  98:218 */       out.add(decoded);
/*  99:    */     }
/* 100:    */   }
/* 101:    */   
/* 102:    */   protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer)
/* 103:    */     throws Exception
/* 104:    */   {
/* 105:231 */     if (this.lineBasedDecoder != null) {
/* 106:232 */       return this.lineBasedDecoder.decode(ctx, buffer);
/* 107:    */     }
/* 108:235 */     int minFrameLength = 2147483647;
/* 109:236 */     ByteBuf minDelim = null;
/* 110:237 */     for (ByteBuf delim : this.delimiters)
/* 111:    */     {
/* 112:238 */       int frameLength = indexOf(buffer, delim);
/* 113:239 */       if ((frameLength >= 0) && (frameLength < minFrameLength))
/* 114:    */       {
/* 115:240 */         minFrameLength = frameLength;
/* 116:241 */         minDelim = delim;
/* 117:    */       }
/* 118:    */     }
/* 119:245 */     if (minDelim != null)
/* 120:    */     {
/* 121:246 */       int minDelimLength = minDelim.capacity();
/* 122:249 */       if (this.discardingTooLongFrame)
/* 123:    */       {
/* 124:252 */         this.discardingTooLongFrame = false;
/* 125:253 */         buffer.skipBytes(minFrameLength + minDelimLength);
/* 126:    */         
/* 127:255 */         int tooLongFrameLength = this.tooLongFrameLength;
/* 128:256 */         this.tooLongFrameLength = 0;
/* 129:257 */         if (!this.failFast) {
/* 130:258 */           fail(ctx, tooLongFrameLength);
/* 131:    */         }
/* 132:260 */         return null;
/* 133:    */       }
/* 134:263 */       if (minFrameLength > this.maxFrameLength)
/* 135:    */       {
/* 136:265 */         buffer.skipBytes(minFrameLength + minDelimLength);
/* 137:266 */         fail(ctx, minFrameLength);
/* 138:267 */         return null;
/* 139:    */       }
/* 140:    */       ByteBuf frame;
/* 141:270 */       if (this.stripDelimiter)
/* 142:    */       {
/* 143:271 */         ByteBuf frame = buffer.readSlice(minFrameLength);
/* 144:272 */         buffer.skipBytes(minDelimLength);
/* 145:    */       }
/* 146:    */       else
/* 147:    */       {
/* 148:274 */         frame = buffer.readSlice(minFrameLength + minDelimLength);
/* 149:    */       }
/* 150:277 */       return frame.retain();
/* 151:    */     }
/* 152:279 */     if (!this.discardingTooLongFrame)
/* 153:    */     {
/* 154:280 */       if (buffer.readableBytes() > this.maxFrameLength)
/* 155:    */       {
/* 156:282 */         this.tooLongFrameLength = buffer.readableBytes();
/* 157:283 */         buffer.skipBytes(buffer.readableBytes());
/* 158:284 */         this.discardingTooLongFrame = true;
/* 159:285 */         if (this.failFast) {
/* 160:286 */           fail(ctx, this.tooLongFrameLength);
/* 161:    */         }
/* 162:    */       }
/* 163:    */     }
/* 164:    */     else
/* 165:    */     {
/* 166:291 */       this.tooLongFrameLength += buffer.readableBytes();
/* 167:292 */       buffer.skipBytes(buffer.readableBytes());
/* 168:    */     }
/* 169:294 */     return null;
/* 170:    */   }
/* 171:    */   
/* 172:    */   private void fail(ChannelHandlerContext ctx, long frameLength)
/* 173:    */   {
/* 174:299 */     if (frameLength > 0L) {
/* 175:300 */       ctx.fireExceptionCaught(new TooLongFrameException("frame length exceeds " + this.maxFrameLength + ": " + frameLength + " - discarded"));
/* 176:    */     } else {
/* 177:305 */       ctx.fireExceptionCaught(new TooLongFrameException("frame length exceeds " + this.maxFrameLength + " - discarding"));
/* 178:    */     }
/* 179:    */   }
/* 180:    */   
/* 181:    */   private static int indexOf(ByteBuf haystack, ByteBuf needle)
/* 182:    */   {
/* 183:318 */     for (int i = haystack.readerIndex(); i < haystack.writerIndex(); i++)
/* 184:    */     {
/* 185:319 */       int haystackIndex = i;
/* 186:321 */       for (int needleIndex = 0; needleIndex < needle.capacity(); needleIndex++)
/* 187:    */       {
/* 188:322 */         if (haystack.getByte(haystackIndex) != needle.getByte(needleIndex)) {
/* 189:    */           break;
/* 190:    */         }
/* 191:325 */         haystackIndex++;
/* 192:326 */         if ((haystackIndex == haystack.writerIndex()) && (needleIndex != needle.capacity() - 1)) {
/* 193:328 */           return -1;
/* 194:    */         }
/* 195:    */       }
/* 196:333 */       if (needleIndex == needle.capacity()) {
/* 197:335 */         return i - haystack.readerIndex();
/* 198:    */       }
/* 199:    */     }
/* 200:338 */     return -1;
/* 201:    */   }
/* 202:    */   
/* 203:    */   private static void validateDelimiter(ByteBuf delimiter)
/* 204:    */   {
/* 205:342 */     if (delimiter == null) {
/* 206:343 */       throw new NullPointerException("delimiter");
/* 207:    */     }
/* 208:345 */     if (!delimiter.isReadable()) {
/* 209:346 */       throw new IllegalArgumentException("empty delimiter");
/* 210:    */     }
/* 211:    */   }
/* 212:    */   
/* 213:    */   private static void validateMaxFrameLength(int maxFrameLength)
/* 214:    */   {
/* 215:351 */     if (maxFrameLength <= 0) {
/* 216:352 */       throw new IllegalArgumentException("maxFrameLength must be a positive integer: " + maxFrameLength);
/* 217:    */     }
/* 218:    */   }
/* 219:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.DelimiterBasedFrameDecoder
 * JD-Core Version:    0.7.0.1
 */