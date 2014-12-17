/*   1:    */ package com.google.gson;
/*   2:    */ 
/*   3:    */ import com.google.gson.internal..Gson.Preconditions;
/*   4:    */ import java.lang.annotation.Annotation;
/*   5:    */ import java.lang.reflect.Field;
/*   6:    */ import java.lang.reflect.Type;
/*   7:    */ import java.util.Arrays;
/*   8:    */ import java.util.Collection;
/*   9:    */ 
/*  10:    */ public final class FieldAttributes
/*  11:    */ {
/*  12:    */   private final Field field;
/*  13:    */   
/*  14:    */   public FieldAttributes(Field f)
/*  15:    */   {
/*  16: 45 */     .Gson.Preconditions.checkNotNull(f);
/*  17: 46 */     this.field = f;
/*  18:    */   }
/*  19:    */   
/*  20:    */   public Class<?> getDeclaringClass()
/*  21:    */   {
/*  22: 53 */     return this.field.getDeclaringClass();
/*  23:    */   }
/*  24:    */   
/*  25:    */   public String getName()
/*  26:    */   {
/*  27: 60 */     return this.field.getName();
/*  28:    */   }
/*  29:    */   
/*  30:    */   public Type getDeclaredType()
/*  31:    */   {
/*  32: 80 */     return this.field.getGenericType();
/*  33:    */   }
/*  34:    */   
/*  35:    */   public Class<?> getDeclaredClass()
/*  36:    */   {
/*  37:100 */     return this.field.getType();
/*  38:    */   }
/*  39:    */   
/*  40:    */   public <T extends Annotation> T getAnnotation(Class<T> annotation)
/*  41:    */   {
/*  42:111 */     return this.field.getAnnotation(annotation);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public Collection<Annotation> getAnnotations()
/*  46:    */   {
/*  47:121 */     return Arrays.asList(this.field.getAnnotations());
/*  48:    */   }
/*  49:    */   
/*  50:    */   public boolean hasModifier(int modifier)
/*  51:    */   {
/*  52:135 */     return (this.field.getModifiers() & modifier) != 0;
/*  53:    */   }
/*  54:    */   
/*  55:    */   Object get(Object instance)
/*  56:    */     throws IllegalAccessException
/*  57:    */   {
/*  58:146 */     return this.field.get(instance);
/*  59:    */   }
/*  60:    */   
/*  61:    */   boolean isSynthetic()
/*  62:    */   {
/*  63:155 */     return this.field.isSynthetic();
/*  64:    */   }
/*  65:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.FieldAttributes
 * JD-Core Version:    0.7.0.1
 */