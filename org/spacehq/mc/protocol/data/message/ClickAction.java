/*  1:   */ package org.spacehq.mc.protocol.data.message;
/*  2:   */ 
/*  3:   */ public enum ClickAction
/*  4:   */ {
/*  5: 5 */   RUN_COMMAND,  SUGGEST_COMMAND,  OPEN_URL,  OPEN_FILE;
/*  6:   */   
/*  7:   */   public String toString()
/*  8:   */   {
/*  9:12 */     return name().toLowerCase();
/* 10:   */   }
/* 11:   */   
/* 12:   */   public static ClickAction byName(String name)
/* 13:   */   {
/* 14:16 */     name = name.toLowerCase();
/* 15:17 */     for (ClickAction action : values()) {
/* 16:18 */       if (action.toString().equals(name)) {
/* 17:19 */         return action;
/* 18:   */       }
/* 19:   */     }
/* 20:23 */     return null;
/* 21:   */   }
/* 22:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.message.ClickAction
 * JD-Core Version:    0.7.0.1
 */