/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.client.player;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ClientSteerVehiclePacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private float sideways;
/* 12:   */   private float forward;
/* 13:   */   private boolean jump;
/* 14:   */   private boolean dismount;
/* 15:   */   
/* 16:   */   private ClientSteerVehiclePacket() {}
/* 17:   */   
/* 18:   */   public ClientSteerVehiclePacket(float sideways, float forward, boolean jump, boolean dismount)
/* 19:   */   {
/* 20:21 */     this.sideways = sideways;
/* 21:22 */     this.forward = forward;
/* 22:23 */     this.jump = jump;
/* 23:24 */     this.dismount = dismount;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public float getSideways()
/* 27:   */   {
/* 28:28 */     return this.sideways;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public float getForward()
/* 32:   */   {
/* 33:32 */     return this.forward;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public boolean getJumping()
/* 37:   */   {
/* 38:36 */     return this.jump;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public boolean getDismounting()
/* 42:   */   {
/* 43:40 */     return this.dismount;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public void read(NetInput in)
/* 47:   */     throws IOException
/* 48:   */   {
/* 49:45 */     this.sideways = in.readFloat();
/* 50:46 */     this.forward = in.readFloat();
/* 51:47 */     this.jump = in.readBoolean();
/* 52:48 */     this.dismount = in.readBoolean();
/* 53:   */   }
/* 54:   */   
/* 55:   */   public void write(NetOutput out)
/* 56:   */     throws IOException
/* 57:   */   {
/* 58:53 */     out.writeFloat(this.sideways);
/* 59:54 */     out.writeFloat(this.forward);
/* 60:55 */     out.writeBoolean(this.jump);
/* 61:56 */     out.writeBoolean(this.dismount);
/* 62:   */   }
/* 63:   */   
/* 64:   */   public boolean isPriority()
/* 65:   */   {
/* 66:61 */     return false;
/* 67:   */   }
/* 68:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.player.ClientSteerVehiclePacket
 * JD-Core Version:    0.7.0.1
 */