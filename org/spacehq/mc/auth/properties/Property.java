/*  1:   */ package org.spacehq.mc.auth.properties;
/*  2:   */ 
/*  3:   */ import java.security.PublicKey;
/*  4:   */ import java.security.Signature;
/*  5:   */ import org.spacehq.mc.auth.exception.SignatureValidateException;
/*  6:   */ import org.spacehq.mc.auth.util.Base64;
/*  7:   */ 
/*  8:   */ public class Property
/*  9:   */ {
/* 10:   */   private String name;
/* 11:   */   private String value;
/* 12:   */   private String signature;
/* 13:   */   
/* 14:   */   public Property(String value, String name)
/* 15:   */   {
/* 16:16 */     this(value, name, null);
/* 17:   */   }
/* 18:   */   
/* 19:   */   public Property(String name, String value, String signature)
/* 20:   */   {
/* 21:20 */     this.name = name;
/* 22:21 */     this.value = value;
/* 23:22 */     this.signature = signature;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public String getName()
/* 27:   */   {
/* 28:26 */     return this.name;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public String getValue()
/* 32:   */   {
/* 33:30 */     return this.value;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public String getSignature()
/* 37:   */   {
/* 38:34 */     return this.signature;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public boolean hasSignature()
/* 42:   */   {
/* 43:38 */     return this.signature != null;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public boolean isSignatureValid(PublicKey key)
/* 47:   */     throws SignatureValidateException
/* 48:   */   {
/* 49:   */     try
/* 50:   */     {
/* 51:43 */       Signature sig = Signature.getInstance("SHA1withRSA");
/* 52:44 */       sig.initVerify(key);
/* 53:45 */       sig.update(this.value.getBytes());
/* 54:46 */       return sig.verify(Base64.decode(this.signature.getBytes("UTF-8")));
/* 55:   */     }
/* 56:   */     catch (Exception e)
/* 57:   */     {
/* 58:48 */       throw new SignatureValidateException("Could not validate property signature.", e);
/* 59:   */     }
/* 60:   */   }
/* 61:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.properties.Property
 * JD-Core Version:    0.7.0.1
 */