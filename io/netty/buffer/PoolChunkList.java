/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ 
/*   5:    */ final class PoolChunkList<T>
/*   6:    */ {
/*   7:    */   private final PoolArena<T> arena;
/*   8:    */   private final PoolChunkList<T> nextList;
/*   9:    */   PoolChunkList<T> prevList;
/*  10:    */   private final int minUsage;
/*  11:    */   private final int maxUsage;
/*  12:    */   private PoolChunk<T> head;
/*  13:    */   
/*  14:    */   PoolChunkList(PoolArena<T> arena, PoolChunkList<T> nextList, int minUsage, int maxUsage)
/*  15:    */   {
/*  16: 35 */     this.arena = arena;
/*  17: 36 */     this.nextList = nextList;
/*  18: 37 */     this.minUsage = minUsage;
/*  19: 38 */     this.maxUsage = maxUsage;
/*  20:    */   }
/*  21:    */   
/*  22:    */   boolean allocate(PooledByteBuf<T> buf, int reqCapacity, int normCapacity)
/*  23:    */   {
/*  24: 42 */     if (this.head == null) {
/*  25: 43 */       return false;
/*  26:    */     }
/*  27: 46 */     PoolChunk<T> cur = this.head;
/*  28:    */     for (;;)
/*  29:    */     {
/*  30: 47 */       long handle = cur.allocate(normCapacity);
/*  31: 48 */       if (handle < 0L)
/*  32:    */       {
/*  33: 49 */         cur = cur.next;
/*  34: 50 */         if (cur == null) {
/*  35: 51 */           return false;
/*  36:    */         }
/*  37:    */       }
/*  38:    */       else
/*  39:    */       {
/*  40: 54 */         cur.initBuf(buf, handle, reqCapacity);
/*  41: 55 */         if (cur.usage() >= this.maxUsage)
/*  42:    */         {
/*  43: 56 */           remove(cur);
/*  44: 57 */           this.nextList.add(cur);
/*  45:    */         }
/*  46: 59 */         return true;
/*  47:    */       }
/*  48:    */     }
/*  49:    */   }
/*  50:    */   
/*  51:    */   void free(PoolChunk<T> chunk, long handle)
/*  52:    */   {
/*  53: 65 */     chunk.free(handle);
/*  54: 66 */     if (chunk.usage() < this.minUsage)
/*  55:    */     {
/*  56: 67 */       remove(chunk);
/*  57: 68 */       if (this.prevList == null)
/*  58:    */       {
/*  59: 69 */         assert (chunk.usage() == 0);
/*  60: 70 */         this.arena.destroyChunk(chunk);
/*  61:    */       }
/*  62:    */       else
/*  63:    */       {
/*  64: 72 */         this.prevList.add(chunk);
/*  65:    */       }
/*  66:    */     }
/*  67:    */   }
/*  68:    */   
/*  69:    */   void add(PoolChunk<T> chunk)
/*  70:    */   {
/*  71: 78 */     if (chunk.usage() >= this.maxUsage)
/*  72:    */     {
/*  73: 79 */       this.nextList.add(chunk);
/*  74: 80 */       return;
/*  75:    */     }
/*  76: 83 */     chunk.parent = this;
/*  77: 84 */     if (this.head == null)
/*  78:    */     {
/*  79: 85 */       this.head = chunk;
/*  80: 86 */       chunk.prev = null;
/*  81: 87 */       chunk.next = null;
/*  82:    */     }
/*  83:    */     else
/*  84:    */     {
/*  85: 89 */       chunk.prev = null;
/*  86: 90 */       chunk.next = this.head;
/*  87: 91 */       this.head.prev = chunk;
/*  88: 92 */       this.head = chunk;
/*  89:    */     }
/*  90:    */   }
/*  91:    */   
/*  92:    */   private void remove(PoolChunk<T> cur)
/*  93:    */   {
/*  94: 97 */     if (cur == this.head)
/*  95:    */     {
/*  96: 98 */       this.head = cur.next;
/*  97: 99 */       if (this.head != null) {
/*  98:100 */         this.head.prev = null;
/*  99:    */       }
/* 100:    */     }
/* 101:    */     else
/* 102:    */     {
/* 103:103 */       PoolChunk<T> next = cur.next;
/* 104:104 */       cur.prev.next = next;
/* 105:105 */       if (next != null) {
/* 106:106 */         next.prev = cur.prev;
/* 107:    */       }
/* 108:    */     }
/* 109:    */   }
/* 110:    */   
/* 111:    */   public String toString()
/* 112:    */   {
/* 113:113 */     if (this.head == null) {
/* 114:114 */       return "none";
/* 115:    */     }
/* 116:117 */     StringBuilder buf = new StringBuilder();
/* 117:118 */     PoolChunk<T> cur = this.head;
/* 118:    */     for (;;)
/* 119:    */     {
/* 120:119 */       buf.append(cur);
/* 121:120 */       cur = cur.next;
/* 122:121 */       if (cur == null) {
/* 123:    */         break;
/* 124:    */       }
/* 125:124 */       buf.append(StringUtil.NEWLINE);
/* 126:    */     }
/* 127:127 */     return buf.toString();
/* 128:    */   }
/* 129:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.PoolChunkList
 * JD-Core Version:    0.7.0.1
 */