import { Injectable, Logger, BadRequestException } from '@nestjs/common';
import { FirestoreService } from '../../core/firebase/firestore.service';
import { ErrorCodes } from '../constants/error-codes';

export interface OtpRecord {
  email: string;
  otp: string;
  purpose: 'REGISTER' | 'FORGOT_PASSWORD';
  expiresAt: number;
  createdAt: number;
}

/**
 * OTP Service
 *
 * Handles OTP generation, storage, and verification.
 * - Generate 6-digit OTP
 * - Store in Firestore with 15min expiry
 * - Verify OTP
 * - Rate limiting (1 OTP/min/email)
 */
@Injectable()
export class OtpService {
  private readonly logger = new Logger(OtpService.name);
  private readonly OTP_COLLECTION = 'otps';
  private readonly OTP_EXPIRY_MS = 15 * 60 * 1000; // 15 minutes
  private readonly RATE_LIMIT_MS = 60 * 1000; // 1 minute

  constructor(private readonly firestoreService: FirestoreService) {}

  /**
   * Generate and store OTP
   */
  async generateOtp(email: string, purpose: 'REGISTER' | 'FORGOT_PASSWORD'): Promise<string> {
    // Check rate limit
    await this.checkRateLimit(email, purpose);

    // Generate 6-digit OTP
    const otp = this.generateRandomOtp();

    // Store in Firestore
    const now = Date.now();
    const otpRecord: OtpRecord = {
      email,
      otp,
      purpose,
      expiresAt: now + this.OTP_EXPIRY_MS,
      createdAt: now,
    };

    const docId = `${email}_${purpose}`;
    await this.firestoreService.db.collection(this.OTP_COLLECTION).doc(docId).set(otpRecord);

    this.logger.log(`Generated OTP for ${email} (purpose: ${purpose}): ${otp}`);

    return otp;
  }

  /**
   * Verify OTP
   */
  async verifyOtp(
    email: string,
    otp: string,
    purpose: 'REGISTER' | 'FORGOT_PASSWORD',
  ): Promise<boolean> {
    const docId = `${email}_${purpose}`;
    const doc = await this.firestoreService.db.collection(this.OTP_COLLECTION).doc(docId).get();

    if (!doc.exists) {
      throw new BadRequestException({
        code: ErrorCodes.AUTH_OTP_INVALID,
        message: 'OTP không hợp lệ',
      });
    }

    const record = doc.data() as OtpRecord;

    // Check if expired
    if (Date.now() > record.expiresAt) {
      // Clean up expired OTP
      await doc.ref.delete();
      throw new BadRequestException({
        code: ErrorCodes.AUTH_OTP_EXPIRED,
        message: 'OTP đã hết hạn',
      });
    }

    // Check if OTP matches
    if (record.otp !== otp) {
      throw new BadRequestException({
        code: ErrorCodes.AUTH_OTP_INVALID,
        message: 'OTP không đúng',
      });
    }

    // OTP is valid - delete it (one-time use)
    await doc.ref.delete();

    this.logger.log(`OTP verified successfully for ${email}`);
    return true;
  }

  /**
   * Check rate limit (1 OTP per minute)
   */
  private async checkRateLimit(
    email: string,
    purpose: 'REGISTER' | 'FORGOT_PASSWORD',
  ): Promise<void> {
    const docId = `${email}_${purpose}`;
    const doc = await this.firestoreService.db.collection(this.OTP_COLLECTION).doc(docId).get();

    if (doc.exists) {
      const record = doc.data() as OtpRecord;
      const timeSinceCreated = Date.now() - record.createdAt;

      if (timeSinceCreated < this.RATE_LIMIT_MS) {
        const remainingSeconds = Math.ceil((this.RATE_LIMIT_MS - timeSinceCreated) / 1000);
        throw new BadRequestException({
          code: 'OTP_004',
          message: `Vui lòng chờ ${remainingSeconds}s trước khi gửi lại OTP`,
        });
      }
    }
  }

  /**
   * Generate random 6-digit OTP
   */
  private generateRandomOtp(): string {
    return Math.floor(100000 + Math.random() * 900000).toString();
  }

  /**
   * Delete OTP (for cleanup)
   */
  async deleteOtp(email: string, purpose: 'REGISTER' | 'FORGOT_PASSWORD'): Promise<void> {
    const docId = `${email}_${purpose}`;
    await this.firestoreService.db.collection(this.OTP_COLLECTION).doc(docId).delete();
  }
}
