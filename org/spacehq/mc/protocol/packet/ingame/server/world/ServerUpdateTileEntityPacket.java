/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.mc.protocol.util.NetUtil;
/*  5:   */ import org.spacehq.opennbt.tag.builtin.CompoundTag;
/*  6:   */ import org.spacehq.packetlib.io.NetInput;
/*  7:   */ import org.spacehq.packetlib.io.NetOutput;
/*  8:   */ import org.spacehq.packetlib.packet.Packet;
/*  9:   */ 
/* 10:   */ public class ServerUpdateTileEntityPacket
/* 11:   */   implements Packet
/* 12:   */ {
/* 13:   */   private int x;
/* 14:   */   private int y;
/* 15:   */   private int z;
/* 16:   */   private Type type;
/* 17:   */   private CompoundTag nbt;
/* 18:   */   
/* 19:   */   private ServerUpdateTileEntityPacket() {}
/* 20:   */   
/* 21:   */   public ServerUpdateTileEntityPacket(int breakerEntityId, int x, int y, int z, Type type, CompoundTag nbt)
/* 22:   */   {
/* 23:24 */     this.x = x;
/* 24:25 */     this.y = y;
/* 25:26 */     this.z = z;
/* 26:27 */     this.type = type;
/* 27:28 */     this.nbt = nbt;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public int getX()
/* 31:   */   {
/* 32:32 */     return this.x;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public int getY()
/* 36:   */   {
/* 37:36 */     return this.y;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public int getZ()
/* 41:   */   {
/* 42:40 */     return this.z;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public Type getType()
/* 46:   */   {
/* 47:44 */     return this.type;
/* 48:   */   }
/* 49:   */   
/* 50:   */   public CompoundTag getNBT()
/* 51:   */   {
/* 52:48 */     return this.nbt;
/* 53:   */   }
/* 54:   */   
/* 55:   */   public void read(NetInput in)
/* 56:   */     throws IOException
/* 57:   */   {
/* 58:53 */     this.x = in.readInt();
/* 59:54 */     this.y = in.readShort();
/* 60:55 */     this.z = in.readInt();
/* 61:56 */     this.type = Type.values()[(in.readUnsignedByte() - 1)];
/* 62:57 */     this.nbt = NetUtil.readNBT(in);
/* 63:   */   }
/* 64:   */   
/* 65:   */   public void write(NetOutput out)
/* 66:   */     throws IOException
/* 67:   */   {
/* 68:62 */     out.writeInt(this.x);
/* 69:63 */     out.writeShort(this.y);
/* 70:64 */     out.writeInt(this.z);
/* 71:65 */     out.writeByte(this.type.ordinal() + 1);
/* 72:66 */     NetUtil.writeNBT(out, this.nbt);
/* 73:   */   }
/* 74:   */   
/* 75:   */   public boolean isPriority()
/* 76:   */   {
/* 77:71 */     return false;
/* 78:   */   }
/* 79:   */   
/* 80:   */   public static enum Type
/* 81:   */   {
/* 82:75 */     MOB_SPAWNER,  COMMAND_BLOCK,  BEACON,  SKULL,  FLOWER_POT;
/* 83:   */   }
/* 84:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerUpdateTileEntityPacket
 * JD-Core Version:    0.7.0.1
 */