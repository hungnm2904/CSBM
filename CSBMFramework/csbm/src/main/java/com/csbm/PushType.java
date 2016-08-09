package com.csbm;

/** package */ enum PushType {
  NONE ("none"),
  PPNS ("ppns"),
  GCM ("gcm");
  
  private final String pushType;

  PushType(String pushType) {
    this.pushType = pushType;
  }
  
  static PushType fromString(String pushType) {
    if ("none".equals(pushType)) {
      return PushType.NONE;
    } else if ("ppns".equals(pushType)) {
      return PushType.PPNS;
    } else if ("gcm".equals(pushType)) {
      return PushType.GCM;
    } else {
      return null;
    }
  }
  
  @Override
  public String toString() {
    return pushType;
  }
}
