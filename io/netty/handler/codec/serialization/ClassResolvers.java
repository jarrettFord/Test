/*  1:   */ package io.netty.handler.codec.serialization;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.PlatformDependent;
/*  4:   */ import java.util.HashMap;
/*  5:   */ 
/*  6:   */ public final class ClassResolvers
/*  7:   */ {
/*  8:   */   public static ClassResolver cacheDisabled(ClassLoader classLoader)
/*  9:   */   {
/* 10:31 */     return new ClassLoaderClassResolver(defaultClassLoader(classLoader));
/* 11:   */   }
/* 12:   */   
/* 13:   */   public static ClassResolver weakCachingResolver(ClassLoader classLoader)
/* 14:   */   {
/* 15:42 */     return new CachingClassResolver(new ClassLoaderClassResolver(defaultClassLoader(classLoader)), new WeakReferenceMap(new HashMap()));
/* 16:   */   }
/* 17:   */   
/* 18:   */   public static ClassResolver softCachingResolver(ClassLoader classLoader)
/* 19:   */   {
/* 20:55 */     return new CachingClassResolver(new ClassLoaderClassResolver(defaultClassLoader(classLoader)), new SoftReferenceMap(new HashMap()));
/* 21:   */   }
/* 22:   */   
/* 23:   */   public static ClassResolver weakCachingConcurrentResolver(ClassLoader classLoader)
/* 24:   */   {
/* 25:68 */     return new CachingClassResolver(new ClassLoaderClassResolver(defaultClassLoader(classLoader)), new WeakReferenceMap(PlatformDependent.newConcurrentHashMap()));
/* 26:   */   }
/* 27:   */   
/* 28:   */   public static ClassResolver softCachingConcurrentResolver(ClassLoader classLoader)
/* 29:   */   {
/* 30:82 */     return new CachingClassResolver(new ClassLoaderClassResolver(defaultClassLoader(classLoader)), new SoftReferenceMap(PlatformDependent.newConcurrentHashMap()));
/* 31:   */   }
/* 32:   */   
/* 33:   */   static ClassLoader defaultClassLoader(ClassLoader classLoader)
/* 34:   */   {
/* 35:89 */     if (classLoader != null) {
/* 36:90 */       return classLoader;
/* 37:   */     }
/* 38:93 */     ClassLoader contextClassLoader = PlatformDependent.getContextClassLoader();
/* 39:94 */     if (contextClassLoader != null) {
/* 40:95 */       return contextClassLoader;
/* 41:   */     }
/* 42:98 */     return PlatformDependent.getClassLoader(ClassResolvers.class);
/* 43:   */   }
/* 44:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.serialization.ClassResolvers
 * JD-Core Version:    0.7.0.1
 */