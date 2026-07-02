package vn.ktt.shared;

public interface IDataMapper<DataFrom, DataTo> {
    Class<? extends DataFrom> getDataFromClass();
    Class<? extends DataTo> getDataToClass();

    DataTo transform(DataFrom dataFrom);
    DataFrom reverseTransform(DataTo dataTo);
}
