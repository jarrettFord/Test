/*  1:   */ package org.spacehq.mc.protocol.packet.login.server;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import java.security.PublicKey;
/*  5:   */ import org.spacehq.mc.protocol.util.CryptUtil;
/*  6:   */ import org.spacehq.packetlib.io.NetInput;
/*  7:   */ import org.spacehq.packetlib.io.NetOutput;
/*  8:   */ import org.spacehq.packetlib.packet.Packet;
/*  9:   */ 
/* 10:   */ public class EncryptionRequestPacket
/* 11:   */   implements Packet
/* 12:   */ {
/* 13:   */   private String serverId;
/* 14:   */   private PublicKey publicKey;
/* 15:   */   private byte[] verifyToken;
/* 16:   */   
/* 17:   */   private EncryptionRequestPacket() {}
/* 18:   */   
/* 19:   */   public EncryptionRequestPacket(String serverId, PublicKey publicKey, byte[] verifyToken)
/* 20:   */   {
/* 21:22 */     this.serverId = serverId;
/* 22:23 */     this.publicKey = publicKey;
/* 23:24 */     this.verifyToken = verifyToken;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public String getServerId()
/* 27:   */   {
/* 28:28 */     return this.serverId;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public PublicKey getPublicKey()
/* 32:   */   {
/* 33:32 */     return this.publicKey;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public byte[] getVerifyToken()
/* 37:   */   {
/* 38:36 */     return this.verifyToken;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public void read(NetInput in)
/* 42:   */     throws IOException
/* 43:   */   {
/* 44:41 */     this.serverId = in.readString();
/* 45:42 */     this.publicKey = CryptUtil.decodePublicKey(in.readPrefixedBytes());
/* 46:43 */     this.verifyToken = in.readPrefixedBytes();
/* 47:   */   }
/* 48:   */   
/* 49:   */   public void write(NetOutput out)
/* 50:   */     throws IOException
/* 51:   */   {
/* 52:48 */     out.writeString(this.serverId);
/* 53:49 */     out.writePrefixedBytes(this.publicKey.getEncoded());
/* 54:50 */     out.writePrefixedBytes(this.verifyToken);
/* 55:   */   }
/* 56:   */   
/* 57:   */   public boolean isPriority()
/* 58:   */   {
/* 59:55 */     return true;
/* 60:   */   }
/* 61:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.login.server.EncryptionRequestPacket
 * JD-Core Version:    0.7.0.1
 */