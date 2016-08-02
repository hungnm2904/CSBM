/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@code BEACL} is used to control which users can access or modify a particular object. Each
 * {@link BEObject} can have its own {@code BEACL}. You can grant read and write permissions
 * separately to specific users, to groups of users that belong to roles, or you can grant
 * permissions to "the public" so that, for example, any user could read a particular object but
 * only a particular set of users could write to that object.
 */
public class BEACL {
  private static final String PUBLIC_KEY = "*";
  private final static String UNRESOLVED_KEY = "*unresolved";
  private static final String KEY_ROLE_PREFIX = "role:";
  private static final String UNRESOLVED_USER_JSON_KEY = "unresolvedUser";

  private static class Permissions {
    private static final String READ_PERMISSION = "read";
    private static final String WRITE_PERMISSION = "write";

    private final boolean readPermission;
    private final boolean writePermission;

    /* package */ Permissions(boolean readPermission, boolean write) {
      this.readPermission = readPermission;
      this.writePermission = write;
    }

    /* package */ Permissions(Permissions permissions) {
      this.readPermission = permissions.readPermission;
      this.writePermission = permissions.writePermission;
    }

    /* package */ JSONObject toJSONObject() {
      JSONObject json = new JSONObject();

      try {
        if (readPermission) {
          json.put(READ_PERMISSION, true);
        }
        if (writePermission) {
          json.put(WRITE_PERMISSION, true);
        }
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      return json;
    }

    /* package */ boolean getReadPermission() {
      return readPermission;
    }

    /* package */ boolean getWritePermission() {
      return writePermission;
    }

    /* package */ static Permissions createPermissionsFromJSONObject(JSONObject object) {
      boolean read = object.optBoolean(READ_PERMISSION, false);
      boolean write = object.optBoolean(WRITE_PERMISSION, false);
      return new Permissions(read, write);
    }
  }

  private static BEDefaultACLController getDefaultACLController() {
    return BECorePlugins.getInstance().getDefaultACLController();
  }

  /**
   * Sets a default ACL that will be applied to all {@link BEObject}s when they are created.
   *
   * @param acl
   *          The ACL to use as a template for all {@link BEObject}s created after setDefaultACL
   *          has been called. This value will be copied and used as a template for the creation of
   *          new ACLs, so changes to the instance after {@code setDefaultACL(BEACL, boolean)}
   *          has been called will not be reflected in new {@link BEObject}s.
   * @param withAccessForCurrentUser
   *          If {@code true}, the {@code BEACL} that is applied to newly-created
   *          {@link BEObject}s will provide read and write access to the
   *          {@link BEUser#getCurrentUser()} at the time of creation. If {@code false}, the
   *          provided ACL will be used without modification. If acl is {@code null}, this value is
   *          ignored.
   */
  public static void setDefaultACL(BEACL acl, boolean withAccessForCurrentUser) {
    getDefaultACLController().set(acl, withAccessForCurrentUser);
  }

  /* package */ static BEACL getDefaultACL() {
    return getDefaultACLController().get();
  }

  // State
  private final Map<String, Permissions> permissionsById = new HashMap<>();
  private boolean shared;

  /**
   * A lazy user that hasn't been saved to CSBM.
   */
  //TODO (grantland): This should be a list for multiple lazy users with read/write permissions.
  private BEUser unresolvedUser;

  /**
   * Creates an ACL with no permissions granted.
   */
  public BEACL() {
    // do nothing
  }

  /**
   * Creates a copy of {@code acl}.
   *
   * @param acl
   *          The acl to copy.
   */
  public BEACL(BEACL acl) {
    for (String id : acl.permissionsById.keySet()) {
      permissionsById.put(id, new Permissions(acl.permissionsById.get(id)));
    }
    unresolvedUser = acl.unresolvedUser;
    if (unresolvedUser != null) {
      unresolvedUser.registerSaveListener(new UserResolutionListener(this));
    }
  }

  /* package for tests */ BEACL copy() {
    return new BEACL(this);
  }

  boolean isShared() {
    return shared;
  }

  void setShared(boolean shared) {
    this.shared = shared;
  }

