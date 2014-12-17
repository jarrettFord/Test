/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*   5:    */ 
/*   6:    */ public abstract class AbstractReferenceCounted
/*   7:    */   implements ReferenceCounted
/*   8:    */ {
/*   9:    */   private static final AtomicIntegerFieldUpdater<AbstractReferenceCounted> refCntUpdater;
/*  10:    */   
/*  11:    */   static
/*  12:    */   {
/*  13: 30 */     AtomicIntegerFieldUpdater<AbstractReferenceCounted> updater = PlatformDependent.newAtomicIntegerFieldUpdater(AbstractReferenceCounted.class, "refCnt");
/*  14: 32 */     if (updater == null) {
/*  15: 33 */       updater = AtomicIntegerFieldUpdater.newUpdater(AbstractReferenceCounted.class, "refCnt");
/*  16:    */     }
/*  17: 35 */     refCntUpdater = updater;
/*  18:    */   }
/*  19:    */   
/*  20: 38 */   private volatile int refCnt = 1;
/*  21:    */   
/*  22:    */   public final int refCnt()
/*  23:    */   {
/*  24: 43 */     return this.refCnt;
/*  25:    */   }
/*  26:    */   
/*  27:    */   protected final void setRefCnt(int refCnt)
/*  28:    */   {
/*  29: 50 */     this.refCnt = refCnt;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public ReferenceCounted retain()
/*  33:    */   {
/*  34:    */     for (;;)
/*  35:    */     {
/*  36: 56 */       int refCnt = this.refCnt;
/*  37: 57 */       if (refCnt == 0) {
/*  38: 58 */         throw new IllegalReferenceCountException(0, 1);
/*  39:    */       }
/*  40: 60 */       if (refCnt == 2147483647) {
/*  41: 61 */         throw new IllegalReferenceCountException(2147483647, 1);
/*  42:    */       }
/*  43: 63 */       if (refCntUpdater.compareAndSet(this, refCnt, refCnt + 1)) {
/*  44:    */         break;
/*  45:    */       }
/*  46:    */     }
/*  47: 67 */     return this;
/*  48:    */   }
/*  49:    */   
/*  50:    */   public ReferenceCounted retain(int increment)
/*  51:    */   {
/*  52: 72 */     if (increment <= 0) {
/*  53: 73 */       throw new IllegalArgumentException("increment: " + increment + " (expected: > 0)");
/*  54:    */     }
/*  55:    */     for (;;)
/*  56:    */     {
/*  57: 77 */       int refCnt = this.refCnt;
/*  58: 78 */       if (refCnt == 0) {
/*  59: 79 */         throw new IllegalReferenceCountException(0, 1);
/*  60:    */       }
/*  61: 81 */       if (refCnt > 2147483647 - increment) {
/*  62: 82 */         throw new IllegalReferenceCountException(refCnt, increment);
/*  63:    */       }
/*  64: 84 */       if (refCntUpdater.compareAndSet(this, refCnt, refCnt + increment)) {
/*  65:    */         break;
/*  66:    */       }
/*  67:    */     }
/*  68: 88 */     return this;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public final boolean release()
/*  72:    */   {
/*  73:    */     for (;;)
/*  74:    */     {
/*  75: 94 */       int refCnt = this.refCnt;
/*  76: 95 */       if (refCnt == 0) {
/*  77: 96 */         throw new IllegalReferenceCountException(0, -1);
/*  78:    */       }
/*  79: 99 */       if (refCntUpdater.compareAndSet(this, refCnt, refCnt - 1))
/*  80:    */       {
/*  81:100 */         if (refCnt == 1)
/*  82:    */         {
/*  83:101 */           deallocate();
/*  84:102 */           return true;
/*  85:    */         }
/*  86:104 */         return false;
/*  87:    */       }
/*  88:    */     }
/*  89:    */   }
/*  90:    */   
/*  91:    */   public final boolean release(int decrement)
/*  92:    */   {
/*  93:111 */     if (decrement <= 0) {
/*  94:112 */       throw new IllegalArgumentException("decrement: " + decrement + " (expected: > 0)");
/*  95:    */     }
/*  96:    */     for (;;)
/*  97:    */     {
/*  98:116 */       int refCnt = this.refCnt;
/*  99:117 */       if (refCnt < decrement) {
/* 100:118 */         throw new IllegalReferenceCountException(refCnt, -decrement);
/* 101:    */       }
/* 102:121 */       if (refCntUpdater.compareAndSet(this, refCnt, refCnt - decrement))
/* 103:    */       {
/* 104:122 */         if (refCnt == decrement)
/* 105:    */         {
/* 106:123 */           deallocate();
/* 107:124 */           return true;
/* 108:    */         }
/* 109:126 */         return false;
/* 110:    */       }
/* 111:    */     }
/* 112:    */   }
/* 113:    */   
/* 114:    */   protected abstract void deallocate();
/* 115:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.AbstractReferenceCounted
 * JD-Core Version:    0.7.0.1
 */