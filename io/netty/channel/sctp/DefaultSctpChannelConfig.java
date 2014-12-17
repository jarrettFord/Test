/*   1:    */ package io.netty.channel.sctp;
/*   2:    */ 
/*   3:    */ import com.sun.nio.sctp.SctpStandardSocketOptions;
/*   4:    */ import com.sun.nio.sctp.SctpStandardSocketOptions.InitMaxStreams;
/*   5:    */ import io.netty.buffer.ByteBufAllocator;
/*   6:    */ import io.netty.channel.ChannelException;
/*   7:    */ import io.netty.channel.ChannelOption;
/*   8:    */ import io.netty.channel.DefaultChannelConfig;
/*   9:    */ import io.netty.channel.MessageSizeEstimator;
/*  10:    */ import io.netty.channel.RecvByteBufAllocator;
/*  11:    */ import io.netty.util.internal.PlatformDependent;
/*  12:    */ import java.io.IOException;
/*  13:    */ import java.util.Map;
/*  14:    */ 
/*  15:    */ public class DefaultSctpChannelConfig
/*  16:    */   extends DefaultChannelConfig
/*  17:    */   implements SctpChannelConfig
/*  18:    */ {
/*  19:    */   private final com.sun.nio.sctp.SctpChannel javaChannel;
/*  20:    */   
/*  21:    */   public DefaultSctpChannelConfig(SctpChannel channel, com.sun.nio.sctp.SctpChannel javaChannel)
/*  22:    */   {
/*  23: 42 */     super(channel);
/*  24: 43 */     if (javaChannel == null) {
/*  25: 44 */       throw new NullPointerException("javaChannel");
/*  26:    */     }
/*  27: 46 */     this.javaChannel = javaChannel;
/*  28: 49 */     if (PlatformDependent.canEnableTcpNoDelayByDefault()) {
/*  29:    */       try
/*  30:    */       {
/*  31: 51 */         setSctpNoDelay(true);
/*  32:    */       }
/*  33:    */       catch (Exception e) {}
/*  34:    */     }
/*  35:    */   }
/*  36:    */   
/*  37:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  38:    */   {
/*  39: 60 */     return getOptions(super.getOptions(), new ChannelOption[] { SctpChannelOption.SO_RCVBUF, SctpChannelOption.SO_SNDBUF, SctpChannelOption.SCTP_NODELAY, SctpChannelOption.SCTP_INIT_MAXSTREAMS });
/*  40:    */   }
/*  41:    */   
/*  42:    */   public <T> T getOption(ChannelOption<T> option)
/*  43:    */   {
/*  44: 68 */     if (option == SctpChannelOption.SO_RCVBUF) {
/*  45: 69 */       return Integer.valueOf(getReceiveBufferSize());
/*  46:    */     }
/*  47: 71 */     if (option == SctpChannelOption.SO_SNDBUF) {
/*  48: 72 */       return Integer.valueOf(getSendBufferSize());
/*  49:    */     }
/*  50: 74 */     if (option == SctpChannelOption.SCTP_NODELAY) {
/*  51: 75 */       return Boolean.valueOf(isSctpNoDelay());
/*  52:    */     }
/*  53: 77 */     return super.getOption(option);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  57:    */   {
/*  58: 82 */     validate(option, value);
/*  59: 84 */     if (option == SctpChannelOption.SO_RCVBUF) {
/*  60: 85 */       setReceiveBufferSize(((Integer)value).intValue());
/*  61: 86 */     } else if (option == SctpChannelOption.SO_SNDBUF) {
/*  62: 87 */       setSendBufferSize(((Integer)value).intValue());
/*  63: 88 */     } else if (option == SctpChannelOption.SCTP_NODELAY) {
/*  64: 89 */       setSctpNoDelay(((Boolean)value).booleanValue());
/*  65: 90 */     } else if (option == SctpChannelOption.SCTP_INIT_MAXSTREAMS) {
/*  66: 91 */       setInitMaxStreams((SctpStandardSocketOptions.InitMaxStreams)value);
/*  67:    */     } else {
/*  68: 93 */       return super.setOption(option, value);
/*  69:    */     }
/*  70: 96 */     return true;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean isSctpNoDelay()
/*  74:    */   {
/*  75:    */     try
/*  76:    */     {
/*  77:102 */       return ((Boolean)this.javaChannel.getOption(SctpStandardSocketOptions.SCTP_NODELAY)).booleanValue();
/*  78:    */     }
/*  79:    */     catch (IOException e)
/*  80:    */     {
/*  81:104 */       throw new ChannelException(e);
/*  82:    */     }
/*  83:    */   }
/*  84:    */   
/*  85:    */   public SctpChannelConfig setSctpNoDelay(boolean sctpNoDelay)
/*  86:    */   {
/*  87:    */     try
/*  88:    */     {
/*  89:111 */       this.javaChannel.setOption(SctpStandardSocketOptions.SCTP_NODELAY, Boolean.valueOf(sctpNoDelay));
/*  90:    */     }
/*  91:    */     catch (IOException e)
/*  92:    */     {
/*  93:113 */       throw new ChannelException(e);
/*  94:    */     }
/*  95:115 */     return this;
/*  96:    */   }
/*  97:    */   
/*  98:    */   public int getSendBufferSize()
/*  99:    */   {
/* 100:    */     try
/* 101:    */     {
/* 102:121 */       return ((Integer)this.javaChannel.getOption(SctpStandardSocketOptions.SO_SNDBUF)).intValue();
/* 103:    */     }
/* 104:    */     catch (IOException e)
/* 105:    */     {
/* 106:123 */       throw new ChannelException(e);
/* 107:    */     }
/* 108:    */   }
/* 109:    */   
/* 110:    */   public SctpChannelConfig setSendBufferSize(int sendBufferSize)
/* 111:    */   {
/* 112:    */     try
/* 113:    */     {
/* 114:130 */       this.javaChannel.setOption(SctpStandardSocketOptions.SO_SNDBUF, Integer.valueOf(sendBufferSize));
/* 115:    */     }
/* 116:    */     catch (IOException e)
/* 117:    */     {
/* 118:132 */       throw new ChannelException(e);
/* 119:    */     }
/* 120:134 */     return this;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public int getReceiveBufferSize()
/* 124:    */   {
/* 125:    */     try
/* 126:    */     {
/* 127:140 */       return ((Integer)this.javaChannel.getOption(SctpStandardSocketOptions.SO_RCVBUF)).intValue();
/* 128:    */     }
/* 129:    */     catch (IOException e)
/* 130:    */     {
/* 131:142 */       throw new ChannelException(e);
/* 132:    */     }
/* 133:    */   }
/* 134:    */   
/* 135:    */   public SctpChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 136:    */   {
/* 137:    */     try
/* 138:    */     {
/* 139:149 */       this.javaChannel.setOption(SctpStandardSocketOptions.SO_RCVBUF, Integer.valueOf(receiveBufferSize));
/* 140:    */     }
/* 141:    */     catch (IOException e)
/* 142:    */     {
/* 143:151 */       throw new ChannelException(e);
/* 144:    */     }
/* 145:153 */     return this;
/* 146:    */   }
/* 147:    */   
/* 148:    */   public SctpStandardSocketOptions.InitMaxStreams getInitMaxStreams()
/* 149:    */   {
/* 150:    */     try
/* 151:    */     {
/* 152:159 */       return (SctpStandardSocketOptions.InitMaxStreams)this.javaChannel.getOption(SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS);
/* 153:    */     }
/* 154:    */     catch (IOException e)
/* 155:    */     {
/* 156:161 */       throw new ChannelException(e);
/* 157:    */     }
/* 158:    */   }
/* 159:    */   
/* 160:    */   public SctpChannelConfig setInitMaxStreams(SctpStandardSocketOptions.InitMaxStreams initMaxStreams)
/* 161:    */   {
/* 162:    */     try
/* 163:    */     {
/* 164:168 */       this.javaChannel.setOption(SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS, initMaxStreams);
/* 165:    */     }
/* 166:    */     catch (IOException e)
/* 167:    */     {
/* 168:170 */       throw new ChannelException(e);
/* 169:    */     }
/* 170:172 */     return this;
/* 171:    */   }
/* 172:    */   
/* 173:    */   public SctpChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 174:    */   {
/* 175:177 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 176:178 */     return this;
/* 177:    */   }
/* 178:    */   
/* 179:    */   public SctpChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 180:    */   {
/* 181:183 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 182:184 */     return this;
/* 183:    */   }
/* 184:    */   
/* 185:    */   public SctpChannelConfig setWriteSpinCount(int writeSpinCount)
/* 186:    */   {
/* 187:189 */     super.setWriteSpinCount(writeSpinCount);
/* 188:190 */     return this;
/* 189:    */   }
/* 190:    */   
/* 191:    */   public SctpChannelConfig setAllocator(ByteBufAllocator allocator)
/* 192:    */   {
/* 193:195 */     super.setAllocator(allocator);
/* 194:196 */     return this;
/* 195:    */   }
/* 196:    */   
/* 197:    */   public SctpChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 198:    */   {
/* 199:201 */     super.setRecvByteBufAllocator(allocator);
/* 200:202 */     return this;
/* 201:    */   }
/* 202:    */   
/* 203:    */   public SctpChannelConfig setAutoRead(boolean autoRead)
/* 204:    */   {
/* 205:207 */     super.setAutoRead(autoRead);
/* 206:208 */     return this;
/* 207:    */   }
/* 208:    */   
/* 209:    */   public SctpChannelConfig setAutoClose(boolean autoClose)
/* 210:    */   {
/* 211:213 */     super.setAutoClose(autoClose);
/* 212:214 */     return this;
/* 213:    */   }
/* 214:    */   
/* 215:    */   public SctpChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 216:    */   {
/* 217:219 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 218:220 */     return this;
/* 219:    */   }
/* 220:    */   
/* 221:    */   public SctpChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 222:    */   {
/* 223:225 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 224:226 */     return this;
/* 225:    */   }
/* 226:    */   
/* 227:    */   public SctpChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 228:    */   {
/* 229:231 */     super.setMessageSizeEstimator(estimator);
/* 230:232 */     return this;
/* 231:    */   }
/* 232:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.sctp.DefaultSctpChannelConfig
 * JD-Core Version:    0.7.0.1
 */