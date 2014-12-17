/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.window;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerConfirmTransactionPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int windowId;
/* 12:   */   private int actionId;
/* 13:   */   private boolean accepted;
/* 14:   */   
/* 15:   */   private ServerConfirmTransactionPacket() {}
/* 16:   */   
/* 17:   */   public ServerConfirmTransactionPacket(int windowId, int actionId, boolean accepted)
/* 18:   */   {
/* 19:20 */     this.windowId = windowId;
/* 20:21 */     this.actionId = actionId;
/* 21:22 */     this.accepted = accepted;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public int getWindowId()
/* 25:   */   {
/* 26:26 */     return this.windowId;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public int getActionId()
/* 30:   */   {
/* 31:30 */     return this.actionId;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public boolean getAccepted()
/* 35:   */   {
/* 36:34 */     return this.accepted;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void read(NetInput in)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     this.windowId = in.readUnsignedByte();
/* 43:40 */     this.actionId = in.readShort();
/* 44:41 */     this.accepted = in.readBoolean();
/* 45:   */   }
/* 46:   */   
/* 47:   */   public void write(NetOutput out)
/* 48:   */     throws IOException
/* 49:   */   {
/* 50:46 */     out.writeByte(this.windowId);
/* 51:47 */     out.writeShort(this.actionId);
/* 52:48 */     out.writeBoolean(this.accepted);
/* 53:   */   }
/* 54:   */   
/* 55:   */   public boolean isPriority()
/* 56:   */   {
/* 57:53 */     return false;
/* 58:   */   }
/* 59:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.window.ServerConfirmTransactionPacket
 * JD-Core Version:    0.7.0.1
 */