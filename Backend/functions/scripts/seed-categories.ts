/**
 * Seed Categories Script
 *
 * Seeds initial categories data to Firestore.
 * Can be run multiple times safely (idempotent).
 *
 * Usage:
 *   npx ts-node scripts/seed-categories.ts
 *   npx ts-node scripts/seed-categories.ts --force  (để ghi đè)
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

// Categories to seed - theo entity mới
const categories = [
  {
    name: 'Cơm',
    slug: 'com',
    description: 'Các món cơm đa dạng, từ cơm tấm đến cơm văn phòng',
    icon: 'rice_bowl',
    displayOrder: 1,
    status: 'active',
  },
  {
    name: 'Phở & Bún',
    slug: 'pho-bun',
    description: 'Phở, bún bò, bún riêu và các món bún ngon',
    icon: 'ramen_dining',
    displayOrder: 2,
    status: 'active',
  },
  {
    name: 'Mì',
    slug: 'mi',
    description: 'Mì xào, mì trộn, mì Quảng và các loại mì khác',
    icon: 'dinner_dining',
    displayOrder: 3,
    status: 'active',
  },
  {
    name: 'Bánh mì',
    slug: 'banh-mi',
    description: 'Bánh mì thịt, bánh mì chả cá, sandwich',
    icon: 'bakery_dining',
    displayOrder: 4,
    status: 'active',
  },
  {
    name: 'Đồ ăn vặt',
    slug: 'do-an-vat',
    description: 'Snack, xúc xích, khoai tây chiên, gà rán',
    icon: 'fastfood',
    displayOrder: 5,
    status: 'active',
  },
  {
    name: 'Trà sữa & Đồ uống',
    slug: 'tra-sua-do-uong',
    description: 'Trà sữa, cà phê, nước ép, sinh tố',
    icon: 'local_cafe',
    displayOrder: 6,
    status: 'active',
  },
  {
    name: 'Chè & Tráng miệng',
    slug: 'che-trang-mieng',
    description: 'Chè, kem, bánh ngọt, trái cây',
    icon: 'icecream',
    displayOrder: 7,
    status: 'active',
  },
  {
    name: 'Đồ chay',
    slug: 'do-chay',
    description: 'Các món chay thanh đạm',
    icon: 'eco',
    displayOrder: 8,
    status: 'active',
  },
  {
    name: 'Lẩu & Nướng',
    slug: 'lau-nuong',
    description: 'Lẩu, đồ nướng BBQ, buffet',
    icon: 'outdoor_grill',
    displayOrder: 9,
    status: 'active',
  },
  {
    name: 'Khác',
    slug: 'khac',
    description: 'Các món ăn khác không thuộc danh mục trên',
    icon: 'restaurant',
    displayOrder: 100,
    status: 'active',
  },
];

async function seedCategories() {
  console.log('\n🌱 Starting categories seed...\n');

  const collection = db.collection('categories');
  let created = 0;
  let skipped = 0;

  for (const category of categories) {
    // Check if category already exists by slug
    const existing = await collection.where('slug', '==', category.slug).limit(1).get();

    if (!existing.empty) {
      console.log(`⏭️  Skipped: "${category.name}" (slug already exists)`);
      skipped++;
      continue;
    }

    // Create new category
    const docRef = collection.doc();
    await docRef.set({
      ...category,
      productCount: 0,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    console.log(`✅ Created: "${category.name}" (ID: ${docRef.id})`);
    created++;
  }

  console.log('\n📊 Seed Summary:');
  console.log(`   Created: ${created}`);
  console.log(`   Skipped: ${skipped}`);
  console.log(`   Total:   ${categories.length}\n`);
  console.log('✨ Seed completed!\n');

  process.exit(0);
}

// Run the seed
seedCategories().catch((error) => {
  console.error('\n❌ Error seeding categories:', error);
  process.exit(1);
});
