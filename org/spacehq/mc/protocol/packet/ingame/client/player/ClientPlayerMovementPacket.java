/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.client.player;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ClientPlayerMovementPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   protected double x;
/* 12:   */   protected double feetY;
/* 13:   */   protected double headY;
/* 14:   */   protected double z;
/* 15:   */   protected float yaw;
/* 16:   */   protected float pitch;
/* 17:   */   protected boolean onGround;
/* 18:19 */   protected boolean pos = false;
/* 19:20 */   protected boolean rot = false;
/* 20:   */   
/* 21:   */   protected ClientPlayerMovementPacket() {}
/* 22:   */   
/* 23:   */   public ClientPlayerMovementPacket(boolean onGround)
/* 24:   */   {
/* 25:26 */     this.onGround = onGround;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public double getX()
/* 29:   */   {
/* 30:30 */     return this.x;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public double getFeetY()
/* 34:   */   {
/* 35:34 */     return this.feetY;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public double getHeadY()
/* 39:   */   {
/* 40:38 */     return this.headY;
/* 41:   */   }
/* 42:   */   
/* 43:   */   public double getZ()
/* 44:   */   {
/* 45:42 */     return this.z;
/* 46:   */   }
/* 47:   */   
/* 48:   */   public double getYaw()
/* 49:   */   {
/* 50:46 */     return this.yaw;
/* 51:   */   }
/* 52:   */   
/* 53:   */   public double getPitch()
/* 54:   */   {
/* 55:50 */     return this.pitch;
/* 56:   */   }
/* 57:   */   
/* 58:   */   public boolean isOnGround()
/* 59:   */   {
/* 60:54 */     return this.onGround;
/* 61:   */   }
/* 62:   */   
/* 63:   */   public void read(NetInput in)
/* 64:   */     throws IOException
/* 65:   */   {
/* 66:59 */     if (this.pos)
/* 67:   */     {
/* 68:60 */       this.x = in.readDouble();
/* 69:61 */       this.feetY = in.readDouble();
/* 70:62 */       this.headY = in.readDouble();
/* 71:63 */       this.z = in.readDouble();
/* 72:   */     }
/* 73:66 */     if (this.rot)
/* 74:   */     {
/* 75:67 */       this.yaw = in.readFloat();
/* 76:68 */       this.pitch = in.readFloat();
/* 77:   */     }
/* 78:71 */     this.onGround = in.readBoolean();
/* 79:   */   }
/* 80:   */   
/* 81:   */   public void write(NetOutput out)
/* 82:   */     throws IOException
/* 83:   */   {
/* 84:76 */     if (this.pos)
/* 85:   */     {
/* 86:77 */       out.writeDouble(this.x);
/* 87:78 */       out.writeDouble(this.feetY);
/* 88:79 */       out.writeDouble(this.headY);
/* 89:80 */       out.writeDouble(this.z);
/* 90:   */     }
/* 91:83 */     if (this.rot)
/* 92:   */     {
/* 93:84 */       out.writeFloat(this.yaw);
/* 94:85 */       out.writeFloat(this.pitch);
/* 95:   */     }
/* 96:88 */     out.writeBoolean(this.onGround);
/* 97:   */   }
/* 98:   */   
/* 99:   */   public boolean isPriority()
/* :0:   */   {
/* :1:93 */     return false;
/* :2:   */   }
/* :3:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerMovementPacket
 * JD-Core Version:    0.7.0.1
 */