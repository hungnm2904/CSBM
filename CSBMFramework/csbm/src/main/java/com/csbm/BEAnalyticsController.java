package com.csbm;

import org.json.JSONObject;

import java.util.Map;

import bolts.Task;

/** package */ class BEAnalyticsController {

  /* package for test */ BEEventuallyQueue eventuallyQueue;

  public BEAnalyticsController(BEEventuallyQueue eventuallyQueue) {
    this.eventuallyQueue = eventuallyQueue;
  }

  public Task<Void> trackEventInBackground(final String name,
                                           Map<String, String> dimensions, String sessionToken) {
    BERESTCommand command = BERESTAnalyticsCommand.trackEventCommand(name, dimensions,
        sessionToken);

    Task<JSONObject> eventuallyTask = eventuallyQueue.enqueueEventuallyAsync(command, null);
    return eventuallyTask.makeVoid();
  }

  public Task<Void> trackAppOpenedInBackground(String pushHash, String sessionToken) {
    BERESTCommand command = BERESTAnalyticsCommand.trackAppOpenedCommand(pushHash,
        sessionToken);

    Task<JSONObject> eventuallyTask = eventuallyQueue.enqueueEventuallyAsync(command, null);
    return eventuallyTask.makeVoid();
  }
}
