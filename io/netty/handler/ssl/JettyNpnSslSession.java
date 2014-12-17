/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import java.security.Principal;
/*   4:    */ import java.security.cert.Certificate;
/*   5:    */ import javax.net.ssl.SSLEngine;
/*   6:    */ import javax.net.ssl.SSLPeerUnverifiedException;
/*   7:    */ import javax.net.ssl.SSLSession;
/*   8:    */ import javax.net.ssl.SSLSessionContext;
/*   9:    */ import javax.security.cert.X509Certificate;
/*  10:    */ 
/*  11:    */ final class JettyNpnSslSession
/*  12:    */   implements SSLSession
/*  13:    */ {
/*  14:    */   private final SSLEngine engine;
/*  15:    */   private volatile String applicationProtocol;
/*  16:    */   
/*  17:    */   JettyNpnSslSession(SSLEngine engine)
/*  18:    */   {
/*  19: 33 */     this.engine = engine;
/*  20:    */   }
/*  21:    */   
/*  22:    */   void setApplicationProtocol(String applicationProtocol)
/*  23:    */   {
/*  24: 37 */     if (applicationProtocol != null) {
/*  25: 38 */       applicationProtocol = applicationProtocol.replace(':', '_');
/*  26:    */     }
/*  27: 40 */     this.applicationProtocol = applicationProtocol;
/*  28:    */   }
/*  29:    */   
/*  30:    */   public String getProtocol()
/*  31:    */   {
/*  32: 45 */     String protocol = unwrap().getProtocol();
/*  33: 46 */     String applicationProtocol = this.applicationProtocol;
/*  34: 48 */     if (applicationProtocol == null)
/*  35:    */     {
/*  36: 49 */       if (protocol != null) {
/*  37: 50 */         return protocol.replace(':', '_');
/*  38:    */       }
/*  39: 52 */       return null;
/*  40:    */     }
/*  41: 56 */     StringBuilder buf = new StringBuilder(32);
/*  42: 57 */     if (protocol != null)
/*  43:    */     {
/*  44: 58 */       buf.append(protocol.replace(':', '_'));
/*  45: 59 */       buf.append(':');
/*  46:    */     }
/*  47:    */     else
/*  48:    */     {
/*  49: 61 */       buf.append("null:");
/*  50:    */     }
/*  51: 63 */     buf.append(applicationProtocol);
/*  52: 64 */     return buf.toString();
/*  53:    */   }
/*  54:    */   
/*  55:    */   private SSLSession unwrap()
/*  56:    */   {
/*  57: 68 */     return this.engine.getSession();
/*  58:    */   }
/*  59:    */   
/*  60:    */   public byte[] getId()
/*  61:    */   {
/*  62: 73 */     return unwrap().getId();
/*  63:    */   }
/*  64:    */   
/*  65:    */   public SSLSessionContext getSessionContext()
/*  66:    */   {
/*  67: 78 */     return unwrap().getSessionContext();
/*  68:    */   }
/*  69:    */   
/*  70:    */   public long getCreationTime()
/*  71:    */   {
/*  72: 83 */     return unwrap().getCreationTime();
/*  73:    */   }
/*  74:    */   
/*  75:    */   public long getLastAccessedTime()
/*  76:    */   {
/*  77: 88 */     return unwrap().getLastAccessedTime();
/*  78:    */   }
/*  79:    */   
/*  80:    */   public void invalidate()
/*  81:    */   {
/*  82: 93 */     unwrap().invalidate();
/*  83:    */   }
/*  84:    */   
/*  85:    */   public boolean isValid()
/*  86:    */   {
/*  87: 98 */     return unwrap().isValid();
/*  88:    */   }
/*  89:    */   
/*  90:    */   public void putValue(String s, Object o)
/*  91:    */   {
/*  92:103 */     unwrap().putValue(s, o);
/*  93:    */   }
/*  94:    */   
/*  95:    */   public Object getValue(String s)
/*  96:    */   {
/*  97:108 */     return unwrap().getValue(s);
/*  98:    */   }
/*  99:    */   
/* 100:    */   public void removeValue(String s)
/* 101:    */   {
/* 102:113 */     unwrap().removeValue(s);
/* 103:    */   }
/* 104:    */   
/* 105:    */   public String[] getValueNames()
/* 106:    */   {
/* 107:118 */     return unwrap().getValueNames();
/* 108:    */   }
/* 109:    */   
/* 110:    */   public Certificate[] getPeerCertificates()
/* 111:    */     throws SSLPeerUnverifiedException
/* 112:    */   {
/* 113:123 */     return unwrap().getPeerCertificates();
/* 114:    */   }
/* 115:    */   
/* 116:    */   public Certificate[] getLocalCertificates()
/* 117:    */   {
/* 118:128 */     return unwrap().getLocalCertificates();
/* 119:    */   }
/* 120:    */   
/* 121:    */   public X509Certificate[] getPeerCertificateChain()
/* 122:    */     throws SSLPeerUnverifiedException
/* 123:    */   {
/* 124:133 */     return unwrap().getPeerCertificateChain();
/* 125:    */   }
/* 126:    */   
/* 127:    */   public Principal getPeerPrincipal()
/* 128:    */     throws SSLPeerUnverifiedException
/* 129:    */   {
/* 130:138 */     return unwrap().getPeerPrincipal();
/* 131:    */   }
/* 132:    */   
/* 133:    */   public Principal getLocalPrincipal()
/* 134:    */   {
/* 135:143 */     return unwrap().getLocalPrincipal();
/* 136:    */   }
/* 137:    */   
/* 138:    */   public String getCipherSuite()
/* 139:    */   {
/* 140:148 */     return unwrap().getCipherSuite();
/* 141:    */   }
/* 142:    */   
/* 143:    */   public String getPeerHost()
/* 144:    */   {
/* 145:153 */     return unwrap().getPeerHost();
/* 146:    */   }
/* 147:    */   
/* 148:    */   public int getPeerPort()
/* 149:    */   {
/* 150:158 */     return unwrap().getPeerPort();
/* 151:    */   }
/* 152:    */   
/* 153:    */   public int getPacketBufferSize()
/* 154:    */   {
/* 155:163 */     return unwrap().getPacketBufferSize();
/* 156:    */   }
/* 157:    */   
/* 158:    */   public int getApplicationBufferSize()
/* 159:    */   {
/* 160:168 */     return unwrap().getApplicationBufferSize();
/* 161:    */   }
/* 162:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.JettyNpnSslSession
 * JD-Core Version:    0.7.0.1
 */