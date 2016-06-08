//
//  BEPropertyInfo_Runtime.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEPropertyInfo_Runtime.h"
#import <objc/message.h>
#import <objc/runtime.h>
#import "BEAssert.h"
#import "BEBaseState.h"

#define NO_TYPECHECK_SYMBOL(ret, fn, args...) static ret fn ## _noTypeCheck (args) __attribute__((weakref(#fn)));
#define OBJECT_GETOFFSET_PTR(obj, offset) (void *) ((uintptr_t)obj + offset)

NO_TYPECHECK_SYMBOL(void *, objc_loadWeak, void **);

NO_TYPECHECK_SYMBOL(void *, objc_storeWeak, void **, void *);
NO_TYPECHECK_SYMBOL(void *, objc_storeStrong, void **, void *);

NO_TYPECHECK_SYMBOL(void *, objc_autorelease, void *);
NO_TYPECHECK_SYMBOL(void *, objc_retainAutorelease, void *);

void object_getIvarValue_safe(__unsafe_unretained id obj, Ivar ivar, void *toMemory, uint8_t associationType) {
  ptrdiff_t offset = ivar_getOffset(ivar);
  void *location = OBJECT_GETOFFSET_PTR(obj, offset);
  
  switch (associationType) {
      case BEPropertyInfoAssociationTypeDefault:
      BEParameterAssertionFailure(@"Invalid association type `Default`.");
      break;
      
      case BEPropertyInfoAssociationTypeAssign: {
        NSUInteger size = 0;
        NSGetSizeAndAlignment(ivar_getTypeEncoding(ivar), &size, NULL);
        
        memcpy(toMemory, location, size);
        break;
      }
      
      case BEPropertyInfoAssociationTypeWeak: {
        void *results = objc_loadWeak_noTypeCheck(location);
        
        memcpy(toMemory, &results, sizeof(id));
        break;
      }
      
      case BEPropertyInfoAssociationTypeStrong:
      case BEPropertyInfoAssociationTypeCopy:
      case BEPropertyInfoAssociationTypeMutableCopy: {
        void *objectValue = *(void **)location;
        objectValue = objc_retainAutorelease_noTypeCheck(objectValue);
        
        memcpy(toMemory, &objectValue, sizeof(id));
        break;
      }
  }
}

void object_setIvarValue_safe(__unsafe_unretained id obj, Ivar ivar, void *fromMemory, uint8_t associationType) {
  ptrdiff_t offset = ivar_getOffset(ivar);
  void *location = OBJECT_GETOFFSET_PTR(obj, offset);
  
  NSUInteger size = 0;
  NSGetSizeAndAlignment(ivar_getTypeEncoding(ivar), &size, NULL);
  
  void *newValue = NULL;
  
  switch (associationType) {
      case BEPropertyInfoAssociationTypeDefault:
      BEParameterAssertionFailure(@"Invalid association type `Default`.");
      return;
      
      case BEPropertyInfoAssociationTypeAssign: {
        memcpy(location, fromMemory, size);
        return;
      }
      
      case BEPropertyInfoAssociationTypeWeak: {
        objc_storeWeak_noTypeCheck(location, *(void **)fromMemory);
        return;
      }
      
      case BEPropertyInfoAssociationTypeStrong:
      newValue = *(void **)fromMemory;
      break;
      
      case BEPropertyInfoAssociationTypeCopy:
      case BEPropertyInfoAssociationTypeMutableCopy: {
        SEL command = (associationType == BEPropertyInfoAssociationTypeCopy) ? @selector(copy)
        : @selector(mutableCopy);
        
        
        void *(*objc_msgSend_casted)(void *, SEL) = (void *)objc_msgSend;
        void *oldValue = *(void **)fromMemory;
        
        newValue = objc_msgSend_casted(oldValue, command);
        newValue = objc_autorelease_noTypeCheck(newValue);
        break;
      }
  }
  
  objc_storeStrong_noTypeCheck(location, newValue);
}
