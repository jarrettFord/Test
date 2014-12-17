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
/* 12:   */ import java.sql.Time;
/* 13:   */ import java.text.DateFormat;
/* 14:   */ import java.text.ParseException;
/* 15:   */ import java.text.SimpleDateFormat;
/* 16:   */ import java.util.Date;
/* 17:   */ 
/* 18:   */ public final class TimeTypeAdapter
/* 19:   */   extends TypeAdapter<Time>
/* 20:   */ {
/* 21:41 */   public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory()
/* 22:   */   {
/* 23:   */     public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
/* 24:   */     {
/* 25:44 */       return typeToken.getRawType() == Time.class ? new TimeTypeAdapter() : null;
/* 26:   */     }
/* 27:   */   };
/* 28:48 */   private final DateFormat format = new SimpleDateFormat("hh:mm:ss a");
/* 29:   */   
/* 30:   */   public synchronized Time read(JsonReader in)
/* 31:   */     throws IOException
/* 32:   */   {
/* 33:51 */     if (in.peek() == JsonToken.NULL)
/* 34:   */     {
/* 35:52 */       in.nextNull();
/* 36:53 */       return null;
/* 37:   */     }
/* 38:   */     try
/* 39:   */     {
/* 40:56 */       Date date = this.format.parse(in.nextString());
/* 41:57 */       return new Time(date.getTime());
/* 42:   */     }
/* 43:   */     catch (ParseException e)
/* 44:   */     {
/* 45:59 */       throw new JsonSyntaxException(e);
/* 46:   */     }
/* 47:   */   }
/* 48:   */   
/* 49:   */   public synchronized void write(JsonWriter out, Time value)
/* 50:   */     throws IOException
/* 51:   */   {
/* 52:64 */     out.value(value == null ? null : this.format.format(value));
/* 53:   */   }
/* 54:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.bind.TimeTypeAdapter
 * JD-Core Version:    0.7.0.1
 */