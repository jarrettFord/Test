/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.ByteBufHolder;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelMetadata;
/*   8:    */ import io.netty.channel.ChannelOption;
/*   9:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  10:    */ import io.netty.channel.ChannelPipeline;
/*  11:    */ import io.netty.channel.ChannelPromise;
/*  12:    */ import io.netty.channel.EventLoop;
/*  13:    */ import io.netty.channel.RecvByteBufAllocator;
/*  14:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  15:    */ import io.netty.channel.socket.DatagramChannel;
/*  16:    */ import io.netty.channel.socket.DatagramChannelConfig;
/*  17:    */ import io.netty.channel.socket.DatagramPacket;
/*  18:    */ import io.netty.util.internal.StringUtil;
/*  19:    */ import java.io.IOException;
/*  20:    */ import java.net.InetAddress;
/*  21:    */ import java.net.InetSocketAddress;
/*  22:    */ import java.net.NetworkInterface;
/*  23:    */ import java.net.SocketAddress;
/*  24:    */ import java.net.SocketException;
/*  25:    */ import java.nio.ByteBuffer;
/*  26:    */ import java.nio.channels.NotYetConnectedException;
/*  27:    */ 
/*  28:    */ public final class EpollDatagramChannel
/*  29:    */   extends AbstractEpollChannel
/*  30:    */   implements DatagramChannel
/*  31:    */ {
/*  32: 46 */   private static final ChannelMetadata METADATA = new ChannelMetadata(true);
/*  33:    */   private volatile InetSocketAddress local;
/*  34:    */   private volatile InetSocketAddress remote;
/*  35:    */   private volatile boolean connected;
/*  36:    */   private final EpollDatagramChannelConfig config;
/*  37:    */   
/*  38:    */   public EpollDatagramChannel()
/*  39:    */   {
/*  40: 54 */     super(Native.socketDgramFd(), 1);
/*  41: 55 */     this.config = new EpollDatagramChannelConfig(this);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public ChannelMetadata metadata()
/*  45:    */   {
/*  46: 60 */     return METADATA;
/*  47:    */   }
/*  48:    */   
/*  49:    */   public boolean isActive()
/*  50:    */   {
/*  51: 65 */     return (this.fd != -1) && (((((Boolean)this.config.getOption(ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION)).booleanValue()) && (isRegistered())) || (this.active));
/*  52:    */   }
/*  53:    */   
/*  54:    */   public boolean isConnected()
/*  55:    */   {
/*  56: 72 */     return this.connected;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public ChannelFuture joinGroup(InetAddress multicastAddress)
/*  60:    */   {
/*  61: 77 */     return joinGroup(multicastAddress, newPromise());
/*  62:    */   }
/*  63:    */   
/*  64:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, ChannelPromise promise)
/*  65:    */   {
/*  66:    */     try
/*  67:    */     {
/*  68: 83 */       return joinGroup(multicastAddress, NetworkInterface.getByInetAddress(localAddress().getAddress()), null, promise);
/*  69:    */     }
/*  70:    */     catch (SocketException e)
/*  71:    */     {
/*  72: 88 */       promise.setFailure(e);
/*  73:    */     }
/*  74: 90 */     return promise;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/*  78:    */   {
/*  79: 96 */     return joinGroup(multicastAddress, networkInterface, newPromise());
/*  80:    */   }
/*  81:    */   
/*  82:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/*  83:    */   {
/*  84:103 */     return joinGroup(multicastAddress.getAddress(), networkInterface, null, promise);
/*  85:    */   }
/*  86:    */   
/*  87:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/*  88:    */   {
/*  89:109 */     return joinGroup(multicastAddress, networkInterface, source, newPromise());
/*  90:    */   }
/*  91:    */   
/*  92:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/*  93:    */   {
/*  94:117 */     if (multicastAddress == null) {
/*  95:118 */       throw new NullPointerException("multicastAddress");
/*  96:    */     }
/*  97:121 */     if (networkInterface == null) {
/*  98:122 */       throw new NullPointerException("networkInterface");
/*  99:    */     }
/* 100:125 */     promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
/* 101:126 */     return promise;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress)
/* 105:    */   {
/* 106:131 */     return leaveGroup(multicastAddress, newPromise());
/* 107:    */   }
/* 108:    */   
/* 109:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, ChannelPromise promise)
/* 110:    */   {
/* 111:    */     try
/* 112:    */     {
/* 113:137 */       return leaveGroup(multicastAddress, NetworkInterface.getByInetAddress(localAddress().getAddress()), null, promise);
/* 114:    */     }
/* 115:    */     catch (SocketException e)
/* 116:    */     {
/* 117:140 */       promise.setFailure(e);
/* 118:    */     }
/* 119:142 */     return promise;
/* 120:    */   }
/* 121:    */   
/* 122:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/* 123:    */   {
/* 124:148 */     return leaveGroup(multicastAddress, networkInterface, newPromise());
/* 125:    */   }
/* 126:    */   
/* 127:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/* 128:    */   {
/* 129:155 */     return leaveGroup(multicastAddress.getAddress(), networkInterface, null, promise);
/* 130:    */   }
/* 131:    */   
/* 132:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/* 133:    */   {
/* 134:161 */     return leaveGroup(multicastAddress, networkInterface, source, newPromise());
/* 135:    */   }
/* 136:    */   
/* 137:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/* 138:    */   {
/* 139:168 */     if (multicastAddress == null) {
/* 140:169 */       throw new NullPointerException("multicastAddress");
/* 141:    */     }
/* 142:171 */     if (networkInterface == null) {
/* 143:172 */       throw new NullPointerException("networkInterface");
/* 144:    */     }
/* 145:175 */     promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
/* 146:    */     
/* 147:177 */     return promise;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock)
/* 151:    */   {
/* 152:184 */     return block(multicastAddress, networkInterface, sourceToBlock, newPromise());
/* 153:    */   }
/* 154:    */   
/* 155:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock, ChannelPromise promise)
/* 156:    */   {
/* 157:191 */     if (multicastAddress == null) {
/* 158:192 */       throw new NullPointerException("multicastAddress");
/* 159:    */     }
/* 160:194 */     if (sourceToBlock == null) {
/* 161:195 */       throw new NullPointerException("sourceToBlock");
/* 162:    */     }
/* 163:198 */     if (networkInterface == null) {
/* 164:199 */       throw new NullPointerException("networkInterface");
/* 165:    */     }
/* 166:201 */     promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
/* 167:202 */     return promise;
/* 168:    */   }
/* 169:    */   
/* 170:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock)
/* 171:    */   {
/* 172:207 */     return block(multicastAddress, sourceToBlock, newPromise());
/* 173:    */   }
/* 174:    */   
/* 175:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock, ChannelPromise promise)
/* 176:    */   {
/* 177:    */     try
/* 178:    */     {
/* 179:214 */       return block(multicastAddress, NetworkInterface.getByInetAddress(localAddress().getAddress()), sourceToBlock, promise);
/* 180:    */     }
/* 181:    */     catch (Throwable e)
/* 182:    */     {
/* 183:219 */       promise.setFailure(e);
/* 184:    */     }
/* 185:221 */     return promise;
/* 186:    */   }
/* 187:    */   
/* 188:    */   protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe()
/* 189:    */   {
/* 190:226 */     return new EpollDatagramChannelUnsafe();
/* 191:    */   }
/* 192:    */   
/* 193:    */   protected InetSocketAddress localAddress0()
/* 194:    */   {
/* 195:231 */     return this.local;
/* 196:    */   }
/* 197:    */   
/* 198:    */   protected InetSocketAddress remoteAddress0()
/* 199:    */   {
/* 200:236 */     return this.remote;
/* 201:    */   }
/* 202:    */   
/* 203:    */   protected void doBind(SocketAddress localAddress)
/* 204:    */     throws Exception
/* 205:    */   {
/* 206:241 */     InetSocketAddress addr = (InetSocketAddress)localAddress;
/* 207:242 */     checkResolvable(addr);
/* 208:243 */     Native.bind(this.fd, addr.getAddress(), addr.getPort());
/* 209:244 */     this.local = Native.localAddress(this.fd);
/* 210:245 */     this.active = true;
/* 211:    */   }
/* 212:    */   
/* 213:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 214:    */     throws Exception
/* 215:    */   {
/* 216:    */     for (;;)
/* 217:    */     {
/* 218:251 */       Object msg = in.current();
/* 219:252 */       if (msg == null)
/* 220:    */       {
/* 221:254 */         clearEpollOut();
/* 222:255 */         break;
/* 223:    */       }
/* 224:258 */       boolean done = false;
/* 225:259 */       for (int i = config().getWriteSpinCount() - 1; i >= 0; i--) {
/* 226:260 */         if (doWriteMessage(msg))
/* 227:    */         {
/* 228:261 */           done = true;
/* 229:262 */           break;
/* 230:    */         }
/* 231:    */       }
/* 232:266 */       if (done)
/* 233:    */       {
/* 234:267 */         in.remove();
/* 235:    */       }
/* 236:    */       else
/* 237:    */       {
/* 238:270 */         setEpollOut();
/* 239:271 */         break;
/* 240:    */       }
/* 241:    */     }
/* 242:    */   }
/* 243:    */   
/* 244:    */   private boolean doWriteMessage(Object msg)
/* 245:    */     throws IOException
/* 246:    */   {
/* 247:    */     Object m;
/* 248:    */     Object m;
/* 249:    */     InetSocketAddress remoteAddress;
/* 250:280 */     if ((msg instanceof DatagramPacket))
/* 251:    */     {
/* 252:282 */       DatagramPacket packet = (DatagramPacket)msg;
/* 253:283 */       InetSocketAddress remoteAddress = (InetSocketAddress)packet.recipient();
/* 254:284 */       m = packet.content();
/* 255:    */     }
/* 256:    */     else
/* 257:    */     {
/* 258:286 */       m = msg;
/* 259:287 */       remoteAddress = null;
/* 260:    */     }
/* 261:    */     ByteBuf data;
/* 262:290 */     if ((m instanceof ByteBufHolder))
/* 263:    */     {
/* 264:291 */       data = ((ByteBufHolder)m).content();
/* 265:    */     }
/* 266:    */     else
/* 267:    */     {
/* 268:    */       ByteBuf data;
/* 269:292 */       if ((m instanceof ByteBuf)) {
/* 270:293 */         data = (ByteBuf)m;
/* 271:    */       } else {
/* 272:295 */         throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg));
/* 273:    */       }
/* 274:    */     }
/* 275:    */     ByteBuf data;
/* 276:298 */     int dataLen = data.readableBytes();
/* 277:299 */     if (dataLen == 0) {
/* 278:300 */       return true;
/* 279:    */     }
/* 280:303 */     if (remoteAddress == null)
/* 281:    */     {
/* 282:304 */       remoteAddress = this.remote;
/* 283:305 */       if (remoteAddress == null) {
/* 284:306 */         throw new NotYetConnectedException();
/* 285:    */       }
/* 286:    */     }
/* 287:    */     int writtenBytes;
/* 288:    */     int writtenBytes;
/* 289:311 */     if (data.hasMemoryAddress())
/* 290:    */     {
/* 291:312 */       long memoryAddress = data.memoryAddress();
/* 292:313 */       writtenBytes = Native.sendToAddress(this.fd, memoryAddress, data.readerIndex(), data.writerIndex(), remoteAddress.getAddress(), remoteAddress.getPort());
/* 293:    */     }
/* 294:    */     else
/* 295:    */     {
/* 296:316 */       ByteBuffer nioData = data.internalNioBuffer(data.readerIndex(), data.readableBytes());
/* 297:317 */       writtenBytes = Native.sendTo(this.fd, nioData, nioData.position(), nioData.limit(), remoteAddress.getAddress(), remoteAddress.getPort());
/* 298:    */     }
/* 299:320 */     return writtenBytes > 0;
/* 300:    */   }
/* 301:    */   
/* 302:    */   public EpollDatagramChannelConfig config()
/* 303:    */   {
/* 304:325 */     return this.config;
/* 305:    */   }
/* 306:    */   
/* 307:    */   protected void doDisconnect()
/* 308:    */     throws Exception
/* 309:    */   {
/* 310:330 */     this.connected = false;
/* 311:    */   }
/* 312:    */   
/* 313:    */   final class EpollDatagramChannelUnsafe
/* 314:    */     extends AbstractEpollChannel.AbstractEpollUnsafe
/* 315:    */   {
/* 316:    */     private RecvByteBufAllocator.Handle allocHandle;
/* 317:    */     
/* 318:    */     EpollDatagramChannelUnsafe()
/* 319:    */     {
/* 320:333 */       super();
/* 321:    */     }
/* 322:    */     
/* 323:    */     public void connect(SocketAddress remote, SocketAddress local, ChannelPromise channelPromise)
/* 324:    */     {
/* 325:338 */       boolean success = false;
/* 326:    */       try
/* 327:    */       {
/* 328:    */         try
/* 329:    */         {
/* 330:341 */           InetSocketAddress remoteAddress = (InetSocketAddress)remote;
/* 331:342 */           if (local != null)
/* 332:    */           {
/* 333:343 */             InetSocketAddress localAddress = (InetSocketAddress)local;
/* 334:344 */             EpollDatagramChannel.this.doBind(localAddress);
/* 335:    */           }
/* 336:347 */           AbstractEpollChannel.checkResolvable(remoteAddress);
/* 337:348 */           EpollDatagramChannel.this.remote = remoteAddress;
/* 338:349 */           EpollDatagramChannel.this.local = Native.localAddress(EpollDatagramChannel.this.fd);
/* 339:350 */           success = true;
/* 340:    */         }
/* 341:    */         finally
/* 342:    */         {
/* 343:352 */           if (!success)
/* 344:    */           {
/* 345:353 */             EpollDatagramChannel.this.doClose();
/* 346:    */           }
/* 347:    */           else
/* 348:    */           {
/* 349:355 */             channelPromise.setSuccess();
/* 350:356 */             EpollDatagramChannel.this.connected = true;
/* 351:    */           }
/* 352:    */         }
/* 353:    */       }
/* 354:    */       catch (Throwable cause)
/* 355:    */       {
/* 356:360 */         channelPromise.setFailure(cause);
/* 357:    */       }
/* 358:    */     }
/* 359:    */     
/* 360:    */     void epollInReady()
/* 361:    */     {
/* 362:366 */       DatagramChannelConfig config = EpollDatagramChannel.this.config();
/* 363:367 */       RecvByteBufAllocator.Handle allocHandle = this.allocHandle;
/* 364:368 */       if (allocHandle == null) {
/* 365:369 */         this.allocHandle = (allocHandle = config.getRecvByteBufAllocator().newHandle());
/* 366:    */       }
/* 367:372 */       assert (EpollDatagramChannel.this.eventLoop().inEventLoop());
/* 368:373 */       ChannelPipeline pipeline = EpollDatagramChannel.this.pipeline();
/* 369:    */       try
/* 370:    */       {
/* 371:    */         for (;;)
/* 372:    */         {
/* 373:376 */           ByteBuf data = null;
/* 374:    */           try
/* 375:    */           {
/* 376:378 */             data = allocHandle.allocate(config.getAllocator());
/* 377:379 */             int writerIndex = data.writerIndex();
/* 378:    */             EpollDatagramChannel.DatagramSocketAddress remoteAddress;
/* 379:    */             EpollDatagramChannel.DatagramSocketAddress remoteAddress;
/* 380:381 */             if (data.hasMemoryAddress())
/* 381:    */             {
/* 382:383 */               remoteAddress = Native.recvFromAddress(EpollDatagramChannel.this.fd, data.memoryAddress(), writerIndex, data.capacity());
/* 383:    */             }
/* 384:    */             else
/* 385:    */             {
/* 386:386 */               ByteBuffer nioData = data.internalNioBuffer(writerIndex, data.writableBytes());
/* 387:387 */               remoteAddress = Native.recvFrom(EpollDatagramChannel.this.fd, nioData, nioData.position(), nioData.limit());
/* 388:    */             }
/* 389:391 */             if (remoteAddress == null)
/* 390:    */             {
/* 391:407 */               if (data == null) {
/* 392:    */                 break;
/* 393:    */               }
/* 394:408 */               data.release(); break;
/* 395:    */             }
/* 396:395 */             int readBytes = remoteAddress.receivedAmount;
/* 397:396 */             data.writerIndex(data.writerIndex() + readBytes);
/* 398:397 */             allocHandle.record(readBytes);
/* 399:398 */             this.readPending = false;
/* 400:399 */             pipeline.fireChannelRead(new DatagramPacket(data, (InetSocketAddress)localAddress(), remoteAddress));
/* 401:    */             
/* 402:401 */             data = null;
/* 403:    */           }
/* 404:    */           catch (Throwable t)
/* 405:    */           {
/* 406:404 */             pipeline.fireChannelReadComplete();
/* 407:405 */             pipeline.fireExceptionCaught(t);
/* 408:    */           }
/* 409:    */           finally
/* 410:    */           {
/* 411:407 */             if (data != null) {
/* 412:408 */               data.release();
/* 413:    */             }
/* 414:    */           }
/* 415:    */         }
/* 416:    */       }
/* 417:    */       finally
/* 418:    */       {
/* 419:419 */         if ((!EpollDatagramChannel.this.config().isAutoRead()) && (!this.readPending)) {
/* 420:420 */           EpollDatagramChannel.this.clearEpollIn();
/* 421:    */         }
/* 422:    */       }
/* 423:    */     }
/* 424:    */     
/* 425:    */     public void write(Object msg, ChannelPromise promise)
/* 426:    */     {
/* 427:427 */       if ((msg instanceof DatagramPacket))
/* 428:    */       {
/* 429:428 */         DatagramPacket packet = (DatagramPacket)msg;
/* 430:429 */         ByteBuf content = (ByteBuf)packet.content();
/* 431:430 */         if (isCopyNeeded(content))
/* 432:    */         {
/* 433:433 */           int readable = content.readableBytes();
/* 434:434 */           ByteBuf dst = EpollDatagramChannel.this.alloc().directBuffer(readable);
/* 435:435 */           dst.writeBytes(content, content.readerIndex(), readable);
/* 436:    */           
/* 437:437 */           content.release();
/* 438:438 */           msg = new DatagramPacket(dst, (InetSocketAddress)packet.recipient(), (InetSocketAddress)packet.sender());
/* 439:    */         }
/* 440:    */       }
/* 441:440 */       else if ((msg instanceof ByteBuf))
/* 442:    */       {
/* 443:441 */         ByteBuf buf = (ByteBuf)msg;
/* 444:442 */         if (isCopyNeeded(buf))
/* 445:    */         {
/* 446:445 */           int readable = buf.readableBytes();
/* 447:446 */           ByteBuf dst = EpollDatagramChannel.this.alloc().directBuffer(readable);
/* 448:447 */           dst.writeBytes(buf, buf.readerIndex(), readable);
/* 449:    */           
/* 450:449 */           buf.release();
/* 451:450 */           msg = dst;
/* 452:    */         }
/* 453:    */       }
/* 454:454 */       super.write(msg, promise);
/* 455:    */     }
/* 456:    */     
/* 457:    */     private boolean isCopyNeeded(ByteBuf content)
/* 458:    */     {
/* 459:458 */       return (!content.hasMemoryAddress()) || (content.nioBufferCount() != 1);
/* 460:    */     }
/* 461:    */   }
/* 462:    */   
/* 463:    */   static final class DatagramSocketAddress
/* 464:    */     extends InetSocketAddress
/* 465:    */   {
/* 466:    */     final int receivedAmount;
/* 467:    */     
/* 468:    */     DatagramSocketAddress(String addr, int port, int receivedAmount)
/* 469:    */     {
/* 470:471 */       super(port);
/* 471:472 */       this.receivedAmount = receivedAmount;
/* 472:    */     }
/* 473:    */   }
/* 474:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.epoll.EpollDatagramChannel
 * JD-Core Version:    0.7.0.1
 */