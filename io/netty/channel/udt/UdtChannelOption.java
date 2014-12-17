/*  1:   */ package io.netty.channel.udt;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelOption;
/*  4:   */ 
/*  5:   */ public final class UdtChannelOption<T>
/*  6:   */   extends ChannelOption<T>
/*  7:   */ {
/*  8:29 */   public static final UdtChannelOption<Integer> PROTOCOL_RECEIVE_BUFFER_SIZE = new UdtChannelOption("PROTOCOL_RECEIVE_BUFFER_SIZE");
/*  9:35 */   public static final UdtChannelOption<Integer> PROTOCOL_SEND_BUFFER_SIZE = new UdtChannelOption("PROTOCOL_SEND_BUFFER_SIZE");
/* 10:41 */   public static final UdtChannelOption<Integer> SYSTEM_RECEIVE_BUFFER_SIZE = new UdtChannelOption("SYSTEM_RECEIVE_BUFFER_SIZE");
/* 11:47 */   public static final UdtChannelOption<Integer> SYSTEM_SEND_BUFFER_SIZE = new UdtChannelOption("SYSTEM_SEND_BUFFER_SIZE");
/* 12:   */   
/* 13:   */   private UdtChannelOption(String name)
/* 14:   */   {
/* 15:52 */     super(name);
/* 16:   */   }
/* 17:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.udt.UdtChannelOption
 * JD-Core Version:    0.7.0.1
 */