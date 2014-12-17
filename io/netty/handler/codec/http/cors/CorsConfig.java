/*   1:    */ package io.netty.handler.codec.http.cors;
/*   2:    */ 
/*   3:    */ import io.netty.handler.codec.http.DefaultHttpHeaders;
/*   4:    */ import io.netty.handler.codec.http.HttpHeaders;
/*   5:    */ import io.netty.handler.codec.http.HttpMethod;
/*   6:    */ import io.netty.util.internal.StringUtil;
/*   7:    */ import java.util.Arrays;
/*   8:    */ import java.util.Collections;
/*   9:    */ import java.util.Date;
/*  10:    */ import java.util.HashMap;
/*  11:    */ import java.util.HashSet;
/*  12:    */ import java.util.Iterator;
/*  13:    */ import java.util.LinkedHashSet;
/*  14:    */ import java.util.Map;
/*  15:    */ import java.util.Map.Entry;
/*  16:    */ import java.util.Set;
/*  17:    */ import java.util.concurrent.Callable;
/*  18:    */ 
/*  19:    */ public final class CorsConfig
/*  20:    */ {
/*  21:    */   private final Set<String> origins;
/*  22:    */   private final boolean anyOrigin;
/*  23:    */   private final boolean enabled;
/*  24:    */   private final Set<String> exposeHeaders;
/*  25:    */   private final boolean allowCredentials;
/*  26:    */   private final long maxAge;
/*  27:    */   private final Set<HttpMethod> allowedRequestMethods;
/*  28:    */   private final Set<String> allowedRequestHeaders;
/*  29:    */   private final boolean allowNullOrigin;
/*  30:    */   private final Map<CharSequence, Callable<?>> preflightHeaders;
/*  31:    */   private final boolean shortCurcuit;
/*  32:    */   
/*  33:    */   private CorsConfig(Builder builder)
/*  34:    */   {
/*  35: 53 */     this.origins = new LinkedHashSet(builder.origins);
/*  36: 54 */     this.anyOrigin = builder.anyOrigin;
/*  37: 55 */     this.enabled = builder.enabled;
/*  38: 56 */     this.exposeHeaders = builder.exposeHeaders;
/*  39: 57 */     this.allowCredentials = builder.allowCredentials;
/*  40: 58 */     this.maxAge = builder.maxAge;
/*  41: 59 */     this.allowedRequestMethods = builder.requestMethods;
/*  42: 60 */     this.allowedRequestHeaders = builder.requestHeaders;
/*  43: 61 */     this.allowNullOrigin = builder.allowNullOrigin;
/*  44: 62 */     this.preflightHeaders = builder.preflightHeaders;
/*  45: 63 */     this.shortCurcuit = builder.shortCurcuit;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public boolean isCorsSupportEnabled()
/*  49:    */   {
/*  50: 72 */     return this.enabled;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public boolean isAnyOriginSupported()
/*  54:    */   {
/*  55: 81 */     return this.anyOrigin;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public String origin()
/*  59:    */   {
/*  60: 90 */     return this.origins.isEmpty() ? "*" : (String)this.origins.iterator().next();
/*  61:    */   }
/*  62:    */   
/*  63:    */   public Set<String> origins()
/*  64:    */   {
/*  65: 99 */     return this.origins;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public boolean isNullOriginAllowed()
/*  69:    */   {
/*  70:112 */     return this.allowNullOrigin;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public Set<String> exposedHeaders()
/*  74:    */   {
/*  75:138 */     return Collections.unmodifiableSet(this.exposeHeaders);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public boolean isCredentialsAllowed()
/*  79:    */   {
/*  80:159 */     return this.allowCredentials;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public long maxAge()
/*  84:    */   {
/*  85:173 */     return this.maxAge;
/*  86:    */   }
/*  87:    */   
/*  88:    */   public Set<HttpMethod> allowedRequestMethods()
/*  89:    */   {
/*  90:183 */     return Collections.unmodifiableSet(this.allowedRequestMethods);
/*  91:    */   }
/*  92:    */   
/*  93:    */   public Set<String> allowedRequestHeaders()
/*  94:    */   {
/*  95:195 */     return Collections.unmodifiableSet(this.allowedRequestHeaders);
/*  96:    */   }
/*  97:    */   
/*  98:    */   public HttpHeaders preflightResponseHeaders()
/*  99:    */   {
/* 100:204 */     if (this.preflightHeaders.isEmpty()) {
/* 101:205 */       return HttpHeaders.EMPTY_HEADERS;
/* 102:    */     }
/* 103:207 */     HttpHeaders preflightHeaders = new DefaultHttpHeaders();
/* 104:208 */     for (Map.Entry<CharSequence, Callable<?>> entry : this.preflightHeaders.entrySet())
/* 105:    */     {
/* 106:209 */       Object value = getValue((Callable)entry.getValue());
/* 107:210 */       if ((value instanceof Iterable)) {
/* 108:211 */         preflightHeaders.add((CharSequence)entry.getKey(), (Iterable)value);
/* 109:    */       } else {
/* 110:213 */         preflightHeaders.add((CharSequence)entry.getKey(), value);
/* 111:    */       }
/* 112:    */     }
/* 113:216 */     return preflightHeaders;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public boolean isShortCurcuit()
/* 117:    */   {
/* 118:230 */     return this.shortCurcuit;
/* 119:    */   }
/* 120:    */   
/* 121:    */   private static <T> T getValue(Callable<T> callable)
/* 122:    */   {
/* 123:    */     try
/* 124:    */     {
/* 125:235 */       return callable.call();
/* 126:    */     }
/* 127:    */     catch (Exception e)
/* 128:    */     {
/* 129:237 */       throw new IllegalStateException("Could not generate value for callable [" + callable + ']', e);
/* 130:    */     }
/* 131:    */   }
/* 132:    */   
/* 133:    */   public String toString()
/* 134:    */   {
/* 135:243 */     return StringUtil.simpleClassName(this) + "[enabled=" + this.enabled + ", origins=" + this.origins + ", anyOrigin=" + this.anyOrigin + ", exposedHeaders=" + this.exposeHeaders + ", isCredentialsAllowed=" + this.allowCredentials + ", maxAge=" + this.maxAge + ", allowedRequestMethods=" + this.allowedRequestMethods + ", allowedRequestHeaders=" + this.allowedRequestHeaders + ", preflightHeaders=" + this.preflightHeaders + ']';
/* 136:    */   }
/* 137:    */   
/* 138:    */   public static Builder withAnyOrigin()
/* 139:    */   {
/* 140:260 */     return new Builder();
/* 141:    */   }
/* 142:    */   
/* 143:    */   public static Builder withOrigin(String origin)
/* 144:    */   {
/* 145:269 */     if (origin.equals("*")) {
/* 146:270 */       return new Builder();
/* 147:    */     }
/* 148:272 */     return new Builder(new String[] { origin });
/* 149:    */   }
/* 150:    */   
/* 151:    */   public static Builder withOrigins(String... origins)
/* 152:    */   {
/* 153:281 */     return new Builder(origins);
/* 154:    */   }
/* 155:    */   
/* 156:    */   public static class Builder
/* 157:    */   {
/* 158:    */     private final Set<String> origins;
/* 159:    */     private final boolean anyOrigin;
/* 160:    */     private boolean allowNullOrigin;
/* 161:292 */     private boolean enabled = true;
/* 162:    */     private boolean allowCredentials;
/* 163:294 */     private final Set<String> exposeHeaders = new HashSet();
/* 164:    */     private long maxAge;
/* 165:296 */     private final Set<HttpMethod> requestMethods = new HashSet();
/* 166:297 */     private final Set<String> requestHeaders = new HashSet();
/* 167:298 */     private final Map<CharSequence, Callable<?>> preflightHeaders = new HashMap();
/* 168:    */     private boolean noPreflightHeaders;
/* 169:    */     private boolean shortCurcuit;
/* 170:    */     
/* 171:    */     public Builder(String... origins)
/* 172:    */     {
/* 173:308 */       this.origins = new LinkedHashSet(Arrays.asList(origins));
/* 174:309 */       this.anyOrigin = false;
/* 175:    */     }
/* 176:    */     
/* 177:    */     public Builder()
/* 178:    */     {
/* 179:318 */       this.anyOrigin = true;
/* 180:319 */       this.origins = Collections.emptySet();
/* 181:    */     }
/* 182:    */     
/* 183:    */     public Builder allowNullOrigin()
/* 184:    */     {
/* 185:330 */       this.allowNullOrigin = true;
/* 186:331 */       return this;
/* 187:    */     }
/* 188:    */     
/* 189:    */     public Builder disable()
/* 190:    */     {
/* 191:340 */       this.enabled = false;
/* 192:341 */       return this;
/* 193:    */     }
/* 194:    */     
/* 195:    */     public Builder exposeHeaders(String... headers)
/* 196:    */     {
/* 197:370 */       this.exposeHeaders.addAll(Arrays.asList(headers));
/* 198:371 */       return this;
/* 199:    */     }
/* 200:    */     
/* 201:    */     public Builder allowCredentials()
/* 202:    */     {
/* 203:390 */       this.allowCredentials = true;
/* 204:391 */       return this;
/* 205:    */     }
/* 206:    */     
/* 207:    */     public Builder maxAge(long max)
/* 208:    */     {
/* 209:404 */       this.maxAge = max;
/* 210:405 */       return this;
/* 211:    */     }
/* 212:    */     
/* 213:    */     public Builder allowedRequestMethods(HttpMethod... methods)
/* 214:    */     {
/* 215:416 */       this.requestMethods.addAll(Arrays.asList(methods));
/* 216:417 */       return this;
/* 217:    */     }
/* 218:    */     
/* 219:    */     public Builder allowedRequestHeaders(String... headers)
/* 220:    */     {
/* 221:437 */       this.requestHeaders.addAll(Arrays.asList(headers));
/* 222:438 */       return this;
/* 223:    */     }
/* 224:    */     
/* 225:    */     public Builder preflightResponseHeader(CharSequence name, Object... values)
/* 226:    */     {
/* 227:452 */       if (values.length == 1) {
/* 228:453 */         this.preflightHeaders.put(name, new CorsConfig.ConstantValueGenerator(values[0], null));
/* 229:    */       } else {
/* 230:455 */         preflightResponseHeader(name, Arrays.asList(values));
/* 231:    */       }
/* 232:457 */       return this;
/* 233:    */     }
/* 234:    */     
/* 235:    */     public <T> Builder preflightResponseHeader(CharSequence name, Iterable<T> value)
/* 236:    */     {
/* 237:472 */       this.preflightHeaders.put(name, new CorsConfig.ConstantValueGenerator(value, null));
/* 238:473 */       return this;
/* 239:    */     }
/* 240:    */     
/* 241:    */     public <T> Builder preflightResponseHeader(String name, Callable<T> valueGenerator)
/* 242:    */     {
/* 243:492 */       this.preflightHeaders.put(name, valueGenerator);
/* 244:493 */       return this;
/* 245:    */     }
/* 246:    */     
/* 247:    */     public Builder noPreflightResponseHeaders()
/* 248:    */     {
/* 249:502 */       this.noPreflightHeaders = true;
/* 250:503 */       return this;
/* 251:    */     }
/* 252:    */     
/* 253:    */     public CorsConfig build()
/* 254:    */     {
/* 255:512 */       if ((this.preflightHeaders.isEmpty()) && (!this.noPreflightHeaders))
/* 256:    */       {
/* 257:513 */         this.preflightHeaders.put("Date", new CorsConfig.DateValueGenerator());
/* 258:514 */         this.preflightHeaders.put("Content-Length", new CorsConfig.ConstantValueGenerator("0", null));
/* 259:    */       }
/* 260:516 */       return new CorsConfig(this, null);
/* 261:    */     }
/* 262:    */     
/* 263:    */     public Builder shortCurcuit()
/* 264:    */     {
/* 265:530 */       this.shortCurcuit = true;
/* 266:531 */       return this;
/* 267:    */     }
/* 268:    */   }
/* 269:    */   
/* 270:    */   private static final class ConstantValueGenerator
/* 271:    */     implements Callable<Object>
/* 272:    */   {
/* 273:    */     private final Object value;
/* 274:    */     
/* 275:    */     private ConstantValueGenerator(Object value)
/* 276:    */     {
/* 277:550 */       if (value == null) {
/* 278:551 */         throw new IllegalArgumentException("value must not be null");
/* 279:    */       }
/* 280:553 */       this.value = value;
/* 281:    */     }
/* 282:    */     
/* 283:    */     public Object call()
/* 284:    */     {
/* 285:558 */       return this.value;
/* 286:    */     }
/* 287:    */   }
/* 288:    */   
/* 289:    */   public static final class DateValueGenerator
/* 290:    */     implements Callable<Date>
/* 291:    */   {
/* 292:    */     public Date call()
/* 293:    */       throws Exception
/* 294:    */     {
/* 295:571 */       return new Date();
/* 296:    */     }
/* 297:    */   }
/* 298:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.cors.CorsConfig
 * JD-Core Version:    0.7.0.1
 */