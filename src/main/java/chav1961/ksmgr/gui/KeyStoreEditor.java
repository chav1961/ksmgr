package chav1961.ksmgr.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.SecretKey;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import chav1961.ksmgr.interfaces.AliasDescriptor;
import chav1961.ksmgr.interfaces.KeyStoreEntityType;
import chav1961.ksmgr.interfaces.SelectedWindows;
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
import chav1961.purelib.ui.swing.useful.FileTransferable;
import chav1961.purelib.ui.swing.useful.JFileItemDescriptor;

public class KeyStoreEditor extends JPanel implements LoggerFacadeOwner, LocalizerOwner, LocaleChangeListener {
	private static final String			TITLE_NEW_FILE = "chav1961.ksmgr.gui.KeyStoreEditor.newFile";
	private static final String			TITLE_NEW_FILE_TT = "chav1961.ksmgr.gui.KeyStoreEditor.newFile.tt";
	private static final long 			serialVersionUID = 1L;

	private final Localizer 			localizer;
	private final LoggerFacade 			logger;
	private final SelectedWindows		place;
	private final KeyStoreWrapper 		wrapper;
	private final PasswordsRepo			repo;
	private final JLabel				caption = new JLabel();
	private final JButton				save = new JButton();
	private final JList<AliasKeeper>	content = new JList<>();

	public KeyStoreEditor(final Localizer localizer, final LoggerFacade logger, final SelectedWindows place, final KeyStoreWrapper wrapper, final PasswordsRepo repo) {
		super(new BorderLayout());
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (place == null) {
			throw new NullPointerException("Place can't be null");
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
			this.place = place;
			this.wrapper = wrapper;
			this.repo = repo;
			
			final JPanel	topPanel = new JPanel(new BorderLayout(3, 3));
			
			topPanel.add(caption, BorderLayout.CENTER);
			topPanel.add(save, BorderLayout.EAST);
			
			add(topPanel, BorderLayout.NORTH);
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
			content.setDropMode(DropMode.ON_OR_INSERT);
			content.setModel(new DefaultListModel<AliasKeeper>());
			content.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			content.setCellRenderer(this::getListCellRendererComponent);

			DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(content, DnDConstants.ACTION_COPY, new DragGestureHandler(content));
			
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

	public void addActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			save.addActionListener(listener);
		}
	}

	public void removeActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			save.removeActionListener(listener);
		}
	}
	
	public void addSelectionListener(final ListSelectionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			content.addListSelectionListener(listener);
		}
	}

	public void removeSelectionListener(final ListSelectionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			content.removeListSelectionListener(listener);
		}
	}
	
	public boolean hasAnySelection() {
		return content.getSelectedIndex() != -1;
	}
	
	public int getSelectionCount() {
		return content.getSelectedValuesList().size();
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
			final AliasKeeper					item = new AliasKeeper(KeyStoreEntityType.SECRET_KEY, entryName); 
			
			wrapper.keyStore.setEntry(entryName, secret, ppPassword);
			model.addElement(item);
			return item.passwordId;
		}
	}

	private void fillContent(final JList<AliasKeeper> content, final KeyStore keyStore) {
		try {
			final DefaultListModel<AliasKeeper>	model = (DefaultListModel<AliasKeeper>)content.getModel() ;
			final Enumeration<String> 	aliases = keyStore.aliases();
			
			while (aliases.hasMoreElements()) {
				final String				item = aliases.nextElement();
				final KeyStoreEntityType	type;
				
				if (keyStore.entryInstanceOf(item, KeyStore.SecretKeyEntry.class)) {
					type = KeyStoreEntityType.SECRET_KEY;
				}
				else if (keyStore.entryInstanceOf(item, KeyStore.PrivateKeyEntry.class)) {
					type = KeyStoreEntityType.KEY_PAIR;
				}
				else if (keyStore.entryInstanceOf(item, KeyStore.TrustedCertificateEntry.class)) {
					type = KeyStoreEntityType.CERTIFICATE;
				}
				else {
					type = KeyStoreEntityType.UNKNOWN;
				}
				model.addElement(new AliasKeeper(type, item));
			}
		} catch (KeyStoreException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
		
	}

	private Component getListCellRendererComponent(final JList<? extends AliasKeeper> list, final AliasKeeper value, final int index, final boolean isSelected, final boolean cellHasFocus) {
		final JLabel	label = new JLabel(value.name, value.type.getIcon(), JLabel.LEADING);
		final String	passwordId = AliasKeeper.PASSWD_PREFIX+"."+value.passwordId; 
		
		label.setOpaque(true);
		if (isSelected) {
			label.setBackground(list.getSelectionBackground());
			label.setForeground(repo.hasPasswordFor(passwordId) ? Color.RED : list.getSelectionForeground());
		}
		else {
			label.setBackground(list.getBackground());
			label.setForeground(repo.hasPasswordFor(passwordId) ? Color.RED : list.getForeground());
		}
		if (cellHasFocus) {
			label.setBorder(new LineBorder(Color.BLUE));
		}
		
		return label;
	}
	
	private void fillLocalizedStrings() {
		final String	passwdId = PasswordsRepo.KEY_STORE_PREFIX+'.'+wrapper.entryId;
		
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

	private static class AliasKeeper {
		private static final String			PASSWD_PREFIX = "alias";
		private static final AtomicInteger	unique = new AtomicInteger(1);
		
		final KeyStoreEntityType	type;
		final String				name;
		final int					passwordId = unique.incrementAndGet();
		
		private AliasKeeper(final KeyStoreEntityType type, final String name) {
			this.type = type;
			this.name = name;
		}
	}

    private class DragGestureHandler implements DragGestureListener {
        private final JList<AliasKeeper>	list;

        private DragGestureHandler(final JList<AliasKeeper> list) {
            this.list = list;
        }

        @Override
        public void dragGestureRecognized(final DragGestureEvent dge) {
            final AliasKeeper 		selectedValue = list.getSelectedValue();
            final AliasDescriptor	desc = AliasDescriptor.of(place, wrapper, selectedValue.name, selectedValue.type);  
            final Transferable 		t = new FileTransferable(new JFileItemDescriptor(selectedValue.name, "./", false, 0, new Date(0), desc));
            
            dge.getDragSource().startDrag(dge, null, t, new DragSourceListener() {
				@Override public void dragEnter(DragSourceDragEvent dsde) {}
				@Override public void dragOver(DragSourceDragEvent dsde) {}
				@Override public void dropActionChanged(DragSourceDragEvent dsde) {}
				@Override public void dragExit(DragSourceEvent dse) {}
				@Override public void dragDropEnd(DragSourceDropEvent dsde) {}
			});
        }
    }
}
