/*  1:   */ package io.netty.handler.codec.http.multipart;
/*  2:   */ 
/*  3:   */ import java.io.Serializable;
/*  4:   */ import java.util.Comparator;
/*  5:   */ 
/*  6:   */ final class CaseIgnoringComparator
/*  7:   */   implements Comparator<String>, Serializable
/*  8:   */ {
/*  9:   */   private static final long serialVersionUID = 4582133183775373862L;
/* 10:25 */   static final CaseIgnoringComparator INSTANCE = new CaseIgnoringComparator();
/* 11:   */   
/* 12:   */   public int compare(String o1, String o2)
/* 13:   */   {
/* 14:32 */     return o1.compareToIgnoreCase(o2);
/* 15:   */   }
/* 16:   */   
/* 17:   */   private Object readResolve()
/* 18:   */   {
/* 19:36 */     return INSTANCE;
/* 20:   */   }
/* 21:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.CaseIgnoringComparator
 * JD-Core Version:    0.7.0.1
 */