/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.logging.InternalLogger;
/*   4:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   5:    */ import java.security.SecureRandom;
/*   6:    */ import java.util.Random;
/*   7:    */ import java.util.concurrent.BlockingQueue;
/*   8:    */ import java.util.concurrent.LinkedBlockingQueue;
/*   9:    */ import java.util.concurrent.TimeUnit;
/*  10:    */ import java.util.concurrent.atomic.AtomicLong;
/*  11:    */ 
/*  12:    */ public final class ThreadLocalRandom
/*  13:    */   extends Random
/*  14:    */ {
/*  15: 63 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ThreadLocalRandom.class);
/*  16: 65 */   private static final AtomicLong seedUniquifier = new AtomicLong();
/*  17:    */   private static volatile long initialSeedUniquifier;
/*  18:    */   private static final long multiplier = 25214903917L;
/*  19:    */   private static final long addend = 11L;
/*  20:    */   private static final long mask = 281474976710655L;
/*  21:    */   private long rnd;
/*  22:    */   boolean initialized;
/*  23:    */   private long pad0;
/*  24:    */   private long pad1;
/*  25:    */   private long pad2;
/*  26:    */   private long pad3;
/*  27:    */   private long pad4;
/*  28:    */   private long pad5;
/*  29:    */   private long pad6;
/*  30:    */   private long pad7;
/*  31:    */   private static final long serialVersionUID = -5851777807851030925L;
/*  32:    */   
/*  33:    */   public static void setInitialSeedUniquifier(long initialSeedUniquifier)
/*  34:    */   {
/*  35: 70 */     initialSeedUniquifier = initialSeedUniquifier;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public static synchronized long getInitialSeedUniquifier()
/*  39:    */   {
/*  40: 75 */     long initialSeedUniquifier = initialSeedUniquifier;
/*  41: 76 */     if (initialSeedUniquifier == 0L) {
/*  42: 78 */       initialSeedUniquifier = initialSeedUniquifier = SystemPropertyUtil.getLong("io.netty.initialSeedUniquifier", 0L);
/*  43:    */     }
/*  44: 83 */     if (initialSeedUniquifier == 0L)
/*  45:    */     {
/*  46: 86 */       final BlockingQueue<byte[]> queue = new LinkedBlockingQueue();
/*  47: 87 */       Thread generatorThread = new Thread("initialSeedUniquifierGenerator")
/*  48:    */       {
/*  49:    */         public void run()
/*  50:    */         {
/*  51: 90 */           SecureRandom random = new SecureRandom();
/*  52: 91 */           queue.add(random.generateSeed(8));
/*  53:    */         }
/*  54: 93 */       };
/*  55: 94 */       generatorThread.setDaemon(true);
/*  56: 95 */       generatorThread.start();
/*  57: 96 */       generatorThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
/*  58:    */       {
/*  59:    */         public void uncaughtException(Thread t, Throwable e)
/*  60:    */         {
/*  61: 99 */           ThreadLocalRandom.logger.debug("An exception has been raised by {}", t.getName(), e);
/*  62:    */         }
/*  63:103 */       });
/*  64:104 */       long timeoutSeconds = 3L;
/*  65:105 */       long deadLine = System.nanoTime() + TimeUnit.SECONDS.toNanos(3L);
/*  66:106 */       boolean interrupted = false;
/*  67:    */       for (;;)
/*  68:    */       {
/*  69:108 */         long waitTime = deadLine - System.nanoTime();
/*  70:109 */         if (waitTime <= 0L)
/*  71:    */         {
/*  72:110 */           generatorThread.interrupt();
/*  73:111 */           logger.warn("Failed to generate a seed from SecureRandom within {} seconds. Not enough entrophy?", Long.valueOf(3L));
/*  74:    */           
/*  75:    */ 
/*  76:    */ 
/*  77:115 */           break;
/*  78:    */         }
/*  79:    */         try
/*  80:    */         {
/*  81:119 */           byte[] seed = (byte[])queue.poll(waitTime, TimeUnit.NANOSECONDS);
/*  82:120 */           if (seed != null)
/*  83:    */           {
/*  84:121 */             initialSeedUniquifier = (seed[0] & 0xFF) << 56 | (seed[1] & 0xFF) << 48 | (seed[2] & 0xFF) << 40 | (seed[3] & 0xFF) << 32 | (seed[4] & 0xFF) << 24 | (seed[5] & 0xFF) << 16 | (seed[6] & 0xFF) << 8 | seed[7] & 0xFF;
/*  85:    */             
/*  86:    */ 
/*  87:    */ 
/*  88:    */ 
/*  89:    */ 
/*  90:    */ 
/*  91:    */ 
/*  92:    */ 
/*  93:130 */             break;
/*  94:    */           }
/*  95:    */         }
/*  96:    */         catch (InterruptedException e)
/*  97:    */         {
/*  98:133 */           interrupted = true;
/*  99:134 */           logger.warn("Failed to generate a seed from SecureRandom due to an InterruptedException.");
/* 100:135 */           break;
/* 101:    */         }
/* 102:    */       }
/* 103:140 */       initialSeedUniquifier ^= 0x33BAE119;
/* 104:141 */       initialSeedUniquifier ^= Long.reverse(System.nanoTime());
/* 105:    */       
/* 106:143 */       initialSeedUniquifier = initialSeedUniquifier;
/* 107:145 */       if (interrupted)
/* 108:    */       {
/* 109:147 */         Thread.currentThread().interrupt();
/* 110:    */         
/* 111:    */ 
/* 112:    */ 
/* 113:151 */         generatorThread.interrupt();
/* 114:    */       }
/* 115:    */     }
/* 116:155 */     return initialSeedUniquifier;
/* 117:    */   }
/* 118:    */   
/* 119:    */   private static long newSeed()
/* 120:    */   {
/* 121:    */     for (;;)
/* 122:    */     {
/* 123:160 */       long current = seedUniquifier.get();
/* 124:161 */       long actualCurrent = current != 0L ? current : getInitialSeedUniquifier();
/* 125:    */       
/* 126:    */ 
/* 127:164 */       long next = actualCurrent * 181783497276652981L;
/* 128:166 */       if (seedUniquifier.compareAndSet(current, next))
/* 129:    */       {
/* 130:167 */         if ((current == 0L) && (logger.isDebugEnabled())) {
/* 131:168 */           logger.debug(String.format("-Dio.netty.initialSeedUniquifier: 0x%016x", new Object[] { Long.valueOf(actualCurrent) }));
/* 132:    */         }
/* 133:170 */         return next ^ System.nanoTime();
/* 134:    */       }
/* 135:    */     }
/* 136:    */   }
/* 137:    */   
/* 138:    */   ThreadLocalRandom()
/* 139:    */   {
/* 140:202 */     super(newSeed());
/* 141:203 */     this.initialized = true;
/* 142:    */   }
/* 143:    */   
/* 144:    */   public static ThreadLocalRandom current()
/* 145:    */   {
/* 146:212 */     return InternalThreadLocalMap.get().random();
/* 147:    */   }
/* 148:    */   
/* 149:    */   public void setSeed(long seed)
/* 150:    */   {
/* 151:222 */     if (this.initialized) {
/* 152:223 */       throw new UnsupportedOperationException();
/* 153:    */     }
/* 154:225 */     this.rnd = ((seed ^ 0xDEECE66D) & 0xFFFFFFFF);
/* 155:    */   }
/* 156:    */   
/* 157:    */   protected int next(int bits)
/* 158:    */   {
/* 159:229 */     this.rnd = (this.rnd * 25214903917L + 11L & 0xFFFFFFFF);
/* 160:230 */     return (int)(this.rnd >>> 48 - bits);
/* 161:    */   }
/* 162:    */   
/* 163:    */   public int nextInt(int least, int bound)
/* 164:    */   {
/* 165:244 */     if (least >= bound) {
/* 166:245 */       throw new IllegalArgumentException();
/* 167:    */     }
/* 168:247 */     return nextInt(bound - least) + least;
/* 169:    */   }
/* 170:    */   
/* 171:    */   public long nextLong(long n)
/* 172:    */   {
/* 173:260 */     if (n <= 0L) {
/* 174:261 */       throw new IllegalArgumentException("n must be positive");
/* 175:    */     }
/* 176:269 */     long offset = 0L;
/* 177:270 */     while (n >= 2147483647L)
/* 178:    */     {
/* 179:271 */       int bits = next(2);
/* 180:272 */       long half = n >>> 1;
/* 181:273 */       long nextn = (bits & 0x2) == 0 ? half : n - half;
/* 182:274 */       if ((bits & 0x1) == 0) {
/* 183:275 */         offset += n - nextn;
/* 184:    */       }
/* 185:277 */       n = nextn;
/* 186:    */     }
/* 187:279 */     return offset + nextInt((int)n);
/* 188:    */   }
/* 189:    */   
/* 190:    */   public long nextLong(long least, long bound)
/* 191:    */   {
/* 192:293 */     if (least >= bound) {
/* 193:294 */       throw new IllegalArgumentException();
/* 194:    */     }
/* 195:296 */     return nextLong(bound - least) + least;
/* 196:    */   }
/* 197:    */   
/* 198:    */   public double nextDouble(double n)
/* 199:    */   {
/* 200:309 */     if (n <= 0.0D) {
/* 201:310 */       throw new IllegalArgumentException("n must be positive");
/* 202:    */     }
/* 203:312 */     return nextDouble() * n;
/* 204:    */   }
/* 205:    */   
/* 206:    */   public double nextDouble(double least, double bound)
/* 207:    */   {
/* 208:326 */     if (least >= bound) {
/* 209:327 */       throw new IllegalArgumentException();
/* 210:    */     }
/* 211:329 */     return nextDouble() * (bound - least) + least;
/* 212:    */   }
/* 213:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.ThreadLocalRandom
 * JD-Core Version:    0.7.0.1
 */