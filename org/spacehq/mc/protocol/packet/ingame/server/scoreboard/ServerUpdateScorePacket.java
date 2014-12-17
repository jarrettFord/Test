/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.scoreboard;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerUpdateScorePacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private String entry;
/* 12:   */   private Action action;
/* 13:   */   private String objective;
/* 14:   */   private int value;
/* 15:   */   
/* 16:   */   private ServerUpdateScorePacket() {}
/* 17:   */   
/* 18:   */   public ServerUpdateScorePacket(String entry)
/* 19:   */   {
/* 20:21 */     this.entry = entry;
/* 21:22 */     this.action = Action.REMOVE;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public ServerUpdateScorePacket(String entry, String objective, int value)
/* 25:   */   {
/* 26:26 */     this.entry = entry;
/* 27:27 */     this.objective = objective;
/* 28:28 */     this.value = value;
/* 29:29 */     this.action = Action.ADD_OR_UPDATE;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public String getEntry()
/* 33:   */   {
/* 34:33 */     return this.entry;
/* 35:   */   }
/* 36:   */   
/* 37:   */   public Action getAction()
/* 38:   */   {
/* 39:37 */     return this.action;
/* 40:   */   }
/* 41:   */   
/* 42:   */   public String getObjective()
/* 43:   */   {
/* 44:41 */     return this.objective;
/* 45:   */   }
/* 46:   */   
/* 47:   */   public int getValue()
/* 48:   */   {
/* 49:45 */     return this.value;
/* 50:   */   }
/* 51:   */   
/* 52:   */   public void read(NetInput in)
/* 53:   */     throws IOException
/* 54:   */   {
/* 55:50 */     this.entry = in.readString();
/* 56:51 */     this.action = Action.values()[in.readByte()];
/* 57:52 */     if (this.action == Action.ADD_OR_UPDATE)
/* 58:   */     {
/* 59:53 */       this.objective = in.readString();
/* 60:54 */       this.value = in.readInt();
/* 61:   */     }
/* 62:   */   }
/* 63:   */   
/* 64:   */   public void write(NetOutput out)
/* 65:   */     throws IOException
/* 66:   */   {
/* 67:60 */     out.writeString(this.entry);
/* 68:61 */     out.writeByte(this.action.ordinal());
/* 69:62 */     if (this.action == Action.ADD_OR_UPDATE)
/* 70:   */     {
/* 71:63 */       out.writeString(this.objective);
/* 72:64 */       out.writeInt(this.value);
/* 73:   */     }
/* 74:   */   }
/* 75:   */   
/* 76:   */   public boolean isPriority()
/* 77:   */   {
/* 78:70 */     return false;
/* 79:   */   }
/* 80:   */   
/* 81:   */   public static enum Action
/* 82:   */   {
/* 83:74 */     ADD_OR_UPDATE,  REMOVE;
/* 84:   */   }
/* 85:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.scoreboard.ServerUpdateScorePacket
 * JD-Core Version:    0.7.0.1
 */