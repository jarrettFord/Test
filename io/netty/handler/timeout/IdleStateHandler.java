/*   1:    */ package io.netty.handler.timeout;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelDuplexHandler;
/*   5:    */ import io.netty.channel.ChannelFuture;
/*   6:    */ import io.netty.channel.ChannelFutureListener;
/*   7:    */ import io.netty.channel.ChannelHandlerContext;
/*   8:    */ import io.netty.channel.ChannelPromise;
/*   9:    */ import io.netty.util.concurrent.EventExecutor;
/*  10:    */ import java.util.concurrent.ScheduledFuture;
/*  11:    */ import java.util.concurrent.TimeUnit;
/*  12:    */ 
/*  13:    */ public class IdleStateHandler
/*  14:    */   extends ChannelDuplexHandler
/*  15:    */ {
/*  16: 98 */   private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
/*  17:    */   private final long readerIdleTimeNanos;
/*  18:    */   private final long writerIdleTimeNanos;
/*  19:    */   private final long allIdleTimeNanos;
/*  20:    */   volatile ScheduledFuture<?> readerIdleTimeout;
/*  21:    */   volatile long lastReadTime;
/*  22:106 */   private boolean firstReaderIdleEvent = true;
/*  23:    */   volatile ScheduledFuture<?> writerIdleTimeout;
/*  24:    */   volatile long lastWriteTime;
/*  25:110 */   private boolean firstWriterIdleEvent = true;
/*  26:    */   volatile ScheduledFuture<?> allIdleTimeout;
/*  27:113 */   private boolean firstAllIdleEvent = true;
/*  28:    */   private volatile int state;
/*  29:    */   
/*  30:    */   public IdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds)
/*  31:    */   {
/*  32:138 */     this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public IdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit)
/*  36:    */   {
/*  37:164 */     if (unit == null) {
/*  38:165 */       throw new NullPointerException("unit");
/*  39:    */     }
/*  40:168 */     if (readerIdleTime <= 0L) {
/*  41:169 */       this.readerIdleTimeNanos = 0L;
/*  42:    */     } else {
/*  43:171 */       this.readerIdleTimeNanos = Math.max(unit.toNanos(readerIdleTime), MIN_TIMEOUT_NANOS);
/*  44:    */     }
/*  45:173 */     if (writerIdleTime <= 0L) {
/*  46:174 */       this.writerIdleTimeNanos = 0L;
/*  47:    */     } else {
/*  48:176 */       this.writerIdleTimeNanos = Math.max(unit.toNanos(writerIdleTime), MIN_TIMEOUT_NANOS);
/*  49:    */     }
/*  50:178 */     if (allIdleTime <= 0L) {
/*  51:179 */       this.allIdleTimeNanos = 0L;
/*  52:    */     } else {
/*  53:181 */       this.allIdleTimeNanos = Math.max(unit.toNanos(allIdleTime), MIN_TIMEOUT_NANOS);
/*  54:    */     }
/*  55:    */   }
/*  56:    */   
/*  57:    */   public long getReaderIdleTimeInMillis()
/*  58:    */   {
/*  59:190 */     return TimeUnit.NANOSECONDS.toMillis(this.readerIdleTimeNanos);
/*  60:    */   }
/*  61:    */   
/*  62:    */   public long getWriterIdleTimeInMillis()
/*  63:    */   {
/*  64:198 */     return TimeUnit.NANOSECONDS.toMillis(this.writerIdleTimeNanos);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public long getAllIdleTimeInMillis()
/*  68:    */   {
/*  69:206 */     return TimeUnit.NANOSECONDS.toMillis(this.allIdleTimeNanos);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  73:    */     throws Exception
/*  74:    */   {
/*  75:211 */     if ((ctx.channel().isActive()) && (ctx.channel().isRegistered())) {
/*  76:214 */       initialize(ctx);
/*  77:    */     }
/*  78:    */   }
/*  79:    */   
/*  80:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/*  81:    */     throws Exception
/*  82:    */   {
/*  83:223 */     destroy();
/*  84:    */   }
/*  85:    */   
/*  86:    */   public void channelRegistered(ChannelHandlerContext ctx)
/*  87:    */     throws Exception
/*  88:    */   {
/*  89:229 */     if (ctx.channel().isActive()) {
/*  90:230 */       initialize(ctx);
/*  91:    */     }
/*  92:232 */     super.channelRegistered(ctx);
/*  93:    */   }
/*  94:    */   
/*  95:    */   public void channelActive(ChannelHandlerContext ctx)
/*  96:    */     throws Exception
/*  97:    */   {
/*  98:240 */     initialize(ctx);
/*  99:241 */     super.channelActive(ctx);
/* 100:    */   }
/* 101:    */   
/* 102:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 103:    */     throws Exception
/* 104:    */   {
/* 105:246 */     destroy();
/* 106:247 */     super.channelInactive(ctx);
/* 107:    */   }
/* 108:    */   
/* 109:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 110:    */     throws Exception
/* 111:    */   {
/* 112:252 */     this.lastReadTime = System.nanoTime();
/* 113:253 */     this.firstReaderIdleEvent = (this.firstAllIdleEvent = 1);
/* 114:254 */     ctx.fireChannelRead(msg);
/* 115:    */   }
/* 116:    */   
/* 117:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 118:    */     throws Exception
/* 119:    */   {
/* 120:259 */     promise.addListener(new ChannelFutureListener()
/* 121:    */     {
/* 122:    */       public void operationComplete(ChannelFuture future)
/* 123:    */         throws Exception
/* 124:    */       {
/* 125:262 */         IdleStateHandler.this.lastWriteTime = System.nanoTime();
/* 126:263 */         IdleStateHandler.this.firstWriterIdleEvent = IdleStateHandler.access$102(IdleStateHandler.this, true);
/* 127:    */       }
/* 128:265 */     });
/* 129:266 */     ctx.write(msg, promise);
/* 130:    */   }
/* 131:    */   
/* 132:    */   private void initialize(ChannelHandlerContext ctx)
/* 133:    */   {
/* 134:272 */     switch (this.state)
/* 135:    */     {
/* 136:    */     case 1: 
/* 137:    */     case 2: 
/* 138:275 */       return;
/* 139:    */     }
/* 140:278 */     this.state = 1;
/* 141:    */     
/* 142:280 */     EventExecutor loop = ctx.executor();
/* 143:    */     
/* 144:282 */     this.lastReadTime = (this.lastWriteTime = System.nanoTime());
/* 145:283 */     if (this.readerIdleTimeNanos > 0L) {
/* 146:284 */       this.readerIdleTimeout = loop.schedule(new ReaderIdleTimeoutTask(ctx), this.readerIdleTimeNanos, TimeUnit.NANOSECONDS);
/* 147:    */     }
/* 148:288 */     if (this.writerIdleTimeNanos > 0L) {
/* 149:289 */       this.writerIdleTimeout = loop.schedule(new WriterIdleTimeoutTask(ctx), this.writerIdleTimeNanos, TimeUnit.NANOSECONDS);
/* 150:    */     }
/* 151:293 */     if (this.allIdleTimeNanos > 0L) {
/* 152:294 */       this.allIdleTimeout = loop.schedule(new AllIdleTimeoutTask(ctx), this.allIdleTimeNanos, TimeUnit.NANOSECONDS);
/* 153:    */     }
/* 154:    */   }
/* 155:    */   
/* 156:    */   private void destroy()
/* 157:    */   {
/* 158:301 */     this.state = 2;
/* 159:303 */     if (this.readerIdleTimeout != null)
/* 160:    */     {
/* 161:304 */       this.readerIdleTimeout.cancel(false);
/* 162:305 */       this.readerIdleTimeout = null;
/* 163:    */     }
/* 164:307 */     if (this.writerIdleTimeout != null)
/* 165:    */     {
/* 166:308 */       this.writerIdleTimeout.cancel(false);
/* 167:309 */       this.writerIdleTimeout = null;
/* 168:    */     }
/* 169:311 */     if (this.allIdleTimeout != null)
/* 170:    */     {
/* 171:312 */       this.allIdleTimeout.cancel(false);
/* 172:313 */       this.allIdleTimeout = null;
/* 173:    */     }
/* 174:    */   }
/* 175:    */   
/* 176:    */   protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt)
/* 177:    */     throws Exception
/* 178:    */   {
/* 179:322 */     ctx.fireUserEventTriggered(evt);
/* 180:    */   }
/* 181:    */   
/* 182:    */   private final class ReaderIdleTimeoutTask
/* 183:    */     implements Runnable
/* 184:    */   {
/* 185:    */     private final ChannelHandlerContext ctx;
/* 186:    */     
/* 187:    */     ReaderIdleTimeoutTask(ChannelHandlerContext ctx)
/* 188:    */     {
/* 189:330 */       this.ctx = ctx;
/* 190:    */     }
/* 191:    */     
/* 192:    */     public void run()
/* 193:    */     {
/* 194:335 */       if (!this.ctx.channel().isOpen()) {
/* 195:336 */         return;
/* 196:    */       }
/* 197:339 */       long currentTime = System.nanoTime();
/* 198:340 */       long lastReadTime = IdleStateHandler.this.lastReadTime;
/* 199:341 */       long nextDelay = IdleStateHandler.this.readerIdleTimeNanos - (currentTime - lastReadTime);
/* 200:342 */       if (nextDelay <= 0L)
/* 201:    */       {
/* 202:344 */         IdleStateHandler.this.readerIdleTimeout = this.ctx.executor().schedule(this, IdleStateHandler.this.readerIdleTimeNanos, TimeUnit.NANOSECONDS);
/* 203:    */         try
/* 204:    */         {
/* 205:    */           IdleStateEvent event;
/* 206:    */           IdleStateEvent event;
/* 207:348 */           if (IdleStateHandler.this.firstReaderIdleEvent)
/* 208:    */           {
/* 209:349 */             IdleStateHandler.this.firstReaderIdleEvent = false;
/* 210:350 */             event = IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT;
/* 211:    */           }
/* 212:    */           else
/* 213:    */           {
/* 214:352 */             event = IdleStateEvent.READER_IDLE_STATE_EVENT;
/* 215:    */           }
/* 216:354 */           IdleStateHandler.this.channelIdle(this.ctx, event);
/* 217:    */         }
/* 218:    */         catch (Throwable t)
/* 219:    */         {
/* 220:356 */           this.ctx.fireExceptionCaught(t);
/* 221:    */         }
/* 222:    */       }
/* 223:    */       else
/* 224:    */       {
/* 225:360 */         IdleStateHandler.this.readerIdleTimeout = this.ctx.executor().schedule(this, nextDelay, TimeUnit.NANOSECONDS);
/* 226:    */       }
/* 227:    */     }
/* 228:    */   }
/* 229:    */   
/* 230:    */   private final class WriterIdleTimeoutTask
/* 231:    */     implements Runnable
/* 232:    */   {
/* 233:    */     private final ChannelHandlerContext ctx;
/* 234:    */     
/* 235:    */     WriterIdleTimeoutTask(ChannelHandlerContext ctx)
/* 236:    */     {
/* 237:370 */       this.ctx = ctx;
/* 238:    */     }
/* 239:    */     
/* 240:    */     public void run()
/* 241:    */     {
/* 242:375 */       if (!this.ctx.channel().isOpen()) {
/* 243:376 */         return;
/* 244:    */       }
/* 245:379 */       long currentTime = System.nanoTime();
/* 246:380 */       long lastWriteTime = IdleStateHandler.this.lastWriteTime;
/* 247:381 */       long nextDelay = IdleStateHandler.this.writerIdleTimeNanos - (currentTime - lastWriteTime);
/* 248:382 */       if (nextDelay <= 0L)
/* 249:    */       {
/* 250:384 */         IdleStateHandler.this.writerIdleTimeout = this.ctx.executor().schedule(this, IdleStateHandler.this.writerIdleTimeNanos, TimeUnit.NANOSECONDS);
/* 251:    */         try
/* 252:    */         {
/* 253:    */           IdleStateEvent event;
/* 254:    */           IdleStateEvent event;
/* 255:388 */           if (IdleStateHandler.this.firstWriterIdleEvent)
/* 256:    */           {
/* 257:389 */             IdleStateHandler.this.firstWriterIdleEvent = false;
/* 258:390 */             event = IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT;
/* 259:    */           }
/* 260:    */           else
/* 261:    */           {
/* 262:392 */             event = IdleStateEvent.WRITER_IDLE_STATE_EVENT;
/* 263:    */           }
/* 264:394 */           IdleStateHandler.this.channelIdle(this.ctx, event);
/* 265:    */         }
/* 266:    */         catch (Throwable t)
/* 267:    */         {
/* 268:396 */           this.ctx.fireExceptionCaught(t);
/* 269:    */         }
/* 270:    */       }
/* 271:    */       else
/* 272:    */       {
/* 273:400 */         IdleStateHandler.this.writerIdleTimeout = this.ctx.executor().schedule(this, nextDelay, TimeUnit.NANOSECONDS);
/* 274:    */       }
/* 275:    */     }
/* 276:    */   }
/* 277:    */   
/* 278:    */   private final class AllIdleTimeoutTask
/* 279:    */     implements Runnable
/* 280:    */   {
/* 281:    */     private final ChannelHandlerContext ctx;
/* 282:    */     
/* 283:    */     AllIdleTimeoutTask(ChannelHandlerContext ctx)
/* 284:    */     {
/* 285:410 */       this.ctx = ctx;
/* 286:    */     }
/* 287:    */     
/* 288:    */     public void run()
/* 289:    */     {
/* 290:415 */       if (!this.ctx.channel().isOpen()) {
/* 291:416 */         return;
/* 292:    */       }
/* 293:419 */       long currentTime = System.nanoTime();
/* 294:420 */       long lastIoTime = Math.max(IdleStateHandler.this.lastReadTime, IdleStateHandler.this.lastWriteTime);
/* 295:421 */       long nextDelay = IdleStateHandler.this.allIdleTimeNanos - (currentTime - lastIoTime);
/* 296:422 */       if (nextDelay <= 0L)
/* 297:    */       {
/* 298:425 */         IdleStateHandler.this.allIdleTimeout = this.ctx.executor().schedule(this, IdleStateHandler.this.allIdleTimeNanos, TimeUnit.NANOSECONDS);
/* 299:    */         try
/* 300:    */         {
/* 301:    */           IdleStateEvent event;
/* 302:    */           IdleStateEvent event;
/* 303:429 */           if (IdleStateHandler.this.firstAllIdleEvent)
/* 304:    */           {
/* 305:430 */             IdleStateHandler.this.firstAllIdleEvent = false;
/* 306:431 */             event = IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT;
/* 307:    */           }
/* 308:    */           else
/* 309:    */           {
/* 310:433 */             event = IdleStateEvent.ALL_IDLE_STATE_EVENT;
/* 311:    */           }
/* 312:435 */           IdleStateHandler.this.channelIdle(this.ctx, event);
/* 313:    */         }
/* 314:    */         catch (Throwable t)
/* 315:    */         {
/* 316:437 */           this.ctx.fireExceptionCaught(t);
/* 317:    */         }
/* 318:    */       }
/* 319:    */       else
/* 320:    */       {
/* 321:442 */         IdleStateHandler.this.allIdleTimeout = this.ctx.executor().schedule(this, nextDelay, TimeUnit.NANOSECONDS);
/* 322:    */       }
/* 323:    */     }
/* 324:    */   }
/* 325:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.timeout.IdleStateHandler
 * JD-Core Version:    0.7.0.1
 */