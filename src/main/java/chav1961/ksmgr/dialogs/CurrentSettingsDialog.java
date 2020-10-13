package chav1961.ksmgr.dialogs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator.LRUPersistence;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.dialogs.CurrentSettingsDialog/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.dialogs.settingsdialog",tooltip="chav1961.ksmgr.dialogs.settingsdialog.tt",help="help.aboutApplication")
public class CurrentSettingsDialog implements FormManager<Object, CurrentSettingsDialog>, LRUPersistence {
	public static final String	KEY_KEEP_PASSWORDS = "keepPasswords";
	public static final String	KEY_LRU_LIST = "LRUList";
	public static final String	KEY_CURRENT_LANG = "currentLang";
	
	private final LoggerFacade	facade;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.settingsdialog.keeppasswords",tooltip="chav1961.ksmgr.dialogs.settingsdialog.keeppasswords.tt")
	@Format("1m")
	public boolean				keepPasswords = true;

	public String				currentLang;
	
	public final String			configFile;
	public final SubstitutableProperties	props;
	
	public CurrentSettingsDialog(final LoggerFacade facade, final String configFile) throws NullPointerException, IllegalArgumentException, IOException {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else if (configFile == null || configFile.isEmpty()) {
			throw new IllegalArgumentException("Config file can't be null or empty"); 
		}
		else {
			final File		content = new File(configFile);
			
			this.facade = facade;
			this.configFile = configFile;
			this.props = content.exists() && content.isFile() && content.canRead() ? Utils.mkProps(content) : new SubstitutableProperties();
			this.keepPasswords = props.getProperty(KEY_KEEP_PASSWORDS, boolean.class, "true");
			this.currentLang = props.getProperty(KEY_CURRENT_LANG, String.class, Locale.getDefault().getLanguage());
		}
	}

	public void store() {
		final File		content = new File(configFile);

		props.setProperty(KEY_KEEP_PASSWORDS,String.valueOf(keepPasswords));
		props.setProperty(KEY_CURRENT_LANG,currentLang);
		
		if (!content.exists() || content.isFile() && content.canWrite()) {
			try(final OutputStream	os = new FileOutputStream(content)) {
				
				props.store(os,"");
			} catch (IOException e) {
				getLogger().message(Severity.error,"Error storing configuration into []: "+e.getLocalizedMessage());
			}
		}
		else {
			getLogger().message(Severity.error,"Error storing configuration: target file [] is nit a file or can't be written");
		}
	}
	
	@Override
	public RefreshMode onField(final CurrentSettingsDialog inst, final Object id, final String fieldName, final Object oldValue) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}

	@Override
	public LoggerFacade getLogger() {
		return facade;
	}

	@Override
	public void loadLRU(final List<String> lru) throws IOException {
		for (String name : props.getProperty(KEY_LRU_LIST,String.class,"").split("\\"+File.pathSeparatorChar)) {
			if (!name.isEmpty()) {
				lru.add(name);
			}
		}
	}

	@Override
	public void saveLRU(final List<String> lru) throws IOException {
		final StringBuilder	sb = new StringBuilder();
		
		for (String item : lru) {
			final File		f = new File(item);
			final String	path = f.getAbsolutePath().replace(File.separatorChar,'/');
			
			sb.append(File.pathSeparatorChar).append(path.charAt(0) == '/' ? path : "/"+path);
		}
		props.setProperty(KEY_LRU_LIST, sb.substring(1));
		store();
	}
}
