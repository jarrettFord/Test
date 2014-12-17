/*   1:    */ package io.netty.channel.nio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelConfig;
/*   5:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   6:    */ import io.netty.channel.ChannelPipeline;
/*   7:    */ import io.netty.channel.ServerChannel;
/*   8:    */ import java.io.IOException;
/*   9:    */ import java.nio.channels.SelectableChannel;
/*  10:    */ import java.nio.channels.SelectionKey;
/*  11:    */ import java.util.ArrayList;
/*  12:    */ import java.util.List;
/*  13:    */ 
/*  14:    */ public abstract class AbstractNioMessageChannel
/*  15:    */   extends AbstractNioChannel
/*  16:    */ {
/*  17:    */   protected AbstractNioMessageChannel(Channel parent, SelectableChannel ch, int readInterestOp)
/*  18:    */   {
/*  19: 39 */     super(parent, ch, readInterestOp);
/*  20:    */   }
/*  21:    */   
/*  22:    */   protected AbstractNioChannel.AbstractNioUnsafe newUnsafe()
/*  23:    */   {
/*  24: 44 */     return new NioMessageUnsafe(null);
/*  25:    */   }
/*  26:    */   
/*  27:    */   private final class NioMessageUnsafe
/*  28:    */     extends AbstractNioChannel.AbstractNioUnsafe
/*  29:    */   {
/*  30:    */     private NioMessageUnsafe()
/*  31:    */     {
/*  32: 47 */       super();
/*  33:    */     }
/*  34:    */     
/*  35: 49 */     private final List<Object> readBuf = new ArrayList();
/*  36:    */     
/*  37:    */     public void read()
/*  38:    */     {
/*  39: 53 */       assert (AbstractNioMessageChannel.this.eventLoop().inEventLoop());
/*  40: 54 */       ChannelConfig config = AbstractNioMessageChannel.this.config();
/*  41: 55 */       if ((!config.isAutoRead()) && (!AbstractNioMessageChannel.this.isReadPending()))
/*  42:    */       {
/*  43: 57 */         removeReadOp();
/*  44: 58 */         return;
/*  45:    */       }
/*  46: 61 */       int maxMessagesPerRead = config.getMaxMessagesPerRead();
/*  47: 62 */       ChannelPipeline pipeline = AbstractNioMessageChannel.this.pipeline();
/*  48: 63 */       boolean closed = false;
/*  49: 64 */       Throwable exception = null;
/*  50:    */       try
/*  51:    */       {
/*  52:    */         try
/*  53:    */         {
/*  54:    */           for (;;)
/*  55:    */           {
/*  56: 68 */             int localRead = AbstractNioMessageChannel.this.doReadMessages(this.readBuf);
/*  57: 69 */             if (localRead == 0) {
/*  58:    */               break;
/*  59:    */             }
/*  60: 72 */             if (localRead < 0) {
/*  61: 73 */               closed = true;
/*  62: 78 */             } else if (config.isAutoRead()) {
/*  63: 82 */               if (this.readBuf.size() >= maxMessagesPerRead) {
/*  64:    */                 break;
/*  65:    */               }
/*  66:    */             }
/*  67:    */           }
/*  68:    */         }
/*  69:    */         catch (Throwable t)
/*  70:    */         {
/*  71: 87 */           exception = t;
/*  72:    */         }
/*  73: 89 */         AbstractNioMessageChannel.this.setReadPending(false);
/*  74: 90 */         int size = this.readBuf.size();
/*  75: 91 */         for (int i = 0; i < size; i++) {
/*  76: 92 */           pipeline.fireChannelRead(this.readBuf.get(i));
/*  77:    */         }
/*  78: 95 */         this.readBuf.clear();
/*  79: 96 */         pipeline.fireChannelReadComplete();
/*  80: 98 */         if (exception != null)
/*  81:    */         {
/*  82: 99 */           if ((exception instanceof IOException)) {
/*  83:102 */             closed = !(AbstractNioMessageChannel.this instanceof ServerChannel);
/*  84:    */           }
/*  85:105 */           pipeline.fireExceptionCaught(exception);
/*  86:    */         }
/*  87:108 */         if ((closed) && 
/*  88:109 */           (AbstractNioMessageChannel.this.isOpen())) {
/*  89:110 */           close(voidPromise());
/*  90:    */         }
/*  91:    */       }
/*  92:    */       finally
/*  93:    */       {
/*  94:120 */         if ((!config.isAutoRead()) && (!AbstractNioMessageChannel.this.isReadPending())) {
/*  95:121 */           removeReadOp();
/*  96:    */         }
/*  97:    */       }
/*  98:    */     }
/*  99:    */   }
/* 100:    */   
/* 101:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 102:    */     throws Exception
/* 103:    */   {
/* 104:129 */     SelectionKey key = selectionKey();
/* 105:130 */     int interestOps = key.interestOps();
/* 106:    */     for (;;)
/* 107:    */     {
/* 108:133 */       Object msg = in.current();
/* 109:134 */       if (msg == null)
/* 110:    */       {
/* 111:136 */         if ((interestOps & 0x4) == 0) {
/* 112:    */           break;
/* 113:    */         }
/* 114:137 */         key.interestOps(interestOps & 0xFFFFFFFB); break;
/* 115:    */       }
/* 116:142 */       boolean done = false;
/* 117:143 */       for (int i = config().getWriteSpinCount() - 1; i >= 0; i--) {
/* 118:144 */         if (doWriteMessage(msg, in))
/* 119:    */         {
/* 120:145 */           done = true;
/* 121:146 */           break;
/* 122:    */         }
/* 123:    */       }
/* 124:150 */       if (done)
/* 125:    */       {
/* 126:151 */         in.remove();
/* 127:    */       }
/* 128:    */       else
/* 129:    */       {
/* 130:154 */         if ((interestOps & 0x4) != 0) {
/* 131:    */           break;
/* 132:    */         }
/* 133:155 */         key.interestOps(interestOps | 0x4); break;
/* 134:    */       }
/* 135:    */     }
/* 136:    */   }
/* 137:    */   
/* 138:    */   protected abstract int doReadMessages(List<Object> paramList)
/* 139:    */     throws Exception;
/* 140:    */   
/* 141:    */   protected abstract boolean doWriteMessage(Object paramObject, ChannelOutboundBuffer paramChannelOutboundBuffer)
/* 142:    */     throws Exception;
/* 143:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.nio.AbstractNioMessageChannel
 * JD-Core Version:    0.7.0.1
 */