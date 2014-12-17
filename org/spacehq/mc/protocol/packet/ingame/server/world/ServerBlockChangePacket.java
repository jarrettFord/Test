/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.mc.protocol.data.game.BlockChangeRecord;
/*  5:   */ import org.spacehq.packetlib.io.NetInput;
/*  6:   */ import org.spacehq.packetlib.io.NetOutput;
/*  7:   */ import org.spacehq.packetlib.packet.Packet;
/*  8:   */ 
/*  9:   */ public class ServerBlockChangePacket
/* 10:   */   implements Packet
/* 11:   */ {
/* 12:   */   private BlockChangeRecord record;
/* 13:   */   
/* 14:   */   private ServerBlockChangePacket() {}
/* 15:   */   
/* 16:   */   public ServerBlockChangePacket(BlockChangeRecord record)
/* 17:   */   {
/* 18:19 */     this.record = record;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public BlockChangeRecord getRecord()
/* 22:   */   {
/* 23:23 */     return this.record;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public void read(NetInput in)
/* 27:   */     throws IOException
/* 28:   */   {
/* 29:28 */     this.record = new BlockChangeRecord(in.readInt(), in.readUnsignedByte(), in.readInt(), in.readVarInt(), in.readUnsignedByte());
/* 30:   */   }
/* 31:   */   
/* 32:   */   public void write(NetOutput out)
/* 33:   */     throws IOException
/* 34:   */   {
/* 35:33 */     out.writeInt(this.record.getX());
/* 36:34 */     out.writeByte(this.record.getY());
/* 37:35 */     out.writeInt(this.record.getZ());
/* 38:36 */     out.writeVarInt(this.record.getId());
/* 39:37 */     out.writeByte(this.record.getMetadata());
/* 40:   */   }
/* 41:   */   
/* 42:   */   public boolean isPriority()
/* 43:   */   {
/* 44:42 */     return false;
/* 45:   */   }
/* 46:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket
 * JD-Core Version:    0.7.0.1
 */