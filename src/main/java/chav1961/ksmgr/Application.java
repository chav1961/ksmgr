package chav1961.ksmgr;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.KeyTransRecipientInformation;
import org.bouncycastle.cms.RecipientInfoGenerator;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Store;

import chav1961.ksmgr.dialogs.AskPasswordDialog;
import chav1961.ksmgr.dialogs.ChangePasswordDialog;
import chav1961.ksmgr.dialogs.CreateKeystoreDialog;
import chav1961.ksmgr.dialogs.CurrentSettingsDialog;
import chav1961.ksmgr.dialogs.KeyPairCreateDialog;
import chav1961.ksmgr.dialogs.OpenKeystoreDialog;
import chav1961.ksmgr.dialogs.SelfSignedCertificateCreateDialog;
import chav1961.ksmgr.interfaces.KeyStoreType;
import chav1961.ksmgr.internal.AlgorithmRepo;
import chav1961.ksmgr.internal.KeyStoreUtils;
import chav1961.ksmgr.internal.KeyStoreViewer;
import chav1961.ksmgr.internal.KeyStoreViewer.ItemDescriptor;
import chav1961.ksmgr.internal.KeyStoreViewer.KeyStoreModel;
import chav1961.ksmgr.internal.PasswordsRepo;
import chav1961.ksmgr.internal.PureLibClient;
import chav1961.ksmgr.internal.RightPanelContainer;
import chav1961.ksmgr.internal.RightPanelContainer.RightPanelType;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.MimeParseException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.DnDManager;
import chav1961.purelib.ui.swing.useful.DnDManager.DnDInterface;
import chav1961.purelib.ui.swing.useful.DnDManager.DnDMode;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator.FileContentChangeListener;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator.FileContentChangedEvent;
import chav1961.purelib.ui.swing.useful.JFileList;
import chav1961.purelib.ui.swing.useful.JFileListItemDescriptor;
import chav1961.purelib.ui.swing.useful.JStateString;

// https://www.baeldung.com/java-bouncy-castle
public class Application extends JFrame implements LocaleChangeListener {
	private static final long 				serialVersionUID = 4122983600087461494L;	
	public static final String				ARG_HELP_PORT = "port";
	public static final String				ARG_LOCAL_CONFIG = "localConfig";
	public static final String				ARG_LOCAL_CONFIG_DEFAULT = ".ksmgr.config";
	public static final String				TITLE_APPLICATION = "application.title";
	public static final String				HELP_ABOUT_APPLICATION = "application.help";
	public static final String				TITLE_HELP_ABOUT_APPLICATION = "application.help.title";
	public static final String				LEFT_PLACEHOLDER_APPLICATION = "application.placeholder.left";

	private static final String				KEYSTORE_TARGET = "keystore.target";

	private static Application				application;
	
	private final ContentMetadataInterface 	app;
	private final Localizer					localizer;
	private final int 						localHelpPort;
	private final CountDownLatch			latch;
	private final JMenuBar					menu;
	private final JStateString				state;
	private final JFileContentManipulator	contentManipulator, rightContentManipulator;
	private final JSplitPane				split = new JSplitPane();
	private final CurrentSettingsDialog		settings;
	private final FileContentChangeListener	listener = (e)->SwingUtilities.invokeLater(()->changeState(e));
	private final ActionListener			lruListener = (e)->openLRUKeystore(e.getActionCommand());
	private final FileSystemInterface		fsi = FileSystemFactory.createFileSystem(URI.create("fsys:file:/"));
	private final RightPanelContainer		rightPanel;
	private final DnDManager				dnd;
	private final AlgorithmRepo				algo = new AlgorithmRepo();
	private final PasswordsRepo				passwords;

	private KeyStoreViewer					leftPanel = null;
	private	KeyStore						current = null, right = null;
	private char[]							currentPassword = null, rightPassword = null;

