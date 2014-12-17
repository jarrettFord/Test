/*   1:    */ package io.netty.handler.ssl.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.FastThreadLocal;
/*   4:    */ import java.security.InvalidAlgorithmParameterException;
/*   5:    */ import java.security.KeyStore;
/*   6:    */ import java.security.KeyStoreException;
/*   7:    */ import java.security.Provider;
/*   8:    */ import javax.net.ssl.ManagerFactoryParameters;
/*   9:    */ import javax.net.ssl.TrustManager;
/*  10:    */ import javax.net.ssl.TrustManagerFactory;
/*  11:    */ import javax.net.ssl.TrustManagerFactorySpi;
/*  12:    */ 
/*  13:    */ public abstract class SimpleTrustManagerFactory
/*  14:    */   extends TrustManagerFactory
/*  15:    */ {
/*  16: 35 */   private static final Provider PROVIDER = new Provider("", 0.0D, "")
/*  17:    */   {
/*  18:    */     private static final long serialVersionUID = -2680540247105807895L;
/*  19:    */   };
/*  20: 47 */   private static final FastThreadLocal<SimpleTrustManagerFactorySpi> CURRENT_SPI = new FastThreadLocal()
/*  21:    */   {
/*  22:    */     protected SimpleTrustManagerFactory.SimpleTrustManagerFactorySpi initialValue()
/*  23:    */     {
/*  24: 51 */       return new SimpleTrustManagerFactory.SimpleTrustManagerFactorySpi();
/*  25:    */     }
/*  26:    */   };
/*  27:    */   
/*  28:    */   protected SimpleTrustManagerFactory()
/*  29:    */   {
/*  30: 59 */     this("");
/*  31:    */   }
/*  32:    */   
/*  33:    */   protected SimpleTrustManagerFactory(String name)
/*  34:    */   {
/*  35: 68 */     super((TrustManagerFactorySpi)CURRENT_SPI.get(), PROVIDER, name);
/*  36: 69 */     ((SimpleTrustManagerFactorySpi)CURRENT_SPI.get()).init(this);
/*  37: 70 */     CURRENT_SPI.remove();
/*  38: 72 */     if (name == null) {
/*  39: 73 */       throw new NullPointerException("name");
/*  40:    */     }
/*  41:    */   }
/*  42:    */   
/*  43:    */   protected abstract void engineInit(KeyStore paramKeyStore)
/*  44:    */     throws Exception;
/*  45:    */   
/*  46:    */   protected abstract void engineInit(ManagerFactoryParameters paramManagerFactoryParameters)
/*  47:    */     throws Exception;
/*  48:    */   
/*  49:    */   protected abstract TrustManager[] engineGetTrustManagers();
/*  50:    */   
/*  51:    */   static final class SimpleTrustManagerFactorySpi
/*  52:    */     extends TrustManagerFactorySpi
/*  53:    */   {
/*  54:    */     private SimpleTrustManagerFactory parent;
/*  55:    */     
/*  56:    */     void init(SimpleTrustManagerFactory parent)
/*  57:    */     {
/*  58:103 */       this.parent = parent;
/*  59:    */     }
/*  60:    */     
/*  61:    */     protected void engineInit(KeyStore keyStore)
/*  62:    */       throws KeyStoreException
/*  63:    */     {
/*  64:    */       try
/*  65:    */       {
/*  66:109 */         this.parent.engineInit(keyStore);
/*  67:    */       }
/*  68:    */       catch (KeyStoreException e)
/*  69:    */       {
/*  70:111 */         throw e;
/*  71:    */       }
/*  72:    */       catch (Exception e)
/*  73:    */       {
/*  74:113 */         throw new KeyStoreException(e);
/*  75:    */       }
/*  76:    */     }
/*  77:    */     
/*  78:    */     protected void engineInit(ManagerFactoryParameters managerFactoryParameters)
/*  79:    */       throws InvalidAlgorithmParameterException
/*  80:    */     {
/*  81:    */       try
/*  82:    */       {
/*  83:121 */         this.parent.engineInit(managerFactoryParameters);
/*  84:    */       }
/*  85:    */       catch (InvalidAlgorithmParameterException e)
/*  86:    */       {
/*  87:123 */         throw e;
/*  88:    */       }
/*  89:    */       catch (Exception e)
/*  90:    */       {
/*  91:125 */         throw new InvalidAlgorithmParameterException(e);
/*  92:    */       }
/*  93:    */     }
/*  94:    */     
/*  95:    */     protected TrustManager[] engineGetTrustManagers()
/*  96:    */     {
/*  97:131 */       return this.parent.engineGetTrustManagers();
/*  98:    */     }
/*  99:    */   }
/* 100:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.util.SimpleTrustManagerFactory
 * JD-Core Version:    0.7.0.1
 */