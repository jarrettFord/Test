/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ final class PoolSubpage<T>
/*   4:    */ {
/*   5:    */   final PoolChunk<T> chunk;
/*   6:    */   private final int memoryMapIdx;
/*   7:    */   private final int runOffset;
/*   8:    */   private final int pageSize;
/*   9:    */   private final long[] bitmap;
/*  10:    */   PoolSubpage<T> prev;
/*  11:    */   PoolSubpage<T> next;
/*  12:    */   boolean doNotDestroy;
/*  13:    */   int elemSize;
/*  14:    */   private int maxNumElems;
/*  15:    */   private int bitmapLength;
/*  16:    */   private int nextAvail;
/*  17:    */   private int numAvail;
/*  18:    */   
/*  19:    */   PoolSubpage(int pageSize)
/*  20:    */   {
/*  21: 42 */     this.chunk = null;
/*  22: 43 */     this.memoryMapIdx = -1;
/*  23: 44 */     this.runOffset = -1;
/*  24: 45 */     this.elemSize = -1;
/*  25: 46 */     this.pageSize = pageSize;
/*  26: 47 */     this.bitmap = null;
/*  27:    */   }
/*  28:    */   
/*  29:    */   PoolSubpage(PoolChunk<T> chunk, int memoryMapIdx, int runOffset, int pageSize, int elemSize)
/*  30:    */   {
/*  31: 51 */     this.chunk = chunk;
/*  32: 52 */     this.memoryMapIdx = memoryMapIdx;
/*  33: 53 */     this.runOffset = runOffset;
/*  34: 54 */     this.pageSize = pageSize;
/*  35: 55 */     this.bitmap = new long[pageSize >>> 10];
/*  36: 56 */     init(elemSize);
/*  37:    */   }
/*  38:    */   
/*  39:    */   void init(int elemSize)
/*  40:    */   {
/*  41: 60 */     this.doNotDestroy = true;
/*  42: 61 */     this.elemSize = elemSize;
/*  43: 62 */     if (elemSize != 0)
/*  44:    */     {
/*  45: 63 */       this.maxNumElems = (this.numAvail = this.pageSize / elemSize);
/*  46: 64 */       this.nextAvail = 0;
/*  47: 65 */       this.bitmapLength = (this.maxNumElems >>> 6);
/*  48: 66 */       if ((this.maxNumElems & 0x3F) != 0) {
/*  49: 67 */         this.bitmapLength += 1;
/*  50:    */       }
/*  51: 70 */       for (int i = 0; i < this.bitmapLength; i++) {
/*  52: 71 */         this.bitmap[i] = 0L;
/*  53:    */       }
/*  54:    */     }
/*  55: 75 */     addToPool();
/*  56:    */   }
/*  57:    */   
/*  58:    */   long allocate()
/*  59:    */   {
/*  60: 82 */     if (this.elemSize == 0) {
/*  61: 83 */       return toHandle(0);
/*  62:    */     }
/*  63: 86 */     if ((this.numAvail == 0) || (!this.doNotDestroy)) {
/*  64: 87 */       return -1L;
/*  65:    */     }
/*  66: 90 */     int bitmapIdx = getNextAvail();
/*  67: 91 */     int q = bitmapIdx >>> 6;
/*  68: 92 */     int r = bitmapIdx & 0x3F;
/*  69: 93 */     assert ((this.bitmap[q] >>> r & 1L) == 0L);
/*  70: 94 */     this.bitmap[q] |= 1L << r;
/*  71: 96 */     if (--this.numAvail == 0) {
/*  72: 97 */       removeFromPool();
/*  73:    */     }
/*  74:100 */     return toHandle(bitmapIdx);
/*  75:    */   }
/*  76:    */   
/*  77:    */   boolean free(int bitmapIdx)
/*  78:    */   {
/*  79:109 */     if (this.elemSize == 0) {
/*  80:110 */       return true;
/*  81:    */     }
/*  82:113 */     int q = bitmapIdx >>> 6;
/*  83:114 */     int r = bitmapIdx & 0x3F;
/*  84:115 */     assert ((this.bitmap[q] >>> r & 1L) != 0L);
/*  85:116 */     this.bitmap[q] ^= 1L << r;
/*  86:    */     
/*  87:118 */     setNextAvail(bitmapIdx);
/*  88:120 */     if (this.numAvail++ == 0)
/*  89:    */     {
/*  90:121 */       addToPool();
/*  91:122 */       return true;
/*  92:    */     }
/*  93:125 */     if (this.numAvail != this.maxNumElems) {
/*  94:126 */       return true;
/*  95:    */     }
/*  96:129 */     if (this.prev == this.next) {
/*  97:131 */       return true;
/*  98:    */     }
/*  99:135 */     this.doNotDestroy = false;
/* 100:136 */     removeFromPool();
/* 101:137 */     return false;
/* 102:    */   }
/* 103:    */   
/* 104:    */   private void addToPool()
/* 105:    */   {
/* 106:142 */     PoolSubpage<T> head = this.chunk.arena.findSubpagePoolHead(this.elemSize);
/* 107:143 */     assert ((this.prev == null) && (this.next == null));
/* 108:144 */     this.prev = head;
/* 109:145 */     this.next = head.next;
/* 110:146 */     this.next.prev = this;
/* 111:147 */     head.next = this;
/* 112:    */   }
/* 113:    */   
/* 114:    */   private void removeFromPool()
/* 115:    */   {
/* 116:151 */     assert ((this.prev != null) && (this.next != null));
/* 117:152 */     this.prev.next = this.next;
/* 118:153 */     this.next.prev = this.prev;
/* 119:154 */     this.next = null;
/* 120:155 */     this.prev = null;
/* 121:    */   }
/* 122:    */   
/* 123:    */   private void setNextAvail(int bitmapIdx)
/* 124:    */   {
/* 125:159 */     this.nextAvail = bitmapIdx;
/* 126:    */   }
/* 127:    */   
/* 128:    */   private int getNextAvail()
/* 129:    */   {
/* 130:163 */     int nextAvail = this.nextAvail;
/* 131:164 */     if (nextAvail >= 0)
/* 132:    */     {
/* 133:165 */       this.nextAvail = -1;
/* 134:166 */       return nextAvail;
/* 135:    */     }
/* 136:168 */     return findNextAvail();
/* 137:    */   }
/* 138:    */   
/* 139:    */   private int findNextAvail()
/* 140:    */   {
/* 141:172 */     long[] bitmap = this.bitmap;
/* 142:173 */     int bitmapLength = this.bitmapLength;
/* 143:174 */     for (int i = 0; i < bitmapLength; i++)
/* 144:    */     {
/* 145:175 */       long bits = bitmap[i];
/* 146:176 */       if ((bits ^ 0xFFFFFFFF) != 0L) {
/* 147:177 */         return findNextAvail0(i, bits);
/* 148:    */       }
/* 149:    */     }
/* 150:180 */     return -1;
/* 151:    */   }
/* 152:    */   
/* 153:    */   private int findNextAvail0(int i, long bits)
/* 154:    */   {
/* 155:184 */     int maxNumElems = this.maxNumElems;
/* 156:185 */     int baseVal = i << 6;
/* 157:187 */     for (int j = 0; j < 64; j++)
/* 158:    */     {
/* 159:188 */       if ((bits & 1L) == 0L)
/* 160:    */       {
/* 161:189 */         int val = baseVal | j;
/* 162:190 */         if (val >= maxNumElems) {
/* 163:    */           break;
/* 164:    */         }
/* 165:191 */         return val;
/* 166:    */       }
/* 167:196 */       bits >>>= 1;
/* 168:    */     }
/* 169:198 */     return -1;
/* 170:    */   }
/* 171:    */   
/* 172:    */   private long toHandle(int bitmapIdx)
/* 173:    */   {
/* 174:202 */     return 0x0 | bitmapIdx << 32 | this.memoryMapIdx;
/* 175:    */   }
/* 176:    */   
/* 177:    */   public String toString()
/* 178:    */   {
/* 179:206 */     if (!this.doNotDestroy) {
/* 180:207 */       return "(" + this.memoryMapIdx + ": not in use)";
/* 181:    */     }
/* 182:210 */     return String.valueOf('(') + this.memoryMapIdx + ": " + (this.maxNumElems - this.numAvail) + '/' + this.maxNumElems + ", offset: " + this.runOffset + ", length: " + this.pageSize + ", elemSize: " + this.elemSize + ')';
/* 183:    */   }
/* 184:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.PoolSubpage
 * JD-Core Version:    0.7.0.1
 */