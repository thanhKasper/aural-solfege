package vn.ktt.shared;

public interface IDataMapper<Key, DataFrom, DataTo> {
    Key getKey();
    Class<? extends DataFrom> getDataFromClass();
    Class<? extends DataTo> getDataToClass();

    DataTo transform(DataFrom dataFrom);
    DataFrom reverseTransform(DataTo dataTo);
}