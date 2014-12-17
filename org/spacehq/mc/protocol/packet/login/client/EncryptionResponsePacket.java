/*  1:   */ package org.spacehq.mc.protocol.packet.login.client;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import java.security.PrivateKey;
/*  5:   */ import java.security.PublicKey;
/*  6:   */ import javax.crypto.SecretKey;
/*  7:   */ import org.spacehq.mc.protocol.util.CryptUtil;
/*  8:   */ import org.spacehq.packetlib.io.NetInput;
/*  9:   */ import org.spacehq.packetlib.io.NetOutput;
/* 10:   */ import org.spacehq.packetlib.packet.Packet;
/* 11:   */ 
/* 12:   */ public class EncryptionResponsePacket
/* 13:   */   implements Packet
/* 14:   */ {
/* 15:   */   private byte[] sharedKey;
/* 16:   */   private byte[] verifyToken;
/* 17:   */   
/* 18:   */   private EncryptionResponsePacket() {}
/* 19:   */   
/* 20:   */   public EncryptionResponsePacket(SecretKey secretKey, PublicKey publicKey, byte[] verifyToken)
/* 21:   */   {
/* 22:24 */     this.sharedKey = CryptUtil.encryptData(publicKey, secretKey.getEncoded());
/* 23:25 */     this.verifyToken = CryptUtil.encryptData(publicKey, verifyToken);
/* 24:   */   }
/* 25:   */   
/* 26:   */   public SecretKey getSecretKey(PrivateKey privateKey)
/* 27:   */   {
/* 28:29 */     return CryptUtil.decryptSharedKey(privateKey, this.sharedKey);
/* 29:   */   }
/* 30:   */   
/* 31:   */   public byte[] getVerifyToken(PrivateKey privateKey)
/* 32:   */   {
/* 33:33 */     return CryptUtil.decryptData(privateKey, this.verifyToken);
/* 34:   */   }
/* 35:   */   
/* 36:   */   public void read(NetInput in)
/* 37:   */     throws IOException
/* 38:   */   {
/* 39:38 */     this.sharedKey = in.readPrefixedBytes();
/* 40:39 */     this.verifyToken = in.readPrefixedBytes();
/* 41:   */   }
/* 42:   */   
/* 43:   */   public void write(NetOutput out)
/* 44:   */     throws IOException
/* 45:   */   {
/* 46:44 */     out.writePrefixedBytes(this.sharedKey);
/* 47:45 */     out.writePrefixedBytes(this.verifyToken);
/* 48:   */   }
/* 49:   */   
/* 50:   */   public boolean isPriority()
/* 51:   */   {
/* 52:50 */     return true;
/* 53:   */   }
/* 54:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.login.client.EncryptionResponsePacket
 * JD-Core Version:    0.7.0.1
 */