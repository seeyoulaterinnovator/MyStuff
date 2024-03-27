<script>
  import {
    show,
    text,
    isRegistration,
    isUpdateProfile,
    isBadEmail,
    isBadPhone,
    loginUrl,
    showInfo,
    isLimitExceeded,
    isPhoneError,
    isSecondSwitcher,
    isEmailVer
  } from './stores.js';
  import {showModal} from "../Cities/stores";

  const hasRegistration = window.location.href.includes('registration');
  const hasUpdateProfile = window.location.href.includes('UPDATE_PROFILE');
  const alert = document.querySelector('.alert .text-accentRed');
  const hasAlert = !!alert;
  const hasBadEmail = hasAlert && alert.classList.contains('bad_email');
  const hasBadPhone = hasAlert && alert.classList.contains('bad_phone');
  const hasLimitCode = hasAlert && alert.classList.contains('limit-exceeded');

  const info = document.querySelector('.alert .text-black');
  const hasInfo = !!info;
  const hasEmailVer = hasInfo && info.classList.contains('email-ver');
  const submitButton = document.getElementById("closeWindow");
  const switcher = document.getElementById("loginPasswordButton");
  const hasPhoneErrorMessage = hasAlert && alert.classList.contains('phone_error');
  const secondSwitcher = document.getElementById("smsLoginButton");
  const hasSecondSwitcher = secondSwitcher !== null;


  const newText = hasAlert ? alert.innerText : hasInfo ? info.innerText : '';
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
  showInfo.set(hasInfo);
  isBadEmail.set(hasBadEmail);
  isPhoneError.set(hasPhoneErrorMessage);
  isBadPhone.set(hasBadPhone);
  isRegistration.set(hasRegistration);
  isUpdateProfile.set(hasUpdateProfile);
  text.set(newText);
  isLimitExceeded.set(hasLimitCode);
  isSecondSwitcher.set(hasSecondSwitcher);
  isEmailVer.set(hasEmailVer)

  if (hasRegistration) {
    if (hasBadEmail && hasBadPhone) {
      text.set('Адрес электронной почты и номер мобильного телефона уже используется на другой учетной записи. Если Вы уже регистрировались, попробуйте войти в свою учетную запись, либо укажите другой адрес электронной почты и номер мобильного телефона.');
    } else if (hasBadEmail) {
      text.set('Адрес электронной почты уже используется на другой учетной записи. Если Вы уже регистрировались, попробуйте войти в свою учетную запись, либо укажите другой E-mail.');
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

  if (!document.getElementsByName('phone')[0]) {
    console.log('phone is ' + document.getElementsByName('phone')[0]);
    text.set("front Ваш E-mail успешно подтверждён!")
  }

  function handleHide() {
    show.set(false);
  }

  function handleHide2() {
    if ($isEmailVer) {
      showInfo.set(false);
    }
  }

  setTimeout(handleHide2, 2800);

  function switchToPassword() {
    if (switcher !== null) {
      switcher.click();
    } else {
      show.set(false);
    }
  }

  function clickSecondSwitcher() {
    secondSwitcher.click();
  }

  function closeAndSubmit() {
    submitButton.click();
  }

  function handleClick(e) {
    e.stopPropagation();
  }
</script>

{#if $show}
  <div class="message__fade flex justify-center items-center" on:click={handleHide}>
    <div class={$isRegistration ? 'message new-message' : 'message'} on:click={handleClick}>
      <div class="message__title flex flex-row justify-between items-center gap-4">
        <span>
          {#if $isBadEmail || $isBadPhone}
            Учетная запись существует
          {:else if $isLimitExceeded}
            Превышен лимит
          {:else}
            Ошибка
          {/if}
        </span>
        {#if $isSecondSwitcher && $isPhoneError}
          <button class="message__close-dialog-button" on:click={clickSecondSwitcher}>
            <svg width="14" height="13" viewBox="0 0 14 13" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path fill-rule="evenodd" clip-rule="evenodd"
                    d="M1.95959 0.540478C1.70575 0.286637 1.2942 0.286637 1.04036 0.540478C0.786515 0.794319 0.786515 1.20588 1.04036 1.45972L6.08074 6.5001L1.04036 11.5405C0.786515 11.7943 0.786515 12.2059 1.04036 12.4597C1.2942 12.7136 1.70575 12.7136 1.95959 12.4597L6.99998 7.41934L12.0404 12.4597C12.2942 12.7136 12.7058 12.7136 12.9596 12.4597C13.2134 12.2059 13.2134 11.7943 12.9596 11.5405L7.91921 6.5001L12.9596 1.45972C13.2134 1.20588 13.2134 0.79432 12.9596 0.540478C12.7058 0.286638 12.2942 0.286638 12.0404 0.540478L6.99998 5.58086L1.95959 0.540478Z"
                    fill="#16629A"/>
            </svg>
          </button>
        {:else}
          <button class="message__close-dialog-button" on:click={handleHide}>
            <svg width="14" height="13" viewBox="0 0 14 13" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path fill-rule="evenodd" clip-rule="evenodd"
                    d="M1.95959 0.540478C1.70575 0.286637 1.2942 0.286637 1.04036 0.540478C0.786515 0.794319 0.786515 1.20588 1.04036 1.45972L6.08074 6.5001L1.04036 11.5405C0.786515 11.7943 0.786515 12.2059 1.04036 12.4597C1.2942 12.7136 1.70575 12.7136 1.95959 12.4597L6.99998 7.41934L12.0404 12.4597C12.2942 12.7136 12.7058 12.7136 12.9596 12.4597C13.2134 12.2059 13.2134 11.7943 12.9596 11.5405L7.91921 6.5001L12.9596 1.45972C13.2134 1.20588 13.2134 0.79432 12.9596 0.540478C12.7058 0.286638 12.2942 0.286638 12.0404 0.540478L6.99998 5.58086L1.95959 0.540478Z"
                    fill="#16629A"/>
            </svg>
          </button>
        {/if}
      </div>
      <p>{$text}</p>
      {#if $isLimitExceeded}

      {:else}
        <div class="flex flex-col md:flex-row justify-start items-start gap-4" style="margin-top: 1rem">
          {#if $isRegistration}
            <a href="{$loginUrl}" class="btn btn-main w-full md:w-auto reg-button" on:click={handleHide}>
              Войти
            </a>
            {#if $isBadEmail}
              <button class="btn text-accentBlue w-full md:w-auto reg-button" on:click={handleHide}>
                Указать другой адрес
              </button>
            {:else if $isBadPhone}
              <button class="btn text-accentBlue w-full md:w-auto reg-button" on:click={handleHide}>
                Указать другой номер
              </button>
            {:else}
              <button class="btn text-accentBlue w-full md:w-auto reg-button" on:click={handleHide}>
                Отмена
              </button>
            {/if}
          {:else if $isUpdateProfile}
            <a href="{$loginUrl}" class="btn btn-main w-full md:w-auto" on:click={handleHide}>
              Обновить
            </a>
            <button class="btn text-accentBlue w-full md:w-auto" on:click={handleHide}>
              Изменить данные
            </button>
          {:else if $isPhoneError}
            <button class="btn btn-switcher.modified.modified" style="background-color: #C51F1F; color: #FFFFFF;"
                    on:click={switchToPassword}>
              Войти с помощью логина
            </button>
          {:else}
            <button class="btn btn-main btn-enter" on:click={handleHide}>
              Понятно
            </button>
          {/if}
        </div>
      {/if}
    </div>
  </div>
{:else if $showInfo}
  <div class="message__fade flex justify-center items-center" on:click={closeAndSubmit}>
    <div class="message" style="{$isEmailVer ? 'width: 390px' : 'width: 288px'}" on:click={closeAndSubmit}>
      <div class="message__title flex flex-row justify-between items-center gap-4">
        <span>
          {#if $isEmailVer}
            Подтверждение
          {:else}
            Подтверждение данных
          {/if}
        </span>
        {#if $isEmailVer}
          <button class="message__close-dialog-button" on:click={handleHide2}>
            <svg width="14" height="13" viewBox="0 0 14 13" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path fill-rule="evenodd" clip-rule="evenodd"
                    d="M1.95959 0.540478C1.70575 0.286637 1.2942 0.286637 1.04036 0.540478C0.786515 0.794319 0.786515 1.20588 1.04036 1.45972L6.08074 6.5001L1.04036 11.5405C0.786515 11.7943 0.786515 12.2059 1.04036 12.4597C1.2942 12.7136 1.70575 12.7136 1.95959 12.4597L6.99998 7.41934L12.0404 12.4597C12.2942 12.7136 12.7058 12.7136 12.9596 12.4597C13.2134 12.2059 13.2134 11.7943 12.9596 11.5405L7.91921 6.5001L12.9596 1.45972C13.2134 1.20588 13.2134 0.79432 12.9596 0.540478C12.7058 0.286638 12.2942 0.286638 12.0404 0.540478L6.99998 5.58086L1.95959 0.540478Z"
                    fill="#16629A"/>
            </svg>
          </button>
        {:else}
          <button class="message__close-dialog-button" on:click={handleHide2}>
            <svg width="14" height="13" viewBox="0 0 14 13" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path fill-rule="evenodd" clip-rule="evenodd"
                    d="M1.95959 0.540478C1.70575 0.286637 1.2942 0.286637 1.04036 0.540478C0.786515 0.794319 0.786515 1.20588 1.04036 1.45972L6.08074 6.5001L1.04036 11.5405C0.786515 11.7943 0.786515 12.2059 1.04036 12.4597C1.2942 12.7136 1.70575 12.7136 1.95959 12.4597L6.99998 7.41934L12.0404 12.4597C12.2942 12.7136 12.7058 12.7136 12.9596 12.4597C13.2134 12.2059 13.2134 11.7943 12.9596 11.5405L7.91921 6.5001L12.9596 1.45972C13.2134 1.20588 13.2134 0.79432 12.9596 0.540478C12.7058 0.286638 12.2942 0.286638 12.0404 0.540478L6.99998 5.58086L1.95959 0.540478Z"
                    fill="#16629A"/>
            </svg>
          </button>
        {/if}
      </div>
      <p>{$text}</p>
    </div>
  </div>
{/if}
