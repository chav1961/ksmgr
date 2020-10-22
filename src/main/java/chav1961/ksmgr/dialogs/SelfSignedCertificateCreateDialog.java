package chav1961.ksmgr.dialogs;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import chav1961.ksmgr.interfaces.KeyLength;
import chav1961.ksmgr.interfaces.SecurityAlgorithm;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.Constraint;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.dialogs.SelfSignedCertificateCreateDialog/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog",tooltip="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.tt",help="help.aboutApplication")
public class SelfSignedCertificateCreateDialog implements FormManager<Object, SelfSignedCertificateCreateDialog> {
	private final LoggerFacade 	facade;
	private final KeyStore 		ks;

	@LocaleResource(value="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.algorithm",tooltip="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.algorithm.tt")
	@Format("30ms")
	public SecurityAlgorithm	algorithm = SecurityAlgorithm.RSA;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.keylength",tooltip="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.keylength.tt")
	@Format("30ms")
	public KeyLength			keyLength = KeyLength.KEY1024;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.usesecurerandom",tooltip="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.usesecurerandom.tt")
	@Format("1m")
	public boolean				useSecureRandom = true;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.principalname",tooltip="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.principalname.tt")
	@Format("30ms")
	@Constraint(value="!principalName.isEmpty()",messageId="TestSet1",severity=Severity.warning)
	public String				principalName = "myself";
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.serialnumber",tooltip="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.serialnumber.tt")
	@Format("30ms")
	public BigInteger			serialNumber = BigInteger.ONE;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.issuername",tooltip="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.issuername.tt")
	@Format("30ms")
	public String				issuerName = "myself"; 

	@LocaleResource(value="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.subject",tooltip="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.subject.tt")
	@Format("30s")
	public String				subject = "some shit";
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.datefrom",tooltip="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.datefrom.tt")
	@Format("(YYYY-MM-dd)ms")
	public Date					dateFrom;

	@LocaleResource(value="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.dateto",tooltip="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.dateto.tt")
	@Format("(YYYY-MM-dd)ms")
	public Date					dateTo;

	@LocaleResource(value="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.alias",tooltip="chav1961.ksmgr.dialogs.selfsignedcertificatecreatedialog.alias.tt")
	@Format("30ms")
	public String		alias = "myCertificate";
	
	
	public SelfSignedCertificateCreateDialog(final LoggerFacade facade, final KeyStore ks) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else if (ks == null) {
			throw new NullPointerException("Keystore can't be null"); 
		}
		else {
			final long		current = System.currentTimeMillis();
			final Calendar	cal = Calendar.getInstance();
			
			this.facade = facade;
			this.ks = ks;
			cal.setTimeInMillis(current);
			
			cal.add(Calendar.DAY_OF_MONTH,-1);
		    this.dateFrom = new Date(cal.getTimeInMillis());
			cal.add(Calendar.YEAR,1);
		    this.dateTo = new Date(cal.getTimeInMillis());
		}
	}

	@Override
	public RefreshMode onField(final SelfSignedCertificateCreateDialog inst, final Object id, final String fieldName, final Object oldValue) throws FlowException, LocalizationException {
		switch (fieldName) {
			case "alias"	:
				try{final Enumeration<String>	aliases = ks.aliases();
				
				while (aliases.hasMoreElements()) {
					final String	name = aliases.nextElement();
					
					if (alias.equals(name)) {
						getLogger().message(Severity.warning,"Alias ["+alias+"] already exists in the store");
						return RefreshMode.REJECT;
					}
				}
			} catch (KeyStoreException e) {
				getLogger().message(Severity.error,e,"Error processing key store: "+e.getLocalizedMessage());
				return RefreshMode.REJECT;
			}
		}
		return RefreshMode.DEFAULT;
	}

	@Override
	public LoggerFacade getLogger() {
		return facade;
	}
	
	
	public Certificate generate(final String principalName) throws CertificateEncodingException, InvalidKeyException, IllegalStateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException {
		  // generate a key pair
	    final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
	    
	    keyPairGenerator.initialize(4096, new SecureRandom());
	    final KeyPair 	keyPair = keyPairGenerator.generateKeyPair();
	
	    
	    X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
	    X500Principal dnName = new X500Principal("cn="+principalName);
	
	    // add some options
	    certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
	    certGen.setSubjectDN(new X509Name("dc=name"));
	    certGen.setIssuerDN(dnName); // use the same
	    // yesterday
	    certGen.setNotBefore(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
	    // in 2 years
	    certGen.setNotAfter(new Date(System.currentTimeMillis() + 2 * 365 * 24 * 60 * 60 * 1000));
	    certGen.setPublicKey(keyPair.getPublic());
	    certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
	
	    // finally, sign the certificate with the private key of the same KeyPair
	    X509Certificate cert = certGen.generate(keyPair.getPrivate(), "BC");
	    
	    return cert;
	}
	
	
	
	
	
	
	
	
	
	
}
