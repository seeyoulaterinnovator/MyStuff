(function (){
  const handler = () => {
    document.querySelectorAll('[flex-wrap-justify-content]').forEach(parent => {
      const isWrap = [...parent.children].map(child => child.getBoundingClientRect())
        .flatMap(({left, right}) => [left, right])
        .some((e, i, a) => e < a[i-1]);
      const justifyContent = parent.getAttribute('flex-wrap-justify-content');
      parent.style.justifyContent = isWrap ? justifyContent : '';
    });
  }
  window.addEventListener('resize', handler);
  document.addEventListener('readystatechange', handler);
})();
