/*   1:    */ package io.netty.channel.socket;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.DefaultChannelConfig;
/*   7:    */ import io.netty.channel.MessageSizeEstimator;
/*   8:    */ import io.netty.channel.RecvByteBufAllocator;
/*   9:    */ import io.netty.util.internal.PlatformDependent;
/*  10:    */ import java.net.Socket;
/*  11:    */ import java.net.SocketException;
/*  12:    */ import java.util.Map;
/*  13:    */ 
/*  14:    */ public class DefaultSocketChannelConfig
/*  15:    */   extends DefaultChannelConfig
/*  16:    */   implements SocketChannelConfig
/*  17:    */ {
/*  18:    */   protected final Socket javaSocket;
/*  19:    */   private volatile boolean allowHalfClosure;
/*  20:    */   
/*  21:    */   public DefaultSocketChannelConfig(SocketChannel channel, Socket javaSocket)
/*  22:    */   {
/*  23: 45 */     super(channel);
/*  24: 46 */     if (javaSocket == null) {
/*  25: 47 */       throw new NullPointerException("javaSocket");
/*  26:    */     }
/*  27: 49 */     this.javaSocket = javaSocket;
/*  28: 52 */     if (PlatformDependent.canEnableTcpNoDelayByDefault()) {
/*  29:    */       try
/*  30:    */       {
/*  31: 54 */         setTcpNoDelay(true);
/*  32:    */       }
/*  33:    */       catch (Exception e) {}
/*  34:    */     }
/*  35:    */   }
/*  36:    */   
/*  37:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  38:    */   {
/*  39: 63 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, ChannelOption.TCP_NODELAY, ChannelOption.SO_KEEPALIVE, ChannelOption.SO_REUSEADDR, ChannelOption.SO_LINGER, ChannelOption.IP_TOS, ChannelOption.ALLOW_HALF_CLOSURE });
/*  40:    */   }
/*  41:    */   
/*  42:    */   public <T> T getOption(ChannelOption<T> option)
/*  43:    */   {
/*  44: 72 */     if (option == ChannelOption.SO_RCVBUF) {
/*  45: 73 */       return Integer.valueOf(getReceiveBufferSize());
/*  46:    */     }
/*  47: 75 */     if (option == ChannelOption.SO_SNDBUF) {
/*  48: 76 */       return Integer.valueOf(getSendBufferSize());
/*  49:    */     }
/*  50: 78 */     if (option == ChannelOption.TCP_NODELAY) {
/*  51: 79 */       return Boolean.valueOf(isTcpNoDelay());
/*  52:    */     }
/*  53: 81 */     if (option == ChannelOption.SO_KEEPALIVE) {
/*  54: 82 */       return Boolean.valueOf(isKeepAlive());
/*  55:    */     }
/*  56: 84 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  57: 85 */       return Boolean.valueOf(isReuseAddress());
/*  58:    */     }
/*  59: 87 */     if (option == ChannelOption.SO_LINGER) {
/*  60: 88 */       return Integer.valueOf(getSoLinger());
/*  61:    */     }
/*  62: 90 */     if (option == ChannelOption.IP_TOS) {
/*  63: 91 */       return Integer.valueOf(getTrafficClass());
/*  64:    */     }
/*  65: 93 */     if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
/*  66: 94 */       return Boolean.valueOf(isAllowHalfClosure());
/*  67:    */     }
/*  68: 97 */     return super.getOption(option);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  72:    */   {
/*  73:102 */     validate(option, value);
/*  74:104 */     if (option == ChannelOption.SO_RCVBUF) {
/*  75:105 */       setReceiveBufferSize(((Integer)value).intValue());
/*  76:106 */     } else if (option == ChannelOption.SO_SNDBUF) {
/*  77:107 */       setSendBufferSize(((Integer)value).intValue());
/*  78:108 */     } else if (option == ChannelOption.TCP_NODELAY) {
/*  79:109 */       setTcpNoDelay(((Boolean)value).booleanValue());
/*  80:110 */     } else if (option == ChannelOption.SO_KEEPALIVE) {
/*  81:111 */       setKeepAlive(((Boolean)value).booleanValue());
/*  82:112 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  83:113 */       setReuseAddress(((Boolean)value).booleanValue());
/*  84:114 */     } else if (option == ChannelOption.SO_LINGER) {
/*  85:115 */       setSoLinger(((Integer)value).intValue());
/*  86:116 */     } else if (option == ChannelOption.IP_TOS) {
/*  87:117 */       setTrafficClass(((Integer)value).intValue());
/*  88:118 */     } else if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
/*  89:119 */       setAllowHalfClosure(((Boolean)value).booleanValue());
/*  90:    */     } else {
/*  91:121 */       return super.setOption(option, value);
/*  92:    */     }
/*  93:124 */     return true;
/*  94:    */   }
/*  95:    */   
/*  96:    */   public int getReceiveBufferSize()
/*  97:    */   {
/*  98:    */     try
/*  99:    */     {
/* 100:130 */       return this.javaSocket.getReceiveBufferSize();
/* 101:    */     }
/* 102:    */     catch (SocketException e)
/* 103:    */     {
/* 104:132 */       throw new ChannelException(e);
/* 105:    */     }
/* 106:    */   }
/* 107:    */   
/* 108:    */   public int getSendBufferSize()
/* 109:    */   {
/* 110:    */     try
/* 111:    */     {
/* 112:139 */       return this.javaSocket.getSendBufferSize();
/* 113:    */     }
/* 114:    */     catch (SocketException e)
/* 115:    */     {
/* 116:141 */       throw new ChannelException(e);
/* 117:    */     }
/* 118:    */   }
/* 119:    */   
/* 120:    */   public int getSoLinger()
/* 121:    */   {
/* 122:    */     try
/* 123:    */     {
/* 124:148 */       return this.javaSocket.getSoLinger();
/* 125:    */     }
/* 126:    */     catch (SocketException e)
/* 127:    */     {
/* 128:150 */       throw new ChannelException(e);
/* 129:    */     }
/* 130:    */   }
/* 131:    */   
/* 132:    */   public int getTrafficClass()
/* 133:    */   {
/* 134:    */     try
/* 135:    */     {
/* 136:157 */       return this.javaSocket.getTrafficClass();
/* 137:    */     }
/* 138:    */     catch (SocketException e)
/* 139:    */     {
/* 140:159 */       throw new ChannelException(e);
/* 141:    */     }
/* 142:    */   }
/* 143:    */   
/* 144:    */   public boolean isKeepAlive()
/* 145:    */   {
/* 146:    */     try
/* 147:    */     {
/* 148:166 */       return this.javaSocket.getKeepAlive();
/* 149:    */     }
/* 150:    */     catch (SocketException e)
/* 151:    */     {
/* 152:168 */       throw new ChannelException(e);
/* 153:    */     }
/* 154:    */   }
/* 155:    */   
/* 156:    */   public boolean isReuseAddress()
/* 157:    */   {
/* 158:    */     try
/* 159:    */     {
/* 160:175 */       return this.javaSocket.getReuseAddress();
/* 161:    */     }
/* 162:    */     catch (SocketException e)
/* 163:    */     {
/* 164:177 */       throw new ChannelException(e);
/* 165:    */     }
/* 166:    */   }
/* 167:    */   
/* 168:    */   public boolean isTcpNoDelay()
/* 169:    */   {
/* 170:    */     try
/* 171:    */     {
/* 172:184 */       return this.javaSocket.getTcpNoDelay();
/* 173:    */     }
/* 174:    */     catch (SocketException e)
/* 175:    */     {
/* 176:186 */       throw new ChannelException(e);
/* 177:    */     }
/* 178:    */   }
/* 179:    */   
/* 180:    */   public SocketChannelConfig setKeepAlive(boolean keepAlive)
/* 181:    */   {
/* 182:    */     try
/* 183:    */     {
/* 184:193 */       this.javaSocket.setKeepAlive(keepAlive);
/* 185:    */     }
/* 186:    */     catch (SocketException e)
/* 187:    */     {
/* 188:195 */       throw new ChannelException(e);
/* 189:    */     }
/* 190:197 */     return this;
/* 191:    */   }
/* 192:    */   
/* 193:    */   public SocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/* 194:    */   {
/* 195:203 */     this.javaSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
/* 196:204 */     return this;
/* 197:    */   }
/* 198:    */   
/* 199:    */   public SocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 200:    */   {
/* 201:    */     try
/* 202:    */     {
/* 203:210 */       this.javaSocket.setReceiveBufferSize(receiveBufferSize);
/* 204:    */     }
/* 205:    */     catch (SocketException e)
/* 206:    */     {
/* 207:212 */       throw new ChannelException(e);
/* 208:    */     }
/* 209:214 */     return this;
/* 210:    */   }
/* 211:    */   
/* 212:    */   public SocketChannelConfig setReuseAddress(boolean reuseAddress)
/* 213:    */   {
/* 214:    */     try
/* 215:    */     {
/* 216:220 */       this.javaSocket.setReuseAddress(reuseAddress);
/* 217:    */     }
/* 218:    */     catch (SocketException e)
/* 219:    */     {
/* 220:222 */       throw new ChannelException(e);
/* 221:    */     }
/* 222:224 */     return this;
/* 223:    */   }
/* 224:    */   
/* 225:    */   public SocketChannelConfig setSendBufferSize(int sendBufferSize)
/* 226:    */   {
/* 227:    */     try
/* 228:    */     {
/* 229:230 */       this.javaSocket.setSendBufferSize(sendBufferSize);
/* 230:    */     }
/* 231:    */     catch (SocketException e)
/* 232:    */     {
/* 233:232 */       throw new ChannelException(e);
/* 234:    */     }
/* 235:234 */     return this;
/* 236:    */   }
/* 237:    */   
/* 238:    */   public SocketChannelConfig setSoLinger(int soLinger)
/* 239:    */   {
/* 240:    */     try
/* 241:    */     {
/* 242:240 */       if (soLinger < 0) {
/* 243:241 */         this.javaSocket.setSoLinger(false, 0);
/* 244:    */       } else {
/* 245:243 */         this.javaSocket.setSoLinger(true, soLinger);
/* 246:    */       }
/* 247:    */     }
/* 248:    */     catch (SocketException e)
/* 249:    */     {
/* 250:246 */       throw new ChannelException(e);
/* 251:    */     }
/* 252:248 */     return this;
/* 253:    */   }
/* 254:    */   
/* 255:    */   public SocketChannelConfig setTcpNoDelay(boolean tcpNoDelay)
/* 256:    */   {
/* 257:    */     try
/* 258:    */     {
/* 259:254 */       this.javaSocket.setTcpNoDelay(tcpNoDelay);
/* 260:    */     }
/* 261:    */     catch (SocketException e)
/* 262:    */     {
/* 263:256 */       throw new ChannelException(e);
/* 264:    */     }
/* 265:258 */     return this;
/* 266:    */   }
/* 267:    */   
/* 268:    */   public SocketChannelConfig setTrafficClass(int trafficClass)
/* 269:    */   {
/* 270:    */     try
/* 271:    */     {
/* 272:264 */       this.javaSocket.setTrafficClass(trafficClass);
/* 273:    */     }
/* 274:    */     catch (SocketException e)
/* 275:    */     {
/* 276:266 */       throw new ChannelException(e);
/* 277:    */     }
/* 278:268 */     return this;
/* 279:    */   }
/* 280:    */   
/* 281:    */   public boolean isAllowHalfClosure()
/* 282:    */   {
/* 283:273 */     return this.allowHalfClosure;
/* 284:    */   }
/* 285:    */   
/* 286:    */   public SocketChannelConfig setAllowHalfClosure(boolean allowHalfClosure)
/* 287:    */   {
/* 288:278 */     this.allowHalfClosure = allowHalfClosure;
/* 289:279 */     return this;
/* 290:    */   }
/* 291:    */   
/* 292:    */   public SocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 293:    */   {
/* 294:284 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 295:285 */     return this;
/* 296:    */   }
/* 297:    */   
/* 298:    */   public SocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 299:    */   {
/* 300:290 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 301:291 */     return this;
/* 302:    */   }
/* 303:    */   
/* 304:    */   public SocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 305:    */   {
/* 306:296 */     super.setWriteSpinCount(writeSpinCount);
/* 307:297 */     return this;
/* 308:    */   }
/* 309:    */   
/* 310:    */   public SocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 311:    */   {
/* 312:302 */     super.setAllocator(allocator);
/* 313:303 */     return this;
/* 314:    */   }
/* 315:    */   
/* 316:    */   public SocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 317:    */   {
/* 318:308 */     super.setRecvByteBufAllocator(allocator);
/* 319:309 */     return this;
/* 320:    */   }
/* 321:    */   
/* 322:    */   public SocketChannelConfig setAutoRead(boolean autoRead)
/* 323:    */   {
/* 324:314 */     super.setAutoRead(autoRead);
/* 325:315 */     return this;
/* 326:    */   }
/* 327:    */   
/* 328:    */   public SocketChannelConfig setAutoClose(boolean autoClose)
/* 329:    */   {
/* 330:320 */     super.setAutoClose(autoClose);
/* 331:321 */     return this;
/* 332:    */   }
/* 333:    */   
/* 334:    */   public SocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 335:    */   {
/* 336:326 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 337:327 */     return this;
/* 338:    */   }
/* 339:    */   
/* 340:    */   public SocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 341:    */   {
/* 342:332 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 343:333 */     return this;
/* 344:    */   }
/* 345:    */   
/* 346:    */   public SocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 347:    */   {
/* 348:338 */     super.setMessageSizeEstimator(estimator);
/* 349:339 */     return this;
/* 350:    */   }
/* 351:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.socket.DefaultSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */