/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import java.util.Queue;
/*  4:   */ import java.util.Set;
/*  5:   */ 
/*  6:   */ public class ThreadPerChannelEventLoop
/*  7:   */   extends SingleThreadEventLoop
/*  8:   */ {
/*  9:   */   private final ThreadPerChannelEventLoopGroup parent;
/* 10:   */   private Channel ch;
/* 11:   */   
/* 12:   */   public ThreadPerChannelEventLoop(ThreadPerChannelEventLoopGroup parent)
/* 13:   */   {
/* 14:29 */     super(parent, parent.threadFactory, true);
/* 15:30 */     this.parent = parent;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public ChannelFuture register(Channel channel, ChannelPromise promise)
/* 19:   */   {
/* 20:35 */     super.register(channel, promise).addListener(new ChannelFutureListener()
/* 21:   */     {
/* 22:   */       public void operationComplete(ChannelFuture future)
/* 23:   */         throws Exception
/* 24:   */       {
/* 25:39 */         if (future.isSuccess()) {
/* 26:40 */           ThreadPerChannelEventLoop.this.ch = future.channel();
/* 27:   */         } else {
/* 28:42 */           ThreadPerChannelEventLoop.this.deregister();
/* 29:   */         }
/* 30:   */       }
/* 31:   */     });
/* 32:   */   }
/* 33:   */   
/* 34:   */   protected void run()
/* 35:   */   {
/* 36:   */     for (;;)
/* 37:   */     {
/* 38:51 */       Runnable task = takeTask();
/* 39:52 */       if (task != null)
/* 40:   */       {
/* 41:53 */         task.run();
/* 42:54 */         updateLastExecutionTime();
/* 43:   */       }
/* 44:57 */       Channel ch = this.ch;
/* 45:58 */       if (isShuttingDown())
/* 46:   */       {
/* 47:59 */         if (ch != null) {
/* 48:60 */           ch.unsafe().close(ch.unsafe().voidPromise());
/* 49:   */         }
/* 50:62 */         if (confirmShutdown()) {
/* 51:   */           break;
/* 52:   */         }
/* 53:   */       }
/* 54:66 */       else if (ch != null)
/* 55:   */       {
/* 56:68 */         if (!ch.isRegistered())
/* 57:   */         {
/* 58:69 */           runAllTasks();
/* 59:70 */           deregister();
/* 60:   */         }
/* 61:   */       }
/* 62:   */     }
/* 63:   */   }
/* 64:   */   
/* 65:   */   protected void deregister()
/* 66:   */   {
/* 67:78 */     this.ch = null;
/* 68:79 */     this.parent.activeChildren.remove(this);
/* 69:80 */     this.parent.idleChildren.add(this);
/* 70:   */   }
/* 71:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.ThreadPerChannelEventLoop
 * JD-Core Version:    0.7.0.1
 */