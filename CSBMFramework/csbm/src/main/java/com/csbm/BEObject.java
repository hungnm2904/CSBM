package com.csbm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * The {@code BEObject} is a local representation of data that can be saved and retrieved from
 * the CSBM cloud.
 * <p/>
 * The basic workflow for creating new data is to construct a new {@code BEObject}, use
 * {@link #put(String, Object)} to fill it with data, and then use {@link #saveInBackground()} to
 * persist to the cloud.
 * <p/>
 * The basic workflow for accessing existing data is to use a {@link BEQuery} to specify which
 * existing data to retrieve.
 */
public class BEObject {
  private static final String AUTO_CLASS_NAME = "_Automatic";
  /* package */ static final String VERSION_NAME = "1.13.1-SNAPSHOT";

  /*
  REST JSON Keys
  */
  private static final String KEY_OBJECT_ID = "objectId";
  private static final String KEY_CLASS_NAME = "className";
  private static final String KEY_ACL = "ACL";
  private static final String KEY_CREATED_AT = "createdAt";
  private static final String KEY_UPDATED_AT = "updatedAt";

  /*
  Internal JSON Keys - Used to store internal data when persisting {@code BEObject}s locally.
  */
  private static final String KEY_COMPLETE = "__complete";
  private static final String KEY_OPERATIONS = "__operations";
  /* package */ static final String KEY_IS_DELETING_EVENTUALLY = "__isDeletingEventually";
  // Because Grantland messed up naming this... We'll only try to read from this for backward
  // compat, but I think we can be safe to assume any deleteEventuallys from long ago are obsolete
  // and not check after a while
  private static final String KEY_IS_DELETING_EVENTUALLY_OLD = "isDeletingEventually";

  private static BEObjectController getObjectController() {
    return BECorePlugins.getInstance().getObjectController();
  }

  private static LocalIdManager getLocalIdManager() {
    return BECorePlugins.getInstance().getLocalIdManager();
  }

  private static BEObjectSubclassingController getSubclassingController() {
    return BECorePlugins.getInstance().getSubclassingController();
  }

  /** package */ static class State {

    public static Init<?> newBuilder(String className) {
      if ("_User".equals(className)) {
        return new BEUser.State.Builder();
      }
      return new Builder(className);
    }

    /** package */ static abstract class Init<T extends Init> {

      private final String className;
      private String objectId;
      private long createdAt = -1;
      private long updatedAt = -1;
      private boolean isComplete;
      /* package */ Map<String, Object> serverData = new HashMap<>();

      public Init(String className) {
        this.className = className;
      }

      /* package */ Init(State state) {
        className = state.className();
        objectId = state.objectId();
        createdAt = state.createdAt();
        updatedAt = state.updatedAt();
        for (String key : state.keySet()) {
          serverData.put(key, state.get(key));
        }
        isComplete = state.isComplete();
      }

      /* package */ abstract T self();

      /* package */ abstract <S extends State> S build();

      public T objectId(String objectId) {
        this.objectId = objectId;
        return self();
      }

      public T createdAt(Date createdAt) {
        this.createdAt = createdAt.getTime();
        return self();
      }

      public T createdAt(long createdAt) {
        this.createdAt = createdAt;
        return self();
      }

      public T updatedAt(Date updatedAt) {
        this.updatedAt = updatedAt.getTime();
        return self();
      }

      public T updatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
        return self();
      }

      public T isComplete(boolean complete) {
        isComplete = complete;
        return self();
      }

      public T put(String key, Object value) {
        serverData.put(key, value);
        return self();
      }

      public T remove(String key) {
        serverData.remove(key);
        return self();
      }

      public T clear() {
        objectId = null;
        createdAt = -1;
        updatedAt = -1;
        isComplete = false;
        serverData.clear();
        return self();
      }

      /**
       * Applies a {@code State} on top of this {@code Builder} instance.
       *
       * @param other The {@code State} to apply over this instance.
       * @return A new {@code Builder} instance.
       */
      public T apply(State other) {
        if (other.objectId() != null) {
          objectId(other.objectId());
        }
        if (other.createdAt() > 0) {
          createdAt(other.createdAt());
        }
        if (other.updatedAt() > 0) {
          updatedAt(other.updatedAt());
        }
        isComplete(isComplete || other.isComplete());
        for (String key : other.keySet()) {
          put(key, other.get(key));
        }
        return self();
      }

      public T apply(BEOperationSet operations) {
        for (String key : operations.keySet()) {
          BEFieldOperation operation = operations.get(key);
          Object oldValue = serverData.get(key);
          Object newValue = operation.apply(oldValue, key);
          if (newValue != null) {
            put(key, newValue);
          } else {
            remove(key);
          }
        }
        return self();
      }
    }

    /* package */ static class Builder extends Init<Builder> {

      public Builder(String className) {
        super(className);
      }

      public Builder(State state) {
        super(state);
      }

      @Override
      /* package */ Builder self() {
        return this;
      }

      public State build() {
        return new State(this);
      }
    }

    private final String className;
    private final String objectId;
    private final long createdAt;
    private final long updatedAt;
    private final Map<String, Object> serverData;
    private final boolean isComplete;

    /* package */ State(Init<?> builder) {
      className = builder.className;
      objectId = builder.objectId;
      createdAt = builder.createdAt;
      updatedAt = builder.updatedAt > 0
          ? builder.updatedAt
          : createdAt;
      serverData = Collections.unmodifiableMap(new HashMap<>(builder.serverData));
      isComplete = builder.isComplete;
    }

    @SuppressWarnings("unchecked")
    public <T extends Init<?>> T newBuilder() {
      return (T) new Builder(this);
    }

    public String className() {
      return className;
    }

    public String objectId() {
      return objectId;
    }

    public long createdAt() {
      return createdAt;
    }

    public long updatedAt() {
      return updatedAt;
    }

    public boolean isComplete() {
      return isComplete;
    }

    public Object get(String key) {
      return serverData.get(key);
    }

    public Set<String> keySet() {
      return serverData.keySet();
    }

    @Override
    public String toString() {
      return String.format(Locale.US, "%s@%s[" +
              "className=%s, objectId=%s, createdAt=%d, updatedAt=%d, isComplete=%s, " +
              "serverData=%s]",
          getClass().getName(),
          Integer.toHexString(hashCode()),
          className,
          objectId,
          createdAt,
          updatedAt,
          isComplete,
          serverData);
    }
  }

  /* package */ final Object mutex = new Object();
  /* package */ final TaskQueue taskQueue = new TaskQueue();

  private State state;
  /* package */ final LinkedList<BEOperationSet> operationSetQueue;

  // Cached State
  private final Map<String, Object> estimatedData;

  private String localId;
  private final BEMulticastDelegate<BEObject> saveEvent = new BEMulticastDelegate<>();

  /* package */ boolean isDeleted;
  //TODO (grantland): Derive this off the EventuallyPins as opposed to +/- count.
  /* package */ int isDeletingEventually;

  private static final ThreadLocal<String> isCreatingPointerForObjectId =
      new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
          return null;
        }
      };

  /*
   * This is used only so that we can pass it to createWithoutData as the objectId to make it create
   * an unfetched pointer that has no objectId. This is useful only in the context of the offline
   * store, where you can have an unfetched pointer for an object that can later be fetched from the
   * store.
   */
  /* package */ private static final String NEW_OFFLINE_OBJECT_ID_PLACEHOLDER =
      "*** Offline Object ***";

  /**
   * The base class constructor to call in subclasses. Uses the class name specified with the
   * {@link BEClassName} annotation on the subclass.
   */
  protected BEObject() {
    this(AUTO_CLASS_NAME);
  }

  /**
   * Constructs a new {@code BEObject} with no data in it. A {@code BEObject} constructed in
   * this way will not have an objectId and will not persist to the database until {@link #save()}
   * is called.
   * <p>
   * Class names must be alphanumerical plus underscore, and start with a letter. It is recommended
   * to name classes in <code>PascalCaseLikeThis</code>.
   *
   * @param theClassName
   *          The className for this {@code BEObject}.
   */
  public BEObject(String theClassName) {
    // We use a ThreadLocal rather than passing a parameter so that createWithoutData can do the
    // right thing with subclasses. It's ugly and terrible, but it does provide the development
    // experience we generally want, so... yeah. Sorry to whomever has to deal with this in the
    // future. I pinky-swear we won't make a habit of this -- you believe me, don't you?
    String objectIdForPointer = isCreatingPointerForObjectId.get();

    if (theClassName == null) {
      throw new IllegalArgumentException(
          "You must specify a BE class name when creating a new BEObject.");
    }
    if (AUTO_CLASS_NAME.equals(theClassName)) {
      theClassName = getSubclassingController().getClassName(getClass());
    }

    // If this is supposed to be created by a factory but wasn't, throw an exception.
    if (!getSubclassingController().isSubclassValid(theClassName, getClass())) {
      throw new IllegalArgumentException(
          "You must create this type of BEObject using BEObject.create() or the proper subclass.");
    }

    operationSetQueue = new LinkedList<>();
    operationSetQueue.add(new BEOperationSet());
    estimatedData = new HashMap<>();

    State.Init<?> builder = newStateBuilder(theClassName);
    // When called from new, assume hasData for the whole object is true.
    if (objectIdForPointer == null) {
      setDefaultValues();
      builder.isComplete(true);
    } else {
      if (!objectIdForPointer.equals(NEW_OFFLINE_OBJECT_ID_PLACEHOLDER)) {
        builder.objectId(objectIdForPointer);
      }
      builder.isComplete(false);
    }
    // This is a new untouched object, we don't need cache rebuilding, etc.
    state = builder.build();

    OfflineStore store = CSBM.getLocalDatastore();
    if (store != null) {
      store.registerNewObject(this);
    }
  }

  /**
   * Creates a new {@code BEObject} based upon a class name. If the class name is a special type
   * (e.g. for {@code BEUser}), then the appropriate type of {@code BEObject} is returned.
   *
   * @param className
   *          The class of object to create.
   * @return A new {@code BEObject} for the given class name.
   */
  public static BEObject create(String className) {
    return getSubclassingController().newInstance(className);
  }

  /**
   * Creates a new {@code BEObject} based upon a subclass type. Note that the object will be
   * created based upon the {@link BEClassName} of the given subclass type. For example, calling
   * create(BEUser.class) may create an instance of a custom subclass of {@code BEUser}.
   *
   * @param subclass
   *          The class of object to create.
   * @return A new {@code BEObject} based upon the class name of the given subclass type.
   */
  @SuppressWarnings("unchecked")
  public static <T extends BEObject> T create(Class<T> subclass) {
    return (T) create(getSubclassingController().getClassName(subclass));
  }

  /**
   * Creates a reference to an existing {@code BEObject} for use in creating associations between
   * {@code BEObject}s. Calling {@link #isDataAvailable()} on this object will return
   * {@code false} until {@link #fetchIfNeeded()} or {@link #refresh()} has been called. No network
   * request will be made.
   *
   * @param className
   *          The object's class.
   * @param objectId
   *          The object id for the referenced object.
   * @return A {@code BEObject} without data.
   */
  public static BEObject createWithoutData(String className, String objectId) {
    OfflineStore store = CSBM.getLocalDatastore();
    try {
      if (objectId == null) {
        isCreatingPointerForObjectId.set(NEW_OFFLINE_OBJECT_ID_PLACEHOLDER);
      } else {
        isCreatingPointerForObjectId.set(objectId);
      }
      BEObject object = null;
      if (store != null && objectId != null) {
        object = store.getObject(className, objectId);
      }

      if (object == null) {
        object = create(className);
        if (object.hasChanges()) {
          throw new IllegalStateException(
              "A BEObject subclass default constructor must not make changes "
                  + "to the object that cause it to be dirty."
          );
        }
      }

      return object;

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Failed to create instance of subclass.", e);
    } finally {
      isCreatingPointerForObjectId.set(null);
    }
  }

  /**
   * Creates a reference to an existing {@code BEObject} for use in creating associations between
   * {@code BEObject}s. Calling {@link #isDataAvailable()} on this object will return
   * {@code false} until  {@link #fetchIfNeeded()} or {@link #refresh()} has been called. No network
   * request will be made.
   *
   * @param subclass
   *          The {@code BEObject} subclass to create.
   * @param objectId
   *          The object id for the referenced object.
   * @return A {@code BEObject} without data.
   */
  @SuppressWarnings({"unused", "unchecked"})
  public static <T extends BEObject> T createWithoutData(Class<T> subclass, String objectId) {
    return (T) createWithoutData(getSubclassingController().getClassName(subclass), objectId);
  }

  /**
   * Registers a custom subclass type with the BE SDK, enabling strong-typing of those
   * {@code BEObject}s whenever they appear. Subclasses must specify the {@link BEClassName}
   * annotation and have a default constructor.
   *
   * @param subclass
   *          The subclass type to register.
   */
  public static void registerSubclass(Class<? extends BEObject> subclass) {
    getSubclassingController().registerSubclass(subclass);
  }

  /* package for tests */ static void unregisterSubclass(Class<? extends BEObject> subclass) {
    getSubclassingController().unregisterSubclass(subclass);
  }

  /**
   * Adds a task to the queue for all of the given objects.
   */
  static <T> Task<T> enqueueForAll(final List<? extends BEObject> objects,
      Continuation<Void, Task<T>> taskStart) {
    // The task that will be complete when all of the child queues indicate they're ready to start.
    final TaskCompletionSource<Void> readyToStart = new TaskCompletionSource<>();

    // First, we need to lock the mutex for the queue for every object. We have to hold this
    // from at least when taskStart() is called to when obj.taskQueue enqueue is called, so
    // that saves actually get executed in the order they were setup by taskStart().
    // The locks have to be sorted so that we always acquire them in the same order.
    // Otherwise, there's some risk of deadlock.
    List<Lock> locks = new ArrayList<>(objects.size());
    for (BEObject obj : objects) {
      locks.add(obj.taskQueue.getLock());
    }
    LockSet lock = new LockSet(locks);

    lock.lock();
    try {
      // The task produced by TaskStart
      final Task<T> fullTask;
      try {
        // By running this immediately, we allow everything prior to toAwait to run before waiting
        // for all of the queues on all of the objects.
        fullTask = taskStart.then(readyToStart.getTask());
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      // Add fullTask to each of the objects' queues.
      final List<Task<Void>> childTasks = new ArrayList<>();
      for (BEObject obj : objects) {
        obj.taskQueue.enqueue(new Continuation<Void, Task<T>>() {
          @Override
          public Task<T> then(Task<Void> task) throws Exception {
            childTasks.add(task);
            return fullTask;
          }
        });
      }

      // When all of the objects' queues are ready, signal fullTask that it's ready to go on.
      Task.whenAll(childTasks).continueWith(new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
          readyToStart.setResult(null);
          return null;
        }
      });
      return fullTask;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Converts a {@code BEObject.State} to a {@code BEObject}.
   *
   * @param state
   *          The {@code BEObject.State} to convert from.
   * @return A {@code BEObject} instance.
   */
  /* package */ static <T extends BEObject> T from(BEObject.State state) {
    @SuppressWarnings("unchecked")
    T object = (T) BEObject.createWithoutData(state.className(), state.objectId());
    synchronized (object.mutex) {
      State newState;
      if (state.isComplete()) {
        newState = state;
      } else {
        newState = object.getState().newBuilder().apply(state).build();
      }
      object.setState(newState);
    }
    return object;
  }

  /**
   * Creates a new {@code BEObject} based on data from the BE server.
   *
   * @param json
   *          The object's data.
   * @param defaultClassName
   *          The className of the object, if none is in the JSON.
   * @param isComplete
   *          {@code true} if this is all of the data on the server for the object.
   */
  /* package */ static <T extends BEObject> T fromJSON(JSONObject json, String defaultClassName,
                                                          boolean isComplete) {
    return fromJSON(json, defaultClassName, isComplete, BEDecoder.get());
  }

  /**
   * Creates a new {@code BEObject} based on data from the CSBM server.
   *
   * @param json
   *          The object's data.
   * @param defaultClassName
   *          The className of the object, if none is in the JSON.
   * @param isComplete
   *          {@code true} if this is all of the data on the server for the object.
   * @param decoder
   *          Delegate for knowing how to decode the values in the JSON.
   */
  /* package */ static <T extends BEObject> T fromJSON(JSONObject json, String defaultClassName,
                                                          boolean isComplete, BEDecoder decoder) {
    String className = json.optString(KEY_CLASS_NAME, defaultClassName);
    if (className == null) {
      return null;
    }
    String objectId = json.optString(KEY_OBJECT_ID, null);
    @SuppressWarnings("unchecked")
    T object = (T) BEObject.createWithoutData(className, objectId);
    State newState = object.mergeFromServer(object.getState(), json, decoder, isComplete);
    object.setState(newState);
    return object;
  }

  /**
   * Method used by csbm server webhooks implementation to convert raw JSON to BE Object
   *
   * Method is used by csbm server webhooks implementation to create a
   * new {@code BEObject} from the incoming json payload. The method is different from
   * {@link #fromJSON(JSONObject, String, boolean)} ()} in that it calls
   * {@link #build(JSONObject, BEDecoder)} which populates operation queue
   * rather then the server data from the incoming JSON, as at external server the incoming
   * JSON may not represent the actual server data. Also it handles
   * {@link BEFieldOperations} separately.
   *
   * @param json
   *          The object's data.
   * @param decoder
   *          Delegate for knowing how to decode the values in the JSON.
   */
  /* package */ static <T extends BEObject> T fromJSONPayload(
          JSONObject json, BEDecoder decoder) {
    String className = json.optString(KEY_CLASS_NAME);
    if (className == null || BETextUtils.isEmpty(className)) {
      return null;
    }
    String objectId = json.optString(KEY_OBJECT_ID, null);
    @SuppressWarnings("unchecked")
    T object = (T) BEObject.createWithoutData(className, objectId);
    object.build(json, decoder);
    return object;
  }

  //region Getter/Setter helper methods

  /* package */ State.Init<?> newStateBuilder(String className) {
    return new State.Builder(className);
  }

  /* package */ State getState() {
    synchronized (mutex) {
      return state;
    }
  }

  /**
   * Updates the current state of this object as well as updates our in memory cached state.
   *
   * @param newState The new state.
   */
  /* package */ void setState(State newState) {
    synchronized (mutex) {
      setState(newState, true);
    }
  }

  private void setState(State newState, boolean notifyIfObjectIdChanges) {
    synchronized (mutex) {
      String oldObjectId = state.objectId();
      String newObjectId = newState.objectId();

      state = newState;

      if (notifyIfObjectIdChanges && !BETextUtils.equals(oldObjectId, newObjectId)) {
        notifyObjectIdChanged(oldObjectId, newObjectId);
      }

      rebuildEstimatedData();
    }
  }

  /**
   * Accessor to the class name.
   */
  public String getClassName() {
    synchronized (mutex) {
      return state.className();
    }
  }

  /**
   * This reports time as the server sees it, so that if you make changes to a {@code BEObject}, then
   * wait a while, and then call {@link #save()}, the updated time will be the time of the
   * {@link #save()} call rather than the time the object was changed locally.
   *
   * @return The last time this object was updated on the server.
   */
  public Date getUpdatedAt() {
    long updatedAt = getState().updatedAt();
    return updatedAt > 0
        ? new Date(updatedAt)
        : null;
  }

  /**
   * This reports time as the server sees it, so that if you create a {@code BEObject}, then wait a
   * while, and then call {@link #save()}, the creation time will be the time of the first
   * {@link #save()} call rather than the time the object was created locally.
   *
   * @return The first time this object was saved on the server.
   */
  public Date getCreatedAt() {
    long createdAt = getState().createdAt();
    return createdAt > 0
        ? new Date(createdAt)
        : null;
  }

  //endregion

  /**
   * Returns a set view of the keys contained in this object. This does not include createdAt,
   * updatedAt, authData, or objectId. It does include things like username and ACL.
   */
  public Set<String> keySet() {
    synchronized (mutex) {
      return Collections.unmodifiableSet(estimatedData.keySet());
    }
  }

  /**
   * Copies all of the operations that have been performed on another object since its last save
   * onto this one.
   */
  /* package */ void copyChangesFrom(BEObject other) {
    synchronized (mutex) {
      BEOperationSet operations = other.operationSetQueue.getFirst();
      for (String key : operations.keySet()) {
        performOperation(key, operations.get(key));
      }
    }
  }

  /* package */ void mergeFromObject(BEObject other) {
    synchronized (mutex) {
      // If they point to the same instance, we don't need to merge.
      if (this == other) {
        return;
      }

      State copy = other.getState().newBuilder().build();

      // We don't want to notify if an objectId changed here since we utilize this method to merge
      // an anonymous current user with a new BEUser instance that's calling signUp(). This
      // doesn't make any sense and we should probably remove that code in BEUser.
      // Otherwise, there shouldn't be any objectId changes here since this method is only otherwise
      // used in fetchAll.
      setState(copy, false);
    }
  }

  /**
   * Clears changes to this object's {@code key} made since the last call to {@link #save()} or
   * {@link #saveInBackground()}.
   *
   * @param key The {@code key} to revert changes for.
   */
  public void revert(String key) {
    synchronized (mutex) {
      if (isDirty(key)) {
        currentOperations().remove(key);
        rebuildEstimatedData();
      }
    }
  }

  /**
   * Clears any changes to this object made since the last call to {@link #save()} or
   * {@link #saveInBackground()}.
   */
  public void revert() {
    synchronized (mutex) {
      if (isDirty()) {
        currentOperations().clear();
        rebuildEstimatedData();
      }
    }
  }

  /**
   * Deep traversal on this object to grab a copy of any object referenced by this object. These
   * instances may have already been fetched, and we don't want to lose their data when refreshing
   * or saving.
   *
   * @return the map mapping from objectId to {@code BEObject} which has been fetched.
   */
  private Map<String, BEObject> collectFetchedObjects() {
    final Map<String, BEObject> fetchedObjects = new HashMap<>();
    BETraverser traverser = new BETraverser() {
      @Override
      protected boolean visit(Object object) {
        if (object instanceof BEObject) {
          BEObject beObj = (BEObject) object;
          State state = beObj.getState();
          if (state.objectId() != null && state.isComplete()) {
            fetchedObjects.put(state.objectId(), beObj);
          }
        }
        return true;
      }
    };
    traverser.traverse(estimatedData);
    return fetchedObjects;
  }

  /**
   * Helper method called by {@link #fromJSONPayload(JSONObject, BEDecoder)}
   *
   * The method helps webhooks implementation to build BE object from raw JSON payload.
   * It is different from {@link #mergeFromServer(State, JSONObject, BEDecoder, boolean)}
   * as the method saves the key value pairs (other than className, objectId, updatedAt and
   * createdAt) in the operation queue rather than the server data. It also handles
   * {@link BEFieldOperations} differently.
   *
   * @param json : JSON object to be converted to BE object
   * @param decoder : Decoder to be used for Decoding JSON
   */
  /* package */ void build(JSONObject json, BEDecoder decoder) {
    try {
      State.Builder builder = new State.Builder(state)
        .isComplete(true);

      builder.clear();

      Iterator<?> keys = json.keys();
      while (keys.hasNext()) {
        String key = (String) keys.next();
        /*
        __className:  Used by fromJSONPayload, should be stripped out by the time it gets here...
         */
        if (key.equals(KEY_CLASS_NAME)) {
          continue;
        }
        if (key.equals(KEY_OBJECT_ID)) {
          String newObjectId = json.getString(key);
          builder.objectId(newObjectId);
          continue;
        }
        if (key.equals(KEY_CREATED_AT)) {
          builder.createdAt(BEDateFormat.getInstance().parse(json.getString(key)));
          continue;
        }
        if (key.equals(KEY_UPDATED_AT)) {
          builder.updatedAt(BEDateFormat.getInstance().parse(json.getString(key)));
          continue;
        }

        Object value = json.get(key);
        Object decodedObject = decoder.decode(value);
        if (decodedObject instanceof BEFieldOperation) {
          performOperation(key, (BEFieldOperation)decodedObject);
        }
        else {
          put(key, decodedObject);
        }
      }

      setState(builder.build());
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Merges from JSON in REST format.
   *
   * Updates this object with data from the server.
   *
   * @see #toJSONObjectForSaving(State, BEOperationSet, BEEncoder)
   */
  /* package */ State mergeFromServer(
          State state, JSONObject json, BEDecoder decoder, boolean completeData) {
    try {
      // If server data is complete, consider this object to be fetched.
      State.Init<?> builder = state.newBuilder();
      if (completeData) {
        builder.clear();
      }
      builder.isComplete(state.isComplete() || completeData);

      Iterator<?> keys = json.keys();
      while (keys.hasNext()) {
        String key = (String) keys.next();
        /*
        __type:       Returned by queries and cloud functions to designate body is a BEObject
        __className:  Used by fromJSON, should be stripped out by the time it gets here...
         */
        if (key.equals("__type") || key.equals(KEY_CLASS_NAME)) {
          continue;
        }
        if (key.equals(KEY_OBJECT_ID)) {
          String newObjectId = json.getString(key);
          builder.objectId(newObjectId);
          continue;
        }
        if (key.equals(KEY_CREATED_AT)) {
          builder.createdAt(BEDateFormat.getInstance().parse(json.getString(key)));
          continue;
        }
        if (key.equals(KEY_UPDATED_AT)) {
          builder.updatedAt(BEDateFormat.getInstance().parse(json.getString(key)));
          continue;
        }
        if (key.equals(KEY_ACL)) {
          BEACL acl = BEACL.createACLFromJSONObject(json.getJSONObject(key), decoder);
          builder.put(KEY_ACL, acl);
          continue;
        }

        Object value = json.get(key);
        Object decodedObject = decoder.decode(value);
        builder.put(key, decodedObject);
      }

      return builder.build();
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  //region LDS-processing methods.

  /**
   * Convert to REST JSON for persisting in LDS.
   *
   * @see #mergeREST(State, JSONObject, BEDecoder)
   */
  /* package */ JSONObject toRest(BEEncoder encoder) {
    State state;
    List<BEOperationSet> operationSetQueueCopy;
    synchronized (mutex) {
      // mutex needed to lock access to state and operationSetQueue and operationSetQueue & children
      // are mutable
      state = getState();

      // operationSetQueue is a List of Lists, so we'll need custom copying logic
      int operationSetQueueSize = operationSetQueue.size();
      operationSetQueueCopy = new ArrayList<>(operationSetQueueSize);
      for (int i = 0; i < operationSetQueueSize; i++) {
        BEOperationSet original = operationSetQueue.get(i);
        BEOperationSet copy = new BEOperationSet(original);
        operationSetQueueCopy.add(copy);
      }
    }
    return toRest(state, operationSetQueueCopy, encoder);
  }

  /* package */ JSONObject toRest(
          State state, List<BEOperationSet> operationSetQueue, BEEncoder objectEncoder) {
      // Public data goes in dataJSON; special fields go in objectJSON.
      JSONObject json = new JSONObject();

      try {
        // REST JSON (State)
        json.put(KEY_CLASS_NAME, state.className());
        if (state.objectId() != null) {
          json.put(KEY_OBJECT_ID, state.objectId());
        }
        if (state.createdAt() > 0) {
          json.put(KEY_CREATED_AT,
              BEDateFormat.getInstance().format(new Date(state.createdAt())));
        }
        if (state.updatedAt() > 0) {
          json.put(KEY_UPDATED_AT,
              BEDateFormat.getInstance().format(new Date(state.updatedAt())));
        }
        for (String key : state.keySet()) {
          Object value = state.get(key);
          json.put(key, objectEncoder.encode(value));
        }

        // Internal JSON
        //TODO(klimt): We'll need to rip all this stuff out and put it somewhere else if we start
        // using the REST api and want to send data to CSBM.
        json.put(KEY_COMPLETE, state.isComplete());
        json.put(KEY_IS_DELETING_EVENTUALLY, isDeletingEventually);

        // Operation Set Queue
        JSONArray operations = new JSONArray();
        for (BEOperationSet operationSet : operationSetQueue) {
          operations.put(operationSet.toRest(objectEncoder));
        }
        json.put(KEY_OPERATIONS, operations);

      } catch (JSONException e) {
        throw new RuntimeException("could not serialize object to JSON");
      }

      return json;
  }

  /**
   * Merge with REST JSON from LDS.
   *
   * @see #toRest(BEEncoder)
   */
  /* package */ void mergeREST(State state, JSONObject json, BEDecoder decoder) {
    ArrayList<BEOperationSet> saveEventuallyOperationSets = new ArrayList<>();

    synchronized (mutex) {
      try {
        boolean isComplete = json.getBoolean(KEY_COMPLETE);
        isDeletingEventually = BEJSONUtils.getInt(json, Arrays.asList(
            KEY_IS_DELETING_EVENTUALLY,
            KEY_IS_DELETING_EVENTUALLY_OLD
        ));
        JSONArray operations = json.getJSONArray(KEY_OPERATIONS);
        {
          BEOperationSet newerOperations = currentOperations();
          operationSetQueue.clear();

          // Add and enqueue any saveEventually operations, roll forward any other operation sets
          // (operation sets here are generally failed/incomplete saves).
          BEOperationSet current = null;
          for (int i = 0; i < operations.length(); i++) {
            JSONObject operationSetJSON = operations.getJSONObject(i);
            BEOperationSet operationSet = BEOperationSet.fromRest(operationSetJSON, decoder);

            if (operationSet.isSaveEventually()) {
              if (current != null) {
                operationSetQueue.add(current);
                current = null;
              }
              saveEventuallyOperationSets.add(operationSet);
              operationSetQueue.add(operationSet);
              continue;
            }

            if (current != null) {
              operationSet.mergeFrom(current);
            }
            current = operationSet;
          }
          if (current != null) {
            operationSetQueue.add(current);
          }

          // Merge the changes that were previously in memory into the updated object.
          currentOperations().mergeFrom(newerOperations);
        }

        // We only want to merge server data if we our updatedAt is null (we're unsaved or from
        // #createWithoutData) or if the JSON's updatedAt is newer than ours.
        boolean mergeServerData = false;
        if (state.updatedAt() < 0) {
          mergeServerData = true;
        } else if (json.has(KEY_UPDATED_AT)) {
          Date otherUpdatedAt = BEDateFormat.getInstance().parse(json.getString(KEY_UPDATED_AT));
          if (new Date(state.updatedAt()).compareTo(otherUpdatedAt) < 0) {
            mergeServerData = true;
          }
        }

        if (mergeServerData) {
          // Clean up internal json keys
          JSONObject mergeJSON = BEJSONUtils.create(json, Arrays.asList(
              KEY_COMPLETE, KEY_IS_DELETING_EVENTUALLY, KEY_IS_DELETING_EVENTUALLY_OLD,
              KEY_OPERATIONS
          ));
          State newState = mergeFromServer(state, mergeJSON, decoder, isComplete);
          setState(newState);
        }
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    // We cannot modify the taskQueue inside synchronized (mutex).
    for (BEOperationSet operationSet : saveEventuallyOperationSets) {
      enqueueSaveEventuallyOperationAsync(operationSet);
    }
  }

  //endregion

  private boolean hasDirtyChildren() {
    synchronized (mutex) {
      // We only need to consider the currently estimated children here,
      // because they're the only ones that might need to be saved in a
      // subsequent call to save, which is the meaning of "dirtiness".
      List<BEObject> unsavedChildren = new ArrayList<>();
      collectDirtyChildren(estimatedData, unsavedChildren, null);
      return unsavedChildren.size() > 0;
    }
  }

  /**
   * Whether any key-value pair in this object (or its children) has been added/updated/removed and
   * not saved yet.
   *
   * @return Whether this object has been altered and not saved yet.
   */
  public boolean isDirty() {
    return this.isDirty(true);
  }

  /* package */ boolean isDirty(boolean considerChildren) {
    synchronized (mutex) {
      return (isDeleted || getObjectId() == null || hasChanges() || (considerChildren && hasDirtyChildren()));
    }
  }

  boolean hasChanges() {
    synchronized (mutex) {
      return currentOperations().size() > 0;
    }
  }

  /**
   * Returns {@code true} if this {@code BEObject} has operations in operationSetQueue that
   * haven't been completed yet, {@code false} if there are no operations in the operationSetQueue.
   */
  /* package */ boolean hasOutstandingOperations() {
    synchronized (mutex) {
      // > 1 since 1 is for unsaved changes.
      return operationSetQueue.size() > 1;
    }
  }

  /**
   * Whether a value associated with a key has been added/updated/removed and not saved yet.
   *
   * @param key
   *          The key to check for
   * @return Whether this key has been altered and not saved yet.
   */
  public boolean isDirty(String key) {
    synchronized (mutex) {
      return currentOperations().containsKey(key);
    }
  }

  /**
   * Accessor to the object id. An object id is assigned as soon as an object is saved to the
   * server. The combination of a className and an objectId uniquely identifies an object in your
   * application.
   *
   * @return The object id.
   */
  public String getObjectId() {
    synchronized (mutex) {
      return state.objectId();
    }
  }

  /**
   * Setter for the object id. In general you do not need to use this. However, in some cases this
   * can be convenient. For example, if you are serializing a {@code BEObject} yourself and wish
   * to recreate it, you can use this to recreate the {@code BEObject} exactly.
   */
  public void setObjectId(String newObjectId) {
    synchronized (mutex) {
      String oldObjectId = state.objectId();
      if (BETextUtils.equals(oldObjectId, newObjectId)) {
        return;
      }

      // We don't need to use setState since it doesn't affect our cached state.
      state = state.newBuilder().objectId(newObjectId).build();
      notifyObjectIdChanged(oldObjectId, newObjectId);
    }
  }

  /**
   * Returns the localId, which is used internally for serializing relations to objects that don't
   * yet have an objectId.
   */
  /* package */ String getOrCreateLocalId() {
    synchronized (mutex) {
      if (localId == null) {
        if (state.objectId() != null) {
          throw new IllegalStateException(
              "Attempted to get a localId for an object with an objectId.");
        }
        localId = getLocalIdManager().createLocalId();
      }
      return localId;
    }
  }

  // Sets the objectId without marking dirty.
  private void notifyObjectIdChanged(String oldObjectId, String newObjectId) {
    synchronized (mutex) {
      // The offline store may throw if this object already had a different objectId.
      OfflineStore store = CSBM.getLocalDatastore();
      if (store != null) {
        store.updateObjectId(this, oldObjectId, newObjectId);
      }

      if (localId != null) {
        getLocalIdManager().setObjectId(localId, newObjectId);
        localId = null;
      }
    }
  }

  private BERESTObjectCommand currentSaveEventuallyCommand(
          BEOperationSet operations, BEEncoder objectEncoder, String sessionToken)
      throws BEException {
    State state = getState();

    /*
     * Get the JSON representation of the object, and use some of the information to construct the
     * command.
     */
    JSONObject objectJSON = toJSONObjectForSaving(state, operations, objectEncoder);

    BERESTObjectCommand command = BERESTObjectCommand.saveObjectCommand(
          state,
          objectJSON,
          sessionToken);
    command.enableRetrying();
    return command;
  }

  /**
   * Converts a {@code BEObject} to a JSON representation for saving to CSBM.
   *
   * <pre>
   * {
   *   data: { // objectId plus any BEFieldOperations },
   *   classname: class name for the object
   * }
   * </pre>
   *
   * updatedAt and createdAt are not included. only dirty keys are represented in the data.
   *
   * @see #mergeFromServer(State state, JSONObject, BEDecoder, boolean)
   */
  // Currently only used by saveEventually
  /* package */ <T extends State> JSONObject toJSONObjectForSaving(
          T state, BEOperationSet operations, BEEncoder objectEncoder) {
    JSONObject objectJSON = new JSONObject();

    try {
      // Serialize the data
      for (String key : operations.keySet()) {
        BEFieldOperation operation = operations.get(key);
        objectJSON.put(key, objectEncoder.encode(operation));

        // TODO(grantland): Use cached value from hashedObjects if it's a set operation.
      }

      if (state.objectId() != null) {
        objectJSON.put(KEY_OBJECT_ID, state.objectId());
      }
    } catch (JSONException e) {
      throw new RuntimeException("could not serialize object to JSON");
    }

    return objectJSON;
  }

  /**
   * Handles the result of {@code save}.
   *
   * Should be called on success or failure.
   */
  // TODO(grantland): Remove once we convert saveEventually and BEUser.signUp/resolveLaziness
  // to controllers
  /* package */ Task<Void> handleSaveResultAsync(
          final JSONObject result, final BEOperationSet operationsBeforeSave) {
    BEObject.State newState = null;

    if (result != null) { // Success
      synchronized (mutex) {
        final Map<String, BEObject> fetchedObjects = collectFetchedObjects();
        BEDecoder decoder = new KnownBEObjectDecoder(fetchedObjects);
        newState = BEObjectCoder.get().decode(getState().newBuilder().clear(), result, decoder)
            .isComplete(false)
            .build();
      }
    }

    return handleSaveResultAsync(newState, operationsBeforeSave);
  }

  /**
   * Handles the result of {@code save}.
   *
   * Should be called on success or failure.
   */
  /* package */ Task<Void> handleSaveResultAsync(
          final BEObject.State result, final BEOperationSet operationsBeforeSave) {
    Task<Void> task = Task.forResult((Void) null);

    final boolean success = result != null;
    synchronized (mutex) {
      // Find operationsBeforeSave in the queue so that we can remove it and move to the next
      // operation set.
      ListIterator<BEOperationSet> opIterator =
          operationSetQueue.listIterator(operationSetQueue.indexOf(operationsBeforeSave));
      opIterator.next();
      opIterator.remove();

      if (!success) {
        // Merge the data from the failed save into the next save.
        BEOperationSet nextOperation = opIterator.next();
        nextOperation.mergeFrom(operationsBeforeSave);
        return task;
      }
    }

    /*
     * If this object is in the offline store, then we need to make sure that we pull in any dirty
     * changes it may have before merging the server data into it.
     */
    final OfflineStore store = CSBM.getLocalDatastore();
    if (store != null) {
      task = task.onSuccessTask(new Continuation<Void, Task<Void>>() {
        @Override
        public Task<Void> then(Task<Void> task) throws Exception {
          return store.fetchLocallyAsync(BEObject.this).makeVoid();
        }
      });
    }

    // fetchLocallyAsync will return an error if this object isn't in the LDS yet and that's ok
    task = task.continueWith(new Continuation<Void, Void>() {
      @Override
      public Void then(Task<Void> task) throws Exception {
        synchronized (mutex) {
          State newState;
          if (result.isComplete()) {
            // Result is complete, so just replace
            newState = result;
          } else {
            // Result is incomplete, so we'll need to apply it to the current state
            newState = getState().newBuilder()
                .apply(operationsBeforeSave)
                .apply(result)
                .build();
          }
          setState(newState);
        }
        return null;
      }
    });

    if (store != null) {
      task = task.onSuccessTask(new Continuation<Void, Task<Void>>() {
        @Override
        public Task<Void> then(Task<Void> task) throws Exception {
          return store.updateDataForObjectAsync(BEObject.this);
        }
      });
    }

    task = task.onSuccess(new Continuation<Void, Void>() {
      @Override
      public Void then(Task<Void> task) throws Exception {
        saveEvent.invoke(BEObject.this, null);
        return null;
      }
    });

    return task;
  }

  /* package */ BEOperationSet startSave() {
    synchronized (mutex) {
      BEOperationSet currentOperations = currentOperations();
      operationSetQueue.addLast(new BEOperationSet());
      return currentOperations;
    }
  }

  /* package */ void validateSave() {
    // do nothing
  }

  /**
   * Saves this object to the server. Typically, you should use {@link #saveInBackground} instead of
   * this, unless you are managing your own threading.
   *
   * @throws BEException
   *           Throws an exception if the server is inaccessible.
   */
  public final void save() throws BEException {
    BETaskUtils.wait(saveInBackground());
  }

  /**
   * Saves this object to the server in a background thread. This is preferable to using {@link #save()},
   * unless your code is already running from a background thread.
   *
   * @return A {@link Task} that is resolved when the save completes.
   */
  public final Task<Void> saveInBackground() {
    return BEUser.getCurrentUserAsync().onSuccessTask(new Continuation<BEUser, Task<String>>() {
      @Override
      public Task<String> then(Task<BEUser> task) throws Exception {
        final BEUser current = task.getResult();
        if (current == null) {
          return Task.forResult(null);
        }
        if (!current.isLazy()) {
          return Task.forResult(current.getSessionToken());
        }

        // The current user is lazy/unresolved. If it is attached to us via ACL, we'll need to
        // resolve/save it before proceeding.
        if (!isDataAvailable(KEY_ACL)) {
          return Task.forResult(null);
        }
        final BEACL acl = getACL(false);
        if (acl == null) {
          return Task.forResult(null);
        }
        final BEUser user = acl.getUnresolvedUser();
        if (user == null || !user.isCurrentUser()) {
          return Task.forResult(null);
        }
        return user.saveAsync(null).onSuccess(new Continuation<Void, String>() {
          @Override
          public String then(Task<Void> task) throws Exception {
            if (acl.hasUnresolvedUser()) {
              throw new IllegalStateException("ACL has an unresolved BEUser. "
                  + "Save or sign up before attempting to serialize the ACL.");
            }
            return user.getSessionToken();
          }
        });
      }
    }).onSuccessTask(new Continuation<String, Task<Void>>() {
      @Override
      public Task<Void> then(Task<String> task) throws Exception {
        final String sessionToken = task.getResult();
        return saveAsync(sessionToken);
      }
    });
  }

  /* package */ Task<Void> saveAsync(final String sessionToken) {
    return taskQueue.enqueue(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> toAwait) throws Exception {
        return saveAsync(sessionToken, toAwait);
      }
    });
  }

  /* package */ Task<Void> saveAsync(final String sessionToken, final Task<Void> toAwait) {
    if (!isDirty()) {
      return Task.forResult(null);
    }

    final BEOperationSet operations;
    synchronized (mutex) {
      updateBeforeSave();
      validateSave();
      operations = startSave();
    }

    Task<Void> task;
    synchronized (mutex) {
      // Recursively save children

      /*
       * TODO(klimt): Why is this estimatedData and not... I mean, what if a child is
       * removed after save is called, but before the unresolved user gets resolved? It
       * won't get saved.
       */
      task = deepSaveAsync(estimatedData, sessionToken);
    }

    return task.onSuccessTask(
        TaskQueue.<Void>waitFor(toAwait)
    ).onSuccessTask(new Continuation<Void, Task<State>>() {
      @Override
      public Task<State> then(Task<Void> task) throws Exception {
        final Map<String, BEObject> fetchedObjects = collectFetchedObjects();
        BEDecoder decoder = new KnownBEObjectDecoder(fetchedObjects);
        return getObjectController().saveAsync(getState(), operations, sessionToken, decoder);
      }
    }).continueWithTask(new Continuation<State, Task<Void>>() {
      @Override
      public Task<Void> then(final Task<State> saveTask) throws Exception {
        BEObject.State result = saveTask.getResult();
        return handleSaveResultAsync(result, operations).continueWithTask(new Continuation<Void, Task<Void>>() {
          @Override
          public Task<Void> then(Task<Void> task) throws Exception {
            if (task.isFaulted() || task.isCancelled()) {
              return task;
            }

            // We still want to propagate saveTask errors
            return saveTask.makeVoid();
          }
        });
      }
    });
  }

  // Currently only used by BEPinningEventuallyQueue for saveEventually due to the limitation in
  // BECommandCache that it can only return JSONObject result.
  /* package */ Task<JSONObject> saveAsync(
      BEHttpClient client,
      final BEOperationSet operationSet,
      String sessionToken) throws BEException {
    final BERESTCommand command =
        currentSaveEventuallyCommand(operationSet, PointerEncoder.get(), sessionToken);
    return command.executeAsync(client);
  }

  /**
   * Saves this object to the server in a background thread. This is preferable to using {@link #save()},
   * unless your code is already running from a background thread.
   *
   * @param callback
   *          {@code callback.done(e)} is called when the save completes.
   */
  public final void saveInBackground(SaveCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(saveInBackground(), callback);
  }

  /* package */ void validateSaveEventually() throws BEException {
    // do nothing
  }

  /**
   * Saves this object to the server at some unspecified time in the future, even if CSBM is
   * currently inaccessible. Use this when you may not have a solid network connection, and don't
   * need to know when the save completes. If there is some problem with the object such that it
   * can't be saved, it will be silently discarded. Objects saved with this method will be stored
   * locally in an on-disk cache until they can be delivered to CSBM. They will be sent immediately
   * if possible. Otherwise, they will be sent the next time a network connection is available.
   * Objects saved this way will persist even after the app is closed, in which case they will be
   * sent the next time the app is opened. If more than 10MB of data is waiting to be sent,
   * subsequent calls to {@code #saveEventually()} or {@link #deleteEventually()}  will cause old
   * saves to be silently  discarded until the connection can be re-established, and the queued
   * objects can be saved.
   *
   * @param callback
   *          - A callback which will be called if the save completes before the app exits.
   */
  public final void saveEventually(SaveCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(saveEventually(), callback);
  }

  /**
   * Saves this object to the server at some unspecified time in the future, even if CSBM is
   * currently inaccessible. Use this when you may not have a solid network connection, and don't
   * need to know when the save completes. If there is some problem with the object such that it
   * can't be saved, it will be silently discarded. Objects saved with this method will be stored
   * locally in an on-disk cache until they can be delivered to CSBM. They will be sent immediately
   * if possible. Otherwise, they will be sent the next time a network connection is available.
   * Objects saved this way will persist even after the app is closed, in which case they will be
   * sent the next time the app is opened. If more than 10MB of data is waiting to be sent,
   * subsequent calls to {@code #saveEventually()} or {@link #deleteEventually()}  will cause old
   * saves to be silently  discarded until the connection can be re-established, and the queued
   * objects can be saved.
   *
   * @return A {@link Task} that is resolved when the save completes.
   */
  public final Task<Void> saveEventually() {
    if (!isDirty()) {
      CSBM.getEventuallyQueue().fakeObjectUpdate();
      return Task.forResult(null);
    }

    final BEOperationSet operationSet;
    final BERESTCommand command;
    final Task<JSONObject> runEventuallyTask;

    synchronized (mutex) {
      updateBeforeSave();
      try {
        validateSaveEventually();
      } catch (BEException e) {
        return Task.forError(e);
      }

      // TODO(klimt): Once we allow multiple saves on an object, this
      // should be collecting dirty children from the estimate based on
      // whatever data is going to be sent by this saveEventually, which
      // won't necessarily be the current estimatedData. We should resolve
      // this when the multiple save code is added.
      List<BEObject> unsavedChildren = new ArrayList<>();
      collectDirtyChildren(estimatedData, unsavedChildren, null);

      String localId = null;
      if (getObjectId() == null) {
        localId = getOrCreateLocalId();
      }

      operationSet = startSave();
      operationSet.setIsSaveEventually(true);

      //TODO (grantland): Convert to async
      final String sessionToken = BEUser.getCurrentSessionToken();

      try {
        // See [1]
        command = currentSaveEventuallyCommand(operationSet, PointerOrLocalIdEncoder.get(),
            sessionToken);

        // TODO: Make this logic make sense once we have deepSaveEventually
        command.setLocalId(localId);

        // Mark the command with a UUID so that we can match it up later.
        command.setOperationSetUUID(operationSet.getUUID());

        // Ensure local ids are retained before saveEventually-ing children
        command.retainLocalIds();

        for (BEObject object : unsavedChildren) {
          object.saveEventually();
        }
      } catch (BEException exception) {
        throw new IllegalStateException("Unable to saveEventually.", exception);
      }
    }

    // We cannot modify the taskQueue inside synchronized (mutex).
    BEEventuallyQueue cache = CSBM.getEventuallyQueue();
    runEventuallyTask = cache.enqueueEventuallyAsync(command, BEObject.this);
    enqueueSaveEventuallyOperationAsync(operationSet);

    // Release the extra retained local ids.
    command.releaseLocalIds();

    Task<Void> handleSaveResultTask;
    if (CSBM.isLocalDatastoreEnabled()) {
      // BEPinningEventuallyQueue calls handleSaveEventuallyResultAsync directly.
      handleSaveResultTask = runEventuallyTask.makeVoid();
    } else {
      handleSaveResultTask = runEventuallyTask.onSuccessTask(new Continuation<JSONObject, Task<Void>>() {
        @Override
        public Task<Void> then(Task<JSONObject> task) throws Exception {
          JSONObject json = task.getResult();
          return handleSaveEventuallyResultAsync(json, operationSet);
        }
      });
    }
    return handleSaveResultTask;
  }

  /**
   * Enqueues the saveEventually BEOperationSet in {@link #taskQueue}.
   */
  private Task<Void> enqueueSaveEventuallyOperationAsync(final BEOperationSet operationSet) {
    if (!operationSet.isSaveEventually()) {
      throw new IllegalStateException(
          "This should only be used to enqueue saveEventually operation sets");
    }

    return taskQueue.enqueue(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> toAwait) throws Exception {
        return toAwait.continueWithTask(new Continuation<Void, Task<Void>>() {
          @Override
          public Task<Void> then(Task<Void> task) throws Exception {
            BEEventuallyQueue cache = CSBM.getEventuallyQueue();
            return cache.waitForOperationSetAndEventuallyPin(operationSet, null).makeVoid();
          }
        });
      }
    });
  }

  /**
   * Handles the result of {@code saveEventually}.
   *
   * In addition to normal save handling, this also notifies the saveEventually test helper.
   *
   * Should be called on success or failure.
   */
  /* package */ Task<Void> handleSaveEventuallyResultAsync(
          JSONObject json, BEOperationSet operationSet) {
    final boolean success = json != null;
    Task<Void> handleSaveResultTask = handleSaveResultAsync(json, operationSet);

    return handleSaveResultTask.onSuccessTask(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> task) throws Exception {
        if (success) {
          CSBM.getEventuallyQueue()
              .notifyTestHelper(BECommandCache.TestHelper.OBJECT_UPDATED);
        }
        return task;
      }
    });
  }

  /**
   * Called by {@link #saveInBackground()} and {@link #saveEventually(SaveCallback)}
   * and guaranteed to be thread-safe. Subclasses can override this method to do any custom updates
   * before an object gets saved.
   */
  /* package */ void updateBeforeSave() {
    // do nothing
  }

  /**
   * Deletes this object from the server at some unspecified time in the future, even if CSBM is
   * currently inaccessible. Use this when you may not have a solid network connection, and don't
   * need to know when the delete completes. If there is some problem with the object such that it
   * can't be deleted, the request will be silently discarded. Delete requests made with this method
   * will be stored locally in an on-disk cache until they can be transmitted to CSBM. They will be
   * sent immediately if possible. Otherwise, they will be sent the next time a network connection
   * is available. Delete instructions saved this way will persist even after the app is closed, in
   * which case they will be sent the next time the app is opened. If more than 10MB of commands are
   * waiting to be sent, subsequent calls to {@code #deleteEventually()} or
   * {@link #saveEventually()} will cause old instructions to be silently discarded until the
   * connection can be re-established, and the queued objects can be saved.
   *
   * @param callback
   *          - A callback which will be called if the delete completes before the app exits.
   */
  public final void deleteEventually(DeleteCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(deleteEventually(), callback);
  }

  /**
   * Deletes this object from the server at some unspecified time in the future, even if CSBM is
   * currently inaccessible. Use this when you may not have a solid network connection, and don't
   * need to know when the delete completes. If there is some problem with the object such that it
   * can't be deleted, the request will be silently discarded. Delete requests made with this method
   * will be stored locally in an on-disk cache until they can be transmitted to CSBM. They will be
   * sent immediately if possible. Otherwise, they will be sent the next time a network connection
   * is available. Delete instructions saved this way will persist even after the app is closed, in
   * which case they will be sent the next time the app is opened. If more than 10MB of commands are
   * waiting to be sent, subsequent calls to {@code #deleteEventually()} or
   * {@link #saveEventually()} will cause old instructions to be silently discarded until the
   * connection can be re-established, and the queued objects can be saved.
   *
   * @return A {@link Task} that is resolved when the delete completes.
   */
  public final Task<Void> deleteEventually() {
    final BERESTCommand command;
    final Task<JSONObject> runEventuallyTask;
    synchronized (mutex) {
      validateDelete();
      isDeletingEventually += 1;

      String localId = null;
      if (getObjectId() == null) {
        localId = getOrCreateLocalId();
      }

      // TODO(grantland): Convert to async
      final String sessionToken = BEUser.getCurrentSessionToken();

      // See [1]
      command = BERESTObjectCommand.deleteObjectCommand(
          getState(), sessionToken);
      command.enableRetrying();
      command.setLocalId(localId);

      runEventuallyTask = CSBM.getEventuallyQueue().enqueueEventuallyAsync(command, BEObject.this);
    }

    Task<Void> handleDeleteResultTask;
    if (CSBM.isLocalDatastoreEnabled()) {
      // BEPinningEventuallyQueue calls handleDeleteEventuallyResultAsync directly.
      handleDeleteResultTask = runEventuallyTask.makeVoid();
    } else {
      handleDeleteResultTask = runEventuallyTask.onSuccessTask(new Continuation<JSONObject, Task<Void>>() {
        @Override
        public Task<Void> then(Task<JSONObject> task) throws Exception {
          return handleDeleteEventuallyResultAsync();
        }
      });
    }

    return handleDeleteResultTask;
  }

  /**
   * Handles the result of {@code deleteEventually}.
   *
   * Should only be called on success.
   */
  /* package */ Task<Void> handleDeleteEventuallyResultAsync() {
    synchronized (mutex) {
      isDeletingEventually -= 1;
    }
    Task<Void> handleDeleteResultTask = handleDeleteResultAsync();

    return handleDeleteResultTask.onSuccessTask(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> task) throws Exception {
        CSBM.getEventuallyQueue()
            .notifyTestHelper(BECommandCache.TestHelper.OBJECT_REMOVED);
        return task;
      }
    });
  }

  /**
   * Handles the result of {@code fetch}.
   *
   * Should only be called on success.
   */
  /* package */ Task<Void> handleFetchResultAsync(final BEObject.State result) {
    Task<Void> task = Task.forResult((Void) null);

    /*
     * If this object is in the offline store, then we need to make sure that we pull in any dirty
     * changes it may have before merging the server data into it.
     */
    final OfflineStore store =CSBM.getLocalDatastore();
    if (store != null) {
      task = task.onSuccessTask(new Continuation<Void, Task<Void>>() {
        @Override
        public Task<Void> then(Task<Void> task) throws Exception {
          return store.fetchLocallyAsync(BEObject.this).makeVoid();
        }
      }).continueWithTask(new Continuation<Void, Task<Void>>() {
        @Override
        public Task<Void> then(Task<Void> task) throws Exception {
          // Catch CACHE_MISS
          if (task.getError() instanceof BEException
              && ((BEException)task.getError()).getCode() == BEException.CACHE_MISS) {
            return null;
          }
          return task;
        }
      });
    }

    task = task.onSuccessTask(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> task) throws Exception {
        synchronized (mutex) {
          State newState;
          if (result.isComplete()) {
            // Result is complete, so just replace
            newState = result;
          } else {
            // Result is incomplete, so we'll need to apply it to the current state
            newState = getState().newBuilder().apply(result).build();
          }
          setState(newState);
        }
        return null;
      }
    });

    if (store != null) {
      task = task.onSuccessTask(new Continuation<Void, Task<Void>>() {
        @Override
        public Task<Void> then(Task<Void> task) throws Exception {
          return store.updateDataForObjectAsync(BEObject.this);
        }
      }).continueWithTask(new Continuation<Void, Task<Void>>() {
        @Override
        public Task<Void> then(Task<Void> task) throws Exception {
          // Catch CACHE_MISS
          if (task.getError() instanceof BEException
              && ((BEException) task.getError()).getCode() == BEException.CACHE_MISS) {
            return null;
          }
          return task;
        }
      });
    }

    return task;
  }

  /**
   * Refreshes this object with the data from the server. Call this whenever you want the state of
   * the object to reflect exactly what is on the server.
   *
   * @throws BEException
   *           Throws an exception if the server is inaccessible.
   *
   * @deprecated Please use {@link #fetch()} instead.
   */
  @Deprecated
  public final void refresh() throws BEException {
    fetch();
  }

  /**
   * Refreshes this object with the data from the server in a background thread. This is preferable
   * to using refresh(), unless your code is already running from a background thread.
   *
   * @param callback
   *          {@code callback.done(object, e)} is called when the refresh completes.
   *
   * @deprecated Please use {@link #fetchInBackground(GetCallback)} instead.
   */
  @Deprecated
  public final void refreshInBackground(RefreshCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(fetchInBackground(), callback);
  }

  /**
   * Fetches this object with the data from the server. Call this whenever you want the state of the
   * object to reflect exactly what is on the server.
   *
   * @throws BEException
   *           Throws an exception if the server is inaccessible.
   * @return The {@code BEObject} that was fetched.
   */
  public <T extends BEObject> T fetch() throws BEException {
    return BETaskUtils.wait(this.<T>fetchInBackground());
  }

  @SuppressWarnings("unchecked")
  /* package */ <T extends BEObject> Task<T> fetchAsync(
          final String sessionToken, Task<Void> toAwait) {
    return toAwait.onSuccessTask(new Continuation<Void, Task<State>>() {
      @Override
      public Task<State> then(Task<Void> task) throws Exception {
        State state;
        Map<String, BEObject> fetchedObjects;
        synchronized (mutex) {
          state = getState();
          fetchedObjects = collectFetchedObjects();
        }
        BEDecoder decoder = new KnownBEObjectDecoder(fetchedObjects);
        return getObjectController().fetchAsync(state, sessionToken, decoder);
      }
    }).onSuccessTask(new Continuation<State, Task<Void>>() {
      @Override
      public Task<Void> then(Task<State> task) throws Exception {
        BEObject.State result = task.getResult();
        return handleFetchResultAsync(result);
      }
    }).onSuccess(new Continuation<Void, T>() {
      @Override
      public T then(Task<Void> task) throws Exception {
        return (T) BEObject.this;
      }
    });
  }

  /**
   * Fetches this object with the data from the server in a background thread. This is preferable to
   * using fetch(), unless your code is already running from a background thread.
   *
   * @return A {@link Task} that is resolved when fetch completes.
   */
  public final <T extends BEObject> Task<T> fetchInBackground() {
    return BEUser.getCurrentSessionTokenAsync().onSuccessTask(new Continuation<String, Task<T>>() {
      @Override
      public Task<T> then(Task<String> task) throws Exception {
        final String sessionToken = task.getResult();
        return taskQueue.enqueue(new Continuation<Void, Task<T>>() {
          @Override
          public Task<T> then(Task<Void> toAwait) throws Exception {
            return fetchAsync(sessionToken, toAwait);
          }
        });
      }
    });
  }

  /**
   * Fetches this object with the data from the server in a background thread. This is preferable to
   * using fetch(), unless your code is already running from a background thread.
   *
   * @param callback
   *          {@code callback.done(object, e)} is called when the fetch completes.
   */
  public final <T extends BEObject> void fetchInBackground(GetCallback<T> callback) {
    BETaskUtils.callbackOnMainThreadAsync(this.<T>fetchInBackground(), callback);
  }

  /**
   * If this {@code BEObject} has not been fetched (i.e. {@link #isDataAvailable()} returns {@code false}),
   * fetches this object with the data from the server in a background thread. This is preferable to
   * using {@link #fetchIfNeeded()}, unless your code is already running from a background thread.
   *
   * @return A {@link Task} that is resolved when fetch completes.
   */
  public final <T extends BEObject> Task<T> fetchIfNeededInBackground() {
    if (isDataAvailable()) {
      return Task.forResult((T) this);
    }
    return BEUser.getCurrentSessionTokenAsync().onSuccessTask(new Continuation<String, Task<T>>() {
      @Override
      public Task<T> then(Task<String> task) throws Exception {
        final String sessionToken = task.getResult();
        return taskQueue.enqueue(new Continuation<Void, Task<T>>() {
          @Override
          public Task<T> then(Task<Void> toAwait) throws Exception {
            if (isDataAvailable()) {
              return Task.forResult((T) BEObject.this);
            }
            return fetchAsync(sessionToken, toAwait);
          }
        });
      }
    });

  }

  /**
   * If this {@code BEObject} has not been fetched (i.e. {@link #isDataAvailable()} returns {@code false}),
   * fetches this object with the data from the server.
   *
   * @throws BEException
   *           Throws an exception if the server is inaccessible.
   * @return The fetched {@code BEObject}.
   */
  public <T extends BEObject> T fetchIfNeeded() throws BEException {
    return BETaskUtils.wait(this.<T>fetchIfNeededInBackground());
  }

  /**
   * If this {@code BEObject} has not been fetched (i.e. {@link #isDataAvailable()} returns {@code false}),
   * fetches this object with the data from the server in a background thread. This is preferable to
   * using {@link #fetchIfNeeded()}, unless your code is already running from a background thread.
   *
   * @param callback
   *          {@code callback.done(object, e)} is called when the fetch completes.
   */
  public final <T extends BEObject> void fetchIfNeededInBackground(GetCallback<T> callback) {
    BETaskUtils.callbackOnMainThreadAsync(this.<T>fetchIfNeededInBackground(), callback);
  }

  // Validates the delete method
  /* package */ void validateDelete() {
    // do nothing
  }

  private Task<Void> deleteAsync(final String sessionToken, Task<Void> toAwait) {
    validateDelete();

    return toAwait.onSuccessTask(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> task) throws Exception {
        if (state.objectId() == null) {
          return task.cast(); // no reason to call delete since it doesn't exist
        }
        return deleteAsync(sessionToken);
      }
    }).onSuccessTask(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> task) throws Exception {
        return handleDeleteResultAsync();
      }
    });
  }

  //TODO (grantland): I'm not sure we want direct access to this. All access to `delete` should
  // enqueue on the taskQueue...
  /* package */ Task<Void> deleteAsync(String sessionToken) throws BEException {
    return getObjectController().deleteAsync(getState(), sessionToken);
  }

  /**
   * Handles the result of {@code delete}.
   *
   * Should only be called on success.
   */
  /* package */ Task<Void> handleDeleteResultAsync() {
    Task<Void> task = Task.forResult(null);

    synchronized (mutex) {
      isDeleted = true;
    }

    final OfflineStore store = CSBM.getLocalDatastore();
    if (store != null) {
      task = task.continueWithTask(new Continuation<Void, Task<Void>>() {
        @Override
        public Task<Void> then(Task<Void> task) throws Exception {
          synchronized (mutex) {
            if (isDeleted) {
              store.unregisterObject(BEObject.this);
              return store.deleteDataForObjectAsync(BEObject.this);
            } else {
              return store.updateDataForObjectAsync(BEObject.this);
            }
          }
        }
      });
    }

    return task;
  }

  /**
   * Deletes this object on the server in a background thread. This is preferable to using
   * {@link #delete()}, unless your code is already running from a background thread.
   *
   * @return A {@link Task} that is resolved when delete completes.
   */
  public final Task<Void> deleteInBackground() {
    return BEUser.getCurrentSessionTokenAsync().onSuccessTask(new Continuation<String, Task<Void>>() {
      @Override
      public Task<Void> then(Task<String> task) throws Exception {
        final String sessionToken = task.getResult();
        return taskQueue.enqueue(new Continuation<Void, Task<Void>>() {
          @Override
          public Task<Void> then(Task<Void> toAwait) throws Exception {
            return deleteAsync(sessionToken, toAwait);
          }
        });
      }
    });
  }

  /**
   * Deletes this object on the server. This does not delete or destroy the object locally.
   *
   * @throws BEException
   *           Throws an error if the object does not exist or if the internet fails.
   */
  public final void delete() throws BEException {
    BETaskUtils.wait(deleteInBackground());
  }

  /**
   * Deletes this object on the server in a background thread. This is preferable to using
   * {@link #delete()}, unless your code is already running from a background thread.
   *
   * @param callback
   *          {@code callback.done(e)} is called when the save completes.
   */
  public final void deleteInBackground(DeleteCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(deleteInBackground(), callback);
  }

  /**
   * This deletes all of the objects from the given List.
   */
  private static <T extends BEObject> Task<Void> deleteAllAsync(
          final List<T> objects, final String sessionToken) {
    if (objects.size() == 0) {
      return Task.forResult(null);
    }

    // Create a list of unique objects based on objectIds
    int objectCount = objects.size();
    final List<BEObject> uniqueObjects = new ArrayList<>(objectCount);
    final HashSet<String> idSet = new HashSet<>();
    for (int i = 0; i < objectCount; i++) {
      BEObject obj = objects.get(i);
      if (!idSet.contains(obj.getObjectId())) {
        idSet.add(obj.getObjectId());
        uniqueObjects.add(obj);
      }
    }

    return enqueueForAll(uniqueObjects, new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> toAwait) throws Exception {
        return deleteAllAsync(uniqueObjects, sessionToken, toAwait);
      }
    });
  }

  private static <T extends BEObject> Task<Void> deleteAllAsync(
          final List<T> uniqueObjects, final String sessionToken, Task<Void> toAwait) {
    return toAwait.continueWithTask(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> task) throws Exception {
        int objectCount = uniqueObjects.size();
        List<State> states = new ArrayList<>(objectCount);
        for (int i = 0; i < objectCount; i++) {
          BEObject object = uniqueObjects.get(i);
          object.validateDelete();
          states.add(object.getState());
        }
        List<Task<Void>> batchTasks = getObjectController().deleteAllAsync(states, sessionToken);

        List<Task<Void>> tasks = new ArrayList<>(objectCount);
        for (int i = 0; i < objectCount; i++) {
          Task<Void> batchTask = batchTasks.get(i);
          final T object = uniqueObjects.get(i);
          tasks.add(batchTask.onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(final Task<Void> batchTask) throws Exception {
              return object.handleDeleteResultAsync().continueWithTask(new Continuation<Void, Task<Void>>() {
                @Override
                public Task<Void> then(Task<Void> task) throws Exception {
                  return batchTask;
                }
              });
            }
          }));
        }
        return Task.whenAll(tasks);
      }
    });
  }

  /**
   * Deletes each object in the provided list. This is faster than deleting each object individually
   * because it batches the requests.
   *
   * @param objects
   *          The objects to delete.
   * @throws BEException
   *           Throws an exception if the server returns an error or is inaccessible.
   */
  public static <T extends BEObject> void deleteAll(List<T> objects) throws BEException {
    BETaskUtils.wait(deleteAllInBackground(objects));
  }

  /**
   * Deletes each object in the provided list. This is faster than deleting each object individually
   * because it batches the requests.
   *
   * @param objects
   *          The objects to delete.
   * @param callback
   *          The callback method to execute when completed.
   */
  public static <T extends BEObject> void deleteAllInBackground(List<T> objects, DeleteCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(deleteAllInBackground(objects), callback);
  }

  /**
   * Deletes each object in the provided list. This is faster than deleting each object individually
   * because it batches the requests.
   *
   * @param objects
   *          The objects to delete.
   *
   * @return A {@link Task} that is resolved when deleteAll completes.
   */
  public static <T extends BEObject> Task<Void> deleteAllInBackground(final List<T> objects) {
    return BEUser.getCurrentSessionTokenAsync().onSuccessTask(new Continuation<String, Task<Void>>() {
      @Override
      public Task<Void> then(Task<String> task) throws Exception {
        String sessionToken = task.getResult();
        return deleteAllAsync(objects, sessionToken);
      }
    });
  }

  /**
   * Finds all of the objects that are reachable from child, including child itself, and adds them
   * to the given mutable array. It traverses arrays and json objects.
   *
   * @param node
   *          An kind object to search for children.
   * @param dirtyChildren
   *          The array to collect the {@code BEObject}s into.
   * @param dirtyFiles
   *          The array to collect the {@link BEFile}s into.
   * @param alreadySeen
   *          The set of all objects that have already been seen.
   * @param alreadySeenNew
   *          The set of new objects that have already been seen since the last existing object.
   */
  private static void collectDirtyChildren(Object node,
      final Collection<BEObject> dirtyChildren,
      final Collection<BEFile> dirtyFiles,
      final Set<BEObject> alreadySeen,
      final Set<BEObject> alreadySeenNew) {

    new BETraverser() {
      @Override
      protected boolean visit(Object node) {
        // If it's a file, then add it to the list if it's dirty.
        if (node instanceof BEFile) {
          if (dirtyFiles == null) {
            return true;
          }

          BEFile file = (BEFile) node;
          if (file.getUrl() == null) {
            dirtyFiles.add(file);
          }
          return true;
        }

        // If it's anything other than a file, then just continue;
        if (!(node instanceof BEObject)) {
          return true;
        }

        if (dirtyChildren == null) {
          return true;
        }

        // For files, we need to handle recursion manually to find cycles of new objects.
        BEObject object = (BEObject) node;
        Set<BEObject> seen = alreadySeen;
        Set<BEObject> seenNew = alreadySeenNew;

        // Check for cycles of new objects. Any such cycle means it will be
        // impossible to save this collection of objects, so throw an exception.
        if (object.getObjectId() != null) {
          seenNew = new HashSet<>();
        } else {
          if (seenNew.contains(object)) {
            throw new RuntimeException("Found a circular dependency while saving.");
          }
          seenNew = new HashSet<>(seenNew);
          seenNew.add(object);
        }

        // Check for cycles of any object. If this occurs, then there's no
        // problem, but we shouldn't recurse any deeper, because it would be
        // an infinite recursion.
        if (seen.contains(object)) {
          return true;
        }
        seen = new HashSet<>(seen);
        seen.add(object);

        // Recurse into this object's children looking for dirty children.
        // We only need to look at the child object's current estimated data,
        // because that's the only data that might need to be saved now.
        collectDirtyChildren(object.estimatedData, dirtyChildren, dirtyFiles, seen, seenNew);

        if (object.isDirty(false)) {
          dirtyChildren.add(object);
        }

        return true;
      }
    }.setYieldRoot(true).traverse(node);
  }

  /**
   * Helper version of collectDirtyChildren so that callers don't have to add the internally used
   * parameters.
   */
  private static void collectDirtyChildren(Object node, Collection<BEObject> dirtyChildren,
                                           Collection<BEFile> dirtyFiles) {
    collectDirtyChildren(node, dirtyChildren, dirtyFiles,
        new HashSet<BEObject>(),
        new HashSet<BEObject>());
  }

  /**
   * Returns {@code true} if this object can be serialized for saving.
   */
  private boolean canBeSerialized() {
    synchronized (mutex) {
      final Capture<Boolean> result = new Capture<>(true);

      // This method is only used for batching sets of objects for saveAll
      // and when saving children automatically. Since it's only used to
      // determine whether or not save should be called on them, it only
      // needs to examine their current values, so we use estimatedData.
      new BETraverser() {
        @Override
        protected boolean visit(Object value) {
          if (value instanceof BEFile) {
            BEFile file = (BEFile) value;
            if (file.isDirty()) {
              result.set(false);
            }
          }

          if (value instanceof BEObject) {
            BEObject object = (BEObject) value;
            if (object.getObjectId() == null) {
              result.set(false);
            }
          }

          // Continue to traverse only if it can still be serialized.
          return result.get();
        }
      }.setYieldRoot(false).setTraverseBEObjects(true).traverse(this);

      return result.get();
    }
  }

  /**
   * This saves all of the objects and files reachable from the given object. It does its work in
   * multiple waves, saving as many as possible in each wave. If there's ever an error, it just
   * gives up, sets error, and returns NO.
   */
  private static Task<Void> deepSaveAsync(final Object object, final String sessionToken) {
    Set<BEObject> objects = new HashSet<>();
    Set<BEFile> files = new HashSet<>();
    collectDirtyChildren(object, objects, files);

    // This has to happen separately from everything else because BEUser.save() is
    // special-cased to work for lazy users, but new users can't be created by
    // BEMultiCommand's regular save.
    Set<BEUser> users = new HashSet<>();
    for (BEObject o : objects) {
      if (o instanceof BEUser) {
        BEUser user = (BEUser) o;
        if (user.isLazy()) {
          users.add((BEUser) o);
        }
      }
    }
    objects.removeAll(users);

    // objects will need to wait for files to be complete since they may be nested children.
    final AtomicBoolean filesComplete = new AtomicBoolean(false);
    List<Task<Void>> tasks = new ArrayList<>();
    for (BEFile file : files) {
      tasks.add(file.saveAsync(sessionToken, null, null));
    }
    Task<Void> filesTask = Task.whenAll(tasks).continueWith(new Continuation<Void, Void>() {
      @Override
      public Void then(Task<Void> task) throws Exception {
        filesComplete.set(true);
        return null;
      }
    });

    // objects will need to wait for users to be complete since they may be nested children.
    final AtomicBoolean usersComplete = new AtomicBoolean(false);
    tasks = new ArrayList<>();
    for (final BEUser user : users) {
      tasks.add(user.saveAsync(sessionToken));
    }
    Task<Void> usersTask = Task.whenAll(tasks).continueWith(new Continuation<Void, Void>() {
      @Override
      public Void then(Task<Void> task) throws Exception {
        usersComplete.set(true);
        return null;
      }
    });

    final Capture<Set<BEObject>> remaining = new Capture<>(objects);
    Task<Void> objectsTask = Task.forResult(null).continueWhile(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return remaining.get().size() > 0;
      }
    }, new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> task) throws Exception {
        // Partition the objects into two sets: those that can be save immediately,
        // and those that rely on other objects to be created first.
        final List<BEObject> current = new ArrayList<>();
        final Set<BEObject> nextBatch = new HashSet<>();
        for (BEObject obj : remaining.get()) {
          if (obj.canBeSerialized()) {
            current.add(obj);
          } else {
            nextBatch.add(obj);
          }
        }
        remaining.set(nextBatch);

        if (current.size() == 0 && filesComplete.get() && usersComplete.get()) {
          // We do cycle-detection when building the list of objects passed to this function, so
          // this should never get called. But we should check for it anyway, so that we get an
          // exception instead of an infinite loop.
          throw new RuntimeException("Unable to save a BEObject with a relation to a cycle.");
        }

        // Package all save commands together
        if (current.size() == 0) {
          return Task.forResult(null);
        }

        return enqueueForAll(current, new Continuation<Void, Task<Void>>() {
          @Override
          public Task<Void> then(Task<Void> toAwait) throws Exception {
            return saveAllAsync(current, sessionToken, toAwait);
          }
        });
      }
    });

    return Task.whenAll(Arrays.asList(filesTask, usersTask, objectsTask));
  }

  private static <T extends BEObject> Task<Void> saveAllAsync(
          final List<T> uniqueObjects, final String sessionToken, Task<Void> toAwait) {
    return toAwait.continueWithTask(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> task) throws Exception {
        int objectCount = uniqueObjects.size();
        List<State> states = new ArrayList<>(objectCount);
        List<BEOperationSet> operationsList = new ArrayList<>(objectCount);
        List<BEDecoder> decoders = new ArrayList<>(objectCount);
        for (int i = 0; i < objectCount; i++) {
          BEObject object = uniqueObjects.get(i);
          object.updateBeforeSave();
          object.validateSave();

          states.add(object.getState());
          operationsList.add(object.startSave());
          final Map<String, BEObject> fetchedObjects = object.collectFetchedObjects();
          decoders.add(new KnownBEObjectDecoder(fetchedObjects));
        }
        List<Task<State>> batchTasks = getObjectController().saveAllAsync(
            states, operationsList, sessionToken, decoders);

        List<Task<Void>> tasks = new ArrayList<>(objectCount);
        for (int i = 0; i < objectCount; i++) {
          Task<State> batchTask = batchTasks.get(i);
          final T object = uniqueObjects.get(i);
          final BEOperationSet operations = operationsList.get(i);
          tasks.add(batchTask.continueWithTask(new Continuation<State, Task<Void>>() {
            @Override
            public Task<Void> then(final Task<State> batchTask) throws Exception {
              BEObject.State result = batchTask.getResult(); // will be null on failure
              return object.handleSaveResultAsync(result, operations).continueWithTask(new Continuation<Void, Task<Void>>() {
                @Override
                public Task<Void> then(Task<Void> task) throws Exception {
                  if (task.isFaulted() || task.isCancelled()) {
                    return task;
                  }

                  // We still want to propagate batchTask errors
                  return batchTask.makeVoid();
                }
              });
            }
          }));
        }
        return Task.whenAll(tasks);
      }
    });
  }

  /**
   * Saves each object in the provided list. This is faster than saving each object individually
   * because it batches the requests.
   *
   * @param objects
   *          The objects to save.
   * @throws BEException
   *           Throws an exception if the server returns an error or is inaccessible.
   */
  public static <T extends BEObject> void saveAll(List<T> objects) throws BEException {
    BETaskUtils.wait(saveAllInBackground(objects));
  }

  /**
   * Saves each object in the provided list to the server in a background thread. This is preferable
   * to using saveAll, unless your code is already running from a background thread.
   *
   * @param objects
   *          The objects to save.
   * @param callback
   *          {@code callback.done(e)} is called when the save completes.
   */
  public static <T extends BEObject> void saveAllInBackground(List<T> objects, SaveCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(saveAllInBackground(objects), callback);
  }

  /**
   * Saves each object in the provided list to the server in a background thread. This is preferable
   * to using saveAll, unless your code is already running from a background thread.
   *
   * @param objects
   *          The objects to save.
   *
   * @return A {@link Task} that is resolved when saveAll completes.
   */
  public static <T extends BEObject> Task<Void> saveAllInBackground(final List<T> objects) {
    return BEUser.getCurrentUserAsync().onSuccessTask(new Continuation<BEUser, Task<String>>() {
      @Override
      public Task<String> then(Task<BEUser> task) throws Exception {
        final BEUser current = task.getResult();
        if (current == null) {
          return Task.forResult(null);
        }
        if (!current.isLazy()) {
          return Task.forResult(current.getSessionToken());
        }

        // The current user is lazy/unresolved. If it is attached to any of the objects via ACL,
        // we'll need to resolve/save it before proceeding.
        for (BEObject object : objects) {
          if (!object.isDataAvailable(KEY_ACL)) {
            continue;
          }
          final BEACL acl = object.getACL(false);
          if (acl == null) {
            continue;
          }
          final BEUser user = acl.getUnresolvedUser();
          if (user != null && user.isCurrentUser()) {
            // We only need to find one, since there's only one current user.
            return user.saveAsync(null).onSuccess(new Continuation<Void, String>() {
              @Override
              public String then(Task<Void> task) throws Exception {
                if (acl.hasUnresolvedUser()) {
                  throw new IllegalStateException("ACL has an unresolved BEUser. "
                      + "Save or sign up before attempting to serialize the ACL.");
                }
                return user.getSessionToken();
              }
            });
          }
        }

        // There were no objects with ACLs pointing to unresolved users.
        return Task.forResult(null);
      }
    }).onSuccessTask(new Continuation<String, Task<Void>>() {
      @Override
      public Task<Void> then(Task<String> task) throws Exception {
        final String sessionToken = task.getResult();
        return deepSaveAsync(objects, sessionToken);
      }
    });
  }

  /**
   * Fetches all the objects that don't have data in the provided list in the background.
   *
   * @param objects
   *          The list of objects to fetch.
   *
   * @return A {@link Task} that is resolved when fetchAllIfNeeded completes.
   */
  public static <T extends BEObject> Task<List<T>> fetchAllIfNeededInBackground(
      final List<T> objects) {
    return fetchAllAsync(objects, true);
  }

  /**
   * Fetches all the objects that don't have data in the provided list.
   *
   * @param objects
   *          The list of objects to fetch.
   * @return The list passed in for convenience.
   * @throws BEException
   *           Throws an exception if the server returns an error or is inaccessible.
   */
  public static <T extends BEObject> List<T> fetchAllIfNeeded(List<T> objects)
      throws BEException {
    return BETaskUtils.wait(fetchAllIfNeededInBackground(objects));
  }

  /**
   * Fetches all the objects that don't have data in the provided list in the background.
   *
   * @param objects
   *          The list of objects to fetch.
   * @param callback
   *          {@code callback.done(result, e)} is called when the fetch completes.
   */
  public static <T extends BEObject> void fetchAllIfNeededInBackground(final List<T> objects,
                                                                          FindCallback<T> callback) {
    BETaskUtils.callbackOnMainThreadAsync(fetchAllIfNeededInBackground(objects), callback);
  }

  private static <T extends BEObject> Task<List<T>> fetchAllAsync(
          final List<T> objects, final boolean onlyIfNeeded) {
    return BEUser.getCurrentUserAsync().onSuccessTask(new Continuation<BEUser, Task<List<T>>>() {
      @Override
      public Task<List<T>> then(Task<BEUser> task) throws Exception {
        final BEUser user = task.getResult();
        return enqueueForAll(objects, new Continuation<Void, Task<List<T>>>() {
          @Override
          public Task<List<T>> then(Task<Void> task) throws Exception {
            return fetchAllAsync(objects, user, onlyIfNeeded, task);
          }
        });
      }
    });
  }

  /**
   * @param onlyIfNeeded If enabled, will only fetch if the object has an objectId and
   *                     !isDataAvailable, otherwise it requires objectIds and will fetch regardless
   *                     of data availability.
   */
  // TODO(grantland): Convert to BEUser.State
  private static <T extends BEObject> Task<List<T>> fetchAllAsync(
          final List<T> objects, final BEUser user, final boolean onlyIfNeeded, Task<Void> toAwait) {
    if (objects.size() == 0) {
      return Task.forResult(objects);
    }

    List<String> objectIds = new ArrayList<>();
    String className = null;
    for (T object : objects) {
      if (onlyIfNeeded && object.isDataAvailable()) {
        continue;
      }

      if (className != null && !object.getClassName().equals(className)) {
        throw new IllegalArgumentException("All objects should have the same class");
      }
      className = object.getClassName();

      String objectId = object.getObjectId();
      if (objectId != null) {
        objectIds.add(object.getObjectId());
      } else if (!onlyIfNeeded) {
        throw new IllegalArgumentException("All objects must exist on the server");
      }
    }

    if (objectIds.size() == 0) {
      return Task.forResult(objects);
    }

    final BEQuery<T> query = BEQuery.<T>getQuery(className)
        .whereContainedIn(KEY_OBJECT_ID, objectIds);
    return toAwait.continueWithTask(new Continuation<Void, Task<List<T>>>() {
      @Override
      public Task<List<T>> then(Task<Void> task) throws Exception {
        return query.findAsync(query.getBuilder().build(), user, null);
      }
    }).onSuccess(new Continuation<List<T>, List<T>>() {
      @Override
      public List<T> then(Task<List<T>> task) throws Exception {
        Map<String, T> resultMap = new HashMap<>();
        for (T o : task.getResult()) {
          resultMap.put(o.getObjectId(), o);
        }
        for (T object : objects) {
          if (onlyIfNeeded && object.isDataAvailable()) {
            continue;
          }

          T newObject = resultMap.get(object.getObjectId());
          if (newObject == null) {
            throw new BEException(
                BEException.OBJECT_NOT_FOUND,
                "Object id " + object.getObjectId() + " does not exist");
          }
          if (!CSBM.isLocalDatastoreEnabled()) {
            // We only need to merge if LDS is disabled, since single instance will do the merging
            // for us.
            object.mergeFromObject(newObject);
          }
        }
        return objects;
      }
    });
  }

  /**
   * Fetches all the objects in the provided list in the background.
   *
   * @param objects
   *          The list of objects to fetch.
   *
   * @return A {@link Task} that is resolved when fetch completes.
   */
  public static <T extends BEObject> Task<List<T>> fetchAllInBackground(final List<T> objects) {
    return fetchAllAsync(objects, false);
  }

  /**
   * Fetches all the objects in the provided list.
   *
   * @param objects
   *          The list of objects to fetch.
   * @return The list passed in.
   * @throws BEException
   *           Throws an exception if the server returns an error or is inaccessible.
   */
  public static <T extends BEObject> List<T> fetchAll(List<T> objects) throws BEException {
    return BETaskUtils.wait(fetchAllInBackground(objects));
  }

  /**
   * Fetches all the objects in the provided list in the background.
   *
   * @param objects
   *          The list of objects to fetch.
   * @param callback
   *          {@code callback.done(result, e)} is called when the fetch completes.
   */
  public static <T extends BEObject> void fetchAllInBackground(List<T> objects,
                                                                  FindCallback<T> callback) {
    BETaskUtils.callbackOnMainThreadAsync(fetchAllInBackground(objects), callback);
  }

  /**
   * Return the operations that will be sent in the next call to save.
   */
  private BEOperationSet currentOperations() {
    synchronized (mutex) {
      return operationSetQueue.getLast();
    }
  }

  /**
   * Updates the estimated values in the map based on the given set of BEFieldOperations.
   */
  private void applyOperations(BEOperationSet operations, Map<String, Object> map) {
    for (String key : operations.keySet()) {
      BEFieldOperation operation = operations.get(key);
      Object oldValue = map.get(key);
      Object newValue = operation.apply(oldValue, key);
      if (newValue != null) {
        map.put(key, newValue);
      } else {
        map.remove(key);
      }
    }
  }

  /**
   * Regenerates the estimatedData map from the serverData and operations.
   */
  private void rebuildEstimatedData() {
    synchronized (mutex) {
      estimatedData.clear();
      for (String key : state.keySet()) {
        estimatedData.put(key, state.get(key));
      }
      for (BEOperationSet operations : operationSetQueue) {
        applyOperations(operations, estimatedData);
      }
    }
  }

  /**
   * performOperation() is like {@link #put(String, Object)} but instead of just taking a new value,
   * it takes a BEFieldOperation that modifies the value.
   */
  /* package */ void performOperation(String key, BEFieldOperation operation) {
    synchronized (mutex) {
      Object oldValue = estimatedData.get(key);
      Object newValue = operation.apply(oldValue, key);
      if (newValue != null) {
        estimatedData.put(key, newValue);
      } else {
        estimatedData.remove(key);
      }

      BEFieldOperation oldOperation = currentOperations().get(key);
      BEFieldOperation newOperation = operation.mergeWithPrevious(oldOperation);
      currentOperations().put(key, newOperation);
    }
  }

  /**
   * Add a key-value pair to this object. It is recommended to name keys in
   * <code>camelCaseLikeThis</code>.
   *
   * @param key
   *          Keys must be alphanumerical plus underscore, and start with a letter.
   * @param value
   *          Values may be numerical, {@link String}, {@link JSONObject}, {@link JSONArray},
   *          {@link JSONObject#NULL}, or other {@code BEObject}s. value may not be {@code null}.
   */
  public void put(String key, Object value) {
    checkKeyIsMutable(key);

    performPut(key, value);
  }

  /* package */ void performPut(String key, Object value) {
    if (key == null) {
      throw new IllegalArgumentException("key may not be null.");
    }

    if (value == null) {
      throw new IllegalArgumentException("value may not be null.");
    }

    if (value instanceof JSONObject) {
      BEDecoder decoder = BEDecoder.get();
      value = decoder.convertJSONObjectToMap((JSONObject) value);
    } else if (value instanceof JSONArray){
      BEDecoder decoder = BEDecoder.get();
      value = decoder.convertJSONArrayToList((JSONArray) value);
    }

    if (!BEEncoder.isValidType(value)) {
      throw new IllegalArgumentException("invalid type for value: " + value.getClass().toString());
    }

    performOperation(key, new BESetOperation(value));
  }

  /**
   * Whether this object has a particular key. Same as {@link #containsKey(String)}.
   *
   * @param key
   *          The key to check for
   * @return Whether this object contains the key
   */
  public boolean has(String key) {
    return containsKey(key);
  }

  /**
   * Atomically increments the given key by 1.
   *
   * @param key
   *          The key to increment.
   */
  public void increment(String key) {
    increment(key, 1);
  }

  /**
   * Atomically increments the given key by the given number.
   *
   * @param key
   *          The key to increment.
   * @param amount
   *          The amount to increment by.
   */
  public void increment(String key, Number amount) {
    BEIncrementOperation operation = new BEIncrementOperation(amount);
    performOperation(key, operation);
  }

  /**
   * Atomically adds an object to the end of the array associated with a given key.
   *
   * @param key
   *          The key.
   * @param value
   *          The object to add.
   */
  public void add(String key, Object value) {
    this.addAll(key, Arrays.asList(value));
  }

  /**
   * Atomically adds the objects contained in a {@code Collection} to the end of the array
   * associated with a given key.
   *
   * @param key
   *          The key.
   * @param values
   *          The objects to add.
   */
  public void addAll(String key, Collection<?> values) {
    BEAddOperation operation = new BEAddOperation(values);
    performOperation(key, operation);
  }

  /**
   * Atomically adds an object to the array associated with a given key, only if it is not already
   * present in the array. The position of the insert is not guaranteed.
   *
   * @param key
   *          The key.
   * @param value
   *          The object to add.
   */
  public void addUnique(String key, Object value) {
    this.addAllUnique(key, Arrays.asList(value));
  }

  /**
   * Atomically adds the objects contained in a {@code Collection} to the array associated with a
   * given key, only adding elements which are not already present in the array. The position of the
   * insert is not guaranteed.
   *
   * @param key
   *          The key.
   * @param values
   *          The objects to add.
   */
  public void addAllUnique(String key, Collection<?> values) {
    BEAddUniqueOperation operation = new BEAddUniqueOperation(values);
    performOperation(key, operation);
  }

  /**
   * Removes a key from this object's data if it exists.
   *
   * @param key
   *          The key to remove.
   */
  public void remove(String key) {
    checkKeyIsMutable(key);

    performRemove(key);
  }

  /* package */ void performRemove(String key) {
    synchronized (mutex) {
      Object object = get(key);

      if (object != null) {
        performOperation(key, BEDeleteOperation.getInstance());
      }
    }
  }

  /**
   * Atomically removes all instances of the objects contained in a {@code Collection} from the
   * array associated with a given key. To maintain consistency with the Java Collection API, there
   * is no method removing all instances of a single object. Instead, you can call
   * {@code beObject.removeAll(key, Arrays.asList(value))}.
   *
   * @param key
   *          The key.
   * @param values
   *          The objects to remove.
   */
  public void removeAll(String key, Collection<?> values) {
    checkKeyIsMutable(key);

    BERemoveOperation operation = new BERemoveOperation(values);
    performOperation(key, operation);
  }

  private void checkKeyIsMutable(String key) {
    if (!isKeyMutable(key)) {
      throw new IllegalArgumentException("Cannot modify `" + key
          + "` property of an " + getClassName() + " object.");
    }
  }

  /* package */ boolean isKeyMutable(String key) {
    return true;
  }

  /**
   * Whether this object has a particular key. Same as {@link #has(String)}.
   *
   * @param key
   *          The key to check for
   * @return Whether this object contains the key
   */
  public boolean containsKey(String key) {
    synchronized (mutex) {
      return estimatedData.containsKey(key);
    }
  }

  /**
   * Access a {@link String} value.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code null} if there is no such key or if it is not a {@link String}.
   */
  public String getString(String key) {
    synchronized (mutex) {
      checkGetAccess(key);
      Object value = estimatedData.get(key);
      if (!(value instanceof String)) {
        return null;
      }
      return (String) value;
    }
  }

  /**
   * Access a {@code byte[]} value.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code null} if there is no such key or if it is not a {@code byte[]}.
   */
  public byte[] getBytes(String key) {
    synchronized (mutex) {
      checkGetAccess(key);
      Object value = estimatedData.get(key);
      if (!(value instanceof byte[])) {
        return null;
      }

      return (byte[]) value;
    }
  }

  /**
   * Access a {@link Number} value.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code null} if there is no such key or if it is not a {@link Number}.
   */
  public Number getNumber(String key) {
    synchronized (mutex) {
      checkGetAccess(key);
      Object value = estimatedData.get(key);
      if (!(value instanceof Number)) {
        return null;
      }
      return (Number) value;
    }
  }

  /**
   * Access a {@link JSONArray} value.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code null} if there is no such key or if it is not a {@link JSONArray}.
   */
  public JSONArray getJSONArray(String key) {
    synchronized (mutex) {
      checkGetAccess(key);
      Object value = estimatedData.get(key);

      if (value instanceof List) {
        value = PointerOrLocalIdEncoder.get().encode(value);
      }

      if (!(value instanceof JSONArray)) {
        return null;
      }
      return (JSONArray) value;
    }
  }

  /**
   * Access a {@link List} value.
   *
   * @param key
   *          The key to access the value for
   * @return {@code null} if there is no such key or if the value can't be converted to a
   *          {@link List}.
   */
  public <T> List<T> getList(String key) {
    synchronized (mutex) {
      Object value = estimatedData.get(key);
      if (!(value instanceof List)) {
        return null;
      }
      @SuppressWarnings("unchecked")
      List<T> returnValue = (List<T>) value;
      return returnValue;
    }
  }

  /**
   * Access a {@link Map} value
   *
   * @param key
   *          The key to access the value for
   * @return {@code null} if there is no such key or if the value can't be converted to a
   *          {@link Map}.
   */
  public <V> Map<String, V> getMap(String key) {
    synchronized (mutex) {
      Object value = estimatedData.get(key);
      if (!(value instanceof Map)) {
        return null;
      }
      @SuppressWarnings("unchecked")
      Map<String, V> returnValue = (Map<String, V>) value;
      return returnValue;
    }
  }

  /**
   * Access a {@link JSONObject} value.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code null} if there is no such key or if it is not a {@link JSONObject}.
   */
  public JSONObject getJSONObject(String key) {
    synchronized (mutex) {
      checkGetAccess(key);
      Object value = estimatedData.get(key);

      if (value instanceof Map) {
        value = PointerOrLocalIdEncoder.get().encode(value);
      }

      if (!(value instanceof JSONObject)) {
        return null;
      }

      return (JSONObject) value;
    }
  }

  /**
   * Access an {@code int} value.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code 0} if there is no such key or if it is not a {@code int}.
   */
  public int getInt(String key) {
    Number number = getNumber(key);
    if (number == null) {
      return 0;
    }
    return number.intValue();
  }

  /**
   * Access a {@code double} value.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code 0} if there is no such key or if it is not a {@code double}.
   */
  public double getDouble(String key) {
    Number number = getNumber(key);
    if (number == null) {
      return 0;
    }
    return number.doubleValue();
  }

  /**
   * Access a {@code long} value.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code 0} if there is no such key or if it is not a {@code long}.
   */
  public long getLong(String key) {
    Number number = getNumber(key);
    if (number == null) {
      return 0;
    }
    return number.longValue();
  }

  /**
   * Access a {@code boolean} value.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code false} if there is no such key or if it is not a {@code boolean}.
   */
  public boolean getBoolean(String key) {
    synchronized (mutex) {
      checkGetAccess(key);
      Object value = estimatedData.get(key);
      if (!(value instanceof Boolean)) {
        return false;
      }
      return (Boolean) value;
    }
  }

  /**
   * Access a {@link Date} value.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code null} if there is no such key or if it is not a {@link Date}.
   */
  public Date getDate(String key) {
    synchronized (mutex) {
      checkGetAccess(key);
      Object value = estimatedData.get(key);
      if (!(value instanceof Date)) {
        return null;
      }
      return (Date) value;
    }
  }

  /**
   * Access a {@code BEObject} value. This function will not perform a network request. Unless the
   * {@code BEObject} has been downloaded (e.g. by a {@link BEQuery#include(String)} or by calling
   * {@link #fetchIfNeeded()} or {@link #refresh()}), {@link #isDataAvailable()} will return
   * {@code false}.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code null} if there is no such key or if it is not a {@codeBEObject}.
   */
  public BEObject getBEObject(String key) {
    Object value = get(key);
    if (!(value instanceof BEObject)) {
      return null;
    }
    return (BEObject) value;
  }

  /**
   * Access a {@link BEUser} value. This function will not perform a network request. Unless the
   * {@code BEObject} has been downloaded (e.g. by a {@link BEQuery#include(String)} or by calling
   * {@link #fetchIfNeeded()} or {@link #refresh()}), {@link #isDataAvailable()} will return
   * {@code false}.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code null} if there is no such key or if the value is not a {@link BEUser}.
   */
  public BEUser getBEUser(String key) {
    Object value = get(key);
    if (!(value instanceof BEUser)) {
      return null;
    }
    return (BEUser) value;
  }

  /**
   * Access a {@link BEFile} value. This function will not perform a network request. Unless the
   * {@link BEFile} has been downloaded (e.g. by calling {@link BEFile#getData()}),
   * {@link BEFile#isDataAvailable()} will return {@code false}.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code null} if there is no such key or if it is not a {@link BEFile}.
   */
  public BEFile getBEFile(String key) {
    Object value = get(key);
    if (!(value instanceof BEFile)) {
      return null;
    }
    return (BEFile) value;
  }

  /**
   * Access a {@link BEGeoPoint} value.
   *
   * @param key
   *          The key to access the value for
   * @return {@code null} if there is no such key or if it is not a {@link BEGeoPoint}.
   */
  public BEGeoPoint getBEGeoPoint(String key) {
    synchronized (mutex) {
      checkGetAccess(key);
      Object value = estimatedData.get(key);
      if (!(value instanceof BEGeoPoint)) {
        return null;
      }
      return (BEGeoPoint) value;
    }
  }

  /**
   * Access the {@link BEACL} governing this object.
   */
  public BEACL getACL() {
    return getACL(true);
  }

  private BEACL getACL(boolean mayCopy) {
    synchronized (mutex) {
      checkGetAccess(KEY_ACL);
      Object acl = estimatedData.get(KEY_ACL);
      if (acl == null) {
        return null;
      }
      if (!(acl instanceof BEACL)) {
        throw new RuntimeException("only ACLs can be stored in the ACL key");
      }
      if (mayCopy && ((BEACL) acl).isShared()) {
        BEACL copy = new BEACL((BEACL) acl);
        estimatedData.put(KEY_ACL, copy);
        return copy;
      }
      return (BEACL) acl;
    }
  }

  /**
   * Set the {@link BEACL} governing this object.
   */
  public void setACL(BEACL acl) {
    put(KEY_ACL, acl);
  }

  /**
   * Gets whether the {@code BEObject} has been fetched.
   *
   * @return {@code true} if the {@code BEObject} is new or has been fetched or refreshed. {@code false}
   *         otherwise.
   */
  public boolean isDataAvailable() {
    synchronized (mutex) {
      return state.isComplete();
    }
  }

  /* package for tests */ boolean isDataAvailable(String key) {
    synchronized (mutex) {
      return isDataAvailable() || estimatedData.containsKey(key);
    }
  }

  /**
   * Access or create a {@link BERelation} value for a key
   *
   * @param key
   *          The key to access the relation for.
   * @return the BERelation object if the relation already exists for the key or can be created
   *         for this key.
   */
  public <T extends BEObject> BERelation<T> getRelation(String key) {
    synchronized (mutex) {
      // All the sanity checking is done when add or remove is called on the relation.
      Object value = estimatedData.get(key);
      if (value instanceof BERelation) {
        @SuppressWarnings("unchecked")
        BERelation<T> relation = (BERelation<T>) value;
        relation.ensureParentAndKey(this, key);
        return relation;
      } else {
        BERelation<T> relation = new BERelation<>(this, key);
        /*
         * We put the relation into the estimated data so that we'll get the same instance later,
         * which may have known objects cached. If we rebuildEstimatedData, then this relation will
         * be lost, and we'll get a new one. That's okay, because any cached objects it knows about
         * must be replayable from the operations in the queue. If there were any objects in this
         * relation that weren't still in the queue, then they would be in the copy of the
         * BERelation that's in the serverData, so we would have gotten that instance instead.
         */
        estimatedData.put(key, relation);
        return relation;
      }
    }
  }

  /**
   * Access a value. In most cases it is more convenient to use a helper function such as
   * {@link #getString(String)} or {@link #getInt(String)}.
   *
   * @param key
   *          The key to access the value for.
   * @return {@code null} if there is no such key.
   */
  public Object get(String key) {
    synchronized (mutex) {
      if (key.equals(KEY_ACL)) {
        return getACL();
      }

      checkGetAccess(key);
      Object value = estimatedData.get(key);

      // A relation may be deserialized without a parent or key.
      // Either way, make sure it's consistent.
      if (value instanceof BERelation) {
        ((BERelation<?>) value).ensureParentAndKey(this, key);
      }

      return value;
    }
  }

  private void checkGetAccess(String key) {
    if (!isDataAvailable(key)) {
      throw new IllegalStateException(
          "BEObject has no data for '" + key + "'. Call fetchIfNeeded() to get the data.");
    }
  }

  public boolean hasSameId(BEObject other) {
    synchronized (mutex) {
      return this.getClassName() != null && this.getObjectId() != null
          && this.getClassName().equals(other.getClassName())
          && this.getObjectId().equals(other.getObjectId());
    }
  }

  /* package */ void registerSaveListener(GetCallback<BEObject> callback) {
    synchronized (mutex) {
      saveEvent.subscribe(callback);
    }
  }

  /* package */ void unregisterSaveListener(GetCallback<BEObject> callback) {
    synchronized (mutex) {
      saveEvent.unsubscribe(callback);
    }
  }

  /**
   * Called when a non-pointer is being created to allow additional initialization to occur.
   */
  void setDefaultValues() {
    if (needsDefaultACL() && BEACL.getDefaultACL() != null) {
      this.setACL(BEACL.getDefaultACL());
    }
  }

  /**
   * Determines whether this object should get a default ACL. Override in subclasses to turn off
   * default ACLs.
   */
  boolean needsDefaultACL() {
    return true;
  }

  /**
   * Registers the BE-provided {@code BEObject} subclasses. Do this here in a real method rather than
   * as part of a static initializer because doing this in a static initializer can lead to
   * deadlocks: https://our.intern.facebook.com/intern/tasks/?t=3508472
   */
  /* package */ static void registerBESubclasses() {
    registerSubclass(BEUser.class);
    registerSubclass(BERole.class);
    registerSubclass(BEInstallation.class);
    registerSubclass(BESession.class);

    registerSubclass(BEPin.class);
    registerSubclass(EventuallyPin.class);
  }

  /* package */ static void unregisterBESubclasses() {
    unregisterSubclass(BEUser.class);
    unregisterSubclass(BERole.class);
    unregisterSubclass(BEInstallation.class);
    unregisterSubclass(BESession.class);

    unregisterSubclass(BEPin.class);
    unregisterSubclass(EventuallyPin.class);
  }

  /**
   * Default name for pinning if not specified.
   *
   * @see #pin()
   * @see #unpin()
   */
  public static final String DEFAULT_PIN = "_default";

  /**
   * Stores the objects and every object they point to in the local datastore, recursively. If
   * those other objects have not been fetched from BE, they will not be stored. However, if they
   * have changed data, all of the changes will be retained. To get the objects back later, you can
   * use {@link BEQuery#fromLocalDatastore()}, or you can create an unfetched pointer with
   * {@link #createWithoutData(Class, String)} and then call {@link #fetchFromLocalDatastore()} on it.
   *
   * @see #unpinAllInBackground(String, List, DeleteCallback)
   *
   * @param name
   *          the name
   * @param objects
   *          the objects to be pinned
   * @param callback
   *          the callback
   */
  public static <T extends BEObject> void pinAllInBackground(String name,
                                                                List<T> objects, SaveCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(pinAllInBackground(name, objects), callback);
  }

  /**
   * Stores the objects and every object they point to in the local datastore, recursively. If
   * those other objects have not been fetched from BE, they will not be stored. However, if they
   * have changed data, all of the changes will be retained. To get the objects back later, you can
   * use {@link BEQuery#fromLocalDatastore()}, or you can create an unfetched pointer with
   * {@link #createWithoutData(Class, String)} and then call {@link #fetchFromLocalDatastore()} on it.
   *
   * @see #unpinAllInBackground(String, List)
   *
   * @param name
   *          the name
   * @param objects
   *          the objects to be pinned
   *
   * @return A {@link Task} that is resolved when pinning all completes.
   */
  public static <T extends BEObject> Task<Void> pinAllInBackground(final String name,
                                                                      final List<T> objects) {
    return pinAllInBackground(name, objects, true);
  }

  private static <T extends BEObject> Task<Void> pinAllInBackground(final String name,
                                                                       final List<T> objects, final boolean includeAllChildren) {
    if (!CSBM.isLocalDatastoreEnabled()) {
      throw new IllegalStateException("Method requires Local Datastore. " +
          "Please refer to `BE#enableLocalDatastore(Context)`.");
    }

    Task<Void> task = Task.forResult(null);

    // Resolve and persist unresolved users attached via ACL, similarly how we do in saveAsync
    for (final BEObject object : objects) {
      task = task.onSuccessTask(new Continuation<Void, Task<Void>>() {
        @Override
        public Task<Void> then(Task<Void> task) throws Exception {
          if (!object.isDataAvailable(KEY_ACL)) {
            return Task.forResult(null);
          }

          final BEACL acl = object.getACL(false);
          if (acl == null) {
            return Task.forResult(null);
          }

          BEUser user = acl.getUnresolvedUser();
          if (user == null || !user.isCurrentUser()) {
            return Task.forResult(null);
          }

          return BEUser.pinCurrentUserIfNeededAsync(user);
        }
      });
    }

    return task.onSuccessTask(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> task) throws Exception {
        return CSBM.getLocalDatastore().pinAllObjectsAsync(
            name != null ? name : DEFAULT_PIN,
            objects,
            includeAllChildren);
      }
    }).onSuccessTask(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> task) throws Exception {
        // Hack to emulate persisting current user on disk after a save like in BEUser#saveAsync
        // Note: This does not persist current user if it's a child object of `objects`, it probably
        // should, but we can't unless we do something similar to #deepSaveAsync.
        if (BECorePlugins.PIN_CURRENT_USER.equals(name)) {
          return task;
        }
        for (BEObject object : objects) {
          if (object instanceof BEUser) {
            final BEUser user = (BEUser) object;
            if (user.isCurrentUser()) {
              return BEUser.pinCurrentUserIfNeededAsync(user);
            }
          }
        }
        return task;
      }
    });
  }

  /**
   * Stores the objects and every object they point to in the local datastore, recursively. If
   * those other objects have not been fetched from BE, they will not be stored. However, if they
   * have changed data, all of the changes will be retained. To get the objects back later, you can
   * use {@link BEQuery#fromLocalDatastore()}, or you can create an unfetched pointer with
   * {@link #createWithoutData(Class, String)} and then call {@link #fetchFromLocalDatastore()} on it.
   * {@link #fetchFromLocalDatastore()} on it.
   *
   * @see #unpinAll(String, List)
   *
   * @param name
   *          the name
   * @param objects
   *          the objects to be pinned
   *
   * @throws BEException
   */
  public static <T extends BEObject> void pinAll(String name,
                                                    List<T> objects) throws BEException {
    BETaskUtils.wait(pinAllInBackground(name, objects));
  }

  /**
   * Stores the objects and every object they point to in the local datastore, recursively. If
   * those other objects have not been fetched from BE, they will not be stored. However, if they
   * have changed data, all of the changes will be retained. To get the objects back later, you can
   * use {@link BEQuery#fromLocalDatastore()}, or you can create an unfetched pointer with
   * {@link #createWithoutData(Class, String)} and then call {@link #fetchFromLocalDatastore()} on it.
   *
   * @see #unpinAllInBackground(List, DeleteCallback)
   * @see #DEFAULT_PIN
   *
   * @param objects
   *          the objects to be pinned
   * @param callback
   *          the callback
   */
  public static <T extends BEObject> void pinAllInBackground(List<T> objects,
                                                                SaveCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(pinAllInBackground(DEFAULT_PIN, objects), callback);
  }

  /**
   * Stores the objects and every object they point to in the local datastore, recursively. If
   * those other objects have not been fetched from BE, they will not be stored. However, if they
   * have changed data, all of the changes will be retained. To get the objects back later, you can
   * use {@link BEQuery#fromLocalDatastore()}, or you can create an unfetched pointer with
   * {@link #createWithoutData(Class, String)} and then call {@link #fetchFromLocalDatastore()} on it.
   *
   * @see #unpinAllInBackground(List)
   * @see #DEFAULT_PIN
   *
   * @param objects
   *          the objects to be pinned
   *
   * @return A {@link Task} that is resolved when pinning all completes.
   */
  public static <T extends BEObject> Task<Void> pinAllInBackground(List<T> objects) {
    return pinAllInBackground(DEFAULT_PIN, objects);
  }

  /**
   * Stores the objects and every object they point to in the local datastore, recursively. If
   * those other objects have not been fetched from CSBM, they will not be stored. However, if they
   * have changed data, all of the changes will be retained. To get the objects back later, you can
   * use {@link BEQuery#fromLocalDatastore()}, or you can create an unfetched pointer with
   * {@link #createWithoutData(Class, String)} and then call {@link #fetchFromLocalDatastore()} on it.
   *
   * @see #unpinAll(List)
   * @see #DEFAULT_PIN
   *
   * @param objects
   *          the objects to be pinned
   * @throws BEException
   */
  public static <T extends BEObject> void pinAll(List<T> objects) throws BEException {
    BETaskUtils.wait(pinAllInBackground(DEFAULT_PIN, objects));
  }

  /**
   * Removes the objects and every object they point to in the local datastore, recursively.
   *
   * @see #pinAllInBackground(String, List, SaveCallback)
   *
   * @param name
   *          the name
   * @param objects
   *          the objects
   * @param callback
   *          the callback
   */
  public static <T extends BEObject> void unpinAllInBackground(String name, List<T> objects,
                                                                  DeleteCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(unpinAllInBackground(name, objects), callback);
  }

  /**
   * Removes the objects and every object they point to in the local datastore, recursively.
   *
   * @see #pinAllInBackground(String, List)
   *
   * @param name
   *          the name
   * @param objects
   *          the objects
   *
   * @return A {@link Task} that is resolved when unpinning all completes.
   */
  public static <T extends BEObject> Task<Void> unpinAllInBackground(String name,
                                                                        List<T> objects) {
    if (!CSBM.isLocalDatastoreEnabled()) {
      throw new IllegalStateException("Method requires Local Datastore. " +
          "Please refer to `BE#enableLocalDatastore(Context)`.");
    }
    if (name == null) {
      name = DEFAULT_PIN;
    }
    return CSBM.getLocalDatastore().unpinAllObjectsAsync(name, objects);
  }

  /**
   * Removes the objects and every object they point to in the local datastore, recursively.
   *
   * @see #pinAll(String, List)
   *
   * @param name
   *          the name
   * @param objects
   *          the objects
   *
   * @throws BEException
   */
  public static <T extends BEObject> void unpinAll(String name,
                                                      List<T> objects) throws BEException {
    BETaskUtils.wait(unpinAllInBackground(name, objects));
  }

  /**
   * Removes the objects and every object they point to in the local datastore, recursively.
   *
   * @see #pinAllInBackground(List, SaveCallback)
   * @see #DEFAULT_PIN
   *
   * @param objects
   *          the objects
   * @param callback
   *          the callback
   */
  public static <T extends BEObject> void unpinAllInBackground(List<T> objects,
                                                                  DeleteCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(unpinAllInBackground(DEFAULT_PIN, objects), callback);
  }

  /**
   * Removes the objects and every object they point to in the local datastore, recursively.
   *
   * @see #pinAllInBackground(List)
   * @see #DEFAULT_PIN
   *
   * @param objects
   *          the objects
   *
   * @return A {@link Task} that is resolved when unpinning all completes.
   */
  public static <T extends BEObject> Task<Void> unpinAllInBackground(List<T> objects) {
    return unpinAllInBackground(DEFAULT_PIN, objects);
  }

  /**
   * Removes the objects and every object they point to in the local datastore, recursively.
   *
   * @see #pinAll(List)
   * @see #DEFAULT_PIN
   *
   * @param objects
   *          the objects
   *
   * @throws BEException
   */
  public static <T extends BEObject> void unpinAll(List<T> objects) throws BEException {
    BETaskUtils.wait(unpinAllInBackground(DEFAULT_PIN, objects));
  }

  /**
   * Removes the objects and every object they point to in the local datastore, recursively.
   *
   * @see #pinAll(String, List)
   *
   * @param name
   *          the name
   * @param callback
   *          the callback
   */
  public static void unpinAllInBackground(String name, DeleteCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(unpinAllInBackground(name), callback);
  }

  /**
   * Removes the objects and every object they point to in the local datastore, recursively.
   *
   * @see #pinAll(String, List)
   *
   * @param name
   *          the name
   *
   * @return A {@link Task} that is resolved when unpinning all completes.
   */
  public static Task<Void> unpinAllInBackground(String name) {
    if (!CSBM.isLocalDatastoreEnabled()) {
      throw new IllegalStateException("Method requires Local Datastore. " +
          "Please refer to `BE#enableLocalDatastore(Context)`.");
    }
    if (name == null) {
      name = DEFAULT_PIN;
    }
    return CSBM.getLocalDatastore().unpinAllObjectsAsync(name);
  }

  /**
   * Removes the objects and every object they point to in the local datastore, recursively.
   *
   * @see #pinAll(String, List)
   *
   * @param name
   *          the name
   *
   * @throws BEException
   */
  public static void unpinAll(String name) throws BEException {
    BETaskUtils.wait(unpinAllInBackground(name));
  }

  /**
   * Removes the objects and every object they point to in the local datastore, recursively.
   *
   * @see #pinAllInBackground(List, SaveCallback)
   * @see #DEFAULT_PIN
   *
   * @param callback
   *          the callback
   */
  public static void unpinAllInBackground(DeleteCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(unpinAllInBackground(), callback);
  }

  /**
   * Removes the objects and every object they point to in the local datastore, recursively.
   *
   * @see #pinAllInBackground(List, SaveCallback)
   * @see #DEFAULT_PIN
   *
   * @return A {@link Task} that is resolved when unpinning all completes.
   */
  public static Task<Void> unpinAllInBackground() {
    return unpinAllInBackground(DEFAULT_PIN);
  }

  /**
   * Removes the objects and every object they point to in the local datastore, recursively.
   *
   * @see #pinAll(List)
   * @see #DEFAULT_PIN
   *
   * @throws BEException
   */
  public static void unpinAll() throws BEException {
    BETaskUtils.wait(unpinAllInBackground());
  }

  /**
   * Loads data from the local datastore into this object, if it has not been fetched from the
   * server already. If the object is not stored in the local datastore, this method with do
   * nothing.
   */
  @SuppressWarnings("unchecked")
  /* package */ <T extends BEObject> Task<T> fetchFromLocalDatastoreAsync() {
    if (!CSBM.isLocalDatastoreEnabled()) {
      throw new IllegalStateException("Method requires Local Datastore. " +
          "Please refer to `BE#enableLocalDatastore(Context)`.");
    }
    return CSBM.getLocalDatastore().fetchLocallyAsync((T) this);
  }

  /**
   * Loads data from the local datastore into this object, if it has not been fetched from the
   * server already. If the object is not stored in the local datastore, this method with do
   * nothing.
   */
  public <T extends BEObject> void fetchFromLocalDatastoreInBackground(GetCallback<T> callback) {
    BETaskUtils.callbackOnMainThreadAsync(this.<T>fetchFromLocalDatastoreAsync(), callback);
  }

  /**
   * Loads data from the local datastore into this object, if it has not been fetched from the
   * server already. If the object is not stored in the local datastore, this method with throw a
   * CACHE_MISS exception.
   *
   * @throws BEException
   */
  public void fetchFromLocalDatastore() throws BEException {
    BETaskUtils.wait(fetchFromLocalDatastoreAsync());
  }

  /**
   * Stores the object and every object it points to in the local datastore, recursively. If those
   * other objects have not been fetched from CSBM, they will not be stored. However, if they have
   * changed data, all of the changes will be retained. To get the objects back later, you can use
   * {@link BEQuery#fromLocalDatastore()}, or you can create an unfetched pointer with
   * {@link #createWithoutData(Class, String)} and then call {@link #fetchFromLocalDatastore()} on
   * it.
   *
   * @see #unpinInBackground(String, DeleteCallback)
   *
   * @param callback
   *          the callback
   */
  public void pinInBackground(String name, SaveCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(pinInBackground(name), callback);
  }

  /**
   * Stores the object and every object it points to in the local datastore, recursively. If those
   * other objects have not been fetched from CSBM, they will not be stored. However, if they have
   * changed data, all of the changes will be retained. To get the objects back later, you can use
   * {@link BEQuery#fromLocalDatastore()}, or you can create an unfetched pointer with
   * {@link #createWithoutData(Class, String)} and then call {@link #fetchFromLocalDatastore()} on
   * it.
   *
   * @return A {@link Task} that is resolved when pinning completes.
   *
   * @see #unpinInBackground(String)
   */
  public Task<Void> pinInBackground(String name) {
    return pinAllInBackground(name, Collections.singletonList(this));
  }

  /* package */ Task<Void> pinInBackground(String name, boolean includeAllChildren) {
    return pinAllInBackground(name, Collections.singletonList(this), includeAllChildren);
  }

  /**
   * Stores the object and every object it points to in the local datastore, recursively. If those
   * other objects have not been fetched from CSBM, they will not be stored. However, if they have
   * changed data, all of the changes will be retained. To get the objects back later, you can use
   * {@link BEQuery#fromLocalDatastore()}, or you can create an unfetched pointer with
   * {@link #createWithoutData(Class, String)} and then call {@link #fetchFromLocalDatastore()} on
   * it.
   *
   * @see #unpin(String)
   *
   * @throws BEException
   */
  public void pin(String name) throws BEException {
    BETaskUtils.wait(pinInBackground(name));
  }

  /**
   * Stores the object and every object it points to in the local datastore, recursively. If those
   * other objects have not been fetched from CSBM, they will not be stored. However, if they have
   * changed data, all of the changes will be retained. To get the objects back later, you can use
   * {@link BEQuery#fromLocalDatastore()}, or you can create an unfetched pointer with
   * {@link #createWithoutData(Class, String)} and then call {@link #fetchFromLocalDatastore()} on
   * it.
   *
   * @see #unpinInBackground(DeleteCallback)
   * @see #DEFAULT_PIN
   *
   * @param callback
   *          the callback
   */
  public void pinInBackground(SaveCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(pinInBackground(), callback);
  }

  /**
   * Stores the object and every object it points to in the local datastore, recursively. If those
   * other objects have not been fetched from CSBM, they will not be stored. However, if they have
   * changed data, all of the changes will be retained. To get the objects back later, you can use
   * {@link BEQuery#fromLocalDatastore()}, or you can create an unfetched pointer with
   * {@link #createWithoutData(Class, String)} and then call {@link #fetchFromLocalDatastore()} on
   * it.
   *
   * @return A {@link Task} that is resolved when pinning completes.
   *
   * @see #unpinInBackground()
   * @see #DEFAULT_PIN
   */
  public Task<Void> pinInBackground() {
    return pinAllInBackground(DEFAULT_PIN, Arrays.asList(this));
  }

  /**
   * Stores the object and every object it points to in the local datastore, recursively. If those
   * other objects have not been fetched from CSBM, they will not be stored. However, if they have
   * changed data, all of the changes will be retained. To get the objects back later, you can use
   * {@link BEQuery#fromLocalDatastore()}, or you can create an unfetched pointer with
   * {@link #createWithoutData(Class, String)} and then call {@link #fetchFromLocalDatastore()} on
   * it.
   *
   * @see #unpin()
   * @see #DEFAULT_PIN
   *
   * @throws BEException
   */
  public void pin() throws BEException {
    BETaskUtils.wait(pinInBackground());
  }

  /**
   * Removes the object and every object it points to in the local datastore, recursively.
   *
   * @see #pinInBackground(String, SaveCallback)
   *
   * @param callback
   *          the callback
   */
  public void unpinInBackground(String name, DeleteCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(unpinInBackground(name), callback);
  }

  /**
   * Removes the object and every object it points to in the local datastore, recursively.
   *
   * @return A {@link Task} that is resolved when unpinning completes.
   *
   * @see #pinInBackground(String)
   */
  public Task<Void> unpinInBackground(String name) {
    return unpinAllInBackground(name, Arrays.asList(this));
  }

  /**
   * Removes the object and every object it points to in the local datastore, recursively.
   *
   * @see #pin(String)
   */
  public void unpin(String name) throws BEException {
    BETaskUtils.wait(unpinInBackground(name));
  }

  /**
   * Removes the object and every object it points to in the local datastore, recursively.
   *
   * @see #pinInBackground(SaveCallback)
   * @see #DEFAULT_PIN
   *
   * @param callback
   *          the callback
   */
  public void unpinInBackground(DeleteCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(unpinInBackground(), callback);
  }

  /**
   * Removes the object and every object it points to in the local datastore, recursively.
   *
   * @return A {@link Task} that is resolved when unpinning completes.
   *
   * @see #pinInBackground()
   * @see #DEFAULT_PIN
   */
  public Task<Void> unpinInBackground() {
    return unpinAllInBackground(DEFAULT_PIN, Arrays.asList(this));
  }

  /**
   * Removes the object and every object it points to in the local datastore, recursively.
   *
   * @see #pin()
   * @see #DEFAULT_PIN
   */
  public void unpin() throws BEException {
    BETaskUtils.wait(unpinInBackground());
  }
}

// [1] Normally we should only construct the command from state when it's our turn in the
// taskQueue so that new objects will have an updated objectId from previous saves.
// We can't do this for save/deleteEventually since this will break the promise that we'll
// try to run the command eventually, since our process might die before it's our turn in
// the taskQueue.
// This seems like this will only be a problem for new objects that are saved &
// save/deleteEventually'd at the same time, as the first will create 2 objects and the second
// the delete might fail.
