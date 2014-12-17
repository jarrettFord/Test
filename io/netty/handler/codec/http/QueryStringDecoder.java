/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import java.net.URI;
/*   4:    */ import java.nio.charset.Charset;
/*   5:    */ import java.util.ArrayList;
/*   6:    */ import java.util.Collections;
/*   7:    */ import java.util.LinkedHashMap;
/*   8:    */ import java.util.List;
/*   9:    */ import java.util.Map;
/*  10:    */ 
/*  11:    */ public class QueryStringDecoder
/*  12:    */ {
/*  13:    */   private static final int DEFAULT_MAX_PARAMS = 1024;
/*  14:    */   private final Charset charset;
/*  15:    */   private final String uri;
/*  16:    */   private final boolean hasPath;
/*  17:    */   private final int maxParams;
/*  18:    */   private String path;
/*  19:    */   private Map<String, List<String>> params;
/*  20:    */   private int nParams;
/*  21:    */   
/*  22:    */   public QueryStringDecoder(String uri)
/*  23:    */   {
/*  24: 73 */     this(uri, HttpConstants.DEFAULT_CHARSET);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public QueryStringDecoder(String uri, boolean hasPath)
/*  28:    */   {
/*  29: 81 */     this(uri, HttpConstants.DEFAULT_CHARSET, hasPath);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public QueryStringDecoder(String uri, Charset charset)
/*  33:    */   {
/*  34: 89 */     this(uri, charset, true);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public QueryStringDecoder(String uri, Charset charset, boolean hasPath)
/*  38:    */   {
/*  39: 97 */     this(uri, charset, hasPath, 1024);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public QueryStringDecoder(String uri, Charset charset, boolean hasPath, int maxParams)
/*  43:    */   {
/*  44:105 */     if (uri == null) {
/*  45:106 */       throw new NullPointerException("getUri");
/*  46:    */     }
/*  47:108 */     if (charset == null) {
/*  48:109 */       throw new NullPointerException("charset");
/*  49:    */     }
/*  50:111 */     if (maxParams <= 0) {
/*  51:112 */       throw new IllegalArgumentException("maxParams: " + maxParams + " (expected: a positive integer)");
/*  52:    */     }
/*  53:116 */     this.uri = uri;
/*  54:117 */     this.charset = charset;
/*  55:118 */     this.maxParams = maxParams;
/*  56:119 */     this.hasPath = hasPath;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public QueryStringDecoder(URI uri)
/*  60:    */   {
/*  61:127 */     this(uri, HttpConstants.DEFAULT_CHARSET);
/*  62:    */   }
/*  63:    */   
/*  64:    */   public QueryStringDecoder(URI uri, Charset charset)
/*  65:    */   {
/*  66:135 */     this(uri, charset, 1024);
/*  67:    */   }
/*  68:    */   
/*  69:    */   public QueryStringDecoder(URI uri, Charset charset, int maxParams)
/*  70:    */   {
/*  71:143 */     if (uri == null) {
/*  72:144 */       throw new NullPointerException("getUri");
/*  73:    */     }
/*  74:146 */     if (charset == null) {
/*  75:147 */       throw new NullPointerException("charset");
/*  76:    */     }
/*  77:149 */     if (maxParams <= 0) {
/*  78:150 */       throw new IllegalArgumentException("maxParams: " + maxParams + " (expected: a positive integer)");
/*  79:    */     }
/*  80:154 */     String rawPath = uri.getRawPath();
/*  81:155 */     if (rawPath != null)
/*  82:    */     {
/*  83:156 */       this.hasPath = true;
/*  84:    */     }
/*  85:    */     else
/*  86:    */     {
/*  87:158 */       rawPath = "";
/*  88:159 */       this.hasPath = false;
/*  89:    */     }
/*  90:162 */     this.uri = (rawPath + '?' + uri.getRawQuery());
/*  91:    */     
/*  92:164 */     this.charset = charset;
/*  93:165 */     this.maxParams = maxParams;
/*  94:    */   }
/*  95:    */   
/*  96:    */   public String path()
/*  97:    */   {
/*  98:172 */     if (this.path == null)
/*  99:    */     {
/* 100:173 */       if (!this.hasPath) {
/* 101:174 */         return this.path = "";
/* 102:    */       }
/* 103:177 */       int pathEndPos = this.uri.indexOf('?');
/* 104:178 */       if (pathEndPos < 0) {
/* 105:179 */         this.path = this.uri;
/* 106:    */       } else {
/* 107:181 */         return this.path = this.uri.substring(0, pathEndPos);
/* 108:    */       }
/* 109:    */     }
/* 110:184 */     return this.path;
/* 111:    */   }
/* 112:    */   
/* 113:    */   public Map<String, List<String>> parameters()
/* 114:    */   {
/* 115:191 */     if (this.params == null) {
/* 116:192 */       if (this.hasPath)
/* 117:    */       {
/* 118:193 */         int pathLength = path().length();
/* 119:194 */         if (this.uri.length() == pathLength) {
/* 120:195 */           return Collections.emptyMap();
/* 121:    */         }
/* 122:197 */         decodeParams(this.uri.substring(pathLength + 1));
/* 123:    */       }
/* 124:    */       else
/* 125:    */       {
/* 126:199 */         if (this.uri.isEmpty()) {
/* 127:200 */           return Collections.emptyMap();
/* 128:    */         }
/* 129:202 */         decodeParams(this.uri);
/* 130:    */       }
/* 131:    */     }
/* 132:205 */     return this.params;
/* 133:    */   }
/* 134:    */   
/* 135:    */   private void decodeParams(String s)
/* 136:    */   {
/* 137:209 */     Map<String, List<String>> params = this.params = new LinkedHashMap();
/* 138:210 */     this.nParams = 0;
/* 139:211 */     String name = null;
/* 140:212 */     int pos = 0;
/* 141:215 */     for (int i = 0; i < s.length(); i++)
/* 142:    */     {
/* 143:216 */       char c = s.charAt(i);
/* 144:217 */       if ((c == '=') && (name == null))
/* 145:    */       {
/* 146:218 */         if (pos != i) {
/* 147:219 */           name = decodeComponent(s.substring(pos, i), this.charset);
/* 148:    */         }
/* 149:221 */         pos = i + 1;
/* 150:    */       }
/* 151:223 */       else if ((c == '&') || (c == ';'))
/* 152:    */       {
/* 153:224 */         if ((name == null) && (pos != i))
/* 154:    */         {
/* 155:228 */           if (addParam(params, decodeComponent(s.substring(pos, i), this.charset), "")) {}
/* 156:    */         }
/* 157:231 */         else if (name != null)
/* 158:    */         {
/* 159:232 */           if (!addParam(params, name, decodeComponent(s.substring(pos, i), this.charset))) {
/* 160:233 */             return;
/* 161:    */           }
/* 162:235 */           name = null;
/* 163:    */         }
/* 164:237 */         pos = i + 1;
/* 165:    */       }
/* 166:    */     }
/* 167:241 */     if (pos != i)
/* 168:    */     {
/* 169:242 */       if (name == null) {
/* 170:243 */         addParam(params, decodeComponent(s.substring(pos, i), this.charset), "");
/* 171:    */       } else {
/* 172:245 */         addParam(params, name, decodeComponent(s.substring(pos, i), this.charset));
/* 173:    */       }
/* 174:    */     }
/* 175:247 */     else if (name != null) {
/* 176:248 */       addParam(params, name, "");
/* 177:    */     }
/* 178:    */   }
/* 179:    */   
/* 180:    */   private boolean addParam(Map<String, List<String>> params, String name, String value)
/* 181:    */   {
/* 182:253 */     if (this.nParams >= this.maxParams) {
/* 183:254 */       return false;
/* 184:    */     }
/* 185:257 */     List<String> values = (List)params.get(name);
/* 186:258 */     if (values == null)
/* 187:    */     {
/* 188:259 */       values = new ArrayList(1);
/* 189:260 */       params.put(name, values);
/* 190:    */     }
/* 191:262 */     values.add(value);
/* 192:263 */     this.nParams += 1;
/* 193:264 */     return true;
/* 194:    */   }
/* 195:    */   
/* 196:    */   public static String decodeComponent(String s)
/* 197:    */   {
/* 198:279 */     return decodeComponent(s, HttpConstants.DEFAULT_CHARSET);
/* 199:    */   }
/* 200:    */   
/* 201:    */   public static String decodeComponent(String s, Charset charset)
/* 202:    */   {
/* 203:305 */     if (s == null) {
/* 204:306 */       return "";
/* 205:    */     }
/* 206:308 */     int size = s.length();
/* 207:309 */     boolean modified = false;
/* 208:310 */     for (int i = 0; i < size; i++)
/* 209:    */     {
/* 210:311 */       char c = s.charAt(i);
/* 211:312 */       if ((c == '%') || (c == '+'))
/* 212:    */       {
/* 213:313 */         modified = true;
/* 214:314 */         break;
/* 215:    */       }
/* 216:    */     }
/* 217:317 */     if (!modified) {
/* 218:318 */       return s;
/* 219:    */     }
/* 220:320 */     byte[] buf = new byte[size];
/* 221:321 */     int pos = 0;
/* 222:322 */     for (int i = 0; i < size; i++)
/* 223:    */     {
/* 224:323 */       char c = s.charAt(i);
/* 225:324 */       switch (c)
/* 226:    */       {
/* 227:    */       case '+': 
/* 228:326 */         buf[(pos++)] = 32;
/* 229:327 */         break;
/* 230:    */       case '%': 
/* 231:329 */         if (i == size - 1) {
/* 232:330 */           throw new IllegalArgumentException("unterminated escape sequence at end of string: " + s);
/* 233:    */         }
/* 234:333 */         c = s.charAt(++i);
/* 235:334 */         if (c == '%')
/* 236:    */         {
/* 237:335 */           buf[(pos++)] = 37;
/* 238:    */         }
/* 239:    */         else
/* 240:    */         {
/* 241:338 */           if (i == size - 1) {
/* 242:339 */             throw new IllegalArgumentException("partial escape sequence at end of string: " + s);
/* 243:    */           }
/* 244:342 */           c = decodeHexNibble(c);
/* 245:343 */           char c2 = decodeHexNibble(s.charAt(++i));
/* 246:344 */           if ((c == 65535) || (c2 == 65535)) {
/* 247:345 */             throw new IllegalArgumentException("invalid escape sequence `%" + s.charAt(i - 1) + s.charAt(i) + "' at index " + (i - 2) + " of: " + s);
/* 248:    */           }
/* 249:350 */           c = (char)(c * '\020' + c2);
/* 250:    */         }
/* 251:    */         break;
/* 252:    */       default: 
/* 253:353 */         buf[(pos++)] = ((byte)c);
/* 254:    */       }
/* 255:    */     }
/* 256:357 */     return new String(buf, 0, pos, charset);
/* 257:    */   }
/* 258:    */   
/* 259:    */   private static char decodeHexNibble(char c)
/* 260:    */   {
/* 261:368 */     if (('0' <= c) && (c <= '9')) {
/* 262:369 */       return (char)(c - '0');
/* 263:    */     }
/* 264:370 */     if (('a' <= c) && (c <= 'f')) {
/* 265:371 */       return (char)(c - 'a' + 10);
/* 266:    */     }
/* 267:372 */     if (('A' <= c) && (c <= 'F')) {
/* 268:373 */       return (char)(c - 'A' + 10);
/* 269:    */     }
/* 270:375 */     return 65535;
/* 271:    */   }
/* 272:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.QueryStringDecoder
 * JD-Core Version:    0.7.0.1
 */