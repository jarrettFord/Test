/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity.player;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerUpdateHealthPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private float health;
/* 12:   */   private int food;
/* 13:   */   private float saturation;
/* 14:   */   
/* 15:   */   private ServerUpdateHealthPacket() {}
/* 16:   */   
/* 17:   */   public ServerUpdateHealthPacket(float health, int food, float saturation)
/* 18:   */   {
/* 19:20 */     this.health = health;
/* 20:21 */     this.food = food;
/* 21:22 */     this.saturation = saturation;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public float getHealth()
/* 25:   */   {
/* 26:26 */     return this.health;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public int getFood()
/* 30:   */   {
/* 31:30 */     return this.food;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public float getSaturation()
/* 35:   */   {
/* 36:34 */     return this.saturation;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void read(NetInput in)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     this.health = in.readFloat();
/* 43:40 */     this.food = in.readShort();
/* 44:41 */     this.saturation = in.readFloat();
/* 45:   */   }
/* 46:   */   
/* 47:   */   public void write(NetOutput out)
/* 48:   */     throws IOException
/* 49:   */   {
/* 50:46 */     out.writeFloat(this.health);
/* 51:47 */     out.writeShort(this.food);
/* 52:48 */     out.writeFloat(this.saturation);
/* 53:   */   }
/* 54:   */   
/* 55:   */   public boolean isPriority()
/* 56:   */   {
/* 57:53 */     return false;
/* 58:   */   }
/* 59:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerUpdateHealthPacket
 * JD-Core Version:    0.7.0.1
 */