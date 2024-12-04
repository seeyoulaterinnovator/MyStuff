export const PASSWORD_CHARSET = {
  lowercase: 'abcdefghijklmnopqrstuvwxyz',
  uppercase: 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
  numbers: '0123456789',
  extraChars: '^&*%$@#!.-[]_',
};

export const WRONG_PASS_REG = /([^A-Za-z0-9^&*%$@#\-!.\[\]_]+)/g;

export const REQUIRED_PASSWORD = /^(?=.{8,16}$)(?=.*[A-Z])(?=.*\d)[0-9a-zA-Z^&*%$@#\-!.\[\]_].*$/

export const HIGHLIGHT_VALIDATION_CHARSET = {
  letters: {
    lowercase: PASSWORD_CHARSET.lowercase,
    uppercase: PASSWORD_CHARSET.uppercase,
  },
  numbers: PASSWORD_CHARSET.numbers,
  extraChars: PASSWORD_CHARSET.extraChars,
};
