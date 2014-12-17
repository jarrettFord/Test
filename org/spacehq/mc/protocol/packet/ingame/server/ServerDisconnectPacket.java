/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.mc.protocol.data.message.Message;
/*  5:   */ import org.spacehq.packetlib.io.NetInput;
/*  6:   */ import org.spacehq.packetlib.io.NetOutput;
/*  7:   */ import org.spacehq.packetlib.packet.Packet;
/*  8:   */ 
/*  9:   */ public class ServerDisconnectPacket
/* 10:   */   implements Packet
/* 11:   */ {
/* 12:   */   private Message message;
/* 13:   */   
/* 14:   */   private ServerDisconnectPacket() {}
/* 15:   */   
/* 16:   */   public ServerDisconnectPacket(String text)
/* 17:   */   {
/* 18:19 */     this(Message.fromString(text));
/* 19:   */   }
/* 20:   */   
/* 21:   */   public ServerDisconnectPacket(Message message)
/* 22:   */   {
/* 23:23 */     this.message = message;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public Message getReason()
/* 27:   */   {
/* 28:27 */     return this.message;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public void read(NetInput in)
/* 32:   */     throws IOException
/* 33:   */   {
/* 34:32 */     this.message = Message.fromString(in.readString());
/* 35:   */   }
/* 36:   */   
/* 37:   */   public void write(NetOutput out)
/* 38:   */     throws IOException
/* 39:   */   {
/* 40:37 */     out.writeString(this.message.toJsonString());
/* 41:   */   }
/* 42:   */   
/* 43:   */   public boolean isPriority()
/* 44:   */   {
/* 45:42 */     return true;
/* 46:   */   }
/* 47:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.ServerDisconnectPacket
 * JD-Core Version:    0.7.0.1
 */