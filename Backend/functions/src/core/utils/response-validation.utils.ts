/**
 * Response Validation Utilities
 *
 * Helpers to detect and validate single-layer response wrapping.
 * Used to ensure TransformInterceptor is the ONLY wrapping layer.
 */

/**
 * Validates that a response is properly wrapped (single layer only)
 * Expected structure: { success: boolean, data: T, timestamp: string }
 *
 * @param response The HTTP response body to validate
 * @returns true if response is correctly single-wrapped, false if double-wrapped or invalid
 *
 * @example
 * const response = { success: true, data: { id: '123' }, timestamp: '...' };
 * isValidSingleWrap(response) // true
 *
 * const doubleWrapped = { success: true, data: { success: true, data: { id: '123' }, timestamp: '...' }, timestamp: '...' };
 * isValidSingleWrap(doubleWrapped) // false
 */
export function isValidSingleWrap(response: any): boolean {
  // Must have success, data, and timestamp at root level
  if (!response || typeof response !== 'object') {
    return false;
  }

  if (!('success' in response) || !('data' in response) || !('timestamp' in response)) {
    return false;
  }

  // success must be boolean
  if (typeof response.success !== 'boolean') {
    return false;
  }

  // timestamp must be string (ISO format)
  if (typeof response.timestamp !== 'string') {
    return false;
  }

  // Data should NOT have nested success/data/timestamp (double-wrap detection)
  if (response.data && typeof response.data === 'object') {
    // Error responses may not have nested success property, that's OK
    // But if data.data exists, that's a sign of double-wrapping
    if ('data' in response.data && typeof response.data.data !== 'undefined') {
      // Check if this looks like a double-wrapped response
      if ('success' in response.data && 'timestamp' in response.data) {
        return false; // Detected double-wrap pattern
      }
    }
  }

  return true;
}

/**
 * Detects if a response is double-wrapped (has nested response structure)
 * @param response The HTTP response body to check
 * @returns true if double-wrapped, false otherwise
 */
export function isDoubleWrapped(response: any): boolean {
  return !!(
    response &&
    typeof response === 'object' &&
    'data' in response &&
    response.data &&
    typeof response.data === 'object' &&
    'success' in response.data &&
    'data' in response.data &&
    'timestamp' in response.data
  );
}

/**
 * Gets the unwrapped data from a response
 * Useful for comparing expected vs actual when double-wrapped
 *
 * @param response The HTTP response (may be single or double-wrapped)
 * @returns The actual data payload, unwrapped as many times as needed
 */
export function getUnwrappedData(response: any): any {
  if (!response || typeof response !== 'object') {
    return response;
  }

  // If it's double-wrapped, unwrap once to get the actual service response
  if (isDoubleWrapped(response)) {
    return response.data.data;
  }

  // If it's single-wrapped (correct), return the data
  if ('data' in response) {
    return response.data;
  }

  return response;
}

/**
 * Validates that data does NOT have nested success/data structure
 * Used to ensure services return raw DTOs, not pre-wrapped responses
 *
 * @param data The data payload from response.data
 * @returns true if data is not wrapped, false if wrapped
 */
export function isDataNotWrapped(data: any): boolean {
  if (!data || typeof data !== 'object') {
    return true;
  }

  // Check if this looks like a wrapped response
  if ('success' in data && 'data' in data && 'timestamp' in data) {
    return false; // Data itself is wrapped
  }

  return true;
}
