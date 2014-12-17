/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ public abstract interface ByteBufProcessor
/*   4:    */ {
/*   5: 24 */   public static final ByteBufProcessor FIND_NUL = new ByteBufProcessor()
/*   6:    */   {
/*   7:    */     public boolean process(byte value)
/*   8:    */       throws Exception
/*   9:    */     {
/*  10: 27 */       return value != 0;
/*  11:    */     }
/*  12:    */   };
/*  13: 34 */   public static final ByteBufProcessor FIND_NON_NUL = new ByteBufProcessor()
/*  14:    */   {
/*  15:    */     public boolean process(byte value)
/*  16:    */       throws Exception
/*  17:    */     {
/*  18: 37 */       return value == 0;
/*  19:    */     }
/*  20:    */   };
/*  21: 44 */   public static final ByteBufProcessor FIND_CR = new ByteBufProcessor()
/*  22:    */   {
/*  23:    */     public boolean process(byte value)
/*  24:    */       throws Exception
/*  25:    */     {
/*  26: 47 */       return value != 13;
/*  27:    */     }
/*  28:    */   };
/*  29: 54 */   public static final ByteBufProcessor FIND_NON_CR = new ByteBufProcessor()
/*  30:    */   {
/*  31:    */     public boolean process(byte value)
/*  32:    */       throws Exception
/*  33:    */     {
/*  34: 57 */       return value == 13;
/*  35:    */     }
/*  36:    */   };
/*  37: 64 */   public static final ByteBufProcessor FIND_LF = new ByteBufProcessor()
/*  38:    */   {
/*  39:    */     public boolean process(byte value)
/*  40:    */       throws Exception
/*  41:    */     {
/*  42: 67 */       return value != 10;
/*  43:    */     }
/*  44:    */   };
/*  45: 74 */   public static final ByteBufProcessor FIND_NON_LF = new ByteBufProcessor()
/*  46:    */   {
/*  47:    */     public boolean process(byte value)
/*  48:    */       throws Exception
/*  49:    */     {
/*  50: 77 */       return value == 10;
/*  51:    */     }
/*  52:    */   };
/*  53: 84 */   public static final ByteBufProcessor FIND_CRLF = new ByteBufProcessor()
/*  54:    */   {
/*  55:    */     public boolean process(byte value)
/*  56:    */       throws Exception
/*  57:    */     {
/*  58: 87 */       return (value != 13) && (value != 10);
/*  59:    */     }
/*  60:    */   };
/*  61: 94 */   public static final ByteBufProcessor FIND_NON_CRLF = new ByteBufProcessor()
/*  62:    */   {
/*  63:    */     public boolean process(byte value)
/*  64:    */       throws Exception
/*  65:    */     {
/*  66: 97 */       return (value == 13) || (value == 10);
/*  67:    */     }
/*  68:    */   };
/*  69:104 */   public static final ByteBufProcessor FIND_LINEAR_WHITESPACE = new ByteBufProcessor()
/*  70:    */   {
/*  71:    */     public boolean process(byte value)
/*  72:    */       throws Exception
/*  73:    */     {
/*  74:107 */       return (value != 32) && (value != 9);
/*  75:    */     }
/*  76:    */   };
/*  77:114 */   public static final ByteBufProcessor FIND_NON_LINEAR_WHITESPACE = new ByteBufProcessor()
/*  78:    */   {
/*  79:    */     public boolean process(byte value)
/*  80:    */       throws Exception
/*  81:    */     {
/*  82:117 */       return (value == 32) || (value == 9);
/*  83:    */     }
/*  84:    */   };
/*  85:    */   
/*  86:    */   public abstract boolean process(byte paramByte)
/*  87:    */     throws Exception;
/*  88:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.ByteBufProcessor
 * JD-Core Version:    0.7.0.1
 */