/*  1:   */ package com.google.gson.stream;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ 
/*  5:   */ public final class MalformedJsonException
/*  6:   */   extends IOException
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = 1L;
/*  9:   */   
/* 10:   */   public MalformedJsonException(String msg)
/* 11:   */   {
/* 12:29 */     super(msg);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public MalformedJsonException(String msg, Throwable throwable)
/* 16:   */   {
/* 17:33 */     super(msg);
/* 18:   */     
/* 19:   */ 
/* 20:36 */     initCause(throwable);
/* 21:   */   }
/* 22:   */   
/* 23:   */   public MalformedJsonException(Throwable throwable)
/* 24:   */   {
/* 25:42 */     initCause(throwable);
/* 26:   */   }
/* 27:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.stream.MalformedJsonException
 * JD-Core Version:    0.7.0.1
 */