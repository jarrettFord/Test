/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.mc.protocol.data.game.BlockChangeRecord;
/*  5:   */ import org.spacehq.packetlib.io.NetInput;
/*  6:   */ import org.spacehq.packetlib.io.NetOutput;
/*  7:   */ import org.spacehq.packetlib.packet.Packet;
/*  8:   */ 
/*  9:   */ public class ServerMultiBlockChangePacket
/* 10:   */   implements Packet
/* 11:   */ {
/* 12:   */   private BlockChangeRecord[] records;
/* 13:   */   
/* 14:   */   private ServerMultiBlockChangePacket() {}
/* 15:   */   
/* 16:   */   public ServerMultiBlockChangePacket(BlockChangeRecord... records)
/* 17:   */   {
/* 18:19 */     if ((records == null) || (records.length == 0)) {
/* 19:20 */       throw new IllegalArgumentException("Records must contain at least 1 value.");
/* 20:   */     }
/* 21:23 */     this.records = records;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public BlockChangeRecord[] getRecords()
/* 25:   */   {
/* 26:27 */     return this.records;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public void read(NetInput in)
/* 30:   */     throws IOException
/* 31:   */   {
/* 32:32 */     int chunkX = in.readInt();
/* 33:33 */     int chunkZ = in.readInt();
/* 34:34 */     this.records = new BlockChangeRecord[in.readShort()];
/* 35:35 */     in.readInt();
/* 36:36 */     for (int index = 0; index < this.records.length; index++)
/* 37:   */     {
/* 38:37 */       short coords = in.readShort();
/* 39:38 */       short block = in.readShort();
/* 40:39 */       int x = (chunkX << 4) + (coords >> 12 & 0xF);
/* 41:40 */       int y = coords & 0xFF;
/* 42:41 */       int z = (chunkZ << 4) + (coords >> 8 & 0xF);
/* 43:42 */       int id = block >> 4 & 0xFFF;
/* 44:43 */       int metadata = block & 0xF;
/* 45:44 */       this.records[index] = new BlockChangeRecord(x, y, z, id, metadata);
/* 46:   */     }
/* 47:   */   }
/* 48:   */   
/* 49:   */   public void write(NetOutput out)
/* 50:   */     throws IOException
/* 51:   */   {
/* 52:50 */     int chunkX = this.records[0].getX() >> 4;
/* 53:51 */     int chunkZ = this.records[0].getZ() >> 4;
/* 54:52 */     out.writeInt(chunkX);
/* 55:53 */     out.writeInt(chunkZ);
/* 56:54 */     out.writeShort(this.records.length);
/* 57:55 */     out.writeInt(this.records.length * 4);
/* 58:56 */     for (BlockChangeRecord record : this.records)
/* 59:   */     {
/* 60:57 */       out.writeShort(record.getX() - (chunkX << 4) << 12 | record.getZ() - (chunkZ << 4) << 8 | record.getY());
/* 61:58 */       out.writeShort((record.getId() & 0xFFF) << 4 | record.getMetadata() & 0xF);
/* 62:   */     }
/* 63:   */   }
/* 64:   */   
/* 65:   */   public boolean isPriority()
/* 66:   */   {
/* 67:64 */     return false;
/* 68:   */   }
/* 69:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerMultiBlockChangePacket
 * JD-Core Version:    0.7.0.1
 */