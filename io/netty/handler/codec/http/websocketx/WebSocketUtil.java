/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.handler.codec.base64.Base64;
/*   6:    */ import io.netty.util.CharsetUtil;
/*   7:    */ import java.security.MessageDigest;
/*   8:    */ import java.security.NoSuchAlgorithmException;
/*   9:    */ 
/*  10:    */ final class WebSocketUtil
/*  11:    */ {
/*  12:    */   static byte[] md5(byte[] data)
/*  13:    */   {
/*  14:    */     try
/*  15:    */     {
/*  16: 39 */       MessageDigest md = MessageDigest.getInstance("MD5");
/*  17:    */       
/*  18: 41 */       return md.digest(data);
/*  19:    */     }
/*  20:    */     catch (NoSuchAlgorithmException e)
/*  21:    */     {
/*  22: 44 */       throw new InternalError("MD5 not supported on this platform - Outdated?");
/*  23:    */     }
/*  24:    */   }
/*  25:    */   
/*  26:    */   static byte[] sha1(byte[] data)
/*  27:    */   {
/*  28:    */     try
/*  29:    */     {
/*  30: 57 */       MessageDigest md = MessageDigest.getInstance("SHA1");
/*  31:    */       
/*  32: 59 */       return md.digest(data);
/*  33:    */     }
/*  34:    */     catch (NoSuchAlgorithmException e)
/*  35:    */     {
/*  36: 62 */       throw new InternalError("SHA-1 is not supported on this platform - Outdated?");
/*  37:    */     }
/*  38:    */   }
/*  39:    */   
/*  40:    */   static String base64(byte[] data)
/*  41:    */   {
/*  42: 73 */     ByteBuf encodedData = Unpooled.wrappedBuffer(data);
/*  43: 74 */     ByteBuf encoded = Base64.encode(encodedData);
/*  44: 75 */     String encodedString = encoded.toString(CharsetUtil.UTF_8);
/*  45: 76 */     encoded.release();
/*  46: 77 */     return encodedString;
/*  47:    */   }
/*  48:    */   
/*  49:    */   static byte[] randomBytes(int size)
/*  50:    */   {
/*  51: 87 */     byte[] bytes = new byte[size];
/*  52: 89 */     for (int index = 0; index < size; index++) {
/*  53: 90 */       bytes[index] = ((byte)randomNumber(0, 255));
/*  54:    */     }
/*  55: 93 */     return bytes;
/*  56:    */   }
/*  57:    */   
/*  58:    */   static int randomNumber(int minimum, int maximum)
/*  59:    */   {
/*  60:104 */     return (int)(Math.random() * maximum + minimum);
/*  61:    */   }
/*  62:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketUtil
 * JD-Core Version:    0.7.0.1
 */