package chav1961.ksmgr.dialogs;

import java.math.BigInteger;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;

import chav1961.ksmgr.interfaces.SecurityAlgorithm;
import chav1961.ksmgr.interfaces.SignatureAlgorithm;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.dialogs.SignCertificateDialog/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.dialogs.signcertificatedialog",tooltip="chav1961.ksmgr.dialogs.signcertificatedialog.tt",help="help.aboutApplication")
public class SignCertificateDialog implements FormManager<Object, SignCertificateDialog> {
	private final LoggerFacade 	facade;
	private final KeyStore 		ks;

	@LocaleResource(value="chav1961.ksmgr.dialogs.signcertificatedialog.algorithm",tooltip="chav1961.ksmgr.dialogs.signcertificatedialog.algorithm.tt")
	@Format("30ms")
	public SignatureAlgorithm	algorithm = SignatureAlgorithm.SHA1withRSA;

	@LocaleResource(value="chav1961.ksmgr.dialogs.signcertificatedialog.serialnumber",tooltip="chav1961.ksmgr.dialogs.signcertificatedialog.serialnumber.tt")
	@Format("30ms")
	public BigInteger			serialNumber = new BigInteger(32, new SecureRandom());
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.signcertificatedialog.datefrom",tooltip="chav1961.ksmgr.dialogs.signcertificatedialog.datefrom.tt")
	@Format("(YYYY-MM-dd)ms")
	public Date					dateFrom;

	@LocaleResource(value="chav1961.ksmgr.dialogs.signcertificatedialog.dateto",tooltip="chav1961.ksmgr.dialogs.signcertificatedialog.dateto.tt")
	@Format("(YYYY-MM-dd)ms")
	public Date					dateTo;

	public SignCertificateDialog(final LoggerFacade facade, final KeyStore ks) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else if (ks == null) {
			throw new NullPointerException("Keystore can't be null"); 
		}
		else {
			final long		current = System.currentTimeMillis();
			final Calendar	cal = Calendar.getInstance();
			
			this.facade = facade;
			this.ks = ks;
			cal.setTimeInMillis(current);
			
			cal.add(Calendar.DAY_OF_MONTH,-1);
		    this.dateFrom = new Date(cal.getTimeInMillis());
			cal.add(Calendar.YEAR,1);
		    this.dateTo = new Date(cal.getTimeInMillis());
		}
	}

	@Override
	public RefreshMode onField(SignCertificateDialog inst, Object id, String fieldName, Object oldValue) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}

	@Override
	public LoggerFacade getLogger() {
		return facade;
	}
}
