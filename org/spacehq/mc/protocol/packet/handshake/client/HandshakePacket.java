/*  1:   */ package org.spacehq.mc.protocol.packet.handshake.client;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class HandshakePacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int protocolVersion;
/* 12:   */   private String hostname;
/* 13:   */   private int port;
/* 14:   */   private int intent;
/* 15:   */   
/* 16:   */   private HandshakePacket() {}
/* 17:   */   
/* 18:   */   public HandshakePacket(int protocolVersion, String hostname, int port, int nextState)
/* 19:   */   {
/* 20:21 */     this.protocolVersion = protocolVersion;
/* 21:22 */     this.hostname = hostname;
/* 22:23 */     this.port = port;
/* 23:24 */     this.intent = nextState;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public int getProtocolVersion()
/* 27:   */   {
/* 28:28 */     return this.protocolVersion;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public String getHostName()
/* 32:   */   {
/* 33:32 */     return this.hostname;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public int getPort()
/* 37:   */   {
/* 38:36 */     return this.port;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public int getIntent()
/* 42:   */   {
/* 43:40 */     return this.intent;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public void read(NetInput in)
/* 47:   */     throws IOException
/* 48:   */   {
/* 49:45 */     this.protocolVersion = in.readVarInt();
/* 50:46 */     this.hostname = in.readString();
/* 51:47 */     this.port = in.readUnsignedShort();
/* 52:48 */     this.intent = in.readVarInt();
/* 53:   */   }
/* 54:   */   
/* 55:   */   public void write(NetOutput out)
/* 56:   */     throws IOException
/* 57:   */   {
/* 58:53 */     out.writeVarInt(this.protocolVersion);
/* 59:54 */     out.writeString(this.hostname);
/* 60:55 */     out.writeShort(this.port);
/* 61:56 */     out.writeVarInt(this.intent);
/* 62:   */   }
/* 63:   */   
/* 64:   */   public boolean isPriority()
/* 65:   */   {
/* 66:61 */     return true;
/* 67:   */   }
/* 68:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.handshake.client.HandshakePacket
 * JD-Core Version:    0.7.0.1
 */