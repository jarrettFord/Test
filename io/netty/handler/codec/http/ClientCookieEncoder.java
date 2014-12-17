/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import java.util.Iterator;
/*   4:    */ import java.util.Set;
/*   5:    */ 
/*   6:    */ public final class ClientCookieEncoder
/*   7:    */ {
/*   8:    */   public static String encode(String name, String value)
/*   9:    */   {
/*  10: 37 */     return encode(new DefaultCookie(name, value));
/*  11:    */   }
/*  12:    */   
/*  13:    */   public static String encode(Cookie cookie)
/*  14:    */   {
/*  15: 41 */     if (cookie == null) {
/*  16: 42 */       throw new NullPointerException("cookie");
/*  17:    */     }
/*  18: 45 */     StringBuilder buf = CookieEncoderUtil.stringBuilder();
/*  19: 46 */     encode(buf, cookie);
/*  20: 47 */     return CookieEncoderUtil.stripTrailingSeparator(buf);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public static String encode(Cookie... cookies)
/*  24:    */   {
/*  25: 51 */     if (cookies == null) {
/*  26: 52 */       throw new NullPointerException("cookies");
/*  27:    */     }
/*  28: 55 */     StringBuilder buf = CookieEncoderUtil.stringBuilder();
/*  29: 56 */     for (Cookie c : cookies)
/*  30:    */     {
/*  31: 57 */       if (c == null) {
/*  32:    */         break;
/*  33:    */       }
/*  34: 61 */       encode(buf, c);
/*  35:    */     }
/*  36: 63 */     return CookieEncoderUtil.stripTrailingSeparator(buf);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public static String encode(Iterable<Cookie> cookies)
/*  40:    */   {
/*  41: 67 */     if (cookies == null) {
/*  42: 68 */       throw new NullPointerException("cookies");
/*  43:    */     }
/*  44: 71 */     StringBuilder buf = CookieEncoderUtil.stringBuilder();
/*  45: 72 */     for (Cookie c : cookies)
/*  46:    */     {
/*  47: 73 */       if (c == null) {
/*  48:    */         break;
/*  49:    */       }
/*  50: 77 */       encode(buf, c);
/*  51:    */     }
/*  52: 79 */     return CookieEncoderUtil.stripTrailingSeparator(buf);
/*  53:    */   }
/*  54:    */   
/*  55:    */   private static void encode(StringBuilder buf, Cookie c)
/*  56:    */   {
/*  57: 83 */     if (c.getVersion() >= 1) {
/*  58: 84 */       CookieEncoderUtil.add(buf, "$Version", 1L);
/*  59:    */     }
/*  60: 87 */     CookieEncoderUtil.add(buf, c.getName(), c.getValue());
/*  61: 89 */     if (c.getPath() != null) {
/*  62: 90 */       CookieEncoderUtil.add(buf, "$Path", c.getPath());
/*  63:    */     }
/*  64: 93 */     if (c.getDomain() != null) {
/*  65: 94 */       CookieEncoderUtil.add(buf, "$Domain", c.getDomain());
/*  66:    */     }
/*  67: 97 */     if ((c.getVersion() >= 1) && 
/*  68: 98 */       (!c.getPorts().isEmpty()))
/*  69:    */     {
/*  70: 99 */       buf.append('$');
/*  71:100 */       buf.append("Port");
/*  72:101 */       buf.append('=');
/*  73:102 */       buf.append('"');
/*  74:103 */       for (Iterator i$ = c.getPorts().iterator(); i$.hasNext();)
/*  75:    */       {
/*  76:103 */         int port = ((Integer)i$.next()).intValue();
/*  77:104 */         buf.append(port);
/*  78:105 */         buf.append(',');
/*  79:    */       }
/*  80:107 */       buf.setCharAt(buf.length() - 1, '"');
/*  81:108 */       buf.append(';');
/*  82:109 */       buf.append(' ');
/*  83:    */     }
/*  84:    */   }
/*  85:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.ClientCookieEncoder
 * JD-Core Version:    0.7.0.1
 */