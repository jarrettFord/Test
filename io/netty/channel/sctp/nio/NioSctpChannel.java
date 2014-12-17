/*   1:    */ package io.netty.channel.sctp.nio;
/*   2:    */ 
/*   3:    */ import com.sun.nio.sctp.Association;
/*   4:    */ import com.sun.nio.sctp.MessageInfo;
/*   5:    */ import com.sun.nio.sctp.NotificationHandler;
/*   6:    */ import io.netty.buffer.ByteBuf;
/*   7:    */ import io.netty.buffer.ByteBufAllocator;
/*   8:    */ import io.netty.channel.Channel;
/*   9:    */ import io.netty.channel.ChannelException;
/*  10:    */ import io.netty.channel.ChannelFuture;
/*  11:    */ import io.netty.channel.ChannelMetadata;
/*  12:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  13:    */ import io.netty.channel.ChannelPromise;
/*  14:    */ import io.netty.channel.RecvByteBufAllocator;
/*  15:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  16:    */ import io.netty.channel.nio.AbstractNioMessageChannel;
/*  17:    */ import io.netty.channel.nio.NioEventLoop;
/*  18:    */ import io.netty.channel.sctp.DefaultSctpChannelConfig;
/*  19:    */ import io.netty.channel.sctp.SctpChannelConfig;
/*  20:    */ import io.netty.channel.sctp.SctpMessage;
/*  21:    */ import io.netty.channel.sctp.SctpNotificationHandler;
/*  22:    */ import io.netty.channel.sctp.SctpServerChannel;
/*  23:    */ import io.netty.util.internal.PlatformDependent;
/*  24:    */ import io.netty.util.internal.logging.InternalLogger;
/*  25:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  26:    */ import java.io.IOException;
/*  27:    */ import java.net.InetAddress;
/*  28:    */ import java.net.InetSocketAddress;
/*  29:    */ import java.net.SocketAddress;
/*  30:    */ import java.nio.ByteBuffer;
/*  31:    */ import java.nio.channels.SelectionKey;
/*  32:    */ import java.util.Collections;
/*  33:    */ import java.util.HashSet;
/*  34:    */ import java.util.Iterator;
/*  35:    */ import java.util.LinkedHashSet;
/*  36:    */ import java.util.List;
/*  37:    */ import java.util.Set;
/*  38:    */ 
/*  39:    */ public class NioSctpChannel
/*  40:    */   extends AbstractNioMessageChannel
/*  41:    */   implements io.netty.channel.sctp.SctpChannel
/*  42:    */ {
/*  43: 62 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  44: 64 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioSctpChannel.class);
/*  45:    */   private final SctpChannelConfig config;
/*  46:    */   private final NotificationHandler<?> notificationHandler;
/*  47:    */   private RecvByteBufAllocator.Handle allocHandle;
/*  48:    */   
/*  49:    */   private static com.sun.nio.sctp.SctpChannel newSctpChannel()
/*  50:    */   {
/*  51:    */     try
/*  52:    */     {
/*  53: 74 */       return com.sun.nio.sctp.SctpChannel.open();
/*  54:    */     }
/*  55:    */     catch (IOException e)
/*  56:    */     {
/*  57: 76 */       throw new ChannelException("Failed to open a sctp channel.", e);
/*  58:    */     }
/*  59:    */   }
/*  60:    */   
/*  61:    */   public NioSctpChannel()
/*  62:    */   {
/*  63: 84 */     this(newSctpChannel());
/*  64:    */   }
/*  65:    */   
/*  66:    */   public NioSctpChannel(com.sun.nio.sctp.SctpChannel sctpChannel)
/*  67:    */   {
/*  68: 91 */     this(null, sctpChannel);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public NioSctpChannel(Channel parent, com.sun.nio.sctp.SctpChannel sctpChannel)
/*  72:    */   {
/*  73:102 */     super(parent, sctpChannel, 1);
/*  74:    */     try
/*  75:    */     {
/*  76:104 */       sctpChannel.configureBlocking(false);
/*  77:105 */       this.config = new NioSctpChannelConfig(this, sctpChannel, null);
/*  78:106 */       this.notificationHandler = new SctpNotificationHandler(this);
/*  79:    */     }
/*  80:    */     catch (IOException e)
/*  81:    */     {
/*  82:    */       try
/*  83:    */       {
/*  84:109 */         sctpChannel.close();
/*  85:    */       }
/*  86:    */       catch (IOException e2)
/*  87:    */       {
/*  88:111 */         if (logger.isWarnEnabled()) {
/*  89:112 */           logger.warn("Failed to close a partially initialized sctp channel.", e2);
/*  90:    */         }
/*  91:    */       }
/*  92:117 */       throw new ChannelException("Failed to enter non-blocking mode.", e);
/*  93:    */     }
/*  94:    */   }
/*  95:    */   
/*  96:    */   public InetSocketAddress localAddress()
/*  97:    */   {
/*  98:123 */     return (InetSocketAddress)super.localAddress();
/*  99:    */   }
/* 100:    */   
/* 101:    */   public InetSocketAddress remoteAddress()
/* 102:    */   {
/* 103:128 */     return (InetSocketAddress)super.remoteAddress();
/* 104:    */   }
/* 105:    */   
/* 106:    */   public SctpServerChannel parent()
/* 107:    */   {
/* 108:133 */     return (SctpServerChannel)super.parent();
/* 109:    */   }
/* 110:    */   
/* 111:    */   public ChannelMetadata metadata()
/* 112:    */   {
/* 113:138 */     return METADATA;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public Association association()
/* 117:    */   {
/* 118:    */     try
/* 119:    */     {
/* 120:144 */       return javaChannel().association();
/* 121:    */     }
/* 122:    */     catch (IOException e) {}
/* 123:146 */     return null;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public Set<InetSocketAddress> allLocalAddresses()
/* 127:    */   {
/* 128:    */     try
/* 129:    */     {
/* 130:153 */       Set<SocketAddress> allLocalAddresses = javaChannel().getAllLocalAddresses();
/* 131:154 */       Set<InetSocketAddress> addresses = new LinkedHashSet(allLocalAddresses.size());
/* 132:155 */       for (SocketAddress socketAddress : allLocalAddresses) {
/* 133:156 */         addresses.add((InetSocketAddress)socketAddress);
/* 134:    */       }
/* 135:158 */       return addresses;
/* 136:    */     }
/* 137:    */     catch (Throwable t) {}
/* 138:160 */     return Collections.emptySet();
/* 139:    */   }
/* 140:    */   
/* 141:    */   public SctpChannelConfig config()
/* 142:    */   {
/* 143:166 */     return this.config;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public Set<InetSocketAddress> allRemoteAddresses()
/* 147:    */   {
/* 148:    */     try
/* 149:    */     {
/* 150:172 */       Set<SocketAddress> allLocalAddresses = javaChannel().getRemoteAddresses();
/* 151:173 */       Set<InetSocketAddress> addresses = new HashSet(allLocalAddresses.size());
/* 152:174 */       for (SocketAddress socketAddress : allLocalAddresses) {
/* 153:175 */         addresses.add((InetSocketAddress)socketAddress);
/* 154:    */       }
/* 155:177 */       return addresses;
/* 156:    */     }
/* 157:    */     catch (Throwable t) {}
/* 158:179 */     return Collections.emptySet();
/* 159:    */   }
/* 160:    */   
/* 161:    */   protected com.sun.nio.sctp.SctpChannel javaChannel()
/* 162:    */   {
/* 163:185 */     return (com.sun.nio.sctp.SctpChannel)super.javaChannel();
/* 164:    */   }
/* 165:    */   
/* 166:    */   public boolean isActive()
/* 167:    */   {
/* 168:190 */     com.sun.nio.sctp.SctpChannel ch = javaChannel();
/* 169:191 */     return (ch.isOpen()) && (association() != null);
/* 170:    */   }
/* 171:    */   
/* 172:    */   protected SocketAddress localAddress0()
/* 173:    */   {
/* 174:    */     try
/* 175:    */     {
/* 176:197 */       Iterator<SocketAddress> i = javaChannel().getAllLocalAddresses().iterator();
/* 177:198 */       if (i.hasNext()) {
/* 178:199 */         return (SocketAddress)i.next();
/* 179:    */       }
/* 180:    */     }
/* 181:    */     catch (IOException e) {}
/* 182:204 */     return null;
/* 183:    */   }
/* 184:    */   
/* 185:    */   protected SocketAddress remoteAddress0()
/* 186:    */   {
/* 187:    */     try
/* 188:    */     {
/* 189:210 */       Iterator<SocketAddress> i = javaChannel().getRemoteAddresses().iterator();
/* 190:211 */       if (i.hasNext()) {
/* 191:212 */         return (SocketAddress)i.next();
/* 192:    */       }
/* 193:    */     }
/* 194:    */     catch (IOException e) {}
/* 195:217 */     return null;
/* 196:    */   }
/* 197:    */   
/* 198:    */   protected void doBind(SocketAddress localAddress)
/* 199:    */     throws Exception
/* 200:    */   {
/* 201:222 */     javaChannel().bind(localAddress);
/* 202:    */   }
/* 203:    */   
/* 204:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 205:    */     throws Exception
/* 206:    */   {
/* 207:227 */     if (localAddress != null) {
/* 208:228 */       javaChannel().bind(localAddress);
/* 209:    */     }
/* 210:231 */     boolean success = false;
/* 211:    */     try
/* 212:    */     {
/* 213:233 */       boolean connected = javaChannel().connect(remoteAddress);
/* 214:234 */       if (!connected) {
/* 215:235 */         selectionKey().interestOps(8);
/* 216:    */       }
/* 217:237 */       success = true;
/* 218:238 */       return connected;
/* 219:    */     }
/* 220:    */     finally
/* 221:    */     {
/* 222:240 */       if (!success) {
/* 223:241 */         doClose();
/* 224:    */       }
/* 225:    */     }
/* 226:    */   }
/* 227:    */   
/* 228:    */   protected void doFinishConnect()
/* 229:    */     throws Exception
/* 230:    */   {
/* 231:248 */     if (!javaChannel().finishConnect()) {
/* 232:249 */       throw new Error();
/* 233:    */     }
/* 234:    */   }
/* 235:    */   
/* 236:    */   protected void doDisconnect()
/* 237:    */     throws Exception
/* 238:    */   {
/* 239:255 */     doClose();
/* 240:    */   }
/* 241:    */   
/* 242:    */   protected void doClose()
/* 243:    */     throws Exception
/* 244:    */   {
/* 245:260 */     javaChannel().close();
/* 246:    */   }
/* 247:    */   
/* 248:    */   protected int doReadMessages(List<Object> buf)
/* 249:    */     throws Exception
/* 250:    */   {
/* 251:265 */     com.sun.nio.sctp.SctpChannel ch = javaChannel();
/* 252:    */     
/* 253:267 */     RecvByteBufAllocator.Handle allocHandle = this.allocHandle;
/* 254:268 */     if (allocHandle == null) {
/* 255:269 */       this.allocHandle = (allocHandle = config().getRecvByteBufAllocator().newHandle());
/* 256:    */     }
/* 257:271 */     ByteBuf buffer = allocHandle.allocate(config().getAllocator());
/* 258:272 */     boolean free = true;
/* 259:    */     try
/* 260:    */     {
/* 261:274 */       ByteBuffer data = buffer.internalNioBuffer(buffer.writerIndex(), buffer.writableBytes());
/* 262:275 */       pos = data.position();
/* 263:    */       
/* 264:277 */       MessageInfo messageInfo = ch.receive(data, null, this.notificationHandler);
/* 265:    */       int i;
/* 266:278 */       if (messageInfo == null)
/* 267:    */       {
/* 268:    */         int bytesRead;
/* 269:279 */         return 0;
/* 270:    */       }
/* 271:281 */       buf.add(new SctpMessage(messageInfo, buffer.writerIndex(buffer.writerIndex() + (data.position() - pos))));
/* 272:282 */       free = false;
/* 273:    */       int bytesRead;
/* 274:283 */       return 1;
/* 275:    */     }
/* 276:    */     catch (Throwable cause)
/* 277:    */     {
/* 278:    */       int pos;
/* 279:285 */       PlatformDependent.throwException(cause);
/* 280:    */       int bytesRead;
/* 281:286 */       return -1;
/* 282:    */     }
/* 283:    */     finally
/* 284:    */     {
/* 285:288 */       int bytesRead = buffer.readableBytes();
/* 286:289 */       allocHandle.record(bytesRead);
/* 287:290 */       if (free) {
/* 288:291 */         buffer.release();
/* 289:    */       }
/* 290:    */     }
/* 291:    */   }
/* 292:    */   
/* 293:    */   protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in)
/* 294:    */     throws Exception
/* 295:    */   {
/* 296:298 */     SctpMessage packet = (SctpMessage)msg;
/* 297:299 */     ByteBuf data = packet.content();
/* 298:300 */     int dataLen = data.readableBytes();
/* 299:301 */     if (dataLen == 0) {
/* 300:302 */       return true;
/* 301:    */     }
/* 302:305 */     ByteBufAllocator alloc = alloc();
/* 303:306 */     boolean needsCopy = data.nioBufferCount() != 1;
/* 304:307 */     if ((!needsCopy) && 
/* 305:308 */       (!data.isDirect()) && (alloc.isDirectBufferPooled())) {
/* 306:309 */       needsCopy = true;
/* 307:    */     }
/* 308:    */     ByteBuffer nioData;
/* 309:    */     ByteBuffer nioData;
/* 310:313 */     if (!needsCopy)
/* 311:    */     {
/* 312:314 */       nioData = data.nioBuffer();
/* 313:    */     }
/* 314:    */     else
/* 315:    */     {
/* 316:316 */       data = alloc.directBuffer(dataLen).writeBytes(data);
/* 317:317 */       nioData = data.nioBuffer();
/* 318:    */     }
/* 319:320 */     MessageInfo mi = MessageInfo.createOutgoing(association(), null, packet.streamIdentifier());
/* 320:321 */     mi.payloadProtocolID(packet.protocolIdentifier());
/* 321:322 */     mi.streamNumber(packet.streamIdentifier());
/* 322:    */     
/* 323:324 */     int writtenBytes = javaChannel().send(nioData, mi);
/* 324:    */     
/* 325:326 */     boolean done = writtenBytes > 0;
/* 326:327 */     if (needsCopy) {
/* 327:328 */       if (!done) {
/* 328:329 */         in.current(new SctpMessage(mi, data));
/* 329:    */       } else {
/* 330:331 */         in.current(data);
/* 331:    */       }
/* 332:    */     }
/* 333:334 */     return done;
/* 334:    */   }
/* 335:    */   
/* 336:    */   public ChannelFuture bindAddress(InetAddress localAddress)
/* 337:    */   {
/* 338:339 */     return bindAddress(localAddress, newPromise());
/* 339:    */   }
/* 340:    */   
/* 341:    */   public ChannelFuture bindAddress(final InetAddress localAddress, final ChannelPromise promise)
/* 342:    */   {
/* 343:344 */     if (eventLoop().inEventLoop()) {
/* 344:    */       try
/* 345:    */       {
/* 346:346 */         javaChannel().bindAddress(localAddress);
/* 347:347 */         promise.setSuccess();
/* 348:    */       }
/* 349:    */       catch (Throwable t)
/* 350:    */       {
/* 351:349 */         promise.setFailure(t);
/* 352:    */       }
/* 353:    */     } else {
/* 354:352 */       eventLoop().execute(new Runnable()
/* 355:    */       {
/* 356:    */         public void run()
/* 357:    */         {
/* 358:355 */           NioSctpChannel.this.bindAddress(localAddress, promise);
/* 359:    */         }
/* 360:    */       });
/* 361:    */     }
/* 362:359 */     return promise;
/* 363:    */   }
/* 364:    */   
/* 365:    */   public ChannelFuture unbindAddress(InetAddress localAddress)
/* 366:    */   {
/* 367:364 */     return unbindAddress(localAddress, newPromise());
/* 368:    */   }
/* 369:    */   
/* 370:    */   public ChannelFuture unbindAddress(final InetAddress localAddress, final ChannelPromise promise)
/* 371:    */   {
/* 372:369 */     if (eventLoop().inEventLoop()) {
/* 373:    */       try
/* 374:    */       {
/* 375:371 */         javaChannel().unbindAddress(localAddress);
/* 376:372 */         promise.setSuccess();
/* 377:    */       }
/* 378:    */       catch (Throwable t)
/* 379:    */       {
/* 380:374 */         promise.setFailure(t);
/* 381:    */       }
/* 382:    */     } else {
/* 383:377 */       eventLoop().execute(new Runnable()
/* 384:    */       {
/* 385:    */         public void run()
/* 386:    */         {
/* 387:380 */           NioSctpChannel.this.unbindAddress(localAddress, promise);
/* 388:    */         }
/* 389:    */       });
/* 390:    */     }
/* 391:384 */     return promise;
/* 392:    */   }
/* 393:    */   
/* 394:    */   private final class NioSctpChannelConfig
/* 395:    */     extends DefaultSctpChannelConfig
/* 396:    */   {
/* 397:    */     private NioSctpChannelConfig(NioSctpChannel channel, com.sun.nio.sctp.SctpChannel javaChannel)
/* 398:    */     {
/* 399:389 */       super(javaChannel);
/* 400:    */     }
/* 401:    */     
/* 402:    */     protected void autoReadCleared()
/* 403:    */     {
/* 404:394 */       NioSctpChannel.this.setReadPending(false);
/* 405:    */     }
/* 406:    */   }
/* 407:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.sctp.nio.NioSctpChannel
 * JD-Core Version:    0.7.0.1
 */