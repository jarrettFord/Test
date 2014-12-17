/*   1:    */ package com.google.gson;
/*   2:    */ 
/*   3:    */ import com.google.gson.internal.Streams;
/*   4:    */ import com.google.gson.stream.JsonReader;
/*   5:    */ import com.google.gson.stream.JsonToken;
/*   6:    */ import com.google.gson.stream.MalformedJsonException;
/*   7:    */ import java.io.EOFException;
/*   8:    */ import java.io.IOException;
/*   9:    */ import java.io.Reader;
/*  10:    */ import java.io.StringReader;
/*  11:    */ import java.util.Iterator;
/*  12:    */ import java.util.NoSuchElementException;
/*  13:    */ 
/*  14:    */ public final class JsonStreamParser
/*  15:    */   implements Iterator<JsonElement>
/*  16:    */ {
/*  17:    */   private final JsonReader parser;
/*  18:    */   private final Object lock;
/*  19:    */   
/*  20:    */   public JsonStreamParser(String json)
/*  21:    */   {
/*  22: 61 */     this(new StringReader(json));
/*  23:    */   }
/*  24:    */   
/*  25:    */   public JsonStreamParser(Reader reader)
/*  26:    */   {
/*  27: 69 */     this.parser = new JsonReader(reader);
/*  28: 70 */     this.parser.setLenient(true);
/*  29: 71 */     this.lock = new Object();
/*  30:    */   }
/*  31:    */   
/*  32:    */   public JsonElement next()
/*  33:    */     throws JsonParseException
/*  34:    */   {
/*  35: 82 */     if (!hasNext()) {
/*  36: 83 */       throw new NoSuchElementException();
/*  37:    */     }
/*  38:    */     try
/*  39:    */     {
/*  40: 87 */       return Streams.parse(this.parser);
/*  41:    */     }
/*  42:    */     catch (StackOverflowError e)
/*  43:    */     {
/*  44: 89 */       throw new JsonParseException("Failed parsing JSON source to Json", e);
/*  45:    */     }
/*  46:    */     catch (OutOfMemoryError e)
/*  47:    */     {
/*  48: 91 */       throw new JsonParseException("Failed parsing JSON source to Json", e);
/*  49:    */     }
/*  50:    */     catch (JsonParseException e)
/*  51:    */     {
/*  52: 93 */       throw ((e.getCause() instanceof EOFException) ? new NoSuchElementException() : e);
/*  53:    */     }
/*  54:    */   }
/*  55:    */   
/*  56:    */   public boolean hasNext()
/*  57:    */   {
/*  58:103 */     synchronized (this.lock)
/*  59:    */     {
/*  60:    */       try
/*  61:    */       {
/*  62:105 */         return this.parser.peek() != JsonToken.END_DOCUMENT;
/*  63:    */       }
/*  64:    */       catch (MalformedJsonException e)
/*  65:    */       {
/*  66:107 */         throw new JsonSyntaxException(e);
/*  67:    */       }
/*  68:    */       catch (IOException e)
/*  69:    */       {
/*  70:109 */         throw new JsonIOException(e);
/*  71:    */       }
/*  72:    */     }
/*  73:    */   }
/*  74:    */   
/*  75:    */   public void remove()
/*  76:    */   {
/*  77:120 */     throw new UnsupportedOperationException();
/*  78:    */   }
/*  79:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.JsonStreamParser
 * JD-Core Version:    0.7.0.1
 */