/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import io.netty.util.internal.StringUtil;
/*   5:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   6:    */ import io.netty.util.internal.logging.InternalLogger;
/*   7:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   8:    */ import java.lang.ref.PhantomReference;
/*   9:    */ import java.lang.ref.ReferenceQueue;
/*  10:    */ import java.util.ArrayDeque;
/*  11:    */ import java.util.Deque;
/*  12:    */ import java.util.EnumSet;
/*  13:    */ import java.util.concurrent.ConcurrentMap;
/*  14:    */ import java.util.concurrent.atomic.AtomicBoolean;
/*  15:    */ 
/*  16:    */ public final class ResourceLeakDetector<T>
/*  17:    */ {
/*  18:    */   private static final String PROP_LEVEL = "io.netty.leakDetectionLevel";
/*  19: 37 */   private static final Level DEFAULT_LEVEL = Level.SIMPLE;
/*  20:    */   private static Level level;
/*  21:    */   
/*  22:    */   public static enum Level
/*  23:    */   {
/*  24: 46 */     DISABLED,  SIMPLE,  ADVANCED,  PARANOID;
/*  25:    */     
/*  26:    */     private Level() {}
/*  27:    */   }
/*  28:    */   
/*  29: 66 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ResourceLeakDetector.class);
/*  30:    */   private static final int DEFAULT_SAMPLING_INTERVAL = 113;
/*  31:    */   
/*  32:    */   static
/*  33:    */   {
/*  34:    */     boolean disabled;
/*  35: 70 */     if (SystemPropertyUtil.get("io.netty.noResourceLeakDetection") != null)
/*  36:    */     {
/*  37: 71 */       boolean disabled = SystemPropertyUtil.getBoolean("io.netty.noResourceLeakDetection", false);
/*  38: 72 */       logger.debug("-Dio.netty.noResourceLeakDetection: {}", Boolean.valueOf(disabled));
/*  39: 73 */       logger.warn("-Dio.netty.noResourceLeakDetection is deprecated. Use '-D{}={}' instead.", "io.netty.leakDetectionLevel", DEFAULT_LEVEL.name().toLowerCase());
/*  40:    */     }
/*  41:    */     else
/*  42:    */     {
/*  43: 77 */       disabled = false;
/*  44:    */     }
/*  45: 80 */     Level defaultLevel = disabled ? Level.DISABLED : DEFAULT_LEVEL;
/*  46: 81 */     String levelStr = SystemPropertyUtil.get("io.netty.leakDetectionLevel", defaultLevel.name()).trim().toUpperCase();
/*  47: 82 */     Level level = DEFAULT_LEVEL;
/*  48: 83 */     for (Level l : EnumSet.allOf(Level.class)) {
/*  49: 84 */       if ((levelStr.equals(l.name())) || (levelStr.equals(String.valueOf(l.ordinal())))) {
/*  50: 85 */         level = l;
/*  51:    */       }
/*  52:    */     }
/*  53: 89 */     level = level;
/*  54: 90 */     if (logger.isDebugEnabled()) {
/*  55: 91 */       logger.debug("-D{}: {}", "io.netty.leakDetectionLevel", level.name().toLowerCase());
/*  56:    */     }
/*  57:    */   }
/*  58:    */   
/*  59:    */   @Deprecated
/*  60:    */   public static void setEnabled(boolean enabled)
/*  61:    */   {
/*  62:102 */     setLevel(enabled ? Level.SIMPLE : Level.DISABLED);
/*  63:    */   }
/*  64:    */   
/*  65:    */   public static boolean isEnabled()
/*  66:    */   {
/*  67:109 */     return getLevel().ordinal() > Level.DISABLED.ordinal();
/*  68:    */   }
/*  69:    */   
/*  70:    */   public static void setLevel(Level level)
/*  71:    */   {
/*  72:116 */     if (level == null) {
/*  73:117 */       throw new NullPointerException("level");
/*  74:    */     }
/*  75:119 */     level = level;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public static Level getLevel()
/*  79:    */   {
/*  80:126 */     return level;
/*  81:    */   }
/*  82:    */   
/*  83:130 */   private final ResourceLeakDetector<T>.DefaultResourceLeak head = new DefaultResourceLeak(null);
/*  84:131 */   private final ResourceLeakDetector<T>.DefaultResourceLeak tail = new DefaultResourceLeak(null);
/*  85:133 */   private final ReferenceQueue<Object> refQueue = new ReferenceQueue();
/*  86:134 */   private final ConcurrentMap<String, Boolean> reportedLeaks = PlatformDependent.newConcurrentHashMap();
/*  87:    */   private final String resourceType;
/*  88:    */   private final int samplingInterval;
/*  89:    */   private final long maxActive;
/*  90:    */   private long active;
/*  91:140 */   private final AtomicBoolean loggedTooManyActive = new AtomicBoolean();
/*  92:    */   private long leakCheckCnt;
/*  93:    */   
/*  94:    */   public ResourceLeakDetector(Class<?> resourceType)
/*  95:    */   {
/*  96:145 */     this(StringUtil.simpleClassName(resourceType));
/*  97:    */   }
/*  98:    */   
/*  99:    */   public ResourceLeakDetector(String resourceType)
/* 100:    */   {
/* 101:149 */     this(resourceType, 113, 9223372036854775807L);
/* 102:    */   }
/* 103:    */   
/* 104:    */   public ResourceLeakDetector(Class<?> resourceType, int samplingInterval, long maxActive)
/* 105:    */   {
/* 106:153 */     this(StringUtil.simpleClassName(resourceType), samplingInterval, maxActive);
/* 107:    */   }
/* 108:    */   
/* 109:    */   public ResourceLeakDetector(String resourceType, int samplingInterval, long maxActive)
/* 110:    */   {
/* 111:157 */     if (resourceType == null) {
/* 112:158 */       throw new NullPointerException("resourceType");
/* 113:    */     }
/* 114:160 */     if (samplingInterval <= 0) {
/* 115:161 */       throw new IllegalArgumentException("samplingInterval: " + samplingInterval + " (expected: 1+)");
/* 116:    */     }
/* 117:163 */     if (maxActive <= 0L) {
/* 118:164 */       throw new IllegalArgumentException("maxActive: " + maxActive + " (expected: 1+)");
/* 119:    */     }
/* 120:167 */     this.resourceType = resourceType;
/* 121:168 */     this.samplingInterval = samplingInterval;
/* 122:169 */     this.maxActive = maxActive;
/* 123:    */     
/* 124:171 */     this.head.next = this.tail;
/* 125:172 */     this.tail.prev = this.head;
/* 126:    */   }
/* 127:    */   
/* 128:    */   public ResourceLeak open(T obj)
/* 129:    */   {
/* 130:182 */     Level level = level;
/* 131:183 */     if (level == Level.DISABLED) {
/* 132:184 */       return null;
/* 133:    */     }
/* 134:187 */     if (level.ordinal() < Level.PARANOID.ordinal())
/* 135:    */     {
/* 136:188 */       if (this.leakCheckCnt++ % this.samplingInterval == 0L)
/* 137:    */       {
/* 138:189 */         reportLeak(level);
/* 139:190 */         return new DefaultResourceLeak(obj);
/* 140:    */       }
/* 141:192 */       return null;
/* 142:    */     }
/* 143:195 */     reportLeak(level);
/* 144:196 */     return new DefaultResourceLeak(obj);
/* 145:    */   }
/* 146:    */   
/* 147:    */   private void reportLeak(Level level)
/* 148:    */   {
/* 149:201 */     if (!logger.isErrorEnabled())
/* 150:    */     {
/* 151:    */       for (;;)
/* 152:    */       {
/* 153:204 */         ResourceLeakDetector<T>.DefaultResourceLeak ref = (DefaultResourceLeak)this.refQueue.poll();
/* 154:205 */         if (ref == null) {
/* 155:    */           break;
/* 156:    */         }
/* 157:208 */         ref.close();
/* 158:    */       }
/* 159:210 */       return;
/* 160:    */     }
/* 161:214 */     int samplingInterval = level == Level.PARANOID ? 1 : this.samplingInterval;
/* 162:215 */     if ((this.active * samplingInterval > this.maxActive) && (this.loggedTooManyActive.compareAndSet(false, true))) {
/* 163:216 */       logger.error("LEAK: You are creating too many " + this.resourceType + " instances.  " + this.resourceType + " is a shared resource that must be reused across the JVM," + "so that only a few instances are created.");
/* 164:    */     }
/* 165:    */     for (;;)
/* 166:    */     {
/* 167:224 */       ResourceLeakDetector<T>.DefaultResourceLeak ref = (DefaultResourceLeak)this.refQueue.poll();
/* 168:225 */       if (ref == null) {
/* 169:    */         break;
/* 170:    */       }
/* 171:229 */       ref.clear();
/* 172:231 */       if (ref.close())
/* 173:    */       {
/* 174:235 */         String records = ref.toString();
/* 175:236 */         if (this.reportedLeaks.putIfAbsent(records, Boolean.TRUE) == null) {
/* 176:237 */           if (records.isEmpty()) {
/* 177:238 */             logger.error("LEAK: {}.release() was not called before it's garbage-collected. Enable advanced leak reporting to find out where the leak occurred. To enable advanced leak reporting, specify the JVM option '-D{}={}' or call {}.setLevel()", new Object[] { this.resourceType, "io.netty.leakDetectionLevel", Level.ADVANCED.name().toLowerCase(), StringUtil.simpleClassName(this) });
/* 178:    */           } else {
/* 179:244 */             logger.error("LEAK: {}.release() was not called before it's garbage-collected.{}", this.resourceType, records);
/* 180:    */           }
/* 181:    */         }
/* 182:    */       }
/* 183:    */     }
/* 184:    */   }
/* 185:    */   
/* 186:    */   private final class DefaultResourceLeak
/* 187:    */     extends PhantomReference<Object>
/* 188:    */     implements ResourceLeak
/* 189:    */   {
/* 190:    */     private static final int MAX_RECORDS = 4;
/* 191:    */     private final String creationRecord;
/* 192:257 */     private final Deque<String> lastRecords = new ArrayDeque();
/* 193:    */     private final AtomicBoolean freed;
/* 194:    */     private ResourceLeakDetector<T>.DefaultResourceLeak prev;
/* 195:    */     private ResourceLeakDetector<T>.DefaultResourceLeak next;
/* 196:    */     
/* 197:    */     DefaultResourceLeak(Object referent)
/* 198:    */     {
/* 199:263 */       super(referent != null ? ResourceLeakDetector.this.refQueue : null);
/* 200:    */       ResourceLeakDetector.Level level;
/* 201:265 */       if (referent != null)
/* 202:    */       {
/* 203:266 */         level = ResourceLeakDetector.getLevel();
/* 204:267 */         if (level.ordinal() >= ResourceLeakDetector.Level.ADVANCED.ordinal()) {
/* 205:268 */           this.creationRecord = ResourceLeakDetector.newRecord(3);
/* 206:    */         } else {
/* 207:270 */           this.creationRecord = null;
/* 208:    */         }
/* 209:274 */         synchronized (ResourceLeakDetector.this.head)
/* 210:    */         {
/* 211:275 */           this.prev = ResourceLeakDetector.this.head;
/* 212:276 */           this.next = ResourceLeakDetector.this.head.next;
/* 213:277 */           ResourceLeakDetector.this.head.next.prev = this;
/* 214:278 */           ResourceLeakDetector.this.head.next = this;
/* 215:279 */           ResourceLeakDetector.access$408(ResourceLeakDetector.this);
/* 216:    */         }
/* 217:281 */         this.freed = new AtomicBoolean();
/* 218:    */       }
/* 219:    */       else
/* 220:    */       {
/* 221:283 */         this.creationRecord = null;
/* 222:284 */         this.freed = new AtomicBoolean(true);
/* 223:    */       }
/* 224:    */     }
/* 225:    */     
/* 226:    */     public void record()
/* 227:    */     {
/* 228:290 */       if (this.creationRecord != null)
/* 229:    */       {
/* 230:291 */         String value = ResourceLeakDetector.newRecord(2);
/* 231:293 */         synchronized (this.lastRecords)
/* 232:    */         {
/* 233:294 */           int size = this.lastRecords.size();
/* 234:295 */           if ((size == 0) || (!((String)this.lastRecords.getLast()).equals(value))) {
/* 235:296 */             this.lastRecords.add(value);
/* 236:    */           }
/* 237:298 */           if (size > 4) {
/* 238:299 */             this.lastRecords.removeFirst();
/* 239:    */           }
/* 240:    */         }
/* 241:    */       }
/* 242:    */     }
/* 243:    */     
/* 244:    */     public boolean close()
/* 245:    */     {
/* 246:307 */       if (this.freed.compareAndSet(false, true))
/* 247:    */       {
/* 248:308 */         synchronized (ResourceLeakDetector.this.head)
/* 249:    */         {
/* 250:309 */           ResourceLeakDetector.access$410(ResourceLeakDetector.this);
/* 251:310 */           this.prev.next = this.next;
/* 252:311 */           this.next.prev = this.prev;
/* 253:312 */           this.prev = null;
/* 254:313 */           this.next = null;
/* 255:    */         }
/* 256:315 */         return true;
/* 257:    */       }
/* 258:317 */       return false;
/* 259:    */     }
/* 260:    */     
/* 261:    */     public String toString()
/* 262:    */     {
/* 263:321 */       if (this.creationRecord == null) {
/* 264:322 */         return "";
/* 265:    */       }
/* 266:    */       Object[] array;
/* 267:326 */       synchronized (this.lastRecords)
/* 268:    */       {
/* 269:327 */         array = this.lastRecords.toArray();
/* 270:    */       }
/* 271:330 */       StringBuilder buf = new StringBuilder(16384);
/* 272:331 */       buf.append(StringUtil.NEWLINE);
/* 273:332 */       buf.append("Recent access records: ");
/* 274:333 */       buf.append(array.length);
/* 275:334 */       buf.append(StringUtil.NEWLINE);
/* 276:336 */       if (array.length > 0) {
/* 277:337 */         for (int i = array.length - 1; i >= 0; i--)
/* 278:    */         {
/* 279:338 */           buf.append('#');
/* 280:339 */           buf.append(i + 1);
/* 281:340 */           buf.append(':');
/* 282:341 */           buf.append(StringUtil.NEWLINE);
/* 283:342 */           buf.append(array[i]);
/* 284:    */         }
/* 285:    */       }
/* 286:346 */       buf.append("Created at:");
/* 287:347 */       buf.append(StringUtil.NEWLINE);
/* 288:348 */       buf.append(this.creationRecord);
/* 289:349 */       buf.setLength(buf.length() - StringUtil.NEWLINE.length());
/* 290:    */       
/* 291:351 */       return buf.toString();
/* 292:    */     }
/* 293:    */   }
/* 294:    */   
/* 295:355 */   private static final String[] STACK_TRACE_ELEMENT_EXCLUSIONS = { "io.netty.buffer.AbstractByteBufAllocator.toLeakAwareBuffer(" };
/* 296:    */   
/* 297:    */   static String newRecord(int recordsToSkip)
/* 298:    */   {
/* 299:360 */     StringBuilder buf = new StringBuilder(4096);
/* 300:361 */     StackTraceElement[] array = new Throwable().getStackTrace();
/* 301:362 */     for (StackTraceElement e : array) {
/* 302:363 */       if (recordsToSkip > 0)
/* 303:    */       {
/* 304:364 */         recordsToSkip--;
/* 305:    */       }
/* 306:    */       else
/* 307:    */       {
/* 308:366 */         String estr = e.toString();
/* 309:    */         
/* 310:    */ 
/* 311:369 */         boolean excluded = false;
/* 312:370 */         for (String exclusion : STACK_TRACE_ELEMENT_EXCLUSIONS) {
/* 313:371 */           if (estr.startsWith(exclusion))
/* 314:    */           {
/* 315:372 */             excluded = true;
/* 316:373 */             break;
/* 317:    */           }
/* 318:    */         }
/* 319:377 */         if (!excluded)
/* 320:    */         {
/* 321:378 */           buf.append('\t');
/* 322:379 */           buf.append(estr);
/* 323:380 */           buf.append(StringUtil.NEWLINE);
/* 324:    */         }
/* 325:    */       }
/* 326:    */     }
/* 327:385 */     return buf.toString();
/* 328:    */   }
/* 329:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.ResourceLeakDetector
 * JD-Core Version:    0.7.0.1
 */