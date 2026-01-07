import { Inject, NotFoundException } from '@nestjs/common';
import { Firestore, Timestamp } from 'firebase-admin/firestore';
import {
  IBaseEntity,
  IBaseRepository,
  PaginatedResult,
  QueryOptions,
} from '../interfaces';

/**
 * FirestoreBaseRepository - Abstract base class cho Firestore repositories
 *
 * Implement IBaseRepository với Firestore-specific logic.
 * Tất cả Firestore repositories PHẢI extend class này.
 *
 * Benefits:
 * - DRY: Common logic chỉ viết 1 lần
 * - Consistency: Tất cả repos hoạt động giống nhau
 * - Easy to swap: Chỉ cần tạo MongoBaseRepository để đổi DB
 *
 * @example
 * ```typescript
 * @Injectable()
 * class FirestoreUsersRepository extends FirestoreBaseRepository<UserEntity> {
 *   constructor(@Inject('FIRESTORE') firestore: Firestore) {
 *     super(firestore, 'users');
 *   }
 *
 *   // Custom methods
 *   async findByEmail(email: string): Promise<UserEntity | null> {
 *     return this.findOneWhere({ email });
 *   }
 * }
 * ```
 */
export abstract class FirestoreBaseRepository<T extends IBaseEntity>
  implements IBaseRepository<T>
{
  protected readonly collection: FirebaseFirestore.CollectionReference;

  constructor(
    @Inject('FIRESTORE') protected readonly firestore: Firestore,
    protected readonly collectionName: string,
  ) {
    this.collection = this.firestore.collection(collectionName);
  }

  // ============================================
  // READ OPERATIONS
  // ============================================

  /**
   * Tìm entity theo ID
   */
  async findById(id: string): Promise<T | null> {
    const doc = await this.collection.doc(id).get();

    if (!doc.exists) {
      return null;
    }

    return this.mapDocToEntity(doc);
  }

  /**
   * Tìm entity theo ID, throw error nếu không tìm thấy
   */
  async findByIdOrThrow(id: string): Promise<T> {
    const entity = await this.findById(id);

    if (!entity) {
      throw new NotFoundException(
        `${this.collectionName} with ID "${id}" not found`,
      );
    }

    return entity;
  }

  /**
   * Lấy tất cả entities
   */
  async findAll(): Promise<T[]> {
    const snapshot = await this.collection.get();
    return snapshot.docs.map((doc) => this.mapDocToEntity(doc));
  }

  /**
   * Lấy entities với query options
   */
  async findMany(options?: QueryOptions<T>): Promise<T[]> {
    let query: FirebaseFirestore.Query = this.collection;

    // Apply where conditions
    if (options?.where) {
      for (const [field, value] of Object.entries(options.where)) {
        if (value !== undefined) {
          query = query.where(field, '==', value);
        }
      }
    }

    // Apply orderBy
    if (options?.orderBy) {
      query = query.orderBy(
        options.orderBy.field as string,
        options.orderBy.direction,
      );
    }

    // Apply pagination
    if (options?.pagination) {
      const { page = 1, limit = 20 } = options.pagination;
      const offset = (page - 1) * limit;
      query = query.offset(offset).limit(limit);
    }

    const snapshot = await query.get();
    return snapshot.docs.map((doc) => this.mapDocToEntity(doc));
  }

  /**
   * Lấy entities với pagination
   */
  async findPaginated(options?: QueryOptions<T>): Promise<PaginatedResult<T>> {
    const { page = 1, limit = 20 } = options?.pagination || {};

    // Get total count
    const total = await this.count(options?.where);

    // Get data
    const data = await this.findMany(options);

    return {
      data,
      meta: {
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit),
      },
    };
  }

  /**
   * Đếm số lượng entities
   */
  async count(where?: Partial<Record<keyof T, any>>): Promise<number> {
    let query: FirebaseFirestore.Query = this.collection;

    if (where) {
      for (const [field, value] of Object.entries(where)) {
        if (value !== undefined) {
          query = query.where(field, '==', value);
        }
      }
    }

    const snapshot = await query.count().get();
    return snapshot.data().count;
  }

  /**
   * Kiểm tra entity tồn tại
   */
  async exists(id: string): Promise<boolean> {
    const doc = await this.collection.doc(id).get();
    return doc.exists;
  }

  /**
   * Tìm một entity theo điều kiện
   */
  protected async findOneWhere(
    where: Partial<Record<keyof T, any>>,
  ): Promise<T | null> {
    let query: FirebaseFirestore.Query = this.collection;

    for (const [field, value] of Object.entries(where)) {
      if (value !== undefined) {
        query = query.where(field, '==', value);
      }
    }

    const snapshot = await query.limit(1).get();

    if (snapshot.empty) {
      return null;
    }

    return this.mapDocToEntity(snapshot.docs[0]);
  }

  // ============================================
  // WRITE OPERATIONS
  // ============================================

  /**
   * Tạo entity mới
   */
  async create(data: Omit<T, 'id' | 'createdAt' | 'updatedAt'>): Promise<T> {
    const now = Timestamp.now();

    const docData = {
      ...data,
      createdAt: now,
      updatedAt: now,
    };

    const docRef = await this.collection.add(docData);

    return {
      id: docRef.id,
      ...docData,
    } as T;
  }

  /**
   * Tạo entity với ID cụ thể
   */
  async createWithId(
    id: string,
    data: Omit<T, 'id' | 'createdAt' | 'updatedAt'>,
  ): Promise<T> {
    const now = Timestamp.now();

    const docData = {
      ...data,
      createdAt: now,
      updatedAt: now,
    };

    await this.collection.doc(id).set(docData);

    return {
      id,
      ...docData,
    } as T;
  }

  /**
   * Update entity
   */
  async update(
    id: string,
    data: Partial<Omit<T, 'id' | 'createdAt' | 'updatedAt'>>,
  ): Promise<T> {
    const docRef = this.collection.doc(id);
    const doc = await docRef.get();

    if (!doc.exists) {
      throw new NotFoundException(
        `${this.collectionName} with ID "${id}" not found`,
      );
    }

    const updateData = {
      ...data,
      updatedAt: Timestamp.now(),
    };

    await docRef.update(updateData);

    return {
      id: doc.id,
      ...doc.data(),
      ...updateData,
    } as T;
  }

  /**
   * Xóa entity (hard delete)
   */
  async delete(id: string): Promise<void> {
    const docRef = this.collection.doc(id);
    const doc = await docRef.get();

    if (!doc.exists) {
      throw new NotFoundException(
        `${this.collectionName} with ID "${id}" not found`,
      );
    }

    await docRef.delete();
  }

  /**
   * Soft delete (set deletedAt)
   */
  async softDelete(id: string): Promise<void> {
    const docRef = this.collection.doc(id);
    const doc = await docRef.get();

    if (!doc.exists) {
      throw new NotFoundException(
        `${this.collectionName} with ID "${id}" not found`,
      );
    }

    await docRef.update({
      deletedAt: Timestamp.now(),
      updatedAt: Timestamp.now(),
    });
  }

  // ============================================
  // HELPER METHODS
  // ============================================

  /**
   * Map Firestore document to Entity
   * Override nếu cần custom mapping
   */
  protected mapDocToEntity(doc: FirebaseFirestore.DocumentSnapshot): T {
    return {
      id: doc.id,
      ...doc.data(),
    } as T;
  }

  /**
   * Get Firestore reference (for transactions)
   */
  getDocRef(id: string): FirebaseFirestore.DocumentReference {
    return this.collection.doc(id);
  }

  /**
   * Get collection reference (for complex queries)
   */
  getCollectionRef(): FirebaseFirestore.CollectionReference {
    return this.collection;
  }
}
