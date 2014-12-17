/*   1:    */ package io.netty.channel.rxtx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelOption;
/*   5:    */ import io.netty.channel.DefaultChannelConfig;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator;
/*   8:    */ import java.util.Map;
/*   9:    */ 
/*  10:    */ final class DefaultRxtxChannelConfig
/*  11:    */   extends DefaultChannelConfig
/*  12:    */   implements RxtxChannelConfig
/*  13:    */ {
/*  14: 33 */   private volatile int baudrate = 115200;
/*  15:    */   private volatile boolean dtr;
/*  16:    */   private volatile boolean rts;
/*  17: 36 */   private volatile RxtxChannelConfig.Stopbits stopbits = RxtxChannelConfig.Stopbits.STOPBITS_1;
/*  18: 37 */   private volatile RxtxChannelConfig.Databits databits = RxtxChannelConfig.Databits.DATABITS_8;
/*  19: 38 */   private volatile RxtxChannelConfig.Paritybit paritybit = RxtxChannelConfig.Paritybit.NONE;
/*  20:    */   private volatile int waitTime;
/*  21: 40 */   private volatile int readTimeout = 1000;
/*  22:    */   
/*  23:    */   public DefaultRxtxChannelConfig(RxtxChannel channel)
/*  24:    */   {
/*  25: 43 */     super(channel);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  29:    */   {
/*  30: 48 */     return getOptions(super.getOptions(), new ChannelOption[] { RxtxChannelOption.BAUD_RATE, RxtxChannelOption.DTR, RxtxChannelOption.RTS, RxtxChannelOption.STOP_BITS, RxtxChannelOption.DATA_BITS, RxtxChannelOption.PARITY_BIT, RxtxChannelOption.WAIT_TIME });
/*  31:    */   }
/*  32:    */   
/*  33:    */   public <T> T getOption(ChannelOption<T> option)
/*  34:    */   {
/*  35: 54 */     if (option == RxtxChannelOption.BAUD_RATE) {
/*  36: 55 */       return Integer.valueOf(getBaudrate());
/*  37:    */     }
/*  38: 57 */     if (option == RxtxChannelOption.DTR) {
/*  39: 58 */       return Boolean.valueOf(isDtr());
/*  40:    */     }
/*  41: 60 */     if (option == RxtxChannelOption.RTS) {
/*  42: 61 */       return Boolean.valueOf(isRts());
/*  43:    */     }
/*  44: 63 */     if (option == RxtxChannelOption.STOP_BITS) {
/*  45: 64 */       return getStopbits();
/*  46:    */     }
/*  47: 66 */     if (option == RxtxChannelOption.DATA_BITS) {
/*  48: 67 */       return getDatabits();
/*  49:    */     }
/*  50: 69 */     if (option == RxtxChannelOption.PARITY_BIT) {
/*  51: 70 */       return getParitybit();
/*  52:    */     }
/*  53: 72 */     if (option == RxtxChannelOption.WAIT_TIME) {
/*  54: 73 */       return Integer.valueOf(getWaitTimeMillis());
/*  55:    */     }
/*  56: 75 */     if (option == RxtxChannelOption.READ_TIMEOUT) {
/*  57: 76 */       return Integer.valueOf(getReadTimeout());
/*  58:    */     }
/*  59: 78 */     return super.getOption(option);
/*  60:    */   }
/*  61:    */   
/*  62:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  63:    */   {
/*  64: 83 */     validate(option, value);
/*  65: 85 */     if (option == RxtxChannelOption.BAUD_RATE) {
/*  66: 86 */       setBaudrate(((Integer)value).intValue());
/*  67: 87 */     } else if (option == RxtxChannelOption.DTR) {
/*  68: 88 */       setDtr(((Boolean)value).booleanValue());
/*  69: 89 */     } else if (option == RxtxChannelOption.RTS) {
/*  70: 90 */       setRts(((Boolean)value).booleanValue());
/*  71: 91 */     } else if (option == RxtxChannelOption.STOP_BITS) {
/*  72: 92 */       setStopbits((RxtxChannelConfig.Stopbits)value);
/*  73: 93 */     } else if (option == RxtxChannelOption.DATA_BITS) {
/*  74: 94 */       setDatabits((RxtxChannelConfig.Databits)value);
/*  75: 95 */     } else if (option == RxtxChannelOption.PARITY_BIT) {
/*  76: 96 */       setParitybit((RxtxChannelConfig.Paritybit)value);
/*  77: 97 */     } else if (option == RxtxChannelOption.WAIT_TIME) {
/*  78: 98 */       setWaitTimeMillis(((Integer)value).intValue());
/*  79: 99 */     } else if (option == RxtxChannelOption.READ_TIMEOUT) {
/*  80:100 */       setReadTimeout(((Integer)value).intValue());
/*  81:    */     } else {
/*  82:102 */       return super.setOption(option, value);
/*  83:    */     }
/*  84:104 */     return true;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public RxtxChannelConfig setBaudrate(int baudrate)
/*  88:    */   {
/*  89:109 */     this.baudrate = baudrate;
/*  90:110 */     return this;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public RxtxChannelConfig setStopbits(RxtxChannelConfig.Stopbits stopbits)
/*  94:    */   {
/*  95:115 */     this.stopbits = stopbits;
/*  96:116 */     return this;
/*  97:    */   }
/*  98:    */   
/*  99:    */   public RxtxChannelConfig setDatabits(RxtxChannelConfig.Databits databits)
/* 100:    */   {
/* 101:121 */     this.databits = databits;
/* 102:122 */     return this;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public RxtxChannelConfig setParitybit(RxtxChannelConfig.Paritybit paritybit)
/* 106:    */   {
/* 107:127 */     this.paritybit = paritybit;
/* 108:128 */     return this;
/* 109:    */   }
/* 110:    */   
/* 111:    */   public int getBaudrate()
/* 112:    */   {
/* 113:133 */     return this.baudrate;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public RxtxChannelConfig.Stopbits getStopbits()
/* 117:    */   {
/* 118:138 */     return this.stopbits;
/* 119:    */   }
/* 120:    */   
/* 121:    */   public RxtxChannelConfig.Databits getDatabits()
/* 122:    */   {
/* 123:143 */     return this.databits;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public RxtxChannelConfig.Paritybit getParitybit()
/* 127:    */   {
/* 128:148 */     return this.paritybit;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public boolean isDtr()
/* 132:    */   {
/* 133:153 */     return this.dtr;
/* 134:    */   }
/* 135:    */   
/* 136:    */   public RxtxChannelConfig setDtr(boolean dtr)
/* 137:    */   {
/* 138:158 */     this.dtr = dtr;
/* 139:159 */     return this;
/* 140:    */   }
/* 141:    */   
/* 142:    */   public boolean isRts()
/* 143:    */   {
/* 144:164 */     return this.rts;
/* 145:    */   }
/* 146:    */   
/* 147:    */   public RxtxChannelConfig setRts(boolean rts)
/* 148:    */   {
/* 149:169 */     this.rts = rts;
/* 150:170 */     return this;
/* 151:    */   }
/* 152:    */   
/* 153:    */   public int getWaitTimeMillis()
/* 154:    */   {
/* 155:175 */     return this.waitTime;
/* 156:    */   }
/* 157:    */   
/* 158:    */   public RxtxChannelConfig setWaitTimeMillis(int waitTimeMillis)
/* 159:    */   {
/* 160:180 */     if (waitTimeMillis < 0) {
/* 161:181 */       throw new IllegalArgumentException("Wait time must be >= 0");
/* 162:    */     }
/* 163:183 */     this.waitTime = waitTimeMillis;
/* 164:184 */     return this;
/* 165:    */   }
/* 166:    */   
/* 167:    */   public RxtxChannelConfig setReadTimeout(int readTimeout)
/* 168:    */   {
/* 169:189 */     if (readTimeout < 0) {
/* 170:190 */       throw new IllegalArgumentException("readTime must be >= 0");
/* 171:    */     }
/* 172:192 */     this.readTimeout = readTimeout;
/* 173:193 */     return this;
/* 174:    */   }
/* 175:    */   
/* 176:    */   public int getReadTimeout()
/* 177:    */   {
/* 178:198 */     return this.readTimeout;
/* 179:    */   }
/* 180:    */   
/* 181:    */   public RxtxChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 182:    */   {
/* 183:203 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 184:204 */     return this;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public RxtxChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 188:    */   {
/* 189:209 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 190:210 */     return this;
/* 191:    */   }
/* 192:    */   
/* 193:    */   public RxtxChannelConfig setWriteSpinCount(int writeSpinCount)
/* 194:    */   {
/* 195:215 */     super.setWriteSpinCount(writeSpinCount);
/* 196:216 */     return this;
/* 197:    */   }
/* 198:    */   
/* 199:    */   public RxtxChannelConfig setAllocator(ByteBufAllocator allocator)
/* 200:    */   {
/* 201:221 */     super.setAllocator(allocator);
/* 202:222 */     return this;
/* 203:    */   }
/* 204:    */   
/* 205:    */   public RxtxChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 206:    */   {
/* 207:227 */     super.setRecvByteBufAllocator(allocator);
/* 208:228 */     return this;
/* 209:    */   }
/* 210:    */   
/* 211:    */   public RxtxChannelConfig setAutoRead(boolean autoRead)
/* 212:    */   {
/* 213:233 */     super.setAutoRead(autoRead);
/* 214:234 */     return this;
/* 215:    */   }
/* 216:    */   
/* 217:    */   public RxtxChannelConfig setAutoClose(boolean autoClose)
/* 218:    */   {
/* 219:239 */     super.setAutoClose(autoClose);
/* 220:240 */     return this;
/* 221:    */   }
/* 222:    */   
/* 223:    */   public RxtxChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 224:    */   {
/* 225:245 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 226:246 */     return this;
/* 227:    */   }
/* 228:    */   
/* 229:    */   public RxtxChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 230:    */   {
/* 231:251 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 232:252 */     return this;
/* 233:    */   }
/* 234:    */   
/* 235:    */   public RxtxChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 236:    */   {
/* 237:257 */     super.setMessageSizeEstimator(estimator);
/* 238:258 */     return this;
/* 239:    */   }
/* 240:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.rxtx.DefaultRxtxChannelConfig
 * JD-Core Version:    0.7.0.1
 */