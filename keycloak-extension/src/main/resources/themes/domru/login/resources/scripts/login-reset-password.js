import IMask from 'imask';
import VALIDATION_RULES from '../src/constants/validationRules.js';

export default (function() {
  const formElement = document.getElementById('loginResetPasswordForm');
  if (!formElement) return;

  const submitElement = document.getElementById('submit');
  const usernameElement = document.getElementById('username');
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

  usernameElement.addEventListener('input', () => validate());

  function validate() {
    if (
      dynamicMask.unmaskedValue.match(VALIDATION_RULES.email) ||
      dynamicMask.unmaskedValue.match(VALIDATION_RULES.phone)
    )
      isUsernameValid = true;
    else isUsernameValid = false;

    setButtonAvailability();
  }

  function setButtonAvailability() {
    if (isUsernameValid) submitElement.disabled = false;
    else submitElement.disabled = true;
  }
})();
