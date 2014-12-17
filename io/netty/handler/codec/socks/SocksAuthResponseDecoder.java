/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandlerContext;
/*  5:   */ import io.netty.channel.ChannelPipeline;
/*  6:   */ import io.netty.handler.codec.ReplayingDecoder;
/*  7:   */ import java.util.List;
/*  8:   */ 
/*  9:   */ public class SocksAuthResponseDecoder
/* 10:   */   extends ReplayingDecoder<State>
/* 11:   */ {
/* 12:   */   private static final String name = "SOCKS_AUTH_RESPONSE_DECODER";
/* 13:   */   private SocksSubnegotiationVersion version;
/* 14:   */   private SocksAuthStatus authStatus;
/* 15:   */   
/* 16:   */   @Deprecated
/* 17:   */   public static String getName()
/* 18:   */   {
/* 19:36 */     return "SOCKS_AUTH_RESPONSE_DECODER";
/* 20:   */   }
/* 21:   */   
/* 22:41 */   private SocksResponse msg = SocksCommonUtils.UNKNOWN_SOCKS_RESPONSE;
/* 23:   */   
/* 24:   */   public SocksAuthResponseDecoder()
/* 25:   */   {
/* 26:44 */     super(State.CHECK_PROTOCOL_VERSION);
/* 27:   */   }
/* 28:   */   
/* 29:   */   protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out)
/* 30:   */     throws Exception
/* 31:   */   {
/* 32:50 */     switch (1.$SwitchMap$io$netty$handler$codec$socks$SocksAuthResponseDecoder$State[((State)state()).ordinal()])
/* 33:   */     {
/* 34:   */     case 1: 
/* 35:52 */       this.version = SocksSubnegotiationVersion.valueOf(byteBuf.readByte());
/* 36:53 */       if (this.version == SocksSubnegotiationVersion.AUTH_PASSWORD) {
/* 37:56 */         checkpoint(State.READ_AUTH_RESPONSE);
/* 38:   */       }
/* 39:   */       break;
/* 40:   */     case 2: 
/* 41:59 */       this.authStatus = SocksAuthStatus.valueOf(byteBuf.readByte());
/* 42:60 */       this.msg = new SocksAuthResponse(this.authStatus);
/* 43:   */     }
/* 44:63 */     channelHandlerContext.pipeline().remove(this);
/* 45:64 */     out.add(this.msg);
/* 46:   */   }
/* 47:   */   
/* 48:   */   static enum State
/* 49:   */   {
/* 50:68 */     CHECK_PROTOCOL_VERSION,  READ_AUTH_RESPONSE;
/* 51:   */     
/* 52:   */     private State() {}
/* 53:   */   }
/* 54:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksAuthResponseDecoder
 * JD-Core Version:    0.7.0.1
 */