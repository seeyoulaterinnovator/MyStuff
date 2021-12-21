import linkPasswords from './link-passwords.js';
import { createForm } from 'final-form';

import { setButtonAvailability, isEmpty } from './helpers.js';

import VALIDATION_RULES from '../constants/validationRules.js';

export default (function() {
  const formElement = document.getElementById('loginUpdatePasswordForm');
  if (!formElement) return;

  const submitElement = document.getElementById('submit');
  const passwordElement = document.getElementById('password-new');
  submitElement.disabled = true;

  // Создаем объект формы с помощью final-form
  const registered = {};
  const form = createForm({
    onSubmit: () => {},
    initialValues: {
      'password-new': '',
      'password-confirm': '',
    },
    validate,
    validateOnBlur: true,
  });
  // Валидация паролей
  function validate(values) {
    const errors = {};

    function checkExistence() {
      Object.keys(registered).forEach(name => {
        if (name && !values[name]) {
          errors[name] = 'Обязательное поле';
        }
      });
    }

    if (!values['password-new'].match(VALIDATION_RULES['password_8-16']))
      errors['password-new'] = 'Пароль не подходит';

    checkExistence();

    return errors;
  }
  // Регистрируем поля формы, за которыми будем наблюдать
  [...formElement]
    .filter(elem => elem.tagName === 'INPUT')
    .forEach(input => {
      registerField(input);
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
          input
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

      const submitButton = submitElement;

      if (!isEmpty(errors)) submitButton.disabled = true;
      else submitButton.disabled = false;
    },
    {
      values: true,
      errors: true,
    },
  );

  function getPassword() {
    return passwordElement.value;
  }
  function setPassword(password) {
    passwordElement.value = '';
    passwordElement.value = password;
    passwordElement.dispatchEvent(new Event('input'));
    passwordElement.dispatchEvent(new Event('focus'));
    passwordElement.dispatchEvent(new Event('blur'));
  }
  function getConfirmation() {
    return passwordElement.value;
  }
  function setConfirmation(confirmation) {
    passwordElement.value = '';
    passwordElement.value = password;
  }

  linkPasswords(getPassword, setPassword, getConfirmation, setConfirmation, passwordElement);

  passwordElement.addEventListener('input', () => {
    submitElement.disabled = !isEmpty(validate({'password-new': getPassword(), 'password-confirm': getConfirmation()}));
  });

})();
