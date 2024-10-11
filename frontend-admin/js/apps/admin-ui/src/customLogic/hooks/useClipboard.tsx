import { useEffect } from "react";

export const useClipboard = () => {
  const handleCopy = (e: ClipboardEvent) => {
    const selection = window.getSelection()?.toString();

    if (selection) {
      const trimmedText = selection.trim();

      e.clipboardData?.setData("text/plain", trimmedText);
      e.preventDefault();
    }
  };

  useEffect(() => {
    document.addEventListener("copy", handleCopy);

    return () => {
      document.removeEventListener("copy", handleCopy);
    };
  }, []);
};
