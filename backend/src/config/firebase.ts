import admin from "firebase-admin";

// This file initializes the Firebase Admin SDK.
// You can configure credentials using either:
// 1) GOOGLE_APPLICATION_CREDENTIALS env var pointing to a service account JSON
// 2) Explicit service account JSON loaded from an env var or file.

if (!admin.apps.length) {
  admin.initializeApp({
    // For most deployments, using the default credentials via
    // GOOGLE_APPLICATION_CREDENTIALS is recommended, so we don't
    // need to pass anything here.
  });
}

export const db = admin.firestore();

export type Firestore = FirebaseFirestore.Firestore;
