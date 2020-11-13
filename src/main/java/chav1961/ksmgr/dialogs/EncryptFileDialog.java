package chav1961.ksmgr.dialogs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import chav1961.ksmgr.interfaces.CipherKeyLength;
import chav1961.ksmgr.internal.AlgorithmRepo;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.dialogs.EncryptFileDialog/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.dialogs.encryptfiledialog",tooltip="chav1961.ksmgr.dialogs.encryptfiledialog.tt",help="help.aboutApplication")
public class EncryptFileDialog implements FormManager<Object, EncryptFileDialog> {
	private final LoggerFacade	facade;
	private final AlgorithmRepo	repo;
	private final String		provider;

	@LocaleResource(value="chav1961.ksmgr.dialogs.encryptfiledialog.cipheralgorithm",tooltip="chav1961.ksmgr.dialogs.encryptfiledialog.cipheralgorithm.tt")
	@Format("30smdN")
	public String				cipherAlgorithm;

	@LocaleResource(value="chav1961.ksmgr.dialogs.encryptfiledialog.cipheralgorithmsuffix",tooltip="chav1961.ksmgr.dialogs.encryptfiledialog.cipheralgorithmsuffix.tt")
	@Format("30sm")
	public String				cipherAlgorithmSuffix = "CTR/NOPADDING";
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.encryptfiledialog.cipherkeylength",tooltip="chav1961.ksmgr.dialogs.encryptfiledialog.cipherkeylength.tt")
	@Format("30sm")
	public CipherKeyLength		cipherKeyLength = CipherKeyLength.KEY256;

	@LocaleResource(value="chav1961.ksmgr.dialogs.encryptfiledialog.iterations",tooltip="chav1961.ksmgr.dialogs.encryptfiledialog.iterations.tt")
	@Format("7sm")
	public int					iterations = 1000;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.encryptfiledialog.currentsalt",tooltip="chav1961.ksmgr.dialogs.encryptfiledialog.currentsalt.tt")
	@Format("30sm")
	public String				currentSalt;

	@LocaleResource(value="chav1961.ksmgr.dialogs.encryptfiledialog.currentrandomseed",tooltip="chav1961.ksmgr.dialogs.encryptfiledialog.currentrandomseed.tt")
	@Format("30sm")
	public long					currentRandomSeed;
	
	public EncryptFileDialog(final LoggerFacade facade, final AlgorithmRepo repo, final String provider, final String currentSalt, final long currentRandomSeed) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else if (repo == null) {
			throw new NullPointerException("Algorithm repo can't be null"); 
		}
		else if (provider == null || provider.isEmpty()) {
			throw new IllegalArgumentException("Algorithm provider can't be null or empty"); 
		}
		else if (currentSalt == null) {
			throw new NullPointerException("Current salt can't be null"); 
		}
		else {
			this.facade = facade;
			this.repo = repo;
			this.provider = provider;
			this.currentSalt = currentSalt;
			this.currentRandomSeed = currentRandomSeed;
		}
	}

	@Override
	public RefreshMode onField(final EncryptFileDialog inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		switch (fieldName) {
			case "cipherAlgorithm"			:
				if (cipherAlgorithm.trim().isEmpty()) {
					getLogger().message(Severity.warning,"Field must be filled!");
					return RefreshMode.REJECT;
				}
				else {
					return RefreshMode.DEFAULT;
				}
			case "cipherAlgorithmSuffix"	:
				if (cipherAlgorithmSuffix.trim().isEmpty()) {
					getLogger().message(Severity.warning,"Field must be filled!");
					return RefreshMode.REJECT;
				}
				else {
					return RefreshMode.DEFAULT;
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
	public <T> T[] getForEditorContent(final EncryptFileDialog inst, final Object id, final String fieldName, final Object... parameters) throws FlowException {
		switch (fieldName) {
			case "cipherAlgorithm"	: 
				final Set<String>	algorithms = new HashSet<>();
				final String		prefix = ((String)parameters[0]).toUpperCase();

				for (String item : repo.getAlgorithms("Cipher",provider)) {
					if (item.toUpperCase().startsWith(prefix)) {
						algorithms.add(item);
					}
				}
				return (T[])algorithms.toArray(new String[algorithms.size()]);
			default :
				return null;
		}
	} 
}
