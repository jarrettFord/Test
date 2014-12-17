/*   1:    */ package io.netty.channel.sctp.oio;
/*   2:    */ 
/*   3:    */ import com.sun.nio.sctp.Association;
/*   4:    */ import com.sun.nio.sctp.MessageInfo;
/*   5:    */ import com.sun.nio.sctp.NotificationHandler;
/*   6:    */ import io.netty.buffer.ByteBuf;
/*   7:    */ import io.netty.channel.Channel;
/*   8:    */ import io.netty.channel.ChannelException;
/*   9:    */ import io.netty.channel.ChannelFuture;
/*  10:    */ import io.netty.channel.ChannelMetadata;
/*  11:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  12:    */ import io.netty.channel.ChannelPromise;
/*  13:    */ import io.netty.channel.EventLoop;
/*  14:    */ import io.netty.channel.RecvByteBufAllocator;
/*  15:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  16:    */ import io.netty.channel.oio.AbstractOioMessageChannel;
/*  17:    */ import io.netty.channel.sctp.DefaultSctpChannelConfig;
/*  18:    */ import io.netty.channel.sctp.SctpChannelConfig;
/*  19:    */ import io.netty.channel.sctp.SctpMessage;
/*  20:    */ import io.netty.channel.sctp.SctpNotificationHandler;
/*  21:    */ import io.netty.channel.sctp.SctpServerChannel;
/*  22:    */ import io.netty.util.internal.PlatformDependent;
/*  23:    */ import io.netty.util.internal.logging.InternalLogger;
/*  24:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  25:    */ import java.io.IOException;
/*  26:    */ import java.net.InetAddress;
/*  27:    */ import java.net.InetSocketAddress;
/*  28:    */ import java.net.SocketAddress;
/*  29:    */ import java.nio.ByteBuffer;
/*  30:    */ import java.nio.channels.SelectionKey;
/*  31:    */ import java.nio.channels.Selector;
/*  32:    */ import java.util.Collections;
/*  33:    */ import java.util.Iterator;
/*  34:    */ import java.util.LinkedHashSet;
/*  35:    */ import java.util.List;
/*  36:    */ import java.util.Set;
/*  37:    */ 
/*  38:    */ public class OioSctpChannel
/*  39:    */   extends AbstractOioMessageChannel
/*  40:    */   implements io.netty.channel.sctp.SctpChannel
/*  41:    */ {
/*  42: 63 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(OioSctpChannel.class);
/*  43: 66 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  44:    */   private final com.sun.nio.sctp.SctpChannel ch;
/*  45:    */   private final SctpChannelConfig config;
/*  46:    */   private final Selector readSelector;
/*  47:    */   private final Selector writeSelector;
/*  48:    */   private final Selector connectSelector;
/*  49:    */   private final NotificationHandler<?> notificationHandler;
/*  50:    */   private RecvByteBufAllocator.Handle allocHandle;
/*  51:    */   
/*  52:    */   private static com.sun.nio.sctp.SctpChannel openChannel()
/*  53:    */   {
/*  54:    */     try
/*  55:    */     {
/*  56: 81 */       return com.sun.nio.sctp.SctpChannel.open();
/*  57:    */     }
/*  58:    */     catch (IOException e)
/*  59:    */     {
/*  60: 83 */       throw new ChannelException("Failed to open a sctp channel.", e);
/*  61:    */     }
/*  62:    */   }
/*  63:    */   
/*  64:    */   public OioSctpChannel()
/*  65:    */   {
/*  66: 91 */     this(openChannel());
/*  67:    */   }
/*  68:    */   
/*  69:    */   public OioSctpChannel(com.sun.nio.sctp.SctpChannel ch)
/*  70:    */   {
/*  71:100 */     this(null, ch);
/*  72:    */   }
/*  73:    */   
/*  74:    */   public OioSctpChannel(Channel parent, com.sun.nio.sctp.SctpChannel ch)
/*  75:    */   {
/*  76:111 */     super(parent);
/*  77:112 */     this.ch = ch;
/*  78:113 */     boolean success = false;
/*  79:    */     try
/*  80:    */     {
/*  81:115 */       ch.configureBlocking(false);
/*  82:116 */       this.readSelector = Selector.open();
/*  83:117 */       this.writeSelector = Selector.open();
/*  84:118 */       this.connectSelector = Selector.open();
/*  85:    */       
/*  86:120 */       ch.register(this.readSelector, 1);
/*  87:121 */       ch.register(this.writeSelector, 4);
/*  88:122 */       ch.register(this.connectSelector, 8);
/*  89:    */       
/*  90:124 */       this.config = new OioSctpChannelConfig(this, ch, null);
/*  91:125 */       this.notificationHandler = new SctpNotificationHandler(this);
/*  92:126 */       success = true; return;
/*  93:    */     }
/*  94:    */     catch (Exception e)
/*  95:    */     {
/*  96:128 */       throw new ChannelException("failed to initialize a sctp channel", e);
/*  97:    */     }
/*  98:    */     finally
/*  99:    */     {
/* 100:130 */       if (!success) {
/* 101:    */         try
/* 102:    */         {
/* 103:132 */           ch.close();
/* 104:    */         }
/* 105:    */         catch (IOException e)
/* 106:    */         {
/* 107:134 */           logger.warn("Failed to close a sctp channel.", e);
/* 108:    */         }
/* 109:    */       }
/* 110:    */     }
/* 111:    */   }
/* 112:    */   
/* 113:    */   public InetSocketAddress localAddress()
/* 114:    */   {
/* 115:142 */     return (InetSocketAddress)super.localAddress();
/* 116:    */   }
/* 117:    */   
/* 118:    */   public InetSocketAddress remoteAddress()
/* 119:    */   {
/* 120:147 */     return (InetSocketAddress)super.remoteAddress();
/* 121:    */   }
/* 122:    */   
/* 123:    */   public SctpServerChannel parent()
/* 124:    */   {
/* 125:152 */     return (SctpServerChannel)super.parent();
/* 126:    */   }
/* 127:    */   
/* 128:    */   public ChannelMetadata metadata()
/* 129:    */   {
/* 130:157 */     return METADATA;
/* 131:    */   }
/* 132:    */   
/* 133:    */   public SctpChannelConfig config()
/* 134:    */   {
/* 135:162 */     return this.config;
/* 136:    */   }
/* 137:    */   
/* 138:    */   public boolean isOpen()
/* 139:    */   {
/* 140:167 */     return this.ch.isOpen();
/* 141:    */   }
/* 142:    */   
/* 143:    */   protected int doReadMessages(List<Object> msgs)
/* 144:    */     throws Exception
/* 145:    */   {
/* 146:172 */     if (!this.readSelector.isOpen()) {
/* 147:173 */       return 0;
/* 148:    */     }
/* 149:176 */     int readMessages = 0;
/* 150:    */     
/* 151:178 */     int selectedKeys = this.readSelector.select(1000L);
/* 152:179 */     boolean keysSelected = selectedKeys > 0;
/* 153:181 */     if (!keysSelected) {
/* 154:182 */       return readMessages;
/* 155:    */     }
/* 156:185 */     Set<SelectionKey> reableKeys = this.readSelector.selectedKeys();
/* 157:    */     try
/* 158:    */     {
/* 159:187 */       for (SelectionKey ignored : reableKeys)
/* 160:    */       {
/* 161:188 */         RecvByteBufAllocator.Handle allocHandle = this.allocHandle;
/* 162:189 */         if (allocHandle == null) {
/* 163:190 */           this.allocHandle = (allocHandle = config().getRecvByteBufAllocator().newHandle());
/* 164:    */         }
/* 165:192 */         ByteBuf buffer = allocHandle.allocate(config().getAllocator());
/* 166:193 */         boolean free = true;
/* 167:    */         try
/* 168:    */         {
/* 169:196 */           ByteBuffer data = buffer.nioBuffer(buffer.writerIndex(), buffer.writableBytes());
/* 170:197 */           MessageInfo messageInfo = this.ch.receive(data, null, this.notificationHandler);
/* 171:198 */           if (messageInfo == null)
/* 172:    */           {
/* 173:199 */             int i = readMessages;
/* 174:    */             
/* 175:    */ 
/* 176:    */ 
/* 177:    */ 
/* 178:    */ 
/* 179:    */ 
/* 180:    */ 
/* 181:    */ 
/* 182:    */ 
/* 183:209 */             int bytesRead = buffer.readableBytes();
/* 184:210 */             allocHandle.record(bytesRead);
/* 185:211 */             if (free) {
/* 186:212 */               buffer.release();
/* 187:    */             }
/* 188:217 */             return i;
/* 189:    */           }
/* 190:202 */           data.flip();
/* 191:203 */           msgs.add(new SctpMessage(messageInfo, buffer.writerIndex(buffer.writerIndex() + data.remaining())));
/* 192:204 */           free = false;
/* 193:205 */           readMessages++;
/* 194:    */         }
/* 195:    */         catch (Throwable cause)
/* 196:    */         {
/* 197:    */           int bytesRead;
/* 198:207 */           PlatformDependent.throwException(cause);
/* 199:    */         }
/* 200:    */         finally
/* 201:    */         {
/* 202:    */           int bytesRead;
/* 203:209 */           int bytesRead = buffer.readableBytes();
/* 204:210 */           allocHandle.record(bytesRead);
/* 205:211 */           if (free) {
/* 206:212 */             buffer.release();
/* 207:    */           }
/* 208:    */         }
/* 209:    */       }
/* 210:    */     }
/* 211:    */     finally
/* 212:    */     {
/* 213:217 */       reableKeys.clear();
/* 214:    */     }
/* 215:219 */     return readMessages;
/* 216:    */   }
/* 217:    */   
/* 218:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 219:    */     throws Exception
/* 220:    */   {
/* 221:224 */     if (!this.writeSelector.isOpen()) {
/* 222:225 */       return;
/* 223:    */     }
/* 224:227 */     int size = in.size();
/* 225:228 */     int selectedKeys = this.writeSelector.select(1000L);
/* 226:229 */     if (selectedKeys > 0)
/* 227:    */     {
/* 228:230 */       Set<SelectionKey> writableKeys = this.writeSelector.selectedKeys();
/* 229:231 */       if (writableKeys.isEmpty()) {
/* 230:232 */         return;
/* 231:    */       }
/* 232:234 */       Iterator<SelectionKey> writableKeysIt = writableKeys.iterator();
/* 233:235 */       int written = 0;
/* 234:    */       for (;;)
/* 235:    */       {
/* 236:237 */         if (written == size) {
/* 237:239 */           return;
/* 238:    */         }
/* 239:241 */         writableKeysIt.next();
/* 240:242 */         writableKeysIt.remove();
/* 241:    */         
/* 242:244 */         SctpMessage packet = (SctpMessage)in.current();
/* 243:245 */         if (packet == null) {
/* 244:246 */           return;
/* 245:    */         }
/* 246:249 */         ByteBuf data = packet.content();
/* 247:250 */         int dataLen = data.readableBytes();
/* 248:    */         ByteBuffer nioData;
/* 249:    */         ByteBuffer nioData;
/* 250:253 */         if (data.nioBufferCount() != -1)
/* 251:    */         {
/* 252:254 */           nioData = data.nioBuffer();
/* 253:    */         }
/* 254:    */         else
/* 255:    */         {
/* 256:256 */           nioData = ByteBuffer.allocate(dataLen);
/* 257:257 */           data.getBytes(data.readerIndex(), nioData);
/* 258:258 */           nioData.flip();
/* 259:    */         }
/* 260:261 */         MessageInfo mi = MessageInfo.createOutgoing(association(), null, packet.streamIdentifier());
/* 261:262 */         mi.payloadProtocolID(packet.protocolIdentifier());
/* 262:263 */         mi.streamNumber(packet.streamIdentifier());
/* 263:    */         
/* 264:265 */         this.ch.send(nioData, mi);
/* 265:266 */         written++;
/* 266:267 */         in.remove();
/* 267:269 */         if (!writableKeysIt.hasNext()) {
/* 268:270 */           return;
/* 269:    */         }
/* 270:    */       }
/* 271:    */     }
/* 272:    */   }
/* 273:    */   
/* 274:    */   public Association association()
/* 275:    */   {
/* 276:    */     try
/* 277:    */     {
/* 278:279 */       return this.ch.association();
/* 279:    */     }
/* 280:    */     catch (IOException e) {}
/* 281:281 */     return null;
/* 282:    */   }
/* 283:    */   
/* 284:    */   public boolean isActive()
/* 285:    */   {
/* 286:287 */     return (isOpen()) && (association() != null);
/* 287:    */   }
/* 288:    */   
/* 289:    */   protected SocketAddress localAddress0()
/* 290:    */   {
/* 291:    */     try
/* 292:    */     {
/* 293:293 */       Iterator<SocketAddress> i = this.ch.getAllLocalAddresses().iterator();
/* 294:294 */       if (i.hasNext()) {
/* 295:295 */         return (SocketAddress)i.next();
/* 296:    */       }
/* 297:    */     }
/* 298:    */     catch (IOException e) {}
/* 299:300 */     return null;
/* 300:    */   }
/* 301:    */   
/* 302:    */   public Set<InetSocketAddress> allLocalAddresses()
/* 303:    */   {
/* 304:    */     try
/* 305:    */     {
/* 306:306 */       Set<SocketAddress> allLocalAddresses = this.ch.getAllLocalAddresses();
/* 307:307 */       Set<InetSocketAddress> addresses = new LinkedHashSet(allLocalAddresses.size());
/* 308:308 */       for (SocketAddress socketAddress : allLocalAddresses) {
/* 309:309 */         addresses.add((InetSocketAddress)socketAddress);
/* 310:    */       }
/* 311:311 */       return addresses;
/* 312:    */     }
/* 313:    */     catch (Throwable t) {}
/* 314:313 */     return Collections.emptySet();
/* 315:    */   }
/* 316:    */   
/* 317:    */   protected SocketAddress remoteAddress0()
/* 318:    */   {
/* 319:    */     try
/* 320:    */     {
/* 321:320 */       Iterator<SocketAddress> i = this.ch.getRemoteAddresses().iterator();
/* 322:321 */       if (i.hasNext()) {
/* 323:322 */         return (SocketAddress)i.next();
/* 324:    */       }
/* 325:    */     }
/* 326:    */     catch (IOException e) {}
/* 327:327 */     return null;
/* 328:    */   }
/* 329:    */   
/* 330:    */   public Set<InetSocketAddress> allRemoteAddresses()
/* 331:    */   {
/* 332:    */     try
/* 333:    */     {
/* 334:333 */       Set<SocketAddress> allLocalAddresses = this.ch.getRemoteAddresses();
/* 335:334 */       Set<InetSocketAddress> addresses = new LinkedHashSet(allLocalAddresses.size());
/* 336:335 */       for (SocketAddress socketAddress : allLocalAddresses) {
/* 337:336 */         addresses.add((InetSocketAddress)socketAddress);
/* 338:    */       }
/* 339:338 */       return addresses;
/* 340:    */     }
/* 341:    */     catch (Throwable t) {}
/* 342:340 */     return Collections.emptySet();
/* 343:    */   }
/* 344:    */   
/* 345:    */   protected void doBind(SocketAddress localAddress)
/* 346:    */     throws Exception
/* 347:    */   {
/* 348:346 */     this.ch.bind(localAddress);
/* 349:    */   }
/* 350:    */   
/* 351:    */   protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 352:    */     throws Exception
/* 353:    */   {
/* 354:352 */     if (localAddress != null) {
/* 355:353 */       this.ch.bind(localAddress);
/* 356:    */     }
/* 357:356 */     boolean success = false;
/* 358:    */     try
/* 359:    */     {
/* 360:358 */       this.ch.connect(remoteAddress);
/* 361:359 */       boolean finishConnect = false;
/* 362:360 */       while (!finishConnect) {
/* 363:361 */         if (this.connectSelector.select(1000L) >= 0)
/* 364:    */         {
/* 365:362 */           Set<SelectionKey> selectionKeys = this.connectSelector.selectedKeys();
/* 366:363 */           for (SelectionKey key : selectionKeys) {
/* 367:364 */             if (key.isConnectable())
/* 368:    */             {
/* 369:365 */               selectionKeys.clear();
/* 370:366 */               finishConnect = true;
/* 371:367 */               break;
/* 372:    */             }
/* 373:    */           }
/* 374:370 */           selectionKeys.clear();
/* 375:    */         }
/* 376:    */       }
/* 377:373 */       success = this.ch.finishConnect();
/* 378:    */     }
/* 379:    */     finally
/* 380:    */     {
/* 381:375 */       if (!success) {
/* 382:376 */         doClose();
/* 383:    */       }
/* 384:    */     }
/* 385:    */   }
/* 386:    */   
/* 387:    */   protected void doDisconnect()
/* 388:    */     throws Exception
/* 389:    */   {
/* 390:383 */     doClose();
/* 391:    */   }
/* 392:    */   
/* 393:    */   protected void doClose()
/* 394:    */     throws Exception
/* 395:    */   {
/* 396:388 */     closeSelector("read", this.readSelector);
/* 397:389 */     closeSelector("write", this.writeSelector);
/* 398:390 */     closeSelector("connect", this.connectSelector);
/* 399:391 */     this.ch.close();
/* 400:    */   }
/* 401:    */   
/* 402:    */   private static void closeSelector(String selectorName, Selector selector)
/* 403:    */   {
/* 404:    */     try
/* 405:    */     {
/* 406:396 */       selector.close();
/* 407:    */     }
/* 408:    */     catch (IOException e)
/* 409:    */     {
/* 410:398 */       logger.warn("Failed to close a " + selectorName + " selector.", e);
/* 411:    */     }
/* 412:    */   }
/* 413:    */   
/* 414:    */   public ChannelFuture bindAddress(InetAddress localAddress)
/* 415:    */   {
/* 416:404 */     return bindAddress(localAddress, newPromise());
/* 417:    */   }
/* 418:    */   
/* 419:    */   public ChannelFuture bindAddress(final InetAddress localAddress, final ChannelPromise promise)
/* 420:    */   {
/* 421:409 */     if (eventLoop().inEventLoop()) {
/* 422:    */       try
/* 423:    */       {
/* 424:411 */         this.ch.bindAddress(localAddress);
/* 425:412 */         promise.setSuccess();
/* 426:    */       }
/* 427:    */       catch (Throwable t)
/* 428:    */       {
/* 429:414 */         promise.setFailure(t);
/* 430:    */       }
/* 431:    */     } else {
/* 432:417 */       eventLoop().execute(new Runnable()
/* 433:    */       {
/* 434:    */         public void run()
/* 435:    */         {
/* 436:420 */           OioSctpChannel.this.bindAddress(localAddress, promise);
/* 437:    */         }
/* 438:    */       });
/* 439:    */     }
/* 440:424 */     return promise;
/* 441:    */   }
/* 442:    */   
/* 443:    */   public ChannelFuture unbindAddress(InetAddress localAddress)
/* 444:    */   {
/* 445:429 */     return unbindAddress(localAddress, newPromise());
/* 446:    */   }
/* 447:    */   
/* 448:    */   public ChannelFuture unbindAddress(final InetAddress localAddress, final ChannelPromise promise)
/* 449:    */   {
/* 450:434 */     if (eventLoop().inEventLoop()) {
/* 451:    */       try
/* 452:    */       {
/* 453:436 */         this.ch.unbindAddress(localAddress);
/* 454:437 */         promise.setSuccess();
/* 455:    */       }
/* 456:    */       catch (Throwable t)
/* 457:    */       {
/* 458:439 */         promise.setFailure(t);
/* 459:    */       }
/* 460:    */     } else {
/* 461:442 */       eventLoop().execute(new Runnable()
/* 462:    */       {
/* 463:    */         public void run()
/* 464:    */         {
/* 465:445 */           OioSctpChannel.this.unbindAddress(localAddress, promise);
/* 466:    */         }
/* 467:    */       });
/* 468:    */     }
/* 469:449 */     return promise;
/* 470:    */   }
/* 471:    */   
/* 472:    */   private final class OioSctpChannelConfig
/* 473:    */     extends DefaultSctpChannelConfig
/* 474:    */   {
/* 475:    */     private OioSctpChannelConfig(OioSctpChannel channel, com.sun.nio.sctp.SctpChannel javaChannel)
/* 476:    */     {
/* 477:454 */       super(javaChannel);
/* 478:    */     }
/* 479:    */     
/* 480:    */     protected void autoReadCleared()
/* 481:    */     {
/* 482:459 */       OioSctpChannel.this.setReadPending(false);
/* 483:    */     }
/* 484:    */   }
/* 485:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.sctp.oio.OioSctpChannel
 * JD-Core Version:    0.7.0.1
 */