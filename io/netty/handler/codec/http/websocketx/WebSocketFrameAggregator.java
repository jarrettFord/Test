/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.CompositeByteBuf;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.handler.codec.MessageToMessageDecoder;
/*   8:    */ import io.netty.handler.codec.TooLongFrameException;
/*   9:    */ import java.util.List;
/*  10:    */ 
/*  11:    */ public class WebSocketFrameAggregator
/*  12:    */   extends MessageToMessageDecoder<WebSocketFrame>
/*  13:    */ {
/*  14:    */   private final int maxFrameSize;
/*  15:    */   private WebSocketFrame currentFrame;
/*  16:    */   private boolean tooLongFrameFound;
/*  17:    */   
/*  18:    */   public WebSocketFrameAggregator(int maxFrameSize)
/*  19:    */   {
/*  20: 44 */     if (maxFrameSize < 1) {
/*  21: 45 */       throw new IllegalArgumentException("maxFrameSize must be > 0");
/*  22:    */     }
/*  23: 47 */     this.maxFrameSize = maxFrameSize;
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out)
/*  27:    */     throws Exception
/*  28:    */   {
/*  29: 52 */     if (this.currentFrame == null)
/*  30:    */     {
/*  31: 53 */       this.tooLongFrameFound = false;
/*  32: 54 */       if (msg.isFinalFragment())
/*  33:    */       {
/*  34: 55 */         out.add(msg.retain());
/*  35: 56 */         return;
/*  36:    */       }
/*  37: 58 */       ByteBuf buf = ctx.alloc().compositeBuffer().addComponent(msg.content().retain());
/*  38: 59 */       buf.writerIndex(buf.writerIndex() + msg.content().readableBytes());
/*  39: 61 */       if ((msg instanceof TextWebSocketFrame))
/*  40:    */       {
/*  41: 62 */         this.currentFrame = new TextWebSocketFrame(true, msg.rsv(), buf);
/*  42:    */       }
/*  43: 63 */       else if ((msg instanceof BinaryWebSocketFrame))
/*  44:    */       {
/*  45: 64 */         this.currentFrame = new BinaryWebSocketFrame(true, msg.rsv(), buf);
/*  46:    */       }
/*  47:    */       else
/*  48:    */       {
/*  49: 66 */         buf.release();
/*  50: 67 */         throw new IllegalStateException("WebSocket frame was not of type TextWebSocketFrame or BinaryWebSocketFrame");
/*  51:    */       }
/*  52: 70 */       return;
/*  53:    */     }
/*  54: 72 */     if ((msg instanceof ContinuationWebSocketFrame))
/*  55:    */     {
/*  56: 73 */       if (this.tooLongFrameFound)
/*  57:    */       {
/*  58: 74 */         if (msg.isFinalFragment()) {
/*  59: 75 */           this.currentFrame = null;
/*  60:    */         }
/*  61: 77 */         return;
/*  62:    */       }
/*  63: 79 */       CompositeByteBuf content = (CompositeByteBuf)this.currentFrame.content();
/*  64: 80 */       if (content.readableBytes() > this.maxFrameSize - msg.content().readableBytes())
/*  65:    */       {
/*  66: 82 */         this.currentFrame.release();
/*  67: 83 */         this.tooLongFrameFound = true;
/*  68: 84 */         throw new TooLongFrameException("WebSocketFrame length exceeded " + content + " bytes.");
/*  69:    */       }
/*  70: 88 */       content.addComponent(msg.content().retain());
/*  71: 89 */       content.writerIndex(content.writerIndex() + msg.content().readableBytes());
/*  72: 91 */       if (msg.isFinalFragment())
/*  73:    */       {
/*  74: 92 */         WebSocketFrame currentFrame = this.currentFrame;
/*  75: 93 */         this.currentFrame = null;
/*  76: 94 */         out.add(currentFrame);
/*  77: 95 */         return;
/*  78:    */       }
/*  79: 97 */       return;
/*  80:    */     }
/*  81:102 */     out.add(msg.retain());
/*  82:    */   }
/*  83:    */   
/*  84:    */   public void channelInactive(ChannelHandlerContext ctx)
/*  85:    */     throws Exception
/*  86:    */   {
/*  87:107 */     super.channelInactive(ctx);
/*  88:109 */     if (this.currentFrame != null)
/*  89:    */     {
/*  90:110 */       this.currentFrame.release();
/*  91:111 */       this.currentFrame = null;
/*  92:    */     }
/*  93:    */   }
/*  94:    */   
/*  95:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/*  96:    */     throws Exception
/*  97:    */   {
/*  98:117 */     super.handlerRemoved(ctx);
/*  99:120 */     if (this.currentFrame != null)
/* 100:    */     {
/* 101:121 */       this.currentFrame.release();
/* 102:122 */       this.currentFrame = null;
/* 103:    */     }
/* 104:    */   }
/* 105:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator
 * JD-Core Version:    0.7.0.1
 */