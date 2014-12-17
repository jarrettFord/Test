/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.util.internal.logging.InternalLogger;
/*   5:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   6:    */ import java.io.File;
/*   7:    */ import java.util.ArrayList;
/*   8:    */ import java.util.Collections;
/*   9:    */ import java.util.List;
/*  10:    */ import javax.net.ssl.SSLEngine;
/*  11:    */ import javax.net.ssl.SSLException;
/*  12:    */ import org.apache.tomcat.jni.Pool;
/*  13:    */ import org.apache.tomcat.jni.SSL;
/*  14:    */ import org.apache.tomcat.jni.SSLContext;
/*  15:    */ 
/*  16:    */ public final class OpenSslServerContext
/*  17:    */   extends SslContext
/*  18:    */ {
/*  19: 37 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(OpenSslServerContext.class);
/*  20:    */   private static final List<String> DEFAULT_CIPHERS;
/*  21:    */   private final long aprPool;
/*  22:    */   
/*  23:    */   static
/*  24:    */   {
/*  25: 41 */     List<String> ciphers = new ArrayList();
/*  26:    */     
/*  27: 43 */     Collections.addAll(ciphers, new String[] { "ECDHE-RSA-AES128-GCM-SHA256", "ECDHE-RSA-RC4-SHA", "ECDHE-RSA-AES128-SHA", "ECDHE-RSA-AES256-SHA", "AES128-GCM-SHA256", "RC4-SHA", "RC4-MD5", "AES128-SHA", "AES256-SHA", "DES-CBC3-SHA" });
/*  28:    */     
/*  29:    */ 
/*  30:    */ 
/*  31:    */ 
/*  32:    */ 
/*  33:    */ 
/*  34:    */ 
/*  35:    */ 
/*  36:    */ 
/*  37:    */ 
/*  38:    */ 
/*  39: 55 */     DEFAULT_CIPHERS = Collections.unmodifiableList(ciphers);
/*  40: 57 */     if (logger.isDebugEnabled()) {
/*  41: 58 */       logger.debug("Default cipher suite (OpenSSL): " + ciphers);
/*  42:    */     }
/*  43:    */   }
/*  44:    */   
/*  45: 64 */   private final List<String> ciphers = new ArrayList();
/*  46: 65 */   private final List<String> unmodifiableCiphers = Collections.unmodifiableList(this.ciphers);
/*  47:    */   private final long sessionCacheSize;
/*  48:    */   private final long sessionTimeout;
/*  49:    */   private final List<String> nextProtocols;
/*  50:    */   private final long ctx;
/*  51:    */   private final OpenSslSessionStats stats;
/*  52:    */   
/*  53:    */   public OpenSslServerContext(File certChainFile, File keyFile)
/*  54:    */     throws SSLException
/*  55:    */   {
/*  56: 81 */     this(certChainFile, keyFile, null);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword)
/*  60:    */     throws SSLException
/*  61:    */   {
/*  62: 93 */     this(certChainFile, keyFile, keyPassword, null, null, 0L, 0L);
/*  63:    */   }
/*  64:    */   
/*  65:    */   public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*  66:    */     throws SSLException
/*  67:    */   {
/*  68:117 */     OpenSsl.ensureAvailability();
/*  69:119 */     if (certChainFile == null) {
/*  70:120 */       throw new NullPointerException("certChainFile");
/*  71:    */     }
/*  72:122 */     if (!certChainFile.isFile()) {
/*  73:123 */       throw new IllegalArgumentException("certChainFile is not a file: " + certChainFile);
/*  74:    */     }
/*  75:125 */     if (keyFile == null) {
/*  76:126 */       throw new NullPointerException("keyPath");
/*  77:    */     }
/*  78:128 */     if (!keyFile.isFile()) {
/*  79:129 */       throw new IllegalArgumentException("keyPath is not a file: " + keyFile);
/*  80:    */     }
/*  81:131 */     if (ciphers == null) {
/*  82:132 */       ciphers = DEFAULT_CIPHERS;
/*  83:    */     }
/*  84:135 */     if (keyPassword == null) {
/*  85:136 */       keyPassword = "";
/*  86:    */     }
/*  87:138 */     if (nextProtocols == null) {
/*  88:139 */       nextProtocols = Collections.emptyList();
/*  89:    */     }
/*  90:142 */     for (String c : ciphers)
/*  91:    */     {
/*  92:143 */       if (c == null) {
/*  93:    */         break;
/*  94:    */       }
/*  95:146 */       this.ciphers.add(c);
/*  96:    */     }
/*  97:149 */     List<String> nextProtoList = new ArrayList();
/*  98:150 */     for (String p : nextProtocols)
/*  99:    */     {
/* 100:151 */       if (p == null) {
/* 101:    */         break;
/* 102:    */       }
/* 103:154 */       nextProtoList.add(p);
/* 104:    */     }
/* 105:156 */     this.nextProtocols = Collections.unmodifiableList(nextProtoList);
/* 106:    */     
/* 107:    */ 
/* 108:159 */     this.aprPool = Pool.create(0L);
/* 109:    */     
/* 110:    */ 
/* 111:162 */     boolean success = false;
/* 112:    */     try
/* 113:    */     {
/* 114:164 */       synchronized (OpenSslServerContext.class)
/* 115:    */       {
/* 116:    */         try
/* 117:    */         {
/* 118:166 */           this.ctx = SSLContext.make(this.aprPool, 6, 1);
/* 119:    */         }
/* 120:    */         catch (Exception e)
/* 121:    */         {
/* 122:168 */           throw new SSLException("failed to create an SSL_CTX", e);
/* 123:    */         }
/* 124:171 */         SSLContext.setOptions(this.ctx, 4095);
/* 125:172 */         SSLContext.setOptions(this.ctx, 16777216);
/* 126:173 */         SSLContext.setOptions(this.ctx, 4194304);
/* 127:174 */         SSLContext.setOptions(this.ctx, 524288);
/* 128:175 */         SSLContext.setOptions(this.ctx, 1048576);
/* 129:176 */         SSLContext.setOptions(this.ctx, 65536);
/* 130:    */         try
/* 131:    */         {
/* 132:181 */           StringBuilder cipherBuf = new StringBuilder();
/* 133:182 */           for (String c : this.ciphers)
/* 134:    */           {
/* 135:183 */             cipherBuf.append(c);
/* 136:184 */             cipherBuf.append(':');
/* 137:    */           }
/* 138:186 */           cipherBuf.setLength(cipherBuf.length() - 1);
/* 139:    */           
/* 140:188 */           SSLContext.setCipherSuite(this.ctx, cipherBuf.toString());
/* 141:    */         }
/* 142:    */         catch (SSLException e)
/* 143:    */         {
/* 144:190 */           throw e;
/* 145:    */         }
/* 146:    */         catch (Exception e)
/* 147:    */         {
/* 148:192 */           throw new SSLException("failed to set cipher suite: " + this.ciphers, e);
/* 149:    */         }
/* 150:196 */         SSLContext.setVerify(this.ctx, 0, 10);
/* 151:    */         try
/* 152:    */         {
/* 153:200 */           if (!SSLContext.setCertificate(this.ctx, certChainFile.getPath(), keyFile.getPath(), keyPassword, 0)) {
/* 154:202 */             throw new SSLException("failed to set certificate: " + certChainFile + " and " + keyFile + " (" + SSL.getLastError() + ')');
/* 155:    */           }
/* 156:    */         }
/* 157:    */         catch (SSLException e)
/* 158:    */         {
/* 159:206 */           throw e;
/* 160:    */         }
/* 161:    */         catch (Exception e)
/* 162:    */         {
/* 163:208 */           throw new SSLException("failed to set certificate: " + certChainFile + " and " + keyFile, e);
/* 164:    */         }
/* 165:212 */         if (!SSLContext.setCertificateChainFile(this.ctx, certChainFile.getPath(), true))
/* 166:    */         {
/* 167:213 */           String error = SSL.getLastError();
/* 168:214 */           if (!error.startsWith("error:00000000:")) {
/* 169:215 */             throw new SSLException("failed to set certificate chain: " + certChainFile + " (" + SSL.getLastError() + ')');
/* 170:    */           }
/* 171:    */         }
/* 172:221 */         if (!nextProtoList.isEmpty())
/* 173:    */         {
/* 174:223 */           StringBuilder nextProtocolBuf = new StringBuilder();
/* 175:224 */           for (String p : nextProtoList)
/* 176:    */           {
/* 177:225 */             nextProtocolBuf.append(p);
/* 178:226 */             nextProtocolBuf.append(',');
/* 179:    */           }
/* 180:228 */           nextProtocolBuf.setLength(nextProtocolBuf.length() - 1);
/* 181:    */           
/* 182:230 */           SSLContext.setNextProtos(this.ctx, nextProtocolBuf.toString());
/* 183:    */         }
/* 184:234 */         if (sessionCacheSize > 0L)
/* 185:    */         {
/* 186:235 */           this.sessionCacheSize = sessionCacheSize;
/* 187:236 */           SSLContext.setSessionCacheSize(this.ctx, sessionCacheSize);
/* 188:    */         }
/* 189:    */         else
/* 190:    */         {
/* 191:239 */           this.sessionCacheSize = (sessionCacheSize = SSLContext.setSessionCacheSize(this.ctx, 20480L));
/* 192:    */           
/* 193:241 */           SSLContext.setSessionCacheSize(this.ctx, sessionCacheSize);
/* 194:    */         }
/* 195:245 */         if (sessionTimeout > 0L)
/* 196:    */         {
/* 197:246 */           this.sessionTimeout = sessionTimeout;
/* 198:247 */           SSLContext.setSessionCacheTimeout(this.ctx, sessionTimeout);
/* 199:    */         }
/* 200:    */         else
/* 201:    */         {
/* 202:250 */           this.sessionTimeout = (sessionTimeout = SSLContext.setSessionCacheTimeout(this.ctx, 300L));
/* 203:    */           
/* 204:252 */           SSLContext.setSessionCacheTimeout(this.ctx, sessionTimeout);
/* 205:    */         }
/* 206:    */       }
/* 207:255 */       success = true;
/* 208:    */     }
/* 209:    */     finally
/* 210:    */     {
/* 211:257 */       if (!success) {
/* 212:258 */         destroyPools();
/* 213:    */       }
/* 214:    */     }
/* 215:262 */     this.stats = new OpenSslSessionStats(this.ctx);
/* 216:    */   }
/* 217:    */   
/* 218:    */   public boolean isClient()
/* 219:    */   {
/* 220:267 */     return false;
/* 221:    */   }
/* 222:    */   
/* 223:    */   public List<String> cipherSuites()
/* 224:    */   {
/* 225:272 */     return this.unmodifiableCiphers;
/* 226:    */   }
/* 227:    */   
/* 228:    */   public long sessionCacheSize()
/* 229:    */   {
/* 230:277 */     return this.sessionCacheSize;
/* 231:    */   }
/* 232:    */   
/* 233:    */   public long sessionTimeout()
/* 234:    */   {
/* 235:282 */     return this.sessionTimeout;
/* 236:    */   }
/* 237:    */   
/* 238:    */   public List<String> nextProtocols()
/* 239:    */   {
/* 240:287 */     return this.nextProtocols;
/* 241:    */   }
/* 242:    */   
/* 243:    */   public long context()
/* 244:    */   {
/* 245:294 */     return this.ctx;
/* 246:    */   }
/* 247:    */   
/* 248:    */   public OpenSslSessionStats stats()
/* 249:    */   {
/* 250:301 */     return this.stats;
/* 251:    */   }
/* 252:    */   
/* 253:    */   public SSLEngine newEngine(ByteBufAllocator alloc)
/* 254:    */   {
/* 255:309 */     if (this.nextProtocols.isEmpty()) {
/* 256:310 */       return new OpenSslEngine(this.ctx, alloc, null);
/* 257:    */     }
/* 258:312 */     return new OpenSslEngine(this.ctx, alloc, (String)this.nextProtocols.get(this.nextProtocols.size() - 1));
/* 259:    */   }
/* 260:    */   
/* 261:    */   public SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort)
/* 262:    */   {
/* 263:318 */     throw new UnsupportedOperationException();
/* 264:    */   }
/* 265:    */   
/* 266:    */   public void setTicketKeys(byte[] keys)
/* 267:    */   {
/* 268:325 */     if (keys != null) {
/* 269:326 */       throw new NullPointerException("keys");
/* 270:    */     }
/* 271:328 */     SSLContext.setSessionTicketKeys(this.ctx, keys);
/* 272:    */   }
/* 273:    */   
/* 274:    */   protected void finalize()
/* 275:    */     throws Throwable
/* 276:    */   {
/* 277:334 */     super.finalize();
/* 278:335 */     synchronized (OpenSslServerContext.class)
/* 279:    */     {
/* 280:336 */       if (this.ctx != 0L) {
/* 281:337 */         SSLContext.free(this.ctx);
/* 282:    */       }
/* 283:    */     }
/* 284:341 */     destroyPools();
/* 285:    */   }
/* 286:    */   
/* 287:    */   private void destroyPools()
/* 288:    */   {
/* 289:345 */     if (this.aprPool != 0L) {
/* 290:346 */       Pool.destroy(this.aprPool);
/* 291:    */     }
/* 292:    */   }
/* 293:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.OpenSslServerContext
 * JD-Core Version:    0.7.0.1
 */