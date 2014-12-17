/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import java.util.ArrayList;
/*   4:    */ import java.util.Collection;
/*   5:    */ import java.util.Date;
/*   6:    */ import java.util.Iterator;
/*   7:    */ import java.util.List;
/*   8:    */ import java.util.Set;
/*   9:    */ 
/*  10:    */ public final class ServerCookieEncoder
/*  11:    */ {
/*  12:    */   public static String encode(String name, String value)
/*  13:    */   {
/*  14: 42 */     return encode(new DefaultCookie(name, value));
/*  15:    */   }
/*  16:    */   
/*  17:    */   public static String encode(Cookie cookie)
/*  18:    */   {
/*  19: 46 */     if (cookie == null) {
/*  20: 47 */       throw new NullPointerException("cookie");
/*  21:    */     }
/*  22: 50 */     StringBuilder buf = CookieEncoderUtil.stringBuilder();
/*  23:    */     
/*  24: 52 */     CookieEncoderUtil.add(buf, cookie.getName(), cookie.getValue());
/*  25: 54 */     if (cookie.getMaxAge() != -9223372036854775808L) {
/*  26: 55 */       if (cookie.getVersion() == 0) {
/*  27: 56 */         CookieEncoderUtil.addUnquoted(buf, "Expires", HttpHeaderDateFormat.get().format(new Date(System.currentTimeMillis() + cookie.getMaxAge() * 1000L)));
/*  28:    */       } else {
/*  29: 61 */         CookieEncoderUtil.add(buf, "Max-Age", cookie.getMaxAge());
/*  30:    */       }
/*  31:    */     }
/*  32: 65 */     if (cookie.getPath() != null) {
/*  33: 66 */       if (cookie.getVersion() > 0) {
/*  34: 67 */         CookieEncoderUtil.add(buf, "Path", cookie.getPath());
/*  35:    */       } else {
/*  36: 69 */         CookieEncoderUtil.addUnquoted(buf, "Path", cookie.getPath());
/*  37:    */       }
/*  38:    */     }
/*  39: 73 */     if (cookie.getDomain() != null) {
/*  40: 74 */       if (cookie.getVersion() > 0) {
/*  41: 75 */         CookieEncoderUtil.add(buf, "Domain", cookie.getDomain());
/*  42:    */       } else {
/*  43: 77 */         CookieEncoderUtil.addUnquoted(buf, "Domain", cookie.getDomain());
/*  44:    */       }
/*  45:    */     }
/*  46: 80 */     if (cookie.isSecure())
/*  47:    */     {
/*  48: 81 */       buf.append("Secure");
/*  49: 82 */       buf.append(';');
/*  50: 83 */       buf.append(' ');
/*  51:    */     }
/*  52: 85 */     if (cookie.isHttpOnly())
/*  53:    */     {
/*  54: 86 */       buf.append("HTTPOnly");
/*  55: 87 */       buf.append(';');
/*  56: 88 */       buf.append(' ');
/*  57:    */     }
/*  58: 90 */     if (cookie.getVersion() >= 1)
/*  59:    */     {
/*  60: 91 */       if (cookie.getComment() != null) {
/*  61: 92 */         CookieEncoderUtil.add(buf, "Comment", cookie.getComment());
/*  62:    */       }
/*  63: 95 */       CookieEncoderUtil.add(buf, "Version", 1L);
/*  64: 97 */       if (cookie.getCommentUrl() != null) {
/*  65: 98 */         CookieEncoderUtil.addQuoted(buf, "CommentURL", cookie.getCommentUrl());
/*  66:    */       }
/*  67:101 */       if (!cookie.getPorts().isEmpty())
/*  68:    */       {
/*  69:102 */         buf.append("Port");
/*  70:103 */         buf.append('=');
/*  71:104 */         buf.append('"');
/*  72:105 */         for (Iterator i$ = cookie.getPorts().iterator(); i$.hasNext();)
/*  73:    */         {
/*  74:105 */           int port = ((Integer)i$.next()).intValue();
/*  75:106 */           buf.append(port);
/*  76:107 */           buf.append(',');
/*  77:    */         }
/*  78:109 */         buf.setCharAt(buf.length() - 1, '"');
/*  79:110 */         buf.append(';');
/*  80:111 */         buf.append(' ');
/*  81:    */       }
/*  82:113 */       if (cookie.isDiscard())
/*  83:    */       {
/*  84:114 */         buf.append("Discard");
/*  85:115 */         buf.append(';');
/*  86:116 */         buf.append(' ');
/*  87:    */       }
/*  88:    */     }
/*  89:120 */     return CookieEncoderUtil.stripTrailingSeparator(buf);
/*  90:    */   }
/*  91:    */   
/*  92:    */   public static List<String> encode(Cookie... cookies)
/*  93:    */   {
/*  94:124 */     if (cookies == null) {
/*  95:125 */       throw new NullPointerException("cookies");
/*  96:    */     }
/*  97:128 */     List<String> encoded = new ArrayList(cookies.length);
/*  98:129 */     for (Cookie c : cookies)
/*  99:    */     {
/* 100:130 */       if (c == null) {
/* 101:    */         break;
/* 102:    */       }
/* 103:133 */       encoded.add(encode(c));
/* 104:    */     }
/* 105:135 */     return encoded;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public static List<String> encode(Collection<Cookie> cookies)
/* 109:    */   {
/* 110:139 */     if (cookies == null) {
/* 111:140 */       throw new NullPointerException("cookies");
/* 112:    */     }
/* 113:143 */     List<String> encoded = new ArrayList(cookies.size());
/* 114:144 */     for (Cookie c : cookies)
/* 115:    */     {
/* 116:145 */       if (c == null) {
/* 117:    */         break;
/* 118:    */       }
/* 119:148 */       encoded.add(encode(c));
/* 120:    */     }
/* 121:150 */     return encoded;
/* 122:    */   }
/* 123:    */   
/* 124:    */   public static List<String> encode(Iterable<Cookie> cookies)
/* 125:    */   {
/* 126:154 */     if (cookies == null) {
/* 127:155 */       throw new NullPointerException("cookies");
/* 128:    */     }
/* 129:158 */     List<String> encoded = new ArrayList();
/* 130:159 */     for (Cookie c : cookies)
/* 131:    */     {
/* 132:160 */       if (c == null) {
/* 133:    */         break;
/* 134:    */       }
/* 135:163 */       encoded.add(encode(c));
/* 136:    */     }
/* 137:165 */     return encoded;
/* 138:    */   }
/* 139:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.ServerCookieEncoder
 * JD-Core Version:    0.7.0.1
 */