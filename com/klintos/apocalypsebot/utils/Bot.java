/*  1:   */ package com.klintos.apocalypsebot.utils;
/*  2:   */ 
/*  3:   */ import org.spacehq.mc.auth.exception.AuthenticationException;
/*  4:   */ import org.spacehq.mc.protocol.MinecraftProtocol;
/*  5:   */ import org.spacehq.packetlib.Client;
/*  6:   */ import org.spacehq.packetlib.Session;
/*  7:   */ import org.spacehq.packetlib.event.session.SessionListener;
/*  8:   */ import org.spacehq.packetlib.tcp.TcpSessionFactory;
/*  9:   */ 
/* 10:   */ public class Bot
/* 11:   */ {
/* 12:   */   private String username;
/* 13:   */   private String password;
/* 14:   */   private final Client client;
/* 15:   */   
/* 16:   */   public Bot(String username, String password, String host, int port)
/* 17:   */     throws AuthenticationException
/* 18:   */   {
/* 19:19 */     this.username = username;
/* 20:20 */     this.password = password;
/* 21:21 */     this.client = new Client(host, port, new MinecraftProtocol(username, password, false), new TcpSessionFactory());
/* 22:   */   }
/* 23:   */   
/* 24:   */   public String getUsername()
/* 25:   */   {
/* 26:26 */     return this.username;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public String getPassword()
/* 30:   */   {
/* 31:31 */     return this.password;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public Session getSession()
/* 35:   */   {
/* 36:36 */     return this.client.getSession();
/* 37:   */   }
/* 38:   */   
/* 39:   */   public String getHost()
/* 40:   */   {
/* 41:41 */     return this.client.getHost();
/* 42:   */   }
/* 43:   */   
/* 44:   */   public void addListener(SessionListener listener)
/* 45:   */   {
/* 46:46 */     this.client.getSession().addListener(listener);
/* 47:   */   }
/* 48:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.klintos.apocalypsebot.utils.Bot
 * JD-Core Version:    0.7.0.1
 */