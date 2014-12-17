/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import java.util.Collections;
/*   4:    */ import java.util.Iterator;
/*   5:    */ import java.util.Set;
/*   6:    */ import java.util.TreeSet;
/*   7:    */ 
/*   8:    */ public class DefaultCookie
/*   9:    */   implements Cookie
/*  10:    */ {
/*  11:    */   private final String name;
/*  12:    */   private String value;
/*  13:    */   private String domain;
/*  14:    */   private String path;
/*  15:    */   private String comment;
/*  16:    */   private String commentUrl;
/*  17:    */   private boolean discard;
/*  18: 36 */   private Set<Integer> ports = Collections.emptySet();
/*  19: 37 */   private Set<Integer> unmodifiablePorts = this.ports;
/*  20: 38 */   private long maxAge = -9223372036854775808L;
/*  21:    */   private int version;
/*  22:    */   private boolean secure;
/*  23:    */   private boolean httpOnly;
/*  24:    */   
/*  25:    */   public DefaultCookie(String name, String value)
/*  26:    */   {
/*  27: 47 */     if (name == null) {
/*  28: 48 */       throw new NullPointerException("name");
/*  29:    */     }
/*  30: 50 */     name = name.trim();
/*  31: 51 */     if (name.isEmpty()) {
/*  32: 52 */       throw new IllegalArgumentException("empty name");
/*  33:    */     }
/*  34: 55 */     for (int i = 0; i < name.length(); i++)
/*  35:    */     {
/*  36: 56 */       char c = name.charAt(i);
/*  37: 57 */       if (c > '') {
/*  38: 58 */         throw new IllegalArgumentException("name contains non-ascii character: " + name);
/*  39:    */       }
/*  40: 63 */       switch (c)
/*  41:    */       {
/*  42:    */       case '\t': 
/*  43:    */       case '\n': 
/*  44:    */       case '\013': 
/*  45:    */       case '\f': 
/*  46:    */       case '\r': 
/*  47:    */       case ' ': 
/*  48:    */       case ',': 
/*  49:    */       case ';': 
/*  50:    */       case '=': 
/*  51: 66 */         throw new IllegalArgumentException("name contains one of the following prohibited characters: =,; \\t\\r\\n\\v\\f: " + name);
/*  52:    */       }
/*  53:    */     }
/*  54: 72 */     if (name.charAt(0) == '$') {
/*  55: 73 */       throw new IllegalArgumentException("name starting with '$' not allowed: " + name);
/*  56:    */     }
/*  57: 76 */     this.name = name;
/*  58: 77 */     setValue(value);
/*  59:    */   }
/*  60:    */   
/*  61:    */   public String getName()
/*  62:    */   {
/*  63: 82 */     return this.name;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public String getValue()
/*  67:    */   {
/*  68: 87 */     return this.value;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public void setValue(String value)
/*  72:    */   {
/*  73: 92 */     if (value == null) {
/*  74: 93 */       throw new NullPointerException("value");
/*  75:    */     }
/*  76: 95 */     this.value = value;
/*  77:    */   }
/*  78:    */   
/*  79:    */   public String getDomain()
/*  80:    */   {
/*  81:100 */     return this.domain;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public void setDomain(String domain)
/*  85:    */   {
/*  86:105 */     this.domain = validateValue("domain", domain);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public String getPath()
/*  90:    */   {
/*  91:110 */     return this.path;
/*  92:    */   }
/*  93:    */   
/*  94:    */   public void setPath(String path)
/*  95:    */   {
/*  96:115 */     this.path = validateValue("path", path);
/*  97:    */   }
/*  98:    */   
/*  99:    */   public String getComment()
/* 100:    */   {
/* 101:120 */     return this.comment;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public void setComment(String comment)
/* 105:    */   {
/* 106:125 */     this.comment = validateValue("comment", comment);
/* 107:    */   }
/* 108:    */   
/* 109:    */   public String getCommentUrl()
/* 110:    */   {
/* 111:130 */     return this.commentUrl;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public void setCommentUrl(String commentUrl)
/* 115:    */   {
/* 116:135 */     this.commentUrl = validateValue("commentUrl", commentUrl);
/* 117:    */   }
/* 118:    */   
/* 119:    */   public boolean isDiscard()
/* 120:    */   {
/* 121:140 */     return this.discard;
/* 122:    */   }
/* 123:    */   
/* 124:    */   public void setDiscard(boolean discard)
/* 125:    */   {
/* 126:145 */     this.discard = discard;
/* 127:    */   }
/* 128:    */   
/* 129:    */   public Set<Integer> getPorts()
/* 130:    */   {
/* 131:150 */     if (this.unmodifiablePorts == null) {
/* 132:151 */       this.unmodifiablePorts = Collections.unmodifiableSet(this.ports);
/* 133:    */     }
/* 134:153 */     return this.unmodifiablePorts;
/* 135:    */   }
/* 136:    */   
/* 137:    */   public void setPorts(int... ports)
/* 138:    */   {
/* 139:158 */     if (ports == null) {
/* 140:159 */       throw new NullPointerException("ports");
/* 141:    */     }
/* 142:162 */     int[] portsCopy = (int[])ports.clone();
/* 143:163 */     if (portsCopy.length == 0)
/* 144:    */     {
/* 145:164 */       this.unmodifiablePorts = (this.ports = Collections.emptySet());
/* 146:    */     }
/* 147:    */     else
/* 148:    */     {
/* 149:166 */       Set<Integer> newPorts = new TreeSet();
/* 150:167 */       for (int p : portsCopy)
/* 151:    */       {
/* 152:168 */         if ((p <= 0) || (p > 65535)) {
/* 153:169 */           throw new IllegalArgumentException("port out of range: " + p);
/* 154:    */         }
/* 155:171 */         newPorts.add(Integer.valueOf(p));
/* 156:    */       }
/* 157:173 */       this.ports = newPorts;
/* 158:174 */       this.unmodifiablePorts = null;
/* 159:    */     }
/* 160:    */   }
/* 161:    */   
/* 162:    */   public void setPorts(Iterable<Integer> ports)
/* 163:    */   {
/* 164:180 */     Set<Integer> newPorts = new TreeSet();
/* 165:181 */     for (Iterator i$ = ports.iterator(); i$.hasNext();)
/* 166:    */     {
/* 167:181 */       int p = ((Integer)i$.next()).intValue();
/* 168:182 */       if ((p <= 0) || (p > 65535)) {
/* 169:183 */         throw new IllegalArgumentException("port out of range: " + p);
/* 170:    */       }
/* 171:185 */       newPorts.add(Integer.valueOf(p));
/* 172:    */     }
/* 173:187 */     if (newPorts.isEmpty())
/* 174:    */     {
/* 175:188 */       this.unmodifiablePorts = (this.ports = Collections.emptySet());
/* 176:    */     }
/* 177:    */     else
/* 178:    */     {
/* 179:190 */       this.ports = newPorts;
/* 180:191 */       this.unmodifiablePorts = null;
/* 181:    */     }
/* 182:    */   }
/* 183:    */   
/* 184:    */   public long getMaxAge()
/* 185:    */   {
/* 186:197 */     return this.maxAge;
/* 187:    */   }
/* 188:    */   
/* 189:    */   public void setMaxAge(long maxAge)
/* 190:    */   {
/* 191:202 */     this.maxAge = maxAge;
/* 192:    */   }
/* 193:    */   
/* 194:    */   public int getVersion()
/* 195:    */   {
/* 196:207 */     return this.version;
/* 197:    */   }
/* 198:    */   
/* 199:    */   public void setVersion(int version)
/* 200:    */   {
/* 201:212 */     this.version = version;
/* 202:    */   }
/* 203:    */   
/* 204:    */   public boolean isSecure()
/* 205:    */   {
/* 206:217 */     return this.secure;
/* 207:    */   }
/* 208:    */   
/* 209:    */   public void setSecure(boolean secure)
/* 210:    */   {
/* 211:222 */     this.secure = secure;
/* 212:    */   }
/* 213:    */   
/* 214:    */   public boolean isHttpOnly()
/* 215:    */   {
/* 216:227 */     return this.httpOnly;
/* 217:    */   }
/* 218:    */   
/* 219:    */   public void setHttpOnly(boolean httpOnly)
/* 220:    */   {
/* 221:232 */     this.httpOnly = httpOnly;
/* 222:    */   }
/* 223:    */   
/* 224:    */   public int hashCode()
/* 225:    */   {
/* 226:237 */     return getName().hashCode();
/* 227:    */   }
/* 228:    */   
/* 229:    */   public boolean equals(Object o)
/* 230:    */   {
/* 231:242 */     if (!(o instanceof Cookie)) {
/* 232:243 */       return false;
/* 233:    */     }
/* 234:246 */     Cookie that = (Cookie)o;
/* 235:247 */     if (!getName().equalsIgnoreCase(that.getName())) {
/* 236:248 */       return false;
/* 237:    */     }
/* 238:251 */     if (getPath() == null)
/* 239:    */     {
/* 240:252 */       if (that.getPath() != null) {
/* 241:253 */         return false;
/* 242:    */       }
/* 243:    */     }
/* 244:    */     else
/* 245:    */     {
/* 246:255 */       if (that.getPath() == null) {
/* 247:256 */         return false;
/* 248:    */       }
/* 249:257 */       if (!getPath().equals(that.getPath())) {
/* 250:258 */         return false;
/* 251:    */       }
/* 252:    */     }
/* 253:261 */     if (getDomain() == null)
/* 254:    */     {
/* 255:262 */       if (that.getDomain() != null) {
/* 256:263 */         return false;
/* 257:    */       }
/* 258:    */     }
/* 259:    */     else
/* 260:    */     {
/* 261:265 */       if (that.getDomain() == null) {
/* 262:266 */         return false;
/* 263:    */       }
/* 264:268 */       return getDomain().equalsIgnoreCase(that.getDomain());
/* 265:    */     }
/* 266:271 */     return true;
/* 267:    */   }
/* 268:    */   
/* 269:    */   public int compareTo(Cookie c)
/* 270:    */   {
/* 271:277 */     int v = getName().compareToIgnoreCase(c.getName());
/* 272:278 */     if (v != 0) {
/* 273:279 */       return v;
/* 274:    */     }
/* 275:282 */     if (getPath() == null)
/* 276:    */     {
/* 277:283 */       if (c.getPath() != null) {
/* 278:284 */         return -1;
/* 279:    */       }
/* 280:    */     }
/* 281:    */     else
/* 282:    */     {
/* 283:286 */       if (c.getPath() == null) {
/* 284:287 */         return 1;
/* 285:    */       }
/* 286:289 */       v = getPath().compareTo(c.getPath());
/* 287:290 */       if (v != 0) {
/* 288:291 */         return v;
/* 289:    */       }
/* 290:    */     }
/* 291:295 */     if (getDomain() == null)
/* 292:    */     {
/* 293:296 */       if (c.getDomain() != null) {
/* 294:297 */         return -1;
/* 295:    */       }
/* 296:    */     }
/* 297:    */     else
/* 298:    */     {
/* 299:299 */       if (c.getDomain() == null) {
/* 300:300 */         return 1;
/* 301:    */       }
/* 302:302 */       v = getDomain().compareToIgnoreCase(c.getDomain());
/* 303:303 */       return v;
/* 304:    */     }
/* 305:306 */     return 0;
/* 306:    */   }
/* 307:    */   
/* 308:    */   public String toString()
/* 309:    */   {
/* 310:311 */     StringBuilder buf = new StringBuilder();
/* 311:312 */     buf.append(getName());
/* 312:313 */     buf.append('=');
/* 313:314 */     buf.append(getValue());
/* 314:315 */     if (getDomain() != null)
/* 315:    */     {
/* 316:316 */       buf.append(", domain=");
/* 317:317 */       buf.append(getDomain());
/* 318:    */     }
/* 319:319 */     if (getPath() != null)
/* 320:    */     {
/* 321:320 */       buf.append(", path=");
/* 322:321 */       buf.append(getPath());
/* 323:    */     }
/* 324:323 */     if (getComment() != null)
/* 325:    */     {
/* 326:324 */       buf.append(", comment=");
/* 327:325 */       buf.append(getComment());
/* 328:    */     }
/* 329:327 */     if (getMaxAge() >= 0L)
/* 330:    */     {
/* 331:328 */       buf.append(", maxAge=");
/* 332:329 */       buf.append(getMaxAge());
/* 333:330 */       buf.append('s');
/* 334:    */     }
/* 335:332 */     if (isSecure()) {
/* 336:333 */       buf.append(", secure");
/* 337:    */     }
/* 338:335 */     if (isHttpOnly()) {
/* 339:336 */       buf.append(", HTTPOnly");
/* 340:    */     }
/* 341:338 */     return buf.toString();
/* 342:    */   }
/* 343:    */   
/* 344:    */   private static String validateValue(String name, String value)
/* 345:    */   {
/* 346:342 */     if (value == null) {
/* 347:343 */       return null;
/* 348:    */     }
/* 349:345 */     value = value.trim();
/* 350:346 */     if (value.isEmpty()) {
/* 351:347 */       return null;
/* 352:    */     }
/* 353:349 */     for (int i = 0; i < value.length(); i++)
/* 354:    */     {
/* 355:350 */       char c = value.charAt(i);
/* 356:351 */       switch (c)
/* 357:    */       {
/* 358:    */       case '\n': 
/* 359:    */       case '\013': 
/* 360:    */       case '\f': 
/* 361:    */       case '\r': 
/* 362:    */       case ';': 
/* 363:353 */         throw new IllegalArgumentException(name + " contains one of the following prohibited characters: " + ";\\r\\n\\f\\v (" + value + ')');
/* 364:    */       }
/* 365:    */     }
/* 366:358 */     return value;
/* 367:    */   }
/* 368:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.DefaultCookie
 * JD-Core Version:    0.7.0.1
 */