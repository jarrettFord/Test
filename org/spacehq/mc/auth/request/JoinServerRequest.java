/*  1:   */ package org.spacehq.mc.auth.request;
/*  2:   */ 
/*  3:   */ import java.util.UUID;
/*  4:   */ 
/*  5:   */ public class JoinServerRequest
/*  6:   */ {
/*  7:   */   private String accessToken;
/*  8:   */   private UUID selectedProfile;
/*  9:   */   private String serverId;
/* 10:   */   
/* 11:   */   public JoinServerRequest(String accessToken, UUID id, String serverId)
/* 12:   */   {
/* 13:15 */     this.accessToken = accessToken;
/* 14:16 */     this.selectedProfile = id;
/* 15:17 */     this.serverId = serverId;
/* 16:   */   }
/* 17:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.request.JoinServerRequest
 * JD-Core Version:    0.7.0.1
 */