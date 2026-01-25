import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { serializeTimestamps } from '../utils/timestamp.serializer';

/**
 * Response Wrapper Interceptor
 *
 * Wraps all OrdersController responses in standard format:
 * { success: boolean, data: T, timestamp: string }
 *
 * Also serializes all timestamp fields to ISO-8601 strings.
 *
 * Applied to: OrdersController (Customer endpoints only)
 */
@Injectable()
export class ResponseWrapperInterceptor implements NestInterceptor {
  intercept(_context: ExecutionContext, next: CallHandler): Observable<any> {
    return next.handle().pipe(
      map((data) => {
        // Serialize timestamps first (convert Firestore objects to ISO strings)
        const serialized = serializeTimestamps(data);

        // Return wrapped response
        return {
          success: true,
          data: serialized,
          timestamp: new Date().toISOString(),
        };
      }),
    );
  }
}
