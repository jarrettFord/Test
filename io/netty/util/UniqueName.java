/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import java.util.concurrent.ConcurrentMap;
/*   4:    */ import java.util.concurrent.atomic.AtomicInteger;
/*   5:    */ 
/*   6:    */ @Deprecated
/*   7:    */ public class UniqueName
/*   8:    */   implements Comparable<UniqueName>
/*   9:    */ {
/*  10: 29 */   private static final AtomicInteger nextId = new AtomicInteger();
/*  11:    */   private final int id;
/*  12:    */   private final String name;
/*  13:    */   
/*  14:    */   public UniqueName(ConcurrentMap<String, Boolean> map, String name, Object... args)
/*  15:    */   {
/*  16: 42 */     if (map == null) {
/*  17: 43 */       throw new NullPointerException("map");
/*  18:    */     }
/*  19: 45 */     if (name == null) {
/*  20: 46 */       throw new NullPointerException("name");
/*  21:    */     }
/*  22: 48 */     if ((args != null) && (args.length > 0)) {
/*  23: 49 */       validateArgs(args);
/*  24:    */     }
/*  25: 52 */     if (map.putIfAbsent(name, Boolean.TRUE) != null) {
/*  26: 53 */       throw new IllegalArgumentException(String.format("'%s' is already in use", new Object[] { name }));
/*  27:    */     }
/*  28: 56 */     this.id = nextId.incrementAndGet();
/*  29: 57 */     this.name = name;
/*  30:    */   }
/*  31:    */   
/*  32:    */   protected void validateArgs(Object... args) {}
/*  33:    */   
/*  34:    */   public final String name()
/*  35:    */   {
/*  36: 77 */     return this.name;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public final int id()
/*  40:    */   {
/*  41: 86 */     return this.id;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public final int hashCode()
/*  45:    */   {
/*  46: 91 */     return super.hashCode();
/*  47:    */   }
/*  48:    */   
/*  49:    */   public final boolean equals(Object o)
/*  50:    */   {
/*  51: 96 */     return super.equals(o);
/*  52:    */   }
/*  53:    */   
/*  54:    */   public int compareTo(UniqueName other)
/*  55:    */   {
/*  56:101 */     if (this == other) {
/*  57:102 */       return 0;
/*  58:    */     }
/*  59:105 */     int returnCode = this.name.compareTo(other.name);
/*  60:106 */     if (returnCode != 0) {
/*  61:107 */       return returnCode;
/*  62:    */     }
/*  63:110 */     return Integer.valueOf(this.id).compareTo(Integer.valueOf(other.id));
/*  64:    */   }
/*  65:    */   
/*  66:    */   public String toString()
/*  67:    */   {
/*  68:115 */     return name();
/*  69:    */   }
/*  70:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.UniqueName
 * JD-Core Version:    0.7.0.1
 */