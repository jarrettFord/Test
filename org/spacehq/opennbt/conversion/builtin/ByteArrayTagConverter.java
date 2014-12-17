/*  1:   */ package org.spacehq.opennbt.conversion.builtin;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.ByteArrayTag;
/*  5:   */ 
/*  6:   */ public class ByteArrayTagConverter
/*  7:   */   implements TagConverter<ByteArrayTag, byte[]>
/*  8:   */ {
/*  9:   */   public byte[] convert(ByteArrayTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public ByteArrayTag convert(String name, byte[] value)
/* 15:   */   {
/* 16:18 */     return new ByteArrayTag(name, value);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.ByteArrayTagConverter
 * JD-Core Version:    0.7.0.1
 */