/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerTabCompletePacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private String[] matches;
/* 12:   */   
/* 13:   */   private ServerTabCompletePacket() {}
/* 14:   */   
/* 15:   */   public ServerTabCompletePacket(String[] matches)
/* 16:   */   {
/* 17:18 */     this.matches = matches;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public String[] getMatches()
/* 21:   */   {
/* 22:22 */     return this.matches;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public void read(NetInput in)
/* 26:   */     throws IOException
/* 27:   */   {
/* 28:27 */     this.matches = new String[in.readVarInt()];
/* 29:28 */     for (int index = 0; index < this.matches.length; index++) {
/* 30:29 */       this.matches[index] = in.readString();
/* 31:   */     }
/* 32:   */   }
/* 33:   */   
/* 34:   */   public void write(NetOutput out)
/* 35:   */     throws IOException
/* 36:   */   {
/* 37:35 */     out.writeVarInt(this.matches.length);
/* 38:36 */     for (String match : this.matches) {
/* 39:37 */       out.writeString(match);
/* 40:   */     }
/* 41:   */   }
/* 42:   */   
/* 43:   */   public boolean isPriority()
/* 44:   */   {
/* 45:43 */     return false;
/* 46:   */   }
/* 47:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.ServerTabCompletePacket
 * JD-Core Version:    0.7.0.1
 */