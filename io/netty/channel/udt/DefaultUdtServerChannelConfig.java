/*   1:    */ package io.netty.channel.udt;
/*   2:    */ 
/*   3:    */ import com.barchart.udt.nio.ChannelUDT;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator;
/*   8:    */ import java.io.IOException;
/*   9:    */ import java.util.Map;
/*  10:    */ 
/*  11:    */ public class DefaultUdtServerChannelConfig
/*  12:    */   extends DefaultUdtChannelConfig
/*  13:    */   implements UdtServerChannelConfig
/*  14:    */ {
/*  15: 35 */   private volatile int backlog = 64;
/*  16:    */   
/*  17:    */   public DefaultUdtServerChannelConfig(UdtChannel channel, ChannelUDT channelUDT, boolean apply)
/*  18:    */     throws IOException
/*  19:    */   {
/*  20: 39 */     super(channel, channelUDT, apply);
/*  21: 40 */     if (apply) {
/*  22: 41 */       apply(channelUDT);
/*  23:    */     }
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected void apply(ChannelUDT channelUDT)
/*  27:    */     throws IOException
/*  28:    */   {}
/*  29:    */   
/*  30:    */   public int getBacklog()
/*  31:    */   {
/*  32: 52 */     return this.backlog;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public <T> T getOption(ChannelOption<T> option)
/*  36:    */   {
/*  37: 58 */     if (option == ChannelOption.SO_BACKLOG) {
/*  38: 59 */       return Integer.valueOf(getBacklog());
/*  39:    */     }
/*  40: 61 */     return super.getOption(option);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  44:    */   {
/*  45: 66 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_BACKLOG });
/*  46:    */   }
/*  47:    */   
/*  48:    */   public UdtServerChannelConfig setBacklog(int backlog)
/*  49:    */   {
/*  50: 71 */     this.backlog = backlog;
/*  51: 72 */     return this;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  55:    */   {
/*  56: 77 */     validate(option, value);
/*  57: 78 */     if (option == ChannelOption.SO_BACKLOG) {
/*  58: 79 */       setBacklog(((Integer)value).intValue());
/*  59:    */     } else {
/*  60: 81 */       return super.setOption(option, value);
/*  61:    */     }
/*  62: 83 */     return true;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public UdtServerChannelConfig setProtocolReceiveBufferSize(int protocolReceiveBuferSize)
/*  66:    */   {
/*  67: 89 */     super.setProtocolReceiveBufferSize(protocolReceiveBuferSize);
/*  68: 90 */     return this;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public UdtServerChannelConfig setProtocolSendBufferSize(int protocolSendBuferSize)
/*  72:    */   {
/*  73: 96 */     super.setProtocolSendBufferSize(protocolSendBuferSize);
/*  74: 97 */     return this;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public UdtServerChannelConfig setReceiveBufferSize(int receiveBufferSize)
/*  78:    */   {
/*  79:103 */     super.setReceiveBufferSize(receiveBufferSize);
/*  80:104 */     return this;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public UdtServerChannelConfig setReuseAddress(boolean reuseAddress)
/*  84:    */   {
/*  85:109 */     super.setReuseAddress(reuseAddress);
/*  86:110 */     return this;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public UdtServerChannelConfig setSendBufferSize(int sendBufferSize)
/*  90:    */   {
/*  91:115 */     super.setSendBufferSize(sendBufferSize);
/*  92:116 */     return this;
/*  93:    */   }
/*  94:    */   
/*  95:    */   public UdtServerChannelConfig setSoLinger(int soLinger)
/*  96:    */   {
/*  97:121 */     super.setSoLinger(soLinger);
/*  98:122 */     return this;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public UdtServerChannelConfig setSystemReceiveBufferSize(int systemSendBuferSize)
/* 102:    */   {
/* 103:128 */     super.setSystemReceiveBufferSize(systemSendBuferSize);
/* 104:129 */     return this;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public UdtServerChannelConfig setSystemSendBufferSize(int systemReceiveBufferSize)
/* 108:    */   {
/* 109:135 */     super.setSystemSendBufferSize(systemReceiveBufferSize);
/* 110:136 */     return this;
/* 111:    */   }
/* 112:    */   
/* 113:    */   public UdtServerChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 114:    */   {
/* 115:141 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 116:142 */     return this;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public UdtServerChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 120:    */   {
/* 121:147 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 122:148 */     return this;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public UdtServerChannelConfig setWriteSpinCount(int writeSpinCount)
/* 126:    */   {
/* 127:153 */     super.setWriteSpinCount(writeSpinCount);
/* 128:154 */     return this;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public UdtServerChannelConfig setAllocator(ByteBufAllocator allocator)
/* 132:    */   {
/* 133:159 */     super.setAllocator(allocator);
/* 134:160 */     return this;
/* 135:    */   }
/* 136:    */   
/* 137:    */   public UdtServerChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 138:    */   {
/* 139:165 */     super.setRecvByteBufAllocator(allocator);
/* 140:166 */     return this;
/* 141:    */   }
/* 142:    */   
/* 143:    */   public UdtServerChannelConfig setAutoRead(boolean autoRead)
/* 144:    */   {
/* 145:171 */     super.setAutoRead(autoRead);
/* 146:172 */     return this;
/* 147:    */   }
/* 148:    */   
/* 149:    */   public UdtServerChannelConfig setAutoClose(boolean autoClose)
/* 150:    */   {
/* 151:177 */     super.setAutoClose(autoClose);
/* 152:178 */     return this;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public UdtServerChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 156:    */   {
/* 157:183 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 158:184 */     return this;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public UdtServerChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 162:    */   {
/* 163:189 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 164:190 */     return this;
/* 165:    */   }
/* 166:    */   
/* 167:    */   public UdtServerChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 168:    */   {
/* 169:195 */     super.setMessageSizeEstimator(estimator);
/* 170:196 */     return this;
/* 171:    */   }
/* 172:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.udt.DefaultUdtServerChannelConfig
 * JD-Core Version:    0.7.0.1
 */