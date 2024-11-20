export const HashRestorer = ({ children }: any) => {
  const hash = sessionStorage.getItem("hash");
  if (hash && location.hash.substring(1) !== hash) {
    sessionStorage.removeItem("hash");
    location.replace(
      location.href.replaceAll(/#.*$/g, "") +
        `#${hash.replace(/^\/realms/g, "")}`,
    );
  }
  return children;
};
