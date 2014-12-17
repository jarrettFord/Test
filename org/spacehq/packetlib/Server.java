/*   1:    */ package org.spacehq.packetlib;
/*   2:    */ 
/*   3:    */ import java.lang.reflect.Constructor;
/*   4:    */ import java.util.ArrayList;
/*   5:    */ import java.util.HashMap;
/*   6:    */ import java.util.List;
/*   7:    */ import java.util.Map;
/*   8:    */ import org.spacehq.packetlib.event.server.ServerBoundEvent;
/*   9:    */ import org.spacehq.packetlib.event.server.ServerClosedEvent;
/*  10:    */ import org.spacehq.packetlib.event.server.ServerClosingEvent;
/*  11:    */ import org.spacehq.packetlib.event.server.ServerEvent;
/*  12:    */ import org.spacehq.packetlib.event.server.ServerListener;
/*  13:    */ import org.spacehq.packetlib.event.server.SessionAddedEvent;
/*  14:    */ import org.spacehq.packetlib.event.server.SessionRemovedEvent;
/*  15:    */ import org.spacehq.packetlib.packet.PacketProtocol;
/*  16:    */ 
/*  17:    */ public class Server
/*  18:    */ {
/*  19:    */   private String host;
/*  20:    */   private int port;
/*  21:    */   private Class<? extends PacketProtocol> protocol;
/*  22:    */   private SessionFactory factory;
/*  23:    */   private ConnectionListener listener;
/*  24: 22 */   private List<Session> sessions = new ArrayList();
/*  25: 24 */   private Map<String, Object> flags = new HashMap();
/*  26: 25 */   private List<ServerListener> listeners = new ArrayList();
/*  27:    */   
/*  28:    */   public Server(String host, int port, Class<? extends PacketProtocol> protocol, SessionFactory factory)
/*  29:    */   {
/*  30: 28 */     this.host = host;
/*  31: 29 */     this.port = port;
/*  32: 30 */     this.protocol = protocol;
/*  33: 31 */     this.factory = factory;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public Server bind()
/*  37:    */   {
/*  38: 40 */     this.listener = this.factory.createServerListener(this);
/*  39: 41 */     callEvent(new ServerBoundEvent(this));
/*  40: 42 */     return this;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public String getHost()
/*  44:    */   {
/*  45: 51 */     return this.host;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public int getPort()
/*  49:    */   {
/*  50: 60 */     return this.port;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public Class<? extends PacketProtocol> getPacketProtocol()
/*  54:    */   {
/*  55: 69 */     return this.protocol;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public PacketProtocol createPacketProtocol()
/*  59:    */   {
/*  60:    */     try
/*  61:    */     {
/*  62: 80 */       Constructor<? extends PacketProtocol> constructor = this.protocol.getDeclaredConstructor(new Class[0]);
/*  63: 81 */       if (!constructor.isAccessible()) {
/*  64: 82 */         constructor.setAccessible(true);
/*  65:    */       }
/*  66: 85 */       return (PacketProtocol)constructor.newInstance(new Object[0]);
/*  67:    */     }
/*  68:    */     catch (NoSuchMethodError e)
/*  69:    */     {
/*  70: 87 */       throw new IllegalStateException("PacketProtocol \"" + this.protocol.getName() + "\" does not have a no-params constructor for instantiation.");
/*  71:    */     }
/*  72:    */     catch (Exception e)
/*  73:    */     {
/*  74: 89 */       throw new IllegalStateException("Failed to instantiate PacketProtocol " + this.protocol.getName() + ".", e);
/*  75:    */     }
/*  76:    */   }
/*  77:    */   
/*  78:    */   public Map<String, Object> getGlobalFlags()
/*  79:    */   {
/*  80: 99 */     return new HashMap(this.flags);
/*  81:    */   }
/*  82:    */   
/*  83:    */   public boolean hasGlobalFlag(String key)
/*  84:    */   {
/*  85:109 */     return this.flags.containsKey(key);
/*  86:    */   }
/*  87:    */   
/*  88:    */   public <T> T getGlobalFlag(String key)
/*  89:    */   {
/*  90:123 */     Object value = this.flags.get(key);
/*  91:124 */     if (value == null) {
/*  92:125 */       return null;
/*  93:    */     }
/*  94:    */     try
/*  95:    */     {
/*  96:129 */       return value;
/*  97:    */     }
/*  98:    */     catch (ClassCastException e)
/*  99:    */     {
/* 100:131 */       throw new IllegalStateException("Tried to get flag \"" + key + "\" as the wrong type. Actual type: " + value.getClass().getName());
/* 101:    */     }
/* 102:    */   }
/* 103:    */   
/* 104:    */   public void setGlobalFlag(String key, Object value)
/* 105:    */   {
/* 106:143 */     this.flags.put(key, value);
/* 107:    */   }
/* 108:    */   
/* 109:    */   public List<ServerListener> getListeners()
/* 110:    */   {
/* 111:152 */     return new ArrayList(this.listeners);
/* 112:    */   }
/* 113:    */   
/* 114:    */   public void addListener(ServerListener listener)
/* 115:    */   {
/* 116:161 */     this.listeners.add(listener);
/* 117:    */   }
/* 118:    */   
/* 119:    */   public void removeListener(ServerListener listener)
/* 120:    */   {
/* 121:170 */     this.listeners.remove(listener);
/* 122:    */   }
/* 123:    */   
/* 124:    */   public void callEvent(ServerEvent event)
/* 125:    */   {
/* 126:179 */     for (ServerListener listener : this.listeners) {
/* 127:180 */       event.call(listener);
/* 128:    */     }
/* 129:    */   }
/* 130:    */   
/* 131:    */   public List<Session> getSessions()
/* 132:    */   {
/* 133:190 */     return new ArrayList(this.sessions);
/* 134:    */   }
/* 135:    */   
/* 136:    */   public void addSession(Session session)
/* 137:    */   {
/* 138:199 */     this.sessions.add(session);
/* 139:200 */     callEvent(new SessionAddedEvent(this, session));
/* 140:    */   }
/* 141:    */   
/* 142:    */   public void removeSession(Session session)
/* 143:    */   {
/* 144:209 */     this.sessions.remove(session);
/* 145:210 */     if (session.isConnected()) {
/* 146:211 */       session.disconnect("Connection closed.");
/* 147:    */     }
/* 148:214 */     callEvent(new SessionRemovedEvent(this, session));
/* 149:    */   }
/* 150:    */   
/* 151:    */   public boolean isListening()
/* 152:    */   {
/* 153:223 */     return this.listener.isListening();
/* 154:    */   }
/* 155:    */   
/* 156:    */   public void close()
/* 157:    */   {
/* 158:244 */     new Thread(new Runnable()
/* 159:    */     {
/* 160:    */       public void run()
/* 161:    */       {
/* 162:234 */         Server.this.callEvent(new ServerClosingEvent(Server.this));
/* 163:235 */         for (Session session : Server.this.getSessions()) {
/* 164:236 */           if (session.isConnected()) {
/* 165:237 */             session.disconnect("Server closed.");
/* 166:    */           }
/* 167:    */         }
/* 168:241 */         Server.this.listener.close();
/* 169:242 */         Server.this.callEvent(new ServerClosedEvent(Server.this));
/* 170:    */       }
/* 171:244 */     }, "CloseServer").start();
/* 172:    */   }
/* 173:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.Server
 * JD-Core Version:    0.7.0.1
 */