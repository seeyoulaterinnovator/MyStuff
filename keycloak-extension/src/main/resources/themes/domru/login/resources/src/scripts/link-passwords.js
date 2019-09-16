import { HIGHLIGHT_VALIDATION_CHARSET } from '../constants/passwordCharset.js';
import { fetchPassword } from './helpers';

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
  const generatedPasswordContainer = document.getElementById(
    'generated-password-container',
  );
  const generatedPassword = document.getElementById('generated-password');

  function generatePassword() {
    fetchPassword().then(data => {
      const password = data.password;
      generatedPassword.textContent = password;
      setPassword(password);
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
  }
  passwordElement.addEventListener('input', highlightRules);
};
