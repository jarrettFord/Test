/*  1:   */ package io.netty.channel.epoll;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelOption;
/*  4:   */ 
/*  5:   */ public final class EpollChannelOption<T>
/*  6:   */   extends ChannelOption<T>
/*  7:   */ {
/*  8:22 */   public static final ChannelOption<Boolean> TCP_CORK = valueOf("TCP_CORK");
/*  9:23 */   public static final ChannelOption<Integer> TCP_KEEPIDLE = valueOf("TCP_KEEPIDLE");
/* 10:24 */   public static final ChannelOption<Integer> TCP_KEEPINTVL = valueOf("TCP_KEEPINTVL");
/* 11:25 */   public static final ChannelOption<Integer> TCP_KEEPCNT = valueOf("TCP_KEEPCNT");
/* 12:27 */   public static final ChannelOption<Boolean> SO_REUSEPORT = valueOf("SO_REUSEPORT");
/* 13:   */   
/* 14:   */   private EpollChannelOption(String name)
/* 15:   */   {
/* 16:31 */     super(name);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.epoll.EpollChannelOption
 * JD-Core Version:    0.7.0.1
 */