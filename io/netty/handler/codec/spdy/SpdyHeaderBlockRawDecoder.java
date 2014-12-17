/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ 
/*   6:    */ public class SpdyHeaderBlockRawDecoder
/*   7:    */   extends SpdyHeaderBlockDecoder
/*   8:    */ {
/*   9:    */   private static final int LENGTH_FIELD_SIZE = 4;
/*  10:    */   private final int maxHeaderSize;
/*  11:    */   private State state;
/*  12:    */   private ByteBuf cumulation;
/*  13:    */   private int headerSize;
/*  14:    */   private int numHeaders;
/*  15:    */   private int length;
/*  16:    */   private String name;
/*  17:    */   
/*  18:    */   private static enum State
/*  19:    */   {
/*  20: 38 */     READ_NUM_HEADERS,  READ_NAME_LENGTH,  READ_NAME,  SKIP_NAME,  READ_VALUE_LENGTH,  READ_VALUE,  SKIP_VALUE,  END_HEADER_BLOCK,  ERROR;
/*  21:    */     
/*  22:    */     private State() {}
/*  23:    */   }
/*  24:    */   
/*  25:    */   public SpdyHeaderBlockRawDecoder(SpdyVersion spdyVersion, int maxHeaderSize)
/*  26:    */   {
/*  27: 50 */     if (spdyVersion == null) {
/*  28: 51 */       throw new NullPointerException("spdyVersion");
/*  29:    */     }
/*  30: 53 */     this.maxHeaderSize = maxHeaderSize;
/*  31: 54 */     this.state = State.READ_NUM_HEADERS;
/*  32:    */   }
/*  33:    */   
/*  34:    */   private static int readLengthField(ByteBuf buffer)
/*  35:    */   {
/*  36: 58 */     int length = SpdyCodecUtil.getSignedInt(buffer, buffer.readerIndex());
/*  37: 59 */     buffer.skipBytes(4);
/*  38: 60 */     return length;
/*  39:    */   }
/*  40:    */   
/*  41:    */   void decode(ByteBuf headerBlock, SpdyHeadersFrame frame)
/*  42:    */     throws Exception
/*  43:    */   {
/*  44: 65 */     if (headerBlock == null) {
/*  45: 66 */       throw new NullPointerException("headerBlock");
/*  46:    */     }
/*  47: 68 */     if (frame == null) {
/*  48: 69 */       throw new NullPointerException("frame");
/*  49:    */     }
/*  50: 72 */     if (this.cumulation == null)
/*  51:    */     {
/*  52: 73 */       decodeHeaderBlock(headerBlock, frame);
/*  53: 74 */       if (headerBlock.isReadable())
/*  54:    */       {
/*  55: 75 */         this.cumulation = headerBlock.alloc().buffer(headerBlock.readableBytes());
/*  56: 76 */         this.cumulation.writeBytes(headerBlock);
/*  57:    */       }
/*  58:    */     }
/*  59:    */     else
/*  60:    */     {
/*  61: 79 */       this.cumulation.writeBytes(headerBlock);
/*  62: 80 */       decodeHeaderBlock(this.cumulation, frame);
/*  63: 81 */       if (this.cumulation.isReadable()) {
/*  64: 82 */         this.cumulation.discardReadBytes();
/*  65:    */       } else {
/*  66: 84 */         releaseBuffer();
/*  67:    */       }
/*  68:    */     }
/*  69:    */   }
/*  70:    */   
/*  71:    */   protected void decodeHeaderBlock(ByteBuf headerBlock, SpdyHeadersFrame frame)
/*  72:    */     throws Exception
/*  73:    */   {
/*  74: 91 */     while (headerBlock.isReadable())
/*  75:    */     {
/*  76:    */       int skipLength;
/*  77: 92 */       switch (1.$SwitchMap$io$netty$handler$codec$spdy$SpdyHeaderBlockRawDecoder$State[this.state.ordinal()])
/*  78:    */       {
/*  79:    */       case 1: 
/*  80: 94 */         if (headerBlock.readableBytes() < 4) {
/*  81: 95 */           return;
/*  82:    */         }
/*  83: 98 */         this.numHeaders = readLengthField(headerBlock);
/*  84:100 */         if (this.numHeaders < 0)
/*  85:    */         {
/*  86:101 */           this.state = State.ERROR;
/*  87:102 */           frame.setInvalid();
/*  88:    */         }
/*  89:103 */         else if (this.numHeaders == 0)
/*  90:    */         {
/*  91:104 */           this.state = State.END_HEADER_BLOCK;
/*  92:    */         }
/*  93:    */         else
/*  94:    */         {
/*  95:106 */           this.state = State.READ_NAME_LENGTH;
/*  96:    */         }
/*  97:108 */         break;
/*  98:    */       case 2: 
/*  99:111 */         if (headerBlock.readableBytes() < 4) {
/* 100:112 */           return;
/* 101:    */         }
/* 102:115 */         this.length = readLengthField(headerBlock);
/* 103:118 */         if (this.length <= 0)
/* 104:    */         {
/* 105:119 */           this.state = State.ERROR;
/* 106:120 */           frame.setInvalid();
/* 107:    */         }
/* 108:121 */         else if ((this.length > this.maxHeaderSize) || (this.headerSize > this.maxHeaderSize - this.length))
/* 109:    */         {
/* 110:122 */           this.headerSize = (this.maxHeaderSize + 1);
/* 111:123 */           this.state = State.SKIP_NAME;
/* 112:124 */           frame.setTruncated();
/* 113:    */         }
/* 114:    */         else
/* 115:    */         {
/* 116:126 */           this.headerSize += this.length;
/* 117:127 */           this.state = State.READ_NAME;
/* 118:    */         }
/* 119:129 */         break;
/* 120:    */       case 3: 
/* 121:132 */         if (headerBlock.readableBytes() < this.length) {
/* 122:133 */           return;
/* 123:    */         }
/* 124:136 */         byte[] nameBytes = new byte[this.length];
/* 125:137 */         headerBlock.readBytes(nameBytes);
/* 126:138 */         this.name = new String(nameBytes, "UTF-8");
/* 127:141 */         if (frame.headers().contains(this.name))
/* 128:    */         {
/* 129:142 */           this.state = State.ERROR;
/* 130:143 */           frame.setInvalid();
/* 131:    */         }
/* 132:    */         else
/* 133:    */         {
/* 134:145 */           this.state = State.READ_VALUE_LENGTH;
/* 135:    */         }
/* 136:147 */         break;
/* 137:    */       case 4: 
/* 138:150 */         skipLength = Math.min(headerBlock.readableBytes(), this.length);
/* 139:151 */         headerBlock.skipBytes(skipLength);
/* 140:152 */         this.length -= skipLength;
/* 141:154 */         if (this.length == 0) {
/* 142:155 */           this.state = State.READ_VALUE_LENGTH;
/* 143:    */         }
/* 144:    */         break;
/* 145:    */       case 5: 
/* 146:160 */         if (headerBlock.readableBytes() < 4) {
/* 147:161 */           return;
/* 148:    */         }
/* 149:164 */         this.length = readLengthField(headerBlock);
/* 150:167 */         if (this.length < 0)
/* 151:    */         {
/* 152:168 */           this.state = State.ERROR;
/* 153:169 */           frame.setInvalid();
/* 154:    */         }
/* 155:170 */         else if (this.length == 0)
/* 156:    */         {
/* 157:171 */           if (!frame.isTruncated()) {
/* 158:173 */             frame.headers().add(this.name, "");
/* 159:    */           }
/* 160:176 */           this.name = null;
/* 161:177 */           if (--this.numHeaders == 0) {
/* 162:178 */             this.state = State.END_HEADER_BLOCK;
/* 163:    */           } else {
/* 164:180 */             this.state = State.READ_NAME_LENGTH;
/* 165:    */           }
/* 166:    */         }
/* 167:183 */         else if ((this.length > this.maxHeaderSize) || (this.headerSize > this.maxHeaderSize - this.length))
/* 168:    */         {
/* 169:184 */           this.headerSize = (this.maxHeaderSize + 1);
/* 170:185 */           this.name = null;
/* 171:186 */           this.state = State.SKIP_VALUE;
/* 172:187 */           frame.setTruncated();
/* 173:    */         }
/* 174:    */         else
/* 175:    */         {
/* 176:189 */           this.headerSize += this.length;
/* 177:190 */           this.state = State.READ_VALUE;
/* 178:    */         }
/* 179:192 */         break;
/* 180:    */       case 6: 
/* 181:195 */         if (headerBlock.readableBytes() < this.length) {
/* 182:196 */           return;
/* 183:    */         }
/* 184:199 */         byte[] valueBytes = new byte[this.length];
/* 185:200 */         headerBlock.readBytes(valueBytes);
/* 186:    */         
/* 187:    */ 
/* 188:203 */         int index = 0;
/* 189:204 */         int offset = 0;
/* 190:207 */         if (valueBytes[0] == 0)
/* 191:    */         {
/* 192:208 */           this.state = State.ERROR;
/* 193:209 */           frame.setInvalid();
/* 194:    */         }
/* 195:    */         else
/* 196:    */         {
/* 197:213 */           while (index < this.length)
/* 198:    */           {
/* 199:214 */             while ((index < valueBytes.length) && (valueBytes[index] != 0)) {
/* 200:215 */               index++;
/* 201:    */             }
/* 202:217 */             if (index < valueBytes.length) {
/* 203:219 */               if ((index + 1 == valueBytes.length) || (valueBytes[(index + 1)] == 0))
/* 204:    */               {
/* 205:223 */                 this.state = State.ERROR;
/* 206:224 */                 frame.setInvalid();
/* 207:225 */                 break;
/* 208:    */               }
/* 209:    */             }
/* 210:228 */             String value = new String(valueBytes, offset, index - offset, "UTF-8");
/* 211:    */             try
/* 212:    */             {
/* 213:231 */               frame.headers().add(this.name, value);
/* 214:    */             }
/* 215:    */             catch (IllegalArgumentException e)
/* 216:    */             {
/* 217:234 */               this.state = State.ERROR;
/* 218:235 */               frame.setInvalid();
/* 219:236 */               break;
/* 220:    */             }
/* 221:238 */             index++;
/* 222:239 */             offset = index;
/* 223:    */           }
/* 224:242 */           this.name = null;
/* 225:245 */           if (this.state != State.ERROR) {
/* 226:249 */             if (--this.numHeaders == 0) {
/* 227:250 */               this.state = State.END_HEADER_BLOCK;
/* 228:    */             } else {
/* 229:252 */               this.state = State.READ_NAME_LENGTH;
/* 230:    */             }
/* 231:    */           }
/* 232:    */         }
/* 233:254 */         break;
/* 234:    */       case 7: 
/* 235:257 */         skipLength = Math.min(headerBlock.readableBytes(), this.length);
/* 236:258 */         headerBlock.skipBytes(skipLength);
/* 237:259 */         this.length -= skipLength;
/* 238:261 */         if (this.length == 0) {
/* 239:262 */           if (--this.numHeaders == 0) {
/* 240:263 */             this.state = State.END_HEADER_BLOCK;
/* 241:    */           } else {
/* 242:265 */             this.state = State.READ_NAME_LENGTH;
/* 243:    */           }
/* 244:    */         }
/* 245:    */         break;
/* 246:    */       case 8: 
/* 247:271 */         this.state = State.ERROR;
/* 248:272 */         frame.setInvalid();
/* 249:273 */         break;
/* 250:    */       case 9: 
/* 251:276 */         headerBlock.skipBytes(headerBlock.readableBytes());
/* 252:277 */         return;
/* 253:    */       default: 
/* 254:280 */         throw new Error("Shouldn't reach here.");
/* 255:    */       }
/* 256:    */     }
/* 257:    */   }
/* 258:    */   
/* 259:    */   void endHeaderBlock(SpdyHeadersFrame frame)
/* 260:    */     throws Exception
/* 261:    */   {
/* 262:287 */     if (this.state != State.END_HEADER_BLOCK) {
/* 263:288 */       frame.setInvalid();
/* 264:    */     }
/* 265:291 */     releaseBuffer();
/* 266:    */     
/* 267:    */ 
/* 268:294 */     this.headerSize = 0;
/* 269:295 */     this.name = null;
/* 270:296 */     this.state = State.READ_NUM_HEADERS;
/* 271:    */   }
/* 272:    */   
/* 273:    */   void end()
/* 274:    */   {
/* 275:301 */     releaseBuffer();
/* 276:    */   }
/* 277:    */   
/* 278:    */   private void releaseBuffer()
/* 279:    */   {
/* 280:305 */     if (this.cumulation != null)
/* 281:    */     {
/* 282:306 */       this.cumulation.release();
/* 283:307 */       this.cumulation = null;
/* 284:    */     }
/* 285:    */   }
/* 286:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyHeaderBlockRawDecoder
 * JD-Core Version:    0.7.0.1
 */