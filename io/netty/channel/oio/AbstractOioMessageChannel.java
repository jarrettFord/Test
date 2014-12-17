/*  1:   */ package io.netty.channel.oio;
/*  2:   */ 
/*  3:   */ import io.netty.channel.Channel;
/*  4:   */ import io.netty.channel.Channel.Unsafe;
/*  5:   */ import io.netty.channel.ChannelConfig;
/*  6:   */ import io.netty.channel.ChannelPipeline;
/*  7:   */ import java.io.IOException;
/*  8:   */ import java.util.ArrayList;
/*  9:   */ import java.util.List;
/* 10:   */ 
/* 11:   */ public abstract class AbstractOioMessageChannel
/* 12:   */   extends AbstractOioChannel
/* 13:   */ {
/* 14:31 */   private final List<Object> readBuf = new ArrayList();
/* 15:   */   
/* 16:   */   protected AbstractOioMessageChannel(Channel parent)
/* 17:   */   {
/* 18:34 */     super(parent);
/* 19:   */   }
/* 20:   */   
/* 21:   */   protected void doRead()
/* 22:   */   {
/* 23:39 */     ChannelConfig config = config();
/* 24:40 */     ChannelPipeline pipeline = pipeline();
/* 25:41 */     boolean closed = false;
/* 26:42 */     int maxMessagesPerRead = config.getMaxMessagesPerRead();
/* 27:   */     
/* 28:44 */     Throwable exception = null;
/* 29:45 */     int localRead = 0;
/* 30:   */     try
/* 31:   */     {
/* 32:   */       for (;;)
/* 33:   */       {
/* 34:48 */         localRead = doReadMessages(this.readBuf);
/* 35:49 */         if (localRead != 0) {
/* 36:52 */           if (localRead < 0) {
/* 37:53 */             closed = true;
/* 38:57 */           } else if (this.readBuf.size() < maxMessagesPerRead) {
/* 39:57 */             if (!config.isAutoRead()) {
/* 40:   */               break;
/* 41:   */             }
/* 42:   */           }
/* 43:   */         }
/* 44:   */       }
/* 45:   */     }
/* 46:   */     catch (Throwable t)
/* 47:   */     {
/* 48:62 */       exception = t;
/* 49:   */     }
/* 50:65 */     int size = this.readBuf.size();
/* 51:66 */     for (int i = 0; i < size; i++) {
/* 52:67 */       pipeline.fireChannelRead(this.readBuf.get(i));
/* 53:   */     }
/* 54:69 */     this.readBuf.clear();
/* 55:70 */     pipeline.fireChannelReadComplete();
/* 56:72 */     if (exception != null)
/* 57:   */     {
/* 58:73 */       if ((exception instanceof IOException)) {
/* 59:74 */         closed = true;
/* 60:   */       }
/* 61:77 */       pipeline().fireExceptionCaught(exception);
/* 62:   */     }
/* 63:80 */     if (closed)
/* 64:   */     {
/* 65:81 */       if (isOpen()) {
/* 66:82 */         unsafe().close(unsafe().voidPromise());
/* 67:   */       }
/* 68:   */     }
/* 69:84 */     else if ((localRead == 0) && (isActive())) {
/* 70:91 */       read();
/* 71:   */     }
/* 72:   */   }
/* 73:   */   
/* 74:   */   protected abstract int doReadMessages(List<Object> paramList)
/* 75:   */     throws Exception;
/* 76:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.oio.AbstractOioMessageChannel
 * JD-Core Version:    0.7.0.1
 */