/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.DefaultChannelConfig;
/*   7:    */ import io.netty.channel.FixedRecvByteBufAllocator;
/*   8:    */ import io.netty.channel.MessageSizeEstimator;
/*   9:    */ import io.netty.channel.RecvByteBufAllocator;
/*  10:    */ import io.netty.channel.socket.DatagramChannelConfig;
/*  11:    */ import java.net.InetAddress;
/*  12:    */ import java.net.NetworkInterface;
/*  13:    */ import java.util.Map;
/*  14:    */ 
/*  15:    */ public final class EpollDatagramChannelConfig
/*  16:    */   extends DefaultChannelConfig
/*  17:    */   implements DatagramChannelConfig
/*  18:    */ {
/*  19: 31 */   private static final RecvByteBufAllocator DEFAULT_RCVBUF_ALLOCATOR = new FixedRecvByteBufAllocator(2048);
/*  20:    */   private final EpollDatagramChannel datagramChannel;
/*  21:    */   private boolean activeOnOpen;
/*  22:    */   
/*  23:    */   EpollDatagramChannelConfig(EpollDatagramChannel channel)
/*  24:    */   {
/*  25: 36 */     super(channel);
/*  26: 37 */     this.datagramChannel = channel;
/*  27: 38 */     setRecvByteBufAllocator(DEFAULT_RCVBUF_ALLOCATOR);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  31:    */   {
/*  32: 43 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_BROADCAST, ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, ChannelOption.SO_REUSEADDR, ChannelOption.IP_MULTICAST_LOOP_DISABLED, ChannelOption.IP_MULTICAST_ADDR, ChannelOption.IP_MULTICAST_IF, ChannelOption.IP_MULTICAST_TTL, ChannelOption.IP_TOS, ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION, EpollChannelOption.SO_REUSEPORT });
/*  33:    */   }
/*  34:    */   
/*  35:    */   public <T> T getOption(ChannelOption<T> option)
/*  36:    */   {
/*  37: 55 */     if (option == ChannelOption.SO_BROADCAST) {
/*  38: 56 */       return Boolean.valueOf(isBroadcast());
/*  39:    */     }
/*  40: 58 */     if (option == ChannelOption.SO_RCVBUF) {
/*  41: 59 */       return Integer.valueOf(getReceiveBufferSize());
/*  42:    */     }
/*  43: 61 */     if (option == ChannelOption.SO_SNDBUF) {
/*  44: 62 */       return Integer.valueOf(getSendBufferSize());
/*  45:    */     }
/*  46: 64 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  47: 65 */       return Boolean.valueOf(isReuseAddress());
/*  48:    */     }
/*  49: 67 */     if (option == ChannelOption.IP_MULTICAST_LOOP_DISABLED) {
/*  50: 68 */       return Boolean.valueOf(isLoopbackModeDisabled());
/*  51:    */     }
/*  52: 70 */     if (option == ChannelOption.IP_MULTICAST_ADDR)
/*  53:    */     {
/*  54: 71 */       T i = getInterface();
/*  55: 72 */       return i;
/*  56:    */     }
/*  57: 74 */     if (option == ChannelOption.IP_MULTICAST_IF)
/*  58:    */     {
/*  59: 75 */       T i = getNetworkInterface();
/*  60: 76 */       return i;
/*  61:    */     }
/*  62: 78 */     if (option == ChannelOption.IP_MULTICAST_TTL) {
/*  63: 79 */       return Integer.valueOf(getTimeToLive());
/*  64:    */     }
/*  65: 81 */     if (option == ChannelOption.IP_TOS) {
/*  66: 82 */       return Integer.valueOf(getTrafficClass());
/*  67:    */     }
/*  68: 84 */     if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) {
/*  69: 85 */       return Boolean.valueOf(this.activeOnOpen);
/*  70:    */     }
/*  71: 87 */     if (option == EpollChannelOption.SO_REUSEPORT) {
/*  72: 88 */       return Boolean.valueOf(isReusePort());
/*  73:    */     }
/*  74: 90 */     return super.getOption(option);
/*  75:    */   }
/*  76:    */   
/*  77:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  78:    */   {
/*  79: 95 */     validate(option, value);
/*  80: 97 */     if (option == ChannelOption.SO_BROADCAST) {
/*  81: 98 */       setBroadcast(((Boolean)value).booleanValue());
/*  82: 99 */     } else if (option == ChannelOption.SO_RCVBUF) {
/*  83:100 */       setReceiveBufferSize(((Integer)value).intValue());
/*  84:101 */     } else if (option == ChannelOption.SO_SNDBUF) {
/*  85:102 */       setSendBufferSize(((Integer)value).intValue());
/*  86:103 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  87:104 */       setReuseAddress(((Boolean)value).booleanValue());
/*  88:105 */     } else if (option == ChannelOption.IP_MULTICAST_LOOP_DISABLED) {
/*  89:106 */       setLoopbackModeDisabled(((Boolean)value).booleanValue());
/*  90:107 */     } else if (option == ChannelOption.IP_MULTICAST_ADDR) {
/*  91:108 */       setInterface((InetAddress)value);
/*  92:109 */     } else if (option == ChannelOption.IP_MULTICAST_IF) {
/*  93:110 */       setNetworkInterface((NetworkInterface)value);
/*  94:111 */     } else if (option == ChannelOption.IP_MULTICAST_TTL) {
/*  95:112 */       setTimeToLive(((Integer)value).intValue());
/*  96:113 */     } else if (option == ChannelOption.IP_TOS) {
/*  97:114 */       setTrafficClass(((Integer)value).intValue());
/*  98:115 */     } else if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) {
/*  99:116 */       setActiveOnOpen(((Boolean)value).booleanValue());
/* 100:117 */     } else if (option == EpollChannelOption.SO_REUSEPORT) {
/* 101:118 */       setReusePort(((Boolean)value).booleanValue());
/* 102:    */     } else {
/* 103:120 */       return super.setOption(option, value);
/* 104:    */     }
/* 105:123 */     return true;
/* 106:    */   }
/* 107:    */   
/* 108:    */   private void setActiveOnOpen(boolean activeOnOpen)
/* 109:    */   {
/* 110:127 */     if (this.channel.isRegistered()) {
/* 111:128 */       throw new IllegalStateException("Can only changed before channel was registered");
/* 112:    */     }
/* 113:130 */     this.activeOnOpen = activeOnOpen;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public EpollDatagramChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 117:    */   {
/* 118:135 */     super.setMessageSizeEstimator(estimator);
/* 119:136 */     return this;
/* 120:    */   }
/* 121:    */   
/* 122:    */   public EpollDatagramChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 123:    */   {
/* 124:141 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 125:142 */     return this;
/* 126:    */   }
/* 127:    */   
/* 128:    */   public EpollDatagramChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 129:    */   {
/* 130:147 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 131:148 */     return this;
/* 132:    */   }
/* 133:    */   
/* 134:    */   public EpollDatagramChannelConfig setAutoClose(boolean autoClose)
/* 135:    */   {
/* 136:153 */     super.setAutoClose(autoClose);
/* 137:154 */     return this;
/* 138:    */   }
/* 139:    */   
/* 140:    */   public EpollDatagramChannelConfig setAutoRead(boolean autoRead)
/* 141:    */   {
/* 142:159 */     super.setAutoRead(autoRead);
/* 143:160 */     return this;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public EpollDatagramChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 147:    */   {
/* 148:165 */     super.setRecvByteBufAllocator(allocator);
/* 149:166 */     return this;
/* 150:    */   }
/* 151:    */   
/* 152:    */   public EpollDatagramChannelConfig setWriteSpinCount(int writeSpinCount)
/* 153:    */   {
/* 154:171 */     super.setWriteSpinCount(writeSpinCount);
/* 155:172 */     return this;
/* 156:    */   }
/* 157:    */   
/* 158:    */   public EpollDatagramChannelConfig setAllocator(ByteBufAllocator allocator)
/* 159:    */   {
/* 160:177 */     super.setAllocator(allocator);
/* 161:178 */     return this;
/* 162:    */   }
/* 163:    */   
/* 164:    */   public EpollDatagramChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 165:    */   {
/* 166:183 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 167:184 */     return this;
/* 168:    */   }
/* 169:    */   
/* 170:    */   public EpollDatagramChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 171:    */   {
/* 172:189 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 173:190 */     return this;
/* 174:    */   }
/* 175:    */   
/* 176:    */   public int getSendBufferSize()
/* 177:    */   {
/* 178:195 */     return Native.getSendBufferSize(this.datagramChannel.fd);
/* 179:    */   }
/* 180:    */   
/* 181:    */   public EpollDatagramChannelConfig setSendBufferSize(int sendBufferSize)
/* 182:    */   {
/* 183:200 */     Native.setSendBufferSize(this.datagramChannel.fd, sendBufferSize);
/* 184:201 */     return this;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public int getReceiveBufferSize()
/* 188:    */   {
/* 189:206 */     return Native.getReceiveBufferSize(this.datagramChannel.fd);
/* 190:    */   }
/* 191:    */   
/* 192:    */   public EpollDatagramChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 193:    */   {
/* 194:211 */     Native.setReceiveBufferSize(this.datagramChannel.fd, receiveBufferSize);
/* 195:212 */     return this;
/* 196:    */   }
/* 197:    */   
/* 198:    */   public int getTrafficClass()
/* 199:    */   {
/* 200:217 */     return Native.getTrafficClass(this.datagramChannel.fd);
/* 201:    */   }
/* 202:    */   
/* 203:    */   public EpollDatagramChannelConfig setTrafficClass(int trafficClass)
/* 204:    */   {
/* 205:222 */     Native.setTrafficClass(this.datagramChannel.fd, trafficClass);
/* 206:223 */     return this;
/* 207:    */   }
/* 208:    */   
/* 209:    */   public boolean isReuseAddress()
/* 210:    */   {
/* 211:228 */     return Native.isReuseAddress(this.datagramChannel.fd) == 1;
/* 212:    */   }
/* 213:    */   
/* 214:    */   public EpollDatagramChannelConfig setReuseAddress(boolean reuseAddress)
/* 215:    */   {
/* 216:233 */     Native.setReuseAddress(this.datagramChannel.fd, reuseAddress ? 1 : 0);
/* 217:234 */     return this;
/* 218:    */   }
/* 219:    */   
/* 220:    */   public boolean isBroadcast()
/* 221:    */   {
/* 222:239 */     return Native.isBroadcast(this.datagramChannel.fd) == 1;
/* 223:    */   }
/* 224:    */   
/* 225:    */   public EpollDatagramChannelConfig setBroadcast(boolean broadcast)
/* 226:    */   {
/* 227:244 */     Native.setBroadcast(this.datagramChannel.fd, broadcast ? 1 : 0);
/* 228:245 */     return this;
/* 229:    */   }
/* 230:    */   
/* 231:    */   public boolean isLoopbackModeDisabled()
/* 232:    */   {
/* 233:250 */     return false;
/* 234:    */   }
/* 235:    */   
/* 236:    */   public DatagramChannelConfig setLoopbackModeDisabled(boolean loopbackModeDisabled)
/* 237:    */   {
/* 238:255 */     throw new UnsupportedOperationException("Multicast not supported");
/* 239:    */   }
/* 240:    */   
/* 241:    */   public int getTimeToLive()
/* 242:    */   {
/* 243:260 */     return -1;
/* 244:    */   }
/* 245:    */   
/* 246:    */   public EpollDatagramChannelConfig setTimeToLive(int ttl)
/* 247:    */   {
/* 248:265 */     throw new UnsupportedOperationException("Multicast not supported");
/* 249:    */   }
/* 250:    */   
/* 251:    */   public InetAddress getInterface()
/* 252:    */   {
/* 253:270 */     return null;
/* 254:    */   }
/* 255:    */   
/* 256:    */   public EpollDatagramChannelConfig setInterface(InetAddress interfaceAddress)
/* 257:    */   {
/* 258:275 */     throw new UnsupportedOperationException("Multicast not supported");
/* 259:    */   }
/* 260:    */   
/* 261:    */   public NetworkInterface getNetworkInterface()
/* 262:    */   {
/* 263:280 */     return null;
/* 264:    */   }
/* 265:    */   
/* 266:    */   public EpollDatagramChannelConfig setNetworkInterface(NetworkInterface networkInterface)
/* 267:    */   {
/* 268:285 */     throw new UnsupportedOperationException("Multicast not supported");
/* 269:    */   }
/* 270:    */   
/* 271:    */   public boolean isReusePort()
/* 272:    */   {
/* 273:292 */     return Native.isReusePort(this.datagramChannel.fd) == 1;
/* 274:    */   }
/* 275:    */   
/* 276:    */   public EpollDatagramChannelConfig setReusePort(boolean reusePort)
/* 277:    */   {
/* 278:303 */     Native.setReusePort(this.datagramChannel.fd, reusePort ? 1 : 0);
/* 279:304 */     return this;
/* 280:    */   }
/* 281:    */   
/* 282:    */   protected void autoReadCleared()
/* 283:    */   {
/* 284:309 */     this.datagramChannel.clearEpollIn();
/* 285:    */   }
/* 286:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.epoll.EpollDatagramChannelConfig
 * JD-Core Version:    0.7.0.1
 */