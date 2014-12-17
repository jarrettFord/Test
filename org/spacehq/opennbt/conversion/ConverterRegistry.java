/*   1:    */ package org.spacehq.opennbt.conversion;
/*   2:    */ 
/*   3:    */ import java.io.Serializable;
/*   4:    */ import java.util.HashMap;
/*   5:    */ import java.util.HashSet;
/*   6:    */ import java.util.LinkedHashSet;
/*   7:    */ import java.util.List;
/*   8:    */ import java.util.Map;
/*   9:    */ import java.util.Set;
/*  10:    */ import org.spacehq.opennbt.conversion.builtin.ByteArrayTagConverter;
/*  11:    */ import org.spacehq.opennbt.conversion.builtin.ByteTagConverter;
/*  12:    */ import org.spacehq.opennbt.conversion.builtin.CompoundTagConverter;
/*  13:    */ import org.spacehq.opennbt.conversion.builtin.DoubleTagConverter;
/*  14:    */ import org.spacehq.opennbt.conversion.builtin.FloatTagConverter;
/*  15:    */ import org.spacehq.opennbt.conversion.builtin.IntArrayTagConverter;
/*  16:    */ import org.spacehq.opennbt.conversion.builtin.IntTagConverter;
/*  17:    */ import org.spacehq.opennbt.conversion.builtin.ListTagConverter;
/*  18:    */ import org.spacehq.opennbt.conversion.builtin.LongTagConverter;
/*  19:    */ import org.spacehq.opennbt.conversion.builtin.ShortTagConverter;
/*  20:    */ import org.spacehq.opennbt.conversion.builtin.StringTagConverter;
/*  21:    */ import org.spacehq.opennbt.conversion.builtin.custom.DoubleArrayTagConverter;
/*  22:    */ import org.spacehq.opennbt.conversion.builtin.custom.FloatArrayTagConverter;
/*  23:    */ import org.spacehq.opennbt.conversion.builtin.custom.LongArrayTagConverter;
/*  24:    */ import org.spacehq.opennbt.conversion.builtin.custom.SerializableArrayTagConverter;
/*  25:    */ import org.spacehq.opennbt.conversion.builtin.custom.SerializableTagConverter;
/*  26:    */ import org.spacehq.opennbt.conversion.builtin.custom.ShortArrayTagConverter;
/*  27:    */ import org.spacehq.opennbt.conversion.builtin.custom.StringArrayTagConverter;
/*  28:    */ import org.spacehq.opennbt.tag.TagRegisterException;
/*  29:    */ import org.spacehq.opennbt.tag.builtin.ByteArrayTag;
/*  30:    */ import org.spacehq.opennbt.tag.builtin.ByteTag;
/*  31:    */ import org.spacehq.opennbt.tag.builtin.CompoundTag;
/*  32:    */ import org.spacehq.opennbt.tag.builtin.DoubleTag;
/*  33:    */ import org.spacehq.opennbt.tag.builtin.FloatTag;
/*  34:    */ import org.spacehq.opennbt.tag.builtin.IntArrayTag;
/*  35:    */ import org.spacehq.opennbt.tag.builtin.IntTag;
/*  36:    */ import org.spacehq.opennbt.tag.builtin.ListTag;
/*  37:    */ import org.spacehq.opennbt.tag.builtin.LongTag;
/*  38:    */ import org.spacehq.opennbt.tag.builtin.ShortTag;
/*  39:    */ import org.spacehq.opennbt.tag.builtin.StringTag;
/*  40:    */ import org.spacehq.opennbt.tag.builtin.Tag;
/*  41:    */ import org.spacehq.opennbt.tag.builtin.custom.DoubleArrayTag;
/*  42:    */ import org.spacehq.opennbt.tag.builtin.custom.FloatArrayTag;
/*  43:    */ import org.spacehq.opennbt.tag.builtin.custom.LongArrayTag;
/*  44:    */ import org.spacehq.opennbt.tag.builtin.custom.SerializableArrayTag;
/*  45:    */ import org.spacehq.opennbt.tag.builtin.custom.SerializableTag;
/*  46:    */ import org.spacehq.opennbt.tag.builtin.custom.ShortArrayTag;
/*  47:    */ import org.spacehq.opennbt.tag.builtin.custom.StringArrayTag;
/*  48:    */ 
/*  49:    */ public class ConverterRegistry
/*  50:    */ {
/*  51: 17 */   private static final Map<Class<? extends Tag>, TagConverter<? extends Tag, ?>> tagToConverter = new HashMap();
/*  52: 18 */   private static final Map<Class<?>, TagConverter<? extends Tag, ?>> typeToConverter = new HashMap();
/*  53:    */   
/*  54:    */   static
/*  55:    */   {
/*  56: 21 */     register(ByteTag.class, Byte.class, new ByteTagConverter());
/*  57: 22 */     register(ShortTag.class, Short.class, new ShortTagConverter());
/*  58: 23 */     register(IntTag.class, Integer.class, new IntTagConverter());
/*  59: 24 */     register(LongTag.class, Long.class, new LongTagConverter());
/*  60: 25 */     register(FloatTag.class, Float.class, new FloatTagConverter());
/*  61: 26 */     register(DoubleTag.class, Double.class, new DoubleTagConverter());
/*  62: 27 */     register(ByteArrayTag.class, [B.class, new ByteArrayTagConverter());
/*  63: 28 */     register(StringTag.class, String.class, new StringTagConverter());
/*  64: 29 */     register(ListTag.class, List.class, new ListTagConverter());
/*  65: 30 */     register(CompoundTag.class, Map.class, new CompoundTagConverter());
/*  66: 31 */     register(IntArrayTag.class, [I.class, new IntArrayTagConverter());
/*  67:    */     
/*  68: 33 */     register(DoubleArrayTag.class, [D.class, new DoubleArrayTagConverter());
/*  69: 34 */     register(FloatArrayTag.class, [F.class, new FloatArrayTagConverter());
/*  70: 35 */     register(LongArrayTag.class, [J.class, new LongArrayTagConverter());
/*  71: 36 */     register(SerializableArrayTag.class, [Ljava.io.Serializable.class, new SerializableArrayTagConverter());
/*  72: 37 */     register(SerializableTag.class, Serializable.class, new SerializableTagConverter());
/*  73: 38 */     register(ShortArrayTag.class, [S.class, new ShortArrayTagConverter());
/*  74: 39 */     register(StringArrayTag.class, [Ljava.lang.String.class, new StringArrayTagConverter());
/*  75:    */   }
/*  76:    */   
/*  77:    */   public static <T extends Tag, V> void register(Class<T> tag, Class<V> type, TagConverter<T, V> converter)
/*  78:    */     throws ConverterRegisterException
/*  79:    */   {
/*  80: 51 */     if (tagToConverter.containsKey(tag)) {
/*  81: 52 */       throw new TagRegisterException("Type conversion to tag " + tag.getName() + " is already registered.");
/*  82:    */     }
/*  83: 55 */     if (typeToConverter.containsKey(type)) {
/*  84: 56 */       throw new TagRegisterException("Tag conversion to type " + type.getName() + " is already registered.");
/*  85:    */     }
/*  86: 59 */     tagToConverter.put(tag, converter);
/*  87: 60 */     typeToConverter.put(type, converter);
/*  88:    */   }
/*  89:    */   
/*  90:    */   public static <T extends Tag, V> V convertToValue(T tag)
/*  91:    */     throws ConversionException
/*  92:    */   {
/*  93: 71 */     if ((tag == null) || (tag.getValue() == null)) {
/*  94: 72 */       return null;
/*  95:    */     }
/*  96: 75 */     if (!tagToConverter.containsKey(tag.getClass())) {
/*  97: 76 */       throw new ConversionException("Tag type " + tag.getClass().getName() + " has no converter.");
/*  98:    */     }
/*  99: 79 */     TagConverter<T, ?> converter = (TagConverter)tagToConverter.get(tag.getClass());
/* 100: 80 */     return converter.convert(tag);
/* 101:    */   }
/* 102:    */   
/* 103:    */   public static <V, T extends Tag> T convertToTag(String name, V value)
/* 104:    */     throws ConversionException
/* 105:    */   {
/* 106: 92 */     if (value == null) {
/* 107: 93 */       return null;
/* 108:    */     }
/* 109: 96 */     TagConverter<T, V> converter = (TagConverter)typeToConverter.get(value.getClass());
/* 110: 97 */     if (converter == null) {
/* 111: 98 */       for (Class<?> clazz : getAllClasses(value.getClass())) {
/* 112: 99 */         if (typeToConverter.containsKey(clazz)) {
/* 113:    */           try
/* 114:    */           {
/* 115:101 */             converter = (TagConverter)typeToConverter.get(clazz);
/* 116:    */           }
/* 117:    */           catch (ClassCastException localClassCastException) {}
/* 118:    */         }
/* 119:    */       }
/* 120:    */     }
/* 121:109 */     if (converter == null) {
/* 122:110 */       throw new ConversionException("Value type " + value.getClass().getName() + " has no converter.");
/* 123:    */     }
/* 124:113 */     return converter.convert(name, value);
/* 125:    */   }
/* 126:    */   
/* 127:    */   private static Set<Class<?>> getAllClasses(Class<?> clazz)
/* 128:    */   {
/* 129:117 */     Set<Class<?>> ret = new LinkedHashSet();
/* 130:118 */     Class<?> c = clazz;
/* 131:119 */     while (c != null)
/* 132:    */     {
/* 133:120 */       ret.add(c);
/* 134:121 */       ret.addAll(getAllSuperInterfaces(c));
/* 135:122 */       c = c.getSuperclass();
/* 136:    */     }
/* 137:126 */     if (ret.contains(Serializable.class))
/* 138:    */     {
/* 139:127 */       ret.remove(Serializable.class);
/* 140:128 */       ret.add(Serializable.class);
/* 141:    */     }
/* 142:131 */     return ret;
/* 143:    */   }
/* 144:    */   
/* 145:    */   private static Set<Class<?>> getAllSuperInterfaces(Class<?> clazz)
/* 146:    */   {
/* 147:135 */     Set<Class<?>> ret = new HashSet();
/* 148:136 */     for (Class<?> c : clazz.getInterfaces())
/* 149:    */     {
/* 150:137 */       ret.add(c);
/* 151:138 */       ret.addAll(getAllSuperInterfaces(c));
/* 152:    */     }
/* 153:141 */     return ret;
/* 154:    */   }
/* 155:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.ConverterRegistry
 * JD-Core Version:    0.7.0.1
 */