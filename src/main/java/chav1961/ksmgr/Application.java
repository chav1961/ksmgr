package chav1961.ksmgr;


import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigInteger;
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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
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
import chav1961.ksmgr.internal.KeyStoreViewer;
import chav1961.ksmgr.internal.PureLibClient;
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
import chav1961.purelib.fsys.FileSystemOnFile;
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
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator.FileContentChangeListener;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator.FileContentChangedEvent;
import chav1961.purelib.ui.swing.useful.JFileList;
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
			
			this.localHelpPort = helpPort;
			this.latch = latch;
			this.state = new JStateString(this.localizer,10);
			this.settings = new CurrentSettingsDialog(state,configFile);
			this.menu = SwingUtils.toJComponent(app.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class);
			SwingUtils.assignActionListeners(menu,this);
			
			this.contentManipulator = new JFileContentManipulator(new FileSystemOnFile(URI.create("file://./")),this.localizer
												,()->{return new InputStream() {@Override public int read() throws IOException {return -1;}};}
												,()->{return new OutputStream() {@Override public void write(int b) throws IOException {}};}
												,this.settings);
			this.contentManipulator.addFileContentChangeListener(listener);
			this.rightContentManipulator = new JFileContentManipulator(new FileSystemOnFile(URI.create("file://./")),this.localizer
								,()->{return new InputStream() {@Override public int read() throws IOException {return -1;}};}
								,()->{return new OutputStream() {@Override public void write(int b) throws IOException {}};}
								,this.settings);
			this.rightContentManipulator.addFileContentChangeListener(listener);

			state.setAutomaticClearTime(Severity.error,1,TimeUnit.MINUTES);
			state.setAutomaticClearTime(Severity.warning,15,TimeUnit.SECONDS);
			state.setAutomaticClearTime(Severity.info,5,TimeUnit.SECONDS);
			((JMenu)SwingUtils.findComponentByName(menu, "menu.tasks")).setEnabled(false);
			((JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.savekeystore")).setEnabled(false);
			((JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.savekeystoreas")).setEnabled(false);

			((JMenu)SwingUtils.findComponentByName(menu, "menu.file.lru")).setEnabled(!contentManipulator.getLastUsed().isEmpty());
			fillLRUSubmenu();
			
			SwingUtils.centerMainWindow(this,0.75f);
			SwingUtils.assignExitMethod4MainWindow(this,()->{exitApplication();});
			
			getContentPane().add(menu,BorderLayout.NORTH);
			getContentPane().add(split,BorderLayout.CENTER);
			getContentPane().add(state,BorderLayout.SOUTH);

			PureLibSettings.PURELIB_LOCALIZER.addLocaleChangeListener(this);
			localizer.setCurrentLocale(Locale.forLanguageTag(settings.currentLang));
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
				current.load(null,cks.password);
				
				refreshLeftPanel(ks,cks.password);
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
				
				if (askPassword(apd)) {
					final KeyStore	temp = KeyStore.getInstance(from,apd.password); 
					
					if (KeyStoreType.PKCS12.name().equalsIgnoreCase(temp.getType())) {
						refreshLeftPanel(temp,apd.password);
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
		try{if (contentManipulator.openFile(name)) {
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
				
				if (currentPassword != null || askPassword(apd)) {
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
				
				if (currentPassword != null || askPassword(apd)) {
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
		
		if (ask(kpcd,300,150) && askPassword(apd)) {
			try{final KeyPair			pair = kpcd.generate();
				final Certificate		cert = createSelfSigned(pair);
				
				current.setKeyEntry(kpcd.alias, pair.getPrivate(), apd.password, new Certificate[] {cert});
				state.message(Severity.info,"Key pair for alias ["+kpcd.alias+"] placed into the current keystore successfully");
				leftPanel.refresh();
			} catch (KeyStoreException | NoSuchAlgorithmException | OperatorCreationException | CertIOException | CertificateException e) {
				state.message(Severity.error,"Error creating key pair for alias ["+kpcd.alias+"]: "+e.getLocalizedMessage());
			}
		}
	}
		
	@OnAction("action:/certificatesCreateSelfsigned")
	private void createSelfSignedCeritificate() {
		final SelfSignedCertificateCreateDialog	ssscd = new SelfSignedCertificateCreateDialog(state,current);
		
		if (ask(ssscd,350,300)) {
			try{final Certificate	cert = ssscd.generate();
				current.setCertificateEntry(ssscd.alias,cert);
				state.message(Severity.info,"Certificate for alias ["+ssscd.alias+"] placed into the current keystore successfully");
				leftPanel.refresh();
			} catch (CertificateEncodingException | InvalidKeyException | NoSuchProviderException | NoSuchAlgorithmException | SignatureException | KeyStoreException e) {
				state.message(Severity.error,"Error creating certificate for alias ["+ssscd.alias+"]: "+e.getLocalizedMessage());
			}
		}
	}
	
	
	private Certificate createSelfSigned(final KeyPair pair)  throws OperatorCreationException, CertIOException, CertificateException {
	        X500Name dnName = new X500Name("CN=publickeystorageonly");
	        BigInteger certSerialNumber = BigInteger.ONE;

	        Date startDate = new Date(); // now

	        Calendar calendar = Calendar.getInstance();
	        calendar.setTime(startDate);
	        calendar.add(Calendar.YEAR, 1);
	        Date endDate = calendar.getTime();

	        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(pair.getPrivate());
	        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, pair.getPublic());

	        return new JcaX509CertificateConverter().getCertificate(certBuilder.build(contentSigner));
	    }

	@OnAction("action:/asKeyStore")
	private void asKeyStore() {
		final JMenu			edit = (JMenu)SwingUtils.findComponentByName(menu, "menu.edit");
		
		edit.setEnabled(true);
		if (right == null) {
			try{if (rightContentManipulator.openFile()) {
					final String			file = rightContentManipulator.getCurrentPathOfTheFile();
					final File				from = new File(file).getAbsoluteFile();
					final AskPasswordDialog	apd = new AskPasswordDialog(state);
					
					if (askPassword(apd)) {
						final KeyStore	temp = KeyStore.getInstance(from,apd.password); 
						
						if (KeyStoreType.PKCS12.name().equalsIgnoreCase(temp.getType())) {
							right = temp;
							rightPassword = settings.keepPasswords ? apd.password : null;
							state.message(Severity.info,"Keystore ["+file+"] loaded successfully, repository type type is "+current.getType());
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
		split.setRightComponent(new JScrollPane(new KeyStoreViewer(right)));		
	}

	@OnAction("action:/asProperties")
	private void asProperties() {
		final JMenu			edit = (JMenu)SwingUtils.findComponentByName(menu, "menu.edit");
		
		edit.setEnabled(false);
	}
	
	@OnAction("action:/asFileSystem")
	private void asFileSystem() throws IOException {
		final JMenu			edit = (JMenu)SwingUtils.findComponentByName(menu, "menu.edit");
		final JFileList		l = new JFileList(state,fsi,true,false,true) {
								private static final long serialVersionUID = -138848126883302434L;
								@Override
								protected void selectAndAccept(final String path) {
									final OpenKeystoreDialog	oks = new OpenKeystoreDialog(state);
									
									oks.file = path;
									if (ask(oks,300,100)) {
										try{final KeyStore	ks = KeyStore.getInstance(new File(oks.file),oks.password);
											if (leftPanel == null) {
												refreshLeftPanel(ks,oks.password);
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
							
		edit.setEnabled(true);
		split.setRightComponent(new JScrollPane(l));
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
			item.setEnabled(settings.keepPasswords);
			settings.store();
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
	
	private void fillLocalizedStrings() throws LocalizationException, IllegalArgumentException {
		setTitle(localizer.getValue(TITLE_APPLICATION));
	}

	private void openKeystore(final String file) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		final File				from = new File(file).getAbsoluteFile();
		final AskPasswordDialog	apd = new AskPasswordDialog(state);
		
		if (askPassword(apd)) {
			refreshLeftPanel(KeyStore.getInstance(from,apd.password),apd.password);
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

	private <T> boolean askPassword(final AskPasswordDialog dialog) {
		return ask(dialog,250,50);
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

	private void refreshLeftPanel(final KeyStore ks, final char[] password) {
		current = ks; 
		currentPassword = settings.keepPasswords ? password : null;
		split.setLeftComponent(new JScrollPane(leftPanel = new KeyStoreViewer(current)));
	}

	private void changeState(final FileContentChangedEvent event) {
		final JMenuItem		save = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.savekeystore");
		final JMenuItem		saveAs = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.savekeystoreas");
		final JMenu			tasks = (JMenu)SwingUtils.findComponentByName(menu, "menu.tasks");
		
		switch (event.getChangeType()) {
			case FILE_LOADED		:
				save.setEnabled(false);
				saveAs.setEnabled(true);
				tasks.setEnabled(true);
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
				tasks.setEnabled(true);
				break;
			case FILE_STORED		:
				break;
			default:
				throw new UnsupportedOperationException("Change event type ["+event.getChangeType()+"] is not supported yet");
		}
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
			new Application(xda,PureLibSettings.PURELIB_LOCALIZER,parser.getValue(ARG_HELP_PORT,int.class), parser.isTyped(ARG_LOCAL_CONFIG) ? parser.getValue(ARG_LOCAL_CONFIG,String.class) : ARG_LOCAL_CONFIG_DEFAULT, latch).setVisible(true);
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
	}}
