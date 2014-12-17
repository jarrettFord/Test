/*  1:   */ package org.spacehq.opennbt.conversion;
/*  2:   */ 
/*  3:   */ public class ConverterRegisterException
/*  4:   */   extends RuntimeException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = -2022049594558041160L;
/*  7:   */   
/*  8:   */   public ConverterRegisterException() {}
/*  9:   */   
/* 10:   */   public ConverterRegisterException(String message)
/* 11:   */   {
/* 12:15 */     super(message);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public ConverterRegisterException(Throwable cause)
/* 16:   */   {
/* 17:19 */     super(cause);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public ConverterRegisterException(String message, Throwable cause)
/* 21:   */   {
/* 22:23 */     super(message, cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.ConverterRegisterException
 * JD-Core Version:    0.7.0.1
 */