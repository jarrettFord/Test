/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerEntityTeleportPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   protected int entityId;
/* 12:   */   protected double x;
/* 13:   */   protected double y;
/* 14:   */   protected double z;
/* 15:   */   protected float yaw;
/* 16:   */   protected float pitch;
/* 17:   */   
/* 18:   */   private ServerEntityTeleportPacket() {}
/* 19:   */   
/* 20:   */   public ServerEntityTeleportPacket(int entityId, double x, double y, double z, float yaw, float pitch)
/* 21:   */   {
/* 22:23 */     this.entityId = entityId;
/* 23:24 */     this.x = x;
/* 24:25 */     this.y = y;
/* 25:26 */     this.z = z;
/* 26:27 */     this.yaw = yaw;
/* 27:28 */     this.pitch = pitch;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public int getEntityId()
/* 31:   */   {
/* 32:32 */     return this.entityId;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public double getX()
/* 36:   */   {
/* 37:36 */     return this.x;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public double getY()
/* 41:   */   {
/* 42:40 */     return this.y;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public double getZ()
/* 46:   */   {
/* 47:44 */     return this.z;
/* 48:   */   }
/* 49:   */   
/* 50:   */   public float getYaw()
/* 51:   */   {
/* 52:48 */     return this.yaw;
/* 53:   */   }
/* 54:   */   
/* 55:   */   public float getPitch()
/* 56:   */   {
/* 57:52 */     return this.pitch;
/* 58:   */   }
/* 59:   */   
/* 60:   */   public void read(NetInput in)
/* 61:   */     throws IOException
/* 62:   */   {
/* 63:57 */     this.entityId = in.readInt();
/* 64:58 */     this.x = (in.readInt() / 32.0D);
/* 65:59 */     this.y = (in.readInt() / 32.0D);
/* 66:60 */     this.z = (in.readInt() / 32.0D);
/* 67:61 */     this.yaw = (in.readByte() * 360 / 256.0F);
/* 68:62 */     this.pitch = (in.readByte() * 360 / 256.0F);
/* 69:   */   }
/* 70:   */   
/* 71:   */   public void write(NetOutput out)
/* 72:   */     throws IOException
/* 73:   */   {
/* 74:67 */     out.writeInt(this.entityId);
/* 75:68 */     out.writeInt((int)(this.x * 32.0D));
/* 76:69 */     out.writeInt((int)(this.y * 32.0D));
/* 77:70 */     out.writeInt((int)(this.z * 32.0D));
/* 78:71 */     out.writeByte((byte)(int)(this.yaw * 256.0F / 360.0F));
/* 79:72 */     out.writeByte((byte)(int)(this.pitch * 256.0F / 360.0F));
/* 80:   */   }
/* 81:   */   
/* 82:   */   public boolean isPriority()
/* 83:   */   {
/* 84:77 */     return false;
/* 85:   */   }
/* 86:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityTeleportPacket
 * JD-Core Version:    0.7.0.1
 */