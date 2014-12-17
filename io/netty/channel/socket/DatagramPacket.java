/*  1:   */ package io.netty.channel.socket;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufHolder;
/*  5:   */ import io.netty.channel.DefaultAddressedEnvelope;
/*  6:   */ import java.net.InetSocketAddress;
/*  7:   */ 
/*  8:   */ public final class DatagramPacket
/*  9:   */   extends DefaultAddressedEnvelope<ByteBuf, InetSocketAddress>
/* 10:   */   implements ByteBufHolder
/* 11:   */ {
/* 12:   */   public DatagramPacket(ByteBuf data, InetSocketAddress recipient)
/* 13:   */   {
/* 14:34 */     super(data, recipient);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public DatagramPacket(ByteBuf data, InetSocketAddress recipient, InetSocketAddress sender)
/* 18:   */   {
/* 19:42 */     super(data, recipient, sender);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public DatagramPacket copy()
/* 23:   */   {
/* 24:47 */     return new DatagramPacket(((ByteBuf)content()).copy(), (InetSocketAddress)recipient(), (InetSocketAddress)sender());
/* 25:   */   }
/* 26:   */   
/* 27:   */   public DatagramPacket duplicate()
/* 28:   */   {
/* 29:52 */     return new DatagramPacket(((ByteBuf)content()).duplicate(), (InetSocketAddress)recipient(), (InetSocketAddress)sender());
/* 30:   */   }
/* 31:   */   
/* 32:   */   public DatagramPacket retain()
/* 33:   */   {
/* 34:57 */     super.retain();
/* 35:58 */     return this;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public DatagramPacket retain(int increment)
/* 39:   */   {
/* 40:63 */     super.retain(increment);
/* 41:64 */     return this;
/* 42:   */   }
/* 43:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.socket.DatagramPacket
 * JD-Core Version:    0.7.0.1
 */