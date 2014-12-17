/*   1:    */ package org.spacehq.opennbt.tag;
/*   2:    */ 
/*   3:    */ import java.lang.reflect.Constructor;
/*   4:    */ import java.util.HashMap;
/*   5:    */ import java.util.Map;
/*   6:    */ import org.spacehq.opennbt.tag.builtin.ByteArrayTag;
/*   7:    */ import org.spacehq.opennbt.tag.builtin.ByteTag;
/*   8:    */ import org.spacehq.opennbt.tag.builtin.CompoundTag;
/*   9:    */ import org.spacehq.opennbt.tag.builtin.DoubleTag;
/*  10:    */ import org.spacehq.opennbt.tag.builtin.FloatTag;
/*  11:    */ import org.spacehq.opennbt.tag.builtin.IntArrayTag;
/*  12:    */ import org.spacehq.opennbt.tag.builtin.IntTag;
/*  13:    */ import org.spacehq.opennbt.tag.builtin.ListTag;
/*  14:    */ import org.spacehq.opennbt.tag.builtin.LongTag;
/*  15:    */ import org.spacehq.opennbt.tag.builtin.ShortTag;
/*  16:    */ import org.spacehq.opennbt.tag.builtin.StringTag;
/*  17:    */ import org.spacehq.opennbt.tag.builtin.Tag;
/*  18:    */ import org.spacehq.opennbt.tag.builtin.custom.DoubleArrayTag;
/*  19:    */ import org.spacehq.opennbt.tag.builtin.custom.FloatArrayTag;
/*  20:    */ import org.spacehq.opennbt.tag.builtin.custom.LongArrayTag;
/*  21:    */ import org.spacehq.opennbt.tag.builtin.custom.SerializableArrayTag;
/*  22:    */ import org.spacehq.opennbt.tag.builtin.custom.SerializableTag;
/*  23:    */ import org.spacehq.opennbt.tag.builtin.custom.ShortArrayTag;
/*  24:    */ import org.spacehq.opennbt.tag.builtin.custom.StringArrayTag;
/*  25:    */ 
/*  26:    */ public class TagRegistry
/*  27:    */ {
/*  28: 15 */   private static final Map<Integer, Class<? extends Tag>> idToTag = new HashMap();
/*  29: 16 */   private static final Map<Class<? extends Tag>, Integer> tagToId = new HashMap();
/*  30:    */   
/*  31:    */   static
/*  32:    */   {
/*  33: 19 */     register(1, ByteTag.class);
/*  34: 20 */     register(2, ShortTag.class);
/*  35: 21 */     register(3, IntTag.class);
/*  36: 22 */     register(4, LongTag.class);
/*  37: 23 */     register(5, FloatTag.class);
/*  38: 24 */     register(6, DoubleTag.class);
/*  39: 25 */     register(7, ByteArrayTag.class);
/*  40: 26 */     register(8, StringTag.class);
/*  41: 27 */     register(9, ListTag.class);
/*  42: 28 */     register(10, CompoundTag.class);
/*  43: 29 */     register(11, IntArrayTag.class);
/*  44:    */     
/*  45: 31 */     register(60, DoubleArrayTag.class);
/*  46: 32 */     register(61, FloatArrayTag.class);
/*  47: 33 */     register(62, LongArrayTag.class);
/*  48: 34 */     register(63, SerializableArrayTag.class);
/*  49: 35 */     register(64, SerializableTag.class);
/*  50: 36 */     register(65, ShortArrayTag.class);
/*  51: 37 */     register(66, StringArrayTag.class);
/*  52:    */   }
/*  53:    */   
/*  54:    */   public static void register(int id, Class<? extends Tag> tag)
/*  55:    */     throws TagRegisterException
/*  56:    */   {
/*  57: 47 */     if (idToTag.containsKey(Integer.valueOf(id))) {
/*  58: 48 */       throw new TagRegisterException("Tag ID \"" + id + "\" is already in use.");
/*  59:    */     }
/*  60: 51 */     if (tagToId.containsKey(tag)) {
/*  61: 52 */       throw new TagRegisterException("Tag \"" + tag.getSimpleName() + "\" is already registered.");
/*  62:    */     }
/*  63: 55 */     idToTag.put(Integer.valueOf(id), tag);
/*  64: 56 */     tagToId.put(tag, Integer.valueOf(id));
/*  65:    */   }
/*  66:    */   
/*  67:    */   public static Class<? extends Tag> getClassFor(int id)
/*  68:    */   {
/*  69: 66 */     if (!idToTag.containsKey(Integer.valueOf(id))) {
/*  70: 67 */       return null;
/*  71:    */     }
/*  72: 70 */     return (Class)idToTag.get(Integer.valueOf(id));
/*  73:    */   }
/*  74:    */   
/*  75:    */   public static int getIdFor(Class<? extends Tag> clazz)
/*  76:    */   {
/*  77: 80 */     if (!tagToId.containsKey(clazz)) {
/*  78: 81 */       return -1;
/*  79:    */     }
/*  80: 84 */     return ((Integer)tagToId.get(clazz)).intValue();
/*  81:    */   }
/*  82:    */   
/*  83:    */   public static Tag createInstance(int id, String tagName)
/*  84:    */     throws TagCreateException
/*  85:    */   {
/*  86: 96 */     Class<? extends Tag> clazz = (Class)idToTag.get(Integer.valueOf(id));
/*  87: 97 */     if (clazz == null) {
/*  88: 98 */       throw new TagCreateException("Could not find tag with ID \"" + id + "\".");
/*  89:    */     }
/*  90:    */     try
/*  91:    */     {
/*  92:102 */       Constructor<? extends Tag> constructor = clazz.getDeclaredConstructor(new Class[] { String.class });
/*  93:103 */       constructor.setAccessible(true);
/*  94:104 */       return (Tag)constructor.newInstance(new Object[] { tagName });
/*  95:    */     }
/*  96:    */     catch (Exception e)
/*  97:    */     {
/*  98:106 */       throw new TagCreateException("Failed to create instance of tag \"" + clazz.getSimpleName() + "\".", e);
/*  99:    */     }
/* 100:    */   }
/* 101:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.TagRegistry
 * JD-Core Version:    0.7.0.1
 */