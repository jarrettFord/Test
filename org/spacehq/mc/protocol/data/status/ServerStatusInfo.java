/*  1:   */ package org.spacehq.mc.protocol.data.status;
/*  2:   */ 
/*  3:   */ import java.awt.image.BufferedImage;
/*  4:   */ import org.spacehq.mc.protocol.data.message.Message;
/*  5:   */ 
/*  6:   */ public class ServerStatusInfo
/*  7:   */ {
/*  8:   */   private VersionInfo version;
/*  9:   */   private PlayerInfo players;
/* 10:   */   private Message description;
/* 11:   */   private BufferedImage icon;
/* 12:   */   
/* 13:   */   public ServerStatusInfo(VersionInfo version, PlayerInfo players, Message description, BufferedImage icon)
/* 14:   */   {
/* 15:15 */     this.version = version;
/* 16:16 */     this.players = players;
/* 17:17 */     this.description = description;
/* 18:18 */     this.icon = icon;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public VersionInfo getVersionInfo()
/* 22:   */   {
/* 23:22 */     return this.version;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public PlayerInfo getPlayerInfo()
/* 27:   */   {
/* 28:26 */     return this.players;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public Message getDescription()
/* 32:   */   {
/* 33:30 */     return this.description;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public BufferedImage getIcon()
/* 37:   */   {
/* 38:34 */     return this.icon;
/* 39:   */   }
/* 40:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.status.ServerStatusInfo
 * JD-Core Version:    0.7.0.1
 */