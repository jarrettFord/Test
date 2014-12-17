/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.FastThreadLocal;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   6:    */ import io.netty.util.internal.logging.InternalLogger;
/*   7:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   8:    */ import java.nio.ByteBuffer;
/*   9:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  10:    */ 
/*  11:    */ public class PooledByteBufAllocator
/*  12:    */   extends AbstractByteBufAllocator
/*  13:    */ {
/*  14: 30 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(PooledByteBufAllocator.class);
/*  15:    */   private static final int DEFAULT_NUM_HEAP_ARENA;
/*  16:    */   private static final int DEFAULT_NUM_DIRECT_ARENA;
/*  17:    */   private static final int DEFAULT_PAGE_SIZE;
/*  18:    */   private static final int DEFAULT_MAX_ORDER;
/*  19:    */   private static final int DEFAULT_TINY_CACHE_SIZE;
/*  20:    */   private static final int DEFAULT_SMALL_CACHE_SIZE;
/*  21:    */   private static final int DEFAULT_NORMAL_CACHE_SIZE;
/*  22:    */   private static final int DEFAULT_MAX_CACHED_BUFFER_CAPACITY;
/*  23:    */   private static final int DEFAULT_CACHE_TRIM_INTERVAL;
/*  24:    */   private static final int MIN_PAGE_SIZE = 4096;
/*  25:    */   private static final int MAX_CHUNK_SIZE = 1073741824;
/*  26:    */   
/*  27:    */   static
/*  28:    */   {
/*  29: 46 */     int defaultPageSize = SystemPropertyUtil.getInt("io.netty.allocator.pageSize", 8192);
/*  30: 47 */     Throwable pageSizeFallbackCause = null;
/*  31:    */     try
/*  32:    */     {
/*  33: 49 */       validateAndCalculatePageShifts(defaultPageSize);
/*  34:    */     }
/*  35:    */     catch (Throwable t)
/*  36:    */     {
/*  37: 51 */       pageSizeFallbackCause = t;
/*  38: 52 */       defaultPageSize = 8192;
/*  39:    */     }
/*  40: 54 */     DEFAULT_PAGE_SIZE = defaultPageSize;
/*  41:    */     
/*  42: 56 */     int defaultMaxOrder = SystemPropertyUtil.getInt("io.netty.allocator.maxOrder", 11);
/*  43: 57 */     Throwable maxOrderFallbackCause = null;
/*  44:    */     try
/*  45:    */     {
/*  46: 59 */       validateAndCalculateChunkSize(DEFAULT_PAGE_SIZE, defaultMaxOrder);
/*  47:    */     }
/*  48:    */     catch (Throwable t)
/*  49:    */     {
/*  50: 61 */       maxOrderFallbackCause = t;
/*  51: 62 */       defaultMaxOrder = 11;
/*  52:    */     }
/*  53: 64 */     DEFAULT_MAX_ORDER = defaultMaxOrder;
/*  54:    */     
/*  55:    */ 
/*  56:    */ 
/*  57: 68 */     Runtime runtime = Runtime.getRuntime();
/*  58: 69 */     int defaultChunkSize = DEFAULT_PAGE_SIZE << DEFAULT_MAX_ORDER;
/*  59: 70 */     DEFAULT_NUM_HEAP_ARENA = Math.max(0, SystemPropertyUtil.getInt("io.netty.allocator.numHeapArenas", (int)Math.min(runtime.availableProcessors(), Runtime.getRuntime().maxMemory() / defaultChunkSize / 2L / 3L)));
/*  60:    */     
/*  61:    */ 
/*  62:    */ 
/*  63:    */ 
/*  64:    */ 
/*  65: 76 */     DEFAULT_NUM_DIRECT_ARENA = Math.max(0, SystemPropertyUtil.getInt("io.netty.allocator.numDirectArenas", (int)Math.min(runtime.availableProcessors(), PlatformDependent.maxDirectMemory() / defaultChunkSize / 2L / 3L)));
/*  66:    */     
/*  67:    */ 
/*  68:    */ 
/*  69:    */ 
/*  70:    */ 
/*  71:    */ 
/*  72:    */ 
/*  73: 84 */     DEFAULT_TINY_CACHE_SIZE = SystemPropertyUtil.getInt("io.netty.allocator.tinyCacheSize", 512);
/*  74: 85 */     DEFAULT_SMALL_CACHE_SIZE = SystemPropertyUtil.getInt("io.netty.allocator.smallCacheSize", 256);
/*  75: 86 */     DEFAULT_NORMAL_CACHE_SIZE = SystemPropertyUtil.getInt("io.netty.allocator.normalCacheSize", 64);
/*  76:    */     
/*  77:    */ 
/*  78:    */ 
/*  79: 90 */     DEFAULT_MAX_CACHED_BUFFER_CAPACITY = SystemPropertyUtil.getInt("io.netty.allocator.maxCachedBufferCapacity", 32768);
/*  80:    */     
/*  81:    */ 
/*  82:    */ 
/*  83: 94 */     DEFAULT_CACHE_TRIM_INTERVAL = SystemPropertyUtil.getInt("io.netty.allocator.cacheTrimInterval", 8192);
/*  84: 97 */     if (logger.isDebugEnabled())
/*  85:    */     {
/*  86: 98 */       logger.debug("-Dio.netty.allocator.numHeapArenas: {}", Integer.valueOf(DEFAULT_NUM_HEAP_ARENA));
/*  87: 99 */       logger.debug("-Dio.netty.allocator.numDirectArenas: {}", Integer.valueOf(DEFAULT_NUM_DIRECT_ARENA));
/*  88:100 */       if (pageSizeFallbackCause == null) {
/*  89:101 */         logger.debug("-Dio.netty.allocator.pageSize: {}", Integer.valueOf(DEFAULT_PAGE_SIZE));
/*  90:    */       } else {
/*  91:103 */         logger.debug("-Dio.netty.allocator.pageSize: {}", Integer.valueOf(DEFAULT_PAGE_SIZE), pageSizeFallbackCause);
/*  92:    */       }
/*  93:105 */       if (maxOrderFallbackCause == null) {
/*  94:106 */         logger.debug("-Dio.netty.allocator.maxOrder: {}", Integer.valueOf(DEFAULT_MAX_ORDER));
/*  95:    */       } else {
/*  96:108 */         logger.debug("-Dio.netty.allocator.maxOrder: {}", Integer.valueOf(DEFAULT_MAX_ORDER), maxOrderFallbackCause);
/*  97:    */       }
/*  98:110 */       logger.debug("-Dio.netty.allocator.chunkSize: {}", Integer.valueOf(DEFAULT_PAGE_SIZE << DEFAULT_MAX_ORDER));
/*  99:111 */       logger.debug("-Dio.netty.allocator.tinyCacheSize: {}", Integer.valueOf(DEFAULT_TINY_CACHE_SIZE));
/* 100:112 */       logger.debug("-Dio.netty.allocator.smallCacheSize: {}", Integer.valueOf(DEFAULT_SMALL_CACHE_SIZE));
/* 101:113 */       logger.debug("-Dio.netty.allocator.normalCacheSize: {}", Integer.valueOf(DEFAULT_NORMAL_CACHE_SIZE));
/* 102:114 */       logger.debug("-Dio.netty.allocator.maxCachedBufferCapacity: {}", Integer.valueOf(DEFAULT_MAX_CACHED_BUFFER_CAPACITY));
/* 103:115 */       logger.debug("-Dio.netty.allocator.cacheTrimInterval: {}", Integer.valueOf(DEFAULT_CACHE_TRIM_INTERVAL));
/* 104:    */     }
/* 105:    */   }
/* 106:    */   
/* 107:119 */   public static final PooledByteBufAllocator DEFAULT = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());
/* 108:    */   private final PoolArena<byte[]>[] heapArenas;
/* 109:    */   private final PoolArena<ByteBuffer>[] directArenas;
/* 110:    */   private final int tinyCacheSize;
/* 111:    */   private final int smallCacheSize;
/* 112:    */   private final int normalCacheSize;
/* 113:    */   final PoolThreadLocalCache threadCache;
/* 114:    */   
/* 115:    */   public PooledByteBufAllocator()
/* 116:    */   {
/* 117:131 */     this(false);
/* 118:    */   }
/* 119:    */   
/* 120:    */   public PooledByteBufAllocator(boolean preferDirect)
/* 121:    */   {
/* 122:135 */     this(preferDirect, DEFAULT_NUM_HEAP_ARENA, DEFAULT_NUM_DIRECT_ARENA, DEFAULT_PAGE_SIZE, DEFAULT_MAX_ORDER);
/* 123:    */   }
/* 124:    */   
/* 125:    */   public PooledByteBufAllocator(int nHeapArena, int nDirectArena, int pageSize, int maxOrder)
/* 126:    */   {
/* 127:139 */     this(false, nHeapArena, nDirectArena, pageSize, maxOrder);
/* 128:    */   }
/* 129:    */   
/* 130:    */   public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder)
/* 131:    */   {
/* 132:143 */     this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder, DEFAULT_TINY_CACHE_SIZE, DEFAULT_SMALL_CACHE_SIZE, DEFAULT_NORMAL_CACHE_SIZE);
/* 133:    */   }
/* 134:    */   
/* 135:    */   public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder, int tinyCacheSize, int smallCacheSize, int normalCacheSize)
/* 136:    */   {
/* 137:149 */     super(preferDirect);
/* 138:150 */     this.threadCache = new PoolThreadLocalCache();
/* 139:151 */     this.tinyCacheSize = tinyCacheSize;
/* 140:152 */     this.smallCacheSize = smallCacheSize;
/* 141:153 */     this.normalCacheSize = normalCacheSize;
/* 142:154 */     int chunkSize = validateAndCalculateChunkSize(pageSize, maxOrder);
/* 143:156 */     if (nHeapArena < 0) {
/* 144:157 */       throw new IllegalArgumentException("nHeapArena: " + nHeapArena + " (expected: >= 0)");
/* 145:    */     }
/* 146:159 */     if (nDirectArena < 0) {
/* 147:160 */       throw new IllegalArgumentException("nDirectArea: " + nDirectArena + " (expected: >= 0)");
/* 148:    */     }
/* 149:163 */     int pageShifts = validateAndCalculatePageShifts(pageSize);
/* 150:165 */     if (nHeapArena > 0)
/* 151:    */     {
/* 152:166 */       this.heapArenas = newArenaArray(nHeapArena);
/* 153:167 */       for (int i = 0; i < this.heapArenas.length; i++) {
/* 154:168 */         this.heapArenas[i] = new PoolArena.HeapArena(this, pageSize, maxOrder, pageShifts, chunkSize);
/* 155:    */       }
/* 156:    */     }
/* 157:    */     else
/* 158:    */     {
/* 159:171 */       this.heapArenas = null;
/* 160:    */     }
/* 161:174 */     if (nDirectArena > 0)
/* 162:    */     {
/* 163:175 */       this.directArenas = newArenaArray(nDirectArena);
/* 164:176 */       for (int i = 0; i < this.directArenas.length; i++) {
/* 165:177 */         this.directArenas[i] = new PoolArena.DirectArena(this, pageSize, maxOrder, pageShifts, chunkSize);
/* 166:    */       }
/* 167:    */     }
/* 168:    */     else
/* 169:    */     {
/* 170:180 */       this.directArenas = null;
/* 171:    */     }
/* 172:    */   }
/* 173:    */   
/* 174:    */   @Deprecated
/* 175:    */   public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder, int tinyCacheSize, int smallCacheSize, int normalCacheSize, long cacheThreadAliveCheckInterval)
/* 176:    */   {
/* 177:189 */     this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder, tinyCacheSize, smallCacheSize, normalCacheSize);
/* 178:    */   }
/* 179:    */   
/* 180:    */   private static <T> PoolArena<T>[] newArenaArray(int size)
/* 181:    */   {
/* 182:195 */     return new PoolArena[size];
/* 183:    */   }
/* 184:    */   
/* 185:    */   private static int validateAndCalculatePageShifts(int pageSize)
/* 186:    */   {
/* 187:199 */     if (pageSize < 4096) {
/* 188:200 */       throw new IllegalArgumentException("pageSize: " + pageSize + " (expected: " + 4096 + "+)");
/* 189:    */     }
/* 190:203 */     if ((pageSize & pageSize - 1) != 0) {
/* 191:204 */       throw new IllegalArgumentException("pageSize: " + pageSize + " (expected: power of 2)");
/* 192:    */     }
/* 193:208 */     return 31 - Integer.numberOfLeadingZeros(pageSize);
/* 194:    */   }
/* 195:    */   
/* 196:    */   private static int validateAndCalculateChunkSize(int pageSize, int maxOrder)
/* 197:    */   {
/* 198:212 */     if (maxOrder > 14) {
/* 199:213 */       throw new IllegalArgumentException("maxOrder: " + maxOrder + " (expected: 0-14)");
/* 200:    */     }
/* 201:217 */     int chunkSize = pageSize;
/* 202:218 */     for (int i = maxOrder; i > 0; i--)
/* 203:    */     {
/* 204:219 */       if (chunkSize > 536870912) {
/* 205:220 */         throw new IllegalArgumentException(String.format("pageSize (%d) << maxOrder (%d) must not exceed %d", new Object[] { Integer.valueOf(pageSize), Integer.valueOf(maxOrder), Integer.valueOf(1073741824) }));
/* 206:    */       }
/* 207:223 */       chunkSize <<= 1;
/* 208:    */     }
/* 209:225 */     return chunkSize;
/* 210:    */   }
/* 211:    */   
/* 212:    */   protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity)
/* 213:    */   {
/* 214:230 */     PoolThreadCache cache = (PoolThreadCache)this.threadCache.get();
/* 215:231 */     PoolArena<byte[]> heapArena = cache.heapArena;
/* 216:    */     ByteBuf buf;
/* 217:    */     ByteBuf buf;
/* 218:234 */     if (heapArena != null) {
/* 219:235 */       buf = heapArena.allocate(cache, initialCapacity, maxCapacity);
/* 220:    */     } else {
/* 221:237 */       buf = new UnpooledHeapByteBuf(this, initialCapacity, maxCapacity);
/* 222:    */     }
/* 223:240 */     return toLeakAwareBuffer(buf);
/* 224:    */   }
/* 225:    */   
/* 226:    */   protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity)
/* 227:    */   {
/* 228:245 */     PoolThreadCache cache = (PoolThreadCache)this.threadCache.get();
/* 229:246 */     PoolArena<ByteBuffer> directArena = cache.directArena;
/* 230:    */     ByteBuf buf;
/* 231:    */     ByteBuf buf;
/* 232:249 */     if (directArena != null)
/* 233:    */     {
/* 234:250 */       buf = directArena.allocate(cache, initialCapacity, maxCapacity);
/* 235:    */     }
/* 236:    */     else
/* 237:    */     {
/* 238:    */       ByteBuf buf;
/* 239:252 */       if (PlatformDependent.hasUnsafe()) {
/* 240:253 */         buf = new UnpooledUnsafeDirectByteBuf(this, initialCapacity, maxCapacity);
/* 241:    */       } else {
/* 242:255 */         buf = new UnpooledDirectByteBuf(this, initialCapacity, maxCapacity);
/* 243:    */       }
/* 244:    */     }
/* 245:259 */     return toLeakAwareBuffer(buf);
/* 246:    */   }
/* 247:    */   
/* 248:    */   public boolean isDirectBufferPooled()
/* 249:    */   {
/* 250:264 */     return this.directArenas != null;
/* 251:    */   }
/* 252:    */   
/* 253:    */   @Deprecated
/* 254:    */   public boolean hasThreadLocalCache()
/* 255:    */   {
/* 256:273 */     return this.threadCache.isSet();
/* 257:    */   }
/* 258:    */   
/* 259:    */   @Deprecated
/* 260:    */   public void freeThreadLocalCache()
/* 261:    */   {
/* 262:281 */     this.threadCache.remove();
/* 263:    */   }
/* 264:    */   
/* 265:    */   final class PoolThreadLocalCache
/* 266:    */     extends FastThreadLocal<PoolThreadCache>
/* 267:    */   {
/* 268:285 */     private final AtomicInteger index = new AtomicInteger();
/* 269:    */     
/* 270:    */     PoolThreadLocalCache() {}
/* 271:    */     
/* 272:    */     protected PoolThreadCache initialValue()
/* 273:    */     {
/* 274:289 */       int idx = this.index.getAndIncrement();
/* 275:    */       PoolArena<byte[]> heapArena;
/* 276:    */       PoolArena<byte[]> heapArena;
/* 277:293 */       if (PooledByteBufAllocator.this.heapArenas != null) {
/* 278:294 */         heapArena = PooledByteBufAllocator.this.heapArenas[Math.abs(idx % PooledByteBufAllocator.this.heapArenas.length)];
/* 279:    */       } else {
/* 280:296 */         heapArena = null;
/* 281:    */       }
/* 282:    */       PoolArena<ByteBuffer> directArena;
/* 283:    */       PoolArena<ByteBuffer> directArena;
/* 284:299 */       if (PooledByteBufAllocator.this.directArenas != null) {
/* 285:300 */         directArena = PooledByteBufAllocator.this.directArenas[Math.abs(idx % PooledByteBufAllocator.this.directArenas.length)];
/* 286:    */       } else {
/* 287:302 */         directArena = null;
/* 288:    */       }
/* 289:305 */       return new PoolThreadCache(heapArena, directArena, PooledByteBufAllocator.this.tinyCacheSize, PooledByteBufAllocator.this.smallCacheSize, PooledByteBufAllocator.this.normalCacheSize, PooledByteBufAllocator.DEFAULT_MAX_CACHED_BUFFER_CAPACITY, PooledByteBufAllocator.DEFAULT_CACHE_TRIM_INTERVAL);
/* 290:    */     }
/* 291:    */     
/* 292:    */     protected void onRemoval(PoolThreadCache value)
/* 293:    */     {
/* 294:312 */       value.free();
/* 295:    */     }
/* 296:    */   }
/* 297:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.PooledByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */