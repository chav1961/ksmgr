package chav1961.ksmgr.dialogs;

import java.security.Provider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import chav1961.bt.security.AlgorithmUtils;
import chav1961.bt.security.AlgorithmUtils.AlgorithmDescriptor;
import chav1961.bt.security.interfaces.AlgorithmType;
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

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.dialogs.CreateKeystoreDialog/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.dialogs.createkeystoredialog",tooltip="chav1961.ksmgr.dialogs.createkeystoredialog.tt",help="chav1961.ksmgr.dialogs.createkeystoredialog.help")
public class CreateKeystoreDialog implements FormManager<Object, CreateKeystoreDialog> {
	private final LoggerFacade	facade;
	private final Provider		provider;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.createkeystoredialog.type",tooltip="chav1961.ksmgr.dialogs.createkeystoredialog.type.tt")
	@Format("30msd")
	public String				type = "unknown";

	@LocaleResource(value="chav1961.ksmgr.dialogs.createkeystoredialog.password",tooltip="chav1961.ksmgr.dialogs.createkeystoredialog.password.tt")
	@Format("30ms")
	public char[]				password = null;

	@LocaleResource(value="chav1961.ksmgr.dialogs.createkeystoredialog.passwordretype",tooltip="chav1961.ksmgr.dialogs.createkeystoredialog.passwordretype.tt")
	@Format("30ms")
	public char[]				passwordRetype = null;

	public CreateKeystoreDialog(final LoggerFacade facade, final Provider provider) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else if (provider == null || provider.isEmpty()) {
			throw new IllegalArgumentException("preferred provider can't be null or empty"); 
		}
		else {
			this.facade = facade;
			this.provider = provider;
		}
	}
	
	@Override
	public RefreshMode onField(final CreateKeystoreDialog inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		switch (fieldName) {
			case "password" : case "passwordRetype" :
				if (!Arrays.equals(password,passwordRetype)) {
					if (beforeCommit) {
						getLogger().message(Severity.error,"Password and retype password differ!");
						return RefreshMode.REJECT;
					}
					else {
						getLogger().message(Severity.warning,"Password and retype password differ!");
					}
				}
				else {
					getLogger().message(Severity.info,"Password and retype password identical");
				}
				return RefreshMode.DEFAULT;
			case "type" :
				if (!AlgorithmUtils.exists(provider, AlgorithmType.KEY_STORE, type)) {
					if (beforeCommit) {
						getLogger().message(Severity.error,"Unknown key store type ["+type+"]");
						return RefreshMode.REJECT;
					}
					else {
						getLogger().message(Severity.warning,"Unknown key store type ["+type+"]");
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
	public <T> T[] getForEditorContent(final CreateKeystoreDialog inst, final Object id, final String fieldName, final Object... parameters) throws FlowException {
		switch (fieldName) {
			case "type"	: 
				final Set<String>	algorithms = new HashSet<>();
				final String		prefix = ((String)parameters[0]).toUpperCase();

				for(AlgorithmDescriptor item : AlgorithmUtils.getAlgorithms(provider, AlgorithmType.KEY_STORE)) {
					if (item.getName().startsWith(prefix)) {
						algorithms.add(item.getName());
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
