/*  1:   */ package io.netty.handler.codec;
/*  2:   */ 
/*  3:   */ public class UnsupportedMessageTypeException
/*  4:   */   extends CodecException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 2799598826487038726L;
/*  7:   */   
/*  8:   */   public UnsupportedMessageTypeException(Object message, Class<?>... expectedTypes)
/*  9:   */   {
/* 10:27 */     super(message(message == null ? "null" : message.getClass().getName(), expectedTypes));
/* 11:   */   }
/* 12:   */   
/* 13:   */   public UnsupportedMessageTypeException() {}
/* 14:   */   
/* 15:   */   public UnsupportedMessageTypeException(String message, Throwable cause)
/* 16:   */   {
/* 17:34 */     super(message, cause);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public UnsupportedMessageTypeException(String s)
/* 21:   */   {
/* 22:38 */     super(s);
/* 23:   */   }
/* 24:   */   
/* 25:   */   public UnsupportedMessageTypeException(Throwable cause)
/* 26:   */   {
/* 27:42 */     super(cause);
/* 28:   */   }
/* 29:   */   
/* 30:   */   private static String message(String actualType, Class<?>... expectedTypes)
/* 31:   */   {
/* 32:47 */     StringBuilder buf = new StringBuilder(actualType);
/* 33:49 */     if ((expectedTypes != null) && (expectedTypes.length > 0))
/* 34:   */     {
/* 35:50 */       buf.append(" (expected: ").append(expectedTypes[0].getName());
/* 36:51 */       for (int i = 1; i < expectedTypes.length; i++)
/* 37:   */       {
/* 38:52 */         Class<?> t = expectedTypes[i];
/* 39:53 */         if (t == null) {
/* 40:   */           break;
/* 41:   */         }
/* 42:56 */         buf.append(", ").append(t.getName());
/* 43:   */       }
/* 44:58 */       buf.append(')');
/* 45:   */     }
/* 46:61 */     return buf.toString();
/* 47:   */   }
/* 48:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.UnsupportedMessageTypeException
 * JD-Core Version:    0.7.0.1
 */