/**
 * Seed FAQs Script for KTX Delivery Chatbot
 *
 * This script seeds ~100 FAQ entries into Firestore for AI chatbot training.
 * Run: npx ts-node scripts/seed-faqs.ts
 */

import * as admin from 'firebase-admin';
import * as dotenv from 'dotenv';
import * as path from 'path';

// Load environment variables
dotenv.config({ path: path.resolve(__dirname, '../.env') });

// Initialize Firebase Admin if not already initialized
if (!admin.apps.length) {
  const serviceAccountPath = process.env.GOOGLE_APPLICATION_CREDENTIALS
    || path.resolve(__dirname, '../../service-account.json');

  admin.initializeApp({
    credential: admin.credential.cert(require(serviceAccountPath)),
  });
}

const db = admin.firestore();

interface FAQ {
  question: string;
  answer: string;
  category: string;
  keywords: string[];
  isActive: boolean;
  createdAt: admin.firestore.FieldValue;
}

// ============================================
// FAQ DATA - 100 Questions & Answers
// ============================================

const faqData: Omit<FAQ, 'createdAt' | 'isActive'>[] = [
  // ============================================
  // CATEGORY: ordering (Đặt hàng) - 20 questions
  // ============================================
  {
    category: 'ordering',
    question: 'Làm sao để đặt hàng trên ứng dụng?',
    answer: 'Để đặt hàng, bạn mở ứng dụng → Chọn Shop → Xem Menu → Thêm món vào giỏ hàng → Vào Giỏ hàng → Nhập địa chỉ giao hàng → Chọn phương thức thanh toán → Nhấn "Đặt hàng". Đơn hàng sẽ được gửi đến quán ngay.',
    keywords: ['đặt hàng', 'order', 'cách đặt', 'mua', 'đặt món']
  },
  {
    category: 'ordering',
    question: 'Có thể đặt hàng từ nhiều quán cùng lúc không?',
    answer: 'Không, mỗi đơn hàng chỉ có thể chứa sản phẩm từ một quán duy nhất. Nếu bạn muốn đặt từ nhiều quán, vui lòng tạo nhiều đơn hàng riêng biệt.',
    keywords: ['nhiều quán', 'đặt nhiều', 'gộp đơn', 'multi shop']
  },
  {
    category: 'ordering',
    question: 'Tôi có thể đặt hàng trước không?',
    answer: 'Hiện tại ứng dụng chỉ hỗ trợ đặt hàng ngay. Tính năng đặt hàng trước sẽ được phát triển trong tương lai.',
    keywords: ['đặt trước', 'order ahead', 'hẹn giờ', 'schedule']
  },
  {
    category: 'ordering',
    question: 'Làm sao để xem lại đơn hàng đã đặt?',
    answer: 'Bạn vào mục "Đơn hàng của tôi" (My Orders) trong app để xem tất cả đơn hàng. Bạn có thể lọc theo trạng thái: Đang xử lý, Đang giao, Đã giao hoặc Đã hủy.',
    keywords: ['xem đơn', 'lịch sử', 'đơn hàng', 'my orders', 'history']
  },
  {
    category: 'ordering',
    question: 'Có số lượng đặt hàng tối thiểu không?',
    answer: 'Mỗi quán có thể đặt số lượng đặt hàng tối thiểu riêng. Thông tin này sẽ hiển thị khi bạn xem chi tiết quán. Nhiều quán không yêu cầu đơn tối thiểu.',
    keywords: ['tối thiểu', 'minimum', 'min order', 'đơn nhỏ nhất']
  },
  {
    category: 'ordering',
    question: 'Làm sao để thêm ghi chú cho món ăn?',
    answer: 'Khi thêm món vào giỏ hàng, bạn có thể nhập ghi chú (ví dụ: "Ít đường", "Không hành"). Ghi chú sẽ được gửi tới quán để chuẩn bị theo yêu cầu của bạn.',
    keywords: ['ghi chú', 'note', 'yêu cầu', 'đặc biệt', 'customize']
  },
  {
    category: 'ordering',
    question: 'Tôi có thể thay đổi đơn hàng sau khi đã đặt không?',
    answer: 'Bạn chỉ có thể hủy đơn khi đơn còn ở trạng thái "Chờ xác nhận" (PENDING). Sau khi quán đã xác nhận, bạn không thể thay đổi hay hủy đơn.',
    keywords: ['thay đổi', 'sửa đơn', 'chỉnh sửa', 'modify', 'edit order']
  },
  {
    category: 'ordering',
    question: 'Quán chưa mở cửa thì có đặt được không?',
    answer: 'Không, bạn chỉ có thể đặt hàng khi quán đang mở cửa (hiển thị biểu tượng "Đang mở"). Quán đóng cửa sẽ có trạng thái "Đã đóng".',
    keywords: ['đóng cửa', 'mở cửa', 'giờ mở', 'closed', 'open']
  },
  {
    category: 'ordering',
    question: 'Làm sao để tìm quán yêu thích?',
    answer: 'Bạn có thể sử dụng tính năng "Tìm kiếm" trên trang chủ. Gõ tên quán, tên món hoặc danh mục để tìm. Ngoài ra có thể lọc theo đánh giá, khoảng cách.',
    keywords: ['tìm kiếm', 'search', 'tìm quán', 'filter', 'lọc']
  },
  {
    category: 'ordering',
    question: 'Tôi có thể lưu quán yêu thích không?',
    answer: 'Có! Nhấn vào biểu tượng trái tim trên trang chi tiết quán để thêm vào danh sách yêu thích. Bạn xem danh sách này trong mục "Yêu thích".',
    keywords: ['yêu thích', 'favorite', 'lưu quán', 'bookmark', 'tim']
  },
  {
    category: 'ordering',
    question: 'Làm thế nào để biết đơn hàng đang ở đâu?',
    answer: 'Vào chi tiết đơn hàng để theo dõi trạng thái. Khi shipper nhận đơn, bạn sẽ thấy thông tin shipper và trạng thái giao hàng theo thời gian thực.',
    keywords: ['theo dõi', 'track', 'đơn đang ở đâu', 'tracking', 'vị trí']
  },
  {
    category: 'ordering',
    question: 'Địa chỉ giao hàng có lưu được không?',
    answer: 'Có, bạn có thể lưu nhiều địa chỉ giao hàng trong mục "Quản lý địa chỉ". Đặt một địa chỉ làm mặc định để tiết kiệm thời gian khi đặt hàng.',
    keywords: ['địa chỉ', 'lưu địa chỉ', 'address', 'mặc định', 'saved address']
  },
  {
    category: 'ordering',
    question: 'Thêm địa chỉ mới như thế nào?',
    answer: 'Vào Tài khoản → Quản lý địa chỉ → Thêm địa chỉ mới. Nhập tên tòa (ví dụ: "Tòa A"), số phòng, và ghi chú nếu cần.',
    keywords: ['thêm địa chỉ', 'địa chỉ mới', 'add address', 'tạo địa chỉ']
  },
  {
    category: 'ordering',
    question: 'Tôi không thấy món ăn mình muốn đặt?',
    answer: 'Có thể món đó đã hết hoặc quán tạm ngưng phục vụ. Bạn thử liên hệ quán qua số điện thoại hiển thị trên trang chi tiết quán.',
    keywords: ['không thấy món', 'hết món', 'không có', 'out of stock']
  },
  {
    category: 'ordering',
    question: 'Có thể gọi đồ uống riêng không?',
    answer: 'Có, nếu quán có bán đồ uống riêng thì bạn có thể đặt. Tuy nhiên mỗi đơn chỉ đặt từ 1 quán, không gộp đồ ăn và đồ uống từ các quán khác.',
    keywords: ['đồ uống', 'nước', 'drink', 'beverage']
  },
  {
    category: 'ordering',
    question: 'Phí ship tính như thế nào?',
    answer: 'Phí ship được quán thiết lập, thường từ 3.000đ - 10.000đ/đơn. Thông tin phí ship hiển thị trên trang chi tiết quán và khi bạn checkout.',
    keywords: ['phí ship', 'shipping', 'delivery fee', 'phí giao hàng']
  },
  {
    category: 'ordering',
    question: 'Có được miễn phí ship không?',
    answer: 'Một số quán có chương trình miễn phí ship cho đơn từ mức nhất định. Kiểm tra thông tin khuyến mãi trên trang chi tiết quán.',
    keywords: ['miễn phí ship', 'free ship', 'free delivery', 'không phí ship']
  },
  {
    category: 'ordering',
    question: 'Tôi có thể đặt hàng cho người khác không?',
    answer: 'Có, bạn chỉ cần nhập địa chỉ giao hàng của người nhận và ghi chú rõ tên người nhận để shipper dễ liên lạc.',
    keywords: ['đặt cho người khác', 'tặng', 'gift', 'giao cho bạn']
  },
  {
    category: 'ordering',
    question: 'Làm sao để biết quán có đáng tin không?',
    answer: 'Bạn xem đánh giá (ratings) và reviews từ khách hàng trước đó. Quán có rating cao và nhiều reviews tốt thường đáng tin cậy hơn.',
    keywords: ['đánh giá', 'review', 'rating', 'tin cậy', 'uy tín']
  },
  {
    category: 'ordering',
    question: 'Có thể đặt hàng qua hotline không?',
    answer: 'Hiện tại chỉ hỗ trợ đặt hàng qua ứng dụng. Tuy nhiên bạn có thể liên hệ quán trực tiếp qua số điện thoại nếu cần hỗ trợ.',
    keywords: ['hotline', 'gọi điện', 'đặt qua điện thoại', 'call']
  },

  // ============================================
  // CATEGORY: payment (Thanh toán) - 15 questions
  // ============================================
  {
    category: 'payment',
    question: 'Có những phương thức thanh toán nào?',
    answer: 'Hiện hỗ trợ 4 phương thức: COD (tiền mặt khi nhận hàng), ZaloPay, MoMo, và SePay. Chọn phương thức phù hợp khi checkout.',
    keywords: ['thanh toán', 'payment', 'phương thức', 'momo', 'zalopay', 'cod']
  },
  {
    category: 'payment',
    question: 'Thanh toán COD là gì?',
    answer: 'COD (Cash On Delivery) là thanh toán tiền mặt khi nhận hàng. Shipper sẽ thu tiền của bạn khi giao hàng thành công.',
    keywords: ['cod', 'tiền mặt', 'cash', 'trả khi nhận']
  },
  {
    category: 'payment',
    question: 'Thanh toán online có an toàn không?',
    answer: 'Hoàn toàn an toàn! Chúng tôi tích hợp với các cổng thanh toán uy tín (ZaloPay, MoMo, SePay). Thông tin thanh toán được mã hóa và bảo mật.',
    keywords: ['an toàn', 'bảo mật', 'secure', 'online payment']
  },
  {
    category: 'payment',
    question: 'Thanh toán thất bại thì làm sao?',
    answer: 'Nếu thanh toán thất bại, đơn hàng vẫn được tạo nhưng trạng thái là "Chờ thanh toán". Bạn có thể thử thanh toán lại hoặc chuyển sang COD.',
    keywords: ['thất bại', 'failed', 'lỗi thanh toán', 'payment failed']
  },
  {
    category: 'payment',
    question: 'Tôi đã thanh toán nhưng đơn hàng báo chưa thanh toán?',
    answer: 'Đôi khi hệ thống cần 1-2 phút để xác nhận. Nếu sau 5 phút vẫn chưa cập nhật, vui lòng liên hệ hỗ trợ qua chatbot hoặc email.',
    keywords: ['chưa cập nhật', 'pending', 'xác nhận thanh toán', 'đã trả tiền']
  },
  {
    category: 'payment',
    question: 'Có thể thanh toán bằng thẻ ngân hàng không?',
    answer: 'Có, bạn có thể liên kết thẻ ngân hàng với ZaloPay hoặc MoMo để thanh toán. Hệ thống hỗ trợ hầu hết các ngân hàng Việt Nam.',
    keywords: ['thẻ ngân hàng', 'bank card', 'visa', 'mastercard', 'atm']
  },
  {
    category: 'payment',
    question: 'Làm sao để được hoàn tiền?',
    answer: 'Nếu đơn hàng bị hủy sau khi thanh toán online, tiền sẽ được hoàn về ví thanh toán trong vòng 3-5 ngày làm việc.',
    keywords: ['hoàn tiền', 'refund', 'trả tiền', 'hoàn lại']
  },
  {
    category: 'payment',
    question: 'Tại sao phải thanh toán trước?',
    answer: 'Thanh toán online giúp đảm bảo đơn hàng được xử lý nhanh hơn và shipper không cần ứng tiền. Đây cũng là tiêu chuẩn của các app giao hàng.',
    keywords: ['thanh toán trước', 'prepaid', 'trả trước']
  },
  {
    category: 'payment',
    question: 'Có xuất hóa đơn VAT không?',
    answer: 'Hiện tại chúng tôi chưa hỗ trợ xuất hóa đơn VAT. Tính năng này sẽ được phát triển trong phiên bản sau.',
    keywords: ['hóa đơn', 'vat', 'invoice', 'xuất hóa đơn']
  },
  {
    category: 'payment',
    question: 'Thanh toán có phí không?',
    answer: 'Không, thanh toán qua app hoàn toàn miễn phí. Bạn chỉ trả tiền đồ ăn và phí ship (nếu có).',
    keywords: ['phí thanh toán', 'phí giao dịch', 'transaction fee', 'miễn phí']
  },
  {
    category: 'payment',
    question: 'ZaloPay là gì?',
    answer: 'ZaloPay là ví điện tử của Zalo. Bạn tải app ZaloPay, liên kết ngân hàng và có thể thanh toán nhanh chóng khi đặt hàng.',
    keywords: ['zalopay', 'zalo pay', 'ví zalo']
  },
  {
    category: 'payment',
    question: 'MoMo là gì?',
    answer: 'MoMo là ví điện tử phổ biến tại Việt Nam. Tải app MoMo, nạp tiền hoặc liên kết ngân hàng để thanh toán.',
    keywords: ['momo', 'ví momo', 'ví điện tử']
  },
  {
    category: 'payment',
    question: 'SePay là gì?',
    answer: 'SePay là cổng thanh toán hỗ trợ chuyển khoản ngân hàng tự động. Bạn chuyển khoản theo thông tin hiển thị và hệ thống sẽ tự xác nhận.',
    keywords: ['sepay', 'chuyển khoản', 'bank transfer']
  },
  {
    category: 'payment',
    question: 'Có voucher giảm giá không?',
    answer: 'Có! Kiểm tra mục "Voucher" trong app hoặc theo dõi fanpage để nhận mã giảm giá. Nhập mã khi checkout để được giảm.',
    keywords: ['voucher', 'giảm giá', 'mã giảm', 'discount', 'promo']
  },
  {
    category: 'payment',
    question: 'Voucher hết hạn thì sao?',
    answer: 'Voucher hết hạn sẽ không sử dụng được. Kiểm tra ngày hết hạn trước khi sử dụng. Voucher mới sẽ được cập nhật thường xuyên.',
    keywords: ['hết hạn', 'expired', 'voucher hết', 'còn hạn']
  },

  // ============================================
  // CATEGORY: delivery (Giao hàng) - 15 questions
  // ============================================
  {
    category: 'delivery',
    question: 'Thời gian giao hàng khoảng bao lâu?',
    answer: 'Thông thường 15-30 phút tùy khoảng cách và độ bận của quán. Đơn hàng phức tạp có thể lâu hơn. Bạn theo dõi trạng thái trong app.',
    keywords: ['thời gian giao', 'bao lâu', 'delivery time', 'mấy phút']
  },
  {
    category: 'delivery',
    question: 'Shipper giao hàng tận phòng không?',
    answer: 'Có, shipper sẽ giao đến tận phòng KTX của bạn theo địa chỉ đã nhập. Hãy đảm bảo địa chỉ chính xác (tòa, phòng, tầng).',
    keywords: ['giao tận nơi', 'tận phòng', 'door to door', 'giao đến nơi']
  },
  {
    category: 'delivery',
    question: 'Có thể hẹn giờ giao hàng không?',
    answer: 'Hiện chưa hỗ trợ hẹn giờ giao hàng. Đơn hàng sẽ được giao ngay sau khi quán chuẩn bị xong và shipper nhận đơn.',
    keywords: ['hẹn giờ', 'schedule', 'giao lúc', 'time slot']
  },
  {
    category: 'delivery',
    question: 'Shipper có liên hệ trước khi giao không?',
    answer: 'Có, shipper sẽ gọi hoặc nhắn tin khi đến nơi. Hãy để điện thoại bật chuông để không bỏ lỡ cuộc gọi.',
    keywords: ['liên hệ', 'gọi điện', 'contact', 'phone call']
  },
  {
    category: 'delivery',
    question: 'Tôi không ở nhà thì đơn hàng sẽ thế nào?',
    answer: 'Shipper sẽ cố gắng liên lạc. Nếu không liên lạc được sau nhiều lần, đơn có thể bị hủy. Hãy đảm bảo bạn có mặt khi chọn thời điểm đặt.',
    keywords: ['không ở nhà', 'vắng mặt', 'not home', 'absent']
  },
  {
    category: 'delivery',
    question: 'Có thể thay đổi địa chỉ giao sau khi đặt không?',
    answer: 'Sau khi đặt hàng, bạn không thể tự thay đổi địa chỉ. Nếu cần thay đổi, liên hệ shipper trực tiếp qua số điện thoại.',
    keywords: ['thay đổi địa chỉ', 'đổi địa chỉ', 'change address']
  },
  {
    category: 'delivery',
    question: 'Shipper giao hàng từ mấy giờ đến mấy giờ?',
    answer: 'Thời gian giao hàng phụ thuộc vào giờ mở cửa của quán. Thường từ 7h sáng đến 22h tối. Một số quán có thể mở sớm/muộn hơn.',
    keywords: ['giờ giao hàng', 'giờ hoạt động', 'operating hours', 'time']
  },
  {
    category: 'delivery',
    question: 'Có giao hàng cuối tuần không?',
    answer: 'Có, các quán hoạt động bình thường vào cuối tuần. Tuy nhiên có thể đông hơn nên thời gian giao có thể lâu hơn.',
    keywords: ['cuối tuần', 'weekend', 'thứ 7', 'chủ nhật']
  },
  {
    category: 'delivery',
    question: 'Làm sao để liên hệ shipper?',
    answer: 'Khi đơn hàng có shipper, thông tin liên hệ sẽ hiển thị trong chi tiết đơn hàng. Bạn có thể gọi trực tiếp cho shipper.',
    keywords: ['liên hệ shipper', 'gọi shipper', 'contact driver']
  },
  {
    category: 'delivery',
    question: 'Đơn hàng bị chậm phải làm sao?',
    answer: 'Kiểm tra trạng thái đơn trong app. Nếu quá 1 giờ mà chưa nhận được hàng, liên hệ shipper hoặc hỗ trợ qua chatbot.',
    keywords: ['chậm', 'trễ', 'delay', 'late', 'lâu quá']
  },
  {
    category: 'delivery',
    question: 'Shipper có mang theo tiền thối không?',
    answer: 'Với đơn COD, shipper sẽ chuẩn bị tiền thối. Tuy nhiên, hãy cố gắng chuẩn bị tiền lẻ để giao dịch nhanh hơn.',
    keywords: ['tiền thối', 'tiền lẻ', 'change', 'cod']
  },
  {
    category: 'delivery',
    question: 'Đồ ăn bị đổ/hư hỏng khi nhận?',
    answer: 'Chụp ảnh ngay và báo cáo qua app trong vòng 30 phút. Chúng tôi sẽ xem xét hoàn tiền hoặc giao lại tùy tình huống.',
    keywords: ['hư hỏng', 'đổ', 'bể', 'damaged', 'spill']
  },
  {
    category: 'delivery',
    question: 'Có thể tip cho shipper không?',
    answer: 'Hiện app chưa có tính năng tip online. Bạn có thể tự tip tiền mặt cho shipper nếu muốn.',
    keywords: ['tip', 'tiền boa', 'tip shipper', 'gratuity']
  },
  {
    category: 'delivery',
    question: 'Shipper có bảo quản đồ ăn nóng không?',
    answer: 'Shipper được trang bị túi giữ nhiệt để bảo quản đồ ăn. Tuy nhiên, đồ ăn có thể nguội một chút nếu giao xa.',
    keywords: ['giữ nóng', 'bảo quản', 'túi giữ nhiệt', 'insulated']
  },
  {
    category: 'delivery',
    question: 'Có giao hàng trong trời mưa không?',
    answer: 'Có, shipper vẫn giao hàng trong thời tiết xấu. Tuy nhiên thời gian giao có thể lâu hơn. Một số quán có thể tạm ngưng khi mưa quá to.',
    keywords: ['trời mưa', 'thời tiết', 'mưa', 'rain', 'weather']
  },

  // ============================================
  // CATEGORY: cancellation (Hủy đơn) - 10 questions
  // ============================================
  {
    category: 'cancellation',
    question: 'Làm sao để hủy đơn hàng?',
    answer: 'Vào chi tiết đơn hàng → Nhấn "Hủy đơn". Bạn chỉ có thể hủy khi đơn còn ở trạng thái "Chờ xác nhận" (PENDING).',
    keywords: ['hủy đơn', 'cancel', 'hủy order', 'bỏ đơn']
  },
  {
    category: 'cancellation',
    question: 'Tại sao không thể hủy đơn?',
    answer: 'Đơn hàng chỉ hủy được khi ở trạng thái "Chờ xác nhận". Sau khi quán xác nhận hoặc đang chuẩn bị, bạn không thể tự hủy được nữa.',
    keywords: ['không hủy được', 'không thể hủy', 'cannot cancel']
  },
  {
    category: 'cancellation',
    question: 'Hủy đơn có bị phạt không?',
    answer: 'Hiện tại hủy đơn không bị phạt. Tuy nhiên, việc hủy thường xuyên có thể ảnh hưởng đến tài khoản của bạn.',
    keywords: ['phạt', 'mất tiền', 'penalty', 'fine']
  },
  {
    category: 'cancellation',
    question: 'Quán từ chối đơn thì sao?',
    answer: 'Nếu quán từ chối (hết nguyên liệu, quá bận...), đơn sẽ tự động hủy. Tiền đã thanh toán sẽ được hoàn lại.',
    keywords: ['từ chối', 'reject', 'quán không nhận']
  },
  {
    category: 'cancellation',
    question: 'Hủy đơn thì tiền về khi nào?',
    answer: 'Tiền hoàn về ví thanh toán (ZaloPay/MoMo) trong vòng 3-5 ngày làm việc. Đơn COD không cần hoàn tiền.',
    keywords: ['hoàn tiền', 'tiền về', 'refund', 'khi nào có tiền']
  },
  {
    category: 'cancellation',
    question: 'Shipper hủy đơn thì sao?',
    answer: 'Nếu shipper hủy đơn (trước khi lấy hàng), đơn sẽ chuyển về trạng thái chờ shipper khác nhận. Bạn sẽ được thông báo.',
    keywords: ['shipper hủy', 'shipper cancel', 'tài xế hủy']
  },
  {
    category: 'cancellation',
    question: 'Đơn tự động hủy khi nào?',
    answer: 'Đơn có thể bị hủy tự động nếu: 1) Quán không xác nhận trong 15 phút, 2) Thanh toán online chưa hoàn tất trong 15 phút.',
    keywords: ['tự động hủy', 'auto cancel', 'đơn bị hủy']
  },
  {
    category: 'cancellation',
    question: 'Muốn hoàn tiền nhưng đơn không hủy được?',
    answer: 'Liên hệ hỗ trợ qua chatbot hoặc email. Chúng tôi sẽ xem xét và xử lý theo chính sách hoàn tiền.',
    keywords: ['không hủy được', 'muốn hoàn tiền', 'hỗ trợ']
  },
  {
    category: 'cancellation',
    question: 'Đã nhận hàng rồi có được hủy không?',
    answer: 'Không, đơn hàng đã giao thành công không thể hủy. Nếu có vấn đề với đồ ăn, vui lòng báo cáo qua app để được hỗ trợ.',
    keywords: ['đã nhận', 'sau khi nhận', 'delivered']
  },
  {
    category: 'cancellation',
    question: 'Hủy nhiều đơn có bị khóa tài khoản không?',
    answer: 'Việc hủy đơn thường xuyên có thể bị hệ thống đánh dấu. Tài khoản có thể bị hạn chế nếu lạm dụng.',
    keywords: ['khóa tài khoản', 'hủy nhiều', 'banned', 'block']
  },

  // ============================================
  // CATEGORY: account (Tài khoản) - 15 questions
  // ============================================
  {
    category: 'account',
    question: 'Làm sao để tạo tài khoản?',
    answer: 'Mở app → Nhấn "Đăng ký" → Nhập email, mật khẩu, tên → Chọn vai trò (Khách hàng/Chủ quán/Shipper) → Xác nhận email → Hoàn tất!',
    keywords: ['đăng ký', 'tạo tài khoản', 'register', 'sign up']
  },
  {
    category: 'account',
    question: 'Quên mật khẩu thì làm sao?',
    answer: 'Nhấn "Quên mật khẩu" ở màn hình đăng nhập → Nhập email → Nhận mã OTP → Nhập mã và đặt mật khẩu mới.',
    keywords: ['quên mật khẩu', 'forgot password', 'reset password', 'đặt lại mật khẩu']
  },
  {
    category: 'account',
    question: 'Làm sao để đổi mật khẩu?',
    answer: 'Vào Tài khoản → Cài đặt → Đổi mật khẩu → Nhập mật khẩu cũ và mật khẩu mới → Xác nhận.',
    keywords: ['đổi mật khẩu', 'change password', 'thay mật khẩu']
  },
  {
    category: 'account',
    question: 'Có thể đăng nhập bằng Google không?',
    answer: 'Có! Nhấn nút "Đăng nhập bằng Google" và chọn tài khoản Google của bạn. Nhanh và tiện lợi hơn.',
    keywords: ['google', 'đăng nhập google', 'google sign in']
  },
  {
    category: 'account',
    question: 'Làm sao để cập nhật thông tin cá nhân?',
    answer: 'Vào Tài khoản → Chỉnh sửa hồ sơ → Thay đổi tên, số điện thoại, ảnh đại diện → Lưu.',
    keywords: ['cập nhật thông tin', 'sửa hồ sơ', 'edit profile', 'update info']
  },
  {
    category: 'account',
    question: 'Làm sao để thay đổi ảnh đại diện?',
    answer: 'Vào Tài khoản → Nhấn vào ảnh đại diện → Chọn ảnh từ thư viện hoặc chụp ảnh mới → Xác nhận.',
    keywords: ['ảnh đại diện', 'avatar', 'change photo', 'đổi ảnh']
  },
  {
    category: 'account',
    question: 'Một email có thể tạo nhiều tài khoản không?',
    answer: 'Không, mỗi email chỉ tạo được 1 tài khoản. Nếu muốn dùng vai trò khác (Customer/Owner/Shipper), cần email khác.',
    keywords: ['nhiều tài khoản', 'nhiều email', 'multi account']
  },
  {
    category: 'account',
    question: 'Làm sao để đăng xuất?',
    answer: 'Vào Tài khoản → Cuộn xuống cuối → Nhấn "Đăng xuất" → Xác nhận.',
    keywords: ['đăng xuất', 'logout', 'sign out', 'thoát']
  },
  {
    category: 'account',
    question: 'Có thể xóa tài khoản không?',
    answer: 'Hiện chưa hỗ trợ tự xóa tài khoản. Nếu cần, vui lòng liên hệ support@ktxdelivery.com để được hỗ trợ.',
    keywords: ['xóa tài khoản', 'delete account', 'remove account']
  },
  {
    category: 'account',
    question: 'Tài khoản bị khóa phải làm sao?',
    answer: 'Liên hệ support@ktxdelivery.com để biết lý do và hướng giải quyết. Tài khoản có thể bị khóa vì vi phạm chính sách.',
    keywords: ['bị khóa', 'locked', 'banned', 'suspended']
  },
  {
    category: 'account',
    question: 'Làm sao để bật thông báo?',
    answer: 'Vào Cài đặt điện thoại → Thông báo → Tìm app KTX Delivery → Bật thông báo. Trong app cũng có thể tùy chỉnh.',
    keywords: ['thông báo', 'notification', 'bật thông báo', 'push']
  },
  {
    category: 'account',
    question: 'Không nhận được mã OTP?',
    answer: 'Kiểm tra thư mục Spam/Junk. Nếu vẫn không có, chờ 1-2 phút rồi nhấn "Gửi lại mã". Đảm bảo email nhập đúng.',
    keywords: ['otp', 'mã xác nhận', 'không nhận được mã', 'verification code']
  },
  {
    category: 'account',
    question: 'Có thể đổi email đăng nhập không?',
    answer: 'Hiện chưa hỗ trợ đổi email. Bạn cần tạo tài khoản mới với email muốn dùng.',
    keywords: ['đổi email', 'change email', 'thay email']
  },
  {
    category: 'account',
    question: 'Số điện thoại có bắt buộc không?',
    answer: 'Không bắt buộc khi đăng ký, nhưng nên thêm để shipper dễ liên hệ khi giao hàng.',
    keywords: ['số điện thoại', 'phone number', 'bắt buộc']
  },
  {
    category: 'account',
    question: 'Làm sao để thay đổi ngôn ngữ?',
    answer: 'Hiện app chỉ hỗ trợ tiếng Việt. Tính năng đa ngôn ngữ sẽ được phát triển trong tương lai.',
    keywords: ['ngôn ngữ', 'language', 'tiếng anh', 'english']
  },

  // ============================================
  // CATEGORY: owner (Chủ quán) - 15 questions
  // ============================================
  {
    category: 'owner',
    question: 'Làm sao để đăng ký bán hàng trên app?',
    answer: 'Đăng ký tài khoản với vai trò "Chủ quán" (OWNER) → Thiết lập thông tin quán → Thêm menu → Mở quán và bắt đầu nhận đơn!',
    keywords: ['đăng ký bán', 'bán hàng', 'owner', 'chủ quán', 'mở quán']
  },
  {
    category: 'owner',
    question: 'Phí dịch vụ cho chủ quán là bao nhiêu?',
    answer: 'Hiện tại không thu phí dịch vụ nền tảng. Chủ quán tự quy định mức phí ship và nhận 100% tiền từ đơn hàng.',
    keywords: ['phí dịch vụ', 'commission', 'service fee', 'platform fee']
  },
  {
    category: 'owner',
    question: 'Làm sao để thêm món vào menu?',
    answer: 'Vào Quản lý Menu → Thêm sản phẩm → Nhập tên, mô tả, giá, ảnh → Chọn danh mục → Lưu.',
    keywords: ['thêm món', 'add product', 'thêm sản phẩm', 'menu']
  },
  {
    category: 'owner',
    question: 'Có thể đặt giờ mở/đóng cửa không?',
    answer: 'Có, vào Cài đặt quán → Thiết lập giờ mở cửa và đóng cửa. Bạn cũng có thể tạm đóng quán thủ công.',
    keywords: ['giờ mở cửa', 'open time', 'close time', 'opening hours']
  },
  {
    category: 'owner',
    question: 'Làm sao để xem doanh thu?',
    answer: 'Vào Dashboard → Xem tổng doanh thu, số đơn hàng, đánh giá. Có thể lọc theo ngày/tuần/tháng.',
    keywords: ['doanh thu', 'revenue', 'thống kê', 'analytics', 'dashboard']
  },
  {
    category: 'owner',
    question: 'Phí ship tính thế nào?',
    answer: 'Chủ quán tự đặt phí ship cho mỗi đơn (tối thiểu 3.000đ). Phí này sẽ được trích từ tổng đơn và trả cho shipper.',
    keywords: ['phí ship', 'ship fee', 'delivery fee', 'tính phí']
  },
  {
    category: 'owner',
    question: 'Tiền về ví khi nào?',
    answer: 'Tiền từ đơn hàng thành công sẽ vào ví sau 24 giờ (pending → available). Sau đó có thể rút về ngân hàng.',
    keywords: ['tiền về ví', 'wallet', 'ví tiền', 'pending']
  },
  {
    category: 'owner',
    question: 'Làm sao để rút tiền về ngân hàng?',
    answer: 'Vào Ví → Rút tiền → Nhập số tiền (tối thiểu 100.000đ) → Chọn ngân hàng → Xác nhận. Tiền sẽ về trong 1-3 ngày làm việc.',
    keywords: ['rút tiền', 'payout', 'rút về ngân hàng', 'withdraw']
  },
  {
    category: 'owner',
    question: 'Món hết thì làm sao?',
    answer: 'Vào Quản lý Menu → Chọn món → Tắt "Còn hàng" (isAvailable). Món sẽ không hiển thị cho khách đặt.',
    keywords: ['hết món', 'out of stock', 'tạm hết', 'không còn']
  },
  {
    category: 'owner',
    question: 'Có thể từ chối đơn hàng không?',
    answer: 'Có, khi nhận đơn mới bạn có thể "Xác nhận" hoặc "Từ chối". Nếu từ chối, nên ghi rõ lý do.',
    keywords: ['từ chối đơn', 'reject order', 'không nhận đơn']
  },
  {
    category: 'owner',
    question: 'Làm sao để nhận thông báo đơn mới?',
    answer: 'Đảm bảo đã bật thông báo cho app trên điện thoại. Khi có đơn mới, bạn sẽ nhận được push notification.',
    keywords: ['thông báo đơn', 'order notification', 'đơn mới']
  },
  {
    category: 'owner',
    question: 'Quy trình xử lý đơn hàng như thế nào?',
    answer: 'Nhận đơn → Xác nhận → Chuẩn bị → Sẵn sàng giao. Shipper sẽ nhận khi bạn báo "Sẵn sàng giao".',
    keywords: ['quy trình', 'process', 'xử lý đơn', 'order flow']
  },
  {
    category: 'owner',
    question: 'Làm sao để xem đánh giá từ khách?',
    answer: 'Vào Dashboard hoặc Đánh giá để xem tất cả reviews từ khách hàng. Rating trung bình sẽ hiển thị trên trang quán.',
    keywords: ['xem đánh giá', 'reviews', 'feedback', 'rating']
  },
  {
    category: 'owner',
    question: 'Có thể có nhiều người quản lý quán không?',
    answer: 'Hiện một tài khoản = một quán. Tính năng nhiều admin cho một quán sẽ được phát triển sau.',
    keywords: ['nhiều admin', 'nhiều người quản lý', 'multi admin']
  },
  {
    category: 'owner',
    question: 'Có hỗ trợ in đơn hàng không?',
    answer: 'Hiện chưa tích hợp máy in. Bạn có thể xem chi tiết đơn trên app. Tính năng in sẽ có trong tương lai.',
    keywords: ['in đơn', 'print', 'máy in', 'printer']
  },

  // ============================================
  // CATEGORY: shipper (Shipper) - 10 questions
  // ============================================
  {
    category: 'shipper',
    question: 'Làm sao để đăng ký làm shipper?',
    answer: 'Đăng ký tài khoản với vai trò "Shipper" → Hoàn tất hồ sơ (CMND, giấy phép lái xe nếu có) → Chờ duyệt → Bắt đầu nhận đơn!',
    keywords: ['đăng ký shipper', 'làm shipper', 'tài xế', 'driver']
  },
  {
    category: 'shipper',
    question: 'Shipper kiếm được bao nhiêu?',
    answer: 'Thu nhập = Phí ship của mỗi đơn (do quán đặt, thường 3.000-10.000đ/đơn). Càng giao nhiều đơn, thu nhập càng cao.',
    keywords: ['thu nhập shipper', 'lương', 'income', 'earnings']
  },
  {
    category: 'shipper',
    question: 'Làm sao để nhận đơn?',
    answer: 'Bật trạng thái "Online" → Đơn hàng sẽ hiển thị → Chọn đơn muốn giao → Nhấn "Nhận đơn".',
    keywords: ['nhận đơn', 'claim order', 'accept order']
  },
  {
    category: 'shipper',
    question: 'Có thể từ chối đơn đã nhận không?',
    answer: 'Có, nhưng chỉ trước khi lấy hàng. Sau khi đã lấy hàng, bạn phải hoàn tất giao hàng.',
    keywords: ['từ chối đơn', 'reject order', 'hủy đơn shipper']
  },
  {
    category: 'shipper',
    question: 'COD thì shipper phải ứng tiền không?',
    answer: 'Đúng, với đơn COD shipper ứng tiền cho quán khi lấy hàng, sau đó thu lại từ khách khi giao.',
    keywords: ['ứng tiền', 'cod', 'tiền mặt', 'advance payment']
  },
  {
    category: 'shipper',
    question: 'Shipper rút tiền về ngân hàng thế nào?',
    answer: 'Vào Ví → Rút tiền → Nhập số tiền (tối thiểu 50.000đ) → Chọn ngân hàng → Xác nhận.',
    keywords: ['rút tiền shipper', 'shipper payout', 'withdraw']
  },
  {
    category: 'shipper',
    question: 'Giao hàng xong phải làm gì?',
    answer: 'Chụp ảnh xác nhận giao hàng (delivery proof) → Upload lên app → Nhấn "Hoàn thành giao hàng".',
    keywords: ['hoàn thành', 'confirm delivery', 'delivery proof']
  },
  {
    category: 'shipper',
    question: 'Làm sao để xem lịch sử giao hàng?',
    answer: 'Vào mục "Lịch sử" để xem tất cả đơn đã giao, thu nhập từng đơn, và tổng thu nhập.',
    keywords: ['lịch sử giao', 'delivery history', 'đơn đã giao']
  },
  {
    category: 'shipper',
    question: 'Rating của shipper tính thế nào?',
    answer: 'Khách đánh giá sau mỗi đơn. Rating = trung bình tất cả đánh giá. Rating cao giúp bạn được ưu tiên nhận đơn.',
    keywords: ['rating shipper', 'đánh giá', 'shipper rating']
  },
  {
    category: 'shipper',
    question: 'Có bị phạt khi hủy đơn không?',
    answer: 'Hủy đơn thường xuyên sẽ ảnh hưởng đến rating và có thể bị hạn chế nhận đơn. Cố gắng hoàn thành tất cả đơn đã nhận.',
    keywords: ['phạt shipper', 'hủy đơn shipper', 'penalty']
  },

  // ============================================
  // CATEGORY: technical (Kỹ thuật/App) - 10 questions
  // ============================================
  {
    category: 'technical',
    question: 'App hoạt động trên điện thoại nào?',
    answer: 'App hoạt động trên Android (từ Android 7.0 trở lên). Phiên bản iOS sẽ được phát triển trong tương lai.',
    keywords: ['điện thoại', 'android', 'ios', 'compatible', 'hệ điều hành']
  },
  {
    category: 'technical',
    question: 'App bị lỗi/crash phải làm sao?',
    answer: 'Thử: 1) Tắt và mở lại app, 2) Xóa cache app, 3) Cập nhật phiên bản mới nhất, 4) Liên hệ support nếu vẫn lỗi.',
    keywords: ['lỗi', 'crash', 'không mở được', 'bug', 'error']
  },
  {
    category: 'technical',
    question: 'Làm sao để cập nhật app?',
    answer: 'Vào Google Play Store → Tìm "KTX Delivery" → Nhấn "Cập nhật" (nếu có phiên bản mới).',
    keywords: ['cập nhật', 'update', 'phiên bản mới', 'version']
  },
  {
    category: 'technical',
    question: 'App có cần kết nối mạng không?',
    answer: 'Có, app cần kết nối internet (WiFi hoặc 4G/5G) để hoạt động. Không có chế độ offline.',
    keywords: ['mạng', 'internet', 'wifi', 'offline', 'online']
  },
  {
    category: 'technical',
    question: 'Mật khẩu có an toàn không?',
    answer: 'Hoàn toàn an toàn! Mật khẩu được mã hóa và lưu trữ theo tiêu chuẩn Firebase Authentication.',
    keywords: ['bảo mật', 'mật khẩu', 'security', 'password']
  },
  {
    category: 'technical',
    question: 'App thu thập dữ liệu gì?',
    answer: 'Chúng tôi thu thập: email, số điện thoại, địa chỉ giao hàng, lịch sử đơn hàng. Dữ liệu chỉ dùng để vận hành dịch vụ.',
    keywords: ['dữ liệu', 'privacy', 'data', 'quyền riêng tư']
  },
  {
    category: 'technical',
    question: 'App có chiếm nhiều bộ nhớ không?',
    answer: 'App nhẹ, chiếm khoảng 50-100MB. Cache có thể tăng lên theo thời gian, bạn có thể xóa cache trong cài đặt.',
    keywords: ['bộ nhớ', 'memory', 'storage', 'dung lượng']
  },
  {
    category: 'technical',
    question: 'Không nhận được thông báo push?',
    answer: 'Kiểm tra: 1) Bật thông báo cho app trong cài đặt điện thoại, 2) Pin/Battery Optimization không chặn app, 3) Đăng nhập lại.',
    keywords: ['push notification', 'thông báo', 'notification']
  },
  {
    category: 'technical',
    question: 'App có web version không?',
    answer: 'Hiện chỉ có mobile app (Android). Admin Panel dành cho quản trị viên có phiên bản web.',
    keywords: ['web', 'website', 'desktop', 'browser']
  },
  {
    category: 'technical',
    question: 'Làm sao để báo lỗi/góp ý?',
    answer: 'Gửi email đến support@ktxdelivery.com hoặc sử dụng chatbot trong app. Chúng tôi rất trân trọng mọi góp ý!',
    keywords: ['báo lỗi', 'góp ý', 'feedback', 'report bug', 'suggestion']
  }
];

