/*  1:   */ package org.spacehq.mc.auth.response;
/*  2:   */ 
/*  3:   */ public class Response
/*  4:   */ {
/*  5:   */   private String error;
/*  6:   */   private String errorMessage;
/*  7:   */   private String cause;
/*  8:   */   
/*  9:   */   public String getError()
/* 10:   */   {
/* 11:10 */     return this.error;
/* 12:   */   }
/* 13:   */   
/* 14:   */   public void setError(String error)
/* 15:   */   {
/* 16:14 */     this.error = error;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public String getCause()
/* 20:   */   {
/* 21:18 */     return this.cause;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public void setCause(String cause)
/* 25:   */   {
/* 26:22 */     this.cause = cause;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public String getErrorMessage()
/* 30:   */   {
/* 31:26 */     return this.errorMessage;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public void setErrorMessage(String errorMessage)
/* 35:   */   {
/* 36:30 */     this.errorMessage = errorMessage;
/* 37:   */   }
/* 38:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.response.Response
 * JD-Core Version:    0.7.0.1
 */