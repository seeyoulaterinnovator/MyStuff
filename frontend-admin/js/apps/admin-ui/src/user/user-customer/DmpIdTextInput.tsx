import { useMask } from "@react-input/mask";
import { TextInput } from "@patternfly/react-core";
import { useTranslation } from "react-i18next";

export const DmpIdTextInput = (props: {
  onChange: (value: string) => void;
}) => {
  const { onChange } = props;
  const { t } = useTranslation();
  const inputRef = useMask({
    mask: "________-____-____-____-____________",
    replacement: { _: /[0-9a-f]/ },
  });
  return (
    <TextInput
      aria-label={t("dmpId")}
      onChange={(_event, value) => {
        onChange(value);
      }}
      placeholder={t("dmpId")}
      type="text"
      ref={inputRef}
    />
  );
};
