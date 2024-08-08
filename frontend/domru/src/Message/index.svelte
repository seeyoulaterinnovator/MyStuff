<script>
  import {
    show,
    text,
    isRegistration,
    isUpdateProfile,
    isBadEmail,
    isBadPhone,
    loginUrl,
    registrationUrl,
    showInfo,
    isLimitExceeded,
    isPhoneError,
    isSecondSwitcher,
    isEmailVer, isLoginFailToRegistration
  } from './stores.js';
  import CloseButton from "../Common/Buttons/CloseButton.svelte";

  const hasRegistration = window.location.href.includes('registration');
  const hasUpdateProfile = window.location.href.includes('UPDATE_PROFILE');
  const alert = document.querySelector('.alert .text-accentRed');
  const hasAlert = !!alert;
  const hasBadEmail = hasAlert && alert.classList.contains('bad_email');
  const hasBadPhone = hasAlert && alert.classList.contains('bad_phone');
  const hasLimitCode = hasAlert && alert.classList.contains('limit-exceeded');
  const hasLoginFailToRegistration = hasAlert && alert.classList.contains('login-fail-to-registration');

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
  isLoginFailToRegistration.set(hasLoginFailToRegistration);
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

  // if (!document.getElementsByName('phone')[0]) {
  //   console.log('phone is ' + document.getElementsByName('phone')[0]);
  //   text.set("front Ваш E-mail успешно подтверждён!")
  // }

  function handleHide() {
    console.log("handleHide");
    show.set(false);
    if($isLoginFailToRegistration && $registrationUrl) {
      document.location = $registrationUrl;
    }
  }

  function handleHide2() {
    // if ($isEmailVer) {
      console.log("handleHide2")
      showInfo.set(false);
      closeAndSubmit();
    // }
  }

  setTimeout(handleHide2, 2300);

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

  function closeAndSubmit(e) {
    if(submitButton) {
      submitButton.click();
    } else if(e) {
      handleHide();
    }
  }

  function handleClick(e) {
    e.stopPropagation();
  }
</script>

{#if $show}
  <div class="message__fade flex justify-center items-center" on:click={handleHide}>
    <div class="message custom-pt-md custom-pb-lg custom-pl-md custom-pr-md {$isRegistration ? 'new-message' : 'message'}"
         on:click={handleClick}>
      <div class="message__title flex flex-row justify-between items-center gap-4 custom-mb-sm">
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
          <CloseButton on:click={clickSecondSwitcher}/>
        {:else}
          <CloseButton on:click={closeAndSubmit}/>
        {/if}
      </div>
      <p>{$text}</p>
      {#if $isLimitExceeded}

      {:else}
        <div class="flex flex-col md:flex-row justify-start items-start gap-4 custom-mt-md">
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
      <div class="message__title flex flex-row justify-between items-center gap-4 custom-mb-sm">
        <span>
          {#if $isEmailVer}
            Подтверждение
          {:else}
            Подтверждение данных
          {/if}
        </span>
        {#if $isEmailVer}
          <CloseButton on:click={closeAndSubmit}/>
        {:else}
          <CloseButton click={closeAndSubmit}/>
        {/if}
      </div>
      <p>{$text}</p>
    </div>
  </div>
{/if}
