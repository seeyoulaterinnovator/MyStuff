import { HIGHLIGHT_VALIDATION_CHARSET } from '../constants/passwordCharset.js';
import { fetchPassword } from './helpers';

// Функция нужна для того, чтобы привязать логику к блоку password-block
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

      if (
        ![...password].some(character =>
          [...HIGHLIGHT_VALIDATION_CHARSET[category]].includes(character),
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
  passwordElement.addEventListener('input', highlightRules);
};
