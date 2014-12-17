/*  1:   */ package io.netty.handler.codec.rtsp;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.handler.codec.http.FullHttpResponse;
/*  5:   */ import io.netty.handler.codec.http.HttpResponse;
/*  6:   */ import io.netty.handler.codec.http.HttpResponseStatus;
/*  7:   */ import io.netty.handler.codec.http.HttpVersion;
/*  8:   */ import io.netty.util.CharsetUtil;
/*  9:   */ 
/* 10:   */ public class RtspResponseEncoder
/* 11:   */   extends RtspObjectEncoder<HttpResponse>
/* 12:   */ {
/* 13:31 */   private static final byte[] CRLF = { 13, 10 };
/* 14:   */   
/* 15:   */   public boolean acceptOutboundMessage(Object msg)
/* 16:   */     throws Exception
/* 17:   */   {
/* 18:35 */     return msg instanceof FullHttpResponse;
/* 19:   */   }
/* 20:   */   
/* 21:   */   protected void encodeInitialLine(ByteBuf buf, HttpResponse response)
/* 22:   */     throws Exception
/* 23:   */   {
/* 24:41 */     encodeAscii(response.getProtocolVersion().toString(), buf);
/* 25:42 */     buf.writeByte(32);
/* 26:43 */     buf.writeBytes(String.valueOf(response.getStatus().code()).getBytes(CharsetUtil.US_ASCII));
/* 27:44 */     buf.writeByte(32);
/* 28:45 */     encodeAscii(String.valueOf(response.getStatus().reasonPhrase()), buf);
/* 29:46 */     buf.writeBytes(CRLF);
/* 30:   */   }
/* 31:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.rtsp.RtspResponseEncoder
 * JD-Core Version:    0.7.0.1
 */