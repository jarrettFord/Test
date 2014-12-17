/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.entity.spawn;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.mc.auth.GameProfile;
/*   5:    */ import org.spacehq.mc.auth.properties.Property;
/*   6:    */ import org.spacehq.mc.auth.properties.PropertyMap;
/*   7:    */ import org.spacehq.mc.protocol.data.game.EntityMetadata;
/*   8:    */ import org.spacehq.mc.protocol.util.NetUtil;
/*   9:    */ import org.spacehq.packetlib.io.NetInput;
/*  10:    */ import org.spacehq.packetlib.io.NetOutput;
/*  11:    */ import org.spacehq.packetlib.packet.Packet;
/*  12:    */ 
/*  13:    */ public class ServerSpawnPlayerPacket
/*  14:    */   implements Packet
/*  15:    */ {
/*  16:    */   private int entityId;
/*  17:    */   private GameProfile profile;
/*  18:    */   private double x;
/*  19:    */   private double y;
/*  20:    */   private double z;
/*  21:    */   private float yaw;
/*  22:    */   private float pitch;
/*  23:    */   private int currentItem;
/*  24:    */   private EntityMetadata[] metadata;
/*  25:    */   
/*  26:    */   private ServerSpawnPlayerPacket() {}
/*  27:    */   
/*  28:    */   public ServerSpawnPlayerPacket(int entityId, GameProfile profile, double x, double y, double z, float yaw, float pitch, int currentItem, EntityMetadata[] metadata)
/*  29:    */   {
/*  30: 30 */     this.entityId = entityId;
/*  31: 31 */     this.profile = profile;
/*  32: 32 */     this.x = x;
/*  33: 33 */     this.y = y;
/*  34: 34 */     this.z = z;
/*  35: 35 */     this.yaw = yaw;
/*  36: 36 */     this.pitch = pitch;
/*  37: 37 */     this.currentItem = currentItem;
/*  38: 38 */     this.metadata = metadata;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public int getEntityId()
/*  42:    */   {
/*  43: 42 */     return this.entityId;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public GameProfile getProfile()
/*  47:    */   {
/*  48: 46 */     return this.profile;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public double getX()
/*  52:    */   {
/*  53: 50 */     return this.x;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public double getY()
/*  57:    */   {
/*  58: 54 */     return this.y;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public double getZ()
/*  62:    */   {
/*  63: 58 */     return this.z;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public float getYaw()
/*  67:    */   {
/*  68: 62 */     return this.yaw;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public float getPitch()
/*  72:    */   {
/*  73: 66 */     return this.pitch;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public int getCurrentItem()
/*  77:    */   {
/*  78: 70 */     return this.currentItem;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public EntityMetadata[] getMetadata()
/*  82:    */   {
/*  83: 74 */     return this.metadata;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public void read(NetInput in)
/*  87:    */     throws IOException
/*  88:    */   {
/*  89: 79 */     this.entityId = in.readVarInt();
/*  90: 80 */     this.profile = new GameProfile(in.readString(), in.readString());
/*  91: 81 */     int numProperties = in.readVarInt();
/*  92: 82 */     for (int count = 0; count < numProperties; count++)
/*  93:    */     {
/*  94: 83 */       String name = in.readString();
/*  95: 84 */       String value = in.readString();
/*  96: 85 */       String signature = in.readString();
/*  97: 86 */       this.profile.getProperties().put(name, new Property(name, value, signature));
/*  98:    */     }
/*  99: 89 */     this.x = (in.readInt() / 32.0D);
/* 100: 90 */     this.y = (in.readInt() / 32.0D);
/* 101: 91 */     this.z = (in.readInt() / 32.0D);
/* 102: 92 */     this.yaw = (in.readByte() * 360 / 256.0F);
/* 103: 93 */     this.pitch = (in.readByte() * 360 / 256.0F);
/* 104: 94 */     this.currentItem = in.readShort();
/* 105: 95 */     this.metadata = NetUtil.readEntityMetadata(in);
/* 106:    */   }
/* 107:    */   
/* 108:    */   public void write(NetOutput out)
/* 109:    */     throws IOException
/* 110:    */   {
/* 111:100 */     out.writeVarInt(this.entityId);
/* 112:101 */     out.writeString(this.profile.getIdAsString());
/* 113:102 */     out.writeString(this.profile.getName());
/* 114:103 */     out.writeVarInt(this.profile.getProperties().size());
/* 115:104 */     for (Property property : this.profile.getProperties().values())
/* 116:    */     {
/* 117:105 */       out.writeString(property.getName());
/* 118:106 */       out.writeString(property.getValue());
/* 119:107 */       out.writeString(property.getSignature());
/* 120:    */     }
/* 121:110 */     out.writeInt((int)(this.x * 32.0D));
/* 122:111 */     out.writeInt((int)(this.y * 32.0D));
/* 123:112 */     out.writeInt((int)(this.z * 32.0D));
/* 124:113 */     out.writeByte((byte)(int)(this.yaw * 256.0F / 360.0F));
/* 125:114 */     out.writeByte((byte)(int)(this.pitch * 256.0F / 360.0F));
/* 126:115 */     out.writeShort(this.currentItem);
/* 127:116 */     NetUtil.writeEntityMetadata(out, this.metadata);
/* 128:    */   }
/* 129:    */   
/* 130:    */   public boolean isPriority()
/* 131:    */   {
/* 132:121 */     return false;
/* 133:    */   }
/* 134:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket
 * JD-Core Version:    0.7.0.1
 */