/*  1:   */ package com.google.gson.internal.bind;
/*  2:   */ 
/*  3:   */ import com.google.gson.Gson;
/*  4:   */ import com.google.gson.TypeAdapter;
/*  5:   */ import com.google.gson.TypeAdapterFactory;
/*  6:   */ import com.google.gson.internal..Gson.Types;
/*  7:   */ import com.google.gson.reflect.TypeToken;
/*  8:   */ import com.google.gson.stream.JsonReader;
/*  9:   */ import com.google.gson.stream.JsonToken;
/* 10:   */ import com.google.gson.stream.JsonWriter;
/* 11:   */ import java.io.IOException;
/* 12:   */ import java.lang.reflect.Array;
/* 13:   */ import java.lang.reflect.GenericArrayType;
/* 14:   */ import java.lang.reflect.Type;
/* 15:   */ import java.util.ArrayList;
/* 16:   */ import java.util.List;
/* 17:   */ 
/* 18:   */ public final class ArrayTypeAdapter<E>
/* 19:   */   extends TypeAdapter<Object>
/* 20:   */ {
/* 21:39 */   public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory()
/* 22:   */   {
/* 23:   */     public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
/* 24:   */     {
/* 25:42 */       Type type = typeToken.getType();
/* 26:43 */       if ((!(type instanceof GenericArrayType)) && ((!(type instanceof Class)) || (!((Class)type).isArray()))) {
/* 27:44 */         return null;
/* 28:   */       }
/* 29:47 */       Type componentType = .Gson.Types.getArrayComponentType(type);
/* 30:48 */       TypeAdapter<?> componentTypeAdapter = gson.getAdapter(TypeToken.get(componentType));
/* 31:49 */       return new ArrayTypeAdapter(gson, componentTypeAdapter, .Gson.Types.getRawType(componentType));
/* 32:   */     }
/* 33:   */   };
/* 34:   */   private final Class<E> componentType;
/* 35:   */   private final TypeAdapter<E> componentTypeAdapter;
/* 36:   */   
/* 37:   */   public ArrayTypeAdapter(Gson context, TypeAdapter<E> componentTypeAdapter, Class<E> componentType)
/* 38:   */   {
/* 39:58 */     this.componentTypeAdapter = new TypeAdapterRuntimeTypeWrapper(context, componentTypeAdapter, componentType);
/* 40:   */     
/* 41:60 */     this.componentType = componentType;
/* 42:   */   }
/* 43:   */   
/* 44:   */   public Object read(JsonReader in)
/* 45:   */     throws IOException
/* 46:   */   {
/* 47:64 */     if (in.peek() == JsonToken.NULL)
/* 48:   */     {
/* 49:65 */       in.nextNull();
/* 50:66 */       return null;
/* 51:   */     }
/* 52:69 */     List<E> list = new ArrayList();
/* 53:70 */     in.beginArray();
/* 54:71 */     while (in.hasNext())
/* 55:   */     {
/* 56:72 */       E instance = this.componentTypeAdapter.read(in);
/* 57:73 */       list.add(instance);
/* 58:   */     }
/* 59:75 */     in.endArray();
/* 60:76 */     Object array = Array.newInstance(this.componentType, list.size());
/* 61:77 */     for (int i = 0; i < list.size(); i++) {
/* 62:78 */       Array.set(array, i, list.get(i));
/* 63:   */     }
/* 64:80 */     return array;
/* 65:   */   }
/* 66:   */   
/* 67:   */   public void write(JsonWriter out, Object array)
/* 68:   */     throws IOException
/* 69:   */   {
/* 70:85 */     if (array == null)
/* 71:   */     {
/* 72:86 */       out.nullValue();
/* 73:87 */       return;
/* 74:   */     }
/* 75:90 */     out.beginArray();
/* 76:91 */     int i = 0;
/* 77:91 */     for (int length = Array.getLength(array); i < length; i++)
/* 78:   */     {
/* 79:92 */       E value = Array.get(array, i);
/* 80:93 */       this.componentTypeAdapter.write(out, value);
/* 81:   */     }
/* 82:95 */     out.endArray();
/* 83:   */   }
/* 84:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.bind.ArrayTypeAdapter
 * JD-Core Version:    0.7.0.1
 */