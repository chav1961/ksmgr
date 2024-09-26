package chav1961.ksmgr.gui;

import java.security.Provider;
import java.security.Security;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.interfaces.UIFormManager;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.gui.SettingsDialog/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.gui.settingsdialog",tooltip="chav1961.ksmgr.gui.settingsdialog.tt",help="chav1961.ksmgr.dialogs.settingsdialog.help")
public class SettingsDialog implements FormManager<Object, SettingsDialog>, ModuleAccessor, UIFormManager<Object, SettingsDialog> {
	public static final String	SETTINGS_KEEP_PASSWORD = "keepPassword";
	public static final String	SETTINGS_PREFERRED_PROVIDER = "preferredProvider";
	public static final String	SETTINGS_PRINCIPAL_NAME = "principalName";
	public static final String	SETTINGS_CURRENT_SALT = "currentSalt";
	public static final String	SETTINGS_CURRENT_RANDOM_SEED = "currentRandomSeed";
	
	private final LoggerFacade	facade;
	
	@LocaleResource(value="chav1961.ksmgr.gui.settingsdialog.keeppasswords",tooltip="chav1961.ksmgr.gui.settingsdialog.keeppasswords.tt")
	@Format("30ms")
	public boolean				keepPasswords = false;

	@LocaleResource(value="chav1961.ksmgr.gui.settingsdialog.preferredprovider",tooltip="chav1961.ksmgr.gui.settingsdialog.preferredprovider.tt")
	@Format("30msd")
	public String				preferredProvider = "";

	@LocaleResource(value="chav1961.ksmgr.gui.settingsdialog.principalname",tooltip="chav1961.ksmgr.gui.settingsdialog.principalname.tt")
	@Format("30ms")
	public String				principalName = System.getProperty("user.name");

	@LocaleResource(value="chav1961.ksmgr.gui.settingsdialog.currentsalt",tooltip="chav1961.ksmgr.gui.settingsdialog.currentsalt.tt")
	@Format("30ms")
	public String				currentSalt = "";

	@LocaleResource(value="chav1961.ksmgr.gui.settingsdialog.currentrandomseed",tooltip="chav1961.ksmgr.gui.settingsdialog.currentrandomseed.tt")
	@Format("30ms")
	public long					currentRandomSeed = 0;

	public SettingsDialog(final LoggerFacade facade) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			this.facade = facade;
		}
	}
	
	@Override
	public RefreshMode onField(final SettingsDialog inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException {
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
	
	public void loadSettings(final SubstitutableProperties props) {
		if (props == null) {
			throw new NullPointerException("Properties to load cant be null");
		}
		else {
			if (props.containsKey(SETTINGS_KEEP_PASSWORD)) {
				keepPasswords = props.getProperty(SETTINGS_KEEP_PASSWORD, boolean.class);
			}
			if (props.containsKey(SETTINGS_PREFERRED_PROVIDER)) {
				preferredProvider = props.getProperty(SETTINGS_PREFERRED_PROVIDER, String.class);
			}
			if (props.containsKey(SETTINGS_PRINCIPAL_NAME)) {
				principalName = props.getProperty(SETTINGS_PRINCIPAL_NAME, String.class);
			}
			if (props.containsKey(SETTINGS_CURRENT_SALT)) {
				currentSalt = props.getProperty(SETTINGS_CURRENT_SALT, String.class);
			}
			if (props.containsKey(SETTINGS_CURRENT_RANDOM_SEED)) {
				currentRandomSeed = props.getProperty(SETTINGS_CURRENT_RANDOM_SEED, long.class);
			}
		}
	}

	public void storeSettings(final SubstitutableProperties props) {
		if (props == null) {
			throw new NullPointerException("Properties to load cant be null");
		}
		else {
			props.setProperty(SETTINGS_KEEP_PASSWORD, String.valueOf(keepPasswords));
			props.setProperty(SETTINGS_PREFERRED_PROVIDER, preferredProvider);
			props.setProperty(SETTINGS_PRINCIPAL_NAME, principalName);
			props.setProperty(SETTINGS_CURRENT_SALT, currentSalt);
			props.setProperty(SETTINGS_CURRENT_RANDOM_SEED, String.valueOf(currentRandomSeed));
		}
	}

	@Override
	public AvailableAndVisible getItemState(final ContentNodeMetadata meta) {
		return AvailableAndVisible.DEFAULT;
	}

	@Override
	public FormManager<?, ?> getForEditor(final SettingsDialog inst, final Object id, final String fieldName, final Object... parameters) throws FlowException {
		return null;
	}

	@Override
	public <T> T[] getForEditorContent(final SettingsDialog inst, final Object id, final String fieldName, final Object... parameters) throws FlowException {
		if ("preferredProvider".equalsIgnoreCase(fieldName)) {
			final Provider[] 	providers = Security.getProviders();
			final String[]		names = new String[providers.length];
			
			for(int index = 0; index < names.length; index++) {
				names[index] = providers[index].getName();
			}
			return (T[]) names;
		}
		else {
			return null;
		}
	}
}
