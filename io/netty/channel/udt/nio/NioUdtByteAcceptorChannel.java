/*  1:   */ package io.netty.channel.udt.nio;
/*  2:   */ 
/*  3:   */ import com.barchart.udt.TypeUDT;
/*  4:   */ import com.barchart.udt.nio.ServerSocketChannelUDT;
/*  5:   */ import com.barchart.udt.nio.SocketChannelUDT;
/*  6:   */ import io.netty.channel.ChannelMetadata;
/*  7:   */ import java.util.List;
/*  8:   */ 
/*  9:   */ public class NioUdtByteAcceptorChannel
/* 10:   */   extends NioUdtAcceptorChannel
/* 11:   */ {
/* 12:29 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/* 13:   */   
/* 14:   */   public NioUdtByteAcceptorChannel()
/* 15:   */   {
/* 16:32 */     super(TypeUDT.STREAM);
/* 17:   */   }
/* 18:   */   
/* 19:   */   protected int doReadMessages(List<Object> buf)
/* 20:   */     throws Exception
/* 21:   */   {
/* 22:37 */     SocketChannelUDT channelUDT = javaChannel().accept();
/* 23:38 */     if (channelUDT == null) {
/* 24:39 */       return 0;
/* 25:   */     }
/* 26:41 */     buf.add(new NioUdtByteConnectorChannel(this, channelUDT));
/* 27:42 */     return 1;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public ChannelMetadata metadata()
/* 31:   */   {
/* 32:48 */     return METADATA;
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.udt.nio.NioUdtByteAcceptorChannel
 * JD-Core Version:    0.7.0.1
 */