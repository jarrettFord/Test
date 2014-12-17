/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.nio.AbstractNioByteChannel;
/*   5:    */ import java.util.IdentityHashMap;
/*   6:    */ import java.util.Map;
/*   7:    */ import java.util.Map.Entry;
/*   8:    */ 
/*   9:    */ public class DefaultChannelConfig
/*  10:    */   implements ChannelConfig
/*  11:    */ {
/*  12: 33 */   private static final RecvByteBufAllocator DEFAULT_RCVBUF_ALLOCATOR = AdaptiveRecvByteBufAllocator.DEFAULT;
/*  13: 34 */   private static final MessageSizeEstimator DEFAULT_MSG_SIZE_ESTIMATOR = DefaultMessageSizeEstimator.DEFAULT;
/*  14:    */   private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
/*  15:    */   protected final Channel channel;
/*  16: 40 */   private volatile ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
/*  17: 41 */   private volatile RecvByteBufAllocator rcvBufAllocator = DEFAULT_RCVBUF_ALLOCATOR;
/*  18: 42 */   private volatile MessageSizeEstimator msgSizeEstimator = DEFAULT_MSG_SIZE_ESTIMATOR;
/*  19: 44 */   private volatile int connectTimeoutMillis = 30000;
/*  20:    */   private volatile int maxMessagesPerRead;
/*  21: 46 */   private volatile int writeSpinCount = 16;
/*  22: 47 */   private volatile boolean autoRead = true;
/*  23: 48 */   private volatile boolean autoClose = true;
/*  24: 49 */   private volatile int writeBufferHighWaterMark = 65536;
/*  25: 50 */   private volatile int writeBufferLowWaterMark = 32768;
/*  26:    */   
/*  27:    */   public DefaultChannelConfig(Channel channel)
/*  28:    */   {
/*  29: 53 */     if (channel == null) {
/*  30: 54 */       throw new NullPointerException("channel");
/*  31:    */     }
/*  32: 56 */     this.channel = channel;
/*  33: 58 */     if (((channel instanceof ServerChannel)) || ((channel instanceof AbstractNioByteChannel))) {
/*  34: 63 */       this.maxMessagesPerRead = 16;
/*  35:    */     } else {
/*  36: 65 */       this.maxMessagesPerRead = 1;
/*  37:    */     }
/*  38:    */   }
/*  39:    */   
/*  40:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  41:    */   {
/*  42: 72 */     return getOptions(null, new ChannelOption[] { ChannelOption.CONNECT_TIMEOUT_MILLIS, ChannelOption.MAX_MESSAGES_PER_READ, ChannelOption.WRITE_SPIN_COUNT, ChannelOption.ALLOCATOR, ChannelOption.AUTO_READ, ChannelOption.AUTO_CLOSE, ChannelOption.RCVBUF_ALLOCATOR, ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, ChannelOption.MESSAGE_SIZE_ESTIMATOR });
/*  43:    */   }
/*  44:    */   
/*  45:    */   protected Map<ChannelOption<?>, Object> getOptions(Map<ChannelOption<?>, Object> result, ChannelOption<?>... options)
/*  46:    */   {
/*  47: 81 */     if (result == null) {
/*  48: 82 */       result = new IdentityHashMap();
/*  49:    */     }
/*  50: 84 */     for (ChannelOption<?> o : options) {
/*  51: 85 */       result.put(o, getOption(o));
/*  52:    */     }
/*  53: 87 */     return result;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public boolean setOptions(Map<ChannelOption<?>, ?> options)
/*  57:    */   {
/*  58: 93 */     if (options == null) {
/*  59: 94 */       throw new NullPointerException("options");
/*  60:    */     }
/*  61: 97 */     boolean setAllOptions = true;
/*  62: 98 */     for (Map.Entry<ChannelOption<?>, ?> e : options.entrySet()) {
/*  63: 99 */       if (!setOption((ChannelOption)e.getKey(), e.getValue())) {
/*  64:100 */         setAllOptions = false;
/*  65:    */       }
/*  66:    */     }
/*  67:104 */     return setAllOptions;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public <T> T getOption(ChannelOption<T> option)
/*  71:    */   {
/*  72:110 */     if (option == null) {
/*  73:111 */       throw new NullPointerException("option");
/*  74:    */     }
/*  75:114 */     if (option == ChannelOption.CONNECT_TIMEOUT_MILLIS) {
/*  76:115 */       return Integer.valueOf(getConnectTimeoutMillis());
/*  77:    */     }
/*  78:117 */     if (option == ChannelOption.MAX_MESSAGES_PER_READ) {
/*  79:118 */       return Integer.valueOf(getMaxMessagesPerRead());
/*  80:    */     }
/*  81:120 */     if (option == ChannelOption.WRITE_SPIN_COUNT) {
/*  82:121 */       return Integer.valueOf(getWriteSpinCount());
/*  83:    */     }
/*  84:123 */     if (option == ChannelOption.ALLOCATOR) {
/*  85:124 */       return getAllocator();
/*  86:    */     }
/*  87:126 */     if (option == ChannelOption.RCVBUF_ALLOCATOR) {
/*  88:127 */       return getRecvByteBufAllocator();
/*  89:    */     }
/*  90:129 */     if (option == ChannelOption.AUTO_READ) {
/*  91:130 */       return Boolean.valueOf(isAutoRead());
/*  92:    */     }
/*  93:132 */     if (option == ChannelOption.AUTO_CLOSE) {
/*  94:133 */       return Boolean.valueOf(isAutoClose());
/*  95:    */     }
/*  96:135 */     if (option == ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK) {
/*  97:136 */       return Integer.valueOf(getWriteBufferHighWaterMark());
/*  98:    */     }
/*  99:138 */     if (option == ChannelOption.WRITE_BUFFER_LOW_WATER_MARK) {
/* 100:139 */       return Integer.valueOf(getWriteBufferLowWaterMark());
/* 101:    */     }
/* 102:141 */     if (option == ChannelOption.MESSAGE_SIZE_ESTIMATOR) {
/* 103:142 */       return getMessageSizeEstimator();
/* 104:    */     }
/* 105:144 */     return null;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/* 109:    */   {
/* 110:150 */     validate(option, value);
/* 111:152 */     if (option == ChannelOption.CONNECT_TIMEOUT_MILLIS) {
/* 112:153 */       setConnectTimeoutMillis(((Integer)value).intValue());
/* 113:154 */     } else if (option == ChannelOption.MAX_MESSAGES_PER_READ) {
/* 114:155 */       setMaxMessagesPerRead(((Integer)value).intValue());
/* 115:156 */     } else if (option == ChannelOption.WRITE_SPIN_COUNT) {
/* 116:157 */       setWriteSpinCount(((Integer)value).intValue());
/* 117:158 */     } else if (option == ChannelOption.ALLOCATOR) {
/* 118:159 */       setAllocator((ByteBufAllocator)value);
/* 119:160 */     } else if (option == ChannelOption.RCVBUF_ALLOCATOR) {
/* 120:161 */       setRecvByteBufAllocator((RecvByteBufAllocator)value);
/* 121:162 */     } else if (option == ChannelOption.AUTO_READ) {
/* 122:163 */       setAutoRead(((Boolean)value).booleanValue());
/* 123:164 */     } else if (option == ChannelOption.AUTO_CLOSE) {
/* 124:165 */       setAutoClose(((Boolean)value).booleanValue());
/* 125:166 */     } else if (option == ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK) {
/* 126:167 */       setWriteBufferHighWaterMark(((Integer)value).intValue());
/* 127:168 */     } else if (option == ChannelOption.WRITE_BUFFER_LOW_WATER_MARK) {
/* 128:169 */       setWriteBufferLowWaterMark(((Integer)value).intValue());
/* 129:170 */     } else if (option == ChannelOption.MESSAGE_SIZE_ESTIMATOR) {
/* 130:171 */       setMessageSizeEstimator((MessageSizeEstimator)value);
/* 131:    */     } else {
/* 132:173 */       return false;
/* 133:    */     }
/* 134:176 */     return true;
/* 135:    */   }
/* 136:    */   
/* 137:    */   protected <T> void validate(ChannelOption<T> option, T value)
/* 138:    */   {
/* 139:180 */     if (option == null) {
/* 140:181 */       throw new NullPointerException("option");
/* 141:    */     }
/* 142:183 */     option.validate(value);
/* 143:    */   }
/* 144:    */   
/* 145:    */   public int getConnectTimeoutMillis()
/* 146:    */   {
/* 147:188 */     return this.connectTimeoutMillis;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public ChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 151:    */   {
/* 152:193 */     if (connectTimeoutMillis < 0) {
/* 153:194 */       throw new IllegalArgumentException(String.format("connectTimeoutMillis: %d (expected: >= 0)", new Object[] { Integer.valueOf(connectTimeoutMillis) }));
/* 154:    */     }
/* 155:197 */     this.connectTimeoutMillis = connectTimeoutMillis;
/* 156:198 */     return this;
/* 157:    */   }
/* 158:    */   
/* 159:    */   public int getMaxMessagesPerRead()
/* 160:    */   {
/* 161:203 */     return this.maxMessagesPerRead;
/* 162:    */   }
/* 163:    */   
/* 164:    */   public ChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 165:    */   {
/* 166:208 */     if (maxMessagesPerRead <= 0) {
/* 167:209 */       throw new IllegalArgumentException("maxMessagesPerRead: " + maxMessagesPerRead + " (expected: > 0)");
/* 168:    */     }
/* 169:211 */     this.maxMessagesPerRead = maxMessagesPerRead;
/* 170:212 */     return this;
/* 171:    */   }
/* 172:    */   
/* 173:    */   public int getWriteSpinCount()
/* 174:    */   {
/* 175:217 */     return this.writeSpinCount;
/* 176:    */   }
/* 177:    */   
/* 178:    */   public ChannelConfig setWriteSpinCount(int writeSpinCount)
/* 179:    */   {
/* 180:222 */     if (writeSpinCount <= 0) {
/* 181:223 */       throw new IllegalArgumentException("writeSpinCount must be a positive integer.");
/* 182:    */     }
/* 183:226 */     this.writeSpinCount = writeSpinCount;
/* 184:227 */     return this;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public ByteBufAllocator getAllocator()
/* 188:    */   {
/* 189:232 */     return this.allocator;
/* 190:    */   }
/* 191:    */   
/* 192:    */   public ChannelConfig setAllocator(ByteBufAllocator allocator)
/* 193:    */   {
/* 194:237 */     if (allocator == null) {
/* 195:238 */       throw new NullPointerException("allocator");
/* 196:    */     }
/* 197:240 */     this.allocator = allocator;
/* 198:241 */     return this;
/* 199:    */   }
/* 200:    */   
/* 201:    */   public RecvByteBufAllocator getRecvByteBufAllocator()
/* 202:    */   {
/* 203:246 */     return this.rcvBufAllocator;
/* 204:    */   }
/* 205:    */   
/* 206:    */   public ChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 207:    */   {
/* 208:251 */     if (allocator == null) {
/* 209:252 */       throw new NullPointerException("allocator");
/* 210:    */     }
/* 211:254 */     this.rcvBufAllocator = allocator;
/* 212:255 */     return this;
/* 213:    */   }
/* 214:    */   
/* 215:    */   public boolean isAutoRead()
/* 216:    */   {
/* 217:260 */     return this.autoRead;
/* 218:    */   }
/* 219:    */   
/* 220:    */   public ChannelConfig setAutoRead(boolean autoRead)
/* 221:    */   {
/* 222:265 */     boolean oldAutoRead = this.autoRead;
/* 223:266 */     this.autoRead = autoRead;
/* 224:267 */     if ((autoRead) && (!oldAutoRead)) {
/* 225:268 */       this.channel.read();
/* 226:269 */     } else if ((!autoRead) && (oldAutoRead)) {
/* 227:270 */       autoReadCleared();
/* 228:    */     }
/* 229:272 */     return this;
/* 230:    */   }
/* 231:    */   
/* 232:    */   protected void autoReadCleared() {}
/* 233:    */   
/* 234:    */   public boolean isAutoClose()
/* 235:    */   {
/* 236:283 */     return this.autoClose;
/* 237:    */   }
/* 238:    */   
/* 239:    */   public ChannelConfig setAutoClose(boolean autoClose)
/* 240:    */   {
/* 241:288 */     this.autoClose = autoClose;
/* 242:289 */     return this;
/* 243:    */   }
/* 244:    */   
/* 245:    */   public int getWriteBufferHighWaterMark()
/* 246:    */   {
/* 247:294 */     return this.writeBufferHighWaterMark;
/* 248:    */   }
/* 249:    */   
/* 250:    */   public ChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 251:    */   {
/* 252:299 */     if (writeBufferHighWaterMark < getWriteBufferLowWaterMark()) {
/* 253:300 */       throw new IllegalArgumentException("writeBufferHighWaterMark cannot be less than writeBufferLowWaterMark (" + getWriteBufferLowWaterMark() + "): " + writeBufferHighWaterMark);
/* 254:    */     }
/* 255:305 */     if (writeBufferHighWaterMark < 0) {
/* 256:306 */       throw new IllegalArgumentException("writeBufferHighWaterMark must be >= 0");
/* 257:    */     }
/* 258:309 */     this.writeBufferHighWaterMark = writeBufferHighWaterMark;
/* 259:310 */     return this;
/* 260:    */   }
/* 261:    */   
/* 262:    */   public int getWriteBufferLowWaterMark()
/* 263:    */   {
/* 264:315 */     return this.writeBufferLowWaterMark;
/* 265:    */   }
/* 266:    */   
/* 267:    */   public ChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 268:    */   {
/* 269:320 */     if (writeBufferLowWaterMark > getWriteBufferHighWaterMark()) {
/* 270:321 */       throw new IllegalArgumentException("writeBufferLowWaterMark cannot be greater than writeBufferHighWaterMark (" + getWriteBufferHighWaterMark() + "): " + writeBufferLowWaterMark);
/* 271:    */     }
/* 272:326 */     if (writeBufferLowWaterMark < 0) {
/* 273:327 */       throw new IllegalArgumentException("writeBufferLowWaterMark must be >= 0");
/* 274:    */     }
/* 275:330 */     this.writeBufferLowWaterMark = writeBufferLowWaterMark;
/* 276:331 */     return this;
/* 277:    */   }
/* 278:    */   
/* 279:    */   public MessageSizeEstimator getMessageSizeEstimator()
/* 280:    */   {
/* 281:336 */     return this.msgSizeEstimator;
/* 282:    */   }
/* 283:    */   
/* 284:    */   public ChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 285:    */   {
/* 286:341 */     if (estimator == null) {
/* 287:342 */       throw new NullPointerException("estimator");
/* 288:    */     }
/* 289:344 */     this.msgSizeEstimator = estimator;
/* 290:345 */     return this;
/* 291:    */   }
/* 292:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.DefaultChannelConfig
 * JD-Core Version:    0.7.0.1
 */