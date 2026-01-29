import { Injectable, NestMiddleware, Logger } from '@nestjs/common';
import { Request, Response, NextFunction } from 'express';
import Busboy from 'busboy';

/**
 * RawBodyMiddleware
 *
 * Xử lý multipart/form-data trong Cloud Functions v2 (Cloud Run).
 *
 * Vấn đề:
 * - Cloud Functions đã parse body và lưu vào req.rawBody
 * - Stream gốc đã bị consume, Multer không thể đọc
 *
 * Giải pháp:
 * - Parse req.rawBody bằng Busboy
 * - Attach file vào req.file để controller có thể sử dụng
 */
@Injectable()
export class RawBodyMiddleware implements NestMiddleware {
  private readonly logger = new Logger(RawBodyMiddleware.name);

  use(req: Request, _res: Response, next: NextFunction) {
    // Chỉ xử lý multipart/form-data
    const contentType = req.headers['content-type'] || '';
    if (!contentType.includes('multipart/form-data')) {
      return next();
    }

    // Kiểm tra xem có rawBody không (Cloud Functions environment)
    const rawBody = (req as any).rawBody;
    if (!rawBody) {
      // Không có rawBody, để Multer xử lý bình thường (local dev)
      return next();
    }

    this.logger.log(
      `Processing multipart/form-data with rawBody, size: ${rawBody.length} bytes`,
    );

    try {
      const busboy = Busboy({ headers: req.headers });
      const fields: Record<string, string> = {};
      let fileData: {
        fieldname: string;
        originalname: string;
        encoding: string;
        mimetype: string;
        buffer: Buffer;
        size: number;
      } | null = null;

      // Handle text fields
      busboy.on('field', (fieldname: string, val: string) => {
        fields[fieldname] = val;
      });

      // Handle file
      busboy.on(
        'file',
        (
          fieldname: string,
          file: NodeJS.ReadableStream,
          info: { filename: string; encoding: string; mimeType: string },
        ) => {
          const { filename, encoding, mimeType } = info;
          const chunks: Buffer[] = [];

          file.on('data', (data: Buffer) => {
            chunks.push(data);
          });

          file.on('end', () => {
            const buffer = Buffer.concat(chunks);
            fileData = {
              fieldname,
              originalname: filename,
              encoding,
              mimetype: mimeType,
              buffer,
              size: buffer.length,
            };
            this.logger.debug(
              `File received: ${filename}, size: ${buffer.length} bytes, type: ${mimeType}`,
            );
          });
        },
      );

      busboy.on('finish', () => {
        // Merge parsed fields with existing body (don't completely replace)
        (req as any).body = { ...(req as any).body, ...fields };
        if (fileData) {
          (req as any).file = fileData;
          this.logger.log(
            `File parsed successfully: ${fileData.originalname}, ${fileData.size} bytes, ${fileData.mimetype}`,
          );
        } else {
          this.logger.warn('No file found in multipart request');
        }
        next();
      });

      busboy.on('error', (error: Error) => {
        this.logger.error('Busboy parsing error:', error);
        next(error);
      });

      // Write rawBody to busboy
      busboy.end(rawBody);
    } catch (error) {
      this.logger.error('Failed to parse multipart data:', error);
      next(error);
    }
  }
}
