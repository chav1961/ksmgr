package chav1961.ksmgr.interfaces;

import chav1961.ksmgr.internal.KeyStoreWrapper;
import chav1961.purelib.basic.Utils;

public interface AliasDescriptor {
	SelectedWindows getSelectedWindow();
	KeyStoreWrapper getKeyStoreWrapper();
	String getAlias();
	KeyStoreEntityType getEntityType();
	
	public static AliasDescriptor of(final SelectedWindows window, final KeyStoreWrapper wrapper, final String alias, final KeyStoreEntityType entityType) {
		if (window == null) {
			throw new NullPointerException("Window selected can't be null");
		}
		else if (wrapper == null) {
			throw new NullPointerException("Key store wrapper can't be null");
		}
		else if (Utils.checkEmptyOrNullString(alias)) {
			throw new IllegalArgumentException("Alias can't be null or empty");
		}
		else if (entityType == null) {
			throw new NullPointerException("Entity type can't be null");
		}
		else {
			return new AliasDescriptor() {
				@Override
				public SelectedWindows getSelectedWindow() {
					return window;
				}
				
				@Override
				public KeyStoreWrapper getKeyStoreWrapper() {
					return wrapper;
				}
				
				@Override
				public KeyStoreEntityType getEntityType() {
					return entityType;
				}
				
				@Override
				public String getAlias() {
					return alias;
				}
			};
		}
	}
}
