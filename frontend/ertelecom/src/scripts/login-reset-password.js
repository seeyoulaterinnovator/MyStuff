import VALIDATION_RULES from '../constants/validationRules.js';
import {
  iMaskInstancePhoneWithoutPrepareAndEmail,
  setButtonAvailability
} from './helpers.js';
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

  const dynamicMask = iMaskInstancePhoneWithoutPrepareAndEmail(usernameElement);

  /**
   * Phone Listener Start
   *
   * Этот Listener и переменные нужны для того, чтобы
   * при написании 8 появлялось +7, но при нажатии на букву, +7 заменялась на 8-ку.
   * То есть, когда пишем цифры с "8", например, "8951", то получим в выводе "+7951",
   * но если мы введём "8951abc", то получим "8951abc".
   * Не советую разбираться в коде или фиксить код этого Listener'а
   */
  let firstCharInUsernameElement = '';
  let testOnlyDigitsFirstTime = false;
  let emailStage = false;
  usernameElement.addEventListener('input', () => {
    if (dynamicMask.unmaskedValue.length === 0) {
      testOnlyDigitsFirstTime = false;
      emailStage = false;
    }

    const onlyDigits = /^\d+$/.test(dynamicMask.unmaskedValue);

    if (onlyDigits === true && testOnlyDigitsFirstTime === false) {
      testOnlyDigitsFirstTime = true;
      if (dynamicMask.unmaskedValue === '78') {
        firstCharInUsernameElement = '8';
        dynamicMask.unmaskedValue = '7';
      } else if (dynamicMask.unmaskedValue.length === 2) {
        firstCharInUsernameElement = dynamicMask.unmaskedValue.charAt(1);
      } else {
        firstCharInUsernameElement = '7';
      }
    }

    if (onlyDigits === false && testOnlyDigitsFirstTime === true && emailStage === false) {
      dynamicMask.unmaskedValue = firstCharInUsernameElement + dynamicMask.unmaskedValue.slice(1);
      emailStage = true;
    }
  });
  /* Phone Listener End */

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
