/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import java.util.regex.Pattern;

/**
 * Represents a Role on the CSBM server. {@code BERole}s represent groupings of
 * {@code BEUsers} for the purposes of granting permissions (e.g. specifying a {@link BEACL}
 * for a {@link BEObject}). Roles are specified by their sets of child users and child roles, all
 * of which are granted any permissions that the parent role has.<br />
 * <br />
 * Roles must have a name (which cannot be changed after creation of the role), and must specify an
 * ACL.
 */
@BEClassName("_Role")
public class BERole extends BEObject {
  private static final Pattern NAME_PATTERN = Pattern.compile("^[0-9a-zA-Z_\\- ]+$");

  /**
   * Used for the factory methods. Developers will need to set a name on objects created like this,
   * which is why the constructor with a roleName is exposed publicly.
   */
  BERole() {
  }

  /**
   * Constructs a new BERole with the given name. If no default ACL has been specified, you must
   * provide an ACL for the role.
   *
   * @param name
   *          The name of the Role to create.
   */
  public BERole(String name) {
    this();
    setName(name);
  }

  /**
   * Constructs a new BERole with the given name.
   *
   * @param name
   *          The name of the Role to create.
   * @param acl
   *          The ACL for this role. Roles must have an ACL.
   */
  public BERole(String name, BEACL acl) {
    this(name);
    setACL(acl);
  }

  /**
   * Sets the name for a role. This value must be set before the role has been saved to the server,
   * and cannot be set once the role has been saved.<br />
   * <br />
   * A role's name can only contain alphanumeric characters, _, -, and spaces.
   * 
   * @param name
   *          The name of the role.
   * @throws IllegalStateException
   *           if the object has already been saved to the server.
   */
  public void setName(String name) {
    this.put("name", name);
  }

  /**
   * Gets the name of the role.
   * 
   * @return the name of the role.
   */
  public String getName() {
    return this.getString("name");
  }

  /**
   * Gets the {@link BERelation} for the {@link BEUser}s that are direct children of this
   * role. These users are granted any privileges that this role has been granted (e.g. read or
   * write access through ACLs). You can add or remove users from the role through this relation.
   * 
   * @return the relation for the users belonging to this role.
   */
  public BERelation<BEUser> getUsers() {
    return getRelation("users");
  }

  /**
   * Gets the {@link BERelation} for the {@link BERole}s that are direct children of this
   * role. These roles' users are granted any privileges that this role has been granted (e.g. read
   * or write access through ACLs). You can add or remove child roles from this role through this
   * relation.
   * 
   * @return the relation for the roles belonging to this role.
   */
  public BERelation<BERole> getRoles() {
    return getRelation("roles");
  }

  @Override
  /* package */ void validateSave() {
    synchronized (mutex) {
      if (this.getObjectId() == null && getName() == null) {
        throw new IllegalStateException("New roles must specify a name.");
      }
      super.validateSave();
    }
  }

  @Override
  public void put(String key, Object value) {
    if ("name".equals(key)) {
      if (this.getObjectId() != null) {
        throw new IllegalArgumentException(
            "A role's name can only be set before it has been saved.");
      }
      if (!(value instanceof String)) {
        throw new IllegalArgumentException("A role's name must be a String.");
      }
      if (!NAME_PATTERN.matcher((String) value).matches()) {
        throw new IllegalArgumentException(
            "A role's name can only contain alphanumeric characters, _, -, and spaces.");
      }
    }
    super.put(key, value);
  }

  /**
   * Gets a {@link BEQuery} over the Role collection.
   * 
   * @return A new query over the Role collection.
   */
  public static BEQuery<BERole> getQuery() {
    return BEQuery.getQuery(BERole.class);
  }
}
