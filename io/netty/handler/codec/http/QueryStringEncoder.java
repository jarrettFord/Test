/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import java.io.UnsupportedEncodingException;
/*   4:    */ import java.net.URI;
/*   5:    */ import java.net.URISyntaxException;
/*   6:    */ import java.net.URLEncoder;
/*   7:    */ import java.nio.charset.Charset;
/*   8:    */ import java.nio.charset.UnsupportedCharsetException;
/*   9:    */ import java.util.ArrayList;
/*  10:    */ import java.util.List;
/*  11:    */ 
/*  12:    */ public class QueryStringEncoder
/*  13:    */ {
/*  14:    */   private final Charset charset;
/*  15:    */   private final String uri;
/*  16: 42 */   private final List<Param> params = new ArrayList();
/*  17:    */   
/*  18:    */   public QueryStringEncoder(String uri)
/*  19:    */   {
/*  20: 49 */     this(uri, HttpConstants.DEFAULT_CHARSET);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public QueryStringEncoder(String uri, Charset charset)
/*  24:    */   {
/*  25: 57 */     if (uri == null) {
/*  26: 58 */       throw new NullPointerException("getUri");
/*  27:    */     }
/*  28: 60 */     if (charset == null) {
/*  29: 61 */       throw new NullPointerException("charset");
/*  30:    */     }
/*  31: 64 */     this.uri = uri;
/*  32: 65 */     this.charset = charset;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public void addParam(String name, String value)
/*  36:    */   {
/*  37: 72 */     if (name == null) {
/*  38: 73 */       throw new NullPointerException("name");
/*  39:    */     }
/*  40: 75 */     this.params.add(new Param(name, value));
/*  41:    */   }
/*  42:    */   
/*  43:    */   public URI toUri()
/*  44:    */     throws URISyntaxException
/*  45:    */   {
/*  46: 84 */     return new URI(toString());
/*  47:    */   }
/*  48:    */   
/*  49:    */   public String toString()
/*  50:    */   {
/*  51: 94 */     if (this.params.isEmpty()) {
/*  52: 95 */       return this.uri;
/*  53:    */     }
/*  54: 97 */     StringBuilder sb = new StringBuilder(this.uri).append('?');
/*  55: 98 */     for (int i = 0; i < this.params.size(); i++)
/*  56:    */     {
/*  57: 99 */       Param param = (Param)this.params.get(i);
/*  58:100 */       sb.append(encodeComponent(param.name, this.charset));
/*  59:101 */       if (param.value != null)
/*  60:    */       {
/*  61:102 */         sb.append('=');
/*  62:103 */         sb.append(encodeComponent(param.value, this.charset));
/*  63:    */       }
/*  64:105 */       if (i != this.params.size() - 1) {
/*  65:106 */         sb.append('&');
/*  66:    */       }
/*  67:    */     }
/*  68:109 */     return sb.toString();
/*  69:    */   }
/*  70:    */   
/*  71:    */   private static String encodeComponent(String s, Charset charset)
/*  72:    */   {
/*  73:    */     try
/*  74:    */     {
/*  75:116 */       return URLEncoder.encode(s, charset.name()).replace("+", "%20");
/*  76:    */     }
/*  77:    */     catch (UnsupportedEncodingException ignored)
/*  78:    */     {
/*  79:118 */       throw new UnsupportedCharsetException(charset.name());
/*  80:    */     }
/*  81:    */   }
/*  82:    */   
/*  83:    */   private static final class Param
/*  84:    */   {
/*  85:    */     final String name;
/*  86:    */     final String value;
/*  87:    */     
/*  88:    */     Param(String name, String value)
/*  89:    */     {
/*  90:128 */       this.value = value;
/*  91:129 */       this.name = name;
/*  92:    */     }
/*  93:    */   }
/*  94:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.QueryStringEncoder
 * JD-Core Version:    0.7.0.1
 */