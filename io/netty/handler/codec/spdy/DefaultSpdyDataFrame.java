/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.util.IllegalReferenceCountException;
/*   6:    */ import io.netty.util.internal.StringUtil;
/*   7:    */ 
/*   8:    */ public class DefaultSpdyDataFrame
/*   9:    */   extends DefaultSpdyStreamFrame
/*  10:    */   implements SpdyDataFrame
/*  11:    */ {
/*  12:    */   private final ByteBuf data;
/*  13:    */   
/*  14:    */   public DefaultSpdyDataFrame(int streamId)
/*  15:    */   {
/*  16: 36 */     this(streamId, Unpooled.buffer(0));
/*  17:    */   }
/*  18:    */   
/*  19:    */   public DefaultSpdyDataFrame(int streamId, ByteBuf data)
/*  20:    */   {
/*  21: 46 */     super(streamId);
/*  22: 47 */     if (data == null) {
/*  23: 48 */       throw new NullPointerException("data");
/*  24:    */     }
/*  25: 50 */     this.data = validate(data);
/*  26:    */   }
/*  27:    */   
/*  28:    */   private static ByteBuf validate(ByteBuf data)
/*  29:    */   {
/*  30: 54 */     if (data.readableBytes() > 16777215) {
/*  31: 55 */       throw new IllegalArgumentException("data payload cannot exceed 16777215 bytes");
/*  32:    */     }
/*  33: 58 */     return data;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public SpdyDataFrame setStreamId(int streamId)
/*  37:    */   {
/*  38: 63 */     super.setStreamId(streamId);
/*  39: 64 */     return this;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public SpdyDataFrame setLast(boolean last)
/*  43:    */   {
/*  44: 69 */     super.setLast(last);
/*  45: 70 */     return this;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public ByteBuf content()
/*  49:    */   {
/*  50: 75 */     if (this.data.refCnt() <= 0) {
/*  51: 76 */       throw new IllegalReferenceCountException(this.data.refCnt());
/*  52:    */     }
/*  53: 78 */     return this.data;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public SpdyDataFrame copy()
/*  57:    */   {
/*  58: 83 */     SpdyDataFrame frame = new DefaultSpdyDataFrame(streamId(), content().copy());
/*  59: 84 */     frame.setLast(isLast());
/*  60: 85 */     return frame;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public SpdyDataFrame duplicate()
/*  64:    */   {
/*  65: 90 */     SpdyDataFrame frame = new DefaultSpdyDataFrame(streamId(), content().duplicate());
/*  66: 91 */     frame.setLast(isLast());
/*  67: 92 */     return frame;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public int refCnt()
/*  71:    */   {
/*  72: 97 */     return this.data.refCnt();
/*  73:    */   }
/*  74:    */   
/*  75:    */   public SpdyDataFrame retain()
/*  76:    */   {
/*  77:102 */     this.data.retain();
/*  78:103 */     return this;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public SpdyDataFrame retain(int increment)
/*  82:    */   {
/*  83:108 */     this.data.retain(increment);
/*  84:109 */     return this;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public boolean release()
/*  88:    */   {
/*  89:114 */     return this.data.release();
/*  90:    */   }
/*  91:    */   
/*  92:    */   public boolean release(int decrement)
/*  93:    */   {
/*  94:119 */     return this.data.release(decrement);
/*  95:    */   }
/*  96:    */   
/*  97:    */   public String toString()
/*  98:    */   {
/*  99:124 */     StringBuilder buf = new StringBuilder();
/* 100:125 */     buf.append(StringUtil.simpleClassName(this));
/* 101:126 */     buf.append("(last: ");
/* 102:127 */     buf.append(isLast());
/* 103:128 */     buf.append(')');
/* 104:129 */     buf.append(StringUtil.NEWLINE);
/* 105:130 */     buf.append("--> Stream-ID = ");
/* 106:131 */     buf.append(streamId());
/* 107:132 */     buf.append(StringUtil.NEWLINE);
/* 108:133 */     buf.append("--> Size = ");
/* 109:134 */     if (refCnt() == 0) {
/* 110:135 */       buf.append("(freed)");
/* 111:    */     } else {
/* 112:137 */       buf.append(content().readableBytes());
/* 113:    */     }
/* 114:139 */     return buf.toString();
/* 115:    */   }
/* 116:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.DefaultSpdyDataFrame
 * JD-Core Version:    0.7.0.1
 */