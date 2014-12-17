/*   1:    */ package com.google.gson;
/*   2:    */ 
/*   3:    */ import com.google.gson.internal.LinkedTreeMap;
/*   4:    */ import java.util.Map.Entry;
/*   5:    */ import java.util.Set;
/*   6:    */ 
/*   7:    */ public final class JsonObject
/*   8:    */   extends JsonElement
/*   9:    */ {
/*  10: 33 */   private final LinkedTreeMap<String, JsonElement> members = new LinkedTreeMap();
/*  11:    */   
/*  12:    */   JsonObject deepCopy()
/*  13:    */   {
/*  14: 38 */     JsonObject result = new JsonObject();
/*  15: 39 */     for (Map.Entry<String, JsonElement> entry : this.members.entrySet()) {
/*  16: 40 */       result.add((String)entry.getKey(), ((JsonElement)entry.getValue()).deepCopy());
/*  17:    */     }
/*  18: 42 */     return result;
/*  19:    */   }
/*  20:    */   
/*  21:    */   public void add(String property, JsonElement value)
/*  22:    */   {
/*  23: 54 */     if (value == null) {
/*  24: 55 */       value = JsonNull.INSTANCE;
/*  25:    */     }
/*  26: 57 */     this.members.put(property, value);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public JsonElement remove(String property)
/*  30:    */   {
/*  31: 68 */     return (JsonElement)this.members.remove(property);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public void addProperty(String property, String value)
/*  35:    */   {
/*  36: 79 */     add(property, createJsonElement(value));
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void addProperty(String property, Number value)
/*  40:    */   {
/*  41: 90 */     add(property, createJsonElement(value));
/*  42:    */   }
/*  43:    */   
/*  44:    */   public void addProperty(String property, Boolean value)
/*  45:    */   {
/*  46:101 */     add(property, createJsonElement(value));
/*  47:    */   }
/*  48:    */   
/*  49:    */   public void addProperty(String property, Character value)
/*  50:    */   {
/*  51:112 */     add(property, createJsonElement(value));
/*  52:    */   }
/*  53:    */   
/*  54:    */   private JsonElement createJsonElement(Object value)
/*  55:    */   {
/*  56:122 */     return value == null ? JsonNull.INSTANCE : new JsonPrimitive(value);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public Set<Map.Entry<String, JsonElement>> entrySet()
/*  60:    */   {
/*  61:132 */     return this.members.entrySet();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public boolean has(String memberName)
/*  65:    */   {
/*  66:142 */     return this.members.containsKey(memberName);
/*  67:    */   }
/*  68:    */   
/*  69:    */   public JsonElement get(String memberName)
/*  70:    */   {
/*  71:152 */     return (JsonElement)this.members.get(memberName);
/*  72:    */   }
/*  73:    */   
/*  74:    */   public JsonPrimitive getAsJsonPrimitive(String memberName)
/*  75:    */   {
/*  76:162 */     return (JsonPrimitive)this.members.get(memberName);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public JsonArray getAsJsonArray(String memberName)
/*  80:    */   {
/*  81:172 */     return (JsonArray)this.members.get(memberName);
/*  82:    */   }
/*  83:    */   
/*  84:    */   public JsonObject getAsJsonObject(String memberName)
/*  85:    */   {
/*  86:182 */     return (JsonObject)this.members.get(memberName);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public boolean equals(Object o)
/*  90:    */   {
/*  91:187 */     return (o == this) || (((o instanceof JsonObject)) && (((JsonObject)o).members.equals(this.members)));
/*  92:    */   }
/*  93:    */   
/*  94:    */   public int hashCode()
/*  95:    */   {
/*  96:193 */     return this.members.hashCode();
/*  97:    */   }
/*  98:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.JsonObject
 * JD-Core Version:    0.7.0.1
 */