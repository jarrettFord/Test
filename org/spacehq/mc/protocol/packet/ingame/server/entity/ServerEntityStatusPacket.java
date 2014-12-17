/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.packetlib.io.NetInput;
/*   5:    */ import org.spacehq.packetlib.io.NetOutput;
/*   6:    */ import org.spacehq.packetlib.packet.Packet;
/*   7:    */ 
/*   8:    */ public class ServerEntityStatusPacket
/*   9:    */   implements Packet
/*  10:    */ {
/*  11:    */   protected int entityId;
/*  12:    */   protected Status status;
/*  13:    */   
/*  14:    */   private ServerEntityStatusPacket() {}
/*  15:    */   
/*  16:    */   public ServerEntityStatusPacket(int entityId, Status status)
/*  17:    */   {
/*  18: 19 */     this.entityId = entityId;
/*  19: 20 */     this.status = status;
/*  20:    */   }
/*  21:    */   
/*  22:    */   public int getEntityId()
/*  23:    */   {
/*  24: 24 */     return this.entityId;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public Status getStatus()
/*  28:    */   {
/*  29: 28 */     return this.status;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public void read(NetInput in)
/*  33:    */     throws IOException
/*  34:    */   {
/*  35: 33 */     this.entityId = in.readInt();
/*  36: 34 */     this.status = valueToStatus(in.readByte());
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void write(NetOutput out)
/*  40:    */     throws IOException
/*  41:    */   {
/*  42: 39 */     out.writeInt(this.entityId);
/*  43: 40 */     out.writeByte(statusToValue(this.status));
/*  44:    */   }
/*  45:    */   
/*  46:    */   public boolean isPriority()
/*  47:    */   {
/*  48: 45 */     return false;
/*  49:    */   }
/*  50:    */   
/*  51:    */   private static byte statusToValue(Status status)
/*  52:    */     throws IOException
/*  53:    */   {
/*  54: 49 */     switch (status)
/*  55:    */     {
/*  56:    */     case ANIMAL_HEARTS: 
/*  57: 51 */       return 1;
/*  58:    */     case DEAD: 
/*  59: 53 */       return 2;
/*  60:    */     case FINISHED_EATING: 
/*  61: 55 */       return 3;
/*  62:    */     case FIREWORK_EXPLODING: 
/*  63: 57 */       return 4;
/*  64:    */     case HURT_OR_MINECART_SPAWNER_DELAY_RESET: 
/*  65: 59 */       return 6;
/*  66:    */     case IRON_GOLEM_ROSE: 
/*  67: 61 */       return 7;
/*  68:    */     case IRON_GOLEM_THROW: 
/*  69: 63 */       return 8;
/*  70:    */     case LIVING_HURT: 
/*  71: 65 */       return 9;
/*  72:    */     case SHEEP_GRAZING_OR_TNT_CART_EXPLODING: 
/*  73: 67 */       return 10;
/*  74:    */     case TAMED: 
/*  75: 69 */       return 11;
/*  76:    */     case TAMING: 
/*  77: 71 */       return 12;
/*  78:    */     case VILLAGER_ANGRY: 
/*  79: 73 */       return 13;
/*  80:    */     case VILLAGER_HAPPY: 
/*  81: 75 */       return 14;
/*  82:    */     case VILLAGER_HEARTS: 
/*  83: 77 */       return 15;
/*  84:    */     case WITCH_MAGIC_PARTICLES: 
/*  85: 79 */       return 16;
/*  86:    */     case WOLF_SHAKING: 
/*  87: 81 */       return 17;
/*  88:    */     case ZOMBIE_VILLAGER_SHAKING: 
/*  89: 83 */       return 18;
/*  90:    */     }
/*  91: 85 */     throw new IOException("Unmapped entity status: " + status);
/*  92:    */   }
/*  93:    */   
/*  94:    */   private static Status valueToStatus(byte value)
/*  95:    */     throws IOException
/*  96:    */   {
/*  97: 90 */     switch (value)
/*  98:    */     {
/*  99:    */     case 1: 
/* 100: 92 */       return Status.HURT_OR_MINECART_SPAWNER_DELAY_RESET;
/* 101:    */     case 2: 
/* 102: 94 */       return Status.LIVING_HURT;
/* 103:    */     case 3: 
/* 104: 96 */       return Status.DEAD;
/* 105:    */     case 4: 
/* 106: 98 */       return Status.IRON_GOLEM_THROW;
/* 107:    */     case 6: 
/* 108:100 */       return Status.TAMING;
/* 109:    */     case 7: 
/* 110:102 */       return Status.TAMED;
/* 111:    */     case 8: 
/* 112:104 */       return Status.WOLF_SHAKING;
/* 113:    */     case 9: 
/* 114:106 */       return Status.FINISHED_EATING;
/* 115:    */     case 10: 
/* 116:108 */       return Status.SHEEP_GRAZING_OR_TNT_CART_EXPLODING;
/* 117:    */     case 11: 
/* 118:110 */       return Status.IRON_GOLEM_ROSE;
/* 119:    */     case 12: 
/* 120:112 */       return Status.VILLAGER_HEARTS;
/* 121:    */     case 13: 
/* 122:114 */       return Status.VILLAGER_ANGRY;
/* 123:    */     case 14: 
/* 124:116 */       return Status.VILLAGER_HAPPY;
/* 125:    */     case 15: 
/* 126:118 */       return Status.WITCH_MAGIC_PARTICLES;
/* 127:    */     case 16: 
/* 128:120 */       return Status.ZOMBIE_VILLAGER_SHAKING;
/* 129:    */     case 17: 
/* 130:122 */       return Status.FIREWORK_EXPLODING;
/* 131:    */     case 18: 
/* 132:124 */       return Status.ANIMAL_HEARTS;
/* 133:    */     }
/* 134:126 */     throw new IOException("Unknown entity status value: " + value);
/* 135:    */   }
/* 136:    */   
/* 137:    */   public static enum Status
/* 138:    */   {
/* 139:131 */     HURT_OR_MINECART_SPAWNER_DELAY_RESET,  LIVING_HURT,  DEAD,  IRON_GOLEM_THROW,  TAMING,  TAMED,  WOLF_SHAKING,  FINISHED_EATING,  SHEEP_GRAZING_OR_TNT_CART_EXPLODING,  IRON_GOLEM_ROSE,  VILLAGER_HEARTS,  VILLAGER_ANGRY,  VILLAGER_HAPPY,  WITCH_MAGIC_PARTICLES,  ZOMBIE_VILLAGER_SHAKING,  FIREWORK_EXPLODING,  ANIMAL_HEARTS;
/* 140:    */   }
/* 141:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityStatusPacket
 * JD-Core Version:    0.7.0.1
 */