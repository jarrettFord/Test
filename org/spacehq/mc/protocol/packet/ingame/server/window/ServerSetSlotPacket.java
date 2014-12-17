/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.window;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.mc.protocol.data.game.ItemStack;
/*  5:   */ import org.spacehq.mc.protocol.util.NetUtil;
/*  6:   */ import org.spacehq.packetlib.io.NetInput;
/*  7:   */ import org.spacehq.packetlib.io.NetOutput;
/*  8:   */ import org.spacehq.packetlib.packet.Packet;
/*  9:   */ 
/* 10:   */ public class ServerSetSlotPacket
/* 11:   */   implements Packet
/* 12:   */ {
/* 13:   */   private int windowId;
/* 14:   */   private int slot;
/* 15:   */   private ItemStack item;
/* 16:   */   
/* 17:   */   private ServerSetSlotPacket() {}
/* 18:   */   
/* 19:   */   public ServerSetSlotPacket(int windowId, int slot, ItemStack item)
/* 20:   */   {
/* 21:22 */     this.windowId = windowId;
/* 22:23 */     this.slot = slot;
/* 23:24 */     this.item = item;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public int getWindowId()
/* 27:   */   {
/* 28:28 */     return this.windowId;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public int getSlot()
/* 32:   */   {
/* 33:32 */     return this.slot;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public ItemStack getItem()
/* 37:   */   {
/* 38:36 */     return this.item;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public void read(NetInput in)
/* 42:   */     throws IOException
/* 43:   */   {
/* 44:41 */     this.windowId = in.readUnsignedByte();
/* 45:42 */     this.slot = in.readShort();
/* 46:43 */     this.item = NetUtil.readItem(in);
/* 47:   */   }
/* 48:   */   
/* 49:   */   public void write(NetOutput out)
/* 50:   */     throws IOException
/* 51:   */   {
/* 52:48 */     out.writeByte(this.windowId);
/* 53:49 */     out.writeShort(this.slot);
/* 54:50 */     NetUtil.writeItem(out, this.item);
/* 55:   */   }
/* 56:   */   
/* 57:   */   public boolean isPriority()
/* 58:   */   {
/* 59:55 */     return false;
/* 60:   */   }
/* 61:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.window.ServerSetSlotPacket
 * JD-Core Version:    0.7.0.1
 */