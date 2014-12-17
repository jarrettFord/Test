/*   1:    */ package io.netty.util.collection;
/*   2:    */ 
/*   3:    */ import java.lang.reflect.Array;
/*   4:    */ import java.util.Arrays;
/*   5:    */ import java.util.Iterator;
/*   6:    */ import java.util.NoSuchElementException;
/*   7:    */ 
/*   8:    */ public class IntObjectHashMap<V>
/*   9:    */   implements IntObjectMap<V>, Iterable<IntObjectMap.Entry<V>>
/*  10:    */ {
/*  11:    */   private static final byte AVAILABLE = 0;
/*  12:    */   private static final byte OCCUPIED = 1;
/*  13:    */   private static final byte REMOVED = 2;
/*  14:    */   private static final int DEFAULT_CAPACITY = 11;
/*  15:    */   private static final float DEFAULT_LOAD_FACTOR = 0.5F;
/*  16:    */   private int maxSize;
/*  17:    */   private final float loadFactor;
/*  18:    */   private byte[] states;
/*  19:    */   private int[] keys;
/*  20:    */   private V[] values;
/*  21:    */   private int size;
/*  22:    */   private int available;
/*  23:    */   
/*  24:    */   public IntObjectHashMap()
/*  25:    */   {
/*  26: 60 */     this(11, 0.5F);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public IntObjectHashMap(int initialCapacity)
/*  30:    */   {
/*  31: 64 */     this(initialCapacity, 0.5F);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public IntObjectHashMap(int initialCapacity, float loadFactor)
/*  35:    */   {
/*  36: 68 */     if (initialCapacity < 1) {
/*  37: 69 */       throw new IllegalArgumentException("initialCapacity must be >= 1");
/*  38:    */     }
/*  39: 71 */     if (loadFactor <= 0.0F) {
/*  40: 72 */       throw new IllegalArgumentException("loadFactor must be > 0");
/*  41:    */     }
/*  42: 75 */     this.loadFactor = loadFactor;
/*  43:    */     
/*  44:    */ 
/*  45: 78 */     this.states = new byte[initialCapacity];
/*  46: 79 */     this.keys = new int[initialCapacity];
/*  47:    */     
/*  48: 81 */     V[] temp = (Object[])new Object[initialCapacity];
/*  49: 82 */     this.values = temp;
/*  50:    */     
/*  51:    */ 
/*  52: 85 */     this.maxSize = calcMaxSize(initialCapacity);
/*  53:    */     
/*  54:    */ 
/*  55: 88 */     this.available = (initialCapacity - this.size);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public V get(int key)
/*  59:    */   {
/*  60: 93 */     int index = indexOf(key);
/*  61: 94 */     return index < 0 ? null : this.values[index];
/*  62:    */   }
/*  63:    */   
/*  64:    */   public V put(int key, V value)
/*  65:    */   {
/*  66: 99 */     int hash = hash(key);
/*  67:100 */     int capacity = capacity();
/*  68:101 */     int index = hash % capacity;
/*  69:102 */     int increment = 1 + hash % (capacity - 2);
/*  70:103 */     int startIndex = index;
/*  71:104 */     int firstRemovedIndex = -1;
/*  72:    */     do
/*  73:    */     {
/*  74:106 */       switch (this.states[index])
/*  75:    */       {
/*  76:    */       case 0: 
/*  77:111 */         if (firstRemovedIndex != -1)
/*  78:    */         {
/*  79:115 */           insertAt(firstRemovedIndex, key, value);
/*  80:116 */           return null;
/*  81:    */         }
/*  82:120 */         insertAt(index, key, value);
/*  83:121 */         return null;
/*  84:    */       case 1: 
/*  85:123 */         if (this.keys[index] == key)
/*  86:    */         {
/*  87:124 */           V previousValue = this.values[index];
/*  88:125 */           insertAt(index, key, value);
/*  89:126 */           return previousValue;
/*  90:    */         }
/*  91:    */         break;
/*  92:    */       case 2: 
/*  93:131 */         if (firstRemovedIndex == -1) {
/*  94:132 */           firstRemovedIndex = index;
/*  95:    */         }
/*  96:    */         break;
/*  97:    */       default: 
/*  98:136 */         throw new AssertionError("Invalid state: " + this.states[index]);
/*  99:    */       }
/* 100:140 */       index += increment;
/* 101:141 */       if (index >= capacity) {
/* 102:143 */         index -= capacity;
/* 103:    */       }
/* 104:145 */     } while (index != startIndex);
/* 105:147 */     if (firstRemovedIndex == -1) {
/* 106:149 */       throw new AssertionError("Unable to insert");
/* 107:    */     }
/* 108:153 */     insertAt(firstRemovedIndex, key, value);
/* 109:154 */     return null;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public void putAll(IntObjectMap<V> sourceMap)
/* 113:    */   {
/* 114:159 */     if ((sourceMap instanceof IntObjectHashMap))
/* 115:    */     {
/* 116:161 */       IntObjectHashMap<V> source = (IntObjectHashMap)sourceMap;
/* 117:162 */       int i = -1;
/* 118:163 */       while ((i = source.nextEntryIndex(i + 1)) >= 0) {
/* 119:164 */         put(source.keys[i], source.values[i]);
/* 120:    */       }
/* 121:166 */       return;
/* 122:    */     }
/* 123:170 */     for (IntObjectMap.Entry<V> entry : sourceMap.entries()) {
/* 124:171 */       put(entry.key(), entry.value());
/* 125:    */     }
/* 126:    */   }
/* 127:    */   
/* 128:    */   public V remove(int key)
/* 129:    */   {
/* 130:177 */     int index = indexOf(key);
/* 131:178 */     if (index < 0) {
/* 132:179 */       return null;
/* 133:    */     }
/* 134:182 */     V prev = this.values[index];
/* 135:183 */     removeAt(index);
/* 136:184 */     return prev;
/* 137:    */   }
/* 138:    */   
/* 139:    */   public int size()
/* 140:    */   {
/* 141:189 */     return this.size;
/* 142:    */   }
/* 143:    */   
/* 144:    */   public boolean isEmpty()
/* 145:    */   {
/* 146:194 */     return this.size == 0;
/* 147:    */   }
/* 148:    */   
/* 149:    */   public void clear()
/* 150:    */   {
/* 151:199 */     Arrays.fill(this.states, (byte)0);
/* 152:200 */     Arrays.fill(this.values, null);
/* 153:201 */     this.size = 0;
/* 154:202 */     this.available = capacity();
/* 155:    */   }
/* 156:    */   
/* 157:    */   public boolean containsKey(int key)
/* 158:    */   {
/* 159:207 */     return indexOf(key) >= 0;
/* 160:    */   }
/* 161:    */   
/* 162:    */   public boolean containsValue(V value)
/* 163:    */   {
/* 164:212 */     int i = -1;
/* 165:213 */     while ((i = nextEntryIndex(i + 1)) >= 0)
/* 166:    */     {
/* 167:214 */       V next = this.values[i];
/* 168:215 */       if ((value == next) || ((value != null) && (value.equals(next)))) {
/* 169:216 */         return true;
/* 170:    */       }
/* 171:    */     }
/* 172:219 */     return false;
/* 173:    */   }
/* 174:    */   
/* 175:    */   public Iterable<IntObjectMap.Entry<V>> entries()
/* 176:    */   {
/* 177:224 */     return this;
/* 178:    */   }
/* 179:    */   
/* 180:    */   public Iterator<IntObjectMap.Entry<V>> iterator()
/* 181:    */   {
/* 182:229 */     return new IteratorImpl();
/* 183:    */   }
/* 184:    */   
/* 185:    */   public int[] keys()
/* 186:    */   {
/* 187:234 */     int[] outKeys = new int[size()];
/* 188:235 */     copyEntries(this.keys, outKeys);
/* 189:236 */     return outKeys;
/* 190:    */   }
/* 191:    */   
/* 192:    */   public V[] values(Class<V> clazz)
/* 193:    */   {
/* 194:242 */     V[] outValues = (Object[])Array.newInstance(clazz, size());
/* 195:243 */     copyEntries(this.values, outValues);
/* 196:244 */     return outValues;
/* 197:    */   }
/* 198:    */   
/* 199:    */   private void copyEntries(Object sourceArray, Object targetArray)
/* 200:    */   {
/* 201:251 */     int sourceIx = -1;
/* 202:252 */     int targetIx = 0;
/* 203:253 */     while ((sourceIx = nextEntryIndex(sourceIx + 1)) >= 0)
/* 204:    */     {
/* 205:254 */       Object obj = Array.get(sourceArray, sourceIx);
/* 206:255 */       Array.set(targetArray, targetIx++, obj);
/* 207:    */     }
/* 208:    */   }
/* 209:    */   
/* 210:    */   private int indexOf(int key)
/* 211:    */   {
/* 212:266 */     int hash = hash(key);
/* 213:267 */     int capacity = capacity();
/* 214:268 */     int increment = 1 + hash % (capacity - 2);
/* 215:269 */     int index = hash % capacity;
/* 216:270 */     int startIndex = index;
/* 217:    */     do
/* 218:    */     {
/* 219:272 */       switch (this.states[index])
/* 220:    */       {
/* 221:    */       case 0: 
/* 222:275 */         return -1;
/* 223:    */       case 1: 
/* 224:277 */         if (key == this.keys[index]) {
/* 225:279 */           return index;
/* 226:    */         }
/* 227:    */         break;
/* 228:    */       }
/* 229:287 */       index += increment;
/* 230:288 */       if (index >= capacity) {
/* 231:290 */         index -= capacity;
/* 232:    */       }
/* 233:292 */     } while (index != startIndex);
/* 234:295 */     return -1;
/* 235:    */   }
/* 236:    */   
/* 237:    */   private int capacity()
/* 238:    */   {
/* 239:302 */     return this.keys.length;
/* 240:    */   }
/* 241:    */   
/* 242:    */   private int hash(int key)
/* 243:    */   {
/* 244:310 */     return key & 0x7FFFFFFF;
/* 245:    */   }
/* 246:    */   
/* 247:    */   private void insertAt(int index, int key, V value)
/* 248:    */   {
/* 249:322 */     byte state = this.states[index];
/* 250:323 */     if (state != 1)
/* 251:    */     {
/* 252:325 */       this.size += 1;
/* 253:327 */       if (state == 0) {
/* 254:329 */         this.available -= 1;
/* 255:    */       }
/* 256:    */     }
/* 257:333 */     this.keys[index] = key;
/* 258:334 */     this.values[index] = value;
/* 259:335 */     this.states[index] = 1;
/* 260:337 */     if (this.size > this.maxSize) {
/* 261:340 */       rehash(capacity() * 2);
/* 262:341 */     } else if (this.available == 0) {
/* 263:344 */       rehash(capacity());
/* 264:    */     }
/* 265:    */   }
/* 266:    */   
/* 267:    */   private void removeAt(int index)
/* 268:    */   {
/* 269:357 */     if (this.states[index] == 1) {
/* 270:358 */       this.size -= 1;
/* 271:    */     }
/* 272:360 */     this.states[index] = 2;
/* 273:361 */     this.values[index] = null;
/* 274:    */   }
/* 275:    */   
/* 276:    */   private int calcMaxSize(int capacity)
/* 277:    */   {
/* 278:370 */     int upperBound = capacity - 1;
/* 279:371 */     return Math.min(upperBound, (int)(capacity * this.loadFactor));
/* 280:    */   }
/* 281:    */   
/* 282:    */   private void rehash(int newCapacity)
/* 283:    */   {
/* 284:380 */     int oldCapacity = capacity();
/* 285:381 */     int[] oldKeys = this.keys;
/* 286:382 */     V[] oldVals = this.values;
/* 287:383 */     byte[] oldStates = this.states;
/* 288:    */     
/* 289:    */ 
/* 290:386 */     this.states = new byte[newCapacity];
/* 291:387 */     this.keys = new int[newCapacity];
/* 292:    */     
/* 293:389 */     V[] temp = (Object[])new Object[newCapacity];
/* 294:390 */     this.values = temp;
/* 295:    */     
/* 296:392 */     this.size = 0;
/* 297:393 */     this.available = newCapacity;
/* 298:394 */     this.maxSize = calcMaxSize(newCapacity);
/* 299:397 */     for (int i = 0; i < oldCapacity; i++) {
/* 300:398 */       if (oldStates[i] == 1) {
/* 301:399 */         put(oldKeys[i], oldVals[i]);
/* 302:    */       }
/* 303:    */     }
/* 304:    */   }
/* 305:    */   
/* 306:    */   private int nextEntryIndex(int index)
/* 307:    */   {
/* 308:411 */     int capacity = capacity();
/* 309:412 */     for (; index < capacity; index++) {
/* 310:413 */       if (this.states[index] == 1) {
/* 311:414 */         return index;
/* 312:    */       }
/* 313:    */     }
/* 314:417 */     return -1;
/* 315:    */   }
/* 316:    */   
/* 317:    */   private final class IteratorImpl
/* 318:    */     implements Iterator<IntObjectMap.Entry<V>>
/* 319:    */   {
/* 320:    */     int prevIndex;
/* 321:    */     int nextIndex;
/* 322:    */     
/* 323:    */     IteratorImpl()
/* 324:    */     {
/* 325:428 */       this.prevIndex = -1;
/* 326:429 */       this.nextIndex = IntObjectHashMap.this.nextEntryIndex(0);
/* 327:    */     }
/* 328:    */     
/* 329:    */     public boolean hasNext()
/* 330:    */     {
/* 331:434 */       return this.nextIndex >= 0;
/* 332:    */     }
/* 333:    */     
/* 334:    */     public IntObjectMap.Entry<V> next()
/* 335:    */     {
/* 336:439 */       if (!hasNext()) {
/* 337:440 */         throw new NoSuchElementException();
/* 338:    */       }
/* 339:443 */       this.prevIndex = this.nextIndex;
/* 340:444 */       this.nextIndex = IntObjectHashMap.this.nextEntryIndex(this.nextIndex + 1);
/* 341:445 */       return new IntObjectHashMap.EntryImpl(IntObjectHashMap.this, this.prevIndex);
/* 342:    */     }
/* 343:    */     
/* 344:    */     public void remove()
/* 345:    */     {
/* 346:450 */       if (this.prevIndex < 0) {
/* 347:451 */         throw new IllegalStateException("Next must be called before removing.");
/* 348:    */       }
/* 349:453 */       IntObjectHashMap.this.removeAt(this.prevIndex);
/* 350:454 */       this.prevIndex = -1;
/* 351:    */     }
/* 352:    */   }
/* 353:    */   
/* 354:    */   private final class EntryImpl
/* 355:    */     implements IntObjectMap.Entry<V>
/* 356:    */   {
/* 357:    */     final int index;
/* 358:    */     
/* 359:    */     EntryImpl(int index)
/* 360:    */     {
/* 361:465 */       this.index = index;
/* 362:    */     }
/* 363:    */     
/* 364:    */     public int key()
/* 365:    */     {
/* 366:470 */       return IntObjectHashMap.this.keys[this.index];
/* 367:    */     }
/* 368:    */     
/* 369:    */     public V value()
/* 370:    */     {
/* 371:475 */       return IntObjectHashMap.this.values[this.index];
/* 372:    */     }
/* 373:    */     
/* 374:    */     public void setValue(V value)
/* 375:    */     {
/* 376:480 */       IntObjectHashMap.this.values[this.index] = value;
/* 377:    */     }
/* 378:    */   }
/* 379:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.collection.IntObjectHashMap
 * JD-Core Version:    0.7.0.1
 */