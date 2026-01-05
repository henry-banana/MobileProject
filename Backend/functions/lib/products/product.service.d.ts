import { Firestore } from "firebase-admin/firestore";
interface Product {
    id?: string;
    name: string;
    price: number;
    description?: string;
    createdAt?: Date;
}
export declare class ProductService {
    private readonly firestore;
    private readonly collectionName;
    constructor(firestore: Firestore);
    getAllProducts(): Promise<Product[]>;
    createProduct(productData: Omit<Product, "id" | "createdAt">): Promise<Product>;
}
export {};
