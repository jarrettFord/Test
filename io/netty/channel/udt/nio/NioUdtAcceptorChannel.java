/*   1:    */ package io.netty.channel.udt.nio;
/*   2:    */ 
/*   3:    */ import com.barchart.udt.TypeUDT;
/*   4:    */ import com.barchart.udt.nio.NioServerSocketUDT;
/*   5:    */ import com.barchart.udt.nio.ServerSocketChannelUDT;
/*   6:    */ import io.netty.channel.ChannelException;
/*   7:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   8:    */ import io.netty.channel.nio.AbstractNioMessageChannel;
/*   9:    */ import io.netty.channel.udt.DefaultUdtServerChannelConfig;
/*  10:    */ import io.netty.channel.udt.UdtServerChannel;
/*  11:    */ import io.netty.channel.udt.UdtServerChannelConfig;
/*  12:    */ import io.netty.util.internal.logging.InternalLogger;
/*  13:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  14:    */ import java.net.InetSocketAddress;
/*  15:    */ import java.net.SocketAddress;
/*  16:    */ 
/*  17:    */ public abstract class NioUdtAcceptorChannel
/*  18:    */   extends AbstractNioMessageChannel
/*  19:    */   implements UdtServerChannel
/*  20:    */ {
/*  21: 39 */   protected static final InternalLogger logger = InternalLoggerFactory.getInstance(NioUdtAcceptorChannel.class);
/*  22:    */   private final UdtServerChannelConfig config;
/*  23:    */   
/*  24:    */   protected NioUdtAcceptorChannel(ServerSocketChannelUDT channelUDT)
/*  25:    */   {
/*  26: 45 */     super(null, channelUDT, 16);
/*  27:    */     try
/*  28:    */     {
/*  29: 47 */       channelUDT.configureBlocking(false);
/*  30: 48 */       this.config = new DefaultUdtServerChannelConfig(this, channelUDT, true);
/*  31:    */     }
/*  32:    */     catch (Exception e)
/*  33:    */     {
/*  34:    */       try
/*  35:    */       {
/*  36: 51 */         channelUDT.close();
/*  37:    */       }
/*  38:    */       catch (Exception e2)
/*  39:    */       {
/*  40: 53 */         if (logger.isWarnEnabled()) {
/*  41: 54 */           logger.warn("Failed to close channel.", e2);
/*  42:    */         }
/*  43:    */       }
/*  44: 57 */       throw new ChannelException("Failed to configure channel.", e);
/*  45:    */     }
/*  46:    */   }
/*  47:    */   
/*  48:    */   protected NioUdtAcceptorChannel(TypeUDT type)
/*  49:    */   {
/*  50: 62 */     this(NioUdtProvider.newAcceptorChannelUDT(type));
/*  51:    */   }
/*  52:    */   
/*  53:    */   public UdtServerChannelConfig config()
/*  54:    */   {
/*  55: 67 */     return this.config;
/*  56:    */   }
/*  57:    */   
/*  58:    */   protected void doBind(SocketAddress localAddress)
/*  59:    */     throws Exception
/*  60:    */   {
/*  61: 72 */     javaChannel().socket().bind(localAddress, this.config.getBacklog());
/*  62:    */   }
/*  63:    */   
/*  64:    */   protected void doClose()
/*  65:    */     throws Exception
/*  66:    */   {
/*  67: 77 */     javaChannel().close();
/*  68:    */   }
/*  69:    */   
/*  70:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/*  71:    */     throws Exception
/*  72:    */   {
/*  73: 83 */     throw new UnsupportedOperationException();
/*  74:    */   }
/*  75:    */   
/*  76:    */   protected void doDisconnect()
/*  77:    */     throws Exception
/*  78:    */   {
/*  79: 88 */     throw new UnsupportedOperationException();
/*  80:    */   }
/*  81:    */   
/*  82:    */   protected void doFinishConnect()
/*  83:    */     throws Exception
/*  84:    */   {
/*  85: 93 */     throw new UnsupportedOperationException();
/*  86:    */   }
/*  87:    */   
/*  88:    */   protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in)
/*  89:    */     throws Exception
/*  90:    */   {
/*  91: 98 */     throw new UnsupportedOperationException();
/*  92:    */   }
/*  93:    */   
/*  94:    */   public boolean isActive()
/*  95:    */   {
/*  96:103 */     return javaChannel().socket().isBound();
/*  97:    */   }
/*  98:    */   
/*  99:    */   protected ServerSocketChannelUDT javaChannel()
/* 100:    */   {
/* 101:108 */     return (ServerSocketChannelUDT)super.javaChannel();
/* 102:    */   }
/* 103:    */   
/* 104:    */   protected SocketAddress localAddress0()
/* 105:    */   {
/* 106:113 */     return javaChannel().socket().getLocalSocketAddress();
/* 107:    */   }
/* 108:    */   
/* 109:    */   public InetSocketAddress localAddress()
/* 110:    */   {
/* 111:117 */     return (InetSocketAddress)super.localAddress();
/* 112:    */   }
/* 113:    */   
/* 114:    */   public InetSocketAddress remoteAddress()
/* 115:    */   {
/* 116:122 */     return null;
/* 117:    */   }
/* 118:    */   
/* 119:    */   protected SocketAddress remoteAddress0()
/* 120:    */   {
/* 121:127 */     return null;
/* 122:    */   }
/* 123:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.udt.nio.NioUdtAcceptorChannel
 * JD-Core Version:    0.7.0.1
 */