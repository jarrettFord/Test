/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.logging.InternalLogger;
/*  4:   */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  5:   */ import java.lang.reflect.Method;
/*  6:   */ import javassist.ClassClassPath;
/*  7:   */ import javassist.ClassPath;
/*  8:   */ import javassist.ClassPool;
/*  9:   */ import javassist.CtClass;
/* 10:   */ import javassist.CtMethod;
/* 11:   */ import javassist.NotFoundException;
/* 12:   */ 
/* 13:   */ public final class JavassistTypeParameterMatcherGenerator
/* 14:   */ {
/* 15:32 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(JavassistTypeParameterMatcherGenerator.class);
/* 16:35 */   private static final ClassPool classPool = new ClassPool(true);
/* 17:   */   
/* 18:   */   static
/* 19:   */   {
/* 20:38 */     classPool.appendClassPath(new ClassClassPath(NoOpTypeParameterMatcher.class));
/* 21:   */   }
/* 22:   */   
/* 23:   */   public static void appendClassPath(ClassPath classpath)
/* 24:   */   {
/* 25:42 */     classPool.appendClassPath(classpath);
/* 26:   */   }
/* 27:   */   
/* 28:   */   public static void appendClassPath(String pathname)
/* 29:   */     throws NotFoundException
/* 30:   */   {
/* 31:46 */     classPool.appendClassPath(pathname);
/* 32:   */   }
/* 33:   */   
/* 34:   */   public static TypeParameterMatcher generate(Class<?> type)
/* 35:   */   {
/* 36:50 */     ClassLoader classLoader = PlatformDependent.getContextClassLoader();
/* 37:51 */     if (classLoader == null) {
/* 38:52 */       classLoader = PlatformDependent.getSystemClassLoader();
/* 39:   */     }
/* 40:54 */     return generate(type, classLoader);
/* 41:   */   }
/* 42:   */   
/* 43:   */   public static TypeParameterMatcher generate(Class<?> type, ClassLoader classLoader)
/* 44:   */   {
/* 45:58 */     String typeName = typeName(type);
/* 46:59 */     String className = "io.netty.util.internal.__matchers__." + typeName + "Matcher";
/* 47:   */     try
/* 48:   */     {
/* 49:62 */       return (TypeParameterMatcher)Class.forName(className, true, classLoader).newInstance();
/* 50:   */     }
/* 51:   */     catch (Exception e)
/* 52:   */     {
/* 53:67 */       CtClass c = classPool.getAndRename(NoOpTypeParameterMatcher.class.getName(), className);
/* 54:68 */       c.setModifiers(c.getModifiers() | 0x10);
/* 55:69 */       c.getDeclaredMethod("match").setBody("{ return $1 instanceof " + typeName + "; }");
/* 56:70 */       byte[] byteCode = c.toBytecode();
/* 57:71 */       c.detach();
/* 58:72 */       Method method = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] { String.class, [B.class, Integer.TYPE, Integer.TYPE });
/* 59:   */       
/* 60:74 */       method.setAccessible(true);
/* 61:   */       
/* 62:76 */       Class<?> generated = (Class)method.invoke(classLoader, new Object[] { className, byteCode, Integer.valueOf(0), Integer.valueOf(byteCode.length) });
/* 63:77 */       if (type != Object.class) {
/* 64:78 */         logger.debug("Generated: {}", generated.getName());
/* 65:   */       }
/* 66:82 */       return (TypeParameterMatcher)generated.newInstance();
/* 67:   */     }
/* 68:   */     catch (RuntimeException e)
/* 69:   */     {
/* 70:84 */       throw e;
/* 71:   */     }
/* 72:   */     catch (Exception e)
/* 73:   */     {
/* 74:86 */       throw new RuntimeException(e);
/* 75:   */     }
/* 76:   */   }
/* 77:   */   
/* 78:   */   private static String typeName(Class<?> type)
/* 79:   */   {
/* 80:91 */     if (type.isArray()) {
/* 81:92 */       return typeName(type.getComponentType()) + "[]";
/* 82:   */     }
/* 83:95 */     return type.getName();
/* 84:   */   }
/* 85:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.JavassistTypeParameterMatcherGenerator
 * JD-Core Version:    0.7.0.1
 */