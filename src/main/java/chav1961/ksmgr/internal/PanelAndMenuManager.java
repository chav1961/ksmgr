package chav1961.ksmgr.internal;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import chav1961.ksmgr.Application;
import chav1961.ksmgr.internal.KeyStoreViewer.KeyStoreViewerSelectionType;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.JFileList;
import chav1961.purelib.ui.swing.useful.JFileList.JFileListSelectionType;

public class PanelAndMenuManager {
	private final JMenuBar				menu;
	private final RightPanelContainer	rightContainer;
	private final JSplitPane			splitter;
	private final FocusListener			leftFocusListener = new FocusListener() {
											@Override public void focusLost(FocusEvent e) {}
											@Override public void focusGained(FocusEvent e) {focusLeftPanel();}
										};
	private final FocusListener			rightFocusListener = new FocusListener() {
											@Override public void focusLost(FocusEvent e) {}
											@Override public void focusGained(FocusEvent e) {focusRightPanel();}
										};
	private final ListSelectionListener	leftSelectionListener = new ListSelectionListener() {
											@Override public void valueChanged(ListSelectionEvent e) {refreshMenuState();}
										};
	private final ListSelectionListener	rightSelectionListener = new ListSelectionListener() {
											@Override public void valueChanged(ListSelectionEvent e) {refreshMenuState();}
										};
	
	private KeyStoreViewer				leftComponent = null;
	private boolean						leftFocused = false; 
	private JComponent					rightComponent = null;
	private boolean						rightFocused = false; 
									
	public PanelAndMenuManager(final Application parent, final JMenuBar menu, final Localizer localizer, final LoggerFacade logger, final FileSystemInterface fsi, final JSplitPane splitter) {
		this.splitter = splitter;
		this.rightContainer = new RightPanelContainer(parent, localizer, logger, fsi, splitter);
		this.menu = menu;
		refreshMenuState();
	}

	public void setLeftComponent(final KeyStoreViewer component) {
		if (leftComponent != null) {
			leftComponent.removeFocusListener(leftFocusListener);
			leftComponent.getSelectionModel().removeListSelectionListener(leftSelectionListener);
		}
		leftComponent = component;
		leftComponent.addFocusListener(leftFocusListener);
		leftComponent.getSelectionModel().addListSelectionListener(leftSelectionListener);
		splitter.setLeftComponent(new JScrollPane(leftComponent));
		refreshMenuState();
	}

	public KeyStoreViewer getLeftComponent() {
		return leftComponent;
	}

	public boolean isLeftPanelFocused() {
		return leftFocused;
	}
	
	public void setRightComponent(final KeyStoreViewer component) {
		if (rightComponent != null) {
			rightComponent.removeFocusListener(rightFocusListener);
			if (rightComponent instanceof KeyStoreViewer) {
				((KeyStoreViewer)rightComponent).getSelectionModel().removeListSelectionListener(rightSelectionListener);
			}
			else if (rightComponent instanceof JFileList) {
				((JFileList)rightComponent).getSelectionModel().removeListSelectionListener(rightSelectionListener);
			}
		}
		rightComponent = component;
		rightComponent.addFocusListener(rightFocusListener);
		component.getSelectionModel().addListSelectionListener(rightSelectionListener);
//		getRightContainer().setPanelTypeAsKeystore(component);
		splitter.setRightComponent(new JScrollPane(component));
		((JRadioButtonMenuItem)SwingUtils.findComponentByName(menu, "menu.settings.view.keystore")).setSelected(true);
		refreshMenuState();
	}

	public void setRightComponent(final JFileList component) {
		if (rightComponent != null) {
			rightComponent.removeFocusListener(rightFocusListener);
		}
		rightComponent = component;
		rightComponent.addFocusListener(rightFocusListener);
		component.getSelectionModel().addListSelectionListener(rightSelectionListener);
		getRightContainer().setPanelTypeAsFileSystem(component);
		splitter.setRightComponent(new JScrollPane(component));
		((JRadioButtonMenuItem)SwingUtils.findComponentByName(menu, "menu.settings.view.filesystem")).setSelected(true);
		refreshMenuState();
	}

