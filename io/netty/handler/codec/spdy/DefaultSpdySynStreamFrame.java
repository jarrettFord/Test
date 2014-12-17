/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ 
/*   5:    */ public class DefaultSpdySynStreamFrame
/*   6:    */   extends DefaultSpdyHeadersFrame
/*   7:    */   implements SpdySynStreamFrame
/*   8:    */ {
/*   9:    */   private int associatedStreamId;
/*  10:    */   private byte priority;
/*  11:    */   private boolean unidirectional;
/*  12:    */   
/*  13:    */   public DefaultSpdySynStreamFrame(int streamId, int associatedStreamId, byte priority)
/*  14:    */   {
/*  15: 38 */     super(streamId);
/*  16: 39 */     setAssociatedStreamId(associatedStreamId);
/*  17: 40 */     setPriority(priority);
/*  18:    */   }
/*  19:    */   
/*  20:    */   public SpdySynStreamFrame setStreamId(int streamId)
/*  21:    */   {
/*  22: 45 */     super.setStreamId(streamId);
/*  23: 46 */     return this;
/*  24:    */   }
/*  25:    */   
/*  26:    */   public SpdySynStreamFrame setLast(boolean last)
/*  27:    */   {
/*  28: 51 */     super.setLast(last);
/*  29: 52 */     return this;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public SpdySynStreamFrame setInvalid()
/*  33:    */   {
/*  34: 57 */     super.setInvalid();
/*  35: 58 */     return this;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public int associatedStreamId()
/*  39:    */   {
/*  40: 63 */     return this.associatedStreamId;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public SpdySynStreamFrame setAssociatedStreamId(int associatedStreamId)
/*  44:    */   {
/*  45: 68 */     if (associatedStreamId < 0) {
/*  46: 69 */       throw new IllegalArgumentException("Associated-To-Stream-ID cannot be negative: " + associatedStreamId);
/*  47:    */     }
/*  48: 73 */     this.associatedStreamId = associatedStreamId;
/*  49: 74 */     return this;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public byte priority()
/*  53:    */   {
/*  54: 79 */     return this.priority;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public SpdySynStreamFrame setPriority(byte priority)
/*  58:    */   {
/*  59: 84 */     if ((priority < 0) || (priority > 7)) {
/*  60: 85 */       throw new IllegalArgumentException("Priority must be between 0 and 7 inclusive: " + priority);
/*  61:    */     }
/*  62: 88 */     this.priority = priority;
/*  63: 89 */     return this;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public boolean isUnidirectional()
/*  67:    */   {
/*  68: 94 */     return this.unidirectional;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public SpdySynStreamFrame setUnidirectional(boolean unidirectional)
/*  72:    */   {
/*  73: 99 */     this.unidirectional = unidirectional;
/*  74:100 */     return this;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public String toString()
/*  78:    */   {
/*  79:105 */     StringBuilder buf = new StringBuilder();
/*  80:106 */     buf.append(StringUtil.simpleClassName(this));
/*  81:107 */     buf.append("(last: ");
/*  82:108 */     buf.append(isLast());
/*  83:109 */     buf.append("; unidirectional: ");
/*  84:110 */     buf.append(isUnidirectional());
/*  85:111 */     buf.append(')');
/*  86:112 */     buf.append(StringUtil.NEWLINE);
/*  87:113 */     buf.append("--> Stream-ID = ");
/*  88:114 */     buf.append(streamId());
/*  89:115 */     buf.append(StringUtil.NEWLINE);
/*  90:116 */     if (this.associatedStreamId != 0)
/*  91:    */     {
/*  92:117 */       buf.append("--> Associated-To-Stream-ID = ");
/*  93:118 */       buf.append(associatedStreamId());
/*  94:119 */       buf.append(StringUtil.NEWLINE);
/*  95:    */     }
/*  96:121 */     buf.append("--> Priority = ");
/*  97:122 */     buf.append(priority());
/*  98:123 */     buf.append(StringUtil.NEWLINE);
/*  99:124 */     buf.append("--> Headers:");
/* 100:125 */     buf.append(StringUtil.NEWLINE);
/* 101:126 */     appendHeaders(buf);
/* 102:    */     
/* 103:    */ 
/* 104:129 */     buf.setLength(buf.length() - StringUtil.NEWLINE.length());
/* 105:130 */     return buf.toString();
/* 106:    */   }
/* 107:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.DefaultSpdySynStreamFrame
 * JD-Core Version:    0.7.0.1
 */