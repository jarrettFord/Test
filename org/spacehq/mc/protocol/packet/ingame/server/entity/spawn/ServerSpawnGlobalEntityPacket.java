/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity.spawn;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerSpawnGlobalEntityPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int entityId;
/* 12:   */   private Type type;
/* 13:   */   private int x;
/* 14:   */   private int y;
/* 15:   */   private int z;
/* 16:   */   
/* 17:   */   private ServerSpawnGlobalEntityPacket() {}
/* 18:   */   
/* 19:   */   public ServerSpawnGlobalEntityPacket(int entityId, Type type, int x, int y, int z)
/* 20:   */   {
/* 21:22 */     this.entityId = entityId;
/* 22:23 */     this.type = type;
/* 23:24 */     this.x = x;
/* 24:25 */     this.y = y;
/* 25:26 */     this.z = z;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public int getEntityId()
/* 29:   */   {
/* 30:30 */     return this.entityId;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public Type getType()
/* 34:   */   {
/* 35:34 */     return this.type;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public int getX()
/* 39:   */   {
/* 40:38 */     return this.x;
/* 41:   */   }
/* 42:   */   
/* 43:   */   public int getY()
/* 44:   */   {
/* 45:42 */     return this.y;
/* 46:   */   }
/* 47:   */   
/* 48:   */   public int getZ()
/* 49:   */   {
/* 50:46 */     return this.z;
/* 51:   */   }
/* 52:   */   
/* 53:   */   public void read(NetInput in)
/* 54:   */     throws IOException
/* 55:   */   {
/* 56:51 */     this.entityId = in.readVarInt();
/* 57:52 */     this.type = Type.values()[(in.readByte() - 1)];
/* 58:53 */     this.x = in.readInt();
/* 59:54 */     this.y = in.readInt();
/* 60:55 */     this.z = in.readInt();
/* 61:   */   }
/* 62:   */   
/* 63:   */   public void write(NetOutput out)
/* 64:   */     throws IOException
/* 65:   */   {
/* 66:60 */     out.writeVarInt(this.entityId);
/* 67:61 */     out.writeByte(this.type.ordinal() + 1);
/* 68:62 */     out.writeInt(this.x);
/* 69:63 */     out.writeInt(this.y);
/* 70:64 */     out.writeInt(this.z);
/* 71:   */   }
/* 72:   */   
/* 73:   */   public boolean isPriority()
/* 74:   */   {
/* 75:69 */     return false;
/* 76:   */   }
/* 77:   */   
/* 78:   */   public static enum Type
/* 79:   */   {
/* 80:73 */     LIGHTNING_BOLT;
/* 81:   */   }
/* 82:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnGlobalEntityPacket
 * JD-Core Version:    0.7.0.1
 */