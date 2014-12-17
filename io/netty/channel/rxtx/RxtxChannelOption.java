/*  1:   */ package io.netty.channel.rxtx;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelOption;
/*  4:   */ 
/*  5:   */ public final class RxtxChannelOption<T>
/*  6:   */   extends ChannelOption<T>
/*  7:   */ {
/*  8:27 */   public static final RxtxChannelOption<Integer> BAUD_RATE = new RxtxChannelOption("BAUD_RATE");
/*  9:30 */   public static final RxtxChannelOption<Boolean> DTR = new RxtxChannelOption("DTR");
/* 10:33 */   public static final RxtxChannelOption<Boolean> RTS = new RxtxChannelOption("RTS");
/* 11:36 */   public static final RxtxChannelOption<RxtxChannelConfig.Stopbits> STOP_BITS = new RxtxChannelOption("STOP_BITS");
/* 12:39 */   public static final RxtxChannelOption<RxtxChannelConfig.Databits> DATA_BITS = new RxtxChannelOption("DATA_BITS");
/* 13:42 */   public static final RxtxChannelOption<RxtxChannelConfig.Paritybit> PARITY_BIT = new RxtxChannelOption("PARITY_BIT");
/* 14:45 */   public static final RxtxChannelOption<Integer> WAIT_TIME = new RxtxChannelOption("WAIT_TIME");
/* 15:48 */   public static final RxtxChannelOption<Integer> READ_TIMEOUT = new RxtxChannelOption("READ_TIMEOUT");
/* 16:   */   
/* 17:   */   private RxtxChannelOption(String name)
/* 18:   */   {
/* 19:53 */     super(name);
/* 20:   */   }
/* 21:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.rxtx.RxtxChannelOption
 * JD-Core Version:    0.7.0.1
 */