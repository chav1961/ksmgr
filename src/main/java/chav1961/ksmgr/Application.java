package chav1961.ksmgr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import chav1961.ksmgr.dialogs.AskPasswordDialog;
import chav1961.ksmgr.dialogs.CreateKeystoreDialog;
import chav1961.ksmgr.dialogs.CurrentSettingsDialog;
import chav1961.ksmgr.internal.AlgorithmRepo;
import chav1961.ksmgr.internal.JHighlightedScrollPane;
import chav1961.ksmgr.internal.PureLibClient;
import chav1961.ksmgr.keystore.KeyStoreState;
import chav1961.ksmgr.keystore.KeyStoreViewer;
import chav1961.ksmgr.utils.GuiUtils;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JDropTargetPlaceholder;
import chav1961.purelib.ui.swing.useful.JFileItemDescriptor;
import chav1961.purelib.ui.swing.useful.JFileList;
import chav1961.purelib.ui.swing.useful.JFileList.ContentViewType;
import chav1961.purelib.ui.swing.useful.JFileList.SelectedObjects;
import chav1961.purelib.ui.swing.useful.JFileList.SelectionType;
import chav1961.purelib.ui.swing.useful.JFileTree;
import chav1961.purelib.ui.swing.useful.JStateString;
 
public class Application extends JFrame implements LocaleChangeListener, LoggerFacadeOwner {
	private static final long serialVersionUID = -1812854125768597438L;
	public static final String				ARG_HELP_PORT = "port";
	public static final String				ARG_LOCAL_CONFIG = "localConfig";
	public static final String				ARG_LOCAL_CONFIG_DEFAULT = ".ksmgr.config";
	public static final String				TITLE_APPLICATION = "application.title";
	public static final String				HELP_ABOUT_APPLICATION = "application.help";
	public static final String				TITLE_HELP_ABOUT_APPLICATION = "application.help.title";
	public static final String				LEFT_PLACEHOLDER_APPLICATION = "application.placeholder.left";

