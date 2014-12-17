/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.util.internal.EmptyArrays;
/*   6:    */ import io.netty.util.internal.logging.InternalLogger;
/*   7:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   8:    */ import java.nio.ByteBuffer;
/*   9:    */ import java.nio.ReadOnlyBufferException;
/*  10:    */ import java.security.Principal;
/*  11:    */ import java.security.cert.Certificate;
/*  12:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  13:    */ import javax.net.ssl.SSLEngine;
/*  14:    */ import javax.net.ssl.SSLEngineResult;
/*  15:    */ import javax.net.ssl.SSLEngineResult.HandshakeStatus;
/*  16:    */ import javax.net.ssl.SSLEngineResult.Status;
/*  17:    */ import javax.net.ssl.SSLException;
/*  18:    */ import javax.net.ssl.SSLSession;
/*  19:    */ import javax.net.ssl.SSLSessionContext;
/*  20:    */ import javax.security.cert.X509Certificate;
/*  21:    */ import org.apache.tomcat.jni.Buffer;
/*  22:    */ import org.apache.tomcat.jni.SSL;
/*  23:    */ 
/*  24:    */ public final class OpenSslEngine
/*  25:    */   extends SSLEngine
/*  26:    */ {
/*  27: 47 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(OpenSslEngine.class);
/*  28: 49 */   private static final Certificate[] EMPTY_CERTIFICATES = new Certificate[0];
/*  29: 50 */   private static final X509Certificate[] EMPTY_X509_CERTIFICATES = new X509Certificate[0];
/*  30: 52 */   private static final SSLException ENGINE_CLOSED = new SSLException("engine closed");
/*  31: 53 */   private static final SSLException RENEGOTIATION_UNSUPPORTED = new SSLException("renegotiation unsupported");
/*  32: 54 */   private static final SSLException ENCRYPTED_PACKET_OVERSIZED = new SSLException("encrypted packet oversized");
/*  33:    */   private static final int MAX_PLAINTEXT_LENGTH = 16384;
/*  34:    */   private static final int MAX_COMPRESSED_LENGTH = 17408;
/*  35:    */   private static final int MAX_CIPHERTEXT_LENGTH = 18432;
/*  36:    */   static final int MAX_ENCRYPTED_PACKET_LENGTH = 18713;
/*  37:    */   static final int MAX_ENCRYPTION_OVERHEAD_LENGTH = 2329;
/*  38:    */   
/*  39:    */   static
/*  40:    */   {
/*  41: 57 */     ENGINE_CLOSED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
/*  42: 58 */     RENEGOTIATION_UNSUPPORTED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
/*  43: 59 */     ENCRYPTED_PACKET_OVERSIZED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
/*  44:    */   }
/*  45:    */   
/*  46: 71 */   private static final AtomicIntegerFieldUpdater<OpenSslEngine> DESTROYED_UPDATER = AtomicIntegerFieldUpdater.newUpdater(OpenSslEngine.class, "destroyed");
/*  47:    */   private long ssl;
/*  48:    */   private long networkBIO;
/*  49:    */   private int accepted;
/*  50:    */   private boolean handshakeFinished;
/*  51:    */   private boolean receivedShutdown;
/*  52:    */   private volatile int destroyed;
/*  53:    */   private String cipher;
/*  54:    */   private volatile String applicationProtocol;
/*  55:    */   private boolean isInboundDone;
/*  56:    */   private boolean isOutboundDone;
/*  57:    */   private boolean engineClosed;
/*  58:    */   private int lastPrimingReadResult;
/*  59:    */   private final ByteBufAllocator alloc;
/*  60:    */   private final String fallbackApplicationProtocol;
/*  61:    */   private SSLSession session;
/*  62:    */   
/*  63:    */   public OpenSslEngine(long sslCtx, ByteBufAllocator alloc, String fallbackApplicationProtocol)
/*  64:    */   {
/*  65:108 */     OpenSsl.ensureAvailability();
/*  66:109 */     if (sslCtx == 0L) {
/*  67:110 */       throw new NullPointerException("sslContext");
/*  68:    */     }
/*  69:112 */     if (alloc == null) {
/*  70:113 */       throw new NullPointerException("alloc");
/*  71:    */     }
/*  72:116 */     this.alloc = alloc;
/*  73:117 */     this.ssl = SSL.newSSL(sslCtx, true);
/*  74:118 */     this.networkBIO = SSL.makeNetworkBIO(this.ssl);
/*  75:119 */     this.fallbackApplicationProtocol = fallbackApplicationProtocol;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public synchronized void shutdown()
/*  79:    */   {
/*  80:126 */     if (DESTROYED_UPDATER.compareAndSet(this, 0, 1))
/*  81:    */     {
/*  82:127 */       SSL.freeSSL(this.ssl);
/*  83:128 */       SSL.freeBIO(this.networkBIO);
/*  84:129 */       this.ssl = (this.networkBIO = 0L);
/*  85:    */       
/*  86:    */ 
/*  87:132 */       this.isInboundDone = (this.isOutboundDone = this.engineClosed = 1);
/*  88:    */     }
/*  89:    */   }
/*  90:    */   
/*  91:    */   private int writePlaintextData(ByteBuffer src)
/*  92:    */   {
/*  93:142 */     int pos = src.position();
/*  94:143 */     int limit = src.limit();
/*  95:144 */     int len = Math.min(limit - pos, 16384);
/*  96:    */     int sslWrote;
/*  97:147 */     if (src.isDirect())
/*  98:    */     {
/*  99:148 */       long addr = Buffer.address(src) + pos;
/* 100:149 */       int sslWrote = SSL.writeToSSL(this.ssl, addr, len);
/* 101:150 */       if (sslWrote > 0)
/* 102:    */       {
/* 103:151 */         src.position(pos + sslWrote);
/* 104:152 */         return sslWrote;
/* 105:    */       }
/* 106:    */     }
/* 107:    */     else
/* 108:    */     {
/* 109:155 */       ByteBuf buf = this.alloc.directBuffer(len);
/* 110:    */       try
/* 111:    */       {
/* 112:    */         long addr;
/* 113:    */         long addr;
/* 114:158 */         if (buf.hasMemoryAddress()) {
/* 115:159 */           addr = buf.memoryAddress();
/* 116:    */         } else {
/* 117:161 */           addr = Buffer.address(buf.nioBuffer());
/* 118:    */         }
/* 119:164 */         src.limit(pos + len);
/* 120:    */         
/* 121:166 */         buf.setBytes(0, src);
/* 122:167 */         src.limit(limit);
/* 123:    */         
/* 124:169 */         sslWrote = SSL.writeToSSL(this.ssl, addr, len);
/* 125:170 */         if (sslWrote > 0)
/* 126:    */         {
/* 127:171 */           src.position(pos + sslWrote);
/* 128:172 */           return sslWrote;
/* 129:    */         }
/* 130:174 */         src.position(pos);
/* 131:    */       }
/* 132:    */       finally
/* 133:    */       {
/* 134:177 */         buf.release();
/* 135:    */       }
/* 136:    */     }
/* 137:181 */     throw new IllegalStateException("SSL.writeToSSL() returned a non-positive value: " + sslWrote);
/* 138:    */   }
/* 139:    */   
/* 140:    */   private int writeEncryptedData(ByteBuffer src)
/* 141:    */   {
/* 142:188 */     int pos = src.position();
/* 143:189 */     int len = src.remaining();
/* 144:190 */     if (src.isDirect())
/* 145:    */     {
/* 146:191 */       long addr = Buffer.address(src) + pos;
/* 147:192 */       int netWrote = SSL.writeToBIO(this.networkBIO, addr, len);
/* 148:193 */       if (netWrote >= 0)
/* 149:    */       {
/* 150:194 */         src.position(pos + netWrote);
/* 151:195 */         this.lastPrimingReadResult = SSL.readFromSSL(this.ssl, addr, 0);
/* 152:196 */         return netWrote;
/* 153:    */       }
/* 154:    */     }
/* 155:    */     else
/* 156:    */     {
/* 157:199 */       ByteBuf buf = this.alloc.directBuffer(len);
/* 158:    */       try
/* 159:    */       {
/* 160:    */         long addr;
/* 161:    */         long addr;
/* 162:202 */         if (buf.hasMemoryAddress()) {
/* 163:203 */           addr = buf.memoryAddress();
/* 164:    */         } else {
/* 165:205 */           addr = Buffer.address(buf.nioBuffer());
/* 166:    */         }
/* 167:208 */         buf.setBytes(0, src);
/* 168:    */         
/* 169:210 */         int netWrote = SSL.writeToBIO(this.networkBIO, addr, len);
/* 170:211 */         if (netWrote >= 0)
/* 171:    */         {
/* 172:212 */           src.position(pos + netWrote);
/* 173:213 */           this.lastPrimingReadResult = SSL.readFromSSL(this.ssl, addr, 0);
/* 174:214 */           return netWrote;
/* 175:    */         }
/* 176:216 */         src.position(pos);
/* 177:    */       }
/* 178:    */       finally
/* 179:    */       {
/* 180:219 */         buf.release();
/* 181:    */       }
/* 182:    */     }
/* 183:223 */     return 0;
/* 184:    */   }
/* 185:    */   
/* 186:    */   private int readPlaintextData(ByteBuffer dst)
/* 187:    */   {
/* 188:230 */     if (dst.isDirect())
/* 189:    */     {
/* 190:231 */       int pos = dst.position();
/* 191:232 */       long addr = Buffer.address(dst) + pos;
/* 192:233 */       int len = dst.limit() - pos;
/* 193:234 */       int sslRead = SSL.readFromSSL(this.ssl, addr, len);
/* 194:235 */       if (sslRead > 0)
/* 195:    */       {
/* 196:236 */         dst.position(pos + sslRead);
/* 197:237 */         return sslRead;
/* 198:    */       }
/* 199:    */     }
/* 200:    */     else
/* 201:    */     {
/* 202:240 */       int pos = dst.position();
/* 203:241 */       int limit = dst.limit();
/* 204:242 */       int len = Math.min(18713, limit - pos);
/* 205:243 */       ByteBuf buf = this.alloc.directBuffer(len);
/* 206:    */       try
/* 207:    */       {
/* 208:    */         long addr;
/* 209:    */         long addr;
/* 210:246 */         if (buf.hasMemoryAddress()) {
/* 211:247 */           addr = buf.memoryAddress();
/* 212:    */         } else {
/* 213:249 */           addr = Buffer.address(buf.nioBuffer());
/* 214:    */         }
/* 215:252 */         int sslRead = SSL.readFromSSL(this.ssl, addr, len);
/* 216:253 */         if (sslRead > 0)
/* 217:    */         {
/* 218:254 */           dst.limit(pos + sslRead);
/* 219:255 */           buf.getBytes(0, dst);
/* 220:256 */           dst.limit(limit);
/* 221:257 */           return sslRead;
/* 222:    */         }
/* 223:    */       }
/* 224:    */       finally
/* 225:    */       {
/* 226:260 */         buf.release();
/* 227:    */       }
/* 228:    */     }
/* 229:264 */     return 0;
/* 230:    */   }
/* 231:    */   
/* 232:    */   private int readEncryptedData(ByteBuffer dst, int pending)
/* 233:    */   {
/* 234:271 */     if ((dst.isDirect()) && (dst.remaining() >= pending))
/* 235:    */     {
/* 236:272 */       int pos = dst.position();
/* 237:273 */       long addr = Buffer.address(dst) + pos;
/* 238:274 */       int bioRead = SSL.readFromBIO(this.networkBIO, addr, pending);
/* 239:275 */       if (bioRead > 0)
/* 240:    */       {
/* 241:276 */         dst.position(pos + bioRead);
/* 242:277 */         return bioRead;
/* 243:    */       }
/* 244:    */     }
/* 245:    */     else
/* 246:    */     {
/* 247:280 */       ByteBuf buf = this.alloc.directBuffer(pending);
/* 248:    */       try
/* 249:    */       {
/* 250:    */         long addr;
/* 251:    */         long addr;
/* 252:283 */         if (buf.hasMemoryAddress()) {
/* 253:284 */           addr = buf.memoryAddress();
/* 254:    */         } else {
/* 255:286 */           addr = Buffer.address(buf.nioBuffer());
/* 256:    */         }
/* 257:289 */         int bioRead = SSL.readFromBIO(this.networkBIO, addr, pending);
/* 258:290 */         if (bioRead > 0)
/* 259:    */         {
/* 260:291 */           int oldLimit = dst.limit();
/* 261:292 */           dst.limit(dst.position() + bioRead);
/* 262:293 */           buf.getBytes(0, dst);
/* 263:294 */           dst.limit(oldLimit);
/* 264:295 */           return bioRead;
/* 265:    */         }
/* 266:    */       }
/* 267:    */       finally
/* 268:    */       {
/* 269:298 */         buf.release();
/* 270:    */       }
/* 271:    */     }
/* 272:302 */     return 0;
/* 273:    */   }
/* 274:    */   
/* 275:    */   public synchronized SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int length, ByteBuffer dst)
/* 276:    */     throws SSLException
/* 277:    */   {
/* 278:310 */     if (this.destroyed != 0) {
/* 279:311 */       return new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0);
/* 280:    */     }
/* 281:315 */     if (srcs == null) {
/* 282:316 */       throw new NullPointerException("srcs");
/* 283:    */     }
/* 284:318 */     if (dst == null) {
/* 285:319 */       throw new NullPointerException("dst");
/* 286:    */     }
/* 287:322 */     if ((offset >= srcs.length) || (offset + length > srcs.length)) {
/* 288:323 */       throw new IndexOutOfBoundsException("offset: " + offset + ", length: " + length + " (expected: offset <= offset + length <= srcs.length (" + srcs.length + "))");
/* 289:    */     }
/* 290:328 */     if (dst.isReadOnly()) {
/* 291:329 */       throw new ReadOnlyBufferException();
/* 292:    */     }
/* 293:333 */     if (this.accepted == 0) {
/* 294:334 */       beginHandshakeImplicitly();
/* 295:    */     }
/* 296:339 */     SSLEngineResult.HandshakeStatus handshakeStatus = getHandshakeStatus();
/* 297:340 */     if (((!this.handshakeFinished) || (this.engineClosed)) && (handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP)) {
/* 298:341 */       return new SSLEngineResult(getEngineStatus(), SSLEngineResult.HandshakeStatus.NEED_UNWRAP, 0, 0);
/* 299:    */     }
/* 300:344 */     int bytesProduced = 0;
/* 301:    */     
/* 302:    */ 
/* 303:    */ 
/* 304:348 */     int pendingNet = SSL.pendingWrittenBytesInBIO(this.networkBIO);
/* 305:349 */     if (pendingNet > 0)
/* 306:    */     {
/* 307:351 */       int capacity = dst.remaining();
/* 308:352 */       if (capacity < pendingNet) {
/* 309:353 */         return new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, handshakeStatus, 0, bytesProduced);
/* 310:    */       }
/* 311:    */       try
/* 312:    */       {
/* 313:358 */         bytesProduced += readEncryptedData(dst, pendingNet);
/* 314:    */       }
/* 315:    */       catch (Exception e)
/* 316:    */       {
/* 317:360 */         throw new SSLException(e);
/* 318:    */       }
/* 319:366 */       if (this.isOutboundDone) {
/* 320:367 */         shutdown();
/* 321:    */       }
/* 322:370 */       return new SSLEngineResult(getEngineStatus(), getHandshakeStatus(), 0, bytesProduced);
/* 323:    */     }
/* 324:374 */     int bytesConsumed = 0;
/* 325:375 */     for (int i = offset; i < length; i++)
/* 326:    */     {
/* 327:376 */       ByteBuffer src = srcs[i];
/* 328:377 */       while (src.hasRemaining())
/* 329:    */       {
/* 330:    */         try
/* 331:    */         {
/* 332:381 */           bytesConsumed += writePlaintextData(src);
/* 333:    */         }
/* 334:    */         catch (Exception e)
/* 335:    */         {
/* 336:383 */           throw new SSLException(e);
/* 337:    */         }
/* 338:387 */         pendingNet = SSL.pendingWrittenBytesInBIO(this.networkBIO);
/* 339:388 */         if (pendingNet > 0)
/* 340:    */         {
/* 341:390 */           int capacity = dst.remaining();
/* 342:391 */           if (capacity < pendingNet) {
/* 343:392 */             return new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, getHandshakeStatus(), bytesConsumed, bytesProduced);
/* 344:    */           }
/* 345:    */           try
/* 346:    */           {
/* 347:397 */             bytesProduced += readEncryptedData(dst, pendingNet);
/* 348:    */           }
/* 349:    */           catch (Exception e)
/* 350:    */           {
/* 351:399 */             throw new SSLException(e);
/* 352:    */           }
/* 353:402 */           return new SSLEngineResult(getEngineStatus(), getHandshakeStatus(), bytesConsumed, bytesProduced);
/* 354:    */         }
/* 355:    */       }
/* 356:    */     }
/* 357:407 */     return new SSLEngineResult(getEngineStatus(), getHandshakeStatus(), bytesConsumed, bytesProduced);
/* 358:    */   }
/* 359:    */   
/* 360:    */   public synchronized SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts, int offset, int length)
/* 361:    */     throws SSLException
/* 362:    */   {
/* 363:415 */     if (this.destroyed != 0) {
/* 364:416 */       return new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0);
/* 365:    */     }
/* 366:420 */     if (src == null) {
/* 367:421 */       throw new NullPointerException("src");
/* 368:    */     }
/* 369:423 */     if (dsts == null) {
/* 370:424 */       throw new NullPointerException("dsts");
/* 371:    */     }
/* 372:426 */     if ((offset >= dsts.length) || (offset + length > dsts.length)) {
/* 373:427 */       throw new IndexOutOfBoundsException("offset: " + offset + ", length: " + length + " (expected: offset <= offset + length <= dsts.length (" + dsts.length + "))");
/* 374:    */     }
/* 375:432 */     int capacity = 0;
/* 376:433 */     int endOffset = offset + length;
/* 377:434 */     for (int i = offset; i < endOffset; i++)
/* 378:    */     {
/* 379:435 */       ByteBuffer dst = dsts[i];
/* 380:436 */       if (dst == null) {
/* 381:437 */         throw new IllegalArgumentException();
/* 382:    */       }
/* 383:439 */       if (dst.isReadOnly()) {
/* 384:440 */         throw new ReadOnlyBufferException();
/* 385:    */       }
/* 386:442 */       capacity += dst.remaining();
/* 387:    */     }
/* 388:446 */     if (this.accepted == 0) {
/* 389:447 */       beginHandshakeImplicitly();
/* 390:    */     }
/* 391:452 */     SSLEngineResult.HandshakeStatus handshakeStatus = getHandshakeStatus();
/* 392:453 */     if (((!this.handshakeFinished) || (this.engineClosed)) && (handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP)) {
/* 393:454 */       return new SSLEngineResult(getEngineStatus(), SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, 0);
/* 394:    */     }
/* 395:458 */     if (src.remaining() > 18713)
/* 396:    */     {
/* 397:459 */       this.isInboundDone = true;
/* 398:460 */       this.isOutboundDone = true;
/* 399:461 */       this.engineClosed = true;
/* 400:462 */       shutdown();
/* 401:463 */       throw ENCRYPTED_PACKET_OVERSIZED;
/* 402:    */     }
/* 403:467 */     int bytesConsumed = 0;
/* 404:468 */     this.lastPrimingReadResult = 0;
/* 405:    */     try
/* 406:    */     {
/* 407:470 */       bytesConsumed += writeEncryptedData(src);
/* 408:    */     }
/* 409:    */     catch (Exception e)
/* 410:    */     {
/* 411:472 */       throw new SSLException(e);
/* 412:    */     }
/* 413:476 */     String error = SSL.getLastError();
/* 414:477 */     if ((error != null) && (!error.startsWith("error:00000000:")))
/* 415:    */     {
/* 416:478 */       if (logger.isInfoEnabled()) {
/* 417:479 */         logger.info("SSL_read failed: primingReadResult: " + this.lastPrimingReadResult + "; OpenSSL error: '" + error + '\'');
/* 418:    */       }
/* 419:485 */       shutdown();
/* 420:486 */       throw new SSLException(error);
/* 421:    */     }
/* 422:490 */     int pendingApp = SSL.isInInit(this.ssl) == 0 ? SSL.pendingReadableBytesInSSL(this.ssl) : 0;
/* 423:493 */     if (capacity < pendingApp) {
/* 424:494 */       return new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, getHandshakeStatus(), bytesConsumed, 0);
/* 425:    */     }
/* 426:498 */     int bytesProduced = 0;
/* 427:499 */     int idx = offset;
/* 428:500 */     while (idx < endOffset)
/* 429:    */     {
/* 430:501 */       ByteBuffer dst = dsts[idx];
/* 431:502 */       if (!dst.hasRemaining())
/* 432:    */       {
/* 433:503 */         idx++;
/* 434:    */       }
/* 435:    */       else
/* 436:    */       {
/* 437:507 */         if (pendingApp <= 0) {
/* 438:    */           break;
/* 439:    */         }
/* 440:    */         int bytesRead;
/* 441:    */         try
/* 442:    */         {
/* 443:513 */           bytesRead = readPlaintextData(dst);
/* 444:    */         }
/* 445:    */         catch (Exception e)
/* 446:    */         {
/* 447:515 */           throw new SSLException(e);
/* 448:    */         }
/* 449:518 */         if (bytesRead == 0) {
/* 450:    */           break;
/* 451:    */         }
/* 452:522 */         bytesProduced += bytesRead;
/* 453:523 */         pendingApp -= bytesRead;
/* 454:525 */         if (!dst.hasRemaining()) {
/* 455:526 */           idx++;
/* 456:    */         }
/* 457:    */       }
/* 458:    */     }
/* 459:531 */     if ((!this.receivedShutdown) && ((SSL.getShutdown(this.ssl) & 0x2) == 2))
/* 460:    */     {
/* 461:532 */       this.receivedShutdown = true;
/* 462:533 */       closeOutbound();
/* 463:534 */       closeInbound();
/* 464:    */     }
/* 465:537 */     return new SSLEngineResult(getEngineStatus(), getHandshakeStatus(), bytesConsumed, bytesProduced);
/* 466:    */   }
/* 467:    */   
/* 468:    */   public Runnable getDelegatedTask()
/* 469:    */   {
/* 470:545 */     return null;
/* 471:    */   }
/* 472:    */   
/* 473:    */   public synchronized void closeInbound()
/* 474:    */     throws SSLException
/* 475:    */   {
/* 476:550 */     if (this.isInboundDone) {
/* 477:551 */       return;
/* 478:    */     }
/* 479:554 */     this.isInboundDone = true;
/* 480:555 */     this.engineClosed = true;
/* 481:557 */     if (this.accepted != 0)
/* 482:    */     {
/* 483:558 */       if (!this.receivedShutdown)
/* 484:    */       {
/* 485:559 */         shutdown();
/* 486:560 */         throw new SSLException("Inbound closed before receiving peer's close_notify: possible truncation attack?");
/* 487:    */       }
/* 488:    */     }
/* 489:    */     else {
/* 490:565 */       shutdown();
/* 491:    */     }
/* 492:    */   }
/* 493:    */   
/* 494:    */   public synchronized boolean isInboundDone()
/* 495:    */   {
/* 496:571 */     return (this.isInboundDone) || (this.engineClosed);
/* 497:    */   }
/* 498:    */   
/* 499:    */   public synchronized void closeOutbound()
/* 500:    */   {
/* 501:576 */     if (this.isOutboundDone) {
/* 502:577 */       return;
/* 503:    */     }
/* 504:580 */     this.isOutboundDone = true;
/* 505:581 */     this.engineClosed = true;
/* 506:583 */     if ((this.accepted != 0) && (this.destroyed == 0))
/* 507:    */     {
/* 508:584 */       int mode = SSL.getShutdown(this.ssl);
/* 509:585 */       if ((mode & 0x1) != 1) {
/* 510:586 */         SSL.shutdownSSL(this.ssl);
/* 511:    */       }
/* 512:    */     }
/* 513:    */     else
/* 514:    */     {
/* 515:590 */       shutdown();
/* 516:    */     }
/* 517:    */   }
/* 518:    */   
/* 519:    */   public synchronized boolean isOutboundDone()
/* 520:    */   {
/* 521:596 */     return this.isOutboundDone;
/* 522:    */   }
/* 523:    */   
/* 524:    */   public String[] getSupportedCipherSuites()
/* 525:    */   {
/* 526:601 */     return EmptyArrays.EMPTY_STRINGS;
/* 527:    */   }
/* 528:    */   
/* 529:    */   public String[] getEnabledCipherSuites()
/* 530:    */   {
/* 531:606 */     return EmptyArrays.EMPTY_STRINGS;
/* 532:    */   }
/* 533:    */   
/* 534:    */   public void setEnabledCipherSuites(String[] strings)
/* 535:    */   {
/* 536:611 */     throw new UnsupportedOperationException();
/* 537:    */   }
/* 538:    */   
/* 539:    */   public String[] getSupportedProtocols()
/* 540:    */   {
/* 541:616 */     return EmptyArrays.EMPTY_STRINGS;
/* 542:    */   }
/* 543:    */   
/* 544:    */   public String[] getEnabledProtocols()
/* 545:    */   {
/* 546:621 */     return EmptyArrays.EMPTY_STRINGS;
/* 547:    */   }
/* 548:    */   
/* 549:    */   public void setEnabledProtocols(String[] strings)
/* 550:    */   {
/* 551:626 */     throw new UnsupportedOperationException();
/* 552:    */   }
/* 553:    */   
/* 554:    */   public SSLSession getSession()
/* 555:    */   {
/* 556:631 */     SSLSession session = this.session;
/* 557:632 */     if (session == null) {
/* 558:633 */       this.session = (session = new SSLSession()
/* 559:    */       {
/* 560:    */         public byte[] getId()
/* 561:    */         {
/* 562:636 */           return String.valueOf(OpenSslEngine.this.ssl).getBytes();
/* 563:    */         }
/* 564:    */         
/* 565:    */         public SSLSessionContext getSessionContext()
/* 566:    */         {
/* 567:641 */           return null;
/* 568:    */         }
/* 569:    */         
/* 570:    */         public long getCreationTime()
/* 571:    */         {
/* 572:646 */           return 0L;
/* 573:    */         }
/* 574:    */         
/* 575:    */         public long getLastAccessedTime()
/* 576:    */         {
/* 577:651 */           return 0L;
/* 578:    */         }
/* 579:    */         
/* 580:    */         public void invalidate() {}
/* 581:    */         
/* 582:    */         public boolean isValid()
/* 583:    */         {
/* 584:660 */           return false;
/* 585:    */         }
/* 586:    */         
/* 587:    */         public void putValue(String s, Object o) {}
/* 588:    */         
/* 589:    */         public Object getValue(String s)
/* 590:    */         {
/* 591:669 */           return null;
/* 592:    */         }
/* 593:    */         
/* 594:    */         public void removeValue(String s) {}
/* 595:    */         
/* 596:    */         public String[] getValueNames()
/* 597:    */         {
/* 598:678 */           return EmptyArrays.EMPTY_STRINGS;
/* 599:    */         }
/* 600:    */         
/* 601:    */         public Certificate[] getPeerCertificates()
/* 602:    */         {
/* 603:683 */           return OpenSslEngine.EMPTY_CERTIFICATES;
/* 604:    */         }
/* 605:    */         
/* 606:    */         public Certificate[] getLocalCertificates()
/* 607:    */         {
/* 608:688 */           return OpenSslEngine.EMPTY_CERTIFICATES;
/* 609:    */         }
/* 610:    */         
/* 611:    */         public X509Certificate[] getPeerCertificateChain()
/* 612:    */         {
/* 613:693 */           return OpenSslEngine.EMPTY_X509_CERTIFICATES;
/* 614:    */         }
/* 615:    */         
/* 616:    */         public Principal getPeerPrincipal()
/* 617:    */         {
/* 618:698 */           return null;
/* 619:    */         }
/* 620:    */         
/* 621:    */         public Principal getLocalPrincipal()
/* 622:    */         {
/* 623:703 */           return null;
/* 624:    */         }
/* 625:    */         
/* 626:    */         public String getCipherSuite()
/* 627:    */         {
/* 628:708 */           return OpenSslEngine.this.cipher;
/* 629:    */         }
/* 630:    */         
/* 631:    */         public String getProtocol()
/* 632:    */         {
/* 633:714 */           String applicationProtocol = OpenSslEngine.this.applicationProtocol;
/* 634:715 */           if (applicationProtocol == null) {
/* 635:716 */             return "unknown";
/* 636:    */           }
/* 637:718 */           return "unknown:" + applicationProtocol;
/* 638:    */         }
/* 639:    */         
/* 640:    */         public String getPeerHost()
/* 641:    */         {
/* 642:724 */           return null;
/* 643:    */         }
/* 644:    */         
/* 645:    */         public int getPeerPort()
/* 646:    */         {
/* 647:729 */           return 0;
/* 648:    */         }
/* 649:    */         
/* 650:    */         public int getPacketBufferSize()
/* 651:    */         {
/* 652:734 */           return 18713;
/* 653:    */         }
/* 654:    */         
/* 655:    */         public int getApplicationBufferSize()
/* 656:    */         {
/* 657:739 */           return 16384;
/* 658:    */         }
/* 659:    */       });
/* 660:    */     }
/* 661:744 */     return session;
/* 662:    */   }
/* 663:    */   
/* 664:    */   public synchronized void beginHandshake()
/* 665:    */     throws SSLException
/* 666:    */   {
/* 667:749 */     if (this.engineClosed) {
/* 668:750 */       throw ENGINE_CLOSED;
/* 669:    */     }
/* 670:753 */     switch (this.accepted)
/* 671:    */     {
/* 672:    */     case 0: 
/* 673:755 */       SSL.doHandshake(this.ssl);
/* 674:756 */       this.accepted = 2;
/* 675:757 */       break;
/* 676:    */     case 1: 
/* 677:765 */       this.accepted = 2;
/* 678:766 */       break;
/* 679:    */     case 2: 
/* 680:768 */       throw RENEGOTIATION_UNSUPPORTED;
/* 681:    */     default: 
/* 682:770 */       throw new Error();
/* 683:    */     }
/* 684:    */   }
/* 685:    */   
/* 686:    */   private synchronized void beginHandshakeImplicitly()
/* 687:    */     throws SSLException
/* 688:    */   {
/* 689:775 */     if (this.engineClosed) {
/* 690:776 */       throw ENGINE_CLOSED;
/* 691:    */     }
/* 692:779 */     if (this.accepted == 0)
/* 693:    */     {
/* 694:780 */       SSL.doHandshake(this.ssl);
/* 695:781 */       this.accepted = 1;
/* 696:    */     }
/* 697:    */   }
/* 698:    */   
/* 699:    */   private SSLEngineResult.Status getEngineStatus()
/* 700:    */   {
/* 701:786 */     return this.engineClosed ? SSLEngineResult.Status.CLOSED : SSLEngineResult.Status.OK;
/* 702:    */   }
/* 703:    */   
/* 704:    */   public synchronized SSLEngineResult.HandshakeStatus getHandshakeStatus()
/* 705:    */   {
/* 706:791 */     if ((this.accepted == 0) || (this.destroyed != 0)) {
/* 707:792 */       return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
/* 708:    */     }
/* 709:796 */     if (!this.handshakeFinished)
/* 710:    */     {
/* 711:798 */       if (SSL.pendingWrittenBytesInBIO(this.networkBIO) != 0) {
/* 712:799 */         return SSLEngineResult.HandshakeStatus.NEED_WRAP;
/* 713:    */       }
/* 714:804 */       if (SSL.isInInit(this.ssl) == 0)
/* 715:    */       {
/* 716:805 */         this.handshakeFinished = true;
/* 717:806 */         this.cipher = SSL.getCipherForSSL(this.ssl);
/* 718:807 */         String applicationProtocol = SSL.getNextProtoNegotiated(this.ssl);
/* 719:808 */         if (applicationProtocol == null) {
/* 720:809 */           applicationProtocol = this.fallbackApplicationProtocol;
/* 721:    */         }
/* 722:811 */         if (applicationProtocol != null) {
/* 723:812 */           this.applicationProtocol = applicationProtocol.replace(':', '_');
/* 724:    */         } else {
/* 725:814 */           this.applicationProtocol = null;
/* 726:    */         }
/* 727:816 */         return SSLEngineResult.HandshakeStatus.FINISHED;
/* 728:    */       }
/* 729:821 */       return SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
/* 730:    */     }
/* 731:825 */     if (this.engineClosed)
/* 732:    */     {
/* 733:827 */       if (SSL.pendingWrittenBytesInBIO(this.networkBIO) != 0) {
/* 734:828 */         return SSLEngineResult.HandshakeStatus.NEED_WRAP;
/* 735:    */       }
/* 736:832 */       return SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
/* 737:    */     }
/* 738:835 */     return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
/* 739:    */   }
/* 740:    */   
/* 741:    */   public void setUseClientMode(boolean clientMode)
/* 742:    */   {
/* 743:840 */     if (clientMode) {
/* 744:841 */       throw new UnsupportedOperationException();
/* 745:    */     }
/* 746:    */   }
/* 747:    */   
/* 748:    */   public boolean getUseClientMode()
/* 749:    */   {
/* 750:847 */     return false;
/* 751:    */   }
/* 752:    */   
/* 753:    */   public void setNeedClientAuth(boolean b)
/* 754:    */   {
/* 755:852 */     if (b) {
/* 756:853 */       throw new UnsupportedOperationException();
/* 757:    */     }
/* 758:    */   }
/* 759:    */   
/* 760:    */   public boolean getNeedClientAuth()
/* 761:    */   {
/* 762:859 */     return false;
/* 763:    */   }
/* 764:    */   
/* 765:    */   public void setWantClientAuth(boolean b)
/* 766:    */   {
/* 767:864 */     if (b) {
/* 768:865 */       throw new UnsupportedOperationException();
/* 769:    */     }
/* 770:    */   }
/* 771:    */   
/* 772:    */   public boolean getWantClientAuth()
/* 773:    */   {
/* 774:871 */     return false;
/* 775:    */   }
/* 776:    */   
/* 777:    */   public void setEnableSessionCreation(boolean b)
/* 778:    */   {
/* 779:876 */     if (b) {
/* 780:877 */       throw new UnsupportedOperationException();
/* 781:    */     }
/* 782:    */   }
/* 783:    */   
/* 784:    */   public boolean getEnableSessionCreation()
/* 785:    */   {
/* 786:883 */     return false;
/* 787:    */   }
/* 788:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.OpenSslEngine
 * JD-Core Version:    0.7.0.1
 */