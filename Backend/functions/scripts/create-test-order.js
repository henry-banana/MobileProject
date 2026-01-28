/**
 * Create test DELIVERED order for review testing
 * Usage: node scripts/create-test-order.js
 */

const admin = require('firebase-admin');
const serviceAccount = require('../../service-account.json');

// Initialize Firebase Admin
if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

const db = admin.firestore();

async function createTestOrder() {
  console.log('Creating test order for review testing...\n');

  // Customer info
  const customerId = 'ujQm1FYhRpPLtdfKxTMPW2M1Nrl1'; // testcustomer999@test.com
  
  // Shop info  
  const shopId = 'nzIfau9GtqIPyWkmLyku'; // Hiệp Thập Cẩm
  const shopName = 'Hiệp Thập Cẩm';
  
  // Product info
  const productId = 'i9STmaAMR77WN9jLdBEN';
  const productName = 'Mỳ ý';
  const price = 35000;

  // Create order with DELIVERED status
  const orderRef = db.collection('orders').doc();
  const orderId = orderRef.id;
  const orderNumber = `KTX-${Date.now().toString(36).toUpperCase()}`;

  const orderData = {
    id: orderId,
    orderNumber: orderNumber,
    customerId: customerId,
    customerName: 'Test Customer 999',
    customerPhone: '0901234567',
    shopId: shopId,
    shopName: shopName,
    items: [{
      productId: productId,
      productName: productName,
      quantity: 2,
      price: price,
      total: price * 2
    }],
    subtotal: price * 2,
    shipFee: 5000,
    discount: 0,
    total: price * 2 + 5000,
    deliveryAddress: {
      fullAddress: 'KTX Khu B - Tòa B5',
      building: 'B5',
      room: '101',
      note: 'Test order for review'
    },
    status: 'DELIVERED',
    paymentMethod: 'COD',
    paymentStatus: 'PAID',
    shipperId: null,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    deliveredAt: admin.firestore.FieldValue.serverTimestamp()
  };

  await orderRef.set(orderData);

  console.log('✅ Test order created!');
  console.log(`   Order ID: ${orderId}`);
  console.log(`   Order Number: ${orderNumber}`);
  console.log(`   Customer: testcustomer999@test.com`);
  console.log(`   Shop: ${shopName}`);
  console.log(`   Status: DELIVERED`);
  console.log(`   Total: ${orderData.total.toLocaleString('vi-VN')}đ`);
  console.log('\n🎉 You can now test creating a review for this order!');
  console.log(`\nOrder ID to use: ${orderId}\n`);
}

createTestOrder()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error('❌ Error:', error);
    process.exit(1);
  });
