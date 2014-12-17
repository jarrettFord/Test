/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.entity.spawn;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.packetlib.io.NetInput;
/*   5:    */ import org.spacehq.packetlib.io.NetOutput;
/*   6:    */ import org.spacehq.packetlib.packet.Packet;
/*   7:    */ 
/*   8:    */ public class ServerSpawnPaintingPacket
/*   9:    */   implements Packet
/*  10:    */ {
/*  11:    */   private int entityId;
/*  12:    */   private Art art;
/*  13:    */   private int x;
/*  14:    */   private int y;
/*  15:    */   private int z;
/*  16:    */   private Direction direction;
/*  17:    */   
/*  18:    */   private ServerSpawnPaintingPacket() {}
/*  19:    */   
/*  20:    */   public ServerSpawnPaintingPacket(int entityId, Art art, int x, int y, int z, Direction direction)
/*  21:    */   {
/*  22: 23 */     this.entityId = entityId;
/*  23: 24 */     this.art = art;
/*  24: 25 */     this.x = x;
/*  25: 26 */     this.y = y;
/*  26: 27 */     this.z = z;
/*  27: 28 */     this.direction = direction;
/*  28:    */   }
/*  29:    */   
/*  30:    */   public int getEntityId()
/*  31:    */   {
/*  32: 32 */     return this.entityId;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public Art getArt()
/*  36:    */   {
/*  37: 36 */     return this.art;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public int getX()
/*  41:    */   {
/*  42: 40 */     return this.x;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public int getY()
/*  46:    */   {
/*  47: 44 */     return this.y;
/*  48:    */   }
/*  49:    */   
/*  50:    */   public int getZ()
/*  51:    */   {
/*  52: 48 */     return this.z;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public Direction getDirection()
/*  56:    */   {
/*  57: 52 */     return this.direction;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public void read(NetInput in)
/*  61:    */     throws IOException
/*  62:    */   {
/*  63: 57 */     this.entityId = in.readVarInt();
/*  64: 58 */     this.art = Art.valueOf(in.readString());
/*  65: 59 */     this.x = in.readInt();
/*  66: 60 */     this.y = in.readInt();
/*  67: 61 */     this.z = in.readInt();
/*  68: 62 */     this.direction = Direction.values()[in.readInt()];
/*  69:    */   }
/*  70:    */   
/*  71:    */   public void write(NetOutput out)
/*  72:    */     throws IOException
/*  73:    */   {
/*  74: 67 */     out.writeVarInt(this.entityId);
/*  75: 68 */     out.writeString(this.art.name());
/*  76: 69 */     out.writeInt(this.x);
/*  77: 70 */     out.writeInt(this.y);
/*  78: 71 */     out.writeInt(this.z);
/*  79: 72 */     out.writeInt(this.direction.ordinal());
/*  80:    */   }
/*  81:    */   
/*  82:    */   public boolean isPriority()
/*  83:    */   {
/*  84: 77 */     return false;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public static enum Direction
/*  88:    */   {
/*  89: 81 */     BOTTOM,  TOP,  EAST,  WEST,  NORTH,  SOUTH;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public static enum Art
/*  93:    */   {
/*  94: 90 */     Kebab,  Aztec,  Alban,  Aztec2,  Bomb,  Plant,  Wasteland,  Pool,  Courbet,  Sea,  Sunset,  Creebet,  Wanderer,  Graham,  Match,  Bust,  Stage,  Void,  SkullAndRoses,  Wither,  Fighters,  Pointer,  Pigscene,  BurningSkull,  Skeleton,  DonkeyKong;
/*  95:    */   }
/*  96:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPaintingPacket
 * JD-Core Version:    0.7.0.1
 */