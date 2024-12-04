import {writable} from "svelte/store";

export const customConfig = writable({
  isLoaded: false,
  b2bChatWidgetUrl: '',
  b2bChatWidgetServer: ''
});

fetch('/auth/realms/user/config-custom').then(response => response.json())
  .then(json => {
    customConfig.set({
      isLoaded: true,
      b2bChatWidgetUrl: json.results.b2bChatWidgetUrl,
      b2bChatWidgetServer: json.results.b2bChatWidgetServer
    });
  })
  .catch(error => {
    console.log(error);
  });
