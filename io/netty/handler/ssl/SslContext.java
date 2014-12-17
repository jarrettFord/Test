/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import java.io.File;
/*   5:    */ import java.util.List;
/*   6:    */ import javax.net.ssl.SSLEngine;
/*   7:    */ import javax.net.ssl.SSLException;
/*   8:    */ import javax.net.ssl.TrustManagerFactory;
/*   9:    */ 
/*  10:    */ public abstract class SslContext
/*  11:    */ {
/*  12:    */   public static SslProvider defaultServerProvider()
/*  13:    */   {
/*  14: 61 */     if (OpenSsl.isAvailable()) {
/*  15: 62 */       return SslProvider.OPENSSL;
/*  16:    */     }
/*  17: 64 */     return SslProvider.JDK;
/*  18:    */   }
/*  19:    */   
/*  20:    */   public static SslProvider defaultClientProvider()
/*  21:    */   {
/*  22: 74 */     return SslProvider.JDK;
/*  23:    */   }
/*  24:    */   
/*  25:    */   public static SslContext newServerContext(File certChainFile, File keyFile)
/*  26:    */     throws SSLException
/*  27:    */   {
/*  28: 85 */     return newServerContext(null, certChainFile, keyFile, null, null, null, 0L, 0L);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public static SslContext newServerContext(File certChainFile, File keyFile, String keyPassword)
/*  32:    */     throws SSLException
/*  33:    */   {
/*  34: 99 */     return newServerContext(null, certChainFile, keyFile, keyPassword, null, null, 0L, 0L);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public static SslContext newServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*  38:    */     throws SSLException
/*  39:    */   {
/*  40:123 */     return newServerContext(null, certChainFile, keyFile, keyPassword, ciphers, nextProtocols, sessionCacheSize, sessionTimeout);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public static SslContext newServerContext(SslProvider provider, File certChainFile, File keyFile)
/*  44:    */     throws SSLException
/*  45:    */   {
/*  46:139 */     return newServerContext(provider, certChainFile, keyFile, null, null, null, 0L, 0L);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public static SslContext newServerContext(SslProvider provider, File certChainFile, File keyFile, String keyPassword)
/*  50:    */     throws SSLException
/*  51:    */   {
/*  52:155 */     return newServerContext(provider, certChainFile, keyFile, keyPassword, null, null, 0L, 0L);
/*  53:    */   }
/*  54:    */   
/*  55:    */   public static SslContext newServerContext(SslProvider provider, File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*  56:    */     throws SSLException
/*  57:    */   {
/*  58:183 */     if (provider == null) {
/*  59:184 */       provider = OpenSsl.isAvailable() ? SslProvider.OPENSSL : SslProvider.JDK;
/*  60:    */     }
/*  61:187 */     switch (1.$SwitchMap$io$netty$handler$ssl$SslProvider[provider.ordinal()])
/*  62:    */     {
/*  63:    */     case 1: 
/*  64:189 */       return new JdkSslServerContext(certChainFile, keyFile, keyPassword, ciphers, nextProtocols, sessionCacheSize, sessionTimeout);
/*  65:    */     case 2: 
/*  66:193 */       return new OpenSslServerContext(certChainFile, keyFile, keyPassword, ciphers, nextProtocols, sessionCacheSize, sessionTimeout);
/*  67:    */     }
/*  68:197 */     throw new Error(provider.toString());
/*  69:    */   }
/*  70:    */   
/*  71:    */   public static SslContext newClientContext()
/*  72:    */     throws SSLException
/*  73:    */   {
/*  74:207 */     return newClientContext(null, null, null, null, null, 0L, 0L);
/*  75:    */   }
/*  76:    */   
/*  77:    */   public static SslContext newClientContext(File certChainFile)
/*  78:    */     throws SSLException
/*  79:    */   {
/*  80:218 */     return newClientContext(null, certChainFile, null, null, null, 0L, 0L);
/*  81:    */   }
/*  82:    */   
/*  83:    */   public static SslContext newClientContext(TrustManagerFactory trustManagerFactory)
/*  84:    */     throws SSLException
/*  85:    */   {
/*  86:231 */     return newClientContext(null, null, trustManagerFactory, null, null, 0L, 0L);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public static SslContext newClientContext(File certChainFile, TrustManagerFactory trustManagerFactory)
/*  90:    */     throws SSLException
/*  91:    */   {
/*  92:247 */     return newClientContext(null, certChainFile, trustManagerFactory, null, null, 0L, 0L);
/*  93:    */   }
/*  94:    */   
/*  95:    */   public static SslContext newClientContext(File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*  96:    */     throws SSLException
/*  97:    */   {
/*  98:273 */     return newClientContext(null, certChainFile, trustManagerFactory, ciphers, nextProtocols, sessionCacheSize, sessionTimeout);
/*  99:    */   }
/* 100:    */   
/* 101:    */   public static SslContext newClientContext(SslProvider provider)
/* 102:    */     throws SSLException
/* 103:    */   {
/* 104:287 */     return newClientContext(provider, null, null, null, null, 0L, 0L);
/* 105:    */   }
/* 106:    */   
/* 107:    */   public static SslContext newClientContext(SslProvider provider, File certChainFile)
/* 108:    */     throws SSLException
/* 109:    */   {
/* 110:301 */     return newClientContext(provider, certChainFile, null, null, null, 0L, 0L);
/* 111:    */   }
/* 112:    */   
/* 113:    */   public static SslContext newClientContext(SslProvider provider, TrustManagerFactory trustManagerFactory)
/* 114:    */     throws SSLException
/* 115:    */   {
/* 116:317 */     return newClientContext(provider, null, trustManagerFactory, null, null, 0L, 0L);
/* 117:    */   }
/* 118:    */   
/* 119:    */   public static SslContext newClientContext(SslProvider provider, File certChainFile, TrustManagerFactory trustManagerFactory)
/* 120:    */     throws SSLException
/* 121:    */   {
/* 122:335 */     return newClientContext(provider, certChainFile, trustManagerFactory, null, null, 0L, 0L);
/* 123:    */   }
/* 124:    */   
/* 125:    */   public static SslContext newClientContext(SslProvider provider, File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/* 126:    */     throws SSLException
/* 127:    */   {
/* 128:365 */     if ((provider != null) && (provider != SslProvider.JDK)) {
/* 129:366 */       throw new SSLException("client context unsupported for: " + provider);
/* 130:    */     }
/* 131:369 */     return new JdkSslClientContext(certChainFile, trustManagerFactory, ciphers, nextProtocols, sessionCacheSize, sessionTimeout);
/* 132:    */   }
/* 133:    */   
/* 134:    */   public final boolean isServer()
/* 135:    */   {
/* 136:380 */     return !isClient();
/* 137:    */   }
/* 138:    */   
/* 139:    */   public abstract boolean isClient();
/* 140:    */   
/* 141:    */   public abstract List<String> cipherSuites();
/* 142:    */   
/* 143:    */   public abstract long sessionCacheSize();
/* 144:    */   
/* 145:    */   public abstract long sessionTimeout();
/* 146:    */   
/* 147:    */   public abstract List<String> nextProtocols();
/* 148:    */   
/* 149:    */   public abstract SSLEngine newEngine(ByteBufAllocator paramByteBufAllocator);
/* 150:    */   
/* 151:    */   public abstract SSLEngine newEngine(ByteBufAllocator paramByteBufAllocator, String paramString, int paramInt);
/* 152:    */   
/* 153:    */   public final SslHandler newHandler(ByteBufAllocator alloc)
/* 154:    */   {
/* 155:434 */     return newHandler(newEngine(alloc));
/* 156:    */   }
/* 157:    */   
/* 158:    */   public final SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort)
/* 159:    */   {
/* 160:446 */     return newHandler(newEngine(alloc, peerHost, peerPort));
/* 161:    */   }
/* 162:    */   
/* 163:    */   private static SslHandler newHandler(SSLEngine engine)
/* 164:    */   {
/* 165:450 */     return new SslHandler(engine);
/* 166:    */   }
/* 167:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.SslContext
 * JD-Core Version:    0.7.0.1
 */