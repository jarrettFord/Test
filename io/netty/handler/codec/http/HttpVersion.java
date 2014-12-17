/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.util.CharsetUtil;
/*   5:    */ import java.util.regex.Matcher;
/*   6:    */ import java.util.regex.Pattern;
/*   7:    */ 
/*   8:    */ public class HttpVersion
/*   9:    */   implements Comparable<HttpVersion>
/*  10:    */ {
/*  11: 31 */   private static final Pattern VERSION_PATTERN = Pattern.compile("(\\S+)/(\\d+)\\.(\\d+)");
/*  12:    */   private static final String HTTP_1_0_STRING = "HTTP/1.0";
/*  13:    */   private static final String HTTP_1_1_STRING = "HTTP/1.1";
/*  14: 40 */   public static final HttpVersion HTTP_1_0 = new HttpVersion("HTTP", 1, 0, false, true);
/*  15: 45 */   public static final HttpVersion HTTP_1_1 = new HttpVersion("HTTP", 1, 1, true, true);
/*  16:    */   private final String protocolName;
/*  17:    */   private final int majorVersion;
/*  18:    */   private final int minorVersion;
/*  19:    */   private final String text;
/*  20:    */   private final boolean keepAliveDefault;
/*  21:    */   private final byte[] bytes;
/*  22:    */   
/*  23:    */   public static HttpVersion valueOf(String text)
/*  24:    */   {
/*  25: 56 */     if (text == null) {
/*  26: 57 */       throw new NullPointerException("text");
/*  27:    */     }
/*  28: 60 */     text = text.trim();
/*  29: 62 */     if (text.isEmpty()) {
/*  30: 63 */       throw new IllegalArgumentException("text is empty");
/*  31:    */     }
/*  32: 77 */     HttpVersion version = version0(text);
/*  33: 78 */     if (version == null)
/*  34:    */     {
/*  35: 79 */       text = text.toUpperCase();
/*  36:    */       
/*  37: 81 */       version = version0(text);
/*  38: 82 */       if (version == null) {
/*  39: 84 */         version = new HttpVersion(text, true);
/*  40:    */       }
/*  41:    */     }
/*  42: 87 */     return version;
/*  43:    */   }
/*  44:    */   
/*  45:    */   private static HttpVersion version0(String text)
/*  46:    */   {
/*  47: 91 */     if ("HTTP/1.1".equals(text)) {
/*  48: 92 */       return HTTP_1_1;
/*  49:    */     }
/*  50: 94 */     if ("HTTP/1.0".equals(text)) {
/*  51: 95 */       return HTTP_1_0;
/*  52:    */     }
/*  53: 97 */     return null;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public HttpVersion(String text, boolean keepAliveDefault)
/*  57:    */   {
/*  58:119 */     if (text == null) {
/*  59:120 */       throw new NullPointerException("text");
/*  60:    */     }
/*  61:123 */     text = text.trim().toUpperCase();
/*  62:124 */     if (text.isEmpty()) {
/*  63:125 */       throw new IllegalArgumentException("empty text");
/*  64:    */     }
/*  65:128 */     Matcher m = VERSION_PATTERN.matcher(text);
/*  66:129 */     if (!m.matches()) {
/*  67:130 */       throw new IllegalArgumentException("invalid version format: " + text);
/*  68:    */     }
/*  69:133 */     this.protocolName = m.group(1);
/*  70:134 */     this.majorVersion = Integer.parseInt(m.group(2));
/*  71:135 */     this.minorVersion = Integer.parseInt(m.group(3));
/*  72:136 */     this.text = (this.protocolName + '/' + this.majorVersion + '.' + this.minorVersion);
/*  73:137 */     this.keepAliveDefault = keepAliveDefault;
/*  74:138 */     this.bytes = null;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public HttpVersion(String protocolName, int majorVersion, int minorVersion, boolean keepAliveDefault)
/*  78:    */   {
/*  79:155 */     this(protocolName, majorVersion, minorVersion, keepAliveDefault, false);
/*  80:    */   }
/*  81:    */   
/*  82:    */   private HttpVersion(String protocolName, int majorVersion, int minorVersion, boolean keepAliveDefault, boolean bytes)
/*  83:    */   {
/*  84:161 */     if (protocolName == null) {
/*  85:162 */       throw new NullPointerException("protocolName");
/*  86:    */     }
/*  87:165 */     protocolName = protocolName.trim().toUpperCase();
/*  88:166 */     if (protocolName.isEmpty()) {
/*  89:167 */       throw new IllegalArgumentException("empty protocolName");
/*  90:    */     }
/*  91:170 */     for (int i = 0; i < protocolName.length(); i++) {
/*  92:171 */       if ((Character.isISOControl(protocolName.charAt(i))) || (Character.isWhitespace(protocolName.charAt(i)))) {
/*  93:173 */         throw new IllegalArgumentException("invalid character in protocolName");
/*  94:    */       }
/*  95:    */     }
/*  96:177 */     if (majorVersion < 0) {
/*  97:178 */       throw new IllegalArgumentException("negative majorVersion");
/*  98:    */     }
/*  99:180 */     if (minorVersion < 0) {
/* 100:181 */       throw new IllegalArgumentException("negative minorVersion");
/* 101:    */     }
/* 102:184 */     this.protocolName = protocolName;
/* 103:185 */     this.majorVersion = majorVersion;
/* 104:186 */     this.minorVersion = minorVersion;
/* 105:187 */     this.text = (protocolName + '/' + majorVersion + '.' + minorVersion);
/* 106:188 */     this.keepAliveDefault = keepAliveDefault;
/* 107:190 */     if (bytes) {
/* 108:191 */       this.bytes = this.text.getBytes(CharsetUtil.US_ASCII);
/* 109:    */     } else {
/* 110:193 */       this.bytes = null;
/* 111:    */     }
/* 112:    */   }
/* 113:    */   
/* 114:    */   public String protocolName()
/* 115:    */   {
/* 116:201 */     return this.protocolName;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public int majorVersion()
/* 120:    */   {
/* 121:208 */     return this.majorVersion;
/* 122:    */   }
/* 123:    */   
/* 124:    */   public int minorVersion()
/* 125:    */   {
/* 126:215 */     return this.minorVersion;
/* 127:    */   }
/* 128:    */   
/* 129:    */   public String text()
/* 130:    */   {
/* 131:222 */     return this.text;
/* 132:    */   }
/* 133:    */   
/* 134:    */   public boolean isKeepAliveDefault()
/* 135:    */   {
/* 136:230 */     return this.keepAliveDefault;
/* 137:    */   }
/* 138:    */   
/* 139:    */   public String toString()
/* 140:    */   {
/* 141:238 */     return text();
/* 142:    */   }
/* 143:    */   
/* 144:    */   public int hashCode()
/* 145:    */   {
/* 146:243 */     return (protocolName().hashCode() * 31 + majorVersion()) * 31 + minorVersion();
/* 147:    */   }
/* 148:    */   
/* 149:    */   public boolean equals(Object o)
/* 150:    */   {
/* 151:249 */     if (!(o instanceof HttpVersion)) {
/* 152:250 */       return false;
/* 153:    */     }
/* 154:253 */     HttpVersion that = (HttpVersion)o;
/* 155:254 */     return (minorVersion() == that.minorVersion()) && (majorVersion() == that.majorVersion()) && (protocolName().equals(that.protocolName()));
/* 156:    */   }
/* 157:    */   
/* 158:    */   public int compareTo(HttpVersion o)
/* 159:    */   {
/* 160:261 */     int v = protocolName().compareTo(o.protocolName());
/* 161:262 */     if (v != 0) {
/* 162:263 */       return v;
/* 163:    */     }
/* 164:266 */     v = majorVersion() - o.majorVersion();
/* 165:267 */     if (v != 0) {
/* 166:268 */       return v;
/* 167:    */     }
/* 168:271 */     return minorVersion() - o.minorVersion();
/* 169:    */   }
/* 170:    */   
/* 171:    */   void encode(ByteBuf buf)
/* 172:    */   {
/* 173:275 */     if (this.bytes == null) {
/* 174:276 */       HttpHeaders.encodeAscii0(this.text, buf);
/* 175:    */     } else {
/* 176:278 */       buf.writeBytes(this.bytes);
/* 177:    */     }
/* 178:    */   }
/* 179:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpVersion
 * JD-Core Version:    0.7.0.1
 */