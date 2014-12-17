/*   1:    */ package com.google.gson.internal.bind;
/*   2:    */ 
/*   3:    */ import com.google.gson.Gson;
/*   4:    */ import com.google.gson.TypeAdapter;
/*   5:    */ import com.google.gson.TypeAdapterFactory;
/*   6:    */ import com.google.gson.internal.LinkedTreeMap;
/*   7:    */ import com.google.gson.reflect.TypeToken;
/*   8:    */ import com.google.gson.stream.JsonReader;
/*   9:    */ import com.google.gson.stream.JsonToken;
/*  10:    */ import com.google.gson.stream.JsonWriter;
/*  11:    */ import java.io.IOException;
/*  12:    */ import java.util.ArrayList;
/*  13:    */ import java.util.List;
/*  14:    */ import java.util.Map;
/*  15:    */ 
/*  16:    */ public final class ObjectTypeAdapter
/*  17:    */   extends TypeAdapter<Object>
/*  18:    */ {
/*  19: 38 */   public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory()
/*  20:    */   {
/*  21:    */     public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type)
/*  22:    */     {
/*  23: 41 */       if (type.getRawType() == Object.class) {
/*  24: 42 */         return new ObjectTypeAdapter(gson, null);
/*  25:    */       }
/*  26: 44 */       return null;
/*  27:    */     }
/*  28:    */   };
/*  29:    */   private final Gson gson;
/*  30:    */   
/*  31:    */   private ObjectTypeAdapter(Gson gson)
/*  32:    */   {
/*  33: 51 */     this.gson = gson;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public Object read(JsonReader in)
/*  37:    */     throws IOException
/*  38:    */   {
/*  39: 55 */     JsonToken token = in.peek();
/*  40: 56 */     switch (2.$SwitchMap$com$google$gson$stream$JsonToken[token.ordinal()])
/*  41:    */     {
/*  42:    */     case 1: 
/*  43: 58 */       List<Object> list = new ArrayList();
/*  44: 59 */       in.beginArray();
/*  45: 60 */       while (in.hasNext()) {
/*  46: 61 */         list.add(read(in));
/*  47:    */       }
/*  48: 63 */       in.endArray();
/*  49: 64 */       return list;
/*  50:    */     case 2: 
/*  51: 67 */       Map<String, Object> map = new LinkedTreeMap();
/*  52: 68 */       in.beginObject();
/*  53: 69 */       while (in.hasNext()) {
/*  54: 70 */         map.put(in.nextName(), read(in));
/*  55:    */       }
/*  56: 72 */       in.endObject();
/*  57: 73 */       return map;
/*  58:    */     case 3: 
/*  59: 76 */       return in.nextString();
/*  60:    */     case 4: 
/*  61: 79 */       return Double.valueOf(in.nextDouble());
/*  62:    */     case 5: 
/*  63: 82 */       return Boolean.valueOf(in.nextBoolean());
/*  64:    */     case 6: 
/*  65: 85 */       in.nextNull();
/*  66: 86 */       return null;
/*  67:    */     }
/*  68: 89 */     throw new IllegalStateException();
/*  69:    */   }
/*  70:    */   
/*  71:    */   public void write(JsonWriter out, Object value)
/*  72:    */     throws IOException
/*  73:    */   {
/*  74: 95 */     if (value == null)
/*  75:    */     {
/*  76: 96 */       out.nullValue();
/*  77: 97 */       return;
/*  78:    */     }
/*  79:100 */     TypeAdapter<Object> typeAdapter = this.gson.getAdapter(value.getClass());
/*  80:101 */     if ((typeAdapter instanceof ObjectTypeAdapter))
/*  81:    */     {
/*  82:102 */       out.beginObject();
/*  83:103 */       out.endObject();
/*  84:104 */       return;
/*  85:    */     }
/*  86:107 */     typeAdapter.write(out, value);
/*  87:    */   }
/*  88:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.bind.ObjectTypeAdapter
 * JD-Core Version:    0.7.0.1
 */