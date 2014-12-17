/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import java.io.InputStream;
/*   5:    */ import java.io.PrintStream;
/*   6:    */ import java.net.URL;
/*   7:    */ import java.text.ParseException;
/*   8:    */ import java.text.SimpleDateFormat;
/*   9:    */ import java.util.Date;
/*  10:    */ import java.util.Enumeration;
/*  11:    */ import java.util.HashSet;
/*  12:    */ import java.util.Map;
/*  13:    */ import java.util.Properties;
/*  14:    */ import java.util.Set;
/*  15:    */ import java.util.TreeMap;
/*  16:    */ 
/*  17:    */ public final class Version
/*  18:    */ {
/*  19:    */   private static final String PROP_VERSION = ".version";
/*  20:    */   private static final String PROP_BUILD_DATE = ".buildDate";
/*  21:    */   private static final String PROP_COMMIT_DATE = ".commitDate";
/*  22:    */   private static final String PROP_SHORT_COMMIT_HASH = ".shortCommitHash";
/*  23:    */   private static final String PROP_LONG_COMMIT_HASH = ".longCommitHash";
/*  24:    */   private static final String PROP_REPO_STATUS = ".repoStatus";
/*  25:    */   private final String artifactId;
/*  26:    */   private final String artifactVersion;
/*  27:    */   private final long buildTimeMillis;
/*  28:    */   private final long commitTimeMillis;
/*  29:    */   private final String shortCommitHash;
/*  30:    */   private final String longCommitHash;
/*  31:    */   private final String repositoryStatus;
/*  32:    */   
/*  33:    */   public static Map<String, Version> identify()
/*  34:    */   {
/*  35: 56 */     return identify(null);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public static Map<String, Version> identify(ClassLoader classLoader)
/*  39:    */   {
/*  40: 65 */     if (classLoader == null) {
/*  41: 66 */       classLoader = PlatformDependent.getContextClassLoader();
/*  42:    */     }
/*  43: 70 */     Properties props = new Properties();
/*  44:    */     try
/*  45:    */     {
/*  46: 72 */       Enumeration<URL> resources = classLoader.getResources("META-INF/io.netty.versions.properties");
/*  47:    */       for (;;)
/*  48:    */       {
/*  49: 73 */         if (resources.hasMoreElements())
/*  50:    */         {
/*  51: 74 */           URL url = (URL)resources.nextElement();
/*  52: 75 */           InputStream in = url.openStream();
/*  53:    */           try
/*  54:    */           {
/*  55: 77 */             props.load(in);
/*  56:    */             try
/*  57:    */             {
/*  58: 80 */               in.close();
/*  59:    */             }
/*  60:    */             catch (Exception ignore) {}
/*  61:    */           }
/*  62:    */           finally
/*  63:    */           {
/*  64:    */             try
/*  65:    */             {
/*  66: 80 */               in.close();
/*  67:    */             }
/*  68:    */             catch (Exception ignore) {}
/*  69:    */           }
/*  70:    */         }
/*  71:    */       }
/*  72:    */     }
/*  73:    */     catch (Exception ignore) {}
/*  74: 91 */     Set<String> artifactIds = new HashSet();
/*  75: 92 */     for (Object o : props.keySet())
/*  76:    */     {
/*  77: 93 */       String k = (String)o;
/*  78:    */       
/*  79: 95 */       int dotIndex = k.indexOf('.');
/*  80: 96 */       if (dotIndex > 0)
/*  81:    */       {
/*  82:100 */         String artifactId = k.substring(0, dotIndex);
/*  83:103 */         if ((props.containsKey(artifactId + ".version")) && (props.containsKey(artifactId + ".buildDate")) && (props.containsKey(artifactId + ".commitDate")) && (props.containsKey(artifactId + ".shortCommitHash")) && (props.containsKey(artifactId + ".longCommitHash")) && (props.containsKey(artifactId + ".repoStatus"))) {
/*  84:112 */           artifactIds.add(artifactId);
/*  85:    */         }
/*  86:    */       }
/*  87:    */     }
/*  88:115 */     Map<String, Version> versions = new TreeMap();
/*  89:116 */     for (String artifactId : artifactIds) {
/*  90:117 */       versions.put(artifactId, new Version(artifactId, props.getProperty(artifactId + ".version"), parseIso8601(props.getProperty(artifactId + ".buildDate")), parseIso8601(props.getProperty(artifactId + ".commitDate")), props.getProperty(artifactId + ".shortCommitHash"), props.getProperty(artifactId + ".longCommitHash"), props.getProperty(artifactId + ".repoStatus")));
/*  91:    */     }
/*  92:129 */     return versions;
/*  93:    */   }
/*  94:    */   
/*  95:    */   private static long parseIso8601(String value)
/*  96:    */   {
/*  97:    */     try
/*  98:    */     {
/*  99:134 */       return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse(value).getTime();
/* 100:    */     }
/* 101:    */     catch (ParseException e) {}
/* 102:136 */     return 0L;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public static void main(String[] args)
/* 106:    */   {
/* 107:144 */     for (Version v : identify().values()) {
/* 108:145 */       System.err.println(v);
/* 109:    */     }
/* 110:    */   }
/* 111:    */   
/* 112:    */   private Version(String artifactId, String artifactVersion, long buildTimeMillis, long commitTimeMillis, String shortCommitHash, String longCommitHash, String repositoryStatus)
/* 113:    */   {
/* 114:161 */     this.artifactId = artifactId;
/* 115:162 */     this.artifactVersion = artifactVersion;
/* 116:163 */     this.buildTimeMillis = buildTimeMillis;
/* 117:164 */     this.commitTimeMillis = commitTimeMillis;
/* 118:165 */     this.shortCommitHash = shortCommitHash;
/* 119:166 */     this.longCommitHash = longCommitHash;
/* 120:167 */     this.repositoryStatus = repositoryStatus;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public String artifactId()
/* 124:    */   {
/* 125:171 */     return this.artifactId;
/* 126:    */   }
/* 127:    */   
/* 128:    */   public String artifactVersion()
/* 129:    */   {
/* 130:175 */     return this.artifactVersion;
/* 131:    */   }
/* 132:    */   
/* 133:    */   public long buildTimeMillis()
/* 134:    */   {
/* 135:179 */     return this.buildTimeMillis;
/* 136:    */   }
/* 137:    */   
/* 138:    */   public long commitTimeMillis()
/* 139:    */   {
/* 140:183 */     return this.commitTimeMillis;
/* 141:    */   }
/* 142:    */   
/* 143:    */   public String shortCommitHash()
/* 144:    */   {
/* 145:187 */     return this.shortCommitHash;
/* 146:    */   }
/* 147:    */   
/* 148:    */   public String longCommitHash()
/* 149:    */   {
/* 150:191 */     return this.longCommitHash;
/* 151:    */   }
/* 152:    */   
/* 153:    */   public String repositoryStatus()
/* 154:    */   {
/* 155:195 */     return this.repositoryStatus;
/* 156:    */   }
/* 157:    */   
/* 158:    */   public String toString()
/* 159:    */   {
/* 160:200 */     return this.artifactId + '-' + this.artifactVersion + '.' + this.shortCommitHash + ("clean".equals(this.repositoryStatus) ? "" : new StringBuilder().append(" (repository: ").append(this.repositoryStatus).append(')').toString());
/* 161:    */   }
/* 162:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.Version
 * JD-Core Version:    0.7.0.1
 */