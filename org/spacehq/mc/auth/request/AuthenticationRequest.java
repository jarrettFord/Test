/*  1:   */ package org.spacehq.mc.auth.request;
/*  2:   */ 
/*  3:   */ import org.spacehq.mc.auth.UserAuthentication;
/*  4:   */ 
/*  5:   */ public class AuthenticationRequest
/*  6:   */ {
/*  7:   */   private Agent agent;
/*  8:   */   private String username;
/*  9:   */   private String password;
/* 10:   */   private String clientToken;
/* 11:12 */   private boolean requestUser = true;
/* 12:   */   
/* 13:   */   public AuthenticationRequest(UserAuthentication auth, String username, String password)
/* 14:   */   {
/* 15:15 */     this.agent = new Agent("Minecraft", 1);
/* 16:16 */     this.username = username;
/* 17:17 */     this.clientToken = auth.getClientToken();
/* 18:18 */     this.password = password;
/* 19:   */   }
/* 20:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.request.AuthenticationRequest
 * JD-Core Version:    0.7.0.1
 */