<script>
  import { beforeUpdate, afterUpdate } from 'svelte';
  import { onMount } from 'svelte';
  import Cookie from 'js-cookie';

  import axios from 'axios';
  import MockAdapter from 'axios-mock-adapter';
  import citiesDB from '../mock/cities.json';

  import {
    status,
    city,
    showModal,
    editingStarted,
    allCities,
  } from './stores.js';
  import { STATUS } from './constants.js';

  let groupedCities = [];

  export let search;

  function handleClick(currentCity) {
    city.set(currentCity);
    Cookie.set('CITY', currentCity);
    status.set(STATUS.CONFIRMED);
    showModal.set(false);
    editingStarted.set(false);
  }

  function groupByFirstCharacter(arr) {
    const groupedCitiesObject = arr.reduce((acc, value) => {
      let firstCharacter = value.name[0];

      if (!acc[firstCharacter])
        acc[firstCharacter] = { firstCharacter, cities: [value.name] };
      else acc[firstCharacter].cities.push(value.name);

      return acc;
    }, []);

    return Object.values(groupedCitiesObject);
  }

  onMount(() => {
    const mock = new MockAdapter(axios);
    const url = '/cities';

    mock.onGet(url).reply(200, citiesDB);

    axios
      .get(url)
      .then(response => {
        allCities.set(response.data.cities);

        groupedCities = groupByFirstCharacter($allCities);
      })
      .catch(error => console.error('Error:', error));
  });

  afterUpdate(() => {
    const displayingCities = $editingStarted
      ? $allCities.filter(cityObject => cityObject.name.startsWith(search))
      : $allCities;
    groupedCities = groupByFirstCharacter(displayingCities);
  });
</script>

<ul
  class="flex flex-col overflow-x-hidden overflow-y-auto scrollable-container
  w-full h-full">
  {#each groupedCities as group}
    <li class="flex flex-col sm:flex-row mb-4 px-2">
      <h2 class="text-extra mr-2">{group.firstCharacter}</h2>
      <ul>
        {#each group.cities as city}
          <li class="mb-2 px-2">
            <button on:click={() => handleClick(city)}>{city}</button>
          </li>
        {:else}
          <div />
        {/each}
      </ul>
    </li>
  {:else}
    <p class="m-auto">Нет заданного города</p>
  {/each}
</ul>
