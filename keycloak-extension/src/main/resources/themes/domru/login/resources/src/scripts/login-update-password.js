import linkPasswords from './link-passwords.js';
import { createForm } from 'final-form';

import { setButtonAvailability, isEmpty } from './helpers.js';

import VALIDATION_RULES from '../constants/validationRules.js';

export default (function() {
  const formElement = document.getElementById('loginUpdatePasswordForm');
  if (!formElement) return;

  const submitElement = document.getElementById('submit');
  const passwordElement = document.getElementById('password-new');
  const passwordConfirmElement = document.getElementById('password-confirm');
  submitElement.disabled = true;

  let isPasswordExists = false;
  let isPasswordConfirmExists = false;
  let arePasswordsEqual = false;

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
        if (name && !values[name]) errors[name] = 'Обязательное поле';
      });
    }

    if (!values['password-new'].match(VALIDATION_RULES['password_8-16']))
      errors['password-new'] = 'Пароль не подходит. Попробуйте другой';

    if (values['password-confirm'] !== values['password-new'])
      errors['password-confirm'] = 'Пароли не совпадают';

    checkExistence();
    // isPasswordExists = passwordElement.value !== '';
    // isPasswordConfirmExists = passwordConfirmElement.value !== '';
    // arePasswordsEqual = passwordElement.value === passwordConfirmElement.value;

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
    return form.getFieldState('password-new').value;
  }
  function setPassword(password) {
    // passwordElement.value = password;
    // passwordConfirmElement.value = password;
    // setButtonAvailability(validate, submitElement);
  }
  function getConfirmation() {
    return form.getFieldState('password-confirm').value;
  }

  linkPasswords(getPassword, setPassword, getConfirmation, passwordElement, passwordConfirmElement);

  // passwordElement.addEventListener('input', () => {
  //   setButtonAvailability(validate, submitElement);
  // });
  //
  // passwordConfirmElement.addEventListener('input', () => {
  //   setButtonAvailability(validate, submitElement);
  // });

})();
