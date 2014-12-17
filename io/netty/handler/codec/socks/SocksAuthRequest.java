/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.util.CharsetUtil;
/*  5:   */ import java.nio.charset.CharsetEncoder;
/*  6:   */ 
/*  7:   */ public final class SocksAuthRequest
/*  8:   */   extends SocksRequest
/*  9:   */ {
/* 10:30 */   private static final CharsetEncoder asciiEncoder = CharsetUtil.getEncoder(CharsetUtil.US_ASCII);
/* 11:31 */   private static final SocksSubnegotiationVersion SUBNEGOTIATION_VERSION = SocksSubnegotiationVersion.AUTH_PASSWORD;
/* 12:   */   private final String username;
/* 13:   */   private final String password;
/* 14:   */   
/* 15:   */   public SocksAuthRequest(String username, String password)
/* 16:   */   {
/* 17:36 */     super(SocksRequestType.AUTH);
/* 18:37 */     if (username == null) {
/* 19:38 */       throw new NullPointerException("username");
/* 20:   */     }
/* 21:40 */     if (password == null) {
/* 22:41 */       throw new NullPointerException("username");
/* 23:   */     }
/* 24:43 */     if ((!asciiEncoder.canEncode(username)) || (!asciiEncoder.canEncode(password))) {
/* 25:44 */       throw new IllegalArgumentException(" username: " + username + " or password: " + password + " values should be in pure ascii");
/* 26:   */     }
/* 27:47 */     if (username.length() > 255) {
/* 28:48 */       throw new IllegalArgumentException(username + " exceeds 255 char limit");
/* 29:   */     }
/* 30:50 */     if (password.length() > 255) {
/* 31:51 */       throw new IllegalArgumentException(password + " exceeds 255 char limit");
/* 32:   */     }
/* 33:53 */     this.username = username;
/* 34:54 */     this.password = password;
/* 35:   */   }
/* 36:   */   
/* 37:   */   public String username()
/* 38:   */   {
/* 39:63 */     return this.username;
/* 40:   */   }
/* 41:   */   
/* 42:   */   public String password()
/* 43:   */   {
/* 44:72 */     return this.password;
/* 45:   */   }
/* 46:   */   
/* 47:   */   public void encodeAsByteBuf(ByteBuf byteBuf)
/* 48:   */   {
/* 49:77 */     byteBuf.writeByte(SUBNEGOTIATION_VERSION.byteValue());
/* 50:78 */     byteBuf.writeByte(this.username.length());
/* 51:79 */     byteBuf.writeBytes(this.username.getBytes(CharsetUtil.US_ASCII));
/* 52:80 */     byteBuf.writeByte(this.password.length());
/* 53:81 */     byteBuf.writeBytes(this.password.getBytes(CharsetUtil.US_ASCII));
/* 54:   */   }
/* 55:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksAuthRequest
 * JD-Core Version:    0.7.0.1
 */