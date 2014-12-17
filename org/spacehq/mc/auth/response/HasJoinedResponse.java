/*  1:   */ package org.spacehq.mc.auth.response;
/*  2:   */ 
/*  3:   */ import java.util.UUID;
/*  4:   */ import org.spacehq.mc.auth.properties.PropertyMap;
/*  5:   */ 
/*  6:   */ public class HasJoinedResponse
/*  7:   */   extends Response
/*  8:   */ {
/*  9:   */   private UUID id;
/* 10:   */   private PropertyMap properties;
/* 11:   */   
/* 12:   */   public UUID getId()
/* 13:   */   {
/* 14:13 */     return this.id;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public PropertyMap getProperties()
/* 18:   */   {
/* 19:17 */     return this.properties;
/* 20:   */   }
/* 21:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.response.HasJoinedResponse
 * JD-Core Version:    0.7.0.1
 */