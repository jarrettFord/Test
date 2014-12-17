/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.util.ArrayList;
/*   5:    */ import java.util.List;
/*   6:    */ import org.spacehq.packetlib.io.NetInput;
/*   7:    */ import org.spacehq.packetlib.io.NetOutput;
/*   8:    */ import org.spacehq.packetlib.packet.Packet;
/*   9:    */ 
/*  10:    */ public class ServerExplosionPacket
/*  11:    */   implements Packet
/*  12:    */ {
/*  13:    */   private float x;
/*  14:    */   private float y;
/*  15:    */   private float z;
/*  16:    */   private float radius;
/*  17:    */   private List<ExplodedBlockRecord> exploded;
/*  18:    */   private float pushX;
/*  19:    */   private float pushY;
/*  20:    */   private float pushZ;
/*  21:    */   
/*  22:    */   private ServerExplosionPacket() {}
/*  23:    */   
/*  24:    */   public ServerExplosionPacket(float x, float y, float z, float radius, List<ExplodedBlockRecord> exploded, float pushX, float pushY, float pushZ)
/*  25:    */   {
/*  26: 27 */     this.x = x;
/*  27: 28 */     this.y = y;
/*  28: 29 */     this.z = z;
/*  29: 30 */     this.radius = radius;
/*  30: 31 */     this.exploded = exploded;
/*  31: 32 */     this.pushX = pushX;
/*  32: 33 */     this.pushY = pushY;
/*  33: 34 */     this.pushZ = pushZ;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public float getX()
/*  37:    */   {
/*  38: 38 */     return this.x;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public float getY()
/*  42:    */   {
/*  43: 42 */     return this.y;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public float getZ()
/*  47:    */   {
/*  48: 46 */     return this.z;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public float getRadius()
/*  52:    */   {
/*  53: 50 */     return this.radius;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public List<ExplodedBlockRecord> getExploded()
/*  57:    */   {
/*  58: 54 */     return this.exploded;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public float getPushX()
/*  62:    */   {
/*  63: 58 */     return this.pushX;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public float getPushY()
/*  67:    */   {
/*  68: 62 */     return this.pushY;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public float getPushZ()
/*  72:    */   {
/*  73: 66 */     return this.pushZ;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public void read(NetInput in)
/*  77:    */     throws IOException
/*  78:    */   {
/*  79: 71 */     this.x = in.readFloat();
/*  80: 72 */     this.y = in.readFloat();
/*  81: 73 */     this.z = in.readFloat();
/*  82: 74 */     this.radius = in.readFloat();
/*  83: 75 */     this.exploded = new ArrayList();
/*  84: 76 */     int length = in.readInt();
/*  85: 77 */     for (int count = 0; count < length; count++) {
/*  86: 78 */       this.exploded.add(new ExplodedBlockRecord(in.readByte(), in.readByte(), in.readByte()));
/*  87:    */     }
/*  88: 81 */     this.pushX = in.readFloat();
/*  89: 82 */     this.pushY = in.readFloat();
/*  90: 83 */     this.pushZ = in.readFloat();
/*  91:    */   }
/*  92:    */   
/*  93:    */   public void write(NetOutput out)
/*  94:    */     throws IOException
/*  95:    */   {
/*  96: 88 */     out.writeFloat(this.x);
/*  97: 89 */     out.writeFloat(this.y);
/*  98: 90 */     out.writeFloat(this.z);
/*  99: 91 */     out.writeFloat(this.radius);
/* 100: 92 */     out.writeInt(this.exploded.size());
/* 101: 93 */     for (ExplodedBlockRecord record : this.exploded)
/* 102:    */     {
/* 103: 94 */       out.writeByte(record.getX());
/* 104: 95 */       out.writeByte(record.getY());
/* 105: 96 */       out.writeByte(record.getZ());
/* 106:    */     }
/* 107: 99 */     out.writeFloat(this.pushX);
/* 108:100 */     out.writeFloat(this.pushY);
/* 109:101 */     out.writeFloat(this.pushZ);
/* 110:    */   }
/* 111:    */   
/* 112:    */   public boolean isPriority()
/* 113:    */   {
/* 114:106 */     return false;
/* 115:    */   }
/* 116:    */   
/* 117:    */   public static class ExplodedBlockRecord
/* 118:    */   {
/* 119:    */     private int x;
/* 120:    */     private int y;
/* 121:    */     private int z;
/* 122:    */     
/* 123:    */     public ExplodedBlockRecord(int x, int y, int z)
/* 124:    */     {
/* 125:115 */       this.x = x;
/* 126:116 */       this.y = y;
/* 127:117 */       this.z = z;
/* 128:    */     }
/* 129:    */     
/* 130:    */     public int getX()
/* 131:    */     {
/* 132:121 */       return this.x;
/* 133:    */     }
/* 134:    */     
/* 135:    */     public int getY()
/* 136:    */     {
/* 137:125 */       return this.y;
/* 138:    */     }
/* 139:    */     
/* 140:    */     public int getZ()
/* 141:    */     {
/* 142:129 */       return this.z;
/* 143:    */     }
/* 144:    */   }
/* 145:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerExplosionPacket
 * JD-Core Version:    0.7.0.1
 */