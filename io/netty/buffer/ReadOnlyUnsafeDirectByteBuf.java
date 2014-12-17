/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import java.nio.Buffer;
/*   5:    */ import java.nio.ByteBuffer;
/*   6:    */ import java.nio.ByteOrder;
/*   7:    */ 
/*   8:    */ final class ReadOnlyUnsafeDirectByteBuf
/*   9:    */   extends ReadOnlyByteBufferBuf
/*  10:    */ {
/*  11: 30 */   private static final boolean NATIVE_ORDER = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
/*  12:    */   private final long memoryAddress;
/*  13:    */   
/*  14:    */   public ReadOnlyUnsafeDirectByteBuf(ByteBufAllocator allocator, ByteBuffer buffer)
/*  15:    */   {
/*  16: 34 */     super(allocator, buffer);
/*  17: 35 */     this.memoryAddress = PlatformDependent.directBufferAddress(buffer);
/*  18:    */   }
/*  19:    */   
/*  20:    */   protected byte _getByte(int index)
/*  21:    */   {
/*  22: 40 */     return PlatformDependent.getByte(addr(index));
/*  23:    */   }
/*  24:    */   
/*  25:    */   protected short _getShort(int index)
/*  26:    */   {
/*  27: 45 */     short v = PlatformDependent.getShort(addr(index));
/*  28: 46 */     return NATIVE_ORDER ? v : Short.reverseBytes(v);
/*  29:    */   }
/*  30:    */   
/*  31:    */   protected int _getUnsignedMedium(int index)
/*  32:    */   {
/*  33: 51 */     long addr = addr(index);
/*  34: 52 */     return (PlatformDependent.getByte(addr) & 0xFF) << 16 | (PlatformDependent.getByte(addr + 1L) & 0xFF) << 8 | PlatformDependent.getByte(addr + 2L) & 0xFF;
/*  35:    */   }
/*  36:    */   
/*  37:    */   protected int _getInt(int index)
/*  38:    */   {
/*  39: 59 */     int v = PlatformDependent.getInt(addr(index));
/*  40: 60 */     return NATIVE_ORDER ? v : Integer.reverseBytes(v);
/*  41:    */   }
/*  42:    */   
/*  43:    */   protected long _getLong(int index)
/*  44:    */   {
/*  45: 65 */     long v = PlatformDependent.getLong(addr(index));
/*  46: 66 */     return NATIVE_ORDER ? v : Long.reverseBytes(v);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  50:    */   {
/*  51: 71 */     checkIndex(index, length);
/*  52: 72 */     if (dst == null) {
/*  53: 73 */       throw new NullPointerException("dst");
/*  54:    */     }
/*  55: 75 */     if ((dstIndex < 0) || (dstIndex > dst.capacity() - length)) {
/*  56: 76 */       throw new IndexOutOfBoundsException("dstIndex: " + dstIndex);
/*  57:    */     }
/*  58: 79 */     if (dst.hasMemoryAddress()) {
/*  59: 80 */       PlatformDependent.copyMemory(addr(index), dst.memoryAddress() + dstIndex, length);
/*  60: 81 */     } else if (dst.hasArray()) {
/*  61: 82 */       PlatformDependent.copyMemory(addr(index), dst.array(), dst.arrayOffset() + dstIndex, length);
/*  62:    */     } else {
/*  63: 84 */       dst.setBytes(dstIndex, this, index, length);
/*  64:    */     }
/*  65: 86 */     return this;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/*  69:    */   {
/*  70: 91 */     checkIndex(index, length);
/*  71: 92 */     if (dst == null) {
/*  72: 93 */       throw new NullPointerException("dst");
/*  73:    */     }
/*  74: 95 */     if ((dstIndex < 0) || (dstIndex > dst.length - length)) {
/*  75: 96 */       throw new IndexOutOfBoundsException(String.format("dstIndex: %d, length: %d (expected: range(0, %d))", new Object[] { Integer.valueOf(dstIndex), Integer.valueOf(length), Integer.valueOf(dst.length) }));
/*  76:    */     }
/*  77:100 */     if (length != 0) {
/*  78:101 */       PlatformDependent.copyMemory(addr(index), dst, dstIndex, length);
/*  79:    */     }
/*  80:103 */     return this;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/*  84:    */   {
/*  85:108 */     checkIndex(index);
/*  86:109 */     if (dst == null) {
/*  87:110 */       throw new NullPointerException("dst");
/*  88:    */     }
/*  89:113 */     int bytesToCopy = Math.min(capacity() - index, dst.remaining());
/*  90:114 */     ByteBuffer tmpBuf = internalNioBuffer();
/*  91:115 */     tmpBuf.clear().position(index).limit(index + bytesToCopy);
/*  92:116 */     dst.put(tmpBuf);
/*  93:117 */     return this;
/*  94:    */   }
/*  95:    */   
/*  96:    */   public ByteBuf copy(int index, int length)
/*  97:    */   {
/*  98:122 */     checkIndex(index, length);
/*  99:123 */     ByteBuf copy = alloc().directBuffer(length, maxCapacity());
/* 100:124 */     if (length != 0) {
/* 101:125 */       if (copy.hasMemoryAddress())
/* 102:    */       {
/* 103:126 */         PlatformDependent.copyMemory(addr(index), copy.memoryAddress(), length);
/* 104:127 */         copy.setIndex(0, length);
/* 105:    */       }
/* 106:    */       else
/* 107:    */       {
/* 108:129 */         copy.writeBytes(this, index, length);
/* 109:    */       }
/* 110:    */     }
/* 111:132 */     return copy;
/* 112:    */   }
/* 113:    */   
/* 114:    */   private long addr(int index)
/* 115:    */   {
/* 116:136 */     return this.memoryAddress + index;
/* 117:    */   }
/* 118:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.ReadOnlyUnsafeDirectByteBuf
 * JD-Core Version:    0.7.0.1
 */