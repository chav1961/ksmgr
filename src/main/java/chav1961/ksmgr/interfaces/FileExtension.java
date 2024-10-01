package chav1961.ksmgr.interfaces;

import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.interfaces.FileExtension/chav1961/ksmgr/i18n/i18n.xml")
// https://datatracker.ietf.org/doc/html/rfc7468
public enum FileExtension implements ModuleAccessor {
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileextension.pem",tooltip="chav1961.ksmgr.interfaces.fileextension.pem.tt")
	PEM(FileFormat.PEM, "pem"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileextension.crt",tooltip="chav1961.ksmgr.interfaces.fileextension.crt.tt")
	CRT(FileFormat.PEM, "crt"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileextension.cer",tooltip="chav1961.ksmgr.interfaces.fileextension.cer.tt")
	CER(FileFormat.PEM, "cer"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileextension.key",tooltip="chav1961.ksmgr.interfaces.fileextension.key.tt")
	KEY(FileFormat.PEM, "key"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileextension.p7b",tooltip="chav1961.ksmgr.interfaces.fileextension.p7b.tt")
	P7B(FileFormat.PKCS_7, "p7b"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileextension.p7c",tooltip="chav1961.ksmgr.interfaces.fileextension.p7c.tt")
	P7C(FileFormat.PKCS_7, "p7c"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileextension.der",tooltip="chav1961.ksmgr.interfaces.fileextension.der.tt")
	DER(FileFormat.DER, "der"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileextension.cerd",tooltip="chav1961.ksmgr.interfaces.fileextension.cerd.tt")
	CERD(FileFormat.DER, "cer"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileextension.pfx",tooltip="chav1961.ksmgr.interfaces.fileextension.pfx.tt")
	PFX(FileFormat.PKCS_12, "pfx"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileextension.p12",tooltip="chav1961.ksmgr.interfaces.fileextension.p12.tt")
	P12(FileFormat.PKCS_12, "p12");
	
	private final FileFormat	format;
	private final String		extension;
	
	private FileExtension(final FileFormat format, final String extension) {
		this.format = format;
		this.extension = extension;
	}
	
	public FileFormat getFileFormat() {
		return format;
	}
	
	public String getFileExtension() {
		return extension;
	}
	
	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}
}
