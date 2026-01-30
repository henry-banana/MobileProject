import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
  BadRequestException,
  Logger,
} from '@nestjs/common';
import { Observable } from 'rxjs';

/**
 * BodyValidationInterceptor
 *
 * Detects and handles malformed request bodies BEFORE they reach validation.
 * 
 * Problem: When client sends incorrect Content-Type with FormData, or when
 * a JSON string gets character-split into {0: '{', 1: '"', 2: 'n', ...},
 * validation produces hundreds of "property X should not exist" errors.
 *
 * Solution: Detect this pattern early and return a helpful error message.
 *
 * Patterns detected:
 * 1. Body with mostly numeric keys (0, 1, 2, ...) - likely char-split JSON
 * 2. Body that is an array when object expected
 */
@Injectable()
export class BodyValidationInterceptor implements NestInterceptor {
  private readonly logger = new Logger(BodyValidationInterceptor.name);

  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const request = context.switchToHttp().getRequest();
    const body = request.body;
    const contentType = request.headers['content-type'] || '';

    // Skip if no body
    if (!body) {
      return next.handle();
    }

    // Skip validation for multipart/form-data with proper boundary
    // The RawBodyMiddleware should have already parsed it correctly
    // If body still has numeric keys, it means middleware didn't run or failed
    if (contentType.includes('multipart/form-data') && contentType.includes('boundary=')) {
      // Check if body looks properly parsed (has expected field names, not numeric keys)
      const keys = Object.keys(body);
      const hasOnlyNumericKeys = keys.length > 0 && keys.every(k => /^\d+$/.test(k));
      
      if (!hasOnlyNumericKeys) {
        // Body looks properly parsed
        this.logger.debug(`[BODY_VALIDATION] Multipart body parsed correctly: ${keys.join(', ')}`);
        return next.handle();
      }
      
      // Body has numeric keys even after middleware - this is a server-side issue
      this.logger.error(
        `[BODY_VALIDATION] Multipart body still has numeric keys after middleware. ` +
        `This indicates RawBodyMiddleware failed to parse. Keys: ${keys.slice(0, 10).join(', ')}...`
      );
      
      throw new BadRequestException({
        message: 'Server failed to parse multipart request. Please try again or contact support.',
        errorCode: 'MULTIPART_PARSE_ERROR',
        details: {
          hint: 'This is likely a server-side issue. The multipart body was not parsed correctly by the middleware.',
        },
      });
    }

    // Check 1: Body is an array when object expected
    if (Array.isArray(body)) {
      this.logger.warn(
        `[BODY_VALIDATION] Request body is array, expected object. ` +
        `Content-Type: ${contentType}, Path: ${request.path}`
      );
      throw new BadRequestException({
        message: 'Invalid request body: Expected JSON object, received array.',
        errorCode: 'INVALID_BODY_FORMAT',
        details: {
          hint: 'Ensure you are sending a JSON object, not an array.',
          contentType: contentType,
        },
      });
    }

    // Check 2: Body has numeric keys (char-split JSON or wrong serialization)
    if (typeof body === 'object' && this.hasNumericKeys(body)) {
      const keys = Object.keys(body);
      
      // Try to reconstruct if it looks like char-split JSON
      const reconstructed = this.tryReconstructJson(body);
      if (reconstructed) {
        this.logger.log(
          `[BODY_VALIDATION] Reconstructed JSON from char-split body. Path: ${request.path}`
        );
        request.body = reconstructed;
        return next.handle();
      }

      // Cannot recover - throw helpful error
      this.logger.error(
        `[BODY_VALIDATION] Malformed body with ${keys.length} numeric keys. ` +
        `Content-Type: ${contentType}, Path: ${request.path}, ` +
        `First keys: ${keys.slice(0, 10).join(', ')}`
      );

      throw new BadRequestException({
        message: `Invalid request body encoding. Body appears to be incorrectly serialized ` +
          `(found ${keys.length} numeric-indexed properties like "0", "1", "2"...). ` +
          `This usually happens when Content-Type doesn't match the body format.`,
        errorCode: 'INVALID_BODY_ENCODING',
        details: {
          hint: this.getHintForContentType(contentType),
          receivedContentType: contentType,
          sampleKeys: keys.slice(0, 10),
        },
      });
    }

    return next.handle();
  }

  /**
   * Check if body has mostly numeric keys
   */
  private hasNumericKeys(obj: any): boolean {
    if (!obj || typeof obj !== 'object') return false;
    const keys = Object.keys(obj);
    if (keys.length < 10) return false; // Only flag if many keys
    
    const numericKeyCount = keys.filter(k => /^\d+$/.test(k)).length;
    return numericKeyCount > 10 && numericKeyCount / keys.length > 0.7;
  }

  /**
   * Try to reconstruct JSON from char-split body
   */
  private tryReconstructJson(obj: any): any | null {
    try {
      const keys = Object.keys(obj);
      // Ensure keys are sequential numbers
      const sortedKeys = keys
        .filter(k => /^\d+$/.test(k))
        .sort((a, b) => parseInt(a) - parseInt(b));
      
      if (sortedKeys.length !== keys.length) return null;
      
      const chars = sortedKeys.map(k => obj[k]);
      const jsonStr = chars.join('');
      
      // Try parsing as JSON
      const parsed = JSON.parse(jsonStr);
      
      // Only accept if result is an object (not array, string, etc.)
      if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
        return parsed;
      }
      return null;
    } catch {
      return null;
    }
  }

  /**
   * Get appropriate hint based on Content-Type
   */
  private getHintForContentType(contentType: string): string {
    if (contentType.includes('multipart/form-data')) {
      return 'For multipart/form-data: Do NOT manually set Content-Type header when using FormData. ' +
        'The browser/HTTP client will automatically set it with the correct boundary. ' +
        'Example (JavaScript): fetch(url, { method: "POST", body: formData }) without Content-Type header.';
    }
    
    if (contentType.includes('application/json')) {
      return 'For application/json: Ensure body is JSON.stringify(data), not FormData or raw object. ' +
        'Example: fetch(url, { method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify(data) })';
    }
    
    if (contentType.includes('application/x-www-form-urlencoded')) {
      return 'For form-urlencoded: Use URLSearchParams or similar encoding. ' +
        'For file uploads, use multipart/form-data instead.';
    }
    
    return 'Ensure Content-Type matches your body format: ' +
      'multipart/form-data for file uploads (FormData), ' +
      'application/json for JSON (JSON.stringify).';
  }
}
