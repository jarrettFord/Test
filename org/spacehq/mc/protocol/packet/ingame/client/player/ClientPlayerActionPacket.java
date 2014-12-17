/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.client.player;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ClientPlayerActionPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int entityId;
/* 12:   */   private Action action;
/* 13:   */   private int jumpBoost;
/* 14:   */   
/* 15:   */   private ClientPlayerActionPacket() {}
/* 16:   */   
/* 17:   */   public ClientPlayerActionPacket(int entityId, Action action)
/* 18:   */   {
/* 19:20 */     this(entityId, action, 0);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public ClientPlayerActionPacket(int entityId, Action action, int jumpBoost)
/* 23:   */   {
/* 24:24 */     this.entityId = entityId;
/* 25:25 */     this.action = action;
/* 26:26 */     this.jumpBoost = jumpBoost;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public int getEntityId()
/* 30:   */   {
/* 31:30 */     return this.entityId;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public Action getAction()
/* 35:   */   {
/* 36:34 */     return this.action;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public int getJumpBoost()
/* 40:   */   {
/* 41:38 */     return this.jumpBoost;
/* 42:   */   }
/* 43:   */   
/* 44:   */   public void read(NetInput in)
/* 45:   */     throws IOException
/* 46:   */   {
/* 47:43 */     this.entityId = in.readInt();
/* 48:44 */     this.action = Action.values()[(in.readByte() - 1)];
/* 49:45 */     this.jumpBoost = in.readInt();
/* 50:   */   }
/* 51:   */   
/* 52:   */   public void write(NetOutput out)
/* 53:   */     throws IOException
/* 54:   */   {
/* 55:50 */     out.writeInt(this.entityId);
/* 56:51 */     out.writeByte(this.action.ordinal() + 1);
/* 57:52 */     out.writeInt(this.jumpBoost);
/* 58:   */   }
/* 59:   */   
/* 60:   */   public boolean isPriority()
/* 61:   */   {
/* 62:57 */     return false;
/* 63:   */   }
/* 64:   */   
/* 65:   */   public static enum Action
/* 66:   */   {
/* 67:61 */     CROUCH,  UNCROUCH,  LEAVE_BED,  START_SPRINTING,  STOP_SPRINTING;
/* 68:   */   }
/* 69:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerActionPacket
 * JD-Core Version:    0.7.0.1
 */