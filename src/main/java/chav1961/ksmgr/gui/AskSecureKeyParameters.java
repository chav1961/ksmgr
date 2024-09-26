package chav1961.ksmgr.gui;

import chav1961.ksmgr.interfaces.CipherKeyLength;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.gui.AskPassword/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparametersdialog",tooltip="chav1961.ksmgr.gui.asksecurekeyparametersdialog.tt",help="chav1961.ksmgr.dialogs.asksecurekeyparametersdialog.help")
public class AskSecureKeyParameters implements FormManager<Object, AskSecureKeyParameters>, ModuleAccessor {
	private final LoggerFacade	facade;

	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparametersdialog.name",tooltip="chav1961.ksmgr.gui.asksecurekeyparametersdialog.name.tt")
	@Format("30ms")
	public String				name = "newSecretKey";
	
	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparametersdialog.length",tooltip="chav1961.ksmgr.gui.asksecurekeyparametersdialog.length.tt")
	@Format("30ms")
	public CipherKeyLength		length = CipherKeyLength.KEY128;

	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparametersdialog.useSecureRandom",tooltip="chav1961.ksmgr.gui.asksecurekeyparametersdialog.useSecureRandom.tt")
	@Format("1m")
	public boolean				useSecureRandom = true;
	
	@LocaleResource(value="chav1961.ksmgr.gui.asksecurekeyparametersdialog.usePassword",tooltip="chav1961.ksmgr.gui.asksecurekeyparametersdialog.usePassword.tt")
	@Format("1m")
	public boolean				usePassword = true;
	

	public AskSecureKeyParameters(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}
	
	
	@Override
	public RefreshMode onField(AskSecureKeyParameters inst, Object id, String fieldName, Object oldValue, boolean beforeCommit) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}

	@Override
	public LoggerFacade getLogger() {
		return facade;
	}

	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}
}
