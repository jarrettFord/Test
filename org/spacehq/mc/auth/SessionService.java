/*   1:    */ package org.spacehq.mc.auth;
/*   2:    */ 
/*   3:    */ import com.google.gson.Gson;
/*   4:    */ import com.google.gson.GsonBuilder;
/*   5:    */ import java.net.URL;
/*   6:    */ import java.security.KeyFactory;
/*   7:    */ import java.security.PublicKey;
/*   8:    */ import java.security.spec.X509EncodedKeySpec;
/*   9:    */ import java.util.Calendar;
/*  10:    */ import java.util.Date;
/*  11:    */ import java.util.HashMap;
/*  12:    */ import java.util.Map;
/*  13:    */ import java.util.UUID;
/*  14:    */ import org.spacehq.mc.auth.exception.AuthenticationException;
/*  15:    */ import org.spacehq.mc.auth.exception.AuthenticationUnavailableException;
/*  16:    */ import org.spacehq.mc.auth.exception.ProfileException;
/*  17:    */ import org.spacehq.mc.auth.exception.ProfileLookupException;
/*  18:    */ import org.spacehq.mc.auth.exception.ProfileNotFoundException;
/*  19:    */ import org.spacehq.mc.auth.exception.ProfileTextureException;
/*  20:    */ import org.spacehq.mc.auth.exception.PropertyException;
/*  21:    */ import org.spacehq.mc.auth.properties.Property;
/*  22:    */ import org.spacehq.mc.auth.properties.PropertyMap;
/*  23:    */ import org.spacehq.mc.auth.request.JoinServerRequest;
/*  24:    */ import org.spacehq.mc.auth.response.HasJoinedResponse;
/*  25:    */ import org.spacehq.mc.auth.response.MinecraftProfilePropertiesResponse;
/*  26:    */ import org.spacehq.mc.auth.response.MinecraftTexturesPayload;
/*  27:    */ import org.spacehq.mc.auth.response.Response;
/*  28:    */ import org.spacehq.mc.auth.serialize.UUIDSerializer;
/*  29:    */ import org.spacehq.mc.auth.util.Base64;
/*  30:    */ import org.spacehq.mc.auth.util.IOUtils;
/*  31:    */ import org.spacehq.mc.auth.util.URLUtils;
/*  32:    */ 
/*  33:    */ public class SessionService
/*  34:    */ {
/*  35:    */   private static final String BASE_URL = "https://sessionserver.mojang.com/session/minecraft/";
/*  36: 39 */   private static final URL JOIN_URL = URLUtils.constantURL("https://sessionserver.mojang.com/session/minecraft/join");
/*  37: 40 */   private static final URL CHECK_URL = URLUtils.constantURL("https://sessionserver.mojang.com/session/minecraft/hasJoined");
/*  38:    */   private static final PublicKey SIGNATURE_KEY;
/*  39: 43 */   private static final Gson GSON = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDSerializer()).create();
/*  40:    */   
/*  41:    */   static
/*  42:    */   {
/*  43:    */     try
/*  44:    */     {
/*  45: 47 */       X509EncodedKeySpec spec = new X509EncodedKeySpec(IOUtils.toByteArray(SessionService.class.getResourceAsStream("/yggdrasil_session_pubkey.der")));
/*  46: 48 */       KeyFactory keyFactory = KeyFactory.getInstance("RSA");
/*  47: 49 */       SIGNATURE_KEY = keyFactory.generatePublic(spec);
/*  48:    */     }
/*  49:    */     catch (Exception e)
/*  50:    */     {
/*  51: 51 */       throw new ExceptionInInitializerError("Missing/invalid yggdrasil public key.");
/*  52:    */     }
/*  53:    */   }
/*  54:    */   
/*  55:    */   public void joinServer(GameProfile profile, String authenticationToken, String serverId)
/*  56:    */     throws AuthenticationException
/*  57:    */   {
/*  58: 56 */     JoinServerRequest request = new JoinServerRequest(authenticationToken, profile.getId(), serverId);
/*  59: 57 */     URLUtils.makeRequest(JOIN_URL, request, Response.class);
/*  60:    */   }
/*  61:    */   
/*  62:    */   public GameProfile hasJoinedServer(GameProfile user, String serverId)
/*  63:    */     throws AuthenticationUnavailableException
/*  64:    */   {
/*  65: 61 */     Map<String, Object> arguments = new HashMap();
/*  66: 62 */     arguments.put("username", user.getName());
/*  67: 63 */     arguments.put("serverId", serverId);
/*  68: 64 */     URL url = URLUtils.concatenateURL(CHECK_URL, URLUtils.buildQuery(arguments));
/*  69:    */     try
/*  70:    */     {
/*  71: 66 */       HasJoinedResponse response = (HasJoinedResponse)URLUtils.makeRequest(url, null, HasJoinedResponse.class);
/*  72: 67 */       if ((response != null) && (response.getId() != null))
/*  73:    */       {
/*  74: 68 */         GameProfile result = new GameProfile(response.getId(), user.getName());
/*  75: 69 */         if (response.getProperties() != null) {
/*  76: 70 */           result.getProperties().putAll(response.getProperties());
/*  77:    */         }
/*  78: 73 */         return result;
/*  79:    */       }
/*  80: 75 */       return null;
/*  81:    */     }
/*  82:    */     catch (AuthenticationUnavailableException e)
/*  83:    */     {
/*  84: 78 */       throw e;
/*  85:    */     }
/*  86:    */     catch (AuthenticationException e) {}
/*  87: 80 */     return null;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public Map<ProfileTextureType, ProfileTexture> getTextures(GameProfile profile, boolean requireSecure)
/*  91:    */     throws PropertyException
/*  92:    */   {
/*  93: 85 */     Property textures = (Property)profile.getProperties().get("textures");
/*  94: 86 */     if (textures != null)
/*  95:    */     {
/*  96: 87 */       if (!textures.hasSignature()) {
/*  97: 88 */         throw new ProfileTextureException("Signature is missing from textures payload.");
/*  98:    */       }
/*  99: 91 */       if (!textures.isSignatureValid(SIGNATURE_KEY)) {
/* 100: 92 */         throw new ProfileTextureException("Textures payload has been tampered with. (signature invalid)");
/* 101:    */       }
/* 102:    */       try
/* 103:    */       {
/* 104: 97 */         String json = new String(Base64.decode(textures.getValue().getBytes("UTF-8")));
/* 105: 98 */         result = (MinecraftTexturesPayload)GSON.fromJson(json, MinecraftTexturesPayload.class);
/* 106:    */       }
/* 107:    */       catch (Exception e)
/* 108:    */       {
/* 109:    */         MinecraftTexturesPayload result;
/* 110:100 */         throw new ProfileTextureException("Could not decode texture payload.", e);
/* 111:    */       }
/* 112:    */       MinecraftTexturesPayload result;
/* 113:103 */       if ((result.getProfileId() == null) || (!result.getProfileId().equals(profile.getId()))) {
/* 114:104 */         throw new ProfileTextureException("Decrypted textures payload was for another user. (expected id " + profile.getId() + " but was for " + result.getProfileId() + ")");
/* 115:    */       }
/* 116:107 */       if ((result.getProfileName() == null) || (!result.getProfileName().equals(profile.getName()))) {
/* 117:108 */         throw new ProfileTextureException("Decrypted textures payload was for another user. (expected name " + profile.getName() + " but was for " + result.getProfileName() + ")");
/* 118:    */       }
/* 119:110 */       if (requireSecure)
/* 120:    */       {
/* 121:111 */         if (result.isPublic()) {
/* 122:112 */           throw new ProfileTextureException("Decrypted textures payload was public when secure data is required.");
/* 123:    */         }
/* 124:115 */         Calendar limit = Calendar.getInstance();
/* 125:116 */         limit.add(5, -1);
/* 126:117 */         Date validFrom = new Date(result.getTimestamp());
/* 127:118 */         if (validFrom.before(limit.getTime())) {
/* 128:119 */           throw new ProfileTextureException("Decrypted textures payload is too old. (" + validFrom + ", needs to be at least " + limit + ")");
/* 129:    */         }
/* 130:    */       }
/* 131:123 */       return result.getTextures() == null ? new HashMap() : result.getTextures();
/* 132:    */     }
/* 133:126 */     return new HashMap();
/* 134:    */   }
/* 135:    */   
/* 136:    */   public GameProfile fillProfileProperties(GameProfile profile)
/* 137:    */     throws ProfileException
/* 138:    */   {
/* 139:130 */     if (profile.getId() == null) {
/* 140:131 */       return profile;
/* 141:    */     }
/* 142:    */     try
/* 143:    */     {
/* 144:135 */       URL url = URLUtils.constantURL("https://sessionserver.mojang.com/session/minecraft/profile/" + UUIDSerializer.fromUUID(profile.getId()));
/* 145:136 */       MinecraftProfilePropertiesResponse response = (MinecraftProfilePropertiesResponse)URLUtils.makeRequest(url, null, MinecraftProfilePropertiesResponse.class);
/* 146:137 */       if (response == null) {
/* 147:138 */         throw new ProfileNotFoundException("Couldn't fetch profile properties for " + profile + " as the profile does not exist.");
/* 148:    */       }
/* 149:141 */       GameProfile result = new GameProfile(response.getId(), response.getName());
/* 150:142 */       result.getProperties().putAll(response.getProperties());
/* 151:143 */       profile.getProperties().putAll(response.getProperties());
/* 152:144 */       return result;
/* 153:    */     }
/* 154:    */     catch (AuthenticationException e)
/* 155:    */     {
/* 156:146 */       throw new ProfileLookupException("Couldn't look up profile properties for " + profile, e);
/* 157:    */     }
/* 158:    */   }
/* 159:    */   
/* 160:    */   public String toString()
/* 161:    */   {
/* 162:152 */     return "SessionService{}";
/* 163:    */   }
/* 164:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.SessionService
 * JD-Core Version:    0.7.0.1
 */