/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerEntityMovementPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   protected int entityId;
/* 12:   */   protected double moveX;
/* 13:   */   protected double moveY;
/* 14:   */   protected double moveZ;
/* 15:   */   protected float yaw;
/* 16:   */   protected float pitch;
/* 17:18 */   protected boolean pos = false;
/* 18:19 */   protected boolean rot = false;
/* 19:   */   
/* 20:   */   protected ServerEntityMovementPacket() {}
/* 21:   */   
/* 22:   */   public ServerEntityMovementPacket(int entityId)
/* 23:   */   {
/* 24:25 */     this.entityId = entityId;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public int getEntityId()
/* 28:   */   {
/* 29:29 */     return this.entityId;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public double getMovementX()
/* 33:   */   {
/* 34:33 */     return this.moveX;
/* 35:   */   }
/* 36:   */   
/* 37:   */   public double getMovementY()
/* 38:   */   {
/* 39:37 */     return this.moveY;
/* 40:   */   }
/* 41:   */   
/* 42:   */   public double getMovementZ()
/* 43:   */   {
/* 44:41 */     return this.moveZ;
/* 45:   */   }
/* 46:   */   
/* 47:   */   public float getYaw()
/* 48:   */   {
/* 49:45 */     return this.yaw;
/* 50:   */   }
/* 51:   */   
/* 52:   */   public float getPitch()
/* 53:   */   {
/* 54:49 */     return this.pitch;
/* 55:   */   }
/* 56:   */   
/* 57:   */   public void read(NetInput in)
/* 58:   */     throws IOException
/* 59:   */   {
/* 60:54 */     this.entityId = in.readInt();
/* 61:55 */     if (this.pos)
/* 62:   */     {
/* 63:56 */       this.moveX = (in.readByte() / 32.0D);
/* 64:57 */       this.moveY = (in.readByte() / 32.0D);
/* 65:58 */       this.moveZ = (in.readByte() / 32.0D);
/* 66:   */     }
/* 67:61 */     if (this.rot)
/* 68:   */     {
/* 69:62 */       this.yaw = (in.readByte() * 360 / 256.0F);
/* 70:63 */       this.pitch = (in.readByte() * 360 / 256.0F);
/* 71:   */     }
/* 72:   */   }
/* 73:   */   
/* 74:   */   public void write(NetOutput out)
/* 75:   */     throws IOException
/* 76:   */   {
/* 77:69 */     out.writeInt(this.entityId);
/* 78:70 */     if (this.pos)
/* 79:   */     {
/* 80:71 */       out.writeByte((int)(this.moveX * 32.0D));
/* 81:72 */       out.writeByte((int)(this.moveY * 32.0D));
/* 82:73 */       out.writeByte((int)(this.moveZ * 32.0D));
/* 83:   */     }
/* 84:76 */     if (this.rot)
/* 85:   */     {
/* 86:77 */       out.writeByte((byte)(int)(this.yaw * 256.0F / 360.0F));
/* 87:78 */       out.writeByte((byte)(int)(this.pitch * 256.0F / 360.0F));
/* 88:   */     }
/* 89:   */   }
/* 90:   */   
/* 91:   */   public boolean isPriority()
/* 92:   */   {
/* 93:84 */     return false;
/* 94:   */   }
/* 95:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityMovementPacket
 * JD-Core Version:    0.7.0.1
 */