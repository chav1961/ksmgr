package chav1961.ksmgr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.LRUPersistence;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JFileItemDescriptor;
import chav1961.purelib.ui.swing.useful.JFileTree;
import chav1961.purelib.ui.swing.useful.JStateString;
 
public class Application extends JFrame implements LocaleChangeListener, LoggerFacadeOwner {
	private static final long serialVersionUID = -1812854125768597438L;
	public static final String				ARG_LOCAL_CONFIG = "localConfig";
	public static final String				ARG_LOCAL_CONFIG_DEFAULT = ".ksmgr.config";
	public static final String				TITLE_APPLICATION = "application.title";
	public static final String				HELP_ABOUT_APPLICATION = "application.help";
	public static final String				TITLE_HELP_ABOUT_APPLICATION = "application.help.title";
	public static final String				LEFT_PLACEHOLDER_APPLICATION = "application.placeholder.left";

	public static final String				KEYSTORE_ITEM_ID = "<keystore>";
	
	
	private final ContentMetadataInterface 	app;
	private final SubstitutableProperties	props;
	private final Localizer					localizer;
	private final JMenuBar					menu;
	private final JStateString				state;
	private final JSplitPane				leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private final JSplitPane				totalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final FileSystemInterface		root = FileSystemInterface.Factory.newInstance(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/"));
	private final LRUPersistence			pers;
	private final JFileContentManipulator	fcm;
	
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
			this.localizer = LocalizerFactory.getLocalizer(app.getRoot().getLocalizerAssociated());

			parent.push(localizer);
			localizer.addLocaleChangeListener(this);
			this.pers = LRUPersistence.of(props, "LRU.item"); 
			this.fcm = new JFileContentManipulator(root, localizer, ()->null, ()->null, this.pers);
			
			this.state = new JStateString(this.localizer, 10);
			
			this.menu = SwingUtils.toJComponent(app.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class);
			SwingUtils.assignActionListeners(menu, this);
			SwingUtils.assignExitMethod4MainWindow(this, ()->exitApplication());

			this.leftSplit.setLeftComponent(new JScrollPane(new JLabel("Left top")));
			this.leftSplit.setRightComponent(new JScrollPane(new JLabel("Left bottom")));
			totalSplit.setLeftComponent(leftSplit);
			totalSplit.setRightComponent(new JScrollPane(new JFileTree(this.state, this.root, true) {
				private static final long serialVersionUID = 1L;

				@Override
				public void placeFileContent(final Point location, final Iterable<JFileItemDescriptor> content) {
				}

				@Override
				public void refreshLinkedContent(final FileSystemInterface content) {
				}
			}));			
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

	@OnAction("action:/newKeyStore")
	private void newKeystore() {
	}
	
	
	@OnAction("action:/closeKeystore")
	private void closeKeyStore () {
	}
	
	@OnAction("action:/exit")
	private void exitApplication () {
		setVisible(false);
		dispose();
	}

	@OnAction("action:/builtin.languages")
	private void selectLang(final Hashtable<String,String[]> langs) {
		final SupportedLanguages	newLang = SupportedLanguages.valueOf(langs.get("lang")[0]);
		final Locale				newLocale = newLang.getLocale();
		
		PureLibSettings.PURELIB_LOCALIZER.setCurrentLocale(newLocale);
	}
	
	@OnAction("action:/helpAbout")
	private void showAboutScreen() {
		SwingUtils.showAboutScreen(this, localizer, TITLE_HELP_ABOUT_APPLICATION, HELP_ABOUT_APPLICATION, URI.create("root://chav1961.ksmgr.Application/chav1961/ksmgr/avatar.jpg"), new Dimension(300,300));
	}

	private void fillLocalizedStrings() throws LocalizationException, IllegalArgumentException {
		setTitle(localizer.getValue(TITLE_APPLICATION));
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
}
