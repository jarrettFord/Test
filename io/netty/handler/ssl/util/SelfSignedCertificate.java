/*   1:    */ package io.netty.handler.ssl.util;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.handler.codec.base64.Base64;
/*   6:    */ import io.netty.util.CharsetUtil;
/*   7:    */ import io.netty.util.internal.logging.InternalLogger;
/*   8:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   9:    */ import java.io.File;
/*  10:    */ import java.io.FileOutputStream;
/*  11:    */ import java.io.IOException;
/*  12:    */ import java.io.OutputStream;
/*  13:    */ import java.security.KeyPair;
/*  14:    */ import java.security.KeyPairGenerator;
/*  15:    */ import java.security.NoSuchAlgorithmException;
/*  16:    */ import java.security.PrivateKey;
/*  17:    */ import java.security.SecureRandom;
/*  18:    */ import java.security.cert.CertificateEncodingException;
/*  19:    */ import java.security.cert.CertificateException;
/*  20:    */ import java.security.cert.X509Certificate;
/*  21:    */ import java.util.Date;
/*  22:    */ 
/*  23:    */ public final class SelfSignedCertificate
/*  24:    */ {
/*  25: 57 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(SelfSignedCertificate.class);
/*  26: 60 */   static final Date NOT_BEFORE = new Date(System.currentTimeMillis() - 31536000000L);
/*  27: 62 */   static final Date NOT_AFTER = new Date(253402300799000L);
/*  28:    */   private final File certificate;
/*  29:    */   private final File privateKey;
/*  30:    */   
/*  31:    */   public SelfSignedCertificate()
/*  32:    */     throws CertificateException
/*  33:    */   {
/*  34: 71 */     this("example.com");
/*  35:    */   }
/*  36:    */   
/*  37:    */   public SelfSignedCertificate(String fqdn)
/*  38:    */     throws CertificateException
/*  39:    */   {
/*  40: 82 */     this(fqdn, ThreadLocalInsecureRandom.current(), 1024);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public SelfSignedCertificate(String fqdn, SecureRandom random, int bits)
/*  44:    */     throws CertificateException
/*  45:    */   {
/*  46:    */     KeyPair keypair;
/*  47:    */     try
/*  48:    */     {
/*  49: 96 */       KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
/*  50: 97 */       keyGen.initialize(bits, random);
/*  51: 98 */       keypair = keyGen.generateKeyPair();
/*  52:    */     }
/*  53:    */     catch (NoSuchAlgorithmException e)
/*  54:    */     {
/*  55:101 */       throw new Error(e);
/*  56:    */     }
/*  57:    */     String[] paths;
/*  58:    */     try
/*  59:    */     {
/*  60:107 */       paths = OpenJdkSelfSignedCertGenerator.generate(fqdn, keypair, random);
/*  61:    */     }
/*  62:    */     catch (Throwable t)
/*  63:    */     {
/*  64:109 */       logger.debug("Failed to generate a self-signed X.509 certificate using sun.security.x509:", t);
/*  65:    */       try
/*  66:    */       {
/*  67:112 */         paths = BouncyCastleSelfSignedCertGenerator.generate(fqdn, keypair, random);
/*  68:    */       }
/*  69:    */       catch (Throwable t2)
/*  70:    */       {
/*  71:114 */         logger.debug("Failed to generate a self-signed X.509 certificate using Bouncy Castle:", t2);
/*  72:115 */         throw new CertificateException("No provider succeeded to generate a self-signed certificate. See debug log for the root cause.");
/*  73:    */       }
/*  74:    */     }
/*  75:121 */     this.certificate = new File(paths[0]);
/*  76:122 */     this.privateKey = new File(paths[1]);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public File certificate()
/*  80:    */   {
/*  81:129 */     return this.certificate;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public File privateKey()
/*  85:    */   {
/*  86:136 */     return this.privateKey;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public void delete()
/*  90:    */   {
/*  91:143 */     safeDelete(this.certificate);
/*  92:144 */     safeDelete(this.privateKey);
/*  93:    */   }
/*  94:    */   
/*  95:    */   static String[] newSelfSignedCertificate(String fqdn, PrivateKey key, X509Certificate cert)
/*  96:    */     throws IOException, CertificateEncodingException
/*  97:    */   {
/*  98:151 */     String keyText = "-----BEGIN PRIVATE KEY-----\n" + Base64.encode(Unpooled.wrappedBuffer(key.getEncoded()), true).toString(CharsetUtil.US_ASCII) + "\n-----END PRIVATE KEY-----\n";
/*  99:    */     
/* 100:    */ 
/* 101:    */ 
/* 102:155 */     File keyFile = File.createTempFile("keyutil_" + fqdn + '_', ".key");
/* 103:156 */     keyFile.deleteOnExit();
/* 104:    */     
/* 105:158 */     OutputStream keyOut = new FileOutputStream(keyFile);
/* 106:    */     try
/* 107:    */     {
/* 108:160 */       keyOut.write(keyText.getBytes(CharsetUtil.US_ASCII));
/* 109:161 */       keyOut.close();
/* 110:162 */       keyOut = null;
/* 111:    */     }
/* 112:    */     finally
/* 113:    */     {
/* 114:164 */       if (keyOut != null)
/* 115:    */       {
/* 116:165 */         safeClose(keyFile, keyOut);
/* 117:166 */         safeDelete(keyFile);
/* 118:    */       }
/* 119:    */     }
/* 120:171 */     String certText = "-----BEGIN CERTIFICATE-----\n" + Base64.encode(Unpooled.wrappedBuffer(cert.getEncoded()), true).toString(CharsetUtil.US_ASCII) + "\n-----END CERTIFICATE-----\n";
/* 121:    */     
/* 122:    */ 
/* 123:    */ 
/* 124:175 */     File certFile = File.createTempFile("keyutil_" + fqdn + '_', ".crt");
/* 125:176 */     certFile.deleteOnExit();
/* 126:    */     
/* 127:178 */     OutputStream certOut = new FileOutputStream(certFile);
/* 128:    */     try
/* 129:    */     {
/* 130:180 */       certOut.write(certText.getBytes(CharsetUtil.US_ASCII));
/* 131:181 */       certOut.close();
/* 132:182 */       certOut = null;
/* 133:    */     }
/* 134:    */     finally
/* 135:    */     {
/* 136:184 */       if (certOut != null)
/* 137:    */       {
/* 138:185 */         safeClose(certFile, certOut);
/* 139:186 */         safeDelete(certFile);
/* 140:187 */         safeDelete(keyFile);
/* 141:    */       }
/* 142:    */     }
/* 143:191 */     return new String[] { certFile.getPath(), keyFile.getPath() };
/* 144:    */   }
/* 145:    */   
/* 146:    */   private static void safeDelete(File certFile)
/* 147:    */   {
/* 148:195 */     if (!certFile.delete()) {
/* 149:196 */       logger.warn("Failed to delete a file: " + certFile);
/* 150:    */     }
/* 151:    */   }
/* 152:    */   
/* 153:    */   private static void safeClose(File keyFile, OutputStream keyOut)
/* 154:    */   {
/* 155:    */     try
/* 156:    */     {
/* 157:202 */       keyOut.close();
/* 158:    */     }
/* 159:    */     catch (IOException e)
/* 160:    */     {
/* 161:204 */       logger.warn("Failed to close a file: " + keyFile, e);
/* 162:    */     }
/* 163:    */   }
/* 164:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.util.SelfSignedCertificate
 * JD-Core Version:    0.7.0.1
 */