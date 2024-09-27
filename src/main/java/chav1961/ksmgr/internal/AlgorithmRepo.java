package chav1961.ksmgr.internal;

import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import chav1961.purelib.basic.Utils;

public class AlgorithmRepo {
	private static final AlgorithmRepo	REPO = new AlgorithmRepo(); 
	
	private final Set<String>				providers = new HashSet<>();
	private final Set<String>				services = new HashSet<>();
	private final Map<String,Set<String>>	algos = new HashMap<>();
	private final Map<String,Map<String,Set<String>>>	providerAlgos = new HashMap<>();
	
	private AlgorithmRepo() {
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
	
	public boolean hasProvider(final String provider) {
		if (Utils.checkEmptyOrNullString(provider)) {
			throw new IllegalArgumentException("Provider to test can't be null or empty");
		}
		else {
			return providers.contains(provider);
		}
	}
	
	public Iterable<String> getServices() {
		return services; 
	}
	
	public boolean hasService(final String service) {
		if (Utils.checkEmptyOrNullString(service)) {
			throw new IllegalArgumentException("Service to test can't be null or empty");
		}
		else {
			return service.contains(service);
		}
	}

	public boolean hasService(final String provider, final String service) {
		if (Utils.checkEmptyOrNullString(provider)) {
			throw new IllegalArgumentException("Provider to test can't be null or empty");
		}
		else if (Utils.checkEmptyOrNullString(service)) {
			throw new IllegalArgumentException("Service to test can't be null or empty");
		}
		else {
			return providerAlgos.containsKey(provider) && providerAlgos.get(provider).containsKey(service);
		}
	}
	
	public Iterable<String> getAlgorithms(final String service) {
		if (Utils.checkEmptyOrNullString(service)) {
			throw new IllegalArgumentException("Service name can't be null or empty"); 
		}
		else if (!algos.containsKey(service)) {
			throw new IllegalArgumentException("Unknown service name ["+service+"]"); 
		}
		else {
			return algos.get(service);
		}
	}

	public Iterable<String> getAlgorithms(final String provider, final String service) {
		if (Utils.checkEmptyOrNullString(provider)) {
			throw new IllegalArgumentException("Provider name can't be null or empty"); 
		}
		else if (Utils.checkEmptyOrNullString(service)) {
			throw new IllegalArgumentException("Service name can't be null or empty"); 
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
	
	public boolean exists(final String service, final String provider, final String algorithm) {
		if (Utils.checkEmptyOrNullString(service)) {
			throw new IllegalArgumentException("Service name can't be null or empty"); 
		}
		else if (Utils.checkEmptyOrNullString(provider)) {
			throw new IllegalArgumentException("Provider name can't be null or empty"); 
		}
		else if (Utils.checkEmptyOrNullString(algorithm)) {
			throw new IllegalArgumentException("Algorithm name can't be null or empty"); 
		}
		else if (hasService(provider, service)) {
			for (String item : getAlgorithms(service, provider)) {
				if (algorithm.equals(item)) {
					return true;
				}
			}
			return false;
		}
		else {
			return false;
		}
	}
	
	public static AlgorithmRepo getInstance() {
		return REPO;
	}
}
