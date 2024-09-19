export const isExistGuard = <T>(item?: T): item is NonNullable<T> => {
	return item != null;
};

export const isEnumValueGuard =
  <T extends object>(object: T) =>
    (checkedValue: unknown): checkedValue is T[keyof T] => {
      return Object.values(object).includes(checkedValue as T[keyof T]);
    };

export const isReadonlyArray = <T,>(arr: unknown): arr is readonly T[] => {
  return Array.isArray(arr);
}
