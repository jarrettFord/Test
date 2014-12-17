/*  1:   */ package org.spacehq.mc.auth;
/*  2:   */ 
/*  3:   */ import java.util.UUID;
/*  4:   */ import org.spacehq.mc.auth.properties.PropertyMap;
/*  5:   */ 
/*  6:   */ public class GameProfile
/*  7:   */ {
/*  8:   */   private UUID id;
/*  9:   */   private String name;
/* 10:11 */   private PropertyMap properties = new PropertyMap();
/* 11:   */   private boolean legacy;
/* 12:   */   
/* 13:   */   public GameProfile(String id, String name)
/* 14:   */   {
/* 15:15 */     this((id == null) || (id.equals("")) ? null : UUID.fromString(id), name);
/* 16:   */   }
/* 17:   */   
/* 18:   */   public GameProfile(UUID id, String name)
/* 19:   */   {
/* 20:19 */     if ((id == null) && ((name == null) || (name.equals("")))) {
/* 21:20 */       throw new IllegalArgumentException("Name and ID cannot both be blank");
/* 22:   */     }
/* 23:22 */     this.id = id;
/* 24:23 */     this.name = name;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public UUID getId()
/* 28:   */   {
/* 29:28 */     return this.id;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public String getIdAsString()
/* 33:   */   {
/* 34:32 */     return this.id != null ? this.id.toString() : "";
/* 35:   */   }
/* 36:   */   
/* 37:   */   public String getName()
/* 38:   */   {
/* 39:36 */     return this.name;
/* 40:   */   }
/* 41:   */   
/* 42:   */   public PropertyMap getProperties()
/* 43:   */   {
/* 44:40 */     return this.properties;
/* 45:   */   }
/* 46:   */   
/* 47:   */   public boolean isLegacy()
/* 48:   */   {
/* 49:44 */     return this.legacy;
/* 50:   */   }
/* 51:   */   
/* 52:   */   public boolean isComplete()
/* 53:   */   {
/* 54:48 */     return (this.id != null) && (this.name != null) && (!this.name.equals(""));
/* 55:   */   }
/* 56:   */   
/* 57:   */   public boolean equals(Object o)
/* 58:   */   {
/* 59:53 */     if (this == o) {
/* 60:54 */       return true;
/* 61:   */     }
/* 62:55 */     if ((o != null) && (getClass() == o.getClass()))
/* 63:   */     {
/* 64:56 */       GameProfile that = (GameProfile)o;
/* 65:57 */       return (this.id != null ? this.id.equals(that.id) : that.id == null) && (this.name != null ? this.name.equals(that.name) : that.name == null);
/* 66:   */     }
/* 67:59 */     return false;
/* 68:   */   }
/* 69:   */   
/* 70:   */   public int hashCode()
/* 71:   */   {
/* 72:65 */     int result = this.id != null ? this.id.hashCode() : 0;
/* 73:66 */     result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
/* 74:67 */     return result;
/* 75:   */   }
/* 76:   */   
/* 77:   */   public String toString()
/* 78:   */   {
/* 79:72 */     return "GameProfile{id=" + this.id + ", name=" + this.name + ", properties=" + this.properties + ", legacy=" + this.legacy + "}";
/* 80:   */   }
/* 81:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.GameProfile
 * JD-Core Version:    0.7.0.1
 */