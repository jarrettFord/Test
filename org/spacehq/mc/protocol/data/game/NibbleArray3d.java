/*  1:   */ package org.spacehq.mc.protocol.data.game;
/*  2:   */ 
/*  3:   */ public class NibbleArray3d
/*  4:   */ {
/*  5:   */   private byte[] data;
/*  6:   */   
/*  7:   */   public NibbleArray3d(int size)
/*  8:   */   {
/*  9: 8 */     this.data = new byte[size >> 1];
/* 10:   */   }
/* 11:   */   
/* 12:   */   public NibbleArray3d(byte[] array)
/* 13:   */   {
/* 14:12 */     this.data = array;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public byte[] getData()
/* 18:   */   {
/* 19:16 */     return this.data;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public int get(int x, int y, int z)
/* 23:   */   {
/* 24:20 */     int key = y << 8 | z << 4 | x;
/* 25:21 */     int index = key >> 1;
/* 26:22 */     int part = key & 0x1;
/* 27:23 */     return part == 0 ? this.data[index] & 0xF : this.data[index] >> 4 & 0xF;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public void set(int x, int y, int z, int val)
/* 31:   */   {
/* 32:27 */     int key = y << 8 | z << 4 | x;
/* 33:28 */     int index = key >> 1;
/* 34:29 */     int part = key & 0x1;
/* 35:30 */     if (part == 0) {
/* 36:31 */       this.data[index] = ((byte)(this.data[index] & 0xF0 | val & 0xF));
/* 37:   */     } else {
/* 38:33 */       this.data[index] = ((byte)(this.data[index] & 0xF | (val & 0xF) << 4));
/* 39:   */     }
/* 40:   */   }
/* 41:   */   
/* 42:   */   public void fill(int val)
/* 43:   */   {
/* 44:38 */     for (int index = 0; index < this.data.length << 1; index++)
/* 45:   */     {
/* 46:39 */       int ind = index >> 1;
/* 47:40 */       int part = index & 0x1;
/* 48:41 */       if (part == 0) {
/* 49:42 */         this.data[ind] = ((byte)(this.data[ind] & 0xF0 | val & 0xF));
/* 50:   */       } else {
/* 51:44 */         this.data[ind] = ((byte)(this.data[ind] & 0xF | (val & 0xF) << 4));
/* 52:   */       }
/* 53:   */     }
/* 54:   */   }
/* 55:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.game.NibbleArray3d
 * JD-Core Version:    0.7.0.1
 */