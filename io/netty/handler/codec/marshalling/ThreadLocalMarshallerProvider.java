/*  1:   */ package io.netty.handler.codec.marshalling;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelHandlerContext;
/*  4:   */ import io.netty.util.concurrent.FastThreadLocal;
/*  5:   */ import org.jboss.marshalling.Marshaller;
/*  6:   */ import org.jboss.marshalling.MarshallerFactory;
/*  7:   */ import org.jboss.marshalling.MarshallingConfiguration;
/*  8:   */ 
/*  9:   */ public class ThreadLocalMarshallerProvider
/* 10:   */   implements MarshallerProvider
/* 11:   */ {
/* 12:31 */   private final FastThreadLocal<Marshaller> marshallers = new FastThreadLocal();
/* 13:   */   private final MarshallerFactory factory;
/* 14:   */   private final MarshallingConfiguration config;
/* 15:   */   
/* 16:   */   public ThreadLocalMarshallerProvider(MarshallerFactory factory, MarshallingConfiguration config)
/* 17:   */   {
/* 18:43 */     this.factory = factory;
/* 19:44 */     this.config = config;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public Marshaller getMarshaller(ChannelHandlerContext ctx)
/* 23:   */     throws Exception
/* 24:   */   {
/* 25:49 */     Marshaller marshaller = (Marshaller)this.marshallers.get();
/* 26:50 */     if (marshaller == null)
/* 27:   */     {
/* 28:51 */       marshaller = this.factory.createMarshaller(this.config);
/* 29:52 */       this.marshallers.set(marshaller);
/* 30:   */     }
/* 31:54 */     return marshaller;
/* 32:   */   }
/* 33:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.marshalling.ThreadLocalMarshallerProvider
 * JD-Core Version:    0.7.0.1
 */