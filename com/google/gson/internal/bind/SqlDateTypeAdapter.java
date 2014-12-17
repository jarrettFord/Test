/*  1:   */ package com.google.gson.internal.bind;
/*  2:   */ 
/*  3:   */ import com.google.gson.Gson;
/*  4:   */ import com.google.gson.JsonSyntaxException;
/*  5:   */ import com.google.gson.TypeAdapter;
/*  6:   */ import com.google.gson.TypeAdapterFactory;
/*  7:   */ import com.google.gson.reflect.TypeToken;
/*  8:   */ import com.google.gson.stream.JsonReader;
/*  9:   */ import com.google.gson.stream.JsonToken;
/* 10:   */ import com.google.gson.stream.JsonWriter;
/* 11:   */ import java.io.IOException;
/* 12:   */ import java.text.DateFormat;
/* 13:   */ import java.text.ParseException;
/* 14:   */ import java.text.SimpleDateFormat;
/* 15:   */ 
/* 16:   */ public final class SqlDateTypeAdapter
/* 17:   */   extends TypeAdapter<java.sql.Date>
/* 18:   */ {
/* 19:39 */   public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory()
/* 20:   */   {
/* 21:   */     public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
/* 22:   */     {
/* 23:42 */       return typeToken.getRawType() == java.sql.Date.class ? new SqlDateTypeAdapter() : null;
/* 24:   */     }
/* 25:   */   };
/* 26:47 */   private final DateFormat format = new SimpleDateFormat("MMM d, yyyy");
/* 27:   */   
/* 28:   */   public synchronized java.sql.Date read(JsonReader in)
/* 29:   */     throws IOException
/* 30:   */   {
/* 31:51 */     if (in.peek() == JsonToken.NULL)
/* 32:   */     {
/* 33:52 */       in.nextNull();
/* 34:53 */       return null;
/* 35:   */     }
/* 36:   */     try
/* 37:   */     {
/* 38:56 */       long utilDate = this.format.parse(in.nextString()).getTime();
/* 39:57 */       return new java.sql.Date(utilDate);
/* 40:   */     }
/* 41:   */     catch (ParseException e)
/* 42:   */     {
/* 43:59 */       throw new JsonSyntaxException(e);
/* 44:   */     }
/* 45:   */   }
/* 46:   */   
/* 47:   */   public synchronized void write(JsonWriter out, java.sql.Date value)
/* 48:   */     throws IOException
/* 49:   */   {
/* 50:65 */     out.value(value == null ? null : this.format.format(value));
/* 51:   */   }
/* 52:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.bind.SqlDateTypeAdapter
 * JD-Core Version:    0.7.0.1
 */