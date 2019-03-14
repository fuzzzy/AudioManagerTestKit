package fz.rnd.audiotest;

import android.content.Context;

/**
 * Created by fz on 15.02.16.
 */
abstract class IMediaLoad {
    protected LoadConfig _cfg = null;
    protected Context _ctx = null;

    IMediaLoad(Context c, LoadConfig configuration) {
        _cfg = configuration;
        _ctx = c;
    }

    abstract void start();

    abstract void stop();
}
