/*  1:   */ package org.spacehq.opennbt.tag;
/*  2:   */ 
/*  3:   */ public class TagCreateException
/*  4:   */   extends Exception
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = -2022049594558041160L;
/*  7:   */   
/*  8:   */   public TagCreateException() {}
/*  9:   */   
/* 10:   */   public TagCreateException(String message)
/* 11:   */   {
/* 12:15 */     super(message);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public TagCreateException(Throwable cause)
/* 16:   */   {
/* 17:19 */     super(cause);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public TagCreateException(String message, Throwable cause)
/* 21:   */   {
/* 22:23 */     super(message, cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.TagCreateException
 * JD-Core Version:    0.7.0.1
 */