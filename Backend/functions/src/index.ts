/**
 * Firebase Functions Entry Point
 *
 * File này export NestJS app như một Firebase Cloud Function.
 * Sử dụng lazy initialization để optimize cold start.
 */

import { NestFactory } from '@nestjs/core';
import { onRequest } from 'firebase-functions/v2/https';
import { onSchedule } from 'firebase-functions/v2/scheduler';
import * as admin from 'firebase-admin';
import express from 'express';
import { ValidationPipe, INestApplication } from '@nestjs/common';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { AppModule } from './app.module';
import { runOrphanFilesCleanup } from './jobs/orphanFilesCleanup.job';

/**
 * Bootstrap NestJS application
 */
const createNestServer = async () => {
  // Let NestJS create and manage the Express instance internally
  const app = await NestFactory.create(AppModule, {
    logger: ['error', 'warn', 'log'],
  });

  // Enable CORS
  app.enableCors({
    origin: true,
    credentials: true,
  });

  // Global validation pipe
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
      transformOptions: {
        // NOTE: Disabled enableImplicitConversion to prevent implicit boolean conversion
        // from interfering with @Transform decorators. Query params like ?read=false
        // were being converted to true before @Transform could see the string value.
        enableImplicitConversion: false,
      },
    }),
  );

  // No global prefix needed - function name is already 'api'
  // Routes: /api/health, /api/categories, etc.

  // Swagger setup
  const config = new DocumentBuilder()
    .setTitle('KTX Delivery API')
    .setDescription('REST API cho ứng dụng giao hàng KTX')
    .setVersion('2.0')
    .addServer('/api', 'Production (Cloud Functions)')
    .addServer('http://localhost:3000/api', 'Local Development')
    .addBearerAuth(
      {
        type: 'http',
        scheme: 'bearer',
        bearerFormat: 'Firebase ID Token',
        description: 'Enter Firebase ID Token (get from Firebase Auth client SDK)',
      },
      'firebase-auth',
    )
    .addTag('Auth', 'Authentication & Authorization')
    .addTag('Admin - Categories', 'Admin: Category management')
    .addTag('Admin - Users', 'Admin: User management')
    .addTag('Admin - Payouts', 'Admin: Payout approval')
    .addTag('Admin - Shops', 'Admin: Shop management')
    .addTag('Admin - Dashboard', 'Admin: Statistics')
    .addTag('Categories', 'Public: Product categories')
    .build();

  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('docs', app, document, {
    swaggerOptions: {
      persistAuthorization: true, // Remember token in browser
      tagsSorter: 'alpha',
      operationsSorter: 'alpha',
    },
  });

  await app.init();
  
  // Get the underlying Express instance
  const expressInstance = app.getHttpAdapter().getInstance();
  return { app, expressInstance };
};

// Lazy initialization - cache to avoid cold starts
let cachedServer: INestApplication | null = null;
let cachedExpressInstance: express.Express | null = null;

/**
 * Main API endpoint
 *
 * URL: https://{region}-{project}.cloudfunctions.net/api
 * Local: http://127.0.0.1:5001/{project}/asia-southeast1/api
 */
export const api = onRequest(
  {
    region: 'asia-southeast1',
    memory: '512MiB',
    timeoutSeconds: 60,
    minInstances: 0,
    maxInstances: 100,
  },
  async (req, res) => {
    if (!cachedServer) {
      const { app, expressInstance } = await createNestServer();
      cachedServer = app;
      cachedExpressInstance = expressInstance;
    }
    cachedExpressInstance!(req, res);
  },
);

/**
 * Scheduled job: Orphan Avatar Files Cleanup
 *
 * Runs every 10 minutes to retry deletion of orphaned avatar files.
 * Queries `orphanFiles` collection for AVATAR records with status PENDING
 * and retries deletion with exponential backoff.
 *
 * Schedule: every 10 minutes (cron: every 10 minutes)
 * Memory: 256 MiB (only needs Firestore/Storage access)
 * Timeout: 30 seconds (cleanup typically completes in less than 10 seconds)
 */
export const orphanFilesCleanup = onSchedule(
  {
    region: 'asia-southeast1',
    schedule: 'every 10 minutes',
    memory: '256MiB',
    timeoutSeconds: 30,
  },
  async () => {
    // Initialize Firebase Admin SDK if not already done
    if (!admin.apps.length) {
      admin.initializeApp();
    }

    const firestore = admin.firestore();
    await runOrphanFilesCleanup(firestore);
  },
);
