/*    1:     */ package io.netty.channel;
/*    2:     */ 
/*    3:     */ import io.netty.util.ReferenceCountUtil;
/*    4:     */ import io.netty.util.concurrent.EventExecutor;
/*    5:     */ import io.netty.util.concurrent.EventExecutorGroup;
/*    6:     */ import io.netty.util.internal.PlatformDependent;
/*    7:     */ import io.netty.util.internal.StringUtil;
/*    8:     */ import io.netty.util.internal.logging.InternalLogger;
/*    9:     */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   10:     */ import java.net.SocketAddress;
/*   11:     */ import java.util.ArrayList;
/*   12:     */ import java.util.HashMap;
/*   13:     */ import java.util.IdentityHashMap;
/*   14:     */ import java.util.Iterator;
/*   15:     */ import java.util.LinkedHashMap;
/*   16:     */ import java.util.List;
/*   17:     */ import java.util.Map;
/*   18:     */ import java.util.Map.Entry;
/*   19:     */ import java.util.NoSuchElementException;
/*   20:     */ import java.util.Set;
/*   21:     */ import java.util.WeakHashMap;
/*   22:     */ import java.util.concurrent.ExecutionException;
/*   23:     */ import java.util.concurrent.Future;
/*   24:     */ 
/*   25:     */ final class DefaultChannelPipeline
/*   26:     */   implements ChannelPipeline
/*   27:     */ {
/*   28:     */   static final InternalLogger logger;
/*   29:     */   private static final WeakHashMap<Class<?>, String>[] nameCaches;
/*   30:     */   final AbstractChannel channel;
/*   31:     */   final AbstractChannelHandlerContext head;
/*   32:     */   final AbstractChannelHandlerContext tail;
/*   33:     */   
/*   34:     */   static
/*   35:     */   {
/*   36:  46 */     logger = InternalLoggerFactory.getInstance(DefaultChannelPipeline.class);
/*   37:     */     
/*   38:     */ 
/*   39:  49 */     nameCaches = new WeakHashMap[Runtime.getRuntime().availableProcessors()];
/*   40:  53 */     for (int i = 0; i < nameCaches.length; i++) {
/*   41:  54 */       nameCaches[i] = new WeakHashMap();
/*   42:     */     }
/*   43:     */   }
/*   44:     */   
/*   45:  63 */   private final Map<String, AbstractChannelHandlerContext> name2ctx = new HashMap(4);
/*   46:  66 */   final Map<EventExecutorGroup, EventExecutor> childExecutors = new IdentityHashMap();
/*   47:     */   
/*   48:     */   public DefaultChannelPipeline(AbstractChannel channel)
/*   49:     */   {
/*   50:  70 */     if (channel == null) {
/*   51:  71 */       throw new NullPointerException("channel");
/*   52:     */     }
/*   53:  73 */     this.channel = channel;
/*   54:     */     
/*   55:  75 */     this.tail = new TailContext(this);
/*   56:  76 */     this.head = new HeadContext(this);
/*   57:     */     
/*   58:  78 */     this.head.next = this.tail;
/*   59:  79 */     this.tail.prev = this.head;
/*   60:     */   }
/*   61:     */   
/*   62:     */   public Channel channel()
/*   63:     */   {
/*   64:  84 */     return this.channel;
/*   65:     */   }
/*   66:     */   
/*   67:     */   public ChannelPipeline addFirst(String name, ChannelHandler handler)
/*   68:     */   {
/*   69:  89 */     return addFirst(null, name, handler);
/*   70:     */   }
/*   71:     */   
/*   72:     */   public ChannelPipeline addFirst(EventExecutorGroup group, String name, ChannelHandler handler)
/*   73:     */   {
/*   74:  94 */     synchronized (this)
/*   75:     */     {
/*   76:  95 */       checkDuplicateName(name);
/*   77:  96 */       AbstractChannelHandlerContext newCtx = new DefaultChannelHandlerContext(this, group, name, handler);
/*   78:  97 */       addFirst0(name, newCtx);
/*   79:     */     }
/*   80: 100 */     return this;
/*   81:     */   }
/*   82:     */   
/*   83:     */   private void addFirst0(String name, AbstractChannelHandlerContext newCtx)
/*   84:     */   {
/*   85: 104 */     checkMultiplicity(newCtx);
/*   86:     */     
/*   87: 106 */     AbstractChannelHandlerContext nextCtx = this.head.next;
/*   88: 107 */     newCtx.prev = this.head;
/*   89: 108 */     newCtx.next = nextCtx;
/*   90: 109 */     this.head.next = newCtx;
/*   91: 110 */     nextCtx.prev = newCtx;
/*   92:     */     
/*   93: 112 */     this.name2ctx.put(name, newCtx);
/*   94:     */     
/*   95: 114 */     callHandlerAdded(newCtx);
/*   96:     */   }
/*   97:     */   
/*   98:     */   public ChannelPipeline addLast(String name, ChannelHandler handler)
/*   99:     */   {
/*  100: 119 */     return addLast(null, name, handler);
/*  101:     */   }
/*  102:     */   
/*  103:     */   public ChannelPipeline addLast(EventExecutorGroup group, String name, ChannelHandler handler)
/*  104:     */   {
/*  105: 124 */     synchronized (this)
/*  106:     */     {
/*  107: 125 */       checkDuplicateName(name);
/*  108:     */       
/*  109: 127 */       AbstractChannelHandlerContext newCtx = new DefaultChannelHandlerContext(this, group, name, handler);
/*  110: 128 */       addLast0(name, newCtx);
/*  111:     */     }
/*  112: 131 */     return this;
/*  113:     */   }
/*  114:     */   
/*  115:     */   private void addLast0(String name, AbstractChannelHandlerContext newCtx)
/*  116:     */   {
/*  117: 135 */     checkMultiplicity(newCtx);
/*  118:     */     
/*  119: 137 */     AbstractChannelHandlerContext prev = this.tail.prev;
/*  120: 138 */     newCtx.prev = prev;
/*  121: 139 */     newCtx.next = this.tail;
/*  122: 140 */     prev.next = newCtx;
/*  123: 141 */     this.tail.prev = newCtx;
/*  124:     */     
/*  125: 143 */     this.name2ctx.put(name, newCtx);
/*  126:     */     
/*  127: 145 */     callHandlerAdded(newCtx);
/*  128:     */   }
/*  129:     */   
/*  130:     */   public ChannelPipeline addBefore(String baseName, String name, ChannelHandler handler)
/*  131:     */   {
/*  132: 150 */     return addBefore(null, baseName, name, handler);
/*  133:     */   }
/*  134:     */   
/*  135:     */   public ChannelPipeline addBefore(EventExecutorGroup group, String baseName, String name, ChannelHandler handler)
/*  136:     */   {
/*  137: 156 */     synchronized (this)
/*  138:     */     {
/*  139: 157 */       AbstractChannelHandlerContext ctx = getContextOrDie(baseName);
/*  140: 158 */       checkDuplicateName(name);
/*  141: 159 */       AbstractChannelHandlerContext newCtx = new DefaultChannelHandlerContext(this, group, name, handler);
/*  142: 160 */       addBefore0(name, ctx, newCtx);
/*  143:     */     }
/*  144: 162 */     return this;
/*  145:     */   }
/*  146:     */   
/*  147:     */   private void addBefore0(String name, AbstractChannelHandlerContext ctx, AbstractChannelHandlerContext newCtx)
/*  148:     */   {
/*  149: 167 */     checkMultiplicity(newCtx);
/*  150:     */     
/*  151: 169 */     newCtx.prev = ctx.prev;
/*  152: 170 */     newCtx.next = ctx;
/*  153: 171 */     ctx.prev.next = newCtx;
/*  154: 172 */     ctx.prev = newCtx;
/*  155:     */     
/*  156: 174 */     this.name2ctx.put(name, newCtx);
/*  157:     */     
/*  158: 176 */     callHandlerAdded(newCtx);
/*  159:     */   }
/*  160:     */   
/*  161:     */   public ChannelPipeline addAfter(String baseName, String name, ChannelHandler handler)
/*  162:     */   {
/*  163: 181 */     return addAfter(null, baseName, name, handler);
/*  164:     */   }
/*  165:     */   
/*  166:     */   public ChannelPipeline addAfter(EventExecutorGroup group, String baseName, String name, ChannelHandler handler)
/*  167:     */   {
/*  168: 187 */     synchronized (this)
/*  169:     */     {
/*  170: 188 */       AbstractChannelHandlerContext ctx = getContextOrDie(baseName);
/*  171: 189 */       checkDuplicateName(name);
/*  172: 190 */       AbstractChannelHandlerContext newCtx = new DefaultChannelHandlerContext(this, group, name, handler);
/*  173:     */       
/*  174: 192 */       addAfter0(name, ctx, newCtx);
/*  175:     */     }
/*  176: 195 */     return this;
/*  177:     */   }
/*  178:     */   
/*  179:     */   private void addAfter0(String name, AbstractChannelHandlerContext ctx, AbstractChannelHandlerContext newCtx)
/*  180:     */   {
/*  181: 199 */     checkDuplicateName(name);
/*  182: 200 */     checkMultiplicity(newCtx);
/*  183:     */     
/*  184: 202 */     newCtx.prev = ctx;
/*  185: 203 */     newCtx.next = ctx.next;
/*  186: 204 */     ctx.next.prev = newCtx;
/*  187: 205 */     ctx.next = newCtx;
/*  188:     */     
/*  189: 207 */     this.name2ctx.put(name, newCtx);
/*  190:     */     
/*  191: 209 */     callHandlerAdded(newCtx);
/*  192:     */   }
/*  193:     */   
/*  194:     */   public ChannelPipeline addFirst(ChannelHandler... handlers)
/*  195:     */   {
/*  196: 214 */     return addFirst(null, handlers);
/*  197:     */   }
/*  198:     */   
/*  199:     */   public ChannelPipeline addFirst(EventExecutorGroup executor, ChannelHandler... handlers)
/*  200:     */   {
/*  201: 219 */     if (handlers == null) {
/*  202: 220 */       throw new NullPointerException("handlers");
/*  203:     */     }
/*  204: 222 */     if ((handlers.length == 0) || (handlers[0] == null)) {
/*  205: 223 */       return this;
/*  206:     */     }
/*  207: 227 */     for (int size = 1; size < handlers.length; size++) {
/*  208: 228 */       if (handlers[size] == null) {
/*  209:     */         break;
/*  210:     */       }
/*  211:     */     }
/*  212: 233 */     for (int i = size - 1; i >= 0; i--)
/*  213:     */     {
/*  214: 234 */       ChannelHandler h = handlers[i];
/*  215: 235 */       addFirst(executor, generateName(h), h);
/*  216:     */     }
/*  217: 238 */     return this;
/*  218:     */   }
/*  219:     */   
/*  220:     */   public ChannelPipeline addLast(ChannelHandler... handlers)
/*  221:     */   {
/*  222: 243 */     return addLast(null, handlers);
/*  223:     */   }
/*  224:     */   
/*  225:     */   public ChannelPipeline addLast(EventExecutorGroup executor, ChannelHandler... handlers)
/*  226:     */   {
/*  227: 248 */     if (handlers == null) {
/*  228: 249 */       throw new NullPointerException("handlers");
/*  229:     */     }
/*  230: 252 */     for (ChannelHandler h : handlers)
/*  231:     */     {
/*  232: 253 */       if (h == null) {
/*  233:     */         break;
/*  234:     */       }
/*  235: 256 */       addLast(executor, generateName(h), h);
/*  236:     */     }
/*  237: 259 */     return this;
/*  238:     */   }
/*  239:     */   
/*  240:     */   private String generateName(ChannelHandler handler)
/*  241:     */   {
/*  242: 263 */     WeakHashMap<Class<?>, String> cache = nameCaches[((int)(Thread.currentThread().getId() % nameCaches.length))];
/*  243: 264 */     Class<?> handlerType = handler.getClass();
/*  244:     */     String name;
/*  245: 266 */     synchronized (cache)
/*  246:     */     {
/*  247: 267 */       name = (String)cache.get(handlerType);
/*  248: 268 */       if (name == null)
/*  249:     */       {
/*  250: 269 */         name = generateName0(handlerType);
/*  251: 270 */         cache.put(handlerType, name);
/*  252:     */       }
/*  253:     */     }
/*  254: 274 */     synchronized (this)
/*  255:     */     {
/*  256: 277 */       if (this.name2ctx.containsKey(name))
/*  257:     */       {
/*  258: 278 */         String baseName = name.substring(0, name.length() - 1);
/*  259: 279 */         for (int i = 1;; i++)
/*  260:     */         {
/*  261: 280 */           String newName = baseName + i;
/*  262: 281 */           if (!this.name2ctx.containsKey(newName))
/*  263:     */           {
/*  264: 282 */             name = newName;
/*  265: 283 */             break;
/*  266:     */           }
/*  267:     */         }
/*  268:     */       }
/*  269:     */     }
/*  270: 289 */     return name;
/*  271:     */   }
/*  272:     */   
/*  273:     */   private static String generateName0(Class<?> handlerType)
/*  274:     */   {
/*  275: 293 */     return StringUtil.simpleClassName(handlerType) + "#0";
/*  276:     */   }
/*  277:     */   
/*  278:     */   public ChannelPipeline remove(ChannelHandler handler)
/*  279:     */   {
/*  280: 298 */     remove(getContextOrDie(handler));
/*  281: 299 */     return this;
/*  282:     */   }
/*  283:     */   
/*  284:     */   public ChannelHandler remove(String name)
/*  285:     */   {
/*  286: 304 */     return remove(getContextOrDie(name)).handler();
/*  287:     */   }
/*  288:     */   
/*  289:     */   public <T extends ChannelHandler> T remove(Class<T> handlerType)
/*  290:     */   {
/*  291: 310 */     return remove(getContextOrDie(handlerType)).handler();
/*  292:     */   }
/*  293:     */   
/*  294:     */   private AbstractChannelHandlerContext remove(final AbstractChannelHandlerContext ctx)
/*  295:     */   {
/*  296: 314 */     assert ((ctx != this.head) && (ctx != this.tail));
/*  297:     */     Future<?> future;
/*  298:     */     AbstractChannelHandlerContext context;
/*  299: 319 */     synchronized (this)
/*  300:     */     {
/*  301: 320 */       if ((!ctx.channel().isRegistered()) || (ctx.executor().inEventLoop()))
/*  302:     */       {
/*  303: 321 */         remove0(ctx);
/*  304: 322 */         return ctx;
/*  305:     */       }
/*  306: 324 */       future = ctx.executor().submit(new Runnable()
/*  307:     */       {
/*  308:     */         public void run()
/*  309:     */         {
/*  310: 327 */           synchronized (DefaultChannelPipeline.this)
/*  311:     */           {
/*  312: 328 */             DefaultChannelPipeline.this.remove0(ctx);
/*  313:     */           }
/*  314:     */         }
/*  315: 331 */       });
/*  316: 332 */       context = ctx;
/*  317:     */     }
/*  318: 339 */     waitForFuture(future);
/*  319:     */     
/*  320: 341 */     return context;
/*  321:     */   }
/*  322:     */   
/*  323:     */   void remove0(AbstractChannelHandlerContext ctx)
/*  324:     */   {
/*  325: 345 */     AbstractChannelHandlerContext prev = ctx.prev;
/*  326: 346 */     AbstractChannelHandlerContext next = ctx.next;
/*  327: 347 */     prev.next = next;
/*  328: 348 */     next.prev = prev;
/*  329: 349 */     this.name2ctx.remove(ctx.name());
/*  330: 350 */     callHandlerRemoved(ctx);
/*  331:     */   }
/*  332:     */   
/*  333:     */   public ChannelHandler removeFirst()
/*  334:     */   {
/*  335: 355 */     if (this.head.next == this.tail) {
/*  336: 356 */       throw new NoSuchElementException();
/*  337:     */     }
/*  338: 358 */     return remove(this.head.next).handler();
/*  339:     */   }
/*  340:     */   
/*  341:     */   public ChannelHandler removeLast()
/*  342:     */   {
/*  343: 363 */     if (this.head.next == this.tail) {
/*  344: 364 */       throw new NoSuchElementException();
/*  345:     */     }
/*  346: 366 */     return remove(this.tail.prev).handler();
/*  347:     */   }
/*  348:     */   
/*  349:     */   public ChannelPipeline replace(ChannelHandler oldHandler, String newName, ChannelHandler newHandler)
/*  350:     */   {
/*  351: 371 */     replace(getContextOrDie(oldHandler), newName, newHandler);
/*  352: 372 */     return this;
/*  353:     */   }
/*  354:     */   
/*  355:     */   public ChannelHandler replace(String oldName, String newName, ChannelHandler newHandler)
/*  356:     */   {
/*  357: 377 */     return replace(getContextOrDie(oldName), newName, newHandler);
/*  358:     */   }
/*  359:     */   
/*  360:     */   public <T extends ChannelHandler> T replace(Class<T> oldHandlerType, String newName, ChannelHandler newHandler)
/*  361:     */   {
/*  362: 384 */     return replace(getContextOrDie(oldHandlerType), newName, newHandler);
/*  363:     */   }
/*  364:     */   
/*  365:     */   private ChannelHandler replace(final AbstractChannelHandlerContext ctx, final String newName, ChannelHandler newHandler)
/*  366:     */   {
/*  367: 391 */     assert ((ctx != this.head) && (ctx != this.tail));
/*  368:     */     Future<?> future;
/*  369: 394 */     synchronized (this)
/*  370:     */     {
/*  371: 395 */       boolean sameName = ctx.name().equals(newName);
/*  372: 396 */       if (!sameName) {
/*  373: 397 */         checkDuplicateName(newName);
/*  374:     */       }
/*  375: 400 */       final AbstractChannelHandlerContext newCtx = new DefaultChannelHandlerContext(this, ctx.executor, newName, newHandler);
/*  376: 403 */       if ((!newCtx.channel().isRegistered()) || (newCtx.executor().inEventLoop()))
/*  377:     */       {
/*  378: 404 */         replace0(ctx, newName, newCtx);
/*  379: 405 */         return ctx.handler();
/*  380:     */       }
/*  381: 407 */       future = newCtx.executor().submit(new Runnable()
/*  382:     */       {
/*  383:     */         public void run()
/*  384:     */         {
/*  385: 410 */           synchronized (DefaultChannelPipeline.this)
/*  386:     */           {
/*  387: 411 */             DefaultChannelPipeline.this.replace0(ctx, newName, newCtx);
/*  388:     */           }
/*  389:     */         }
/*  390:     */       });
/*  391:     */     }
/*  392: 421 */     waitForFuture(future);
/*  393:     */     
/*  394: 423 */     return ctx.handler();
/*  395:     */   }
/*  396:     */   
/*  397:     */   private void replace0(AbstractChannelHandlerContext oldCtx, String newName, AbstractChannelHandlerContext newCtx)
/*  398:     */   {
/*  399: 428 */     checkMultiplicity(newCtx);
/*  400:     */     
/*  401: 430 */     AbstractChannelHandlerContext prev = oldCtx.prev;
/*  402: 431 */     AbstractChannelHandlerContext next = oldCtx.next;
/*  403: 432 */     newCtx.prev = prev;
/*  404: 433 */     newCtx.next = next;
/*  405:     */     
/*  406:     */ 
/*  407:     */ 
/*  408:     */ 
/*  409:     */ 
/*  410: 439 */     prev.next = newCtx;
/*  411: 440 */     next.prev = newCtx;
/*  412: 442 */     if (!oldCtx.name().equals(newName)) {
/*  413: 443 */       this.name2ctx.remove(oldCtx.name());
/*  414:     */     }
/*  415: 445 */     this.name2ctx.put(newName, newCtx);
/*  416:     */     
/*  417:     */ 
/*  418: 448 */     oldCtx.prev = newCtx;
/*  419: 449 */     oldCtx.next = newCtx;
/*  420:     */     
/*  421:     */ 
/*  422:     */ 
/*  423:     */ 
/*  424: 454 */     callHandlerAdded(newCtx);
/*  425: 455 */     callHandlerRemoved(oldCtx);
/*  426:     */   }
/*  427:     */   
/*  428:     */   private static void checkMultiplicity(ChannelHandlerContext ctx)
/*  429:     */   {
/*  430: 459 */     ChannelHandler handler = ctx.handler();
/*  431: 460 */     if ((handler instanceof ChannelHandlerAdapter))
/*  432:     */     {
/*  433: 461 */       ChannelHandlerAdapter h = (ChannelHandlerAdapter)handler;
/*  434: 462 */       if ((!h.isSharable()) && (h.added)) {
/*  435: 463 */         throw new ChannelPipelineException(h.getClass().getName() + " is not a @Sharable handler, so can't be added or removed multiple times.");
/*  436:     */       }
/*  437: 467 */       h.added = true;
/*  438:     */     }
/*  439:     */   }
/*  440:     */   
/*  441:     */   private void callHandlerAdded(final ChannelHandlerContext ctx)
/*  442:     */   {
/*  443: 472 */     if ((ctx.channel().isRegistered()) && (!ctx.executor().inEventLoop()))
/*  444:     */     {
/*  445: 473 */       ctx.executor().execute(new Runnable()
/*  446:     */       {
/*  447:     */         public void run()
/*  448:     */         {
/*  449: 476 */           DefaultChannelPipeline.this.callHandlerAdded0(ctx);
/*  450:     */         }
/*  451: 478 */       });
/*  452: 479 */       return;
/*  453:     */     }
/*  454: 481 */     callHandlerAdded0(ctx);
/*  455:     */   }
/*  456:     */   
/*  457:     */   private void callHandlerAdded0(ChannelHandlerContext ctx)
/*  458:     */   {
/*  459:     */     try
/*  460:     */     {
/*  461: 486 */       ctx.handler().handlerAdded(ctx);
/*  462:     */     }
/*  463:     */     catch (Throwable t)
/*  464:     */     {
/*  465: 488 */       boolean removed = false;
/*  466:     */       try
/*  467:     */       {
/*  468: 490 */         remove((AbstractChannelHandlerContext)ctx);
/*  469: 491 */         removed = true;
/*  470:     */       }
/*  471:     */       catch (Throwable t2)
/*  472:     */       {
/*  473: 493 */         if (logger.isWarnEnabled()) {
/*  474: 494 */           logger.warn("Failed to remove a handler: " + ctx.name(), t2);
/*  475:     */         }
/*  476:     */       }
/*  477: 498 */       if (removed) {
/*  478: 499 */         fireExceptionCaught(new ChannelPipelineException(ctx.handler().getClass().getName() + ".handlerAdded() has thrown an exception; removed.", t));
/*  479:     */       } else {
/*  480: 503 */         fireExceptionCaught(new ChannelPipelineException(ctx.handler().getClass().getName() + ".handlerAdded() has thrown an exception; also failed to remove.", t));
/*  481:     */       }
/*  482:     */     }
/*  483:     */   }
/*  484:     */   
/*  485:     */   private void callHandlerRemoved(final AbstractChannelHandlerContext ctx)
/*  486:     */   {
/*  487: 511 */     if ((ctx.channel().isRegistered()) && (!ctx.executor().inEventLoop()))
/*  488:     */     {
/*  489: 512 */       ctx.executor().execute(new Runnable()
/*  490:     */       {
/*  491:     */         public void run()
/*  492:     */         {
/*  493: 515 */           DefaultChannelPipeline.this.callHandlerRemoved0(ctx);
/*  494:     */         }
/*  495: 517 */       });
/*  496: 518 */       return;
/*  497:     */     }
/*  498: 520 */     callHandlerRemoved0(ctx);
/*  499:     */   }
/*  500:     */   
/*  501:     */   private void callHandlerRemoved0(AbstractChannelHandlerContext ctx)
/*  502:     */   {
/*  503:     */     try
/*  504:     */     {
/*  505: 526 */       ctx.handler().handlerRemoved(ctx);
/*  506: 527 */       ctx.setRemoved();
/*  507:     */     }
/*  508:     */     catch (Throwable t)
/*  509:     */     {
/*  510: 529 */       fireExceptionCaught(new ChannelPipelineException(ctx.handler().getClass().getName() + ".handlerRemoved() has thrown an exception.", t));
/*  511:     */     }
/*  512:     */   }
/*  513:     */   
/*  514:     */   private static void waitForFuture(Future<?> future)
/*  515:     */   {
/*  516:     */     try
/*  517:     */     {
/*  518: 551 */       future.get();
/*  519:     */     }
/*  520:     */     catch (ExecutionException ex)
/*  521:     */     {
/*  522: 554 */       PlatformDependent.throwException(ex.getCause());
/*  523:     */     }
/*  524:     */     catch (InterruptedException ex)
/*  525:     */     {
/*  526: 557 */       Thread.currentThread().interrupt();
/*  527:     */     }
/*  528:     */   }
/*  529:     */   
/*  530:     */   public ChannelHandler first()
/*  531:     */   {
/*  532: 563 */     ChannelHandlerContext first = firstContext();
/*  533: 564 */     if (first == null) {
/*  534: 565 */       return null;
/*  535:     */     }
/*  536: 567 */     return first.handler();
/*  537:     */   }
/*  538:     */   
/*  539:     */   public ChannelHandlerContext firstContext()
/*  540:     */   {
/*  541: 572 */     AbstractChannelHandlerContext first = this.head.next;
/*  542: 573 */     if (first == this.tail) {
/*  543: 574 */       return null;
/*  544:     */     }
/*  545: 576 */     return this.head.next;
/*  546:     */   }
/*  547:     */   
/*  548:     */   public ChannelHandler last()
/*  549:     */   {
/*  550: 581 */     AbstractChannelHandlerContext last = this.tail.prev;
/*  551: 582 */     if (last == this.head) {
/*  552: 583 */       return null;
/*  553:     */     }
/*  554: 585 */     return last.handler();
/*  555:     */   }
/*  556:     */   
/*  557:     */   public ChannelHandlerContext lastContext()
/*  558:     */   {
/*  559: 590 */     AbstractChannelHandlerContext last = this.tail.prev;
/*  560: 591 */     if (last == this.head) {
/*  561: 592 */       return null;
/*  562:     */     }
/*  563: 594 */     return last;
/*  564:     */   }
/*  565:     */   
/*  566:     */   public ChannelHandler get(String name)
/*  567:     */   {
/*  568: 599 */     ChannelHandlerContext ctx = context(name);
/*  569: 600 */     if (ctx == null) {
/*  570: 601 */       return null;
/*  571:     */     }
/*  572: 603 */     return ctx.handler();
/*  573:     */   }
/*  574:     */   
/*  575:     */   public <T extends ChannelHandler> T get(Class<T> handlerType)
/*  576:     */   {
/*  577: 610 */     ChannelHandlerContext ctx = context(handlerType);
/*  578: 611 */     if (ctx == null) {
/*  579: 612 */       return null;
/*  580:     */     }
/*  581: 614 */     return ctx.handler();
/*  582:     */   }
/*  583:     */   
/*  584:     */   public ChannelHandlerContext context(String name)
/*  585:     */   {
/*  586: 620 */     if (name == null) {
/*  587: 621 */       throw new NullPointerException("name");
/*  588:     */     }
/*  589: 624 */     synchronized (this)
/*  590:     */     {
/*  591: 625 */       return (ChannelHandlerContext)this.name2ctx.get(name);
/*  592:     */     }
/*  593:     */   }
/*  594:     */   
/*  595:     */   public ChannelHandlerContext context(ChannelHandler handler)
/*  596:     */   {
/*  597: 631 */     if (handler == null) {
/*  598: 632 */       throw new NullPointerException("handler");
/*  599:     */     }
/*  600: 635 */     AbstractChannelHandlerContext ctx = this.head.next;
/*  601:     */     for (;;)
/*  602:     */     {
/*  603: 638 */       if (ctx == null) {
/*  604: 639 */         return null;
/*  605:     */       }
/*  606: 642 */       if (ctx.handler() == handler) {
/*  607: 643 */         return ctx;
/*  608:     */       }
/*  609: 646 */       ctx = ctx.next;
/*  610:     */     }
/*  611:     */   }
/*  612:     */   
/*  613:     */   public ChannelHandlerContext context(Class<? extends ChannelHandler> handlerType)
/*  614:     */   {
/*  615: 652 */     if (handlerType == null) {
/*  616: 653 */       throw new NullPointerException("handlerType");
/*  617:     */     }
/*  618: 656 */     AbstractChannelHandlerContext ctx = this.head.next;
/*  619:     */     for (;;)
/*  620:     */     {
/*  621: 658 */       if (ctx == null) {
/*  622: 659 */         return null;
/*  623:     */       }
/*  624: 661 */       if (handlerType.isAssignableFrom(ctx.handler().getClass())) {
/*  625: 662 */         return ctx;
/*  626:     */       }
/*  627: 664 */       ctx = ctx.next;
/*  628:     */     }
/*  629:     */   }
/*  630:     */   
/*  631:     */   public List<String> names()
/*  632:     */   {
/*  633: 670 */     List<String> list = new ArrayList();
/*  634: 671 */     AbstractChannelHandlerContext ctx = this.head.next;
/*  635:     */     for (;;)
/*  636:     */     {
/*  637: 673 */       if (ctx == null) {
/*  638: 674 */         return list;
/*  639:     */       }
/*  640: 676 */       list.add(ctx.name());
/*  641: 677 */       ctx = ctx.next;
/*  642:     */     }
/*  643:     */   }
/*  644:     */   
/*  645:     */   public Map<String, ChannelHandler> toMap()
/*  646:     */   {
/*  647: 683 */     Map<String, ChannelHandler> map = new LinkedHashMap();
/*  648: 684 */     AbstractChannelHandlerContext ctx = this.head.next;
/*  649:     */     for (;;)
/*  650:     */     {
/*  651: 686 */       if (ctx == this.tail) {
/*  652: 687 */         return map;
/*  653:     */       }
/*  654: 689 */       map.put(ctx.name(), ctx.handler());
/*  655: 690 */       ctx = ctx.next;
/*  656:     */     }
/*  657:     */   }
/*  658:     */   
/*  659:     */   public Iterator<Map.Entry<String, ChannelHandler>> iterator()
/*  660:     */   {
/*  661: 696 */     return toMap().entrySet().iterator();
/*  662:     */   }
/*  663:     */   
/*  664:     */   public String toString()
/*  665:     */   {
/*  666: 704 */     StringBuilder buf = new StringBuilder();
/*  667: 705 */     buf.append(StringUtil.simpleClassName(this));
/*  668: 706 */     buf.append('{');
/*  669: 707 */     AbstractChannelHandlerContext ctx = this.head.next;
/*  670: 709 */     while (ctx != this.tail)
/*  671:     */     {
/*  672: 713 */       buf.append('(');
/*  673: 714 */       buf.append(ctx.name());
/*  674: 715 */       buf.append(" = ");
/*  675: 716 */       buf.append(ctx.handler().getClass().getName());
/*  676: 717 */       buf.append(')');
/*  677:     */       
/*  678: 719 */       ctx = ctx.next;
/*  679: 720 */       if (ctx == this.tail) {
/*  680:     */         break;
/*  681:     */       }
/*  682: 724 */       buf.append(", ");
/*  683:     */     }
/*  684: 726 */     buf.append('}');
/*  685: 727 */     return buf.toString();
/*  686:     */   }
/*  687:     */   
/*  688:     */   public ChannelPipeline fireChannelRegistered()
/*  689:     */   {
/*  690: 732 */     this.head.fireChannelRegistered();
/*  691: 733 */     return this;
/*  692:     */   }
/*  693:     */   
/*  694:     */   public ChannelPipeline fireChannelUnregistered()
/*  695:     */   {
/*  696: 738 */     this.head.fireChannelUnregistered();
/*  697: 741 */     if (!this.channel.isOpen()) {
/*  698: 742 */       teardownAll();
/*  699:     */     }
/*  700: 744 */     return this;
/*  701:     */   }
/*  702:     */   
/*  703:     */   private void teardownAll()
/*  704:     */   {
/*  705: 753 */     this.tail.prev.teardown();
/*  706:     */   }
/*  707:     */   
/*  708:     */   public ChannelPipeline fireChannelActive()
/*  709:     */   {
/*  710: 758 */     this.head.fireChannelActive();
/*  711: 760 */     if (this.channel.config().isAutoRead()) {
/*  712: 761 */       this.channel.read();
/*  713:     */     }
/*  714: 764 */     return this;
/*  715:     */   }
/*  716:     */   
/*  717:     */   public ChannelPipeline fireChannelInactive()
/*  718:     */   {
/*  719: 769 */     this.head.fireChannelInactive();
/*  720: 770 */     return this;
/*  721:     */   }
/*  722:     */   
/*  723:     */   public ChannelPipeline fireExceptionCaught(Throwable cause)
/*  724:     */   {
/*  725: 775 */     this.head.fireExceptionCaught(cause);
/*  726: 776 */     return this;
/*  727:     */   }
/*  728:     */   
/*  729:     */   public ChannelPipeline fireUserEventTriggered(Object event)
/*  730:     */   {
/*  731: 781 */     this.head.fireUserEventTriggered(event);
/*  732: 782 */     return this;
/*  733:     */   }
/*  734:     */   
/*  735:     */   public ChannelPipeline fireChannelRead(Object msg)
/*  736:     */   {
/*  737: 787 */     this.head.fireChannelRead(msg);
/*  738: 788 */     return this;
/*  739:     */   }
/*  740:     */   
/*  741:     */   public ChannelPipeline fireChannelReadComplete()
/*  742:     */   {
/*  743: 793 */     this.head.fireChannelReadComplete();
/*  744: 794 */     if (this.channel.config().isAutoRead()) {
/*  745: 795 */       read();
/*  746:     */     }
/*  747: 797 */     return this;
/*  748:     */   }
/*  749:     */   
/*  750:     */   public ChannelPipeline fireChannelWritabilityChanged()
/*  751:     */   {
/*  752: 802 */     this.head.fireChannelWritabilityChanged();
/*  753: 803 */     return this;
/*  754:     */   }
/*  755:     */   
/*  756:     */   public ChannelFuture bind(SocketAddress localAddress)
/*  757:     */   {
/*  758: 808 */     return this.tail.bind(localAddress);
/*  759:     */   }
/*  760:     */   
/*  761:     */   public ChannelFuture connect(SocketAddress remoteAddress)
/*  762:     */   {
/*  763: 813 */     return this.tail.connect(remoteAddress);
/*  764:     */   }
/*  765:     */   
/*  766:     */   public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress)
/*  767:     */   {
/*  768: 818 */     return this.tail.connect(remoteAddress, localAddress);
/*  769:     */   }
/*  770:     */   
/*  771:     */   public ChannelFuture disconnect()
/*  772:     */   {
/*  773: 823 */     return this.tail.disconnect();
/*  774:     */   }
/*  775:     */   
/*  776:     */   public ChannelFuture close()
/*  777:     */   {
/*  778: 828 */     return this.tail.close();
/*  779:     */   }
/*  780:     */   
/*  781:     */   public ChannelFuture deregister()
/*  782:     */   {
/*  783: 833 */     return this.tail.deregister();
/*  784:     */   }
/*  785:     */   
/*  786:     */   public ChannelPipeline flush()
/*  787:     */   {
/*  788: 838 */     this.tail.flush();
/*  789: 839 */     return this;
/*  790:     */   }
/*  791:     */   
/*  792:     */   public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise)
/*  793:     */   {
/*  794: 844 */     return this.tail.bind(localAddress, promise);
/*  795:     */   }
/*  796:     */   
/*  797:     */   public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise)
/*  798:     */   {
/*  799: 849 */     return this.tail.connect(remoteAddress, promise);
/*  800:     */   }
/*  801:     */   
/*  802:     */   public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/*  803:     */   {
/*  804: 854 */     return this.tail.connect(remoteAddress, localAddress, promise);
/*  805:     */   }
/*  806:     */   
/*  807:     */   public ChannelFuture disconnect(ChannelPromise promise)
/*  808:     */   {
/*  809: 859 */     return this.tail.disconnect(promise);
/*  810:     */   }
/*  811:     */   
/*  812:     */   public ChannelFuture close(ChannelPromise promise)
/*  813:     */   {
/*  814: 864 */     return this.tail.close(promise);
/*  815:     */   }
/*  816:     */   
/*  817:     */   public ChannelFuture deregister(ChannelPromise promise)
/*  818:     */   {
/*  819: 869 */     return this.tail.deregister(promise);
/*  820:     */   }
/*  821:     */   
/*  822:     */   public ChannelPipeline read()
/*  823:     */   {
/*  824: 874 */     this.tail.read();
/*  825: 875 */     return this;
/*  826:     */   }
/*  827:     */   
/*  828:     */   public ChannelFuture write(Object msg)
/*  829:     */   {
/*  830: 880 */     return this.tail.write(msg);
/*  831:     */   }
/*  832:     */   
/*  833:     */   public ChannelFuture write(Object msg, ChannelPromise promise)
/*  834:     */   {
/*  835: 885 */     return this.tail.write(msg, promise);
/*  836:     */   }
/*  837:     */   
/*  838:     */   public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise)
/*  839:     */   {
/*  840: 890 */     return this.tail.writeAndFlush(msg, promise);
/*  841:     */   }
/*  842:     */   
/*  843:     */   public ChannelFuture writeAndFlush(Object msg)
/*  844:     */   {
/*  845: 895 */     return this.tail.writeAndFlush(msg);
/*  846:     */   }
/*  847:     */   
/*  848:     */   private void checkDuplicateName(String name)
/*  849:     */   {
/*  850: 899 */     if (this.name2ctx.containsKey(name)) {
/*  851: 900 */       throw new IllegalArgumentException("Duplicate handler name: " + name);
/*  852:     */     }
/*  853:     */   }
/*  854:     */   
/*  855:     */   private AbstractChannelHandlerContext getContextOrDie(String name)
/*  856:     */   {
/*  857: 905 */     AbstractChannelHandlerContext ctx = (AbstractChannelHandlerContext)context(name);
/*  858: 906 */     if (ctx == null) {
/*  859: 907 */       throw new NoSuchElementException(name);
/*  860:     */     }
/*  861: 909 */     return ctx;
/*  862:     */   }
/*  863:     */   
/*  864:     */   private AbstractChannelHandlerContext getContextOrDie(ChannelHandler handler)
/*  865:     */   {
/*  866: 914 */     AbstractChannelHandlerContext ctx = (AbstractChannelHandlerContext)context(handler);
/*  867: 915 */     if (ctx == null) {
/*  868: 916 */       throw new NoSuchElementException(handler.getClass().getName());
/*  869:     */     }
/*  870: 918 */     return ctx;
/*  871:     */   }
/*  872:     */   
/*  873:     */   private AbstractChannelHandlerContext getContextOrDie(Class<? extends ChannelHandler> handlerType)
/*  874:     */   {
/*  875: 923 */     AbstractChannelHandlerContext ctx = (AbstractChannelHandlerContext)context(handlerType);
/*  876: 924 */     if (ctx == null) {
/*  877: 925 */       throw new NoSuchElementException(handlerType.getName());
/*  878:     */     }
/*  879: 927 */     return ctx;
/*  880:     */   }
/*  881:     */   
/*  882:     */   static final class TailContext
/*  883:     */     extends AbstractChannelHandlerContext
/*  884:     */     implements ChannelInboundHandler
/*  885:     */   {
/*  886: 934 */     private static final String TAIL_NAME = DefaultChannelPipeline.generateName0(TailContext.class);
/*  887:     */     
/*  888:     */     TailContext(DefaultChannelPipeline pipeline)
/*  889:     */     {
/*  890: 937 */       super(null, TAIL_NAME, true, false);
/*  891:     */     }
/*  892:     */     
/*  893:     */     public ChannelHandler handler()
/*  894:     */     {
/*  895: 942 */       return this;
/*  896:     */     }
/*  897:     */     
/*  898:     */     public void channelRegistered(ChannelHandlerContext ctx)
/*  899:     */       throws Exception
/*  900:     */     {}
/*  901:     */     
/*  902:     */     public void channelUnregistered(ChannelHandlerContext ctx)
/*  903:     */       throws Exception
/*  904:     */     {}
/*  905:     */     
/*  906:     */     public void channelActive(ChannelHandlerContext ctx)
/*  907:     */       throws Exception
/*  908:     */     {}
/*  909:     */     
/*  910:     */     public void channelInactive(ChannelHandlerContext ctx)
/*  911:     */       throws Exception
/*  912:     */     {}
/*  913:     */     
/*  914:     */     public void channelWritabilityChanged(ChannelHandlerContext ctx)
/*  915:     */       throws Exception
/*  916:     */     {}
/*  917:     */     
/*  918:     */     public void handlerAdded(ChannelHandlerContext ctx)
/*  919:     */       throws Exception
/*  920:     */     {}
/*  921:     */     
/*  922:     */     public void handlerRemoved(ChannelHandlerContext ctx)
/*  923:     */       throws Exception
/*  924:     */     {}
/*  925:     */     
/*  926:     */     public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
/*  927:     */       throws Exception
/*  928:     */     {}
/*  929:     */     
/*  930:     */     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/*  931:     */       throws Exception
/*  932:     */     {
/*  933: 971 */       DefaultChannelPipeline.logger.warn("An exceptionCaught() event was fired, and it reached at the tail of the pipeline. It usually means the last handler in the pipeline did not handle the exception.", cause);
/*  934:     */     }
/*  935:     */     
/*  936:     */     public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  937:     */       throws Exception
/*  938:     */     {
/*  939:     */       try
/*  940:     */       {
/*  941: 979 */         DefaultChannelPipeline.logger.debug("Discarded inbound message {} that reached at the tail of the pipeline. Please check your pipeline configuration.", msg);
/*  942:     */       }
/*  943:     */       finally
/*  944:     */       {
/*  945: 983 */         ReferenceCountUtil.release(msg);
/*  946:     */       }
/*  947:     */     }
/*  948:     */     
/*  949:     */     public void channelReadComplete(ChannelHandlerContext ctx)
/*  950:     */       throws Exception
/*  951:     */     {}
/*  952:     */   }
/*  953:     */   
/*  954:     */   static final class HeadContext
/*  955:     */     extends AbstractChannelHandlerContext
/*  956:     */     implements ChannelOutboundHandler
/*  957:     */   {
/*  958: 993 */     private static final String HEAD_NAME = DefaultChannelPipeline.generateName0(HeadContext.class);
/*  959:     */     protected final Channel.Unsafe unsafe;
/*  960:     */     
/*  961:     */     HeadContext(DefaultChannelPipeline pipeline)
/*  962:     */     {
/*  963: 998 */       super(null, HEAD_NAME, false, true);
/*  964: 999 */       this.unsafe = pipeline.channel().unsafe();
/*  965:     */     }
/*  966:     */     
/*  967:     */     public ChannelHandler handler()
/*  968:     */     {
/*  969:1004 */       return this;
/*  970:     */     }
/*  971:     */     
/*  972:     */     public void handlerAdded(ChannelHandlerContext ctx)
/*  973:     */       throws Exception
/*  974:     */     {}
/*  975:     */     
/*  976:     */     public void handlerRemoved(ChannelHandlerContext ctx)
/*  977:     */       throws Exception
/*  978:     */     {}
/*  979:     */     
/*  980:     */     public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
/*  981:     */       throws Exception
/*  982:     */     {
/*  983:1021 */       this.unsafe.bind(localAddress, promise);
/*  984:     */     }
/*  985:     */     
/*  986:     */     public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/*  987:     */       throws Exception
/*  988:     */     {
/*  989:1029 */       this.unsafe.connect(remoteAddress, localAddress, promise);
/*  990:     */     }
/*  991:     */     
/*  992:     */     public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/*  993:     */       throws Exception
/*  994:     */     {
/*  995:1034 */       this.unsafe.disconnect(promise);
/*  996:     */     }
/*  997:     */     
/*  998:     */     public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/*  999:     */       throws Exception
/* 1000:     */     {
/* 1001:1039 */       this.unsafe.close(promise);
/* 1002:     */     }
/* 1003:     */     
/* 1004:     */     public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
/* 1005:     */       throws Exception
/* 1006:     */     {
/* 1007:1044 */       this.unsafe.deregister(promise);
/* 1008:     */     }
/* 1009:     */     
/* 1010:     */     public void read(ChannelHandlerContext ctx)
/* 1011:     */     {
/* 1012:1049 */       this.unsafe.beginRead();
/* 1013:     */     }
/* 1014:     */     
/* 1015:     */     public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 1016:     */       throws Exception
/* 1017:     */     {
/* 1018:1054 */       this.unsafe.write(msg, promise);
/* 1019:     */     }
/* 1020:     */     
/* 1021:     */     public void flush(ChannelHandlerContext ctx)
/* 1022:     */       throws Exception
/* 1023:     */     {
/* 1024:1059 */       this.unsafe.flush();
/* 1025:     */     }
/* 1026:     */     
/* 1027:     */     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 1028:     */       throws Exception
/* 1029:     */     {
/* 1030:1064 */       ctx.fireExceptionCaught(cause);
/* 1031:     */     }
/* 1032:     */   }
/* 1033:     */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.DefaultChannelPipeline
 * JD-Core Version:    0.7.0.1
 */