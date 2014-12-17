/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import java.util.zip.DataFormatException;
/*   6:    */ import java.util.zip.Inflater;
/*   7:    */ 
/*   8:    */ final class SpdyHeaderBlockZlibDecoder
/*   9:    */   extends SpdyHeaderBlockRawDecoder
/*  10:    */ {
/*  11:    */   private static final int DEFAULT_BUFFER_CAPACITY = 4096;
/*  12: 29 */   private static final SpdyProtocolException INVALID_HEADER_BLOCK = new SpdyProtocolException("Invalid Header Block");
/*  13: 32 */   private final Inflater decompressor = new Inflater();
/*  14:    */   private ByteBuf decompressed;
/*  15:    */   
/*  16:    */   SpdyHeaderBlockZlibDecoder(SpdyVersion spdyVersion, int maxHeaderSize)
/*  17:    */   {
/*  18: 37 */     super(spdyVersion, maxHeaderSize);
/*  19:    */   }
/*  20:    */   
/*  21:    */   void decode(ByteBuf headerBlock, SpdyHeadersFrame frame)
/*  22:    */     throws Exception
/*  23:    */   {
/*  24: 42 */     int len = setInput(headerBlock);
/*  25:    */     int numBytes;
/*  26:    */     do
/*  27:    */     {
/*  28: 46 */       numBytes = decompress(headerBlock.alloc(), frame);
/*  29: 47 */     } while (numBytes > 0);
/*  30: 51 */     if (this.decompressor.getRemaining() != 0) {
/*  31: 53 */       throw INVALID_HEADER_BLOCK;
/*  32:    */     }
/*  33: 56 */     headerBlock.skipBytes(len);
/*  34:    */   }
/*  35:    */   
/*  36:    */   private int setInput(ByteBuf compressed)
/*  37:    */   {
/*  38: 60 */     int len = compressed.readableBytes();
/*  39: 62 */     if (compressed.hasArray())
/*  40:    */     {
/*  41: 63 */       this.decompressor.setInput(compressed.array(), compressed.arrayOffset() + compressed.readerIndex(), len);
/*  42:    */     }
/*  43:    */     else
/*  44:    */     {
/*  45: 65 */       byte[] in = new byte[len];
/*  46: 66 */       compressed.getBytes(compressed.readerIndex(), in);
/*  47: 67 */       this.decompressor.setInput(in, 0, in.length);
/*  48:    */     }
/*  49: 70 */     return len;
/*  50:    */   }
/*  51:    */   
/*  52:    */   private int decompress(ByteBufAllocator alloc, SpdyHeadersFrame frame)
/*  53:    */     throws Exception
/*  54:    */   {
/*  55: 74 */     ensureBuffer(alloc);
/*  56: 75 */     byte[] out = this.decompressed.array();
/*  57: 76 */     int off = this.decompressed.arrayOffset() + this.decompressed.writerIndex();
/*  58:    */     try
/*  59:    */     {
/*  60: 78 */       int numBytes = this.decompressor.inflate(out, off, this.decompressed.writableBytes());
/*  61: 79 */       if ((numBytes == 0) && (this.decompressor.needsDictionary()))
/*  62:    */       {
/*  63:    */         try
/*  64:    */         {
/*  65: 81 */           this.decompressor.setDictionary(SpdyCodecUtil.SPDY_DICT);
/*  66:    */         }
/*  67:    */         catch (IllegalArgumentException ignored)
/*  68:    */         {
/*  69: 83 */           throw INVALID_HEADER_BLOCK;
/*  70:    */         }
/*  71: 85 */         numBytes = this.decompressor.inflate(out, off, this.decompressed.writableBytes());
/*  72:    */       }
/*  73: 87 */       if (frame != null)
/*  74:    */       {
/*  75: 88 */         this.decompressed.writerIndex(this.decompressed.writerIndex() + numBytes);
/*  76: 89 */         decodeHeaderBlock(this.decompressed, frame);
/*  77: 90 */         this.decompressed.discardReadBytes();
/*  78:    */       }
/*  79: 93 */       return numBytes;
/*  80:    */     }
/*  81:    */     catch (DataFormatException e)
/*  82:    */     {
/*  83: 95 */       throw new SpdyProtocolException("Received invalid header block", e);
/*  84:    */     }
/*  85:    */   }
/*  86:    */   
/*  87:    */   private void ensureBuffer(ByteBufAllocator alloc)
/*  88:    */   {
/*  89:100 */     if (this.decompressed == null) {
/*  90:101 */       this.decompressed = alloc.heapBuffer(4096);
/*  91:    */     }
/*  92:103 */     this.decompressed.ensureWritable(1);
/*  93:    */   }
/*  94:    */   
/*  95:    */   void endHeaderBlock(SpdyHeadersFrame frame)
/*  96:    */     throws Exception
/*  97:    */   {
/*  98:108 */     super.endHeaderBlock(frame);
/*  99:109 */     releaseBuffer();
/* 100:    */   }
/* 101:    */   
/* 102:    */   public void end()
/* 103:    */   {
/* 104:114 */     super.end();
/* 105:115 */     releaseBuffer();
/* 106:116 */     this.decompressor.end();
/* 107:    */   }
/* 108:    */   
/* 109:    */   private void releaseBuffer()
/* 110:    */   {
/* 111:120 */     if (this.decompressed != null)
/* 112:    */     {
/* 113:121 */       this.decompressed.release();
/* 114:122 */       this.decompressed = null;
/* 115:    */     }
/* 116:    */   }
/* 117:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyHeaderBlockZlibDecoder
 * JD-Core Version:    0.7.0.1
 */