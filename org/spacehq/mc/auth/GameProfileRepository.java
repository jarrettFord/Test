/*  1:   */ package org.spacehq.mc.auth;
/*  2:   */ 
/*  3:   */ import java.util.ArrayList;
/*  4:   */ import java.util.HashSet;
/*  5:   */ import java.util.Iterator;
/*  6:   */ import java.util.List;
/*  7:   */ import java.util.Set;
/*  8:   */ import org.spacehq.mc.auth.exception.AuthenticationException;
/*  9:   */ import org.spacehq.mc.auth.exception.ProfileNotFoundException;
/* 10:   */ import org.spacehq.mc.auth.response.ProfileSearchResultsResponse;
/* 11:   */ import org.spacehq.mc.auth.util.URLUtils;
/* 12:   */ 
/* 13:   */ public class GameProfileRepository
/* 14:   */ {
/* 15:   */   private static final String BASE_URL = "https://api.mojang.com/";
/* 16:   */   private static final String SEARCH_URL = "https://api.mojang.com/profiles/minecraft";
/* 17:   */   private static final int MAX_FAIL_COUNT = 3;
/* 18:   */   private static final int DELAY_BETWEEN_PAGES = 100;
/* 19:   */   private static final int DELAY_BETWEEN_FAILURES = 750;
/* 20:   */   private static final int PROFILES_PER_REQUEST = 100;
/* 21:   */   
/* 22:   */   public void findProfilesByNames(String[] names, ProfileLookupCallback callback)
/* 23:   */   {
/* 24:24 */     Set<String> criteria = new HashSet();
/* 25:25 */     for (String name : names) {
/* 26:26 */       if ((name != null) && (!name.isEmpty())) {
/* 27:27 */         criteria.add(name.toLowerCase());
/* 28:   */       }
/* 29:   */     }
/* 30:   */     int failCount;
/* 31:   */     boolean tryAgain;
/* 32:   */     label366:
/* 33:31 */     for (Iterator localIterator1 = partition(criteria, 100).iterator(); localIterator1.hasNext(); tryAgain)
/* 34:   */     {
/* 35:31 */       Set<String> request = (Set)localIterator1.next();
/* 36:32 */       Object error = null;
/* 37:33 */       failCount = 0;
/* 38:34 */       tryAgain = true;
/* 39:35 */       continue;
/* 40:36 */       tryAgain = false;
/* 41:   */       try
/* 42:   */       {
/* 43:38 */         ProfileSearchResultsResponse response = (ProfileSearchResultsResponse)URLUtils.makeRequest(URLUtils.constantURL("https://api.mojang.com/profiles/minecraft"), request, ProfileSearchResultsResponse.class);
/* 44:39 */         failCount = 0;
/* 45:40 */         error = null;
/* 46:41 */         Set<String> missing = new HashSet(request);
/* 47:42 */         for (GameProfile profile : response.getProfiles())
/* 48:   */         {
/* 49:43 */           missing.remove(profile.getName().toLowerCase());
/* 50:44 */           callback.onProfileLookupSucceeded(profile);
/* 51:   */         }
/* 52:47 */         for (String name : missing) {
/* 53:48 */           callback.onProfileLookupFailed(new GameProfile(null, name), new ProfileNotFoundException("Server could not find the requested profile."));
/* 54:   */         }
/* 55:   */         try
/* 56:   */         {
/* 57:52 */           Thread.sleep(100L);
/* 58:   */         }
/* 59:   */         catch (InterruptedException localInterruptedException) {}
/* 60:   */         String name;
/* 61:35 */         if (failCount >= 3) {
/* 62:   */           break label366;
/* 63:   */         }
/* 64:   */       }
/* 65:   */       catch (AuthenticationException e)
/* 66:   */       {
/* 67:56 */         error = e;
/* 68:57 */         failCount++;
/* 69:58 */         if (failCount >= 3)
/* 70:   */         {
/* 71:59 */           for (localInterruptedException = request.iterator(); localInterruptedException.hasNext();)
/* 72:   */           {
/* 73:59 */             name = (String)localInterruptedException.next();
/* 74:60 */             callback.onProfileLookupFailed(new GameProfile(null, name), (Exception)error);
/* 75:   */           }
/* 76:   */         }
/* 77:   */         else
/* 78:   */         {
/* 79:   */           try
/* 80:   */           {
/* 81:64 */             Thread.sleep(750L);
/* 82:   */           }
/* 83:   */           catch (InterruptedException localInterruptedException1) {}
/* 84:68 */           tryAgain = true;
/* 85:   */         }
/* 86:   */       }
/* 87:   */     }
/* 88:   */   }
/* 89:   */   
/* 90:   */   private static Set<Set<String>> partition(Set<String> set, int size)
/* 91:   */   {
/* 92:76 */     List<String> list = new ArrayList(set);
/* 93:77 */     Set<Set<String>> ret = new HashSet();
/* 94:78 */     for (int i = 0; i < list.size(); i += size)
/* 95:   */     {
/* 96:79 */       Set<String> s = new HashSet();
/* 97:80 */       s.addAll(list.subList(i, Math.min(i + size, list.size())));
/* 98:81 */       ret.add(s);
/* 99:   */     }
/* :0:84 */     return ret;
/* :1:   */   }
/* :2:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.GameProfileRepository
 * JD-Core Version:    0.7.0.1
 */