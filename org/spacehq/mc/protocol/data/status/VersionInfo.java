/*  1:   */ package org.spacehq.mc.protocol.data.status;
/*  2:   */ 
/*  3:   */ public class VersionInfo
/*  4:   */ {
/*  5:   */   private String name;
/*  6:   */   private int protocol;
/*  7:   */   
/*  8:   */   public VersionInfo(String name, int protocol)
/*  9:   */   {
/* 10: 9 */     this.name = name;
/* 11:10 */     this.protocol = protocol;
/* 12:   */   }
/* 13:   */   
/* 14:   */   public String getVersionName()
/* 15:   */   {
/* 16:14 */     return this.name;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public int getProtocolVersion()
/* 20:   */   {
/* 21:18 */     return this.protocol;
/* 22:   */   }
/* 23:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.status.VersionInfo
 * JD-Core Version:    0.7.0.1
 */