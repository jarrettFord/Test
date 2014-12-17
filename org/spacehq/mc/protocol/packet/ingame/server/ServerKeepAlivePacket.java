/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerKeepAlivePacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int id;
/* 12:   */   
/* 13:   */   private ServerKeepAlivePacket() {}
/* 14:   */   
/* 15:   */   public ServerKeepAlivePacket(int id)
/* 16:   */   {
/* 17:18 */     this.id = id;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public int getPingId()
/* 21:   */   {
/* 22:22 */     return this.id;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public void read(NetInput in)
/* 26:   */     throws IOException
/* 27:   */   {
/* 28:27 */     this.id = in.readInt();
/* 29:   */   }
/* 30:   */   
/* 31:   */   public void write(NetOutput out)
/* 32:   */     throws IOException
/* 33:   */   {
/* 34:32 */     out.writeInt(this.id);
/* 35:   */   }
/* 36:   */   
/* 37:   */   public boolean isPriority()
/* 38:   */   {
/* 39:37 */     return false;
/* 40:   */   }
/* 41:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.ServerKeepAlivePacket
 * JD-Core Version:    0.7.0.1
 */