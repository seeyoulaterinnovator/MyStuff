export const HashInterceptor = ({ children }: any) => {
  const hash = location.hash.substring(1);
  const lastHash = sessionStorage.getItem("hash");
  if (hash && (!lastHash || lastHash !== hash)) {
    sessionStorage.setItem("hash", hash);
  }
  return children;
};
