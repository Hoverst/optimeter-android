import admin from "firebase-admin";


const serviceAccount = require("../../service-account.json");

if (!admin.apps.length) {
  admin.initializeApp({
    // 2. Явно передаємо цей ключ у Firebase
    credential: admin.credential.cert(serviceAccount)
  });
}

export const db = admin.firestore();
export type Firestore = FirebaseFirestore.Firestore;