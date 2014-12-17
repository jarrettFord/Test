/*   1:    */ package org.spacehq.packetlib.packet;
/*   2:    */ 
/*   3:    */ import java.lang.reflect.Constructor;
/*   4:    */ import java.util.HashMap;
/*   5:    */ import java.util.Map;
/*   6:    */ import org.spacehq.packetlib.Client;
/*   7:    */ import org.spacehq.packetlib.Server;
/*   8:    */ import org.spacehq.packetlib.Session;
/*   9:    */ import org.spacehq.packetlib.crypt.PacketEncryption;
/*  10:    */ 
/*  11:    */ public abstract class PacketProtocol
/*  12:    */ {
/*  13: 18 */   private final Map<Integer, Class<? extends Packet>> incoming = new HashMap();
/*  14: 19 */   private final Map<Class<? extends Packet>, Integer> outgoing = new HashMap();
/*  15:    */   
/*  16:    */   public abstract boolean needsPacketSizer();
/*  17:    */   
/*  18:    */   public abstract boolean needsPacketEncryptor();
/*  19:    */   
/*  20:    */   public abstract PacketHeader getPacketHeader();
/*  21:    */   
/*  22:    */   public abstract PacketEncryption getEncryption();
/*  23:    */   
/*  24:    */   public abstract void newClientSession(Client paramClient, Session paramSession);
/*  25:    */   
/*  26:    */   public abstract void newServerSession(Server paramServer, Session paramSession);
/*  27:    */   
/*  28:    */   public final void clearPackets()
/*  29:    */   {
/*  30: 69 */     this.incoming.clear();
/*  31: 70 */     this.outgoing.clear();
/*  32:    */   }
/*  33:    */   
/*  34:    */   public final void register(int id, Class<? extends Packet> packet)
/*  35:    */   {
/*  36: 81 */     registerIncoming(id, packet);
/*  37: 82 */     registerOutgoing(id, packet);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public final void registerIncoming(int id, Class<? extends Packet> packet)
/*  41:    */   {
/*  42: 93 */     this.incoming.put(Integer.valueOf(id), packet);
/*  43:    */     try
/*  44:    */     {
/*  45: 95 */       createIncomingPacket(id);
/*  46:    */     }
/*  47:    */     catch (IllegalStateException e)
/*  48:    */     {
/*  49: 97 */       this.incoming.remove(Integer.valueOf(id));
/*  50: 98 */       throw new IllegalArgumentException(e.getMessage(), e.getCause());
/*  51:    */     }
/*  52:    */   }
/*  53:    */   
/*  54:    */   public final void registerOutgoing(int id, Class<? extends Packet> packet)
/*  55:    */   {
/*  56:109 */     this.outgoing.put(packet, Integer.valueOf(id));
/*  57:    */   }
/*  58:    */   
/*  59:    */   public final Packet createIncomingPacket(int id)
/*  60:    */   {
/*  61:121 */     if ((id < 0) || (!this.incoming.containsKey(Integer.valueOf(id))) || (this.incoming.get(Integer.valueOf(id)) == null)) {
/*  62:122 */       throw new IllegalArgumentException("Invalid packet id: " + id);
/*  63:    */     }
/*  64:125 */     Class<? extends Packet> packet = (Class)this.incoming.get(Integer.valueOf(id));
/*  65:    */     try
/*  66:    */     {
/*  67:127 */       Constructor<? extends Packet> constructor = packet.getDeclaredConstructor(new Class[0]);
/*  68:128 */       if (!constructor.isAccessible()) {
/*  69:129 */         constructor.setAccessible(true);
/*  70:    */       }
/*  71:132 */       return (Packet)constructor.newInstance(new Object[0]);
/*  72:    */     }
/*  73:    */     catch (NoSuchMethodError e)
/*  74:    */     {
/*  75:134 */       throw new IllegalStateException("Packet \"" + id + ", " + packet.getName() + "\" does not have a no-params constructor for instantiation.");
/*  76:    */     }
/*  77:    */     catch (Exception e)
/*  78:    */     {
/*  79:136 */       throw new IllegalStateException("Failed to instantiate packet \"" + id + ", " + packet.getName() + "\".", e);
/*  80:    */     }
/*  81:    */   }
/*  82:    */   
/*  83:    */   public final int getOutgoingId(Class<? extends Packet> packet)
/*  84:    */   {
/*  85:148 */     if ((!this.outgoing.containsKey(packet)) || (this.outgoing.get(packet) == null)) {
/*  86:149 */       throw new IllegalArgumentException("Unregistered outgoing packet class: " + packet.getName());
/*  87:    */     }
/*  88:152 */     return ((Integer)this.outgoing.get(packet)).intValue();
/*  89:    */   }
/*  90:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.packet.PacketProtocol
 * JD-Core Version:    0.7.0.1
 */