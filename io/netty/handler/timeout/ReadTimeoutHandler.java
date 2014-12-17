/*   1:    */ package io.netty.handler.timeout;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*   6:    */ import io.netty.util.concurrent.EventExecutor;
/*   7:    */ import java.util.concurrent.ScheduledFuture;
/*   8:    */ import java.util.concurrent.TimeUnit;
/*   9:    */ 
/*  10:    */ public class ReadTimeoutHandler
/*  11:    */   extends ChannelInboundHandlerAdapter
/*  12:    */ {
/*  13: 65 */   private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
/*  14:    */   private final long timeoutNanos;
/*  15:    */   private volatile ScheduledFuture<?> timeout;
/*  16:    */   private volatile long lastReadTime;
/*  17:    */   private volatile int state;
/*  18:    */   private boolean closed;
/*  19:    */   
/*  20:    */   public ReadTimeoutHandler(int timeoutSeconds)
/*  21:    */   {
/*  22: 83 */     this(timeoutSeconds, TimeUnit.SECONDS);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public ReadTimeoutHandler(long timeout, TimeUnit unit)
/*  26:    */   {
/*  27: 95 */     if (unit == null) {
/*  28: 96 */       throw new NullPointerException("unit");
/*  29:    */     }
/*  30: 99 */     if (timeout <= 0L) {
/*  31:100 */       this.timeoutNanos = 0L;
/*  32:    */     } else {
/*  33:102 */       this.timeoutNanos = Math.max(unit.toNanos(timeout), MIN_TIMEOUT_NANOS);
/*  34:    */     }
/*  35:    */   }
/*  36:    */   
/*  37:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  38:    */     throws Exception
/*  39:    */   {
/*  40:108 */     if ((ctx.channel().isActive()) && (ctx.channel().isRegistered())) {
/*  41:111 */       initialize(ctx);
/*  42:    */     }
/*  43:    */   }
/*  44:    */   
/*  45:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/*  46:    */     throws Exception
/*  47:    */   {
/*  48:120 */     destroy();
/*  49:    */   }
/*  50:    */   
/*  51:    */   public void channelRegistered(ChannelHandlerContext ctx)
/*  52:    */     throws Exception
/*  53:    */   {
/*  54:126 */     if (ctx.channel().isActive()) {
/*  55:127 */       initialize(ctx);
/*  56:    */     }
/*  57:129 */     super.channelRegistered(ctx);
/*  58:    */   }
/*  59:    */   
/*  60:    */   public void channelActive(ChannelHandlerContext ctx)
/*  61:    */     throws Exception
/*  62:    */   {
/*  63:137 */     initialize(ctx);
/*  64:138 */     super.channelActive(ctx);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public void channelInactive(ChannelHandlerContext ctx)
/*  68:    */     throws Exception
/*  69:    */   {
/*  70:143 */     destroy();
/*  71:144 */     super.channelInactive(ctx);
/*  72:    */   }
/*  73:    */   
/*  74:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  75:    */     throws Exception
/*  76:    */   {
/*  77:149 */     this.lastReadTime = System.nanoTime();
/*  78:150 */     ctx.fireChannelRead(msg);
/*  79:    */   }
/*  80:    */   
/*  81:    */   private void initialize(ChannelHandlerContext ctx)
/*  82:    */   {
/*  83:156 */     switch (this.state)
/*  84:    */     {
/*  85:    */     case 1: 
/*  86:    */     case 2: 
/*  87:159 */       return;
/*  88:    */     }
/*  89:162 */     this.state = 1;
/*  90:    */     
/*  91:164 */     this.lastReadTime = System.nanoTime();
/*  92:165 */     if (this.timeoutNanos > 0L) {
/*  93:166 */       this.timeout = ctx.executor().schedule(new ReadTimeoutTask(ctx), this.timeoutNanos, TimeUnit.NANOSECONDS);
/*  94:    */     }
/*  95:    */   }
/*  96:    */   
/*  97:    */   private void destroy()
/*  98:    */   {
/*  99:173 */     this.state = 2;
/* 100:175 */     if (this.timeout != null)
/* 101:    */     {
/* 102:176 */       this.timeout.cancel(false);
/* 103:177 */       this.timeout = null;
/* 104:    */     }
/* 105:    */   }
/* 106:    */   
/* 107:    */   protected void readTimedOut(ChannelHandlerContext ctx)
/* 108:    */     throws Exception
/* 109:    */   {
/* 110:185 */     if (!this.closed)
/* 111:    */     {
/* 112:186 */       ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
/* 113:187 */       ctx.close();
/* 114:188 */       this.closed = true;
/* 115:    */     }
/* 116:    */   }
/* 117:    */   
/* 118:    */   private final class ReadTimeoutTask
/* 119:    */     implements Runnable
/* 120:    */   {
/* 121:    */     private final ChannelHandlerContext ctx;
/* 122:    */     
/* 123:    */     ReadTimeoutTask(ChannelHandlerContext ctx)
/* 124:    */     {
/* 125:197 */       this.ctx = ctx;
/* 126:    */     }
/* 127:    */     
/* 128:    */     public void run()
/* 129:    */     {
/* 130:202 */       if (!this.ctx.channel().isOpen()) {
/* 131:203 */         return;
/* 132:    */       }
/* 133:206 */       long currentTime = System.nanoTime();
/* 134:207 */       long nextDelay = ReadTimeoutHandler.this.timeoutNanos - (currentTime - ReadTimeoutHandler.this.lastReadTime);
/* 135:208 */       if (nextDelay <= 0L)
/* 136:    */       {
/* 137:210 */         ReadTimeoutHandler.this.timeout = this.ctx.executor().schedule(this, ReadTimeoutHandler.this.timeoutNanos, TimeUnit.NANOSECONDS);
/* 138:    */         try
/* 139:    */         {
/* 140:212 */           ReadTimeoutHandler.this.readTimedOut(this.ctx);
/* 141:    */         }
/* 142:    */         catch (Throwable t)
/* 143:    */         {
/* 144:214 */           this.ctx.fireExceptionCaught(t);
/* 145:    */         }
/* 146:    */       }
/* 147:    */       else
/* 148:    */       {
/* 149:218 */         ReadTimeoutHandler.this.timeout = this.ctx.executor().schedule(this, nextDelay, TimeUnit.NANOSECONDS);
/* 150:    */       }
/* 151:    */     }
/* 152:    */   }
/* 153:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.timeout.ReadTimeoutHandler
 * JD-Core Version:    0.7.0.1
 */