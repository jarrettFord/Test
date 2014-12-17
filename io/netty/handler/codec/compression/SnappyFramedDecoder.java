/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.ByteBufUtil;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*   8:    */ import java.util.Arrays;
/*   9:    */ import java.util.List;
/*  10:    */ 
/*  11:    */ public class SnappyFramedDecoder
/*  12:    */   extends ByteToMessageDecoder
/*  13:    */ {
/*  14:    */   static enum ChunkType
/*  15:    */   {
/*  16: 40 */     STREAM_IDENTIFIER,  COMPRESSED_DATA,  UNCOMPRESSED_DATA,  RESERVED_UNSKIPPABLE,  RESERVED_SKIPPABLE;
/*  17:    */     
/*  18:    */     private ChunkType() {}
/*  19:    */   }
/*  20:    */   
/*  21: 47 */   private static final byte[] SNAPPY = { 115, 78, 97, 80, 112, 89 };
/*  22: 49 */   private final Snappy snappy = new Snappy();
/*  23:    */   private final boolean validateChecksums;
/*  24:    */   private boolean started;
/*  25:    */   private boolean corrupted;
/*  26:    */   
/*  27:    */   public SnappyFramedDecoder()
/*  28:    */   {
/*  29: 61 */     this(false);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public SnappyFramedDecoder(boolean validateChecksums)
/*  33:    */   {
/*  34: 74 */     this.validateChecksums = validateChecksums;
/*  35:    */   }
/*  36:    */   
/*  37:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  38:    */     throws Exception
/*  39:    */   {
/*  40: 79 */     if (this.corrupted)
/*  41:    */     {
/*  42: 80 */       in.skipBytes(in.readableBytes());
/*  43: 81 */       return;
/*  44:    */     }
/*  45:    */     try
/*  46:    */     {
/*  47: 85 */       int idx = in.readerIndex();
/*  48: 86 */       int inSize = in.writerIndex() - idx;
/*  49: 87 */       if (inSize < 4) {
/*  50: 90 */         return;
/*  51:    */       }
/*  52: 93 */       int chunkTypeVal = in.getUnsignedByte(idx);
/*  53: 94 */       ChunkType chunkType = mapChunkType((byte)chunkTypeVal);
/*  54: 95 */       int chunkLength = ByteBufUtil.swapMedium(in.getUnsignedMedium(idx + 1));
/*  55: 97 */       switch (1.$SwitchMap$io$netty$handler$codec$compression$SnappyFramedDecoder$ChunkType[chunkType.ordinal()])
/*  56:    */       {
/*  57:    */       case 1: 
/*  58: 99 */         if (chunkLength != SNAPPY.length) {
/*  59:100 */           throw new DecompressionException("Unexpected length of stream identifier: " + chunkLength);
/*  60:    */         }
/*  61:103 */         if (inSize >= 4 + SNAPPY.length)
/*  62:    */         {
/*  63:107 */           byte[] identifier = new byte[chunkLength];
/*  64:108 */           in.skipBytes(4).readBytes(identifier);
/*  65:110 */           if (!Arrays.equals(identifier, SNAPPY)) {
/*  66:111 */             throw new DecompressionException("Unexpected stream identifier contents. Mismatched snappy protocol version?");
/*  67:    */           }
/*  68:115 */           this.started = true;
/*  69:    */         }
/*  70:116 */         break;
/*  71:    */       case 2: 
/*  72:118 */         if (!this.started) {
/*  73:119 */           throw new DecompressionException("Received RESERVED_SKIPPABLE tag before STREAM_IDENTIFIER");
/*  74:    */         }
/*  75:122 */         if (inSize < 4 + chunkLength) {
/*  76:124 */           return;
/*  77:    */         }
/*  78:127 */         in.skipBytes(4 + chunkLength);
/*  79:128 */         break;
/*  80:    */       case 3: 
/*  81:133 */         throw new DecompressionException("Found reserved unskippable chunk type: 0x" + Integer.toHexString(chunkTypeVal));
/*  82:    */       case 4: 
/*  83:136 */         if (!this.started) {
/*  84:137 */           throw new DecompressionException("Received UNCOMPRESSED_DATA tag before STREAM_IDENTIFIER");
/*  85:    */         }
/*  86:139 */         if (chunkLength > 65540) {
/*  87:140 */           throw new DecompressionException("Received UNCOMPRESSED_DATA larger than 65540 bytes");
/*  88:    */         }
/*  89:143 */         if (inSize < 4 + chunkLength) {
/*  90:144 */           return;
/*  91:    */         }
/*  92:147 */         in.skipBytes(4);
/*  93:148 */         if (this.validateChecksums)
/*  94:    */         {
/*  95:149 */           int checksum = ByteBufUtil.swapInt(in.readInt());
/*  96:150 */           Snappy.validateChecksum(checksum, in, in.readerIndex(), chunkLength - 4);
/*  97:    */         }
/*  98:    */         else
/*  99:    */         {
/* 100:152 */           in.skipBytes(4);
/* 101:    */         }
/* 102:154 */         out.add(in.readSlice(chunkLength - 4).retain());
/* 103:155 */         break;
/* 104:    */       case 5: 
/* 105:157 */         if (!this.started) {
/* 106:158 */           throw new DecompressionException("Received COMPRESSED_DATA tag before STREAM_IDENTIFIER");
/* 107:    */         }
/* 108:161 */         if (inSize < 4 + chunkLength) {
/* 109:162 */           return;
/* 110:    */         }
/* 111:165 */         in.skipBytes(4);
/* 112:166 */         int checksum = ByteBufUtil.swapInt(in.readInt());
/* 113:167 */         ByteBuf uncompressed = ctx.alloc().buffer(0);
/* 114:168 */         if (this.validateChecksums)
/* 115:    */         {
/* 116:169 */           int oldWriterIndex = in.writerIndex();
/* 117:    */           try
/* 118:    */           {
/* 119:171 */             in.writerIndex(in.readerIndex() + chunkLength - 4);
/* 120:172 */             this.snappy.decode(in, uncompressed);
/* 121:    */           }
/* 122:    */           finally
/* 123:    */           {
/* 124:174 */             in.writerIndex(oldWriterIndex);
/* 125:    */           }
/* 126:176 */           Snappy.validateChecksum(checksum, uncompressed, 0, uncompressed.writerIndex());
/* 127:    */         }
/* 128:    */         else
/* 129:    */         {
/* 130:178 */           this.snappy.decode(in.readSlice(chunkLength - 4), uncompressed);
/* 131:    */         }
/* 132:180 */         out.add(uncompressed);
/* 133:181 */         this.snappy.reset();
/* 134:    */       }
/* 135:    */     }
/* 136:    */     catch (Exception e)
/* 137:    */     {
/* 138:185 */       this.corrupted = true;
/* 139:186 */       throw e;
/* 140:    */     }
/* 141:    */   }
/* 142:    */   
/* 143:    */   static ChunkType mapChunkType(byte type)
/* 144:    */   {
/* 145:197 */     if (type == 0) {
/* 146:198 */       return ChunkType.COMPRESSED_DATA;
/* 147:    */     }
/* 148:199 */     if (type == 1) {
/* 149:200 */       return ChunkType.UNCOMPRESSED_DATA;
/* 150:    */     }
/* 151:201 */     if (type == -1) {
/* 152:202 */       return ChunkType.STREAM_IDENTIFIER;
/* 153:    */     }
/* 154:203 */     if ((type & 0x80) == 128) {
/* 155:204 */       return ChunkType.RESERVED_SKIPPABLE;
/* 156:    */     }
/* 157:206 */     return ChunkType.RESERVED_UNSKIPPABLE;
/* 158:    */   }
/* 159:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.compression.SnappyFramedDecoder
 * JD-Core Version:    0.7.0.1
 */