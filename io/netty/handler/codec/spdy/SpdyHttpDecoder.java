/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.handler.codec.MessageToMessageDecoder;
/*   6:    */ import io.netty.handler.codec.TooLongFrameException;
/*   7:    */ import io.netty.handler.codec.http.DefaultFullHttpRequest;
/*   8:    */ import io.netty.handler.codec.http.DefaultFullHttpResponse;
/*   9:    */ import io.netty.handler.codec.http.FullHttpMessage;
/*  10:    */ import io.netty.handler.codec.http.FullHttpRequest;
/*  11:    */ import io.netty.handler.codec.http.FullHttpResponse;
/*  12:    */ import io.netty.handler.codec.http.HttpHeaders;
/*  13:    */ import io.netty.handler.codec.http.HttpMethod;
/*  14:    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*  15:    */ import io.netty.handler.codec.http.HttpVersion;
/*  16:    */ import java.util.HashMap;
/*  17:    */ import java.util.List;
/*  18:    */ import java.util.Map;
/*  19:    */ import java.util.Map.Entry;
/*  20:    */ 
/*  21:    */ public class SpdyHttpDecoder
/*  22:    */   extends MessageToMessageDecoder<SpdyFrame>
/*  23:    */ {
/*  24:    */   private final int spdyVersion;
/*  25:    */   private final int maxContentLength;
/*  26:    */   private final Map<Integer, FullHttpMessage> messageMap;
/*  27:    */   
/*  28:    */   public SpdyHttpDecoder(SpdyVersion version, int maxContentLength)
/*  29:    */   {
/*  30: 55 */     this(version, maxContentLength, new HashMap());
/*  31:    */   }
/*  32:    */   
/*  33:    */   protected SpdyHttpDecoder(SpdyVersion version, int maxContentLength, Map<Integer, FullHttpMessage> messageMap)
/*  34:    */   {
/*  35: 68 */     if (version == null) {
/*  36: 69 */       throw new NullPointerException("version");
/*  37:    */     }
/*  38: 71 */     if (maxContentLength <= 0) {
/*  39: 72 */       throw new IllegalArgumentException("maxContentLength must be a positive integer: " + maxContentLength);
/*  40:    */     }
/*  41: 75 */     this.spdyVersion = version.getVersion();
/*  42: 76 */     this.maxContentLength = maxContentLength;
/*  43: 77 */     this.messageMap = messageMap;
/*  44:    */   }
/*  45:    */   
/*  46:    */   protected FullHttpMessage putMessage(int streamId, FullHttpMessage message)
/*  47:    */   {
/*  48: 81 */     return (FullHttpMessage)this.messageMap.put(Integer.valueOf(streamId), message);
/*  49:    */   }
/*  50:    */   
/*  51:    */   protected FullHttpMessage getMessage(int streamId)
/*  52:    */   {
/*  53: 85 */     return (FullHttpMessage)this.messageMap.get(Integer.valueOf(streamId));
/*  54:    */   }
/*  55:    */   
/*  56:    */   protected FullHttpMessage removeMessage(int streamId)
/*  57:    */   {
/*  58: 89 */     return (FullHttpMessage)this.messageMap.remove(Integer.valueOf(streamId));
/*  59:    */   }
/*  60:    */   
/*  61:    */   protected void decode(ChannelHandlerContext ctx, SpdyFrame msg, List<Object> out)
/*  62:    */     throws Exception
/*  63:    */   {
/*  64: 95 */     if ((msg instanceof SpdySynStreamFrame))
/*  65:    */     {
/*  66: 98 */       SpdySynStreamFrame spdySynStreamFrame = (SpdySynStreamFrame)msg;
/*  67: 99 */       int streamId = spdySynStreamFrame.streamId();
/*  68:101 */       if (SpdyCodecUtil.isServerId(streamId))
/*  69:    */       {
/*  70:103 */         int associatedToStreamId = spdySynStreamFrame.associatedStreamId();
/*  71:107 */         if (associatedToStreamId == 0)
/*  72:    */         {
/*  73:108 */           SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.INVALID_STREAM);
/*  74:    */           
/*  75:110 */           ctx.writeAndFlush(spdyRstStreamFrame);
/*  76:111 */           return;
/*  77:    */         }
/*  78:114 */         String URL = SpdyHeaders.getUrl(this.spdyVersion, spdySynStreamFrame);
/*  79:118 */         if (URL == null)
/*  80:    */         {
/*  81:119 */           SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.PROTOCOL_ERROR);
/*  82:    */           
/*  83:121 */           ctx.writeAndFlush(spdyRstStreamFrame);
/*  84:122 */           return;
/*  85:    */         }
/*  86:127 */         if (spdySynStreamFrame.isTruncated())
/*  87:    */         {
/*  88:128 */           SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.INTERNAL_ERROR);
/*  89:    */           
/*  90:130 */           ctx.writeAndFlush(spdyRstStreamFrame);
/*  91:131 */           return;
/*  92:    */         }
/*  93:    */         try
/*  94:    */         {
/*  95:135 */           FullHttpResponse httpResponseWithEntity = createHttpResponse(this.spdyVersion, spdySynStreamFrame);
/*  96:    */           
/*  97:    */ 
/*  98:    */ 
/*  99:139 */           SpdyHttpHeaders.setStreamId(httpResponseWithEntity, streamId);
/* 100:140 */           SpdyHttpHeaders.setAssociatedToStreamId(httpResponseWithEntity, associatedToStreamId);
/* 101:141 */           SpdyHttpHeaders.setPriority(httpResponseWithEntity, spdySynStreamFrame.priority());
/* 102:142 */           SpdyHttpHeaders.setUrl(httpResponseWithEntity, URL);
/* 103:144 */           if (spdySynStreamFrame.isLast())
/* 104:    */           {
/* 105:145 */             HttpHeaders.setContentLength(httpResponseWithEntity, 0L);
/* 106:146 */             out.add(httpResponseWithEntity);
/* 107:    */           }
/* 108:    */           else
/* 109:    */           {
/* 110:149 */             putMessage(streamId, httpResponseWithEntity);
/* 111:    */           }
/* 112:    */         }
/* 113:    */         catch (Exception e)
/* 114:    */         {
/* 115:152 */           SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.PROTOCOL_ERROR);
/* 116:    */           
/* 117:154 */           ctx.writeAndFlush(spdyRstStreamFrame);
/* 118:    */         }
/* 119:    */       }
/* 120:    */       else
/* 121:    */       {
/* 122:161 */         if (spdySynStreamFrame.isTruncated())
/* 123:    */         {
/* 124:162 */           SpdySynReplyFrame spdySynReplyFrame = new DefaultSpdySynReplyFrame(streamId);
/* 125:163 */           spdySynReplyFrame.setLast(true);
/* 126:164 */           SpdyHeaders.setStatus(this.spdyVersion, spdySynReplyFrame, HttpResponseStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
/* 127:    */           
/* 128:    */ 
/* 129:167 */           SpdyHeaders.setVersion(this.spdyVersion, spdySynReplyFrame, HttpVersion.HTTP_1_0);
/* 130:168 */           ctx.writeAndFlush(spdySynReplyFrame);
/* 131:169 */           return;
/* 132:    */         }
/* 133:    */         try
/* 134:    */         {
/* 135:173 */           FullHttpRequest httpRequestWithEntity = createHttpRequest(this.spdyVersion, spdySynStreamFrame);
/* 136:    */           
/* 137:    */ 
/* 138:176 */           SpdyHttpHeaders.setStreamId(httpRequestWithEntity, streamId);
/* 139:178 */           if (spdySynStreamFrame.isLast()) {
/* 140:179 */             out.add(httpRequestWithEntity);
/* 141:    */           } else {
/* 142:182 */             putMessage(streamId, httpRequestWithEntity);
/* 143:    */           }
/* 144:    */         }
/* 145:    */         catch (Exception e)
/* 146:    */         {
/* 147:188 */           SpdySynReplyFrame spdySynReplyFrame = new DefaultSpdySynReplyFrame(streamId);
/* 148:189 */           spdySynReplyFrame.setLast(true);
/* 149:190 */           SpdyHeaders.setStatus(this.spdyVersion, spdySynReplyFrame, HttpResponseStatus.BAD_REQUEST);
/* 150:191 */           SpdyHeaders.setVersion(this.spdyVersion, spdySynReplyFrame, HttpVersion.HTTP_1_0);
/* 151:192 */           ctx.writeAndFlush(spdySynReplyFrame);
/* 152:    */         }
/* 153:    */       }
/* 154:    */     }
/* 155:196 */     else if ((msg instanceof SpdySynReplyFrame))
/* 156:    */     {
/* 157:198 */       SpdySynReplyFrame spdySynReplyFrame = (SpdySynReplyFrame)msg;
/* 158:199 */       int streamId = spdySynReplyFrame.streamId();
/* 159:203 */       if (spdySynReplyFrame.isTruncated())
/* 160:    */       {
/* 161:204 */         SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.INTERNAL_ERROR);
/* 162:    */         
/* 163:206 */         ctx.writeAndFlush(spdyRstStreamFrame);
/* 164:207 */         return;
/* 165:    */       }
/* 166:    */       try
/* 167:    */       {
/* 168:211 */         FullHttpResponse httpResponseWithEntity = createHttpResponse(this.spdyVersion, spdySynReplyFrame);
/* 169:    */         
/* 170:    */ 
/* 171:214 */         SpdyHttpHeaders.setStreamId(httpResponseWithEntity, streamId);
/* 172:216 */         if (spdySynReplyFrame.isLast())
/* 173:    */         {
/* 174:217 */           HttpHeaders.setContentLength(httpResponseWithEntity, 0L);
/* 175:218 */           out.add(httpResponseWithEntity);
/* 176:    */         }
/* 177:    */         else
/* 178:    */         {
/* 179:221 */           putMessage(streamId, httpResponseWithEntity);
/* 180:    */         }
/* 181:    */       }
/* 182:    */       catch (Exception e)
/* 183:    */       {
/* 184:226 */         SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.PROTOCOL_ERROR);
/* 185:    */         
/* 186:228 */         ctx.writeAndFlush(spdyRstStreamFrame);
/* 187:    */       }
/* 188:    */     }
/* 189:231 */     else if ((msg instanceof SpdyHeadersFrame))
/* 190:    */     {
/* 191:233 */       SpdyHeadersFrame spdyHeadersFrame = (SpdyHeadersFrame)msg;
/* 192:234 */       int streamId = spdyHeadersFrame.streamId();
/* 193:235 */       FullHttpMessage fullHttpMessage = getMessage(streamId);
/* 194:238 */       if (fullHttpMessage == null) {
/* 195:239 */         return;
/* 196:    */       }
/* 197:243 */       if (!spdyHeadersFrame.isTruncated()) {
/* 198:244 */         for (Map.Entry<String, String> e : spdyHeadersFrame.headers()) {
/* 199:245 */           fullHttpMessage.headers().add((String)e.getKey(), e.getValue());
/* 200:    */         }
/* 201:    */       }
/* 202:249 */       if (spdyHeadersFrame.isLast())
/* 203:    */       {
/* 204:250 */         HttpHeaders.setContentLength(fullHttpMessage, fullHttpMessage.content().readableBytes());
/* 205:251 */         removeMessage(streamId);
/* 206:252 */         out.add(fullHttpMessage);
/* 207:    */       }
/* 208:    */     }
/* 209:255 */     else if ((msg instanceof SpdyDataFrame))
/* 210:    */     {
/* 211:257 */       SpdyDataFrame spdyDataFrame = (SpdyDataFrame)msg;
/* 212:258 */       int streamId = spdyDataFrame.streamId();
/* 213:259 */       FullHttpMessage fullHttpMessage = getMessage(streamId);
/* 214:262 */       if (fullHttpMessage == null) {
/* 215:263 */         return;
/* 216:    */       }
/* 217:266 */       ByteBuf content = fullHttpMessage.content();
/* 218:267 */       if (content.readableBytes() > this.maxContentLength - spdyDataFrame.content().readableBytes())
/* 219:    */       {
/* 220:268 */         removeMessage(streamId);
/* 221:269 */         throw new TooLongFrameException("HTTP content length exceeded " + this.maxContentLength + " bytes.");
/* 222:    */       }
/* 223:273 */       ByteBuf spdyDataFrameData = spdyDataFrame.content();
/* 224:274 */       int spdyDataFrameDataLen = spdyDataFrameData.readableBytes();
/* 225:275 */       content.writeBytes(spdyDataFrameData, spdyDataFrameData.readerIndex(), spdyDataFrameDataLen);
/* 226:277 */       if (spdyDataFrame.isLast())
/* 227:    */       {
/* 228:278 */         HttpHeaders.setContentLength(fullHttpMessage, content.readableBytes());
/* 229:279 */         removeMessage(streamId);
/* 230:280 */         out.add(fullHttpMessage);
/* 231:    */       }
/* 232:    */     }
/* 233:283 */     else if ((msg instanceof SpdyRstStreamFrame))
/* 234:    */     {
/* 235:285 */       SpdyRstStreamFrame spdyRstStreamFrame = (SpdyRstStreamFrame)msg;
/* 236:286 */       int streamId = spdyRstStreamFrame.streamId();
/* 237:287 */       removeMessage(streamId);
/* 238:    */     }
/* 239:    */   }
/* 240:    */   
/* 241:    */   private static FullHttpRequest createHttpRequest(int spdyVersion, SpdyHeadersFrame requestFrame)
/* 242:    */     throws Exception
/* 243:    */   {
/* 244:294 */     HttpMethod method = SpdyHeaders.getMethod(spdyVersion, requestFrame);
/* 245:295 */     String url = SpdyHeaders.getUrl(spdyVersion, requestFrame);
/* 246:296 */     HttpVersion httpVersion = SpdyHeaders.getVersion(spdyVersion, requestFrame);
/* 247:297 */     SpdyHeaders.removeMethod(spdyVersion, requestFrame);
/* 248:298 */     SpdyHeaders.removeUrl(spdyVersion, requestFrame);
/* 249:299 */     SpdyHeaders.removeVersion(spdyVersion, requestFrame);
/* 250:    */     
/* 251:301 */     FullHttpRequest req = new DefaultFullHttpRequest(httpVersion, method, url);
/* 252:    */     
/* 253:    */ 
/* 254:304 */     SpdyHeaders.removeScheme(spdyVersion, requestFrame);
/* 255:306 */     if (spdyVersion >= 3)
/* 256:    */     {
/* 257:308 */       String host = SpdyHeaders.getHost(requestFrame);
/* 258:309 */       SpdyHeaders.removeHost(requestFrame);
/* 259:310 */       HttpHeaders.setHost(req, host);
/* 260:    */     }
/* 261:313 */     for (Map.Entry<String, String> e : requestFrame.headers()) {
/* 262:314 */       req.headers().add((String)e.getKey(), e.getValue());
/* 263:    */     }
/* 264:318 */     HttpHeaders.setKeepAlive(req, true);
/* 265:    */     
/* 266:    */ 
/* 267:321 */     req.headers().remove("Transfer-Encoding");
/* 268:    */     
/* 269:323 */     return req;
/* 270:    */   }
/* 271:    */   
/* 272:    */   private static FullHttpResponse createHttpResponse(int spdyVersion, SpdyHeadersFrame responseFrame)
/* 273:    */     throws Exception
/* 274:    */   {
/* 275:329 */     HttpResponseStatus status = SpdyHeaders.getStatus(spdyVersion, responseFrame);
/* 276:330 */     HttpVersion version = SpdyHeaders.getVersion(spdyVersion, responseFrame);
/* 277:331 */     SpdyHeaders.removeStatus(spdyVersion, responseFrame);
/* 278:332 */     SpdyHeaders.removeVersion(spdyVersion, responseFrame);
/* 279:    */     
/* 280:334 */     FullHttpResponse res = new DefaultFullHttpResponse(version, status);
/* 281:335 */     for (Map.Entry<String, String> e : responseFrame.headers()) {
/* 282:336 */       res.headers().add((String)e.getKey(), e.getValue());
/* 283:    */     }
/* 284:340 */     HttpHeaders.setKeepAlive(res, true);
/* 285:    */     
/* 286:    */ 
/* 287:343 */     res.headers().remove("Transfer-Encoding");
/* 288:344 */     res.headers().remove("Trailer");
/* 289:    */     
/* 290:346 */     return res;
/* 291:    */   }
/* 292:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyHttpDecoder
 * JD-Core Version:    0.7.0.1
 */