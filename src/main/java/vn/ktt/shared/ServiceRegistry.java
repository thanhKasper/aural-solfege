package vn.ktt.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ServiceRegistry<Key, Service> {
    protected Map<Class<? extends Key>, Service> serviceMap;

    public ServiceRegistry(List<IServiceIndex<Key, Service>> services) {
        this.serviceMap = new HashMap<>();
        for (var service : services) {
            this.serviceMap.put(service.getKey(), service.getService());
        }
    }

    public Service getService(Key key) {
        if (serviceMap.containsKey(key.getClass())) {
            return this.serviceMap.get(key.getClass());
        }

        return null;
    }
}
