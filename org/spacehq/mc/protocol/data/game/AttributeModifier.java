/*  1:   */ package org.spacehq.mc.protocol.data.game;
/*  2:   */ 
/*  3:   */ import java.util.UUID;
/*  4:   */ 
/*  5:   */ public class AttributeModifier
/*  6:   */ {
/*  7:   */   private UUID uuid;
/*  8:   */   private double amount;
/*  9:   */   private int operation;
/* 10:   */   
/* 11:   */   public AttributeModifier(UUID uuid, double amount, int operation)
/* 12:   */   {
/* 13:12 */     this.uuid = uuid;
/* 14:13 */     this.amount = amount;
/* 15:14 */     this.operation = operation;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public UUID getUUID()
/* 19:   */   {
/* 20:18 */     return this.uuid;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public double getAmount()
/* 24:   */   {
/* 25:22 */     return this.amount;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public int getOperation()
/* 29:   */   {
/* 30:26 */     return this.operation;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public static class Operations
/* 34:   */   {
/* 35:   */     public static final int ADD = 0;
/* 36:   */     public static final int ADD_MULTIPLIED = 1;
/* 37:   */     public static final int MULTIPLY = 2;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public static class UUIDs
/* 41:   */   {
/* 42:36 */     public static final UUID CREATURE_FLEE_SPEED_BONUS = UUID.fromString("E199AD21-BA8A-4C53-8D13-6182D5C69D3A");
/* 43:37 */     public static final UUID ENDERMAN_ATTACK_SPEED_BOOST = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
/* 44:38 */     public static final UUID SPRINT_SPEED_BOOST = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
/* 45:39 */     public static final UUID PIGZOMBIE_ATTACK_SPEED_BOOST = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
/* 46:40 */     public static final UUID WITCH_DRINKING_SPEED_PENALTY = UUID.fromString("5CD17E52-A79A-43D3-A529-90FDE04B181E");
/* 47:41 */     public static final UUID ZOMBIE_BABY_SPEED_BOOST = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
/* 48:42 */     public static final UUID ITEM_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
/* 49:43 */     public static final UUID SPEED_POTION_MODIFIER = UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635");
/* 50:44 */     public static final UUID HEALTH_BOOST_POTION_MODIFIER = UUID.fromString("5D6F0BA2-1186-46AC-B896-C61C5CEE99CC");
/* 51:45 */     public static final UUID SLOW_POTION_MODIFIER = UUID.fromString("7107DE5E-7CE8-4030-940E-514C1F160890");
/* 52:46 */     public static final UUID STRENGTH_POTION_MODIFIER = UUID.fromString("648D7064-6A60-4F59-8ABE-C2C23A6DD7A9");
/* 53:47 */     public static final UUID WEAKNESS_POTION_MODIFIER = UUID.fromString("22653B89-116E-49DC-9B6B-9971489B5BE5");
/* 54:   */   }
/* 55:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.game.AttributeModifier
 * JD-Core Version:    0.7.0.1
 */