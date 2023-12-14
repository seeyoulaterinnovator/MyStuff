<script>
  import ErChat from 'er-chat/dist/er-chat.esm';
  import {onMount} from 'svelte';
  import Cookie from 'js-cookie';

  // TODO: find way to use environment variables
  // TIP: `process` is NodeJS global variable
  let SVELTE_APP_ENVIRONMENT = 'develop';
  if (typeof process !== 'undefined') {
    SVELTE_APP_ENVIRONMENT = process.env.SVELTE_APP_ENVIRONMENT
  }

  let chatContent;
  let chatWrapper;
  let isOpen = false;
  let showChat = SVELTE_APP_ENVIRONMENT === 'production' && ( isFramed === undefined || isFramed === false);
  // let showChat = isFramed === undefined || isFramed === false;
  let test = isChatHidden;


  console.log("isFramed s " + isFramed);

  const toggleChat = () => {
    isOpen = !isOpen
  };

onMount(() => {
  const chat = new ErChat({
  nickname: 'Пользователь',
  subject: 'Вопросы со страницы авторизации',
  city: Cookie.get('city-domain') || 'yar',
  isProd: SVELTE_APP_ENVIRONMENT === 'production'
  // isProd: true
});
  chat.attach(chatContent);
});
</script>

{#if showChat && test!==null}
  <div class="er-chat er-chat-hidden" class:er-chat-hidden={!isOpen}>
    <div class="er-chat__header">
      <div class="er-chat__header__close" on:click={toggleChat} />
    </div>
    <div class="er-chat__content" bind:this={chatContent} />
  </div>
  {#if !isOpen}
    <div id="er-chat-label" class="er-chat-label" on:click={toggleChat}>
      <div class="er-chat-label__circle">
        <div class="er-chat-label__circle__icon" />
      </div>
      <div class="er-chat-label__text">Онлайн-консультант</div>
    </div>
  {/if}
{/if}
