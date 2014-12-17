/*  1:   */ package org.spacehq.mc.auth.request;
/*  2:   */ 
/*  3:   */ import org.spacehq.mc.auth.GameProfile;
/*  4:   */ import org.spacehq.mc.auth.UserAuthentication;
/*  5:   */ 
/*  6:   */ public class RefreshRequest
/*  7:   */ {
/*  8:   */   private String clientToken;
/*  9:   */   private String accessToken;
/* 10:   */   private GameProfile selectedProfile;
/* 11:   */   private boolean requestUser;
/* 12:   */   
/* 13:   */   public RefreshRequest(UserAuthentication authService)
/* 14:   */   {
/* 15:15 */     this(authService, null);
/* 16:   */   }
/* 17:   */   
/* 18:   */   public RefreshRequest(UserAuthentication authService, GameProfile profile)
/* 19:   */   {
/* 20:19 */     this.requestUser = true;
/* 21:20 */     this.clientToken = authService.getClientToken();
/* 22:21 */     this.accessToken = authService.getAccessToken();
/* 23:22 */     this.selectedProfile = profile;
/* 24:   */   }
/* 25:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.request.RefreshRequest
 * JD-Core Version:    0.7.0.1
 */