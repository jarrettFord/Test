/*  1:   */ package org.spacehq.mc.protocol.data.status;
/*  2:   */ 
/*  3:   */ import org.spacehq.mc.auth.GameProfile;
/*  4:   */ 
/*  5:   */ public class PlayerInfo
/*  6:   */ {
/*  7:   */   private int max;
/*  8:   */   private int online;
/*  9:   */   private GameProfile[] players;
/* 10:   */   
/* 11:   */   public PlayerInfo(int max, int online, GameProfile[] players)
/* 12:   */   {
/* 13:12 */     this.max = max;
/* 14:13 */     this.online = online;
/* 15:14 */     this.players = players;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public int getMaxPlayers()
/* 19:   */   {
/* 20:18 */     return this.max;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public int getOnlinePlayers()
/* 24:   */   {
/* 25:22 */     return this.online;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public GameProfile[] getPlayers()
/* 29:   */   {
/* 30:26 */     return this.players;
/* 31:   */   }
/* 32:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.status.PlayerInfo
 * JD-Core Version:    0.7.0.1
 */