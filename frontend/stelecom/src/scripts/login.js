import {iMaskInstancePhoneAndEmail, setButtonAvailability} from './helpers.js';
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

  const dynamicMask = iMaskInstancePhoneAndEmail(usernameElement);;

  let isUsernameValid = false;
  let isPasswordExists = false;

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
