/*   1:    */ package org.spacehq.opennbt;
/*   2:    */ 
/*   3:    */ import java.io.DataInputStream;
/*   4:    */ import java.io.DataOutputStream;
/*   5:    */ import java.io.File;
/*   6:    */ import java.io.FileInputStream;
/*   7:    */ import java.io.FileOutputStream;
/*   8:    */ import java.io.IOException;
/*   9:    */ import java.io.InputStream;
/*  10:    */ import java.io.OutputStream;
/*  11:    */ import java.nio.charset.Charset;
/*  12:    */ import java.util.zip.GZIPInputStream;
/*  13:    */ import java.util.zip.GZIPOutputStream;
/*  14:    */ import org.spacehq.opennbt.tag.TagCreateException;
/*  15:    */ import org.spacehq.opennbt.tag.TagRegistry;
/*  16:    */ import org.spacehq.opennbt.tag.builtin.CompoundTag;
/*  17:    */ import org.spacehq.opennbt.tag.builtin.Tag;
/*  18:    */ 
/*  19:    */ public class NBTIO
/*  20:    */ {
/*  21: 18 */   public static final Charset CHARSET = Charset.forName("UTF-8");
/*  22:    */   
/*  23:    */   public static CompoundTag readFile(String path)
/*  24:    */     throws IOException
/*  25:    */   {
/*  26: 28 */     return readFile(new File(path));
/*  27:    */   }
/*  28:    */   
/*  29:    */   public static CompoundTag readFile(File file)
/*  30:    */     throws IOException
/*  31:    */   {
/*  32: 39 */     return readFile(file, true);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public static CompoundTag readFile(String path, boolean compressed)
/*  36:    */     throws IOException
/*  37:    */   {
/*  38: 51 */     return readFile(new File(path), compressed);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public static CompoundTag readFile(File file, boolean compressed)
/*  42:    */     throws IOException
/*  43:    */   {
/*  44: 63 */     InputStream in = new FileInputStream(file);
/*  45: 64 */     if (compressed) {
/*  46: 65 */       in = new GZIPInputStream(in);
/*  47:    */     }
/*  48: 68 */     Tag tag = readTag(new DataInputStream(in));
/*  49: 69 */     if (!(tag instanceof CompoundTag)) {
/*  50: 70 */       throw new IOException("Root tag is not a CompoundTag!");
/*  51:    */     }
/*  52: 73 */     return (CompoundTag)tag;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public static void writeFile(CompoundTag tag, String path)
/*  56:    */     throws IOException
/*  57:    */   {
/*  58: 84 */     writeFile(tag, new File(path));
/*  59:    */   }
/*  60:    */   
/*  61:    */   public static void writeFile(CompoundTag tag, File file)
/*  62:    */     throws IOException
/*  63:    */   {
/*  64: 95 */     writeFile(tag, file, true);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public static void writeFile(CompoundTag tag, String path, boolean compressed)
/*  68:    */     throws IOException
/*  69:    */   {
/*  70:107 */     writeFile(tag, new File(path), compressed);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public static void writeFile(CompoundTag tag, File file, boolean compressed)
/*  74:    */     throws IOException
/*  75:    */   {
/*  76:119 */     if (!file.exists())
/*  77:    */     {
/*  78:120 */       if ((file.getParentFile() != null) && (!file.getParentFile().exists())) {
/*  79:121 */         file.getParentFile().mkdirs();
/*  80:    */       }
/*  81:124 */       file.createNewFile();
/*  82:    */     }
/*  83:127 */     OutputStream out = new FileOutputStream(file);
/*  84:128 */     if (compressed) {
/*  85:129 */       out = new GZIPOutputStream(out);
/*  86:    */     }
/*  87:132 */     writeTag(new DataOutputStream(out), tag);
/*  88:133 */     out.close();
/*  89:    */   }
/*  90:    */   
/*  91:    */   public static Tag readTag(DataInputStream in)
/*  92:    */     throws IOException
/*  93:    */   {
/*  94:144 */     int id = in.readUnsignedByte();
/*  95:145 */     if (id == 0) {
/*  96:146 */       return null;
/*  97:    */     }
/*  98:149 */     byte[] nameBytes = new byte[in.readUnsignedShort()];
/*  99:150 */     in.readFully(nameBytes);
/* 100:151 */     String name = new String(nameBytes, CHARSET);
/* 101:152 */     Tag tag = null;
/* 102:    */     try
/* 103:    */     {
/* 104:154 */       tag = TagRegistry.createInstance(id, name);
/* 105:    */     }
/* 106:    */     catch (TagCreateException e)
/* 107:    */     {
/* 108:156 */       throw new IOException("Failed to create tag.", e);
/* 109:    */     }
/* 110:159 */     tag.read(in);
/* 111:160 */     return tag;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public static void writeTag(DataOutputStream out, Tag tag)
/* 115:    */     throws IOException
/* 116:    */   {
/* 117:171 */     byte[] nameBytes = tag.getName().getBytes(CHARSET);
/* 118:172 */     out.writeByte(TagRegistry.getIdFor(tag.getClass()));
/* 119:173 */     out.writeShort(nameBytes.length);
/* 120:174 */     out.write(nameBytes);
/* 121:175 */     tag.write(out);
/* 122:    */   }
/* 123:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.NBTIO
 * JD-Core Version:    0.7.0.1
 */