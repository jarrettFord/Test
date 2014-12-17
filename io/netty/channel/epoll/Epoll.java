/*  1:   */ package io.netty.channel.epoll;
/*  2:   */ 
/*  3:   */ public final class Epoll
/*  4:   */ {
/*  5:   */   private static final boolean IS_AVAILABLE;
/*  6:   */   
/*  7:   */   static
/*  8:   */   {
/*  9:27 */     int epollFd = -1;
/* 10:28 */     int eventFd = -1;
/* 11:   */     try
/* 12:   */     {
/* 13:30 */       epollFd = Native.epollCreate();
/* 14:31 */       eventFd = Native.eventFd();
/* 15:32 */       boolean available = true;
/* 16:37 */       if (epollFd != -1) {
/* 17:   */         try
/* 18:   */         {
/* 19:39 */           Native.close(epollFd);
/* 20:   */         }
/* 21:   */         catch (Exception ignore) {}
/* 22:   */       }
/* 23:44 */       if (eventFd != -1) {
/* 24:   */         try
/* 25:   */         {
/* 26:46 */           Native.close(eventFd);
/* 27:   */         }
/* 28:   */         catch (Exception ignore) {}
/* 29:   */       }
/* 30:52 */       IS_AVAILABLE = available;
/* 31:   */     }
/* 32:   */     catch (Throwable cause)
/* 33:   */     {
/* 34:35 */       available = false;
/* 35:37 */       if (epollFd != -1) {
/* 36:   */         try
/* 37:   */         {
/* 38:39 */           Native.close(epollFd);
/* 39:   */         }
/* 40:   */         catch (Exception ignore) {}
/* 41:   */       }
/* 42:44 */       if (eventFd != -1) {
/* 43:   */         try
/* 44:   */         {
/* 45:46 */           Native.close(eventFd);
/* 46:   */         }
/* 47:   */         catch (Exception ignore) {}
/* 48:   */       }
/* 49:   */     }
/* 50:   */     finally
/* 51:   */     {
/* 52:37 */       if (epollFd != -1) {
/* 53:   */         try
/* 54:   */         {
/* 55:39 */           Native.close(epollFd);
/* 56:   */         }
/* 57:   */         catch (Exception ignore) {}
/* 58:   */       }
/* 59:44 */       if (eventFd != -1) {
/* 60:   */         try
/* 61:   */         {
/* 62:46 */           Native.close(eventFd);
/* 63:   */         }
/* 64:   */         catch (Exception ignore) {}
/* 65:   */       }
/* 66:   */     }
/* 67:   */   }
/* 68:   */   
/* 69:   */   public static boolean isAvailable()
/* 70:   */   {
/* 71:60 */     return IS_AVAILABLE;
/* 72:   */   }
/* 73:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.epoll.Epoll
 * JD-Core Version:    0.7.0.1
 */