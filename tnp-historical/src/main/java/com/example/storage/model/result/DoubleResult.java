package com.example.storage.model.result;

public class DoubleResult {
    private String _id;

    private Double value;

    public DoubleResult(String _id, Double value) {
        super();
        this._id = _id;
        this.value = value;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
