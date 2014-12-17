/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import java.nio.ByteOrder;
/*   6:    */ import java.util.Set;
/*   7:    */ 
/*   8:    */ public class SpdyFrameEncoder
/*   9:    */ {
/*  10:    */   private final int version;
/*  11:    */   
/*  12:    */   public SpdyFrameEncoder(SpdyVersion spdyVersion)
/*  13:    */   {
/*  14: 37 */     if (spdyVersion == null) {
/*  15: 38 */       throw new NullPointerException("spdyVersion");
/*  16:    */     }
/*  17: 40 */     this.version = spdyVersion.getVersion();
/*  18:    */   }
/*  19:    */   
/*  20:    */   private void writeControlFrameHeader(ByteBuf buffer, int type, byte flags, int length)
/*  21:    */   {
/*  22: 44 */     buffer.writeShort(this.version | 0x8000);
/*  23: 45 */     buffer.writeShort(type);
/*  24: 46 */     buffer.writeByte(flags);
/*  25: 47 */     buffer.writeMedium(length);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public ByteBuf encodeDataFrame(ByteBufAllocator allocator, int streamId, boolean last, ByteBuf data)
/*  29:    */   {
/*  30: 51 */     byte flags = last ? 1 : 0;
/*  31: 52 */     int length = data.readableBytes();
/*  32: 53 */     ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
/*  33: 54 */     frame.writeInt(streamId & 0x7FFFFFFF);
/*  34: 55 */     frame.writeByte(flags);
/*  35: 56 */     frame.writeMedium(length);
/*  36: 57 */     frame.writeBytes(data, data.readerIndex(), length);
/*  37: 58 */     return frame;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public ByteBuf encodeSynStreamFrame(ByteBufAllocator allocator, int streamId, int associatedToStreamId, byte priority, boolean last, boolean unidirectional, ByteBuf headerBlock)
/*  41:    */   {
/*  42: 63 */     int headerBlockLength = headerBlock.readableBytes();
/*  43: 64 */     byte flags = last ? 1 : 0;
/*  44: 65 */     if (unidirectional) {
/*  45: 66 */       flags = (byte)(flags | 0x2);
/*  46:    */     }
/*  47: 68 */     int length = 10 + headerBlockLength;
/*  48: 69 */     ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
/*  49: 70 */     writeControlFrameHeader(frame, 1, flags, length);
/*  50: 71 */     frame.writeInt(streamId);
/*  51: 72 */     frame.writeInt(associatedToStreamId);
/*  52: 73 */     frame.writeShort((priority & 0xFF) << 13);
/*  53: 74 */     frame.writeBytes(headerBlock, headerBlock.readerIndex(), headerBlockLength);
/*  54: 75 */     return frame;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public ByteBuf encodeSynReplyFrame(ByteBufAllocator allocator, int streamId, boolean last, ByteBuf headerBlock)
/*  58:    */   {
/*  59: 79 */     int headerBlockLength = headerBlock.readableBytes();
/*  60: 80 */     byte flags = last ? 1 : 0;
/*  61: 81 */     int length = 4 + headerBlockLength;
/*  62: 82 */     ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
/*  63: 83 */     writeControlFrameHeader(frame, 2, flags, length);
/*  64: 84 */     frame.writeInt(streamId);
/*  65: 85 */     frame.writeBytes(headerBlock, headerBlock.readerIndex(), headerBlockLength);
/*  66: 86 */     return frame;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public ByteBuf encodeRstStreamFrame(ByteBufAllocator allocator, int streamId, int statusCode)
/*  70:    */   {
/*  71: 90 */     byte flags = 0;
/*  72: 91 */     int length = 8;
/*  73: 92 */     ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
/*  74: 93 */     writeControlFrameHeader(frame, 3, flags, length);
/*  75: 94 */     frame.writeInt(streamId);
/*  76: 95 */     frame.writeInt(statusCode);
/*  77: 96 */     return frame;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public ByteBuf encodeSettingsFrame(ByteBufAllocator allocator, SpdySettingsFrame spdySettingsFrame)
/*  81:    */   {
/*  82:100 */     Set<Integer> ids = spdySettingsFrame.ids();
/*  83:101 */     int numSettings = ids.size();
/*  84:    */     
/*  85:103 */     byte flags = spdySettingsFrame.clearPreviouslyPersistedSettings() ? 1 : 0;
/*  86:    */     
/*  87:105 */     int length = 4 + 8 * numSettings;
/*  88:106 */     ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
/*  89:107 */     writeControlFrameHeader(frame, 4, flags, length);
/*  90:108 */     frame.writeInt(numSettings);
/*  91:109 */     for (Integer id : ids)
/*  92:    */     {
/*  93:110 */       flags = 0;
/*  94:111 */       if (spdySettingsFrame.isPersistValue(id.intValue())) {
/*  95:112 */         flags = (byte)(flags | 0x1);
/*  96:    */       }
/*  97:114 */       if (spdySettingsFrame.isPersisted(id.intValue())) {
/*  98:115 */         flags = (byte)(flags | 0x2);
/*  99:    */       }
/* 100:117 */       frame.writeByte(flags);
/* 101:118 */       frame.writeMedium(id.intValue());
/* 102:119 */       frame.writeInt(spdySettingsFrame.getValue(id.intValue()));
/* 103:    */     }
/* 104:121 */     return frame;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public ByteBuf encodePingFrame(ByteBufAllocator allocator, int id)
/* 108:    */   {
/* 109:125 */     byte flags = 0;
/* 110:126 */     int length = 4;
/* 111:127 */     ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
/* 112:128 */     writeControlFrameHeader(frame, 6, flags, length);
/* 113:129 */     frame.writeInt(id);
/* 114:130 */     return frame;
/* 115:    */   }
/* 116:    */   
/* 117:    */   public ByteBuf encodeGoAwayFrame(ByteBufAllocator allocator, int lastGoodStreamId, int statusCode)
/* 118:    */   {
/* 119:134 */     byte flags = 0;
/* 120:135 */     int length = 8;
/* 121:136 */     ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
/* 122:137 */     writeControlFrameHeader(frame, 7, flags, length);
/* 123:138 */     frame.writeInt(lastGoodStreamId);
/* 124:139 */     frame.writeInt(statusCode);
/* 125:140 */     return frame;
/* 126:    */   }
/* 127:    */   
/* 128:    */   public ByteBuf encodeHeadersFrame(ByteBufAllocator allocator, int streamId, boolean last, ByteBuf headerBlock)
/* 129:    */   {
/* 130:144 */     int headerBlockLength = headerBlock.readableBytes();
/* 131:145 */     byte flags = last ? 1 : 0;
/* 132:146 */     int length = 4 + headerBlockLength;
/* 133:147 */     ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
/* 134:148 */     writeControlFrameHeader(frame, 8, flags, length);
/* 135:149 */     frame.writeInt(streamId);
/* 136:150 */     frame.writeBytes(headerBlock, headerBlock.readerIndex(), headerBlockLength);
/* 137:151 */     return frame;
/* 138:    */   }
/* 139:    */   
/* 140:    */   public ByteBuf encodeWindowUpdateFrame(ByteBufAllocator allocator, int streamId, int deltaWindowSize)
/* 141:    */   {
/* 142:155 */     byte flags = 0;
/* 143:156 */     int length = 8;
/* 144:157 */     ByteBuf frame = allocator.ioBuffer(8 + length).order(ByteOrder.BIG_ENDIAN);
/* 145:158 */     writeControlFrameHeader(frame, 9, flags, length);
/* 146:159 */     frame.writeInt(streamId);
/* 147:160 */     frame.writeInt(deltaWindowSize);
/* 148:161 */     return frame;
/* 149:    */   }
/* 150:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyFrameEncoder
 * JD-Core Version:    0.7.0.1
 */