package chav1961.ksmgr.dialogs;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import chav1961.ksmgr.interfaces.CipherKeyLength;
import chav1961.ksmgr.interfaces.ImportEntityType;
import chav1961.ksmgr.internal.AlgorithmRepo;
import chav1961.ksmgr.keystore.KeyStoreUtils;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.dialogs.KeyImportDialog/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.dialogs.keyimportdialog",tooltip="chav1961.ksmgr.dialogs.keyimportdialog.tt",help="chav1961.ksmgr.dialogs.keyimportdialog.help")
public class KeyImportDialog implements FormManager<Object, KeyImportDialog> {
	private static final String	ALGORITHM_TYPE = "SecretKeyFactory";
	
	private final LoggerFacade 	facade;
	private final KeyStore 		ks;
	private final AlgorithmRepo	repo;
	private final String		provider;

	@LocaleResource(value="chav1961.ksmgr.dialogs.keyimportdialog.entitytype",tooltip="chav1961.ksmgr.dialogs.keyimportdialog.entitytype.tt")
	@Format("30smd")
	public ImportEntityType		type = ImportEntityType.CERTIFICATE;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.keyimportdialog.keyalgorithm",tooltip="chav1961.ksmgr.dialogs.keyimportdialog.keyalgorithm.tt")
	@Format("30smd")
	public String				keyAlgorithm = "unknown";

	@LocaleResource(value="chav1961.ksmgr.dialogs.keyimportdialog.alias",tooltip="chav1961.ksmgr.dialogs.keyimportdialog.alias.tt")
	@Format("30ms")
	public String				alias = "myAlias";

	public KeyImportDialog(final LoggerFacade facade, final KeyStore ks, final AlgorithmRepo repo, final String provider) {
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
		else {
			this.facade = facade;
			this.ks = ks;
			this.repo = repo;
			this.provider = provider;
		}
	}

	@Override
	public RefreshMode onField(final KeyImportDialog inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		switch (fieldName) {
			case "alias"	:
				try{if (KeyStoreUtils.isAliasInKeyStore(alias, ks)) {
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
			default :
				return RefreshMode.DEFAULT;
		}
	}

	@Override
	public LoggerFacade getLogger() {
		return facade;
	}
	
	@Override
	public <T> T[] getForEditorContent(final KeyImportDialog inst, final Object id, final String fieldName, final Object... parameters) throws FlowException {
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
