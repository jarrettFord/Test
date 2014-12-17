/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity.spawn;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerSpawnExpOrbPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int entityId;
/* 12:   */   private double x;
/* 13:   */   private double y;
/* 14:   */   private double z;
/* 15:   */   private int exp;
/* 16:   */   
/* 17:   */   private ServerSpawnExpOrbPacket() {}
/* 18:   */   
/* 19:   */   public ServerSpawnExpOrbPacket(int entityId, double x, double y, double z, int exp)
/* 20:   */   {
/* 21:22 */     this.entityId = entityId;
/* 22:23 */     this.x = x;
/* 23:24 */     this.y = y;
/* 24:25 */     this.z = z;
/* 25:26 */     this.exp = exp;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public int getEntityId()
/* 29:   */   {
/* 30:30 */     return this.entityId;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public double getX()
/* 34:   */   {
/* 35:34 */     return this.x;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public double getY()
/* 39:   */   {
/* 40:38 */     return this.y;
/* 41:   */   }
/* 42:   */   
/* 43:   */   public double getZ()
/* 44:   */   {
/* 45:42 */     return this.z;
/* 46:   */   }
/* 47:   */   
/* 48:   */   public int getExp()
/* 49:   */   {
/* 50:46 */     return this.exp;
/* 51:   */   }
/* 52:   */   
/* 53:   */   public void read(NetInput in)
/* 54:   */     throws IOException
/* 55:   */   {
/* 56:51 */     this.entityId = in.readVarInt();
/* 57:52 */     this.x = (in.readInt() / 32.0D);
/* 58:53 */     this.y = (in.readInt() / 32.0D);
/* 59:54 */     this.z = (in.readInt() / 32.0D);
/* 60:55 */     this.exp = in.readShort();
/* 61:   */   }
/* 62:   */   
/* 63:   */   public void write(NetOutput out)
/* 64:   */     throws IOException
/* 65:   */   {
/* 66:60 */     out.writeVarInt(this.entityId);
/* 67:61 */     out.writeInt((int)(this.x * 32.0D));
/* 68:62 */     out.writeInt((int)(this.y * 32.0D));
/* 69:63 */     out.writeInt((int)(this.z * 32.0D));
/* 70:64 */     out.writeShort(this.exp);
/* 71:   */   }
/* 72:   */   
/* 73:   */   public boolean isPriority()
/* 74:   */   {
/* 75:69 */     return false;
/* 76:   */   }
/* 77:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnExpOrbPacket
 * JD-Core Version:    0.7.0.1
 */