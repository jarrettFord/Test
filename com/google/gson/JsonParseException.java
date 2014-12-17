/*  1:   */ package com.google.gson;
/*  2:   */ 
/*  3:   */ public class JsonParseException
/*  4:   */   extends RuntimeException
/*  5:   */ {
/*  6:   */   static final long serialVersionUID = -4086729973971783390L;
/*  7:   */   
/*  8:   */   public JsonParseException(String msg)
/*  9:   */   {
/* 10:42 */     super(msg);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public JsonParseException(String msg, Throwable cause)
/* 14:   */   {
/* 15:52 */     super(msg, cause);
/* 16:   */   }
/* 17:   */   
/* 18:   */   public JsonParseException(Throwable cause)
/* 19:   */   {
/* 20:62 */     super(cause);
/* 21:   */   }
/* 22:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.JsonParseException
 * JD-Core Version:    0.7.0.1
 */