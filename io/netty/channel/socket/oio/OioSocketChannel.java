/*   1:    */ package io.netty.channel.socket.oio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.ChannelException;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelPromise;
/*   8:    */ import io.netty.channel.ConnectTimeoutException;
/*   9:    */ import io.netty.channel.EventLoop;
/*  10:    */ import io.netty.channel.oio.OioByteStreamChannel;
/*  11:    */ import io.netty.channel.socket.ServerSocketChannel;
/*  12:    */ import io.netty.channel.socket.SocketChannel;
/*  13:    */ import io.netty.util.internal.logging.InternalLogger;
/*  14:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  15:    */ import java.io.IOException;
/*  16:    */ import java.net.InetSocketAddress;
/*  17:    */ import java.net.Socket;
/*  18:    */ import java.net.SocketAddress;
/*  19:    */ import java.net.SocketTimeoutException;
/*  20:    */ 
/*  21:    */ public class OioSocketChannel
/*  22:    */   extends OioByteStreamChannel
/*  23:    */   implements SocketChannel
/*  24:    */ {
/*  25: 43 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(OioSocketChannel.class);
/*  26:    */   private final Socket socket;
/*  27:    */   private final OioSocketChannelConfig config;
/*  28:    */   
/*  29:    */   public OioSocketChannel()
/*  30:    */   {
/*  31: 53 */     this(new Socket());
/*  32:    */   }
/*  33:    */   
/*  34:    */   public OioSocketChannel(Socket socket)
/*  35:    */   {
/*  36: 62 */     this(null, socket);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public OioSocketChannel(Channel parent, Socket socket)
/*  40:    */   {
/*  41: 73 */     super(parent);
/*  42: 74 */     this.socket = socket;
/*  43: 75 */     this.config = new DefaultOioSocketChannelConfig(this, socket);
/*  44:    */     
/*  45: 77 */     boolean success = false;
/*  46:    */     try
/*  47:    */     {
/*  48: 79 */       if (socket.isConnected()) {
/*  49: 80 */         activate(socket.getInputStream(), socket.getOutputStream());
/*  50:    */       }
/*  51: 82 */       socket.setSoTimeout(1000);
/*  52: 83 */       success = true; return;
/*  53:    */     }
/*  54:    */     catch (Exception e)
/*  55:    */     {
/*  56: 85 */       throw new ChannelException("failed to initialize a socket", e);
/*  57:    */     }
/*  58:    */     finally
/*  59:    */     {
/*  60: 87 */       if (!success) {
/*  61:    */         try
/*  62:    */         {
/*  63: 89 */           socket.close();
/*  64:    */         }
/*  65:    */         catch (IOException e)
/*  66:    */         {
/*  67: 91 */           logger.warn("Failed to close a socket.", e);
/*  68:    */         }
/*  69:    */       }
/*  70:    */     }
/*  71:    */   }
/*  72:    */   
/*  73:    */   public ServerSocketChannel parent()
/*  74:    */   {
/*  75: 99 */     return (ServerSocketChannel)super.parent();
/*  76:    */   }
/*  77:    */   
/*  78:    */   public OioSocketChannelConfig config()
/*  79:    */   {
/*  80:104 */     return this.config;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public boolean isOpen()
/*  84:    */   {
/*  85:109 */     return !this.socket.isClosed();
/*  86:    */   }
/*  87:    */   
/*  88:    */   public boolean isActive()
/*  89:    */   {
/*  90:114 */     return (!this.socket.isClosed()) && (this.socket.isConnected());
/*  91:    */   }
/*  92:    */   
/*  93:    */   public boolean isInputShutdown()
/*  94:    */   {
/*  95:119 */     return super.isInputShutdown();
/*  96:    */   }
/*  97:    */   
/*  98:    */   public boolean isOutputShutdown()
/*  99:    */   {
/* 100:124 */     return (this.socket.isOutputShutdown()) || (!isActive());
/* 101:    */   }
/* 102:    */   
/* 103:    */   public ChannelFuture shutdownOutput()
/* 104:    */   {
/* 105:129 */     return shutdownOutput(newPromise());
/* 106:    */   }
/* 107:    */   
/* 108:    */   protected int doReadBytes(ByteBuf buf)
/* 109:    */     throws Exception
/* 110:    */   {
/* 111:134 */     if (this.socket.isClosed()) {
/* 112:135 */       return -1;
/* 113:    */     }
/* 114:    */     try
/* 115:    */     {
/* 116:138 */       return super.doReadBytes(buf);
/* 117:    */     }
/* 118:    */     catch (SocketTimeoutException e) {}
/* 119:140 */     return 0;
/* 120:    */   }
/* 121:    */   
/* 122:    */   public ChannelFuture shutdownOutput(final ChannelPromise future)
/* 123:    */   {
/* 124:146 */     EventLoop loop = eventLoop();
/* 125:147 */     if (loop.inEventLoop()) {
/* 126:    */       try
/* 127:    */       {
/* 128:149 */         this.socket.shutdownOutput();
/* 129:150 */         future.setSuccess();
/* 130:    */       }
/* 131:    */       catch (Throwable t)
/* 132:    */       {
/* 133:152 */         future.setFailure(t);
/* 134:    */       }
/* 135:    */     } else {
/* 136:155 */       loop.execute(new Runnable()
/* 137:    */       {
/* 138:    */         public void run()
/* 139:    */         {
/* 140:158 */           OioSocketChannel.this.shutdownOutput(future);
/* 141:    */         }
/* 142:    */       });
/* 143:    */     }
/* 144:162 */     return future;
/* 145:    */   }
/* 146:    */   
/* 147:    */   public InetSocketAddress localAddress()
/* 148:    */   {
/* 149:167 */     return (InetSocketAddress)super.localAddress();
/* 150:    */   }
/* 151:    */   
/* 152:    */   public InetSocketAddress remoteAddress()
/* 153:    */   {
/* 154:172 */     return (InetSocketAddress)super.remoteAddress();
/* 155:    */   }
/* 156:    */   
/* 157:    */   protected SocketAddress localAddress0()
/* 158:    */   {
/* 159:177 */     return this.socket.getLocalSocketAddress();
/* 160:    */   }
/* 161:    */   
/* 162:    */   protected SocketAddress remoteAddress0()
/* 163:    */   {
/* 164:182 */     return this.socket.getRemoteSocketAddress();
/* 165:    */   }
/* 166:    */   
/* 167:    */   protected void doBind(SocketAddress localAddress)
/* 168:    */     throws Exception
/* 169:    */   {
/* 170:187 */     this.socket.bind(localAddress);
/* 171:    */   }
/* 172:    */   
/* 173:    */   protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 174:    */     throws Exception
/* 175:    */   {
/* 176:193 */     if (localAddress != null) {
/* 177:194 */       this.socket.bind(localAddress);
/* 178:    */     }
/* 179:197 */     boolean success = false;
/* 180:    */     try
/* 181:    */     {
/* 182:199 */       this.socket.connect(remoteAddress, config().getConnectTimeoutMillis());
/* 183:200 */       activate(this.socket.getInputStream(), this.socket.getOutputStream());
/* 184:201 */       success = true;
/* 185:    */     }
/* 186:    */     catch (SocketTimeoutException e)
/* 187:    */     {
/* 188:203 */       ConnectTimeoutException cause = new ConnectTimeoutException("connection timed out: " + remoteAddress);
/* 189:204 */       cause.setStackTrace(e.getStackTrace());
/* 190:205 */       throw cause;
/* 191:    */     }
/* 192:    */     finally
/* 193:    */     {
/* 194:207 */       if (!success) {
/* 195:208 */         doClose();
/* 196:    */       }
/* 197:    */     }
/* 198:    */   }
/* 199:    */   
/* 200:    */   protected void doDisconnect()
/* 201:    */     throws Exception
/* 202:    */   {
/* 203:215 */     doClose();
/* 204:    */   }
/* 205:    */   
/* 206:    */   protected void doClose()
/* 207:    */     throws Exception
/* 208:    */   {
/* 209:220 */     this.socket.close();
/* 210:    */   }
/* 211:    */   
/* 212:    */   protected boolean checkInputShutdown()
/* 213:    */   {
/* 214:225 */     if (isInputShutdown())
/* 215:    */     {
/* 216:    */       try
/* 217:    */       {
/* 218:227 */         Thread.sleep(config().getSoTimeout());
/* 219:    */       }
/* 220:    */       catch (Throwable e) {}
/* 221:231 */       return true;
/* 222:    */     }
/* 223:233 */     return false;
/* 224:    */   }
/* 225:    */   
/* 226:    */   protected void setReadPending(boolean readPending)
/* 227:    */   {
/* 228:238 */     super.setReadPending(readPending);
/* 229:    */   }
/* 230:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.socket.oio.OioSocketChannel
 * JD-Core Version:    0.7.0.1
 */