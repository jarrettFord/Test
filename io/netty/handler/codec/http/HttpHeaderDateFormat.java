/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.FastThreadLocal;
/*  4:   */ import java.text.ParsePosition;
/*  5:   */ import java.text.SimpleDateFormat;
/*  6:   */ import java.util.Date;
/*  7:   */ import java.util.Locale;
/*  8:   */ import java.util.TimeZone;
/*  9:   */ 
/* 10:   */ final class HttpHeaderDateFormat
/* 11:   */   extends SimpleDateFormat
/* 12:   */ {
/* 13:   */   private static final long serialVersionUID = -925286159755905325L;
/* 14:39 */   private final SimpleDateFormat format1 = new HttpHeaderDateFormatObsolete1();
/* 15:40 */   private final SimpleDateFormat format2 = new HttpHeaderDateFormatObsolete2();
/* 16:42 */   private static final FastThreadLocal<HttpHeaderDateFormat> dateFormatThreadLocal = new FastThreadLocal()
/* 17:   */   {
/* 18:   */     protected HttpHeaderDateFormat initialValue()
/* 19:   */     {
/* 20:46 */       return new HttpHeaderDateFormat(null);
/* 21:   */     }
/* 22:   */   };
/* 23:   */   
/* 24:   */   static HttpHeaderDateFormat get()
/* 25:   */   {
/* 26:51 */     return (HttpHeaderDateFormat)dateFormatThreadLocal.get();
/* 27:   */   }
/* 28:   */   
/* 29:   */   private HttpHeaderDateFormat()
/* 30:   */   {
/* 31:59 */     super("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
/* 32:60 */     setTimeZone(TimeZone.getTimeZone("GMT"));
/* 33:   */   }
/* 34:   */   
/* 35:   */   public Date parse(String text, ParsePosition pos)
/* 36:   */   {
/* 37:65 */     Date date = super.parse(text, pos);
/* 38:66 */     if (date == null) {
/* 39:67 */       date = this.format1.parse(text, pos);
/* 40:   */     }
/* 41:69 */     if (date == null) {
/* 42:70 */       date = this.format2.parse(text, pos);
/* 43:   */     }
/* 44:72 */     return date;
/* 45:   */   }
/* 46:   */   
/* 47:   */   private static final class HttpHeaderDateFormatObsolete1
/* 48:   */     extends SimpleDateFormat
/* 49:   */   {
/* 50:   */     private static final long serialVersionUID = -3178072504225114298L;
/* 51:   */     
/* 52:   */     HttpHeaderDateFormatObsolete1()
/* 53:   */     {
/* 54:83 */       super(Locale.ENGLISH);
/* 55:84 */       setTimeZone(TimeZone.getTimeZone("GMT"));
/* 56:   */     }
/* 57:   */   }
/* 58:   */   
/* 59:   */   private static final class HttpHeaderDateFormatObsolete2
/* 60:   */     extends SimpleDateFormat
/* 61:   */   {
/* 62:   */     private static final long serialVersionUID = 3010674519968303714L;
/* 63:   */     
/* 64:   */     HttpHeaderDateFormatObsolete2()
/* 65:   */     {
/* 66:97 */       super(Locale.ENGLISH);
/* 67:98 */       setTimeZone(TimeZone.getTimeZone("GMT"));
/* 68:   */     }
/* 69:   */   }
/* 70:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpHeaderDateFormat
 * JD-Core Version:    0.7.0.1
 */