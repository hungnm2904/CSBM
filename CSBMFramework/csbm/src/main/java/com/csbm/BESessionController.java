package com.csbm;

import bolts.Task;

/** package */ interface BESessionController {

  Task<BEObject.State> getSessionAsync(String sessionToken);

  Task<Void> revokeAsync(String sessionToken);

  Task<BEObject.State> upgradeToRevocable(String sessionToken);
}
