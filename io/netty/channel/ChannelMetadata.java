/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ public final class ChannelMetadata
/*  4:   */ {
/*  5:   */   private final boolean hasDisconnect;
/*  6:   */   
/*  7:   */   public ChannelMetadata(boolean hasDisconnect)
/*  8:   */   {
/*  9:35 */     this.hasDisconnect = hasDisconnect;
/* 10:   */   }
/* 11:   */   
/* 12:   */   public boolean hasDisconnect()
/* 13:   */   {
/* 14:44 */     return this.hasDisconnect;
/* 15:   */   }
/* 16:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.ChannelMetadata
 * JD-Core Version:    0.7.0.1
 */