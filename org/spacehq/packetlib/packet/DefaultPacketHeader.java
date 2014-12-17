/*  1:   */ package org.spacehq.packetlib.packet;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ 
/*  7:   */ public class DefaultPacketHeader
/*  8:   */   implements PacketHeader
/*  9:   */ {
/* 10:   */   public boolean isLengthVariable()
/* 11:   */   {
/* 12:12 */     return true;
/* 13:   */   }
/* 14:   */   
/* 15:   */   public int getLengthSize()
/* 16:   */   {
/* 17:17 */     return 5;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public int getLengthSize(int length)
/* 21:   */   {
/* 22:22 */     return varintLength(length);
/* 23:   */   }
/* 24:   */   
/* 25:   */   public int readLength(NetInput in, int available)
/* 26:   */     throws IOException
/* 27:   */   {
/* 28:27 */     return in.readVarInt();
/* 29:   */   }
/* 30:   */   
/* 31:   */   public void writeLength(NetOutput out, int length)
/* 32:   */     throws IOException
/* 33:   */   {
/* 34:32 */     out.writeVarInt(length);
/* 35:   */   }
/* 36:   */   
/* 37:   */   public int readPacketId(NetInput in)
/* 38:   */     throws IOException
/* 39:   */   {
/* 40:37 */     return in.readVarInt();
/* 41:   */   }
/* 42:   */   
/* 43:   */   public void writePacketId(NetOutput out, int packetId)
/* 44:   */     throws IOException
/* 45:   */   {
/* 46:42 */     out.writeVarInt(packetId);
/* 47:   */   }
/* 48:   */   
/* 49:   */   private static int varintLength(int i)
/* 50:   */   {
/* 51:46 */     if ((i & 0xFFFFFF80) == 0) {
/* 52:47 */       return 1;
/* 53:   */     }
/* 54:48 */     if ((i & 0xFFFFC000) == 0) {
/* 55:49 */       return 2;
/* 56:   */     }
/* 57:50 */     if ((i & 0xFFE00000) == 0) {
/* 58:51 */       return 3;
/* 59:   */     }
/* 60:52 */     if ((i & 0xF0000000) == 0) {
/* 61:53 */       return 4;
/* 62:   */     }
/* 63:55 */     return 5;
/* 64:   */   }
/* 65:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.packet.DefaultPacketHeader
 * JD-Core Version:    0.7.0.1
 */