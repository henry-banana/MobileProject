import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { SuccessResponse } from '../interfaces/response.interface';
import { serializeTimestamps } from '../../modules/orders/utils/timestamp.serializer';

/**
 * Transform Interceptor
 *
 * Wraps all successful responses in a standard format.
 * For 204 No Content responses, returns undefined (no body).
 *
 * Also serializes Firestore Timestamp objects to ISO-8601 strings for order responses.
 */
@Injectable()
export class TransformInterceptor<T> implements NestInterceptor<T, SuccessResponse<T>> {
  intercept(context: ExecutionContext, next: CallHandler): Observable<SuccessResponse<T>> {
    const response = context.switchToHttp().getResponse();
    const statusCode = response.statusCode;
    const request = context.switchToHttp().getRequest();
    const isOrdersEndpoint = request.url?.includes('/orders');

    return next.handle().pipe(
      map((data) => {
        // For 204 No Content, don't wrap the response
        if (statusCode === 204) {
          return undefined as any;
        }

        // Serialize timestamps for order endpoints
        let processedData = data;
        if (isOrdersEndpoint && data) {
          processedData = serializeTimestamps(data);
        }

        return {
          success: true as const,
          data: processedData,
          timestamp: new Date().toISOString(),
        };
      }),
    );
  }
}
