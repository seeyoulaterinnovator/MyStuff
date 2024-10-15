<script>
  import {onMount} from 'svelte';
  import {customConfig} from "../Config/stores";
  import Cookie from "js-cookie";

  onMount(() => {
    if(window.parent === window) {
      const initialize = () => {
        customConfig.subscribe(config => {
          if (!window.B2B_CHAT_WIDGET_PARAMS && config.isLoaded && config.b2bChatWidgetUrl) {
            window.B2B_CHAT_WIDGET_PARAMS = {
              server: config.b2bChatWidgetServer,
              userData: {
                citydomain: Cookie.get('city-domain') || 'interzet'
              }
            };
            const element = document.createElement('script');
            element.src = config.b2bChatWidgetUrl;
            document.head.appendChild(element);
          }
        });
      };
      if(document.readyState === 'complete') {
        initialize();
      } else {
        document.addEventListener('readystatechange', () => {
          if(document.readyState === 'complete') {
            initialize();
          }
        });
      }
    }
  });
</script>
