package chav1961.ksmgr.internal;

import java.net.URI;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;

public class PureLibClient {
//	public static final Localizer	ROOT_LOCALIZER;
	
	static {
//		try{ROOT_LOCALIZER = LocalizerFactory.getLocalizer(URI.create(Localizer.LOCALIZER_SCHEME+":xml:root://"+PureLibClient.class.getCanonicalName()+"/chav1961/ksmgr/i18n/i18n.xml"));
//			PureLibSettings.PURELIB_LOCALIZER.add(ROOT_LOCALIZER);
//			ROOT_LOCALIZER.add(PureLibSettings.PURELIB_LOCALIZER);
//		} catch (LocalizationException e) {
//			throw new PreparationException("Registration of localizer in module ["+PureLibClient.class.getModule().getName()+"] failed: "+e.getLocalizedMessage(),e);
//		}
		
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public static void registerInPureLib() {
	}
}
