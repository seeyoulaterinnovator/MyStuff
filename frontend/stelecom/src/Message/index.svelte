<script>
  import {
    show,
    text,
    isRegistration,
    isUpdateProfile,
    isAccountExists,
    loginUrl,
  } from './stores.js';

  const alert = document.querySelector('.alert .text-accentRed');

  if (alert) {
    const hasBadEmail = alert.classList.contains('bad_email');
    const hasRegistration = window.location.href.includes('registration');
    const hasUpdateProfile = window.location.href.includes('UPDATE_PROFILE');
    isAccountExists.set(hasBadEmail);
    isRegistration.set(hasRegistration);
    isUpdateProfile.set(hasUpdateProfile);

    if (hasBadEmail && (hasRegistration || hasUpdateProfile)) {
      show.set(true);
      text.set(alert.innerText);
      alert.parentElement.remove();
    }
  }

  const email = document.getElementsByName('email')[0];

  if (email && email.value) {
    if ($isUpdateProfile) {
      text.set(`Пользователь с электронной почтой ${email.value} уже существует. Хотите обновить учетную запись пользователя?`);
    }

    email.value = '';
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
          {#if $isRegistration || $isUpdateProfile}
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
          {#if $isAccountExists}
            <button class="btn text-extra w-full md:w-auto" on:click={handleHide}>
              Указать другой адрес
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
