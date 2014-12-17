/*  1:   */ package io.netty.channel.rxtx;
/*  2:   */ 
/*  3:   */ import java.net.SocketAddress;
/*  4:   */ 
/*  5:   */ public class RxtxDeviceAddress
/*  6:   */   extends SocketAddress
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = -2907820090993709523L;
/*  9:   */   private final String value;
/* 10:   */   
/* 11:   */   public RxtxDeviceAddress(String value)
/* 12:   */   {
/* 13:36 */     this.value = value;
/* 14:   */   }
/* 15:   */   
/* 16:   */   public String value()
/* 17:   */   {
/* 18:43 */     return this.value;
/* 19:   */   }
/* 20:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.rxtx.RxtxDeviceAddress
 * JD-Core Version:    0.7.0.1
 */