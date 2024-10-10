package chav1961.ksmgr.gui;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKey;

import chav1961.ksmgr.interfaces.FileExtension;
import chav1961.ksmgr.interfaces.FileFormat;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.gui.AskExportSecureKey/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.gui.askexportsecurekey",tooltip="chav1961.ksmgr.gui.askexportsecurekey.tt",help="chav1961.ksmgr.gui.askexportsecurekey.help")
public class AskExportSecureKey implements FormManager<Object, AskExportSecureKey>,  ModuleAccessor {
	private static final byte[]		BEGIN_MARKER = "".getBytes();
	private static final byte[]		END_MARKER = "".getBytes();
	
	private final LoggerFacade 		facade;

	@LocaleResource(value="chav1961.ksmgr.gui.askexportsecurekey.format",tooltip="chav1961.ksmgr.gui.askexportsecurekey.format.tt")
	@Format("30sm")
	public FileFormat		fileFormat;

	@LocaleResource(value="chav1961.ksmgr.gui.askexportsecurekey.extension",tooltip="chav1961.ksmgr.gui.askexportsecurekey.extension.tt")
	@Format("30sm")
	public FileExtension	fileExtansion;
	
	@LocaleResource(value="chav1961.ksmgr.gui.askexportsecurekey.password",tooltip="chav1961.ksmgr.gui.askexportsecurekey.password.tt")
	@Format("30s")
	public char[]			password = null;

	@LocaleResource(value="chav1961.ksmgr.gui.askexportsecurekey.passwordretype",tooltip="chav1961.ksmgr.gui.askexportsecurekey.passwordretype.tt")
	@Format("30s")
	public char[]			passwordRetype = null;

	
	public AskExportSecureKey(final LoggerFacade facade) {
		this.facade = facade;
	}
	
	@Override
	public RefreshMode onField(final AskExportSecureKey inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		switch (fieldName) {
			case "password" : case "passwordRetype" :
				if (!Arrays.equals(password, passwordRetype)) {
					if (beforeCommit) {
						getLogger().message(Severity.error,"Password and retype password differ!");
						return RefreshMode.REJECT;
					}
					else {
						getLogger().message(Severity.warning,"Password and retype password differ!");
					}
				}
				else {
					getLogger().message(Severity.info,"Passwords are identical");
				}
				return RefreshMode.DEFAULT;
			default :
				return RefreshMode.DEFAULT;
		}
	}

	@Override
	public LoggerFacade getLogger() {
		return facade;
	}

	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
		FileFormat.PEM.allowUnnamedModuleAccess(unnamedModules);
		FileExtension.PEM.allowUnnamedModuleAccess(unnamedModules);
	}

	public void exportKey(final SecretKey key, final OutputStream os) throws IOException {
		switch (fileFormat) {
			case DER		:
				break;
			case PEM		:
				os.write(BEGIN_MARKER);
				os.write(Base64.getEncoder().encode(key.getEncoded()));
				os.write(END_MARKER);
				os.flush();
				break;
			case PKCS_12	:
				break;
			case PKCS_7		:
				break;
			default:
				throw new UnsupportedOperationException("File format ["+fileFormat+"] is not supported yet");
		}
	}
}

