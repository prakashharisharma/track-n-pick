package com.example.dto.io;

import java.io.Serializable;
import java.util.List;

public class MCResult implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7704441081622739216L;

    String s;

    List<Long> t;

    List<Double> o;

    List<Double> h;

    List<Double> l;

    List<Double> c;

    List<Long> v;

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public List<Long> getT() {
        return t;
    }

    public void setT(List<Long> t) {
        this.t = t;
    }

    public List<Double> getO() {
        return o;
    }

    public void setO(List<Double> o) {
        this.o = o;
    }

    public List<Double> getH() {
        return h;
    }

    public void setH(List<Double> h) {
        this.h = h;
    }

    public List<Double> getL() {
        return l;
    }

    public void setL(List<Double> l) {
        this.l = l;
    }

    public List<Double> getC() {
        return c;
    }

    public void setC(List<Double> c) {
        this.c = c;
    }

    public List<Long> getV() {
        return v;
    }

    public void setV(List<Long> v) {
        this.v = v;
    }
}
