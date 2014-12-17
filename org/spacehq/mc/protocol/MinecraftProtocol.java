/*   1:    */ package org.spacehq.mc.protocol;
/*   2:    */ 
/*   3:    */ import java.security.GeneralSecurityException;
/*   4:    */ import java.security.Key;
/*   5:    */ import java.util.UUID;
/*   6:    */ import org.spacehq.mc.auth.GameProfile;
/*   7:    */ import org.spacehq.mc.auth.UserAuthentication;
/*   8:    */ import org.spacehq.mc.auth.exception.AuthenticationException;
/*   9:    */ import org.spacehq.mc.protocol.packet.handshake.client.HandshakePacket;
/*  10:    */ import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
/*  11:    */ import org.spacehq.mc.protocol.packet.ingame.client.ClientKeepAlivePacket;
/*  12:    */ import org.spacehq.mc.protocol.packet.ingame.client.ClientPluginMessagePacket;
/*  13:    */ import org.spacehq.mc.protocol.packet.ingame.client.ClientRequestPacket;
/*  14:    */ import org.spacehq.mc.protocol.packet.ingame.client.ClientSettingsPacket;
/*  15:    */ import org.spacehq.mc.protocol.packet.ingame.client.ClientTabCompletePacket;
/*  16:    */ import org.spacehq.mc.protocol.packet.ingame.client.player.ClientChangeHeldItemPacket;
/*  17:    */ import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerAbilitiesPacket;
/*  18:    */ import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerActionPacket;
/*  19:    */ import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerAnimationPacket;
/*  20:    */ import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerDigPacket;
/*  21:    */ import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerInteractEntityPacket;
/*  22:    */ import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerMovementPacket;
/*  23:    */ import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerPlaceBlockPacket;
/*  24:    */ import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
/*  25:    */ import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
/*  26:    */ import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerRotationPacket;
/*  27:    */ import org.spacehq.mc.protocol.packet.ingame.client.player.ClientSteerVehiclePacket;
/*  28:    */ import org.spacehq.mc.protocol.packet.ingame.client.window.ClientCloseWindowPacket;
/*  29:    */ import org.spacehq.mc.protocol.packet.ingame.client.window.ClientConfirmTransactionPacket;
/*  30:    */ import org.spacehq.mc.protocol.packet.ingame.client.window.ClientCreativeInventoryActionPacket;
/*  31:    */ import org.spacehq.mc.protocol.packet.ingame.client.window.ClientEnchantItemPacket;
/*  32:    */ import org.spacehq.mc.protocol.packet.ingame.client.window.ClientWindowActionPacket;
/*  33:    */ import org.spacehq.mc.protocol.packet.ingame.client.world.ClientUpdateSignPacket;
/*  34:    */ import org.spacehq.mc.protocol.packet.ingame.server.ServerChatPacket;
/*  35:    */ import org.spacehq.mc.protocol.packet.ingame.server.ServerDisconnectPacket;
/*  36:    */ import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
/*  37:    */ import org.spacehq.mc.protocol.packet.ingame.server.ServerKeepAlivePacket;
/*  38:    */ import org.spacehq.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
/*  39:    */ import org.spacehq.mc.protocol.packet.ingame.server.ServerPluginMessagePacket;
/*  40:    */ import org.spacehq.mc.protocol.packet.ingame.server.ServerRespawnPacket;
/*  41:    */ import org.spacehq.mc.protocol.packet.ingame.server.ServerStatisticsPacket;
/*  42:    */ import org.spacehq.mc.protocol.packet.ingame.server.ServerTabCompletePacket;
/*  43:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerAnimationPacket;
/*  44:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerCollectItemPacket;
/*  45:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerDestroyEntitiesPacket;
/*  46:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityAttachPacket;
/*  47:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityEffectPacket;
/*  48:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityEquipmentPacket;
/*  49:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityHeadLookPacket;
/*  50:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityMetadataPacket;
/*  51:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityMovementPacket;
/*  52:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityPositionPacket;
/*  53:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityPositionRotationPacket;
/*  54:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityPropertiesPacket;
/*  55:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityRemoveEffectPacket;
/*  56:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityRotationPacket;
/*  57:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityStatusPacket;
/*  58:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityTeleportPacket;
/*  59:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityVelocityPacket;
/*  60:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerChangeHeldItemPacket;
/*  61:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerAbilitiesPacket;
/*  62:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
/*  63:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerUseBedPacket;
/*  64:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerSetExperiencePacket;
/*  65:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerUpdateHealthPacket;
/*  66:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnExpOrbPacket;
/*  67:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnGlobalEntityPacket;
/*  68:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
/*  69:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnObjectPacket;
/*  70:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPaintingPacket;
/*  71:    */ import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
/*  72:    */ import org.spacehq.mc.protocol.packet.ingame.server.scoreboard.ServerDisplayScoreboardPacket;
/*  73:    */ import org.spacehq.mc.protocol.packet.ingame.server.scoreboard.ServerScoreboardObjectivePacket;
/*  74:    */ import org.spacehq.mc.protocol.packet.ingame.server.scoreboard.ServerTeamPacket;
/*  75:    */ import org.spacehq.mc.protocol.packet.ingame.server.scoreboard.ServerUpdateScorePacket;
/*  76:    */ import org.spacehq.mc.protocol.packet.ingame.server.window.ServerCloseWindowPacket;
/*  77:    */ import org.spacehq.mc.protocol.packet.ingame.server.window.ServerConfirmTransactionPacket;
/*  78:    */ import org.spacehq.mc.protocol.packet.ingame.server.window.ServerOpenWindowPacket;
/*  79:    */ import org.spacehq.mc.protocol.packet.ingame.server.window.ServerSetSlotPacket;
/*  80:    */ import org.spacehq.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
/*  81:    */ import org.spacehq.mc.protocol.packet.ingame.server.window.ServerWindowPropertyPacket;
/*  82:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockBreakAnimPacket;
/*  83:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket;
/*  84:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockValuePacket;
/*  85:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
/*  86:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerExplosionPacket;
/*  87:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMapDataPacket;
/*  88:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMultiBlockChangePacket;
/*  89:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMultiChunkDataPacket;
/*  90:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerNotifyClientPacket;
/*  91:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerOpenTileEntityEditorPacket;
/*  92:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerPlayEffectPacket;
/*  93:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerPlaySoundPacket;
/*  94:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerSpawnParticlePacket;
/*  95:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerSpawnPositionPacket;
/*  96:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerUpdateSignPacket;
/*  97:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerUpdateTileEntityPacket;
/*  98:    */ import org.spacehq.mc.protocol.packet.ingame.server.world.ServerUpdateTimePacket;
/*  99:    */ import org.spacehq.mc.protocol.packet.login.client.EncryptionResponsePacket;
/* 100:    */ import org.spacehq.mc.protocol.packet.login.client.LoginStartPacket;
/* 101:    */ import org.spacehq.mc.protocol.packet.login.server.EncryptionRequestPacket;
/* 102:    */ import org.spacehq.mc.protocol.packet.login.server.LoginDisconnectPacket;
/* 103:    */ import org.spacehq.mc.protocol.packet.login.server.LoginSuccessPacket;
/* 104:    */ import org.spacehq.mc.protocol.packet.status.client.StatusPingPacket;
/* 105:    */ import org.spacehq.mc.protocol.packet.status.client.StatusQueryPacket;
/* 106:    */ import org.spacehq.mc.protocol.packet.status.server.StatusPongPacket;
/* 107:    */ import org.spacehq.mc.protocol.packet.status.server.StatusResponsePacket;
/* 108:    */ import org.spacehq.packetlib.Client;
/* 109:    */ import org.spacehq.packetlib.Server;
/* 110:    */ import org.spacehq.packetlib.Session;
/* 111:    */ import org.spacehq.packetlib.crypt.AESEncryption;
/* 112:    */ import org.spacehq.packetlib.crypt.PacketEncryption;
/* 113:    */ import org.spacehq.packetlib.packet.DefaultPacketHeader;
/* 114:    */ import org.spacehq.packetlib.packet.PacketHeader;
/* 115:    */ import org.spacehq.packetlib.packet.PacketProtocol;
/* 116:    */ 
/* 117:    */ public class MinecraftProtocol
/* 118:    */   extends PacketProtocol
/* 119:    */ {
/* 120:120 */   private ProtocolMode mode = ProtocolMode.HANDSHAKE;
/* 121:121 */   private PacketHeader header = new DefaultPacketHeader();
/* 122:    */   private AESEncryption encrypt;
/* 123:    */   private GameProfile profile;
/* 124:125 */   private String accessToken = "";
/* 125:    */   private ClientListener clientListener;
/* 126:    */   
/* 127:    */   private MinecraftProtocol() {}
/* 128:    */   
/* 129:    */   public MinecraftProtocol(ProtocolMode mode)
/* 130:    */   {
/* 131:132 */     if ((mode != ProtocolMode.LOGIN) && (mode != ProtocolMode.STATUS)) {
/* 132:133 */       throw new IllegalArgumentException("Only login and status modes are permitted.");
/* 133:    */     }
/* 134:136 */     this.mode = mode;
/* 135:137 */     if (mode == ProtocolMode.LOGIN) {
/* 136:138 */       this.profile = new GameProfile(null, "Player");
/* 137:    */     }
/* 138:141 */     this.clientListener = new ClientListener();
/* 139:    */   }
/* 140:    */   
/* 141:    */   public MinecraftProtocol(String username)
/* 142:    */   {
/* 143:145 */     this(ProtocolMode.LOGIN);
/* 144:146 */     this.profile = new GameProfile(null, username);
/* 145:147 */     this.clientListener = new ClientListener();
/* 146:    */   }
/* 147:    */   
/* 148:    */   public MinecraftProtocol(String username, String using, boolean token)
/* 149:    */     throws AuthenticationException
/* 150:    */   {
/* 151:151 */     this(ProtocolMode.LOGIN);
/* 152:152 */     String clientToken = UUID.randomUUID().toString();
/* 153:153 */     UserAuthentication auth = new UserAuthentication(clientToken);
/* 154:154 */     auth.setUsername(username);
/* 155:155 */     if (token) {
/* 156:156 */       auth.setAccessToken(using);
/* 157:    */     } else {
/* 158:158 */       auth.setPassword(using);
/* 159:    */     }
/* 160:161 */     auth.login();
/* 161:162 */     this.profile = auth.getSelectedProfile();
/* 162:163 */     this.accessToken = auth.getAccessToken();
/* 163:164 */     this.clientListener = new ClientListener();
/* 164:    */   }
/* 165:    */   
/* 166:    */   public boolean needsPacketSizer()
/* 167:    */   {
/* 168:169 */     return true;
/* 169:    */   }
/* 170:    */   
/* 171:    */   public boolean needsPacketEncryptor()
/* 172:    */   {
/* 173:174 */     return true;
/* 174:    */   }
/* 175:    */   
/* 176:    */   public PacketHeader getPacketHeader()
/* 177:    */   {
/* 178:179 */     return this.header;
/* 179:    */   }
/* 180:    */   
/* 181:    */   public PacketEncryption getEncryption()
/* 182:    */   {
/* 183:184 */     return this.encrypt;
/* 184:    */   }
/* 185:    */   
/* 186:    */   public void newClientSession(Client client, Session session)
/* 187:    */   {
/* 188:189 */     if (this.profile != null)
/* 189:    */     {
/* 190:190 */       session.setFlag("profile", this.profile);
/* 191:191 */       session.setFlag("access-token", this.accessToken);
/* 192:    */     }
/* 193:194 */     setMode(this.mode, true, session);
/* 194:195 */     session.addListener(this.clientListener);
/* 195:    */   }
/* 196:    */   
/* 197:    */   public void newServerSession(Server server, Session session)
/* 198:    */   {
/* 199:200 */     setMode(ProtocolMode.HANDSHAKE, false, session);
/* 200:201 */     session.addListener(new ServerListener());
/* 201:    */   }
/* 202:    */   
/* 203:    */   protected void enableEncryption(Key key)
/* 204:    */   {
/* 205:    */     try
/* 206:    */     {
/* 207:206 */       this.encrypt = new AESEncryption(key);
/* 208:    */     }
/* 209:    */     catch (GeneralSecurityException e)
/* 210:    */     {
/* 211:208 */       throw new Error("Failed to enable protocol encryption.", e);
/* 212:    */     }
/* 213:    */   }
/* 214:    */   
/* 215:    */   public ProtocolMode getMode()
/* 216:    */   {
/* 217:213 */     return this.mode;
/* 218:    */   }
/* 219:    */   
/* 220:    */   protected void setMode(ProtocolMode mode, boolean client, Session session)
/* 221:    */   {
/* 222:217 */     clearPackets();
/* 223:218 */     switch (mode)
/* 224:    */     {
/* 225:    */     case GAME: 
/* 226:220 */       if (client) {
/* 227:221 */         initClientHandshake(session);
/* 228:    */       } else {
/* 229:223 */         initServerHandshake(session);
/* 230:    */       }
/* 231:226 */       break;
/* 232:    */     case HANDSHAKE: 
/* 233:228 */       if (client) {
/* 234:229 */         initClientLogin(session);
/* 235:    */       } else {
/* 236:231 */         initServerLogin(session);
/* 237:    */       }
/* 238:234 */       break;
/* 239:    */     case LOGIN: 
/* 240:236 */       if (client) {
/* 241:237 */         initClientGame(session);
/* 242:    */       } else {
/* 243:239 */         initServerGame(session);
/* 244:    */       }
/* 245:242 */       break;
/* 246:    */     case STATUS: 
/* 247:244 */       if (client) {
/* 248:245 */         initClientStatus(session);
/* 249:    */       } else {
/* 250:247 */         initServerStatus(session);
/* 251:    */       }
/* 252:    */       break;
/* 253:    */     }
/* 254:253 */     this.mode = mode;
/* 255:    */   }
/* 256:    */   
/* 257:    */   private void initClientHandshake(Session session)
/* 258:    */   {
/* 259:257 */     registerOutgoing(0, HandshakePacket.class);
/* 260:    */   }
/* 261:    */   
/* 262:    */   private void initServerHandshake(Session session)
/* 263:    */   {
/* 264:261 */     registerIncoming(0, HandshakePacket.class);
/* 265:    */   }
/* 266:    */   
/* 267:    */   private void initClientLogin(Session session)
/* 268:    */   {
/* 269:265 */     registerIncoming(0, LoginDisconnectPacket.class);
/* 270:266 */     registerIncoming(1, EncryptionRequestPacket.class);
/* 271:267 */     registerIncoming(2, LoginSuccessPacket.class);
/* 272:    */     
/* 273:269 */     registerOutgoing(0, LoginStartPacket.class);
/* 274:270 */     registerOutgoing(1, EncryptionResponsePacket.class);
/* 275:    */   }
/* 276:    */   
/* 277:    */   private void initServerLogin(Session session)
/* 278:    */   {
/* 279:274 */     registerIncoming(0, LoginStartPacket.class);
/* 280:275 */     registerIncoming(1, EncryptionResponsePacket.class);
/* 281:    */     
/* 282:277 */     registerOutgoing(0, LoginDisconnectPacket.class);
/* 283:278 */     registerOutgoing(1, EncryptionRequestPacket.class);
/* 284:279 */     registerOutgoing(2, LoginSuccessPacket.class);
/* 285:    */   }
/* 286:    */   
/* 287:    */   private void initClientGame(Session session)
/* 288:    */   {
/* 289:283 */     registerIncoming(0, ServerKeepAlivePacket.class);
/* 290:284 */     registerIncoming(1, ServerJoinGamePacket.class);
/* 291:285 */     registerIncoming(2, ServerChatPacket.class);
/* 292:286 */     registerIncoming(3, ServerUpdateTimePacket.class);
/* 293:287 */     registerIncoming(4, ServerEntityEquipmentPacket.class);
/* 294:288 */     registerIncoming(5, ServerSpawnPositionPacket.class);
/* 295:289 */     registerIncoming(6, ServerUpdateHealthPacket.class);
/* 296:290 */     registerIncoming(7, ServerRespawnPacket.class);
/* 297:291 */     registerIncoming(8, ServerPlayerPositionRotationPacket.class);
/* 298:292 */     registerIncoming(9, ServerChangeHeldItemPacket.class);
/* 299:293 */     registerIncoming(10, ServerPlayerUseBedPacket.class);
/* 300:294 */     registerIncoming(11, ServerAnimationPacket.class);
/* 301:295 */     registerIncoming(12, ServerSpawnPlayerPacket.class);
/* 302:296 */     registerIncoming(13, ServerCollectItemPacket.class);
/* 303:297 */     registerIncoming(14, ServerSpawnObjectPacket.class);
/* 304:298 */     registerIncoming(15, ServerSpawnMobPacket.class);
/* 305:299 */     registerIncoming(16, ServerSpawnPaintingPacket.class);
/* 306:300 */     registerIncoming(17, ServerSpawnExpOrbPacket.class);
/* 307:301 */     registerIncoming(18, ServerEntityVelocityPacket.class);
/* 308:302 */     registerIncoming(19, ServerDestroyEntitiesPacket.class);
/* 309:303 */     registerIncoming(20, ServerEntityMovementPacket.class);
/* 310:304 */     registerIncoming(21, ServerEntityPositionPacket.class);
/* 311:305 */     registerIncoming(22, ServerEntityRotationPacket.class);
/* 312:306 */     registerIncoming(23, ServerEntityPositionRotationPacket.class);
/* 313:307 */     registerIncoming(24, ServerEntityTeleportPacket.class);
/* 314:308 */     registerIncoming(25, ServerEntityHeadLookPacket.class);
/* 315:309 */     registerIncoming(26, ServerEntityStatusPacket.class);
/* 316:310 */     registerIncoming(27, ServerEntityAttachPacket.class);
/* 317:311 */     registerIncoming(28, ServerEntityMetadataPacket.class);
/* 318:312 */     registerIncoming(29, ServerEntityEffectPacket.class);
/* 319:313 */     registerIncoming(30, ServerEntityRemoveEffectPacket.class);
/* 320:314 */     registerIncoming(31, ServerSetExperiencePacket.class);
/* 321:315 */     registerIncoming(32, ServerEntityPropertiesPacket.class);
/* 322:316 */     registerIncoming(33, ServerChunkDataPacket.class);
/* 323:317 */     registerIncoming(34, ServerMultiBlockChangePacket.class);
/* 324:318 */     registerIncoming(35, ServerBlockChangePacket.class);
/* 325:319 */     registerIncoming(36, ServerBlockValuePacket.class);
/* 326:320 */     registerIncoming(37, ServerBlockBreakAnimPacket.class);
/* 327:321 */     registerIncoming(38, ServerMultiChunkDataPacket.class);
/* 328:322 */     registerIncoming(39, ServerExplosionPacket.class);
/* 329:323 */     registerIncoming(40, ServerPlayEffectPacket.class);
/* 330:324 */     registerIncoming(41, ServerPlaySoundPacket.class);
/* 331:325 */     registerIncoming(42, ServerSpawnParticlePacket.class);
/* 332:326 */     registerIncoming(43, ServerNotifyClientPacket.class);
/* 333:327 */     registerIncoming(44, ServerSpawnGlobalEntityPacket.class);
/* 334:328 */     registerIncoming(45, ServerOpenWindowPacket.class);
/* 335:329 */     registerIncoming(46, ServerCloseWindowPacket.class);
/* 336:330 */     registerIncoming(47, ServerSetSlotPacket.class);
/* 337:331 */     registerIncoming(48, ServerWindowItemsPacket.class);
/* 338:332 */     registerIncoming(49, ServerWindowPropertyPacket.class);
/* 339:333 */     registerIncoming(50, ServerConfirmTransactionPacket.class);
/* 340:334 */     registerIncoming(51, ServerUpdateSignPacket.class);
/* 341:335 */     registerIncoming(52, ServerMapDataPacket.class);
/* 342:336 */     registerIncoming(53, ServerUpdateTileEntityPacket.class);
/* 343:337 */     registerIncoming(54, ServerOpenTileEntityEditorPacket.class);
/* 344:338 */     registerIncoming(55, ServerStatisticsPacket.class);
/* 345:339 */     registerIncoming(56, ServerPlayerListEntryPacket.class);
/* 346:340 */     registerIncoming(57, ServerPlayerAbilitiesPacket.class);
/* 347:341 */     registerIncoming(58, ServerTabCompletePacket.class);
/* 348:342 */     registerIncoming(59, ServerScoreboardObjectivePacket.class);
/* 349:343 */     registerIncoming(60, ServerUpdateScorePacket.class);
/* 350:344 */     registerIncoming(61, ServerDisplayScoreboardPacket.class);
/* 351:345 */     registerIncoming(62, ServerTeamPacket.class);
/* 352:346 */     registerIncoming(63, ServerPluginMessagePacket.class);
/* 353:347 */     registerIncoming(64, ServerDisconnectPacket.class);
/* 354:    */     
/* 355:349 */     registerOutgoing(0, ClientKeepAlivePacket.class);
/* 356:350 */     registerOutgoing(1, ClientChatPacket.class);
/* 357:351 */     registerOutgoing(2, ClientPlayerInteractEntityPacket.class);
/* 358:352 */     registerOutgoing(3, ClientPlayerMovementPacket.class);
/* 359:353 */     registerOutgoing(4, ClientPlayerPositionPacket.class);
/* 360:354 */     registerOutgoing(5, ClientPlayerRotationPacket.class);
/* 361:355 */     registerOutgoing(6, ClientPlayerPositionRotationPacket.class);
/* 362:356 */     registerOutgoing(7, ClientPlayerDigPacket.class);
/* 363:357 */     registerOutgoing(8, ClientPlayerPlaceBlockPacket.class);
/* 364:358 */     registerOutgoing(9, ClientChangeHeldItemPacket.class);
/* 365:359 */     registerOutgoing(10, ClientPlayerAnimationPacket.class);
/* 366:360 */     registerOutgoing(11, ClientPlayerActionPacket.class);
/* 367:361 */     registerOutgoing(12, ClientSteerVehiclePacket.class);
/* 368:362 */     registerOutgoing(13, ClientCloseWindowPacket.class);
/* 369:363 */     registerOutgoing(14, ClientWindowActionPacket.class);
/* 370:364 */     registerOutgoing(15, ClientConfirmTransactionPacket.class);
/* 371:365 */     registerOutgoing(16, ClientCreativeInventoryActionPacket.class);
/* 372:366 */     registerOutgoing(17, ClientEnchantItemPacket.class);
/* 373:367 */     registerOutgoing(18, ClientUpdateSignPacket.class);
/* 374:368 */     registerOutgoing(19, ClientPlayerAbilitiesPacket.class);
/* 375:369 */     registerOutgoing(20, ClientTabCompletePacket.class);
/* 376:370 */     registerOutgoing(21, ClientSettingsPacket.class);
/* 377:371 */     registerOutgoing(22, ClientRequestPacket.class);
/* 378:372 */     registerOutgoing(23, ClientPluginMessagePacket.class);
/* 379:    */   }
/* 380:    */   
/* 381:    */   private void initServerGame(Session session)
/* 382:    */   {
/* 383:376 */     registerIncoming(0, ClientKeepAlivePacket.class);
/* 384:377 */     registerIncoming(1, ClientChatPacket.class);
/* 385:378 */     registerIncoming(2, ClientPlayerInteractEntityPacket.class);
/* 386:379 */     registerIncoming(3, ClientPlayerMovementPacket.class);
/* 387:380 */     registerIncoming(4, ClientPlayerPositionPacket.class);
/* 388:381 */     registerIncoming(5, ClientPlayerRotationPacket.class);
/* 389:382 */     registerIncoming(6, ClientPlayerPositionRotationPacket.class);
/* 390:383 */     registerIncoming(7, ClientPlayerDigPacket.class);
/* 391:384 */     registerIncoming(8, ClientPlayerPlaceBlockPacket.class);
/* 392:385 */     registerIncoming(9, ClientChangeHeldItemPacket.class);
/* 393:386 */     registerIncoming(10, ClientPlayerAnimationPacket.class);
/* 394:387 */     registerIncoming(11, ClientPlayerActionPacket.class);
/* 395:388 */     registerIncoming(12, ClientSteerVehiclePacket.class);
/* 396:389 */     registerIncoming(13, ClientCloseWindowPacket.class);
/* 397:390 */     registerIncoming(14, ClientWindowActionPacket.class);
/* 398:391 */     registerIncoming(15, ClientConfirmTransactionPacket.class);
/* 399:392 */     registerIncoming(16, ClientCreativeInventoryActionPacket.class);
/* 400:393 */     registerIncoming(17, ClientEnchantItemPacket.class);
/* 401:394 */     registerIncoming(18, ClientUpdateSignPacket.class);
/* 402:395 */     registerIncoming(19, ClientPlayerAbilitiesPacket.class);
/* 403:396 */     registerIncoming(20, ClientTabCompletePacket.class);
/* 404:397 */     registerIncoming(21, ClientSettingsPacket.class);
/* 405:398 */     registerIncoming(22, ClientRequestPacket.class);
/* 406:399 */     registerIncoming(23, ClientPluginMessagePacket.class);
/* 407:    */     
/* 408:401 */     registerOutgoing(0, ServerKeepAlivePacket.class);
/* 409:402 */     registerOutgoing(1, ServerJoinGamePacket.class);
/* 410:403 */     registerOutgoing(2, ServerChatPacket.class);
/* 411:404 */     registerOutgoing(3, ServerUpdateTimePacket.class);
/* 412:405 */     registerOutgoing(4, ServerEntityEquipmentPacket.class);
/* 413:406 */     registerOutgoing(5, ServerSpawnPositionPacket.class);
/* 414:407 */     registerOutgoing(6, ServerUpdateHealthPacket.class);
/* 415:408 */     registerOutgoing(7, ServerRespawnPacket.class);
/* 416:409 */     registerOutgoing(8, ServerPlayerPositionRotationPacket.class);
/* 417:410 */     registerOutgoing(9, ServerChangeHeldItemPacket.class);
/* 418:411 */     registerOutgoing(10, ServerPlayerUseBedPacket.class);
/* 419:412 */     registerOutgoing(11, ServerAnimationPacket.class);
/* 420:413 */     registerOutgoing(12, ServerSpawnPlayerPacket.class);
/* 421:414 */     registerOutgoing(13, ServerCollectItemPacket.class);
/* 422:415 */     registerOutgoing(14, ServerSpawnObjectPacket.class);
/* 423:416 */     registerOutgoing(15, ServerSpawnMobPacket.class);
/* 424:417 */     registerOutgoing(16, ServerSpawnPaintingPacket.class);
/* 425:418 */     registerOutgoing(17, ServerSpawnExpOrbPacket.class);
/* 426:419 */     registerOutgoing(18, ServerEntityVelocityPacket.class);
/* 427:420 */     registerOutgoing(19, ServerDestroyEntitiesPacket.class);
/* 428:421 */     registerOutgoing(20, ServerEntityMovementPacket.class);
/* 429:422 */     registerOutgoing(21, ServerEntityPositionPacket.class);
/* 430:423 */     registerOutgoing(22, ServerEntityRotationPacket.class);
/* 431:424 */     registerOutgoing(23, ServerEntityPositionRotationPacket.class);
/* 432:425 */     registerOutgoing(24, ServerEntityTeleportPacket.class);
/* 433:426 */     registerOutgoing(25, ServerEntityHeadLookPacket.class);
/* 434:427 */     registerOutgoing(26, ServerEntityStatusPacket.class);
/* 435:428 */     registerOutgoing(27, ServerEntityAttachPacket.class);
/* 436:429 */     registerOutgoing(28, ServerEntityMetadataPacket.class);
/* 437:430 */     registerOutgoing(29, ServerEntityEffectPacket.class);
/* 438:431 */     registerOutgoing(30, ServerEntityRemoveEffectPacket.class);
/* 439:432 */     registerOutgoing(31, ServerSetExperiencePacket.class);
/* 440:433 */     registerOutgoing(32, ServerEntityPropertiesPacket.class);
/* 441:434 */     registerOutgoing(33, ServerChunkDataPacket.class);
/* 442:435 */     registerOutgoing(34, ServerMultiBlockChangePacket.class);
/* 443:436 */     registerOutgoing(35, ServerBlockChangePacket.class);
/* 444:437 */     registerOutgoing(36, ServerBlockValuePacket.class);
/* 445:438 */     registerOutgoing(37, ServerBlockBreakAnimPacket.class);
/* 446:439 */     registerOutgoing(38, ServerMultiChunkDataPacket.class);
/* 447:440 */     registerOutgoing(39, ServerExplosionPacket.class);
/* 448:441 */     registerOutgoing(40, ServerPlayEffectPacket.class);
/* 449:442 */     registerOutgoing(41, ServerPlaySoundPacket.class);
/* 450:443 */     registerOutgoing(42, ServerSpawnParticlePacket.class);
/* 451:444 */     registerOutgoing(43, ServerNotifyClientPacket.class);
/* 452:445 */     registerOutgoing(44, ServerSpawnGlobalEntityPacket.class);
/* 453:446 */     registerOutgoing(45, ServerOpenWindowPacket.class);
/* 454:447 */     registerOutgoing(46, ServerCloseWindowPacket.class);
/* 455:448 */     registerOutgoing(47, ServerSetSlotPacket.class);
/* 456:449 */     registerOutgoing(48, ServerWindowItemsPacket.class);
/* 457:450 */     registerOutgoing(49, ServerWindowPropertyPacket.class);
/* 458:451 */     registerOutgoing(50, ServerConfirmTransactionPacket.class);
/* 459:452 */     registerOutgoing(51, ServerUpdateSignPacket.class);
/* 460:453 */     registerOutgoing(52, ServerMapDataPacket.class);
/* 461:454 */     registerOutgoing(53, ServerUpdateTileEntityPacket.class);
/* 462:455 */     registerOutgoing(54, ServerOpenTileEntityEditorPacket.class);
/* 463:456 */     registerOutgoing(55, ServerStatisticsPacket.class);
/* 464:457 */     registerOutgoing(56, ServerPlayerListEntryPacket.class);
/* 465:458 */     registerOutgoing(57, ServerPlayerAbilitiesPacket.class);
/* 466:459 */     registerOutgoing(58, ServerTabCompletePacket.class);
/* 467:460 */     registerOutgoing(59, ServerScoreboardObjectivePacket.class);
/* 468:461 */     registerOutgoing(60, ServerUpdateScorePacket.class);
/* 469:462 */     registerOutgoing(61, ServerDisplayScoreboardPacket.class);
/* 470:463 */     registerOutgoing(62, ServerTeamPacket.class);
/* 471:464 */     registerOutgoing(63, ServerPluginMessagePacket.class);
/* 472:465 */     registerOutgoing(64, ServerDisconnectPacket.class);
/* 473:    */   }
/* 474:    */   
/* 475:    */   private void initClientStatus(Session session)
/* 476:    */   {
/* 477:469 */     registerIncoming(0, StatusResponsePacket.class);
/* 478:470 */     registerIncoming(1, StatusPongPacket.class);
/* 479:    */     
/* 480:472 */     registerOutgoing(0, StatusQueryPacket.class);
/* 481:473 */     registerOutgoing(1, StatusPingPacket.class);
/* 482:    */   }
/* 483:    */   
/* 484:    */   private void initServerStatus(Session session)
/* 485:    */   {
/* 486:477 */     registerIncoming(0, StatusQueryPacket.class);
/* 487:478 */     registerIncoming(1, StatusPingPacket.class);
/* 488:    */     
/* 489:480 */     registerOutgoing(0, StatusResponsePacket.class);
/* 490:481 */     registerOutgoing(1, StatusPongPacket.class);
/* 491:    */   }
/* 492:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.MinecraftProtocol
 * JD-Core Version:    0.7.0.1
 */