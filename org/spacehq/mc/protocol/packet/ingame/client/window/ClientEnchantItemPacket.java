/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.client.window;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ClientEnchantItemPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int windowId;
/* 12:   */   private int enchantment;
/* 13:   */   
/* 14:   */   private ClientEnchantItemPacket() {}
/* 15:   */   
/* 16:   */   public ClientEnchantItemPacket(int windowId, int enchantment)
/* 17:   */   {
/* 18:19 */     this.windowId = windowId;
/* 19:20 */     this.enchantment = enchantment;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public int getWindowId()
/* 23:   */   {
/* 24:24 */     return this.windowId;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public int getEnchantment()
/* 28:   */   {
/* 29:28 */     return this.enchantment;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public void read(NetInput in)
/* 33:   */     throws IOException
/* 34:   */   {
/* 35:33 */     this.windowId = in.readByte();
/* 36:34 */     this.enchantment = in.readByte();
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void write(NetOutput out)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     out.writeByte(this.windowId);
/* 43:40 */     out.writeByte(this.enchantment);
/* 44:   */   }
/* 45:   */   
/* 46:   */   public boolean isPriority()
/* 47:   */   {
/* 48:45 */     return false;
/* 49:   */   }
/* 50:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.window.ClientEnchantItemPacket
 * JD-Core Version:    0.7.0.1
 */