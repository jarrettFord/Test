/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.client.world;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ClientUpdateSignPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int x;
/* 12:   */   private int y;
/* 13:   */   private int z;
/* 14:   */   private String[] lines;
/* 15:   */   
/* 16:   */   private ClientUpdateSignPacket() {}
/* 17:   */   
/* 18:   */   public ClientUpdateSignPacket(int x, int y, int z, String[] lines)
/* 19:   */   {
/* 20:21 */     if (lines.length != 4) {
/* 21:22 */       throw new IllegalArgumentException("Lines must contain exactly 4 strings!");
/* 22:   */     }
/* 23:25 */     this.x = x;
/* 24:26 */     this.y = y;
/* 25:27 */     this.z = z;
/* 26:28 */     this.lines = lines;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public int getX()
/* 30:   */   {
/* 31:32 */     return this.x;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public int getY()
/* 35:   */   {
/* 36:36 */     return this.y;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public int getZ()
/* 40:   */   {
/* 41:40 */     return this.z;
/* 42:   */   }
/* 43:   */   
/* 44:   */   public String[] getLines()
/* 45:   */   {
/* 46:44 */     return this.lines;
/* 47:   */   }
/* 48:   */   
/* 49:   */   public void read(NetInput in)
/* 50:   */     throws IOException
/* 51:   */   {
/* 52:49 */     this.x = in.readInt();
/* 53:50 */     this.y = in.readShort();
/* 54:51 */     this.z = in.readInt();
/* 55:52 */     this.lines = new String[4];
/* 56:53 */     for (int count = 0; count < this.lines.length; count++) {
/* 57:54 */       this.lines[count] = in.readString();
/* 58:   */     }
/* 59:   */   }
/* 60:   */   
/* 61:   */   public void write(NetOutput out)
/* 62:   */     throws IOException
/* 63:   */   {
/* 64:60 */     out.writeInt(this.x);
/* 65:61 */     out.writeShort(this.y);
/* 66:62 */     out.writeInt(this.z);
/* 67:63 */     for (String line : this.lines) {
/* 68:64 */       out.writeString(line);
/* 69:   */     }
/* 70:   */   }
/* 71:   */   
/* 72:   */   public boolean isPriority()
/* 73:   */   {
/* 74:70 */     return false;
/* 75:   */   }
/* 76:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.world.ClientUpdateSignPacket
 * JD-Core Version:    0.7.0.1
 */