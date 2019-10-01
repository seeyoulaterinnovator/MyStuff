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

  const formElement = document.getElementById('kc-update-profile-form');
  if (!formElement) return;

  // Маска для поля ввода телефона
  const phoneMask = IMask(document.getElementById('user.attributes.phone'), {
    mask: '+{7} (000) 000-00-00',
  });

  // Создаем объект формы с помощью final-form
  const updater = {};
  const form = createForm({
    onSubmit: () => {},
    initialValues: {
    },
    validate,
    validateOnBlur: true,
  });

  // Валидация полей
  function validate(values) {
    const errors = {};

    function checkExistence() {

      Object.keys(updater).forEach(name => {
        console.info('meeee:' +  'name' + name, values[name]);

        if (name && !values[name]) errors[name] = 'Обязательное поле';
      });
    }

    if (values.recaptcha === false)
      errors.recaptcha = 'Подтвердите, что вы не робот';

    if (!phoneMask.unmaskedValue.match(VALIDATION_RULES.phone))
      errors.phone = 'Неверный формат номера';

    checkExistence();

    return errors;
  }

  // Регистрируем поля формы, за которыми будем наблюдать
  [...formElement]
    .filter(elem => elem.tagName === 'INPUT')
    .forEach(input => {
      updaterField(input);
    });

  // Делаем то же и для капчи, которая загружается после всех остальных скриптов
  function onloadRecaptchaCallback() {
    const name = 'recaptcha';
    form.updaterField(
      name,
      fieldState => {
        if (!updater[name]) updater[name] = true;
      },
      { value: true, error: true },
    );
  }
  function recaptchaCallback() {
    console.info('Recaptcha Success');
    form.updaterField('recaptcha', fieldState => fieldState.change(true));
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

  function updaterField(input) {
    const { name } = input;

    form.updaterField(
      name,
      fieldState => {
        const { blur, change, error, focus, touched, value } = fieldState;
        const errorElement = document.getElementById(`${name}-error-message`);

        if (!updater[name]) {
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
          updater[name] = true;
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

      const submitButton = document.getElementById('submit');

      console.log(values, errors);

      if (!isEmpty(errors)) submitButton.disabled = true;
      else submitButton.disabled = false;
    },
    {
      values: true,
      errors: true,
    },
  );

  // function getPassword() {
  //   return form.getFieldState('password').value;
  // }
  // function setPassword(password) {
  //   // form.getFieldState('password').change(password);
  //   // form.getFieldState('password-confirm').change(password);
  //   // form.getFieldState('password-confirm').blur();
  // }
  // linkPasswords(getPassword, setPassword, document.getElementById('password'));
  console.log(document.querySelectorAll('.field__open'));
})();
