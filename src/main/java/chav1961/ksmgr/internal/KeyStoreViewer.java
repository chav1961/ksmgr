package chav1961.ksmgr.internal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import chav1961.ksmgr.Application;
import chav1961.ksmgr.dialogs.AskPasswordDialog;
import chav1961.ksmgr.internal.KeyStoreViewer.ItemDescriptor.ItemType;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.LocalizedFormatter;

public class KeyStoreViewer extends JTable {
	private static final long 			serialVersionUID = -7656534471231370440L;
	public static final int				PREFERRED_WIDTH = 300;
	
	public enum KeyStoreViewerSelectionType {
		NONE,
		EXACTLY_ONE_KEY,
		EXACTLY_ONE_CERTIFICATE,
		KEYS_ONLY,
		CERTIFICATES_ONLY,
		MIX
	}
	
	private static final String			KEY_CONFIRM_DELETION_TITLE = "chav1961.ksmgr.internal.KeyStoreViewer.confirmdeletion.title";
	private static final String			KEY_CONFIRM_DELETION_MESSAGE = "chav1961.ksmgr.internal.KeyStoreViewer.confirmdeletion.message";

	private final ContentNodeMetadata	meta;
	private final Localizer				localizer;
	private final LoggerFacade			logger;
	private final PasswordsRepo			repo;
	private final KeyStore				store;
	
