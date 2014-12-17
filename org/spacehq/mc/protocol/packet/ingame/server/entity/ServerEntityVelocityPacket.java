/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerEntityVelocityPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int entityId;
/* 12:   */   private double motX;
/* 13:   */   private double motY;
/* 14:   */   private double motZ;
/* 15:   */   
/* 16:   */   private ServerEntityVelocityPacket() {}
/* 17:   */   
/* 18:   */   public ServerEntityVelocityPacket(int entityId, double motX, double motY, double motZ)
/* 19:   */   {
/* 20:21 */     this.entityId = entityId;
/* 21:22 */     this.motX = motX;
/* 22:23 */     this.motY = motY;
/* 23:24 */     this.motZ = motZ;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public int getEntityId()
/* 27:   */   {
/* 28:28 */     return this.entityId;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public double getMotionX()
/* 32:   */   {
/* 33:32 */     return this.motX;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public double getMotionY()
/* 37:   */   {
/* 38:36 */     return this.motY;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public double getMotionZ()
/* 42:   */   {
/* 43:40 */     return this.motZ;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public void read(NetInput in)
/* 47:   */     throws IOException
/* 48:   */   {
/* 49:45 */     this.entityId = in.readInt();
/* 50:46 */     this.motX = (in.readShort() / 8000.0D);
/* 51:47 */     this.motY = (in.readShort() / 8000.0D);
/* 52:48 */     this.motZ = (in.readShort() / 8000.0D);
/* 53:   */   }
/* 54:   */   
/* 55:   */   public void write(NetOutput out)
/* 56:   */     throws IOException
/* 57:   */   {
/* 58:53 */     out.writeInt(this.entityId);
/* 59:54 */     out.writeShort((int)(this.motX * 8000.0D));
/* 60:55 */     out.writeShort((int)(this.motY * 8000.0D));
/* 61:56 */     out.writeShort((int)(this.motZ * 8000.0D));
/* 62:   */   }
/* 63:   */   
/* 64:   */   public boolean isPriority()
/* 65:   */   {
/* 66:61 */     return false;
/* 67:   */   }
/* 68:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityVelocityPacket
 * JD-Core Version:    0.7.0.1
 */