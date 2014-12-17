/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.handler.codec.MessageToMessageEncoder;
/*   6:    */ import io.netty.handler.codec.UnsupportedMessageTypeException;
/*   7:    */ import io.netty.handler.codec.http.FullHttpMessage;
/*   8:    */ import io.netty.handler.codec.http.FullHttpRequest;
/*   9:    */ import io.netty.handler.codec.http.HttpContent;
/*  10:    */ import io.netty.handler.codec.http.HttpHeaders;
/*  11:    */ import io.netty.handler.codec.http.HttpMessage;
/*  12:    */ import io.netty.handler.codec.http.HttpObject;
/*  13:    */ import io.netty.handler.codec.http.HttpRequest;
/*  14:    */ import io.netty.handler.codec.http.HttpResponse;
/*  15:    */ import io.netty.handler.codec.http.LastHttpContent;
/*  16:    */ import java.util.List;
/*  17:    */ import java.util.Map.Entry;
/*  18:    */ 
/*  19:    */ public class SpdyHttpEncoder
/*  20:    */   extends MessageToMessageEncoder<HttpObject>
/*  21:    */ {
/*  22:    */   private final int spdyVersion;
/*  23:    */   private int currentStreamId;
/*  24:    */   
/*  25:    */   public SpdyHttpEncoder(SpdyVersion version)
/*  26:    */   {
/*  27:134 */     if (version == null) {
/*  28:135 */       throw new NullPointerException("version");
/*  29:    */     }
/*  30:137 */     this.spdyVersion = version.getVersion();
/*  31:    */   }
/*  32:    */   
/*  33:    */   protected void encode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out)
/*  34:    */     throws Exception
/*  35:    */   {
/*  36:143 */     boolean valid = false;
/*  37:144 */     boolean last = false;
/*  38:146 */     if ((msg instanceof HttpRequest))
/*  39:    */     {
/*  40:148 */       HttpRequest httpRequest = (HttpRequest)msg;
/*  41:149 */       SpdySynStreamFrame spdySynStreamFrame = createSynStreamFrame(httpRequest);
/*  42:150 */       out.add(spdySynStreamFrame);
/*  43:    */       
/*  44:152 */       last = spdySynStreamFrame.isLast();
/*  45:153 */       valid = true;
/*  46:    */     }
/*  47:155 */     if ((msg instanceof HttpResponse))
/*  48:    */     {
/*  49:157 */       HttpResponse httpResponse = (HttpResponse)msg;
/*  50:158 */       if (httpResponse.headers().contains("X-SPDY-Associated-To-Stream-ID"))
/*  51:    */       {
/*  52:159 */         SpdySynStreamFrame spdySynStreamFrame = createSynStreamFrame(httpResponse);
/*  53:160 */         last = spdySynStreamFrame.isLast();
/*  54:161 */         out.add(spdySynStreamFrame);
/*  55:    */       }
/*  56:    */       else
/*  57:    */       {
/*  58:163 */         SpdySynReplyFrame spdySynReplyFrame = createSynReplyFrame(httpResponse);
/*  59:164 */         last = spdySynReplyFrame.isLast();
/*  60:165 */         out.add(spdySynReplyFrame);
/*  61:    */       }
/*  62:168 */       valid = true;
/*  63:    */     }
/*  64:170 */     if (((msg instanceof HttpContent)) && (!last))
/*  65:    */     {
/*  66:172 */       HttpContent chunk = (HttpContent)msg;
/*  67:    */       
/*  68:174 */       chunk.content().retain();
/*  69:175 */       SpdyDataFrame spdyDataFrame = new DefaultSpdyDataFrame(this.currentStreamId, chunk.content());
/*  70:176 */       spdyDataFrame.setLast(chunk instanceof LastHttpContent);
/*  71:177 */       if ((chunk instanceof LastHttpContent))
/*  72:    */       {
/*  73:178 */         LastHttpContent trailer = (LastHttpContent)chunk;
/*  74:179 */         HttpHeaders trailers = trailer.trailingHeaders();
/*  75:180 */         if (trailers.isEmpty())
/*  76:    */         {
/*  77:181 */           out.add(spdyDataFrame);
/*  78:    */         }
/*  79:    */         else
/*  80:    */         {
/*  81:184 */           SpdyHeadersFrame spdyHeadersFrame = new DefaultSpdyHeadersFrame(this.currentStreamId);
/*  82:185 */           for (Map.Entry<String, String> entry : trailers) {
/*  83:186 */             spdyHeadersFrame.headers().add((String)entry.getKey(), entry.getValue());
/*  84:    */           }
/*  85:190 */           out.add(spdyHeadersFrame);
/*  86:191 */           out.add(spdyDataFrame);
/*  87:    */         }
/*  88:    */       }
/*  89:    */       else
/*  90:    */       {
/*  91:194 */         out.add(spdyDataFrame);
/*  92:    */       }
/*  93:197 */       valid = true;
/*  94:    */     }
/*  95:200 */     if (!valid) {
/*  96:201 */       throw new UnsupportedMessageTypeException(msg, new Class[0]);
/*  97:    */     }
/*  98:    */   }
/*  99:    */   
/* 100:    */   private SpdySynStreamFrame createSynStreamFrame(HttpMessage httpMessage)
/* 101:    */     throws Exception
/* 102:    */   {
/* 103:208 */     int streamID = SpdyHttpHeaders.getStreamId(httpMessage);
/* 104:209 */     int associatedToStreamId = SpdyHttpHeaders.getAssociatedToStreamId(httpMessage);
/* 105:210 */     byte priority = SpdyHttpHeaders.getPriority(httpMessage);
/* 106:211 */     String URL = SpdyHttpHeaders.getUrl(httpMessage);
/* 107:212 */     String scheme = SpdyHttpHeaders.getScheme(httpMessage);
/* 108:213 */     SpdyHttpHeaders.removeStreamId(httpMessage);
/* 109:214 */     SpdyHttpHeaders.removeAssociatedToStreamId(httpMessage);
/* 110:215 */     SpdyHttpHeaders.removePriority(httpMessage);
/* 111:216 */     SpdyHttpHeaders.removeUrl(httpMessage);
/* 112:217 */     SpdyHttpHeaders.removeScheme(httpMessage);
/* 113:    */     
/* 114:    */ 
/* 115:    */ 
/* 116:221 */     httpMessage.headers().remove("Connection");
/* 117:222 */     httpMessage.headers().remove("Keep-Alive");
/* 118:223 */     httpMessage.headers().remove("Proxy-Connection");
/* 119:224 */     httpMessage.headers().remove("Transfer-Encoding");
/* 120:    */     
/* 121:226 */     SpdySynStreamFrame spdySynStreamFrame = new DefaultSpdySynStreamFrame(streamID, associatedToStreamId, priority);
/* 122:230 */     if ((httpMessage instanceof FullHttpRequest))
/* 123:    */     {
/* 124:231 */       HttpRequest httpRequest = (HttpRequest)httpMessage;
/* 125:232 */       SpdyHeaders.setMethod(this.spdyVersion, spdySynStreamFrame, httpRequest.getMethod());
/* 126:233 */       SpdyHeaders.setUrl(this.spdyVersion, spdySynStreamFrame, httpRequest.getUri());
/* 127:234 */       SpdyHeaders.setVersion(this.spdyVersion, spdySynStreamFrame, httpMessage.getProtocolVersion());
/* 128:    */     }
/* 129:236 */     if ((httpMessage instanceof HttpResponse))
/* 130:    */     {
/* 131:237 */       HttpResponse httpResponse = (HttpResponse)httpMessage;
/* 132:238 */       SpdyHeaders.setStatus(this.spdyVersion, spdySynStreamFrame, httpResponse.getStatus());
/* 133:239 */       SpdyHeaders.setUrl(this.spdyVersion, spdySynStreamFrame, URL);
/* 134:240 */       SpdyHeaders.setVersion(this.spdyVersion, spdySynStreamFrame, httpMessage.getProtocolVersion());
/* 135:241 */       spdySynStreamFrame.setUnidirectional(true);
/* 136:    */     }
/* 137:245 */     if (this.spdyVersion >= 3)
/* 138:    */     {
/* 139:246 */       String host = HttpHeaders.getHost(httpMessage);
/* 140:247 */       httpMessage.headers().remove("Host");
/* 141:248 */       SpdyHeaders.setHost(spdySynStreamFrame, host);
/* 142:    */     }
/* 143:252 */     if (scheme == null) {
/* 144:253 */       scheme = "https";
/* 145:    */     }
/* 146:255 */     SpdyHeaders.setScheme(this.spdyVersion, spdySynStreamFrame, scheme);
/* 147:258 */     for (Map.Entry<String, String> entry : httpMessage.headers()) {
/* 148:259 */       spdySynStreamFrame.headers().add((String)entry.getKey(), entry.getValue());
/* 149:    */     }
/* 150:261 */     this.currentStreamId = spdySynStreamFrame.streamId();
/* 151:262 */     spdySynStreamFrame.setLast(isLast(httpMessage));
/* 152:    */     
/* 153:264 */     return spdySynStreamFrame;
/* 154:    */   }
/* 155:    */   
/* 156:    */   private SpdySynReplyFrame createSynReplyFrame(HttpResponse httpResponse)
/* 157:    */     throws Exception
/* 158:    */   {
/* 159:270 */     int streamID = SpdyHttpHeaders.getStreamId(httpResponse);
/* 160:271 */     SpdyHttpHeaders.removeStreamId(httpResponse);
/* 161:    */     
/* 162:    */ 
/* 163:    */ 
/* 164:275 */     httpResponse.headers().remove("Connection");
/* 165:276 */     httpResponse.headers().remove("Keep-Alive");
/* 166:277 */     httpResponse.headers().remove("Proxy-Connection");
/* 167:278 */     httpResponse.headers().remove("Transfer-Encoding");
/* 168:    */     
/* 169:280 */     SpdySynReplyFrame spdySynReplyFrame = new DefaultSpdySynReplyFrame(streamID);
/* 170:    */     
/* 171:    */ 
/* 172:283 */     SpdyHeaders.setStatus(this.spdyVersion, spdySynReplyFrame, httpResponse.getStatus());
/* 173:284 */     SpdyHeaders.setVersion(this.spdyVersion, spdySynReplyFrame, httpResponse.getProtocolVersion());
/* 174:287 */     for (Map.Entry<String, String> entry : httpResponse.headers()) {
/* 175:288 */       spdySynReplyFrame.headers().add((String)entry.getKey(), entry.getValue());
/* 176:    */     }
/* 177:291 */     this.currentStreamId = streamID;
/* 178:292 */     spdySynReplyFrame.setLast(isLast(httpResponse));
/* 179:    */     
/* 180:294 */     return spdySynReplyFrame;
/* 181:    */   }
/* 182:    */   
/* 183:    */   private static boolean isLast(HttpMessage httpMessage)
/* 184:    */   {
/* 185:304 */     if ((httpMessage instanceof FullHttpMessage))
/* 186:    */     {
/* 187:305 */       FullHttpMessage fullMessage = (FullHttpMessage)httpMessage;
/* 188:306 */       if ((fullMessage.trailingHeaders().isEmpty()) && (!fullMessage.content().isReadable())) {
/* 189:307 */         return true;
/* 190:    */       }
/* 191:    */     }
/* 192:311 */     return false;
/* 193:    */   }
/* 194:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyHttpEncoder
 * JD-Core Version:    0.7.0.1
 */