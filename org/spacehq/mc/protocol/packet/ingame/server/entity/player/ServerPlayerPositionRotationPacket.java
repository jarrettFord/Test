/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity.player;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerPlayerPositionRotationPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   protected double x;
/* 12:   */   protected double y;
/* 13:   */   protected double z;
/* 14:   */   protected float yaw;
/* 15:   */   protected float pitch;
/* 16:   */   protected boolean onGround;
/* 17:   */   
/* 18:   */   private ServerPlayerPositionRotationPacket() {}
/* 19:   */   
/* 20:   */   public ServerPlayerPositionRotationPacket(double x, double y, double z, float yaw, float pitch, boolean onGround)
/* 21:   */   {
/* 22:23 */     this.x = x;
/* 23:24 */     this.y = y;
/* 24:25 */     this.z = z;
/* 25:26 */     this.yaw = yaw;
/* 26:27 */     this.pitch = pitch;
/* 27:28 */     this.onGround = onGround;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public double getX()
/* 31:   */   {
/* 32:32 */     return this.x;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public double getY()
/* 36:   */   {
/* 37:36 */     return this.y;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public double getZ()
/* 41:   */   {
/* 42:40 */     return this.z;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public float getYaw()
/* 46:   */   {
/* 47:44 */     return this.yaw;
/* 48:   */   }
/* 49:   */   
/* 50:   */   public float getPitch()
/* 51:   */   {
/* 52:48 */     return this.pitch;
/* 53:   */   }
/* 54:   */   
/* 55:   */   public boolean isOnGround()
/* 56:   */   {
/* 57:52 */     return this.onGround;
/* 58:   */   }
/* 59:   */   
/* 60:   */   public void read(NetInput in)
/* 61:   */     throws IOException
/* 62:   */   {
/* 63:57 */     this.x = in.readDouble();
/* 64:58 */     this.y = in.readDouble();
/* 65:59 */     this.z = in.readDouble();
/* 66:60 */     this.yaw = in.readFloat();
/* 67:61 */     this.pitch = in.readFloat();
/* 68:62 */     this.onGround = in.readBoolean();
/* 69:   */   }
/* 70:   */   
/* 71:   */   public void write(NetOutput out)
/* 72:   */     throws IOException
/* 73:   */   {
/* 74:67 */     out.writeDouble(this.x);
/* 75:68 */     out.writeDouble(this.y);
/* 76:69 */     out.writeDouble(this.z);
/* 77:70 */     out.writeFloat(this.yaw);
/* 78:71 */     out.writeFloat(this.pitch);
/* 79:72 */     out.writeBoolean(this.onGround);
/* 80:   */   }
/* 81:   */   
/* 82:   */   public boolean isPriority()
/* 83:   */   {
/* 84:77 */     return false;
/* 85:   */   }
/* 86:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket
 * JD-Core Version:    0.7.0.1
 */