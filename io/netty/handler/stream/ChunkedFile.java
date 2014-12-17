/*   1:    */ package io.netty.handler.stream;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import java.io.File;
/*   7:    */ import java.io.IOException;
/*   8:    */ import java.io.RandomAccessFile;
/*   9:    */ import java.nio.channels.FileChannel;
/*  10:    */ 
/*  11:    */ public class ChunkedFile
/*  12:    */   implements ChunkedInput<ByteBuf>
/*  13:    */ {
/*  14:    */   private final RandomAccessFile file;
/*  15:    */   private final long startOffset;
/*  16:    */   private final long endOffset;
/*  17:    */   private final int chunkSize;
/*  18:    */   private long offset;
/*  19:    */   
/*  20:    */   public ChunkedFile(File file)
/*  21:    */     throws IOException
/*  22:    */   {
/*  23: 45 */     this(file, 8192);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public ChunkedFile(File file, int chunkSize)
/*  27:    */     throws IOException
/*  28:    */   {
/*  29: 55 */     this(new RandomAccessFile(file, "r"), chunkSize);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public ChunkedFile(RandomAccessFile file)
/*  33:    */     throws IOException
/*  34:    */   {
/*  35: 62 */     this(file, 8192);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public ChunkedFile(RandomAccessFile file, int chunkSize)
/*  39:    */     throws IOException
/*  40:    */   {
/*  41: 72 */     this(file, 0L, file.length(), chunkSize);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public ChunkedFile(RandomAccessFile file, long offset, long length, int chunkSize)
/*  45:    */     throws IOException
/*  46:    */   {
/*  47: 84 */     if (file == null) {
/*  48: 85 */       throw new NullPointerException("file");
/*  49:    */     }
/*  50: 87 */     if (offset < 0L) {
/*  51: 88 */       throw new IllegalArgumentException("offset: " + offset + " (expected: 0 or greater)");
/*  52:    */     }
/*  53: 91 */     if (length < 0L) {
/*  54: 92 */       throw new IllegalArgumentException("length: " + length + " (expected: 0 or greater)");
/*  55:    */     }
/*  56: 95 */     if (chunkSize <= 0) {
/*  57: 96 */       throw new IllegalArgumentException("chunkSize: " + chunkSize + " (expected: a positive integer)");
/*  58:    */     }
/*  59:101 */     this.file = file;
/*  60:102 */     this.offset = (this.startOffset = offset);
/*  61:103 */     this.endOffset = (offset + length);
/*  62:104 */     this.chunkSize = chunkSize;
/*  63:    */     
/*  64:106 */     file.seek(offset);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public long startOffset()
/*  68:    */   {
/*  69:113 */     return this.startOffset;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public long endOffset()
/*  73:    */   {
/*  74:120 */     return this.endOffset;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public long currentOffset()
/*  78:    */   {
/*  79:127 */     return this.offset;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public boolean isEndOfInput()
/*  83:    */     throws Exception
/*  84:    */   {
/*  85:132 */     return (this.offset >= this.endOffset) || (!this.file.getChannel().isOpen());
/*  86:    */   }
/*  87:    */   
/*  88:    */   public void close()
/*  89:    */     throws Exception
/*  90:    */   {
/*  91:137 */     this.file.close();
/*  92:    */   }
/*  93:    */   
/*  94:    */   public ByteBuf readChunk(ChannelHandlerContext ctx)
/*  95:    */     throws Exception
/*  96:    */   {
/*  97:142 */     long offset = this.offset;
/*  98:143 */     if (offset >= this.endOffset) {
/*  99:144 */       return null;
/* 100:    */     }
/* 101:147 */     int chunkSize = (int)Math.min(this.chunkSize, this.endOffset - offset);
/* 102:    */     
/* 103:    */ 
/* 104:150 */     ByteBuf buf = ctx.alloc().heapBuffer(chunkSize);
/* 105:151 */     boolean release = true;
/* 106:    */     try
/* 107:    */     {
/* 108:153 */       this.file.readFully(buf.array(), buf.arrayOffset(), chunkSize);
/* 109:154 */       buf.writerIndex(chunkSize);
/* 110:155 */       this.offset = (offset + chunkSize);
/* 111:156 */       release = false;
/* 112:157 */       return buf;
/* 113:    */     }
/* 114:    */     finally
/* 115:    */     {
/* 116:159 */       if (release) {
/* 117:160 */         buf.release();
/* 118:    */       }
/* 119:    */     }
/* 120:    */   }
/* 121:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.stream.ChunkedFile
 * JD-Core Version:    0.7.0.1
 */