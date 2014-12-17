/*   1:    */ package io.netty.handler.stream;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import java.io.File;
/*   7:    */ import java.io.FileInputStream;
/*   8:    */ import java.io.IOException;
/*   9:    */ import java.nio.channels.FileChannel;
/*  10:    */ 
/*  11:    */ public class ChunkedNioFile
/*  12:    */   implements ChunkedInput<ByteBuf>
/*  13:    */ {
/*  14:    */   private final FileChannel in;
/*  15:    */   private final long startOffset;
/*  16:    */   private final long endOffset;
/*  17:    */   private final int chunkSize;
/*  18:    */   private long offset;
/*  19:    */   
/*  20:    */   public ChunkedNioFile(File in)
/*  21:    */     throws IOException
/*  22:    */   {
/*  23: 47 */     this(new FileInputStream(in).getChannel());
/*  24:    */   }
/*  25:    */   
/*  26:    */   public ChunkedNioFile(File in, int chunkSize)
/*  27:    */     throws IOException
/*  28:    */   {
/*  29: 57 */     this(new FileInputStream(in).getChannel(), chunkSize);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public ChunkedNioFile(FileChannel in)
/*  33:    */     throws IOException
/*  34:    */   {
/*  35: 64 */     this(in, 8192);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public ChunkedNioFile(FileChannel in, int chunkSize)
/*  39:    */     throws IOException
/*  40:    */   {
/*  41: 74 */     this(in, 0L, in.size(), chunkSize);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public ChunkedNioFile(FileChannel in, long offset, long length, int chunkSize)
/*  45:    */     throws IOException
/*  46:    */   {
/*  47: 87 */     if (in == null) {
/*  48: 88 */       throw new NullPointerException("in");
/*  49:    */     }
/*  50: 90 */     if (offset < 0L) {
/*  51: 91 */       throw new IllegalArgumentException("offset: " + offset + " (expected: 0 or greater)");
/*  52:    */     }
/*  53: 94 */     if (length < 0L) {
/*  54: 95 */       throw new IllegalArgumentException("length: " + length + " (expected: 0 or greater)");
/*  55:    */     }
/*  56: 98 */     if (chunkSize <= 0) {
/*  57: 99 */       throw new IllegalArgumentException("chunkSize: " + chunkSize + " (expected: a positive integer)");
/*  58:    */     }
/*  59:104 */     if (offset != 0L) {
/*  60:105 */       in.position(offset);
/*  61:    */     }
/*  62:107 */     this.in = in;
/*  63:108 */     this.chunkSize = chunkSize;
/*  64:109 */     this.offset = (this.startOffset = offset);
/*  65:110 */     this.endOffset = (offset + length);
/*  66:    */   }
/*  67:    */   
/*  68:    */   public long startOffset()
/*  69:    */   {
/*  70:117 */     return this.startOffset;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public long endOffset()
/*  74:    */   {
/*  75:124 */     return this.endOffset;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public long currentOffset()
/*  79:    */   {
/*  80:131 */     return this.offset;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public boolean isEndOfInput()
/*  84:    */     throws Exception
/*  85:    */   {
/*  86:136 */     return (this.offset >= this.endOffset) || (!this.in.isOpen());
/*  87:    */   }
/*  88:    */   
/*  89:    */   public void close()
/*  90:    */     throws Exception
/*  91:    */   {
/*  92:141 */     this.in.close();
/*  93:    */   }
/*  94:    */   
/*  95:    */   public ByteBuf readChunk(ChannelHandlerContext ctx)
/*  96:    */     throws Exception
/*  97:    */   {
/*  98:146 */     long offset = this.offset;
/*  99:147 */     if (offset >= this.endOffset) {
/* 100:148 */       return null;
/* 101:    */     }
/* 102:151 */     int chunkSize = (int)Math.min(this.chunkSize, this.endOffset - offset);
/* 103:152 */     ByteBuf buffer = ctx.alloc().buffer(chunkSize);
/* 104:153 */     boolean release = true;
/* 105:    */     try
/* 106:    */     {
/* 107:155 */       int readBytes = 0;
/* 108:    */       int localReadBytes;
/* 109:    */       for (;;)
/* 110:    */       {
/* 111:157 */         localReadBytes = buffer.writeBytes(this.in, chunkSize - readBytes);
/* 112:158 */         if (localReadBytes < 0) {
/* 113:    */           break;
/* 114:    */         }
/* 115:161 */         readBytes += localReadBytes;
/* 116:162 */         if (readBytes == chunkSize) {
/* 117:    */           break;
/* 118:    */         }
/* 119:    */       }
/* 120:166 */       this.offset += readBytes;
/* 121:167 */       release = false;
/* 122:168 */       return buffer;
/* 123:    */     }
/* 124:    */     finally
/* 125:    */     {
/* 126:170 */       if (release) {
/* 127:171 */         buffer.release();
/* 128:    */       }
/* 129:    */     }
/* 130:    */   }
/* 131:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.stream.ChunkedNioFile
 * JD-Core Version:    0.7.0.1
 */