	public Application(final ContentMetadataInterface xda, final Localizer parent, final int helpPort, final String configFile, final CountDownLatch latch) throws NullPointerException, IllegalArgumentException, EnvironmentException, IOException, FlowException, SyntaxException, PreparationException, ContentException {
//		try{final CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
//		
//			final X509Certificate 		certificate = (X509Certificate) certFactory.generateCertificate(new FileInputStream("Baeldung.cer"));
//			 
//			final char[] 				keystorePassword = "password".toCharArray();
//			final char[] 				keyPassword = "password".toCharArray();
//					 
//			final KeyStore 				keystore = KeyStore.getInstance("PKCS12");
//			
//			keystore.load(new FileInputStream("Baeldung.p12"), keystorePassword);
//			final PrivateKey 			key = (PrivateKey) keystore.getKey("baeldung", keyPassword);	
//		} catch (CertificateException | NoSuchProviderException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
			
			parent.push(localizer);
			localizer.addLocaleChangeListener(this);
			
			final JLabel	label = new JLabel(); 
			
			label.setName(KEYSTORE_TARGET);
			split.setLeftComponent(label);
			
			this.localHelpPort = helpPort;
			this.latch = latch;
			this.state = new JStateString(this.localizer,10);
			this.settings = new CurrentSettingsDialog(state,configFile,algo);
			this.passwords = new PasswordsRepo(settings.keepPasswords);
			
			this.menu = SwingUtils.toJComponent(app.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class);
			((JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.savekeystore")).setEnabled(false);
			((JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.savekeystoreas")).setEnabled(false);
			((JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.changePassword")).setEnabled(false);
			SwingUtils.assignActionListeners(menu,this);

			this.rightPanel = new RightPanelContainer(this,localizer,state,fsi.clone(),split);
			asFileSystem();
			
			this.contentManipulator = new JFileContentManipulator(fsi.clone(),this.localizer
												,()->{return new InputStream() {@Override public int read() throws IOException {return -1;}};}
												,()->{return new OutputStream() {@Override public void write(int b) throws IOException {}};}
												,this.settings);
			this.contentManipulator.addFileContentChangeListener(listener);
			this.rightContentManipulator = new JFileContentManipulator(fsi.clone(),this.localizer
												,()->{return new InputStream() {@Override public int read() throws IOException {return -1;}};}
												,()->{return new OutputStream() {@Override public void write(int b) throws IOException {}};}
												,this.settings);
			this.rightContentManipulator.addFileContentChangeListener(listener);

			state.setAutomaticClearTime(Severity.error,1,TimeUnit.MINUTES);
			state.setAutomaticClearTime(Severity.warning,15,TimeUnit.SECONDS);
			state.setAutomaticClearTime(Severity.info,5,TimeUnit.SECONDS);
			((JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.savekeystore")).setEnabled(false);
			((JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.savekeystoreas")).setEnabled(false);

			((JMenu)SwingUtils.findComponentByName(menu, "menu.file.lru")).setEnabled(!contentManipulator.getLastUsed().isEmpty());
			fillLRUSubmenu();
			
			SwingUtils.centerMainWindow(this,0.75f);
			SwingUtils.assignExitMethod4MainWindow(this,()->{exitApplication();});
			
			getContentPane().add(menu,BorderLayout.NORTH);
			getContentPane().add(split,BorderLayout.CENTER);
			getContentPane().add(state,BorderLayout.SOUTH);
			this.dnd = new DnDManager(split, new DnDInterface() {
				@Override public void track(final DnDMode currentMode, final Component from, final int xFromAbsolute, final int yFromAbsolute, final Component to, final int xToAbsolute, final int yToAbsolute) {}
				
				@Override
				public Class<?> getSourceContentClass(final DnDMode currentMode, final Component component, final int x, final int y) {
					if (component instanceof JFileList) {
						final int	index = ((JFileList)component).locationToIndex(new Point(x,y)); 
					
						if (index >= 0) {
							final JFileListItemDescriptor	desc = (((JFileList)component).getModel()).getElementAt(index);
							
							return desc.isDirectory() ? null : FromFileSystem.class;
						}
					}
					else if (component instanceof KeyStoreViewer) {
						final int	index = leftPanel.rowAtPoint(new Point(x,y));
						
						if (index >= 0) {
							return FromKeyStore.class;
						}
					}
					return null;
				}

				@Override
				public Object getSourceContent(final DnDMode currentMode, final Component from, final int xFrom, final int yFrom, final Component to, final int xTo, final int yTo) {
					if (from instanceof JFileList) {
						final int	index = ((JFileList)from).locationToIndex(new Point(xFrom,yFrom)); 
					
						if (index >= 0) {
							final JFileListItemDescriptor	desc = (((JFileList)from).getModel()).getElementAt(index);
							
							return desc.isDirectory() ? null : new FromFileSystem(desc.getPath());
						}
					}
					else if (from instanceof KeyStoreViewer) {
						final int	index = leftPanel.rowAtPoint(new Point(xFrom,yFrom));
						
						if (index >= 0) {
							final ItemDescriptor	item = (ItemDescriptor) ((KeyStoreModel)leftPanel.getModel()).getValueAt(index,0);
							
							return new FromKeyStore(item.alias,current);
						}
					}
					return null;
				}

				@Override
				public boolean canReceive(final DnDMode currentMode, final Component from, final int xFrom, final int yFrom, final Component to, final int xTo, final int yTo, final Class<?> contentClass) {
					if ((from instanceof JFileList) && (to instanceof KeyStoreViewer)) {		// Copy into keystore
						return true;
					}
					else if ((from instanceof KeyStoreViewer) && (to instanceof JFileList)) {	// Copy from keystore
						return true;
					}
					else if ((from instanceof KeyStoreViewer) && (to instanceof KeyStoreViewer)) {	// Keystore/keystore operations
						return true;
					}
					else if ((from instanceof JFileList) && (to instanceof JLabel) && (KEYSTORE_TARGET.equals(to.getName()))) {	// Open keystore after start
						return true;
					}
					else if ((from instanceof JFileList) && (to instanceof JViewport) && leftPanel != null) {	// Append content into keystore
						return true;
					}
					else {
						return false;
					}
				}

				@Override
				public void complete(final DnDMode currentMode, final Component from, final int xFrom, final int yFrom, final Component to, final int xTo, final int yTo, final Object content) {
					if ((from instanceof JFileList) && (to instanceof JLabel)) {
						
						try{if (contentManipulator.openFile(((FromFileSystem)content).name)) {
								openKeystore(((FromFileSystem)content).name);
							}
						} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
							state.message(Severity.error,"Error opening keystore: "+e.getLocalizedMessage());
						}
					}
					else if ((from instanceof JFileList) && ((to instanceof KeyStoreViewer) || (to instanceof JViewport))) {
						final AskPasswordDialog	apd = new AskPasswordDialog(state);
						
						if (askPassword(apd,null)) {
							KeyStoreUtils.keyPairsImport(fsi,state,((FromFileSystem)content).name,current,apd.password);
							leftPanel.refresh();
						}
					}
					else if ((from instanceof KeyStoreViewer) && (to instanceof JFileList)) {
						final FromKeyStore	fks = (FromKeyStore)content;
						
						try{
							if (fks.ks.isKeyEntry(fks.name)) {
								final AskPasswordDialog	apd = new AskPasswordDialog(state);
								
								if (askPassword(apd,((FromKeyStore)content).name)) {
									KeyStoreUtils.keyPairsExport(fsi,state,((FromKeyStore)content).ks,((FromKeyStore)content).name,rightPanel.getCurrentFileSystemPath(),apd.password);
									rightPanel.refresh();
								}
							}
							else if (fks.ks.isCertificateEntry(fks.name)) {
								KeyStoreUtils.certificateExport(fsi,state,((FromKeyStore)content).ks,((FromKeyStore)content).name,rightPanel.getCurrentFileSystemPath());
								rightPanel.refresh();
							}
						} catch (KeyStoreException e) {
							state.message(Severity.error,"Error exporting keystore: "+e.getLocalizedMessage());
						}
						
					}
				}
			});
			this.dnd.selectDnDMode(DnDMode.COPY);

			PureLibSettings.PURELIB_LOCALIZER.addLocaleChangeListener(this);
			localizer.setCurrentLocale(Locale.forLanguageTag(settings.currentLang));
			fillLocalizedStrings();
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		SwingUtils.refreshLocale(state,oldLocale,newLocale);
		SwingUtils.refreshLocale(menu,oldLocale,newLocale);
	}

	@OnAction("action:/newKeyStore")
	private void newKeystore() {
		final CreateKeystoreDialog	cks = new CreateKeystoreDialog(state);
		
		if (ask(cks,300,100)) {
			try{contentManipulator.newFile();
				contentManipulator.setModificationFlag();
				
				final KeyStore	ks = KeyStore.getInstance(cks.type.name());
				ks.load(null,cks.password);
				
				refreshLeftPanel("<new>",ks,cks.password);
				state.message(Severity.info,"["+cks.type+"] key store created");
			} catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
				state.message(Severity.error,"Error creating keystore: "+e.getLocalizedMessage());
			}
		}
	}

	@OnAction("action:/openKeyStore")
	private void openKeystore() {
		try{if (contentManipulator.openFile()) {
				openKeystore(contentManipulator.getCurrentPathOfTheFile());
			}
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			state.message(Severity.error,"Error opening keystore: "+e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/openKeyStorePKCS12")
	private void openKeystorePKCS12() {
		try{if (contentManipulator.openFile()) {
				final String			file = contentManipulator.getCurrentPathOfTheFile();
				final File				from = new File(file).getAbsoluteFile();
				final AskPasswordDialog	apd = new AskPasswordDialog(state);
				
				if (askPassword(apd,null)) {
					final KeyStore	temp = KeyStore.getInstance(from,apd.password); 
					
					if (KeyStoreType.PKCS12.name().equalsIgnoreCase(temp.getType())) {
						refreshLeftPanel(file,temp,apd.password);
						state.message(Severity.info,"Keystore ["+file+"] loaded successfully, repository type type is "+current.getType());
					}
					else {
						state.message(Severity.error,"Error opening keystore: reystore type is "+current.getType()+", not PKCS12.");
					}
				}
			}
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			state.message(Severity.error,"Error opening keystore: "+e.getLocalizedMessage());
		}
	}

	private void openLRUKeystore(final String name) {
		try{if (contentManipulator.openLRUFile(name)) {
				try{openKeystore(name);
				} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
					state.message(Severity.error,"Error opening keystore: "+e.getLocalizedMessage());
				}
			}
			else {
				state.message(Severity.error,"Error opening keystore: file ["+name+"] is not exists or not accessible");
			}
		} catch (IOException e) {
		}
	}	

	@OnAction("action:/saveKeyStore")
	private void saveKeystore() {
		try{if (contentManipulator.saveFile()) {
				final AskPasswordDialog	apd = new AskPasswordDialog(state);
				
				if (currentPassword != null || askPassword(apd,null)) {
					final String	file = contentManipulator.getCurrentPathOfTheFile();
					
					try (final OutputStream	os = new FileOutputStream(new File(file))) {
						current.store(os,currentPassword != null ? currentPassword : apd.password);
					}
					state.message(Severity.info,"Keystore ["+file+"] was saved successfully");
				}
			}
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			state.message(Severity.error,"Error opening keystore: "+e.getLocalizedMessage());
		}
	}

	@OnAction("action:/saveKeyStoreAs")
	private void saveKeystoreAs() {
		try{if (contentManipulator.saveFileAs()) {
				final AskPasswordDialog	apd = new AskPasswordDialog(state);
				
				if (currentPassword != null || askPassword(apd, null)) {
					final String	file = contentManipulator.getCurrentPathOfTheFile();
					
					try (final OutputStream	os = new FileOutputStream(new File(file))) {
						current.store(os,currentPassword != null ? currentPassword : apd.password);
					}
					state.message(Severity.info,"Keystore ["+file+"] saved successfully");
				}
			}
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			state.message(Severity.error,"Error opening keystore: "+e.getLocalizedMessage());
		}
	}

	@OnAction("action:/changePassword")
	private void changePassword() {
		final ChangePasswordDialog	cpd = new ChangePasswordDialog(state);
	
		if (ask(cpd,300,100)) {
			currentPassword = cpd.newPassword;
			state.message(Severity.info,"Password was changed successfully");
		}
	}

	
	@OnAction("action:/keyPairsGenerate")
	private void keyPairsGenerate() {
		final KeyPairCreateDialog	kpcd = new KeyPairCreateDialog(state,current);
		final AskPasswordDialog	apd = new AskPasswordDialog(state);
		
		if (ask(kpcd,300,150) && askPassword(apd,kpcd.alias)) {
			try{final KeyPair			pair = kpcd.generate();
				final Certificate		cert = KeyStoreUtils.createSelfSigned(pair);
				
				current.setKeyEntry(kpcd.alias, pair.getPrivate(), apd.password, new Certificate[] {cert});
				passwords.storePasswordFor(kpcd.alias, apd.password);
				state.message(Severity.info,"Key pair for alias ["+kpcd.alias+"] placed into the current keystore successfully");
				leftPanel.refresh();
			} catch (KeyStoreException | NoSuchAlgorithmException | OperatorCreationException | CertIOException | CertificateException e) {
				state.message(Severity.error,"Error creating key pair for alias ["+kpcd.alias+"]: "+e.getLocalizedMessage());
			}
		}
	}

	@OnAction("action:/keyPairsExport")
	private void keyPairsExport() {
		final int[]		indices = leftPanel.getSelectionModel().getSelectedIndices();
		
		if (indices.length > 0) {
			final AskPasswordDialog	apd = new AskPasswordDialog(state);
			int						count = 0;
			
			for (int index = 0; index < indices.length; index++) {
				final ItemDescriptor	desc = (ItemDescriptor) leftPanel.getModel().getValueAt(indices[index],0);
				
				try{state.message(Severity.info,"Export ["+desc.alias+"]...");
					if (current.isKeyEntry(desc.alias) && askPassword(apd,desc.alias)) {
						KeyStoreUtils.keyPairsExport(fsi, state, current, desc.alias, rightPanel.getCurrentFileSystemPath(), apd.password);
						count++;
					}
				} catch (KeyStoreException e) {
					state.message(Severity.error,"Error exporting key pair for alias ["+desc.alias+"]: "+e.getLocalizedMessage());
				}
			}
			rightPanel.refresh();
			state.message(Severity.info,"Key pairs export completed, ["+count+"] item(s) exported");
		}
	}

	@OnAction("action:/keyPairsImport")
	private void keyPairsImport() {
		final AskPasswordDialog	apd = new AskPasswordDialog(state);
		
		if (askPassword(apd,null)) {
			final String	item = KeyStoreUtils.keyPairsImport(fsi,state,rightPanel.getCurrentFileSystemFile(),current,apd.password);
			
			passwords.storePasswordFor(item, apd.password);
			state.message(Severity.info,"Key pairs import completed, ["+rightPanel.getCurrentFileSystemFile()+"] item imported");
			leftPanel.refresh();
		}
	}	

	@OnAction("action:/keyPairsGenerateAndExport")
	private void keyPairsGenerateAndExport() {
		final KeyPairCreateDialog	kpcd = new KeyPairCreateDialog(state,current);
		final AskPasswordDialog	apd = new AskPasswordDialog(state);
		
		if (ask(kpcd,300,150) && askPassword(apd,kpcd.alias)) {
			try{final KeyPair			pair = kpcd.generate();
				final Certificate		cert = KeyStoreUtils.createSelfSigned(pair);
				
				current.setKeyEntry(kpcd.alias, pair.getPrivate(), apd.password, new Certificate[] {cert});
				passwords.storePasswordFor(kpcd.alias, apd.password);
				KeyStoreUtils.keyPairsExport(fsi, state, current, kpcd.alias, rightPanel.getCurrentFileSystemPath(), apd.password);
				
				state.message(Severity.info,"Key pair for alias ["+kpcd.alias+"] placed into the current keystore and exported to ["+rightPanel.getCurrentFileSystemPath()+"] successfully");
				leftPanel.refresh();
				rightPanel.refresh();
			} catch (KeyStoreException | NoSuchAlgorithmException | OperatorCreationException | CertIOException | CertificateException e) {
				state.message(Severity.error,"Error creating key pair for alias ["+kpcd.alias+"]: "+e.getLocalizedMessage());
			}
		}
	}
	
	@OnAction("action:/certificatesExport")
	private void certificatesExport() {
		final int[]		indices = leftPanel.getSelectionModel().getSelectedIndices();
		int				count = 0;
		
		if (indices.length > 0) {
			for (int index = 0; index < indices.length; index++) {
				final ItemDescriptor	desc = (ItemDescriptor) leftPanel.getModel().getValueAt(indices[index],0);
				
				try{if (current.isCertificateEntry(desc.alias)) {
						KeyStoreUtils.certificateExport(fsi, state, current, desc.alias, rightPanel.getCurrentFileSystemPath());
						count++;
					}
				} catch (KeyStoreException e) {
					state.message(Severity.error,"Error exporting certificate for alias ["+desc.alias+"]: "+e.getLocalizedMessage());
				}
			}
			rightPanel.refresh();
			state.message(Severity.info,"Certificate export completed, ["+count+"] item(s) exported");
		}
	}

	@OnAction("action:/certificatesPrepareRequest")
	private void certificatesPrepareRequest() {
		final int[]		indices = leftPanel.getSelectionModel().getSelectedIndices();
		int				count = 0;
		
		if (indices.length > 0) {
			final AskPasswordDialog	apd = new AskPasswordDialog(state);
			
			for (int index = 0; index < indices.length; index++) {
				final ItemDescriptor	desc = (ItemDescriptor) leftPanel.getModel().getValueAt(indices[index],0);
				
				try{state.message(Severity.info,"Export ["+desc.alias+"]...");
					if (current.isKeyEntry(desc.alias) && askPassword(apd,desc.alias)) {
						KeyStoreUtils.certificateRequestExport(fsi, state, current, desc.alias, settings.principalName, rightPanel.getCurrentFileSystemPath(), apd.password);
						count++;
					}
				} catch (KeyStoreException e) {
					state.message(Severity.error,"Error creating certificate request for alias ["+desc.alias+"]: "+e.getLocalizedMessage());
				}
			}
			rightPanel.refresh();
			state.message(Severity.info,"Certificate requests export completed, ["+count+"] item(s) created and exported");
		}
	}
	
	@OnAction("action:/certificatesCreateSelfsigned")
	private void createSelfSignedCeritificate() {
		final SelfSignedCertificateCreateDialog	ssscd = new SelfSignedCertificateCreateDialog(state,current);
		
		if (ask(ssscd,350,300)) {
			try{final Certificate	cert = ssscd.generate(settings.principalName);
				current.setCertificateEntry(ssscd.alias,cert);
				state.message(Severity.info,"Certificate for alias ["+ssscd.alias+"] created successfully");
				leftPanel.refresh();
			} catch (CertificateEncodingException | InvalidKeyException | NoSuchProviderException | NoSuchAlgorithmException | SignatureException | KeyStoreException e) {
				state.message(Severity.error,"Error creating certificate for alias ["+ssscd.alias+"]: "+e.getLocalizedMessage());
			}
		}
	}
	
	@OnAction("action:/asKeyStore")
	private void asKeyStore() {
		if (right == null) {
			try{if (rightContentManipulator.openFile()) {
					final String			file = rightContentManipulator.getCurrentPathOfTheFile();
					final File				from = new File(file).getAbsoluteFile();
					final AskPasswordDialog	apd = new AskPasswordDialog(state);
					
					if (askPassword(apd,null)) {
						final KeyStore	temp = KeyStore.getInstance(from,apd.password); 
						
						if (KeyStoreType.PKCS12.name().equalsIgnoreCase(temp.getType())) {
							right = temp;
							rightPassword = settings.keepPasswords ? apd.password : null;
							state.message(Severity.info,"Keystore ["+file+"] loaded successfully, repository type type is "+current.getType());
							rightPanel.setPanelTypeAsKeystore(app.getRoot(),file,right);
							setRightPanel(RightPanelType.AS_KEYSTORE);
						}
						else {
							state.message(Severity.error,"Error opening keystore: reystore type is "+current.getType()+", not PKCS12.");
							return;
						}
					}
					else {
						return;
					}
				}
				else {
					return;
				}
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
				state.message(Severity.error,"Error opening key store: "+e.getLocalizedMessage());
				return;
			}		
		}
	}

	@OnAction("action:/asFileSystem")
	private void asFileSystem() throws IOException {
		final JFileList		l = new JFileList(state,fsi.clone(),true,false,true) {
								private static final long serialVersionUID = -138848126883302434L;
								@Override
								protected void selectAndAccept(final String path) {
									final OpenKeystoreDialog	oks = new OpenKeystoreDialog(state);
									
									oks.file = path;
									if (ask(oks,300,100)) {
										try{final KeyStore	ks = KeyStore.getInstance(new File(oks.file),oks.password);
											if (leftPanel == null) {
												refreshLeftPanel(oks.file,ks,oks.password);
											}
											else {
												right = ks;
												rightPassword = settings.keepPasswords ? oks.password : null;
												((JMenuItem)SwingUtils.findComponentByName(menu, "menu.settings.view.keystore")).doClick();
											}
										} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
											state.message(Severity.error,"Error opening key store ["+oks.file+"]: "+e.getLocalizedMessage());
										}
									}
								}
							};
							
		rightPanel.setPanelTypeAsFileSystem(l);
		setRightPanel(RightPanelType.AS_FILESYSTEM);
	}
	
	private void setRightPanel(final RightPanelType type) {
		switch (type) {
			case AS_FILESYSTEM	:
				((JRadioButtonMenuItem)SwingUtils.findComponentByName(menu, "menu.settings.view.filesystem")).setSelected(true);
				break;
			case AS_KEYSTORE	:
				((JRadioButtonMenuItem)SwingUtils.findComponentByName(menu, "menu.settings.view.keystore")).setSelected(true);
				break;
			case UNKNOWN		:
				break;
			default	:
				throw new UnsupportedOperationException("Right panel type ["+type+"] is not supported yet");
		}
		refreshMenuState();
	}
	
	@OnAction("action:/builtin.languages")
	private void selectLang(final Hashtable<String,String[]> langs) throws LocalizationException {
		final SupportedLanguages	newLang = SupportedLanguages.valueOf(langs.get("lang")[0]);
		final Locale				newLocale = newLang.getLocale();
		
		PureLibSettings.PURELIB_LOCALIZER.setCurrentLocale(newLocale);
		localizer.setCurrentLocale(newLocale);
		settings.currentLang = newLang.getLocale().getLanguage();
	}
	
	@OnAction("action:/exit")
	private void exitApplication () {
		try{contentManipulator.removeFileContentChangeListener(listener);
			contentManipulator.close();
			settings.store();
			setVisible(false);
			dispose();
			latch.countDown();
		} catch (IOException e) {
			state.message(Severity.error,"Error saving content: "+e.getLocalizedMessage());
		}
	}

	@OnAction("action:/settings")
	private void settings() {
		if (ask(settings,300,100)) {
			final JMenuItem		item = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.changePassword");
			
			if (!settings.keepPasswords) {
				currentPassword = null;
			}
			if (settings.keepPasswords != passwords.isKeepedPasswords()) {
				passwords.setKeepedPasswords(settings.keepPasswords);
			}
			item.setEnabled(settings.keepPasswords);
			settings.store();
			leftPanel.refresh();
			rightPanel.refresh();
		}
	}
	
	@OnAction("action:/helpOverview")
	private void startBrowser () {
		if (Desktop.isDesktopSupported()) {
			try{Desktop.getDesktop().browse(URI.create("http://localhost:"+localHelpPort+"/static/index.cre"));
			} catch (IOException exc) {
				state.message(Severity.error,"Error starting browser: "+exc.getLocalizedMessage());
			}
		}
	}

	@OnAction("action:/helpAbout")
	private void showAboutScreen() {
		try{final JEditorPane 	pane = new JEditorPane("text/html",null);
			final Icon			icon = new ImageIcon(this.getClass().getResource("avatar.jpg"));
			
			try(final Reader	rdr = localizer.getContent(HELP_ABOUT_APPLICATION,new MimeType("text","x-wiki.creole"),new MimeType("text","html"))) {
				pane.read(rdr,null);
			}
			pane.setEditable(false);
			pane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			pane.setPreferredSize(new Dimension(300,300));
			pane.addHyperlinkListener(new HyperlinkListener() {
								@Override
								public void hyperlinkUpdate(final HyperlinkEvent e) {
									if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
										try{Desktop.getDesktop().browse(e.getURL().toURI());
										} catch (URISyntaxException | IOException exc) {
											exc.printStackTrace();
										}
									}
								}
			});
			
			JOptionPane.showMessageDialog(this,pane,localizer.getValue(TITLE_HELP_ABOUT_APPLICATION),JOptionPane.PLAIN_MESSAGE,icon);
		} catch (LocalizationException | MimeParseException | IOException e) {
			state.message(Severity.error,e.getLocalizedMessage());
		}
	}
	
	private void openKeystore(final String file) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		final File				from = new File(file).getAbsoluteFile();
		final AskPasswordDialog	apd = new AskPasswordDialog(state);
		
		if (askPassword(apd,null)) {
			refreshLeftPanel(file,KeyStore.getInstance(from,apd.password),apd.password);
			state.message(Severity.info,"Keystore ["+file+"] loaded successfully, repository type type is "+current.getType());
		}
	}	
	
	
	public static byte[] encryptData(byte[] data,
			  X509Certificate encryptionCertificate)
			  throws CertificateEncodingException, CMSException, IOException {
			 
			    byte[] encryptedData = null;
			    if (null != data && null != encryptionCertificate) {
			        CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator
			          = new CMSEnvelopedDataGenerator();
			        RecipientInfoGenerator transKeyGen = null;
			        JceKeyTransRecipientInfoGenerator jceKey 
			          = new JceKeyTransRecipientInfoGenerator(encryptionCertificate);
			        cmsEnvelopedDataGenerator.addRecipientInfoGenerator(transKeyGen);
			        CMSTypedData msg = new CMSProcessableByteArray(data);
			        OutputEncryptor encryptor
			          = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC)
			          .setProvider("BC").build();
			        CMSEnvelopedData cmsEnvelopedData = cmsEnvelopedDataGenerator
			          .generate(msg,encryptor);
			        encryptedData = cmsEnvelopedData.getEncoded();
			    }
			    return encryptedData;
			}	

	public static byte[] decryptData(
			  byte[] encryptedData, 
			  PrivateKey decryptionKey) 
			  throws CMSException {
			 
			    byte[] decryptedData = null;
			    if (null != encryptedData && null != decryptionKey) {
			        CMSEnvelopedData envelopedData = new CMSEnvelopedData(encryptedData);
			 
			        Collection<RecipientInformation> recipients
			          = envelopedData.getRecipientInfos().getRecipients();
			        KeyTransRecipientInformation recipientInfo 
			          = (KeyTransRecipientInformation) recipients.iterator().next();
			        JceKeyTransRecipient recipient
			          = new JceKeyTransEnvelopedRecipient(decryptionKey);
			        
			        return recipientInfo.getContent(recipient);
			    }
			    return decryptedData;
			}
	
	public static byte[] signData(
			  byte[] data, 
			  X509Certificate signingCertificate,
			  PrivateKey signingKey) throws Exception {
			 
			    byte[] signedMessage = null;
			    List<X509Certificate> certList = new ArrayList<X509Certificate>();
			    CMSTypedData cmsData= new CMSProcessableByteArray(data);
			    certList.add(signingCertificate);
			    Store certs = new JcaCertStore(certList);
			 
			    CMSSignedDataGenerator cmsGenerator = new CMSSignedDataGenerator();
			    ContentSigner contentSigner 
			      = new JcaContentSignerBuilder("SHA256withRSA").build(signingKey);
			    cmsGenerator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
			      new JcaDigestCalculatorProviderBuilder().setProvider("BC")
			      .build()).build(contentSigner, signingCertificate));
			    cmsGenerator.addCertificates(certs);
			    
			    CMSSignedData cms = cmsGenerator.generate(cmsData, true);
			    signedMessage = cms.getEncoded();
			    return signedMessage;
			}
	
	public static boolean verifSignedData(byte[] signedData)
			  throws Exception {
			 
			    X509Certificate signCert = null;
			    ByteArrayInputStream inputStream
			     = new ByteArrayInputStream(signedData);
			    ASN1InputStream asnInputStream = new ASN1InputStream(inputStream);
			    CMSSignedData cmsSignedData = new CMSSignedData(
			      ContentInfo.getInstance(asnInputStream.readObject()));
			    
			    SignerInformationStore signers 
			      = ((CMSSignedData) cmsSignedData.getCertificates()).getSignerInfos();
			    SignerInformation signer = signers.getSigners().iterator().next();
			    CollectionStore certs = null;
				Collection<X509CertificateHolder> certCollection 
			      = certs .getMatches(signer.getSID());
			    X509CertificateHolder certHolder = certCollection.iterator().next();
			    
			    return signer
			      .verify(new JcaSimpleSignerInfoVerifierBuilder()
			      .build(certHolder));
			}

	
	private <T> boolean ask(final T instance, final int width, final int height) {
		try{final ContentMetadataInterface	mdi = ContentModelFactory.forAnnotatedClass(instance.getClass());
		
			try(final AutoBuiltForm<T>		abf = new AutoBuiltForm<T>(mdi,localizer,PureLibSettings.INTERNAL_LOADER,instance,(FormManager<Object,T>)instance)) {
				
				for (Module m : abf.getUnnamedModules()) {
					instance.getClass().getModule().addExports(instance.getClass().getPackageName(),m);
				}
				abf.setPreferredSize(new Dimension(width,height));
				return AutoBuiltForm.ask(this,localizer,abf);
			}
		} catch (LocalizationException | ContentException e) {
			state.message(Severity.error,e.getLocalizedMessage());
			return false;
		} 
	}
	
	private boolean askPassword(final AskPasswordDialog dialog, final String item, final int width, final int height) {
		if (item != null) {
			if (passwords.hasPasswordFor(item)) {
				dialog.password = passwords.getPasswordFor(item);
				return true;
			}
		}
		return ask(dialog,width,height);
	}

	private void refreshLeftPanel(final String fileName, final KeyStore ks, final char[] password) {
		current = ks; 
		currentPassword = settings.keepPasswords ? password : null;
		split.setLeftComponent(new JScrollPane(leftPanel = new KeyStoreViewer(app.getRoot(),localizer,state,passwords,fileName,current)));
		split.setDividerLocation(KeyStoreViewer.PREFERRED_WIDTH);
		refreshMenuState();
	}

	private void changeState(final FileContentChangedEvent event) {
		final JMenuItem		save = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.savekeystore");
		final JMenuItem		saveAs = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.savekeystoreas");
		final JMenuItem		changePwd = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.changePassword");
		
		switch (event.getChangeType()) {
			case FILE_LOADED		:
				save.setEnabled(false);
				saveAs.setEnabled(true);
				changePwd.setEnabled(true);
			case FILE_STORED_AS		:
				fillLRUSubmenu();
				break;
			case MODIFICATION_FLAG_CLEAR	:
				save.setEnabled(false);
				break;
			case MODIFICATION_FLAG_SET		:
				save.setEnabled(true);
				break;
			case NEW_FILE_CREATED	:
				saveAs.setEnabled(true);
				changePwd.setEnabled(true);
				break;
			case FILE_STORED		:
				break;
			case LRU_LIST_REFRESHED	:
				fillLRUSubmenu();
				break;
			default:
				throw new UnsupportedOperationException("Change event type ["+event.getChangeType()+"] is not supported yet");
		}
		refreshMenuState();
	}

	private void fillLRUSubmenu() {
		final JMenu	lru = (JMenu)SwingUtils.findComponentByName(menu, "menu.file.lru");
		boolean		added = false;

		for (int index = 0; index < lru.getMenuComponentCount(); index++) {
			((JMenuItem)lru.getMenuComponent(index)).removeActionListener(lruListener);
		}
		lru.removeAll();
		for (String item : contentManipulator.getLastUsed()) {
			final String	f = new File(item).getAbsolutePath().replace(File.separatorChar,'/');
			final JMenuItem	mi = new JMenuItem(f);
			final String	name = item; 
			
			mi.addActionListener(lruListener);
			mi.setActionCommand(name);
			lru.add(mi);
			added = true;
		}
		lru.setEnabled(added);
	}

	private void refreshMenuState() {
		final JMenu			edit = (JMenu)SwingUtils.findComponentByName(menu, "menu.edit");
		final JMenu			tasks = (JMenu)SwingUtils.findComponentByName(menu, "menu.tasks");
				
		final JMenuItem		editCopy = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.edit.copy");
		final JMenuItem		editMove = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.edit.move");
		final JMenuItem		kpExport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keypairs.export");
		final JMenuItem		kpImport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keypairs.import");
		final JMenuItem		kpGenerateAndExport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keypairs.generateandexport");
		final JMenuItem		certificatesExport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keypairs.export");
		final JMenuItem		certificatesLoadTrusted = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.certificates.loadtrusted");
		final JMenuItem		certificatesRequest = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.certificates.preparerequest");
		final JMenuItem		certificatesReceive = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.certificates.receive");
		final JMenuItem		desImport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.3DESkeys.import");
		
		if (leftPanel != null) {
			edit.setEnabled(true);
			tasks.setEnabled(true);
			switch (rightPanel.getPanelType()) {
				case AS_FILESYSTEM	:
					editCopy.setEnabled(false);
					editMove.setEnabled(false);
					kpExport.setEnabled(true);
					kpImport.setEnabled(true);
					kpGenerateAndExport.setEnabled(true);
					certificatesExport.setEnabled(true);
					certificatesLoadTrusted.setEnabled(true);
					certificatesRequest.setEnabled(true);
					certificatesReceive.setEnabled(true);
					desImport.setEnabled(true);
					break;
				case AS_KEYSTORE	:
					editCopy.setEnabled(true);
					editMove.setEnabled(true);
					kpExport.setEnabled(false);
					kpImport.setEnabled(false);
					kpGenerateAndExport.setEnabled(false);
					certificatesExport.setEnabled(false);
					certificatesLoadTrusted.setEnabled(false);
					certificatesRequest.setEnabled(false);
					certificatesReceive.setEnabled(false);
					desImport.setEnabled(false);
					break;
				case UNKNOWN		:
					editCopy.setEnabled(false);
					editMove.setEnabled(false);
					kpExport.setEnabled(false);
					kpImport.setEnabled(false);
					kpGenerateAndExport.setEnabled(false);
					certificatesExport.setEnabled(false);
					certificatesLoadTrusted.setEnabled(false);
					certificatesRequest.setEnabled(false);
					certificatesReceive.setEnabled(false);
					desImport.setEnabled(false);
					break;
				default	:
					throw new UnsupportedOperationException("Right panel type ["+rightPanel.getPanelType()+"] is not supported yet");
			}
		}
		else {
			edit.setEnabled(false);
			tasks.setEnabled(false);
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException, IllegalArgumentException {
		setTitle(localizer.getValue(TITLE_APPLICATION));
		if (leftPanel == null) {
			((JLabel)split.getLeftComponent()).setText("<html>"+localizer.getValue(LEFT_PLACEHOLDER_APPLICATION).replace("\n","<br>")+"</html>");
		}
	}

	public static boolean askPassword(final AskPasswordDialog dialog, final String item) {
		if (dialog == null) {
			throw new NullPointerException("Password dialog can't be null");
		}
		else {
			return application.askPassword(dialog,item,250,50);
		}
	}

	
	public static void main(final String[] args) throws IOException, EnvironmentException, FlowException, ContentException, HeadlessException, URISyntaxException {
		final ArgParser		parser = new ApplicationArgParser().parse(args);
		final SubstitutableProperties		props = new SubstitutableProperties(Utils.mkProps(
												 NanoServiceFactory.NANOSERVICE_PORT, ""+parser.getValue(ARG_HELP_PORT,int.class)
												,NanoServiceFactory.NANOSERVICE_ROOT, "fsys:xmlReadOnly:root://chav1961.ksmgr.Application/chav1961/ksmgr/helptree.xml"
												,NanoServiceFactory.NANOSERVICE_CREOLE_PROLOGUE_URI, Application.class.getResource("prolog.cre").toString() 
												,NanoServiceFactory.NANOSERVICE_CREOLE_EPILOGUE_URI, Application.class.getResource("epilog.cre").toString() 
											));
		
		try(final InputStream				is = Application.class.getResourceAsStream("application.xml");
			final NanoServiceFactory		service = new NanoServiceFactory(PureLibSettings.CURRENT_LOGGER,props)) {
			final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
			final CountDownLatch			latch = new CountDownLatch(1);
			
			PureLibClient.registerInPureLib();
			application = new Application(xda,PureLibSettings.PURELIB_LOCALIZER,parser.getValue(ARG_HELP_PORT,int.class), parser.isTyped(ARG_LOCAL_CONFIG) ? parser.getValue(ARG_LOCAL_CONFIG,String.class) : ARG_LOCAL_CONFIG_DEFAULT, latch);
			
			application.setVisible(true);
			service.start();
			latch.await();
			service.stop();
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
	
	private static class FromFileSystem {
		private final String	name;

		public FromFileSystem(String name) {
			super();
			this.name = name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			FromFileSystem other = (FromFileSystem) obj;
			if (name == null) {
				if (other.name != null) return false;
			} else if (!name.equals(other.name)) return false;
			return true;
		}

		@Override
		public String toString() {
			return "FromFileSystem [name=" + name + "]";
		}
	}
	
	private static class FromKeyStore {
		private final String	name;
		private final KeyStore	ks;

		public FromKeyStore(final String name, KeyStore ks) {
			super();
			this.name = name;
			this.ks = ks;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			FromKeyStore other = (FromKeyStore) obj;
			if (name == null) {
				if (other.name != null) return false;
			} else if (!name.equals(other.name)) return false;
			return true;
		}

		@Override
		public String toString() {
			return "FromKeyStore [name=" + name + "]";
		}
	}
}
