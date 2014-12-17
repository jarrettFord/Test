/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.window;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.packetlib.io.NetInput;
/*   5:    */ import org.spacehq.packetlib.io.NetOutput;
/*   6:    */ import org.spacehq.packetlib.packet.Packet;
/*   7:    */ 
/*   8:    */ public class ServerOpenWindowPacket
/*   9:    */   implements Packet
/*  10:    */ {
/*  11:    */   private int windowId;
/*  12:    */   private Type type;
/*  13:    */   private String name;
/*  14:    */   private int slots;
/*  15:    */   private boolean useName;
/*  16:    */   private int ownerEntityId;
/*  17:    */   
/*  18:    */   private ServerOpenWindowPacket() {}
/*  19:    */   
/*  20:    */   public ServerOpenWindowPacket(int windowId, Type type, String name, int slots, boolean useName)
/*  21:    */   {
/*  22: 23 */     this(windowId, type, name, slots, useName, 0);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public ServerOpenWindowPacket(int windowId, Type type, String name, int slots, boolean useName, int ownerEntityId)
/*  26:    */   {
/*  27: 27 */     this.windowId = windowId;
/*  28: 28 */     this.type = type;
/*  29: 29 */     this.name = name;
/*  30: 30 */     this.slots = slots;
/*  31: 31 */     this.useName = useName;
/*  32: 32 */     this.ownerEntityId = ownerEntityId;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public int getWindowId()
/*  36:    */   {
/*  37: 36 */     return this.windowId;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public Type getType()
/*  41:    */   {
/*  42: 40 */     return this.type;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public String getName()
/*  46:    */   {
/*  47: 44 */     return this.name;
/*  48:    */   }
/*  49:    */   
/*  50:    */   public int getSlots()
/*  51:    */   {
/*  52: 48 */     return this.slots;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public boolean getUseName()
/*  56:    */   {
/*  57: 52 */     return this.useName;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public int getOwnerEntityId()
/*  61:    */   {
/*  62: 56 */     return this.ownerEntityId;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public void read(NetInput in)
/*  66:    */     throws IOException
/*  67:    */   {
/*  68: 61 */     this.windowId = in.readUnsignedByte();
/*  69: 62 */     this.type = Type.values()[in.readUnsignedByte()];
/*  70: 63 */     this.name = in.readString();
/*  71: 64 */     this.slots = in.readUnsignedByte();
/*  72: 65 */     this.useName = in.readBoolean();
/*  73: 66 */     if (this.type == Type.HORSE_INVENTORY) {
/*  74: 67 */       this.ownerEntityId = in.readInt();
/*  75:    */     }
/*  76:    */   }
/*  77:    */   
/*  78:    */   public void write(NetOutput out)
/*  79:    */     throws IOException
/*  80:    */   {
/*  81: 73 */     out.writeByte(this.windowId);
/*  82: 74 */     out.writeByte(this.type.ordinal());
/*  83: 75 */     out.writeString(this.name);
/*  84: 76 */     out.writeByte(this.slots);
/*  85: 77 */     out.writeBoolean(this.useName);
/*  86: 78 */     if (this.type == Type.HORSE_INVENTORY) {
/*  87: 79 */       out.writeInt(this.ownerEntityId);
/*  88:    */     }
/*  89:    */   }
/*  90:    */   
/*  91:    */   public boolean isPriority()
/*  92:    */   {
/*  93: 85 */     return false;
/*  94:    */   }
/*  95:    */   
/*  96:    */   public static enum Type
/*  97:    */   {
/*  98: 89 */     CHEST,  CRAFTING_TABLE,  FURNACE,  DISPENSER,  ENCHANTMENT_TABLE,  BREWING_STAND,  VILLAGER_TRADE,  BEACON,  ANVIL,  HOPPER,  DROPPER,  HORSE_INVENTORY;
/*  99:    */   }
/* 100:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.window.ServerOpenWindowPacket
 * JD-Core Version:    0.7.0.1
 */