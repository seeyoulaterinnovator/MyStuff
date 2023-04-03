import IMask from 'imask';
import {setButtonAvailability} from './helpers.js';
import Cookie from 'js-cookie';
import {WRONG_PASS_REG} from "../constants/passwordCharset";

export default (function functionName() {
  console.log("functionName")
  const formElement = document.getElementById('loginForm');
  if (!formElement) return;

  const submitElement = document.getElementById('submit');
  const usernameElement = document.getElementById('username');
  const secondUserName = document.getElementById("username-second");
  const passwordElement = document.getElementById('password');
  const cityElement = document.getElementById('domain-login');
  const secondSubmit = document.getElementById("submit-phone")
  debugger

  console.log(usernameElement)

  // if (submitElement && usernameElement && usernameElement.value === '' && passwordElement.value === '') {
  //   submitElement.disabled = true;
  // } else if (secondSubmit !== null && secondUserName !== null && secondUserName.value === '') {
  //   secondSubmit.disabled = true;
  // }
  if (usernameElement !== null && submitElement !== null) {
    if (passwordElement.value == '' && usernameElement.value == '') {
      submitElement.disabled = true;
    }

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
    formElement.addEventListener('submit', () => {
      if (usernameElement !== null) {
        usernameElement.value = dynamicMask.unmaskedValue;
        cityElement.value = Cookie.get('city-domain') || 'yar';
        return true;
      }
    });
    passwordElement.addEventListener('input', () => {
      replacePassword();
      setButtonAvailability(validate, submitElement);
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


  } else if (secondUserName !== null && secondSubmit !== null) {
    if (secondUserName.value == '') {
      secondSubmit.disabled = true;
    }
    const dynamicMaskTwo = IMask(secondUserName, {
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

    secondUserName.addEventListener('input', () => {
      setButtonAvailability(secondValidate, secondSubmit);
    });

    formElement.addEventListener('submit', () => {
      if (secondUserName !== null) {
        secondUserName.value = dynamicMask.unmaskedValue;
        cityElement.value = Cookie.get('city-domain') || 'yar';
        return true;
      }
    });

    function secondValidate() {
      isUsernameValid = dynamicMaskTwo.unmaskedValue !== '';

      return isUsernameValid;
    }
  }


  // Отправляем на сервер значение телефона без маски
  // Нужно так делать на каждой форме, где есть imask


})();

