/*    1:     */ package io.netty.util.internal.chmv8;
/*    2:     */ 
/*    3:     */ import io.netty.util.internal.IntegerHolder;
/*    4:     */ import io.netty.util.internal.InternalThreadLocalMap;
/*    5:     */ import java.io.IOException;
/*    6:     */ import java.io.ObjectInputStream;
/*    7:     */ import java.io.ObjectOutputStream;
/*    8:     */ import java.io.ObjectOutputStream.PutField;
/*    9:     */ import java.io.ObjectStreamField;
/*   10:     */ import java.io.Serializable;
/*   11:     */ import java.lang.reflect.Array;
/*   12:     */ import java.lang.reflect.Field;
/*   13:     */ import java.lang.reflect.ParameterizedType;
/*   14:     */ import java.lang.reflect.Type;
/*   15:     */ import java.security.AccessController;
/*   16:     */ import java.security.PrivilegedActionException;
/*   17:     */ import java.security.PrivilegedExceptionAction;
/*   18:     */ import java.util.Arrays;
/*   19:     */ import java.util.Collection;
/*   20:     */ import java.util.Enumeration;
/*   21:     */ import java.util.Iterator;
/*   22:     */ import java.util.Map;
/*   23:     */ import java.util.Map.Entry;
/*   24:     */ import java.util.NoSuchElementException;
/*   25:     */ import java.util.Set;
/*   26:     */ import java.util.concurrent.ConcurrentMap;
/*   27:     */ import java.util.concurrent.atomic.AtomicInteger;
/*   28:     */ import java.util.concurrent.atomic.AtomicReference;
/*   29:     */ import java.util.concurrent.locks.LockSupport;
/*   30:     */ import java.util.concurrent.locks.ReentrantLock;
/*   31:     */ import sun.misc.Unsafe;
/*   32:     */ 
/*   33:     */ public class ConcurrentHashMapV8<K, V>
/*   34:     */   implements ConcurrentMap<K, V>, Serializable
/*   35:     */ {
/*   36:     */   private static final long serialVersionUID = 7249069246763182397L;
/*   37:     */   private static final int MAXIMUM_CAPACITY = 1073741824;
/*   38:     */   private static final int DEFAULT_CAPACITY = 16;
/*   39:     */   static final int MAX_ARRAY_SIZE = 2147483639;
/*   40:     */   private static final int DEFAULT_CONCURRENCY_LEVEL = 16;
/*   41:     */   private static final float LOAD_FACTOR = 0.75F;
/*   42:     */   static final int TREEIFY_THRESHOLD = 8;
/*   43:     */   static final int UNTREEIFY_THRESHOLD = 6;
/*   44:     */   static final int MIN_TREEIFY_CAPACITY = 64;
/*   45:     */   private static final int MIN_TRANSFER_STRIDE = 16;
/*   46:     */   static final int MOVED = -1;
/*   47:     */   static final int TREEBIN = -2;
/*   48:     */   static final int RESERVED = -3;
/*   49:     */   static final int HASH_BITS = 2147483647;
/*   50: 594 */   static final int NCPU = Runtime.getRuntime().availableProcessors();
/*   51: 597 */   private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("segments", [Lio.netty.util.internal.chmv8.ConcurrentHashMapV8.Segment.class), new ObjectStreamField("segmentMask", Integer.TYPE), new ObjectStreamField("segmentShift", Integer.TYPE) };
/*   52:     */   volatile transient Node<K, V>[] table;
/*   53:     */   private volatile transient Node<K, V>[] nextTable;
/*   54:     */   private volatile transient long baseCount;
/*   55:     */   private volatile transient int sizeCtl;
/*   56:     */   private volatile transient int transferIndex;
/*   57:     */   private volatile transient int transferOrigin;
/*   58:     */   private volatile transient int cellsBusy;
/*   59:     */   private volatile transient CounterCell[] counterCells;
/*   60:     */   private transient KeySetView<K, V> keySet;
/*   61:     */   private transient ValuesView<K, V> values;
/*   62:     */   private transient EntrySetView<K, V> entrySet;
/*   63:     */   
/*   64:     */   public static abstract interface ConcurrentHashMapSpliterator<T>
/*   65:     */   {
/*   66:     */     public abstract ConcurrentHashMapSpliterator<T> trySplit();
/*   67:     */     
/*   68:     */     public abstract long estimateSize();
/*   69:     */     
/*   70:     */     public abstract void forEachRemaining(ConcurrentHashMapV8.Action<? super T> paramAction);
/*   71:     */     
/*   72:     */     public abstract boolean tryAdvance(ConcurrentHashMapV8.Action<? super T> paramAction);
/*   73:     */   }
/*   74:     */   
/*   75:     */   public static abstract interface Action<A>
/*   76:     */   {
/*   77:     */     public abstract void apply(A paramA);
/*   78:     */   }
/*   79:     */   
/*   80:     */   public static abstract interface BiAction<A, B>
/*   81:     */   {
/*   82:     */     public abstract void apply(A paramA, B paramB);
/*   83:     */   }
/*   84:     */   
/*   85:     */   public static abstract interface Fun<A, T>
/*   86:     */   {
/*   87:     */     public abstract T apply(A paramA);
/*   88:     */   }
/*   89:     */   
/*   90:     */   public static abstract interface BiFun<A, B, T>
/*   91:     */   {
/*   92:     */     public abstract T apply(A paramA, B paramB);
/*   93:     */   }
/*   94:     */   
/*   95:     */   public static abstract interface ObjectToDouble<A>
/*   96:     */   {
/*   97:     */     public abstract double apply(A paramA);
/*   98:     */   }
/*   99:     */   
/*  100:     */   public static abstract interface ObjectToLong<A>
/*  101:     */   {
/*  102:     */     public abstract long apply(A paramA);
/*  103:     */   }
/*  104:     */   
/*  105:     */   public static abstract interface ObjectToInt<A>
/*  106:     */   {
/*  107:     */     public abstract int apply(A paramA);
/*  108:     */   }
/*  109:     */   
/*  110:     */   public static abstract interface ObjectByObjectToDouble<A, B>
/*  111:     */   {
/*  112:     */     public abstract double apply(A paramA, B paramB);
/*  113:     */   }
/*  114:     */   
/*  115:     */   public static abstract interface ObjectByObjectToLong<A, B>
/*  116:     */   {
/*  117:     */     public abstract long apply(A paramA, B paramB);
/*  118:     */   }
/*  119:     */   
/*  120:     */   public static abstract interface ObjectByObjectToInt<A, B>
/*  121:     */   {
/*  122:     */     public abstract int apply(A paramA, B paramB);
/*  123:     */   }
/*  124:     */   
/*  125:     */   public static abstract interface DoubleByDoubleToDouble
/*  126:     */   {
/*  127:     */     public abstract double apply(double paramDouble1, double paramDouble2);
/*  128:     */   }
/*  129:     */   
/*  130:     */   public static abstract interface LongByLongToLong
/*  131:     */   {
/*  132:     */     public abstract long apply(long paramLong1, long paramLong2);
/*  133:     */   }
/*  134:     */   
/*  135:     */   public static abstract interface IntByIntToInt
/*  136:     */   {
/*  137:     */     public abstract int apply(int paramInt1, int paramInt2);
/*  138:     */   }
/*  139:     */   
/*  140:     */   static class Node<K, V>
/*  141:     */     implements Map.Entry<K, V>
/*  142:     */   {
/*  143:     */     final int hash;
/*  144:     */     final K key;
/*  145:     */     volatile V val;
/*  146:     */     volatile Node<K, V> next;
/*  147:     */     
/*  148:     */     Node(int hash, K key, V val, Node<K, V> next)
/*  149:     */     {
/*  150: 620 */       this.hash = hash;
/*  151: 621 */       this.key = key;
/*  152: 622 */       this.val = val;
/*  153: 623 */       this.next = next;
/*  154:     */     }
/*  155:     */     
/*  156:     */     public final K getKey()
/*  157:     */     {
/*  158: 626 */       return this.key;
/*  159:     */     }
/*  160:     */     
/*  161:     */     public final V getValue()
/*  162:     */     {
/*  163: 627 */       return this.val;
/*  164:     */     }
/*  165:     */     
/*  166:     */     public final int hashCode()
/*  167:     */     {
/*  168: 628 */       return this.key.hashCode() ^ this.val.hashCode();
/*  169:     */     }
/*  170:     */     
/*  171:     */     public final String toString()
/*  172:     */     {
/*  173: 629 */       return this.key + "=" + this.val;
/*  174:     */     }
/*  175:     */     
/*  176:     */     public final V setValue(V value)
/*  177:     */     {
/*  178: 631 */       throw new UnsupportedOperationException();
/*  179:     */     }
/*  180:     */     
/*  181:     */     public final boolean equals(Object o)
/*  182:     */     {
/*  183:     */       Map.Entry<?, ?> e;
/*  184:     */       Object k;
/*  185:     */       Object v;
/*  186:     */       Object u;
/*  187: 636 */       return ((o instanceof Map.Entry)) && ((k = (e = (Map.Entry)o).getKey()) != null) && ((v = e.getValue()) != null) && ((k == this.key) || (k.equals(this.key))) && ((v == (u = this.val)) || (v.equals(u)));
/*  188:     */     }
/*  189:     */     
/*  190:     */     Node<K, V> find(int h, Object k)
/*  191:     */     {
/*  192: 647 */       Node<K, V> e = this;
/*  193: 648 */       if (k != null) {
/*  194:     */         do
/*  195:     */         {
/*  196:     */           K ek;
/*  197: 651 */           if ((e.hash == h) && (((ek = e.key) == k) || ((ek != null) && (k.equals(ek))))) {
/*  198: 653 */             return e;
/*  199:     */           }
/*  200: 654 */         } while ((e = e.next) != null);
/*  201:     */       }
/*  202: 656 */       return null;
/*  203:     */     }
/*  204:     */   }
/*  205:     */   
/*  206:     */   static final int spread(int h)
/*  207:     */   {
/*  208: 679 */     return (h ^ h >>> 16) & 0x7FFFFFFF;
/*  209:     */   }
/*  210:     */   
/*  211:     */   private static final int tableSizeFor(int c)
/*  212:     */   {
/*  213: 687 */     int n = c - 1;
/*  214: 688 */     n |= n >>> 1;
/*  215: 689 */     n |= n >>> 2;
/*  216: 690 */     n |= n >>> 4;
/*  217: 691 */     n |= n >>> 8;
/*  218: 692 */     n |= n >>> 16;
/*  219: 693 */     return n >= 1073741824 ? 1073741824 : n < 0 ? 1 : n + 1;
/*  220:     */   }
/*  221:     */   
/*  222:     */   static Class<?> comparableClassFor(Object x)
/*  223:     */   {
/*  224: 701 */     if ((x instanceof Comparable))
/*  225:     */     {
/*  226:     */       Class<?> c;
/*  227: 703 */       if ((c = x.getClass()) == String.class) {
/*  228: 704 */         return c;
/*  229:     */       }
/*  230:     */       Type[] ts;
/*  231: 705 */       if ((ts = c.getGenericInterfaces()) != null) {
/*  232: 706 */         for (int i = 0; i < ts.length; i++)
/*  233:     */         {
/*  234:     */           Type t;
/*  235:     */           ParameterizedType p;
/*  236:     */           Type[] as;
/*  237: 707 */           if ((((t = ts[i]) instanceof ParameterizedType)) && ((p = (ParameterizedType)t).getRawType() == Comparable.class) && ((as = p.getActualTypeArguments()) != null) && (as.length == 1) && (as[0] == c)) {
/*  238: 712 */             return c;
/*  239:     */           }
/*  240:     */         }
/*  241:     */       }
/*  242:     */     }
/*  243: 716 */     return null;
/*  244:     */   }
/*  245:     */   
/*  246:     */   static int compareComparables(Class<?> kc, Object k, Object x)
/*  247:     */   {
/*  248: 725 */     return (x == null) || (x.getClass() != kc) ? 0 : ((Comparable)k).compareTo(x);
/*  249:     */   }
/*  250:     */   
/*  251:     */   static final <K, V> Node<K, V> tabAt(Node<K, V>[] tab, int i)
/*  252:     */   {
/*  253: 749 */     return (Node)U.getObjectVolatile(tab, (i << ASHIFT) + ABASE);
/*  254:     */   }
/*  255:     */   
/*  256:     */   static final <K, V> boolean casTabAt(Node<K, V>[] tab, int i, Node<K, V> c, Node<K, V> v)
/*  257:     */   {
/*  258: 754 */     return U.compareAndSwapObject(tab, (i << ASHIFT) + ABASE, c, v);
/*  259:     */   }
/*  260:     */   
/*  261:     */   static final <K, V> void setTabAt(Node<K, V>[] tab, int i, Node<K, V> v)
/*  262:     */   {
/*  263: 758 */     U.putObjectVolatile(tab, (i << ASHIFT) + ABASE, v);
/*  264:     */   }
/*  265:     */   
/*  266:     */   public ConcurrentHashMapV8(int initialCapacity)
/*  267:     */   {
/*  268: 836 */     if (initialCapacity < 0) {
/*  269: 837 */       throw new IllegalArgumentException();
/*  270:     */     }
/*  271: 838 */     int cap = initialCapacity >= 536870912 ? 1073741824 : tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1);
/*  272:     */     
/*  273:     */ 
/*  274: 841 */     this.sizeCtl = cap;
/*  275:     */   }
/*  276:     */   
/*  277:     */   public ConcurrentHashMapV8(Map<? extends K, ? extends V> m)
/*  278:     */   {
/*  279: 850 */     this.sizeCtl = 16;
/*  280: 851 */     putAll(m);
/*  281:     */   }
/*  282:     */   
/*  283:     */   public ConcurrentHashMapV8(int initialCapacity, float loadFactor)
/*  284:     */   {
/*  285: 870 */     this(initialCapacity, loadFactor, 1);
/*  286:     */   }
/*  287:     */   
/*  288:     */   public ConcurrentHashMapV8(int initialCapacity, float loadFactor, int concurrencyLevel)
/*  289:     */   {
/*  290: 893 */     if ((loadFactor <= 0.0F) || (initialCapacity < 0) || (concurrencyLevel <= 0)) {
/*  291: 894 */       throw new IllegalArgumentException();
/*  292:     */     }
/*  293: 895 */     if (initialCapacity < concurrencyLevel) {
/*  294: 896 */       initialCapacity = concurrencyLevel;
/*  295:     */     }
/*  296: 897 */     long size = (1.0D + (float)initialCapacity / loadFactor);
/*  297: 898 */     int cap = size >= 1073741824L ? 1073741824 : tableSizeFor((int)size);
/*  298:     */     
/*  299: 900 */     this.sizeCtl = cap;
/*  300:     */   }
/*  301:     */   
/*  302:     */   public int size()
/*  303:     */   {
/*  304: 909 */     long n = sumCount();
/*  305: 910 */     return n > 2147483647L ? 2147483647 : n < 0L ? 0 : (int)n;
/*  306:     */   }
/*  307:     */   
/*  308:     */   public boolean isEmpty()
/*  309:     */   {
/*  310: 919 */     return sumCount() <= 0L;
/*  311:     */   }
/*  312:     */   
/*  313:     */   public V get(Object key)
/*  314:     */   {
/*  315: 935 */     int h = spread(key.hashCode());
/*  316:     */     Node<K, V>[] tab;
/*  317:     */     int n;
/*  318:     */     Node<K, V> e;
/*  319: 936 */     if (((tab = this.table) != null) && ((n = tab.length) > 0) && ((e = tabAt(tab, n - 1 & h)) != null))
/*  320:     */     {
/*  321:     */       int eh;
/*  322: 938 */       if ((eh = e.hash) == h)
/*  323:     */       {
/*  324:     */         K ek;
/*  325: 939 */         if (((ek = e.key) == key) || ((ek != null) && (key.equals(ek)))) {
/*  326: 940 */           return e.val;
/*  327:     */         }
/*  328:     */       }
/*  329: 942 */       else if (eh < 0)
/*  330:     */       {
/*  331:     */         Node<K, V> p;
/*  332: 943 */         return (p = e.find(h, key)) != null ? p.val : null;
/*  333:     */       }
/*  334: 944 */       while ((e = e.next) != null)
/*  335:     */       {
/*  336:     */         K ek;
/*  337: 945 */         if ((e.hash == h) && (((ek = e.key) == key) || ((ek != null) && (key.equals(ek))))) {
/*  338: 947 */           return e.val;
/*  339:     */         }
/*  340:     */       }
/*  341:     */     }
/*  342: 950 */     return null;
/*  343:     */   }
/*  344:     */   
/*  345:     */   public boolean containsKey(Object key)
/*  346:     */   {
/*  347: 963 */     return get(key) != null;
/*  348:     */   }
/*  349:     */   
/*  350:     */   public boolean containsValue(Object value)
/*  351:     */   {
/*  352: 977 */     if (value == null) {
/*  353: 978 */       throw new NullPointerException();
/*  354:     */     }
/*  355:     */     Node<K, V>[] t;
/*  356: 980 */     if ((t = this.table) != null)
/*  357:     */     {
/*  358: 981 */       Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
/*  359:     */       Node<K, V> p;
/*  360: 982 */       while ((p = it.advance()) != null)
/*  361:     */       {
/*  362:     */         V v;
/*  363: 984 */         if (((v = p.val) == value) || ((v != null) && (value.equals(v)))) {
/*  364: 985 */           return true;
/*  365:     */         }
/*  366:     */       }
/*  367:     */     }
/*  368: 988 */     return false;
/*  369:     */   }
/*  370:     */   
/*  371:     */   public V put(K key, V value)
/*  372:     */   {
/*  373:1005 */     return putVal(key, value, false);
/*  374:     */   }
/*  375:     */   
/*  376:     */   final V putVal(K key, V value, boolean onlyIfAbsent)
/*  377:     */   {
/*  378:1010 */     if ((key == null) || (value == null)) {
/*  379:1010 */       throw new NullPointerException();
/*  380:     */     }
/*  381:1011 */     int hash = spread(key.hashCode());
/*  382:1012 */     int binCount = 0;
/*  383:1013 */     Node<K, V>[] tab = this.table;
/*  384:     */     for (;;)
/*  385:     */     {
/*  386:     */       int n;
/*  387:1015 */       if ((tab == null) || ((n = tab.length) == 0))
/*  388:     */       {
/*  389:1016 */         tab = initTable();
/*  390:     */       }
/*  391:     */       else
/*  392:     */       {
/*  393:     */         int n;
/*  394:     */         int i;
/*  395:     */         Node<K, V> f;
/*  396:1017 */         if ((f = tabAt(tab, i = n - 1 & hash)) == null)
/*  397:     */         {
/*  398:1018 */           if (casTabAt(tab, i, null, new Node(hash, key, value, null))) {
/*  399:     */             break;
/*  400:     */           }
/*  401:     */         }
/*  402:     */         else
/*  403:     */         {
/*  404:     */           int fh;
/*  405:1022 */           if ((fh = f.hash) == -1)
/*  406:     */           {
/*  407:1023 */             tab = helpTransfer(tab, f);
/*  408:     */           }
/*  409:     */           else
/*  410:     */           {
/*  411:1025 */             V oldVal = null;
/*  412:1026 */             synchronized (f)
/*  413:     */             {
/*  414:1027 */               if (tabAt(tab, i) == f) {
/*  415:1028 */                 if (fh >= 0)
/*  416:     */                 {
/*  417:1029 */                   binCount = 1;
/*  418:1030 */                   for (Node<K, V> e = f;; binCount++)
/*  419:     */                   {
/*  420:     */                     K ek;
/*  421:1032 */                     if ((e.hash == hash) && (((ek = e.key) == key) || ((ek != null) && (key.equals(ek)))))
/*  422:     */                     {
/*  423:1035 */                       oldVal = e.val;
/*  424:1036 */                       if (onlyIfAbsent) {
/*  425:     */                         break;
/*  426:     */                       }
/*  427:1037 */                       e.val = value; break;
/*  428:     */                     }
/*  429:1040 */                     Node<K, V> pred = e;
/*  430:1041 */                     if ((e = e.next) == null)
/*  431:     */                     {
/*  432:1042 */                       pred.next = new Node(hash, key, value, null);
/*  433:     */                       
/*  434:1044 */                       break;
/*  435:     */                     }
/*  436:     */                   }
/*  437:     */                 }
/*  438:1048 */                 else if ((f instanceof TreeBin))
/*  439:     */                 {
/*  440:1050 */                   binCount = 2;
/*  441:     */                   Node<K, V> p;
/*  442:1051 */                   if ((p = ((TreeBin)f).putTreeVal(hash, key, value)) != null)
/*  443:     */                   {
/*  444:1053 */                     oldVal = p.val;
/*  445:1054 */                     if (!onlyIfAbsent) {
/*  446:1055 */                       p.val = value;
/*  447:     */                     }
/*  448:     */                   }
/*  449:     */                 }
/*  450:     */               }
/*  451:     */             }
/*  452:1060 */             if (binCount != 0)
/*  453:     */             {
/*  454:1061 */               if (binCount >= 8) {
/*  455:1062 */                 treeifyBin(tab, i);
/*  456:     */               }
/*  457:1063 */               if (oldVal == null) {
/*  458:     */                 break;
/*  459:     */               }
/*  460:1064 */               return oldVal;
/*  461:     */             }
/*  462:     */           }
/*  463:     */         }
/*  464:     */       }
/*  465:     */     }
/*  466:1069 */     addCount(1L, binCount);
/*  467:1070 */     return null;
/*  468:     */   }
/*  469:     */   
/*  470:     */   public void putAll(Map<? extends K, ? extends V> m)
/*  471:     */   {
/*  472:1081 */     tryPresize(m.size());
/*  473:1082 */     for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
/*  474:1083 */       putVal(e.getKey(), e.getValue(), false);
/*  475:     */     }
/*  476:     */   }
/*  477:     */   
/*  478:     */   public V remove(Object key)
/*  479:     */   {
/*  480:1096 */     return replaceNode(key, null, null);
/*  481:     */   }
/*  482:     */   
/*  483:     */   final V replaceNode(Object key, V value, Object cv)
/*  484:     */   {
/*  485:1105 */     int hash = spread(key.hashCode());
/*  486:1106 */     Node<K, V>[] tab = this.table;
/*  487:     */     int n;
/*  488:     */     int i;
/*  489:     */     Node<K, V> f;
/*  490:1108 */     while ((tab != null) && ((n = tab.length) != 0) && ((f = tabAt(tab, i = n - 1 & hash)) != null))
/*  491:     */     {
/*  492:     */       int fh;
/*  493:1111 */       if ((fh = f.hash) == -1)
/*  494:     */       {
/*  495:1112 */         tab = helpTransfer(tab, f);
/*  496:     */       }
/*  497:     */       else
/*  498:     */       {
/*  499:1114 */         V oldVal = null;
/*  500:1115 */         boolean validated = false;
/*  501:1116 */         synchronized (f)
/*  502:     */         {
/*  503:1117 */           if (tabAt(tab, i) == f) {
/*  504:1118 */             if (fh >= 0)
/*  505:     */             {
/*  506:1119 */               validated = true;
/*  507:1120 */               Node<K, V> e = f;Node<K, V> pred = null;
/*  508:     */               for (;;)
/*  509:     */               {
/*  510:     */                 K ek;
/*  511:1122 */                 if ((e.hash == hash) && (((ek = e.key) == key) || ((ek != null) && (key.equals(ek)))))
/*  512:     */                 {
/*  513:1125 */                   V ev = e.val;
/*  514:1126 */                   if ((cv == null) || (cv == ev) || ((ev != null) && (cv.equals(ev))))
/*  515:     */                   {
/*  516:1128 */                     oldVal = ev;
/*  517:1129 */                     if (value != null) {
/*  518:1130 */                       e.val = value;
/*  519:1131 */                     } else if (pred != null) {
/*  520:1132 */                       pred.next = e.next;
/*  521:     */                     } else {
/*  522:1134 */                       setTabAt(tab, i, e.next);
/*  523:     */                     }
/*  524:     */                   }
/*  525:     */                 }
/*  526:     */                 else
/*  527:     */                 {
/*  528:1138 */                   pred = e;
/*  529:1139 */                   if ((e = e.next) == null) {
/*  530:     */                     break;
/*  531:     */                   }
/*  532:     */                 }
/*  533:     */               }
/*  534:     */             }
/*  535:1143 */             else if ((f instanceof TreeBin))
/*  536:     */             {
/*  537:1144 */               validated = true;
/*  538:1145 */               TreeBin<K, V> t = (TreeBin)f;
/*  539:     */               TreeNode<K, V> r;
/*  540:     */               TreeNode<K, V> p;
/*  541:1147 */               if (((r = t.root) != null) && ((p = r.findTreeNode(hash, key, null)) != null))
/*  542:     */               {
/*  543:1149 */                 V pv = p.val;
/*  544:1150 */                 if ((cv == null) || (cv == pv) || ((pv != null) && (cv.equals(pv))))
/*  545:     */                 {
/*  546:1152 */                   oldVal = pv;
/*  547:1153 */                   if (value != null) {
/*  548:1154 */                     p.val = value;
/*  549:1155 */                   } else if (t.removeTreeNode(p)) {
/*  550:1156 */                     setTabAt(tab, i, untreeify(t.first));
/*  551:     */                   }
/*  552:     */                 }
/*  553:     */               }
/*  554:     */             }
/*  555:     */           }
/*  556:     */         }
/*  557:1162 */         if (validated)
/*  558:     */         {
/*  559:1163 */           if (oldVal == null) {
/*  560:     */             break;
/*  561:     */           }
/*  562:1164 */           if (value == null) {
/*  563:1165 */             addCount(-1L, -1);
/*  564:     */           }
/*  565:1166 */           return oldVal;
/*  566:     */         }
/*  567:     */       }
/*  568:     */     }
/*  569:1172 */     return null;
/*  570:     */   }
/*  571:     */   
/*  572:     */   public void clear()
/*  573:     */   {
/*  574:1179 */     long delta = 0L;
/*  575:1180 */     int i = 0;
/*  576:1181 */     Node<K, V>[] tab = this.table;
/*  577:1182 */     while ((tab != null) && (i < tab.length))
/*  578:     */     {
/*  579:1184 */       Node<K, V> f = tabAt(tab, i);
/*  580:1185 */       if (f == null)
/*  581:     */       {
/*  582:1186 */         i++;
/*  583:     */       }
/*  584:     */       else
/*  585:     */       {
/*  586:     */         int fh;
/*  587:1187 */         if ((fh = f.hash) == -1)
/*  588:     */         {
/*  589:1188 */           tab = helpTransfer(tab, f);
/*  590:1189 */           i = 0;
/*  591:     */         }
/*  592:     */         else
/*  593:     */         {
/*  594:1192 */           synchronized (f)
/*  595:     */           {
/*  596:1193 */             if (tabAt(tab, i) == f)
/*  597:     */             {
/*  598:1194 */               Node<K, V> p = (f instanceof TreeBin) ? ((TreeBin)f).first : fh >= 0 ? f : null;
/*  599:1197 */               while (p != null)
/*  600:     */               {
/*  601:1198 */                 delta -= 1L;
/*  602:1199 */                 p = p.next;
/*  603:     */               }
/*  604:1201 */               setTabAt(tab, i++, null);
/*  605:     */             }
/*  606:     */           }
/*  607:     */         }
/*  608:     */       }
/*  609:     */     }
/*  610:1206 */     if (delta != 0L) {
/*  611:1207 */       addCount(delta, -1);
/*  612:     */     }
/*  613:     */   }
/*  614:     */   
/*  615:     */   public KeySetView<K, V> keySet()
/*  616:     */   {
/*  617:     */     KeySetView<K, V> ks;
/*  618:1230 */     return this.keySet = new KeySetView(this, null);
/*  619:     */   }
/*  620:     */   
/*  621:     */   public Collection<V> values()
/*  622:     */   {
/*  623:     */     ValuesView<K, V> vs;
/*  624:1253 */     return this.values = new ValuesView(this);
/*  625:     */   }
/*  626:     */   
/*  627:     */   public Set<Map.Entry<K, V>> entrySet()
/*  628:     */   {
/*  629:     */     EntrySetView<K, V> es;
/*  630:1275 */     return this.entrySet = new EntrySetView(this);
/*  631:     */   }
/*  632:     */   
/*  633:     */   public int hashCode()
/*  634:     */   {
/*  635:1286 */     int h = 0;
/*  636:     */     Node<K, V>[] t;
/*  637:1288 */     if ((t = this.table) != null)
/*  638:     */     {
/*  639:1289 */       Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
/*  640:     */       Node<K, V> p;
/*  641:1290 */       while ((p = it.advance()) != null) {
/*  642:1291 */         h += (p.key.hashCode() ^ p.val.hashCode());
/*  643:     */       }
/*  644:     */     }
/*  645:1293 */     return h;
/*  646:     */   }
/*  647:     */   
/*  648:     */   public String toString()
/*  649:     */   {
/*  650:     */     Node<K, V>[] t;
/*  651:1309 */     int f = (t = this.table) == null ? 0 : t.length;
/*  652:1310 */     Traverser<K, V> it = new Traverser(t, f, 0, f);
/*  653:1311 */     StringBuilder sb = new StringBuilder();
/*  654:1312 */     sb.append('{');
/*  655:     */     Node<K, V> p;
/*  656:1314 */     if ((p = it.advance()) != null) {
/*  657:     */       for (;;)
/*  658:     */       {
/*  659:1316 */         K k = p.key;
/*  660:1317 */         V v = p.val;
/*  661:1318 */         sb.append(k == this ? "(this Map)" : k);
/*  662:1319 */         sb.append('=');
/*  663:1320 */         sb.append(v == this ? "(this Map)" : v);
/*  664:1321 */         if ((p = it.advance()) == null) {
/*  665:     */           break;
/*  666:     */         }
/*  667:1323 */         sb.append(',').append(' ');
/*  668:     */       }
/*  669:     */     }
/*  670:1326 */     return '}';
/*  671:     */   }
/*  672:     */   
/*  673:     */   public boolean equals(Object o)
/*  674:     */   {
/*  675:1340 */     if (o != this)
/*  676:     */     {
/*  677:1341 */       if (!(o instanceof Map)) {
/*  678:1342 */         return false;
/*  679:     */       }
/*  680:1343 */       Map<?, ?> m = (Map)o;
/*  681:     */       Node<K, V>[] t;
/*  682:1345 */       int f = (t = this.table) == null ? 0 : t.length;
/*  683:1346 */       Traverser<K, V> it = new Traverser(t, f, 0, f);
/*  684:     */       Node<K, V> p;
/*  685:1347 */       while ((p = it.advance()) != null)
/*  686:     */       {
/*  687:1348 */         V val = p.val;
/*  688:1349 */         Object v = m.get(p.key);
/*  689:1350 */         if ((v == null) || ((v != val) && (!v.equals(val)))) {
/*  690:1351 */           return false;
/*  691:     */         }
/*  692:     */       }
/*  693:1353 */       for (Map.Entry<?, ?> e : m.entrySet())
/*  694:     */       {
/*  695:     */         Object mk;
/*  696:     */         Object mv;
/*  697:     */         Object v;
/*  698:1355 */         if (((mk = e.getKey()) == null) || ((mv = e.getValue()) == null) || ((v = get(mk)) == null) || ((mv != v) && (!mv.equals(v)))) {
/*  699:1359 */           return false;
/*  700:     */         }
/*  701:     */       }
/*  702:     */     }
/*  703:1362 */     return true;
/*  704:     */   }
/*  705:     */   
/*  706:     */   static class Segment<K, V>
/*  707:     */     extends ReentrantLock
/*  708:     */     implements Serializable
/*  709:     */   {
/*  710:     */     private static final long serialVersionUID = 2249069246763182397L;
/*  711:     */     final float loadFactor;
/*  712:     */     
/*  713:     */     Segment(float lf)
/*  714:     */     {
/*  715:1372 */       this.loadFactor = lf;
/*  716:     */     }
/*  717:     */   }
/*  718:     */   
/*  719:     */   private void writeObject(ObjectOutputStream s)
/*  720:     */     throws IOException
/*  721:     */   {
/*  722:1388 */     int sshift = 0;
/*  723:1389 */     int ssize = 1;
/*  724:1390 */     while (ssize < 16)
/*  725:     */     {
/*  726:1391 */       sshift++;
/*  727:1392 */       ssize <<= 1;
/*  728:     */     }
/*  729:1394 */     int segmentShift = 32 - sshift;
/*  730:1395 */     int segmentMask = ssize - 1;
/*  731:1396 */     Segment<K, V>[] segments = (Segment[])new Segment[16];
/*  732:1398 */     for (int i = 0; i < segments.length; i++) {
/*  733:1399 */       segments[i] = new Segment(0.75F);
/*  734:     */     }
/*  735:1400 */     s.putFields().put("segments", segments);
/*  736:1401 */     s.putFields().put("segmentShift", segmentShift);
/*  737:1402 */     s.putFields().put("segmentMask", segmentMask);
/*  738:1403 */     s.writeFields();
/*  739:     */     Node<K, V>[] t;
/*  740:1406 */     if ((t = this.table) != null)
/*  741:     */     {
/*  742:1407 */       Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
/*  743:     */       Node<K, V> p;
/*  744:1408 */       while ((p = it.advance()) != null)
/*  745:     */       {
/*  746:1409 */         s.writeObject(p.key);
/*  747:1410 */         s.writeObject(p.val);
/*  748:     */       }
/*  749:     */     }
/*  750:1413 */     s.writeObject(null);
/*  751:1414 */     s.writeObject(null);
/*  752:1415 */     segments = null;
/*  753:     */   }
/*  754:     */   
/*  755:     */   private void readObject(ObjectInputStream s)
/*  756:     */     throws IOException, ClassNotFoundException
/*  757:     */   {
/*  758:1431 */     this.sizeCtl = -1;
/*  759:1432 */     s.defaultReadObject();
/*  760:1433 */     long size = 0L;
/*  761:1434 */     Node<K, V> p = null;
/*  762:     */     for (;;)
/*  763:     */     {
/*  764:1436 */       K k = s.readObject();
/*  765:1437 */       V v = s.readObject();
/*  766:1438 */       if ((k == null) || (v == null)) {
/*  767:     */         break;
/*  768:     */       }
/*  769:1439 */       p = new Node(spread(k.hashCode()), k, v, p);
/*  770:1440 */       size += 1L;
/*  771:     */     }
/*  772:1445 */     if (size == 0L)
/*  773:     */     {
/*  774:1446 */       this.sizeCtl = 0;
/*  775:     */     }
/*  776:     */     else
/*  777:     */     {
/*  778:     */       int n;
/*  779:     */       int n;
/*  780:1449 */       if (size >= 536870912L)
/*  781:     */       {
/*  782:1450 */         n = 1073741824;
/*  783:     */       }
/*  784:     */       else
/*  785:     */       {
/*  786:1452 */         int sz = (int)size;
/*  787:1453 */         n = tableSizeFor(sz + (sz >>> 1) + 1);
/*  788:     */       }
/*  789:1456 */       Node<K, V>[] tab = (Node[])new Node[n];
/*  790:1457 */       int mask = n - 1;
/*  791:1458 */       long added = 0L;
/*  792:1459 */       while (p != null)
/*  793:     */       {
/*  794:1461 */         Node<K, V> next = p.next;
/*  795:1462 */         int h = p.hash;int j = h & mask;
/*  796:     */         Node<K, V> first;
/*  797:     */         boolean insertAtFront;
/*  798:     */         boolean insertAtFront;
/*  799:1463 */         if ((first = tabAt(tab, j)) == null)
/*  800:     */         {
/*  801:1464 */           insertAtFront = true;
/*  802:     */         }
/*  803:     */         else
/*  804:     */         {
/*  805:1466 */           K k = p.key;
/*  806:     */           boolean insertAtFront;
/*  807:1467 */           if (first.hash < 0)
/*  808:     */           {
/*  809:1468 */             TreeBin<K, V> t = (TreeBin)first;
/*  810:1469 */             if (t.putTreeVal(h, k, p.val) == null) {
/*  811:1470 */               added += 1L;
/*  812:     */             }
/*  813:1471 */             insertAtFront = false;
/*  814:     */           }
/*  815:     */           else
/*  816:     */           {
/*  817:1474 */             int binCount = 0;
/*  818:1475 */             insertAtFront = true;
/*  819:1477 */             for (Node<K, V> q = first; q != null; q = q.next)
/*  820:     */             {
/*  821:     */               K qk;
/*  822:1478 */               if ((q.hash == h) && (((qk = q.key) == k) || ((qk != null) && (k.equals(qk)))))
/*  823:     */               {
/*  824:1481 */                 insertAtFront = false;
/*  825:1482 */                 break;
/*  826:     */               }
/*  827:1484 */               binCount++;
/*  828:     */             }
/*  829:1486 */             if ((insertAtFront) && (binCount >= 8))
/*  830:     */             {
/*  831:1487 */               insertAtFront = false;
/*  832:1488 */               added += 1L;
/*  833:1489 */               p.next = first;
/*  834:1490 */               TreeNode<K, V> hd = null;TreeNode<K, V> tl = null;
/*  835:1491 */               for (q = p; q != null; q = q.next)
/*  836:     */               {
/*  837:1492 */                 TreeNode<K, V> t = new TreeNode(q.hash, q.key, q.val, null, null);
/*  838:1494 */                 if ((t.prev = tl) == null) {
/*  839:1495 */                   hd = t;
/*  840:     */                 } else {
/*  841:1497 */                   tl.next = t;
/*  842:     */                 }
/*  843:1498 */                 tl = t;
/*  844:     */               }
/*  845:1500 */               setTabAt(tab, j, new TreeBin(hd));
/*  846:     */             }
/*  847:     */           }
/*  848:     */         }
/*  849:1504 */         if (insertAtFront)
/*  850:     */         {
/*  851:1505 */           added += 1L;
/*  852:1506 */           p.next = first;
/*  853:1507 */           setTabAt(tab, j, p);
/*  854:     */         }
/*  855:1509 */         p = next;
/*  856:     */       }
/*  857:1511 */       this.table = tab;
/*  858:1512 */       this.sizeCtl = (n - (n >>> 2));
/*  859:1513 */       this.baseCount = added;
/*  860:     */     }
/*  861:     */   }
/*  862:     */   
/*  863:     */   public V putIfAbsent(K key, V value)
/*  864:     */   {
/*  865:1527 */     return putVal(key, value, true);
/*  866:     */   }
/*  867:     */   
/*  868:     */   public boolean remove(Object key, Object value)
/*  869:     */   {
/*  870:1536 */     if (key == null) {
/*  871:1537 */       throw new NullPointerException();
/*  872:     */     }
/*  873:1538 */     return (value != null) && (replaceNode(key, null, value) != null);
/*  874:     */   }
/*  875:     */   
/*  876:     */   public boolean replace(K key, V oldValue, V newValue)
/*  877:     */   {
/*  878:1547 */     if ((key == null) || (oldValue == null) || (newValue == null)) {
/*  879:1548 */       throw new NullPointerException();
/*  880:     */     }
/*  881:1549 */     return replaceNode(key, newValue, oldValue) != null;
/*  882:     */   }
/*  883:     */   
/*  884:     */   public V replace(K key, V value)
/*  885:     */   {
/*  886:1560 */     if ((key == null) || (value == null)) {
/*  887:1561 */       throw new NullPointerException();
/*  888:     */     }
/*  889:1562 */     return replaceNode(key, value, null);
/*  890:     */   }
/*  891:     */   
/*  892:     */   public V getOrDefault(Object key, V defaultValue)
/*  893:     */   {
/*  894:     */     V v;
/*  895:1580 */     return (v = get(key)) == null ? defaultValue : v;
/*  896:     */   }
/*  897:     */   
/*  898:     */   public void forEach(BiAction<? super K, ? super V> action)
/*  899:     */   {
/*  900:1584 */     if (action == null) {
/*  901:1584 */       throw new NullPointerException();
/*  902:     */     }
/*  903:     */     Node<K, V>[] t;
/*  904:1586 */     if ((t = this.table) != null)
/*  905:     */     {
/*  906:1587 */       Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
/*  907:     */       Node<K, V> p;
/*  908:1588 */       while ((p = it.advance()) != null) {
/*  909:1589 */         action.apply(p.key, p.val);
/*  910:     */       }
/*  911:     */     }
/*  912:     */   }
/*  913:     */   
/*  914:     */   public void replaceAll(BiFun<? super K, ? super V, ? extends V> function)
/*  915:     */   {
/*  916:1595 */     if (function == null) {
/*  917:1595 */       throw new NullPointerException();
/*  918:     */     }
/*  919:     */     Node<K, V>[] t;
/*  920:1597 */     if ((t = this.table) != null)
/*  921:     */     {
/*  922:1598 */       Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
/*  923:     */       Node<K, V> p;
/*  924:1599 */       while ((p = it.advance()) != null)
/*  925:     */       {
/*  926:1600 */         V oldValue = p.val;
/*  927:1601 */         K key = p.key;
/*  928:     */         for (;;)
/*  929:     */         {
/*  930:1602 */           V newValue = function.apply(key, oldValue);
/*  931:1603 */           if (newValue == null) {
/*  932:1604 */             throw new NullPointerException();
/*  933:     */           }
/*  934:1605 */           if ((replaceNode(key, newValue, oldValue) != null) || ((oldValue = get(key)) == null)) {
/*  935:     */             break;
/*  936:     */           }
/*  937:     */         }
/*  938:     */       }
/*  939:     */     }
/*  940:     */   }
/*  941:     */   
/*  942:     */   public V computeIfAbsent(K key, Fun<? super K, ? extends V> mappingFunction)
/*  943:     */   {
/*  944:1636 */     if ((key == null) || (mappingFunction == null)) {
/*  945:1637 */       throw new NullPointerException();
/*  946:     */     }
/*  947:1638 */     int h = spread(key.hashCode());
/*  948:1639 */     V val = null;
/*  949:1640 */     int binCount = 0;
/*  950:1641 */     Node<K, V>[] tab = this.table;
/*  951:     */     for (;;)
/*  952:     */     {
/*  953:     */       int n;
/*  954:1643 */       if ((tab == null) || ((n = tab.length) == 0))
/*  955:     */       {
/*  956:1644 */         tab = initTable();
/*  957:     */       }
/*  958:     */       else
/*  959:     */       {
/*  960:     */         int n;
/*  961:     */         int i;
/*  962:     */         Node<K, V> f;
/*  963:1645 */         if ((f = tabAt(tab, i = n - 1 & h)) == null)
/*  964:     */         {
/*  965:1646 */           Node<K, V> r = new ReservationNode();
/*  966:1647 */           synchronized (r)
/*  967:     */           {
/*  968:1648 */             if (casTabAt(tab, i, null, r))
/*  969:     */             {
/*  970:1649 */               binCount = 1;
/*  971:1650 */               Node<K, V> node = null;
/*  972:     */               try
/*  973:     */               {
/*  974:1652 */                 if ((val = mappingFunction.apply(key)) != null) {
/*  975:1653 */                   node = new Node(h, key, val, null);
/*  976:     */                 }
/*  977:     */               }
/*  978:     */               finally
/*  979:     */               {
/*  980:1655 */                 setTabAt(tab, i, node);
/*  981:     */               }
/*  982:     */             }
/*  983:     */           }
/*  984:1659 */           if (binCount != 0) {
/*  985:     */             break;
/*  986:     */           }
/*  987:     */         }
/*  988:     */         else
/*  989:     */         {
/*  990:     */           int fh;
/*  991:1662 */           if ((fh = f.hash) == -1)
/*  992:     */           {
/*  993:1663 */             tab = helpTransfer(tab, f);
/*  994:     */           }
/*  995:     */           else
/*  996:     */           {
/*  997:1665 */             boolean added = false;
/*  998:1666 */             synchronized (f)
/*  999:     */             {
/* 1000:1667 */               if (tabAt(tab, i) == f) {
/* 1001:1668 */                 if (fh >= 0)
/* 1002:     */                 {
/* 1003:1669 */                   binCount = 1;
/* 1004:1670 */                   for (Node<K, V> e = f;; binCount++)
/* 1005:     */                   {
/* 1006:     */                     Object ek;
/* 1007:1672 */                     if ((e.hash == h) && (((ek = e.key) == key) || ((ek != null) && (key.equals(ek)))))
/* 1008:     */                     {
/* 1009:1675 */                       val = e.val;
/* 1010:1676 */                       break;
/* 1011:     */                     }
/* 1012:1678 */                     Node<K, V> pred = e;
/* 1013:1679 */                     if ((e = e.next) == null)
/* 1014:     */                     {
/* 1015:1680 */                       if ((val = mappingFunction.apply(key)) == null) {
/* 1016:     */                         break;
/* 1017:     */                       }
/* 1018:1681 */                       added = true;
/* 1019:1682 */                       pred.next = new Node(h, key, val, null); break;
/* 1020:     */                     }
/* 1021:     */                   }
/* 1022:     */                 }
/* 1023:1688 */                 else if ((f instanceof TreeBin))
/* 1024:     */                 {
/* 1025:1689 */                   binCount = 2;
/* 1026:1690 */                   TreeBin<K, V> t = (TreeBin)f;
/* 1027:     */                   Object r;
/* 1028:     */                   Object p;
/* 1029:1692 */                   if (((r = t.root) != null) && ((p = ((TreeNode)r).findTreeNode(h, key, null)) != null))
/* 1030:     */                   {
/* 1031:1694 */                     val = ((TreeNode)p).val;
/* 1032:     */                   }
/* 1033:1695 */                   else if ((val = mappingFunction.apply(key)) != null)
/* 1034:     */                   {
/* 1035:1696 */                     added = true;
/* 1036:1697 */                     t.putTreeVal(h, key, val);
/* 1037:     */                   }
/* 1038:     */                 }
/* 1039:     */               }
/* 1040:     */             }
/* 1041:1702 */             if (binCount != 0)
/* 1042:     */             {
/* 1043:1703 */               if (binCount >= 8) {
/* 1044:1704 */                 treeifyBin(tab, i);
/* 1045:     */               }
/* 1046:1705 */               if (added) {
/* 1047:     */                 break;
/* 1048:     */               }
/* 1049:1706 */               return val;
/* 1050:     */             }
/* 1051:     */           }
/* 1052:     */         }
/* 1053:     */       }
/* 1054:     */     }
/* 1055:1711 */     if (val != null) {
/* 1056:1712 */       addCount(1L, binCount);
/* 1057:     */     }
/* 1058:1713 */     return val;
/* 1059:     */   }
/* 1060:     */   
/* 1061:     */   public V computeIfPresent(K key, BiFun<? super K, ? super V, ? extends V> remappingFunction)
/* 1062:     */   {
/* 1063:1737 */     if ((key == null) || (remappingFunction == null)) {
/* 1064:1738 */       throw new NullPointerException();
/* 1065:     */     }
/* 1066:1739 */     int h = spread(key.hashCode());
/* 1067:1740 */     V val = null;
/* 1068:1741 */     int delta = 0;
/* 1069:1742 */     int binCount = 0;
/* 1070:1743 */     Node<K, V>[] tab = this.table;
/* 1071:     */     for (;;)
/* 1072:     */     {
/* 1073:     */       int n;
/* 1074:1745 */       if ((tab == null) || ((n = tab.length) == 0))
/* 1075:     */       {
/* 1076:1746 */         tab = initTable();
/* 1077:     */       }
/* 1078:     */       else
/* 1079:     */       {
/* 1080:     */         int n;
/* 1081:     */         int i;
/* 1082:     */         Node<K, V> f;
/* 1083:1747 */         if ((f = tabAt(tab, i = n - 1 & h)) == null) {
/* 1084:     */           break;
/* 1085:     */         }
/* 1086:     */         int fh;
/* 1087:1749 */         if ((fh = f.hash) == -1)
/* 1088:     */         {
/* 1089:1750 */           tab = helpTransfer(tab, f);
/* 1090:     */         }
/* 1091:     */         else
/* 1092:     */         {
/* 1093:1752 */           synchronized (f)
/* 1094:     */           {
/* 1095:1753 */             if (tabAt(tab, i) == f) {
/* 1096:1754 */               if (fh >= 0)
/* 1097:     */               {
/* 1098:1755 */                 binCount = 1;
/* 1099:1756 */                 Node<K, V> e = f;
/* 1100:1756 */                 for (Node<K, V> pred = null;; binCount++)
/* 1101:     */                 {
/* 1102:     */                   K ek;
/* 1103:1758 */                   if ((e.hash == h) && (((ek = e.key) == key) || ((ek != null) && (key.equals(ek)))))
/* 1104:     */                   {
/* 1105:1761 */                     val = remappingFunction.apply(key, e.val);
/* 1106:1762 */                     if (val != null)
/* 1107:     */                     {
/* 1108:1763 */                       e.val = val;
/* 1109:     */                     }
/* 1110:     */                     else
/* 1111:     */                     {
/* 1112:1765 */                       delta = -1;
/* 1113:1766 */                       Node<K, V> en = e.next;
/* 1114:1767 */                       if (pred != null) {
/* 1115:1768 */                         pred.next = en;
/* 1116:     */                       } else {
/* 1117:1770 */                         setTabAt(tab, i, en);
/* 1118:     */                       }
/* 1119:     */                     }
/* 1120:     */                   }
/* 1121:     */                   else
/* 1122:     */                   {
/* 1123:1774 */                     pred = e;
/* 1124:1775 */                     if ((e = e.next) == null) {
/* 1125:     */                       break;
/* 1126:     */                     }
/* 1127:     */                   }
/* 1128:     */                 }
/* 1129:     */               }
/* 1130:1779 */               else if ((f instanceof TreeBin))
/* 1131:     */               {
/* 1132:1780 */                 binCount = 2;
/* 1133:1781 */                 TreeBin<K, V> t = (TreeBin)f;
/* 1134:     */                 TreeNode<K, V> r;
/* 1135:     */                 TreeNode<K, V> p;
/* 1136:1783 */                 if (((r = t.root) != null) && ((p = r.findTreeNode(h, key, null)) != null))
/* 1137:     */                 {
/* 1138:1785 */                   val = remappingFunction.apply(key, p.val);
/* 1139:1786 */                   if (val != null)
/* 1140:     */                   {
/* 1141:1787 */                     p.val = val;
/* 1142:     */                   }
/* 1143:     */                   else
/* 1144:     */                   {
/* 1145:1789 */                     delta = -1;
/* 1146:1790 */                     if (t.removeTreeNode(p)) {
/* 1147:1791 */                       setTabAt(tab, i, untreeify(t.first));
/* 1148:     */                     }
/* 1149:     */                   }
/* 1150:     */                 }
/* 1151:     */               }
/* 1152:     */             }
/* 1153:     */           }
/* 1154:1797 */           if (binCount != 0) {
/* 1155:     */             break;
/* 1156:     */           }
/* 1157:     */         }
/* 1158:     */       }
/* 1159:     */     }
/* 1160:1801 */     if (delta != 0) {
/* 1161:1802 */       addCount(delta, binCount);
/* 1162:     */     }
/* 1163:1803 */     return val;
/* 1164:     */   }
/* 1165:     */   
/* 1166:     */   public V compute(K key, BiFun<? super K, ? super V, ? extends V> remappingFunction)
/* 1167:     */   {
/* 1168:1828 */     if ((key == null) || (remappingFunction == null)) {
/* 1169:1829 */       throw new NullPointerException();
/* 1170:     */     }
/* 1171:1830 */     int h = spread(key.hashCode());
/* 1172:1831 */     V val = null;
/* 1173:1832 */     int delta = 0;
/* 1174:1833 */     int binCount = 0;
/* 1175:1834 */     Node<K, V>[] tab = this.table;
/* 1176:     */     for (;;)
/* 1177:     */     {
/* 1178:     */       int n;
/* 1179:1836 */       if ((tab == null) || ((n = tab.length) == 0))
/* 1180:     */       {
/* 1181:1837 */         tab = initTable();
/* 1182:     */       }
/* 1183:     */       else
/* 1184:     */       {
/* 1185:     */         int n;
/* 1186:     */         int i;
/* 1187:     */         Node<K, V> f;
/* 1188:1838 */         if ((f = tabAt(tab, i = n - 1 & h)) == null)
/* 1189:     */         {
/* 1190:1839 */           Node<K, V> r = new ReservationNode();
/* 1191:1840 */           synchronized (r)
/* 1192:     */           {
/* 1193:1841 */             if (casTabAt(tab, i, null, r))
/* 1194:     */             {
/* 1195:1842 */               binCount = 1;
/* 1196:1843 */               Node<K, V> node = null;
/* 1197:     */               try
/* 1198:     */               {
/* 1199:1845 */                 if ((val = remappingFunction.apply(key, null)) != null)
/* 1200:     */                 {
/* 1201:1846 */                   delta = 1;
/* 1202:1847 */                   node = new Node(h, key, val, null);
/* 1203:     */                 }
/* 1204:     */               }
/* 1205:     */               finally
/* 1206:     */               {
/* 1207:1850 */                 setTabAt(tab, i, node);
/* 1208:     */               }
/* 1209:     */             }
/* 1210:     */           }
/* 1211:1854 */           if (binCount != 0) {
/* 1212:     */             break;
/* 1213:     */           }
/* 1214:     */         }
/* 1215:     */         else
/* 1216:     */         {
/* 1217:     */           int fh;
/* 1218:1857 */           if ((fh = f.hash) == -1)
/* 1219:     */           {
/* 1220:1858 */             tab = helpTransfer(tab, f);
/* 1221:     */           }
/* 1222:     */           else
/* 1223:     */           {
/* 1224:1860 */             synchronized (f)
/* 1225:     */             {
/* 1226:1861 */               if (tabAt(tab, i) == f) {
/* 1227:1862 */                 if (fh >= 0)
/* 1228:     */                 {
/* 1229:1863 */                   binCount = 1;
/* 1230:1864 */                   Node<K, V> e = f;
/* 1231:1864 */                   for (Node<K, V> pred = null;; binCount++)
/* 1232:     */                   {
/* 1233:     */                     Object ek;
/* 1234:1866 */                     if ((e.hash == h) && (((ek = e.key) == key) || ((ek != null) && (key.equals(ek)))))
/* 1235:     */                     {
/* 1236:1869 */                       val = remappingFunction.apply(key, e.val);
/* 1237:1870 */                       if (val != null)
/* 1238:     */                       {
/* 1239:1871 */                         e.val = val; break;
/* 1240:     */                       }
/* 1241:1873 */                       delta = -1;
/* 1242:1874 */                       Object en = e.next;
/* 1243:1875 */                       if (pred != null) {
/* 1244:1876 */                         pred.next = ((Node)en);
/* 1245:     */                       } else {
/* 1246:1878 */                         setTabAt(tab, i, (Node)en);
/* 1247:     */                       }
/* 1248:1880 */                       break;
/* 1249:     */                     }
/* 1250:1882 */                     pred = e;
/* 1251:1883 */                     if ((e = e.next) == null)
/* 1252:     */                     {
/* 1253:1884 */                       val = remappingFunction.apply(key, null);
/* 1254:1885 */                       if (val == null) {
/* 1255:     */                         break;
/* 1256:     */                       }
/* 1257:1886 */                       delta = 1;
/* 1258:1887 */                       pred.next = new Node(h, key, val, null); break;
/* 1259:     */                     }
/* 1260:     */                   }
/* 1261:     */                 }
/* 1262:1894 */                 else if ((f instanceof TreeBin))
/* 1263:     */                 {
/* 1264:1895 */                   binCount = 1;
/* 1265:1896 */                   TreeBin<K, V> t = (TreeBin)f;
/* 1266:     */                   TreeNode<K, V> r;
/* 1267:     */                   Object p;
/* 1268:     */                   TreeNode<K, V> p;
/* 1269:1898 */                   if ((r = t.root) != null) {
/* 1270:1899 */                     p = r.findTreeNode(h, key, null);
/* 1271:     */                   } else {
/* 1272:1901 */                     p = null;
/* 1273:     */                   }
/* 1274:1902 */                   Object pv = p == null ? null : p.val;
/* 1275:1903 */                   val = remappingFunction.apply(key, pv);
/* 1276:1904 */                   if (val != null)
/* 1277:     */                   {
/* 1278:1905 */                     if (p != null)
/* 1279:     */                     {
/* 1280:1906 */                       p.val = val;
/* 1281:     */                     }
/* 1282:     */                     else
/* 1283:     */                     {
/* 1284:1908 */                       delta = 1;
/* 1285:1909 */                       t.putTreeVal(h, key, val);
/* 1286:     */                     }
/* 1287:     */                   }
/* 1288:1912 */                   else if (p != null)
/* 1289:     */                   {
/* 1290:1913 */                     delta = -1;
/* 1291:1914 */                     if (t.removeTreeNode(p)) {
/* 1292:1915 */                       setTabAt(tab, i, untreeify(t.first));
/* 1293:     */                     }
/* 1294:     */                   }
/* 1295:     */                 }
/* 1296:     */               }
/* 1297:     */             }
/* 1298:1920 */             if (binCount != 0)
/* 1299:     */             {
/* 1300:1921 */               if (binCount < 8) {
/* 1301:     */                 break;
/* 1302:     */               }
/* 1303:1922 */               treeifyBin(tab, i); break;
/* 1304:     */             }
/* 1305:     */           }
/* 1306:     */         }
/* 1307:     */       }
/* 1308:     */     }
/* 1309:1927 */     if (delta != 0) {
/* 1310:1928 */       addCount(delta, binCount);
/* 1311:     */     }
/* 1312:1929 */     return val;
/* 1313:     */   }
/* 1314:     */   
/* 1315:     */   public V merge(K key, V value, BiFun<? super V, ? super V, ? extends V> remappingFunction)
/* 1316:     */   {
/* 1317:1953 */     if ((key == null) || (value == null) || (remappingFunction == null)) {
/* 1318:1954 */       throw new NullPointerException();
/* 1319:     */     }
/* 1320:1955 */     int h = spread(key.hashCode());
/* 1321:1956 */     V val = null;
/* 1322:1957 */     int delta = 0;
/* 1323:1958 */     int binCount = 0;
/* 1324:1959 */     Node<K, V>[] tab = this.table;
/* 1325:     */     for (;;)
/* 1326:     */     {
/* 1327:     */       int n;
/* 1328:1961 */       if ((tab == null) || ((n = tab.length) == 0))
/* 1329:     */       {
/* 1330:1962 */         tab = initTable();
/* 1331:     */       }
/* 1332:     */       else
/* 1333:     */       {
/* 1334:     */         int n;
/* 1335:     */         int i;
/* 1336:     */         Node<K, V> f;
/* 1337:1963 */         if ((f = tabAt(tab, i = n - 1 & h)) == null)
/* 1338:     */         {
/* 1339:1964 */           if (casTabAt(tab, i, null, new Node(h, key, value, null)))
/* 1340:     */           {
/* 1341:1965 */             delta = 1;
/* 1342:1966 */             val = value;
/* 1343:1967 */             break;
/* 1344:     */           }
/* 1345:     */         }
/* 1346:     */         else
/* 1347:     */         {
/* 1348:     */           int fh;
/* 1349:1970 */           if ((fh = f.hash) == -1)
/* 1350:     */           {
/* 1351:1971 */             tab = helpTransfer(tab, f);
/* 1352:     */           }
/* 1353:     */           else
/* 1354:     */           {
/* 1355:1973 */             synchronized (f)
/* 1356:     */             {
/* 1357:1974 */               if (tabAt(tab, i) == f) {
/* 1358:1975 */                 if (fh >= 0)
/* 1359:     */                 {
/* 1360:1976 */                   binCount = 1;
/* 1361:1977 */                   Node<K, V> e = f;
/* 1362:1977 */                   for (Node<K, V> pred = null;; binCount++)
/* 1363:     */                   {
/* 1364:     */                     K ek;
/* 1365:1979 */                     if ((e.hash == h) && (((ek = e.key) == key) || ((ek != null) && (key.equals(ek)))))
/* 1366:     */                     {
/* 1367:1982 */                       val = remappingFunction.apply(e.val, value);
/* 1368:1983 */                       if (val != null)
/* 1369:     */                       {
/* 1370:1984 */                         e.val = val; break;
/* 1371:     */                       }
/* 1372:1986 */                       delta = -1;
/* 1373:1987 */                       Node<K, V> en = e.next;
/* 1374:1988 */                       if (pred != null) {
/* 1375:1989 */                         pred.next = en;
/* 1376:     */                       } else {
/* 1377:1991 */                         setTabAt(tab, i, en);
/* 1378:     */                       }
/* 1379:1993 */                       break;
/* 1380:     */                     }
/* 1381:1995 */                     pred = e;
/* 1382:1996 */                     if ((e = e.next) == null)
/* 1383:     */                     {
/* 1384:1997 */                       delta = 1;
/* 1385:1998 */                       val = value;
/* 1386:1999 */                       pred.next = new Node(h, key, val, null);
/* 1387:     */                       
/* 1388:2001 */                       break;
/* 1389:     */                     }
/* 1390:     */                   }
/* 1391:     */                 }
/* 1392:2005 */                 else if ((f instanceof TreeBin))
/* 1393:     */                 {
/* 1394:2006 */                   binCount = 2;
/* 1395:2007 */                   TreeBin<K, V> t = (TreeBin)f;
/* 1396:2008 */                   TreeNode<K, V> r = t.root;
/* 1397:2009 */                   TreeNode<K, V> p = r == null ? null : r.findTreeNode(h, key, null);
/* 1398:     */                   
/* 1399:2011 */                   val = p == null ? value : remappingFunction.apply(p.val, value);
/* 1400:2013 */                   if (val != null)
/* 1401:     */                   {
/* 1402:2014 */                     if (p != null)
/* 1403:     */                     {
/* 1404:2015 */                       p.val = val;
/* 1405:     */                     }
/* 1406:     */                     else
/* 1407:     */                     {
/* 1408:2017 */                       delta = 1;
/* 1409:2018 */                       t.putTreeVal(h, key, val);
/* 1410:     */                     }
/* 1411:     */                   }
/* 1412:2021 */                   else if (p != null)
/* 1413:     */                   {
/* 1414:2022 */                     delta = -1;
/* 1415:2023 */                     if (t.removeTreeNode(p)) {
/* 1416:2024 */                       setTabAt(tab, i, untreeify(t.first));
/* 1417:     */                     }
/* 1418:     */                   }
/* 1419:     */                 }
/* 1420:     */               }
/* 1421:     */             }
/* 1422:2029 */             if (binCount != 0)
/* 1423:     */             {
/* 1424:2030 */               if (binCount < 8) {
/* 1425:     */                 break;
/* 1426:     */               }
/* 1427:2031 */               treeifyBin(tab, i); break;
/* 1428:     */             }
/* 1429:     */           }
/* 1430:     */         }
/* 1431:     */       }
/* 1432:     */     }
/* 1433:2036 */     if (delta != 0) {
/* 1434:2037 */       addCount(delta, binCount);
/* 1435:     */     }
/* 1436:2038 */     return val;
/* 1437:     */   }
/* 1438:     */   
/* 1439:     */   @Deprecated
/* 1440:     */   public boolean contains(Object value)
/* 1441:     */   {
/* 1442:2059 */     return containsValue(value);
/* 1443:     */   }
/* 1444:     */   
/* 1445:     */   public Enumeration<K> keys()
/* 1446:     */   {
/* 1447:     */     Node<K, V>[] t;
/* 1448:2070 */     int f = (t = this.table) == null ? 0 : t.length;
/* 1449:2071 */     return new KeyIterator(t, f, 0, f, this);
/* 1450:     */   }
/* 1451:     */   
/* 1452:     */   public Enumeration<V> elements()
/* 1453:     */   {
/* 1454:     */     Node<K, V>[] t;
/* 1455:2082 */     int f = (t = this.table) == null ? 0 : t.length;
/* 1456:2083 */     return new ValueIterator(t, f, 0, f, this);
/* 1457:     */   }
/* 1458:     */   
/* 1459:     */   public long mappingCount()
/* 1460:     */   {
/* 1461:2099 */     long n = sumCount();
/* 1462:2100 */     return n < 0L ? 0L : n;
/* 1463:     */   }
/* 1464:     */   
/* 1465:     */   public static <K> KeySetView<K, Boolean> newKeySet()
/* 1466:     */   {
/* 1467:2111 */     return new KeySetView(new ConcurrentHashMapV8(), Boolean.TRUE);
/* 1468:     */   }
/* 1469:     */   
/* 1470:     */   public static <K> KeySetView<K, Boolean> newKeySet(int initialCapacity)
/* 1471:     */   {
/* 1472:2127 */     return new KeySetView(new ConcurrentHashMapV8(initialCapacity), Boolean.TRUE);
/* 1473:     */   }
/* 1474:     */   
/* 1475:     */   public KeySetView<K, V> keySet(V mappedValue)
/* 1476:     */   {
/* 1477:2143 */     if (mappedValue == null) {
/* 1478:2144 */       throw new NullPointerException();
/* 1479:     */     }
/* 1480:2145 */     return new KeySetView(this, mappedValue);
/* 1481:     */   }
/* 1482:     */   
/* 1483:     */   static final class ForwardingNode<K, V>
/* 1484:     */     extends ConcurrentHashMapV8.Node<K, V>
/* 1485:     */   {
/* 1486:     */     final ConcurrentHashMapV8.Node<K, V>[] nextTable;
/* 1487:     */     
/* 1488:     */     ForwardingNode(ConcurrentHashMapV8.Node<K, V>[] tab)
/* 1489:     */     {
/* 1490:2156 */       super(null, null, null);
/* 1491:2157 */       this.nextTable = tab;
/* 1492:     */     }
/* 1493:     */     
/* 1494:     */     ConcurrentHashMapV8.Node<K, V> find(int h, Object k)
/* 1495:     */     {
/* 1496:2162 */       ConcurrentHashMapV8.Node<K, V>[] tab = this.nextTable;
/* 1497:     */       int n;
/* 1498:     */       ConcurrentHashMapV8.Node<K, V> e;
/* 1499:2164 */       if ((k == null) || (tab == null) || ((n = tab.length) == 0) || ((e = ConcurrentHashMapV8.tabAt(tab, n - 1 & h)) == null)) {
/* 1500:2166 */         return null;
/* 1501:     */       }
/* 1502:     */       for (;;)
/* 1503:     */       {
/* 1504:     */         int n;
/* 1505:     */         ConcurrentHashMapV8.Node<K, V> e;
/* 1506:     */         int eh;
/* 1507:     */         K ek;
/* 1508:2169 */         if (((eh = e.hash) == h) && (((ek = e.key) == k) || ((ek != null) && (k.equals(ek))))) {
/* 1509:2171 */           return e;
/* 1510:     */         }
/* 1511:2172 */         if (eh < 0)
/* 1512:     */         {
/* 1513:2173 */           if ((e instanceof ForwardingNode))
/* 1514:     */           {
/* 1515:2174 */             tab = ((ForwardingNode)e).nextTable;
/* 1516:2175 */             break;
/* 1517:     */           }
/* 1518:2178 */           return e.find(h, k);
/* 1519:     */         }
/* 1520:2180 */         if ((e = e.next) == null) {
/* 1521:2181 */           return null;
/* 1522:     */         }
/* 1523:     */       }
/* 1524:     */     }
/* 1525:     */   }
/* 1526:     */   
/* 1527:     */   static final class ReservationNode<K, V>
/* 1528:     */     extends ConcurrentHashMapV8.Node<K, V>
/* 1529:     */   {
/* 1530:     */     ReservationNode()
/* 1531:     */     {
/* 1532:2192 */       super(null, null, null);
/* 1533:     */     }
/* 1534:     */     
/* 1535:     */     ConcurrentHashMapV8.Node<K, V> find(int h, Object k)
/* 1536:     */     {
/* 1537:2196 */       return null;
/* 1538:     */     }
/* 1539:     */   }
/* 1540:     */   
/* 1541:     */   private final Node<K, V>[] initTable()
/* 1542:     */   {
/* 1543:     */     Node<K, V>[] tab;
/* 1544:2207 */     while (((tab = this.table) == null) || (tab.length == 0))
/* 1545:     */     {
/* 1546:     */       int sc;
/* 1547:2208 */       if ((sc = this.sizeCtl) < 0) {
/* 1548:2209 */         Thread.yield();
/* 1549:2210 */       } else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
/* 1550:     */         try
/* 1551:     */         {
/* 1552:2212 */           if (((tab = this.table) == null) || (tab.length == 0))
/* 1553:     */           {
/* 1554:2213 */             int n = sc > 0 ? sc : 16;
/* 1555:     */             
/* 1556:2215 */             Node<K, V>[] nt = (Node[])new Node[n];
/* 1557:2216 */             this.table = (tab = nt);
/* 1558:2217 */             sc = n - (n >>> 2);
/* 1559:     */           }
/* 1560:     */         }
/* 1561:     */         finally
/* 1562:     */         {
/* 1563:2220 */           this.sizeCtl = sc;
/* 1564:     */         }
/* 1565:     */       }
/* 1566:     */     }
/* 1567:2225 */     return tab;
/* 1568:     */   }
/* 1569:     */   
/* 1570:     */   private final void addCount(long x, int check)
/* 1571:     */   {
/* 1572:     */     CounterCell[] as;
/* 1573:     */     long b;
/* 1574:     */     long s;
/* 1575:     */     long s;
/* 1576:2240 */     if (((as = this.counterCells) != null) || (!U.compareAndSwapLong(this, BASECOUNT, b = this.baseCount, s = b + x)))
/* 1577:     */     {
/* 1578:2243 */       boolean uncontended = true;
/* 1579:2244 */       InternalThreadLocalMap threadLocals = InternalThreadLocalMap.get();
/* 1580:     */       IntegerHolder hc;
/* 1581:     */       int m;
/* 1582:     */       CounterCell a;
/* 1583:     */       long v;
/* 1584:2245 */       if (((hc = threadLocals.counterHashCode()) == null) || (as == null) || ((m = as.length - 1) < 0) || ((a = as[(m & hc.value)]) == null) || (!(uncontended = U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))))
/* 1585:     */       {
/* 1586:2250 */         fullAddCount(threadLocals, x, hc, uncontended); return;
/* 1587:     */       }
/* 1588:     */       int m;
/* 1589:     */       long v;
/* 1590:     */       CounterCell a;
/* 1591:2253 */       if (check <= 1) {
/* 1592:2254 */         return;
/* 1593:     */       }
/* 1594:2255 */       s = sumCount();
/* 1595:     */     }
/* 1596:2257 */     if (check >= 0)
/* 1597:     */     {
/* 1598:     */       int sc;
/* 1599:     */       Node<K, V>[] tab;
/* 1600:2259 */       while ((s >= (sc = this.sizeCtl)) && ((tab = this.table) != null) && (tab.length < 1073741824))
/* 1601:     */       {
/* 1602:2261 */         if (sc < 0)
/* 1603:     */         {
/* 1604:     */           Node<K, V>[] nt;
/* 1605:2262 */           if ((sc == -1) || (this.transferIndex <= this.transferOrigin) || ((nt = this.nextTable) == null)) {
/* 1606:     */             break;
/* 1607:     */           }
/* 1608:2265 */           if (U.compareAndSwapInt(this, SIZECTL, sc, sc - 1)) {
/* 1609:2266 */             transfer(tab, nt);
/* 1610:     */           }
/* 1611:     */         }
/* 1612:2268 */         else if (U.compareAndSwapInt(this, SIZECTL, sc, -2))
/* 1613:     */         {
/* 1614:2269 */           transfer(tab, null);
/* 1615:     */         }
/* 1616:2270 */         s = sumCount();
/* 1617:     */       }
/* 1618:     */     }
/* 1619:     */   }
/* 1620:     */   
/* 1621:     */   final Node<K, V>[] helpTransfer(Node<K, V>[] tab, Node<K, V> f)
/* 1622:     */   {
/* 1623:     */     Node<K, V>[] nextTab;
/* 1624:2280 */     if (((f instanceof ForwardingNode)) && ((nextTab = ((ForwardingNode)f).nextTable) != null))
/* 1625:     */     {
/* 1626:     */       int sc;
/* 1627:2282 */       if ((nextTab == this.nextTable) && (tab == this.table) && (this.transferIndex > this.transferOrigin) && ((sc = this.sizeCtl) < -1) && (U.compareAndSwapInt(this, SIZECTL, sc, sc - 1))) {
/* 1628:2285 */         transfer(tab, nextTab);
/* 1629:     */       }
/* 1630:2286 */       return nextTab;
/* 1631:     */     }
/* 1632:2288 */     return this.table;
/* 1633:     */   }
/* 1634:     */   
/* 1635:     */   private final void tryPresize(int size)
/* 1636:     */   {
/* 1637:2297 */     int c = size >= 536870912 ? 1073741824 : tableSizeFor(size + (size >>> 1) + 1);
/* 1638:     */     int sc;
/* 1639:2300 */     while ((sc = this.sizeCtl) >= 0)
/* 1640:     */     {
/* 1641:2301 */       Node<K, V>[] tab = this.table;
/* 1642:     */       int n;
/* 1643:     */       int n;
/* 1644:2302 */       if ((tab == null) || ((n = tab.length) == 0))
/* 1645:     */       {
/* 1646:2303 */         n = sc > c ? sc : c;
/* 1647:2304 */         if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
/* 1648:     */           try
/* 1649:     */           {
/* 1650:2306 */             if (this.table == tab)
/* 1651:     */             {
/* 1652:2308 */               Node<K, V>[] nt = (Node[])new Node[n];
/* 1653:2309 */               this.table = nt;
/* 1654:2310 */               sc = n - (n >>> 2);
/* 1655:     */             }
/* 1656:     */           }
/* 1657:     */           finally
/* 1658:     */           {
/* 1659:2313 */             this.sizeCtl = sc;
/* 1660:     */           }
/* 1661:     */         }
/* 1662:     */       }
/* 1663:     */       else
/* 1664:     */       {
/* 1665:2317 */         if ((c <= sc) || (n >= 1073741824)) {
/* 1666:     */           break;
/* 1667:     */         }
/* 1668:2319 */         if ((tab == this.table) && (U.compareAndSwapInt(this, SIZECTL, sc, -2))) {
/* 1669:2321 */           transfer(tab, null);
/* 1670:     */         }
/* 1671:     */       }
/* 1672:     */     }
/* 1673:     */   }
/* 1674:     */   
/* 1675:     */   private final void transfer(Node<K, V>[] tab, Node<K, V>[] nextTab)
/* 1676:     */   {
/* 1677:2330 */     int n = tab.length;
/* 1678:     */     int stride;
/* 1679:2331 */     if ((stride = NCPU > 1 ? (n >>> 3) / NCPU : n) < 16) {
/* 1680:2332 */       stride = 16;
/* 1681:     */     }
/* 1682:     */     ForwardingNode<K, V> rev;
/* 1683:     */     int k;
/* 1684:2333 */     if (nextTab == null)
/* 1685:     */     {
/* 1686:     */       try
/* 1687:     */       {
/* 1688:2336 */         Node<K, V>[] nt = (Node[])new Node[n << 1];
/* 1689:2337 */         nextTab = nt;
/* 1690:     */       }
/* 1691:     */       catch (Throwable ex)
/* 1692:     */       {
/* 1693:2339 */         this.sizeCtl = 2147483647;
/* 1694:2340 */         return;
/* 1695:     */       }
/* 1696:2342 */       this.nextTable = nextTab;
/* 1697:2343 */       this.transferOrigin = n;
/* 1698:2344 */       this.transferIndex = n;
/* 1699:2345 */       rev = new ForwardingNode(tab);
/* 1700:2346 */       for (k = n; k > 0;)
/* 1701:     */       {
/* 1702:2347 */         int nextk = k > stride ? k - stride : 0;
/* 1703:2348 */         for (int m = nextk; m < k; m++) {
/* 1704:2349 */           nextTab[m] = rev;
/* 1705:     */         }
/* 1706:2350 */         for (int m = n + nextk; m < n + k; m++) {
/* 1707:2351 */           nextTab[m] = rev;
/* 1708:     */         }
/* 1709:2352 */         U.putOrderedInt(this, TRANSFERORIGIN, k = nextk);
/* 1710:     */       }
/* 1711:     */     }
/* 1712:2355 */     int nextn = nextTab.length;
/* 1713:2356 */     ForwardingNode<K, V> fwd = new ForwardingNode(nextTab);
/* 1714:2357 */     boolean advance = true;
/* 1715:2358 */     boolean finishing = false;
/* 1716:2359 */     int i = 0;int bound = 0;
/* 1717:     */     for (;;)
/* 1718:     */     {
/* 1719:2361 */       if (advance)
/* 1720:     */       {
/* 1721:2362 */         i--;
/* 1722:2362 */         if ((i >= bound) || (finishing))
/* 1723:     */         {
/* 1724:2363 */           advance = false;
/* 1725:     */         }
/* 1726:     */         else
/* 1727:     */         {
/* 1728:     */           int nextIndex;
/* 1729:2364 */           if ((nextIndex = this.transferIndex) <= this.transferOrigin)
/* 1730:     */           {
/* 1731:2365 */             i = -1;
/* 1732:2366 */             advance = false;
/* 1733:     */           }
/* 1734:     */           else
/* 1735:     */           {
/* 1736:     */             int nextBound;
/* 1737:2368 */             if (U.compareAndSwapInt(this, TRANSFERINDEX, nextIndex, nextBound = nextIndex > stride ? nextIndex - stride : 0))
/* 1738:     */             {
/* 1739:2372 */               bound = nextBound;
/* 1740:2373 */               i = nextIndex - 1;
/* 1741:2374 */               advance = false;
/* 1742:     */             }
/* 1743:     */           }
/* 1744:     */         }
/* 1745:     */       }
/* 1746:2377 */       else if ((i < 0) || (i >= n) || (i + n >= nextn))
/* 1747:     */       {
/* 1748:2378 */         if (finishing)
/* 1749:     */         {
/* 1750:2379 */           this.nextTable = null;
/* 1751:2380 */           this.table = nextTab;
/* 1752:2381 */           this.sizeCtl = ((n << 1) - (n >>> 1));
/* 1753:2382 */           return;
/* 1754:     */         }
/* 1755:     */         int sc;
/* 1756:2385 */         while (!U.compareAndSwapInt(this, SIZECTL, sc = this.sizeCtl, ++sc)) {}
/* 1757:2386 */         if (sc != -1) {
/* 1758:2387 */           return;
/* 1759:     */         }
/* 1760:2388 */         finishing = advance = 1;
/* 1761:2389 */         i = n;
/* 1762:     */       }
/* 1763:     */       else
/* 1764:     */       {
/* 1765:     */         Node<K, V> f;
/* 1766:2394 */         if ((f = tabAt(tab, i)) == null)
/* 1767:     */         {
/* 1768:2395 */           if (casTabAt(tab, i, null, fwd))
/* 1769:     */           {
/* 1770:2396 */             setTabAt(nextTab, i, null);
/* 1771:2397 */             setTabAt(nextTab, i + n, null);
/* 1772:2398 */             advance = true;
/* 1773:     */           }
/* 1774:     */         }
/* 1775:     */         else
/* 1776:     */         {
/* 1777:     */           int fh;
/* 1778:2401 */           if ((fh = f.hash) == -1) {
/* 1779:2402 */             advance = true;
/* 1780:     */           } else {
/* 1781:2404 */             synchronized (f)
/* 1782:     */             {
/* 1783:2405 */               if (tabAt(tab, i) == f) {
/* 1784:2407 */                 if (fh >= 0)
/* 1785:     */                 {
/* 1786:2408 */                   int runBit = fh & n;
/* 1787:2409 */                   Node<K, V> lastRun = f;
/* 1788:2410 */                   for (Node<K, V> p = f.next; p != null; p = p.next)
/* 1789:     */                   {
/* 1790:2411 */                     int b = p.hash & n;
/* 1791:2412 */                     if (b != runBit)
/* 1792:     */                     {
/* 1793:2413 */                       runBit = b;
/* 1794:2414 */                       lastRun = p;
/* 1795:     */                     }
/* 1796:     */                   }
/* 1797:     */                   Node<K, V> hn;
/* 1798:     */                   Node<K, V> hn;
/* 1799:     */                   Node<K, V> ln;
/* 1800:2417 */                   if (runBit == 0)
/* 1801:     */                   {
/* 1802:2418 */                     Node<K, V> ln = lastRun;
/* 1803:2419 */                     hn = null;
/* 1804:     */                   }
/* 1805:     */                   else
/* 1806:     */                   {
/* 1807:2422 */                     hn = lastRun;
/* 1808:2423 */                     ln = null;
/* 1809:     */                   }
/* 1810:2425 */                   for (Node<K, V> p = f; p != lastRun; p = p.next)
/* 1811:     */                   {
/* 1812:2426 */                     int ph = p.hash;K pk = p.key;V pv = p.val;
/* 1813:2427 */                     if ((ph & n) == 0) {
/* 1814:2428 */                       ln = new Node(ph, pk, pv, ln);
/* 1815:     */                     } else {
/* 1816:2430 */                       hn = new Node(ph, pk, pv, hn);
/* 1817:     */                     }
/* 1818:     */                   }
/* 1819:2432 */                   setTabAt(nextTab, i, ln);
/* 1820:2433 */                   setTabAt(nextTab, i + n, hn);
/* 1821:2434 */                   setTabAt(tab, i, fwd);
/* 1822:2435 */                   advance = true;
/* 1823:     */                 }
/* 1824:2437 */                 else if ((f instanceof TreeBin))
/* 1825:     */                 {
/* 1826:2438 */                   TreeBin<K, V> t = (TreeBin)f;
/* 1827:2439 */                   TreeNode<K, V> lo = null;TreeNode<K, V> loTail = null;
/* 1828:2440 */                   TreeNode<K, V> hi = null;TreeNode<K, V> hiTail = null;
/* 1829:2441 */                   int lc = 0;int hc = 0;
/* 1830:2442 */                   for (Node<K, V> e = t.first; e != null; e = e.next)
/* 1831:     */                   {
/* 1832:2443 */                     int h = e.hash;
/* 1833:2444 */                     TreeNode<K, V> p = new TreeNode(h, e.key, e.val, null, null);
/* 1834:2446 */                     if ((h & n) == 0)
/* 1835:     */                     {
/* 1836:2447 */                       if ((p.prev = loTail) == null) {
/* 1837:2448 */                         lo = p;
/* 1838:     */                       } else {
/* 1839:2450 */                         loTail.next = p;
/* 1840:     */                       }
/* 1841:2451 */                       loTail = p;
/* 1842:2452 */                       lc++;
/* 1843:     */                     }
/* 1844:     */                     else
/* 1845:     */                     {
/* 1846:2455 */                       if ((p.prev = hiTail) == null) {
/* 1847:2456 */                         hi = p;
/* 1848:     */                       } else {
/* 1849:2458 */                         hiTail.next = p;
/* 1850:     */                       }
/* 1851:2459 */                       hiTail = p;
/* 1852:2460 */                       hc++;
/* 1853:     */                     }
/* 1854:     */                   }
/* 1855:2463 */                   Node<K, V> ln = hc != 0 ? new TreeBin(lo) : lc <= 6 ? untreeify(lo) : t;
/* 1856:     */                   
/* 1857:2465 */                   Node<K, V> hn = lc != 0 ? new TreeBin(hi) : hc <= 6 ? untreeify(hi) : t;
/* 1858:     */                   
/* 1859:2467 */                   setTabAt(nextTab, i, ln);
/* 1860:2468 */                   setTabAt(nextTab, i + n, hn);
/* 1861:2469 */                   setTabAt(tab, i, fwd);
/* 1862:2470 */                   advance = true;
/* 1863:     */                 }
/* 1864:     */               }
/* 1865:     */             }
/* 1866:     */           }
/* 1867:     */         }
/* 1868:     */       }
/* 1869:     */     }
/* 1870:     */   }
/* 1871:     */   
/* 1872:     */   private final void treeifyBin(Node<K, V>[] tab, int index)
/* 1873:     */   {
/* 1874:2486 */     if (tab != null)
/* 1875:     */     {
/* 1876:     */       int n;
/* 1877:2487 */       if ((n = tab.length) < 64)
/* 1878:     */       {
/* 1879:     */         int sc;
/* 1880:2488 */         if ((tab == this.table) && ((sc = this.sizeCtl) >= 0) && (U.compareAndSwapInt(this, SIZECTL, sc, -2))) {
/* 1881:2490 */           transfer(tab, null);
/* 1882:     */         }
/* 1883:     */       }
/* 1884:     */       else
/* 1885:     */       {
/* 1886:     */         Node<K, V> b;
/* 1887:2492 */         if (((b = tabAt(tab, index)) != null) && (b.hash >= 0)) {
/* 1888:2493 */           synchronized (b)
/* 1889:     */           {
/* 1890:2494 */             if (tabAt(tab, index) == b)
/* 1891:     */             {
/* 1892:2495 */               TreeNode<K, V> hd = null;TreeNode<K, V> tl = null;
/* 1893:2496 */               for (Node<K, V> e = b; e != null; e = e.next)
/* 1894:     */               {
/* 1895:2497 */                 TreeNode<K, V> p = new TreeNode(e.hash, e.key, e.val, null, null);
/* 1896:2500 */                 if ((p.prev = tl) == null) {
/* 1897:2501 */                   hd = p;
/* 1898:     */                 } else {
/* 1899:2503 */                   tl.next = p;
/* 1900:     */                 }
/* 1901:2504 */                 tl = p;
/* 1902:     */               }
/* 1903:2506 */               setTabAt(tab, index, new TreeBin(hd));
/* 1904:     */             }
/* 1905:     */           }
/* 1906:     */         }
/* 1907:     */       }
/* 1908:     */     }
/* 1909:     */   }
/* 1910:     */   
/* 1911:     */   static <K, V> Node<K, V> untreeify(Node<K, V> b)
/* 1912:     */   {
/* 1913:2517 */     Node<K, V> hd = null;Node<K, V> tl = null;
/* 1914:2518 */     for (Node<K, V> q = b; q != null; q = q.next)
/* 1915:     */     {
/* 1916:2519 */       Node<K, V> p = new Node(q.hash, q.key, q.val, null);
/* 1917:2520 */       if (tl == null) {
/* 1918:2521 */         hd = p;
/* 1919:     */       } else {
/* 1920:2523 */         tl.next = p;
/* 1921:     */       }
/* 1922:2524 */       tl = p;
/* 1923:     */     }
/* 1924:2526 */     return hd;
/* 1925:     */   }
/* 1926:     */   
/* 1927:     */   static final class TreeNode<K, V>
/* 1928:     */     extends ConcurrentHashMapV8.Node<K, V>
/* 1929:     */   {
/* 1930:     */     TreeNode<K, V> parent;
/* 1931:     */     TreeNode<K, V> left;
/* 1932:     */     TreeNode<K, V> right;
/* 1933:     */     TreeNode<K, V> prev;
/* 1934:     */     boolean red;
/* 1935:     */     
/* 1936:     */     TreeNode(int hash, K key, V val, ConcurrentHashMapV8.Node<K, V> next, TreeNode<K, V> parent)
/* 1937:     */     {
/* 1938:2543 */       super(key, val, next);
/* 1939:2544 */       this.parent = parent;
/* 1940:     */     }
/* 1941:     */     
/* 1942:     */     ConcurrentHashMapV8.Node<K, V> find(int h, Object k)
/* 1943:     */     {
/* 1944:2548 */       return findTreeNode(h, k, null);
/* 1945:     */     }
/* 1946:     */     
/* 1947:     */     final TreeNode<K, V> findTreeNode(int h, Object k, Class<?> kc)
/* 1948:     */     {
/* 1949:2556 */       if (k != null)
/* 1950:     */       {
/* 1951:2557 */         TreeNode<K, V> p = this;
/* 1952:     */         do
/* 1953:     */         {
/* 1954:2560 */           TreeNode<K, V> pl = p.left;TreeNode<K, V> pr = p.right;
/* 1955:     */           int ph;
/* 1956:2561 */           if ((ph = p.hash) > h)
/* 1957:     */           {
/* 1958:2562 */             p = pl;
/* 1959:     */           }
/* 1960:2563 */           else if (ph < h)
/* 1961:     */           {
/* 1962:2564 */             p = pr;
/* 1963:     */           }
/* 1964:     */           else
/* 1965:     */           {
/* 1966:     */             K pk;
/* 1967:2565 */             if (((pk = p.key) == k) || ((pk != null) && (k.equals(pk)))) {
/* 1968:2566 */               return p;
/* 1969:     */             }
/* 1970:2567 */             if ((pl == null) && (pr == null)) {
/* 1971:     */               break;
/* 1972:     */             }
/* 1973:     */             int dir;
/* 1974:2569 */             if (((kc != null) || ((kc = ConcurrentHashMapV8.comparableClassFor(k)) != null)) && ((dir = ConcurrentHashMapV8.compareComparables(kc, k, pk)) != 0))
/* 1975:     */             {
/* 1976:2572 */               p = dir < 0 ? pl : pr;
/* 1977:     */             }
/* 1978:2573 */             else if (pl == null)
/* 1979:     */             {
/* 1980:2574 */               p = pr;
/* 1981:     */             }
/* 1982:     */             else
/* 1983:     */             {
/* 1984:     */               TreeNode<K, V> q;
/* 1985:2575 */               if ((pr == null) || ((q = pr.findTreeNode(h, k, kc)) == null))
/* 1986:     */               {
/* 1987:2577 */                 p = pl;
/* 1988:     */               }
/* 1989:     */               else
/* 1990:     */               {
/* 1991:     */                 TreeNode<K, V> q;
/* 1992:2579 */                 return q;
/* 1993:     */               }
/* 1994:     */             }
/* 1995:     */           }
/* 1996:2580 */         } while (p != null);
/* 1997:     */       }
/* 1998:2582 */       return null;
/* 1999:     */     }
/* 2000:     */   }
/* 2001:     */   
/* 2002:     */   static final class TreeBin<K, V>
/* 2003:     */     extends ConcurrentHashMapV8.Node<K, V>
/* 2004:     */   {
/* 2005:     */     ConcurrentHashMapV8.TreeNode<K, V> root;
/* 2006:     */     volatile ConcurrentHashMapV8.TreeNode<K, V> first;
/* 2007:     */     volatile Thread waiter;
/* 2008:     */     volatile int lockState;
/* 2009:     */     static final int WRITER = 1;
/* 2010:     */     static final int WAITER = 2;
/* 2011:     */     static final int READER = 4;
/* 2012:     */     private static final Unsafe U;
/* 2013:     */     private static final long LOCKSTATE;
/* 2014:     */     
/* 2015:     */     TreeBin(ConcurrentHashMapV8.TreeNode<K, V> b)
/* 2016:     */     {
/* 2017:2609 */       super(null, null, null);
/* 2018:2610 */       this.first = b;
/* 2019:2611 */       ConcurrentHashMapV8.TreeNode<K, V> r = null;
/* 2020:     */       ConcurrentHashMapV8.TreeNode<K, V> next;
/* 2021:2612 */       for (ConcurrentHashMapV8.TreeNode<K, V> x = b; x != null; x = next)
/* 2022:     */       {
/* 2023:2613 */         next = (ConcurrentHashMapV8.TreeNode)x.next;
/* 2024:2614 */         x.left = (x.right = null);
/* 2025:2615 */         if (r == null)
/* 2026:     */         {
/* 2027:2616 */           x.parent = null;
/* 2028:2617 */           x.red = false;
/* 2029:2618 */           r = x;
/* 2030:     */         }
/* 2031:     */         else
/* 2032:     */         {
/* 2033:2621 */           Object key = x.key;
/* 2034:2622 */           int hash = x.hash;
/* 2035:2623 */           Class<?> kc = null;
/* 2036:2624 */           ConcurrentHashMapV8.TreeNode<K, V> p = r;
/* 2037:     */           for (;;)
/* 2038:     */           {
/* 2039:     */             int ph;
/* 2040:     */             int dir;
/* 2041:     */             int dir;
/* 2042:2626 */             if ((ph = p.hash) > hash)
/* 2043:     */             {
/* 2044:2627 */               dir = -1;
/* 2045:     */             }
/* 2046:     */             else
/* 2047:     */             {
/* 2048:     */               int dir;
/* 2049:2628 */               if (ph < hash)
/* 2050:     */               {
/* 2051:2629 */                 dir = 1;
/* 2052:     */               }
/* 2053:     */               else
/* 2054:     */               {
/* 2055:     */                 int dir;
/* 2056:2630 */                 if ((kc != null) || ((kc = ConcurrentHashMapV8.comparableClassFor(key)) != null)) {
/* 2057:2632 */                   dir = ConcurrentHashMapV8.compareComparables(kc, key, p.key);
/* 2058:     */                 } else {
/* 2059:2634 */                   dir = 0;
/* 2060:     */                 }
/* 2061:     */               }
/* 2062:     */             }
/* 2063:2635 */             ConcurrentHashMapV8.TreeNode<K, V> xp = p;
/* 2064:2636 */             if ((p = dir <= 0 ? p.left : p.right) == null)
/* 2065:     */             {
/* 2066:2637 */               x.parent = xp;
/* 2067:2638 */               if (dir <= 0) {
/* 2068:2639 */                 xp.left = x;
/* 2069:     */               } else {
/* 2070:2641 */                 xp.right = x;
/* 2071:     */               }
/* 2072:2642 */               r = balanceInsertion(r, x);
/* 2073:2643 */               break;
/* 2074:     */             }
/* 2075:     */           }
/* 2076:     */         }
/* 2077:     */       }
/* 2078:2648 */       this.root = r;
/* 2079:     */     }
/* 2080:     */     
/* 2081:     */     private final void lockRoot()
/* 2082:     */     {
/* 2083:2655 */       if (!U.compareAndSwapInt(this, LOCKSTATE, 0, 1)) {
/* 2084:2656 */         contendedLock();
/* 2085:     */       }
/* 2086:     */     }
/* 2087:     */     
/* 2088:     */     private final void unlockRoot()
/* 2089:     */     {
/* 2090:2663 */       this.lockState = 0;
/* 2091:     */     }
/* 2092:     */     
/* 2093:     */     private final void contendedLock()
/* 2094:     */     {
/* 2095:2670 */       boolean waiting = false;
/* 2096:     */       for (;;)
/* 2097:     */       {
/* 2098:     */         int s;
/* 2099:2672 */         if (((s = this.lockState) & 0x1) == 0)
/* 2100:     */         {
/* 2101:2673 */           if (U.compareAndSwapInt(this, LOCKSTATE, s, 1)) {
/* 2102:2674 */             if (waiting) {
/* 2103:2675 */               this.waiter = null;
/* 2104:     */             }
/* 2105:     */           }
/* 2106:     */         }
/* 2107:2679 */         else if ((s & 0x2) == 0)
/* 2108:     */         {
/* 2109:2680 */           if (U.compareAndSwapInt(this, LOCKSTATE, s, s | 0x2))
/* 2110:     */           {
/* 2111:2681 */             waiting = true;
/* 2112:2682 */             this.waiter = Thread.currentThread();
/* 2113:     */           }
/* 2114:     */         }
/* 2115:2685 */         else if (waiting) {
/* 2116:2686 */           LockSupport.park(this);
/* 2117:     */         }
/* 2118:     */       }
/* 2119:     */     }
/* 2120:     */     
/* 2121:     */     /* Error */
/* 2122:     */     final ConcurrentHashMapV8.Node<K, V> find(int h, Object k)
/* 2123:     */     {
/* 2124:     */       // Byte code:
/* 2125:     */       //   0: aload_2
/* 2126:     */       //   1: ifnull +213 -> 214
/* 2127:     */       //   4: aload_0
/* 2128:     */       //   5: getfield 2	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeBin:first	Lio/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeNode;
/* 2129:     */       //   8: astore_3
/* 2130:     */       //   9: aload_3
/* 2131:     */       //   10: ifnull +204 -> 214
/* 2132:     */       //   13: aload_0
/* 2133:     */       //   14: getfield 19	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeBin:lockState	I
/* 2134:     */       //   17: dup
/* 2135:     */       //   18: istore 4
/* 2136:     */       //   20: iconst_3
/* 2137:     */       //   21: iand
/* 2138:     */       //   22: ifeq +38 -> 60
/* 2139:     */       //   25: aload_3
/* 2140:     */       //   26: getfield 23	io/netty/util/internal/chmv8/ConcurrentHashMapV8$Node:hash	I
/* 2141:     */       //   29: iload_1
/* 2142:     */       //   30: if_icmpne +176 -> 206
/* 2143:     */       //   33: aload_3
/* 2144:     */       //   34: getfield 24	io/netty/util/internal/chmv8/ConcurrentHashMapV8$Node:key	Ljava/lang/Object;
/* 2145:     */       //   37: dup
/* 2146:     */       //   38: astore 5
/* 2147:     */       //   40: aload_2
/* 2148:     */       //   41: if_acmpeq +17 -> 58
/* 2149:     */       //   44: aload 5
/* 2150:     */       //   46: ifnull +160 -> 206
/* 2151:     */       //   49: aload_2
/* 2152:     */       //   50: aload 5
/* 2153:     */       //   52: invokevirtual 25	java/lang/Object:equals	(Ljava/lang/Object;)Z
/* 2154:     */       //   55: ifeq +151 -> 206
/* 2155:     */       //   58: aload_3
/* 2156:     */       //   59: areturn
/* 2157:     */       //   60: getstatic 15	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeBin:U	Lsun/misc/Unsafe;
/* 2158:     */       //   63: aload_0
/* 2159:     */       //   64: getstatic 16	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeBin:LOCKSTATE	J
/* 2160:     */       //   67: iload 4
/* 2161:     */       //   69: iload 4
/* 2162:     */       //   71: iconst_4
/* 2163:     */       //   72: iadd
/* 2164:     */       //   73: invokevirtual 17	sun/misc/Unsafe:compareAndSwapInt	(Ljava/lang/Object;JII)Z
/* 2165:     */       //   76: ifeq +130 -> 206
/* 2166:     */       //   79: aload_0
/* 2167:     */       //   80: getfield 14	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeBin:root	Lio/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeNode;
/* 2168:     */       //   83: dup
/* 2169:     */       //   84: astore 6
/* 2170:     */       //   86: ifnonnull +7 -> 93
/* 2171:     */       //   89: aconst_null
/* 2172:     */       //   90: goto +11 -> 101
/* 2173:     */       //   93: aload 6
/* 2174:     */       //   95: iload_1
/* 2175:     */       //   96: aload_2
/* 2176:     */       //   97: aconst_null
/* 2177:     */       //   98: invokevirtual 26	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeNode:findTreeNode	(ILjava/lang/Object;Ljava/lang/Class;)Lio/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeNode;
/* 2178:     */       //   101: astore 7
/* 2179:     */       //   103: getstatic 15	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeBin:U	Lsun/misc/Unsafe;
/* 2180:     */       //   106: aload_0
/* 2181:     */       //   107: getstatic 16	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeBin:LOCKSTATE	J
/* 2182:     */       //   110: aload_0
/* 2183:     */       //   111: getfield 19	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeBin:lockState	I
/* 2184:     */       //   114: dup
/* 2185:     */       //   115: istore 9
/* 2186:     */       //   117: iload 9
/* 2187:     */       //   119: iconst_4
/* 2188:     */       //   120: isub
/* 2189:     */       //   121: invokevirtual 17	sun/misc/Unsafe:compareAndSwapInt	(Ljava/lang/Object;JII)Z
/* 2190:     */       //   124: ifeq -21 -> 103
/* 2191:     */       //   127: iload 9
/* 2192:     */       //   129: bipush 6
/* 2193:     */       //   131: if_icmpne +18 -> 149
/* 2194:     */       //   134: aload_0
/* 2195:     */       //   135: getfield 20	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeBin:waiter	Ljava/lang/Thread;
/* 2196:     */       //   138: dup
/* 2197:     */       //   139: astore 8
/* 2198:     */       //   141: ifnull +8 -> 149
/* 2199:     */       //   144: aload 8
/* 2200:     */       //   146: invokestatic 27	java/util/concurrent/locks/LockSupport:unpark	(Ljava/lang/Thread;)V
/* 2201:     */       //   149: goto +54 -> 203
/* 2202:     */       //   152: astore 10
/* 2203:     */       //   154: getstatic 15	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeBin:U	Lsun/misc/Unsafe;
/* 2204:     */       //   157: aload_0
/* 2205:     */       //   158: getstatic 16	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeBin:LOCKSTATE	J
/* 2206:     */       //   161: aload_0
/* 2207:     */       //   162: getfield 19	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeBin:lockState	I
/* 2208:     */       //   165: dup
/* 2209:     */       //   166: istore 12
/* 2210:     */       //   168: iload 12
/* 2211:     */       //   170: iconst_4
/* 2212:     */       //   171: isub
/* 2213:     */       //   172: invokevirtual 17	sun/misc/Unsafe:compareAndSwapInt	(Ljava/lang/Object;JII)Z
/* 2214:     */       //   175: ifeq -21 -> 154
/* 2215:     */       //   178: iload 12
/* 2216:     */       //   180: bipush 6
/* 2217:     */       //   182: if_icmpne +18 -> 200
/* 2218:     */       //   185: aload_0
/* 2219:     */       //   186: getfield 20	io/netty/util/internal/chmv8/ConcurrentHashMapV8$TreeBin:waiter	Ljava/lang/Thread;
/* 2220:     */       //   189: dup
/* 2221:     */       //   190: astore 11
/* 2222:     */       //   192: ifnull +8 -> 200
/* 2223:     */       //   195: aload 11
/* 2224:     */       //   197: invokestatic 27	java/util/concurrent/locks/LockSupport:unpark	(Ljava/lang/Thread;)V
/* 2225:     */       //   200: aload 10
/* 2226:     */       //   202: athrow
/* 2227:     */       //   203: aload 7
/* 2228:     */       //   205: areturn
/* 2229:     */       //   206: aload_3
/* 2230:     */       //   207: getfield 28	io/netty/util/internal/chmv8/ConcurrentHashMapV8$Node:next	Lio/netty/util/internal/chmv8/ConcurrentHashMapV8$Node;
/* 2231:     */       //   210: astore_3
/* 2232:     */       //   211: goto -202 -> 9
/* 2233:     */       //   214: aconst_null
/* 2234:     */       //   215: areturn
/* 2235:     */       // Line number table:
/* 2236:     */       //   Java source line #2696	-> byte code offset #0
/* 2237:     */       //   Java source line #2697	-> byte code offset #4
/* 2238:     */       //   Java source line #2699	-> byte code offset #13
/* 2239:     */       //   Java source line #2700	-> byte code offset #25
/* 2240:     */       //   Java source line #2702	-> byte code offset #58
/* 2241:     */       //   Java source line #2704	-> byte code offset #60
/* 2242:     */       //   Java source line #2708	-> byte code offset #79
/* 2243:     */       //   Java source line #2713	-> byte code offset #103
/* 2244:     */       //   Java source line #2716	-> byte code offset #127
/* 2245:     */       //   Java source line #2717	-> byte code offset #144
/* 2246:     */       //   Java source line #2718	-> byte code offset #149
/* 2247:     */       //   Java source line #2711	-> byte code offset #152
/* 2248:     */       //   Java source line #2713	-> byte code offset #154
/* 2249:     */       //   Java source line #2716	-> byte code offset #178
/* 2250:     */       //   Java source line #2717	-> byte code offset #195
/* 2251:     */       //   Java source line #2718	-> byte code offset #200
/* 2252:     */       //   Java source line #2719	-> byte code offset #203
/* 2253:     */       //   Java source line #2697	-> byte code offset #206
/* 2254:     */       //   Java source line #2723	-> byte code offset #214
/* 2255:     */       // Local variable table:
/* 2256:     */       //   start	length	slot	name	signature
/* 2257:     */       //   0	216	0	this	TreeBin<K, V>
/* 2258:     */       //   0	216	1	h	int
/* 2259:     */       //   0	216	2	k	Object
/* 2260:     */       //   8	203	3	e	ConcurrentHashMapV8.Node<K, V>
/* 2261:     */       //   18	52	4	s	int
/* 2262:     */       //   38	13	5	ek	K
/* 2263:     */       //   84	10	6	r	ConcurrentHashMapV8.TreeNode<K, V>
/* 2264:     */       //   101	103	7	p	ConcurrentHashMapV8.TreeNode<K, V>
/* 2265:     */       //   139	6	8	w	Thread
/* 2266:     */       //   115	13	9	ls	int
/* 2267:     */       //   152	49	10	localObject	Object
/* 2268:     */       //   190	6	11	w	Thread
/* 2269:     */       //   166	13	12	ls	int
/* 2270:     */       // Exception table:
/* 2271:     */       //   from	to	target	type
/* 2272:     */       //   79	103	152	finally
/* 2273:     */       //   152	154	152	finally
/* 2274:     */     }
/* 2275:     */     
/* 2276:     */     final ConcurrentHashMapV8.TreeNode<K, V> putTreeVal(int h, K k, V v)
/* 2277:     */     {
/* 2278:2731 */       Class<?> kc = null;
/* 2279:2732 */       ConcurrentHashMapV8.TreeNode<K, V> p = this.root;
/* 2280:     */       for (;;)
/* 2281:     */       {
/* 2282:2734 */         if (p == null)
/* 2283:     */         {
/* 2284:2735 */           this.first = (this.root = new ConcurrentHashMapV8.TreeNode(h, k, v, null, null));
/* 2285:2736 */           break;
/* 2286:     */         }
/* 2287:     */         int ph;
/* 2288:     */         int dir;
/* 2289:2738 */         if ((ph = p.hash) > h)
/* 2290:     */         {
/* 2291:2739 */           dir = -1;
/* 2292:     */         }
/* 2293:     */         else
/* 2294:     */         {
/* 2295:     */           int dir;
/* 2296:2740 */           if (ph < h)
/* 2297:     */           {
/* 2298:2741 */             dir = 1;
/* 2299:     */           }
/* 2300:     */           else
/* 2301:     */           {
/* 2302:     */             K pk;
/* 2303:2742 */             if (((pk = p.key) == k) || ((pk != null) && (k.equals(pk)))) {
/* 2304:2743 */               return p;
/* 2305:     */             }
/* 2306:     */             int dir;
/* 2307:2744 */             if (((kc == null) && ((kc = ConcurrentHashMapV8.comparableClassFor(k)) == null)) || ((dir = ConcurrentHashMapV8.compareComparables(kc, k, pk)) == 0))
/* 2308:     */             {
/* 2309:     */               int dir;
/* 2310:2747 */               if (p.left == null)
/* 2311:     */               {
/* 2312:2748 */                 dir = 1;
/* 2313:     */               }
/* 2314:     */               else
/* 2315:     */               {
/* 2316:     */                 ConcurrentHashMapV8.TreeNode<K, V> pr;
/* 2317:     */                 ConcurrentHashMapV8.TreeNode<K, V> q;
/* 2318:     */                 int dir;
/* 2319:2749 */                 if (((pr = p.right) == null) || ((q = pr.findTreeNode(h, k, kc)) == null))
/* 2320:     */                 {
/* 2321:2751 */                   dir = -1;
/* 2322:     */                 }
/* 2323:     */                 else
/* 2324:     */                 {
/* 2325:     */                   ConcurrentHashMapV8.TreeNode<K, V> q;
/* 2326:2753 */                   return q;
/* 2327:     */                 }
/* 2328:     */               }
/* 2329:     */             }
/* 2330:     */           }
/* 2331:     */         }
/* 2332:     */         int dir;
/* 2333:2755 */         ConcurrentHashMapV8.TreeNode<K, V> xp = p;
/* 2334:2756 */         if ((p = dir < 0 ? p.left : p.right) == null)
/* 2335:     */         {
/* 2336:2757 */           ConcurrentHashMapV8.TreeNode<K, V> f = this.first;
/* 2337:     */           ConcurrentHashMapV8.TreeNode<K, V> x;
/* 2338:2758 */           this.first = (x = new ConcurrentHashMapV8.TreeNode(h, k, v, f, xp));
/* 2339:2759 */           if (f != null) {
/* 2340:2760 */             f.prev = x;
/* 2341:     */           }
/* 2342:2761 */           if (dir < 0) {
/* 2343:2762 */             xp.left = x;
/* 2344:     */           } else {
/* 2345:2764 */             xp.right = x;
/* 2346:     */           }
/* 2347:2765 */           if (!xp.red)
/* 2348:     */           {
/* 2349:2766 */             x.red = true; break;
/* 2350:     */           }
/* 2351:2768 */           lockRoot();
/* 2352:     */           try
/* 2353:     */           {
/* 2354:2770 */             this.root = balanceInsertion(this.root, x);
/* 2355:     */           }
/* 2356:     */           finally
/* 2357:     */           {
/* 2358:2772 */             unlockRoot();
/* 2359:     */           }
/* 2360:2775 */           break;
/* 2361:     */         }
/* 2362:     */       }
/* 2363:2778 */       assert (checkInvariants(this.root));
/* 2364:2779 */       return null;
/* 2365:     */     }
/* 2366:     */     
/* 2367:     */     final boolean removeTreeNode(ConcurrentHashMapV8.TreeNode<K, V> p)
/* 2368:     */     {
/* 2369:2793 */       ConcurrentHashMapV8.TreeNode<K, V> next = (ConcurrentHashMapV8.TreeNode)p.next;
/* 2370:2794 */       ConcurrentHashMapV8.TreeNode<K, V> pred = p.prev;
/* 2371:2796 */       if (pred == null) {
/* 2372:2797 */         this.first = next;
/* 2373:     */       } else {
/* 2374:2799 */         pred.next = next;
/* 2375:     */       }
/* 2376:2800 */       if (next != null) {
/* 2377:2801 */         next.prev = pred;
/* 2378:     */       }
/* 2379:2802 */       if (this.first == null)
/* 2380:     */       {
/* 2381:2803 */         this.root = null;
/* 2382:2804 */         return true;
/* 2383:     */       }
/* 2384:     */       ConcurrentHashMapV8.TreeNode<K, V> r;
/* 2385:     */       ConcurrentHashMapV8.TreeNode<K, V> rl;
/* 2386:2806 */       if (((r = this.root) == null) || (r.right == null) || ((rl = r.left) == null) || (rl.left == null)) {
/* 2387:2808 */         return true;
/* 2388:     */       }
/* 2389:     */       ConcurrentHashMapV8.TreeNode<K, V> rl;
/* 2390:2809 */       lockRoot();
/* 2391:     */       try
/* 2392:     */       {
/* 2393:2812 */         ConcurrentHashMapV8.TreeNode<K, V> pl = p.left;
/* 2394:2813 */         ConcurrentHashMapV8.TreeNode<K, V> pr = p.right;
/* 2395:     */         ConcurrentHashMapV8.TreeNode<K, V> replacement;
/* 2396:     */         ConcurrentHashMapV8.TreeNode<K, V> replacement;
/* 2397:2814 */         if ((pl != null) && (pr != null))
/* 2398:     */         {
/* 2399:2815 */           ConcurrentHashMapV8.TreeNode<K, V> s = pr;
/* 2400:     */           ConcurrentHashMapV8.TreeNode<K, V> sl;
/* 2401:2816 */           while ((sl = s.left) != null) {
/* 2402:2817 */             s = sl;
/* 2403:     */           }
/* 2404:2818 */           boolean c = s.red;s.red = p.red;p.red = c;
/* 2405:2819 */           ConcurrentHashMapV8.TreeNode<K, V> sr = s.right;
/* 2406:2820 */           ConcurrentHashMapV8.TreeNode<K, V> pp = p.parent;
/* 2407:2821 */           if (s == pr)
/* 2408:     */           {
/* 2409:2822 */             p.parent = s;
/* 2410:2823 */             s.right = p;
/* 2411:     */           }
/* 2412:     */           else
/* 2413:     */           {
/* 2414:2826 */             ConcurrentHashMapV8.TreeNode<K, V> sp = s.parent;
/* 2415:2827 */             if ((p.parent = sp) != null) {
/* 2416:2828 */               if (s == sp.left) {
/* 2417:2829 */                 sp.left = p;
/* 2418:     */               } else {
/* 2419:2831 */                 sp.right = p;
/* 2420:     */               }
/* 2421:     */             }
/* 2422:2833 */             if ((s.right = pr) != null) {
/* 2423:2834 */               pr.parent = s;
/* 2424:     */             }
/* 2425:     */           }
/* 2426:2836 */           p.left = null;
/* 2427:2837 */           if ((p.right = sr) != null) {
/* 2428:2838 */             sr.parent = p;
/* 2429:     */           }
/* 2430:2839 */           if ((s.left = pl) != null) {
/* 2431:2840 */             pl.parent = s;
/* 2432:     */           }
/* 2433:2841 */           if ((s.parent = pp) == null) {
/* 2434:2842 */             r = s;
/* 2435:2843 */           } else if (p == pp.left) {
/* 2436:2844 */             pp.left = s;
/* 2437:     */           } else {
/* 2438:2846 */             pp.right = s;
/* 2439:     */           }
/* 2440:     */           ConcurrentHashMapV8.TreeNode<K, V> replacement;
/* 2441:2847 */           if (sr != null) {
/* 2442:2848 */             replacement = sr;
/* 2443:     */           } else {
/* 2444:2850 */             replacement = p;
/* 2445:     */           }
/* 2446:     */         }
/* 2447:     */         else
/* 2448:     */         {
/* 2449:     */           ConcurrentHashMapV8.TreeNode<K, V> replacement;
/* 2450:2852 */           if (pl != null)
/* 2451:     */           {
/* 2452:2853 */             replacement = pl;
/* 2453:     */           }
/* 2454:     */           else
/* 2455:     */           {
/* 2456:     */             ConcurrentHashMapV8.TreeNode<K, V> replacement;
/* 2457:2854 */             if (pr != null) {
/* 2458:2855 */               replacement = pr;
/* 2459:     */             } else {
/* 2460:2857 */               replacement = p;
/* 2461:     */             }
/* 2462:     */           }
/* 2463:     */         }
/* 2464:2858 */         if (replacement != p)
/* 2465:     */         {
/* 2466:2859 */           ConcurrentHashMapV8.TreeNode<K, V> pp = replacement.parent = p.parent;
/* 2467:2860 */           if (pp == null) {
/* 2468:2861 */             r = replacement;
/* 2469:2862 */           } else if (p == pp.left) {
/* 2470:2863 */             pp.left = replacement;
/* 2471:     */           } else {
/* 2472:2865 */             pp.right = replacement;
/* 2473:     */           }
/* 2474:2866 */           p.left = (p.right = p.parent = null);
/* 2475:     */         }
/* 2476:2869 */         this.root = (p.red ? r : balanceDeletion(r, replacement));
/* 2477:2871 */         if (p == replacement)
/* 2478:     */         {
/* 2479:     */           ConcurrentHashMapV8.TreeNode<K, V> pp;
/* 2480:2873 */           if ((pp = p.parent) != null)
/* 2481:     */           {
/* 2482:2874 */             if (p == pp.left) {
/* 2483:2875 */               pp.left = null;
/* 2484:2876 */             } else if (p == pp.right) {
/* 2485:2877 */               pp.right = null;
/* 2486:     */             }
/* 2487:2878 */             p.parent = null;
/* 2488:     */           }
/* 2489:     */         }
/* 2490:     */       }
/* 2491:     */       finally
/* 2492:     */       {
/* 2493:2882 */         unlockRoot();
/* 2494:     */       }
/* 2495:2884 */       assert (checkInvariants(this.root));
/* 2496:2885 */       return false;
/* 2497:     */     }
/* 2498:     */     
/* 2499:     */     static <K, V> ConcurrentHashMapV8.TreeNode<K, V> rotateLeft(ConcurrentHashMapV8.TreeNode<K, V> root, ConcurrentHashMapV8.TreeNode<K, V> p)
/* 2500:     */     {
/* 2501:     */       ConcurrentHashMapV8.TreeNode<K, V> r;
/* 2502:2894 */       if ((p != null) && ((r = p.right) != null))
/* 2503:     */       {
/* 2504:     */         ConcurrentHashMapV8.TreeNode<K, V> rl;
/* 2505:2895 */         if ((rl = p.right = r.left) != null) {
/* 2506:2896 */           rl.parent = p;
/* 2507:     */         }
/* 2508:     */         ConcurrentHashMapV8.TreeNode<K, V> pp;
/* 2509:2897 */         if ((pp = r.parent = p.parent) == null) {
/* 2510:2898 */           (root = r).red = false;
/* 2511:2899 */         } else if (pp.left == p) {
/* 2512:2900 */           pp.left = r;
/* 2513:     */         } else {
/* 2514:2902 */           pp.right = r;
/* 2515:     */         }
/* 2516:2903 */         r.left = p;
/* 2517:2904 */         p.parent = r;
/* 2518:     */       }
/* 2519:2906 */       return root;
/* 2520:     */     }
/* 2521:     */     
/* 2522:     */     static <K, V> ConcurrentHashMapV8.TreeNode<K, V> rotateRight(ConcurrentHashMapV8.TreeNode<K, V> root, ConcurrentHashMapV8.TreeNode<K, V> p)
/* 2523:     */     {
/* 2524:     */       ConcurrentHashMapV8.TreeNode<K, V> l;
/* 2525:2912 */       if ((p != null) && ((l = p.left) != null))
/* 2526:     */       {
/* 2527:     */         ConcurrentHashMapV8.TreeNode<K, V> lr;
/* 2528:2913 */         if ((lr = p.left = l.right) != null) {
/* 2529:2914 */           lr.parent = p;
/* 2530:     */         }
/* 2531:     */         ConcurrentHashMapV8.TreeNode<K, V> pp;
/* 2532:2915 */         if ((pp = l.parent = p.parent) == null) {
/* 2533:2916 */           (root = l).red = false;
/* 2534:2917 */         } else if (pp.right == p) {
/* 2535:2918 */           pp.right = l;
/* 2536:     */         } else {
/* 2537:2920 */           pp.left = l;
/* 2538:     */         }
/* 2539:2921 */         l.right = p;
/* 2540:2922 */         p.parent = l;
/* 2541:     */       }
/* 2542:2924 */       return root;
/* 2543:     */     }
/* 2544:     */     
/* 2545:     */     static <K, V> ConcurrentHashMapV8.TreeNode<K, V> balanceInsertion(ConcurrentHashMapV8.TreeNode<K, V> root, ConcurrentHashMapV8.TreeNode<K, V> x)
/* 2546:     */     {
/* 2547:2929 */       x.red = true;
/* 2548:     */       for (;;)
/* 2549:     */       {
/* 2550:     */         ConcurrentHashMapV8.TreeNode<K, V> xp;
/* 2551:2931 */         if ((xp = x.parent) == null)
/* 2552:     */         {
/* 2553:2932 */           x.red = false;
/* 2554:2933 */           return x;
/* 2555:     */         }
/* 2556:     */         ConcurrentHashMapV8.TreeNode<K, V> xpp;
/* 2557:2935 */         if ((!xp.red) || ((xpp = xp.parent) == null)) {
/* 2558:2936 */           return root;
/* 2559:     */         }
/* 2560:     */         ConcurrentHashMapV8.TreeNode<K, V> xpp;
/* 2561:     */         ConcurrentHashMapV8.TreeNode<K, V> xppl;
/* 2562:2937 */         if (xp == (xppl = xpp.left))
/* 2563:     */         {
/* 2564:     */           ConcurrentHashMapV8.TreeNode<K, V> xppr;
/* 2565:2938 */           if (((xppr = xpp.right) != null) && (xppr.red))
/* 2566:     */           {
/* 2567:2939 */             xppr.red = false;
/* 2568:2940 */             xp.red = false;
/* 2569:2941 */             xpp.red = true;
/* 2570:2942 */             x = xpp;
/* 2571:     */           }
/* 2572:     */           else
/* 2573:     */           {
/* 2574:2945 */             if (x == xp.right)
/* 2575:     */             {
/* 2576:2946 */               root = rotateLeft(root, x = xp);
/* 2577:2947 */               xpp = (xp = x.parent) == null ? null : xp.parent;
/* 2578:     */             }
/* 2579:2949 */             if (xp != null)
/* 2580:     */             {
/* 2581:2950 */               xp.red = false;
/* 2582:2951 */               if (xpp != null)
/* 2583:     */               {
/* 2584:2952 */                 xpp.red = true;
/* 2585:2953 */                 root = rotateRight(root, xpp);
/* 2586:     */               }
/* 2587:     */             }
/* 2588:     */           }
/* 2589:     */         }
/* 2590:2959 */         else if ((xppl != null) && (xppl.red))
/* 2591:     */         {
/* 2592:2960 */           xppl.red = false;
/* 2593:2961 */           xp.red = false;
/* 2594:2962 */           xpp.red = true;
/* 2595:2963 */           x = xpp;
/* 2596:     */         }
/* 2597:     */         else
/* 2598:     */         {
/* 2599:2966 */           if (x == xp.left)
/* 2600:     */           {
/* 2601:2967 */             root = rotateRight(root, x = xp);
/* 2602:2968 */             xpp = (xp = x.parent) == null ? null : xp.parent;
/* 2603:     */           }
/* 2604:2970 */           if (xp != null)
/* 2605:     */           {
/* 2606:2971 */             xp.red = false;
/* 2607:2972 */             if (xpp != null)
/* 2608:     */             {
/* 2609:2973 */               xpp.red = true;
/* 2610:2974 */               root = rotateLeft(root, xpp);
/* 2611:     */             }
/* 2612:     */           }
/* 2613:     */         }
/* 2614:     */       }
/* 2615:     */     }
/* 2616:     */     
/* 2617:     */     static <K, V> ConcurrentHashMapV8.TreeNode<K, V> balanceDeletion(ConcurrentHashMapV8.TreeNode<K, V> root, ConcurrentHashMapV8.TreeNode<K, V> x)
/* 2618:     */     {
/* 2619:     */       for (;;)
/* 2620:     */       {
/* 2621:2985 */         if ((x == null) || (x == root)) {
/* 2622:2986 */           return root;
/* 2623:     */         }
/* 2624:     */         ConcurrentHashMapV8.TreeNode<K, V> xp;
/* 2625:2987 */         if ((xp = x.parent) == null)
/* 2626:     */         {
/* 2627:2988 */           x.red = false;
/* 2628:2989 */           return x;
/* 2629:     */         }
/* 2630:2991 */         if (x.red)
/* 2631:     */         {
/* 2632:2992 */           x.red = false;
/* 2633:2993 */           return root;
/* 2634:     */         }
/* 2635:     */         ConcurrentHashMapV8.TreeNode<K, V> xpl;
/* 2636:2995 */         if ((xpl = xp.left) == x)
/* 2637:     */         {
/* 2638:     */           ConcurrentHashMapV8.TreeNode<K, V> xpr;
/* 2639:2996 */           if (((xpr = xp.right) != null) && (xpr.red))
/* 2640:     */           {
/* 2641:2997 */             xpr.red = false;
/* 2642:2998 */             xp.red = true;
/* 2643:2999 */             root = rotateLeft(root, xp);
/* 2644:3000 */             xpr = (xp = x.parent) == null ? null : xp.right;
/* 2645:     */           }
/* 2646:3002 */           if (xpr == null)
/* 2647:     */           {
/* 2648:3003 */             x = xp;
/* 2649:     */           }
/* 2650:     */           else
/* 2651:     */           {
/* 2652:3005 */             ConcurrentHashMapV8.TreeNode<K, V> sl = xpr.left;ConcurrentHashMapV8.TreeNode<K, V> sr = xpr.right;
/* 2653:3006 */             if (((sr == null) || (!sr.red)) && ((sl == null) || (!sl.red)))
/* 2654:     */             {
/* 2655:3008 */               xpr.red = true;
/* 2656:3009 */               x = xp;
/* 2657:     */             }
/* 2658:     */             else
/* 2659:     */             {
/* 2660:3012 */               if ((sr == null) || (!sr.red))
/* 2661:     */               {
/* 2662:3013 */                 if (sl != null) {
/* 2663:3014 */                   sl.red = false;
/* 2664:     */                 }
/* 2665:3015 */                 xpr.red = true;
/* 2666:3016 */                 root = rotateRight(root, xpr);
/* 2667:3017 */                 xpr = (xp = x.parent) == null ? null : xp.right;
/* 2668:     */               }
/* 2669:3020 */               if (xpr != null)
/* 2670:     */               {
/* 2671:3021 */                 xpr.red = (xp == null ? false : xp.red);
/* 2672:3022 */                 if ((sr = xpr.right) != null) {
/* 2673:3023 */                   sr.red = false;
/* 2674:     */                 }
/* 2675:     */               }
/* 2676:3025 */               if (xp != null)
/* 2677:     */               {
/* 2678:3026 */                 xp.red = false;
/* 2679:3027 */                 root = rotateLeft(root, xp);
/* 2680:     */               }
/* 2681:3029 */               x = root;
/* 2682:     */             }
/* 2683:     */           }
/* 2684:     */         }
/* 2685:     */         else
/* 2686:     */         {
/* 2687:3034 */           if ((xpl != null) && (xpl.red))
/* 2688:     */           {
/* 2689:3035 */             xpl.red = false;
/* 2690:3036 */             xp.red = true;
/* 2691:3037 */             root = rotateRight(root, xp);
/* 2692:3038 */             xpl = (xp = x.parent) == null ? null : xp.left;
/* 2693:     */           }
/* 2694:3040 */           if (xpl == null)
/* 2695:     */           {
/* 2696:3041 */             x = xp;
/* 2697:     */           }
/* 2698:     */           else
/* 2699:     */           {
/* 2700:3043 */             ConcurrentHashMapV8.TreeNode<K, V> sl = xpl.left;ConcurrentHashMapV8.TreeNode<K, V> sr = xpl.right;
/* 2701:3044 */             if (((sl == null) || (!sl.red)) && ((sr == null) || (!sr.red)))
/* 2702:     */             {
/* 2703:3046 */               xpl.red = true;
/* 2704:3047 */               x = xp;
/* 2705:     */             }
/* 2706:     */             else
/* 2707:     */             {
/* 2708:3050 */               if ((sl == null) || (!sl.red))
/* 2709:     */               {
/* 2710:3051 */                 if (sr != null) {
/* 2711:3052 */                   sr.red = false;
/* 2712:     */                 }
/* 2713:3053 */                 xpl.red = true;
/* 2714:3054 */                 root = rotateLeft(root, xpl);
/* 2715:3055 */                 xpl = (xp = x.parent) == null ? null : xp.left;
/* 2716:     */               }
/* 2717:3058 */               if (xpl != null)
/* 2718:     */               {
/* 2719:3059 */                 xpl.red = (xp == null ? false : xp.red);
/* 2720:3060 */                 if ((sl = xpl.left) != null) {
/* 2721:3061 */                   sl.red = false;
/* 2722:     */                 }
/* 2723:     */               }
/* 2724:3063 */               if (xp != null)
/* 2725:     */               {
/* 2726:3064 */                 xp.red = false;
/* 2727:3065 */                 root = rotateRight(root, xp);
/* 2728:     */               }
/* 2729:3067 */               x = root;
/* 2730:     */             }
/* 2731:     */           }
/* 2732:     */         }
/* 2733:     */       }
/* 2734:     */     }
/* 2735:     */     
/* 2736:     */     static <K, V> boolean checkInvariants(ConcurrentHashMapV8.TreeNode<K, V> t)
/* 2737:     */     {
/* 2738:3078 */       ConcurrentHashMapV8.TreeNode<K, V> tp = t.parent;ConcurrentHashMapV8.TreeNode<K, V> tl = t.left;ConcurrentHashMapV8.TreeNode<K, V> tr = t.right;
/* 2739:3079 */       ConcurrentHashMapV8.TreeNode<K, V> tb = t.prev;ConcurrentHashMapV8.TreeNode<K, V> tn = (ConcurrentHashMapV8.TreeNode)t.next;
/* 2740:3080 */       if ((tb != null) && (tb.next != t)) {
/* 2741:3081 */         return false;
/* 2742:     */       }
/* 2743:3082 */       if ((tn != null) && (tn.prev != t)) {
/* 2744:3083 */         return false;
/* 2745:     */       }
/* 2746:3084 */       if ((tp != null) && (t != tp.left) && (t != tp.right)) {
/* 2747:3085 */         return false;
/* 2748:     */       }
/* 2749:3086 */       if ((tl != null) && ((tl.parent != t) || (tl.hash > t.hash))) {
/* 2750:3087 */         return false;
/* 2751:     */       }
/* 2752:3088 */       if ((tr != null) && ((tr.parent != t) || (tr.hash < t.hash))) {
/* 2753:3089 */         return false;
/* 2754:     */       }
/* 2755:3090 */       if ((t.red) && (tl != null) && (tl.red) && (tr != null) && (tr.red)) {
/* 2756:3091 */         return false;
/* 2757:     */       }
/* 2758:3092 */       if ((tl != null) && (!checkInvariants(tl))) {
/* 2759:3093 */         return false;
/* 2760:     */       }
/* 2761:3094 */       if ((tr != null) && (!checkInvariants(tr))) {
/* 2762:3095 */         return false;
/* 2763:     */       }
/* 2764:3096 */       return true;
/* 2765:     */     }
/* 2766:     */     
/* 2767:     */     static
/* 2768:     */     {
/* 2769:     */       try
/* 2770:     */       {
/* 2771:3103 */         U = ConcurrentHashMapV8.access$000();
/* 2772:3104 */         Class<?> k = TreeBin.class;
/* 2773:3105 */         LOCKSTATE = U.objectFieldOffset(k.getDeclaredField("lockState"));
/* 2774:     */       }
/* 2775:     */       catch (Exception e)
/* 2776:     */       {
/* 2777:3108 */         throw new Error(e);
/* 2778:     */       }
/* 2779:     */     }
/* 2780:     */   }
/* 2781:     */   
/* 2782:     */   static class Traverser<K, V>
/* 2783:     */   {
/* 2784:     */     ConcurrentHashMapV8.Node<K, V>[] tab;
/* 2785:     */     ConcurrentHashMapV8.Node<K, V> next;
/* 2786:     */     int index;
/* 2787:     */     int baseIndex;
/* 2788:     */     int baseLimit;
/* 2789:     */     final int baseSize;
/* 2790:     */     
/* 2791:     */     Traverser(ConcurrentHashMapV8.Node<K, V>[] tab, int size, int index, int limit)
/* 2792:     */     {
/* 2793:3145 */       this.tab = tab;
/* 2794:3146 */       this.baseSize = size;
/* 2795:3147 */       this.baseIndex = (this.index = index);
/* 2796:3148 */       this.baseLimit = limit;
/* 2797:3149 */       this.next = null;
/* 2798:     */     }
/* 2799:     */     
/* 2800:     */     final ConcurrentHashMapV8.Node<K, V> advance()
/* 2801:     */     {
/* 2802:     */       ConcurrentHashMapV8.Node<K, V> e;
/* 2803:3157 */       if ((e = this.next) != null) {
/* 2804:3158 */         e = e.next;
/* 2805:     */       }
/* 2806:     */       for (;;)
/* 2807:     */       {
/* 2808:3161 */         if (e != null) {
/* 2809:3162 */           return this.next = e;
/* 2810:     */         }
/* 2811:     */         ConcurrentHashMapV8.Node<K, V>[] t;
/* 2812:     */         int n;
/* 2813:     */         int i;
/* 2814:3163 */         if ((this.baseIndex >= this.baseLimit) || ((t = this.tab) == null) || ((n = t.length) <= (i = this.index)) || (i < 0)) {
/* 2815:3165 */           return this.next = null;
/* 2816:     */         }
/* 2817:     */         int n;
/* 2818:     */         int i;
/* 2819:     */         ConcurrentHashMapV8.Node<K, V>[] t;
/* 2820:3166 */         if (((e = ConcurrentHashMapV8.tabAt(t, this.index)) != null) && (e.hash < 0))
/* 2821:     */         {
/* 2822:3167 */           if ((e instanceof ConcurrentHashMapV8.ForwardingNode))
/* 2823:     */           {
/* 2824:3168 */             this.tab = ((ConcurrentHashMapV8.ForwardingNode)e).nextTable;
/* 2825:3169 */             e = null;
/* 2826:3170 */             continue;
/* 2827:     */           }
/* 2828:3172 */           if ((e instanceof ConcurrentHashMapV8.TreeBin)) {
/* 2829:3173 */             e = ((ConcurrentHashMapV8.TreeBin)e).first;
/* 2830:     */           } else {
/* 2831:3175 */             e = null;
/* 2832:     */           }
/* 2833:     */         }
/* 2834:3177 */         if (this.index += this.baseSize >= n) {
/* 2835:3178 */           this.index = (++this.baseIndex);
/* 2836:     */         }
/* 2837:     */       }
/* 2838:     */     }
/* 2839:     */   }
/* 2840:     */   
/* 2841:     */   static class BaseIterator<K, V>
/* 2842:     */     extends ConcurrentHashMapV8.Traverser<K, V>
/* 2843:     */   {
/* 2844:     */     final ConcurrentHashMapV8<K, V> map;
/* 2845:     */     ConcurrentHashMapV8.Node<K, V> lastReturned;
/* 2846:     */     
/* 2847:     */     BaseIterator(ConcurrentHashMapV8.Node<K, V>[] tab, int size, int index, int limit, ConcurrentHashMapV8<K, V> map)
/* 2848:     */     {
/* 2849:3192 */       super(size, index, limit);
/* 2850:3193 */       this.map = map;
/* 2851:3194 */       advance();
/* 2852:     */     }
/* 2853:     */     
/* 2854:     */     public final boolean hasNext()
/* 2855:     */     {
/* 2856:3197 */       return this.next != null;
/* 2857:     */     }
/* 2858:     */     
/* 2859:     */     public final boolean hasMoreElements()
/* 2860:     */     {
/* 2861:3198 */       return this.next != null;
/* 2862:     */     }
/* 2863:     */     
/* 2864:     */     public final void remove()
/* 2865:     */     {
/* 2866:     */       ConcurrentHashMapV8.Node<K, V> p;
/* 2867:3202 */       if ((p = this.lastReturned) == null) {
/* 2868:3203 */         throw new IllegalStateException();
/* 2869:     */       }
/* 2870:3204 */       this.lastReturned = null;
/* 2871:3205 */       this.map.replaceNode(p.key, null, null);
/* 2872:     */     }
/* 2873:     */   }
/* 2874:     */   
/* 2875:     */   static final class KeyIterator<K, V>
/* 2876:     */     extends ConcurrentHashMapV8.BaseIterator<K, V>
/* 2877:     */     implements Iterator<K>, Enumeration<K>
/* 2878:     */   {
/* 2879:     */     KeyIterator(ConcurrentHashMapV8.Node<K, V>[] tab, int index, int size, int limit, ConcurrentHashMapV8<K, V> map)
/* 2880:     */     {
/* 2881:3213 */       super(index, size, limit, map);
/* 2882:     */     }
/* 2883:     */     
/* 2884:     */     public final K next()
/* 2885:     */     {
/* 2886:     */       ConcurrentHashMapV8.Node<K, V> p;
/* 2887:3218 */       if ((p = this.next) == null) {
/* 2888:3219 */         throw new NoSuchElementException();
/* 2889:     */       }
/* 2890:3220 */       K k = p.key;
/* 2891:3221 */       this.lastReturned = p;
/* 2892:3222 */       advance();
/* 2893:3223 */       return k;
/* 2894:     */     }
/* 2895:     */     
/* 2896:     */     public final K nextElement()
/* 2897:     */     {
/* 2898:3226 */       return next();
/* 2899:     */     }
/* 2900:     */   }
/* 2901:     */   
/* 2902:     */   static final class ValueIterator<K, V>
/* 2903:     */     extends ConcurrentHashMapV8.BaseIterator<K, V>
/* 2904:     */     implements Iterator<V>, Enumeration<V>
/* 2905:     */   {
/* 2906:     */     ValueIterator(ConcurrentHashMapV8.Node<K, V>[] tab, int index, int size, int limit, ConcurrentHashMapV8<K, V> map)
/* 2907:     */     {
/* 2908:3233 */       super(index, size, limit, map);
/* 2909:     */     }
/* 2910:     */     
/* 2911:     */     public final V next()
/* 2912:     */     {
/* 2913:     */       ConcurrentHashMapV8.Node<K, V> p;
/* 2914:3238 */       if ((p = this.next) == null) {
/* 2915:3239 */         throw new NoSuchElementException();
/* 2916:     */       }
/* 2917:3240 */       V v = p.val;
/* 2918:3241 */       this.lastReturned = p;
/* 2919:3242 */       advance();
/* 2920:3243 */       return v;
/* 2921:     */     }
/* 2922:     */     
/* 2923:     */     public final V nextElement()
/* 2924:     */     {
/* 2925:3246 */       return next();
/* 2926:     */     }
/* 2927:     */   }
/* 2928:     */   
/* 2929:     */   static final class EntryIterator<K, V>
/* 2930:     */     extends ConcurrentHashMapV8.BaseIterator<K, V>
/* 2931:     */     implements Iterator<Map.Entry<K, V>>
/* 2932:     */   {
/* 2933:     */     EntryIterator(ConcurrentHashMapV8.Node<K, V>[] tab, int index, int size, int limit, ConcurrentHashMapV8<K, V> map)
/* 2934:     */     {
/* 2935:3253 */       super(index, size, limit, map);
/* 2936:     */     }
/* 2937:     */     
/* 2938:     */     public final Map.Entry<K, V> next()
/* 2939:     */     {
/* 2940:     */       ConcurrentHashMapV8.Node<K, V> p;
/* 2941:3258 */       if ((p = this.next) == null) {
/* 2942:3259 */         throw new NoSuchElementException();
/* 2943:     */       }
/* 2944:3260 */       K k = p.key;
/* 2945:3261 */       V v = p.val;
/* 2946:3262 */       this.lastReturned = p;
/* 2947:3263 */       advance();
/* 2948:3264 */       return new ConcurrentHashMapV8.MapEntry(k, v, this.map);
/* 2949:     */     }
/* 2950:     */   }
/* 2951:     */   
/* 2952:     */   static final class MapEntry<K, V>
/* 2953:     */     implements Map.Entry<K, V>
/* 2954:     */   {
/* 2955:     */     final K key;
/* 2956:     */     V val;
/* 2957:     */     final ConcurrentHashMapV8<K, V> map;
/* 2958:     */     
/* 2959:     */     MapEntry(K key, V val, ConcurrentHashMapV8<K, V> map)
/* 2960:     */     {
/* 2961:3276 */       this.key = key;
/* 2962:3277 */       this.val = val;
/* 2963:3278 */       this.map = map;
/* 2964:     */     }
/* 2965:     */     
/* 2966:     */     public K getKey()
/* 2967:     */     {
/* 2968:3280 */       return this.key;
/* 2969:     */     }
/* 2970:     */     
/* 2971:     */     public V getValue()
/* 2972:     */     {
/* 2973:3281 */       return this.val;
/* 2974:     */     }
/* 2975:     */     
/* 2976:     */     public int hashCode()
/* 2977:     */     {
/* 2978:3282 */       return this.key.hashCode() ^ this.val.hashCode();
/* 2979:     */     }
/* 2980:     */     
/* 2981:     */     public String toString()
/* 2982:     */     {
/* 2983:3283 */       return this.key + "=" + this.val;
/* 2984:     */     }
/* 2985:     */     
/* 2986:     */     public boolean equals(Object o)
/* 2987:     */     {
/* 2988:     */       Map.Entry<?, ?> e;
/* 2989:     */       Object k;
/* 2990:     */       Object v;
/* 2991:3287 */       return ((o instanceof Map.Entry)) && ((k = (e = (Map.Entry)o).getKey()) != null) && ((v = e.getValue()) != null) && ((k == this.key) || (k.equals(this.key))) && ((v == this.val) || (v.equals(this.val)));
/* 2992:     */     }
/* 2993:     */     
/* 2994:     */     public V setValue(V value)
/* 2995:     */     {
/* 2996:3303 */       if (value == null) {
/* 2997:3303 */         throw new NullPointerException();
/* 2998:     */       }
/* 2999:3304 */       V v = this.val;
/* 3000:3305 */       this.val = value;
/* 3001:3306 */       this.map.put(this.key, value);
/* 3002:3307 */       return v;
/* 3003:     */     }
/* 3004:     */   }
/* 3005:     */   
/* 3006:     */   static final class KeySpliterator<K, V>
/* 3007:     */     extends ConcurrentHashMapV8.Traverser<K, V>
/* 3008:     */     implements ConcurrentHashMapV8.ConcurrentHashMapSpliterator<K>
/* 3009:     */   {
/* 3010:     */     long est;
/* 3011:     */     
/* 3012:     */     KeySpliterator(ConcurrentHashMapV8.Node<K, V>[] tab, int size, int index, int limit, long est)
/* 3013:     */     {
/* 3014:3316 */       super(size, index, limit);
/* 3015:3317 */       this.est = est;
/* 3016:     */     }
/* 3017:     */     
/* 3018:     */     public ConcurrentHashMapV8.ConcurrentHashMapSpliterator<K> trySplit()
/* 3019:     */     {
/* 3020:     */       int i;
/* 3021:     */       int f;
/* 3022:     */       int h;
/* 3023:3322 */       return (h = (i = this.baseIndex) + (f = this.baseLimit) >>> 1) <= i ? null : new KeySpliterator(this.tab, this.baseSize, this.baseLimit = h, f, this.est >>>= 1);
/* 3024:     */     }
/* 3025:     */     
/* 3026:     */     public void forEachRemaining(ConcurrentHashMapV8.Action<? super K> action)
/* 3027:     */     {
/* 3028:3328 */       if (action == null) {
/* 3029:3328 */         throw new NullPointerException();
/* 3030:     */       }
/* 3031:     */       ConcurrentHashMapV8.Node<K, V> p;
/* 3032:3329 */       while ((p = advance()) != null) {
/* 3033:3330 */         action.apply(p.key);
/* 3034:     */       }
/* 3035:     */     }
/* 3036:     */     
/* 3037:     */     public boolean tryAdvance(ConcurrentHashMapV8.Action<? super K> action)
/* 3038:     */     {
/* 3039:3334 */       if (action == null) {
/* 3040:3334 */         throw new NullPointerException();
/* 3041:     */       }
/* 3042:     */       ConcurrentHashMapV8.Node<K, V> p;
/* 3043:3336 */       if ((p = advance()) == null) {
/* 3044:3337 */         return false;
/* 3045:     */       }
/* 3046:3338 */       action.apply(p.key);
/* 3047:3339 */       return true;
/* 3048:     */     }
/* 3049:     */     
/* 3050:     */     public long estimateSize()
/* 3051:     */     {
/* 3052:3342 */       return this.est;
/* 3053:     */     }
/* 3054:     */   }
/* 3055:     */   
/* 3056:     */   static final class ValueSpliterator<K, V>
/* 3057:     */     extends ConcurrentHashMapV8.Traverser<K, V>
/* 3058:     */     implements ConcurrentHashMapV8.ConcurrentHashMapSpliterator<V>
/* 3059:     */   {
/* 3060:     */     long est;
/* 3061:     */     
/* 3062:     */     ValueSpliterator(ConcurrentHashMapV8.Node<K, V>[] tab, int size, int index, int limit, long est)
/* 3063:     */     {
/* 3064:3351 */       super(size, index, limit);
/* 3065:3352 */       this.est = est;
/* 3066:     */     }
/* 3067:     */     
/* 3068:     */     public ConcurrentHashMapV8.ConcurrentHashMapSpliterator<V> trySplit()
/* 3069:     */     {
/* 3070:     */       int i;
/* 3071:     */       int f;
/* 3072:     */       int h;
/* 3073:3357 */       return (h = (i = this.baseIndex) + (f = this.baseLimit) >>> 1) <= i ? null : new ValueSpliterator(this.tab, this.baseSize, this.baseLimit = h, f, this.est >>>= 1);
/* 3074:     */     }
/* 3075:     */     
/* 3076:     */     public void forEachRemaining(ConcurrentHashMapV8.Action<? super V> action)
/* 3077:     */     {
/* 3078:3363 */       if (action == null) {
/* 3079:3363 */         throw new NullPointerException();
/* 3080:     */       }
/* 3081:     */       ConcurrentHashMapV8.Node<K, V> p;
/* 3082:3364 */       while ((p = advance()) != null) {
/* 3083:3365 */         action.apply(p.val);
/* 3084:     */       }
/* 3085:     */     }
/* 3086:     */     
/* 3087:     */     public boolean tryAdvance(ConcurrentHashMapV8.Action<? super V> action)
/* 3088:     */     {
/* 3089:3369 */       if (action == null) {
/* 3090:3369 */         throw new NullPointerException();
/* 3091:     */       }
/* 3092:     */       ConcurrentHashMapV8.Node<K, V> p;
/* 3093:3371 */       if ((p = advance()) == null) {
/* 3094:3372 */         return false;
/* 3095:     */       }
/* 3096:3373 */       action.apply(p.val);
/* 3097:3374 */       return true;
/* 3098:     */     }
/* 3099:     */     
/* 3100:     */     public long estimateSize()
/* 3101:     */     {
/* 3102:3377 */       return this.est;
/* 3103:     */     }
/* 3104:     */   }
/* 3105:     */   
/* 3106:     */   static final class EntrySpliterator<K, V>
/* 3107:     */     extends ConcurrentHashMapV8.Traverser<K, V>
/* 3108:     */     implements ConcurrentHashMapV8.ConcurrentHashMapSpliterator<Map.Entry<K, V>>
/* 3109:     */   {
/* 3110:     */     final ConcurrentHashMapV8<K, V> map;
/* 3111:     */     long est;
/* 3112:     */     
/* 3113:     */     EntrySpliterator(ConcurrentHashMapV8.Node<K, V>[] tab, int size, int index, int limit, long est, ConcurrentHashMapV8<K, V> map)
/* 3114:     */     {
/* 3115:3387 */       super(size, index, limit);
/* 3116:3388 */       this.map = map;
/* 3117:3389 */       this.est = est;
/* 3118:     */     }
/* 3119:     */     
/* 3120:     */     public ConcurrentHashMapV8.ConcurrentHashMapSpliterator<Map.Entry<K, V>> trySplit()
/* 3121:     */     {
/* 3122:     */       int i;
/* 3123:     */       int f;
/* 3124:     */       int h;
/* 3125:3394 */       return (h = (i = this.baseIndex) + (f = this.baseLimit) >>> 1) <= i ? null : new EntrySpliterator(this.tab, this.baseSize, this.baseLimit = h, f, this.est >>>= 1, this.map);
/* 3126:     */     }
/* 3127:     */     
/* 3128:     */     public void forEachRemaining(ConcurrentHashMapV8.Action<? super Map.Entry<K, V>> action)
/* 3129:     */     {
/* 3130:3400 */       if (action == null) {
/* 3131:3400 */         throw new NullPointerException();
/* 3132:     */       }
/* 3133:     */       ConcurrentHashMapV8.Node<K, V> p;
/* 3134:3401 */       while ((p = advance()) != null) {
/* 3135:3402 */         action.apply(new ConcurrentHashMapV8.MapEntry(p.key, p.val, this.map));
/* 3136:     */       }
/* 3137:     */     }
/* 3138:     */     
/* 3139:     */     public boolean tryAdvance(ConcurrentHashMapV8.Action<? super Map.Entry<K, V>> action)
/* 3140:     */     {
/* 3141:3406 */       if (action == null) {
/* 3142:3406 */         throw new NullPointerException();
/* 3143:     */       }
/* 3144:     */       ConcurrentHashMapV8.Node<K, V> p;
/* 3145:3408 */       if ((p = advance()) == null) {
/* 3146:3409 */         return false;
/* 3147:     */       }
/* 3148:3410 */       action.apply(new ConcurrentHashMapV8.MapEntry(p.key, p.val, this.map));
/* 3149:3411 */       return true;
/* 3150:     */     }
/* 3151:     */     
/* 3152:     */     public long estimateSize()
/* 3153:     */     {
/* 3154:3414 */       return this.est;
/* 3155:     */     }
/* 3156:     */   }
/* 3157:     */   
/* 3158:     */   final int batchFor(long b)
/* 3159:     */   {
/* 3160:     */     long n;
/* 3161:3430 */     if ((b == 9223372036854775807L) || ((n = sumCount()) <= 1L) || (n < b)) {
/* 3162:3431 */       return 0;
/* 3163:     */     }
/* 3164:     */     long n;
/* 3165:3432 */     int sp = ForkJoinPool.getCommonPoolParallelism() << 2;
/* 3166:3433 */     return (b <= 0L) || (n /= b >= sp) ? sp : (int)n;
/* 3167:     */   }
/* 3168:     */   
/* 3169:     */   public void forEach(long parallelismThreshold, BiAction<? super K, ? super V> action)
/* 3170:     */   {
/* 3171:3446 */     if (action == null) {
/* 3172:3446 */       throw new NullPointerException();
/* 3173:     */     }
/* 3174:3447 */     new ForEachMappingTask(null, batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
/* 3175:     */   }
/* 3176:     */   
/* 3177:     */   public <U> void forEach(long parallelismThreshold, BiFun<? super K, ? super V, ? extends U> transformer, Action<? super U> action)
/* 3178:     */   {
/* 3179:3467 */     if ((transformer == null) || (action == null)) {
/* 3180:3468 */       throw new NullPointerException();
/* 3181:     */     }
/* 3182:3469 */     new ForEachTransformedMappingTask(null, batchFor(parallelismThreshold), 0, 0, this.table, transformer, action).invoke();
/* 3183:     */   }
/* 3184:     */   
/* 3185:     */   public <U> U search(long parallelismThreshold, BiFun<? super K, ? super V, ? extends U> searchFunction)
/* 3186:     */   {
/* 3187:3491 */     if (searchFunction == null) {
/* 3188:3491 */       throw new NullPointerException();
/* 3189:     */     }
/* 3190:3492 */     return new SearchMappingsTask(null, batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()).invoke();
/* 3191:     */   }
/* 3192:     */   
/* 3193:     */   public <U> U reduce(long parallelismThreshold, BiFun<? super K, ? super V, ? extends U> transformer, BiFun<? super U, ? super U, ? extends U> reducer)
/* 3194:     */   {
/* 3195:3515 */     if ((transformer == null) || (reducer == null)) {
/* 3196:3516 */       throw new NullPointerException();
/* 3197:     */     }
/* 3198:3517 */     return new MapReduceMappingsTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer).invoke();
/* 3199:     */   }
/* 3200:     */   
/* 3201:     */   public double reduceToDouble(long parallelismThreshold, ObjectByObjectToDouble<? super K, ? super V> transformer, double basis, DoubleByDoubleToDouble reducer)
/* 3202:     */   {
/* 3203:3541 */     if ((transformer == null) || (reducer == null)) {
/* 3204:3542 */       throw new NullPointerException();
/* 3205:     */     }
/* 3206:3543 */     return ((Double)new MapReduceMappingsToDoubleTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).doubleValue();
/* 3207:     */   }
/* 3208:     */   
/* 3209:     */   public long reduceToLong(long parallelismThreshold, ObjectByObjectToLong<? super K, ? super V> transformer, long basis, LongByLongToLong reducer)
/* 3210:     */   {
/* 3211:3567 */     if ((transformer == null) || (reducer == null)) {
/* 3212:3568 */       throw new NullPointerException();
/* 3213:     */     }
/* 3214:3569 */     return ((Long)new MapReduceMappingsToLongTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).longValue();
/* 3215:     */   }
/* 3216:     */   
/* 3217:     */   public int reduceToInt(long parallelismThreshold, ObjectByObjectToInt<? super K, ? super V> transformer, int basis, IntByIntToInt reducer)
/* 3218:     */   {
/* 3219:3593 */     if ((transformer == null) || (reducer == null)) {
/* 3220:3594 */       throw new NullPointerException();
/* 3221:     */     }
/* 3222:3595 */     return ((Integer)new MapReduceMappingsToIntTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).intValue();
/* 3223:     */   }
/* 3224:     */   
/* 3225:     */   public void forEachKey(long parallelismThreshold, Action<? super K> action)
/* 3226:     */   {
/* 3227:3610 */     if (action == null) {
/* 3228:3610 */       throw new NullPointerException();
/* 3229:     */     }
/* 3230:3611 */     new ForEachKeyTask(null, batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
/* 3231:     */   }
/* 3232:     */   
/* 3233:     */   public <U> void forEachKey(long parallelismThreshold, Fun<? super K, ? extends U> transformer, Action<? super U> action)
/* 3234:     */   {
/* 3235:3631 */     if ((transformer == null) || (action == null)) {
/* 3236:3632 */       throw new NullPointerException();
/* 3237:     */     }
/* 3238:3633 */     new ForEachTransformedKeyTask(null, batchFor(parallelismThreshold), 0, 0, this.table, transformer, action).invoke();
/* 3239:     */   }
/* 3240:     */   
/* 3241:     */   public <U> U searchKeys(long parallelismThreshold, Fun<? super K, ? extends U> searchFunction)
/* 3242:     */   {
/* 3243:3655 */     if (searchFunction == null) {
/* 3244:3655 */       throw new NullPointerException();
/* 3245:     */     }
/* 3246:3656 */     return new SearchKeysTask(null, batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()).invoke();
/* 3247:     */   }
/* 3248:     */   
/* 3249:     */   public K reduceKeys(long parallelismThreshold, BiFun<? super K, ? super K, ? extends K> reducer)
/* 3250:     */   {
/* 3251:3674 */     if (reducer == null) {
/* 3252:3674 */       throw new NullPointerException();
/* 3253:     */     }
/* 3254:3675 */     return new ReduceKeysTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, reducer).invoke();
/* 3255:     */   }
/* 3256:     */   
/* 3257:     */   public <U> U reduceKeys(long parallelismThreshold, Fun<? super K, ? extends U> transformer, BiFun<? super U, ? super U, ? extends U> reducer)
/* 3258:     */   {
/* 3259:3698 */     if ((transformer == null) || (reducer == null)) {
/* 3260:3699 */       throw new NullPointerException();
/* 3261:     */     }
/* 3262:3700 */     return new MapReduceKeysTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer).invoke();
/* 3263:     */   }
/* 3264:     */   
/* 3265:     */   public double reduceKeysToDouble(long parallelismThreshold, ObjectToDouble<? super K> transformer, double basis, DoubleByDoubleToDouble reducer)
/* 3266:     */   {
/* 3267:3724 */     if ((transformer == null) || (reducer == null)) {
/* 3268:3725 */       throw new NullPointerException();
/* 3269:     */     }
/* 3270:3726 */     return ((Double)new MapReduceKeysToDoubleTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).doubleValue();
/* 3271:     */   }
/* 3272:     */   
/* 3273:     */   public long reduceKeysToLong(long parallelismThreshold, ObjectToLong<? super K> transformer, long basis, LongByLongToLong reducer)
/* 3274:     */   {
/* 3275:3750 */     if ((transformer == null) || (reducer == null)) {
/* 3276:3751 */       throw new NullPointerException();
/* 3277:     */     }
/* 3278:3752 */     return ((Long)new MapReduceKeysToLongTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).longValue();
/* 3279:     */   }
/* 3280:     */   
/* 3281:     */   public int reduceKeysToInt(long parallelismThreshold, ObjectToInt<? super K> transformer, int basis, IntByIntToInt reducer)
/* 3282:     */   {
/* 3283:3776 */     if ((transformer == null) || (reducer == null)) {
/* 3284:3777 */       throw new NullPointerException();
/* 3285:     */     }
/* 3286:3778 */     return ((Integer)new MapReduceKeysToIntTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).intValue();
/* 3287:     */   }
/* 3288:     */   
/* 3289:     */   public void forEachValue(long parallelismThreshold, Action<? super V> action)
/* 3290:     */   {
/* 3291:3793 */     if (action == null) {
/* 3292:3794 */       throw new NullPointerException();
/* 3293:     */     }
/* 3294:3795 */     new ForEachValueTask(null, batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
/* 3295:     */   }
/* 3296:     */   
/* 3297:     */   public <U> void forEachValue(long parallelismThreshold, Fun<? super V, ? extends U> transformer, Action<? super U> action)
/* 3298:     */   {
/* 3299:3815 */     if ((transformer == null) || (action == null)) {
/* 3300:3816 */       throw new NullPointerException();
/* 3301:     */     }
/* 3302:3817 */     new ForEachTransformedValueTask(null, batchFor(parallelismThreshold), 0, 0, this.table, transformer, action).invoke();
/* 3303:     */   }
/* 3304:     */   
/* 3305:     */   public <U> U searchValues(long parallelismThreshold, Fun<? super V, ? extends U> searchFunction)
/* 3306:     */   {
/* 3307:3839 */     if (searchFunction == null) {
/* 3308:3839 */       throw new NullPointerException();
/* 3309:     */     }
/* 3310:3840 */     return new SearchValuesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()).invoke();
/* 3311:     */   }
/* 3312:     */   
/* 3313:     */   public V reduceValues(long parallelismThreshold, BiFun<? super V, ? super V, ? extends V> reducer)
/* 3314:     */   {
/* 3315:3857 */     if (reducer == null) {
/* 3316:3857 */       throw new NullPointerException();
/* 3317:     */     }
/* 3318:3858 */     return new ReduceValuesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, reducer).invoke();
/* 3319:     */   }
/* 3320:     */   
/* 3321:     */   public <U> U reduceValues(long parallelismThreshold, Fun<? super V, ? extends U> transformer, BiFun<? super U, ? super U, ? extends U> reducer)
/* 3322:     */   {
/* 3323:3881 */     if ((transformer == null) || (reducer == null)) {
/* 3324:3882 */       throw new NullPointerException();
/* 3325:     */     }
/* 3326:3883 */     return new MapReduceValuesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer).invoke();
/* 3327:     */   }
/* 3328:     */   
/* 3329:     */   public double reduceValuesToDouble(long parallelismThreshold, ObjectToDouble<? super V> transformer, double basis, DoubleByDoubleToDouble reducer)
/* 3330:     */   {
/* 3331:3907 */     if ((transformer == null) || (reducer == null)) {
/* 3332:3908 */       throw new NullPointerException();
/* 3333:     */     }
/* 3334:3909 */     return ((Double)new MapReduceValuesToDoubleTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).doubleValue();
/* 3335:     */   }
/* 3336:     */   
/* 3337:     */   public long reduceValuesToLong(long parallelismThreshold, ObjectToLong<? super V> transformer, long basis, LongByLongToLong reducer)
/* 3338:     */   {
/* 3339:3933 */     if ((transformer == null) || (reducer == null)) {
/* 3340:3934 */       throw new NullPointerException();
/* 3341:     */     }
/* 3342:3935 */     return ((Long)new MapReduceValuesToLongTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).longValue();
/* 3343:     */   }
/* 3344:     */   
/* 3345:     */   public int reduceValuesToInt(long parallelismThreshold, ObjectToInt<? super V> transformer, int basis, IntByIntToInt reducer)
/* 3346:     */   {
/* 3347:3959 */     if ((transformer == null) || (reducer == null)) {
/* 3348:3960 */       throw new NullPointerException();
/* 3349:     */     }
/* 3350:3961 */     return ((Integer)new MapReduceValuesToIntTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).intValue();
/* 3351:     */   }
/* 3352:     */   
/* 3353:     */   public void forEachEntry(long parallelismThreshold, Action<? super Map.Entry<K, V>> action)
/* 3354:     */   {
/* 3355:3976 */     if (action == null) {
/* 3356:3976 */       throw new NullPointerException();
/* 3357:     */     }
/* 3358:3977 */     new ForEachEntryTask(null, batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
/* 3359:     */   }
/* 3360:     */   
/* 3361:     */   public <U> void forEachEntry(long parallelismThreshold, Fun<Map.Entry<K, V>, ? extends U> transformer, Action<? super U> action)
/* 3362:     */   {
/* 3363:3996 */     if ((transformer == null) || (action == null)) {
/* 3364:3997 */       throw new NullPointerException();
/* 3365:     */     }
/* 3366:3998 */     new ForEachTransformedEntryTask(null, batchFor(parallelismThreshold), 0, 0, this.table, transformer, action).invoke();
/* 3367:     */   }
/* 3368:     */   
/* 3369:     */   public <U> U searchEntries(long parallelismThreshold, Fun<Map.Entry<K, V>, ? extends U> searchFunction)
/* 3370:     */   {
/* 3371:4020 */     if (searchFunction == null) {
/* 3372:4020 */       throw new NullPointerException();
/* 3373:     */     }
/* 3374:4021 */     return new SearchEntriesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()).invoke();
/* 3375:     */   }
/* 3376:     */   
/* 3377:     */   public Map.Entry<K, V> reduceEntries(long parallelismThreshold, BiFun<Map.Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> reducer)
/* 3378:     */   {
/* 3379:4038 */     if (reducer == null) {
/* 3380:4038 */       throw new NullPointerException();
/* 3381:     */     }
/* 3382:4039 */     return (Map.Entry)new ReduceEntriesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, reducer).invoke();
/* 3383:     */   }
/* 3384:     */   
/* 3385:     */   public <U> U reduceEntries(long parallelismThreshold, Fun<Map.Entry<K, V>, ? extends U> transformer, BiFun<? super U, ? super U, ? extends U> reducer)
/* 3386:     */   {
/* 3387:4062 */     if ((transformer == null) || (reducer == null)) {
/* 3388:4063 */       throw new NullPointerException();
/* 3389:     */     }
/* 3390:4064 */     return new MapReduceEntriesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer).invoke();
/* 3391:     */   }
/* 3392:     */   
/* 3393:     */   public double reduceEntriesToDouble(long parallelismThreshold, ObjectToDouble<Map.Entry<K, V>> transformer, double basis, DoubleByDoubleToDouble reducer)
/* 3394:     */   {
/* 3395:4088 */     if ((transformer == null) || (reducer == null)) {
/* 3396:4089 */       throw new NullPointerException();
/* 3397:     */     }
/* 3398:4090 */     return ((Double)new MapReduceEntriesToDoubleTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).doubleValue();
/* 3399:     */   }
/* 3400:     */   
/* 3401:     */   public long reduceEntriesToLong(long parallelismThreshold, ObjectToLong<Map.Entry<K, V>> transformer, long basis, LongByLongToLong reducer)
/* 3402:     */   {
/* 3403:4114 */     if ((transformer == null) || (reducer == null)) {
/* 3404:4115 */       throw new NullPointerException();
/* 3405:     */     }
/* 3406:4116 */     return ((Long)new MapReduceEntriesToLongTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).longValue();
/* 3407:     */   }
/* 3408:     */   
/* 3409:     */   public int reduceEntriesToInt(long parallelismThreshold, ObjectToInt<Map.Entry<K, V>> transformer, int basis, IntByIntToInt reducer)
/* 3410:     */   {
/* 3411:4140 */     if ((transformer == null) || (reducer == null)) {
/* 3412:4141 */       throw new NullPointerException();
/* 3413:     */     }
/* 3414:4142 */     return ((Integer)new MapReduceEntriesToIntTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).intValue();
/* 3415:     */   }
/* 3416:     */   
/* 3417:     */   static abstract class CollectionView<K, V, E>
/* 3418:     */     implements Collection<E>, Serializable
/* 3419:     */   {
/* 3420:     */     private static final long serialVersionUID = 7249069246763182397L;
/* 3421:     */     final ConcurrentHashMapV8<K, V> map;
/* 3422:     */     private static final String oomeMsg = "Required array size too large";
/* 3423:     */     
/* 3424:     */     CollectionView(ConcurrentHashMapV8<K, V> map)
/* 3425:     */     {
/* 3426:4157 */       this.map = map;
/* 3427:     */     }
/* 3428:     */     
/* 3429:     */     public ConcurrentHashMapV8<K, V> getMap()
/* 3430:     */     {
/* 3431:4164 */       return this.map;
/* 3432:     */     }
/* 3433:     */     
/* 3434:     */     public final void clear()
/* 3435:     */     {
/* 3436:4170 */       this.map.clear();
/* 3437:     */     }
/* 3438:     */     
/* 3439:     */     public final int size()
/* 3440:     */     {
/* 3441:4171 */       return this.map.size();
/* 3442:     */     }
/* 3443:     */     
/* 3444:     */     public final boolean isEmpty()
/* 3445:     */     {
/* 3446:4172 */       return this.map.isEmpty();
/* 3447:     */     }
/* 3448:     */     
/* 3449:     */     public abstract Iterator<E> iterator();
/* 3450:     */     
/* 3451:     */     public abstract boolean contains(Object paramObject);
/* 3452:     */     
/* 3453:     */     public abstract boolean remove(Object paramObject);
/* 3454:     */     
/* 3455:     */     public final Object[] toArray()
/* 3456:     */     {
/* 3457:4191 */       long sz = this.map.mappingCount();
/* 3458:4192 */       if (sz > 2147483639L) {
/* 3459:4193 */         throw new OutOfMemoryError("Required array size too large");
/* 3460:     */       }
/* 3461:4194 */       int n = (int)sz;
/* 3462:4195 */       Object[] r = new Object[n];
/* 3463:4196 */       int i = 0;
/* 3464:4197 */       for (E e : this)
/* 3465:     */       {
/* 3466:4198 */         if (i == n)
/* 3467:     */         {
/* 3468:4199 */           if (n >= 2147483639) {
/* 3469:4200 */             throw new OutOfMemoryError("Required array size too large");
/* 3470:     */           }
/* 3471:4201 */           if (n >= 1073741819) {
/* 3472:4202 */             n = 2147483639;
/* 3473:     */           } else {
/* 3474:4204 */             n += (n >>> 1) + 1;
/* 3475:     */           }
/* 3476:4205 */           r = Arrays.copyOf(r, n);
/* 3477:     */         }
/* 3478:4207 */         r[(i++)] = e;
/* 3479:     */       }
/* 3480:4209 */       return i == n ? r : Arrays.copyOf(r, i);
/* 3481:     */     }
/* 3482:     */     
/* 3483:     */     public final <T> T[] toArray(T[] a)
/* 3484:     */     {
/* 3485:4214 */       long sz = this.map.mappingCount();
/* 3486:4215 */       if (sz > 2147483639L) {
/* 3487:4216 */         throw new OutOfMemoryError("Required array size too large");
/* 3488:     */       }
/* 3489:4217 */       int m = (int)sz;
/* 3490:4218 */       T[] r = a.length >= m ? a : (Object[])Array.newInstance(a.getClass().getComponentType(), m);
/* 3491:     */       
/* 3492:     */ 
/* 3493:4221 */       int n = r.length;
/* 3494:4222 */       int i = 0;
/* 3495:4223 */       for (E e : this)
/* 3496:     */       {
/* 3497:4224 */         if (i == n)
/* 3498:     */         {
/* 3499:4225 */           if (n >= 2147483639) {
/* 3500:4226 */             throw new OutOfMemoryError("Required array size too large");
/* 3501:     */           }
/* 3502:4227 */           if (n >= 1073741819) {
/* 3503:4228 */             n = 2147483639;
/* 3504:     */           } else {
/* 3505:4230 */             n += (n >>> 1) + 1;
/* 3506:     */           }
/* 3507:4231 */           r = Arrays.copyOf(r, n);
/* 3508:     */         }
/* 3509:4233 */         r[(i++)] = e;
/* 3510:     */       }
/* 3511:4235 */       if ((a == r) && (i < n))
/* 3512:     */       {
/* 3513:4236 */         r[i] = null;
/* 3514:4237 */         return r;
/* 3515:     */       }
/* 3516:4239 */       return i == n ? r : Arrays.copyOf(r, i);
/* 3517:     */     }
/* 3518:     */     
/* 3519:     */     public final String toString()
/* 3520:     */     {
/* 3521:4254 */       StringBuilder sb = new StringBuilder();
/* 3522:4255 */       sb.append('[');
/* 3523:4256 */       Iterator<E> it = iterator();
/* 3524:4257 */       if (it.hasNext()) {
/* 3525:     */         for (;;)
/* 3526:     */         {
/* 3527:4259 */           Object e = it.next();
/* 3528:4260 */           sb.append(e == this ? "(this Collection)" : e);
/* 3529:4261 */           if (!it.hasNext()) {
/* 3530:     */             break;
/* 3531:     */           }
/* 3532:4263 */           sb.append(',').append(' ');
/* 3533:     */         }
/* 3534:     */       }
/* 3535:4266 */       return ']';
/* 3536:     */     }
/* 3537:     */     
/* 3538:     */     public final boolean containsAll(Collection<?> c)
/* 3539:     */     {
/* 3540:4270 */       if (c != this) {
/* 3541:4271 */         for (Object e : c) {
/* 3542:4272 */           if ((e == null) || (!contains(e))) {
/* 3543:4273 */             return false;
/* 3544:     */           }
/* 3545:     */         }
/* 3546:     */       }
/* 3547:4276 */       return true;
/* 3548:     */     }
/* 3549:     */     
/* 3550:     */     public final boolean removeAll(Collection<?> c)
/* 3551:     */     {
/* 3552:4280 */       boolean modified = false;
/* 3553:4281 */       for (Iterator<E> it = iterator(); it.hasNext();) {
/* 3554:4282 */         if (c.contains(it.next()))
/* 3555:     */         {
/* 3556:4283 */           it.remove();
/* 3557:4284 */           modified = true;
/* 3558:     */         }
/* 3559:     */       }
/* 3560:4287 */       return modified;
/* 3561:     */     }
/* 3562:     */     
/* 3563:     */     public final boolean retainAll(Collection<?> c)
/* 3564:     */     {
/* 3565:4291 */       boolean modified = false;
/* 3566:4292 */       for (Iterator<E> it = iterator(); it.hasNext();) {
/* 3567:4293 */         if (!c.contains(it.next()))
/* 3568:     */         {
/* 3569:4294 */           it.remove();
/* 3570:4295 */           modified = true;
/* 3571:     */         }
/* 3572:     */       }
/* 3573:4298 */       return modified;
/* 3574:     */     }
/* 3575:     */   }
/* 3576:     */   
/* 3577:     */   public static class KeySetView<K, V>
/* 3578:     */     extends ConcurrentHashMapV8.CollectionView<K, V, K>
/* 3579:     */     implements Set<K>, Serializable
/* 3580:     */   {
/* 3581:     */     private static final long serialVersionUID = 7249069246763182397L;
/* 3582:     */     private final V value;
/* 3583:     */     
/* 3584:     */     KeySetView(ConcurrentHashMapV8<K, V> map, V value)
/* 3585:     */     {
/* 3586:4319 */       super();
/* 3587:4320 */       this.value = value;
/* 3588:     */     }
/* 3589:     */     
/* 3590:     */     public V getMappedValue()
/* 3591:     */     {
/* 3592:4330 */       return this.value;
/* 3593:     */     }
/* 3594:     */     
/* 3595:     */     public boolean contains(Object o)
/* 3596:     */     {
/* 3597:4336 */       return this.map.containsKey(o);
/* 3598:     */     }
/* 3599:     */     
/* 3600:     */     public boolean remove(Object o)
/* 3601:     */     {
/* 3602:4347 */       return this.map.remove(o) != null;
/* 3603:     */     }
/* 3604:     */     
/* 3605:     */     public Iterator<K> iterator()
/* 3606:     */     {
/* 3607:4354 */       ConcurrentHashMapV8<K, V> m = this.map;
/* 3608:     */       ConcurrentHashMapV8.Node<K, V>[] t;
/* 3609:4355 */       int f = (t = m.table) == null ? 0 : t.length;
/* 3610:4356 */       return new ConcurrentHashMapV8.KeyIterator(t, f, 0, f, m);
/* 3611:     */     }
/* 3612:     */     
/* 3613:     */     public boolean add(K e)
/* 3614:     */     {
/* 3615:     */       V v;
/* 3616:4371 */       if ((v = this.value) == null) {
/* 3617:4372 */         throw new UnsupportedOperationException();
/* 3618:     */       }
/* 3619:4373 */       return this.map.putVal(e, v, true) == null;
/* 3620:     */     }
/* 3621:     */     
/* 3622:     */     public boolean addAll(Collection<? extends K> c)
/* 3623:     */     {
/* 3624:4388 */       boolean added = false;
/* 3625:     */       V v;
/* 3626:4390 */       if ((v = this.value) == null) {
/* 3627:4391 */         throw new UnsupportedOperationException();
/* 3628:     */       }
/* 3629:4392 */       for (K e : c) {
/* 3630:4393 */         if (this.map.putVal(e, v, true) == null) {
/* 3631:4394 */           added = true;
/* 3632:     */         }
/* 3633:     */       }
/* 3634:4396 */       return added;
/* 3635:     */     }
/* 3636:     */     
/* 3637:     */     public int hashCode()
/* 3638:     */     {
/* 3639:4400 */       int h = 0;
/* 3640:4401 */       for (K e : this) {
/* 3641:4402 */         h += e.hashCode();
/* 3642:     */       }
/* 3643:4403 */       return h;
/* 3644:     */     }
/* 3645:     */     
/* 3646:     */     public boolean equals(Object o)
/* 3647:     */     {
/* 3648:     */       Set<?> c;
/* 3649:4408 */       return ((o instanceof Set)) && (((c = (Set)o) == this) || ((containsAll(c)) && (c.containsAll(this))));
/* 3650:     */     }
/* 3651:     */     
/* 3652:     */     public ConcurrentHashMapV8.ConcurrentHashMapSpliterator<K> spliterator166()
/* 3653:     */     {
/* 3654:4415 */       ConcurrentHashMapV8<K, V> m = this.map;
/* 3655:4416 */       long n = m.sumCount();
/* 3656:     */       ConcurrentHashMapV8.Node<K, V>[] t;
/* 3657:4417 */       int f = (t = m.table) == null ? 0 : t.length;
/* 3658:4418 */       return new ConcurrentHashMapV8.KeySpliterator(t, f, 0, f, n < 0L ? 0L : n);
/* 3659:     */     }
/* 3660:     */     
/* 3661:     */     public void forEach(ConcurrentHashMapV8.Action<? super K> action)
/* 3662:     */     {
/* 3663:4422 */       if (action == null) {
/* 3664:4422 */         throw new NullPointerException();
/* 3665:     */       }
/* 3666:     */       ConcurrentHashMapV8.Node<K, V>[] t;
/* 3667:4424 */       if ((t = this.map.table) != null)
/* 3668:     */       {
/* 3669:4425 */         ConcurrentHashMapV8.Traverser<K, V> it = new ConcurrentHashMapV8.Traverser(t, t.length, 0, t.length);
/* 3670:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 3671:4426 */         while ((p = it.advance()) != null) {
/* 3672:4427 */           action.apply(p.key);
/* 3673:     */         }
/* 3674:     */       }
/* 3675:     */     }
/* 3676:     */   }
/* 3677:     */   
/* 3678:     */   static final class ValuesView<K, V>
/* 3679:     */     extends ConcurrentHashMapV8.CollectionView<K, V, V>
/* 3680:     */     implements Collection<V>, Serializable
/* 3681:     */   {
/* 3682:     */     private static final long serialVersionUID = 2249069246763182397L;
/* 3683:     */     
/* 3684:     */     ValuesView(ConcurrentHashMapV8<K, V> map)
/* 3685:     */     {
/* 3686:4440 */       super();
/* 3687:     */     }
/* 3688:     */     
/* 3689:     */     public final boolean contains(Object o)
/* 3690:     */     {
/* 3691:4442 */       return this.map.containsValue(o);
/* 3692:     */     }
/* 3693:     */     
/* 3694:     */     public final boolean remove(Object o)
/* 3695:     */     {
/* 3696:     */       Iterator<V> it;
/* 3697:4446 */       if (o != null) {
/* 3698:4447 */         for (it = iterator(); it.hasNext();) {
/* 3699:4448 */           if (o.equals(it.next()))
/* 3700:     */           {
/* 3701:4449 */             it.remove();
/* 3702:4450 */             return true;
/* 3703:     */           }
/* 3704:     */         }
/* 3705:     */       }
/* 3706:4454 */       return false;
/* 3707:     */     }
/* 3708:     */     
/* 3709:     */     public final Iterator<V> iterator()
/* 3710:     */     {
/* 3711:4458 */       ConcurrentHashMapV8<K, V> m = this.map;
/* 3712:     */       ConcurrentHashMapV8.Node<K, V>[] t;
/* 3713:4460 */       int f = (t = m.table) == null ? 0 : t.length;
/* 3714:4461 */       return new ConcurrentHashMapV8.ValueIterator(t, f, 0, f, m);
/* 3715:     */     }
/* 3716:     */     
/* 3717:     */     public final boolean add(V e)
/* 3718:     */     {
/* 3719:4465 */       throw new UnsupportedOperationException();
/* 3720:     */     }
/* 3721:     */     
/* 3722:     */     public final boolean addAll(Collection<? extends V> c)
/* 3723:     */     {
/* 3724:4468 */       throw new UnsupportedOperationException();
/* 3725:     */     }
/* 3726:     */     
/* 3727:     */     public ConcurrentHashMapV8.ConcurrentHashMapSpliterator<V> spliterator166()
/* 3728:     */     {
/* 3729:4473 */       ConcurrentHashMapV8<K, V> m = this.map;
/* 3730:4474 */       long n = m.sumCount();
/* 3731:     */       ConcurrentHashMapV8.Node<K, V>[] t;
/* 3732:4475 */       int f = (t = m.table) == null ? 0 : t.length;
/* 3733:4476 */       return new ConcurrentHashMapV8.ValueSpliterator(t, f, 0, f, n < 0L ? 0L : n);
/* 3734:     */     }
/* 3735:     */     
/* 3736:     */     public void forEach(ConcurrentHashMapV8.Action<? super V> action)
/* 3737:     */     {
/* 3738:4480 */       if (action == null) {
/* 3739:4480 */         throw new NullPointerException();
/* 3740:     */       }
/* 3741:     */       ConcurrentHashMapV8.Node<K, V>[] t;
/* 3742:4482 */       if ((t = this.map.table) != null)
/* 3743:     */       {
/* 3744:4483 */         ConcurrentHashMapV8.Traverser<K, V> it = new ConcurrentHashMapV8.Traverser(t, t.length, 0, t.length);
/* 3745:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 3746:4484 */         while ((p = it.advance()) != null) {
/* 3747:4485 */           action.apply(p.val);
/* 3748:     */         }
/* 3749:     */       }
/* 3750:     */     }
/* 3751:     */   }
/* 3752:     */   
/* 3753:     */   static final class EntrySetView<K, V>
/* 3754:     */     extends ConcurrentHashMapV8.CollectionView<K, V, Map.Entry<K, V>>
/* 3755:     */     implements Set<Map.Entry<K, V>>, Serializable
/* 3756:     */   {
/* 3757:     */     private static final long serialVersionUID = 2249069246763182397L;
/* 3758:     */     
/* 3759:     */     EntrySetView(ConcurrentHashMapV8<K, V> map)
/* 3760:     */     {
/* 3761:4498 */       super();
/* 3762:     */     }
/* 3763:     */     
/* 3764:     */     public boolean contains(Object o)
/* 3765:     */     {
/* 3766:     */       Map.Entry<?, ?> e;
/* 3767:     */       Object k;
/* 3768:     */       Object r;
/* 3769:     */       Object v;
/* 3770:4502 */       return ((o instanceof Map.Entry)) && ((k = (e = (Map.Entry)o).getKey()) != null) && ((r = this.map.get(k)) != null) && ((v = e.getValue()) != null) && ((v == r) || (v.equals(r)));
/* 3771:     */     }
/* 3772:     */     
/* 3773:     */     public boolean remove(Object o)
/* 3774:     */     {
/* 3775:     */       Map.Entry<?, ?> e;
/* 3776:     */       Object k;
/* 3777:     */       Object v;
/* 3778:4511 */       return ((o instanceof Map.Entry)) && ((k = (e = (Map.Entry)o).getKey()) != null) && ((v = e.getValue()) != null) && (this.map.remove(k, v));
/* 3779:     */     }
/* 3780:     */     
/* 3781:     */     public Iterator<Map.Entry<K, V>> iterator()
/* 3782:     */     {
/* 3783:4521 */       ConcurrentHashMapV8<K, V> m = this.map;
/* 3784:     */       ConcurrentHashMapV8.Node<K, V>[] t;
/* 3785:4523 */       int f = (t = m.table) == null ? 0 : t.length;
/* 3786:4524 */       return new ConcurrentHashMapV8.EntryIterator(t, f, 0, f, m);
/* 3787:     */     }
/* 3788:     */     
/* 3789:     */     public boolean add(Map.Entry<K, V> e)
/* 3790:     */     {
/* 3791:4528 */       return this.map.putVal(e.getKey(), e.getValue(), false) == null;
/* 3792:     */     }
/* 3793:     */     
/* 3794:     */     public boolean addAll(Collection<? extends Map.Entry<K, V>> c)
/* 3795:     */     {
/* 3796:4532 */       boolean added = false;
/* 3797:4533 */       for (Map.Entry<K, V> e : c) {
/* 3798:4534 */         if (add(e)) {
/* 3799:4535 */           added = true;
/* 3800:     */         }
/* 3801:     */       }
/* 3802:4537 */       return added;
/* 3803:     */     }
/* 3804:     */     
/* 3805:     */     public final int hashCode()
/* 3806:     */     {
/* 3807:4541 */       int h = 0;
/* 3808:     */       ConcurrentHashMapV8.Node<K, V>[] t;
/* 3809:4543 */       if ((t = this.map.table) != null)
/* 3810:     */       {
/* 3811:4544 */         ConcurrentHashMapV8.Traverser<K, V> it = new ConcurrentHashMapV8.Traverser(t, t.length, 0, t.length);
/* 3812:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 3813:4545 */         while ((p = it.advance()) != null) {
/* 3814:4546 */           h += p.hashCode();
/* 3815:     */         }
/* 3816:     */       }
/* 3817:4549 */       return h;
/* 3818:     */     }
/* 3819:     */     
/* 3820:     */     public final boolean equals(Object o)
/* 3821:     */     {
/* 3822:     */       Set<?> c;
/* 3823:4554 */       return ((o instanceof Set)) && (((c = (Set)o) == this) || ((containsAll(c)) && (c.containsAll(this))));
/* 3824:     */     }
/* 3825:     */     
/* 3826:     */     public ConcurrentHashMapV8.ConcurrentHashMapSpliterator<Map.Entry<K, V>> spliterator166()
/* 3827:     */     {
/* 3828:4561 */       ConcurrentHashMapV8<K, V> m = this.map;
/* 3829:4562 */       long n = m.sumCount();
/* 3830:     */       ConcurrentHashMapV8.Node<K, V>[] t;
/* 3831:4563 */       int f = (t = m.table) == null ? 0 : t.length;
/* 3832:4564 */       return new ConcurrentHashMapV8.EntrySpliterator(t, f, 0, f, n < 0L ? 0L : n, m);
/* 3833:     */     }
/* 3834:     */     
/* 3835:     */     public void forEach(ConcurrentHashMapV8.Action<? super Map.Entry<K, V>> action)
/* 3836:     */     {
/* 3837:4568 */       if (action == null) {
/* 3838:4568 */         throw new NullPointerException();
/* 3839:     */       }
/* 3840:     */       ConcurrentHashMapV8.Node<K, V>[] t;
/* 3841:4570 */       if ((t = this.map.table) != null)
/* 3842:     */       {
/* 3843:4571 */         ConcurrentHashMapV8.Traverser<K, V> it = new ConcurrentHashMapV8.Traverser(t, t.length, 0, t.length);
/* 3844:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 3845:4572 */         while ((p = it.advance()) != null) {
/* 3846:4573 */           action.apply(new ConcurrentHashMapV8.MapEntry(p.key, p.val, this.map));
/* 3847:     */         }
/* 3848:     */       }
/* 3849:     */     }
/* 3850:     */   }
/* 3851:     */   
/* 3852:     */   static abstract class BulkTask<K, V, R>
/* 3853:     */     extends CountedCompleter<R>
/* 3854:     */   {
/* 3855:     */     ConcurrentHashMapV8.Node<K, V>[] tab;
/* 3856:     */     ConcurrentHashMapV8.Node<K, V> next;
/* 3857:     */     int index;
/* 3858:     */     int baseIndex;
/* 3859:     */     int baseLimit;
/* 3860:     */     final int baseSize;
/* 3861:     */     int batch;
/* 3862:     */     
/* 3863:     */     BulkTask(BulkTask<K, V, ?> par, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t)
/* 3864:     */     {
/* 3865:4595 */       super();
/* 3866:4596 */       this.batch = b;
/* 3867:4597 */       this.index = (this.baseIndex = i);
/* 3868:4598 */       if ((this.tab = t) == null)
/* 3869:     */       {
/* 3870:4599 */         this.baseSize = (this.baseLimit = 0);
/* 3871:     */       }
/* 3872:4600 */       else if (par == null)
/* 3873:     */       {
/* 3874:4601 */         this.baseSize = (this.baseLimit = t.length);
/* 3875:     */       }
/* 3876:     */       else
/* 3877:     */       {
/* 3878:4603 */         this.baseLimit = f;
/* 3879:4604 */         this.baseSize = par.baseSize;
/* 3880:     */       }
/* 3881:     */     }
/* 3882:     */     
/* 3883:     */     final ConcurrentHashMapV8.Node<K, V> advance()
/* 3884:     */     {
/* 3885:     */       ConcurrentHashMapV8.Node<K, V> e;
/* 3886:4613 */       if ((e = this.next) != null) {
/* 3887:4614 */         e = e.next;
/* 3888:     */       }
/* 3889:     */       for (;;)
/* 3890:     */       {
/* 3891:4617 */         if (e != null) {
/* 3892:4618 */           return this.next = e;
/* 3893:     */         }
/* 3894:     */         ConcurrentHashMapV8.Node<K, V>[] t;
/* 3895:     */         int n;
/* 3896:     */         int i;
/* 3897:4619 */         if ((this.baseIndex >= this.baseLimit) || ((t = this.tab) == null) || ((n = t.length) <= (i = this.index)) || (i < 0)) {
/* 3898:4621 */           return this.next = null;
/* 3899:     */         }
/* 3900:     */         int n;
/* 3901:     */         int i;
/* 3902:     */         ConcurrentHashMapV8.Node<K, V>[] t;
/* 3903:4622 */         if (((e = ConcurrentHashMapV8.tabAt(t, this.index)) != null) && (e.hash < 0))
/* 3904:     */         {
/* 3905:4623 */           if ((e instanceof ConcurrentHashMapV8.ForwardingNode))
/* 3906:     */           {
/* 3907:4624 */             this.tab = ((ConcurrentHashMapV8.ForwardingNode)e).nextTable;
/* 3908:4625 */             e = null;
/* 3909:4626 */             continue;
/* 3910:     */           }
/* 3911:4628 */           if ((e instanceof ConcurrentHashMapV8.TreeBin)) {
/* 3912:4629 */             e = ((ConcurrentHashMapV8.TreeBin)e).first;
/* 3913:     */           } else {
/* 3914:4631 */             e = null;
/* 3915:     */           }
/* 3916:     */         }
/* 3917:4633 */         if (this.index += this.baseSize >= n) {
/* 3918:4634 */           this.index = (++this.baseIndex);
/* 3919:     */         }
/* 3920:     */       }
/* 3921:     */     }
/* 3922:     */   }
/* 3923:     */   
/* 3924:     */   static final class ForEachKeyTask<K, V>
/* 3925:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Void>
/* 3926:     */   {
/* 3927:     */     final ConcurrentHashMapV8.Action<? super K> action;
/* 3928:     */     
/* 3929:     */     ForEachKeyTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ConcurrentHashMapV8.Action<? super K> action)
/* 3930:     */     {
/* 3931:4653 */       super(b, i, f, t);
/* 3932:4654 */       this.action = action;
/* 3933:     */     }
/* 3934:     */     
/* 3935:     */     public final void compute()
/* 3936:     */     {
/* 3937:     */       ConcurrentHashMapV8.Action<? super K> action;
/* 3938:4658 */       if ((action = this.action) != null)
/* 3939:     */       {
/* 3940:     */         int f;
/* 3941:     */         int h;
/* 3942:4659 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 3943:     */         {
/* 3944:4661 */           addToPendingCount(1);
/* 3945:4662 */           new ForEachKeyTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action).fork();
/* 3946:     */         }
/* 3947:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 3948:4666 */         while ((p = advance()) != null) {
/* 3949:4667 */           action.apply(p.key);
/* 3950:     */         }
/* 3951:4668 */         propagateCompletion();
/* 3952:     */       }
/* 3953:     */     }
/* 3954:     */   }
/* 3955:     */   
/* 3956:     */   static final class ForEachValueTask<K, V>
/* 3957:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Void>
/* 3958:     */   {
/* 3959:     */     final ConcurrentHashMapV8.Action<? super V> action;
/* 3960:     */     
/* 3961:     */     ForEachValueTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ConcurrentHashMapV8.Action<? super V> action)
/* 3962:     */     {
/* 3963:4680 */       super(b, i, f, t);
/* 3964:4681 */       this.action = action;
/* 3965:     */     }
/* 3966:     */     
/* 3967:     */     public final void compute()
/* 3968:     */     {
/* 3969:     */       ConcurrentHashMapV8.Action<? super V> action;
/* 3970:4685 */       if ((action = this.action) != null)
/* 3971:     */       {
/* 3972:     */         int f;
/* 3973:     */         int h;
/* 3974:4686 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 3975:     */         {
/* 3976:4688 */           addToPendingCount(1);
/* 3977:4689 */           new ForEachValueTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action).fork();
/* 3978:     */         }
/* 3979:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 3980:4693 */         while ((p = advance()) != null) {
/* 3981:4694 */           action.apply(p.val);
/* 3982:     */         }
/* 3983:4695 */         propagateCompletion();
/* 3984:     */       }
/* 3985:     */     }
/* 3986:     */   }
/* 3987:     */   
/* 3988:     */   static final class ForEachEntryTask<K, V>
/* 3989:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Void>
/* 3990:     */   {
/* 3991:     */     final ConcurrentHashMapV8.Action<? super Map.Entry<K, V>> action;
/* 3992:     */     
/* 3993:     */     ForEachEntryTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ConcurrentHashMapV8.Action<? super Map.Entry<K, V>> action)
/* 3994:     */     {
/* 3995:4707 */       super(b, i, f, t);
/* 3996:4708 */       this.action = action;
/* 3997:     */     }
/* 3998:     */     
/* 3999:     */     public final void compute()
/* 4000:     */     {
/* 4001:     */       ConcurrentHashMapV8.Action<? super Map.Entry<K, V>> action;
/* 4002:4712 */       if ((action = this.action) != null)
/* 4003:     */       {
/* 4004:     */         int f;
/* 4005:     */         int h;
/* 4006:4713 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4007:     */         {
/* 4008:4715 */           addToPendingCount(1);
/* 4009:4716 */           new ForEachEntryTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action).fork();
/* 4010:     */         }
/* 4011:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4012:4720 */         while ((p = advance()) != null) {
/* 4013:4721 */           action.apply(p);
/* 4014:     */         }
/* 4015:4722 */         propagateCompletion();
/* 4016:     */       }
/* 4017:     */     }
/* 4018:     */   }
/* 4019:     */   
/* 4020:     */   static final class ForEachMappingTask<K, V>
/* 4021:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Void>
/* 4022:     */   {
/* 4023:     */     final ConcurrentHashMapV8.BiAction<? super K, ? super V> action;
/* 4024:     */     
/* 4025:     */     ForEachMappingTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ConcurrentHashMapV8.BiAction<? super K, ? super V> action)
/* 4026:     */     {
/* 4027:4734 */       super(b, i, f, t);
/* 4028:4735 */       this.action = action;
/* 4029:     */     }
/* 4030:     */     
/* 4031:     */     public final void compute()
/* 4032:     */     {
/* 4033:     */       ConcurrentHashMapV8.BiAction<? super K, ? super V> action;
/* 4034:4739 */       if ((action = this.action) != null)
/* 4035:     */       {
/* 4036:     */         int f;
/* 4037:     */         int h;
/* 4038:4740 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4039:     */         {
/* 4040:4742 */           addToPendingCount(1);
/* 4041:4743 */           new ForEachMappingTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action).fork();
/* 4042:     */         }
/* 4043:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4044:4747 */         while ((p = advance()) != null) {
/* 4045:4748 */           action.apply(p.key, p.val);
/* 4046:     */         }
/* 4047:4749 */         propagateCompletion();
/* 4048:     */       }
/* 4049:     */     }
/* 4050:     */   }
/* 4051:     */   
/* 4052:     */   static final class ForEachTransformedKeyTask<K, V, U>
/* 4053:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Void>
/* 4054:     */   {
/* 4055:     */     final ConcurrentHashMapV8.Fun<? super K, ? extends U> transformer;
/* 4056:     */     final ConcurrentHashMapV8.Action<? super U> action;
/* 4057:     */     
/* 4058:     */     ForEachTransformedKeyTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ConcurrentHashMapV8.Fun<? super K, ? extends U> transformer, ConcurrentHashMapV8.Action<? super U> action)
/* 4059:     */     {
/* 4060:4762 */       super(b, i, f, t);
/* 4061:4763 */       this.transformer = transformer;this.action = action;
/* 4062:     */     }
/* 4063:     */     
/* 4064:     */     public final void compute()
/* 4065:     */     {
/* 4066:     */       ConcurrentHashMapV8.Fun<? super K, ? extends U> transformer;
/* 4067:     */       ConcurrentHashMapV8.Action<? super U> action;
/* 4068:4768 */       if (((transformer = this.transformer) != null) && ((action = this.action) != null))
/* 4069:     */       {
/* 4070:     */         int f;
/* 4071:     */         int h;
/* 4072:4770 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4073:     */         {
/* 4074:4772 */           addToPendingCount(1);
/* 4075:4773 */           new ForEachTransformedKeyTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action).fork();
/* 4076:     */         }
/* 4077:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4078:4777 */         while ((p = advance()) != null)
/* 4079:     */         {
/* 4080:     */           U u;
/* 4081:4779 */           if ((u = transformer.apply(p.key)) != null) {
/* 4082:4780 */             action.apply(u);
/* 4083:     */           }
/* 4084:     */         }
/* 4085:4782 */         propagateCompletion();
/* 4086:     */       }
/* 4087:     */     }
/* 4088:     */   }
/* 4089:     */   
/* 4090:     */   static final class ForEachTransformedValueTask<K, V, U>
/* 4091:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Void>
/* 4092:     */   {
/* 4093:     */     final ConcurrentHashMapV8.Fun<? super V, ? extends U> transformer;
/* 4094:     */     final ConcurrentHashMapV8.Action<? super U> action;
/* 4095:     */     
/* 4096:     */     ForEachTransformedValueTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ConcurrentHashMapV8.Fun<? super V, ? extends U> transformer, ConcurrentHashMapV8.Action<? super U> action)
/* 4097:     */     {
/* 4098:4795 */       super(b, i, f, t);
/* 4099:4796 */       this.transformer = transformer;this.action = action;
/* 4100:     */     }
/* 4101:     */     
/* 4102:     */     public final void compute()
/* 4103:     */     {
/* 4104:     */       ConcurrentHashMapV8.Fun<? super V, ? extends U> transformer;
/* 4105:     */       ConcurrentHashMapV8.Action<? super U> action;
/* 4106:4801 */       if (((transformer = this.transformer) != null) && ((action = this.action) != null))
/* 4107:     */       {
/* 4108:     */         int f;
/* 4109:     */         int h;
/* 4110:4803 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4111:     */         {
/* 4112:4805 */           addToPendingCount(1);
/* 4113:4806 */           new ForEachTransformedValueTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action).fork();
/* 4114:     */         }
/* 4115:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4116:4810 */         while ((p = advance()) != null)
/* 4117:     */         {
/* 4118:     */           U u;
/* 4119:4812 */           if ((u = transformer.apply(p.val)) != null) {
/* 4120:4813 */             action.apply(u);
/* 4121:     */           }
/* 4122:     */         }
/* 4123:4815 */         propagateCompletion();
/* 4124:     */       }
/* 4125:     */     }
/* 4126:     */   }
/* 4127:     */   
/* 4128:     */   static final class ForEachTransformedEntryTask<K, V, U>
/* 4129:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Void>
/* 4130:     */   {
/* 4131:     */     final ConcurrentHashMapV8.Fun<Map.Entry<K, V>, ? extends U> transformer;
/* 4132:     */     final ConcurrentHashMapV8.Action<? super U> action;
/* 4133:     */     
/* 4134:     */     ForEachTransformedEntryTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ConcurrentHashMapV8.Fun<Map.Entry<K, V>, ? extends U> transformer, ConcurrentHashMapV8.Action<? super U> action)
/* 4135:     */     {
/* 4136:4828 */       super(b, i, f, t);
/* 4137:4829 */       this.transformer = transformer;this.action = action;
/* 4138:     */     }
/* 4139:     */     
/* 4140:     */     public final void compute()
/* 4141:     */     {
/* 4142:     */       ConcurrentHashMapV8.Fun<Map.Entry<K, V>, ? extends U> transformer;
/* 4143:     */       ConcurrentHashMapV8.Action<? super U> action;
/* 4144:4834 */       if (((transformer = this.transformer) != null) && ((action = this.action) != null))
/* 4145:     */       {
/* 4146:     */         int f;
/* 4147:     */         int h;
/* 4148:4836 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4149:     */         {
/* 4150:4838 */           addToPendingCount(1);
/* 4151:4839 */           new ForEachTransformedEntryTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action).fork();
/* 4152:     */         }
/* 4153:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4154:4843 */         while ((p = advance()) != null)
/* 4155:     */         {
/* 4156:     */           U u;
/* 4157:4845 */           if ((u = transformer.apply(p)) != null) {
/* 4158:4846 */             action.apply(u);
/* 4159:     */           }
/* 4160:     */         }
/* 4161:4848 */         propagateCompletion();
/* 4162:     */       }
/* 4163:     */     }
/* 4164:     */   }
/* 4165:     */   
/* 4166:     */   static final class ForEachTransformedMappingTask<K, V, U>
/* 4167:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Void>
/* 4168:     */   {
/* 4169:     */     final ConcurrentHashMapV8.BiFun<? super K, ? super V, ? extends U> transformer;
/* 4170:     */     final ConcurrentHashMapV8.Action<? super U> action;
/* 4171:     */     
/* 4172:     */     ForEachTransformedMappingTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ConcurrentHashMapV8.BiFun<? super K, ? super V, ? extends U> transformer, ConcurrentHashMapV8.Action<? super U> action)
/* 4173:     */     {
/* 4174:4862 */       super(b, i, f, t);
/* 4175:4863 */       this.transformer = transformer;this.action = action;
/* 4176:     */     }
/* 4177:     */     
/* 4178:     */     public final void compute()
/* 4179:     */     {
/* 4180:     */       ConcurrentHashMapV8.BiFun<? super K, ? super V, ? extends U> transformer;
/* 4181:     */       ConcurrentHashMapV8.Action<? super U> action;
/* 4182:4868 */       if (((transformer = this.transformer) != null) && ((action = this.action) != null))
/* 4183:     */       {
/* 4184:     */         int f;
/* 4185:     */         int h;
/* 4186:4870 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4187:     */         {
/* 4188:4872 */           addToPendingCount(1);
/* 4189:4873 */           new ForEachTransformedMappingTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action).fork();
/* 4190:     */         }
/* 4191:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4192:4877 */         while ((p = advance()) != null)
/* 4193:     */         {
/* 4194:     */           U u;
/* 4195:4879 */           if ((u = transformer.apply(p.key, p.val)) != null) {
/* 4196:4880 */             action.apply(u);
/* 4197:     */           }
/* 4198:     */         }
/* 4199:4882 */         propagateCompletion();
/* 4200:     */       }
/* 4201:     */     }
/* 4202:     */   }
/* 4203:     */   
/* 4204:     */   static final class SearchKeysTask<K, V, U>
/* 4205:     */     extends ConcurrentHashMapV8.BulkTask<K, V, U>
/* 4206:     */   {
/* 4207:     */     final ConcurrentHashMapV8.Fun<? super K, ? extends U> searchFunction;
/* 4208:     */     final AtomicReference<U> result;
/* 4209:     */     
/* 4210:     */     SearchKeysTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ConcurrentHashMapV8.Fun<? super K, ? extends U> searchFunction, AtomicReference<U> result)
/* 4211:     */     {
/* 4212:4896 */       super(b, i, f, t);
/* 4213:4897 */       this.searchFunction = searchFunction;this.result = result;
/* 4214:     */     }
/* 4215:     */     
/* 4216:     */     public final U getRawResult()
/* 4217:     */     {
/* 4218:4899 */       return this.result.get();
/* 4219:     */     }
/* 4220:     */     
/* 4221:     */     public final void compute()
/* 4222:     */     {
/* 4223:     */       ConcurrentHashMapV8.Fun<? super K, ? extends U> searchFunction;
/* 4224:     */       AtomicReference<U> result;
/* 4225:4903 */       if (((searchFunction = this.searchFunction) != null) && ((result = this.result) != null))
/* 4226:     */       {
/* 4227:     */         int f;
/* 4228:     */         int h;
/* 4229:4905 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4230:     */         {
/* 4231:4907 */           if (result.get() != null) {
/* 4232:4908 */             return;
/* 4233:     */           }
/* 4234:4909 */           addToPendingCount(1);
/* 4235:4910 */           new SearchKeysTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result).fork();
/* 4236:     */         }
/* 4237:4914 */         while (result.get() == null)
/* 4238:     */         {
/* 4239:     */           ConcurrentHashMapV8.Node<K, V> p;
/* 4240:4917 */           if ((p = advance()) == null)
/* 4241:     */           {
/* 4242:4918 */             propagateCompletion();
/* 4243:4919 */             break;
/* 4244:     */           }
/* 4245:     */           U u;
/* 4246:4921 */           if ((u = searchFunction.apply(p.key)) != null)
/* 4247:     */           {
/* 4248:4922 */             if (!result.compareAndSet(null, u)) {
/* 4249:     */               break;
/* 4250:     */             }
/* 4251:4923 */             quietlyCompleteRoot(); break;
/* 4252:     */           }
/* 4253:     */         }
/* 4254:     */       }
/* 4255:     */     }
/* 4256:     */   }
/* 4257:     */   
/* 4258:     */   static final class SearchValuesTask<K, V, U>
/* 4259:     */     extends ConcurrentHashMapV8.BulkTask<K, V, U>
/* 4260:     */   {
/* 4261:     */     final ConcurrentHashMapV8.Fun<? super V, ? extends U> searchFunction;
/* 4262:     */     final AtomicReference<U> result;
/* 4263:     */     
/* 4264:     */     SearchValuesTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ConcurrentHashMapV8.Fun<? super V, ? extends U> searchFunction, AtomicReference<U> result)
/* 4265:     */     {
/* 4266:4940 */       super(b, i, f, t);
/* 4267:4941 */       this.searchFunction = searchFunction;this.result = result;
/* 4268:     */     }
/* 4269:     */     
/* 4270:     */     public final U getRawResult()
/* 4271:     */     {
/* 4272:4943 */       return this.result.get();
/* 4273:     */     }
/* 4274:     */     
/* 4275:     */     public final void compute()
/* 4276:     */     {
/* 4277:     */       ConcurrentHashMapV8.Fun<? super V, ? extends U> searchFunction;
/* 4278:     */       AtomicReference<U> result;
/* 4279:4947 */       if (((searchFunction = this.searchFunction) != null) && ((result = this.result) != null))
/* 4280:     */       {
/* 4281:     */         int f;
/* 4282:     */         int h;
/* 4283:4949 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4284:     */         {
/* 4285:4951 */           if (result.get() != null) {
/* 4286:4952 */             return;
/* 4287:     */           }
/* 4288:4953 */           addToPendingCount(1);
/* 4289:4954 */           new SearchValuesTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result).fork();
/* 4290:     */         }
/* 4291:4958 */         while (result.get() == null)
/* 4292:     */         {
/* 4293:     */           ConcurrentHashMapV8.Node<K, V> p;
/* 4294:4961 */           if ((p = advance()) == null)
/* 4295:     */           {
/* 4296:4962 */             propagateCompletion();
/* 4297:4963 */             break;
/* 4298:     */           }
/* 4299:     */           U u;
/* 4300:4965 */           if ((u = searchFunction.apply(p.val)) != null)
/* 4301:     */           {
/* 4302:4966 */             if (!result.compareAndSet(null, u)) {
/* 4303:     */               break;
/* 4304:     */             }
/* 4305:4967 */             quietlyCompleteRoot(); break;
/* 4306:     */           }
/* 4307:     */         }
/* 4308:     */       }
/* 4309:     */     }
/* 4310:     */   }
/* 4311:     */   
/* 4312:     */   static final class SearchEntriesTask<K, V, U>
/* 4313:     */     extends ConcurrentHashMapV8.BulkTask<K, V, U>
/* 4314:     */   {
/* 4315:     */     final ConcurrentHashMapV8.Fun<Map.Entry<K, V>, ? extends U> searchFunction;
/* 4316:     */     final AtomicReference<U> result;
/* 4317:     */     
/* 4318:     */     SearchEntriesTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ConcurrentHashMapV8.Fun<Map.Entry<K, V>, ? extends U> searchFunction, AtomicReference<U> result)
/* 4319:     */     {
/* 4320:4984 */       super(b, i, f, t);
/* 4321:4985 */       this.searchFunction = searchFunction;this.result = result;
/* 4322:     */     }
/* 4323:     */     
/* 4324:     */     public final U getRawResult()
/* 4325:     */     {
/* 4326:4987 */       return this.result.get();
/* 4327:     */     }
/* 4328:     */     
/* 4329:     */     public final void compute()
/* 4330:     */     {
/* 4331:     */       ConcurrentHashMapV8.Fun<Map.Entry<K, V>, ? extends U> searchFunction;
/* 4332:     */       AtomicReference<U> result;
/* 4333:4991 */       if (((searchFunction = this.searchFunction) != null) && ((result = this.result) != null))
/* 4334:     */       {
/* 4335:     */         int f;
/* 4336:     */         int h;
/* 4337:4993 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4338:     */         {
/* 4339:4995 */           if (result.get() != null) {
/* 4340:4996 */             return;
/* 4341:     */           }
/* 4342:4997 */           addToPendingCount(1);
/* 4343:4998 */           new SearchEntriesTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result).fork();
/* 4344:     */         }
/* 4345:5002 */         while (result.get() == null)
/* 4346:     */         {
/* 4347:     */           ConcurrentHashMapV8.Node<K, V> p;
/* 4348:5005 */           if ((p = advance()) == null)
/* 4349:     */           {
/* 4350:5006 */             propagateCompletion();
/* 4351:5007 */             break;
/* 4352:     */           }
/* 4353:     */           U u;
/* 4354:5009 */           if ((u = searchFunction.apply(p)) != null)
/* 4355:     */           {
/* 4356:5010 */             if (result.compareAndSet(null, u)) {
/* 4357:5011 */               quietlyCompleteRoot();
/* 4358:     */             }
/* 4359:5012 */             return;
/* 4360:     */           }
/* 4361:     */         }
/* 4362:     */       }
/* 4363:     */     }
/* 4364:     */   }
/* 4365:     */   
/* 4366:     */   static final class SearchMappingsTask<K, V, U>
/* 4367:     */     extends ConcurrentHashMapV8.BulkTask<K, V, U>
/* 4368:     */   {
/* 4369:     */     final ConcurrentHashMapV8.BiFun<? super K, ? super V, ? extends U> searchFunction;
/* 4370:     */     final AtomicReference<U> result;
/* 4371:     */     
/* 4372:     */     SearchMappingsTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ConcurrentHashMapV8.BiFun<? super K, ? super V, ? extends U> searchFunction, AtomicReference<U> result)
/* 4373:     */     {
/* 4374:5028 */       super(b, i, f, t);
/* 4375:5029 */       this.searchFunction = searchFunction;this.result = result;
/* 4376:     */     }
/* 4377:     */     
/* 4378:     */     public final U getRawResult()
/* 4379:     */     {
/* 4380:5031 */       return this.result.get();
/* 4381:     */     }
/* 4382:     */     
/* 4383:     */     public final void compute()
/* 4384:     */     {
/* 4385:     */       ConcurrentHashMapV8.BiFun<? super K, ? super V, ? extends U> searchFunction;
/* 4386:     */       AtomicReference<U> result;
/* 4387:5035 */       if (((searchFunction = this.searchFunction) != null) && ((result = this.result) != null))
/* 4388:     */       {
/* 4389:     */         int f;
/* 4390:     */         int h;
/* 4391:5037 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4392:     */         {
/* 4393:5039 */           if (result.get() != null) {
/* 4394:5040 */             return;
/* 4395:     */           }
/* 4396:5041 */           addToPendingCount(1);
/* 4397:5042 */           new SearchMappingsTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result).fork();
/* 4398:     */         }
/* 4399:5046 */         while (result.get() == null)
/* 4400:     */         {
/* 4401:     */           ConcurrentHashMapV8.Node<K, V> p;
/* 4402:5049 */           if ((p = advance()) == null)
/* 4403:     */           {
/* 4404:5050 */             propagateCompletion();
/* 4405:5051 */             break;
/* 4406:     */           }
/* 4407:     */           U u;
/* 4408:5053 */           if ((u = searchFunction.apply(p.key, p.val)) != null)
/* 4409:     */           {
/* 4410:5054 */             if (!result.compareAndSet(null, u)) {
/* 4411:     */               break;
/* 4412:     */             }
/* 4413:5055 */             quietlyCompleteRoot(); break;
/* 4414:     */           }
/* 4415:     */         }
/* 4416:     */       }
/* 4417:     */     }
/* 4418:     */   }
/* 4419:     */   
/* 4420:     */   static final class ReduceKeysTask<K, V>
/* 4421:     */     extends ConcurrentHashMapV8.BulkTask<K, V, K>
/* 4422:     */   {
/* 4423:     */     final ConcurrentHashMapV8.BiFun<? super K, ? super K, ? extends K> reducer;
/* 4424:     */     K result;
/* 4425:     */     ReduceKeysTask<K, V> rights;
/* 4426:     */     ReduceKeysTask<K, V> nextRight;
/* 4427:     */     
/* 4428:     */     ReduceKeysTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ReduceKeysTask<K, V> nextRight, ConcurrentHashMapV8.BiFun<? super K, ? super K, ? extends K> reducer)
/* 4429:     */     {
/* 4430:5073 */       super(b, i, f, t);this.nextRight = nextRight;
/* 4431:5074 */       this.reducer = reducer;
/* 4432:     */     }
/* 4433:     */     
/* 4434:     */     public final K getRawResult()
/* 4435:     */     {
/* 4436:5076 */       return this.result;
/* 4437:     */     }
/* 4438:     */     
/* 4439:     */     public final void compute()
/* 4440:     */     {
/* 4441:     */       ConcurrentHashMapV8.BiFun<? super K, ? super K, ? extends K> reducer;
/* 4442:5079 */       if ((reducer = this.reducer) != null)
/* 4443:     */       {
/* 4444:     */         int f;
/* 4445:     */         int h;
/* 4446:5080 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4447:     */         {
/* 4448:5082 */           addToPendingCount(1);
/* 4449:5083 */           (this.rights = new ReduceKeysTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, reducer)).fork();
/* 4450:     */         }
/* 4451:5087 */         K r = null;
/* 4452:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4453:5088 */         while ((p = advance()) != null)
/* 4454:     */         {
/* 4455:5089 */           K u = p.key;
/* 4456:5090 */           r = u == null ? r : r == null ? u : reducer.apply(r, u);
/* 4457:     */         }
/* 4458:5092 */         this.result = r;
/* 4459:5094 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 4460:     */         {
/* 4461:5096 */           ReduceKeysTask<K, V> t = (ReduceKeysTask)c;
/* 4462:5097 */           ReduceKeysTask<K, V> s = t.rights;
/* 4463:5098 */           while (s != null)
/* 4464:     */           {
/* 4465:     */             K sr;
/* 4466:5100 */             if ((sr = s.result) != null)
/* 4467:     */             {
/* 4468:     */               K tr;
/* 4469:5101 */               t.result = ((tr = t.result) == null ? sr : reducer.apply(tr, sr));
/* 4470:     */             }
/* 4471:5103 */             s = t.rights = s.nextRight;
/* 4472:     */           }
/* 4473:     */         }
/* 4474:     */       }
/* 4475:     */     }
/* 4476:     */   }
/* 4477:     */   
/* 4478:     */   static final class ReduceValuesTask<K, V>
/* 4479:     */     extends ConcurrentHashMapV8.BulkTask<K, V, V>
/* 4480:     */   {
/* 4481:     */     final ConcurrentHashMapV8.BiFun<? super V, ? super V, ? extends V> reducer;
/* 4482:     */     V result;
/* 4483:     */     ReduceValuesTask<K, V> rights;
/* 4484:     */     ReduceValuesTask<K, V> nextRight;
/* 4485:     */     
/* 4486:     */     ReduceValuesTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ReduceValuesTask<K, V> nextRight, ConcurrentHashMapV8.BiFun<? super V, ? super V, ? extends V> reducer)
/* 4487:     */     {
/* 4488:5120 */       super(b, i, f, t);this.nextRight = nextRight;
/* 4489:5121 */       this.reducer = reducer;
/* 4490:     */     }
/* 4491:     */     
/* 4492:     */     public final V getRawResult()
/* 4493:     */     {
/* 4494:5123 */       return this.result;
/* 4495:     */     }
/* 4496:     */     
/* 4497:     */     public final void compute()
/* 4498:     */     {
/* 4499:     */       ConcurrentHashMapV8.BiFun<? super V, ? super V, ? extends V> reducer;
/* 4500:5126 */       if ((reducer = this.reducer) != null)
/* 4501:     */       {
/* 4502:     */         int f;
/* 4503:     */         int h;
/* 4504:5127 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4505:     */         {
/* 4506:5129 */           addToPendingCount(1);
/* 4507:5130 */           (this.rights = new ReduceValuesTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, reducer)).fork();
/* 4508:     */         }
/* 4509:5134 */         V r = null;
/* 4510:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4511:5135 */         while ((p = advance()) != null)
/* 4512:     */         {
/* 4513:5136 */           V v = p.val;
/* 4514:5137 */           r = r == null ? v : reducer.apply(r, v);
/* 4515:     */         }
/* 4516:5139 */         this.result = r;
/* 4517:5141 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 4518:     */         {
/* 4519:5143 */           ReduceValuesTask<K, V> t = (ReduceValuesTask)c;
/* 4520:5144 */           ReduceValuesTask<K, V> s = t.rights;
/* 4521:5145 */           while (s != null)
/* 4522:     */           {
/* 4523:     */             V sr;
/* 4524:5147 */             if ((sr = s.result) != null)
/* 4525:     */             {
/* 4526:     */               V tr;
/* 4527:5148 */               t.result = ((tr = t.result) == null ? sr : reducer.apply(tr, sr));
/* 4528:     */             }
/* 4529:5150 */             s = t.rights = s.nextRight;
/* 4530:     */           }
/* 4531:     */         }
/* 4532:     */       }
/* 4533:     */     }
/* 4534:     */   }
/* 4535:     */   
/* 4536:     */   static final class ReduceEntriesTask<K, V>
/* 4537:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Map.Entry<K, V>>
/* 4538:     */   {
/* 4539:     */     final ConcurrentHashMapV8.BiFun<Map.Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> reducer;
/* 4540:     */     Map.Entry<K, V> result;
/* 4541:     */     ReduceEntriesTask<K, V> rights;
/* 4542:     */     ReduceEntriesTask<K, V> nextRight;
/* 4543:     */     
/* 4544:     */     ReduceEntriesTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, ReduceEntriesTask<K, V> nextRight, ConcurrentHashMapV8.BiFun<Map.Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> reducer)
/* 4545:     */     {
/* 4546:5167 */       super(b, i, f, t);this.nextRight = nextRight;
/* 4547:5168 */       this.reducer = reducer;
/* 4548:     */     }
/* 4549:     */     
/* 4550:     */     public final Map.Entry<K, V> getRawResult()
/* 4551:     */     {
/* 4552:5170 */       return this.result;
/* 4553:     */     }
/* 4554:     */     
/* 4555:     */     public final void compute()
/* 4556:     */     {
/* 4557:     */       ConcurrentHashMapV8.BiFun<Map.Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> reducer;
/* 4558:5173 */       if ((reducer = this.reducer) != null)
/* 4559:     */       {
/* 4560:     */         int f;
/* 4561:     */         int h;
/* 4562:5174 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4563:     */         {
/* 4564:5176 */           addToPendingCount(1);
/* 4565:5177 */           (this.rights = new ReduceEntriesTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, reducer)).fork();
/* 4566:     */         }
/* 4567:5181 */         Map.Entry<K, V> r = null;
/* 4568:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4569:5182 */         while ((p = advance()) != null) {
/* 4570:5183 */           r = r == null ? p : (Map.Entry)reducer.apply(r, p);
/* 4571:     */         }
/* 4572:5184 */         this.result = r;
/* 4573:5186 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 4574:     */         {
/* 4575:5188 */           ReduceEntriesTask<K, V> t = (ReduceEntriesTask)c;
/* 4576:5189 */           ReduceEntriesTask<K, V> s = t.rights;
/* 4577:5190 */           while (s != null)
/* 4578:     */           {
/* 4579:     */             Map.Entry<K, V> sr;
/* 4580:5192 */             if ((sr = s.result) != null)
/* 4581:     */             {
/* 4582:     */               Map.Entry<K, V> tr;
/* 4583:5193 */               t.result = ((tr = t.result) == null ? sr : (Map.Entry)reducer.apply(tr, sr));
/* 4584:     */             }
/* 4585:5195 */             s = t.rights = s.nextRight;
/* 4586:     */           }
/* 4587:     */         }
/* 4588:     */       }
/* 4589:     */     }
/* 4590:     */   }
/* 4591:     */   
/* 4592:     */   static final class MapReduceKeysTask<K, V, U>
/* 4593:     */     extends ConcurrentHashMapV8.BulkTask<K, V, U>
/* 4594:     */   {
/* 4595:     */     final ConcurrentHashMapV8.Fun<? super K, ? extends U> transformer;
/* 4596:     */     final ConcurrentHashMapV8.BiFun<? super U, ? super U, ? extends U> reducer;
/* 4597:     */     U result;
/* 4598:     */     MapReduceKeysTask<K, V, U> rights;
/* 4599:     */     MapReduceKeysTask<K, V, U> nextRight;
/* 4600:     */     
/* 4601:     */     MapReduceKeysTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceKeysTask<K, V, U> nextRight, ConcurrentHashMapV8.Fun<? super K, ? extends U> transformer, ConcurrentHashMapV8.BiFun<? super U, ? super U, ? extends U> reducer)
/* 4602:     */     {
/* 4603:5214 */       super(b, i, f, t);this.nextRight = nextRight;
/* 4604:5215 */       this.transformer = transformer;
/* 4605:5216 */       this.reducer = reducer;
/* 4606:     */     }
/* 4607:     */     
/* 4608:     */     public final U getRawResult()
/* 4609:     */     {
/* 4610:5218 */       return this.result;
/* 4611:     */     }
/* 4612:     */     
/* 4613:     */     public final void compute()
/* 4614:     */     {
/* 4615:     */       ConcurrentHashMapV8.Fun<? super K, ? extends U> transformer;
/* 4616:     */       ConcurrentHashMapV8.BiFun<? super U, ? super U, ? extends U> reducer;
/* 4617:5222 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 4618:     */       {
/* 4619:     */         int f;
/* 4620:     */         int h;
/* 4621:5224 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4622:     */         {
/* 4623:5226 */           addToPendingCount(1);
/* 4624:5227 */           (this.rights = new MapReduceKeysTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer)).fork();
/* 4625:     */         }
/* 4626:5231 */         U r = null;
/* 4627:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4628:5232 */         while ((p = advance()) != null)
/* 4629:     */         {
/* 4630:     */           U u;
/* 4631:5234 */           if ((u = transformer.apply(p.key)) != null) {
/* 4632:5235 */             r = r == null ? u : reducer.apply(r, u);
/* 4633:     */           }
/* 4634:     */         }
/* 4635:5237 */         this.result = r;
/* 4636:5239 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 4637:     */         {
/* 4638:5241 */           MapReduceKeysTask<K, V, U> t = (MapReduceKeysTask)c;
/* 4639:5242 */           MapReduceKeysTask<K, V, U> s = t.rights;
/* 4640:5243 */           while (s != null)
/* 4641:     */           {
/* 4642:     */             U sr;
/* 4643:5245 */             if ((sr = s.result) != null)
/* 4644:     */             {
/* 4645:     */               U tr;
/* 4646:5246 */               t.result = ((tr = t.result) == null ? sr : reducer.apply(tr, sr));
/* 4647:     */             }
/* 4648:5248 */             s = t.rights = s.nextRight;
/* 4649:     */           }
/* 4650:     */         }
/* 4651:     */       }
/* 4652:     */     }
/* 4653:     */   }
/* 4654:     */   
/* 4655:     */   static final class MapReduceValuesTask<K, V, U>
/* 4656:     */     extends ConcurrentHashMapV8.BulkTask<K, V, U>
/* 4657:     */   {
/* 4658:     */     final ConcurrentHashMapV8.Fun<? super V, ? extends U> transformer;
/* 4659:     */     final ConcurrentHashMapV8.BiFun<? super U, ? super U, ? extends U> reducer;
/* 4660:     */     U result;
/* 4661:     */     MapReduceValuesTask<K, V, U> rights;
/* 4662:     */     MapReduceValuesTask<K, V, U> nextRight;
/* 4663:     */     
/* 4664:     */     MapReduceValuesTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceValuesTask<K, V, U> nextRight, ConcurrentHashMapV8.Fun<? super V, ? extends U> transformer, ConcurrentHashMapV8.BiFun<? super U, ? super U, ? extends U> reducer)
/* 4665:     */     {
/* 4666:5267 */       super(b, i, f, t);this.nextRight = nextRight;
/* 4667:5268 */       this.transformer = transformer;
/* 4668:5269 */       this.reducer = reducer;
/* 4669:     */     }
/* 4670:     */     
/* 4671:     */     public final U getRawResult()
/* 4672:     */     {
/* 4673:5271 */       return this.result;
/* 4674:     */     }
/* 4675:     */     
/* 4676:     */     public final void compute()
/* 4677:     */     {
/* 4678:     */       ConcurrentHashMapV8.Fun<? super V, ? extends U> transformer;
/* 4679:     */       ConcurrentHashMapV8.BiFun<? super U, ? super U, ? extends U> reducer;
/* 4680:5275 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 4681:     */       {
/* 4682:     */         int f;
/* 4683:     */         int h;
/* 4684:5277 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4685:     */         {
/* 4686:5279 */           addToPendingCount(1);
/* 4687:5280 */           (this.rights = new MapReduceValuesTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer)).fork();
/* 4688:     */         }
/* 4689:5284 */         U r = null;
/* 4690:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4691:5285 */         while ((p = advance()) != null)
/* 4692:     */         {
/* 4693:     */           U u;
/* 4694:5287 */           if ((u = transformer.apply(p.val)) != null) {
/* 4695:5288 */             r = r == null ? u : reducer.apply(r, u);
/* 4696:     */           }
/* 4697:     */         }
/* 4698:5290 */         this.result = r;
/* 4699:5292 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 4700:     */         {
/* 4701:5294 */           MapReduceValuesTask<K, V, U> t = (MapReduceValuesTask)c;
/* 4702:5295 */           MapReduceValuesTask<K, V, U> s = t.rights;
/* 4703:5296 */           while (s != null)
/* 4704:     */           {
/* 4705:     */             U sr;
/* 4706:5298 */             if ((sr = s.result) != null)
/* 4707:     */             {
/* 4708:     */               U tr;
/* 4709:5299 */               t.result = ((tr = t.result) == null ? sr : reducer.apply(tr, sr));
/* 4710:     */             }
/* 4711:5301 */             s = t.rights = s.nextRight;
/* 4712:     */           }
/* 4713:     */         }
/* 4714:     */       }
/* 4715:     */     }
/* 4716:     */   }
/* 4717:     */   
/* 4718:     */   static final class MapReduceEntriesTask<K, V, U>
/* 4719:     */     extends ConcurrentHashMapV8.BulkTask<K, V, U>
/* 4720:     */   {
/* 4721:     */     final ConcurrentHashMapV8.Fun<Map.Entry<K, V>, ? extends U> transformer;
/* 4722:     */     final ConcurrentHashMapV8.BiFun<? super U, ? super U, ? extends U> reducer;
/* 4723:     */     U result;
/* 4724:     */     MapReduceEntriesTask<K, V, U> rights;
/* 4725:     */     MapReduceEntriesTask<K, V, U> nextRight;
/* 4726:     */     
/* 4727:     */     MapReduceEntriesTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceEntriesTask<K, V, U> nextRight, ConcurrentHashMapV8.Fun<Map.Entry<K, V>, ? extends U> transformer, ConcurrentHashMapV8.BiFun<? super U, ? super U, ? extends U> reducer)
/* 4728:     */     {
/* 4729:5320 */       super(b, i, f, t);this.nextRight = nextRight;
/* 4730:5321 */       this.transformer = transformer;
/* 4731:5322 */       this.reducer = reducer;
/* 4732:     */     }
/* 4733:     */     
/* 4734:     */     public final U getRawResult()
/* 4735:     */     {
/* 4736:5324 */       return this.result;
/* 4737:     */     }
/* 4738:     */     
/* 4739:     */     public final void compute()
/* 4740:     */     {
/* 4741:     */       ConcurrentHashMapV8.Fun<Map.Entry<K, V>, ? extends U> transformer;
/* 4742:     */       ConcurrentHashMapV8.BiFun<? super U, ? super U, ? extends U> reducer;
/* 4743:5328 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 4744:     */       {
/* 4745:     */         int f;
/* 4746:     */         int h;
/* 4747:5330 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4748:     */         {
/* 4749:5332 */           addToPendingCount(1);
/* 4750:5333 */           (this.rights = new MapReduceEntriesTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer)).fork();
/* 4751:     */         }
/* 4752:5337 */         U r = null;
/* 4753:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4754:5338 */         while ((p = advance()) != null)
/* 4755:     */         {
/* 4756:     */           U u;
/* 4757:5340 */           if ((u = transformer.apply(p)) != null) {
/* 4758:5341 */             r = r == null ? u : reducer.apply(r, u);
/* 4759:     */           }
/* 4760:     */         }
/* 4761:5343 */         this.result = r;
/* 4762:5345 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 4763:     */         {
/* 4764:5347 */           MapReduceEntriesTask<K, V, U> t = (MapReduceEntriesTask)c;
/* 4765:5348 */           MapReduceEntriesTask<K, V, U> s = t.rights;
/* 4766:5349 */           while (s != null)
/* 4767:     */           {
/* 4768:     */             U sr;
/* 4769:5351 */             if ((sr = s.result) != null)
/* 4770:     */             {
/* 4771:     */               U tr;
/* 4772:5352 */               t.result = ((tr = t.result) == null ? sr : reducer.apply(tr, sr));
/* 4773:     */             }
/* 4774:5354 */             s = t.rights = s.nextRight;
/* 4775:     */           }
/* 4776:     */         }
/* 4777:     */       }
/* 4778:     */     }
/* 4779:     */   }
/* 4780:     */   
/* 4781:     */   static final class MapReduceMappingsTask<K, V, U>
/* 4782:     */     extends ConcurrentHashMapV8.BulkTask<K, V, U>
/* 4783:     */   {
/* 4784:     */     final ConcurrentHashMapV8.BiFun<? super K, ? super V, ? extends U> transformer;
/* 4785:     */     final ConcurrentHashMapV8.BiFun<? super U, ? super U, ? extends U> reducer;
/* 4786:     */     U result;
/* 4787:     */     MapReduceMappingsTask<K, V, U> rights;
/* 4788:     */     MapReduceMappingsTask<K, V, U> nextRight;
/* 4789:     */     
/* 4790:     */     MapReduceMappingsTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceMappingsTask<K, V, U> nextRight, ConcurrentHashMapV8.BiFun<? super K, ? super V, ? extends U> transformer, ConcurrentHashMapV8.BiFun<? super U, ? super U, ? extends U> reducer)
/* 4791:     */     {
/* 4792:5373 */       super(b, i, f, t);this.nextRight = nextRight;
/* 4793:5374 */       this.transformer = transformer;
/* 4794:5375 */       this.reducer = reducer;
/* 4795:     */     }
/* 4796:     */     
/* 4797:     */     public final U getRawResult()
/* 4798:     */     {
/* 4799:5377 */       return this.result;
/* 4800:     */     }
/* 4801:     */     
/* 4802:     */     public final void compute()
/* 4803:     */     {
/* 4804:     */       ConcurrentHashMapV8.BiFun<? super K, ? super V, ? extends U> transformer;
/* 4805:     */       ConcurrentHashMapV8.BiFun<? super U, ? super U, ? extends U> reducer;
/* 4806:5381 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 4807:     */       {
/* 4808:     */         int f;
/* 4809:     */         int h;
/* 4810:5383 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4811:     */         {
/* 4812:5385 */           addToPendingCount(1);
/* 4813:5386 */           (this.rights = new MapReduceMappingsTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer)).fork();
/* 4814:     */         }
/* 4815:5390 */         U r = null;
/* 4816:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4817:5391 */         while ((p = advance()) != null)
/* 4818:     */         {
/* 4819:     */           U u;
/* 4820:5393 */           if ((u = transformer.apply(p.key, p.val)) != null) {
/* 4821:5394 */             r = r == null ? u : reducer.apply(r, u);
/* 4822:     */           }
/* 4823:     */         }
/* 4824:5396 */         this.result = r;
/* 4825:5398 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 4826:     */         {
/* 4827:5400 */           MapReduceMappingsTask<K, V, U> t = (MapReduceMappingsTask)c;
/* 4828:5401 */           MapReduceMappingsTask<K, V, U> s = t.rights;
/* 4829:5402 */           while (s != null)
/* 4830:     */           {
/* 4831:     */             U sr;
/* 4832:5404 */             if ((sr = s.result) != null)
/* 4833:     */             {
/* 4834:     */               U tr;
/* 4835:5405 */               t.result = ((tr = t.result) == null ? sr : reducer.apply(tr, sr));
/* 4836:     */             }
/* 4837:5407 */             s = t.rights = s.nextRight;
/* 4838:     */           }
/* 4839:     */         }
/* 4840:     */       }
/* 4841:     */     }
/* 4842:     */   }
/* 4843:     */   
/* 4844:     */   static final class MapReduceKeysToDoubleTask<K, V>
/* 4845:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Double>
/* 4846:     */   {
/* 4847:     */     final ConcurrentHashMapV8.ObjectToDouble<? super K> transformer;
/* 4848:     */     final ConcurrentHashMapV8.DoubleByDoubleToDouble reducer;
/* 4849:     */     final double basis;
/* 4850:     */     double result;
/* 4851:     */     MapReduceKeysToDoubleTask<K, V> rights;
/* 4852:     */     MapReduceKeysToDoubleTask<K, V> nextRight;
/* 4853:     */     
/* 4854:     */     MapReduceKeysToDoubleTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceKeysToDoubleTask<K, V> nextRight, ConcurrentHashMapV8.ObjectToDouble<? super K> transformer, double basis, ConcurrentHashMapV8.DoubleByDoubleToDouble reducer)
/* 4855:     */     {
/* 4856:5428 */       super(b, i, f, t);this.nextRight = nextRight;
/* 4857:5429 */       this.transformer = transformer;
/* 4858:5430 */       this.basis = basis;this.reducer = reducer;
/* 4859:     */     }
/* 4860:     */     
/* 4861:     */     public final Double getRawResult()
/* 4862:     */     {
/* 4863:5432 */       return Double.valueOf(this.result);
/* 4864:     */     }
/* 4865:     */     
/* 4866:     */     public final void compute()
/* 4867:     */     {
/* 4868:     */       ConcurrentHashMapV8.ObjectToDouble<? super K> transformer;
/* 4869:     */       ConcurrentHashMapV8.DoubleByDoubleToDouble reducer;
/* 4870:5436 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 4871:     */       {
/* 4872:5438 */         double r = this.basis;
/* 4873:     */         int f;
/* 4874:     */         int h;
/* 4875:5439 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4876:     */         {
/* 4877:5441 */           addToPendingCount(1);
/* 4878:5442 */           (this.rights = new MapReduceKeysToDoubleTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
/* 4879:     */         }
/* 4880:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4881:5446 */         while ((p = advance()) != null) {
/* 4882:5447 */           r = reducer.apply(r, transformer.apply(p.key));
/* 4883:     */         }
/* 4884:5448 */         this.result = r;
/* 4885:5450 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 4886:     */         {
/* 4887:5452 */           MapReduceKeysToDoubleTask<K, V> t = (MapReduceKeysToDoubleTask)c;
/* 4888:5453 */           MapReduceKeysToDoubleTask<K, V> s = t.rights;
/* 4889:5454 */           while (s != null)
/* 4890:     */           {
/* 4891:5455 */             t.result = reducer.apply(t.result, s.result);
/* 4892:5456 */             s = t.rights = s.nextRight;
/* 4893:     */           }
/* 4894:     */         }
/* 4895:     */       }
/* 4896:     */     }
/* 4897:     */   }
/* 4898:     */   
/* 4899:     */   static final class MapReduceValuesToDoubleTask<K, V>
/* 4900:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Double>
/* 4901:     */   {
/* 4902:     */     final ConcurrentHashMapV8.ObjectToDouble<? super V> transformer;
/* 4903:     */     final ConcurrentHashMapV8.DoubleByDoubleToDouble reducer;
/* 4904:     */     final double basis;
/* 4905:     */     double result;
/* 4906:     */     MapReduceValuesToDoubleTask<K, V> rights;
/* 4907:     */     MapReduceValuesToDoubleTask<K, V> nextRight;
/* 4908:     */     
/* 4909:     */     MapReduceValuesToDoubleTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceValuesToDoubleTask<K, V> nextRight, ConcurrentHashMapV8.ObjectToDouble<? super V> transformer, double basis, ConcurrentHashMapV8.DoubleByDoubleToDouble reducer)
/* 4910:     */     {
/* 4911:5477 */       super(b, i, f, t);this.nextRight = nextRight;
/* 4912:5478 */       this.transformer = transformer;
/* 4913:5479 */       this.basis = basis;this.reducer = reducer;
/* 4914:     */     }
/* 4915:     */     
/* 4916:     */     public final Double getRawResult()
/* 4917:     */     {
/* 4918:5481 */       return Double.valueOf(this.result);
/* 4919:     */     }
/* 4920:     */     
/* 4921:     */     public final void compute()
/* 4922:     */     {
/* 4923:     */       ConcurrentHashMapV8.ObjectToDouble<? super V> transformer;
/* 4924:     */       ConcurrentHashMapV8.DoubleByDoubleToDouble reducer;
/* 4925:5485 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 4926:     */       {
/* 4927:5487 */         double r = this.basis;
/* 4928:     */         int f;
/* 4929:     */         int h;
/* 4930:5488 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4931:     */         {
/* 4932:5490 */           addToPendingCount(1);
/* 4933:5491 */           (this.rights = new MapReduceValuesToDoubleTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
/* 4934:     */         }
/* 4935:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4936:5495 */         while ((p = advance()) != null) {
/* 4937:5496 */           r = reducer.apply(r, transformer.apply(p.val));
/* 4938:     */         }
/* 4939:5497 */         this.result = r;
/* 4940:5499 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 4941:     */         {
/* 4942:5501 */           MapReduceValuesToDoubleTask<K, V> t = (MapReduceValuesToDoubleTask)c;
/* 4943:5502 */           MapReduceValuesToDoubleTask<K, V> s = t.rights;
/* 4944:5503 */           while (s != null)
/* 4945:     */           {
/* 4946:5504 */             t.result = reducer.apply(t.result, s.result);
/* 4947:5505 */             s = t.rights = s.nextRight;
/* 4948:     */           }
/* 4949:     */         }
/* 4950:     */       }
/* 4951:     */     }
/* 4952:     */   }
/* 4953:     */   
/* 4954:     */   static final class MapReduceEntriesToDoubleTask<K, V>
/* 4955:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Double>
/* 4956:     */   {
/* 4957:     */     final ConcurrentHashMapV8.ObjectToDouble<Map.Entry<K, V>> transformer;
/* 4958:     */     final ConcurrentHashMapV8.DoubleByDoubleToDouble reducer;
/* 4959:     */     final double basis;
/* 4960:     */     double result;
/* 4961:     */     MapReduceEntriesToDoubleTask<K, V> rights;
/* 4962:     */     MapReduceEntriesToDoubleTask<K, V> nextRight;
/* 4963:     */     
/* 4964:     */     MapReduceEntriesToDoubleTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceEntriesToDoubleTask<K, V> nextRight, ConcurrentHashMapV8.ObjectToDouble<Map.Entry<K, V>> transformer, double basis, ConcurrentHashMapV8.DoubleByDoubleToDouble reducer)
/* 4965:     */     {
/* 4966:5526 */       super(b, i, f, t);this.nextRight = nextRight;
/* 4967:5527 */       this.transformer = transformer;
/* 4968:5528 */       this.basis = basis;this.reducer = reducer;
/* 4969:     */     }
/* 4970:     */     
/* 4971:     */     public final Double getRawResult()
/* 4972:     */     {
/* 4973:5530 */       return Double.valueOf(this.result);
/* 4974:     */     }
/* 4975:     */     
/* 4976:     */     public final void compute()
/* 4977:     */     {
/* 4978:     */       ConcurrentHashMapV8.ObjectToDouble<Map.Entry<K, V>> transformer;
/* 4979:     */       ConcurrentHashMapV8.DoubleByDoubleToDouble reducer;
/* 4980:5534 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 4981:     */       {
/* 4982:5536 */         double r = this.basis;
/* 4983:     */         int f;
/* 4984:     */         int h;
/* 4985:5537 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 4986:     */         {
/* 4987:5539 */           addToPendingCount(1);
/* 4988:5540 */           (this.rights = new MapReduceEntriesToDoubleTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
/* 4989:     */         }
/* 4990:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 4991:5544 */         while ((p = advance()) != null) {
/* 4992:5545 */           r = reducer.apply(r, transformer.apply(p));
/* 4993:     */         }
/* 4994:5546 */         this.result = r;
/* 4995:5548 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 4996:     */         {
/* 4997:5550 */           MapReduceEntriesToDoubleTask<K, V> t = (MapReduceEntriesToDoubleTask)c;
/* 4998:5551 */           MapReduceEntriesToDoubleTask<K, V> s = t.rights;
/* 4999:5552 */           while (s != null)
/* 5000:     */           {
/* 5001:5553 */             t.result = reducer.apply(t.result, s.result);
/* 5002:5554 */             s = t.rights = s.nextRight;
/* 5003:     */           }
/* 5004:     */         }
/* 5005:     */       }
/* 5006:     */     }
/* 5007:     */   }
/* 5008:     */   
/* 5009:     */   static final class MapReduceMappingsToDoubleTask<K, V>
/* 5010:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Double>
/* 5011:     */   {
/* 5012:     */     final ConcurrentHashMapV8.ObjectByObjectToDouble<? super K, ? super V> transformer;
/* 5013:     */     final ConcurrentHashMapV8.DoubleByDoubleToDouble reducer;
/* 5014:     */     final double basis;
/* 5015:     */     double result;
/* 5016:     */     MapReduceMappingsToDoubleTask<K, V> rights;
/* 5017:     */     MapReduceMappingsToDoubleTask<K, V> nextRight;
/* 5018:     */     
/* 5019:     */     MapReduceMappingsToDoubleTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceMappingsToDoubleTask<K, V> nextRight, ConcurrentHashMapV8.ObjectByObjectToDouble<? super K, ? super V> transformer, double basis, ConcurrentHashMapV8.DoubleByDoubleToDouble reducer)
/* 5020:     */     {
/* 5021:5575 */       super(b, i, f, t);this.nextRight = nextRight;
/* 5022:5576 */       this.transformer = transformer;
/* 5023:5577 */       this.basis = basis;this.reducer = reducer;
/* 5024:     */     }
/* 5025:     */     
/* 5026:     */     public final Double getRawResult()
/* 5027:     */     {
/* 5028:5579 */       return Double.valueOf(this.result);
/* 5029:     */     }
/* 5030:     */     
/* 5031:     */     public final void compute()
/* 5032:     */     {
/* 5033:     */       ConcurrentHashMapV8.ObjectByObjectToDouble<? super K, ? super V> transformer;
/* 5034:     */       ConcurrentHashMapV8.DoubleByDoubleToDouble reducer;
/* 5035:5583 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 5036:     */       {
/* 5037:5585 */         double r = this.basis;
/* 5038:     */         int f;
/* 5039:     */         int h;
/* 5040:5586 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 5041:     */         {
/* 5042:5588 */           addToPendingCount(1);
/* 5043:5589 */           (this.rights = new MapReduceMappingsToDoubleTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
/* 5044:     */         }
/* 5045:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 5046:5593 */         while ((p = advance()) != null) {
/* 5047:5594 */           r = reducer.apply(r, transformer.apply(p.key, p.val));
/* 5048:     */         }
/* 5049:5595 */         this.result = r;
/* 5050:5597 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 5051:     */         {
/* 5052:5599 */           MapReduceMappingsToDoubleTask<K, V> t = (MapReduceMappingsToDoubleTask)c;
/* 5053:5600 */           MapReduceMappingsToDoubleTask<K, V> s = t.rights;
/* 5054:5601 */           while (s != null)
/* 5055:     */           {
/* 5056:5602 */             t.result = reducer.apply(t.result, s.result);
/* 5057:5603 */             s = t.rights = s.nextRight;
/* 5058:     */           }
/* 5059:     */         }
/* 5060:     */       }
/* 5061:     */     }
/* 5062:     */   }
/* 5063:     */   
/* 5064:     */   static final class MapReduceKeysToLongTask<K, V>
/* 5065:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Long>
/* 5066:     */   {
/* 5067:     */     final ConcurrentHashMapV8.ObjectToLong<? super K> transformer;
/* 5068:     */     final ConcurrentHashMapV8.LongByLongToLong reducer;
/* 5069:     */     final long basis;
/* 5070:     */     long result;
/* 5071:     */     MapReduceKeysToLongTask<K, V> rights;
/* 5072:     */     MapReduceKeysToLongTask<K, V> nextRight;
/* 5073:     */     
/* 5074:     */     MapReduceKeysToLongTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceKeysToLongTask<K, V> nextRight, ConcurrentHashMapV8.ObjectToLong<? super K> transformer, long basis, ConcurrentHashMapV8.LongByLongToLong reducer)
/* 5075:     */     {
/* 5076:5624 */       super(b, i, f, t);this.nextRight = nextRight;
/* 5077:5625 */       this.transformer = transformer;
/* 5078:5626 */       this.basis = basis;this.reducer = reducer;
/* 5079:     */     }
/* 5080:     */     
/* 5081:     */     public final Long getRawResult()
/* 5082:     */     {
/* 5083:5628 */       return Long.valueOf(this.result);
/* 5084:     */     }
/* 5085:     */     
/* 5086:     */     public final void compute()
/* 5087:     */     {
/* 5088:     */       ConcurrentHashMapV8.ObjectToLong<? super K> transformer;
/* 5089:     */       ConcurrentHashMapV8.LongByLongToLong reducer;
/* 5090:5632 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 5091:     */       {
/* 5092:5634 */         long r = this.basis;
/* 5093:     */         int f;
/* 5094:     */         int h;
/* 5095:5635 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 5096:     */         {
/* 5097:5637 */           addToPendingCount(1);
/* 5098:5638 */           (this.rights = new MapReduceKeysToLongTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
/* 5099:     */         }
/* 5100:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 5101:5642 */         while ((p = advance()) != null) {
/* 5102:5643 */           r = reducer.apply(r, transformer.apply(p.key));
/* 5103:     */         }
/* 5104:5644 */         this.result = r;
/* 5105:5646 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 5106:     */         {
/* 5107:5648 */           MapReduceKeysToLongTask<K, V> t = (MapReduceKeysToLongTask)c;
/* 5108:5649 */           MapReduceKeysToLongTask<K, V> s = t.rights;
/* 5109:5650 */           while (s != null)
/* 5110:     */           {
/* 5111:5651 */             t.result = reducer.apply(t.result, s.result);
/* 5112:5652 */             s = t.rights = s.nextRight;
/* 5113:     */           }
/* 5114:     */         }
/* 5115:     */       }
/* 5116:     */     }
/* 5117:     */   }
/* 5118:     */   
/* 5119:     */   static final class MapReduceValuesToLongTask<K, V>
/* 5120:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Long>
/* 5121:     */   {
/* 5122:     */     final ConcurrentHashMapV8.ObjectToLong<? super V> transformer;
/* 5123:     */     final ConcurrentHashMapV8.LongByLongToLong reducer;
/* 5124:     */     final long basis;
/* 5125:     */     long result;
/* 5126:     */     MapReduceValuesToLongTask<K, V> rights;
/* 5127:     */     MapReduceValuesToLongTask<K, V> nextRight;
/* 5128:     */     
/* 5129:     */     MapReduceValuesToLongTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceValuesToLongTask<K, V> nextRight, ConcurrentHashMapV8.ObjectToLong<? super V> transformer, long basis, ConcurrentHashMapV8.LongByLongToLong reducer)
/* 5130:     */     {
/* 5131:5673 */       super(b, i, f, t);this.nextRight = nextRight;
/* 5132:5674 */       this.transformer = transformer;
/* 5133:5675 */       this.basis = basis;this.reducer = reducer;
/* 5134:     */     }
/* 5135:     */     
/* 5136:     */     public final Long getRawResult()
/* 5137:     */     {
/* 5138:5677 */       return Long.valueOf(this.result);
/* 5139:     */     }
/* 5140:     */     
/* 5141:     */     public final void compute()
/* 5142:     */     {
/* 5143:     */       ConcurrentHashMapV8.ObjectToLong<? super V> transformer;
/* 5144:     */       ConcurrentHashMapV8.LongByLongToLong reducer;
/* 5145:5681 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 5146:     */       {
/* 5147:5683 */         long r = this.basis;
/* 5148:     */         int f;
/* 5149:     */         int h;
/* 5150:5684 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 5151:     */         {
/* 5152:5686 */           addToPendingCount(1);
/* 5153:5687 */           (this.rights = new MapReduceValuesToLongTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
/* 5154:     */         }
/* 5155:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 5156:5691 */         while ((p = advance()) != null) {
/* 5157:5692 */           r = reducer.apply(r, transformer.apply(p.val));
/* 5158:     */         }
/* 5159:5693 */         this.result = r;
/* 5160:5695 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 5161:     */         {
/* 5162:5697 */           MapReduceValuesToLongTask<K, V> t = (MapReduceValuesToLongTask)c;
/* 5163:5698 */           MapReduceValuesToLongTask<K, V> s = t.rights;
/* 5164:5699 */           while (s != null)
/* 5165:     */           {
/* 5166:5700 */             t.result = reducer.apply(t.result, s.result);
/* 5167:5701 */             s = t.rights = s.nextRight;
/* 5168:     */           }
/* 5169:     */         }
/* 5170:     */       }
/* 5171:     */     }
/* 5172:     */   }
/* 5173:     */   
/* 5174:     */   static final class MapReduceEntriesToLongTask<K, V>
/* 5175:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Long>
/* 5176:     */   {
/* 5177:     */     final ConcurrentHashMapV8.ObjectToLong<Map.Entry<K, V>> transformer;
/* 5178:     */     final ConcurrentHashMapV8.LongByLongToLong reducer;
/* 5179:     */     final long basis;
/* 5180:     */     long result;
/* 5181:     */     MapReduceEntriesToLongTask<K, V> rights;
/* 5182:     */     MapReduceEntriesToLongTask<K, V> nextRight;
/* 5183:     */     
/* 5184:     */     MapReduceEntriesToLongTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceEntriesToLongTask<K, V> nextRight, ConcurrentHashMapV8.ObjectToLong<Map.Entry<K, V>> transformer, long basis, ConcurrentHashMapV8.LongByLongToLong reducer)
/* 5185:     */     {
/* 5186:5722 */       super(b, i, f, t);this.nextRight = nextRight;
/* 5187:5723 */       this.transformer = transformer;
/* 5188:5724 */       this.basis = basis;this.reducer = reducer;
/* 5189:     */     }
/* 5190:     */     
/* 5191:     */     public final Long getRawResult()
/* 5192:     */     {
/* 5193:5726 */       return Long.valueOf(this.result);
/* 5194:     */     }
/* 5195:     */     
/* 5196:     */     public final void compute()
/* 5197:     */     {
/* 5198:     */       ConcurrentHashMapV8.ObjectToLong<Map.Entry<K, V>> transformer;
/* 5199:     */       ConcurrentHashMapV8.LongByLongToLong reducer;
/* 5200:5730 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 5201:     */       {
/* 5202:5732 */         long r = this.basis;
/* 5203:     */         int f;
/* 5204:     */         int h;
/* 5205:5733 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 5206:     */         {
/* 5207:5735 */           addToPendingCount(1);
/* 5208:5736 */           (this.rights = new MapReduceEntriesToLongTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
/* 5209:     */         }
/* 5210:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 5211:5740 */         while ((p = advance()) != null) {
/* 5212:5741 */           r = reducer.apply(r, transformer.apply(p));
/* 5213:     */         }
/* 5214:5742 */         this.result = r;
/* 5215:5744 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 5216:     */         {
/* 5217:5746 */           MapReduceEntriesToLongTask<K, V> t = (MapReduceEntriesToLongTask)c;
/* 5218:5747 */           MapReduceEntriesToLongTask<K, V> s = t.rights;
/* 5219:5748 */           while (s != null)
/* 5220:     */           {
/* 5221:5749 */             t.result = reducer.apply(t.result, s.result);
/* 5222:5750 */             s = t.rights = s.nextRight;
/* 5223:     */           }
/* 5224:     */         }
/* 5225:     */       }
/* 5226:     */     }
/* 5227:     */   }
/* 5228:     */   
/* 5229:     */   static final class MapReduceMappingsToLongTask<K, V>
/* 5230:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Long>
/* 5231:     */   {
/* 5232:     */     final ConcurrentHashMapV8.ObjectByObjectToLong<? super K, ? super V> transformer;
/* 5233:     */     final ConcurrentHashMapV8.LongByLongToLong reducer;
/* 5234:     */     final long basis;
/* 5235:     */     long result;
/* 5236:     */     MapReduceMappingsToLongTask<K, V> rights;
/* 5237:     */     MapReduceMappingsToLongTask<K, V> nextRight;
/* 5238:     */     
/* 5239:     */     MapReduceMappingsToLongTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceMappingsToLongTask<K, V> nextRight, ConcurrentHashMapV8.ObjectByObjectToLong<? super K, ? super V> transformer, long basis, ConcurrentHashMapV8.LongByLongToLong reducer)
/* 5240:     */     {
/* 5241:5771 */       super(b, i, f, t);this.nextRight = nextRight;
/* 5242:5772 */       this.transformer = transformer;
/* 5243:5773 */       this.basis = basis;this.reducer = reducer;
/* 5244:     */     }
/* 5245:     */     
/* 5246:     */     public final Long getRawResult()
/* 5247:     */     {
/* 5248:5775 */       return Long.valueOf(this.result);
/* 5249:     */     }
/* 5250:     */     
/* 5251:     */     public final void compute()
/* 5252:     */     {
/* 5253:     */       ConcurrentHashMapV8.ObjectByObjectToLong<? super K, ? super V> transformer;
/* 5254:     */       ConcurrentHashMapV8.LongByLongToLong reducer;
/* 5255:5779 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 5256:     */       {
/* 5257:5781 */         long r = this.basis;
/* 5258:     */         int f;
/* 5259:     */         int h;
/* 5260:5782 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 5261:     */         {
/* 5262:5784 */           addToPendingCount(1);
/* 5263:5785 */           (this.rights = new MapReduceMappingsToLongTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
/* 5264:     */         }
/* 5265:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 5266:5789 */         while ((p = advance()) != null) {
/* 5267:5790 */           r = reducer.apply(r, transformer.apply(p.key, p.val));
/* 5268:     */         }
/* 5269:5791 */         this.result = r;
/* 5270:5793 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 5271:     */         {
/* 5272:5795 */           MapReduceMappingsToLongTask<K, V> t = (MapReduceMappingsToLongTask)c;
/* 5273:5796 */           MapReduceMappingsToLongTask<K, V> s = t.rights;
/* 5274:5797 */           while (s != null)
/* 5275:     */           {
/* 5276:5798 */             t.result = reducer.apply(t.result, s.result);
/* 5277:5799 */             s = t.rights = s.nextRight;
/* 5278:     */           }
/* 5279:     */         }
/* 5280:     */       }
/* 5281:     */     }
/* 5282:     */   }
/* 5283:     */   
/* 5284:     */   static final class MapReduceKeysToIntTask<K, V>
/* 5285:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Integer>
/* 5286:     */   {
/* 5287:     */     final ConcurrentHashMapV8.ObjectToInt<? super K> transformer;
/* 5288:     */     final ConcurrentHashMapV8.IntByIntToInt reducer;
/* 5289:     */     final int basis;
/* 5290:     */     int result;
/* 5291:     */     MapReduceKeysToIntTask<K, V> rights;
/* 5292:     */     MapReduceKeysToIntTask<K, V> nextRight;
/* 5293:     */     
/* 5294:     */     MapReduceKeysToIntTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceKeysToIntTask<K, V> nextRight, ConcurrentHashMapV8.ObjectToInt<? super K> transformer, int basis, ConcurrentHashMapV8.IntByIntToInt reducer)
/* 5295:     */     {
/* 5296:5820 */       super(b, i, f, t);this.nextRight = nextRight;
/* 5297:5821 */       this.transformer = transformer;
/* 5298:5822 */       this.basis = basis;this.reducer = reducer;
/* 5299:     */     }
/* 5300:     */     
/* 5301:     */     public final Integer getRawResult()
/* 5302:     */     {
/* 5303:5824 */       return Integer.valueOf(this.result);
/* 5304:     */     }
/* 5305:     */     
/* 5306:     */     public final void compute()
/* 5307:     */     {
/* 5308:     */       ConcurrentHashMapV8.ObjectToInt<? super K> transformer;
/* 5309:     */       ConcurrentHashMapV8.IntByIntToInt reducer;
/* 5310:5828 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 5311:     */       {
/* 5312:5830 */         int r = this.basis;
/* 5313:     */         int f;
/* 5314:     */         int h;
/* 5315:5831 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 5316:     */         {
/* 5317:5833 */           addToPendingCount(1);
/* 5318:5834 */           (this.rights = new MapReduceKeysToIntTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
/* 5319:     */         }
/* 5320:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 5321:5838 */         while ((p = advance()) != null) {
/* 5322:5839 */           r = reducer.apply(r, transformer.apply(p.key));
/* 5323:     */         }
/* 5324:5840 */         this.result = r;
/* 5325:5842 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 5326:     */         {
/* 5327:5844 */           MapReduceKeysToIntTask<K, V> t = (MapReduceKeysToIntTask)c;
/* 5328:5845 */           MapReduceKeysToIntTask<K, V> s = t.rights;
/* 5329:5846 */           while (s != null)
/* 5330:     */           {
/* 5331:5847 */             t.result = reducer.apply(t.result, s.result);
/* 5332:5848 */             s = t.rights = s.nextRight;
/* 5333:     */           }
/* 5334:     */         }
/* 5335:     */       }
/* 5336:     */     }
/* 5337:     */   }
/* 5338:     */   
/* 5339:     */   static final class MapReduceValuesToIntTask<K, V>
/* 5340:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Integer>
/* 5341:     */   {
/* 5342:     */     final ConcurrentHashMapV8.ObjectToInt<? super V> transformer;
/* 5343:     */     final ConcurrentHashMapV8.IntByIntToInt reducer;
/* 5344:     */     final int basis;
/* 5345:     */     int result;
/* 5346:     */     MapReduceValuesToIntTask<K, V> rights;
/* 5347:     */     MapReduceValuesToIntTask<K, V> nextRight;
/* 5348:     */     
/* 5349:     */     MapReduceValuesToIntTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceValuesToIntTask<K, V> nextRight, ConcurrentHashMapV8.ObjectToInt<? super V> transformer, int basis, ConcurrentHashMapV8.IntByIntToInt reducer)
/* 5350:     */     {
/* 5351:5869 */       super(b, i, f, t);this.nextRight = nextRight;
/* 5352:5870 */       this.transformer = transformer;
/* 5353:5871 */       this.basis = basis;this.reducer = reducer;
/* 5354:     */     }
/* 5355:     */     
/* 5356:     */     public final Integer getRawResult()
/* 5357:     */     {
/* 5358:5873 */       return Integer.valueOf(this.result);
/* 5359:     */     }
/* 5360:     */     
/* 5361:     */     public final void compute()
/* 5362:     */     {
/* 5363:     */       ConcurrentHashMapV8.ObjectToInt<? super V> transformer;
/* 5364:     */       ConcurrentHashMapV8.IntByIntToInt reducer;
/* 5365:5877 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 5366:     */       {
/* 5367:5879 */         int r = this.basis;
/* 5368:     */         int f;
/* 5369:     */         int h;
/* 5370:5880 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 5371:     */         {
/* 5372:5882 */           addToPendingCount(1);
/* 5373:5883 */           (this.rights = new MapReduceValuesToIntTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
/* 5374:     */         }
/* 5375:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 5376:5887 */         while ((p = advance()) != null) {
/* 5377:5888 */           r = reducer.apply(r, transformer.apply(p.val));
/* 5378:     */         }
/* 5379:5889 */         this.result = r;
/* 5380:5891 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 5381:     */         {
/* 5382:5893 */           MapReduceValuesToIntTask<K, V> t = (MapReduceValuesToIntTask)c;
/* 5383:5894 */           MapReduceValuesToIntTask<K, V> s = t.rights;
/* 5384:5895 */           while (s != null)
/* 5385:     */           {
/* 5386:5896 */             t.result = reducer.apply(t.result, s.result);
/* 5387:5897 */             s = t.rights = s.nextRight;
/* 5388:     */           }
/* 5389:     */         }
/* 5390:     */       }
/* 5391:     */     }
/* 5392:     */   }
/* 5393:     */   
/* 5394:     */   static final class MapReduceEntriesToIntTask<K, V>
/* 5395:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Integer>
/* 5396:     */   {
/* 5397:     */     final ConcurrentHashMapV8.ObjectToInt<Map.Entry<K, V>> transformer;
/* 5398:     */     final ConcurrentHashMapV8.IntByIntToInt reducer;
/* 5399:     */     final int basis;
/* 5400:     */     int result;
/* 5401:     */     MapReduceEntriesToIntTask<K, V> rights;
/* 5402:     */     MapReduceEntriesToIntTask<K, V> nextRight;
/* 5403:     */     
/* 5404:     */     MapReduceEntriesToIntTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceEntriesToIntTask<K, V> nextRight, ConcurrentHashMapV8.ObjectToInt<Map.Entry<K, V>> transformer, int basis, ConcurrentHashMapV8.IntByIntToInt reducer)
/* 5405:     */     {
/* 5406:5918 */       super(b, i, f, t);this.nextRight = nextRight;
/* 5407:5919 */       this.transformer = transformer;
/* 5408:5920 */       this.basis = basis;this.reducer = reducer;
/* 5409:     */     }
/* 5410:     */     
/* 5411:     */     public final Integer getRawResult()
/* 5412:     */     {
/* 5413:5922 */       return Integer.valueOf(this.result);
/* 5414:     */     }
/* 5415:     */     
/* 5416:     */     public final void compute()
/* 5417:     */     {
/* 5418:     */       ConcurrentHashMapV8.ObjectToInt<Map.Entry<K, V>> transformer;
/* 5419:     */       ConcurrentHashMapV8.IntByIntToInt reducer;
/* 5420:5926 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 5421:     */       {
/* 5422:5928 */         int r = this.basis;
/* 5423:     */         int f;
/* 5424:     */         int h;
/* 5425:5929 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 5426:     */         {
/* 5427:5931 */           addToPendingCount(1);
/* 5428:5932 */           (this.rights = new MapReduceEntriesToIntTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
/* 5429:     */         }
/* 5430:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 5431:5936 */         while ((p = advance()) != null) {
/* 5432:5937 */           r = reducer.apply(r, transformer.apply(p));
/* 5433:     */         }
/* 5434:5938 */         this.result = r;
/* 5435:5940 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 5436:     */         {
/* 5437:5942 */           MapReduceEntriesToIntTask<K, V> t = (MapReduceEntriesToIntTask)c;
/* 5438:5943 */           MapReduceEntriesToIntTask<K, V> s = t.rights;
/* 5439:5944 */           while (s != null)
/* 5440:     */           {
/* 5441:5945 */             t.result = reducer.apply(t.result, s.result);
/* 5442:5946 */             s = t.rights = s.nextRight;
/* 5443:     */           }
/* 5444:     */         }
/* 5445:     */       }
/* 5446:     */     }
/* 5447:     */   }
/* 5448:     */   
/* 5449:     */   static final class MapReduceMappingsToIntTask<K, V>
/* 5450:     */     extends ConcurrentHashMapV8.BulkTask<K, V, Integer>
/* 5451:     */   {
/* 5452:     */     final ConcurrentHashMapV8.ObjectByObjectToInt<? super K, ? super V> transformer;
/* 5453:     */     final ConcurrentHashMapV8.IntByIntToInt reducer;
/* 5454:     */     final int basis;
/* 5455:     */     int result;
/* 5456:     */     MapReduceMappingsToIntTask<K, V> rights;
/* 5457:     */     MapReduceMappingsToIntTask<K, V> nextRight;
/* 5458:     */     
/* 5459:     */     MapReduceMappingsToIntTask(ConcurrentHashMapV8.BulkTask<K, V, ?> p, int b, int i, int f, ConcurrentHashMapV8.Node<K, V>[] t, MapReduceMappingsToIntTask<K, V> nextRight, ConcurrentHashMapV8.ObjectByObjectToInt<? super K, ? super V> transformer, int basis, ConcurrentHashMapV8.IntByIntToInt reducer)
/* 5460:     */     {
/* 5461:5967 */       super(b, i, f, t);this.nextRight = nextRight;
/* 5462:5968 */       this.transformer = transformer;
/* 5463:5969 */       this.basis = basis;this.reducer = reducer;
/* 5464:     */     }
/* 5465:     */     
/* 5466:     */     public final Integer getRawResult()
/* 5467:     */     {
/* 5468:5971 */       return Integer.valueOf(this.result);
/* 5469:     */     }
/* 5470:     */     
/* 5471:     */     public final void compute()
/* 5472:     */     {
/* 5473:     */       ConcurrentHashMapV8.ObjectByObjectToInt<? super K, ? super V> transformer;
/* 5474:     */       ConcurrentHashMapV8.IntByIntToInt reducer;
/* 5475:5975 */       if (((transformer = this.transformer) != null) && ((reducer = this.reducer) != null))
/* 5476:     */       {
/* 5477:5977 */         int r = this.basis;
/* 5478:     */         int f;
/* 5479:     */         int h;
/* 5480:5978 */         for (int i = this.baseIndex; (this.batch > 0) && ((h = (f = this.baseLimit) + i >>> 1) > i);)
/* 5481:     */         {
/* 5482:5980 */           addToPendingCount(1);
/* 5483:5981 */           (this.rights = new MapReduceMappingsToIntTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
/* 5484:     */         }
/* 5485:     */         ConcurrentHashMapV8.Node<K, V> p;
/* 5486:5985 */         while ((p = advance()) != null) {
/* 5487:5986 */           r = reducer.apply(r, transformer.apply(p.key, p.val));
/* 5488:     */         }
/* 5489:5987 */         this.result = r;
/* 5490:5989 */         for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete())
/* 5491:     */         {
/* 5492:5991 */           MapReduceMappingsToIntTask<K, V> t = (MapReduceMappingsToIntTask)c;
/* 5493:5992 */           MapReduceMappingsToIntTask<K, V> s = t.rights;
/* 5494:5993 */           while (s != null)
/* 5495:     */           {
/* 5496:5994 */             t.result = reducer.apply(t.result, s.result);
/* 5497:5995 */             s = t.rights = s.nextRight;
/* 5498:     */           }
/* 5499:     */         }
/* 5500:     */       }
/* 5501:     */     }
/* 5502:     */   }
/* 5503:     */   
/* 5504:     */   static final class CounterCell
/* 5505:     */   {
/* 5506:     */     volatile long p0;
/* 5507:     */     volatile long p1;
/* 5508:     */     volatile long p2;
/* 5509:     */     volatile long p3;
/* 5510:     */     volatile long p4;
/* 5511:     */     volatile long p5;
/* 5512:     */     volatile long p6;
/* 5513:     */     volatile long value;
/* 5514:     */     volatile long q0;
/* 5515:     */     volatile long q1;
/* 5516:     */     volatile long q2;
/* 5517:     */     volatile long q3;
/* 5518:     */     volatile long q4;
/* 5519:     */     volatile long q5;
/* 5520:     */     volatile long q6;
/* 5521:     */     
/* 5522:     */     CounterCell(long x)
/* 5523:     */     {
/* 5524:6012 */       this.value = x;
/* 5525:     */     }
/* 5526:     */   }
/* 5527:     */   
/* 5528:6027 */   static final AtomicInteger counterHashCodeGenerator = new AtomicInteger();
/* 5529:     */   static final int SEED_INCREMENT = 1640531527;
/* 5530:     */   private static final Unsafe U;
/* 5531:     */   private static final long SIZECTL;
/* 5532:     */   private static final long TRANSFERINDEX;
/* 5533:     */   private static final long TRANSFERORIGIN;
/* 5534:     */   private static final long BASECOUNT;
/* 5535:     */   private static final long CELLSBUSY;
/* 5536:     */   private static final long CELLVALUE;
/* 5537:     */   private static final long ABASE;
/* 5538:     */   private static final int ASHIFT;
/* 5539:     */   
/* 5540:     */   final long sumCount()
/* 5541:     */   {
/* 5542:6036 */     CounterCell[] as = this.counterCells;
/* 5543:6037 */     long sum = this.baseCount;
/* 5544:6038 */     if (as != null) {
/* 5545:6039 */       for (int i = 0; i < as.length; i++)
/* 5546:     */       {
/* 5547:     */         CounterCell a;
/* 5548:6040 */         if ((a = as[i]) != null) {
/* 5549:6041 */           sum += a.value;
/* 5550:     */         }
/* 5551:     */       }
/* 5552:     */     }
/* 5553:6044 */     return sum;
/* 5554:     */   }
/* 5555:     */   
/* 5556:     */   private final void fullAddCount(InternalThreadLocalMap threadLocals, long x, IntegerHolder hc, boolean wasUncontended)
/* 5557:     */   {
/* 5558:     */     int h;
/* 5559:6052 */     if (hc == null)
/* 5560:     */     {
/* 5561:6053 */       hc = new IntegerHolder();
/* 5562:6054 */       int s = counterHashCodeGenerator.addAndGet(1640531527);
/* 5563:6055 */       int h = hc.value = s == 0 ? 1 : s;
/* 5564:6056 */       threadLocals.setCounterHashCode(hc);
/* 5565:     */     }
/* 5566:     */     else
/* 5567:     */     {
/* 5568:6059 */       h = hc.value;
/* 5569:     */     }
/* 5570:6060 */     boolean collide = false;
/* 5571:     */     for (;;)
/* 5572:     */     {
/* 5573:     */       CounterCell[] as;
/* 5574:     */       int n;
/* 5575:6063 */       if (((as = this.counterCells) != null) && ((n = as.length) > 0))
/* 5576:     */       {
/* 5577:     */         CounterCell a;
/* 5578:6064 */         if ((a = as[(n - 1 & h)]) == null)
/* 5579:     */         {
/* 5580:6065 */           if (this.cellsBusy == 0)
/* 5581:     */           {
/* 5582:6066 */             CounterCell r = new CounterCell(x);
/* 5583:6067 */             if ((this.cellsBusy == 0) && (U.compareAndSwapInt(this, CELLSBUSY, 0, 1)))
/* 5584:     */             {
/* 5585:6069 */               boolean created = false;
/* 5586:     */               try
/* 5587:     */               {
/* 5588:     */                 CounterCell[] rs;
/* 5589:     */                 int m;
/* 5590:     */                 int j;
/* 5591:6072 */                 if (((rs = this.counterCells) != null) && ((m = rs.length) > 0) && (rs[(j = m - 1 & h)] == null))
/* 5592:     */                 {
/* 5593:6075 */                   rs[j] = r;
/* 5594:6076 */                   created = true;
/* 5595:     */                 }
/* 5596:     */               }
/* 5597:     */               finally
/* 5598:     */               {
/* 5599:6079 */                 this.cellsBusy = 0;
/* 5600:     */               }
/* 5601:6081 */               if (!created) {
/* 5602:     */                 continue;
/* 5603:     */               }
/* 5604:6082 */               break;
/* 5605:     */             }
/* 5606:     */           }
/* 5607:6086 */           collide = false;
/* 5608:     */         }
/* 5609:6088 */         else if (!wasUncontended)
/* 5610:     */         {
/* 5611:6089 */           wasUncontended = true;
/* 5612:     */         }
/* 5613:     */         else
/* 5614:     */         {
/* 5615:     */           long v;
/* 5616:6090 */           if (U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x)) {
/* 5617:     */             break;
/* 5618:     */           }
/* 5619:6092 */           if ((this.counterCells != as) || (n >= NCPU))
/* 5620:     */           {
/* 5621:6093 */             collide = false;
/* 5622:     */           }
/* 5623:6094 */           else if (!collide)
/* 5624:     */           {
/* 5625:6095 */             collide = true;
/* 5626:     */           }
/* 5627:6096 */           else if ((this.cellsBusy == 0) && (U.compareAndSwapInt(this, CELLSBUSY, 0, 1)))
/* 5628:     */           {
/* 5629:     */             try
/* 5630:     */             {
/* 5631:6099 */               if (this.counterCells == as)
/* 5632:     */               {
/* 5633:6100 */                 CounterCell[] rs = new CounterCell[n << 1];
/* 5634:6101 */                 for (int i = 0; i < n; i++) {
/* 5635:6102 */                   rs[i] = as[i];
/* 5636:     */                 }
/* 5637:6103 */                 this.counterCells = rs;
/* 5638:     */               }
/* 5639:     */             }
/* 5640:     */             finally
/* 5641:     */             {
/* 5642:6106 */               this.cellsBusy = 0;
/* 5643:     */             }
/* 5644:6108 */             collide = false;
/* 5645:6109 */             continue;
/* 5646:     */           }
/* 5647:     */         }
/* 5648:6111 */         h ^= h << 13;
/* 5649:6112 */         h ^= h >>> 17;
/* 5650:6113 */         h ^= h << 5;
/* 5651:     */       }
/* 5652:6115 */       else if ((this.cellsBusy == 0) && (this.counterCells == as) && (U.compareAndSwapInt(this, CELLSBUSY, 0, 1)))
/* 5653:     */       {
/* 5654:6117 */         boolean init = false;
/* 5655:     */         try
/* 5656:     */         {
/* 5657:6119 */           if (this.counterCells == as)
/* 5658:     */           {
/* 5659:6120 */             CounterCell[] rs = new CounterCell[2];
/* 5660:6121 */             rs[(h & 0x1)] = new CounterCell(x);
/* 5661:6122 */             this.counterCells = rs;
/* 5662:6123 */             init = true;
/* 5663:     */           }
/* 5664:     */         }
/* 5665:     */         finally
/* 5666:     */         {
/* 5667:6126 */           this.cellsBusy = 0;
/* 5668:     */         }
/* 5669:6128 */         if (init) {
/* 5670:     */           break;
/* 5671:     */         }
/* 5672:     */       }
/* 5673:     */       else
/* 5674:     */       {
/* 5675:     */         long v;
/* 5676:6131 */         if (U.compareAndSwapLong(this, BASECOUNT, v = this.baseCount, v + x)) {
/* 5677:     */           break;
/* 5678:     */         }
/* 5679:     */       }
/* 5680:     */     }
/* 5681:6134 */     hc.value = h;
/* 5682:     */   }
/* 5683:     */   
/* 5684:     */   static
/* 5685:     */   {
/* 5686:     */     try
/* 5687:     */     {
/* 5688:6150 */       U = getUnsafe();
/* 5689:6151 */       Class<?> k = ConcurrentHashMapV8.class;
/* 5690:6152 */       SIZECTL = U.objectFieldOffset(k.getDeclaredField("sizeCtl"));
/* 5691:     */       
/* 5692:6154 */       TRANSFERINDEX = U.objectFieldOffset(k.getDeclaredField("transferIndex"));
/* 5693:     */       
/* 5694:6156 */       TRANSFERORIGIN = U.objectFieldOffset(k.getDeclaredField("transferOrigin"));
/* 5695:     */       
/* 5696:6158 */       BASECOUNT = U.objectFieldOffset(k.getDeclaredField("baseCount"));
/* 5697:     */       
/* 5698:6160 */       CELLSBUSY = U.objectFieldOffset(k.getDeclaredField("cellsBusy"));
/* 5699:     */       
/* 5700:6162 */       Class<?> ck = CounterCell.class;
/* 5701:6163 */       CELLVALUE = U.objectFieldOffset(ck.getDeclaredField("value"));
/* 5702:     */       
/* 5703:6165 */       Class<?> ak = [Lio.netty.util.internal.chmv8.ConcurrentHashMapV8.Node.class;
/* 5704:6166 */       ABASE = U.arrayBaseOffset(ak);
/* 5705:6167 */       int scale = U.arrayIndexScale(ak);
/* 5706:6168 */       if ((scale & scale - 1) != 0) {
/* 5707:6169 */         throw new Error("data type scale not a power of two");
/* 5708:     */       }
/* 5709:6170 */       ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
/* 5710:     */     }
/* 5711:     */     catch (Exception e)
/* 5712:     */     {
/* 5713:6172 */       throw new Error(e);
/* 5714:     */     }
/* 5715:     */   }
/* 5716:     */   
/* 5717:     */   private static Unsafe getUnsafe()
/* 5718:     */   {
/* 5719:     */     try
/* 5720:     */     {
/* 5721:6185 */       return Unsafe.getUnsafe();
/* 5722:     */     }
/* 5723:     */     catch (SecurityException tryReflectionInstead)
/* 5724:     */     {
/* 5725:     */       try
/* 5726:     */       {
/* 5727:6188 */         (Unsafe)AccessController.doPrivileged(new PrivilegedExceptionAction()
/* 5728:     */         {
/* 5729:     */           public Unsafe run()
/* 5730:     */             throws Exception
/* 5731:     */           {
/* 5732:6191 */             Class<Unsafe> k = Unsafe.class;
/* 5733:6192 */             for (Field f : k.getDeclaredFields())
/* 5734:     */             {
/* 5735:6193 */               f.setAccessible(true);
/* 5736:6194 */               Object x = f.get(null);
/* 5737:6195 */               if (k.isInstance(x)) {
/* 5738:6196 */                 return (Unsafe)k.cast(x);
/* 5739:     */               }
/* 5740:     */             }
/* 5741:6198 */             throw new NoSuchFieldError("the Unsafe");
/* 5742:     */           }
/* 5743:     */         });
/* 5744:     */       }
/* 5745:     */       catch (PrivilegedActionException e)
/* 5746:     */       {
/* 5747:6201 */         throw new RuntimeException("Could not initialize intrinsics", e.getCause());
/* 5748:     */       }
/* 5749:     */     }
/* 5750:     */   }
/* 5751:     */   
/* 5752:     */   public ConcurrentHashMapV8() {}
/* 5753:     */   
/* 5754:     */   static final class CounterHashCode
/* 5755:     */   {
/* 5756:     */     int code;
/* 5757:     */   }
/* 5758:     */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.chmv8.ConcurrentHashMapV8
 * JD-Core Version:    0.7.0.1
 */