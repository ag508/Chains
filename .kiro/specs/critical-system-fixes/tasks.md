# Implementation Plan

## Phase 1: Critical Signal Protocol Fixes (45+ errors)

- [x] 1. Fix Signal Protocol Integration Issues

  - [x] 1.1 Resolve SenderKeyName import and type alias issues

    - Add proper import for `org.signal.libsignal.protocol.groups.SenderKeyName`
    - Update SignalProtocolTypeAliases.kt with correct Signal Protocol imports
    - Fix all 45+ SenderKeyName unresolved references across crypto package
    - Ensure Signal Protocol library version compatibility in build.gradle
    - _Requirements: 1.1, 1.2_

  - [x] 1.2 Fix SignalProtocolStoreAdapter implementation

    - Implement missing `storeSenderKey(SignalProtocolAddress, UUID, SenderKeyRecord)` method
    - Implement missing `loadSenderKey(SignalProtocolAddress, UUID)` method
    - Fix interface compliance with SenderKeyStore abstract methods
    - Resolve List<Int> to MutableList<Int> type mismatch in loadExistingSessions
    - Add proper override modifiers for interface methods
    - _Requirements: 1.1, 1.2_

  - [x] 1.3 Update SignalEncryptionService type handling

    - Fix ByteArray to UUID conversion in group encryption methods
    - Resolve SenderKeyMessage to ByteArray type mismatches
    - Add missing parameter values for Signal Protocol method calls
    - Update encryption/decryption method signatures for proper types
    - Fix SignalSenderKeyName unresolved references (10+ occurrences)
    - _Requirements: 1.1, 1.2_

  - [x] 1.4 Fix IdentityStorageImpl Signal Protocol integration

    - Add proper import for `org.signal.libsignal.protocol.util.KeyHelper.generateIdentityKeyPair`
    - Fix Direction enum references (import from Signal Protocol)
    - Add missing 'else' branch in when expressions
    - Resolve generateIdentityKeyPair method calls
    - Add override modifier for loadExistingSessions method
    - _Requirements: 1.1, 1.2_

## Phase 2: Model Consolidation and Type System Fixes (38+ errors)

- [x] 2. Consolidate Duplicate Model Definitions

  - [x] 2.1 Remove duplicate VerificationState and VerificationResult

    - Delete duplicate VerificationState.kt in core/security package
    - Delete duplicate VerificationResult.kt in core/security package
    - Keep canonical versions in domain/model/SecurityModels.kt
    - Update all imports to use domain model versions
    - Fix 8+ redeclaration compilation errors
    - _Requirements: 2.1, 2.2_

  - [x] 2.2 Standardize SecurityModels across packages

    - Consolidate SecurityEventType enums (domain vs core conflicts)
    - Merge SecuritySeverity definitions (domain vs core conflicts)
    - Update all 30+ enum comparison errors to use single source
    - Remove duplicate enum definitions in core/security package
    - Update all references to use domain.model.SecurityModels
    - _Requirements: 2.1, 2.2_

  - [x] 2.3 Fix RecordingEvent and ScreenshotEvent redeclarations

    - Resolve RecordingEvent redeclaration in CallRecordingManager vs CallRecordingDetector
    - Fix ScreenshotEvent redeclaration conflicts
    - Consolidate event hierarchies into single definitions
    - Update all event handling to use canonical types
    - Fix CallEvent redeclaration in WebRTCManager vs CallStateMachine
    - _Requirements: 2.1, 2.2_

## Phase 3: Security System Implementation (50+ errors)

- [x] 3. Fix Security System Integration

  - [x] 3.1 Implement missing SecurityAlert sealed class

    - Create proper SecurityAlert sealed class hierarchy in domain/model
    - Remove protected constructor access attempts (5+ errors)
    - Fix 20+ SecurityAlert unresolved reference errors
    - Update AlertNotificationService to use proper SecurityAlert types
    - Fix sealed types instantiation errors
    - _Requirements: 3.1, 3.2_

  - [x] 3.2 Add missing security enums and types

    - Implement SecurityLevel enum (CRITICAL, DANGER, WARNING, SECURE)
    - Add SecurityStatus enum for monitoring states
    - Create SecurityMetrics data class for performance tracking
    - Fix 15+ unresolved reference errors for security types
    - Add missing HIGH enum value for security levels
    - _Requirements: 3.1, 3.2_

  - [x] 3.3 Fix SecurityMonitoringManager implementation

    - Implement missing `reportSecurityEvent(event: SecurityEvent)` abstract method
    - Fix enum comparison issues between domain and core packages (15+ errors)
    - Resolve type inference problems in security event handling
    - Add proper override modifiers for interface methods
    - Fix 'when' expression exhaustiveness issues (5+ cases)
    - Fix variable expected errors in security status assignments
    - _Requirements: 3.1, 3.2_

