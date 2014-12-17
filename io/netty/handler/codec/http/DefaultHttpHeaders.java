/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import java.util.Arrays;
/*   5:    */ import java.util.Calendar;
/*   6:    */ import java.util.Date;
/*   7:    */ import java.util.Iterator;
/*   8:    */ import java.util.LinkedHashSet;
/*   9:    */ import java.util.LinkedList;
/*  10:    */ import java.util.List;
/*  11:    */ import java.util.Map.Entry;
/*  12:    */ import java.util.NoSuchElementException;
/*  13:    */ import java.util.Set;
/*  14:    */ 
/*  15:    */ public class DefaultHttpHeaders
/*  16:    */   extends HttpHeaders
/*  17:    */ {
/*  18:    */   private static final int BUCKET_SIZE = 17;
/*  19:    */   
/*  20:    */   private static int index(int hash)
/*  21:    */   {
/*  22: 37 */     return hash % 17;
/*  23:    */   }
/*  24:    */   
/*  25: 40 */   private final HeaderEntry[] entries = new HeaderEntry[17];
/*  26: 41 */   private final HeaderEntry head = new HeaderEntry();
/*  27:    */   protected final boolean validate;
/*  28:    */   
/*  29:    */   public DefaultHttpHeaders()
/*  30:    */   {
/*  31: 45 */     this(true);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public DefaultHttpHeaders(boolean validate)
/*  35:    */   {
/*  36: 49 */     this.validate = validate;
/*  37: 50 */     this.head.before = (this.head.after = this.head);
/*  38:    */   }
/*  39:    */   
/*  40:    */   void validateHeaderName0(CharSequence headerName)
/*  41:    */   {
/*  42: 54 */     validateHeaderName(headerName);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public HttpHeaders add(HttpHeaders headers)
/*  46:    */   {
/*  47: 59 */     if ((headers instanceof DefaultHttpHeaders))
/*  48:    */     {
/*  49: 60 */       DefaultHttpHeaders defaultHttpHeaders = (DefaultHttpHeaders)headers;
/*  50: 61 */       HeaderEntry e = defaultHttpHeaders.head.after;
/*  51: 62 */       while (e != defaultHttpHeaders.head)
/*  52:    */       {
/*  53: 63 */         add(e.key, e.value);
/*  54: 64 */         e = e.after;
/*  55:    */       }
/*  56: 66 */       return this;
/*  57:    */     }
/*  58: 68 */     return super.add(headers);
/*  59:    */   }
/*  60:    */   
/*  61:    */   public HttpHeaders set(HttpHeaders headers)
/*  62:    */   {
/*  63: 74 */     if ((headers instanceof DefaultHttpHeaders))
/*  64:    */     {
/*  65: 75 */       clear();
/*  66: 76 */       DefaultHttpHeaders defaultHttpHeaders = (DefaultHttpHeaders)headers;
/*  67: 77 */       HeaderEntry e = defaultHttpHeaders.head.after;
/*  68: 78 */       while (e != defaultHttpHeaders.head)
/*  69:    */       {
/*  70: 79 */         add(e.key, e.value);
/*  71: 80 */         e = e.after;
/*  72:    */       }
/*  73: 82 */       return this;
/*  74:    */     }
/*  75: 84 */     return super.set(headers);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public HttpHeaders add(String name, Object value)
/*  79:    */   {
/*  80: 90 */     return add(name, value);
/*  81:    */   }
/*  82:    */   
/*  83:    */   public HttpHeaders add(CharSequence name, Object value)
/*  84:    */   {
/*  85:    */     CharSequence strVal;
/*  86: 96 */     if (this.validate)
/*  87:    */     {
/*  88: 97 */       validateHeaderName0(name);
/*  89: 98 */       CharSequence strVal = toCharSequence(value);
/*  90: 99 */       validateHeaderValue(strVal);
/*  91:    */     }
/*  92:    */     else
/*  93:    */     {
/*  94:101 */       strVal = toCharSequence(value);
/*  95:    */     }
/*  96:103 */     int h = hash(name);
/*  97:104 */     int i = index(h);
/*  98:105 */     add0(h, i, name, strVal);
/*  99:106 */     return this;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public HttpHeaders add(String name, Iterable<?> values)
/* 103:    */   {
/* 104:111 */     return add(name, values);
/* 105:    */   }
/* 106:    */   
/* 107:    */   public HttpHeaders add(CharSequence name, Iterable<?> values)
/* 108:    */   {
/* 109:116 */     if (this.validate) {
/* 110:117 */       validateHeaderName0(name);
/* 111:    */     }
/* 112:119 */     int h = hash(name);
/* 113:120 */     int i = index(h);
/* 114:121 */     for (Object v : values)
/* 115:    */     {
/* 116:122 */       CharSequence vstr = toCharSequence(v);
/* 117:123 */       if (this.validate) {
/* 118:124 */         validateHeaderValue(vstr);
/* 119:    */       }
/* 120:126 */       add0(h, i, name, vstr);
/* 121:    */     }
/* 122:128 */     return this;
/* 123:    */   }
/* 124:    */   
/* 125:    */   private void add0(int h, int i, CharSequence name, CharSequence value)
/* 126:    */   {
/* 127:133 */     HeaderEntry e = this.entries[i]; void 
/* 128:    */     
/* 129:135 */       tmp25_22 = new HeaderEntry(h, name, value);HeaderEntry newEntry = tmp25_22;this.entries[i] = tmp25_22;
/* 130:136 */     newEntry.next = e;
/* 131:    */     
/* 132:    */ 
/* 133:139 */     newEntry.addBefore(this.head);
/* 134:    */   }
/* 135:    */   
/* 136:    */   public HttpHeaders remove(String name)
/* 137:    */   {
/* 138:144 */     return remove(name);
/* 139:    */   }
/* 140:    */   
/* 141:    */   public HttpHeaders remove(CharSequence name)
/* 142:    */   {
/* 143:149 */     if (name == null) {
/* 144:150 */       throw new NullPointerException("name");
/* 145:    */     }
/* 146:152 */     int h = hash(name);
/* 147:153 */     int i = index(h);
/* 148:154 */     remove0(h, i, name);
/* 149:155 */     return this;
/* 150:    */   }
/* 151:    */   
/* 152:    */   private void remove0(int h, int i, CharSequence name)
/* 153:    */   {
/* 154:159 */     HeaderEntry e = this.entries[i];
/* 155:160 */     if (e == null) {
/* 156:161 */       return;
/* 157:    */     }
/* 158:165 */     while ((e.hash == h) && (equalsIgnoreCase(name, e.key)))
/* 159:    */     {
/* 160:166 */       e.remove();
/* 161:167 */       HeaderEntry next = e.next;
/* 162:168 */       if (next != null)
/* 163:    */       {
/* 164:169 */         this.entries[i] = next;
/* 165:170 */         e = next;
/* 166:    */       }
/* 167:    */       else
/* 168:    */       {
/* 169:172 */         this.entries[i] = null; return;
/* 170:    */       }
/* 171:    */     }
/* 172:    */     for (;;)
/* 173:    */     {
/* 174:181 */       HeaderEntry next = e.next;
/* 175:182 */       if (next == null) {
/* 176:    */         break;
/* 177:    */       }
/* 178:185 */       if ((next.hash == h) && (equalsIgnoreCase(name, next.key)))
/* 179:    */       {
/* 180:186 */         e.next = next.next;
/* 181:187 */         next.remove();
/* 182:    */       }
/* 183:    */       else
/* 184:    */       {
/* 185:189 */         e = next;
/* 186:    */       }
/* 187:    */     }
/* 188:    */   }
/* 189:    */   
/* 190:    */   public HttpHeaders set(String name, Object value)
/* 191:    */   {
/* 192:196 */     return set(name, value);
/* 193:    */   }
/* 194:    */   
/* 195:    */   public HttpHeaders set(CharSequence name, Object value)
/* 196:    */   {
/* 197:    */     CharSequence strVal;
/* 198:202 */     if (this.validate)
/* 199:    */     {
/* 200:203 */       validateHeaderName0(name);
/* 201:204 */       CharSequence strVal = toCharSequence(value);
/* 202:205 */       validateHeaderValue(strVal);
/* 203:    */     }
/* 204:    */     else
/* 205:    */     {
/* 206:207 */       strVal = toCharSequence(value);
/* 207:    */     }
/* 208:209 */     int h = hash(name);
/* 209:210 */     int i = index(h);
/* 210:211 */     remove0(h, i, name);
/* 211:212 */     add0(h, i, name, strVal);
/* 212:213 */     return this;
/* 213:    */   }
/* 214:    */   
/* 215:    */   public HttpHeaders set(String name, Iterable<?> values)
/* 216:    */   {
/* 217:218 */     return set(name, values);
/* 218:    */   }
/* 219:    */   
/* 220:    */   public HttpHeaders set(CharSequence name, Iterable<?> values)
/* 221:    */   {
/* 222:223 */     if (values == null) {
/* 223:224 */       throw new NullPointerException("values");
/* 224:    */     }
/* 225:226 */     if (this.validate) {
/* 226:227 */       validateHeaderName0(name);
/* 227:    */     }
/* 228:230 */     int h = hash(name);
/* 229:231 */     int i = index(h);
/* 230:    */     
/* 231:233 */     remove0(h, i, name);
/* 232:234 */     for (Object v : values)
/* 233:    */     {
/* 234:235 */       if (v == null) {
/* 235:    */         break;
/* 236:    */       }
/* 237:238 */       CharSequence strVal = toCharSequence(v);
/* 238:239 */       if (this.validate) {
/* 239:240 */         validateHeaderValue(strVal);
/* 240:    */       }
/* 241:242 */       add0(h, i, name, strVal);
/* 242:    */     }
/* 243:245 */     return this;
/* 244:    */   }
/* 245:    */   
/* 246:    */   public HttpHeaders clear()
/* 247:    */   {
/* 248:250 */     Arrays.fill(this.entries, null);
/* 249:251 */     this.head.before = (this.head.after = this.head);
/* 250:252 */     return this;
/* 251:    */   }
/* 252:    */   
/* 253:    */   public String get(String name)
/* 254:    */   {
/* 255:257 */     return get(name);
/* 256:    */   }
/* 257:    */   
/* 258:    */   public String get(CharSequence name)
/* 259:    */   {
/* 260:262 */     if (name == null) {
/* 261:263 */       throw new NullPointerException("name");
/* 262:    */     }
/* 263:266 */     int h = hash(name);
/* 264:267 */     int i = index(h);
/* 265:268 */     HeaderEntry e = this.entries[i];
/* 266:269 */     CharSequence value = null;
/* 267:271 */     while (e != null)
/* 268:    */     {
/* 269:272 */       if ((e.hash == h) && (equalsIgnoreCase(name, e.key))) {
/* 270:273 */         value = e.value;
/* 271:    */       }
/* 272:276 */       e = e.next;
/* 273:    */     }
/* 274:278 */     if (value == null) {
/* 275:279 */       return null;
/* 276:    */     }
/* 277:281 */     return value.toString();
/* 278:    */   }
/* 279:    */   
/* 280:    */   public List<String> getAll(String name)
/* 281:    */   {
/* 282:286 */     return getAll(name);
/* 283:    */   }
/* 284:    */   
/* 285:    */   public List<String> getAll(CharSequence name)
/* 286:    */   {
/* 287:291 */     if (name == null) {
/* 288:292 */       throw new NullPointerException("name");
/* 289:    */     }
/* 290:295 */     LinkedList<String> values = new LinkedList();
/* 291:    */     
/* 292:297 */     int h = hash(name);
/* 293:298 */     int i = index(h);
/* 294:299 */     HeaderEntry e = this.entries[i];
/* 295:300 */     while (e != null)
/* 296:    */     {
/* 297:301 */       if ((e.hash == h) && (equalsIgnoreCase(name, e.key))) {
/* 298:302 */         values.addFirst(e.getValue());
/* 299:    */       }
/* 300:304 */       e = e.next;
/* 301:    */     }
/* 302:306 */     return values;
/* 303:    */   }
/* 304:    */   
/* 305:    */   public List<Map.Entry<String, String>> entries()
/* 306:    */   {
/* 307:311 */     List<Map.Entry<String, String>> all = new LinkedList();
/* 308:    */     
/* 309:    */ 
/* 310:314 */     HeaderEntry e = this.head.after;
/* 311:315 */     while (e != this.head)
/* 312:    */     {
/* 313:316 */       all.add(e);
/* 314:317 */       e = e.after;
/* 315:    */     }
/* 316:319 */     return all;
/* 317:    */   }
/* 318:    */   
/* 319:    */   public Iterator<Map.Entry<String, String>> iterator()
/* 320:    */   {
/* 321:324 */     return new HeaderIterator(null);
/* 322:    */   }
/* 323:    */   
/* 324:    */   public boolean contains(String name)
/* 325:    */   {
/* 326:329 */     return get(name) != null;
/* 327:    */   }
/* 328:    */   
/* 329:    */   public boolean contains(CharSequence name)
/* 330:    */   {
/* 331:334 */     return get(name) != null;
/* 332:    */   }
/* 333:    */   
/* 334:    */   public boolean isEmpty()
/* 335:    */   {
/* 336:339 */     return this.head == this.head.after;
/* 337:    */   }
/* 338:    */   
/* 339:    */   public boolean contains(String name, String value, boolean ignoreCaseValue)
/* 340:    */   {
/* 341:344 */     return contains(name, value, ignoreCaseValue);
/* 342:    */   }
/* 343:    */   
/* 344:    */   public boolean contains(CharSequence name, CharSequence value, boolean ignoreCaseValue)
/* 345:    */   {
/* 346:349 */     if (name == null) {
/* 347:350 */       throw new NullPointerException("name");
/* 348:    */     }
/* 349:353 */     int h = hash(name);
/* 350:354 */     int i = index(h);
/* 351:355 */     HeaderEntry e = this.entries[i];
/* 352:356 */     while (e != null)
/* 353:    */     {
/* 354:357 */       if ((e.hash == h) && (equalsIgnoreCase(name, e.key))) {
/* 355:358 */         if (ignoreCaseValue)
/* 356:    */         {
/* 357:359 */           if (equalsIgnoreCase(e.value, value)) {
/* 358:360 */             return true;
/* 359:    */           }
/* 360:    */         }
/* 361:363 */         else if (e.value.equals(value)) {
/* 362:364 */           return true;
/* 363:    */         }
/* 364:    */       }
/* 365:368 */       e = e.next;
/* 366:    */     }
/* 367:370 */     return false;
/* 368:    */   }
/* 369:    */   
/* 370:    */   public Set<String> names()
/* 371:    */   {
/* 372:375 */     Set<String> names = new LinkedHashSet();
/* 373:376 */     HeaderEntry e = this.head.after;
/* 374:377 */     while (e != this.head)
/* 375:    */     {
/* 376:378 */       names.add(e.getKey());
/* 377:379 */       e = e.after;
/* 378:    */     }
/* 379:381 */     return names;
/* 380:    */   }
/* 381:    */   
/* 382:    */   private static CharSequence toCharSequence(Object value)
/* 383:    */   {
/* 384:385 */     if (value == null) {
/* 385:386 */       return null;
/* 386:    */     }
/* 387:388 */     if ((value instanceof CharSequence)) {
/* 388:389 */       return (CharSequence)value;
/* 389:    */     }
/* 390:391 */     if ((value instanceof Number)) {
/* 391:392 */       return value.toString();
/* 392:    */     }
/* 393:394 */     if ((value instanceof Date)) {
/* 394:395 */       return HttpHeaderDateFormat.get().format((Date)value);
/* 395:    */     }
/* 396:397 */     if ((value instanceof Calendar)) {
/* 397:398 */       return HttpHeaderDateFormat.get().format(((Calendar)value).getTime());
/* 398:    */     }
/* 399:400 */     return value.toString();
/* 400:    */   }
/* 401:    */   
/* 402:    */   void encode(ByteBuf buf)
/* 403:    */   {
/* 404:404 */     HeaderEntry e = this.head.after;
/* 405:405 */     while (e != this.head)
/* 406:    */     {
/* 407:406 */       e.encode(buf);
/* 408:407 */       e = e.after;
/* 409:    */     }
/* 410:    */   }
/* 411:    */   
/* 412:    */   private final class HeaderIterator
/* 413:    */     implements Iterator<Map.Entry<String, String>>
/* 414:    */   {
/* 415:413 */     private DefaultHttpHeaders.HeaderEntry current = DefaultHttpHeaders.this.head;
/* 416:    */     
/* 417:    */     private HeaderIterator() {}
/* 418:    */     
/* 419:    */     public boolean hasNext()
/* 420:    */     {
/* 421:417 */       return this.current.after != DefaultHttpHeaders.this.head;
/* 422:    */     }
/* 423:    */     
/* 424:    */     public Map.Entry<String, String> next()
/* 425:    */     {
/* 426:422 */       this.current = this.current.after;
/* 427:424 */       if (this.current == DefaultHttpHeaders.this.head) {
/* 428:425 */         throw new NoSuchElementException();
/* 429:    */       }
/* 430:428 */       return this.current;
/* 431:    */     }
/* 432:    */     
/* 433:    */     public void remove()
/* 434:    */     {
/* 435:433 */       throw new UnsupportedOperationException();
/* 436:    */     }
/* 437:    */   }
/* 438:    */   
/* 439:    */   private final class HeaderEntry
/* 440:    */     implements Map.Entry<String, String>
/* 441:    */   {
/* 442:    */     final int hash;
/* 443:    */     final CharSequence key;
/* 444:    */     CharSequence value;
/* 445:    */     HeaderEntry next;
/* 446:    */     HeaderEntry before;
/* 447:    */     HeaderEntry after;
/* 448:    */     
/* 449:    */     HeaderEntry(int hash, CharSequence key, CharSequence value)
/* 450:    */     {
/* 451:445 */       this.hash = hash;
/* 452:446 */       this.key = key;
/* 453:447 */       this.value = value;
/* 454:    */     }
/* 455:    */     
/* 456:    */     HeaderEntry()
/* 457:    */     {
/* 458:451 */       this.hash = -1;
/* 459:452 */       this.key = null;
/* 460:453 */       this.value = null;
/* 461:    */     }
/* 462:    */     
/* 463:    */     void remove()
/* 464:    */     {
/* 465:457 */       this.before.after = this.after;
/* 466:458 */       this.after.before = this.before;
/* 467:    */     }
/* 468:    */     
/* 469:    */     void addBefore(HeaderEntry e)
/* 470:    */     {
/* 471:462 */       this.after = e;
/* 472:463 */       this.before = e.before;
/* 473:464 */       this.before.after = this;
/* 474:465 */       this.after.before = this;
/* 475:    */     }
/* 476:    */     
/* 477:    */     public String getKey()
/* 478:    */     {
/* 479:470 */       return this.key.toString();
/* 480:    */     }
/* 481:    */     
/* 482:    */     public String getValue()
/* 483:    */     {
/* 484:475 */       return this.value.toString();
/* 485:    */     }
/* 486:    */     
/* 487:    */     public String setValue(String value)
/* 488:    */     {
/* 489:480 */       if (value == null) {
/* 490:481 */         throw new NullPointerException("value");
/* 491:    */       }
/* 492:483 */       HttpHeaders.validateHeaderValue(value);
/* 493:484 */       CharSequence oldValue = this.value;
/* 494:485 */       this.value = value;
/* 495:486 */       return oldValue.toString();
/* 496:    */     }
/* 497:    */     
/* 498:    */     public String toString()
/* 499:    */     {
/* 500:491 */       return this.key.toString() + '=' + this.value.toString();
/* 501:    */     }
/* 502:    */     
/* 503:    */     void encode(ByteBuf buf)
/* 504:    */     {
/* 505:495 */       HttpHeaders.encode(this.key, this.value, buf);
/* 506:    */     }
/* 507:    */   }
/* 508:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.DefaultHttpHeaders
 * JD-Core Version:    0.7.0.1
 */