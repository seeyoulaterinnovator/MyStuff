import {
  iMaskInstancePhoneWithoutPrepareAndEmail,
  setButtonAvailability
} from './helpers.js';
import Cookie from 'js-cookie';
import {WRONG_PASS_REG} from "../constants/passwordCharset";

export default (function() {
  const formElement = document.getElementById('loginForm');
  if (!formElement) return;

  const submitElement = document.getElementById('submit');
  const usernameElement = document.getElementById('username');
  const passwordElement = document.getElementById('password');
  const cityElement = document.getElementById('domain-login');
  submitElement.disabled = true;

  const dynamicMask = iMaskInstancePhoneWithoutPrepareAndEmail(usernameElement);;

  let isUsernameValid = false;
  let isPasswordExists = false;

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

  passwordElement.addEventListener('input', () => {
    replacePassword();
    setButtonAvailability(validate, submitElement);
  });

  // Отправляем на сервер значение телефона без маски
  // Нужно так делать на каждой форме, где есть imask
  formElement.addEventListener('submit', () => {
    usernameElement.value = dynamicMask.unmaskedValue;
    cityElement.value = Cookie.get('city-domain') || 'interzet';
    return true;
  });

  function validate() {
    isPasswordExists = passwordElement.value !== '';
    isUsernameValid = dynamicMask.unmaskedValue !== '';

    return isPasswordExists && isUsernameValid;
  }

  function replacePassword() {
    const regExp = new RegExp(WRONG_PASS_REG);
    const passwordRaw = passwordElement.value;
    if (regExp.test(passwordRaw)) {
      const replacePassword = passwordRaw.replace(WRONG_PASS_REG, '');
      passwordElement.value = replacePassword;
    }
  }
})();
