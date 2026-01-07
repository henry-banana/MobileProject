/**
 * Set Admin Role Script
 *
 * Script để set role ADMIN cho user đã tồn tại
 *
 * Chạy: npx ts-node scripts/set-admin-role.ts <uid>
 *
 * Ví dụ: npx ts-node scripts/set-admin-role.ts abc123xyz
 */

import * as admin from 'firebase-admin';
import * as path from 'path';

// Initialize Firebase Admin
const serviceAccountPath = path.join(__dirname, '..', 'service-account.json');
const serviceAccount = require(serviceAccountPath);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const auth = admin.auth();
const db = admin.firestore();
const { Timestamp } = admin.firestore;

async function setAdminRole(uid: string) {
  console.log(`🔐 Setting admin role for UID: ${uid}\n`);

  try {
    // 1. Kiểm tra user tồn tại
    const userRecord = await auth.getUser(uid);
    console.log(`  ✅ Found user: ${userRecord.email}`);

    // 2. Kiểm tra current claims
    const currentClaims = userRecord.customClaims || {};
    console.log(`  Current role: ${currentClaims.role || 'none'}`);

    if (currentClaims.role === 'ADMIN') {
      console.log('\n⚠️  User is already an admin!');
      process.exit(0);
    }

    // 3. Set custom claims
    console.log('🔑 Setting admin custom claims...');
    await auth.setCustomUserClaims(uid, {
      ...currentClaims,
      role: 'ADMIN',
    });
    console.log('  ✅ Set role: ADMIN');

    // 4. Update Firestore user document
    console.log('📄 Updating user document...');
    const now = Timestamp.now();
    await db.collection('users').doc(uid).update({
      role: 'ADMIN',
      updatedAt: now,
    });
    console.log('  ✅ Updated user document');

    // 5. Create admin record if not exists
    const adminDoc = await db.collection('admins').doc(uid).get();
    if (!adminDoc.exists) {
      console.log('📋 Creating admin record...');
      await db.collection('admins').doc(uid).set({
        userId: uid,
        email: userRecord.email,
        displayName: userRecord.displayName || null,
        permissions: ['all'],
        createdAt: now,
        updatedAt: now,
      });
      console.log('  ✅ Created admin record');
    }

    console.log('\n🎉 Successfully set admin role!\n');
    console.log('================================');
    console.log(`  Email: ${userRecord.email}`);
    console.log(`  UID: ${uid}`);
    console.log(`  Role: ADMIN`);
    console.log('================================\n');

    console.log('⚠️  User needs to sign out and sign in again for changes to take effect.\n');

  } catch (error: any) {
    if (error.code === 'auth/user-not-found') {
      console.error(`❌ User with UID "${uid}" not found`);
    } else {
      console.error('❌ Error:', error.message);
    }
    throw error;
  }
}

// Parse args and run
const uid = process.argv[2];
if (!uid) {
  console.error('❌ Usage: npx ts-node scripts/set-admin-role.ts <uid>');
  console.error('');
  console.error('Example:');
  console.error('  npx ts-node scripts/set-admin-role.ts abc123xyz');
  process.exit(1);
}

setAdminRole(uid)
  .then(() => {
    console.log('Done!');
    process.exit(0);
  })
  .catch(() => {
    process.exit(1);
  });
