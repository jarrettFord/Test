/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerPluginMessagePacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private String channel;
/* 12:   */   private byte[] data;
/* 13:   */   
/* 14:   */   private ServerPluginMessagePacket() {}
/* 15:   */   
/* 16:   */   public ServerPluginMessagePacket(String channel, byte[] data)
/* 17:   */   {
/* 18:19 */     this.channel = channel;
/* 19:20 */     this.data = data;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public String getChannel()
/* 23:   */   {
/* 24:24 */     return this.channel;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public byte[] getData()
/* 28:   */   {
/* 29:28 */     return this.data;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public void read(NetInput in)
/* 33:   */     throws IOException
/* 34:   */   {
/* 35:33 */     this.channel = in.readString();
/* 36:34 */     this.data = in.readBytes(in.readShort());
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void write(NetOutput out)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     out.writeString(this.channel);
/* 43:40 */     out.writeShort(this.data.length);
/* 44:41 */     out.writeBytes(this.data);
/* 45:   */   }
/* 46:   */   
/* 47:   */   public boolean isPriority()
/* 48:   */   {
/* 49:46 */     return false;
/* 50:   */   }
/* 51:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.ServerPluginMessagePacket
 * JD-Core Version:    0.7.0.1
 */