package chav1961.ksmgr.dialogs;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import chav1961.ksmgr.interfaces.CipherKeyLength;
import chav1961.ksmgr.internal.AlgorithmRepo;
import chav1961.ksmgr.internal.KeyStoreUtils;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.dialogs.KeyCreateDialog/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.dialogs.keycreatedialog",tooltip="chav1961.ksmgr.dialogs.keycreatedialog.tt",help="chav1961.ksmgr.dialogs.keycreatedialog.help")
public class KeyCreateDialog implements FormManager<Object, KeyCreateDialog> {
	private static final String	ALGORITHM_TYPE = "SecretKeyFactory";
	
	private final LoggerFacade 	facade;
	private final KeyStore 		ks;
	private final AlgorithmRepo	repo;
	private final String		provider;

	@LocaleResource(value="chav1961.ksmgr.dialogs.keycreatedialog.keyalgorithm",tooltip="chav1961.ksmgr.dialogs.keycreatedialog.keyalgorithm.tt")
	@Format("30smd")
	public String				keyAlgorithm = "unknown";

	@LocaleResource(value="chav1961.ksmgr.dialogs.keycreatedialog.keylength",tooltip="chav1961.ksmgr.dialogs.keycreatedialog.keylength.tt")
	@Format("30sm")
	public CipherKeyLength		cipherKeyLength = CipherKeyLength.KEY256;

	@LocaleResource(value="chav1961.ksmgr.dialogs.keycreatedialog.iterations",tooltip="chav1961.ksmgr.dialogs.keycreatedialog.iterations.tt")
	@Format("7sm")
	public int					iterations = 1000;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.keycreatedialog.currentsalt",tooltip="chav1961.ksmgr.dialogs.keycreatedialog.currentsalt.tt")
	@Format("30sm")
	public String				currentSalt;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.keycreatedialog.password",tooltip="chav1961.ksmgr.dialogs.keycreatedialog.password.tt")
	@Format("30ms")
	public char[]				password = null;

	@LocaleResource(value="chav1961.ksmgr.dialogs.keycreatedialog.passwordretype",tooltip="chav1961.ksmgr.dialogs.keycreatedialog.passwordretype.tt")
	@Format("30ms")
	public char[]				passwordRetype = null;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.keycreatedialog.alias",tooltip="chav1961.ksmgr.dialogs.keycreatedialog.alias.tt")
	@Format("30ms")
	public String				alias = "myAlias";

	public KeyCreateDialog(final LoggerFacade facade, final KeyStore ks, final AlgorithmRepo repo, final String provider, final String currentSalt) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else if (ks == null) {
			throw new NullPointerException("Key store can't be null"); 
		}
		else if (repo == null) {
			throw new NullPointerException("Algorithm repo can't be null"); 
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
			this.repo = repo;
			this.provider = provider;
			this.currentSalt = currentSalt;
		}
	}

	@Override
	public RefreshMode onField(final KeyCreateDialog inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		switch (fieldName) {
			case "alias"	:
				try{if (KeyStoreUtils.isAliasInKeyStore(alias, ks)) {
						getLogger().message(Severity.warning,"Alias ["+alias+"] already exists in the store");
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
				if (!Arrays.equals(password,passwordRetype)) {
					getLogger().message(Severity.warning,"Password and retype password differ!");
				}
				else {
					getLogger().message(Severity.info,"Password and retype password identical");
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
	public <T> T[] getForEditorContent(final KeyCreateDialog inst, final Object id, final String fieldName, final Object... parameters) throws FlowException {
		switch (fieldName) {
			case "keyAlgorithm"	: 
				final Set<String>	algorithms = new HashSet<>();
				final String		prefix = ((String)parameters[0]).toUpperCase();

				for (String item : repo.getAlgorithms(ALGORITHM_TYPE,provider)) {
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

}
