<script>
  import ErChat from 'er-chat/dist/er-chat.esm';
  import {onMount} from 'svelte';
  import Cookie from 'js-cookie';

  let chatContent;
  let chatWrapper;
  let isOpen = false;
  let showChat = isFramed === undefined || isFramed === false;
  let test = isChatHidden;

  console.log("isFramed s " + isFramed);

  const toggleChat = () => {
    isOpen = !isOpen
  };

  onMount(() => {
    const chat = new ErChat({
      nickname: 'Пользователь',
      subject: 'Вопросы со страницы авторизации',
      city: Cookie.get('city-domain') || 'interzet',
      isProd: true
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
<div id="er-chat-label" class="flex flex-col justify-between er-chat-label" >
  <div class="flex justify-between w-full items-center flex-basis-auto label-header">
    <div class="flex flex-1 flex-col mr-6 circle">
      <div class="circle__icon" />
    </div>
    <span>Нужна помощь? <br>С удовольствием поможем!</span>
  </div>
  <div class="label__text flex-basis-auto w-full" on:click={toggleChat}>Связаться со специалистом &#8250;</div>
</div>
{/if}
{/if}
