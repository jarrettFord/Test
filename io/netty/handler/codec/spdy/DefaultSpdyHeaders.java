/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import java.util.Iterator;
/*   4:    */ import java.util.LinkedList;
/*   5:    */ import java.util.List;
/*   6:    */ import java.util.Map.Entry;
/*   7:    */ import java.util.NoSuchElementException;
/*   8:    */ import java.util.Set;
/*   9:    */ import java.util.TreeSet;
/*  10:    */ 
/*  11:    */ public class DefaultSpdyHeaders
/*  12:    */   extends SpdyHeaders
/*  13:    */ {
/*  14:    */   private static final int BUCKET_SIZE = 17;
/*  15:    */   
/*  16:    */   private static int hash(String name)
/*  17:    */   {
/*  18: 33 */     int h = 0;
/*  19: 34 */     for (int i = name.length() - 1; i >= 0; i--)
/*  20:    */     {
/*  21: 35 */       char c = name.charAt(i);
/*  22: 36 */       if ((c >= 'A') && (c <= 'Z')) {
/*  23: 37 */         c = (char)(c + ' ');
/*  24:    */       }
/*  25: 39 */       h = 31 * h + c;
/*  26:    */     }
/*  27: 42 */     if (h > 0) {
/*  28: 43 */       return h;
/*  29:    */     }
/*  30: 44 */     if (h == -2147483648) {
/*  31: 45 */       return 2147483647;
/*  32:    */     }
/*  33: 47 */     return -h;
/*  34:    */   }
/*  35:    */   
/*  36:    */   private static boolean eq(String name1, String name2)
/*  37:    */   {
/*  38: 52 */     int nameLen = name1.length();
/*  39: 53 */     if (nameLen != name2.length()) {
/*  40: 54 */       return false;
/*  41:    */     }
/*  42: 57 */     for (int i = nameLen - 1; i >= 0; i--)
/*  43:    */     {
/*  44: 58 */       char c1 = name1.charAt(i);
/*  45: 59 */       char c2 = name2.charAt(i);
/*  46: 60 */       if (c1 != c2)
/*  47:    */       {
/*  48: 61 */         if ((c1 >= 'A') && (c1 <= 'Z')) {
/*  49: 62 */           c1 = (char)(c1 + ' ');
/*  50:    */         }
/*  51: 64 */         if ((c2 >= 'A') && (c2 <= 'Z')) {
/*  52: 65 */           c2 = (char)(c2 + ' ');
/*  53:    */         }
/*  54: 67 */         if (c1 != c2) {
/*  55: 68 */           return false;
/*  56:    */         }
/*  57:    */       }
/*  58:    */     }
/*  59: 72 */     return true;
/*  60:    */   }
/*  61:    */   
/*  62:    */   private static int index(int hash)
/*  63:    */   {
/*  64: 76 */     return hash % 17;
/*  65:    */   }
/*  66:    */   
/*  67: 79 */   private final HeaderEntry[] entries = new HeaderEntry[17];
/*  68: 80 */   private final HeaderEntry head = new HeaderEntry(-1, null, null);
/*  69:    */   
/*  70:    */   DefaultSpdyHeaders()
/*  71:    */   {
/*  72: 83 */     this.head.before = (this.head.after = this.head);
/*  73:    */   }
/*  74:    */   
/*  75:    */   public SpdyHeaders add(String name, Object value)
/*  76:    */   {
/*  77: 88 */     String lowerCaseName = name.toLowerCase();
/*  78: 89 */     SpdyCodecUtil.validateHeaderName(lowerCaseName);
/*  79: 90 */     String strVal = toString(value);
/*  80: 91 */     SpdyCodecUtil.validateHeaderValue(strVal);
/*  81: 92 */     int h = hash(lowerCaseName);
/*  82: 93 */     int i = index(h);
/*  83: 94 */     add0(h, i, lowerCaseName, strVal);
/*  84: 95 */     return this;
/*  85:    */   }
/*  86:    */   
/*  87:    */   private void add0(int h, int i, String name, String value)
/*  88:    */   {
/*  89:100 */     HeaderEntry e = this.entries[i]; void 
/*  90:    */     
/*  91:102 */       tmp24_21 = new HeaderEntry(h, name, value);HeaderEntry newEntry = tmp24_21;this.entries[i] = tmp24_21;
/*  92:103 */     newEntry.next = e;
/*  93:    */     
/*  94:    */ 
/*  95:106 */     newEntry.addBefore(this.head);
/*  96:    */   }
/*  97:    */   
/*  98:    */   public SpdyHeaders remove(String name)
/*  99:    */   {
/* 100:111 */     if (name == null) {
/* 101:112 */       throw new NullPointerException("name");
/* 102:    */     }
/* 103:114 */     String lowerCaseName = name.toLowerCase();
/* 104:115 */     int h = hash(lowerCaseName);
/* 105:116 */     int i = index(h);
/* 106:117 */     remove0(h, i, lowerCaseName);
/* 107:118 */     return this;
/* 108:    */   }
/* 109:    */   
/* 110:    */   private void remove0(int h, int i, String name)
/* 111:    */   {
/* 112:122 */     HeaderEntry e = this.entries[i];
/* 113:123 */     if (e == null) {
/* 114:124 */       return;
/* 115:    */     }
/* 116:128 */     while ((e.hash == h) && (eq(name, e.key)))
/* 117:    */     {
/* 118:129 */       e.remove();
/* 119:130 */       HeaderEntry next = e.next;
/* 120:131 */       if (next != null)
/* 121:    */       {
/* 122:132 */         this.entries[i] = next;
/* 123:133 */         e = next;
/* 124:    */       }
/* 125:    */       else
/* 126:    */       {
/* 127:135 */         this.entries[i] = null; return;
/* 128:    */       }
/* 129:    */     }
/* 130:    */     for (;;)
/* 131:    */     {
/* 132:144 */       HeaderEntry next = e.next;
/* 133:145 */       if (next == null) {
/* 134:    */         break;
/* 135:    */       }
/* 136:148 */       if ((next.hash == h) && (eq(name, next.key)))
/* 137:    */       {
/* 138:149 */         e.next = next.next;
/* 139:150 */         next.remove();
/* 140:    */       }
/* 141:    */       else
/* 142:    */       {
/* 143:152 */         e = next;
/* 144:    */       }
/* 145:    */     }
/* 146:    */   }
/* 147:    */   
/* 148:    */   public SpdyHeaders set(String name, Object value)
/* 149:    */   {
/* 150:159 */     String lowerCaseName = name.toLowerCase();
/* 151:160 */     SpdyCodecUtil.validateHeaderName(lowerCaseName);
/* 152:161 */     String strVal = toString(value);
/* 153:162 */     SpdyCodecUtil.validateHeaderValue(strVal);
/* 154:163 */     int h = hash(lowerCaseName);
/* 155:164 */     int i = index(h);
/* 156:165 */     remove0(h, i, lowerCaseName);
/* 157:166 */     add0(h, i, lowerCaseName, strVal);
/* 158:167 */     return this;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public SpdyHeaders set(String name, Iterable<?> values)
/* 162:    */   {
/* 163:172 */     if (values == null) {
/* 164:173 */       throw new NullPointerException("values");
/* 165:    */     }
/* 166:176 */     String lowerCaseName = name.toLowerCase();
/* 167:177 */     SpdyCodecUtil.validateHeaderName(lowerCaseName);
/* 168:    */     
/* 169:179 */     int h = hash(lowerCaseName);
/* 170:180 */     int i = index(h);
/* 171:    */     
/* 172:182 */     remove0(h, i, lowerCaseName);
/* 173:183 */     for (Object v : values)
/* 174:    */     {
/* 175:184 */       if (v == null) {
/* 176:    */         break;
/* 177:    */       }
/* 178:187 */       String strVal = toString(v);
/* 179:188 */       SpdyCodecUtil.validateHeaderValue(strVal);
/* 180:189 */       add0(h, i, lowerCaseName, strVal);
/* 181:    */     }
/* 182:191 */     return this;
/* 183:    */   }
/* 184:    */   
/* 185:    */   public SpdyHeaders clear()
/* 186:    */   {
/* 187:196 */     for (int i = 0; i < this.entries.length; i++) {
/* 188:197 */       this.entries[i] = null;
/* 189:    */     }
/* 190:199 */     this.head.before = (this.head.after = this.head);
/* 191:200 */     return this;
/* 192:    */   }
/* 193:    */   
/* 194:    */   public String get(String name)
/* 195:    */   {
/* 196:205 */     if (name == null) {
/* 197:206 */       throw new NullPointerException("name");
/* 198:    */     }
/* 199:209 */     int h = hash(name);
/* 200:210 */     int i = index(h);
/* 201:211 */     HeaderEntry e = this.entries[i];
/* 202:212 */     while (e != null)
/* 203:    */     {
/* 204:213 */       if ((e.hash == h) && (eq(name, e.key))) {
/* 205:214 */         return e.value;
/* 206:    */       }
/* 207:217 */       e = e.next;
/* 208:    */     }
/* 209:219 */     return null;
/* 210:    */   }
/* 211:    */   
/* 212:    */   public List<String> getAll(String name)
/* 213:    */   {
/* 214:224 */     if (name == null) {
/* 215:225 */       throw new NullPointerException("name");
/* 216:    */     }
/* 217:228 */     LinkedList<String> values = new LinkedList();
/* 218:    */     
/* 219:230 */     int h = hash(name);
/* 220:231 */     int i = index(h);
/* 221:232 */     HeaderEntry e = this.entries[i];
/* 222:233 */     while (e != null)
/* 223:    */     {
/* 224:234 */       if ((e.hash == h) && (eq(name, e.key))) {
/* 225:235 */         values.addFirst(e.value);
/* 226:    */       }
/* 227:237 */       e = e.next;
/* 228:    */     }
/* 229:239 */     return values;
/* 230:    */   }
/* 231:    */   
/* 232:    */   public List<Map.Entry<String, String>> entries()
/* 233:    */   {
/* 234:244 */     List<Map.Entry<String, String>> all = new LinkedList();
/* 235:    */     
/* 236:    */ 
/* 237:247 */     HeaderEntry e = this.head.after;
/* 238:248 */     while (e != this.head)
/* 239:    */     {
/* 240:249 */       all.add(e);
/* 241:250 */       e = e.after;
/* 242:    */     }
/* 243:252 */     return all;
/* 244:    */   }
/* 245:    */   
/* 246:    */   public Iterator<Map.Entry<String, String>> iterator()
/* 247:    */   {
/* 248:257 */     return new HeaderIterator(null);
/* 249:    */   }
/* 250:    */   
/* 251:    */   public boolean contains(String name)
/* 252:    */   {
/* 253:262 */     return get(name) != null;
/* 254:    */   }
/* 255:    */   
/* 256:    */   public Set<String> names()
/* 257:    */   {
/* 258:267 */     Set<String> names = new TreeSet();
/* 259:    */     
/* 260:269 */     HeaderEntry e = this.head.after;
/* 261:270 */     while (e != this.head)
/* 262:    */     {
/* 263:271 */       names.add(e.key);
/* 264:272 */       e = e.after;
/* 265:    */     }
/* 266:274 */     return names;
/* 267:    */   }
/* 268:    */   
/* 269:    */   public SpdyHeaders add(String name, Iterable<?> values)
/* 270:    */   {
/* 271:279 */     SpdyCodecUtil.validateHeaderValue(name);
/* 272:280 */     int h = hash(name);
/* 273:281 */     int i = index(h);
/* 274:282 */     for (Object v : values)
/* 275:    */     {
/* 276:283 */       String vstr = toString(v);
/* 277:284 */       SpdyCodecUtil.validateHeaderValue(vstr);
/* 278:285 */       add0(h, i, name, vstr);
/* 279:    */     }
/* 280:287 */     return this;
/* 281:    */   }
/* 282:    */   
/* 283:    */   public boolean isEmpty()
/* 284:    */   {
/* 285:292 */     return this.head == this.head.after;
/* 286:    */   }
/* 287:    */   
/* 288:    */   private static String toString(Object value)
/* 289:    */   {
/* 290:296 */     if (value == null) {
/* 291:297 */       return null;
/* 292:    */     }
/* 293:299 */     return value.toString();
/* 294:    */   }
/* 295:    */   
/* 296:    */   private final class HeaderIterator
/* 297:    */     implements Iterator<Map.Entry<String, String>>
/* 298:    */   {
/* 299:304 */     private DefaultSpdyHeaders.HeaderEntry current = DefaultSpdyHeaders.this.head;
/* 300:    */     
/* 301:    */     private HeaderIterator() {}
/* 302:    */     
/* 303:    */     public boolean hasNext()
/* 304:    */     {
/* 305:308 */       return this.current.after != DefaultSpdyHeaders.this.head;
/* 306:    */     }
/* 307:    */     
/* 308:    */     public Map.Entry<String, String> next()
/* 309:    */     {
/* 310:313 */       this.current = this.current.after;
/* 311:315 */       if (this.current == DefaultSpdyHeaders.this.head) {
/* 312:316 */         throw new NoSuchElementException();
/* 313:    */       }
/* 314:319 */       return this.current;
/* 315:    */     }
/* 316:    */     
/* 317:    */     public void remove()
/* 318:    */     {
/* 319:324 */       throw new UnsupportedOperationException();
/* 320:    */     }
/* 321:    */   }
/* 322:    */   
/* 323:    */   private static final class HeaderEntry
/* 324:    */     implements Map.Entry<String, String>
/* 325:    */   {
/* 326:    */     final int hash;
/* 327:    */     final String key;
/* 328:    */     String value;
/* 329:    */     HeaderEntry next;
/* 330:    */     HeaderEntry before;
/* 331:    */     HeaderEntry after;
/* 332:    */     
/* 333:    */     HeaderEntry(int hash, String key, String value)
/* 334:    */     {
/* 335:336 */       this.hash = hash;
/* 336:337 */       this.key = key;
/* 337:338 */       this.value = value;
/* 338:    */     }
/* 339:    */     
/* 340:    */     void remove()
/* 341:    */     {
/* 342:342 */       this.before.after = this.after;
/* 343:343 */       this.after.before = this.before;
/* 344:    */     }
/* 345:    */     
/* 346:    */     void addBefore(HeaderEntry e)
/* 347:    */     {
/* 348:347 */       this.after = e;
/* 349:348 */       this.before = e.before;
/* 350:349 */       this.before.after = this;
/* 351:350 */       this.after.before = this;
/* 352:    */     }
/* 353:    */     
/* 354:    */     public String getKey()
/* 355:    */     {
/* 356:355 */       return this.key;
/* 357:    */     }
/* 358:    */     
/* 359:    */     public String getValue()
/* 360:    */     {
/* 361:360 */       return this.value;
/* 362:    */     }
/* 363:    */     
/* 364:    */     public String setValue(String value)
/* 365:    */     {
/* 366:365 */       if (value == null) {
/* 367:366 */         throw new NullPointerException("value");
/* 368:    */       }
/* 369:368 */       SpdyCodecUtil.validateHeaderValue(value);
/* 370:369 */       String oldValue = this.value;
/* 371:370 */       this.value = value;
/* 372:371 */       return oldValue;
/* 373:    */     }
/* 374:    */     
/* 375:    */     public String toString()
/* 376:    */     {
/* 377:376 */       return this.key + '=' + this.value;
/* 378:    */     }
/* 379:    */   }
/* 380:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.DefaultSpdyHeaders
 * JD-Core Version:    0.7.0.1
 */