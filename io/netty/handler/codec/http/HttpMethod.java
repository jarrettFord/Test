/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.util.CharsetUtil;
/*   5:    */ import java.util.HashMap;
/*   6:    */ import java.util.Map;
/*   7:    */ 
/*   8:    */ public class HttpMethod
/*   9:    */   implements Comparable<HttpMethod>
/*  10:    */ {
/*  11: 37 */   public static final HttpMethod OPTIONS = new HttpMethod("OPTIONS", true);
/*  12: 45 */   public static final HttpMethod GET = new HttpMethod("GET", true);
/*  13: 51 */   public static final HttpMethod HEAD = new HttpMethod("HEAD", true);
/*  14: 58 */   public static final HttpMethod POST = new HttpMethod("POST", true);
/*  15: 63 */   public static final HttpMethod PUT = new HttpMethod("PUT", true);
/*  16: 69 */   public static final HttpMethod PATCH = new HttpMethod("PATCH", true);
/*  17: 75 */   public static final HttpMethod DELETE = new HttpMethod("DELETE", true);
/*  18: 81 */   public static final HttpMethod TRACE = new HttpMethod("TRACE", true);
/*  19: 87 */   public static final HttpMethod CONNECT = new HttpMethod("CONNECT", true);
/*  20: 89 */   private static final Map<String, HttpMethod> methodMap = new HashMap();
/*  21:    */   private final String name;
/*  22:    */   private final byte[] bytes;
/*  23:    */   
/*  24:    */   static
/*  25:    */   {
/*  26: 93 */     methodMap.put(OPTIONS.toString(), OPTIONS);
/*  27: 94 */     methodMap.put(GET.toString(), GET);
/*  28: 95 */     methodMap.put(HEAD.toString(), HEAD);
/*  29: 96 */     methodMap.put(POST.toString(), POST);
/*  30: 97 */     methodMap.put(PUT.toString(), PUT);
/*  31: 98 */     methodMap.put(PATCH.toString(), PATCH);
/*  32: 99 */     methodMap.put(DELETE.toString(), DELETE);
/*  33:100 */     methodMap.put(TRACE.toString(), TRACE);
/*  34:101 */     methodMap.put(CONNECT.toString(), CONNECT);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public static HttpMethod valueOf(String name)
/*  38:    */   {
/*  39:110 */     if (name == null) {
/*  40:111 */       throw new NullPointerException("name");
/*  41:    */     }
/*  42:114 */     name = name.trim();
/*  43:115 */     if (name.isEmpty()) {
/*  44:116 */       throw new IllegalArgumentException("empty name");
/*  45:    */     }
/*  46:119 */     HttpMethod result = (HttpMethod)methodMap.get(name);
/*  47:120 */     if (result != null) {
/*  48:121 */       return result;
/*  49:    */     }
/*  50:123 */     return new HttpMethod(name);
/*  51:    */   }
/*  52:    */   
/*  53:    */   public HttpMethod(String name)
/*  54:    */   {
/*  55:138 */     this(name, false);
/*  56:    */   }
/*  57:    */   
/*  58:    */   private HttpMethod(String name, boolean bytes)
/*  59:    */   {
/*  60:142 */     if (name == null) {
/*  61:143 */       throw new NullPointerException("name");
/*  62:    */     }
/*  63:146 */     name = name.trim();
/*  64:147 */     if (name.isEmpty()) {
/*  65:148 */       throw new IllegalArgumentException("empty name");
/*  66:    */     }
/*  67:151 */     for (int i = 0; i < name.length(); i++) {
/*  68:152 */       if ((Character.isISOControl(name.charAt(i))) || (Character.isWhitespace(name.charAt(i)))) {
/*  69:154 */         throw new IllegalArgumentException("invalid character in name");
/*  70:    */       }
/*  71:    */     }
/*  72:158 */     this.name = name;
/*  73:159 */     if (bytes) {
/*  74:160 */       this.bytes = name.getBytes(CharsetUtil.US_ASCII);
/*  75:    */     } else {
/*  76:162 */       this.bytes = null;
/*  77:    */     }
/*  78:    */   }
/*  79:    */   
/*  80:    */   public String name()
/*  81:    */   {
/*  82:170 */     return this.name;
/*  83:    */   }
/*  84:    */   
/*  85:    */   public int hashCode()
/*  86:    */   {
/*  87:175 */     return name().hashCode();
/*  88:    */   }
/*  89:    */   
/*  90:    */   public boolean equals(Object o)
/*  91:    */   {
/*  92:180 */     if (!(o instanceof HttpMethod)) {
/*  93:181 */       return false;
/*  94:    */     }
/*  95:184 */     HttpMethod that = (HttpMethod)o;
/*  96:185 */     return name().equals(that.name());
/*  97:    */   }
/*  98:    */   
/*  99:    */   public String toString()
/* 100:    */   {
/* 101:190 */     return name();
/* 102:    */   }
/* 103:    */   
/* 104:    */   public int compareTo(HttpMethod o)
/* 105:    */   {
/* 106:195 */     return name().compareTo(o.name());
/* 107:    */   }
/* 108:    */   
/* 109:    */   void encode(ByteBuf buf)
/* 110:    */   {
/* 111:199 */     if (this.bytes == null) {
/* 112:200 */       HttpHeaders.encodeAscii0(this.name, buf);
/* 113:    */     } else {
/* 114:202 */       buf.writeBytes(this.bytes);
/* 115:    */     }
/* 116:    */   }
/* 117:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpMethod
 * JD-Core Version:    0.7.0.1
 */