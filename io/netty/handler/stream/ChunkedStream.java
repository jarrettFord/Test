/*   1:    */ package io.netty.handler.stream;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.io.PushbackInputStream;
/*   8:    */ 
/*   9:    */ public class ChunkedStream
/*  10:    */   implements ChunkedInput<ByteBuf>
/*  11:    */ {
/*  12:    */   static final int DEFAULT_CHUNK_SIZE = 8192;
/*  13:    */   private final PushbackInputStream in;
/*  14:    */   private final int chunkSize;
/*  15:    */   private long offset;
/*  16:    */   
/*  17:    */   public ChunkedStream(InputStream in)
/*  18:    */   {
/*  19: 46 */     this(in, 8192);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public ChunkedStream(InputStream in, int chunkSize)
/*  23:    */   {
/*  24: 56 */     if (in == null) {
/*  25: 57 */       throw new NullPointerException("in");
/*  26:    */     }
/*  27: 59 */     if (chunkSize <= 0) {
/*  28: 60 */       throw new IllegalArgumentException("chunkSize: " + chunkSize + " (expected: a positive integer)");
/*  29:    */     }
/*  30: 65 */     if ((in instanceof PushbackInputStream)) {
/*  31: 66 */       this.in = ((PushbackInputStream)in);
/*  32:    */     } else {
/*  33: 68 */       this.in = new PushbackInputStream(in);
/*  34:    */     }
/*  35: 70 */     this.chunkSize = chunkSize;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public long transferredBytes()
/*  39:    */   {
/*  40: 77 */     return this.offset;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public boolean isEndOfInput()
/*  44:    */     throws Exception
/*  45:    */   {
/*  46: 82 */     int b = this.in.read();
/*  47: 83 */     if (b < 0) {
/*  48: 84 */       return true;
/*  49:    */     }
/*  50: 86 */     this.in.unread(b);
/*  51: 87 */     return false;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public void close()
/*  55:    */     throws Exception
/*  56:    */   {
/*  57: 93 */     this.in.close();
/*  58:    */   }
/*  59:    */   
/*  60:    */   public ByteBuf readChunk(ChannelHandlerContext ctx)
/*  61:    */     throws Exception
/*  62:    */   {
/*  63: 98 */     if (isEndOfInput()) {
/*  64: 99 */       return null;
/*  65:    */     }
/*  66:102 */     int availableBytes = this.in.available();
/*  67:    */     int chunkSize;
/*  68:    */     int chunkSize;
/*  69:104 */     if (availableBytes <= 0) {
/*  70:105 */       chunkSize = this.chunkSize;
/*  71:    */     } else {
/*  72:107 */       chunkSize = Math.min(this.chunkSize, this.in.available());
/*  73:    */     }
/*  74:110 */     boolean release = true;
/*  75:111 */     ByteBuf buffer = ctx.alloc().buffer(chunkSize);
/*  76:    */     try
/*  77:    */     {
/*  78:114 */       this.offset += buffer.writeBytes(this.in, chunkSize);
/*  79:115 */       release = false;
/*  80:116 */       return buffer;
/*  81:    */     }
/*  82:    */     finally
/*  83:    */     {
/*  84:118 */       if (release) {
/*  85:119 */         buffer.release();
/*  86:    */       }
/*  87:    */     }
/*  88:    */   }
/*  89:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.stream.ChunkedStream
 * JD-Core Version:    0.7.0.1
 */