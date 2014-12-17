/*  1:   */ package org.spacehq.mc.protocol.data.game;
/*  2:   */ 
/*  3:   */ public class Chunk
/*  4:   */ {
/*  5:   */   private ByteArray3d blocks;
/*  6:   */   private NibbleArray3d metadata;
/*  7:   */   private NibbleArray3d blocklight;
/*  8:   */   private NibbleArray3d skylight;
/*  9:   */   private NibbleArray3d extendedBlocks;
/* 10:   */   
/* 11:   */   public Chunk(boolean skylight, boolean extended)
/* 12:   */   {
/* 13:12 */     this(new ByteArray3d(4096), new NibbleArray3d(4096), new NibbleArray3d(4096), skylight ? new NibbleArray3d(4096) : null, extended ? new NibbleArray3d(4096) : null);
/* 14:   */   }
/* 15:   */   
/* 16:   */   public Chunk(ByteArray3d blocks, NibbleArray3d metadata, NibbleArray3d blocklight, NibbleArray3d skylight, NibbleArray3d extendedBlocks)
/* 17:   */   {
/* 18:16 */     this.blocks = blocks;
/* 19:17 */     this.metadata = metadata;
/* 20:18 */     this.blocklight = blocklight;
/* 21:19 */     this.skylight = skylight;
/* 22:20 */     this.extendedBlocks = extendedBlocks;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public ByteArray3d getBlocks()
/* 26:   */   {
/* 27:24 */     return this.blocks;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public NibbleArray3d getMetadata()
/* 31:   */   {
/* 32:28 */     return this.metadata;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public NibbleArray3d getBlockLight()
/* 36:   */   {
/* 37:32 */     return this.blocklight;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public NibbleArray3d getSkyLight()
/* 41:   */   {
/* 42:36 */     return this.skylight;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public NibbleArray3d getExtendedBlocks()
/* 46:   */   {
/* 47:40 */     return this.extendedBlocks;
/* 48:   */   }
/* 49:   */   
/* 50:   */   public void deleteExtendedBlocks()
/* 51:   */   {
/* 52:44 */     this.extendedBlocks = null;
/* 53:   */   }
/* 54:   */   
/* 55:   */   public boolean isEmpty()
/* 56:   */   {
/* 57:48 */     for (byte block : this.blocks.getData()) {
/* 58:49 */       if (block != 0) {
/* 59:50 */         return false;
/* 60:   */       }
/* 61:   */     }
/* 62:54 */     return true;
/* 63:   */   }
/* 64:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.game.Chunk
 * JD-Core Version:    0.7.0.1
 */