package chav1961.ksmgr.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.SecretKey;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;

import chav1961.ksmgr.internal.KeyStoreWrapper;
import chav1961.ksmgr.utils.PasswordsRepo;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.ui.swing.useful.DnDManager;
import chav1961.purelib.ui.swing.useful.DnDManager.DnDInterface;
import chav1961.purelib.ui.swing.useful.DnDManager.DnDMode;

public class KeyStoreEditor extends JPanel implements LoggerFacadeOwner, LocalizerOwner, LocaleChangeListener, DnDInterface {
	private static final String			TITLE_NEW_FILE = "chav1961.ksmgr.gui.KeyStoreEditor.newFile";
	private static final String			TITLE_NEW_FILE_TT = "chav1961.ksmgr.gui.KeyStoreEditor.newFile.tt";
	private static final long 			serialVersionUID = 1L;

	private final Localizer 			localizer;
	private final LoggerFacade 			logger;
	private final KeyStoreWrapper 		wrapper;
	private final PasswordsRepo			repo;
	private final JLabel				caption = new JLabel();
	private final JList<AliasKeeper>	content = new JList<>();
	private final DnDManager			mgr = new DnDManager(this, this);

	public KeyStoreEditor(final Localizer localizer, final LoggerFacade logger, final KeyStoreWrapper wrapper, final PasswordsRepo repo) {
		super(new BorderLayout());
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (wrapper == null) {
			throw new NullPointerException("Keystore wrapper can't be null");
		}
		else if (repo == null) {
			throw new NullPointerException("Passwords repository can't be null");
		}
		else {
			setFocusable(true);
            enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK);
			
			this.localizer = localizer;
			this.logger = logger;
			this.wrapper = wrapper;
			this.repo = repo;
			
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
			content.setDragEnabled(true);
			content.setDropMode(DropMode.INSERT);
			
			content.setModel(new DefaultListModel<AliasKeeper>());
			content.setCellRenderer(this::getListCellRendererComponent);
			mgr.selectDnDMode(DnDMode.COPY);
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

	public KeyStoreWrapper getKeyStoreWrapper() {
		return wrapper;
	}
	
	public int placeSecretKey(final String entryName, final SecretKey secretKey, final char[] password, final boolean testUniqueName) throws KeyStoreException {
		if (Utils.checkEmptyOrNullString(entryName)) {
			throw new IllegalArgumentException("Enry name to place can't be null or empty");
		}
		else if (secretKey == null) {
			throw new NullPointerException("Secret key to place can't be null or empty");
		}
		else if (password == null) {
			throw new NullPointerException("Password can't be null or empty");
		}
		else if (testUniqueName && wrapper.keyStore.containsAlias(entryName)) {
			throw new KeyStoreException("Duplicate alias name ["+entryName+"] to place secret key");
		}
		else {
			final DefaultListModel<AliasKeeper>	model = (DefaultListModel<AliasKeeper>)content.getModel() ;
			final KeyStore.SecretKeyEntry 		secret = new KeyStore.SecretKeyEntry(secretKey);
			final KeyStore.ProtectionParameter	ppPassword = new KeyStore.PasswordProtection(password);
			final AliasKeeper					item = new AliasKeeper(AliasType.KEY, entryName); 
			
			wrapper.keyStore.setEntry(entryName, secret, ppPassword);
			model.addElement(item);
			return item.passwordId;
		}
	}

	@Override
	public Class<?> getSourceContentClass(DnDMode currentMode, Component component, int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getSourceContent(DnDMode currentMode, Component from, int xFrom, int yFrom, Component to, int xTo, int yTo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canReceive(DnDMode currentMode, Component from, int xFrom, int yFrom, Component to, int xTo, int yTo, Class<?> contentClass) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void complete(DnDMode currentMode, Component from, int xFrom, int yFrom, Component to, int xTo, int yTo, Object content) {
		// TODO Auto-generated method stub
		
	}
	
	
	private void fillContent(final JList<AliasKeeper> content, final KeyStore keyStore) {
		try {
			final DefaultListModel<AliasKeeper>	model = (DefaultListModel<AliasKeeper>)content.getModel() ;
			final Enumeration<String> 	aliases = keyStore.aliases();
			
			while (aliases.hasMoreElements()) {
				final String	item = aliases.nextElement();
				final AliasType	type;
				
				if (keyStore.entryInstanceOf(item, KeyStore.SecretKeyEntry.class)) {
					type = AliasType.KEY;
				}
				else if (keyStore.entryInstanceOf(item, KeyStore.PrivateKeyEntry.class)) {
					type = AliasType.KEY_PAIR;
				}
				else if (keyStore.entryInstanceOf(item, KeyStore.TrustedCertificateEntry.class)) {
					type = AliasType.CERTIFICATE;
				}
				else {
					type = AliasType.UNKNOWN;
				}
				model.addElement(new AliasKeeper(type, item));
			}
		} catch (KeyStoreException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
		
	}

	private Component getListCellRendererComponent(final JList<? extends AliasKeeper> list, final AliasKeeper value, final int index, final boolean isSelected, final boolean cellHasFocus) {
		final JLabel	label = new JLabel(value.name, value.type.getIcon(), JLabel.TRAILING);
		final String	passwordId = AliasKeeper.PASSWD_PREFIX+"."+value.passwordId; 
		
		label.setForeground(repo.hasPasswordFor(passwordId) ? Color.RED : Color.blue);
		label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
		if (cellHasFocus) {
			label.setBorder(new LineBorder(Color.BLUE));
		}
		
		return label;
	}
	
	
	private void fillLocalizedStrings() {
		final String	passwdId = KeyStoreWrapper.PASSWD_PREFIX+"."+wrapper.entryId;
		
		if (wrapper.file == null) {
			caption.setText(localizer.getValue(TITLE_NEW_FILE, wrapper.keyStore.getType()));
			caption.setToolTipText(localizer.getValue(TITLE_NEW_FILE_TT));
		}
		else {
			caption.setText(wrapper.file.getName()+" ("+wrapper.keyStore.getType()+")");
			caption.setToolTipText(wrapper.file.getAbsolutePath());
		}
		caption.setForeground(repo.hasPasswordFor(passwdId) ? Color.RED : Color.BLACK);
	}

	private static enum AliasType {
		KEY("key.png"),
		KEY_PAIR("keypair.png"),
		CERTIFICATE("certificate.png"),
		UNKNOWN("unknown.png");
		
		private final String	iconResourceName;
		private final Icon		icon;
		
		private AliasType(final String name) {
			this.iconResourceName = name;
			this.icon = new ImageIcon(getClass().getResource(name));
		}
		
		public String getIconResourceName() {
			return iconResourceName;
		}
		
		public Icon getIcon() {
			return icon;
		}
	}
	
	private static class AliasKeeper {
		private static final String			PASSWD_PREFIX = "alias";
		private static final AtomicInteger	unique = new AtomicInteger(1);
		
		final AliasType		type;
		final String		name;
		final int			passwordId = unique.incrementAndGet();
		
		private AliasKeeper(final AliasType type, final String name) {
			this.type = type;
			this.name = name;
		}
	}
}
