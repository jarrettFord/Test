/*  1:   */ package org.spacehq.mc.auth.serialize;
/*  2:   */ 
/*  3:   */ import com.google.gson.TypeAdapter;
/*  4:   */ import com.google.gson.stream.JsonReader;
/*  5:   */ import com.google.gson.stream.JsonWriter;
/*  6:   */ import java.io.IOException;
/*  7:   */ import java.util.UUID;
/*  8:   */ 
/*  9:   */ public class UUIDSerializer
/* 10:   */   extends TypeAdapter<UUID>
/* 11:   */ {
/* 12:   */   public void write(JsonWriter out, UUID value)
/* 13:   */     throws IOException
/* 14:   */   {
/* 15:13 */     out.value(fromUUID(value));
/* 16:   */   }
/* 17:   */   
/* 18:   */   public UUID read(JsonReader in)
/* 19:   */     throws IOException
/* 20:   */   {
/* 21:17 */     return fromString(in.nextString());
/* 22:   */   }
/* 23:   */   
/* 24:   */   public static String fromUUID(UUID value)
/* 25:   */   {
/* 26:21 */     if (value == null) {
/* 27:22 */       return "";
/* 28:   */     }
/* 29:25 */     return value.toString().replace("-", "");
/* 30:   */   }
/* 31:   */   
/* 32:   */   public static UUID fromString(String input)
/* 33:   */   {
/* 34:29 */     if ((input == null) || (input.equals(""))) {
/* 35:30 */       return null;
/* 36:   */     }
/* 37:33 */     return UUID.fromString(input.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
/* 38:   */   }
/* 39:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.serialize.UUIDSerializer
 * JD-Core Version:    0.7.0.1
 */