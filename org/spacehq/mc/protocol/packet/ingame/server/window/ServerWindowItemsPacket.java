/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.window;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.mc.protocol.data.game.ItemStack;
/*  5:   */ import org.spacehq.mc.protocol.util.NetUtil;
/*  6:   */ import org.spacehq.packetlib.io.NetInput;
/*  7:   */ import org.spacehq.packetlib.io.NetOutput;
/*  8:   */ import org.spacehq.packetlib.packet.Packet;
/*  9:   */ 
/* 10:   */ public class ServerWindowItemsPacket
/* 11:   */   implements Packet
/* 12:   */ {
/* 13:   */   private int windowId;
/* 14:   */   private ItemStack[] items;
/* 15:   */   
/* 16:   */   private ServerWindowItemsPacket() {}
/* 17:   */   
/* 18:   */   public ServerWindowItemsPacket(int windowId, ItemStack[] items)
/* 19:   */   {
/* 20:21 */     this.windowId = windowId;
/* 21:22 */     this.items = items;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public int getWindowId()
/* 25:   */   {
/* 26:26 */     return this.windowId;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public ItemStack[] getItems()
/* 30:   */   {
/* 31:30 */     return this.items;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public void read(NetInput in)
/* 35:   */     throws IOException
/* 36:   */   {
/* 37:35 */     this.windowId = in.readUnsignedByte();
/* 38:36 */     this.items = new ItemStack[in.readShort()];
/* 39:37 */     for (int index = 0; index < this.items.length; index++) {
/* 40:38 */       this.items[index] = NetUtil.readItem(in);
/* 41:   */     }
/* 42:   */   }
/* 43:   */   
/* 44:   */   public void write(NetOutput out)
/* 45:   */     throws IOException
/* 46:   */   {
/* 47:44 */     out.writeByte(this.windowId);
/* 48:45 */     out.writeShort(this.items.length);
/* 49:46 */     for (ItemStack item : this.items) {
/* 50:47 */       NetUtil.writeItem(out, item);
/* 51:   */     }
/* 52:   */   }
/* 53:   */   
/* 54:   */   public boolean isPriority()
/* 55:   */   {
/* 56:53 */     return false;
/* 57:   */   }
/* 58:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket
 * JD-Core Version:    0.7.0.1
 */