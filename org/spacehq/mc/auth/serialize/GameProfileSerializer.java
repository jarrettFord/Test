/*  1:   */ package org.spacehq.mc.auth.serialize;
/*  2:   */ 
/*  3:   */ import com.google.gson.JsonDeserializationContext;
/*  4:   */ import com.google.gson.JsonDeserializer;
/*  5:   */ import com.google.gson.JsonElement;
/*  6:   */ import com.google.gson.JsonObject;
/*  7:   */ import com.google.gson.JsonParseException;
/*  8:   */ import com.google.gson.JsonPrimitive;
/*  9:   */ import com.google.gson.JsonSerializationContext;
/* 10:   */ import com.google.gson.JsonSerializer;
/* 11:   */ import java.lang.reflect.Type;
/* 12:   */ import java.util.UUID;
/* 13:   */ import org.spacehq.mc.auth.GameProfile;
/* 14:   */ 
/* 15:   */ public class GameProfileSerializer
/* 16:   */   implements JsonSerializer<GameProfile>, JsonDeserializer<GameProfile>
/* 17:   */ {
/* 18:   */   public GameProfile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
/* 19:   */     throws JsonParseException
/* 20:   */   {
/* 21:13 */     JsonObject object = (JsonObject)json;
/* 22:14 */     UUID id = object.has("id") ? (UUID)context.deserialize(object.get("id"), UUID.class) : null;
/* 23:15 */     String name = object.has("name") ? object.getAsJsonPrimitive("name").getAsString() : null;
/* 24:16 */     return new GameProfile(id, name);
/* 25:   */   }
/* 26:   */   
/* 27:   */   public JsonElement serialize(GameProfile src, Type typeOfSrc, JsonSerializationContext context)
/* 28:   */   {
/* 29:21 */     JsonObject result = new JsonObject();
/* 30:22 */     if (src.getId() != null) {
/* 31:23 */       result.add("id", context.serialize(src.getId()));
/* 32:   */     }
/* 33:26 */     if (src.getName() != null) {
/* 34:27 */       result.addProperty("name", src.getName());
/* 35:   */     }
/* 36:30 */     return result;
/* 37:   */   }
/* 38:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.serialize.GameProfileSerializer
 * JD-Core Version:    0.7.0.1
 */