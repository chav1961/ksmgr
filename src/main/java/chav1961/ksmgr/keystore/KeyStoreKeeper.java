package chav1961.ksmgr.keystore;

import java.io.File;
import java.security.KeyStore;

import chav1961.ksmgr.Application;
import chav1961.ksmgr.internal.AlgorithmRepo;
import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

public class KeyStoreKeeper {
	private final Application	parent;
	private final KeyStoreFSM	fsm = new KeyStoreFSM((fsm, terminal, fromState, toState, action, parameter)->processFSM(fsm, terminal, fromState, toState, action, parameter));
	private final AlgorithmRepo	algo = new AlgorithmRepo();
	private KeyStore			ks = null;
	private File				ksFile = null;
	
	public KeyStoreKeeper(final Application parent) {
		if (parent == null) {
			throw new NullPointerException("Parent application can't be null"); 
		}
		else {
			this.parent = parent;
		}
	}

	public File getKeyStoreFile() {
		return ksFile;
	}

	public KeyStore getKeyStore() {
		return ks;
	}
	
	public KeyStoreState processAction(final KeyStoreTerminal action, final Object parameter) {
		try{
			fsm.processTerminal(action, parameter);
		} catch (FlowException e) {
			parent.getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
		return fsm.getCurrentState();
	}
	
	private void processFSM(final FSM<KeyStoreTerminal, KeyStoreState, KeyStoreAction, Object> fsm, final KeyStoreTerminal terminal, final KeyStoreState fromState, final KeyStoreState toState, final KeyStoreAction[] action, final Object parameter) {
		for (KeyStoreAction item : action) {
			switch (item) {
				case CREATE_KEYSTORE	:
					break;
				default:
					throw new UnsupportedOperationException("Action item ["+item+"] is not supported yet");
			}
		}
	}
}
