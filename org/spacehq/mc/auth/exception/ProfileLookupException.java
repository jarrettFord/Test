/*  1:   */ package org.spacehq.mc.auth.exception;
/*  2:   */ 
/*  3:   */ public class ProfileLookupException
/*  4:   */   extends ProfileException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 1L;
/*  7:   */   
/*  8:   */   public ProfileLookupException() {}
/*  9:   */   
/* 10:   */   public ProfileLookupException(String message)
/* 11:   */   {
/* 12:11 */     super(message);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public ProfileLookupException(String message, Throwable cause)
/* 16:   */   {
/* 17:15 */     super(message, cause);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public ProfileLookupException(Throwable cause)
/* 21:   */   {
/* 22:19 */     super(cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.exception.ProfileLookupException
 * JD-Core Version:    0.7.0.1
 */