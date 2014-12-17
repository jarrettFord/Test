/*   1:    */ package org.spacehq.mc.auth.util;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ 
/*   5:    */ public class Base64
/*   6:    */ {
/*   7:    */   private static final byte EQUALS_SIGN = 61;
/*   8:    */   private static final byte WHITE_SPACE_ENC = -5;
/*   9:    */   private static final byte EQUALS_SIGN_ENC = -1;
/*  10:  8 */   private static final byte[] _STANDARD_ALPHABET = { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };
/*  11:  9 */   private static final byte[] _STANDARD_DECODABET = { -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, -9, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, -9, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9 };
/*  12:    */   
/*  13:    */   private static byte[] getAlphabet()
/*  14:    */   {
/*  15: 12 */     return _STANDARD_ALPHABET;
/*  16:    */   }
/*  17:    */   
/*  18:    */   private static byte[] getDecodabet()
/*  19:    */   {
/*  20: 16 */     return _STANDARD_DECODABET;
/*  21:    */   }
/*  22:    */   
/*  23:    */   private static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset)
/*  24:    */   {
/*  25: 20 */     byte[] ALPHABET = getAlphabet();
/*  26: 21 */     int inBuff = (numSigBytes > 0 ? source[srcOffset] << 24 >>> 8 : 0) | (numSigBytes > 1 ? source[(srcOffset + 1)] << 24 >>> 16 : 0) | (numSigBytes > 2 ? source[(srcOffset + 2)] << 24 >>> 24 : 0);
/*  27: 22 */     switch (numSigBytes)
/*  28:    */     {
/*  29:    */     case 3: 
/*  30: 24 */       destination[destOffset] = ALPHABET[(inBuff >>> 18)];
/*  31: 25 */       destination[(destOffset + 1)] = ALPHABET[(inBuff >>> 12 & 0x3F)];
/*  32: 26 */       destination[(destOffset + 2)] = ALPHABET[(inBuff >>> 6 & 0x3F)];
/*  33: 27 */       destination[(destOffset + 3)] = ALPHABET[(inBuff & 0x3F)];
/*  34: 28 */       return destination;
/*  35:    */     case 2: 
/*  36: 30 */       destination[destOffset] = ALPHABET[(inBuff >>> 18)];
/*  37: 31 */       destination[(destOffset + 1)] = ALPHABET[(inBuff >>> 12 & 0x3F)];
/*  38: 32 */       destination[(destOffset + 2)] = ALPHABET[(inBuff >>> 6 & 0x3F)];
/*  39: 33 */       destination[(destOffset + 3)] = 61;
/*  40: 34 */       return destination;
/*  41:    */     case 1: 
/*  42: 36 */       destination[destOffset] = ALPHABET[(inBuff >>> 18)];
/*  43: 37 */       destination[(destOffset + 1)] = ALPHABET[(inBuff >>> 12 & 0x3F)];
/*  44: 38 */       destination[(destOffset + 2)] = 61;
/*  45: 39 */       destination[(destOffset + 3)] = 61;
/*  46: 40 */       return destination;
/*  47:    */     }
/*  48: 42 */     return destination;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public static byte[] encode(byte[] source)
/*  52:    */   {
/*  53: 47 */     return encode(source, 0, source.length);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static byte[] encode(byte[] source, int off, int len)
/*  57:    */   {
/*  58: 51 */     if (source == null) {
/*  59: 52 */       throw new NullPointerException("Cannot serialize a null array.");
/*  60:    */     }
/*  61: 55 */     if (off < 0) {
/*  62: 56 */       throw new IllegalArgumentException("Cannot have negative offset: " + off);
/*  63:    */     }
/*  64: 59 */     if (len < 0) {
/*  65: 60 */       throw new IllegalArgumentException("Cannot have length offset: " + len);
/*  66:    */     }
/*  67: 63 */     if (off + len > source.length) {
/*  68: 64 */       throw new IllegalArgumentException(String.format("Cannot have offset of %d and length of %d with array of length %d", new Object[] { Integer.valueOf(off), Integer.valueOf(len), Integer.valueOf(source.length) }));
/*  69:    */     }
/*  70: 67 */     int encLen = len / 3 * 4 + (len % 3 > 0 ? 4 : 0);
/*  71: 68 */     byte[] outBuff = new byte[encLen];
/*  72: 69 */     int d = 0;
/*  73: 70 */     int e = 0;
/*  74: 71 */     int len2 = len - 2;
/*  75: 72 */     for (; d < len2; e += 4)
/*  76:    */     {
/*  77: 73 */       encode3to4(source, d + off, 3, outBuff, e);d += 3;
/*  78:    */     }
/*  79: 76 */     if (d < len)
/*  80:    */     {
/*  81: 77 */       encode3to4(source, d + off, len - d, outBuff, e);
/*  82: 78 */       e += 4;
/*  83:    */     }
/*  84: 81 */     if (e <= outBuff.length - 1)
/*  85:    */     {
/*  86: 82 */       byte[] finalOut = new byte[e];
/*  87: 83 */       System.arraycopy(outBuff, 0, finalOut, 0, e);
/*  88: 84 */       return finalOut;
/*  89:    */     }
/*  90: 86 */     return outBuff;
/*  91:    */   }
/*  92:    */   
/*  93:    */   private static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset)
/*  94:    */   {
/*  95: 91 */     if (source == null) {
/*  96: 92 */       throw new NullPointerException("Source array was null.");
/*  97:    */     }
/*  98: 95 */     if (destination == null) {
/*  99: 96 */       throw new NullPointerException("Destination array was null.");
/* 100:    */     }
/* 101: 99 */     if ((srcOffset < 0) || (srcOffset + 3 >= source.length)) {
/* 102:100 */       throw new IllegalArgumentException(String.format("Source array with length %d cannot have offset of %d and still process four bytes.", new Object[] { Integer.valueOf(source.length), Integer.valueOf(srcOffset) }));
/* 103:    */     }
/* 104:103 */     if ((destOffset < 0) || (destOffset + 2 >= destination.length)) {
/* 105:104 */       throw new IllegalArgumentException(String.format("Destination array with length %d cannot have offset of %d and still store three bytes.", new Object[] { Integer.valueOf(destination.length), Integer.valueOf(destOffset) }));
/* 106:    */     }
/* 107:107 */     byte[] DECODABET = getDecodabet();
/* 108:108 */     if (source[(srcOffset + 2)] == 61)
/* 109:    */     {
/* 110:109 */       int outBuff = (DECODABET[source[srcOffset]] & 0xFF) << 18 | (DECODABET[source[(srcOffset + 1)]] & 0xFF) << 12;
/* 111:110 */       destination[destOffset] = ((byte)(outBuff >>> 16));
/* 112:111 */       return 1;
/* 113:    */     }
/* 114:112 */     if (source[(srcOffset + 3)] == 61)
/* 115:    */     {
/* 116:113 */       int outBuff = (DECODABET[source[srcOffset]] & 0xFF) << 18 | (DECODABET[source[(srcOffset + 1)]] & 0xFF) << 12 | (DECODABET[source[(srcOffset + 2)]] & 0xFF) << 6;
/* 117:114 */       destination[destOffset] = ((byte)(outBuff >>> 16));
/* 118:115 */       destination[(destOffset + 1)] = ((byte)(outBuff >>> 8));
/* 119:116 */       return 2;
/* 120:    */     }
/* 121:118 */     int outBuff = (DECODABET[source[srcOffset]] & 0xFF) << 18 | (DECODABET[source[(srcOffset + 1)]] & 0xFF) << 12 | (DECODABET[source[(srcOffset + 2)]] & 0xFF) << 6 | DECODABET[source[(srcOffset + 3)]] & 0xFF;
/* 122:119 */     destination[destOffset] = ((byte)(outBuff >> 16));
/* 123:120 */     destination[(destOffset + 1)] = ((byte)(outBuff >> 8));
/* 124:121 */     destination[(destOffset + 2)] = ((byte)outBuff);
/* 125:122 */     return 3;
/* 126:    */   }
/* 127:    */   
/* 128:    */   public static byte[] decode(byte[] source)
/* 129:    */     throws IOException
/* 130:    */   {
/* 131:127 */     return decode(source, 0, source.length);
/* 132:    */   }
/* 133:    */   
/* 134:    */   public static byte[] decode(byte[] source, int off, int len)
/* 135:    */     throws IOException
/* 136:    */   {
/* 137:131 */     if (source == null) {
/* 138:132 */       throw new NullPointerException("Cannot decode null source array.");
/* 139:    */     }
/* 140:135 */     if ((off < 0) || (off + len > source.length)) {
/* 141:136 */       throw new IllegalArgumentException(String.format("Source array with length %d cannot have offset of %d and process %d bytes.", new Object[] { Integer.valueOf(source.length), Integer.valueOf(off), Integer.valueOf(len) }));
/* 142:    */     }
/* 143:139 */     if (len == 0) {
/* 144:140 */       return new byte[0];
/* 145:    */     }
/* 146:141 */     if (len < 4) {
/* 147:142 */       throw new IllegalArgumentException("Base64-encoded string must have at least four characters, but length specified was " + len);
/* 148:    */     }
/* 149:145 */     byte[] DECODABET = getDecodabet();
/* 150:146 */     int len34 = len * 3 / 4;
/* 151:147 */     byte[] outBuff = new byte[len34];
/* 152:148 */     int outBuffPosn = 0;
/* 153:149 */     byte[] b4 = new byte[4];
/* 154:150 */     int b4Posn = 0;
/* 155:151 */     int i = 0;
/* 156:152 */     byte sbiDecode = 0;
/* 157:153 */     for (i = off; i < off + len; i++)
/* 158:    */     {
/* 159:154 */       sbiDecode = DECODABET[(source[i] & 0xFF)];
/* 160:155 */       if (sbiDecode >= -5)
/* 161:    */       {
/* 162:156 */         if (sbiDecode >= -1)
/* 163:    */         {
/* 164:157 */           b4[(b4Posn++)] = source[i];
/* 165:158 */           if (b4Posn > 3)
/* 166:    */           {
/* 167:159 */             outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
/* 168:160 */             b4Posn = 0;
/* 169:161 */             if (source[i] == 61) {
/* 170:    */               break;
/* 171:    */             }
/* 172:    */           }
/* 173:    */         }
/* 174:    */       }
/* 175:    */       else {
/* 176:167 */         throw new IOException(String.format("Bad Base64 input character decimal %d in array position %d", new Object[] { Integer.valueOf(source[i] & 0xFF), Integer.valueOf(i) }));
/* 177:    */       }
/* 178:    */     }
/* 179:171 */     byte[] out = new byte[outBuffPosn];
/* 180:172 */     System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
/* 181:173 */     return out;
/* 182:    */   }
/* 183:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.util.Base64
 * JD-Core Version:    0.7.0.1
 */