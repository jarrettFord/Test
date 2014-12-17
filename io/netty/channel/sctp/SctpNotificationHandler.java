/*  1:   */ package io.netty.channel.sctp;
/*  2:   */ 
/*  3:   */ import com.sun.nio.sctp.AbstractNotificationHandler;
/*  4:   */ import com.sun.nio.sctp.AssociationChangeNotification;
/*  5:   */ import com.sun.nio.sctp.HandlerResult;
/*  6:   */ import com.sun.nio.sctp.Notification;
/*  7:   */ import com.sun.nio.sctp.PeerAddressChangeNotification;
/*  8:   */ import com.sun.nio.sctp.SendFailedNotification;
/*  9:   */ import com.sun.nio.sctp.ShutdownNotification;
/* 10:   */ import io.netty.channel.ChannelPipeline;
/* 11:   */ 
/* 12:   */ public final class SctpNotificationHandler
/* 13:   */   extends AbstractNotificationHandler<Object>
/* 14:   */ {
/* 15:   */   private final SctpChannel sctpChannel;
/* 16:   */   
/* 17:   */   public SctpNotificationHandler(SctpChannel sctpChannel)
/* 18:   */   {
/* 19:38 */     if (sctpChannel == null) {
/* 20:39 */       throw new NullPointerException("sctpChannel");
/* 21:   */     }
/* 22:41 */     this.sctpChannel = sctpChannel;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public HandlerResult handleNotification(AssociationChangeNotification notification, Object o)
/* 26:   */   {
/* 27:46 */     fireEvent(notification);
/* 28:47 */     return HandlerResult.CONTINUE;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public HandlerResult handleNotification(PeerAddressChangeNotification notification, Object o)
/* 32:   */   {
/* 33:52 */     fireEvent(notification);
/* 34:53 */     return HandlerResult.CONTINUE;
/* 35:   */   }
/* 36:   */   
/* 37:   */   public HandlerResult handleNotification(SendFailedNotification notification, Object o)
/* 38:   */   {
/* 39:58 */     fireEvent(notification);
/* 40:59 */     return HandlerResult.CONTINUE;
/* 41:   */   }
/* 42:   */   
/* 43:   */   public HandlerResult handleNotification(ShutdownNotification notification, Object o)
/* 44:   */   {
/* 45:64 */     fireEvent(notification);
/* 46:65 */     this.sctpChannel.close();
/* 47:66 */     return HandlerResult.RETURN;
/* 48:   */   }
/* 49:   */   
/* 50:   */   private void fireEvent(Notification notification)
/* 51:   */   {
/* 52:70 */     this.sctpChannel.pipeline().fireUserEventTriggered(notification);
/* 53:   */   }
/* 54:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.sctp.SctpNotificationHandler
 * JD-Core Version:    0.7.0.1
 */