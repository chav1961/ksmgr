package chav1961.ksmgr.keystore;

import chav1961.purelib.basic.FSM;

public class KeyStoreFSM extends FSM<KeyStoreTerminal, KeyStoreState, KeyStoreAction, Object>{
	private static final FSMLine<KeyStoreTerminal, KeyStoreState, KeyStoreAction>[]	TABLE = new FSMLine[] {
			
						};

	public KeyStoreFSM(final FSMCallback<KeyStoreTerminal, KeyStoreState, KeyStoreAction, Object> callback) throws NullPointerException, IllegalArgumentException {
		super(callback, KeyStoreState.MISSING, TABLE);
	}
}
