package chav1961.ksmgr.gui;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.gui.AskRenameItem/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.gui.askrenameitemdialog",tooltip="chav1961.ksmgr.gui.askrenameitemdialog.tt",help="chav1961.ksmgr.gui.askrenameitemdialog.help")
public class AskRenameItem implements FormManager<Object, AskRenameItem>, ModuleAccessor {
	private final LoggerFacade	facade;
	
	@LocaleResource(value="chav1961.ksmgr.gui.askrenameitemdialog.newname",tooltip="chav1961.ksmgr.gui.askrenameitemdialog.newname.tt")
	@Format("30ms")
	public String		newName = "";

	public AskRenameItem(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}
	
	@Override
	public RefreshMode onField(final AskRenameItem inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
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
