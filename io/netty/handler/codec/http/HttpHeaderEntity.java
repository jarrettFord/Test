/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.util.CharsetUtil;
/*  5:   */ 
/*  6:   */ final class HttpHeaderEntity
/*  7:   */   implements CharSequence
/*  8:   */ {
/*  9:   */   private final String name;
/* 10:   */   private final int hash;
/* 11:   */   private final byte[] bytes;
/* 12:   */   
/* 13:   */   public HttpHeaderEntity(String name)
/* 14:   */   {
/* 15:28 */     this.name = name;
/* 16:29 */     this.hash = HttpHeaders.hash(name);
/* 17:30 */     this.bytes = name.getBytes(CharsetUtil.US_ASCII);
/* 18:   */   }
/* 19:   */   
/* 20:   */   int hash()
/* 21:   */   {
/* 22:34 */     return this.hash;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public int length()
/* 26:   */   {
/* 27:39 */     return this.bytes.length;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public char charAt(int index)
/* 31:   */   {
/* 32:44 */     return (char)this.bytes[index];
/* 33:   */   }
/* 34:   */   
/* 35:   */   public CharSequence subSequence(int start, int end)
/* 36:   */   {
/* 37:49 */     return new HttpHeaderEntity(this.name.substring(start, end));
/* 38:   */   }
/* 39:   */   
/* 40:   */   public String toString()
/* 41:   */   {
/* 42:54 */     return this.name;
/* 43:   */   }
/* 44:   */   
/* 45:   */   void encode(ByteBuf buf)
/* 46:   */   {
/* 47:58 */     buf.writeBytes(this.bytes);
/* 48:   */   }
/* 49:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpHeaderEntity
 * JD-Core Version:    0.7.0.1
 */