/*  1:   */ package org.spacehq.mc.protocol.util;
/*  2:   */ 
/*  3:   */ import org.spacehq.mc.protocol.data.game.Chunk;
/*  4:   */ 
/*  5:   */ public class ParsedChunkData
/*  6:   */ {
/*  7:   */   private Chunk[] chunks;
/*  8:   */   private byte[] biomes;
/*  9:   */   
/* 10:   */   public ParsedChunkData(Chunk[] chunks, byte[] biomes)
/* 11:   */   {
/* 12:11 */     this.chunks = chunks;
/* 13:12 */     this.biomes = biomes;
/* 14:   */   }
/* 15:   */   
/* 16:   */   public Chunk[] getChunks()
/* 17:   */   {
/* 18:16 */     return this.chunks;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public byte[] getBiomes()
/* 22:   */   {
/* 23:20 */     return this.biomes;
/* 24:   */   }
/* 25:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.util.ParsedChunkData
 * JD-Core Version:    0.7.0.1
 */