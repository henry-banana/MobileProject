import * as admin from "firebase-admin";
export declare const FIRESTORE_PROVIDER = "FIRESTORE";
export declare const FirestoreProvider: {
    provide: string;
    useFactory: () => admin.firestore.Firestore;
};
