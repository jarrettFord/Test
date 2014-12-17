/*  1:   */ package io.netty.channel.udt.nio;
/*  2:   */ 
/*  3:   */ import com.barchart.udt.TypeUDT;
/*  4:   */ 
/*  5:   */ public class NioUdtByteRendezvousChannel
/*  6:   */   extends NioUdtByteConnectorChannel
/*  7:   */ {
/*  8:   */   public NioUdtByteRendezvousChannel()
/*  9:   */   {
/* 10:26 */     super(NioUdtProvider.newRendezvousChannelUDT(TypeUDT.STREAM));
/* 11:   */   }
/* 12:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.udt.nio.NioUdtByteRendezvousChannel
 * JD-Core Version:    0.7.0.1
 */