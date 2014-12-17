/*   1:    */ package io.netty.channel.socket.nio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.ByteBufHolder;
/*   6:    */ import io.netty.channel.AddressedEnvelope;
/*   7:    */ import io.netty.channel.ChannelException;
/*   8:    */ import io.netty.channel.ChannelFuture;
/*   9:    */ import io.netty.channel.ChannelMetadata;
/*  10:    */ import io.netty.channel.ChannelOption;
/*  11:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  12:    */ import io.netty.channel.ChannelPromise;
/*  13:    */ import io.netty.channel.DefaultAddressedEnvelope;
/*  14:    */ import io.netty.channel.RecvByteBufAllocator;
/*  15:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  16:    */ import io.netty.channel.nio.AbstractNioMessageChannel;
/*  17:    */ import io.netty.channel.socket.DatagramChannelConfig;
/*  18:    */ import io.netty.channel.socket.DatagramPacket;
/*  19:    */ import io.netty.channel.socket.InternetProtocolFamily;
/*  20:    */ import io.netty.util.internal.PlatformDependent;
/*  21:    */ import io.netty.util.internal.StringUtil;
/*  22:    */ import java.io.IOException;
/*  23:    */ import java.net.DatagramSocket;
/*  24:    */ import java.net.InetAddress;
/*  25:    */ import java.net.InetSocketAddress;
/*  26:    */ import java.net.NetworkInterface;
/*  27:    */ import java.net.SocketAddress;
/*  28:    */ import java.net.SocketException;
/*  29:    */ import java.nio.ByteBuffer;
/*  30:    */ import java.nio.channels.MembershipKey;
/*  31:    */ import java.nio.channels.spi.SelectorProvider;
/*  32:    */ import java.util.ArrayList;
/*  33:    */ import java.util.HashMap;
/*  34:    */ import java.util.Iterator;
/*  35:    */ import java.util.List;
/*  36:    */ import java.util.Map;
/*  37:    */ 
/*  38:    */ public final class NioDatagramChannel
/*  39:    */   extends AbstractNioMessageChannel
/*  40:    */   implements io.netty.channel.socket.DatagramChannel
/*  41:    */ {
/*  42: 65 */   private static final ChannelMetadata METADATA = new ChannelMetadata(true);
/*  43: 66 */   private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
/*  44:    */   private final DatagramChannelConfig config;
/*  45:    */   private Map<InetAddress, List<MembershipKey>> memberships;
/*  46:    */   private RecvByteBufAllocator.Handle allocHandle;
/*  47:    */   
/*  48:    */   private static java.nio.channels.DatagramChannel newSocket(SelectorProvider provider)
/*  49:    */   {
/*  50:    */     try
/*  51:    */     {
/*  52: 81 */       return provider.openDatagramChannel();
/*  53:    */     }
/*  54:    */     catch (IOException e)
/*  55:    */     {
/*  56: 83 */       throw new ChannelException("Failed to open a socket.", e);
/*  57:    */     }
/*  58:    */   }
/*  59:    */   
/*  60:    */   private static java.nio.channels.DatagramChannel newSocket(SelectorProvider provider, InternetProtocolFamily ipFamily)
/*  61:    */   {
/*  62: 88 */     if (ipFamily == null) {
/*  63: 89 */       return newSocket(provider);
/*  64:    */     }
/*  65: 92 */     checkJavaVersion();
/*  66:    */     try
/*  67:    */     {
/*  68: 95 */       return provider.openDatagramChannel(ProtocolFamilyConverter.convert(ipFamily));
/*  69:    */     }
/*  70:    */     catch (IOException e)
/*  71:    */     {
/*  72: 97 */       throw new ChannelException("Failed to open a socket.", e);
/*  73:    */     }
/*  74:    */   }
/*  75:    */   
/*  76:    */   private static void checkJavaVersion()
/*  77:    */   {
/*  78:102 */     if (PlatformDependent.javaVersion() < 7) {
/*  79:103 */       throw new UnsupportedOperationException("Only supported on java 7+.");
/*  80:    */     }
/*  81:    */   }
/*  82:    */   
/*  83:    */   public NioDatagramChannel()
/*  84:    */   {
/*  85:111 */     this(newSocket(DEFAULT_SELECTOR_PROVIDER));
/*  86:    */   }
/*  87:    */   
/*  88:    */   public NioDatagramChannel(SelectorProvider provider)
/*  89:    */   {
/*  90:119 */     this(newSocket(provider));
/*  91:    */   }
/*  92:    */   
/*  93:    */   public NioDatagramChannel(InternetProtocolFamily ipFamily)
/*  94:    */   {
/*  95:127 */     this(newSocket(DEFAULT_SELECTOR_PROVIDER, ipFamily));
/*  96:    */   }
/*  97:    */   
/*  98:    */   public NioDatagramChannel(SelectorProvider provider, InternetProtocolFamily ipFamily)
/*  99:    */   {
/* 100:136 */     this(newSocket(provider, ipFamily));
/* 101:    */   }
/* 102:    */   
/* 103:    */   public NioDatagramChannel(java.nio.channels.DatagramChannel socket)
/* 104:    */   {
/* 105:143 */     super(null, socket, 1);
/* 106:144 */     this.config = new NioDatagramChannelConfig(this, socket);
/* 107:    */   }
/* 108:    */   
/* 109:    */   public ChannelMetadata metadata()
/* 110:    */   {
/* 111:149 */     return METADATA;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public DatagramChannelConfig config()
/* 115:    */   {
/* 116:154 */     return this.config;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public boolean isActive()
/* 120:    */   {
/* 121:159 */     java.nio.channels.DatagramChannel ch = javaChannel();
/* 122:160 */     return (ch.isOpen()) && (((((Boolean)this.config.getOption(ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION)).booleanValue()) && (isRegistered())) || (ch.socket().isBound()));
/* 123:    */   }
/* 124:    */   
/* 125:    */   public boolean isConnected()
/* 126:    */   {
/* 127:167 */     return javaChannel().isConnected();
/* 128:    */   }
/* 129:    */   
/* 130:    */   protected java.nio.channels.DatagramChannel javaChannel()
/* 131:    */   {
/* 132:172 */     return (java.nio.channels.DatagramChannel)super.javaChannel();
/* 133:    */   }
/* 134:    */   
/* 135:    */   protected SocketAddress localAddress0()
/* 136:    */   {
/* 137:177 */     return javaChannel().socket().getLocalSocketAddress();
/* 138:    */   }
/* 139:    */   
/* 140:    */   protected SocketAddress remoteAddress0()
/* 141:    */   {
/* 142:182 */     return javaChannel().socket().getRemoteSocketAddress();
/* 143:    */   }
/* 144:    */   
/* 145:    */   protected void doBind(SocketAddress localAddress)
/* 146:    */     throws Exception
/* 147:    */   {
/* 148:187 */     javaChannel().socket().bind(localAddress);
/* 149:    */   }
/* 150:    */   
/* 151:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 152:    */     throws Exception
/* 153:    */   {
/* 154:193 */     if (localAddress != null) {
/* 155:194 */       javaChannel().socket().bind(localAddress);
/* 156:    */     }
/* 157:197 */     boolean success = false;
/* 158:    */     try
/* 159:    */     {
/* 160:199 */       javaChannel().connect(remoteAddress);
/* 161:200 */       success = true;
/* 162:201 */       return true;
/* 163:    */     }
/* 164:    */     finally
/* 165:    */     {
/* 166:203 */       if (!success) {
/* 167:204 */         doClose();
/* 168:    */       }
/* 169:    */     }
/* 170:    */   }
/* 171:    */   
/* 172:    */   protected void doFinishConnect()
/* 173:    */     throws Exception
/* 174:    */   {
/* 175:211 */     throw new Error();
/* 176:    */   }
/* 177:    */   
/* 178:    */   protected void doDisconnect()
/* 179:    */     throws Exception
/* 180:    */   {
/* 181:216 */     javaChannel().disconnect();
/* 182:    */   }
/* 183:    */   
/* 184:    */   protected void doClose()
/* 185:    */     throws Exception
/* 186:    */   {
/* 187:221 */     javaChannel().close();
/* 188:    */   }
/* 189:    */   
/* 190:    */   protected int doReadMessages(List<Object> buf)
/* 191:    */     throws Exception
/* 192:    */   {
/* 193:226 */     java.nio.channels.DatagramChannel ch = javaChannel();
/* 194:227 */     DatagramChannelConfig config = config();
/* 195:228 */     RecvByteBufAllocator.Handle allocHandle = this.allocHandle;
/* 196:229 */     if (allocHandle == null) {
/* 197:230 */       this.allocHandle = (allocHandle = config.getRecvByteBufAllocator().newHandle());
/* 198:    */     }
/* 199:232 */     ByteBuf data = allocHandle.allocate(config.getAllocator());
/* 200:233 */     boolean free = true;
/* 201:    */     try
/* 202:    */     {
/* 203:235 */       ByteBuffer nioData = data.internalNioBuffer(data.writerIndex(), data.writableBytes());
/* 204:236 */       pos = nioData.position();
/* 205:237 */       InetSocketAddress remoteAddress = (InetSocketAddress)ch.receive(nioData);
/* 206:238 */       if (remoteAddress == null) {
/* 207:239 */         return 0;
/* 208:    */       }
/* 209:242 */       int readBytes = nioData.position() - pos;
/* 210:243 */       data.writerIndex(data.writerIndex() + readBytes);
/* 211:244 */       allocHandle.record(readBytes);
/* 212:    */       
/* 213:246 */       buf.add(new DatagramPacket(data, localAddress(), remoteAddress));
/* 214:247 */       free = false;
/* 215:248 */       return 1;
/* 216:    */     }
/* 217:    */     catch (Throwable cause)
/* 218:    */     {
/* 219:    */       int pos;
/* 220:250 */       PlatformDependent.throwException(cause);
/* 221:251 */       return -1;
/* 222:    */     }
/* 223:    */     finally
/* 224:    */     {
/* 225:253 */       if (free) {
/* 226:254 */         data.release();
/* 227:    */       }
/* 228:    */     }
/* 229:    */   }
/* 230:    */   
/* 231:    */   protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in)
/* 232:    */     throws Exception
/* 233:    */   {
/* 234:    */     Object m;
/* 235:    */     Object m;
/* 236:    */     SocketAddress remoteAddress;
/* 237:264 */     if ((msg instanceof AddressedEnvelope))
/* 238:    */     {
/* 239:266 */       AddressedEnvelope<Object, SocketAddress> envelope = (AddressedEnvelope)msg;
/* 240:267 */       SocketAddress remoteAddress = envelope.recipient();
/* 241:268 */       m = envelope.content();
/* 242:    */     }
/* 243:    */     else
/* 244:    */     {
/* 245:270 */       m = msg;
/* 246:271 */       remoteAddress = null;
/* 247:    */     }
/* 248:    */     ByteBuf data;
/* 249:274 */     if ((m instanceof ByteBufHolder))
/* 250:    */     {
/* 251:275 */       data = ((ByteBufHolder)m).content();
/* 252:    */     }
/* 253:    */     else
/* 254:    */     {
/* 255:    */       ByteBuf data;
/* 256:276 */       if ((m instanceof ByteBuf)) {
/* 257:277 */         data = (ByteBuf)m;
/* 258:    */       } else {
/* 259:279 */         throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg));
/* 260:    */       }
/* 261:    */     }
/* 262:    */     ByteBuf data;
/* 263:282 */     int dataLen = data.readableBytes();
/* 264:283 */     if (dataLen == 0) {
/* 265:284 */       return true;
/* 266:    */     }
/* 267:287 */     ByteBufAllocator alloc = alloc();
/* 268:288 */     boolean needsCopy = data.nioBufferCount() != 1;
/* 269:289 */     if ((!needsCopy) && 
/* 270:290 */       (!data.isDirect()) && (alloc.isDirectBufferPooled())) {
/* 271:291 */       needsCopy = true;
/* 272:    */     }
/* 273:    */     ByteBuffer nioData;
/* 274:    */     ByteBuffer nioData;
/* 275:295 */     if (!needsCopy)
/* 276:    */     {
/* 277:296 */       nioData = data.nioBuffer();
/* 278:    */     }
/* 279:    */     else
/* 280:    */     {
/* 281:298 */       data = alloc.directBuffer(dataLen).writeBytes(data);
/* 282:299 */       nioData = data.nioBuffer();
/* 283:    */     }
/* 284:    */     int writtenBytes;
/* 285:    */     int writtenBytes;
/* 286:303 */     if (remoteAddress != null) {
/* 287:304 */       writtenBytes = javaChannel().send(nioData, remoteAddress);
/* 288:    */     } else {
/* 289:306 */       writtenBytes = javaChannel().write(nioData);
/* 290:    */     }
/* 291:309 */     boolean done = writtenBytes > 0;
/* 292:310 */     if (needsCopy) {
/* 293:313 */       if (remoteAddress == null) {
/* 294:315 */         in.current(data);
/* 295:317 */       } else if (!done) {
/* 296:319 */         in.current(new DefaultAddressedEnvelope(data, remoteAddress));
/* 297:    */       } else {
/* 298:322 */         in.current(data);
/* 299:    */       }
/* 300:    */     }
/* 301:326 */     return done;
/* 302:    */   }
/* 303:    */   
/* 304:    */   public InetSocketAddress localAddress()
/* 305:    */   {
/* 306:331 */     return (InetSocketAddress)super.localAddress();
/* 307:    */   }
/* 308:    */   
/* 309:    */   public InetSocketAddress remoteAddress()
/* 310:    */   {
/* 311:336 */     return (InetSocketAddress)super.remoteAddress();
/* 312:    */   }
/* 313:    */   
/* 314:    */   public ChannelFuture joinGroup(InetAddress multicastAddress)
/* 315:    */   {
/* 316:341 */     return joinGroup(multicastAddress, newPromise());
/* 317:    */   }
/* 318:    */   
/* 319:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, ChannelPromise promise)
/* 320:    */   {
/* 321:    */     try
/* 322:    */     {
/* 323:347 */       return joinGroup(multicastAddress, NetworkInterface.getByInetAddress(localAddress().getAddress()), null, promise);
/* 324:    */     }
/* 325:    */     catch (SocketException e)
/* 326:    */     {
/* 327:352 */       promise.setFailure(e);
/* 328:    */     }
/* 329:354 */     return promise;
/* 330:    */   }
/* 331:    */   
/* 332:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/* 333:    */   {
/* 334:360 */     return joinGroup(multicastAddress, networkInterface, newPromise());
/* 335:    */   }
/* 336:    */   
/* 337:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/* 338:    */   {
/* 339:367 */     return joinGroup(multicastAddress.getAddress(), networkInterface, null, promise);
/* 340:    */   }
/* 341:    */   
/* 342:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/* 343:    */   {
/* 344:373 */     return joinGroup(multicastAddress, networkInterface, source, newPromise());
/* 345:    */   }
/* 346:    */   
/* 347:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/* 348:    */   {
/* 349:    */     
/* 350:383 */     if (multicastAddress == null) {
/* 351:384 */       throw new NullPointerException("multicastAddress");
/* 352:    */     }
/* 353:387 */     if (networkInterface == null) {
/* 354:388 */       throw new NullPointerException("networkInterface");
/* 355:    */     }
/* 356:    */     try
/* 357:    */     {
/* 358:    */       MembershipKey key;
/* 359:    */       MembershipKey key;
/* 360:393 */       if (source == null) {
/* 361:394 */         key = javaChannel().join(multicastAddress, networkInterface);
/* 362:    */       } else {
/* 363:396 */         key = javaChannel().join(multicastAddress, networkInterface, source);
/* 364:    */       }
/* 365:399 */       synchronized (this)
/* 366:    */       {
/* 367:400 */         List<MembershipKey> keys = null;
/* 368:401 */         if (this.memberships == null) {
/* 369:402 */           this.memberships = new HashMap();
/* 370:    */         } else {
/* 371:404 */           keys = (List)this.memberships.get(multicastAddress);
/* 372:    */         }
/* 373:406 */         if (keys == null)
/* 374:    */         {
/* 375:407 */           keys = new ArrayList();
/* 376:408 */           this.memberships.put(multicastAddress, keys);
/* 377:    */         }
/* 378:410 */         keys.add(key);
/* 379:    */       }
/* 380:413 */       promise.setSuccess();
/* 381:    */     }
/* 382:    */     catch (Throwable e)
/* 383:    */     {
/* 384:415 */       promise.setFailure(e);
/* 385:    */     }
/* 386:418 */     return promise;
/* 387:    */   }
/* 388:    */   
/* 389:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress)
/* 390:    */   {
/* 391:423 */     return leaveGroup(multicastAddress, newPromise());
/* 392:    */   }
/* 393:    */   
/* 394:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, ChannelPromise promise)
/* 395:    */   {
/* 396:    */     try
/* 397:    */     {
/* 398:429 */       return leaveGroup(multicastAddress, NetworkInterface.getByInetAddress(localAddress().getAddress()), null, promise);
/* 399:    */     }
/* 400:    */     catch (SocketException e)
/* 401:    */     {
/* 402:432 */       promise.setFailure(e);
/* 403:    */     }
/* 404:434 */     return promise;
/* 405:    */   }
/* 406:    */   
/* 407:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/* 408:    */   {
/* 409:440 */     return leaveGroup(multicastAddress, networkInterface, newPromise());
/* 410:    */   }
/* 411:    */   
/* 412:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/* 413:    */   {
/* 414:447 */     return leaveGroup(multicastAddress.getAddress(), networkInterface, null, promise);
/* 415:    */   }
/* 416:    */   
/* 417:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/* 418:    */   {
/* 419:453 */     return leaveGroup(multicastAddress, networkInterface, source, newPromise());
/* 420:    */   }
/* 421:    */   
/* 422:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/* 423:    */   {
/* 424:    */     
/* 425:462 */     if (multicastAddress == null) {
/* 426:463 */       throw new NullPointerException("multicastAddress");
/* 427:    */     }
/* 428:465 */     if (networkInterface == null) {
/* 429:466 */       throw new NullPointerException("networkInterface");
/* 430:    */     }
/* 431:469 */     synchronized (this)
/* 432:    */     {
/* 433:470 */       if (this.memberships != null)
/* 434:    */       {
/* 435:471 */         List<MembershipKey> keys = (List)this.memberships.get(multicastAddress);
/* 436:472 */         if (keys != null)
/* 437:    */         {
/* 438:473 */           Iterator<MembershipKey> keyIt = keys.iterator();
/* 439:475 */           while (keyIt.hasNext())
/* 440:    */           {
/* 441:476 */             MembershipKey key = (MembershipKey)keyIt.next();
/* 442:477 */             if ((networkInterface.equals(key.networkInterface())) && (
/* 443:478 */               ((source == null) && (key.sourceAddress() == null)) || ((source != null) && (source.equals(key.sourceAddress())))))
/* 444:    */             {
/* 445:480 */               key.drop();
/* 446:481 */               keyIt.remove();
/* 447:    */             }
/* 448:    */           }
/* 449:485 */           if (keys.isEmpty()) {
/* 450:486 */             this.memberships.remove(multicastAddress);
/* 451:    */           }
/* 452:    */         }
/* 453:    */       }
/* 454:    */     }
/* 455:492 */     promise.setSuccess();
/* 456:493 */     return promise;
/* 457:    */   }
/* 458:    */   
/* 459:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock)
/* 460:    */   {
/* 461:503 */     return block(multicastAddress, networkInterface, sourceToBlock, newPromise());
/* 462:    */   }
/* 463:    */   
/* 464:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock, ChannelPromise promise)
/* 465:    */   {
/* 466:    */     
/* 467:515 */     if (multicastAddress == null) {
/* 468:516 */       throw new NullPointerException("multicastAddress");
/* 469:    */     }
/* 470:518 */     if (sourceToBlock == null) {
/* 471:519 */       throw new NullPointerException("sourceToBlock");
/* 472:    */     }
/* 473:522 */     if (networkInterface == null) {
/* 474:523 */       throw new NullPointerException("networkInterface");
/* 475:    */     }
/* 476:525 */     synchronized (this)
/* 477:    */     {
/* 478:526 */       if (this.memberships != null)
/* 479:    */       {
/* 480:527 */         List<MembershipKey> keys = (List)this.memberships.get(multicastAddress);
/* 481:528 */         for (MembershipKey key : keys) {
/* 482:529 */           if (networkInterface.equals(key.networkInterface())) {
/* 483:    */             try
/* 484:    */             {
/* 485:531 */               key.block(sourceToBlock);
/* 486:    */             }
/* 487:    */             catch (IOException e)
/* 488:    */             {
/* 489:533 */               promise.setFailure(e);
/* 490:    */             }
/* 491:    */           }
/* 492:    */         }
/* 493:    */       }
/* 494:    */     }
/* 495:539 */     promise.setSuccess();
/* 496:540 */     return promise;
/* 497:    */   }
/* 498:    */   
/* 499:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock)
/* 500:    */   {
/* 501:549 */     return block(multicastAddress, sourceToBlock, newPromise());
/* 502:    */   }
/* 503:    */   
/* 504:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock, ChannelPromise promise)
/* 505:    */   {
/* 506:    */     try
/* 507:    */     {
/* 508:560 */       return block(multicastAddress, NetworkInterface.getByInetAddress(localAddress().getAddress()), sourceToBlock, promise);
/* 509:    */     }
/* 510:    */     catch (SocketException e)
/* 511:    */     {
/* 512:565 */       promise.setFailure(e);
/* 513:    */     }
/* 514:567 */     return promise;
/* 515:    */   }
/* 516:    */   
/* 517:    */   protected void setReadPending(boolean readPending)
/* 518:    */   {
/* 519:572 */     super.setReadPending(readPending);
/* 520:    */   }
/* 521:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.socket.nio.NioDatagramChannel
 * JD-Core Version:    0.7.0.1
 */