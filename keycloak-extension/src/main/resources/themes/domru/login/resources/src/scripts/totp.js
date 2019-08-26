import { setButtonAvailability } from './helpers.js';
import Timer from './timer.js';

export default (function() {
  const formElement = document.getElementById('totpForm');
  if (!formElement) return;

  const submitElement = document.getElementById('accept');
  const resendElement = document.getElementById('resend');
  submitElement.disabled = true;
  resendElement.disabled = true;

  // Инициируем обратный отсчет таймера.
  // После него появится кнопка "Отправить еще раз"
  const timer = new Timer(30);
  timer.timeElement = document.getElementById('timer-time');
  timer.callback = switchTimer;

  function switchTimer() {
    const timerElement = document.getElementById('timer');

    timerElement.classList.remove('flex');
    timerElement.classList.add('hidden');
    resendElement.classList.remove('hidden');
    resendElement.disabled = false;
  }

  // Обрабатываем события на каждом инпуте
  const inputs = [1, 2, 3, 4, 5, 6].map(index =>
    document.getElementById(`smscode-${index}`),
  );

  inputs.forEach(input => {
    input.addEventListener('input', () => {
      setButtonAvailability(validate, submitElement);

      jumpToNextInput(input);
      cutRedundant(input);
    });

    input.addEventListener('keypress', event => {
      checkNumeric(event);
    });

    input.addEventListener('focus', () => {
      highlightNext(input);
    });

    input.addEventListener('blur', () => {
      clearHighlights();
    });
  });

  function validate() {
    return !inputs.some(input => input.value === '');
  }

  // http://jsfiddle.net/DRSDavidSoft/zb4ft1qq/1/
  function cutRedundant(input) {
    if (input.value.length > input.maxLength)
      input.value = input.value.slice(0, input.maxLength);
  }

  function checkNumeric(evt) {
    const theEvent = evt || window.event;
    let key = theEvent.keyCode || theEvent.which;
    key = String.fromCharCode(key);
    const regex = /[0-9]|\./;
    if (!regex.test(key)) {
      theEvent.returnValue = false;
      if (theEvent.preventDefault) theEvent.preventDefault();
    }
  }

  function jumpToNextInput(input) {
    if (input.value.length === input.maxLength) {
      const currentInput = input.nextElementSibling;

      if (currentInput) currentInput.focus();
    }
  }

  function highlightNext(input) {
    const currentInput = input.nextElementSibling;
    if (currentInput) currentInput.classList.add('border-accentBlue');
  }

  function clearHighlights() {
    inputs.forEach(input => {
      input.classList.remove('border-accentBlue');
    });
  }

  // Заполняем невидимый input, чтобы бэк получил уже готовый код
  formElement.addEventListener('submit', () => {
    const resultSmscodeElement = document.getElementById('smscode');
    const resultSmscode = inputs
      .map(input => input.value)
      .reduce((acc, currentValue) => acc + currentValue);
    resultSmscodeElement.value = resultSmscode;
  });
})();
