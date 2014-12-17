/*  1:   */ package org.spacehq.mc.protocol.data.message;
/*  2:   */ 
/*  3:   */ import com.google.gson.JsonElement;
/*  4:   */ import com.google.gson.JsonObject;
/*  5:   */ import com.google.gson.JsonPrimitive;
/*  6:   */ import java.util.List;
/*  7:   */ 
/*  8:   */ public class TextMessage
/*  9:   */   extends Message
/* 10:   */ {
/* 11:   */   private String text;
/* 12:   */   
/* 13:   */   public TextMessage(String text)
/* 14:   */   {
/* 15:12 */     this.text = text;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public String getText()
/* 19:   */   {
/* 20:17 */     return this.text;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public TextMessage clone()
/* 24:   */   {
/* 25:22 */     return (TextMessage)new TextMessage(getText()).setStyle(getStyle().clone()).setExtra(getExtra());
/* 26:   */   }
/* 27:   */   
/* 28:   */   public JsonElement toJson()
/* 29:   */   {
/* 30:27 */     if ((getStyle().isDefault()) && (getExtra().isEmpty())) {
/* 31:28 */       return new JsonPrimitive(this.text);
/* 32:   */     }
/* 33:30 */     JsonElement e = super.toJson();
/* 34:31 */     if (e.isJsonObject())
/* 35:   */     {
/* 36:32 */       JsonObject json = e.getAsJsonObject();
/* 37:33 */       json.addProperty("text", this.text);
/* 38:34 */       return json;
/* 39:   */     }
/* 40:36 */     return e;
/* 41:   */   }
/* 42:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.message.TextMessage
 * JD-Core Version:    0.7.0.1
 */