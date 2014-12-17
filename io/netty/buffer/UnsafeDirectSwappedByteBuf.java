/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import java.nio.ByteOrder;
/*   5:    */ 
/*   6:    */ final class UnsafeDirectSwappedByteBuf
/*   7:    */   extends SwappedByteBuf
/*   8:    */ {
/*   9: 27 */   private static final boolean NATIVE_ORDER = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
/*  10:    */   private final boolean nativeByteOrder;
/*  11:    */   private final AbstractByteBuf wrapped;
/*  12:    */   
/*  13:    */   UnsafeDirectSwappedByteBuf(AbstractByteBuf buf)
/*  14:    */   {
/*  15: 32 */     super(buf);
/*  16: 33 */     this.wrapped = buf;
/*  17: 34 */     this.nativeByteOrder = (NATIVE_ORDER == (order() == ByteOrder.BIG_ENDIAN));
/*  18:    */   }
/*  19:    */   
/*  20:    */   private long addr(int index)
/*  21:    */   {
/*  22: 42 */     return this.wrapped.memoryAddress() + index;
/*  23:    */   }
/*  24:    */   
/*  25:    */   public long getLong(int index)
/*  26:    */   {
/*  27: 47 */     this.wrapped.checkIndex(index, 8);
/*  28: 48 */     long v = PlatformDependent.getLong(addr(index));
/*  29: 49 */     return this.nativeByteOrder ? v : Long.reverseBytes(v);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public float getFloat(int index)
/*  33:    */   {
/*  34: 54 */     return Float.intBitsToFloat(getInt(index));
/*  35:    */   }
/*  36:    */   
/*  37:    */   public double getDouble(int index)
/*  38:    */   {
/*  39: 59 */     return Double.longBitsToDouble(getLong(index));
/*  40:    */   }
/*  41:    */   
/*  42:    */   public char getChar(int index)
/*  43:    */   {
/*  44: 64 */     return (char)getShort(index);
/*  45:    */   }
/*  46:    */   
/*  47:    */   public long getUnsignedInt(int index)
/*  48:    */   {
/*  49: 69 */     return getInt(index) & 0xFFFFFFFF;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public int getInt(int index)
/*  53:    */   {
/*  54: 74 */     this.wrapped.checkIndex(index, 4);
/*  55: 75 */     int v = PlatformDependent.getInt(addr(index));
/*  56: 76 */     return this.nativeByteOrder ? v : Integer.reverseBytes(v);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public int getUnsignedShort(int index)
/*  60:    */   {
/*  61: 81 */     return getShort(index) & 0xFFFF;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public short getShort(int index)
/*  65:    */   {
/*  66: 86 */     this.wrapped.checkIndex(index, 2);
/*  67: 87 */     short v = PlatformDependent.getShort(addr(index));
/*  68: 88 */     return this.nativeByteOrder ? v : Short.reverseBytes(v);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public ByteBuf setShort(int index, int value)
/*  72:    */   {
/*  73: 93 */     this.wrapped.checkIndex(index, 2);
/*  74: 94 */     _setShort(index, value);
/*  75: 95 */     return this;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public ByteBuf setInt(int index, int value)
/*  79:    */   {
/*  80:100 */     this.wrapped.checkIndex(index, 4);
/*  81:101 */     _setInt(index, value);
/*  82:102 */     return this;
/*  83:    */   }
/*  84:    */   
/*  85:    */   public ByteBuf setLong(int index, long value)
/*  86:    */   {
/*  87:107 */     this.wrapped.checkIndex(index, 8);
/*  88:108 */     _setLong(index, value);
/*  89:109 */     return this;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public ByteBuf setChar(int index, int value)
/*  93:    */   {
/*  94:114 */     setShort(index, value);
/*  95:115 */     return this;
/*  96:    */   }
/*  97:    */   
/*  98:    */   public ByteBuf setFloat(int index, float value)
/*  99:    */   {
/* 100:120 */     setInt(index, Float.floatToRawIntBits(value));
/* 101:121 */     return this;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public ByteBuf setDouble(int index, double value)
/* 105:    */   {
/* 106:126 */     setLong(index, Double.doubleToRawLongBits(value));
/* 107:127 */     return this;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public ByteBuf writeShort(int value)
/* 111:    */   {
/* 112:132 */     this.wrapped.ensureWritable(2);
/* 113:133 */     _setShort(this.wrapped.writerIndex, value);
/* 114:134 */     this.wrapped.writerIndex += 2;
/* 115:135 */     return this;
/* 116:    */   }
/* 117:    */   
/* 118:    */   public ByteBuf writeInt(int value)
/* 119:    */   {
/* 120:140 */     this.wrapped.ensureWritable(4);
/* 121:141 */     _setInt(this.wrapped.writerIndex, value);
/* 122:142 */     this.wrapped.writerIndex += 4;
/* 123:143 */     return this;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public ByteBuf writeLong(long value)
/* 127:    */   {
/* 128:148 */     this.wrapped.ensureWritable(8);
/* 129:149 */     _setLong(this.wrapped.writerIndex, value);
/* 130:150 */     this.wrapped.writerIndex += 8;
/* 131:151 */     return this;
/* 132:    */   }
/* 133:    */   
/* 134:    */   public ByteBuf writeChar(int value)
/* 135:    */   {
/* 136:156 */     writeShort(value);
/* 137:157 */     return this;
/* 138:    */   }
/* 139:    */   
/* 140:    */   public ByteBuf writeFloat(float value)
/* 141:    */   {
/* 142:162 */     writeInt(Float.floatToRawIntBits(value));
/* 143:163 */     return this;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public ByteBuf writeDouble(double value)
/* 147:    */   {
/* 148:168 */     writeLong(Double.doubleToRawLongBits(value));
/* 149:169 */     return this;
/* 150:    */   }
/* 151:    */   
/* 152:    */   private void _setShort(int index, int value)
/* 153:    */   {
/* 154:173 */     PlatformDependent.putShort(addr(index), this.nativeByteOrder ? (short)value : Short.reverseBytes((short)value));
/* 155:    */   }
/* 156:    */   
/* 157:    */   private void _setInt(int index, int value)
/* 158:    */   {
/* 159:177 */     PlatformDependent.putInt(addr(index), this.nativeByteOrder ? value : Integer.reverseBytes(value));
/* 160:    */   }
/* 161:    */   
/* 162:    */   private void _setLong(int index, long value)
/* 163:    */   {
/* 164:181 */     PlatformDependent.putLong(addr(index), this.nativeByteOrder ? value : Long.reverseBytes(value));
/* 165:    */   }
/* 166:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.UnsafeDirectSwappedByteBuf
 * JD-Core Version:    0.7.0.1
 */