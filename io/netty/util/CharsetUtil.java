/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.InternalThreadLocalMap;
/*   4:    */ import java.nio.charset.Charset;
/*   5:    */ import java.nio.charset.CharsetDecoder;
/*   6:    */ import java.nio.charset.CharsetEncoder;
/*   7:    */ import java.nio.charset.CodingErrorAction;
/*   8:    */ import java.util.Map;
/*   9:    */ 
/*  10:    */ public final class CharsetUtil
/*  11:    */ {
/*  12: 36 */   public static final Charset UTF_16 = Charset.forName("UTF-16");
/*  13: 41 */   public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
/*  14: 46 */   public static final Charset UTF_16LE = Charset.forName("UTF-16LE");
/*  15: 51 */   public static final Charset UTF_8 = Charset.forName("UTF-8");
/*  16: 56 */   public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
/*  17: 62 */   public static final Charset US_ASCII = Charset.forName("US-ASCII");
/*  18:    */   
/*  19:    */   public static CharsetEncoder getEncoder(Charset charset)
/*  20:    */   {
/*  21: 69 */     if (charset == null) {
/*  22: 70 */       throw new NullPointerException("charset");
/*  23:    */     }
/*  24: 73 */     Map<Charset, CharsetEncoder> map = InternalThreadLocalMap.get().charsetEncoderCache();
/*  25: 74 */     CharsetEncoder e = (CharsetEncoder)map.get(charset);
/*  26: 75 */     if (e != null)
/*  27:    */     {
/*  28: 76 */       e.reset();
/*  29: 77 */       e.onMalformedInput(CodingErrorAction.REPLACE);
/*  30: 78 */       e.onUnmappableCharacter(CodingErrorAction.REPLACE);
/*  31: 79 */       return e;
/*  32:    */     }
/*  33: 82 */     e = charset.newEncoder();
/*  34: 83 */     e.onMalformedInput(CodingErrorAction.REPLACE);
/*  35: 84 */     e.onUnmappableCharacter(CodingErrorAction.REPLACE);
/*  36: 85 */     map.put(charset, e);
/*  37: 86 */     return e;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public static CharsetDecoder getDecoder(Charset charset)
/*  41:    */   {
/*  42: 94 */     if (charset == null) {
/*  43: 95 */       throw new NullPointerException("charset");
/*  44:    */     }
/*  45: 98 */     Map<Charset, CharsetDecoder> map = InternalThreadLocalMap.get().charsetDecoderCache();
/*  46: 99 */     CharsetDecoder d = (CharsetDecoder)map.get(charset);
/*  47:100 */     if (d != null)
/*  48:    */     {
/*  49:101 */       d.reset();
/*  50:102 */       d.onMalformedInput(CodingErrorAction.REPLACE);
/*  51:103 */       d.onUnmappableCharacter(CodingErrorAction.REPLACE);
/*  52:104 */       return d;
/*  53:    */     }
/*  54:107 */     d = charset.newDecoder();
/*  55:108 */     d.onMalformedInput(CodingErrorAction.REPLACE);
/*  56:109 */     d.onUnmappableCharacter(CodingErrorAction.REPLACE);
/*  57:110 */     map.put(charset, d);
/*  58:111 */     return d;
/*  59:    */   }
/*  60:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.CharsetUtil
 * JD-Core Version:    0.7.0.1
 */