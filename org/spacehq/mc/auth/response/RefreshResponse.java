/*  1:   */ package org.spacehq.mc.auth.response;
/*  2:   */ 
/*  3:   */ import org.spacehq.mc.auth.GameProfile;
/*  4:   */ 
/*  5:   */ public class RefreshResponse
/*  6:   */   extends Response
/*  7:   */ {
/*  8:   */   private String accessToken;
/*  9:   */   private String clientToken;
/* 10:   */   private GameProfile selectedProfile;
/* 11:   */   private GameProfile[] availableProfiles;
/* 12:   */   private User user;
/* 13:   */   
/* 14:   */   public String getAccessToken()
/* 15:   */   {
/* 16:14 */     return this.accessToken;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public String getClientToken()
/* 20:   */   {
/* 21:18 */     return this.clientToken;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public GameProfile[] getAvailableProfiles()
/* 25:   */   {
/* 26:22 */     return this.availableProfiles;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public GameProfile getSelectedProfile()
/* 30:   */   {
/* 31:26 */     return this.selectedProfile;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public User getUser()
/* 35:   */   {
/* 36:30 */     return this.user;
/* 37:   */   }
/* 38:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.response.RefreshResponse
 * JD-Core Version:    0.7.0.1
 */