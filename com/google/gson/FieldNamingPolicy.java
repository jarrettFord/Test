/*   1:    */ package com.google.gson;
/*   2:    */ 
/*   3:    */ import java.lang.reflect.Field;
/*   4:    */ 
/*   5:    */ public enum FieldNamingPolicy
/*   6:    */   implements FieldNamingStrategy
/*   7:    */ {
/*   8: 36 */   IDENTITY,  UPPER_CAMEL_CASE,  UPPER_CAMEL_CASE_WITH_SPACES,  LOWER_CASE_WITH_UNDERSCORES,  LOWER_CASE_WITH_DASHES;
/*   9:    */   
/*  10:    */   private FieldNamingPolicy() {}
/*  11:    */   
/*  12:    */   private static String separateCamelCase(String name, String separator)
/*  13:    */   {
/*  14:123 */     StringBuilder translation = new StringBuilder();
/*  15:124 */     for (int i = 0; i < name.length(); i++)
/*  16:    */     {
/*  17:125 */       char character = name.charAt(i);
/*  18:126 */       if ((Character.isUpperCase(character)) && (translation.length() != 0)) {
/*  19:127 */         translation.append(separator);
/*  20:    */       }
/*  21:129 */       translation.append(character);
/*  22:    */     }
/*  23:131 */     return translation.toString();
/*  24:    */   }
/*  25:    */   
/*  26:    */   private static String upperCaseFirstLetter(String name)
/*  27:    */   {
/*  28:138 */     StringBuilder fieldNameBuilder = new StringBuilder();
/*  29:139 */     int index = 0;
/*  30:140 */     char firstCharacter = name.charAt(index);
/*  31:142 */     while ((index < name.length() - 1) && 
/*  32:143 */       (!Character.isLetter(firstCharacter)))
/*  33:    */     {
/*  34:147 */       fieldNameBuilder.append(firstCharacter);
/*  35:148 */       firstCharacter = name.charAt(++index);
/*  36:    */     }
/*  37:151 */     if (index == name.length()) {
/*  38:152 */       return fieldNameBuilder.toString();
/*  39:    */     }
/*  40:155 */     if (!Character.isUpperCase(firstCharacter))
/*  41:    */     {
/*  42:156 */       String modifiedTarget = modifyString(Character.toUpperCase(firstCharacter), name, ++index);
/*  43:157 */       return modifiedTarget;
/*  44:    */     }
/*  45:159 */     return name;
/*  46:    */   }
/*  47:    */   
/*  48:    */   private static String modifyString(char firstCharacter, String srcString, int indexOfSubstring)
/*  49:    */   {
/*  50:164 */     return indexOfSubstring < srcString.length() ? firstCharacter + srcString.substring(indexOfSubstring) : String.valueOf(firstCharacter);
/*  51:    */   }
/*  52:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.FieldNamingPolicy
 * JD-Core Version:    0.7.0.1
 */