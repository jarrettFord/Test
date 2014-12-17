/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelOption;
/*   5:    */ import io.netty.channel.DefaultChannelConfig;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator;
/*   8:    */ import io.netty.channel.socket.ServerSocketChannelConfig;
/*   9:    */ import io.netty.util.NetUtil;
/*  10:    */ import java.util.Map;
/*  11:    */ 
/*  12:    */ public final class EpollServerSocketChannelConfig
/*  13:    */   extends DefaultChannelConfig
/*  14:    */   implements ServerSocketChannelConfig
/*  15:    */ {
/*  16:    */   private final EpollServerSocketChannel channel;
/*  17: 36 */   private volatile int backlog = NetUtil.SOMAXCONN;
/*  18:    */   
/*  19:    */   EpollServerSocketChannelConfig(EpollServerSocketChannel channel)
/*  20:    */   {
/*  21: 39 */     super(channel);
/*  22: 40 */     this.channel = channel;
/*  23:    */     
/*  24:    */ 
/*  25:    */ 
/*  26:    */ 
/*  27: 45 */     setReuseAddress(true);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  31:    */   {
/*  32: 50 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_REUSEADDR, ChannelOption.SO_BACKLOG, EpollChannelOption.SO_REUSEPORT });
/*  33:    */   }
/*  34:    */   
/*  35:    */   public <T> T getOption(ChannelOption<T> option)
/*  36:    */   {
/*  37: 56 */     if (option == ChannelOption.SO_RCVBUF) {
/*  38: 57 */       return Integer.valueOf(getReceiveBufferSize());
/*  39:    */     }
/*  40: 59 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  41: 60 */       return Boolean.valueOf(isReuseAddress());
/*  42:    */     }
/*  43: 62 */     if (option == ChannelOption.SO_BACKLOG) {
/*  44: 63 */       return Integer.valueOf(getBacklog());
/*  45:    */     }
/*  46: 65 */     if (option == EpollChannelOption.SO_REUSEPORT) {
/*  47: 66 */       return Boolean.valueOf(isReusePort());
/*  48:    */     }
/*  49: 68 */     return super.getOption(option);
/*  50:    */   }
/*  51:    */   
/*  52:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  53:    */   {
/*  54: 73 */     validate(option, value);
/*  55: 75 */     if (option == ChannelOption.SO_RCVBUF) {
/*  56: 76 */       setReceiveBufferSize(((Integer)value).intValue());
/*  57: 77 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  58: 78 */       setReuseAddress(((Boolean)value).booleanValue());
/*  59: 79 */     } else if (option == ChannelOption.SO_BACKLOG) {
/*  60: 80 */       setBacklog(((Integer)value).intValue());
/*  61: 81 */     } else if (option == EpollChannelOption.SO_REUSEPORT) {
/*  62: 82 */       setReusePort(((Boolean)value).booleanValue());
/*  63:    */     } else {
/*  64: 84 */       return super.setOption(option, value);
/*  65:    */     }
/*  66: 87 */     return true;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public boolean isReuseAddress()
/*  70:    */   {
/*  71: 92 */     return Native.isReuseAddress(this.channel.fd) == 1;
/*  72:    */   }
/*  73:    */   
/*  74:    */   public EpollServerSocketChannelConfig setReuseAddress(boolean reuseAddress)
/*  75:    */   {
/*  76: 97 */     Native.setReuseAddress(this.channel.fd, reuseAddress ? 1 : 0);
/*  77: 98 */     return this;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public int getReceiveBufferSize()
/*  81:    */   {
/*  82:103 */     return Native.getReceiveBufferSize(this.channel.fd);
/*  83:    */   }
/*  84:    */   
/*  85:    */   public EpollServerSocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/*  86:    */   {
/*  87:108 */     Native.setReceiveBufferSize(this.channel.fd, receiveBufferSize);
/*  88:    */     
/*  89:110 */     return this;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public EpollServerSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/*  93:    */   {
/*  94:115 */     return this;
/*  95:    */   }
/*  96:    */   
/*  97:    */   public int getBacklog()
/*  98:    */   {
/*  99:120 */     return this.backlog;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public EpollServerSocketChannelConfig setBacklog(int backlog)
/* 103:    */   {
/* 104:125 */     if (backlog < 0) {
/* 105:126 */       throw new IllegalArgumentException("backlog: " + backlog);
/* 106:    */     }
/* 107:128 */     this.backlog = backlog;
/* 108:129 */     return this;
/* 109:    */   }
/* 110:    */   
/* 111:    */   public EpollServerSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 112:    */   {
/* 113:134 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 114:135 */     return this;
/* 115:    */   }
/* 116:    */   
/* 117:    */   public EpollServerSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 118:    */   {
/* 119:140 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 120:141 */     return this;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public EpollServerSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 124:    */   {
/* 125:146 */     super.setWriteSpinCount(writeSpinCount);
/* 126:147 */     return this;
/* 127:    */   }
/* 128:    */   
/* 129:    */   public EpollServerSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 130:    */   {
/* 131:152 */     super.setAllocator(allocator);
/* 132:153 */     return this;
/* 133:    */   }
/* 134:    */   
/* 135:    */   public EpollServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 136:    */   {
/* 137:158 */     super.setRecvByteBufAllocator(allocator);
/* 138:159 */     return this;
/* 139:    */   }
/* 140:    */   
/* 141:    */   public EpollServerSocketChannelConfig setAutoRead(boolean autoRead)
/* 142:    */   {
/* 143:164 */     super.setAutoRead(autoRead);
/* 144:165 */     return this;
/* 145:    */   }
/* 146:    */   
/* 147:    */   public EpollServerSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 148:    */   {
/* 149:170 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 150:171 */     return this;
/* 151:    */   }
/* 152:    */   
/* 153:    */   public EpollServerSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 154:    */   {
/* 155:176 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 156:177 */     return this;
/* 157:    */   }
/* 158:    */   
/* 159:    */   public EpollServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 160:    */   {
/* 161:182 */     super.setMessageSizeEstimator(estimator);
/* 162:183 */     return this;
/* 163:    */   }
/* 164:    */   
/* 165:    */   public boolean isReusePort()
/* 166:    */   {
/* 167:190 */     return Native.isReusePort(this.channel.fd) == 1;
/* 168:    */   }
/* 169:    */   
/* 170:    */   public EpollServerSocketChannelConfig setReusePort(boolean reusePort)
/* 171:    */   {
/* 172:201 */     Native.setReusePort(this.channel.fd, reusePort ? 1 : 0);
/* 173:202 */     return this;
/* 174:    */   }
/* 175:    */   
/* 176:    */   protected void autoReadCleared()
/* 177:    */   {
/* 178:207 */     this.channel.clearEpollIn();
/* 179:    */   }
/* 180:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.epoll.EpollServerSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */