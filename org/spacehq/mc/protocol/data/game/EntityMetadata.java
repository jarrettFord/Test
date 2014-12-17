/*  1:   */ package org.spacehq.mc.protocol.data.game;
/*  2:   */ 
/*  3:   */ public class EntityMetadata
/*  4:   */ {
/*  5:   */   private int id;
/*  6:   */   private Type type;
/*  7:   */   private Object value;
/*  8:   */   
/*  9:   */   public EntityMetadata(int id, Type type, Object value)
/* 10:   */   {
/* 11:10 */     this.id = id;
/* 12:11 */     this.type = type;
/* 13:12 */     this.value = value;
/* 14:   */   }
/* 15:   */   
/* 16:   */   public int getId()
/* 17:   */   {
/* 18:16 */     return this.id;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public Type getType()
/* 22:   */   {
/* 23:20 */     return this.type;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public Object getValue()
/* 27:   */   {
/* 28:24 */     return this.value;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public static enum Type
/* 32:   */   {
/* 33:28 */     BYTE,  SHORT,  INT,  FLOAT,  STRING,  ITEM,  COORDINATES;
/* 34:   */   }
/* 35:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.game.EntityMetadata
 * JD-Core Version:    0.7.0.1
 */