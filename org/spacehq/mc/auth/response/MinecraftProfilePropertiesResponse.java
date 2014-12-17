/*  1:   */ package org.spacehq.mc.auth.response;
/*  2:   */ 
/*  3:   */ import java.util.UUID;
/*  4:   */ import org.spacehq.mc.auth.properties.PropertyMap;
/*  5:   */ 
/*  6:   */ public class MinecraftProfilePropertiesResponse
/*  7:   */   extends Response
/*  8:   */ {
/*  9:   */   private UUID id;
/* 10:   */   private String name;
/* 11:   */   private PropertyMap properties;
/* 12:   */   
/* 13:   */   public UUID getId()
/* 14:   */   {
/* 15:14 */     return this.id;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public String getName()
/* 19:   */   {
/* 20:18 */     return this.name;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public PropertyMap getProperties()
/* 24:   */   {
/* 25:22 */     return this.properties;
/* 26:   */   }
/* 27:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.response.MinecraftProfilePropertiesResponse
 * JD-Core Version:    0.7.0.1
 */