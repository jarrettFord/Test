/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufInputStream;
/*   5:    */ import java.io.File;
/*   6:    */ import java.security.KeyStore;
/*   7:    */ import java.security.cert.CertificateFactory;
/*   8:    */ import java.security.cert.X509Certificate;
/*   9:    */ import java.util.ArrayList;
/*  10:    */ import java.util.Collections;
/*  11:    */ import java.util.Iterator;
/*  12:    */ import java.util.List;
/*  13:    */ import javax.net.ssl.SSLContext;
/*  14:    */ import javax.net.ssl.SSLException;
/*  15:    */ import javax.net.ssl.SSLSessionContext;
/*  16:    */ import javax.net.ssl.TrustManagerFactory;
/*  17:    */ import javax.security.auth.x500.X500Principal;
/*  18:    */ 
/*  19:    */ public final class JdkSslClientContext
/*  20:    */   extends JdkSslContext
/*  21:    */ {
/*  22:    */   private final SSLContext ctx;
/*  23:    */   private final List<String> nextProtocols;
/*  24:    */   
/*  25:    */   public JdkSslClientContext()
/*  26:    */     throws SSLException
/*  27:    */   {
/*  28: 48 */     this(null, null, null, null, 0L, 0L);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public JdkSslClientContext(File certChainFile)
/*  32:    */     throws SSLException
/*  33:    */   {
/*  34: 58 */     this(certChainFile, null);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public JdkSslClientContext(TrustManagerFactory trustManagerFactory)
/*  38:    */     throws SSLException
/*  39:    */   {
/*  40: 69 */     this(null, trustManagerFactory);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public JdkSslClientContext(File certChainFile, TrustManagerFactory trustManagerFactory)
/*  44:    */     throws SSLException
/*  45:    */   {
/*  46: 82 */     this(certChainFile, trustManagerFactory, null, null, 0L, 0L);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public JdkSslClientContext(File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*  50:    */     throws SSLException
/*  51:    */   {
/*  52:107 */     super(ciphers);
/*  53:109 */     if ((nextProtocols != null) && (nextProtocols.iterator().hasNext()))
/*  54:    */     {
/*  55:110 */       if (!JettyNpnSslEngine.isAvailable()) {
/*  56:111 */         throw new SSLException("NPN/ALPN unsupported: " + nextProtocols);
/*  57:    */       }
/*  58:114 */       List<String> nextProtoList = new ArrayList();
/*  59:115 */       for (String p : nextProtocols)
/*  60:    */       {
/*  61:116 */         if (p == null) {
/*  62:    */           break;
/*  63:    */         }
/*  64:119 */         nextProtoList.add(p);
/*  65:    */       }
/*  66:121 */       this.nextProtocols = Collections.unmodifiableList(nextProtoList);
/*  67:    */     }
/*  68:    */     else
/*  69:    */     {
/*  70:123 */       this.nextProtocols = Collections.emptyList();
/*  71:    */     }
/*  72:    */     try
/*  73:    */     {
/*  74:127 */       if (certChainFile == null)
/*  75:    */       {
/*  76:128 */         this.ctx = SSLContext.getInstance("TLS");
/*  77:129 */         if (trustManagerFactory == null)
/*  78:    */         {
/*  79:130 */           this.ctx.init(null, null, null);
/*  80:    */         }
/*  81:    */         else
/*  82:    */         {
/*  83:132 */           trustManagerFactory.init((KeyStore)null);
/*  84:133 */           this.ctx.init(null, trustManagerFactory.getTrustManagers(), null);
/*  85:    */         }
/*  86:    */       }
/*  87:    */       else
/*  88:    */       {
/*  89:136 */         KeyStore ks = KeyStore.getInstance("JKS");
/*  90:137 */         ks.load(null, null);
/*  91:138 */         CertificateFactory cf = CertificateFactory.getInstance("X.509");
/*  92:    */         
/*  93:140 */         ByteBuf[] certs = PemReader.readCertificates(certChainFile);
/*  94:    */         try
/*  95:    */         {
/*  96:142 */           for (ByteBuf buf : certs)
/*  97:    */           {
/*  98:143 */             X509Certificate cert = (X509Certificate)cf.generateCertificate(new ByteBufInputStream(buf));
/*  99:144 */             X500Principal principal = cert.getSubjectX500Principal();
/* 100:145 */             ks.setCertificateEntry(principal.getName("RFC2253"), cert);
/* 101:    */           }
/* 102:    */         }
/* 103:    */         finally
/* 104:    */         {
/* 105:    */           ByteBuf[] arr$;
/* 106:    */           int len$;
/* 107:    */           int i$;
/* 108:    */           ByteBuf buf;
/* 109:148 */           for (ByteBuf buf : certs) {
/* 110:149 */             buf.release();
/* 111:    */           }
/* 112:    */         }
/* 113:154 */         if (trustManagerFactory == null) {
/* 114:155 */           trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
/* 115:    */         }
/* 116:157 */         trustManagerFactory.init(ks);
/* 117:    */         
/* 118:    */ 
/* 119:160 */         this.ctx = SSLContext.getInstance("TLS");
/* 120:161 */         this.ctx.init(null, trustManagerFactory.getTrustManagers(), null);
/* 121:    */       }
/* 122:164 */       SSLSessionContext sessCtx = this.ctx.getClientSessionContext();
/* 123:165 */       if (sessionCacheSize > 0L) {
/* 124:166 */         sessCtx.setSessionCacheSize((int)Math.min(sessionCacheSize, 2147483647L));
/* 125:    */       }
/* 126:168 */       if (sessionTimeout > 0L) {
/* 127:169 */         sessCtx.setSessionTimeout((int)Math.min(sessionTimeout, 2147483647L));
/* 128:    */       }
/* 129:    */     }
/* 130:    */     catch (Exception e)
/* 131:    */     {
/* 132:172 */       throw new SSLException("failed to initialize the server-side SSL context", e);
/* 133:    */     }
/* 134:    */   }
/* 135:    */   
/* 136:    */   public boolean isClient()
/* 137:    */   {
/* 138:178 */     return true;
/* 139:    */   }
/* 140:    */   
/* 141:    */   public List<String> nextProtocols()
/* 142:    */   {
/* 143:183 */     return this.nextProtocols;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public SSLContext context()
/* 147:    */   {
/* 148:188 */     return this.ctx;
/* 149:    */   }
/* 150:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.JdkSslClientContext
 * JD-Core Version:    0.7.0.1
 */