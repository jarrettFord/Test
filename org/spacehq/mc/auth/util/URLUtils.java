/*   1:    */ package org.spacehq.mc.auth.util;
/*   2:    */ 
/*   3:    */ import com.google.gson.Gson;
/*   4:    */ import com.google.gson.GsonBuilder;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.io.OutputStream;
/*   8:    */ import java.io.UnsupportedEncodingException;
/*   9:    */ import java.net.HttpURLConnection;
/*  10:    */ import java.net.MalformedURLException;
/*  11:    */ import java.net.URL;
/*  12:    */ import java.net.URLEncoder;
/*  13:    */ import java.util.Iterator;
/*  14:    */ import java.util.Map;
/*  15:    */ import java.util.Map.Entry;
/*  16:    */ import java.util.Set;
/*  17:    */ import java.util.UUID;
/*  18:    */ import org.spacehq.mc.auth.GameProfile;
/*  19:    */ import org.spacehq.mc.auth.exception.AuthenticationException;
/*  20:    */ import org.spacehq.mc.auth.exception.AuthenticationUnavailableException;
/*  21:    */ import org.spacehq.mc.auth.exception.InvalidCredentialsException;
/*  22:    */ import org.spacehq.mc.auth.exception.UserMigratedException;
/*  23:    */ import org.spacehq.mc.auth.properties.PropertyMap;
/*  24:    */ import org.spacehq.mc.auth.response.ProfileSearchResultsResponse;
/*  25:    */ import org.spacehq.mc.auth.response.Response;
/*  26:    */ import org.spacehq.mc.auth.serialize.GameProfileSerializer;
/*  27:    */ import org.spacehq.mc.auth.serialize.ProfileSearchResultsSerializer;
/*  28:    */ import org.spacehq.mc.auth.serialize.PropertyMapSerializer;
/*  29:    */ import org.spacehq.mc.auth.serialize.UUIDSerializer;
/*  30:    */ 
/*  31:    */ public class URLUtils
/*  32:    */ {
/*  33:    */   private static final Gson GSON;
/*  34:    */   
/*  35:    */   static
/*  36:    */   {
/*  37: 36 */     GsonBuilder builder = new GsonBuilder();
/*  38: 37 */     builder.registerTypeAdapter(GameProfile.class, new GameProfileSerializer());
/*  39: 38 */     builder.registerTypeAdapter(PropertyMap.class, new PropertyMapSerializer());
/*  40: 39 */     builder.registerTypeAdapter(UUID.class, new UUIDSerializer());
/*  41: 40 */     builder.registerTypeAdapter(ProfileSearchResultsResponse.class, new ProfileSearchResultsSerializer());
/*  42: 41 */     GSON = builder.create();
/*  43:    */   }
/*  44:    */   
/*  45:    */   public static URL constantURL(String url)
/*  46:    */   {
/*  47:    */     try
/*  48:    */     {
/*  49: 46 */       return new URL(url);
/*  50:    */     }
/*  51:    */     catch (MalformedURLException e)
/*  52:    */     {
/*  53: 48 */       throw new Error("Malformed constant url: " + url);
/*  54:    */     }
/*  55:    */   }
/*  56:    */   
/*  57:    */   public static URL concatenateURL(URL url, String query)
/*  58:    */   {
/*  59:    */     try
/*  60:    */     {
/*  61: 54 */       return (url.getQuery() != null) && (url.getQuery().length() > 0) ? new URL(url.getProtocol(), url.getHost(), url.getFile() + "&" + query) : new URL(url.getProtocol(), url.getHost(), url.getFile() + "?" + query);
/*  62:    */     }
/*  63:    */     catch (MalformedURLException e)
/*  64:    */     {
/*  65: 56 */       throw new IllegalArgumentException("Concatenated URL was malformed: " + url.toString() + ", " + query);
/*  66:    */     }
/*  67:    */   }
/*  68:    */   
/*  69:    */   public static String buildQuery(Map<String, Object> query)
/*  70:    */   {
/*  71: 61 */     if (query == null) {
/*  72: 62 */       return "";
/*  73:    */     }
/*  74: 64 */     StringBuilder builder = new StringBuilder();
/*  75: 65 */     Iterator<Map.Entry<String, Object>> it = query.entrySet().iterator();
/*  76: 66 */     while (it.hasNext())
/*  77:    */     {
/*  78: 67 */       Map.Entry<String, Object> entry = (Map.Entry)it.next();
/*  79: 68 */       if (builder.length() > 0) {
/*  80: 69 */         builder.append("&");
/*  81:    */       }
/*  82:    */       try
/*  83:    */       {
/*  84: 73 */         builder.append(URLEncoder.encode((String)entry.getKey(), "UTF-8"));
/*  85:    */       }
/*  86:    */       catch (UnsupportedEncodingException localUnsupportedEncodingException) {}
/*  87: 77 */       if (entry.getValue() != null)
/*  88:    */       {
/*  89: 78 */         builder.append("=");
/*  90:    */         try
/*  91:    */         {
/*  92: 80 */           builder.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
/*  93:    */         }
/*  94:    */         catch (UnsupportedEncodingException localUnsupportedEncodingException1) {}
/*  95:    */       }
/*  96:    */     }
/*  97: 86 */     return builder.toString();
/*  98:    */   }
/*  99:    */   
/* 100:    */   public static <T extends Response> T makeRequest(URL url, Object input, Class<T> clazz)
/* 101:    */     throws AuthenticationException
/* 102:    */   {
/* 103: 91 */     T result = null;
/* 104:    */     try
/* 105:    */     {
/* 106: 93 */       String jsonString = input == null ? performGetRequest(url) : performPostRequest(url, GSON.toJson(input), "application/json");
/* 107: 94 */       result = (Response)GSON.fromJson(jsonString, clazz);
/* 108:    */     }
/* 109:    */     catch (Exception e)
/* 110:    */     {
/* 111: 96 */       throw new AuthenticationUnavailableException("Could not make request to auth server.", e);
/* 112:    */     }
/* 113: 99 */     if (result == null) {
/* 114:100 */       return null;
/* 115:    */     }
/* 116:101 */     if ((result.getError() != null) && (!result.getError().equals("")))
/* 117:    */     {
/* 118:102 */       if ((result.getCause() != null) && (result.getCause().equals("UserMigratedException"))) {
/* 119:103 */         throw new UserMigratedException(result.getErrorMessage());
/* 120:    */       }
/* 121:104 */       if (result.getError().equals("ForbiddenOperationException")) {
/* 122:105 */         throw new InvalidCredentialsException(result.getErrorMessage());
/* 123:    */       }
/* 124:107 */       throw new AuthenticationException(result.getErrorMessage());
/* 125:    */     }
/* 126:110 */     return result;
/* 127:    */   }
/* 128:    */   
/* 129:    */   private static HttpURLConnection createUrlConnection(URL url)
/* 130:    */     throws IOException
/* 131:    */   {
/* 132:115 */     if (url == null) {
/* 133:116 */       throw new IllegalArgumentException("URL cannot be null.");
/* 134:    */     }
/* 135:119 */     HttpURLConnection connection = (HttpURLConnection)url.openConnection();
/* 136:120 */     connection.setConnectTimeout(15000);
/* 137:121 */     connection.setReadTimeout(15000);
/* 138:122 */     connection.setUseCaches(false);
/* 139:123 */     return connection;
/* 140:    */   }
/* 141:    */   
/* 142:    */   private static String performPostRequest(URL url, String post, String type)
/* 143:    */     throws IOException
/* 144:    */   {
/* 145:127 */     if (url == null) {
/* 146:128 */       throw new IllegalArgumentException("URL cannot be null.");
/* 147:    */     }
/* 148:131 */     if (post == null) {
/* 149:132 */       throw new IllegalArgumentException("Post cannot be null.");
/* 150:    */     }
/* 151:135 */     if (type == null) {
/* 152:136 */       throw new IllegalArgumentException("Type cannot be null.");
/* 153:    */     }
/* 154:139 */     HttpURLConnection connection = createUrlConnection(url);
/* 155:140 */     byte[] bytes = post.getBytes("UTF-8");
/* 156:141 */     connection.setRequestProperty("Content-Type", type + "; charset=utf-8");
/* 157:142 */     connection.setRequestProperty("Content-Length", bytes.length);
/* 158:143 */     connection.setDoOutput(true);
/* 159:144 */     OutputStream outputStream = null;
/* 160:    */     try
/* 161:    */     {
/* 162:146 */       outputStream = connection.getOutputStream();
/* 163:147 */       outputStream.write(bytes);
/* 164:    */     }
/* 165:    */     finally
/* 166:    */     {
/* 167:149 */       IOUtils.closeQuietly(outputStream);
/* 168:    */     }
/* 169:152 */     InputStream inputStream = null;
/* 170:    */     try
/* 171:    */     {
/* 172:154 */       inputStream = connection.getInputStream();
/* 173:155 */       return IOUtils.toString(inputStream, "UTF-8");
/* 174:    */     }
/* 175:    */     catch (IOException e)
/* 176:    */     {
/* 177:    */       String str;
/* 178:157 */       IOUtils.closeQuietly(inputStream);
/* 179:158 */       inputStream = connection.getErrorStream();
/* 180:159 */       if (inputStream == null) {
/* 181:160 */         throw e;
/* 182:    */       }
/* 183:163 */       return IOUtils.toString(inputStream, "UTF-8");
/* 184:    */     }
/* 185:    */     finally
/* 186:    */     {
/* 187:165 */       IOUtils.closeQuietly(inputStream);
/* 188:    */     }
/* 189:    */   }
/* 190:    */   
/* 191:    */   private static String performGetRequest(URL url)
/* 192:    */     throws IOException
/* 193:    */   {
/* 194:170 */     if (url == null) {
/* 195:171 */       throw new IllegalArgumentException("URL cannot be null.");
/* 196:    */     }
/* 197:174 */     HttpURLConnection connection = createUrlConnection(url);
/* 198:175 */     InputStream inputStream = null;
/* 199:    */     try
/* 200:    */     {
/* 201:177 */       inputStream = connection.getInputStream();
/* 202:178 */       return IOUtils.toString(inputStream, "UTF-8");
/* 203:    */     }
/* 204:    */     catch (IOException e)
/* 205:    */     {
/* 206:    */       String str;
/* 207:180 */       IOUtils.closeQuietly(inputStream);
/* 208:181 */       inputStream = connection.getErrorStream();
/* 209:182 */       if (inputStream == null) {
/* 210:183 */         throw e;
/* 211:    */       }
/* 212:186 */       return IOUtils.toString(inputStream, "UTF-8");
/* 213:    */     }
/* 214:    */     finally
/* 215:    */     {
/* 216:188 */       IOUtils.closeQuietly(inputStream);
/* 217:    */     }
/* 218:    */   }
/* 219:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.util.URLUtils
 * JD-Core Version:    0.7.0.1
 */