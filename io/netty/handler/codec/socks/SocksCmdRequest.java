/*   1:    */ package io.netty.handler.codec.socks;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.util.CharsetUtil;
/*   5:    */ import io.netty.util.NetUtil;
/*   6:    */ import java.net.IDN;
/*   7:    */ 
/*   8:    */ public final class SocksCmdRequest
/*   9:    */   extends SocksRequest
/*  10:    */ {
/*  11:    */   private final SocksCmdType cmdType;
/*  12:    */   private final SocksAddressType addressType;
/*  13:    */   private final String host;
/*  14:    */   private final int port;
/*  15:    */   
/*  16:    */   public SocksCmdRequest(SocksCmdType cmdType, SocksAddressType addressType, String host, int port)
/*  17:    */   {
/*  18: 37 */     super(SocksRequestType.CMD);
/*  19: 38 */     if (cmdType == null) {
/*  20: 39 */       throw new NullPointerException("cmdType");
/*  21:    */     }
/*  22: 41 */     if (addressType == null) {
/*  23: 42 */       throw new NullPointerException("addressType");
/*  24:    */     }
/*  25: 44 */     if (host == null) {
/*  26: 45 */       throw new NullPointerException("host");
/*  27:    */     }
/*  28: 47 */     switch (1.$SwitchMap$io$netty$handler$codec$socks$SocksAddressType[addressType.ordinal()])
/*  29:    */     {
/*  30:    */     case 1: 
/*  31: 49 */       if (!NetUtil.isValidIpV4Address(host)) {
/*  32: 50 */         throw new IllegalArgumentException(host + " is not a valid IPv4 address");
/*  33:    */       }
/*  34:    */       break;
/*  35:    */     case 2: 
/*  36: 54 */       if (IDN.toASCII(host).length() > 255) {
/*  37: 55 */         throw new IllegalArgumentException(host + " IDN: " + IDN.toASCII(host) + " exceeds 255 char limit");
/*  38:    */       }
/*  39:    */       break;
/*  40:    */     case 3: 
/*  41: 59 */       if (!NetUtil.isValidIpV6Address(host)) {
/*  42: 60 */         throw new IllegalArgumentException(host + " is not a valid IPv6 address");
/*  43:    */       }
/*  44:    */       break;
/*  45:    */     }
/*  46: 66 */     if ((port <= 0) || (port >= 65536)) {
/*  47: 67 */       throw new IllegalArgumentException(port + " is not in bounds 0 < x < 65536");
/*  48:    */     }
/*  49: 69 */     this.cmdType = cmdType;
/*  50: 70 */     this.addressType = addressType;
/*  51: 71 */     this.host = IDN.toASCII(host);
/*  52: 72 */     this.port = port;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public SocksCmdType cmdType()
/*  56:    */   {
/*  57: 81 */     return this.cmdType;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public SocksAddressType addressType()
/*  61:    */   {
/*  62: 90 */     return this.addressType;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public String host()
/*  66:    */   {
/*  67: 99 */     return IDN.toUnicode(this.host);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public int port()
/*  71:    */   {
/*  72:108 */     return this.port;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public void encodeAsByteBuf(ByteBuf byteBuf)
/*  76:    */   {
/*  77:113 */     byteBuf.writeByte(protocolVersion().byteValue());
/*  78:114 */     byteBuf.writeByte(this.cmdType.byteValue());
/*  79:115 */     byteBuf.writeByte(0);
/*  80:116 */     byteBuf.writeByte(this.addressType.byteValue());
/*  81:117 */     switch (1.$SwitchMap$io$netty$handler$codec$socks$SocksAddressType[this.addressType.ordinal()])
/*  82:    */     {
/*  83:    */     case 1: 
/*  84:119 */       byteBuf.writeBytes(NetUtil.createByteArrayFromIpAddressString(this.host));
/*  85:120 */       byteBuf.writeShort(this.port);
/*  86:121 */       break;
/*  87:    */     case 2: 
/*  88:125 */       byteBuf.writeByte(this.host.length());
/*  89:126 */       byteBuf.writeBytes(this.host.getBytes(CharsetUtil.US_ASCII));
/*  90:127 */       byteBuf.writeShort(this.port);
/*  91:128 */       break;
/*  92:    */     case 3: 
/*  93:132 */       byteBuf.writeBytes(NetUtil.createByteArrayFromIpAddressString(this.host));
/*  94:133 */       byteBuf.writeShort(this.port);
/*  95:    */     }
/*  96:    */   }
/*  97:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksCmdRequest
 * JD-Core Version:    0.7.0.1
 */