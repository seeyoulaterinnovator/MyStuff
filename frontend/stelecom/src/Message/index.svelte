<script>
  import {
    show,
    text,
    isRegistration,
    isUpdateProfile,
    isBadEmail,
    isBadPhone,
    loginUrl,
  } from './stores.js';

  const hasRegistration = window.location.href.includes('registration');
  const hasUpdateProfile = window.location.href.includes('UPDATE_PROFILE');
  const alert = document.querySelector('.alert .text-accentRed');
  const hasAlert = !!alert;
  const hasBadEmail = hasAlert && alert.classList.contains('bad_email');
  const hasBadPhone = hasAlert && alert.classList.contains('bad_phone');
  const newText = hasAlert ? alert.innerText : '';
  const emailElement = document.getElementsByName('email')[0];
  const phoneElement = document.getElementsByName('phone')[0];

  let email = '';
  let phone = '';

  if (emailElement) {
    email = emailElement ? emailElement.value : '';
    emailElement.value = '';
  }

  if (phoneElement) {
    phone = phoneElement ? phoneElement.value : '';
    phoneElement.value = '';
  }

  hasAlert && alert.parentElement.remove();

  show.set(hasAlert);
  isBadEmail.set(hasBadEmail);
  isBadPhone.set(hasBadPhone);
  isRegistration.set(hasRegistration);
  isUpdateProfile.set(hasUpdateProfile);
  text.set(newText);

  if (hasRegistration) {
    if (hasBadEmail && hasBadPhone) {
      text.set('Адрес электронной почты и номер мобильного телефона уже используется на другой учетной записи. Если Вы уже регистрировались, попробуйте войти в свою учетную запись, либо укажите другой адрес электронной почты и номер мобильного телефона.');
    } else if (hasBadEmail) {
      text.set('Адрес электронной почты уже используется на другой учетной записи. Если Вы уже регистрировались, попробуйте войти в свою учетную запись, либо укажите другой адрес электронной почты.');
    } else if (hasBadPhone) {
      text.set('Номер мобильного телефона уже используется на другой учетной записи. Если Вы уже регистрировались, попробуйте войти в свою учетную запись, либо укажите другой номер мобильного телефона.');
    } else {
      text.set('Регистрация временно не доступна попробуйте повторить попытку позже.');
    }
  } else if (hasUpdateProfile) {
    if (hasBadEmail && hasBadPhone) {
      if (email && phone) {
        text.set(`Пользователь с электронной почтой ${email} и номером мобильного телефона ${phone} уже существует. Хотите обновить учетную запись пользователя?`);
      } else {
        text.set('Пользователь с указанной электронной почтой и номером мобильного телефона уже существует. Хотите обновить учетную запись пользователя?');
      }
    } else if (hasBadEmail) {
      if (email) {
        text.set(`Пользователь с электронной почтой ${email} уже существует. Хотите обновить учетную запись пользователя?`);
      } else {
        text.set('Пользователь с указанной электронной почтой уже существует. Хотите обновить учетную запись пользователя?');
      }
    } else if (hasBadPhone) {
      if (phone) {
        text.set(`Пользователь с номером мобильного телефона ${phone} уже существует. Хотите обновить учетную запись пользователя?`);
      } else {
        text.set('Пользователь с указанным номером мобильного телефона уже существует. Хотите обновить учетную запись пользователя?');
      }
    }
  }

  function handleHide() {
    show.set(false);
  }

  function handleClick(e) {
    e.stopPropagation();
  }
</script>

{#if $show}
  <div class="message__fade flex justify-center items-center" on:click={handleHide}>
    <div class="message" on:click={handleClick}>
      <div class="message__title flex flex-row justify-between items-center gap-4">
        <span>
          {#if $isBadEmail || $isBadPhone}
            Учетная запись существует
          {:else}
            Ошибка
          {/if}
        </span>
        <button class="btn message__btn" on:click={handleHide}>
          &times;
        </button>
      </div>
      <p>{$text}</p>
      <div class="flex flex-col md:flex-row justify-start items-start gap-4">
        {#if $isRegistration}
          <a href="{$loginUrl}" class="btn btn-main w-full md:w-auto" on:click={handleHide}>
            Войти
          </a>
          {#if $isBadEmail}
            <button class="btn text-extra w-full md:w-auto" on:click={handleHide}>
              Указать другой адрес
            </button>
          {:else if $isBadPhone}
            <button class="btn text-extra w-full md:w-auto" on:click={handleHide}>
              Указать другой номер
            </button>
          {:else}
            <button class="btn text-extra w-full md:w-auto" on:click={handleHide}>
              Отмена
            </button>
          {/if}
        {:else if $isUpdateProfile}
          <a href="{$loginUrl}" class="btn btn-main w-full md:w-auto" on:click={handleHide}>
            Обновить
          </a>
          <button class="btn text-extra w-full md:w-auto" on:click={handleHide}>
            Изменить данные
          </button>
        {:else}
          <button class="btn text-extra w-full md:w-auto" on:click={handleHide}>
            Спасибо
          </button>
        {/if}
      </div>
    </div>
  </div>

{/if}