	public <T extends JComponent> T getRightComponent() {
		return (T) rightComponent;
	}

	public RightPanelContainer getRightContainer() {
		return rightContainer;
	}
	
	public boolean isRightPanelFocused() {
		return rightFocused;
	}

	public void refreshRightComponent() {
		rightContainer.refresh();
	}
	
	private void focusLeftPanel() {
		leftFocused = true;
		rightFocused = false;
		((JComponent)splitter.getLeftComponent()).setBorder(new LineBorder(Color.BLUE));
		((JComponent)splitter.getRightComponent()).setBorder(new EmptyBorder(0,0,0,0));
		refreshMenuState();
	}

	private void focusRightPanel() {
		leftFocused = false;
		rightFocused = true;
		((JComponent)splitter.getLeftComponent()).setBorder(new EmptyBorder(0,0,0,0));
		((JComponent)splitter.getRightComponent()).setBorder(new LineBorder(Color.BLUE));
		refreshMenuState();
	}
	
	public void refreshMenuState() {
		refreshCommonMenuState();
		if (isLeftPanelFocused()) {
			refreshLeftFocusedMenuState();
		}
		else if (isRightPanelFocused()) {
			refreshRightFocusedMenuState();
		}
	}	

	private void refreshCommonMenuState() {
		final JMenu			edit = (JMenu)SwingUtils.findComponentByName(menu, "menu.edit");
		final JMenu			tasks = (JMenu)SwingUtils.findComponentByName(menu, "menu.tasks");
		
		if (leftComponent != null) {
			edit.setEnabled(true);
			tasks.setEnabled(true);
		}
		else {
			edit.setEnabled(false);
			tasks.setEnabled(false);
		}
	}

