/*  1:   */ package org.spacehq.opennbt.conversion.builtin.custom;
/*  2:   */ 
/*  3:   */ import java.io.Serializable;
/*  4:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  5:   */ import org.spacehq.opennbt.tag.builtin.custom.SerializableArrayTag;
/*  6:   */ 
/*  7:   */ public class SerializableArrayTagConverter
/*  8:   */   implements TagConverter<SerializableArrayTag, Serializable[]>
/*  9:   */ {
/* 10:   */   public Serializable[] convert(SerializableArrayTag tag)
/* 11:   */   {
/* 12:15 */     return tag.getValue();
/* 13:   */   }
/* 14:   */   
/* 15:   */   public SerializableArrayTag convert(String name, Serializable[] value)
/* 16:   */   {
/* 17:20 */     return new SerializableArrayTag(name, value);
/* 18:   */   }
/* 19:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.custom.SerializableArrayTagConverter
 * JD-Core Version:    0.7.0.1
 */