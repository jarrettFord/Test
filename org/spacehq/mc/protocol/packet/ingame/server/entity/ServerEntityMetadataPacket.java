/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.mc.protocol.data.game.EntityMetadata;
/*  5:   */ import org.spacehq.mc.protocol.util.NetUtil;
/*  6:   */ import org.spacehq.packetlib.io.NetInput;
/*  7:   */ import org.spacehq.packetlib.io.NetOutput;
/*  8:   */ import org.spacehq.packetlib.packet.Packet;
/*  9:   */ 
/* 10:   */ public class ServerEntityMetadataPacket
/* 11:   */   implements Packet
/* 12:   */ {
/* 13:   */   private int entityId;
/* 14:   */   private EntityMetadata[] metadata;
/* 15:   */   
/* 16:   */   private ServerEntityMetadataPacket() {}
/* 17:   */   
/* 18:   */   public ServerEntityMetadataPacket(int entityId, EntityMetadata[] metadata)
/* 19:   */   {
/* 20:21 */     this.entityId = entityId;
/* 21:22 */     this.metadata = metadata;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public int getEntityId()
/* 25:   */   {
/* 26:26 */     return this.entityId;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public EntityMetadata[] getMetadata()
/* 30:   */   {
/* 31:30 */     return this.metadata;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public void read(NetInput in)
/* 35:   */     throws IOException
/* 36:   */   {
/* 37:35 */     this.entityId = in.readInt();
/* 38:36 */     this.metadata = NetUtil.readEntityMetadata(in);
/* 39:   */   }
/* 40:   */   
/* 41:   */   public void write(NetOutput out)
/* 42:   */     throws IOException
/* 43:   */   {
/* 44:41 */     out.writeInt(this.entityId);
/* 45:42 */     NetUtil.writeEntityMetadata(out, this.metadata);
/* 46:   */   }
/* 47:   */   
/* 48:   */   public boolean isPriority()
/* 49:   */   {
/* 50:47 */     return false;
/* 51:   */   }
/* 52:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityMetadataPacket
 * JD-Core Version:    0.7.0.1
 */