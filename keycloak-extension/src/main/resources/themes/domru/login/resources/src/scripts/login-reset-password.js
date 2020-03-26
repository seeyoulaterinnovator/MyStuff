import IMask from 'imask';
import VALIDATION_RULES from '../constants/validationRules.js';
import { setButtonAvailability } from './helpers.js';
import { city } from '../Cities/stores';

export default (function() {
  const formElement = document.getElementById('loginResetPasswordForm');
  if (!formElement) return;

  const cityInput = document.getElementById('city');
  const unsubscribeCity = city.subscribe(value => {
    cityInput.value = value
  });

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

  usernameElement.addEventListener('input', () => {
    setButtonAvailability(validate, submitElement);
  });

  function validate() {
    return !!(
      dynamicMask.unmaskedValue.match(VALIDATION_RULES.email) ||
      dynamicMask.unmaskedValue.match(VALIDATION_RULES.phone)
    );
  }
})();
