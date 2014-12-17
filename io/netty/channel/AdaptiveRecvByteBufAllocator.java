/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import java.util.ArrayList;
/*   6:    */ import java.util.List;
/*   7:    */ 
/*   8:    */ public class AdaptiveRecvByteBufAllocator
/*   9:    */   implements RecvByteBufAllocator
/*  10:    */ {
/*  11:    */   static final int DEFAULT_MINIMUM = 64;
/*  12:    */   static final int DEFAULT_INITIAL = 1024;
/*  13:    */   static final int DEFAULT_MAXIMUM = 65536;
/*  14:    */   private static final int INDEX_INCREMENT = 4;
/*  15:    */   private static final int INDEX_DECREMENT = 1;
/*  16:    */   private static final int[] SIZE_TABLE;
/*  17:    */   
/*  18:    */   static
/*  19:    */   {
/*  20: 46 */     List<Integer> sizeTable = new ArrayList();
/*  21: 47 */     for (int i = 16; i < 512; i += 16) {
/*  22: 48 */       sizeTable.add(Integer.valueOf(i));
/*  23:    */     }
/*  24: 51 */     for (int i = 512; i > 0; i <<= 1) {
/*  25: 52 */       sizeTable.add(Integer.valueOf(i));
/*  26:    */     }
/*  27: 55 */     SIZE_TABLE = new int[sizeTable.size()];
/*  28: 56 */     for (int i = 0; i < SIZE_TABLE.length; i++) {
/*  29: 57 */       SIZE_TABLE[i] = ((Integer)sizeTable.get(i)).intValue();
/*  30:    */     }
/*  31:    */   }
/*  32:    */   
/*  33: 61 */   public static final AdaptiveRecvByteBufAllocator DEFAULT = new AdaptiveRecvByteBufAllocator();
/*  34:    */   private final int minIndex;
/*  35:    */   private final int maxIndex;
/*  36:    */   private final int initial;
/*  37:    */   
/*  38:    */   private static int getSizeTableIndex(int size)
/*  39:    */   {
/*  40: 64 */     int low = 0;int high = SIZE_TABLE.length - 1;
/*  41:    */     for (;;)
/*  42:    */     {
/*  43: 65 */       if (high < low) {
/*  44: 66 */         return low;
/*  45:    */       }
/*  46: 68 */       if (high == low) {
/*  47: 69 */         return high;
/*  48:    */       }
/*  49: 72 */       int mid = low + high >>> 1;
/*  50: 73 */       int a = SIZE_TABLE[mid];
/*  51: 74 */       int b = SIZE_TABLE[(mid + 1)];
/*  52: 75 */       if (size > b)
/*  53:    */       {
/*  54: 76 */         low = mid + 1;
/*  55:    */       }
/*  56: 77 */       else if (size < a)
/*  57:    */       {
/*  58: 78 */         high = mid - 1;
/*  59:    */       }
/*  60:    */       else
/*  61:    */       {
/*  62: 79 */         if (size == a) {
/*  63: 80 */           return mid;
/*  64:    */         }
/*  65: 82 */         return mid + 1;
/*  66:    */       }
/*  67:    */     }
/*  68:    */   }
/*  69:    */   
/*  70:    */   private static final class HandleImpl
/*  71:    */     implements RecvByteBufAllocator.Handle
/*  72:    */   {
/*  73:    */     private final int minIndex;
/*  74:    */     private final int maxIndex;
/*  75:    */     private int index;
/*  76:    */     private int nextReceiveBufferSize;
/*  77:    */     private boolean decreaseNow;
/*  78:    */     
/*  79:    */     HandleImpl(int minIndex, int maxIndex, int initial)
/*  80:    */     {
/*  81: 95 */       this.minIndex = minIndex;
/*  82: 96 */       this.maxIndex = maxIndex;
/*  83:    */       
/*  84: 98 */       this.index = AdaptiveRecvByteBufAllocator.getSizeTableIndex(initial);
/*  85: 99 */       this.nextReceiveBufferSize = AdaptiveRecvByteBufAllocator.SIZE_TABLE[this.index];
/*  86:    */     }
/*  87:    */     
/*  88:    */     public ByteBuf allocate(ByteBufAllocator alloc)
/*  89:    */     {
/*  90:104 */       return alloc.ioBuffer(this.nextReceiveBufferSize);
/*  91:    */     }
/*  92:    */     
/*  93:    */     public int guess()
/*  94:    */     {
/*  95:109 */       return this.nextReceiveBufferSize;
/*  96:    */     }
/*  97:    */     
/*  98:    */     public void record(int actualReadBytes)
/*  99:    */     {
/* 100:114 */       if (actualReadBytes <= AdaptiveRecvByteBufAllocator.SIZE_TABLE[Math.max(0, this.index - 1 - 1)])
/* 101:    */       {
/* 102:115 */         if (this.decreaseNow)
/* 103:    */         {
/* 104:116 */           this.index = Math.max(this.index - 1, this.minIndex);
/* 105:117 */           this.nextReceiveBufferSize = AdaptiveRecvByteBufAllocator.SIZE_TABLE[this.index];
/* 106:118 */           this.decreaseNow = false;
/* 107:    */         }
/* 108:    */         else
/* 109:    */         {
/* 110:120 */           this.decreaseNow = true;
/* 111:    */         }
/* 112:    */       }
/* 113:122 */       else if (actualReadBytes >= this.nextReceiveBufferSize)
/* 114:    */       {
/* 115:123 */         this.index = Math.min(this.index + 4, this.maxIndex);
/* 116:124 */         this.nextReceiveBufferSize = AdaptiveRecvByteBufAllocator.SIZE_TABLE[this.index];
/* 117:125 */         this.decreaseNow = false;
/* 118:    */       }
/* 119:    */     }
/* 120:    */   }
/* 121:    */   
/* 122:    */   private AdaptiveRecvByteBufAllocator()
/* 123:    */   {
/* 124:140 */     this(64, 1024, 65536);
/* 125:    */   }
/* 126:    */   
/* 127:    */   public AdaptiveRecvByteBufAllocator(int minimum, int initial, int maximum)
/* 128:    */   {
/* 129:151 */     if (minimum <= 0) {
/* 130:152 */       throw new IllegalArgumentException("minimum: " + minimum);
/* 131:    */     }
/* 132:154 */     if (initial < minimum) {
/* 133:155 */       throw new IllegalArgumentException("initial: " + initial);
/* 134:    */     }
/* 135:157 */     if (maximum < initial) {
/* 136:158 */       throw new IllegalArgumentException("maximum: " + maximum);
/* 137:    */     }
/* 138:161 */     int minIndex = getSizeTableIndex(minimum);
/* 139:162 */     if (SIZE_TABLE[minIndex] < minimum) {
/* 140:163 */       this.minIndex = (minIndex + 1);
/* 141:    */     } else {
/* 142:165 */       this.minIndex = minIndex;
/* 143:    */     }
/* 144:168 */     int maxIndex = getSizeTableIndex(maximum);
/* 145:169 */     if (SIZE_TABLE[maxIndex] > maximum) {
/* 146:170 */       this.maxIndex = (maxIndex - 1);
/* 147:    */     } else {
/* 148:172 */       this.maxIndex = maxIndex;
/* 149:    */     }
/* 150:175 */     this.initial = initial;
/* 151:    */   }
/* 152:    */   
/* 153:    */   public RecvByteBufAllocator.Handle newHandle()
/* 154:    */   {
/* 155:180 */     return new HandleImpl(this.minIndex, this.maxIndex, this.initial);
/* 156:    */   }
/* 157:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.AdaptiveRecvByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */