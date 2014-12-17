/*   1:    */ package io.netty.channel.udt;
/*   2:    */ 
/*   3:    */ import com.barchart.udt.OptionUDT;
/*   4:    */ import com.barchart.udt.SocketUDT;
/*   5:    */ import com.barchart.udt.nio.ChannelUDT;
/*   6:    */ import io.netty.buffer.ByteBufAllocator;
/*   7:    */ import io.netty.channel.ChannelOption;
/*   8:    */ import io.netty.channel.DefaultChannelConfig;
/*   9:    */ import io.netty.channel.MessageSizeEstimator;
/*  10:    */ import io.netty.channel.RecvByteBufAllocator;
/*  11:    */ import java.io.IOException;
/*  12:    */ import java.util.Map;
/*  13:    */ 
/*  14:    */ public class DefaultUdtChannelConfig
/*  15:    */   extends DefaultChannelConfig
/*  16:    */   implements UdtChannelConfig
/*  17:    */ {
/*  18:    */   private static final int K = 1024;
/*  19:    */   private static final int M = 1048576;
/*  20: 41 */   private volatile int protocolReceiveBuferSize = 10485760;
/*  21: 42 */   private volatile int protocolSendBuferSize = 10485760;
/*  22: 44 */   private volatile int systemReceiveBufferSize = 1048576;
/*  23: 45 */   private volatile int systemSendBuferSize = 1048576;
/*  24: 47 */   private volatile int allocatorReceiveBufferSize = 131072;
/*  25: 48 */   private volatile int allocatorSendBufferSize = 131072;
/*  26:    */   private volatile int soLinger;
/*  27: 52 */   private volatile boolean reuseAddress = true;
/*  28:    */   
/*  29:    */   public DefaultUdtChannelConfig(UdtChannel channel, ChannelUDT channelUDT, boolean apply)
/*  30:    */     throws IOException
/*  31:    */   {
/*  32: 57 */     super(channel);
/*  33: 58 */     if (apply) {
/*  34: 59 */       apply(channelUDT);
/*  35:    */     }
/*  36:    */   }
/*  37:    */   
/*  38:    */   protected void apply(ChannelUDT channelUDT)
/*  39:    */     throws IOException
/*  40:    */   {
/*  41: 64 */     SocketUDT socketUDT = channelUDT.socketUDT();
/*  42: 65 */     socketUDT.setReuseAddress(isReuseAddress());
/*  43: 66 */     socketUDT.setSendBufferSize(getSendBufferSize());
/*  44: 67 */     if (getSoLinger() <= 0) {
/*  45: 68 */       socketUDT.setSoLinger(false, 0);
/*  46:    */     } else {
/*  47: 70 */       socketUDT.setSoLinger(true, getSoLinger());
/*  48:    */     }
/*  49: 72 */     socketUDT.setOption(OptionUDT.Protocol_Receive_Buffer_Size, Integer.valueOf(getProtocolReceiveBufferSize()));
/*  50:    */     
/*  51: 74 */     socketUDT.setOption(OptionUDT.Protocol_Send_Buffer_Size, Integer.valueOf(getProtocolSendBufferSize()));
/*  52:    */     
/*  53: 76 */     socketUDT.setOption(OptionUDT.System_Receive_Buffer_Size, Integer.valueOf(getSystemReceiveBufferSize()));
/*  54:    */     
/*  55: 78 */     socketUDT.setOption(OptionUDT.System_Send_Buffer_Size, Integer.valueOf(getSystemSendBufferSize()));
/*  56:    */   }
/*  57:    */   
/*  58:    */   public int getProtocolReceiveBufferSize()
/*  59:    */   {
/*  60: 84 */     return this.protocolReceiveBuferSize;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public <T> T getOption(ChannelOption<T> option)
/*  64:    */   {
/*  65: 90 */     if (option == UdtChannelOption.PROTOCOL_RECEIVE_BUFFER_SIZE) {
/*  66: 91 */       return Integer.valueOf(getProtocolReceiveBufferSize());
/*  67:    */     }
/*  68: 93 */     if (option == UdtChannelOption.PROTOCOL_SEND_BUFFER_SIZE) {
/*  69: 94 */       return Integer.valueOf(getProtocolSendBufferSize());
/*  70:    */     }
/*  71: 96 */     if (option == UdtChannelOption.SYSTEM_RECEIVE_BUFFER_SIZE) {
/*  72: 97 */       return Integer.valueOf(getSystemReceiveBufferSize());
/*  73:    */     }
/*  74: 99 */     if (option == UdtChannelOption.SYSTEM_SEND_BUFFER_SIZE) {
/*  75:100 */       return Integer.valueOf(getSystemSendBufferSize());
/*  76:    */     }
/*  77:102 */     if (option == UdtChannelOption.SO_RCVBUF) {
/*  78:103 */       return Integer.valueOf(getReceiveBufferSize());
/*  79:    */     }
/*  80:105 */     if (option == UdtChannelOption.SO_SNDBUF) {
/*  81:106 */       return Integer.valueOf(getSendBufferSize());
/*  82:    */     }
/*  83:108 */     if (option == UdtChannelOption.SO_REUSEADDR) {
/*  84:109 */       return Boolean.valueOf(isReuseAddress());
/*  85:    */     }
/*  86:111 */     if (option == UdtChannelOption.SO_LINGER) {
/*  87:112 */       return Integer.valueOf(getSoLinger());
/*  88:    */     }
/*  89:114 */     return super.getOption(option);
/*  90:    */   }
/*  91:    */   
/*  92:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  93:    */   {
/*  94:119 */     return getOptions(super.getOptions(), new ChannelOption[] { UdtChannelOption.PROTOCOL_RECEIVE_BUFFER_SIZE, UdtChannelOption.PROTOCOL_SEND_BUFFER_SIZE, UdtChannelOption.SYSTEM_RECEIVE_BUFFER_SIZE, UdtChannelOption.SYSTEM_SEND_BUFFER_SIZE, UdtChannelOption.SO_RCVBUF, UdtChannelOption.SO_SNDBUF, UdtChannelOption.SO_REUSEADDR, UdtChannelOption.SO_LINGER });
/*  95:    */   }
/*  96:    */   
/*  97:    */   public int getReceiveBufferSize()
/*  98:    */   {
/*  99:127 */     return this.allocatorReceiveBufferSize;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public int getSendBufferSize()
/* 103:    */   {
/* 104:132 */     return this.allocatorSendBufferSize;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public int getSoLinger()
/* 108:    */   {
/* 109:137 */     return this.soLinger;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public boolean isReuseAddress()
/* 113:    */   {
/* 114:142 */     return this.reuseAddress;
/* 115:    */   }
/* 116:    */   
/* 117:    */   public UdtChannelConfig setProtocolReceiveBufferSize(int protocolReceiveBuferSize)
/* 118:    */   {
/* 119:147 */     this.protocolReceiveBuferSize = protocolReceiveBuferSize;
/* 120:148 */     return this;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/* 124:    */   {
/* 125:153 */     validate(option, value);
/* 126:154 */     if (option == UdtChannelOption.PROTOCOL_RECEIVE_BUFFER_SIZE) {
/* 127:155 */       setProtocolReceiveBufferSize(((Integer)value).intValue());
/* 128:156 */     } else if (option == UdtChannelOption.PROTOCOL_SEND_BUFFER_SIZE) {
/* 129:157 */       setProtocolSendBufferSize(((Integer)value).intValue());
/* 130:158 */     } else if (option == UdtChannelOption.SYSTEM_RECEIVE_BUFFER_SIZE) {
/* 131:159 */       setSystemReceiveBufferSize(((Integer)value).intValue());
/* 132:160 */     } else if (option == UdtChannelOption.SYSTEM_SEND_BUFFER_SIZE) {
/* 133:161 */       setSystemSendBufferSize(((Integer)value).intValue());
/* 134:162 */     } else if (option == UdtChannelOption.SO_RCVBUF) {
/* 135:163 */       setReceiveBufferSize(((Integer)value).intValue());
/* 136:164 */     } else if (option == UdtChannelOption.SO_SNDBUF) {
/* 137:165 */       setSendBufferSize(((Integer)value).intValue());
/* 138:166 */     } else if (option == UdtChannelOption.SO_REUSEADDR) {
/* 139:167 */       setReuseAddress(((Boolean)value).booleanValue());
/* 140:168 */     } else if (option == UdtChannelOption.SO_LINGER) {
/* 141:169 */       setSoLinger(((Integer)value).intValue());
/* 142:    */     } else {
/* 143:171 */       return super.setOption(option, value);
/* 144:    */     }
/* 145:173 */     return true;
/* 146:    */   }
/* 147:    */   
/* 148:    */   public UdtChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 149:    */   {
/* 150:178 */     this.allocatorReceiveBufferSize = receiveBufferSize;
/* 151:179 */     return this;
/* 152:    */   }
/* 153:    */   
/* 154:    */   public UdtChannelConfig setReuseAddress(boolean reuseAddress)
/* 155:    */   {
/* 156:184 */     this.reuseAddress = reuseAddress;
/* 157:185 */     return this;
/* 158:    */   }
/* 159:    */   
/* 160:    */   public UdtChannelConfig setSendBufferSize(int sendBufferSize)
/* 161:    */   {
/* 162:190 */     this.allocatorSendBufferSize = sendBufferSize;
/* 163:191 */     return this;
/* 164:    */   }
/* 165:    */   
/* 166:    */   public UdtChannelConfig setSoLinger(int soLinger)
/* 167:    */   {
/* 168:196 */     this.soLinger = soLinger;
/* 169:197 */     return this;
/* 170:    */   }
/* 171:    */   
/* 172:    */   public int getSystemReceiveBufferSize()
/* 173:    */   {
/* 174:202 */     return this.systemReceiveBufferSize;
/* 175:    */   }
/* 176:    */   
/* 177:    */   public UdtChannelConfig setSystemSendBufferSize(int systemReceiveBufferSize)
/* 178:    */   {
/* 179:208 */     this.systemReceiveBufferSize = systemReceiveBufferSize;
/* 180:209 */     return this;
/* 181:    */   }
/* 182:    */   
/* 183:    */   public int getProtocolSendBufferSize()
/* 184:    */   {
/* 185:214 */     return this.protocolSendBuferSize;
/* 186:    */   }
/* 187:    */   
/* 188:    */   public UdtChannelConfig setProtocolSendBufferSize(int protocolSendBuferSize)
/* 189:    */   {
/* 190:220 */     this.protocolSendBuferSize = protocolSendBuferSize;
/* 191:221 */     return this;
/* 192:    */   }
/* 193:    */   
/* 194:    */   public UdtChannelConfig setSystemReceiveBufferSize(int systemSendBuferSize)
/* 195:    */   {
/* 196:227 */     this.systemSendBuferSize = systemSendBuferSize;
/* 197:228 */     return this;
/* 198:    */   }
/* 199:    */   
/* 200:    */   public int getSystemSendBufferSize()
/* 201:    */   {
/* 202:233 */     return this.systemSendBuferSize;
/* 203:    */   }
/* 204:    */   
/* 205:    */   public UdtChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 206:    */   {
/* 207:238 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 208:239 */     return this;
/* 209:    */   }
/* 210:    */   
/* 211:    */   public UdtChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 212:    */   {
/* 213:244 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 214:245 */     return this;
/* 215:    */   }
/* 216:    */   
/* 217:    */   public UdtChannelConfig setWriteSpinCount(int writeSpinCount)
/* 218:    */   {
/* 219:250 */     super.setWriteSpinCount(writeSpinCount);
/* 220:251 */     return this;
/* 221:    */   }
/* 222:    */   
/* 223:    */   public UdtChannelConfig setAllocator(ByteBufAllocator allocator)
/* 224:    */   {
/* 225:256 */     super.setAllocator(allocator);
/* 226:257 */     return this;
/* 227:    */   }
/* 228:    */   
/* 229:    */   public UdtChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 230:    */   {
/* 231:262 */     super.setRecvByteBufAllocator(allocator);
/* 232:263 */     return this;
/* 233:    */   }
/* 234:    */   
/* 235:    */   public UdtChannelConfig setAutoRead(boolean autoRead)
/* 236:    */   {
/* 237:268 */     super.setAutoRead(autoRead);
/* 238:269 */     return this;
/* 239:    */   }
/* 240:    */   
/* 241:    */   public UdtChannelConfig setAutoClose(boolean autoClose)
/* 242:    */   {
/* 243:274 */     super.setAutoClose(autoClose);
/* 244:275 */     return this;
/* 245:    */   }
/* 246:    */   
/* 247:    */   public UdtChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 248:    */   {
/* 249:280 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 250:281 */     return this;
/* 251:    */   }
/* 252:    */   
/* 253:    */   public UdtChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 254:    */   {
/* 255:286 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 256:287 */     return this;
/* 257:    */   }
/* 258:    */   
/* 259:    */   public UdtChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 260:    */   {
/* 261:292 */     super.setMessageSizeEstimator(estimator);
/* 262:293 */     return this;
/* 263:    */   }
/* 264:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.udt.DefaultUdtChannelConfig
 * JD-Core Version:    0.7.0.1
 */