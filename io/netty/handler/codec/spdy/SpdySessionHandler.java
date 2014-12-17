/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.ChannelDuplexHandler;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelFutureListener;
/*   8:    */ import io.netty.channel.ChannelHandlerContext;
/*   9:    */ import io.netty.channel.ChannelPromise;
/*  10:    */ import io.netty.util.internal.EmptyArrays;
/*  11:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  12:    */ 
/*  13:    */ public class SpdySessionHandler
/*  14:    */   extends ChannelDuplexHandler
/*  15:    */ {
/*  16: 35 */   private static final SpdyProtocolException PROTOCOL_EXCEPTION = new SpdyProtocolException();
/*  17: 36 */   private static final SpdyProtocolException STREAM_CLOSED = new SpdyProtocolException("Stream closed");
/*  18:    */   private static final int DEFAULT_WINDOW_SIZE = 65536;
/*  19:    */   
/*  20:    */   static
/*  21:    */   {
/*  22: 39 */     PROTOCOL_EXCEPTION.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
/*  23: 40 */     STREAM_CLOSED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
/*  24:    */   }
/*  25:    */   
/*  26: 44 */   private int initialSendWindowSize = 65536;
/*  27: 45 */   private int initialReceiveWindowSize = 65536;
/*  28: 46 */   private volatile int initialSessionReceiveWindowSize = 65536;
/*  29: 48 */   private final SpdySession spdySession = new SpdySession(this.initialSendWindowSize, this.initialReceiveWindowSize);
/*  30:    */   private int lastGoodStreamId;
/*  31:    */   private static final int DEFAULT_MAX_CONCURRENT_STREAMS = 2147483647;
/*  32: 52 */   private int remoteConcurrentStreams = 2147483647;
/*  33: 53 */   private int localConcurrentStreams = 2147483647;
/*  34: 55 */   private final Object flowControlLock = new Object();
/*  35: 57 */   private final AtomicInteger pings = new AtomicInteger();
/*  36:    */   private boolean sentGoAwayFrame;
/*  37:    */   private boolean receivedGoAwayFrame;
/*  38:    */   private ChannelFutureListener closeSessionFutureListener;
/*  39:    */   private final boolean server;
/*  40:    */   private final int minorVersion;
/*  41:    */   
/*  42:    */   public SpdySessionHandler(SpdyVersion version, boolean server)
/*  43:    */   {
/*  44: 77 */     if (version == null) {
/*  45: 78 */       throw new NullPointerException("version");
/*  46:    */     }
/*  47: 80 */     this.server = server;
/*  48: 81 */     this.minorVersion = version.getMinorVersion();
/*  49:    */   }
/*  50:    */   
/*  51:    */   public void setSessionReceiveWindowSize(int sessionReceiveWindowSize)
/*  52:    */   {
/*  53: 85 */     if (sessionReceiveWindowSize < 0) {
/*  54: 86 */       throw new IllegalArgumentException("sessionReceiveWindowSize");
/*  55:    */     }
/*  56: 94 */     this.initialSessionReceiveWindowSize = sessionReceiveWindowSize;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  60:    */     throws Exception
/*  61:    */   {
/*  62: 99 */     if ((msg instanceof SpdyDataFrame))
/*  63:    */     {
/*  64:123 */       SpdyDataFrame spdyDataFrame = (SpdyDataFrame)msg;
/*  65:124 */       int streamId = spdyDataFrame.streamId();
/*  66:    */       
/*  67:126 */       int deltaWindowSize = -1 * spdyDataFrame.content().readableBytes();
/*  68:127 */       int newSessionWindowSize = this.spdySession.updateReceiveWindowSize(0, deltaWindowSize);
/*  69:131 */       if (newSessionWindowSize < 0)
/*  70:    */       {
/*  71:132 */         issueSessionError(ctx, SpdySessionStatus.PROTOCOL_ERROR);
/*  72:133 */         return;
/*  73:    */       }
/*  74:137 */       if (newSessionWindowSize <= this.initialSessionReceiveWindowSize / 2)
/*  75:    */       {
/*  76:138 */         int sessionDeltaWindowSize = this.initialSessionReceiveWindowSize - newSessionWindowSize;
/*  77:139 */         this.spdySession.updateReceiveWindowSize(0, sessionDeltaWindowSize);
/*  78:140 */         SpdyWindowUpdateFrame spdyWindowUpdateFrame = new DefaultSpdyWindowUpdateFrame(0, sessionDeltaWindowSize);
/*  79:    */         
/*  80:142 */         ctx.writeAndFlush(spdyWindowUpdateFrame);
/*  81:    */       }
/*  82:147 */       if (!this.spdySession.isActiveStream(streamId))
/*  83:    */       {
/*  84:148 */         spdyDataFrame.release();
/*  85:149 */         if (streamId <= this.lastGoodStreamId) {
/*  86:150 */           issueStreamError(ctx, streamId, SpdyStreamStatus.PROTOCOL_ERROR);
/*  87:151 */         } else if (!this.sentGoAwayFrame) {
/*  88:152 */           issueStreamError(ctx, streamId, SpdyStreamStatus.INVALID_STREAM);
/*  89:    */         }
/*  90:154 */         return;
/*  91:    */       }
/*  92:159 */       if (this.spdySession.isRemoteSideClosed(streamId))
/*  93:    */       {
/*  94:160 */         spdyDataFrame.release();
/*  95:161 */         issueStreamError(ctx, streamId, SpdyStreamStatus.STREAM_ALREADY_CLOSED);
/*  96:162 */         return;
/*  97:    */       }
/*  98:166 */       if ((!isRemoteInitiatedId(streamId)) && (!this.spdySession.hasReceivedReply(streamId)))
/*  99:    */       {
/* 100:167 */         spdyDataFrame.release();
/* 101:168 */         issueStreamError(ctx, streamId, SpdyStreamStatus.PROTOCOL_ERROR);
/* 102:169 */         return;
/* 103:    */       }
/* 104:179 */       int newWindowSize = this.spdySession.updateReceiveWindowSize(streamId, deltaWindowSize);
/* 105:186 */       if (newWindowSize < this.spdySession.getReceiveWindowSizeLowerBound(streamId))
/* 106:    */       {
/* 107:187 */         spdyDataFrame.release();
/* 108:188 */         issueStreamError(ctx, streamId, SpdyStreamStatus.FLOW_CONTROL_ERROR);
/* 109:189 */         return;
/* 110:    */       }
/* 111:194 */       if (newWindowSize < 0) {
/* 112:195 */         while (spdyDataFrame.content().readableBytes() > this.initialReceiveWindowSize)
/* 113:    */         {
/* 114:196 */           SpdyDataFrame partialDataFrame = new DefaultSpdyDataFrame(streamId, spdyDataFrame.content().readSlice(this.initialReceiveWindowSize).retain());
/* 115:    */           
/* 116:198 */           ctx.writeAndFlush(partialDataFrame);
/* 117:    */         }
/* 118:    */       }
/* 119:203 */       if ((newWindowSize <= this.initialReceiveWindowSize / 2) && (!spdyDataFrame.isLast()))
/* 120:    */       {
/* 121:204 */         int streamDeltaWindowSize = this.initialReceiveWindowSize - newWindowSize;
/* 122:205 */         this.spdySession.updateReceiveWindowSize(streamId, streamDeltaWindowSize);
/* 123:206 */         SpdyWindowUpdateFrame spdyWindowUpdateFrame = new DefaultSpdyWindowUpdateFrame(streamId, streamDeltaWindowSize);
/* 124:    */         
/* 125:208 */         ctx.writeAndFlush(spdyWindowUpdateFrame);
/* 126:    */       }
/* 127:212 */       if (spdyDataFrame.isLast()) {
/* 128:213 */         halfCloseStream(streamId, true, ctx.newSucceededFuture());
/* 129:    */       }
/* 130:    */     }
/* 131:216 */     else if ((msg instanceof SpdySynStreamFrame))
/* 132:    */     {
/* 133:232 */       SpdySynStreamFrame spdySynStreamFrame = (SpdySynStreamFrame)msg;
/* 134:233 */       int streamId = spdySynStreamFrame.streamId();
/* 135:236 */       if ((spdySynStreamFrame.isInvalid()) || (!isRemoteInitiatedId(streamId)) || (this.spdySession.isActiveStream(streamId)))
/* 136:    */       {
/* 137:239 */         issueStreamError(ctx, streamId, SpdyStreamStatus.PROTOCOL_ERROR);
/* 138:240 */         return;
/* 139:    */       }
/* 140:244 */       if (streamId <= this.lastGoodStreamId)
/* 141:    */       {
/* 142:245 */         issueSessionError(ctx, SpdySessionStatus.PROTOCOL_ERROR);
/* 143:246 */         return;
/* 144:    */       }
/* 145:250 */       byte priority = spdySynStreamFrame.priority();
/* 146:251 */       boolean remoteSideClosed = spdySynStreamFrame.isLast();
/* 147:252 */       boolean localSideClosed = spdySynStreamFrame.isUnidirectional();
/* 148:253 */       if (!acceptStream(streamId, priority, remoteSideClosed, localSideClosed))
/* 149:    */       {
/* 150:254 */         issueStreamError(ctx, streamId, SpdyStreamStatus.REFUSED_STREAM);
/* 151:255 */         return;
/* 152:    */       }
/* 153:    */     }
/* 154:258 */     else if ((msg instanceof SpdySynReplyFrame))
/* 155:    */     {
/* 156:267 */       SpdySynReplyFrame spdySynReplyFrame = (SpdySynReplyFrame)msg;
/* 157:268 */       int streamId = spdySynReplyFrame.streamId();
/* 158:271 */       if ((spdySynReplyFrame.isInvalid()) || (isRemoteInitiatedId(streamId)) || (this.spdySession.isRemoteSideClosed(streamId)))
/* 159:    */       {
/* 160:274 */         issueStreamError(ctx, streamId, SpdyStreamStatus.INVALID_STREAM);
/* 161:275 */         return;
/* 162:    */       }
/* 163:279 */       if (this.spdySession.hasReceivedReply(streamId))
/* 164:    */       {
/* 165:280 */         issueStreamError(ctx, streamId, SpdyStreamStatus.STREAM_IN_USE);
/* 166:281 */         return;
/* 167:    */       }
/* 168:284 */       this.spdySession.receivedReply(streamId);
/* 169:287 */       if (spdySynReplyFrame.isLast()) {
/* 170:288 */         halfCloseStream(streamId, true, ctx.newSucceededFuture());
/* 171:    */       }
/* 172:    */     }
/* 173:291 */     else if ((msg instanceof SpdyRstStreamFrame))
/* 174:    */     {
/* 175:302 */       SpdyRstStreamFrame spdyRstStreamFrame = (SpdyRstStreamFrame)msg;
/* 176:303 */       removeStream(spdyRstStreamFrame.streamId(), ctx.newSucceededFuture());
/* 177:    */     }
/* 178:305 */     else if ((msg instanceof SpdySettingsFrame))
/* 179:    */     {
/* 180:307 */       SpdySettingsFrame spdySettingsFrame = (SpdySettingsFrame)msg;
/* 181:    */       
/* 182:309 */       int settingsMinorVersion = spdySettingsFrame.getValue(0);
/* 183:310 */       if ((settingsMinorVersion >= 0) && (settingsMinorVersion != this.minorVersion))
/* 184:    */       {
/* 185:312 */         issueSessionError(ctx, SpdySessionStatus.PROTOCOL_ERROR);
/* 186:313 */         return;
/* 187:    */       }
/* 188:316 */       int newConcurrentStreams = spdySettingsFrame.getValue(4);
/* 189:318 */       if (newConcurrentStreams >= 0) {
/* 190:319 */         this.remoteConcurrentStreams = newConcurrentStreams;
/* 191:    */       }
/* 192:325 */       if (spdySettingsFrame.isPersisted(7)) {
/* 193:326 */         spdySettingsFrame.removeValue(7);
/* 194:    */       }
/* 195:328 */       spdySettingsFrame.setPersistValue(7, false);
/* 196:    */       
/* 197:330 */       int newInitialWindowSize = spdySettingsFrame.getValue(7);
/* 198:332 */       if (newInitialWindowSize >= 0) {
/* 199:333 */         updateInitialSendWindowSize(newInitialWindowSize);
/* 200:    */       }
/* 201:    */     }
/* 202:336 */     else if ((msg instanceof SpdyPingFrame))
/* 203:    */     {
/* 204:347 */       SpdyPingFrame spdyPingFrame = (SpdyPingFrame)msg;
/* 205:349 */       if (isRemoteInitiatedId(spdyPingFrame.id()))
/* 206:    */       {
/* 207:350 */         ctx.writeAndFlush(spdyPingFrame);
/* 208:351 */         return;
/* 209:    */       }
/* 210:355 */       if (this.pings.get() == 0) {
/* 211:356 */         return;
/* 212:    */       }
/* 213:358 */       this.pings.getAndDecrement();
/* 214:    */     }
/* 215:360 */     else if ((msg instanceof SpdyGoAwayFrame))
/* 216:    */     {
/* 217:362 */       this.receivedGoAwayFrame = true;
/* 218:    */     }
/* 219:364 */     else if ((msg instanceof SpdyHeadersFrame))
/* 220:    */     {
/* 221:366 */       SpdyHeadersFrame spdyHeadersFrame = (SpdyHeadersFrame)msg;
/* 222:367 */       int streamId = spdyHeadersFrame.streamId();
/* 223:370 */       if (spdyHeadersFrame.isInvalid())
/* 224:    */       {
/* 225:371 */         issueStreamError(ctx, streamId, SpdyStreamStatus.PROTOCOL_ERROR);
/* 226:372 */         return;
/* 227:    */       }
/* 228:375 */       if (this.spdySession.isRemoteSideClosed(streamId))
/* 229:    */       {
/* 230:376 */         issueStreamError(ctx, streamId, SpdyStreamStatus.INVALID_STREAM);
/* 231:377 */         return;
/* 232:    */       }
/* 233:381 */       if (spdyHeadersFrame.isLast()) {
/* 234:382 */         halfCloseStream(streamId, true, ctx.newSucceededFuture());
/* 235:    */       }
/* 236:    */     }
/* 237:385 */     else if ((msg instanceof SpdyWindowUpdateFrame))
/* 238:    */     {
/* 239:397 */       SpdyWindowUpdateFrame spdyWindowUpdateFrame = (SpdyWindowUpdateFrame)msg;
/* 240:398 */       int streamId = spdyWindowUpdateFrame.streamId();
/* 241:399 */       int deltaWindowSize = spdyWindowUpdateFrame.deltaWindowSize();
/* 242:402 */       if ((streamId != 0) && (this.spdySession.isLocalSideClosed(streamId))) {
/* 243:403 */         return;
/* 244:    */       }
/* 245:407 */       if (this.spdySession.getSendWindowSize(streamId) > 2147483647 - deltaWindowSize)
/* 246:    */       {
/* 247:408 */         if (streamId == 0) {
/* 248:409 */           issueSessionError(ctx, SpdySessionStatus.PROTOCOL_ERROR);
/* 249:    */         } else {
/* 250:411 */           issueStreamError(ctx, streamId, SpdyStreamStatus.FLOW_CONTROL_ERROR);
/* 251:    */         }
/* 252:413 */         return;
/* 253:    */       }
/* 254:416 */       updateSendWindowSize(ctx, streamId, deltaWindowSize);
/* 255:    */     }
/* 256:419 */     ctx.fireChannelRead(msg);
/* 257:    */   }
/* 258:    */   
/* 259:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 260:    */     throws Exception
/* 261:    */   {
/* 262:424 */     for (Integer streamId : this.spdySession.getActiveStreams()) {
/* 263:425 */       removeStream(streamId.intValue(), ctx.newSucceededFuture());
/* 264:    */     }
/* 265:427 */     ctx.fireChannelInactive();
/* 266:    */   }
/* 267:    */   
/* 268:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 269:    */     throws Exception
/* 270:    */   {
/* 271:432 */     if ((cause instanceof SpdyProtocolException)) {
/* 272:433 */       issueSessionError(ctx, SpdySessionStatus.PROTOCOL_ERROR);
/* 273:    */     }
/* 274:436 */     ctx.fireExceptionCaught(cause);
/* 275:    */   }
/* 276:    */   
/* 277:    */   public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/* 278:    */     throws Exception
/* 279:    */   {
/* 280:441 */     sendGoAwayFrame(ctx, promise);
/* 281:    */   }
/* 282:    */   
/* 283:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 284:    */     throws Exception
/* 285:    */   {
/* 286:446 */     if (((msg instanceof SpdyDataFrame)) || ((msg instanceof SpdySynStreamFrame)) || ((msg instanceof SpdySynReplyFrame)) || ((msg instanceof SpdyRstStreamFrame)) || ((msg instanceof SpdySettingsFrame)) || ((msg instanceof SpdyPingFrame)) || ((msg instanceof SpdyGoAwayFrame)) || ((msg instanceof SpdyHeadersFrame)) || ((msg instanceof SpdyWindowUpdateFrame))) {
/* 287:456 */       handleOutboundMessage(ctx, msg, promise);
/* 288:    */     } else {
/* 289:458 */       ctx.write(msg, promise);
/* 290:    */     }
/* 291:    */   }
/* 292:    */   
/* 293:    */   private void handleOutboundMessage(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 294:    */     throws Exception
/* 295:    */   {
/* 296:463 */     if ((msg instanceof SpdyDataFrame))
/* 297:    */     {
/* 298:465 */       SpdyDataFrame spdyDataFrame = (SpdyDataFrame)msg;
/* 299:466 */       int streamId = spdyDataFrame.streamId();
/* 300:469 */       if (this.spdySession.isLocalSideClosed(streamId))
/* 301:    */       {
/* 302:470 */         spdyDataFrame.release();
/* 303:471 */         promise.setFailure(PROTOCOL_EXCEPTION);
/* 304:472 */         return;
/* 305:    */       }
/* 306:488 */       synchronized (this.flowControlLock)
/* 307:    */       {
/* 308:489 */         int dataLength = spdyDataFrame.content().readableBytes();
/* 309:490 */         int sendWindowSize = this.spdySession.getSendWindowSize(streamId);
/* 310:491 */         int sessionSendWindowSize = this.spdySession.getSendWindowSize(0);
/* 311:492 */         sendWindowSize = Math.min(sendWindowSize, sessionSendWindowSize);
/* 312:494 */         if (sendWindowSize <= 0)
/* 313:    */         {
/* 314:496 */           this.spdySession.putPendingWrite(streamId, new SpdySession.PendingWrite(spdyDataFrame, promise));
/* 315:497 */           return;
/* 316:    */         }
/* 317:498 */         if (sendWindowSize < dataLength)
/* 318:    */         {
/* 319:500 */           this.spdySession.updateSendWindowSize(streamId, -1 * sendWindowSize);
/* 320:501 */           this.spdySession.updateSendWindowSize(0, -1 * sendWindowSize);
/* 321:    */           
/* 322:    */ 
/* 323:504 */           SpdyDataFrame partialDataFrame = new DefaultSpdyDataFrame(streamId, spdyDataFrame.content().readSlice(sendWindowSize).retain());
/* 324:    */           
/* 325:    */ 
/* 326:    */ 
/* 327:508 */           this.spdySession.putPendingWrite(streamId, new SpdySession.PendingWrite(spdyDataFrame, promise));
/* 328:    */           
/* 329:    */ 
/* 330:    */ 
/* 331:512 */           final ChannelHandlerContext context = ctx;
/* 332:513 */           ctx.write(partialDataFrame).addListener(new ChannelFutureListener()
/* 333:    */           {
/* 334:    */             public void operationComplete(ChannelFuture future)
/* 335:    */               throws Exception
/* 336:    */             {
/* 337:516 */               if (!future.isSuccess()) {
/* 338:517 */                 SpdySessionHandler.this.issueSessionError(context, SpdySessionStatus.INTERNAL_ERROR);
/* 339:    */               }
/* 340:    */             }
/* 341:520 */           });
/* 342:521 */           return;
/* 343:    */         }
/* 344:524 */         this.spdySession.updateSendWindowSize(streamId, -1 * dataLength);
/* 345:525 */         this.spdySession.updateSendWindowSize(0, -1 * dataLength);
/* 346:    */         
/* 347:    */ 
/* 348:    */ 
/* 349:529 */         final ChannelHandlerContext context = ctx;
/* 350:530 */         promise.addListener(new ChannelFutureListener()
/* 351:    */         {
/* 352:    */           public void operationComplete(ChannelFuture future)
/* 353:    */             throws Exception
/* 354:    */           {
/* 355:533 */             if (!future.isSuccess()) {
/* 356:534 */               SpdySessionHandler.this.issueSessionError(context, SpdySessionStatus.INTERNAL_ERROR);
/* 357:    */             }
/* 358:    */           }
/* 359:    */         });
/* 360:    */       }
/* 361:542 */       if (spdyDataFrame.isLast()) {
/* 362:543 */         halfCloseStream(streamId, false, promise);
/* 363:    */       }
/* 364:    */     }
/* 365:546 */     else if ((msg instanceof SpdySynStreamFrame))
/* 366:    */     {
/* 367:548 */       SpdySynStreamFrame spdySynStreamFrame = (SpdySynStreamFrame)msg;
/* 368:549 */       int streamId = spdySynStreamFrame.streamId();
/* 369:551 */       if (isRemoteInitiatedId(streamId))
/* 370:    */       {
/* 371:552 */         promise.setFailure(PROTOCOL_EXCEPTION);
/* 372:553 */         return;
/* 373:    */       }
/* 374:556 */       byte priority = spdySynStreamFrame.priority();
/* 375:557 */       boolean remoteSideClosed = spdySynStreamFrame.isUnidirectional();
/* 376:558 */       boolean localSideClosed = spdySynStreamFrame.isLast();
/* 377:559 */       if (!acceptStream(streamId, priority, remoteSideClosed, localSideClosed))
/* 378:    */       {
/* 379:560 */         promise.setFailure(PROTOCOL_EXCEPTION);
/* 380:561 */         return;
/* 381:    */       }
/* 382:    */     }
/* 383:564 */     else if ((msg instanceof SpdySynReplyFrame))
/* 384:    */     {
/* 385:566 */       SpdySynReplyFrame spdySynReplyFrame = (SpdySynReplyFrame)msg;
/* 386:567 */       int streamId = spdySynReplyFrame.streamId();
/* 387:570 */       if ((!isRemoteInitiatedId(streamId)) || (this.spdySession.isLocalSideClosed(streamId)))
/* 388:    */       {
/* 389:571 */         promise.setFailure(PROTOCOL_EXCEPTION);
/* 390:572 */         return;
/* 391:    */       }
/* 392:576 */       if (spdySynReplyFrame.isLast()) {
/* 393:577 */         halfCloseStream(streamId, false, promise);
/* 394:    */       }
/* 395:    */     }
/* 396:580 */     else if ((msg instanceof SpdyRstStreamFrame))
/* 397:    */     {
/* 398:582 */       SpdyRstStreamFrame spdyRstStreamFrame = (SpdyRstStreamFrame)msg;
/* 399:583 */       removeStream(spdyRstStreamFrame.streamId(), promise);
/* 400:    */     }
/* 401:585 */     else if ((msg instanceof SpdySettingsFrame))
/* 402:    */     {
/* 403:587 */       SpdySettingsFrame spdySettingsFrame = (SpdySettingsFrame)msg;
/* 404:    */       
/* 405:589 */       int settingsMinorVersion = spdySettingsFrame.getValue(0);
/* 406:590 */       if ((settingsMinorVersion >= 0) && (settingsMinorVersion != this.minorVersion))
/* 407:    */       {
/* 408:592 */         promise.setFailure(PROTOCOL_EXCEPTION);
/* 409:593 */         return;
/* 410:    */       }
/* 411:596 */       int newConcurrentStreams = spdySettingsFrame.getValue(4);
/* 412:598 */       if (newConcurrentStreams >= 0) {
/* 413:599 */         this.localConcurrentStreams = newConcurrentStreams;
/* 414:    */       }
/* 415:605 */       if (spdySettingsFrame.isPersisted(7)) {
/* 416:606 */         spdySettingsFrame.removeValue(7);
/* 417:    */       }
/* 418:608 */       spdySettingsFrame.setPersistValue(7, false);
/* 419:    */       
/* 420:610 */       int newInitialWindowSize = spdySettingsFrame.getValue(7);
/* 421:612 */       if (newInitialWindowSize >= 0) {
/* 422:613 */         updateInitialReceiveWindowSize(newInitialWindowSize);
/* 423:    */       }
/* 424:    */     }
/* 425:616 */     else if ((msg instanceof SpdyPingFrame))
/* 426:    */     {
/* 427:618 */       SpdyPingFrame spdyPingFrame = (SpdyPingFrame)msg;
/* 428:619 */       if (isRemoteInitiatedId(spdyPingFrame.id()))
/* 429:    */       {
/* 430:620 */         ctx.fireExceptionCaught(new IllegalArgumentException("invalid PING ID: " + spdyPingFrame.id()));
/* 431:    */         
/* 432:622 */         return;
/* 433:    */       }
/* 434:624 */       this.pings.getAndIncrement();
/* 435:    */     }
/* 436:    */     else
/* 437:    */     {
/* 438:626 */       if ((msg instanceof SpdyGoAwayFrame))
/* 439:    */       {
/* 440:630 */         promise.setFailure(PROTOCOL_EXCEPTION);
/* 441:631 */         return;
/* 442:    */       }
/* 443:633 */       if ((msg instanceof SpdyHeadersFrame))
/* 444:    */       {
/* 445:635 */         SpdyHeadersFrame spdyHeadersFrame = (SpdyHeadersFrame)msg;
/* 446:636 */         int streamId = spdyHeadersFrame.streamId();
/* 447:639 */         if (this.spdySession.isLocalSideClosed(streamId))
/* 448:    */         {
/* 449:640 */           promise.setFailure(PROTOCOL_EXCEPTION);
/* 450:641 */           return;
/* 451:    */         }
/* 452:645 */         if (spdyHeadersFrame.isLast()) {
/* 453:646 */           halfCloseStream(streamId, false, promise);
/* 454:    */         }
/* 455:    */       }
/* 456:649 */       else if ((msg instanceof SpdyWindowUpdateFrame))
/* 457:    */       {
/* 458:652 */         promise.setFailure(PROTOCOL_EXCEPTION);
/* 459:653 */         return;
/* 460:    */       }
/* 461:    */     }
/* 462:656 */     ctx.write(msg, promise);
/* 463:    */   }
/* 464:    */   
/* 465:    */   private void issueSessionError(ChannelHandlerContext ctx, SpdySessionStatus status)
/* 466:    */   {
/* 467:671 */     sendGoAwayFrame(ctx, status).addListener(new ClosingChannelFutureListener(ctx, ctx.newPromise()));
/* 468:    */   }
/* 469:    */   
/* 470:    */   private void issueStreamError(ChannelHandlerContext ctx, int streamId, SpdyStreamStatus status)
/* 471:    */   {
/* 472:686 */     boolean fireChannelRead = !this.spdySession.isRemoteSideClosed(streamId);
/* 473:687 */     ChannelPromise promise = ctx.newPromise();
/* 474:688 */     removeStream(streamId, promise);
/* 475:    */     
/* 476:690 */     SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, status);
/* 477:691 */     ctx.writeAndFlush(spdyRstStreamFrame, promise);
/* 478:692 */     if (fireChannelRead) {
/* 479:693 */       ctx.fireChannelRead(spdyRstStreamFrame);
/* 480:    */     }
/* 481:    */   }
/* 482:    */   
/* 483:    */   private boolean isRemoteInitiatedId(int id)
/* 484:    */   {
/* 485:702 */     boolean serverId = SpdyCodecUtil.isServerId(id);
/* 486:703 */     return ((this.server) && (!serverId)) || ((!this.server) && (serverId));
/* 487:    */   }
/* 488:    */   
/* 489:    */   private synchronized void updateInitialSendWindowSize(int newInitialWindowSize)
/* 490:    */   {
/* 491:708 */     int deltaWindowSize = newInitialWindowSize - this.initialSendWindowSize;
/* 492:709 */     this.initialSendWindowSize = newInitialWindowSize;
/* 493:710 */     this.spdySession.updateAllSendWindowSizes(deltaWindowSize);
/* 494:    */   }
/* 495:    */   
/* 496:    */   private synchronized void updateInitialReceiveWindowSize(int newInitialWindowSize)
/* 497:    */   {
/* 498:715 */     int deltaWindowSize = newInitialWindowSize - this.initialReceiveWindowSize;
/* 499:716 */     this.initialReceiveWindowSize = newInitialWindowSize;
/* 500:717 */     this.spdySession.updateAllReceiveWindowSizes(deltaWindowSize);
/* 501:    */   }
/* 502:    */   
/* 503:    */   private synchronized boolean acceptStream(int streamId, byte priority, boolean remoteSideClosed, boolean localSideClosed)
/* 504:    */   {
/* 505:724 */     if ((this.receivedGoAwayFrame) || (this.sentGoAwayFrame)) {
/* 506:725 */       return false;
/* 507:    */     }
/* 508:728 */     boolean remote = isRemoteInitiatedId(streamId);
/* 509:729 */     int maxConcurrentStreams = remote ? this.localConcurrentStreams : this.remoteConcurrentStreams;
/* 510:730 */     if (this.spdySession.numActiveStreams(remote) >= maxConcurrentStreams) {
/* 511:731 */       return false;
/* 512:    */     }
/* 513:733 */     this.spdySession.acceptStream(streamId, priority, remoteSideClosed, localSideClosed, this.initialSendWindowSize, this.initialReceiveWindowSize, remote);
/* 514:736 */     if (remote) {
/* 515:737 */       this.lastGoodStreamId = streamId;
/* 516:    */     }
/* 517:739 */     return true;
/* 518:    */   }
/* 519:    */   
/* 520:    */   private void halfCloseStream(int streamId, boolean remote, ChannelFuture future)
/* 521:    */   {
/* 522:743 */     if (remote) {
/* 523:744 */       this.spdySession.closeRemoteSide(streamId, isRemoteInitiatedId(streamId));
/* 524:    */     } else {
/* 525:746 */       this.spdySession.closeLocalSide(streamId, isRemoteInitiatedId(streamId));
/* 526:    */     }
/* 527:748 */     if ((this.closeSessionFutureListener != null) && (this.spdySession.noActiveStreams())) {
/* 528:749 */       future.addListener(this.closeSessionFutureListener);
/* 529:    */     }
/* 530:    */   }
/* 531:    */   
/* 532:    */   private void removeStream(int streamId, ChannelFuture future)
/* 533:    */   {
/* 534:754 */     this.spdySession.removeStream(streamId, STREAM_CLOSED, isRemoteInitiatedId(streamId));
/* 535:756 */     if ((this.closeSessionFutureListener != null) && (this.spdySession.noActiveStreams())) {
/* 536:757 */       future.addListener(this.closeSessionFutureListener);
/* 537:    */     }
/* 538:    */   }
/* 539:    */   
/* 540:    */   private void updateSendWindowSize(final ChannelHandlerContext ctx, int streamId, int deltaWindowSize)
/* 541:    */   {
/* 542:762 */     synchronized (this.flowControlLock)
/* 543:    */     {
/* 544:763 */       int newWindowSize = this.spdySession.updateSendWindowSize(streamId, deltaWindowSize);
/* 545:764 */       if (streamId != 0)
/* 546:    */       {
/* 547:765 */         int sessionSendWindowSize = this.spdySession.getSendWindowSize(0);
/* 548:766 */         newWindowSize = Math.min(newWindowSize, sessionSendWindowSize);
/* 549:    */       }
/* 550:769 */       while (newWindowSize > 0)
/* 551:    */       {
/* 552:771 */         SpdySession.PendingWrite pendingWrite = this.spdySession.getPendingWrite(streamId);
/* 553:772 */         if (pendingWrite == null) {
/* 554:    */           break;
/* 555:    */         }
/* 556:776 */         SpdyDataFrame spdyDataFrame = pendingWrite.spdyDataFrame;
/* 557:777 */         int dataFrameSize = spdyDataFrame.content().readableBytes();
/* 558:778 */         int writeStreamId = spdyDataFrame.streamId();
/* 559:779 */         if (streamId == 0) {
/* 560:780 */           newWindowSize = Math.min(newWindowSize, this.spdySession.getSendWindowSize(writeStreamId));
/* 561:    */         }
/* 562:783 */         if (newWindowSize >= dataFrameSize)
/* 563:    */         {
/* 564:785 */           this.spdySession.removePendingWrite(writeStreamId);
/* 565:786 */           newWindowSize = this.spdySession.updateSendWindowSize(writeStreamId, -1 * dataFrameSize);
/* 566:787 */           int sessionSendWindowSize = this.spdySession.updateSendWindowSize(0, -1 * dataFrameSize);
/* 567:    */           
/* 568:789 */           newWindowSize = Math.min(newWindowSize, sessionSendWindowSize);
/* 569:792 */           if (spdyDataFrame.isLast()) {
/* 570:793 */             halfCloseStream(writeStreamId, false, pendingWrite.promise);
/* 571:    */           }
/* 572:798 */           ctx.writeAndFlush(spdyDataFrame, pendingWrite.promise).addListener(new ChannelFutureListener()
/* 573:    */           {
/* 574:    */             public void operationComplete(ChannelFuture future)
/* 575:    */               throws Exception
/* 576:    */             {
/* 577:801 */               if (!future.isSuccess()) {
/* 578:802 */                 SpdySessionHandler.this.issueSessionError(ctx, SpdySessionStatus.INTERNAL_ERROR);
/* 579:    */               }
/* 580:    */             }
/* 581:    */           });
/* 582:    */         }
/* 583:    */         else
/* 584:    */         {
/* 585:808 */           this.spdySession.updateSendWindowSize(writeStreamId, -1 * newWindowSize);
/* 586:809 */           this.spdySession.updateSendWindowSize(0, -1 * newWindowSize);
/* 587:    */           
/* 588:    */ 
/* 589:812 */           SpdyDataFrame partialDataFrame = new DefaultSpdyDataFrame(writeStreamId, spdyDataFrame.content().readSlice(newWindowSize).retain());
/* 590:    */           
/* 591:    */ 
/* 592:    */ 
/* 593:    */ 
/* 594:817 */           ctx.writeAndFlush(partialDataFrame).addListener(new ChannelFutureListener()
/* 595:    */           {
/* 596:    */             public void operationComplete(ChannelFuture future)
/* 597:    */               throws Exception
/* 598:    */             {
/* 599:820 */               if (!future.isSuccess()) {
/* 600:821 */                 SpdySessionHandler.this.issueSessionError(ctx, SpdySessionStatus.INTERNAL_ERROR);
/* 601:    */               }
/* 602:    */             }
/* 603:825 */           });
/* 604:826 */           newWindowSize = 0;
/* 605:    */         }
/* 606:    */       }
/* 607:    */     }
/* 608:    */   }
/* 609:    */   
/* 610:    */   private void sendGoAwayFrame(ChannelHandlerContext ctx, ChannelPromise future)
/* 611:    */   {
/* 612:834 */     if (!ctx.channel().isActive())
/* 613:    */     {
/* 614:835 */       ctx.close(future);
/* 615:836 */       return;
/* 616:    */     }
/* 617:839 */     ChannelFuture f = sendGoAwayFrame(ctx, SpdySessionStatus.OK);
/* 618:840 */     if (this.spdySession.noActiveStreams()) {
/* 619:841 */       f.addListener(new ClosingChannelFutureListener(ctx, future));
/* 620:    */     } else {
/* 621:843 */       this.closeSessionFutureListener = new ClosingChannelFutureListener(ctx, future);
/* 622:    */     }
/* 623:    */   }
/* 624:    */   
/* 625:    */   private synchronized ChannelFuture sendGoAwayFrame(ChannelHandlerContext ctx, SpdySessionStatus status)
/* 626:    */   {
/* 627:850 */     if (!this.sentGoAwayFrame)
/* 628:    */     {
/* 629:851 */       this.sentGoAwayFrame = true;
/* 630:852 */       SpdyGoAwayFrame spdyGoAwayFrame = new DefaultSpdyGoAwayFrame(this.lastGoodStreamId, status);
/* 631:853 */       return ctx.writeAndFlush(spdyGoAwayFrame);
/* 632:    */     }
/* 633:855 */     return ctx.newSucceededFuture();
/* 634:    */   }
/* 635:    */   
/* 636:    */   private static final class ClosingChannelFutureListener
/* 637:    */     implements ChannelFutureListener
/* 638:    */   {
/* 639:    */     private final ChannelHandlerContext ctx;
/* 640:    */     private final ChannelPromise promise;
/* 641:    */     
/* 642:    */     ClosingChannelFutureListener(ChannelHandlerContext ctx, ChannelPromise promise)
/* 643:    */     {
/* 644:864 */       this.ctx = ctx;
/* 645:865 */       this.promise = promise;
/* 646:    */     }
/* 647:    */     
/* 648:    */     public void operationComplete(ChannelFuture sentGoAwayFuture)
/* 649:    */       throws Exception
/* 650:    */     {
/* 651:870 */       this.ctx.close(this.promise);
/* 652:    */     }
/* 653:    */   }
/* 654:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdySessionHandler
 * JD-Core Version:    0.7.0.1
 */