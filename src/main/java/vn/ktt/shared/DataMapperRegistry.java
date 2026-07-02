package vn.ktt.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataMapperRegistry<DataFrom, DataTo, DataMapper extends IDataMapper<DataFrom, DataTo>> {
    protected Map<Class<? extends DataFrom>, DataMapper> dataFromMapper;
    protected Map<Class<? extends DataTo>, DataMapper> dataToMapper;

    public DataMapperRegistry(List<DataMapper> dataMappers) {
        this.dataFromMapper = new HashMap<>();
        this.dataToMapper = new HashMap<>();

        for (var dataMapper : dataMappers) {
            this.dataFromMapper.put(dataMapper.getDataFromClass(), dataMapper);
            this.dataToMapper.put(dataMapper.getDataToClass(), dataMapper);
        }
    }

    protected IDataMapper<DataFrom, DataTo> getMapperBaseOnDataFrom(DataFrom dataFrom) {
        if (dataFromMapper.containsKey(dataFrom.getClass())) {
            return dataFromMapper.get(dataFrom.getClass());
        }
        return null;
    }

    protected IDataMapper<DataFrom, DataTo> getMapperBaseOnDataTo(DataTo dataTo) {
        if (dataFromMapper.containsKey(dataTo.getClass())) {
            return dataToMapper.get(dataTo.getClass());
        }
        return null;
    }
}
