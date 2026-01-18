/**
 * Pagination Response Validation Utilities
 *
 * Ensures paginated responses follow the standardized format:
 * { page, limit, total, totalPages }
 *
 * No hasNext, hasPrev, or extra pagination metadata fields.
 */

export interface PaginationMeta {
  page: number;
  limit: number;
  total: number;
  totalPages: number;
}

/**
 * Validates that a paginated response object contains only the required
 * pagination metadata fields and no extra fields like hasNext, hasPrev.
 *
 * @param meta - The pagination metadata to validate
 * @throws Error if validation fails
 *
 * @example
 * const response = { page: 1, limit: 10, total: 100, totalPages: 10 };
 * validatePaginationMeta(response); // passes
 *
 * const invalid = { page: 1, limit: 10, total: 100, totalPages: 10, hasNext: true };
 * validatePaginationMeta(invalid); // throws error
 */
export function validatePaginationMeta(meta: any): void {
  if (!meta) {
    throw new Error('Pagination meta is missing');
  }

  const requiredFields = ['page', 'limit', 'total', 'totalPages'];
  const actualKeys = Object.keys(meta).sort();
  const expectedKeys = requiredFields.sort();

  // Check for missing required fields
  for (const field of requiredFields) {
    if (meta[field] === undefined || meta[field] === null) {
      throw new Error(`Required pagination field missing: ${field}`);
    }
  }

  // Check that pagination meta only contains expected fields
  if (actualKeys.join(',') !== expectedKeys.join(',')) {
    const extraFields = actualKeys.filter((key) => !requiredFields.includes(key));
    const missingFields = requiredFields.filter((key) => !actualKeys.includes(key));

    if (extraFields.length > 0) {
      throw new Error(
        `Pagination meta contains unexpected fields: ${extraFields.join(', ')}. ` +
        `Remove hasNext, hasPrev, and other extra fields. Only allow: ${requiredFields.join(', ')}`,
      );
    }

    if (missingFields.length > 0) {
      throw new Error(
        `Pagination meta is missing required fields: ${missingFields.join(', ')}`,
      );
    }
  }

  // Validate field types and values
  if (typeof meta.page !== 'number' || meta.page < 1) {
    throw new Error(`Invalid page: must be a number >= 1, got ${meta.page}`);
  }

  if (typeof meta.limit !== 'number' || meta.limit < 1) {
    throw new Error(`Invalid limit: must be a number >= 1, got ${meta.limit}`);
  }

  if (typeof meta.total !== 'number' || meta.total < 0) {
    throw new Error(`Invalid total: must be a number >= 0, got ${meta.total}`);
  }

  if (typeof meta.totalPages !== 'number' || meta.totalPages < 0) {
    throw new Error(`Invalid totalPages: must be a number >= 0, got ${meta.totalPages}`);
  }

  // Validate logical consistency
  const expectedTotalPages = meta.total === 0 ? 0 : Math.ceil(meta.total / meta.limit);
  if (meta.totalPages !== expectedTotalPages) {
    throw new Error(
      `totalPages mismatch: expected ${expectedTotalPages} (ceil(${meta.total}/${meta.limit})), ` +
      `got ${meta.totalPages}`,
    );
  }

  if (meta.page > meta.totalPages && meta.total > 0) {
    throw new Error(`page (${meta.page}) cannot exceed totalPages (${meta.totalPages})`);
  }
}

/**
 * Validates the complete paginated response structure.
 *
 * @param response - The full response object
 * @param dataFieldName - Name of the data field (default: 'data')
 * @param itemsFieldName - Name of the items array within data (e.g., 'orders', 'groups')
 *
 * @example
 * const response = {
 *   success: true,
 *   data: {
 *     orders: [...],
 *     page: 1,
 *     limit: 10,
 *     total: 100,
 *     totalPages: 10
 *   },
 *   timestamp: '2024-01-18T...'
 * };
 * validatePaginatedResponse(response, 'data', 'orders');
 */
export function validatePaginatedResponse(
  response: any,
  dataFieldName: string = 'data',
  itemsFieldName: string = 'items',
): void {
  if (!response) {
    throw new Error('Response is missing');
  }

  if (response.success !== true) {
    throw new Error(`Response.success must be true, got ${response.success}`);
  }

  const data = response[dataFieldName];
  if (!data) {
    throw new Error(`Response.${dataFieldName} is missing`);
  }

  if (!Array.isArray(data[itemsFieldName])) {
    throw new Error(`Response.${dataFieldName}.${itemsFieldName} must be an array`);
  }

  // Extract pagination meta (all fields except itemsFieldName)
  const { [itemsFieldName]: items, ...paginationMeta } = data;

  validatePaginationMeta(paginationMeta);

  // Validate items count consistency
  if (items.length > paginationMeta.limit) {
    throw new Error(
      `Item count (${items.length}) exceeds limit (${paginationMeta.limit})`,
    );
  }
}

/**
 * Test helper: Asserts a paginated response has the correct structure
 *
 * @example
 * const result = await service.getOrders(customerId);
 * expectPaginationStructure(result, 'orders', { total: 42, page: 1, limit: 10 });
 */
export function expectPaginationStructure(
  data: any,
  itemsFieldName: string,
  expectedValues?: Partial<PaginationMeta>,
): void {
  if (!data[itemsFieldName]) {
    throw new Error(`Field '${itemsFieldName}' not found in response`);
  }

  const { [itemsFieldName]: items, ...meta } = data;

  // Validate structure
  validatePaginationMeta(meta);

  // Validate expected values if provided
  if (expectedValues) {
    for (const [key, value] of Object.entries(expectedValues)) {
      if (meta[key as keyof PaginationMeta] !== value) {
        throw new Error(
          `${key} mismatch: expected ${value}, got ${meta[key as keyof PaginationMeta]}`,
        );
      }
    }
  }
}
