/*  1:   */ package io.netty.handler.codec.http.websocketx;
/*  2:   */ 
/*  3:   */ import io.netty.handler.codec.http.HttpHeaders;
/*  4:   */ import java.net.URI;
/*  5:   */ 
/*  6:   */ public final class WebSocketClientHandshakerFactory
/*  7:   */ {
/*  8:   */   public static WebSocketClientHandshaker newHandshaker(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders)
/*  9:   */   {
/* 10:53 */     return newHandshaker(webSocketURL, version, subprotocol, allowExtensions, customHeaders, 65536);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public static WebSocketClientHandshaker newHandshaker(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength)
/* 14:   */   {
/* 15:77 */     if (version == WebSocketVersion.V13) {
/* 16:78 */       return new WebSocketClientHandshaker13(webSocketURL, WebSocketVersion.V13, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength);
/* 17:   */     }
/* 18:81 */     if (version == WebSocketVersion.V08) {
/* 19:82 */       return new WebSocketClientHandshaker08(webSocketURL, WebSocketVersion.V08, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength);
/* 20:   */     }
/* 21:85 */     if (version == WebSocketVersion.V07) {
/* 22:86 */       return new WebSocketClientHandshaker07(webSocketURL, WebSocketVersion.V07, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength);
/* 23:   */     }
/* 24:89 */     if (version == WebSocketVersion.V00) {
/* 25:90 */       return new WebSocketClientHandshaker00(webSocketURL, WebSocketVersion.V00, subprotocol, customHeaders, maxFramePayloadLength);
/* 26:   */     }
/* 27:94 */     throw new WebSocketHandshakeException("Protocol version " + version + " not supported.");
/* 28:   */   }
/* 29:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory
 * JD-Core Version:    0.7.0.1
 */