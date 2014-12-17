/*   1:    */ package com.google.gson;
/*   2:    */ 
/*   3:    */ import com.google.gson.internal.bind.JsonTreeReader;
/*   4:    */ import com.google.gson.internal.bind.JsonTreeWriter;
/*   5:    */ import com.google.gson.stream.JsonReader;
/*   6:    */ import com.google.gson.stream.JsonToken;
/*   7:    */ import com.google.gson.stream.JsonWriter;
/*   8:    */ import java.io.IOException;
/*   9:    */ import java.io.Reader;
/*  10:    */ import java.io.StringReader;
/*  11:    */ import java.io.StringWriter;
/*  12:    */ import java.io.Writer;
/*  13:    */ 
/*  14:    */ public abstract class TypeAdapter<T>
/*  15:    */ {
/*  16:    */   public abstract void write(JsonWriter paramJsonWriter, T paramT)
/*  17:    */     throws IOException;
/*  18:    */   
/*  19:    */   public final void toJson(Writer out, T value)
/*  20:    */     throws IOException
/*  21:    */   {
/*  22:141 */     JsonWriter writer = new JsonWriter(out);
/*  23:142 */     write(writer, value);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public final TypeAdapter<T> nullSafe()
/*  27:    */   {
/*  28:186 */     new TypeAdapter()
/*  29:    */     {
/*  30:    */       public void write(JsonWriter out, T value)
/*  31:    */         throws IOException
/*  32:    */       {
/*  33:188 */         if (value == null) {
/*  34:189 */           out.nullValue();
/*  35:    */         } else {
/*  36:191 */           TypeAdapter.this.write(out, value);
/*  37:    */         }
/*  38:    */       }
/*  39:    */       
/*  40:    */       public T read(JsonReader reader)
/*  41:    */         throws IOException
/*  42:    */       {
/*  43:195 */         if (reader.peek() == JsonToken.NULL)
/*  44:    */         {
/*  45:196 */           reader.nextNull();
/*  46:197 */           return null;
/*  47:    */         }
/*  48:199 */         return TypeAdapter.this.read(reader);
/*  49:    */       }
/*  50:    */     };
/*  51:    */   }
/*  52:    */   
/*  53:    */   public final String toJson(T value)
/*  54:    */     throws IOException
/*  55:    */   {
/*  56:215 */     StringWriter stringWriter = new StringWriter();
/*  57:216 */     toJson(stringWriter, value);
/*  58:217 */     return stringWriter.toString();
/*  59:    */   }
/*  60:    */   
/*  61:    */   public final JsonElement toJsonTree(T value)
/*  62:    */   {
/*  63:    */     try
/*  64:    */     {
/*  65:229 */       JsonTreeWriter jsonWriter = new JsonTreeWriter();
/*  66:230 */       write(jsonWriter, value);
/*  67:231 */       return jsonWriter.get();
/*  68:    */     }
/*  69:    */     catch (IOException e)
/*  70:    */     {
/*  71:233 */       throw new JsonIOException(e);
/*  72:    */     }
/*  73:    */   }
/*  74:    */   
/*  75:    */   public abstract T read(JsonReader paramJsonReader)
/*  76:    */     throws IOException;
/*  77:    */   
/*  78:    */   public final T fromJson(Reader in)
/*  79:    */     throws IOException
/*  80:    */   {
/*  81:255 */     JsonReader reader = new JsonReader(in);
/*  82:256 */     return read(reader);
/*  83:    */   }
/*  84:    */   
/*  85:    */   public final T fromJson(String json)
/*  86:    */     throws IOException
/*  87:    */   {
/*  88:269 */     return fromJson(new StringReader(json));
/*  89:    */   }
/*  90:    */   
/*  91:    */   public final T fromJsonTree(JsonElement jsonTree)
/*  92:    */   {
/*  93:    */     try
/*  94:    */     {
/*  95:280 */       JsonReader jsonReader = new JsonTreeReader(jsonTree);
/*  96:281 */       return read(jsonReader);
/*  97:    */     }
/*  98:    */     catch (IOException e)
/*  99:    */     {
/* 100:283 */       throw new JsonIOException(e);
/* 101:    */     }
/* 102:    */   }
/* 103:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.TypeAdapter
 * JD-Core Version:    0.7.0.1
 */