## Phase 4: Date/Time and Data Access Fixes (25+ errors)

- [x] 4. Implement Date/Time Conversion Utilities

  - [x] 4.1 Create comprehensive TimeUtils

    - Add LocalDateTime to Long conversion methods (toEpochMilli)
    - Implement Date to timestamp utilities
    - Add timezone handling functions
    - Create utility methods for all date/time conversions
    - Add isAfter method for date comparisons
    - _Requirements: 4.1, 4.2_

  - [x] 4.2 Update all date/time usage across codebase

    - Fix 10+ Date to Long conversion errors in GroupManagerImpl
    - Update CrossDeviceSyncServiceImpl timestamp handling
    - Fix LocalDateTime to Long conversion in security monitoring
    - Standardize all date/time operations using TimeUtils
    - Fix isAfter method calls in ConflictResolverImpl
    - _Requirements: 4.1, 4.2_

- [x] 5. Resolve Dependency Injection Issues

  - [x] 5.1 Add missing DAO methods

    - Implement `insertChat(ChatEntity)` method in ChatDao
    - Add `updateChat(ChatEntity)` method in ChatDao
    - Implement `insertMessage(MessageEntity)` method in MessageDao
    - Add `observeChat(String): Flow<ChatEntity?>` method with proper typing
    - Fix 15+ unresolved DAO method references
    - _Requirements: 5.1, 5.2_

  - [x] 5.2 Fix repository method implementations

    - Add `getUserSettings()` method to SettingsRepository
    - Implement `markChatAsRead(String)` method
    - Add `getUnreadMessageCount(String)` method
    - Implement `getChatById(String)` method
    - Fix NotificationManager dependency resolution
    - Add missing repository methods for notification system
    - Add `getIncomingMessages()` method
    - _Requirements: 5.1, 5.2_

  - [x] 5.3 Update User model constructor parameters

    - Add missing `publicKey: String` parameter to User model constructor
    - Fix 20+ AuthenticationService User instantiation errors
    - Update all User creation calls with required publicKey parameter
    - Ensure User model consistency across all usage
    - _Requirements: 5.1, 5.2_

## Phase 5: Type System and UI Component Fixes (35+ errors)

- [x] 6. Fix Type System and Smart Cast Issues

  - [x] 6.1 Resolve generic type inference problems

    - Fix 15+ DeepRecursiveFunction usage errors in ErrorHandler
    - Add explicit type parameters where compiler cannot infer
    - Resolve "cannot infer type" errors in lambda expressions
    - Fix coroutine scope and async/await type issues
    - Fix suspendCoroutine unresolved references
    - _Requirements: 6.1, 6.2_

  - [x] 6.2 Fix function signature and parameter issues

    - Correct ComponentErrorHandlers type parameter placement (move before function name)
    - Fix SyncProgressDialog onDismiss parameter type (Any to () -> Unit)
    - Resolve method override conflicts and missing override modifiers
    - Fix suspend function calls outside coroutine context
    - Add missing 'content' parameter values in notification builders
    - _Requirements: 6.1, 6.2_

- [x] 7. Fix UI Component Integration Issues

  - [x] 7.1 Add missing resource references

    - Create missing drawable resources (ic_call_missed, ic_launcher_foreground)
    - Add missing notification defaults and setDefaults method calls
    - Fix 8+ resource reference errors in notification system
    - Create placeholder resources for missing UI elements
    - _Requirements: 7.1, 7.2_

  - [x] 7.2 Fix ViewModel and UI state management

    - Resolve CallViewModel event handling issues (RemoteStreamAdded, RemoteStreamRemoved)
    - Fix SecurityMonitoringViewModel Flow collection type issues
    - Update presentation layer type handling for proper smart casts
    - Fix 'when' expression exhaustiveness in UI components (5+ cases)
    - Add missing event properties (callId, stream, error, callSession)
    - Fix IceCandidateReceived unresolved references
    - _Requirements: 7.1, 7.2_

