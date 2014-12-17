/*   1:    */ package io.netty.handler.codec.socks;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.util.CharsetUtil;
/*   5:    */ import io.netty.util.NetUtil;
/*   6:    */ import java.net.IDN;
/*   7:    */ 
/*   8:    */ public final class SocksCmdResponse
/*   9:    */   extends SocksResponse
/*  10:    */ {
/*  11:    */   private final SocksCmdStatus cmdStatus;
/*  12:    */   private final SocksAddressType addressType;
/*  13:    */   private final String host;
/*  14:    */   private final int port;
/*  15: 38 */   private static final byte[] DOMAIN_ZEROED = { 0 };
/*  16: 39 */   private static final byte[] IPv4_HOSTNAME_ZEROED = { 0, 0, 0, 0 };
/*  17: 40 */   private static final byte[] IPv6_HOSTNAME_ZEROED = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
/*  18:    */   
/*  19:    */   public SocksCmdResponse(SocksCmdStatus cmdStatus, SocksAddressType addressType)
/*  20:    */   {
/*  21: 46 */     this(cmdStatus, addressType, null, 0);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public SocksCmdResponse(SocksCmdStatus cmdStatus, SocksAddressType addressType, String host, int port)
/*  25:    */   {
/*  26: 63 */     super(SocksResponseType.CMD);
/*  27: 64 */     if (cmdStatus == null) {
/*  28: 65 */       throw new NullPointerException("cmdStatus");
/*  29:    */     }
/*  30: 67 */     if (addressType == null) {
/*  31: 68 */       throw new NullPointerException("addressType");
/*  32:    */     }
/*  33: 70 */     if (host != null)
/*  34:    */     {
/*  35: 71 */       switch (1.$SwitchMap$io$netty$handler$codec$socks$SocksAddressType[addressType.ordinal()])
/*  36:    */       {
/*  37:    */       case 1: 
/*  38: 73 */         if (!NetUtil.isValidIpV4Address(host)) {
/*  39: 74 */           throw new IllegalArgumentException(host + " is not a valid IPv4 address");
/*  40:    */         }
/*  41:    */         break;
/*  42:    */       case 2: 
/*  43: 78 */         if (IDN.toASCII(host).length() > 255) {
/*  44: 79 */           throw new IllegalArgumentException(host + " IDN: " + IDN.toASCII(host) + " exceeds 255 char limit");
/*  45:    */         }
/*  46:    */         break;
/*  47:    */       case 3: 
/*  48: 84 */         if (!NetUtil.isValidIpV6Address(host)) {
/*  49: 85 */           throw new IllegalArgumentException(host + " is not a valid IPv6 address");
/*  50:    */         }
/*  51:    */         break;
/*  52:    */       }
/*  53: 91 */       host = IDN.toASCII(host);
/*  54:    */     }
/*  55: 93 */     if ((port < 0) || (port > 65535)) {
/*  56: 94 */       throw new IllegalArgumentException(port + " is not in bounds 0 <= x <= 65535");
/*  57:    */     }
/*  58: 96 */     this.cmdStatus = cmdStatus;
/*  59: 97 */     this.addressType = addressType;
/*  60: 98 */     this.host = host;
/*  61: 99 */     this.port = port;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public SocksCmdStatus cmdStatus()
/*  65:    */   {
/*  66:108 */     return this.cmdStatus;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public SocksAddressType addressType()
/*  70:    */   {
/*  71:117 */     return this.addressType;
/*  72:    */   }
/*  73:    */   
/*  74:    */   public String host()
/*  75:    */   {
/*  76:129 */     if (this.host != null) {
/*  77:130 */       return IDN.toUnicode(this.host);
/*  78:    */     }
/*  79:132 */     return null;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public int port()
/*  83:    */   {
/*  84:143 */     return this.port;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public void encodeAsByteBuf(ByteBuf byteBuf)
/*  88:    */   {
/*  89:148 */     byteBuf.writeByte(protocolVersion().byteValue());
/*  90:149 */     byteBuf.writeByte(this.cmdStatus.byteValue());
/*  91:150 */     byteBuf.writeByte(0);
/*  92:151 */     byteBuf.writeByte(this.addressType.byteValue());
/*  93:152 */     switch (1.$SwitchMap$io$netty$handler$codec$socks$SocksAddressType[this.addressType.ordinal()])
/*  94:    */     {
/*  95:    */     case 1: 
/*  96:154 */       byte[] hostContent = this.host == null ? IPv4_HOSTNAME_ZEROED : NetUtil.createByteArrayFromIpAddressString(this.host);
/*  97:    */       
/*  98:156 */       byteBuf.writeBytes(hostContent);
/*  99:157 */       byteBuf.writeShort(this.port);
/* 100:158 */       break;
/* 101:    */     case 2: 
/* 102:161 */       byte[] hostContent = this.host == null ? DOMAIN_ZEROED : this.host.getBytes(CharsetUtil.US_ASCII);
/* 103:    */       
/* 104:163 */       byteBuf.writeByte(hostContent.length);
/* 105:164 */       byteBuf.writeBytes(hostContent);
/* 106:165 */       byteBuf.writeShort(this.port);
/* 107:166 */       break;
/* 108:    */     case 3: 
/* 109:169 */       byte[] hostContent = this.host == null ? IPv6_HOSTNAME_ZEROED : NetUtil.createByteArrayFromIpAddressString(this.host);
/* 110:    */       
/* 111:171 */       byteBuf.writeBytes(hostContent);
/* 112:172 */       byteBuf.writeShort(this.port);
/* 113:173 */       break;
/* 114:    */     }
/* 115:    */   }
/* 116:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksCmdResponse
 * JD-Core Version:    0.7.0.1
 */