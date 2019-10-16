import { HIGHLIGHT_VALIDATION_CHARSET, WRONG_PASS_REG, REQUIRED_PASSWORD } from '../constants/passwordCharset.js';
import { fetchPassword } from './helpers';

// Привязывает логику к блоку 'password-block'
export default (
  getPassword,
  setPassword,
  getConfirmation,
  passwordElement = document.getElementById('password'),
  passwordConfirmElement = document.getElementById('password-confirm'),
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
  const generatedPasswordContainer = document.getElementById(
    'generated-password-container',
  );
  const generatedPassword = document.getElementById('generated-password');

  function generatePassword() {
    fetchPassword().then(data => {
      const password = data.password;
      generatedPassword.textContent = password;
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

    const regExp = new RegExp(WRONG_PASS_REG);
    if (regExp.test(password)) {
      passwordElement.value = (password.replace(WRONG_PASS_REG, ''))
    }
  }
  passwordElement.addEventListener('input', highlightRules);
  function checkPasswordConfirmation() {
    let checkPassword = (password) => REQUIRED_PASSWORD.test(password)
    const password = getPassword();
    const confirmation = getConfirmation();
    const ok = document.querySelectorAll('.passw_ok');
    // console.log(passwordElement);
    // console.log(passwordConfirmElement);
    // console.log(password);
    // console.log(confirmation);

    if (checkPassword(password) && confirmation === password) {
      passwordElement.classList.add('field-good');
      passwordConfirmElement.classList.add('field-good');
      ok.forEach((img_block) => img_block.classList.remove('hidden'));
    }
    else {
      passwordElement.classList.remove('field-good');
      passwordConfirmElement.classList.remove('field-good');
      ok.forEach((img_block) => img_block.classList.add('hidden'));
    }
  }
  passwordElement.addEventListener('input', checkPasswordConfirmation);
  passwordConfirmElement.addEventListener('input', checkPasswordConfirmation);
};