	private void refreshLeftFocusedMenuState() {
		final JMenu			crypto = (JMenu)SwingUtils.findComponentByName(menu, "menu.tasks.crypto");
		
		final JMenuItem		editCopy = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.edit.copy");
		final JMenuItem		editMove = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.edit.move");
		final JMenuItem		editRename = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.edit.rename");
		final JMenuItem		editDelete = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.edit.delete");
		
		final JMenuItem		kpGenerate = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keypairs.generate");
		final JMenuItem		kpExport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keypairs.export");
		final JMenuItem		kpImport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keypairs.import");
		final JMenuItem		kpGenerateAndExport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keypairs.generateandexport");
		
		final JMenuItem		certificatesExport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.certificates.export");
		final JMenuItem		certificatesLoadTrusted = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.certificates.loadtrusted");
		final JMenuItem		certificatesRequest = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.certificates.preparerequest");
		final JMenuItem		certificatesSignRequest = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.certificates.signrequest");
		final JMenuItem		certificatesSelfSigned = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.certificates.selfsigned");
		
		final JMenuItem		keyGenerate = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keys.generate");
		final JMenuItem		keyExport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keys.export");
		final JMenuItem		keyImport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keys.import");
		
		final KeyStoreViewerSelectionType selType = leftComponent == null ? KeyStoreViewerSelectionType.NONE : leftComponent.getSelectionType();
		
		switch (rightContainer.getPanelType()) {
			case AS_FILESYSTEM	:
				final JFileListSelectionType	rightSelType = ((JFileList)getRightComponent()).getSelectionType();
				
				editCopy.setEnabled(false);
				editMove.setEnabled(false);
				editRename.setEnabled(Set.of(KeyStoreViewerSelectionType.EXACTLY_ONE_KEY_PAIR,KeyStoreViewerSelectionType.EXACTLY_ONE_SECRET_KEY,KeyStoreViewerSelectionType.EXACTLY_ONE_CERTIFICATE).contains(selType));
				editDelete.setEnabled(selType != KeyStoreViewerSelectionType.NONE);
		
				kpGenerate.setEnabled(true);
				kpExport.setEnabled(Set.of(KeyStoreViewerSelectionType.EXACTLY_ONE_KEY_PAIR,KeyStoreViewerSelectionType.KEY_PAIRS_ONLY).contains(selType));
				kpImport.setEnabled(true);
				kpGenerateAndExport.setEnabled(true);
				
				certificatesExport.setEnabled(selType != KeyStoreViewerSelectionType.NONE);
				certificatesLoadTrusted.setEnabled(true);
				certificatesRequest.setEnabled(Set.of(KeyStoreViewerSelectionType.EXACTLY_ONE_KEY_PAIR).contains(selType));
				certificatesSignRequest.setEnabled(Set.of(KeyStoreViewerSelectionType.EXACTLY_ONE_KEY_PAIR).contains(selType));
				certificatesSelfSigned.setEnabled(true);
				
				keyGenerate.setEnabled(true);
				keyExport.setEnabled(Set.of(KeyStoreViewerSelectionType.EXACTLY_ONE_SECRET_KEY).contains(selType));
				keyImport.setEnabled(true);
				
				crypto.setEnabled(Set.of(JFileListSelectionType.EXACTLY_ONE_FILE,JFileListSelectionType.FILES_ONLY).contains(rightSelType) && Set.of(KeyStoreViewerSelectionType.EXACTLY_ONE_KEY_PAIR,KeyStoreViewerSelectionType.EXACTLY_ONE_SECRET_KEY,KeyStoreViewerSelectionType.EXACTLY_ONE_CERTIFICATE).contains(selType));
				break;
			case AS_KEYSTORE	:
				editCopy.setEnabled(true);
				editMove.setEnabled(true);
				editRename.setEnabled(Set.of(KeyStoreViewerSelectionType.EXACTLY_ONE_KEY_PAIR,KeyStoreViewerSelectionType.EXACTLY_ONE_SECRET_KEY,KeyStoreViewerSelectionType.EXACTLY_ONE_CERTIFICATE).contains(selType));
				editDelete.setEnabled(selType != KeyStoreViewerSelectionType.NONE);
				
				kpGenerate.setEnabled(false);
				kpExport.setEnabled(false);
				kpImport.setEnabled(false);
				kpGenerateAndExport.setEnabled(false);
				
				certificatesExport.setEnabled(false);
				certificatesLoadTrusted.setEnabled(false);
				certificatesRequest.setEnabled(false);
				certificatesSignRequest.setEnabled(false);
				certificatesSelfSigned.setEnabled(true);
				
				keyGenerate.setEnabled(true);
				keyExport.setEnabled(false);
				keyImport.setEnabled(false);
				
				crypto.setEnabled(false);
				break;
			default	:
				throw new UnsupportedOperationException("Right panel type ["+rightContainer.getPanelType()+"] is not supported yet");
		}
	}
	
