/*  1:   */ package org.spacehq.mc.auth.exception;
/*  2:   */ 
/*  3:   */ public class SignatureValidateException
/*  4:   */   extends PropertyException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 1L;
/*  7:   */   
/*  8:   */   public SignatureValidateException() {}
/*  9:   */   
/* 10:   */   public SignatureValidateException(String message)
/* 11:   */   {
/* 12:11 */     super(message);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public SignatureValidateException(String message, Throwable cause)
/* 16:   */   {
/* 17:15 */     super(message, cause);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public SignatureValidateException(Throwable cause)
/* 21:   */   {
/* 22:19 */     super(cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.exception.SignatureValidateException
 * JD-Core Version:    0.7.0.1
 */