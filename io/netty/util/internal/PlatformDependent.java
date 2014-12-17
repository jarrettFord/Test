/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.CharsetUtil;
/*   4:    */ import io.netty.util.internal.chmv8.ConcurrentHashMapV8;
/*   5:    */ import io.netty.util.internal.logging.InternalLogger;
/*   6:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   7:    */ import java.io.BufferedReader;
/*   8:    */ import java.io.File;
/*   9:    */ import java.io.IOException;
/*  10:    */ import java.io.InputStreamReader;
/*  11:    */ import java.lang.reflect.Field;
/*  12:    */ import java.lang.reflect.Method;
/*  13:    */ import java.net.InetSocketAddress;
/*  14:    */ import java.net.ServerSocket;
/*  15:    */ import java.nio.ByteBuffer;
/*  16:    */ import java.util.List;
/*  17:    */ import java.util.Locale;
/*  18:    */ import java.util.Map;
/*  19:    */ import java.util.Queue;
/*  20:    */ import java.util.concurrent.BlockingQueue;
/*  21:    */ import java.util.concurrent.ConcurrentHashMap;
/*  22:    */ import java.util.concurrent.ConcurrentMap;
/*  23:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  24:    */ import java.util.concurrent.atomic.AtomicLongFieldUpdater;
/*  25:    */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*  26:    */ import java.util.regex.Matcher;
/*  27:    */ import java.util.regex.Pattern;
/*  28:    */ 
/*  29:    */ public final class PlatformDependent
/*  30:    */ {
/*  31: 56 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(PlatformDependent.class);
/*  32: 58 */   private static final Pattern MAX_DIRECT_MEMORY_SIZE_ARG_PATTERN = Pattern.compile("\\s*-XX:MaxDirectMemorySize\\s*=\\s*([0-9]+)\\s*([kKmMgG]?)\\s*$");
/*  33: 61 */   private static final boolean IS_ANDROID = isAndroid0();
/*  34: 62 */   private static final boolean IS_WINDOWS = isWindows0();
/*  35: 63 */   private static final boolean IS_ROOT = isRoot0();
/*  36: 65 */   private static final int JAVA_VERSION = javaVersion0();
/*  37: 67 */   private static final boolean CAN_ENABLE_TCP_NODELAY_BY_DEFAULT = !isAndroid();
/*  38: 69 */   private static final boolean HAS_UNSAFE = hasUnsafe0();
/*  39: 70 */   private static final boolean CAN_USE_CHM_V8 = (HAS_UNSAFE) && (JAVA_VERSION < 8);
/*  40: 71 */   private static final boolean DIRECT_BUFFER_PREFERRED = (HAS_UNSAFE) && (!SystemPropertyUtil.getBoolean("io.netty.noPreferDirect", false));
/*  41: 73 */   private static final long MAX_DIRECT_MEMORY = maxDirectMemory0();
/*  42: 75 */   private static final long ARRAY_BASE_OFFSET = arrayBaseOffset0();
/*  43: 77 */   private static final boolean HAS_JAVASSIST = hasJavassist0();
/*  44: 79 */   private static final File TMPDIR = tmpdir0();
/*  45: 81 */   private static final int BIT_MODE = bitMode0();
/*  46:    */   
/*  47:    */   static
/*  48:    */   {
/*  49: 84 */     if (logger.isDebugEnabled()) {
/*  50: 85 */       logger.debug("-Dio.netty.noPreferDirect: {}", Boolean.valueOf(!DIRECT_BUFFER_PREFERRED));
/*  51:    */     }
/*  52: 88 */     if ((!hasUnsafe()) && (!isAndroid())) {
/*  53: 89 */       logger.info("Your platform does not provide complete low-level API for accessing direct buffers reliably. Unless explicitly requested, heap buffer will always be preferred to avoid potential system unstability.");
/*  54:    */     }
/*  55:    */   }
/*  56:    */   
/*  57:    */   public static boolean isAndroid()
/*  58:    */   {
/*  59:100 */     return IS_ANDROID;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public static boolean isWindows()
/*  63:    */   {
/*  64:107 */     return IS_WINDOWS;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public static boolean isRoot()
/*  68:    */   {
/*  69:115 */     return IS_ROOT;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public static int javaVersion()
/*  73:    */   {
/*  74:122 */     return JAVA_VERSION;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public static boolean canEnableTcpNoDelayByDefault()
/*  78:    */   {
/*  79:129 */     return CAN_ENABLE_TCP_NODELAY_BY_DEFAULT;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public static boolean hasUnsafe()
/*  83:    */   {
/*  84:137 */     return HAS_UNSAFE;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public static boolean directBufferPreferred()
/*  88:    */   {
/*  89:145 */     return DIRECT_BUFFER_PREFERRED;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public static long maxDirectMemory()
/*  93:    */   {
/*  94:152 */     return MAX_DIRECT_MEMORY;
/*  95:    */   }
/*  96:    */   
/*  97:    */   public static boolean hasJavassist()
/*  98:    */   {
/*  99:159 */     return HAS_JAVASSIST;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public static File tmpdir()
/* 103:    */   {
/* 104:166 */     return TMPDIR;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public static int bitMode()
/* 108:    */   {
/* 109:173 */     return BIT_MODE;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public static void throwException(Throwable t)
/* 113:    */   {
/* 114:180 */     if (hasUnsafe()) {
/* 115:181 */       PlatformDependent0.throwException(t);
/* 116:    */     } else {
/* 117:183 */       throwException0(t);
/* 118:    */     }
/* 119:    */   }
/* 120:    */   
/* 121:    */   private static <E extends Throwable> void throwException0(Throwable t)
/* 122:    */     throws Throwable
/* 123:    */   {
/* 124:189 */     throw t;
/* 125:    */   }
/* 126:    */   
/* 127:    */   public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap()
/* 128:    */   {
/* 129:196 */     if (CAN_USE_CHM_V8) {
/* 130:197 */       return new ConcurrentHashMapV8();
/* 131:    */     }
/* 132:199 */     return new ConcurrentHashMap();
/* 133:    */   }
/* 134:    */   
/* 135:    */   public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap(int initialCapacity)
/* 136:    */   {
/* 137:207 */     if (CAN_USE_CHM_V8) {
/* 138:208 */       return new ConcurrentHashMapV8(initialCapacity);
/* 139:    */     }
/* 140:210 */     return new ConcurrentHashMap(initialCapacity);
/* 141:    */   }
/* 142:    */   
/* 143:    */   public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap(int initialCapacity, float loadFactor)
/* 144:    */   {
/* 145:218 */     if (CAN_USE_CHM_V8) {
/* 146:219 */       return new ConcurrentHashMapV8(initialCapacity, loadFactor);
/* 147:    */     }
/* 148:221 */     return new ConcurrentHashMap(initialCapacity, loadFactor);
/* 149:    */   }
/* 150:    */   
/* 151:    */   public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel)
/* 152:    */   {
/* 153:230 */     if (CAN_USE_CHM_V8) {
/* 154:231 */       return new ConcurrentHashMapV8(initialCapacity, loadFactor, concurrencyLevel);
/* 155:    */     }
/* 156:233 */     return new ConcurrentHashMap(initialCapacity, loadFactor, concurrencyLevel);
/* 157:    */   }
/* 158:    */   
/* 159:    */   public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap(Map<? extends K, ? extends V> map)
/* 160:    */   {
/* 161:241 */     if (CAN_USE_CHM_V8) {
/* 162:242 */       return new ConcurrentHashMapV8(map);
/* 163:    */     }
/* 164:244 */     return new ConcurrentHashMap(map);
/* 165:    */   }
/* 166:    */   
/* 167:    */   public static void freeDirectBuffer(ByteBuffer buffer)
/* 168:    */   {
/* 169:253 */     if ((hasUnsafe()) && (!isAndroid())) {
/* 170:256 */       PlatformDependent0.freeDirectBuffer(buffer);
/* 171:    */     }
/* 172:    */   }
/* 173:    */   
/* 174:    */   public static long directBufferAddress(ByteBuffer buffer)
/* 175:    */   {
/* 176:261 */     return PlatformDependent0.directBufferAddress(buffer);
/* 177:    */   }
/* 178:    */   
/* 179:    */   public static Object getObject(Object object, long fieldOffset)
/* 180:    */   {
/* 181:265 */     return PlatformDependent0.getObject(object, fieldOffset);
/* 182:    */   }
/* 183:    */   
/* 184:    */   public static Object getObjectVolatile(Object object, long fieldOffset)
/* 185:    */   {
/* 186:269 */     return PlatformDependent0.getObjectVolatile(object, fieldOffset);
/* 187:    */   }
/* 188:    */   
/* 189:    */   public static int getInt(Object object, long fieldOffset)
/* 190:    */   {
/* 191:273 */     return PlatformDependent0.getInt(object, fieldOffset);
/* 192:    */   }
/* 193:    */   
/* 194:    */   public static long objectFieldOffset(Field field)
/* 195:    */   {
/* 196:277 */     return PlatformDependent0.objectFieldOffset(field);
/* 197:    */   }
/* 198:    */   
/* 199:    */   public static byte getByte(long address)
/* 200:    */   {
/* 201:281 */     return PlatformDependent0.getByte(address);
/* 202:    */   }
/* 203:    */   
/* 204:    */   public static short getShort(long address)
/* 205:    */   {
/* 206:285 */     return PlatformDependent0.getShort(address);
/* 207:    */   }
/* 208:    */   
/* 209:    */   public static int getInt(long address)
/* 210:    */   {
/* 211:289 */     return PlatformDependent0.getInt(address);
/* 212:    */   }
/* 213:    */   
/* 214:    */   public static long getLong(long address)
/* 215:    */   {
/* 216:293 */     return PlatformDependent0.getLong(address);
/* 217:    */   }
/* 218:    */   
/* 219:    */   public static void putOrderedObject(Object object, long address, Object value)
/* 220:    */   {
/* 221:297 */     PlatformDependent0.putOrderedObject(object, address, value);
/* 222:    */   }
/* 223:    */   
/* 224:    */   public static void putByte(long address, byte value)
/* 225:    */   {
/* 226:301 */     PlatformDependent0.putByte(address, value);
/* 227:    */   }
/* 228:    */   
/* 229:    */   public static void putShort(long address, short value)
/* 230:    */   {
/* 231:305 */     PlatformDependent0.putShort(address, value);
/* 232:    */   }
/* 233:    */   
/* 234:    */   public static void putInt(long address, int value)
/* 235:    */   {
/* 236:309 */     PlatformDependent0.putInt(address, value);
/* 237:    */   }
/* 238:    */   
/* 239:    */   public static void putLong(long address, long value)
/* 240:    */   {
/* 241:313 */     PlatformDependent0.putLong(address, value);
/* 242:    */   }
/* 243:    */   
/* 244:    */   public static void copyMemory(long srcAddr, long dstAddr, long length)
/* 245:    */   {
/* 246:317 */     PlatformDependent0.copyMemory(srcAddr, dstAddr, length);
/* 247:    */   }
/* 248:    */   
/* 249:    */   public static void copyMemory(byte[] src, int srcIndex, long dstAddr, long length)
/* 250:    */   {
/* 251:321 */     PlatformDependent0.copyMemory(src, ARRAY_BASE_OFFSET + srcIndex, null, dstAddr, length);
/* 252:    */   }
/* 253:    */   
/* 254:    */   public static void copyMemory(long srcAddr, byte[] dst, int dstIndex, long length)
/* 255:    */   {
/* 256:325 */     PlatformDependent0.copyMemory(null, srcAddr, dst, ARRAY_BASE_OFFSET + dstIndex, length);
/* 257:    */   }
/* 258:    */   
/* 259:    */   public static <U, W> AtomicReferenceFieldUpdater<U, W> newAtomicReferenceFieldUpdater(Class<U> tclass, String fieldName)
/* 260:    */   {
/* 261:335 */     if (hasUnsafe()) {
/* 262:    */       try
/* 263:    */       {
/* 264:337 */         return PlatformDependent0.newAtomicReferenceFieldUpdater(tclass, fieldName);
/* 265:    */       }
/* 266:    */       catch (Throwable ignore) {}
/* 267:    */     }
/* 268:342 */     return null;
/* 269:    */   }
/* 270:    */   
/* 271:    */   public static <T> AtomicIntegerFieldUpdater<T> newAtomicIntegerFieldUpdater(Class<?> tclass, String fieldName)
/* 272:    */   {
/* 273:352 */     if (hasUnsafe()) {
/* 274:    */       try
/* 275:    */       {
/* 276:354 */         return PlatformDependent0.newAtomicIntegerFieldUpdater(tclass, fieldName);
/* 277:    */       }
/* 278:    */       catch (Throwable ignore) {}
/* 279:    */     }
/* 280:359 */     return null;
/* 281:    */   }
/* 282:    */   
/* 283:    */   public static <T> AtomicLongFieldUpdater<T> newAtomicLongFieldUpdater(Class<?> tclass, String fieldName)
/* 284:    */   {
/* 285:369 */     if (hasUnsafe()) {
/* 286:    */       try
/* 287:    */       {
/* 288:371 */         return PlatformDependent0.newAtomicLongFieldUpdater(tclass, fieldName);
/* 289:    */       }
/* 290:    */       catch (Throwable ignore) {}
/* 291:    */     }
/* 292:376 */     return null;
/* 293:    */   }
/* 294:    */   
/* 295:    */   public static <T> Queue<T> newMpscQueue()
/* 296:    */   {
/* 297:384 */     return new MpscLinkedQueue();
/* 298:    */   }
/* 299:    */   
/* 300:    */   public static ClassLoader getClassLoader(Class<?> clazz)
/* 301:    */   {
/* 302:391 */     return PlatformDependent0.getClassLoader(clazz);
/* 303:    */   }
/* 304:    */   
/* 305:    */   public static ClassLoader getContextClassLoader()
/* 306:    */   {
/* 307:398 */     return PlatformDependent0.getContextClassLoader();
/* 308:    */   }
/* 309:    */   
/* 310:    */   public static ClassLoader getSystemClassLoader()
/* 311:    */   {
/* 312:405 */     return PlatformDependent0.getSystemClassLoader();
/* 313:    */   }
/* 314:    */   
/* 315:    */   private static boolean isAndroid0()
/* 316:    */   {
/* 317:    */     boolean android;
/* 318:    */     try
/* 319:    */     {
/* 320:411 */       Class.forName("android.app.Application", false, getSystemClassLoader());
/* 321:412 */       android = true;
/* 322:    */     }
/* 323:    */     catch (Exception e)
/* 324:    */     {
/* 325:415 */       android = false;
/* 326:    */     }
/* 327:418 */     if (android) {
/* 328:419 */       logger.debug("Platform: Android");
/* 329:    */     }
/* 330:421 */     return android;
/* 331:    */   }
/* 332:    */   
/* 333:    */   private static boolean isWindows0()
/* 334:    */   {
/* 335:425 */     boolean windows = SystemPropertyUtil.get("os.name", "").toLowerCase(Locale.US).contains("win");
/* 336:426 */     if (windows) {
/* 337:427 */       logger.debug("Platform: Windows");
/* 338:    */     }
/* 339:429 */     return windows;
/* 340:    */   }
/* 341:    */   
/* 342:    */   private static boolean isRoot0()
/* 343:    */   {
/* 344:433 */     if (isWindows()) {
/* 345:434 */       return false;
/* 346:    */     }
/* 347:437 */     String[] ID_COMMANDS = { "/usr/bin/id", "/bin/id", "id", "/usr/xpg4/bin/id" };
/* 348:438 */     Pattern UID_PATTERN = Pattern.compile("^(?:0|[1-9][0-9]*)$");
/* 349:    */     String idCmd;
/* 350:439 */     for (idCmd : ID_COMMANDS)
/* 351:    */     {
/* 352:440 */       Process p = null;
/* 353:441 */       BufferedReader in = null;
/* 354:442 */       String uid = null;
/* 355:    */       try
/* 356:    */       {
/* 357:444 */         p = Runtime.getRuntime().exec(new String[] { idCmd, "-u" });
/* 358:445 */         in = new BufferedReader(new InputStreamReader(p.getInputStream(), CharsetUtil.US_ASCII));
/* 359:446 */         uid = in.readLine();
/* 360:447 */         in.close();
/* 361:    */         for (;;)
/* 362:    */         {
/* 363:    */           try
/* 364:    */           {
/* 365:451 */             int exitCode = p.waitFor();
/* 366:452 */             if (exitCode != 0) {
/* 367:453 */               uid = null;
/* 368:    */             }
/* 369:    */           }
/* 370:    */           catch (InterruptedException e) {}
/* 371:    */         }
/* 372:464 */         if (in != null) {
/* 373:    */           try
/* 374:    */           {
/* 375:466 */             in.close();
/* 376:    */           }
/* 377:    */           catch (IOException e) {}
/* 378:    */         }
/* 379:471 */         if (p != null) {
/* 380:    */           try
/* 381:    */           {
/* 382:473 */             p.destroy();
/* 383:    */           }
/* 384:    */           catch (Exception e) {}
/* 385:    */         }
/* 386:480 */         if (uid == null) {
/* 387:    */           continue;
/* 388:    */         }
/* 389:    */       }
/* 390:    */       catch (Exception e)
/* 391:    */       {
/* 392:462 */         uid = null;
/* 393:    */       }
/* 394:    */       finally
/* 395:    */       {
/* 396:464 */         if (in != null) {
/* 397:    */           try
/* 398:    */           {
/* 399:466 */             in.close();
/* 400:    */           }
/* 401:    */           catch (IOException e) {}
/* 402:    */         }
/* 403:471 */         if (p != null) {
/* 404:    */           try
/* 405:    */           {
/* 406:473 */             p.destroy();
/* 407:    */           }
/* 408:    */           catch (Exception e) {}
/* 409:    */         }
/* 410:    */       }
/* 411:480 */       if (UID_PATTERN.matcher(uid).matches())
/* 412:    */       {
/* 413:481 */         logger.debug("UID: {}", uid);
/* 414:482 */         return "0".equals(uid);
/* 415:    */       }
/* 416:    */     }
/* 417:486 */     logger.debug("Could not determine the current UID using /usr/bin/id; attempting to bind at privileged ports.");
/* 418:    */     
/* 419:488 */     Pattern PERMISSION_DENIED = Pattern.compile(".*(?:denied|not.*permitted).*");
/* 420:489 */     for (int i = 1023; i > 0; i--)
/* 421:    */     {
/* 422:490 */       ServerSocket ss = null;
/* 423:    */       try
/* 424:    */       {
/* 425:492 */         ss = new ServerSocket();
/* 426:493 */         ss.setReuseAddress(true);
/* 427:494 */         ss.bind(new InetSocketAddress(i));
/* 428:495 */         if (logger.isDebugEnabled()) {
/* 429:496 */           logger.debug("UID: 0 (succeded to bind at port {})", Integer.valueOf(i));
/* 430:    */         }
/* 431:498 */         return 1;
/* 432:    */       }
/* 433:    */       catch (Exception e)
/* 434:    */       {
/* 435:502 */         String message = e.getMessage();
/* 436:503 */         if (message == null) {
/* 437:504 */           message = "";
/* 438:    */         }
/* 439:506 */         message = message.toLowerCase();
/* 440:507 */         if (PERMISSION_DENIED.matcher(message).matches())
/* 441:    */         {
/* 442:511 */           if (ss == null) {
/* 443:    */             break;
/* 444:    */           }
/* 445:    */           try
/* 446:    */           {
/* 447:513 */             ss.close();
/* 448:    */           }
/* 449:    */           catch (Exception e) {}
/* 450:    */         }
/* 451:    */       }
/* 452:    */       finally
/* 453:    */       {
/* 454:511 */         if (ss != null) {
/* 455:    */           try
/* 456:    */           {
/* 457:513 */             ss.close();
/* 458:    */           }
/* 459:    */           catch (Exception e) {}
/* 460:    */         }
/* 461:    */       }
/* 462:    */     }
/* 463:521 */     logger.debug("UID: non-root (failed to bind at any privileged ports)");
/* 464:522 */     return false;
/* 465:    */   }
/* 466:    */   
/* 467:    */   private static int javaVersion0()
/* 468:    */   {
/* 469:    */     int javaVersion;
/* 470:    */     int javaVersion;
/* 471:532 */     if (isAndroid()) {
/* 472:533 */       javaVersion = 6;
/* 473:    */     } else {
/* 474:    */       try
/* 475:    */       {
/* 476:538 */         Class.forName("java.time.Clock", false, getClassLoader(Object.class));
/* 477:539 */         javaVersion = 8;
/* 478:    */       }
/* 479:    */       catch (Exception e)
/* 480:    */       {
/* 481:    */         try
/* 482:    */         {
/* 483:546 */           Class.forName("java.util.concurrent.LinkedTransferQueue", false, getClassLoader(BlockingQueue.class));
/* 484:547 */           javaVersion = 7;
/* 485:    */         }
/* 486:    */         catch (Exception e)
/* 487:    */         {
/* 488:553 */           javaVersion = 6;
/* 489:    */         }
/* 490:    */       }
/* 491:    */     }
/* 492:557 */     if (logger.isDebugEnabled()) {
/* 493:558 */       logger.debug("Java version: {}", Integer.valueOf(javaVersion));
/* 494:    */     }
/* 495:560 */     return javaVersion;
/* 496:    */   }
/* 497:    */   
/* 498:    */   private static boolean hasUnsafe0()
/* 499:    */   {
/* 500:564 */     boolean noUnsafe = SystemPropertyUtil.getBoolean("io.netty.noUnsafe", false);
/* 501:565 */     logger.debug("-Dio.netty.noUnsafe: {}", Boolean.valueOf(noUnsafe));
/* 502:567 */     if (isAndroid())
/* 503:    */     {
/* 504:568 */       logger.debug("sun.misc.Unsafe: unavailable (Android)");
/* 505:569 */       return false;
/* 506:    */     }
/* 507:572 */     if (noUnsafe)
/* 508:    */     {
/* 509:573 */       logger.debug("sun.misc.Unsafe: unavailable (io.netty.noUnsafe)");
/* 510:574 */       return false;
/* 511:    */     }
/* 512:    */     boolean tryUnsafe;
/* 513:    */     boolean tryUnsafe;
/* 514:579 */     if (SystemPropertyUtil.contains("io.netty.tryUnsafe")) {
/* 515:580 */       tryUnsafe = SystemPropertyUtil.getBoolean("io.netty.tryUnsafe", true);
/* 516:    */     } else {
/* 517:582 */       tryUnsafe = SystemPropertyUtil.getBoolean("org.jboss.netty.tryUnsafe", true);
/* 518:    */     }
/* 519:585 */     if (!tryUnsafe)
/* 520:    */     {
/* 521:586 */       logger.debug("sun.misc.Unsafe: unavailable (io.netty.tryUnsafe/org.jboss.netty.tryUnsafe)");
/* 522:587 */       return false;
/* 523:    */     }
/* 524:    */     try
/* 525:    */     {
/* 526:591 */       boolean hasUnsafe = PlatformDependent0.hasUnsafe();
/* 527:592 */       logger.debug("sun.misc.Unsafe: {}", hasUnsafe ? "available" : "unavailable");
/* 528:593 */       return hasUnsafe;
/* 529:    */     }
/* 530:    */     catch (Throwable t) {}
/* 531:596 */     return false;
/* 532:    */   }
/* 533:    */   
/* 534:    */   private static long arrayBaseOffset0()
/* 535:    */   {
/* 536:601 */     if (!hasUnsafe()) {
/* 537:602 */       return -1L;
/* 538:    */     }
/* 539:605 */     return PlatformDependent0.arrayBaseOffset();
/* 540:    */   }
/* 541:    */   
/* 542:    */   private static long maxDirectMemory0()
/* 543:    */   {
/* 544:609 */     long maxDirectMemory = 0L;
/* 545:    */     try
/* 546:    */     {
/* 547:612 */       Class<?> vmClass = Class.forName("sun.misc.VM", true, getSystemClassLoader());
/* 548:613 */       Method m = vmClass.getDeclaredMethod("maxDirectMemory", new Class[0]);
/* 549:614 */       maxDirectMemory = ((Number)m.invoke(null, new Object[0])).longValue();
/* 550:    */     }
/* 551:    */     catch (Throwable t) {}
/* 552:619 */     if (maxDirectMemory > 0L) {
/* 553:620 */       return maxDirectMemory;
/* 554:    */     }
/* 555:    */     try
/* 556:    */     {
/* 557:626 */       Class<?> mgmtFactoryClass = Class.forName("java.lang.management.ManagementFactory", true, getSystemClassLoader());
/* 558:    */       
/* 559:628 */       Class<?> runtimeClass = Class.forName("java.lang.management.RuntimeMXBean", true, getSystemClassLoader());
/* 560:    */       
/* 561:    */ 
/* 562:631 */       Object runtime = mgmtFactoryClass.getDeclaredMethod("getRuntimeMXBean", new Class[0]).invoke(null, new Object[0]);
/* 563:    */       
/* 564:    */ 
/* 565:634 */       List<String> vmArgs = (List)runtimeClass.getDeclaredMethod("getInputArguments", new Class[0]).invoke(runtime, new Object[0]);
/* 566:635 */       for (int i = vmArgs.size() - 1; i >= 0; i--)
/* 567:    */       {
/* 568:636 */         Matcher m = MAX_DIRECT_MEMORY_SIZE_ARG_PATTERN.matcher((CharSequence)vmArgs.get(i));
/* 569:637 */         if (m.matches())
/* 570:    */         {
/* 571:641 */           maxDirectMemory = Long.parseLong(m.group(1));
/* 572:642 */           switch (m.group(2).charAt(0))
/* 573:    */           {
/* 574:    */           case 'K': 
/* 575:    */           case 'k': 
/* 576:644 */             maxDirectMemory *= 1024L;
/* 577:645 */             break;
/* 578:    */           case 'M': 
/* 579:    */           case 'm': 
/* 580:647 */             maxDirectMemory *= 1048576L;
/* 581:648 */             break;
/* 582:    */           case 'G': 
/* 583:    */           case 'g': 
/* 584:650 */             maxDirectMemory *= 1073741824L;
/* 585:    */           }
/* 586:653 */           break;
/* 587:    */         }
/* 588:    */       }
/* 589:    */     }
/* 590:    */     catch (Throwable t) {}
/* 591:659 */     if (maxDirectMemory <= 0L)
/* 592:    */     {
/* 593:660 */       maxDirectMemory = Runtime.getRuntime().maxMemory();
/* 594:661 */       logger.debug("maxDirectMemory: {} bytes (maybe)", Long.valueOf(maxDirectMemory));
/* 595:    */     }
/* 596:    */     else
/* 597:    */     {
/* 598:663 */       logger.debug("maxDirectMemory: {} bytes", Long.valueOf(maxDirectMemory));
/* 599:    */     }
/* 600:666 */     return maxDirectMemory;
/* 601:    */   }
/* 602:    */   
/* 603:    */   private static boolean hasJavassist0()
/* 604:    */   {
/* 605:670 */     if (isAndroid()) {
/* 606:671 */       return false;
/* 607:    */     }
/* 608:674 */     boolean noJavassist = SystemPropertyUtil.getBoolean("io.netty.noJavassist", false);
/* 609:675 */     logger.debug("-Dio.netty.noJavassist: {}", Boolean.valueOf(noJavassist));
/* 610:677 */     if (noJavassist)
/* 611:    */     {
/* 612:678 */       logger.debug("Javassist: unavailable (io.netty.noJavassist)");
/* 613:679 */       return false;
/* 614:    */     }
/* 615:    */     try
/* 616:    */     {
/* 617:683 */       JavassistTypeParameterMatcherGenerator.generate(Object.class, getClassLoader(PlatformDependent.class));
/* 618:684 */       logger.debug("Javassist: available");
/* 619:685 */       return true;
/* 620:    */     }
/* 621:    */     catch (Throwable t)
/* 622:    */     {
/* 623:688 */       logger.debug("Javassist: unavailable");
/* 624:689 */       logger.debug("You don't have Javassist in your class path or you don't have enough permission to load dynamically generated classes.  Please check the configuration for better performance.");
/* 625:    */     }
/* 626:692 */     return false;
/* 627:    */   }
/* 628:    */   
/* 629:    */   private static File tmpdir0()
/* 630:    */   {
/* 631:    */     File f;
/* 632:    */     try
/* 633:    */     {
/* 634:699 */       f = toDirectory(SystemPropertyUtil.get("io.netty.tmpdir"));
/* 635:700 */       if (f != null)
/* 636:    */       {
/* 637:701 */         logger.debug("-Dio.netty.tmpdir: {}", f);
/* 638:702 */         return f;
/* 639:    */       }
/* 640:705 */       f = toDirectory(SystemPropertyUtil.get("java.io.tmpdir"));
/* 641:706 */       if (f != null)
/* 642:    */       {
/* 643:707 */         logger.debug("-Dio.netty.tmpdir: {} (java.io.tmpdir)", f);
/* 644:708 */         return f;
/* 645:    */       }
/* 646:712 */       if (isWindows())
/* 647:    */       {
/* 648:713 */         f = toDirectory(System.getenv("TEMP"));
/* 649:714 */         if (f != null)
/* 650:    */         {
/* 651:715 */           logger.debug("-Dio.netty.tmpdir: {} (%TEMP%)", f);
/* 652:716 */           return f;
/* 653:    */         }
/* 654:719 */         String userprofile = System.getenv("USERPROFILE");
/* 655:720 */         if (userprofile != null)
/* 656:    */         {
/* 657:721 */           f = toDirectory(userprofile + "\\AppData\\Local\\Temp");
/* 658:722 */           if (f != null)
/* 659:    */           {
/* 660:723 */             logger.debug("-Dio.netty.tmpdir: {} (%USERPROFILE%\\AppData\\Local\\Temp)", f);
/* 661:724 */             return f;
/* 662:    */           }
/* 663:727 */           f = toDirectory(userprofile + "\\Local Settings\\Temp");
/* 664:728 */           if (f != null)
/* 665:    */           {
/* 666:729 */             logger.debug("-Dio.netty.tmpdir: {} (%USERPROFILE%\\Local Settings\\Temp)", f);
/* 667:730 */             return f;
/* 668:    */           }
/* 669:    */         }
/* 670:    */       }
/* 671:    */       else
/* 672:    */       {
/* 673:734 */         f = toDirectory(System.getenv("TMPDIR"));
/* 674:735 */         if (f != null)
/* 675:    */         {
/* 676:736 */           logger.debug("-Dio.netty.tmpdir: {} ($TMPDIR)", f);
/* 677:737 */           return f;
/* 678:    */         }
/* 679:    */       }
/* 680:    */     }
/* 681:    */     catch (Exception ignored) {}
/* 682:    */     File f;
/* 683:745 */     if (isWindows()) {
/* 684:746 */       f = new File("C:\\Windows\\Temp");
/* 685:    */     } else {
/* 686:748 */       f = new File("/tmp");
/* 687:    */     }
/* 688:751 */     logger.warn("Failed to get the temporary directory; falling back to: {}", f);
/* 689:752 */     return f;
/* 690:    */   }
/* 691:    */   
/* 692:    */   private static File toDirectory(String path)
/* 693:    */   {
/* 694:757 */     if (path == null) {
/* 695:758 */       return null;
/* 696:    */     }
/* 697:761 */     File f = new File(path);
/* 698:762 */     if (!f.exists()) {
/* 699:763 */       f.mkdirs();
/* 700:    */     }
/* 701:766 */     if (!f.isDirectory()) {
/* 702:767 */       return null;
/* 703:    */     }
/* 704:    */     try
/* 705:    */     {
/* 706:771 */       return f.getAbsoluteFile();
/* 707:    */     }
/* 708:    */     catch (Exception ignored) {}
/* 709:773 */     return f;
/* 710:    */   }
/* 711:    */   
/* 712:    */   private static int bitMode0()
/* 713:    */   {
/* 714:779 */     int bitMode = SystemPropertyUtil.getInt("io.netty.bitMode", 0);
/* 715:780 */     if (bitMode > 0)
/* 716:    */     {
/* 717:781 */       logger.debug("-Dio.netty.bitMode: {}", Integer.valueOf(bitMode));
/* 718:782 */       return bitMode;
/* 719:    */     }
/* 720:786 */     bitMode = SystemPropertyUtil.getInt("sun.arch.data.model", 0);
/* 721:787 */     if (bitMode > 0)
/* 722:    */     {
/* 723:788 */       logger.debug("-Dio.netty.bitMode: {} (sun.arch.data.model)", Integer.valueOf(bitMode));
/* 724:789 */       return bitMode;
/* 725:    */     }
/* 726:791 */     bitMode = SystemPropertyUtil.getInt("com.ibm.vm.bitmode", 0);
/* 727:792 */     if (bitMode > 0)
/* 728:    */     {
/* 729:793 */       logger.debug("-Dio.netty.bitMode: {} (com.ibm.vm.bitmode)", Integer.valueOf(bitMode));
/* 730:794 */       return bitMode;
/* 731:    */     }
/* 732:798 */     String arch = SystemPropertyUtil.get("os.arch", "").toLowerCase(Locale.US).trim();
/* 733:799 */     if (("amd64".equals(arch)) || ("x86_64".equals(arch))) {
/* 734:800 */       bitMode = 64;
/* 735:801 */     } else if (("i386".equals(arch)) || ("i486".equals(arch)) || ("i586".equals(arch)) || ("i686".equals(arch))) {
/* 736:802 */       bitMode = 32;
/* 737:    */     }
/* 738:805 */     if (bitMode > 0) {
/* 739:806 */       logger.debug("-Dio.netty.bitMode: {} (os.arch: {})", Integer.valueOf(bitMode), arch);
/* 740:    */     }
/* 741:810 */     String vm = SystemPropertyUtil.get("java.vm.name", "").toLowerCase(Locale.US);
/* 742:811 */     Pattern BIT_PATTERN = Pattern.compile("([1-9][0-9]+)-?bit");
/* 743:812 */     Matcher m = BIT_PATTERN.matcher(vm);
/* 744:813 */     if (m.find()) {
/* 745:814 */       return Integer.parseInt(m.group(1));
/* 746:    */     }
/* 747:816 */     return 64;
/* 748:    */   }
/* 749:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.PlatformDependent
 * JD-Core Version:    0.7.0.1
 */