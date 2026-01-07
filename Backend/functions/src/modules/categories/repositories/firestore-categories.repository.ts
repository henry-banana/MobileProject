import { Injectable, Inject } from '@nestjs/common';
import { Firestore, Timestamp, FieldValue } from 'firebase-admin/firestore';
import { FirestoreBaseRepository } from '../../../core/database';
import { ICategoriesRepository } from '../interfaces';
import { CategoryEntity, CategoryStatus } from '../entities/category.entity';

/**
 * FirestoreCategoriesRepository - Firestore implementation của ICategoriesRepository
 *
 * Extend FirestoreBaseRepository để có CRUD operations,
 * implement thêm category-specific methods.
 *
 * NOTE: Class này KHÔNG được inject trực tiếp vào Service.
 * Phải register trong module providers với token CATEGORIES_REPOSITORY_TOKEN.
 */
@Injectable()
export class FirestoreCategoriesRepository
  extends FirestoreBaseRepository<CategoryEntity>
  implements ICategoriesRepository
{
  constructor(@Inject('FIRESTORE') firestore: Firestore) {
    super(firestore, 'categories');
  }

  /**
   * Tìm category theo slug
   */
  async findBySlug(slug: string): Promise<CategoryEntity | null> {
    return this.findOneWhere({ slug });
  }

  /**
   * Lấy danh sách categories đang active, sắp xếp theo displayOrder
   */
  async findActive(): Promise<CategoryEntity[]> {
    const snapshot = await this.collection
      .where('status', '==', CategoryStatus.ACTIVE)
      .orderBy('displayOrder', 'asc')
      .get();

    return snapshot.docs.map((doc) => this.mapDocToEntity(doc));
  }

  /**
   * Kiểm tra slug đã tồn tại
   * @param slug - Slug cần kiểm tra
   * @param excludeId - ID category cần loại trừ (khi update)
   */
  async isSlugExist(slug: string, excludeId?: string): Promise<boolean> {
    const existing = await this.findBySlug(slug);

    if (!existing) {
      return false;
    }

    // Nếu có excludeId và match, nghĩa là đang update chính nó
    if (excludeId && existing.id === excludeId) {
      return false;
    }

    return true;
  }

  /**
   * Lấy displayOrder lớn nhất
   */
  async getMaxDisplayOrder(): Promise<number> {
    const snapshot = await this.collection
      .orderBy('displayOrder', 'desc')
      .limit(1)
      .get();

    if (snapshot.empty) {
      return 0;
    }

    return snapshot.docs[0].data().displayOrder || 0;
  }

  /**
   * Increment product count
   */
  async incrementProductCount(id: string): Promise<void> {
    await this.collection.doc(id).update({
      productCount: FieldValue.increment(1),
      updatedAt: Timestamp.now(),
    });
  }

  /**
   * Decrement product count
   */
  async decrementProductCount(id: string): Promise<void> {
    await this.collection.doc(id).update({
      productCount: FieldValue.increment(-1),
      updatedAt: Timestamp.now(),
    });
  }

  /**
   * Override findAll để sắp xếp theo displayOrder
   */
  async findAll(): Promise<CategoryEntity[]> {
    const snapshot = await this.collection
      .orderBy('displayOrder', 'asc')
      .get();

    return snapshot.docs.map((doc) => this.mapDocToEntity(doc));
  }
}
