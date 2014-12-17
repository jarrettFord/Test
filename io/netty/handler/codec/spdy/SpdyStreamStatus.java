/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ public class SpdyStreamStatus
/*   4:    */   implements Comparable<SpdyStreamStatus>
/*   5:    */ {
/*   6: 26 */   public static final SpdyStreamStatus PROTOCOL_ERROR = new SpdyStreamStatus(1, "PROTOCOL_ERROR");
/*   7: 32 */   public static final SpdyStreamStatus INVALID_STREAM = new SpdyStreamStatus(2, "INVALID_STREAM");
/*   8: 38 */   public static final SpdyStreamStatus REFUSED_STREAM = new SpdyStreamStatus(3, "REFUSED_STREAM");
/*   9: 44 */   public static final SpdyStreamStatus UNSUPPORTED_VERSION = new SpdyStreamStatus(4, "UNSUPPORTED_VERSION");
/*  10: 50 */   public static final SpdyStreamStatus CANCEL = new SpdyStreamStatus(5, "CANCEL");
/*  11: 56 */   public static final SpdyStreamStatus INTERNAL_ERROR = new SpdyStreamStatus(6, "INTERNAL_ERROR");
/*  12: 62 */   public static final SpdyStreamStatus FLOW_CONTROL_ERROR = new SpdyStreamStatus(7, "FLOW_CONTROL_ERROR");
/*  13: 68 */   public static final SpdyStreamStatus STREAM_IN_USE = new SpdyStreamStatus(8, "STREAM_IN_USE");
/*  14: 74 */   public static final SpdyStreamStatus STREAM_ALREADY_CLOSED = new SpdyStreamStatus(9, "STREAM_ALREADY_CLOSED");
/*  15: 80 */   public static final SpdyStreamStatus INVALID_CREDENTIALS = new SpdyStreamStatus(10, "INVALID_CREDENTIALS");
/*  16: 86 */   public static final SpdyStreamStatus FRAME_TOO_LARGE = new SpdyStreamStatus(11, "FRAME_TOO_LARGE");
/*  17:    */   private final int code;
/*  18:    */   private final String statusPhrase;
/*  19:    */   
/*  20:    */   public static SpdyStreamStatus valueOf(int code)
/*  21:    */   {
/*  22: 95 */     if (code == 0) {
/*  23: 96 */       throw new IllegalArgumentException("0 is not a valid status code for a RST_STREAM");
/*  24:    */     }
/*  25:100 */     switch (code)
/*  26:    */     {
/*  27:    */     case 1: 
/*  28:102 */       return PROTOCOL_ERROR;
/*  29:    */     case 2: 
/*  30:104 */       return INVALID_STREAM;
/*  31:    */     case 3: 
/*  32:106 */       return REFUSED_STREAM;
/*  33:    */     case 4: 
/*  34:108 */       return UNSUPPORTED_VERSION;
/*  35:    */     case 5: 
/*  36:110 */       return CANCEL;
/*  37:    */     case 6: 
/*  38:112 */       return INTERNAL_ERROR;
/*  39:    */     case 7: 
/*  40:114 */       return FLOW_CONTROL_ERROR;
/*  41:    */     case 8: 
/*  42:116 */       return STREAM_IN_USE;
/*  43:    */     case 9: 
/*  44:118 */       return STREAM_ALREADY_CLOSED;
/*  45:    */     case 10: 
/*  46:120 */       return INVALID_CREDENTIALS;
/*  47:    */     case 11: 
/*  48:122 */       return FRAME_TOO_LARGE;
/*  49:    */     }
/*  50:125 */     return new SpdyStreamStatus(code, "UNKNOWN (" + code + ')');
/*  51:    */   }
/*  52:    */   
/*  53:    */   public SpdyStreamStatus(int code, String statusPhrase)
/*  54:    */   {
/*  55:137 */     if (code == 0) {
/*  56:138 */       throw new IllegalArgumentException("0 is not a valid status code for a RST_STREAM");
/*  57:    */     }
/*  58:142 */     if (statusPhrase == null) {
/*  59:143 */       throw new NullPointerException("statusPhrase");
/*  60:    */     }
/*  61:146 */     this.code = code;
/*  62:147 */     this.statusPhrase = statusPhrase;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public int code()
/*  66:    */   {
/*  67:154 */     return this.code;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public String statusPhrase()
/*  71:    */   {
/*  72:161 */     return this.statusPhrase;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public int hashCode()
/*  76:    */   {
/*  77:166 */     return code();
/*  78:    */   }
/*  79:    */   
/*  80:    */   public boolean equals(Object o)
/*  81:    */   {
/*  82:171 */     if (!(o instanceof SpdyStreamStatus)) {
/*  83:172 */       return false;
/*  84:    */     }
/*  85:175 */     return code() == ((SpdyStreamStatus)o).code();
/*  86:    */   }
/*  87:    */   
/*  88:    */   public String toString()
/*  89:    */   {
/*  90:180 */     return statusPhrase();
/*  91:    */   }
/*  92:    */   
/*  93:    */   public int compareTo(SpdyStreamStatus o)
/*  94:    */   {
/*  95:185 */     return code() - o.code();
/*  96:    */   }
/*  97:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyStreamStatus
 * JD-Core Version:    0.7.0.1
 */