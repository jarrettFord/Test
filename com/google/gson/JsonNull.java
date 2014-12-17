/*  1:   */ package com.google.gson;
/*  2:   */ 
/*  3:   */ public final class JsonNull
/*  4:   */   extends JsonElement
/*  5:   */ {
/*  6:32 */   public static final JsonNull INSTANCE = new JsonNull();
/*  7:   */   
/*  8:   */   JsonNull deepCopy()
/*  9:   */   {
/* 10:45 */     return INSTANCE;
/* 11:   */   }
/* 12:   */   
/* 13:   */   public int hashCode()
/* 14:   */   {
/* 15:53 */     return JsonNull.class.hashCode();
/* 16:   */   }
/* 17:   */   
/* 18:   */   public boolean equals(Object other)
/* 19:   */   {
/* 20:61 */     return (this == other) || ((other instanceof JsonNull));
/* 21:   */   }
/* 22:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.JsonNull
 * JD-Core Version:    0.7.0.1
 */