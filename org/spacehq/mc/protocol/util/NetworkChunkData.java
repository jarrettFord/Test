/*  1:   */ package org.spacehq.mc.protocol.util;
/*  2:   */ 
/*  3:   */ public class NetworkChunkData
/*  4:   */ {
/*  5:   */   private int mask;
/*  6:   */   private int extendedMask;
/*  7:   */   private boolean fullChunk;
/*  8:   */   private boolean sky;
/*  9:   */   private byte[] data;
/* 10:   */   
/* 11:   */   public NetworkChunkData(int mask, int extendedMask, boolean fullChunk, boolean sky, byte[] data)
/* 12:   */   {
/* 13:12 */     this.mask = mask;
/* 14:13 */     this.extendedMask = extendedMask;
/* 15:14 */     this.fullChunk = fullChunk;
/* 16:15 */     this.sky = sky;
/* 17:16 */     this.data = data;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public int getMask()
/* 21:   */   {
/* 22:20 */     return this.mask;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public int getExtendedMask()
/* 26:   */   {
/* 27:24 */     return this.extendedMask;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public boolean isFullChunk()
/* 31:   */   {
/* 32:28 */     return this.fullChunk;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public boolean hasSkyLight()
/* 36:   */   {
/* 37:32 */     return this.sky;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public byte[] getData()
/* 41:   */   {
/* 42:36 */     return this.data;
/* 43:   */   }
/* 44:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.util.NetworkChunkData
 * JD-Core Version:    0.7.0.1
 */