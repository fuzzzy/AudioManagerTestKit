package fz.rnd.audiotest;

import android.content.Context;

/**
 * Created by fz on 15.02.16.
 */
abstract class IMediaLoad {
    protected LoadConfig mConfig;
    protected Context mContext;

    IMediaLoad(Context c, LoadConfig configuration) {
        mConfig = configuration;
        mContext = c;
    }

    abstract void start();

    abstract void stop();
}
