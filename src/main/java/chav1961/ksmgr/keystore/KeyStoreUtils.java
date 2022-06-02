package chav1961.ksmgr.keystore;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.encoders.Base64;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class KeyStoreUtils {
	private static final String			DEFAULT_ENCODING = "UTF-8";

	//
	//		Service methods
	//
	
	public static boolean isAliasInKeyStore(final String alias, final KeyStore ks) throws KeyStoreException, IllegalArgumentException, NullPointerException {
		if (alias == null || alias.isEmpty()) {
			throw new IllegalArgumentException("Alias name can't be null or empty"); 
		}
		else if (ks == null) {
			throw new NullPointerException("Key store can't be null"); 
		}
		else {
			return ks.containsAlias(alias);
		}
	}

	public static String extractFileName(final String path) {
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("FIle path can't be null or empty"); 
		}
		else {
			final int	index = path.lastIndexOf('/');
			
			return index == -1 ? path : path.substring(index+1);
		}
	}
	
	public static String cutExtension(final String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty"); 
		}
		else {
			final int	dot = name.lastIndexOf('.');
			
			return dot == -1 ? name : name.substring(0,dot);
		}
	}

	public static String buildUniqueAlias(final KeyStore ks, final String alias) throws KeyStoreException {
		if (ks == null) {
			throw new NullPointerException("Key store can't be null"); 
		}
		else if (alias == null || alias.isEmpty()) {
			throw new IllegalArgumentException("Alias name can't be null or empty"); 
		}
		else {
			String				currentAlias = alias;
			int					suffix = 1;
			
			while (ks.containsAlias(currentAlias)) {
				currentAlias = alias+'-'+suffix++; 
			}
			return currentAlias;
		}
	}
	
	//
	//		Import methods
	//
	
	public static void keyPairsImport(final FileSystemInterface fsi, final LoggerFacade logger, final String from, final KeyStore to, final String alias, final char[] password) {
		final String	file = from;
		
		try(final FileSystemInterface	in = fsi.clone().open(file);
			final Reader				rdr = in.charRead(DEFAULT_ENCODING);
			final PEMParser				parser = new PEMParser(rdr)) {
			final Object 				pair = parser.readObject();

			if (pair == null) {
				logger.message(Severity.error,"Key pair ["+file+"]: read failed...");
			}
			else {
				final JcaPEMKeyConverter 	converter = new JcaPEMKeyConverter();
				final KeyPair				kp;

				if (pair instanceof PEMKeyPair) {
					final PEMKeyPair 	keyPair = (PEMKeyPair)pair;
					
					kp = converter.getKeyPair(keyPair);
				}
				else if ((pair instanceof PEMEncryptedKeyPair)) {
				    final PEMEncryptedKeyPair 	encryptedKeyPair = (PEMEncryptedKeyPair)pair;
				    final PEMDecryptorProvider	decryptor = new JcePEMDecryptorProviderBuilder().build(password);

				    kp = converter.getKeyPair(encryptedKeyPair.decryptKeyPair(decryptor));
				}
				else {
					kp = null;
				}
				
				if (kp != null) {
					final Certificate	ss = createSelfSigned(kp);
					
					to.setKeyEntry(alias, kp.getPrivate(), password, new Certificate[] {ss});
					logger.message(Severity.info,"Key pair content was successfully imported into keystore with alias ["+alias+"]");
				}
				else {
					logger.message(Severity.error,"Key pair ["+file+"] was not identified as valid type");
				}
			}
		} catch (IOException | KeyStoreException | OperatorCreationException | CertificateException e) {
			logger.message(Severity.error,"Error importing key pair ["+file+"]: "+e.getLocalizedMessage());
		}
	}

	public static void secretKeyImport(final FileSystemInterface fsi, final LoggerFacade logger, final String from, final String algorithm, final KeyStore to, final String alias, final char[] password) {
		try(final FileSystemInterface	fs = fsi.clone().open(from);
			final InputStream			is = fs.read();
			final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			
			Utils.copyStream(is, baos);
			baos.flush();
			
			final SecretKey			key = new SecretKeySpec(baos.toByteArray(), algorithm);
			
			to.setKeyEntry(alias, key, password, null);
			logger.message(Severity.info,"Secret key for alias ["+alias+"] imported successfully");
		} catch (IOException | KeyStoreException e) {
			logger.message(Severity.error,"Error importing secret key for alias ["+alias+"]: "+e.getLocalizedMessage());
		}
	}	

	//
	//		Export methods
	//
	
	public static boolean keyPairsExport(final FileSystemInterface fsi, final LoggerFacade logger, final KeyStore from, final String item, final String to, final char[] password) {
		try{final Key			privK = from.getKey(item,password);
			final Certificate	cert = from.getCertificate(item);
			final String		targetDir = to;
			final String		exportFileName = item+".keys";
			
			if (privK != null && cert != null) {
				try(final FileSystemInterface	out = fsi.clone().open(targetDir+"/"+exportFileName).create();
					final Writer				wr = out.charWrite(DEFAULT_ENCODING);
					final JcaPEMWriter			pemWriter = new JcaPEMWriter(wr)) {
						
				    pemWriter.writeObject(privK);
				    pemWriter.writeObject(cert.getPublicKey());
				    pemWriter.flush();
					logger.message(Severity.info,"Key pair content was exported to ["+targetDir+"/"+exportFileName+"]");
					return true;
				}
			}
			else {
				logger.message(Severity.error,"Key pair alias ["+item+"] doesn't read");
				return false;
			}
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			logger.message(Severity.error,e,"Key pair alias ["+item+"] doesn't read");
			return false;
		}
	}

	public static boolean certificateExport(final FileSystemInterface fsi, final LoggerFacade logger, final KeyStore from, final String item, final String to) {
		try{final Certificate	cert = from.getCertificate(item);
			final String		targetDir = to;
			final String		exportFileName = item+".crt";
			
			if (cert != null) {
				try(final FileSystemInterface	out = fsi.clone().open(targetDir+"/"+exportFileName).create();
					final Writer				wr = out.charWrite(DEFAULT_ENCODING);
					final JcaPEMWriter			pemWriter = new JcaPEMWriter(wr)) {
						
				    pemWriter.writeObject(cert);
				    pemWriter.flush();
					logger.message(Severity.info,"Key pair content was exported to ["+targetDir+"/"+exportFileName+"]");
					return true;
				}
			}
			else {
				logger.message(Severity.error,"Certificate alias ["+item+"] doesn't read");
				return false;
			}
		} catch (KeyStoreException | IOException e) {
			logger.message(Severity.error,e,"Key pair alias ["+item+"] doesn't read");
			return false;
		}
	}

	public static boolean certificateRequestExport(final FileSystemInterface fsi, final LoggerFacade logger, final KeyStore from, final String item, final String principalName, final String to, final char[] password) {
		try{final Key								privK = from.getKey(item,password);
			final Certificate						cert = from.getCertificate(item);
			final PKCS10CertificationRequestBuilder	p10Builder = new JcaPKCS10CertificationRequestBuilder(new X500Principal("CN="+principalName), cert.getPublicKey());
			final JcaContentSignerBuilder			csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
			final ContentSigner 					signer = csBuilder.build((PrivateKey) privK);
			final PKCS10CertificationRequest 		csr = p10Builder.build(signer);
			final String							targetDir = to;
			final String							exportFileName = item+".csr";
			
			if (privK != null && cert != null) {
				try(final FileSystemInterface	out = fsi.clone().open(targetDir+"/"+exportFileName).create();
					final Writer				wr = out.charWrite(DEFAULT_ENCODING);
					final JcaPEMWriter			pemWriter = new JcaPEMWriter(wr)) {
					
					
				    pemWriter.writeObject(csr);
				    pemWriter.flush();
					logger.message(Severity.info,"Certificate request was exported to ["+targetDir+"/"+exportFileName+"]");
					return true;
				}
			}
			else {
				logger.message(Severity.error,"Key pair alias ["+item+"] doesn't read");
				return false;
			}
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | OperatorCreationException | IOException e) {
			logger.message(Severity.error,e,"Key pair alias ["+item+"] doesn't read");
			return false;
		}
	}

	public static boolean certificateRequestSign(final FileSystemInterface fsi, final LoggerFacade logger, final KeyStore repo, final String item, final String fileName, final String algorithm, final BigInteger serial, final Date dateFrom, final Date dateTo, final char[] password) {
		try{final PrivateKey 		signerKey = (PrivateKey)repo.getKey(item, password);
		    final X509Certificate 	signerCert = (X509Certificate)repo.getCertificate(item);
		    final String			fromFile = fileName, toFile = fromFile.substring(0,fromFile.lastIndexOf('.'))+".crs";
	    
		    try(final FileSystemInterface	fs = fsi.clone().open(fromFile);
		    	final Reader				rdr = fs.charRead("UTF-8");
		    	final PEMParser 			reader = new PEMParser(rdr)) {
		    	final PKCS10CertificationRequest csr = (PKCS10CertificationRequest)reader.readObject();
			    
			    final AlgorithmIdentifier 	sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(algorithm);
			    final AlgorithmIdentifier 	digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
			    final X500Name 				issuer = new X500Name(signerCert.getSubjectX500Principal().getName());
	
			    final X509v3CertificateBuilder	certgen = new X509v3CertificateBuilder(issuer, serial, dateFrom, dateTo, csr.getSubject(), csr.getSubjectPublicKeyInfo());
			    
			    certgen.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
			    certgen.addExtension(Extension.subjectKeyIdentifier, false, new SubjectKeyIdentifier(csr.getSubjectPublicKeyInfo().getEncoded()));
			    certgen.addExtension(Extension.authorityKeyIdentifier, false, new AuthorityKeyIdentifier(new GeneralNames(new GeneralName(new X500Name(signerCert.getSubjectX500Principal().getName()))), signerCert.getSerialNumber()));
	
			    ContentSigner signer = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey(signerKey.getEncoded()));
			    X509CertificateHolder holder = certgen.build(signer);
			    byte[] certencoded = holder.toASN1Structure().getEncoded();
	
			    CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
			    signer = new JcaContentSignerBuilder(algorithm).build(signerKey);
			    generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build()).build(signer, signerCert));
			    generator.addCertificate(new X509CertificateHolder(certencoded));
			    generator.addCertificate(new X509CertificateHolder(signerCert.getEncoded()));
			    CMSTypedData content = new CMSProcessableByteArray(certencoded);
			    CMSSignedData signeddata = generator.generate(content, true);

			    try(final FileSystemInterface	fsw = fsi.clone().open(toFile).create();
				    final Writer				wr = fsw.charWrite("UTF-8")) {

			    	writeContent(wr, "PKCS #7 SIGNED DATA", signeddata.getEncoded());
			    }
				logger.message(Severity.info,"Certificate sign was exported to ["+toFile+"]");
			    return true;
			} catch (IOException | OperatorCreationException | CertificateEncodingException | CMSException e) {
				logger.message(Severity.error,"Sign certificate failed: "+e.getLocalizedMessage());
				return false;
			}
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			logger.message(Severity.error,e,"Key pair alias ["+item+"] doesn't read");
			return false;
		}
	}
	
	public static Certificate createSelfSigned(final KeyPair pair)  throws OperatorCreationException, CertIOException, CertificateException {
        final X500Name 		dnName = new X500Name("CN=publickeystorageonly");
        final BigInteger 	certSerialNumber = BigInteger.ONE;
        final Date 			startDate = new Date();
        final Calendar 		calendar = Calendar.getInstance();
        
        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 1);
        
        final Date 			endDate = calendar.getTime();
        final ContentSigner	contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(pair.getPrivate());
        final JcaX509v3CertificateBuilder 	certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, pair.getPublic());

        return new JcaX509CertificateConverter().getCertificate(certBuilder.build(contentSigner));
    }
	
	public static void writeContent(final Writer wr, final String contentType, final byte[] content) throws IOException {
		if (wr == null) {
			throw new NullPointerException("Writer can't be null");
		}
		else if (contentType == null || contentType.isEmpty()) {
			throw new IllegalArgumentException("Content type can't be null or empty");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
		    wr.write("-----BEGIN "+contentType+"-----\n");
		    wr.write(new String(Base64.encode(content)));
		    wr.write("\n-----END "+contentType+"-----\n");
			wr.flush();
		}
	}

	public static byte[] readContent(final Reader rdr, final String contentType) throws IOException {
		if (rdr == null) {
			throw new NullPointerException("Writer can't be null");
		}
		else if (contentType == null || contentType.isEmpty()) {
			throw new IllegalArgumentException("Content type can't be null or empty");
		}
		else {
			final BufferedReader	brdr = new BufferedReader(rdr);
		    final StringBuilder		sb = new StringBuilder();
		    
			String		line;
		    
		    while ((line = brdr.readLine()) != null) {
		    	if (line.startsWith("-----BEGIN ")) {
		    		if (!line.contains(contentType)) {
		    			throw new IOException("Illegal stream content: ["+contentType+"] awaited");
		    		}
		    	}
		    	else if (line.startsWith("-----END ")) {
		    		break;
		    	}
		    	else {
		    		sb.append(line);
		    	}
		    }
		    return Base64.decode(sb.toString());
		}
	}
	
}
