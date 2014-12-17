/*  1:   */ package com.google.gson;
/*  2:   */ 
/*  3:   */ import com.google.gson.internal.Streams;
/*  4:   */ import com.google.gson.stream.JsonReader;
/*  5:   */ import com.google.gson.stream.JsonToken;
/*  6:   */ import com.google.gson.stream.MalformedJsonException;
/*  7:   */ import java.io.IOException;
/*  8:   */ import java.io.Reader;
/*  9:   */ import java.io.StringReader;
/* 10:   */ 
/* 11:   */ public final class JsonParser
/* 12:   */ {
/* 13:   */   public JsonElement parse(String json)
/* 14:   */     throws JsonSyntaxException
/* 15:   */   {
/* 16:45 */     return parse(new StringReader(json));
/* 17:   */   }
/* 18:   */   
/* 19:   */   public JsonElement parse(Reader json)
/* 20:   */     throws JsonIOException, JsonSyntaxException
/* 21:   */   {
/* 22:   */     try
/* 23:   */     {
/* 24:58 */       JsonReader jsonReader = new JsonReader(json);
/* 25:59 */       JsonElement element = parse(jsonReader);
/* 26:60 */       if ((!element.isJsonNull()) && (jsonReader.peek() != JsonToken.END_DOCUMENT)) {
/* 27:61 */         throw new JsonSyntaxException("Did not consume the entire document.");
/* 28:   */       }
/* 29:63 */       return element;
/* 30:   */     }
/* 31:   */     catch (MalformedJsonException e)
/* 32:   */     {
/* 33:65 */       throw new JsonSyntaxException(e);
/* 34:   */     }
/* 35:   */     catch (IOException e)
/* 36:   */     {
/* 37:67 */       throw new JsonIOException(e);
/* 38:   */     }
/* 39:   */     catch (NumberFormatException e)
/* 40:   */     {
/* 41:69 */       throw new JsonSyntaxException(e);
/* 42:   */     }
/* 43:   */   }
/* 44:   */   
/* 45:   */   public JsonElement parse(JsonReader json)
/* 46:   */     throws JsonIOException, JsonSyntaxException
/* 47:   */   {
/* 48:81 */     boolean lenient = json.isLenient();
/* 49:82 */     json.setLenient(true);
/* 50:   */     try
/* 51:   */     {
/* 52:84 */       return Streams.parse(json);
/* 53:   */     }
/* 54:   */     catch (StackOverflowError e)
/* 55:   */     {
/* 56:86 */       throw new JsonParseException("Failed parsing JSON source: " + json + " to Json", e);
/* 57:   */     }
/* 58:   */     catch (OutOfMemoryError e)
/* 59:   */     {
/* 60:88 */       throw new JsonParseException("Failed parsing JSON source: " + json + " to Json", e);
/* 61:   */     }
/* 62:   */     finally
/* 63:   */     {
/* 64:90 */       json.setLenient(lenient);
/* 65:   */     }
/* 66:   */   }
/* 67:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.JsonParser
 * JD-Core Version:    0.7.0.1
 */