	private void refreshRightFocusedMenuState() {
		final JMenu			crypto = (JMenu)SwingUtils.findComponentByName(menu, "menu.tasks.crypto");
		
		final JMenuItem		editCopy = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.edit.copy");
		final JMenuItem		editMove = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.edit.move");
		final JMenuItem		editRename = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.edit.rename");
		final JMenuItem		editDelete = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.edit.delete");
		
		final JMenuItem		kpGenerate = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keypairs.generate");
		final JMenuItem		kpExport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keypairs.export");
		final JMenuItem		kpImport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keypairs.import");
		final JMenuItem		kpGenerateAndExport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keypairs.generateandexport");
		
		final JMenuItem		certificatesExport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.certificates.export");
		final JMenuItem		certificatesLoadTrusted = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.certificates.loadtrusted");
		final JMenuItem		certificatesRequest = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.certificates.preparerequest");
		final JMenuItem		certificatesSignRequest = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.certificates.signrequest");
		final JMenuItem		certificatesSelfSigned = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.certificates.selfsigned");
		
		final JMenuItem		keyGenerate = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keys.generate");
		final JMenuItem		keyExport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keys.export");
		final JMenuItem		keyImport = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.tasks.keys.import");
		
		final KeyStoreViewerSelectionType leftSelType = leftComponent == null ? KeyStoreViewerSelectionType.NONE : leftComponent.getSelectionType();
		
		switch (rightContainer.getPanelType()) {
			case AS_FILESYSTEM	:
				final JFileListSelectionType	rightSelType = ((JFileList)getRightComponent()).getSelectionType();
				
				editCopy.setEnabled(false);
				editMove.setEnabled(false);
				editRename.setEnabled(Set.of(JFileListSelectionType.EXACTLY_ONE_FILE,JFileListSelectionType.EXACTLY_ONE_DIRECTORY).contains(rightSelType));
				editDelete.setEnabled(rightSelType != JFileListSelectionType.NONE);
				
				kpGenerate.setEnabled(false);
				kpExport.setEnabled(false);
				kpImport.setEnabled(Set.of(JFileListSelectionType.EXACTLY_ONE_FILE,JFileListSelectionType.FILES_ONLY).contains(rightSelType));
				kpGenerateAndExport.setEnabled(false);
				
				certificatesExport.setEnabled(false);
				certificatesLoadTrusted.setEnabled(Set.of(JFileListSelectionType.EXACTLY_ONE_FILE,JFileListSelectionType.FILES_ONLY).contains(rightSelType));
				certificatesRequest.setEnabled(false);
				certificatesSignRequest.setEnabled(false);
				certificatesSelfSigned.setEnabled(false);
				
				keyGenerate.setEnabled(false);
				keyExport.setEnabled(Set.of(KeyStoreViewerSelectionType.EXACTLY_ONE_SECRET_KEY).contains(leftSelType));
				keyImport.setEnabled(Set.of(JFileListSelectionType.EXACTLY_ONE_FILE).contains(rightSelType));
				
				crypto.setEnabled(Set.of(JFileListSelectionType.EXACTLY_ONE_FILE,JFileListSelectionType.FILES_ONLY).contains(rightSelType) && Set.of(KeyStoreViewerSelectionType.EXACTLY_ONE_KEY_PAIR,KeyStoreViewerSelectionType.EXACTLY_ONE_SECRET_KEY,KeyStoreViewerSelectionType.EXACTLY_ONE_CERTIFICATE).contains(leftSelType));
				break;
			case AS_KEYSTORE	:
				final KeyStoreViewerSelectionType	selType = ((KeyStoreViewer)rightComponent).getSelectionType();
				
				editCopy.setEnabled(true);
				editMove.setEnabled(true);
				editRename.setEnabled(Set.of(KeyStoreViewerSelectionType.EXACTLY_ONE_KEY_PAIR,KeyStoreViewerSelectionType.EXACTLY_ONE_CERTIFICATE).contains(selType));
				editDelete.setEnabled(selType != KeyStoreViewerSelectionType.NONE);
				
				kpGenerate.setEnabled(true);
				kpExport.setEnabled(false);
				kpImport.setEnabled(false);
				kpGenerateAndExport.setEnabled(false);
				
				certificatesExport.setEnabled(false);
				certificatesLoadTrusted.setEnabled(false);
				certificatesRequest.setEnabled(false);
				certificatesSignRequest.setEnabled(false);
				certificatesSelfSigned.setEnabled(true);
				
				keyGenerate.setEnabled(true);
				keyImport.setEnabled(false);
				
				crypto.setEnabled(false);
				break;
			default	:
				throw new UnsupportedOperationException("Right panel type ["+rightContainer.getPanelType()+"] is not supported yet");
		}
	}
}
