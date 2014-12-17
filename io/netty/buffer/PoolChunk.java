/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ final class PoolChunk<T>
/*   4:    */ {
/*   5:    */   final PoolArena<T> arena;
/*   6:    */   final T memory;
/*   7:    */   final boolean unpooled;
/*   8:    */   private final byte[] memoryMap;
/*   9:    */   private final byte[] depthMap;
/*  10:    */   private final PoolSubpage<T>[] subpages;
/*  11:    */   private final int subpageOverflowMask;
/*  12:    */   private final int pageSize;
/*  13:    */   private final int pageShifts;
/*  14:    */   private final int maxOrder;
/*  15:    */   private final int chunkSize;
/*  16:    */   private final int log2ChunkSize;
/*  17:    */   private final int maxSubpageAllocs;
/*  18:    */   private final byte unusable;
/*  19:    */   private int freeBytes;
/*  20:    */   PoolChunkList<T> parent;
/*  21:    */   PoolChunk<T> prev;
/*  22:    */   PoolChunk<T> next;
/*  23:    */   
/*  24:    */   PoolChunk(PoolArena<T> arena, T memory, int pageSize, int maxOrder, int pageShifts, int chunkSize)
/*  25:    */   {
/*  26:134 */     this.unpooled = false;
/*  27:135 */     this.arena = arena;
/*  28:136 */     this.memory = memory;
/*  29:137 */     this.pageSize = pageSize;
/*  30:138 */     this.pageShifts = pageShifts;
/*  31:139 */     this.maxOrder = maxOrder;
/*  32:140 */     this.chunkSize = chunkSize;
/*  33:141 */     this.unusable = ((byte)(maxOrder + 1));
/*  34:142 */     this.log2ChunkSize = log2(chunkSize);
/*  35:143 */     this.subpageOverflowMask = (pageSize - 1 ^ 0xFFFFFFFF);
/*  36:144 */     this.freeBytes = chunkSize;
/*  37:    */     
/*  38:146 */     assert (maxOrder < 30) : ("maxOrder should be < 30, but is: " + maxOrder);
/*  39:147 */     this.maxSubpageAllocs = (1 << maxOrder);
/*  40:    */     
/*  41:    */ 
/*  42:150 */     this.memoryMap = new byte[this.maxSubpageAllocs << 1];
/*  43:151 */     this.depthMap = new byte[this.memoryMap.length];
/*  44:152 */     int memoryMapIndex = 1;
/*  45:153 */     for (int d = 0; d <= maxOrder; d++)
/*  46:    */     {
/*  47:154 */       int depth = 1 << d;
/*  48:155 */       for (int p = 0; p < depth; p++)
/*  49:    */       {
/*  50:157 */         this.memoryMap[memoryMapIndex] = ((byte)d);
/*  51:158 */         this.depthMap[memoryMapIndex] = ((byte)d);
/*  52:159 */         memoryMapIndex++;
/*  53:    */       }
/*  54:    */     }
/*  55:163 */     this.subpages = newSubpageArray(this.maxSubpageAllocs);
/*  56:    */   }
/*  57:    */   
/*  58:    */   PoolChunk(PoolArena<T> arena, T memory, int size)
/*  59:    */   {
/*  60:168 */     this.unpooled = true;
/*  61:169 */     this.arena = arena;
/*  62:170 */     this.memory = memory;
/*  63:171 */     this.memoryMap = null;
/*  64:172 */     this.depthMap = null;
/*  65:173 */     this.subpages = null;
/*  66:174 */     this.subpageOverflowMask = 0;
/*  67:175 */     this.pageSize = 0;
/*  68:176 */     this.pageShifts = 0;
/*  69:177 */     this.maxOrder = 0;
/*  70:178 */     this.unusable = ((byte)(this.maxOrder + 1));
/*  71:179 */     this.chunkSize = size;
/*  72:180 */     this.log2ChunkSize = log2(this.chunkSize);
/*  73:181 */     this.maxSubpageAllocs = 0;
/*  74:    */   }
/*  75:    */   
/*  76:    */   private PoolSubpage<T>[] newSubpageArray(int size)
/*  77:    */   {
/*  78:186 */     return new PoolSubpage[size];
/*  79:    */   }
/*  80:    */   
/*  81:    */   int usage()
/*  82:    */   {
/*  83:190 */     int freeBytes = this.freeBytes;
/*  84:191 */     if (freeBytes == 0) {
/*  85:192 */       return 100;
/*  86:    */     }
/*  87:195 */     int freePercentage = (int)(freeBytes * 100L / this.chunkSize);
/*  88:196 */     if (freePercentage == 0) {
/*  89:197 */       return 99;
/*  90:    */     }
/*  91:199 */     return 100 - freePercentage;
/*  92:    */   }
/*  93:    */   
/*  94:    */   long allocate(int normCapacity)
/*  95:    */   {
/*  96:203 */     if ((normCapacity & this.subpageOverflowMask) != 0) {
/*  97:204 */       return allocateRun(normCapacity);
/*  98:    */     }
/*  99:206 */     return allocateSubpage(normCapacity);
/* 100:    */   }
/* 101:    */   
/* 102:    */   private void updateParentsAlloc(int id)
/* 103:    */   {
/* 104:219 */     while (id > 1)
/* 105:    */     {
/* 106:220 */       int parentId = id >>> 1;
/* 107:221 */       byte val1 = value(id);
/* 108:222 */       byte val2 = value(id ^ 0x1);
/* 109:223 */       byte val = val1 < val2 ? val1 : val2;
/* 110:224 */       setValue(parentId, val);
/* 111:225 */       id = parentId;
/* 112:    */     }
/* 113:    */   }
/* 114:    */   
/* 115:    */   private void updateParentsFree(int id)
/* 116:    */   {
/* 117:237 */     int logChild = depth(id) + 1;
/* 118:238 */     while (id > 1)
/* 119:    */     {
/* 120:239 */       int parentId = id >>> 1;
/* 121:240 */       byte val1 = value(id);
/* 122:241 */       byte val2 = value(id ^ 0x1);
/* 123:242 */       logChild--;
/* 124:244 */       if ((val1 == logChild) && (val2 == logChild))
/* 125:    */       {
/* 126:245 */         setValue(parentId, (byte)(logChild - 1));
/* 127:    */       }
/* 128:    */       else
/* 129:    */       {
/* 130:247 */         byte val = val1 < val2 ? val1 : val2;
/* 131:248 */         setValue(parentId, val);
/* 132:    */       }
/* 133:251 */       id = parentId;
/* 134:    */     }
/* 135:    */   }
/* 136:    */   
/* 137:    */   private int allocateNode(int d)
/* 138:    */   {
/* 139:263 */     int id = 1;
/* 140:264 */     int initial = -(1 << d);
/* 141:265 */     byte val = value(id);
/* 142:266 */     if (val > d) {
/* 143:267 */       return -1;
/* 144:    */     }
/* 145:269 */     while ((val < d) || ((id & initial) == 0))
/* 146:    */     {
/* 147:270 */       id <<= 1;
/* 148:271 */       val = value(id);
/* 149:272 */       if (val > d)
/* 150:    */       {
/* 151:273 */         id ^= 0x1;
/* 152:274 */         val = value(id);
/* 153:    */       }
/* 154:    */     }
/* 155:277 */     byte value = value(id);
/* 156:278 */     if ((!$assertionsDisabled) && ((value != d) || ((id & initial) != 1 << d))) {
/* 157:278 */       throw new AssertionError(String.format("val = %d, id & initial = %d, d = %d", new Object[] { Byte.valueOf(value), Integer.valueOf(id & initial), Integer.valueOf(d) }));
/* 158:    */     }
/* 159:280 */     setValue(id, this.unusable);
/* 160:281 */     updateParentsAlloc(id);
/* 161:282 */     return id;
/* 162:    */   }
/* 163:    */   
/* 164:    */   private long allocateRun(int normCapacity)
/* 165:    */   {
/* 166:292 */     int d = this.maxOrder - (log2(normCapacity) - this.pageShifts);
/* 167:293 */     int id = allocateNode(d);
/* 168:294 */     if (id < 0) {
/* 169:295 */       return id;
/* 170:    */     }
/* 171:297 */     this.freeBytes -= runLength(id);
/* 172:298 */     return id;
/* 173:    */   }
/* 174:    */   
/* 175:    */   private long allocateSubpage(int normCapacity)
/* 176:    */   {
/* 177:309 */     int d = this.maxOrder;
/* 178:310 */     int id = allocateNode(d);
/* 179:311 */     if (id < 0) {
/* 180:312 */       return id;
/* 181:    */     }
/* 182:315 */     PoolSubpage<T>[] subpages = this.subpages;
/* 183:316 */     int pageSize = this.pageSize;
/* 184:    */     
/* 185:318 */     this.freeBytes -= pageSize;
/* 186:    */     
/* 187:320 */     int subpageIdx = subpageIdx(id);
/* 188:321 */     PoolSubpage<T> subpage = subpages[subpageIdx];
/* 189:322 */     if (subpage == null)
/* 190:    */     {
/* 191:323 */       subpage = new PoolSubpage(this, id, runOffset(id), pageSize, normCapacity);
/* 192:324 */       subpages[subpageIdx] = subpage;
/* 193:    */     }
/* 194:    */     else
/* 195:    */     {
/* 196:326 */       subpage.init(normCapacity);
/* 197:    */     }
/* 198:328 */     return subpage.allocate();
/* 199:    */   }
/* 200:    */   
/* 201:    */   void free(long handle)
/* 202:    */   {
/* 203:340 */     int memoryMapIdx = (int)handle;
/* 204:341 */     int bitmapIdx = (int)(handle >>> 32);
/* 205:343 */     if (bitmapIdx != 0)
/* 206:    */     {
/* 207:344 */       PoolSubpage<T> subpage = this.subpages[subpageIdx(memoryMapIdx)];
/* 208:345 */       assert ((subpage != null) && (subpage.doNotDestroy));
/* 209:346 */       if (subpage.free(bitmapIdx & 0x3FFFFFFF)) {
/* 210:347 */         return;
/* 211:    */       }
/* 212:    */     }
/* 213:350 */     this.freeBytes += runLength(memoryMapIdx);
/* 214:351 */     setValue(memoryMapIdx, depth(memoryMapIdx));
/* 215:352 */     updateParentsFree(memoryMapIdx);
/* 216:    */   }
/* 217:    */   
/* 218:    */   void initBuf(PooledByteBuf<T> buf, long handle, int reqCapacity)
/* 219:    */   {
/* 220:356 */     int memoryMapIdx = (int)handle;
/* 221:357 */     int bitmapIdx = (int)(handle >>> 32);
/* 222:358 */     if (bitmapIdx == 0)
/* 223:    */     {
/* 224:359 */       byte val = value(memoryMapIdx);
/* 225:360 */       assert (val == this.unusable) : String.valueOf(val);
/* 226:361 */       buf.init(this, handle, runOffset(memoryMapIdx), reqCapacity, runLength(memoryMapIdx));
/* 227:    */     }
/* 228:    */     else
/* 229:    */     {
/* 230:363 */       initBufWithSubpage(buf, handle, bitmapIdx, reqCapacity);
/* 231:    */     }
/* 232:    */   }
/* 233:    */   
/* 234:    */   void initBufWithSubpage(PooledByteBuf<T> buf, long handle, int reqCapacity)
/* 235:    */   {
/* 236:368 */     initBufWithSubpage(buf, handle, (int)(handle >>> 32), reqCapacity);
/* 237:    */   }
/* 238:    */   
/* 239:    */   private void initBufWithSubpage(PooledByteBuf<T> buf, long handle, int bitmapIdx, int reqCapacity)
/* 240:    */   {
/* 241:372 */     assert (bitmapIdx != 0);
/* 242:    */     
/* 243:374 */     int memoryMapIdx = (int)handle;
/* 244:    */     
/* 245:376 */     PoolSubpage<T> subpage = this.subpages[subpageIdx(memoryMapIdx)];
/* 246:377 */     assert (subpage.doNotDestroy);
/* 247:378 */     assert (reqCapacity <= subpage.elemSize);
/* 248:    */     
/* 249:380 */     buf.init(this, handle, runOffset(memoryMapIdx) + (bitmapIdx & 0x3FFFFFFF) * subpage.elemSize, reqCapacity, subpage.elemSize);
/* 250:    */   }
/* 251:    */   
/* 252:    */   private byte value(int id)
/* 253:    */   {
/* 254:386 */     return this.memoryMap[id];
/* 255:    */   }
/* 256:    */   
/* 257:    */   private void setValue(int id, byte val)
/* 258:    */   {
/* 259:390 */     this.memoryMap[id] = val;
/* 260:    */   }
/* 261:    */   
/* 262:    */   private byte depth(int id)
/* 263:    */   {
/* 264:394 */     return this.depthMap[id];
/* 265:    */   }
/* 266:    */   
/* 267:    */   private static int log2(int val)
/* 268:    */   {
/* 269:399 */     return 31 - Integer.numberOfLeadingZeros(val);
/* 270:    */   }
/* 271:    */   
/* 272:    */   private int runLength(int id)
/* 273:    */   {
/* 274:404 */     return 1 << this.log2ChunkSize - depth(id);
/* 275:    */   }
/* 276:    */   
/* 277:    */   private int runOffset(int id)
/* 278:    */   {
/* 279:409 */     int shift = id ^ 1 << depth(id);
/* 280:410 */     return shift * runLength(id);
/* 281:    */   }
/* 282:    */   
/* 283:    */   private int subpageIdx(int memoryMapIdx)
/* 284:    */   {
/* 285:414 */     return memoryMapIdx ^ this.maxSubpageAllocs;
/* 286:    */   }
/* 287:    */   
/* 288:    */   public String toString()
/* 289:    */   {
/* 290:419 */     StringBuilder buf = new StringBuilder();
/* 291:420 */     buf.append("Chunk(");
/* 292:421 */     buf.append(Integer.toHexString(System.identityHashCode(this)));
/* 293:422 */     buf.append(": ");
/* 294:423 */     buf.append(usage());
/* 295:424 */     buf.append("%, ");
/* 296:425 */     buf.append(this.chunkSize - this.freeBytes);
/* 297:426 */     buf.append('/');
/* 298:427 */     buf.append(this.chunkSize);
/* 299:428 */     buf.append(')');
/* 300:429 */     return buf.toString();
/* 301:    */   }
/* 302:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.PoolChunk
 * JD-Core Version:    0.7.0.1
 */