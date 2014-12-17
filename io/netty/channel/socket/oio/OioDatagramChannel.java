/*   1:    */ package io.netty.channel.socket.oio;
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
/*  13:    */ import io.netty.channel.RecvByteBufAllocator;
/*  14:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  15:    */ import io.netty.channel.oio.AbstractOioMessageChannel;
/*  16:    */ import io.netty.channel.socket.DatagramChannel;
/*  17:    */ import io.netty.channel.socket.DatagramChannelConfig;
/*  18:    */ import io.netty.channel.socket.DefaultDatagramChannelConfig;
/*  19:    */ import io.netty.util.internal.EmptyArrays;
/*  20:    */ import io.netty.util.internal.PlatformDependent;
/*  21:    */ import io.netty.util.internal.StringUtil;
/*  22:    */ import io.netty.util.internal.logging.InternalLogger;
/*  23:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  24:    */ import java.io.IOException;
/*  25:    */ import java.net.InetAddress;
/*  26:    */ import java.net.InetSocketAddress;
/*  27:    */ import java.net.MulticastSocket;
/*  28:    */ import java.net.NetworkInterface;
/*  29:    */ import java.net.SocketAddress;
/*  30:    */ import java.net.SocketException;
/*  31:    */ import java.net.SocketTimeoutException;
/*  32:    */ import java.util.List;
/*  33:    */ import java.util.Locale;
/*  34:    */ 
/*  35:    */ public class OioDatagramChannel
/*  36:    */   extends AbstractOioMessageChannel
/*  37:    */   implements DatagramChannel
/*  38:    */ {
/*  39: 61 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(OioDatagramChannel.class);
/*  40: 63 */   private static final ChannelMetadata METADATA = new ChannelMetadata(true);
/*  41:    */   private final MulticastSocket socket;
/*  42:    */   private final DatagramChannelConfig config;
/*  43: 67 */   private final java.net.DatagramPacket tmpPacket = new java.net.DatagramPacket(EmptyArrays.EMPTY_BYTES, 0);
/*  44:    */   private RecvByteBufAllocator.Handle allocHandle;
/*  45:    */   
/*  46:    */   private static MulticastSocket newSocket()
/*  47:    */   {
/*  48:    */     try
/*  49:    */     {
/*  50: 73 */       return new MulticastSocket(null);
/*  51:    */     }
/*  52:    */     catch (Exception e)
/*  53:    */     {
/*  54: 75 */       throw new ChannelException("failed to create a new socket", e);
/*  55:    */     }
/*  56:    */   }
/*  57:    */   
/*  58:    */   public OioDatagramChannel()
/*  59:    */   {
/*  60: 83 */     this(newSocket());
/*  61:    */   }
/*  62:    */   
/*  63:    */   public OioDatagramChannel(MulticastSocket socket)
/*  64:    */   {
/*  65: 92 */     super(null);
/*  66:    */     
/*  67: 94 */     boolean success = false;
/*  68:    */     try
/*  69:    */     {
/*  70: 96 */       socket.setSoTimeout(1000);
/*  71: 97 */       socket.setBroadcast(false);
/*  72: 98 */       success = true;
/*  73:    */     }
/*  74:    */     catch (SocketException e)
/*  75:    */     {
/*  76:100 */       throw new ChannelException("Failed to configure the datagram socket timeout.", e);
/*  77:    */     }
/*  78:    */     finally
/*  79:    */     {
/*  80:103 */       if (!success) {
/*  81:104 */         socket.close();
/*  82:    */       }
/*  83:    */     }
/*  84:108 */     this.socket = socket;
/*  85:109 */     this.config = new DefaultDatagramChannelConfig(this, socket);
/*  86:    */   }
/*  87:    */   
/*  88:    */   public ChannelMetadata metadata()
/*  89:    */   {
/*  90:114 */     return METADATA;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public DatagramChannelConfig config()
/*  94:    */   {
/*  95:119 */     return this.config;
/*  96:    */   }
/*  97:    */   
/*  98:    */   public boolean isOpen()
/*  99:    */   {
/* 100:124 */     return !this.socket.isClosed();
/* 101:    */   }
/* 102:    */   
/* 103:    */   public boolean isActive()
/* 104:    */   {
/* 105:129 */     return (isOpen()) && (((((Boolean)this.config.getOption(ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION)).booleanValue()) && (isRegistered())) || (this.socket.isBound()));
/* 106:    */   }
/* 107:    */   
/* 108:    */   public boolean isConnected()
/* 109:    */   {
/* 110:136 */     return this.socket.isConnected();
/* 111:    */   }
/* 112:    */   
/* 113:    */   protected SocketAddress localAddress0()
/* 114:    */   {
/* 115:141 */     return this.socket.getLocalSocketAddress();
/* 116:    */   }
/* 117:    */   
/* 118:    */   protected SocketAddress remoteAddress0()
/* 119:    */   {
/* 120:146 */     return this.socket.getRemoteSocketAddress();
/* 121:    */   }
/* 122:    */   
/* 123:    */   protected void doBind(SocketAddress localAddress)
/* 124:    */     throws Exception
/* 125:    */   {
/* 126:151 */     this.socket.bind(localAddress);
/* 127:    */   }
/* 128:    */   
/* 129:    */   public InetSocketAddress localAddress()
/* 130:    */   {
/* 131:156 */     return (InetSocketAddress)super.localAddress();
/* 132:    */   }
/* 133:    */   
/* 134:    */   public InetSocketAddress remoteAddress()
/* 135:    */   {
/* 136:161 */     return (InetSocketAddress)super.remoteAddress();
/* 137:    */   }
/* 138:    */   
/* 139:    */   protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 140:    */     throws Exception
/* 141:    */   {
/* 142:167 */     if (localAddress != null) {
/* 143:168 */       this.socket.bind(localAddress);
/* 144:    */     }
/* 145:171 */     boolean success = false;
/* 146:    */     try
/* 147:    */     {
/* 148:173 */       this.socket.connect(remoteAddress);
/* 149:174 */       success = true; return;
/* 150:    */     }
/* 151:    */     finally
/* 152:    */     {
/* 153:176 */       if (!success) {
/* 154:    */         try
/* 155:    */         {
/* 156:178 */           this.socket.close();
/* 157:    */         }
/* 158:    */         catch (Throwable t)
/* 159:    */         {
/* 160:180 */           logger.warn("Failed to close a socket.", t);
/* 161:    */         }
/* 162:    */       }
/* 163:    */     }
/* 164:    */   }
/* 165:    */   
/* 166:    */   protected void doDisconnect()
/* 167:    */     throws Exception
/* 168:    */   {
/* 169:188 */     this.socket.disconnect();
/* 170:    */   }
/* 171:    */   
/* 172:    */   protected void doClose()
/* 173:    */     throws Exception
/* 174:    */   {
/* 175:193 */     this.socket.close();
/* 176:    */   }
/* 177:    */   
/* 178:    */   protected int doReadMessages(List<Object> buf)
/* 179:    */     throws Exception
/* 180:    */   {
/* 181:198 */     DatagramChannelConfig config = config();
/* 182:199 */     RecvByteBufAllocator.Handle allocHandle = this.allocHandle;
/* 183:200 */     if (allocHandle == null) {
/* 184:201 */       this.allocHandle = (allocHandle = config.getRecvByteBufAllocator().newHandle());
/* 185:    */     }
/* 186:204 */     ByteBuf data = config.getAllocator().heapBuffer(allocHandle.guess());
/* 187:205 */     boolean free = true;
/* 188:    */     try
/* 189:    */     {
/* 190:207 */       this.tmpPacket.setData(data.array(), data.arrayOffset(), data.capacity());
/* 191:208 */       this.socket.receive(this.tmpPacket);
/* 192:    */       
/* 193:210 */       InetSocketAddress remoteAddr = (InetSocketAddress)this.tmpPacket.getSocketAddress();
/* 194:211 */       if (remoteAddr == null) {
/* 195:212 */         remoteAddr = remoteAddress();
/* 196:    */       }
/* 197:215 */       readBytes = this.tmpPacket.getLength();
/* 198:216 */       allocHandle.record(readBytes);
/* 199:217 */       buf.add(new io.netty.channel.socket.DatagramPacket(data.writerIndex(readBytes), localAddress(), remoteAddr));
/* 200:218 */       free = false;
/* 201:219 */       return 1;
/* 202:    */     }
/* 203:    */     catch (SocketTimeoutException e)
/* 204:    */     {
/* 205:222 */       return 0;
/* 206:    */     }
/* 207:    */     catch (SocketException e)
/* 208:    */     {
/* 209:224 */       if (!e.getMessage().toLowerCase(Locale.US).contains("socket closed")) {
/* 210:225 */         throw e;
/* 211:    */       }
/* 212:227 */       return -1;
/* 213:    */     }
/* 214:    */     catch (Throwable cause)
/* 215:    */     {
/* 216:    */       int readBytes;
/* 217:229 */       PlatformDependent.throwException(cause);
/* 218:230 */       return -1;
/* 219:    */     }
/* 220:    */     finally
/* 221:    */     {
/* 222:232 */       if (free) {
/* 223:233 */         data.release();
/* 224:    */       }
/* 225:    */     }
/* 226:    */   }
/* 227:    */   
/* 228:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 229:    */     throws Exception
/* 230:    */   {
/* 231:    */     for (;;)
/* 232:    */     {
/* 233:241 */       Object o = in.current();
/* 234:242 */       if (o == null) {
/* 235:    */         break;
/* 236:    */       }
/* 237:    */       Object m;
/* 238:    */       Object m;
/* 239:    */       SocketAddress remoteAddress;
/* 240:249 */       if ((o instanceof AddressedEnvelope))
/* 241:    */       {
/* 242:251 */         AddressedEnvelope<Object, SocketAddress> envelope = (AddressedEnvelope)o;
/* 243:252 */         SocketAddress remoteAddress = envelope.recipient();
/* 244:253 */         m = envelope.content();
/* 245:    */       }
/* 246:    */       else
/* 247:    */       {
/* 248:255 */         m = o;
/* 249:256 */         remoteAddress = null;
/* 250:    */       }
/* 251:    */       ByteBuf data;
/* 252:259 */       if ((m instanceof ByteBufHolder))
/* 253:    */       {
/* 254:260 */         data = ((ByteBufHolder)m).content();
/* 255:    */       }
/* 256:    */       else
/* 257:    */       {
/* 258:    */         ByteBuf data;
/* 259:261 */         if ((m instanceof ByteBuf)) {
/* 260:262 */           data = (ByteBuf)m;
/* 261:    */         } else {
/* 262:264 */           throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(o));
/* 263:    */         }
/* 264:    */       }
/* 265:    */       ByteBuf data;
/* 266:267 */       int length = data.readableBytes();
/* 267:268 */       if (remoteAddress != null) {
/* 268:269 */         this.tmpPacket.setSocketAddress(remoteAddress);
/* 269:    */       }
/* 270:271 */       if (data.hasArray())
/* 271:    */       {
/* 272:272 */         this.tmpPacket.setData(data.array(), data.arrayOffset() + data.readerIndex(), length);
/* 273:    */       }
/* 274:    */       else
/* 275:    */       {
/* 276:274 */         byte[] tmp = new byte[length];
/* 277:275 */         data.getBytes(data.readerIndex(), tmp);
/* 278:276 */         this.tmpPacket.setData(tmp);
/* 279:    */       }
/* 280:278 */       this.socket.send(this.tmpPacket);
/* 281:279 */       in.remove();
/* 282:    */     }
/* 283:    */   }
/* 284:    */   
/* 285:    */   public ChannelFuture joinGroup(InetAddress multicastAddress)
/* 286:    */   {
/* 287:285 */     return joinGroup(multicastAddress, newPromise());
/* 288:    */   }
/* 289:    */   
/* 290:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, ChannelPromise promise)
/* 291:    */   {
/* 292:290 */     ensureBound();
/* 293:    */     try
/* 294:    */     {
/* 295:292 */       this.socket.joinGroup(multicastAddress);
/* 296:293 */       promise.setSuccess();
/* 297:    */     }
/* 298:    */     catch (IOException e)
/* 299:    */     {
/* 300:295 */       promise.setFailure(e);
/* 301:    */     }
/* 302:297 */     return promise;
/* 303:    */   }
/* 304:    */   
/* 305:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/* 306:    */   {
/* 307:302 */     return joinGroup(multicastAddress, networkInterface, newPromise());
/* 308:    */   }
/* 309:    */   
/* 310:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/* 311:    */   {
/* 312:309 */     ensureBound();
/* 313:    */     try
/* 314:    */     {
/* 315:311 */       this.socket.joinGroup(multicastAddress, networkInterface);
/* 316:312 */       promise.setSuccess();
/* 317:    */     }
/* 318:    */     catch (IOException e)
/* 319:    */     {
/* 320:314 */       promise.setFailure(e);
/* 321:    */     }
/* 322:316 */     return promise;
/* 323:    */   }
/* 324:    */   
/* 325:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/* 326:    */   {
/* 327:322 */     return newFailedFuture(new UnsupportedOperationException());
/* 328:    */   }
/* 329:    */   
/* 330:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/* 331:    */   {
/* 332:329 */     promise.setFailure(new UnsupportedOperationException());
/* 333:330 */     return promise;
/* 334:    */   }
/* 335:    */   
/* 336:    */   private void ensureBound()
/* 337:    */   {
/* 338:334 */     if (!isActive()) {
/* 339:335 */       throw new IllegalStateException(DatagramChannel.class.getName() + " must be bound to join a group.");
/* 340:    */     }
/* 341:    */   }
/* 342:    */   
/* 343:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress)
/* 344:    */   {
/* 345:343 */     return leaveGroup(multicastAddress, newPromise());
/* 346:    */   }
/* 347:    */   
/* 348:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, ChannelPromise promise)
/* 349:    */   {
/* 350:    */     try
/* 351:    */     {
/* 352:349 */       this.socket.leaveGroup(multicastAddress);
/* 353:350 */       promise.setSuccess();
/* 354:    */     }
/* 355:    */     catch (IOException e)
/* 356:    */     {
/* 357:352 */       promise.setFailure(e);
/* 358:    */     }
/* 359:354 */     return promise;
/* 360:    */   }
/* 361:    */   
/* 362:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/* 363:    */   {
/* 364:360 */     return leaveGroup(multicastAddress, networkInterface, newPromise());
/* 365:    */   }
/* 366:    */   
/* 367:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/* 368:    */   {
/* 369:    */     try
/* 370:    */     {
/* 371:368 */       this.socket.leaveGroup(multicastAddress, networkInterface);
/* 372:369 */       promise.setSuccess();
/* 373:    */     }
/* 374:    */     catch (IOException e)
/* 375:    */     {
/* 376:371 */       promise.setFailure(e);
/* 377:    */     }
/* 378:373 */     return promise;
/* 379:    */   }
/* 380:    */   
/* 381:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/* 382:    */   {
/* 383:379 */     return newFailedFuture(new UnsupportedOperationException());
/* 384:    */   }
/* 385:    */   
/* 386:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/* 387:    */   {
/* 388:386 */     promise.setFailure(new UnsupportedOperationException());
/* 389:387 */     return promise;
/* 390:    */   }
/* 391:    */   
/* 392:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock)
/* 393:    */   {
/* 394:393 */     return newFailedFuture(new UnsupportedOperationException());
/* 395:    */   }
/* 396:    */   
/* 397:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock, ChannelPromise promise)
/* 398:    */   {
/* 399:400 */     promise.setFailure(new UnsupportedOperationException());
/* 400:401 */     return promise;
/* 401:    */   }
/* 402:    */   
/* 403:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock)
/* 404:    */   {
/* 405:407 */     return newFailedFuture(new UnsupportedOperationException());
/* 406:    */   }
/* 407:    */   
/* 408:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock, ChannelPromise promise)
/* 409:    */   {
/* 410:413 */     promise.setFailure(new UnsupportedOperationException());
/* 411:414 */     return promise;
/* 412:    */   }
/* 413:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.socket.oio.OioDatagramChannel
 * JD-Core Version:    0.7.0.1
 */