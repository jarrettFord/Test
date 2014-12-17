/*  1:   */ package com.google.gson.internal;
/*  2:   */ 
/*  3:   */ public final class $Gson$Preconditions
/*  4:   */ {
/*  5:   */   public static <T> T checkNotNull(T obj)
/*  6:   */   {
/*  7:34 */     if (obj == null) {
/*  8:35 */       throw new NullPointerException();
/*  9:   */     }
/* 10:37 */     return obj;
/* 11:   */   }
/* 12:   */   
/* 13:   */   public static void checkArgument(boolean condition)
/* 14:   */   {
/* 15:41 */     if (!condition) {
/* 16:42 */       throw new IllegalArgumentException();
/* 17:   */     }
/* 18:   */   }
/* 19:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal..Gson.Preconditions
 * JD-Core Version:    0.7.0.1
 */