package chav1961.ksmgr.utils;

import java.util.HashMap;
import java.util.Map;

public class PasswordsRepo {
	public static final String		KEY_STORE_PREFIX = "KeyStore";
	public static final String		KEY_STORE_ITEM_PREFIX = "KeyStoreItem";
	public static final String		SECRET_KEY_PREFIX = "SecretKey";
	
	private final Map<String,char[]>	passwords = new HashMap<>();
	private boolean						keepPasswords; 
	
	public PasswordsRepo(final boolean keepPasswords) {
		this.keepPasswords = keepPasswords;
	}
	
	public boolean isKeepedPasswords() {
		return keepPasswords;
	}

	public void setKeepedPasswords(final boolean keepPasswords) {
		this.keepPasswords = keepPasswords;
		passwords.clear();
	}
	
	public boolean hasPasswordFor(final String item) {
		if (item == null || item.isEmpty()) {
			throw new IllegalArgumentException("Item to test password for can't be null or empty"); 
		}
		else if (isKeepedPasswords()) {
			return passwords.containsKey(item.toLowerCase());
		}
		else {
			return false;
		}
	}
	
	public char[] getPasswordFor(final String item) {
		if (item == null || item.isEmpty()) {
			throw new IllegalArgumentException("Item to test password for can't be null or empty"); 
		}
		else if (isKeepedPasswords()) {
			return passwords.get(item.toLowerCase());
		}
		else {
			return null;
		}
	}
	
	public void storePasswordFor(final String item, final char[] password) {
		if (item == null || item.isEmpty()) {
			throw new IllegalArgumentException("Item to store password for can't be null or empty"); 
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password to store can't be null or empty"); 
		}
		else if (isKeepedPasswords()) {
			passwords.put(item.toLowerCase(), password);
		}
	}

	public void deletePasswordFor(final String item) {
		if (item == null || item.isEmpty()) {
			throw new IllegalArgumentException("Item to remove password for can't be null or empty"); 
		}
		else if (isKeepedPasswords()) {
			passwords.remove(item.toLowerCase());
		}
	}
}
