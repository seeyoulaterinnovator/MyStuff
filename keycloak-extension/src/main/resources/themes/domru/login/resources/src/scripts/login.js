import IMask from 'imask';
import VALIDATION_RULES from '../constants/validationRules.js';
import { setButtonAvailability } from './helpers.js';

export default (function() {
  if (!document.getElementById('loginForm')) return;

  const submitElement = document.getElementById('submit');
  const usernameElement = document.getElementById('username');
  const passwordElement = document.getElementById('password');
  submitElement.disabled = true;

  const dynamicMask = IMask(usernameElement, {
    mask: [
      {
        mask: '+{7} (000) 000-00-00',
      },
      {
        mask: /^\S*@?\S*$/,
      },
    ],
  });

  let isUsernameValid = false;
  let isPasswordExists = false;

  usernameElement.addEventListener('input', () => {
    setButtonAvailability(validate, submitElement);
  });

  passwordElement.addEventListener('input', () => {
    setButtonAvailability(validate, submitElement);
  });

  function validate() {
    // isPasswordExists = passwordElement.value !== '';
    // isUsernameValid =
    //   dynamicMask.unmaskedValue.match(VALIDATION_RULES.email) ||
    //   dynamicMask.unmaskedValue.match(VALIDATION_RULES.phone);

    // return isPasswordExists && isUsernameValid;
    return true;
  }
})();
