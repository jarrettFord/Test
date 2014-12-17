/*   1:    */ package com.google.gson;
/*   2:    */ 
/*   3:    */ import java.lang.reflect.Type;
/*   4:    */ import java.sql.Timestamp;
/*   5:    */ import java.text.DateFormat;
/*   6:    */ import java.text.ParseException;
/*   7:    */ import java.text.SimpleDateFormat;
/*   8:    */ import java.util.Locale;
/*   9:    */ import java.util.TimeZone;
/*  10:    */ 
/*  11:    */ final class DefaultDateTypeAdapter
/*  12:    */   implements JsonSerializer<java.util.Date>, JsonDeserializer<java.util.Date>
/*  13:    */ {
/*  14:    */   private final DateFormat enUsFormat;
/*  15:    */   private final DateFormat localFormat;
/*  16:    */   private final DateFormat iso8601Format;
/*  17:    */   
/*  18:    */   DefaultDateTypeAdapter()
/*  19:    */   {
/*  20: 44 */     this(DateFormat.getDateTimeInstance(2, 2, Locale.US), DateFormat.getDateTimeInstance(2, 2));
/*  21:    */   }
/*  22:    */   
/*  23:    */   DefaultDateTypeAdapter(String datePattern)
/*  24:    */   {
/*  25: 49 */     this(new SimpleDateFormat(datePattern, Locale.US), new SimpleDateFormat(datePattern));
/*  26:    */   }
/*  27:    */   
/*  28:    */   DefaultDateTypeAdapter(int style)
/*  29:    */   {
/*  30: 53 */     this(DateFormat.getDateInstance(style, Locale.US), DateFormat.getDateInstance(style));
/*  31:    */   }
/*  32:    */   
/*  33:    */   public DefaultDateTypeAdapter(int dateStyle, int timeStyle)
/*  34:    */   {
/*  35: 57 */     this(DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US), DateFormat.getDateTimeInstance(dateStyle, timeStyle));
/*  36:    */   }
/*  37:    */   
/*  38:    */   DefaultDateTypeAdapter(DateFormat enUsFormat, DateFormat localFormat)
/*  39:    */   {
/*  40: 62 */     this.enUsFormat = enUsFormat;
/*  41: 63 */     this.localFormat = localFormat;
/*  42: 64 */     this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
/*  43: 65 */     this.iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
/*  44:    */   }
/*  45:    */   
/*  46:    */   public JsonElement serialize(java.util.Date src, Type typeOfSrc, JsonSerializationContext context)
/*  47:    */   {
/*  48: 71 */     synchronized (this.localFormat)
/*  49:    */     {
/*  50: 72 */       String dateFormatAsString = this.enUsFormat.format(src);
/*  51: 73 */       return new JsonPrimitive(dateFormatAsString);
/*  52:    */     }
/*  53:    */   }
/*  54:    */   
/*  55:    */   public java.util.Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
/*  56:    */     throws JsonParseException
/*  57:    */   {
/*  58: 79 */     if (!(json instanceof JsonPrimitive)) {
/*  59: 80 */       throw new JsonParseException("The date should be a string value");
/*  60:    */     }
/*  61: 82 */     java.util.Date date = deserializeToDate(json);
/*  62: 83 */     if (typeOfT == java.util.Date.class) {
/*  63: 84 */       return date;
/*  64:    */     }
/*  65: 85 */     if (typeOfT == Timestamp.class) {
/*  66: 86 */       return new Timestamp(date.getTime());
/*  67:    */     }
/*  68: 87 */     if (typeOfT == java.sql.Date.class) {
/*  69: 88 */       return new java.sql.Date(date.getTime());
/*  70:    */     }
/*  71: 90 */     throw new IllegalArgumentException(getClass() + " cannot deserialize to " + typeOfT);
/*  72:    */   }
/*  73:    */   
/*  74:    */   private java.util.Date deserializeToDate(JsonElement json)
/*  75:    */   {
/*  76: 95 */     synchronized (this.localFormat)
/*  77:    */     {
/*  78:    */       try
/*  79:    */       {
/*  80: 97 */         return this.localFormat.parse(json.getAsString());
/*  81:    */       }
/*  82:    */       catch (ParseException ignored)
/*  83:    */       {
/*  84:    */         try
/*  85:    */         {
/*  86:101 */           return this.enUsFormat.parse(json.getAsString());
/*  87:    */         }
/*  88:    */         catch (ParseException ignored)
/*  89:    */         {
/*  90:    */           try
/*  91:    */           {
/*  92:105 */             return this.iso8601Format.parse(json.getAsString());
/*  93:    */           }
/*  94:    */           catch (ParseException e)
/*  95:    */           {
/*  96:107 */             throw new JsonSyntaxException(json.getAsString(), e);
/*  97:    */           }
/*  98:    */         }
/*  99:    */       }
/* 100:    */     }
/* 101:    */   }
/* 102:    */   
/* 103:    */   public String toString()
/* 104:    */   {
/* 105:114 */     StringBuilder sb = new StringBuilder();
/* 106:115 */     sb.append(DefaultDateTypeAdapter.class.getSimpleName());
/* 107:116 */     sb.append('(').append(this.localFormat.getClass().getSimpleName()).append(')');
/* 108:117 */     return sb.toString();
/* 109:    */   }
/* 110:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.DefaultDateTypeAdapter
 * JD-Core Version:    0.7.0.1
 */