package vn.ktt.shared;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DataMapperRegistry<Key, DataFrom, DataTo> {

    protected final Map<Key, IDataMapper<Key, DataFrom, DataTo>> mapperRegistry;
    private final Map<Class<? extends DataFrom>, Set<Key>> dataFromKeyMap;
    private final Map<Class<? extends DataTo>, Set<Key>> dataToKeyMap;

    protected DataMapperRegistry(List<? extends IDataMapper<Key, DataFrom, DataTo>> dataMappers) {
        this.mapperRegistry = new HashMap<>();
        this.dataFromKeyMap = new HashMap<>();
        this.dataToKeyMap = new HashMap<>();

        for (var dataMapper : dataMappers) {
            var key = dataMapper.getKey();
            if (this.mapperRegistry.putIfAbsent(key, dataMapper) != null) {
                throw new IllegalArgumentException("Duplicate mapper key registered: " + key);
            }

            var source = dataMapper.getDataFromClass();
            var target = dataMapper.getDataToClass();

            this.dataFromKeyMap.computeIfAbsent(source, unused -> new HashSet<>()).add(key);
            this.dataToKeyMap.computeIfAbsent(target, unused -> new HashSet<>()).add(key);
        }
    }

    protected DataTo transform(DataFrom from) {
        return transform(from, null);
    }

    protected DataTo transform(DataFrom from, Class<? extends DataTo> targetClass) {
        var sourceKeys = findKeys(this.dataFromKeyMap, from.getClass());
        var candidateKeys = targetClass == null
                ? sourceKeys
                : intersect(sourceKeys, findKeys(this.dataToKeyMap, targetClass));

        return resolveMapper(resolveUniqueKey(candidateKeys)).transform(from);
    }

    protected DataFrom reverseTransform(DataTo to) {
        return reverseTransform(to, null);
    }

    protected DataFrom reverseTransform(DataTo to, Class<? extends DataFrom> sourceClass) {
        var targetKeys = findKeys(this.dataToKeyMap, to.getClass());
        var candidateKeys = sourceClass == null
                ? targetKeys
                : intersect(targetKeys, findKeys(this.dataFromKeyMap, sourceClass));

        return resolveMapper(resolveUniqueKey(candidateKeys)).reverseTransform(to);
    }

    private Set<Key> findKeys(Map<?, Set<Key>> index, Class<?> clazz) {
        return index.getOrDefault(clazz, Set.of());
    }

    private Set<Key> intersect(Set<Key> sourceKeys, Set<Key> targetKeys) {
        var result = new HashSet<>(sourceKeys);
        result.retainAll(targetKeys);
        return result;
    }

    private Key resolveUniqueKey(Set<Key> candidateKeys) {
        if (candidateKeys.isEmpty()) {
            throw new IllegalArgumentException("No mapper registered for the given types");
        }
        if (candidateKeys.size() > 1) {
            throw new IllegalArgumentException(
                    "Ambiguous mapper resolution (" + candidateKeys.size() + " mappers match). "
                            + "Provide an explicit target/source class to disambiguate. Matched keys: " + candidateKeys);
        }
        return candidateKeys.iterator().next();
    }

    private IDataMapper<Key, DataFrom, DataTo> resolveMapper(Key key) {
        var mapper = this.mapperRegistry.get(key);
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper registered for key: " + key);
        }
        return mapper;
    }
}