/*    1:     */ package io.netty.util.internal.chmv8;
/*    2:     */ 
/*    3:     */ import java.io.IOException;
/*    4:     */ import java.io.ObjectInputStream;
/*    5:     */ import java.io.ObjectOutputStream;
/*    6:     */ import java.io.Serializable;
/*    7:     */ import java.lang.ref.ReferenceQueue;
/*    8:     */ import java.lang.ref.WeakReference;
/*    9:     */ import java.lang.reflect.Field;
/*   10:     */ import java.security.AccessController;
/*   11:     */ import java.security.PrivilegedActionException;
/*   12:     */ import java.security.PrivilegedExceptionAction;
/*   13:     */ import java.util.Collection;
/*   14:     */ import java.util.List;
/*   15:     */ import java.util.RandomAccess;
/*   16:     */ import java.util.concurrent.Callable;
/*   17:     */ import java.util.concurrent.CancellationException;
/*   18:     */ import java.util.concurrent.ExecutionException;
/*   19:     */ import java.util.concurrent.Future;
/*   20:     */ import java.util.concurrent.RunnableFuture;
/*   21:     */ import java.util.concurrent.TimeUnit;
/*   22:     */ import java.util.concurrent.TimeoutException;
/*   23:     */ import java.util.concurrent.locks.ReentrantLock;
/*   24:     */ import sun.misc.Unsafe;
/*   25:     */ 
/*   26:     */ public abstract class ForkJoinTask<V>
/*   27:     */   implements Future<V>, Serializable
/*   28:     */ {
/*   29:     */   volatile int status;
/*   30:     */   static final int DONE_MASK = -268435456;
/*   31:     */   static final int NORMAL = -268435456;
/*   32:     */   static final int CANCELLED = -1073741824;
/*   33:     */   static final int EXCEPTIONAL = -2147483648;
/*   34:     */   static final int SIGNAL = 65536;
/*   35:     */   static final int SMASK = 65535;
/*   36:     */   private static final ExceptionNode[] exceptionTable;
/*   37:     */   
/*   38:     */   private int setCompletion(int completion)
/*   39:     */   {
/*   40:     */     int s;
/*   41:     */     do
/*   42:     */     {
/*   43: 259 */       if ((s = this.status) < 0) {
/*   44: 260 */         return s;
/*   45:     */       }
/*   46: 261 */     } while (!U.compareAndSwapInt(this, STATUS, s, s | completion));
/*   47: 262 */     if (s >>> 16 != 0) {
/*   48: 263 */       synchronized (this)
/*   49:     */       {
/*   50: 263 */         notifyAll();
/*   51:     */       }
/*   52:     */     }
/*   53: 264 */     return completion;
/*   54:     */   }
/*   55:     */   
/*   56:     */   final int doExec()
/*   57:     */   {
/*   58:     */     int s;
/*   59: 278 */     if ((s = this.status) >= 0)
/*   60:     */     {
/*   61:     */       boolean completed;
/*   62:     */       try
/*   63:     */       {
/*   64: 280 */         completed = exec();
/*   65:     */       }
/*   66:     */       catch (Throwable rex)
/*   67:     */       {
/*   68: 282 */         return setExceptionalCompletion(rex);
/*   69:     */       }
/*   70: 284 */       if (completed) {
/*   71: 285 */         s = setCompletion(-268435456);
/*   72:     */       }
/*   73:     */     }
/*   74: 287 */     return s;
/*   75:     */   }
/*   76:     */   
/*   77:     */   final boolean trySetSignal()
/*   78:     */   {
/*   79: 298 */     int s = this.status;
/*   80: 299 */     return (s >= 0) && (U.compareAndSwapInt(this, STATUS, s, s | 0x10000));
/*   81:     */   }
/*   82:     */   
/*   83:     */   private int externalAwaitDone()
/*   84:     */   {
/*   85: 308 */     ForkJoinPool cp = ForkJoinPool.common;
/*   86:     */     int s;
/*   87: 309 */     if ((s = this.status) >= 0)
/*   88:     */     {
/*   89: 310 */       if (cp != null) {
/*   90: 311 */         if ((this instanceof CountedCompleter)) {
/*   91: 312 */           s = cp.externalHelpComplete((CountedCompleter)this);
/*   92: 313 */         } else if (cp.tryExternalUnpush(this)) {
/*   93: 314 */           s = doExec();
/*   94:     */         }
/*   95:     */       }
/*   96: 316 */       if ((s >= 0) && ((s = this.status) >= 0))
/*   97:     */       {
/*   98: 317 */         boolean interrupted = false;
/*   99:     */         do
/*  100:     */         {
/*  101: 319 */           if (U.compareAndSwapInt(this, STATUS, s, s | 0x10000)) {
/*  102: 320 */             synchronized (this)
/*  103:     */             {
/*  104: 321 */               if (this.status >= 0) {
/*  105:     */                 try
/*  106:     */                 {
/*  107: 323 */                   wait();
/*  108:     */                 }
/*  109:     */                 catch (InterruptedException ie)
/*  110:     */                 {
/*  111: 325 */                   interrupted = true;
/*  112:     */                 }
/*  113:     */               } else {
/*  114: 329 */                 notifyAll();
/*  115:     */               }
/*  116:     */             }
/*  117:     */           }
/*  118: 332 */         } while ((s = this.status) >= 0);
/*  119: 333 */         if (interrupted) {
/*  120: 334 */           Thread.currentThread().interrupt();
/*  121:     */         }
/*  122:     */       }
/*  123:     */     }
/*  124: 337 */     return s;
/*  125:     */   }
/*  126:     */   
/*  127:     */   private int externalInterruptibleAwaitDone()
/*  128:     */     throws InterruptedException
/*  129:     */   {
/*  130: 345 */     ForkJoinPool cp = ForkJoinPool.common;
/*  131: 346 */     if (Thread.interrupted()) {
/*  132: 347 */       throw new InterruptedException();
/*  133:     */     }
/*  134:     */     int s;
/*  135: 348 */     if (((s = this.status) >= 0) && (cp != null)) {
/*  136: 349 */       if ((this instanceof CountedCompleter)) {
/*  137: 350 */         cp.externalHelpComplete((CountedCompleter)this);
/*  138: 351 */       } else if (cp.tryExternalUnpush(this)) {
/*  139: 352 */         doExec();
/*  140:     */       }
/*  141:     */     }
/*  142: 354 */     while ((s = this.status) >= 0) {
/*  143: 355 */       if (U.compareAndSwapInt(this, STATUS, s, s | 0x10000)) {
/*  144: 356 */         synchronized (this)
/*  145:     */         {
/*  146: 357 */           if (this.status >= 0) {
/*  147: 358 */             wait();
/*  148:     */           } else {
/*  149: 360 */             notifyAll();
/*  150:     */           }
/*  151:     */         }
/*  152:     */       }
/*  153:     */     }
/*  154: 364 */     return s;
/*  155:     */   }
/*  156:     */   
/*  157:     */   private int doJoin()
/*  158:     */   {
/*  159:     */     int s;
/*  160:     */     Thread t;
/*  161:     */     ForkJoinWorkerThread wt;
/*  162:     */     ForkJoinPool.WorkQueue w;
/*  163: 377 */     return ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ? wt.pool.awaitJoin(w, this) : ((w = (wt = (ForkJoinWorkerThread)t).workQueue).tryUnpush(this)) && ((s = doExec()) < 0) ? s : (s = this.status) < 0 ? s : externalAwaitDone();
/*  164:     */   }
/*  165:     */   
/*  166:     */   private int doInvoke()
/*  167:     */   {
/*  168:     */     int s;
/*  169:     */     Thread t;
/*  170:     */     ForkJoinWorkerThread wt;
/*  171: 392 */     return ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ? (wt = (ForkJoinWorkerThread)t).pool.awaitJoin(wt.workQueue, this) : (s = doExec()) < 0 ? s : externalAwaitDone();
/*  172:     */   }
/*  173:     */   
/*  174:     */   static final class ExceptionNode
/*  175:     */     extends WeakReference<ForkJoinTask<?>>
/*  176:     */   {
/*  177:     */     final Throwable ex;
/*  178:     */     ExceptionNode next;
/*  179:     */     final long thrower;
/*  180:     */     
/*  181:     */     ExceptionNode(ForkJoinTask<?> task, Throwable ex, ExceptionNode next)
/*  182:     */     {
/*  183: 435 */       super(ForkJoinTask.exceptionTableRefQueue);
/*  184: 436 */       this.ex = ex;
/*  185: 437 */       this.next = next;
/*  186: 438 */       this.thrower = Thread.currentThread().getId();
/*  187:     */     }
/*  188:     */   }
/*  189:     */   
/*  190:     */   final int recordExceptionalCompletion(Throwable ex)
/*  191:     */   {
/*  192:     */     int s;
/*  193: 449 */     if ((s = this.status) >= 0)
/*  194:     */     {
/*  195: 450 */       int h = System.identityHashCode(this);
/*  196: 451 */       ReentrantLock lock = exceptionTableLock;
/*  197: 452 */       lock.lock();
/*  198:     */       try
/*  199:     */       {
/*  200: 454 */         expungeStaleExceptions();
/*  201: 455 */         ExceptionNode[] t = exceptionTable;
/*  202: 456 */         int i = h & t.length - 1;
/*  203: 457 */         for (ExceptionNode e = t[i];; e = e.next) {
/*  204: 458 */           if (e == null) {
/*  205: 459 */             t[i] = new ExceptionNode(this, ex, t[i]);
/*  206:     */           } else {
/*  207: 462 */             if (e.get() == this) {
/*  208:     */               break;
/*  209:     */             }
/*  210:     */           }
/*  211:     */         }
/*  212:     */       }
/*  213:     */       finally
/*  214:     */       {
/*  215: 466 */         lock.unlock();
/*  216:     */       }
/*  217: 468 */       s = setCompletion(-2147483648);
/*  218:     */     }
/*  219: 470 */     return s;
/*  220:     */   }
/*  221:     */   
/*  222:     */   private int setExceptionalCompletion(Throwable ex)
/*  223:     */   {
/*  224: 479 */     int s = recordExceptionalCompletion(ex);
/*  225: 480 */     if ((s & 0xF0000000) == -2147483648) {
/*  226: 481 */       internalPropagateException(ex);
/*  227:     */     }
/*  228: 482 */     return s;
/*  229:     */   }
/*  230:     */   
/*  231:     */   static final void cancelIgnoringExceptions(ForkJoinTask<?> t)
/*  232:     */   {
/*  233: 498 */     if ((t != null) && (t.status >= 0)) {
/*  234:     */       try
/*  235:     */       {
/*  236: 500 */         t.cancel(false);
/*  237:     */       }
/*  238:     */       catch (Throwable ignore) {}
/*  239:     */     }
/*  240:     */   }
/*  241:     */   
/*  242:     */   private void clearExceptionalCompletion()
/*  243:     */   {
/*  244: 510 */     int h = System.identityHashCode(this);
/*  245: 511 */     ReentrantLock lock = exceptionTableLock;
/*  246: 512 */     lock.lock();
/*  247:     */     try
/*  248:     */     {
/*  249: 514 */       ExceptionNode[] t = exceptionTable;
/*  250: 515 */       int i = h & t.length - 1;
/*  251: 516 */       ExceptionNode e = t[i];
/*  252: 517 */       ExceptionNode pred = null;
/*  253: 518 */       while (e != null)
/*  254:     */       {
/*  255: 519 */         ExceptionNode next = e.next;
/*  256: 520 */         if (e.get() == this)
/*  257:     */         {
/*  258: 521 */           if (pred == null)
/*  259:     */           {
/*  260: 522 */             t[i] = next; break;
/*  261:     */           }
/*  262: 524 */           pred.next = next;
/*  263: 525 */           break;
/*  264:     */         }
/*  265: 527 */         pred = e;
/*  266: 528 */         e = next;
/*  267:     */       }
/*  268: 530 */       expungeStaleExceptions();
/*  269: 531 */       this.status = 0;
/*  270:     */     }
/*  271:     */     finally
/*  272:     */     {
/*  273: 533 */       lock.unlock();
/*  274:     */     }
/*  275:     */   }
/*  276:     */   
/*  277:     */   private Throwable getThrowableException()
/*  278:     */   {
/*  279: 552 */     if ((this.status & 0xF0000000) != -2147483648) {
/*  280: 553 */       return null;
/*  281:     */     }
/*  282: 554 */     int h = System.identityHashCode(this);
/*  283:     */     
/*  284: 556 */     ReentrantLock lock = exceptionTableLock;
/*  285: 557 */     lock.lock();
/*  286:     */     ExceptionNode e;
/*  287:     */     try
/*  288:     */     {
/*  289: 559 */       expungeStaleExceptions();
/*  290: 560 */       ExceptionNode[] t = exceptionTable;
/*  291: 561 */       e = t[(h & t.length - 1)];
/*  292: 562 */       while ((e != null) && (e.get() != this)) {
/*  293: 563 */         e = e.next;
/*  294:     */       }
/*  295:     */     }
/*  296:     */     finally
/*  297:     */     {
/*  298: 565 */       lock.unlock();
/*  299:     */     }
/*  300:     */     Throwable ex;
/*  301: 568 */     if ((e == null) || ((ex = e.ex) == null)) {
/*  302: 569 */       return null;
/*  303:     */     }
/*  304:     */     Throwable ex;
/*  305: 591 */     return ex;
/*  306:     */   }
/*  307:     */   
/*  308:     */   private static void expungeStaleExceptions()
/*  309:     */   {
/*  310:     */     Object x;
/*  311: 598 */     while ((x = exceptionTableRefQueue.poll()) != null) {
/*  312: 599 */       if ((x instanceof ExceptionNode))
/*  313:     */       {
/*  314: 600 */         ForkJoinTask<?> key = (ForkJoinTask)((ExceptionNode)x).get();
/*  315: 601 */         ExceptionNode[] t = exceptionTable;
/*  316: 602 */         int i = System.identityHashCode(key) & t.length - 1;
/*  317: 603 */         ExceptionNode e = t[i];
/*  318: 604 */         ExceptionNode pred = null;
/*  319: 605 */         while (e != null)
/*  320:     */         {
/*  321: 606 */           ExceptionNode next = e.next;
/*  322: 607 */           if (e == x)
/*  323:     */           {
/*  324: 608 */             if (pred == null)
/*  325:     */             {
/*  326: 609 */               t[i] = next; break;
/*  327:     */             }
/*  328: 611 */             pred.next = next;
/*  329: 612 */             break;
/*  330:     */           }
/*  331: 614 */           pred = e;
/*  332: 615 */           e = next;
/*  333:     */         }
/*  334:     */       }
/*  335:     */     }
/*  336:     */   }
/*  337:     */   
/*  338:     */   static final void helpExpungeStaleExceptions()
/*  339:     */   {
/*  340: 626 */     ReentrantLock lock = exceptionTableLock;
/*  341: 627 */     if (lock.tryLock()) {
/*  342:     */       try
/*  343:     */       {
/*  344: 629 */         expungeStaleExceptions();
/*  345:     */       }
/*  346:     */       finally
/*  347:     */       {
/*  348: 631 */         lock.unlock();
/*  349:     */       }
/*  350:     */     }
/*  351:     */   }
/*  352:     */   
/*  353:     */   static void rethrow(Throwable ex)
/*  354:     */   {
/*  355: 640 */     if (ex != null) {
/*  356: 641 */       uncheckedThrow(ex);
/*  357:     */     }
/*  358:     */   }
/*  359:     */   
/*  360:     */   static <T extends Throwable> void uncheckedThrow(Throwable t)
/*  361:     */     throws Throwable
/*  362:     */   {
/*  363: 651 */     throw t;
/*  364:     */   }
/*  365:     */   
/*  366:     */   private void reportException(int s)
/*  367:     */   {
/*  368: 658 */     if (s == -1073741824) {
/*  369: 659 */       throw new CancellationException();
/*  370:     */     }
/*  371: 660 */     if (s == -2147483648) {
/*  372: 661 */       rethrow(getThrowableException());
/*  373:     */     }
/*  374:     */   }
/*  375:     */   
/*  376:     */   public final ForkJoinTask<V> fork()
/*  377:     */   {
/*  378:     */     Thread t;
/*  379: 683 */     if (((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)) {
/*  380: 684 */       ((ForkJoinWorkerThread)t).workQueue.push(this);
/*  381:     */     } else {
/*  382: 686 */       ForkJoinPool.common.externalPush(this);
/*  383:     */     }
/*  384: 687 */     return this;
/*  385:     */   }
/*  386:     */   
/*  387:     */   public final V join()
/*  388:     */   {
/*  389:     */     int s;
/*  390: 703 */     if ((s = doJoin() & 0xF0000000) != -268435456) {
/*  391: 704 */       reportException(s);
/*  392:     */     }
/*  393: 705 */     return getRawResult();
/*  394:     */   }
/*  395:     */   
/*  396:     */   public final V invoke()
/*  397:     */   {
/*  398:     */     int s;
/*  399: 718 */     if ((s = doInvoke() & 0xF0000000) != -268435456) {
/*  400: 719 */       reportException(s);
/*  401:     */     }
/*  402: 720 */     return getRawResult();
/*  403:     */   }
/*  404:     */   
/*  405:     */   public static void invokeAll(ForkJoinTask<?> t1, ForkJoinTask<?> t2)
/*  406:     */   {
/*  407: 742 */     t2.fork();
/*  408:     */     int s1;
/*  409: 743 */     if ((s1 = t1.doInvoke() & 0xF0000000) != -268435456) {
/*  410: 744 */       t1.reportException(s1);
/*  411:     */     }
/*  412:     */     int s2;
/*  413: 745 */     if ((s2 = t2.doJoin() & 0xF0000000) != -268435456) {
/*  414: 746 */       t2.reportException(s2);
/*  415:     */     }
/*  416:     */   }
/*  417:     */   
/*  418:     */   public static void invokeAll(ForkJoinTask<?>... tasks)
/*  419:     */   {
/*  420: 765 */     Throwable ex = null;
/*  421: 766 */     int last = tasks.length - 1;
/*  422: 767 */     for (int i = last; i >= 0; i--)
/*  423:     */     {
/*  424: 768 */       ForkJoinTask<?> t = tasks[i];
/*  425: 769 */       if (t == null)
/*  426:     */       {
/*  427: 770 */         if (ex == null) {
/*  428: 771 */           ex = new NullPointerException();
/*  429:     */         }
/*  430:     */       }
/*  431: 773 */       else if (i != 0) {
/*  432: 774 */         t.fork();
/*  433: 775 */       } else if ((t.doInvoke() < -268435456) && (ex == null)) {
/*  434: 776 */         ex = t.getException();
/*  435:     */       }
/*  436:     */     }
/*  437: 778 */     for (int i = 1; i <= last; i++)
/*  438:     */     {
/*  439: 779 */       ForkJoinTask<?> t = tasks[i];
/*  440: 780 */       if (t != null) {
/*  441: 781 */         if (ex != null) {
/*  442: 782 */           t.cancel(false);
/*  443: 783 */         } else if (t.doJoin() < -268435456) {
/*  444: 784 */           ex = t.getException();
/*  445:     */         }
/*  446:     */       }
/*  447:     */     }
/*  448: 787 */     if (ex != null) {
/*  449: 788 */       rethrow(ex);
/*  450:     */     }
/*  451:     */   }
/*  452:     */   
/*  453:     */   public static <T extends ForkJoinTask<?>> Collection<T> invokeAll(Collection<T> tasks)
/*  454:     */   {
/*  455: 809 */     if ((!(tasks instanceof RandomAccess)) || (!(tasks instanceof List)))
/*  456:     */     {
/*  457: 810 */       invokeAll((ForkJoinTask[])tasks.toArray(new ForkJoinTask[tasks.size()]));
/*  458: 811 */       return tasks;
/*  459:     */     }
/*  460: 814 */     List<? extends ForkJoinTask<?>> ts = (List)tasks;
/*  461:     */     
/*  462: 816 */     Throwable ex = null;
/*  463: 817 */     int last = ts.size() - 1;
/*  464: 818 */     for (int i = last; i >= 0; i--)
/*  465:     */     {
/*  466: 819 */       ForkJoinTask<?> t = (ForkJoinTask)ts.get(i);
/*  467: 820 */       if (t == null)
/*  468:     */       {
/*  469: 821 */         if (ex == null) {
/*  470: 822 */           ex = new NullPointerException();
/*  471:     */         }
/*  472:     */       }
/*  473: 824 */       else if (i != 0) {
/*  474: 825 */         t.fork();
/*  475: 826 */       } else if ((t.doInvoke() < -268435456) && (ex == null)) {
/*  476: 827 */         ex = t.getException();
/*  477:     */       }
/*  478:     */     }
/*  479: 829 */     for (int i = 1; i <= last; i++)
/*  480:     */     {
/*  481: 830 */       ForkJoinTask<?> t = (ForkJoinTask)ts.get(i);
/*  482: 831 */       if (t != null) {
/*  483: 832 */         if (ex != null) {
/*  484: 833 */           t.cancel(false);
/*  485: 834 */         } else if (t.doJoin() < -268435456) {
/*  486: 835 */           ex = t.getException();
/*  487:     */         }
/*  488:     */       }
/*  489:     */     }
/*  490: 838 */     if (ex != null) {
/*  491: 839 */       rethrow(ex);
/*  492:     */     }
/*  493: 840 */     return tasks;
/*  494:     */   }
/*  495:     */   
/*  496:     */   public boolean cancel(boolean mayInterruptIfRunning)
/*  497:     */   {
/*  498: 871 */     return (setCompletion(-1073741824) & 0xF0000000) == -1073741824;
/*  499:     */   }
/*  500:     */   
/*  501:     */   public final boolean isDone()
/*  502:     */   {
/*  503: 875 */     return this.status < 0;
/*  504:     */   }
/*  505:     */   
/*  506:     */   public final boolean isCancelled()
/*  507:     */   {
/*  508: 879 */     return (this.status & 0xF0000000) == -1073741824;
/*  509:     */   }
/*  510:     */   
/*  511:     */   public final boolean isCompletedAbnormally()
/*  512:     */   {
/*  513: 888 */     return this.status < -268435456;
/*  514:     */   }
/*  515:     */   
/*  516:     */   public final boolean isCompletedNormally()
/*  517:     */   {
/*  518: 899 */     return (this.status & 0xF0000000) == -268435456;
/*  519:     */   }
/*  520:     */   
/*  521:     */   public final Throwable getException()
/*  522:     */   {
/*  523: 910 */     int s = this.status & 0xF0000000;
/*  524: 911 */     return s == -1073741824 ? new CancellationException() : s >= -268435456 ? null : getThrowableException();
/*  525:     */   }
/*  526:     */   
/*  527:     */   public void completeExceptionally(Throwable ex)
/*  528:     */   {
/*  529: 931 */     setExceptionalCompletion(((ex instanceof RuntimeException)) || ((ex instanceof Error)) ? ex : new RuntimeException(ex));
/*  530:     */   }
/*  531:     */   
/*  532:     */   public void complete(V value)
/*  533:     */   {
/*  534:     */     try
/*  535:     */     {
/*  536: 951 */       setRawResult(value);
/*  537:     */     }
/*  538:     */     catch (Throwable rex)
/*  539:     */     {
/*  540: 953 */       setExceptionalCompletion(rex);
/*  541: 954 */       return;
/*  542:     */     }
/*  543: 956 */     setCompletion(-268435456);
/*  544:     */   }
/*  545:     */   
/*  546:     */   public final void quietlyComplete()
/*  547:     */   {
/*  548: 968 */     setCompletion(-268435456);
/*  549:     */   }
/*  550:     */   
/*  551:     */   public final V get()
/*  552:     */     throws InterruptedException, ExecutionException
/*  553:     */   {
/*  554: 983 */     int s = (Thread.currentThread() instanceof ForkJoinWorkerThread) ? doJoin() : externalInterruptibleAwaitDone();
/*  555: 986 */     if ((s &= 0xF0000000) == -1073741824) {
/*  556: 987 */       throw new CancellationException();
/*  557:     */     }
/*  558:     */     Throwable ex;
/*  559: 988 */     if ((s == -2147483648) && ((ex = getThrowableException()) != null)) {
/*  560: 989 */       throw new ExecutionException(ex);
/*  561:     */     }
/*  562: 990 */     return getRawResult();
/*  563:     */   }
/*  564:     */   
/*  565:     */   public final V get(long timeout, TimeUnit unit)
/*  566:     */     throws InterruptedException, ExecutionException, TimeoutException
/*  567:     */   {
/*  568:1009 */     if (Thread.interrupted()) {
/*  569:1010 */       throw new InterruptedException();
/*  570:     */     }
/*  571:1013 */     long ns = unit.toNanos(timeout);
/*  572:     */     int s;
/*  573:1015 */     if (((s = this.status) >= 0) && (ns > 0L))
/*  574:     */     {
/*  575:1016 */       long deadline = System.nanoTime() + ns;
/*  576:1017 */       ForkJoinPool p = null;
/*  577:1018 */       ForkJoinPool.WorkQueue w = null;
/*  578:1019 */       Thread t = Thread.currentThread();
/*  579:1020 */       if ((t instanceof ForkJoinWorkerThread))
/*  580:     */       {
/*  581:1021 */         ForkJoinWorkerThread wt = (ForkJoinWorkerThread)t;
/*  582:1022 */         p = wt.pool;
/*  583:1023 */         w = wt.workQueue;
/*  584:1024 */         p.helpJoinOnce(w, this);
/*  585:     */       }
/*  586:     */       else
/*  587:     */       {
/*  588:     */         ForkJoinPool cp;
/*  589:1026 */         if ((cp = ForkJoinPool.common) != null) {
/*  590:1027 */           if ((this instanceof CountedCompleter)) {
/*  591:1028 */             cp.externalHelpComplete((CountedCompleter)this);
/*  592:1029 */           } else if (cp.tryExternalUnpush(this)) {
/*  593:1030 */             doExec();
/*  594:     */           }
/*  595:     */         }
/*  596:     */       }
/*  597:1032 */       boolean canBlock = false;
/*  598:1033 */       boolean interrupted = false;
/*  599:     */       try
/*  600:     */       {
/*  601:1035 */         while ((s = this.status) >= 0) {
/*  602:1036 */           if ((w != null) && (w.qlock < 0))
/*  603:     */           {
/*  604:1037 */             cancelIgnoringExceptions(this);
/*  605:     */           }
/*  606:1038 */           else if (!canBlock)
/*  607:     */           {
/*  608:1039 */             if ((p == null) || (p.tryCompensate(p.ctl))) {
/*  609:1040 */               canBlock = true;
/*  610:     */             }
/*  611:     */           }
/*  612:     */           else
/*  613:     */           {
/*  614:     */             long ms;
/*  615:1043 */             if (((ms = TimeUnit.NANOSECONDS.toMillis(ns)) > 0L) && (U.compareAndSwapInt(this, STATUS, s, s | 0x10000))) {
/*  616:1045 */               synchronized (this)
/*  617:     */               {
/*  618:1046 */                 if (this.status >= 0) {
/*  619:     */                   try
/*  620:     */                   {
/*  621:1048 */                     wait(ms);
/*  622:     */                   }
/*  623:     */                   catch (InterruptedException ie)
/*  624:     */                   {
/*  625:1050 */                     if (p == null) {
/*  626:1051 */                       interrupted = true;
/*  627:     */                     }
/*  628:     */                   }
/*  629:     */                 } else {
/*  630:1055 */                   notifyAll();
/*  631:     */                 }
/*  632:     */               }
/*  633:     */             }
/*  634:1058 */             if (((s = this.status) >= 0) && (!interrupted)) {
/*  635:1058 */               if ((ns = deadline - System.nanoTime()) <= 0L) {
/*  636:     */                 break;
/*  637:     */               }
/*  638:     */             }
/*  639:     */           }
/*  640:     */         }
/*  641:     */       }
/*  642:     */       finally
/*  643:     */       {
/*  644:1064 */         if ((p != null) && (canBlock)) {
/*  645:1065 */           p.incrementActiveCount();
/*  646:     */         }
/*  647:     */       }
/*  648:1067 */       if (interrupted) {
/*  649:1068 */         throw new InterruptedException();
/*  650:     */       }
/*  651:     */     }
/*  652:1070 */     if ((s &= 0xF0000000) != -268435456)
/*  653:     */     {
/*  654:1072 */       if (s == -1073741824) {
/*  655:1073 */         throw new CancellationException();
/*  656:     */       }
/*  657:1074 */       if (s != -2147483648) {
/*  658:1075 */         throw new TimeoutException();
/*  659:     */       }
/*  660:     */       Throwable ex;
/*  661:1076 */       if ((ex = getThrowableException()) != null) {
/*  662:1077 */         throw new ExecutionException(ex);
/*  663:     */       }
/*  664:     */     }
/*  665:1079 */     return getRawResult();
/*  666:     */   }
/*  667:     */   
/*  668:     */   public final void quietlyJoin()
/*  669:     */   {
/*  670:1089 */     doJoin();
/*  671:     */   }
/*  672:     */   
/*  673:     */   public final void quietlyInvoke()
/*  674:     */   {
/*  675:1098 */     doInvoke();
/*  676:     */   }
/*  677:     */   
/*  678:     */   public static void helpQuiesce()
/*  679:     */   {
/*  680:     */     Thread t;
/*  681:1110 */     if (((t = Thread.currentThread()) instanceof ForkJoinWorkerThread))
/*  682:     */     {
/*  683:1111 */       ForkJoinWorkerThread wt = (ForkJoinWorkerThread)t;
/*  684:1112 */       wt.pool.helpQuiescePool(wt.workQueue);
/*  685:     */     }
/*  686:     */     else
/*  687:     */     {
/*  688:1115 */       ForkJoinPool.quiesceCommonPool();
/*  689:     */     }
/*  690:     */   }
/*  691:     */   
/*  692:     */   public void reinitialize()
/*  693:     */   {
/*  694:1135 */     if ((this.status & 0xF0000000) == -2147483648) {
/*  695:1136 */       clearExceptionalCompletion();
/*  696:     */     } else {
/*  697:1138 */       this.status = 0;
/*  698:     */     }
/*  699:     */   }
/*  700:     */   
/*  701:     */   public static ForkJoinPool getPool()
/*  702:     */   {
/*  703:1149 */     Thread t = Thread.currentThread();
/*  704:1150 */     return (t instanceof ForkJoinWorkerThread) ? ((ForkJoinWorkerThread)t).pool : null;
/*  705:     */   }
/*  706:     */   
/*  707:     */   public static boolean inForkJoinPool()
/*  708:     */   {
/*  709:1163 */     return Thread.currentThread() instanceof ForkJoinWorkerThread;
/*  710:     */   }
/*  711:     */   
/*  712:     */   public boolean tryUnfork()
/*  713:     */   {
/*  714:     */     Thread t;
/*  715:1178 */     return ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ? ((ForkJoinWorkerThread)t).workQueue.tryUnpush(this) : ForkJoinPool.common.tryExternalUnpush(this);
/*  716:     */   }
/*  717:     */   
/*  718:     */   public static int getQueuedTaskCount()
/*  719:     */   {
/*  720:     */     Thread t;
/*  721:     */     ForkJoinPool.WorkQueue q;
/*  722:     */     ForkJoinPool.WorkQueue q;
/*  723:1193 */     if (((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)) {
/*  724:1194 */       q = ((ForkJoinWorkerThread)t).workQueue;
/*  725:     */     } else {
/*  726:1196 */       q = ForkJoinPool.commonSubmitterQueue();
/*  727:     */     }
/*  728:1197 */     return q == null ? 0 : q.queueSize();
/*  729:     */   }
/*  730:     */   
/*  731:     */   public static int getSurplusQueuedTaskCount()
/*  732:     */   {
/*  733:1214 */     return ForkJoinPool.getSurplusQueuedTaskCount();
/*  734:     */   }
/*  735:     */   
/*  736:     */   protected static ForkJoinTask<?> peekNextLocalTask()
/*  737:     */   {
/*  738:     */     Thread t;
/*  739:     */     ForkJoinPool.WorkQueue q;
/*  740:     */     ForkJoinPool.WorkQueue q;
/*  741:1269 */     if (((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)) {
/*  742:1270 */       q = ((ForkJoinWorkerThread)t).workQueue;
/*  743:     */     } else {
/*  744:1272 */       q = ForkJoinPool.commonSubmitterQueue();
/*  745:     */     }
/*  746:1273 */     return q == null ? null : q.peek();
/*  747:     */   }
/*  748:     */   
/*  749:     */   protected static ForkJoinTask<?> pollNextLocalTask()
/*  750:     */   {
/*  751:     */     Thread t;
/*  752:1287 */     return ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ? ((ForkJoinWorkerThread)t).workQueue.nextLocalTask() : null;
/*  753:     */   }
/*  754:     */   
/*  755:     */   protected static ForkJoinTask<?> pollTask()
/*  756:     */   {
/*  757:     */     Thread t;
/*  758:     */     ForkJoinWorkerThread wt;
/*  759:1307 */     return ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ? (wt = (ForkJoinWorkerThread)t).pool.nextTaskFor(wt.workQueue) : null;
/*  760:     */   }
/*  761:     */   
/*  762:     */   public final short getForkJoinTaskTag()
/*  763:     */   {
/*  764:1321 */     return (short)this.status;
/*  765:     */   }
/*  766:     */   
/*  767:     */   public final short setForkJoinTaskTag(short tag)
/*  768:     */   {
/*  769:     */     int s;
/*  770:1333 */     while (!U.compareAndSwapInt(this, STATUS, s = this.status, s & 0xFFFF0000 | tag & 0xFFFF)) {}
/*  771:1335 */     return (short)s;
/*  772:     */   }
/*  773:     */   
/*  774:     */   public final boolean compareAndSetForkJoinTaskTag(short e, short tag)
/*  775:     */   {
/*  776:     */     int s;
/*  777:     */     do
/*  778:     */     {
/*  779:1355 */       if ((short)(s = this.status) != e) {
/*  780:1356 */         return false;
/*  781:     */       }
/*  782:1357 */     } while (!U.compareAndSwapInt(this, STATUS, s, s & 0xFFFF0000 | tag & 0xFFFF));
/*  783:1359 */     return true;
/*  784:     */   }
/*  785:     */   
/*  786:     */   static final class AdaptedRunnable<T>
/*  787:     */     extends ForkJoinTask<T>
/*  788:     */     implements RunnableFuture<T>
/*  789:     */   {
/*  790:     */     final Runnable runnable;
/*  791:     */     T result;
/*  792:     */     private static final long serialVersionUID = 5232453952276885070L;
/*  793:     */     
/*  794:     */     AdaptedRunnable(Runnable runnable, T result)
/*  795:     */     {
/*  796:1373 */       if (runnable == null) {
/*  797:1373 */         throw new NullPointerException();
/*  798:     */       }
/*  799:1374 */       this.runnable = runnable;
/*  800:1375 */       this.result = result;
/*  801:     */     }
/*  802:     */     
/*  803:     */     public final T getRawResult()
/*  804:     */     {
/*  805:1377 */       return this.result;
/*  806:     */     }
/*  807:     */     
/*  808:     */     public final void setRawResult(T v)
/*  809:     */     {
/*  810:1378 */       this.result = v;
/*  811:     */     }
/*  812:     */     
/*  813:     */     public final boolean exec()
/*  814:     */     {
/*  815:1379 */       this.runnable.run();return true;
/*  816:     */     }
/*  817:     */     
/*  818:     */     public final void run()
/*  819:     */     {
/*  820:1380 */       invoke();
/*  821:     */     }
/*  822:     */   }
/*  823:     */   
/*  824:     */   static final class AdaptedRunnableAction
/*  825:     */     extends ForkJoinTask<Void>
/*  826:     */     implements RunnableFuture<Void>
/*  827:     */   {
/*  828:     */     final Runnable runnable;
/*  829:     */     private static final long serialVersionUID = 5232453952276885070L;
/*  830:     */     
/*  831:     */     AdaptedRunnableAction(Runnable runnable)
/*  832:     */     {
/*  833:1391 */       if (runnable == null) {
/*  834:1391 */         throw new NullPointerException();
/*  835:     */       }
/*  836:1392 */       this.runnable = runnable;
/*  837:     */     }
/*  838:     */     
/*  839:     */     public final Void getRawResult()
/*  840:     */     {
/*  841:1394 */       return null;
/*  842:     */     }
/*  843:     */     
/*  844:     */     public final void setRawResult(Void v) {}
/*  845:     */     
/*  846:     */     public final boolean exec()
/*  847:     */     {
/*  848:1396 */       this.runnable.run();return true;
/*  849:     */     }
/*  850:     */     
/*  851:     */     public final void run()
/*  852:     */     {
/*  853:1397 */       invoke();
/*  854:     */     }
/*  855:     */   }
/*  856:     */   
/*  857:     */   static final class RunnableExecuteAction
/*  858:     */     extends ForkJoinTask<Void>
/*  859:     */   {
/*  860:     */     final Runnable runnable;
/*  861:     */     private static final long serialVersionUID = 5232453952276885070L;
/*  862:     */     
/*  863:     */     RunnableExecuteAction(Runnable runnable)
/*  864:     */     {
/*  865:1407 */       if (runnable == null) {
/*  866:1407 */         throw new NullPointerException();
/*  867:     */       }
/*  868:1408 */       this.runnable = runnable;
/*  869:     */     }
/*  870:     */     
/*  871:     */     public final Void getRawResult()
/*  872:     */     {
/*  873:1410 */       return null;
/*  874:     */     }
/*  875:     */     
/*  876:     */     public final void setRawResult(Void v) {}
/*  877:     */     
/*  878:     */     public final boolean exec()
/*  879:     */     {
/*  880:1412 */       this.runnable.run();return true;
/*  881:     */     }
/*  882:     */     
/*  883:     */     void internalPropagateException(Throwable ex)
/*  884:     */     {
/*  885:1414 */       rethrow(ex);
/*  886:     */     }
/*  887:     */   }
/*  888:     */   
/*  889:     */   static final class AdaptedCallable<T>
/*  890:     */     extends ForkJoinTask<T>
/*  891:     */     implements RunnableFuture<T>
/*  892:     */   {
/*  893:     */     final Callable<? extends T> callable;
/*  894:     */     T result;
/*  895:     */     private static final long serialVersionUID = 2838392045355241008L;
/*  896:     */     
/*  897:     */     AdaptedCallable(Callable<? extends T> callable)
/*  898:     */     {
/*  899:1427 */       if (callable == null) {
/*  900:1427 */         throw new NullPointerException();
/*  901:     */       }
/*  902:1428 */       this.callable = callable;
/*  903:     */     }
/*  904:     */     
/*  905:     */     public final T getRawResult()
/*  906:     */     {
/*  907:1430 */       return this.result;
/*  908:     */     }
/*  909:     */     
/*  910:     */     public final void setRawResult(T v)
/*  911:     */     {
/*  912:1431 */       this.result = v;
/*  913:     */     }
/*  914:     */     
/*  915:     */     public final boolean exec()
/*  916:     */     {
/*  917:     */       try
/*  918:     */       {
/*  919:1434 */         this.result = this.callable.call();
/*  920:1435 */         return true;
/*  921:     */       }
/*  922:     */       catch (Error err)
/*  923:     */       {
/*  924:1437 */         throw err;
/*  925:     */       }
/*  926:     */       catch (RuntimeException rex)
/*  927:     */       {
/*  928:1439 */         throw rex;
/*  929:     */       }
/*  930:     */       catch (Exception ex)
/*  931:     */       {
/*  932:1441 */         throw new RuntimeException(ex);
/*  933:     */       }
/*  934:     */     }
/*  935:     */     
/*  936:     */     public final void run()
/*  937:     */     {
/*  938:1444 */       invoke();
/*  939:     */     }
/*  940:     */   }
/*  941:     */   
/*  942:     */   public static ForkJoinTask<?> adapt(Runnable runnable)
/*  943:     */   {
/*  944:1457 */     return new AdaptedRunnableAction(runnable);
/*  945:     */   }
/*  946:     */   
/*  947:     */   public static <T> ForkJoinTask<T> adapt(Runnable runnable, T result)
/*  948:     */   {
/*  949:1470 */     return new AdaptedRunnable(runnable, result);
/*  950:     */   }
/*  951:     */   
/*  952:     */   public static <T> ForkJoinTask<T> adapt(Callable<? extends T> callable)
/*  953:     */   {
/*  954:1483 */     return new AdaptedCallable(callable);
/*  955:     */   }
/*  956:     */   
/*  957:     */   private void writeObject(ObjectOutputStream s)
/*  958:     */     throws IOException
/*  959:     */   {
/*  960:1498 */     s.defaultWriteObject();
/*  961:1499 */     s.writeObject(getException());
/*  962:     */   }
/*  963:     */   
/*  964:     */   private void readObject(ObjectInputStream s)
/*  965:     */     throws IOException, ClassNotFoundException
/*  966:     */   {
/*  967:1507 */     s.defaultReadObject();
/*  968:1508 */     Object ex = s.readObject();
/*  969:1509 */     if (ex != null) {
/*  970:1510 */       setExceptionalCompletion((Throwable)ex);
/*  971:     */     }
/*  972:     */   }
/*  973:     */   
/*  974:1518 */   private static final ReentrantLock exceptionTableLock = new ReentrantLock();
/*  975:1519 */   private static final ReferenceQueue<Object> exceptionTableRefQueue = new ReferenceQueue();
/*  976:     */   private static final int EXCEPTION_MAP_CAPACITY = 32;
/*  977:     */   private static final long serialVersionUID = -7721805057305804111L;
/*  978:     */   private static final Unsafe U;
/*  979:     */   private static final long STATUS;
/*  980:     */   
/*  981:     */   static
/*  982:     */   {
/*  983:1520 */     exceptionTable = new ExceptionNode[32];
/*  984:     */     try
/*  985:     */     {
/*  986:1522 */       U = getUnsafe();
/*  987:1523 */       Class<?> k = ForkJoinTask.class;
/*  988:1524 */       STATUS = U.objectFieldOffset(k.getDeclaredField("status"));
/*  989:     */     }
/*  990:     */     catch (Exception e)
/*  991:     */     {
/*  992:1527 */       throw new Error(e);
/*  993:     */     }
/*  994:     */   }
/*  995:     */   
/*  996:     */   private static Unsafe getUnsafe()
/*  997:     */   {
/*  998:     */     try
/*  999:     */     {
/* 1000:1540 */       return Unsafe.getUnsafe();
/* 1001:     */     }
/* 1002:     */     catch (SecurityException tryReflectionInstead)
/* 1003:     */     {
/* 1004:     */       try
/* 1005:     */       {
/* 1006:1543 */         (Unsafe)AccessController.doPrivileged(new PrivilegedExceptionAction()
/* 1007:     */         {
/* 1008:     */           public Unsafe run()
/* 1009:     */             throws Exception
/* 1010:     */           {
/* 1011:1546 */             Class<Unsafe> k = Unsafe.class;
/* 1012:1547 */             for (Field f : k.getDeclaredFields())
/* 1013:     */             {
/* 1014:1548 */               f.setAccessible(true);
/* 1015:1549 */               Object x = f.get(null);
/* 1016:1550 */               if (k.isInstance(x)) {
/* 1017:1551 */                 return (Unsafe)k.cast(x);
/* 1018:     */               }
/* 1019:     */             }
/* 1020:1553 */             throw new NoSuchFieldError("the Unsafe");
/* 1021:     */           }
/* 1022:     */         });
/* 1023:     */       }
/* 1024:     */       catch (PrivilegedActionException e)
/* 1025:     */       {
/* 1026:1556 */         throw new RuntimeException("Could not initialize intrinsics", e.getCause());
/* 1027:     */       }
/* 1028:     */     }
/* 1029:     */   }
/* 1030:     */   
/* 1031:     */   void internalPropagateException(Throwable ex) {}
/* 1032:     */   
/* 1033:     */   public abstract V getRawResult();
/* 1034:     */   
/* 1035:     */   protected abstract void setRawResult(V paramV);
/* 1036:     */   
/* 1037:     */   protected abstract boolean exec();
/* 1038:     */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.chmv8.ForkJoinTask
 * JD-Core Version:    0.7.0.1
 */