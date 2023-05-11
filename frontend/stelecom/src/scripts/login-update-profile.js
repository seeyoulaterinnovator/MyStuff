import IMask from 'imask';
import { createForm } from 'final-form';

import { isEmpty } from './helpers';

import VALIDATION_RULES from '../constants/validationRules.js';

export default (function() {

  const formElement = document.getElementById('kc-update-profile-form');
  console.log(111);
  if (!formElement) return;

  let wrongEmail = document.querySelector('.bad_email');
  let wrongPhone = document.querySelector('.bad_phone');
  const firstName = document.getElementById('firstName');
  const emailField = document.getElementById('email');
  const phoneField = document.getElementById('phone');

  // Маска для поля ввода телефона
  const phoneMask = iMaskInstancePhone(phoneField);;

  // Убираем красные рамки инпутов на событии ввода после получения ошибки
  function cleanBorder() {
    if (wrongEmail) {
      wrongEmail.classList.remove('bad_email')
    }
    if (wrongPhone) {
      wrongPhone.classList.remove('bad_phone')
    }
    this.style.borderColor = '';
    this.removeEventListener('input', cleanBorder, false)
  }
  if (wrongEmail) {
    emailField.style.borderColor = '#e31e24';
    emailField.addEventListener('input', cleanBorder, false)
  }
  if (wrongPhone) {
    phoneField.style.borderColor = '#e31e24';
    phoneField.addEventListener('input', cleanBorder, false)
  }

  // Создаем объект формы с помощью final-form
  const registered = {};
  const form = createForm({
    onSubmit: () => {},
    initialValues: {
      firstName: firstName && firstName.value || '',
      email: emailField && emailField.value || '',
      phone: phoneField && phoneField.value || '',
    },
    validate,
    validateOnBlur: false,
  });

  // Валидация полей
  function validate(values) {
    const errors = {};

    function checkExistence() {
      Object.keys(registered).forEach(name => {
        if (name && !values[name]) errors[name] = 'Обязательное поле';
      });
    }

    if (values.recaptcha === false)
      errors.recaptcha = 'Подтвердите, что Вы не робот';

    if (!phoneMask.unmaskedValue.match(VALIDATION_RULES.phone))
      errors.phone = 'Неверный формат номера';

    if (values.email && !values.email.match(VALIDATION_RULES.email))
      errors.email = 'Неверный формат email';

    checkExistence();

    return errors;
  }

  // Регистрируем поля формы, за которыми будем наблюдать
  [...formElement]
    .filter(elem => elem.tagName === 'INPUT')
    .forEach(input => {
    registerField(input);
    });

  // Отправляем на сервер значение телефона без маски
  // Нужно так делать на каждой форме, где есть imask
  formElement.addEventListener('submit', () => {
    phoneField.value = phoneMask.unmaskedValue;
    return true;
  });

  // Делаем то же и для капчи, которая загружается после всех остальных скриптов
  function onloadRecaptchaCallback() {
    const name = 'recaptcha';
    form.registerField(
      name,
      fieldState => {
        if (!registered[name]) registered[name] = true;
      },
      { value: true, error: true },
    );
  }
  function recaptchaCallback() {
    console.info('Recaptcha Success');
    form.registerField('recaptcha', fieldState => fieldState.change(true));
    form.getFieldState('recaptcha').blur();
  }
  function recaptchaExpiredCallback() {
    console.warn('Recaptcha Expired');
    form.getFieldState('recaptcha').change(false);
  }
  function recaptchaErrorCallback() {
    console.error('Recaptcha Error');
  }
  window.onloadRecaptchaCallback = onloadRecaptchaCallback;
  window.recaptchaCallback = recaptchaCallback;
  window.recaptchaExpiredCallback = recaptchaExpiredCallback;
  window.recaptchaErrorCallback = recaptchaErrorCallback;

  // resizing ReCaptcha function
  function scaleCaptcha() {
    const reCaptcha = document.querySelector(".g-recaptcha");
    const reCaptchaWidth = 304;
    const containerWidth = document.getElementById('update-profile-submit').offsetWidth;
    if(reCaptcha && reCaptchaWidth !== containerWidth) {
      const captchaScale = containerWidth / reCaptchaWidth;
      reCaptcha.style.transform = 'scale('+captchaScale+')';
    }
  }
  // resizing ReCaptcha initial
  scaleCaptcha();
  // resizing ReCaptcha on window resize
  window.addEventListener('resize', function(){
    scaleCaptcha();
  });

  function registerField(input) {
    const { name } = input;

    form.registerField(
      name,
      fieldState => {
        const { blur, change, error, focus, touched, value } = fieldState;
        const errorElement = document.getElementById(`${name}-error-message`);

        if (!registered[name]) {
          // first time, register event listeners
          input.addEventListener('blur', () => blur());
          input.addEventListener('input', event =>
            change(
              input.type === 'checkbox'
                ? event.target.checked
                : event.target.value,
            ),
          );
          input.addEventListener('focus', () => focus());
          registered[name] = true;
        }

        // update value
        if (input.type === 'checkbox') {
          input.checked = value;
        } else {
          input.value = value === undefined ? '' : value;
        }

        // show/hide errors
        if (errorElement) {
          if (touched && error) {
            input.parentElement.classList.add('field--error');

            if (errorElement) errorElement.textContent = error;
          } else {
            input.parentElement.classList.remove('field--error');
            if (errorElement) errorElement.textContent = '';
          }
        }
      },
      {
        value: true,
        error: true,
        touched: true,
      },
    );
  }

  // Подписываемся на ошибки, чтобы включать или отключать кнопку отправки
  const unsubscribe = form.subscribe(
    formState => {
      const { values, errors } = formState;

      const submitButton = document.getElementById('update-profile-submit');

      if (!isEmpty(errors)) submitButton.disabled = true;
      else submitButton.disabled = false;
    },
    {
      values: true,
      errors: true,
    },
  );
})();
