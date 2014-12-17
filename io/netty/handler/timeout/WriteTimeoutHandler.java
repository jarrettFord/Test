/*   1:    */ package io.netty.handler.timeout;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelFuture;
/*   4:    */ import io.netty.channel.ChannelFutureListener;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.channel.ChannelOutboundHandlerAdapter;
/*   7:    */ import io.netty.channel.ChannelPromise;
/*   8:    */ import io.netty.util.concurrent.EventExecutor;
/*   9:    */ import java.util.concurrent.ScheduledFuture;
/*  10:    */ import java.util.concurrent.TimeUnit;
/*  11:    */ 
/*  12:    */ public class WriteTimeoutHandler
/*  13:    */   extends ChannelOutboundHandlerAdapter
/*  14:    */ {
/*  15: 68 */   private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
/*  16:    */   private final long timeoutNanos;
/*  17:    */   private boolean closed;
/*  18:    */   
/*  19:    */   public WriteTimeoutHandler(int timeoutSeconds)
/*  20:    */   {
/*  21: 81 */     this(timeoutSeconds, TimeUnit.SECONDS);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public WriteTimeoutHandler(long timeout, TimeUnit unit)
/*  25:    */   {
/*  26: 93 */     if (unit == null) {
/*  27: 94 */       throw new NullPointerException("unit");
/*  28:    */     }
/*  29: 97 */     if (timeout <= 0L) {
/*  30: 98 */       this.timeoutNanos = 0L;
/*  31:    */     } else {
/*  32:100 */       this.timeoutNanos = Math.max(unit.toNanos(timeout), MIN_TIMEOUT_NANOS);
/*  33:    */     }
/*  34:    */   }
/*  35:    */   
/*  36:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  37:    */     throws Exception
/*  38:    */   {
/*  39:106 */     scheduleTimeout(ctx, promise);
/*  40:107 */     ctx.write(msg, promise);
/*  41:    */   }
/*  42:    */   
/*  43:    */   private void scheduleTimeout(final ChannelHandlerContext ctx, final ChannelPromise future)
/*  44:    */   {
/*  45:111 */     if (this.timeoutNanos > 0L)
/*  46:    */     {
/*  47:113 */       final ScheduledFuture<?> sf = ctx.executor().schedule(new Runnable()
/*  48:    */       {
/*  49:    */         public void run()
/*  50:    */         {
/*  51:119 */           if (!future.isDone()) {
/*  52:    */             try
/*  53:    */             {
/*  54:121 */               WriteTimeoutHandler.this.writeTimedOut(ctx);
/*  55:    */             }
/*  56:    */             catch (Throwable t)
/*  57:    */             {
/*  58:123 */               ctx.fireExceptionCaught(t);
/*  59:    */             }
/*  60:    */           }
/*  61:    */         }
/*  62:123 */       }, this.timeoutNanos, TimeUnit.NANOSECONDS);
/*  63:    */       
/*  64:    */ 
/*  65:    */ 
/*  66:    */ 
/*  67:    */ 
/*  68:    */ 
/*  69:130 */       future.addListener(new ChannelFutureListener()
/*  70:    */       {
/*  71:    */         public void operationComplete(ChannelFuture future)
/*  72:    */           throws Exception
/*  73:    */         {
/*  74:133 */           sf.cancel(false);
/*  75:    */         }
/*  76:    */       });
/*  77:    */     }
/*  78:    */   }
/*  79:    */   
/*  80:    */   protected void writeTimedOut(ChannelHandlerContext ctx)
/*  81:    */     throws Exception
/*  82:    */   {
/*  83:143 */     if (!this.closed)
/*  84:    */     {
/*  85:144 */       ctx.fireExceptionCaught(WriteTimeoutException.INSTANCE);
/*  86:145 */       ctx.close();
/*  87:146 */       this.closed = true;
/*  88:    */     }
/*  89:    */   }
/*  90:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.timeout.WriteTimeoutHandler
 * JD-Core Version:    0.7.0.1
 */