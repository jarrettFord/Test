/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import java.util.Collections;
/*  5:   */ import java.util.List;
/*  6:   */ 
/*  7:   */ public final class SocksInitRequest
/*  8:   */   extends SocksRequest
/*  9:   */ {
/* 10:   */   private final List<SocksAuthScheme> authSchemes;
/* 11:   */   
/* 12:   */   public SocksInitRequest(List<SocksAuthScheme> authSchemes)
/* 13:   */   {
/* 14:33 */     super(SocksRequestType.INIT);
/* 15:34 */     if (authSchemes == null) {
/* 16:35 */       throw new NullPointerException("authSchemes");
/* 17:   */     }
/* 18:37 */     this.authSchemes = authSchemes;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public List<SocksAuthScheme> authSchemes()
/* 22:   */   {
/* 23:46 */     return Collections.unmodifiableList(this.authSchemes);
/* 24:   */   }
/* 25:   */   
/* 26:   */   public void encodeAsByteBuf(ByteBuf byteBuf)
/* 27:   */   {
/* 28:51 */     byteBuf.writeByte(protocolVersion().byteValue());
/* 29:52 */     byteBuf.writeByte(this.authSchemes.size());
/* 30:53 */     for (SocksAuthScheme authScheme : this.authSchemes) {
/* 31:54 */       byteBuf.writeByte(authScheme.byteValue());
/* 32:   */     }
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksInitRequest
 * JD-Core Version:    0.7.0.1
 */