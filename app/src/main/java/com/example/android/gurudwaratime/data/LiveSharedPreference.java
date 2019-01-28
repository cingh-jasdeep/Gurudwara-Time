package com.example.android.gurudwaratime.data;

import android.arch.lifecycle.MutableLiveData;
import android.content.SharedPreferences;

/**
 * sr https://gist.github.com/MikeFot/65d550ab20eae32f15a0f95f7576bc79
 *
 * @param <T> type of shared preference
 */

public class LiveSharedPreference<T> extends MutableLiveData<T> {

    private final SharedPreferences mSharedPreferences;
    private final SharedPreferences.OnSharedPreferenceChangeListener mListener;
    private final String mPreferenceKey;


    public LiveSharedPreference(final String preferenceKey,
                                final SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
        mPreferenceKey = preferenceKey;
        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

                if (key.equals(mPreferenceKey)) {
                    updateLivePrefValue(sharedPreferences, key);
                }

            }
        };


    }

    private void updateLivePrefValue(SharedPreferences sharedPreferences, String key) {
        final T value = (T) sharedPreferences.getAll().get(key);
        setValue(value);
    }

    @Override
    protected void onActive() {
        super.onActive();
        updateLivePrefValue(mSharedPreferences, mPreferenceKey);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);
    }
}
