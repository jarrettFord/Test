/*   1:    */ package io.netty.channel.nio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.AbstractChannel;
/*   4:    */ import io.netty.channel.AbstractChannel.AbstractUnsafe;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.Channel.Unsafe;
/*   7:    */ import io.netty.channel.ChannelConfig;
/*   8:    */ import io.netty.channel.ChannelException;
/*   9:    */ import io.netty.channel.ChannelFuture;
/*  10:    */ import io.netty.channel.ChannelFutureListener;
/*  11:    */ import io.netty.channel.ChannelPipeline;
/*  12:    */ import io.netty.channel.ChannelPromise;
/*  13:    */ import io.netty.channel.ConnectTimeoutException;
/*  14:    */ import io.netty.channel.EventLoop;
/*  15:    */ import io.netty.util.internal.OneTimeTask;
/*  16:    */ import io.netty.util.internal.logging.InternalLogger;
/*  17:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  18:    */ import java.io.IOException;
/*  19:    */ import java.net.ConnectException;
/*  20:    */ import java.net.SocketAddress;
/*  21:    */ import java.nio.channels.CancelledKeyException;
/*  22:    */ import java.nio.channels.SelectableChannel;
/*  23:    */ import java.nio.channels.SelectionKey;
/*  24:    */ import java.util.concurrent.ScheduledFuture;
/*  25:    */ import java.util.concurrent.TimeUnit;
/*  26:    */ 
/*  27:    */ public abstract class AbstractNioChannel
/*  28:    */   extends AbstractChannel
/*  29:    */ {
/*  30: 44 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractNioChannel.class);
/*  31:    */   private final SelectableChannel ch;
/*  32:    */   protected final int readInterestOp;
/*  33:    */   volatile SelectionKey selectionKey;
/*  34:    */   private volatile boolean inputShutdown;
/*  35:    */   private volatile boolean readPending;
/*  36:    */   private ChannelPromise connectPromise;
/*  37:    */   private ScheduledFuture<?> connectTimeoutFuture;
/*  38:    */   private SocketAddress requestedRemoteAddress;
/*  39:    */   
/*  40:    */   protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp)
/*  41:    */   {
/*  42: 69 */     super(parent);
/*  43: 70 */     this.ch = ch;
/*  44: 71 */     this.readInterestOp = readInterestOp;
/*  45:    */     try
/*  46:    */     {
/*  47: 73 */       ch.configureBlocking(false);
/*  48:    */     }
/*  49:    */     catch (IOException e)
/*  50:    */     {
/*  51:    */       try
/*  52:    */       {
/*  53: 76 */         ch.close();
/*  54:    */       }
/*  55:    */       catch (IOException e2)
/*  56:    */       {
/*  57: 78 */         if (logger.isWarnEnabled()) {
/*  58: 79 */           logger.warn("Failed to close a partially initialized socket.", e2);
/*  59:    */         }
/*  60:    */       }
/*  61: 84 */       throw new ChannelException("Failed to enter non-blocking mode.", e);
/*  62:    */     }
/*  63:    */   }
/*  64:    */   
/*  65:    */   public boolean isOpen()
/*  66:    */   {
/*  67: 90 */     return this.ch.isOpen();
/*  68:    */   }
/*  69:    */   
/*  70:    */   public NioUnsafe unsafe()
/*  71:    */   {
/*  72: 95 */     return (NioUnsafe)super.unsafe();
/*  73:    */   }
/*  74:    */   
/*  75:    */   protected SelectableChannel javaChannel()
/*  76:    */   {
/*  77: 99 */     return this.ch;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public NioEventLoop eventLoop()
/*  81:    */   {
/*  82:104 */     return (NioEventLoop)super.eventLoop();
/*  83:    */   }
/*  84:    */   
/*  85:    */   protected SelectionKey selectionKey()
/*  86:    */   {
/*  87:111 */     assert (this.selectionKey != null);
/*  88:112 */     return this.selectionKey;
/*  89:    */   }
/*  90:    */   
/*  91:    */   protected boolean isReadPending()
/*  92:    */   {
/*  93:116 */     return this.readPending;
/*  94:    */   }
/*  95:    */   
/*  96:    */   protected void setReadPending(boolean readPending)
/*  97:    */   {
/*  98:120 */     this.readPending = readPending;
/*  99:    */   }
/* 100:    */   
/* 101:    */   protected boolean isInputShutdown()
/* 102:    */   {
/* 103:127 */     return this.inputShutdown;
/* 104:    */   }
/* 105:    */   
/* 106:    */   void setInputShutdown()
/* 107:    */   {
/* 108:134 */     this.inputShutdown = true;
/* 109:    */   }
/* 110:    */   
/* 111:    */   protected abstract class AbstractNioUnsafe
/* 112:    */     extends AbstractChannel.AbstractUnsafe
/* 113:    */     implements AbstractNioChannel.NioUnsafe
/* 114:    */   {
/* 115:    */     protected AbstractNioUnsafe()
/* 116:    */     {
/* 117:159 */       super();
/* 118:    */     }
/* 119:    */     
/* 120:    */     protected final void removeReadOp()
/* 121:    */     {
/* 122:162 */       SelectionKey key = AbstractNioChannel.this.selectionKey();
/* 123:166 */       if (!key.isValid()) {
/* 124:167 */         return;
/* 125:    */       }
/* 126:169 */       int interestOps = key.interestOps();
/* 127:170 */       if ((interestOps & AbstractNioChannel.this.readInterestOp) != 0) {
/* 128:172 */         key.interestOps(interestOps & (AbstractNioChannel.this.readInterestOp ^ 0xFFFFFFFF));
/* 129:    */       }
/* 130:    */     }
/* 131:    */     
/* 132:    */     public void beginRead()
/* 133:    */     {
/* 134:179 */       AbstractNioChannel.this.readPending = true;
/* 135:180 */       super.beginRead();
/* 136:    */     }
/* 137:    */     
/* 138:    */     public SelectableChannel ch()
/* 139:    */     {
/* 140:185 */       return AbstractNioChannel.this.javaChannel();
/* 141:    */     }
/* 142:    */     
/* 143:    */     public void connect(final SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 144:    */     {
/* 145:191 */       if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/* 146:192 */         return;
/* 147:    */       }
/* 148:    */       try
/* 149:    */       {
/* 150:196 */         if (AbstractNioChannel.this.connectPromise != null) {
/* 151:197 */           throw new IllegalStateException("connection attempt already made");
/* 152:    */         }
/* 153:200 */         boolean wasActive = AbstractNioChannel.this.isActive();
/* 154:201 */         if (AbstractNioChannel.this.doConnect(remoteAddress, localAddress))
/* 155:    */         {
/* 156:202 */           fulfillConnectPromise(promise, wasActive);
/* 157:    */         }
/* 158:    */         else
/* 159:    */         {
/* 160:204 */           AbstractNioChannel.this.connectPromise = promise;
/* 161:205 */           AbstractNioChannel.this.requestedRemoteAddress = remoteAddress;
/* 162:    */           
/* 163:    */ 
/* 164:208 */           int connectTimeoutMillis = AbstractNioChannel.this.config().getConnectTimeoutMillis();
/* 165:209 */           if (connectTimeoutMillis > 0) {
/* 166:210 */             AbstractNioChannel.this.connectTimeoutFuture = AbstractNioChannel.this.eventLoop().schedule(new OneTimeTask()
/* 167:    */             {
/* 168:    */               public void run()
/* 169:    */               {
/* 170:213 */                 ChannelPromise connectPromise = AbstractNioChannel.this.connectPromise;
/* 171:214 */                 ConnectTimeoutException cause = new ConnectTimeoutException("connection timed out: " + remoteAddress);
/* 172:216 */                 if ((connectPromise != null) && (connectPromise.tryFailure(cause))) {
/* 173:217 */                   AbstractNioChannel.AbstractNioUnsafe.this.close(AbstractNioChannel.AbstractNioUnsafe.this.voidPromise());
/* 174:    */                 }
/* 175:    */               }
/* 176:217 */             }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
/* 177:    */           }
/* 178:223 */           promise.addListener(new ChannelFutureListener()
/* 179:    */           {
/* 180:    */             public void operationComplete(ChannelFuture future)
/* 181:    */               throws Exception
/* 182:    */             {
/* 183:226 */               if (future.isCancelled())
/* 184:    */               {
/* 185:227 */                 if (AbstractNioChannel.this.connectTimeoutFuture != null) {
/* 186:228 */                   AbstractNioChannel.this.connectTimeoutFuture.cancel(false);
/* 187:    */                 }
/* 188:230 */                 AbstractNioChannel.this.connectPromise = null;
/* 189:231 */                 AbstractNioChannel.AbstractNioUnsafe.this.close(AbstractNioChannel.AbstractNioUnsafe.this.voidPromise());
/* 190:    */               }
/* 191:    */             }
/* 192:    */           });
/* 193:    */         }
/* 194:    */       }
/* 195:    */       catch (Throwable t)
/* 196:    */       {
/* 197:237 */         if ((t instanceof ConnectException))
/* 198:    */         {
/* 199:238 */           Throwable newT = new ConnectException(t.getMessage() + ": " + remoteAddress);
/* 200:239 */           newT.setStackTrace(t.getStackTrace());
/* 201:240 */           t = newT;
/* 202:    */         }
/* 203:242 */         promise.tryFailure(t);
/* 204:243 */         closeIfClosed();
/* 205:    */       }
/* 206:    */     }
/* 207:    */     
/* 208:    */     private void fulfillConnectPromise(ChannelPromise promise, boolean wasActive)
/* 209:    */     {
/* 210:248 */       if (promise == null) {
/* 211:250 */         return;
/* 212:    */       }
/* 213:254 */       boolean promiseSet = promise.trySuccess();
/* 214:258 */       if ((!wasActive) && (AbstractNioChannel.this.isActive())) {
/* 215:259 */         AbstractNioChannel.this.pipeline().fireChannelActive();
/* 216:    */       }
/* 217:263 */       if (!promiseSet) {
/* 218:264 */         close(voidPromise());
/* 219:    */       }
/* 220:    */     }
/* 221:    */     
/* 222:    */     private void fulfillConnectPromise(ChannelPromise promise, Throwable cause)
/* 223:    */     {
/* 224:269 */       if (promise == null) {}
/* 225:274 */       promise.tryFailure(cause);
/* 226:275 */       closeIfClosed();
/* 227:    */     }
/* 228:    */     
/* 229:    */     public void finishConnect()
/* 230:    */     {
/* 231:283 */       assert (AbstractNioChannel.this.eventLoop().inEventLoop());
/* 232:    */       try
/* 233:    */       {
/* 234:286 */         boolean wasActive = AbstractNioChannel.this.isActive();
/* 235:287 */         AbstractNioChannel.this.doFinishConnect();
/* 236:288 */         fulfillConnectPromise(AbstractNioChannel.this.connectPromise, wasActive);
/* 237:    */       }
/* 238:    */       catch (Throwable t)
/* 239:    */       {
/* 240:290 */         if ((t instanceof ConnectException))
/* 241:    */         {
/* 242:291 */           Throwable newT = new ConnectException(t.getMessage() + ": " + AbstractNioChannel.this.requestedRemoteAddress);
/* 243:292 */           newT.setStackTrace(t.getStackTrace());
/* 244:293 */           t = newT;
/* 245:    */         }
/* 246:296 */         fulfillConnectPromise(AbstractNioChannel.this.connectPromise, t);
/* 247:    */       }
/* 248:    */       finally
/* 249:    */       {
/* 250:300 */         if (AbstractNioChannel.this.connectTimeoutFuture != null) {
/* 251:301 */           AbstractNioChannel.this.connectTimeoutFuture.cancel(false);
/* 252:    */         }
/* 253:303 */         AbstractNioChannel.this.connectPromise = null;
/* 254:    */       }
/* 255:    */     }
/* 256:    */     
/* 257:    */     protected void flush0()
/* 258:    */     {
/* 259:312 */       if (isFlushPending()) {
/* 260:313 */         return;
/* 261:    */       }
/* 262:315 */       super.flush0();
/* 263:    */     }
/* 264:    */     
/* 265:    */     public void forceFlush()
/* 266:    */     {
/* 267:321 */       super.flush0();
/* 268:    */     }
/* 269:    */     
/* 270:    */     private boolean isFlushPending()
/* 271:    */     {
/* 272:325 */       SelectionKey selectionKey = AbstractNioChannel.this.selectionKey();
/* 273:326 */       return (selectionKey.isValid()) && ((selectionKey.interestOps() & 0x4) != 0);
/* 274:    */     }
/* 275:    */   }
/* 276:    */   
/* 277:    */   protected boolean isCompatible(EventLoop loop)
/* 278:    */   {
/* 279:332 */     return loop instanceof NioEventLoop;
/* 280:    */   }
/* 281:    */   
/* 282:    */   protected void doRegister()
/* 283:    */     throws Exception
/* 284:    */   {
/* 285:337 */     boolean selected = false;
/* 286:    */     for (;;)
/* 287:    */     {
/* 288:    */       try
/* 289:    */       {
/* 290:340 */         this.selectionKey = javaChannel().register(eventLoop().selector, 0, this);
/* 291:341 */         return;
/* 292:    */       }
/* 293:    */       catch (CancelledKeyException e)
/* 294:    */       {
/* 295:343 */         if (!selected)
/* 296:    */         {
/* 297:346 */           eventLoop().selectNow();
/* 298:347 */           selected = true;
/* 299:    */         }
/* 300:    */         else
/* 301:    */         {
/* 302:351 */           throw e;
/* 303:    */         }
/* 304:    */       }
/* 305:    */     }
/* 306:    */   }
/* 307:    */   
/* 308:    */   protected void doDeregister()
/* 309:    */     throws Exception
/* 310:    */   {
/* 311:359 */     eventLoop().cancel(selectionKey());
/* 312:    */   }
/* 313:    */   
/* 314:    */   protected void doBeginRead()
/* 315:    */     throws Exception
/* 316:    */   {
/* 317:364 */     if (this.inputShutdown) {
/* 318:365 */       return;
/* 319:    */     }
/* 320:368 */     SelectionKey selectionKey = this.selectionKey;
/* 321:369 */     if (!selectionKey.isValid()) {
/* 322:370 */       return;
/* 323:    */     }
/* 324:373 */     int interestOps = selectionKey.interestOps();
/* 325:374 */     if ((interestOps & this.readInterestOp) == 0) {
/* 326:375 */       selectionKey.interestOps(interestOps | this.readInterestOp);
/* 327:    */     }
/* 328:    */   }
/* 329:    */   
/* 330:    */   protected abstract boolean doConnect(SocketAddress paramSocketAddress1, SocketAddress paramSocketAddress2)
/* 331:    */     throws Exception;
/* 332:    */   
/* 333:    */   protected abstract void doFinishConnect()
/* 334:    */     throws Exception;
/* 335:    */   
/* 336:    */   public static abstract interface NioUnsafe
/* 337:    */     extends Channel.Unsafe
/* 338:    */   {
/* 339:    */     public abstract SelectableChannel ch();
/* 340:    */     
/* 341:    */     public abstract void finishConnect();
/* 342:    */     
/* 343:    */     public abstract void read();
/* 344:    */     
/* 345:    */     public abstract void forceFlush();
/* 346:    */   }
/* 347:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.nio.AbstractNioChannel
 * JD-Core Version:    0.7.0.1
 */