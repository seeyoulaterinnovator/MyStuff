import {
  iMaskInstancePhone,
  iMaskInstancePhoneWithoutPrepareAndEmail,
  setButtonAvailability
} from './helpers.js';
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
  const errorMessageElement = document.querySelector(".span-line");
  debugger

  console.log(usernameElement)

  if (usernameElement !== null && submitElement !== null) {
    if (passwordElement.value == '' && usernameElement.value == '') {
      submitElement.disabled = true;
    }

    const dynamicMask = iMaskInstancePhoneWithoutPrepareAndEmail(usernameElement);
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
    usernameElement.addEventListener("mouseout", (event) => {
      const unmaskedValue = dynamicMask.unmaskedValue;
      const onlyDigits = /^\d+$/.test(unmaskedValue);

      if (!unmaskedValue) {
        errorMessageElement.textContent = "";
        usernameElement.classList.remove("field__input--error");
      } else if (onlyDigits) {
        const isPhoneNumber = /^79\d{9}$/.test(unmaskedValue);
        if (isPhoneNumber) {
          errorMessageElement.textContent = "";
          usernameElement.classList.remove("field__input--error");
        } else {
          errorMessageElement.textContent = "Введён неверный номер телефона";
          usernameElement.classList.add("field__input--error");
        }
      } else {
        const isEmail = /^[a-zA-Z\d_!#$%&�*+/=?`{|}~^.-]+@[a-zA-Z\d.-]+$/.test(unmaskedValue);
        if (isEmail) {
          errorMessageElement.textContent = "";
          usernameElement.classList.remove("field__input--error");
        } else {
          errorMessageElement.textContent = "Введён неверный E-mail";
          usernameElement.classList.add("field__input--error");
        }
      }
    });

    usernameElement.addEventListener("input", (event) => {
      const unmaskedValue = dynamicMask.unmaskedValue;

      if (!unmaskedValue || event.inputType === "insertFromPaste" || event.inputType === "insertText" || event.inputType === "deleteContentBackward" || event.inputType === "deleteContentForward") {
        errorMessageElement.textContent = "";
        usernameElement.classList.remove("field__input--error");
      }
    });


  } else if (secondUserName !== null && secondSubmit !== null) {
    if (secondUserName.value == '') {
      secondSubmit.disabled = true;
    }
    const dynamicMaskTwo = iMaskInstancePhone(secondUserName);
    let isUsernameValid = false;
    let isPasswordExists = false;

    secondUserName.addEventListener('input', () => {
      setButtonAvailability(secondValidate, secondSubmit);
    });

    formElement.addEventListener('submit', () => {
      if (secondUserName !== null) {
        secondUserName.value = dynamicMaskTwo.unmaskedValue;
        cityElement.value = Cookie.get('city-domain') || 'yar';
        return true;
      }
    });

    function secondValidate() {
      isUsernameValid = dynamicMaskTwo.unmaskedValue !== '';

      return isUsernameValid;
    }
    secondUserName.addEventListener("mouseout", (event) => {
      const unmaskedValue = dynamicMaskTwo.unmaskedValue;
      const onlyDigits = /^\d+$/.test(unmaskedValue);

      if (onlyDigits) {
        const isPhoneNumber = /^79\d{9}$/.test(unmaskedValue);
        if (isPhoneNumber) {
          errorMessageElement.textContent = "";
          secondUserName.classList.remove("field__input--error");
        } else {
          errorMessageElement.textContent = "Введён неверный номер телефона";
          secondUserName.classList.add("field__input--error");
        }
      }
    });

    secondUserName.addEventListener("input", (event) => {
      const unmaskedValue = dynamicMaskTwo.unmaskedValue;

      if (!unmaskedValue || event.inputType === "insertFromPaste" || event.inputType === "insertText" || event.inputType === "deleteContentBackward" || event.inputType === "deleteContentForward") {
        errorMessageElement.textContent = "";
        secondUserName.classList.remove("field__input--error");
      }
    });
  }

})();
