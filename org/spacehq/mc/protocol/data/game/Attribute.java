/*  1:   */ package org.spacehq.mc.protocol.data.game;
/*  2:   */ 
/*  3:   */ import java.util.ArrayList;
/*  4:   */ import java.util.List;
/*  5:   */ 
/*  6:   */ public class Attribute
/*  7:   */ {
/*  8:   */   private String key;
/*  9:   */   private double value;
/* 10:   */   private List<AttributeModifier> modifiers;
/* 11:   */   
/* 12:   */   public Attribute(DefaultAttribute type)
/* 13:   */   {
/* 14:13 */     this(type.getKey(), type.getDefault());
/* 15:   */   }
/* 16:   */   
/* 17:   */   public Attribute(DefaultAttribute type, double value)
/* 18:   */   {
/* 19:17 */     this(type.getKey(), value);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public Attribute(DefaultAttribute type, double value, List<AttributeModifier> modifiers)
/* 23:   */   {
/* 24:21 */     this(type.getKey(), value, modifiers);
/* 25:   */   }
/* 26:   */   
/* 27:   */   public Attribute(String key, double value)
/* 28:   */   {
/* 29:25 */     this(key, value, new ArrayList());
/* 30:   */   }
/* 31:   */   
/* 32:   */   public Attribute(String key, double value, List<AttributeModifier> modifiers)
/* 33:   */   {
/* 34:29 */     this.key = key;
/* 35:30 */     this.value = value;
/* 36:31 */     this.modifiers = modifiers;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public String getKey()
/* 40:   */   {
/* 41:35 */     return this.key;
/* 42:   */   }
/* 43:   */   
/* 44:   */   public double getValue()
/* 45:   */   {
/* 46:39 */     return this.value;
/* 47:   */   }
/* 48:   */   
/* 49:   */   public List<AttributeModifier> getModifiers()
/* 50:   */   {
/* 51:43 */     return this.modifiers;
/* 52:   */   }
/* 53:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.game.Attribute
 * JD-Core Version:    0.7.0.1
 */