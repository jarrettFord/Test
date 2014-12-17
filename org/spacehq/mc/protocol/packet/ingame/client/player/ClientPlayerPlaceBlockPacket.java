/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.client.player;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.mc.protocol.data.game.ItemStack;
/*   5:    */ import org.spacehq.mc.protocol.util.NetUtil;
/*   6:    */ import org.spacehq.packetlib.io.NetInput;
/*   7:    */ import org.spacehq.packetlib.io.NetOutput;
/*   8:    */ import org.spacehq.packetlib.packet.Packet;
/*   9:    */ 
/*  10:    */ public class ClientPlayerPlaceBlockPacket
/*  11:    */   implements Packet
/*  12:    */ {
/*  13:    */   private int x;
/*  14:    */   private int y;
/*  15:    */   private int z;
/*  16:    */   private Face face;
/*  17:    */   private ItemStack held;
/*  18:    */   private float cursorX;
/*  19:    */   private float cursorY;
/*  20:    */   private float cursorZ;
/*  21:    */   
/*  22:    */   private ClientPlayerPlaceBlockPacket() {}
/*  23:    */   
/*  24:    */   public ClientPlayerPlaceBlockPacket(int x, int y, int z, Face face, ItemStack held, float cursorX, float cursorY, float cursorZ)
/*  25:    */   {
/*  26: 27 */     this.x = x;
/*  27: 28 */     this.y = y;
/*  28: 29 */     this.z = z;
/*  29: 30 */     this.face = face;
/*  30: 31 */     this.held = held;
/*  31: 32 */     this.cursorX = cursorX;
/*  32: 33 */     this.cursorY = cursorY;
/*  33: 34 */     this.cursorZ = cursorZ;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public int getX()
/*  37:    */   {
/*  38: 38 */     return this.x;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public int getY()
/*  42:    */   {
/*  43: 42 */     return this.y;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public int getZ()
/*  47:    */   {
/*  48: 46 */     return this.z;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public Face getFace()
/*  52:    */   {
/*  53: 50 */     return this.face;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public ItemStack getHeldItem()
/*  57:    */   {
/*  58: 54 */     return this.held;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public float getCursorX()
/*  62:    */   {
/*  63: 58 */     return this.cursorX;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public float getCursorY()
/*  67:    */   {
/*  68: 62 */     return this.cursorY;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public float getCursorZ()
/*  72:    */   {
/*  73: 66 */     return this.cursorZ;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public void read(NetInput in)
/*  77:    */     throws IOException
/*  78:    */   {
/*  79: 71 */     this.x = in.readInt();
/*  80: 72 */     this.y = in.readUnsignedByte();
/*  81: 73 */     this.z = in.readInt();
/*  82: 74 */     int face = in.readUnsignedByte();
/*  83: 75 */     this.face = (face == 255 ? Face.UNKNOWN : Face.values()[face]);
/*  84: 76 */     this.held = NetUtil.readItem(in);
/*  85: 77 */     this.cursorX = (in.readByte() / 16.0F);
/*  86: 78 */     this.cursorY = (in.readByte() / 16.0F);
/*  87: 79 */     this.cursorZ = (in.readByte() / 16.0F);
/*  88:    */   }
/*  89:    */   
/*  90:    */   public void write(NetOutput out)
/*  91:    */     throws IOException
/*  92:    */   {
/*  93: 84 */     out.writeInt(this.x);
/*  94: 85 */     out.writeByte(this.y);
/*  95: 86 */     out.writeInt(this.z);
/*  96: 87 */     out.writeByte(this.face == Face.UNKNOWN ? 255 : this.face.ordinal());
/*  97: 88 */     NetUtil.writeItem(out, this.held);
/*  98: 89 */     out.writeByte((int)(this.cursorX * 16.0F));
/*  99: 90 */     out.writeByte((int)(this.cursorY * 16.0F));
/* 100: 91 */     out.writeByte((int)(this.cursorZ * 16.0F));
/* 101:    */   }
/* 102:    */   
/* 103:    */   public boolean isPriority()
/* 104:    */   {
/* 105: 96 */     return false;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public static enum Face
/* 109:    */   {
/* 110:100 */     BOTTOM,  TOP,  EAST,  WEST,  NORTH,  SOUTH,  UNKNOWN;
/* 111:    */   }
/* 112:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerPlaceBlockPacket
 * JD-Core Version:    0.7.0.1
 */