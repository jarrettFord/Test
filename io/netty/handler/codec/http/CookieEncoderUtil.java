/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.InternalThreadLocalMap;
/*  4:   */ 
/*  5:   */ final class CookieEncoderUtil
/*  6:   */ {
/*  7:   */   static StringBuilder stringBuilder()
/*  8:   */   {
/*  9:24 */     return InternalThreadLocalMap.get().stringBuilder();
/* 10:   */   }
/* 11:   */   
/* 12:   */   static String stripTrailingSeparator(StringBuilder buf)
/* 13:   */   {
/* 14:28 */     if (buf.length() > 0) {
/* 15:29 */       buf.setLength(buf.length() - 2);
/* 16:   */     }
/* 17:31 */     return buf.toString();
/* 18:   */   }
/* 19:   */   
/* 20:   */   static void add(StringBuilder sb, String name, String val)
/* 21:   */   {
/* 22:35 */     if (val == null)
/* 23:   */     {
/* 24:36 */       addQuoted(sb, name, "");
/* 25:37 */       return;
/* 26:   */     }
/* 27:40 */     for (int i = 0; i < val.length(); i++)
/* 28:   */     {
/* 29:41 */       char c = val.charAt(i);
/* 30:42 */       switch (c)
/* 31:   */       {
/* 32:   */       case '\t': 
/* 33:   */       case ' ': 
/* 34:   */       case '"': 
/* 35:   */       case '(': 
/* 36:   */       case ')': 
/* 37:   */       case ',': 
/* 38:   */       case '/': 
/* 39:   */       case ':': 
/* 40:   */       case ';': 
/* 41:   */       case '<': 
/* 42:   */       case '=': 
/* 43:   */       case '>': 
/* 44:   */       case '?': 
/* 45:   */       case '@': 
/* 46:   */       case '[': 
/* 47:   */       case '\\': 
/* 48:   */       case ']': 
/* 49:   */       case '{': 
/* 50:   */       case '}': 
/* 51:47 */         addQuoted(sb, name, val);
/* 52:48 */         return;
/* 53:   */       }
/* 54:   */     }
/* 55:52 */     addUnquoted(sb, name, val);
/* 56:   */   }
/* 57:   */   
/* 58:   */   static void addUnquoted(StringBuilder sb, String name, String val)
/* 59:   */   {
/* 60:56 */     sb.append(name);
/* 61:57 */     sb.append('=');
/* 62:58 */     sb.append(val);
/* 63:59 */     sb.append(';');
/* 64:60 */     sb.append(' ');
/* 65:   */   }
/* 66:   */   
/* 67:   */   static void addQuoted(StringBuilder sb, String name, String val)
/* 68:   */   {
/* 69:64 */     if (val == null) {
/* 70:65 */       val = "";
/* 71:   */     }
/* 72:68 */     sb.append(name);
/* 73:69 */     sb.append('=');
/* 74:70 */     sb.append('"');
/* 75:71 */     sb.append(val.replace("\\", "\\\\").replace("\"", "\\\""));
/* 76:72 */     sb.append('"');
/* 77:73 */     sb.append(';');
/* 78:74 */     sb.append(' ');
/* 79:   */   }
/* 80:   */   
/* 81:   */   static void add(StringBuilder sb, String name, long val)
/* 82:   */   {
/* 83:78 */     sb.append(name);
/* 84:79 */     sb.append('=');
/* 85:80 */     sb.append(val);
/* 86:81 */     sb.append(';');
/* 87:82 */     sb.append(' ');
/* 88:   */   }
/* 89:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.CookieEncoderUtil
 * JD-Core Version:    0.7.0.1
 */