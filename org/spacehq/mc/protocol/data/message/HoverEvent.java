/*  1:   */ package org.spacehq.mc.protocol.data.message;
/*  2:   */ 
/*  3:   */ public class HoverEvent
/*  4:   */   implements Cloneable
/*  5:   */ {
/*  6:   */   private HoverAction action;
/*  7:   */   private Message value;
/*  8:   */   
/*  9:   */   public HoverEvent(HoverAction action, Message value)
/* 10:   */   {
/* 11: 9 */     this.action = action;
/* 12:10 */     this.value = value;
/* 13:   */   }
/* 14:   */   
/* 15:   */   public HoverAction getAction()
/* 16:   */   {
/* 17:14 */     return this.action;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public Message getValue()
/* 21:   */   {
/* 22:18 */     return this.value;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public HoverEvent clone()
/* 26:   */   {
/* 27:23 */     return new HoverEvent(this.action, this.value.clone());
/* 28:   */   }
/* 29:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.message.HoverEvent
 * JD-Core Version:    0.7.0.1
 */