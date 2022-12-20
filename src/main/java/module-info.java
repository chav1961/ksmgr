module chav1961.ksmgr {
	requires transitive chav1961.purelib;
	requires chav1961.bt.security;
	requires java.base;
	requires bcprov.jdk15on;
	
	exports chav1961.ksmgr;
	opens chav1961.ksmgr to chav1961.purelib;
}
