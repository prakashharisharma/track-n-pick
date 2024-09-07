package com.example.storage.model.result;

public class AverageResult {

    private String _id;

    private double avg;

    public AverageResult(String _id, double avg) {
        super();
        this._id = _id;
        this.avg = avg;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }
}