	public KeyStoreViewer(final ContentNodeMetadata meta, final Localizer localizer, final LoggerFacade logger, final PasswordsRepo repo, final String fileName, final KeyStore store) {
		super(new KeyStoreModel(fileName,store,logger,repo));
		
		if (meta == null) {
			throw new NullPointerException("Node metadata can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (repo == null) {
			throw new NullPointerException("Passwords repo can't be null"); 
		}
		else if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("Key store file name can't be null"); 
		}
		else if (store == null) {
			throw new NullPointerException("Key store can't be null"); 
		}
		else {
			this.meta = meta;
			this.localizer = localizer;
			this.logger = logger;
			this.repo = repo;
			this.store = store;
			
			setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3) {
						final int			row = rowAtPoint(e.getPoint());
						
						getSelectionModel().setSelectionInterval(row,row);						
						KeyStoreViewer.this.requestFocusInWindow();
						
						SwingUtilities.invokeLater(()->{
							final JPopupMenu	pm = SwingUtils.toJComponent(meta.getOwner().byUIPath(URI.create("ui:/model/navigation.top.keystoreActions")), JPopupMenu.class);

							pm.show(KeyStoreViewer.this, e.getX(), e.getY());
						});
					}
				}
			});
			setDefaultRenderer(ItemDescriptor.class,new TableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
					final ItemDescriptor	val = (ItemDescriptor)value;
					
					try{final JLabel		label = new JLabel(val.alias,new ImageIcon(ImageIO.read(this.getClass().getResource(val.type.getIconName()))),JLabel.LEFT);
						
						label.setOpaque(true);
						if (isSelected) {
							label.setBackground(getSelectionBackground());
							label.setForeground(getSelectionForeground());
						}
						else {
							label.setBackground(getBackground());
							label.setForeground(getForeground());
						}
						if (hasFocus) {
							label.setBorder(new LineBorder(Color.BLACK));
						}
						if (repo.hasPasswordFor(val.alias)) {
							label.setForeground(Color.RED);
						}
						
						return label;
					} catch (IOException e) {
						return new JLabel(e.getLocalizedMessage());
					}
				}
			});
			setDefaultEditor(ItemDescriptor.class,new DefaultCellEditor(new JTextField()) {
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
			        delegate.setValue(((ItemDescriptor)value).alias);
			        return editorComponent;
				}
			});
			setRowHeight(26);
			getColumnModel().getColumn(0).setPreferredWidth(PREFERRED_WIDTH);
			
			SwingUtils.assignActionKey(this,SwingUtils.KS_DELETE, (e)->deleteItems(), "delete");
			SwingUtils.assignActionKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), (e)->callPopup(), "popup");
			refresh();
		}
	}
	
	@Override
	public String getToolTipText(final MouseEvent event) {
		final int	row = this.rowAtPoint(event.getPoint());
		
		if (row >= 0) {
			try{final ItemDescriptor	desc = (ItemDescriptor) getModel().getValueAt(row, 0);
				final Certificate		cert = store.getCertificate(desc.alias);

				if (cert != null) {
					if (cert instanceof X509Certificate) {
						final StringBuilder		sb = new StringBuilder();
						final X509Certificate	x509 = (X509Certificate)cert; 
						
						sb.append("type=").append(x509.getType()).append(", version=").append(x509.getVersion()).append("<br>");
						sb.append("serial#=").append(x509.getSerialNumber()).append("<br>");
						sb.append("Issuer principal: ").append(x509.getIssuerX500Principal().getName()).append("<br>");
						sb.append("Subject principal: ").append(x509.getSubjectX500Principal().getName()).append("<br>");
						sb.append("Date range from ").append(x509.getNotBefore()).append(" to ").append(x509.getNotAfter()).append("<br>");
						if (repo.hasPasswordFor(desc.alias)) {
							sb.append("<font color=red>Password keeps!</font><br>");
						}
						return "<html><body>"+sb+"</body></html>";
					}
					else {
						return null;
					}
				}
				else {
					return null;
				}
			} catch (KeyStoreException e) {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	public void refresh() {
		((KeyStoreModel)getModel()).refreshContent();
	}

	public KeyStoreViewerSelectionType getSelectionType() {
		final int[]	selection = getSelectedRows();
		
		if (selection == null || selection.length == 0) {
			return KeyStoreViewerSelectionType.NONE;
		}
		else if (selection.length == 1) {
			final ItemDescriptor	desc = (ItemDescriptor) getModel().getValueAt(selection[0],0);
			
			switch (desc.type) {
				case CERTIFICATE	:
					return KeyStoreViewerSelectionType.EXACTLY_ONE_CERTIFICATE;
				case KEY_PAIR		:
					return KeyStoreViewerSelectionType.EXACTLY_ONE_KEY;
				case UNKNOWN		:
					return KeyStoreViewerSelectionType.NONE;
				default	:
					throw new UnsupportedOperationException("Description type ["+desc.type+"] is not supported yet"); 
			}
		}
		else {
			final Set<ItemType>	types = new HashSet<>();
			
			for (int item : selection) {
				types.add(((ItemDescriptor) getModel().getValueAt(item,0)).type);
			}
			if (types.size() == 1) {
				if (types.contains(ItemType.CERTIFICATE)) {
					return KeyStoreViewerSelectionType.CERTIFICATES_ONLY;
				}
				else if (types.contains(ItemType.KEY_PAIR)) {
					return KeyStoreViewerSelectionType.KEYS_ONLY;
				}
				else {
					return KeyStoreViewerSelectionType.NONE;
				}
			}
			else {
				return KeyStoreViewerSelectionType.MIX;
			}
		}
	}
	
//	@OnAction("action:/delete")
	public void deleteItems() {
		final int[]	indices = getSelectionModel().getSelectedIndices();
		
		if (indices != null && indices.length > 0) {
			final StringBuilder	sb = new StringBuilder();
			
			for (int index = 0; index < indices.length; index++) {
				final ItemDescriptor	item = (ItemDescriptor) ((KeyStoreModel)getModel()).getValueAt(indices[index],0);
				
				sb.append("\n").append(item.alias);
			}
			
			try{if (new JLocalizedOptionPane(localizer).confirm(this, new LocalizedFormatter(KEY_CONFIRM_DELETION_MESSAGE,sb.substring(1)), KEY_CONFIRM_DELETION_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					ItemDescriptor	desc = null;
					
					for (int index : indices) {
						desc = (ItemDescriptor)getModel().getValueAt(index,0);
						store.deleteEntry(desc.alias);
						repo.deletePasswordFor(desc.alias);
					}
					((KeyStoreModel)getModel()).refreshContent();
					if (indices.length > 1) {
						logger.message(Severity.info,"%1$d entries were deleted",indices.length);
					}
					else {
						logger.message(Severity.info,"Alias [%1$s] was deleted",desc.alias);
					}
				}
			} catch (LocalizationException | KeyStoreException  e) {
				logger.message(Severity.error, e.getLocalizedMessage());
			}
		}
	}

//	@OnAction("action:/rename")
	public void rename() {
		editCellAt(getSelectionModel().getMinSelectionIndex(),0);
	}

	private void callPopup() {
		final int				row = getSelectionModel().getMinSelectionIndex();
		
		if (row >= 0) {
			final JPopupMenu	pm = SwingUtils.toJComponent(meta.getOwner().byUIPath(URI.create("ui:/model/navigation.top.keystoreActions")), JPopupMenu.class);
			final Rectangle		rect = getCellRect(row, 0, false);
			
			getSelectionModel().setSelectionInterval(row,row);
			SwingUtils.assignActionListeners(pm,KeyStoreViewer.this);
			pm.show(KeyStoreViewer.this, rect.x+rect.width/2, rect.y+rect.height/2);
		}
	}

	public static class KeyStoreModel extends DefaultTableModel {
		private static final long serialVersionUID = -3488066941423522586L;
		
		private final String				keyStoreName;
		private final KeyStore				ks;
		private final LoggerFacade			logger;
		private final PasswordsRepo			repo;
		private final List<ItemDescriptor>	list = new ArrayList<>();

		public KeyStoreModel(final String keyStoreName, final KeyStore ks, final LoggerFacade logger, final PasswordsRepo repo) {
			if (keyStoreName == null) {
				throw new NullPointerException("Key store file name can't be null");
			}
			else if (ks == null) {
				throw new NullPointerException("Key store can't be null");
			}
			else if (logger == null) {
				throw new NullPointerException("Logger facade can't be null");
			}
			else if (repo == null) {
				throw new NullPointerException("Passwords repo can't be null");
			}
			else {
				this.keyStoreName = keyStoreName;
				this.ks = ks;
				this.logger = logger;
				this.repo = repo;
			}
		}
		
		@Override
		public int getRowCount() {
			if (ks == null) {
				return 0;
			}
			else {
				return list.size();
			}
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			return keyStoreName;
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return ItemDescriptor.class;
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return true;
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			return list.get(rowIndex);
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			renameItem((ItemDescriptor)getValueAt(rowIndex, columnIndex), aValue.toString());
		}
		
		public void refreshContent() {
			try{list.clear();
				
				final Enumeration<String>	aliases = ks.aliases();
			
				while (aliases.hasMoreElements()) {
					final String	item = aliases.nextElement();
					
					if (ks.isCertificateEntry(item)) {
						list.add(new ItemDescriptor(item,ItemDescriptor.ItemType.CERTIFICATE, null));
					}
					else if (ks.isKeyEntry(item)) {
						list.add(new ItemDescriptor(item,ItemDescriptor.ItemType.KEY_PAIR, null));
					}
				}
				fireTableDataChanged();
			} catch (KeyStoreException e) {
				logger.message(Severity.error,"Error refreshing key store content: "+e.getLocalizedMessage());
			}
		}

		private void renameItem(final ItemDescriptor desc, final String newAlias) {
			try{if (ks.containsAlias(newAlias)) {
					logger.message(Severity.error,"Alias ["+newAlias+"] already exists in the key store");
				}
				else if (ks.isKeyEntry(desc.alias)) {
					final AskPasswordDialog	apd = new AskPasswordDialog(logger);
					
					if (Application.askPassword(apd,desc.alias)) {
						final Key 			key = ks.getKey(desc.alias, apd.password);
						final Certificate[]	chain = ks.getCertificateChain(desc.alias);

						ks.setKeyEntry(newAlias, key, apd.password, chain);
						ks.deleteEntry(desc.alias);
						repo.deletePasswordFor(desc.alias);
						repo.storePasswordFor(newAlias, apd.password);
						refreshContent();
					}
				}
				else if (ks.isCertificateEntry(desc.alias)) {
					final Certificate	cert = ks.getCertificate(desc.alias);
					
					ks.setCertificateEntry(newAlias,cert);
					ks.deleteEntry(desc.alias);
					refreshContent();
				}
			} catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
				logger.message(Severity.error,"Error renaming alias in the key store: "+e.getLocalizedMessage());
			}
		}

		public void insertItem(final ItemDescriptor item) {
			// TODO Auto-generated method stub
			refreshContent();
		}
		
		public void deleteItem(final ItemDescriptor item) {
			// TODO Auto-generated method stub
			refreshContent();
		}
	}
	
	public static class ItemDescriptor {
		public enum ItemType {
			CERTIFICATE("certificate.png"),
			KEY_PAIR("key.png"),
			UNKNOWN("unknown.png");
			
			private final String iconName; 
			
			ItemType(final String iconName) {
				this.iconName = iconName;
			}
			
			public String getIconName() {
				return iconName;
			}
		}
		
		public String	alias;
		public ItemType	type;
		public char[]	passwordAssociated;
		
		public ItemDescriptor(String alias, ItemType type, char[] passwordAssociated) {
			this.alias = alias;
			this.type = type;
			this.passwordAssociated = passwordAssociated;
		}

		@Override
		public String toString() {
			return "ItemDescriptor [alias=" + alias + ", type=" + type + ", passwordAssociated=" + Arrays.toString(passwordAssociated) + "]";
		}
	}
}
