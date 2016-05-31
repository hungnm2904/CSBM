package com.csbm;

import org.json.JSONObject;

import java.util.Map;

import bolts.Continuation;
import bolts.Task;

/** package */ class NetworkUserController implements BEUserController {

  private static final int STATUS_CODE_CREATED = 201;

  private final BEHttpClient client;
  private final BEObjectCoder coder;
  private final boolean revocableSession;

  public NetworkUserController(BEHttpClient client) {
    this(client, false);
  }

  public NetworkUserController(BEHttpClient client, boolean revocableSession) {
    this.client = client;
    this.coder = BEObjectCoder.get(); // TODO(grantland): Inject
    this.revocableSession = revocableSession;
  }

  @Override
  public Task<BEUser.State> signUpAsync(
      final BEObject.State state,
      BEOperationSet operations,
      String sessionToken) {
    JSONObject objectJSON = coder.encode(state, operations, PointerEncoder.get());
    BERESTCommand command = BERESTUserCommand.signUpUserCommand(
        objectJSON, sessionToken, revocableSession);

    return command.executeAsync(client).onSuccess(new Continuation<JSONObject, BEUser.State>() {
      @Override
      public BEUser.State then(Task<JSONObject> task) throws Exception {
        JSONObject result = task.getResult();
        return coder.decode(new BEUser.State.Builder(), result, BEDecoder.get())
            .isComplete(false)
            .isNew(true)
            .build();
      }
    });
  }

  //region logInAsync

  @Override
  public Task<BEUser.State> logInAsync(
          String username, String password) {
    BERESTCommand command = BERESTUserCommand.logInUserCommand(
        username, password, revocableSession);
    return command.executeAsync(client).onSuccess(new Continuation<JSONObject, BEUser.State>() {
      @Override
      public BEUser.State then(Task<JSONObject> task) throws Exception {
        JSONObject result = task.getResult();

        return coder.decode(new BEUser.State.Builder(), result, BEDecoder.get())
            .isComplete(true)
            .build();
      }
    });
  }

  @Override
  public Task<BEUser.State> logInAsync(
          BEUser.State state, BEOperationSet operations) {
    JSONObject objectJSON = coder.encode(state, operations, PointerEncoder.get());
    final BERESTUserCommand command = BERESTUserCommand.serviceLogInUserCommand(
        objectJSON, state.sessionToken(), revocableSession);

    return command.executeAsync(client).onSuccess(new Continuation<JSONObject, BEUser.State>() {
      @Override
      public BEUser.State then(Task<JSONObject> task) throws Exception {
        JSONObject result = task.getResult();

        // TODO(grantland): Does the server really respond back with complete object data if the
        // object isn't new?
        boolean isNew = command.getStatusCode() == STATUS_CODE_CREATED;
        boolean isComplete = !isNew;

        return coder.decode(new BEUser.State.Builder(), result, BEDecoder.get())
            .isComplete(isComplete)
            .isNew(isNew)
            .build();
      }
    });
  }

  @Override
  public Task<BEUser.State> logInAsync(
          final String authType, final Map<String, String> authData) {
    final BERESTUserCommand command = BERESTUserCommand.serviceLogInUserCommand(
        authType, authData, revocableSession);
    return command.executeAsync(client).onSuccess(new Continuation<JSONObject, BEUser.State>() {
      @Override
      public BEUser.State then(Task<JSONObject> task) throws Exception {
        JSONObject result = task.getResult();

        return coder.decode(new BEUser.State.Builder(), result, BEDecoder.get())
            .isComplete(true)
            .isNew(command.getStatusCode() == STATUS_CODE_CREATED)
            .putAuthData(authType, authData)
            .build();
      }
    });
  }

  //endregion

  @Override
  public Task<BEUser.State> getUserAsync(String sessionToken) {
    BERESTCommand command = BERESTUserCommand.getCurrentUserCommand(sessionToken);
    return command.executeAsync(client).onSuccess(new Continuation<JSONObject, BEUser.State>() {
      @Override
      public BEUser.State then(Task<JSONObject> task) throws Exception {
        JSONObject result = task.getResult();

        return coder.decode(new BEUser.State.Builder(), result, BEDecoder.get())
            .isComplete(true)
            .build();
      }
    });
  }

  @Override
  public Task<Void> requestPasswordResetAsync(String email) {
    BERESTCommand command = BERESTUserCommand.resetPasswordResetCommand(email);
    return command.executeAsync(client).makeVoid();
  }
}
