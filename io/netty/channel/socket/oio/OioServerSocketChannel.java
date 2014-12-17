/*   1:    */ package io.netty.channel.socket.oio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelException;
/*   4:    */ import io.netty.channel.ChannelMetadata;
/*   5:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   6:    */ import io.netty.channel.oio.AbstractOioMessageChannel;
/*   7:    */ import io.netty.channel.socket.ServerSocketChannel;
/*   8:    */ import io.netty.util.internal.logging.InternalLogger;
/*   9:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  10:    */ import java.io.IOException;
/*  11:    */ import java.net.InetSocketAddress;
/*  12:    */ import java.net.ServerSocket;
/*  13:    */ import java.net.Socket;
/*  14:    */ import java.net.SocketAddress;
/*  15:    */ import java.net.SocketTimeoutException;
/*  16:    */ import java.util.List;
/*  17:    */ import java.util.concurrent.locks.Lock;
/*  18:    */ import java.util.concurrent.locks.ReentrantLock;
/*  19:    */ 
/*  20:    */ public class OioServerSocketChannel
/*  21:    */   extends AbstractOioMessageChannel
/*  22:    */   implements ServerSocketChannel
/*  23:    */ {
/*  24: 44 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(OioServerSocketChannel.class);
/*  25: 47 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  26:    */   final ServerSocket socket;
/*  27:    */   
/*  28:    */   private static ServerSocket newServerSocket()
/*  29:    */   {
/*  30:    */     try
/*  31:    */     {
/*  32: 51 */       return new ServerSocket();
/*  33:    */     }
/*  34:    */     catch (IOException e)
/*  35:    */     {
/*  36: 53 */       throw new ChannelException("failed to create a server socket", e);
/*  37:    */     }
/*  38:    */   }
/*  39:    */   
/*  40: 58 */   final Lock shutdownLock = new ReentrantLock();
/*  41:    */   private final OioServerSocketChannelConfig config;
/*  42:    */   
/*  43:    */   public OioServerSocketChannel()
/*  44:    */   {
/*  45: 65 */     this(newServerSocket());
/*  46:    */   }
/*  47:    */   
/*  48:    */   public OioServerSocketChannel(ServerSocket socket)
/*  49:    */   {
/*  50: 74 */     super(null);
/*  51: 75 */     if (socket == null) {
/*  52: 76 */       throw new NullPointerException("socket");
/*  53:    */     }
/*  54: 79 */     boolean success = false;
/*  55:    */     try
/*  56:    */     {
/*  57: 81 */       socket.setSoTimeout(1000);
/*  58: 82 */       success = true;
/*  59: 87 */       if (!success) {
/*  60:    */         try
/*  61:    */         {
/*  62: 89 */           socket.close();
/*  63:    */         }
/*  64:    */         catch (IOException e)
/*  65:    */         {
/*  66: 91 */           if (logger.isWarnEnabled()) {
/*  67: 92 */             logger.warn("Failed to close a partially initialized socket.", e);
/*  68:    */           }
/*  69:    */         }
/*  70:    */       }
/*  71: 98 */       this.socket = socket;
/*  72:    */     }
/*  73:    */     catch (IOException e)
/*  74:    */     {
/*  75: 84 */       throw new ChannelException("Failed to set the server socket timeout.", e);
/*  76:    */     }
/*  77:    */     finally
/*  78:    */     {
/*  79: 87 */       if (!success) {
/*  80:    */         try
/*  81:    */         {
/*  82: 89 */           socket.close();
/*  83:    */         }
/*  84:    */         catch (IOException e)
/*  85:    */         {
/*  86: 91 */           if (logger.isWarnEnabled()) {
/*  87: 92 */             logger.warn("Failed to close a partially initialized socket.", e);
/*  88:    */           }
/*  89:    */         }
/*  90:    */       }
/*  91:    */     }
/*  92: 99 */     this.config = new DefaultOioServerSocketChannelConfig(this, socket);
/*  93:    */   }
/*  94:    */   
/*  95:    */   public InetSocketAddress localAddress()
/*  96:    */   {
/*  97:104 */     return (InetSocketAddress)super.localAddress();
/*  98:    */   }
/*  99:    */   
/* 100:    */   public ChannelMetadata metadata()
/* 101:    */   {
/* 102:109 */     return METADATA;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public OioServerSocketChannelConfig config()
/* 106:    */   {
/* 107:114 */     return this.config;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public InetSocketAddress remoteAddress()
/* 111:    */   {
/* 112:119 */     return null;
/* 113:    */   }
/* 114:    */   
/* 115:    */   public boolean isOpen()
/* 116:    */   {
/* 117:124 */     return !this.socket.isClosed();
/* 118:    */   }
/* 119:    */   
/* 120:    */   public boolean isActive()
/* 121:    */   {
/* 122:129 */     return (isOpen()) && (this.socket.isBound());
/* 123:    */   }
/* 124:    */   
/* 125:    */   protected SocketAddress localAddress0()
/* 126:    */   {
/* 127:134 */     return this.socket.getLocalSocketAddress();
/* 128:    */   }
/* 129:    */   
/* 130:    */   protected void doBind(SocketAddress localAddress)
/* 131:    */     throws Exception
/* 132:    */   {
/* 133:139 */     this.socket.bind(localAddress, this.config.getBacklog());
/* 134:    */   }
/* 135:    */   
/* 136:    */   protected void doClose()
/* 137:    */     throws Exception
/* 138:    */   {
/* 139:144 */     this.socket.close();
/* 140:    */   }
/* 141:    */   
/* 142:    */   protected int doReadMessages(List<Object> buf)
/* 143:    */     throws Exception
/* 144:    */   {
/* 145:149 */     if (this.socket.isClosed()) {
/* 146:150 */       return -1;
/* 147:    */     }
/* 148:    */     try
/* 149:    */     {
/* 150:154 */       Socket s = this.socket.accept();
/* 151:    */       try
/* 152:    */       {
/* 153:156 */         if (s != null)
/* 154:    */         {
/* 155:157 */           buf.add(new OioSocketChannel(this, s));
/* 156:158 */           return 1;
/* 157:    */         }
/* 158:    */       }
/* 159:    */       catch (Throwable t)
/* 160:    */       {
/* 161:161 */         logger.warn("Failed to create a new channel from an accepted socket.", t);
/* 162:162 */         if (s != null) {
/* 163:    */           try
/* 164:    */           {
/* 165:164 */             s.close();
/* 166:    */           }
/* 167:    */           catch (Throwable t2)
/* 168:    */           {
/* 169:166 */             logger.warn("Failed to close a socket.", t2);
/* 170:    */           }
/* 171:    */         }
/* 172:    */       }
/* 173:    */     }
/* 174:    */     catch (SocketTimeoutException e) {}
/* 175:173 */     return 0;
/* 176:    */   }
/* 177:    */   
/* 178:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 179:    */     throws Exception
/* 180:    */   {
/* 181:178 */     throw new UnsupportedOperationException();
/* 182:    */   }
/* 183:    */   
/* 184:    */   protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 185:    */     throws Exception
/* 186:    */   {
/* 187:184 */     throw new UnsupportedOperationException();
/* 188:    */   }
/* 189:    */   
/* 190:    */   protected SocketAddress remoteAddress0()
/* 191:    */   {
/* 192:189 */     return null;
/* 193:    */   }
/* 194:    */   
/* 195:    */   protected void doDisconnect()
/* 196:    */     throws Exception
/* 197:    */   {
/* 198:194 */     throw new UnsupportedOperationException();
/* 199:    */   }
/* 200:    */   
/* 201:    */   protected void setReadPending(boolean readPending)
/* 202:    */   {
/* 203:199 */     super.setReadPending(readPending);
/* 204:    */   }
/* 205:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.socket.oio.OioServerSocketChannel
 * JD-Core Version:    0.7.0.1
 */