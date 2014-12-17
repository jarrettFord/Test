/*  1:   */ package org.spacehq.mc.protocol.packet.status.client;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class StatusQueryPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   public void read(NetInput in)
/* 12:   */     throws IOException
/* 13:   */   {}
/* 14:   */   
/* 15:   */   public void write(NetOutput out)
/* 16:   */     throws IOException
/* 17:   */   {}
/* 18:   */   
/* 19:   */   public boolean isPriority()
/* 20:   */   {
/* 21:24 */     return false;
/* 22:   */   }
/* 23:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.status.client.StatusQueryPacket
 * JD-Core Version:    0.7.0.1
 */