/*   1:    */ package io.netty.handler.stream;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import java.nio.ByteBuffer;
/*   7:    */ import java.nio.channels.ReadableByteChannel;
/*   8:    */ 
/*   9:    */ public class ChunkedNioStream
/*  10:    */   implements ChunkedInput<ByteBuf>
/*  11:    */ {
/*  12:    */   private final ReadableByteChannel in;
/*  13:    */   private final int chunkSize;
/*  14:    */   private long offset;
/*  15:    */   private final ByteBuffer byteBuffer;
/*  16:    */   
/*  17:    */   public ChunkedNioStream(ReadableByteChannel in)
/*  18:    */   {
/*  19: 45 */     this(in, 8192);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public ChunkedNioStream(ReadableByteChannel in, int chunkSize)
/*  23:    */   {
/*  24: 55 */     if (in == null) {
/*  25: 56 */       throw new NullPointerException("in");
/*  26:    */     }
/*  27: 58 */     if (chunkSize <= 0) {
/*  28: 59 */       throw new IllegalArgumentException("chunkSize: " + chunkSize + " (expected: a positive integer)");
/*  29:    */     }
/*  30: 62 */     this.in = in;
/*  31: 63 */     this.offset = 0L;
/*  32: 64 */     this.chunkSize = chunkSize;
/*  33: 65 */     this.byteBuffer = ByteBuffer.allocate(chunkSize);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public long transferredBytes()
/*  37:    */   {
/*  38: 72 */     return this.offset;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public boolean isEndOfInput()
/*  42:    */     throws Exception
/*  43:    */   {
/*  44: 77 */     if (this.byteBuffer.position() > 0) {
/*  45: 79 */       return false;
/*  46:    */     }
/*  47: 81 */     if (this.in.isOpen())
/*  48:    */     {
/*  49: 83 */       int b = this.in.read(this.byteBuffer);
/*  50: 84 */       if (b < 0) {
/*  51: 85 */         return true;
/*  52:    */       }
/*  53: 87 */       this.offset += b;
/*  54: 88 */       return false;
/*  55:    */     }
/*  56: 91 */     return true;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public void close()
/*  60:    */     throws Exception
/*  61:    */   {
/*  62: 96 */     this.in.close();
/*  63:    */   }
/*  64:    */   
/*  65:    */   public ByteBuf readChunk(ChannelHandlerContext ctx)
/*  66:    */     throws Exception
/*  67:    */   {
/*  68:101 */     if (isEndOfInput()) {
/*  69:102 */       return null;
/*  70:    */     }
/*  71:105 */     int readBytes = this.byteBuffer.position();
/*  72:    */     for (;;)
/*  73:    */     {
/*  74:107 */       int localReadBytes = this.in.read(this.byteBuffer);
/*  75:108 */       if (localReadBytes < 0) {
/*  76:    */         break;
/*  77:    */       }
/*  78:111 */       readBytes += localReadBytes;
/*  79:112 */       this.offset += localReadBytes;
/*  80:113 */       if (readBytes == this.chunkSize) {
/*  81:    */         break;
/*  82:    */       }
/*  83:    */     }
/*  84:117 */     this.byteBuffer.flip();
/*  85:118 */     boolean release = true;
/*  86:119 */     ByteBuf buffer = ctx.alloc().buffer(this.byteBuffer.remaining());
/*  87:    */     try
/*  88:    */     {
/*  89:121 */       buffer.writeBytes(this.byteBuffer);
/*  90:122 */       this.byteBuffer.clear();
/*  91:123 */       release = false;
/*  92:124 */       return buffer;
/*  93:    */     }
/*  94:    */     finally
/*  95:    */     {
/*  96:126 */       if (release) {
/*  97:127 */         buffer.release();
/*  98:    */       }
/*  99:    */     }
/* 100:    */   }
/* 101:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.stream.ChunkedNioStream
 * JD-Core Version:    0.7.0.1
 */