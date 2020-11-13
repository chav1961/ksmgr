package chav1961.ksmgr.dialogs;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.dialogs.OpenKeystoreDialog/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.dialogs.openkeystoredialog",tooltip="chav1961.ksmgr.dialogs.openkeystoredialog.tt",help="help.aboutApplication")
public class OpenKeystoreDialog implements FormManager<Object, OpenKeystoreDialog> {
	private final LoggerFacade	facade;

	@LocaleResource(value="chav1961.ksmgr.dialogs.openkeystoredialog.file",tooltip="chav1961.ksmgr.dialogs.openkeystoredialog.file.tt")
	@Format("30r")
	public String				file;

	@LocaleResource(value="chav1961.ksmgr.dialogs.openkeystoredialog.password",tooltip="chav1961.ksmgr.dialogs.openkeystoredialog.password.tt")
	@Format("30ms")
	public char[]				password = null;

	public OpenKeystoreDialog(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}

	@Override
	public RefreshMode onField(final OpenKeystoreDialog inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}

	@Override
	public LoggerFacade getLogger() {
		return facade;
	}
}
