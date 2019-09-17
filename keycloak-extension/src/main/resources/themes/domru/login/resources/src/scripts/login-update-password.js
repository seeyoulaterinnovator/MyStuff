import linkPasswords from './link-passwords.js';
import { setButtonAvailability } from './helpers.js';

export default (function() {
  const formElement = document.getElementById('loginUpdatePasswordForm');
  if (!formElement) return;

  const submitElement = document.getElementById('submit');
  const passwordElement = document.getElementById('password-new');
  const passwordConfirmElement = document.getElementById('password-confirm');
  submitElement.disabled = true;

  let isPasswordExists = false;
  let isPasswordConfirmExists = false;
  let arePasswordsEqual = false;

  passwordElement.addEventListener('input', () => {
    setButtonAvailability(validate, submitElement);
  });

  passwordConfirmElement.addEventListener('input', () => {
    setButtonAvailability(validate, submitElement);
  });

  function getPassword() {
    return passwordElement.value;
  }
  function setPassword(password) {
    passwordElement.value = password;
    passwordConfirmElement.value = password;
    setButtonAvailability(validate, submitElement);
  }
  linkPasswords(getPassword, setPassword, passwordElement);

  function validate() {
    isPasswordExists = passwordElement.value !== '';
    isPasswordConfirmExists = passwordConfirmElement.value !== '';
    arePasswordsEqual = passwordElement.value === passwordConfirmElement.value;

    return isPasswordExists && isPasswordConfirmExists && arePasswordsEqual;
  }
})();
