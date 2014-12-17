/*  1:   */ package org.spacehq.mc.protocol.data.message;
/*  2:   */ 
/*  3:   */ public class ClickEvent
/*  4:   */   implements Cloneable
/*  5:   */ {
/*  6:   */   private ClickAction action;
/*  7:   */   private String value;
/*  8:   */   
/*  9:   */   public ClickEvent(ClickAction action, String value)
/* 10:   */   {
/* 11: 9 */     this.action = action;
/* 12:10 */     this.value = value;
/* 13:   */   }
/* 14:   */   
/* 15:   */   public ClickAction getAction()
/* 16:   */   {
/* 17:14 */     return this.action;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public String getValue()
/* 21:   */   {
/* 22:18 */     return this.value;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public ClickEvent clone()
/* 26:   */   {
/* 27:23 */     return new ClickEvent(this.action, this.value);
/* 28:   */   }
/* 29:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.message.ClickEvent
 * JD-Core Version:    0.7.0.1
 */