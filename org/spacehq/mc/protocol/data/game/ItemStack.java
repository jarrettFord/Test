/*  1:   */ package org.spacehq.mc.protocol.data.game;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.tag.builtin.CompoundTag;
/*  4:   */ 
/*  5:   */ public class ItemStack
/*  6:   */ {
/*  7:   */   private int id;
/*  8:   */   private int amount;
/*  9:   */   private int data;
/* 10:   */   private CompoundTag nbt;
/* 11:   */   
/* 12:   */   public ItemStack(int id)
/* 13:   */   {
/* 14:13 */     this(id, 1);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public ItemStack(int id, int amount)
/* 18:   */   {
/* 19:17 */     this(id, amount, 0);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public ItemStack(int id, int amount, int data)
/* 23:   */   {
/* 24:21 */     this(id, amount, data, null);
/* 25:   */   }
/* 26:   */   
/* 27:   */   public ItemStack(int id, int amount, int data, CompoundTag nbt)
/* 28:   */   {
/* 29:25 */     this.id = id;
/* 30:26 */     this.amount = amount;
/* 31:27 */     this.data = data;
/* 32:28 */     this.nbt = nbt;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public int getId()
/* 36:   */   {
/* 37:32 */     return this.id;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public int getAmount()
/* 41:   */   {
/* 42:36 */     return this.amount;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public int getData()
/* 46:   */   {
/* 47:40 */     return this.data;
/* 48:   */   }
/* 49:   */   
/* 50:   */   public CompoundTag getNBT()
/* 51:   */   {
/* 52:44 */     return this.nbt;
/* 53:   */   }
/* 54:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.game.ItemStack
 * JD-Core Version:    0.7.0.1
 */