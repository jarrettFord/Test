/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.scoreboard;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerScoreboardObjectivePacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private String name;
/* 12:   */   private String displayName;
/* 13:   */   private Action action;
/* 14:   */   
/* 15:   */   private ServerScoreboardObjectivePacket() {}
/* 16:   */   
/* 17:   */   public ServerScoreboardObjectivePacket(String name, String displayName, Action action)
/* 18:   */   {
/* 19:20 */     this.name = name;
/* 20:21 */     this.displayName = displayName;
/* 21:22 */     this.action = action;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public String getName()
/* 25:   */   {
/* 26:26 */     return this.name;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public String getDisplayName()
/* 30:   */   {
/* 31:30 */     return this.displayName;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public Action getAction()
/* 35:   */   {
/* 36:34 */     return this.action;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void read(NetInput in)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     this.name = in.readString();
/* 43:40 */     this.displayName = in.readString();
/* 44:41 */     this.action = Action.values()[in.readByte()];
/* 45:   */   }
/* 46:   */   
/* 47:   */   public void write(NetOutput out)
/* 48:   */     throws IOException
/* 49:   */   {
/* 50:46 */     out.writeString(this.name);
/* 51:47 */     out.writeString(this.displayName);
/* 52:48 */     out.writeByte(this.action.ordinal());
/* 53:   */   }
/* 54:   */   
/* 55:   */   public boolean isPriority()
/* 56:   */   {
/* 57:53 */     return false;
/* 58:   */   }
/* 59:   */   
/* 60:   */   public static enum Action
/* 61:   */   {
/* 62:57 */     ADD,  REMOVE,  UPDATE;
/* 63:   */   }
/* 64:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.scoreboard.ServerScoreboardObjectivePacket
 * JD-Core Version:    0.7.0.1
 */