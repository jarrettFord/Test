/*  1:   */ package com.google.gson.internal.bind;
/*  2:   */ 
/*  3:   */ import com.google.gson.Gson;
/*  4:   */ import com.google.gson.TypeAdapter;
/*  5:   */ import com.google.gson.reflect.TypeToken;
/*  6:   */ import com.google.gson.stream.JsonReader;
/*  7:   */ import com.google.gson.stream.JsonWriter;
/*  8:   */ import java.io.IOException;
/*  9:   */ import java.lang.reflect.Type;
/* 10:   */ import java.lang.reflect.TypeVariable;
/* 11:   */ 
/* 12:   */ final class TypeAdapterRuntimeTypeWrapper<T>
/* 13:   */   extends TypeAdapter<T>
/* 14:   */ {
/* 15:   */   private final Gson context;
/* 16:   */   private final TypeAdapter<T> delegate;
/* 17:   */   private final Type type;
/* 18:   */   
/* 19:   */   TypeAdapterRuntimeTypeWrapper(Gson context, TypeAdapter<T> delegate, Type type)
/* 20:   */   {
/* 21:33 */     this.context = context;
/* 22:34 */     this.delegate = delegate;
/* 23:35 */     this.type = type;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public T read(JsonReader in)
/* 27:   */     throws IOException
/* 28:   */   {
/* 29:40 */     return this.delegate.read(in);
/* 30:   */   }
/* 31:   */   
/* 32:   */   public void write(JsonWriter out, T value)
/* 33:   */     throws IOException
/* 34:   */   {
/* 35:52 */     TypeAdapter chosen = this.delegate;
/* 36:53 */     Type runtimeType = getRuntimeTypeIfMoreSpecific(this.type, value);
/* 37:54 */     if (runtimeType != this.type)
/* 38:   */     {
/* 39:55 */       TypeAdapter runtimeTypeAdapter = this.context.getAdapter(TypeToken.get(runtimeType));
/* 40:56 */       if (!(runtimeTypeAdapter instanceof ReflectiveTypeAdapterFactory.Adapter)) {
/* 41:58 */         chosen = runtimeTypeAdapter;
/* 42:59 */       } else if (!(this.delegate instanceof ReflectiveTypeAdapterFactory.Adapter)) {
/* 43:62 */         chosen = this.delegate;
/* 44:   */       } else {
/* 45:65 */         chosen = runtimeTypeAdapter;
/* 46:   */       }
/* 47:   */     }
/* 48:68 */     chosen.write(out, value);
/* 49:   */   }
/* 50:   */   
/* 51:   */   private Type getRuntimeTypeIfMoreSpecific(Type type, Object value)
/* 52:   */   {
/* 53:75 */     if ((value != null) && ((type == Object.class) || ((type instanceof TypeVariable)) || ((type instanceof Class)))) {
/* 54:77 */       type = value.getClass();
/* 55:   */     }
/* 56:79 */     return type;
/* 57:   */   }
/* 58:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.bind.TypeAdapterRuntimeTypeWrapper
 * JD-Core Version:    0.7.0.1
 */