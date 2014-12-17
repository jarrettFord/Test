/*  1:   */ package org.spacehq.mc.auth.exception;
/*  2:   */ 
/*  3:   */ public class AuthenticationUnavailableException
/*  4:   */   extends AuthenticationException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 1L;
/*  7:   */   
/*  8:   */   public AuthenticationUnavailableException() {}
/*  9:   */   
/* 10:   */   public AuthenticationUnavailableException(String message)
/* 11:   */   {
/* 12:11 */     super(message);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public AuthenticationUnavailableException(String message, Throwable cause)
/* 16:   */   {
/* 17:15 */     super(message, cause);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public AuthenticationUnavailableException(Throwable cause)
/* 21:   */   {
/* 22:19 */     super(cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.exception.AuthenticationUnavailableException
 * JD-Core Version:    0.7.0.1
 */