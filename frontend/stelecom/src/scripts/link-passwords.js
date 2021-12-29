import {HIGHLIGHT_VALIDATION_CHARSET, REQUIRED_PASSWORD, WRONG_PASS_REG} from '../constants/passwordCharset.js';
import {fetchPassword} from './helpers';

// Привязывает логику к блоку 'password-block'
export default (
  getPassword,
  setPassword,
  passwordElement = document.getElementById('password'),
) => {
  const passwordBlock = document.getElementById('password-block');
  if (!passwordBlock) return;

  // Генерация пароля
  const generatePasswordButton = document.getElementById(
    'generate-password-button',
  );
  const refreshPasswordButton = document.getElementById(
    'refresh-password-button',
  );

  const generatedPassword = document.getElementById('generated-password');

  function generatePassword() {
    fetchPassword().then(data => {
      generatedPassword.textContent = data.password;
      setPassword(data.password);
      inputSomePass(passwordElement);
      highlightRules();
    });
  }

  generatePasswordButton.addEventListener('click', () => {
    generatePassword();
    refreshPasswordButton.classList.add('rotate');
    setTimeout(() => {
      refreshPasswordButton.classList.remove('rotate');
    }, 500);
  });

  refreshPasswordButton.addEventListener('click', () => {
    generatePassword();
    refreshPasswordButton.classList.add('rotate');
    setTimeout(() => {
      refreshPasswordButton.classList.remove('rotate');
    }, 500);
  });

  // Красим блоки в разные цвета согласно правилам валидации пароля
  function highlightRules() {
    const password = getPassword();

    for (let category in HIGHLIGHT_VALIDATION_CHARSET) {
      const ruleElement = document.getElementById(`${category}-password`);

      let ruleAccepted = typeof HIGHLIGHT_VALIDATION_CHARSET[category] === 'string'
        && [...password].some(character => [...HIGHLIGHT_VALIDATION_CHARSET[category]].includes(character));

      if (typeof HIGHLIGHT_VALIDATION_CHARSET[category] !== 'string' ){
        ruleAccepted = true;

        Object.values(HIGHLIGHT_VALIDATION_CHARSET[category]).map(rule => {
          if (![...password].some(character => [...rule].includes(character))) ruleAccepted = false;
        })
      }

      if (ruleAccepted) {
        ruleElement.classList.remove('text-accentRed');
        ruleElement.classList.add('text-accentGreen');
      } else {
        ruleElement.classList.remove('text-accentGreen');
        ruleElement.classList.add('text-accentRed');
      }
    }
  }
  passwordElement.addEventListener('input', highlightRules);

  function inputSomePass(element) {
    const elementId = element.id;
    const passwordRaw = getPassword();
    const regExp = new RegExp(WRONG_PASS_REG);

    if (regExp.test(passwordRaw)) {
      const replacePassword = passwordRaw.replace(WRONG_PASS_REG, '');
      // element.value = replacePassword;
      setPassword(replacePassword);
    }
  }
  passwordElement.addEventListener('input', function() { inputSomePass(passwordElement); });

  function checkPasswordConfirmation() {
    const password = getPassword();
    const ok = document.querySelectorAll('.passw_ok');

    if (REQUIRED_PASSWORD.test(password)) {
      passwordElement.classList.add('field-good');
      ok.forEach((img_block) => img_block.classList.remove('hidden'));
      generatedPassword.textContent = password;
    }
    else {
      passwordElement.classList.remove('field-good');
      generatedPassword.textContent = '';
      ok.forEach((img_block) => img_block.classList.add('hidden'));
    }
  }
  passwordElement.addEventListener('input', checkPasswordConfirmation);
};
