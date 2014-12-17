/*   1:    */ package com.google.gson;
/*   2:    */ 
/*   3:    */ import com.google.gson.internal..Gson.Preconditions;
/*   4:    */ import com.google.gson.internal.Streams;
/*   5:    */ import com.google.gson.reflect.TypeToken;
/*   6:    */ import com.google.gson.stream.JsonReader;
/*   7:    */ import com.google.gson.stream.JsonWriter;
/*   8:    */ import java.io.IOException;
/*   9:    */ 
/*  10:    */ final class TreeTypeAdapter<T>
/*  11:    */   extends TypeAdapter<T>
/*  12:    */ {
/*  13:    */   private final JsonSerializer<T> serializer;
/*  14:    */   private final JsonDeserializer<T> deserializer;
/*  15:    */   private final Gson gson;
/*  16:    */   private final TypeToken<T> typeToken;
/*  17:    */   private final TypeAdapterFactory skipPast;
/*  18:    */   private TypeAdapter<T> delegate;
/*  19:    */   
/*  20:    */   private TreeTypeAdapter(JsonSerializer<T> serializer, JsonDeserializer<T> deserializer, Gson gson, TypeToken<T> typeToken, TypeAdapterFactory skipPast)
/*  21:    */   {
/*  22: 43 */     this.serializer = serializer;
/*  23: 44 */     this.deserializer = deserializer;
/*  24: 45 */     this.gson = gson;
/*  25: 46 */     this.typeToken = typeToken;
/*  26: 47 */     this.skipPast = skipPast;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public T read(JsonReader in)
/*  30:    */     throws IOException
/*  31:    */   {
/*  32: 51 */     if (this.deserializer == null) {
/*  33: 52 */       return delegate().read(in);
/*  34:    */     }
/*  35: 54 */     JsonElement value = Streams.parse(in);
/*  36: 55 */     if (value.isJsonNull()) {
/*  37: 56 */       return null;
/*  38:    */     }
/*  39: 58 */     return this.deserializer.deserialize(value, this.typeToken.getType(), this.gson.deserializationContext);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public void write(JsonWriter out, T value)
/*  43:    */     throws IOException
/*  44:    */   {
/*  45: 62 */     if (this.serializer == null)
/*  46:    */     {
/*  47: 63 */       delegate().write(out, value);
/*  48: 64 */       return;
/*  49:    */     }
/*  50: 66 */     if (value == null)
/*  51:    */     {
/*  52: 67 */       out.nullValue();
/*  53: 68 */       return;
/*  54:    */     }
/*  55: 70 */     JsonElement tree = this.serializer.serialize(value, this.typeToken.getType(), this.gson.serializationContext);
/*  56: 71 */     Streams.write(tree, out);
/*  57:    */   }
/*  58:    */   
/*  59:    */   private TypeAdapter<T> delegate()
/*  60:    */   {
/*  61: 75 */     TypeAdapter<T> d = this.delegate;
/*  62: 76 */     return this.delegate = this.gson.getDelegateAdapter(this.skipPast, this.typeToken);
/*  63:    */   }
/*  64:    */   
/*  65:    */   public static TypeAdapterFactory newFactory(TypeToken<?> exactType, Object typeAdapter)
/*  66:    */   {
/*  67: 85 */     return new SingleTypeFactory(typeAdapter, exactType, false, null, null);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public static TypeAdapterFactory newFactoryWithMatchRawType(TypeToken<?> exactType, Object typeAdapter)
/*  71:    */   {
/*  72: 95 */     boolean matchRawType = exactType.getType() == exactType.getRawType();
/*  73: 96 */     return new SingleTypeFactory(typeAdapter, exactType, matchRawType, null, null);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public static TypeAdapterFactory newTypeHierarchyFactory(Class<?> hierarchyType, Object typeAdapter)
/*  77:    */   {
/*  78:105 */     return new SingleTypeFactory(typeAdapter, null, false, hierarchyType, null);
/*  79:    */   }
/*  80:    */   
/*  81:    */   private static class SingleTypeFactory
/*  82:    */     implements TypeAdapterFactory
/*  83:    */   {
/*  84:    */     private final TypeToken<?> exactType;
/*  85:    */     private final boolean matchRawType;
/*  86:    */     private final Class<?> hierarchyType;
/*  87:    */     private final JsonSerializer<?> serializer;
/*  88:    */     private final JsonDeserializer<?> deserializer;
/*  89:    */     
/*  90:    */     private SingleTypeFactory(Object typeAdapter, TypeToken<?> exactType, boolean matchRawType, Class<?> hierarchyType)
/*  91:    */     {
/*  92:117 */       this.serializer = ((typeAdapter instanceof JsonSerializer) ? (JsonSerializer)typeAdapter : null);
/*  93:    */       
/*  94:    */ 
/*  95:120 */       this.deserializer = ((typeAdapter instanceof JsonDeserializer) ? (JsonDeserializer)typeAdapter : null);
/*  96:    */       
/*  97:    */ 
/*  98:123 */       .Gson.Preconditions.checkArgument((this.serializer != null) || (this.deserializer != null));
/*  99:124 */       this.exactType = exactType;
/* 100:125 */       this.matchRawType = matchRawType;
/* 101:126 */       this.hierarchyType = hierarchyType;
/* 102:    */     }
/* 103:    */     
/* 104:    */     public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type)
/* 105:    */     {
/* 106:131 */       boolean matches = this.exactType != null ? false : (this.exactType.equals(type)) || ((this.matchRawType) && (this.exactType.getType() == type.getRawType())) ? true : this.hierarchyType.isAssignableFrom(type.getRawType());
/* 107:    */       
/* 108:    */ 
/* 109:134 */       return matches ? new TreeTypeAdapter(this.serializer, this.deserializer, gson, type, this, null) : null;
/* 110:    */     }
/* 111:    */   }
/* 112:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.TreeTypeAdapter
 * JD-Core Version:    0.7.0.1
 */