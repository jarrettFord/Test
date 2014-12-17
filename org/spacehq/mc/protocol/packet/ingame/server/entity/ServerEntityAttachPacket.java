/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerEntityAttachPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int entityId;
/* 12:   */   private int attachedToId;
/* 13:   */   private boolean leash;
/* 14:   */   
/* 15:   */   private ServerEntityAttachPacket() {}
/* 16:   */   
/* 17:   */   public ServerEntityAttachPacket(int entityId, int attachedToId, boolean leash)
/* 18:   */   {
/* 19:20 */     this.entityId = entityId;
/* 20:21 */     this.attachedToId = attachedToId;
/* 21:22 */     this.leash = leash;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public int getEntityId()
/* 25:   */   {
/* 26:26 */     return this.entityId;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public int getAttachedToId()
/* 30:   */   {
/* 31:30 */     return this.attachedToId;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public boolean getLeash()
/* 35:   */   {
/* 36:34 */     return this.leash;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void read(NetInput in)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     this.entityId = in.readInt();
/* 43:40 */     this.attachedToId = in.readInt();
/* 44:41 */     this.leash = in.readBoolean();
/* 45:   */   }
/* 46:   */   
/* 47:   */   public void write(NetOutput out)
/* 48:   */     throws IOException
/* 49:   */   {
/* 50:46 */     out.writeInt(this.entityId);
/* 51:47 */     out.writeInt(this.attachedToId);
/* 52:48 */     out.writeBoolean(this.leash);
/* 53:   */   }
/* 54:   */   
/* 55:   */   public boolean isPriority()
/* 56:   */   {
/* 57:53 */     return false;
/* 58:   */   }
/* 59:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityAttachPacket
 * JD-Core Version:    0.7.0.1
 */