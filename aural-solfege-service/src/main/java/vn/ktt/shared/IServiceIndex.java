package vn.ktt.shared;

public interface IServiceIndex<Key, Service> {
    Class<? extends Key> getKey();
    Service getService();
}
