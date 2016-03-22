package io.github.cluo29.contextdatareading.noisiness;

import io.github.cluo29.contextdatareading.table.*;

import java.util.Random;

public final class SimpleDataNoiser implements DataNoiser {
    private final long timestamp;
    private final long duration;
    private final double noiseProbability;
    private final double lower;
    private final double upper;
    private Random random = new Random();
    private Random randomInt = new Random();

    public SimpleDataNoiser(long timestamp, long duration, double noiseProbability, double lower, double upper) {
        this.timestamp=timestamp;
        this.duration=duration;
        this.noiseProbability=noiseProbability;
        this.lower=lower;
        this.upper=upper;
    }


    //int batteryVoltage, int batteryTemperature
    public Noiser<Battery> battery() {
        return new Noiser<Battery>() {
            public Battery apply(Battery rv) {
                if(rv.timestamp()>=timestamp && rv.timestamp()<=timestamp+duration && random.nextDouble()<noiseProbability){

            }
                return rv;
            }
        };
    }

}
