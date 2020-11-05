package chav1961.ksmgr.dialogs;

import java.net.URI;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.dialogs.AskServerURIDialog/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.dialogs.askserveruridialog",tooltip="chav1961.ksmgr.dialogs.askserveruridialog.tt",help="help.aboutApplication")
public class AskServerURIDialog implements FormManager<Object, AskServerURIDialog> {
	private final LoggerFacade	facade;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.askserveruridialog.uri",tooltip="chav1961.ksmgr.dialogs.askserveruridialog.uri.tt")
	@Format("30s")
	public URI			serverURI = URI.create("https://someshit.com");

	public AskServerURIDialog(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}
	
	@Override
	public RefreshMode onField(AskServerURIDialog inst, Object id, String fieldName, Object oldValue) throws FlowException, LocalizationException {
		if (serverURI.getPort() <= 0) {
			getLogger().message(Severity.warning, "Port number missing (usually 433 required)");
			return RefreshMode.REJECT;
		}
		else {
			return RefreshMode.DEFAULT;
		}
	}

	@Override
	public LoggerFacade getLogger() {
		return facade;
	}

}
