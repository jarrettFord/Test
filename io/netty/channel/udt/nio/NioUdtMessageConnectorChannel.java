/*   1:    */ package io.netty.channel.udt.nio;
/*   2:    */ 
/*   3:    */ import com.barchart.udt.TypeUDT;
/*   4:    */ import com.barchart.udt.nio.NioSocketUDT;
/*   5:    */ import com.barchart.udt.nio.SocketChannelUDT;
/*   6:    */ import io.netty.buffer.ByteBuf;
/*   7:    */ import io.netty.buffer.ByteBufAllocator;
/*   8:    */ import io.netty.channel.Channel;
/*   9:    */ import io.netty.channel.ChannelException;
/*  10:    */ import io.netty.channel.ChannelMetadata;
/*  11:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  12:    */ import io.netty.channel.nio.AbstractNioMessageChannel;
/*  13:    */ import io.netty.channel.udt.DefaultUdtChannelConfig;
/*  14:    */ import io.netty.channel.udt.UdtChannel;
/*  15:    */ import io.netty.channel.udt.UdtChannelConfig;
/*  16:    */ import io.netty.channel.udt.UdtMessage;
/*  17:    */ import io.netty.util.internal.logging.InternalLogger;
/*  18:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  19:    */ import java.net.InetSocketAddress;
/*  20:    */ import java.net.SocketAddress;
/*  21:    */ import java.nio.channels.SelectionKey;
/*  22:    */ import java.util.List;
/*  23:    */ 
/*  24:    */ public class NioUdtMessageConnectorChannel
/*  25:    */   extends AbstractNioMessageChannel
/*  26:    */   implements UdtChannel
/*  27:    */ {
/*  28: 46 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioUdtMessageConnectorChannel.class);
/*  29: 49 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  30:    */   private final UdtChannelConfig config;
/*  31:    */   
/*  32:    */   public NioUdtMessageConnectorChannel()
/*  33:    */   {
/*  34: 54 */     this(TypeUDT.DATAGRAM);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public NioUdtMessageConnectorChannel(Channel parent, SocketChannelUDT channelUDT)
/*  38:    */   {
/*  39: 58 */     super(parent, channelUDT, 1);
/*  40:    */     try
/*  41:    */     {
/*  42: 60 */       channelUDT.configureBlocking(false);
/*  43: 61 */       switch (1.$SwitchMap$com$barchart$udt$StatusUDT[channelUDT.socketUDT().status().ordinal()])
/*  44:    */       {
/*  45:    */       case 1: 
/*  46:    */       case 2: 
/*  47: 64 */         this.config = new DefaultUdtChannelConfig(this, channelUDT, true);
/*  48: 65 */         break;
/*  49:    */       default: 
/*  50: 67 */         this.config = new DefaultUdtChannelConfig(this, channelUDT, false);
/*  51:    */       }
/*  52:    */     }
/*  53:    */     catch (Exception e)
/*  54:    */     {
/*  55:    */       try
/*  56:    */       {
/*  57: 72 */         channelUDT.close();
/*  58:    */       }
/*  59:    */       catch (Exception e2)
/*  60:    */       {
/*  61: 74 */         if (logger.isWarnEnabled()) {
/*  62: 75 */           logger.warn("Failed to close channel.", e2);
/*  63:    */         }
/*  64:    */       }
/*  65: 78 */       throw new ChannelException("Failed to configure channel.", e);
/*  66:    */     }
/*  67:    */   }
/*  68:    */   
/*  69:    */   public NioUdtMessageConnectorChannel(SocketChannelUDT channelUDT)
/*  70:    */   {
/*  71: 83 */     this(null, channelUDT);
/*  72:    */   }
/*  73:    */   
/*  74:    */   public NioUdtMessageConnectorChannel(TypeUDT type)
/*  75:    */   {
/*  76: 87 */     this(NioUdtProvider.newConnectorChannelUDT(type));
/*  77:    */   }
/*  78:    */   
/*  79:    */   public UdtChannelConfig config()
/*  80:    */   {
/*  81: 92 */     return this.config;
/*  82:    */   }
/*  83:    */   
/*  84:    */   protected void doBind(SocketAddress localAddress)
/*  85:    */     throws Exception
/*  86:    */   {
/*  87: 97 */     javaChannel().bind(localAddress);
/*  88:    */   }
/*  89:    */   
/*  90:    */   protected void doClose()
/*  91:    */     throws Exception
/*  92:    */   {
/*  93:102 */     javaChannel().close();
/*  94:    */   }
/*  95:    */   
/*  96:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/*  97:    */     throws Exception
/*  98:    */   {
/*  99:108 */     doBind(localAddress != null ? localAddress : new InetSocketAddress(0));
/* 100:109 */     boolean success = false;
/* 101:    */     try
/* 102:    */     {
/* 103:111 */       boolean connected = javaChannel().connect(remoteAddress);
/* 104:112 */       if (!connected) {
/* 105:113 */         selectionKey().interestOps(selectionKey().interestOps() | 0x8);
/* 106:    */       }
/* 107:116 */       success = true;
/* 108:117 */       return connected;
/* 109:    */     }
/* 110:    */     finally
/* 111:    */     {
/* 112:119 */       if (!success) {
/* 113:120 */         doClose();
/* 114:    */       }
/* 115:    */     }
/* 116:    */   }
/* 117:    */   
/* 118:    */   protected void doDisconnect()
/* 119:    */     throws Exception
/* 120:    */   {
/* 121:127 */     doClose();
/* 122:    */   }
/* 123:    */   
/* 124:    */   protected void doFinishConnect()
/* 125:    */     throws Exception
/* 126:    */   {
/* 127:132 */     if (javaChannel().finishConnect()) {
/* 128:133 */       selectionKey().interestOps(selectionKey().interestOps() & 0xFFFFFFF7);
/* 129:    */     } else {
/* 130:136 */       throw new Error("Provider error: failed to finish connect. Provider library should be upgraded.");
/* 131:    */     }
/* 132:    */   }
/* 133:    */   
/* 134:    */   protected int doReadMessages(List<Object> buf)
/* 135:    */     throws Exception
/* 136:    */   {
/* 137:144 */     int maximumMessageSize = this.config.getReceiveBufferSize();
/* 138:    */     
/* 139:146 */     ByteBuf byteBuf = this.config.getAllocator().directBuffer(maximumMessageSize);
/* 140:    */     
/* 141:    */ 
/* 142:149 */     int receivedMessageSize = byteBuf.writeBytes(javaChannel(), maximumMessageSize);
/* 143:152 */     if (receivedMessageSize <= 0)
/* 144:    */     {
/* 145:153 */       byteBuf.release();
/* 146:154 */       return 0;
/* 147:    */     }
/* 148:157 */     if (receivedMessageSize >= maximumMessageSize)
/* 149:    */     {
/* 150:158 */       javaChannel().close();
/* 151:159 */       throw new ChannelException("Invalid config : increase receive buffer size to avoid message truncation");
/* 152:    */     }
/* 153:164 */     buf.add(new UdtMessage(byteBuf));
/* 154:    */     
/* 155:166 */     return 1;
/* 156:    */   }
/* 157:    */   
/* 158:    */   protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in)
/* 159:    */     throws Exception
/* 160:    */   {
/* 161:172 */     UdtMessage message = (UdtMessage)msg;
/* 162:    */     
/* 163:174 */     ByteBuf byteBuf = message.content();
/* 164:    */     
/* 165:176 */     int messageSize = byteBuf.readableBytes();
/* 166:    */     long writtenBytes;
/* 167:    */     long writtenBytes;
/* 168:179 */     if (byteBuf.nioBufferCount() == 1) {
/* 169:180 */       writtenBytes = javaChannel().write(byteBuf.nioBuffer());
/* 170:    */     } else {
/* 171:182 */       writtenBytes = javaChannel().write(byteBuf.nioBuffers());
/* 172:    */     }
/* 173:186 */     if ((writtenBytes <= 0L) && (messageSize > 0)) {
/* 174:187 */       return false;
/* 175:    */     }
/* 176:191 */     if (writtenBytes != messageSize) {
/* 177:192 */       throw new Error("Provider error: failed to write message. Provider library should be upgraded.");
/* 178:    */     }
/* 179:196 */     return true;
/* 180:    */   }
/* 181:    */   
/* 182:    */   public boolean isActive()
/* 183:    */   {
/* 184:201 */     SocketChannelUDT channelUDT = javaChannel();
/* 185:202 */     return (channelUDT.isOpen()) && (channelUDT.isConnectFinished());
/* 186:    */   }
/* 187:    */   
/* 188:    */   protected SocketChannelUDT javaChannel()
/* 189:    */   {
/* 190:207 */     return (SocketChannelUDT)super.javaChannel();
/* 191:    */   }
/* 192:    */   
/* 193:    */   protected SocketAddress localAddress0()
/* 194:    */   {
/* 195:212 */     return javaChannel().socket().getLocalSocketAddress();
/* 196:    */   }
/* 197:    */   
/* 198:    */   public ChannelMetadata metadata()
/* 199:    */   {
/* 200:217 */     return METADATA;
/* 201:    */   }
/* 202:    */   
/* 203:    */   protected SocketAddress remoteAddress0()
/* 204:    */   {
/* 205:222 */     return javaChannel().socket().getRemoteSocketAddress();
/* 206:    */   }
/* 207:    */   
/* 208:    */   public InetSocketAddress localAddress()
/* 209:    */   {
/* 210:227 */     return (InetSocketAddress)super.localAddress();
/* 211:    */   }
/* 212:    */   
/* 213:    */   public InetSocketAddress remoteAddress()
/* 214:    */   {
/* 215:232 */     return (InetSocketAddress)super.remoteAddress();
/* 216:    */   }
/* 217:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.udt.nio.NioUdtMessageConnectorChannel
 * JD-Core Version:    0.7.0.1
 */