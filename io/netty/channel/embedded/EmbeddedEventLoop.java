/*   1:    */ package io.netty.channel.embedded;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.Channel.Unsafe;
/*   5:    */ import io.netty.channel.ChannelFuture;
/*   6:    */ import io.netty.channel.ChannelPromise;
/*   7:    */ import io.netty.channel.DefaultChannelPromise;
/*   8:    */ import io.netty.channel.EventLoop;
/*   9:    */ import io.netty.channel.EventLoopGroup;
/*  10:    */ import io.netty.util.concurrent.AbstractEventExecutor;
/*  11:    */ import io.netty.util.concurrent.Future;
/*  12:    */ import java.util.ArrayDeque;
/*  13:    */ import java.util.Queue;
/*  14:    */ import java.util.concurrent.TimeUnit;
/*  15:    */ 
/*  16:    */ final class EmbeddedEventLoop
/*  17:    */   extends AbstractEventExecutor
/*  18:    */   implements EventLoop
/*  19:    */ {
/*  20: 33 */   private final Queue<Runnable> tasks = new ArrayDeque(2);
/*  21:    */   
/*  22:    */   public void execute(Runnable command)
/*  23:    */   {
/*  24: 37 */     if (command == null) {
/*  25: 38 */       throw new NullPointerException("command");
/*  26:    */     }
/*  27: 40 */     this.tasks.add(command);
/*  28:    */   }
/*  29:    */   
/*  30:    */   void runTasks()
/*  31:    */   {
/*  32:    */     for (;;)
/*  33:    */     {
/*  34: 45 */       Runnable task = (Runnable)this.tasks.poll();
/*  35: 46 */       if (task == null) {
/*  36:    */         break;
/*  37:    */       }
/*  38: 50 */       task.run();
/*  39:    */     }
/*  40:    */   }
/*  41:    */   
/*  42:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/*  43:    */   {
/*  44: 56 */     throw new UnsupportedOperationException();
/*  45:    */   }
/*  46:    */   
/*  47:    */   public Future<?> terminationFuture()
/*  48:    */   {
/*  49: 61 */     throw new UnsupportedOperationException();
/*  50:    */   }
/*  51:    */   
/*  52:    */   @Deprecated
/*  53:    */   public void shutdown()
/*  54:    */   {
/*  55: 67 */     throw new UnsupportedOperationException();
/*  56:    */   }
/*  57:    */   
/*  58:    */   public boolean isShuttingDown()
/*  59:    */   {
/*  60: 72 */     return false;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public boolean isShutdown()
/*  64:    */   {
/*  65: 77 */     return false;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public boolean isTerminated()
/*  69:    */   {
/*  70: 82 */     return false;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean awaitTermination(long timeout, TimeUnit unit)
/*  74:    */     throws InterruptedException
/*  75:    */   {
/*  76: 88 */     Thread.sleep(unit.toMillis(timeout));
/*  77: 89 */     return false;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public ChannelFuture register(Channel channel)
/*  81:    */   {
/*  82: 94 */     return register(channel, new DefaultChannelPromise(channel, this));
/*  83:    */   }
/*  84:    */   
/*  85:    */   public ChannelFuture register(Channel channel, ChannelPromise promise)
/*  86:    */   {
/*  87: 99 */     channel.unsafe().register(this, promise);
/*  88:100 */     return promise;
/*  89:    */   }
/*  90:    */   
/*  91:    */   public boolean inEventLoop()
/*  92:    */   {
/*  93:105 */     return true;
/*  94:    */   }
/*  95:    */   
/*  96:    */   public boolean inEventLoop(Thread thread)
/*  97:    */   {
/*  98:110 */     return true;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public EventLoop next()
/* 102:    */   {
/* 103:115 */     return this;
/* 104:    */   }
/* 105:    */   
/* 106:    */   public EventLoopGroup parent()
/* 107:    */   {
/* 108:120 */     return this;
/* 109:    */   }
/* 110:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.embedded.EmbeddedEventLoop
 * JD-Core Version:    0.7.0.1
 */