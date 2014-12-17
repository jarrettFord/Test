/*   1:    */ package io.netty.channel.udt.nio;
/*   2:    */ 
/*   3:    */ import com.barchart.udt.TypeUDT;
/*   4:    */ import com.barchart.udt.nio.NioSocketUDT;
/*   5:    */ import com.barchart.udt.nio.SocketChannelUDT;
/*   6:    */ import io.netty.buffer.ByteBuf;
/*   7:    */ import io.netty.channel.Channel;
/*   8:    */ import io.netty.channel.ChannelException;
/*   9:    */ import io.netty.channel.ChannelMetadata;
/*  10:    */ import io.netty.channel.FileRegion;
/*  11:    */ import io.netty.channel.nio.AbstractNioByteChannel;
/*  12:    */ import io.netty.channel.udt.DefaultUdtChannelConfig;
/*  13:    */ import io.netty.channel.udt.UdtChannel;
/*  14:    */ import io.netty.channel.udt.UdtChannelConfig;
/*  15:    */ import io.netty.util.internal.logging.InternalLogger;
/*  16:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  17:    */ import java.net.InetSocketAddress;
/*  18:    */ import java.net.SocketAddress;
/*  19:    */ import java.nio.channels.SelectionKey;
/*  20:    */ 
/*  21:    */ public class NioUdtByteConnectorChannel
/*  22:    */   extends AbstractNioByteChannel
/*  23:    */   implements UdtChannel
/*  24:    */ {
/*  25: 42 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioUdtByteConnectorChannel.class);
/*  26: 45 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  27:    */   private final UdtChannelConfig config;
/*  28:    */   
/*  29:    */   public NioUdtByteConnectorChannel()
/*  30:    */   {
/*  31: 50 */     this(TypeUDT.STREAM);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public NioUdtByteConnectorChannel(Channel parent, SocketChannelUDT channelUDT)
/*  35:    */   {
/*  36: 54 */     super(parent, channelUDT);
/*  37:    */     try
/*  38:    */     {
/*  39: 56 */       channelUDT.configureBlocking(false);
/*  40: 57 */       switch (1.$SwitchMap$com$barchart$udt$StatusUDT[channelUDT.socketUDT().status().ordinal()])
/*  41:    */       {
/*  42:    */       case 1: 
/*  43:    */       case 2: 
/*  44: 60 */         this.config = new DefaultUdtChannelConfig(this, channelUDT, true);
/*  45: 61 */         break;
/*  46:    */       default: 
/*  47: 63 */         this.config = new DefaultUdtChannelConfig(this, channelUDT, false);
/*  48:    */       }
/*  49:    */     }
/*  50:    */     catch (Exception e)
/*  51:    */     {
/*  52:    */       try
/*  53:    */       {
/*  54: 68 */         channelUDT.close();
/*  55:    */       }
/*  56:    */       catch (Exception e2)
/*  57:    */       {
/*  58: 70 */         if (logger.isWarnEnabled()) {
/*  59: 71 */           logger.warn("Failed to close channel.", e2);
/*  60:    */         }
/*  61:    */       }
/*  62: 74 */       throw new ChannelException("Failed to configure channel.", e);
/*  63:    */     }
/*  64:    */   }
/*  65:    */   
/*  66:    */   public NioUdtByteConnectorChannel(SocketChannelUDT channelUDT)
/*  67:    */   {
/*  68: 79 */     this(null, channelUDT);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public NioUdtByteConnectorChannel(TypeUDT type)
/*  72:    */   {
/*  73: 83 */     this(NioUdtProvider.newConnectorChannelUDT(type));
/*  74:    */   }
/*  75:    */   
/*  76:    */   public UdtChannelConfig config()
/*  77:    */   {
/*  78: 88 */     return this.config;
/*  79:    */   }
/*  80:    */   
/*  81:    */   protected void doBind(SocketAddress localAddress)
/*  82:    */     throws Exception
/*  83:    */   {
/*  84: 93 */     javaChannel().bind(localAddress);
/*  85:    */   }
/*  86:    */   
/*  87:    */   protected void doClose()
/*  88:    */     throws Exception
/*  89:    */   {
/*  90: 98 */     javaChannel().close();
/*  91:    */   }
/*  92:    */   
/*  93:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/*  94:    */     throws Exception
/*  95:    */   {
/*  96:104 */     doBind(localAddress != null ? localAddress : new InetSocketAddress(0));
/*  97:105 */     boolean success = false;
/*  98:    */     try
/*  99:    */     {
/* 100:107 */       boolean connected = javaChannel().connect(remoteAddress);
/* 101:108 */       if (!connected) {
/* 102:109 */         selectionKey().interestOps(selectionKey().interestOps() | 0x8);
/* 103:    */       }
/* 104:112 */       success = true;
/* 105:113 */       return connected;
/* 106:    */     }
/* 107:    */     finally
/* 108:    */     {
/* 109:115 */       if (!success) {
/* 110:116 */         doClose();
/* 111:    */       }
/* 112:    */     }
/* 113:    */   }
/* 114:    */   
/* 115:    */   protected void doDisconnect()
/* 116:    */     throws Exception
/* 117:    */   {
/* 118:123 */     doClose();
/* 119:    */   }
/* 120:    */   
/* 121:    */   protected void doFinishConnect()
/* 122:    */     throws Exception
/* 123:    */   {
/* 124:128 */     if (javaChannel().finishConnect()) {
/* 125:129 */       selectionKey().interestOps(selectionKey().interestOps() & 0xFFFFFFF7);
/* 126:    */     } else {
/* 127:132 */       throw new Error("Provider error: failed to finish connect. Provider library should be upgraded.");
/* 128:    */     }
/* 129:    */   }
/* 130:    */   
/* 131:    */   protected int doReadBytes(ByteBuf byteBuf)
/* 132:    */     throws Exception
/* 133:    */   {
/* 134:139 */     return byteBuf.writeBytes(javaChannel(), byteBuf.writableBytes());
/* 135:    */   }
/* 136:    */   
/* 137:    */   protected int doWriteBytes(ByteBuf byteBuf)
/* 138:    */     throws Exception
/* 139:    */   {
/* 140:144 */     int expectedWrittenBytes = byteBuf.readableBytes();
/* 141:145 */     int writtenBytes = byteBuf.readBytes(javaChannel(), expectedWrittenBytes);
/* 142:146 */     return writtenBytes;
/* 143:    */   }
/* 144:    */   
/* 145:    */   protected long doWriteFileRegion(FileRegion region)
/* 146:    */     throws Exception
/* 147:    */   {
/* 148:151 */     throw new UnsupportedOperationException();
/* 149:    */   }
/* 150:    */   
/* 151:    */   public boolean isActive()
/* 152:    */   {
/* 153:156 */     SocketChannelUDT channelUDT = javaChannel();
/* 154:157 */     return (channelUDT.isOpen()) && (channelUDT.isConnectFinished());
/* 155:    */   }
/* 156:    */   
/* 157:    */   protected SocketChannelUDT javaChannel()
/* 158:    */   {
/* 159:162 */     return (SocketChannelUDT)super.javaChannel();
/* 160:    */   }
/* 161:    */   
/* 162:    */   protected SocketAddress localAddress0()
/* 163:    */   {
/* 164:167 */     return javaChannel().socket().getLocalSocketAddress();
/* 165:    */   }
/* 166:    */   
/* 167:    */   public ChannelMetadata metadata()
/* 168:    */   {
/* 169:172 */     return METADATA;
/* 170:    */   }
/* 171:    */   
/* 172:    */   protected SocketAddress remoteAddress0()
/* 173:    */   {
/* 174:177 */     return javaChannel().socket().getRemoteSocketAddress();
/* 175:    */   }
/* 176:    */   
/* 177:    */   public InetSocketAddress localAddress()
/* 178:    */   {
/* 179:182 */     return (InetSocketAddress)super.localAddress();
/* 180:    */   }
/* 181:    */   
/* 182:    */   public InetSocketAddress remoteAddress()
/* 183:    */   {
/* 184:187 */     return (InetSocketAddress)super.remoteAddress();
/* 185:    */   }
/* 186:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.udt.nio.NioUdtByteConnectorChannel
 * JD-Core Version:    0.7.0.1
 */