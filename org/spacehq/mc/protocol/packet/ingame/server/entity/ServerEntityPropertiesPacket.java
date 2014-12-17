/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import java.util.ArrayList;
/*  5:   */ import java.util.Iterator;
/*  6:   */ import java.util.List;
/*  7:   */ import java.util.UUID;
/*  8:   */ import org.spacehq.mc.protocol.data.game.Attribute;
/*  9:   */ import org.spacehq.mc.protocol.data.game.AttributeModifier;
/* 10:   */ import org.spacehq.packetlib.io.NetInput;
/* 11:   */ import org.spacehq.packetlib.io.NetOutput;
/* 12:   */ import org.spacehq.packetlib.packet.Packet;
/* 13:   */ 
/* 14:   */ public class ServerEntityPropertiesPacket
/* 15:   */   implements Packet
/* 16:   */ {
/* 17:   */   private int entityId;
/* 18:   */   private List<Attribute> attributes;
/* 19:   */   
/* 20:   */   private ServerEntityPropertiesPacket() {}
/* 21:   */   
/* 22:   */   public ServerEntityPropertiesPacket(int entityId, List<Attribute> attributes)
/* 23:   */   {
/* 24:24 */     this.entityId = entityId;
/* 25:25 */     this.attributes = attributes;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public int getEntityId()
/* 29:   */   {
/* 30:29 */     return this.entityId;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public List<Attribute> getAttributes()
/* 34:   */   {
/* 35:33 */     return this.attributes;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public void read(NetInput in)
/* 39:   */     throws IOException
/* 40:   */   {
/* 41:38 */     this.entityId = in.readInt();
/* 42:39 */     this.attributes = new ArrayList();
/* 43:40 */     int length = in.readInt();
/* 44:41 */     for (int index = 0; index < length; index++)
/* 45:   */     {
/* 46:42 */       String key = in.readString();
/* 47:43 */       double value = in.readDouble();
/* 48:44 */       List<AttributeModifier> modifiers = new ArrayList();
/* 49:45 */       short len = in.readShort();
/* 50:46 */       for (int ind = 0; ind < len; ind++) {
/* 51:47 */         modifiers.add(new AttributeModifier(new UUID(in.readLong(), in.readLong()), in.readDouble(), in.readByte()));
/* 52:   */       }
/* 53:50 */       this.attributes.add(new Attribute(key, value, modifiers));
/* 54:   */     }
/* 55:   */   }
/* 56:   */   
/* 57:   */   public void write(NetOutput out)
/* 58:   */     throws IOException
/* 59:   */   {
/* 60:56 */     out.writeInt(this.entityId);
/* 61:57 */     out.writeInt(this.attributes.size());
/* 62:   */     Iterator localIterator2;
/* 63:58 */     for (Iterator localIterator1 = this.attributes.iterator(); localIterator1.hasNext(); localIterator2.hasNext())
/* 64:   */     {
/* 65:58 */       Attribute attribute = (Attribute)localIterator1.next();
/* 66:59 */       out.writeString(attribute.getKey());
/* 67:60 */       out.writeDouble(attribute.getValue());
/* 68:61 */       out.writeShort(attribute.getModifiers().size());
/* 69:62 */       localIterator2 = attribute.getModifiers().iterator(); continue;AttributeModifier modifier = (AttributeModifier)localIterator2.next();
/* 70:63 */       out.writeLong(modifier.getUUID().getMostSignificantBits());
/* 71:64 */       out.writeLong(modifier.getUUID().getLeastSignificantBits());
/* 72:65 */       out.writeDouble(modifier.getAmount());
/* 73:66 */       out.writeByte(modifier.getOperation());
/* 74:   */     }
/* 75:   */   }
/* 76:   */   
/* 77:   */   public boolean isPriority()
/* 78:   */   {
/* 79:73 */     return false;
/* 80:   */   }
/* 81:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityPropertiesPacket
 * JD-Core Version:    0.7.0.1
 */