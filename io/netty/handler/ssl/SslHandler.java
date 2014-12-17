/*    1:     */ package io.netty.handler.ssl;
/*    2:     */ 
/*    3:     */ import io.netty.buffer.ByteBuf;
/*    4:     */ import io.netty.buffer.ByteBufAllocator;
/*    5:     */ import io.netty.buffer.ByteBufUtil;
/*    6:     */ import io.netty.buffer.Unpooled;
/*    7:     */ import io.netty.channel.Channel;
/*    8:     */ import io.netty.channel.ChannelException;
/*    9:     */ import io.netty.channel.ChannelFuture;
/*   10:     */ import io.netty.channel.ChannelFutureListener;
/*   11:     */ import io.netty.channel.ChannelHandlerContext;
/*   12:     */ import io.netty.channel.ChannelOutboundHandler;
/*   13:     */ import io.netty.channel.ChannelPromise;
/*   14:     */ import io.netty.handler.codec.ByteToMessageDecoder;
/*   15:     */ import io.netty.util.concurrent.DefaultPromise;
/*   16:     */ import io.netty.util.concurrent.EventExecutor;
/*   17:     */ import io.netty.util.concurrent.Future;
/*   18:     */ import io.netty.util.concurrent.GenericFutureListener;
/*   19:     */ import io.netty.util.concurrent.ImmediateExecutor;
/*   20:     */ import io.netty.util.internal.EmptyArrays;
/*   21:     */ import io.netty.util.internal.PendingWrite;
/*   22:     */ import io.netty.util.internal.PlatformDependent;
/*   23:     */ import io.netty.util.internal.logging.InternalLogger;
/*   24:     */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   25:     */ import java.io.IOException;
/*   26:     */ import java.net.SocketAddress;
/*   27:     */ import java.nio.ByteBuffer;
/*   28:     */ import java.nio.channels.ClosedChannelException;
/*   29:     */ import java.nio.channels.DatagramChannel;
/*   30:     */ import java.nio.channels.SocketChannel;
/*   31:     */ import java.util.ArrayDeque;
/*   32:     */ import java.util.ArrayList;
/*   33:     */ import java.util.Deque;
/*   34:     */ import java.util.List;
/*   35:     */ import java.util.concurrent.CountDownLatch;
/*   36:     */ import java.util.concurrent.Executor;
/*   37:     */ import java.util.concurrent.ScheduledFuture;
/*   38:     */ import java.util.concurrent.TimeUnit;
/*   39:     */ import java.util.regex.Matcher;
/*   40:     */ import java.util.regex.Pattern;
/*   41:     */ import javax.net.ssl.SSLEngine;
/*   42:     */ import javax.net.ssl.SSLEngineResult;
/*   43:     */ import javax.net.ssl.SSLEngineResult.HandshakeStatus;
/*   44:     */ import javax.net.ssl.SSLEngineResult.Status;
/*   45:     */ import javax.net.ssl.SSLException;
/*   46:     */ import javax.net.ssl.SSLSession;
/*   47:     */ 
/*   48:     */ public class SslHandler
/*   49:     */   extends ByteToMessageDecoder
/*   50:     */   implements ChannelOutboundHandler
/*   51:     */ {
/*   52:     */   private static final InternalLogger logger;
/*   53:     */   private static final Pattern IGNORABLE_CLASS_IN_STACK;
/*   54:     */   private static final Pattern IGNORABLE_ERROR_MESSAGE;
/*   55:     */   private static final SSLException SSLENGINE_CLOSED;
/*   56:     */   private static final SSLException HANDSHAKE_TIMED_OUT;
/*   57:     */   private static final ClosedChannelException CHANNEL_CLOSED;
/*   58:     */   private volatile ChannelHandlerContext ctx;
/*   59:     */   private final SSLEngine engine;
/*   60:     */   private final int maxPacketBufferSize;
/*   61:     */   private final Executor delegatedTaskExecutor;
/*   62:     */   private final boolean wantsDirectBuffer;
/*   63:     */   private final boolean wantsLargeOutboundNetworkBuffer;
/*   64:     */   private boolean wantsInboundHeapBuffer;
/*   65:     */   private final boolean startTls;
/*   66:     */   private boolean sentFirstMessage;
/*   67:     */   private boolean flushedBeforeHandshakeDone;
/*   68:     */   
/*   69:     */   static
/*   70:     */   {
/*   71: 161 */     logger = InternalLoggerFactory.getInstance(SslHandler.class);
/*   72:     */     
/*   73:     */ 
/*   74: 164 */     IGNORABLE_CLASS_IN_STACK = Pattern.compile("^.*(?:Socket|Datagram|Sctp|Udt)Channel.*$");
/*   75:     */     
/*   76: 166 */     IGNORABLE_ERROR_MESSAGE = Pattern.compile("^.*(?:connection.*(?:reset|closed|abort|broken)|broken.*pipe).*$", 2);
/*   77:     */     
/*   78:     */ 
/*   79: 169 */     SSLENGINE_CLOSED = new SSLException("SSLEngine closed already");
/*   80: 170 */     HANDSHAKE_TIMED_OUT = new SSLException("handshake timed out");
/*   81: 171 */     CHANNEL_CLOSED = new ClosedChannelException();
/*   82:     */     
/*   83:     */ 
/*   84: 174 */     SSLENGINE_CLOSED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
/*   85: 175 */     HANDSHAKE_TIMED_OUT.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
/*   86: 176 */     CHANNEL_CLOSED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
/*   87:     */   }
/*   88:     */   
/*   89: 210 */   private final LazyChannelPromise handshakePromise = new LazyChannelPromise(null);
/*   90: 211 */   private final LazyChannelPromise sslCloseFuture = new LazyChannelPromise(null);
/*   91: 212 */   private final Deque<PendingWrite> pendingUnencryptedWrites = new ArrayDeque();
/*   92:     */   private boolean needsFlush;
/*   93:     */   private int packetLength;
/*   94: 222 */   private volatile long handshakeTimeoutMillis = 10000L;
/*   95: 223 */   private volatile long closeNotifyTimeoutMillis = 3000L;
/*   96:     */   
/*   97:     */   public SslHandler(SSLEngine engine)
/*   98:     */   {
/*   99: 231 */     this(engine, false);
/*  100:     */   }
/*  101:     */   
/*  102:     */   public SslHandler(SSLEngine engine, boolean startTls)
/*  103:     */   {
/*  104: 243 */     this(engine, startTls, ImmediateExecutor.INSTANCE);
/*  105:     */   }
/*  106:     */   
/*  107:     */   @Deprecated
/*  108:     */   public SslHandler(SSLEngine engine, Executor delegatedTaskExecutor)
/*  109:     */   {
/*  110: 251 */     this(engine, false, delegatedTaskExecutor);
/*  111:     */   }
/*  112:     */   
/*  113:     */   @Deprecated
/*  114:     */   public SslHandler(SSLEngine engine, boolean startTls, Executor delegatedTaskExecutor)
/*  115:     */   {
/*  116: 259 */     if (engine == null) {
/*  117: 260 */       throw new NullPointerException("engine");
/*  118:     */     }
/*  119: 262 */     if (delegatedTaskExecutor == null) {
/*  120: 263 */       throw new NullPointerException("delegatedTaskExecutor");
/*  121:     */     }
/*  122: 265 */     this.engine = engine;
/*  123: 266 */     this.delegatedTaskExecutor = delegatedTaskExecutor;
/*  124: 267 */     this.startTls = startTls;
/*  125: 268 */     this.maxPacketBufferSize = engine.getSession().getPacketBufferSize();
/*  126:     */     
/*  127: 270 */     this.wantsDirectBuffer = (engine instanceof OpenSslEngine);
/*  128: 271 */     this.wantsLargeOutboundNetworkBuffer = (!(engine instanceof OpenSslEngine));
/*  129:     */   }
/*  130:     */   
/*  131:     */   public long getHandshakeTimeoutMillis()
/*  132:     */   {
/*  133: 275 */     return this.handshakeTimeoutMillis;
/*  134:     */   }
/*  135:     */   
/*  136:     */   public void setHandshakeTimeout(long handshakeTimeout, TimeUnit unit)
/*  137:     */   {
/*  138: 279 */     if (unit == null) {
/*  139: 280 */       throw new NullPointerException("unit");
/*  140:     */     }
/*  141: 283 */     setHandshakeTimeoutMillis(unit.toMillis(handshakeTimeout));
/*  142:     */   }
/*  143:     */   
/*  144:     */   public void setHandshakeTimeoutMillis(long handshakeTimeoutMillis)
/*  145:     */   {
/*  146: 287 */     if (handshakeTimeoutMillis < 0L) {
/*  147: 288 */       throw new IllegalArgumentException("handshakeTimeoutMillis: " + handshakeTimeoutMillis + " (expected: >= 0)");
/*  148:     */     }
/*  149: 291 */     this.handshakeTimeoutMillis = handshakeTimeoutMillis;
/*  150:     */   }
/*  151:     */   
/*  152:     */   public long getCloseNotifyTimeoutMillis()
/*  153:     */   {
/*  154: 295 */     return this.closeNotifyTimeoutMillis;
/*  155:     */   }
/*  156:     */   
/*  157:     */   public void setCloseNotifyTimeout(long closeNotifyTimeout, TimeUnit unit)
/*  158:     */   {
/*  159: 299 */     if (unit == null) {
/*  160: 300 */       throw new NullPointerException("unit");
/*  161:     */     }
/*  162: 303 */     setCloseNotifyTimeoutMillis(unit.toMillis(closeNotifyTimeout));
/*  163:     */   }
/*  164:     */   
/*  165:     */   public void setCloseNotifyTimeoutMillis(long closeNotifyTimeoutMillis)
/*  166:     */   {
/*  167: 307 */     if (closeNotifyTimeoutMillis < 0L) {
/*  168: 308 */       throw new IllegalArgumentException("closeNotifyTimeoutMillis: " + closeNotifyTimeoutMillis + " (expected: >= 0)");
/*  169:     */     }
/*  170: 311 */     this.closeNotifyTimeoutMillis = closeNotifyTimeoutMillis;
/*  171:     */   }
/*  172:     */   
/*  173:     */   public SSLEngine engine()
/*  174:     */   {
/*  175: 318 */     return this.engine;
/*  176:     */   }
/*  177:     */   
/*  178:     */   public Future<Channel> handshakeFuture()
/*  179:     */   {
/*  180: 325 */     return this.handshakePromise;
/*  181:     */   }
/*  182:     */   
/*  183:     */   public ChannelFuture close()
/*  184:     */   {
/*  185: 333 */     return close(this.ctx.newPromise());
/*  186:     */   }
/*  187:     */   
/*  188:     */   public ChannelFuture close(final ChannelPromise future)
/*  189:     */   {
/*  190: 340 */     final ChannelHandlerContext ctx = this.ctx;
/*  191: 341 */     ctx.executor().execute(new Runnable()
/*  192:     */     {
/*  193:     */       public void run()
/*  194:     */       {
/*  195: 344 */         SslHandler.this.engine.closeOutbound();
/*  196:     */         try
/*  197:     */         {
/*  198: 346 */           SslHandler.this.write(ctx, Unpooled.EMPTY_BUFFER, future);
/*  199: 347 */           SslHandler.this.flush(ctx);
/*  200:     */         }
/*  201:     */         catch (Exception e)
/*  202:     */         {
/*  203: 349 */           if (!future.tryFailure(e)) {
/*  204: 350 */             SslHandler.logger.warn("flush() raised a masked exception.", e);
/*  205:     */           }
/*  206:     */         }
/*  207:     */       }
/*  208: 355 */     });
/*  209: 356 */     return future;
/*  210:     */   }
/*  211:     */   
/*  212:     */   public Future<Channel> sslCloseFuture()
/*  213:     */   {
/*  214: 368 */     return this.sslCloseFuture;
/*  215:     */   }
/*  216:     */   
/*  217:     */   public void handlerRemoved0(ChannelHandlerContext ctx)
/*  218:     */     throws Exception
/*  219:     */   {
/*  220:     */     for (;;)
/*  221:     */     {
/*  222: 374 */       PendingWrite write = (PendingWrite)this.pendingUnencryptedWrites.poll();
/*  223: 375 */       if (write == null) {
/*  224:     */         break;
/*  225:     */       }
/*  226: 378 */       write.failAndRecycle(new ChannelException("Pending write on removal of SslHandler"));
/*  227:     */     }
/*  228:     */   }
/*  229:     */   
/*  230:     */   public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
/*  231:     */     throws Exception
/*  232:     */   {
/*  233: 384 */     ctx.bind(localAddress, promise);
/*  234:     */   }
/*  235:     */   
/*  236:     */   public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/*  237:     */     throws Exception
/*  238:     */   {
/*  239: 390 */     ctx.connect(remoteAddress, localAddress, promise);
/*  240:     */   }
/*  241:     */   
/*  242:     */   public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
/*  243:     */     throws Exception
/*  244:     */   {
/*  245: 395 */     ctx.deregister(promise);
/*  246:     */   }
/*  247:     */   
/*  248:     */   public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/*  249:     */     throws Exception
/*  250:     */   {
/*  251: 401 */     closeOutboundAndChannel(ctx, promise, true);
/*  252:     */   }
/*  253:     */   
/*  254:     */   public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/*  255:     */     throws Exception
/*  256:     */   {
/*  257: 407 */     closeOutboundAndChannel(ctx, promise, false);
/*  258:     */   }
/*  259:     */   
/*  260:     */   public void read(ChannelHandlerContext ctx)
/*  261:     */   {
/*  262: 412 */     ctx.read();
/*  263:     */   }
/*  264:     */   
/*  265:     */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  266:     */     throws Exception
/*  267:     */   {
/*  268: 417 */     this.pendingUnencryptedWrites.add(PendingWrite.newInstance(msg, promise));
/*  269:     */   }
/*  270:     */   
/*  271:     */   public void flush(ChannelHandlerContext ctx)
/*  272:     */     throws Exception
/*  273:     */   {
/*  274: 424 */     if ((this.startTls) && (!this.sentFirstMessage))
/*  275:     */     {
/*  276: 425 */       this.sentFirstMessage = true;
/*  277:     */       for (;;)
/*  278:     */       {
/*  279: 427 */         PendingWrite pendingWrite = (PendingWrite)this.pendingUnencryptedWrites.poll();
/*  280: 428 */         if (pendingWrite == null) {
/*  281:     */           break;
/*  282:     */         }
/*  283: 431 */         ctx.write(pendingWrite.msg(), (ChannelPromise)pendingWrite.recycleAndGet());
/*  284:     */       }
/*  285: 433 */       ctx.flush();
/*  286: 434 */       return;
/*  287:     */     }
/*  288: 436 */     if (this.pendingUnencryptedWrites.isEmpty()) {
/*  289: 437 */       this.pendingUnencryptedWrites.add(PendingWrite.newInstance(Unpooled.EMPTY_BUFFER, null));
/*  290:     */     }
/*  291: 439 */     if (!this.handshakePromise.isDone()) {
/*  292: 440 */       this.flushedBeforeHandshakeDone = true;
/*  293:     */     }
/*  294: 442 */     wrap(ctx, false);
/*  295: 443 */     ctx.flush();
/*  296:     */   }
/*  297:     */   
/*  298:     */   private void wrap(ChannelHandlerContext ctx, boolean inUnwrap)
/*  299:     */     throws SSLException
/*  300:     */   {
/*  301: 447 */     ByteBuf out = null;
/*  302: 448 */     ChannelPromise promise = null;
/*  303:     */     try
/*  304:     */     {
/*  305:     */       for (;;)
/*  306:     */       {
/*  307: 451 */         PendingWrite pending = (PendingWrite)this.pendingUnencryptedWrites.peek();
/*  308: 452 */         if (pending == null) {
/*  309:     */           break;
/*  310:     */         }
/*  311: 456 */         if (!(pending.msg() instanceof ByteBuf))
/*  312:     */         {
/*  313: 457 */           ctx.write(pending.msg(), (ChannelPromise)pending.recycleAndGet());
/*  314: 458 */           this.pendingUnencryptedWrites.remove();
/*  315:     */         }
/*  316:     */         else
/*  317:     */         {
/*  318: 462 */           ByteBuf buf = (ByteBuf)pending.msg();
/*  319: 463 */           if (out == null) {
/*  320: 464 */             out = allocateOutNetBuf(ctx, buf.readableBytes());
/*  321:     */           }
/*  322: 467 */           SSLEngineResult result = wrap(this.engine, buf, out);
/*  323: 469 */           if (!buf.isReadable())
/*  324:     */           {
/*  325: 470 */             buf.release();
/*  326: 471 */             promise = (ChannelPromise)pending.recycleAndGet();
/*  327: 472 */             this.pendingUnencryptedWrites.remove();
/*  328:     */           }
/*  329:     */           else
/*  330:     */           {
/*  331: 474 */             promise = null;
/*  332:     */           }
/*  333: 477 */           if (result.getStatus() == SSLEngineResult.Status.CLOSED)
/*  334:     */           {
/*  335:     */             for (;;)
/*  336:     */             {
/*  337: 481 */               PendingWrite w = (PendingWrite)this.pendingUnencryptedWrites.poll();
/*  338: 482 */               if (w == null) {
/*  339:     */                 break;
/*  340:     */               }
/*  341: 485 */               w.failAndRecycle(SSLENGINE_CLOSED);
/*  342:     */             }
/*  343:     */             return;
/*  344:     */           }
/*  345: 489 */           switch (8.$SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[result.getHandshakeStatus().ordinal()])
/*  346:     */           {
/*  347:     */           case 1: 
/*  348: 491 */             runDelegatedTasks();
/*  349: 492 */             break;
/*  350:     */           case 2: 
/*  351: 494 */             setHandshakeSuccess();
/*  352:     */           case 3: 
/*  353: 497 */             setHandshakeSuccessIfStillHandshaking();
/*  354:     */           case 4: 
/*  355: 500 */             finishWrap(ctx, out, promise, inUnwrap);
/*  356: 501 */             promise = null;
/*  357: 502 */             out = null;
/*  358: 503 */             break;
/*  359:     */           case 5: 
/*  360:     */             return;
/*  361:     */           default: 
/*  362: 507 */             throw new IllegalStateException("Unknown handshake status: " + result.getHandshakeStatus());
/*  363:     */           }
/*  364:     */         }
/*  365:     */       }
/*  366:     */     }
/*  367:     */     catch (SSLException e)
/*  368:     */     {
/*  369: 513 */       setHandshakeFailure(e);
/*  370: 514 */       throw e;
/*  371:     */     }
/*  372:     */     finally
/*  373:     */     {
/*  374: 516 */       finishWrap(ctx, out, promise, inUnwrap);
/*  375:     */     }
/*  376:     */   }
/*  377:     */   
/*  378:     */   private void finishWrap(ChannelHandlerContext ctx, ByteBuf out, ChannelPromise promise, boolean inUnwrap)
/*  379:     */   {
/*  380: 521 */     if (out == null)
/*  381:     */     {
/*  382: 522 */       out = Unpooled.EMPTY_BUFFER;
/*  383:     */     }
/*  384: 523 */     else if (!out.isReadable())
/*  385:     */     {
/*  386: 524 */       out.release();
/*  387: 525 */       out = Unpooled.EMPTY_BUFFER;
/*  388:     */     }
/*  389: 528 */     if (promise != null) {
/*  390: 529 */       ctx.write(out, promise);
/*  391:     */     } else {
/*  392: 531 */       ctx.write(out);
/*  393:     */     }
/*  394: 534 */     if (inUnwrap) {
/*  395: 535 */       this.needsFlush = true;
/*  396:     */     }
/*  397:     */   }
/*  398:     */   
/*  399:     */   private void wrapNonAppData(ChannelHandlerContext ctx, boolean inUnwrap)
/*  400:     */     throws SSLException
/*  401:     */   {
/*  402: 540 */     ByteBuf out = null;
/*  403:     */     try
/*  404:     */     {
/*  405:     */       for (;;)
/*  406:     */       {
/*  407: 543 */         if (out == null) {
/*  408: 544 */           out = allocateOutNetBuf(ctx, 0);
/*  409:     */         }
/*  410: 546 */         SSLEngineResult result = wrap(this.engine, Unpooled.EMPTY_BUFFER, out);
/*  411: 548 */         if (result.bytesProduced() > 0)
/*  412:     */         {
/*  413: 549 */           ctx.write(out);
/*  414: 550 */           if (inUnwrap) {
/*  415: 551 */             this.needsFlush = true;
/*  416:     */           }
/*  417: 553 */           out = null;
/*  418:     */         }
/*  419: 556 */         switch (8.$SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[result.getHandshakeStatus().ordinal()])
/*  420:     */         {
/*  421:     */         case 2: 
/*  422: 558 */           setHandshakeSuccess();
/*  423: 559 */           break;
/*  424:     */         case 1: 
/*  425: 561 */           runDelegatedTasks();
/*  426: 562 */           break;
/*  427:     */         case 5: 
/*  428: 564 */           if (!inUnwrap) {
/*  429: 565 */             unwrapNonAppData(ctx);
/*  430:     */           }
/*  431:     */           break;
/*  432:     */         case 4: 
/*  433:     */           break;
/*  434:     */         case 3: 
/*  435: 571 */           setHandshakeSuccessIfStillHandshaking();
/*  436: 574 */           if (!inUnwrap) {
/*  437: 575 */             unwrapNonAppData(ctx);
/*  438:     */           }
/*  439:     */           break;
/*  440:     */         default: 
/*  441: 579 */           throw new IllegalStateException("Unknown handshake status: " + result.getHandshakeStatus());
/*  442:     */         }
/*  443: 582 */         if (result.bytesProduced() == 0) {
/*  444:     */           break;
/*  445:     */         }
/*  446:     */       }
/*  447:     */     }
/*  448:     */     catch (SSLException e)
/*  449:     */     {
/*  450: 587 */       setHandshakeFailure(e);
/*  451: 588 */       throw e;
/*  452:     */     }
/*  453:     */     finally
/*  454:     */     {
/*  455: 590 */       if (out != null) {
/*  456: 591 */         out.release();
/*  457:     */       }
/*  458:     */     }
/*  459:     */   }
/*  460:     */   
/*  461:     */   private SSLEngineResult wrap(SSLEngine engine, ByteBuf in, ByteBuf out)
/*  462:     */     throws SSLException
/*  463:     */   {
/*  464: 597 */     ByteBuffer in0 = in.nioBuffer();
/*  465: 598 */     if (!in0.isDirect())
/*  466:     */     {
/*  467: 599 */       ByteBuffer newIn0 = ByteBuffer.allocateDirect(in0.remaining());
/*  468: 600 */       newIn0.put(in0).flip();
/*  469: 601 */       in0 = newIn0;
/*  470:     */     }
/*  471:     */     for (;;)
/*  472:     */     {
/*  473: 605 */       ByteBuffer out0 = out.nioBuffer(out.writerIndex(), out.writableBytes());
/*  474: 606 */       SSLEngineResult result = engine.wrap(in0, out0);
/*  475: 607 */       in.skipBytes(result.bytesConsumed());
/*  476: 608 */       out.writerIndex(out.writerIndex() + result.bytesProduced());
/*  477: 610 */       switch (result.getStatus())
/*  478:     */       {
/*  479:     */       case BUFFER_OVERFLOW: 
/*  480: 612 */         out.ensureWritable(this.maxPacketBufferSize);
/*  481: 613 */         break;
/*  482:     */       default: 
/*  483: 615 */         return result;
/*  484:     */       }
/*  485:     */     }
/*  486:     */   }
/*  487:     */   
/*  488:     */   public void channelInactive(ChannelHandlerContext ctx)
/*  489:     */     throws Exception
/*  490:     */   {
/*  491: 624 */     setHandshakeFailure(CHANNEL_CLOSED);
/*  492: 625 */     super.channelInactive(ctx);
/*  493:     */   }
/*  494:     */   
/*  495:     */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/*  496:     */     throws Exception
/*  497:     */   {
/*  498: 630 */     if (ignoreException(cause))
/*  499:     */     {
/*  500: 633 */       if (logger.isDebugEnabled()) {
/*  501: 634 */         logger.debug("Swallowing a harmless 'connection reset by peer / broken pipe' error that occurred while writing close_notify in response to the peer's close_notify", cause);
/*  502:     */       }
/*  503: 641 */       if (ctx.channel().isActive()) {
/*  504: 642 */         ctx.close();
/*  505:     */       }
/*  506:     */     }
/*  507:     */     else
/*  508:     */     {
/*  509: 645 */       ctx.fireExceptionCaught(cause);
/*  510:     */     }
/*  511:     */   }
/*  512:     */   
/*  513:     */   private boolean ignoreException(Throwable t)
/*  514:     */   {
/*  515: 659 */     if ((!(t instanceof SSLException)) && ((t instanceof IOException)) && (this.sslCloseFuture.isDone()))
/*  516:     */     {
/*  517: 660 */       String message = String.valueOf(t.getMessage()).toLowerCase();
/*  518: 664 */       if (IGNORABLE_ERROR_MESSAGE.matcher(message).matches()) {
/*  519: 665 */         return true;
/*  520:     */       }
/*  521: 669 */       StackTraceElement[] elements = t.getStackTrace();
/*  522: 670 */       for (StackTraceElement element : elements)
/*  523:     */       {
/*  524: 671 */         String classname = element.getClassName();
/*  525: 672 */         String methodname = element.getMethodName();
/*  526: 675 */         if (!classname.startsWith("io.netty.")) {
/*  527: 680 */           if ("read".equals(methodname))
/*  528:     */           {
/*  529: 686 */             if (IGNORABLE_CLASS_IN_STACK.matcher(classname).matches()) {
/*  530: 687 */               return true;
/*  531:     */             }
/*  532:     */             try
/*  533:     */             {
/*  534: 694 */               Class<?> clazz = PlatformDependent.getClassLoader(getClass()).loadClass(classname);
/*  535: 696 */               if ((SocketChannel.class.isAssignableFrom(clazz)) || (DatagramChannel.class.isAssignableFrom(clazz))) {
/*  536: 698 */                 return true;
/*  537:     */               }
/*  538: 702 */               if ((PlatformDependent.javaVersion() >= 7) && ("com.sun.nio.sctp.SctpChannel".equals(clazz.getSuperclass().getName()))) {
/*  539: 704 */                 return true;
/*  540:     */               }
/*  541:     */             }
/*  542:     */             catch (ClassNotFoundException e) {}
/*  543:     */           }
/*  544:     */         }
/*  545:     */       }
/*  546:     */     }
/*  547: 712 */     return false;
/*  548:     */   }
/*  549:     */   
/*  550:     */   public static boolean isEncrypted(ByteBuf buffer)
/*  551:     */   {
/*  552: 728 */     if (buffer.readableBytes() < 5) {
/*  553: 729 */       throw new IllegalArgumentException("buffer must have at least 5 readable bytes");
/*  554:     */     }
/*  555: 731 */     return getEncryptedPacketLength(buffer, buffer.readerIndex()) != -1;
/*  556:     */   }
/*  557:     */   
/*  558:     */   private static int getEncryptedPacketLength(ByteBuf buffer, int offset)
/*  559:     */   {
/*  560: 748 */     int packetLength = 0;
/*  561:     */     boolean tls;
/*  562: 752 */     switch (buffer.getUnsignedByte(offset))
/*  563:     */     {
/*  564:     */     case 20: 
/*  565:     */     case 21: 
/*  566:     */     case 22: 
/*  567:     */     case 23: 
/*  568: 757 */       tls = true;
/*  569: 758 */       break;
/*  570:     */     default: 
/*  571: 761 */       tls = false;
/*  572:     */     }
/*  573: 764 */     if (tls)
/*  574:     */     {
/*  575: 766 */       int majorVersion = buffer.getUnsignedByte(offset + 1);
/*  576: 767 */       if (majorVersion == 3)
/*  577:     */       {
/*  578: 769 */         packetLength = buffer.getUnsignedShort(offset + 3) + 5;
/*  579: 770 */         if (packetLength <= 5) {
/*  580: 772 */           tls = false;
/*  581:     */         }
/*  582:     */       }
/*  583:     */       else
/*  584:     */       {
/*  585: 776 */         tls = false;
/*  586:     */       }
/*  587:     */     }
/*  588: 780 */     if (!tls)
/*  589:     */     {
/*  590: 782 */       boolean sslv2 = true;
/*  591: 783 */       int headerLength = (buffer.getUnsignedByte(offset) & 0x80) != 0 ? 2 : 3;
/*  592: 784 */       int majorVersion = buffer.getUnsignedByte(offset + headerLength + 1);
/*  593: 785 */       if ((majorVersion == 2) || (majorVersion == 3))
/*  594:     */       {
/*  595: 787 */         if (headerLength == 2) {
/*  596: 788 */           packetLength = (buffer.getShort(offset) & 0x7FFF) + 2;
/*  597:     */         } else {
/*  598: 790 */           packetLength = (buffer.getShort(offset) & 0x3FFF) + 3;
/*  599:     */         }
/*  600: 792 */         if (packetLength <= headerLength) {
/*  601: 793 */           sslv2 = false;
/*  602:     */         }
/*  603:     */       }
/*  604:     */       else
/*  605:     */       {
/*  606: 796 */         sslv2 = false;
/*  607:     */       }
/*  608: 799 */       if (!sslv2) {
/*  609: 800 */         return -1;
/*  610:     */       }
/*  611:     */     }
/*  612: 803 */     return packetLength;
/*  613:     */   }
/*  614:     */   
/*  615:     */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  616:     */     throws SSLException
/*  617:     */   {
/*  618: 809 */     int startOffset = in.readerIndex();
/*  619: 810 */     int endOffset = in.writerIndex();
/*  620: 811 */     int offset = startOffset;
/*  621: 812 */     int totalLength = 0;
/*  622: 815 */     if (this.packetLength > 0)
/*  623:     */     {
/*  624: 816 */       if (endOffset - startOffset < this.packetLength) {
/*  625: 817 */         return;
/*  626:     */       }
/*  627: 819 */       offset += this.packetLength;
/*  628: 820 */       totalLength = this.packetLength;
/*  629: 821 */       this.packetLength = 0;
/*  630:     */     }
/*  631: 825 */     boolean nonSslRecord = false;
/*  632: 827 */     while (totalLength < 18713)
/*  633:     */     {
/*  634: 828 */       int readableBytes = endOffset - offset;
/*  635: 829 */       if (readableBytes < 5) {
/*  636:     */         break;
/*  637:     */       }
/*  638: 833 */       int packetLength = getEncryptedPacketLength(in, offset);
/*  639: 834 */       if (packetLength == -1)
/*  640:     */       {
/*  641: 835 */         nonSslRecord = true;
/*  642: 836 */         break;
/*  643:     */       }
/*  644: 839 */       assert (packetLength > 0);
/*  645: 841 */       if (packetLength > readableBytes)
/*  646:     */       {
/*  647: 843 */         this.packetLength = packetLength;
/*  648: 844 */         break;
/*  649:     */       }
/*  650: 847 */       int newTotalLength = totalLength + packetLength;
/*  651: 848 */       if (newTotalLength > 18713) {
/*  652:     */         break;
/*  653:     */       }
/*  654: 855 */       offset += packetLength;
/*  655: 856 */       totalLength = newTotalLength;
/*  656:     */     }
/*  657: 859 */     if (totalLength > 0)
/*  658:     */     {
/*  659: 871 */       in.skipBytes(totalLength);
/*  660: 872 */       ByteBuffer inNetBuf = in.nioBuffer(startOffset, totalLength);
/*  661: 873 */       unwrap(ctx, inNetBuf, totalLength);
/*  662: 874 */       assert ((!inNetBuf.hasRemaining()) || (this.engine.isInboundDone()));
/*  663:     */     }
/*  664: 877 */     if (nonSslRecord)
/*  665:     */     {
/*  666: 879 */       NotSslRecordException e = new NotSslRecordException("not an SSL/TLS record: " + ByteBufUtil.hexDump(in));
/*  667:     */       
/*  668: 881 */       in.skipBytes(in.readableBytes());
/*  669: 882 */       ctx.fireExceptionCaught(e);
/*  670: 883 */       setHandshakeFailure(e);
/*  671:     */     }
/*  672:     */   }
/*  673:     */   
/*  674:     */   public void channelReadComplete(ChannelHandlerContext ctx)
/*  675:     */     throws Exception
/*  676:     */   {
/*  677: 889 */     if (this.needsFlush)
/*  678:     */     {
/*  679: 890 */       this.needsFlush = false;
/*  680: 891 */       ctx.flush();
/*  681:     */     }
/*  682: 893 */     super.channelReadComplete(ctx);
/*  683:     */   }
/*  684:     */   
/*  685:     */   private void unwrapNonAppData(ChannelHandlerContext ctx)
/*  686:     */     throws SSLException
/*  687:     */   {
/*  688: 900 */     unwrap(ctx, Unpooled.EMPTY_BUFFER.nioBuffer(), 0);
/*  689:     */   }
/*  690:     */   
/*  691:     */   private void unwrap(ChannelHandlerContext ctx, ByteBuffer packet, int initialOutAppBufCapacity)
/*  692:     */     throws SSLException
/*  693:     */   {
/*  694: 912 */     int oldPos = packet.position();
/*  695:     */     ByteBuffer oldPacket;
/*  696:     */     ByteBuf newPacket;
/*  697: 913 */     if ((this.wantsInboundHeapBuffer) && (packet.isDirect()))
/*  698:     */     {
/*  699: 914 */       ByteBuf newPacket = ctx.alloc().heapBuffer(packet.limit() - oldPos);
/*  700: 915 */       newPacket.writeBytes(packet);
/*  701: 916 */       ByteBuffer oldPacket = packet;
/*  702: 917 */       packet = newPacket.nioBuffer();
/*  703:     */     }
/*  704:     */     else
/*  705:     */     {
/*  706: 919 */       oldPacket = null;
/*  707: 920 */       newPacket = null;
/*  708:     */     }
/*  709: 923 */     boolean wrapLater = false;
/*  710: 924 */     ByteBuf decodeOut = allocate(ctx, initialOutAppBufCapacity);
/*  711:     */     try
/*  712:     */     {
/*  713:     */       for (;;)
/*  714:     */       {
/*  715: 927 */         SSLEngineResult result = unwrap(this.engine, packet, decodeOut);
/*  716: 928 */         SSLEngineResult.Status status = result.getStatus();
/*  717: 929 */         SSLEngineResult.HandshakeStatus handshakeStatus = result.getHandshakeStatus();
/*  718: 930 */         int produced = result.bytesProduced();
/*  719: 931 */         int consumed = result.bytesConsumed();
/*  720: 933 */         if (status == SSLEngineResult.Status.CLOSED) {
/*  721: 935 */           this.sslCloseFuture.trySuccess(ctx.channel());
/*  722:     */         } else {
/*  723: 939 */           switch (8.$SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[handshakeStatus.ordinal()])
/*  724:     */           {
/*  725:     */           case 5: 
/*  726:     */             break;
/*  727:     */           case 4: 
/*  728: 943 */             wrapNonAppData(ctx, true);
/*  729: 944 */             break;
/*  730:     */           case 1: 
/*  731: 946 */             runDelegatedTasks();
/*  732: 947 */             break;
/*  733:     */           case 2: 
/*  734: 949 */             setHandshakeSuccess();
/*  735: 950 */             wrapLater = true;
/*  736: 951 */             break;
/*  737:     */           case 3: 
/*  738: 953 */             if (setHandshakeSuccessIfStillHandshaking())
/*  739:     */             {
/*  740: 954 */               wrapLater = true;
/*  741:     */             }
/*  742: 957 */             else if (this.flushedBeforeHandshakeDone)
/*  743:     */             {
/*  744: 961 */               this.flushedBeforeHandshakeDone = false;
/*  745: 962 */               wrapLater = true;
/*  746:     */             }
/*  747:     */             break;
/*  748:     */           default: 
/*  749: 967 */             throw new IllegalStateException("Unknown handshake status: " + handshakeStatus);
/*  750: 970 */             if ((status == SSLEngineResult.Status.BUFFER_UNDERFLOW) || ((consumed == 0) && (produced == 0))) {
/*  751:     */               break label296;
/*  752:     */             }
/*  753:     */           }
/*  754:     */         }
/*  755:     */       }
/*  756:     */       label296:
/*  757: 975 */       if (wrapLater) {
/*  758: 976 */         wrap(ctx, true);
/*  759:     */       }
/*  760:     */     }
/*  761:     */     catch (SSLException e)
/*  762:     */     {
/*  763: 979 */       setHandshakeFailure(e);
/*  764: 980 */       throw e;
/*  765:     */     }
/*  766:     */     finally
/*  767:     */     {
/*  768: 984 */       if (newPacket != null)
/*  769:     */       {
/*  770: 985 */         oldPacket.position(oldPos + packet.position());
/*  771: 986 */         newPacket.release();
/*  772:     */       }
/*  773: 989 */       if (decodeOut.isReadable()) {
/*  774: 990 */         ctx.fireChannelRead(decodeOut);
/*  775:     */       } else {
/*  776: 992 */         decodeOut.release();
/*  777:     */       }
/*  778:     */     }
/*  779:     */   }
/*  780:     */   
/*  781:     */   private static SSLEngineResult unwrap(SSLEngine engine, ByteBuffer in, ByteBuf out)
/*  782:     */     throws SSLException
/*  783:     */   {
/*  784: 998 */     int overflows = 0;
/*  785:     */     for (;;)
/*  786:     */     {
/*  787:1000 */       ByteBuffer out0 = out.nioBuffer(out.writerIndex(), out.writableBytes());
/*  788:1001 */       SSLEngineResult result = engine.unwrap(in, out0);
/*  789:1002 */       out.writerIndex(out.writerIndex() + result.bytesProduced());
/*  790:1003 */       switch (result.getStatus())
/*  791:     */       {
/*  792:     */       case BUFFER_OVERFLOW: 
/*  793:1005 */         int max = engine.getSession().getApplicationBufferSize();
/*  794:1006 */         switch (overflows++)
/*  795:     */         {
/*  796:     */         case 0: 
/*  797:1008 */           out.ensureWritable(Math.min(max, in.remaining()));
/*  798:1009 */           break;
/*  799:     */         default: 
/*  800:1011 */           out.ensureWritable(max);
/*  801:     */         }
/*  802:1013 */         break;
/*  803:     */       default: 
/*  804:1015 */         return result;
/*  805:     */       }
/*  806:     */     }
/*  807:     */   }
/*  808:     */   
/*  809:     */   private void runDelegatedTasks()
/*  810:     */   {
/*  811:1027 */     if (this.delegatedTaskExecutor == ImmediateExecutor.INSTANCE) {
/*  812:     */       for (;;)
/*  813:     */       {
/*  814:1029 */         Runnable task = this.engine.getDelegatedTask();
/*  815:1030 */         if (task == null) {
/*  816:     */           break;
/*  817:     */         }
/*  818:1034 */         task.run();
/*  819:     */       }
/*  820:     */     }
/*  821:1037 */     final List<Runnable> tasks = new ArrayList(2);
/*  822:     */     for (;;)
/*  823:     */     {
/*  824:1039 */       Runnable task = this.engine.getDelegatedTask();
/*  825:1040 */       if (task == null) {
/*  826:     */         break;
/*  827:     */       }
/*  828:1044 */       tasks.add(task);
/*  829:     */     }
/*  830:1047 */     if (tasks.isEmpty()) {
/*  831:1048 */       return;
/*  832:     */     }
/*  833:1051 */     final CountDownLatch latch = new CountDownLatch(1);
/*  834:1052 */     this.delegatedTaskExecutor.execute(new Runnable()
/*  835:     */     {
/*  836:     */       public void run()
/*  837:     */       {
/*  838:     */         try
/*  839:     */         {
/*  840:1056 */           for (Runnable task : tasks) {
/*  841:1057 */             task.run();
/*  842:     */           }
/*  843:     */         }
/*  844:     */         catch (Exception e)
/*  845:     */         {
/*  846:1060 */           SslHandler.this.ctx.fireExceptionCaught(e);
/*  847:     */         }
/*  848:     */         finally
/*  849:     */         {
/*  850:1062 */           latch.countDown();
/*  851:     */         }
/*  852:     */       }
/*  853:1066 */     });
/*  854:1067 */     boolean interrupted = false;
/*  855:1068 */     while (latch.getCount() != 0L) {
/*  856:     */       try
/*  857:     */       {
/*  858:1070 */         latch.await();
/*  859:     */       }
/*  860:     */       catch (InterruptedException e)
/*  861:     */       {
/*  862:1073 */         interrupted = true;
/*  863:     */       }
/*  864:     */     }
/*  865:1077 */     if (interrupted) {
/*  866:1078 */       Thread.currentThread().interrupt();
/*  867:     */     }
/*  868:     */   }
/*  869:     */   
/*  870:     */   private boolean setHandshakeSuccessIfStillHandshaking()
/*  871:     */   {
/*  872:1091 */     if (!this.handshakePromise.isDone())
/*  873:     */     {
/*  874:1092 */       setHandshakeSuccess();
/*  875:1093 */       return true;
/*  876:     */     }
/*  877:1095 */     return false;
/*  878:     */   }
/*  879:     */   
/*  880:     */   private void setHandshakeSuccess()
/*  881:     */   {
/*  882:1103 */     String cipherSuite = String.valueOf(this.engine.getSession().getCipherSuite());
/*  883:1104 */     if ((!this.wantsDirectBuffer) && ((cipherSuite.contains("_GCM_")) || (cipherSuite.contains("-GCM-")))) {
/*  884:1105 */       this.wantsInboundHeapBuffer = true;
/*  885:     */     }
/*  886:1108 */     if (this.handshakePromise.trySuccess(this.ctx.channel()))
/*  887:     */     {
/*  888:1109 */       if (logger.isDebugEnabled()) {
/*  889:1110 */         logger.debug(this.ctx.channel() + " HANDSHAKEN: " + this.engine.getSession().getCipherSuite());
/*  890:     */       }
/*  891:1112 */       this.ctx.fireUserEventTriggered(SslHandshakeCompletionEvent.SUCCESS);
/*  892:     */     }
/*  893:     */   }
/*  894:     */   
/*  895:     */   private void setHandshakeFailure(Throwable cause)
/*  896:     */   {
/*  897:1122 */     this.engine.closeOutbound();
/*  898:     */     try
/*  899:     */     {
/*  900:1125 */       this.engine.closeInbound();
/*  901:     */     }
/*  902:     */     catch (SSLException e)
/*  903:     */     {
/*  904:1131 */       String msg = e.getMessage();
/*  905:1132 */       if ((msg == null) || (!msg.contains("possible truncation attack"))) {
/*  906:1133 */         logger.debug("SSLEngine.closeInbound() raised an exception.", e);
/*  907:     */       }
/*  908:     */     }
/*  909:1136 */     notifyHandshakeFailure(cause);
/*  910:     */     for (;;)
/*  911:     */     {
/*  912:1138 */       PendingWrite write = (PendingWrite)this.pendingUnencryptedWrites.poll();
/*  913:1139 */       if (write == null) {
/*  914:     */         break;
/*  915:     */       }
/*  916:1142 */       write.failAndRecycle(cause);
/*  917:     */     }
/*  918:     */   }
/*  919:     */   
/*  920:     */   private void notifyHandshakeFailure(Throwable cause)
/*  921:     */   {
/*  922:1147 */     if (this.handshakePromise.tryFailure(cause))
/*  923:     */     {
/*  924:1148 */       this.ctx.fireUserEventTriggered(new SslHandshakeCompletionEvent(cause));
/*  925:1149 */       this.ctx.close();
/*  926:     */     }
/*  927:     */   }
/*  928:     */   
/*  929:     */   private void closeOutboundAndChannel(ChannelHandlerContext ctx, ChannelPromise promise, boolean disconnect)
/*  930:     */     throws Exception
/*  931:     */   {
/*  932:1155 */     if (!ctx.channel().isActive())
/*  933:     */     {
/*  934:1156 */       if (disconnect) {
/*  935:1157 */         ctx.disconnect(promise);
/*  936:     */       } else {
/*  937:1159 */         ctx.close(promise);
/*  938:     */       }
/*  939:1161 */       return;
/*  940:     */     }
/*  941:1164 */     this.engine.closeOutbound();
/*  942:     */     
/*  943:1166 */     ChannelPromise closeNotifyFuture = ctx.newPromise();
/*  944:1167 */     write(ctx, Unpooled.EMPTY_BUFFER, closeNotifyFuture);
/*  945:1168 */     flush(ctx);
/*  946:1169 */     safeClose(ctx, closeNotifyFuture, promise);
/*  947:     */   }
/*  948:     */   
/*  949:     */   public void handlerAdded(ChannelHandlerContext ctx)
/*  950:     */     throws Exception
/*  951:     */   {
/*  952:1174 */     this.ctx = ctx;
/*  953:1176 */     if ((ctx.channel().isActive()) && (this.engine.getUseClientMode())) {
/*  954:1179 */       handshake();
/*  955:     */     }
/*  956:     */   }
/*  957:     */   
/*  958:     */   private Future<Channel> handshake()
/*  959:     */   {
/*  960:     */     ScheduledFuture<?> timeoutFuture;
/*  961:     */     final ScheduledFuture<?> timeoutFuture;
/*  962:1188 */     if (this.handshakeTimeoutMillis > 0L) {
/*  963:1189 */       timeoutFuture = this.ctx.executor().schedule(new Runnable()
/*  964:     */       {
/*  965:     */         public void run()
/*  966:     */         {
/*  967:1192 */           if (SslHandler.this.handshakePromise.isDone()) {
/*  968:1193 */             return;
/*  969:     */           }
/*  970:1195 */           SslHandler.this.notifyHandshakeFailure(SslHandler.HANDSHAKE_TIMED_OUT);
/*  971:     */         }
/*  972:1195 */       }, this.handshakeTimeoutMillis, TimeUnit.MILLISECONDS);
/*  973:     */     } else {
/*  974:1199 */       timeoutFuture = null;
/*  975:     */     }
/*  976:1202 */     this.handshakePromise.addListener(new GenericFutureListener()
/*  977:     */     {
/*  978:     */       public void operationComplete(Future<Channel> f)
/*  979:     */         throws Exception
/*  980:     */       {
/*  981:1205 */         if (timeoutFuture != null) {
/*  982:1206 */           timeoutFuture.cancel(false);
/*  983:     */         }
/*  984:     */       }
/*  985:     */     });
/*  986:     */     try
/*  987:     */     {
/*  988:1211 */       this.engine.beginHandshake();
/*  989:1212 */       wrapNonAppData(this.ctx, false);
/*  990:1213 */       this.ctx.flush();
/*  991:     */     }
/*  992:     */     catch (Exception e)
/*  993:     */     {
/*  994:1215 */       notifyHandshakeFailure(e);
/*  995:     */     }
/*  996:1217 */     return this.handshakePromise;
/*  997:     */   }
/*  998:     */   
/*  999:     */   public void channelActive(final ChannelHandlerContext ctx)
/* 1000:     */     throws Exception
/* 1001:     */   {
/* 1002:1225 */     if ((!this.startTls) && (this.engine.getUseClientMode())) {
/* 1003:1228 */       handshake().addListener(new GenericFutureListener()
/* 1004:     */       {
/* 1005:     */         public void operationComplete(Future<Channel> future)
/* 1006:     */           throws Exception
/* 1007:     */         {
/* 1008:1231 */           if (!future.isSuccess())
/* 1009:     */           {
/* 1010:1232 */             SslHandler.logger.debug("Failed to complete handshake", future.cause());
/* 1011:1233 */             ctx.close();
/* 1012:     */           }
/* 1013:     */         }
/* 1014:     */       });
/* 1015:     */     }
/* 1016:1238 */     ctx.fireChannelActive();
/* 1017:     */   }
/* 1018:     */   
/* 1019:     */   private void safeClose(final ChannelHandlerContext ctx, ChannelFuture flushFuture, final ChannelPromise promise)
/* 1020:     */   {
/* 1021:1244 */     if (!ctx.channel().isActive())
/* 1022:     */     {
/* 1023:1245 */       ctx.close(promise); return;
/* 1024:     */     }
/* 1025:     */     ScheduledFuture<?> timeoutFuture;
/* 1026:     */     final ScheduledFuture<?> timeoutFuture;
/* 1027:1250 */     if (this.closeNotifyTimeoutMillis > 0L) {
/* 1028:1252 */       timeoutFuture = ctx.executor().schedule(new Runnable()
/* 1029:     */       {
/* 1030:     */         public void run()
/* 1031:     */         {
/* 1032:1255 */           SslHandler.logger.warn(ctx.channel() + " last write attempt timed out." + " Force-closing the connection.");
/* 1033:     */           
/* 1034:     */ 
/* 1035:1258 */           ctx.close(promise);
/* 1036:     */         }
/* 1037:1258 */       }, this.closeNotifyTimeoutMillis, TimeUnit.MILLISECONDS);
/* 1038:     */     } else {
/* 1039:1262 */       timeoutFuture = null;
/* 1040:     */     }
/* 1041:1266 */     flushFuture.addListener(new ChannelFutureListener()
/* 1042:     */     {
/* 1043:     */       public void operationComplete(ChannelFuture f)
/* 1044:     */         throws Exception
/* 1045:     */       {
/* 1046:1270 */         if (timeoutFuture != null) {
/* 1047:1271 */           timeoutFuture.cancel(false);
/* 1048:     */         }
/* 1049:1275 */         ctx.close(promise);
/* 1050:     */       }
/* 1051:     */     });
/* 1052:     */   }
/* 1053:     */   
/* 1054:     */   private ByteBuf allocate(ChannelHandlerContext ctx, int capacity)
/* 1055:     */   {
/* 1056:1285 */     ByteBufAllocator alloc = ctx.alloc();
/* 1057:1286 */     if (this.wantsDirectBuffer) {
/* 1058:1287 */       return alloc.directBuffer(capacity);
/* 1059:     */     }
/* 1060:1289 */     return alloc.buffer(capacity);
/* 1061:     */   }
/* 1062:     */   
/* 1063:     */   private ByteBuf allocateOutNetBuf(ChannelHandlerContext ctx, int pendingBytes)
/* 1064:     */   {
/* 1065:1298 */     if (this.wantsLargeOutboundNetworkBuffer) {
/* 1066:1299 */       return allocate(ctx, this.maxPacketBufferSize);
/* 1067:     */     }
/* 1068:1301 */     return allocate(ctx, Math.min(pendingBytes + 2329, this.maxPacketBufferSize));
/* 1069:     */   }
/* 1070:     */   
/* 1071:     */   private final class LazyChannelPromise
/* 1072:     */     extends DefaultPromise<Channel>
/* 1073:     */   {
/* 1074:     */     private LazyChannelPromise() {}
/* 1075:     */     
/* 1076:     */     protected EventExecutor executor()
/* 1077:     */     {
/* 1078:1311 */       if (SslHandler.this.ctx == null) {
/* 1079:1312 */         throw new IllegalStateException();
/* 1080:     */       }
/* 1081:1314 */       return SslHandler.this.ctx.executor();
/* 1082:     */     }
/* 1083:     */   }
/* 1084:     */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.SslHandler
 * JD-Core Version:    0.7.0.1
 */