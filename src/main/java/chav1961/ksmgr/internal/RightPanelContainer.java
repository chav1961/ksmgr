package chav1961.ksmgr.internal;

import java.io.IOException;
import java.security.KeyStore;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import chav1961.ksmgr.Application;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.JFileList;

public class RightPanelContainer implements AutoCloseable, LocaleChangeListener {
	public enum RightPanelType {
		AS_KEYSTORE,
		AS_FILESYSTEM,
		UNKNOWN
	}
	
	private final Application 			app;
	private final Localizer				localizer;
	private final LoggerFacade			logger;
	private final FileSystemInterface	fsi;
	private final JSplitPane			container;
	
	private RightPanelType				type = RightPanelType.UNKNOWN;
	private JComponent					component = new JLabel();
	private KeyStore					keystore = null;
	
	public RightPanelContainer(final Application app, final Localizer localizer, final LoggerFacade logger, final FileSystemInterface fsi, final JSplitPane container) {
		if (app == null) {
			throw new NullPointerException("Application can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (fsi == null) {
			throw new NullPointerException("File system interface can't be null"); 
		}
		else if (container == null) {
			throw new NullPointerException("Split pane container can't be null"); 
		}
		else {
			this.app = app;
			this.localizer = localizer;
			this.logger = logger;
			this.fsi = fsi;
			this.container = container;
			container.setRightComponent(component);
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		SwingUtils.refreshLocale(component,oldLocale,newLocale);
	}
	
	@Override
	public void close() throws Exception {
		fsi.close();
	}

	public void setPanelTypeAsKeystore(final ContentNodeMetadata root, final String fileName, final KeyStore keystore) {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty"); 
		}
		else if (keystore == null) {
			throw new NullPointerException("Key store can't be null"); 
		}
		else {
			setPanelType(RightPanelType.AS_KEYSTORE,new KeyStoreViewer(root, localizer, logger, null, fileName, keystore));
			this.keystore = keystore;
		}
	}

	public void setPanelTypeAsFileSystem(final JFileList list) {
		if (list == null) {
			throw new NullPointerException("File list can't be null"); 
		}
		else {
			setPanelType(RightPanelType.AS_FILESYSTEM,list);
		}
	}

	public RightPanelType getPanelType() {
		return type;
	}

	public void refresh() {
		switch (getPanelType()) {
			case AS_FILESYSTEM	:
				try{((JFileList)component).refresh();
				} catch (IOException e) {
					logger.message(Severity.error,"Error refreshilg right panel: "+e.getLocalizedMessage());
				}
				break;
			case AS_KEYSTORE	:
				((KeyStoreViewer)component).refresh();
				break;
			case UNKNOWN		:
				break;
			default:
				throw new UnsupportedOperationException("Panel type ["+getPanelType()+"] is not supported yet");
		}
	}
	
	public String getCurrentFileSystemPath() {
		if (getPanelType() != RightPanelType.AS_FILESYSTEM) {
			throw new IllegalStateException("Current panel type is not a ["+RightPanelType.AS_FILESYSTEM+"], and this method can't be called");
		}
		else {
			return ((JFileList)component).getCurrentLocation();
		}
	}

	public String getCurrentFileSystemFile() {
		if (getPanelType() != RightPanelType.AS_FILESYSTEM) {
			throw new IllegalStateException("Current panel type is not a ["+RightPanelType.AS_FILESYSTEM+"], and this method can't be called");
		}
		else {
			return ((JFileList)component).getCurrentSelection();
		}
	}
	
	public KeyStore getKeyStore() {
		if (getPanelType() != RightPanelType.AS_KEYSTORE) {
			throw new IllegalStateException("Current panel type is not a ["+RightPanelType.AS_KEYSTORE+"], and this method can't be called");
		}
		else {
			return keystore;
		}
	}

	private void setPanelType(final RightPanelType type, final JComponent content) {
		if (type == null) {
			throw new NullPointerException("Panel type can't be null"); 
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else {
			this.type = type;
			this.component = content;
			container.setRightComponent(new JScrollPane(content));
		}
	}
	
	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}
}
