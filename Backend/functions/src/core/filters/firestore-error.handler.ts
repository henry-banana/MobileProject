import { HttpException, HttpStatus, Logger } from '@nestjs/common';

/**
 * Firestore Error Handler
 * Maps Firestore error codes to appropriate HTTP responses
 * Handles special cases like missing indexes
 */
export class FirestoreErrorHandler {
  private static readonly logger = new Logger(FirestoreErrorHandler.name);

  /**
   * Handle Firestore errors and convert to appropriate HTTP exceptions
   * Special handling for FAILED_PRECONDITION (missing indexes)
   */
  static handle(error: any): never {
    // Check if it's a Firestore error
    if (error.code) {
      const code = error.code.toUpperCase();

      // Handle FAILED_PRECONDITION for missing indexes
      if (code === 'FAILED_PRECONDITION' || code === 'FAILED_PRECONDITION') {
        if (error.message?.includes('requires an index')) {
          const indexUrl = this.extractIndexUrl(error.message) || 
            'https://console.firebase.google.com/firestore/indexes';
          
          this.logger.warn(
            `Firestore missing index: ${error.message}. Index URL: ${indexUrl}`
          );

          throw new HttpException(
            {
              success: false,
              message: `Query requires a Firestore index. Please create the index at: ${indexUrl}`,
              errorCode: 'ORDER_INDEX_REQUIRED',
              details: {
                firestoreMessage: error.message,
                indexUrl,
              },
              timestamp: new Date().toISOString(),
            },
            HttpStatus.PRECONDITION_FAILED // 412
          );
        }
      }

      // Handle other known Firestore errors
      const statusMap: Record<string, number> = {
        'INVALID_ARGUMENT': HttpStatus.BAD_REQUEST,
        'NOT_FOUND': HttpStatus.NOT_FOUND,
        'ALREADY_EXISTS': HttpStatus.CONFLICT,
        'PERMISSION_DENIED': HttpStatus.FORBIDDEN,
        'RESOURCE_EXHAUSTED': HttpStatus.TOO_MANY_REQUESTS,
        'DEADLINE_EXCEEDED': HttpStatus.REQUEST_TIMEOUT,
      };

      const status = statusMap[code] || HttpStatus.INTERNAL_SERVER_ERROR;
      
      this.logger.error(
        `Firestore error [${code}]: ${error.message}`
      );

      throw new HttpException(
        {
          success: false,
          message: error.message || 'Database operation failed',
          errorCode: `FIRESTORE_${code}`,
          timestamp: new Date().toISOString(),
        },
        status
      );
    }

    // If not a Firestore error, re-throw
    throw error;
  }

  /**
   * Extract Firebase Console index URL from error message
   * Firebase provides a clickable link in the error message
   */
  private static extractIndexUrl(message: string): string | null {
    // Firebase error messages contain URLs in various formats
    // Example: "The query requires an index. You can create it here: https://console.firebase.google.com/..."
    const urlMatch = message.match(/(https:\/\/console\.firebase\.google\.com\/[^\s)]+)/);
    return urlMatch ? urlMatch[1] : null;
  }
}