// ============================================
// SEED FUNCTION
// ============================================

async function seedFAQs() {
  console.log('🌱 Starting FAQ seed process...\n');

  const batch = db.batch();
  const faqsRef = db.collection('faqs');

  // Clear existing FAQs (optional - uncomment if needed)
  // const existingFaqs = await faqsRef.get();
  // existingFaqs.forEach(doc => batch.delete(doc.ref));
  // console.log(`🗑️  Cleared ${existingFaqs.size} existing FAQs\n`);

  // Add new FAQs
  let count = 0;
  for (const faq of faqData) {
    const docRef = faqsRef.doc();
    batch.set(docRef, {
      id: docRef.id,
      ...faq,
      isActive: true,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    count++;
  }

  // Commit the batch
  await batch.commit();

  // Summary
  const categoryCounts: Record<string, number> = {};
  faqData.forEach(faq => {
    categoryCounts[faq.category] = (categoryCounts[faq.category] || 0) + 1;
  });

  console.log('✅ FAQ seed completed!\n');
  console.log('📊 Summary:');
  console.log(`   Total FAQs: ${count}\n`);
  console.log('   By Category:');
  Object.entries(categoryCounts).forEach(([category, cnt]) => {
    console.log(`   - ${category}: ${cnt} questions`);
  });
  console.log('\n🎉 Done!');
}

// Run the seed
seedFAQs()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error('❌ Seed failed:', error);
    process.exit(1);
  });
