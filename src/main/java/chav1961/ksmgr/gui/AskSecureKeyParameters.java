package chav1961.ksmgr.gui;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import chav1961.ksmgr.interfaces.CipherKeyLength;
import chav1961.ksmgr.internal.AlgorithmRepo;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.interfaces.UIFormManager;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.gui.AskSecureKeyParameters/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.tt",help="chav1961.ksmgr.gui.asksecurekeyparameters.help")
public class AskSecureKeyParameters implements FormManager<Object, AskSecureKeyParameters>,  UIFormManager<Object, AskSecureKeyParameters> {
	private static final String	ALGORITHM_TYPE = "SecretKeyFactory";
	
	private final LoggerFacade 	facade;
	private final KeyStore 		ks;
	private final String		provider;

	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.keyalgorithm",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.keyalgorithm.tt")
	@Format("30smd")
	public String				keyAlgorithm = "unknown";

	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.keylength",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.keylength.tt")
	@Format("30sm")
	public CipherKeyLength		cipherKeyLength = CipherKeyLength.KEY256;

	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.iterations",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.iterations.tt")
	@Format("7sm")
	public int					iterations = 1000;
	
	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.currentsalt",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.currentsalt.tt")
	@Format("30sm")
	public String				currentSalt;
	
	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.password",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.password.tt")
	@Format("30ms")
	public char[]				password = null;

	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.passwordretype",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.passwordretype.tt")
	@Format("30ms")
	public char[]				passwordRetype = null;
	
	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.alias",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.alias.tt")
	@Format("30ms")
	public String				alias = "myAlias";

	public AskSecureKeyParameters(final LoggerFacade facade, final KeyStore ks,  final String provider, final String currentSalt) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else if (ks == null) {
			throw new NullPointerException("Key store can't be null"); 
		}
		else if (provider == null || provider.isEmpty()) {
			throw new IllegalArgumentException("Algorithm provider can't be null or empty"); 
		}
		else if (currentSalt == null || currentSalt.isEmpty()) {
			throw new IllegalArgumentException("Current salt can't be null or empty"); 
		}
		else {
			this.facade = facade;
			this.ks = ks;
			this.provider = provider;
			this.currentSalt = currentSalt;
		}
	}

	@Override
	public RefreshMode onField(final AskSecureKeyParameters inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		switch (fieldName) {
			case "alias"	:
				try{if (ks.containsAlias(alias)) {
						getLogger().message(beforeCommit ? Severity.error : Severity.warning,"Alias ["+alias+"] already exists in the store");
						return RefreshMode.REJECT;
					}
					else {
						return RefreshMode.DEFAULT;
					}
				} catch (KeyStoreException e) {
					getLogger().message(Severity.error,e,"Error processing key store: "+e.getLocalizedMessage());
					return RefreshMode.REJECT;
				}
			case "password" : case "passwordRetype" :
				if (!Arrays.equals(password, passwordRetype)) {
					if (beforeCommit) {
						getLogger().message(Severity.error,"Password and retype password differ!");
						return RefreshMode.REJECT;
					}
					else {
						getLogger().message(Severity.warning,"Password and retype password differ!");
					}
				}
				return RefreshMode.DEFAULT;
			default :
				return RefreshMode.DEFAULT;
		}
	}

	@Override
	public LoggerFacade getLogger() {
		return facade;
	}
	
	@Override
	public <T> T[] getForEditorContent(final AskSecureKeyParameters inst, final Object id, final String fieldName, final Object... parameters) throws FlowException {
		switch (fieldName) {
			case "keyAlgorithm"	: 
				final Set<String>	algorithms = new HashSet<>();
				final String		prefix = ((String)parameters[0]).toUpperCase();

				for (String item : AlgorithmRepo.getInstance().getAlgorithms(ALGORITHM_TYPE, provider)) {
					if (item.toUpperCase().startsWith(prefix)) {
						algorithms.add(item);
					}
				}
				final String[]		result = algorithms.toArray(new String[algorithms.size()]); 
				
				Arrays.sort(result);
				return (T[])result;
			default :
				return null;
		}
	}

	@Override
	public AvailableAndVisible getItemState(final ContentNodeMetadata meta) {
		return AvailableAndVisible.DEFAULT;
	}

	@Override
	public FormManager<?, ?> getForEditor(final AskSecureKeyParameters inst, final Object id, final String fieldName, final Object... parameters) throws FlowException {
		return null;
	}
	
	public SecretKey createSecretKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
		final boolean		usePassword = password == null || password.length == 0;
		final String			cipher = "AES";
		final SecretKey		key;
		
		if (usePassword) {
			final byte[]					salt = new byte[100];
			final SecureRandom	random = new SecureRandom();
		    
			random.nextBytes(salt);
		    final PBEKeySpec 		pbeKeySpec = new PBEKeySpec(password, salt, iterations, cipherKeyLength.getKeyLength());
		    final SecretKey 			pbeKey = SecretKeyFactory.getInstance(keyAlgorithm).generateSecret(pbeKeySpec);
		    
		    key = new SecretKeySpec(pbeKey.getEncoded(), cipher);					
		}
		else {
			final byte[]					secureRandomKeyBytes = new byte[cipherKeyLength.getKeyLength() / 8];
			final SecureRandom 	secureRandom = new SecureRandom();
			
		    secureRandom.nextBytes(secureRandomKeyBytes);
		    key = new SecretKeySpec(secureRandomKeyBytes, cipher);
		}
		return key;
	}
}
