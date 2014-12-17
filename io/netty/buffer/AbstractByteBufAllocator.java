/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.ResourceLeak;
/*   4:    */ import io.netty.util.ResourceLeakDetector;
/*   5:    */ import io.netty.util.internal.PlatformDependent;
/*   6:    */ import io.netty.util.internal.StringUtil;
/*   7:    */ 
/*   8:    */ public abstract class AbstractByteBufAllocator
/*   9:    */   implements ByteBufAllocator
/*  10:    */ {
/*  11:    */   private static final int DEFAULT_INITIAL_CAPACITY = 256;
/*  12:    */   private static final int DEFAULT_MAX_COMPONENTS = 16;
/*  13:    */   private final boolean directByDefault;
/*  14:    */   private final ByteBuf emptyBuf;
/*  15:    */   
/*  16:    */   protected static ByteBuf toLeakAwareBuffer(ByteBuf buf)
/*  17:    */   {
/*  18:    */     ResourceLeak leak;
/*  19: 33 */     switch (1.$SwitchMap$io$netty$util$ResourceLeakDetector$Level[ResourceLeakDetector.getLevel().ordinal()])
/*  20:    */     {
/*  21:    */     case 1: 
/*  22: 35 */       leak = AbstractByteBuf.leakDetector.open(buf);
/*  23: 36 */       if (leak != null) {
/*  24: 37 */         buf = new SimpleLeakAwareByteBuf(buf, leak);
/*  25:    */       }
/*  26:    */       break;
/*  27:    */     case 2: 
/*  28:    */     case 3: 
/*  29: 42 */       leak = AbstractByteBuf.leakDetector.open(buf);
/*  30: 43 */       if (leak != null) {
/*  31: 44 */         buf = new AdvancedLeakAwareByteBuf(buf, leak);
/*  32:    */       }
/*  33:    */       break;
/*  34:    */     }
/*  35: 48 */     return buf;
/*  36:    */   }
/*  37:    */   
/*  38:    */   protected AbstractByteBufAllocator()
/*  39:    */   {
/*  40: 58 */     this(false);
/*  41:    */   }
/*  42:    */   
/*  43:    */   protected AbstractByteBufAllocator(boolean preferDirect)
/*  44:    */   {
/*  45: 68 */     this.directByDefault = ((preferDirect) && (PlatformDependent.hasUnsafe()));
/*  46: 69 */     this.emptyBuf = new EmptyByteBuf(this);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public ByteBuf buffer()
/*  50:    */   {
/*  51: 74 */     if (this.directByDefault) {
/*  52: 75 */       return directBuffer();
/*  53:    */     }
/*  54: 77 */     return heapBuffer();
/*  55:    */   }
/*  56:    */   
/*  57:    */   public ByteBuf buffer(int initialCapacity)
/*  58:    */   {
/*  59: 82 */     if (this.directByDefault) {
/*  60: 83 */       return directBuffer(initialCapacity);
/*  61:    */     }
/*  62: 85 */     return heapBuffer(initialCapacity);
/*  63:    */   }
/*  64:    */   
/*  65:    */   public ByteBuf buffer(int initialCapacity, int maxCapacity)
/*  66:    */   {
/*  67: 90 */     if (this.directByDefault) {
/*  68: 91 */       return directBuffer(initialCapacity, maxCapacity);
/*  69:    */     }
/*  70: 93 */     return heapBuffer(initialCapacity, maxCapacity);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public ByteBuf ioBuffer()
/*  74:    */   {
/*  75: 98 */     if (PlatformDependent.hasUnsafe()) {
/*  76: 99 */       return directBuffer(256);
/*  77:    */     }
/*  78:101 */     return heapBuffer(256);
/*  79:    */   }
/*  80:    */   
/*  81:    */   public ByteBuf ioBuffer(int initialCapacity)
/*  82:    */   {
/*  83:106 */     if (PlatformDependent.hasUnsafe()) {
/*  84:107 */       return directBuffer(initialCapacity);
/*  85:    */     }
/*  86:109 */     return heapBuffer(initialCapacity);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public ByteBuf ioBuffer(int initialCapacity, int maxCapacity)
/*  90:    */   {
/*  91:114 */     if (PlatformDependent.hasUnsafe()) {
/*  92:115 */       return directBuffer(initialCapacity, maxCapacity);
/*  93:    */     }
/*  94:117 */     return heapBuffer(initialCapacity, maxCapacity);
/*  95:    */   }
/*  96:    */   
/*  97:    */   public ByteBuf heapBuffer()
/*  98:    */   {
/*  99:122 */     return heapBuffer(256, 2147483647);
/* 100:    */   }
/* 101:    */   
/* 102:    */   public ByteBuf heapBuffer(int initialCapacity)
/* 103:    */   {
/* 104:127 */     return heapBuffer(initialCapacity, 2147483647);
/* 105:    */   }
/* 106:    */   
/* 107:    */   public ByteBuf heapBuffer(int initialCapacity, int maxCapacity)
/* 108:    */   {
/* 109:132 */     if ((initialCapacity == 0) && (maxCapacity == 0)) {
/* 110:133 */       return this.emptyBuf;
/* 111:    */     }
/* 112:135 */     validate(initialCapacity, maxCapacity);
/* 113:136 */     return newHeapBuffer(initialCapacity, maxCapacity);
/* 114:    */   }
/* 115:    */   
/* 116:    */   public ByteBuf directBuffer()
/* 117:    */   {
/* 118:141 */     return directBuffer(256, 2147483647);
/* 119:    */   }
/* 120:    */   
/* 121:    */   public ByteBuf directBuffer(int initialCapacity)
/* 122:    */   {
/* 123:146 */     return directBuffer(initialCapacity, 2147483647);
/* 124:    */   }
/* 125:    */   
/* 126:    */   public ByteBuf directBuffer(int initialCapacity, int maxCapacity)
/* 127:    */   {
/* 128:151 */     if ((initialCapacity == 0) && (maxCapacity == 0)) {
/* 129:152 */       return this.emptyBuf;
/* 130:    */     }
/* 131:154 */     validate(initialCapacity, maxCapacity);
/* 132:155 */     return newDirectBuffer(initialCapacity, maxCapacity);
/* 133:    */   }
/* 134:    */   
/* 135:    */   public CompositeByteBuf compositeBuffer()
/* 136:    */   {
/* 137:160 */     if (this.directByDefault) {
/* 138:161 */       return compositeDirectBuffer();
/* 139:    */     }
/* 140:163 */     return compositeHeapBuffer();
/* 141:    */   }
/* 142:    */   
/* 143:    */   public CompositeByteBuf compositeBuffer(int maxNumComponents)
/* 144:    */   {
/* 145:168 */     if (this.directByDefault) {
/* 146:169 */       return compositeDirectBuffer(maxNumComponents);
/* 147:    */     }
/* 148:171 */     return compositeHeapBuffer(maxNumComponents);
/* 149:    */   }
/* 150:    */   
/* 151:    */   public CompositeByteBuf compositeHeapBuffer()
/* 152:    */   {
/* 153:176 */     return compositeHeapBuffer(16);
/* 154:    */   }
/* 155:    */   
/* 156:    */   public CompositeByteBuf compositeHeapBuffer(int maxNumComponents)
/* 157:    */   {
/* 158:181 */     return new CompositeByteBuf(this, false, maxNumComponents);
/* 159:    */   }
/* 160:    */   
/* 161:    */   public CompositeByteBuf compositeDirectBuffer()
/* 162:    */   {
/* 163:186 */     return compositeDirectBuffer(16);
/* 164:    */   }
/* 165:    */   
/* 166:    */   public CompositeByteBuf compositeDirectBuffer(int maxNumComponents)
/* 167:    */   {
/* 168:191 */     return new CompositeByteBuf(this, true, maxNumComponents);
/* 169:    */   }
/* 170:    */   
/* 171:    */   private static void validate(int initialCapacity, int maxCapacity)
/* 172:    */   {
/* 173:195 */     if (initialCapacity < 0) {
/* 174:196 */       throw new IllegalArgumentException("initialCapacity: " + initialCapacity + " (expectd: 0+)");
/* 175:    */     }
/* 176:198 */     if (initialCapacity > maxCapacity) {
/* 177:199 */       throw new IllegalArgumentException(String.format("initialCapacity: %d (expected: not greater than maxCapacity(%d)", new Object[] { Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) }));
/* 178:    */     }
/* 179:    */   }
/* 180:    */   
/* 181:    */   protected abstract ByteBuf newHeapBuffer(int paramInt1, int paramInt2);
/* 182:    */   
/* 183:    */   protected abstract ByteBuf newDirectBuffer(int paramInt1, int paramInt2);
/* 184:    */   
/* 185:    */   public String toString()
/* 186:    */   {
/* 187:217 */     return StringUtil.simpleClassName(this) + "(directByDefault: " + this.directByDefault + ')';
/* 188:    */   }
/* 189:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.AbstractByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */