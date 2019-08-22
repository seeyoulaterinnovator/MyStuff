import IMask from 'imask';

import { createForm } from 'final-form';
import { isEmpty, getPassword } from './helpers';
import PASSWORD_CHARSET from '../constants/passwordCharset.js';
import VALIDATION_RULES from '../constants/validationRules.js';

export default (function() {
  const formElement = document.getElementById('registrationForm');
  if (!formElement) return;

  // Маска для поля ввода телефона
  const phoneMask = IMask(document.getElementById('phone'), {
    mask: '+{7} (000) 000-00-00',
  });

  // Создаем объект формы с помощью final-form
  const registered = {};
  const form = createForm({
    onSubmit,
    initialValues: {
      orgName: '',
      firstName: '',
      lastName: '-',
      email: '',
      password: '',
      'password-confirm': '',
      recaptcha: false,
    },
    validate,
    validateOnBlur: true,
  });

  function onSubmit(values) {
    console.log('onsubmit', values);
  }

  // Валидация полей
  function validate(values) {
    const errors = {};

    function checkExistence() {
      Object.keys(registered).forEach(name => {
        if (name && !values[name]) errors[name] = 'Обязательное поле';
      });
    }

    if (!values.password.match(VALIDATION_RULES['password_8-16']))
      errors.password = 'Пароль не подходит. Попробуйте другой';

    if (values['password-confirm'] !== values.password)
      errors['password-confirm'] = 'Пароли не совпадают';

    if (!values.recaptcha) errors.recaptcha = 'Подтвердите, что вы не робот';

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
    form.getFieldState('recaptcha').change(true);
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

  // Красим блоки в разные цвета согласно правилам валидации пароля
  function highlightRules() {
    console.log('highlight');
    const password = form.getFieldState('password').value;

    for (let category in PASSWORD_CHARSET) {
      const ruleElement = document.getElementById(`${category}-password`);

      if (
        ![...password].some(character =>
          [...PASSWORD_CHARSET[category]].includes(character),
        )
      ) {
        ruleElement.classList.remove('text-accentGreen');
        ruleElement.classList.add('text-accentRed');
      } else {
        ruleElement.classList.remove('text-accentRed');
        ruleElement.classList.add('text-accentGreen');
      }
    }
  }
  document.getElementById('password').addEventListener('input', highlightRules);

  // Генерация пароля
  const generatePasswordButton = document.getElementById(
    'generate-password-button',
  );
  const refreshPasswordButton = document.getElementById(
    'refresh-password-button',
  );
  const generatedPasswordContainer = document.getElementById(
    'generated-password-container',
  );
  const generatedPassword = document.getElementById('generated-password');

  function generatePassword() {
    getPassword().then(data => {
      const password = data.password;
      generatedPassword.textContent = password;
      form.getFieldState('password').change(password);
      form.getFieldState('password-confirm').change(password);
      form.getFieldState('password-confirm').blur();
      highlightRules();
    });
  }

  generatePasswordButton.addEventListener('click', () => {
    generatePassword();
    generatedPasswordContainer.classList.remove('hidden');
    generatePasswordButton.classList.add('hidden');
  });

  refreshPasswordButton.addEventListener('click', () => {
    generatePassword();
    refreshPasswordButton.classList.add('rotate');
    setTimeout(() => {
      refreshPasswordButton.classList.remove('rotate');
    }, 500);
  });
})();
