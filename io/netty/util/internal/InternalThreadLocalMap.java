/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.FastThreadLocalThread;
/*   4:    */ import java.nio.charset.Charset;
/*   5:    */ import java.nio.charset.CharsetDecoder;
/*   6:    */ import java.nio.charset.CharsetEncoder;
/*   7:    */ import java.util.Arrays;
/*   8:    */ import java.util.IdentityHashMap;
/*   9:    */ import java.util.Map;
/*  10:    */ import java.util.WeakHashMap;
/*  11:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  12:    */ 
/*  13:    */ public final class InternalThreadLocalMap
/*  14:    */   extends UnpaddedInternalThreadLocalMap
/*  15:    */ {
/*  16: 37 */   public static final Object UNSET = new Object();
/*  17:    */   public long rp1;
/*  18:    */   public long rp2;
/*  19:    */   public long rp3;
/*  20:    */   public long rp4;
/*  21:    */   public long rp5;
/*  22:    */   public long rp6;
/*  23:    */   public long rp7;
/*  24:    */   public long rp8;
/*  25:    */   public long rp9;
/*  26:    */   
/*  27:    */   public static InternalThreadLocalMap getIfSet()
/*  28:    */   {
/*  29: 40 */     Thread thread = Thread.currentThread();
/*  30:    */     InternalThreadLocalMap threadLocalMap;
/*  31:    */     InternalThreadLocalMap threadLocalMap;
/*  32: 42 */     if ((thread instanceof FastThreadLocalThread))
/*  33:    */     {
/*  34: 43 */       threadLocalMap = ((FastThreadLocalThread)thread).threadLocalMap();
/*  35:    */     }
/*  36:    */     else
/*  37:    */     {
/*  38: 45 */       ThreadLocal<InternalThreadLocalMap> slowThreadLocalMap = UnpaddedInternalThreadLocalMap.slowThreadLocalMap;
/*  39:    */       InternalThreadLocalMap threadLocalMap;
/*  40: 46 */       if (slowThreadLocalMap == null) {
/*  41: 47 */         threadLocalMap = null;
/*  42:    */       } else {
/*  43: 49 */         threadLocalMap = (InternalThreadLocalMap)slowThreadLocalMap.get();
/*  44:    */       }
/*  45:    */     }
/*  46: 52 */     return threadLocalMap;
/*  47:    */   }
/*  48:    */   
/*  49:    */   public static InternalThreadLocalMap get()
/*  50:    */   {
/*  51: 56 */     Thread thread = Thread.currentThread();
/*  52: 57 */     if ((thread instanceof FastThreadLocalThread)) {
/*  53: 58 */       return fastGet((FastThreadLocalThread)thread);
/*  54:    */     }
/*  55: 60 */     return slowGet();
/*  56:    */   }
/*  57:    */   
/*  58:    */   private static InternalThreadLocalMap fastGet(FastThreadLocalThread thread)
/*  59:    */   {
/*  60: 65 */     InternalThreadLocalMap threadLocalMap = thread.threadLocalMap();
/*  61: 66 */     if (threadLocalMap == null) {
/*  62: 67 */       thread.setThreadLocalMap(threadLocalMap = new InternalThreadLocalMap());
/*  63:    */     }
/*  64: 69 */     return threadLocalMap;
/*  65:    */   }
/*  66:    */   
/*  67:    */   private static InternalThreadLocalMap slowGet()
/*  68:    */   {
/*  69: 73 */     ThreadLocal<InternalThreadLocalMap> slowThreadLocalMap = UnpaddedInternalThreadLocalMap.slowThreadLocalMap;
/*  70: 74 */     if (slowThreadLocalMap == null) {
/*  71: 75 */       UnpaddedInternalThreadLocalMap.slowThreadLocalMap = slowThreadLocalMap = new ThreadLocal();
/*  72:    */     }
/*  73: 79 */     InternalThreadLocalMap ret = (InternalThreadLocalMap)slowThreadLocalMap.get();
/*  74: 80 */     if (ret == null)
/*  75:    */     {
/*  76: 81 */       ret = new InternalThreadLocalMap();
/*  77: 82 */       slowThreadLocalMap.set(ret);
/*  78:    */     }
/*  79: 84 */     return ret;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public static void remove()
/*  83:    */   {
/*  84: 88 */     Thread thread = Thread.currentThread();
/*  85: 89 */     if ((thread instanceof FastThreadLocalThread))
/*  86:    */     {
/*  87: 90 */       ((FastThreadLocalThread)thread).setThreadLocalMap(null);
/*  88:    */     }
/*  89:    */     else
/*  90:    */     {
/*  91: 92 */       ThreadLocal<InternalThreadLocalMap> slowThreadLocalMap = UnpaddedInternalThreadLocalMap.slowThreadLocalMap;
/*  92: 93 */       if (slowThreadLocalMap != null) {
/*  93: 94 */         slowThreadLocalMap.remove();
/*  94:    */       }
/*  95:    */     }
/*  96:    */   }
/*  97:    */   
/*  98:    */   public static void destroy()
/*  99:    */   {
/* 100:100 */     slowThreadLocalMap = null;
/* 101:    */   }
/* 102:    */   
/* 103:    */   public static int nextVariableIndex()
/* 104:    */   {
/* 105:104 */     int index = nextIndex.getAndIncrement();
/* 106:105 */     if (index < 0)
/* 107:    */     {
/* 108:106 */       nextIndex.decrementAndGet();
/* 109:107 */       throw new IllegalStateException("too many thread-local indexed variables");
/* 110:    */     }
/* 111:109 */     return index;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public static int lastVariableIndex()
/* 115:    */   {
/* 116:113 */     return nextIndex.get() - 1;
/* 117:    */   }
/* 118:    */   
/* 119:    */   private InternalThreadLocalMap()
/* 120:    */   {
/* 121:121 */     super(newIndexedVariableTable());
/* 122:    */   }
/* 123:    */   
/* 124:    */   private static Object[] newIndexedVariableTable()
/* 125:    */   {
/* 126:125 */     Object[] array = new Object[32];
/* 127:126 */     Arrays.fill(array, UNSET);
/* 128:127 */     return array;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public int size()
/* 132:    */   {
/* 133:131 */     int count = 0;
/* 134:133 */     if (this.futureListenerStackDepth != 0) {
/* 135:134 */       count++;
/* 136:    */     }
/* 137:136 */     if (this.localChannelReaderStackDepth != 0) {
/* 138:137 */       count++;
/* 139:    */     }
/* 140:139 */     if (this.handlerSharableCache != null) {
/* 141:140 */       count++;
/* 142:    */     }
/* 143:142 */     if (this.counterHashCode != null) {
/* 144:143 */       count++;
/* 145:    */     }
/* 146:145 */     if (this.random != null) {
/* 147:146 */       count++;
/* 148:    */     }
/* 149:148 */     if (this.typeParameterMatcherGetCache != null) {
/* 150:149 */       count++;
/* 151:    */     }
/* 152:151 */     if (this.typeParameterMatcherFindCache != null) {
/* 153:152 */       count++;
/* 154:    */     }
/* 155:154 */     if (this.stringBuilder != null) {
/* 156:155 */       count++;
/* 157:    */     }
/* 158:157 */     if (this.charsetEncoderCache != null) {
/* 159:158 */       count++;
/* 160:    */     }
/* 161:160 */     if (this.charsetDecoderCache != null) {
/* 162:161 */       count++;
/* 163:    */     }
/* 164:164 */     for (Object o : this.indexedVariables) {
/* 165:165 */       if (o != UNSET) {
/* 166:166 */         count++;
/* 167:    */       }
/* 168:    */     }
/* 169:172 */     return count - 1;
/* 170:    */   }
/* 171:    */   
/* 172:    */   public StringBuilder stringBuilder()
/* 173:    */   {
/* 174:176 */     StringBuilder builder = this.stringBuilder;
/* 175:177 */     if (builder == null) {
/* 176:178 */       this.stringBuilder = (builder = new StringBuilder(512));
/* 177:    */     } else {
/* 178:180 */       builder.setLength(0);
/* 179:    */     }
/* 180:182 */     return builder;
/* 181:    */   }
/* 182:    */   
/* 183:    */   public Map<Charset, CharsetEncoder> charsetEncoderCache()
/* 184:    */   {
/* 185:186 */     Map<Charset, CharsetEncoder> cache = this.charsetEncoderCache;
/* 186:187 */     if (cache == null) {
/* 187:188 */       this.charsetEncoderCache = (cache = new IdentityHashMap());
/* 188:    */     }
/* 189:190 */     return cache;
/* 190:    */   }
/* 191:    */   
/* 192:    */   public Map<Charset, CharsetDecoder> charsetDecoderCache()
/* 193:    */   {
/* 194:194 */     Map<Charset, CharsetDecoder> cache = this.charsetDecoderCache;
/* 195:195 */     if (cache == null) {
/* 196:196 */       this.charsetDecoderCache = (cache = new IdentityHashMap());
/* 197:    */     }
/* 198:198 */     return cache;
/* 199:    */   }
/* 200:    */   
/* 201:    */   public int futureListenerStackDepth()
/* 202:    */   {
/* 203:202 */     return this.futureListenerStackDepth;
/* 204:    */   }
/* 205:    */   
/* 206:    */   public void setFutureListenerStackDepth(int futureListenerStackDepth)
/* 207:    */   {
/* 208:206 */     this.futureListenerStackDepth = futureListenerStackDepth;
/* 209:    */   }
/* 210:    */   
/* 211:    */   public ThreadLocalRandom random()
/* 212:    */   {
/* 213:210 */     ThreadLocalRandom r = this.random;
/* 214:211 */     if (r == null) {
/* 215:212 */       this.random = (r = new ThreadLocalRandom());
/* 216:    */     }
/* 217:214 */     return r;
/* 218:    */   }
/* 219:    */   
/* 220:    */   public Map<Class<?>, TypeParameterMatcher> typeParameterMatcherGetCache()
/* 221:    */   {
/* 222:218 */     Map<Class<?>, TypeParameterMatcher> cache = this.typeParameterMatcherGetCache;
/* 223:219 */     if (cache == null) {
/* 224:220 */       this.typeParameterMatcherGetCache = (cache = new IdentityHashMap());
/* 225:    */     }
/* 226:222 */     return cache;
/* 227:    */   }
/* 228:    */   
/* 229:    */   public Map<Class<?>, Map<String, TypeParameterMatcher>> typeParameterMatcherFindCache()
/* 230:    */   {
/* 231:226 */     Map<Class<?>, Map<String, TypeParameterMatcher>> cache = this.typeParameterMatcherFindCache;
/* 232:227 */     if (cache == null) {
/* 233:228 */       this.typeParameterMatcherFindCache = (cache = new IdentityHashMap());
/* 234:    */     }
/* 235:230 */     return cache;
/* 236:    */   }
/* 237:    */   
/* 238:    */   public IntegerHolder counterHashCode()
/* 239:    */   {
/* 240:234 */     return this.counterHashCode;
/* 241:    */   }
/* 242:    */   
/* 243:    */   public void setCounterHashCode(IntegerHolder counterHashCode)
/* 244:    */   {
/* 245:238 */     this.counterHashCode = counterHashCode;
/* 246:    */   }
/* 247:    */   
/* 248:    */   public Map<Class<?>, Boolean> handlerSharableCache()
/* 249:    */   {
/* 250:242 */     Map<Class<?>, Boolean> cache = this.handlerSharableCache;
/* 251:243 */     if (cache == null) {
/* 252:245 */       this.handlerSharableCache = (cache = new WeakHashMap(4));
/* 253:    */     }
/* 254:247 */     return cache;
/* 255:    */   }
/* 256:    */   
/* 257:    */   public int localChannelReaderStackDepth()
/* 258:    */   {
/* 259:251 */     return this.localChannelReaderStackDepth;
/* 260:    */   }
/* 261:    */   
/* 262:    */   public void setLocalChannelReaderStackDepth(int localChannelReaderStackDepth)
/* 263:    */   {
/* 264:255 */     this.localChannelReaderStackDepth = localChannelReaderStackDepth;
/* 265:    */   }
/* 266:    */   
/* 267:    */   public Object indexedVariable(int index)
/* 268:    */   {
/* 269:259 */     Object[] lookup = this.indexedVariables;
/* 270:260 */     return index < lookup.length ? lookup[index] : UNSET;
/* 271:    */   }
/* 272:    */   
/* 273:    */   public boolean setIndexedVariable(int index, Object value)
/* 274:    */   {
/* 275:267 */     Object[] lookup = this.indexedVariables;
/* 276:268 */     if (index < lookup.length)
/* 277:    */     {
/* 278:269 */       Object oldValue = lookup[index];
/* 279:270 */       lookup[index] = value;
/* 280:271 */       return oldValue == UNSET;
/* 281:    */     }
/* 282:273 */     expandIndexedVariableTableAndSet(index, value);
/* 283:274 */     return true;
/* 284:    */   }
/* 285:    */   
/* 286:    */   private void expandIndexedVariableTableAndSet(int index, Object value)
/* 287:    */   {
/* 288:279 */     Object[] oldArray = this.indexedVariables;
/* 289:280 */     int oldCapacity = oldArray.length;
/* 290:281 */     int newCapacity = index;
/* 291:282 */     newCapacity |= newCapacity >>> 1;
/* 292:283 */     newCapacity |= newCapacity >>> 2;
/* 293:284 */     newCapacity |= newCapacity >>> 4;
/* 294:285 */     newCapacity |= newCapacity >>> 8;
/* 295:286 */     newCapacity |= newCapacity >>> 16;
/* 296:287 */     newCapacity++;
/* 297:    */     
/* 298:289 */     Object[] newArray = Arrays.copyOf(oldArray, newCapacity);
/* 299:290 */     Arrays.fill(newArray, oldCapacity, newArray.length, UNSET);
/* 300:291 */     newArray[index] = value;
/* 301:292 */     this.indexedVariables = newArray;
/* 302:    */   }
/* 303:    */   
/* 304:    */   public Object removeIndexedVariable(int index)
/* 305:    */   {
/* 306:296 */     Object[] lookup = this.indexedVariables;
/* 307:297 */     if (index < lookup.length)
/* 308:    */     {
/* 309:298 */       Object v = lookup[index];
/* 310:299 */       lookup[index] = UNSET;
/* 311:300 */       return v;
/* 312:    */     }
/* 313:302 */     return UNSET;
/* 314:    */   }
/* 315:    */   
/* 316:    */   public boolean isIndexedVariableSet(int index)
/* 317:    */   {
/* 318:307 */     Object[] lookup = this.indexedVariables;
/* 319:308 */     return (index < lookup.length) && (lookup[index] != UNSET);
/* 320:    */   }
/* 321:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.InternalThreadLocalMap
 * JD-Core Version:    0.7.0.1
 */