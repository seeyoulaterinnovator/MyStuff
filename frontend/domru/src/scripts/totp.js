import {setButtonAvailability} from './helpers.js';
import Timer from './timer.js';

export default (function() {
  const formElement = document.getElementById('totpForm');
  if (!formElement) return;

  const timerElement = document.getElementById('timer');
  const timerElement2 = document.getElementById('timer2');
  const submitElement = document.getElementById('accept');
  const resendElement = document.getElementById('resend');
  const sentCode = document.getElementById('sentCode');

  const codeNumbers = document.getElementById('codeNumbers');
  const expirationSeconds = document.getElementById('expirationSeconds');
  submitElement.disabled = true;

  // Инициируем обратный отсчет таймера.
  // После него появится кнопка "Отправить еще раз"
  const timer = new Timer(expirationSeconds.value || 30);
  timer.timeElement = document.getElementById('timer-time');

  if (expirationSeconds.value == 0) {
    switchTimer();
    timer.stopTimer();
  } else {
    timer.callback = switchTimer;
  }

  function switchTimer() {
    if (timerElement) {
      timerElement.classList.remove('flex');
      timerElement.classList.add('hidden');
    }

    if (timerElement2) {
      timerElement2.classList.remove('flex');
      timerElement2.classList.add('hidden');
    }

    if (sentCode) {
      sentCode.classList.remove('hidden');
      sentCode.disabled = false;
    }

    if (resendElement) {
      resendElement.classList.remove('hidden');
      resendElement.disabled = false;
    }
  }

  // Обрабатываем события на каждом инпуте
  const inputs = [1, 2, 3, 4, 5, 6]
    .slice(0, codeNumbers.value || 6)
    .map(index => document.getElementById(`smscode-${index}`));

  inputs.forEach((input, index) => {
    input.addEventListener('input', () => {
      setButtonAvailability(validate, submitElement);

      jumpToNextInput(input);
      cutRedundant(input);
      checkInputs();
    });

    input.addEventListener('keypress', event => {
      checkNumeric(event);
    });
  });

  function checkInputs() {
    console.log("checkInputs")
    const inputs = [1, 2, 3, 4, 5, 6]
      .slice(0, codeNumbers.value || 6)
      .map(index => document.getElementById(`smscode-${index}`));
    const filledInputs = inputs
      .filter(input => input.value.length === 1);
    if (filledInputs.length === inputs.length) {
      submitElement.click();
    }
  }

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

  function isInputsFilled(exceptInput) {
    return inputs.every(input => {
      if (input.id === exceptInput.id) return true;
      return input.value;
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
  // Закомментил дублирующиеся изменения по автоматическому нажатию кнопки при достижении определённого условия
  // function checkInputs() {
  //   const inputs = [1, 2, 3, 4, 5, 6].slice(0, codeNumbers.value || 6).map(index => document.getElementById(`smscode-${index}`));
  //   const filledInputs = inputs.filter(input => input.value.length === 1);
  //   if (filledInputs.length === inputs.length) {
  //     submitElement.click();
  //   }
  // }
  //
  // inputs.forEach((input, index) => {
  //   input.addEventListener('input', () => {
  //     setButtonAvailability(validate, submitElement);
  //
  //     jumpToNextInput(input);
  //     cutRedundant(input);
  //
  //     checkInputs();
  //   });
  // });
})();
