import { Module, Global } from "@nestjs/common";
import * as admin from "firebase-admin";
import { FirestoreProvider } from "./firebase.provider";

@Global()
@Module({
  providers: [
    {
      provide: "FIREBASE_ADMIN",
      useFactory: () => {
        if (!admin.apps.length) {
          admin.initializeApp();
        }
        return admin;
      },
    },
    FirestoreProvider,
  ],
  exports: ["FIREBASE_ADMIN", FirestoreProvider],
})
export class FirebaseModule {}
