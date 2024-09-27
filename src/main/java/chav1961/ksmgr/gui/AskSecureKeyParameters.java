package chav1961.ksmgr.gui;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import chav1961.ksmgr.interfaces.CipherKeyLength;
import chav1961.ksmgr.internal.AlgorithmRepo;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.interfaces.UIFormManager;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.gui.AskSecureKeyParameters/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.tt",help="chav1961.ksmgr.gui.asksecurekeyparameters.help")
public class AskSecureKeyParameters implements FormManager<Object, AskSecureKeyParameters>,  UIFormManager<Object, AskSecureKeyParameters>, ModuleAccessor {
	private static final String		KEY_SERVICE_TYPE = "SecretKeyFactory";
	private static final String		RANDOM_SERVICE_TYPE = "SecurityRandom";
// see https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#securerandom-number-generation-algorithms	
	private static final String[]	RANDOM_ALGORITHMS = {"NativePRNG", "NativePRNGBlocking", "NativePRNGNonBlocking", "PKCS11", "DRBG", "SHA1PRNG", "Windows-PRNG"};
	
	private final LoggerFacade 		facade;
	private final KeyStore 			ks;

	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.keyprovider",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.keyprovider.tt")
	@Format("30smd")
	public String				keyProvider;
	
	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.keyalgorithm",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.keyalgorithm.tt")
	@Format("30smd")
	public String				keyAlgorithm = "unknown";

	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.keylength",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.keylength.tt")
	@Format("30sm")
	public CipherKeyLength		cipherKeyLength = CipherKeyLength.KEY128;

	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.iterations",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.iterations.tt")
	@Format("7sm")
	public int					iterations = 1000;

	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.usepassword",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.usepassword.tt")
	@Format("30sm")
	public boolean				usePassword = true;
	
	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.currentsalt",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.currentsalt.tt")
	@Format("30s")
	public String				currentSalt;
	
	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.password",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.password.tt")
	@Format("30s")
	public char[]				password = null;

	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.passwordretype",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.passwordretype.tt")
	@Format("30s")
	public char[]				passwordRetype = null;

	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.securityrandomprovider",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.securityrandomprovider.tt")
	@Format("30sd")
	public String				securityRandomProvider;
	
	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.securityrandomalgorithm",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.securityrandomalgorithm.tt")
	@Format("30sd")
	public String				securityRandomAlgorithm = "SHA1PRNG";
	
	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparameters.alias",tooltip="chav1961.ksmgr.gui.asksecurekeyparameters.alias.tt")
	@Format("30ms")
	public String				alias = "myAlias";

	public AskSecureKeyParameters(final LoggerFacade facade, final KeyStore ks, final String preferredProvider, final String preferredSalt) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else if (ks == null) {
			throw new NullPointerException("Key store can't be null"); 
		}
		else if (preferredProvider == null || preferredProvider.isEmpty()) {
			throw new IllegalArgumentException("Algorithm provider can't be null or empty"); 
		}
		else if (preferredSalt == null || preferredSalt.isEmpty()) {
			throw new IllegalArgumentException("Current salt can't be null or empty"); 
		}
		else {
			this.facade = facade;
			this.ks = ks;
			this.keyProvider = preferredProvider;
			this.securityRandomProvider = preferredProvider;
			this.currentSalt = preferredSalt;
			if (!isProviderSupportsSecureRandoms(preferredProvider)) {
				getLogger().message(Severity.warning, "Current provider ["+preferredProvider+"] doesn't support secure randoms, select another one");
			}
			if (!isProviderSupportsSecretKeys(preferredProvider)) {
				getLogger().message(Severity.warning, "Current provider ["+preferredProvider+"] doesn't support secret keys, select another one");
			}
		}
	}

	@Override
	public RefreshMode onField(final AskSecureKeyParameters inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		switch (fieldName) {
			case "usePassword"	:
				return RefreshMode.RECORD_ONLY;
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
				else {
					getLogger().message(Severity.info,"Passwords are identical");
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
			case "keyProvider"	:
				final Set<String>	keyProviders = new HashSet<>();
				
				for(String item : AlgorithmRepo.getInstance().getProviders()) {
					if (AlgorithmRepo.getInstance().hasService(item, KEY_SERVICE_TYPE)) {
						keyProviders.add(item);
					}
				}
				final String[]		resultKeyProviders = keyProviders.toArray(new String[keyProviders.size()]); 
				
				Arrays.sort(resultKeyProviders);
				return (T[])resultKeyProviders;
			case "keyAlgorithm"	: 
				final Set<String>	keyAlgorithms = new HashSet<>();

				if (AlgorithmRepo.getInstance().hasProvider(keyProvider) && AlgorithmRepo.getInstance().hasService(keyProvider, KEY_SERVICE_TYPE)) {
					for (String item : AlgorithmRepo.getInstance().getAlgorithms(keyProvider, KEY_SERVICE_TYPE)) {
						keyAlgorithms.add(item);
					}
					final String[]	resultKeyAlgorithms = keyAlgorithms.toArray(new String[keyAlgorithms.size()]); 
					
					Arrays.sort(resultKeyAlgorithms);
					return (T[])resultKeyAlgorithms;
				}
				else {
					getLogger().message(Severity.error, "Unknown provider ["+keyProvider+"] or it doesn't support ["+KEY_SERVICE_TYPE+"] service! Select another accessible provider");
					return null;
				}
			case "securityRandomProvider"	:
				final Set<String>	secureRandomProviders = new HashSet<>();
				
				for(String item : AlgorithmRepo.getInstance().getProviders()) {
					if (AlgorithmRepo.getInstance().hasService(item, RANDOM_SERVICE_TYPE)) {
						secureRandomProviders.add(item);
					}
				}
				final String[]		resultSecureRandomProviders = secureRandomProviders.toArray(new String[secureRandomProviders.size()]); 
				
				Arrays.sort(resultSecureRandomProviders);
				return (T[])resultSecureRandomProviders;
			case "securityRandomAlgorithm"	: 
				final Set<String>	randomAlgorithms = new HashSet<>();

				if (AlgorithmRepo.getInstance().hasProvider(keyProvider) && AlgorithmRepo.getInstance().hasService(keyProvider, RANDOM_SERVICE_TYPE)) {
					for (String item : AlgorithmRepo.getInstance().getAlgorithms(keyProvider, RANDOM_SERVICE_TYPE)) {
						randomAlgorithms.add(item);
					}
					final String[]	resultRandomAlgorithms = randomAlgorithms.toArray(new String[randomAlgorithms.size()]); 
					
					Arrays.sort(resultRandomAlgorithms);
					return (T[])resultRandomAlgorithms;
				}
				else {
					return (T[]) RANDOM_ALGORITHMS;
				}
			default :
				return null;
		}
	}

	@Override
	public AvailableAndVisible getItemState(final ContentNodeMetadata meta) {
		switch (meta.getName()) {
			case "currentSalt" : case "password" : case "passwordRetype" :
				return usePassword ? AvailableAndVisible.AVAILABLE : AvailableAndVisible.NOTAVAILABLE;
			case "securityRandomProvider" : case "securityRandomAlgorithm" :
				return usePassword ? AvailableAndVisible.NOTAVAILABLE : AvailableAndVisible.AVAILABLE;
			default :
				return AvailableAndVisible.DEFAULT;
		}
	}

	@Override
	public FormManager<?, ?> getForEditor(final AskSecureKeyParameters inst, final Object id, final String fieldName, final Object... parameters) throws FlowException {
		return null;
	}

	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
		CipherKeyLength.KEY128.allowUnnamedModuleAccess(unnamedModules);
	}
	
	public SecretKey createSecretKey() throws InvalidKeySpecException, NoSuchAlgorithmException, UnsupportedEncodingException {
		final SecretKey		key;
		
		if (usePassword) {
			final byte[]		salt = currentSalt.getBytes(PureLibSettings.DEFAULT_CONTENT_ENCODING);
		    final PBEKeySpec 	pbeKeySpec = new PBEKeySpec(password, salt, iterations, cipherKeyLength.getKeyLength());
		    
		    key = SecretKeyFactory.getInstance(keyAlgorithm).generateSecret(pbeKeySpec);
		}
		else {
			final byte[]		secureRandomKeyBytes = new byte[cipherKeyLength.getKeyLength() / 8];
			final SecureRandom 	secureRandom = SecureRandom.getInstance(securityRandomAlgorithm);
			
		    secureRandom.nextBytes(secureRandomKeyBytes);
		    key = new SecretKeySpec(secureRandomKeyBytes, keyAlgorithm);
		}
		return key;
	}
	
	public static boolean isProviderSupportsSecretKeys(final String provider) {
		if (Utils.checkEmptyOrNullString(provider)) {
			throw new IllegalArgumentException("Provider to test can't be null or empty");
		}
		else if (AlgorithmRepo.getInstance().hasProvider(provider)) {
			return AlgorithmRepo.getInstance().hasService(provider, KEY_SERVICE_TYPE);
		}
		else {
			return false;
		}
	}

	public static boolean isProviderSupportsSecureRandoms(final String provider) {
		if (Utils.checkEmptyOrNullString(provider)) {
			throw new IllegalArgumentException("Provider to test can't be null or empty");
		}
		else if (AlgorithmRepo.getInstance().hasProvider(provider)) {
			return AlgorithmRepo.getInstance().hasService(provider, RANDOM_SERVICE_TYPE);
		}
		else {
			return false;
		}
	}
}
