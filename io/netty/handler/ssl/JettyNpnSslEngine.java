/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import java.nio.ByteBuffer;
/*   4:    */ import java.util.List;
/*   5:    */ import javax.net.ssl.SSLEngine;
/*   6:    */ import javax.net.ssl.SSLEngineResult;
/*   7:    */ import javax.net.ssl.SSLEngineResult.HandshakeStatus;
/*   8:    */ import javax.net.ssl.SSLException;
/*   9:    */ import javax.net.ssl.SSLParameters;
/*  10:    */ import javax.net.ssl.SSLSession;
/*  11:    */ import org.eclipse.jetty.npn.NextProtoNego;
/*  12:    */ import org.eclipse.jetty.npn.NextProtoNego.ClientProvider;
/*  13:    */ import org.eclipse.jetty.npn.NextProtoNego.ServerProvider;
/*  14:    */ 
/*  15:    */ final class JettyNpnSslEngine
/*  16:    */   extends SSLEngine
/*  17:    */ {
/*  18:    */   private static boolean available;
/*  19:    */   private final SSLEngine engine;
/*  20:    */   private final JettyNpnSslSession session;
/*  21:    */   
/*  22:    */   static boolean isAvailable()
/*  23:    */   {
/*  24: 37 */     updateAvailability();
/*  25: 38 */     return available;
/*  26:    */   }
/*  27:    */   
/*  28:    */   private static void updateAvailability()
/*  29:    */   {
/*  30: 42 */     if (available) {
/*  31: 43 */       return;
/*  32:    */     }
/*  33:    */     try
/*  34:    */     {
/*  35: 47 */       ClassLoader bootloader = ClassLoader.getSystemClassLoader().getParent();
/*  36: 48 */       if (bootloader == null) {
/*  37: 51 */         bootloader = ClassLoader.getSystemClassLoader();
/*  38:    */       }
/*  39: 53 */       Class.forName("sun.security.ssl.NextProtoNegoExtension", true, bootloader);
/*  40: 54 */       available = true;
/*  41:    */     }
/*  42:    */     catch (Exception ignore) {}
/*  43:    */   }
/*  44:    */   
/*  45:    */   JettyNpnSslEngine(SSLEngine engine, final List<String> nextProtocols, boolean server)
/*  46:    */   {
/*  47: 64 */     assert (!nextProtocols.isEmpty());
/*  48:    */     
/*  49: 66 */     this.engine = engine;
/*  50: 67 */     this.session = new JettyNpnSslSession(engine);
/*  51: 69 */     if (server)
/*  52:    */     {
/*  53: 70 */       NextProtoNego.put(engine, new NextProtoNego.ServerProvider()
/*  54:    */       {
/*  55:    */         public void unsupported()
/*  56:    */         {
/*  57: 73 */           JettyNpnSslEngine.this.getSession().setApplicationProtocol((String)nextProtocols.get(nextProtocols.size() - 1));
/*  58:    */         }
/*  59:    */         
/*  60:    */         public List<String> protocols()
/*  61:    */         {
/*  62: 78 */           return nextProtocols;
/*  63:    */         }
/*  64:    */         
/*  65:    */         public void protocolSelected(String protocol)
/*  66:    */         {
/*  67: 83 */           JettyNpnSslEngine.this.getSession().setApplicationProtocol(protocol);
/*  68:    */         }
/*  69:    */       });
/*  70:    */     }
/*  71:    */     else
/*  72:    */     {
/*  73: 87 */       final String[] list = (String[])nextProtocols.toArray(new String[nextProtocols.size()]);
/*  74: 88 */       final String fallback = list[(list.length - 1)];
/*  75:    */       
/*  76: 90 */       NextProtoNego.put(engine, new NextProtoNego.ClientProvider()
/*  77:    */       {
/*  78:    */         public boolean supports()
/*  79:    */         {
/*  80: 93 */           return true;
/*  81:    */         }
/*  82:    */         
/*  83:    */         public void unsupported()
/*  84:    */         {
/*  85: 98 */           JettyNpnSslEngine.this.session.setApplicationProtocol(null);
/*  86:    */         }
/*  87:    */         
/*  88:    */         public String selectProtocol(List<String> protocols)
/*  89:    */         {
/*  90:103 */           for (String p : list) {
/*  91:104 */             if (protocols.contains(p)) {
/*  92:105 */               return p;
/*  93:    */             }
/*  94:    */           }
/*  95:108 */           return fallback;
/*  96:    */         }
/*  97:    */       });
/*  98:    */     }
/*  99:    */   }
/* 100:    */   
/* 101:    */   public JettyNpnSslSession getSession()
/* 102:    */   {
/* 103:116 */     return this.session;
/* 104:    */   }
/* 105:    */   
/* 106:    */   public void closeInbound()
/* 107:    */     throws SSLException
/* 108:    */   {
/* 109:121 */     NextProtoNego.remove(this.engine);
/* 110:122 */     this.engine.closeInbound();
/* 111:    */   }
/* 112:    */   
/* 113:    */   public void closeOutbound()
/* 114:    */   {
/* 115:127 */     NextProtoNego.remove(this.engine);
/* 116:128 */     this.engine.closeOutbound();
/* 117:    */   }
/* 118:    */   
/* 119:    */   public String getPeerHost()
/* 120:    */   {
/* 121:133 */     return this.engine.getPeerHost();
/* 122:    */   }
/* 123:    */   
/* 124:    */   public int getPeerPort()
/* 125:    */   {
/* 126:138 */     return this.engine.getPeerPort();
/* 127:    */   }
/* 128:    */   
/* 129:    */   public SSLEngineResult wrap(ByteBuffer byteBuffer, ByteBuffer byteBuffer2)
/* 130:    */     throws SSLException
/* 131:    */   {
/* 132:143 */     return this.engine.wrap(byteBuffer, byteBuffer2);
/* 133:    */   }
/* 134:    */   
/* 135:    */   public SSLEngineResult wrap(ByteBuffer[] byteBuffers, ByteBuffer byteBuffer)
/* 136:    */     throws SSLException
/* 137:    */   {
/* 138:148 */     return this.engine.wrap(byteBuffers, byteBuffer);
/* 139:    */   }
/* 140:    */   
/* 141:    */   public SSLEngineResult wrap(ByteBuffer[] byteBuffers, int i, int i2, ByteBuffer byteBuffer)
/* 142:    */     throws SSLException
/* 143:    */   {
/* 144:153 */     return this.engine.wrap(byteBuffers, i, i2, byteBuffer);
/* 145:    */   }
/* 146:    */   
/* 147:    */   public SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer byteBuffer2)
/* 148:    */     throws SSLException
/* 149:    */   {
/* 150:158 */     return this.engine.unwrap(byteBuffer, byteBuffer2);
/* 151:    */   }
/* 152:    */   
/* 153:    */   public SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer[] byteBuffers)
/* 154:    */     throws SSLException
/* 155:    */   {
/* 156:163 */     return this.engine.unwrap(byteBuffer, byteBuffers);
/* 157:    */   }
/* 158:    */   
/* 159:    */   public SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer[] byteBuffers, int i, int i2)
/* 160:    */     throws SSLException
/* 161:    */   {
/* 162:168 */     return this.engine.unwrap(byteBuffer, byteBuffers, i, i2);
/* 163:    */   }
/* 164:    */   
/* 165:    */   public Runnable getDelegatedTask()
/* 166:    */   {
/* 167:173 */     return this.engine.getDelegatedTask();
/* 168:    */   }
/* 169:    */   
/* 170:    */   public boolean isInboundDone()
/* 171:    */   {
/* 172:178 */     return this.engine.isInboundDone();
/* 173:    */   }
/* 174:    */   
/* 175:    */   public boolean isOutboundDone()
/* 176:    */   {
/* 177:183 */     return this.engine.isOutboundDone();
/* 178:    */   }
/* 179:    */   
/* 180:    */   public String[] getSupportedCipherSuites()
/* 181:    */   {
/* 182:188 */     return this.engine.getSupportedCipherSuites();
/* 183:    */   }
/* 184:    */   
/* 185:    */   public String[] getEnabledCipherSuites()
/* 186:    */   {
/* 187:193 */     return this.engine.getEnabledCipherSuites();
/* 188:    */   }
/* 189:    */   
/* 190:    */   public void setEnabledCipherSuites(String[] strings)
/* 191:    */   {
/* 192:198 */     this.engine.setEnabledCipherSuites(strings);
/* 193:    */   }
/* 194:    */   
/* 195:    */   public String[] getSupportedProtocols()
/* 196:    */   {
/* 197:203 */     return this.engine.getSupportedProtocols();
/* 198:    */   }
/* 199:    */   
/* 200:    */   public String[] getEnabledProtocols()
/* 201:    */   {
/* 202:208 */     return this.engine.getEnabledProtocols();
/* 203:    */   }
/* 204:    */   
/* 205:    */   public void setEnabledProtocols(String[] strings)
/* 206:    */   {
/* 207:213 */     this.engine.setEnabledProtocols(strings);
/* 208:    */   }
/* 209:    */   
/* 210:    */   public SSLSession getHandshakeSession()
/* 211:    */   {
/* 212:218 */     return this.engine.getHandshakeSession();
/* 213:    */   }
/* 214:    */   
/* 215:    */   public void beginHandshake()
/* 216:    */     throws SSLException
/* 217:    */   {
/* 218:223 */     this.engine.beginHandshake();
/* 219:    */   }
/* 220:    */   
/* 221:    */   public SSLEngineResult.HandshakeStatus getHandshakeStatus()
/* 222:    */   {
/* 223:228 */     return this.engine.getHandshakeStatus();
/* 224:    */   }
/* 225:    */   
/* 226:    */   public void setUseClientMode(boolean b)
/* 227:    */   {
/* 228:233 */     this.engine.setUseClientMode(b);
/* 229:    */   }
/* 230:    */   
/* 231:    */   public boolean getUseClientMode()
/* 232:    */   {
/* 233:238 */     return this.engine.getUseClientMode();
/* 234:    */   }
/* 235:    */   
/* 236:    */   public void setNeedClientAuth(boolean b)
/* 237:    */   {
/* 238:243 */     this.engine.setNeedClientAuth(b);
/* 239:    */   }
/* 240:    */   
/* 241:    */   public boolean getNeedClientAuth()
/* 242:    */   {
/* 243:248 */     return this.engine.getNeedClientAuth();
/* 244:    */   }
/* 245:    */   
/* 246:    */   public void setWantClientAuth(boolean b)
/* 247:    */   {
/* 248:253 */     this.engine.setWantClientAuth(b);
/* 249:    */   }
/* 250:    */   
/* 251:    */   public boolean getWantClientAuth()
/* 252:    */   {
/* 253:258 */     return this.engine.getWantClientAuth();
/* 254:    */   }
/* 255:    */   
/* 256:    */   public void setEnableSessionCreation(boolean b)
/* 257:    */   {
/* 258:263 */     this.engine.setEnableSessionCreation(b);
/* 259:    */   }
/* 260:    */   
/* 261:    */   public boolean getEnableSessionCreation()
/* 262:    */   {
/* 263:268 */     return this.engine.getEnableSessionCreation();
/* 264:    */   }
/* 265:    */   
/* 266:    */   public SSLParameters getSSLParameters()
/* 267:    */   {
/* 268:273 */     return this.engine.getSSLParameters();
/* 269:    */   }
/* 270:    */   
/* 271:    */   public void setSSLParameters(SSLParameters sslParameters)
/* 272:    */   {
/* 273:278 */     this.engine.setSSLParameters(sslParameters);
/* 274:    */   }
/* 275:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.JettyNpnSslEngine
 * JD-Core Version:    0.7.0.1
 */