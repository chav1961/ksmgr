package chav1961.ksmgr.internal;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import chav1961.bt.security.keystore.KeyStoreController;
import chav1961.ksmgr.PasswordsRepo;


public class KeyStoreTree extends JTree {
	private static final long serialVersionUID = -4722748512411614408L;

	public KeyStoreTree(final KeyStoreController controller, final PasswordsRepo pwdRepo) {
		super(buildTreeModel(controller));
	}

	static DefaultMutableTreeNode buildTreeModel(final KeyStoreController controller) {
		// TODO Auto-generated method stub
		return null;
	}
}
