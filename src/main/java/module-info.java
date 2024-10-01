module chav1961.ksmgr {
	requires transitive chav1961.purelib;
	requires java.base;
	requires java.desktop;
	
	exports chav1961.ksmgr;
	opens chav1961.ksmgr to chav1961.purelib;
}
