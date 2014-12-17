/*  1:   */ package org.spacehq.mc.auth;
/*  2:   */ 
/*  3:   */ public class ProfileCriteria
/*  4:   */ {
/*  5:   */   private final String name;
/*  6: 6 */   private final String agent = "minecraft";
/*  7:   */   
/*  8:   */   public ProfileCriteria(String name)
/*  9:   */   {
/* 10: 9 */     this.name = name;
/* 11:   */   }
/* 12:   */   
/* 13:   */   public String getName()
/* 14:   */   {
/* 15:13 */     return this.name;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public boolean equals(Object o)
/* 19:   */   {
/* 20:17 */     if (this == o) {
/* 21:18 */       return true;
/* 22:   */     }
/* 23:19 */     if ((o != null) && (getClass() == o.getClass()))
/* 24:   */     {
/* 25:20 */       ProfileCriteria that = (ProfileCriteria)o;
/* 26:21 */       return this.name.toLowerCase().equals(that.name.toLowerCase());
/* 27:   */     }
/* 28:23 */     return false;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public int hashCode()
/* 32:   */   {
/* 33:28 */     return 31 * this.name.toLowerCase().hashCode();
/* 34:   */   }
/* 35:   */   
/* 36:   */   public String toString()
/* 37:   */   {
/* 38:32 */     return "GameProfileRepository{name=" + this.name + "}";
/* 39:   */   }
/* 40:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.ProfileCriteria
 * JD-Core Version:    0.7.0.1
 */