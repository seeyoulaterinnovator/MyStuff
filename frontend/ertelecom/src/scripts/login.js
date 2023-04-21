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
  const errorMessageElement = document.querySelector(".span-line");
  debugger

  console.log(usernameElement)

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
          errorMessageElement.textContent = "����� �������� E-mail";
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
          errorMessageElement.textContent = "����� �������� ����� ��������";
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

    secondUserName.addEventListener("keypress", (event) => {
      if (!/^\d$/.test(event.key) && event.key !== "Backspace" && event.key !== "Delete" && event.key !== "Enter") {
        event.preventDefault();
      }
    });
  }

})();
