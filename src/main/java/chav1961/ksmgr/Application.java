package chav1961.ksmgr;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.SecretKey;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import chav1961.ksmgr.gui.AskChangePassword;
import chav1961.ksmgr.gui.AskExportSecureKey;
import chav1961.ksmgr.gui.AskNewPassword;
import chav1961.ksmgr.gui.AskPassword;
import chav1961.ksmgr.gui.AskSecureKeyParameters;
import chav1961.ksmgr.gui.KeyStoreEditor;
import chav1961.ksmgr.gui.SettingsDialog;
import chav1961.ksmgr.interfaces.AliasDescriptor;
import chav1961.ksmgr.interfaces.SelectedWindows;
import chav1961.ksmgr.internal.KeyStoreWrapper;
import chav1961.ksmgr.utils.PasswordsRepo;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.LRUPersistence;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JFileItemDescriptor;
import chav1961.purelib.ui.swing.useful.JFileTree;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.JPlaceHolder;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.LocalizedFormatter;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangedEvent;
 
public class Application extends JFrame implements LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner, InputStreamGetter, OutputStreamGetter {
	private static final long serialVersionUID = -1812854125768597438L;
	public static final String				ARG_LOCAL_CONFIG = "localConfig";
	public static final String				ARG_LOCAL_CONFIG_DEFAULT = ".ksmgr.config";
	
	public static final String				TITLE_APPLICATION = "application.title";
	public static final String				HELP_ABOUT_APPLICATION = "application.help";
	public static final String				TITLE_HELP_ABOUT_APPLICATION = "application.help.title";
	public static final String				COPY_FILE_APPLICATION = "application.copy.file";
	public static final String				TITLE_COPY_FILE_APPLICATION = "application.copy.file.title";

	public static final String				KEYSTORE_ITEM_ID = "<keystore>";
	
	private static final String				MENU_FILE_LRU = "menu.file.lru";
	
