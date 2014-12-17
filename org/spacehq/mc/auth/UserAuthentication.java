/*   1:    */ package org.spacehq.mc.auth;
/*   2:    */ 
/*   3:    */ import java.net.URL;
/*   4:    */ import java.util.ArrayList;
/*   5:    */ import java.util.Arrays;
/*   6:    */ import java.util.HashMap;
/*   7:    */ import java.util.List;
/*   8:    */ import java.util.Map;
/*   9:    */ import org.spacehq.mc.auth.exception.AuthenticationException;
/*  10:    */ import org.spacehq.mc.auth.exception.InvalidCredentialsException;
/*  11:    */ import org.spacehq.mc.auth.exception.PropertyDeserializeException;
/*  12:    */ import org.spacehq.mc.auth.properties.Property;
/*  13:    */ import org.spacehq.mc.auth.properties.PropertyMap;
/*  14:    */ import org.spacehq.mc.auth.request.AuthenticationRequest;
/*  15:    */ import org.spacehq.mc.auth.request.RefreshRequest;
/*  16:    */ import org.spacehq.mc.auth.response.AuthenticationResponse;
/*  17:    */ import org.spacehq.mc.auth.response.RefreshResponse;
/*  18:    */ import org.spacehq.mc.auth.response.User;
/*  19:    */ import org.spacehq.mc.auth.serialize.UUIDSerializer;
/*  20:    */ import org.spacehq.mc.auth.util.URLUtils;
/*  21:    */ 
/*  22:    */ public class UserAuthentication
/*  23:    */ {
/*  24:    */   private static final String BASE_URL = "https://authserver.mojang.com/";
/*  25: 26 */   private static final URL ROUTE_AUTHENTICATE = URLUtils.constantURL("https://authserver.mojang.com/authenticate");
/*  26: 27 */   private static final URL ROUTE_REFRESH = URLUtils.constantURL("https://authserver.mojang.com/refresh");
/*  27:    */   private static final String STORAGE_KEY_PROFILE_NAME = "displayName";
/*  28:    */   private static final String STORAGE_KEY_PROFILE_ID = "uuid";
/*  29:    */   private static final String STORAGE_KEY_PROFILE_PROPERTIES = "profileProperties";
/*  30:    */   private static final String STORAGE_KEY_USER_NAME = "username";
/*  31:    */   private static final String STORAGE_KEY_USER_ID = "userid";
/*  32:    */   private static final String STORAGE_KEY_USER_PROPERTIES = "userProperties";
/*  33:    */   private static final String STORAGE_KEY_ACCESS_TOKEN = "accessToken";
/*  34:    */   private String clientToken;
/*  35: 37 */   private PropertyMap userProperties = new PropertyMap();
/*  36:    */   private String userId;
/*  37:    */   private String username;
/*  38:    */   private String password;
/*  39:    */   private String accessToken;
/*  40:    */   private boolean isOnline;
/*  41: 43 */   private List<GameProfile> profiles = new ArrayList();
/*  42:    */   private GameProfile selectedProfile;
/*  43:    */   private UserType userType;
/*  44:    */   
/*  45:    */   public UserAuthentication(String clientToken)
/*  46:    */   {
/*  47: 48 */     if (clientToken == null) {
/*  48: 49 */       throw new IllegalArgumentException("ClientToken cannot be null.");
/*  49:    */     }
/*  50: 52 */     this.clientToken = clientToken;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public String getClientToken()
/*  54:    */   {
/*  55: 56 */     return this.clientToken;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public String getUserID()
/*  59:    */   {
/*  60: 60 */     return this.userId;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public String getAccessToken()
/*  64:    */   {
/*  65: 64 */     return this.accessToken;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public List<GameProfile> getAvailableProfiles()
/*  69:    */   {
/*  70: 68 */     return this.profiles;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public GameProfile getSelectedProfile()
/*  74:    */   {
/*  75: 72 */     return this.selectedProfile;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public UserType getUserType()
/*  79:    */   {
/*  80: 76 */     return isLoggedIn() ? this.userType : this.userType == null ? UserType.LEGACY : null;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public PropertyMap getUserProperties()
/*  84:    */   {
/*  85: 80 */     return isLoggedIn() ? new PropertyMap(this.userProperties) : new PropertyMap();
/*  86:    */   }
/*  87:    */   
/*  88:    */   public boolean isLoggedIn()
/*  89:    */   {
/*  90: 84 */     return (this.accessToken != null) && (!this.accessToken.equals(""));
/*  91:    */   }
/*  92:    */   
/*  93:    */   public boolean canPlayOnline()
/*  94:    */   {
/*  95: 88 */     return (isLoggedIn()) && (getSelectedProfile() != null) && (this.isOnline);
/*  96:    */   }
/*  97:    */   
/*  98:    */   public boolean canLogIn()
/*  99:    */   {
/* 100: 92 */     return (!canPlayOnline()) && (this.username != null) && (!this.username.equals("")) && (((this.password != null) && (!this.password.equals(""))) || ((this.accessToken != null) && (!this.accessToken.equals(""))));
/* 101:    */   }
/* 102:    */   
/* 103:    */   public void setUsername(String username)
/* 104:    */   {
/* 105: 96 */     if ((isLoggedIn()) && (canPlayOnline())) {
/* 106: 97 */       throw new IllegalStateException("Cannot change username whilst logged in & online");
/* 107:    */     }
/* 108: 99 */     this.username = username;
/* 109:    */   }
/* 110:    */   
/* 111:    */   public void setPassword(String password)
/* 112:    */   {
/* 113:104 */     if ((isLoggedIn()) && (canPlayOnline()) && (this.password != null) && (!this.password.equals(""))) {
/* 114:105 */       throw new IllegalStateException("Cannot set password whilst logged in & online");
/* 115:    */     }
/* 116:107 */     this.password = password;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public void setAccessToken(String accessToken)
/* 120:    */   {
/* 121:112 */     if ((isLoggedIn()) && (canPlayOnline())) {
/* 122:113 */       throw new IllegalStateException("Cannot change accessToken whilst logged in & online");
/* 123:    */     }
/* 124:115 */     this.accessToken = accessToken;
/* 125:    */   }
/* 126:    */   
/* 127:    */   public void loadFromStorage(Map<String, Object> credentials)
/* 128:    */     throws PropertyDeserializeException
/* 129:    */   {
/* 130:120 */     logout();
/* 131:121 */     setUsername((String)credentials.get("username"));
/* 132:122 */     if (credentials.containsKey("userid")) {
/* 133:123 */       this.userId = ((String)credentials.get("userid"));
/* 134:    */     } else {
/* 135:125 */       this.userId = this.username;
/* 136:    */     }
/* 137:    */     String name;
/* 138:128 */     if (credentials.containsKey("userProperties")) {
/* 139:    */       try
/* 140:    */       {
/* 141:130 */         List<Map<String, String>> list = (List)credentials.get("userProperties");
/* 142:131 */         for (Map<String, String> propertyMap : list)
/* 143:    */         {
/* 144:132 */           name = (String)propertyMap.get("name");
/* 145:133 */           String value = (String)propertyMap.get("value");
/* 146:134 */           String signature = (String)propertyMap.get("signature");
/* 147:135 */           if (signature == null) {
/* 148:136 */             this.userProperties.put(name, new Property(name, value));
/* 149:    */           } else {
/* 150:138 */             this.userProperties.put(name, new Property(name, value, signature));
/* 151:    */           }
/* 152:    */         }
/* 153:    */       }
/* 154:    */       catch (Throwable t)
/* 155:    */       {
/* 156:142 */         throw new PropertyDeserializeException("Couldn't deserialize user properties", t);
/* 157:    */       }
/* 158:    */     }
/* 159:146 */     if ((credentials.containsKey("displayName")) && (credentials.containsKey("uuid")))
/* 160:    */     {
/* 161:147 */       GameProfile profile = new GameProfile(UUIDSerializer.fromString((String)credentials.get("uuid")), (String)credentials.get("displayName"));
/* 162:148 */       if (credentials.containsKey("profileProperties")) {
/* 163:    */         try
/* 164:    */         {
/* 165:150 */           List<Map<String, String>> list = (List)credentials.get("profileProperties");
/* 166:151 */           for (Object propertyMap : list)
/* 167:    */           {
/* 168:152 */             String name = (String)((Map)propertyMap).get("name");
/* 169:153 */             String value = (String)((Map)propertyMap).get("value");
/* 170:154 */             String signature = (String)((Map)propertyMap).get("signature");
/* 171:155 */             if (signature == null) {
/* 172:156 */               profile.getProperties().put(name, new Property(name, value));
/* 173:    */             } else {
/* 174:158 */               profile.getProperties().put(name, new Property(name, value, signature));
/* 175:    */             }
/* 176:    */           }
/* 177:    */         }
/* 178:    */         catch (Throwable t)
/* 179:    */         {
/* 180:162 */           throw new PropertyDeserializeException("Couldn't deserialize profile properties", t);
/* 181:    */         }
/* 182:    */       }
/* 183:166 */       this.selectedProfile = profile;
/* 184:    */     }
/* 185:169 */     this.accessToken = ((String)credentials.get("accessToken"));
/* 186:    */   }
/* 187:    */   
/* 188:    */   public Map<String, Object> saveForStorage()
/* 189:    */   {
/* 190:173 */     Map<String, Object> result = new HashMap();
/* 191:174 */     if (this.username != null) {
/* 192:175 */       result.put("username", this.username);
/* 193:    */     }
/* 194:178 */     if (getUserID() != null) {
/* 195:179 */       result.put("userid", this.userId);
/* 196:    */     }
/* 197:    */     Map<String, String> property;
/* 198:182 */     if (!getUserProperties().isEmpty())
/* 199:    */     {
/* 200:183 */       List<Map<String, String>> properties = new ArrayList();
/* 201:184 */       for (Property userProperty : getUserProperties().values())
/* 202:    */       {
/* 203:185 */         property = new HashMap();
/* 204:186 */         property.put("name", userProperty.getName());
/* 205:187 */         property.put("value", userProperty.getValue());
/* 206:188 */         property.put("signature", userProperty.getSignature());
/* 207:189 */         properties.add(property);
/* 208:    */       }
/* 209:192 */       result.put("userProperties", properties);
/* 210:    */     }
/* 211:195 */     GameProfile selectedProfile = getSelectedProfile();
/* 212:196 */     if (selectedProfile != null)
/* 213:    */     {
/* 214:197 */       result.put("displayName", selectedProfile.getName());
/* 215:198 */       result.put("uuid", selectedProfile.getId());
/* 216:199 */       List<Map<String, String>> properties = new ArrayList();
/* 217:200 */       for (Property profileProperty : selectedProfile.getProperties().values())
/* 218:    */       {
/* 219:201 */         Map<String, String> property = new HashMap();
/* 220:202 */         property.put("name", profileProperty.getName());
/* 221:203 */         property.put("value", profileProperty.getValue());
/* 222:204 */         property.put("signature", profileProperty.getSignature());
/* 223:205 */         properties.add(property);
/* 224:    */       }
/* 225:208 */       if (!properties.isEmpty()) {
/* 226:209 */         result.put("profileProperties", properties);
/* 227:    */       }
/* 228:    */     }
/* 229:213 */     if ((this.accessToken != null) && (!this.accessToken.equals(""))) {
/* 230:214 */       result.put("accessToken", this.accessToken);
/* 231:    */     }
/* 232:217 */     return result;
/* 233:    */   }
/* 234:    */   
/* 235:    */   public void login()
/* 236:    */     throws AuthenticationException
/* 237:    */   {
/* 238:221 */     if ((this.username == null) || (this.username.equals(""))) {
/* 239:222 */       throw new InvalidCredentialsException("Invalid username");
/* 240:    */     }
/* 241:224 */     if ((this.accessToken != null) && (!this.accessToken.equals("")))
/* 242:    */     {
/* 243:225 */       loginWithToken();
/* 244:    */     }
/* 245:    */     else
/* 246:    */     {
/* 247:227 */       if ((this.password == null) || (this.password.equals(""))) {
/* 248:228 */         throw new InvalidCredentialsException("Invalid password");
/* 249:    */       }
/* 250:231 */       loginWithPassword();
/* 251:    */     }
/* 252:    */   }
/* 253:    */   
/* 254:    */   private void loginWithPassword()
/* 255:    */     throws AuthenticationException
/* 256:    */   {
/* 257:237 */     if ((this.username == null) || (this.username.equals(""))) {
/* 258:238 */       throw new InvalidCredentialsException("Invalid username");
/* 259:    */     }
/* 260:239 */     if ((this.password == null) || (this.password.equals(""))) {
/* 261:240 */       throw new InvalidCredentialsException("Invalid password");
/* 262:    */     }
/* 263:242 */     AuthenticationRequest request = new AuthenticationRequest(this, this.username, this.password);
/* 264:243 */     AuthenticationResponse response = (AuthenticationResponse)URLUtils.makeRequest(ROUTE_AUTHENTICATE, request, AuthenticationResponse.class);
/* 265:244 */     if (!response.getClientToken().equals(getClientToken())) {
/* 266:245 */       throw new AuthenticationException("Server requested we change our client token. Don't know how to handle this!");
/* 267:    */     }
/* 268:247 */     if (response.getSelectedProfile() != null) {
/* 269:248 */       this.userType = (response.getSelectedProfile().isLegacy() ? UserType.LEGACY : UserType.MOJANG);
/* 270:249 */     } else if ((response.getAvailableProfiles() != null) && (response.getAvailableProfiles().length != 0)) {
/* 271:250 */       this.userType = (response.getAvailableProfiles()[0].isLegacy() ? UserType.LEGACY : UserType.MOJANG);
/* 272:    */     }
/* 273:253 */     if ((response.getUser() != null) && (response.getUser().getId() != null)) {
/* 274:254 */       this.userId = response.getUser().getId();
/* 275:    */     } else {
/* 276:256 */       this.userId = this.username;
/* 277:    */     }
/* 278:259 */     this.isOnline = true;
/* 279:260 */     this.accessToken = response.getAccessToken();
/* 280:261 */     this.profiles = Arrays.asList(response.getAvailableProfiles());
/* 281:262 */     this.selectedProfile = response.getSelectedProfile();
/* 282:263 */     updateProperties(response.getUser());
/* 283:    */   }
/* 284:    */   
/* 285:    */   private void loginWithToken()
/* 286:    */     throws AuthenticationException
/* 287:    */   {
/* 288:269 */     if ((this.userId == null) || (this.userId.equals("")))
/* 289:    */     {
/* 290:270 */       if ((this.username == null) || (this.username.equals(""))) {
/* 291:271 */         throw new InvalidCredentialsException("Invalid uuid & username");
/* 292:    */       }
/* 293:274 */       this.userId = this.username;
/* 294:    */     }
/* 295:277 */     if ((this.accessToken == null) || (this.accessToken.equals(""))) {
/* 296:278 */       throw new InvalidCredentialsException("Invalid access token");
/* 297:    */     }
/* 298:280 */     RefreshRequest request = new RefreshRequest(this);
/* 299:281 */     RefreshResponse response = (RefreshResponse)URLUtils.makeRequest(ROUTE_REFRESH, request, RefreshResponse.class);
/* 300:282 */     if (!response.getClientToken().equals(getClientToken())) {
/* 301:283 */       throw new AuthenticationException("Server requested we change our client token. Don't know how to handle this!");
/* 302:    */     }
/* 303:285 */     if (response.getSelectedProfile() != null) {
/* 304:286 */       this.userType = (response.getSelectedProfile().isLegacy() ? UserType.LEGACY : UserType.MOJANG);
/* 305:287 */     } else if ((response.getAvailableProfiles() != null) && (response.getAvailableProfiles().length != 0)) {
/* 306:288 */       this.userType = (response.getAvailableProfiles()[0].isLegacy() ? UserType.LEGACY : UserType.MOJANG);
/* 307:    */     }
/* 308:291 */     if ((response.getUser() != null) && (response.getUser().getId() != null)) {
/* 309:292 */       this.userId = response.getUser().getId();
/* 310:    */     } else {
/* 311:294 */       this.userId = this.username;
/* 312:    */     }
/* 313:297 */     this.isOnline = true;
/* 314:298 */     this.accessToken = response.getAccessToken();
/* 315:299 */     this.profiles = Arrays.asList(response.getAvailableProfiles());
/* 316:300 */     this.selectedProfile = response.getSelectedProfile();
/* 317:301 */     updateProperties(response.getUser());
/* 318:    */   }
/* 319:    */   
/* 320:    */   public void logout()
/* 321:    */   {
/* 322:307 */     this.password = null;
/* 323:308 */     this.userId = null;
/* 324:309 */     this.selectedProfile = null;
/* 325:310 */     this.userProperties.clear();
/* 326:311 */     this.accessToken = null;
/* 327:312 */     this.profiles = null;
/* 328:313 */     this.isOnline = false;
/* 329:314 */     this.userType = null;
/* 330:    */   }
/* 331:    */   
/* 332:    */   public void selectGameProfile(GameProfile profile)
/* 333:    */     throws AuthenticationException
/* 334:    */   {
/* 335:318 */     if (!isLoggedIn()) {
/* 336:319 */       throw new AuthenticationException("Cannot change game profile whilst not logged in");
/* 337:    */     }
/* 338:320 */     if (getSelectedProfile() != null) {
/* 339:321 */       throw new AuthenticationException("Cannot change game profile. You must log out and back in.");
/* 340:    */     }
/* 341:322 */     if ((profile != null) && (this.profiles.contains(profile)))
/* 342:    */     {
/* 343:323 */       RefreshRequest request = new RefreshRequest(this, profile);
/* 344:324 */       RefreshResponse response = (RefreshResponse)URLUtils.makeRequest(ROUTE_REFRESH, request, RefreshResponse.class);
/* 345:325 */       if (!response.getClientToken().equals(getClientToken())) {
/* 346:326 */         throw new AuthenticationException("Server requested we change our client token. Don't know how to handle this!");
/* 347:    */       }
/* 348:328 */       this.isOnline = true;
/* 349:329 */       this.accessToken = response.getAccessToken();
/* 350:330 */       this.selectedProfile = response.getSelectedProfile();
/* 351:    */     }
/* 352:    */     else
/* 353:    */     {
/* 354:333 */       throw new IllegalArgumentException("Invalid profile '" + profile + "'");
/* 355:    */     }
/* 356:    */   }
/* 357:    */   
/* 358:    */   public String toString()
/* 359:    */   {
/* 360:339 */     return "UserAuthentication{profiles=" + this.profiles + ", selectedProfile=" + getSelectedProfile() + ", username=" + this.username + ", isLoggedIn=" + isLoggedIn() + ", canPlayOnline=" + canPlayOnline() + ", accessToken=" + this.accessToken + ", clientToken=" + getClientToken() + "}";
/* 361:    */   }
/* 362:    */   
/* 363:    */   private void updateProperties(User user)
/* 364:    */   {
/* 365:343 */     this.userProperties.clear();
/* 366:344 */     if ((user != null) && (user.getProperties() != null)) {
/* 367:345 */       this.userProperties.putAll(user.getProperties());
/* 368:    */     }
/* 369:    */   }
/* 370:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.UserAuthentication
 * JD-Core Version:    0.7.0.1
 */