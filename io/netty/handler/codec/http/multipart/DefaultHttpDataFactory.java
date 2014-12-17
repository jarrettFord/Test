/*   1:    */ package io.netty.handler.codec.http.multipart;
/*   2:    */ 
/*   3:    */ import io.netty.handler.codec.http.HttpRequest;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.nio.charset.Charset;
/*   7:    */ import java.util.ArrayList;
/*   8:    */ import java.util.List;
/*   9:    */ import java.util.Map;
/*  10:    */ 
/*  11:    */ public class DefaultHttpDataFactory
/*  12:    */   implements HttpDataFactory
/*  13:    */ {
/*  14:    */   public static final long MINSIZE = 16384L;
/*  15:    */   private final boolean useDisk;
/*  16:    */   private final boolean checkSize;
/*  17:    */   private long minSize;
/*  18: 50 */   private final Map<HttpRequest, List<HttpData>> requestFileDeleteMap = PlatformDependent.newConcurrentHashMap();
/*  19:    */   
/*  20:    */   public DefaultHttpDataFactory()
/*  21:    */   {
/*  22: 57 */     this.useDisk = false;
/*  23: 58 */     this.checkSize = true;
/*  24: 59 */     this.minSize = 16384L;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public DefaultHttpDataFactory(boolean useDisk)
/*  28:    */   {
/*  29: 66 */     this.useDisk = useDisk;
/*  30: 67 */     this.checkSize = false;
/*  31:    */   }
/*  32:    */   
/*  33:    */   public DefaultHttpDataFactory(long minSize)
/*  34:    */   {
/*  35: 75 */     this.useDisk = false;
/*  36: 76 */     this.checkSize = true;
/*  37: 77 */     this.minSize = minSize;
/*  38:    */   }
/*  39:    */   
/*  40:    */   private List<HttpData> getList(HttpRequest request)
/*  41:    */   {
/*  42: 84 */     List<HttpData> list = (List)this.requestFileDeleteMap.get(request);
/*  43: 85 */     if (list == null)
/*  44:    */     {
/*  45: 86 */       list = new ArrayList();
/*  46: 87 */       this.requestFileDeleteMap.put(request, list);
/*  47:    */     }
/*  48: 89 */     return list;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public Attribute createAttribute(HttpRequest request, String name)
/*  52:    */   {
/*  53: 94 */     if (this.useDisk)
/*  54:    */     {
/*  55: 95 */       Attribute attribute = new DiskAttribute(name);
/*  56: 96 */       List<HttpData> fileToDelete = getList(request);
/*  57: 97 */       fileToDelete.add(attribute);
/*  58: 98 */       return attribute;
/*  59:    */     }
/*  60:100 */     if (this.checkSize)
/*  61:    */     {
/*  62:101 */       Attribute attribute = new MixedAttribute(name, this.minSize);
/*  63:102 */       List<HttpData> fileToDelete = getList(request);
/*  64:103 */       fileToDelete.add(attribute);
/*  65:104 */       return attribute;
/*  66:    */     }
/*  67:106 */     return new MemoryAttribute(name);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public Attribute createAttribute(HttpRequest request, String name, String value)
/*  71:    */   {
/*  72:111 */     if (this.useDisk)
/*  73:    */     {
/*  74:    */       Attribute attribute;
/*  75:    */       try
/*  76:    */       {
/*  77:114 */         attribute = new DiskAttribute(name, value);
/*  78:    */       }
/*  79:    */       catch (IOException e)
/*  80:    */       {
/*  81:117 */         attribute = new MixedAttribute(name, value, this.minSize);
/*  82:    */       }
/*  83:119 */       List<HttpData> fileToDelete = getList(request);
/*  84:120 */       fileToDelete.add(attribute);
/*  85:121 */       return attribute;
/*  86:    */     }
/*  87:123 */     if (this.checkSize)
/*  88:    */     {
/*  89:124 */       Attribute attribute = new MixedAttribute(name, value, this.minSize);
/*  90:125 */       List<HttpData> fileToDelete = getList(request);
/*  91:126 */       fileToDelete.add(attribute);
/*  92:127 */       return attribute;
/*  93:    */     }
/*  94:    */     try
/*  95:    */     {
/*  96:130 */       return new MemoryAttribute(name, value);
/*  97:    */     }
/*  98:    */     catch (IOException e)
/*  99:    */     {
/* 100:132 */       throw new IllegalArgumentException(e);
/* 101:    */     }
/* 102:    */   }
/* 103:    */   
/* 104:    */   public FileUpload createFileUpload(HttpRequest request, String name, String filename, String contentType, String contentTransferEncoding, Charset charset, long size)
/* 105:    */   {
/* 106:140 */     if (this.useDisk)
/* 107:    */     {
/* 108:141 */       FileUpload fileUpload = new DiskFileUpload(name, filename, contentType, contentTransferEncoding, charset, size);
/* 109:    */       
/* 110:143 */       List<HttpData> fileToDelete = getList(request);
/* 111:144 */       fileToDelete.add(fileUpload);
/* 112:145 */       return fileUpload;
/* 113:    */     }
/* 114:147 */     if (this.checkSize)
/* 115:    */     {
/* 116:148 */       FileUpload fileUpload = new MixedFileUpload(name, filename, contentType, contentTransferEncoding, charset, size, this.minSize);
/* 117:    */       
/* 118:150 */       List<HttpData> fileToDelete = getList(request);
/* 119:151 */       fileToDelete.add(fileUpload);
/* 120:152 */       return fileUpload;
/* 121:    */     }
/* 122:154 */     return new MemoryFileUpload(name, filename, contentType, contentTransferEncoding, charset, size);
/* 123:    */   }
/* 124:    */   
/* 125:    */   public void removeHttpDataFromClean(HttpRequest request, InterfaceHttpData data)
/* 126:    */   {
/* 127:160 */     if ((data instanceof HttpData))
/* 128:    */     {
/* 129:161 */       List<HttpData> fileToDelete = getList(request);
/* 130:162 */       fileToDelete.remove(data);
/* 131:    */     }
/* 132:    */   }
/* 133:    */   
/* 134:    */   public void cleanRequestHttpDatas(HttpRequest request)
/* 135:    */   {
/* 136:168 */     List<HttpData> fileToDelete = (List)this.requestFileDeleteMap.remove(request);
/* 137:169 */     if (fileToDelete != null)
/* 138:    */     {
/* 139:170 */       for (HttpData data : fileToDelete) {
/* 140:171 */         data.delete();
/* 141:    */       }
/* 142:173 */       fileToDelete.clear();
/* 143:    */     }
/* 144:    */   }
/* 145:    */   
/* 146:    */   public void cleanAllHttpDatas()
/* 147:    */   {
/* 148:179 */     for (HttpRequest request : this.requestFileDeleteMap.keySet())
/* 149:    */     {
/* 150:180 */       List<HttpData> fileToDelete = (List)this.requestFileDeleteMap.get(request);
/* 151:181 */       if (fileToDelete != null)
/* 152:    */       {
/* 153:182 */         for (HttpData data : fileToDelete) {
/* 154:183 */           data.delete();
/* 155:    */         }
/* 156:185 */         fileToDelete.clear();
/* 157:    */       }
/* 158:187 */       this.requestFileDeleteMap.remove(request);
/* 159:    */     }
/* 160:    */   }
/* 161:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.DefaultHttpDataFactory
 * JD-Core Version:    0.7.0.1
 */