/*   1:    */ package io.netty.channel.socket.nio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelException;
/*   4:    */ import io.netty.channel.ChannelMetadata;
/*   5:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   6:    */ import io.netty.channel.nio.AbstractNioMessageChannel;
/*   7:    */ import io.netty.channel.socket.DefaultServerSocketChannelConfig;
/*   8:    */ import io.netty.channel.socket.ServerSocketChannelConfig;
/*   9:    */ import io.netty.util.internal.logging.InternalLogger;
/*  10:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  11:    */ import java.io.IOException;
/*  12:    */ import java.net.InetSocketAddress;
/*  13:    */ import java.net.ServerSocket;
/*  14:    */ import java.net.SocketAddress;
/*  15:    */ import java.nio.channels.SocketChannel;
/*  16:    */ import java.nio.channels.spi.SelectorProvider;
/*  17:    */ import java.util.List;
/*  18:    */ 
/*  19:    */ public class NioServerSocketChannel
/*  20:    */   extends AbstractNioMessageChannel
/*  21:    */   implements io.netty.channel.socket.ServerSocketChannel
/*  22:    */ {
/*  23: 44 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  24: 45 */   private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
/*  25: 47 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioServerSocketChannel.class);
/*  26:    */   private final ServerSocketChannelConfig config;
/*  27:    */   
/*  28:    */   private static java.nio.channels.ServerSocketChannel newSocket(SelectorProvider provider)
/*  29:    */   {
/*  30:    */     try
/*  31:    */     {
/*  32: 57 */       return provider.openServerSocketChannel();
/*  33:    */     }
/*  34:    */     catch (IOException e)
/*  35:    */     {
/*  36: 59 */       throw new ChannelException("Failed to open a server socket.", e);
/*  37:    */     }
/*  38:    */   }
/*  39:    */   
/*  40:    */   public NioServerSocketChannel()
/*  41:    */   {
/*  42: 70 */     this(newSocket(DEFAULT_SELECTOR_PROVIDER));
/*  43:    */   }
/*  44:    */   
/*  45:    */   public NioServerSocketChannel(SelectorProvider provider)
/*  46:    */   {
/*  47: 77 */     this(newSocket(provider));
/*  48:    */   }
/*  49:    */   
/*  50:    */   public NioServerSocketChannel(java.nio.channels.ServerSocketChannel channel)
/*  51:    */   {
/*  52: 84 */     super(null, channel, 16);
/*  53: 85 */     this.config = new NioServerSocketChannelConfig(this, javaChannel().socket(), null);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public InetSocketAddress localAddress()
/*  57:    */   {
/*  58: 90 */     return (InetSocketAddress)super.localAddress();
/*  59:    */   }
/*  60:    */   
/*  61:    */   public ChannelMetadata metadata()
/*  62:    */   {
/*  63: 95 */     return METADATA;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public ServerSocketChannelConfig config()
/*  67:    */   {
/*  68:100 */     return this.config;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public boolean isActive()
/*  72:    */   {
/*  73:105 */     return javaChannel().socket().isBound();
/*  74:    */   }
/*  75:    */   
/*  76:    */   public InetSocketAddress remoteAddress()
/*  77:    */   {
/*  78:110 */     return null;
/*  79:    */   }
/*  80:    */   
/*  81:    */   protected java.nio.channels.ServerSocketChannel javaChannel()
/*  82:    */   {
/*  83:115 */     return (java.nio.channels.ServerSocketChannel)super.javaChannel();
/*  84:    */   }
/*  85:    */   
/*  86:    */   protected SocketAddress localAddress0()
/*  87:    */   {
/*  88:120 */     return javaChannel().socket().getLocalSocketAddress();
/*  89:    */   }
/*  90:    */   
/*  91:    */   protected void doBind(SocketAddress localAddress)
/*  92:    */     throws Exception
/*  93:    */   {
/*  94:125 */     javaChannel().socket().bind(localAddress, this.config.getBacklog());
/*  95:    */   }
/*  96:    */   
/*  97:    */   protected void doClose()
/*  98:    */     throws Exception
/*  99:    */   {
/* 100:130 */     javaChannel().close();
/* 101:    */   }
/* 102:    */   
/* 103:    */   protected int doReadMessages(List<Object> buf)
/* 104:    */     throws Exception
/* 105:    */   {
/* 106:135 */     SocketChannel ch = javaChannel().accept();
/* 107:    */     try
/* 108:    */     {
/* 109:138 */       if (ch != null)
/* 110:    */       {
/* 111:139 */         buf.add(new NioSocketChannel(this, ch));
/* 112:140 */         return 1;
/* 113:    */       }
/* 114:    */     }
/* 115:    */     catch (Throwable t)
/* 116:    */     {
/* 117:143 */       logger.warn("Failed to create a new channel from an accepted socket.", t);
/* 118:    */       try
/* 119:    */       {
/* 120:146 */         ch.close();
/* 121:    */       }
/* 122:    */       catch (Throwable t2)
/* 123:    */       {
/* 124:148 */         logger.warn("Failed to close a socket.", t2);
/* 125:    */       }
/* 126:    */     }
/* 127:152 */     return 0;
/* 128:    */   }
/* 129:    */   
/* 130:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 131:    */     throws Exception
/* 132:    */   {
/* 133:159 */     throw new UnsupportedOperationException();
/* 134:    */   }
/* 135:    */   
/* 136:    */   protected void doFinishConnect()
/* 137:    */     throws Exception
/* 138:    */   {
/* 139:164 */     throw new UnsupportedOperationException();
/* 140:    */   }
/* 141:    */   
/* 142:    */   protected SocketAddress remoteAddress0()
/* 143:    */   {
/* 144:169 */     return null;
/* 145:    */   }
/* 146:    */   
/* 147:    */   protected void doDisconnect()
/* 148:    */     throws Exception
/* 149:    */   {
/* 150:174 */     throw new UnsupportedOperationException();
/* 151:    */   }
/* 152:    */   
/* 153:    */   protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in)
/* 154:    */     throws Exception
/* 155:    */   {
/* 156:179 */     throw new UnsupportedOperationException();
/* 157:    */   }
/* 158:    */   
/* 159:    */   private final class NioServerSocketChannelConfig
/* 160:    */     extends DefaultServerSocketChannelConfig
/* 161:    */   {
/* 162:    */     private NioServerSocketChannelConfig(NioServerSocketChannel channel, ServerSocket javaSocket)
/* 163:    */     {
/* 164:184 */       super(javaSocket);
/* 165:    */     }
/* 166:    */     
/* 167:    */     protected void autoReadCleared()
/* 168:    */     {
/* 169:189 */       NioServerSocketChannel.this.setReadPending(false);
/* 170:    */     }
/* 171:    */   }
/* 172:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.socket.nio.NioServerSocketChannel
 * JD-Core Version:    0.7.0.1
 */