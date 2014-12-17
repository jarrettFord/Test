/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerSpawnPositionPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int x;
/* 12:   */   private int y;
/* 13:   */   private int z;
/* 14:   */   
/* 15:   */   private ServerSpawnPositionPacket() {}
/* 16:   */   
/* 17:   */   public ServerSpawnPositionPacket(int x, int y, int z)
/* 18:   */   {
/* 19:20 */     this.x = x;
/* 20:21 */     this.y = y;
/* 21:22 */     this.z = z;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public int getX()
/* 25:   */   {
/* 26:26 */     return this.x;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public int getY()
/* 30:   */   {
/* 31:30 */     return this.y;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public int getZ()
/* 35:   */   {
/* 36:34 */     return this.z;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void read(NetInput in)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     this.x = in.readInt();
/* 43:40 */     this.y = in.readInt();
/* 44:41 */     this.z = in.readInt();
/* 45:   */   }
/* 46:   */   
/* 47:   */   public void write(NetOutput out)
/* 48:   */     throws IOException
/* 49:   */   {
/* 50:46 */     out.writeInt(this.x);
/* 51:47 */     out.writeInt(this.y);
/* 52:48 */     out.writeInt(this.z);
/* 53:   */   }
/* 54:   */   
/* 55:   */   public boolean isPriority()
/* 56:   */   {
/* 57:53 */     return false;
/* 58:   */   }
/* 59:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerSpawnPositionPacket
 * JD-Core Version:    0.7.0.1
 */