/*   1:    */ package org.spacehq.mc.protocol;
/*   2:    */ 
/*   3:    */ import java.math.BigInteger;
/*   4:    */ import java.security.KeyPair;
/*   5:    */ import java.security.PrivateKey;
/*   6:    */ import java.util.Arrays;
/*   7:    */ import java.util.Random;
/*   8:    */ import java.util.UUID;
/*   9:    */ import javax.crypto.SecretKey;
/*  10:    */ import org.spacehq.mc.auth.GameProfile;
/*  11:    */ import org.spacehq.mc.auth.SessionService;
/*  12:    */ import org.spacehq.mc.auth.exception.AuthenticationUnavailableException;
/*  13:    */ import org.spacehq.mc.protocol.data.status.ServerStatusInfo;
/*  14:    */ import org.spacehq.mc.protocol.data.status.handler.ServerInfoBuilder;
/*  15:    */ import org.spacehq.mc.protocol.packet.handshake.client.HandshakePacket;
/*  16:    */ import org.spacehq.mc.protocol.packet.ingame.client.ClientKeepAlivePacket;
/*  17:    */ import org.spacehq.mc.protocol.packet.ingame.server.ServerDisconnectPacket;
/*  18:    */ import org.spacehq.mc.protocol.packet.ingame.server.ServerKeepAlivePacket;
/*  19:    */ import org.spacehq.mc.protocol.packet.login.client.EncryptionResponsePacket;
/*  20:    */ import org.spacehq.mc.protocol.packet.login.client.LoginStartPacket;
/*  21:    */ import org.spacehq.mc.protocol.packet.login.server.EncryptionRequestPacket;
/*  22:    */ import org.spacehq.mc.protocol.packet.login.server.LoginDisconnectPacket;
/*  23:    */ import org.spacehq.mc.protocol.packet.login.server.LoginSuccessPacket;
/*  24:    */ import org.spacehq.mc.protocol.packet.status.client.StatusPingPacket;
/*  25:    */ import org.spacehq.mc.protocol.packet.status.client.StatusQueryPacket;
/*  26:    */ import org.spacehq.mc.protocol.packet.status.server.StatusPongPacket;
/*  27:    */ import org.spacehq.mc.protocol.packet.status.server.StatusResponsePacket;
/*  28:    */ import org.spacehq.mc.protocol.util.CryptUtil;
/*  29:    */ import org.spacehq.packetlib.Session;
/*  30:    */ import org.spacehq.packetlib.event.session.DisconnectingEvent;
/*  31:    */ import org.spacehq.packetlib.event.session.PacketReceivedEvent;
/*  32:    */ import org.spacehq.packetlib.event.session.SessionAdapter;
/*  33:    */ 
/*  34:    */ public class ServerListener
/*  35:    */   extends SessionAdapter
/*  36:    */ {
/*  37: 37 */   private static KeyPair pair = ;
/*  38: 39 */   private byte[] verifyToken = new byte[4];
/*  39: 40 */   private String serverId = "";
/*  40: 41 */   private String username = "";
/*  41: 43 */   private long lastPingTime = 0L;
/*  42: 44 */   private int lastPingId = 0;
/*  43:    */   
/*  44:    */   public ServerListener()
/*  45:    */   {
/*  46: 47 */     new Random().nextBytes(this.verifyToken);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public void packetReceived(PacketReceivedEvent event)
/*  50:    */   {
/*  51: 52 */     MinecraftProtocol protocol = (MinecraftProtocol)event.getSession().getPacketProtocol();
/*  52: 53 */     if ((protocol.getMode() == ProtocolMode.HANDSHAKE) && 
/*  53: 54 */       ((event.getPacket() instanceof HandshakePacket)))
/*  54:    */     {
/*  55: 55 */       HandshakePacket packet = (HandshakePacket)event.getPacket();
/*  56: 56 */       switch (packet.getIntent())
/*  57:    */       {
/*  58:    */       case 1: 
/*  59: 58 */         protocol.setMode(ProtocolMode.STATUS, false, event.getSession());
/*  60: 59 */         break;
/*  61:    */       case 2: 
/*  62: 61 */         protocol.setMode(ProtocolMode.LOGIN, false, event.getSession());
/*  63: 62 */         if (packet.getProtocolVersion() > ProtocolConstants.PROTOCOL_VERSION) {
/*  64: 63 */           event.getSession().disconnect("Outdated server! I'm still on 1.7.7.");
/*  65: 64 */         } else if (packet.getProtocolVersion() < ProtocolConstants.PROTOCOL_VERSION) {
/*  66: 65 */           event.getSession().disconnect("Outdated client! Please use 1.7.7.");
/*  67:    */         }
/*  68: 68 */         break;
/*  69:    */       default: 
/*  70: 70 */         throw new UnsupportedOperationException("Invalid client intent: " + packet.getIntent());
/*  71:    */       }
/*  72:    */     }
/*  73: 75 */     if (protocol.getMode() == ProtocolMode.LOGIN) {
/*  74: 76 */       if ((event.getPacket() instanceof LoginStartPacket))
/*  75:    */       {
/*  76: 77 */         this.username = ((LoginStartPacket)event.getPacket()).getUsername();
/*  77: 78 */         boolean verify = event.getSession().hasFlag("verify-users") ? ((Boolean)event.getSession().getFlag("verify-users")).booleanValue() : true;
/*  78: 79 */         if (verify)
/*  79:    */         {
/*  80: 80 */           event.getSession().send(new EncryptionRequestPacket(this.serverId, pair.getPublic(), this.verifyToken));
/*  81:    */         }
/*  82:    */         else
/*  83:    */         {
/*  84: 82 */           GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.username).getBytes()), this.username);
/*  85: 83 */           event.getSession().send(new LoginSuccessPacket(profile));
/*  86: 84 */           event.getSession().setFlag("profile", profile);
/*  87: 85 */           protocol.setMode(ProtocolMode.GAME, false, event.getSession());
/*  88: 86 */           ServerLoginHandler handler = (ServerLoginHandler)event.getSession().getFlag("login-handler");
/*  89: 87 */           if (handler != null) {
/*  90: 88 */             handler.loggedIn(event.getSession());
/*  91:    */           }
/*  92: 91 */           new KeepAliveThread(event.getSession()).start();
/*  93:    */         }
/*  94:    */       }
/*  95: 93 */       else if ((event.getPacket() instanceof EncryptionResponsePacket))
/*  96:    */       {
/*  97: 94 */         EncryptionResponsePacket packet = (EncryptionResponsePacket)event.getPacket();
/*  98: 95 */         PrivateKey privateKey = pair.getPrivate();
/*  99: 96 */         if (!Arrays.equals(this.verifyToken, packet.getVerifyToken(privateKey))) {
/* 100: 97 */           throw new IllegalStateException("Invalid nonce!");
/* 101:    */         }
/* 102: 99 */         SecretKey key = packet.getSecretKey(privateKey);
/* 103:100 */         protocol.enableEncryption(key);
/* 104:101 */         new UserAuthThread(event.getSession(), key).start();
/* 105:    */       }
/* 106:    */     }
/* 107:106 */     if (protocol.getMode() == ProtocolMode.STATUS) {
/* 108:107 */       if ((event.getPacket() instanceof StatusQueryPacket))
/* 109:    */       {
/* 110:108 */         ServerInfoBuilder builder = (ServerInfoBuilder)event.getSession().getFlag("info-builder");
/* 111:109 */         if (builder == null) {
/* 112:110 */           event.getSession().disconnect("No server info builder set.");
/* 113:    */         }
/* 114:113 */         ServerStatusInfo info = builder.buildInfo(event.getSession());
/* 115:114 */         event.getSession().send(new StatusResponsePacket(info));
/* 116:    */       }
/* 117:115 */       else if ((event.getPacket() instanceof StatusPingPacket))
/* 118:    */       {
/* 119:116 */         event.getSession().send(new StatusPongPacket(((StatusPingPacket)event.getPacket()).getPingTime()));
/* 120:    */       }
/* 121:    */     }
/* 122:120 */     if ((protocol.getMode() == ProtocolMode.GAME) && 
/* 123:121 */       ((event.getPacket() instanceof ClientKeepAlivePacket)))
/* 124:    */     {
/* 125:122 */       ClientKeepAlivePacket packet = (ClientKeepAlivePacket)event.getPacket();
/* 126:123 */       if (packet.getPingId() == this.lastPingId)
/* 127:    */       {
/* 128:124 */         long time = System.nanoTime() / 1000000L - this.lastPingTime;
/* 129:125 */         event.getSession().setFlag("ping", Long.valueOf(time));
/* 130:    */       }
/* 131:    */     }
/* 132:    */   }
/* 133:    */   
/* 134:    */   public void disconnecting(DisconnectingEvent event)
/* 135:    */   {
/* 136:133 */     MinecraftProtocol protocol = (MinecraftProtocol)event.getSession().getPacketProtocol();
/* 137:134 */     if (protocol.getMode() == ProtocolMode.LOGIN) {
/* 138:135 */       event.getSession().send(new LoginDisconnectPacket(event.getReason()));
/* 139:136 */     } else if (protocol.getMode() == ProtocolMode.GAME) {
/* 140:137 */       event.getSession().send(new ServerDisconnectPacket(event.getReason()));
/* 141:    */     }
/* 142:    */   }
/* 143:    */   
/* 144:    */   private class UserAuthThread
/* 145:    */     extends Thread
/* 146:    */   {
/* 147:    */     private Session session;
/* 148:    */     private SecretKey key;
/* 149:    */     
/* 150:    */     public UserAuthThread(Session session, SecretKey key)
/* 151:    */     {
/* 152:146 */       this.key = key;
/* 153:147 */       this.session = session;
/* 154:    */     }
/* 155:    */     
/* 156:    */     public void run()
/* 157:    */     {
/* 158:152 */       MinecraftProtocol protocol = (MinecraftProtocol)this.session.getPacketProtocol();
/* 159:    */       try
/* 160:    */       {
/* 161:154 */         String serverHash = new BigInteger(CryptUtil.getServerIdHash(ServerListener.this.serverId, ServerListener.pair.getPublic(), this.key)).toString(16);
/* 162:155 */         SessionService service = new SessionService();
/* 163:156 */         GameProfile profile = service.hasJoinedServer(new GameProfile(null, ServerListener.this.username), serverHash);
/* 164:157 */         if (profile != null)
/* 165:    */         {
/* 166:158 */           this.session.send(new LoginSuccessPacket(profile));
/* 167:159 */           this.session.setFlag("profile", profile);
/* 168:160 */           protocol.setMode(ProtocolMode.GAME, false, this.session);
/* 169:161 */           ServerLoginHandler handler = (ServerLoginHandler)this.session.getFlag("login-handler");
/* 170:162 */           if (handler != null) {
/* 171:163 */             handler.loggedIn(this.session);
/* 172:    */           }
/* 173:166 */           new ServerListener.KeepAliveThread(ServerListener.this, this.session).start();
/* 174:    */         }
/* 175:    */         else
/* 176:    */         {
/* 177:168 */           this.session.disconnect("Failed to verify username!");
/* 178:    */         }
/* 179:    */       }
/* 180:    */       catch (AuthenticationUnavailableException e)
/* 181:    */       {
/* 182:171 */         this.session.disconnect("Authentication servers are down. Please try again later, sorry!");
/* 183:    */       }
/* 184:    */     }
/* 185:    */   }
/* 186:    */   
/* 187:    */   private class KeepAliveThread
/* 188:    */     extends Thread
/* 189:    */   {
/* 190:    */     private Session session;
/* 191:    */     
/* 192:    */     public KeepAliveThread(Session session)
/* 193:    */     {
/* 194:180 */       this.session = session;
/* 195:    */     }
/* 196:    */     
/* 197:    */     public void run()
/* 198:    */     {
/* 199:185 */       ServerListener.this.lastPingTime = (System.nanoTime() / 1000000L);
/* 200:186 */       while (this.session.isConnected())
/* 201:    */       {
/* 202:187 */         long curr = System.nanoTime() / 1000000L;
/* 203:188 */         long time = curr - ServerListener.this.lastPingTime;
/* 204:189 */         if (time > 2000L)
/* 205:    */         {
/* 206:190 */           ServerListener.this.lastPingTime = curr;
/* 207:191 */           ServerListener.this.lastPingId = ((int)curr);
/* 208:192 */           this.session.send(new ServerKeepAlivePacket(ServerListener.this.lastPingId));
/* 209:    */         }
/* 210:    */         try
/* 211:    */         {
/* 212:196 */           Thread.sleep(10L);
/* 213:    */         }
/* 214:    */         catch (InterruptedException e)
/* 215:    */         {
/* 216:198 */           e.printStackTrace();
/* 217:    */         }
/* 218:    */       }
/* 219:    */     }
/* 220:    */   }
/* 221:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.ServerListener
 * JD-Core Version:    0.7.0.1
 */