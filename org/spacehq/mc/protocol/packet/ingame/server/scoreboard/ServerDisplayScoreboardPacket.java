/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.scoreboard;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerDisplayScoreboardPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private Position position;
/* 12:   */   private String name;
/* 13:   */   
/* 14:   */   private ServerDisplayScoreboardPacket() {}
/* 15:   */   
/* 16:   */   public ServerDisplayScoreboardPacket(Position position, String name)
/* 17:   */   {
/* 18:19 */     this.position = position;
/* 19:20 */     this.name = name;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public Position getPosition()
/* 23:   */   {
/* 24:24 */     return this.position;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public String getScoreboardName()
/* 28:   */   {
/* 29:28 */     return this.name;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public void read(NetInput in)
/* 33:   */     throws IOException
/* 34:   */   {
/* 35:33 */     this.position = Position.values()[in.readByte()];
/* 36:34 */     this.name = in.readString();
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void write(NetOutput out)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     out.writeByte(this.position.ordinal());
/* 43:40 */     out.writeString(this.name);
/* 44:   */   }
/* 45:   */   
/* 46:   */   public boolean isPriority()
/* 47:   */   {
/* 48:45 */     return false;
/* 49:   */   }
/* 50:   */   
/* 51:   */   public static enum Position
/* 52:   */   {
/* 53:49 */     PLAYER_LIST,  SIDEBAR,  BELOW_NAME;
/* 54:   */   }
/* 55:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.scoreboard.ServerDisplayScoreboardPacket
 * JD-Core Version:    0.7.0.1
 */