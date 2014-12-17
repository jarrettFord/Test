/*  1:   */ package org.spacehq.mc.auth.response;
/*  2:   */ 
/*  3:   */ import org.spacehq.mc.auth.GameProfile;
/*  4:   */ 
/*  5:   */ public class ProfileSearchResultsResponse
/*  6:   */   extends Response
/*  7:   */ {
/*  8:   */   private GameProfile[] profiles;
/*  9:   */   
/* 10:   */   public GameProfile[] getProfiles()
/* 11:   */   {
/* 12:10 */     return this.profiles;
/* 13:   */   }
/* 14:   */   
/* 15:   */   public void setProfiles(GameProfile[] profiles)
/* 16:   */   {
/* 17:14 */     this.profiles = profiles;
/* 18:   */   }
/* 19:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.response.ProfileSearchResultsResponse
 * JD-Core Version:    0.7.0.1
 */