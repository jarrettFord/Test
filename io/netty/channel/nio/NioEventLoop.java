/*   1:    */ package io.netty.channel.nio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelException;
/*   4:    */ import io.netty.channel.EventLoopException;
/*   5:    */ import io.netty.channel.SingleThreadEventLoop;
/*   6:    */ import io.netty.util.internal.PlatformDependent;
/*   7:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   8:    */ import io.netty.util.internal.logging.InternalLogger;
/*   9:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  10:    */ import java.io.IOException;
/*  11:    */ import java.lang.reflect.Field;
/*  12:    */ import java.nio.channels.CancelledKeyException;
/*  13:    */ import java.nio.channels.SelectableChannel;
/*  14:    */ import java.nio.channels.SelectionKey;
/*  15:    */ import java.nio.channels.Selector;
/*  16:    */ import java.nio.channels.spi.SelectorProvider;
/*  17:    */ import java.util.ArrayList;
/*  18:    */ import java.util.Collection;
/*  19:    */ import java.util.ConcurrentModificationException;
/*  20:    */ import java.util.Iterator;
/*  21:    */ import java.util.Queue;
/*  22:    */ import java.util.Set;
/*  23:    */ import java.util.concurrent.ThreadFactory;
/*  24:    */ import java.util.concurrent.atomic.AtomicBoolean;
/*  25:    */ 
/*  26:    */ public final class NioEventLoop
/*  27:    */   extends SingleThreadEventLoop
/*  28:    */ {
/*  29: 52 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioEventLoop.class);
/*  30:    */   private static final int CLEANUP_INTERVAL = 256;
/*  31: 56 */   private static final boolean DISABLE_KEYSET_OPTIMIZATION = SystemPropertyUtil.getBoolean("io.netty.noKeySetOptimization", false);
/*  32:    */   private static final int MIN_PREMATURE_SELECTOR_RETURNS = 3;
/*  33:    */   private static final int SELECTOR_AUTO_REBUILD_THRESHOLD;
/*  34:    */   Selector selector;
/*  35:    */   private SelectedSelectionKeySet selectedKeys;
/*  36:    */   private final SelectorProvider provider;
/*  37:    */   
/*  38:    */   static
/*  39:    */   {
/*  40: 68 */     String key = "sun.nio.ch.bugLevel";
/*  41:    */     try
/*  42:    */     {
/*  43: 70 */       String buglevel = SystemPropertyUtil.get(key);
/*  44: 71 */       if (buglevel == null) {
/*  45: 72 */         System.setProperty(key, "");
/*  46:    */       }
/*  47:    */     }
/*  48:    */     catch (SecurityException e)
/*  49:    */     {
/*  50: 75 */       if (logger.isDebugEnabled()) {
/*  51: 76 */         logger.debug("Unable to get/set System Property: {}", key, e);
/*  52:    */       }
/*  53:    */     }
/*  54: 80 */     int selectorAutoRebuildThreshold = SystemPropertyUtil.getInt("io.netty.selectorAutoRebuildThreshold", 512);
/*  55: 81 */     if (selectorAutoRebuildThreshold < 3) {
/*  56: 82 */       selectorAutoRebuildThreshold = 0;
/*  57:    */     }
/*  58: 85 */     SELECTOR_AUTO_REBUILD_THRESHOLD = selectorAutoRebuildThreshold;
/*  59: 87 */     if (logger.isDebugEnabled())
/*  60:    */     {
/*  61: 88 */       logger.debug("-Dio.netty.noKeySetOptimization: {}", Boolean.valueOf(DISABLE_KEYSET_OPTIMIZATION));
/*  62: 89 */       logger.debug("-Dio.netty.selectorAutoRebuildThreshold: {}", Integer.valueOf(SELECTOR_AUTO_REBUILD_THRESHOLD));
/*  63:    */     }
/*  64:    */   }
/*  65:    */   
/*  66:107 */   private final AtomicBoolean wakenUp = new AtomicBoolean();
/*  67:    */   private boolean oldWakenUp;
/*  68:110 */   private volatile int ioRatio = 50;
/*  69:    */   private int cancelledKeys;
/*  70:    */   private boolean needsToSelectAgain;
/*  71:    */   
/*  72:    */   NioEventLoop(NioEventLoopGroup parent, ThreadFactory threadFactory, SelectorProvider selectorProvider)
/*  73:    */   {
/*  74:115 */     super(parent, threadFactory, false);
/*  75:116 */     if (selectorProvider == null) {
/*  76:117 */       throw new NullPointerException("selectorProvider");
/*  77:    */     }
/*  78:119 */     this.provider = selectorProvider;
/*  79:120 */     this.selector = openSelector();
/*  80:    */   }
/*  81:    */   
/*  82:    */   private Selector openSelector()
/*  83:    */   {
/*  84:    */     Selector selector;
/*  85:    */     try
/*  86:    */     {
/*  87:126 */       selector = this.provider.openSelector();
/*  88:    */     }
/*  89:    */     catch (IOException e)
/*  90:    */     {
/*  91:128 */       throw new ChannelException("failed to open a new selector", e);
/*  92:    */     }
/*  93:131 */     if (DISABLE_KEYSET_OPTIMIZATION) {
/*  94:132 */       return selector;
/*  95:    */     }
/*  96:    */     try
/*  97:    */     {
/*  98:136 */       SelectedSelectionKeySet selectedKeySet = new SelectedSelectionKeySet();
/*  99:    */       
/* 100:138 */       Class<?> selectorImplClass = Class.forName("sun.nio.ch.SelectorImpl", false, PlatformDependent.getSystemClassLoader());
/* 101:142 */       if (!selectorImplClass.isAssignableFrom(selector.getClass())) {
/* 102:143 */         return selector;
/* 103:    */       }
/* 104:146 */       Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
/* 105:147 */       Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");
/* 106:    */       
/* 107:149 */       selectedKeysField.setAccessible(true);
/* 108:150 */       publicSelectedKeysField.setAccessible(true);
/* 109:    */       
/* 110:152 */       selectedKeysField.set(selector, selectedKeySet);
/* 111:153 */       publicSelectedKeysField.set(selector, selectedKeySet);
/* 112:    */       
/* 113:155 */       this.selectedKeys = selectedKeySet;
/* 114:156 */       logger.trace("Instrumented an optimized java.util.Set into: {}", selector);
/* 115:    */     }
/* 116:    */     catch (Throwable t)
/* 117:    */     {
/* 118:158 */       this.selectedKeys = null;
/* 119:159 */       logger.trace("Failed to instrument an optimized java.util.Set into: {}", selector, t);
/* 120:    */     }
/* 121:162 */     return selector;
/* 122:    */   }
/* 123:    */   
/* 124:    */   protected Queue<Runnable> newTaskQueue()
/* 125:    */   {
/* 126:168 */     return PlatformDependent.newMpscQueue();
/* 127:    */   }
/* 128:    */   
/* 129:    */   public void register(SelectableChannel ch, int interestOps, NioTask<?> task)
/* 130:    */   {
/* 131:177 */     if (ch == null) {
/* 132:178 */       throw new NullPointerException("ch");
/* 133:    */     }
/* 134:180 */     if (interestOps == 0) {
/* 135:181 */       throw new IllegalArgumentException("interestOps must be non-zero.");
/* 136:    */     }
/* 137:183 */     if ((interestOps & (ch.validOps() ^ 0xFFFFFFFF)) != 0) {
/* 138:184 */       throw new IllegalArgumentException("invalid interestOps: " + interestOps + "(validOps: " + ch.validOps() + ')');
/* 139:    */     }
/* 140:187 */     if (task == null) {
/* 141:188 */       throw new NullPointerException("task");
/* 142:    */     }
/* 143:191 */     if (isShutdown()) {
/* 144:192 */       throw new IllegalStateException("event loop shut down");
/* 145:    */     }
/* 146:    */     try
/* 147:    */     {
/* 148:196 */       ch.register(this.selector, interestOps, task);
/* 149:    */     }
/* 150:    */     catch (Exception e)
/* 151:    */     {
/* 152:198 */       throw new EventLoopException("failed to register a channel", e);
/* 153:    */     }
/* 154:    */   }
/* 155:    */   
/* 156:    */   public int getIoRatio()
/* 157:    */   {
/* 158:206 */     return this.ioRatio;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public void setIoRatio(int ioRatio)
/* 162:    */   {
/* 163:214 */     if ((ioRatio <= 0) || (ioRatio > 100)) {
/* 164:215 */       throw new IllegalArgumentException("ioRatio: " + ioRatio + " (expected: 0 < ioRatio <= 100)");
/* 165:    */     }
/* 166:217 */     this.ioRatio = ioRatio;
/* 167:    */   }
/* 168:    */   
/* 169:    */   public void rebuildSelector()
/* 170:    */   {
/* 171:225 */     if (!inEventLoop())
/* 172:    */     {
/* 173:226 */       execute(new Runnable()
/* 174:    */       {
/* 175:    */         public void run()
/* 176:    */         {
/* 177:229 */           NioEventLoop.this.rebuildSelector();
/* 178:    */         }
/* 179:231 */       });
/* 180:232 */       return;
/* 181:    */     }
/* 182:235 */     Selector oldSelector = this.selector;
/* 183:238 */     if (oldSelector == null) {
/* 184:    */       return;
/* 185:    */     }
/* 186:    */     Selector newSelector;
/* 187:    */     try
/* 188:    */     {
/* 189:243 */       newSelector = openSelector();
/* 190:    */     }
/* 191:    */     catch (Exception e)
/* 192:    */     {
/* 193:245 */       logger.warn("Failed to create a new Selector.", e);
/* 194:246 */       return;
/* 195:    */     }
/* 196:250 */     int nChannels = 0;
/* 197:    */     for (;;)
/* 198:    */     {
/* 199:    */       try
/* 200:    */       {
/* 201:253 */         Iterator i$ = oldSelector.keys().iterator();
/* 202:253 */         if (i$.hasNext())
/* 203:    */         {
/* 204:253 */           SelectionKey key = (SelectionKey)i$.next();
/* 205:254 */           Object a = key.attachment();
/* 206:    */           try
/* 207:    */           {
/* 208:256 */             if ((key.isValid()) && (key.channel().keyFor(newSelector) != null)) {
/* 209:    */               continue;
/* 210:    */             }
/* 211:260 */             int interestOps = key.interestOps();
/* 212:261 */             key.cancel();
/* 213:262 */             SelectionKey newKey = key.channel().register(newSelector, interestOps, a);
/* 214:263 */             if ((a instanceof AbstractNioChannel)) {
/* 215:265 */               ((AbstractNioChannel)a).selectionKey = newKey;
/* 216:    */             }
/* 217:267 */             nChannels++;
/* 218:    */           }
/* 219:    */           catch (Exception e)
/* 220:    */           {
/* 221:269 */             logger.warn("Failed to re-register a Channel to the new Selector.", e);
/* 222:270 */             if ((a instanceof AbstractNioChannel))
/* 223:    */             {
/* 224:271 */               AbstractNioChannel ch = (AbstractNioChannel)a;
/* 225:272 */               ch.unsafe().close(ch.unsafe().voidPromise());
/* 226:    */             }
/* 227:    */             else
/* 228:    */             {
/* 229:275 */               NioTask<SelectableChannel> task = (NioTask)a;
/* 230:276 */               invokeChannelUnregistered(task, key, e);
/* 231:    */             }
/* 232:    */           }
/* 233:    */         }
/* 234:    */       }
/* 235:    */       catch (ConcurrentModificationException e) {}
/* 236:    */     }
/* 237:288 */     this.selector = newSelector;
/* 238:    */     try
/* 239:    */     {
/* 240:292 */       oldSelector.close();
/* 241:    */     }
/* 242:    */     catch (Throwable t)
/* 243:    */     {
/* 244:294 */       if (logger.isWarnEnabled()) {
/* 245:295 */         logger.warn("Failed to close the old Selector.", t);
/* 246:    */       }
/* 247:    */     }
/* 248:299 */     logger.info("Migrated " + nChannels + " channel(s) to the new Selector.");
/* 249:    */   }
/* 250:    */   
/* 251:    */   protected void run()
/* 252:    */   {
/* 253:    */     for (;;)
/* 254:    */     {
/* 255:305 */       this.oldWakenUp = this.wakenUp.getAndSet(false);
/* 256:    */       try
/* 257:    */       {
/* 258:307 */         if (hasTasks())
/* 259:    */         {
/* 260:308 */           selectNow();
/* 261:    */         }
/* 262:    */         else
/* 263:    */         {
/* 264:310 */           select();
/* 265:340 */           if (this.wakenUp.get()) {
/* 266:341 */             this.selector.wakeup();
/* 267:    */           }
/* 268:    */         }
/* 269:345 */         this.cancelledKeys = 0;
/* 270:346 */         this.needsToSelectAgain = false;
/* 271:347 */         int ioRatio = this.ioRatio;
/* 272:348 */         if (ioRatio == 100)
/* 273:    */         {
/* 274:349 */           processSelectedKeys();
/* 275:350 */           runAllTasks();
/* 276:    */         }
/* 277:    */         else
/* 278:    */         {
/* 279:352 */           long ioStartTime = System.nanoTime();
/* 280:    */           
/* 281:354 */           processSelectedKeys();
/* 282:    */           
/* 283:356 */           long ioTime = System.nanoTime() - ioStartTime;
/* 284:357 */           runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
/* 285:    */         }
/* 286:360 */         if (isShuttingDown())
/* 287:    */         {
/* 288:361 */           closeAll();
/* 289:362 */           if (confirmShutdown()) {
/* 290:    */             break;
/* 291:    */           }
/* 292:    */         }
/* 293:    */       }
/* 294:    */       catch (Throwable t)
/* 295:    */       {
/* 296:367 */         logger.warn("Unexpected exception in the selector loop.", t);
/* 297:    */         try
/* 298:    */         {
/* 299:372 */           Thread.sleep(1000L);
/* 300:    */         }
/* 301:    */         catch (InterruptedException e) {}
/* 302:    */       }
/* 303:    */     }
/* 304:    */   }
/* 305:    */   
/* 306:    */   private void processSelectedKeys()
/* 307:    */   {
/* 308:381 */     if (this.selectedKeys != null) {
/* 309:382 */       processSelectedKeysOptimized(this.selectedKeys.flip());
/* 310:    */     } else {
/* 311:384 */       processSelectedKeysPlain(this.selector.selectedKeys());
/* 312:    */     }
/* 313:    */   }
/* 314:    */   
/* 315:    */   protected void cleanup()
/* 316:    */   {
/* 317:    */     try
/* 318:    */     {
/* 319:391 */       this.selector.close();
/* 320:    */     }
/* 321:    */     catch (IOException e)
/* 322:    */     {
/* 323:393 */       logger.warn("Failed to close a selector.", e);
/* 324:    */     }
/* 325:    */   }
/* 326:    */   
/* 327:    */   void cancel(SelectionKey key)
/* 328:    */   {
/* 329:398 */     key.cancel();
/* 330:399 */     this.cancelledKeys += 1;
/* 331:400 */     if (this.cancelledKeys >= 256)
/* 332:    */     {
/* 333:401 */       this.cancelledKeys = 0;
/* 334:402 */       this.needsToSelectAgain = true;
/* 335:    */     }
/* 336:    */   }
/* 337:    */   
/* 338:    */   protected Runnable pollTask()
/* 339:    */   {
/* 340:408 */     Runnable task = super.pollTask();
/* 341:409 */     if (this.needsToSelectAgain) {
/* 342:410 */       selectAgain();
/* 343:    */     }
/* 344:412 */     return task;
/* 345:    */   }
/* 346:    */   
/* 347:    */   private void processSelectedKeysPlain(Set<SelectionKey> selectedKeys)
/* 348:    */   {
/* 349:419 */     if (selectedKeys.isEmpty()) {
/* 350:420 */       return;
/* 351:    */     }
/* 352:423 */     Iterator<SelectionKey> i = selectedKeys.iterator();
/* 353:    */     for (;;)
/* 354:    */     {
/* 355:425 */       SelectionKey k = (SelectionKey)i.next();
/* 356:426 */       Object a = k.attachment();
/* 357:427 */       i.remove();
/* 358:429 */       if ((a instanceof AbstractNioChannel))
/* 359:    */       {
/* 360:430 */         processSelectedKey(k, (AbstractNioChannel)a);
/* 361:    */       }
/* 362:    */       else
/* 363:    */       {
/* 364:433 */         NioTask<SelectableChannel> task = (NioTask)a;
/* 365:434 */         processSelectedKey(k, task);
/* 366:    */       }
/* 367:437 */       if (!i.hasNext()) {
/* 368:    */         break;
/* 369:    */       }
/* 370:441 */       if (this.needsToSelectAgain)
/* 371:    */       {
/* 372:442 */         selectAgain();
/* 373:443 */         selectedKeys = this.selector.selectedKeys();
/* 374:446 */         if (selectedKeys.isEmpty()) {
/* 375:    */           break;
/* 376:    */         }
/* 377:449 */         i = selectedKeys.iterator();
/* 378:    */       }
/* 379:    */     }
/* 380:    */   }
/* 381:    */   
/* 382:    */   private void processSelectedKeysOptimized(SelectionKey[] selectedKeys)
/* 383:    */   {
/* 384:456 */     for (int i = 0;; i++)
/* 385:    */     {
/* 386:457 */       SelectionKey k = selectedKeys[i];
/* 387:458 */       if (k == null) {
/* 388:    */         break;
/* 389:    */       }
/* 390:463 */       selectedKeys[i] = null;
/* 391:    */       
/* 392:465 */       Object a = k.attachment();
/* 393:467 */       if ((a instanceof AbstractNioChannel))
/* 394:    */       {
/* 395:468 */         processSelectedKey(k, (AbstractNioChannel)a);
/* 396:    */       }
/* 397:    */       else
/* 398:    */       {
/* 399:471 */         NioTask<SelectableChannel> task = (NioTask)a;
/* 400:472 */         processSelectedKey(k, task);
/* 401:    */       }
/* 402:475 */       if (this.needsToSelectAgain)
/* 403:    */       {
/* 404:479 */         while (selectedKeys[i] != null)
/* 405:    */         {
/* 406:482 */           selectedKeys[i] = null;
/* 407:483 */           i++;
/* 408:    */         }
/* 409:486 */         selectAgain();
/* 410:    */         
/* 411:    */ 
/* 412:    */ 
/* 413:    */ 
/* 414:    */ 
/* 415:492 */         selectedKeys = this.selectedKeys.flip();
/* 416:493 */         i = -1;
/* 417:    */       }
/* 418:    */     }
/* 419:    */   }
/* 420:    */   
/* 421:    */   private static void processSelectedKey(SelectionKey k, AbstractNioChannel ch)
/* 422:    */   {
/* 423:499 */     AbstractNioChannel.NioUnsafe unsafe = ch.unsafe();
/* 424:500 */     if (!k.isValid())
/* 425:    */     {
/* 426:502 */       unsafe.close(unsafe.voidPromise());
/* 427:503 */       return;
/* 428:    */     }
/* 429:    */     try
/* 430:    */     {
/* 431:507 */       int readyOps = k.readyOps();
/* 432:510 */       if (((readyOps & 0x11) != 0) || (readyOps == 0))
/* 433:    */       {
/* 434:511 */         unsafe.read();
/* 435:512 */         if (!ch.isOpen()) {
/* 436:514 */           return;
/* 437:    */         }
/* 438:    */       }
/* 439:517 */       if ((readyOps & 0x4) != 0) {
/* 440:519 */         ch.unsafe().forceFlush();
/* 441:    */       }
/* 442:521 */       if ((readyOps & 0x8) != 0)
/* 443:    */       {
/* 444:524 */         int ops = k.interestOps();
/* 445:525 */         ops &= 0xFFFFFFF7;
/* 446:526 */         k.interestOps(ops);
/* 447:    */         
/* 448:528 */         unsafe.finishConnect();
/* 449:    */       }
/* 450:    */     }
/* 451:    */     catch (CancelledKeyException e)
/* 452:    */     {
/* 453:531 */       unsafe.close(unsafe.voidPromise());
/* 454:    */     }
/* 455:    */   }
/* 456:    */   
/* 457:    */   private static void processSelectedKey(SelectionKey k, NioTask<SelectableChannel> task)
/* 458:    */   {
/* 459:536 */     int state = 0;
/* 460:    */     try
/* 461:    */     {
/* 462:538 */       task.channelReady(k.channel(), k);
/* 463:539 */       state = 1;
/* 464:    */     }
/* 465:    */     catch (Exception e)
/* 466:    */     {
/* 467:541 */       k.cancel();
/* 468:542 */       invokeChannelUnregistered(task, k, e);
/* 469:543 */       state = 2;
/* 470:    */     }
/* 471:    */     finally
/* 472:    */     {
/* 473:545 */       switch (state)
/* 474:    */       {
/* 475:    */       case 0: 
/* 476:547 */         k.cancel();
/* 477:548 */         invokeChannelUnregistered(task, k, null);
/* 478:549 */         break;
/* 479:    */       case 1: 
/* 480:551 */         if (!k.isValid()) {
/* 481:552 */           invokeChannelUnregistered(task, k, null);
/* 482:    */         }
/* 483:    */         break;
/* 484:    */       }
/* 485:    */     }
/* 486:    */   }
/* 487:    */   
/* 488:    */   private void closeAll()
/* 489:    */   {
/* 490:560 */     selectAgain();
/* 491:561 */     Set<SelectionKey> keys = this.selector.keys();
/* 492:562 */     Collection<AbstractNioChannel> channels = new ArrayList(keys.size());
/* 493:563 */     for (SelectionKey k : keys)
/* 494:    */     {
/* 495:564 */       Object a = k.attachment();
/* 496:565 */       if ((a instanceof AbstractNioChannel))
/* 497:    */       {
/* 498:566 */         channels.add((AbstractNioChannel)a);
/* 499:    */       }
/* 500:    */       else
/* 501:    */       {
/* 502:568 */         k.cancel();
/* 503:    */         
/* 504:570 */         NioTask<SelectableChannel> task = (NioTask)a;
/* 505:571 */         invokeChannelUnregistered(task, k, null);
/* 506:    */       }
/* 507:    */     }
/* 508:575 */     for (AbstractNioChannel ch : channels) {
/* 509:576 */       ch.unsafe().close(ch.unsafe().voidPromise());
/* 510:    */     }
/* 511:    */   }
/* 512:    */   
/* 513:    */   private static void invokeChannelUnregistered(NioTask<SelectableChannel> task, SelectionKey k, Throwable cause)
/* 514:    */   {
/* 515:    */     try
/* 516:    */     {
/* 517:582 */       task.channelUnregistered(k.channel(), cause);
/* 518:    */     }
/* 519:    */     catch (Exception e)
/* 520:    */     {
/* 521:584 */       logger.warn("Unexpected exception while running NioTask.channelUnregistered()", e);
/* 522:    */     }
/* 523:    */   }
/* 524:    */   
/* 525:    */   protected void wakeup(boolean inEventLoop)
/* 526:    */   {
/* 527:590 */     if ((!inEventLoop) && (this.wakenUp.compareAndSet(false, true))) {
/* 528:591 */       this.selector.wakeup();
/* 529:    */     }
/* 530:    */   }
/* 531:    */   
/* 532:    */   void selectNow()
/* 533:    */     throws IOException
/* 534:    */   {
/* 535:    */     try
/* 536:    */     {
/* 537:597 */       this.selector.selectNow();
/* 538:    */     }
/* 539:    */     finally
/* 540:    */     {
/* 541:600 */       if (this.wakenUp.get()) {
/* 542:601 */         this.selector.wakeup();
/* 543:    */       }
/* 544:    */     }
/* 545:    */   }
/* 546:    */   
/* 547:    */   private void select()
/* 548:    */     throws IOException
/* 549:    */   {
/* 550:607 */     Selector selector = this.selector;
/* 551:    */     try
/* 552:    */     {
/* 553:609 */       int selectCnt = 0;
/* 554:610 */       long currentTimeNanos = System.nanoTime();
/* 555:611 */       long selectDeadLineNanos = currentTimeNanos + delayNanos(currentTimeNanos);
/* 556:    */       for (;;)
/* 557:    */       {
/* 558:613 */         long timeoutMillis = (selectDeadLineNanos - currentTimeNanos + 500000L) / 1000000L;
/* 559:614 */         if (timeoutMillis <= 0L)
/* 560:    */         {
/* 561:615 */           if (selectCnt != 0) {
/* 562:    */             break;
/* 563:    */           }
/* 564:616 */           selector.selectNow();
/* 565:617 */           selectCnt = 1; break;
/* 566:    */         }
/* 567:622 */         int selectedKeys = selector.select(timeoutMillis);
/* 568:623 */         selectCnt++;
/* 569:625 */         if ((selectedKeys != 0) || (this.oldWakenUp) || (this.wakenUp.get()) || (hasTasks())) {
/* 570:    */           break;
/* 571:    */         }
/* 572:631 */         if (Thread.interrupted())
/* 573:    */         {
/* 574:637 */           if (logger.isDebugEnabled()) {
/* 575:638 */             logger.debug("Selector.select() returned prematurely because Thread.currentThread().interrupt() was called. Use NioEventLoop.shutdownGracefully() to shutdown the NioEventLoop.");
/* 576:    */           }
/* 577:642 */           selectCnt = 1;
/* 578:643 */           break;
/* 579:    */         }
/* 580:645 */         if ((SELECTOR_AUTO_REBUILD_THRESHOLD > 0) && (selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD))
/* 581:    */         {
/* 582:649 */           logger.warn("Selector.select() returned prematurely {} times in a row; rebuilding selector.", Integer.valueOf(selectCnt));
/* 583:    */           
/* 584:    */ 
/* 585:    */ 
/* 586:653 */           rebuildSelector();
/* 587:654 */           selector = this.selector;
/* 588:    */           
/* 589:    */ 
/* 590:657 */           selector.selectNow();
/* 591:658 */           selectCnt = 1;
/* 592:659 */           break;
/* 593:    */         }
/* 594:662 */         currentTimeNanos = System.nanoTime();
/* 595:    */       }
/* 596:665 */       if ((selectCnt > 3) && 
/* 597:666 */         (logger.isDebugEnabled())) {
/* 598:667 */         logger.debug("Selector.select() returned prematurely {} times in a row.", Integer.valueOf(selectCnt - 1));
/* 599:    */       }
/* 600:    */     }
/* 601:    */     catch (CancelledKeyException e)
/* 602:    */     {
/* 603:671 */       if (logger.isDebugEnabled()) {
/* 604:672 */         logger.debug(CancelledKeyException.class.getSimpleName() + " raised by a Selector - JDK bug?", e);
/* 605:    */       }
/* 606:    */     }
/* 607:    */   }
/* 608:    */   
/* 609:    */   private void selectAgain()
/* 610:    */   {
/* 611:679 */     this.needsToSelectAgain = false;
/* 612:    */     try
/* 613:    */     {
/* 614:681 */       this.selector.selectNow();
/* 615:    */     }
/* 616:    */     catch (Throwable t)
/* 617:    */     {
/* 618:683 */       logger.warn("Failed to update SelectionKeys.", t);
/* 619:    */     }
/* 620:    */   }
/* 621:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.nio.NioEventLoop
 * JD-Core Version:    0.7.0.1
 */