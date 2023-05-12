import VALIDATION_RULES from '../constants/validationRules.js';
import {iMaskInstancePhoneAndEmail, setButtonAvailability} from './helpers.js';
import { domain } from '../Cities/stores.js';

export default (function() {
  const formElement = document.getElementById('loginResetPasswordForm');
  if (!formElement) return;

  const cityInput = document.getElementById('city');
  domain.subscribe(value => {
    cityInput.value = value;
  });

  const submitElement = document.getElementById('submit');
  const usernameElement = document.getElementById('username');
  submitElement.disabled = true;

  const dynamicMask = iMaskInstancePhoneAndEmail(usernameElement);;

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
