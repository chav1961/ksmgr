package chav1961.ksmgr.keystore;

public enum KeyStoreState {
	MISSING(new String[]{"menu.file.newkeystore", "menu.file.openkeystore"}, new String[]{"menu.file.savekeystore", "menu.file.savekeystoreas", "menu.file.closekeystore"}),
	NEW(new String[]{"menu.file.newkeystore", "menu.file.openkeystore", "menu.file.savekeystoreas", "menu.file.closekeystore"}, new String[]{"menu.file.savekeystore"}),
	NEW_MODIFIED(new String[]{"menu.file.newkeystore", "menu.file.openkeystore", "menu.file.savekeystoreas", "menu.file.closekeystore"}, new String[]{"menu.file.savekeystore"}),
	OPEN(new String[]{"menu.file.newkeystore", "menu.file.openkeystore", "menu.file.savekeystore", "menu.file.savekeystoreas", "menu.file.closekeystore"}, new String[]{}),
	OPEN_MODIFIED(new String[]{"menu.file.newkeystore", "menu.file.openkeystore", "menu.file.savekeystore", "menu.file.savekeystoreas", "menu.file.closekeystore"}, new String[]{});
	
	private final String[] 	enabledItems;
	private final String[] 	disabledItems;
	
	private KeyStoreState(final String[] enabledItems, final String[] disabledItems) {
		this.enabledItems = enabledItems;
		this.disabledItems = disabledItems;
	}
	
	public String[] getEnabledItems() {
		return enabledItems;
	}
	
	public String[] getDisabledItems() {
		return disabledItems;
	}
}
