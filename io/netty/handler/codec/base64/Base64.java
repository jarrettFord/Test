/*   1:    */ package io.netty.handler.codec.base64;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ 
/*   7:    */ public final class Base64
/*   8:    */ {
/*   9:    */   private static final int MAX_LINE_LENGTH = 76;
/*  10:    */   private static final byte EQUALS_SIGN = 61;
/*  11:    */   private static final byte NEW_LINE = 10;
/*  12:    */   private static final byte WHITE_SPACE_ENC = -5;
/*  13:    */   private static final byte EQUALS_SIGN_ENC = -1;
/*  14:    */   
/*  15:    */   private static byte[] alphabet(Base64Dialect dialect)
/*  16:    */   {
/*  17: 49 */     if (dialect == null) {
/*  18: 50 */       throw new NullPointerException("dialect");
/*  19:    */     }
/*  20: 52 */     return dialect.alphabet;
/*  21:    */   }
/*  22:    */   
/*  23:    */   private static byte[] decodabet(Base64Dialect dialect)
/*  24:    */   {
/*  25: 56 */     if (dialect == null) {
/*  26: 57 */       throw new NullPointerException("dialect");
/*  27:    */     }
/*  28: 59 */     return dialect.decodabet;
/*  29:    */   }
/*  30:    */   
/*  31:    */   private static boolean breakLines(Base64Dialect dialect)
/*  32:    */   {
/*  33: 63 */     if (dialect == null) {
/*  34: 64 */       throw new NullPointerException("dialect");
/*  35:    */     }
/*  36: 66 */     return dialect.breakLinesByDefault;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public static ByteBuf encode(ByteBuf src)
/*  40:    */   {
/*  41: 70 */     return encode(src, Base64Dialect.STANDARD);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public static ByteBuf encode(ByteBuf src, Base64Dialect dialect)
/*  45:    */   {
/*  46: 74 */     return encode(src, breakLines(dialect), dialect);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public static ByteBuf encode(ByteBuf src, boolean breakLines)
/*  50:    */   {
/*  51: 78 */     return encode(src, breakLines, Base64Dialect.STANDARD);
/*  52:    */   }
/*  53:    */   
/*  54:    */   public static ByteBuf encode(ByteBuf src, boolean breakLines, Base64Dialect dialect)
/*  55:    */   {
/*  56: 83 */     if (src == null) {
/*  57: 84 */       throw new NullPointerException("src");
/*  58:    */     }
/*  59: 87 */     ByteBuf dest = encode(src, src.readerIndex(), src.readableBytes(), breakLines, dialect);
/*  60: 88 */     src.readerIndex(src.writerIndex());
/*  61: 89 */     return dest;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public static ByteBuf encode(ByteBuf src, int off, int len)
/*  65:    */   {
/*  66: 93 */     return encode(src, off, len, Base64Dialect.STANDARD);
/*  67:    */   }
/*  68:    */   
/*  69:    */   public static ByteBuf encode(ByteBuf src, int off, int len, Base64Dialect dialect)
/*  70:    */   {
/*  71: 97 */     return encode(src, off, len, breakLines(dialect), dialect);
/*  72:    */   }
/*  73:    */   
/*  74:    */   public static ByteBuf encode(ByteBuf src, int off, int len, boolean breakLines)
/*  75:    */   {
/*  76:102 */     return encode(src, off, len, breakLines, Base64Dialect.STANDARD);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public static ByteBuf encode(ByteBuf src, int off, int len, boolean breakLines, Base64Dialect dialect)
/*  80:    */   {
/*  81:108 */     if (src == null) {
/*  82:109 */       throw new NullPointerException("src");
/*  83:    */     }
/*  84:111 */     if (dialect == null) {
/*  85:112 */       throw new NullPointerException("dialect");
/*  86:    */     }
/*  87:115 */     int len43 = len * 4 / 3;
/*  88:116 */     ByteBuf dest = Unpooled.buffer(len43 + (len % 3 > 0 ? 4 : 0) + (breakLines ? len43 / 76 : 0)).order(src.order());
/*  89:    */     
/*  90:    */ 
/*  91:    */ 
/*  92:120 */     int d = 0;
/*  93:121 */     int e = 0;
/*  94:122 */     int len2 = len - 2;
/*  95:123 */     int lineLength = 0;
/*  96:124 */     for (; d < len2; e += 4)
/*  97:    */     {
/*  98:125 */       encode3to4(src, d + off, 3, dest, e, dialect);
/*  99:    */       
/* 100:127 */       lineLength += 4;
/* 101:128 */       if ((breakLines) && (lineLength == 76))
/* 102:    */       {
/* 103:129 */         dest.setByte(e + 4, 10);
/* 104:130 */         e++;
/* 105:131 */         lineLength = 0;
/* 106:    */       }
/* 107:124 */       d += 3;
/* 108:    */     }
/* 109:135 */     if (d < len)
/* 110:    */     {
/* 111:136 */       encode3to4(src, d + off, len - d, dest, e, dialect);
/* 112:137 */       e += 4;
/* 113:    */     }
/* 114:140 */     return dest.slice(0, e);
/* 115:    */   }
/* 116:    */   
/* 117:    */   private static void encode3to4(ByteBuf src, int srcOffset, int numSigBytes, ByteBuf dest, int destOffset, Base64Dialect dialect)
/* 118:    */   {
/* 119:147 */     byte[] ALPHABET = alphabet(dialect);
/* 120:    */     
/* 121:    */ 
/* 122:    */ 
/* 123:    */ 
/* 124:    */ 
/* 125:    */ 
/* 126:    */ 
/* 127:    */ 
/* 128:    */ 
/* 129:    */ 
/* 130:    */ 
/* 131:    */ 
/* 132:160 */     int inBuff = (numSigBytes > 0 ? src.getByte(srcOffset) << 24 >>> 8 : 0) | (numSigBytes > 1 ? src.getByte(srcOffset + 1) << 24 >>> 16 : 0) | (numSigBytes > 2 ? src.getByte(srcOffset + 2) << 24 >>> 24 : 0);
/* 133:165 */     switch (numSigBytes)
/* 134:    */     {
/* 135:    */     case 3: 
/* 136:167 */       dest.setByte(destOffset, ALPHABET[(inBuff >>> 18)]);
/* 137:168 */       dest.setByte(destOffset + 1, ALPHABET[(inBuff >>> 12 & 0x3F)]);
/* 138:169 */       dest.setByte(destOffset + 2, ALPHABET[(inBuff >>> 6 & 0x3F)]);
/* 139:170 */       dest.setByte(destOffset + 3, ALPHABET[(inBuff & 0x3F)]);
/* 140:171 */       break;
/* 141:    */     case 2: 
/* 142:173 */       dest.setByte(destOffset, ALPHABET[(inBuff >>> 18)]);
/* 143:174 */       dest.setByte(destOffset + 1, ALPHABET[(inBuff >>> 12 & 0x3F)]);
/* 144:175 */       dest.setByte(destOffset + 2, ALPHABET[(inBuff >>> 6 & 0x3F)]);
/* 145:176 */       dest.setByte(destOffset + 3, 61);
/* 146:177 */       break;
/* 147:    */     case 1: 
/* 148:179 */       dest.setByte(destOffset, ALPHABET[(inBuff >>> 18)]);
/* 149:180 */       dest.setByte(destOffset + 1, ALPHABET[(inBuff >>> 12 & 0x3F)]);
/* 150:181 */       dest.setByte(destOffset + 2, 61);
/* 151:182 */       dest.setByte(destOffset + 3, 61);
/* 152:    */     }
/* 153:    */   }
/* 154:    */   
/* 155:    */   public static ByteBuf decode(ByteBuf src)
/* 156:    */   {
/* 157:188 */     return decode(src, Base64Dialect.STANDARD);
/* 158:    */   }
/* 159:    */   
/* 160:    */   public static ByteBuf decode(ByteBuf src, Base64Dialect dialect)
/* 161:    */   {
/* 162:193 */     if (src == null) {
/* 163:194 */       throw new NullPointerException("src");
/* 164:    */     }
/* 165:197 */     ByteBuf dest = decode(src, src.readerIndex(), src.readableBytes(), dialect);
/* 166:198 */     src.readerIndex(src.writerIndex());
/* 167:199 */     return dest;
/* 168:    */   }
/* 169:    */   
/* 170:    */   public static ByteBuf decode(ByteBuf src, int off, int len)
/* 171:    */   {
/* 172:204 */     return decode(src, off, len, Base64Dialect.STANDARD);
/* 173:    */   }
/* 174:    */   
/* 175:    */   public static ByteBuf decode(ByteBuf src, int off, int len, Base64Dialect dialect)
/* 176:    */   {
/* 177:210 */     if (src == null) {
/* 178:211 */       throw new NullPointerException("src");
/* 179:    */     }
/* 180:213 */     if (dialect == null) {
/* 181:214 */       throw new NullPointerException("dialect");
/* 182:    */     }
/* 183:217 */     byte[] DECODABET = decodabet(dialect);
/* 184:    */     
/* 185:219 */     int len34 = len * 3 / 4;
/* 186:220 */     ByteBuf dest = src.alloc().buffer(len34).order(src.order());
/* 187:221 */     int outBuffPosn = 0;
/* 188:    */     
/* 189:223 */     byte[] b4 = new byte[4];
/* 190:224 */     int b4Posn = 0;
/* 191:228 */     for (int i = off; i < off + len; i++)
/* 192:    */     {
/* 193:229 */       byte sbiCrop = (byte)(src.getByte(i) & 0x7F);
/* 194:230 */       byte sbiDecode = DECODABET[sbiCrop];
/* 195:232 */       if (sbiDecode >= -5)
/* 196:    */       {
/* 197:233 */         if (sbiDecode >= -1)
/* 198:    */         {
/* 199:234 */           b4[(b4Posn++)] = sbiCrop;
/* 200:235 */           if (b4Posn > 3)
/* 201:    */           {
/* 202:236 */             outBuffPosn += decode4to3(b4, 0, dest, outBuffPosn, dialect);
/* 203:    */             
/* 204:238 */             b4Posn = 0;
/* 205:241 */             if (sbiCrop == 61) {
/* 206:    */               break;
/* 207:    */             }
/* 208:    */           }
/* 209:    */         }
/* 210:    */       }
/* 211:    */       else {
/* 212:247 */         throw new IllegalArgumentException("bad Base64 input character at " + i + ": " + src.getUnsignedByte(i) + " (decimal)");
/* 213:    */       }
/* 214:    */     }
/* 215:253 */     return dest.slice(0, outBuffPosn);
/* 216:    */   }
/* 217:    */   
/* 218:    */   private static int decode4to3(byte[] src, int srcOffset, ByteBuf dest, int destOffset, Base64Dialect dialect)
/* 219:    */   {
/* 220:260 */     byte[] DECODABET = decodabet(dialect);
/* 221:262 */     if (src[(srcOffset + 2)] == 61)
/* 222:    */     {
/* 223:264 */       int outBuff = (DECODABET[src[srcOffset]] & 0xFF) << 18 | (DECODABET[src[(srcOffset + 1)]] & 0xFF) << 12;
/* 224:    */       
/* 225:    */ 
/* 226:    */ 
/* 227:268 */       dest.setByte(destOffset, (byte)(outBuff >>> 16));
/* 228:269 */       return 1;
/* 229:    */     }
/* 230:270 */     if (src[(srcOffset + 3)] == 61)
/* 231:    */     {
/* 232:272 */       int outBuff = (DECODABET[src[srcOffset]] & 0xFF) << 18 | (DECODABET[src[(srcOffset + 1)]] & 0xFF) << 12 | (DECODABET[src[(srcOffset + 2)]] & 0xFF) << 6;
/* 233:    */       
/* 234:    */ 
/* 235:    */ 
/* 236:    */ 
/* 237:277 */       dest.setByte(destOffset, (byte)(outBuff >>> 16));
/* 238:278 */       dest.setByte(destOffset + 1, (byte)(outBuff >>> 8));
/* 239:279 */       return 2;
/* 240:    */     }
/* 241:    */     int outBuff;
/* 242:    */     try
/* 243:    */     {
/* 244:284 */       outBuff = (DECODABET[src[srcOffset]] & 0xFF) << 18 | (DECODABET[src[(srcOffset + 1)]] & 0xFF) << 12 | (DECODABET[src[(srcOffset + 2)]] & 0xFF) << 6 | DECODABET[src[(srcOffset + 3)]] & 0xFF;
/* 245:    */     }
/* 246:    */     catch (IndexOutOfBoundsException e)
/* 247:    */     {
/* 248:290 */       throw new IllegalArgumentException("not encoded in Base64");
/* 249:    */     }
/* 250:293 */     dest.setByte(destOffset, (byte)(outBuff >> 16));
/* 251:294 */     dest.setByte(destOffset + 1, (byte)(outBuff >> 8));
/* 252:295 */     dest.setByte(destOffset + 2, (byte)outBuff);
/* 253:296 */     return 3;
/* 254:    */   }
/* 255:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.base64.Base64
 * JD-Core Version:    0.7.0.1
 */