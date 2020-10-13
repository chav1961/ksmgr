package chav1961.ksmgr.dialogs;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.dialogs.AskPasswordDialog/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.dialogs.askpassworddialog",tooltip="chav1961.ksmgr.dialogs.askpassworddialog.tt",help="help.aboutApplication")
public class AskPasswordDialog implements FormManager<Object, AskPasswordDialog> {
	private final LoggerFacade	facade;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.askpassworddialog.password",tooltip="chav1961.ksmgr.dialogs.askpassworddialog.password.tt")
	@Format("30s")
	public char[]				password = null;

	public AskPasswordDialog(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}
	
	@Override
	public RefreshMode onField(final AskPasswordDialog inst, final Object id, final String fieldName, final Object oldValue) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}

	@Override
	public LoggerFacade getLogger() {
		return facade;
	}

}
