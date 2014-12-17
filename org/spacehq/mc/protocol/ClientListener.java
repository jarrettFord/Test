/*   1:    */ package org.spacehq.mc.protocol;
/*   2:    */ 
/*   3:    */ import java.math.BigInteger;
/*   4:    */ import javax.crypto.SecretKey;
/*   5:    */ import org.spacehq.mc.auth.GameProfile;
/*   6:    */ import org.spacehq.mc.auth.SessionService;
/*   7:    */ import org.spacehq.mc.auth.exception.AuthenticationException;
/*   8:    */ import org.spacehq.mc.auth.exception.AuthenticationUnavailableException;
/*   9:    */ import org.spacehq.mc.auth.exception.InvalidCredentialsException;
/*  10:    */ import org.spacehq.mc.protocol.data.message.Message;
/*  11:    */ import org.spacehq.mc.protocol.data.status.ServerStatusInfo;
/*  12:    */ import org.spacehq.mc.protocol.data.status.handler.ServerInfoHandler;
/*  13:    */ import org.spacehq.mc.protocol.data.status.handler.ServerPingTimeHandler;
/*  14:    */ import org.spacehq.mc.protocol.packet.handshake.client.HandshakePacket;
/*  15:    */ import org.spacehq.mc.protocol.packet.ingame.client.ClientKeepAlivePacket;
/*  16:    */ import org.spacehq.mc.protocol.packet.ingame.server.ServerDisconnectPacket;
/*  17:    */ import org.spacehq.mc.protocol.packet.ingame.server.ServerKeepAlivePacket;
/*  18:    */ import org.spacehq.mc.protocol.packet.login.client.EncryptionResponsePacket;
/*  19:    */ import org.spacehq.mc.protocol.packet.login.client.LoginStartPacket;
/*  20:    */ import org.spacehq.mc.protocol.packet.login.server.EncryptionRequestPacket;
/*  21:    */ import org.spacehq.mc.protocol.packet.login.server.LoginDisconnectPacket;
/*  22:    */ import org.spacehq.mc.protocol.packet.login.server.LoginSuccessPacket;
/*  23:    */ import org.spacehq.mc.protocol.packet.status.client.StatusPingPacket;
/*  24:    */ import org.spacehq.mc.protocol.packet.status.client.StatusQueryPacket;
/*  25:    */ import org.spacehq.mc.protocol.packet.status.server.StatusPongPacket;
/*  26:    */ import org.spacehq.mc.protocol.packet.status.server.StatusResponsePacket;
/*  27:    */ import org.spacehq.mc.protocol.util.CryptUtil;
/*  28:    */ import org.spacehq.packetlib.Session;
/*  29:    */ import org.spacehq.packetlib.event.session.ConnectedEvent;
/*  30:    */ import org.spacehq.packetlib.event.session.PacketReceivedEvent;
/*  31:    */ import org.spacehq.packetlib.event.session.PacketSentEvent;
/*  32:    */ import org.spacehq.packetlib.event.session.SessionAdapter;
/*  33:    */ 
/*  34:    */ public class ClientListener
/*  35:    */   extends SessionAdapter
/*  36:    */ {
/*  37:    */   private SecretKey key;
/*  38:    */   
/*  39:    */   public void packetReceived(PacketReceivedEvent event)
/*  40:    */   {
/*  41: 39 */     MinecraftProtocol protocol = (MinecraftProtocol)event.getSession().getPacketProtocol();
/*  42: 40 */     if (protocol.getMode() == ProtocolMode.LOGIN) {
/*  43: 41 */       if ((event.getPacket() instanceof EncryptionRequestPacket))
/*  44:    */       {
/*  45: 42 */         EncryptionRequestPacket packet = (EncryptionRequestPacket)event.getPacket();
/*  46: 43 */         this.key = CryptUtil.generateSharedKey();
/*  47:    */         
/*  48: 45 */         GameProfile profile = (GameProfile)event.getSession().getFlag("profile");
/*  49: 46 */         String serverHash = new BigInteger(CryptUtil.getServerIdHash(packet.getServerId(), packet.getPublicKey(), this.key)).toString(16);
/*  50: 47 */         String accessToken = (String)event.getSession().getFlag("access-token");
/*  51:    */         try
/*  52:    */         {
/*  53: 49 */           new SessionService().joinServer(profile, accessToken, serverHash);
/*  54:    */         }
/*  55:    */         catch (AuthenticationUnavailableException e)
/*  56:    */         {
/*  57: 51 */           event.getSession().disconnect("Login failed: Authentication service unavailable.");
/*  58: 52 */           return;
/*  59:    */         }
/*  60:    */         catch (InvalidCredentialsException e)
/*  61:    */         {
/*  62: 54 */           event.getSession().disconnect("Login failed: Invalid login session.");
/*  63: 55 */           return;
/*  64:    */         }
/*  65:    */         catch (AuthenticationException e)
/*  66:    */         {
/*  67: 57 */           event.getSession().disconnect("Login failed: Authentication error: " + e.getMessage());
/*  68: 58 */           return;
/*  69:    */         }
/*  70: 61 */         event.getSession().send(new EncryptionResponsePacket(this.key, packet.getPublicKey(), packet.getVerifyToken()));
/*  71:    */       }
/*  72: 62 */       else if ((event.getPacket() instanceof LoginSuccessPacket))
/*  73:    */       {
/*  74: 63 */         LoginSuccessPacket packet = (LoginSuccessPacket)event.getPacket();
/*  75: 64 */         event.getSession().setFlag("profile", packet.getProfile());
/*  76: 65 */         protocol.setMode(ProtocolMode.GAME, true, event.getSession());
/*  77:    */       }
/*  78: 66 */       else if ((event.getPacket() instanceof LoginDisconnectPacket))
/*  79:    */       {
/*  80: 67 */         LoginDisconnectPacket packet = (LoginDisconnectPacket)event.getPacket();
/*  81: 68 */         event.getSession().disconnect(packet.getReason().getFullText());
/*  82:    */       }
/*  83:    */     }
/*  84: 72 */     if (protocol.getMode() == ProtocolMode.STATUS) {
/*  85: 73 */       if ((event.getPacket() instanceof StatusResponsePacket))
/*  86:    */       {
/*  87: 74 */         ServerStatusInfo info = ((StatusResponsePacket)event.getPacket()).getInfo();
/*  88: 75 */         ServerInfoHandler handler = (ServerInfoHandler)event.getSession().getFlag("server-info-handler");
/*  89: 76 */         if (handler != null) {
/*  90: 77 */           handler.handle(event.getSession(), info);
/*  91:    */         }
/*  92: 80 */         event.getSession().send(new StatusPingPacket(System.nanoTime() / 1000000L));
/*  93:    */       }
/*  94: 81 */       else if ((event.getPacket() instanceof StatusPongPacket))
/*  95:    */       {
/*  96: 82 */         long time = System.nanoTime() / 1000000L - ((StatusPongPacket)event.getPacket()).getPingTime();
/*  97: 83 */         ServerPingTimeHandler handler = (ServerPingTimeHandler)event.getSession().getFlag("server-ping-time-handler");
/*  98: 84 */         if (handler != null) {
/*  99: 85 */           handler.handle(event.getSession(), time);
/* 100:    */         }
/* 101: 88 */         event.getSession().disconnect("Finished");
/* 102:    */       }
/* 103:    */     }
/* 104: 92 */     if (protocol.getMode() == ProtocolMode.GAME) {
/* 105: 93 */       if ((event.getPacket() instanceof ServerKeepAlivePacket)) {
/* 106: 94 */         event.getSession().send(new ClientKeepAlivePacket(((ServerKeepAlivePacket)event.getPacket()).getPingId()));
/* 107: 95 */       } else if ((event.getPacket() instanceof ServerDisconnectPacket)) {
/* 108: 96 */         event.getSession().disconnect(((ServerDisconnectPacket)event.getPacket()).getReason().getFullText());
/* 109:    */       }
/* 110:    */     }
/* 111:    */   }
/* 112:    */   
/* 113:    */   public void packetSent(PacketSentEvent event)
/* 114:    */   {
/* 115:103 */     MinecraftProtocol protocol = (MinecraftProtocol)event.getSession().getPacketProtocol();
/* 116:104 */     if ((protocol.getMode() == ProtocolMode.LOGIN) && ((event.getPacket() instanceof EncryptionResponsePacket))) {
/* 117:105 */       protocol.enableEncryption(this.key);
/* 118:    */     }
/* 119:    */   }
/* 120:    */   
/* 121:    */   public void connected(ConnectedEvent event)
/* 122:    */   {
/* 123:111 */     MinecraftProtocol protocol = (MinecraftProtocol)event.getSession().getPacketProtocol();
/* 124:112 */     if (protocol.getMode() == ProtocolMode.LOGIN)
/* 125:    */     {
/* 126:113 */       GameProfile profile = (GameProfile)event.getSession().getFlag("profile");
/* 127:114 */       protocol.setMode(ProtocolMode.HANDSHAKE, true, event.getSession());
/* 128:115 */       event.getSession().send(new HandshakePacket(ProtocolConstants.PROTOCOL_VERSION, event.getSession().getHost(), event.getSession().getPort(), 2));
/* 129:116 */       protocol.setMode(ProtocolMode.LOGIN, true, event.getSession());
/* 130:117 */       event.getSession().send(new LoginStartPacket(profile != null ? profile.getName() : ""));
/* 131:    */     }
/* 132:118 */     else if (protocol.getMode() == ProtocolMode.STATUS)
/* 133:    */     {
/* 134:119 */       protocol.setMode(ProtocolMode.HANDSHAKE, true, event.getSession());
/* 135:120 */       event.getSession().send(new HandshakePacket(ProtocolConstants.PROTOCOL_VERSION, event.getSession().getHost(), event.getSession().getPort(), 1));
/* 136:121 */       protocol.setMode(ProtocolMode.STATUS, true, event.getSession());
/* 137:122 */       event.getSession().send(new StatusQueryPacket());
/* 138:    */     }
/* 139:    */   }
/* 140:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.ClientListener
 * JD-Core Version:    0.7.0.1
 */