/*  1:   */ package org.spacehq.opennbt.conversion.builtin.custom;
/*  2:   */ 
/*  3:   */ import java.io.Serializable;
/*  4:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  5:   */ import org.spacehq.opennbt.tag.builtin.custom.SerializableTag;
/*  6:   */ 
/*  7:   */ public class SerializableTagConverter
/*  8:   */   implements TagConverter<SerializableTag, Serializable>
/*  9:   */ {
/* 10:   */   public Serializable convert(SerializableTag tag)
/* 11:   */   {
/* 12:15 */     return tag.getValue();
/* 13:   */   }
/* 14:   */   
/* 15:   */   public SerializableTag convert(String name, Serializable value)
/* 16:   */   {
/* 17:20 */     return new SerializableTag(name, value);
/* 18:   */   }
/* 19:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.custom.SerializableTagConverter
 * JD-Core Version:    0.7.0.1
 */