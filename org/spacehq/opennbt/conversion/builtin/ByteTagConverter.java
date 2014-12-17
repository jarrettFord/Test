/*  1:   */ package org.spacehq.opennbt.conversion.builtin;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.ByteTag;
/*  5:   */ 
/*  6:   */ public class ByteTagConverter
/*  7:   */   implements TagConverter<ByteTag, Byte>
/*  8:   */ {
/*  9:   */   public Byte convert(ByteTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public ByteTag convert(String name, Byte value)
/* 15:   */   {
/* 16:18 */     return new ByteTag(name, value.byteValue());
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.ByteTagConverter
 * JD-Core Version:    0.7.0.1
 */