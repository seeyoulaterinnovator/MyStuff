export const isExistGuard = <T>(item?: T): item is NonNullable<T> => {
  return item != null;
};