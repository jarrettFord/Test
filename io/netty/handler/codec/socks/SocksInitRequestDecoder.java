/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandlerContext;
/*  5:   */ import io.netty.channel.ChannelPipeline;
/*  6:   */ import io.netty.handler.codec.ReplayingDecoder;
/*  7:   */ import java.util.ArrayList;
/*  8:   */ import java.util.List;
/*  9:   */ 
/* 10:   */ public class SocksInitRequestDecoder
/* 11:   */   extends ReplayingDecoder<State>
/* 12:   */ {
/* 13:   */   private static final String name = "SOCKS_INIT_REQUEST_DECODER";
/* 14:   */   
/* 15:   */   @Deprecated
/* 16:   */   public static String getName()
/* 17:   */   {
/* 18:37 */     return "SOCKS_INIT_REQUEST_DECODER";
/* 19:   */   }
/* 20:   */   
/* 21:40 */   private final List<SocksAuthScheme> authSchemes = new ArrayList();
/* 22:   */   private SocksProtocolVersion version;
/* 23:   */   private byte authSchemeNum;
/* 24:43 */   private SocksRequest msg = SocksCommonUtils.UNKNOWN_SOCKS_REQUEST;
/* 25:   */   
/* 26:   */   public SocksInitRequestDecoder()
/* 27:   */   {
/* 28:46 */     super(State.CHECK_PROTOCOL_VERSION);
/* 29:   */   }
/* 30:   */   
/* 31:   */   protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out)
/* 32:   */     throws Exception
/* 33:   */   {
/* 34:51 */     switch (1.$SwitchMap$io$netty$handler$codec$socks$SocksInitRequestDecoder$State[((State)state()).ordinal()])
/* 35:   */     {
/* 36:   */     case 1: 
/* 37:53 */       this.version = SocksProtocolVersion.valueOf(byteBuf.readByte());
/* 38:54 */       if (this.version == SocksProtocolVersion.SOCKS5) {
/* 39:57 */         checkpoint(State.READ_AUTH_SCHEMES);
/* 40:   */       }
/* 41:   */       break;
/* 42:   */     case 2: 
/* 43:60 */       this.authSchemes.clear();
/* 44:61 */       this.authSchemeNum = byteBuf.readByte();
/* 45:62 */       for (int i = 0; i < this.authSchemeNum; i++) {
/* 46:63 */         this.authSchemes.add(SocksAuthScheme.valueOf(byteBuf.readByte()));
/* 47:   */       }
/* 48:65 */       this.msg = new SocksInitRequest(this.authSchemes);
/* 49:   */     }
/* 50:69 */     ctx.pipeline().remove(this);
/* 51:70 */     out.add(this.msg);
/* 52:   */   }
/* 53:   */   
/* 54:   */   static enum State
/* 55:   */   {
/* 56:74 */     CHECK_PROTOCOL_VERSION,  READ_AUTH_SCHEMES;
/* 57:   */     
/* 58:   */     private State() {}
/* 59:   */   }
/* 60:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksInitRequestDecoder
 * JD-Core Version:    0.7.0.1
 */