/*  1:   */ package io.netty.util;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.PlatformDependent;
/*  4:   */ import java.util.concurrent.ConcurrentMap;
/*  5:   */ 
/*  6:   */ public final class AttributeKey<T>
/*  7:   */   extends UniqueName
/*  8:   */ {
/*  9:32 */   private static final ConcurrentMap<String, Boolean> names = ;
/* 10:   */   
/* 11:   */   public static <T> AttributeKey<T> valueOf(String name)
/* 12:   */   {
/* 13:39 */     return new AttributeKey(name);
/* 14:   */   }
/* 15:   */   
/* 16:   */   @Deprecated
/* 17:   */   public AttributeKey(String name)
/* 18:   */   {
/* 19:47 */     super(names, name, new Object[0]);
/* 20:   */   }
/* 21:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.AttributeKey
 * JD-Core Version:    0.7.0.1
 */