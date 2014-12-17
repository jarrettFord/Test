/*  1:   */ package com.google.gson.internal.bind;
/*  2:   */ 
/*  3:   */ import com.google.gson.Gson;
/*  4:   */ import com.google.gson.TypeAdapter;
/*  5:   */ import com.google.gson.TypeAdapterFactory;
/*  6:   */ import com.google.gson.internal..Gson.Types;
/*  7:   */ import com.google.gson.internal.ConstructorConstructor;
/*  8:   */ import com.google.gson.internal.ObjectConstructor;
/*  9:   */ import com.google.gson.reflect.TypeToken;
/* 10:   */ import com.google.gson.stream.JsonReader;
/* 11:   */ import com.google.gson.stream.JsonToken;
/* 12:   */ import com.google.gson.stream.JsonWriter;
/* 13:   */ import java.io.IOException;
/* 14:   */ import java.lang.reflect.Type;
/* 15:   */ import java.util.Collection;
/* 16:   */ 
/* 17:   */ public final class CollectionTypeAdapterFactory
/* 18:   */   implements TypeAdapterFactory
/* 19:   */ {
/* 20:   */   private final ConstructorConstructor constructorConstructor;
/* 21:   */   
/* 22:   */   public CollectionTypeAdapterFactory(ConstructorConstructor constructorConstructor)
/* 23:   */   {
/* 24:40 */     this.constructorConstructor = constructorConstructor;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
/* 28:   */   {
/* 29:44 */     Type type = typeToken.getType();
/* 30:   */     
/* 31:46 */     Class<? super T> rawType = typeToken.getRawType();
/* 32:47 */     if (!Collection.class.isAssignableFrom(rawType)) {
/* 33:48 */       return null;
/* 34:   */     }
/* 35:51 */     Type elementType = .Gson.Types.getCollectionElementType(type, rawType);
/* 36:52 */     TypeAdapter<?> elementTypeAdapter = gson.getAdapter(TypeToken.get(elementType));
/* 37:53 */     ObjectConstructor<T> constructor = this.constructorConstructor.get(typeToken);
/* 38:   */     
/* 39:   */ 
/* 40:56 */     TypeAdapter<T> result = new Adapter(gson, elementType, elementTypeAdapter, constructor);
/* 41:57 */     return result;
/* 42:   */   }
/* 43:   */   
/* 44:   */   private static final class Adapter<E>
/* 45:   */     extends TypeAdapter<Collection<E>>
/* 46:   */   {
/* 47:   */     private final TypeAdapter<E> elementTypeAdapter;
/* 48:   */     private final ObjectConstructor<? extends Collection<E>> constructor;
/* 49:   */     
/* 50:   */     public Adapter(Gson context, Type elementType, TypeAdapter<E> elementTypeAdapter, ObjectConstructor<? extends Collection<E>> constructor)
/* 51:   */     {
/* 52:67 */       this.elementTypeAdapter = new TypeAdapterRuntimeTypeWrapper(context, elementTypeAdapter, elementType);
/* 53:   */       
/* 54:69 */       this.constructor = constructor;
/* 55:   */     }
/* 56:   */     
/* 57:   */     public Collection<E> read(JsonReader in)
/* 58:   */       throws IOException
/* 59:   */     {
/* 60:73 */       if (in.peek() == JsonToken.NULL)
/* 61:   */       {
/* 62:74 */         in.nextNull();
/* 63:75 */         return null;
/* 64:   */       }
/* 65:78 */       Collection<E> collection = (Collection)this.constructor.construct();
/* 66:79 */       in.beginArray();
/* 67:80 */       while (in.hasNext())
/* 68:   */       {
/* 69:81 */         E instance = this.elementTypeAdapter.read(in);
/* 70:82 */         collection.add(instance);
/* 71:   */       }
/* 72:84 */       in.endArray();
/* 73:85 */       return collection;
/* 74:   */     }
/* 75:   */     
/* 76:   */     public void write(JsonWriter out, Collection<E> collection)
/* 77:   */       throws IOException
/* 78:   */     {
/* 79:89 */       if (collection == null)
/* 80:   */       {
/* 81:90 */         out.nullValue();
/* 82:91 */         return;
/* 83:   */       }
/* 84:94 */       out.beginArray();
/* 85:95 */       for (E element : collection) {
/* 86:96 */         this.elementTypeAdapter.write(out, element);
/* 87:   */       }
/* 88:98 */       out.endArray();
/* 89:   */     }
/* 90:   */   }
/* 91:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.bind.CollectionTypeAdapterFactory
 * JD-Core Version:    0.7.0.1
 */