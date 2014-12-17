/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.client.player;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ClientChangeHeldItemPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int slot;
/* 12:   */   
/* 13:   */   private ClientChangeHeldItemPacket() {}
/* 14:   */   
/* 15:   */   public ClientChangeHeldItemPacket(int slot)
/* 16:   */   {
/* 17:18 */     this.slot = slot;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public int getSlot()
/* 21:   */   {
/* 22:22 */     return this.slot;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public void read(NetInput in)
/* 26:   */     throws IOException
/* 27:   */   {
/* 28:27 */     this.slot = in.readShort();
/* 29:   */   }
/* 30:   */   
/* 31:   */   public void write(NetOutput out)
/* 32:   */     throws IOException
/* 33:   */   {
/* 34:32 */     out.writeShort(this.slot);
/* 35:   */   }
/* 36:   */   
/* 37:   */   public boolean isPriority()
/* 38:   */   {
/* 39:37 */     return false;
/* 40:   */   }
/* 41:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.player.ClientChangeHeldItemPacket
 * JD-Core Version:    0.7.0.1
 */