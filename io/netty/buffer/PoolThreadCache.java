/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.ThreadDeathWatcher;
/*   4:    */ import io.netty.util.internal.logging.InternalLogger;
/*   5:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   6:    */ import java.nio.ByteBuffer;
/*   7:    */ 
/*   8:    */ final class PoolThreadCache
/*   9:    */ {
/*  10: 34 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(PoolThreadCache.class);
/*  11:    */   final PoolArena<byte[]> heapArena;
/*  12:    */   final PoolArena<ByteBuffer> directArena;
/*  13:    */   private final MemoryRegionCache<byte[]>[] tinySubPageHeapCaches;
/*  14:    */   private final MemoryRegionCache<byte[]>[] smallSubPageHeapCaches;
/*  15:    */   private final MemoryRegionCache<ByteBuffer>[] tinySubPageDirectCaches;
/*  16:    */   private final MemoryRegionCache<ByteBuffer>[] smallSubPageDirectCaches;
/*  17:    */   private final MemoryRegionCache<byte[]>[] normalHeapCaches;
/*  18:    */   private final MemoryRegionCache<ByteBuffer>[] normalDirectCaches;
/*  19:    */   private final int numShiftsNormalDirect;
/*  20:    */   private final int numShiftsNormalHeap;
/*  21:    */   private final int freeSweepAllocationThreshold;
/*  22:    */   private int allocations;
/*  23: 54 */   private final Thread thread = Thread.currentThread();
/*  24: 55 */   private final Runnable freeTask = new Runnable()
/*  25:    */   {
/*  26:    */     public void run()
/*  27:    */     {
/*  28: 58 */       PoolThreadCache.this.free0();
/*  29:    */     }
/*  30:    */   };
/*  31:    */   
/*  32:    */   PoolThreadCache(PoolArena<byte[]> heapArena, PoolArena<ByteBuffer> directArena, int tinyCacheSize, int smallCacheSize, int normalCacheSize, int maxCachedBufferCapacity, int freeSweepAllocationThreshold)
/*  33:    */   {
/*  34: 68 */     if (maxCachedBufferCapacity < 0) {
/*  35: 69 */       throw new IllegalArgumentException("maxCachedBufferCapacity: " + maxCachedBufferCapacity + " (expected: >= 0)");
/*  36:    */     }
/*  37: 72 */     if (freeSweepAllocationThreshold < 1) {
/*  38: 73 */       throw new IllegalArgumentException("freeSweepAllocationThreshold: " + maxCachedBufferCapacity + " (expected: > 0)");
/*  39:    */     }
/*  40: 76 */     this.freeSweepAllocationThreshold = freeSweepAllocationThreshold;
/*  41: 77 */     this.heapArena = heapArena;
/*  42: 78 */     this.directArena = directArena;
/*  43: 79 */     if (directArena != null)
/*  44:    */     {
/*  45: 80 */       this.tinySubPageDirectCaches = createSubPageCaches(tinyCacheSize, 32);
/*  46: 81 */       this.smallSubPageDirectCaches = createSubPageCaches(smallCacheSize, directArena.numSmallSubpagePools);
/*  47:    */       
/*  48: 83 */       this.numShiftsNormalDirect = log2(directArena.pageSize);
/*  49: 84 */       this.normalDirectCaches = createNormalCaches(normalCacheSize, maxCachedBufferCapacity, directArena);
/*  50:    */     }
/*  51:    */     else
/*  52:    */     {
/*  53: 88 */       this.tinySubPageDirectCaches = null;
/*  54: 89 */       this.smallSubPageDirectCaches = null;
/*  55: 90 */       this.normalDirectCaches = null;
/*  56: 91 */       this.numShiftsNormalDirect = -1;
/*  57:    */     }
/*  58: 93 */     if (heapArena != null)
/*  59:    */     {
/*  60: 95 */       this.tinySubPageHeapCaches = createSubPageCaches(tinyCacheSize, 32);
/*  61: 96 */       this.smallSubPageHeapCaches = createSubPageCaches(smallCacheSize, heapArena.numSmallSubpagePools);
/*  62:    */       
/*  63: 98 */       this.numShiftsNormalHeap = log2(heapArena.pageSize);
/*  64: 99 */       this.normalHeapCaches = createNormalCaches(normalCacheSize, maxCachedBufferCapacity, heapArena);
/*  65:    */     }
/*  66:    */     else
/*  67:    */     {
/*  68:103 */       this.tinySubPageHeapCaches = null;
/*  69:104 */       this.smallSubPageHeapCaches = null;
/*  70:105 */       this.normalHeapCaches = null;
/*  71:106 */       this.numShiftsNormalHeap = -1;
/*  72:    */     }
/*  73:111 */     ThreadDeathWatcher.watch(this.thread, this.freeTask);
/*  74:    */   }
/*  75:    */   
/*  76:    */   private static <T> SubPageMemoryRegionCache<T>[] createSubPageCaches(int cacheSize, int numCaches)
/*  77:    */   {
/*  78:115 */     if (cacheSize > 0)
/*  79:    */     {
/*  80:117 */       SubPageMemoryRegionCache<T>[] cache = new SubPageMemoryRegionCache[numCaches];
/*  81:118 */       for (int i = 0; i < cache.length; i++) {
/*  82:120 */         cache[i] = new SubPageMemoryRegionCache(cacheSize);
/*  83:    */       }
/*  84:122 */       return cache;
/*  85:    */     }
/*  86:124 */     return null;
/*  87:    */   }
/*  88:    */   
/*  89:    */   private static <T> NormalMemoryRegionCache<T>[] createNormalCaches(int cacheSize, int maxCachedBufferCapacity, PoolArena<T> area)
/*  90:    */   {
/*  91:130 */     if (cacheSize > 0)
/*  92:    */     {
/*  93:131 */       int max = Math.min(area.chunkSize, maxCachedBufferCapacity);
/*  94:132 */       int arraySize = Math.max(1, max / area.pageSize);
/*  95:    */       
/*  96:    */ 
/*  97:135 */       NormalMemoryRegionCache<T>[] cache = new NormalMemoryRegionCache[arraySize];
/*  98:136 */       for (int i = 0; i < cache.length; i++) {
/*  99:137 */         cache[i] = new NormalMemoryRegionCache(cacheSize);
/* 100:    */       }
/* 101:139 */       return cache;
/* 102:    */     }
/* 103:141 */     return null;
/* 104:    */   }
/* 105:    */   
/* 106:    */   private static int log2(int val)
/* 107:    */   {
/* 108:146 */     int res = 0;
/* 109:147 */     while (val > 1)
/* 110:    */     {
/* 111:148 */       val >>= 1;
/* 112:149 */       res++;
/* 113:    */     }
/* 114:151 */     return res;
/* 115:    */   }
/* 116:    */   
/* 117:    */   boolean allocateTiny(PoolArena<?> area, PooledByteBuf<?> buf, int reqCapacity, int normCapacity)
/* 118:    */   {
/* 119:158 */     return allocate(cacheForTiny(area, normCapacity), buf, reqCapacity);
/* 120:    */   }
/* 121:    */   
/* 122:    */   boolean allocateSmall(PoolArena<?> area, PooledByteBuf<?> buf, int reqCapacity, int normCapacity)
/* 123:    */   {
/* 124:165 */     return allocate(cacheForSmall(area, normCapacity), buf, reqCapacity);
/* 125:    */   }
/* 126:    */   
/* 127:    */   boolean allocateNormal(PoolArena<?> area, PooledByteBuf<?> buf, int reqCapacity, int normCapacity)
/* 128:    */   {
/* 129:172 */     return allocate(cacheForNormal(area, normCapacity), buf, reqCapacity);
/* 130:    */   }
/* 131:    */   
/* 132:    */   private boolean allocate(MemoryRegionCache<?> cache, PooledByteBuf buf, int reqCapacity)
/* 133:    */   {
/* 134:177 */     if (cache == null) {
/* 135:179 */       return false;
/* 136:    */     }
/* 137:181 */     boolean allocated = cache.allocate(buf, reqCapacity);
/* 138:182 */     if (++this.allocations >= this.freeSweepAllocationThreshold)
/* 139:    */     {
/* 140:183 */       this.allocations = 0;
/* 141:184 */       trim();
/* 142:    */     }
/* 143:186 */     return allocated;
/* 144:    */   }
/* 145:    */   
/* 146:    */   boolean add(PoolArena<?> area, PoolChunk chunk, long handle, int normCapacity)
/* 147:    */   {
/* 148:    */     MemoryRegionCache<?> cache;
/* 149:    */     MemoryRegionCache<?> cache;
/* 150:196 */     if (area.isTinyOrSmall(normCapacity))
/* 151:    */     {
/* 152:    */       MemoryRegionCache<?> cache;
/* 153:197 */       if (PoolArena.isTiny(normCapacity)) {
/* 154:198 */         cache = cacheForTiny(area, normCapacity);
/* 155:    */       } else {
/* 156:200 */         cache = cacheForSmall(area, normCapacity);
/* 157:    */       }
/* 158:    */     }
/* 159:    */     else
/* 160:    */     {
/* 161:203 */       cache = cacheForNormal(area, normCapacity);
/* 162:    */     }
/* 163:205 */     if (cache == null) {
/* 164:206 */       return false;
/* 165:    */     }
/* 166:208 */     return cache.add(chunk, handle);
/* 167:    */   }
/* 168:    */   
/* 169:    */   void free()
/* 170:    */   {
/* 171:215 */     ThreadDeathWatcher.unwatch(this.thread, this.freeTask);
/* 172:216 */     free0();
/* 173:    */   }
/* 174:    */   
/* 175:    */   private void free0()
/* 176:    */   {
/* 177:220 */     int numFreed = free(this.tinySubPageDirectCaches) + free(this.smallSubPageDirectCaches) + free(this.normalDirectCaches) + free(this.tinySubPageHeapCaches) + free(this.smallSubPageHeapCaches) + free(this.normalHeapCaches);
/* 178:227 */     if ((numFreed > 0) && (logger.isDebugEnabled())) {
/* 179:228 */       logger.debug("Freed {} thread-local buffer(s) from thread: {}", Integer.valueOf(numFreed), this.thread.getName());
/* 180:    */     }
/* 181:    */   }
/* 182:    */   
/* 183:    */   private static int free(MemoryRegionCache<?>[] caches)
/* 184:    */   {
/* 185:233 */     if (caches == null) {
/* 186:234 */       return 0;
/* 187:    */     }
/* 188:237 */     int numFreed = 0;
/* 189:238 */     for (MemoryRegionCache<?> c : caches) {
/* 190:239 */       numFreed += free(c);
/* 191:    */     }
/* 192:241 */     return numFreed;
/* 193:    */   }
/* 194:    */   
/* 195:    */   private static int free(MemoryRegionCache<?> cache)
/* 196:    */   {
/* 197:245 */     if (cache == null) {
/* 198:246 */       return 0;
/* 199:    */     }
/* 200:248 */     return cache.free();
/* 201:    */   }
/* 202:    */   
/* 203:    */   void trim()
/* 204:    */   {
/* 205:252 */     trim(this.tinySubPageDirectCaches);
/* 206:253 */     trim(this.smallSubPageDirectCaches);
/* 207:254 */     trim(this.normalDirectCaches);
/* 208:255 */     trim(this.tinySubPageHeapCaches);
/* 209:256 */     trim(this.smallSubPageHeapCaches);
/* 210:257 */     trim(this.normalHeapCaches);
/* 211:    */   }
/* 212:    */   
/* 213:    */   private static void trim(MemoryRegionCache<?>[] caches)
/* 214:    */   {
/* 215:261 */     if (caches == null) {
/* 216:262 */       return;
/* 217:    */     }
/* 218:264 */     for (MemoryRegionCache<?> c : caches) {
/* 219:265 */       trim(c);
/* 220:    */     }
/* 221:    */   }
/* 222:    */   
/* 223:    */   private static void trim(MemoryRegionCache<?> cache)
/* 224:    */   {
/* 225:270 */     if (cache == null) {
/* 226:271 */       return;
/* 227:    */     }
/* 228:273 */     cache.trim();
/* 229:    */   }
/* 230:    */   
/* 231:    */   private MemoryRegionCache<?> cacheForTiny(PoolArena<?> area, int normCapacity)
/* 232:    */   {
/* 233:277 */     int idx = PoolArena.tinyIdx(normCapacity);
/* 234:278 */     if (area.isDirect()) {
/* 235:279 */       return cache(this.tinySubPageDirectCaches, idx);
/* 236:    */     }
/* 237:281 */     return cache(this.tinySubPageHeapCaches, idx);
/* 238:    */   }
/* 239:    */   
/* 240:    */   private MemoryRegionCache<?> cacheForSmall(PoolArena<?> area, int normCapacity)
/* 241:    */   {
/* 242:285 */     int idx = PoolArena.smallIdx(normCapacity);
/* 243:286 */     if (area.isDirect()) {
/* 244:287 */       return cache(this.smallSubPageDirectCaches, idx);
/* 245:    */     }
/* 246:289 */     return cache(this.smallSubPageHeapCaches, idx);
/* 247:    */   }
/* 248:    */   
/* 249:    */   private MemoryRegionCache<?> cacheForNormal(PoolArena<?> area, int normCapacity)
/* 250:    */   {
/* 251:293 */     if (area.isDirect())
/* 252:    */     {
/* 253:294 */       int idx = log2(normCapacity >> this.numShiftsNormalDirect);
/* 254:295 */       return cache(this.normalDirectCaches, idx);
/* 255:    */     }
/* 256:297 */     int idx = log2(normCapacity >> this.numShiftsNormalHeap);
/* 257:298 */     return cache(this.normalHeapCaches, idx);
/* 258:    */   }
/* 259:    */   
/* 260:    */   private static <T> MemoryRegionCache<T> cache(MemoryRegionCache<T>[] cache, int idx)
/* 261:    */   {
/* 262:302 */     if ((cache == null) || (idx > cache.length - 1)) {
/* 263:303 */       return null;
/* 264:    */     }
/* 265:305 */     return cache[idx];
/* 266:    */   }
/* 267:    */   
/* 268:    */   private static final class SubPageMemoryRegionCache<T>
/* 269:    */     extends PoolThreadCache.MemoryRegionCache<T>
/* 270:    */   {
/* 271:    */     SubPageMemoryRegionCache(int size)
/* 272:    */     {
/* 273:313 */       super();
/* 274:    */     }
/* 275:    */     
/* 276:    */     protected void initBuf(PoolChunk<T> chunk, long handle, PooledByteBuf<T> buf, int reqCapacity)
/* 277:    */     {
/* 278:319 */       chunk.initBufWithSubpage(buf, handle, reqCapacity);
/* 279:    */     }
/* 280:    */   }
/* 281:    */   
/* 282:    */   private static final class NormalMemoryRegionCache<T>
/* 283:    */     extends PoolThreadCache.MemoryRegionCache<T>
/* 284:    */   {
/* 285:    */     NormalMemoryRegionCache(int size)
/* 286:    */     {
/* 287:328 */       super();
/* 288:    */     }
/* 289:    */     
/* 290:    */     protected void initBuf(PoolChunk<T> chunk, long handle, PooledByteBuf<T> buf, int reqCapacity)
/* 291:    */     {
/* 292:334 */       chunk.initBuf(buf, handle, reqCapacity);
/* 293:    */     }
/* 294:    */   }
/* 295:    */   
/* 296:    */   private static abstract class MemoryRegionCache<T>
/* 297:    */   {
/* 298:    */     private final Entry<T>[] entries;
/* 299:    */     private final int maxUnusedCached;
/* 300:    */     private int head;
/* 301:    */     private int tail;
/* 302:    */     private int maxEntriesInUse;
/* 303:    */     private int entriesInUse;
/* 304:    */     
/* 305:    */     MemoryRegionCache(int size)
/* 306:    */     {
/* 307:351 */       this.entries = new Entry[powerOfTwo(size)];
/* 308:352 */       for (int i = 0; i < this.entries.length; i++) {
/* 309:353 */         this.entries[i] = new Entry(null);
/* 310:    */       }
/* 311:355 */       this.maxUnusedCached = (size / 2);
/* 312:    */     }
/* 313:    */     
/* 314:    */     private static int powerOfTwo(int res)
/* 315:    */     {
/* 316:359 */       if (res <= 2) {
/* 317:360 */         return 2;
/* 318:    */       }
/* 319:362 */       res--;
/* 320:363 */       res |= res >> 1;
/* 321:364 */       res |= res >> 2;
/* 322:365 */       res |= res >> 4;
/* 323:366 */       res |= res >> 8;
/* 324:367 */       res |= res >> 16;
/* 325:368 */       res++;
/* 326:369 */       return res;
/* 327:    */     }
/* 328:    */     
/* 329:    */     protected abstract void initBuf(PoolChunk<T> paramPoolChunk, long paramLong, PooledByteBuf<T> paramPooledByteBuf, int paramInt);
/* 330:    */     
/* 331:    */     public boolean add(PoolChunk<T> chunk, long handle)
/* 332:    */     {
/* 333:382 */       Entry<T> entry = this.entries[this.tail];
/* 334:383 */       if (entry.chunk != null) {
/* 335:385 */         return false;
/* 336:    */       }
/* 337:387 */       this.entriesInUse -= 1;
/* 338:    */       
/* 339:389 */       entry.chunk = chunk;
/* 340:390 */       entry.handle = handle;
/* 341:391 */       this.tail = nextIdx(this.tail);
/* 342:392 */       return true;
/* 343:    */     }
/* 344:    */     
/* 345:    */     public boolean allocate(PooledByteBuf<T> buf, int reqCapacity)
/* 346:    */     {
/* 347:399 */       Entry<T> entry = this.entries[this.head];
/* 348:400 */       if (entry.chunk == null) {
/* 349:401 */         return false;
/* 350:    */       }
/* 351:404 */       this.entriesInUse += 1;
/* 352:405 */       if (this.maxEntriesInUse < this.entriesInUse) {
/* 353:406 */         this.maxEntriesInUse = this.entriesInUse;
/* 354:    */       }
/* 355:408 */       initBuf(entry.chunk, entry.handle, buf, reqCapacity);
/* 356:    */       
/* 357:410 */       entry.chunk = null;
/* 358:411 */       this.head = nextIdx(this.head);
/* 359:412 */       return true;
/* 360:    */     }
/* 361:    */     
/* 362:    */     public int free()
/* 363:    */     {
/* 364:419 */       int numFreed = 0;
/* 365:420 */       this.entriesInUse = 0;
/* 366:421 */       this.maxEntriesInUse = 0;
/* 367:422 */       for (int i = this.head;; i = nextIdx(i)) {
/* 368:423 */         if (freeEntry(this.entries[i])) {
/* 369:424 */           numFreed++;
/* 370:    */         } else {
/* 371:427 */           return numFreed;
/* 372:    */         }
/* 373:    */       }
/* 374:    */     }
/* 375:    */     
/* 376:    */     private void trim()
/* 377:    */     {
/* 378:436 */       int free = size() - this.maxEntriesInUse;
/* 379:437 */       this.entriesInUse = 0;
/* 380:438 */       this.maxEntriesInUse = 0;
/* 381:440 */       if (free <= this.maxUnusedCached) {
/* 382:441 */         return;
/* 383:    */       }
/* 384:444 */       int i = this.head;
/* 385:445 */       for (; free > 0; free--)
/* 386:    */       {
/* 387:446 */         if (!freeEntry(this.entries[i])) {
/* 388:448 */           return;
/* 389:    */         }
/* 390:450 */         i = nextIdx(i);
/* 391:    */       }
/* 392:    */     }
/* 393:    */     
/* 394:    */     private static boolean freeEntry(Entry entry)
/* 395:    */     {
/* 396:456 */       PoolChunk chunk = entry.chunk;
/* 397:457 */       if (chunk == null) {
/* 398:458 */         return false;
/* 399:    */       }
/* 400:461 */       synchronized (chunk.arena)
/* 401:    */       {
/* 402:462 */         chunk.parent.free(chunk, entry.handle);
/* 403:    */       }
/* 404:464 */       entry.chunk = null;
/* 405:465 */       return true;
/* 406:    */     }
/* 407:    */     
/* 408:    */     private int size()
/* 409:    */     {
/* 410:472 */       return this.tail - this.head & this.entries.length - 1;
/* 411:    */     }
/* 412:    */     
/* 413:    */     private int nextIdx(int index)
/* 414:    */     {
/* 415:477 */       return index + 1 & this.entries.length - 1;
/* 416:    */     }
/* 417:    */     
/* 418:    */     private static final class Entry<T>
/* 419:    */     {
/* 420:    */       PoolChunk<T> chunk;
/* 421:    */       long handle;
/* 422:    */     }
/* 423:    */   }
/* 424:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.PoolThreadCache
 * JD-Core Version:    0.7.0.1
 */