# Job Restrictions Plugin: Queue Performance

This document describes how the Job Restrictions Plugin optimizes queue scheduling performance.

## canTake Cache

`JobRestrictionProperty.canTake(Queue.BuildableItem)` is invoked during `Queue.maintain()` for each (node, item) pair when building candidates. With many buildable items and many nodes, this can be called hundreds of times per maintenance cycle.

### Caching

The plugin caches `canTake` results with a TTL to reduce repeated work. The cache key includes both `item.getId()` and `task.getFullDisplayName()` so that when queue item IDs are reused (e.g. a pipeline flyweight task after a blocked item leaves), we do not return a stale "block" from a different item.

- **Cache key:** `itemId + "|" + task.getFullDisplayName()` (avoids ID reuse collisions)
- **Only "allow" (null) is cached** – "block" results are never cached to avoid stale blocks
- **TTL:** 30 seconds (configurable via `cacheTtlMs`)
- **Max entries:** 500 per property instance (configurable via `cacheMaxEntries`)

### Cache Implementation

The cache `ConcurrentHashMap` is declared `transient volatile` with lazy double-checked-locking initialization via `getCache()`. This is required because Jenkins persists `NodeProperty` instances via XStream, which deserializes objects **bypassing constructors and field initializers**. A non-transient field initializer (e.g. `new ConcurrentHashMap<>()`) never runs after deserialization, leaving the field `null` and causing `NullPointerException` on the first `cache.get()` call. Jenkins core's `Node.canTake()` catches the exception (line 456 of `Node.java`) and treats it as a block — silently preventing all items from being dispatched to the node.

### Configuration

| System Property | Default | Description |
|-----------------|---------|-------------|
| `JobRestrictionProperty.cacheDisabled` | false | Set to true to disable caching |
| `JobRestrictionProperty.cacheTtlMs` | 30000 | Cache TTL in milliseconds |
| `JobRestrictionProperty.cacheMaxEntries` | 500 | Max cache entries per node |

### Built-in controller with pipeline orchestration

When the built-in controller restricts jobs (e.g. only `_jervis_generator`, `^__.*`, and WorkflowJob/WorkflowMultiBranchProject/OrganizationFolder for flyweight orchestration), pipeline flyweight tasks must be allowed. The composite cache key ensures that when a blocked item leaves and a flyweight gets the same queue ID, we do not incorrectly return a cached "block".

### Interaction with Leastload Plugin

When used with the [leastload plugin](https://plugins.jenkins.io/leastload/), `canTake` is called for each candidate executor. The cache reduces repeated restriction checks when items remain in buildables across maintenance cycles.

## Jenkins Core Queue Context

### How canTake is called

`Queue.maintain()` holds a single `ReentrantLock` for its entire duration. For non-flyweight tasks, it builds candidates by iterating parked executors and calling `JobOffer.getCauseOfBlockage(item)` → `Node.canTake(item)` → iterates all `NodeProperty` instances including `JobRestrictionProperty`. For flyweight tasks, `makeFlyWeightTaskBuildable()` calls `Node.canTake(item)` for each candidate node.

`Node.canTake()` (line 452–462 of `Node.java`) wraps each `NodeProperty.canTake()` in a try-catch. If a `NodeProperty` throws any `Throwable`, the exception is caught and treated as a block (`CauseOfBlockage.fromMessage`). This means bugs in `canTake()` (such as the transient cache NPE described above) silently block all items from the node rather than failing visibly.

### Queue.maintain() bottlenecks

| Bottleneck | Description |
|------------|-------------|
| **Global lock** | `maintain()` holds `Queue.lock` for the entire method. All `schedule()`, `cancel()`, `getItems()`, and `onStartExecuting()` calls block until it releases. |
| **getCauseOfBlockageForItem × 3** | Called for every blocked item, waiting item, and buildable item per cycle. Each call checks `task.getCauseOfBlockage()`, all `QueueTaskDispatcher`s, and (for buildables) `Node.canTake()` for every parked executor. |
| **O(buildables × executors × nodeProperties)** | Each buildable checks each parked executor, each executor checks each `NodeProperty`. Quadratic with queue depth × executor count. |
| **updateSnapshot() O(N)** | Called after each state transition inside maintain. Creates a new `Snapshot` copying all lists. |
| **5-second timer floor** | `MaintainTask` uses `scheduleWithFixedDelay` with default 5000ms. Even if maintain() is fast, there's a 5s minimum between cycles. |
