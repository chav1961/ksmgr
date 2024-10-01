package chav1961.ksmgr.interfaces;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.interfaces.KeyStoreEntityType/chav1961/ksmgr/i18n/i18n.xml")
public enum KeyStoreEntityType {
	@LocaleResource(value="chav1961.ksmgr.interfaces.keystoreentitytype.secretkey",tooltip="chav1961.ksmgr.interfaces.keystoreentitytype.secretkey.tt")
	SECRET_KEY("key.png"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.keystoreentitytype.keypair",tooltip="chav1961.ksmgr.interfaces.keystoreentitytype.keypair.tt")
	KEY_PAIR("keypair.png"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.keystoreentitytype.certificate",tooltip="chav1961.ksmgr.interfaces.keystoreentitytype.certificate.tt")
	CERTIFICATE("certificate.png"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.keystoreentitytype.unknown",tooltip="chav1961.ksmgr.interfaces.keystoreentitytype.unknown.tt")
	UNKNOWN("unknown.png");
	
	private final String	iconResourceName;
	private final Icon		icon;

	private KeyStoreEntityType(final String icon) {
		this.iconResourceName = icon;
		this.icon = new ImageIcon(getClass().getResource(icon));
	}
	
	public String getIconResourceName() {
		return iconResourceName;
	}
	
	public Icon getIcon() {
		return icon;
	}
	
}
