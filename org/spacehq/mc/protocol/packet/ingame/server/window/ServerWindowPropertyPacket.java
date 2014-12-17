/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.window;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerWindowPropertyPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int windowId;
/* 12:   */   private int property;
/* 13:   */   private int value;
/* 14:   */   
/* 15:   */   private ServerWindowPropertyPacket() {}
/* 16:   */   
/* 17:   */   public ServerWindowPropertyPacket(int windowId, int property, int value)
/* 18:   */   {
/* 19:20 */     this.windowId = windowId;
/* 20:21 */     this.property = property;
/* 21:22 */     this.value = value;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public int getWindowId()
/* 25:   */   {
/* 26:26 */     return this.windowId;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public int getProperty()
/* 30:   */   {
/* 31:30 */     return this.property;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public int getValue()
/* 35:   */   {
/* 36:34 */     return this.value;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void read(NetInput in)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     this.windowId = in.readUnsignedByte();
/* 43:40 */     this.property = in.readShort();
/* 44:41 */     this.value = in.readShort();
/* 45:   */   }
/* 46:   */   
/* 47:   */   public void write(NetOutput out)
/* 48:   */     throws IOException
/* 49:   */   {
/* 50:46 */     out.writeByte(this.windowId);
/* 51:47 */     out.writeShort(this.property);
/* 52:48 */     out.writeShort(this.value);
/* 53:   */   }
/* 54:   */   
/* 55:   */   public boolean isPriority()
/* 56:   */   {
/* 57:53 */     return false;
/* 58:   */   }
/* 59:   */   
/* 60:   */   public static class Property
/* 61:   */   {
/* 62:   */     public static final int FURNACE_PROGRESS = 0;
/* 63:   */     public static final int FURNACE_FUEL = 1;
/* 64:   */     public static final int ENCHANTMENT_SLOT_1 = 0;
/* 65:   */     public static final int ENCHANTMENT_SLOT_2 = 1;
/* 66:   */     public static final int ENCHANTMENT_SLOT_3 = 2;
/* 67:   */   }
/* 68:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.window.ServerWindowPropertyPacket
 * JD-Core Version:    0.7.0.1
 */