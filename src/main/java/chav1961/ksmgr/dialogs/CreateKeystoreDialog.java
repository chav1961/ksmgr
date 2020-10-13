package chav1961.ksmgr.dialogs;

import java.util.Arrays;

import chav1961.ksmgr.interfaces.KeyStoreType;
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
@LocaleResource(value="chav1961.ksmgr.dialogs.createkeystoredialog",tooltip="chav1961.ksmgr.dialogs.createkeystoredialog.tt",help="help.aboutApplication")
public class CreateKeystoreDialog implements FormManager<Object, CreateKeystoreDialog> {
	private final LoggerFacade	facade;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.createkeystoredialog.type",tooltip="chav1961.ksmgr.dialogs.createkeystoredialog.type.tt")
	@Format("30m")
	public KeyStoreType			type = KeyStoreType.PKCS12;

	@LocaleResource(value="chav1961.ksmgr.dialogs.createkeystoredialog.password",tooltip="chav1961.ksmgr.dialogs.createkeystoredialog.password.tt")
	@Format("30ms")
	public char[]				password = null;

	@LocaleResource(value="chav1961.ksmgr.dialogs.createkeystoredialog.passwordretype",tooltip="chav1961.ksmgr.dialogs.createkeystoredialog.passwordretype.tt")
	@Format("30ms")
	public char[]				passwordRetype = null;

	public CreateKeystoreDialog(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}
	
	@Override
	public RefreshMode onField(final CreateKeystoreDialog inst, final Object id, final String fieldName, final Object oldValue) throws FlowException, LocalizationException {
		if (!Arrays.equals(password,passwordRetype)) {
			getLogger().message(Severity.warning,"Password and retype password differ!");
		}
		else {
			getLogger().message(Severity.info,"Password and retype password identical");
		}
		return RefreshMode.DEFAULT;
	}

	@Override
	public LoggerFacade getLogger() {
		return facade;
	}
}
