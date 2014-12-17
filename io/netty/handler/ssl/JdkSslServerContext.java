/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufInputStream;
/*   5:    */ import java.io.File;
/*   6:    */ import java.security.KeyFactory;
/*   7:    */ import java.security.KeyStore;
/*   8:    */ import java.security.PrivateKey;
/*   9:    */ import java.security.Security;
/*  10:    */ import java.security.cert.Certificate;
/*  11:    */ import java.security.cert.CertificateFactory;
/*  12:    */ import java.security.spec.InvalidKeySpecException;
/*  13:    */ import java.security.spec.PKCS8EncodedKeySpec;
/*  14:    */ import java.util.ArrayList;
/*  15:    */ import java.util.Collections;
/*  16:    */ import java.util.Iterator;
/*  17:    */ import java.util.List;
/*  18:    */ import javax.net.ssl.KeyManagerFactory;
/*  19:    */ import javax.net.ssl.SSLContext;
/*  20:    */ import javax.net.ssl.SSLException;
/*  21:    */ import javax.net.ssl.SSLSessionContext;
/*  22:    */ 
/*  23:    */ public final class JdkSslServerContext
/*  24:    */   extends JdkSslContext
/*  25:    */ {
/*  26:    */   private final SSLContext ctx;
/*  27:    */   private final List<String> nextProtocols;
/*  28:    */   
/*  29:    */   public JdkSslServerContext(File certChainFile, File keyFile)
/*  30:    */     throws SSLException
/*  31:    */   {
/*  32: 54 */     this(certChainFile, keyFile, null);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public JdkSslServerContext(File certChainFile, File keyFile, String keyPassword)
/*  36:    */     throws SSLException
/*  37:    */   {
/*  38: 66 */     this(certChainFile, keyFile, keyPassword, null, null, 0L, 0L);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public JdkSslServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*  42:    */     throws SSLException
/*  43:    */   {
/*  44: 90 */     super(ciphers);
/*  45: 92 */     if (certChainFile == null) {
/*  46: 93 */       throw new NullPointerException("certChainFile");
/*  47:    */     }
/*  48: 95 */     if (keyFile == null) {
/*  49: 96 */       throw new NullPointerException("keyFile");
/*  50:    */     }
/*  51: 99 */     if (keyPassword == null) {
/*  52:100 */       keyPassword = "";
/*  53:    */     }
/*  54:103 */     if ((nextProtocols != null) && (nextProtocols.iterator().hasNext()))
/*  55:    */     {
/*  56:104 */       if (!JettyNpnSslEngine.isAvailable()) {
/*  57:105 */         throw new SSLException("NPN/ALPN unsupported: " + nextProtocols);
/*  58:    */       }
/*  59:108 */       List<String> list = new ArrayList();
/*  60:109 */       for (String p : nextProtocols)
/*  61:    */       {
/*  62:110 */         if (p == null) {
/*  63:    */           break;
/*  64:    */         }
/*  65:113 */         list.add(p);
/*  66:    */       }
/*  67:116 */       this.nextProtocols = Collections.unmodifiableList(list);
/*  68:    */     }
/*  69:    */     else
/*  70:    */     {
/*  71:118 */       this.nextProtocols = Collections.emptyList();
/*  72:    */     }
/*  73:121 */     String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
/*  74:122 */     if (algorithm == null) {
/*  75:123 */       algorithm = "SunX509";
/*  76:    */     }
/*  77:    */     try
/*  78:    */     {
/*  79:127 */       KeyStore ks = KeyStore.getInstance("JKS");
/*  80:128 */       ks.load(null, null);
/*  81:129 */       CertificateFactory cf = CertificateFactory.getInstance("X.509");
/*  82:130 */       KeyFactory rsaKF = KeyFactory.getInstance("RSA");
/*  83:131 */       KeyFactory dsaKF = KeyFactory.getInstance("DSA");
/*  84:    */       
/*  85:133 */       ByteBuf encodedKeyBuf = PemReader.readPrivateKey(keyFile);
/*  86:134 */       byte[] encodedKey = new byte[encodedKeyBuf.readableBytes()];
/*  87:135 */       encodedKeyBuf.readBytes(encodedKey).release();
/*  88:136 */       PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(encodedKey);
/*  89:    */       PrivateKey key;
/*  90:    */       try
/*  91:    */       {
/*  92:140 */         key = rsaKF.generatePrivate(encodedKeySpec);
/*  93:    */       }
/*  94:    */       catch (InvalidKeySpecException ignore)
/*  95:    */       {
/*  96:142 */         key = dsaKF.generatePrivate(encodedKeySpec);
/*  97:    */       }
/*  98:145 */       List<Certificate> certChain = new ArrayList();
/*  99:146 */       ByteBuf[] certs = PemReader.readCertificates(certChainFile);
/* 100:    */       try
/* 101:    */       {
/* 102:148 */         for (ByteBuf buf : certs) {
/* 103:149 */           certChain.add(cf.generateCertificate(new ByteBufInputStream(buf)));
/* 104:    */         }
/* 105:    */       }
/* 106:    */       finally
/* 107:    */       {
/* 108:    */         ByteBuf[] arr$;
/* 109:    */         int len$;
/* 110:    */         int i$;
/* 111:    */         ByteBuf buf;
/* 112:152 */         for (ByteBuf buf : certs) {
/* 113:153 */           buf.release();
/* 114:    */         }
/* 115:    */       }
/* 116:157 */       ks.setKeyEntry("key", key, keyPassword.toCharArray(), (Certificate[])certChain.toArray(new Certificate[certChain.size()]));
/* 117:    */       
/* 118:    */ 
/* 119:160 */       KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
/* 120:161 */       kmf.init(ks, keyPassword.toCharArray());
/* 121:    */       
/* 122:    */ 
/* 123:164 */       this.ctx = SSLContext.getInstance("TLS");
/* 124:165 */       this.ctx.init(kmf.getKeyManagers(), null, null);
/* 125:    */       
/* 126:167 */       SSLSessionContext sessCtx = this.ctx.getServerSessionContext();
/* 127:168 */       if (sessionCacheSize > 0L) {
/* 128:169 */         sessCtx.setSessionCacheSize((int)Math.min(sessionCacheSize, 2147483647L));
/* 129:    */       }
/* 130:171 */       if (sessionTimeout > 0L) {
/* 131:172 */         sessCtx.setSessionTimeout((int)Math.min(sessionTimeout, 2147483647L));
/* 132:    */       }
/* 133:    */     }
/* 134:    */     catch (Exception e)
/* 135:    */     {
/* 136:175 */       throw new SSLException("failed to initialize the server-side SSL context", e);
/* 137:    */     }
/* 138:    */   }
/* 139:    */   
/* 140:    */   public boolean isClient()
/* 141:    */   {
/* 142:181 */     return false;
/* 143:    */   }
/* 144:    */   
/* 145:    */   public List<String> nextProtocols()
/* 146:    */   {
/* 147:186 */     return this.nextProtocols;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public SSLContext context()
/* 151:    */   {
/* 152:191 */     return this.ctx;
/* 153:    */   }
/* 154:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.JdkSslServerContext
 * JD-Core Version:    0.7.0.1
 */