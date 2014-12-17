/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerBlockBreakAnimPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int breakerEntityId;
/* 12:   */   private int x;
/* 13:   */   private int y;
/* 14:   */   private int z;
/* 15:   */   private Stage stage;
/* 16:   */   
/* 17:   */   private ServerBlockBreakAnimPacket() {}
/* 18:   */   
/* 19:   */   public ServerBlockBreakAnimPacket(int breakerEntityId, int x, int y, int z, Stage stage)
/* 20:   */   {
/* 21:22 */     this.breakerEntityId = breakerEntityId;
/* 22:23 */     this.x = x;
/* 23:24 */     this.y = y;
/* 24:25 */     this.z = z;
/* 25:26 */     this.stage = stage;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public int getBreakerEntityId()
/* 29:   */   {
/* 30:30 */     return this.breakerEntityId;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public int getX()
/* 34:   */   {
/* 35:34 */     return this.x;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public int getY()
/* 39:   */   {
/* 40:38 */     return this.y;
/* 41:   */   }
/* 42:   */   
/* 43:   */   public int getZ()
/* 44:   */   {
/* 45:42 */     return this.z;
/* 46:   */   }
/* 47:   */   
/* 48:   */   public Stage getStage()
/* 49:   */   {
/* 50:46 */     return this.stage;
/* 51:   */   }
/* 52:   */   
/* 53:   */   public void read(NetInput in)
/* 54:   */     throws IOException
/* 55:   */   {
/* 56:51 */     this.breakerEntityId = in.readVarInt();
/* 57:52 */     this.x = in.readInt();
/* 58:53 */     this.y = in.readInt();
/* 59:54 */     this.z = in.readInt();
/* 60:   */     try
/* 61:   */     {
/* 62:56 */       this.stage = Stage.values()[in.readUnsignedByte()];
/* 63:   */     }
/* 64:   */     catch (ArrayIndexOutOfBoundsException e)
/* 65:   */     {
/* 66:58 */       this.stage = Stage.RESET;
/* 67:   */     }
/* 68:   */   }
/* 69:   */   
/* 70:   */   public void write(NetOutput out)
/* 71:   */     throws IOException
/* 72:   */   {
/* 73:64 */     out.writeVarInt(this.breakerEntityId);
/* 74:65 */     out.writeInt(this.x);
/* 75:66 */     out.writeInt(this.y);
/* 76:67 */     out.writeInt(this.z);
/* 77:68 */     out.writeByte(this.stage == Stage.RESET ? -1 : this.stage.ordinal());
/* 78:   */   }
/* 79:   */   
/* 80:   */   public boolean isPriority()
/* 81:   */   {
/* 82:73 */     return false;
/* 83:   */   }
/* 84:   */   
/* 85:   */   public static enum Stage
/* 86:   */   {
/* 87:77 */     STAGE_1,  STAGE_2,  STAGE_3,  STAGE_4,  STAGE_5,  STAGE_6,  STAGE_7,  STAGE_8,  STAGE_9,  STAGE_10,  RESET;
/* 88:   */   }
/* 89:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockBreakAnimPacket
 * JD-Core Version:    0.7.0.1
 */