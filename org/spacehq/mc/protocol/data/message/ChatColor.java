/*  1:   */ package org.spacehq.mc.protocol.data.message;
/*  2:   */ 
/*  3:   */ public enum ChatColor
/*  4:   */ {
/*  5: 5 */   BLACK,  DARK_BLUE,  DARK_GREEN,  DARK_AQUA,  DARK_RED,  DARK_PURPLE,  GOLD,  GRAY,  DARK_GRAY,  BLUE,  GREEN,  AQUA,  RED,  LIGHT_PURPLE,  YELLOW,  WHITE,  RESET;
/*  6:   */   
/*  7:   */   public String toString()
/*  8:   */   {
/*  9:25 */     return name().toLowerCase();
/* 10:   */   }
/* 11:   */   
/* 12:   */   public static ChatColor byName(String name)
/* 13:   */   {
/* 14:29 */     name = name.toLowerCase();
/* 15:30 */     for (ChatColor color : values()) {
/* 16:31 */       if (color.toString().equals(name)) {
/* 17:32 */         return color;
/* 18:   */       }
/* 19:   */     }
/* 20:36 */     return null;
/* 21:   */   }
/* 22:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.message.ChatColor
 * JD-Core Version:    0.7.0.1
 */