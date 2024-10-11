<script>
  import { createEventDispatcher } from 'svelte';
  import CloseIcon from "../Icons/CloseIcon.svelte";

  const dispatch = createEventDispatcher();

  function handleClick() {
    dispatch('click');
  }
</script>

<button class="message__close-dialog-button highlighted-nested-hover-svg" on:click={handleClick}>
  <CloseIcon />
</button>
