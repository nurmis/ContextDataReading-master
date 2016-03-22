package io.github.cluo29.contextdatareading.noisiness;

import io.github.cluo29.contextdatareading.table.*;
import io.github.cluo29.contextdatareading.*;


public interface DataNoiser {

    interface Noiser<T extends AbstractEvent> {
        T apply(T event);
    }

    public Noiser<Battery> battery();

}
