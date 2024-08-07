<script>
  import { beforeUpdate, afterUpdate } from 'svelte';
  import { onMount } from 'svelte';
  import Cookie from 'js-cookie';

  import axios from 'axios';

  import {
    status,
    city,
    showModal,
    editingStarted,
    allCities,
    quarter,
  } from './stores.js';
  import { STATUS } from './constants.js';

  import './selection';

  import * as citiesJson  from '../mock/cities.json'
  import {selectCity} from "./selection";

  let groupedCities = [];

  export let search;

  function handleClick(currentCity) {
      selectCity($allCities.find(obj => obj.name === currentCity.name));
  }

  function groupByFirstCharacter(arr) {
    if (!arr || !arr.length) return [];
    arr = arr.sort((a, b) => a.name.localeCompare(b.name));
    let quarterStore = 0;
    const unsubscribe = quarter.subscribe(value => {
      quarterStore = value;
    });
    if (quarterStore  < 1) quarter.set(Math.floor(arr.length / 4));
    let partCounter = 0;
    let currentQuarter = quarterStore;

    const groupedCitiesObject = arr.reduce((acc, value, index) => {
      let firstCharacter = value.name[0];

      if (!acc[partCounter]) acc.push([]);
      if (!acc[partCounter][firstCharacter]) {

        if ( index >= currentQuarter && partCounter < 3) {
          partCounter++;
          currentQuarter = currentQuarter + quarterStore;
        }
        if (!acc[partCounter]) acc.push([]);
        acc[partCounter][firstCharacter] = {
            firstCharacter,
            cities: [{ name: value.name, domain: !value.bss && value.city }]
        };
      } else {
        acc[partCounter][firstCharacter].cities.push({ name: value.name, domain: !value.bss && value.city });
      }

      return acc;
    }, []);

    return groupedCitiesObject.map(part => Object.values(part));
  }

  onMount(() => {
    const url = '/auth/realms/user/cities';

    axios
      .get(url)
      .then(response => {
        const respCities  = response.data.results.cities || []; //citiesJson.results.cities || [];
        const replacedCities = respCities.map(city => city.name === 'Холдинг' ? {
            ...city,
            name: 'Федеральный Клиент',
        } : city);
        allCities.set(replacedCities);

        groupedCities = groupByFirstCharacter($allCities);
        editingStarted.set(false);

      })
      .catch(error => console.error('Error:', error));
  });

  afterUpdate(() => {
    const displayingCities = $editingStarted
      ? $allCities.filter(cityObject => cityObject.name.toLowerCase().startsWith(search.toLowerCase()))
      : $allCities;
    groupedCities = groupByFirstCharacter(displayingCities);
  });
</script>

<ul class="flex flex-wrap flex-row cities-container w-full px-4">
    {#each groupedCities as groupPart}
      <ul class="flex flex-col cities-column">
      {#each groupPart as group}

        <ul class="flex mb-4 capital flex-col px-0 md:px-2 capital">
          <h2 class="city-group-letter capitalize custom-mb-sm sm:px-2">
            {group.firstCharacter}
          </h2>

           <ul class="flex flex-col">
           {#each group.cities as city}
              <li class="mb-2 sm:px-2 hover:bg-extra city">
                <button class="city text-left" on:click={() => handleClick(city)}>{city.name}</button>
              </li>
            {:else}
              <div />
            {/each}
            </ul>

        </ul>

      {/each}
      </ul>
    {:else}
      <p class="m-auto">Ничего не найдено. Пожалуйста, проверьте правильность написания города</p>
    {/each}
</ul>
