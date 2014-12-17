/*  1:   */ package io.netty.channel.sctp;
/*  2:   */ 
/*  3:   */ import com.sun.nio.sctp.SctpStandardSocketOptions.InitMaxStreams;
/*  4:   */ import io.netty.channel.ChannelOption;
/*  5:   */ import java.net.SocketAddress;
/*  6:   */ 
/*  7:   */ public class SctpChannelOption<T>
/*  8:   */   extends ChannelOption<T>
/*  9:   */ {
/* 10:28 */   public static final SctpChannelOption<Boolean> SCTP_DISABLE_FRAGMENTS = new SctpChannelOption("SCTP_DISABLE_FRAGMENTS");
/* 11:30 */   public static final SctpChannelOption<Boolean> SCTP_EXPLICIT_COMPLETE = new SctpChannelOption("SCTP_EXPLICIT_COMPLETE");
/* 12:32 */   public static final SctpChannelOption<Integer> SCTP_FRAGMENT_INTERLEAVE = new SctpChannelOption("SCTP_FRAGMENT_INTERLEAVE");
/* 13:34 */   public static final SctpChannelOption<SctpStandardSocketOptions.InitMaxStreams> SCTP_INIT_MAXSTREAMS = new SctpChannelOption("SCTP_INIT_MAXSTREAMS");
/* 14:37 */   public static final SctpChannelOption<Boolean> SCTP_NODELAY = new SctpChannelOption("SCTP_NODELAY");
/* 15:39 */   public static final SctpChannelOption<SocketAddress> SCTP_PRIMARY_ADDR = new SctpChannelOption("SCTP_PRIMARY_ADDR");
/* 16:41 */   public static final SctpChannelOption<SocketAddress> SCTP_SET_PEER_PRIMARY_ADDR = new SctpChannelOption("SCTP_SET_PEER_PRIMARY_ADDR");
/* 17:   */   
/* 18:   */   @Deprecated
/* 19:   */   protected SctpChannelOption(String name)
/* 20:   */   {
/* 21:49 */     super(name);
/* 22:   */   }
/* 23:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.sctp.SctpChannelOption
 * JD-Core Version:    0.7.0.1
 */