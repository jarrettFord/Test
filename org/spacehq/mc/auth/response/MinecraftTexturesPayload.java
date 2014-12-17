/*  1:   */ package org.spacehq.mc.auth.response;
/*  2:   */ 
/*  3:   */ import java.util.Map;
/*  4:   */ import java.util.UUID;
/*  5:   */ import org.spacehq.mc.auth.ProfileTexture;
/*  6:   */ import org.spacehq.mc.auth.ProfileTextureType;
/*  7:   */ 
/*  8:   */ public class MinecraftTexturesPayload
/*  9:   */ {
/* 10:   */   private long timestamp;
/* 11:   */   private UUID profileId;
/* 12:   */   private String profileName;
/* 13:   */   private boolean isPublic;
/* 14:   */   private Map<ProfileTextureType, ProfileTexture> textures;
/* 15:   */   
/* 16:   */   public long getTimestamp()
/* 17:   */   {
/* 18:18 */     return this.timestamp;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public UUID getProfileId()
/* 22:   */   {
/* 23:22 */     return this.profileId;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public String getProfileName()
/* 27:   */   {
/* 28:26 */     return this.profileName;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public boolean isPublic()
/* 32:   */   {
/* 33:30 */     return this.isPublic;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public Map<ProfileTextureType, ProfileTexture> getTextures()
/* 37:   */   {
/* 38:34 */     return this.textures;
/* 39:   */   }
/* 40:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.response.MinecraftTexturesPayload
 * JD-Core Version:    0.7.0.1
 */