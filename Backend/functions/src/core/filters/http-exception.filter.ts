import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpException,
  HttpStatus,
  Logger,
} from '@nestjs/common';
import { Response } from 'express';
import { ErrorResponse } from '../interfaces/response.interface';

/**
 * HTTP Exception Filter
 *
 * Global exception filter that formats all HTTP exceptions
 * into a standard error response format.
 *
 * Special handling for:
 * - Validation errors with numeric keys (malformed body detection)
 * - Truncating very long error messages
 */
@Catch()
export class HttpExceptionFilter implements ExceptionFilter {
  private readonly logger = new Logger(HttpExceptionFilter.name);

  catch(exception: unknown, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();
    const request = ctx.getRequest();

    let status = HttpStatus.INTERNAL_SERVER_ERROR;
    let message = 'Internal server error';
    let errorCode = 'INTERNAL_ERROR';
    let details: Record<string, unknown> | undefined;

    if (exception instanceof HttpException) {
      status = exception.getStatus();
      const exceptionResponse = exception.getResponse();

      if (typeof exceptionResponse === 'string') {
        message = exceptionResponse;
      } else if (typeof exceptionResponse === 'object') {
        const res = exceptionResponse as Record<string, unknown>;
        message = (res.message as string) || message;
        errorCode = (res.errorCode as string) || this.getErrorCode(status);
        details = res.details as Record<string, unknown>;

        // Handle class-validator errors
        if (Array.isArray(res.message)) {
          // Detect numeric-key validation errors (malformed body)
          const processedMessage = this.processValidationErrors(res.message);
          message = processedMessage.message;
          errorCode = 'VALIDATION_ERROR';
          if (processedMessage.hint) {
            details = { ...details, hint: processedMessage.hint };
          }
        }
      }
    } else if (exception instanceof Error) {
      message = exception.message;
      this.logger.error(`Unhandled exception: ${exception.message}`, exception.stack);
    }

    const errorResponse: ErrorResponse = {
      success: false,
      message,
      errorCode,
      details,
      timestamp: new Date().toISOString(),
    };

    // Log error (truncate message if too long)
    const truncatedMsg = message.length > 200 ? message.substring(0, 200) + '...' : message;
    this.logger.error(`${request.method} ${request.url} - ${status} - ${truncatedMsg}`);

    response.status(status).json(errorResponse);
  }

  /**
   * Process validation error messages
   * Detects numeric-key errors (malformed body) and provides helpful hints
   */
  private processValidationErrors(messages: string[]): { message: string; hint?: string } {
    // Pattern: "property X should not exist" where X is numeric
    const numericPropertyPattern = /property (\d+) should not exist/;
    const numericErrors = messages.filter(m => numericPropertyPattern.test(m));
    
    // If most errors are numeric property errors, this is a malformed body issue
    if (numericErrors.length > 5 && numericErrors.length / messages.length > 0.5) {
      // Extract first 10 numeric keys for reference
      const numericKeys = numericErrors
        .slice(0, 10)
        .map(m => {
          const match = m.match(numericPropertyPattern);
          return match ? match[1] : '';
        })
        .filter(Boolean);

      this.logger.warn(
        `[MALFORMED_BODY_VALIDATION] Detected ${numericErrors.length} numeric property errors. ` +
        `This usually means the request body was incorrectly encoded. ` +
        `First 10 keys: ${numericKeys.join(', ')}`
      );

      return {
        message: `Invalid request body encoding. Received ${numericErrors.length} unexpected numeric properties (${numericKeys.slice(0, 5).join(', ')}...). ` +
          `Please check your Content-Type header and request body format.`,
        hint: `For file uploads, use Content-Type: multipart/form-data (do NOT set manually with FormData - let the browser/client set it). ` +
          `For JSON data, use Content-Type: application/json with JSON.stringify(body). ` +
          `Do not manually set Content-Type when using FormData as it needs the boundary parameter.`
      };
    }

    // Normal validation errors - truncate if too many
    if (messages.length > 10) {
      const truncated = messages.slice(0, 10);
      return {
        message: `${truncated.join(', ')} ... and ${messages.length - 10} more errors`
      };
    }

    return { message: messages.join(', ') };
  }

  private getErrorCode(status: number): string {
    const errorCodes: Record<number, string> = {
      400: 'BAD_REQUEST',
      401: 'UNAUTHORIZED',
      403: 'FORBIDDEN',
      404: 'NOT_FOUND',
      409: 'CONFLICT',
      412: 'PRECONDITION_FAILED',
      422: 'UNPROCESSABLE_ENTITY',
      429: 'TOO_MANY_REQUESTS',
      500: 'INTERNAL_ERROR',
    };
    return errorCodes[status] || 'UNKNOWN_ERROR';
  }
}
