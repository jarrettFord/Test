/*  1:   */ package io.netty.handler.ssl.util;
/*  2:   */ 
/*  3:   */ import java.math.BigInteger;
/*  4:   */ import java.security.KeyPair;
/*  5:   */ import java.security.PrivateKey;
/*  6:   */ import java.security.SecureRandom;
/*  7:   */ import java.security.cert.CertificateException;
/*  8:   */ import sun.security.x509.AlgorithmId;
/*  9:   */ import sun.security.x509.CertificateAlgorithmId;
/* 10:   */ import sun.security.x509.CertificateIssuerName;
/* 11:   */ import sun.security.x509.CertificateSerialNumber;
/* 12:   */ import sun.security.x509.CertificateSubjectName;
/* 13:   */ import sun.security.x509.CertificateValidity;
/* 14:   */ import sun.security.x509.CertificateVersion;
/* 15:   */ import sun.security.x509.CertificateX509Key;
/* 16:   */ import sun.security.x509.X500Name;
/* 17:   */ import sun.security.x509.X509CertImpl;
/* 18:   */ import sun.security.x509.X509CertInfo;
/* 19:   */ 
/* 20:   */ final class OpenJdkSelfSignedCertGenerator
/* 21:   */ {
/* 22:   */   static String[] generate(String fqdn, KeyPair keypair, SecureRandom random)
/* 23:   */     throws Exception
/* 24:   */   {
/* 25:45 */     PrivateKey key = keypair.getPrivate();
/* 26:   */     
/* 27:   */ 
/* 28:48 */     X509CertInfo info = new X509CertInfo();
/* 29:49 */     X500Name owner = new X500Name("CN=" + fqdn);
/* 30:50 */     info.set("version", new CertificateVersion(2));
/* 31:51 */     info.set("serialNumber", new CertificateSerialNumber(new BigInteger(64, random)));
/* 32:   */     try
/* 33:   */     {
/* 34:53 */       info.set("subject", new CertificateSubjectName(owner));
/* 35:   */     }
/* 36:   */     catch (CertificateException ignore)
/* 37:   */     {
/* 38:55 */       info.set("subject", owner);
/* 39:   */     }
/* 40:   */     try
/* 41:   */     {
/* 42:58 */       info.set("issuer", new CertificateIssuerName(owner));
/* 43:   */     }
/* 44:   */     catch (CertificateException ignore)
/* 45:   */     {
/* 46:60 */       info.set("issuer", owner);
/* 47:   */     }
/* 48:62 */     info.set("validity", new CertificateValidity(SelfSignedCertificate.NOT_BEFORE, SelfSignedCertificate.NOT_AFTER));
/* 49:63 */     info.set("key", new CertificateX509Key(keypair.getPublic()));
/* 50:64 */     info.set("algorithmID", new CertificateAlgorithmId(new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid)));
/* 51:   */     
/* 52:   */ 
/* 53:   */ 
/* 54:68 */     X509CertImpl cert = new X509CertImpl(info);
/* 55:69 */     cert.sign(key, "SHA1withRSA");
/* 56:   */     
/* 57:   */ 
/* 58:72 */     info.set("algorithmID.algorithm", cert.get("x509.algorithm"));
/* 59:73 */     cert = new X509CertImpl(info);
/* 60:74 */     cert.sign(key, "SHA1withRSA");
/* 61:75 */     cert.verify(keypair.getPublic());
/* 62:   */     
/* 63:77 */     return SelfSignedCertificate.newSelfSignedCertificate(fqdn, key, cert);
/* 64:   */   }
/* 65:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator
 * JD-Core Version:    0.7.0.1
 */