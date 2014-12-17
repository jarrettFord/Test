/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerUpdateTimePacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private long age;
/* 12:   */   private long time;
/* 13:   */   
/* 14:   */   private ServerUpdateTimePacket() {}
/* 15:   */   
/* 16:   */   public ServerUpdateTimePacket(long age, long time)
/* 17:   */   {
/* 18:19 */     this.age = age;
/* 19:20 */     this.time = time;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public long getWorldAge()
/* 23:   */   {
/* 24:24 */     return this.age;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public long getTime()
/* 28:   */   {
/* 29:28 */     return this.time;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public void read(NetInput in)
/* 33:   */     throws IOException
/* 34:   */   {
/* 35:33 */     this.age = in.readLong();
/* 36:34 */     this.time = in.readLong();
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void write(NetOutput out)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     out.writeLong(this.age);
/* 43:40 */     out.writeLong(this.time);
/* 44:   */   }
/* 45:   */   
/* 46:   */   public boolean isPriority()
/* 47:   */   {
/* 48:45 */     return false;
/* 49:   */   }
/* 50:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerUpdateTimePacket
 * JD-Core Version:    0.7.0.1
 */