export type Order = 'asc' | 'desc';

export interface SortingOptions<T extends string = string> {
  order: Order;
  orderBy?: T;
}
