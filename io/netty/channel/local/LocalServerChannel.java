/*   1:    */ package io.netty.channel.local;
/*   2:    */ 
/*   3:    */ import io.netty.channel.AbstractServerChannel;
/*   4:    */ import io.netty.channel.Channel.Unsafe;
/*   5:    */ import io.netty.channel.ChannelConfig;
/*   6:    */ import io.netty.channel.ChannelPipeline;
/*   7:    */ import io.netty.channel.DefaultChannelConfig;
/*   8:    */ import io.netty.channel.EventLoop;
/*   9:    */ import io.netty.channel.SingleThreadEventLoop;
/*  10:    */ import io.netty.util.concurrent.SingleThreadEventExecutor;
/*  11:    */ import java.net.SocketAddress;
/*  12:    */ import java.util.ArrayDeque;
/*  13:    */ import java.util.Queue;
/*  14:    */ 
/*  15:    */ public class LocalServerChannel
/*  16:    */   extends AbstractServerChannel
/*  17:    */ {
/*  18: 36 */   private final ChannelConfig config = new DefaultChannelConfig(this);
/*  19: 37 */   private final Queue<Object> inboundBuffer = new ArrayDeque();
/*  20: 38 */   private final Runnable shutdownHook = new Runnable()
/*  21:    */   {
/*  22:    */     public void run()
/*  23:    */     {
/*  24: 41 */       LocalServerChannel.this.unsafe().close(LocalServerChannel.this.unsafe().voidPromise());
/*  25:    */     }
/*  26:    */   };
/*  27:    */   private volatile int state;
/*  28:    */   private volatile LocalAddress localAddress;
/*  29:    */   private volatile boolean acceptInProgress;
/*  30:    */   
/*  31:    */   public ChannelConfig config()
/*  32:    */   {
/*  33: 51 */     return this.config;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public LocalAddress localAddress()
/*  37:    */   {
/*  38: 56 */     return (LocalAddress)super.localAddress();
/*  39:    */   }
/*  40:    */   
/*  41:    */   public LocalAddress remoteAddress()
/*  42:    */   {
/*  43: 61 */     return (LocalAddress)super.remoteAddress();
/*  44:    */   }
/*  45:    */   
/*  46:    */   public boolean isOpen()
/*  47:    */   {
/*  48: 66 */     return this.state < 2;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public boolean isActive()
/*  52:    */   {
/*  53: 71 */     return this.state == 1;
/*  54:    */   }
/*  55:    */   
/*  56:    */   protected boolean isCompatible(EventLoop loop)
/*  57:    */   {
/*  58: 76 */     return loop instanceof SingleThreadEventLoop;
/*  59:    */   }
/*  60:    */   
/*  61:    */   protected SocketAddress localAddress0()
/*  62:    */   {
/*  63: 81 */     return this.localAddress;
/*  64:    */   }
/*  65:    */   
/*  66:    */   protected void doRegister()
/*  67:    */     throws Exception
/*  68:    */   {
/*  69: 86 */     ((SingleThreadEventExecutor)eventLoop()).addShutdownHook(this.shutdownHook);
/*  70:    */   }
/*  71:    */   
/*  72:    */   protected void doBind(SocketAddress localAddress)
/*  73:    */     throws Exception
/*  74:    */   {
/*  75: 91 */     this.localAddress = LocalChannelRegistry.register(this, this.localAddress, localAddress);
/*  76: 92 */     this.state = 1;
/*  77:    */   }
/*  78:    */   
/*  79:    */   protected void doClose()
/*  80:    */     throws Exception
/*  81:    */   {
/*  82: 97 */     if (this.state <= 1)
/*  83:    */     {
/*  84: 99 */       if (this.localAddress != null)
/*  85:    */       {
/*  86:100 */         LocalChannelRegistry.unregister(this.localAddress);
/*  87:101 */         this.localAddress = null;
/*  88:    */       }
/*  89:103 */       this.state = 2;
/*  90:    */     }
/*  91:    */   }
/*  92:    */   
/*  93:    */   protected void doDeregister()
/*  94:    */     throws Exception
/*  95:    */   {
/*  96:109 */     ((SingleThreadEventExecutor)eventLoop()).removeShutdownHook(this.shutdownHook);
/*  97:    */   }
/*  98:    */   
/*  99:    */   protected void doBeginRead()
/* 100:    */     throws Exception
/* 101:    */   {
/* 102:114 */     if (this.acceptInProgress) {
/* 103:115 */       return;
/* 104:    */     }
/* 105:118 */     Queue<Object> inboundBuffer = this.inboundBuffer;
/* 106:119 */     if (inboundBuffer.isEmpty())
/* 107:    */     {
/* 108:120 */       this.acceptInProgress = true;
/* 109:121 */       return;
/* 110:    */     }
/* 111:124 */     ChannelPipeline pipeline = pipeline();
/* 112:    */     for (;;)
/* 113:    */     {
/* 114:126 */       Object m = inboundBuffer.poll();
/* 115:127 */       if (m == null) {
/* 116:    */         break;
/* 117:    */       }
/* 118:130 */       pipeline.fireChannelRead(m);
/* 119:    */     }
/* 120:132 */     pipeline.fireChannelReadComplete();
/* 121:    */   }
/* 122:    */   
/* 123:    */   LocalChannel serve(LocalChannel peer)
/* 124:    */   {
/* 125:136 */     final LocalChannel child = new LocalChannel(this, peer);
/* 126:137 */     if (eventLoop().inEventLoop()) {
/* 127:138 */       serve0(child);
/* 128:    */     } else {
/* 129:140 */       eventLoop().execute(new Runnable()
/* 130:    */       {
/* 131:    */         public void run()
/* 132:    */         {
/* 133:143 */           LocalServerChannel.this.serve0(child);
/* 134:    */         }
/* 135:    */       });
/* 136:    */     }
/* 137:147 */     return child;
/* 138:    */   }
/* 139:    */   
/* 140:    */   private void serve0(LocalChannel child)
/* 141:    */   {
/* 142:151 */     this.inboundBuffer.add(child);
/* 143:152 */     if (this.acceptInProgress)
/* 144:    */     {
/* 145:153 */       this.acceptInProgress = false;
/* 146:154 */       ChannelPipeline pipeline = pipeline();
/* 147:    */       for (;;)
/* 148:    */       {
/* 149:156 */         Object m = this.inboundBuffer.poll();
/* 150:157 */         if (m == null) {
/* 151:    */           break;
/* 152:    */         }
/* 153:160 */         pipeline.fireChannelRead(m);
/* 154:    */       }
/* 155:162 */       pipeline.fireChannelReadComplete();
/* 156:    */     }
/* 157:    */   }
/* 158:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.local.LocalServerChannel
 * JD-Core Version:    0.7.0.1
 */