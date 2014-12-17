/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import java.util.zip.Deflater;
/*   7:    */ 
/*   8:    */ class SpdyHeaderBlockZlibEncoder
/*   9:    */   extends SpdyHeaderBlockRawEncoder
/*  10:    */ {
/*  11:    */   private final Deflater compressor;
/*  12:    */   private boolean finished;
/*  13:    */   
/*  14:    */   SpdyHeaderBlockZlibEncoder(SpdyVersion spdyVersion, int compressionLevel)
/*  15:    */   {
/*  16: 32 */     super(spdyVersion);
/*  17: 33 */     if ((compressionLevel < 0) || (compressionLevel > 9)) {
/*  18: 34 */       throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)");
/*  19:    */     }
/*  20: 37 */     this.compressor = new Deflater(compressionLevel);
/*  21: 38 */     this.compressor.setDictionary(SpdyCodecUtil.SPDY_DICT);
/*  22:    */   }
/*  23:    */   
/*  24:    */   private int setInput(ByteBuf decompressed)
/*  25:    */   {
/*  26: 42 */     int len = decompressed.readableBytes();
/*  27: 44 */     if (decompressed.hasArray())
/*  28:    */     {
/*  29: 45 */       this.compressor.setInput(decompressed.array(), decompressed.arrayOffset() + decompressed.readerIndex(), len);
/*  30:    */     }
/*  31:    */     else
/*  32:    */     {
/*  33: 47 */       byte[] in = new byte[len];
/*  34: 48 */       decompressed.getBytes(decompressed.readerIndex(), in);
/*  35: 49 */       this.compressor.setInput(in, 0, in.length);
/*  36:    */     }
/*  37: 52 */     return len;
/*  38:    */   }
/*  39:    */   
/*  40:    */   private void encode(ByteBuf compressed)
/*  41:    */   {
/*  42: 56 */     while (compressInto(compressed)) {
/*  43: 58 */       compressed.ensureWritable(compressed.capacity() << 1);
/*  44:    */     }
/*  45:    */   }
/*  46:    */   
/*  47:    */   private boolean compressInto(ByteBuf compressed)
/*  48:    */   {
/*  49: 63 */     byte[] out = compressed.array();
/*  50: 64 */     int off = compressed.arrayOffset() + compressed.writerIndex();
/*  51: 65 */     int toWrite = compressed.writableBytes();
/*  52: 66 */     int numBytes = this.compressor.deflate(out, off, toWrite, 2);
/*  53: 67 */     compressed.writerIndex(compressed.writerIndex() + numBytes);
/*  54: 68 */     return numBytes == toWrite;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public ByteBuf encode(SpdyHeadersFrame frame)
/*  58:    */     throws Exception
/*  59:    */   {
/*  60: 73 */     if (frame == null) {
/*  61: 74 */       throw new IllegalArgumentException("frame");
/*  62:    */     }
/*  63: 77 */     if (this.finished) {
/*  64: 78 */       return Unpooled.EMPTY_BUFFER;
/*  65:    */     }
/*  66: 81 */     ByteBuf decompressed = super.encode(frame);
/*  67: 82 */     if (decompressed.readableBytes() == 0) {
/*  68: 83 */       return Unpooled.EMPTY_BUFFER;
/*  69:    */     }
/*  70: 86 */     ByteBuf compressed = decompressed.alloc().heapBuffer(decompressed.readableBytes());
/*  71: 87 */     int len = setInput(decompressed);
/*  72: 88 */     encode(compressed);
/*  73: 89 */     decompressed.skipBytes(len);
/*  74:    */     
/*  75: 91 */     return compressed;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public void end()
/*  79:    */   {
/*  80: 96 */     if (this.finished) {
/*  81: 97 */       return;
/*  82:    */     }
/*  83: 99 */     this.finished = true;
/*  84:100 */     this.compressor.end();
/*  85:101 */     super.end();
/*  86:    */   }
/*  87:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyHeaderBlockZlibEncoder
 * JD-Core Version:    0.7.0.1
 */