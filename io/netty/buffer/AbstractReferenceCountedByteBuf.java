/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.IllegalReferenceCountException;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*   6:    */ 
/*   7:    */ public abstract class AbstractReferenceCountedByteBuf
/*   8:    */   extends AbstractByteBuf
/*   9:    */ {
/*  10:    */   private static final AtomicIntegerFieldUpdater<AbstractReferenceCountedByteBuf> refCntUpdater;
/*  11:    */   
/*  12:    */   static
/*  13:    */   {
/*  14: 32 */     AtomicIntegerFieldUpdater<AbstractReferenceCountedByteBuf> updater = PlatformDependent.newAtomicIntegerFieldUpdater(AbstractReferenceCountedByteBuf.class, "refCnt");
/*  15: 34 */     if (updater == null) {
/*  16: 35 */       updater = AtomicIntegerFieldUpdater.newUpdater(AbstractReferenceCountedByteBuf.class, "refCnt");
/*  17:    */     }
/*  18: 37 */     refCntUpdater = updater;
/*  19:    */   }
/*  20:    */   
/*  21: 40 */   private volatile int refCnt = 1;
/*  22:    */   
/*  23:    */   protected AbstractReferenceCountedByteBuf(int maxCapacity)
/*  24:    */   {
/*  25: 44 */     super(maxCapacity);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public final int refCnt()
/*  29:    */   {
/*  30: 49 */     return this.refCnt;
/*  31:    */   }
/*  32:    */   
/*  33:    */   protected final void setRefCnt(int refCnt)
/*  34:    */   {
/*  35: 56 */     this.refCnt = refCnt;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public ByteBuf retain()
/*  39:    */   {
/*  40:    */     for (;;)
/*  41:    */     {
/*  42: 62 */       int refCnt = this.refCnt;
/*  43: 63 */       if (refCnt == 0) {
/*  44: 64 */         throw new IllegalReferenceCountException(0, 1);
/*  45:    */       }
/*  46: 66 */       if (refCnt == 2147483647) {
/*  47: 67 */         throw new IllegalReferenceCountException(2147483647, 1);
/*  48:    */       }
/*  49: 69 */       if (refCntUpdater.compareAndSet(this, refCnt, refCnt + 1)) {
/*  50:    */         break;
/*  51:    */       }
/*  52:    */     }
/*  53: 73 */     return this;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public ByteBuf retain(int increment)
/*  57:    */   {
/*  58: 78 */     if (increment <= 0) {
/*  59: 79 */       throw new IllegalArgumentException("increment: " + increment + " (expected: > 0)");
/*  60:    */     }
/*  61:    */     for (;;)
/*  62:    */     {
/*  63: 83 */       int refCnt = this.refCnt;
/*  64: 84 */       if (refCnt == 0) {
/*  65: 85 */         throw new IllegalReferenceCountException(0, increment);
/*  66:    */       }
/*  67: 87 */       if (refCnt > 2147483647 - increment) {
/*  68: 88 */         throw new IllegalReferenceCountException(refCnt, increment);
/*  69:    */       }
/*  70: 90 */       if (refCntUpdater.compareAndSet(this, refCnt, refCnt + increment)) {
/*  71:    */         break;
/*  72:    */       }
/*  73:    */     }
/*  74: 94 */     return this;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public final boolean release()
/*  78:    */   {
/*  79:    */     for (;;)
/*  80:    */     {
/*  81:100 */       int refCnt = this.refCnt;
/*  82:101 */       if (refCnt == 0) {
/*  83:102 */         throw new IllegalReferenceCountException(0, -1);
/*  84:    */       }
/*  85:105 */       if (refCntUpdater.compareAndSet(this, refCnt, refCnt - 1))
/*  86:    */       {
/*  87:106 */         if (refCnt == 1)
/*  88:    */         {
/*  89:107 */           deallocate();
/*  90:108 */           return true;
/*  91:    */         }
/*  92:110 */         return false;
/*  93:    */       }
/*  94:    */     }
/*  95:    */   }
/*  96:    */   
/*  97:    */   public final boolean release(int decrement)
/*  98:    */   {
/*  99:117 */     if (decrement <= 0) {
/* 100:118 */       throw new IllegalArgumentException("decrement: " + decrement + " (expected: > 0)");
/* 101:    */     }
/* 102:    */     for (;;)
/* 103:    */     {
/* 104:122 */       int refCnt = this.refCnt;
/* 105:123 */       if (refCnt < decrement) {
/* 106:124 */         throw new IllegalReferenceCountException(refCnt, -decrement);
/* 107:    */       }
/* 108:127 */       if (refCntUpdater.compareAndSet(this, refCnt, refCnt - decrement))
/* 109:    */       {
/* 110:128 */         if (refCnt == decrement)
/* 111:    */         {
/* 112:129 */           deallocate();
/* 113:130 */           return true;
/* 114:    */         }
/* 115:132 */         return false;
/* 116:    */       }
/* 117:    */     }
/* 118:    */   }
/* 119:    */   
/* 120:    */   protected abstract void deallocate();
/* 121:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.AbstractReferenceCountedByteBuf
 * JD-Core Version:    0.7.0.1
 */