/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.Recycler;
/*   4:    */ import io.netty.util.Recycler.Handle;
/*   5:    */ import java.nio.ByteBuffer;
/*   6:    */ import java.nio.ByteOrder;
/*   7:    */ 
/*   8:    */ abstract class PooledByteBuf<T>
/*   9:    */   extends AbstractReferenceCountedByteBuf
/*  10:    */ {
/*  11:    */   private final Recycler.Handle recyclerHandle;
/*  12:    */   protected PoolChunk<T> chunk;
/*  13:    */   protected long handle;
/*  14:    */   protected T memory;
/*  15:    */   protected int offset;
/*  16:    */   protected int length;
/*  17:    */   int maxLength;
/*  18:    */   private ByteBuffer tmpNioBuf;
/*  19:    */   
/*  20:    */   protected PooledByteBuf(Recycler.Handle recyclerHandle, int maxCapacity)
/*  21:    */   {
/*  22: 38 */     super(maxCapacity);
/*  23: 39 */     this.recyclerHandle = recyclerHandle;
/*  24:    */   }
/*  25:    */   
/*  26:    */   void init(PoolChunk<T> chunk, long handle, int offset, int length, int maxLength)
/*  27:    */   {
/*  28: 43 */     assert (handle >= 0L);
/*  29: 44 */     assert (chunk != null);
/*  30:    */     
/*  31: 46 */     this.chunk = chunk;
/*  32: 47 */     this.handle = handle;
/*  33: 48 */     this.memory = chunk.memory;
/*  34: 49 */     this.offset = offset;
/*  35: 50 */     this.length = length;
/*  36: 51 */     this.maxLength = maxLength;
/*  37: 52 */     setIndex(0, 0);
/*  38: 53 */     this.tmpNioBuf = null;
/*  39:    */   }
/*  40:    */   
/*  41:    */   void initUnpooled(PoolChunk<T> chunk, int length)
/*  42:    */   {
/*  43: 57 */     assert (chunk != null);
/*  44:    */     
/*  45: 59 */     this.chunk = chunk;
/*  46: 60 */     this.handle = 0L;
/*  47: 61 */     this.memory = chunk.memory;
/*  48: 62 */     this.offset = 0;
/*  49: 63 */     this.length = (this.maxLength = length);
/*  50: 64 */     setIndex(0, 0);
/*  51: 65 */     this.tmpNioBuf = null;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public final int capacity()
/*  55:    */   {
/*  56: 70 */     return this.length;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public final ByteBuf capacity(int newCapacity)
/*  60:    */   {
/*  61: 75 */     ensureAccessible();
/*  62: 78 */     if (this.chunk.unpooled)
/*  63:    */     {
/*  64: 79 */       if (newCapacity == this.length) {
/*  65: 80 */         return this;
/*  66:    */       }
/*  67:    */     }
/*  68: 83 */     else if (newCapacity > this.length)
/*  69:    */     {
/*  70: 84 */       if (newCapacity <= this.maxLength)
/*  71:    */       {
/*  72: 85 */         this.length = newCapacity;
/*  73: 86 */         return this;
/*  74:    */       }
/*  75:    */     }
/*  76: 88 */     else if (newCapacity < this.length)
/*  77:    */     {
/*  78: 89 */       if (newCapacity > this.maxLength >>> 1) {
/*  79: 90 */         if (this.maxLength <= 512)
/*  80:    */         {
/*  81: 91 */           if (newCapacity > this.maxLength - 16)
/*  82:    */           {
/*  83: 92 */             this.length = newCapacity;
/*  84: 93 */             setIndex(Math.min(readerIndex(), newCapacity), Math.min(writerIndex(), newCapacity));
/*  85: 94 */             return this;
/*  86:    */           }
/*  87:    */         }
/*  88:    */         else
/*  89:    */         {
/*  90: 97 */           this.length = newCapacity;
/*  91: 98 */           setIndex(Math.min(readerIndex(), newCapacity), Math.min(writerIndex(), newCapacity));
/*  92: 99 */           return this;
/*  93:    */         }
/*  94:    */       }
/*  95:    */     }
/*  96:    */     else {
/*  97:103 */       return this;
/*  98:    */     }
/*  99:108 */     this.chunk.arena.reallocate(this, newCapacity, true);
/* 100:109 */     return this;
/* 101:    */   }
/* 102:    */   
/* 103:    */   public final ByteBufAllocator alloc()
/* 104:    */   {
/* 105:114 */     return this.chunk.arena.parent;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public final ByteOrder order()
/* 109:    */   {
/* 110:119 */     return ByteOrder.BIG_ENDIAN;
/* 111:    */   }
/* 112:    */   
/* 113:    */   public final ByteBuf unwrap()
/* 114:    */   {
/* 115:124 */     return null;
/* 116:    */   }
/* 117:    */   
/* 118:    */   protected final ByteBuffer internalNioBuffer()
/* 119:    */   {
/* 120:128 */     ByteBuffer tmpNioBuf = this.tmpNioBuf;
/* 121:129 */     if (tmpNioBuf == null) {
/* 122:130 */       this.tmpNioBuf = (tmpNioBuf = newInternalNioBuffer(this.memory));
/* 123:    */     }
/* 124:132 */     return tmpNioBuf;
/* 125:    */   }
/* 126:    */   
/* 127:    */   protected abstract ByteBuffer newInternalNioBuffer(T paramT);
/* 128:    */   
/* 129:    */   protected final void deallocate()
/* 130:    */   {
/* 131:139 */     if (this.handle >= 0L)
/* 132:    */     {
/* 133:140 */       long handle = this.handle;
/* 134:141 */       this.handle = -1L;
/* 135:142 */       this.memory = null;
/* 136:143 */       this.chunk.arena.free(this.chunk, handle, this.maxLength);
/* 137:144 */       recycle();
/* 138:    */     }
/* 139:    */   }
/* 140:    */   
/* 141:    */   private void recycle()
/* 142:    */   {
/* 143:150 */     Recycler.Handle recyclerHandle = this.recyclerHandle;
/* 144:151 */     if (recyclerHandle != null) {
/* 145:152 */       recycler().recycle(this, recyclerHandle);
/* 146:    */     }
/* 147:    */   }
/* 148:    */   
/* 149:    */   protected abstract Recycler<?> recycler();
/* 150:    */   
/* 151:    */   protected final int idx(int index)
/* 152:    */   {
/* 153:159 */     return this.offset + index;
/* 154:    */   }
/* 155:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.PooledByteBuf
 * JD-Core Version:    0.7.0.1
 */