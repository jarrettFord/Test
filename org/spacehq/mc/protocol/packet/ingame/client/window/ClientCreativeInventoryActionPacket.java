/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.client.window;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.mc.protocol.data.game.ItemStack;
/*  5:   */ import org.spacehq.mc.protocol.util.NetUtil;
/*  6:   */ import org.spacehq.packetlib.io.NetInput;
/*  7:   */ import org.spacehq.packetlib.io.NetOutput;
/*  8:   */ import org.spacehq.packetlib.packet.Packet;
/*  9:   */ 
/* 10:   */ public class ClientCreativeInventoryActionPacket
/* 11:   */   implements Packet
/* 12:   */ {
/* 13:   */   private int slot;
/* 14:   */   private ItemStack clicked;
/* 15:   */   
/* 16:   */   private ClientCreativeInventoryActionPacket() {}
/* 17:   */   
/* 18:   */   public ClientCreativeInventoryActionPacket(int slot, ItemStack clicked)
/* 19:   */   {
/* 20:21 */     this.slot = slot;
/* 21:22 */     this.clicked = clicked;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public int getSlot()
/* 25:   */   {
/* 26:26 */     return this.slot;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public ItemStack getClickedItem()
/* 30:   */   {
/* 31:30 */     return this.clicked;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public void read(NetInput in)
/* 35:   */     throws IOException
/* 36:   */   {
/* 37:35 */     this.slot = in.readShort();
/* 38:36 */     this.clicked = NetUtil.readItem(in);
/* 39:   */   }
/* 40:   */   
/* 41:   */   public void write(NetOutput out)
/* 42:   */     throws IOException
/* 43:   */   {
/* 44:41 */     out.writeShort(this.slot);
/* 45:42 */     NetUtil.writeItem(out, this.clicked);
/* 46:   */   }
/* 47:   */   
/* 48:   */   public boolean isPriority()
/* 49:   */   {
/* 50:47 */     return false;
/* 51:   */   }
/* 52:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.window.ClientCreativeInventoryActionPacket
 * JD-Core Version:    0.7.0.1
 */