  // Internally we expose the json object this wraps
  /* package */ JSONObject toJSONObject(BEEncoder objectEncoder) {
    JSONObject json = new JSONObject();
    try {
      for (String id: permissionsById.keySet()) {
        json.put(id, permissionsById.get(id).toJSONObject());
      }
      if (unresolvedUser != null) {
        Object encoded = objectEncoder.encode(unresolvedUser);
        json.put(UNRESOLVED_USER_JSON_KEY, encoded);
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return json;
  }

  // A helper for creating a BEACL from the wire.
  // We iterate over it rather than just copying to permissionsById so that we
  // can ensure it's the right format.
  /* package */ static BEACL createACLFromJSONObject(JSONObject object, BEDecoder decoder) {
    BEACL acl = new BEACL();

    for (String key : BEJSONUtils.keys(object)) {
      if (key.equals(UNRESOLVED_USER_JSON_KEY)) {
        JSONObject unresolvedUser;
        try {
          unresolvedUser = object.getJSONObject(key);
        } catch (JSONException e) {
          throw new RuntimeException(e);
        }
        acl.unresolvedUser = (BEUser) decoder.decode(unresolvedUser);
      } else {
        try {
          Permissions permissions = Permissions.createPermissionsFromJSONObject(object.getJSONObject(key));
          acl.permissionsById.put(key, permissions);
        } catch (JSONException e) {
          throw new RuntimeException("could not decode ACL: " + e.getMessage());
        }
      }
    }
    return acl;
  }

  /**
   * Creates an ACL where only the provided user has access.
   *
   * @param owner
   *          The only user that can read or write objects governed by this ACL.
   */
  public BEACL(BEUser owner) {
    this();
    setReadAccess(owner, true);
    setWriteAccess(owner, true);
  }

  /* package for tests */ void resolveUser(BEUser user) {
    if (user != unresolvedUser) {
      return;
    }
    if (permissionsById.containsKey(UNRESOLVED_KEY)) {
      permissionsById.put(user.getObjectId(), permissionsById.get(UNRESOLVED_KEY));
      permissionsById.remove(UNRESOLVED_KEY);
    }
    unresolvedUser = null;
  }

  /* package */ boolean hasUnresolvedUser() {
    return unresolvedUser != null;
  }

  /* package */ BEUser getUnresolvedUser() {
    return unresolvedUser;
  }

  // Helper for setting stuff
  private void setPermissionsIfNonEmpty(String userId, boolean readPermission, boolean writePermission) {
    if (!(readPermission || writePermission)) {
      permissionsById.remove(userId);
    }
    else {
      permissionsById.put(userId, new Permissions(readPermission, writePermission));
    }
  }

  /**
   * Set whether the public is allowed to read this object.
   */
  public void setPublicReadAccess(boolean allowed) {
    setReadAccess(PUBLIC_KEY, allowed);
  }

  /**
   * Get whether the public is allowed to read this object.
   */
  public boolean getPublicReadAccess() {
    return getReadAccess(PUBLIC_KEY);
  }

  /**
   * Set whether the public is allowed to write this object.
   */
  public void setPublicWriteAccess(boolean allowed) {
    setWriteAccess(PUBLIC_KEY, allowed);
  }

  /**
   * Set whether the public is allowed to write this object.
   */
  public boolean getPublicWriteAccess() {
    return getWriteAccess(PUBLIC_KEY);
  }

  /**
   * Set whether the given user id is allowed to read this object.
   */
  public void setReadAccess(String userId, boolean allowed) {
    if (userId == null) {
      throw new IllegalArgumentException("cannot setReadAccess for null userId");
    }
    boolean writePermission = getWriteAccess(userId);
    setPermissionsIfNonEmpty(userId, allowed, writePermission);
  }

  /**
   * Get whether the given user id is *explicitly* allowed to read this object. Even if this returns
   * {@code false}, the user may still be able to access it if getPublicReadAccess returns
   * {@code true} or a role  that the user belongs to has read access.
   */
  public boolean getReadAccess(String userId) {
    if (userId == null) {
      throw new IllegalArgumentException("cannot getReadAccess for null userId");
    }
    Permissions permissions = permissionsById.get(userId);
    return permissions != null && permissions.getReadPermission();
  }

  /**
   * Set whether the given user id is allowed to write this object.
   */
  public void setWriteAccess(String userId, boolean allowed) {
    if (userId == null) {
      throw new IllegalArgumentException("cannot setWriteAccess for null userId");
    }
    boolean readPermission = getReadAccess(userId);
    setPermissionsIfNonEmpty(userId, readPermission, allowed);
  }

  /**
   * Get whether the given user id is *explicitly* allowed to write this object. Even if this
   * returns {@code false}, the user may still be able to write it if getPublicWriteAccess returns
   * {@code true} or a role that the user belongs to has write access.
   */
  public boolean getWriteAccess(String userId) {
    if (userId == null) {
      throw new IllegalArgumentException("cannot getWriteAccess for null userId");
    }
    Permissions permissions = permissionsById.get(userId);
    return permissions != null && permissions.getWritePermission();
  }

  /**
   * Set whether the given user is allowed to read this object.
   */
  public void setReadAccess(BEUser user, boolean allowed) {
    if (user.getObjectId() == null) {
      if (user.isLazy()) {
        setUnresolvedReadAccess(user, allowed);
        return;
      }
      throw new IllegalArgumentException("cannot setReadAccess for a user with null id");
    }
    setReadAccess(user.getObjectId(), allowed);
  }

  private void setUnresolvedReadAccess(BEUser user, boolean allowed) {
    prepareUnresolvedUser(user);
    setReadAccess(UNRESOLVED_KEY, allowed);
  }

  private void setUnresolvedWriteAccess(BEUser user, boolean allowed) {
    prepareUnresolvedUser(user);
    setWriteAccess(UNRESOLVED_KEY, allowed);
  }

  private void prepareUnresolvedUser(BEUser user) {
    // Registers a listener for the user so that when it is saved, the
    // unresolved ACL will be resolved.
    if (this.unresolvedUser != user) {
      permissionsById.remove(UNRESOLVED_KEY);
      unresolvedUser = user;
      user.registerSaveListener(new UserResolutionListener(this));
    }
  }

  /**
   * Get whether the given user id is *explicitly* allowed to read this object. Even if this returns
   * {@code false}, the user may still be able to access it if getPublicReadAccess returns
   * {@code true} or a role that the user belongs to has read access.
   */
  public boolean getReadAccess(BEUser user) {
    if (user == unresolvedUser) {
      return getReadAccess(UNRESOLVED_KEY);
    }
    if (user.isLazy()) {
      return false;
    }
    if (user.getObjectId() == null) {
      throw new IllegalArgumentException("cannot getReadAccess for a user with null id");
    }
    return getReadAccess(user.getObjectId());
  }

  /**
   * Set whether the given user is allowed to write this object.
   */
  public void setWriteAccess(BEUser user, boolean allowed) {
    if (user.getObjectId() == null) {
      if (user.isLazy()) {
        setUnresolvedWriteAccess(user, allowed);
        return;
      }
      throw new IllegalArgumentException("cannot setWriteAccess for a user with null id");
    }
    setWriteAccess(user.getObjectId(), allowed);
  }

  /**
   * Get whether the given user id is *explicitly* allowed to write this object. Even if this
   * returns {@code false}, the user may still be able to write it if getPublicWriteAccess returns
   * {@code true} or a role that the user belongs to has write access.
   */
  public boolean getWriteAccess(BEUser user) {
    if (user == unresolvedUser) {
      return getWriteAccess(UNRESOLVED_KEY);
    }
    if (user.isLazy()) {
      return false;
    }
    if (user.getObjectId() == null) {
      throw new IllegalArgumentException("cannot getWriteAccess for a user with null id");
    }
    return getWriteAccess(user.getObjectId());
  }

  /**
   * Get whether users belonging to the role with the given roleName are allowed to read this
   * object. Even if this returns {@code false}, the role may still be able to read it if a parent
   * role has read access.
   * 
   * @param roleName
   *          The name of the role.
   * @return {@code true} if the role has read access. {@code false} otherwise.
   */
  public boolean getRoleReadAccess(String roleName) {
    return getReadAccess(KEY_ROLE_PREFIX + roleName);
  }

  /**
   * Set whether users belonging to the role with the given roleName are allowed to read this
   * object.
   * 
   * @param roleName
   *          The name of the role.
   * @param allowed
   *          Whether the given role can read this object.
   */
  public void setRoleReadAccess(String roleName, boolean allowed) {
    setReadAccess(KEY_ROLE_PREFIX + roleName, allowed);
  }

  /**
   * Get whether users belonging to the role with the given roleName are allowed to write this
   * object. Even if this returns {@code false}, the role may still be able to write it if a parent
   * role has write access.
   * 
   * @param roleName
   *          The name of the role.
   * @return {@code true} if the role has write access. {@code false} otherwise.
   */
  public boolean getRoleWriteAccess(String roleName) {
    return getWriteAccess(KEY_ROLE_PREFIX + roleName);
  }

  /**
   * Set whether users belonging to the role with the given roleName are allowed to write this
   * object.
   * 
   * @param roleName
   *          The name of the role.
   * @param allowed
   *          Whether the given role can write this object.
   */
  public void setRoleWriteAccess(String roleName, boolean allowed) {
    setWriteAccess(KEY_ROLE_PREFIX + roleName, allowed);
  }

  private static void validateRoleState(BERole role) {
    if (role == null || role.getObjectId() == null) {
      throw new IllegalArgumentException(
          "Roles must be saved to the server before they can be used in an ACL.");
    }
  }

  /**
   * Get whether users belonging to the given role are allowed to read this object. Even if this
   * returns {@code false}, the role may still be able to read it if a parent role has read access.
   * The role must already be saved on the server and its data must have been fetched in order to
   * use this method.
   * 
   * @param role
   *          The role to check for access.
   * @return {@code true} if the role has read access. {@code false} otherwise.
   */
  public boolean getRoleReadAccess(BERole role) {
    validateRoleState(role);
    return getRoleReadAccess(role.getName());
  }

  /**
   * Set whether users belonging to the given role are allowed to read this object. The role must
   * already be saved on the server and its data must have been fetched in order to use this method.
   * 
   * @param role
   *          The role to assign access.
   * @param allowed
   *          Whether the given role can read this object.
   */
  public void setRoleReadAccess(BERole role, boolean allowed) {
    validateRoleState(role);
    setRoleReadAccess(role.getName(), allowed);
  }

  /**
   * Get whether users belonging to the given role are allowed to write this object. Even if this
   * returns {@code false}, the role may still be able to write it if a parent role has write
   * access. The role must already be saved on the server and its data must have been fetched in
   * order to use this method.
   * 
   * @param role
   *          The role to check for access.
   * @return {@code true} if the role has write access. {@code false} otherwise.
   */
  public boolean getRoleWriteAccess(BERole role) {
    validateRoleState(role);
    return getRoleWriteAccess(role.getName());
  }

  /**
   * Set whether users belonging to the given role are allowed to write this object. The role must
   * already be saved on the server and its data must have been fetched in order to use this method.
   * 
   * @param role
   *          The role to assign access.
   * @param allowed
   *          Whether the given role can write this object.
   */
  public void setRoleWriteAccess(BERole role, boolean allowed) {
    validateRoleState(role);
    setRoleWriteAccess(role.getName(), allowed);
  }

  private static class UserResolutionListener implements GetCallback<BEObject> {
    private final WeakReference<BEACL> parent;

    public UserResolutionListener(BEACL parent) {
      this.parent = new WeakReference<>(parent);
    }

    @Override
    public void done(BEObject object, BEException e) {
      // A callback that will resolve the user when it is saved for any
      // ACL that is listening to it.
      try {
        BEACL parent = this.parent.get();
        if (parent != null) {
          parent.resolveUser((BEUser) object);
        }
      } finally {
        object.unregisterSaveListener(this);
      }
    }

  }

  /* package for tests */ Map<String, Permissions> getPermissionsById() {
    return permissionsById;
  }
}