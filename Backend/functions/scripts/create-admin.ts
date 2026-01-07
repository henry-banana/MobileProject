/**
 * Create Admin User Script
 *
 * Creates an admin user with ADMIN role.
 * This should be run once to create the first admin.
 *
 * Usage:
 *   npx ts-node scripts/create-admin.ts <email> <password> <displayName>
 *
 * Example:
 *   npx ts-node scripts/create-admin.ts admin@ktx.com Admin123! "Admin User"
 */

import * as admin from 'firebase-admin';
import * as path from 'path';
import * as dotenv from 'dotenv';

// Load environment variables
dotenv.config();

// Initialize Firebase Admin
const serviceAccountPath = path.resolve(__dirname, '../../service-account.json');

if (!admin.apps.length) {
  try {
    const serviceAccount = require(serviceAccountPath);
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
    });
    console.log('✅ Firebase Admin initialized');
  } catch (error) {
    console.error('❌ Error loading service account:', error);
    process.exit(1);
  }
}

const db = admin.firestore();

async function createAdmin(email: string, password: string, displayName: string) {
  console.log('\n👨‍💼 Creating admin user...\n');

  try {
    // Create Firebase Auth user
    const userRecord = await admin.auth().createUser({
      email,
      password,
      displayName,
      emailVerified: true, // Auto-verify admin
    });

    console.log(`✅ Created Firebase Auth user: ${userRecord.uid}`);

    // Set custom claims (ADMIN role)
    await admin.auth().setCustomUserClaims(userRecord.uid, {
      role: 'ADMIN',
    });

    console.log(`✅ Set custom claims: role=ADMIN`);

    // Create Firestore user document
    await db.collection('users').doc(userRecord.uid).set({
      id: userRecord.uid,
      email: userRecord.email,
      displayName: userRecord.displayName,
      role: 'ADMIN',
      status: 'ACTIVE',
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    console.log(`✅ Created Firestore user document`);

    console.log('\n✨ Admin user created successfully!\n');
    console.log('📋 Details:');
    console.log(`   UID:   ${userRecord.uid}`);
    console.log(`   Email: ${userRecord.email}`);
    console.log(`   Name:  ${userRecord.displayName}`);
    console.log(`   Role:  ADMIN\n`);

    process.exit(0);
  } catch (error: any) {
    console.error('\n❌ Error creating admin user:', error.message);
    process.exit(1);
  }
}

// Parse command line arguments
const args = process.argv.slice(2);

if (args.length < 3) {
  console.error('\n❌ Missing arguments\n');
  console.log('Usage:');
  console.log('  npx ts-node scripts/create-admin.ts <email> <password> <displayName>\n');
  console.log('Example:');
  console.log('  npx ts-node scripts/create-admin.ts admin@ktx.com Admin123! "Admin User"\n');
  process.exit(1);
}

const [email, password, displayName] = args;

// Validate inputs
if (!email.includes('@')) {
  console.error('\n❌ Invalid email format\n');
  process.exit(1);
}

if (password.length < 6) {
  console.error('\n❌ Password must be at least 6 characters\n');
  process.exit(1);
}

// Run the script
createAdmin(email, password, displayName);
