/*  1:   */ package org.spacehq.mc.auth.serialize;
/*  2:   */ 
/*  3:   */ import com.google.gson.JsonDeserializationContext;
/*  4:   */ import com.google.gson.JsonDeserializer;
/*  5:   */ import com.google.gson.JsonElement;
/*  6:   */ import com.google.gson.JsonObject;
/*  7:   */ import com.google.gson.JsonParseException;
/*  8:   */ import com.google.gson.JsonPrimitive;
/*  9:   */ import java.lang.reflect.Type;
/* 10:   */ import org.spacehq.mc.auth.GameProfile;
/* 11:   */ import org.spacehq.mc.auth.response.ProfileSearchResultsResponse;
/* 12:   */ 
/* 13:   */ public class ProfileSearchResultsSerializer
/* 14:   */   implements JsonDeserializer<ProfileSearchResultsResponse>
/* 15:   */ {
/* 16:   */   public ProfileSearchResultsResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
/* 17:   */     throws JsonParseException
/* 18:   */   {
/* 19:15 */     ProfileSearchResultsResponse result = new ProfileSearchResultsResponse();
/* 20:16 */     if ((json instanceof JsonObject))
/* 21:   */     {
/* 22:17 */       JsonObject object = (JsonObject)json;
/* 23:18 */       if (object.has("error")) {
/* 24:19 */         result.setError(object.getAsJsonPrimitive("error").getAsString());
/* 25:   */       }
/* 26:22 */       if (object.has("errorMessage")) {
/* 27:23 */         result.setError(object.getAsJsonPrimitive("errorMessage").getAsString());
/* 28:   */       }
/* 29:26 */       if (object.has("cause")) {
/* 30:27 */         result.setError(object.getAsJsonPrimitive("cause").getAsString());
/* 31:   */       }
/* 32:   */     }
/* 33:   */     else
/* 34:   */     {
/* 35:30 */       result.setProfiles((GameProfile[])context.deserialize(json, [Lorg.spacehq.mc.auth.GameProfile.class));
/* 36:   */     }
/* 37:33 */     return result;
/* 38:   */   }
/* 39:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.serialize.ProfileSearchResultsSerializer
 * JD-Core Version:    0.7.0.1
 */