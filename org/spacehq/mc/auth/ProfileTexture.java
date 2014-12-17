/*  1:   */ package org.spacehq.mc.auth;
/*  2:   */ 
/*  3:   */ public class ProfileTexture
/*  4:   */ {
/*  5:   */   private String url;
/*  6:   */   
/*  7:   */   public ProfileTexture(String url)
/*  8:   */   {
/*  9: 8 */     this.url = url;
/* 10:   */   }
/* 11:   */   
/* 12:   */   public String getUrl()
/* 13:   */   {
/* 14:12 */     return this.url;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public String getHash()
/* 18:   */   {
/* 19:16 */     String url = this.url.endsWith("/") ? this.url.substring(0, this.url.length() - 1) : this.url;
/* 20:17 */     int slash = url.lastIndexOf("/");
/* 21:18 */     int dot = url.lastIndexOf(".", slash);
/* 22:19 */     return url.substring(slash + 1, dot != -1 ? dot : url.length());
/* 23:   */   }
/* 24:   */   
/* 25:   */   public String toString()
/* 26:   */   {
/* 27:24 */     return "ProfileTexture{url=" + this.url + ", hash=" + getHash() + "}";
/* 28:   */   }
/* 29:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.ProfileTexture
 * JD-Core Version:    0.7.0.1
 */