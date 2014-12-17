/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ public class SpdySessionStatus
/*   4:    */   implements Comparable<SpdySessionStatus>
/*   5:    */ {
/*   6: 26 */   public static final SpdySessionStatus OK = new SpdySessionStatus(0, "OK");
/*   7: 32 */   public static final SpdySessionStatus PROTOCOL_ERROR = new SpdySessionStatus(1, "PROTOCOL_ERROR");
/*   8: 38 */   public static final SpdySessionStatus INTERNAL_ERROR = new SpdySessionStatus(2, "INTERNAL_ERROR");
/*   9:    */   private final int code;
/*  10:    */   private final String statusPhrase;
/*  11:    */   
/*  12:    */   public static SpdySessionStatus valueOf(int code)
/*  13:    */   {
/*  14: 47 */     switch (code)
/*  15:    */     {
/*  16:    */     case 0: 
/*  17: 49 */       return OK;
/*  18:    */     case 1: 
/*  19: 51 */       return PROTOCOL_ERROR;
/*  20:    */     case 2: 
/*  21: 53 */       return INTERNAL_ERROR;
/*  22:    */     }
/*  23: 56 */     return new SpdySessionStatus(code, "UNKNOWN (" + code + ')');
/*  24:    */   }
/*  25:    */   
/*  26:    */   public SpdySessionStatus(int code, String statusPhrase)
/*  27:    */   {
/*  28: 68 */     if (statusPhrase == null) {
/*  29: 69 */       throw new NullPointerException("statusPhrase");
/*  30:    */     }
/*  31: 72 */     this.code = code;
/*  32: 73 */     this.statusPhrase = statusPhrase;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public int code()
/*  36:    */   {
/*  37: 80 */     return this.code;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public String statusPhrase()
/*  41:    */   {
/*  42: 87 */     return this.statusPhrase;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public int hashCode()
/*  46:    */   {
/*  47: 92 */     return code();
/*  48:    */   }
/*  49:    */   
/*  50:    */   public boolean equals(Object o)
/*  51:    */   {
/*  52: 97 */     if (!(o instanceof SpdySessionStatus)) {
/*  53: 98 */       return false;
/*  54:    */     }
/*  55:101 */     return code() == ((SpdySessionStatus)o).code();
/*  56:    */   }
/*  57:    */   
/*  58:    */   public String toString()
/*  59:    */   {
/*  60:106 */     return statusPhrase();
/*  61:    */   }
/*  62:    */   
/*  63:    */   public int compareTo(SpdySessionStatus o)
/*  64:    */   {
/*  65:111 */     return code() - o.code();
/*  66:    */   }
/*  67:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdySessionStatus
 * JD-Core Version:    0.7.0.1
 */