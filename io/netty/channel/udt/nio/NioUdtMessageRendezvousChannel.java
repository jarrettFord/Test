/*  1:   */ package io.netty.channel.udt.nio;
/*  2:   */ 
/*  3:   */ import com.barchart.udt.TypeUDT;
/*  4:   */ 
/*  5:   */ public class NioUdtMessageRendezvousChannel
/*  6:   */   extends NioUdtMessageConnectorChannel
/*  7:   */ {
/*  8:   */   public NioUdtMessageRendezvousChannel()
/*  9:   */   {
/* 10:30 */     super(NioUdtProvider.newRendezvousChannelUDT(TypeUDT.DATAGRAM));
/* 11:   */   }
/* 12:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.udt.nio.NioUdtMessageRendezvousChannel
 * JD-Core Version:    0.7.0.1
 */