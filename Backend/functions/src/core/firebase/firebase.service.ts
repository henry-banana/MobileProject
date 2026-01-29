import { Injectable, Logger } from '@nestjs/common';
import * as admin from 'firebase-admin';
import * as path from 'path';
import * as fs from 'fs';

/**
 * Firebase Service
 *
 * Singleton service quản lý Firebase Admin SDK initialization.
 * Cung cấp access đến Auth, Firestore, Storage, Messaging.
 */
@Injectable()
export class FirebaseService {
  private readonly logger = new Logger(FirebaseService.name);

  private _app: admin.app.App;
  private _auth: admin.auth.Auth;
  private _firestore: admin.firestore.Firestore;
  private _storage: admin.storage.Storage;
  private _messaging: admin.messaging.Messaging;

  constructor() {
    this.logger.log('FirebaseService constructor called');
    this.logger.log(`FIREBASE_CONFIG present: ${!!process.env.FIREBASE_CONFIG}`);
    this.logger.log(`Current working directory: ${process.cwd()}`);
    this.initialize();
  }

  private initialize() {
    this.logger.log('Starting Firebase initialization...');
    try {
      if (!admin.apps.length) {
        // Check if running in Firebase Functions environment
        if (process.env.FIREBASE_CONFIG) {
          this.logger.log('Detected Cloud Functions environment (FIREBASE_CONFIG present)');
          // Firebase Functions auto-initializes with environment credentials
          // In Cloud Functions v2, need to specify serviceAccountId for signBlob permission
          const projectId = process.env.MY_PROJECT_ID || 'foodappproject-7c136';
          this._app = admin.initializeApp({
            serviceAccountId: `${projectId}@appspot.gserviceaccount.com`,
          });
          this.logger.log('Firebase Admin SDK initialized (Cloud Functions environment)');
        } else {
          this.logger.log('Detected local development environment (no FIREBASE_CONFIG)');
          // Local development only - load service account
          try {
            const serviceAccountPath = this.findServiceAccount();
            this.logger.log(`Service account path found: ${serviceAccountPath || 'null'}`);
            if (serviceAccountPath) {
              this.logger.log(`Attempting to require: ${serviceAccountPath}`);
              // eslint-disable-next-line @typescript-eslint/no-var-requires
              const serviceAccount = require(serviceAccountPath);
              this._app = admin.initializeApp({
                credential: admin.credential.cert(serviceAccount),
                storageBucket: `${serviceAccount.project_id}.firebasestorage.app`,
              });
              this.logger.log(`Firebase Admin SDK initialized with: ${serviceAccountPath}`);
            } else {
              // No service account file found - use default credentials
              this.logger.log('No service account found, using default credentials');
              this._app = admin.initializeApp();
              this.logger.log('Firebase Admin SDK initialized with default credentials');
            }
          } catch (error: any) {
            this.logger.error(`Failed to load service account: ${error.message}`);
            this.logger.error(`Error stack: ${error.stack}`);
            this._app = admin.initializeApp();
            this.logger.log('Firebase Admin SDK initialized with default credentials (fallback)');
          }
        }
      } else {
        this._app = admin.apps[0]!;
        this.logger.log('Firebase Admin SDK already initialized');
      }

      this._auth = admin.auth();
      this._firestore = admin.firestore();
      this._storage = admin.storage();
      this._messaging = admin.messaging();
    } catch (error) {
      this.logger.error('Failed to initialize Firebase Admin SDK', error);
      throw error;
    }
  }

  /**
   * Find service-account.json in possible locations
   */
  private findServiceAccount(): string | null {
    const possiblePaths = [
      path.join(process.cwd(), 'service-account.json'), // Backend/service-account.json
      path.join(process.cwd(), '../service-account.json'), // functions/../service-account.json
      path.join(process.cwd(), '../../service-account.json'), // Fallback
    ];

    for (const p of possiblePaths) {
      try {
        if (fs.existsSync(p)) {
          return p;
        }
      } catch (error: any) {
        // Ignore file system errors in Cloud Functions
        this.logger.debug(`Cannot access ${p}: ${error?.message || error}`);
      }
    }

    return null;
  }

  get app(): admin.app.App {
    return this._app;
  }

  get auth(): admin.auth.Auth {
    return this._auth;
  }

  get firestore(): admin.firestore.Firestore {
    return this._firestore;
  }

  get storage(): admin.storage.Storage {
    return this._storage;
  }

  get messaging(): admin.messaging.Messaging {
    return this._messaging;
  }
}
