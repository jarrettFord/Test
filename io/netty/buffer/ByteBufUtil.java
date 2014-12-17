/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.CharsetUtil;
/*   4:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   5:    */ import io.netty.util.internal.logging.InternalLogger;
/*   6:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   7:    */ import java.nio.ByteBuffer;
/*   8:    */ import java.nio.ByteOrder;
/*   9:    */ import java.nio.CharBuffer;
/*  10:    */ import java.nio.charset.CharacterCodingException;
/*  11:    */ import java.nio.charset.Charset;
/*  12:    */ import java.nio.charset.CharsetDecoder;
/*  13:    */ import java.nio.charset.CharsetEncoder;
/*  14:    */ import java.nio.charset.CoderResult;
/*  15:    */ import java.util.Locale;
/*  16:    */ 
/*  17:    */ public final class ByteBufUtil
/*  18:    */ {
/*  19: 38 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ByteBufUtil.class);
/*  20: 40 */   private static final char[] HEXDUMP_TABLE = new char[1024];
/*  21:    */   static final ByteBufAllocator DEFAULT_ALLOCATOR;
/*  22:    */   
/*  23:    */   static
/*  24:    */   {
/*  25: 45 */     char[] DIGITS = "0123456789abcdef".toCharArray();
/*  26: 46 */     for (int i = 0; i < 256; i++)
/*  27:    */     {
/*  28: 47 */       HEXDUMP_TABLE[(i << 1)] = DIGITS[(i >>> 4 & 0xF)];
/*  29: 48 */       HEXDUMP_TABLE[((i << 1) + 1)] = DIGITS[(i & 0xF)];
/*  30:    */     }
/*  31: 51 */     String allocType = SystemPropertyUtil.get("io.netty.allocator.type", "unpooled").toLowerCase(Locale.US).trim();
/*  32:    */     ByteBufAllocator alloc;
/*  33: 53 */     if ("unpooled".equals(allocType))
/*  34:    */     {
/*  35: 54 */       ByteBufAllocator alloc = UnpooledByteBufAllocator.DEFAULT;
/*  36: 55 */       logger.debug("-Dio.netty.allocator.type: {}", allocType);
/*  37:    */     }
/*  38: 56 */     else if ("pooled".equals(allocType))
/*  39:    */     {
/*  40: 57 */       ByteBufAllocator alloc = PooledByteBufAllocator.DEFAULT;
/*  41: 58 */       logger.debug("-Dio.netty.allocator.type: {}", allocType);
/*  42:    */     }
/*  43:    */     else
/*  44:    */     {
/*  45: 60 */       alloc = UnpooledByteBufAllocator.DEFAULT;
/*  46: 61 */       logger.debug("-Dio.netty.allocator.type: unpooled (unknown: {})", allocType);
/*  47:    */     }
/*  48: 64 */     DEFAULT_ALLOCATOR = alloc;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public static String hexDump(ByteBuf buffer)
/*  52:    */   {
/*  53: 72 */     return hexDump(buffer, buffer.readerIndex(), buffer.readableBytes());
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static String hexDump(ByteBuf buffer, int fromIndex, int length)
/*  57:    */   {
/*  58: 80 */     if (length < 0) {
/*  59: 81 */       throw new IllegalArgumentException("length: " + length);
/*  60:    */     }
/*  61: 83 */     if (length == 0) {
/*  62: 84 */       return "";
/*  63:    */     }
/*  64: 87 */     int endIndex = fromIndex + length;
/*  65: 88 */     char[] buf = new char[length << 1];
/*  66:    */     
/*  67: 90 */     int srcIdx = fromIndex;
/*  68: 91 */     for (int dstIdx = 0; srcIdx < endIndex; dstIdx += 2)
/*  69:    */     {
/*  70: 93 */       System.arraycopy(HEXDUMP_TABLE, buffer.getUnsignedByte(srcIdx) << 1, buf, dstIdx, 2);srcIdx++;
/*  71:    */     }
/*  72: 98 */     return new String(buf);
/*  73:    */   }
/*  74:    */   
/*  75:    */   public static int hashCode(ByteBuf buffer)
/*  76:    */   {
/*  77:106 */     int aLen = buffer.readableBytes();
/*  78:107 */     int intCount = aLen >>> 2;
/*  79:108 */     int byteCount = aLen & 0x3;
/*  80:    */     
/*  81:110 */     int hashCode = 1;
/*  82:111 */     int arrayIndex = buffer.readerIndex();
/*  83:112 */     if (buffer.order() == ByteOrder.BIG_ENDIAN) {
/*  84:113 */       for (int i = intCount; i > 0; i--)
/*  85:    */       {
/*  86:114 */         hashCode = 31 * hashCode + buffer.getInt(arrayIndex);
/*  87:115 */         arrayIndex += 4;
/*  88:    */       }
/*  89:    */     } else {
/*  90:118 */       for (int i = intCount; i > 0; i--)
/*  91:    */       {
/*  92:119 */         hashCode = 31 * hashCode + swapInt(buffer.getInt(arrayIndex));
/*  93:120 */         arrayIndex += 4;
/*  94:    */       }
/*  95:    */     }
/*  96:124 */     for (int i = byteCount; i > 0; i--) {
/*  97:125 */       hashCode = 31 * hashCode + buffer.getByte(arrayIndex++);
/*  98:    */     }
/*  99:128 */     if (hashCode == 0) {
/* 100:129 */       hashCode = 1;
/* 101:    */     }
/* 102:132 */     return hashCode;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public static boolean equals(ByteBuf bufferA, ByteBuf bufferB)
/* 106:    */   {
/* 107:141 */     int aLen = bufferA.readableBytes();
/* 108:142 */     if (aLen != bufferB.readableBytes()) {
/* 109:143 */       return false;
/* 110:    */     }
/* 111:146 */     int longCount = aLen >>> 3;
/* 112:147 */     int byteCount = aLen & 0x7;
/* 113:    */     
/* 114:149 */     int aIndex = bufferA.readerIndex();
/* 115:150 */     int bIndex = bufferB.readerIndex();
/* 116:152 */     if (bufferA.order() == bufferB.order()) {
/* 117:153 */       for (int i = longCount; i > 0; i--)
/* 118:    */       {
/* 119:154 */         if (bufferA.getLong(aIndex) != bufferB.getLong(bIndex)) {
/* 120:155 */           return false;
/* 121:    */         }
/* 122:157 */         aIndex += 8;
/* 123:158 */         bIndex += 8;
/* 124:    */       }
/* 125:    */     } else {
/* 126:161 */       for (int i = longCount; i > 0; i--)
/* 127:    */       {
/* 128:162 */         if (bufferA.getLong(aIndex) != swapLong(bufferB.getLong(bIndex))) {
/* 129:163 */           return false;
/* 130:    */         }
/* 131:165 */         aIndex += 8;
/* 132:166 */         bIndex += 8;
/* 133:    */       }
/* 134:    */     }
/* 135:170 */     for (int i = byteCount; i > 0; i--)
/* 136:    */     {
/* 137:171 */       if (bufferA.getByte(aIndex) != bufferB.getByte(bIndex)) {
/* 138:172 */         return false;
/* 139:    */       }
/* 140:174 */       aIndex++;
/* 141:175 */       bIndex++;
/* 142:    */     }
/* 143:178 */     return true;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public static int compare(ByteBuf bufferA, ByteBuf bufferB)
/* 147:    */   {
/* 148:186 */     int aLen = bufferA.readableBytes();
/* 149:187 */     int bLen = bufferB.readableBytes();
/* 150:188 */     int minLength = Math.min(aLen, bLen);
/* 151:189 */     int uintCount = minLength >>> 2;
/* 152:190 */     int byteCount = minLength & 0x3;
/* 153:    */     
/* 154:192 */     int aIndex = bufferA.readerIndex();
/* 155:193 */     int bIndex = bufferB.readerIndex();
/* 156:195 */     if (bufferA.order() == bufferB.order()) {
/* 157:196 */       for (int i = uintCount; i > 0; i--)
/* 158:    */       {
/* 159:197 */         long va = bufferA.getUnsignedInt(aIndex);
/* 160:198 */         long vb = bufferB.getUnsignedInt(bIndex);
/* 161:199 */         if (va > vb) {
/* 162:200 */           return 1;
/* 163:    */         }
/* 164:202 */         if (va < vb) {
/* 165:203 */           return -1;
/* 166:    */         }
/* 167:205 */         aIndex += 4;
/* 168:206 */         bIndex += 4;
/* 169:    */       }
/* 170:    */     } else {
/* 171:209 */       for (int i = uintCount; i > 0; i--)
/* 172:    */       {
/* 173:210 */         long va = bufferA.getUnsignedInt(aIndex);
/* 174:211 */         long vb = swapInt(bufferB.getInt(bIndex)) & 0xFFFFFFFF;
/* 175:212 */         if (va > vb) {
/* 176:213 */           return 1;
/* 177:    */         }
/* 178:215 */         if (va < vb) {
/* 179:216 */           return -1;
/* 180:    */         }
/* 181:218 */         aIndex += 4;
/* 182:219 */         bIndex += 4;
/* 183:    */       }
/* 184:    */     }
/* 185:223 */     for (int i = byteCount; i > 0; i--)
/* 186:    */     {
/* 187:224 */       short va = bufferA.getUnsignedByte(aIndex);
/* 188:225 */       short vb = bufferB.getUnsignedByte(bIndex);
/* 189:226 */       if (va > vb) {
/* 190:227 */         return 1;
/* 191:    */       }
/* 192:229 */       if (va < vb) {
/* 193:230 */         return -1;
/* 194:    */       }
/* 195:232 */       aIndex++;
/* 196:233 */       bIndex++;
/* 197:    */     }
/* 198:236 */     return aLen - bLen;
/* 199:    */   }
/* 200:    */   
/* 201:    */   public static int indexOf(ByteBuf buffer, int fromIndex, int toIndex, byte value)
/* 202:    */   {
/* 203:244 */     if (fromIndex <= toIndex) {
/* 204:245 */       return firstIndexOf(buffer, fromIndex, toIndex, value);
/* 205:    */     }
/* 206:247 */     return lastIndexOf(buffer, fromIndex, toIndex, value);
/* 207:    */   }
/* 208:    */   
/* 209:    */   public static short swapShort(short value)
/* 210:    */   {
/* 211:255 */     return Short.reverseBytes(value);
/* 212:    */   }
/* 213:    */   
/* 214:    */   public static int swapMedium(int value)
/* 215:    */   {
/* 216:262 */     int swapped = value << 16 & 0xFF0000 | value & 0xFF00 | value >>> 16 & 0xFF;
/* 217:263 */     if ((swapped & 0x800000) != 0) {
/* 218:264 */       swapped |= 0xFF000000;
/* 219:    */     }
/* 220:266 */     return swapped;
/* 221:    */   }
/* 222:    */   
/* 223:    */   public static int swapInt(int value)
/* 224:    */   {
/* 225:273 */     return Integer.reverseBytes(value);
/* 226:    */   }
/* 227:    */   
/* 228:    */   public static long swapLong(long value)
/* 229:    */   {
/* 230:280 */     return Long.reverseBytes(value);
/* 231:    */   }
/* 232:    */   
/* 233:    */   public static ByteBuf readBytes(ByteBufAllocator alloc, ByteBuf buffer, int length)
/* 234:    */   {
/* 235:287 */     boolean release = true;
/* 236:288 */     ByteBuf dst = alloc.buffer(length);
/* 237:    */     try
/* 238:    */     {
/* 239:290 */       buffer.readBytes(dst);
/* 240:291 */       release = false;
/* 241:292 */       return dst;
/* 242:    */     }
/* 243:    */     finally
/* 244:    */     {
/* 245:294 */       if (release) {
/* 246:295 */         dst.release();
/* 247:    */       }
/* 248:    */     }
/* 249:    */   }
/* 250:    */   
/* 251:    */   private static int firstIndexOf(ByteBuf buffer, int fromIndex, int toIndex, byte value)
/* 252:    */   {
/* 253:301 */     fromIndex = Math.max(fromIndex, 0);
/* 254:302 */     if ((fromIndex >= toIndex) || (buffer.capacity() == 0)) {
/* 255:303 */       return -1;
/* 256:    */     }
/* 257:306 */     for (int i = fromIndex; i < toIndex; i++) {
/* 258:307 */       if (buffer.getByte(i) == value) {
/* 259:308 */         return i;
/* 260:    */       }
/* 261:    */     }
/* 262:312 */     return -1;
/* 263:    */   }
/* 264:    */   
/* 265:    */   private static int lastIndexOf(ByteBuf buffer, int fromIndex, int toIndex, byte value)
/* 266:    */   {
/* 267:316 */     fromIndex = Math.min(fromIndex, buffer.capacity());
/* 268:317 */     if ((fromIndex < 0) || (buffer.capacity() == 0)) {
/* 269:318 */       return -1;
/* 270:    */     }
/* 271:321 */     for (int i = fromIndex - 1; i >= toIndex; i--) {
/* 272:322 */       if (buffer.getByte(i) == value) {
/* 273:323 */         return i;
/* 274:    */       }
/* 275:    */     }
/* 276:327 */     return -1;
/* 277:    */   }
/* 278:    */   
/* 279:    */   public static ByteBuf encodeString(ByteBufAllocator alloc, CharBuffer src, Charset charset)
/* 280:    */   {
/* 281:335 */     CharsetEncoder encoder = CharsetUtil.getEncoder(charset);
/* 282:336 */     int length = (int)(src.remaining() * encoder.maxBytesPerChar());
/* 283:337 */     boolean release = true;
/* 284:338 */     ByteBuf dst = alloc.buffer(length);
/* 285:    */     try
/* 286:    */     {
/* 287:340 */       ByteBuffer dstBuf = dst.internalNioBuffer(0, length);
/* 288:341 */       int pos = dstBuf.position();
/* 289:342 */       CoderResult cr = encoder.encode(src, dstBuf, true);
/* 290:343 */       if (!cr.isUnderflow()) {
/* 291:344 */         cr.throwException();
/* 292:    */       }
/* 293:346 */       cr = encoder.flush(dstBuf);
/* 294:347 */       if (!cr.isUnderflow()) {
/* 295:348 */         cr.throwException();
/* 296:    */       }
/* 297:350 */       dst.writerIndex(dst.writerIndex() + dstBuf.position() - pos);
/* 298:351 */       release = false;
/* 299:352 */       return dst;
/* 300:    */     }
/* 301:    */     catch (CharacterCodingException x)
/* 302:    */     {
/* 303:354 */       throw new IllegalStateException(x);
/* 304:    */     }
/* 305:    */     finally
/* 306:    */     {
/* 307:356 */       if (release) {
/* 308:357 */         dst.release();
/* 309:    */       }
/* 310:    */     }
/* 311:    */   }
/* 312:    */   
/* 313:    */   static String decodeString(ByteBuffer src, Charset charset)
/* 314:    */   {
/* 315:363 */     CharsetDecoder decoder = CharsetUtil.getDecoder(charset);
/* 316:364 */     CharBuffer dst = CharBuffer.allocate((int)(src.remaining() * decoder.maxCharsPerByte()));
/* 317:    */     try
/* 318:    */     {
/* 319:367 */       CoderResult cr = decoder.decode(src, dst, true);
/* 320:368 */       if (!cr.isUnderflow()) {
/* 321:369 */         cr.throwException();
/* 322:    */       }
/* 323:371 */       cr = decoder.flush(dst);
/* 324:372 */       if (!cr.isUnderflow()) {
/* 325:373 */         cr.throwException();
/* 326:    */       }
/* 327:    */     }
/* 328:    */     catch (CharacterCodingException x)
/* 329:    */     {
/* 330:376 */       throw new IllegalStateException(x);
/* 331:    */     }
/* 332:378 */     return dst.flip().toString();
/* 333:    */   }
/* 334:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.ByteBufUtil
 * JD-Core Version:    0.7.0.1
 */