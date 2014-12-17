/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufUtil;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.handler.codec.MessageToByteEncoder;
/*   7:    */ 
/*   8:    */ public class SnappyFramedEncoder
/*   9:    */   extends MessageToByteEncoder<ByteBuf>
/*  10:    */ {
/*  11:    */   private static final int MIN_COMPRESSIBLE_LENGTH = 18;
/*  12: 42 */   private static final byte[] STREAM_START = { -1, 6, 0, 0, 115, 78, 97, 80, 112, 89 };
/*  13: 46 */   private final Snappy snappy = new Snappy();
/*  14:    */   private boolean started;
/*  15:    */   
/*  16:    */   protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out)
/*  17:    */     throws Exception
/*  18:    */   {
/*  19: 51 */     if (!in.isReadable()) {
/*  20: 52 */       return;
/*  21:    */     }
/*  22: 55 */     if (!this.started)
/*  23:    */     {
/*  24: 56 */       this.started = true;
/*  25: 57 */       out.writeBytes(STREAM_START);
/*  26:    */     }
/*  27: 60 */     int dataLength = in.readableBytes();
/*  28: 61 */     if (dataLength > 18) {
/*  29:    */       for (;;)
/*  30:    */       {
/*  31: 63 */         int lengthIdx = out.writerIndex() + 1;
/*  32: 64 */         if (dataLength < 18)
/*  33:    */         {
/*  34: 65 */           ByteBuf slice = in.readSlice(dataLength);
/*  35: 66 */           writeUnencodedChunk(slice, out, dataLength);
/*  36: 67 */           break;
/*  37:    */         }
/*  38: 70 */         out.writeInt(0);
/*  39: 71 */         if (dataLength > 32767)
/*  40:    */         {
/*  41: 72 */           ByteBuf slice = in.readSlice(32767);
/*  42: 73 */           calculateAndWriteChecksum(slice, out);
/*  43: 74 */           this.snappy.encode(slice, out, 32767);
/*  44: 75 */           setChunkLength(out, lengthIdx);
/*  45: 76 */           dataLength -= 32767;
/*  46:    */         }
/*  47:    */         else
/*  48:    */         {
/*  49: 78 */           ByteBuf slice = in.readSlice(dataLength);
/*  50: 79 */           calculateAndWriteChecksum(slice, out);
/*  51: 80 */           this.snappy.encode(slice, out, dataLength);
/*  52: 81 */           setChunkLength(out, lengthIdx);
/*  53: 82 */           break;
/*  54:    */         }
/*  55:    */       }
/*  56:    */     }
/*  57: 86 */     writeUnencodedChunk(in, out, dataLength);
/*  58:    */   }
/*  59:    */   
/*  60:    */   private static void writeUnencodedChunk(ByteBuf in, ByteBuf out, int dataLength)
/*  61:    */   {
/*  62: 91 */     out.writeByte(1);
/*  63: 92 */     writeChunkLength(out, dataLength + 4);
/*  64: 93 */     calculateAndWriteChecksum(in, out);
/*  65: 94 */     out.writeBytes(in, dataLength);
/*  66:    */   }
/*  67:    */   
/*  68:    */   private static void setChunkLength(ByteBuf out, int lengthIdx)
/*  69:    */   {
/*  70: 98 */     int chunkLength = out.writerIndex() - lengthIdx - 3;
/*  71: 99 */     if (chunkLength >>> 24 != 0) {
/*  72:100 */       throw new CompressionException("compressed data too large: " + chunkLength);
/*  73:    */     }
/*  74:102 */     out.setMedium(lengthIdx, ByteBufUtil.swapMedium(chunkLength));
/*  75:    */   }
/*  76:    */   
/*  77:    */   private static void writeChunkLength(ByteBuf out, int chunkLength)
/*  78:    */   {
/*  79:112 */     out.writeMedium(ByteBufUtil.swapMedium(chunkLength));
/*  80:    */   }
/*  81:    */   
/*  82:    */   private static void calculateAndWriteChecksum(ByteBuf slice, ByteBuf out)
/*  83:    */   {
/*  84:122 */     out.writeInt(ByteBufUtil.swapInt(Snappy.calculateChecksum(slice)));
/*  85:    */   }
/*  86:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.compression.SnappyFramedEncoder
 * JD-Core Version:    0.7.0.1
 */