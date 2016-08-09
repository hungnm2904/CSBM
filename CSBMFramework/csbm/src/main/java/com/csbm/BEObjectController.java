package com.csbm;

import java.util.List;

import bolts.Task;

/** package */ interface BEObjectController {

  Task<BEObject.State> fetchAsync(
          BEObject.State state, String sessionToken, BEDecoder decoder);

  Task<BEObject.State> saveAsync(
          BEObject.State state,
          BEOperationSet operations,
          String sessionToken,
          BEDecoder decoder);

  List<Task<BEObject.State>> saveAllAsync(
          List<BEObject.State> states,
          List<BEOperationSet> operationsList,
          String sessionToken,
          List<BEDecoder> decoders);

  Task<Void> deleteAsync(BEObject.State state, String sessionToken);

  List<Task<Void>> deleteAllAsync(List<BEObject.State> states, String sessionToken);
}
