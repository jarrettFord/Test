/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import java.nio.charset.Charset;
/*  4:   */ import java.nio.charset.CharsetDecoder;
/*  5:   */ import java.nio.charset.CharsetEncoder;
/*  6:   */ import java.util.Map;
/*  7:   */ import java.util.concurrent.atomic.AtomicInteger;
/*  8:   */ 
/*  9:   */ class UnpaddedInternalThreadLocalMap
/* 10:   */ {
/* 11:   */   static ThreadLocal<InternalThreadLocalMap> slowThreadLocalMap;
/* 12:35 */   static final AtomicInteger nextIndex = new AtomicInteger();
/* 13:   */   Object[] indexedVariables;
/* 14:   */   int futureListenerStackDepth;
/* 15:   */   int localChannelReaderStackDepth;
/* 16:   */   Map<Class<?>, Boolean> handlerSharableCache;
/* 17:   */   IntegerHolder counterHashCode;
/* 18:   */   ThreadLocalRandom random;
/* 19:   */   Map<Class<?>, TypeParameterMatcher> typeParameterMatcherGetCache;
/* 20:   */   Map<Class<?>, Map<String, TypeParameterMatcher>> typeParameterMatcherFindCache;
/* 21:   */   StringBuilder stringBuilder;
/* 22:   */   Map<Charset, CharsetEncoder> charsetEncoderCache;
/* 23:   */   Map<Charset, CharsetDecoder> charsetDecoderCache;
/* 24:   */   
/* 25:   */   UnpaddedInternalThreadLocalMap(Object[] indexedVariables)
/* 26:   */   {
/* 27:55 */     this.indexedVariables = indexedVariables;
/* 28:   */   }
/* 29:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.UnpaddedInternalThreadLocalMap
 * JD-Core Version:    0.7.0.1
 */