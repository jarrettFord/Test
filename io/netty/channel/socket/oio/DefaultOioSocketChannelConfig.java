/*   1:    */ package io.netty.channel.socket.oio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator;
/*   8:    */ import io.netty.channel.socket.DefaultSocketChannelConfig;
/*   9:    */ import io.netty.channel.socket.SocketChannel;
/*  10:    */ import java.io.IOException;
/*  11:    */ import java.net.Socket;
/*  12:    */ import java.util.Map;
/*  13:    */ 
/*  14:    */ public class DefaultOioSocketChannelConfig
/*  15:    */   extends DefaultSocketChannelConfig
/*  16:    */   implements OioSocketChannelConfig
/*  17:    */ {
/*  18:    */   @Deprecated
/*  19:    */   public DefaultOioSocketChannelConfig(SocketChannel channel, Socket javaSocket)
/*  20:    */   {
/*  21: 38 */     super(channel, javaSocket);
/*  22:    */   }
/*  23:    */   
/*  24:    */   DefaultOioSocketChannelConfig(OioSocketChannel channel, Socket javaSocket)
/*  25:    */   {
/*  26: 42 */     super(channel, javaSocket);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  30:    */   {
/*  31: 47 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_TIMEOUT });
/*  32:    */   }
/*  33:    */   
/*  34:    */   public <T> T getOption(ChannelOption<T> option)
/*  35:    */   {
/*  36: 54 */     if (option == ChannelOption.SO_TIMEOUT) {
/*  37: 55 */       return Integer.valueOf(getSoTimeout());
/*  38:    */     }
/*  39: 57 */     return super.getOption(option);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  43:    */   {
/*  44: 62 */     validate(option, value);
/*  45: 64 */     if (option == ChannelOption.SO_TIMEOUT) {
/*  46: 65 */       setSoTimeout(((Integer)value).intValue());
/*  47:    */     } else {
/*  48: 67 */       return super.setOption(option, value);
/*  49:    */     }
/*  50: 69 */     return true;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public OioSocketChannelConfig setSoTimeout(int timeout)
/*  54:    */   {
/*  55:    */     try
/*  56:    */     {
/*  57: 75 */       this.javaSocket.setSoTimeout(timeout);
/*  58:    */     }
/*  59:    */     catch (IOException e)
/*  60:    */     {
/*  61: 77 */       throw new ChannelException(e);
/*  62:    */     }
/*  63: 79 */     return this;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public int getSoTimeout()
/*  67:    */   {
/*  68:    */     try
/*  69:    */     {
/*  70: 85 */       return this.javaSocket.getSoTimeout();
/*  71:    */     }
/*  72:    */     catch (IOException e)
/*  73:    */     {
/*  74: 87 */       throw new ChannelException(e);
/*  75:    */     }
/*  76:    */   }
/*  77:    */   
/*  78:    */   public OioSocketChannelConfig setTcpNoDelay(boolean tcpNoDelay)
/*  79:    */   {
/*  80: 93 */     super.setTcpNoDelay(tcpNoDelay);
/*  81: 94 */     return this;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public OioSocketChannelConfig setSoLinger(int soLinger)
/*  85:    */   {
/*  86: 99 */     super.setSoLinger(soLinger);
/*  87:100 */     return this;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public OioSocketChannelConfig setSendBufferSize(int sendBufferSize)
/*  91:    */   {
/*  92:105 */     super.setSendBufferSize(sendBufferSize);
/*  93:106 */     return this;
/*  94:    */   }
/*  95:    */   
/*  96:    */   public OioSocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/*  97:    */   {
/*  98:111 */     super.setReceiveBufferSize(receiveBufferSize);
/*  99:112 */     return this;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public OioSocketChannelConfig setKeepAlive(boolean keepAlive)
/* 103:    */   {
/* 104:117 */     super.setKeepAlive(keepAlive);
/* 105:118 */     return this;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public OioSocketChannelConfig setTrafficClass(int trafficClass)
/* 109:    */   {
/* 110:123 */     super.setTrafficClass(trafficClass);
/* 111:124 */     return this;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public OioSocketChannelConfig setReuseAddress(boolean reuseAddress)
/* 115:    */   {
/* 116:129 */     super.setReuseAddress(reuseAddress);
/* 117:130 */     return this;
/* 118:    */   }
/* 119:    */   
/* 120:    */   public OioSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/* 121:    */   {
/* 122:135 */     super.setPerformancePreferences(connectionTime, latency, bandwidth);
/* 123:136 */     return this;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public OioSocketChannelConfig setAllowHalfClosure(boolean allowHalfClosure)
/* 127:    */   {
/* 128:141 */     super.setAllowHalfClosure(allowHalfClosure);
/* 129:142 */     return this;
/* 130:    */   }
/* 131:    */   
/* 132:    */   public OioSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 133:    */   {
/* 134:147 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 135:148 */     return this;
/* 136:    */   }
/* 137:    */   
/* 138:    */   public OioSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 139:    */   {
/* 140:153 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 141:154 */     return this;
/* 142:    */   }
/* 143:    */   
/* 144:    */   public OioSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 145:    */   {
/* 146:159 */     super.setWriteSpinCount(writeSpinCount);
/* 147:160 */     return this;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public OioSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 151:    */   {
/* 152:165 */     super.setAllocator(allocator);
/* 153:166 */     return this;
/* 154:    */   }
/* 155:    */   
/* 156:    */   public OioSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 157:    */   {
/* 158:171 */     super.setRecvByteBufAllocator(allocator);
/* 159:172 */     return this;
/* 160:    */   }
/* 161:    */   
/* 162:    */   public OioSocketChannelConfig setAutoRead(boolean autoRead)
/* 163:    */   {
/* 164:177 */     super.setAutoRead(autoRead);
/* 165:178 */     return this;
/* 166:    */   }
/* 167:    */   
/* 168:    */   protected void autoReadCleared()
/* 169:    */   {
/* 170:183 */     if ((this.channel instanceof OioSocketChannel)) {
/* 171:184 */       ((OioSocketChannel)this.channel).setReadPending(false);
/* 172:    */     }
/* 173:    */   }
/* 174:    */   
/* 175:    */   public OioSocketChannelConfig setAutoClose(boolean autoClose)
/* 176:    */   {
/* 177:190 */     super.setAutoClose(autoClose);
/* 178:191 */     return this;
/* 179:    */   }
/* 180:    */   
/* 181:    */   public OioSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 182:    */   {
/* 183:196 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 184:197 */     return this;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public OioSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 188:    */   {
/* 189:202 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 190:203 */     return this;
/* 191:    */   }
/* 192:    */   
/* 193:    */   public OioSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 194:    */   {
/* 195:208 */     super.setMessageSizeEstimator(estimator);
/* 196:209 */     return this;
/* 197:    */   }
/* 198:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.socket.oio.DefaultOioSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */