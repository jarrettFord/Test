/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ 
/*  5:   */ public class HttpResponseEncoder
/*  6:   */   extends HttpObjectEncoder<HttpResponse>
/*  7:   */ {
/*  8:27 */   private static final byte[] CRLF = { 13, 10 };
/*  9:   */   
/* 10:   */   public boolean acceptOutboundMessage(Object msg)
/* 11:   */     throws Exception
/* 12:   */   {
/* 13:31 */     return (super.acceptOutboundMessage(msg)) && (!(msg instanceof HttpRequest));
/* 14:   */   }
/* 15:   */   
/* 16:   */   protected void encodeInitialLine(ByteBuf buf, HttpResponse response)
/* 17:   */     throws Exception
/* 18:   */   {
/* 19:36 */     response.getProtocolVersion().encode(buf);
/* 20:37 */     buf.writeByte(32);
/* 21:38 */     response.getStatus().encode(buf);
/* 22:39 */     buf.writeBytes(CRLF);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpResponseEncoder
 * JD-Core Version:    0.7.0.1
 */