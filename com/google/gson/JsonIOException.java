/*  1:   */ package com.google.gson;
/*  2:   */ 
/*  3:   */ public final class JsonIOException
/*  4:   */   extends JsonParseException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 1L;
/*  7:   */   
/*  8:   */   public JsonIOException(String msg)
/*  9:   */   {
/* 10:29 */     super(msg);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public JsonIOException(String msg, Throwable cause)
/* 14:   */   {
/* 15:33 */     super(msg, cause);
/* 16:   */   }
/* 17:   */   
/* 18:   */   public JsonIOException(Throwable cause)
/* 19:   */   {
/* 20:43 */     super(cause);
/* 21:   */   }
/* 22:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.JsonIOException
 * JD-Core Version:    0.7.0.1
 */