/*   1:    */ package com.google.gson.internal;
/*   2:    */ 
/*   3:    */ import java.lang.reflect.Type;
/*   4:    */ import java.util.Collections;
/*   5:    */ import java.util.HashMap;
/*   6:    */ import java.util.Map;
/*   7:    */ 
/*   8:    */ public final class Primitives
/*   9:    */ {
/*  10:    */   private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER_TYPE;
/*  11:    */   private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_TYPE;
/*  12:    */   
/*  13:    */   static
/*  14:    */   {
/*  15: 43 */     Map<Class<?>, Class<?>> primToWrap = new HashMap(16);
/*  16: 44 */     Map<Class<?>, Class<?>> wrapToPrim = new HashMap(16);
/*  17:    */     
/*  18: 46 */     add(primToWrap, wrapToPrim, Boolean.TYPE, Boolean.class);
/*  19: 47 */     add(primToWrap, wrapToPrim, Byte.TYPE, Byte.class);
/*  20: 48 */     add(primToWrap, wrapToPrim, Character.TYPE, Character.class);
/*  21: 49 */     add(primToWrap, wrapToPrim, Double.TYPE, Double.class);
/*  22: 50 */     add(primToWrap, wrapToPrim, Float.TYPE, Float.class);
/*  23: 51 */     add(primToWrap, wrapToPrim, Integer.TYPE, Integer.class);
/*  24: 52 */     add(primToWrap, wrapToPrim, Long.TYPE, Long.class);
/*  25: 53 */     add(primToWrap, wrapToPrim, Short.TYPE, Short.class);
/*  26: 54 */     add(primToWrap, wrapToPrim, Void.TYPE, Void.class);
/*  27:    */     
/*  28: 56 */     PRIMITIVE_TO_WRAPPER_TYPE = Collections.unmodifiableMap(primToWrap);
/*  29: 57 */     WRAPPER_TO_PRIMITIVE_TYPE = Collections.unmodifiableMap(wrapToPrim);
/*  30:    */   }
/*  31:    */   
/*  32:    */   private static void add(Map<Class<?>, Class<?>> forward, Map<Class<?>, Class<?>> backward, Class<?> key, Class<?> value)
/*  33:    */   {
/*  34: 62 */     forward.put(key, value);
/*  35: 63 */     backward.put(value, key);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public static boolean isPrimitive(Type type)
/*  39:    */   {
/*  40: 70 */     return PRIMITIVE_TO_WRAPPER_TYPE.containsKey(type);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public static boolean isWrapperType(Type type)
/*  44:    */   {
/*  45: 80 */     return WRAPPER_TO_PRIMITIVE_TYPE.containsKey(.Gson.Preconditions.checkNotNull(type));
/*  46:    */   }
/*  47:    */   
/*  48:    */   public static <T> Class<T> wrap(Class<T> type)
/*  49:    */   {
/*  50: 96 */     Class<T> wrapped = (Class)PRIMITIVE_TO_WRAPPER_TYPE.get(.Gson.Preconditions.checkNotNull(type));
/*  51:    */     
/*  52: 98 */     return wrapped == null ? type : wrapped;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public static <T> Class<T> unwrap(Class<T> type)
/*  56:    */   {
/*  57:113 */     Class<T> unwrapped = (Class)WRAPPER_TO_PRIMITIVE_TYPE.get(.Gson.Preconditions.checkNotNull(type));
/*  58:    */     
/*  59:115 */     return unwrapped == null ? type : unwrapped;
/*  60:    */   }
/*  61:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.Primitives
 * JD-Core Version:    0.7.0.1
 */