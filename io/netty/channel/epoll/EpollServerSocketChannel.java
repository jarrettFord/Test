/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   4:    */ import io.netty.channel.ChannelPipeline;
/*   5:    */ import io.netty.channel.ChannelPromise;
/*   6:    */ import io.netty.channel.EventLoop;
/*   7:    */ import io.netty.channel.socket.ServerSocketChannel;
/*   8:    */ import java.net.InetSocketAddress;
/*   9:    */ import java.net.SocketAddress;
/*  10:    */ 
/*  11:    */ public final class EpollServerSocketChannel
/*  12:    */   extends AbstractEpollChannel
/*  13:    */   implements ServerSocketChannel
/*  14:    */ {
/*  15:    */   private final EpollServerSocketChannelConfig config;
/*  16:    */   private volatile InetSocketAddress local;
/*  17:    */   
/*  18:    */   public EpollServerSocketChannel()
/*  19:    */   {
/*  20: 37 */     super(Native.socketStreamFd(), 4);
/*  21: 38 */     this.config = new EpollServerSocketChannelConfig(this);
/*  22:    */   }
/*  23:    */   
/*  24:    */   protected boolean isCompatible(EventLoop loop)
/*  25:    */   {
/*  26: 43 */     return loop instanceof EpollEventLoop;
/*  27:    */   }
/*  28:    */   
/*  29:    */   protected void doBind(SocketAddress localAddress)
/*  30:    */     throws Exception
/*  31:    */   {
/*  32: 48 */     InetSocketAddress addr = (InetSocketAddress)localAddress;
/*  33: 49 */     checkResolvable(addr);
/*  34: 50 */     Native.bind(this.fd, addr.getAddress(), addr.getPort());
/*  35: 51 */     this.local = Native.localAddress(this.fd);
/*  36: 52 */     Native.listen(this.fd, this.config.getBacklog());
/*  37: 53 */     this.active = true;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public EpollServerSocketChannelConfig config()
/*  41:    */   {
/*  42: 58 */     return this.config;
/*  43:    */   }
/*  44:    */   
/*  45:    */   protected InetSocketAddress localAddress0()
/*  46:    */   {
/*  47: 63 */     return this.local;
/*  48:    */   }
/*  49:    */   
/*  50:    */   protected InetSocketAddress remoteAddress0()
/*  51:    */   {
/*  52: 68 */     return null;
/*  53:    */   }
/*  54:    */   
/*  55:    */   protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe()
/*  56:    */   {
/*  57: 73 */     return new EpollServerSocketUnsafe();
/*  58:    */   }
/*  59:    */   
/*  60:    */   protected void doWrite(ChannelOutboundBuffer in)
/*  61:    */   {
/*  62: 78 */     throw new UnsupportedOperationException();
/*  63:    */   }
/*  64:    */   
/*  65:    */   final class EpollServerSocketUnsafe
/*  66:    */     extends AbstractEpollChannel.AbstractEpollUnsafe
/*  67:    */   {
/*  68:    */     EpollServerSocketUnsafe()
/*  69:    */     {
/*  70: 81 */       super();
/*  71:    */     }
/*  72:    */     
/*  73:    */     public void connect(SocketAddress socketAddress, SocketAddress socketAddress2, ChannelPromise channelPromise)
/*  74:    */     {
/*  75: 86 */       channelPromise.setFailure(new UnsupportedOperationException());
/*  76:    */     }
/*  77:    */     
/*  78:    */     void epollInReady()
/*  79:    */     {
/*  80: 91 */       assert (EpollServerSocketChannel.this.eventLoop().inEventLoop());
/*  81: 92 */       ChannelPipeline pipeline = EpollServerSocketChannel.this.pipeline();
/*  82: 93 */       Throwable exception = null;
/*  83:    */       try
/*  84:    */       {
/*  85:    */         try
/*  86:    */         {
/*  87:    */           for (;;)
/*  88:    */           {
/*  89: 97 */             int socketFd = Native.accept(EpollServerSocketChannel.this.fd);
/*  90: 98 */             if (socketFd == -1) {
/*  91:    */               break;
/*  92:    */             }
/*  93:    */             try
/*  94:    */             {
/*  95:103 */               this.readPending = false;
/*  96:104 */               pipeline.fireChannelRead(new EpollSocketChannel(EpollServerSocketChannel.this, socketFd));
/*  97:    */             }
/*  98:    */             catch (Throwable t)
/*  99:    */             {
/* 100:107 */               pipeline.fireChannelReadComplete();
/* 101:108 */               pipeline.fireExceptionCaught(t);
/* 102:    */             }
/* 103:    */           }
/* 104:    */         }
/* 105:    */         catch (Throwable t)
/* 106:    */         {
/* 107:112 */           exception = t;
/* 108:    */         }
/* 109:114 */         pipeline.fireChannelReadComplete();
/* 110:116 */         if (exception != null) {
/* 111:117 */           pipeline.fireExceptionCaught(exception);
/* 112:    */         }
/* 113:    */       }
/* 114:    */       finally
/* 115:    */       {
/* 116:126 */         if ((!EpollServerSocketChannel.this.config.isAutoRead()) && (!this.readPending)) {
/* 117:127 */           clearEpollIn0();
/* 118:    */         }
/* 119:    */       }
/* 120:    */     }
/* 121:    */   }
/* 122:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.epoll.EpollServerSocketChannel
 * JD-Core Version:    0.7.0.1
 */