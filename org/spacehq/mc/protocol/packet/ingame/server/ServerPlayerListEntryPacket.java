/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerPlayerListEntryPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private String name;
/* 12:   */   private boolean online;
/* 13:   */   private int ping;
/* 14:   */   
/* 15:   */   private ServerPlayerListEntryPacket() {}
/* 16:   */   
/* 17:   */   public ServerPlayerListEntryPacket(String name, boolean online, int ping)
/* 18:   */   {
/* 19:20 */     this.name = name;
/* 20:21 */     this.online = online;
/* 21:22 */     this.ping = ping;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public String getName()
/* 25:   */   {
/* 26:26 */     return this.name;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public boolean getOnline()
/* 30:   */   {
/* 31:30 */     return this.online;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public int getPing()
/* 35:   */   {
/* 36:34 */     return this.ping;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void read(NetInput in)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     this.name = in.readString();
/* 43:40 */     this.online = in.readBoolean();
/* 44:41 */     this.ping = in.readShort();
/* 45:   */   }
/* 46:   */   
/* 47:   */   public void write(NetOutput out)
/* 48:   */     throws IOException
/* 49:   */   {
/* 50:46 */     out.writeString(this.name);
/* 51:47 */     out.writeBoolean(this.online);
/* 52:48 */     out.writeShort(this.ping);
/* 53:   */   }
/* 54:   */   
/* 55:   */   public boolean isPriority()
/* 56:   */   {
/* 57:53 */     return false;
/* 58:   */   }
/* 59:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket
 * JD-Core Version:    0.7.0.1
 */