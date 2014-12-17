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
/* 15:   */ import java.util.Date;
/* 16:   */ import java.util.Locale;
/* 17:   */ import java.util.TimeZone;
/* 18:   */ 
/* 19:   */ public final class DateTypeAdapter
/* 20:   */   extends TypeAdapter<Date>
/* 21:   */ {
/* 22:42 */   public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory()
/* 23:   */   {
/* 24:   */     public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
/* 25:   */     {
/* 26:45 */       return typeToken.getRawType() == Date.class ? new DateTypeAdapter() : null;
/* 27:   */     }
/* 28:   */   };
/* 29:49 */   private final DateFormat enUsFormat = DateFormat.getDateTimeInstance(2, 2, Locale.US);
/* 30:51 */   private final DateFormat localFormat = DateFormat.getDateTimeInstance(2, 2);
/* 31:53 */   private final DateFormat iso8601Format = buildIso8601Format();
/* 32:   */   
/* 33:   */   private static DateFormat buildIso8601Format()
/* 34:   */   {
/* 35:56 */     DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
/* 36:57 */     iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
/* 37:58 */     return iso8601Format;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public Date read(JsonReader in)
/* 41:   */     throws IOException
/* 42:   */   {
/* 43:62 */     if (in.peek() == JsonToken.NULL)
/* 44:   */     {
/* 45:63 */       in.nextNull();
/* 46:64 */       return null;
/* 47:   */     }
/* 48:66 */     return deserializeToDate(in.nextString());
/* 49:   */   }
/* 50:   */   
/* 51:   */   private synchronized Date deserializeToDate(String json)
/* 52:   */   {
/* 53:   */     try
/* 54:   */     {
/* 55:71 */       return this.localFormat.parse(json);
/* 56:   */     }
/* 57:   */     catch (ParseException ignored)
/* 58:   */     {
/* 59:   */       try
/* 60:   */       {
/* 61:75 */         return this.enUsFormat.parse(json);
/* 62:   */       }
/* 63:   */       catch (ParseException ignored)
/* 64:   */       {
/* 65:   */         try
/* 66:   */         {
/* 67:79 */           return this.iso8601Format.parse(json);
/* 68:   */         }
/* 69:   */         catch (ParseException e)
/* 70:   */         {
/* 71:81 */           throw new JsonSyntaxException(json, e);
/* 72:   */         }
/* 73:   */       }
/* 74:   */     }
/* 75:   */   }
/* 76:   */   
/* 77:   */   public synchronized void write(JsonWriter out, Date value)
/* 78:   */     throws IOException
/* 79:   */   {
/* 80:86 */     if (value == null)
/* 81:   */     {
/* 82:87 */       out.nullValue();
/* 83:88 */       return;
/* 84:   */     }
/* 85:90 */     String dateFormatAsString = this.enUsFormat.format(value);
/* 86:91 */     out.value(dateFormatAsString);
/* 87:   */   }
/* 88:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.bind.DateTypeAdapter
 * JD-Core Version:    0.7.0.1
 */