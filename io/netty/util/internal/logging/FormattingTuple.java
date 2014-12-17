/*  1:   */ package io.netty.util.internal.logging;
/*  2:   */ 
/*  3:   */ class FormattingTuple
/*  4:   */ {
/*  5:47 */   static final FormattingTuple NULL = new FormattingTuple(null);
/*  6:   */   private final String message;
/*  7:   */   private final Throwable throwable;
/*  8:   */   private final Object[] argArray;
/*  9:   */   
/* 10:   */   FormattingTuple(String message)
/* 11:   */   {
/* 12:54 */     this(message, null, null);
/* 13:   */   }
/* 14:   */   
/* 15:   */   FormattingTuple(String message, Object[] argArray, Throwable throwable)
/* 16:   */   {
/* 17:58 */     this.message = message;
/* 18:59 */     this.throwable = throwable;
/* 19:60 */     if (throwable == null) {
/* 20:61 */       this.argArray = argArray;
/* 21:   */     } else {
/* 22:63 */       this.argArray = trimmedCopy(argArray);
/* 23:   */     }
/* 24:   */   }
/* 25:   */   
/* 26:   */   static Object[] trimmedCopy(Object[] argArray)
/* 27:   */   {
/* 28:68 */     if ((argArray == null) || (argArray.length == 0)) {
/* 29:69 */       throw new IllegalStateException("non-sensical empty or null argument array");
/* 30:   */     }
/* 31:71 */     int trimemdLen = argArray.length - 1;
/* 32:72 */     Object[] trimmed = new Object[trimemdLen];
/* 33:73 */     System.arraycopy(argArray, 0, trimmed, 0, trimemdLen);
/* 34:74 */     return trimmed;
/* 35:   */   }
/* 36:   */   
/* 37:   */   public String getMessage()
/* 38:   */   {
/* 39:78 */     return this.message;
/* 40:   */   }
/* 41:   */   
/* 42:   */   public Object[] getArgArray()
/* 43:   */   {
/* 44:82 */     return this.argArray;
/* 45:   */   }
/* 46:   */   
/* 47:   */   public Throwable getThrowable()
/* 48:   */   {
/* 49:86 */     return this.throwable;
/* 50:   */   }
/* 51:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.logging.FormattingTuple
 * JD-Core Version:    0.7.0.1
 */