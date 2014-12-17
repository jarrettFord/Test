/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelOption;
/*   5:    */ import io.netty.channel.DefaultChannelConfig;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator;
/*   8:    */ import io.netty.channel.socket.SocketChannelConfig;
/*   9:    */ import io.netty.util.internal.PlatformDependent;
/*  10:    */ import java.util.Map;
/*  11:    */ 
/*  12:    */ public final class EpollSocketChannelConfig
/*  13:    */   extends DefaultChannelConfig
/*  14:    */   implements SocketChannelConfig
/*  15:    */ {
/*  16:    */   protected final EpollSocketChannel channel;
/*  17:    */   private volatile boolean allowHalfClosure;
/*  18:    */   
/*  19:    */   EpollSocketChannelConfig(EpollSocketChannel channel)
/*  20:    */   {
/*  21: 40 */     super(channel);
/*  22:    */     
/*  23: 42 */     this.channel = channel;
/*  24: 43 */     if (PlatformDependent.canEnableTcpNoDelayByDefault()) {
/*  25: 44 */       setTcpNoDelay(true);
/*  26:    */     }
/*  27:    */   }
/*  28:    */   
/*  29:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  30:    */   {
/*  31: 50 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, ChannelOption.TCP_NODELAY, ChannelOption.SO_KEEPALIVE, ChannelOption.SO_REUSEADDR, ChannelOption.SO_LINGER, ChannelOption.IP_TOS, ChannelOption.ALLOW_HALF_CLOSURE, EpollChannelOption.TCP_CORK, EpollChannelOption.TCP_KEEPCNT, EpollChannelOption.TCP_KEEPIDLE, EpollChannelOption.TCP_KEEPINTVL });
/*  32:    */   }
/*  33:    */   
/*  34:    */   public <T> T getOption(ChannelOption<T> option)
/*  35:    */   {
/*  36: 60 */     if (option == ChannelOption.SO_RCVBUF) {
/*  37: 61 */       return Integer.valueOf(getReceiveBufferSize());
/*  38:    */     }
/*  39: 63 */     if (option == ChannelOption.SO_SNDBUF) {
/*  40: 64 */       return Integer.valueOf(getSendBufferSize());
/*  41:    */     }
/*  42: 66 */     if (option == ChannelOption.TCP_NODELAY) {
/*  43: 67 */       return Boolean.valueOf(isTcpNoDelay());
/*  44:    */     }
/*  45: 69 */     if (option == ChannelOption.SO_KEEPALIVE) {
/*  46: 70 */       return Boolean.valueOf(isKeepAlive());
/*  47:    */     }
/*  48: 72 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  49: 73 */       return Boolean.valueOf(isReuseAddress());
/*  50:    */     }
/*  51: 75 */     if (option == ChannelOption.SO_LINGER) {
/*  52: 76 */       return Integer.valueOf(getSoLinger());
/*  53:    */     }
/*  54: 78 */     if (option == ChannelOption.IP_TOS) {
/*  55: 79 */       return Integer.valueOf(getTrafficClass());
/*  56:    */     }
/*  57: 81 */     if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
/*  58: 82 */       return Boolean.valueOf(isAllowHalfClosure());
/*  59:    */     }
/*  60: 84 */     if (option == EpollChannelOption.TCP_CORK) {
/*  61: 85 */       return Boolean.valueOf(isTcpCork());
/*  62:    */     }
/*  63: 87 */     if (option == EpollChannelOption.TCP_KEEPIDLE) {
/*  64: 88 */       return Integer.valueOf(getTcpKeepIdle());
/*  65:    */     }
/*  66: 90 */     if (option == EpollChannelOption.TCP_KEEPINTVL) {
/*  67: 91 */       return Integer.valueOf(getTcpKeepIntvl());
/*  68:    */     }
/*  69: 93 */     if (option == EpollChannelOption.TCP_KEEPCNT) {
/*  70: 94 */       return Integer.valueOf(getTcpKeepCnt());
/*  71:    */     }
/*  72: 96 */     return super.getOption(option);
/*  73:    */   }
/*  74:    */   
/*  75:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  76:    */   {
/*  77:101 */     validate(option, value);
/*  78:103 */     if (option == ChannelOption.SO_RCVBUF) {
/*  79:104 */       setReceiveBufferSize(((Integer)value).intValue());
/*  80:105 */     } else if (option == ChannelOption.SO_SNDBUF) {
/*  81:106 */       setSendBufferSize(((Integer)value).intValue());
/*  82:107 */     } else if (option == ChannelOption.TCP_NODELAY) {
/*  83:108 */       setTcpNoDelay(((Boolean)value).booleanValue());
/*  84:109 */     } else if (option == ChannelOption.SO_KEEPALIVE) {
/*  85:110 */       setKeepAlive(((Boolean)value).booleanValue());
/*  86:111 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  87:112 */       setReuseAddress(((Boolean)value).booleanValue());
/*  88:113 */     } else if (option == ChannelOption.SO_LINGER) {
/*  89:114 */       setSoLinger(((Integer)value).intValue());
/*  90:115 */     } else if (option == ChannelOption.IP_TOS) {
/*  91:116 */       setTrafficClass(((Integer)value).intValue());
/*  92:117 */     } else if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
/*  93:118 */       setAllowHalfClosure(((Boolean)value).booleanValue());
/*  94:119 */     } else if (option == EpollChannelOption.TCP_CORK) {
/*  95:120 */       setTcpCork(((Boolean)value).booleanValue());
/*  96:121 */     } else if (option == EpollChannelOption.TCP_KEEPIDLE) {
/*  97:122 */       setTcpKeepIdle(((Integer)value).intValue());
/*  98:123 */     } else if (option == EpollChannelOption.TCP_KEEPCNT) {
/*  99:124 */       setTcpKeepCntl(((Integer)value).intValue());
/* 100:125 */     } else if (option == EpollChannelOption.TCP_KEEPINTVL) {
/* 101:126 */       setTcpKeepIntvl(((Integer)value).intValue());
/* 102:    */     } else {
/* 103:128 */       return super.setOption(option, value);
/* 104:    */     }
/* 105:131 */     return true;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public int getReceiveBufferSize()
/* 109:    */   {
/* 110:136 */     return Native.getReceiveBufferSize(this.channel.fd);
/* 111:    */   }
/* 112:    */   
/* 113:    */   public int getSendBufferSize()
/* 114:    */   {
/* 115:141 */     return Native.getSendBufferSize(this.channel.fd);
/* 116:    */   }
/* 117:    */   
/* 118:    */   public int getSoLinger()
/* 119:    */   {
/* 120:146 */     return Native.getSoLinger(this.channel.fd);
/* 121:    */   }
/* 122:    */   
/* 123:    */   public int getTrafficClass()
/* 124:    */   {
/* 125:151 */     return Native.getTrafficClass(this.channel.fd);
/* 126:    */   }
/* 127:    */   
/* 128:    */   public boolean isKeepAlive()
/* 129:    */   {
/* 130:156 */     return Native.isKeepAlive(this.channel.fd) == 1;
/* 131:    */   }
/* 132:    */   
/* 133:    */   public boolean isReuseAddress()
/* 134:    */   {
/* 135:161 */     return Native.isReuseAddress(this.channel.fd) == 1;
/* 136:    */   }
/* 137:    */   
/* 138:    */   public boolean isTcpNoDelay()
/* 139:    */   {
/* 140:166 */     return Native.isTcpNoDelay(this.channel.fd) == 1;
/* 141:    */   }
/* 142:    */   
/* 143:    */   public boolean isTcpCork()
/* 144:    */   {
/* 145:173 */     return Native.isTcpCork(this.channel.fd) == 1;
/* 146:    */   }
/* 147:    */   
/* 148:    */   public int getTcpKeepIdle()
/* 149:    */   {
/* 150:180 */     return Native.getTcpKeepIdle(this.channel.fd);
/* 151:    */   }
/* 152:    */   
/* 153:    */   public int getTcpKeepIntvl()
/* 154:    */   {
/* 155:187 */     return Native.getTcpKeepIntvl(this.channel.fd);
/* 156:    */   }
/* 157:    */   
/* 158:    */   public int getTcpKeepCnt()
/* 159:    */   {
/* 160:194 */     return Native.getTcpKeepCnt(this.channel.fd);
/* 161:    */   }
/* 162:    */   
/* 163:    */   public EpollSocketChannelConfig setKeepAlive(boolean keepAlive)
/* 164:    */   {
/* 165:199 */     Native.setKeepAlive(this.channel.fd, keepAlive ? 1 : 0);
/* 166:200 */     return this;
/* 167:    */   }
/* 168:    */   
/* 169:    */   public EpollSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/* 170:    */   {
/* 171:206 */     return this;
/* 172:    */   }
/* 173:    */   
/* 174:    */   public EpollSocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 175:    */   {
/* 176:211 */     Native.setReceiveBufferSize(this.channel.fd, receiveBufferSize);
/* 177:212 */     return this;
/* 178:    */   }
/* 179:    */   
/* 180:    */   public EpollSocketChannelConfig setReuseAddress(boolean reuseAddress)
/* 181:    */   {
/* 182:217 */     Native.setReuseAddress(this.channel.fd, reuseAddress ? 1 : 0);
/* 183:218 */     return this;
/* 184:    */   }
/* 185:    */   
/* 186:    */   public EpollSocketChannelConfig setSendBufferSize(int sendBufferSize)
/* 187:    */   {
/* 188:223 */     Native.setSendBufferSize(this.channel.fd, sendBufferSize);
/* 189:224 */     return this;
/* 190:    */   }
/* 191:    */   
/* 192:    */   public EpollSocketChannelConfig setSoLinger(int soLinger)
/* 193:    */   {
/* 194:229 */     Native.setSoLinger(this.channel.fd, soLinger);
/* 195:230 */     return this;
/* 196:    */   }
/* 197:    */   
/* 198:    */   public EpollSocketChannelConfig setTcpNoDelay(boolean tcpNoDelay)
/* 199:    */   {
/* 200:235 */     Native.setTcpNoDelay(this.channel.fd, tcpNoDelay ? 1 : 0);
/* 201:236 */     return this;
/* 202:    */   }
/* 203:    */   
/* 204:    */   public EpollSocketChannelConfig setTcpCork(boolean tcpCork)
/* 205:    */   {
/* 206:243 */     Native.setTcpCork(this.channel.fd, tcpCork ? 1 : 0);
/* 207:244 */     return this;
/* 208:    */   }
/* 209:    */   
/* 210:    */   public EpollSocketChannelConfig setTrafficClass(int trafficClass)
/* 211:    */   {
/* 212:249 */     Native.setTrafficClass(this.channel.fd, trafficClass);
/* 213:250 */     return this;
/* 214:    */   }
/* 215:    */   
/* 216:    */   public EpollSocketChannelConfig setTcpKeepIdle(int seconds)
/* 217:    */   {
/* 218:257 */     Native.setTcpKeepIdle(this.channel.fd, seconds);
/* 219:258 */     return this;
/* 220:    */   }
/* 221:    */   
/* 222:    */   public EpollSocketChannelConfig setTcpKeepIntvl(int seconds)
/* 223:    */   {
/* 224:265 */     Native.setTcpKeepIntvl(this.channel.fd, seconds);
/* 225:266 */     return this;
/* 226:    */   }
/* 227:    */   
/* 228:    */   public EpollSocketChannelConfig setTcpKeepCntl(int probes)
/* 229:    */   {
/* 230:273 */     Native.setTcpKeepCnt(this.channel.fd, probes);
/* 231:274 */     return this;
/* 232:    */   }
/* 233:    */   
/* 234:    */   public boolean isAllowHalfClosure()
/* 235:    */   {
/* 236:279 */     return this.allowHalfClosure;
/* 237:    */   }
/* 238:    */   
/* 239:    */   public EpollSocketChannelConfig setAllowHalfClosure(boolean allowHalfClosure)
/* 240:    */   {
/* 241:284 */     this.allowHalfClosure = allowHalfClosure;
/* 242:285 */     return this;
/* 243:    */   }
/* 244:    */   
/* 245:    */   public EpollSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 246:    */   {
/* 247:290 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 248:291 */     return this;
/* 249:    */   }
/* 250:    */   
/* 251:    */   public EpollSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 252:    */   {
/* 253:296 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 254:297 */     return this;
/* 255:    */   }
/* 256:    */   
/* 257:    */   public EpollSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 258:    */   {
/* 259:302 */     super.setWriteSpinCount(writeSpinCount);
/* 260:303 */     return this;
/* 261:    */   }
/* 262:    */   
/* 263:    */   public EpollSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 264:    */   {
/* 265:308 */     super.setAllocator(allocator);
/* 266:309 */     return this;
/* 267:    */   }
/* 268:    */   
/* 269:    */   public EpollSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 270:    */   {
/* 271:314 */     super.setRecvByteBufAllocator(allocator);
/* 272:315 */     return this;
/* 273:    */   }
/* 274:    */   
/* 275:    */   public EpollSocketChannelConfig setAutoRead(boolean autoRead)
/* 276:    */   {
/* 277:320 */     super.setAutoRead(autoRead);
/* 278:321 */     return this;
/* 279:    */   }
/* 280:    */   
/* 281:    */   public EpollSocketChannelConfig setAutoClose(boolean autoClose)
/* 282:    */   {
/* 283:326 */     super.setAutoClose(autoClose);
/* 284:327 */     return this;
/* 285:    */   }
/* 286:    */   
/* 287:    */   public EpollSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 288:    */   {
/* 289:332 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 290:333 */     return this;
/* 291:    */   }
/* 292:    */   
/* 293:    */   public EpollSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 294:    */   {
/* 295:338 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 296:339 */     return this;
/* 297:    */   }
/* 298:    */   
/* 299:    */   public EpollSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 300:    */   {
/* 301:344 */     super.setMessageSizeEstimator(estimator);
/* 302:345 */     return this;
/* 303:    */   }
/* 304:    */   
/* 305:    */   protected void autoReadCleared()
/* 306:    */   {
/* 307:350 */     this.channel.clearEpollIn();
/* 308:    */   }
/* 309:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.epoll.EpollSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */