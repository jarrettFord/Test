/*  1:   */ package org.spacehq.mc.protocol.packet.status.client;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class StatusPingPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private long time;
/* 12:   */   
/* 13:   */   private StatusPingPacket() {}
/* 14:   */   
/* 15:   */   public StatusPingPacket(long time)
/* 16:   */   {
/* 17:18 */     this.time = time;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public long getPingTime()
/* 21:   */   {
/* 22:22 */     return this.time;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public void read(NetInput in)
/* 26:   */     throws IOException
/* 27:   */   {
/* 28:27 */     this.time = in.readLong();
/* 29:   */   }
/* 30:   */   
/* 31:   */   public void write(NetOutput out)
/* 32:   */     throws IOException
/* 33:   */   {
/* 34:32 */     out.writeLong(this.time);
/* 35:   */   }
/* 36:   */   
/* 37:   */   public boolean isPriority()
/* 38:   */   {
/* 39:37 */     return false;
/* 40:   */   }
/* 41:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.status.client.StatusPingPacket
 * JD-Core Version:    0.7.0.1
 */