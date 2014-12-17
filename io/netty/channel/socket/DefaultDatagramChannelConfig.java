/*   1:    */ package io.netty.channel.socket;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.ChannelException;
/*   6:    */ import io.netty.channel.ChannelOption;
/*   7:    */ import io.netty.channel.DefaultChannelConfig;
/*   8:    */ import io.netty.channel.FixedRecvByteBufAllocator;
/*   9:    */ import io.netty.channel.MessageSizeEstimator;
/*  10:    */ import io.netty.channel.RecvByteBufAllocator;
/*  11:    */ import io.netty.util.internal.PlatformDependent;
/*  12:    */ import io.netty.util.internal.logging.InternalLogger;
/*  13:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  14:    */ import java.io.IOException;
/*  15:    */ import java.net.DatagramSocket;
/*  16:    */ import java.net.InetAddress;
/*  17:    */ import java.net.MulticastSocket;
/*  18:    */ import java.net.NetworkInterface;
/*  19:    */ import java.net.SocketException;
/*  20:    */ import java.util.Map;
/*  21:    */ 
/*  22:    */ public class DefaultDatagramChannelConfig
/*  23:    */   extends DefaultChannelConfig
/*  24:    */   implements DatagramChannelConfig
/*  25:    */ {
/*  26: 44 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultDatagramChannelConfig.class);
/*  27: 46 */   private static final RecvByteBufAllocator DEFAULT_RCVBUF_ALLOCATOR = new FixedRecvByteBufAllocator(2048);
/*  28:    */   private final DatagramSocket javaSocket;
/*  29:    */   private volatile boolean activeOnOpen;
/*  30:    */   
/*  31:    */   public DefaultDatagramChannelConfig(DatagramChannel channel, DatagramSocket javaSocket)
/*  32:    */   {
/*  33: 55 */     super(channel);
/*  34: 56 */     if (javaSocket == null) {
/*  35: 57 */       throw new NullPointerException("javaSocket");
/*  36:    */     }
/*  37: 59 */     this.javaSocket = javaSocket;
/*  38: 60 */     setRecvByteBufAllocator(DEFAULT_RCVBUF_ALLOCATOR);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  42:    */   {
/*  43: 65 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_BROADCAST, ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, ChannelOption.SO_REUSEADDR, ChannelOption.IP_MULTICAST_LOOP_DISABLED, ChannelOption.IP_MULTICAST_ADDR, ChannelOption.IP_MULTICAST_IF, ChannelOption.IP_MULTICAST_TTL, ChannelOption.IP_TOS, ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION });
/*  44:    */   }
/*  45:    */   
/*  46:    */   public <T> T getOption(ChannelOption<T> option)
/*  47:    */   {
/*  48: 74 */     if (option == ChannelOption.SO_BROADCAST) {
/*  49: 75 */       return Boolean.valueOf(isBroadcast());
/*  50:    */     }
/*  51: 77 */     if (option == ChannelOption.SO_RCVBUF) {
/*  52: 78 */       return Integer.valueOf(getReceiveBufferSize());
/*  53:    */     }
/*  54: 80 */     if (option == ChannelOption.SO_SNDBUF) {
/*  55: 81 */       return Integer.valueOf(getSendBufferSize());
/*  56:    */     }
/*  57: 83 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  58: 84 */       return Boolean.valueOf(isReuseAddress());
/*  59:    */     }
/*  60: 86 */     if (option == ChannelOption.IP_MULTICAST_LOOP_DISABLED) {
/*  61: 87 */       return Boolean.valueOf(isLoopbackModeDisabled());
/*  62:    */     }
/*  63: 89 */     if (option == ChannelOption.IP_MULTICAST_ADDR)
/*  64:    */     {
/*  65: 90 */       T i = getInterface();
/*  66: 91 */       return i;
/*  67:    */     }
/*  68: 93 */     if (option == ChannelOption.IP_MULTICAST_IF)
/*  69:    */     {
/*  70: 94 */       T i = getNetworkInterface();
/*  71: 95 */       return i;
/*  72:    */     }
/*  73: 97 */     if (option == ChannelOption.IP_MULTICAST_TTL) {
/*  74: 98 */       return Integer.valueOf(getTimeToLive());
/*  75:    */     }
/*  76:100 */     if (option == ChannelOption.IP_TOS) {
/*  77:101 */       return Integer.valueOf(getTrafficClass());
/*  78:    */     }
/*  79:103 */     if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) {
/*  80:104 */       return Boolean.valueOf(this.activeOnOpen);
/*  81:    */     }
/*  82:106 */     return super.getOption(option);
/*  83:    */   }
/*  84:    */   
/*  85:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  86:    */   {
/*  87:111 */     validate(option, value);
/*  88:113 */     if (option == ChannelOption.SO_BROADCAST) {
/*  89:114 */       setBroadcast(((Boolean)value).booleanValue());
/*  90:115 */     } else if (option == ChannelOption.SO_RCVBUF) {
/*  91:116 */       setReceiveBufferSize(((Integer)value).intValue());
/*  92:117 */     } else if (option == ChannelOption.SO_SNDBUF) {
/*  93:118 */       setSendBufferSize(((Integer)value).intValue());
/*  94:119 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  95:120 */       setReuseAddress(((Boolean)value).booleanValue());
/*  96:121 */     } else if (option == ChannelOption.IP_MULTICAST_LOOP_DISABLED) {
/*  97:122 */       setLoopbackModeDisabled(((Boolean)value).booleanValue());
/*  98:123 */     } else if (option == ChannelOption.IP_MULTICAST_ADDR) {
/*  99:124 */       setInterface((InetAddress)value);
/* 100:125 */     } else if (option == ChannelOption.IP_MULTICAST_IF) {
/* 101:126 */       setNetworkInterface((NetworkInterface)value);
/* 102:127 */     } else if (option == ChannelOption.IP_MULTICAST_TTL) {
/* 103:128 */       setTimeToLive(((Integer)value).intValue());
/* 104:129 */     } else if (option == ChannelOption.IP_TOS) {
/* 105:130 */       setTrafficClass(((Integer)value).intValue());
/* 106:131 */     } else if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) {
/* 107:132 */       setActiveOnOpen(((Boolean)value).booleanValue());
/* 108:    */     } else {
/* 109:134 */       return super.setOption(option, value);
/* 110:    */     }
/* 111:137 */     return true;
/* 112:    */   }
/* 113:    */   
/* 114:    */   private void setActiveOnOpen(boolean activeOnOpen)
/* 115:    */   {
/* 116:141 */     if (this.channel.isRegistered()) {
/* 117:142 */       throw new IllegalStateException("Can only changed before channel was registered");
/* 118:    */     }
/* 119:144 */     this.activeOnOpen = activeOnOpen;
/* 120:    */   }
/* 121:    */   
/* 122:    */   public boolean isBroadcast()
/* 123:    */   {
/* 124:    */     try
/* 125:    */     {
/* 126:149 */       return this.javaSocket.getBroadcast();
/* 127:    */     }
/* 128:    */     catch (SocketException e)
/* 129:    */     {
/* 130:151 */       throw new ChannelException(e);
/* 131:    */     }
/* 132:    */   }
/* 133:    */   
/* 134:    */   public DatagramChannelConfig setBroadcast(boolean broadcast)
/* 135:    */   {
/* 136:    */     try
/* 137:    */     {
/* 138:159 */       if ((broadcast) && (!PlatformDependent.isWindows()) && (!PlatformDependent.isRoot()) && (!this.javaSocket.getLocalAddress().isAnyLocalAddress())) {
/* 139:164 */         logger.warn("A non-root user can't receive a broadcast packet if the socket is not bound to a wildcard address; setting the SO_BROADCAST flag anyway as requested on the socket which is bound to " + this.javaSocket.getLocalSocketAddress() + '.');
/* 140:    */       }
/* 141:171 */       this.javaSocket.setBroadcast(broadcast);
/* 142:    */     }
/* 143:    */     catch (SocketException e)
/* 144:    */     {
/* 145:173 */       throw new ChannelException(e);
/* 146:    */     }
/* 147:175 */     return this;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public InetAddress getInterface()
/* 151:    */   {
/* 152:180 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 153:    */       try
/* 154:    */       {
/* 155:182 */         return ((MulticastSocket)this.javaSocket).getInterface();
/* 156:    */       }
/* 157:    */       catch (SocketException e)
/* 158:    */       {
/* 159:184 */         throw new ChannelException(e);
/* 160:    */       }
/* 161:    */     }
/* 162:187 */     throw new UnsupportedOperationException();
/* 163:    */   }
/* 164:    */   
/* 165:    */   public DatagramChannelConfig setInterface(InetAddress interfaceAddress)
/* 166:    */   {
/* 167:193 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 168:    */       try
/* 169:    */       {
/* 170:195 */         ((MulticastSocket)this.javaSocket).setInterface(interfaceAddress);
/* 171:    */       }
/* 172:    */       catch (SocketException e)
/* 173:    */       {
/* 174:197 */         throw new ChannelException(e);
/* 175:    */       }
/* 176:    */     } else {
/* 177:200 */       throw new UnsupportedOperationException();
/* 178:    */     }
/* 179:202 */     return this;
/* 180:    */   }
/* 181:    */   
/* 182:    */   public boolean isLoopbackModeDisabled()
/* 183:    */   {
/* 184:207 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 185:    */       try
/* 186:    */       {
/* 187:209 */         return ((MulticastSocket)this.javaSocket).getLoopbackMode();
/* 188:    */       }
/* 189:    */       catch (SocketException e)
/* 190:    */       {
/* 191:211 */         throw new ChannelException(e);
/* 192:    */       }
/* 193:    */     }
/* 194:214 */     throw new UnsupportedOperationException();
/* 195:    */   }
/* 196:    */   
/* 197:    */   public DatagramChannelConfig setLoopbackModeDisabled(boolean loopbackModeDisabled)
/* 198:    */   {
/* 199:220 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 200:    */       try
/* 201:    */       {
/* 202:222 */         ((MulticastSocket)this.javaSocket).setLoopbackMode(loopbackModeDisabled);
/* 203:    */       }
/* 204:    */       catch (SocketException e)
/* 205:    */       {
/* 206:224 */         throw new ChannelException(e);
/* 207:    */       }
/* 208:    */     } else {
/* 209:227 */       throw new UnsupportedOperationException();
/* 210:    */     }
/* 211:229 */     return this;
/* 212:    */   }
/* 213:    */   
/* 214:    */   public NetworkInterface getNetworkInterface()
/* 215:    */   {
/* 216:234 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 217:    */       try
/* 218:    */       {
/* 219:236 */         return ((MulticastSocket)this.javaSocket).getNetworkInterface();
/* 220:    */       }
/* 221:    */       catch (SocketException e)
/* 222:    */       {
/* 223:238 */         throw new ChannelException(e);
/* 224:    */       }
/* 225:    */     }
/* 226:241 */     throw new UnsupportedOperationException();
/* 227:    */   }
/* 228:    */   
/* 229:    */   public DatagramChannelConfig setNetworkInterface(NetworkInterface networkInterface)
/* 230:    */   {
/* 231:247 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 232:    */       try
/* 233:    */       {
/* 234:249 */         ((MulticastSocket)this.javaSocket).setNetworkInterface(networkInterface);
/* 235:    */       }
/* 236:    */       catch (SocketException e)
/* 237:    */       {
/* 238:251 */         throw new ChannelException(e);
/* 239:    */       }
/* 240:    */     } else {
/* 241:254 */       throw new UnsupportedOperationException();
/* 242:    */     }
/* 243:256 */     return this;
/* 244:    */   }
/* 245:    */   
/* 246:    */   public boolean isReuseAddress()
/* 247:    */   {
/* 248:    */     try
/* 249:    */     {
/* 250:262 */       return this.javaSocket.getReuseAddress();
/* 251:    */     }
/* 252:    */     catch (SocketException e)
/* 253:    */     {
/* 254:264 */       throw new ChannelException(e);
/* 255:    */     }
/* 256:    */   }
/* 257:    */   
/* 258:    */   public DatagramChannelConfig setReuseAddress(boolean reuseAddress)
/* 259:    */   {
/* 260:    */     try
/* 261:    */     {
/* 262:271 */       this.javaSocket.setReuseAddress(reuseAddress);
/* 263:    */     }
/* 264:    */     catch (SocketException e)
/* 265:    */     {
/* 266:273 */       throw new ChannelException(e);
/* 267:    */     }
/* 268:275 */     return this;
/* 269:    */   }
/* 270:    */   
/* 271:    */   public int getReceiveBufferSize()
/* 272:    */   {
/* 273:    */     try
/* 274:    */     {
/* 275:281 */       return this.javaSocket.getReceiveBufferSize();
/* 276:    */     }
/* 277:    */     catch (SocketException e)
/* 278:    */     {
/* 279:283 */       throw new ChannelException(e);
/* 280:    */     }
/* 281:    */   }
/* 282:    */   
/* 283:    */   public DatagramChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 284:    */   {
/* 285:    */     try
/* 286:    */     {
/* 287:290 */       this.javaSocket.setReceiveBufferSize(receiveBufferSize);
/* 288:    */     }
/* 289:    */     catch (SocketException e)
/* 290:    */     {
/* 291:292 */       throw new ChannelException(e);
/* 292:    */     }
/* 293:294 */     return this;
/* 294:    */   }
/* 295:    */   
/* 296:    */   public int getSendBufferSize()
/* 297:    */   {
/* 298:    */     try
/* 299:    */     {
/* 300:300 */       return this.javaSocket.getSendBufferSize();
/* 301:    */     }
/* 302:    */     catch (SocketException e)
/* 303:    */     {
/* 304:302 */       throw new ChannelException(e);
/* 305:    */     }
/* 306:    */   }
/* 307:    */   
/* 308:    */   public DatagramChannelConfig setSendBufferSize(int sendBufferSize)
/* 309:    */   {
/* 310:    */     try
/* 311:    */     {
/* 312:309 */       this.javaSocket.setSendBufferSize(sendBufferSize);
/* 313:    */     }
/* 314:    */     catch (SocketException e)
/* 315:    */     {
/* 316:311 */       throw new ChannelException(e);
/* 317:    */     }
/* 318:313 */     return this;
/* 319:    */   }
/* 320:    */   
/* 321:    */   public int getTimeToLive()
/* 322:    */   {
/* 323:318 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 324:    */       try
/* 325:    */       {
/* 326:320 */         return ((MulticastSocket)this.javaSocket).getTimeToLive();
/* 327:    */       }
/* 328:    */       catch (IOException e)
/* 329:    */       {
/* 330:322 */         throw new ChannelException(e);
/* 331:    */       }
/* 332:    */     }
/* 333:325 */     throw new UnsupportedOperationException();
/* 334:    */   }
/* 335:    */   
/* 336:    */   public DatagramChannelConfig setTimeToLive(int ttl)
/* 337:    */   {
/* 338:331 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 339:    */       try
/* 340:    */       {
/* 341:333 */         ((MulticastSocket)this.javaSocket).setTimeToLive(ttl);
/* 342:    */       }
/* 343:    */       catch (IOException e)
/* 344:    */       {
/* 345:335 */         throw new ChannelException(e);
/* 346:    */       }
/* 347:    */     } else {
/* 348:338 */       throw new UnsupportedOperationException();
/* 349:    */     }
/* 350:340 */     return this;
/* 351:    */   }
/* 352:    */   
/* 353:    */   public int getTrafficClass()
/* 354:    */   {
/* 355:    */     try
/* 356:    */     {
/* 357:346 */       return this.javaSocket.getTrafficClass();
/* 358:    */     }
/* 359:    */     catch (SocketException e)
/* 360:    */     {
/* 361:348 */       throw new ChannelException(e);
/* 362:    */     }
/* 363:    */   }
/* 364:    */   
/* 365:    */   public DatagramChannelConfig setTrafficClass(int trafficClass)
/* 366:    */   {
/* 367:    */     try
/* 368:    */     {
/* 369:355 */       this.javaSocket.setTrafficClass(trafficClass);
/* 370:    */     }
/* 371:    */     catch (SocketException e)
/* 372:    */     {
/* 373:357 */       throw new ChannelException(e);
/* 374:    */     }
/* 375:359 */     return this;
/* 376:    */   }
/* 377:    */   
/* 378:    */   public DatagramChannelConfig setWriteSpinCount(int writeSpinCount)
/* 379:    */   {
/* 380:364 */     super.setWriteSpinCount(writeSpinCount);
/* 381:365 */     return this;
/* 382:    */   }
/* 383:    */   
/* 384:    */   public DatagramChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 385:    */   {
/* 386:370 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 387:371 */     return this;
/* 388:    */   }
/* 389:    */   
/* 390:    */   public DatagramChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 391:    */   {
/* 392:376 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 393:377 */     return this;
/* 394:    */   }
/* 395:    */   
/* 396:    */   public DatagramChannelConfig setAllocator(ByteBufAllocator allocator)
/* 397:    */   {
/* 398:382 */     super.setAllocator(allocator);
/* 399:383 */     return this;
/* 400:    */   }
/* 401:    */   
/* 402:    */   public DatagramChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 403:    */   {
/* 404:388 */     super.setRecvByteBufAllocator(allocator);
/* 405:389 */     return this;
/* 406:    */   }
/* 407:    */   
/* 408:    */   public DatagramChannelConfig setAutoRead(boolean autoRead)
/* 409:    */   {
/* 410:394 */     super.setAutoRead(autoRead);
/* 411:395 */     return this;
/* 412:    */   }
/* 413:    */   
/* 414:    */   public DatagramChannelConfig setAutoClose(boolean autoClose)
/* 415:    */   {
/* 416:400 */     super.setAutoClose(autoClose);
/* 417:401 */     return this;
/* 418:    */   }
/* 419:    */   
/* 420:    */   public DatagramChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 421:    */   {
/* 422:406 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 423:407 */     return this;
/* 424:    */   }
/* 425:    */   
/* 426:    */   public DatagramChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 427:    */   {
/* 428:412 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 429:413 */     return this;
/* 430:    */   }
/* 431:    */   
/* 432:    */   public DatagramChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 433:    */   {
/* 434:418 */     super.setMessageSizeEstimator(estimator);
/* 435:419 */     return this;
/* 436:    */   }
/* 437:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.socket.DefaultDatagramChannelConfig
 * JD-Core Version:    0.7.0.1
 */