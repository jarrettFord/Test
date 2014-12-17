/*  1:   */ package io.netty.handler.codec.spdy;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.Unpooled;
/*  5:   */ import java.util.Set;
/*  6:   */ 
/*  7:   */ public class SpdyHeaderBlockRawEncoder
/*  8:   */   extends SpdyHeaderBlockEncoder
/*  9:   */ {
/* 10:   */   private final int version;
/* 11:   */   
/* 12:   */   public SpdyHeaderBlockRawEncoder(SpdyVersion version)
/* 13:   */   {
/* 14:30 */     if (version == null) {
/* 15:31 */       throw new NullPointerException("version");
/* 16:   */     }
/* 17:33 */     this.version = version.getVersion();
/* 18:   */   }
/* 19:   */   
/* 20:   */   private void setLengthField(ByteBuf buffer, int writerIndex, int length)
/* 21:   */   {
/* 22:37 */     buffer.setInt(writerIndex, length);
/* 23:   */   }
/* 24:   */   
/* 25:   */   private void writeLengthField(ByteBuf buffer, int length)
/* 26:   */   {
/* 27:41 */     buffer.writeInt(length);
/* 28:   */   }
/* 29:   */   
/* 30:   */   public ByteBuf encode(SpdyHeadersFrame frame)
/* 31:   */     throws Exception
/* 32:   */   {
/* 33:46 */     Set<String> names = frame.headers().names();
/* 34:47 */     int numHeaders = names.size();
/* 35:48 */     if (numHeaders == 0) {
/* 36:49 */       return Unpooled.EMPTY_BUFFER;
/* 37:   */     }
/* 38:51 */     if (numHeaders > 65535) {
/* 39:52 */       throw new IllegalArgumentException("header block contains too many headers");
/* 40:   */     }
/* 41:55 */     ByteBuf headerBlock = Unpooled.buffer();
/* 42:56 */     writeLengthField(headerBlock, numHeaders);
/* 43:57 */     for (String name : names)
/* 44:   */     {
/* 45:58 */       byte[] nameBytes = name.getBytes("UTF-8");
/* 46:59 */       writeLengthField(headerBlock, nameBytes.length);
/* 47:60 */       headerBlock.writeBytes(nameBytes);
/* 48:61 */       int savedIndex = headerBlock.writerIndex();
/* 49:62 */       int valueLength = 0;
/* 50:63 */       writeLengthField(headerBlock, valueLength);
/* 51:64 */       for (String value : frame.headers().getAll(name))
/* 52:   */       {
/* 53:65 */         byte[] valueBytes = value.getBytes("UTF-8");
/* 54:66 */         if (valueBytes.length > 0)
/* 55:   */         {
/* 56:67 */           headerBlock.writeBytes(valueBytes);
/* 57:68 */           headerBlock.writeByte(0);
/* 58:69 */           valueLength += valueBytes.length + 1;
/* 59:   */         }
/* 60:   */       }
/* 61:72 */       if (valueLength != 0) {
/* 62:73 */         valueLength--;
/* 63:   */       }
/* 64:75 */       if (valueLength > 65535) {
/* 65:76 */         throw new IllegalArgumentException("header exceeds allowable length: " + name);
/* 66:   */       }
/* 67:79 */       if (valueLength > 0)
/* 68:   */       {
/* 69:80 */         setLengthField(headerBlock, savedIndex, valueLength);
/* 70:81 */         headerBlock.writerIndex(headerBlock.writerIndex() - 1);
/* 71:   */       }
/* 72:   */     }
/* 73:84 */     return headerBlock;
/* 74:   */   }
/* 75:   */   
/* 76:   */   void end() {}
/* 77:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyHeaderBlockRawEncoder
 * JD-Core Version:    0.7.0.1
 */