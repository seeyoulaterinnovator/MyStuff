import IMask from 'imask';
import { setButtonAvailability } from './helpers.js';
import Cookie from 'js-cookie';

export default (function() {
  const formElement = document.getElementById('loginForm');
  if (!formElement) return;

  const submitElement = document.getElementById('submit');
  const usernameElement = document.getElementById('username');
  const passwordElement = document.getElementById('password');
  const cityElement = document.getElementById('domain-login');
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

  // Отправляем на сервер значение телефона без маски
  // Нужно так делать на каждой форме, где есть imask
  formElement.addEventListener('submit', () => {
    usernameElement.value = dynamicMask.unmaskedValue;
    cityElement.value = Cookie.get('city-domain');
    return true;
  });

  function validate() {
    isPasswordExists = passwordElement.value !== '';
    isUsernameValid = dynamicMask.unmaskedValue !== '';

    return isPasswordExists && isUsernameValid;
  }
})();
