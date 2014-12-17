/*  1:   */ package com.google.gson.internal;
/*  2:   */ 
/*  3:   */ import java.io.ObjectStreamException;
/*  4:   */ import java.math.BigDecimal;
/*  5:   */ 
/*  6:   */ public final class LazilyParsedNumber
/*  7:   */   extends Number
/*  8:   */ {
/*  9:   */   private final String value;
/* 10:   */   
/* 11:   */   public LazilyParsedNumber(String value)
/* 12:   */   {
/* 13:30 */     this.value = value;
/* 14:   */   }
/* 15:   */   
/* 16:   */   public int intValue()
/* 17:   */   {
/* 18:   */     try
/* 19:   */     {
/* 20:36 */       return Integer.parseInt(this.value);
/* 21:   */     }
/* 22:   */     catch (NumberFormatException e)
/* 23:   */     {
/* 24:   */       try
/* 25:   */       {
/* 26:39 */         return (int)Long.parseLong(this.value);
/* 27:   */       }
/* 28:   */       catch (NumberFormatException nfe) {}
/* 29:   */     }
/* 30:41 */     return new BigDecimal(this.value).intValue();
/* 31:   */   }
/* 32:   */   
/* 33:   */   public long longValue()
/* 34:   */   {
/* 35:   */     try
/* 36:   */     {
/* 37:49 */       return Long.parseLong(this.value);
/* 38:   */     }
/* 39:   */     catch (NumberFormatException e) {}
/* 40:51 */     return new BigDecimal(this.value).longValue();
/* 41:   */   }
/* 42:   */   
/* 43:   */   public float floatValue()
/* 44:   */   {
/* 45:57 */     return Float.parseFloat(this.value);
/* 46:   */   }
/* 47:   */   
/* 48:   */   public double doubleValue()
/* 49:   */   {
/* 50:62 */     return Double.parseDouble(this.value);
/* 51:   */   }
/* 52:   */   
/* 53:   */   public String toString()
/* 54:   */   {
/* 55:67 */     return this.value;
/* 56:   */   }
/* 57:   */   
/* 58:   */   private Object writeReplace()
/* 59:   */     throws ObjectStreamException
/* 60:   */   {
/* 61:76 */     return new BigDecimal(this.value);
/* 62:   */   }
/* 63:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.LazilyParsedNumber
 * JD-Core Version:    0.7.0.1
 */