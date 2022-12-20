package chav1961.ksmgr.internal;

import java.security.Provider;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class PureLibClient {
//	public static final Localizer	ROOT_LOCALIZER;
	public static final Provider	PROVIDER;
	
	static {
//		try{ROOT_LOCALIZER = LocalizerFactory.getLocalizer(URI.create(Localizer.LOCALIZER_SCHEME+":xml:root://"+PureLibClient.class.getCanonicalName()+"/chav1961/ksmgr/i18n/i18n.xml"));
//			PureLibSettings.PURELIB_LOCALIZER.add(ROOT_LOCALIZER);
//			ROOT_LOCALIZER.add(PureLibSettings.PURELIB_LOCALIZER);
//		} catch (LocalizationException e) {
//			throw new PreparationException("Registration of localizer in module ["+PureLibClient.class.getModule().getName()+"] failed: "+e.getLocalizedMessage(),e);
//		}
		
		Security.addProvider(PROVIDER = new BouncyCastleProvider());
	}
	
	public static void registerInPureLib() {
	}
}
