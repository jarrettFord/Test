/*  1:   */ package org.spacehq.mc.auth.request;
/*  2:   */ 
/*  3:   */ public class Agent
/*  4:   */ {
/*  5:   */   private String name;
/*  6:   */   private int version;
/*  7:   */   
/*  8:   */   public Agent(String name, int version)
/*  9:   */   {
/* 10: 9 */     this.name = name;
/* 11:10 */     this.version = version;
/* 12:   */   }
/* 13:   */   
/* 14:   */   public String getName()
/* 15:   */   {
/* 16:14 */     return this.name;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public int getVersion()
/* 20:   */   {
/* 21:18 */     return this.version;
/* 22:   */   }
/* 23:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.request.Agent
 * JD-Core Version:    0.7.0.1
 */