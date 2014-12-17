/*  1:   */ package org.spacehq.mc.protocol.util;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import java.io.UnsupportedEncodingException;
/*  5:   */ import java.security.GeneralSecurityException;
/*  6:   */ import java.security.Key;
/*  7:   */ import java.security.KeyFactory;
/*  8:   */ import java.security.KeyPair;
/*  9:   */ import java.security.KeyPairGenerator;
/* 10:   */ import java.security.MessageDigest;
/* 11:   */ import java.security.NoSuchAlgorithmException;
/* 12:   */ import java.security.PrivateKey;
/* 13:   */ import java.security.PublicKey;
/* 14:   */ import java.security.spec.X509EncodedKeySpec;
/* 15:   */ import javax.crypto.Cipher;
/* 16:   */ import javax.crypto.KeyGenerator;
/* 17:   */ import javax.crypto.SecretKey;
/* 18:   */ import javax.crypto.spec.SecretKeySpec;
/* 19:   */ 
/* 20:   */ public class CryptUtil
/* 21:   */ {
/* 22:   */   public static SecretKey generateSharedKey()
/* 23:   */   {
/* 24:   */     try
/* 25:   */     {
/* 26:25 */       KeyGenerator gen = KeyGenerator.getInstance("AES");
/* 27:26 */       gen.init(128);
/* 28:27 */       return gen.generateKey();
/* 29:   */     }
/* 30:   */     catch (NoSuchAlgorithmException e)
/* 31:   */     {
/* 32:29 */       throw new Error("Failed to generate shared key.", e);
/* 33:   */     }
/* 34:   */   }
/* 35:   */   
/* 36:   */   public static KeyPair generateKeyPair()
/* 37:   */   {
/* 38:   */     try
/* 39:   */     {
/* 40:35 */       KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
/* 41:36 */       gen.initialize(1024);
/* 42:37 */       return gen.generateKeyPair();
/* 43:   */     }
/* 44:   */     catch (NoSuchAlgorithmException e)
/* 45:   */     {
/* 46:39 */       throw new Error("Failed to generate key pair.", e);
/* 47:   */     }
/* 48:   */   }
/* 49:   */   
/* 50:   */   public static PublicKey decodePublicKey(byte[] bytes)
/* 51:   */     throws IOException
/* 52:   */   {
/* 53:   */     try
/* 54:   */     {
/* 55:45 */       return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
/* 56:   */     }
/* 57:   */     catch (GeneralSecurityException e)
/* 58:   */     {
/* 59:47 */       throw new IOException("Could not decrypt public key.", e);
/* 60:   */     }
/* 61:   */   }
/* 62:   */   
/* 63:   */   public static SecretKey decryptSharedKey(PrivateKey privateKey, byte[] sharedKey)
/* 64:   */   {
/* 65:52 */     return new SecretKeySpec(decryptData(privateKey, sharedKey), "AES");
/* 66:   */   }
/* 67:   */   
/* 68:   */   public static byte[] encryptData(Key key, byte[] data)
/* 69:   */   {
/* 70:56 */     return runEncryption(1, key, data);
/* 71:   */   }
/* 72:   */   
/* 73:   */   public static byte[] decryptData(Key key, byte[] data)
/* 74:   */   {
/* 75:60 */     return runEncryption(2, key, data);
/* 76:   */   }
/* 77:   */   
/* 78:   */   private static byte[] runEncryption(int mode, Key key, byte[] data)
/* 79:   */   {
/* 80:   */     try
/* 81:   */     {
/* 82:65 */       Cipher cipher = Cipher.getInstance(key.getAlgorithm());
/* 83:66 */       cipher.init(mode, key);
/* 84:67 */       return cipher.doFinal(data);
/* 85:   */     }
/* 86:   */     catch (GeneralSecurityException e)
/* 87:   */     {
/* 88:69 */       throw new Error("Failed to run encryption.", e);
/* 89:   */     }
/* 90:   */   }
/* 91:   */   
/* 92:   */   public static byte[] getServerIdHash(String serverId, PublicKey publicKey, SecretKey secretKey)
/* 93:   */   {
/* 94:   */     try
/* 95:   */     {
/* 96:75 */       return encrypt("SHA-1", new byte[][] { serverId.getBytes("ISO_8859_1"), secretKey.getEncoded(), publicKey.getEncoded() });
/* 97:   */     }
/* 98:   */     catch (UnsupportedEncodingException e)
/* 99:   */     {
/* :0:77 */       throw new Error("Failed to generate server id hash.", e);
/* :1:   */     }
/* :2:   */   }
/* :3:   */   
/* :4:   */   private static byte[] encrypt(String encryption, byte[]... data)
/* :5:   */   {
/* :6:   */     try
/* :7:   */     {
/* :8:83 */       MessageDigest digest = MessageDigest.getInstance(encryption);
/* :9:84 */       for (byte[] array : data) {
/* ;0:85 */         digest.update(array);
/* ;1:   */       }
/* ;2:88 */       return digest.digest();
/* ;3:   */     }
/* ;4:   */     catch (NoSuchAlgorithmException e)
/* ;5:   */     {
/* ;6:90 */       throw new Error("Failed to encrypt data.", e);
/* ;7:   */     }
/* ;8:   */   }
/* ;9:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.util.CryptUtil
 * JD-Core Version:    0.7.0.1
 */