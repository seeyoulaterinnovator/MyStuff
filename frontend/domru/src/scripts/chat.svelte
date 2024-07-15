<script>
  import {onMount} from 'svelte';
  import {b2bChatWidgetUrl} from "../Cities/stores";
  import Cookie from "js-cookie";

  onMount(() => {
    const initialize = () => {
      b2bChatWidgetUrl.subscribe(src => {
        if (!window.B2B_CHAT_WIDGET_PARAMS && src) {
          window.B2B_CHAT_WIDGET_PARAMS = {
            userData: {
              citydomain: Cookie.get('city-domain') || 'interzet'
            }
          };
          const element = document.createElement('script');
          element.src = src;
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
  });
</script>
