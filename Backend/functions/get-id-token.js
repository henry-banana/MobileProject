/**
 * Get Firebase ID Token for testing protected APIs in Swagger
 *
 * Usage:
 *   node get-id-token.js <email>
 *
 * Example:
 *   node get-id-token.js hoatong1211@gmail.com
 */

const admin = require('firebase-admin');
const serviceAccount = require('../service-account.json');

// Initialize Firebase Admin
if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });
}

async function getIdToken(email) {
  try {
    console.log(`\n🔍 Looking up user: ${email}...`);

    // Get user by email
    const user = await admin.auth().getUserByEmail(email);
    console.log(`✅ Found user: ${user.uid}`);

    // Create custom token
    const customToken = await admin.auth().createCustomToken(user.uid);
    console.log('✅ Custom token created');

    // Get Firebase Web API Key from environment or prompt
    const apiKey = process.env.MY_FIREBASE_API_KEY || 'AIzaSyDbh9zQqMUuPEvELoWOP6Uukl04qIuTWeA';

    // Exchange custom token for ID token (simulating client sign-in)
    const response = await fetch(
      `https://identitytoolkit.googleapis.com/v1/accounts:signInWithCustomToken?key=${apiKey}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          token: customToken,
          returnSecureToken: true,
        }),
      },
    );

    if (!response.ok) {
      const error = await response.json();
      throw new Error(`Firebase API error: ${JSON.stringify(error)}`);
    }

    const data = await response.json();

    console.log('\n' + '='.repeat(80));
    console.log('🔑 ID TOKEN FOR SWAGGER');
    console.log('='.repeat(80));
    console.log('\n📋 Copy this entire line and paste into Swagger Authorize:\n');
    console.log(`Bearer ${data.idToken}`);
    console.log('\n' + '='.repeat(80));
    console.log('⏰ Token expires in: 1 hour');
    console.log('🔄 Re-run this script to get a new token');
    console.log('='.repeat(80) + '\n');

    // Also save to file for easy copy
    const fs = require('fs');
    fs.writeFileSync('id-token.txt', `Bearer ${data.idToken}`);
    console.log('💾 Token saved to: id-token.txt\n');
  } catch (error) {
    console.error('\n❌ Error:', error.message);

    if (error.message.includes('no user record')) {
      console.log('\n💡 Tip: User not found. Make sure to register first:');
      console.log('   POST http://localhost:3000/api/auth/register');
    }

    process.exit(1);
  }
}

// Get email from command line argument
const email = process.argv[2];

if (!email) {
  console.log('\n❌ Usage: node get-id-token.js <email>');
  console.log('\nExample:');
  console.log('  node get-id-token.js hoatong1211@gmail.com\n');
  process.exit(1);
}

// Run
getIdToken(email);
