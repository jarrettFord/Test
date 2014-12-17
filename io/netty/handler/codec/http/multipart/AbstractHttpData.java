/*   1:    */ package io.netty.handler.codec.http.multipart;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.handler.codec.http.HttpConstants;
/*   6:    */ import io.netty.util.AbstractReferenceCounted;
/*   7:    */ import java.io.IOException;
/*   8:    */ import java.nio.charset.Charset;
/*   9:    */ import java.util.regex.Matcher;
/*  10:    */ import java.util.regex.Pattern;
/*  11:    */ 
/*  12:    */ public abstract class AbstractHttpData
/*  13:    */   extends AbstractReferenceCounted
/*  14:    */   implements HttpData
/*  15:    */ {
/*  16: 32 */   private static final Pattern STRIP_PATTERN = Pattern.compile("(?:^\\s+|\\s+$|\\n)");
/*  17: 33 */   private static final Pattern REPLACE_PATTERN = Pattern.compile("[\\r\\t]");
/*  18:    */   protected final String name;
/*  19:    */   protected long definedSize;
/*  20:    */   protected long size;
/*  21: 38 */   protected Charset charset = HttpConstants.DEFAULT_CHARSET;
/*  22:    */   protected boolean completed;
/*  23:    */   
/*  24:    */   protected AbstractHttpData(String name, Charset charset, long size)
/*  25:    */   {
/*  26: 42 */     if (name == null) {
/*  27: 43 */       throw new NullPointerException("name");
/*  28:    */     }
/*  29: 46 */     name = REPLACE_PATTERN.matcher(name).replaceAll(" ");
/*  30: 47 */     name = STRIP_PATTERN.matcher(name).replaceAll("");
/*  31: 49 */     if (name.isEmpty()) {
/*  32: 50 */       throw new IllegalArgumentException("empty name");
/*  33:    */     }
/*  34: 53 */     this.name = name;
/*  35: 54 */     if (charset != null) {
/*  36: 55 */       setCharset(charset);
/*  37:    */     }
/*  38: 57 */     this.definedSize = size;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public String getName()
/*  42:    */   {
/*  43: 62 */     return this.name;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public boolean isCompleted()
/*  47:    */   {
/*  48: 67 */     return this.completed;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public Charset getCharset()
/*  52:    */   {
/*  53: 72 */     return this.charset;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public void setCharset(Charset charset)
/*  57:    */   {
/*  58: 77 */     if (charset == null) {
/*  59: 78 */       throw new NullPointerException("charset");
/*  60:    */     }
/*  61: 80 */     this.charset = charset;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public long length()
/*  65:    */   {
/*  66: 85 */     return this.size;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public ByteBuf content()
/*  70:    */   {
/*  71:    */     try
/*  72:    */     {
/*  73: 91 */       return getByteBuf();
/*  74:    */     }
/*  75:    */     catch (IOException e)
/*  76:    */     {
/*  77: 93 */       throw new ChannelException(e);
/*  78:    */     }
/*  79:    */   }
/*  80:    */   
/*  81:    */   protected void deallocate()
/*  82:    */   {
/*  83: 99 */     delete();
/*  84:    */   }
/*  85:    */   
/*  86:    */   public HttpData retain()
/*  87:    */   {
/*  88:104 */     super.retain();
/*  89:105 */     return this;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public HttpData retain(int increment)
/*  93:    */   {
/*  94:110 */     super.retain(increment);
/*  95:111 */     return this;
/*  96:    */   }
/*  97:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.AbstractHttpData
 * JD-Core Version:    0.7.0.1
 */