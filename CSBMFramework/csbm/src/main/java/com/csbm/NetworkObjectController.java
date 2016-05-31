package com.csbm;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/** package */ class NetworkObjectController implements BEObjectController {

  private BEHttpClient client;
  private BEObjectCoder coder;

  public NetworkObjectController(BEHttpClient client) {
    this.client = client;
    this.coder = BEObjectCoder.get();
  }

  @Override
  public Task<BEObject.State> fetchAsync(
          final BEObject.State state, String sessionToken, final BEDecoder decoder) {
    final BERESTCommand command = BERESTObjectCommand.getObjectCommand(
        state.objectId(),
        state.className(),
        sessionToken);
    command.enableRetrying();

    return command.executeAsync(client).onSuccess(new Continuation<JSONObject, BEObject.State>() {
      @Override
      public BEObject.State then(Task<JSONObject> task) throws Exception {
        JSONObject result = task.getResult();
        // Copy and clear to create an new empty instance of the same type as `state`
        BEObject.State.Init<?> builder = state.newBuilder().clear();
        return coder.decode(builder, result, decoder)
            .isComplete(true)
            .build();
      }
    });
  }

  @Override
  public Task<BEObject.State> saveAsync(
      final BEObject.State state,
      final BEOperationSet operations,
      String sessionToken,
      final BEDecoder decoder) {
    /*
     * Get the JSON representation of the object, and use some of the information to construct the
     * command.
     */
    JSONObject objectJSON = coder.encode(state, operations, PointerEncoder.get());

    BERESTObjectCommand command = BERESTObjectCommand.saveObjectCommand(
        state,
        objectJSON,
        sessionToken);
    command.enableRetrying();
    return command.executeAsync(client).onSuccess(new Continuation<JSONObject, BEObject.State>() {
      @Override
      public BEObject.State then(Task<JSONObject> task) throws Exception {
        JSONObject result = task.getResult();
        // Copy and clear to create an new empty instance of the same type as `state`
        BEObject.State.Init<?> builder = state.newBuilder().clear();
        return coder.decode(builder, result, decoder)
            .isComplete(false)
            .build();
      }
    });
  }

  @Override
  public List<Task<BEObject.State>> saveAllAsync(
      List<BEObject.State> states,
      List<BEOperationSet> operationsList,
      String sessionToken,
      List<BEDecoder> decoders) {
    int batchSize = states.size();

    List<BERESTObjectCommand> commands = new ArrayList<>(batchSize);
    BEEncoder encoder = PointerEncoder.get();
    for (int i = 0; i < batchSize; i++) {
      BEObject.State state = states.get(i);
      BEOperationSet operations = operationsList.get(i);
      JSONObject objectJSON = coder.encode(state, operations, encoder);

      BERESTObjectCommand command = BERESTObjectCommand.saveObjectCommand(
          state, objectJSON, sessionToken);
      commands.add(command);
    }

    final List<Task<JSONObject>> batchTasks =
            BERESTObjectBatchCommand.executeBatch(client, commands, sessionToken);

    final List<Task<BEObject.State>> tasks = new ArrayList<>(batchSize);
    for (int i = 0; i < batchSize; i++) {
      final BEObject.State state = states.get(i);
      final BEDecoder decoder = decoders.get(i);
      tasks.add(batchTasks.get(i).onSuccess(new Continuation<JSONObject, BEObject.State>() {
        @Override
        public BEObject.State then(Task<JSONObject> task) throws Exception {
          JSONObject result = task.getResult();
          // Copy and clear to create an new empty instance of the same type as `state`
          BEObject.State.Init<?> builder = state.newBuilder().clear();
          return coder.decode(builder, result, decoder)
              .isComplete(false)
              .build();
        }
      }));
    }
    return tasks;
  }

  @Override
  public Task<Void> deleteAsync(BEObject.State state, String sessionToken) {
    BERESTObjectCommand command = BERESTObjectCommand.deleteObjectCommand(
        state, sessionToken);
    command.enableRetrying();

    return command.executeAsync(client).makeVoid();
  }

  @Override
  public List<Task<Void>> deleteAllAsync(
          List<BEObject.State> states, String sessionToken) {
    int batchSize = states.size();

    List<BERESTObjectCommand> commands = new ArrayList<>(batchSize);
    for (int i = 0; i < batchSize; i++) {
      BEObject.State state = states.get(i);
      BERESTObjectCommand command = BERESTObjectCommand.deleteObjectCommand(
          state, sessionToken);
      command.enableRetrying();
      commands.add(command);
    }

    final List<Task<JSONObject>> batchTasks =
            BERESTObjectBatchCommand.executeBatch(client, commands, sessionToken);

    List<Task<Void>> tasks = new ArrayList<>(batchSize);
    for (int i = 0; i < batchSize; i++) {
      tasks.add(batchTasks.get(i).makeVoid());
    }
    return tasks;
  }
}
