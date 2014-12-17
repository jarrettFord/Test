/*  1:   */ package org.spacehq.mc.auth.serialize;
/*  2:   */ 
/*  3:   */ import com.google.gson.JsonArray;
/*  4:   */ import com.google.gson.JsonDeserializationContext;
/*  5:   */ import com.google.gson.JsonDeserializer;
/*  6:   */ import com.google.gson.JsonElement;
/*  7:   */ import com.google.gson.JsonObject;
/*  8:   */ import com.google.gson.JsonParseException;
/*  9:   */ import com.google.gson.JsonPrimitive;
/* 10:   */ import com.google.gson.JsonSerializationContext;
/* 11:   */ import com.google.gson.JsonSerializer;
/* 12:   */ import java.lang.reflect.Type;
/* 13:   */ import java.util.Iterator;
/* 14:   */ import java.util.Map.Entry;
/* 15:   */ import java.util.Set;
/* 16:   */ import org.spacehq.mc.auth.properties.Property;
/* 17:   */ import org.spacehq.mc.auth.properties.PropertyMap;
/* 18:   */ 
/* 19:   */ public class PropertyMapSerializer
/* 20:   */   implements JsonSerializer<PropertyMap>, JsonDeserializer<PropertyMap>
/* 21:   */ {
/* 22:   */   public PropertyMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
/* 23:   */     throws JsonParseException
/* 24:   */   {
/* 25:14 */     PropertyMap result = new PropertyMap();
/* 26:   */     Map.Entry<String, JsonElement> entry;
/* 27:15 */     if ((json instanceof JsonObject))
/* 28:   */     {
/* 29:16 */       JsonObject object = (JsonObject)json;
/* 30:17 */       for (Iterator localIterator1 = object.entrySet().iterator(); localIterator1.hasNext();)
/* 31:   */       {
/* 32:17 */         entry = (Map.Entry)localIterator1.next();
/* 33:18 */         if ((entry.getValue() instanceof JsonArray)) {
/* 34:19 */           for (JsonElement element : (JsonArray)entry.getValue()) {
/* 35:20 */             result.put((String)entry.getKey(), new Property((String)entry.getKey(), element.getAsString()));
/* 36:   */           }
/* 37:   */         }
/* 38:   */       }
/* 39:   */     }
/* 40:24 */     else if ((json instanceof JsonArray))
/* 41:   */     {
/* 42:25 */       for (JsonElement element : (JsonArray)json) {
/* 43:26 */         if ((element instanceof JsonObject))
/* 44:   */         {
/* 45:27 */           JsonObject object = (JsonObject)element;
/* 46:28 */           String name = object.getAsJsonPrimitive("name").getAsString();
/* 47:29 */           String value = object.getAsJsonPrimitive("value").getAsString();
/* 48:30 */           if (object.has("signature")) {
/* 49:31 */             result.put(name, new Property(name, value, object.getAsJsonPrimitive("signature").getAsString()));
/* 50:   */           } else {
/* 51:33 */             result.put(name, new Property(name, value));
/* 52:   */           }
/* 53:   */         }
/* 54:   */       }
/* 55:   */     }
/* 56:39 */     return result;
/* 57:   */   }
/* 58:   */   
/* 59:   */   public JsonElement serialize(PropertyMap src, Type typeOfSrc, JsonSerializationContext context)
/* 60:   */   {
/* 61:44 */     JsonArray result = new JsonArray();
/* 62:45 */     for (Property property : src.values())
/* 63:   */     {
/* 64:46 */       JsonObject object = new JsonObject();
/* 65:47 */       object.addProperty("name", property.getName());
/* 66:48 */       object.addProperty("value", property.getValue());
/* 67:49 */       if (property.hasSignature()) {
/* 68:50 */         object.addProperty("signature", property.getSignature());
/* 69:   */       }
/* 70:53 */       result.add(object);
/* 71:   */     }
/* 72:56 */     return result;
/* 73:   */   }
/* 74:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.serialize.PropertyMapSerializer
 * JD-Core Version:    0.7.0.1
 */