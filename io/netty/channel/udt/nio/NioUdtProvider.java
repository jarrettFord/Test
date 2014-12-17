/*   1:    */ package io.netty.channel.udt.nio;
/*   2:    */ 
/*   3:    */ import com.barchart.udt.SocketUDT;
/*   4:    */ import com.barchart.udt.TypeUDT;
/*   5:    */ import com.barchart.udt.nio.ChannelUDT;
/*   6:    */ import com.barchart.udt.nio.KindUDT;
/*   7:    */ import com.barchart.udt.nio.RendezvousChannelUDT;
/*   8:    */ import com.barchart.udt.nio.SelectorProviderUDT;
/*   9:    */ import com.barchart.udt.nio.ServerSocketChannelUDT;
/*  10:    */ import com.barchart.udt.nio.SocketChannelUDT;
/*  11:    */ import io.netty.bootstrap.ChannelFactory;
/*  12:    */ import io.netty.channel.Channel;
/*  13:    */ import io.netty.channel.ChannelException;
/*  14:    */ import io.netty.channel.udt.UdtChannel;
/*  15:    */ import io.netty.channel.udt.UdtServerChannel;
/*  16:    */ import java.io.IOException;
/*  17:    */ import java.nio.channels.spi.SelectorProvider;
/*  18:    */ 
/*  19:    */ public final class NioUdtProvider<T extends UdtChannel>
/*  20:    */   implements ChannelFactory<T>
/*  21:    */ {
/*  22: 48 */   public static final ChannelFactory<UdtServerChannel> BYTE_ACCEPTOR = new NioUdtProvider(TypeUDT.STREAM, KindUDT.ACCEPTOR);
/*  23: 55 */   public static final ChannelFactory<UdtChannel> BYTE_CONNECTOR = new NioUdtProvider(TypeUDT.STREAM, KindUDT.CONNECTOR);
/*  24: 62 */   public static final SelectorProvider BYTE_PROVIDER = SelectorProviderUDT.STREAM;
/*  25: 68 */   public static final ChannelFactory<UdtChannel> BYTE_RENDEZVOUS = new NioUdtProvider(TypeUDT.STREAM, KindUDT.RENDEZVOUS);
/*  26: 75 */   public static final ChannelFactory<UdtServerChannel> MESSAGE_ACCEPTOR = new NioUdtProvider(TypeUDT.DATAGRAM, KindUDT.ACCEPTOR);
/*  27: 82 */   public static final ChannelFactory<UdtChannel> MESSAGE_CONNECTOR = new NioUdtProvider(TypeUDT.DATAGRAM, KindUDT.CONNECTOR);
/*  28: 89 */   public static final SelectorProvider MESSAGE_PROVIDER = SelectorProviderUDT.DATAGRAM;
/*  29: 95 */   public static final ChannelFactory<UdtChannel> MESSAGE_RENDEZVOUS = new NioUdtProvider(TypeUDT.DATAGRAM, KindUDT.RENDEZVOUS);
/*  30:    */   private final KindUDT kind;
/*  31:    */   private final TypeUDT type;
/*  32:    */   
/*  33:    */   public static ChannelUDT channelUDT(Channel channel)
/*  34:    */   {
/*  35:106 */     if ((channel instanceof NioUdtByteAcceptorChannel)) {
/*  36:107 */       return ((NioUdtByteAcceptorChannel)channel).javaChannel();
/*  37:    */     }
/*  38:109 */     if ((channel instanceof NioUdtByteConnectorChannel)) {
/*  39:110 */       return ((NioUdtByteConnectorChannel)channel).javaChannel();
/*  40:    */     }
/*  41:112 */     if ((channel instanceof NioUdtByteRendezvousChannel)) {
/*  42:113 */       return ((NioUdtByteRendezvousChannel)channel).javaChannel();
/*  43:    */     }
/*  44:116 */     if ((channel instanceof NioUdtMessageAcceptorChannel)) {
/*  45:117 */       return ((NioUdtMessageAcceptorChannel)channel).javaChannel();
/*  46:    */     }
/*  47:119 */     if ((channel instanceof NioUdtMessageConnectorChannel)) {
/*  48:120 */       return ((NioUdtMessageConnectorChannel)channel).javaChannel();
/*  49:    */     }
/*  50:122 */     if ((channel instanceof NioUdtMessageRendezvousChannel)) {
/*  51:123 */       return ((NioUdtMessageRendezvousChannel)channel).javaChannel();
/*  52:    */     }
/*  53:125 */     return null;
/*  54:    */   }
/*  55:    */   
/*  56:    */   protected static ServerSocketChannelUDT newAcceptorChannelUDT(TypeUDT type)
/*  57:    */   {
/*  58:    */     try
/*  59:    */     {
/*  60:134 */       return SelectorProviderUDT.from(type).openServerSocketChannel();
/*  61:    */     }
/*  62:    */     catch (IOException e)
/*  63:    */     {
/*  64:136 */       throw new ChannelException("Failed to open channel");
/*  65:    */     }
/*  66:    */   }
/*  67:    */   
/*  68:    */   protected static SocketChannelUDT newConnectorChannelUDT(TypeUDT type)
/*  69:    */   {
/*  70:    */     try
/*  71:    */     {
/*  72:145 */       return SelectorProviderUDT.from(type).openSocketChannel();
/*  73:    */     }
/*  74:    */     catch (IOException e)
/*  75:    */     {
/*  76:147 */       throw new ChannelException("Failed to open channel");
/*  77:    */     }
/*  78:    */   }
/*  79:    */   
/*  80:    */   protected static RendezvousChannelUDT newRendezvousChannelUDT(TypeUDT type)
/*  81:    */   {
/*  82:    */     try
/*  83:    */     {
/*  84:157 */       return SelectorProviderUDT.from(type).openRendezvousChannel();
/*  85:    */     }
/*  86:    */     catch (IOException e)
/*  87:    */     {
/*  88:159 */       throw new ChannelException("Failed to open channel");
/*  89:    */     }
/*  90:    */   }
/*  91:    */   
/*  92:    */   public static SocketUDT socketUDT(Channel channel)
/*  93:    */   {
/*  94:170 */     ChannelUDT channelUDT = channelUDT(channel);
/*  95:171 */     if (channelUDT == null) {
/*  96:172 */       return null;
/*  97:    */     }
/*  98:174 */     return channelUDT.socketUDT();
/*  99:    */   }
/* 100:    */   
/* 101:    */   private NioUdtProvider(TypeUDT type, KindUDT kind)
/* 102:    */   {
/* 103:185 */     this.type = type;
/* 104:186 */     this.kind = kind;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public KindUDT kind()
/* 108:    */   {
/* 109:193 */     return this.kind;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public T newChannel()
/* 113:    */   {
/* 114:203 */     switch (1.$SwitchMap$com$barchart$udt$nio$KindUDT[this.kind.ordinal()])
/* 115:    */     {
/* 116:    */     case 1: 
/* 117:205 */       switch (this.type)
/* 118:    */       {
/* 119:    */       case DATAGRAM: 
/* 120:207 */         return new NioUdtMessageAcceptorChannel();
/* 121:    */       case STREAM: 
/* 122:209 */         return new NioUdtByteAcceptorChannel();
/* 123:    */       }
/* 124:211 */       throw new IllegalStateException("wrong type=" + this.type);
/* 125:    */     case 2: 
/* 126:214 */       switch (this.type)
/* 127:    */       {
/* 128:    */       case DATAGRAM: 
/* 129:216 */         return new NioUdtMessageConnectorChannel();
/* 130:    */       case STREAM: 
/* 131:218 */         return new NioUdtByteConnectorChannel();
/* 132:    */       }
/* 133:220 */       throw new IllegalStateException("wrong type=" + this.type);
/* 134:    */     case 3: 
/* 135:223 */       switch (this.type)
/* 136:    */       {
/* 137:    */       case DATAGRAM: 
/* 138:225 */         return new NioUdtMessageRendezvousChannel();
/* 139:    */       case STREAM: 
/* 140:227 */         return new NioUdtByteRendezvousChannel();
/* 141:    */       }
/* 142:229 */       throw new IllegalStateException("wrong type=" + this.type);
/* 143:    */     }
/* 144:232 */     throw new IllegalStateException("wrong kind=" + this.kind);
/* 145:    */   }
/* 146:    */   
/* 147:    */   public TypeUDT type()
/* 148:    */   {
/* 149:240 */     return this.type;
/* 150:    */   }
/* 151:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.udt.nio.NioUdtProvider
 * JD-Core Version:    0.7.0.1
 */