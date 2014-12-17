/*  1:   */ package io.netty.handler.ssl.util;
/*  2:   */ 
/*  3:   */ import java.math.BigInteger;
/*  4:   */ import java.security.KeyPair;
/*  5:   */ import java.security.PrivateKey;
/*  6:   */ import java.security.Provider;
/*  7:   */ import java.security.SecureRandom;
/*  8:   */ import java.security.cert.X509Certificate;
/*  9:   */ import org.bouncycastle.asn1.x500.X500Name;
/* 10:   */ import org.bouncycastle.cert.X509CertificateHolder;
/* 11:   */ import org.bouncycastle.cert.X509v3CertificateBuilder;
/* 12:   */ import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
/* 13:   */ import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
/* 14:   */ import org.bouncycastle.jce.provider.BouncyCastleProvider;
/* 15:   */ import org.bouncycastle.operator.ContentSigner;
/* 16:   */ import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
/* 17:   */ 
/* 18:   */ final class BouncyCastleSelfSignedCertGenerator
/* 19:   */ {
/* 20:42 */   private static final Provider PROVIDER = new BouncyCastleProvider();
/* 21:   */   
/* 22:   */   static String[] generate(String fqdn, KeyPair keypair, SecureRandom random)
/* 23:   */     throws Exception
/* 24:   */   {
/* 25:45 */     PrivateKey key = keypair.getPrivate();
/* 26:   */     
/* 27:   */ 
/* 28:48 */     X500Name owner = new X500Name("CN=" + fqdn);
/* 29:49 */     X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(owner, new BigInteger(64, random), SelfSignedCertificate.NOT_BEFORE, SelfSignedCertificate.NOT_AFTER, owner, keypair.getPublic());
/* 30:   */     
/* 31:   */ 
/* 32:52 */     ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(key);
/* 33:53 */     X509CertificateHolder certHolder = builder.build(signer);
/* 34:54 */     X509Certificate cert = new JcaX509CertificateConverter().setProvider(PROVIDER).getCertificate(certHolder);
/* 35:55 */     cert.verify(keypair.getPublic());
/* 36:   */     
/* 37:57 */     return SelfSignedCertificate.newSelfSignedCertificate(fqdn, key, cert);
/* 38:   */   }
/* 39:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.util.BouncyCastleSelfSignedCertGenerator
 * JD-Core Version:    0.7.0.1
 */