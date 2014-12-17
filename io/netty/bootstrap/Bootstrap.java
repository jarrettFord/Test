/*   1:    */ package io.netty.bootstrap;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelConfig;
/*   5:    */ import io.netty.channel.ChannelFuture;
/*   6:    */ import io.netty.channel.ChannelFutureListener;
/*   7:    */ import io.netty.channel.ChannelHandler;
/*   8:    */ import io.netty.channel.ChannelOption;
/*   9:    */ import io.netty.channel.ChannelPipeline;
/*  10:    */ import io.netty.channel.ChannelPromise;
/*  11:    */ import io.netty.channel.EventLoop;
/*  12:    */ import io.netty.util.Attribute;
/*  13:    */ import io.netty.util.AttributeKey;
/*  14:    */ import io.netty.util.internal.logging.InternalLogger;
/*  15:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  16:    */ import java.net.InetAddress;
/*  17:    */ import java.net.InetSocketAddress;
/*  18:    */ import java.net.SocketAddress;
/*  19:    */ import java.util.Map;
/*  20:    */ import java.util.Map.Entry;
/*  21:    */ 
/*  22:    */ public final class Bootstrap
/*  23:    */   extends AbstractBootstrap<Bootstrap, Channel>
/*  24:    */ {
/*  25: 43 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(Bootstrap.class);
/*  26:    */   private volatile SocketAddress remoteAddress;
/*  27:    */   
/*  28:    */   public Bootstrap() {}
/*  29:    */   
/*  30:    */   private Bootstrap(Bootstrap bootstrap)
/*  31:    */   {
/*  32: 50 */     super(bootstrap);
/*  33: 51 */     this.remoteAddress = bootstrap.remoteAddress;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public Bootstrap remoteAddress(SocketAddress remoteAddress)
/*  37:    */   {
/*  38: 59 */     this.remoteAddress = remoteAddress;
/*  39: 60 */     return this;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public Bootstrap remoteAddress(String inetHost, int inetPort)
/*  43:    */   {
/*  44: 67 */     this.remoteAddress = new InetSocketAddress(inetHost, inetPort);
/*  45: 68 */     return this;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public Bootstrap remoteAddress(InetAddress inetHost, int inetPort)
/*  49:    */   {
/*  50: 75 */     this.remoteAddress = new InetSocketAddress(inetHost, inetPort);
/*  51: 76 */     return this;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public ChannelFuture connect()
/*  55:    */   {
/*  56: 83 */     validate();
/*  57: 84 */     SocketAddress remoteAddress = this.remoteAddress;
/*  58: 85 */     if (remoteAddress == null) {
/*  59: 86 */       throw new IllegalStateException("remoteAddress not set");
/*  60:    */     }
/*  61: 89 */     return doConnect(remoteAddress, localAddress());
/*  62:    */   }
/*  63:    */   
/*  64:    */   public ChannelFuture connect(String inetHost, int inetPort)
/*  65:    */   {
/*  66: 96 */     return connect(new InetSocketAddress(inetHost, inetPort));
/*  67:    */   }
/*  68:    */   
/*  69:    */   public ChannelFuture connect(InetAddress inetHost, int inetPort)
/*  70:    */   {
/*  71:103 */     return connect(new InetSocketAddress(inetHost, inetPort));
/*  72:    */   }
/*  73:    */   
/*  74:    */   public ChannelFuture connect(SocketAddress remoteAddress)
/*  75:    */   {
/*  76:110 */     if (remoteAddress == null) {
/*  77:111 */       throw new NullPointerException("remoteAddress");
/*  78:    */     }
/*  79:114 */     validate();
/*  80:115 */     return doConnect(remoteAddress, localAddress());
/*  81:    */   }
/*  82:    */   
/*  83:    */   public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress)
/*  84:    */   {
/*  85:122 */     if (remoteAddress == null) {
/*  86:123 */       throw new NullPointerException("remoteAddress");
/*  87:    */     }
/*  88:125 */     validate();
/*  89:126 */     return doConnect(remoteAddress, localAddress);
/*  90:    */   }
/*  91:    */   
/*  92:    */   private ChannelFuture doConnect(final SocketAddress remoteAddress, final SocketAddress localAddress)
/*  93:    */   {
/*  94:133 */     final ChannelFuture regFuture = initAndRegister();
/*  95:134 */     final Channel channel = regFuture.channel();
/*  96:135 */     if (regFuture.cause() != null) {
/*  97:136 */       return regFuture;
/*  98:    */     }
/*  99:139 */     final ChannelPromise promise = channel.newPromise();
/* 100:140 */     if (regFuture.isDone()) {
/* 101:141 */       doConnect0(regFuture, channel, remoteAddress, localAddress, promise);
/* 102:    */     } else {
/* 103:143 */       regFuture.addListener(new ChannelFutureListener()
/* 104:    */       {
/* 105:    */         public void operationComplete(ChannelFuture future)
/* 106:    */           throws Exception
/* 107:    */         {
/* 108:146 */           Bootstrap.doConnect0(regFuture, channel, remoteAddress, localAddress, promise);
/* 109:    */         }
/* 110:    */       });
/* 111:    */     }
/* 112:151 */     return promise;
/* 113:    */   }
/* 114:    */   
/* 115:    */   private static void doConnect0(ChannelFuture regFuture, final Channel channel, final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise)
/* 116:    */   {
/* 117:160 */     channel.eventLoop().execute(new Runnable()
/* 118:    */     {
/* 119:    */       public void run()
/* 120:    */       {
/* 121:163 */         if (this.val$regFuture.isSuccess())
/* 122:    */         {
/* 123:164 */           if (localAddress == null) {
/* 124:165 */             channel.connect(remoteAddress, promise);
/* 125:    */           } else {
/* 126:167 */             channel.connect(remoteAddress, localAddress, promise);
/* 127:    */           }
/* 128:169 */           promise.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
/* 129:    */         }
/* 130:    */         else
/* 131:    */         {
/* 132:171 */           promise.setFailure(this.val$regFuture.cause());
/* 133:    */         }
/* 134:    */       }
/* 135:    */     });
/* 136:    */   }
/* 137:    */   
/* 138:    */   void init(Channel channel)
/* 139:    */     throws Exception
/* 140:    */   {
/* 141:180 */     ChannelPipeline p = channel.pipeline();
/* 142:181 */     p.addLast(new ChannelHandler[] { handler() });
/* 143:    */     
/* 144:183 */     Map<ChannelOption<?>, Object> options = options();
/* 145:184 */     synchronized (options)
/* 146:    */     {
/* 147:185 */       for (Map.Entry<ChannelOption<?>, Object> e : options.entrySet()) {
/* 148:    */         try
/* 149:    */         {
/* 150:187 */           if (!channel.config().setOption((ChannelOption)e.getKey(), e.getValue())) {
/* 151:188 */             logger.warn("Unknown channel option: " + e);
/* 152:    */           }
/* 153:    */         }
/* 154:    */         catch (Throwable t)
/* 155:    */         {
/* 156:191 */           logger.warn("Failed to set a channel option: " + channel, t);
/* 157:    */         }
/* 158:    */       }
/* 159:    */     }
/* 160:196 */     Map<AttributeKey<?>, Object> attrs = attrs();
/* 161:197 */     synchronized (attrs)
/* 162:    */     {
/* 163:198 */       for (Map.Entry<AttributeKey<?>, Object> e : attrs.entrySet()) {
/* 164:199 */         channel.attr((AttributeKey)e.getKey()).set(e.getValue());
/* 165:    */       }
/* 166:    */     }
/* 167:    */   }
/* 168:    */   
/* 169:    */   public Bootstrap validate()
/* 170:    */   {
/* 171:206 */     super.validate();
/* 172:207 */     if (handler() == null) {
/* 173:208 */       throw new IllegalStateException("handler not set");
/* 174:    */     }
/* 175:210 */     return this;
/* 176:    */   }
/* 177:    */   
/* 178:    */   public Bootstrap clone()
/* 179:    */   {
/* 180:216 */     return new Bootstrap(this);
/* 181:    */   }
/* 182:    */   
/* 183:    */   public String toString()
/* 184:    */   {
/* 185:221 */     if (this.remoteAddress == null) {
/* 186:222 */       return super.toString();
/* 187:    */     }
/* 188:225 */     StringBuilder buf = new StringBuilder(super.toString());
/* 189:226 */     buf.setLength(buf.length() - 1);
/* 190:227 */     buf.append(", remoteAddress: ");
/* 191:228 */     buf.append(this.remoteAddress);
/* 192:229 */     buf.append(')');
/* 193:    */     
/* 194:231 */     return buf.toString();
/* 195:    */   }
/* 196:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.bootstrap.Bootstrap
 * JD-Core Version:    0.7.0.1
 */