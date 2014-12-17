/*  1:   */ package org.spacehq.mc.protocol.data.message;
/*  2:   */ 
/*  3:   */ public enum HoverAction
/*  4:   */ {
/*  5: 5 */   SHOW_TEXT,  SHOW_ITEM,  SHOW_ACHIEVEMENT,  SHOW_ENTITY;
/*  6:   */   
/*  7:   */   public String toString()
/*  8:   */   {
/*  9:12 */     return name().toLowerCase();
/* 10:   */   }
/* 11:   */   
/* 12:   */   public static HoverAction byName(String name)
/* 13:   */   {
/* 14:16 */     name = name.toLowerCase();
/* 15:17 */     for (HoverAction action : values()) {
/* 16:18 */       if (action.toString().equals(name)) {
/* 17:19 */         return action;
/* 18:   */       }
/* 19:   */     }
/* 20:23 */     return null;
/* 21:   */   }
/* 22:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.message.HoverAction
 * JD-Core Version:    0.7.0.1
 */