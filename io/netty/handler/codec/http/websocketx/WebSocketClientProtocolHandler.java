/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelHandlerContext;
/*   4:    */ import io.netty.channel.ChannelPipeline;
/*   5:    */ import io.netty.handler.codec.http.HttpHeaders;
/*   6:    */ import java.net.URI;
/*   7:    */ import java.util.List;
/*   8:    */ 
/*   9:    */ public class WebSocketClientProtocolHandler
/*  10:    */   extends WebSocketProtocolHandler
/*  11:    */ {
/*  12:    */   private final WebSocketClientHandshaker handshaker;
/*  13:    */   private final boolean handleCloseFrames;
/*  14:    */   
/*  15:    */   public static enum ClientHandshakeStateEvent
/*  16:    */   {
/*  17: 52 */     HANDSHAKE_ISSUED,  HANDSHAKE_COMPLETE;
/*  18:    */     
/*  19:    */     private ClientHandshakeStateEvent() {}
/*  20:    */   }
/*  21:    */   
/*  22:    */   public WebSocketClientProtocolHandler(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength, boolean handleCloseFrames)
/*  23:    */   {
/*  24: 80 */     this(WebSocketClientHandshakerFactory.newHandshaker(webSocketURL, version, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength), handleCloseFrames);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public WebSocketClientProtocolHandler(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength)
/*  28:    */   {
/*  29:102 */     this(webSocketURL, version, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength, true);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public WebSocketClientProtocolHandler(WebSocketClientHandshaker handshaker, boolean handleCloseFrames)
/*  33:    */   {
/*  34:116 */     this.handshaker = handshaker;
/*  35:117 */     this.handleCloseFrames = handleCloseFrames;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public WebSocketClientProtocolHandler(WebSocketClientHandshaker handshaker)
/*  39:    */   {
/*  40:128 */     this(handshaker, true);
/*  41:    */   }
/*  42:    */   
/*  43:    */   protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out)
/*  44:    */     throws Exception
/*  45:    */   {
/*  46:133 */     if ((this.handleCloseFrames) && ((frame instanceof CloseWebSocketFrame)))
/*  47:    */     {
/*  48:134 */       ctx.close();
/*  49:135 */       return;
/*  50:    */     }
/*  51:137 */     super.decode(ctx, frame, out);
/*  52:    */   }
/*  53:    */   
/*  54:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  55:    */   {
/*  56:142 */     ChannelPipeline cp = ctx.pipeline();
/*  57:143 */     if (cp.get(WebSocketClientProtocolHandshakeHandler.class) == null) {
/*  58:145 */       ctx.pipeline().addBefore(ctx.name(), WebSocketClientProtocolHandshakeHandler.class.getName(), new WebSocketClientProtocolHandshakeHandler(this.handshaker));
/*  59:    */     }
/*  60:    */   }
/*  61:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler
 * JD-Core Version:    0.7.0.1
 */