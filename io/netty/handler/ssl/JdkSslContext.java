/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.util.internal.logging.InternalLogger;
/*   5:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   6:    */ import java.util.ArrayList;
/*   7:    */ import java.util.Arrays;
/*   8:    */ import java.util.Collections;
/*   9:    */ import java.util.List;
/*  10:    */ import javax.net.ssl.SSLContext;
/*  11:    */ import javax.net.ssl.SSLEngine;
/*  12:    */ import javax.net.ssl.SSLSessionContext;
/*  13:    */ 
/*  14:    */ public abstract class JdkSslContext
/*  15:    */   extends SslContext
/*  16:    */ {
/*  17: 36 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(JdkSslContext.class);
/*  18:    */   static final String PROTOCOL = "TLS";
/*  19:    */   static final String[] PROTOCOLS;
/*  20:    */   static final List<String> DEFAULT_CIPHERS;
/*  21:    */   private final String[] cipherSuites;
/*  22:    */   private final List<String> unmodifiableCipherSuites;
/*  23:    */   
/*  24:    */   static
/*  25:    */   {
/*  26:    */     SSLContext context;
/*  27:    */     try
/*  28:    */     {
/*  29: 45 */       context = SSLContext.getInstance("TLS");
/*  30: 46 */       context.init(null, null, null);
/*  31:    */     }
/*  32:    */     catch (Exception e)
/*  33:    */     {
/*  34: 48 */       throw new Error("failed to initialize the default SSL context", e);
/*  35:    */     }
/*  36: 51 */     SSLEngine engine = context.createSSLEngine();
/*  37:    */     
/*  38:    */ 
/*  39: 54 */     String[] supportedProtocols = engine.getSupportedProtocols();
/*  40: 55 */     List<String> protocols = new ArrayList();
/*  41: 56 */     addIfSupported(supportedProtocols, protocols, new String[] { "TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3" });
/*  42: 60 */     if (!protocols.isEmpty()) {
/*  43: 61 */       PROTOCOLS = (String[])protocols.toArray(new String[protocols.size()]);
/*  44:    */     } else {
/*  45: 63 */       PROTOCOLS = engine.getEnabledProtocols();
/*  46:    */     }
/*  47: 67 */     String[] supportedCiphers = engine.getSupportedCipherSuites();
/*  48: 68 */     List<String> ciphers = new ArrayList();
/*  49: 69 */     addIfSupported(supportedCiphers, ciphers, new String[] { "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_RC4_128_SHA", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_128_GCM_SHA256", "SSL_RSA_WITH_RC4_128_SHA", "SSL_RSA_WITH_RC4_128_MD5", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA" });
/*  50: 87 */     if (!ciphers.isEmpty()) {
/*  51: 88 */       DEFAULT_CIPHERS = Collections.unmodifiableList(ciphers);
/*  52:    */     } else {
/*  53: 91 */       DEFAULT_CIPHERS = Collections.unmodifiableList(Arrays.asList(engine.getEnabledCipherSuites()));
/*  54:    */     }
/*  55: 94 */     if (logger.isDebugEnabled())
/*  56:    */     {
/*  57: 95 */       logger.debug("Default protocols (JDK): {} ", Arrays.asList(PROTOCOLS));
/*  58: 96 */       logger.debug("Default cipher suites (JDK): {}", DEFAULT_CIPHERS);
/*  59:    */     }
/*  60:    */   }
/*  61:    */   
/*  62:    */   private static void addIfSupported(String[] supported, List<String> enabled, String... names)
/*  63:    */   {
/*  64:101 */     for (String n : names) {
/*  65:102 */       for (String s : supported) {
/*  66:103 */         if (n.equals(s))
/*  67:    */         {
/*  68:104 */           enabled.add(s);
/*  69:105 */           break;
/*  70:    */         }
/*  71:    */       }
/*  72:    */     }
/*  73:    */   }
/*  74:    */   
/*  75:    */   JdkSslContext(Iterable<String> ciphers)
/*  76:    */   {
/*  77:115 */     this.cipherSuites = toCipherSuiteArray(ciphers);
/*  78:116 */     this.unmodifiableCipherSuites = Collections.unmodifiableList(Arrays.asList(this.cipherSuites));
/*  79:    */   }
/*  80:    */   
/*  81:    */   public final SSLSessionContext sessionContext()
/*  82:    */   {
/*  83:128 */     if (isServer()) {
/*  84:129 */       return context().getServerSessionContext();
/*  85:    */     }
/*  86:131 */     return context().getClientSessionContext();
/*  87:    */   }
/*  88:    */   
/*  89:    */   public final List<String> cipherSuites()
/*  90:    */   {
/*  91:137 */     return this.unmodifiableCipherSuites;
/*  92:    */   }
/*  93:    */   
/*  94:    */   public final long sessionCacheSize()
/*  95:    */   {
/*  96:142 */     return sessionContext().getSessionCacheSize();
/*  97:    */   }
/*  98:    */   
/*  99:    */   public final long sessionTimeout()
/* 100:    */   {
/* 101:147 */     return sessionContext().getSessionTimeout();
/* 102:    */   }
/* 103:    */   
/* 104:    */   public final SSLEngine newEngine(ByteBufAllocator alloc)
/* 105:    */   {
/* 106:152 */     SSLEngine engine = context().createSSLEngine();
/* 107:153 */     engine.setEnabledCipherSuites(this.cipherSuites);
/* 108:154 */     engine.setEnabledProtocols(PROTOCOLS);
/* 109:155 */     engine.setUseClientMode(isClient());
/* 110:156 */     return wrapEngine(engine);
/* 111:    */   }
/* 112:    */   
/* 113:    */   public final SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort)
/* 114:    */   {
/* 115:161 */     SSLEngine engine = context().createSSLEngine(peerHost, peerPort);
/* 116:162 */     engine.setEnabledCipherSuites(this.cipherSuites);
/* 117:163 */     engine.setEnabledProtocols(PROTOCOLS);
/* 118:164 */     engine.setUseClientMode(isClient());
/* 119:165 */     return wrapEngine(engine);
/* 120:    */   }
/* 121:    */   
/* 122:    */   private SSLEngine wrapEngine(SSLEngine engine)
/* 123:    */   {
/* 124:169 */     if (nextProtocols().isEmpty()) {
/* 125:170 */       return engine;
/* 126:    */     }
/* 127:172 */     return new JettyNpnSslEngine(engine, nextProtocols(), isServer());
/* 128:    */   }
/* 129:    */   
/* 130:    */   private static String[] toCipherSuiteArray(Iterable<String> ciphers)
/* 131:    */   {
/* 132:177 */     if (ciphers == null) {
/* 133:178 */       return (String[])DEFAULT_CIPHERS.toArray(new String[DEFAULT_CIPHERS.size()]);
/* 134:    */     }
/* 135:180 */     List<String> newCiphers = new ArrayList();
/* 136:181 */     for (String c : ciphers)
/* 137:    */     {
/* 138:182 */       if (c == null) {
/* 139:    */         break;
/* 140:    */       }
/* 141:185 */       newCiphers.add(c);
/* 142:    */     }
/* 143:187 */     return (String[])newCiphers.toArray(new String[newCiphers.size()]);
/* 144:    */   }
/* 145:    */   
/* 146:    */   public abstract SSLContext context();
/* 147:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.JdkSslContext
 * JD-Core Version:    0.7.0.1
 */