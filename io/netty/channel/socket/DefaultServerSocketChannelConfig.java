/*   1:    */ package io.netty.channel.socket;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.DefaultChannelConfig;
/*   7:    */ import io.netty.channel.MessageSizeEstimator;
/*   8:    */ import io.netty.channel.RecvByteBufAllocator;
/*   9:    */ import io.netty.util.NetUtil;
/*  10:    */ import java.net.ServerSocket;
/*  11:    */ import java.net.SocketException;
/*  12:    */ import java.util.Map;
/*  13:    */ 
/*  14:    */ public class DefaultServerSocketChannelConfig
/*  15:    */   extends DefaultChannelConfig
/*  16:    */   implements ServerSocketChannelConfig
/*  17:    */ {
/*  18:    */   protected final ServerSocket javaSocket;
/*  19: 39 */   private volatile int backlog = NetUtil.SOMAXCONN;
/*  20:    */   
/*  21:    */   public DefaultServerSocketChannelConfig(ServerSocketChannel channel, ServerSocket javaSocket)
/*  22:    */   {
/*  23: 45 */     super(channel);
/*  24: 46 */     if (javaSocket == null) {
/*  25: 47 */       throw new NullPointerException("javaSocket");
/*  26:    */     }
/*  27: 49 */     this.javaSocket = javaSocket;
/*  28:    */   }
/*  29:    */   
/*  30:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  31:    */   {
/*  32: 54 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_REUSEADDR, ChannelOption.SO_BACKLOG });
/*  33:    */   }
/*  34:    */   
/*  35:    */   public <T> T getOption(ChannelOption<T> option)
/*  36:    */   {
/*  37: 60 */     if (option == ChannelOption.SO_RCVBUF) {
/*  38: 61 */       return Integer.valueOf(getReceiveBufferSize());
/*  39:    */     }
/*  40: 63 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  41: 64 */       return Boolean.valueOf(isReuseAddress());
/*  42:    */     }
/*  43: 66 */     if (option == ChannelOption.SO_BACKLOG) {
/*  44: 67 */       return Integer.valueOf(getBacklog());
/*  45:    */     }
/*  46: 70 */     return super.getOption(option);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  50:    */   {
/*  51: 75 */     validate(option, value);
/*  52: 77 */     if (option == ChannelOption.SO_RCVBUF) {
/*  53: 78 */       setReceiveBufferSize(((Integer)value).intValue());
/*  54: 79 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  55: 80 */       setReuseAddress(((Boolean)value).booleanValue());
/*  56: 81 */     } else if (option == ChannelOption.SO_BACKLOG) {
/*  57: 82 */       setBacklog(((Integer)value).intValue());
/*  58:    */     } else {
/*  59: 84 */       return super.setOption(option, value);
/*  60:    */     }
/*  61: 87 */     return true;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public boolean isReuseAddress()
/*  65:    */   {
/*  66:    */     try
/*  67:    */     {
/*  68: 93 */       return this.javaSocket.getReuseAddress();
/*  69:    */     }
/*  70:    */     catch (SocketException e)
/*  71:    */     {
/*  72: 95 */       throw new ChannelException(e);
/*  73:    */     }
/*  74:    */   }
/*  75:    */   
/*  76:    */   public ServerSocketChannelConfig setReuseAddress(boolean reuseAddress)
/*  77:    */   {
/*  78:    */     try
/*  79:    */     {
/*  80:102 */       this.javaSocket.setReuseAddress(reuseAddress);
/*  81:    */     }
/*  82:    */     catch (SocketException e)
/*  83:    */     {
/*  84:104 */       throw new ChannelException(e);
/*  85:    */     }
/*  86:106 */     return this;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public int getReceiveBufferSize()
/*  90:    */   {
/*  91:    */     try
/*  92:    */     {
/*  93:112 */       return this.javaSocket.getReceiveBufferSize();
/*  94:    */     }
/*  95:    */     catch (SocketException e)
/*  96:    */     {
/*  97:114 */       throw new ChannelException(e);
/*  98:    */     }
/*  99:    */   }
/* 100:    */   
/* 101:    */   public ServerSocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 102:    */   {
/* 103:    */     try
/* 104:    */     {
/* 105:121 */       this.javaSocket.setReceiveBufferSize(receiveBufferSize);
/* 106:    */     }
/* 107:    */     catch (SocketException e)
/* 108:    */     {
/* 109:123 */       throw new ChannelException(e);
/* 110:    */     }
/* 111:125 */     return this;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public ServerSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/* 115:    */   {
/* 116:130 */     this.javaSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
/* 117:131 */     return this;
/* 118:    */   }
/* 119:    */   
/* 120:    */   public int getBacklog()
/* 121:    */   {
/* 122:136 */     return this.backlog;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public ServerSocketChannelConfig setBacklog(int backlog)
/* 126:    */   {
/* 127:141 */     if (backlog < 0) {
/* 128:142 */       throw new IllegalArgumentException("backlog: " + backlog);
/* 129:    */     }
/* 130:144 */     this.backlog = backlog;
/* 131:145 */     return this;
/* 132:    */   }
/* 133:    */   
/* 134:    */   public ServerSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 135:    */   {
/* 136:150 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 137:151 */     return this;
/* 138:    */   }
/* 139:    */   
/* 140:    */   public ServerSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 141:    */   {
/* 142:156 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 143:157 */     return this;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public ServerSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 147:    */   {
/* 148:162 */     super.setWriteSpinCount(writeSpinCount);
/* 149:163 */     return this;
/* 150:    */   }
/* 151:    */   
/* 152:    */   public ServerSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 153:    */   {
/* 154:168 */     super.setAllocator(allocator);
/* 155:169 */     return this;
/* 156:    */   }
/* 157:    */   
/* 158:    */   public ServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 159:    */   {
/* 160:174 */     super.setRecvByteBufAllocator(allocator);
/* 161:175 */     return this;
/* 162:    */   }
/* 163:    */   
/* 164:    */   public ServerSocketChannelConfig setAutoRead(boolean autoRead)
/* 165:    */   {
/* 166:180 */     super.setAutoRead(autoRead);
/* 167:181 */     return this;
/* 168:    */   }
/* 169:    */   
/* 170:    */   public ServerSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 171:    */   {
/* 172:186 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 173:187 */     return this;
/* 174:    */   }
/* 175:    */   
/* 176:    */   public ServerSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 177:    */   {
/* 178:192 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 179:193 */     return this;
/* 180:    */   }
/* 181:    */   
/* 182:    */   public ServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 183:    */   {
/* 184:198 */     super.setMessageSizeEstimator(estimator);
/* 185:199 */     return this;
/* 186:    */   }
/* 187:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.socket.DefaultServerSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */