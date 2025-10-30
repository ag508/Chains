# Duplicate Model Definitions Catalog

## Summary
Found multiple duplicate model definitions that need consolidation to resolve compilation conflicts.

## Duplicate Definitions Found

### 1. IdentityStorage Interface
**Duplicates:**
- `app/src/main/java/com/chain/messaging/core/crypto/IdentityStorage.kt` (standalone interface)
- `app/src/main/java/com/chain/messaging/core/crypto/SignalProtocolStore.kt` (embedded interface)

**Differences:**
- Standalone version has more detailed documentation and includes Direction enum
- SignalProtocolStore version uses Signal Protocol's IdentityKeyStore.Direction
- Both have same method signatures but different Direction enum sources

**Usage Pattern:**
- Used in DI modules (CryptoModule)
- Referenced by SignalProtocolStore implementation
- Used by IdentityStorageImpl

**Recommendation:** Keep standalone version as authoritative source

### 2. SessionStorage Interface
**Duplicates:**
- `app/src/main/java/com/chain/messaging/core/crypto/SessionStorage.kt` (standalone interface)
- `app/src/main/java/com/chain/messaging/core/crypto/SignalProtocolStore.kt` (embedded interface)

**Differences:**
- Both have identical method signatures
- Standalone version has better documentation

**Usage Pattern:**
- Used in DI modules and SignalProtocolStore
- Referenced by SessionStorageImpl

**Recommendation:** Keep standalone version as authoritative source

### 3. CryptoException Class
**Duplicates:**
- `app/src/main/java/com/chain/messaging/core/crypto/SignalEncryptionService.kt` (line 295)
- `app/src/main/java/com/chain/messaging/core/crypto/KeyManager.kt` (line 457)

**Differences:**
- Identical class definitions
- Both extend Exception with same constructor signature

**Usage Pattern:**
- Used locally within their respective files
- No cross-file imports found

**Recommendation:** Create single definition in domain.model package

### 4. SecurityAlert Class
**Duplicates:**
- `app/src/main/java/com/chain/messaging/domain/model/SecurityModels.kt` (sealed class)
- `app/src/main/java/com/chain/messaging/core/security/SecurityMonitoringManager.kt` (data class)

**Differences:**
- domain.model version: Comprehensive sealed class with multiple subtypes (KeyMismatch, IdentityKeyChanged, SuspiciousActivity, PolicyViolation)
- SecurityMonitoringManager version: Simple data class with different properties

**Usage Pattern:**
- domain.model version: Used by presentation layer (ViewModels, Screens)
- SecurityMonitoringManager version: Used internally by security monitoring system
- Some files import both with aliases to avoid conflicts

**Recommendation:** Use domain.model version as authoritative source for UI consumption

### 5. SecurityRecommendation Class
**Duplicates:**
- `app/src/main/java/com/chain/messaging/domain/model/SecurityModels.kt` (sealed class)
- No other direct duplicates found, but referenced in multiple places

**Usage Pattern:**
- Used by presentation layer and security managers
- Already consolidated in domain.model package

**Recommendation:** domain.model version is already the authoritative source

## Consolidation Strategy

### Phase 1: Choose Authoritative Sources
- **IdentityStorage**: `app/src/main/java/com/chain/messaging/core/crypto/IdentityStorage.kt`
- **SessionStorage**: `app/src/main/java/com/chain/messaging/core/crypto/SessionStorage.kt`
- **CryptoException**: Create new in `app/src/main/java/com/chain/messaging/domain/model/`
- **SecurityAlert**: `app/src/main/java/com/chain/messaging/domain/model/SecurityModels.kt`
- **SecurityRecommendation**: `app/src/main/java/com/chain/messaging/domain/model/SecurityModels.kt`

### Phase 2: Files to Modify
1. Remove duplicate interfaces from `SignalProtocolStore.kt`
2. Remove duplicate CryptoException from `SignalEncryptionService.kt` and `KeyManager.kt`
3. Remove duplicate SecurityAlert from `SecurityMonitoringManager.kt`
4. Update all imports to use consolidated models

### Phase 3: Import Updates Required
- Update DI modules to import from authoritative sources
- Update all security-related files to use domain.model types
- Fix any type alias conflicts in presentation layer