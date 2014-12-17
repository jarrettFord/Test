/*  1:   */ package org.spacehq.mc.protocol.data.message;
/*  2:   */ 
/*  3:   */ import com.google.gson.JsonArray;
/*  4:   */ import com.google.gson.JsonElement;
/*  5:   */ import com.google.gson.JsonObject;
/*  6:   */ import java.util.Arrays;
/*  7:   */ 
/*  8:   */ public class TranslationMessage
/*  9:   */   extends Message
/* 10:   */ {
/* 11:   */   private String translationKey;
/* 12:   */   private Message[] translationParams;
/* 13:   */   
/* 14:   */   public TranslationMessage(String translationKey, Message... translationParams)
/* 15:   */   {
/* 16:15 */     this.translationKey = translationKey;
/* 17:16 */     this.translationParams = translationParams;
/* 18:17 */     this.translationParams = getTranslationParams();
/* 19:18 */     for (Message param : this.translationParams) {
/* 20:19 */       param.getStyle().setParent(getStyle());
/* 21:   */     }
/* 22:   */   }
/* 23:   */   
/* 24:   */   public String getTranslationKey()
/* 25:   */   {
/* 26:24 */     return this.translationKey;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public Message[] getTranslationParams()
/* 30:   */   {
/* 31:28 */     Message[] copy = (Message[])Arrays.copyOf(this.translationParams, this.translationParams.length);
/* 32:29 */     for (int index = 0; index < copy.length; index++) {
/* 33:30 */       copy[index] = copy[index].clone();
/* 34:   */     }
/* 35:33 */     return copy;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public Message setStyle(MessageStyle style)
/* 39:   */   {
/* 40:38 */     super.setStyle(style);
/* 41:39 */     for (Message param : this.translationParams) {
/* 42:40 */       param.getStyle().setParent(getStyle());
/* 43:   */     }
/* 44:43 */     return this;
/* 45:   */   }
/* 46:   */   
/* 47:   */   public String getText()
/* 48:   */   {
/* 49:48 */     return this.translationKey;
/* 50:   */   }
/* 51:   */   
/* 52:   */   public TranslationMessage clone()
/* 53:   */   {
/* 54:53 */     return (TranslationMessage)new TranslationMessage(getTranslationKey(), getTranslationParams()).setStyle(getStyle().clone()).setExtra(getExtra());
/* 55:   */   }
/* 56:   */   
/* 57:   */   public JsonElement toJson()
/* 58:   */   {
/* 59:58 */     JsonElement e = super.toJson();
/* 60:59 */     if (e.isJsonObject())
/* 61:   */     {
/* 62:60 */       JsonObject json = e.getAsJsonObject();
/* 63:61 */       json.addProperty("translate", this.translationKey);
/* 64:62 */       JsonArray params = new JsonArray();
/* 65:63 */       for (Message param : this.translationParams) {
/* 66:64 */         params.add(param.toJson());
/* 67:   */       }
/* 68:67 */       json.add("with", params);
/* 69:68 */       return json;
/* 70:   */     }
/* 71:70 */     return e;
/* 72:   */   }
/* 73:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.message.TranslationMessage
 * JD-Core Version:    0.7.0.1
 */