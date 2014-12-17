/*   1:    */ package io.netty.handler.stream;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufHolder;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import io.netty.channel.Channel;
/*   7:    */ import io.netty.channel.ChannelDuplexHandler;
/*   8:    */ import io.netty.channel.ChannelFuture;
/*   9:    */ import io.netty.channel.ChannelFutureListener;
/*  10:    */ import io.netty.channel.ChannelHandlerContext;
/*  11:    */ import io.netty.channel.ChannelProgressivePromise;
/*  12:    */ import io.netty.channel.ChannelPromise;
/*  13:    */ import io.netty.util.ReferenceCountUtil;
/*  14:    */ import io.netty.util.concurrent.EventExecutor;
/*  15:    */ import io.netty.util.internal.logging.InternalLogger;
/*  16:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  17:    */ import java.nio.channels.ClosedChannelException;
/*  18:    */ import java.util.ArrayDeque;
/*  19:    */ import java.util.Queue;
/*  20:    */ 
/*  21:    */ public class ChunkedWriteHandler
/*  22:    */   extends ChannelDuplexHandler
/*  23:    */ {
/*  24: 72 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChunkedWriteHandler.class);
/*  25: 75 */   private final Queue<PendingWrite> queue = new ArrayDeque();
/*  26:    */   private volatile ChannelHandlerContext ctx;
/*  27:    */   private PendingWrite currentWrite;
/*  28:    */   
/*  29:    */   public ChunkedWriteHandler() {}
/*  30:    */   
/*  31:    */   @Deprecated
/*  32:    */   public ChunkedWriteHandler(int maxPendingWrites)
/*  33:    */   {
/*  34: 87 */     if (maxPendingWrites <= 0) {
/*  35: 88 */       throw new IllegalArgumentException("maxPendingWrites: " + maxPendingWrites + " (expected: > 0)");
/*  36:    */     }
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  40:    */     throws Exception
/*  41:    */   {
/*  42: 95 */     this.ctx = ctx;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public void resumeTransfer()
/*  46:    */   {
/*  47:102 */     final ChannelHandlerContext ctx = this.ctx;
/*  48:103 */     if (ctx == null) {
/*  49:104 */       return;
/*  50:    */     }
/*  51:106 */     if (ctx.executor().inEventLoop()) {
/*  52:    */       try
/*  53:    */       {
/*  54:108 */         doFlush(ctx);
/*  55:    */       }
/*  56:    */       catch (Exception e)
/*  57:    */       {
/*  58:110 */         if (logger.isWarnEnabled()) {
/*  59:111 */           logger.warn("Unexpected exception while sending chunks.", e);
/*  60:    */         }
/*  61:    */       }
/*  62:    */     } else {
/*  63:116 */       ctx.executor().execute(new Runnable()
/*  64:    */       {
/*  65:    */         public void run()
/*  66:    */         {
/*  67:    */           try
/*  68:    */           {
/*  69:121 */             ChunkedWriteHandler.this.doFlush(ctx);
/*  70:    */           }
/*  71:    */           catch (Exception e)
/*  72:    */           {
/*  73:123 */             if (ChunkedWriteHandler.logger.isWarnEnabled()) {
/*  74:124 */               ChunkedWriteHandler.logger.warn("Unexpected exception while sending chunks.", e);
/*  75:    */             }
/*  76:    */           }
/*  77:    */         }
/*  78:    */       });
/*  79:    */     }
/*  80:    */   }
/*  81:    */   
/*  82:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  83:    */     throws Exception
/*  84:    */   {
/*  85:134 */     this.queue.add(new PendingWrite(msg, promise));
/*  86:    */   }
/*  87:    */   
/*  88:    */   public void flush(ChannelHandlerContext ctx)
/*  89:    */     throws Exception
/*  90:    */   {
/*  91:139 */     Channel channel = ctx.channel();
/*  92:140 */     if ((channel.isWritable()) || (!channel.isActive())) {
/*  93:141 */       doFlush(ctx);
/*  94:    */     }
/*  95:    */   }
/*  96:    */   
/*  97:    */   public void channelInactive(ChannelHandlerContext ctx)
/*  98:    */     throws Exception
/*  99:    */   {
/* 100:147 */     doFlush(ctx);
/* 101:148 */     super.channelInactive(ctx);
/* 102:    */   }
/* 103:    */   
/* 104:    */   public void channelWritabilityChanged(ChannelHandlerContext ctx)
/* 105:    */     throws Exception
/* 106:    */   {
/* 107:153 */     if (ctx.channel().isWritable()) {
/* 108:155 */       doFlush(ctx);
/* 109:    */     }
/* 110:157 */     ctx.fireChannelWritabilityChanged();
/* 111:    */   }
/* 112:    */   
/* 113:    */   private void discard(Throwable cause)
/* 114:    */   {
/* 115:    */     for (;;)
/* 116:    */     {
/* 117:162 */       PendingWrite currentWrite = this.currentWrite;
/* 118:164 */       if (this.currentWrite == null) {
/* 119:165 */         currentWrite = (PendingWrite)this.queue.poll();
/* 120:    */       } else {
/* 121:167 */         this.currentWrite = null;
/* 122:    */       }
/* 123:170 */       if (currentWrite == null) {
/* 124:    */         break;
/* 125:    */       }
/* 126:173 */       Object message = currentWrite.msg;
/* 127:174 */       if ((message instanceof ChunkedInput))
/* 128:    */       {
/* 129:175 */         ChunkedInput<?> in = (ChunkedInput)message;
/* 130:    */         try
/* 131:    */         {
/* 132:177 */           if (!in.isEndOfInput())
/* 133:    */           {
/* 134:178 */             if (cause == null) {
/* 135:179 */               cause = new ClosedChannelException();
/* 136:    */             }
/* 137:181 */             currentWrite.fail(cause);
/* 138:    */           }
/* 139:    */           else
/* 140:    */           {
/* 141:183 */             currentWrite.success();
/* 142:    */           }
/* 143:185 */           closeInput(in);
/* 144:    */         }
/* 145:    */         catch (Exception e)
/* 146:    */         {
/* 147:187 */           currentWrite.fail(e);
/* 148:188 */           logger.warn(ChunkedInput.class.getSimpleName() + ".isEndOfInput() failed", e);
/* 149:189 */           closeInput(in);
/* 150:    */         }
/* 151:    */       }
/* 152:    */       else
/* 153:    */       {
/* 154:192 */         if (cause == null) {
/* 155:193 */           cause = new ClosedChannelException();
/* 156:    */         }
/* 157:195 */         currentWrite.fail(cause);
/* 158:    */       }
/* 159:    */     }
/* 160:    */   }
/* 161:    */   
/* 162:    */   private void doFlush(ChannelHandlerContext ctx)
/* 163:    */     throws Exception
/* 164:    */   {
/* 165:201 */     final Channel channel = ctx.channel();
/* 166:202 */     if (!channel.isActive())
/* 167:    */     {
/* 168:203 */       discard(null);
/* 169:204 */       return;
/* 170:    */     }
/* 171:206 */     while (channel.isWritable())
/* 172:    */     {
/* 173:207 */       if (this.currentWrite == null) {
/* 174:208 */         this.currentWrite = ((PendingWrite)this.queue.poll());
/* 175:    */       }
/* 176:211 */       if (this.currentWrite == null) {
/* 177:    */         break;
/* 178:    */       }
/* 179:214 */       final PendingWrite currentWrite = this.currentWrite;
/* 180:215 */       final Object pendingMessage = currentWrite.msg;
/* 181:217 */       if ((pendingMessage instanceof ChunkedInput))
/* 182:    */       {
/* 183:218 */         final ChunkedInput<?> chunks = (ChunkedInput)pendingMessage;
/* 184:    */         
/* 185:    */ 
/* 186:221 */         Object message = null;
/* 187:    */         boolean endOfInput;
/* 188:    */         boolean suspend;
/* 189:    */         try
/* 190:    */         {
/* 191:223 */           message = chunks.readChunk(ctx);
/* 192:224 */           endOfInput = chunks.isEndOfInput();
/* 193:    */           boolean suspend;
/* 194:226 */           if (message == null) {
/* 195:228 */             suspend = !endOfInput;
/* 196:    */           } else {
/* 197:230 */             suspend = false;
/* 198:    */           }
/* 199:    */         }
/* 200:    */         catch (Throwable t)
/* 201:    */         {
/* 202:233 */           this.currentWrite = null;
/* 203:235 */           if (message != null) {
/* 204:236 */             ReferenceCountUtil.release(message);
/* 205:    */           }
/* 206:239 */           currentWrite.fail(t);
/* 207:240 */           closeInput(chunks);
/* 208:241 */           break;
/* 209:    */         }
/* 210:244 */         if (suspend) {
/* 211:    */           break;
/* 212:    */         }
/* 213:251 */         if (message == null) {
/* 214:254 */           message = Unpooled.EMPTY_BUFFER;
/* 215:    */         }
/* 216:257 */         final int amount = amount(message);
/* 217:258 */         ChannelFuture f = ctx.write(message);
/* 218:259 */         if (endOfInput)
/* 219:    */         {
/* 220:260 */           this.currentWrite = null;
/* 221:    */           
/* 222:    */ 
/* 223:    */ 
/* 224:    */ 
/* 225:    */ 
/* 226:    */ 
/* 227:267 */           f.addListener(new ChannelFutureListener()
/* 228:    */           {
/* 229:    */             public void operationComplete(ChannelFuture future)
/* 230:    */               throws Exception
/* 231:    */             {
/* 232:270 */               currentWrite.progress(amount);
/* 233:271 */               currentWrite.success();
/* 234:272 */               ChunkedWriteHandler.closeInput(chunks);
/* 235:    */             }
/* 236:    */           });
/* 237:    */         }
/* 238:275 */         else if (channel.isWritable())
/* 239:    */         {
/* 240:276 */           f.addListener(new ChannelFutureListener()
/* 241:    */           {
/* 242:    */             public void operationComplete(ChannelFuture future)
/* 243:    */               throws Exception
/* 244:    */             {
/* 245:279 */               if (!future.isSuccess())
/* 246:    */               {
/* 247:280 */                 ChunkedWriteHandler.closeInput((ChunkedInput)pendingMessage);
/* 248:281 */                 currentWrite.fail(future.cause());
/* 249:    */               }
/* 250:    */               else
/* 251:    */               {
/* 252:283 */                 currentWrite.progress(amount);
/* 253:    */               }
/* 254:    */             }
/* 255:    */           });
/* 256:    */         }
/* 257:    */         else
/* 258:    */         {
/* 259:288 */           f.addListener(new ChannelFutureListener()
/* 260:    */           {
/* 261:    */             public void operationComplete(ChannelFuture future)
/* 262:    */               throws Exception
/* 263:    */             {
/* 264:291 */               if (!future.isSuccess())
/* 265:    */               {
/* 266:292 */                 ChunkedWriteHandler.closeInput((ChunkedInput)pendingMessage);
/* 267:293 */                 currentWrite.fail(future.cause());
/* 268:    */               }
/* 269:    */               else
/* 270:    */               {
/* 271:295 */                 currentWrite.progress(amount);
/* 272:296 */                 if (channel.isWritable()) {
/* 273:297 */                   ChunkedWriteHandler.this.resumeTransfer();
/* 274:    */                 }
/* 275:    */               }
/* 276:    */             }
/* 277:    */           });
/* 278:    */         }
/* 279:    */       }
/* 280:    */       else
/* 281:    */       {
/* 282:304 */         ctx.write(pendingMessage, currentWrite.promise);
/* 283:305 */         this.currentWrite = null;
/* 284:    */       }
/* 285:309 */       ctx.flush();
/* 286:311 */       if (!channel.isActive())
/* 287:    */       {
/* 288:312 */         discard(new ClosedChannelException());
/* 289:313 */         return;
/* 290:    */       }
/* 291:    */     }
/* 292:    */   }
/* 293:    */   
/* 294:    */   static void closeInput(ChunkedInput<?> chunks)
/* 295:    */   {
/* 296:    */     try
/* 297:    */     {
/* 298:320 */       chunks.close();
/* 299:    */     }
/* 300:    */     catch (Throwable t)
/* 301:    */     {
/* 302:322 */       if (logger.isWarnEnabled()) {
/* 303:323 */         logger.warn("Failed to close a chunked input.", t);
/* 304:    */       }
/* 305:    */     }
/* 306:    */   }
/* 307:    */   
/* 308:    */   private static final class PendingWrite
/* 309:    */   {
/* 310:    */     final Object msg;
/* 311:    */     final ChannelPromise promise;
/* 312:    */     private long progress;
/* 313:    */     
/* 314:    */     PendingWrite(Object msg, ChannelPromise promise)
/* 315:    */     {
/* 316:334 */       this.msg = msg;
/* 317:335 */       this.promise = promise;
/* 318:    */     }
/* 319:    */     
/* 320:    */     void fail(Throwable cause)
/* 321:    */     {
/* 322:339 */       ReferenceCountUtil.release(this.msg);
/* 323:340 */       this.promise.tryFailure(cause);
/* 324:    */     }
/* 325:    */     
/* 326:    */     void success()
/* 327:    */     {
/* 328:344 */       if (this.promise.isDone()) {
/* 329:346 */         return;
/* 330:    */       }
/* 331:349 */       if ((this.promise instanceof ChannelProgressivePromise)) {
/* 332:351 */         ((ChannelProgressivePromise)this.promise).tryProgress(this.progress, this.progress);
/* 333:    */       }
/* 334:354 */       this.promise.trySuccess();
/* 335:    */     }
/* 336:    */     
/* 337:    */     void progress(int amount)
/* 338:    */     {
/* 339:358 */       this.progress += amount;
/* 340:359 */       if ((this.promise instanceof ChannelProgressivePromise)) {
/* 341:360 */         ((ChannelProgressivePromise)this.promise).tryProgress(this.progress, -1L);
/* 342:    */       }
/* 343:    */     }
/* 344:    */   }
/* 345:    */   
/* 346:    */   private static int amount(Object msg)
/* 347:    */   {
/* 348:366 */     if ((msg instanceof ByteBuf)) {
/* 349:367 */       return ((ByteBuf)msg).readableBytes();
/* 350:    */     }
/* 351:369 */     if ((msg instanceof ByteBufHolder)) {
/* 352:370 */       return ((ByteBufHolder)msg).content().readableBytes();
/* 353:    */     }
/* 354:372 */     return 1;
/* 355:    */   }
/* 356:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.stream.ChunkedWriteHandler
 * JD-Core Version:    0.7.0.1
 */