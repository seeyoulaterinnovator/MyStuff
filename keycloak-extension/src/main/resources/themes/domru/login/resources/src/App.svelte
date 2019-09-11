<script>
  import Cities from './Cities/index.svelte';
  import CitiesButton from './Cities/Button.svelte';

  const cities = new Cities({
    target: document.getElementById('cities-modal'),
  });

  const citiesButton = new CitiesButton({
    target: document.getElementById('cities-button'),
  });
</script>
