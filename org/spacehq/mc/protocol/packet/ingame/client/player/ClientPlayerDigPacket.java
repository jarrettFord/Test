/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.client.player;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.packetlib.io.NetInput;
/*   5:    */ import org.spacehq.packetlib.io.NetOutput;
/*   6:    */ import org.spacehq.packetlib.packet.Packet;
/*   7:    */ 
/*   8:    */ public class ClientPlayerDigPacket
/*   9:    */   implements Packet
/*  10:    */ {
/*  11:    */   private Status status;
/*  12:    */   private int x;
/*  13:    */   private int y;
/*  14:    */   private int z;
/*  15:    */   private Face face;
/*  16:    */   
/*  17:    */   private ClientPlayerDigPacket() {}
/*  18:    */   
/*  19:    */   public ClientPlayerDigPacket(Status status, int x, int y, int z, Face face)
/*  20:    */   {
/*  21: 22 */     this.status = status;
/*  22: 23 */     this.x = x;
/*  23: 24 */     this.y = y;
/*  24: 25 */     this.z = z;
/*  25: 26 */     this.face = face;
/*  26:    */   }
/*  27:    */   
/*  28:    */   public Status getStatus()
/*  29:    */   {
/*  30: 30 */     return this.status;
/*  31:    */   }
/*  32:    */   
/*  33:    */   public int getX()
/*  34:    */   {
/*  35: 34 */     return this.x;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public int getY()
/*  39:    */   {
/*  40: 38 */     return this.y;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public int getZ()
/*  44:    */   {
/*  45: 42 */     return this.z;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public Face getFace()
/*  49:    */   {
/*  50: 46 */     return this.face;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public void read(NetInput in)
/*  54:    */     throws IOException
/*  55:    */   {
/*  56: 51 */     this.status = Status.values()[in.readUnsignedByte()];
/*  57: 52 */     this.x = in.readInt();
/*  58: 53 */     this.y = in.readUnsignedByte();
/*  59: 54 */     this.z = in.readInt();
/*  60: 55 */     this.face = valueToFace(in.readUnsignedByte());
/*  61:    */   }
/*  62:    */   
/*  63:    */   public void write(NetOutput out)
/*  64:    */     throws IOException
/*  65:    */   {
/*  66: 60 */     out.writeByte(this.status.ordinal());
/*  67: 61 */     out.writeInt(this.x);
/*  68: 62 */     out.writeByte(this.y);
/*  69: 63 */     out.writeInt(this.z);
/*  70: 64 */     out.writeByte(faceToValue(this.face));
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean isPriority()
/*  74:    */   {
/*  75: 69 */     return false;
/*  76:    */   }
/*  77:    */   
/*  78:    */   private static Face valueToFace(int value)
/*  79:    */   {
/*  80: 73 */     switch (value)
/*  81:    */     {
/*  82:    */     case 0: 
/*  83: 75 */       return Face.BOTTOM;
/*  84:    */     case 1: 
/*  85: 77 */       return Face.TOP;
/*  86:    */     case 2: 
/*  87: 79 */       return Face.EAST;
/*  88:    */     case 3: 
/*  89: 81 */       return Face.WEST;
/*  90:    */     case 4: 
/*  91: 83 */       return Face.NORTH;
/*  92:    */     case 5: 
/*  93: 85 */       return Face.SOUTH;
/*  94:    */     }
/*  95: 87 */     return Face.INVALID;
/*  96:    */   }
/*  97:    */   
/*  98:    */   private static int faceToValue(Face face)
/*  99:    */   {
/* 100: 92 */     switch (face)
/* 101:    */     {
/* 102:    */     case BOTTOM: 
/* 103: 94 */       return 0;
/* 104:    */     case EAST: 
/* 105: 96 */       return 1;
/* 106:    */     case INVALID: 
/* 107: 98 */       return 2;
/* 108:    */     case NORTH: 
/* 109:100 */       return 3;
/* 110:    */     case SOUTH: 
/* 111:102 */       return 4;
/* 112:    */     case TOP: 
/* 113:104 */       return 5;
/* 114:    */     }
/* 115:106 */     return 255;
/* 116:    */   }
/* 117:    */   
/* 118:    */   public static enum Status
/* 119:    */   {
/* 120:111 */     START_DIGGING,  CANCEL_DIGGING,  FINISH_DIGGING,  DROP_ITEM_STACK,  DROP_ITEM,  SHOOT_ARROW_OR_FINISH_EATING;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public static enum Face
/* 124:    */   {
/* 125:120 */     BOTTOM,  TOP,  EAST,  WEST,  NORTH,  SOUTH,  INVALID;
/* 126:    */   }
/* 127:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerDigPacket
 * JD-Core Version:    0.7.0.1
 */