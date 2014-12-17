/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import java.util.Map.Entry;
/*   5:    */ 
/*   6:    */ public class DefaultSpdyHeadersFrame
/*   7:    */   extends DefaultSpdyStreamFrame
/*   8:    */   implements SpdyHeadersFrame
/*   9:    */ {
/*  10:    */   private boolean invalid;
/*  11:    */   private boolean truncated;
/*  12: 30 */   private final SpdyHeaders headers = new DefaultSpdyHeaders();
/*  13:    */   
/*  14:    */   public DefaultSpdyHeadersFrame(int streamId)
/*  15:    */   {
/*  16: 38 */     super(streamId);
/*  17:    */   }
/*  18:    */   
/*  19:    */   public SpdyHeadersFrame setStreamId(int streamId)
/*  20:    */   {
/*  21: 43 */     super.setStreamId(streamId);
/*  22: 44 */     return this;
/*  23:    */   }
/*  24:    */   
/*  25:    */   public SpdyHeadersFrame setLast(boolean last)
/*  26:    */   {
/*  27: 49 */     super.setLast(last);
/*  28: 50 */     return this;
/*  29:    */   }
/*  30:    */   
/*  31:    */   public boolean isInvalid()
/*  32:    */   {
/*  33: 55 */     return this.invalid;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public SpdyHeadersFrame setInvalid()
/*  37:    */   {
/*  38: 60 */     this.invalid = true;
/*  39: 61 */     return this;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public boolean isTruncated()
/*  43:    */   {
/*  44: 66 */     return this.truncated;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public SpdyHeadersFrame setTruncated()
/*  48:    */   {
/*  49: 71 */     this.truncated = true;
/*  50: 72 */     return this;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public SpdyHeaders headers()
/*  54:    */   {
/*  55: 77 */     return this.headers;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public String toString()
/*  59:    */   {
/*  60: 82 */     StringBuilder buf = new StringBuilder();
/*  61: 83 */     buf.append(StringUtil.simpleClassName(this));
/*  62: 84 */     buf.append("(last: ");
/*  63: 85 */     buf.append(isLast());
/*  64: 86 */     buf.append(')');
/*  65: 87 */     buf.append(StringUtil.NEWLINE);
/*  66: 88 */     buf.append("--> Stream-ID = ");
/*  67: 89 */     buf.append(streamId());
/*  68: 90 */     buf.append(StringUtil.NEWLINE);
/*  69: 91 */     buf.append("--> Headers:");
/*  70: 92 */     buf.append(StringUtil.NEWLINE);
/*  71: 93 */     appendHeaders(buf);
/*  72:    */     
/*  73:    */ 
/*  74: 96 */     buf.setLength(buf.length() - StringUtil.NEWLINE.length());
/*  75: 97 */     return buf.toString();
/*  76:    */   }
/*  77:    */   
/*  78:    */   protected void appendHeaders(StringBuilder buf)
/*  79:    */   {
/*  80:101 */     for (Map.Entry<String, String> e : headers())
/*  81:    */     {
/*  82:102 */       buf.append("    ");
/*  83:103 */       buf.append((String)e.getKey());
/*  84:104 */       buf.append(": ");
/*  85:105 */       buf.append((String)e.getValue());
/*  86:106 */       buf.append(StringUtil.NEWLINE);
/*  87:    */     }
/*  88:    */   }
/*  89:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.DefaultSpdyHeadersFrame
 * JD-Core Version:    0.7.0.1
 */