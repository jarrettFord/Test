/*  1:   */ package org.spacehq.packetlib.crypt;
/*  2:   */ 
/*  3:   */ import java.security.GeneralSecurityException;
/*  4:   */ import java.security.Key;
/*  5:   */ import javax.crypto.Cipher;
/*  6:   */ import javax.crypto.spec.IvParameterSpec;
/*  7:   */ 
/*  8:   */ public class AESEncryption
/*  9:   */   implements PacketEncryption
/* 10:   */ {
/* 11:   */   private Cipher inCipher;
/* 12:   */   private Cipher outCipher;
/* 13:   */   
/* 14:   */   public AESEncryption(Key key)
/* 15:   */     throws GeneralSecurityException
/* 16:   */   {
/* 17:17 */     this.inCipher = Cipher.getInstance("AES/CFB8/NoPadding");
/* 18:18 */     this.inCipher.init(2, key, new IvParameterSpec(key.getEncoded()));
/* 19:19 */     this.outCipher = Cipher.getInstance("AES/CFB8/NoPadding");
/* 20:20 */     this.outCipher.init(1, key, new IvParameterSpec(key.getEncoded()));
/* 21:   */   }
/* 22:   */   
/* 23:   */   public int getDecryptOutputSize(int length)
/* 24:   */   {
/* 25:25 */     return this.inCipher.getOutputSize(length);
/* 26:   */   }
/* 27:   */   
/* 28:   */   public int getEncryptOutputSize(int length)
/* 29:   */   {
/* 30:30 */     return this.outCipher.getOutputSize(length);
/* 31:   */   }
/* 32:   */   
/* 33:   */   public int decrypt(byte[] input, int inputOffset, int inputLength, byte[] output, int outputOffset)
/* 34:   */     throws Exception
/* 35:   */   {
/* 36:35 */     return this.inCipher.update(input, inputOffset, inputLength, output, outputOffset);
/* 37:   */   }
/* 38:   */   
/* 39:   */   public int encrypt(byte[] input, int inputOffset, int inputLength, byte[] output, int outputOffset)
/* 40:   */     throws Exception
/* 41:   */   {
/* 42:40 */     return this.outCipher.update(input, inputOffset, inputLength, output, outputOffset);
/* 43:   */   }
/* 44:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.crypt.AESEncryption
 * JD-Core Version:    0.7.0.1
 */