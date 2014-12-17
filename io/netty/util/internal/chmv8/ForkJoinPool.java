/*    1:     */ package io.netty.util.internal.chmv8;
/*    2:     */ 
/*    3:     */ import io.netty.util.internal.ThreadLocalRandom;
/*    4:     */ import java.lang.reflect.Field;
/*    5:     */ import java.security.AccessController;
/*    6:     */ import java.security.PrivilegedAction;
/*    7:     */ import java.security.PrivilegedActionException;
/*    8:     */ import java.security.PrivilegedExceptionAction;
/*    9:     */ import java.util.ArrayList;
/*   10:     */ import java.util.Arrays;
/*   11:     */ import java.util.Collection;
/*   12:     */ import java.util.Collections;
/*   13:     */ import java.util.List;
/*   14:     */ import java.util.concurrent.AbstractExecutorService;
/*   15:     */ import java.util.concurrent.Callable;
/*   16:     */ import java.util.concurrent.Future;
/*   17:     */ import java.util.concurrent.RejectedExecutionException;
/*   18:     */ import java.util.concurrent.RunnableFuture;
/*   19:     */ import java.util.concurrent.TimeUnit;
/*   20:     */ import sun.misc.Unsafe;
/*   21:     */ 
/*   22:     */ public class ForkJoinPool
/*   23:     */   extends AbstractExecutorService
/*   24:     */ {
/*   25:     */   static final ThreadLocal<Submitter> submitters;
/*   26:     */   public static final ForkJoinWorkerThreadFactory defaultForkJoinWorkerThreadFactory;
/*   27:     */   private static final RuntimePermission modifyThreadPermission;
/*   28:     */   static final ForkJoinPool common;
/*   29:     */   static final int commonParallelism;
/*   30:     */   private static int poolNumberSequence;
/*   31:     */   private static final long IDLE_TIMEOUT = 2000000000L;
/*   32:     */   private static final long FAST_IDLE_TIMEOUT = 200000000L;
/*   33:     */   private static final long TIMEOUT_SLOP = 2000000L;
/*   34:     */   private static final int MAX_HELP = 64;
/*   35:     */   private static final int SEED_INCREMENT = 1640531527;
/*   36:     */   private static final int AC_SHIFT = 48;
/*   37:     */   private static final int TC_SHIFT = 32;
/*   38:     */   private static final int ST_SHIFT = 31;
/*   39:     */   private static final int EC_SHIFT = 16;
/*   40:     */   private static final int SMASK = 65535;
/*   41:     */   private static final int MAX_CAP = 32767;
/*   42:     */   private static final int EVENMASK = 65534;
/*   43:     */   private static final int SQMASK = 126;
/*   44:     */   private static final int SHORT_SIGN = 32768;
/*   45:     */   private static final int INT_SIGN = -2147483648;
/*   46:     */   private static final long STOP_BIT = 2147483648L;
/*   47:     */   private static final long AC_MASK = -281474976710656L;
/*   48:     */   private static final long TC_MASK = 281470681743360L;
/*   49:     */   private static final long TC_UNIT = 4294967296L;
/*   50:     */   private static final long AC_UNIT = 281474976710656L;
/*   51:     */   private static final int UAC_SHIFT = 16;
/*   52:     */   private static final int UTC_SHIFT = 0;
/*   53:     */   private static final int UAC_MASK = -65536;
/*   54:     */   private static final int UTC_MASK = 65535;
/*   55:     */   private static final int UAC_UNIT = 65536;
/*   56:     */   private static final int UTC_UNIT = 1;
/*   57:     */   private static final int E_MASK = 2147483647;
/*   58:     */   private static final int E_SEQ = 65536;
/*   59:     */   private static final int SHUTDOWN = -2147483648;
/*   60:     */   private static final int PL_LOCK = 2;
/*   61:     */   private static final int PL_SIGNAL = 1;
/*   62:     */   private static final int PL_SPINS = 256;
/*   63:     */   static final int LIFO_QUEUE = 0;
/*   64:     */   static final int FIFO_QUEUE = 1;
/*   65:     */   static final int SHARED_QUEUE = -1;
/*   66:     */   volatile long pad00;
/*   67:     */   volatile long pad01;
/*   68:     */   volatile long pad02;
/*   69:     */   volatile long pad03;
/*   70:     */   volatile long pad04;
/*   71:     */   volatile long pad05;
/*   72:     */   volatile long pad06;
/*   73:     */   volatile long stealCount;
/*   74:     */   volatile long ctl;
/*   75:     */   volatile int plock;
/*   76:     */   volatile int indexSeed;
/*   77:     */   final short parallelism;
/*   78:     */   final short mode;
/*   79:     */   WorkQueue[] workQueues;
/*   80:     */   final ForkJoinWorkerThreadFactory factory;
/*   81:     */   final Thread.UncaughtExceptionHandler ueh;
/*   82:     */   final String workerNamePrefix;
/*   83:     */   volatile Object pad10;
/*   84:     */   volatile Object pad11;
/*   85:     */   volatile Object pad12;
/*   86:     */   volatile Object pad13;
/*   87:     */   volatile Object pad14;
/*   88:     */   volatile Object pad15;
/*   89:     */   volatile Object pad16;
/*   90:     */   volatile Object pad17;
/*   91:     */   volatile Object pad18;
/*   92:     */   volatile Object pad19;
/*   93:     */   volatile Object pad1a;
/*   94:     */   volatile Object pad1b;
/*   95:     */   private static final Unsafe U;
/*   96:     */   private static final long CTL;
/*   97:     */   private static final long PARKBLOCKER;
/*   98:     */   private static final int ABASE;
/*   99:     */   private static final int ASHIFT;
/*  100:     */   private static final long STEALCOUNT;
/*  101:     */   private static final long PLOCK;
/*  102:     */   private static final long INDEXSEED;
/*  103:     */   private static final long QBASE;
/*  104:     */   private static final long QLOCK;
/*  105:     */   
/*  106:     */   private static void checkPermission()
/*  107:     */   {
/*  108: 534 */     SecurityManager security = System.getSecurityManager();
/*  109: 535 */     if (security != null) {
/*  110: 536 */       security.checkPermission(modifyThreadPermission);
/*  111:     */     }
/*  112:     */   }
/*  113:     */   
/*  114:     */   public static abstract interface ForkJoinWorkerThreadFactory
/*  115:     */   {
/*  116:     */     public abstract ForkJoinWorkerThread newThread(ForkJoinPool paramForkJoinPool);
/*  117:     */   }
/*  118:     */   
/*  119:     */   static final class DefaultForkJoinWorkerThreadFactory
/*  120:     */     implements ForkJoinPool.ForkJoinWorkerThreadFactory
/*  121:     */   {
/*  122:     */     public final ForkJoinWorkerThread newThread(ForkJoinPool pool)
/*  123:     */     {
/*  124: 565 */       return new ForkJoinWorkerThread(pool);
/*  125:     */     }
/*  126:     */   }
/*  127:     */   
/*  128:     */   static final class EmptyTask
/*  129:     */     extends ForkJoinTask<Void>
/*  130:     */   {
/*  131:     */     private static final long serialVersionUID = -7721805057305804111L;
/*  132:     */     
/*  133:     */     EmptyTask()
/*  134:     */     {
/*  135: 577 */       this.status = -268435456;
/*  136:     */     }
/*  137:     */     
/*  138:     */     public final Void getRawResult()
/*  139:     */     {
/*  140: 578 */       return null;
/*  141:     */     }
/*  142:     */     
/*  143:     */     public final void setRawResult(Void x) {}
/*  144:     */     
/*  145:     */     public final boolean exec()
/*  146:     */     {
/*  147: 580 */       return true;
/*  148:     */     }
/*  149:     */   }
/*  150:     */   
/*  151:     */   static final class WorkQueue
/*  152:     */   {
/*  153:     */     static final int INITIAL_QUEUE_CAPACITY = 8192;
/*  154:     */     static final int MAXIMUM_QUEUE_CAPACITY = 67108864;
/*  155:     */     volatile long pad00;
/*  156:     */     volatile long pad01;
/*  157:     */     volatile long pad02;
/*  158:     */     volatile long pad03;
/*  159:     */     volatile long pad04;
/*  160:     */     volatile long pad05;
/*  161:     */     volatile long pad06;
/*  162:     */     volatile int eventCount;
/*  163:     */     int nextWait;
/*  164:     */     int nsteals;
/*  165:     */     int hint;
/*  166:     */     short poolIndex;
/*  167:     */     final short mode;
/*  168:     */     volatile int qlock;
/*  169:     */     volatile int base;
/*  170:     */     int top;
/*  171:     */     ForkJoinTask<?>[] array;
/*  172:     */     final ForkJoinPool pool;
/*  173:     */     final ForkJoinWorkerThread owner;
/*  174:     */     volatile Thread parker;
/*  175:     */     volatile ForkJoinTask<?> currentJoin;
/*  176:     */     ForkJoinTask<?> currentSteal;
/*  177:     */     volatile Object pad10;
/*  178:     */     volatile Object pad11;
/*  179:     */     volatile Object pad12;
/*  180:     */     volatile Object pad13;
/*  181:     */     volatile Object pad14;
/*  182:     */     volatile Object pad15;
/*  183:     */     volatile Object pad16;
/*  184:     */     volatile Object pad17;
/*  185:     */     volatile Object pad18;
/*  186:     */     volatile Object pad19;
/*  187:     */     volatile Object pad1a;
/*  188:     */     volatile Object pad1b;
/*  189:     */     volatile Object pad1c;
/*  190:     */     volatile Object pad1d;
/*  191:     */     private static final Unsafe U;
/*  192:     */     private static final long QBASE;
/*  193:     */     private static final long QLOCK;
/*  194:     */     private static final int ABASE;
/*  195:     */     private static final int ASHIFT;
/*  196:     */     
/*  197:     */     WorkQueue(ForkJoinPool pool, ForkJoinWorkerThread owner, int mode, int seed)
/*  198:     */     {
/*  199: 676 */       this.pool = pool;
/*  200: 677 */       this.owner = owner;
/*  201: 678 */       this.mode = ((short)mode);
/*  202: 679 */       this.hint = seed;
/*  203:     */       
/*  204: 681 */       this.base = (this.top = 4096);
/*  205:     */     }
/*  206:     */     
/*  207:     */     final int queueSize()
/*  208:     */     {
/*  209: 688 */       int n = this.base - this.top;
/*  210: 689 */       return n >= 0 ? 0 : -n;
/*  211:     */     }
/*  212:     */     
/*  213:     */     final boolean isEmpty()
/*  214:     */     {
/*  215:     */       int s;
/*  216: 699 */       int n = this.base - (s = this.top);
/*  217:     */       ForkJoinTask<?>[] a;
/*  218:     */       int m;
/*  219: 700 */       return (n >= 0) || ((n == -1) && (((a = this.array) == null) || ((m = a.length - 1) < 0) || (U.getObject(a, ((m & s - 1) << ASHIFT) + ABASE) == null)));
/*  220:     */     }
/*  221:     */     
/*  222:     */     final void push(ForkJoinTask<?> task)
/*  223:     */     {
/*  224: 717 */       int s = this.top;
/*  225:     */       ForkJoinTask<?>[] a;
/*  226: 718 */       if ((a = this.array) != null)
/*  227:     */       {
/*  228: 719 */         int m = a.length - 1;
/*  229: 720 */         U.putOrderedObject(a, ((m & s) << ASHIFT) + ABASE, task);
/*  230:     */         int n;
/*  231: 721 */         if ((n = (this.top = s + 1) - this.base) <= 2)
/*  232:     */         {
/*  233:     */           ForkJoinPool p;
/*  234: 722 */           (p = this.pool).signalWork(p.workQueues, this);
/*  235:     */         }
/*  236: 723 */         else if (n >= m)
/*  237:     */         {
/*  238: 724 */           growArray();
/*  239:     */         }
/*  240:     */       }
/*  241:     */     }
/*  242:     */     
/*  243:     */     final ForkJoinTask<?>[] growArray()
/*  244:     */     {
/*  245: 734 */       ForkJoinTask<?>[] oldA = this.array;
/*  246: 735 */       int size = oldA != null ? oldA.length << 1 : 8192;
/*  247: 736 */       if (size > 67108864) {
/*  248: 737 */         throw new RejectedExecutionException("Queue capacity exceeded");
/*  249:     */       }
/*  250: 739 */       ForkJoinTask<?>[] a = this.array = new ForkJoinTask[size];
/*  251:     */       int oldMask;
/*  252:     */       int t;
/*  253:     */       int b;
/*  254: 740 */       if ((oldA != null) && ((oldMask = oldA.length - 1) >= 0) && ((t = this.top) - (b = this.base) > 0))
/*  255:     */       {
/*  256: 742 */         int mask = size - 1;
/*  257:     */         do
/*  258:     */         {
/*  259: 745 */           int oldj = ((b & oldMask) << ASHIFT) + ABASE;
/*  260: 746 */           int j = ((b & mask) << ASHIFT) + ABASE;
/*  261: 747 */           ForkJoinTask<?> x = (ForkJoinTask)U.getObjectVolatile(oldA, oldj);
/*  262: 748 */           if ((x != null) && (U.compareAndSwapObject(oldA, oldj, x, null))) {
/*  263: 750 */             U.putObjectVolatile(a, j, x);
/*  264:     */           }
/*  265: 751 */           b++;
/*  266: 751 */         } while (b != t);
/*  267:     */       }
/*  268: 753 */       return a;
/*  269:     */     }
/*  270:     */     
/*  271:     */     final ForkJoinTask<?> pop()
/*  272:     */     {
/*  273:     */       ForkJoinTask<?>[] a;
/*  274:     */       int m;
/*  275: 762 */       if (((a = this.array) != null) && ((m = a.length - 1) >= 0))
/*  276:     */       {
/*  277:     */         int s;
/*  278: 763 */         while ((s = this.top - 1) - this.base >= 0)
/*  279:     */         {
/*  280: 764 */           long j = ((m & s) << ASHIFT) + ABASE;
/*  281:     */           ForkJoinTask<?> t;
/*  282: 765 */           if ((t = (ForkJoinTask)U.getObject(a, j)) == null) {
/*  283:     */             break;
/*  284:     */           }
/*  285: 767 */           if (U.compareAndSwapObject(a, j, t, null))
/*  286:     */           {
/*  287: 768 */             this.top = s;
/*  288: 769 */             return t;
/*  289:     */           }
/*  290:     */         }
/*  291:     */       }
/*  292: 773 */       return null;
/*  293:     */     }
/*  294:     */     
/*  295:     */     final ForkJoinTask<?> pollAt(int b)
/*  296:     */     {
/*  297:     */       ForkJoinTask<?>[] a;
/*  298: 783 */       if ((a = this.array) != null)
/*  299:     */       {
/*  300: 784 */         int j = ((a.length - 1 & b) << ASHIFT) + ABASE;
/*  301:     */         ForkJoinTask<?> t;
/*  302: 785 */         if (((t = (ForkJoinTask)U.getObjectVolatile(a, j)) != null) && (this.base == b) && (U.compareAndSwapObject(a, j, t, null)))
/*  303:     */         {
/*  304: 787 */           U.putOrderedInt(this, QBASE, b + 1);
/*  305: 788 */           return t;
/*  306:     */         }
/*  307:     */       }
/*  308: 791 */       return null;
/*  309:     */     }
/*  310:     */     
/*  311:     */     final ForkJoinTask<?> poll()
/*  312:     */     {
/*  313:     */       int b;
/*  314:     */       ForkJoinTask<?>[] a;
/*  315: 799 */       while (((b = this.base) - this.top < 0) && ((a = this.array) != null))
/*  316:     */       {
/*  317: 800 */         int j = ((a.length - 1 & b) << ASHIFT) + ABASE;
/*  318: 801 */         ForkJoinTask<?> t = (ForkJoinTask)U.getObjectVolatile(a, j);
/*  319: 802 */         if (t != null)
/*  320:     */         {
/*  321: 803 */           if (U.compareAndSwapObject(a, j, t, null))
/*  322:     */           {
/*  323: 804 */             U.putOrderedInt(this, QBASE, b + 1);
/*  324: 805 */             return t;
/*  325:     */           }
/*  326:     */         }
/*  327: 808 */         else if (this.base == b)
/*  328:     */         {
/*  329: 809 */           if (b + 1 == this.top) {
/*  330:     */             break;
/*  331:     */           }
/*  332: 811 */           Thread.yield();
/*  333:     */         }
/*  334:     */       }
/*  335: 814 */       return null;
/*  336:     */     }
/*  337:     */     
/*  338:     */     final ForkJoinTask<?> nextLocalTask()
/*  339:     */     {
/*  340: 821 */       return this.mode == 0 ? pop() : poll();
/*  341:     */     }
/*  342:     */     
/*  343:     */     final ForkJoinTask<?> peek()
/*  344:     */     {
/*  345: 828 */       ForkJoinTask<?>[] a = this.array;
/*  346:     */       int m;
/*  347: 829 */       if ((a == null) || ((m = a.length - 1) < 0)) {
/*  348: 830 */         return null;
/*  349:     */       }
/*  350:     */       int m;
/*  351: 831 */       int i = this.mode == 0 ? this.top - 1 : this.base;
/*  352: 832 */       int j = ((i & m) << ASHIFT) + ABASE;
/*  353: 833 */       return (ForkJoinTask)U.getObjectVolatile(a, j);
/*  354:     */     }
/*  355:     */     
/*  356:     */     final boolean tryUnpush(ForkJoinTask<?> t)
/*  357:     */     {
/*  358:     */       ForkJoinTask<?>[] a;
/*  359:     */       int s;
/*  360: 842 */       if (((a = this.array) != null) && ((s = this.top) != this.base) && (U.compareAndSwapObject(a, ((a.length - 1 & --s) << ASHIFT) + ABASE, t, null)))
/*  361:     */       {
/*  362: 845 */         this.top = s;
/*  363: 846 */         return true;
/*  364:     */       }
/*  365: 848 */       return false;
/*  366:     */     }
/*  367:     */     
/*  368:     */     final void cancelAll()
/*  369:     */     {
/*  370: 855 */       ForkJoinTask.cancelIgnoringExceptions(this.currentJoin);
/*  371: 856 */       ForkJoinTask.cancelIgnoringExceptions(this.currentSteal);
/*  372:     */       ForkJoinTask<?> t;
/*  373: 857 */       while ((t = poll()) != null) {
/*  374: 858 */         ForkJoinTask.cancelIgnoringExceptions(t);
/*  375:     */       }
/*  376:     */     }
/*  377:     */     
/*  378:     */     final void pollAndExecAll()
/*  379:     */     {
/*  380:     */       ForkJoinTask<?> t;
/*  381: 867 */       while ((t = poll()) != null) {
/*  382: 868 */         t.doExec();
/*  383:     */       }
/*  384:     */     }
/*  385:     */     
/*  386:     */     final void runTask(ForkJoinTask<?> task)
/*  387:     */     {
/*  388: 876 */       if ((this.currentSteal = task) != null)
/*  389:     */       {
/*  390: 877 */         task.doExec();
/*  391: 878 */         ForkJoinTask<?>[] a = this.array;
/*  392: 879 */         int md = this.mode;
/*  393: 880 */         this.nsteals += 1;
/*  394: 881 */         this.currentSteal = null;
/*  395: 882 */         if (md != 0)
/*  396:     */         {
/*  397: 883 */           pollAndExecAll();
/*  398:     */         }
/*  399: 884 */         else if (a != null)
/*  400:     */         {
/*  401: 885 */           int m = a.length - 1;
/*  402:     */           int s;
/*  403: 886 */           while ((s = this.top - 1) - this.base >= 0)
/*  404:     */           {
/*  405: 887 */             long i = ((m & s) << ASHIFT) + ABASE;
/*  406: 888 */             ForkJoinTask<?> t = (ForkJoinTask)U.getObject(a, i);
/*  407: 889 */             if (t == null) {
/*  408:     */               break;
/*  409:     */             }
/*  410: 891 */             if (U.compareAndSwapObject(a, i, t, null))
/*  411:     */             {
/*  412: 892 */               this.top = s;
/*  413: 893 */               t.doExec();
/*  414:     */             }
/*  415:     */           }
/*  416:     */         }
/*  417:     */       }
/*  418:     */     }
/*  419:     */     
/*  420:     */     final boolean tryRemoveAndExec(ForkJoinTask<?> task)
/*  421:     */     {
/*  422:     */       ForkJoinTask<?>[] a;
/*  423:     */       int m;
/*  424:     */       int s;
/*  425:     */       int b;
/*  426:     */       int n;
/*  427:     */       boolean stat;
/*  428: 910 */       if ((task != null) && ((a = this.array) != null) && ((m = a.length - 1) >= 0) && ((n = (s = this.top) - (b = this.base)) > 0))
/*  429:     */       {
/*  430: 912 */         boolean removed = false;boolean empty = true;
/*  431: 913 */         boolean stat = true;
/*  432:     */         for (;;)
/*  433:     */         {
/*  434: 915 */           s--;long j = ((s & m) << ASHIFT) + ABASE;
/*  435: 916 */           ForkJoinTask<?> t = (ForkJoinTask)U.getObject(a, j);
/*  436: 917 */           if (t == null) {
/*  437:     */             break;
/*  438:     */           }
/*  439: 919 */           if (t == task)
/*  440:     */           {
/*  441: 920 */             if (s + 1 == this.top)
/*  442:     */             {
/*  443: 921 */               if (!U.compareAndSwapObject(a, j, task, null)) {
/*  444:     */                 break;
/*  445:     */               }
/*  446: 923 */               this.top = s;
/*  447: 924 */               removed = true; break;
/*  448:     */             }
/*  449: 926 */             if (this.base != b) {
/*  450:     */               break;
/*  451:     */             }
/*  452: 927 */             removed = U.compareAndSwapObject(a, j, task, new ForkJoinPool.EmptyTask()); break;
/*  453:     */           }
/*  454: 931 */           if (t.status >= 0)
/*  455:     */           {
/*  456: 932 */             empty = false;
/*  457:     */           }
/*  458: 933 */           else if (s + 1 == this.top)
/*  459:     */           {
/*  460: 934 */             if (!U.compareAndSwapObject(a, j, t, null)) {
/*  461:     */               break;
/*  462:     */             }
/*  463: 935 */             this.top = s; break;
/*  464:     */           }
/*  465: 938 */           n--;
/*  466: 938 */           if (n == 0)
/*  467:     */           {
/*  468: 939 */             if ((empty) || (this.base != b)) {
/*  469:     */               break;
/*  470:     */             }
/*  471: 940 */             stat = false; break;
/*  472:     */           }
/*  473:     */         }
/*  474: 944 */         if (removed) {
/*  475: 945 */           task.doExec();
/*  476:     */         }
/*  477:     */       }
/*  478:     */       else
/*  479:     */       {
/*  480: 948 */         stat = false;
/*  481:     */       }
/*  482: 949 */       return stat;
/*  483:     */     }
/*  484:     */     
/*  485:     */     final boolean pollAndExecCC(CountedCompleter<?> root)
/*  486:     */     {
/*  487:     */       int b;
/*  488:     */       ForkJoinTask<?>[] a;
/*  489: 958 */       if (((b = this.base) - this.top < 0) && ((a = this.array) != null))
/*  490:     */       {
/*  491: 959 */         long j = ((a.length - 1 & b) << ASHIFT) + ABASE;
/*  492:     */         Object o;
/*  493: 960 */         if ((o = U.getObjectVolatile(a, j)) == null) {
/*  494: 961 */           return true;
/*  495:     */         }
/*  496: 962 */         if ((o instanceof CountedCompleter))
/*  497:     */         {
/*  498: 963 */           CountedCompleter<?> t = (CountedCompleter)o;CountedCompleter<?> r = t;
/*  499:     */           for (;;)
/*  500:     */           {
/*  501: 964 */             if (r == root)
/*  502:     */             {
/*  503: 965 */               if ((this.base == b) && (U.compareAndSwapObject(a, j, t, null)))
/*  504:     */               {
/*  505: 967 */                 U.putOrderedInt(this, QBASE, b + 1);
/*  506: 968 */                 t.doExec();
/*  507:     */               }
/*  508: 970 */               return true;
/*  509:     */             }
/*  510: 972 */             if ((r = r.completer) == null) {
/*  511:     */               break;
/*  512:     */             }
/*  513:     */           }
/*  514:     */         }
/*  515:     */       }
/*  516: 977 */       return false;
/*  517:     */     }
/*  518:     */     
/*  519:     */     final boolean externalPopAndExecCC(CountedCompleter<?> root)
/*  520:     */     {
/*  521:     */       int s;
/*  522:     */       ForkJoinTask<?>[] a;
/*  523: 986 */       if ((this.base - (s = this.top) < 0) && ((a = this.array) != null))
/*  524:     */       {
/*  525: 987 */         long j = ((a.length - 1 & s - 1) << ASHIFT) + ABASE;
/*  526:     */         Object o;
/*  527: 988 */         if (((o = U.getObject(a, j)) instanceof CountedCompleter))
/*  528:     */         {
/*  529: 989 */           CountedCompleter<?> t = (CountedCompleter)o;CountedCompleter<?> r = t;
/*  530:     */           for (;;)
/*  531:     */           {
/*  532: 990 */             if (r == root)
/*  533:     */             {
/*  534: 991 */               if (U.compareAndSwapInt(this, QLOCK, 0, 1)) {
/*  535: 992 */                 if ((this.top == s) && (this.array == a) && (U.compareAndSwapObject(a, j, t, null)))
/*  536:     */                 {
/*  537: 994 */                   this.top = (s - 1);
/*  538: 995 */                   this.qlock = 0;
/*  539: 996 */                   t.doExec();
/*  540:     */                 }
/*  541:     */                 else
/*  542:     */                 {
/*  543: 999 */                   this.qlock = 0;
/*  544:     */                 }
/*  545:     */               }
/*  546:1001 */               return true;
/*  547:     */             }
/*  548:1003 */             if ((r = r.completer) == null) {
/*  549:     */               break;
/*  550:     */             }
/*  551:     */           }
/*  552:     */         }
/*  553:     */       }
/*  554:1008 */       return false;
/*  555:     */     }
/*  556:     */     
/*  557:     */     final boolean internalPopAndExecCC(CountedCompleter<?> root)
/*  558:     */     {
/*  559:     */       int s;
/*  560:     */       ForkJoinTask<?>[] a;
/*  561:1016 */       if ((this.base - (s = this.top) < 0) && ((a = this.array) != null))
/*  562:     */       {
/*  563:1017 */         long j = ((a.length - 1 & s - 1) << ASHIFT) + ABASE;
/*  564:     */         Object o;
/*  565:1018 */         if (((o = U.getObject(a, j)) instanceof CountedCompleter))
/*  566:     */         {
/*  567:1019 */           CountedCompleter<?> t = (CountedCompleter)o;CountedCompleter<?> r = t;
/*  568:     */           for (;;)
/*  569:     */           {
/*  570:1020 */             if (r == root)
/*  571:     */             {
/*  572:1021 */               if (U.compareAndSwapObject(a, j, t, null))
/*  573:     */               {
/*  574:1022 */                 this.top = (s - 1);
/*  575:1023 */                 t.doExec();
/*  576:     */               }
/*  577:1025 */               return true;
/*  578:     */             }
/*  579:1027 */             if ((r = r.completer) == null) {
/*  580:     */               break;
/*  581:     */             }
/*  582:     */           }
/*  583:     */         }
/*  584:     */       }
/*  585:1032 */       return false;
/*  586:     */     }
/*  587:     */     
/*  588:     */     final boolean isApparentlyUnblocked()
/*  589:     */     {
/*  590:     */       Thread wt;
/*  591:     */       Thread.State s;
/*  592:1040 */       return (this.eventCount >= 0) && ((wt = this.owner) != null) && ((s = wt.getState()) != Thread.State.BLOCKED) && (s != Thread.State.WAITING) && (s != Thread.State.TIMED_WAITING);
/*  593:     */     }
/*  594:     */     
/*  595:     */     static
/*  596:     */     {
/*  597:     */       try
/*  598:     */       {
/*  599:1055 */         U = ForkJoinPool.access$000();
/*  600:1056 */         Class<?> k = WorkQueue.class;
/*  601:1057 */         Class<?> ak = [Lio.netty.util.internal.chmv8.ForkJoinTask.class;
/*  602:1058 */         QBASE = U.objectFieldOffset(k.getDeclaredField("base"));
/*  603:     */         
/*  604:1060 */         QLOCK = U.objectFieldOffset(k.getDeclaredField("qlock"));
/*  605:     */         
/*  606:1062 */         ABASE = U.arrayBaseOffset(ak);
/*  607:1063 */         int scale = U.arrayIndexScale(ak);
/*  608:1064 */         if ((scale & scale - 1) != 0) {
/*  609:1065 */           throw new Error("data type scale not a power of two");
/*  610:     */         }
/*  611:1066 */         ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
/*  612:     */       }
/*  613:     */       catch (Exception e)
/*  614:     */       {
/*  615:1068 */         throw new Error(e);
/*  616:     */       }
/*  617:     */     }
/*  618:     */   }
/*  619:     */   
/*  620:     */   private static final synchronized int nextPoolId()
/*  621:     */   {
/*  622:1123 */     return ++poolNumberSequence;
/*  623:     */   }
/*  624:     */   
/*  625:     */   private int acquirePlock()
/*  626:     */   {
/*  627:1275 */     int spins = 256;
/*  628:     */     for (;;)
/*  629:     */     {
/*  630:     */       int ps;
/*  631:     */       int nps;
/*  632:1277 */       if ((((ps = this.plock) & 0x2) == 0) && (U.compareAndSwapInt(this, PLOCK, ps, nps = ps + 2))) {
/*  633:1279 */         return nps;
/*  634:     */       }
/*  635:1280 */       if (spins >= 0)
/*  636:     */       {
/*  637:1281 */         if (ThreadLocalRandom.current().nextInt() >= 0) {
/*  638:1282 */           spins--;
/*  639:     */         }
/*  640:     */       }
/*  641:1284 */       else if (U.compareAndSwapInt(this, PLOCK, ps, ps | 0x1)) {
/*  642:1285 */         synchronized (this)
/*  643:     */         {
/*  644:1286 */           if ((this.plock & 0x1) != 0) {
/*  645:     */             try
/*  646:     */             {
/*  647:1288 */               wait();
/*  648:     */             }
/*  649:     */             catch (InterruptedException ie)
/*  650:     */             {
/*  651:     */               try
/*  652:     */               {
/*  653:1291 */                 Thread.currentThread().interrupt();
/*  654:     */               }
/*  655:     */               catch (SecurityException ignore) {}
/*  656:     */             }
/*  657:     */           } else {
/*  658:1297 */             notifyAll();
/*  659:     */           }
/*  660:     */         }
/*  661:     */       }
/*  662:     */     }
/*  663:     */   }
/*  664:     */   
/*  665:     */   private void releasePlock(int ps)
/*  666:     */   {
/*  667:1308 */     this.plock = ps;
/*  668:1309 */     synchronized (this)
/*  669:     */     {
/*  670:1309 */       notifyAll();
/*  671:     */     }
/*  672:     */   }
/*  673:     */   
/*  674:     */   private void tryAddWorker()
/*  675:     */   {
/*  676:     */     long c;
/*  677:     */     int u;
/*  678:     */     int e;
/*  679:1319 */     while (((u = (int)((c = this.ctl) >>> 32)) < 0) && ((u & 0x8000) != 0) && ((e = (int)c) >= 0))
/*  680:     */     {
/*  681:1320 */       long nc = (u + 1 & 0xFFFF | u + 65536 & 0xFFFF0000) << 32 | e;
/*  682:1322 */       if (U.compareAndSwapLong(this, CTL, c, nc))
/*  683:     */       {
/*  684:1324 */         Throwable ex = null;
/*  685:1325 */         ForkJoinWorkerThread wt = null;
/*  686:     */         try
/*  687:     */         {
/*  688:     */           ForkJoinWorkerThreadFactory fac;
/*  689:1327 */           if (((fac = this.factory) != null) && ((wt = fac.newThread(this)) != null))
/*  690:     */           {
/*  691:1329 */             wt.start();
/*  692:1330 */             break;
/*  693:     */           }
/*  694:     */         }
/*  695:     */         catch (Throwable rex)
/*  696:     */         {
/*  697:1333 */           ex = rex;
/*  698:     */         }
/*  699:1335 */         deregisterWorker(wt, ex);
/*  700:1336 */         break;
/*  701:     */       }
/*  702:     */     }
/*  703:     */   }
/*  704:     */   
/*  705:     */   final WorkQueue registerWorker(ForkJoinWorkerThread wt)
/*  706:     */   {
/*  707:1355 */     wt.setDaemon(true);
/*  708:     */     Thread.UncaughtExceptionHandler handler;
/*  709:1356 */     if ((handler = this.ueh) != null) {
/*  710:1357 */       wt.setUncaughtExceptionHandler(handler);
/*  711:     */     }
/*  712:     */     int s;
/*  713:     */     do
/*  714:     */     {
/*  715:1359 */       s += 1640531527;
/*  716:1359 */     } while ((!U.compareAndSwapInt(this, INDEXSEED, s = this.indexSeed, s)) || (s == 0));
/*  717:1361 */     WorkQueue w = new WorkQueue(this, wt, this.mode, s);
/*  718:     */     int ps;
/*  719:1362 */     if ((((ps = this.plock) & 0x2) != 0) || (!U.compareAndSwapInt(this, PLOCK, , ps))) {
/*  720:1364 */       ps = acquirePlock();
/*  721:     */     }
/*  722:1365 */     int nps = ps & 0x80000000 | ps + 2 & 0x7FFFFFFF;
/*  723:     */     try
/*  724:     */     {
/*  725:     */       WorkQueue[] ws;
/*  726:1367 */       if ((ws = this.workQueues) != null)
/*  727:     */       {
/*  728:1368 */         int n = ws.length;int m = n - 1;
/*  729:1369 */         int r = s << 1 | 0x1;
/*  730:1370 */         if (ws[(r &= m)] != null)
/*  731:     */         {
/*  732:1371 */           int probes = 0;
/*  733:1372 */           int step = n <= 4 ? 2 : (n >>> 1 & 0xFFFE) + 2;
/*  734:1373 */           while (ws[(r = r + step & m)] != null)
/*  735:     */           {
/*  736:1374 */             probes++;
/*  737:1374 */             if (probes >= n)
/*  738:     */             {
/*  739:1375 */               this.workQueues = (ws = (WorkQueue[])Arrays.copyOf(ws, n <<= 1));
/*  740:1376 */               m = n - 1;
/*  741:1377 */               probes = 0;
/*  742:     */             }
/*  743:     */           }
/*  744:     */         }
/*  745:1381 */         w.poolIndex = ((short)r);
/*  746:1382 */         w.eventCount = r;
/*  747:1383 */         ws[r] = w;
/*  748:     */       }
/*  749:     */     }
/*  750:     */     finally
/*  751:     */     {
/*  752:1386 */       if (!U.compareAndSwapInt(this, PLOCK, ps, nps)) {
/*  753:1387 */         releasePlock(nps);
/*  754:     */       }
/*  755:     */     }
/*  756:1389 */     wt.setName(this.workerNamePrefix.concat(Integer.toString(w.poolIndex >>> 1)));
/*  757:1390 */     return w;
/*  758:     */   }
/*  759:     */   
/*  760:     */   final void deregisterWorker(ForkJoinWorkerThread wt, Throwable ex)
/*  761:     */   {
/*  762:1403 */     WorkQueue w = null;
/*  763:1404 */     if ((wt != null) && ((w = wt.workQueue) != null))
/*  764:     */     {
/*  765:1406 */       w.qlock = -1;
/*  766:     */       long sc;
/*  767:1407 */       while (!U.compareAndSwapLong(this, STEALCOUNT, sc = this.stealCount, sc + w.nsteals)) {}
/*  768:     */       int ps;
/*  769:1410 */       if ((((ps = this.plock) & 0x2) != 0) || (!U.compareAndSwapInt(this, PLOCK, , ps))) {
/*  770:1412 */         ps = acquirePlock();
/*  771:     */       }
/*  772:1413 */       int nps = ps & 0x80000000 | ps + 2 & 0x7FFFFFFF;
/*  773:     */       try
/*  774:     */       {
/*  775:1415 */         int idx = w.poolIndex;
/*  776:1416 */         WorkQueue[] ws = this.workQueues;
/*  777:1417 */         if ((ws != null) && (idx >= 0) && (idx < ws.length) && (ws[idx] == w)) {
/*  778:1418 */           ws[idx] = null;
/*  779:     */         }
/*  780:     */       }
/*  781:     */       finally
/*  782:     */       {
/*  783:1420 */         if (!U.compareAndSwapInt(this, PLOCK, ps, nps)) {
/*  784:1421 */           releasePlock(nps);
/*  785:     */         }
/*  786:     */       }
/*  787:     */     }
/*  788:     */     long c;
/*  789:1426 */     while (!U.compareAndSwapLong(this, CTL, c = this.ctl, c - 281474976710656L & 0x0 | c - 4294967296L & 0x0 | c & 0xFFFFFFFF)) {}
/*  790:1431 */     if ((!tryTerminate(false, false)) && (w != null) && (w.array != null))
/*  791:     */     {
/*  792:1432 */       w.cancelAll();
/*  793:     */       int u;
/*  794:     */       int e;
/*  795:1434 */       while (((u = (int)((c = this.ctl) >>> 32)) < 0) && ((e = (int)c) >= 0)) {
/*  796:1435 */         if (e > 0)
/*  797:     */         {
/*  798:     */           WorkQueue[] ws;
/*  799:     */           int i;
/*  800:     */           WorkQueue v;
/*  801:1436 */           if (((ws = this.workQueues) != null) && ((i = e & 0xFFFF) < ws.length) && ((v = ws[i]) != null))
/*  802:     */           {
/*  803:1440 */             long nc = v.nextWait & 0x7FFFFFFF | u + 65536 << 32;
/*  804:1442 */             if (v.eventCount == (e | 0x80000000)) {
/*  805:1444 */               if (U.compareAndSwapLong(this, CTL, c, nc))
/*  806:     */               {
/*  807:1445 */                 v.eventCount = (e + 65536 & 0x7FFFFFFF);
/*  808:     */                 Thread p;
/*  809:1446 */                 if ((p = v.parker) == null) {
/*  810:     */                   break;
/*  811:     */                 }
/*  812:1447 */                 U.unpark(p);
/*  813:     */               }
/*  814:     */             }
/*  815:     */           }
/*  816:     */         }
/*  817:1452 */         else if ((short)u < 0)
/*  818:     */         {
/*  819:1453 */           tryAddWorker();
/*  820:     */         }
/*  821:     */       }
/*  822:     */     }
/*  823:1458 */     if (ex == null) {
/*  824:1459 */       ForkJoinTask.helpExpungeStaleExceptions();
/*  825:     */     } else {
/*  826:1461 */       ForkJoinTask.rethrow(ex);
/*  827:     */     }
/*  828:     */   }
/*  829:     */   
/*  830:     */   static final class Submitter
/*  831:     */   {
/*  832:     */     int seed;
/*  833:     */     
/*  834:     */     Submitter(int s)
/*  835:     */     {
/*  836:1483 */       this.seed = s;
/*  837:     */     }
/*  838:     */   }
/*  839:     */   
/*  840:     */   final void externalPush(ForkJoinTask<?> task)
/*  841:     */   {
/*  842:1495 */     Submitter z = (Submitter)submitters.get();
/*  843:     */     
/*  844:1497 */     int ps = this.plock;
/*  845:1498 */     WorkQueue[] ws = this.workQueues;
/*  846:     */     int m;
/*  847:     */     int r;
/*  848:     */     WorkQueue q;
/*  849:1499 */     if ((z != null) && (ps > 0) && (ws != null) && ((m = ws.length - 1) >= 0) && ((q = ws[(m & (r = z.seed) & 0x7E)]) != null) && (r != 0) && (U.compareAndSwapInt(q, QLOCK, 0, 1)))
/*  850:     */     {
/*  851:     */       ForkJoinTask<?>[] a;
/*  852:     */       int am;
/*  853:     */       int s;
/*  854:     */       int n;
/*  855:1502 */       if (((a = q.array) != null) && ((am = a.length - 1) > (n = (s = q.top) - q.base)))
/*  856:     */       {
/*  857:1504 */         int j = ((am & s) << ASHIFT) + ABASE;
/*  858:1505 */         U.putOrderedObject(a, j, task);
/*  859:1506 */         q.top = (s + 1);
/*  860:1507 */         q.qlock = 0;
/*  861:1508 */         if (n <= 1) {
/*  862:1509 */           signalWork(ws, q);
/*  863:     */         }
/*  864:1510 */         return;
/*  865:     */       }
/*  866:1512 */       q.qlock = 0;
/*  867:     */     }
/*  868:1514 */     fullExternalPush(task);
/*  869:     */   }
/*  870:     */   
/*  871:     */   private void fullExternalPush(ForkJoinTask<?> task)
/*  872:     */   {
/*  873:1535 */     int r = 0;
/*  874:1536 */     Submitter z = (Submitter)submitters.get();
/*  875:     */     for (;;)
/*  876:     */     {
/*  877:1538 */       if (z == null)
/*  878:     */       {
/*  879:1539 */         r += 1640531527;
/*  880:1539 */         if ((U.compareAndSwapInt(this, INDEXSEED, r = this.indexSeed, r)) && (r != 0)) {
/*  881:1541 */           submitters.set(z = new Submitter(r));
/*  882:     */         }
/*  883:     */       }
/*  884:1543 */       else if (r == 0)
/*  885:     */       {
/*  886:1544 */         r = z.seed;
/*  887:1545 */         r ^= r << 13;
/*  888:1546 */         r ^= r >>> 17;
/*  889:1547 */         z.seed = (r ^= r << 5);
/*  890:     */       }
/*  891:     */       int ps;
/*  892:1549 */       if ((ps = this.plock) < 0) {
/*  893:1550 */         throw new RejectedExecutionException();
/*  894:     */       }
/*  895:     */       WorkQueue[] ws;
/*  896:     */       int m;
/*  897:     */       WorkQueue[] ws;
/*  898:1551 */       if ((ps == 0) || ((ws = this.workQueues) == null) || ((m = ws.length - 1) < 0))
/*  899:     */       {
/*  900:1553 */         int p = this.parallelism;
/*  901:1554 */         int n = p > 1 ? p - 1 : 1;
/*  902:1555 */         n |= n >>> 1;n |= n >>> 2;n |= n >>> 4;
/*  903:1556 */         n |= n >>> 8;n |= n >>> 16;n = n + 1 << 1;
/*  904:1557 */         WorkQueue[] nws = ((ws = this.workQueues) == null) || (ws.length == 0) ? new WorkQueue[n] : null;
/*  905:1559 */         if ((((ps = this.plock) & 0x2) != 0) || (!U.compareAndSwapInt(this, PLOCK, , ps))) {
/*  906:1561 */           ps = acquirePlock();
/*  907:     */         }
/*  908:1562 */         if ((((ws = this.workQueues) == null) || (ws.length == 0)) && (nws != null)) {
/*  909:1563 */           this.workQueues = nws;
/*  910:     */         }
/*  911:1564 */         int nps = ps & 0x80000000 | ps + 2 & 0x7FFFFFFF;
/*  912:1565 */         if (!U.compareAndSwapInt(this, PLOCK, ps, nps)) {
/*  913:1566 */           releasePlock(nps);
/*  914:     */         }
/*  915:     */       }
/*  916:     */       else
/*  917:     */       {
/*  918:     */         int m;
/*  919:     */         int k;
/*  920:     */         WorkQueue q;
/*  921:1568 */         if ((q = ws[(k = r & m & 0x7E)]) != null)
/*  922:     */         {
/*  923:1569 */           if ((q.qlock == 0) && (U.compareAndSwapInt(q, QLOCK, 0, 1)))
/*  924:     */           {
/*  925:1570 */             ForkJoinTask<?>[] a = q.array;
/*  926:1571 */             int s = q.top;
/*  927:1572 */             boolean submitted = false;
/*  928:     */             try
/*  929:     */             {
/*  930:1574 */               if (((a != null) && (a.length > s + 1 - q.base)) || ((a = q.growArray()) != null))
/*  931:     */               {
/*  932:1576 */                 int j = ((a.length - 1 & s) << ASHIFT) + ABASE;
/*  933:1577 */                 U.putOrderedObject(a, j, task);
/*  934:1578 */                 q.top = (s + 1);
/*  935:1579 */                 submitted = true;
/*  936:     */               }
/*  937:     */             }
/*  938:     */             finally
/*  939:     */             {
/*  940:1582 */               q.qlock = 0;
/*  941:     */             }
/*  942:1584 */             if (submitted)
/*  943:     */             {
/*  944:1585 */               signalWork(ws, q);
/*  945:1586 */               return;
/*  946:     */             }
/*  947:     */           }
/*  948:1589 */           r = 0;
/*  949:     */         }
/*  950:1591 */         else if (((ps = this.plock) & 0x2) == 0)
/*  951:     */         {
/*  952:1592 */           q = new WorkQueue(this, null, -1, r);
/*  953:1593 */           q.poolIndex = ((short)k);
/*  954:1594 */           if ((((ps = this.plock) & 0x2) != 0) || (!U.compareAndSwapInt(this, PLOCK, , ps))) {
/*  955:1596 */             ps = acquirePlock();
/*  956:     */           }
/*  957:1597 */           if (((ws = this.workQueues) != null) && (k < ws.length) && (ws[k] == null)) {
/*  958:1598 */             ws[k] = q;
/*  959:     */           }
/*  960:1599 */           int nps = ps & 0x80000000 | ps + 2 & 0x7FFFFFFF;
/*  961:1600 */           if (!U.compareAndSwapInt(this, PLOCK, ps, nps)) {
/*  962:1601 */             releasePlock(nps);
/*  963:     */           }
/*  964:     */         }
/*  965:     */         else
/*  966:     */         {
/*  967:1604 */           r = 0;
/*  968:     */         }
/*  969:     */       }
/*  970:     */     }
/*  971:     */   }
/*  972:     */   
/*  973:     */   final void incrementActiveCount()
/*  974:     */   {
/*  975:     */     long c;
/*  976:1615 */     while (!U.compareAndSwapLong(this, CTL, c = this.ctl, c & 0xFFFFFFFF | (c & 0x0) + 281474976710656L)) {}
/*  977:     */   }
/*  978:     */   
/*  979:     */   final void signalWork(WorkQueue[] ws, WorkQueue q)
/*  980:     */   {
/*  981:     */     long c;
/*  982:     */     int u;
/*  983:1629 */     while ((u = (int)((c = this.ctl) >>> 32)) < 0)
/*  984:     */     {
/*  985:     */       int e;
/*  986:1631 */       if ((e = (int)c) <= 0)
/*  987:     */       {
/*  988:1632 */         if ((short)u < 0) {
/*  989:1633 */           tryAddWorker();
/*  990:     */         }
/*  991:     */       }
/*  992:     */       else
/*  993:     */       {
/*  994:     */         int i;
/*  995:     */         WorkQueue w;
/*  996:1636 */         if ((ws != null) && (ws.length > (i = e & 0xFFFF)) && ((w = ws[i]) != null))
/*  997:     */         {
/*  998:1639 */           long nc = w.nextWait & 0x7FFFFFFF | u + 65536 << 32;
/*  999:     */           
/* 1000:1641 */           int ne = e + 65536 & 0x7FFFFFFF;
/* 1001:1642 */           if ((w.eventCount == (e | 0x80000000)) && (U.compareAndSwapLong(this, CTL, c, nc)))
/* 1002:     */           {
/* 1003:1644 */             w.eventCount = ne;
/* 1004:     */             Thread p;
/* 1005:1645 */             if ((p = w.parker) != null) {
/* 1006:1646 */               U.unpark(p);
/* 1007:     */             }
/* 1008:     */           }
/* 1009:     */           else
/* 1010:     */           {
/* 1011:1649 */             if ((q != null) && (q.base >= q.top)) {
/* 1012:     */               break;
/* 1013:     */             }
/* 1014:     */           }
/* 1015:     */         }
/* 1016:     */       }
/* 1017:     */     }
/* 1018:     */   }
/* 1019:     */   
/* 1020:     */   final void runWorker(WorkQueue w)
/* 1021:     */   {
/* 1022:1660 */     w.growArray();
/* 1023:1661 */     for (int r = w.hint; scan(w, r) == 0; r ^= r << 5)
/* 1024:     */     {
/* 1025:1662 */       r ^= r << 13;r ^= r >>> 17;
/* 1026:     */     }
/* 1027:     */   }
/* 1028:     */   
/* 1029:     */   private final int scan(WorkQueue w, int r)
/* 1030:     */   {
/* 1031:1690 */     long c = this.ctl;
/* 1032:     */     WorkQueue[] ws;
/* 1033:     */     int m;
/* 1034:1691 */     if (((ws = this.workQueues) != null) && ((m = ws.length - 1) >= 0) && (w != null))
/* 1035:     */     {
/* 1036:1692 */       int j = m + m + 1;int ec = w.eventCount;
/* 1037:     */       for (;;)
/* 1038:     */       {
/* 1039:     */         WorkQueue q;
/* 1040:     */         int b;
/* 1041:     */         ForkJoinTask<?>[] a;
/* 1042:1694 */         if (((q = ws[(r - j & m)]) != null) && ((b = q.base) - q.top < 0) && ((a = q.array) != null))
/* 1043:     */         {
/* 1044:1696 */           long i = ((a.length - 1 & b) << ASHIFT) + ABASE;
/* 1045:     */           ForkJoinTask<?> t;
/* 1046:1697 */           if ((t = (ForkJoinTask)U.getObjectVolatile(a, i)) == null) {
/* 1047:     */             break;
/* 1048:     */           }
/* 1049:1699 */           if (ec < 0)
/* 1050:     */           {
/* 1051:1700 */             helpRelease(c, ws, w, q, b); break;
/* 1052:     */           }
/* 1053:1701 */           if ((q.base != b) || (!U.compareAndSwapObject(a, i, t, null))) {
/* 1054:     */             break;
/* 1055:     */           }
/* 1056:1703 */           U.putOrderedInt(q, QBASE, b + 1);
/* 1057:1704 */           if (b + 1 - q.top < 0) {
/* 1058:1705 */             signalWork(ws, q);
/* 1059:     */           }
/* 1060:1706 */           w.runTask(t); break;
/* 1061:     */         }
/* 1062:1711 */         j--;
/* 1063:1711 */         if (j < 0)
/* 1064:     */         {
/* 1065:     */           int e;
/* 1066:1712 */           if ((ec | (e = (int)c)) < 0) {
/* 1067:1713 */             return awaitWork(w, c, ec);
/* 1068:     */           }
/* 1069:1714 */           if (this.ctl != c) {
/* 1070:     */             break;
/* 1071:     */           }
/* 1072:1715 */           long nc = ec | c - 281474976710656L & 0x0;
/* 1073:1716 */           w.nextWait = e;
/* 1074:1717 */           w.eventCount = (ec | 0x80000000);
/* 1075:1718 */           if (!U.compareAndSwapLong(this, CTL, c, nc)) {
/* 1076:1719 */             w.eventCount = ec;
/* 1077:     */           }
/* 1078:1720 */           break;
/* 1079:     */         }
/* 1080:     */       }
/* 1081:     */     }
/* 1082:1725 */     return 0;
/* 1083:     */   }
/* 1084:     */   
/* 1085:     */   private final int awaitWork(WorkQueue w, long c, int ec)
/* 1086:     */   {
/* 1087:     */     int stat;
/* 1088:1744 */     if (((stat = w.qlock) >= 0) && (w.eventCount == ec) && (this.ctl == c) && (!Thread.interrupted()))
/* 1089:     */     {
/* 1090:1746 */       int e = (int)c;
/* 1091:1747 */       int u = (int)(c >>> 32);
/* 1092:1748 */       int d = (u >> 16) + this.parallelism;
/* 1093:1750 */       if ((e < 0) || ((d <= 0) && (tryTerminate(false, false))))
/* 1094:     */       {
/* 1095:1751 */         stat = w.qlock = -1;
/* 1096:     */       }
/* 1097:     */       else
/* 1098:     */       {
/* 1099:     */         int ns;
/* 1100:1752 */         if ((ns = w.nsteals) != 0)
/* 1101:     */         {
/* 1102:1754 */           w.nsteals = 0;
/* 1103:     */           long sc;
/* 1104:1755 */           while (!U.compareAndSwapLong(this, STEALCOUNT, sc = this.stealCount, sc + ns)) {}
/* 1105:     */         }
/* 1106:     */         else
/* 1107:     */         {
/* 1108:1759 */           long pc = (d > 0) || (ec != (e | 0x80000000)) ? 0L : w.nextWait & 0x7FFFFFFF | u + 65536 << 32;
/* 1109:     */           long deadline;
/* 1110:     */           long deadline;
/* 1111:     */           long parkTime;
/* 1112:1762 */           if (pc != 0L)
/* 1113:     */           {
/* 1114:1763 */             int dc = -(short)(int)(c >>> 32);
/* 1115:1764 */             long parkTime = dc < 0 ? 200000000L : (dc + 1) * 2000000000L;
/* 1116:     */             
/* 1117:1766 */             deadline = System.nanoTime() + parkTime - 2000000L;
/* 1118:     */           }
/* 1119:     */           else
/* 1120:     */           {
/* 1121:1769 */             parkTime = deadline = 0L;
/* 1122:     */           }
/* 1123:1770 */           if ((w.eventCount == ec) && (this.ctl == c))
/* 1124:     */           {
/* 1125:1771 */             Thread wt = Thread.currentThread();
/* 1126:1772 */             U.putObject(wt, PARKBLOCKER, this);
/* 1127:1773 */             w.parker = wt;
/* 1128:1774 */             if ((w.eventCount == ec) && (this.ctl == c)) {
/* 1129:1775 */               U.park(false, parkTime);
/* 1130:     */             }
/* 1131:1776 */             w.parker = null;
/* 1132:1777 */             U.putObject(wt, PARKBLOCKER, null);
/* 1133:1778 */             if ((parkTime != 0L) && (this.ctl == c) && (deadline - System.nanoTime() <= 0L) && (U.compareAndSwapLong(this, CTL, c, pc))) {
/* 1134:1781 */               stat = w.qlock = -1;
/* 1135:     */             }
/* 1136:     */           }
/* 1137:     */         }
/* 1138:     */       }
/* 1139:     */     }
/* 1140:1785 */     return stat;
/* 1141:     */   }
/* 1142:     */   
/* 1143:     */   private final void helpRelease(long c, WorkQueue[] ws, WorkQueue w, WorkQueue q, int b)
/* 1144:     */   {
/* 1145:     */     int e;
/* 1146:     */     int i;
/* 1147:     */     WorkQueue v;
/* 1148:1797 */     if ((w != null) && (w.eventCount < 0) && ((e = (int)c) > 0) && (ws != null) && (ws.length > (i = e & 0xFFFF)) && ((v = ws[i]) != null) && (this.ctl == c))
/* 1149:     */     {
/* 1150:1800 */       long nc = v.nextWait & 0x7FFFFFFF | (int)(c >>> 32) + 65536 << 32;
/* 1151:     */       
/* 1152:1802 */       int ne = e + 65536 & 0x7FFFFFFF;
/* 1153:1803 */       if ((q != null) && (q.base == b) && (w.eventCount < 0) && (v.eventCount == (e | 0x80000000)) && (U.compareAndSwapLong(this, CTL, c, nc)))
/* 1154:     */       {
/* 1155:1806 */         v.eventCount = ne;
/* 1156:     */         Thread p;
/* 1157:1807 */         if ((p = v.parker) != null) {
/* 1158:1808 */           U.unpark(p);
/* 1159:     */         }
/* 1160:     */       }
/* 1161:     */     }
/* 1162:     */   }
/* 1163:     */   
/* 1164:     */   private int tryHelpStealer(WorkQueue joiner, ForkJoinTask<?> task)
/* 1165:     */   {
/* 1166:1832 */     int stat = 0;int steps = 0;
/* 1167:1833 */     if ((task != null) && (joiner != null) && (joiner.base - joiner.top >= 0))
/* 1168:     */     {
/* 1169:     */       break label107;
/* 1170:     */       label25:
/* 1171:1836 */       ForkJoinTask<?> subtask = task;
/* 1172:1837 */       WorkQueue j = joiner;
/* 1173:     */       label471:
/* 1174:     */       label472:
/* 1175:     */       label474:
/* 1176:     */       for (;;)
/* 1177:     */       {
/* 1178:     */         int s;
/* 1179:1839 */         if ((s = task.status) < 0)
/* 1180:     */         {
/* 1181:1840 */           stat = s;
/* 1182:1841 */           return stat;
/* 1183:     */         }
/* 1184:     */         WorkQueue[] ws;
/* 1185:     */         int m;
/* 1186:1843 */         if (((ws = this.workQueues) == null) || ((m = ws.length - 1) <= 0)) {
/* 1187:     */           return stat;
/* 1188:     */         }
/* 1189:     */         int h;
/* 1190:     */         WorkQueue v;
/* 1191:1845 */         if (((v = ws[(h = (j.hint | 0x1) & m)]) == null) || (v.currentSteal != subtask))
/* 1192:     */         {
/* 1193:1847 */           int origin = h;
/* 1194:     */           label107:
/* 1195:1848 */           if ((((h = h + 2 & m) & 0xF) == 1) && ((subtask.status < 0) || (j.currentJoin != subtask))) {
/* 1196:     */             break label25;
/* 1197:     */           }
/* 1198:1851 */           if (((v = ws[h]) != null) && (v.currentSteal == subtask))
/* 1199:     */           {
/* 1200:1853 */             j.hint = h;
/* 1201:     */           }
/* 1202:     */           else
/* 1203:     */           {
/* 1204:1856 */             if (h != origin) {
/* 1205:     */               break;
/* 1206:     */             }
/* 1207:1857 */             return stat;
/* 1208:     */           }
/* 1209:     */         }
/* 1210:     */         for (;;)
/* 1211:     */         {
/* 1212:1862 */           if (subtask.status < 0) {
/* 1213:     */             break label472;
/* 1214:     */           }
/* 1215:     */           int b;
/* 1216:     */           ForkJoinTask[] a;
/* 1217:1864 */           if (((b = v.base) - v.top < 0) && ((a = v.array) != null))
/* 1218:     */           {
/* 1219:1865 */             int i = ((a.length - 1 & b) << ASHIFT) + ABASE;
/* 1220:1866 */             ForkJoinTask<?> t = (ForkJoinTask)U.getObjectVolatile(a, i);
/* 1221:1868 */             if ((subtask.status < 0) || (j.currentJoin != subtask) || (v.currentSteal != subtask)) {
/* 1222:     */               break;
/* 1223:     */             }
/* 1224:1871 */             stat = 1;
/* 1225:1872 */             if (v.base == b)
/* 1226:     */             {
/* 1227:1873 */               if (t == null) {
/* 1228:     */                 return stat;
/* 1229:     */               }
/* 1230:1875 */               if (U.compareAndSwapObject(a, i, t, null))
/* 1231:     */               {
/* 1232:1876 */                 U.putOrderedInt(v, QBASE, b + 1);
/* 1233:1877 */                 ForkJoinTask<?> ps = joiner.currentSteal;
/* 1234:1878 */                 int jt = joiner.top;
/* 1235:     */                 do
/* 1236:     */                 {
/* 1237:1880 */                   joiner.currentSteal = t;
/* 1238:1881 */                   t.doExec();
/* 1239:1883 */                 } while ((task.status >= 0) && (joiner.top != jt) && ((t = joiner.pop()) != null));
/* 1240:1885 */                 joiner.currentSteal = ps;
/* 1241:1886 */                 return stat;
/* 1242:     */               }
/* 1243:     */             }
/* 1244:     */             break label471;
/* 1245:     */           }
/* 1246:1891 */           ForkJoinTask<?> next = v.currentJoin;
/* 1247:1892 */           if ((subtask.status < 0) || (j.currentJoin != subtask) || (v.currentSteal != subtask)) {
/* 1248:     */             break;
/* 1249:     */           }
/* 1250:1895 */           if (next == null) {
/* 1251:     */             return stat;
/* 1252:     */           }
/* 1253:1895 */           steps++;
/* 1254:1895 */           if (steps == 64) {
/* 1255:     */             return stat;
/* 1256:     */           }
/* 1257:1898 */           subtask = next;
/* 1258:1899 */           j = v;
/* 1259:     */           break label474;
/* 1260:     */         }
/* 1261:     */         break label25;
/* 1262:     */       }
/* 1263:     */     }
/* 1264:1907 */     return stat;
/* 1265:     */   }
/* 1266:     */   
/* 1267:     */   private int helpComplete(WorkQueue joiner, CountedCompleter<?> task)
/* 1268:     */   {
/* 1269:1918 */     int s = 0;
/* 1270:     */     WorkQueue[] ws;
/* 1271:     */     int m;
/* 1272:1919 */     if (((ws = this.workQueues) != null) && ((m = ws.length - 1) >= 0) && (joiner != null) && (task != null))
/* 1273:     */     {
/* 1274:1921 */       int j = joiner.poolIndex;
/* 1275:1922 */       int scans = m + m + 1;
/* 1276:1923 */       long c = 0L;
/* 1277:1924 */       int k = scans;
/* 1278:1926 */       for (; (s = task.status) >= 0; j += 2) {
/* 1279:1928 */         if (joiner.internalPopAndExecCC(task))
/* 1280:     */         {
/* 1281:1929 */           k = scans;
/* 1282:     */         }
/* 1283:     */         else
/* 1284:     */         {
/* 1285:1930 */           if ((s = task.status) < 0) {
/* 1286:     */             break;
/* 1287:     */           }
/* 1288:     */           WorkQueue q;
/* 1289:1932 */           if (((q = ws[(j & m)]) != null) && (q.pollAndExecCC(task)))
/* 1290:     */           {
/* 1291:1933 */             k = scans;
/* 1292:     */           }
/* 1293:     */           else
/* 1294:     */           {
/* 1295:1934 */             k--;
/* 1296:1934 */             if (k < 0)
/* 1297:     */             {
/* 1298:1935 */               if (c == (c = this.ctl)) {
/* 1299:     */                 break;
/* 1300:     */               }
/* 1301:1937 */               k = scans;
/* 1302:     */             }
/* 1303:     */           }
/* 1304:     */         }
/* 1305:     */       }
/* 1306:     */     }
/* 1307:1941 */     return s;
/* 1308:     */   }
/* 1309:     */   
/* 1310:     */   final boolean tryCompensate(long c)
/* 1311:     */   {
/* 1312:1954 */     WorkQueue[] ws = this.workQueues;
/* 1313:1955 */     int pc = this.parallelism;int e = (int)c;
/* 1314:     */     int m;
/* 1315:1956 */     if ((ws != null) && ((m = ws.length - 1) >= 0) && (e >= 0) && (this.ctl == c))
/* 1316:     */     {
/* 1317:1957 */       WorkQueue w = ws[(e & m)];
/* 1318:1958 */       if ((e != 0) && (w != null))
/* 1319:     */       {
/* 1320:1960 */         long nc = w.nextWait & 0x7FFFFFFF | c & 0x0;
/* 1321:     */         
/* 1322:1962 */         int ne = e + 65536 & 0x7FFFFFFF;
/* 1323:1963 */         if ((w.eventCount == (e | 0x80000000)) && (U.compareAndSwapLong(this, CTL, c, nc)))
/* 1324:     */         {
/* 1325:1965 */           w.eventCount = ne;
/* 1326:     */           Thread p;
/* 1327:1966 */           if ((p = w.parker) != null) {
/* 1328:1967 */             U.unpark(p);
/* 1329:     */           }
/* 1330:1968 */           return true;
/* 1331:     */         }
/* 1332:     */       }
/* 1333:     */       else
/* 1334:     */       {
/* 1335:     */         int tc;
/* 1336:1971 */         if (((tc = (short)(int)(c >>> 32)) >= 0) && ((int)(c >> 48) + pc > 1))
/* 1337:     */         {
/* 1338:1973 */           long nc = c - 281474976710656L & 0x0 | c & 0xFFFFFFFF;
/* 1339:1974 */           if (U.compareAndSwapLong(this, CTL, c, nc)) {
/* 1340:1975 */             return true;
/* 1341:     */           }
/* 1342:     */         }
/* 1343:1977 */         else if (tc + pc < 32767)
/* 1344:     */         {
/* 1345:1978 */           long nc = c + 4294967296L & 0x0 | c & 0xFFFFFFFF;
/* 1346:1979 */           if (U.compareAndSwapLong(this, CTL, c, nc))
/* 1347:     */           {
/* 1348:1981 */             Throwable ex = null;
/* 1349:1982 */             ForkJoinWorkerThread wt = null;
/* 1350:     */             try
/* 1351:     */             {
/* 1352:     */               ForkJoinWorkerThreadFactory fac;
/* 1353:1984 */               if (((fac = this.factory) != null) && ((wt = fac.newThread(this)) != null))
/* 1354:     */               {
/* 1355:1986 */                 wt.start();
/* 1356:1987 */                 return true;
/* 1357:     */               }
/* 1358:     */             }
/* 1359:     */             catch (Throwable rex)
/* 1360:     */             {
/* 1361:1990 */               ex = rex;
/* 1362:     */             }
/* 1363:1992 */             deregisterWorker(wt, ex);
/* 1364:     */           }
/* 1365:     */         }
/* 1366:     */       }
/* 1367:     */     }
/* 1368:1996 */     return false;
/* 1369:     */   }
/* 1370:     */   
/* 1371:     */   final int awaitJoin(WorkQueue joiner, ForkJoinTask<?> task)
/* 1372:     */   {
/* 1373:2007 */     int s = 0;
/* 1374:2008 */     if ((task != null) && ((s = task.status) >= 0) && (joiner != null))
/* 1375:     */     {
/* 1376:2009 */       ForkJoinTask<?> prevJoin = joiner.currentJoin;
/* 1377:2010 */       joiner.currentJoin = task;
/* 1378:2011 */       while ((joiner.tryRemoveAndExec(task)) && ((s = task.status) >= 0)) {}
/* 1379:2013 */       if ((s >= 0) && ((task instanceof CountedCompleter))) {
/* 1380:2014 */         s = helpComplete(joiner, (CountedCompleter)task);
/* 1381:     */       }
/* 1382:2015 */       long cc = 0L;
/* 1383:2016 */       while ((s >= 0) && ((s = task.status) >= 0)) {
/* 1384:2017 */         if (((s = tryHelpStealer(joiner, task)) == 0) && ((s = task.status) >= 0)) {
/* 1385:2019 */           if (!tryCompensate(cc))
/* 1386:     */           {
/* 1387:2020 */             cc = this.ctl;
/* 1388:     */           }
/* 1389:     */           else
/* 1390:     */           {
/* 1391:2022 */             if ((task.trySetSignal()) && ((s = task.status) >= 0)) {
/* 1392:2023 */               synchronized (task)
/* 1393:     */               {
/* 1394:2024 */                 if (task.status >= 0) {
/* 1395:     */                   try
/* 1396:     */                   {
/* 1397:2026 */                     task.wait();
/* 1398:     */                   }
/* 1399:     */                   catch (InterruptedException ie) {}
/* 1400:     */                 } else {
/* 1401:2031 */                   task.notifyAll();
/* 1402:     */                 }
/* 1403:     */               }
/* 1404:     */             }
/* 1405:     */             long c;
/* 1406:2035 */             while (!U.compareAndSwapLong(this, CTL, c = this.ctl, c & 0xFFFFFFFF | (c & 0x0) + 281474976710656L)) {}
/* 1407:     */           }
/* 1408:     */         }
/* 1409:     */       }
/* 1410:2042 */       joiner.currentJoin = prevJoin;
/* 1411:     */     }
/* 1412:2044 */     return s;
/* 1413:     */   }
/* 1414:     */   
/* 1415:     */   final void helpJoinOnce(WorkQueue joiner, ForkJoinTask<?> task)
/* 1416:     */   {
/* 1417:     */     int s;
/* 1418:2057 */     if ((joiner != null) && (task != null) && ((s = task.status) >= 0))
/* 1419:     */     {
/* 1420:2058 */       ForkJoinTask<?> prevJoin = joiner.currentJoin;
/* 1421:2059 */       joiner.currentJoin = task;
/* 1422:2060 */       while ((joiner.tryRemoveAndExec(task)) && ((s = task.status) >= 0)) {}
/* 1423:2062 */       if (s >= 0)
/* 1424:     */       {
/* 1425:2063 */         if ((task instanceof CountedCompleter)) {
/* 1426:2064 */           helpComplete(joiner, (CountedCompleter)task);
/* 1427:     */         }
/* 1428:2065 */         while ((task.status >= 0) && (tryHelpStealer(joiner, task) > 0)) {}
/* 1429:     */       }
/* 1430:2068 */       joiner.currentJoin = prevJoin;
/* 1431:     */     }
/* 1432:     */   }
/* 1433:     */   
/* 1434:     */   private WorkQueue findNonEmptyStealQueue()
/* 1435:     */   {
/* 1436:2078 */     int r = ThreadLocalRandom.current().nextInt();
/* 1437:     */     for (;;)
/* 1438:     */     {
/* 1439:2080 */       int ps = this.plock;
/* 1440:     */       WorkQueue[] ws;
/* 1441:     */       int m;
/* 1442:2081 */       if (((ws = this.workQueues) != null) && ((m = ws.length - 1) >= 0)) {
/* 1443:2082 */         for (int j = m + 1 << 2; j >= 0; j--)
/* 1444:     */         {
/* 1445:     */           WorkQueue q;
/* 1446:2083 */           if (((q = ws[((r - j << 1 | 0x1) & m)]) != null) && (q.base - q.top < 0)) {
/* 1447:2085 */             return q;
/* 1448:     */           }
/* 1449:     */         }
/* 1450:     */       }
/* 1451:2088 */       if (this.plock == ps) {
/* 1452:2089 */         return null;
/* 1453:     */       }
/* 1454:     */     }
/* 1455:     */   }
/* 1456:     */   
/* 1457:     */   final void helpQuiescePool(WorkQueue w)
/* 1458:     */   {
/* 1459:2100 */     ForkJoinTask<?> ps = w.currentSteal;
/* 1460:2101 */     boolean active = true;
/* 1461:     */     for (;;)
/* 1462:     */     {
/* 1463:     */       ForkJoinTask<?> t;
/* 1464:2103 */       if ((t = w.nextLocalTask()) != null)
/* 1465:     */       {
/* 1466:2104 */         t.doExec();
/* 1467:     */       }
/* 1468:     */       else
/* 1469:     */       {
/* 1470:     */         WorkQueue q;
/* 1471:2105 */         if ((q = findNonEmptyStealQueue()) != null)
/* 1472:     */         {
/* 1473:2106 */           if (!active)
/* 1474:     */           {
/* 1475:2107 */             active = true;
/* 1476:     */             long c;
/* 1477:2108 */             while (!U.compareAndSwapLong(this, CTL, c = this.ctl, c & 0xFFFFFFFF | (c & 0x0) + 281474976710656L)) {}
/* 1478:     */           }
/* 1479:     */           int b;
/* 1480:2113 */           if (((b = q.base) - q.top < 0) && ((t = q.pollAt(b)) != null))
/* 1481:     */           {
/* 1482:2114 */             (w.currentSteal = t).doExec();
/* 1483:2115 */             w.currentSteal = ps;
/* 1484:     */           }
/* 1485:     */         }
/* 1486:2118 */         else if (active)
/* 1487:     */         {
/* 1488:     */           long c;
/* 1489:2119 */           long nc = (c = this.ctl) & 0xFFFFFFFF | (c & 0x0) - 281474976710656L;
/* 1490:2120 */           if ((int)(nc >> 48) + this.parallelism == 0) {
/* 1491:     */             break;
/* 1492:     */           }
/* 1493:2122 */           if (U.compareAndSwapLong(this, CTL, c, nc)) {
/* 1494:2123 */             active = false;
/* 1495:     */           }
/* 1496:     */         }
/* 1497:     */         else
/* 1498:     */         {
/* 1499:     */           long c;
/* 1500:2125 */           if (((int)((c = this.ctl) >> 48) + this.parallelism <= 0) && (U.compareAndSwapLong(this, CTL, c, c & 0xFFFFFFFF | (c & 0x0) + 281474976710656L))) {
/* 1501:     */             break;
/* 1502:     */           }
/* 1503:     */         }
/* 1504:     */       }
/* 1505:     */     }
/* 1506:     */   }
/* 1507:     */   
/* 1508:     */   final ForkJoinTask<?> nextTaskFor(WorkQueue w)
/* 1509:     */   {
/* 1510:     */     for (;;)
/* 1511:     */     {
/* 1512:     */       ForkJoinTask<?> t;
/* 1513:2141 */       if ((t = w.nextLocalTask()) != null) {
/* 1514:2142 */         return t;
/* 1515:     */       }
/* 1516:     */       WorkQueue q;
/* 1517:2143 */       if ((q = findNonEmptyStealQueue()) == null) {
/* 1518:2144 */         return null;
/* 1519:     */       }
/* 1520:     */       int b;
/* 1521:2145 */       if (((b = q.base) - q.top < 0) && ((t = q.pollAt(b)) != null)) {
/* 1522:2146 */         return t;
/* 1523:     */       }
/* 1524:     */     }
/* 1525:     */   }
/* 1526:     */   
/* 1527:     */   static int getSurplusQueuedTaskCount()
/* 1528:     */   {
/* 1529:     */     Thread t;
/* 1530:2198 */     if (((t = Thread.currentThread()) instanceof ForkJoinWorkerThread))
/* 1531:     */     {
/* 1532:     */       ForkJoinWorkerThread wt;
/* 1533:     */       ForkJoinPool pool;
/* 1534:2199 */       int p = (pool = (wt = (ForkJoinWorkerThread)t).pool).parallelism;
/* 1535:     */       WorkQueue q;
/* 1536:2200 */       int n = (q = wt.workQueue).top - q.base;
/* 1537:2201 */       int a = (int)(pool.ctl >> 48) + p;
/* 1538:2202 */       return n - (a > p >>>= 1 ? 4 : a > p >>>= 1 ? 2 : a > p >>>= 1 ? 1 : a > p >>>= 1 ? 0 : 8);
/* 1539:     */     }
/* 1540:2208 */     return 0;
/* 1541:     */   }
/* 1542:     */   
/* 1543:     */   private boolean tryTerminate(boolean now, boolean enable)
/* 1544:     */   {
/* 1545:2229 */     if (this == common) {
/* 1546:2230 */       return false;
/* 1547:     */     }
/* 1548:     */     int ps;
/* 1549:2231 */     if ((ps = this.plock) >= 0)
/* 1550:     */     {
/* 1551:2232 */       if (!enable) {
/* 1552:2233 */         return false;
/* 1553:     */       }
/* 1554:2234 */       if (((ps & 0x2) != 0) || (!U.compareAndSwapInt(this, PLOCK, , ps))) {
/* 1555:2236 */         ps = acquirePlock();
/* 1556:     */       }
/* 1557:2237 */       int nps = ps + 2 & 0x7FFFFFFF | 0x80000000;
/* 1558:2238 */       if (!U.compareAndSwapInt(this, PLOCK, ps, nps)) {
/* 1559:2239 */         releasePlock(nps);
/* 1560:     */       }
/* 1561:     */     }
/* 1562:     */     for (;;)
/* 1563:     */     {
/* 1564:     */       long c;
/* 1565:2242 */       if (((c = this.ctl) & 0x80000000) != 0L)
/* 1566:     */       {
/* 1567:2243 */         if ((short)(int)(c >>> 32) + this.parallelism <= 0) {
/* 1568:2244 */           synchronized (this)
/* 1569:     */           {
/* 1570:2245 */             notifyAll();
/* 1571:     */           }
/* 1572:     */         }
/* 1573:2248 */         return true;
/* 1574:     */       }
/* 1575:2250 */       if (!now)
/* 1576:     */       {
/* 1577:2252 */         if ((int)(c >> 48) + this.parallelism > 0) {
/* 1578:2253 */           return false;
/* 1579:     */         }
/* 1580:     */         WorkQueue[] ws;
/* 1581:2254 */         if ((ws = this.workQueues) != null) {
/* 1582:2255 */           for (int i = 0; i < ws.length; i++)
/* 1583:     */           {
/* 1584:     */             WorkQueue w;
/* 1585:2256 */             if (((w = ws[i]) != null) && ((!w.isEmpty()) || (((i & 0x1) != 0) && (w.eventCount >= 0))))
/* 1586:     */             {
/* 1587:2259 */               signalWork(ws, w);
/* 1588:2260 */               return false;
/* 1589:     */             }
/* 1590:     */           }
/* 1591:     */         }
/* 1592:     */       }
/* 1593:2265 */       if (U.compareAndSwapLong(this, CTL, c, c | 0x80000000)) {
/* 1594:2266 */         for (int pass = 0; pass < 3; pass++)
/* 1595:     */         {
/* 1596:     */           WorkQueue[] ws;
/* 1597:2268 */           if ((ws = this.workQueues) != null)
/* 1598:     */           {
/* 1599:2269 */             int n = ws.length;
/* 1600:2270 */             for (int i = 0; i < n; i++)
/* 1601:     */             {
/* 1602:     */               WorkQueue w;
/* 1603:2271 */               if ((w = ws[i]) != null)
/* 1604:     */               {
/* 1605:2272 */                 w.qlock = -1;
/* 1606:2273 */                 if (pass > 0)
/* 1607:     */                 {
/* 1608:2274 */                   w.cancelAll();
/* 1609:     */                   Thread wt;
/* 1610:2275 */                   if ((pass > 1) && ((wt = w.owner) != null))
/* 1611:     */                   {
/* 1612:2276 */                     if (!wt.isInterrupted()) {
/* 1613:     */                       try
/* 1614:     */                       {
/* 1615:2278 */                         wt.interrupt();
/* 1616:     */                       }
/* 1617:     */                       catch (Throwable ignore) {}
/* 1618:     */                     }
/* 1619:2282 */                     U.unpark(wt);
/* 1620:     */                   }
/* 1621:     */                 }
/* 1622:     */               }
/* 1623:     */             }
/* 1624:     */             long cc;
/* 1625:     */             int e;
/* 1626:     */             int i;
/* 1627:     */             WorkQueue w;
/* 1628:2290 */             while (((e = (int)(cc = this.ctl) & 0x7FFFFFFF) != 0) && ((i = e & 0xFFFF) < n) && (i >= 0) && ((w = ws[i]) != null))
/* 1629:     */             {
/* 1630:2292 */               long nc = w.nextWait & 0x7FFFFFFF | cc + 281474976710656L & 0x0 | cc & 0x80000000;
/* 1631:2295 */               if ((w.eventCount == (e | 0x80000000)) && (U.compareAndSwapLong(this, CTL, cc, nc)))
/* 1632:     */               {
/* 1633:2297 */                 w.eventCount = (e + 65536 & 0x7FFFFFFF);
/* 1634:2298 */                 w.qlock = -1;
/* 1635:     */                 Thread p;
/* 1636:2299 */                 if ((p = w.parker) != null) {
/* 1637:2300 */                   U.unpark(p);
/* 1638:     */                 }
/* 1639:     */               }
/* 1640:     */             }
/* 1641:     */           }
/* 1642:     */         }
/* 1643:     */       }
/* 1644:     */     }
/* 1645:     */   }
/* 1646:     */   
/* 1647:     */   static WorkQueue commonSubmitterQueue()
/* 1648:     */   {
/* 1649:     */     Submitter z;
/* 1650:     */     ForkJoinPool p;
/* 1651:     */     WorkQueue[] ws;
/* 1652:     */     int m;
/* 1653:2317 */     return ((z = (Submitter)submitters.get()) != null) && ((p = common) != null) && ((ws = p.workQueues) != null) && ((m = ws.length - 1) >= 0) ? ws[(m & z.seed & 0x7E)] : null;
/* 1654:     */   }
/* 1655:     */   
/* 1656:     */   final boolean tryExternalUnpush(ForkJoinTask<?> task)
/* 1657:     */   {
/* 1658:2329 */     Submitter z = (Submitter)submitters.get();
/* 1659:2330 */     WorkQueue[] ws = this.workQueues;
/* 1660:2331 */     boolean popped = false;
/* 1661:     */     int m;
/* 1662:     */     WorkQueue joiner;
/* 1663:     */     int s;
/* 1664:     */     ForkJoinTask<?>[] a;
/* 1665:2332 */     if ((z != null) && (ws != null) && ((m = ws.length - 1) >= 0) && ((joiner = ws[(z.seed & m & 0x7E)]) != null) && (joiner.base != (s = joiner.top)) && ((a = joiner.array) != null))
/* 1666:     */     {
/* 1667:2336 */       long j = ((a.length - 1 & s - 1) << ASHIFT) + ABASE;
/* 1668:2337 */       if ((U.getObject(a, j) == task) && (U.compareAndSwapInt(joiner, QLOCK, 0, 1)))
/* 1669:     */       {
/* 1670:2339 */         if ((joiner.top == s) && (joiner.array == a) && (U.compareAndSwapObject(a, j, task, null)))
/* 1671:     */         {
/* 1672:2341 */           joiner.top = (s - 1);
/* 1673:2342 */           popped = true;
/* 1674:     */         }
/* 1675:2344 */         joiner.qlock = 0;
/* 1676:     */       }
/* 1677:     */     }
/* 1678:2347 */     return popped;
/* 1679:     */   }
/* 1680:     */   
/* 1681:     */   final int externalHelpComplete(CountedCompleter<?> task)
/* 1682:     */   {
/* 1683:2352 */     Submitter z = (Submitter)submitters.get();
/* 1684:2353 */     WorkQueue[] ws = this.workQueues;
/* 1685:2354 */     int s = 0;
/* 1686:     */     int m;
/* 1687:     */     int j;
/* 1688:     */     WorkQueue joiner;
/* 1689:2355 */     if ((z != null) && (ws != null) && ((m = ws.length - 1) >= 0) && ((joiner = ws[((j = z.seed) & m & 0x7E)]) != null) && (task != null))
/* 1690:     */     {
/* 1691:2357 */       int scans = m + m + 1;
/* 1692:2358 */       long c = 0L;
/* 1693:2359 */       j |= 0x1;
/* 1694:2360 */       int k = scans;
/* 1695:2362 */       for (; (s = task.status) >= 0; j += 2) {
/* 1696:2364 */         if (joiner.externalPopAndExecCC(task))
/* 1697:     */         {
/* 1698:2365 */           k = scans;
/* 1699:     */         }
/* 1700:     */         else
/* 1701:     */         {
/* 1702:2366 */           if ((s = task.status) < 0) {
/* 1703:     */             break;
/* 1704:     */           }
/* 1705:     */           WorkQueue q;
/* 1706:2368 */           if (((q = ws[(j & m)]) != null) && (q.pollAndExecCC(task)))
/* 1707:     */           {
/* 1708:2369 */             k = scans;
/* 1709:     */           }
/* 1710:     */           else
/* 1711:     */           {
/* 1712:2370 */             k--;
/* 1713:2370 */             if (k < 0)
/* 1714:     */             {
/* 1715:2371 */               if (c == (c = this.ctl)) {
/* 1716:     */                 break;
/* 1717:     */               }
/* 1718:2373 */               k = scans;
/* 1719:     */             }
/* 1720:     */           }
/* 1721:     */         }
/* 1722:     */       }
/* 1723:     */     }
/* 1724:2377 */     return s;
/* 1725:     */   }
/* 1726:     */   
/* 1727:     */   public ForkJoinPool()
/* 1728:     */   {
/* 1729:2396 */     this(Math.min(32767, Runtime.getRuntime().availableProcessors()), defaultForkJoinWorkerThreadFactory, null, false);
/* 1730:     */   }
/* 1731:     */   
/* 1732:     */   public ForkJoinPool(int parallelism)
/* 1733:     */   {
/* 1734:2415 */     this(parallelism, defaultForkJoinWorkerThreadFactory, null, false);
/* 1735:     */   }
/* 1736:     */   
/* 1737:     */   public ForkJoinPool(int parallelism, ForkJoinWorkerThreadFactory factory, Thread.UncaughtExceptionHandler handler, boolean asyncMode)
/* 1738:     */   {
/* 1739:2446 */     this(checkParallelism(parallelism), checkFactory(factory), handler, asyncMode ? 1 : 0, "ForkJoinPool-" + nextPoolId() + "-worker-");
/* 1740:     */     
/* 1741:     */ 
/* 1742:     */ 
/* 1743:     */ 
/* 1744:2451 */     checkPermission();
/* 1745:     */   }
/* 1746:     */   
/* 1747:     */   private static int checkParallelism(int parallelism)
/* 1748:     */   {
/* 1749:2455 */     if ((parallelism <= 0) || (parallelism > 32767)) {
/* 1750:2456 */       throw new IllegalArgumentException();
/* 1751:     */     }
/* 1752:2457 */     return parallelism;
/* 1753:     */   }
/* 1754:     */   
/* 1755:     */   private static ForkJoinWorkerThreadFactory checkFactory(ForkJoinWorkerThreadFactory factory)
/* 1756:     */   {
/* 1757:2462 */     if (factory == null) {
/* 1758:2463 */       throw new NullPointerException();
/* 1759:     */     }
/* 1760:2464 */     return factory;
/* 1761:     */   }
/* 1762:     */   
/* 1763:     */   private ForkJoinPool(int parallelism, ForkJoinWorkerThreadFactory factory, Thread.UncaughtExceptionHandler handler, int mode, String workerNamePrefix)
/* 1764:     */   {
/* 1765:2477 */     this.workerNamePrefix = workerNamePrefix;
/* 1766:2478 */     this.factory = factory;
/* 1767:2479 */     this.ueh = handler;
/* 1768:2480 */     this.mode = ((short)mode);
/* 1769:2481 */     this.parallelism = ((short)parallelism);
/* 1770:2482 */     long np = -parallelism;
/* 1771:2483 */     this.ctl = (np << 48 & 0x0 | np << 32 & 0x0);
/* 1772:     */   }
/* 1773:     */   
/* 1774:     */   public static ForkJoinPool commonPool()
/* 1775:     */   {
/* 1776:2501 */     return common;
/* 1777:     */   }
/* 1778:     */   
/* 1779:     */   public <T> T invoke(ForkJoinTask<T> task)
/* 1780:     */   {
/* 1781:2523 */     if (task == null) {
/* 1782:2524 */       throw new NullPointerException();
/* 1783:     */     }
/* 1784:2525 */     externalPush(task);
/* 1785:2526 */     return task.join();
/* 1786:     */   }
/* 1787:     */   
/* 1788:     */   public void execute(ForkJoinTask<?> task)
/* 1789:     */   {
/* 1790:2538 */     if (task == null) {
/* 1791:2539 */       throw new NullPointerException();
/* 1792:     */     }
/* 1793:2540 */     externalPush(task);
/* 1794:     */   }
/* 1795:     */   
/* 1796:     */   public void execute(Runnable task)
/* 1797:     */   {
/* 1798:2551 */     if (task == null) {
/* 1799:2552 */       throw new NullPointerException();
/* 1800:     */     }
/* 1801:     */     ForkJoinTask<?> job;
/* 1802:     */     ForkJoinTask<?> job;
/* 1803:2554 */     if ((task instanceof ForkJoinTask)) {
/* 1804:2555 */       job = (ForkJoinTask)task;
/* 1805:     */     } else {
/* 1806:2557 */       job = new ForkJoinTask.RunnableExecuteAction(task);
/* 1807:     */     }
/* 1808:2558 */     externalPush(job);
/* 1809:     */   }
/* 1810:     */   
/* 1811:     */   public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task)
/* 1812:     */   {
/* 1813:2571 */     if (task == null) {
/* 1814:2572 */       throw new NullPointerException();
/* 1815:     */     }
/* 1816:2573 */     externalPush(task);
/* 1817:2574 */     return task;
/* 1818:     */   }
/* 1819:     */   
/* 1820:     */   public <T> ForkJoinTask<T> submit(Callable<T> task)
/* 1821:     */   {
/* 1822:2583 */     ForkJoinTask<T> job = new ForkJoinTask.AdaptedCallable(task);
/* 1823:2584 */     externalPush(job);
/* 1824:2585 */     return job;
/* 1825:     */   }
/* 1826:     */   
/* 1827:     */   public <T> ForkJoinTask<T> submit(Runnable task, T result)
/* 1828:     */   {
/* 1829:2594 */     ForkJoinTask<T> job = new ForkJoinTask.AdaptedRunnable(task, result);
/* 1830:2595 */     externalPush(job);
/* 1831:2596 */     return job;
/* 1832:     */   }
/* 1833:     */   
/* 1834:     */   public ForkJoinTask<?> submit(Runnable task)
/* 1835:     */   {
/* 1836:2605 */     if (task == null) {
/* 1837:2606 */       throw new NullPointerException();
/* 1838:     */     }
/* 1839:     */     ForkJoinTask<?> job;
/* 1840:     */     ForkJoinTask<?> job;
/* 1841:2608 */     if ((task instanceof ForkJoinTask)) {
/* 1842:2609 */       job = (ForkJoinTask)task;
/* 1843:     */     } else {
/* 1844:2611 */       job = new ForkJoinTask.AdaptedRunnableAction(task);
/* 1845:     */     }
/* 1846:2612 */     externalPush(job);
/* 1847:2613 */     return job;
/* 1848:     */   }
/* 1849:     */   
/* 1850:     */   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
/* 1851:     */   {
/* 1852:2624 */     ArrayList<Future<T>> futures = new ArrayList(tasks.size());
/* 1853:     */     
/* 1854:2626 */     boolean done = false;
/* 1855:     */     try
/* 1856:     */     {
/* 1857:2628 */       for (Callable<T> t : tasks)
/* 1858:     */       {
/* 1859:2629 */         ForkJoinTask<T> f = new ForkJoinTask.AdaptedCallable(t);
/* 1860:2630 */         futures.add(f);
/* 1861:2631 */         externalPush(f);
/* 1862:     */       }
/* 1863:2633 */       int i = 0;
/* 1864:2633 */       for (int size = futures.size(); i < size; i++) {
/* 1865:2634 */         ((ForkJoinTask)futures.get(i)).quietlyJoin();
/* 1866:     */       }
/* 1867:2635 */       done = true;
/* 1868:     */       int i;
/* 1869:     */       int size;
/* 1870:2636 */       return futures;
/* 1871:     */     }
/* 1872:     */     finally
/* 1873:     */     {
/* 1874:2638 */       if (!done)
/* 1875:     */       {
/* 1876:2639 */         int i = 0;
/* 1877:2639 */         for (int size = futures.size(); i < size; i++) {
/* 1878:2640 */           ((Future)futures.get(i)).cancel(false);
/* 1879:     */         }
/* 1880:     */       }
/* 1881:     */     }
/* 1882:     */   }
/* 1883:     */   
/* 1884:     */   public ForkJoinWorkerThreadFactory getFactory()
/* 1885:     */   {
/* 1886:2650 */     return this.factory;
/* 1887:     */   }
/* 1888:     */   
/* 1889:     */   public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler()
/* 1890:     */   {
/* 1891:2660 */     return this.ueh;
/* 1892:     */   }
/* 1893:     */   
/* 1894:     */   public int getParallelism()
/* 1895:     */   {
/* 1896:     */     int par;
/* 1897:2670 */     return (par = this.parallelism) > 0 ? par : 1;
/* 1898:     */   }
/* 1899:     */   
/* 1900:     */   public static int getCommonPoolParallelism()
/* 1901:     */   {
/* 1902:2680 */     return commonParallelism;
/* 1903:     */   }
/* 1904:     */   
/* 1905:     */   public int getPoolSize()
/* 1906:     */   {
/* 1907:2692 */     return this.parallelism + (short)(int)(this.ctl >>> 32);
/* 1908:     */   }
/* 1909:     */   
/* 1910:     */   public boolean getAsyncMode()
/* 1911:     */   {
/* 1912:2702 */     return this.mode == 1;
/* 1913:     */   }
/* 1914:     */   
/* 1915:     */   public int getRunningThreadCount()
/* 1916:     */   {
/* 1917:2714 */     int rc = 0;
/* 1918:     */     WorkQueue[] ws;
/* 1919:2716 */     if ((ws = this.workQueues) != null) {
/* 1920:2717 */       for (int i = 1; i < ws.length; i += 2)
/* 1921:     */       {
/* 1922:     */         WorkQueue w;
/* 1923:2718 */         if (((w = ws[i]) != null) && (w.isApparentlyUnblocked())) {
/* 1924:2719 */           rc++;
/* 1925:     */         }
/* 1926:     */       }
/* 1927:     */     }
/* 1928:2722 */     return rc;
/* 1929:     */   }
/* 1930:     */   
/* 1931:     */   public int getActiveThreadCount()
/* 1932:     */   {
/* 1933:2733 */     int r = this.parallelism + (int)(this.ctl >> 48);
/* 1934:2734 */     return r <= 0 ? 0 : r;
/* 1935:     */   }
/* 1936:     */   
/* 1937:     */   public boolean isQuiescent()
/* 1938:     */   {
/* 1939:2749 */     return this.parallelism + (int)(this.ctl >> 48) <= 0;
/* 1940:     */   }
/* 1941:     */   
/* 1942:     */   public long getStealCount()
/* 1943:     */   {
/* 1944:2764 */     long count = this.stealCount;
/* 1945:     */     WorkQueue[] ws;
/* 1946:2766 */     if ((ws = this.workQueues) != null) {
/* 1947:2767 */       for (int i = 1; i < ws.length; i += 2)
/* 1948:     */       {
/* 1949:     */         WorkQueue w;
/* 1950:2768 */         if ((w = ws[i]) != null) {
/* 1951:2769 */           count += w.nsteals;
/* 1952:     */         }
/* 1953:     */       }
/* 1954:     */     }
/* 1955:2772 */     return count;
/* 1956:     */   }
/* 1957:     */   
/* 1958:     */   public long getQueuedTaskCount()
/* 1959:     */   {
/* 1960:2786 */     long count = 0L;
/* 1961:     */     WorkQueue[] ws;
/* 1962:2788 */     if ((ws = this.workQueues) != null) {
/* 1963:2789 */       for (int i = 1; i < ws.length; i += 2)
/* 1964:     */       {
/* 1965:     */         WorkQueue w;
/* 1966:2790 */         if ((w = ws[i]) != null) {
/* 1967:2791 */           count += w.queueSize();
/* 1968:     */         }
/* 1969:     */       }
/* 1970:     */     }
/* 1971:2794 */     return count;
/* 1972:     */   }
/* 1973:     */   
/* 1974:     */   public int getQueuedSubmissionCount()
/* 1975:     */   {
/* 1976:2805 */     int count = 0;
/* 1977:     */     WorkQueue[] ws;
/* 1978:2807 */     if ((ws = this.workQueues) != null) {
/* 1979:2808 */       for (int i = 0; i < ws.length; i += 2)
/* 1980:     */       {
/* 1981:     */         WorkQueue w;
/* 1982:2809 */         if ((w = ws[i]) != null) {
/* 1983:2810 */           count += w.queueSize();
/* 1984:     */         }
/* 1985:     */       }
/* 1986:     */     }
/* 1987:2813 */     return count;
/* 1988:     */   }
/* 1989:     */   
/* 1990:     */   public boolean hasQueuedSubmissions()
/* 1991:     */   {
/* 1992:     */     WorkQueue[] ws;
/* 1993:2824 */     if ((ws = this.workQueues) != null) {
/* 1994:2825 */       for (int i = 0; i < ws.length; i += 2)
/* 1995:     */       {
/* 1996:     */         WorkQueue w;
/* 1997:2826 */         if (((w = ws[i]) != null) && (!w.isEmpty())) {
/* 1998:2827 */           return true;
/* 1999:     */         }
/* 2000:     */       }
/* 2001:     */     }
/* 2002:2830 */     return false;
/* 2003:     */   }
/* 2004:     */   
/* 2005:     */   protected ForkJoinTask<?> pollSubmission()
/* 2006:     */   {
/* 2007:     */     WorkQueue[] ws;
/* 2008:2842 */     if ((ws = this.workQueues) != null) {
/* 2009:2843 */       for (int i = 0; i < ws.length; i += 2)
/* 2010:     */       {
/* 2011:     */         WorkQueue w;
/* 2012:     */         ForkJoinTask<?> t;
/* 2013:2844 */         if (((w = ws[i]) != null) && ((t = w.poll()) != null)) {
/* 2014:2845 */           return t;
/* 2015:     */         }
/* 2016:     */       }
/* 2017:     */     }
/* 2018:2848 */     return null;
/* 2019:     */   }
/* 2020:     */   
/* 2021:     */   protected int drainTasksTo(Collection<? super ForkJoinTask<?>> c)
/* 2022:     */   {
/* 2023:2869 */     int count = 0;
/* 2024:     */     WorkQueue[] ws;
/* 2025:2871 */     if ((ws = this.workQueues) != null) {
/* 2026:2872 */       for (int i = 0; i < ws.length; i++)
/* 2027:     */       {
/* 2028:     */         WorkQueue w;
/* 2029:2873 */         if ((w = ws[i]) != null)
/* 2030:     */         {
/* 2031:     */           ForkJoinTask<?> t;
/* 2032:2874 */           while ((t = w.poll()) != null)
/* 2033:     */           {
/* 2034:2875 */             c.add(t);
/* 2035:2876 */             count++;
/* 2036:     */           }
/* 2037:     */         }
/* 2038:     */       }
/* 2039:     */     }
/* 2040:2881 */     return count;
/* 2041:     */   }
/* 2042:     */   
/* 2043:     */   public String toString()
/* 2044:     */   {
/* 2045:2893 */     long qt = 0L;long qs = 0L;int rc = 0;
/* 2046:2894 */     long st = this.stealCount;
/* 2047:2895 */     long c = this.ctl;
/* 2048:     */     WorkQueue[] ws;
/* 2049:2897 */     if ((ws = this.workQueues) != null) {
/* 2050:2898 */       for (int i = 0; i < ws.length; i++)
/* 2051:     */       {
/* 2052:     */         WorkQueue w;
/* 2053:2899 */         if ((w = ws[i]) != null)
/* 2054:     */         {
/* 2055:2900 */           int size = w.queueSize();
/* 2056:2901 */           if ((i & 0x1) == 0)
/* 2057:     */           {
/* 2058:2902 */             qs += size;
/* 2059:     */           }
/* 2060:     */           else
/* 2061:     */           {
/* 2062:2904 */             qt += size;
/* 2063:2905 */             st += w.nsteals;
/* 2064:2906 */             if (w.isApparentlyUnblocked()) {
/* 2065:2907 */               rc++;
/* 2066:     */             }
/* 2067:     */           }
/* 2068:     */         }
/* 2069:     */       }
/* 2070:     */     }
/* 2071:2912 */     int pc = this.parallelism;
/* 2072:2913 */     int tc = pc + (short)(int)(c >>> 32);
/* 2073:2914 */     int ac = pc + (int)(c >> 48);
/* 2074:2915 */     if (ac < 0) {
/* 2075:2916 */       ac = 0;
/* 2076:     */     }
/* 2077:     */     String level;
/* 2078:     */     String level;
/* 2079:2918 */     if ((c & 0x80000000) != 0L) {
/* 2080:2919 */       level = tc == 0 ? "Terminated" : "Terminating";
/* 2081:     */     } else {
/* 2082:2921 */       level = this.plock < 0 ? "Shutting down" : "Running";
/* 2083:     */     }
/* 2084:2922 */     return super.toString() + "[" + level + ", parallelism = " + pc + ", size = " + tc + ", active = " + ac + ", running = " + rc + ", steals = " + st + ", tasks = " + qt + ", submissions = " + qs + "]";
/* 2085:     */   }
/* 2086:     */   
/* 2087:     */   public void shutdown()
/* 2088:     */   {
/* 2089:2949 */     checkPermission();
/* 2090:2950 */     tryTerminate(false, true);
/* 2091:     */   }
/* 2092:     */   
/* 2093:     */   public List<Runnable> shutdownNow()
/* 2094:     */   {
/* 2095:2972 */     checkPermission();
/* 2096:2973 */     tryTerminate(true, true);
/* 2097:2974 */     return Collections.emptyList();
/* 2098:     */   }
/* 2099:     */   
/* 2100:     */   public boolean isTerminated()
/* 2101:     */   {
/* 2102:2983 */     long c = this.ctl;
/* 2103:2984 */     return ((c & 0x80000000) != 0L) && ((short)(int)(c >>> 32) + this.parallelism <= 0);
/* 2104:     */   }
/* 2105:     */   
/* 2106:     */   public boolean isTerminating()
/* 2107:     */   {
/* 2108:3002 */     long c = this.ctl;
/* 2109:3003 */     return ((c & 0x80000000) != 0L) && ((short)(int)(c >>> 32) + this.parallelism > 0);
/* 2110:     */   }
/* 2111:     */   
/* 2112:     */   public boolean isShutdown()
/* 2113:     */   {
/* 2114:3013 */     return this.plock < 0;
/* 2115:     */   }
/* 2116:     */   
/* 2117:     */   public boolean awaitTermination(long timeout, TimeUnit unit)
/* 2118:     */     throws InterruptedException
/* 2119:     */   {
/* 2120:3032 */     if (Thread.interrupted()) {
/* 2121:3033 */       throw new InterruptedException();
/* 2122:     */     }
/* 2123:3034 */     if (this == common)
/* 2124:     */     {
/* 2125:3035 */       awaitQuiescence(timeout, unit);
/* 2126:3036 */       return false;
/* 2127:     */     }
/* 2128:3038 */     long nanos = unit.toNanos(timeout);
/* 2129:3039 */     if (isTerminated()) {
/* 2130:3040 */       return true;
/* 2131:     */     }
/* 2132:3041 */     if (nanos <= 0L) {
/* 2133:3042 */       return false;
/* 2134:     */     }
/* 2135:3043 */     long deadline = System.nanoTime() + nanos;
/* 2136:3044 */     synchronized (this)
/* 2137:     */     {
/* 2138:3046 */       if (isTerminated()) {
/* 2139:3047 */         return true;
/* 2140:     */       }
/* 2141:3048 */       if (nanos <= 0L) {
/* 2142:3049 */         return false;
/* 2143:     */       }
/* 2144:3050 */       long millis = TimeUnit.NANOSECONDS.toMillis(nanos);
/* 2145:3051 */       wait(millis > 0L ? millis : 1L);
/* 2146:3052 */       nanos = deadline - System.nanoTime();
/* 2147:     */     }
/* 2148:     */   }
/* 2149:     */   
/* 2150:     */   public boolean awaitQuiescence(long timeout, TimeUnit unit)
/* 2151:     */   {
/* 2152:3069 */     long nanos = unit.toNanos(timeout);
/* 2153:     */     
/* 2154:3071 */     Thread thread = Thread.currentThread();
/* 2155:     */     ForkJoinWorkerThread wt;
/* 2156:3072 */     if (((thread instanceof ForkJoinWorkerThread)) && ((wt = (ForkJoinWorkerThread)thread).pool == this))
/* 2157:     */     {
/* 2158:3074 */       helpQuiescePool(wt.workQueue);
/* 2159:3075 */       return true;
/* 2160:     */     }
/* 2161:3077 */     long startTime = System.nanoTime();
/* 2162:     */     
/* 2163:3079 */     int r = 0;
/* 2164:3080 */     boolean found = true;
/* 2165:     */     WorkQueue[] ws;
/* 2166:     */     int m;
/* 2167:3081 */     while ((!isQuiescent()) && ((ws = this.workQueues) != null) && ((m = ws.length - 1) >= 0))
/* 2168:     */     {
/* 2169:3083 */       if (!found)
/* 2170:     */       {
/* 2171:3084 */         if (System.nanoTime() - startTime > nanos) {
/* 2172:3085 */           return false;
/* 2173:     */         }
/* 2174:3086 */         Thread.yield();
/* 2175:     */       }
/* 2176:3088 */       found = false;
/* 2177:3089 */       for (int j = m + 1 << 2; j >= 0; j--)
/* 2178:     */       {
/* 2179:     */         WorkQueue q;
/* 2180:     */         int b;
/* 2181:3091 */         if (((q = ws[(r++ & m)]) != null) && ((b = q.base) - q.top < 0))
/* 2182:     */         {
/* 2183:3092 */           found = true;
/* 2184:     */           ForkJoinTask<?> t;
/* 2185:3093 */           if ((t = q.pollAt(b)) == null) {
/* 2186:     */             break;
/* 2187:     */           }
/* 2188:3094 */           t.doExec(); break;
/* 2189:     */         }
/* 2190:     */       }
/* 2191:     */     }
/* 2192:3099 */     return true;
/* 2193:     */   }
/* 2194:     */   
/* 2195:     */   static void quiesceCommonPool()
/* 2196:     */   {
/* 2197:3107 */     common.awaitQuiescence(9223372036854775807L, TimeUnit.NANOSECONDS);
/* 2198:     */   }
/* 2199:     */   
/* 2200:     */   public static void managedBlock(ManagedBlocker blocker)
/* 2201:     */     throws InterruptedException
/* 2202:     */   {
/* 2203:3206 */     Thread t = Thread.currentThread();
/* 2204:3207 */     if ((t instanceof ForkJoinWorkerThread))
/* 2205:     */     {
/* 2206:3208 */       ForkJoinPool p = ((ForkJoinWorkerThread)t).pool;
/* 2207:3209 */       while (!blocker.isReleasable()) {
/* 2208:3210 */         if (p.tryCompensate(p.ctl)) {
/* 2209:     */           try
/* 2210:     */           {
/* 2211:     */             do
/* 2212:     */             {
/* 2213:3212 */               if (blocker.isReleasable()) {
/* 2214:     */                 break;
/* 2215:     */               }
/* 2216:3212 */             } while (!blocker.block());
/* 2217:     */           }
/* 2218:     */           finally
/* 2219:     */           {
/* 2220:3215 */             p.incrementActiveCount();
/* 2221:     */           }
/* 2222:     */         }
/* 2223:     */       }
/* 2224:     */     }
/* 2225:3222 */     while ((!blocker.isReleasable()) && (!blocker.block())) {}
/* 2226:     */   }
/* 2227:     */   
/* 2228:     */   protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value)
/* 2229:     */   {
/* 2230:3232 */     return new ForkJoinTask.AdaptedRunnable(runnable, value);
/* 2231:     */   }
/* 2232:     */   
/* 2233:     */   protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable)
/* 2234:     */   {
/* 2235:3236 */     return new ForkJoinTask.AdaptedCallable(callable);
/* 2236:     */   }
/* 2237:     */   
/* 2238:     */   static
/* 2239:     */   {
/* 2240:     */     try
/* 2241:     */     {
/* 2242:3254 */       U = getUnsafe();
/* 2243:3255 */       Class<?> k = ForkJoinPool.class;
/* 2244:3256 */       CTL = U.objectFieldOffset(k.getDeclaredField("ctl"));
/* 2245:     */       
/* 2246:3258 */       STEALCOUNT = U.objectFieldOffset(k.getDeclaredField("stealCount"));
/* 2247:     */       
/* 2248:3260 */       PLOCK = U.objectFieldOffset(k.getDeclaredField("plock"));
/* 2249:     */       
/* 2250:3262 */       INDEXSEED = U.objectFieldOffset(k.getDeclaredField("indexSeed"));
/* 2251:     */       
/* 2252:3264 */       Class<?> tk = Thread.class;
/* 2253:3265 */       PARKBLOCKER = U.objectFieldOffset(tk.getDeclaredField("parkBlocker"));
/* 2254:     */       
/* 2255:3267 */       Class<?> wk = WorkQueue.class;
/* 2256:3268 */       QBASE = U.objectFieldOffset(wk.getDeclaredField("base"));
/* 2257:     */       
/* 2258:3270 */       QLOCK = U.objectFieldOffset(wk.getDeclaredField("qlock"));
/* 2259:     */       
/* 2260:3272 */       Class<?> ak = [Lio.netty.util.internal.chmv8.ForkJoinTask.class;
/* 2261:3273 */       ABASE = U.arrayBaseOffset(ak);
/* 2262:3274 */       int scale = U.arrayIndexScale(ak);
/* 2263:3275 */       if ((scale & scale - 1) != 0) {
/* 2264:3276 */         throw new Error("data type scale not a power of two");
/* 2265:     */       }
/* 2266:3277 */       ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
/* 2267:     */     }
/* 2268:     */     catch (Exception e)
/* 2269:     */     {
/* 2270:3279 */       throw new Error(e);
/* 2271:     */     }
/* 2272:3282 */     submitters = new ThreadLocal();
/* 2273:3283 */     defaultForkJoinWorkerThreadFactory = new DefaultForkJoinWorkerThreadFactory();
/* 2274:     */     
/* 2275:3285 */     modifyThreadPermission = new RuntimePermission("modifyThread");
/* 2276:     */     
/* 2277:3287 */     common = (ForkJoinPool)AccessController.doPrivileged(new PrivilegedAction()
/* 2278:     */     {
/* 2279:     */       public ForkJoinPool run()
/* 2280:     */       {
/* 2281:3289 */         return ForkJoinPool.access$100();
/* 2282:     */       }
/* 2283:3289 */     });
/* 2284:3290 */     int par = common.parallelism;
/* 2285:3291 */     commonParallelism = par > 0 ? par : 1;
/* 2286:     */   }
/* 2287:     */   
/* 2288:     */   private static ForkJoinPool makeCommonPool()
/* 2289:     */   {
/* 2290:3299 */     int parallelism = -1;
/* 2291:3300 */     ForkJoinWorkerThreadFactory factory = defaultForkJoinWorkerThreadFactory;
/* 2292:     */     
/* 2293:3302 */     Thread.UncaughtExceptionHandler handler = null;
/* 2294:     */     try
/* 2295:     */     {
/* 2296:3304 */       String pp = System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism");
/* 2297:     */       
/* 2298:3306 */       String fp = System.getProperty("java.util.concurrent.ForkJoinPool.common.threadFactory");
/* 2299:     */       
/* 2300:3308 */       String hp = System.getProperty("java.util.concurrent.ForkJoinPool.common.exceptionHandler");
/* 2301:3310 */       if (pp != null) {
/* 2302:3311 */         parallelism = Integer.parseInt(pp);
/* 2303:     */       }
/* 2304:3312 */       if (fp != null) {
/* 2305:3313 */         factory = (ForkJoinWorkerThreadFactory)ClassLoader.getSystemClassLoader().loadClass(fp).newInstance();
/* 2306:     */       }
/* 2307:3315 */       if (hp != null) {
/* 2308:3316 */         handler = (Thread.UncaughtExceptionHandler)ClassLoader.getSystemClassLoader().loadClass(hp).newInstance();
/* 2309:     */       }
/* 2310:     */     }
/* 2311:     */     catch (Exception ignore) {}
/* 2312:3321 */     if ((parallelism < 0) && ((parallelism = Runtime.getRuntime().availableProcessors() - 1) < 0)) {
/* 2313:3323 */       parallelism = 0;
/* 2314:     */     }
/* 2315:3324 */     if (parallelism > 32767) {
/* 2316:3325 */       parallelism = 32767;
/* 2317:     */     }
/* 2318:3326 */     return new ForkJoinPool(parallelism, factory, handler, 0, "ForkJoinPool.commonPool-worker-");
/* 2319:     */   }
/* 2320:     */   
/* 2321:     */   private static Unsafe getUnsafe()
/* 2322:     */   {
/* 2323:     */     try
/* 2324:     */     {
/* 2325:3339 */       return Unsafe.getUnsafe();
/* 2326:     */     }
/* 2327:     */     catch (SecurityException tryReflectionInstead)
/* 2328:     */     {
/* 2329:     */       try
/* 2330:     */       {
/* 2331:3342 */         (Unsafe)AccessController.doPrivileged(new PrivilegedExceptionAction()
/* 2332:     */         {
/* 2333:     */           public Unsafe run()
/* 2334:     */             throws Exception
/* 2335:     */           {
/* 2336:3345 */             Class<Unsafe> k = Unsafe.class;
/* 2337:3346 */             for (Field f : k.getDeclaredFields())
/* 2338:     */             {
/* 2339:3347 */               f.setAccessible(true);
/* 2340:3348 */               Object x = f.get(null);
/* 2341:3349 */               if (k.isInstance(x)) {
/* 2342:3350 */                 return (Unsafe)k.cast(x);
/* 2343:     */               }
/* 2344:     */             }
/* 2345:3352 */             throw new NoSuchFieldError("the Unsafe");
/* 2346:     */           }
/* 2347:     */         });
/* 2348:     */       }
/* 2349:     */       catch (PrivilegedActionException e)
/* 2350:     */       {
/* 2351:3355 */         throw new RuntimeException("Could not initialize intrinsics", e.getCause());
/* 2352:     */       }
/* 2353:     */     }
/* 2354:     */   }
/* 2355:     */   
/* 2356:     */   public static abstract interface ManagedBlocker
/* 2357:     */   {
/* 2358:     */     public abstract boolean block()
/* 2359:     */       throws InterruptedException;
/* 2360:     */     
/* 2361:     */     public abstract boolean isReleasable();
/* 2362:     */   }
/* 2363:     */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.chmv8.ForkJoinPool
 * JD-Core Version:    0.7.0.1
 */