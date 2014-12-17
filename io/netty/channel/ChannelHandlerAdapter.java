/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.InternalThreadLocalMap;
/*  4:   */ import java.util.Map;
/*  5:   */ 
/*  6:   */ public abstract class ChannelHandlerAdapter
/*  7:   */   implements ChannelHandler
/*  8:   */ {
/*  9:   */   boolean added;
/* 10:   */   
/* 11:   */   public boolean isSharable()
/* 12:   */   {
/* 13:45 */     Class<?> clazz = getClass();
/* 14:46 */     Map<Class<?>, Boolean> cache = InternalThreadLocalMap.get().handlerSharableCache();
/* 15:47 */     Boolean sharable = (Boolean)cache.get(clazz);
/* 16:48 */     if (sharable == null)
/* 17:   */     {
/* 18:49 */       sharable = Boolean.valueOf(clazz.isAnnotationPresent(ChannelHandler.Sharable.class));
/* 19:50 */       cache.put(clazz, sharable);
/* 20:   */     }
/* 21:52 */     return sharable.booleanValue();
/* 22:   */   }
/* 23:   */   
/* 24:   */   public void handlerAdded(ChannelHandlerContext ctx)
/* 25:   */     throws Exception
/* 26:   */   {}
/* 27:   */   
/* 28:   */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 29:   */     throws Exception
/* 30:   */   {}
/* 31:   */   
/* 32:   */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 33:   */     throws Exception
/* 34:   */   {
/* 35:79 */     ctx.fireExceptionCaught(cause);
/* 36:   */   }
/* 37:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.ChannelHandlerAdapter
 * JD-Core Version:    0.7.0.1
 */