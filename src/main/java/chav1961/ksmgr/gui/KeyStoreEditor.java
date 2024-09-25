package chav1961.ksmgr.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.security.KeyStore;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import chav1961.ksmgr.internal.KeyStoreWrapper;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;

public class KeyStoreEditor extends JPanel implements LoggerFacadeOwner, LocalizerOwner, LocaleChangeListener {
	private static final String		TITLE_NEW_FILE = "chav1961.ksmgr.gui.KeyStoreEditor.newFile";
	private static final String		TITLE_NEW_FILE_TT = "chav1961.ksmgr.gui.KeyStoreEditor.newFile.tt";
	private static final long serialVersionUID = 1L;

	private final Localizer 		localizer;
	private final LoggerFacade 		logger;
	private final KeyStoreWrapper 	wrapper;
	private final JLabel			caption = new JLabel();
	private final JList<?>			content = new JList<>();

	public KeyStoreEditor(final Localizer localizer, final LoggerFacade logger, final KeyStoreWrapper wrapper) {
		super(new BorderLayout());
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (wrapper == null) {
			throw new NullPointerException("Keystore wwrapper can't be null");
		}
		else {
			setFocusable(true);
            enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK);
			
			this.localizer = localizer;
			this.logger = logger;
			this.wrapper = wrapper;
			
			add(caption, BorderLayout.NORTH);
			add(new JScrollPane(content), BorderLayout.CENTER);
			
			content.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					for(FocusListener item : KeyStoreEditor.this.getFocusListeners()) {
						item.focusLost(e);
					}
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					for(FocusListener item : KeyStoreEditor.this.getFocusListeners()) {
						item.focusGained(e);
					}
				}
			});
			
			fillContent(content, wrapper.keyStore);
			fillLocalizedStrings();
		}
	}


	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}

	@Override
	public LoggerFacade getLogger() {
		return logger;
	}

	private void fillContent(final JList content, final KeyStore keyStore) {
		// TODO Auto-generated method stub
	}
	
	private void fillLocalizedStrings() {
		if (wrapper.file == null) {
			caption.setText(localizer.getValue(TITLE_NEW_FILE, wrapper.keyStore.getType()));
			caption.setToolTipText(localizer.getValue(TITLE_NEW_FILE_TT));
		}
		else {
			caption.setText(wrapper.file.getName()+" ("+wrapper.keyStore.getType()+")");
			caption.setToolTipText(wrapper.file.getAbsolutePath());
		}
	}
}
