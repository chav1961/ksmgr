package chav1961.ksmgr.gui;


import java.util.Arrays;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.gui.AskNewPassword/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.gui.asknewpassword",tooltip="chav1961.ksmgr.gui.asknewpassword.tt",help="chav1961.ksmgr.dialogs.asknewpassword.help")
public class AskNewPassword implements FormManager<Object, AskNewPassword>, ModuleAccessor {
	public static final String	ERROR_DIFFERENT_PASSWORDS = "error.asknewpassword.different.passwords";
	public static final String	INFO_IDENTICAL_PASSWORDS = "info.asknewpassword.identical.passwords";
	
	private final LoggerFacade	facade;
	private final Localizer		localizer;
	
	@LocaleResource(value="chav1961.ksmgr.gui.asknewpassword.password",tooltip="chav1961.ksmgr.gui.asknewpassword.password.tt")
	@Format("30ms")
	public char[]				password = null;

	@LocaleResource(value="chav1961.ksmgr.gui.asknewpassword.duplicate",tooltip="chav1961.ksmgr.gui.asknewpassword.duplicate.tt")
	@Format("30ms")
	public char[]				duplicate = null;
	
	public AskNewPassword(final LoggerFacade facade, final Localizer localizer) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
			this.localizer = localizer;
		}
	}
	
	@Override
	public RefreshMode onField(final AskNewPassword inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		switch (fieldName) {
			case "password" : case "duplicate" :
				if (!Arrays.equals(password, duplicate)) {
					if (beforeCommit) {
						getLogger().message(Severity.error, localizer.getValue(ERROR_DIFFERENT_PASSWORDS));
						return RefreshMode.REJECT;
					}
					else {
						getLogger().message(Severity.warning, localizer.getValue(ERROR_DIFFERENT_PASSWORDS));
					}
				}
				else {
					getLogger().message(Severity.info, localizer.getValue(INFO_IDENTICAL_PASSWORDS));
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
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}
}
