/*  1:   */ package org.spacehq.mc.protocol.data.message;
/*  2:   */ 
/*  3:   */ public enum ChatFormat
/*  4:   */ {
/*  5: 5 */   BOLD,  UNDERLINED,  STRIKETHROUGH,  ITALIC,  OBFUSCATED;
/*  6:   */   
/*  7:   */   public String toString()
/*  8:   */   {
/*  9:13 */     return name().toLowerCase();
/* 10:   */   }
/* 11:   */   
/* 12:   */   public static ChatFormat byName(String name)
/* 13:   */   {
/* 14:17 */     name = name.toLowerCase();
/* 15:18 */     for (ChatFormat format : values()) {
/* 16:19 */       if (format.toString().equals(name)) {
/* 17:20 */         return format;
/* 18:   */       }
/* 19:   */     }
/* 20:24 */     return null;
/* 21:   */   }
/* 22:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.message.ChatFormat
 * JD-Core Version:    0.7.0.1
 */