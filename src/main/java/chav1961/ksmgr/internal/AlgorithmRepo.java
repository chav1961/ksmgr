package chav1961.ksmgr.internal;

import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AlgorithmRepo {
	private final Set<String>				providers = new HashSet<>();
	private final Map<String,Set<String>>	algos = new HashMap<>();
	
	public AlgorithmRepo() {
		for (Provider p : Security.getProviders()) {
			providers.add(p.getName());
			for (Service s : p.getServices()) {
				if (!algos.containsKey(s.getType())) {
					algos.put(s.getType(),new HashSet<>());
				}
				algos.get(s.getType()).add(s.getAlgorithm());
			}
		}
	}
	
	public Iterable<String> getProviders() {
		return providers; 
	}
	
	public Iterable<String> getServices() {
		return algos.keySet(); 
	}
	
	public Iterable<String> getAlgorithms(final String service) {
		if (service == null || service.isEmpty()) {
			throw new IllegalArgumentException("Service name can't be null or empty"); 
		}
		else if (!algos.containsKey(service)) {
			throw new IllegalArgumentException("Unknown service name ["+service+"]"); 
		}
		else {
			return algos.get(service);
		}
	}
}
