package chav1961.ksmgr.internal;

import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AlgorithmRepo {
	private final Set<String>							providers = new HashSet<>();
	private final Set<String>							services = new HashSet<>();
	private final Map<String,Set<String>>				algos = new HashMap<>();
	private final Map<String,Map<String,Set<String>>>	providerAlgos = new HashMap<>();
	
	public AlgorithmRepo() {
		for (Provider p : Security.getProviders()) {
			providers.add(p.getName());
			providerAlgos.put(p.getName(),new HashMap<>());
			for (Service s : p.getServices()) {
				services.add(s.getType());
				if (!algos.containsKey(s.getType())) {
					algos.put(s.getType(),new HashSet<>());
				}
				algos.get(s.getType()).add(s.getAlgorithm());
				if (!providerAlgos.get(p.getName()).containsKey(s.getType())) {
					providerAlgos.get(p.getName()).put(s.getType(),new HashSet<>());
				}
				providerAlgos.get(p.getName()).get(s.getType()).add(s.getAlgorithm());
			}
		}
	}
	
	public Iterable<String> getProviders() {
		return providers; 
	}
	
	public Iterable<String> getServices() {
		return services; 
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

	public Iterable<String> getAlgorithms(final String service, final String provider) {
		if (service == null || service.isEmpty()) {
			throw new IllegalArgumentException("Service name can't be null or empty"); 
		}
		else if (provider == null || provider.isEmpty()) {
			throw new IllegalArgumentException("Provider name can't be null or empty"); 
		}
		else if (!providerAlgos.containsKey(provider)) {
			throw new IllegalArgumentException("Unknown provider name ["+provider+"]"); 
		}
		else if (!providerAlgos.get(provider).containsKey(service)) {
			throw new IllegalArgumentException("Unknown service name ["+service+"] for provider ["+provider+"]"); 
		}
		else {
			return providerAlgos.get(provider).get(service);
		}
	}
}
