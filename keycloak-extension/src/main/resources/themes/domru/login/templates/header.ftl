<#import "./svg.ftl" as svg>

<#macro defaultTemplate withCity=true>
  <header id="page-header" class="flex items-center">
    <div class="w-full flex justify-between items-start md:items-center lg:items-start">
      <a class="w-full max-w-200px" href="${(homePage)!"https://newlkb2b.dom.ru"}">
        <div class=" h-5 md:h-6 lg:h-6 bg-contain bg-no-repeat logo logo--domru"></div>
      </a>
      <div class="flex items-center">
        <div id="cities-button" data-city=""></div>
        <a href="${(phoneConstLink)!"tel:88005500479"}"
          class="highlighted-hover-text highlighted-nested-hover-svg custom-ml-md ${withCity ? string("phone--hidden-small phone-call-center", "phone--hidden-small phone-call-center")}">
          <div class="flex h-6 items-center">
            <@svg.phoneIcon />
            <span class="phone-number">
              ${(phoneConst)!"8 800 333 9000"}
            </span>
          </div>
        </a>
      </div>
    </div>
  </header>
</#macro>