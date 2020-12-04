import IMask from 'imask';
import { createForm } from 'final-form';

import linkPasswords from './link-passwords.js';
import { isEmpty } from './helpers';

import VALIDATION_RULES from '../constants/validationRules.js';

export default (function() {
  const setVisiblePass = field => e => {
    const active = field.classList.contains('active');
    const open = field.querySelector('.open');
    const close = field.querySelector('.close');

    const inputId = field.getAttribute('target');
    const input = document.getElementById(inputId);

    if (active) {
      field.classList.remove('active');
      open.classList.add('hidden');
      close.classList.remove('hidden');
      input.setAttribute('type', 'password');
    } else {
      field.classList.add('active');
      open.classList.remove('hidden');
      close.classList.add('hidden');
      input.setAttribute('type', 'input');
    }
  };

  document
    .querySelectorAll('.field__open')
    .forEach(eye => eye.addEventListener('click', setVisiblePass(eye), false));

  // @todo

  // Wrong email and/or phone number
  let wrongEmail = document.querySelector('.bad_email');
  let wrongPhone = document.querySelector('.bad_phone');
  const orgNameField = document.getElementById('orgName');
  const firstName = document.getElementById('firstName');
  const emailField = document.getElementById('email');
  const phoneField = document.getElementById('phone');

  const formElement = document.getElementById('registrationForm');
  if (!formElement) return;

  // Маска для поля ввода телефона
  const phoneMask = IMask(document.getElementById('phone'), {
    mask: '+{7} (000) 000-00-00',
  });

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
      orgName: orgNameField && orgNameField.value || '',
      firstName: firstName && firstName.value || '',
      lastName: '-',
      email: emailField && emailField.value || '',
      phone: phoneField && phoneField.value || '',
      password: '',
      'password-confirm': '',
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

    if (!values.password.match(VALIDATION_RULES['password_8-16']))
      errors.password = 'Пароль не подходит';

    // if (!values['password-confirm'].match(VALIDATION_RULES['password_8-16']))
    //   errors.password = 'Пароль не подходит. Попробуйте другой';

    if (values['password-confirm'] !== values.password)
      errors['password-confirm'] = 'Пароли не совпадают';

    if (values.recaptcha === false)
      errors.recaptcha = 'Подтвердите, что Вы не робот';

    if (!phoneMask.unmaskedValue.match(VALIDATION_RULES.phone))
      errors.phone = 'Неверный формат номера';

    if (!values.email.match(VALIDATION_RULES.email))
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
    const containerWidth = document.getElementById('password').offsetWidth;
    if(reCaptchaWidth !== containerWidth) {
      const captchaScale = containerWidth / reCaptchaWidth;
      if (reCaptcha)
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
        // Скрываем сообщение об ошибке во время фокуса на ошибочном поле
        if (errorElement) {
          if (touched && error) {
            input.parentElement.classList.add('field--error');
            errorElement.textContent = error;
            input.addEventListener('focus', (e) => {
              e.target.parentElement.nextElementSibling.classList.add('hidden')
            })
            input.addEventListener('blur', (e) => {
              e.target.parentElement.nextElementSibling.classList.remove('hidden')
            })
          } else {
            input.parentElement.classList.remove('field--error');
            errorElement.textContent = '';
            input.removeEventListener('focus', (e) => {
              e.target.parentElement.nextElementSibling.classList.add('hidden')
            })
            input.removeEventListener('blur', (e) => {
              e.target.parentElement.nextElementSibling.classList.remove('hidden')
            })
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

      const submitButton = document.getElementById('submit');

      if (!isEmpty(errors)) submitButton.disabled = true;
      else submitButton.disabled = false;
    },
    {
      values: true,
      errors: true,
    },
  );

  function getPassword() {
    return form.getFieldState('password').value;
  }
  function setPassword(password) {
    form.getFieldState('password').change(password);
    // form.getFieldState('password-confirm').change(password);
    // form.getFieldState('password-confirm').blur();
  }
  function getConfirmation() {
    return form.getFieldState('password-confirm').value;
  }
  function setConfirmation(confirmation) {
    form.getFieldState('password-confirm').change(confirmation);
  }
  linkPasswords(getPassword, setPassword, getConfirmation, setConfirmation, document.getElementById('password'), document.getElementById('password-confirm'));
})();
