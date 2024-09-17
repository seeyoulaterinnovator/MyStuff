import { Button, type ButtonProps } from "@patternfly/react-core";
import { useRef, ChangeEvent } from "react";

export interface UploadButtonProps extends ButtonProps {
  extensions?: string;
  multiple?: boolean;
  onUpload: (event: ChangeEvent<HTMLInputElement>) => void;
}

export const UploadButton = (props: UploadButtonProps) => {
  const {
    onClick,
    children,
    onUpload,
    extensions,
    multiple = false,
    ...otherProps
  } = props;

  const inputFileRef = useRef<HTMLInputElement | null>(null);

  return (
    <Button
      {...otherProps}
      onClick={(event) => {
        inputFileRef.current?.click();
        onClick?.(event);
      }}
    >
      {children}
      <input
        accept={extensions}
        multiple={multiple}
        ref={inputFileRef}
        hidden
        type="file"
        onChange={onUpload}
      />
    </Button>
  );
};
