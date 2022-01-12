<script>
  import Cities from './Cities/index.svelte';
  import CitiesButton from './Cities/Button.svelte';
  import Chat from './scripts/chat.svelte';
  import Message from './Message/index.svelte';

  const chat = new Chat({
    target: document.body,
  });

  const cities = new Cities({
    target: document.getElementById('cities-modal'),
  });

  const citiesButton = new CitiesButton({
    target: document.getElementById('cities-button'),
  });

  const message = new Message({
    target: document.getElementById('message-modal'),
  });
</script>