## Phase 6: Advanced System Integration (40+ errors)

- [x] 8. Fix Advanced System Integration Issues

  - [x] 8.1 Resolve P2P and Blockchain integration

    - Fix `initialize()` method calls in P2PManagerImpl and BlockchainManagerImpl
    - Resolve Random.nextInt() parameter issues in networking code
    - Fix 'break' and 'continue' usage outside loops in DHTPeerDiscovery
    - Resolve suspension point in critical section issues
    - Add missing initialize methods for managers
    - _Requirements: 8.1, 8.2_

  - [x] 8.2 Fix WebRTC and Call system integration

    - Resolve CallEvent redeclaration issues
    - Fix missing CallNotificationEvent references
    - Add missing call state properties and methods
    - Fix coroutine context issues in call management (launch unresolved)
    - Resolve codec and media handling type issues
    - Fix showCallEndedNotification, clearIncomingCallNotification methods
    - _Requirements: 8.1, 8.2_

  - [x] 8.3 Fix Cloud and Authentication integration

    - Resolve jsonObject unresolved references in cloud managers (5+ occurrences)
    - Fix OAuth and authentication type mismatches
    - Add missing credential and Builder references
    - Fix cloud storage initialization issues (initialize, initializeService)
    - Fix Int to Long type mismatches in cloud auth
    - _Requirements: 8.1, 8.2_

  - [x] 8.4 Fix Performance and Memory management

    - Resolve getGlobalGcTime and getTotalPss unresolved references
    - Fix coroutine scope issues in performance monitoring
    - Add missing performance storage methods (storePerformanceReport)
    - Fix delay() function parameter issues
    - Fix async and awaitAll unresolved references
    - _Requirements: 8.1, 8.2_

## Phase 7: Additional Critical Fixes (30+ errors)

- [-] 9. Fix Remaining Critical Issues

  - [x] 9.1 Resolve messaging system integration

    - Fix `message` unresolved references in MessagingService
    - Add missing message properties and methods
    - Resolve message content and media handling
    - Fix getMediaContent unresolved references
    - _Requirements: 1.1, 1.2_

  - [x] 9.2 Fix offline and sync system issues

    - Fix conflict resolution type issues
    - Add missing sync methods and properties
    - Fix replyToId parameter issues in sync service
    - Fix SyncResult data property override conflicts
    - Fix SyncStatus to String type mismatches
    - _Requirements: 4.1, 4.2_

  - [x] 9.3 Fix privacy and screenshot detection

    - Resolve `startDetection` method calls
    - Fix screenshot notification type issues
    - Add missing privacy event handling
    - Fix disappearing message notification system
    - Fix map function type inference issues
    - _Requirements: 3.1, 3.2_

  - [x] 9.4 Fix media compression and codec issues

    - Resolve CodecCapabilities unresolved references
    - Fix integer literal type conformance issues (4+ cases)
    - Add missing media handling methods
    - Fix thumbnail and compression utilities
    - _Requirements: 7.1, 7.2_

  - [x] 9.5 Fix authentication and deployment issues

    - Fix setDebugEnabled unresolved references in BuildConfigManager
    - Resolve ECPrivateKey to PrivateKey type mismatches
    - Fix PasskeyManager Builder and credential references
    - Add missing deployment configuration methods
    - _Requirements: 8.1, 8.2_

## Phase 8: Final Integration and Validation

- [x] 10. Build System Validation and Final Integration


  - [x] 10.1 Validate incremental compilation success

    - Test compilation after each major fix phase
    - Ensure no new errors are introduced by fixes
    - Validate that existing functionality is preserved
    - Run incremental builds to verify fix effectiveness
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

  - [x] 10.2 Complete end-to-end build validation

    - Perform full clean build of the entire application
    - Resolve any remaining compilation errors
    - Ensure APK can be generated successfully
    - Test application launch and basic functionality
    - Validate all build variants (debug, release, staging)
    - _Requirements: 8.4_

- [ ] 11. Testing and Validation

  - [ ] 11.1 Run comprehensive test suite

    - Execute all existing unit tests to ensure no regressions
    - Run integration tests for fixed components
    - Validate that dependency injection works correctly at runtime
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

  - [ ] 11.2 Perform regression testing
    - Test that all previously working features still function
    - Validate UI components render correctly
    - Ensure performance is not degraded by fixes
    - Document any remaining issues for future resolution
    - _Requirements: 8.4_
