<#import "./svg.ftl" as svg>

<#macro defaultTemplate withCity=true>
  <header id="page-header" class="flex items-center">
    <div class="w-full flex justify-between items-start md:items-center lg:items-start">
      <a class="w-full max-w-200px" href="${(homePage)!"https://my.2090909.ru"}">
        <div class=" h-8 md:h-10 lg:h-12 bg-contain bg-no-repeat logo logo--domru"></div>
      </a>
      <div class="flex items-center">
        <#--  TODO: Убрать связанный функционал при рефакторинге  -->
        <#--  <div id="cities-button" data-city=""></div>  -->
        <a href="${(phoneConstLink)!"tel:83832090909"}"
          class="highlighted-hover-text highlighted-nested-hover-svg custom-ml-md ${withCity ? string("phone--hidden-small phone-call-center", "phone--hidden-small phone-call-center")}">
          <div class="flex h-6 items-center">
            <@svg.phoneIcon />
            <span class="phone-number">
              ${(phoneConst)!"209 09 09"}
            </span>
          </div>
        </a>
      </div>
    </div>
  </header>
</#macro>