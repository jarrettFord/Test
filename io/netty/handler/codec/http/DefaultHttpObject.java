/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.handler.codec.DecoderResult;
/*  4:   */ 
/*  5:   */ public class DefaultHttpObject
/*  6:   */   implements HttpObject
/*  7:   */ {
/*  8:22 */   private DecoderResult decoderResult = DecoderResult.SUCCESS;
/*  9:   */   
/* 10:   */   public DecoderResult getDecoderResult()
/* 11:   */   {
/* 12:30 */     return this.decoderResult;
/* 13:   */   }
/* 14:   */   
/* 15:   */   public void setDecoderResult(DecoderResult decoderResult)
/* 16:   */   {
/* 17:35 */     if (decoderResult == null) {
/* 18:36 */       throw new NullPointerException("decoderResult");
/* 19:   */     }
/* 20:38 */     this.decoderResult = decoderResult;
/* 21:   */   }
/* 22:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.DefaultHttpObject
 * JD-Core Version:    0.7.0.1
 */