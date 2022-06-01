package chav1961.ksmgr;

import chav1961.purelib.basic.FSM;

public class KeyStoreFSM extends FSM<KeystoreTerminal, KeystoreState, KeystoreAction, Application>{
	private static final FSMLine<KeystoreTerminal, KeystoreState, KeystoreAction>[]	TABLE = new FSMLine[] {
			
						};

	public KeyStoreFSM(final FSMCallback<KeystoreTerminal, KeystoreState, KeystoreAction, Application> callback) throws NullPointerException, IllegalArgumentException {
		super(callback, KeystoreState.MISSING, TABLE);
	}
}
