/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity.player;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerPlayerUseBedPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int entityId;
/* 12:   */   private int x;
/* 13:   */   private int y;
/* 14:   */   private int z;
/* 15:   */   
/* 16:   */   private ServerPlayerUseBedPacket() {}
/* 17:   */   
/* 18:   */   public ServerPlayerUseBedPacket(int entityId, int x, int y, int z)
/* 19:   */   {
/* 20:21 */     this.entityId = entityId;
/* 21:22 */     this.x = x;
/* 22:23 */     this.y = y;
/* 23:24 */     this.z = z;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public int getEntityId()
/* 27:   */   {
/* 28:28 */     return this.entityId;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public int getX()
/* 32:   */   {
/* 33:32 */     return this.x;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public int getY()
/* 37:   */   {
/* 38:36 */     return this.y;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public int getZ()
/* 42:   */   {
/* 43:40 */     return this.z;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public void read(NetInput in)
/* 47:   */     throws IOException
/* 48:   */   {
/* 49:45 */     this.entityId = in.readInt();
/* 50:46 */     this.x = in.readInt();
/* 51:47 */     this.y = in.readUnsignedByte();
/* 52:48 */     this.z = in.readInt();
/* 53:   */   }
/* 54:   */   
/* 55:   */   public void write(NetOutput out)
/* 56:   */     throws IOException
/* 57:   */   {
/* 58:53 */     out.writeInt(this.entityId);
/* 59:54 */     out.writeInt(this.x);
/* 60:55 */     out.writeByte(this.y);
/* 61:56 */     out.writeInt(this.z);
/* 62:   */   }
/* 63:   */   
/* 64:   */   public boolean isPriority()
/* 65:   */   {
/* 66:61 */     return false;
/* 67:   */   }
/* 68:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerUseBedPacket
 * JD-Core Version:    0.7.0.1
 */