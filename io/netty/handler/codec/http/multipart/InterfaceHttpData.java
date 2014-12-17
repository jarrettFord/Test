/*  1:   */ package io.netty.handler.codec.http.multipart;
/*  2:   */ 
/*  3:   */ import io.netty.util.ReferenceCounted;
/*  4:   */ 
/*  5:   */ public abstract interface InterfaceHttpData
/*  6:   */   extends Comparable<InterfaceHttpData>, ReferenceCounted
/*  7:   */ {
/*  8:   */   public abstract String getName();
/*  9:   */   
/* 10:   */   public abstract HttpDataType getHttpDataType();
/* 11:   */   
/* 12:   */   public static enum HttpDataType
/* 13:   */   {
/* 14:25 */     Attribute,  FileUpload,  InternalAttribute;
/* 15:   */     
/* 16:   */     private HttpDataType() {}
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.InterfaceHttpData
 * JD-Core Version:    0.7.0.1
 */