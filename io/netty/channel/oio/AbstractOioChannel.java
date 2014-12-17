/*   1:    */ package io.netty.channel.oio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.AbstractChannel;
/*   4:    */ import io.netty.channel.AbstractChannel.AbstractUnsafe;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.ChannelConfig;
/*   7:    */ import io.netty.channel.ChannelPipeline;
/*   8:    */ import io.netty.channel.ChannelPromise;
/*   9:    */ import io.netty.channel.EventLoop;
/*  10:    */ import io.netty.channel.ThreadPerChannelEventLoop;
/*  11:    */ import java.net.ConnectException;
/*  12:    */ import java.net.SocketAddress;
/*  13:    */ 
/*  14:    */ public abstract class AbstractOioChannel
/*  15:    */   extends AbstractChannel
/*  16:    */ {
/*  17:    */   protected static final int SO_TIMEOUT = 1000;
/*  18:    */   private volatile boolean readPending;
/*  19: 36 */   private final Runnable readTask = new Runnable()
/*  20:    */   {
/*  21:    */     public void run()
/*  22:    */     {
/*  23: 39 */       if ((!AbstractOioChannel.this.isReadPending()) && (!AbstractOioChannel.this.config().isAutoRead())) {
/*  24: 41 */         return;
/*  25:    */       }
/*  26: 44 */       AbstractOioChannel.this.setReadPending(false);
/*  27: 45 */       AbstractOioChannel.this.doRead();
/*  28:    */     }
/*  29:    */   };
/*  30:    */   
/*  31:    */   protected AbstractOioChannel(Channel parent)
/*  32:    */   {
/*  33: 53 */     super(parent);
/*  34:    */   }
/*  35:    */   
/*  36:    */   protected AbstractChannel.AbstractUnsafe newUnsafe()
/*  37:    */   {
/*  38: 58 */     return new DefaultOioUnsafe(null);
/*  39:    */   }
/*  40:    */   
/*  41:    */   private final class DefaultOioUnsafe
/*  42:    */     extends AbstractChannel.AbstractUnsafe
/*  43:    */   {
/*  44:    */     private DefaultOioUnsafe()
/*  45:    */     {
/*  46: 61 */       super();
/*  47:    */     }
/*  48:    */     
/*  49:    */     public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/*  50:    */     {
/*  51: 66 */       if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/*  52: 67 */         return;
/*  53:    */       }
/*  54:    */       try
/*  55:    */       {
/*  56: 71 */         boolean wasActive = AbstractOioChannel.this.isActive();
/*  57: 72 */         AbstractOioChannel.this.doConnect(remoteAddress, localAddress);
/*  58: 73 */         safeSetSuccess(promise);
/*  59: 74 */         if ((!wasActive) && (AbstractOioChannel.this.isActive())) {
/*  60: 75 */           AbstractOioChannel.this.pipeline().fireChannelActive();
/*  61:    */         }
/*  62:    */       }
/*  63:    */       catch (Throwable t)
/*  64:    */       {
/*  65: 78 */         if ((t instanceof ConnectException))
/*  66:    */         {
/*  67: 79 */           Throwable newT = new ConnectException(t.getMessage() + ": " + remoteAddress);
/*  68: 80 */           newT.setStackTrace(t.getStackTrace());
/*  69: 81 */           t = newT;
/*  70:    */         }
/*  71: 83 */         safeSetFailure(promise, t);
/*  72: 84 */         closeIfClosed();
/*  73:    */       }
/*  74:    */     }
/*  75:    */   }
/*  76:    */   
/*  77:    */   protected boolean isCompatible(EventLoop loop)
/*  78:    */   {
/*  79: 91 */     return loop instanceof ThreadPerChannelEventLoop;
/*  80:    */   }
/*  81:    */   
/*  82:    */   protected abstract void doConnect(SocketAddress paramSocketAddress1, SocketAddress paramSocketAddress2)
/*  83:    */     throws Exception;
/*  84:    */   
/*  85:    */   protected void doBeginRead()
/*  86:    */     throws Exception
/*  87:    */   {
/*  88:102 */     if (isReadPending()) {
/*  89:103 */       return;
/*  90:    */     }
/*  91:106 */     setReadPending(true);
/*  92:107 */     eventLoop().execute(this.readTask);
/*  93:    */   }
/*  94:    */   
/*  95:    */   protected abstract void doRead();
/*  96:    */   
/*  97:    */   protected boolean isReadPending()
/*  98:    */   {
/*  99:113 */     return this.readPending;
/* 100:    */   }
/* 101:    */   
/* 102:    */   protected void setReadPending(boolean readPending)
/* 103:    */   {
/* 104:117 */     this.readPending = readPending;
/* 105:    */   }
/* 106:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.oio.AbstractOioChannel
 * JD-Core Version:    0.7.0.1
 */