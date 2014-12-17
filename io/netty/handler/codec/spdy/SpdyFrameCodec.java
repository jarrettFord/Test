/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.ChannelFuture;
/*   6:    */ import io.netty.channel.ChannelFutureListener;
/*   7:    */ import io.netty.channel.ChannelHandlerContext;
/*   8:    */ import io.netty.channel.ChannelOutboundHandler;
/*   9:    */ import io.netty.channel.ChannelPromise;
/*  10:    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*  11:    */ import io.netty.handler.codec.UnsupportedMessageTypeException;
/*  12:    */ import java.net.SocketAddress;
/*  13:    */ import java.util.List;
/*  14:    */ 
/*  15:    */ public class SpdyFrameCodec
/*  16:    */   extends ByteToMessageDecoder
/*  17:    */   implements SpdyFrameDecoderDelegate, ChannelOutboundHandler
/*  18:    */ {
/*  19: 37 */   private static final SpdyProtocolException INVALID_FRAME = new SpdyProtocolException("Received invalid frame");
/*  20:    */   private final SpdyFrameDecoder spdyFrameDecoder;
/*  21:    */   private final SpdyFrameEncoder spdyFrameEncoder;
/*  22:    */   private final SpdyHeaderBlockDecoder spdyHeaderBlockDecoder;
/*  23:    */   private final SpdyHeaderBlockEncoder spdyHeaderBlockEncoder;
/*  24:    */   private SpdyHeadersFrame spdyHeadersFrame;
/*  25:    */   private SpdySettingsFrame spdySettingsFrame;
/*  26:    */   private ChannelHandlerContext ctx;
/*  27:    */   
/*  28:    */   public SpdyFrameCodec(SpdyVersion version)
/*  29:    */   {
/*  30: 58 */     this(version, 8192, 16384, 6, 15, 8);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public SpdyFrameCodec(SpdyVersion version, int maxChunkSize, int maxHeaderSize, int compressionLevel, int windowBits, int memLevel)
/*  34:    */   {
/*  35: 67 */     this(version, maxChunkSize, SpdyHeaderBlockDecoder.newInstance(version, maxHeaderSize), SpdyHeaderBlockEncoder.newInstance(version, compressionLevel, windowBits, memLevel));
/*  36:    */   }
/*  37:    */   
/*  38:    */   protected SpdyFrameCodec(SpdyVersion version, int maxChunkSize, SpdyHeaderBlockDecoder spdyHeaderBlockDecoder, SpdyHeaderBlockEncoder spdyHeaderBlockEncoder)
/*  39:    */   {
/*  40: 74 */     this.spdyFrameDecoder = new SpdyFrameDecoder(version, this, maxChunkSize);
/*  41: 75 */     this.spdyFrameEncoder = new SpdyFrameEncoder(version);
/*  42: 76 */     this.spdyHeaderBlockDecoder = spdyHeaderBlockDecoder;
/*  43: 77 */     this.spdyHeaderBlockEncoder = spdyHeaderBlockEncoder;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  47:    */     throws Exception
/*  48:    */   {
/*  49: 82 */     super.handlerAdded(ctx);
/*  50: 83 */     this.ctx = ctx;
/*  51: 84 */     ctx.channel().closeFuture().addListener(new ChannelFutureListener()
/*  52:    */     {
/*  53:    */       public void operationComplete(ChannelFuture future)
/*  54:    */         throws Exception
/*  55:    */       {
/*  56: 87 */         SpdyFrameCodec.this.spdyHeaderBlockDecoder.end();
/*  57: 88 */         SpdyFrameCodec.this.spdyHeaderBlockEncoder.end();
/*  58:    */       }
/*  59:    */     });
/*  60:    */   }
/*  61:    */   
/*  62:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  63:    */     throws Exception
/*  64:    */   {
/*  65: 95 */     this.spdyFrameDecoder.decode(in);
/*  66:    */   }
/*  67:    */   
/*  68:    */   public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
/*  69:    */     throws Exception
/*  70:    */   {
/*  71:100 */     ctx.bind(localAddress, promise);
/*  72:    */   }
/*  73:    */   
/*  74:    */   public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/*  75:    */     throws Exception
/*  76:    */   {
/*  77:106 */     ctx.connect(remoteAddress, localAddress, promise);
/*  78:    */   }
/*  79:    */   
/*  80:    */   public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/*  81:    */     throws Exception
/*  82:    */   {
/*  83:111 */     ctx.disconnect(promise);
/*  84:    */   }
/*  85:    */   
/*  86:    */   public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/*  87:    */     throws Exception
/*  88:    */   {
/*  89:116 */     ctx.close(promise);
/*  90:    */   }
/*  91:    */   
/*  92:    */   public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
/*  93:    */     throws Exception
/*  94:    */   {
/*  95:121 */     ctx.deregister(promise);
/*  96:    */   }
/*  97:    */   
/*  98:    */   public void read(ChannelHandlerContext ctx)
/*  99:    */     throws Exception
/* 100:    */   {
/* 101:126 */     ctx.read();
/* 102:    */   }
/* 103:    */   
/* 104:    */   public void flush(ChannelHandlerContext ctx)
/* 105:    */     throws Exception
/* 106:    */   {
/* 107:131 */     ctx.flush();
/* 108:    */   }
/* 109:    */   
/* 110:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 111:    */     throws Exception
/* 112:    */   {
/* 113:138 */     if ((msg instanceof SpdyDataFrame))
/* 114:    */     {
/* 115:140 */       SpdyDataFrame spdyDataFrame = (SpdyDataFrame)msg;
/* 116:141 */       ByteBuf frame = this.spdyFrameEncoder.encodeDataFrame(ctx.alloc(), spdyDataFrame.streamId(), spdyDataFrame.isLast(), spdyDataFrame.content());
/* 117:    */       
/* 118:    */ 
/* 119:    */ 
/* 120:    */ 
/* 121:    */ 
/* 122:147 */       spdyDataFrame.release();
/* 123:148 */       ctx.write(frame, promise);
/* 124:    */     }
/* 125:150 */     else if ((msg instanceof SpdySynStreamFrame))
/* 126:    */     {
/* 127:152 */       SpdySynStreamFrame spdySynStreamFrame = (SpdySynStreamFrame)msg;
/* 128:153 */       ByteBuf headerBlock = this.spdyHeaderBlockEncoder.encode(spdySynStreamFrame);
/* 129:    */       ByteBuf frame;
/* 130:    */       try
/* 131:    */       {
/* 132:155 */         frame = this.spdyFrameEncoder.encodeSynStreamFrame(ctx.alloc(), spdySynStreamFrame.streamId(), spdySynStreamFrame.associatedStreamId(), spdySynStreamFrame.priority(), spdySynStreamFrame.isLast(), spdySynStreamFrame.isUnidirectional(), headerBlock);
/* 133:    */       }
/* 134:    */       finally
/* 135:    */       {
/* 136:165 */         headerBlock.release();
/* 137:    */       }
/* 138:167 */       ctx.write(frame, promise);
/* 139:    */     }
/* 140:169 */     else if ((msg instanceof SpdySynReplyFrame))
/* 141:    */     {
/* 142:171 */       SpdySynReplyFrame spdySynReplyFrame = (SpdySynReplyFrame)msg;
/* 143:172 */       ByteBuf headerBlock = this.spdyHeaderBlockEncoder.encode(spdySynReplyFrame);
/* 144:    */       ByteBuf frame;
/* 145:    */       try
/* 146:    */       {
/* 147:174 */         frame = this.spdyFrameEncoder.encodeSynReplyFrame(ctx.alloc(), spdySynReplyFrame.streamId(), spdySynReplyFrame.isLast(), headerBlock);
/* 148:    */       }
/* 149:    */       finally
/* 150:    */       {
/* 151:181 */         headerBlock.release();
/* 152:    */       }
/* 153:183 */       ctx.write(frame, promise);
/* 154:    */     }
/* 155:185 */     else if ((msg instanceof SpdyRstStreamFrame))
/* 156:    */     {
/* 157:187 */       SpdyRstStreamFrame spdyRstStreamFrame = (SpdyRstStreamFrame)msg;
/* 158:188 */       ByteBuf frame = this.spdyFrameEncoder.encodeRstStreamFrame(ctx.alloc(), spdyRstStreamFrame.streamId(), spdyRstStreamFrame.status().code());
/* 159:    */       
/* 160:    */ 
/* 161:    */ 
/* 162:    */ 
/* 163:193 */       ctx.write(frame, promise);
/* 164:    */     }
/* 165:195 */     else if ((msg instanceof SpdySettingsFrame))
/* 166:    */     {
/* 167:197 */       SpdySettingsFrame spdySettingsFrame = (SpdySettingsFrame)msg;
/* 168:198 */       ByteBuf frame = this.spdyFrameEncoder.encodeSettingsFrame(ctx.alloc(), spdySettingsFrame);
/* 169:    */       
/* 170:    */ 
/* 171:    */ 
/* 172:202 */       ctx.write(frame, promise);
/* 173:    */     }
/* 174:204 */     else if ((msg instanceof SpdyPingFrame))
/* 175:    */     {
/* 176:206 */       SpdyPingFrame spdyPingFrame = (SpdyPingFrame)msg;
/* 177:207 */       ByteBuf frame = this.spdyFrameEncoder.encodePingFrame(ctx.alloc(), spdyPingFrame.id());
/* 178:    */       
/* 179:    */ 
/* 180:    */ 
/* 181:211 */       ctx.write(frame, promise);
/* 182:    */     }
/* 183:213 */     else if ((msg instanceof SpdyGoAwayFrame))
/* 184:    */     {
/* 185:215 */       SpdyGoAwayFrame spdyGoAwayFrame = (SpdyGoAwayFrame)msg;
/* 186:216 */       ByteBuf frame = this.spdyFrameEncoder.encodeGoAwayFrame(ctx.alloc(), spdyGoAwayFrame.lastGoodStreamId(), spdyGoAwayFrame.status().code());
/* 187:    */       
/* 188:    */ 
/* 189:    */ 
/* 190:    */ 
/* 191:221 */       ctx.write(frame, promise);
/* 192:    */     }
/* 193:223 */     else if ((msg instanceof SpdyHeadersFrame))
/* 194:    */     {
/* 195:225 */       SpdyHeadersFrame spdyHeadersFrame = (SpdyHeadersFrame)msg;
/* 196:226 */       ByteBuf headerBlock = this.spdyHeaderBlockEncoder.encode(spdyHeadersFrame);
/* 197:    */       ByteBuf frame;
/* 198:    */       try
/* 199:    */       {
/* 200:228 */         frame = this.spdyFrameEncoder.encodeHeadersFrame(ctx.alloc(), spdyHeadersFrame.streamId(), spdyHeadersFrame.isLast(), headerBlock);
/* 201:    */       }
/* 202:    */       finally
/* 203:    */       {
/* 204:235 */         headerBlock.release();
/* 205:    */       }
/* 206:237 */       ctx.write(frame, promise);
/* 207:    */     }
/* 208:239 */     else if ((msg instanceof SpdyWindowUpdateFrame))
/* 209:    */     {
/* 210:241 */       SpdyWindowUpdateFrame spdyWindowUpdateFrame = (SpdyWindowUpdateFrame)msg;
/* 211:242 */       ByteBuf frame = this.spdyFrameEncoder.encodeWindowUpdateFrame(ctx.alloc(), spdyWindowUpdateFrame.streamId(), spdyWindowUpdateFrame.deltaWindowSize());
/* 212:    */       
/* 213:    */ 
/* 214:    */ 
/* 215:    */ 
/* 216:247 */       ctx.write(frame, promise);
/* 217:    */     }
/* 218:    */     else
/* 219:    */     {
/* 220:249 */       throw new UnsupportedMessageTypeException(msg, new Class[0]);
/* 221:    */     }
/* 222:    */     ByteBuf frame;
/* 223:    */   }
/* 224:    */   
/* 225:    */   public void readDataFrame(int streamId, boolean last, ByteBuf data)
/* 226:    */   {
/* 227:255 */     SpdyDataFrame spdyDataFrame = new DefaultSpdyDataFrame(streamId, data);
/* 228:256 */     spdyDataFrame.setLast(last);
/* 229:257 */     this.ctx.fireChannelRead(spdyDataFrame);
/* 230:    */   }
/* 231:    */   
/* 232:    */   public void readSynStreamFrame(int streamId, int associatedToStreamId, byte priority, boolean last, boolean unidirectional)
/* 233:    */   {
/* 234:263 */     SpdySynStreamFrame spdySynStreamFrame = new DefaultSpdySynStreamFrame(streamId, associatedToStreamId, priority);
/* 235:264 */     spdySynStreamFrame.setLast(last);
/* 236:265 */     spdySynStreamFrame.setUnidirectional(unidirectional);
/* 237:266 */     this.spdyHeadersFrame = spdySynStreamFrame;
/* 238:    */   }
/* 239:    */   
/* 240:    */   public void readSynReplyFrame(int streamId, boolean last)
/* 241:    */   {
/* 242:271 */     SpdySynReplyFrame spdySynReplyFrame = new DefaultSpdySynReplyFrame(streamId);
/* 243:272 */     spdySynReplyFrame.setLast(last);
/* 244:273 */     this.spdyHeadersFrame = spdySynReplyFrame;
/* 245:    */   }
/* 246:    */   
/* 247:    */   public void readRstStreamFrame(int streamId, int statusCode)
/* 248:    */   {
/* 249:278 */     SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, statusCode);
/* 250:279 */     this.ctx.fireChannelRead(spdyRstStreamFrame);
/* 251:    */   }
/* 252:    */   
/* 253:    */   public void readSettingsFrame(boolean clearPersisted)
/* 254:    */   {
/* 255:284 */     this.spdySettingsFrame = new DefaultSpdySettingsFrame();
/* 256:285 */     this.spdySettingsFrame.setClearPreviouslyPersistedSettings(clearPersisted);
/* 257:    */   }
/* 258:    */   
/* 259:    */   public void readSetting(int id, int value, boolean persistValue, boolean persisted)
/* 260:    */   {
/* 261:290 */     this.spdySettingsFrame.setValue(id, value, persistValue, persisted);
/* 262:    */   }
/* 263:    */   
/* 264:    */   public void readSettingsEnd()
/* 265:    */   {
/* 266:295 */     Object frame = this.spdySettingsFrame;
/* 267:296 */     this.spdySettingsFrame = null;
/* 268:297 */     this.ctx.fireChannelRead(frame);
/* 269:    */   }
/* 270:    */   
/* 271:    */   public void readPingFrame(int id)
/* 272:    */   {
/* 273:302 */     SpdyPingFrame spdyPingFrame = new DefaultSpdyPingFrame(id);
/* 274:303 */     this.ctx.fireChannelRead(spdyPingFrame);
/* 275:    */   }
/* 276:    */   
/* 277:    */   public void readGoAwayFrame(int lastGoodStreamId, int statusCode)
/* 278:    */   {
/* 279:308 */     SpdyGoAwayFrame spdyGoAwayFrame = new DefaultSpdyGoAwayFrame(lastGoodStreamId, statusCode);
/* 280:309 */     this.ctx.fireChannelRead(spdyGoAwayFrame);
/* 281:    */   }
/* 282:    */   
/* 283:    */   public void readHeadersFrame(int streamId, boolean last)
/* 284:    */   {
/* 285:314 */     this.spdyHeadersFrame = new DefaultSpdyHeadersFrame(streamId);
/* 286:315 */     this.spdyHeadersFrame.setLast(last);
/* 287:    */   }
/* 288:    */   
/* 289:    */   public void readWindowUpdateFrame(int streamId, int deltaWindowSize)
/* 290:    */   {
/* 291:320 */     SpdyWindowUpdateFrame spdyWindowUpdateFrame = new DefaultSpdyWindowUpdateFrame(streamId, deltaWindowSize);
/* 292:321 */     this.ctx.fireChannelRead(spdyWindowUpdateFrame);
/* 293:    */   }
/* 294:    */   
/* 295:    */   public void readHeaderBlock(ByteBuf headerBlock)
/* 296:    */   {
/* 297:    */     try
/* 298:    */     {
/* 299:327 */       this.spdyHeaderBlockDecoder.decode(headerBlock, this.spdyHeadersFrame);
/* 300:    */     }
/* 301:    */     catch (Exception e)
/* 302:    */     {
/* 303:329 */       this.ctx.fireExceptionCaught(e);
/* 304:    */     }
/* 305:    */     finally
/* 306:    */     {
/* 307:331 */       headerBlock.release();
/* 308:    */     }
/* 309:    */   }
/* 310:    */   
/* 311:    */   public void readHeaderBlockEnd()
/* 312:    */   {
/* 313:337 */     Object frame = null;
/* 314:    */     try
/* 315:    */     {
/* 316:339 */       this.spdyHeaderBlockDecoder.endHeaderBlock(this.spdyHeadersFrame);
/* 317:340 */       frame = this.spdyHeadersFrame;
/* 318:341 */       this.spdyHeadersFrame = null;
/* 319:    */     }
/* 320:    */     catch (Exception e)
/* 321:    */     {
/* 322:343 */       this.ctx.fireExceptionCaught(e);
/* 323:    */     }
/* 324:345 */     if (frame != null) {
/* 325:346 */       this.ctx.fireChannelRead(frame);
/* 326:    */     }
/* 327:    */   }
/* 328:    */   
/* 329:    */   public void readFrameError(String message)
/* 330:    */   {
/* 331:352 */     this.ctx.fireExceptionCaught(INVALID_FRAME);
/* 332:    */   }
/* 333:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyFrameCodec
 * JD-Core Version:    0.7.0.1
 */