	public static final String				KEYSTORE_ITEM_ID = "<keystore>";
	
	
	private final ContentMetadataInterface 	app;
	private final Localizer					localizer;
	private final int 						localHelpPort;
	private final CountDownLatch			latch;
	private final JMenuBar					menu;
	private final JStateString				state;
	private final JSplitPane				leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final JSplitPane				totalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private final CurrentSettingsDialog		settings;
	private final AlgorithmRepo				algo = new AlgorithmRepo();
	private final PasswordsRepo				passwords;
	private final FileSystemInterface		root = FileSystemInterface.Factory.newInstance(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/")); 
	private final JFileTree					leftTree;
	private final JFileList					rightList;
//	private final JFileContentManipulator	contentManipulator;
	private KeyStore						currentKeystore = null;
	
	public Application(final ContentMetadataInterface xda, final Localizer parent, final int helpPort, final String configFile, final CountDownLatch latch) throws NullPointerException, IllegalArgumentException, EnvironmentException, IOException, FlowException, SyntaxException, PreparationException, ContentException {
		if (xda == null) {
			throw new NullPointerException("Application descriptor can't be null");
		}
		else if (parent == null) {
			throw new NullPointerException("Parent localizer can't be null");
		}
		else if (configFile == null || configFile.isEmpty()) {
			throw new IllegalArgumentException("Config file can't be null or empty");
		}
		else if (latch == null) {
			throw new NullPointerException("Latch to notify closure can't be null");
		}
		else {
			this.app = xda;
			this.localizer = LocalizerFactory.getLocalizer(app.getRoot().getLocalizerAssociated());
			this.localHelpPort = helpPort;
			this.latch = latch;

			parent.push(localizer);
			localizer.addLocaleChangeListener(this);
			
			this.state = new JStateString(this.localizer,10);
			
			this.menu = SwingUtils.toJComponent(app.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class);
			SwingUtils.assignActionListeners(menu, this);
			SwingUtils.assignExitMethod4MainWindow(this, ()->exitApplication());

			this.settings = new CurrentSettingsDialog(state, configFile, algo);
			this.passwords = new PasswordsRepo(settings.keepPasswords);
			this.leftTree = new JFileTree(getLogger(), root, false) {
									private static final long serialVersionUID = 1L;

									@Override
									public void refreshLinkedContent(FileSystemInterface content) {
										// TODO Auto-generated method stub
										try {
//											System.err.println("In: "+content.getPath());
											rightList.refreshContent(content.getPath());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}

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
			
			getContentPane().add(menu, BorderLayout.NORTH);
			getContentPane().add(totalSplit, BorderLayout.CENTER);
			getContentPane().add(state, BorderLayout.SOUTH);
			SwingUtils.centerMainWindow(this, 0.75f);
			
			localizer.setCurrentLocale(Locale.forLanguageTag(settings.currentLang));
			refreshMenuState(KeyStoreState.MISSING);
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
	
	public PasswordsRepo getPasswordsRepo() {
		return passwords;
	}
	
	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		
		if (visible) {
			leftSplit.setDividerLocation(0.5);
			totalSplit.setDividerLocation(0.3);
		}
	}

	public boolean askPassword(final AskPasswordDialog dialog, final String item, final int width, final int height) {
		if (dialog == null) {
			throw new NullPointerException("Ask apssword dialog can't be null");
		}
		else {
			if (item != null) {
				if (passwords.hasPasswordFor(item)) {
					dialog.password = passwords.getPasswordFor(item);
					return true;
				}
			}
			return GuiUtils.askDialog(this, localizer, dialog, width, height);
		}
	}
	
	@OnAction("action:/newKeyStore")
	private void newKeystore() {
		final CreateKeystoreDialog	cks = new CreateKeystoreDialog(state, PureLibClient.PROVIDER);
		
		if (GuiUtils.askDialog(this, localizer, cks, 300, 100)) {
//			try{final KeyStoreController	ks = new KeyStoreController();
//				ks.load(null,cks.password);
//				
//				contentManipulator.newFile();
//				contentManipulator.setModificationFlag();
//				
//				final KeyStore	ks = KeyStore.getInstance(cks.type);
//				ks.load(null,cks.password);
//				
//				refreshLeftPanel("<new>",ks,cks.password);
//				state.message(Severity.info,"New ["+cks.type+"] key store created");
//			} catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
//				state.message(Severity.error,"Error creating keystore: "+e.getLocalizedMessage());
//			}
		}
	}
	
	
	@OnAction("action:/closeKeystore")
	private void closeKeyStore () {
		if (currentKeystore != null) {
			if (settings.keepPasswords) {
				passwords.deletePasswordFor(KEYSTORE_ITEM_ID);
			}
			currentKeystore = null;
			setPlaceHolder();
			leftSplit.setDividerLocation(0.5);
		}
	}
	
	@OnAction("action:/exit")
	private void exitApplication () {
		setVisible(false);
		dispose();
		latch.countDown();
	}

	@OnAction("action:/builtin.languages")
	private void selectLang(final Hashtable<String,String[]> langs) {
		final SupportedLanguages	newLang = SupportedLanguages.valueOf(langs.get("lang")[0]);
		final Locale				newLocale = newLang.getLocale();
		
		PureLibSettings.PURELIB_LOCALIZER.setCurrentLocale(newLocale);
		settings.currentLang = newLang.getLocale().getLanguage();
	}
	
	@OnAction("action:/helpAbout")
	private void showAboutScreen() {
		SwingUtils.showAboutScreen(this, localizer, TITLE_HELP_ABOUT_APPLICATION, HELP_ABOUT_APPLICATION, URI.create("root://chav1961.ksmgr.Application/chav1961/ksmgr/avatar.jpg"), new Dimension(300,300));
	}

	private void setPlaceHolder() {
		leftSplit.setRightComponent(new JDropTargetPlaceholder(localizer, LEFT_PLACEHOLDER_APPLICATION, DataFlavor.javaFileListFlavor) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean processDropOperation(final DataFlavor flavor, final Object content) throws ContentException, IOException {
				if (flavor.equals(DataFlavor.javaFileListFlavor) && !((List<File>)content).isEmpty()) {
					for (File item : (List<File>)content) {
						openKeystore(item);
						break;
					}
					leftSplit.setDividerLocation(0.5);
					return true;
				}
				else {
					return false;
				}
			}
		});
	}
	
	private void openKeystore(final File keystore) {
		final AskPasswordDialog	apd = new AskPasswordDialog(state);
		
		if (askPassword(apd, KEYSTORE_ITEM_ID, 250, 50) && openKeystore(keystore, apd.password)) {
			if (settings.keepPasswords) {
				passwords.storePasswordFor(KEYSTORE_ITEM_ID, apd.password);
			}
		}
	}

	private boolean openKeystore(final File file, final char[] password) {
		try{final KeyStore 			ks = KeyStore.getInstance(file, password);
			final KeyStoreViewer	ksv = new KeyStoreViewer(app.getRoot(), localizer, state, passwords, file.getName(), ks); 
			
			currentKeystore = ks; 
			leftSplit.setRightComponent(new JHighlightedScrollPane(ksv));
			ksv.requestFocusInWindow();
			return true;
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			getLogger().message(Severity.error, e, "Keystore error: "+e.getLocalizedMessage());
			return false;
		}		
	}

	
	private void refreshMenuState(final KeyStoreState state) {
		for (String item : state.getEnabledItems()) {
			refreshMenuState(item, true);
		}
		for (String item : state.getDisabledItems()) {
			refreshMenuState(item, false);
		}
	}
	
	private void refreshMenuState(final String modelName, final boolean enableState) {
		// TODO Auto-generated method stub
		
	}

	private void fillLocalizedStrings() throws LocalizationException, IllegalArgumentException {
		setTitle(localizer.getValue(TITLE_APPLICATION));
	}

	public static void main(final String[] args) throws IOException, EnvironmentException, FlowException, ContentException, HeadlessException, URISyntaxException {
		final ArgParser		parser = new ApplicationArgParser().parse(args);
		
		try(final InputStream				is = Application.class.getResourceAsStream("application.xml")) {
			final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
			final CountDownLatch			latch = new CountDownLatch(1);
			
			PureLibClient.registerInPureLib();
			PureLibSettings.instance().setProperty("helpReference","http://localhost:"+parser.getValue(ARG_HELP_PORT,int.class));
			final Application	application = new Application(xda,PureLibSettings.PURELIB_LOCALIZER,parser.getValue(ARG_HELP_PORT,int.class), parser.isTyped(ARG_LOCAL_CONFIG) ? parser.getValue(ARG_LOCAL_CONFIG,String.class) : ARG_LOCAL_CONFIG_DEFAULT, latch);
			
			application.setVisible(true);
			latch.await();
		} catch (InterruptedException e) {
		}
	}
	
	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new IntegerArg(ARG_HELP_PORT, true, "Help port to use for help browser", PureLibSettings.instance().getProperty(PureLibSettings.BUILTIN_HELP_PORT,int.class),new long[][]{{1024,65535}}),
			new URIArg(ARG_LOCAL_CONFIG, false, false, "Local configuration descriptor")
		};
		
		ApplicationArgParser() {
			super(KEYS);
		}
	}

}
