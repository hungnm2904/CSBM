package com.csbm;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;

/** package */ class NetworkSessionController implements BESessionController {

  private final BEHttpClient client;
  private final BEObjectCoder coder;

  public NetworkSessionController(BEHttpClient client) {
    this.client = client;
    this.coder = BEObjectCoder.get(); // TODO(grantland): Inject
  }

  @Override
  public Task<BEObject.State> getSessionAsync(String sessionToken) {
    BERESTSessionCommand command =
            BERESTSessionCommand.getCurrentSessionCommand(sessionToken);

    return command.executeAsync(client).onSuccess(new Continuation<JSONObject, BEObject.State>() {
      @Override
      public BEObject.State then(Task<JSONObject> task) throws Exception {
        JSONObject result = task.getResult();
        return coder.decode(new BEObject.State.Builder("_Session"), result, BEDecoder.get())
            .isComplete(true)
            .build();
      }
    });
  }

  @Override
  public Task<Void> revokeAsync(String sessionToken) {
    return BERESTSessionCommand.revoke(sessionToken)
        .executeAsync(client)
        .makeVoid();
  }

  @Override
  public Task<BEObject.State> upgradeToRevocable(String sessionToken) {
    BERESTSessionCommand command =
            BERESTSessionCommand.upgradeToRevocableSessionCommand(sessionToken);
    return command.executeAsync(client).onSuccess(new Continuation<JSONObject, BEObject.State>() {
      @Override
      public BEObject.State then(Task<JSONObject> task) throws Exception {
        JSONObject result = task.getResult();
        return coder.decode(new BEObject.State.Builder("_Session"), result, BEDecoder.get())
            .isComplete(true)
            .build();
      }
    });
  }
}
