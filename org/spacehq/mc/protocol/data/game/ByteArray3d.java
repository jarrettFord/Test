/*  1:   */ package org.spacehq.mc.protocol.data.game;
/*  2:   */ 
/*  3:   */ import java.util.Arrays;
/*  4:   */ 
/*  5:   */ public class ByteArray3d
/*  6:   */ {
/*  7:   */   private byte[] data;
/*  8:   */   
/*  9:   */   public ByteArray3d(int size)
/* 10:   */   {
/* 11:10 */     this.data = new byte[size];
/* 12:   */   }
/* 13:   */   
/* 14:   */   public ByteArray3d(byte[] array)
/* 15:   */   {
/* 16:14 */     this.data = array;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public byte[] getData()
/* 20:   */   {
/* 21:18 */     return this.data;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public int get(int x, int y, int z)
/* 25:   */   {
/* 26:22 */     return this.data[(y << 8 | z << 4 | x)] & 0xFF;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public void set(int x, int y, int z, int val)
/* 30:   */   {
/* 31:26 */     this.data[(y << 8 | z << 4 | x)] = ((byte)val);
/* 32:   */   }
/* 33:   */   
/* 34:   */   public void fill(int val)
/* 35:   */   {
/* 36:30 */     Arrays.fill(this.data, (byte)val);
/* 37:   */   }
/* 38:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.game.ByteArray3d
 * JD-Core Version:    0.7.0.1
 */