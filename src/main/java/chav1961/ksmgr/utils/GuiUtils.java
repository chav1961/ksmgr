package chav1961.ksmgr.utils;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SwingUtils;

public class GuiUtils {
	private static final URL[]	EMPTY_URLS = new URL[0];
	
	public static <T,K> boolean askDialog(final Frame owner, final Localizer localizer, final T instance, final int width, final int height) {
		try{final ContentMetadataInterface	mdi = ContentModelFactory.forAnnotatedClass(instance.getClass());
			
			try(final SimpleURLClassLoader	loader = new SimpleURLClassLoader(EMPTY_URLS);
				final AutoBuiltForm<T,K>	abf = new AutoBuiltForm<T,K>(mdi, localizer, loader, instance, (FormManager<K,T>)instance)) {
				
				for (Module m : abf.getUnnamedModules()) {
					instance.getClass().getModule().addExports(instance.getClass().getPackageName(),m);
				}
				abf.setPreferredSize(new Dimension(width,height));
				return AutoBuiltForm.ask(owner, localizer, abf);
			}
		} catch (ContentException | IOException e) {
			SwingUtils.getNearestLogger(owner).message(Severity.error, e.getLocalizedMessage());
			return false;
		} 
	}

	public static <T,K> boolean askDialog(final Dialog owner, final Localizer localizer, final T instance, final int width, final int height) {
		try{final ContentMetadataInterface	mdi = ContentModelFactory.forAnnotatedClass(instance.getClass());
			
			try(final SimpleURLClassLoader	loader = new SimpleURLClassLoader(EMPTY_URLS);
				final AutoBuiltForm<T,K>	abf = new AutoBuiltForm<T,K>(mdi, localizer, loader, instance, (FormManager<K,T>)instance)) {
				
				for (Module m : abf.getUnnamedModules()) {
					instance.getClass().getModule().addExports(instance.getClass().getPackageName(),m);
				}
				abf.setPreferredSize(new Dimension(width,height));
				return AutoBuiltForm.ask(owner, localizer, abf);
			}
		} catch (ContentException | IOException e) {
			SwingUtils.getNearestLogger(owner).message(Severity.error, e.getLocalizedMessage());
			return false;
		} 
	}
}
