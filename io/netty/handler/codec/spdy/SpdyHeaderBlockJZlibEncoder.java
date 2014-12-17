/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import com.jcraft.jzlib.Deflater;
/*   4:    */ import com.jcraft.jzlib.JZlib;
/*   5:    */ import io.netty.buffer.ByteBuf;
/*   6:    */ import io.netty.buffer.ByteBufAllocator;
/*   7:    */ import io.netty.buffer.Unpooled;
/*   8:    */ import io.netty.handler.codec.compression.CompressionException;
/*   9:    */ 
/*  10:    */ class SpdyHeaderBlockJZlibEncoder
/*  11:    */   extends SpdyHeaderBlockRawEncoder
/*  12:    */ {
/*  13: 28 */   private final Deflater z = new Deflater();
/*  14:    */   private boolean finished;
/*  15:    */   
/*  16:    */   SpdyHeaderBlockJZlibEncoder(SpdyVersion version, int compressionLevel, int windowBits, int memLevel)
/*  17:    */   {
/*  18: 34 */     super(version);
/*  19: 35 */     if ((compressionLevel < 0) || (compressionLevel > 9)) {
/*  20: 36 */       throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)");
/*  21:    */     }
/*  22: 39 */     if ((windowBits < 9) || (windowBits > 15)) {
/*  23: 40 */       throw new IllegalArgumentException("windowBits: " + windowBits + " (expected: 9-15)");
/*  24:    */     }
/*  25: 43 */     if ((memLevel < 1) || (memLevel > 9)) {
/*  26: 44 */       throw new IllegalArgumentException("memLevel: " + memLevel + " (expected: 1-9)");
/*  27:    */     }
/*  28: 48 */     int resultCode = this.z.deflateInit(compressionLevel, windowBits, memLevel, JZlib.W_ZLIB);
/*  29: 50 */     if (resultCode != 0) {
/*  30: 51 */       throw new CompressionException("failed to initialize an SPDY header block deflater: " + resultCode);
/*  31:    */     }
/*  32: 54 */     resultCode = this.z.deflateSetDictionary(SpdyCodecUtil.SPDY_DICT, SpdyCodecUtil.SPDY_DICT.length);
/*  33: 55 */     if (resultCode != 0) {
/*  34: 56 */       throw new CompressionException("failed to set the SPDY dictionary: " + resultCode);
/*  35:    */     }
/*  36:    */   }
/*  37:    */   
/*  38:    */   private void setInput(ByteBuf decompressed)
/*  39:    */   {
/*  40: 63 */     byte[] in = new byte[decompressed.readableBytes()];
/*  41: 64 */     decompressed.readBytes(in);
/*  42: 65 */     this.z.next_in = in;
/*  43: 66 */     this.z.next_in_index = 0;
/*  44: 67 */     this.z.avail_in = in.length;
/*  45:    */   }
/*  46:    */   
/*  47:    */   private void encode(ByteBuf compressed)
/*  48:    */   {
/*  49:    */     try
/*  50:    */     {
/*  51: 72 */       byte[] out = new byte[(int)Math.ceil(this.z.next_in.length * 1.001D) + 12];
/*  52: 73 */       this.z.next_out = out;
/*  53: 74 */       this.z.next_out_index = 0;
/*  54: 75 */       this.z.avail_out = out.length;
/*  55:    */       
/*  56: 77 */       int resultCode = this.z.deflate(2);
/*  57: 78 */       if (resultCode != 0) {
/*  58: 79 */         throw new CompressionException("compression failure: " + resultCode);
/*  59:    */       }
/*  60: 82 */       if (this.z.next_out_index != 0) {
/*  61: 83 */         compressed.writeBytes(out, 0, this.z.next_out_index);
/*  62:    */       }
/*  63:    */     }
/*  64:    */     finally
/*  65:    */     {
/*  66: 90 */       this.z.next_in = null;
/*  67: 91 */       this.z.next_out = null;
/*  68:    */     }
/*  69:    */   }
/*  70:    */   
/*  71:    */   public ByteBuf encode(SpdyHeadersFrame frame)
/*  72:    */     throws Exception
/*  73:    */   {
/*  74: 97 */     if (frame == null) {
/*  75: 98 */       throw new IllegalArgumentException("frame");
/*  76:    */     }
/*  77:101 */     if (this.finished) {
/*  78:102 */       return Unpooled.EMPTY_BUFFER;
/*  79:    */     }
/*  80:105 */     ByteBuf decompressed = super.encode(frame);
/*  81:106 */     if (decompressed.readableBytes() == 0) {
/*  82:107 */       return Unpooled.EMPTY_BUFFER;
/*  83:    */     }
/*  84:110 */     ByteBuf compressed = decompressed.alloc().buffer();
/*  85:111 */     setInput(decompressed);
/*  86:112 */     encode(compressed);
/*  87:113 */     return compressed;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public void end()
/*  91:    */   {
/*  92:118 */     if (this.finished) {
/*  93:119 */       return;
/*  94:    */     }
/*  95:121 */     this.finished = true;
/*  96:122 */     this.z.deflateEnd();
/*  97:123 */     this.z.next_in = null;
/*  98:124 */     this.z.next_out = null;
/*  99:    */   }
/* 100:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyHeaderBlockJZlibEncoder
 * JD-Core Version:    0.7.0.1
 */