export default (function() {
  const isIframe = (location.search.indexOf('hiddenHeader%3Dtrue') > 0) || (location.search.indexOf('hiddenHeader=true') > 0);

  if (isIframe) {
    document.getElementById('page-header').style.display = 'none';
    document.getElementById('page-footer').style.display = 'none';
    document.getElementById('content').style.padding = '0';
  }
})();
