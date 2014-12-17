/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.handler.codec.http.HttpMethod;
/*   4:    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*   5:    */ import io.netty.handler.codec.http.HttpVersion;
/*   6:    */ import java.util.Collections;
/*   7:    */ import java.util.Iterator;
/*   8:    */ import java.util.List;
/*   9:    */ import java.util.Map.Entry;
/*  10:    */ import java.util.Set;
/*  11:    */ 
/*  12:    */ public abstract class SpdyHeaders
/*  13:    */   implements Iterable<Map.Entry<String, String>>
/*  14:    */ {
/*  15: 34 */   public static final SpdyHeaders EMPTY_HEADERS = new SpdyHeaders()
/*  16:    */   {
/*  17:    */     public List<String> getAll(String name)
/*  18:    */     {
/*  19: 38 */       return Collections.emptyList();
/*  20:    */     }
/*  21:    */     
/*  22:    */     public List<Map.Entry<String, String>> entries()
/*  23:    */     {
/*  24: 43 */       return Collections.emptyList();
/*  25:    */     }
/*  26:    */     
/*  27:    */     public boolean contains(String name)
/*  28:    */     {
/*  29: 48 */       return false;
/*  30:    */     }
/*  31:    */     
/*  32:    */     public boolean isEmpty()
/*  33:    */     {
/*  34: 53 */       return true;
/*  35:    */     }
/*  36:    */     
/*  37:    */     public Set<String> names()
/*  38:    */     {
/*  39: 58 */       return Collections.emptySet();
/*  40:    */     }
/*  41:    */     
/*  42:    */     public SpdyHeaders add(String name, Object value)
/*  43:    */     {
/*  44: 63 */       throw new UnsupportedOperationException("read only");
/*  45:    */     }
/*  46:    */     
/*  47:    */     public SpdyHeaders add(String name, Iterable<?> values)
/*  48:    */     {
/*  49: 68 */       throw new UnsupportedOperationException("read only");
/*  50:    */     }
/*  51:    */     
/*  52:    */     public SpdyHeaders set(String name, Object value)
/*  53:    */     {
/*  54: 73 */       throw new UnsupportedOperationException("read only");
/*  55:    */     }
/*  56:    */     
/*  57:    */     public SpdyHeaders set(String name, Iterable<?> values)
/*  58:    */     {
/*  59: 78 */       throw new UnsupportedOperationException("read only");
/*  60:    */     }
/*  61:    */     
/*  62:    */     public SpdyHeaders remove(String name)
/*  63:    */     {
/*  64: 83 */       throw new UnsupportedOperationException("read only");
/*  65:    */     }
/*  66:    */     
/*  67:    */     public SpdyHeaders clear()
/*  68:    */     {
/*  69: 88 */       throw new UnsupportedOperationException("read only");
/*  70:    */     }
/*  71:    */     
/*  72:    */     public Iterator<Map.Entry<String, String>> iterator()
/*  73:    */     {
/*  74: 93 */       return entries().iterator();
/*  75:    */     }
/*  76:    */     
/*  77:    */     public String get(String name)
/*  78:    */     {
/*  79: 98 */       return null;
/*  80:    */     }
/*  81:    */   };
/*  82:    */   
/*  83:    */   public static String getHeader(SpdyHeadersFrame frame, String name)
/*  84:    */   {
/*  85:142 */     return frame.headers().get(name);
/*  86:    */   }
/*  87:    */   
/*  88:    */   public static String getHeader(SpdyHeadersFrame frame, String name, String defaultValue)
/*  89:    */   {
/*  90:154 */     String value = frame.headers().get(name);
/*  91:155 */     if (value == null) {
/*  92:156 */       return defaultValue;
/*  93:    */     }
/*  94:158 */     return value;
/*  95:    */   }
/*  96:    */   
/*  97:    */   public static void setHeader(SpdyHeadersFrame frame, String name, Object value)
/*  98:    */   {
/*  99:166 */     frame.headers().set(name, value);
/* 100:    */   }
/* 101:    */   
/* 102:    */   public static void setHeader(SpdyHeadersFrame frame, String name, Iterable<?> values)
/* 103:    */   {
/* 104:174 */     frame.headers().set(name, values);
/* 105:    */   }
/* 106:    */   
/* 107:    */   public static void addHeader(SpdyHeadersFrame frame, String name, Object value)
/* 108:    */   {
/* 109:181 */     frame.headers().add(name, value);
/* 110:    */   }
/* 111:    */   
/* 112:    */   public static void removeHost(SpdyHeadersFrame frame)
/* 113:    */   {
/* 114:188 */     frame.headers().remove(":host");
/* 115:    */   }
/* 116:    */   
/* 117:    */   public static String getHost(SpdyHeadersFrame frame)
/* 118:    */   {
/* 119:195 */     return frame.headers().get(":host");
/* 120:    */   }
/* 121:    */   
/* 122:    */   public static void setHost(SpdyHeadersFrame frame, String host)
/* 123:    */   {
/* 124:202 */     frame.headers().set(":host", host);
/* 125:    */   }
/* 126:    */   
/* 127:    */   public static void removeMethod(int spdyVersion, SpdyHeadersFrame frame)
/* 128:    */   {
/* 129:209 */     frame.headers().remove(":method");
/* 130:    */   }
/* 131:    */   
/* 132:    */   public static HttpMethod getMethod(int spdyVersion, SpdyHeadersFrame frame)
/* 133:    */   {
/* 134:    */     try
/* 135:    */     {
/* 136:217 */       return HttpMethod.valueOf(frame.headers().get(":method"));
/* 137:    */     }
/* 138:    */     catch (Exception e) {}
/* 139:219 */     return null;
/* 140:    */   }
/* 141:    */   
/* 142:    */   public static void setMethod(int spdyVersion, SpdyHeadersFrame frame, HttpMethod method)
/* 143:    */   {
/* 144:227 */     frame.headers().set(":method", method.name());
/* 145:    */   }
/* 146:    */   
/* 147:    */   public static void removeScheme(int spdyVersion, SpdyHeadersFrame frame)
/* 148:    */   {
/* 149:234 */     frame.headers().remove(":scheme");
/* 150:    */   }
/* 151:    */   
/* 152:    */   public static String getScheme(int spdyVersion, SpdyHeadersFrame frame)
/* 153:    */   {
/* 154:241 */     return frame.headers().get(":scheme");
/* 155:    */   }
/* 156:    */   
/* 157:    */   public static void setScheme(int spdyVersion, SpdyHeadersFrame frame, String scheme)
/* 158:    */   {
/* 159:248 */     frame.headers().set(":scheme", scheme);
/* 160:    */   }
/* 161:    */   
/* 162:    */   public static void removeStatus(int spdyVersion, SpdyHeadersFrame frame)
/* 163:    */   {
/* 164:255 */     frame.headers().remove(":status");
/* 165:    */   }
/* 166:    */   
/* 167:    */   public static HttpResponseStatus getStatus(int spdyVersion, SpdyHeadersFrame frame)
/* 168:    */   {
/* 169:    */     try
/* 170:    */     {
/* 171:263 */       String status = frame.headers().get(":status");
/* 172:264 */       int space = status.indexOf(' ');
/* 173:265 */       if (space == -1) {
/* 174:266 */         return HttpResponseStatus.valueOf(Integer.parseInt(status));
/* 175:    */       }
/* 176:268 */       int code = Integer.parseInt(status.substring(0, space));
/* 177:269 */       String reasonPhrase = status.substring(space + 1);
/* 178:270 */       HttpResponseStatus responseStatus = HttpResponseStatus.valueOf(code);
/* 179:271 */       if (responseStatus.reasonPhrase().equals(reasonPhrase)) {
/* 180:272 */         return responseStatus;
/* 181:    */       }
/* 182:274 */       return new HttpResponseStatus(code, reasonPhrase);
/* 183:    */     }
/* 184:    */     catch (Exception e) {}
/* 185:278 */     return null;
/* 186:    */   }
/* 187:    */   
/* 188:    */   public static void setStatus(int spdyVersion, SpdyHeadersFrame frame, HttpResponseStatus status)
/* 189:    */   {
/* 190:286 */     frame.headers().set(":status", status.toString());
/* 191:    */   }
/* 192:    */   
/* 193:    */   public static void removeUrl(int spdyVersion, SpdyHeadersFrame frame)
/* 194:    */   {
/* 195:293 */     frame.headers().remove(":path");
/* 196:    */   }
/* 197:    */   
/* 198:    */   public static String getUrl(int spdyVersion, SpdyHeadersFrame frame)
/* 199:    */   {
/* 200:300 */     return frame.headers().get(":path");
/* 201:    */   }
/* 202:    */   
/* 203:    */   public static void setUrl(int spdyVersion, SpdyHeadersFrame frame, String path)
/* 204:    */   {
/* 205:307 */     frame.headers().set(":path", path);
/* 206:    */   }
/* 207:    */   
/* 208:    */   public static void removeVersion(int spdyVersion, SpdyHeadersFrame frame)
/* 209:    */   {
/* 210:314 */     frame.headers().remove(":version");
/* 211:    */   }
/* 212:    */   
/* 213:    */   public static HttpVersion getVersion(int spdyVersion, SpdyHeadersFrame frame)
/* 214:    */   {
/* 215:    */     try
/* 216:    */     {
/* 217:322 */       return HttpVersion.valueOf(frame.headers().get(":version"));
/* 218:    */     }
/* 219:    */     catch (Exception e) {}
/* 220:324 */     return null;
/* 221:    */   }
/* 222:    */   
/* 223:    */   public static void setVersion(int spdyVersion, SpdyHeadersFrame frame, HttpVersion httpVersion)
/* 224:    */   {
/* 225:332 */     frame.headers().set(":version", httpVersion.text());
/* 226:    */   }
/* 227:    */   
/* 228:    */   public Iterator<Map.Entry<String, String>> iterator()
/* 229:    */   {
/* 230:337 */     return entries().iterator();
/* 231:    */   }
/* 232:    */   
/* 233:    */   public abstract String get(String paramString);
/* 234:    */   
/* 235:    */   public abstract List<String> getAll(String paramString);
/* 236:    */   
/* 237:    */   public abstract List<Map.Entry<String, String>> entries();
/* 238:    */   
/* 239:    */   public abstract boolean contains(String paramString);
/* 240:    */   
/* 241:    */   public abstract Set<String> names();
/* 242:    */   
/* 243:    */   public abstract SpdyHeaders add(String paramString, Object paramObject);
/* 244:    */   
/* 245:    */   public abstract SpdyHeaders add(String paramString, Iterable<?> paramIterable);
/* 246:    */   
/* 247:    */   public abstract SpdyHeaders set(String paramString, Object paramObject);
/* 248:    */   
/* 249:    */   public abstract SpdyHeaders set(String paramString, Iterable<?> paramIterable);
/* 250:    */   
/* 251:    */   public abstract SpdyHeaders remove(String paramString);
/* 252:    */   
/* 253:    */   public abstract SpdyHeaders clear();
/* 254:    */   
/* 255:    */   public abstract boolean isEmpty();
/* 256:    */   
/* 257:    */   public static final class HttpNames
/* 258:    */   {
/* 259:    */     public static final String HOST = ":host";
/* 260:    */     public static final String METHOD = ":method";
/* 261:    */     public static final String PATH = ":path";
/* 262:    */     public static final String SCHEME = ":scheme";
/* 263:    */     public static final String STATUS = ":status";
/* 264:    */     public static final String VERSION = ":version";
/* 265:    */   }
/* 266:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyHeaders
 * JD-Core Version:    0.7.0.1
 */