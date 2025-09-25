export interface BrandRepresentation {
  brandId: string;
  brandName: string;
  code: string;
}

export interface RealmBrandRepresentation extends BrandRepresentation {
  default: boolean;
}
