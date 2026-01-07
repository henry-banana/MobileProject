import { Module, Global } from '@nestjs/common';
import { FirebaseService } from './firebase.service';
import { FirestoreService } from './firestore.service';

/**
 * Firebase Module
 *
 * Cung cấp Firebase Admin SDK services cho toàn app.
 * Global module - không cần import lại trong các feature modules.
 * 
 * FIRESTORE token: Inject trực tiếp Firestore instance cho repositories
 */
@Global()
@Module({
  providers: [
    FirebaseService,
    FirestoreService,
    // FIRESTORE token for repositories that extend FirestoreBaseRepository
    {
      provide: 'FIRESTORE',
      useFactory: (firebaseService: FirebaseService) => {
        // Ensure firestore is initialized
        if (!firebaseService.firestore) {
          throw new Error('Firestore not initialized. Make sure FirebaseService.onModuleInit() has run.');
        }
        return firebaseService.firestore;
      },
      inject: [FirebaseService],
    },
  ],
  exports: [FirebaseService, FirestoreService, 'FIRESTORE'],
})
export class FirebaseModule {}

