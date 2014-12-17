/*   1:    */ package io.netty.handler.codec.socks;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.channel.ChannelPipeline;
/*   6:    */ import io.netty.handler.codec.ReplayingDecoder;
/*   7:    */ import io.netty.util.CharsetUtil;
/*   8:    */ import java.util.List;
/*   9:    */ 
/*  10:    */ public class SocksCmdResponseDecoder
/*  11:    */   extends ReplayingDecoder<State>
/*  12:    */ {
/*  13:    */   private static final String name = "SOCKS_CMD_RESPONSE_DECODER";
/*  14:    */   private SocksProtocolVersion version;
/*  15:    */   private int fieldLength;
/*  16:    */   private SocksCmdStatus cmdStatus;
/*  17:    */   private SocksAddressType addressType;
/*  18:    */   private byte reserved;
/*  19:    */   private String host;
/*  20:    */   private int port;
/*  21:    */   
/*  22:    */   @Deprecated
/*  23:    */   public static String getName()
/*  24:    */   {
/*  25: 37 */     return "SOCKS_CMD_RESPONSE_DECODER";
/*  26:    */   }
/*  27:    */   
/*  28: 47 */   private SocksResponse msg = SocksCommonUtils.UNKNOWN_SOCKS_RESPONSE;
/*  29:    */   
/*  30:    */   public SocksCmdResponseDecoder()
/*  31:    */   {
/*  32: 50 */     super(State.CHECK_PROTOCOL_VERSION);
/*  33:    */   }
/*  34:    */   
/*  35:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out)
/*  36:    */     throws Exception
/*  37:    */   {
/*  38: 55 */     switch (1.$SwitchMap$io$netty$handler$codec$socks$SocksCmdResponseDecoder$State[((State)state()).ordinal()])
/*  39:    */     {
/*  40:    */     case 1: 
/*  41: 57 */       this.version = SocksProtocolVersion.valueOf(byteBuf.readByte());
/*  42: 58 */       if (this.version == SocksProtocolVersion.SOCKS5) {
/*  43: 61 */         checkpoint(State.READ_CMD_HEADER);
/*  44:    */       }
/*  45:    */       break;
/*  46:    */     case 2: 
/*  47: 64 */       this.cmdStatus = SocksCmdStatus.valueOf(byteBuf.readByte());
/*  48: 65 */       this.reserved = byteBuf.readByte();
/*  49: 66 */       this.addressType = SocksAddressType.valueOf(byteBuf.readByte());
/*  50: 67 */       checkpoint(State.READ_CMD_ADDRESS);
/*  51:    */     case 3: 
/*  52: 70 */       switch (1.$SwitchMap$io$netty$handler$codec$socks$SocksAddressType[this.addressType.ordinal()])
/*  53:    */       {
/*  54:    */       case 1: 
/*  55: 72 */         this.host = SocksCommonUtils.intToIp(byteBuf.readInt());
/*  56: 73 */         this.port = byteBuf.readUnsignedShort();
/*  57: 74 */         this.msg = new SocksCmdResponse(this.cmdStatus, this.addressType, this.host, this.port);
/*  58: 75 */         break;
/*  59:    */       case 2: 
/*  60: 78 */         this.fieldLength = byteBuf.readByte();
/*  61: 79 */         this.host = byteBuf.readBytes(this.fieldLength).toString(CharsetUtil.US_ASCII);
/*  62: 80 */         this.port = byteBuf.readUnsignedShort();
/*  63: 81 */         this.msg = new SocksCmdResponse(this.cmdStatus, this.addressType, this.host, this.port);
/*  64: 82 */         break;
/*  65:    */       case 3: 
/*  66: 85 */         this.host = SocksCommonUtils.ipv6toStr(byteBuf.readBytes(16).array());
/*  67: 86 */         this.port = byteBuf.readUnsignedShort();
/*  68: 87 */         this.msg = new SocksCmdResponse(this.cmdStatus, this.addressType, this.host, this.port);
/*  69:    */       }
/*  70: 88 */       break;
/*  71:    */     }
/*  72: 95 */     ctx.pipeline().remove(this);
/*  73: 96 */     out.add(this.msg);
/*  74:    */   }
/*  75:    */   
/*  76:    */   static enum State
/*  77:    */   {
/*  78:100 */     CHECK_PROTOCOL_VERSION,  READ_CMD_HEADER,  READ_CMD_ADDRESS;
/*  79:    */     
/*  80:    */     private State() {}
/*  81:    */   }
/*  82:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksCmdResponseDecoder
 * JD-Core Version:    0.7.0.1
 */