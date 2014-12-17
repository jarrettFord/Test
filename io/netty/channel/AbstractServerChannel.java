/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.ReferenceCountUtil;
/*  4:   */ import java.net.SocketAddress;
/*  5:   */ 
/*  6:   */ public abstract class AbstractServerChannel
/*  7:   */   extends AbstractChannel
/*  8:   */   implements ServerChannel
/*  9:   */ {
/* 10:35 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/* 11:   */   
/* 12:   */   protected AbstractServerChannel()
/* 13:   */   {
/* 14:41 */     super(null);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public ChannelMetadata metadata()
/* 18:   */   {
/* 19:46 */     return METADATA;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public SocketAddress remoteAddress()
/* 23:   */   {
/* 24:51 */     return null;
/* 25:   */   }
/* 26:   */   
/* 27:   */   protected SocketAddress remoteAddress0()
/* 28:   */   {
/* 29:56 */     return null;
/* 30:   */   }
/* 31:   */   
/* 32:   */   protected void doDisconnect()
/* 33:   */     throws Exception
/* 34:   */   {
/* 35:61 */     throw new UnsupportedOperationException();
/* 36:   */   }
/* 37:   */   
/* 38:   */   protected AbstractChannel.AbstractUnsafe newUnsafe()
/* 39:   */   {
/* 40:66 */     return new DefaultServerUnsafe(null);
/* 41:   */   }
/* 42:   */   
/* 43:   */   protected void doWrite(ChannelOutboundBuffer in)
/* 44:   */     throws Exception
/* 45:   */   {
/* 46:71 */     throw new UnsupportedOperationException();
/* 47:   */   }
/* 48:   */   
/* 49:   */   private final class DefaultServerUnsafe
/* 50:   */     extends AbstractChannel.AbstractUnsafe
/* 51:   */   {
/* 52:   */     private DefaultServerUnsafe()
/* 53:   */     {
/* 54:74 */       super();
/* 55:   */     }
/* 56:   */     
/* 57:   */     public void write(Object msg, ChannelPromise promise)
/* 58:   */     {
/* 59:77 */       ReferenceCountUtil.release(msg);
/* 60:78 */       reject(promise);
/* 61:   */     }
/* 62:   */     
/* 63:   */     public void flush() {}
/* 64:   */     
/* 65:   */     public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 66:   */     {
/* 67:88 */       reject(promise);
/* 68:   */     }
/* 69:   */     
/* 70:   */     private void reject(ChannelPromise promise)
/* 71:   */     {
/* 72:92 */       safeSetFailure(promise, new UnsupportedOperationException());
/* 73:   */     }
/* 74:   */   }
/* 75:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.AbstractServerChannel
 * JD-Core Version:    0.7.0.1
 */