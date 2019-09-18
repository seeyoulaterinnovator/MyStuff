export default (function() {
  const isIframe = location.search.indexOf('iframe%3D1') > 0;

  if (isIframe) {
    document.getElementById('page-header').style.display = 'none';
    document.getElementById('content').style.padding = '0';
  }
})();
