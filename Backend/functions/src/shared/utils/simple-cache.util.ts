/**
 * Simple In-Memory Cache Utility
 *
 * Lightweight caching solution for Cloud Functions.
 * - TTL-based expiration
 * - Thread-safe within a single instance
 * - No external dependencies
 *
 * Note: Each Cloud Function instance has its own cache.
 * Cache is lost on cold starts.
 */

interface CacheEntry<T = unknown> {
  value: T;
  expiry: number;
}

export class SimpleCache {
  private cache = new Map<string, CacheEntry>();
  private readonly maxSize: number;

  constructor(maxSize = 1000) {
    this.maxSize = maxSize;
  }

  /**
   * Get a cached value by key
   * Returns null if not found or expired
   */
  get<T = unknown>(key: string): T | null {
    const entry = this.cache.get(key);

    if (!entry) {
      return null;
    }

    // Check if expired
    if (Date.now() > entry.expiry) {
      this.cache.delete(key);
      console.log(`📦 Cache EXPIRED: ${key}`);
      return null;
    }

    console.log(`📦 Cache HIT: ${key}`);
    return entry.value as T;
  }

  /**
   * Set a cache value with TTL
   * @param key Cache key
   * @param value Value to cache
   * @param ttlMs Time to live in milliseconds
   */
  set<T = unknown>(key: string, value: T, ttlMs: number): void {
    // Evict oldest entries if cache is full
    if (this.cache.size >= this.maxSize) {
      const oldestKey = this.cache.keys().next().value;
      if (oldestKey) {
        this.cache.delete(oldestKey);
        console.log(`📦 Cache EVICTED (full): ${oldestKey}`);
      }
    }

    this.cache.set(key, {
      value,
      expiry: Date.now() + ttlMs,
    });

    console.log(`📦 Cache SET: ${key} (TTL: ${ttlMs}ms)`);
  }

  /**
   * Invalidate a specific cache entry
   */
  invalidate(key: string): boolean {
    const deleted = this.cache.delete(key);
    if (deleted) {
      console.log(`📦 Cache INVALIDATED: ${key}`);
    }
    return deleted;
  }

  /**
   * Invalidate all entries matching a prefix
   * Useful for invalidating shop:* patterns
   */
  invalidateByPrefix(prefix: string): number {
    let count = 0;
    for (const key of this.cache.keys()) {
      if (key.startsWith(prefix)) {
        this.cache.delete(key);
        count++;
      }
    }
    if (count > 0) {
      console.log(`📦 Cache INVALIDATED ${count} entries with prefix: ${prefix}`);
    }
    return count;
  }

  /**
   * Clear all cache entries
   */
  clear(): void {
    const size = this.cache.size;
    this.cache.clear();
    console.log(`📦 Cache CLEARED: ${size} entries`);
  }

  /**
   * Get cache statistics
   */
  getStats(): { size: number; maxSize: number } {
    return {
      size: this.cache.size,
      maxSize: this.maxSize,
    };
  }

  /**
   * Check if a key exists and is valid
   */
  has(key: string): boolean {
    const entry = this.cache.get(key);
    if (!entry) return false;
    if (Date.now() > entry.expiry) {
      this.cache.delete(key);
      return false;
    }
    return true;
  }
}

// Default cache TTLs (in milliseconds)
export const CACHE_TTL = {
  MENU: 2 * 60 * 1000, // 2 minutes for shop menu
  SEARCH_INDEX: 5 * 60 * 1000, // 5 minutes for search index
  FAQS: 5 * 60 * 1000, // 5 minutes for FAQs
  SHOP_DETAIL: 1 * 60 * 1000, // 1 minute for shop details
} as const;

// Global cache instance (shared within Cloud Function instance)
export const globalCache = new SimpleCache(1000);
