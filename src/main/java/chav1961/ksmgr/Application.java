package chav1961.ksmgr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JSplitPane;

import chav1961.ksmgr.internal.PureLibClient;
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
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JDropTargetPlaceholder;
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

	private final ContentMetadataInterface 	app;
	private final Localizer					localizer;
	private final int 						localHelpPort;
	private final CountDownLatch			latch;
	private final JMenuBar					menu;
	private final JStateString				state;
	private final JSplitPane				leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final JSplitPane				totalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	
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
		
			leftSplit.setLeftComponent(new JLabel("LEFT / LEFT"));
			setPlaceHolder();
			leftSplit.setRightComponent(new JDropTargetPlaceholder(localizer, LEFT_PLACEHOLDER_APPLICATION, DataFlavor.javaFileListFlavor) {
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean processDropOperation(final DataFlavor flavor, final Object content) throws ContentException, IOException {
					leftSplit.setRightComponent(new JLabel("?????????????"));
					leftSplit.setDividerLocation(0.5);
					return true;
				}
			});
			totalSplit.setLeftComponent(leftSplit);
			totalSplit.setRightComponent(new JLabel("RIGHT / RIGHT"));
			
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
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		
		if (visible) {
			leftSplit.setDividerLocation(0.5);
			totalSplit.setDividerLocation(0.3);
		}
	}

	@OnAction("action:/closeKeystore")
	private void closeKeyStore () {
		setPlaceHolder();
		leftSplit.setDividerLocation(0.5);
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
		localizer.setCurrentLocale(newLocale);
//		settings.currentLang = newLang.getLocale().getLanguage();
	}
	
	
	@OnAction("action:/helpAbout")
	private void showAboutScreen() {
		SwingUtils.showAboutScreen(this, localizer, TITLE_HELP_ABOUT_APPLICATION, HELP_ABOUT_APPLICATION, URI.create("root://chav1961.ksmgr.Application/chav1961/ksmgr/avatar.jpg"), new Dimension(300,300));
//		
//		try{final JEditorPane 	pane = new JEditorPane("text/html",null);
//			final Icon			icon = new ImageIcon(this.getClass().getResource("avatar.jpg"));
//			
//			try(final Reader	rdr = localizer.getContent(HELP_ABOUT_APPLICATION,new MimeType("text","x-wiki.creole"),new MimeType("text","html"))) {
//				pane.read(rdr,null);
//			}
//			pane.setEditable(false);
//			pane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
//			pane.setPreferredSize(new Dimension(300,300));
//			pane.addHyperlinkListener(new HyperlinkListener() {
//								@Override
//								public void hyperlinkUpdate(final HyperlinkEvent e) {
//									if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//										try{Desktop.getDesktop().browse(e.getURL().toURI());
//										} catch (URISyntaxException | IOException exc) {
//											exc.printStackTrace();
//										}
//									}
//								}
//			});
//			
//			JOptionPane.showMessageDialog(this,pane,localizer.getValue(TITLE_HELP_ABOUT_APPLICATION),JOptionPane.PLAIN_MESSAGE,icon);
//		} catch (MimeParseException | IOException e) {
//			state.message(Severity.error, e.getLocalizedMessage());
//		}
	}

	private void setPlaceHolder() {
		leftSplit.setRightComponent(new JDropTargetPlaceholder(localizer, LEFT_PLACEHOLDER_APPLICATION, DataFlavor.javaFileListFlavor) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean processDropOperation(final DataFlavor flavor, final Object content) throws ContentException, IOException {
				leftSplit.setRightComponent(new JLabel("?????????????"));
				leftSplit.setDividerLocation(0.5);
				return true;
			}
		});
	}
	
	private void fillLocalizedStrings() throws LocalizationException, IllegalArgumentException {
		setTitle(localizer.getValue(TITLE_APPLICATION));
	}
	
	public static void main(final String[] args) throws IOException, EnvironmentException, FlowException, ContentException, HeadlessException, URISyntaxException {
		final ArgParser		parser = new ApplicationArgParser().parse(args);
		final SubstitutableProperties		props = new SubstitutableProperties(Utils.mkProps(
												 NanoServiceFactory.NANOSERVICE_PORT, ""+parser.getValue(ARG_HELP_PORT,int.class)
												,NanoServiceFactory.NANOSERVICE_ROOT, "fsys:xmlReadOnly:root://chav1961.ksmgr.Application/chav1961/ksmgr/helptree.xml"
												,NanoServiceFactory.NANOSERVICE_CREOLE_PROLOGUE_URI, OldApplication.class.getResource("prolog.cre").toString() 
												,NanoServiceFactory.NANOSERVICE_CREOLE_EPILOGUE_URI, OldApplication.class.getResource("epilog.cre").toString() 
											));
		
		try(final InputStream				is = Application.class.getResourceAsStream("application.xml");
			final NanoServiceFactory		service = new NanoServiceFactory(PureLibSettings.CURRENT_LOGGER,props)) {
			final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
			final CountDownLatch			latch = new CountDownLatch(1);
			
			PureLibClient.registerInPureLib();
			PureLibSettings.instance().setProperty("helpReference","http://localhost:"+parser.getValue(ARG_HELP_PORT,int.class));
			final Application	application = new Application(xda,PureLibSettings.PURELIB_LOCALIZER,parser.getValue(ARG_HELP_PORT,int.class), parser.isTyped(ARG_LOCAL_CONFIG) ? parser.getValue(ARG_LOCAL_CONFIG,String.class) : ARG_LOCAL_CONFIG_DEFAULT, latch);
			
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

}
