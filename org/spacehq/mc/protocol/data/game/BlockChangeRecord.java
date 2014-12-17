/*  1:   */ package org.spacehq.mc.protocol.data.game;
/*  2:   */ 
/*  3:   */ public class BlockChangeRecord
/*  4:   */ {
/*  5:   */   private int x;
/*  6:   */   private int y;
/*  7:   */   private int z;
/*  8:   */   private int id;
/*  9:   */   private int metadata;
/* 10:   */   
/* 11:   */   public BlockChangeRecord(int x, int y, int z, int id, int metadata)
/* 12:   */   {
/* 13:12 */     this.x = x;
/* 14:13 */     this.y = y;
/* 15:14 */     this.z = z;
/* 16:15 */     this.id = id;
/* 17:16 */     this.metadata = metadata;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public int getX()
/* 21:   */   {
/* 22:20 */     return this.x;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public int getY()
/* 26:   */   {
/* 27:24 */     return this.y;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public int getZ()
/* 31:   */   {
/* 32:28 */     return this.z;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public int getId()
/* 36:   */   {
/* 37:32 */     return this.id;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public int getMetadata()
/* 41:   */   {
/* 42:36 */     return this.metadata;
/* 43:   */   }
/* 44:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.game.BlockChangeRecord
 * JD-Core Version:    0.7.0.1
 */