	private static final long 				FILE_LRU = 1L << 0;
	private static final long 				FILE_SAVE = 1L << 1;
	private static final long 				FILE_SAVE_AS = 1L << 2;

	
	private final ContentMetadataInterface 	app;
	private final SubstitutableProperties	props;
	private final File						propsLocation;
	private final Localizer					parentLocalizer;
	private final Localizer					localizer;
	private final JMenuBar					menu;
	private final JStateString				state;
	private final MainMenuManager			emm;
	private final JSplitPane				topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JLabel(), new JLabel());
	private final JSplitPane				totalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JLabel(), new JLabel());
	private final FileSystemInterface		root = FileSystemInterface.Factory.newInstance(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/"));
	private final PasswordsRepo				passwords = new PasswordsRepo(false);
	private final List<String>				lruFiles = new ArrayList<>();
	private final LRUPersistence			pers;
	private final JFileContentManipulator	fcm;
	private final KeyStoreEditor[]			ksList = new KeyStoreEditor[2];
	private final AtomicInteger				unique = new AtomicInteger(1);
	private final SettingsDialog			settings;
	private final ActionListener			leftActionListener = (e)->saveAs(SelectedWindows.LEFT);
	private final ActionListener			rightActionListener = (e)->saveAs(SelectedWindows.RIGHT);
	private final ListSelectionListener		leftSelectionListener = (e)->selectLeft(!((JList<?>)e.getSource()).getSelectionModel().isSelectionEmpty());
	private final ListSelectionListener		rightSelectionListener = (e)->selectRight(!((JList<?>)e.getSource()).getSelectionModel().isSelectionEmpty());
	private SelectedWindows					selected = SelectedWindows.BOTTOM; 
	
	public Application(final ContentMetadataInterface xda, final Localizer parent, final File props) throws NullPointerException, IllegalArgumentException, EnvironmentException, IOException, FlowException, SyntaxException, PreparationException, ContentException {
		if (xda == null) {
			throw new NullPointerException("Application descriptor can't be null");
		}
		else if (parent == null) {
			throw new NullPointerException("Parent localizer can't be null");
		}
		else if (props == null) {
			throw new NullPointerException("Config properties can't be null");
		}
		else {
			this.app = xda;
			this.propsLocation = props;
			this.props = SubstitutableProperties.of(props);
			this.parentLocalizer = parent;
			this.localizer = LocalizerFactory.getLocalizer(app.getRoot().getLocalizerAssociated());
			
			parent.push(localizer);
			localizer.addLocaleChangeListener(this);
			this.state = new JStateString(this.localizer, 10);
			this.settings = new SettingsDialog(state);
			
			settings.loadSettings(this.props);			
			this.pers = LRUPersistence.of(props, "LRU.item"); 
			this.fcm = new JFileContentManipulator("system", root, localizer, this, this, this.pers, lruFiles);
			this.fcm.addFileContentChangeListener((e)->processLRU(e));
			this.fcm.setOwner(this);
			this.fcm.setProgressIndicator(state);
			this.fcm.appendNewFileSupport();
			
			this.menu = SwingUtils.toJComponent(app.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class);
			this.emm = new MainMenuManager(settings, menu);
			SwingUtils.assignActionListeners(menu, this);
			SwingUtils.assignExitMethod4MainWindow(this, ()->exitApplication());

			selectCurrentPanel(SelectedWindows.LEFT);
			setCurrentPanel(new KSPlaceHolder(localizer, SelectedWindows.LEFT));
			selectCurrentPanel(SelectedWindows.RIGHT);
			setCurrentPanel(new KSPlaceHolder(localizer, SelectedWindows.RIGHT));
			totalSplit.setLeftComponent(topSplit);
			totalSplit.setRightComponent(new JScrollPane(new JFileTree(this.state, this.root, true) {
				private static final long serialVersionUID = 1L;

				{
					addFocusListener(new SimpleFocusListener(()->selectCurrentPanel(SelectedWindows.BOTTOM)));
				}
				
				@Override
				public void placeFileContent(final Point location, final Iterable<JFileItemDescriptor> content) {
					final TreePath	path = getPathForLocation(location.x, location.y);
					
					for(JFileItemDescriptor item : content) {
						if (item.getCargo() != null) {
							exportAlias((JFileItemDescriptor)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject(), (AliasDescriptor)item.getCargo());
						}
						else {
							copyFileContent((JFileItemDescriptor)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject(), item);
						}
					}
				}

				@Override
				public void refreshLinkedContent(final FileSystemInterface content) {
				}
			}));			
			selectCurrentPanel(SelectedWindows.BOTTOM);
			
			getContentPane().add(menu, BorderLayout.NORTH);
			getContentPane().add(totalSplit, BorderLayout.CENTER);
			getContentPane().add(state, BorderLayout.SOUTH);
			SwingUtils.centerMainWindow(this, 0.75f);
			
//			localizer.setCurrentLocale(Locale.forLanguageTag(settings.currentLang));
			fillLocalizedStrings();
		}
	}
	
	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		SwingUtils.refreshLocale(state,oldLocale,newLocale);
		SwingUtils.refreshLocale(menu,oldLocale,newLocale);
		SwingUtils.refreshLocale(totalSplit,oldLocale,newLocale);
	}

	@Override
	public LoggerFacade getLogger() {
		return state;
	}

	@Override
	public OutputStream getOutputContent() throws IOException {
		switch (selected) {
			case BOTTOM	:
				return OutputStream.nullOutputStream();
			case LEFT	:
				return OutputStream.nullOutputStream();
			case RIGHT	:
				return OutputStream.nullOutputStream();
			default :
				throw new UnsupportedOperationException("Selected window ["+selected+"] is not supported yet");
		}
	}

	@Override
	public InputStream getInputContent() throws IOException {
		switch (selected) {
			case BOTTOM	:
				return InputStream.nullInputStream();
			case LEFT	:
				if (ksList[0] != null) {
					final KeyStoreWrapper	wrapper = ksList[0].getKeyStoreWrapper();
					
					try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
						final char[]	password = askPassword(PasswordsRepo.KEY_STORE_PREFIX+'.'+wrapper.entryId);
						
						if (password != null) {
							wrapper.keyStore.store(baos, password);
							return new ByteArrayInputStream(baos.toByteArray());
						}
						else {
							return InputStream.nullInputStream();
						}
					} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
						throw new IOException(e);
					}
				}
				else {
					return InputStream.nullInputStream();
				}
			case RIGHT	:
				if (ksList[1] != null) {
					final KeyStoreWrapper	wrapper = ksList[1].getKeyStoreWrapper();
					
					try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
						final char[]	password = askPassword(PasswordsRepo.KEY_STORE_PREFIX+'.'+wrapper.entryId);
						
						if (password != null) {
							wrapper.keyStore.store(baos, password);
							return new ByteArrayInputStream(baos.toByteArray());
						}
						else {
							return InputStream.nullInputStream();
						}
					} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
						throw new IOException(e);
					}
				}
				else {
					return InputStream.nullInputStream();
				}
			default :
				throw new UnsupportedOperationException("Selected window ["+selected+"] is not supported yet");
		}
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}
	
	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		
		if (visible) {
			topSplit.setDividerLocation(0.5);
			totalSplit.setDividerLocation(0.3);
		}
	}

	@OnAction("action:/newKeyStoreJCEKS")
	private void newKeystoreJCEKS() {
		newKeystore("jceks");
	}

	@OnAction("action:/newKeyStoreJKS")
	private void newKeystoreJKS() {
		newKeystore("jks");
	}

	@OnAction("action:/newKeyStorePKCS12")
	private void newKeystorePKCS12() {
		newKeystore("pkcs12");
	}

	@OnAction("action:/openKeyStore")
	private void openKeyStore() {
		final int		keyStoreId = unique.incrementAndGet();
		final String	keyStoreString = PasswordsRepo.KEY_STORE_PREFIX+'.'+keyStoreId;
		final char[]	passwd;
		
		try {
			if (fcm.openFile() && (passwd = askPassword(keyStoreString)) != null) {
				loadKeyStore(fcm.getCurrentPathOfTheFile(), passwd, keyStoreId);
			}
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			getLogger().message(Severity.error, e, "Error loading keystore: "+e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/saveKeyStore")
	private void saveKeyStore() {
		try {
			fcm.saveFile();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, "Error saving keystore: "+e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/saveKeyStoreAs")
	private void saveKeyStoreAs() {
		try {
			fcm.saveFileAs();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, "Error saving keystore: "+e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/closeKeyStore")
	private void closeKeyStore() {
		setCurrentPanel(new KSPlaceHolder(localizer, selected));
	}

	@OnAction("action:/changePassword")
	private void changePassword() {
		final char[]	passwd = askChangePassword(new char[0]);
		
		if (passwd != null) {
			
		}
	}
	
	@OnAction("action:/exit")
	private void exitApplication() {
		setVisible(false);
		dispose();
	}

	
	@OnAction("action:/keyGenerate")
	private void createSymmetricKey() {
		final int	ksIndex = selected == SelectedWindows.LEFT ? 0 : 1;
		final KeyStore					ks = ksList[ksIndex].getKeyStoreWrapper().keyStore;
		final AskSecureKeyParameters	askp = new AskSecureKeyParameters(getLogger(), ks, settings.preferredProvider, settings.preferredSalt);
		
		if (ask(askp, 400, 300)) {
			final char[]	password = askNewPassword();
			
			if (password != null) {
				try {
					final SecretKey		key = askp.createSecretKey();
					final int			passwordId;
					
					switch (selected) {
						case BOTTOM	:
							passwordId = 0;
							break;
						case LEFT : case RIGHT :
							passwordId = ksList[ksIndex].placeSecretKey(askp.alias, key, password, true);
							break;
						default :
							throw new UnsupportedOperationException("Selected window ["+selected+"] is not supported yet"); 
					}
					if (passwordId != 0 && passwords.isKeepedPasswords()) {
						passwords.storePasswordFor(PasswordsRepo.KEY_STORE_ITEM_PREFIX+'.'+passwordId, password);
						passwords.storePasswordFor(PasswordsRepo.SECRET_KEY_PREFIX+'.'+passwordId, askp.password);
					}
					fcm.setModificationFlag();
				} catch (InvalidKeySpecException | NoSuchAlgorithmException | KeyStoreException | UnsupportedEncodingException | NoSuchProviderException e) {
					getLogger().message(Severity.error, e.getLocalizedMessage());
				}
			}
		}
	}
	
	@OnAction("action:builtin:/builtin.languages")
	private void selectLang(final Hashtable<String,String[]> langs) {
		final SupportedLanguages	newLang = SupportedLanguages.valueOf(langs.get("lang")[0]);
		final Locale				newLocale = newLang.getLocale();
		
		parentLocalizer.setCurrentLocale(newLocale);
	}

	@OnAction("action:/settings")
	private void showSettings() {
		if (ask(settings, 450, 180)) {
			try {
				settings.storeSettings(props);
				props.store(propsLocation);
			} catch (IOException e) {
				getLogger().message(Severity.error, e.getLocalizedMessage());
			}
		}
	}
	
	@OnAction("action:/helpAbout")
	private void showAboutScreen() {
		SwingUtils.showAboutScreen(this, localizer, TITLE_HELP_ABOUT_APPLICATION, HELP_ABOUT_APPLICATION, URI.create("root://chav1961.ksmgr.Application/chav1961/ksmgr/avatar.jpg"), new Dimension(300,300));
	}
  
	private void newKeystore(final String keyStoreType) {
		final int		keyStoreId = unique.incrementAndGet();
		final String	keyStoreString = PasswordsRepo.KEY_STORE_PREFIX+'.'+keyStoreId;
		final char[]	passwd = askNewPassword();
		
		if (passwd != null) {
			try{
				if (fcm.newFile()) {
					final KeyStore			ks = KeyStore.getInstance(keyStoreType);
					final KeyStoreWrapper	wrapper = new KeyStoreWrapper(keyStoreId, null, ks);
					
					ks.load(null, passwd);
					setCurrentPanel(wrapper);
					if (passwords.isKeepedPasswords()) {
						passwords.storePasswordFor(keyStoreString, passwd);
					}
				}
			} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
	}
	
	private void openKeystore(final String keyStorePath) {
		final int		keyStoreId = unique.incrementAndGet();
		final String	keyStoreString = PasswordsRepo.KEY_STORE_PREFIX+'.'+keyStoreId;
		final char[]	passwd = askPassword(keyStoreString);
		
		if (passwd != null) {
			try{
				if (fcm.openFile(keyStorePath)) {
					loadKeyStore(keyStorePath, passwd, keyStoreId);
				}
			} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
	}

	private void loadKeyStore(final String keyStorePath, final char[] passwd, final int keyStoreId) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		final File				f = new File(keyStorePath);
		final KeyStore			ks = KeyStore.getInstance(f, passwd);
		final KeyStoreWrapper	wrapper = new KeyStoreWrapper(keyStoreId, f, ks);
		final String			keyStoreString = PasswordsRepo.KEY_STORE_PREFIX+'.'+keyStoreId;
		
		setCurrentPanel(wrapper);
		if (passwords.isKeepedPasswords()) {
			passwords.storePasswordFor(keyStoreString, passwd);
		}
	}
	
	private void processLRU(final FileContentChangedEvent<?> event) {
		switch (event.getChangeType()) {
			case LRU_LIST_REFRESHED			:
				fillLRU(fcm.getLastUsed());
				break;
			case FILE_LOADED 				:
		        getCurrentWrapper();
				emm.setEnableMaskOn(FILE_SAVE_AS);
				fillTitle();
				break;
			case FILE_STORED 				:
				fcm.clearModificationFlag();
				break;
			case FILE_STORED_AS 			:
				fcm.clearModificationFlag();
				fillTitle();
				break;
			case MODIFICATION_FLAG_CLEAR 	:
				emm.setEnableMaskOff(FILE_SAVE);
				fillTitle();
				break;
			case MODIFICATION_FLAG_SET 		:
				emm.setEnableMaskTo(FILE_SAVE, !fcm.isFileNew());
				fillTitle();
				break;
			case FILE_SUPPORT_ID_CHANGED	:
				emm.setEnableMaskTo(FILE_SAVE, !fcm.isFileNew());
				fillTitle();
				break;
			case NEW_FILE_CREATED 			:
				emm.setEnableMaskOff(FILE_SAVE);
				emm.setEnableMaskOn(FILE_SAVE_AS);
				fillTitle();
				break;
			default :
				throw new UnsupportedOperationException("Change type ["+event.getChangeType()+"] is not supported yet");
		}
	}
	
	private void fillLRU(final List<String> lastUsed) {
		if (lastUsed.isEmpty()) {
			emm.setEnableMaskOff(FILE_LRU);
		}
		else {
			final JMenu	menu = (JMenu)SwingUtils.findComponentByName(this.menu, MENU_FILE_LRU);
			
			menu.removeAll();
			for (String file : lastUsed) {
				final JMenuItem	item = new JMenuItem(file);
				
				item.addActionListener((e)->loadLRU(item.getText()));
				menu.add(item);
			}
			emm.setEnableMaskOn(FILE_LRU);
		}
	}

	private void fillTitle() {
		setTitle(localizer.getValue(TITLE_APPLICATION));
	}
	
	private KeyStoreWrapper getCurrentWrapper() {
		return null;
	}
	
	private void loadLRU(final String path) {
		final File	f = new File(path);
		
		if (f.exists() && f.isFile() && f.canRead()) {
			try{fcm.openFile(path);
			} catch (IOException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
		else {
			fcm.removeFileNameFromLRU(path);
//			getLogger().message(Severity.warning, KEY_APPLICATION_MESSAGE_FILE_NOT_EXISTS, path);
		}
	}

	private char[] askPassword(final String passwordId) {
		final AskPassword	ap = new AskPassword(getLogger());

		if (passwords.isKeepedPasswords() && passwords.hasPasswordFor(passwordId)) {
			ap.password = passwords.getPasswordFor(passwordId);
			if (settings.silentlySubstitutePasswords) {
				return ap.password; 
			}
		}
		
		if (ask(ap, 250, 50)) {
			if (passwords.isKeepedPasswords()) {
				passwords.storePasswordFor(passwordId, ap.password);
			}
			return ap.password; 
		}
		else {
			return null;
		}
	}

	private char[] askNewPassword() {
		final AskNewPassword	anp = new AskNewPassword(getLogger(), getLocalizer());

		if (ask(anp, 250, 70)) {
			if (!Arrays.equals(anp.password, anp.duplicate)) {
				getLogger().message(Severity.error, localizer.getValue(AskNewPassword.ERROR_DIFFERENT_PASSWORDS));
				return null;
			}
			else {
				return anp.password;
			}
		}
		else {
			return null;
		}
	}	

	private char[] askChangePassword(final char[] oldPassword) {
		final AskChangePassword	acp = new AskChangePassword(getLogger(), getLocalizer());

		acp.oldPassword = oldPassword;
		if (ask(acp, 250, 70)) {
			if (!Arrays.equals(acp.password, acp.duplicate)) {
				getLogger().message(Severity.error, localizer.getValue(AskNewPassword.ERROR_DIFFERENT_PASSWORDS));
				return null;
			}
			else {
				return acp.password;
			}
		}
		else {
			return null;
		}
	}	
	
	private <T> boolean ask(final T instance, final int width, final int height) {
		try{final ContentMetadataInterface	mdi = ContentModelFactory.forAnnotatedClass(instance.getClass());
			try(final AutoBuiltForm<T,?>	abf = new AutoBuiltForm<>(mdi, getLocalizer(), getLogger(), PureLibSettings.INTERNAL_LOADER, instance, (FormManager<Object,T>)instance)) {
				
				((ModuleAccessor)instance).allowUnnamedModuleAccess(abf.getUnnamedModules());
				abf.setPreferredSize(new Dimension(width,height));
				return AutoBuiltForm.ask(this, getLocalizer(), abf);
			}
		} catch (ContentException e) {
			getLogger().message(Severity.error,e.getLocalizedMessage());
			return false;
		} 
	}

	private void selectCurrentPanel(final SelectedWindows window) {
		final LineBorder	border = new LineBorder(Color.BLUE);
		
		selected = window;
		((JComponent)topSplit.getLeftComponent()).setBorder(null);
		((JComponent)topSplit.getRightComponent()).setBorder(null);
		((JComponent)totalSplit.getRightComponent()).setBorder(null);
		switch (selected) {
			case BOTTOM	:
				((JComponent)totalSplit.getRightComponent()).setBorder(border);
				break;
			case LEFT	:
				((JComponent)topSplit.getLeftComponent()).setBorder(border);
				break;
			case RIGHT:
				((JComponent)topSplit.getRightComponent()).setBorder(border);
				break;
			default :
				throw new UnsupportedOperationException("Selected window ["+selected+"] is not supported yet");
		}
		emm.setSelection(selected);
	}
	
	private void setCurrentPanel(final KeyStoreWrapper wrapper) {
		final KeyStoreEditor	editor = new KeyStoreEditor(getLocalizer(), getLogger(), selected, wrapper, passwords);
		
		switch (selected) {
			case BOTTOM	:
				throw new IllegalStateException("Current panel is not keystore keeper");
			case LEFT	:
				ksList[0] = editor;
				editor.addSelectionListener(leftSelectionListener);
				editor.addActionListener(leftActionListener);
				topSplit.setLeftComponent(editor);
				editor.addFocusListener(new SimpleFocusListener(()->selectCurrentPanel(SelectedWindows.LEFT)));
				emm.enableLeftRepo(true);
				emm.setLeftFileNameDefined(wrapper.file != null);
				break;
			case RIGHT:
				ksList[1] = editor;
				editor.addSelectionListener(rightSelectionListener);
				editor.addActionListener(rightActionListener);
				topSplit.setRightComponent(editor);
				editor.addFocusListener(new SimpleFocusListener(()->selectCurrentPanel(SelectedWindows.RIGHT)));
				emm.enableRightRepo(true);
				emm.setRightFileNameDefined(wrapper.file != null);
				break;
			default :
				throw new UnsupportedOperationException("Selected window ["+selected+"] is not supported yet");
		}
	}

	private void setCurrentPanel(final KSPlaceHolder holder) {
		KeyStoreEditor	prev;
		
		switch (selected) {
			case BOTTOM	:
				throw new IllegalStateException("Current panel is not keystore keeper");
			case LEFT	:
				prev = ksList[0];
				if (prev != null) {
					prev.removeSelectionListener(leftSelectionListener);
					prev.removeActionListener(leftActionListener);
				}
				ksList[0] = null;
				topSplit.setLeftComponent(holder);
				holder.addFocusListener(new SimpleFocusListener(()->selectCurrentPanel(SelectedWindows.LEFT)));
				emm.enableLeftRepo(false);
				break;
			case RIGHT:
				prev = ksList[1]; 
				if (prev != null) {
					prev.removeSelectionListener(rightSelectionListener);
					prev.removeActionListener(rightActionListener);
				}
				ksList[1] = null;
				topSplit.setRightComponent(holder);
				holder.addFocusListener(new SimpleFocusListener(()->selectCurrentPanel(SelectedWindows.RIGHT)));
				emm.enableRightRepo(false);
				break;
			default :
				throw new UnsupportedOperationException("Selected window ["+selected+"] is not supported yet");
		}
	}

	private void exportAlias(final JFileItemDescriptor target, final AliasDescriptor source) {
		final AskExportSecureKey	aesk = new AskExportSecureKey(getLogger());
		
		if (ask(aesk, 150, 350)) {
			System.err.println("Entry: "+target+", desc="+source);
		}
	}
	
	private void copyFileContent(final JFileItemDescriptor target, final JFileItemDescriptor source) {
		if (new JLocalizedOptionPane(getLocalizer()).confirm(this, 
							new LocalizedFormatter(COPY_FILE_APPLICATION, source.getPath(), target.getPath()), 
							TITLE_COPY_FILE_APPLICATION, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			System.err.println("File: "+target+", desc="+source);
		}
	}

	private void saveAs(final SelectedWindows left) {
		// TODO Auto-generated method stub
	}
	
	
	private void selectLeft(final boolean selected) {
		emm.setLeftRepoSelected(selected);
	}

	private void selectRight(final boolean selected) {
		emm.setRightRepoSelected(selected);
	}
	
	private void fillLocalizedStrings() throws LocalizationException, IllegalArgumentException {
		fillTitle();
	}

	public static void main(final String[] args) throws IOException, EnvironmentException, FlowException, ContentException, HeadlessException, URISyntaxException {
		final ArgParser		parser = new ApplicationArgParser().parse(args);
		
		try(final InputStream		is = Application.class.getResourceAsStream("application.xml")) {
			final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
			final Application		application = new Application(xda, PureLibSettings.PURELIB_LOCALIZER, parser.getValue(ARG_LOCAL_CONFIG, File.class));
			
			application.setVisible(true);
		}
	}
	
	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new FileArg(ARG_LOCAL_CONFIG, true, "Local configuration descriptor", ARG_LOCAL_CONFIG_DEFAULT)
		};
		
		ApplicationArgParser() {
			super(KEYS);
		} 
	}
	
	private class KSPlaceHolder extends JPlaceHolder {
		private static final String	PLACEHOLDER_TEXT = "chav1961.ksmgr.placeholder.text";
		private static final String	PLACEHOLDER_TOOLTIP = "chav1961.ksmgr.placeholder.tt";
		
		private static final long serialVersionUID = 1L;
		
		private final SelectedWindows	selected;
		
		public KSPlaceHolder(final Localizer localizer, final SelectedWindows selected) {
			super(localizer, PLACEHOLDER_TEXT, PLACEHOLDER_TOOLTIP, (tr, df)->tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor));
			this.selected = selected;
		}
		
		@Override
		public boolean acceptDrop(final Transferable trans) throws IOException {
			if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				try {
					for(Object item : (List<?>)trans.getTransferData(DataFlavor.javaFileListFlavor)) {
						if (item instanceof File) {
							selectCurrentPanel(selected);
							openKeystore(((File)item).getAbsolutePath());
							return true;
						}
						else if (item instanceof JFileItemDescriptor) {
							selectCurrentPanel(selected);
							openKeystore(((JFileItemDescriptor)item).getPath());
							return true;
						}
					}
				} catch (UnsupportedFlavorException e) {
					throw new IOException(e);
				}
				return false;
			}
			else {
				return false;
			}
		}
	}
	
	private static class SimpleFocusListener implements FocusListener {
		private final Runnable	run;
		
		private SimpleFocusListener(final Runnable run) {
			this.run = run;
		}

		@Override
		public void focusGained(final FocusEvent e) {
			run.run();
		}

		@Override
		public void focusLost(final FocusEvent e) {
		}
	}
}
