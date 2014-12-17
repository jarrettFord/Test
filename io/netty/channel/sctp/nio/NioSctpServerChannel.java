/*   1:    */ package io.netty.channel.sctp.nio;
/*   2:    */ 
/*   3:    */ import com.sun.nio.sctp.SctpChannel;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelFuture;
/*   6:    */ import io.netty.channel.ChannelMetadata;
/*   7:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   8:    */ import io.netty.channel.ChannelPromise;
/*   9:    */ import io.netty.channel.nio.AbstractNioMessageChannel;
/*  10:    */ import io.netty.channel.nio.NioEventLoop;
/*  11:    */ import io.netty.channel.sctp.DefaultSctpServerChannelConfig;
/*  12:    */ import io.netty.channel.sctp.SctpServerChannelConfig;
/*  13:    */ import java.io.IOException;
/*  14:    */ import java.net.InetAddress;
/*  15:    */ import java.net.InetSocketAddress;
/*  16:    */ import java.net.SocketAddress;
/*  17:    */ import java.util.Collections;
/*  18:    */ import java.util.Iterator;
/*  19:    */ import java.util.LinkedHashSet;
/*  20:    */ import java.util.List;
/*  21:    */ import java.util.Set;
/*  22:    */ 
/*  23:    */ public class NioSctpServerChannel
/*  24:    */   extends AbstractNioMessageChannel
/*  25:    */   implements io.netty.channel.sctp.SctpServerChannel
/*  26:    */ {
/*  27: 49 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  28:    */   private final SctpServerChannelConfig config;
/*  29:    */   
/*  30:    */   private static com.sun.nio.sctp.SctpServerChannel newSocket()
/*  31:    */   {
/*  32:    */     try
/*  33:    */     {
/*  34: 53 */       return com.sun.nio.sctp.SctpServerChannel.open();
/*  35:    */     }
/*  36:    */     catch (IOException e)
/*  37:    */     {
/*  38: 55 */       throw new ChannelException("Failed to open a server socket.", e);
/*  39:    */     }
/*  40:    */   }
/*  41:    */   
/*  42:    */   public NioSctpServerChannel()
/*  43:    */   {
/*  44: 66 */     super(null, newSocket(), 16);
/*  45: 67 */     this.config = new NioSctpServerChannelConfig(this, javaChannel(), null);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public ChannelMetadata metadata()
/*  49:    */   {
/*  50: 72 */     return METADATA;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public Set<InetSocketAddress> allLocalAddresses()
/*  54:    */   {
/*  55:    */     try
/*  56:    */     {
/*  57: 78 */       Set<SocketAddress> allLocalAddresses = javaChannel().getAllLocalAddresses();
/*  58: 79 */       Set<InetSocketAddress> addresses = new LinkedHashSet(allLocalAddresses.size());
/*  59: 80 */       for (SocketAddress socketAddress : allLocalAddresses) {
/*  60: 81 */         addresses.add((InetSocketAddress)socketAddress);
/*  61:    */       }
/*  62: 83 */       return addresses;
/*  63:    */     }
/*  64:    */     catch (Throwable t) {}
/*  65: 85 */     return Collections.emptySet();
/*  66:    */   }
/*  67:    */   
/*  68:    */   public SctpServerChannelConfig config()
/*  69:    */   {
/*  70: 91 */     return this.config;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean isActive()
/*  74:    */   {
/*  75: 96 */     return (isOpen()) && (!allLocalAddresses().isEmpty());
/*  76:    */   }
/*  77:    */   
/*  78:    */   public InetSocketAddress remoteAddress()
/*  79:    */   {
/*  80:101 */     return null;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public InetSocketAddress localAddress()
/*  84:    */   {
/*  85:106 */     return (InetSocketAddress)super.localAddress();
/*  86:    */   }
/*  87:    */   
/*  88:    */   protected com.sun.nio.sctp.SctpServerChannel javaChannel()
/*  89:    */   {
/*  90:111 */     return (com.sun.nio.sctp.SctpServerChannel)super.javaChannel();
/*  91:    */   }
/*  92:    */   
/*  93:    */   protected SocketAddress localAddress0()
/*  94:    */   {
/*  95:    */     try
/*  96:    */     {
/*  97:117 */       Iterator<SocketAddress> i = javaChannel().getAllLocalAddresses().iterator();
/*  98:118 */       if (i.hasNext()) {
/*  99:119 */         return (SocketAddress)i.next();
/* 100:    */       }
/* 101:    */     }
/* 102:    */     catch (IOException e) {}
/* 103:124 */     return null;
/* 104:    */   }
/* 105:    */   
/* 106:    */   protected void doBind(SocketAddress localAddress)
/* 107:    */     throws Exception
/* 108:    */   {
/* 109:129 */     javaChannel().bind(localAddress, this.config.getBacklog());
/* 110:    */   }
/* 111:    */   
/* 112:    */   protected void doClose()
/* 113:    */     throws Exception
/* 114:    */   {
/* 115:134 */     javaChannel().close();
/* 116:    */   }
/* 117:    */   
/* 118:    */   protected int doReadMessages(List<Object> buf)
/* 119:    */     throws Exception
/* 120:    */   {
/* 121:139 */     SctpChannel ch = javaChannel().accept();
/* 122:140 */     if (ch == null) {
/* 123:141 */       return 0;
/* 124:    */     }
/* 125:143 */     buf.add(new NioSctpChannel(this, ch));
/* 126:144 */     return 1;
/* 127:    */   }
/* 128:    */   
/* 129:    */   public ChannelFuture bindAddress(InetAddress localAddress)
/* 130:    */   {
/* 131:149 */     return bindAddress(localAddress, newPromise());
/* 132:    */   }
/* 133:    */   
/* 134:    */   public ChannelFuture bindAddress(final InetAddress localAddress, final ChannelPromise promise)
/* 135:    */   {
/* 136:154 */     if (eventLoop().inEventLoop()) {
/* 137:    */       try
/* 138:    */       {
/* 139:156 */         javaChannel().bindAddress(localAddress);
/* 140:157 */         promise.setSuccess();
/* 141:    */       }
/* 142:    */       catch (Throwable t)
/* 143:    */       {
/* 144:159 */         promise.setFailure(t);
/* 145:    */       }
/* 146:    */     } else {
/* 147:162 */       eventLoop().execute(new Runnable()
/* 148:    */       {
/* 149:    */         public void run()
/* 150:    */         {
/* 151:165 */           NioSctpServerChannel.this.bindAddress(localAddress, promise);
/* 152:    */         }
/* 153:    */       });
/* 154:    */     }
/* 155:169 */     return promise;
/* 156:    */   }
/* 157:    */   
/* 158:    */   public ChannelFuture unbindAddress(InetAddress localAddress)
/* 159:    */   {
/* 160:174 */     return unbindAddress(localAddress, newPromise());
/* 161:    */   }
/* 162:    */   
/* 163:    */   public ChannelFuture unbindAddress(final InetAddress localAddress, final ChannelPromise promise)
/* 164:    */   {
/* 165:179 */     if (eventLoop().inEventLoop()) {
/* 166:    */       try
/* 167:    */       {
/* 168:181 */         javaChannel().unbindAddress(localAddress);
/* 169:182 */         promise.setSuccess();
/* 170:    */       }
/* 171:    */       catch (Throwable t)
/* 172:    */       {
/* 173:184 */         promise.setFailure(t);
/* 174:    */       }
/* 175:    */     } else {
/* 176:187 */       eventLoop().execute(new Runnable()
/* 177:    */       {
/* 178:    */         public void run()
/* 179:    */         {
/* 180:190 */           NioSctpServerChannel.this.unbindAddress(localAddress, promise);
/* 181:    */         }
/* 182:    */       });
/* 183:    */     }
/* 184:194 */     return promise;
/* 185:    */   }
/* 186:    */   
/* 187:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 188:    */     throws Exception
/* 189:    */   {
/* 190:201 */     throw new UnsupportedOperationException();
/* 191:    */   }
/* 192:    */   
/* 193:    */   protected void doFinishConnect()
/* 194:    */     throws Exception
/* 195:    */   {
/* 196:206 */     throw new UnsupportedOperationException();
/* 197:    */   }
/* 198:    */   
/* 199:    */   protected SocketAddress remoteAddress0()
/* 200:    */   {
/* 201:211 */     return null;
/* 202:    */   }
/* 203:    */   
/* 204:    */   protected void doDisconnect()
/* 205:    */     throws Exception
/* 206:    */   {
/* 207:216 */     throw new UnsupportedOperationException();
/* 208:    */   }
/* 209:    */   
/* 210:    */   protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in)
/* 211:    */     throws Exception
/* 212:    */   {
/* 213:221 */     throw new UnsupportedOperationException();
/* 214:    */   }
/* 215:    */   
/* 216:    */   private final class NioSctpServerChannelConfig
/* 217:    */     extends DefaultSctpServerChannelConfig
/* 218:    */   {
/* 219:    */     private NioSctpServerChannelConfig(NioSctpServerChannel channel, com.sun.nio.sctp.SctpServerChannel javaChannel)
/* 220:    */     {
/* 221:226 */       super(javaChannel);
/* 222:    */     }
/* 223:    */     
/* 224:    */     protected void autoReadCleared()
/* 225:    */     {
/* 226:231 */       NioSctpServerChannel.this.setReadPending(false);
/* 227:    */     }
/* 228:    */   }
/* 229:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.sctp.nio.NioSctpServerChannel
 * JD-Core Version:    0.7.0.1
 */