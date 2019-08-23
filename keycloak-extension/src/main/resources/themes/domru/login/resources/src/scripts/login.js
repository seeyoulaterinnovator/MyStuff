import IMask from 'imask';

export default (function() {
  if (!document.getElementById('loginForm')) return;

  const loginElement = document.getElementById('login');
  const usernameElement = document.getElementById('username');
  const passwordElement = document.getElementById('password');

  const dynamicLoginMask = IMask(usernameElement, {
    mask: [
      {
        mask: '+{7} (000) 000-00-00',
      },
      {
        mask: /^\S*@?\S*$/,
      },
    ],
  });

  let isUsernameValid = false;
  let isPasswordValid = false;

  usernameElement.addEventListener('input', () => {
    if (usernameElement.value !== '') isUsernameValid = true;
    else isUsernameValid = false;

    setButtonAvailability();
  });

  passwordElement.addEventListener('input', () => {
    if (passwordElement.value !== '') isPasswordValid = true;
    else isPasswordValid = false;

    setButtonAvailability();
  });

  function setButtonAvailability() {
    if (isUsernameValid && isPasswordValid) loginElement.disabled = false;
    else loginElement.disabled = true;
  }
})();
