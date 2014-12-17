/*  1:   */ package io.netty.channel.oio;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ThreadPerChannelEventLoopGroup;
/*  4:   */ import java.util.concurrent.Executors;
/*  5:   */ import java.util.concurrent.ThreadFactory;
/*  6:   */ 
/*  7:   */ public class OioEventLoopGroup
/*  8:   */   extends ThreadPerChannelEventLoopGroup
/*  9:   */ {
/* 10:   */   public OioEventLoopGroup()
/* 11:   */   {
/* 12:39 */     this(0);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public OioEventLoopGroup(int maxChannels)
/* 16:   */   {
/* 17:52 */     this(maxChannels, Executors.defaultThreadFactory());
/* 18:   */   }
/* 19:   */   
/* 20:   */   public OioEventLoopGroup(int maxChannels, ThreadFactory threadFactory)
/* 21:   */   {
/* 22:67 */     super(maxChannels, threadFactory, new Object[0]);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.oio.OioEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */