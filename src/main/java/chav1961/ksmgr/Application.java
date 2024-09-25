package chav1961.ksmgr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;

import chav1961.ksmgr.gui.AskPassword;
import chav1961.ksmgr.gui.KeyStoreEditor;
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
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
<<<<<<< HEAD
=======
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.LRUPersistence;
import chav1961.purelib.ui.swing.AutoBuiltForm;
>>>>>>> branch 'master' of https://github.com/chav1961/ksmgr.git
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
<<<<<<< HEAD
import chav1961.purelib.ui.swing.useful.JDropTargetPlaceholder;
import chav1961.purelib.ui.swing.useful.JFileItemDescriptor;
import chav1961.purelib.ui.swing.useful.JFileList;
import chav1961.purelib.ui.swing.useful.JFileList.ContentViewType;
import chav1961.purelib.ui.swing.useful.JFileList.SelectedObjects;
import chav1961.purelib.ui.swing.useful.JFileList.SelectionType;
=======
import chav1961.purelib.ui.swing.useful.DnDManager.DnDMode;
import chav1961.purelib.ui.swing.useful.JEnableMaskManipulator;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JFileItemDescriptor;
>>>>>>> branch 'master' of https://github.com/chav1961/ksmgr.git
import chav1961.purelib.ui.swing.useful.JFileTree;
import chav1961.purelib.ui.swing.useful.JPlaceHolder;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangedEvent;
 
public class Application extends JFrame implements LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner, InputStreamGetter, OutputStreamGetter {
	private static final long serialVersionUID = -1812854125768597438L;
	public static final String				ARG_LOCAL_CONFIG = "localConfig";
	public static final String				ARG_LOCAL_CONFIG_DEFAULT = ".ksmgr.config";
	public static final String				TITLE_APPLICATION = "application.title";
	public static final String				HELP_ABOUT_APPLICATION = "application.help";
	public static final String				TITLE_HELP_ABOUT_APPLICATION = "application.help.title";

	public static final String				KEYSTORE_ITEM_ID = "<keystore>";
	
	private static final String				MENU_FILE_LRU = "menu.main.file.lru";
	private static final String				MENU_FILE_SAVE = "menu.main.file.lru";
	private static final String				MENU_FILE_SAVE_AS = "menu.main.file.lru";
	private static final String[]			MENUS = {MENU_FILE_LRU,
												     MENU_FILE_SAVE,
												     MENU_FILE_SAVE_AS};
	
	private static final long 				FILE_LRU = 1L << 0;
	private static final long 				FILE_SAVE = 1L << 1;
	private static final long 				FILE_SAVE_AS = 1L << 2;

	
	private static enum SelectedWindows {
		LEFT,
		RIGHT,
		BOTTOM
	}
	
	private final ContentMetadataInterface 	app;
	private final SubstitutableProperties	props;
	private final Localizer					parentLocalizer;
	private final Localizer					localizer;
	private final JMenuBar					menu;
	private final JStateString				state;
	private final JEnableMaskManipulator	emm;
	private final JSplitPane				topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JLabel(), new JLabel());
	private final JSplitPane				totalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JLabel(), new JLabel());
	private final FileSystemInterface		root = FileSystemInterface.Factory.newInstance(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/"));
	private final PasswordsRepo				passwords = new PasswordsRepo(false);
	private final List<String>				lruFiles = new ArrayList<>();
	private final LRUPersistence			pers;
	private final JFileContentManipulator	fcm;
	private final KeyStoreWrapper[]			ksList = new KeyStoreWrapper[2];
	private final AtomicInteger				unique = new AtomicInteger(1);
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
			this.props = SubstitutableProperties.of(props);
			this.parentLocalizer = parent;
			this.localizer = LocalizerFactory.getLocalizer(app.getRoot().getLocalizerAssociated());
			
			parent.push(localizer);
			localizer.addLocaleChangeListener(this);
			this.state = new JStateString(this.localizer, 10);
			this.pers = LRUPersistence.of(props, "LRU.item"); 
			this.fcm = new JFileContentManipulator("system", root, localizer, this, this, this.pers, lruFiles);
			this.fcm.addFileContentChangeListener((e)->processLRU(e));
			this.fcm.setOwner(this);
			this.fcm.setProgressIndicator(state);
			this.fcm.appendNewFileSupport();
			
			this.menu = SwingUtils.toJComponent(app.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class);
			this.emm = new JEnableMaskManipulator(MENUS, true, menu);
			SwingUtils.assignActionListeners(menu, this);
			SwingUtils.assignExitMethod4MainWindow(this, ()->exitApplication());

			selectCurrentPanel(SelectedWindows.LEFT);
			setCurrentPanel(new KSPlaceHolder(localizer));
			selectCurrentPanel(SelectedWindows.RIGHT);
			setCurrentPanel(new KSPlaceHolder(localizer));
			totalSplit.setLeftComponent(topSplit);
			totalSplit.setRightComponent(new JScrollPane(new JFileTree(this.state, this.root, true) {
				private static final long serialVersionUID = 1L;

				{
					addFocusListener(new SimpleFocusListener(()->selectCurrentPanel(SelectedWindows.BOTTOM)));
				}
				
				@Override
				public void placeFileContent(final Point location, final Iterable<JFileItemDescriptor> content) {
				}

<<<<<<< HEAD
									@Override
 								    public void placeFileContent(Point location, java.lang.Iterable<chav1961.purelib.ui.swing.useful.JFileItemDescriptor> content) {
										for( JFileItemDescriptor f : content) {
											System.err.println("Drop left "+f.getPath());									
										}
								    }
								};
			this.rightList = new JFileList(localizer, getLogger(), root, false, SelectionType.MULTIPLE, SelectedObjects.FILES, ContentViewType.AS_ICONS) {
									private static final long serialVersionUID = -1076235686454398505L;
					
									@Override
 								    public void placeFileContent(Point location, java.lang.Iterable<chav1961.purelib.ui.swing.useful.JFileItemDescriptor> content) {
										for( JFileItemDescriptor f : content) {
											System.err.println("Drop right "+f.getPath());									
										}
								    }
								};
			
			this.leftSplit.setLeftComponent(new JScrollPane(leftTree));
			setPlaceHolder();
			totalSplit.setLeftComponent(leftSplit);
			totalSplit.setRightComponent(new JScrollPane(rightList));
=======
				@Override
				public void refreshLinkedContent(final FileSystemInterface content) {
				}
			}));			
			selectCurrentPanel(SelectedWindows.BOTTOM);
>>>>>>> branch 'master' of https://github.com/chav1961/ksmgr.git
			
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
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
	
	@OnAction("action:/closeKeystore")
	private void closeKeyStore () {
		setCurrentPanel(new KSPlaceHolder(localizer));
	}
	
	@OnAction("action:/exit")
	private void exitApplication () {
		setVisible(false);
		dispose();
	}

	@OnAction("action:builtin:/builtin.languages")
	private void selectLang(final Hashtable<String,String[]> langs) {
		final SupportedLanguages	newLang = SupportedLanguages.valueOf(langs.get("lang")[0]);
		final Locale				newLocale = newLang.getLocale();
		
		parentLocalizer.setCurrentLocale(newLocale);
	}
	
	@OnAction("action:/helpAbout")
	private void showAboutScreen() {
		SwingUtils.showAboutScreen(this, localizer, TITLE_HELP_ABOUT_APPLICATION, HELP_ABOUT_APPLICATION, URI.create("root://chav1961.ksmgr.Application/chav1961/ksmgr/avatar.jpg"), new Dimension(300,300));
	}

	private void newKeystore(final String keyStoreType) {
		final int		keyStoreId = unique.incrementAndGet();
		final String	keyStoreString = "keystore."+keyStoreId;
		final char[]	passwd = askPassword(keyStoreString);
		
		if (passwd != null) {
			try{
				if (fcm.newFile()) {
					final KeyStore			ks = KeyStore.getInstance(keyStoreType);
					final KeyStoreWrapper	wrapper = new KeyStoreWrapper(keyStoreId, null, ks);
					
					ks.load(null, passwd);
					setCurrentPanel(wrapper);
				}
			} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
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
		final AskPassword	ap = new AskPassword(state);

		if (passwords.isKeepedPasswords() && passwords.hasPasswordFor(passwordId)) {
			ap.password = passwords.getPasswordFor(passwordId);
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
	
	private <T> boolean ask(final T instance, final int width, final int height) {
		try{final ContentMetadataInterface	mdi = ContentModelFactory.forAnnotatedClass(instance.getClass());
		
			try(final AutoBuiltForm<T,?>	abf = new AutoBuiltForm<>(mdi, localizer, state, PureLibSettings.INTERNAL_LOADER, instance, (FormManager<Object,T>)instance)) {
				
				((ModuleAccessor)instance).allowUnnamedModuleAccess(abf.getUnnamedModules());
				abf.setPreferredSize(new Dimension(width,height));
				return AutoBuiltForm.ask(this, localizer, abf);
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
	}
	
	private void setCurrentPanel(final KeyStoreWrapper wrapper) {
		final KeyStoreEditor	editor = new KeyStoreEditor(localizer, state, wrapper);
		
		switch (selected) {
			case BOTTOM	:
				throw new IllegalStateException("Current panel is not keystore keeper");
			case LEFT	:
				ksList[0] = wrapper;
				topSplit.setLeftComponent(editor);
				editor.addFocusListener(new SimpleFocusListener(()->selectCurrentPanel(SelectedWindows.LEFT)));
				break;
			case RIGHT:
				ksList[1] = wrapper;
				topSplit.setRightComponent(editor);
				editor.addFocusListener(new SimpleFocusListener(()->selectCurrentPanel(SelectedWindows.RIGHT)));
				break;
			default :
				throw new UnsupportedOperationException("Selected window ["+selected+"] is not supported yet");
		}
	}

	private void setCurrentPanel(final KSPlaceHolder holder) {
		switch (selected) {
			case BOTTOM	:
				throw new IllegalStateException("Current panel is not keystore keeper");
			case LEFT	:
				ksList[0] = null;
				topSplit.setLeftComponent(holder);
				holder.addFocusListener(new SimpleFocusListener(()->selectCurrentPanel(SelectedWindows.LEFT)));
				break;
			case RIGHT:
				ksList[1] = null;
				topSplit.setRightComponent(holder);
				holder.addFocusListener(new SimpleFocusListener(()->selectCurrentPanel(SelectedWindows.RIGHT)));
				break;
			default :
				throw new UnsupportedOperationException("Selected window ["+selected+"] is not supported yet");
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException, IllegalArgumentException {
		fillTitle();
	}

	public static void main(final String[] args) throws IOException, EnvironmentException, FlowException, ContentException, HeadlessException, URISyntaxException {
		final ArgParser		parser = new ApplicationArgParser().parse(args);
		
<<<<<<< HEAD
		try(final InputStream				is = Application.class.getResourceAsStream("application.xml")) {
=======
		try(final InputStream		is = Application.class.getResourceAsStream("application.xml")) {
>>>>>>> branch 'master' of https://github.com/chav1961/ksmgr.git
			final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
			final Application		application = new Application(xda, PureLibSettings.PURELIB_LOCALIZER, parser.getValue(ARG_LOCAL_CONFIG, File.class));
			
			application.setVisible(true);
<<<<<<< HEAD
			latch.await();
		} catch (InterruptedException e) {
=======
>>>>>>> branch 'master' of https://github.com/chav1961/ksmgr.git
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
	
	private static class KSPlaceHolder extends JPlaceHolder {
		private static final String	PLACEHOLDER_TEXT = "chav1961.ksmgr.placeholder.text";
		private static final String	PLACEHOLDER_TOOLTIP = "chav1961.ksmgr.placeholder.tt";
		
		private static final long serialVersionUID = 1L;
		
		public KSPlaceHolder(final Localizer localizer) {
			super(localizer, PLACEHOLDER_TEXT, PLACEHOLDER_TOOLTIP);
			getDnDManager().selectDnDMode(DnDMode.COPY);
		}

		@Override
		public boolean canReceive(DnDMode currentMode, Component from, int xFrom, int yFrom, Component to, int xTo, int yTo, Class<?> contentClass) {
			return contentClass == JFileItemDescriptor.class;
		}

		@Override
		public void complete(final DnDMode currentMode, final Component from, final int xFrom, final int yFrom, final Component to, final int xTo, final int yTo, final Object content) {
			System.err.println("Completed");
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
