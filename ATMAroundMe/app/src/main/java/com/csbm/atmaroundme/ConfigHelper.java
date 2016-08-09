package com.csbm.atmaroundme;

import com.csbm.BEConfig;
import com.csbm.BEException;
import com.csbm.ConfigCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by akela on 09/08/2016.
 */
public class ConfigHelper {

    private BEConfig config;
    private long configLastFetchedTime;


    public void fetchConfigIfNeeded() {
        final long configRefreshInterval = 60 * 60; // 1 hour

        if (config == null ||
                System.currentTimeMillis() - configLastFetchedTime > configRefreshInterval) {
            // Set the config to current, just to load the cache
            config = BEConfig.getCurrentConfig();

            // Set the current time, to flag that the operation started and prevent double fetch
            BEConfig.getInBackground(new ConfigCallback() {
                @Override
                public void done(BEConfig parseConfig, BEException e) {
                    if (e == null) {
                        //retrieved successfully
                        config = parseConfig;
                        configLastFetchedTime = System.currentTimeMillis();
                    } else {
                        // Fetch failed, reset the time
                        configLastFetchedTime = 0;
                    }
                }
            });
        }
    }


    public List<Float> getSearchDistanceAvailableOptions() {
        final List<Float> defaultOptions = Arrays.asList(250.0f, 1000.0f, 2000.0f, 5000.0f);

        List<Number> options = config.getList("availableFilterDistances");
        if (options == null) {
            return defaultOptions;
        }

        List<Float> typedOptions = new ArrayList<Float>();
        for (Number option : options) {
            typedOptions.add(option.floatValue());
        }

        return typedOptions;
    }

    public int getPostMaxCharacterCount () {
        int value = config.getInt("postMaxCharacterCount", 140);
        return value;
    }
}
