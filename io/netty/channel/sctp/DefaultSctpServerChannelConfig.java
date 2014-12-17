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
/*  11:    */ import io.netty.util.NetUtil;
/*  12:    */ import java.io.IOException;
/*  13:    */ import java.util.Map;
/*  14:    */ 
/*  15:    */ public class DefaultSctpServerChannelConfig
/*  16:    */   extends DefaultChannelConfig
/*  17:    */   implements SctpServerChannelConfig
/*  18:    */ {
/*  19:    */   private final com.sun.nio.sctp.SctpServerChannel javaChannel;
/*  20: 38 */   private volatile int backlog = NetUtil.SOMAXCONN;
/*  21:    */   
/*  22:    */   public DefaultSctpServerChannelConfig(SctpServerChannel channel, com.sun.nio.sctp.SctpServerChannel javaChannel)
/*  23:    */   {
/*  24: 45 */     super(channel);
/*  25: 46 */     if (javaChannel == null) {
/*  26: 47 */       throw new NullPointerException("javaChannel");
/*  27:    */     }
/*  28: 49 */     this.javaChannel = javaChannel;
/*  29:    */   }
/*  30:    */   
/*  31:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  32:    */   {
/*  33: 54 */     return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, SctpChannelOption.SCTP_INIT_MAXSTREAMS });
/*  34:    */   }
/*  35:    */   
/*  36:    */   public <T> T getOption(ChannelOption<T> option)
/*  37:    */   {
/*  38: 62 */     if (option == ChannelOption.SO_RCVBUF) {
/*  39: 63 */       return Integer.valueOf(getReceiveBufferSize());
/*  40:    */     }
/*  41: 65 */     if (option == ChannelOption.SO_SNDBUF) {
/*  42: 66 */       return Integer.valueOf(getSendBufferSize());
/*  43:    */     }
/*  44: 68 */     return super.getOption(option);
/*  45:    */   }
/*  46:    */   
/*  47:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  48:    */   {
/*  49: 73 */     validate(option, value);
/*  50: 75 */     if (option == ChannelOption.SO_RCVBUF) {
/*  51: 76 */       setReceiveBufferSize(((Integer)value).intValue());
/*  52: 77 */     } else if (option == ChannelOption.SO_SNDBUF) {
/*  53: 78 */       setSendBufferSize(((Integer)value).intValue());
/*  54: 79 */     } else if (option == SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS) {
/*  55: 80 */       setInitMaxStreams((SctpStandardSocketOptions.InitMaxStreams)value);
/*  56:    */     } else {
/*  57: 82 */       return super.setOption(option, value);
/*  58:    */     }
/*  59: 85 */     return true;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public int getSendBufferSize()
/*  63:    */   {
/*  64:    */     try
/*  65:    */     {
/*  66: 91 */       return ((Integer)this.javaChannel.getOption(SctpStandardSocketOptions.SO_SNDBUF)).intValue();
/*  67:    */     }
/*  68:    */     catch (IOException e)
/*  69:    */     {
/*  70: 93 */       throw new ChannelException(e);
/*  71:    */     }
/*  72:    */   }
/*  73:    */   
/*  74:    */   public SctpServerChannelConfig setSendBufferSize(int sendBufferSize)
/*  75:    */   {
/*  76:    */     try
/*  77:    */     {
/*  78:100 */       this.javaChannel.setOption(SctpStandardSocketOptions.SO_SNDBUF, Integer.valueOf(sendBufferSize));
/*  79:    */     }
/*  80:    */     catch (IOException e)
/*  81:    */     {
/*  82:102 */       throw new ChannelException(e);
/*  83:    */     }
/*  84:104 */     return this;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public int getReceiveBufferSize()
/*  88:    */   {
/*  89:    */     try
/*  90:    */     {
/*  91:110 */       return ((Integer)this.javaChannel.getOption(SctpStandardSocketOptions.SO_RCVBUF)).intValue();
/*  92:    */     }
/*  93:    */     catch (IOException e)
/*  94:    */     {
/*  95:112 */       throw new ChannelException(e);
/*  96:    */     }
/*  97:    */   }
/*  98:    */   
/*  99:    */   public SctpServerChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 100:    */   {
/* 101:    */     try
/* 102:    */     {
/* 103:119 */       this.javaChannel.setOption(SctpStandardSocketOptions.SO_RCVBUF, Integer.valueOf(receiveBufferSize));
/* 104:    */     }
/* 105:    */     catch (IOException e)
/* 106:    */     {
/* 107:121 */       throw new ChannelException(e);
/* 108:    */     }
/* 109:123 */     return this;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public SctpStandardSocketOptions.InitMaxStreams getInitMaxStreams()
/* 113:    */   {
/* 114:    */     try
/* 115:    */     {
/* 116:129 */       return (SctpStandardSocketOptions.InitMaxStreams)this.javaChannel.getOption(SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS);
/* 117:    */     }
/* 118:    */     catch (IOException e)
/* 119:    */     {
/* 120:131 */       throw new ChannelException(e);
/* 121:    */     }
/* 122:    */   }
/* 123:    */   
/* 124:    */   public SctpServerChannelConfig setInitMaxStreams(SctpStandardSocketOptions.InitMaxStreams initMaxStreams)
/* 125:    */   {
/* 126:    */     try
/* 127:    */     {
/* 128:138 */       this.javaChannel.setOption(SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS, initMaxStreams);
/* 129:    */     }
/* 130:    */     catch (IOException e)
/* 131:    */     {
/* 132:140 */       throw new ChannelException(e);
/* 133:    */     }
/* 134:142 */     return this;
/* 135:    */   }
/* 136:    */   
/* 137:    */   public int getBacklog()
/* 138:    */   {
/* 139:147 */     return this.backlog;
/* 140:    */   }
/* 141:    */   
/* 142:    */   public SctpServerChannelConfig setBacklog(int backlog)
/* 143:    */   {
/* 144:152 */     if (backlog < 0) {
/* 145:153 */       throw new IllegalArgumentException("backlog: " + backlog);
/* 146:    */     }
/* 147:155 */     this.backlog = backlog;
/* 148:156 */     return this;
/* 149:    */   }
/* 150:    */   
/* 151:    */   public SctpServerChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 152:    */   {
/* 153:161 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 154:162 */     return this;
/* 155:    */   }
/* 156:    */   
/* 157:    */   public SctpServerChannelConfig setWriteSpinCount(int writeSpinCount)
/* 158:    */   {
/* 159:167 */     super.setWriteSpinCount(writeSpinCount);
/* 160:168 */     return this;
/* 161:    */   }
/* 162:    */   
/* 163:    */   public SctpServerChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 164:    */   {
/* 165:173 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 166:174 */     return this;
/* 167:    */   }
/* 168:    */   
/* 169:    */   public SctpServerChannelConfig setAllocator(ByteBufAllocator allocator)
/* 170:    */   {
/* 171:179 */     super.setAllocator(allocator);
/* 172:180 */     return this;
/* 173:    */   }
/* 174:    */   
/* 175:    */   public SctpServerChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 176:    */   {
/* 177:185 */     super.setRecvByteBufAllocator(allocator);
/* 178:186 */     return this;
/* 179:    */   }
/* 180:    */   
/* 181:    */   public SctpServerChannelConfig setAutoRead(boolean autoRead)
/* 182:    */   {
/* 183:191 */     super.setAutoRead(autoRead);
/* 184:192 */     return this;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public SctpServerChannelConfig setAutoClose(boolean autoClose)
/* 188:    */   {
/* 189:197 */     super.setAutoClose(autoClose);
/* 190:198 */     return this;
/* 191:    */   }
/* 192:    */   
/* 193:    */   public SctpServerChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 194:    */   {
/* 195:203 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 196:204 */     return this;
/* 197:    */   }
/* 198:    */   
/* 199:    */   public SctpServerChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 200:    */   {
/* 201:209 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 202:210 */     return this;
/* 203:    */   }
/* 204:    */   
/* 205:    */   public SctpServerChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 206:    */   {
/* 207:215 */     super.setMessageSizeEstimator(estimator);
/* 208:216 */     return this;
/* 209:    */   }
/* 210:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.sctp.DefaultSctpServerChannelConfig
 * JD-Core Version:    0.7.0.1
 */