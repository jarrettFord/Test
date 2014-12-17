/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufProcessor;
/*   5:    */ import io.netty.handler.codec.CorruptedFrameException;
/*   6:    */ 
/*   7:    */ final class Utf8Validator
/*   8:    */   implements ByteBufProcessor
/*   9:    */ {
/*  10:    */   private static final int UTF8_ACCEPT = 0;
/*  11:    */   private static final int UTF8_REJECT = 12;
/*  12: 49 */   private static final byte[] TYPES = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 10, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 11, 6, 6, 6, 5, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 };
/*  13: 60 */   private static final byte[] STATES = { 0, 12, 24, 36, 60, 96, 84, 12, 12, 12, 48, 72, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 0, 12, 12, 12, 12, 12, 0, 12, 0, 12, 12, 12, 24, 12, 12, 12, 12, 12, 24, 12, 24, 12, 12, 12, 12, 12, 12, 12, 12, 12, 24, 12, 12, 12, 12, 12, 24, 12, 12, 12, 12, 12, 12, 12, 24, 12, 12, 12, 12, 12, 12, 12, 12, 12, 36, 12, 36, 12, 12, 12, 36, 12, 12, 12, 12, 12, 36, 12, 36, 12, 12, 12, 36, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12 };
/*  14: 67 */   private int state = 0;
/*  15:    */   private int codep;
/*  16:    */   private boolean checking;
/*  17:    */   
/*  18:    */   public void check(ByteBuf buffer)
/*  19:    */   {
/*  20: 73 */     this.checking = true;
/*  21: 74 */     buffer.forEachByte(this);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public void finish()
/*  25:    */   {
/*  26: 78 */     this.checking = false;
/*  27: 79 */     this.codep = 0;
/*  28: 80 */     if (this.state != 0)
/*  29:    */     {
/*  30: 81 */       this.state = 0;
/*  31: 82 */       throw new CorruptedFrameException("bytes are not UTF-8");
/*  32:    */     }
/*  33:    */   }
/*  34:    */   
/*  35:    */   public boolean process(byte b)
/*  36:    */     throws Exception
/*  37:    */   {
/*  38: 88 */     byte type = TYPES[(b & 0xFF)];
/*  39:    */     
/*  40: 90 */     this.codep = (this.state != 0 ? b & 0x3F | this.codep << 6 : 255 >> type & b);
/*  41:    */     
/*  42: 92 */     this.state = STATES[(this.state + type)];
/*  43: 94 */     if (this.state == 12)
/*  44:    */     {
/*  45: 95 */       this.checking = false;
/*  46: 96 */       throw new CorruptedFrameException("bytes are not UTF-8");
/*  47:    */     }
/*  48: 98 */     return true;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public boolean isChecking()
/*  52:    */   {
/*  53:102 */     return this.checking;
/*  54:    */   }
/*  55:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.Utf8Validator
 * JD-Core Version:    0.7.0.1
 */