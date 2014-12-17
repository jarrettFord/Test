/*   1:    */ package io.netty.handler.ssl.util;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufUtil;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.util.concurrent.FastThreadLocal;
/*   6:    */ import io.netty.util.internal.EmptyArrays;
/*   7:    */ import java.security.KeyStore;
/*   8:    */ import java.security.MessageDigest;
/*   9:    */ import java.security.NoSuchAlgorithmException;
/*  10:    */ import java.security.cert.CertificateEncodingException;
/*  11:    */ import java.security.cert.CertificateException;
/*  12:    */ import java.security.cert.X509Certificate;
/*  13:    */ import java.util.ArrayList;
/*  14:    */ import java.util.Arrays;
/*  15:    */ import java.util.List;
/*  16:    */ import java.util.regex.Matcher;
/*  17:    */ import java.util.regex.Pattern;
/*  18:    */ import javax.net.ssl.ManagerFactoryParameters;
/*  19:    */ import javax.net.ssl.TrustManager;
/*  20:    */ import javax.net.ssl.X509TrustManager;
/*  21:    */ 
/*  22:    */ public final class FingerprintTrustManagerFactory
/*  23:    */   extends SimpleTrustManagerFactory
/*  24:    */ {
/*  25: 66 */   private static final Pattern FINGERPRINT_PATTERN = Pattern.compile("^[0-9a-fA-F:]+$");
/*  26: 67 */   private static final Pattern FINGERPRINT_STRIP_PATTERN = Pattern.compile(":");
/*  27:    */   private static final int SHA1_BYTE_LEN = 20;
/*  28:    */   private static final int SHA1_HEX_LEN = 40;
/*  29: 71 */   private static final FastThreadLocal<MessageDigest> tlmd = new FastThreadLocal()
/*  30:    */   {
/*  31:    */     protected MessageDigest initialValue()
/*  32:    */     {
/*  33:    */       try
/*  34:    */       {
/*  35: 75 */         return MessageDigest.getInstance("SHA1");
/*  36:    */       }
/*  37:    */       catch (NoSuchAlgorithmException e)
/*  38:    */       {
/*  39: 78 */         throw new Error(e);
/*  40:    */       }
/*  41:    */     }
/*  42:    */   };
/*  43: 83 */   private final TrustManager tm = new X509TrustManager()
/*  44:    */   {
/*  45:    */     public void checkClientTrusted(X509Certificate[] chain, String s)
/*  46:    */       throws CertificateException
/*  47:    */     {
/*  48: 87 */       checkTrusted("client", chain);
/*  49:    */     }
/*  50:    */     
/*  51:    */     public void checkServerTrusted(X509Certificate[] chain, String s)
/*  52:    */       throws CertificateException
/*  53:    */     {
/*  54: 92 */       checkTrusted("server", chain);
/*  55:    */     }
/*  56:    */     
/*  57:    */     private void checkTrusted(String type, X509Certificate[] chain)
/*  58:    */       throws CertificateException
/*  59:    */     {
/*  60: 96 */       X509Certificate cert = chain[0];
/*  61: 97 */       byte[] fingerprint = fingerprint(cert);
/*  62: 98 */       boolean found = false;
/*  63: 99 */       for (byte[] allowedFingerprint : FingerprintTrustManagerFactory.this.fingerprints) {
/*  64:100 */         if (Arrays.equals(fingerprint, allowedFingerprint))
/*  65:    */         {
/*  66:101 */           found = true;
/*  67:102 */           break;
/*  68:    */         }
/*  69:    */       }
/*  70:106 */       if (!found) {
/*  71:107 */         throw new CertificateException(type + " certificate with unknown fingerprint: " + cert.getSubjectDN());
/*  72:    */       }
/*  73:    */     }
/*  74:    */     
/*  75:    */     private byte[] fingerprint(X509Certificate cert)
/*  76:    */       throws CertificateEncodingException
/*  77:    */     {
/*  78:113 */       MessageDigest md = (MessageDigest)FingerprintTrustManagerFactory.tlmd.get();
/*  79:114 */       md.reset();
/*  80:115 */       return md.digest(cert.getEncoded());
/*  81:    */     }
/*  82:    */     
/*  83:    */     public X509Certificate[] getAcceptedIssuers()
/*  84:    */     {
/*  85:120 */       return EmptyArrays.EMPTY_X509_CERTIFICATES;
/*  86:    */     }
/*  87:    */   };
/*  88:    */   private final byte[][] fingerprints;
/*  89:    */   
/*  90:    */   public FingerprintTrustManagerFactory(Iterable<String> fingerprints)
/*  91:    */   {
/*  92:132 */     this(toFingerprintArray(fingerprints));
/*  93:    */   }
/*  94:    */   
/*  95:    */   public FingerprintTrustManagerFactory(String... fingerprints)
/*  96:    */   {
/*  97:141 */     this(toFingerprintArray(Arrays.asList(fingerprints)));
/*  98:    */   }
/*  99:    */   
/* 100:    */   public FingerprintTrustManagerFactory(byte[]... fingerprints)
/* 101:    */   {
/* 102:150 */     if (fingerprints == null) {
/* 103:151 */       throw new NullPointerException("fingerprints");
/* 104:    */     }
/* 105:154 */     List<byte[]> list = new ArrayList();
/* 106:155 */     for (byte[] f : fingerprints)
/* 107:    */     {
/* 108:156 */       if (f == null) {
/* 109:    */         break;
/* 110:    */       }
/* 111:159 */       if (f.length != 20) {
/* 112:160 */         throw new IllegalArgumentException("malformed fingerprint: " + ByteBufUtil.hexDump(Unpooled.wrappedBuffer(f)) + " (expected: SHA1)");
/* 113:    */       }
/* 114:163 */       list.add(f.clone());
/* 115:    */     }
/* 116:166 */     this.fingerprints = ((byte[][])list.toArray(new byte[list.size()][]));
/* 117:    */   }
/* 118:    */   
/* 119:    */   private static byte[][] toFingerprintArray(Iterable<String> fingerprints)
/* 120:    */   {
/* 121:170 */     if (fingerprints == null) {
/* 122:171 */       throw new NullPointerException("fingerprints");
/* 123:    */     }
/* 124:174 */     List<byte[]> list = new ArrayList();
/* 125:175 */     for (String f : fingerprints)
/* 126:    */     {
/* 127:176 */       if (f == null) {
/* 128:    */         break;
/* 129:    */       }
/* 130:180 */       if (!FINGERPRINT_PATTERN.matcher(f).matches()) {
/* 131:181 */         throw new IllegalArgumentException("malformed fingerprint: " + f);
/* 132:    */       }
/* 133:183 */       f = FINGERPRINT_STRIP_PATTERN.matcher(f).replaceAll("");
/* 134:184 */       if (f.length() != 40) {
/* 135:185 */         throw new IllegalArgumentException("malformed fingerprint: " + f + " (expected: SHA1)");
/* 136:    */       }
/* 137:188 */       byte[] farr = new byte[20];
/* 138:189 */       for (int i = 0; i < farr.length; i++)
/* 139:    */       {
/* 140:190 */         int strIdx = i << 1;
/* 141:191 */         farr[i] = ((byte)Integer.parseInt(f.substring(strIdx, strIdx + 2), 16));
/* 142:    */       }
/* 143:    */     }
/* 144:195 */     return (byte[][])list.toArray(new byte[list.size()][]);
/* 145:    */   }
/* 146:    */   
/* 147:    */   protected void engineInit(KeyStore keyStore)
/* 148:    */     throws Exception
/* 149:    */   {}
/* 150:    */   
/* 151:    */   protected void engineInit(ManagerFactoryParameters managerFactoryParameters)
/* 152:    */     throws Exception
/* 153:    */   {}
/* 154:    */   
/* 155:    */   protected TrustManager[] engineGetTrustManagers()
/* 156:    */   {
/* 157:206 */     return new TrustManager[] { this.tm };
/* 158:    */   }
/* 159:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.util.FingerprintTrustManagerFactory
 * JD-Core Version:    0.7.0.1
 */