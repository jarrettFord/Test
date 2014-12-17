/*   1:    */ package io.netty.channel.socket.oio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator;
/*   8:    */ import io.netty.channel.socket.DefaultServerSocketChannelConfig;
/*   9:    */ import io.netty.channel.socket.ServerSocketChannel;
/*  10:    */ import java.io.IOException;
/*  11:    */ import java.net.ServerSocket;
/*  12:    */ import java.util.Map;
/*  13:    */ 
/*  14:    */ public class DefaultOioServerSocketChannelConfig
/*  15:    */   extends DefaultServerSocketChannelConfig
/*  16:    */   implements OioServerSocketChannelConfig
/*  17:    */ {
/*  18:    */   @Deprecated
/*  19:    */   public DefaultOioServerSocketChannelConfig(ServerSocketChannel channel, ServerSocket javaSocket)
/*  20:    */   {
/*  21: 40 */     super(channel, javaSocket);
/*  22:    */   }
/*  23:    */   
/*  24:    */   DefaultOioServerSocketChannelConfig(OioServerSocketChannel channel, ServerSocket javaSocket)
/*  25:    */   {
/*  26: 44 */     super(channel, javaSocket);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  30:    */   {
/*  31: 49 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_TIMEOUT });
/*  32:    */   }
/*  33:    */   
/*  34:    */   public <T> T getOption(ChannelOption<T> option)
/*  35:    */   {
/*  36: 56 */     if (option == ChannelOption.SO_TIMEOUT) {
/*  37: 57 */       return Integer.valueOf(getSoTimeout());
/*  38:    */     }
/*  39: 59 */     return super.getOption(option);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  43:    */   {
/*  44: 64 */     validate(option, value);
/*  45: 66 */     if (option == ChannelOption.SO_TIMEOUT) {
/*  46: 67 */       setSoTimeout(((Integer)value).intValue());
/*  47:    */     } else {
/*  48: 69 */       return super.setOption(option, value);
/*  49:    */     }
/*  50: 71 */     return true;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public OioServerSocketChannelConfig setSoTimeout(int timeout)
/*  54:    */   {
/*  55:    */     try
/*  56:    */     {
/*  57: 77 */       this.javaSocket.setSoTimeout(timeout);
/*  58:    */     }
/*  59:    */     catch (IOException e)
/*  60:    */     {
/*  61: 79 */       throw new ChannelException(e);
/*  62:    */     }
/*  63: 81 */     return this;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public int getSoTimeout()
/*  67:    */   {
/*  68:    */     try
/*  69:    */     {
/*  70: 87 */       return this.javaSocket.getSoTimeout();
/*  71:    */     }
/*  72:    */     catch (IOException e)
/*  73:    */     {
/*  74: 89 */       throw new ChannelException(e);
/*  75:    */     }
/*  76:    */   }
/*  77:    */   
/*  78:    */   public OioServerSocketChannelConfig setBacklog(int backlog)
/*  79:    */   {
/*  80: 95 */     super.setBacklog(backlog);
/*  81: 96 */     return this;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public OioServerSocketChannelConfig setReuseAddress(boolean reuseAddress)
/*  85:    */   {
/*  86:101 */     super.setReuseAddress(reuseAddress);
/*  87:102 */     return this;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public OioServerSocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/*  91:    */   {
/*  92:107 */     super.setReceiveBufferSize(receiveBufferSize);
/*  93:108 */     return this;
/*  94:    */   }
/*  95:    */   
/*  96:    */   public OioServerSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/*  97:    */   {
/*  98:113 */     super.setPerformancePreferences(connectionTime, latency, bandwidth);
/*  99:114 */     return this;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public OioServerSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 103:    */   {
/* 104:119 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 105:120 */     return this;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public OioServerSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 109:    */   {
/* 110:125 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 111:126 */     return this;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public OioServerSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 115:    */   {
/* 116:131 */     super.setWriteSpinCount(writeSpinCount);
/* 117:132 */     return this;
/* 118:    */   }
/* 119:    */   
/* 120:    */   public OioServerSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 121:    */   {
/* 122:137 */     super.setAllocator(allocator);
/* 123:138 */     return this;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public OioServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 127:    */   {
/* 128:143 */     super.setRecvByteBufAllocator(allocator);
/* 129:144 */     return this;
/* 130:    */   }
/* 131:    */   
/* 132:    */   public OioServerSocketChannelConfig setAutoRead(boolean autoRead)
/* 133:    */   {
/* 134:149 */     super.setAutoRead(autoRead);
/* 135:150 */     return this;
/* 136:    */   }
/* 137:    */   
/* 138:    */   protected void autoReadCleared()
/* 139:    */   {
/* 140:155 */     if ((this.channel instanceof OioServerSocketChannel)) {
/* 141:156 */       ((OioServerSocketChannel)this.channel).setReadPending(false);
/* 142:    */     }
/* 143:    */   }
/* 144:    */   
/* 145:    */   public OioServerSocketChannelConfig setAutoClose(boolean autoClose)
/* 146:    */   {
/* 147:162 */     super.setAutoClose(autoClose);
/* 148:163 */     return this;
/* 149:    */   }
/* 150:    */   
/* 151:    */   public OioServerSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 152:    */   {
/* 153:168 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 154:169 */     return this;
/* 155:    */   }
/* 156:    */   
/* 157:    */   public OioServerSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 158:    */   {
/* 159:174 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 160:175 */     return this;
/* 161:    */   }
/* 162:    */   
/* 163:    */   public OioServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 164:    */   {
/* 165:180 */     super.setMessageSizeEstimator(estimator);
/* 166:181 */     return this;
/* 167:    */   }
/* 168:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.socket.oio.DefaultOioServerSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */