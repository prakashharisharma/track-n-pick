package com.example.util;

public class SupportAndResistanceUtil {

    public static double pivotPoint(double high, double low, double close){
        return (high + low + close) / 3;
    }

    public static double firstResistance(double pivotPoint, double high, double low){

        return (2 * pivotPoint) - low;
    }

    public static double secondResistance(double pivotPoint,double high, double low){

        return pivotPoint + (high - low);
    }

    public static double thirdResistance(double pivotPoint,double high, double low){

        return high + 2 * (pivotPoint - low);
    }

    public static double firstSupport(double pivotPoint,double high, double low){
        return (2 * pivotPoint) - high;
    }

    public static double secondSupport(double pivotPoint,double high, double low){
        return pivotPoint - (high - low);
    }

    public static double thirdSupport(double pivotPoint,double high, double low){
        return low - 2 * (high - pivotPoint);
    }


}
