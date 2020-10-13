package chav1961.ksmgr.internal;

import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;

public class KeyStoreViewer extends JList<String> {
	private static final long serialVersionUID = -7656534471231370440L;

	private final KeyStore	store;
	
	public KeyStoreViewer(final KeyStore store) {
		super(new DefaultListModel<String>());
		
		if (store == null) {
			throw new NullPointerException("Key store can't be null"); 
		}
		else {
			this.store = store;
			
			setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);			
			refreshContent();
		}
	}
	
	public void refresh() {
		refreshContent();
	}
	
	private void refreshContent() {
		try{final Enumeration<String>	aliases = store.aliases();
		
			removeAll();
			while (aliases.hasMoreElements()) {
				final String	item = aliases.nextElement();
				
				try{final Entry 	entry = store.getEntry(item,null);
				
					((DefaultListModel<String>)getModel()).addElement(item);
				} catch (UnrecoverableEntryException exc) {
					((DefaultListModel<String>)getModel()).addElement(item+"*****");
				}
			}
		} catch (KeyStoreException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
