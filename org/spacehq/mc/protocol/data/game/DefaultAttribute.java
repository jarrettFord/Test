/*  1:   */ package org.spacehq.mc.protocol.data.game;
/*  2:   */ 
/*  3:   */ public class DefaultAttribute
/*  4:   */ {
/*  5: 5 */   public static final DefaultAttribute MAX_HEALTH = new DefaultAttribute("generic.maxHealth", 20.0D, 0.0D, 1.7976931348623157E+308D);
/*  6: 6 */   public static final DefaultAttribute FOLLOW_RANGE = new DefaultAttribute("generic.followRange", 32.0D, 0.0D, 2048.0D);
/*  7: 7 */   public static final DefaultAttribute KNOCKBACK_RESISTANCE = new DefaultAttribute("generic.knockbackResistance", 0.0D, 0.0D, 1.0D);
/*  8: 8 */   public static final DefaultAttribute MOVEMENT_SPEED = new DefaultAttribute("generic.movementSpeed", 0.699999988079071D, 0.0D, 1.7976931348623157E+308D);
/*  9: 9 */   public static final DefaultAttribute ATTACK_DAMAGE = new DefaultAttribute("generic.attackStrength", 2.0D, 0.0D, 1.7976931348623157E+308D);
/* 10:10 */   public static final DefaultAttribute HORSE_JUMP_STRENGTH = new DefaultAttribute("generic.maxHealth", 0.7D, 0.0D, 2.0D);
/* 11:11 */   public static final DefaultAttribute ZOMBIE_SPAWN_REINFORCEMENTS_CHANCE = new DefaultAttribute("generic.maxHealth", 0.0D, 0.0D, 1.0D);
/* 12:   */   private String key;
/* 13:   */   private double def;
/* 14:   */   private double min;
/* 15:   */   private double max;
/* 16:   */   
/* 17:   */   private DefaultAttribute(String key, double def, double min, double max)
/* 18:   */   {
/* 19:19 */     this.key = key;
/* 20:20 */     this.def = def;
/* 21:21 */     this.min = min;
/* 22:22 */     this.max = max;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public String getKey()
/* 26:   */   {
/* 27:26 */     return this.key;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public double getDefault()
/* 31:   */   {
/* 32:30 */     return this.def;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public double getMin()
/* 36:   */   {
/* 37:34 */     return this.min;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public double getMax()
/* 41:   */   {
/* 42:38 */     return this.max;
/* 43:   */   }
/* 44:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.game.DefaultAttribute
 * JD-Core Version:    0.7.0.1
 */