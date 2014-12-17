/*   1:    */ package io.netty.channel.local;
/*   2:    */ 
/*   3:    */ import io.netty.channel.AbstractChannel;
/*   4:    */ import io.netty.channel.AbstractChannel.AbstractUnsafe;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.Channel.Unsafe;
/*   7:    */ import io.netty.channel.ChannelConfig;
/*   8:    */ import io.netty.channel.ChannelException;
/*   9:    */ import io.netty.channel.ChannelMetadata;
/*  10:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  11:    */ import io.netty.channel.ChannelPipeline;
/*  12:    */ import io.netty.channel.ChannelPromise;
/*  13:    */ import io.netty.channel.DefaultChannelConfig;
/*  14:    */ import io.netty.channel.EventLoop;
/*  15:    */ import io.netty.channel.SingleThreadEventLoop;
/*  16:    */ import io.netty.util.ReferenceCountUtil;
/*  17:    */ import io.netty.util.concurrent.SingleThreadEventExecutor;
/*  18:    */ import io.netty.util.internal.InternalThreadLocalMap;
/*  19:    */ import java.net.SocketAddress;
/*  20:    */ import java.nio.channels.AlreadyConnectedException;
/*  21:    */ import java.nio.channels.ClosedChannelException;
/*  22:    */ import java.nio.channels.ConnectionPendingException;
/*  23:    */ import java.nio.channels.NotYetConnectedException;
/*  24:    */ import java.util.ArrayDeque;
/*  25:    */ import java.util.Collections;
/*  26:    */ import java.util.Queue;
/*  27:    */ 
/*  28:    */ public class LocalChannel
/*  29:    */   extends AbstractChannel
/*  30:    */ {
/*  31: 47 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  32:    */   private static final int MAX_READER_STACK_DEPTH = 8;
/*  33: 51 */   private final ChannelConfig config = new DefaultChannelConfig(this);
/*  34: 52 */   private final Queue<Object> inboundBuffer = new ArrayDeque();
/*  35: 53 */   private final Runnable readTask = new Runnable()
/*  36:    */   {
/*  37:    */     public void run()
/*  38:    */     {
/*  39: 56 */       ChannelPipeline pipeline = LocalChannel.this.pipeline();
/*  40:    */       for (;;)
/*  41:    */       {
/*  42: 58 */         Object m = LocalChannel.this.inboundBuffer.poll();
/*  43: 59 */         if (m == null) {
/*  44:    */           break;
/*  45:    */         }
/*  46: 62 */         pipeline.fireChannelRead(m);
/*  47:    */       }
/*  48: 64 */       pipeline.fireChannelReadComplete();
/*  49:    */     }
/*  50:    */   };
/*  51: 68 */   private final Runnable shutdownHook = new Runnable()
/*  52:    */   {
/*  53:    */     public void run()
/*  54:    */     {
/*  55: 71 */       LocalChannel.this.unsafe().close(LocalChannel.this.unsafe().voidPromise());
/*  56:    */     }
/*  57:    */   };
/*  58:    */   private volatile int state;
/*  59:    */   private volatile LocalChannel peer;
/*  60:    */   private volatile LocalAddress localAddress;
/*  61:    */   private volatile LocalAddress remoteAddress;
/*  62:    */   private volatile ChannelPromise connectPromise;
/*  63:    */   private volatile boolean readInProgress;
/*  64:    */   private volatile boolean registerInProgress;
/*  65:    */   
/*  66:    */   public LocalChannel()
/*  67:    */   {
/*  68: 84 */     super(null);
/*  69:    */   }
/*  70:    */   
/*  71:    */   LocalChannel(LocalServerChannel parent, LocalChannel peer)
/*  72:    */   {
/*  73: 88 */     super(parent);
/*  74: 89 */     this.peer = peer;
/*  75: 90 */     this.localAddress = parent.localAddress();
/*  76: 91 */     this.remoteAddress = peer.localAddress();
/*  77:    */   }
/*  78:    */   
/*  79:    */   public ChannelMetadata metadata()
/*  80:    */   {
/*  81: 96 */     return METADATA;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public ChannelConfig config()
/*  85:    */   {
/*  86:101 */     return this.config;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public LocalServerChannel parent()
/*  90:    */   {
/*  91:106 */     return (LocalServerChannel)super.parent();
/*  92:    */   }
/*  93:    */   
/*  94:    */   public LocalAddress localAddress()
/*  95:    */   {
/*  96:111 */     return (LocalAddress)super.localAddress();
/*  97:    */   }
/*  98:    */   
/*  99:    */   public LocalAddress remoteAddress()
/* 100:    */   {
/* 101:116 */     return (LocalAddress)super.remoteAddress();
/* 102:    */   }
/* 103:    */   
/* 104:    */   public boolean isOpen()
/* 105:    */   {
/* 106:121 */     return this.state < 3;
/* 107:    */   }
/* 108:    */   
/* 109:    */   public boolean isActive()
/* 110:    */   {
/* 111:126 */     return this.state == 2;
/* 112:    */   }
/* 113:    */   
/* 114:    */   protected AbstractChannel.AbstractUnsafe newUnsafe()
/* 115:    */   {
/* 116:131 */     return new LocalUnsafe(null);
/* 117:    */   }
/* 118:    */   
/* 119:    */   protected boolean isCompatible(EventLoop loop)
/* 120:    */   {
/* 121:136 */     return loop instanceof SingleThreadEventLoop;
/* 122:    */   }
/* 123:    */   
/* 124:    */   protected SocketAddress localAddress0()
/* 125:    */   {
/* 126:141 */     return this.localAddress;
/* 127:    */   }
/* 128:    */   
/* 129:    */   protected SocketAddress remoteAddress0()
/* 130:    */   {
/* 131:146 */     return this.remoteAddress;
/* 132:    */   }
/* 133:    */   
/* 134:    */   protected void doRegister()
/* 135:    */     throws Exception
/* 136:    */   {
/* 137:156 */     if ((this.peer != null) && (parent() != null))
/* 138:    */     {
/* 139:163 */       final LocalChannel peer = this.peer;
/* 140:164 */       this.registerInProgress = true;
/* 141:165 */       this.state = 2;
/* 142:    */       
/* 143:167 */       peer.remoteAddress = parent().localAddress();
/* 144:168 */       peer.state = 2;
/* 145:    */       
/* 146:    */ 
/* 147:    */ 
/* 148:    */ 
/* 149:    */ 
/* 150:174 */       peer.eventLoop().execute(new Runnable()
/* 151:    */       {
/* 152:    */         public void run()
/* 153:    */         {
/* 154:177 */           LocalChannel.this.registerInProgress = false;
/* 155:178 */           peer.pipeline().fireChannelActive();
/* 156:179 */           peer.connectPromise.setSuccess();
/* 157:    */         }
/* 158:    */       });
/* 159:    */     }
/* 160:183 */     ((SingleThreadEventExecutor)eventLoop()).addShutdownHook(this.shutdownHook);
/* 161:    */   }
/* 162:    */   
/* 163:    */   protected void doBind(SocketAddress localAddress)
/* 164:    */     throws Exception
/* 165:    */   {
/* 166:188 */     this.localAddress = LocalChannelRegistry.register(this, this.localAddress, localAddress);
/* 167:    */     
/* 168:    */ 
/* 169:191 */     this.state = 1;
/* 170:    */   }
/* 171:    */   
/* 172:    */   protected void doDisconnect()
/* 173:    */     throws Exception
/* 174:    */   {
/* 175:196 */     doClose();
/* 176:    */   }
/* 177:    */   
/* 178:    */   protected void doClose()
/* 179:    */     throws Exception
/* 180:    */   {
/* 181:201 */     if (this.state <= 2)
/* 182:    */     {
/* 183:203 */       if (this.localAddress != null)
/* 184:    */       {
/* 185:204 */         if (parent() == null) {
/* 186:205 */           LocalChannelRegistry.unregister(this.localAddress);
/* 187:    */         }
/* 188:207 */         this.localAddress = null;
/* 189:    */       }
/* 190:209 */       this.state = 3;
/* 191:    */     }
/* 192:212 */     final LocalChannel peer = this.peer;
/* 193:213 */     if ((peer != null) && (peer.isActive()))
/* 194:    */     {
/* 195:216 */       EventLoop eventLoop = peer.eventLoop();
/* 196:222 */       if ((eventLoop.inEventLoop()) && (!this.registerInProgress)) {
/* 197:223 */         peer.unsafe().close(unsafe().voidPromise());
/* 198:    */       } else {
/* 199:225 */         peer.eventLoop().execute(new Runnable()
/* 200:    */         {
/* 201:    */           public void run()
/* 202:    */           {
/* 203:228 */             peer.unsafe().close(LocalChannel.this.unsafe().voidPromise());
/* 204:    */           }
/* 205:    */         });
/* 206:    */       }
/* 207:232 */       this.peer = null;
/* 208:    */     }
/* 209:    */   }
/* 210:    */   
/* 211:    */   protected void doDeregister()
/* 212:    */     throws Exception
/* 213:    */   {
/* 214:239 */     ((SingleThreadEventExecutor)eventLoop()).removeShutdownHook(this.shutdownHook);
/* 215:    */   }
/* 216:    */   
/* 217:    */   protected void doBeginRead()
/* 218:    */     throws Exception
/* 219:    */   {
/* 220:244 */     if (this.readInProgress) {
/* 221:245 */       return;
/* 222:    */     }
/* 223:248 */     ChannelPipeline pipeline = pipeline();
/* 224:249 */     Queue<Object> inboundBuffer = this.inboundBuffer;
/* 225:250 */     if (inboundBuffer.isEmpty())
/* 226:    */     {
/* 227:251 */       this.readInProgress = true;
/* 228:252 */       return;
/* 229:    */     }
/* 230:255 */     InternalThreadLocalMap threadLocals = InternalThreadLocalMap.get();
/* 231:256 */     Integer stackDepth = Integer.valueOf(threadLocals.localChannelReaderStackDepth());
/* 232:257 */     if (stackDepth.intValue() < 8)
/* 233:    */     {
/* 234:258 */       threadLocals.setLocalChannelReaderStackDepth(stackDepth.intValue() + 1);
/* 235:    */       try
/* 236:    */       {
/* 237:    */         for (;;)
/* 238:    */         {
/* 239:261 */           Object received = inboundBuffer.poll();
/* 240:262 */           if (received == null) {
/* 241:    */             break;
/* 242:    */           }
/* 243:265 */           pipeline.fireChannelRead(received);
/* 244:    */         }
/* 245:267 */         pipeline.fireChannelReadComplete();
/* 246:    */       }
/* 247:    */       finally
/* 248:    */       {
/* 249:269 */         threadLocals.setLocalChannelReaderStackDepth(stackDepth.intValue());
/* 250:    */       }
/* 251:    */     }
/* 252:    */     else
/* 253:    */     {
/* 254:272 */       eventLoop().execute(this.readTask);
/* 255:    */     }
/* 256:    */   }
/* 257:    */   
/* 258:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 259:    */     throws Exception
/* 260:    */   {
/* 261:278 */     if (this.state < 2) {
/* 262:279 */       throw new NotYetConnectedException();
/* 263:    */     }
/* 264:281 */     if (this.state > 2) {
/* 265:282 */       throw new ClosedChannelException();
/* 266:    */     }
/* 267:285 */     final LocalChannel peer = this.peer;
/* 268:286 */     final ChannelPipeline peerPipeline = peer.pipeline();
/* 269:287 */     EventLoop peerLoop = peer.eventLoop();
/* 270:289 */     if (peerLoop == eventLoop())
/* 271:    */     {
/* 272:    */       for (;;)
/* 273:    */       {
/* 274:291 */         Object msg = in.current();
/* 275:292 */         if (msg == null) {
/* 276:    */           break;
/* 277:    */         }
/* 278:295 */         peer.inboundBuffer.add(msg);
/* 279:296 */         ReferenceCountUtil.retain(msg);
/* 280:297 */         in.remove();
/* 281:    */       }
/* 282:299 */       finishPeerRead(peer, peerPipeline);
/* 283:    */     }
/* 284:    */     else
/* 285:    */     {
/* 286:302 */       final Object[] msgsCopy = new Object[in.size()];
/* 287:303 */       for (int i = 0; i < msgsCopy.length; i++)
/* 288:    */       {
/* 289:304 */         msgsCopy[i] = ReferenceCountUtil.retain(in.current());
/* 290:305 */         in.remove();
/* 291:    */       }
/* 292:308 */       peerLoop.execute(new Runnable()
/* 293:    */       {
/* 294:    */         public void run()
/* 295:    */         {
/* 296:311 */           Collections.addAll(peer.inboundBuffer, msgsCopy);
/* 297:312 */           LocalChannel.finishPeerRead(peer, peerPipeline);
/* 298:    */         }
/* 299:    */       });
/* 300:    */     }
/* 301:    */   }
/* 302:    */   
/* 303:    */   private static void finishPeerRead(LocalChannel peer, ChannelPipeline peerPipeline)
/* 304:    */   {
/* 305:319 */     if (peer.readInProgress)
/* 306:    */     {
/* 307:320 */       peer.readInProgress = false;
/* 308:    */       for (;;)
/* 309:    */       {
/* 310:322 */         Object received = peer.inboundBuffer.poll();
/* 311:323 */         if (received == null) {
/* 312:    */           break;
/* 313:    */         }
/* 314:326 */         peerPipeline.fireChannelRead(received);
/* 315:    */       }
/* 316:328 */       peerPipeline.fireChannelReadComplete();
/* 317:    */     }
/* 318:    */   }
/* 319:    */   
/* 320:    */   private class LocalUnsafe
/* 321:    */     extends AbstractChannel.AbstractUnsafe
/* 322:    */   {
/* 323:    */     private LocalUnsafe()
/* 324:    */     {
/* 325:332 */       super();
/* 326:    */     }
/* 327:    */     
/* 328:    */     public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 329:    */     {
/* 330:337 */       if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/* 331:338 */         return;
/* 332:    */       }
/* 333:341 */       if (LocalChannel.this.state == 2)
/* 334:    */       {
/* 335:342 */         Exception cause = new AlreadyConnectedException();
/* 336:343 */         safeSetFailure(promise, cause);
/* 337:344 */         LocalChannel.this.pipeline().fireExceptionCaught(cause);
/* 338:345 */         return;
/* 339:    */       }
/* 340:348 */       if (LocalChannel.this.connectPromise != null) {
/* 341:349 */         throw new ConnectionPendingException();
/* 342:    */       }
/* 343:352 */       LocalChannel.this.connectPromise = promise;
/* 344:354 */       if (LocalChannel.this.state != 1) {
/* 345:356 */         if (localAddress == null) {
/* 346:357 */           localAddress = new LocalAddress(LocalChannel.this);
/* 347:    */         }
/* 348:    */       }
/* 349:361 */       if (localAddress != null) {
/* 350:    */         try
/* 351:    */         {
/* 352:363 */           LocalChannel.this.doBind(localAddress);
/* 353:    */         }
/* 354:    */         catch (Throwable t)
/* 355:    */         {
/* 356:365 */           safeSetFailure(promise, t);
/* 357:366 */           close(voidPromise());
/* 358:367 */           return;
/* 359:    */         }
/* 360:    */       }
/* 361:371 */       Channel boundChannel = LocalChannelRegistry.get(remoteAddress);
/* 362:372 */       if (!(boundChannel instanceof LocalServerChannel))
/* 363:    */       {
/* 364:373 */         Exception cause = new ChannelException("connection refused");
/* 365:374 */         safeSetFailure(promise, cause);
/* 366:375 */         close(voidPromise());
/* 367:376 */         return;
/* 368:    */       }
/* 369:379 */       LocalServerChannel serverChannel = (LocalServerChannel)boundChannel;
/* 370:380 */       LocalChannel.this.peer = serverChannel.serve(LocalChannel.this);
/* 371:    */     }
/* 372:    */   }
/* 373:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.local.LocalChannel
 * JD-Core Version:    0.7.0.1
 */