/*   1:    */ package com.google.gson.internal;
/*   2:    */ 
/*   3:    */ import com.google.gson.JsonElement;
/*   4:    */ import com.google.gson.JsonIOException;
/*   5:    */ import com.google.gson.JsonNull;
/*   6:    */ import com.google.gson.JsonParseException;
/*   7:    */ import com.google.gson.JsonSyntaxException;
/*   8:    */ import com.google.gson.TypeAdapter;
/*   9:    */ import com.google.gson.internal.bind.TypeAdapters;
/*  10:    */ import com.google.gson.stream.JsonReader;
/*  11:    */ import com.google.gson.stream.JsonWriter;
/*  12:    */ import com.google.gson.stream.MalformedJsonException;
/*  13:    */ import java.io.EOFException;
/*  14:    */ import java.io.IOException;
/*  15:    */ import java.io.Writer;
/*  16:    */ 
/*  17:    */ public final class Streams
/*  18:    */ {
/*  19:    */   public static JsonElement parse(JsonReader reader)
/*  20:    */     throws JsonParseException
/*  21:    */   {
/*  22: 40 */     boolean isEmpty = true;
/*  23:    */     try
/*  24:    */     {
/*  25: 42 */       reader.peek();
/*  26: 43 */       isEmpty = false;
/*  27: 44 */       return (JsonElement)TypeAdapters.JSON_ELEMENT.read(reader);
/*  28:    */     }
/*  29:    */     catch (EOFException e)
/*  30:    */     {
/*  31: 50 */       if (isEmpty) {
/*  32: 51 */         return JsonNull.INSTANCE;
/*  33:    */       }
/*  34: 54 */       throw new JsonSyntaxException(e);
/*  35:    */     }
/*  36:    */     catch (MalformedJsonException e)
/*  37:    */     {
/*  38: 56 */       throw new JsonSyntaxException(e);
/*  39:    */     }
/*  40:    */     catch (IOException e)
/*  41:    */     {
/*  42: 58 */       throw new JsonIOException(e);
/*  43:    */     }
/*  44:    */     catch (NumberFormatException e)
/*  45:    */     {
/*  46: 60 */       throw new JsonSyntaxException(e);
/*  47:    */     }
/*  48:    */   }
/*  49:    */   
/*  50:    */   public static void write(JsonElement element, JsonWriter writer)
/*  51:    */     throws IOException
/*  52:    */   {
/*  53: 68 */     TypeAdapters.JSON_ELEMENT.write(writer, element);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static Writer writerForAppendable(Appendable appendable)
/*  57:    */   {
/*  58: 72 */     return (appendable instanceof Writer) ? (Writer)appendable : new AppendableWriter(appendable, null);
/*  59:    */   }
/*  60:    */   
/*  61:    */   private static final class AppendableWriter
/*  62:    */     extends Writer
/*  63:    */   {
/*  64:    */     private final Appendable appendable;
/*  65: 81 */     private final CurrentWrite currentWrite = new CurrentWrite();
/*  66:    */     
/*  67:    */     private AppendableWriter(Appendable appendable)
/*  68:    */     {
/*  69: 84 */       this.appendable = appendable;
/*  70:    */     }
/*  71:    */     
/*  72:    */     public void write(char[] chars, int offset, int length)
/*  73:    */       throws IOException
/*  74:    */     {
/*  75: 88 */       this.currentWrite.chars = chars;
/*  76: 89 */       this.appendable.append(this.currentWrite, offset, offset + length);
/*  77:    */     }
/*  78:    */     
/*  79:    */     public void write(int i)
/*  80:    */       throws IOException
/*  81:    */     {
/*  82: 93 */       this.appendable.append((char)i);
/*  83:    */     }
/*  84:    */     
/*  85:    */     public void flush() {}
/*  86:    */     
/*  87:    */     public void close() {}
/*  88:    */     
/*  89:    */     static class CurrentWrite
/*  90:    */       implements CharSequence
/*  91:    */     {
/*  92:    */       char[] chars;
/*  93:    */       
/*  94:    */       public int length()
/*  95:    */       {
/*  96:105 */         return this.chars.length;
/*  97:    */       }
/*  98:    */       
/*  99:    */       public char charAt(int i)
/* 100:    */       {
/* 101:108 */         return this.chars[i];
/* 102:    */       }
/* 103:    */       
/* 104:    */       public CharSequence subSequence(int start, int end)
/* 105:    */       {
/* 106:111 */         return new String(this.chars, start, end - start);
/* 107:    */       }
/* 108:    */     }
/* 109:    */   }
/* 110:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.Streams
 * JD-Core Version:    0.7.0.1
 */