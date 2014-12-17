/*  1:   */ package io.netty.handler.ssl.util;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ThreadLocalRandom;
/*  4:   */ import java.security.SecureRandom;
/*  5:   */ import java.util.Random;
/*  6:   */ 
/*  7:   */ final class ThreadLocalInsecureRandom
/*  8:   */   extends SecureRandom
/*  9:   */ {
/* 10:   */   private static final long serialVersionUID = -8209473337192526191L;
/* 11:31 */   private static final SecureRandom INSTANCE = new ThreadLocalInsecureRandom();
/* 12:   */   
/* 13:   */   static SecureRandom current()
/* 14:   */   {
/* 15:34 */     return INSTANCE;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public String getAlgorithm()
/* 19:   */   {
/* 20:41 */     return "insecure";
/* 21:   */   }
/* 22:   */   
/* 23:   */   public void setSeed(byte[] seed) {}
/* 24:   */   
/* 25:   */   public void setSeed(long seed) {}
/* 26:   */   
/* 27:   */   public void nextBytes(byte[] bytes)
/* 28:   */   {
/* 29:52 */     random().nextBytes(bytes);
/* 30:   */   }
/* 31:   */   
/* 32:   */   public byte[] generateSeed(int numBytes)
/* 33:   */   {
/* 34:57 */     byte[] seed = new byte[numBytes];
/* 35:58 */     random().nextBytes(seed);
/* 36:59 */     return seed;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public int nextInt()
/* 40:   */   {
/* 41:64 */     return random().nextInt();
/* 42:   */   }
/* 43:   */   
/* 44:   */   public int nextInt(int n)
/* 45:   */   {
/* 46:69 */     return random().nextInt(n);
/* 47:   */   }
/* 48:   */   
/* 49:   */   public boolean nextBoolean()
/* 50:   */   {
/* 51:74 */     return random().nextBoolean();
/* 52:   */   }
/* 53:   */   
/* 54:   */   public long nextLong()
/* 55:   */   {
/* 56:79 */     return random().nextLong();
/* 57:   */   }
/* 58:   */   
/* 59:   */   public float nextFloat()
/* 60:   */   {
/* 61:84 */     return random().nextFloat();
/* 62:   */   }
/* 63:   */   
/* 64:   */   public double nextDouble()
/* 65:   */   {
/* 66:89 */     return random().nextDouble();
/* 67:   */   }
/* 68:   */   
/* 69:   */   public double nextGaussian()
/* 70:   */   {
/* 71:94 */     return random().nextGaussian();
/* 72:   */   }
/* 73:   */   
/* 74:   */   private static Random random()
/* 75:   */   {
/* 76:98 */     return ThreadLocalRandom.current();
/* 77:   */   }
/* 78:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.util.ThreadLocalInsecureRandom
 * JD-Core Version:    0.7.0.1
 */