package chav1961.ksmgr.internal;

import java.io.IOException;
import java.io.OutputStreamWriter;
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
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class KeyStoreUtils {
	private static final String			DEFAULT_ENCODING = "UTF-8";
	
	public static String keyPairsImport(final FileSystemInterface fsi, final LoggerFacade logger, final String from, final KeyStore to, final char[] password) {
		final String	file = from;
		final String	alias = file.substring(file.lastIndexOf('/')+1);
		
		try(final FileSystemInterface	in = fsi.clone().open(file);
			final Reader				rdr = in.charRead(DEFAULT_ENCODING);
			final PEMParser				parser = new PEMParser(rdr)) {
			final Object 				pair = parser.readObject();

			if (pair == null) {
				logger.message(Severity.error,"Key pair ["+file+"]: read failed...");
				return null;
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
					String				currentAlias = alias;
					int					suffix = 1;
					
					while (to.containsAlias(currentAlias)) {
						currentAlias = alias+'-'+suffix++; 
					}
					
					to.setKeyEntry(currentAlias, kp.getPrivate(), password, new Certificate[] {ss});
					logger.message(Severity.info,"Key pair content was imported into keystore with alias ["+currentAlias+"]");
					return currentAlias;
				}
				else {
					logger.message(Severity.error,"Key pair ["+file+"] was not identified as valid type");
					return null;
				}
			}
		} catch (IOException | KeyStoreException | OperatorCreationException | CertificateException e) {
			logger.message(Severity.error,"Error importing key pair ["+file+"]: "+e.getLocalizedMessage());
			return null;
		}
	}
	
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
			final String							exportFileName = item+".cer";
			
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
}
