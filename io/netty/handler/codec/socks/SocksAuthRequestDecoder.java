/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandlerContext;
/*  5:   */ import io.netty.channel.ChannelPipeline;
/*  6:   */ import io.netty.handler.codec.ReplayingDecoder;
/*  7:   */ import io.netty.util.CharsetUtil;
/*  8:   */ import java.util.List;
/*  9:   */ 
/* 10:   */ public class SocksAuthRequestDecoder
/* 11:   */   extends ReplayingDecoder<State>
/* 12:   */ {
/* 13:   */   private static final String name = "SOCKS_AUTH_REQUEST_DECODER";
/* 14:   */   private SocksSubnegotiationVersion version;
/* 15:   */   private int fieldLength;
/* 16:   */   private String username;
/* 17:   */   private String password;
/* 18:   */   
/* 19:   */   @Deprecated
/* 20:   */   public static String getName()
/* 21:   */   {
/* 22:37 */     return "SOCKS_AUTH_REQUEST_DECODER";
/* 23:   */   }
/* 24:   */   
/* 25:44 */   private SocksRequest msg = SocksCommonUtils.UNKNOWN_SOCKS_REQUEST;
/* 26:   */   
/* 27:   */   public SocksAuthRequestDecoder()
/* 28:   */   {
/* 29:47 */     super(State.CHECK_PROTOCOL_VERSION);
/* 30:   */   }
/* 31:   */   
/* 32:   */   protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out)
/* 33:   */     throws Exception
/* 34:   */   {
/* 35:52 */     switch (1.$SwitchMap$io$netty$handler$codec$socks$SocksAuthRequestDecoder$State[((State)state()).ordinal()])
/* 36:   */     {
/* 37:   */     case 1: 
/* 38:54 */       this.version = SocksSubnegotiationVersion.valueOf(byteBuf.readByte());
/* 39:55 */       if (this.version == SocksSubnegotiationVersion.AUTH_PASSWORD) {
/* 40:58 */         checkpoint(State.READ_USERNAME);
/* 41:   */       }
/* 42:   */       break;
/* 43:   */     case 2: 
/* 44:61 */       this.fieldLength = byteBuf.readByte();
/* 45:62 */       this.username = byteBuf.readBytes(this.fieldLength).toString(CharsetUtil.US_ASCII);
/* 46:63 */       checkpoint(State.READ_PASSWORD);
/* 47:   */     case 3: 
/* 48:66 */       this.fieldLength = byteBuf.readByte();
/* 49:67 */       this.password = byteBuf.readBytes(this.fieldLength).toString(CharsetUtil.US_ASCII);
/* 50:68 */       this.msg = new SocksAuthRequest(this.username, this.password);
/* 51:   */     }
/* 52:71 */     ctx.pipeline().remove(this);
/* 53:72 */     out.add(this.msg);
/* 54:   */   }
/* 55:   */   
/* 56:   */   static enum State
/* 57:   */   {
/* 58:76 */     CHECK_PROTOCOL_VERSION,  READ_USERNAME,  READ_PASSWORD;
/* 59:   */     
/* 60:   */     private State() {}
/* 61:   */   }
/* 62:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksAuthRequestDecoder
 * JD-Core Version:    0.7.0.1
 */