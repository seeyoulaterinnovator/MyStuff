export default interface JsonResponse<T> {
  results: T;
  httpStatus: String;
}
